/*
 * Android host affordance for the context usage view.
 *
 * The web UI's usage tooltip only opens on hover and its context tab is
 * desktop-gated, so the host adds a third tab to the mobile session tab bar
 * and also binds the header usage circle. The panel mirrors the repository's
 * context tab (`packages/app/src/components/session/session-context-tab.tsx`
 * and `session-context-breakdown.ts`) and uses the app's own translations,
 * synced into `assets/usage-i18n.json` at build time. No application code is
 * modified.
 */
(function () {
  if (window.__ocUsagePanel) return 'already installed'
  window.__ocUsagePanel = true

  var I18N = __USAGE_I18N__
  var COLORS = {
    system: 'var(--syntax-info, #6796e6)',
    user: 'var(--syntax-success, #73c991)',
    assistant: 'var(--syntax-property, #b180d7)',
    tool: 'var(--syntax-warning, #e5c07b)',
    other: 'var(--syntax-comment, #7f848e)',
  }

  function dict() {
    var lang = (document.documentElement.lang || navigator.language || 'en').toLowerCase()
    var base = lang.split('-')[0]
    return I18N[lang] || I18N[base] || I18N.en || {}
  }

  function t(key) {
    var d = dict()
    return d[key] || (I18N.en && I18N.en[key]) || key
  }

  function locale() {
    return document.documentElement.lang || navigator.language || 'en'
  }

  function fmtNumber(value) {
    if (value === null || value === undefined) return '—'
    try {
      return new Intl.NumberFormat(locale()).format(value)
    } catch (_) {
      return String(value)
    }
  }

  function fmtCost(value) {
    try {
      return new Intl.NumberFormat(locale(), { style: 'currency', currency: 'USD' }).format(value || 0)
    } catch (_) {
      return '$' + (value || 0)
    }
  }

  function fmtTime(value) {
    if (!value) return '—'
    try {
      return new Intl.DateTimeFormat(locale(), { dateStyle: 'medium', timeStyle: 'short' }).format(new Date(value))
    } catch (_) {
      return new Date(value).toLocaleString()
    }
  }

  function tokenTotal(msg) {
    var t = (msg && msg.tokens) || {}
    var cache = t.cache || {}
    return (t.input || 0) + (t.output || 0) + (t.reasoning || 0) + (cache.read || 0) + (cache.write || 0)
  }

  function sessionID() {
    var match = location.pathname.match(/\/session\/([^/]+)\/?$/)
    return match ? match[1] : null
  }

  function unwrap(entry) {
    return entry && entry.info ? entry.info : entry
  }

  // Mirrors `estimateSessionContextBreakdown`.
  function breakdown(messages, partsByMessage, input, systemPrompt) {
    if (!input) return []
    var charsFromUserPart = function (part) {
      if (!part) return 0
      if (part.type === 'text') return (part.text || '').length
      if (part.type === 'file') return (part.source && part.source.text && part.source.text.value || '').length
      if (part.type === 'agent') return (part.source && part.source.value || '').length
      return 0
    }
    var charsFromAssistantPart = function (part) {
      if (!part) return { assistant: 0, tool: 0 }
      if (part.type === 'text' || part.type === 'reasoning') return { assistant: (part.text || '').length, tool: 0 }
      if (part.type !== 'tool') return { assistant: 0, tool: 0 }
      var state = part.state || {}
      var toolChars = Object.keys(state.input || {}).length * 16
      if (state.status === 'pending') return { assistant: 0, tool: toolChars + ((state.raw || '').length) }
      if (state.status === 'completed') return { assistant: 0, tool: toolChars + ((state.output || '').length) }
      if (state.status === 'error') return { assistant: 0, tool: toolChars + ((state.error || '').length) }
      return { assistant: 0, tool: toolChars }
    }
    var counts = { system: (systemPrompt || '').length, user: 0, assistant: 0, tool: 0 }
    for (var i = 0; i < messages.length; i++) {
      var message = unwrap(messages[i])
      var parts = partsByMessage[message.id] || []
      if (message.role === 'user') {
        counts.user += parts.reduce(function (sum, part) { return sum + charsFromUserPart(part) }, 0)
        continue
      }
      if (message.role !== 'assistant') continue
      var sums = parts.reduce(
        function (acc, part) {
          var next = charsFromAssistantPart(part)
          return { assistant: acc.assistant + next.assistant, tool: acc.tool + next.tool }
        },
        { assistant: 0, tool: 0 },
      )
      counts.assistant += sums.assistant
      counts.tool += sums.tool
    }
    var estimate = function (chars) { return Math.ceil(chars / 4) }
    var tokens = {
      system: estimate(counts.system),
      user: estimate(counts.user),
      assistant: estimate(counts.assistant),
      tool: estimate(counts.tool),
    }
    var estimated = tokens.system + tokens.user + tokens.assistant + tokens.tool
    var scaled
    var other
    if (estimated <= input) {
      scaled = tokens
      other = input - estimated
    } else {
      var scale = input / estimated
      scaled = {
        system: Math.floor(tokens.system * scale),
        user: Math.floor(tokens.user * scale),
        assistant: Math.floor(tokens.assistant * scale),
        tool: Math.floor(tokens.tool * scale),
      }
      other = Math.max(0, input - (scaled.system + scaled.user + scaled.assistant + scaled.tool))
    }
    var all = [
      { key: 'system', tokens: scaled.system },
      { key: 'user', tokens: scaled.user },
      { key: 'assistant', tokens: scaled.assistant },
      { key: 'tool', tokens: scaled.tool },
      { key: 'other', tokens: other },
    ]
    return all
      .filter(function (segment) { return segment.tokens > 0 })
      .map(function (segment) {
        return {
          key: segment.key,
          tokens: segment.tokens,
          width: (segment.tokens / input) * 100,
          percent: Math.round((segment.tokens / input) * 1000) / 10,
        }
      })
  }

  function collect() {
    var id = sessionID()
    if (!id) return Promise.resolve(null)
    return Promise.all([
      fetch('/session/' + id).then(function (r) { return (r.ok ? r.json() : null) }).catch(function () { return null }),
      fetch('/session/' + id + '/message').then(function (r) { return (r.ok ? r.json() : []) }).catch(function () { return [] }),
      fetch('/config/providers').then(function (r) { return (r.ok ? r.json() : null) }).catch(function () { return null }),
    ]).then(function (results) {
      var session = results[0] || {}
      var raw = results[1] || []
      var providers = (results[2] && results[2].providers) || []
      var messages = raw.map(unwrap).filter(Boolean)
      var partsByMessage = {}
      for (var i = 0; i < raw.length; i++) {
        var info = unwrap(raw[i])
        if (info && raw[i].parts) partsByMessage[info.id] = raw[i].parts
      }
      var user = 0
      var assistant = 0
      var last = null
      var systemPrompt
      for (var j = 0; j < messages.length; j++) {
        var m = messages[j]
        if (m.role === 'user') {
          user++
          if (m.system) systemPrompt = m.system
        }
        if (m.role === 'assistant') {
          assistant++
          if (tokenTotal(m) > 0) last = m
        }
      }
      var provider = last ? providers.find(function (p) { return p.id === last.providerID }) : null
      var model = provider && last ? (provider.models || {})[last.modelID] : null
      var limit = model && model.limit ? model.limit.context : null
      var tokens = (last && last.tokens) || {}
      var cache = tokens.cache || {}
      var total = last ? tokenTotal(last) : 0
      var input = (tokens.input || 0) + (cache.read || 0) + (cache.write || 0)
      return {
        directory: session.directory,
        title: session.title || '—',
        messages: messages.length,
        user: user,
        assistant: assistant,
        provider: (provider && (provider.name || provider.id)) || (last && last.providerID) || '—',
        model: (model && model.name) || (last && last.modelID) || '—',
        limit: limit,
        total: total,
        input: tokens.input || 0,
        output: tokens.output || 0,
        reasoning: tokens.reasoning || 0,
        cacheRead: cache.read || 0,
        cacheWrite: cache.write || 0,
        usage: limit ? Math.round((total / limit) * 100) : null,
        cost: session.cost || 0,
        created: session.time && session.time.created,
        updated: session.time && session.time.updated,
        breakdown: breakdown(raw, partsByMessage, input, systemPrompt),
      }
    })
  }

  var sheet = null

  function stat(label, value) {
    var cell = document.createElement('div')
    cell.style.cssText = 'display:flex;flex-direction:column;gap:3px;min-width:0'
    var name = document.createElement('div')
    name.style.cssText = 'font-size:12px;opacity:.6'
    name.textContent = label
    var val = document.createElement('div')
    val.style.cssText = 'font-size:13px;font-weight:600;word-break:break-word'
    val.textContent = value
    cell.appendChild(name)
    cell.appendChild(val)
    return cell
  }

  function ensurePanel() {
    if (sheet) return sheet
    sheet = document.createElement('div')
    sheet.id = 'oc-usage-panel'
    sheet.style.cssText = [
      'position:fixed',
      'inset:0',
      'z-index:2147483647',
      'background:var(--v2-background-bg-base,#111)',
      'color:var(--v2-text-text-base,#eee)',
      'display:none',
      'flex-direction:column',
      'padding:calc(14px + env(safe-area-inset-top)) 16px calc(14px + env(safe-area-inset-bottom))',
    ].join(';')

    var header = document.createElement('div')
    header.style.cssText = 'display:flex;align-items:center;justify-content:space-between;gap:12px;padding-bottom:10px;flex:0 0 auto'
    var title = document.createElement('strong')
    title.style.cssText = 'font-size:15px'
    title.textContent = t('context.usage.view')
    var close = document.createElement('button')
    close.style.cssText = 'all:unset;cursor:pointer;padding:6px 10px;opacity:.75;font-size:16px'
    close.textContent = '✕'
    close.addEventListener('click', function () {
      sheet.style.display = 'none'
      markTab(false)
    })
    header.appendChild(title)
    header.appendChild(close)

    var body = document.createElement('div')
    body.id = 'oc-usage-body'
    body.style.cssText = 'flex:1 1 auto;overflow-y:auto;font-size:13px'
    sheet.appendChild(header)
    sheet.appendChild(body)
    document.body.appendChild(sheet)
    return sheet
  }

  function markTab(selected) {
    var button = document.querySelector('[data-value="oc-usage"]')
    if (!button) return
    if (selected) button.setAttribute('data-selected', '')
    else button.removeAttribute('data-selected')
  }

  function render(body, data) {
    body.textContent = ''
    if (!data) {
      body.appendChild(stat('—', '—'))
      return
    }
    var bar = document.createElement('div')
    bar.style.cssText = 'height:6px;border-radius:3px;background:rgba(255,255,255,.14);overflow:hidden;margin-bottom:14px'
    var fill = document.createElement('div')
    var pct = data.usage === null ? 0 : Math.max(0, Math.min(100, data.usage))
    fill.style.cssText = 'height:100%;width:' + pct + '%;background:var(--v2-icon-icon-base,#8b8b8b)'
    bar.appendChild(fill)
    body.appendChild(bar)

    var grid = document.createElement('div')
    grid.style.cssText = 'display:grid;grid-template-columns:repeat(auto-fit,minmax(150px,1fr));gap:12px 16px'
    var stats = [
      [t('context.stats.session'), data.title],
      [t('context.stats.messages'), fmtNumber(data.messages)],
      [t('context.stats.provider'), data.provider],
      [t('context.stats.model'), data.model],
      [t('context.stats.limit'), fmtNumber(data.limit)],
      [t('context.stats.totalTokens'), fmtNumber(data.total)],
      [t('context.stats.usage'), data.usage === null ? '—' : data.usage + '%'],
      [t('context.stats.inputTokens'), fmtNumber(data.input)],
      [t('context.stats.outputTokens'), fmtNumber(data.output)],
      [t('context.stats.reasoningTokens'), fmtNumber(data.reasoning)],
      [t('context.stats.cacheTokens'), fmtNumber(data.cacheRead) + ' / ' + fmtNumber(data.cacheWrite)],
      [t('context.stats.userMessages'), fmtNumber(data.user)],
      [t('context.stats.assistantMessages'), fmtNumber(data.assistant)],
      [t('context.stats.totalCost'), fmtCost(data.cost)],
      [t('context.stats.sessionCreated'), fmtTime(data.created)],
      [t('context.stats.lastActivity'), fmtTime(data.updated)],
    ]
    for (var i = 0; i < stats.length; i++) grid.appendChild(stat(stats[i][0], stats[i][1]))
    body.appendChild(grid)

    if (data.breakdown && data.breakdown.length) {
      var section = document.createElement('div')
      section.style.cssText = 'display:flex;flex-direction:column;gap:8px;margin-top:22px'
      var label = document.createElement('div')
      label.style.cssText = 'font-size:12px;opacity:.6'
      label.textContent = t('context.breakdown.title')
      var stack = document.createElement('div')
      stack.style.cssText = 'height:8px;border-radius:4px;background:rgba(255,255,255,.12);overflow:hidden;display:flex'
      var legend = document.createElement('div')
      legend.style.cssText = 'display:flex;flex-wrap:wrap;gap:4px 14px'
      for (var k = 0; k < data.breakdown.length; k++) {
        var segment = data.breakdown[k]
        var part = document.createElement('div')
        part.style.cssText = 'height:100%;width:' + segment.width + '%;background:' + COLORS[segment.key]
        stack.appendChild(part)
        var item = document.createElement('div')
        item.style.cssText = 'display:flex;align-items:center;gap:6px;font-size:12px;opacity:.75'
        var dot = document.createElement('span')
        dot.style.cssText = 'width:8px;height:8px;border-radius:2px;background:' + COLORS[segment.key]
        var text = document.createElement('span')
        text.textContent = t('context.breakdown.' + segment.key)
        var percent = document.createElement('span')
        percent.style.cssText = 'opacity:.7'
        percent.textContent = fmtNumber(segment.percent) + '%'
        item.appendChild(dot)
        item.appendChild(text)
        item.appendChild(percent)
        legend.appendChild(item)
      }
      section.appendChild(label)
      section.appendChild(stack)
      section.appendChild(legend)
      body.appendChild(section)
    }
  }

  async function open() {
    var panel = ensurePanel()
    var body = panel.querySelector('#oc-usage-body')
    panel.style.display = 'flex'
    markTab(true)
    body.textContent = ''
    body.appendChild(stat('…', ''))
    var data = await collect()
    render(body, data)
  }

  var filesRoot = null
  var filesPath = ''

  function filesFetch(path) {
    var query = '/file?path=' + encodeURIComponent(path)
    if (filesRoot) query += '&directory=' + encodeURIComponent(filesRoot)
    return fetch(query).then(function (r) {
      if (!r.ok) throw new Error('HTTP ' + r.status)
      return r.json()
    })
  }

  function fileContent(path) {
    var query = '/file/content?path=' + encodeURIComponent(path)
    if (filesRoot) query += '&directory=' + encodeURIComponent(filesRoot)
    return fetch(query).then(function (r) {
      if (!r.ok) throw new Error('HTTP ' + r.status)
      return r.json()
    })
  }

  function icon(folder) {
    var span = document.createElement('span')
    span.style.cssText = 'flex:0 0 auto;width:16px;height:16px;opacity:.65'
    span.innerHTML = folder
      ? '<svg viewBox="0 0 16 16" width="16" height="16" fill="none" stroke="currentColor" stroke-width="1.2"><path d="M1.5 4.5A1 1 0 0 1 2.5 3.5h3l1.2 1.4h5.8a1 1 0 0 1 1 1v6.6a1 1 0 0 1-1 1h-10a1 1 0 0 1-1-1z"/></svg>'
      : '<svg viewBox="0 0 16 16" width="16" height="16" fill="none" stroke="currentColor" stroke-width="1.2"><path d="M4 1.8h5l3 3v9.4H4z"/><path d="M9 1.8v3h3"/></svg>'
    return span
  }

  function filesPanel() {
    var root = document.getElementById('oc-files-panel')
    if (root) return root
    root = document.createElement('div')
    root.id = 'oc-files-panel'
    root.style.cssText = [
      'position:fixed',
      'inset:0',
      'z-index:2147483647',
      'background:var(--v2-background-bg-base,#111)',
      'color:var(--v2-text-text-base,#eee)',
      'display:none',
      'flex-direction:column',
      'padding:calc(14px + env(safe-area-inset-top)) 16px calc(14px + env(safe-area-inset-bottom))',
    ].join(';')

    var header = document.createElement('div')
    header.style.cssText = 'display:flex;align-items:center;gap:8px;padding-bottom:10px;flex:0 0 auto'
    var title = document.createElement('strong')
    title.style.cssText = 'font-size:15px;flex:1 1 auto'
    title.textContent = t('palette.group.files')
    var up = document.createElement('button')
    up.textContent = '↑'
    up.setAttribute('aria-label', t('dialog.directory.parent'))
    up.style.cssText = 'all:unset;cursor:pointer;padding:6px 10px;opacity:.75;font-size:16px'
    up.addEventListener('click', function () {
      if (!filesPath) return
      var parts = filesPath.replace(/\/$/, '').split('/')
      parts.pop()
      filesPath = parts.length ? parts.join('/') + '/' : ''
      void loadFiles()
    })
    var close = document.createElement('button')
    close.textContent = '✕'
    close.style.cssText = 'all:unset;cursor:pointer;padding:6px 10px;opacity:.75;font-size:16px'
    close.addEventListener('click', function () {
      root.style.display = 'none'
      markTabValue('oc-files', false)
    })
    header.appendChild(title)
    header.appendChild(up)
    header.appendChild(close)

    var crumbs = document.createElement('div')
    crumbs.id = 'oc-files-path'
    crumbs.style.cssText = 'font-size:12px;opacity:.55;padding-bottom:8px;word-break:break-all;flex:0 0 auto'

    var body = document.createElement('div')
    body.id = 'oc-files-body'
    body.style.cssText = 'flex:1 1 auto;overflow-y:auto;font-size:13px'

    root.appendChild(header)
    root.appendChild(crumbs)
    root.appendChild(body)
    document.body.appendChild(root)
    return root
  }

  function loadFiles() {
    var panel = filesPanel()
    var body = panel.querySelector('#oc-files-body')
    var crumbs = panel.querySelector('#oc-files-path')
    crumbs.textContent = '/' + filesPath
    body.textContent = ''
    var loading = document.createElement('div')
    loading.style.cssText = 'opacity:.6;padding:8px 0'
    loading.textContent = '…'
    body.appendChild(loading)
    filesFetch(filesPath)
      .then(function (entries) {
        body.textContent = ''
        var list = (entries || []).slice().sort(function (a, b) {
          if (a.type !== b.type) return a.type === 'directory' ? -1 : 1
          return String(a.name).localeCompare(String(b.name))
        })
        if (!list.length) {
          var empty = document.createElement('div')
          empty.style.cssText = 'opacity:.6;padding:8px 0'
          empty.textContent = '—'
          body.appendChild(empty)
          return
        }
        for (var i = 0; i < list.length; i++) {
          ;(function (entry) {
            var row = document.createElement('button')
            row.type = 'button'
            row.style.cssText =
              'all:unset;cursor:pointer;display:flex;align-items:center;gap:10px;width:100%;padding:11px 4px;border-bottom:1px solid rgba(255,255,255,.06);box-sizing:border-box'
            row.appendChild(icon(entry.type === 'directory'))
            var name = document.createElement('span')
            name.style.cssText = 'flex:1 1 auto;min-width:0;overflow:hidden;text-overflow:ellipsis;white-space:nowrap'
            name.textContent = entry.name + (entry.type === 'directory' ? '/' : '')
            row.appendChild(name)
            if (entry.ignored) {
              var ignored = document.createElement('span')
              ignored.style.cssText = 'flex:0 0 auto;font-size:11px;opacity:.4'
              ignored.textContent = '•'
              row.appendChild(ignored)
            }
            row.addEventListener('click', function () {
              if (entry.type === 'directory') {
                filesPath = String(entry.path || '').replace(/\\/g, '/')
                if (filesPath && !filesPath.endsWith('/')) filesPath += '/'
                void loadFiles()
                return
              }
              void openPreview(entry)
            })
            body.appendChild(row)
          })(list[i])
        }
      })
      .catch(function (error) {
        body.textContent = ''
        var failed = document.createElement('div')
        failed.style.cssText = 'opacity:.7;padding:8px 0'
        failed.textContent = String(error && error.message ? error.message : error)
        body.appendChild(failed)
      })
  }

  function openPreview(entry) {
    var panel = filesPanel()
    var body = panel.querySelector('#oc-files-body')
    body.textContent = ''
    var back = document.createElement('button')
    back.type = 'button'
    back.style.cssText = 'all:unset;cursor:pointer;padding:8px 0;opacity:.75;font-size:13px'
    back.textContent = '← ' + (entry.path || entry.name)
    back.addEventListener('click', function () {
      void loadFiles()
    })
    var pre = document.createElement('pre')
    pre.style.cssText =
      'white-space:pre-wrap;word-break:break-word;font-size:12px;line-height:1.5;margin:6px 0 0;opacity:.9'
    pre.textContent = '…'
    body.appendChild(back)
    body.appendChild(pre)
    fileContent(entry.path)
      .then(function (result) {
        pre.textContent = result && result.type === 'text' ? result.content : '[' + ((result && result.type) || 'unknown') + ']'
      })
      .catch(function (error) {
        pre.textContent = String(error && error.message ? error.message : error)
      })
  }

  function openFiles() {
    filesPanel()
    if (!filesRoot) {
      collect().then(function (data) {
        filesRoot = (data && data.directory) || filesRoot
        filesPath = ''
        var panel = filesPanel()
        panel.style.display = 'flex'
        markTabValue('oc-files', true)
        loadFiles()
      })
      return
    }
    filesPanel().style.display = 'flex'
    markTabValue('oc-files', true)
    filesPath = ''
    loadFiles()
  }

  function markTabValue(value, selected) {
    var button = document.querySelector('[data-value="' + value + '"]')
    if (!button) return
    if (selected) button.setAttribute('data-selected', '')
    else button.removeAttribute('data-selected')
  }

  function addHostTab(list, reference, value, label, onClick) {
    var button = document.createElement('button')
    button.type = 'button'
    button.setAttribute('role', 'tab')
    button.setAttribute('data-value', value)
    button.setAttribute('data-slot', 'tabs-trigger')
    button.setAttribute('data-orientation', 'horizontal')
    button.className = reference ? reference.className : ''
    button.textContent = label
    button.addEventListener('click', function (event) {
      event.preventDefault()
      event.stopPropagation()
      onClick()
    })
    list.appendChild(button)
  }

  function makeTab() {
    var list = null
    var lists = document.querySelectorAll('[data-slot="tabs-list"]')
    for (var i = 0; i < lists.length; i++) {
      if (lists[i].querySelector('[data-value="changes"]')) {
        list = lists[i]
        break
      }
    }
    if (!list) return
    if (list.querySelector('[data-value="oc-usage"]')) return
    var reference = list.querySelector('[data-value="changes"]')
    addHostTab(list, reference, 'oc-usage', t('context.stats.usage'), function () {
      void open()
    })
    addHostTab(list, reference, 'oc-files', t('palette.group.files'), function () {
      void openFiles()
    })
  }

  function bindCircle() {
    document.addEventListener(
      'click',
      function (event) {
        var target = event.target instanceof Element ? event.target : null
        if (!target) return
        var trigger = target.closest('[data-component="tooltip-v2-trigger"]')
        if (!trigger || !trigger.querySelector('svg circle')) return
        event.preventDefault()
        event.stopPropagation()
        void open()
      },
      true,
    )
  }

  function observe() {
    makeTab()
    bindCircle()
    new MutationObserver(makeTab).observe(document.body, { childList: true, subtree: true })
    window.addEventListener('popstate', makeTab)
  }

  if (document.readyState === 'loading') document.addEventListener('DOMContentLoaded', observe)
  else observe()
  return 'usage panel installed'
})()
