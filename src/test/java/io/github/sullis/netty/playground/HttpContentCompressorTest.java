package io.github.sullis.netty.playground;

import io.netty.handler.codec.compression.CompressionOptions;
import io.netty.handler.codec.http.HttpContentCompressor;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HttpContentCompressorTest {
    private static final Field FIELD_SUPPORTS_COMPRESSION_OPTIONS = FieldUtils.getDeclaredField(HttpContentCompressor.class, "supportsCompressionOptions", true);

    @Test
    void zeroArgConstructor_supportsCompressionOptionsIsFalse() throws Exception {
        HttpContentCompressor compressor = new HttpContentCompressor();
        assertFalse(((Boolean) FIELD_SUPPORTS_COMPRESSION_OPTIONS.get(compressor)));
    }

    @Test
    void compressorOptionsArrayConstructor_supportsCompressionOptionsIsTrue() throws Exception {
        HttpContentCompressor compressor = new HttpContentCompressor((CompressionOptions[]) null);
        assertTrue(((Boolean) FIELD_SUPPORTS_COMPRESSION_OPTIONS.get(compressor)));
    }
}