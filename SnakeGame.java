package project;
//SnakeGame.java

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JComboBox;
import javax.swing.Timer;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class SnakeGame extends JFrame {
 public SnakeGame() {
     GameSettings settings = new GameSettings();
     if (settings.showDialog()) {
         this.add(new SnakePanel(settings));
         this.setTitle("Snake Game - Enhanced");
         this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         this.setResizable(false);
         this.pack();
         this.setVisible(true);
         this.setLocationRelativeTo(null);
     } else {
         System.exit(0);
     }
 }

 public static void main(String[] args) {
     new SnakeGame();
 }
}

class GameSettings {
 String difficulty = "Medium";
 String theme = "Classic";
 String skin = "Green";

 public boolean showDialog() {
     String[] difficulties = {"Easy", "Medium", "Hard"};
     String[] themes = {"Classic", "Dark", "Neon", "Light"};
     String[] skins = {"Green", "Cyan", "Magenta", "Rainbow"};

     JComboBox<String> diffBox = new JComboBox<>(difficulties);
     JComboBox<String> themeBox = new JComboBox<>(themes);
     JComboBox<String> skinBox = new JComboBox<>(skins);

     JPanel panel = new JPanel(new GridLayout(0, 1));
     panel.add(new JLabel("Select Difficulty:"));
     panel.add(diffBox);
     panel.add(new JLabel("Select Theme:"));
     panel.add(themeBox);
     panel.add(new JLabel("Select Snake Skin:"));
     panel.add(skinBox);

     int result = JOptionPane.showConfirmDialog(null, panel, "Game Settings", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

     if (result == JOptionPane.OK_OPTION) {
         difficulty = (String) diffBox.getSelectedItem();
         theme = (String) themeBox.getSelectedItem();
         skin = (String) skinBox.getSelectedItem();
         return true;
     } else {
         return false;
     }
 }
}

class SnakePanel extends JPanel implements ActionListener {
 static final int SCREEN_WIDTH = 600;
 static final int SCREEN_HEIGHT = 600;
 static final int UNIT_SIZE = 25;
 static final int GAME_UNITS = (SCREEN_WIDTH * SCREEN_HEIGHT) / (UNIT_SIZE * UNIT_SIZE);
 int delay;

 final int[] x = new int[GAME_UNITS];
 final int[] y = new int[GAME_UNITS];
 int bodyParts = 6;
 int applesEaten = 0;
 int coinsCollected = 0;
 int highScore = 0;
 int appleX, appleY;
 int coinX, coinY;
 boolean coinVisible = false;
 char direction = 'R';
 boolean running = false;
 boolean paused = false;
 Timer timer;
 Random random;
 Color snakeColor = Color.GREEN;
 int level = 1;
 Color backgroundColor = Color.BLACK;
 GameSettings settings;
 ArrayList<Point> obstacles = new ArrayList<>();

 SnakePanel(GameSettings settings) {
     this.settings = settings;
     applySettings();
     random = new Random();
     this.setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
     this.setBackground(backgroundColor);
     this.setFocusable(true);
     this.addKeyListener(new MyKeyAdapter());
     highScore = loadHighScore();
     startGame();
 }

 void applySettings() {
     switch (settings.difficulty) {
         case "Easy": delay = 200; break;
         case "Medium": delay = 140; break;
         case "Hard": delay = 80; break;
     }
     switch (settings.skin) {
         case "Green": snakeColor = Color.GREEN; break;
         case "Cyan": snakeColor = Color.CYAN; break;
         case "Magenta": snakeColor = Color.MAGENTA; break;
         case "Rainbow": snakeColor = Color.PINK; break;
     }
     switch (settings.theme) {
         case "Classic": backgroundColor = Color.BLACK; break;
         case "Dark": backgroundColor = Color.DARK_GRAY; break;
         case "Neon": backgroundColor = Color.BLUE; break;
         case "Light": backgroundColor = Color.WHITE; break;
     }
 }

 public void startGame() {
     newApple();
     newCoin();
     generateObstacles(5);
     running = true;
     timer = new Timer(delay, this);
     timer.start();
 }

 public void restartGame() {
     bodyParts = 6;
     applesEaten = 0;
     coinsCollected = 0;
     direction = 'R';
     x[0] = SCREEN_WIDTH / 2;
     y[0] = SCREEN_HEIGHT / 2;
     level = 1;
     applySettings();
     startGame();
     repaint();
 }

 public void paintComponent(Graphics g) {
     super.paintComponent(g);
     draw(g);
 }

 public void draw(Graphics g) {
     if (running) {
         g.setColor(Color.RED);
         g.fillOval(appleX, appleY, UNIT_SIZE, UNIT_SIZE);

         if (coinVisible) {
             g.setColor(Color.YELLOW);
             g.fillOval(coinX, coinY, UNIT_SIZE, UNIT_SIZE);
         }

         g.setColor(Color.GRAY);
         for (Point obstacle : obstacles) {
             g.fillRect(obstacle.x, obstacle.y, UNIT_SIZE, UNIT_SIZE);
         }

         for (int i = 0; i < bodyParts; i++) {
             g.setColor(snakeColor);
             g.fillRect(x[i], y[i], UNIT_SIZE, UNIT_SIZE);
         }

         g.setColor(Color.BLACK);
         g.setFont(new Font("Ink Free", Font.BOLD, 20));
         FontMetrics metrics = getFontMetrics(g.getFont());
         g.drawString("Score: " + applesEaten + " Coins: " + coinsCollected + " Level: " + level + " High Score: " + highScore,
                 (SCREEN_WIDTH - metrics.stringWidth("Score")) / 2, g.getFont().getSize());
     } else {
         gameOver(g);
     }
 }

