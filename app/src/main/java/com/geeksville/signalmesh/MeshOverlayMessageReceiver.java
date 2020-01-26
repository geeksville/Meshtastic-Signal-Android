package com.geeksville.signalmesh;

import org.whispersystems.libsignal.util.guava.Optional;
import org.whispersystems.signalservice.api.SignalServiceMessagePipe;
import org.whispersystems.signalservice.api.SignalServiceMessageReceiver;
import org.whispersystems.signalservice.api.util.CredentialsProvider;
import org.whispersystems.signalservice.api.util.SleepTimer;
import org.whispersystems.signalservice.api.websocket.ConnectivityListener;
import org.whispersystems.signalservice.internal.configuration.SignalServiceConfiguration;
import org.whispersystems.signalservice.internal.push.PushServiceSocket;
import org.whispersystems.signalservice.internal.websocket.WebSocketConnection;

public class MeshOverlayMessageReceiver extends SignalServiceMessageReceiver {
    /**
     * Construct a SignalServiceMessageReceiver.
     *
     * @param urls The URL of the Signal Service.
     * @param credentials The Signal Service user's credentials.
     */
    public MeshOverlayMessageReceiver(SignalServiceConfiguration urls,
                                        CredentialsProvider credentials,
                                        String userAgent,
                                        ConnectivityListener listener,
                                        SleepTimer timer)
    {
        super(urls, credentials, userAgent, listener, timer);
    }

    @Override
    public SignalServiceMessagePipe createMessagePipe() {
        WebSocketConnection webSocket = new WebSocketConnection(urls.getSignalServiceUrls()[0].getUrl(),
                urls.getSignalServiceUrls()[0].getTrustStore(),
                Optional.of(credentialsProvider), userAgent, connectivityListener,
                sleepTimer);

        return new MeshOverlayMessagePipe(webSocket, Optional.of(credentialsProvider));
    }

    @Override
    public SignalServiceMessagePipe createUnidentifiedMessagePipe() {
        WebSocketConnection webSocket = new WebSocketConnection(urls.getSignalServiceUrls()[0].getUrl(),
                urls.getSignalServiceUrls()[0].getTrustStore(),
                Optional.<CredentialsProvider>absent(), userAgent, connectivityListener,
                sleepTimer);

        return new MeshOverlayMessagePipe(webSocket, Optional.of(credentialsProvider));
    }
}
