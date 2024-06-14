package io.github.sullis.netty.playground;

import io.github.nettyplus.leakdetector.junit.NettyLeakDetectorExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@ExtendWith(NettyLeakDetectorExtension.class)
public class WebSocketServerTest {
    private WebSocketServer server;
    private String defaultUrl;

    /*
    static {
        System.setProperty("jdk.internal.httpclient.websocket.debug", "true");
    } */

    @BeforeEach
    public void beforeEach() throws Exception {
        server = new WebSocketServer(TestConstants.WEBSOCKET_PATH);
        server.start(NettyTransport.NIO);
        defaultUrl = server.getDefaultUrl();
    }

    @AfterEach
    public void afterEach() {
        if (server != null) {
            server.stop();
        }
    }

    @Test
    public void jdkHttpClient_http1() throws Exception {
        withJdkHttpClient(HttpClient.Version.HTTP_1_1);
    }

    @Test
    public void jdkHttpClient_http2() throws Exception {
        withJdkHttpClient(HttpClient.Version.HTTP_2);
    }

    private void withJdkHttpClient(final HttpClient.Version httpVersion) throws Exception {
        AtomicBoolean onOpenCalled = new AtomicBoolean(false);
        AtomicBoolean onErrorCalled = new AtomicBoolean(false);
        AtomicBoolean onTextCalled = new AtomicBoolean(false);

        HttpClient client = HttpUtil.createJdkHttpClient(httpVersion);
        CompletableFuture<WebSocket> webSocketFuture = client.newWebSocketBuilder()
                .connectTimeout(Duration.ofMillis(5000))
                .buildAsync(URI.create(defaultUrl), new WebSocket.Listener() {
                    @Override
                    public void onOpen(WebSocket webSocket) {
                        onOpenCalled.set(true);
                        webSocket.sendText("Hello world", true);
                        WebSocket.Listener.super.onOpen(webSocket);
                    }

                    @Override
                    public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
                        onTextCalled.set(true);
                        return WebSocket.Listener.super.onText(webSocket, data, last);
                    }

                    @Override
                    public void onError(WebSocket webSocket, Throwable throwable) {
                        onErrorCalled.set(true);
                        if (throwable != null) {
                            throwable.printStackTrace();
                        }
                    }

                    @Override
                    public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
                        return WebSocket.Listener.super.onClose(webSocket, statusCode, reason);
                    }
               });
        WebSocket webSocket = webSocketFuture.join();
        await().untilTrue(onOpenCalled);
        assertThat(webSocket.isInputClosed()).isFalse();
        assertThat(webSocket.isOutputClosed()).isFalse();
        await().untilTrue(onTextCalled);
        assertThat(onErrorCalled).isFalse();
    }

}
