package io.github.sullis.netty.playground.trustmanager;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TrustManagerFactoryAgentTest {
    private static final String DEFAULT_ALGORITHM = "PKIX";

    @BeforeAll
    static void beforeAllTests() throws Exception {
        TrustManagerFactoryAgent.install();
    }

    @Test
    void happyPath() throws Exception {
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(DEFAULT_ALGORITHM);
        TrustManager[] trustManagers = tmf.getTrustManagers();
        assertThat(trustManagers).hasSize(1);
        TrustManager tm = trustManagers[0];
        assertNotNull(tm);
    }
}
