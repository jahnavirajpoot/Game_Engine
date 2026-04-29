package engine;

import java.awt.Graphics;
import java.util.HashMap;

import scenes.PlayScene;

public class SceneManager {

    private HashMap<String, Scene> scenes;
    private Scene currentScene;

    // Constructor
    public SceneManager() {
        scenes = new HashMap<>();

        // Add scenes here
        scenes.put("play", new PlayScene());

        // Set default scene
        currentScene = scenes.get("play");
    }

    // Change scene
    public void setScene(String name) {
        if (scenes.containsKey(name)) {
            currentScene = scenes.get(name);
        } else {
            System.out.println("Scene not found: " + name);
        }
    }

    public void setScene(Scene scene) {
        currentScene = scene;
    }

    // Update current scene
    public void update() {
        if (currentScene != null) {
            currentScene.update();
        }
    }

    // Render current scene
    public void render(Graphics g) {
        if (currentScene != null) {
            currentScene.render(g);
        }
    }
}