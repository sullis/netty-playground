package io.github.sullis.netty.playground.trustmanager;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.loading.ClassInjector;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.utility.JavaModule;

import static net.bytebuddy.dynamic.loading.ClassInjector.UsingInstrumentation.Target.BOOTSTRAP;
import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.none;

import java.io.File;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.security.ProtectionDomain;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class InsecureTrustManagerFactoryAgent {
    private static final String TARGET_CLAZZ_NAME = "javax.net.ssl.TrustManagerFactory";
    private static final Class<?> TARGET_CLAZZ = findClass(TARGET_CLAZZ_NAME);
    private static final Method GET_INSTANCE_METHOD = findMethod(TARGET_CLAZZ, "getInstance", String.class);

    private static final Class<?> INTERCEPTOR_CLASS = GetInstanceMethodInterceptor.class;
    private static final Class<?> INSECURE_TMF_CLASS = InsecureTrustManagerFactory.class;

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

    public static Instrumentation install() throws Exception {
        return installOn(ByteBuddyAgent.install());
    }

    public static Instrumentation installOn(final Instrumentation instrumentation) throws Exception {
        injectBootstrapClasses(instrumentation);

        ByteBuddy byteBuddy = new ByteBuddy().with(Implementation.Context.Disabled.Factory.INSTANCE);

        final AgentBuilder agentBuilder = new AgentBuilder.Default()
                .with(byteBuddy)
                .ignore(none())
                .disableClassFormatChanges()
                .with(AgentBuilder.RedefinitionStrategy.REDEFINITION)
                .with(AgentBuilder.TypeStrategy.Default.REDEFINE)
                .with(AgentBuilder.InitializationStrategy.NoOp.INSTANCE)
                .type(named(TARGET_CLAZZ_NAME))
                .transform(new ReturnTransformer());


        agentBuilder.installOn(instrumentation);

        return instrumentation;
    }

    public static void premain(String arg, Instrumentation instrumentation) throws Exception {
        installOn(instrumentation);
    }

    public static void agentmain(String arg, Instrumentation instrumentation) throws Exception {
        installOn(instrumentation);
    }


    private static void injectBootstrapClasses(Instrumentation instrumentation) throws IOException {
        File temp = Files.createTempDirectory("tmp").toFile();
        temp.deleteOnExit();

        Map<TypeDescription.ForLoadedType, byte[]> types = Stream.of(INTERCEPTOR_CLASS, INSECURE_TMF_CLASS, DummyProvider.class, InsecureTrustManagerFactory.InsecureTrustManagerFactorySpi.class, InsecureTrustManager.class)
                .collect(Collectors.toMap(TypeDescription.ForLoadedType::new, ClassFileLocator.ForClassLoader::read));

        ClassInjector.UsingInstrumentation.of(temp, BOOTSTRAP, instrumentation)
                .inject(types);
    }

    private static class ReturnTransformer implements AgentBuilder.Transformer {

        public DynamicType.Builder<?> transform(DynamicType.Builder<?> builder,
                                                TypeDescription typeDescription,
                                                ClassLoader classLoader,
                                                JavaModule module,
                                                ProtectionDomain protectionDomain) {
            return builder.visit(Advice.to(INTERCEPTOR_CLASS).on(ElementMatchers.is(GET_INSTANCE_METHOD)));
        }
    }

}