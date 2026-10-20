package playground.host;

import java.nio.file.Path;

record LaunchOptions(String mode, Path jar, String className, boolean help, Path pluginsDir) {
    static final String USAGE = "Использование: --mode=builtin (по умолчанию)\n"
            + "  или --mode=url --jar=<path> --class=<полное имя класса>\n"
            + "  или --mode=spi [--plugins-dir=<path>]";

    static LaunchOptions parse(String[] args) {
        String mode = "builtin";
        String jar = null;
        String className = null;
        String pluginsDir = null;
        for (String arg : args) {
            if (arg.equals("--help")) {
                return new LaunchOptions(mode, null, null, true, null);
            } else if (arg.startsWith("--mode=")) {
                mode = arg.substring("--mode=".length());
            } else if (arg.startsWith("--jar=")) {
                jar = arg.substring("--jar=".length());
            } else if (arg.startsWith("--class=")) {
                className = arg.substring("--class=".length());
            } else if (arg.startsWith("--plugins-dir=")) {
                pluginsDir = arg.substring("--plugins-dir=".length());
            } else {
                throw new IllegalArgumentException("Неизвестный аргумент: " + arg);
            }
        }

        if (mode.equals("builtin")) {
            if (jar != null || className != null) {
                throw new IllegalArgumentException("--jar и --class используются только с --mode=url");
            }
            if (pluginsDir != null) {
                throw new IllegalArgumentException("--plugins-dir используется только с --mode=spi");
            }
            return new LaunchOptions(mode, null, null, false, null);
        }
        if (mode.equals("url")) {
            if (jar == null || jar.isBlank() || className == null || className.isBlank()) {
                throw new IllegalArgumentException("Для --mode=url нужны --jar=<path> и --class=<имя>");
            }
            if (pluginsDir != null) {
                throw new IllegalArgumentException("--plugins-dir используется только с --mode=spi");
            }
            return new LaunchOptions(mode, Path.of(jar), className, false, null);
        }
        if (mode.equals("spi")) {
            if (jar != null || className != null) {
                throw new IllegalArgumentException("--jar и --class используются только с --mode=url");
            }
            if (pluginsDir != null && pluginsDir.isBlank()) {
                throw new IllegalArgumentException("--plugins-dir должен содержать путь");
            }
            return new LaunchOptions(mode, null, null, false,
                    Path.of(pluginsDir == null ? "plugins" : pluginsDir));
        }
        throw new IllegalArgumentException("Неизвестный режим: " + mode);
    }
}
