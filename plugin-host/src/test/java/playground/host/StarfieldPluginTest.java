package playground.host;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class StarfieldPluginTest {
    private final StarfieldPlugin plugin = new StarfieldPlugin();

    @Test
    void hasAReadableName() {
        assertEquals("Starfield", plugin.name());
    }

    @Test
    void rendersTheRequestedDimensions() {
        String[] lines = plugin.renderFrame(0, 60, 20).split("\n", -1);

        assertEquals(20, lines.length);
        for (String line : lines) {
            assertEquals(60, line.length());
        }
    }

    @Test
    void changesWithFrameNumber() {
        assertNotEquals(plugin.renderFrame(0, 60, 20), plugin.renderFrame(1, 60, 20));
    }
}
