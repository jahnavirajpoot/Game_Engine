package engine.graphics;

import java.awt.Graphics2D;
import java.util.List;

/**
 * Animation manages a sequence of Sprites to be rendered over time.
 */
public class Animation {
    private List<Sprite> frames;
    private int currentFrameIndex;
    private long lastTime;
    private long frameDelay;

    public Animation(List<Sprite> frames, long frameDelay) {
        this.frames = frames;
        this.frameDelay = frameDelay;
        this.currentFrameIndex = 0;
        this.lastTime = System.currentTimeMillis();
    }

    public void update() {
        if (frames == null || frames.isEmpty()) return;

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastTime > frameDelay) {
            currentFrameIndex = (currentFrameIndex + 1) % frames.size();
            lastTime = currentTime;
        }
    }

    public void render(Graphics2D g, int x, int y) {
        if (frames != null && !frames.isEmpty()) {
            frames.get(currentFrameIndex).render(g, x, y);
        }
    }
}
