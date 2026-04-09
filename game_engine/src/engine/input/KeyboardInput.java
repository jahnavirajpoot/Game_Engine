package engine.input;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashSet;
import java.util.Set;

public class KeyboardInput implements KeyListener {

    private final Set<Integer> held         = new HashSet<>();
    private final Set<Integer> justPressed  = new HashSet<>();
    private final Set<Integer> justReleased = new HashSet<>();

    // ── KeyListener callbacks ──────────────────────────────────────────────

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        if (!held.contains(code)) {
            justPressed.add(code);   // only add once per press
        }
        held.add(code);
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();
        held.remove(code);
        justReleased.add(code);
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // Not needed for game input — we use key codes
    }

    // ── Query methods ──────────────────────────────────────────────────────

    /** Is the key currently held down? (true for every frame it's held) */
    public boolean isHeld(int keyCode) {
        return held.contains(keyCode);
    }

    /** Was the key pressed this frame? (true only for the first frame) */
    public boolean isJustPressed(int keyCode) {
        return justPressed.contains(keyCode);
    }

    /** Was the key released this frame? */
    public boolean isJustReleased(int keyCode) {
        return justReleased.contains(keyCode);
    }

    // Convenience WASD / Arrow key helpers
    public boolean isLeft()  { return isHeld(KeyEvent.VK_LEFT)  || isHeld(KeyEvent.VK_A); }
    public boolean isRight() { return isHeld(KeyEvent.VK_RIGHT) || isHeld(KeyEvent.VK_D); }
    public boolean isUp()    { return isHeld(KeyEvent.VK_UP)    || isHeld(KeyEvent.VK_W); }
    public boolean isDown()  { return isHeld(KeyEvent.VK_DOWN)  || isHeld(KeyEvent.VK_S); }
    public boolean isJump()  { return isJustPressed(KeyEvent.VK_SPACE); }


    public void endFrame() {
        justPressed.clear();
        justReleased.clear();
    }

    public Set<Integer> getHeldKeys() {
        return new HashSet<>(held);
    }
}
