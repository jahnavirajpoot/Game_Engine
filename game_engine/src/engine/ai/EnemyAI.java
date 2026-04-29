package engine.ai;

import engine.physics.PhysicsBody;

/**
 * EnemyAI defines the base behavior for an intelligent entity.
 */
public abstract class EnemyAI {
    
    protected PhysicsBody body;
    protected float speed;

    public EnemyAI(PhysicsBody body, float speed) {
        this.body = body;
        this.speed = speed;
    }

    /**
     * Updates the AI logic and sets the velocity/state of the PhysicsBody.
     * @param target The target body to chase or interact with.
     */
    public abstract void update(PhysicsBody target);

    public PhysicsBody getBody() {
        return body;
    }
}
