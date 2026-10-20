package playground.host;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TerminalRendererTest {
    @Test
    void restoresCursorOnlyOnce() {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        TerminalRenderer renderer = new TerminalRenderer(new PrintStream(bytes, true, StandardCharsets.UTF_8), 20);

        renderer.start("Mode: builtin\nLoader: AppClassLoader");
        renderer.render("frame");
        renderer.close();
        renderer.close();

        String output = bytes.toString(StandardCharsets.UTF_8);
        assertTrue(output.contains("\u001b[3;1Hframe"));
        assertEquals(1, output.split("\u001b\\[\\?25h", -1).length - 1);
    }
}
