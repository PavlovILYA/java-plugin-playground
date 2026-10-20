package playground.host;

import java.io.Closeable;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Stream;
import playground.api.AnimationPlugin;

final class SpiPluginLoader implements Closeable {
    private final URLClassLoader loader;
    private final ClassLoader originalTccl;
    private final List<AnimationPlugin> withOriginalTccl;
    private final List<AnimationPlugin> withPluginTccl;
    private final List<AnimationPlugin> plugins;
    private final boolean restored;

    SpiPluginLoader(Path directory) throws IOException {
        if (!Files.isDirectory(directory)) {
            throw new IllegalArgumentException("Каталог плагинов не найден: " + directory
                    + ". Выполните ./gradlew preparePlugins");
        }
        List<URL> urls = new ArrayList<>();
        try (Stream<Path> files = Files.list(directory)) {
            for (Path jar : files.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(".jar"))
                    .sorted().toList()) {
                urls.add(jar.toUri().toURL());
            }
        }
        if (urls.isEmpty()) {
            throw new IllegalArgumentException("В каталоге нет JAR: " + directory
                    + ". Выполните ./gradlew preparePlugins");
        }

        URLClassLoader pluginLoader = new URLClassLoader(urls.toArray(URL[]::new),
                AnimationPlugin.class.getClassLoader());
        try {
            Thread thread = Thread.currentThread();
            ClassLoader original = thread.getContextClassLoader();
            List<AnimationPlugin> before;
            List<AnimationPlugin> after;
            List<AnimationPlugin> explicit;
            try {
                before = discover(ServiceLoader.load(AnimationPlugin.class));
                thread.setContextClassLoader(pluginLoader);
                // ServiceLoader ленивый: провайдеры должны быть созданы до восстановления TCCL.
                after = discover(ServiceLoader.load(AnimationPlugin.class));
                thread.setContextClassLoader(original);
                explicit = discover(ServiceLoader.load(AnimationPlugin.class, pluginLoader));
            } finally {
                thread.setContextClassLoader(original);
            }

            loader = pluginLoader;
            originalTccl = original;
            withOriginalTccl = before;
            withPluginTccl = after;
            plugins = explicit;
            restored = thread.getContextClassLoader() == original;
        } catch (RuntimeException | Error e) {
            try {
                pluginLoader.close();
            } catch (IOException closeError) {
                e.addSuppressed(closeError);
            }
            throw e;
        }
    }

    List<AnimationPlugin> plugins() {
        return plugins;
    }

    List<AnimationPlugin> withOriginalTccl() {
        return withOriginalTccl;
    }

    List<AnimationPlugin> withPluginTccl() {
        return withPluginTccl;
    }

    ClassLoader originalTccl() {
        return originalTccl;
    }

    boolean restored() {
        return restored;
    }

    String report() {
        return "Original TCCL: " + describe(originalTccl) + "\n"
                + "AnimationPlugin loader: " + describe(AnimationPlugin.class.getClassLoader()) + "\n"
                + "ServiceLoader.load(service) before: " + withOriginalTccl.size() + " " + names(withOriginalTccl) + "\n"
                + "ServiceLoader.load(service) after: " + withPluginTccl.size() + " " + names(withPluginTccl) + "\n"
                + "ServiceLoader.load(service, pluginLoader): " + plugins.size() + " " + names(plugins) + "\n"
                + "Original TCCL restored: " + restored;
    }

    String summary() {
        return "SPI before TCCL: " + withOriginalTccl.size() + " | after TCCL: "
                + withPluginTccl.size() + " | restored: " + restored;
    }

    private static List<AnimationPlugin> discover(ServiceLoader<AnimationPlugin> serviceLoader) {
        List<AnimationPlugin> found = new ArrayList<>();
        for (AnimationPlugin plugin : serviceLoader) {
            found.add(plugin);
        }
        return List.copyOf(found);
    }

    private static List<String> names(List<AnimationPlugin> plugins) {
        return plugins.stream().map(AnimationPlugin::name).toList();
    }

    private static String describe(ClassLoader classLoader) {
        return classLoader == null ? "bootstrap" : classLoader.getClass().getSimpleName()
                + "@" + Integer.toHexString(System.identityHashCode(classLoader));
    }

    @Override
    public void close() throws IOException {
        loader.close();
    }
}
