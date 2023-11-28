package io.github.sullis.netty.playground.trustmanager;

import java.security.Provider;

public class DummyProvider extends Provider {
    public DummyProvider() {
        super("", "0.0", "");
    }
}
