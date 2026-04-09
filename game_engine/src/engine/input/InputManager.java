package engine.input;

import java.awt.Component;

public class InputManager {

    private static InputManager instance;

    private final KeyboardInput keyboard;
    private final MouseInput mouse;

    private InputManager() {
        keyboard = new KeyboardInput();
        mouse    = new MouseInput();
    }

    public static InputManager getInstance() {
        if (instance == null) {
            instance = new InputManager();
        }
        return instance;
    }


    public void attachTo(Component component) {
        component.addKeyListener(keyboard);
        component.addMouseListener(mouse);
        component.addMouseMotionListener(mouse);
        component.addMouseWheelListener(mouse);
        component.setFocusable(true);
        component.requestFocusInWindow();
        System.out.println("[InputManager] Attached to: " + component.getClass().getSimpleName());
    }

    public void endFrame() {
        keyboard.endFrame();
        mouse.endFrame();
    }

    public KeyboardInput getKeyboard() { return keyboard; }
    public MouseInput getMouse()       { return mouse; }
}
