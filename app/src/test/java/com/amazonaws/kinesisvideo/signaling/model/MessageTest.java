package com.amazonaws.kinesisvideo.signaling.model;

import static org.junit.Assert.*;

import org.junit.Test;

public class MessageTest {

    @Test
    public void when_setMessagePayload_then_sameGetReturnsSameMessagePayload() {
        final String testPayload = "test payload";
        final Message message = new Message();
        message.setMessagePayload(testPayload);

        assertEquals(testPayload, message.getMessagePayload());
    }

    @Test
    public void when_setAction_then_sameGetReturnsSameAction() {
        final String testAction = "SDP_OFFER";
        final Message message = new Message();
        message.setAction(testAction);

        assertEquals(testAction, message.getAction());
    }

    @Test
    public void when_setRecipientClientId_then_sameGetReturnsSameRecipientClientId() {
        final String testRecipientClientId = "viewer";
        final Message message = new Message();
        message.setRecipientClientId(testRecipientClientId);

        assertEquals(testRecipientClientId, message.getRecipientClientId());
    }

    @Test
    public void when_setSenderClientId_then_sameGetReturnsSameSenderClientId() {
        final String testSenderClientId = "master";
        final Message message = new Message();
        message.setSenderClientId(testSenderClientId);

        assertEquals(testSenderClientId, message.getSenderClientId());
    }
}
