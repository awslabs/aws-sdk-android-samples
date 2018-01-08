/*
 * Copyright 2016-2018 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

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
