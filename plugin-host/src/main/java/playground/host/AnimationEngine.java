package playground.host;

import playground.api.AnimationPlugin;

public class AnimationEngine {
    private final AnimationPlugin plugin;
    private final int width;
    private final int height;

    public AnimationEngine(AnimationPlugin plugin, int width, int height) {
        this.plugin = plugin;
        this.width = width;
        this.height = height;
    }

    String frame(long number) {
        return plugin.renderFrame(number, width, height);
    }

    public void run(TerminalRenderer renderer) throws InterruptedException {
        for (long number = 0; ; number++) {
            renderer.render(frame(number));
            Thread.sleep(80);
        }
    }
}
