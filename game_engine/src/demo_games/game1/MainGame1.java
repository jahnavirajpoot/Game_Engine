package demo_games.game1;

import engine.Scene;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferStrategy;

/**
 * MainGame1 — Standalone launcher for Game 1: "Dungeon Chase"
 *
 * Full demo featuring player movement, enemy AI with pathfinding,
 * physics-based collision, and BFS/A* toggle.
 *
 * Run this class directly — it has its own main() method.
 */
public class MainGame1 extends Canvas implements Runnable {

    public static final int WIDTH  = 800;
    public static final int HEIGHT = 600;
    public static final String TITLE = "Game 1 \u2014 Dungeon Chase";

    private boolean running = false;
    private Thread  thread;
    private Scene   scene;

    public MainGame1() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));

        JFrame frame = new JFrame(TITLE);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.add(this);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        // Create the game scene, passing this Canvas for input attachment
        scene = new Game1Scene(this);
    }

    public synchronized void start() {
        if (running) return;
        running = true;
        thread = new Thread(this, "Game1-Loop");
        thread.start();
    }

    public synchronized void stop() {
        if (!running) return;
        running = false;
        try { thread.join(); } catch (InterruptedException e) { e.printStackTrace(); }
    }

    @Override
    public void run() {
        // Fixed-timestep loop targeting 60 FPS
        final double nsPerFrame = 1_000_000_000.0 / 60.0;
        long lastTime = System.nanoTime();

        while (running) {
            long now = System.nanoTime();
            if (now - lastTime >= nsPerFrame) {
                scene.update();
                renderFrame();
                lastTime = now;
            } else {
                Thread.yield();
            }
        }
    }

    private void renderFrame() {
        BufferStrategy bs = getBufferStrategy();
        if (bs == null) {
            createBufferStrategy(3);
            return;
        }
        Graphics g = bs.getDrawGraphics();
        g.clearRect(0, 0, WIDTH, HEIGHT);
        scene.render(g);
        g.dispose();
        bs.show();
    }

    // ── Entry Point ──────────────────────────────────────────────────────
    public static void main(String[] args) {
        MainGame1 game = new MainGame1();
        game.start();
    }
}
