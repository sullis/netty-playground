package io.github.sullis.netty.playground;

import com.aayushatharva.brotli4j.Brotli4jLoader;
import com.aayushatharva.brotli4j.decoder.DirectDecompress;
import io.netty.handler.codec.compression.Brotli;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(NettyParanoidLeakExtension.class)
public class HttpServerTest {
    private HttpServer server;
    private String defaultUrl;

    @BeforeAll
    static public void beforeAll() throws Exception {
        String osName = System.getProperty("os.name");
        String archName = System.getProperty("os.arch");
        System.out.println("os.name: " + osName);
        System.out.println("os.arch: " + archName);
    }

    @BeforeEach
    public void beforeEach() throws Exception {
        Brotli4jLoader.ensureAvailability();
        assertTrue(Brotli4jLoader.isAvailable());
        assertTrue(Brotli.isAvailable());
        server = new HttpServer();
        server.start();
        defaultUrl = "http://localhost:" + server.getPort() + "/";
    }

    @AfterEach
    public void afterEach() {
        if (server != null) {
            server.stop();
        }
    }

    @Test
    public void brotliWithApacheHttpClient() throws Exception {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(defaultUrl);
        httpGet.setHeader("Accept-Encoding", "br");
        CloseableHttpResponse httpResponse = httpClient.execute(httpGet);
        assertEquals("br", httpResponse.getFirstHeader("content-encoding").getValue());
        HttpEntity responseEntity = httpResponse.getEntity();
        assertEquals("text/plain", responseEntity.getContentType());
        byte[] compressedData = EntityUtils.toByteArray(responseEntity);
        System.out.println("HTTP response compressedData length: " + compressedData.length);
        System.out.println("HTTP response compressedData: " + Arrays.toString(compressedData));
        DirectDecompress directDecompress = DirectDecompress.decompress(compressedData);
        System.out.println("DirectDecompress result status: " + directDecompress.getResultStatus());
        byte[] decompressedData = directDecompress.getDecompressedData();
        assertNotNull(decompressedData, "decompressedData");
        String text = new String(decompressedData, TestConstants.CHARSET);
        assertEquals(TestConstants.CONTENT, text);
    }

    @Test
    public void brotliWithJdkHttpClient_http1() throws Exception {
        verifyBrotliWithJdkHttpClient(HttpClient.Version.HTTP_1_1);
    }

    @Test
    public void brotliWithJdkHttpClient_http2() throws Exception {
        verifyBrotliWithJdkHttpClient(HttpClient.Version.HTTP_2);
    }

    private void verifyBrotliWithJdkHttpClient(final HttpClient.Version httpVersion) throws Exception {
        HttpClient client = HttpClient.newBuilder()
                .version(httpVersion)
                .build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(defaultUrl))
                .setHeader("Accept-Encoding", "br")
                .timeout(Duration.ofSeconds(1))
                .build();
        HttpResponse<byte[]> httpResponse = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
        assertEquals(200, httpResponse.statusCode());
        assertEquals("br", httpResponse.headers().firstValue("content-encoding").get());
        assertEquals("text/plain", httpResponse.headers().firstValue("content-type").get());
        byte[] compressedData = httpResponse.body();
        System.out.println("HTTP response compressedData length: " + compressedData.length);
        System.out.println("HTTP response compressedData: " + Arrays.toString(compressedData));
        DirectDecompress directDecompress = DirectDecompress.decompress(compressedData);
        System.out.println("DirectDecompress result status: " + directDecompress.getResultStatus());
        byte[] decompressedData = directDecompress.getDecompressedData();
        assertNotNull(decompressedData, "decompressedData");
        String text = new String(decompressedData, TestConstants.CHARSET);
        assertEquals(TestConstants.CONTENT, text);
    }
}
