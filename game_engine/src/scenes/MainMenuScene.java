package scenes;

import engine.Scene;
import engine.SceneManager;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Font;

/**
 * MainMenuScene is the first screen shown when the game starts.
 */
public class MainMenuScene extends Scene {

    public MainMenuScene(SceneManager sceneManager) {
        super(sceneManager);
    }

    @Override
    public void init() {
        // Initialize menu buttons, background, etc.
    }

    @Override
    public void update(double dt) {
        // Handle input for navigating menu
    }

    @Override
    public void render(Graphics g) {
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, 800, 600); // Assuming 800x600 for now

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 36));
        g.drawString("MAIN MENU", 300, 200);

        g.setFont(new Font("Arial", Font.PLAIN, 24));
        g.drawString("Press ENTER to Start", 280, 300);
    }

    @Override
    public void dispose() {
        // Clean up resources when leaving menu
    }
}
