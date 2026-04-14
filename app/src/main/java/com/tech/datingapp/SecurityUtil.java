package com.tech.datingapp;

import android.os.Build;
import java.io.File;

public class SecurityUtil {
    public static boolean isHackerDevice() {
        // 1. Root Check (Modders use rooted devices)
        String[] paths = {"/system/app/Superuser.apk", "/sbin/su", "/system/bin/su", "/data/local/su"};
        for (String path : paths) {
            if (new File(path).exists()) return true;
        }
        // 2. Emulator Check (Modders use PC emulators)
        if (Build.MODEL.contains("Emulator") || Build.FINGERPRINT.startsWith("generic") || Build.MANUFACTURER.contains("Genymotion")) {
            return true;
        }
        return false;
    }
}
