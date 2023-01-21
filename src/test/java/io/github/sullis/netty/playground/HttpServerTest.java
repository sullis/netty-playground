package io.github.sullis.netty.playground;

import com.aayushatharva.brotli4j.Brotli4jLoader;
import io.netty.handler.codec.compression.Brotli;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class HttpServerTest {
    @BeforeEach
    public void beforeEach() {
        assertTrue(Brotli4jLoader.isAvailable());
        assertTrue(Brotli.isAvailable());
    }

    @Test
    public void startStop() throws Exception {
        HttpServer server = new HttpServer();
        server.start();
        server.stop();
    }
}
