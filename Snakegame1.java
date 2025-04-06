/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package snakegame1;

/**
 *
 * @author Fiona
 */
import javax.swing.Timer.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;
import java.util.*;

public class Snakegame1 {
    private static final String LEADERBOARD_FILE = "leaderboard.txt";
    private static Map<String, Integer> leaderboard = new HashMap<>();
    
    public static void main(String[] args) {
        loadLeaderboard();
        SwingUtilities.invokeLater(Snakegame1::showMainMenu);
    }

    static void showMainMenu() {
        JFrame menuFrame = new JFrame("Snake Game Menu");
        menuFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        menuFrame.setSize(300, 200);
        menuFrame.setLocationRelativeTo(null);
        
        JPanel panel = new JPanel();
        menuFrame.add(panel);
        
        JButton newGameButton = new JButton("New Game");
        JButton continueButton = new JButton("Continue");
        JButton leaderboardButton = new JButton("Leaderboard");
        JButton exitButton = new JButton("Exit");
        
        panel.add(newGameButton);
        panel.add(continueButton);
        panel.add(leaderboardButton);
        panel.add(exitButton);
        
        newGameButton.addActionListener(e -> {
            menuFrame.dispose();
            startNewGame();
        });
        
        continueButton.addActionListener(e -> {
            menuFrame.dispose();
            continueGame();
        });
        
        leaderboardButton.addActionListener(e -> showLeaderboard());
        
        exitButton.addActionListener(e -> System.exit(0));
        
        menuFrame.setVisible(true);
    }
    
    private static void startNewGame() {
        String playerName = JOptionPane.showInputDialog("Enter your name:");
        if (playerName == null || playerName.trim().isEmpty()) return;
        new GameFrame(playerName);
    }
    
    private static void continueGame() {
        String playerName = JOptionPane.showInputDialog("Enter your name to continue:");
        if (playerName == null || playerName.trim().isEmpty()) return;
        new GameFrame(playerName);
    }
    
    private static void showLeaderboard() {
        StringBuilder sb = new StringBuilder("Leaderboard:\n");
        leaderboard.entrySet().stream()
            .sorted((a, b) -> b.getValue() - a.getValue())
            .forEach(entry -> sb.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n"));
        JOptionPane.showMessageDialog(null, sb.toString(), "Leaderboard", JOptionPane.INFORMATION_MESSAGE);
    }
    
    public static void updateScore(String playerName, int score) {
        leaderboard.put(playerName, Math.max(leaderboard.getOrDefault(playerName, 0), score));
        saveLeaderboard();
    }
    
    private static void loadLeaderboard() {
        try (BufferedReader br = new BufferedReader(new FileReader(LEADERBOARD_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                leaderboard.put(parts[0], Integer.parseInt(parts[1]));
            }
        } catch (IOException ignored) {}
    }
    
    private static void saveLeaderboard() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(LEADERBOARD_FILE))) {
            for (var entry : leaderboard.entrySet()) {
                bw.write(entry.getKey() + "," + entry.getValue());
                bw.newLine();
            }
        } catch (IOException ignored) {}
    }
}

class GameFrame extends JFrame {
    public GameFrame(String playerName) {
        this.add(new GamePanel(playerName));
        this.setTitle("Snake Game");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setResizable(false);
        this.pack();
        this.setVisible(true);
        this.setLocationRelativeTo(null);
    }
}

class GamePanel extends JPanel implements ActionListener {
    private static final int WIDTH = 600, HEIGHT = 600, UNIT_SIZE = 25, DELAY = 100;
    private final int[] x = new int[(WIDTH * HEIGHT) / (UNIT_SIZE * UNIT_SIZE)];
    private final int[] y = new int[(WIDTH * HEIGHT) / (UNIT_SIZE * UNIT_SIZE)];
    private int bodyParts = 6, applesEaten, appleX, appleY;
    private char direction = 'R';
    private boolean running = false;
    private javax.swing.Timer timer;
    private final String playerName;
    
    public GamePanel(String playerName) {
        this.playerName = playerName;
        this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        this.setBackground(Color.black);
        this.setFocusable(true);
        this.addKeyListener(new MyKeyAdapter());
        startGame();
    }
    
    private void startGame() {
        newApple();
        running = true;
        timer = new javax.swing.Timer(DELAY, this);
        timer.start();
    }
    
    private void newApple() {
        appleX = (int) (Math.random() * (WIDTH / UNIT_SIZE)) * UNIT_SIZE;
        appleY = (int) (Math.random() * (HEIGHT / UNIT_SIZE)) * UNIT_SIZE;
    }
    
    private void move() {
        for (int i = bodyParts; i > 0; i--) {
            x[i] = x[i - 1];
            y[i] = y[i - 1];
        }
        switch (direction) {
            case 'U' -> y[0] -= UNIT_SIZE;
            case 'D' -> y[0] += UNIT_SIZE;
            case 'L' -> x[0] -= UNIT_SIZE;
            case 'R' -> x[0] += UNIT_SIZE;
        }
    }
    
    private void checkApple() {
        if (x[0] == appleX && y[0] == appleY) {
            bodyParts++;
            applesEaten++;
            newApple();
        }
    }
    
    private void checkCollisions() {
        for (int i = bodyParts; i > 0; i--) {
            if (x[0] == x[i] && y[0] == y[i]) running = false;
        }
        if (x[0] < 0 || x[0] >= WIDTH || y[0] < 0 || y[0] >= HEIGHT) running = false;
        if (!running) {
            timer.stop();
            Snakegame1.updateScore(playerName, applesEaten);
            int choice = JOptionPane.showConfirmDialog(this, "Game Over! Score: " + applesEaten + "\nPlay Again?", "Game Over", JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) {
                new GameFrame(playerName);
            } else {
                Snakegame1.showMainMenu();
            }
        }
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.white);
        g.drawString("Score: " + applesEaten, 10, 20);
        if (running) {
            g.setColor(Color.red);
            g.fillOval(appleX, appleY, UNIT_SIZE, UNIT_SIZE);
            for (int i = 0; i < bodyParts; i++) {
                g.setColor(i == 0 ? Color.green : Color.blue);
                g.fillRect(x[i], y[i], UNIT_SIZE, UNIT_SIZE);
            }
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

    private class MyKeyAdapter extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_LEFT, KeyEvent.VK_A-> direction = 'L';
                case KeyEvent.VK_RIGHT, KeyEvent.VK_D-> direction = 'R';
                case KeyEvent.VK_UP, KeyEvent.VK_W-> direction = 'U';
                case KeyEvent.VK_DOWN, KeyEvent.VK_S -> direction = 'D';
            }
        }
    }
}
