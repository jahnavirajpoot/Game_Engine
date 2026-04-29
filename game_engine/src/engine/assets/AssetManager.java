package engine.assets;

import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * AssetManager is a singleton that loads and caches game assets 
 * like images and sounds to prevent loading the same file multiple times.
 */
public class AssetManager {
    
    private static AssetManager instance;
    private Map<String, BufferedImage> images;

    private AssetManager() {
        images = new HashMap<>();
    }

    public static AssetManager getInstance() {
        if (instance == null) {
            instance = new AssetManager();
        }
        return instance;
    }

    public BufferedImage loadImage(String path) {
        if (images.containsKey(path)) {
            return images.get(path);
        }

        try {
            URL url = getClass().getResource(path);
            if (url == null) {
                System.err.println("Asset not found: " + path);
                return null;
            }
            BufferedImage image = ImageIO.read(url);
            images.put(path, image);
            return image;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Add methods for sound loading here
}
