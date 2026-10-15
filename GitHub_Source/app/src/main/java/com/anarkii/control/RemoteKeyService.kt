package com.anarkii.control

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent

class RemoteKeyService : AccessibilityService() {

    private var foregroundPackage: String? = null

    override fun onServiceConnected() {
        super.onServiceConnected()

        serviceInfo = serviceInfo.apply {
            flags = flags or AccessibilityServiceInfo.FLAG_REQUEST_FILTER_KEY_EVENTS
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val packageName = event?.packageName?.toString()

        if (!packageName.isNullOrBlank() &&
            packageName != "com.anarkii.control") {
            foregroundPackage = packageName
        }
    }

    override fun onKeyEvent(event: KeyEvent): Boolean {
        if (event.keyCode == KeyEvent.KEYCODE_MENU) {

            if (event.action == KeyEvent.ACTION_UP) {

                val solidBackground =
                    foregroundPackage != "com.spocky.projengmenu"

                val intent = Intent(
                    this,
                    MainActivity::class.java
                ).apply {

                    putExtra(
                        "SOLID_BACKGROUND",
                        solidBackground
                    )

                    addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK or
                                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                                Intent.FLAG_ACTIVITY_SINGLE_TOP
                    )
                }

                startActivity(intent)
            }

            return true
        }

        return super.onKeyEvent(event)
    }

    override fun onInterrupt() {
    }
}