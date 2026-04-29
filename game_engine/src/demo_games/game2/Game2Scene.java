package demo_games.game2;

import engine.Scene;
import engine.input.InputManager;
import engine.input.KeyboardInput;
import engine.input.MouseInput;
import engine.pathfinding.AStarPathfinder;
import engine.pathfinding.BFSPathfinder;
import engine.pathfinding.Grid;
import engine.pathfinding.Node;
import engine.audio.AudioEngine;
import engine.ui.PauseMenu;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Game2Scene — "Pathfinding Lab"
 *
 * Interactive pathfinding visualization demo:
 *   • Left-click  — set destination (goal) marker
 *   • Right-click — toggle wall obstacle on a cell
 *   • [P]         — toggle BFS / A*
 *   • [R]         — reset grid (clear all walls & path)
 *   • [SPACE]     — recalculate path manually
 *   • WASD/Arrows — move the start marker
 *
 * Displays explored nodes, final path, and statistics in real time.
 */
public class Game2Scene extends Scene {

    // ── Dimensions ───────────────────────────────────────────────────────
    private static final int W = MainGame2.WIDTH;
    private static final int H = MainGame2.HEIGHT;

    // ── Grid ─────────────────────────────────────────────────────────────
    private static final int CELL = 32;
    private static final int COLS = W / CELL;   // 25
    private static final int ROWS = H / CELL;   // 18 (576 visible, 24px bottom bar)

    private Grid grid;
    private BFSPathfinder  bfs;
    private AStarPathfinder aStar;
    private boolean useAStar = false;

    // ── Start / Goal grid coordinates ────────────────────────────────────
    private int startRow = 1,  startCol = 1;
    private int goalRow  = 16, goalCol  = 23;

    // ── Path state ───────────────────────────────────────────────────────
    private List<Node> currentPath  = null;
    private List<Node> exploredList = null;
    private int nodesExplored  = 0;
    private long searchTimeNs  = 0;
    private boolean pathDirty  = true;     // flag to auto-recalculate

    // ── Input ────────────────────────────────────────────────────────────
    private InputManager input;

    // ── Pulse animation ──────────────────────────────────────────────────
    private int animTick = 0;

    // ── New Features ─────────────────────────────────────────────────────
    private AudioEngine audio;
    private PauseMenu pauseMenu;

    // ── Constructor ──────────────────────────────────────────────────────

    public Game2Scene(Component canvas) {
        input = InputManager.getInstance();
        input.attachTo(canvas);

        audio = new AudioEngine();
        audio.loadSound("click", "/assets/audio/click.wav");
        audio.loadSound("thud", "/assets/audio/thud.wav");
        audio.loadSound("chime", "/assets/audio/chime.wav");

        pauseMenu = new PauseMenu();

        grid  = new Grid(ROWS, COLS, CELL);
        bfs   = new BFSPathfinder(grid);
        aStar = new AStarPathfinder(grid);

        // Seed a few default obstacles to make it interesting
        int[][] defaults = {
            {4, 5}, {5, 5}, {6, 5}, {7, 5}, {8, 5},
            {8, 6}, {8, 7}, {8, 8}, {8, 9},
            {4, 12}, {5, 12}, {6, 12}, {7, 12},
            {10, 15}, {10, 16}, {10, 17}, {10, 18},
            {11, 18}, {12, 18}, {13, 18},
            {14, 10}, {14, 11}, {14, 12}, {14, 13},
        };
        for (int[] o : defaults) {
            if (grid.inBounds(o[0], o[1])) grid.setObstacle(o[0], o[1]);
        }

        recalcPath();
    }

    // ── UPDATE ───────────────────────────────────────────────────────────

