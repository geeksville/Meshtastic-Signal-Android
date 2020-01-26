package com.geeksville.signalmesh;

import org.whispersystems.libsignal.util.guava.Optional;
import org.whispersystems.signalservice.api.SignalServiceMessagePipe;
import org.whispersystems.signalservice.api.crypto.UnidentifiedAccess;
import org.whispersystems.signalservice.api.util.CredentialsProvider;
import org.whispersystems.signalservice.internal.push.OutgoingPushMessageList;
import org.whispersystems.signalservice.internal.push.SendMessageResponse;
import org.whispersystems.signalservice.internal.websocket.WebSocketConnection;

import java.io.IOException;

public class MeshOverlayMessagePipe extends SignalServiceMessagePipe {
    MeshOverlayMessagePipe(WebSocketConnection websocket, Optional<CredentialsProvider> credentialsProvider) {
        super(websocket, credentialsProvider);
    }

    @Override
    public SendMessageResponse send(OutgoingPushMessageList list, Optional<UnidentifiedAccess> unidentifiedAccess) throws IOException {
        return super.send(list, unidentifiedAccess);
    }
}
