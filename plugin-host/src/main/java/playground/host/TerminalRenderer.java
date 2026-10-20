package playground.host;

import java.io.PrintStream;

public class TerminalRenderer {
    private final PrintStream out;
    private final int height;
    private int frameRow = 2;

    public TerminalRenderer(PrintStream out, int height) {
        this.out = out;
        this.height = height;
    }

    public void start(String header) {
        frameRow = header.split("\n", -1).length + 1;
        out.print("\u001b[2J\u001b[H\u001b[?25l" + header + "\n");
        out.flush();
    }

    public void render(String frame) {
        out.print("\u001b[" + frameRow + ";1H" + frame);
        out.flush();
    }

    public void close() {
        out.print("\u001b[" + (frameRow + height) + ";1H\u001b[?25h\n");
        out.flush();
    }
}