    @Override
    public void update() {
        KeyboardInput kb = input.getKeyboard();
        MouseInput    ms = input.getMouse();

        if (kb.isJustPressed(KeyEvent.VK_ESCAPE)) {
            pauseMenu.togglePause();
        }

        if (pauseMenu.isPaused()) {
            pauseMenu.update();
            input.endFrame();
            return;
        }

        animTick++;

        // Toggle algorithm
        if (kb.isJustPressed(KeyEvent.VK_P)) {
            useAStar = !useAStar;
            pathDirty = true;
        }

        // Reset grid
        if (kb.isJustPressed(KeyEvent.VK_R)) {
            resetGrid();
        }

        // Manual recalc
        if (kb.isJump()) {   // SPACE
            audio.playSound("chime");
            pathDirty = true;
        }

        // Move start marker with WASD
        boolean moved = false;
        if (kb.isJustPressed(KeyEvent.VK_W) || kb.isJustPressed(KeyEvent.VK_UP)) {
            if (startRow > 0) { startRow--; moved = true; }
        }
        if (kb.isJustPressed(KeyEvent.VK_S) || kb.isJustPressed(KeyEvent.VK_DOWN)) {
            if (startRow < ROWS - 1) { startRow++; moved = true; }
        }
        if (kb.isJustPressed(KeyEvent.VK_A) || kb.isJustPressed(KeyEvent.VK_LEFT)) {
            if (startCol > 0) { startCol--; moved = true; }
        }
        if (kb.isJustPressed(KeyEvent.VK_D) || kb.isJustPressed(KeyEvent.VK_RIGHT)) {
            if (startCol < COLS - 1) { startCol++; moved = true; }
        }
        if (moved) pathDirty = true;

        // Left click — set goal
        if (ms.isLeftClicked()) {
            int col = ms.getX() / CELL;
            int row = ms.getY() / CELL;
            if (grid.inBounds(row, col)) {
                Node n = grid.getNode(row, col);
                if (n != null && n.walkable) {
                    goalRow = row;
                    goalCol = col;
                    audio.playSound("click");
                    pathDirty = true;
                }
            }
        }

        // Right click — toggle obstacle
        if (ms.isRightClicked()) {
            int col = ms.getX() / CELL;
            int row = ms.getY() / CELL;
            if (grid.inBounds(row, col)) {
                // Don't wall-off start or goal
                if (!(row == startRow && col == startCol) &&
                    !(row == goalRow && col == goalCol)) {
                    Node n = grid.getNode(row, col);
                    if (n != null) {
                        if (n.walkable) {
                            grid.setObstacle(row, col);
                        } else {
                            grid.clearObstacle(row, col);
                        }
                        audio.playSound("thud");
                        pathDirty = true;
                    }
                }
            }
        }

        // Auto-recalculate when dirty
        if (pathDirty) {
            recalcPath();
            pathDirty = false;
        }

        input.endFrame();
    }

    // ── RENDER ───────────────────────────────────────────────────────────

    @Override
    public void render(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);

        // Background — clean dark
        g2.setColor(new Color(18, 22, 32));
        g2.fillRect(0, 0, W, H);

        // Grid cells
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                Node n = grid.getNode(r, c);
                int x = c * CELL, y = r * CELL;

