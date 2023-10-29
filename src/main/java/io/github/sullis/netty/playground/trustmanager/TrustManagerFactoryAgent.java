package io.github.sullis.netty.playground.trustmanager;

import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.agent.builder.ResettableClassFileTransformer;
import net.bytebuddy.implementation.FixedValue;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.instrument.Instrumentation;

public class TrustManagerFactoryAgent {

    public static void install() {
        Instrumentation instrumentation = ByteBuddyAgent.install();
        installOn(instrumentation);
    }

    public static void installOn(Instrumentation instrumentation) {
        ResettableClassFileTransformer transformer = createAgentBuilder().installOn(instrumentation);
        System.out.println("transformer: " + transformer);
    }

    public static void premain(String arg, Instrumentation instrumentation) {
        System.out.println("premain invoked");
        installOn(instrumentation);
    }

    public static void agentmain(String arg, Instrumentation instrumentation) {
        System.out.println("agentmain invoked");
        installOn(instrumentation);
    }

    static AgentBuilder.Identified.Extendable createAgentBuilder() {
        return new AgentBuilder.Default()
                .ignore(ElementMatchers.none())
                .type(ElementMatchers.named("javax.net.ssl.TrustManagerFactory"))
                .transform((builder, type, classLoader, module, protectionDomain) ->
                                builder.method(ElementMatchers.named("getInstance"))
                                        .intercept(FixedValue.value(InsecureTrustManagerFactory.INSTANCE)));
    }
}

