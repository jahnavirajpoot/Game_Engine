package demo_games.game3;

import engine.Scene;
import engine.input.InputManager;
import engine.input.KeyboardInput;
import engine.input.MouseInput;
import engine.physics.PhysicsBody;
import engine.physics.PhysicsEngine;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Game3Scene — "Physics Sandbox"
 *
 * Physics playground demonstrating the PhysicsEngine:
 *   • Click anywhere to spawn a falling object
 *   • Objects fall with gravity and collide with platforms
 *   • [G]     — toggle gravity on/off
 *   • [R]     — reset (remove all spawned objects)
 *   • [1-3]   — change spawn shape (small / medium / large)
 *   • Ground and floating platforms for objects to land on
 *
 * Demonstrates: gravity, collision detection, collision resolution,
 * restitution (bounce), and friction.
 */
public class Game3Scene extends Scene {

    // ── Dimensions ───────────────────────────────────────────────────────
    private static final int W = MainGame3.WIDTH;
    private static final int H = MainGame3.HEIGHT;

    // ── Physics ──────────────────────────────────────────────────────────
    private PhysicsEngine physics;

    // ── Dynamic bodies (spawned by clicking) ─────────────────────────────
    private List<SpawnedBody> spawned = new ArrayList<>();
    private int spawnIdCounter = 0;
    private static final int MAX_BODIES = 60;

    // ── Platform bodies (static) ─────────────────────────────────────────
    private List<PhysicsBody> platforms = new ArrayList<>();

    // ── Spawn size preset (0=small, 1=medium, 2=large) ───────────────────
    private int sizePreset = 1;
    private static final float[][] SIZES = {
        {15, 15},   // small
        {28, 28},   // medium
        {45, 45},   // large
    };

    // ── Gravity toggle ───────────────────────────────────────────────────
    private boolean gravityOn = true;

    // ── Input ────────────────────────────────────────────────────────────
    private InputManager input;
    private static final float DT = 1f / 60f;
    private Random rng = new Random();

    // ── Visual colors for spawned objects ─────────────────────────────────
    private static final Color[] PALETTE = {
        new Color(255, 90,  90),   // coral
        new Color(90,  200, 255),  // sky blue
        new Color(120, 230, 120),  // mint
        new Color(255, 200, 60),   // gold
        new Color(200, 120, 255),  // lavender
        new Color(255, 150, 80),   // tangerine
        new Color(80,  255, 200),  // teal
        new Color(255, 100, 180),  // pink
    };

    // ── Wrapper for spawned bodies (body + color) ────────────────────────

    private static class SpawnedBody {
        PhysicsBody body;
        Color color;
        SpawnedBody(PhysicsBody b, Color c) { body = b; color = c; }
    }

    // ── Constructor ──────────────────────────────────────────────────────

    public Game3Scene(Component canvas) {
        input = InputManager.getInstance();
        input.attachTo(canvas);

        physics = new PhysicsEngine();
        buildPlatforms();
    }

    private void buildPlatforms() {
        // Ground
        addPlatform("ground", 0, H - 30, W, 30);

        // Left wall
        addPlatform("wall_l", -20, 0, 20, H);
        // Right wall
        addPlatform("wall_r", W, 0, 20, H);
        // Ceiling
        addPlatform("ceil", 0, -20, W, 20);

        // Floating platforms — staggered layout
        addPlatform("plat_1", 100, 460, 180, 18);
        addPlatform("plat_2", 450, 380, 200, 18);
        addPlatform("plat_3", 200, 280, 160, 18);
        addPlatform("plat_4", 550, 200, 150, 18);
        addPlatform("plat_5",  50, 160, 120, 18);

        // Small step blocks
        addPlatform("step_1", 350, 500, 80, 18);
        addPlatform("step_2", 650, 480, 100, 18);
    }

    private void addPlatform(String id, float x, float y, float w, float h) {
        PhysicsBody p = PhysicsBody.createStatic(id, x, y, w, h);
        platforms.add(p);
        physics.addBody(p);
    }

    // ── UPDATE ───────────────────────────────────────────────────────────

