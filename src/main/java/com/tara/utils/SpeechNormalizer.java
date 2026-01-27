package com.tara.utils;

public class SpeechNormalizer {

    public static String normalize(String text) {
        if (text == null) return "";

        text = text.toLowerCase().trim();

        // Remove junk characters
        text = text.replaceAll("[^a-z0-9 ]", "");

        // ===== REMOVE FILLERS =====
        text = text.replaceAll("\\b(um|uh|ah|oh boy|oh been|hmm|mm|eh)\\b", "");
        text = text.replaceAll("\\b(like|you know|i mean|okay|ok)\\b", "");

        // ===== REDUCE MULTIPLE SPACES =====
        text = text.replaceAll("\\s+", " ").trim();

        // ===== WAKE WORD FIXES =====
        text = text.replaceAll(
                "hey data|hey donna|hey dara|hey sara|hey tura",
                "hey tara"
        );

        // ===== COMMAND VERB FIXES =====
        text = text.replaceAll("clothes|clouse", "close");
        text = text.replaceAll("op en|oh pen|rip open|the open|open up|opean", "open");
        text = text.replaceAll("web open|webopen|web op en", "webopen");

        // ===== APPLICATION NAMES =====
        text = text.replaceAll("grow|chromen|chrome browser|chrometh|chrom", "chrome");
        text = text.replaceAll("you do|you too|u do|youtub|youtubee|yo tube", "youtube");
        text = text.replaceAll("not barrel|note barrel|note pad|not pad|no pad", "notepad");
        text = text.replaceAll("paint program|can paint|oh pen", "paint");
        text = text.replaceAll("word document|ms word|wordpad|word pad", "word");
        text = text.replaceAll("excel sheet|ms excel|excel spreadsheet", "excel");
        text = text.replaceAll("power point|ms powerpoint|ppt", "powerpoint");

        // ===== COMMON SYSTEM WORDS =====
        text = text.replaceAll("settings panel|system settings|setting", "settings");
        text = text.replaceAll("control panel|ctrl panel", "control panel");
        text = text.replaceAll("file explorer|explorer|windows explorer", "explorer");

        // ===== INTERNET & BROWSING =====
        text = text.replaceAll("google chrome|open chrome|launch chrome", "chrome");
        text = text.replaceAll("search on google|google search|find on google", "search google");
        text = text.replaceAll("web open youtube|launch youtube|youtube site", "youtube");

        // ===== SOCIAL MEDIA =====
        text = text.replaceAll("face book|fb|facebook site", "facebook");
        text = text.replaceAll("insta|instagram site", "instagram");
        text = text.replaceAll("twitter site|tweet site", "twitter");

        // ===== FILE OPERATIONS =====
        text = text.replaceAll("create file|new file|make file", "new file");
        text = text.replaceAll("delete file|remove file|erase file", "delete file");
        text = text.replaceAll("open folder|launch folder|folder open", "open folder");

        // ===== MISC SHORTCUT FIXES =====
        text = text.replaceAll("minimise|minimize window", "minimize");
        text = text.replaceAll("maximise|maximize window", "maximize");
        text = text.replaceAll("close window|exit window|shut window", "close");

        // ===== NUMBERS & ORDINALS =====
        text = text.replaceAll("first one|1st one", "first");
        text = text.replaceAll("second one|2nd one", "second");
        text = text.replaceAll("third one|3rd one", "third");
        text = text.replaceAll("fourth one|4th one", "fourth");
        text = text.replaceAll("fifth one|5th one", "fifth");

        // ===== COMMON MISTAKES / MISPRONOUNCING =====
        text = text.replaceAll("java script|javascrpt|java scrpt", "javascript");
        text = text.replaceAll("py thin|python3|pythone", "python");
        text = text.replaceAll("micro soft|ms", "microsoft");
        text = text.replaceAll("vs code|vscode", "vscode");
        text = text.replaceAll("command prompt|cmd|cmd prompt", "cmd");

        // ===== REMOVE REPETITIVE WORDS =====
        text = text.replaceAll("\\b(please please|um um|ok ok|yes yes)\\b", "");

        // =====================================================
        // 🔥 ADDED: STT FAULTY WORD PATCH (NO REMOVALS ABOVE)
        // =====================================================

        text = text.replaceAll("hey khara|hey kara|hey hara|hey dialogue|hater|sara", "hey tara");
        text = text.replaceAll("chromee|crores", "chrome");
        text = text.replaceAll("open crores", "open chrome");
        text = text.replaceAll("what take|what take java", "what is");
        text = text.replaceAll("reward", "go to sleep");
        text = text.replaceAll("tata|bye tara", "exit");
        text = text.replaceAll("as are worth|his dads his script couldnt have", "");

        // ===== TRIM SPACES AGAIN =====
        text = text.replaceAll("\\s+", " ").trim();

        return text;
    }

    public static boolean isWakeWord(String text) {
        return text != null && text.contains("hey tara");
    }
}
