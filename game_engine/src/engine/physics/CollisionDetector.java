package engine.physics;

import java.util.ArrayList;
import java.util.List;

/**
 * CollisionDetector - Detects AABB collisions between physics bodies.
 *
 * ── PHASE 1 STATUS ──────────────────────────────────────────────────────────
 *  ✅ CollisionEvent data structure
 *  ✅ detectAll() — broad-phase overlap detection
 *  ✅ determineSide() — which face was hit
 *  ⬜ resolveAll()  — collision response / MTV push-out  [PHASE 2]
 *  ⬜ applyVelocityResponse() — bounce / restitution     [PHASE 2]
 * ────────────────────────────────────────────────────────────────────────────
 *
 * Author: [Your Name] — Physics & Collision Module
 */
public class CollisionDetector {

    /**
     * CollisionEvent — fired when two bodies overlap.
     * Stores both bodies, the MTV, and the collision side.
     */
    public static class CollisionEvent {
        public PhysicsBody bodyA;
        public PhysicsBody bodyB;
        public Vector2D    mtv;    // Minimum Translation Vector (how far to push out)
        public String      side;   // "TOP" | "BOTTOM" | "LEFT" | "RIGHT"

        public CollisionEvent(PhysicsBody a, PhysicsBody b, Vector2D mtv, String side) {
            this.bodyA = a;
            this.bodyB = b;
            this.mtv   = mtv;
            this.side  = side;
        }

        @Override
        public String toString() {
            return String.format("Collision[%s <-> %s  side=%s  mtv=%s]",
                    bodyA.getId(), bodyB.getId(), side, mtv);
        }
    }

    private List<CollisionEvent> lastCollisions = new ArrayList<>();

    // ── ✅ DONE: Broad-phase detection ────────────────────────────────────

    /**
     * Check every pair of bodies for AABB overlap.
     * O(n²) — suitable for small scenes in Phase 1.
     *
     * @param bodies All physics bodies in the scene
     * @return       List of detected CollisionEvents (may be empty)
     */
    public List<CollisionEvent> detectAll(List<PhysicsBody> bodies) {
        lastCollisions.clear();

        for (int i = 0; i < bodies.size(); i++) {
            for (int j = i + 1; j < bodies.size(); j++) {
                PhysicsBody a = bodies.get(i);
                PhysicsBody b = bodies.get(j);

                // Two static bodies never move — no need to check
                if (a.isStatic && b.isStatic) continue;

                if (a.bounds.overlaps(b.bounds)) {
                    Vector2D mtv  = a.bounds.getResolutionVector(b.bounds);
                    String   side = determineSide(mtv);
                    CollisionEvent event = new CollisionEvent(a, b, mtv, side);
                    lastCollisions.add(event);

                    // Debug log — useful for Phase 1 demo
                    System.out.println("[Collision Detected] " + event);
                }
            }
        }
        return lastCollisions;
    }

    /** Determine which face of bodyA the collision is occurring on */
    private String determineSide(Vector2D mtv) {
        if (Math.abs(mtv.x) > Math.abs(mtv.y)) {
            return (mtv.x > 0) ? "RIGHT" : "LEFT";
        } else {
            return (mtv.y > 0) ? "BOTTOM" : "TOP";
        }
    }

    // ── ⬜ TODO PHASE 2: Collision Resolution ─────────────────────────────

    /**
     * TODO (Phase 2): Resolve detected collisions using MTV push-out.
     *
     * Plan:
     *  - If bodyA is dynamic and bodyB is static → push A out by full MTV
     *  - If both dynamic → split MTV equally between both
     *  - Call applyVelocityResponse() to zero/bounce velocity on collision axis
     *  - Set body.isGrounded = true when collision side is TOP
     *
     * @param events Collision events from detectAll()
     */
    public void resolveAll(List<CollisionEvent> events) {
        for (CollisionEvent event : events) {
            PhysicsBody a = event.bodyA;
            PhysicsBody b = event.bodyB;
            Vector2D mtv = event.mtv;

            if (a.isStatic && b.isStatic) {
                continue;
            }

            if (!a.isStatic && b.isStatic) {
                moveBody(a, mtv);
                applyVelocityResponse(a, mtv);
                if (mtv.y < 0) {
                    a.isGrounded = true;
                }
                continue;
            }

            if (a.isStatic) {
                Vector2D push = new Vector2D(-mtv.x, -mtv.y);
                moveBody(b, push);
                applyVelocityResponse(b, push);
                if (push.y < 0) {
                    b.isGrounded = true;
                }
                continue;
            }

            Vector2D half = new Vector2D(mtv.x * 0.5f, mtv.y * 0.5f);
            Vector2D inverseHalf = new Vector2D(-half.x, -half.y);

            moveBody(a, half);
            moveBody(b, inverseHalf);

            applyVelocityResponse(a, half);
            applyVelocityResponse(b, inverseHalf);

            if (half.y < 0) {
                a.isGrounded = true;
            }
            if (inverseHalf.y < 0) {
                b.isGrounded = true;
            }
        }
    }

    /**
     * TODO (Phase 2): Zero out velocity on the collision axis.
     * Apply restitution (bounce) factor from PhysicsBody.restitution.
     */
    private void applyVelocityResponse(PhysicsBody body, Vector2D mtv) {
        if (Math.abs(mtv.x) > Math.abs(mtv.y)) {
            body.velocity.x = -body.velocity.x * body.restitution;
            if (Math.abs(body.velocity.x) < 1.0f) {
                body.velocity.x = 0;
            }
            return;
        }

        if (mtv.y < 0 && body.velocity.y > 0) {
            body.velocity.y = 0;
            return;
        }

        body.velocity.y = -body.velocity.y * body.restitution;
        if (Math.abs(body.velocity.y) < 1.0f) {
            body.velocity.y = 0;
        }
    }

    private void moveBody(PhysicsBody body, Vector2D delta) {
        if (body.isStatic) {
            return;
        }
        body.position.x += delta.x;
        body.position.y += delta.y;
        body.syncBounds();
    }

    public List<CollisionEvent> getLastCollisions() { return lastCollisions; }
}
