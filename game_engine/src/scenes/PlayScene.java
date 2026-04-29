package scenes;

import engine.Scene;
import engine.input.InputManager;
import engine.input.KeyboardInput;
import engine.physics.PhysicsBody;
import engine.physics.PhysicsEngine;
import engine.physics.Vector2D;
import engine.pathfinding.AStarPathfinder;
import engine.pathfinding.BFSPathfinder;
import engine.pathfinding.Grid;
import engine.pathfinding.Node;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.List;

/**
 * PlayScene — Full game scene demonstrating:
 *   • Player movement via InputManager (WASD / Arrow keys)
 *   • PhysicsEngine update with collision detection
 *   • Enemy AI chasing player using BFS or A* pathfinding
 *   • Toggle pathfinding algorithm with 'P' key
 *   • Obstacle walls on the grid
 *   • HUD overlay showing algorithm, path length, and nodes explored
 */
public class PlayScene extends Scene {

    // ── Screen ────────────────────────────────────────────────────────────
    private static final int SCREEN_W = 800;
    private static final int SCREEN_H = 600;

    // ── Grid / Pathfinding ────────────────────────────────────────────────
    private static final int CELL_SIZE = 40;
    private static final int GRID_COLS = SCREEN_W / CELL_SIZE;  // 20
    private static final int GRID_ROWS = SCREEN_H / CELL_SIZE;  // 15

    private Grid grid;
    private BFSPathfinder  bfs;
    private AStarPathfinder aStar;
    private boolean useAStar = false;   // false = BFS, true = A*

    private List<Node> currentPath = null;
    private int nodesExplored = 0;

    // ── Physics ───────────────────────────────────────────────────────────
    private PhysicsEngine physics;

    // ── Player ────────────────────────────────────────────────────────────
    private PhysicsBody playerBody;
    private static final float PLAYER_SPEED = 200f;   // pixels / sec
    private static final int   PLAYER_SIZE  = 30;

    // ── Enemy ─────────────────────────────────────────────────────────────
    private PhysicsBody enemyBody;
    private static final float ENEMY_SPEED = 120f;    // pixels / sec
    private static final int   ENEMY_SIZE  = 30;
    private int pathRecalcCounter = 0;
    private static final int PATH_RECALC_INTERVAL = 15;  // recalculate every N frames

    // ── Obstacles (static physics bodies for walls) ───────────────────────
    private PhysicsBody[] wallBodies;

    // ── Obstacle map (grid coords: row, col) ─────────────────────────────
    // Defines walls on the grid for both pathfinding and physics
    private static final int[][] OBSTACLES = {
        // Horizontal wall cluster (upper area)
        {3, 4}, {3, 5}, {3, 6}, {3, 7}, {3, 8},
        // Vertical wall cluster (left side)
        {5, 3}, {6, 3}, {7, 3}, {8, 3},
        // Horizontal wall cluster (middle)
        {7, 7}, {7, 8}, {7, 9}, {7, 10}, {7, 11},
        // L-shaped wall (right side)
        {4, 14}, {5, 14}, {6, 14}, {6, 15}, {6, 16},
        // Small block (lower area)
        {10, 6}, {10, 7}, {11, 6}, {11, 7},
        // Diagonal-ish wall (lower right)
        {10, 13}, {11, 14}, {12, 15},
        // Extra walls for challenge
        {2, 11}, {2, 12}, {2, 13},
        {9, 17}, {10, 17}, {11, 17},
    };

    // ── Input ─────────────────────────────────────────────────────────────
    private InputManager input;
    private boolean inputAttached = false;

    // ── Delta time (fixed ~60fps) ─────────────────────────────────────────
    private static final float DT = 1f / 60f;

    // ── Constructor ───────────────────────────────────────────────────────

    public PlayScene() {
        initInput();
        initGrid();
        initPhysics();
        initEntities();
        recalculatePath();
    }

    private void initInput() {
        input = InputManager.getInstance();
    }

    private void initGrid() {
        grid = new Grid(GRID_ROWS, GRID_COLS, CELL_SIZE);

        // Place obstacles on the grid
        for (int[] obs : OBSTACLES) {
            grid.setObstacle(obs[0], obs[1]);
        }
    }

