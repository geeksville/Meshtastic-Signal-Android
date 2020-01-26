package com.geeksville.signalmesh;

import org.whispersystems.libsignal.state.SignalProtocolStore;
import org.whispersystems.libsignal.util.guava.Optional;
import org.whispersystems.signalservice.api.SignalServiceMessagePipe;
import org.whispersystems.signalservice.api.SignalServiceMessageSender;
import org.whispersystems.signalservice.api.util.CredentialsProvider;
import org.whispersystems.signalservice.internal.configuration.SignalServiceConfiguration;

/// FIXME might no need to keep this?
public class MeshOverlayMessageSender extends SignalServiceMessageSender {
    public MeshOverlayMessageSender(SignalServiceConfiguration urls, CredentialsProvider credentialsProvider, SignalProtocolStore store, String userAgent, boolean isMultiDevice, Optional<SignalServiceMessagePipe> pipe, Optional<SignalServiceMessagePipe> unidentifiedPipe, Optional<EventListener> eventListener) {
        super(urls, credentialsProvider, store, userAgent, isMultiDevice, pipe, unidentifiedPipe, eventListener);
    }
}
