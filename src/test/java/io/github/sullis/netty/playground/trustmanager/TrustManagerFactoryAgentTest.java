package io.github.sullis.netty.playground.trustmanager;

import org.junit.jupiter.api.Test;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TrustManagerFactoryAgentTest {
    static {
        TrustManagerFactoryAgent.install();
    }

    @Test
    void happyPath() throws Exception {
        String algorithm = TrustManagerFactory.getDefaultAlgorithm();
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(algorithm);
        TrustManager[] trustManagers = tmf.getTrustManagers();
        assertThat(trustManagers).hasSize(1);
        TrustManager tm = trustManagers[0];
        assertNotNull(tm);
    }
}
