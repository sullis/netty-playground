package io.github.sullis.netty.playground;

import com.aayushatharva.brotli4j.Brotli4jLoader;
import com.aayushatharva.brotli4j.decoder.BrotliInputStream;
import com.aayushatharva.brotli4j.encoder.BrotliOutputStream;
import com.aayushatharva.brotli4j.encoder.Encoder;
import io.netty.handler.codec.compression.StandardCompressionOptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
        out.write(inputText.getBytes(StandardCharsets.UTF_8));
        out.flush();
        out.close();
        byte[] compressed = baos.toByteArray();
        System.out.println("inputText: " + inputText);
        System.out.println("compressed length: " + compressed.length);
        System.out.println("compressed: " + Arrays.toString(compressed));
        final ByteArrayInputStream bais = new ByteArrayInputStream(compressed);
        BrotliInputStream brotliInputStream = new BrotliInputStream(bais);
        String result = new String(brotliInputStream.readAllBytes(), charset);
        assertEquals(inputText, result);
    }
}
