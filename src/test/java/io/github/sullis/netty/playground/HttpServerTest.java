package io.github.sullis.netty.playground;

import org.junit.jupiter.api.Test;
public class HttpServerTest {
    @Test
    public void startStop() throws Exception {
        HttpServer server = new HttpServer();
        server.start();
        server.stop();
    }
}
