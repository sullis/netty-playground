package io.github.sullis.netty.playground;

import com.aayushatharva.brotli4j.Brotli4jLoader;
import com.aayushatharva.brotli4j.decoder.BrotliInputStream;
import com.aayushatharva.brotli4j.encoder.Encoder;
import io.netty.handler.codec.compression.Brotli;
import io.netty.handler.codec.compression.StandardCompressionOptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BrotliInputStreamTest {
    @BeforeAll
    public static void beforeAll() {
        Brotli4jLoader.ensureAvailability();
    }

    @Test
    public void brotliIsAvailable() throws Throwable {
        Brotli4jLoader.ensureAvailability();
        assertTrue(Brotli4jLoader.isAvailable());
        assertTrue(Brotli.isAvailable());
    }

    @Test
    public void brotliInputStreamHappyPath() throws Exception {
        final var charset = TestConstants.CHARSET;
        final String inputText = TestConstants.CONTENT;
        byte[] compressed = Encoder.compress(inputText.getBytes(), StandardCompressionOptions.brotli().parameters());
        final ByteArrayInputStream bais = new ByteArrayInputStream(compressed);
        BrotliInputStream brotliInputStream = new BrotliInputStream(bais);
        String result = new String(brotliInputStream.readAllBytes(), charset);
        assertEquals(inputText, result);
    }

    @Test
    public void brotliInputStreamEmptyInput() throws Exception {
        final var charset = TestConstants.CHARSET;
        final String inputText = "";
        byte[] compressed = Encoder.compress(inputText.getBytes(), StandardCompressionOptions.brotli().parameters());
        final ByteArrayInputStream bais = new ByteArrayInputStream(compressed);
        BrotliInputStream brotliInputStream = new BrotliInputStream(bais);
        String result = new String(brotliInputStream.readAllBytes(), charset);
        assertEquals(inputText, result);
    }

    @Test
    public void brotliInputStreamLargeInput() throws Exception {
        final var charset = TestConstants.CHARSET;
        final String inputText = TestConstants.CONTENT.repeat(1000);
        byte[] compressed = Encoder.compress(inputText.getBytes(), StandardCompressionOptions.brotli().parameters());
        final ByteArrayInputStream bais = new ByteArrayInputStream(compressed);
        BrotliInputStream brotliInputStream = new BrotliInputStream(bais);
        String result = new String(brotliInputStream.readAllBytes(), charset);
        assertEquals(inputText, result);
        assertTrue(compressed.length < inputText.length(), "Compressed data should be smaller than original");
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 4, 6, 9, 11})
    public void brotliInputStreamVariousQualityLevels(int quality) throws Exception {
        final var charset = TestConstants.CHARSET;
        final String inputText = TestConstants.CONTENT;
        Encoder.Parameters params = new Encoder.Parameters().setQuality(quality);
        byte[] compressed = Encoder.compress(inputText.getBytes(charset), params);
        final ByteArrayInputStream bais = new ByteArrayInputStream(compressed);
        BrotliInputStream brotliInputStream = new BrotliInputStream(bais);
        String result = new String(brotliInputStream.readAllBytes(), charset);
        assertEquals(inputText, result);
    }

    @Test
    public void brotliInputStreamSingleByteReads() throws Exception {
        final var charset = TestConstants.CHARSET;
        final String inputText = TestConstants.CONTENT;
        byte[] compressed = Encoder.compress(inputText.getBytes(charset), StandardCompressionOptions.brotli().parameters());
        final ByteArrayInputStream bais = new ByteArrayInputStream(compressed);
        BrotliInputStream brotliInputStream = new BrotliInputStream(bais);
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        int b;
        while ((b = brotliInputStream.read()) != -1) {
            result.write(b);
        }
        assertEquals(inputText, result.toString(charset));
    }
}
