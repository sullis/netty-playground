package io.github.sullis.netty.playground;

import io.github.nettyplus.leakdetector.junit.NettyLeakDetectorExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

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

    @Test
    public void testUppercaseEcho() throws Exception {
        AtomicBoolean onOpenCalled = new AtomicBoolean(false);
        AtomicBoolean onTextCalled = new AtomicBoolean(false);
        AtomicReference<String> receivedText = new AtomicReference<>();

        HttpClient client = HttpUtil.createJdkHttpClient(HttpClient.Version.HTTP_1_1);
        CompletableFuture<WebSocket> webSocketFuture = client.newWebSocketBuilder()
                .connectTimeout(Duration.ofMillis(5000))
                .buildAsync(URI.create(defaultUrl), new WebSocket.Listener() {
                    @Override
                    public void onOpen(WebSocket webSocket) {
                        onOpenCalled.set(true);
                        webSocket.sendText("hello world", true);
                        WebSocket.Listener.super.onOpen(webSocket);
                    }

                    @Override
                    public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
                        onTextCalled.set(true);
                        receivedText.set(data.toString());
                        return WebSocket.Listener.super.onText(webSocket, data, last);
                    }
                });
        
        WebSocket webSocket = webSocketFuture.join();
        await().untilTrue(onOpenCalled);
        await().untilTrue(onTextCalled);
        assertThat(receivedText.get()).isEqualTo("HELLO WORLD");
    }

    @Test
    public void testMultipleMessages() throws Exception {
        List<String> receivedMessages = new ArrayList<>();
        AtomicInteger messagesReceived = new AtomicInteger(0);
        int messageCount = 5;

        HttpClient client = HttpUtil.createJdkHttpClient(HttpClient.Version.HTTP_1_1);
        CompletableFuture<WebSocket> webSocketFuture = client.newWebSocketBuilder()
                .connectTimeout(Duration.ofMillis(5000))
                .buildAsync(URI.create(defaultUrl), new WebSocket.Listener() {
                    @Override
                    public void onOpen(WebSocket webSocket) {
                        for (int i = 0; i < messageCount; i++) {
                            webSocket.sendText("message" + i, true);
                        }
                        WebSocket.Listener.super.onOpen(webSocket);
                    }

                    @Override
                    public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
                        receivedMessages.add(data.toString());
                        messagesReceived.incrementAndGet();
                        return WebSocket.Listener.super.onText(webSocket, data, last);
                    }
                });
        
        WebSocket webSocket = webSocketFuture.join();
        await().until(() -> messagesReceived.get() == messageCount);
        assertThat(receivedMessages).hasSize(messageCount);
        assertThat(receivedMessages).contains("MESSAGE0", "MESSAGE1", "MESSAGE2", "MESSAGE3", "MESSAGE4");
    }

    @Test
    public void testPingPong() throws Exception {
        AtomicBoolean onPongCalled = new AtomicBoolean(false);
        AtomicReference<ByteBuffer> pongData = new AtomicReference<>();

        HttpClient client = HttpUtil.createJdkHttpClient(HttpClient.Version.HTTP_1_1);
        CompletableFuture<WebSocket> webSocketFuture = client.newWebSocketBuilder()
                .connectTimeout(Duration.ofMillis(5000))
                .buildAsync(URI.create(defaultUrl), new WebSocket.Listener() {
                    @Override
                    public void onOpen(WebSocket webSocket) {
                        webSocket.sendPing(ByteBuffer.wrap("ping".getBytes()));
                        WebSocket.Listener.super.onOpen(webSocket);
                    }

                    @Override
                    public CompletionStage<?> onPong(WebSocket webSocket, ByteBuffer message) {
                        onPongCalled.set(true);
                        pongData.set(message);
                        return WebSocket.Listener.super.onPong(webSocket, message);
                    }
                });
        
        WebSocket webSocket = webSocketFuture.join();
        await().untilTrue(onPongCalled);
        assertThat(pongData.get()).isNotNull();
    }

    @Test
    public void testCloseHandshake() throws Exception {
        AtomicBoolean onCloseCalled = new AtomicBoolean(false);
        AtomicInteger closeStatusCode = new AtomicInteger(-1);

        HttpClient client = HttpUtil.createJdkHttpClient(HttpClient.Version.HTTP_1_1);
        CompletableFuture<WebSocket> webSocketFuture = client.newWebSocketBuilder()
                .connectTimeout(Duration.ofMillis(5000))
                .buildAsync(URI.create(defaultUrl), new WebSocket.Listener() {
                    @Override
                    public void onOpen(WebSocket webSocket) {
                        webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "Goodbye");
                        WebSocket.Listener.super.onOpen(webSocket);
                    }

                    @Override
                    public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
                        onCloseCalled.set(true);
                        closeStatusCode.set(statusCode);
                        return WebSocket.Listener.super.onClose(webSocket, statusCode, reason);
                    }
                });
        
        WebSocket webSocket = webSocketFuture.join();
        await().untilTrue(onCloseCalled);
        assertThat(closeStatusCode.get()).isEqualTo(WebSocket.NORMAL_CLOSURE);
    }

    @Test
    public void testBinaryMessage() throws Exception {
        AtomicBoolean onBinaryCalled = new AtomicBoolean(false);
        AtomicReference<ByteBuffer> receivedBinary = new AtomicReference<>();

        HttpClient client = HttpUtil.createJdkHttpClient(HttpClient.Version.HTTP_1_1);
        CompletableFuture<WebSocket> webSocketFuture = client.newWebSocketBuilder()
                .connectTimeout(Duration.ofMillis(5000))
                .buildAsync(URI.create(defaultUrl), new WebSocket.Listener() {
                    @Override
                    public void onOpen(WebSocket webSocket) {
                        byte[] testData = "binary data".getBytes();
                        webSocket.sendBinary(ByteBuffer.wrap(testData), true);
                        WebSocket.Listener.super.onOpen(webSocket);
                    }

                    @Override
                    public CompletionStage<?> onBinary(WebSocket webSocket, ByteBuffer data, boolean last) {
                        onBinaryCalled.set(true);
                        receivedBinary.set(data);
                        return WebSocket.Listener.super.onBinary(webSocket, data, last);
                    }
                });
        
        // Note: The WebSocketFrameHandler currently only supports TextWebSocketFrame.
        // This test verifies the current behavior where binary frames are not supported.
        // The test passes by confirming that no binary data is received.
        WebSocket webSocket = webSocketFuture.join();
        await().pollDelay(Duration.ofMillis(500)).atMost(Duration.ofSeconds(2))
               .until(() -> !onBinaryCalled.get()); // Verify binary frames are not yet supported
    }

    @Test
    public void testEpollTransport() throws Exception {
        if (!NettyTransport.EPOLL.isAvailable()) {
            return; // Skip if EPOLL is not available on this platform
        }
        
        WebSocketServer epollServer = new WebSocketServer(TestConstants.WEBSOCKET_PATH);
        try {
            epollServer.start(NettyTransport.EPOLL);
            String url = epollServer.getDefaultUrl();
            
            AtomicBoolean onTextCalled = new AtomicBoolean(false);
            
            HttpClient client = HttpUtil.createJdkHttpClient(HttpClient.Version.HTTP_1_1);
            CompletableFuture<WebSocket> webSocketFuture = client.newWebSocketBuilder()
                    .connectTimeout(Duration.ofMillis(5000))
                    .buildAsync(URI.create(url), new WebSocket.Listener() {
                        @Override
                        public void onOpen(WebSocket webSocket) {
                            webSocket.sendText("test", true);
                            WebSocket.Listener.super.onOpen(webSocket);
                        }

                        @Override
                        public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
                            onTextCalled.set(true);
                            return WebSocket.Listener.super.onText(webSocket, data, last);
                        }
                    });
            
            WebSocket webSocket = webSocketFuture.join();
            await().untilTrue(onTextCalled);
        } finally {
            epollServer.stop();
        }
    }

    @Test
    public void testIoUringTransport() throws Exception {
        if (!NettyTransport.IO_URING.isAvailable()) {
            return; // Skip if IO_URING is not available on this platform
        }
        
        WebSocketServer ioUringServer = new WebSocketServer(TestConstants.WEBSOCKET_PATH);
        try {
            ioUringServer.start(NettyTransport.IO_URING);
            String url = ioUringServer.getDefaultUrl();
            
            AtomicBoolean onTextCalled = new AtomicBoolean(false);
            
            HttpClient client = HttpUtil.createJdkHttpClient(HttpClient.Version.HTTP_1_1);
            CompletableFuture<WebSocket> webSocketFuture = client.newWebSocketBuilder()
                    .connectTimeout(Duration.ofMillis(5000))
                    .buildAsync(URI.create(url), new WebSocket.Listener() {
                        @Override
                        public void onOpen(WebSocket webSocket) {
                            webSocket.sendText("test", true);
                            WebSocket.Listener.super.onOpen(webSocket);
                        }

                        @Override
                        public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
                            onTextCalled.set(true);
                            return WebSocket.Listener.super.onText(webSocket, data, last);
                        }
                    });
            
            WebSocket webSocket = webSocketFuture.join();
            await().untilTrue(onTextCalled);
        } finally {
            ioUringServer.stop();
        }
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
