package io.github.sullis.netty.playground.trustmanager;

import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.agent.builder.ResettableClassFileTransformer;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.dynamic.loading.ClassReloadingStrategy;
import net.bytebuddy.implementation.FixedValue;
import net.bytebuddy.matcher.ElementMatcher;
import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.none;
import static net.bytebuddy.matcher.ElementMatchers.returns;
import static net.bytebuddy.matcher.ElementMatchers.takesArguments;

import javax.net.ssl.TrustManagerFactory;
import java.lang.instrument.Instrumentation;

public class TrustManagerFactoryAgent {
    private static final Class<TrustManagerFactory> TARGET_CLAZZ = javax.net.ssl.TrustManagerFactory.class;
    private static final ClassLoader TARGET_CLAZZ_LOADER  = TARGET_CLAZZ.getClassLoader();
    private static final String TARGET_CLAZZ_NAME = TARGET_CLAZZ.getName();
    private static final TrustManagerFactory TMF_INSTANCE = InsecureTrustManagerFactory.INSTANCE;
    private static final ElementMatcher.Junction<MethodDescription> METHOD_MATCHER = named("getInstance").and(takesArguments(String.class)).and(returns(named(TARGET_CLAZZ_NAME)));
    private static final FixedValue.AssignerConfigurable GET_INSTANCE_RESULT = FixedValue.value(TMF_INSTANCE);

    public static void install() {
        Instrumentation instrumentation = ByteBuddyAgent.install();
        System.out.println("instrumentation: " + instrumentation.getClass().getName());
        ByteBuddy byteBuddy = new ByteBuddy();
        byteBuddy.ignore(none())
                .redefine(TARGET_CLAZZ).method(METHOD_MATCHER).intercept(GET_INSTANCE_RESULT).make().load(TARGET_CLAZZ_LOADER, ClassReloadingStrategy.fromInstalledAgent());
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
                .ignore(none())
                .type(named(TARGET_CLAZZ_NAME))
                .transform((builder, type, classLoader, module, protectionDomain) ->
                                builder.method(METHOD_MATCHER)
                                        .intercept(GET_INSTANCE_RESULT));
    }
}

