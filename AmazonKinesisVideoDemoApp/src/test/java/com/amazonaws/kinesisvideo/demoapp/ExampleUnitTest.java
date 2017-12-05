package com.amazonaws.kinesisvideo.demoapp;

import org.junit.Test;

import java.nio.ByteBuffer;

import static org.junit.Assert.assertTrue;

public class ExampleUnitTest {

    private boolean buteBuffersEqual(
            final ByteBuffer a,
            final ByteBuffer b) {

        if (a.limit() != b.limit()) {
            return false;
        }

        for (int i = 0; i < a.limit(); i++) {
            if (a.get(i) != b.get(i)) {
                return false;
            }
        }
        return true;
    }
}