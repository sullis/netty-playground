package io.github.sullis.netty.playground;

import com.aayushatharva.brotli4j.Brotli4jLoader;
import com.aayushatharva.brotli4j.decoder.BrotliInputStream;
import com.aayushatharva.brotli4j.encoder.Encoder;
import io.netty.handler.codec.compression.StandardCompressionOptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BrotliInputStreamTest {
    @BeforeAll
    public static void beforeAll() {
        Brotli4jLoader.ensureAvailability();
    }

    @Test
    public void brotliInputStreamHappyPath() throws Exception {
        final var charset = StandardCharsets.UTF_8;
        final String inputText = TestConstants.CONTENT;
        byte[] compressed = Encoder.compress(inputText.getBytes(charset), StandardCompressionOptions.brotli().parameters());
        System.out.println("inputText: " + inputText);
        System.out.println("compressed: " + Arrays.toString(compressed));
        final ByteArrayInputStream bais = new ByteArrayInputStream(compressed);
        BrotliInputStream brotliInputStream = new BrotliInputStream(bais);
        String result = new String(brotliInputStream.readAllBytes(), charset);
        assertEquals(inputText, result);
    }
}
