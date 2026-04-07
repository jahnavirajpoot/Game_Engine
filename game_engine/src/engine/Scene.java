package engine;

import java.awt.Graphics;

public abstract class Scene {
    public abstract void update();
    public abstract void render(Graphics g);
}