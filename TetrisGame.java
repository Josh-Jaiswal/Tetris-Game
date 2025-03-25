import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * The main Tetris game window.
 * This is where everything comes together - the welcome screen, game board, and status display.
 */
public class TetrisGame extends JFrame {
    // Standard Tetris dimensions - classic 10x20 grid
    private static final int BOARD_WIDTH = 10;
    private static final int BOARD_HEIGHT = 20;
    private static final int BLOCK_SIZE = 30; // Each block is 30 pixels - nice and chunky
    
    private GameBoard gameBoard;
    private WelcomeScreen welcomeScreen;
    private JLabel statusBar;
    private CardLayout cardLayout;
    
    /**
     * Sets up the main game window with all its components.
     * Think of this as building the arcade cabinet that houses our game.
     */
    public TetrisGame() {
        statusBar = new JLabel(" 0");
        gameBoard = new GameBoard(this);
        welcomeScreen = new WelcomeScreen(this);
        gameBoard.setFocusable(true);
        
        // We use cards to flip between screens - just like switching game modes
        cardLayout = new CardLayout();
        JPanel mainPanel = new JPanel(cardLayout);
        mainPanel.add(welcomeScreen, "welcome");
        mainPanel.add(gameBoard, "game");
        
        add(statusBar, BorderLayout.SOUTH);
        add(mainPanel, BorderLayout.CENTER);
        
        // Start with the welcome screen - gotta read the instructions first!
        cardLayout.show(mainPanel, "welcome");
        
        // Size the window to fit our game grid perfectly
        setSize(BOARD_WIDTH * BLOCK_SIZE + 16, BOARD_HEIGHT * BLOCK_SIZE + 39);
        setTitle("Tetris");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center on screen
        setResizable(false); // Keep it locked - no cheating by resizing!
    }
    
    /**
     * Gives other components access to the status bar.
     * This way the game board can update the score display.
     */
    public JLabel getStatusBar() {
        return statusBar;
    }
    
    /**
     * Kicks off the actual gameplay.
     * Flips from the welcome screen to the game board and starts the action!
     */
    public void startGame() {
        cardLayout.show((JPanel)gameBoard.getParent(), "game");
        gameBoard.requestFocusInWindow(); // Make sure keyboard controls work right away
        gameBoard.start();
    }
    
    /**
     * The entry point for our Tetris adventure.
     * Creates the game window and gets everything rolling.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            TetrisGame game = new TetrisGame();
            game.setVisible(true);
        });
    }
}