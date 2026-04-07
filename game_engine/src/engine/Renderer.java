package engine;

import java.awt.Canvas;
import java.awt.Graphics;
import java.awt.image.BufferStrategy;

public class Renderer {

    private Canvas canvas;

    public Renderer(Canvas canvas) {
        this.canvas = canvas;
    }

    public void render(SceneManager sceneManager) {

        BufferStrategy bs = canvas.getBufferStrategy();

        // Create buffer if not exists
        if (bs == null) {
            canvas.createBufferStrategy(3);
            return;
        }

        Graphics g = bs.getDrawGraphics();

        // Clear screen
        g.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        // Render current scene
        sceneManager.render(g);

        // Dispose graphics
        g.dispose();

        // Show buffer
        bs.show();
    }
}