package playground.host;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Reader;
import java.util.List;
import playground.api.AnimationPlugin;

final class PluginMenu {
    static AnimationPlugin select(List<AnimationPlugin> plugins, Reader input, PrintStream output) throws IOException {
        if (plugins.isEmpty()) {
            throw new IllegalArgumentException("SPI-провайдеры не найдены в JAR");
        }

        output.println("╔══════════════════════════════════════╗");
        output.println("║         PLUGIN PLAYGROUND            ║");
        output.println("╚══════════════════════════════════════╝");
        output.println("Discovery mode: ServiceLoader");
        output.println("Available plugins:");
        for (int i = 0; i < plugins.size(); i++) {
            output.println("  " + (i + 1) + ". " + plugins.get(i).name());
        }

        BufferedReader reader = new BufferedReader(input);
        while (true) {
            output.print("Select plugin [1-" + plugins.size() + "]: ");
            output.flush();
            String line = reader.readLine();
            if (line == null) {
                throw new IllegalArgumentException("Ввод закрыт до выбора плагина");
            }
            try {
                int selected = Integer.parseInt(line.trim());
                if (selected >= 1 && selected <= plugins.size()) {
                    return plugins.get(selected - 1);
                }
            } catch (NumberFormatException ignored) {
                // Сообщение ниже одинаково для текста и номера вне диапазона.
            }
            output.println("Введите номер от 1 до " + plugins.size() + ".");
        }
    }
}
