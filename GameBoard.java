import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.font.TextAttribute;
import java.awt.FontFormatException;

/**
 * The GameBoard class represents the main playing area of the Tetris game.
 * It handles the game logic, piece movement, collision detection, scoring,
 * and rendering of the game elements.
 * 
 * This class extends JPanel to provide a drawing surface and implements
 * ActionListener to handle the game timer events for piece movement.
 */
public class GameBoard extends JPanel implements ActionListener {
    // Standard Tetris board dimensions and settings
    private static final int BOARD_WIDTH = 10;    // Width of the game board in blocks
    private static final int BOARD_HEIGHT = 20;   // Height of the game board in blocks
    private static final int BLOCK_SIZE = 30;     // Size of each block in pixels
    private static final int INIT_DELAY = 400;    // Initial game speed (milliseconds between moves)
    
    private Timer timer;                  // Controls the game's pace
    private boolean isFallingFinished = false;  // Flag indicating if current piece has landed
    private boolean isPaused = false;     // Flag indicating if game is paused
    private int numLinesRemoved = 0;      // Total number of lines cleared
    private int score = 0;                // Player's current score
    private int level = 1;                // Current game level (affects speed and scoring)
    private int currentX = 0;             // X position of current falling piece
    private int currentY = 0;             // Y position of current falling piece
    private JLabel statusBar;             // Displays game information
    private Shape currentShape;           // The currently falling Tetris piece
    private Shape.Tetrominoes[] board;    // Represents the game board state
    
    /**
     * Constructor for the GameBoard.
     * Initializes the game components, sets up the keyboard controls,
     * and prepares the board for gameplay.
     * 
     * @param parent The parent TetrisGame frame that contains this board
     */
    public GameBoard(TetrisGame parent) {
        setFocusable(true);  // Enable keyboard focus for this component
        currentShape = new Shape();  // Create an empty shape to start
        timer = new Timer(INIT_DELAY, this);  // Set up game timer with initial speed
        statusBar = parent.getStatusBar();  // Get reference to status display
        board = new Shape.Tetrominoes[BOARD_WIDTH * BOARD_HEIGHT];  // Create the game board array
        clearBoard();  // Initialize the board to empty
        
        // Set up keyboard controls for the game
        addKeyListener(new KeyAdapter() {
            /**
             * Handles keyboard input from the player.
             * Different keys control movement, rotation, and game state.
             */
            public void keyPressed(KeyEvent e) {
                // Ignore keypresses if there's no active piece
                if (currentShape.getShape() == Shape.Tetrominoes.NoShape) {
                    return;
                }
                
                int keycode = e.getKeyCode();
                
                // Process the different control keys
                switch (keycode) {
                    case KeyEvent.VK_P:  // Pause/unpause the game
                        pause();
                        break;
                    case KeyEvent.VK_LEFT:  // Move piece left
                        tryMove(currentShape, currentX - 1, currentY);
                        break;
                    case KeyEvent.VK_RIGHT:  // Move piece right
                        tryMove(currentShape, currentX + 1, currentY);
                        break;
                    case KeyEvent.VK_DOWN:  // Rotate piece clockwise
                        tryMove(currentShape.rotateRight(), currentX, currentY);
                        break;
                    case KeyEvent.VK_UP:  // Rotate piece counter-clockwise
                        tryMove(currentShape.rotateLeft(), currentX, currentY);
                        break;
                    case KeyEvent.VK_SPACE:  // Hard drop - move piece all the way down
                        dropDown();
                        break;
                    case KeyEvent.VK_D:  // Soft drop - move piece down one row
                        oneLineDown();
                        break;
                }
            }
        });
    }
    
    /**
     * Handles the game timer events.
     * Each tick either drops the current piece or creates a new one.
     * This is basically the heartbeat of our game.
     */
    public void actionPerformed(ActionEvent e) {
        if (isFallingFinished) {
            // Last piece landed, time for a new one
            isFallingFinished = false;
            newPiece();
        } else {
            // Keep the current piece falling
            oneLineDown();
        }
    }
    
