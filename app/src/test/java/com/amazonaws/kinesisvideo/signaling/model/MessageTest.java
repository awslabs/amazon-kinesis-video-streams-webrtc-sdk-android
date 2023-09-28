package com.amazonaws.kinesisvideo.signaling.model;

import static org.junit.Assert.*;

import org.junit.Test;

public class MessageTest {

    @Test
    public void when_setMessagePayload_then_sameGetReturnsSameMessagePayload() {
        final String testPayload = "test payload";
        final Message message = new Message();
        message.messagePayload = testPayload;

        assertEquals(testPayload, message.messagePayload);
    }

    @Test
    public void when_setAction_then_sameGetReturnsSameAction() {
        final String testAction = "SDP_OFFER";
        final Message message = new Message();
        message.action = testAction;

        assertEquals(testAction, message.action);
    }

    @Test
    public void when_setRecipientClientId_then_sameGetReturnsSameRecipientClientId() {
        final String testRecipientClientId = "viewer";
        final Message message = new Message();
        message.recipientClientId = testRecipientClientId;

        assertEquals(testRecipientClientId, message.recipientClientId);
    }

    @Test
    public void when_setSenderClientId_then_sameGetReturnsSameSenderClientId() {
        final String testSenderClientId = "master";
        final Message message = new Message();
        message.senderClientId = testSenderClientId;

        assertEquals(testSenderClientId, message.senderClientId);
    }
}
