package demo_games.game1;

import engine.Scene;
import engine.input.InputManager;
import engine.input.KeyboardInput;
import engine.physics.PhysicsBody;
import engine.physics.PhysicsEngine;
import engine.pathfinding.AStarPathfinder;
import engine.pathfinding.BFSPathfinder;
import engine.pathfinding.Grid;
import engine.pathfinding.Node;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Game1Scene — "Dungeon Chase"
 *
 * Full game demo integrating every engine subsystem:
 *   • Player movement via InputManager (WASD / Arrows)
 *   • PhysicsEngine for collision detection against walls
 *   • Enemy AI that chases the player using BFS or A* pathfinding
 *   • Press [P] to toggle pathfinding algorithm
 *   • Press [R] to reset positions
 *   • HUD showing algorithm, path length, nodes explored, and score
 */
public class Game1Scene extends Scene {

    // ── Dimensions ───────────────────────────────────────────────────────
    private static final int W = MainGame1.WIDTH;
    private static final int H = MainGame1.HEIGHT;

    // ── Grid ─────────────────────────────────────────────────────────────
    private static final int CELL = 40;
    private static final int COLS = W / CELL;   // 20
    private static final int ROWS = H / CELL;   // 15

    private Grid grid;
    private BFSPathfinder  bfs;
    private AStarPathfinder aStar;
    private boolean useAStar = false;

    private List<Node> currentPath = null;
    private int nodesExplored = 0;

    // ── Physics ──────────────────────────────────────────────────────────
    private PhysicsEngine physics;

    // ── Player ───────────────────────────────────────────────────────────
    private PhysicsBody player;
    private static final float PLAYER_SPD = 220f;
    private static final int   PLAYER_SZ  = 28;

    // ── Enemy ────────────────────────────────────────────────────────────
    private PhysicsBody enemy;
    private static final float ENEMY_SPD = 110f;
    private static final int   ENEMY_SZ  = 28;
    private int pathTick = 0;
    private static final int PATH_INTERVAL = 12;

    // ── Score ────────────────────────────────────────────────────────────
    private int score = 0;
    private int frameTick = 0;

    // ── Obstacle layout (row, col) ───────────────────────────────────────
    private static final int[][] WALLS = {
        // top corridor
        {2, 3}, {2, 4}, {2, 5}, {2, 6},
        // vertical divider left
        {4, 2}, {5, 2}, {6, 2}, {7, 2},
        // central block
        {5, 7}, {5, 8}, {5, 9}, {6, 7}, {6, 9},
        // right column
        {3, 13}, {4, 13}, {5, 13}, {6, 13}, {7, 13},
        // lower horizontal
        {9, 4}, {9, 5}, {9, 6}, {9, 7}, {9, 8},
        // lower-right block
        {10, 14}, {10, 15}, {11, 14}, {11, 15},
        // lower-left vertical
        {10, 1}, {11, 1}, {12, 1},
        // scattered
        {3, 17}, {4, 17}, {7, 10}, {7, 11},
        {12, 8}, {12, 9}, {12, 10},
        {8, 16}, {9, 16}, {10, 16},
    };

    // ── Input ────────────────────────────────────────────────────────────
    private InputManager input;
    private static final float DT = 1f / 60f;

    // ── Constructor ──────────────────────────────────────────────────────

    public Game1Scene(Component canvas) {
        input = InputManager.getInstance();
        input.attachTo(canvas);

        initGrid();
        initPhysics();
        initEntities();
        recalcPath();
    }

    private void initGrid() {
        grid = new Grid(ROWS, COLS, CELL);
        for (int[] w : WALLS) grid.setObstacle(w[0], w[1]);
    }

    private void initPhysics() {
        physics = new PhysicsEngine();

        // Wall bodies
        for (int[] w : WALLS) {
            physics.addBody(PhysicsBody.createStatic(
                "wall_" + w[0] + "_" + w[1],
                w[1] * CELL, w[0] * CELL, CELL, CELL));
        }

        // Screen boundaries
        physics.addBody(PhysicsBody.createStatic("bnd_t", 0, -20, W, 20));
        physics.addBody(PhysicsBody.createStatic("bnd_b", 0, H,   W, 20));
        physics.addBody(PhysicsBody.createStatic("bnd_l", -20, 0, 20, H));
        physics.addBody(PhysicsBody.createStatic("bnd_r", W, 0,   20, H));
    }

    private void initEntities() {
        float offset = (CELL - PLAYER_SZ) / 2f;

        player = new PhysicsBody("player",
            1 * CELL + offset, 1 * CELL + offset,
            PLAYER_SZ, PLAYER_SZ, 1f);
        player.affectedByGravity = false;
        player.restitution = 0f;
        physics.addBody(player);

        enemy = new PhysicsBody("enemy",
            18 * CELL + offset, 13 * CELL + offset,
            ENEMY_SZ, ENEMY_SZ, 1f);
        enemy.affectedByGravity = false;
        enemy.restitution = 0f;
        physics.addBody(enemy);

        bfs   = new BFSPathfinder(grid);
        aStar = new AStarPathfinder(grid);
    }

