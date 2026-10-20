package playground.host;

import java.io.IOException;
import java.nio.file.Path;
import playground.api.AnimationPlugin;

public class Main {
    public static void main(String[] args) {
        try {
            run(args);
        } catch (IllegalArgumentException | IOException e) {
            System.err.println("Ошибка: " + e.getMessage());
            System.err.println("Используйте --help для списка аргументов.");
            System.exit(2);
        }
    }

    private static void run(String[] args) throws IOException {
        String mode = "builtin";
        String jar = null;
        String className = null;
        for (String arg : args) {
            if (arg.equals("--help")) {
                System.out.println("Использование: --mode=builtin (по умолчанию)\n"
                        + "  или --mode=url --jar=<path> --class=<полное имя класса>");
                return;
            } else if (arg.startsWith("--mode=")) {
                mode = arg.substring("--mode=".length());
            } else if (arg.startsWith("--jar=")) {
                jar = arg.substring("--jar=".length());
            } else if (arg.startsWith("--class=")) {
                className = arg.substring("--class=".length());
            } else {
                throw new IllegalArgumentException("Неизвестный аргумент: " + arg);
            }
        }

        if (mode.equals("builtin")) {
            if (jar != null || className != null) {
                throw new IllegalArgumentException("--jar и --class используются только с --mode=url");
            }
            AnimationPlugin plugin = new StarfieldPlugin();
            animate(plugin, "builtin");
        } else if (mode.equals("url")) {
            if (jar == null || jar.isBlank() || className == null || className.isBlank()) {
                throw new IllegalArgumentException("Для --mode=url нужны --jar=<path> и --class=<имя>");
            }
            // Загрузчик живёт столько же, сколько анимация: классы плагина могут потребоваться и позже.
            try (ExternalPluginLoader loader = new ExternalPluginLoader(Path.of(jar))) {
                // При Ctrl+C JVM запускает hooks, не обязательно выходя из try-with-resources.
                Thread closeOnShutdown = new Thread(() -> {
                    try {
                        loader.close();
                    } catch (IOException e) {
                        System.err.println("Не удалось закрыть загрузчик плагина: " + e.getMessage());
                    }
                });
                Runtime.getRuntime().addShutdownHook(closeOnShutdown);
                try {
                    AnimationPlugin plugin = loader.load(className);
                    animate(plugin, "url");
                } finally {
                    try {
                        Runtime.getRuntime().removeShutdownHook(closeOnShutdown);
                    } catch (IllegalStateException ignored) {
                        // Завершение JVM уже началось; hook закроет загрузчик.
                    }
                }
            }
        } else {
            throw new IllegalArgumentException("Неизвестный режим: " + mode);
        }
    }

    private static void animate(AnimationPlugin plugin, String mode) {
        int width = 60;
        int height = 20;
        TerminalRenderer renderer = new TerminalRenderer(System.out, height);
        Thread shutdownHook = new Thread(renderer::close);
        Runtime.getRuntime().addShutdownHook(shutdownHook);

        String header = "Mode: " + mode + " | Plugin: " + plugin.name() + "\n"
                + "API loader: " + describe(AnimationPlugin.class.getClassLoader())
                + " | Plugin loader: " + describe(plugin.getClass().getClassLoader()) + "\n"
                + "TCCL: " + describe(Thread.currentThread().getContextClassLoader());
        renderer.start(header);
        try {
            new AnimationEngine(plugin, width, height).run(renderer);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            renderer.close();
            Runtime.getRuntime().removeShutdownHook(shutdownHook);
        }
    }

    private static String describe(ClassLoader loader) {
        return loader == null ? "bootstrap" : loader.getClass().getSimpleName()
                + "@" + Integer.toHexString(System.identityHashCode(loader));
    }
}
