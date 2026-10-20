package playground.matrix;

import java.util.Arrays;
import playground.api.AnimationPlugin;

public class MatrixRainPlugin implements AnimationPlugin {
    private static final String GLYPHS = "01ZX<>|/";

    @Override
    public String name() {
        return "Matrix Rain";
    }

    @Override
    public String renderFrame(long frame, int width, int height) {
        char[][] scene = new char[height][width];
        for (char[] row : scene) {
            Arrays.fill(row, ' ');
        }

        for (int x = 0; x < width; x += 3) {
            int length = 4 + x % 6;
            int speed = 1 + (x / 3) % 3;
            int head = (int) Math.floorMod(frame * speed + x * 7L, height + length) - length;
            for (int distance = 0; distance < length; distance++) {
                int y = head - distance;
                if (y >= 0 && y < height) {
                    scene[y][x] = distance == 0 ? '@'
                            : distance >= length - 2 ? '.'
                            : GLYPHS.charAt(Math.floorMod(x * 31 + y * 17 + (int) frame, GLYPHS.length()));
                }
            }
        }

        StringBuilder result = new StringBuilder(height * (width + 1));
        for (int y = 0; y < height; y++) {
            if (y > 0) {
                result.append('\n');
            }
            result.append(scene[y]);
        }
        return result.toString();
    }
}
