package com.tara.core;

import com.tara.tts.Speaker;
import java.util.HashMap;
import java.util.Map;

public class CommandProcessor {

    private static final Map<String, String> APP_COMMANDS = new HashMap<>();
    private static final Map<String, String> WEBSITE_COMMANDS = new HashMap<>();

    static {
        APP_COMMANDS.put("chrome", "chrome.exe");
        APP_COMMANDS.put("vscode", "Code.exe");
        APP_COMMANDS.put("vs code", "Code.exe");
        APP_COMMANDS.put("notepad", "notepad.exe");
        APP_COMMANDS.put("calculator", "calc.exe");
        APP_COMMANDS.put("paint", "mspaint.exe");
        APP_COMMANDS.put("explorer", "explorer.exe");
        APP_COMMANDS.put("task manager", "taskmgr.exe");

        WEBSITE_COMMANDS.put("youtube", "https://www.youtube.com");
        WEBSITE_COMMANDS.put("google", "https://www.google.com");
        WEBSITE_COMMANDS.put("github", "https://www.github.com");
        WEBSITE_COMMANDS.put("facebook", "https://www.facebook.com");
        WEBSITE_COMMANDS.put("instagram", "https://www.instagram.com");
        WEBSITE_COMMANDS.put("twitter", "https://www.twitter.com");
    }

    public static boolean process(String command, Speaker tts) {

        if (command == null || command.isEmpty()) return false;
        command = normalize(command);

        /* ===== WRITE / TYPE (NEW) ===== */
        if (command.startsWith("write ") || command.startsWith("type ")) {
            String text = command.replaceFirst("^(write|type) ", "");
            tts.speak("Writing");
            AppController.typeText(text);
            return true;
        }

        /* ===== CLOSE ===== */
        if (command.startsWith("close ")) {
            String target = command.substring(6).trim();

            if (target.equals("chrome")) {
                tts.speak("Closing Chrome");
                AppController.closeApp("chrome");
                return true;
            }

            if (target.contains("terminal")) {
                tts.speak("Closing terminals");
                AppController.killProcesses("cmd", "powershell", "wt");
                return true;
            }

            if (target.equals("all")) {
                tts.speak("Closing all applications");
                AppController.closeAllUserApps();
                return true;
            }

            if (target.equals("tab")) {
                tts.speak("Closing tab");
                AppController.closeBrowserTab();
                return true;
            }

            return false;
        }

        /* ===== WEB SEARCH ===== */
        if (command.startsWith("webopen ")) {
            String query = command.substring(8).trim();
            if (!query.isEmpty()) {
                tts.speak("Searching for " + query);
                AppController.searchWeb(query);
                return true;
            }
        }

        /* ===== OPEN ===== */
        if (command.startsWith("open ")) {
            String target = command.substring(5).trim();

            if (APP_COMMANDS.containsKey(target)) {
                tts.speak("Opening " + target);
                AppController.openApp(APP_COMMANDS.get(target));
                return true;
            }

            if (WEBSITE_COMMANDS.containsKey(target)) {
                tts.speak("Opening " + target);
                AppController.openWebsite(WEBSITE_COMMANDS.get(target));
                return true;
            }

            if (target.equals("tab")) {
                tts.speak("Opening new tab");
                AppController.openNewBrowserTab();
                return true;
            }
        }

        return false;
    }

    private static String normalize(String text) {
        text = text.toLowerCase().trim();
        text = text.replaceAll("[^a-z0-9 ]", "");
        text = text.replace("clothes ", "close ");
        text = text.replace("clothe ", "close ");
        return text;
    }
}
