package engine.ui;

import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Font;

/**
 * PauseMenu is an overlay UI that appears when the game is paused.
 */
public class PauseMenu {
    
    private boolean isPaused = false;

    public void togglePause() {
        isPaused = !isPaused;
    }

    public boolean isPaused() {
        return isPaused;
    }

    public void update() {
        if (!isPaused) return;
        // Logic for pause menu (e.g., resuming, quitting)
    }

    public void render(Graphics2D g, int screenWidth, int screenHeight) {
        if (!isPaused) return;

        // Semi-transparent background
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRect(0, 0, screenWidth, screenHeight);

        // Pause text
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 48));
        g.drawString("PAUSED", screenWidth / 2 - 100, screenHeight / 2);
    }
}
