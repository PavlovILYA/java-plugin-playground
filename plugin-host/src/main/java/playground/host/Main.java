package playground.host;

import playground.api.AnimationPlugin;

public class Main {
    public static void main(String[] args) {
        AnimationPlugin plugin = new StarfieldPlugin();
        int width = 60;
        int height = 20;
        TerminalRenderer renderer = new TerminalRenderer(System.out, height);
        Runtime.getRuntime().addShutdownHook(new Thread(renderer::close));

        String header = "Plugin: " + plugin.name() + " | Built-in | ClassLoader: "
                + plugin.getClass().getClassLoader().getClass().getSimpleName();
        renderer.start(header);
        try {
            new AnimationEngine(plugin, width, height).run(renderer);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            renderer.close();
        }
    }
}
