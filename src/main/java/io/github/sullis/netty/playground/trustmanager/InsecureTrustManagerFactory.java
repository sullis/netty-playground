package io.github.sullis.netty.playground.trustmanager;

import javax.net.ssl.ManagerFactoryParameters;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.TrustManagerFactorySpi;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.Provider;

/**
 * An insecure {@link TrustManagerFactory} that trusts all X.509 certificates without any verification.
 * <p>
 * <strong>NOTE:</strong>
 * Never use this {@link TrustManagerFactory} in production.
 * It is purely for testing purposes, and thus it is very insecure.
 * </p>
 */
public final class InsecureTrustManagerFactory extends TrustManagerFactory {
    public static final String DEFAULT_ALGORITHM = "PKIX";

    private static final Provider PROVIDER = new DummyProvider();

    public static final TrustManagerFactory INSTANCE = new InsecureTrustManagerFactory();

    private InsecureTrustManagerFactory() {
        super(new InsecureTrustManagerFactorySpi(InsecureTrustManager.INSTANCE), PROVIDER, DEFAULT_ALGORITHM);
    }

    public static final class InsecureTrustManagerFactorySpi extends TrustManagerFactorySpi {

        private final InsecureTrustManager tm;

        public InsecureTrustManagerFactorySpi(final InsecureTrustManager tm) {
            this.tm = tm;
        }

        @Override
        protected void engineInit(KeyStore keyStore) throws KeyStoreException {
            // no-op
        }

        @Override
        protected void engineInit(
                ManagerFactoryParameters managerFactoryParameters) throws InvalidAlgorithmParameterException {
            // no-op
        }

        @Override
        protected TrustManager[] engineGetTrustManagers() {
            return new TrustManager[] { this.tm };
        }

    }
}
