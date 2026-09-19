# Keep the WebView JavaScript interface stable if one is added later.
-keepclassmembers class ai.opencode.android.** {
    @android.webkit.JavascriptInterface <methods>;
}
