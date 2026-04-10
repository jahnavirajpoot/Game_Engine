import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.LinkedList;
import java.util.Random;

public class SnakeGame extends JPanel implements ActionListener, KeyListener {

    private final int TILE = 20;
    private final int WIDTH = 600;
    private final int HEIGHT = 600;

    private LinkedList<Point> snake = new LinkedList<>();
    private Point food;

    private int dx = 1, dy = 0;

    private Timer timer;
    private Random random = new Random();

    public SnakeGame() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);

        snake.add(new Point(10, 10));
        spawnFood();

        timer = new Timer(100, this);
        timer.start();
    }

    private void spawnFood() {
        food = new Point(random.nextInt(WIDTH / TILE), random.nextInt(HEIGHT / TILE));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Draw snake
        g.setColor(Color.GREEN);
        for (Point p : snake) {
            g.fillRect(p.x * TILE, p.y * TILE, TILE, TILE);
        }

        // Draw food
        g.setColor(Color.RED);
        g.fillRect(food.x * TILE, food.y * TILE, TILE, TILE);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Point head = snake.getFirst();
        Point newHead = new Point(head.x + dx, head.y + dy);

        // Wall collision
        if (newHead.x < 0 || newHead.y < 0 ||
            newHead.x >= WIDTH / TILE || newHead.y >= HEIGHT / TILE) {
            resetGame();
            return;
        }

        // Self collision
        if (snake.contains(newHead)) {
            resetGame();
            return;
        }

        snake.addFirst(newHead);

        if (newHead.equals(food)) {
            spawnFood();
        } else {
            snake.removeLast();
        }

        repaint();
    }

    private void resetGame() {
        snake.clear();
        snake.add(new Point(10, 10));
        dx = 1;
        dy = 0;
        spawnFood();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();

        if (key == KeyEvent.VK_UP && dy == 0) {
            dx = 0; dy = -1;
        } else if (key == KeyEvent.VK_DOWN && dy == 0) {
            dx = 0; dy = 1;
        } else if (key == KeyEvent.VK_LEFT && dx == 0) {
            dx = -1; dy = 0;
        } else if (key == KeyEvent.VK_RIGHT && dx == 0) {
            dx = 1; dy = 0;
        }
    }

    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}

    public static void main(String[] args) {
        JFrame frame = new JFrame("Snake Game");
        SnakeGame game = new SnakeGame();

        frame.add(game);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}