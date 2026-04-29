package engine.physics;

public class AABB {

    public float x, y;        // top-left corner position
    public float width, height;

    public AABB(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    /** Returns the right edge (x + width) */
    public float getRight() {
        return x + width;
    }

    /** Returns the bottom edge (y + height) */
    public float getBottom() {
        return y + height;
    }

    /** Returns center X of the bounding box */
    public float getCenterX() {
        return x + width / 2f;
    }

    /** Returns center Y of the bounding box */
    public float getCenterY() {
        return y + height / 2f;
    }

    public boolean overlaps(AABB other) {
        // No overlap if separated on X or Y axis
        if (this.getRight() <= other.x) return false;
        if (other.getRight() <= this.x) return false;
        if (this.getBottom() <= other.y) return false;
        if (other.getBottom() <= this.y) return false;
        return true;
    }


    public Vector2D getResolutionVector(AABB other) {
        float overlapX = Math.min(this.getRight(), other.getRight())
                       - Math.max(this.x, other.x);
        float overlapY = Math.min(this.getBottom(), other.getBottom())
                       - Math.max(this.y, other.y);

        // Push along the axis with least overlap
        if (overlapX < overlapY) {
            float dirX = (this.getCenterX() < other.getCenterX()) ? -1 : 1;
            return new Vector2D(overlapX * dirX, 0);
        } else {
            float dirY = (this.getCenterY() < other.getCenterY()) ? -1 : 1;
            return new Vector2D(0, overlapY * dirY);
        }
    }

    public void updatePosition(float newX, float newY) {
        this.x = newX;
        this.y = newY;
    }

    @Override
    public String toString() {
        return String.format("AABB[x=%.1f, y=%.1f, w=%.1f, h=%.1f]", x, y, width, height);
    }
}
