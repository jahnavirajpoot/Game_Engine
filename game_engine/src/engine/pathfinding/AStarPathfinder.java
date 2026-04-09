package engine.pathfinding;

import java.util.*;

public class AStarPathfinder {

    private Grid grid;

    // Diagnostics
    private int        nodesExplored = 0;
    private List<Node> exploredNodes = new ArrayList<>();

    public AStarPathfinder(Grid grid) {
        this.grid = grid;
    }

    // ── ⬜ TODO PHASE 2: Main A* search ───────────────────────────────────

    /**
     * TODO (Phase 2): Find the optimal path from start to goal using A*.
     *
     * Algorithm outline to implement:
     *
     *  1. Reset grid nodes (clear g/h costs and parent pointers)
     *  2. Validate start and goal nodes exist and are walkable
     *  3. Initialize:
     *       - openSet  = PriorityQueue ordered by fCost (lowest first)
     *       - closedSet = HashSet to track fully processed nodes
     *       - start.gCost = 0
     *       - start.hCost = heuristic(start, goal)
     *       - add start to openSet
     *  4. Loop while openSet is not empty:
     *       a. Poll node with lowest fCost from openSet → 'current'
     *       b. Skip if already in closedSet
     *       c. Add current to closedSet, increment nodesExplored
     *       d. If current == goal → return reconstructPath(goal)
     *       e. For each walkable neighbor of current:
     *            - stepCost = isDiagonal(current, neighbor) ? 1.414f : 1.0f
     *            - tentativeG = current.gCost + stepCost
     *            - If tentativeG < neighbor.gCost:
     *                 neighbor.gCost  = tentativeG
     *                 neighbor.hCost  = heuristic(neighbor, goal)
     *                 neighbor.parent = current
     *                 openSet.add(neighbor)
     *  5. Return empty list if loop ends without reaching goal
     *
     * @param startRow  Row index of the start cell
     * @param startCol  Column index of the start cell
     * @param goalRow   Row index of the goal cell
     * @param goalCol   Column index of the goal cell
     * @return          Ordered list of Nodes from start → goal, or empty list
     */
    public List<Node> findPath(int startRow, int startCol, int goalRow, int goalCol) {
        // TODO Phase 2 — implement A* loop described above

        System.out.println("[AStarPathfinder] findPath() not yet implemented — Phase 2.");
        nodesExplored = 0;
        exploredNodes.clear();
        return Collections.emptyList();
    }

    // ── ✅ DONE: Heuristic function ───────────────────────────────────────

    /**
     * Manhattan distance heuristic.
     * Works best for 4-directional (cardinal) movement grids.
     * For 8-directional grids, switch to Chebyshev distance in Phase 2.
     *
     * h(a, b) = |a.row - b.row| + |a.col - b.col|
     */
    private float heuristic(Node a, Node b) {
        return Math.abs(a.row - b.row) + Math.abs(a.col - b.col);
    }

    // ── ✅ DONE: Path reconstruction ──────────────────────────────────────

    /**
     * Trace parent pointers from goal back to start, then reverse.
     * Works the same as BFS reconstruction — shared pattern.
     */
    private List<Node> reconstructPath(Node goal) {
        List<Node> path    = new ArrayList<>();
        Node       current = goal;

        while (current != null) {
            path.add(current);
            current = current.parent;
        }

        Collections.reverse(path);
        return path;
    }

    // ── ✅ DONE: Helper ───────────────────────────────────────────────────

    /** Returns true if movement from a to b is diagonal */
    private boolean isDiagonal(Node a, Node b) {
        return Math.abs(a.row - b.row) == 1 && Math.abs(a.col - b.col) == 1;
    }

    public int        getNodesExplored() { return nodesExplored; }
    public List<Node> getExploredNodes() { return new ArrayList<>(exploredNodes); }
}
