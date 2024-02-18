package io.github.sullis.netty.playground;

import com.aayushatharva.brotli4j.Brotli4jLoader;
import com.aayushatharva.brotli4j.decoder.DirectDecompress;
import io.netty.handler.codec.compression.Brotli;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactoryBuilder;
import org.apache.hc.client5.http.ssl.TrustAllStrategy;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import javax.net.ssl.SSLContext;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Arrays;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HttpServerTest {
    private HttpServer server;

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
    }

    @AfterEach
    public void afterEach() {
        if (server != null) {
            server.stop();
        }
    }

    @ParameterizedTest
    @MethodSource("availableNettyTransports")
    public void brotliWithApacheHttpClient(NettyTransport transport) throws Exception {
        this.server = new HttpServer(transport);
        this.server.start();
        SSLContext sslContext = SSLContextBuilder.create().loadTrustMaterial(TrustAllStrategy.INSTANCE).build();
        SSLConnectionSocketFactory socketFactory = SSLConnectionSocketFactoryBuilder.create().setHostnameVerifier(NoopHostnameVerifier.INSTANCE).setSslContext(sslContext).build();
        HttpClientConnectionManager connManager = PoolingHttpClientConnectionManagerBuilder.create().setSSLSocketFactory(socketFactory).build();
        CloseableHttpClient httpClient = HttpClients.custom().setConnectionManager(connManager).build();
        HttpGet httpGet = new HttpGet(this.server.getDefaultUrl());
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

    @ParameterizedTest
    @MethodSource("availableNettyTransports")
    public void brotliWithJdkHttpClient_http1(NettyTransport transport) throws Exception {
        verifyBrotliWithJdkHttpClient(HttpClient.Version.HTTP_1_1, transport);
    }

    @ParameterizedTest
    @MethodSource("availableNettyTransports")
    public void brotliWithJdkHttpClient_http2(NettyTransport transport) throws Exception {
        verifyBrotliWithJdkHttpClient(HttpClient.Version.HTTP_2, transport);
    }

    private void verifyBrotliWithJdkHttpClient(final HttpClient.Version httpVersion, NettyTransport transport) throws Exception {
        this.server = new HttpServer(transport);
        this.server.start();
        HttpClient client = HttpUtil.createJdkHttpClient(httpVersion);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(this.server.getDefaultUrl()))
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

    static Stream<NettyTransport> availableNettyTransports() {
        return NettyTransport.availableTransports();
    }
}