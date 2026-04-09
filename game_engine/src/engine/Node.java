package engine.pathfinding;

public class Node {

    public int row, col;        // Grid coordinates
    public boolean walkable;    // false = wall / obstacle

    // A* cost values
    public float gCost;         // Cost from start to this node
    public float hCost;         // Heuristic estimate to goal
    public Node parent;         // Previous node in the path (for backtracking)

    public Node(int row, int col, boolean walkable) {
        this.row      = row;
        this.col      = col;
        this.walkable = walkable;
        this.gCost    = Float.MAX_VALUE;
        this.hCost    = 0;
        this.parent   = null;
    }

    /** f = g + h — total estimated cost (used in A* priority ordering) */
    public float getFCost() {
        return gCost + hCost;
    }

    /** Reset costs for reuse across multiple pathfinding queries */
    public void reset() {
        gCost  = Float.MAX_VALUE;
        hCost  = 0;
        parent = null;
    }

    @Override
    public String toString() {
        return String.format("Node[%d,%d walk=%b g=%.1f h=%.1f]",
                row, col, walkable, gCost, hCost);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Node)) return false;
        Node n = (Node) o;
        return row == n.row && col == n.col;
    }

    @Override
    public int hashCode() {
        return 31 * row + col;
    }
}
