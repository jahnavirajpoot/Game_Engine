package scenes;

import engine.Scene;
import engine.SceneManager;
import engine.input.InputManager;
import engine.input.KeyboardInput;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.KeyEvent;

/**
 * GameOverScene is displayed when the player loses or finishes the game.
 */
public class GameOverScene extends Scene {

    private int finalScore;

    public GameOverScene(SceneManager sceneManager, int finalScore) {
        super(sceneManager);
        this.finalScore = finalScore;
    }

    @Override
    public void init() {
        // Initialize resources if needed
    }

    @Override
    public void update(double dt) {
        KeyboardInput kb = InputManager.getInstance().getKeyboard();
        
        // Return to Main Menu on ENTER
        if (kb.isJustPressed(KeyEvent.VK_ENTER)) {
            sceneManager.setScene(new MainMenuScene(sceneManager));
        }
    }

    @Override
    public void render(Graphics g) {
        g.setColor(new Color(30, 0, 0));
        g.fillRect(0, 0, 800, 600); // Assuming 800x600 window size

        g.setColor(Color.RED);
        g.setFont(new Font("Arial", Font.BOLD, 48));
        g.drawString("GAME OVER", 250, 250);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.PLAIN, 24));
        g.drawString("Final Score: " + finalScore, 320, 320);
        
        g.setColor(Color.LIGHT_GRAY);
        g.setFont(new Font("Arial", Font.PLAIN, 18));
        g.drawString("Press ENTER to return to Main Menu", 240, 400);
    }

    @Override
    public void dispose() {
        // Clean up resources
    }
}
