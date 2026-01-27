package com.tara;

import com.tara.stt.SpeechRecognizer;
import com.tara.tts.Speaker;
import com.tara.ui.SphereUI;
import com.tara.ui.SphereUI.AssistantState;
import com.tara.core.CommandProcessor;
import com.tara.core.OllamaBrain;
import com.tara.utils.SpeechNormalizer;

import java.util.ArrayList;
import java.util.List;

public class Main {

    private static boolean awake = false;
    private static boolean lockedSpeaking = false;
    private static String lastProcessed = "";

    public static void main(String[] args) throws Exception {

        SphereUI.launchUI();
        SphereUI.setState(AssistantState.IDLE);

        SpeechRecognizer stt = new SpeechRecognizer();
        Speaker tts = new Speaker();

        System.out.println("Waiting for wake word: 'hey tara'");

        while (true) {
            try {

                /* =========================
                   HARD SPEECH LOCK
                   ========================= */
                if (tts.isSpeaking() || lockedSpeaking) {
                    SphereUI.setState(AssistantState.SPEAKING);
                    stt.pauseListening();
                    Thread.sleep(60);
                    continue;
                } else {
                    stt.resumeListening();
                }

                SphereUI.setState(
                        awake ? AssistantState.LISTENING : AssistantState.IDLE
                );

                String heard = stt.listen();
                if (heard == null || heard.isBlank()) continue;

                String text = SpeechNormalizer.normalize(heard);

                if (text.length() < 3 || text.equals(lastProcessed)) {
                    continue;
                }

                lastProcessed = text;
                System.out.println("Heard: " + text);

                /* =========================
                   WAKE WORD
                   ========================= */
                if (!awake && SpeechNormalizer.isWakeWord(text)) {
                    awake = true;
                    SphereUI.wakeUp();
                    tts.speak("Yes. I am listening.");
                    continue;
                }

                if (!awake) continue;

                /* =========================
                   SLEEP
                   ========================= */
                if (text.contains("go to sleep") || text.equals("exit")) {
                    awake = false;
                    SphereUI.setState(AssistantState.IDLE);
                    tts.speak("Going to sleep.");
                    continue;
                }

                SphereUI.setState(AssistantState.THINKING);

                /* =========================
                   COMMAND FIRST (RULE BASED)
                   ========================= */
                boolean handled = CommandProcessor.process(text, tts);
                if (handled) continue;

                /* =========================
                   LLM THINK
                   ========================= */
                OllamaBrain.BrainResult result = OllamaBrain.think(text);

                if (result == null) continue;

                /* =========================
                   🔥 COMMAND FROM LLM (FIX)
                   ========================= */
                if ("command".equals(result.type)) {
                    boolean llmHandled =
                            CommandProcessor.process(result.command, tts);
                    if (!llmHandled) {
                        tts.speak("I could not execute that command.");
                    }
                    continue;
                }

                /* =========================
                   NORMAL RESPONSE
                   ========================= */
                if (result.text == null || result.text.isBlank()) {
                    tts.speak("Alright, let me think again.");
                    continue;
                }

                speakNaturally(result.text, tts);
                Thread.sleep(120);

            } catch (Exception e) {
                e.printStackTrace();
                tts.speak("Something went wrong, but I am back.");
                lockedSpeaking = false;
            }
        }
    }

    /* =====================================================
       HUMAN-LIKE SPEECH ENGINE
       ===================================================== */
    private static void speakNaturally(String text, Speaker tts) {

        if (text == null || text.isBlank()) return;

        lockedSpeaking = true;

        boolean longAnswer =
                text.length() > 220 ||
                text.split("\\.").length > 3;

        if (!longAnswer) {
            tts.speak(text);
            lockedSpeaking = false;
            return;
        }

        tts.speak("Wait, let me explain this properly.");

        List<String> chunks = splitIntoChunks(text, 140);

        for (String chunk : chunks) {
            tts.speak(chunk);
        }

        lockedSpeaking = false;
    }

    private static List<String> splitIntoChunks(String text, int maxLen) {

        List<String> chunks = new ArrayList<>();
        String[] sentences = text.split("\\. ");
        StringBuilder current = new StringBuilder();

        for (String s : sentences) {
            if (current.length() + s.length() <= maxLen) {
                current.append(s).append(". ");
            } else {
                chunks.add(current.toString().trim());
                current.setLength(0);
                current.append(s).append(". ");
            }
        }

        if (!current.isEmpty()) {
            chunks.add(current.toString().trim());
        }

        return chunks;
    }
}
