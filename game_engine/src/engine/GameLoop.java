package engine;

import java.awt.Canvas;

public class GameLoop implements Runnable {

    private Thread thread;
    private boolean running = false;

    private SceneManager sceneManager;
    private Renderer renderer;

    // Constructor
    public GameLoop(Canvas canvas, SceneManager sceneManager) {
        this.sceneManager = sceneManager;
        this.renderer = new Renderer(canvas);
    }

    // Start game loop
    public void start() {
        if (running) return;

        running = true;
        thread = new Thread(this);
        thread.start();
    }

    // Stop game loop (optional)
    public void stop() {
        if (!running) return;

        running = false;
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // Main loop
    @Override
public void run() {

    final int FPS = 60;
    final double timePerFrame = 1000000000.0 / FPS;

    long lastTime = System.nanoTime();

    while (running) {
        long now = System.nanoTime();

        if (now - lastTime >= timePerFrame) {

            sceneManager.update();
            renderer.render(sceneManager);

            lastTime = now;
        }
    }
}
}