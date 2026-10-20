package playground.host;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import playground.api.AnimationPlugin;

public class ExternalPluginLoader implements AutoCloseable {
    private final URLClassLoader loader;

    public ExternalPluginLoader(Path jar) throws IOException {
        if (!Files.isRegularFile(jar)) {
            throw new IllegalArgumentException("JAR не найден: " + jar);
        }
        URL url = jar.toAbsolutePath().toUri().toURL();
        loader = new URLClassLoader(new URL[] {url}, AnimationPlugin.class.getClassLoader());
    }

    public AnimationPlugin load(String className) {
        try {
            Class<?> type = loader.loadClass(className);
            if (!AnimationPlugin.class.isAssignableFrom(type)) {
                throw new IllegalArgumentException("Класс не реализует AnimationPlugin: " + className);
            }
            if (type.getClassLoader() != loader) {
                throw new IllegalArgumentException("Класс найден у родительского загрузчика, а не во внешнем JAR: " + className);
            }
            return (AnimationPlugin) type.getConstructor().newInstance();
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Класс не найден: " + className, e);
        } catch (ReflectiveOperationException e) {
            throw new IllegalArgumentException("Не удалось создать " + className
                    + ": нужен доступный public конструктор без аргументов", e);
        }
    }

    @Override
    public void close() throws IOException {
        loader.close();
    }
}
