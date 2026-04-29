package engine.graphics;

/**
 * Camera handles viewport logic for the 2D engine.
 * It tracks an offset (x, y) that can be applied during rendering
 * so that the game world can be larger than the screen window.
 */
public class Camera {
    private float x, y;

    public Camera(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void update(float targetX, float targetY) {
        // Simple lerp or direct assignment to follow target
        // For example, snap to target:
        this.x = targetX;
        this.y = targetY;
    }

    public float getX() { return x; }
    public void setX(float x) { this.x = x; }

    public float getY() { return y; }
    public void setY(float y) { this.y = y; }
}
