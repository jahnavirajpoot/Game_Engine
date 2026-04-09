package engine.pathfinding;

import java.util.ArrayList;
import java.util.List;

public class Grid {

    private final Node[][] nodes;
    public final int rows;
    public final int cols;
    public final int cellSize;       // pixel size of each cell in the world

    // 4-directional: UP, DOWN, LEFT, RIGHT
    private static final int[][] DIRS_4 = {{-1,0},{1,0},{0,-1},{0,1}};

    // 8-directional: includes diagonals
    private static final int[][] DIRS_8 = {
        {-1,0},{1,0},{0,-1},{0,1},
        {-1,-1},{-1,1},{1,-1},{1,1}
    };

    private boolean diagonalMovement = false;

    /**
     * Create a grid of given dimensions with all cells walkable.
     * @param rows     Number of rows
     * @param cols     Number of columns
     * @param cellSize Pixel size of each cell (for world-space conversion)
     */
    public Grid(int rows, int cols, int cellSize) {
        this.rows     = rows;
        this.cols     = cols;
        this.cellSize = cellSize;
        this.nodes    = new Node[rows][cols];

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                nodes[r][c] = new Node(r, c, true);
            }
        }
    }

    /** Mark a cell as an obstacle (not walkable) */
    public void setObstacle(int row, int col) {
        if (inBounds(row, col)) nodes[row][col].walkable = false;
    }

    /** Mark a cell as walkable again */
    public void clearObstacle(int row, int col) {
        if (inBounds(row, col)) nodes[row][col].walkable = true;
    }

    /** Get a node by grid coordinates */
    public Node getNode(int row, int col) {
        if (!inBounds(row, col)) return null;
        return nodes[row][col];
    }

    /** Convert world pixel position → grid node */
    public Node worldToNode(float worldX, float worldY) {
        int col = (int)(worldX / cellSize);
        int row = (int)(worldY / cellSize);
        return getNode(row, col);
    }

    /** Convert grid node → world pixel center */
    public float[] nodeToWorldCenter(Node n) {
        return new float[]{
            n.col * cellSize + cellSize / 2f,
            n.row * cellSize + cellSize / 2f
        };
    }

    /**
     * Get walkable neighbors of a node.
     * @param n    The node to query
     * @return     List of adjacent walkable nodes
     */
    public List<Node> getNeighbors(Node n) {
        List<Node> neighbors = new ArrayList<>();
        int[][] dirs = diagonalMovement ? DIRS_8 : DIRS_4;

        for (int[] d : dirs) {
            int nr = n.row + d[0];
            int nc = n.col + d[1];
            if (inBounds(nr, nc) && nodes[nr][nc].walkable) {
                neighbors.add(nodes[nr][nc]);
            }
        }
        return neighbors;
    }

    /** Reset all nodes' pathfinding state (for running new searches) */
    public void resetNodes() {
        for (Node[] row : nodes) {
            for (Node n : row) {
                n.reset();
            }
        }
    }

    /** Check if coordinates are within grid bounds */
    public boolean inBounds(int row, int col) {
        return row >= 0 && row < rows && col >= 0 && col < cols;
    }

    public void setDiagonalMovement(boolean enabled) {
        this.diagonalMovement = enabled;
    }

    public Node[][] getNodes() { return nodes; }
}