    private void initPhysics() {
        physics = new PhysicsEngine();

        // Create static wall bodies matching each grid obstacle
        wallBodies = new PhysicsBody[OBSTACLES.length];
        for (int i = 0; i < OBSTACLES.length; i++) {
            int row = OBSTACLES[i][0];
            int col = OBSTACLES[i][1];
            PhysicsBody wall = PhysicsBody.createStatic(
                "wall_" + row + "_" + col,
                col * CELL_SIZE, row * CELL_SIZE,
                CELL_SIZE, CELL_SIZE
            );
            wallBodies[i] = wall;
            physics.addBody(wall);
        }

        // Screen boundary walls (invisible)
        physics.addBody(PhysicsBody.createStatic("bound_top",    0, -20, SCREEN_W, 20));
        physics.addBody(PhysicsBody.createStatic("bound_bottom", 0, SCREEN_H, SCREEN_W, 20));
        physics.addBody(PhysicsBody.createStatic("bound_left",  -20, 0, 20, SCREEN_H));
        physics.addBody(PhysicsBody.createStatic("bound_right",  SCREEN_W, 0, 20, SCREEN_H));
    }

    private void initEntities() {
        // Player starts top-left area (grid cell 1,1)
        float px = 1 * CELL_SIZE + (CELL_SIZE - PLAYER_SIZE) / 2f;
        float py = 1 * CELL_SIZE + (CELL_SIZE - PLAYER_SIZE) / 2f;
        playerBody = new PhysicsBody("player", px, py, PLAYER_SIZE, PLAYER_SIZE, 1f);
        playerBody.affectedByGravity = false;  // top-down game
        playerBody.restitution = 0f;
        physics.addBody(playerBody);

        // Enemy starts bottom-right area (grid cell 13,18)
        float ex = 18 * CELL_SIZE + (CELL_SIZE - ENEMY_SIZE) / 2f;
        float ey = 13 * CELL_SIZE + (CELL_SIZE - ENEMY_SIZE) / 2f;
        enemyBody = new PhysicsBody("enemy", ex, ey, ENEMY_SIZE, ENEMY_SIZE, 1f);
        enemyBody.affectedByGravity = false;   // top-down game
        enemyBody.restitution = 0f;
        physics.addBody(enemyBody);

        // Pathfinders
        bfs   = new BFSPathfinder(grid);
        aStar = new AStarPathfinder(grid);
    }

    // ── UPDATE ────────────────────────────────────────────────────────────

    @Override
    public void update() {
        // Lazy-attach input to the focused window component
        tryAttachInput();

        KeyboardInput kb = input.getKeyboard();

        // ── Toggle pathfinding algorithm (P key) ──────────────────────────
        if (kb.isJustPressed(KeyEvent.VK_P)) {
            useAStar = !useAStar;
            recalculatePath();
        }

        // ── Player movement ──────────────────────────────────────────────
        float vx = 0, vy = 0;
        if (kb.isLeft())  vx -= PLAYER_SPEED;
        if (kb.isRight()) vx += PLAYER_SPEED;
        if (kb.isUp())    vy -= PLAYER_SPEED;
        if (kb.isDown())  vy += PLAYER_SPEED;

        // Normalize diagonal movement so it isn't faster
        if (vx != 0 && vy != 0) {
            float factor = (float)(1.0 / Math.sqrt(2));
            vx *= factor;
            vy *= factor;
        }

        playerBody.velocity.x = vx;
        playerBody.velocity.y = vy;

        // ── Enemy AI: follow path toward player ──────────────────────────
        pathRecalcCounter++;
        if (pathRecalcCounter >= PATH_RECALC_INTERVAL) {
            pathRecalcCounter = 0;
            recalculatePath();
        }
        moveEnemyAlongPath();

        // ── Physics step ─────────────────────────────────────────────────
        physics.update(DT);

        // ── End-of-frame input housekeeping ──────────────────────────────
        input.endFrame();
    }

    // ── RENDER ────────────────────────────────────────────────────────────

