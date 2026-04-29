package engine;

import java.awt.Graphics;

public abstract class Scene {

    protected SceneManager sceneManager;

    public Scene() {
        this.sceneManager = null;
    }

    public Scene(SceneManager sceneManager) {
        this.sceneManager = sceneManager;
    }

    public void init() {}

    public void update() {
        update(1.0 / 60.0);
    }

    public void update(double dt) {}

    public abstract void render(Graphics g);

    public void dispose() {}
}