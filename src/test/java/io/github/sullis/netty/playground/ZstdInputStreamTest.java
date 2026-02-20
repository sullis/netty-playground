package io.github.sullis.netty.playground;

import com.github.luben.zstd.ZstdInputStream;
import io.netty.handler.codec.compression.Zstd;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ZstdInputStreamTest {
    @BeforeAll
    public static void beforeAll() throws Throwable {
        Zstd.ensureAvailability();
        assertTrue(Zstd.isAvailable());
    }

    @Test
    public void zstdInputStreamHappyPath() throws Exception {
        final var charset = TestConstants.CHARSET;
        final String inputText = TestConstants.CONTENT;
        byte[] compressed = com.github.luben.zstd.Zstd.compress(inputText.getBytes());
        final ByteArrayInputStream bais = new ByteArrayInputStream(compressed);
        ZstdInputStream zstdInputStream = new ZstdInputStream(bais);
        String result = new String(zstdInputStream.readAllBytes(), charset);
        assertEquals(inputText, result);
    }

    @Test
    public void zstdInputStreamEmptyInput() throws Exception {
        final var charset = TestConstants.CHARSET;
        final String inputText = "";
        byte[] compressed = com.github.luben.zstd.Zstd.compress(inputText.getBytes());
        final ByteArrayInputStream bais = new ByteArrayInputStream(compressed);
        ZstdInputStream zstdInputStream = new ZstdInputStream(bais);
        String result = new String(zstdInputStream.readAllBytes(), charset);
        assertEquals(inputText, result);
    }

    @Test
    public void zstdInputStreamLargeInput() throws Exception {
        final var charset = TestConstants.CHARSET;
        final String inputText = TestConstants.CONTENT.repeat(1000);
        byte[] compressed = com.github.luben.zstd.Zstd.compress(inputText.getBytes());
        final ByteArrayInputStream bais = new ByteArrayInputStream(compressed);
        ZstdInputStream zstdInputStream = new ZstdInputStream(bais);
        String result = new String(zstdInputStream.readAllBytes(), charset);
        assertEquals(inputText, result);
        assertTrue(compressed.length < inputText.length(), "Compressed data should be smaller than original");
    }
}
