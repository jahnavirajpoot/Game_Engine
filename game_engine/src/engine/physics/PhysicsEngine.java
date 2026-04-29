package engine.physics;

import java.util.ArrayList;
import java.util.List;

/**
 * PhysicsEngine — Core physics simulation loop.
 *
 * ── STATUS ──────────────────────────────────────────────────────────
 *  ✅ Body registration (addBody / removeBody)
 *  ✅ Gravity application
 *  ✅ Euler velocity integration (velocity → position)
 *  ✅ Acceleration → velocity integration (applyForce support)
 *  ✅ Collision detection (calls CollisionDetector.detectAll)
 *  ✅ Collision resolution (resolveAll)
 *  ✅ Ground friction (applied when grounded)
 *  ✅ isGrounded state tracking
 *  ✅ Max fall speed clamp (anti-tunneling)
 * ────────────────────────────────────────────────────────────────────────
 *
 * Author: [Your Name] — Physics & Collision Module
 */
public class PhysicsEngine {

    public static final float GRAVITY      = 800f;  // pixels per second²
    public static final float MAX_FALL_SPD = 600f;  // Maximum downward velocity to prevent tunneling
    public static final float FRICTION_SCALE = 0.85f; // Friction damping when grounded (85% retention)

    private List<PhysicsBody>  bodies;
    private CollisionDetector  collisionDetector;

    public PhysicsEngine() {
        this.bodies            = new ArrayList<>();
        this.collisionDetector = new CollisionDetector();
    }

    /** Register a body into the physics world */
    public void addBody(PhysicsBody body) {
        bodies.add(body);
    }

    /** Remove a body from the physics world */
    public void removeBody(PhysicsBody body) {
        bodies.remove(body);
    }

    // ── ✅ DONE: Main update ───────────────────────────────────────────────

    /**
     * Step the physics simulation by deltaTime seconds.
     * Called once per game-loop tick.
     *
     * @param deltaTime Seconds since last frame (typically ~0.0167 at 60 fps)
     */
    public void update(float deltaTime) {

        for (PhysicsBody body : bodies) {
            if (!body.isStatic) {
                body.isGrounded = false;
            }
        }

        // Step 1 ✅ — Apply gravity to all dynamic bodies
        for (PhysicsBody body : bodies) {
            if (body.isStatic || !body.affectedByGravity) continue;
            body.velocity.y += GRAVITY * deltaTime;

            // Clamp downward velocity to prevent tunneling through static geometry
            if (body.velocity.y > MAX_FALL_SPD) {
                body.velocity.y = MAX_FALL_SPD;
            }
        }

        // Step 2 ✅ — Integrate acceleration into velocity, then clear it
        for (PhysicsBody body : bodies) {
            if (body.isStatic) continue;
            body.velocity.addSelf(body.acceleration.scale(deltaTime));
            body.acceleration.zero();
        }

        // Step 3 ✅ — Integrate velocity into position (Euler integration)
        for (PhysicsBody body : bodies) {
            if (body.isStatic) continue;
            body.position.x += body.velocity.x * deltaTime;
            body.position.y += body.velocity.y * deltaTime;
            body.syncBounds();
        }

        // Step 4 ✅ — Detect collisions
        List<CollisionDetector.CollisionEvent> events = collisionDetector.detectAll(bodies);

        // Step 5 ✅ — Resolve collisions
        collisionDetector.resolveAll(events);

        // Step 6 ✅ — Apply friction to grounded bodies
        // Damp horizontal velocity when body is grounded (sliding friction)
        for (PhysicsBody body : bodies) {
            if (body.isStatic || !body.isGrounded) continue;
            body.velocity.x *= FRICTION_SCALE;
        }
    }

    public List<PhysicsBody>  getBodies()           { return bodies; }
    public CollisionDetector  getCollisionDetector() { return collisionDetector; }
}
