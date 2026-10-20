package playground.api;

public interface AnimationPlugin {
    String name();

    String renderFrame(long frame, int width, int height);
}
