package playground.host;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import playground.api.AnimationPlugin;

final class PluginSession implements AutoCloseable {
    private final AnimationPlugin plugin;
    private final Closeable loader;
    private final String diagnostics;
    private boolean closed;

    private PluginSession(AnimationPlugin plugin, Closeable loader, String diagnostics) {
        this.plugin = plugin;
        this.loader = loader;
        this.diagnostics = diagnostics;
    }

    static PluginSession open(LaunchOptions options) throws IOException {
        return open(options, new InputStreamReader(System.in, StandardCharsets.UTF_8), System.out);
    }

    static PluginSession open(LaunchOptions options, Reader input, PrintStream output) throws IOException {
        if (options.mode().equals("builtin")) {
            AnimationPlugin plugin = new StarfieldPlugin();
            return new PluginSession(plugin, null, "");
        }
        if (options.mode().equals("spi")) {
            SpiPluginLoader loader = new SpiPluginLoader(options.pluginsDir());
            try {
                output.println(loader.report());
                AnimationPlugin plugin = PluginMenu.select(loader.plugins(), input, output);
                return new PluginSession(plugin, loader, loader.summary());
            } catch (RuntimeException | IOException e) {
                closeAfterFailure(loader, e);
                throw e;
            }
        }

        ExternalPluginLoader loader = new ExternalPluginLoader(options.jar());
        try {
            return new PluginSession(loader.load(options.className()), loader, "");
        } catch (RuntimeException e) {
            closeAfterFailure(loader, e);
            throw e;
        }
    }

    private static void closeAfterFailure(Closeable loader, Exception failure) {
        try {
            loader.close();
        } catch (IOException closeError) {
            failure.addSuppressed(closeError);
        }
    }

    AnimationPlugin plugin() {
        return plugin;
    }

    String diagnostics() {
        return diagnostics;
    }

    @Override
    public synchronized void close() throws IOException {
        if (!closed) {
            closed = true;
            if (loader != null) {
                loader.close();
            }
        }
    }
}
