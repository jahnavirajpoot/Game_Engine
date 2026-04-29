package engine.physics;

/**
 * PhysicsBody - Represents a physical game entity.
 * Holds position, velocity, acceleration, and mass.
 * Attached to game entities that need physics simulation.
 *
 * Supports: gravity, friction, static/dynamic bodies.
 * Author: [Your Name] - Physics & Collision Module
 */
public class PhysicsBody {

    // Identity
    private String id;

    // Kinematic properties
    public Vector2D position;
    public Vector2D velocity;
    public Vector2D acceleration;

    // Physics properties
    public float mass;
    public float restitution;    // bounciness: 0 = no bounce, 1 = full bounce
    public float friction;       // horizontal friction coefficient

    // Flags
    public boolean isStatic;     // static bodies don't move (walls, platforms)
    public boolean isGrounded;   // true when resting on a surface
    public boolean affectedByGravity;

    // Bounding box for collision
    public AABB bounds;

    /**
     * Create a dynamic physics body.
     * @param id      Unique identifier (e.g. "player", "enemy_1")
     * @param x       Initial X position
     * @param y       Initial Y position
     * @param width   Width of bounding box
     * @param height  Height of bounding box
     * @param mass    Mass (affects force response)
     */
    public PhysicsBody(String id, float x, float y, float width, float height, float mass) {
        this.id = id;
        this.position = new Vector2D(x, y);
        this.velocity = new Vector2D(0, 0);
        this.acceleration = new Vector2D(0, 0);
        this.mass = mass;
        this.restitution = 0.1f;
        this.friction = 0.85f;
        this.isStatic = false;
        this.isGrounded = false;
        this.affectedByGravity = true;
        this.bounds = new AABB(x, y, width, height);
    }

    /** Create a static body (e.g. platform or wall) - no physics applied */
    public static PhysicsBody createStatic(String id, float x, float y,
                                            float width, float height) {
        PhysicsBody body = new PhysicsBody(id, x, y, width, height, Float.MAX_VALUE);
        body.isStatic = true;
        body.affectedByGravity = false;
        return body;
    }

    /** Apply a force to this body (F = ma → a += F/m) */
    public void applyForce(Vector2D force) {
        if (isStatic) return;
        acceleration.x += force.x / mass;
        acceleration.y += force.y / mass;
    }

    /** Apply an instant velocity impulse (e.g. jump) */
    public void applyImpulse(float vx, float vy) {
        if (isStatic) return;
        velocity.x = vx;
        velocity.y = vy;
    }

    /** Sync AABB position with current body position */
    public void syncBounds() {
        bounds.updatePosition(position.x, position.y);
    }

    public String getId() { return id; }

    @Override
    public String toString() {
        return String.format("PhysicsBody[%s pos=%s vel=%s grounded=%b]",
                id, position, velocity, isGrounded);
    }
}