 public void newApple() {
     appleX = random.nextInt((int) (SCREEN_WIDTH / UNIT_SIZE)) * UNIT_SIZE;
     appleY = random.nextInt((int) (SCREEN_HEIGHT / UNIT_SIZE)) * UNIT_SIZE;
 }

 public void newCoin() {
     if (random.nextInt(5) == 0) {
         coinX = random.nextInt((int) (SCREEN_WIDTH / UNIT_SIZE)) * UNIT_SIZE;
         coinY = random.nextInt((int) (SCREEN_HEIGHT / UNIT_SIZE)) * UNIT_SIZE;
         coinVisible = true;
     } else {
         coinVisible = false;
     }
 }

 public void generateObstacles(int count) {
     obstacles.clear();
     for (int i = 0; i < count; i++) {
         int ox = random.nextInt(SCREEN_WIDTH / UNIT_SIZE) * UNIT_SIZE;
         int oy = random.nextInt(SCREEN_HEIGHT / UNIT_SIZE) * UNIT_SIZE;
         obstacles.add(new Point(ox, oy));
     }
 }

 public void move() {
     for (int i = bodyParts; i > 0; i--) {
         x[i] = x[i - 1];
         y[i] = y[i - 1];
     }
     switch (direction) {
         case 'U': y[0] -= UNIT_SIZE; break;
         case 'D': y[0] += UNIT_SIZE; break;
         case 'L': x[0] -= UNIT_SIZE; break;
         case 'R': x[0] += UNIT_SIZE; break;
     }
 }

 public void checkApple() {
     if ((x[0] == appleX) && (y[0] == appleY)) {
         bodyParts++;
         applesEaten++;
         newApple();
         newCoin();
         if (applesEaten % 5 == 0) {
             level++;
             if (delay > 50) delay -= 10;
             timer.setDelay(delay);
             generateObstacles(level + 4);
         }
     }
 }

 public void checkCoin() {
     if (coinVisible && x[0] == coinX && y[0] == coinY) {
         coinsCollected++;
         coinVisible = false;
     }
 }

 public void checkCollisions() {
     for (int i = bodyParts; i > 0; i--) {
         if ((x[0] == x[i]) && (y[0] == y[i])) running = false;
     }
     if (x[0] < 0 || x[0] >= SCREEN_WIDTH || y[0] < 0 || y[0] >= SCREEN_HEIGHT) running = false;

     for (Point obstacle : obstacles) {
         if (x[0] == obstacle.x && y[0] == obstacle.y) {
             running = false;
             break;
         }
     }

     if (!running) {
         timer.stop();
         if (applesEaten > highScore) saveHighScore(applesEaten);
     }
 }

 public void gameOver(Graphics g) {
     g.setColor(Color.RED);
     g.setFont(new Font("Ink Free", Font.BOLD, 40));
     FontMetrics metrics = getFontMetrics(g.getFont());
     g.drawString("Game Over", (SCREEN_WIDTH - metrics.stringWidth("Game Over")) / 2, SCREEN_HEIGHT / 2);

     g.setFont(new Font("Ink Free", Font.PLAIN, 20));
     g.drawString("Press 'R' to Restart", (SCREEN_WIDTH - metrics.stringWidth("Press 'R' to Restart")) / 2, SCREEN_HEIGHT / 2 + 50);
 }

 public void actionPerformed(ActionEvent e) {
     if (running && !paused) {
         move();
         checkApple();
         checkCoin();
         checkCollisions();
     }
     repaint();
 }

 public class MyKeyAdapter extends KeyAdapter {
     public void keyPressed(KeyEvent e) {
         switch (e.getKeyCode()) {
             case KeyEvent.VK_LEFT:
                 if (direction != 'R') direction = 'L';
                 break;
             case KeyEvent.VK_RIGHT:
                 if (direction != 'L') direction = 'R';
                 break;
             case KeyEvent.VK_UP:
                 if (direction != 'D') direction = 'U';
                 break;
             case KeyEvent.VK_DOWN:
                 if (direction != 'U') direction = 'D';
                 break;
             case KeyEvent.VK_R:
                 if (!running) restartGame();
                 break;
             case KeyEvent.VK_P:
                 paused = !paused;
                 break;
         }
     }
 }

 private int loadHighScore() {
     try (BufferedReader br = new BufferedReader(new FileReader("highscore.txt"))) {
         return Integer.parseInt(br.readLine());
     } catch (Exception e) {
         return 0;
     }
 }

 private void saveHighScore(int score) {
     try (BufferedWriter bw = new BufferedWriter(new FileWriter("highscore.txt"))) {
         bw.write(String.valueOf(score));
     } catch (IOException e) {
         e.printStackTrace();
     }
 }
}
