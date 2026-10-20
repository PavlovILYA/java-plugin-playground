package playground.host;

import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.ServiceConfigurationError;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import playground.api.AnimationPlugin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpiPluginLoaderTest {
    private static final String DESCRIPTOR = "META-INF/services/playground.api.AnimationPlugin";

    @TempDir
    Path directory;

    @Test
    void independentJarsDeclareDifferentProvidersWithoutBundlingApi() throws Exception {
        assertJar(Path.of(System.getProperty("matrixJar")), "playground.matrix.MatrixRainPlugin");
        assertJar(Path.of(System.getProperty("aquariumJar")), "playground.aquarium.AquariumPlugin");
    }

    @Test
    void findsBothProvidersWithOneLoaderAndRestoresTccl() throws Exception {
        copyPlugins();
        ClassLoader original = Thread.currentThread().getContextClassLoader();
        assertThrows(ClassNotFoundException.class,
                () -> AnimationPlugin.class.getClassLoader().loadClass("playground.aquarium.AquariumPlugin"));

        try (SpiPluginLoader loader = new SpiPluginLoader(directory)) {
            assertSame(original, loader.originalTccl());
            assertSame(original, Thread.currentThread().getContextClassLoader());
            assertTrue(loader.restored());
            assertEquals(Set.of("Matrix Rain", "Aquarium"), names(loader.plugins()));
            assertEquals(Set.of("Matrix Rain", "Aquarium"), names(loader.withPluginTccl()));
            assertFalse(names(loader.withOriginalTccl()).contains("Matrix Rain"));
            assertFalse(names(loader.withOriginalTccl()).contains("Aquarium"));

            ClassLoader matrixLoader = loader.plugins().get(0).getClass().getClassLoader();
            ClassLoader aquariumLoader = loader.plugins().get(1).getClass().getClassLoader();
            assertInstanceOf(URLClassLoader.class, matrixLoader);
            assertSame(matrixLoader, aquariumLoader);
            assertSame(AnimationPlugin.class.getClassLoader(), matrixLoader.getParent());
            AnimationPlugin aquarium = loader.plugins().stream().filter(p -> p.name().equals("Aquarium"))
                    .findFirst().orElseThrow();
            String firstFrame = aquarium.renderFrame(0, 60, 20);
            assertEquals(20, firstFrame.split("\n", -1).length);
            for (String line : firstFrame.split("\n", -1)) {
                assertEquals(60, line.length());
            }
            assertTrue(firstFrame.contains("~"));
            assertNotEquals(firstFrame, aquarium.renderFrame(4, 60, 20));
        }
        assertSame(original, Thread.currentThread().getContextClassLoader());
    }

    @Test
    void restoresTcclWhenProviderDiscoveryFails() throws Exception {
        Path brokenJar = directory.resolve("broken.jar");
        try (JarOutputStream jar = new JarOutputStream(Files.newOutputStream(brokenJar))) {
            jar.putNextEntry(new JarEntry(DESCRIPTOR));
            jar.write("no.such.Plugin\n".getBytes(StandardCharsets.UTF_8));
            jar.closeEntry();
        }
        ClassLoader original = Thread.currentThread().getContextClassLoader();

        assertThrows(ServiceConfigurationError.class, () -> new SpiPluginLoader(directory));
        assertSame(original, Thread.currentThread().getContextClassLoader());
    }

    @Test
    void reportsMissingOrEmptyDirectory() throws Exception {
        assertThrows(IllegalArgumentException.class, () -> new SpiPluginLoader(directory.resolve("missing")));
        assertThrows(IllegalArgumentException.class, () -> new SpiPluginLoader(directory));
    }

    private void copyPlugins() throws Exception {
        Files.copy(Path.of(System.getProperty("matrixJar")), directory.resolve("matrix.jar"));
        Files.copy(Path.of(System.getProperty("aquariumJar")), directory.resolve("aquarium.jar"));
    }

    private static Set<String> names(List<AnimationPlugin> plugins) {
        return plugins.stream().map(AnimationPlugin::name).collect(java.util.stream.Collectors.toSet());
    }

    private static void assertJar(Path path, String implementation) throws Exception {
        try (JarFile jar = new JarFile(path.toFile())) {
            assertEquals(implementation + "\n", new String(jar.getInputStream(jar.getJarEntry(DESCRIPTOR)).readAllBytes(), StandardCharsets.UTF_8));
            assertFalse(jar.stream().anyMatch(entry -> entry.getName().equals("playground/api/AnimationPlugin.class")));
            assertTrue(jar.stream().anyMatch(entry -> entry.getName().equals(implementation.replace('.', '/') + ".class")));
        }
    }
}
