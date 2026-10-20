package playground.host;

import java.net.URLClassLoader;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import playground.api.AnimationPlugin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PluginSessionTest {
    @Test
    void builtinUsesOrdinaryApplicationLoader() throws Exception {
        try (PluginSession session = PluginSession.open(LaunchOptions.parse(new String[0]))) {
            assertInstanceOf(StarfieldPlugin.class, session.plugin());
            assertSame(AnimationPlugin.class.getClassLoader(), session.plugin().getClass().getClassLoader());
            session.close();
            session.close();
        }
    }

    @Test
    void externalSessionKeepsPluginAvailableUntilClose() throws Exception {
        String jar = System.getProperty("matrixJar");
        LaunchOptions options = LaunchOptions.parse(new String[] {
                "--mode=url", "--jar=" + jar, "--class=playground.matrix.MatrixRainPlugin"
        });
        try (PluginSession session = PluginSession.open(options)) {
            assertEquals("Matrix Rain", session.plugin().name());
            ClassLoader loader = session.plugin().getClass().getClassLoader();
            assertInstanceOf(URLClassLoader.class, loader);
            assertEquals(20, session.plugin().renderFrame(0, 60, 20).split("\n", -1).length);
            assertNotNull(loader.getResource("playground/matrix/MatrixRainPlugin.class"));
            session.close();
            session.close();
            assertNull(loader.getResource("playground/matrix/MatrixRainPlugin.class"));
        }
    }

    @Test
    void missingJarFailsBeforeAnimationStarts() {
        LaunchOptions options = LaunchOptions.parse(new String[] {
                "--mode=url", "--jar=" + Path.of(System.getProperty("matrixJar")).resolveSibling("missing.jar"),
                "--class=playground.matrix.MatrixRainPlugin"
        });

        assertThrows(IllegalArgumentException.class, () -> PluginSession.open(options));
    }
}
