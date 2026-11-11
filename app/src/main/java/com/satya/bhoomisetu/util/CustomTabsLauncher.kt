package com.satya.bhoomisetu.util

import android.content.Context
import android.net.Uri
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent

/**
 * Opens a government portal in a Chrome Custom Tab -- deliberately NOT an
 * embedded WebView. A Custom Tab runs inside the device's actual installed
 * Chrome process, sending the same request a normal browser visit would.
 * An embedded WebView sends a distinctive "wv" User-Agent marker instead,
 * which meebhoomi.ap.gov.in was confirmed (via a real HTTP 403, not a
 * generic failure) to block. Whether the WebView is visible or hidden makes
 * no difference to the server -- only which underlying browser engine makes
 * the request does. Custom Tabs also cannot be scripted or have their DOM
 * read by this app, which is intentional: the user completes their own
 * login/captcha exactly as they would in a normal browser tab, and this app
 * never touches the portal's data directly.
 */
object CustomTabsLauncher {

    fun open(context: Context, url: String) {
        val colorScheme = CustomTabColorSchemeParams.Builder()
            .setToolbarColor(android.graphics.Color.parseColor("#1E5631")) // matches GreenPrimary in presentation/theme/Color.kt
            .build()

        val intent = CustomTabsIntent.Builder()
            .setDefaultColorSchemeParams(colorScheme)
            .setShowTitle(true)
            .build()

        try {
            intent.launchUrl(context, Uri.parse(url))
        } catch (e: Exception) {
            val browserIntent = android.content.Intent(android.content.Intent.ACTION_VIEW, Uri.parse(url)).apply {
                flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(browserIntent)
        }
    }
}
