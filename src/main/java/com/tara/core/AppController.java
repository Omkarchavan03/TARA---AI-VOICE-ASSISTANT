package com.tara.core;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

public class AppController {

    public static void openApp(String appPath) {
        try {
            Runtime.getRuntime().exec("cmd /c start \"\" " + appPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void closeApp(String processName) {
        try {
            Runtime.getRuntime().exec(
                    "taskkill /IM " + processName + ".exe /F"
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void killProcesses(String... processNames) {
        for (String name : processNames) {
            try {
                Runtime.getRuntime().exec(
                        "taskkill /IM " + name + ".exe /F"
                );
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void openWebsite(String url) {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(new URI(url));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void searchWeb(String query) {
        try {
            String encoded = URLEncoder.encode(query, StandardCharsets.UTF_8);
            openWebsite("https://www.google.com/search?q=" + encoded);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* =====================================================
       🔥 ADDED FEATURES (NO REMOVALS ABOVE)
       ===================================================== */

    public static void closeAllUserApps() {
        List<String> commonApps = Arrays.asList(
                "chrome", "msedge", "firefox",
                "notepad", "calc", "mspaint",
                "explorer", "Code", "taskmgr"
        );

        for (String app : commonApps) {
            try {
                Runtime.getRuntime().exec(
                        "taskkill /IM " + app + ".exe /F"
                );
            } catch (IOException ignored) {}
        }
    }

    public static void openNewBrowserTab() {
        try {
            Runtime.getRuntime().exec("cmd /c start chrome");
        } catch (IOException ignored) {}
    }

    public static void closeBrowserTab() {
        try {
            Runtime.getRuntime().exec(
                    "cmd /c powershell -command " +
                    "\"$w = New-Object -ComObject wscript.shell; $w.SendKeys('^w')\""
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /* =====================================================
       🔥 WRITE / TYPE SUPPORT (NEW)
       ===================================================== */

    public static void typeText(String text) {
        try {
            if (text == null || text.isBlank()) return;

            String safe = text.replace("\"", "`\"");

            Runtime.getRuntime().exec(
                    "cmd /c powershell -command " +
                    "\"$w = New-Object -ComObject wscript.shell; " +
                    "$w.SendKeys(\\\"" + safe + "\\\")\""
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
