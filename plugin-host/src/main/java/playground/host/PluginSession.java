package playground.host;

import java.io.IOException;
import playground.api.AnimationPlugin;

final class PluginSession implements AutoCloseable {
    private final AnimationPlugin plugin;
    private final ExternalPluginLoader loader;
    private boolean closed;

    private PluginSession(AnimationPlugin plugin, ExternalPluginLoader loader) {
        this.plugin = plugin;
        this.loader = loader;
    }

    static PluginSession open(LaunchOptions options) throws IOException {
        if (options.mode().equals("builtin")) {
            AnimationPlugin plugin = new StarfieldPlugin();
            return new PluginSession(plugin, null);
        }

        ExternalPluginLoader loader = new ExternalPluginLoader(options.jar());
        try {
            return new PluginSession(loader.load(options.className()), loader);
        } catch (RuntimeException e) {
            try {
                loader.close();
            } catch (IOException closeError) {
                e.addSuppressed(closeError);
            }
            throw e;
        }
    }

    AnimationPlugin plugin() {
        return plugin;
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