                if (n != null && !n.walkable) {
                    // Wall
                    g2.setColor(new Color(50, 55, 75));
                    g2.fillRect(x + 1, y + 1, CELL - 2, CELL - 2);
                    g2.setColor(new Color(70, 75, 95));
                    g2.drawRect(x + 1, y + 1, CELL - 2, CELL - 2);
                } else {
                    // Walkable — subtle fill
                    g2.setColor(new Color(25, 30, 42));
                    g2.fillRect(x + 1, y + 1, CELL - 2, CELL - 2);
                }
            }
        }

        // Grid lines
        g2.setColor(new Color(35, 40, 55));
        for (int r = 0; r <= ROWS; r++) g2.drawLine(0, r * CELL, W, r * CELL);
        for (int c = 0; c <= COLS; c++) g2.drawLine(c * CELL, 0, c * CELL, H);

        // Explored nodes — faint
        if (exploredList != null) {
            Color ec = useAStar
                ? new Color(120, 80, 20, 45)
                : new Color(20, 80, 120, 45);
            g2.setColor(ec);
            for (Node n : exploredList)
                g2.fillRect(n.col * CELL + 3, n.row * CELL + 3, CELL - 6, CELL - 6);
        }

        // Path line
        if (currentPath != null && currentPath.size() > 1) {
            g2.setStroke(new BasicStroke(3f));
            Color pc = useAStar
                ? new Color(255, 170, 40, 200)
                : new Color(40, 190, 255, 200);
            g2.setColor(pc);
            for (int i = 0; i < currentPath.size() - 1; i++) {
                float[] ca = grid.nodeToWorldCenter(currentPath.get(i));
                float[] cb = grid.nodeToWorldCenter(currentPath.get(i + 1));
                g2.drawLine((int) ca[0], (int) ca[1], (int) cb[0], (int) cb[1]);
            }
            // Path dots
            for (Node n : currentPath) {
                float[] c = grid.nodeToWorldCenter(n);
                g2.fillOval((int) c[0] - 4, (int) c[1] - 4, 8, 8);
            }
            g2.setStroke(new BasicStroke(1f));
        }

        // Start marker — green diamond pulse
        {
            int sx = startCol * CELL + CELL / 2;
            int sy = startRow * CELL + CELL / 2;
            int pulse = 10 + (int)(2 * Math.sin(animTick * 0.08));
            g2.setColor(new Color(40, 220, 120, 60));
            g2.fillOval(sx - pulse, sy - pulse, pulse * 2, pulse * 2);
            g2.setColor(new Color(50, 240, 130));
            g2.fillOval(sx - 6, sy - 6, 12, 12);
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("SansSerif", Font.BOLD, 9));
            g2.drawString("S", sx - 3, sy + 4);
        }

        // Goal marker — red diamond pulse
        {
            int gx = goalCol * CELL + CELL / 2;
            int gy = goalRow * CELL + CELL / 2;
            int pulse = 10 + (int)(2 * Math.sin(animTick * 0.08 + Math.PI));
            g2.setColor(new Color(255, 60, 60, 60));
            g2.fillOval(gx - pulse, gy - pulse, pulse * 2, pulse * 2);
            g2.setColor(new Color(240, 60, 60));
            g2.fillOval(gx - 6, gy - 6, 12, 12);
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("SansSerif", Font.BOLD, 9));
            g2.drawString("G", gx - 3, gy + 4);
        }

        // Mouse hover highlight
        {
            MouseInput ms = input.getMouse();
            int hc = ms.getX() / CELL;
            int hr = ms.getY() / CELL;
            if (grid.inBounds(hr, hc)) {
                g2.setColor(new Color(255, 255, 255, 20));
                g2.fillRect(hc * CELL + 1, hr * CELL + 1, CELL - 2, CELL - 2);
            }
        }

        // HUD
        drawHUD(g2);
        
        // Pause Menu
        pauseMenu.render(g2, W, H);
    }

    // ── HUD ──────────────────────────────────────────────────────────────

    private void drawHUD(Graphics2D g2) {
        int panelW = 320, panelH = 120;
        int px = W - panelW - 8, py = 8;

        g2.setColor(new Color(0, 0, 0, 180));
        g2.fillRoundRect(px, py, panelW, panelH, 12, 12);
        g2.setColor(new Color(60, 70, 100));
        g2.drawRoundRect(px, py, panelW, panelH, 12, 12);

        int tx = px + 12, ty = py + 18;

        g2.setFont(new Font("SansSerif", Font.BOLD, 13));
        g2.setColor(new Color(180, 200, 255));
        g2.drawString("Pathfinding Lab", tx, ty);

        g2.setFont(new Font("SansSerif", Font.PLAIN, 12));

        ty += 20;
        String algo = useAStar ? "A* (Optimal)" : "BFS (Breadth-First)";
        Color ac    = useAStar ? new Color(255, 180, 50) : new Color(50, 200, 255);
        g2.setColor(Color.LIGHT_GRAY); g2.drawString("Algorithm:", tx, ty);
        g2.setColor(ac);               g2.drawString(algo, tx + 75, ty);

        ty += 18;
        int pathLen = (currentPath != null) ? currentPath.size() : 0;
        g2.setColor(Color.LIGHT_GRAY);
        g2.drawString("Path: " + pathLen + "   Explored: " + nodesExplored, tx, ty);

        ty += 18;
        double ms = searchTimeNs / 1_000_000.0;
        g2.setColor(Color.LIGHT_GRAY);
        g2.drawString(String.format("Search time: %.3f ms", ms), tx, ty);

        ty += 18;
        g2.setColor(new Color(120, 130, 160));
        g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
        g2.drawString("LClick:Goal  RClick:Wall  WASD:Start  [P]Algo [R]Reset", tx, ty);
    }

    // ── Pathfinding helpers ──────────────────────────────────────────────

    private void recalcPath() {
        Node sn = grid.getNode(startRow, startCol);
        Node gn = grid.getNode(goalRow, goalCol);

        if (sn == null || gn == null || !sn.walkable || !gn.walkable) {
            currentPath  = null;
            exploredList = null;
            nodesExplored = 0;
            searchTimeNs  = 0;
            return;
        }

        long t0 = System.nanoTime();
        if (useAStar) {
            currentPath   = new ArrayList<>(aStar.findPath(startRow, startCol, goalRow, goalCol));
            nodesExplored = aStar.getNodesExplored();
            exploredList  = aStar.getExploredNodes();
        } else {
            currentPath   = new ArrayList<>(bfs.findPath(startRow, startCol, goalRow, goalCol));
            nodesExplored = bfs.getNodesExplored();
            exploredList  = bfs.getExploredNodes();
        }
        searchTimeNs = System.nanoTime() - t0;
    }

    private void resetGrid() {
        // Clear all obstacles
        for (int r = 0; r < ROWS; r++)
            for (int c = 0; c < COLS; c++)
                grid.clearObstacle(r, c);

        startRow = 1;  startCol = 1;
        goalRow  = 16; goalCol  = 23;
        currentPath  = null;
        exploredList = null;
        nodesExplored = 0;
        searchTimeNs  = 0;
        pathDirty = true;
    }
}
