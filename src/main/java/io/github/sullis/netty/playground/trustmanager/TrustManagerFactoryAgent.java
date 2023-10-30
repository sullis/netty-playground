package io.github.sullis.netty.playground.trustmanager;

import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.agent.builder.ResettableClassFileTransformer;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.TypeResolutionStrategy;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.dynamic.loading.ClassReloadingStrategy;
import net.bytebuddy.implementation.FixedValue;
import static net.bytebuddy.matcher.ElementMatchers.is;
import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.none;

import javax.net.ssl.TrustManagerFactory;
import java.io.File;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.security.Security;

public class TrustManagerFactoryAgent {
    private static final String TARGET_CLAZZ_NAME = "javax.net.ssl.TrustManagerFactory";
    private static final Class<?> TARGET_CLAZZ = findClass(TARGET_CLAZZ_NAME);
    private static final Method GET_INSTANCE_METHOD = findMethod(TARGET_CLAZZ, "getInstance", String.class);
    private static final TrustManagerFactory TMF_INSTANCE = InsecureTrustManagerFactory.INSTANCE;
    private static final FixedValue.AssignerConfigurable GET_INSTANCE_RESULT = FixedValue.value(TMF_INSTANCE);

    static Class<?> findClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException(ex);
        }
    }

    static Method findMethod(Class<?> clazz, String methodName, Class<?>... methodParameterTypes) {
        try {
            return clazz.getMethod(methodName, methodParameterTypes);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static void install() throws Exception {
        Security.setProperty("foo", "bar");
        Instrumentation instrumentation = ByteBuddyAgent.install();
        System.out.println("install: instrumentation " + instrumentation.getClass().getName());

        final File folder = createFolder();
        ClassLoadingStrategy<ClassLoader> classLoadingStrategy = new ClassLoadingStrategy.ForBootstrapInjection(instrumentation, folder);

        DynamicType.Loaded<?> loadedType = new ByteBuddy()
            .ignore(none())
            .redefine(TARGET_CLAZZ)
            .method(is(GET_INSTANCE_METHOD))
            .intercept(GET_INSTANCE_RESULT)
            .make(TypeResolutionStrategy.Lazy.INSTANCE)
            .load(ClassReloadingStrategy.BOOTSTRAP_LOADER, classLoadingStrategy);

        Class<?> loadedClazz = loadedType.getLoaded();
        Method m = loadedClazz.getMethod("getInstance", String.class);
        Object result = m.invoke(null, "PKIX");
        System.out.println(result);
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
                                builder.method(is(GET_INSTANCE_METHOD))
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

