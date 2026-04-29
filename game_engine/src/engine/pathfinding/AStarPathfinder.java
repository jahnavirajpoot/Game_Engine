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
     * Find the optimal path from start to goal using A* algorithm.
     *
     * Uses Manhattan distance heuristic and 4-directional movement.
     * Maintains gCost, hCost, and parent pointers for path reconstruction.
     *
     * @param startRow  Row index of the start cell
     * @param startCol  Column index of the start cell
     * @param goalRow   Row index of the goal cell
     * @param goalCol   Column index of the goal cell
     * @return          Ordered list of Nodes from start → goal, or empty list if no path exists
     */
    public List<Node> findPath(int startRow, int startCol, int goalRow, int goalCol) {
        // Step 1: Reset grid nodes to clear previous search state
        grid.resetNodes();
        nodesExplored = 0;
        exploredNodes.clear();

        // Step 2: Validate start and goal nodes
        Node start = grid.getNode(startRow, startCol);
        Node goal  = grid.getNode(goalRow, goalCol);

        if (start == null || goal == null || !start.walkable || !goal.walkable) {
            return Collections.emptyList();
        }

        // Step 3: Initialize open and closed sets
        PriorityQueue<Node> openSet = new PriorityQueue<>(
            Comparator.comparingDouble(Node::getFCost)
        );
        Set<Node> closedSet = new HashSet<>();

        // Initialize start node costs
        start.gCost = 0;
        start.hCost = heuristic(start, goal);
        start.parent = null;
        openSet.add(start);

        // Step 4: Main A* loop
        while (!openSet.isEmpty()) {
            // Poll node with lowest fCost from open set
            Node current = openSet.poll();

            // Skip if already processed
            if (closedSet.contains(current)) {
                continue;
            }

            // Mark as processed and record for diagnostics
            closedSet.add(current);
            nodesExplored++;
            exploredNodes.add(current);

            // Goal reached — reconstruct path
            if (current.equals(goal)) {
                return reconstructPath(goal);
            }

            // Expand all walkable neighbors
            for (Node neighbor : grid.getNeighbors(current)) {
                // Skip if already processed
                if (closedSet.contains(neighbor)) {
                    continue;
                }

                // Calculate step cost (always 1.0 for 4-directional cardinal movement)
                float stepCost = 1.0f;
                float tentativeG = current.gCost + stepCost;

                // Update neighbor if we found a better path to it
                if (tentativeG < neighbor.gCost) {
                    neighbor.gCost = tentativeG;
                    neighbor.hCost = heuristic(neighbor, goal);
                    neighbor.parent = current;
                    openSet.add(neighbor);  // Will be re-evaluated next iteration
                }
            }
        }

        // No path found — open set exhausted
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

    public int        getNodesExplored() { return nodesExplored; }
    public List<Node> getExploredNodes() { return new ArrayList<>(exploredNodes); }
}
