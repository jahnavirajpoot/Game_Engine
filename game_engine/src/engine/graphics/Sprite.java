package engine.graphics;

import java.awt.image.BufferedImage;
import java.awt.Graphics2D;

/**
 * Sprite is a wrapper around BufferedImage to provide 
 * easier rendering and manipulation of 2D images.
 */
public class Sprite {
    private BufferedImage image;

    public Sprite(BufferedImage image) {
        this.image = image;
    }

    public void render(Graphics2D g, int x, int y) {
        if (image != null) {
            g.drawImage(image, x, y, null);
        }
    }

    public int getWidth() {
        return image != null ? image.getWidth() : 0;
    }

    public int getHeight() {
        return image != null ? image.getHeight() : 0;
    }

    public BufferedImage getImage() {
        return image;
    }
}
