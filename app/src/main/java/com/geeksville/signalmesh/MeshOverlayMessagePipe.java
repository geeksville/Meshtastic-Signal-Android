package com.geeksville.signalmesh;

import android.os.RemoteException;
import android.util.Log;

import org.thoughtcrime.securesms.util.Base64;
import org.whispersystems.libsignal.util.guava.Optional;
import org.whispersystems.signalservice.api.SignalServiceMessagePipe;
import org.whispersystems.signalservice.api.crypto.UnidentifiedAccess;
import org.whispersystems.signalservice.api.util.CredentialsProvider;
import org.whispersystems.signalservice.internal.push.OutgoingPushMessage;
import org.whispersystems.signalservice.internal.push.OutgoingPushMessageList;
import org.whispersystems.signalservice.internal.push.SendMessageResponse;
import org.whispersystems.signalservice.internal.push.SignalServiceProtos;
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
            String contentStr = m.getContent();
            byte[] content = Base64.decode(contentStr);

            // Content is a Content protobuf.  But it has already been encrypted by this time (see notes)
            //SignalServiceProtos.Content contentProto = SignalServiceProtos.Content.parseFrom(content);
            // Log.i("MeshOverlayMessagePipe", "Content = " + contentProto);

            int type = m.getType(); // FIXME, we also need to send this over the wir?

            // FIXME base64 decode the string FIRST to save lots of bytes
            try {
                mesh.sendData(dest, content, 0);
            } catch (RemoteException e) {
                throw new IOException("Mesh Exception: " + e.getMessage());
            }
        }

        return new SendMessageResponse();
    }
}
