import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;
import javax.sound.sampled.*;
//import java.io.ByteArrayInputStream;

public class DungeonEscapeGame {
    private static int currentRoom = 1;
    private static int totalAttempts = 0;
    private static int score = 100;
    private static int secretNumber;
    private static Random random = new Random();
    private static int doorShakeOffset = 0;
    private static int doorOpenAmount = 0;
    private static boolean showingWinMessage = false;
    private static String winMessage = "";
    private static int winMessageSize = 10;
    private static int winMessageAlpha = 0;
    
    private static Timer shakeTimer;
    private static Timer doorOpenTimer;
    private static Timer winMessageTimer;
    private static Timer nextRoomTimer;

    // GUI Components
    private static JFrame frame;
    private static JLabel roomLabel, hintLabel, scoreLabel;
    private static JTextField guessField;
    private static JButton submitButton;
    private static JPanel gamePanel;

    // Room colors
    private static final Color[] ROOM_COLORS = {
        new Color(30, 30, 50),   // Dark cell
        new Color(70, 30, 30),   // Torture chamber
        new Color(120, 100, 50), // Treasure room
        new Color(50, 20, 20),   // Boss room
        new Color(30, 70, 30)    // Victory
    };

    // Room names
    private static final String[] ROOM_NAMES = {
        "ROOM 1: THE DARK CELL",
        "ROOM 2: THE TORTURE CHAMBER",
        "ROOM 3: THE TREASURE VAULT",
        "FINAL BOSS: DUNGEON GUARDIAN",
        "CONGRATULATIONS!"
    };

    // Room number ranges
    private static final int[][] ROOM_RANGES = {
        {1, 50}, {1, 75}, {1, 100}, {1, 200}, {0, 0}
    };

    public static void main(String[] args) {
        // Set up the main window
        frame = new JFrame("Escape the Dungeon");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(700, 650);
        frame.setLayout(new BorderLayout());

        // Main game panel
        gamePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // Draw dungeon-like background pattern
                g.setColor(ROOM_COLORS[currentRoom-1]);
                g.fillRect(0, 0, getWidth(), getHeight());
                
                // Add dungeon wall texture
                g.setColor(ROOM_COLORS[currentRoom-1].darker());
                for (int i = 0; i < getWidth(); i += 40) {
                    for (int j = 0; j < getHeight(); j += 40) {
                        g.drawRect(i, j, 20, 20);
                    }
                }
                
                // Draw the dungeon door at the bottom
                drawDoor(g);
                
                // Draw win message if showing
                if (showingWinMessage) {
                    drawWinMessage(g);
                }
                
                // Special effects for treasure room
                if (currentRoom == 3) {
                    g.setColor(new Color(255, 215, 0, 100));
                    int[] xPoints = {getWidth()/2, getWidth(), getWidth()/2, 0};
                    int[] yPoints = {0, getHeight()/2, getHeight(), getHeight()/2};
                    g.fillPolygon(xPoints, yPoints, 4);
                }
                
                // Boss room effect
                if (currentRoom == 4) {
                    g.setColor(new Color(255, 0, 0, 50));
                    g.fillOval(getWidth()/4, getHeight()/4, 
                               getWidth()/2, getHeight()/2);
                }
            }
            
            private void drawDoor(Graphics g) {
                int doorWidth = 120;
                int doorHeight = 180;
                // Position door at bottom center, accounting for shake offset
                int doorX = getWidth()/2 - doorWidth/2 + doorShakeOffset;
                int doorY = getHeight() - doorHeight - 20;
                
                // Door frame
                g.setColor(new Color(70, 50, 30));
                g.fillRect(doorX - 10, doorY - 10, doorWidth + 20, doorHeight + 20);
                
                // Left door (opens to the left)
                g.setColor(new Color(100, 70, 40));
                int leftDoorWidth = doorWidth/2;
                int leftDoorX = doorX + (int)(leftDoorWidth * (1 - doorOpenAmount/100.0));
                g.fillRect(leftDoorX, doorY, leftDoorWidth, doorHeight);
                
                // Right door (opens to the right)
                g.setColor(new Color(100, 70, 40));
                int rightDoorWidth = doorWidth/2;
                int rightDoorX = doorX + doorWidth/2 + (int)(rightDoorWidth * doorOpenAmount/100.0);
                g.fillRect(rightDoorX, doorY, rightDoorWidth, doorHeight);
                
                // Door details
                g.setColor(new Color(80, 60, 30));
                // Left door details
                g.fillRect(leftDoorX + 5, doorY + 10, leftDoorWidth - 10, 15);
                g.fillRect(leftDoorX + 5, doorY + 50, leftDoorWidth - 10, 15);
                g.fillRect(leftDoorX + 5, doorY + 90, leftDoorWidth - 10, 15);
                
                // Right door details
                g.fillRect(rightDoorX + 5, doorY + 10, rightDoorWidth - 10, 15);
                g.fillRect(rightDoorX + 5, doorY + 50, rightDoorWidth - 10, 15);
                g.fillRect(rightDoorX + 5, doorY + 90, rightDoorWidth - 10, 15);
                
                // Door knob (only on right door)
                g.setColor(Color.YELLOW);
                g.fillOval(rightDoorX + rightDoorWidth - 20, doorY + doorHeight/2, 12, 12);
            }
            
