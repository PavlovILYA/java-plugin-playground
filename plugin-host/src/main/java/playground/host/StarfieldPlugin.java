package playground.host;

import java.util.Arrays;
import playground.api.AnimationPlugin;

public class StarfieldPlugin implements AnimationPlugin {
    private static final String[] SHIP = {
            "   /\\   ",
            "  /==\\  ",
            "<|====|>",
            "  /__\\  ",
            "   ||   "
    };

    @Override
    public String name() {
        return "Starfield";
    }

    @Override
    public String renderFrame(long frame, int width, int height) {
        char[][] scene = new char[height][width];
        for (char[] row : scene) {
            Arrays.fill(row, ' ');
        }

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (isStar(x + (int) (frame / 4), y, 53)) {
                    scene[y][x] = '.';
                }
                if (isStar(x + (int) (frame / 2), y + 17, 89)) {
                    scene[y][x] = '+';
                }
                if (isStar(x + (int) frame, y + 31, 137)) {
                    scene[y][x] = '*';
                }
            }
        }

        int shipX = (width - SHIP[0].length()) / 2 + (int) Math.round(Math.sin(frame * 0.08) * 2);
        int shipY = (height - SHIP.length) / 2 + (int) Math.round(Math.sin(frame * 0.12));
        for (int y = 0; y < SHIP.length; y++) {
            for (int x = 0; x < SHIP[y].length(); x++) {
                int screenX = shipX + x;
                int screenY = shipY + y;
                char part = SHIP[y].charAt(x);
                if (part != ' ' && screenX >= 0 && screenX < width && screenY >= 0 && screenY < height) {
                    scene[screenY][screenX] = part;
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

    private static boolean isStar(int x, int y, int spacing) {
        int hash = x * 73856093 ^ y * 19349663;
        hash ^= hash >>> 16;
        return Math.floorMod(hash, spacing) == 0;
    }
}
