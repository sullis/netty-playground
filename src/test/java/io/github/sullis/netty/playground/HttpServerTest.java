package io.github.sullis.netty.playground;

import com.aayushatharva.brotli4j.Brotli4jLoader;
import com.aayushatharva.brotli4j.decoder.BrotliInputStream;
import io.netty.handler.codec.compression.Brotli;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HttpServerTest {
    private HttpServer server;
    private String defaultUrl;

    @BeforeEach
    public void beforeEach() throws Exception {
        Brotli4jLoader.ensureAvailability();
        assertTrue(Brotli4jLoader.isAvailable());
        assertTrue(Brotli.isAvailable());
        server = new HttpServer();
        server.start();
        defaultUrl = "http://localhost:8080/";
    }

    @AfterEach
    public void afterEach() {
        if (server != null) {
            server.stop();
        }
    }

    @Test
    public void brotliWithApacheHttpClient5() throws Exception {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(defaultUrl);
        httpGet.setHeader("Accept-Encoding", "br");
        CloseableHttpResponse httpResponse = httpClient.execute(httpGet);
        assertEquals("br", httpResponse.getFirstHeader("content-encoding").getValue());
        HttpEntity responseEntity = httpResponse.getEntity();
        assertEquals("text/plain", responseEntity.getContentType());
        byte[] responseBodyBytes = EntityUtils.toByteArray(responseEntity);
        System.out.println("responseBodyBytes: " + Arrays.toString(responseBodyBytes));
        BrotliInputStream brotliInputStream = new BrotliInputStream(new ByteArrayInputStream(responseBodyBytes));
        String text = new String(brotliInputStream.readAllBytes(), TestConstants.CHARSET);
        assertEquals(TestConstants.CONTENT, text);
        brotliInputStream.close();
    }
}