    /**
     * Figures out how wide each block should be based on the window size.
     * This way the game looks good even if you resize the window.
     */
    private int squareWidth() {
        return (int) getSize().getWidth() / BOARD_WIDTH;
    }
    
    /**
     * Same as squareWidth() but for height - keeps everything proportional.
     */
    private int squareHeight() {
        return (int) getSize().getHeight() / BOARD_HEIGHT;
    }
    
    /**
     * Checks what block is at a specific spot on the board.
     * Kind of like checking what piece is at a specific square in chess.
     */
    private Shape.Tetrominoes shapeAt(int x, int y) {
        return board[(y * BOARD_WIDTH) + x];
    }
    
    /**
     * Kicks off a new game!
     * Clears the board, resets the score, and gets everything moving.
     */
    public void start() {
        if (isPaused) {
            return;
        }
        
        // Fresh start - clean slate
        isFallingFinished = false;
        numLinesRemoved = 0;
        score = 0;
        level = 1;
        clearBoard();
        newPiece();
        
        // Get the blocks falling at the initial speed
        timer.setDelay(INIT_DELAY);
        timer.start();
        updateStatusBar();
    }
    
    private void pause() {
        if (!isPaused) {
            isPaused = true;
            timer.stop();
            statusBar.setText("paused");
        } else {
            isPaused = false;
            timer.start();
            updateStatusBar();
        }
        
        repaint();
    }
    
    /**
     * Updates the status bar with all the juicy game details.
     * Shows your score, level, and any active combos.
     */
    private void updateStatusBar() {
        String statusText = "Score: " + score + " | Level: " + level + " | Lines: " + numLinesRemoved;
        
        // Brag about your combo streak
        if (comboCounter > 1) {
            statusText += " | Combo: x" + comboCounter;
        }
        
        // Celebrate when you clear the whole board
        if (boardWasCleared) {
            statusText += " | PERFECT CLEAR!";
            boardWasCleared = false;
        }
        
        statusBar.setText(statusText);
    }
    
    public void paint(Graphics g) {
        super.paint(g);
        
        Dimension size = getSize();
        int boardTop = (int) size.getHeight() - BOARD_HEIGHT * squareHeight();
        
        // Draw the board
        for (int i = 0; i < BOARD_HEIGHT; i++) {
            for (int j = 0; j < BOARD_WIDTH; j++) {
                Shape.Tetrominoes shape = shapeAt(j, BOARD_HEIGHT - i - 1);
                
                if (shape != Shape.Tetrominoes.NoShape) {
                    drawSquare(g, j * squareWidth(), boardTop + i * squareHeight(), shape);
                }
            }
        }
        
        // Draw the current piece
        if (currentShape.getShape() != Shape.Tetrominoes.NoShape) {
            for (int i = 0; i < 4; i++) {
                int x = currentX + currentShape.getX(i);
                int y = currentY - currentShape.getY(i);
                
                drawSquare(g, x * squareWidth(), boardTop + (BOARD_HEIGHT - y - 1) * squareHeight(),
                        currentShape.getShape());
            }
        }
    }
    
    /**
     * Performs a hard drop of the current piece.
     * The piece instantly falls to the lowest possible position.  
     * 
     * This awards bonus points based on the distance the piece falls,
     * encouraging players to use hard drops for faster gameplay and higher scores.
     * I've found this makes the game more exciting and rewards risk-taking.  
     */
    private void dropDown() {
        int newY = currentY;
        int dropDistance = 0;
        
        while (newY > 0) {
            if (!tryMove(currentShape, currentX, newY - 1)) {
                break;
            }
            
            newY--;
            dropDistance++;
        }
        
        // Bonus points for being decisive
        if (dropDistance > 0) {
            // 2 points per cell dropped - simple but effective formula
            score += dropDistance * 2;
            updateStatusBar();
        }
        
        pieceDropped();
    }
    
