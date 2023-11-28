package io.github.sullis.netty.playground.trustmanager;

import net.bytebuddy.asm.Advice;

import javax.net.ssl.TrustManagerFactory;

public class GetInstanceMethodInterceptor {
    @Advice.OnMethodExit
    private static void onMethodExit(@Advice.Return(readOnly = false) TrustManagerFactory factory) {
        factory = InsecureTrustManagerFactory.INSTANCE;
    }
}
