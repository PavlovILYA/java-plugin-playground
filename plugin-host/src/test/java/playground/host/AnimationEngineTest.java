package playground.host;

import org.junit.jupiter.api.Test;
import playground.api.AnimationPlugin;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AnimationEngineTest {
    @Test
    void usesAnyAnimationPlugin() {
        AnimationPlugin plugin = new AnimationPlugin() {
            @Override
            public String name() {
                return "Test";
            }

            @Override
            public String renderFrame(long frame, int width, int height) {
                return frame + ":" + width + "x" + height;
            }
        };
        AnimationEngine engine = new AnimationEngine(plugin, 12, 5);

        assertEquals("7:12x5", engine.frame(7));
    }
}
