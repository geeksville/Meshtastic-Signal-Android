package com.geeksville.signalmesh;

import android.os.RemoteException;

import org.whispersystems.libsignal.util.guava.Optional;
import org.whispersystems.signalservice.api.SignalServiceMessagePipe;
import org.whispersystems.signalservice.api.crypto.UnidentifiedAccess;
import org.whispersystems.signalservice.api.util.CredentialsProvider;
import org.whispersystems.signalservice.internal.push.OutgoingPushMessage;
import org.whispersystems.signalservice.internal.push.OutgoingPushMessageList;
import org.whispersystems.signalservice.internal.push.SendMessageResponse;
import org.whispersystems.signalservice.internal.websocket.WebSocketConnection;

import java.io.IOException;

public class MeshOverlayMessagePipe extends SignalServiceMessagePipe {
   private MeshClient mesh;

    MeshOverlayMessagePipe(MeshClient mesh, WebSocketConnection websocket, Optional<CredentialsProvider> credentialsProvider) {
        super(websocket, credentialsProvider);

        this.mesh = mesh;
    }

    @Override
    public SendMessageResponse send(OutgoingPushMessageList list, Optional<UnidentifiedAccess> unidentifiedAccess) throws IOException {
        // FIXME, fail over to the internet version as needed
        // return super.send(list, unidentifiedAccess);

        String dest = list.getDestination();
        for(OutgoingPushMessage m: list.getMessages()) {
            String content = m.getContent();
            int type = m.getType(); // FIXME, we also need to send this over the wire!!!

            // FIXME base64 decode the string FIRST to save lots of bytes
            try {
                mesh.sendData(dest, content.getBytes(), 0);
            } catch (RemoteException e) {
                throw new IOException("Mesh Exception: " + e.getMessage());
            }
        }

        return new SendMessageResponse();
    }
}
