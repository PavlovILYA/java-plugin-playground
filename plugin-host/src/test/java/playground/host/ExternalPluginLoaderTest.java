package playground.host;

import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.jar.JarFile;
import org.junit.jupiter.api.Test;
import playground.api.AnimationPlugin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExternalPluginLoaderTest {
    private final Path jar = Path.of(System.getProperty("matrixJar"));

    @Test
    void loadsMatrixPluginFromJarUsingSharedApi() throws Exception {
        assertThrows(ClassNotFoundException.class,
                () -> AnimationPlugin.class.getClassLoader().loadClass("playground.matrix.MatrixRainPlugin"));
        try (ExternalPluginLoader loader = new ExternalPluginLoader(jar)) {
            AnimationPlugin plugin = loader.load("playground.matrix.MatrixRainPlugin");

            assertEquals("Matrix Rain", plugin.name());
            assertInstanceOf(URLClassLoader.class, plugin.getClass().getClassLoader());
            assertNotEquals(AnimationPlugin.class.getClassLoader(), plugin.getClass().getClassLoader());
            assertEquals(AnimationPlugin.class.getClassLoader(), plugin.getClass().getClassLoader().getParent());
            String[] lines = plugin.renderFrame(7, 60, 20).split("\n", -1);
            assertEquals(20, lines.length);
            for (String line : lines) {
                assertEquals(60, line.length());
            }
            assertNotEquals(plugin.renderFrame(0, 60, 20), plugin.renderFrame(1, 60, 20));
        }
    }

    @Test
    void jarDoesNotBundleTheSpi() throws Exception {
        try (JarFile file = new JarFile(jar.toFile())) {
            assertFalse(file.stream().anyMatch(entry -> entry.getName().equals("playground/api/AnimationPlugin.class")));
            assertTrue(file.stream().anyMatch(entry -> entry.getName().equals("playground/matrix/MatrixRainPlugin.class")));
        }
    }

    @Test
    void rejectsMissingJar() {
        assertThrows(IllegalArgumentException.class,
                () -> new ExternalPluginLoader(jar.resolveSibling("missing-plugin.jar")));
    }

    @Test
    void rejectsIncompatibleClass() throws Exception {
        try (ExternalPluginLoader loader = new ExternalPluginLoader(jar)) {
            assertThrows(IllegalArgumentException.class, () -> loader.load("java.lang.String"));
        }
    }
}
