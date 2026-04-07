package scenes;

import engine.Scene;
import java.awt.Graphics;
import java.awt.Color;

public class PlayScene extends Scene {

    private int x = 100;
    private int y = 100;

    private int width = 50;
    private int height = 50;

    private int dx = 3; // speed in x direction

    private int screenWidth = 800;
    private int screenHeight = 600;

    @Override
    public void update() {
        // Move box
        x += dx;

        // Bounce when hitting left or right wall
        if (x <= 0 || x + width >= screenWidth) {
            dx = -dx;
        }
    }

    @Override
    public void render(Graphics g) {

        // Background
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, screenWidth, screenHeight);

        // Red box
        g.setColor(Color.RED);
        g.fillRect(x, y, width, height);
    }
}