package io.github.sullis.netty.playground;

import io.netty.handler.codec.compression.CompressionOptions;
import io.netty.handler.codec.http.HttpContentCompressor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class HttpContentCompressorTest {
    @Test
    void zeroArgConstructor() throws Exception {
        HttpContentCompressor compressor = new HttpContentCompressor();
        assertNotNull(compressor);
    }

    @Test
    void compressorOptionsArrayConstructor() throws Exception {
        HttpContentCompressor compressor = new HttpContentCompressor((CompressionOptions[]) null);
        assertNotNull(compressor);
    }
}