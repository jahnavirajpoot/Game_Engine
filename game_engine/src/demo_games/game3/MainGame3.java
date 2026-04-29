package demo_games.game3;

import engine.Scene;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferStrategy;

/**
 * MainGame3 — Standalone launcher for Game 3: "Physics Sandbox"
 *
 * Physics playground with gravity, multiple falling/colliding objects,
 * platforms, and interactive spawning.
 *
 * Run this class directly — it has its own main() method.
 */
public class MainGame3 extends Canvas implements Runnable {

    public static final int WIDTH  = 800;
    public static final int HEIGHT = 600;
    public static final String TITLE = "Game 3 \u2014 Physics Sandbox";

    private boolean running = false;
    private Thread  thread;
    private Scene   scene;

    public MainGame3() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));

        JFrame frame = new JFrame(TITLE);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.add(this);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        scene = new Game3Scene(this);
    }

    public synchronized void start() {
        if (running) return;
        running = true;
        thread = new Thread(this, "Game3-Loop");
        thread.start();
    }

    public synchronized void stop() {
        if (!running) return;
        running = false;
        try { thread.join(); } catch (InterruptedException e) { e.printStackTrace(); }
    }

    @Override
    public void run() {
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

    public static void main(String[] args) {
        MainGame3 game = new MainGame3();
        game.start();
    }
}
