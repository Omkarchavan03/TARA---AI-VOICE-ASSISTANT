package com.tara;

import com.tara.stt.SpeechRecognizer;
import com.tara.tts.Speaker;
import com.tara.ui.SphereUI;
import com.tara.ui.SphereUI.AssistantState;
import com.tara.utils.ConversationManager;
import com.tara.core.CommandProcessor;

public class Main {

    public static void main(String[] args) throws Exception {

        // 🔵 Start Sphere UI
        SphereUI.launchUI();
        SphereUI.setState(AssistantState.IDLE);

        SpeechRecognizer stt = new SpeechRecognizer();
        Speaker tts = new Speaker();
        ConversationManager convo = new ConversationManager();

        boolean awake = false;
        String lastProcessed = "";

        System.out.println("Waiting for wake word: 'hey tara'");

        while (true) {

            /* ===== SPEAKING ===== */
            if (tts.isSpeaking()) {
                SphereUI.setState(AssistantState.SPEAKING);
                stt.pauseListening();
                Thread.sleep(60);
                continue;
            } else {
                stt.resumeListening();
            }

            /* ===== LISTENING / IDLE ===== */
            SphereUI.setState(awake ? AssistantState.LISTENING : AssistantState.IDLE);

            // Listen for speech
            String result = stt.listen();
            if (result == null || result.isEmpty()) continue;

            // Normalize text
            String text = result.trim()
                    .toLowerCase()
                    .replaceAll("[^a-z0-9 ]", "");

            if (text.isEmpty() || text.equals(lastProcessed)) continue;
            lastProcessed = text;

            if (text.length() < 3) continue;

            System.out.println("Heard: " + text);

            /* ===== WAKE WORD ===== */
            if (!awake && text.contains("hey tara")) {
                awake = true;
                SphereUI.wakeUp(); // 🔵 WAKE animation
                tts.speak("Yes, I am listening");
                continue;
            }

            // Ignore when asleep
            if (!awake) continue;

            /* ===== SLEEP ===== */
            if (text.contains("go to sleep")) {
                awake = false;
                SphereUI.setState(AssistantState.IDLE);
                tts.speak("Going to sleep");
                continue;
            }

            /* ===== THINKING ===== */
            SphereUI.setState(AssistantState.THINKING);

            // Conversation response
            String response = convo.getResponse(text);
            if (response != null) {
                SphereUI.setState(AssistantState.SPEAKING);
                tts.speak(response);
                continue;
            }

            // Process commands
            boolean handled = CommandProcessor.process(text, tts);
            if (!handled) {
                SphereUI.setState(AssistantState.SPEAKING);
                tts.speak("I don't understand the command");
            }

            Thread.sleep(200);
        }
    }
}
