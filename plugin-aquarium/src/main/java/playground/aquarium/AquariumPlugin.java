package playground.aquarium;

import java.util.Arrays;
import playground.api.AnimationPlugin;

public class AquariumPlugin implements AnimationPlugin {
    @Override
    public String name() {
        return "Aquarium";
    }

    @Override
    public String renderFrame(long frame, int width, int height) {
        char[][] scene = new char[height][width];
        for (char[] row : scene) {
            Arrays.fill(row, ' ');
        }

        if (height > 0) {
            Arrays.fill(scene[height - 1], '~');
        }
        for (int x = 3; x < width; x += 12) {
            put(scene, x, height - 2, '|');
            put(scene, x - 1, height - 3, '/');
            put(scene, x + 1, height - 3, '\\');
        }

        for (int bubble = 0; bubble < 7; bubble++) {
            int x = 5 + bubble * 8;
            int y = height - 2 - (int) Math.floorMod(frame / 3 + bubble * 4L, Math.max(1, height - 2));
            put(scene, x, y, bubble % 2 == 0 ? 'o' : '.');
        }

        for (int fish = 0; fish < 4; fish++) {
            boolean right = fish % 2 == 0;
            int step = (int) Math.floorMod(frame / (2 + fish) + fish * 11L, width + 4);
            int x = right ? step - 3 : width - step;
            int y = 3 + fish * 4;
            String shape = right ? "><>" : "<><";
            for (int part = 0; part < shape.length(); part++) {
                put(scene, x + part, y, shape.charAt(part));
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

    private static void put(char[][] scene, int x, int y, char symbol) {
        if (y >= 0 && y < scene.length && x >= 0 && x < scene[y].length) {
            scene[y][x] = symbol;
        }
    }
}
