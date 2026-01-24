package com.tara.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;

public class ConversationManager {

    private static class ConvoPattern {
        Pattern pattern;
        List<String> responses;

        ConvoPattern(String regex, List<String> responses) {
            // Flexible pattern: ignores case, multiple spaces, and punctuation
            this.pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
            this.responses = responses;
        }
    }

    private final List<ConvoPattern> convoPatterns;
    private final Random random;

    public ConversationManager() {
        random = new Random();
        convoPatterns = new ArrayList<>();

        // Greeting
        convoPatterns.add(new ConvoPattern("\\b(hello|hi|hey|hiya)\\b",
                List.of(
                        "Hello! How can I help you?",
                        "Hi there! What can I do for you?",
                        "Hey! Ready to assist you!"
                )));

        // Name queries
        convoPatterns.add(new ConvoPattern("\\b(what is your name|who are you|your name)\\b",
                List.of(
                        "I am Tara, your assistant.",
                        "You can call me Tara.",
                        "Tara at your service!"
                )));

        // Mood queries
        convoPatterns.add(new ConvoPattern("\\b(how are you|how are you doing|what's up|how's your mood)\\b",
                List.of(
                        "I am doing great, thank you!",
                        "Feeling fantastic and ready to help!",
                        "I am fine, ready to assist you!"
                )));

        // Feeling/mood-specific
        convoPatterns.add(new ConvoPattern("\\b(happy|sad|angry|tired)\\b",
                List.of(
                        "I see, tell me more about it.",
                        "Thanks for sharing, I'm here to help!",
                        "Hmm, I understand. How can I assist you further?"
                )));
    }

    /**
     * Returns a conversational response based on input text.
     * Returns null if no pattern matches.
     */
    public String getResponse(String text) {
        if (text == null || text.isEmpty()) return null;

        // Normalize spaces and lowercase
        text = text.trim().replaceAll("\\s+", " ").toLowerCase();

        for (ConvoPattern cp : convoPatterns) {
            if (cp.pattern.matcher(text).find()) {
                List<String> options = cp.responses;
                return options.get(random.nextInt(options.size())); // random answer
            }
        }
        return null;
    }
}