            private void drawWinMessage(Graphics g) {
                Font font = new Font("Impact", Font.BOLD, winMessageSize);
                g.setFont(font);
                
                // Calculate message position
                FontMetrics fm = g.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(winMessage)) / 2;
                int y = getHeight() / 2;
                
                // Draw glowing effect
                for (int i = 0; i < 5; i++) {
                    g.setColor(new Color(255, 255, 100, winMessageAlpha - i*20));
                    g.drawString(winMessage, x - i, y - i);
                    g.drawString(winMessage, x + i, y + i);
                }
                
                // Draw main text
                g.setColor(new Color(255, 255, 0, winMessageAlpha));
                g.drawString(winMessage, x, y);
                
                // Draw explosion effect
                g.setColor(new Color(255, 200, 0, winMessageAlpha/2));
                g.fillOval(x - winMessageSize, y - winMessageSize, 
                          fm.stringWidth(winMessage) + winMessageSize*2, 
                          winMessageSize*2);
            }
        };
        gamePanel.setLayout(new BoxLayout(gamePanel, BoxLayout.Y_AXIS));
        frame.add(gamePanel, BorderLayout.CENTER);

        // Initialize shake timer
        shakeTimer = new Timer(50, new ActionListener() {
            private int shakeCount = 0;
            private int shakeDirection = 1;
            
            @Override
            public void actionPerformed(ActionEvent e) {
                if (shakeCount < 10) {
                    doorShakeOffset = shakeDirection * (10 - shakeCount);
                    shakeDirection *= -1;
                    shakeCount++;
                    gamePanel.repaint();
                } else {
                    doorShakeOffset = 0;
                    shakeCount = 0;
                    shakeTimer.stop();
                    gamePanel.repaint();
                }
            }
        });

        // Initialize door open timer
        doorOpenTimer = new Timer(20, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (doorOpenAmount < 100) {
                    doorOpenAmount += 5;
                    gamePanel.repaint();
                } else {
                    doorOpenTimer.stop();
                    // Show win message after door is fully open
                    showWinMessage();
                }
            }
        });

        // Initialize win message timer
        winMessageTimer = new Timer(30, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (winMessageSize < 60) {
                    winMessageSize += 2;
                    winMessageAlpha = Math.min(255, winMessageAlpha + 10);
                    gamePanel.repaint();
                } else {
                    // Keep the message visible for longer
                    if (winMessageAlpha > 0) {
                        winMessageAlpha -= 5;
                        gamePanel.repaint();
                    } else {
                        winMessageTimer.stop();
                        // After showing win message, move to next room
                        moveToNextRoom();
                    }
                }
            }
        });

        // Initialize next room timer
        nextRoomTimer = new Timer(2000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                currentRoom++;
                if (currentRoom <= 4) {
                    setupNewRoom();
                } else {
                    // Game completed
                    roomLabel.setText(ROOM_NAMES[4]);
                    hintLabel.setText("You escaped in " + totalAttempts + 
                                     " attempts! Final Score: " + score);
                    submitButton.setEnabled(false);
                }
                nextRoomTimer.stop();
            }
        });

        // Room label
        roomLabel = new JLabel(ROOM_NAMES[0], JLabel.CENTER);
        roomLabel.setFont(new Font("Georgia", Font.BOLD, 24));
        roomLabel.setForeground(Color.WHITE);
        roomLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        gamePanel.add(Box.createVerticalStrut(40));
        gamePanel.add(roomLabel);

        // Hint label
        hintLabel = new JLabel("Guess a number between 1-50 to unlock the door!", 
                             JLabel.CENTER);
        hintLabel.setFont(new Font("Georgia", Font.PLAIN, 16));
        hintLabel.setForeground(Color.WHITE);
        hintLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        gamePanel.add(Box.createVerticalStrut(30));
        gamePanel.add(hintLabel);

        // Guess field
        guessField = new JTextField(10);
        guessField.setMaximumSize(new Dimension(200, 35));
        guessField.setFont(new Font("Georgia", Font.BOLD, 16));
        guessField.setHorizontalAlignment(JTextField.CENTER);
        guessField.setAlignmentX(Component.CENTER_ALIGNMENT);
        gamePanel.add(Box.createVerticalStrut(20));
        gamePanel.add(guessField);

        // Submit button
        submitButton = new JButton("Submit Guess");
        submitButton.setFont(new Font("Georgia", Font.BOLD, 14));
        submitButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        submitButton.addActionListener(new SubmitGuessListener());
        gamePanel.add(Box.createVerticalStrut(20));
        gamePanel.add(submitButton);

        // Score label
        scoreLabel = new JLabel("Score: 100 | Attempts: 0", JLabel.CENTER);
        scoreLabel.setFont(new Font("Georgia", Font.BOLD, 14));
        scoreLabel.setForeground(Color.WHITE);
        scoreLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        gamePanel.add(Box.createVerticalStrut(40));
        gamePanel.add(scoreLabel);

        generateNewNumber(ROOM_RANGES[0][0], ROOM_RANGES[0][1]);
        frame.setVisible(true);
    }

    private static void generateNewNumber(int min, int max) {
        secretNumber = random.nextInt(max) + min;
    }

    private static void playSound(boolean isCorrect) {
        try {
            // Generate simple beep sounds
            byte[] buf = new byte[1];
            AudioFormat af = new AudioFormat(8000f, 8, 1, true, false);
            
            if (isCorrect) {
                // Happy sound (rising tone)
                for (int i = 0; i < 100; i++) {
                    double angle = i / 20.0 * 2.0 * Math.PI;
                    buf[0] = (byte)(Math.sin(angle) * 100);
                }
            } else {
                // Error sound (buzzer)
                for (int i = 0; i < 100; i++) {
                    buf[0] = (byte)(Math.random() * 100);
                }
            }
            
            Clip clip = AudioSystem.getClip();
            clip.open(af, buf, 0, buf.length);
            clip.start();
        } catch (Exception e) {
            System.out.println("Sound error: " + e.getMessage());
        }
    }

    private static void showWinMessage() {
        showingWinMessage = true;
        winMessage = "YOU WON!!!";
        winMessageSize = 10;
        winMessageAlpha = 0;
        winMessageTimer.start();
    }

    private static void moveToNextRoom() {
        showingWinMessage = false;
        doorOpenAmount = 0;
        nextRoomTimer.start();
    }

    private static void setupNewRoom() {
        doorOpenAmount = 0;
        showingWinMessage = false;
        submitButton.setEnabled(true);
        roomLabel.setText(ROOM_NAMES[currentRoom-1]);
        hintLabel.setText("Guess a number between " + 
            ROOM_RANGES[currentRoom-1][0] + "-" + 
            ROOM_RANGES[currentRoom-1][1] + " to proceed!");
        generateNewNumber(ROOM_RANGES[currentRoom-1][0], ROOM_RANGES[currentRoom-1][1]);
        gamePanel.repaint();
    }

    private static class SubmitGuessListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                int guess = Integer.parseInt(guessField.getText());
                totalAttempts++;
                score -= 2;

                if (guess < secretNumber) {
                    hintLabel.setText("Too low! The door shakes violently!");
                    playSound(false);
                    shakeTimer.start();
                } else if (guess > secretNumber) {
                    hintLabel.setText("Too high! The door shakes violently!");
                    playSound(false);
                    shakeTimer.start();
                } else {
                    playSound(true);
                    // Correct guess - open door and show win message
                    doorOpenTimer.start();
                    submitButton.setEnabled(false); // Disable button during animation
                }
                scoreLabel.setText("Score: " + score + " | Attempts: " + totalAttempts);
                guessField.setText("");
            } catch (NumberFormatException ex) {
                hintLabel.setText("Please enter a valid number!");
            }
        }
    }
}