    @Override
    public void update() {
        KeyboardInput kb = input.getKeyboard();
        MouseInput    ms = input.getMouse();

        // Toggle gravity
        if (kb.isJustPressed(KeyEvent.VK_G)) {
            gravityOn = !gravityOn;
            // Update all spawned bodies
            for (SpawnedBody sb : spawned) {
                sb.body.affectedByGravity = gravityOn;
            }
        }

        // Reset spawned objects
        if (kb.isJustPressed(KeyEvent.VK_R)) {
            resetSpawned();
        }

        // Size presets
        if (kb.isJustPressed(KeyEvent.VK_1)) sizePreset = 0;
        if (kb.isJustPressed(KeyEvent.VK_2)) sizePreset = 1;
        if (kb.isJustPressed(KeyEvent.VK_3)) sizePreset = 2;

        // Spawn on left click
        if (ms.isLeftClicked() && spawned.size() < MAX_BODIES) {
            spawnObject(ms.getX(), ms.getY());
        }

        // Burst spawn on right click (5 objects in a cluster)
        if (ms.isRightClicked() && spawned.size() < MAX_BODIES - 5) {
            for (int i = 0; i < 5; i++) {
                float ox = ms.getX() + rng.nextInt(60) - 30;
                float oy = ms.getY() + rng.nextInt(40) - 20;
                if (spawned.size() < MAX_BODIES) spawnObject(ox, oy);
            }
        }

        // Physics step
        physics.update(DT);

        input.endFrame();
    }

    // ── RENDER ───────────────────────────────────────────────────────────

    @Override
    public void render(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);

        // Background — dark gradient feel
        g2.setColor(new Color(12, 14, 22));
        g2.fillRect(0, 0, W, H);

        // Subtle grid dots
        g2.setColor(new Color(30, 34, 48));
        for (int x = 0; x < W; x += 40) {
            for (int y = 0; y < H; y += 40) {
                g2.fillRect(x, y, 1, 1);
            }
        }

        // Platforms
        for (PhysicsBody p : platforms) {
            int px = (int) p.position.x;
            int py = (int) p.position.y;
            int pw = (int) p.bounds.width;
            int ph = (int) p.bounds.height;

            // Only draw visible platforms (skip boundary walls)
            if (px < -10 || py < -10 || px > W || py > H) continue;

            // Platform body
            g2.setColor(new Color(45, 50, 70));
            g2.fillRect(px, py, pw, ph);

            // Top highlight
            g2.setColor(new Color(70, 80, 110));
            g2.fillRect(px, py, pw, 3);

            // Border
            g2.setColor(new Color(55, 62, 85));
            g2.drawRect(px, py, pw, ph);
        }

        // Spawned objects
        for (SpawnedBody sb : spawned) {
            PhysicsBody b = sb.body;
            int bx = (int) b.position.x;
            int by = (int) b.position.y;
            int bw = (int) b.bounds.width;
            int bh = (int) b.bounds.height;

            // Glow
            Color glow = new Color(sb.color.getRed(), sb.color.getGreen(),
                                   sb.color.getBlue(), 30);
            g2.setColor(glow);
            g2.fillOval(bx - 4, by - 4, bw + 8, bh + 8);

            // Body
            g2.setColor(sb.color);
            g2.fillRoundRect(bx, by, bw, bh, 6, 6);

            // Highlight (top-left)
            Color highlight = new Color(
                Math.min(sb.color.getRed() + 60, 255),
                Math.min(sb.color.getGreen() + 60, 255),
                Math.min(sb.color.getBlue() + 60, 255), 120);
            g2.setColor(highlight);
            g2.fillRoundRect(bx + 2, by + 2, bw / 3, bh / 3, 4, 4);

            // Border
            Color border = sb.color.darker();
            g2.setColor(border);
            g2.drawRoundRect(bx, by, bw, bh, 6, 6);

            // Velocity indicator line (faint)
            if (Math.abs(b.velocity.x) > 5 || Math.abs(b.velocity.y) > 5) {
                int cx = bx + bw / 2;
                int cy = by + bh / 2;
                int vx = (int) (b.velocity.x * 0.05f);
                int vy = (int) (b.velocity.y * 0.05f);
                g2.setColor(new Color(255, 255, 255, 40));
                g2.drawLine(cx, cy, cx + vx, cy + vy);
            }
        }

        // HUD
        drawHUD(g2);

