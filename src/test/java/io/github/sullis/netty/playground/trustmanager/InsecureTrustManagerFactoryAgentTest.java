package io.github.sullis.netty.playground.trustmanager;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509ExtendedTrustManager;
import javax.net.ssl.X509TrustManager;

import java.lang.instrument.Instrumentation;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class InsecureTrustManagerFactoryAgentTest {
    private static final String DEFAULT_ALGORITHM = "PKIX";
    private static final TrustManagerFactory initialTrustManagerFactory;

    static {
      try {
          initialTrustManagerFactory = TrustManagerFactory.getInstance(DEFAULT_ALGORITHM);
      } catch (Exception ex) {
          throw new RuntimeException(ex);
      }
    }

    @BeforeAll
    static void beforeAllTests() throws Exception {
        assertNotNull(initialTrustManagerFactory);
        Instrumentation instrumentation = InsecureTrustManagerFactoryAgent.install();
        assertThat(instrumentation.isRedefineClassesSupported()).isTrue();
        assertThat(instrumentation.isModifiableClass(TrustManagerFactory.class)).isTrue();
        assertThat(TrustManagerFactory.getInstance(DEFAULT_ALGORITHM)).isNotSameAs(initialTrustManagerFactory);
    }

    @Test
    void happyPath() throws Exception {
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(DEFAULT_ALGORITHM);
        assertThat(tmf.getClass().getName()).isEqualTo("io.github.sullis.netty.playground.trustmanager.InsecureTrustManagerFactory");
        assertThat(tmf.getAlgorithm()).isEqualTo("PKIX");
        TrustManager[] trustManagers = tmf.getTrustManagers();
        assertThat(trustManagers).hasSize(1);
        X509ExtendedTrustManager tm = (X509ExtendedTrustManager) trustManagers[0];
        assertThat(tm.getClass().getName()).isEqualTo(InsecureTrustManager.class.getName());
        tm.checkClientTrusted(new X509Certificate[0], "whatever");
        tm.checkServerTrusted(new X509Certificate[0], "whatever");
    }

    @Test
    void trustManagerFactoryIsDifferentFromInitialValue() throws Exception {
        final TrustManagerFactory tmf = TrustManagerFactory.getInstance(DEFAULT_ALGORITHM);
        assertNotSame(tmf, initialTrustManagerFactory);
    }

    @Test
    void exceptionThrownWhenAlgorithmIsBogus() {
        assertThatThrownBy(() -> { TrustManagerFactory.getInstance("bogus"); })
                .isInstanceOf(NoSuchAlgorithmException.class)
                .hasMessage("bogus TrustManagerFactory not available");
    }

    @Test
    void defaultAlgorithmIsPkix() {
        assertThat(TrustManagerFactory.getDefaultAlgorithm()).isEqualTo("PKIX");
    }
}