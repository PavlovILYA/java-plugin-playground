package playground.host;

import java.nio.file.Path;

record LaunchOptions(String mode, Path jar, String className, boolean help) {
    static final String USAGE = "Использование: --mode=builtin (по умолчанию)\n"
            + "  или --mode=url --jar=<path> --class=<полное имя класса>";

    static LaunchOptions parse(String[] args) {
        String mode = "builtin";
        String jar = null;
        String className = null;
        for (String arg : args) {
            if (arg.equals("--help")) {
                return new LaunchOptions(mode, null, null, true);
            } else if (arg.startsWith("--mode=")) {
                mode = arg.substring("--mode=".length());
            } else if (arg.startsWith("--jar=")) {
                jar = arg.substring("--jar=".length());
            } else if (arg.startsWith("--class=")) {
                className = arg.substring("--class=".length());
            } else {
                throw new IllegalArgumentException("Неизвестный аргумент: " + arg);
            }
        }

        if (mode.equals("builtin")) {
            if (jar != null || className != null) {
                throw new IllegalArgumentException("--jar и --class используются только с --mode=url");
            }
            return new LaunchOptions(mode, null, null, false);
        }
        if (mode.equals("url")) {
            if (jar == null || jar.isBlank() || className == null || className.isBlank()) {
                throw new IllegalArgumentException("Для --mode=url нужны --jar=<path> и --class=<имя>");
            }
            return new LaunchOptions(mode, Path.of(jar), className, false);
        }
        throw new IllegalArgumentException("Неизвестный режим: " + mode);
    }
}
