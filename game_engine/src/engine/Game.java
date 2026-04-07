package engine;

import javax.swing.JFrame;
import java.awt.Canvas;

public class Game extends Canvas {

    private GameLoop loop;
    private SceneManager sceneManager;

    public Game() {
        JFrame frame = new JFrame("2D Game Engine");

        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        frame.add(this);
        frame.setVisible(true);

        sceneManager = new SceneManager();
        sceneManager.setScene("play");

        loop = new GameLoop(this, sceneManager);
    }

    public void start() {
        loop.start();
    }
}