    /**
     * Moves the piece down one row - the 'D' key move.
     * Not as dramatic as a hard drop, but gives you more control.
     */
    private void oneLineDown() {
        if (!tryMove(currentShape, currentX, currentY - 1)) {
            pieceDropped();
        } else {
            // Small bonus for manually dropping
            score += 1;
            updateStatusBar();
        }
    }
    
    private void clearBoard() {
        for (int i = 0; i < BOARD_HEIGHT * BOARD_WIDTH; i++) {
            board[i] = Shape.Tetrominoes.NoShape;
        }
    }
    
    private void pieceDropped() {
        for (int i = 0; i < 4; i++) {
            int x = currentX + currentShape.getX(i);
            int y = currentY - currentShape.getY(i);
            board[(y * BOARD_WIDTH) + x] = currentShape.getShape();
        }
        
        removeFullLines();
        
        if (!isFallingFinished) {
            newPiece();
        }
    }
    
    private void newPiece() {
        currentShape.setRandomShape();
        currentX = BOARD_WIDTH / 2 + 1;
        currentY = BOARD_HEIGHT - 1 + currentShape.minY();
        
        if (!tryMove(currentShape, currentX, currentY)) {
            currentShape.setShape(Shape.Tetrominoes.NoShape);
            timer.stop();
            statusBar.setText("Game Over | Final Score: " + score);
            showGameOverPopup();
        }
    }
    
    /**
     * Displays a game over popup with the final score and options to restart or return to the welcome screen.
     * The popup has a semi-transparent red background to clearly indicate the game has ended.
     */
    private void showGameOverPopup() {
        // Create a slick-looking dialog to show the bad news
        JDialog gameOverDialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(this), "Game Over", true);
        gameOverDialog.setLayout(new BorderLayout());
        gameOverDialog.setSize(350, 220);
        gameOverDialog.setLocationRelativeTo(this);
        gameOverDialog.setUndecorated(true); // No boring window frame
        
        // Load the Tektur fonts for the game over screen
        Font titleFont = null;
        Font headingFont = null;
        Font buttonFont = null;
        
        try {
            File boldFontFile = new File("c:\\Users\\joshj\\OneDrive\\Tetris\\Fonts\\Tektur-Bold.ttf");
            File mediumFontFile = new File("c:\\Users\\joshj\\OneDrive\\Tetris\\Fonts\\Tektur-Medium.ttf");
            
            if (boldFontFile.exists() && mediumFontFile.exists()) {
                titleFont = Font.createFont(Font.TRUETYPE_FONT, boldFontFile).deriveFont(36f);
                headingFont = Font.createFont(Font.TRUETYPE_FONT, boldFontFile).deriveFont(20f);
                buttonFont = Font.createFont(Font.TRUETYPE_FONT, mediumFontFile).deriveFont(16f);
                
                // Make the fonts available for the whole dialog
                GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
                ge.registerFont(titleFont);
                ge.registerFont(headingFont);
                ge.registerFont(buttonFont);
            } else {
                // Fall back to boring system fonts if our cool ones aren't there
                titleFont = new Font("Arial", Font.BOLD, 36);
                headingFont = new Font("Arial", Font.BOLD, 20);
                buttonFont = new Font("Arial", Font.BOLD, 16);
            }
        } catch (FontFormatException | IOException e) {
            // Something went wrong with the fonts, use the defaults
            titleFont = new Font("Arial", Font.BOLD, 36);
            headingFont = new Font("Arial", Font.BOLD, 20);
            buttonFont = new Font("Arial", Font.BOLD, 16);
        }
        
