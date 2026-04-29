package engine.io;

import java.util.List;

/**
 * LevelLoader uses FileHandler to read map data from a text file.
 */
public class LevelLoader {

    /**
     * Parses a text file into a 2D integer array map layout.
     * @param filePath The path to the level text file.
     * @return 2D integer array representing the map grid.
     */
    public static int[][] loadMap(String filePath) {
        List<String> lines = FileHandler.readLines(filePath);
        if (lines.isEmpty()) return new int[0][0];

        int rows = lines.size();
        int cols = lines.get(0).split(",").length;
        int[][] map = new int[rows][cols];

        for (int r = 0; r < rows; r++) {
            String[] tokens = lines.get(r).split(",");
            for (int c = 0; c < tokens.length && c < cols; c++) {
                try {
                    map[r][c] = Integer.parseInt(tokens[c].trim());
                } catch (NumberFormatException e) {
                    map[r][c] = 0; // Default to empty if parsing fails
                }
            }
        }
        return map;
    }
}
