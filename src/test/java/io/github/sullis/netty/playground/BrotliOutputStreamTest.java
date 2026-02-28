package io.github.sullis.netty.playground;

import com.aayushatharva.brotli4j.Brotli4jLoader;
import com.aayushatharva.brotli4j.decoder.BrotliInputStream;
import com.aayushatharva.brotli4j.encoder.BrotliOutputStream;
import com.aayushatharva.brotli4j.encoder.Encoder;
import io.netty.handler.codec.compression.StandardCompressionOptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BrotliOutputStreamTest {
    @BeforeAll
    public static void beforeAll() {
        Brotli4jLoader.ensureAvailability();
    }

    @Test
    public void brotliOutputStreamHappyPath() throws Exception {
        final var charset = TestConstants.CHARSET;
        final String inputText = TestConstants.CONTENT;
        ByteArrayOutputStream baos = new ByteArrayOutputStream(inputText.length());
        BrotliOutputStream out = new BrotliOutputStream(baos, StandardCompressionOptions.brotli().parameters());
        out.write(inputText.getBytes(charset));
        out.flush();
        out.close();
        byte[] compressed = baos.toByteArray();
        final ByteArrayInputStream bais = new ByteArrayInputStream(compressed);
        BrotliInputStream brotliInputStream = new BrotliInputStream(bais);
        String result = new String(brotliInputStream.readAllBytes(), charset);
        assertEquals(inputText, result);
    }

    @Test
    public void brotliOutputStreamEmptyInput() throws Exception {
        final var charset = TestConstants.CHARSET;
        final String inputText = "";
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        BrotliOutputStream out = new BrotliOutputStream(baos, StandardCompressionOptions.brotli().parameters());
        out.write(inputText.getBytes(charset));
        out.flush();
        out.close();
        byte[] compressed = baos.toByteArray();
        final ByteArrayInputStream bais = new ByteArrayInputStream(compressed);
        BrotliInputStream brotliInputStream = new BrotliInputStream(bais);
        String result = new String(brotliInputStream.readAllBytes(), charset);
        assertEquals(inputText, result);
    }

    @Test
    public void brotliOutputStreamLargeInput() throws Exception {
        final var charset = TestConstants.CHARSET;
        final String inputText = TestConstants.CONTENT.repeat(1000);
        ByteArrayOutputStream baos = new ByteArrayOutputStream(inputText.length());
        BrotliOutputStream out = new BrotliOutputStream(baos, StandardCompressionOptions.brotli().parameters());
        out.write(inputText.getBytes(charset));
        out.flush();
        out.close();
        byte[] compressed = baos.toByteArray();
        final ByteArrayInputStream bais = new ByteArrayInputStream(compressed);
        BrotliInputStream brotliInputStream = new BrotliInputStream(bais);
        String result = new String(brotliInputStream.readAllBytes(), charset);
        assertEquals(inputText, result);
        assertTrue(compressed.length < inputText.length(), "Compressed data should be smaller than original");
    }

    @Test
    public void brotliOutputStreamMultipleWrites() throws Exception {
        final var charset = TestConstants.CHARSET;
        final String part1 = "Hello ";
        final String part2 = "World ";
        final String part3 = "from Brotli!";
        final String expectedText = part1 + part2 + part3;
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        BrotliOutputStream out = new BrotliOutputStream(baos, StandardCompressionOptions.brotli().parameters());
        out.write(part1.getBytes(charset));
        out.flush();
        out.write(part2.getBytes(charset));
        out.flush();
        out.write(part3.getBytes(charset));
        out.flush();
        out.close();
        
        byte[] compressed = baos.toByteArray();
        final ByteArrayInputStream bais = new ByteArrayInputStream(compressed);
        BrotliInputStream brotliInputStream = new BrotliInputStream(bais);
        String result = new String(brotliInputStream.readAllBytes(), charset);
        assertEquals(expectedText, result);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 4, 6, 9, 11})
    public void brotliOutputStreamVariousQualityLevels(int quality) throws Exception {
        final var charset = TestConstants.CHARSET;
        final String inputText = TestConstants.CONTENT;
        Encoder.Parameters params = new Encoder.Parameters().setQuality(quality);
        ByteArrayOutputStream baos = new ByteArrayOutputStream(inputText.length());
        BrotliOutputStream out = new BrotliOutputStream(baos, params);
        out.write(inputText.getBytes(charset));
        out.flush();
        out.close();
        byte[] compressed = baos.toByteArray();
        final ByteArrayInputStream bais = new ByteArrayInputStream(compressed);
        BrotliInputStream brotliInputStream = new BrotliInputStream(bais);
        String result = new String(brotliInputStream.readAllBytes(), charset);
        assertEquals(inputText, result);
    }
}
