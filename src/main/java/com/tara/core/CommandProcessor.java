package com.tara.core;

import com.tara.tts.Speaker;

import java.util.HashMap;
import java.util.Map;

public class CommandProcessor {

    private static final Map<String, String> APP_COMMANDS = new HashMap<>();
    private static final Map<String, String> WEBSITE_COMMANDS = new HashMap<>();

    static {
        // Apps
        APP_COMMANDS.put("chrome", "C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe");
        APP_COMMANDS.put("vscode", "C:\\Users\\Dell\\AppData\\Local\\Programs\\Microsoft VS Code\\Code.exe");
        APP_COMMANDS.put("vs code", "C:\\Users\\Dell\\AppData\\Local\\Programs\\Microsoft VS Code\\Code.exe");

        // Websites
        WEBSITE_COMMANDS.put("youtube", "https://www.youtube.com");
        WEBSITE_COMMANDS.put("google", "https://www.google.com");
        WEBSITE_COMMANDS.put("github", "https://www.github.com");
    }

    /**
     * Process a command string. Returns true if a command matched, false otherwise.
     * Works with normalized text from STT.
     */
    public static boolean process(String command, Speaker tts) {
        if (command == null || command.isEmpty()) return false;

        command = command.toLowerCase().replaceAll("[^a-z0-9 ]", "").trim();

        // Open apps
        for (Map.Entry<String, String> entry : APP_COMMANDS.entrySet()) {
            if (command.contains(entry.getKey())) {
                tts.speak("Opening " + capitalize(entry.getKey()) + " for you");
                AppController.openApp(entry.getValue());
                return true;
            }
        }

        // Open websites
        for (Map.Entry<String, String> entry : WEBSITE_COMMANDS.entrySet()) {
            if (command.contains(entry.getKey())) {
                tts.speak("Opening " + capitalize(entry.getKey()) + " for you");
                AppController.openWebsite(entry.getValue());
                return true;
            }
        }

        // Close apps
        if (command.contains("close chrome")) {
            tts.speak("Closing Chrome");
            AppController.closeApp("chrome");
            return true;
        }

        // Kill terminals / command prompts
        if (command.contains("kill terminal") || command.contains("close terminal")) {
            tts.speak("Closing all terminals");
            AppController.killProcesses("cmd", "powershell", "wt");
            return true;
        }

        // Fallback: command not recognized
        return false;
    }

    private static String capitalize(String text) {
        if (text == null || text.isEmpty()) return text;
        return text.substring(0, 1).toUpperCase() + text.substring(1);
    }
}
