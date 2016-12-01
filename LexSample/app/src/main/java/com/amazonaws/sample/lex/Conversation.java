
package com.amazonaws.sample.lex;

import java.util.ArrayList;
import java.util.List;

public class Conversation {
    private static List<TextMessage> currentConversation;

    public static void clear() {
        currentConversation = new ArrayList<TextMessage>();
    }

    public static void add(final TextMessage message) {
        if (currentConversation == null) {
            clear();
        }
        currentConversation.add(message);
    }

    public static TextMessage getMessage(final int pos) {
        if (currentConversation == null) {
            return null;
        }
        return currentConversation.get(pos);
    }

    public static int getCount() {
        return currentConversation == null ? 0 : currentConversation.size();
    }
}
