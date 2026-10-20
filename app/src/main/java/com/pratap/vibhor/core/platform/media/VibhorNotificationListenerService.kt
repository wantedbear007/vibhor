package com.pratap.vibhor.core.platform.media

import android.service.notification.NotificationListenerService

/**
 * Exists only so the user can grant Vibhor notification access, which stock
 * Android also requires before an app may enumerate media sessions. The
 * service never reads or stores notifications and deliberately overrides no
 * notification callbacks.
 */
class VibhorNotificationListenerService : NotificationListenerService()