    // ── UPDATE ───────────────────────────────────────────────────────────

    @Override
    public void update() {
        KeyboardInput kb = input.getKeyboard();

        // Toggle algorithm
        if (kb.isJustPressed(KeyEvent.VK_P)) {
            useAStar = !useAStar;
            recalcPath();
        }

        // Reset positions
        if (kb.isJustPressed(KeyEvent.VK_R)) {
            resetPositions();
        }

        // Player velocity
        float vx = 0, vy = 0;
        if (kb.isLeft())  vx -= PLAYER_SPD;
        if (kb.isRight()) vx += PLAYER_SPD;
        if (kb.isUp())    vy -= PLAYER_SPD;
        if (kb.isDown())  vy += PLAYER_SPD;

        if (vx != 0 && vy != 0) {
            float f = 0.7071f;
            vx *= f; vy *= f;
        }
        player.velocity.x = vx;
        player.velocity.y = vy;

        // Enemy pathfinding
        pathTick++;
        if (pathTick >= PATH_INTERVAL) {
            pathTick = 0;
            recalcPath();
        }
        moveEnemy();

        // Physics step
        physics.update(DT);

        // Score — +1 every 60 frames survived
        frameTick++;
        if (frameTick >= 60) { frameTick = 0; score++; }

        input.endFrame();
    }

    // ── RENDER ───────────────────────────────────────────────────────────

    @Override
    public void render(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);

        // Background — dark dungeon
        g2.setColor(new Color(15, 12, 25));
        g2.fillRect(0, 0, W, H);

        // Grid lines
        g2.setColor(new Color(30, 25, 45));
        for (int r = 0; r <= ROWS; r++)
            g2.drawLine(0, r * CELL, W, r * CELL);
        for (int c = 0; c <= COLS; c++)
            g2.drawLine(c * CELL, 0, c * CELL, H);

        // Walls — stone blocks
        for (int[] w : WALLS) {
            int ox = w[1] * CELL, oy = w[0] * CELL;
            g2.setColor(new Color(55, 45, 70));
            g2.fillRect(ox + 1, oy + 1, CELL - 2, CELL - 2);
            g2.setColor(new Color(80, 65, 100));
            g2.drawRect(ox + 1, oy + 1, CELL - 2, CELL - 2);
            // brick lines
            g2.setColor(new Color(40, 32, 55));
            g2.drawLine(ox + CELL / 2, oy + 1, ox + CELL / 2, oy + CELL / 2);
            g2.drawLine(ox + 1, oy + CELL / 2, ox + CELL - 2, oy + CELL / 2);
        }

        // Explored nodes overlay
        List<Node> explored = useAStar ? aStar.getExploredNodes() : bfs.getExploredNodes();
        if (explored != null) {
            Color expColor = useAStar
                ? new Color(100, 60, 20, 35)
                : new Color(20, 60, 100, 35);
            g2.setColor(expColor);
            for (Node n : explored)
                g2.fillRect(n.col * CELL + 2, n.row * CELL + 2, CELL - 4, CELL - 4);
        }

        // Path visualization
        if (currentPath != null && currentPath.size() > 1) {
            g2.setStroke(new BasicStroke(2.5f));
            Color pc = useAStar
                ? new Color(255, 160, 40, 160)
                : new Color(40, 180, 255, 160);
            g2.setColor(pc);
            for (int i = 0; i < currentPath.size() - 1; i++) {
                float[] ca = grid.nodeToWorldCenter(currentPath.get(i));
                float[] cb = grid.nodeToWorldCenter(currentPath.get(i + 1));
                g2.drawLine((int) ca[0], (int) ca[1], (int) cb[0], (int) cb[1]);
            }
            for (Node n : currentPath) {
                float[] c = grid.nodeToWorldCenter(n);
                g2.fillOval((int) c[0] - 3, (int) c[1] - 3, 6, 6);
            }
            g2.setStroke(new BasicStroke(1f));
        }

