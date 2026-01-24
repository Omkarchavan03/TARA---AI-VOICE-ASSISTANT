package com.tara.core;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;

public class AppController {

    /** 
     * Open an application by its executable path.
     */
    public static void openApp(String appPath) {
        try {
            System.out.println("[AppController] Opening app: " + appPath);
            Runtime.getRuntime().exec("cmd /c start \"\" \"" + appPath + "\"");
        } catch (IOException e) {
            System.err.println("[AppController] Failed to open app: " + appPath);
            e.printStackTrace();
        }
    }

    /** 
     * Close an application by its executable name (without .exe)
     */
    public static void closeApp(String appName) {
        try {
            System.out.println("[AppController] Closing app: " + appName);
            Runtime.getRuntime().exec("taskkill /IM " + appName + ".exe /F");
        } catch (IOException e) {
            System.err.println("[AppController] Failed to close app: " + appName);
            e.printStackTrace();
        }
    }

    /** 
     * Kill multiple processes by their executable names.
     */
    public static void killProcesses(String... processNames) {
        for (String name : processNames) {
            try {
                System.out.println("[AppController] Killing process: " + name);
                Runtime.getRuntime().exec("taskkill /IM " + name + ".exe /F");
            } catch (IOException e) {
                System.err.println("[AppController] Failed to kill process: " + name);
                e.printStackTrace();
            }
        }
    }

    /** 
     * Open a website in the default browser.
     */
    public static void openWebsite(String url) {
        try {
            System.out.println("[AppController] Opening website: " + url);
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(url));
            } else {
                System.err.println("[AppController] Desktop not supported. Cannot open website.");
            }
        } catch (Exception e) {
            System.err.println("[AppController] Failed to open website: " + url);
            e.printStackTrace();
        }
    }
}