    @Override
    public void render(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // ── Background ───────────────────────────────────────────────────
        g2.setColor(new Color(20, 20, 30));
        g2.fillRect(0, 0, SCREEN_W, SCREEN_H);

        // ── Grid lines ──────────────────────────────────────────────────
        g2.setColor(new Color(40, 40, 55));
        for (int r = 0; r <= GRID_ROWS; r++) {
            g2.drawLine(0, r * CELL_SIZE, SCREEN_W, r * CELL_SIZE);
        }
        for (int c = 0; c <= GRID_COLS; c++) {
            g2.drawLine(c * CELL_SIZE, 0, c * CELL_SIZE, SCREEN_H);
        }

        // ── Obstacles ────────────────────────────────────────────────────
        for (int[] obs : OBSTACLES) {
            int ox = obs[1] * CELL_SIZE;
            int oy = obs[0] * CELL_SIZE;
            g2.setColor(new Color(70, 70, 90));
            g2.fillRect(ox + 1, oy + 1, CELL_SIZE - 2, CELL_SIZE - 2);
            g2.setColor(new Color(100, 100, 130));
            g2.drawRect(ox + 1, oy + 1, CELL_SIZE - 2, CELL_SIZE - 2);
        }

        // ── Explored nodes (faint overlay) ──────────────────────────────
        List<Node> explored = useAStar ? aStar.getExploredNodes() : bfs.getExploredNodes();
        if (explored != null) {
            g2.setColor(new Color(60, 60, 120, 50));
            for (Node n : explored) {
                g2.fillRect(n.col * CELL_SIZE + 2, n.row * CELL_SIZE + 2,
                            CELL_SIZE - 4, CELL_SIZE - 4);
            }
        }

        // ── Path visualization ──────────────────────────────────────────
        if (currentPath != null && currentPath.size() > 1) {
            g2.setStroke(new BasicStroke(3f));
            Color pathColor = useAStar
                ? new Color(255, 180, 50, 180)   // orange for A*
                : new Color(50, 200, 255, 180);  // cyan for BFS

            for (int i = 0; i < currentPath.size() - 1; i++) {
                Node a = currentPath.get(i);
                Node b = currentPath.get(i + 1);
                float[] ca = grid.nodeToWorldCenter(a);
                float[] cb = grid.nodeToWorldCenter(b);
                g2.setColor(pathColor);
                g2.drawLine((int) ca[0], (int) ca[1], (int) cb[0], (int) cb[1]);
            }

            // Draw small dots at path nodes
            g2.setColor(pathColor);
            for (Node n : currentPath) {
                float[] c = grid.nodeToWorldCenter(n);
                g2.fillOval((int) c[0] - 4, (int) c[1] - 4, 8, 8);
            }
            g2.setStroke(new BasicStroke(1f));
        }

        // ── Player ──────────────────────────────────────────────────────
        int px = (int) playerBody.position.x;
        int py = (int) playerBody.position.y;

        // Glow effect
        g2.setColor(new Color(0, 180, 255, 40));
        g2.fillOval(px - 6, py - 6, PLAYER_SIZE + 12, PLAYER_SIZE + 12);

        // Body
        g2.setColor(new Color(0, 200, 255));
        g2.fillRoundRect(px, py, PLAYER_SIZE, PLAYER_SIZE, 8, 8);

        // Border
        g2.setColor(new Color(100, 230, 255));
        g2.drawRoundRect(px, py, PLAYER_SIZE, PLAYER_SIZE, 8, 8);

        // Label
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, 10));
        g2.drawString("P", px + PLAYER_SIZE / 2 - 3, py + PLAYER_SIZE / 2 + 4);

        // ── Enemy ───────────────────────────────────────────────────────
        int ex = (int) enemyBody.position.x;
        int ey = (int) enemyBody.position.y;

        // Glow effect
        g2.setColor(new Color(255, 50, 50, 40));
        g2.fillOval(ex - 6, ey - 6, ENEMY_SIZE + 12, ENEMY_SIZE + 12);

        // Body
        g2.setColor(new Color(220, 50, 50));
        g2.fillRoundRect(ex, ey, ENEMY_SIZE, ENEMY_SIZE, 8, 8);

        // Border
        g2.setColor(new Color(255, 100, 100));
        g2.drawRoundRect(ex, ey, ENEMY_SIZE, ENEMY_SIZE, 8, 8);

        // Label
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, 10));
        g2.drawString("E", ex + ENEMY_SIZE / 2 - 3, ey + ENEMY_SIZE / 2 + 4);

        // ── HUD ─────────────────────────────────────────────────────────
        drawHUD(g2);
    }

    // ── HUD ──────────────────────────────────────────────────────────────

    private void drawHUD(Graphics2D g2) {
        // Semi-transparent panel
        g2.setColor(new Color(0, 0, 0, 160));
        g2.fillRoundRect(8, 8, 280, 100, 12, 12);
        g2.setColor(new Color(100, 100, 140));
        g2.drawRoundRect(8, 8, 280, 100, 12, 12);

        g2.setFont(new Font("SansSerif", Font.BOLD, 13));
        g2.setColor(new Color(200, 200, 255));
        g2.drawString("Game Engine Demo", 18, 28);

        g2.setFont(new Font("SansSerif", Font.PLAIN, 12));

        // Algorithm indicator
        String algo = useAStar ? "A* (Optimal)" : "BFS (Breadth-First)";
        Color algoColor = useAStar ? new Color(255, 180, 50) : new Color(50, 200, 255);
        g2.setColor(Color.LIGHT_GRAY);
        g2.drawString("Algorithm: ", 18, 48);
        g2.setColor(algoColor);
        g2.drawString(algo, 90, 48);

        // Path info
        int pathLen = (currentPath != null) ? currentPath.size() : 0;
        g2.setColor(Color.LIGHT_GRAY);
        g2.drawString("Path length: " + pathLen + "    Explored: " + nodesExplored, 18, 66);

        // Controls
        g2.setColor(new Color(150, 150, 170));
        g2.drawString("WASD/Arrows: Move    [P]: Toggle Algorithm", 18, 86);

        // Player position
        g2.setColor(new Color(100, 100, 120));
        g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
        g2.drawString(String.format("Player: (%.0f, %.0f)  Enemy: (%.0f, %.0f)",
            playerBody.position.x, playerBody.position.y,
            enemyBody.position.x, enemyBody.position.y), 18, 102);
    }

    // ── Pathfinding helpers ──────────────────────────────────────────────

    private void recalculatePath() {
        // Convert player and enemy world positions to grid coordinates
        Node playerNode = grid.worldToNode(
            playerBody.position.x + PLAYER_SIZE / 2f,
            playerBody.position.y + PLAYER_SIZE / 2f
        );
        Node enemyNode = grid.worldToNode(
            enemyBody.position.x + ENEMY_SIZE / 2f,
            enemyBody.position.y + ENEMY_SIZE / 2f
        );

        if (playerNode == null || enemyNode == null) {
            currentPath = null;
            nodesExplored = 0;
            return;
        }
        if (!playerNode.walkable || !enemyNode.walkable) {
            currentPath = null;
            nodesExplored = 0;
            return;
        }

        if (useAStar) {
            currentPath   = aStar.findPath(enemyNode.row, enemyNode.col,
                                            playerNode.row, playerNode.col);
            nodesExplored = aStar.getNodesExplored();
        } else {
            currentPath   = bfs.findPath(enemyNode.row, enemyNode.col,
                                          playerNode.row, playerNode.col);
            nodesExplored = bfs.getNodesExplored();
        }
    }

    private void moveEnemyAlongPath() {
        if (currentPath == null || currentPath.size() < 2) {
            enemyBody.velocity.x = 0;
            enemyBody.velocity.y = 0;
            return;
        }

        // Target the next node in the path (index 1; index 0 is enemy's current cell)
        Node target = currentPath.get(1);
        float[] targetCenter = grid.nodeToWorldCenter(target);

        // Offset to center the enemy body in the cell
        float targetX = targetCenter[0] - ENEMY_SIZE / 2f;
        float targetY = targetCenter[1] - ENEMY_SIZE / 2f;

        float diffX = targetX - enemyBody.position.x;
        float diffY = targetY - enemyBody.position.y;
        float dist  = (float) Math.sqrt(diffX * diffX + diffY * diffY);

        if (dist < 2f) {
            // Reached this waypoint — remove it so we advance
            currentPath.remove(0);
            if (currentPath.size() < 2) {
                enemyBody.velocity.x = 0;
                enemyBody.velocity.y = 0;
            }
            return;
        }

        // Normalize and scale to enemy speed
        float nx = diffX / dist;
        float ny = diffY / dist;
        enemyBody.velocity.x = nx * ENEMY_SPEED;
        enemyBody.velocity.y = ny * ENEMY_SPEED;
    }

    // ── Input attachment helper ──────────────────────────────────────────

    /**
     * Attempt to attach InputManager to the active AWT window.
     * Called lazily because the Canvas may not be displayable at construction time.
     */
    private void tryAttachInput() {
        if (inputAttached) return;

        // Find the first visible Frame (the Game window)
        for (Frame frame : Frame.getFrames()) {
            if (frame.isVisible()) {
                // Attach to the Canvas inside the frame
                Component[] components = frame.getComponents();
                for (Component comp : components) {
                    if (comp instanceof Canvas) {
                        input.attachTo(comp);
                        inputAttached = true;
                        return;
                    }
                }
                // Fallback: attach to the frame itself
                input.attachTo(frame);
                inputAttached = true;
                return;
            }
        }
    }
}