package playground.host;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;
import playground.api.AnimationPlugin;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PluginMenuTest {
    @Test
    void retriesInvalidInputAndSelectsRequestedProvider() throws Exception {
        AnimationPlugin first = new StarfieldPlugin();
        AnimationPlugin second = new StarfieldPlugin();
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        PrintStream output = new PrintStream(bytes, true, StandardCharsets.UTF_8);

        AnimationPlugin selected = PluginMenu.select(List.of(first, second), new StringReader("oops\n3\n2\n"), output);

        assertSame(second, selected);
        assertTrue(bytes.toString(StandardCharsets.UTF_8).contains("Select plugin [1-2]"));
    }

    @Test
    void eofEndsSelection() {
        assertThrows(IllegalArgumentException.class,
                () -> PluginMenu.select(List.of(new StarfieldPlugin()), new StringReader(""), System.out));
    }
}
