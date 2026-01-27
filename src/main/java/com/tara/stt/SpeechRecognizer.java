package com.tara.stt;

import org.vosk.LibVosk;
import org.vosk.LogLevel;
import org.vosk.Model;
import org.vosk.Recognizer;

import javax.sound.sampled.*;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class SpeechRecognizer {

    private Recognizer recognizer;
    private TargetDataLine microphone;
    private BlockingQueue<String> resultsQueue;
    private volatile boolean listening = true;

    public SpeechRecognizer() throws IOException, LineUnavailableException {
        LibVosk.setLogLevel(LogLevel.INFO);

        // ✅ Update: New model path for Indian English
        String modelPath = System.getProperty("model.path",
                "D:/projects/TARA/TaraAssistant/models/vosk-model-en-in-0.5");

        Model model = new Model(modelPath);
        recognizer = new Recognizer(model, 16000);

        resultsQueue = new LinkedBlockingQueue<>();

        // Audio format: 16kHz, 16-bit, mono (matches Vosk model)
        AudioFormat format = new AudioFormat(16000, 16, 1, true, false);
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
        microphone = (TargetDataLine) AudioSystem.getLine(info);
        microphone.open(format);
        microphone.start();

        new Thread(this::captureAudio, "STT-Thread").start();
    }

    private void captureAudio() {
        byte[] buffer = new byte[2048];
        long lastVoiceTime = System.currentTimeMillis();
        boolean isSpeaking = false;

        try {
            while (true) {
                if (!listening) {
                    Thread.sleep(50);
                    continue;
                }

                int n = microphone.read(buffer, 0, buffer.length);
                if (n <= 0) continue;

                // Noise filter – adjust amplitude threshold if needed for new model
                int amplitude = 0;
                for (int i = 0; i < n; i += 2) {
                    int sample = ((buffer[i + 1] << 8) | (buffer[i] & 0xff));
                    amplitude += Math.abs(sample);
                }
                if (amplitude < 1800) continue; // slightly lower for Indian English model

                synchronized (recognizer) {
                    boolean accepted = recognizer.acceptWaveForm(buffer, n);

                    if (accepted) {
                        // Final result
                        String finalResult = recognizer.getFinalResult()
                                .replaceAll(".*\"text\"\\s*:\\s*\"(.*?)\".*", "$1")
                                .trim();
                        if (!finalResult.isEmpty()) resultsQueue.offer(finalResult);
                        isSpeaking = false;
                        lastVoiceTime = System.currentTimeMillis();
                    } else {
                        // Partial result
                        String partial = recognizer.getPartialResult()
                                .replaceAll(".*\"partial\"\\s*:\\s*\"(.*?)\".*", "$1")
                                .trim();
                        if (!partial.isEmpty()) {
                            isSpeaking = true;
                            lastVoiceTime = System.currentTimeMillis();
                        }
                    }
                }

                // Flush after 1.5s silence
                if (isSpeaking && System.currentTimeMillis() - lastVoiceTime > 1500) {
                    synchronized (recognizer) {
                        String finalResult = recognizer.getFinalResult()
                                .replaceAll(".*\"text\"\\s*:\\s*\"(.*?)\".*", "$1")
                                .trim();
                        if (!finalResult.isEmpty()) resultsQueue.offer(finalResult);
                    }
                    isSpeaking = false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Fetch final recognized speech. Waits up to 2 seconds.
     */
    public String listen() {
        try {
            String result = resultsQueue.poll(2, TimeUnit.SECONDS);
            return result != null ? result : "";
        } catch (InterruptedException e) {
            return "";
        }
    }

    public void pauseListening() {
        listening = false;
    }

    public void resumeListening() {
        listening = true;
    }
}
