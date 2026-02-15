package io.github.sullis.netty.playground;

import com.github.luben.zstd.ZstdInputStream;
import com.github.luben.zstd.ZstdOutputStream;
import io.netty.handler.codec.compression.Zstd;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ZstdOutputStreamTest {
    @BeforeAll
    public static void beforeAll() throws Throwable {
        Zstd.ensureAvailability();
        assertTrue(Zstd.isAvailable());
    }

    @Test
    public void zstdOutputStreamHappyPath() throws Exception {
        final var charset = TestConstants.CHARSET;
        final String inputText = TestConstants.CONTENT;
        ByteArrayOutputStream baos = new ByteArrayOutputStream(inputText.length());
        ZstdOutputStream out = new ZstdOutputStream(baos);
        out.write(inputText.getBytes(StandardCharsets.UTF_8));
        out.flush();
        out.close();
        byte[] compressed = baos.toByteArray();
        System.out.println("inputText: " + inputText);
        System.out.println("compressed length: " + compressed.length);
        System.out.println("compressed: " + Arrays.toString(compressed));
        final ByteArrayInputStream bais = new ByteArrayInputStream(compressed);
        ZstdInputStream zstdInputStream = new ZstdInputStream(bais);
        String result = new String(zstdInputStream.readAllBytes(), charset);
        assertEquals(inputText, result);
    }
}
