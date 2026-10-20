package playground.host;

import java.nio.file.Path;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LaunchOptionsTest {
    @Test
    void defaultsToBuiltin() {
        LaunchOptions options = LaunchOptions.parse(new String[0]);

        assertEquals("builtin", options.mode());
        assertFalse(options.help());
    }

    @Test
    void parsesExternalJarOptions() {
        LaunchOptions options = LaunchOptions.parse(new String[] {
                "--mode=url", "--jar=plugins/matrix.jar", "--class=playground.matrix.MatrixRainPlugin"
        });

        assertEquals("url", options.mode());
        assertEquals(Path.of("plugins/matrix.jar"), options.jar());
        assertEquals("playground.matrix.MatrixRainPlugin", options.className());
    }

    @Test
    void helpNeedsNoJar() {
        assertTrue(LaunchOptions.parse(new String[] {"--help"}).help());
    }

    @Test
    void rejectsMissingAndUnrelatedOptions() {
        assertEquals("Для --mode=url нужны --jar=<path> и --class=<имя>",
                assertThrows(IllegalArgumentException.class,
                        () -> LaunchOptions.parse(new String[] {"--mode=url"})).getMessage());
        assertEquals("--jar и --class используются только с --mode=url",
                assertThrows(IllegalArgumentException.class,
                        () -> LaunchOptions.parse(new String[] {"--jar=plugin.jar"})).getMessage());
        assertEquals("Неизвестный аргумент: --unknown",
                assertThrows(IllegalArgumentException.class,
                        () -> LaunchOptions.parse(new String[] {"--unknown"})).getMessage());
    }
}