        // Spawn preview at cursor
        drawCursorPreview(g2);
    }

    // ── HUD ──────────────────────────────────────────────────────────────

    private void drawHUD(Graphics2D g2) {
        g2.setColor(new Color(0, 0, 0, 180));
        g2.fillRoundRect(8, 8, 310, 130, 12, 12);
        g2.setColor(new Color(55, 62, 90));
        g2.drawRoundRect(8, 8, 310, 130, 12, 12);

        int tx = 18, ty = 28;

        g2.setFont(new Font("SansSerif", Font.BOLD, 14));
        g2.setColor(new Color(200, 210, 255));
        g2.drawString("Physics Sandbox", tx, ty);

        g2.setFont(new Font("SansSerif", Font.PLAIN, 12));

        ty += 20;
        g2.setColor(Color.LIGHT_GRAY);
        g2.drawString("Objects: " + spawned.size() + " / " + MAX_BODIES, tx, ty);

        ty += 18;
        String grav = gravityOn ? "ON" : "OFF";
        Color gc = gravityOn ? new Color(100, 255, 150) : new Color(255, 100, 100);
        g2.setColor(Color.LIGHT_GRAY); g2.drawString("Gravity: ", tx, ty);
        g2.setColor(gc);               g2.drawString(grav, tx + 55, ty);

        ty += 18;
        String[] sizeNames = {"Small", "Medium", "Large"};
        Color sc = PALETTE[sizePreset % PALETTE.length];
        g2.setColor(Color.LIGHT_GRAY); g2.drawString("Size: ", tx, ty);
        g2.setColor(sc);               g2.drawString(sizeNames[sizePreset], tx + 38, ty);
        // Draw size indicator squares
        for (int i = 0; i < 3; i++) {
            int sx = tx + 120 + i * 30;
            int sy = ty - 10;
            g2.setColor(i == sizePreset ? new Color(255, 255, 255, 200) : new Color(80, 80, 100));
            g2.drawRect(sx, sy, 12, 12);
            if (i == sizePreset) g2.fillRect(sx + 2, sy + 2, 9, 9);
        }

        ty += 20;
        g2.setColor(new Color(120, 130, 165));
        g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
        g2.drawString("LClick: Spawn    RClick: Burst Spawn (x5)", tx, ty);

        ty += 14;
        g2.drawString("[G]: Gravity   [R]: Reset   [1-3]: Size", tx, ty);
    }

    private void drawCursorPreview(Graphics2D g2) {
        if (spawned.size() >= MAX_BODIES) return;

        MouseInput ms = input.getMouse();
        int mx = ms.getX(), my = ms.getY();
        float sw = SIZES[sizePreset][0];
        float sh = SIZES[sizePreset][1];

        // Ghost preview
        g2.setColor(new Color(255, 255, 255, 25));
        g2.drawRoundRect((int)(mx - sw / 2), (int)(my - sh / 2),
                         (int) sw, (int) sh, 6, 6);

        // Crosshair
        g2.setColor(new Color(255, 255, 255, 40));
        g2.drawLine(mx - 8, my, mx + 8, my);
        g2.drawLine(mx, my - 8, mx, my + 8);
    }

    // ── Spawn helpers ────────────────────────────────────────────────────

    private void spawnObject(float x, float y) {
        float sw = SIZES[sizePreset][0];
        float sh = SIZES[sizePreset][1];

        // Center on click position
        float bx = x - sw / 2f;
        float by = y - sh / 2f;

        // Mass proportional to size
        float mass = (sw * sh) / 200f;

        String id = "obj_" + (spawnIdCounter++);
        PhysicsBody body = new PhysicsBody(id, bx, by, sw, sh, mass);
        body.affectedByGravity = gravityOn;
        body.restitution = 0.3f + rng.nextFloat() * 0.4f;  // 0.3–0.7 bounciness

        // Give a slight random velocity for visual interest
        body.velocity.x = rng.nextFloat() * 100f - 50f;
        body.velocity.y = rng.nextFloat() * -100f;

        physics.addBody(body);

        Color color = PALETTE[rng.nextInt(PALETTE.length)];
        spawned.add(new SpawnedBody(body, color));
    }

    private void resetSpawned() {
        for (SpawnedBody sb : spawned) {
            physics.removeBody(sb.body);
        }
        spawned.clear();
    }
}
