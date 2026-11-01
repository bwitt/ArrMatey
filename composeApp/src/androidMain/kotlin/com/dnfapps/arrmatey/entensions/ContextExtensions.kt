package com.dnfapps.arrmatey.entensions

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri
import com.dnfapps.arrmatey.shared.*
import com.dnfapps.arrmatey.shared.MR
import dev.icerock.moko.resources.desc.Resource
import dev.icerock.moko.resources.desc.StringDesc

fun Context.openLink(url: String) {
    val uri = url.toUri()
    try {
        val customTabIntent =
            CustomTabsIntent
                .Builder()
                .setShowTitle(true)
                .setInstantAppsEnabled(true)
                .build()
        customTabIntent.intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        customTabIntent.launchUrl(this, uri)
    } catch (e: Exception) {
        val intent =
            Intent(Intent.ACTION_VIEW, uri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        startActivityOrNotify(intent)
    }
}

fun Context.openAppSettings() {
    val intent =
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    startActivityOrNotify(intent)
}

fun Context.tryStartActivity(intent: Intent): Boolean = try {
    startActivity(intent)
    true
} catch (e: ActivityNotFoundException) {
    false
}

fun Context.startActivityOrNotify(intent: Intent) {
    if (!tryStartActivity(intent)) {
        Toast.makeText(this, StringDesc.Resource(MR.strings.no_app_to_open).toString(this), Toast.LENGTH_SHORT).show()
    }
}
