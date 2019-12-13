package com.amazonaws.kinesisvideo.demoapp;

import java.nio.ByteBuffer;

class ExampleUnitTest {

    private boolean byteBuffersEqual(
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