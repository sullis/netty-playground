package io.github.sullis.netty.playground;

import com.aayushatharva.brotli4j.Brotli4jLoader;
import io.netty.handler.codec.compression.Brotli;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.entity.DecompressingEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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

        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet("http://localhost:8080/");
        httpGet.setHeader("Accept-Encoding", "br");
        CloseableHttpResponse httpResponse = httpClient.execute(httpGet);
        DecompressingEntity responseEntity = (DecompressingEntity) httpResponse.getEntity();
        assertEquals("text/plain", responseEntity.getContentType());
        assertEquals(-1, responseEntity.getContentLength());
        String text = EntityUtils.toString(responseEntity);
        assertEquals("Hello world", text);
        server.stop();
    }
}
