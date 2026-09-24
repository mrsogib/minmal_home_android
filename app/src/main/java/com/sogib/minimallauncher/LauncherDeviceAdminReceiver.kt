package com.sogib.minimallauncher

import android.app.admin.DeviceAdminReceiver

/**
 * Does nothing but exist so Android will let this app call lockNow().
 * No data is read, stored, or sent anywhere via device-admin — it is used for
 * exactly one call, DevicePolicyManager.lockNow(), triggered only by the
 * double-tap gesture when the user has turned that feature on in Settings.
 */
class LauncherDeviceAdminReceiver : DeviceAdminReceiver()
