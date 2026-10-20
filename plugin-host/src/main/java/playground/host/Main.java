package playground.host;

import java.io.IOException;
import java.util.ServiceConfigurationError;

public class Main {
    public static void main(String[] args) {
        try {
            LaunchOptions options = LaunchOptions.parse(args);
            if (options.help()) {
                System.out.println(LaunchOptions.USAGE);
                return;
            }
            try (PluginSession session = PluginSession.open(options)) {
                AnimationRunner.run(session, options.mode());
            }
        } catch (IllegalArgumentException | IOException | ServiceConfigurationError e) {
            System.err.println("Ошибка: " + e.getMessage());
            System.err.println("Используйте --help для списка аргументов.");
            System.exit(2);
        }
    }
}
