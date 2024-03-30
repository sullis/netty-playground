package io.github.sullis.netty.playground;

import io.github.nettyplus.leakdetector.junit.NettyLeakDetectorExtension;
import io.netty.buffer.ByteBufAllocator;
import io.netty.handler.ssl.OpenSsl;
import io.netty.handler.ssl.OpenSslClientContext;
import io.netty.handler.ssl.OpenSslEngine;
import io.netty.handler.ssl.OpenSslServerContext;
import io.netty.handler.ssl.OpenSslSessionContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.ssl.SslProvider;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.net.ssl.SSLParameters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(NettyLeakDetectorExtension.class)
class OpenSslTest {
    @BeforeEach
    void beforeEach() {
        OpenSsl.ensureAvailability();
        assertTrue(OpenSsl.isAvailable());
        assertThat(OpenSsl.unavailabilityCause()).isNull();
    }

    @Test
    void testBoringSsl() throws Exception {
        assertThat(OpenSsl.versionString()).isEqualTo("BoringSSL");
        assertThat(OpenSsl.supportsKeyManagerFactory()).isTrue();
        assertThat(OpenSslServerContext.defaultClientProvider().name()).isEqualTo("OPENSSL");
        assertThat(OpenSslServerContext.defaultServerProvider().name()).isEqualTo("OPENSSL");
        assertTrue(SslProvider.isAlpnSupported(SslProvider.OPENSSL));
        assertTrue(SslProvider.isTlsv13Supported(SslProvider.OPENSSL));
    }

    @Test
    void testAvailableCipherSuites() {
        assertThat(OpenSsl.availableOpenSslCipherSuites())
                .containsExactlyInAnyOrder( "ECDHE-ECDSA-AES128-GCM-SHA256",
                "ECDHE-RSA-AES128-GCM-SHA256",
                "ECDHE-ECDSA-AES256-GCM-SHA384",
                "ECDHE-RSA-AES256-GCM-SHA384",
                "ECDHE-ECDSA-CHACHA20-POLY1305",
                "ECDHE-RSA-CHACHA20-POLY1305",
                "ECDHE-PSK-CHACHA20-POLY1305",
                "ECDHE-ECDSA-AES128-SHA",
                "ECDHE-RSA-AES128-SHA",
                "ECDHE-PSK-AES128-CBC-SHA",
                "ECDHE-ECDSA-AES256-SHA",
                "ECDHE-RSA-AES256-SHA",
                "ECDHE-PSK-AES256-CBC-SHA",
                "AES128-GCM-SHA256",
                "AES256-GCM-SHA384",
                "AES128-SHA",
                "PSK-AES128-CBC-SHA",
                "AES256-SHA",
                "PSK-AES256-CBC-SHA",
                "TLS_AES_128_GCM_SHA256",
                "TLS_AES_256_GCM_SHA384",
                "TLS_CHACHA20_POLY1305_SHA256",
                "AEAD-AES128-GCM-SHA256",
                "AEAD-AES256-GCM-SHA384",
                "AEAD-CHACHA20-POLY1305-SHA256");
    }

    @Test
    void testClientContext() throws Exception {
        OpenSslClientContext clientCtx = (OpenSslClientContext) SslContextBuilder.forClient().build();
        OpenSslSessionContext sessionCtx = clientCtx.sessionContext();
        assertThat(sessionCtx.getSessionCacheSize()).isEqualTo(20480);
        assertThat(sessionCtx.getSessionTimeout()).isEqualTo(300);
        assertThat(sessionCtx.isSessionCacheEnabled()).isEqualTo(true);
        assertEquals(1, clientCtx.refCnt());
        SslHandler handler = clientCtx.newHandler(ByteBufAllocator.DEFAULT);
        assertNull(handler.applicationProtocol());
        OpenSslEngine engine = (OpenSslEngine) handler.engine();
        assertThat(engine.getEnabledProtocols()).containsExactlyInAnyOrder("SSLv2Hello", "TLSv1.2", "TLSv1.3");
        assertThat(engine.getSupportedProtocols()).containsExactlyInAnyOrder("SSLv2Hello", "TLSv1", "TLSv1.1", "TLSv1.2", "TLSv1.3");
        SSLParameters sslParameters = engine.getSSLParameters();
        assertThat(sslParameters.getNeedClientAuth()).isFalse();
        assertThat(sslParameters.getWantClientAuth()).isFalse();
        assertThat(sslParameters.getEnableRetransmissions()).isTrue();
        assertThat(sslParameters.getUseCipherSuitesOrder()).isTrue();
        assertThat(sslParameters.getCipherSuites()).containsExactly("TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384",
                "TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256",
                "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256",
                "TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384",
                "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA",
                "TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA",
                "TLS_RSA_WITH_AES_128_GCM_SHA256",
                "TLS_RSA_WITH_AES_128_CBC_SHA",
                "TLS_RSA_WITH_AES_256_CBC_SHA",
                "TLS_AES_128_GCM_SHA256",
                "TLS_AES_256_GCM_SHA384",
                "TLS_CHACHA20_POLY1305_SHA256");
        assertThat(sslParameters.getServerNames()).isNull();
    }

    @Test
    void testServerContext() throws Exception {
        SelfSignedCertificate cert = new SelfSignedCertificate();
        OpenSslServerContext serverCtx = (OpenSslServerContext) SslContextBuilder.forServer(cert.key(), cert.cert()).build();
        OpenSslSessionContext sessionCtx = serverCtx.sessionContext();
        assertThat(sessionCtx.getSessionCacheSize()).isEqualTo(20480);
        assertThat(sessionCtx.getSessionTimeout()).isEqualTo(300);
        assertThat(sessionCtx.isSessionCacheEnabled()).isEqualTo(true);
        assertEquals(1, serverCtx.refCnt());
        SslHandler handler = serverCtx.newHandler(ByteBufAllocator.DEFAULT);
        assertNull(handler.applicationProtocol());
        OpenSslEngine engine = (OpenSslEngine) handler.engine();
        assertThat(engine.getEnabledProtocols()).containsExactlyInAnyOrder("SSLv2Hello", "TLSv1.2", "TLSv1.3");
        assertThat(engine.getSupportedProtocols()).containsExactlyInAnyOrder("SSLv2Hello", "TLSv1", "TLSv1.1", "TLSv1.2", "TLSv1.3");
        SSLParameters sslParameters = engine.getSSLParameters();
        assertThat(sslParameters.getNeedClientAuth()).isFalse();
        assertThat(sslParameters.getWantClientAuth()).isFalse();
        assertThat(sslParameters.getEnableRetransmissions()).isTrue();
        assertThat(sslParameters.getUseCipherSuitesOrder()).isTrue();
        assertThat(sslParameters.getCipherSuites()).containsExactlyInAnyOrder("TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384",
                "TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256",
                "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256",
                "TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384",
                "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA",
                "TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA",
                "TLS_RSA_WITH_AES_128_GCM_SHA256",
                "TLS_RSA_WITH_AES_128_CBC_SHA",
                "TLS_RSA_WITH_AES_256_CBC_SHA",
                "TLS_AES_128_GCM_SHA256",
                "TLS_AES_256_GCM_SHA384",
                "TLS_CHACHA20_POLY1305_SHA256");
        assertThat(sslParameters.getServerNames()).isNull();
    }
}