package playground.host;

import java.io.PrintStream;

public class TerminalRenderer {
    private final PrintStream out;
    private final int height;
    private int frameRow = 2;
    private boolean closed;

    public TerminalRenderer(PrintStream out, int height) {
        this.out = out;
        this.height = height;
    }

    public synchronized void start(String header) {
        if (closed) {
            return;
        }
        frameRow = header.split("\n", -1).length + 1;
        out.print("\u001b[2J\u001b[H\u001b[?25l" + header + "\n");
        out.flush();
    }

    public synchronized void render(String frame) {
        if (closed) {
            return;
        }
        out.print("\u001b[" + frameRow + ";1H" + frame);
        out.flush();
    }

    public synchronized void close() {
        if (closed) {
            return;
        }
        closed = true;
        out.print("\u001b[" + (frameRow + height) + ";1H\u001b[?25h\n");
        out.flush();
    }
}
