package engine.input;

import java.awt.event.*;

public class MouseInput implements MouseListener, MouseMotionListener, MouseWheelListener {

    // Current mouse position in screen/panel coordinates
    private int mouseX, mouseY;
    private int prevX, prevY;

    // Button states: index 0=unused, 1=left, 2=middle, 3=right
    private final boolean[] pressed  = new boolean[4];  // currently held
    private final boolean[] clicked  = new boolean[4];  // pressed this frame
    private final boolean[] released = new boolean[4];  // released this frame

    // Drag tracking
    private boolean dragging    = false;
    private int dragStartX, dragStartY;

    // Scroll wheel
    private int scrollDelta = 0;

    // ── MouseListener callbacks ────────────────────────────────────────────

    @Override
    public void mousePressed(MouseEvent e) {
        int btn = e.getButton();
        if (btn < pressed.length) {
            pressed[btn]  = true;
            clicked[btn]  = true;
        }
        dragStartX = e.getX();
        dragStartY = e.getY();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        int btn = e.getButton();
        if (btn < pressed.length) {
            pressed[btn]   = false;
            released[btn]  = true;
        }
        dragging = false;
    }

    @Override public void mouseClicked(MouseEvent e)  { /* handled via pressed/released */ }
    @Override public void mouseEntered(MouseEvent e)  {}
    @Override public void mouseExited(MouseEvent e)   {}

    // ── MouseMotionListener callbacks ──────────────────────────────────────

    @Override
    public void mouseMoved(MouseEvent e) {
        prevX = mouseX; prevY = mouseY;
        mouseX = e.getX(); mouseY = e.getY();
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        prevX = mouseX; prevY = mouseY;
        mouseX = e.getX(); mouseY = e.getY();
        dragging = true;
    }

    // ── MouseWheelListener callback ────────────────────────────────────────

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        scrollDelta += e.getWheelRotation();
    }

    // ── Query methods ──────────────────────────────────────────────────────

    public int getX()     { return mouseX; }
    public int getY()     { return mouseY; }
    public int getDeltaX() { return mouseX - prevX; }
    public int getDeltaY() { return mouseY - prevY; }

    public boolean isLeftHeld()     { return pressed[MouseEvent.BUTTON1]; }
    public boolean isRightHeld()    { return pressed[MouseEvent.BUTTON3]; }
    public boolean isMiddleHeld()   { return pressed[MouseEvent.BUTTON2]; }

    public boolean isLeftClicked()  { return clicked[MouseEvent.BUTTON1]; }
    public boolean isRightClicked() { return clicked[MouseEvent.BUTTON3]; }

    public boolean isDragging()     { return dragging; }
    public int getDragStartX()      { return dragStartX; }
    public int getDragStartY()      { return dragStartY; }

    public int getScrollDelta()     { return scrollDelta; }

    /** Call at end of every frame to clear per-frame click/release state */
    public void endFrame() {
        java.util.Arrays.fill(clicked, false);
        java.util.Arrays.fill(released, false);
        scrollDelta = 0;
        prevX = mouseX;
        prevY = mouseY;
    }

    @Override
    public String toString() {
        return String.format("Mouse[x=%d, y=%d, left=%b, right=%b, dragging=%b]",
                mouseX, mouseY, isLeftHeld(), isRightHeld(), dragging);
    }
}
