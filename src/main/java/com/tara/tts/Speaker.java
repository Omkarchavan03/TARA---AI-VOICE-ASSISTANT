package com.tara.tts;

import marytts.MaryInterface;
import marytts.LocalMaryInterface;
import marytts.util.data.audio.AudioPlayer;

import java.util.concurrent.atomic.AtomicBoolean;

public class Speaker {

    private final MaryInterface mary;
    private final AtomicBoolean speaking = new AtomicBoolean(false);

    public Speaker() throws Exception {
        mary = new LocalMaryInterface();
    }

    /**
     * Speak the given text asynchronously
     */
    public void speak(String text) {
        if (text == null || text.isEmpty()) return;

        new Thread(() -> {
            try {
                speaking.set(true);

                // Generate audio
                AudioPlayer player = new AudioPlayer(mary.generateAudio(text));
                player.start();
                player.join(); // wait until finished

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                speaking.set(false);
            }
        }, "TTS-Thread").start(); // Name the thread for easier debugging
    }

    /**
     * Speak synchronously (blocking call)
     */
    public void speakSync(String text) {
        if (text == null || text.isEmpty()) return;

        try {
            speaking.set(true);
            AudioPlayer player = new AudioPlayer(mary.generateAudio(text));
            player.start();
            player.join();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            speaking.set(false);
        }
    }

    /**
     * Check if TTS is currently speaking
     */
    public boolean isSpeaking() {
        return speaking.get();
    }

    /**
     * Stop speaking immediately (if needed)
     */
    public void stop() {
        try {
            mary.generateAudio("").close(); // quick hack to reset MaryTTS
        } catch (Exception e) {
            // ignore
        } finally {
            speaking.set(false);
        }
    }
}
