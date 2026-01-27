package com.tara.tts;

import marytts.MaryInterface;
import marytts.LocalMaryInterface;
import marytts.util.data.audio.AudioPlayer;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class Speaker {

    private final MaryInterface mary;
    private final BlockingQueue<String> queue = new LinkedBlockingQueue<>();
    private final AtomicBoolean speaking = new AtomicBoolean(false);

    // 🔥 REQUIRED: track current audio
    private volatile AudioPlayer currentPlayer;

    public Speaker() throws Exception {
        mary = new LocalMaryInterface();

        // 🔥 SINGLE background TTS thread
        new Thread(this::speechLoop, "TTS-Main-Thread").start();
    }

    private void speechLoop() {
        while (true) {
            try {
                String text = queue.take(); // waits
                speaking.set(true);

                currentPlayer = new AudioPlayer(mary.generateAudio(text));
                currentPlayer.start();
                currentPlayer.join();

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                speaking.set(false);
                currentPlayer = null;
            }
        }
    }

    /**
     * Queue speech (NON-BLOCKING)
     */
    public void speak(String text) {
        if (text == null || text.isBlank()) return;

        // prevent huge speeches killing UX
        if (text.length() > 400) {
            text = text.substring(0, 400) + "...";
        }

        queue.offer(text);
    }

    /**
     * Check if speaking or pending speech
     */
    public boolean isSpeaking() {
        return speaking.get() || !queue.isEmpty();
    }

    /**
     * Stop all speech immediately
     */
    @SuppressWarnings("deprecation") // AudioPlayer internally uses Thread.stop()
    public void stop() {
        queue.clear();

        if (currentPlayer != null) {
            // ✅ SAFE shutdown first
            currentPlayer.interrupt();

            // ✅ fallback (MaryTTS internal behavior)
            currentPlayer.stop();
        }

        speaking.set(false);
    }
}
