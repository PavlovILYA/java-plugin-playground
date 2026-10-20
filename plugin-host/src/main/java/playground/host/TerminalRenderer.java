package playground.host;

import java.io.PrintStream;

public class TerminalRenderer {
    private final PrintStream out;
    private final int height;

    public TerminalRenderer(PrintStream out, int height) {
        this.out = out;
        this.height = height;
    }

    public void start(String header) {
        out.print("\u001b[2J\u001b[H\u001b[?25l" + header + "\n");
        out.flush();
    }

    public void render(String frame) {
        out.print("\u001b[2;1H" + frame);
        out.flush();
    }

    public void close() {
        out.print("\u001b[" + (height + 2) + ";1H\u001b[?25h\n");
        out.flush();
    }
}