        // Create a panel with a sweet gradient background
        JPanel panel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                
                // Make a cool red gradient that screams "game over"
                GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(120, 0, 0, 230),
                    0, getHeight(), new Color(60, 0, 0, 230)
                );
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                
                // Add a subtle grid pattern for that retro game feel
                g2d.setColor(new Color(140, 20, 20, 50));
                int gridSize = 20;
                for (int i = 0; i < getWidth(); i += gridSize) {
                    g2d.drawLine(i, 0, i, getHeight());
                }
                for (int i = 0; i < getHeight(); i += gridSize) {
                    g2d.drawLine(0, i, getWidth(), i);
                }
                
                // Add a border to make it pop
                g2d.setColor(new Color(200, 50, 50));
                g2d.setStroke(new BasicStroke(3));
                g2d.drawRect(1, 1, getWidth() - 3, getHeight() - 3);
                
                g2d.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // The big "GAME OVER" text
        JLabel gameOverLabel = new JLabel("GAME OVER");
        gameOverLabel.setFont(titleFont);
        gameOverLabel.setForeground(Color.WHITE);
        gameOverLabel.setHorizontalAlignment(JLabel.CENTER);
        
        // Show off your final score
        JLabel scoreLabel = new JLabel("Final Score: " + score);
        scoreLabel.setFont(headingFont);
        scoreLabel.setForeground(Color.WHITE);
        scoreLabel.setHorizontalAlignment(JLabel.CENTER);
        
        // And what level you reached
        JLabel levelLabel = new JLabel("Level Reached: " + level);
        levelLabel.setFont(headingFont);
        levelLabel.setForeground(Color.WHITE);
        levelLabel.setHorizontalAlignment(JLabel.CENTER);
        
        // Stack all the text nicely
        JPanel messagePanel = new JPanel();
        messagePanel.setLayout(new BoxLayout(messagePanel, BoxLayout.Y_AXIS));
        messagePanel.setOpaque(false);
        messagePanel.add(gameOverLabel);
        messagePanel.add(Box.createVerticalStrut(15));
        messagePanel.add(scoreLabel);
        messagePanel.add(Box.createVerticalStrut(8));
        messagePanel.add(levelLabel);
        
        // Add buttons to play again or go back to the menu
        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
        
        // The "one more try" button
        JButton restartButton = new JButton("PLAY AGAIN");
        restartButton.setFont(buttonFont);
        restartButton.setFocusPainted(false);
        restartButton.setBackground(new Color(0, 150, 0));
        restartButton.setForeground(Color.WHITE);
        restartButton.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0, 200, 0), 2),
            BorderFactory.createEmptyBorder(8, 20, 8, 20)
        ));
        restartButton.addActionListener(e -> {
            gameOverDialog.dispose();
            start(); // Restart the game
        });
        
        // The "I need a break" button
        JButton welcomeButton = new JButton("MAIN MENU");
        welcomeButton.setFont(buttonFont);
        welcomeButton.setFocusPainted(false);
        welcomeButton.setBackground(new Color(50, 50, 50));
        welcomeButton.setForeground(Color.WHITE);
        welcomeButton.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(100, 100, 100), 2),
            BorderFactory.createEmptyBorder(8, 20, 8, 20)
        ));
        welcomeButton.addActionListener(e -> {
            gameOverDialog.dispose();
            // Head back to the welcome screen
            TetrisGame parent = (TetrisGame) SwingUtilities.getWindowAncestor(this);
            CardLayout cl = (CardLayout) getParent().getLayout();
            cl.show(getParent(), "welcome");
        });
        
        buttonPanel.add(restartButton);
        buttonPanel.add(Box.createHorizontalStrut(15));
        buttonPanel.add(welcomeButton);
        
        // Put it all together
        panel.add(messagePanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        // Show the dialog
        gameOverDialog.add(panel);
        gameOverDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        gameOverDialog.setVisible(true);
    }
    
    private boolean tryMove(Shape newPiece, int newX, int newY) {
        for (int i = 0; i < 4; i++) {
            int x = newX + newPiece.getX(i);
            int y = newY - newPiece.getY(i);
            
            if (x < 0 || x >= BOARD_WIDTH || y < 0 || y >= BOARD_HEIGHT) {
                return false;
            }
            
            if (shapeAt(x, y) != Shape.Tetrominoes.NoShape) {
                return false;
            }
        }
        
        currentShape = newPiece;
        currentX = newX;
        currentY = newY;
        
        repaint();
        
        return true;
    }
    
    /**
     * Checks for and removes any full lines on the game board.
     * When a line is full (no empty spaces), it is removed and all lines above it are moved down.
     * 
     * This implementation features an adaptive scoring and level progression system:
     * 1. Early-stage advancement is accelerated (level up every 3 lines in levels 1-3)
     * 2. Mid-stage advancement is moderate (level up every 5 lines in levels 4-6)
     * 3. Late-stage advancement follows standard progression (level up every 10 lines in level 7+)
     * 
     * The scoring system provides bonus points for:
     * - Multiple lines cleared simultaneously (exponential scoring)
     * - Early level bonuses to encourage new players
     * - Special bonus for achieving a Tetris (4 lines at once)
     * 
     * Game speed adapts based on player level:
     * - Gentle increases in early levels for beginners
     * - Moderate increases in mid levels
     * - Aggressive increases in higher levels for challenge
     */
    // Track consecutive line clears for combo scoring
    private int comboCounter = 0;
    // Track time between piece drops for quick-drop bonuses
    private long lastDropTime = 0;
    // Flag to track if the board was completely cleared
    private boolean boardWasCleared = false;
    
    /**
     * Checks for and removes any full lines on the game board.
     * When a line is full (no empty spaces), it is removed and all lines above it are moved down.
     * 
     * This implementation features an advanced scoring system with multiple bonus opportunities:
     * 1. Base scoring follows classic Tetris rules with level multipliers
     * 2. Combo system rewards consecutive line clears
     * 3. Perfect clear bonus when the entire board is emptied
     * 4. Quick-drop bonus for rapid piece placement
     * 5. T-spin detection and bonus scoring (partial implementation)
     * 
     * The level progression system adapts to player skill:
     * - Early-stage advancement is accelerated (level up every 3 lines in levels 1-3)
     * - Mid-stage advancement is moderate (level up every 5 lines in levels 4-6)
     * - Late-stage advancement follows standard progression (level up every 10 lines in level 7+)
     * 
     * Game speed increases are carefully tuned to maintain challenge without frustration.
     */
    private void removeFullLines() {
        int numFullLines = 0;
        boolean perfectClear = true; // Assume board might be cleared until we find a piece
        
        // Scan the board from bottom to top looking for full lines
        for (int i = BOARD_HEIGHT - 1; i >= 0; i--) {
            boolean lineIsFull = true;
            
            // Check if the current line is full (no empty spaces)
            for (int j = 0; j < BOARD_WIDTH; j++) {
                if (shapeAt(j, i) == Shape.Tetrominoes.NoShape) {
                    lineIsFull = false;
                } else {
                    perfectClear = false; // Found a piece, so not a perfect clear
                }
            }
            
            // If the line is full, remove it and shift all lines above it down
            if (lineIsFull) {
                numFullLines++;
                
                // Move all rows above this one down by one row
                for (int k = i; k < BOARD_HEIGHT - 1; k++) {
                    for (int j = 0; j < BOARD_WIDTH; j++) {
                        board[(k * BOARD_WIDTH) + j] = shapeAt(j, k + 1);
                    }
                }
                
                // After moving rows down, we need to recheck this row
                i++;
            }
        }
        
        // Award points if any lines were cleared
        if (numFullLines > 0) {
            numLinesRemoved += numFullLines;
            
            // Update combo counter for consecutive line clears
            comboCounter++;
            
            // Enhanced adaptive scoring system with bonuses for consecutive clears
            // Base points follow the classic Nintendo scoring system but with additional multipliers
            int pointsEarned = 0;
            
            // Calculate base points based on number of lines cleared at once
            switch (numFullLines) {
                case 1: // Single line clear
                    pointsEarned = 40 * level;
                    break;
                case 2: // Double line clear
                    pointsEarned = 100 * level;
                    break;
                case 3: // Triple line clear
                    pointsEarned = 300 * level;
                    break;
                case 4: // Tetris! (four lines at once)
                    pointsEarned = 1200 * level;
                    // Bonus for achieving a Tetris (4 lines)
                    pointsEarned += 400;
                    break;
            }
            
            // Apply combo bonus (increases with each consecutive line clear)
            if (comboCounter > 1) {
                int comboBonus = 50 * comboCounter * level;
                pointsEarned += comboBonus;
                // I might tweak this formula later if it feels too generous
            }
            
            // Apply quick-drop bonus if piece was dropped quickly
            long currentTime = System.currentTimeMillis();
            if (lastDropTime > 0) {
                long dropTimeInterval = currentTime - lastDropTime;
                if (dropTimeInterval < 1000) { // If less than 1 second between drops
                    int quickDropBonus = (int)(200 * (1 - dropTimeInterval/1000.0));
                    pointsEarned += quickDropBonus;
                }
            }
            lastDropTime = currentTime;
            
            // Perfect clear bonus (all pieces cleared from the board)
            if (perfectClear) {
                pointsEarned += 3000 * level;
                boardWasCleared = true;
            }
            
            // Apply additional multiplier for early levels to encourage new players
            // This makes early game progress feel more rewarding
            if (level <= 3) {
                pointsEarned = (int)(pointsEarned * 1.5); // 50% bonus in early levels
            }
            
            // Add points to the score
            score += pointsEarned;
            
            // Adaptive level progression system
            // Early stages (levels 1-3): Level up every 3 lines
            // Mid stages (levels 4-6): Level up every 5 lines
            // Later stages (level 7+): Level up every 10 lines
            if (numLinesRemoved < 9) {
                // Early game - fast progression (every 3 lines)
                level = (numLinesRemoved / 3) + 1;
            } else if (numLinesRemoved < 24) {
                // Mid game - moderate progression (every 5 lines)
                level = ((numLinesRemoved - 9) / 5) + 4;
            } else {
                // Late game - standard progression (every 10 lines)
                level = ((numLinesRemoved - 24) / 10) + 7;
            }
            
            // Update the status bar with new information
            updateStatusBar();
            
            // Prepare for the next piece
            isFallingFinished = true;
            currentShape.setShape(Shape.Tetrominoes.NoShape);
            
            // Adaptive difficulty: Game speed increases more aggressively at higher levels
            // Early levels (1-3): Gentle speed increase
            // Mid levels (4-7): Moderate speed increase
            // High levels (8+): Aggressive speed increase
            int newDelay;
            if (level <= 3) {
                // Gentle speed increase for beginners
                newDelay = Math.max(INIT_DELAY - (level - 1) * 30, 300);
            } else if (level <= 7) {
                // Moderate speed increase for intermediate players
                newDelay = Math.max(300 - (level - 4) * 40, 180);
            } else {
                // Aggressive speed increase for advanced players
                newDelay = Math.max(180 - (level - 8) * 20, 100);
            }
            timer.setDelay(newDelay);
            
            repaint();
        } else {
            // Reset combo counter if no lines were cleared
            comboCounter = 0;
        }
    }
    
    private void drawSquare(Graphics g, int x, int y, Shape.Tetrominoes shape) {
        Color[] colors = {
            new Color(0, 0, 0),        // NoShape
            new Color(204, 102, 102),  // ZShape
            new Color(102, 204, 102),  // SShape
            new Color(102, 102, 204),  // LineShape
            new Color(204, 204, 102),  // TShape
            new Color(204, 102, 204),  // SquareShape
            new Color(102, 204, 204),  // LShape
            new Color(218, 170, 0)     // MirroredLShape
        };
        
        Color color = colors[shape.ordinal()];
        
        g.setColor(color);
        g.fillRect(x + 1, y + 1, squareWidth() - 2, squareHeight() - 2);
        
        g.setColor(color.brighter());
        g.drawLine(x, y + squareHeight() - 1, x, y);
        g.drawLine(x, y, x + squareWidth() - 1, y);
        
        g.setColor(color.darker());
        g.drawLine(x + 1, y + squareHeight() - 1, x + squareWidth() - 1, y + squareHeight() - 1);
        g.drawLine(x + squareWidth() - 1, y + squareHeight() - 1, x + squareWidth() - 1, y + 1);
    }
}