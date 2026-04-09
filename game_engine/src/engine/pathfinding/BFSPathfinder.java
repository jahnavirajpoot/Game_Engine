package engine.pathfinding;

import java.util.*;

public class BFSPathfinder {

    private Grid grid;

    // Diagnostics: how many nodes were explored in last search
    private int nodesExplored = 0;
    private List<Node> exploredNodes = new ArrayList<>();

    public BFSPathfinder(Grid grid) {
        this.grid = grid;
    }

    public List<Node> findPath(int startRow, int startCol, int goalRow, int goalCol) {
        grid.resetNodes();
        nodesExplored = 0;
        exploredNodes.clear();

        Node start = grid.getNode(startRow, startCol);
        Node goal  = grid.getNode(goalRow,  goalCol);

        if (start == null || goal == null || !start.walkable || !goal.walkable) {
            return Collections.emptyList();
        }

        Queue<Node> queue   = new LinkedList<>();
        Set<Node>   visited = new HashSet<>();

        queue.add(start);
        visited.add(start);
        start.parent = null;

        while (!queue.isEmpty()) {
            Node current = queue.poll();
            nodesExplored++;
            exploredNodes.add(current);

            // Goal reached — reconstruct path
            if (current.equals(goal)) {
                return reconstructPath(goal);
            }

            // Expand neighbors
            for (Node neighbor : grid.getNeighbors(current)) {
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    neighbor.parent = current;
                    queue.add(neighbor);
                }
            }
        }

        // No path found
        return Collections.emptyList();
    }

    /** Trace parent pointers from goal back to start, then reverse */
    private List<Node> reconstructPath(Node goal) {
        List<Node> path = new ArrayList<>();
        Node current = goal;
        while (current != null) {
            path.add(current);
            current = current.parent;
        }
        Collections.reverse(path);
        return path;
    }

    public int getNodesExplored() { return nodesExplored; }
    public List<Node> getExploredNodes() { return new ArrayList<>(exploredNodes); }
}
