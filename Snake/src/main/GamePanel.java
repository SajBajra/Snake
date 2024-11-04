package main;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.sound.sampled.*;
import javax.swing.*;
import java.util.Random;

public class GamePanel extends JPanel implements ActionListener {
    // Constants
    static final int SCREEN_WIDTH = 600;
    static final int SCREEN_HEIGHT = 600;
    static final int UNIT_SIZE = 25;
    static final int GAME_UNITS = (SCREEN_WIDTH * SCREEN_HEIGHT) / UNIT_SIZE;
    static final int DELAY = 75;
    static final int RESTART_DELAY = 1500; // 1.5 seconds

    // Game variables
    final int[] x = new int[GAME_UNITS];
    final int[] y = new int[GAME_UNITS];
    int bodyParts = 6;
    int applesEaten;
    int appleX;
    int appleY;
    int highScore = 0;
    char direction = 'R';
    boolean running = false;
    boolean paused = false;
    Timer timer;
    Timer restartTimer; // Timer for delayed restart
    Random random;

    // Constructor for GamePanel
    GamePanel() {
        random = new Random();
        this.setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
        this.setBackground(Color.black);
        this.setFocusable(true);
        this.addKeyListener(new MyKeyAdapter());
        loadHighScore();
        startGame();
    }

    // Initialize game settings and start the game
    public void startGame() {
        newApple();
        running = true;
        timer = new Timer(DELAY, this);
        timer.start();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    // Drawing the game components
    public void draw(Graphics g) {
        if (running) {
            drawApple(g);
            drawSnake(g);
            drawScore(g);
            drawPauseScreen(g);
        } else {
            gameOver(g);
        }
    }

    // Draw the apple
    private void drawApple(Graphics g) {
        g.setColor(Color.red);
        g.fillOval(appleX, appleY, UNIT_SIZE, UNIT_SIZE);
    }

    // Draw the snake
    private void drawSnake(Graphics g) {
        for (int i = 0; i < bodyParts; i++) {
            if (i == 0) {
                g.setColor(Color.green); // Head of the snake
            } else {
                // Body parts as circular segments
                int gradient = 255 - (i * 10);
                g.setColor(new Color(255, gradient, gradient));
                g.fillRoundRect(x[i], y[i], UNIT_SIZE, UNIT_SIZE, 15, 15);
            }
        }
    }

    // Draw the score
    private void drawScore(Graphics g) {
        g.setColor(Color.red);
        g.setFont(new Font("Ink Free", Font.BOLD, 40));
        FontMetrics metrics = getFontMetrics(g.getFont());
        g.drawString("SCORE: " + applesEaten, (SCREEN_WIDTH - metrics.stringWidth("SCORE: " + applesEaten)) / 2, g.getFont().getSize());
    }

    // Draw the pause screen
    private void drawPauseScreen(Graphics g) {
        if (paused) {
            g.setColor(Color.white);
            g.setFont(new Font("Ink Free", Font.BOLD, 75));
            FontMetrics metrics = getFontMetrics(g.getFont());
            g.drawString("Paused", (SCREEN_WIDTH - metrics.stringWidth("Paused")) / 2, SCREEN_HEIGHT / 2);
        }
    }

    // Generate a new apple at a random position
    public void newApple() {
        appleX = random.nextInt((int) (SCREEN_WIDTH / UNIT_SIZE)) * UNIT_SIZE;
        appleY = random.nextInt((int) (SCREEN_HEIGHT / UNIT_SIZE)) * UNIT_SIZE;
    }

    // Move the snake
    public void move() {
        for (int i = bodyParts; i > 0; i--) {
            x[i] = x[i - 1];
            y[i] = y[i - 1];
        }

        switch (direction) {
            case 'U':
                y[0] = y[0] - UNIT_SIZE;
                break;
            case 'D':
                y[0] = y[0] + UNIT_SIZE;
                break;
            case 'L':
                x[0] = x[0] - UNIT_SIZE;
                break;
            case 'R':
                x[0] = x[0] + UNIT_SIZE;
                break;
        }
    }

    // Check if the snake has eaten the apple
    public void checkApple() {
        if ((x[0] == appleX) && (y[0] == appleY)) {
            bodyParts++;
            applesEaten++;
            newApple();
            playSound("eat.wav");

            // Increase speed every 5 apples eaten
            if (applesEaten % 5 == 0 && timer.getDelay() > 25) {
                timer.setDelay(timer.getDelay() - 5);
            }
        }
    }

    // Check for collisions with the body or borders
    public void checkCollisions() {
        // Check if head collides with body
        for (int i = bodyParts; i > 0; i--) {
            if ((x[0] == x[i]) && (y[0] == y[i])) {
                running = false;
                break;
            }
        }

        // Check if head collides with borders
        if (x[0] < 0 || x[0] >= SCREEN_WIDTH || y[0] < 0 || y[0] >= SCREEN_HEIGHT) {
            running = false;
        }

        if (!running) {
            timer.stop();
            showGameOver();
        }
    }

    // Display the game over screen and handle high scores
    public void gameOver(Graphics g) {
        g.setColor(Color.red);
        g.setFont(new Font("Ink Free", Font.BOLD, 75));
        FontMetrics metrics1 = getFontMetrics(g.getFont());
        g.drawString("Game Over", (SCREEN_WIDTH - metrics1.stringWidth("Game Over")) / 2, SCREEN_HEIGHT / 2);

        g.setFont(new Font("Ink Free", Font.BOLD, 40));
        FontMetrics metrics2 = getFontMetrics(g.getFont());
        g.drawString("Score: " + applesEaten, (SCREEN_WIDTH - metrics2.stringWidth("Score: " + applesEaten)) / 2, g.getFont().getSize() * 2);
        g.drawString("High Score: " + highScore, (SCREEN_WIDTH - metrics2.stringWidth("High Score: " + highScore)) / 2, g.getFont().getSize() * 3);
    }

    // Show the game over screen and handle high score
    public void showGameOver() {
        playSound("gameover.wav");

        restartTimer = new Timer(RESTART_DELAY, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                restartGame();
                restartTimer.stop();
            }
        });
        restartTimer.setRepeats(false); // Only fire once
        restartTimer.start();
    }

    // Restart the game
    public void restartGame() {
        loadHighScore();
        applesEaten = 0;
        bodyParts = 6;
        direction = 'R';
        x[0] = 0;
        y[0] = 0;
        startGame();
    }

    // Load the high score from a file
    public void loadHighScore() {
        File file = new File("highscore.txt");
        if (!file.exists()) {
            highScore = 0;
            return;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine();
            if (line != null) {
                highScore = Integer.parseInt(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Save the high score to a file
    public void saveHighScore() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("highscore.txt"))) {
            writer.write(String.valueOf(highScore));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Play sound effect
    public void playSound(String soundFile) {
        try {
            File soundPath = new File(soundFile);
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(soundPath);
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
            clip.start();
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (running) {
            move();
            checkApple();
            checkCollisions();
        }
        repaint();
    }

    public class MyKeyAdapter extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_LEFT:
                    if (direction != 'R') {
                        direction = 'L';
                    }
                    break;
                case KeyEvent.VK_RIGHT:
                    if (direction != 'L') {
                        direction = 'R';
                    }
                    break;
                case KeyEvent.VK_UP:
                    if (direction != 'D') {
                        direction = 'U';
                    }
                    break;
                case KeyEvent.VK_DOWN:
                    if (direction != 'U') {
                        direction = 'D';
                    }
                    break;
                case KeyEvent.VK_P: // Pause and resume functionality
                    paused = !paused;
                    pauseGame();
                    break;
            }
        }
    }

    // Pause the game
    public void pauseGame() {
        if (paused) {
            timer.stop();
        } else {
            timer.start();
        }
    }
}
