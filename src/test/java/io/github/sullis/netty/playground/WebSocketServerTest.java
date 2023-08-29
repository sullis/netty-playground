package io.github.sullis.netty.playground;

import io.netty.buffer.ByteBufUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

public class WebSocketServerTest {
    private static NettyLeakListener leakListener;
    private WebSocketServer server;
    private String defaultUrl;
    private ExecutorService executor;

    /*
    static {
        System.setProperty("jdk.internal.httpclient.websocket.debug", "true");
    } */

    @BeforeAll
    static public void beforeAll() throws Exception {
        leakListener = new NettyLeakListener();
        ByteBufUtil.setLeakListener(leakListener);
    }

    @BeforeEach
    public void beforeEach() throws Exception {
        executor = Executors.newCachedThreadPool();
        server = new WebSocketServer(TestConstants.WEBSOCKET_PATH);
        server.start();
        defaultUrl = "ws://127.0.0.1:" + server.getPort() + TestConstants.WEBSOCKET_PATH;
        leakListener.assertZeroLeaks();
    }

    @AfterEach
    public void afterEach() {
        if (server != null) {
            server.stop();
        }
        if (executor != null) {
            executor.shutdown();
        }
        leakListener.assertZeroLeaks();
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

        HttpClient client = HttpClient.newBuilder()
                .version(httpVersion)
                .executor(executor)
                .build();
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