        // Player — emerald
        int px = (int) player.position.x, py = (int) player.position.y;
        g2.setColor(new Color(30, 220, 120, 45));
        g2.fillOval(px - 5, py - 5, PLAYER_SZ + 10, PLAYER_SZ + 10);
        g2.setColor(new Color(40, 200, 100));
        g2.fillRoundRect(px, py, PLAYER_SZ, PLAYER_SZ, 8, 8);
        g2.setColor(new Color(100, 255, 170));
        g2.drawRoundRect(px, py, PLAYER_SZ, PLAYER_SZ, 8, 8);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, 10));
        g2.drawString("P", px + PLAYER_SZ / 2 - 3, py + PLAYER_SZ / 2 + 4);

        // Enemy — crimson
        int ex = (int) enemy.position.x, ey = (int) enemy.position.y;
        g2.setColor(new Color(255, 40, 40, 45));
        g2.fillOval(ex - 5, ey - 5, ENEMY_SZ + 10, ENEMY_SZ + 10);
        g2.setColor(new Color(200, 30, 30));
        g2.fillRoundRect(ex, ey, ENEMY_SZ, ENEMY_SZ, 8, 8);
        g2.setColor(new Color(255, 90, 90));
        g2.drawRoundRect(ex, ey, ENEMY_SZ, ENEMY_SZ, 8, 8);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, 10));
        g2.drawString("E", ex + ENEMY_SZ / 2 - 3, ey + ENEMY_SZ / 2 + 4);

        // HUD
        drawHUD(g2);
    }

    // ── HUD ──────────────────────────────────────────────────────────────

    private void drawHUD(Graphics2D g2) {
        g2.setColor(new Color(0, 0, 0, 170));
        g2.fillRoundRect(8, 8, 300, 112, 12, 12);
        g2.setColor(new Color(80, 65, 110));
        g2.drawRoundRect(8, 8, 300, 112, 12, 12);

        g2.setFont(new Font("SansSerif", Font.BOLD, 14));
        g2.setColor(new Color(200, 180, 255));
        g2.drawString("Dungeon Chase", 18, 28);

        g2.setFont(new Font("SansSerif", Font.PLAIN, 12));

        String algo = useAStar ? "A* (Optimal)" : "BFS (Breadth-First)";
        Color ac    = useAStar ? new Color(255, 180, 50) : new Color(50, 200, 255);
        g2.setColor(Color.LIGHT_GRAY); g2.drawString("Algorithm:", 18, 48);
        g2.setColor(ac);               g2.drawString(algo, 95, 48);

        int pathLen = (currentPath != null) ? currentPath.size() : 0;
        g2.setColor(Color.LIGHT_GRAY);
        g2.drawString("Path: " + pathLen + "   Explored: " + nodesExplored, 18, 66);

        g2.setColor(new Color(100, 255, 170));
        g2.drawString("Score: " + score, 18, 86);

        g2.setColor(new Color(130, 120, 160));
        g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
        g2.drawString("WASD: Move   [P]: Algorithm   [R]: Reset", 18, 106);
    }

    // ── Pathfinding helpers ──────────────────────────────────────────────

    private void recalcPath() {
        Node pn = grid.worldToNode(
            player.position.x + PLAYER_SZ / 2f,
            player.position.y + PLAYER_SZ / 2f);
        Node en = grid.worldToNode(
            enemy.position.x + ENEMY_SZ / 2f,
            enemy.position.y + ENEMY_SZ / 2f);

        if (pn == null || en == null || !pn.walkable || !en.walkable) {
            currentPath = null;
            nodesExplored = 0;
            return;
        }

        if (useAStar) {
            currentPath   = new ArrayList<>(aStar.findPath(en.row, en.col, pn.row, pn.col));
            nodesExplored = aStar.getNodesExplored();
        } else {
            currentPath   = new ArrayList<>(bfs.findPath(en.row, en.col, pn.row, pn.col));
            nodesExplored = bfs.getNodesExplored();
        }
    }

    private void moveEnemy() {
        if (currentPath == null || currentPath.size() < 2) {
            enemy.velocity.x = 0;
            enemy.velocity.y = 0;
            return;
        }

        Node target = currentPath.get(1);
        float[] tc = grid.nodeToWorldCenter(target);
        float tx = tc[0] - ENEMY_SZ / 2f;
        float ty = tc[1] - ENEMY_SZ / 2f;

        float dx = tx - enemy.position.x;
        float dy = ty - enemy.position.y;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);

        if (dist < 2f) {
            currentPath.remove(0);
            if (currentPath.size() < 2) {
                enemy.velocity.x = 0;
                enemy.velocity.y = 0;
            }
            return;
        }

        enemy.velocity.x = (dx / dist) * ENEMY_SPD;
        enemy.velocity.y = (dy / dist) * ENEMY_SPD;
    }

    private void resetPositions() {
        float offset = (CELL - PLAYER_SZ) / 2f;
        player.position.x = 1 * CELL + offset;
        player.position.y = 1 * CELL + offset;
        player.velocity.x = 0; player.velocity.y = 0;
        player.syncBounds();

        enemy.position.x = 18 * CELL + offset;
        enemy.position.y = 13 * CELL + offset;
        enemy.velocity.x = 0; enemy.velocity.y = 0;
        enemy.syncBounds();

        score = 0;
        frameTick = 0;
        recalcPath();
    }
}
