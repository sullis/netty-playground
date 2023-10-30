package io.github.sullis.netty.playground.trustmanager;

import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.agent.builder.ResettableClassFileTransformer;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.dynamic.loading.ClassReloadingStrategy;
import net.bytebuddy.implementation.FixedValue;
import net.bytebuddy.matcher.ElementMatcher;
import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.none;
import static net.bytebuddy.matcher.ElementMatchers.returns;
import static net.bytebuddy.matcher.ElementMatchers.takesArguments;

import javax.net.ssl.TrustManagerFactory;
import java.io.File;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.nio.file.Files;

public class TrustManagerFactoryAgent {
    private static final String TARGET_CLAZZ_NAME = "javax.net.ssl.TrustManagerFactory";
    private static final Class<?> TARGET_CLAZZ = findClass(TARGET_CLAZZ_NAME);
    private static final TrustManagerFactory TMF_INSTANCE = InsecureTrustManagerFactory.INSTANCE;
    private static final ElementMatcher.Junction<MethodDescription> METHOD_MATCHER = named("getInstance").and(takesArguments(String.class)).and(returns(named(TARGET_CLAZZ_NAME)));
    private static final FixedValue.AssignerConfigurable GET_INSTANCE_RESULT = FixedValue.value(TMF_INSTANCE);

    static Class<?> findClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static void install() throws Exception {
        Instrumentation instrumentation = ByteBuddyAgent.install();
        System.out.println("install: instrumentation " + instrumentation.getClass().getName());

        final File folder = createFolder();
        ClassLoadingStrategy<ClassLoader> bootstrapStrategy = new ClassLoadingStrategy.ForBootstrapInjection(instrumentation, folder);

        DynamicType.Loaded<?> loadedType = new ByteBuddy()
            .ignore(none())
            .redefine(TARGET_CLAZZ)
            .method(METHOD_MATCHER)
            .intercept(GET_INSTANCE_RESULT)
            .make()
            .load(ClassReloadingStrategy.BOOTSTRAP_LOADER, bootstrapStrategy);
        System.out.println("install: allLoaded " + loadedType.getAllLoaded().keySet());
        System.out.println("install: loaded type " + loadedType.getLoaded().getName());
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

    private static File createFolder() throws IOException {
        File file = Files.createTempFile("foo", "bar").toFile();
        file.delete();
        File parent = file.getParentFile();
        File folder = new File(parent, "" + System.currentTimeMillis());
        folder.mkdirs();
        return folder;
    }

}

