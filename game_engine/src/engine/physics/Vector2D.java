package engine.physics;

/**
 * Vector2D - 2D vector math utility for physics calculations.
 * Supports addition, subtraction, scaling, dot product, and normalization.
 *
 * Used by: PhysicsBody, PhysicsEngine, CollisionDetector
 * Author: [Your Name] - Physics & Collision Module
 */
public class Vector2D {

    public float x, y;

    public Vector2D(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public Vector2D() {
        this(0, 0);
    }

    /** Add another vector and return a new result */
    public Vector2D add(Vector2D other) {
        return new Vector2D(this.x + other.x, this.y + other.y);
    }

    /** Subtract another vector */
    public Vector2D subtract(Vector2D other) {
        return new Vector2D(this.x - other.x, this.y - other.y);
    }

    /** Scale by a scalar value */
    public Vector2D scale(float scalar) {
        return new Vector2D(this.x * scalar, this.y * scalar);
    }

    /** Dot product of two vectors */
    public float dot(Vector2D other) {
        return this.x * other.x + this.y * other.y;
    }

    /** Magnitude (length) of the vector */
    public float magnitude() {
        return (float) Math.sqrt(x * x + y * y);
    }

    /** Normalize to unit vector (length = 1) */
    public Vector2D normalize() {
        float mag = magnitude();
        if (mag == 0) return new Vector2D(0, 0);
        return new Vector2D(x / mag, y / mag);
    }

    /** Add to self in-place */
    public void addSelf(Vector2D other) {
        this.x += other.x;
        this.y += other.y;
    }

    /** Reset to zero */
    public void zero() {
        this.x = 0;
        this.y = 0;
    }

    @Override
    public String toString() {
        return String.format("Vector2D(%.2f, %.2f)", x, y);
    }
}
