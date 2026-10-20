package playground.host;

import java.io.IOException;
import playground.api.AnimationPlugin;

final class AnimationRunner {
    private static final int WIDTH = 60;
    private static final int HEIGHT = 20;

    static void run(PluginSession session, String mode) {
        TerminalRenderer renderer = new TerminalRenderer(System.out, HEIGHT);
        Thread animationThread = Thread.currentThread();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            animationThread.interrupt();
            try {
                animationThread.join(1_000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            renderer.close();
            try {
                session.close();
            } catch (IOException e) {
                System.err.println("Не удалось закрыть загрузчик плагина: " + e.getMessage());
            }
        }));

        AnimationPlugin plugin = session.plugin();
        String header = "Mode: " + mode + " | Plugin: " + plugin.name() + "\n"
                + "API loader: " + describe(AnimationPlugin.class.getClassLoader())
                + " | Plugin loader: " + describe(plugin.getClass().getClassLoader()) + "\n"
                + "TCCL: " + describe(Thread.currentThread().getContextClassLoader());
        try {
            renderer.start(header);
            new AnimationEngine(plugin, WIDTH, HEIGHT).run(renderer);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            renderer.close();
        }
    }

    private static String describe(ClassLoader loader) {
        return loader == null ? "bootstrap" : loader.getClass().getSimpleName()
                + "@" + Integer.toHexString(System.identityHashCode(loader));
    }
}
