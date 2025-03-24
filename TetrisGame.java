import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class TetrisGame extends JFrame {
    private static final int BOARD_WIDTH = 10;
    private static final int BOARD_HEIGHT = 20;
    private static final int BLOCK_SIZE = 30;
    
    private GameBoard gameBoard;
    private WelcomeScreen welcomeScreen;
    private JLabel statusBar;
    private CardLayout cardLayout;
    
    public TetrisGame() {
        statusBar = new JLabel(" 0");
        gameBoard = new GameBoard(this);
        welcomeScreen = new WelcomeScreen(this);
        gameBoard.setFocusable(true);
        
        // Create a card layout to switch between welcome screen and game board
        cardLayout = new CardLayout();
        JPanel mainPanel = new JPanel(cardLayout);
        mainPanel.add(welcomeScreen, "welcome");
        mainPanel.add(gameBoard, "game");
        
        add(statusBar, BorderLayout.SOUTH);
        add(mainPanel, BorderLayout.CENTER);
        
        // Show welcome screen first
        cardLayout.show(mainPanel, "welcome");
        
        setSize(BOARD_WIDTH * BLOCK_SIZE + 16, BOARD_HEIGHT * BLOCK_SIZE + 39);
        setTitle("Tetris");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
    }
    
    public JLabel getStatusBar() {
        return statusBar;
    }
    
    /**
     * Starts the game by switching from welcome screen to game board
     * and initializing the game.
     */
    public void startGame() {
        cardLayout.show((JPanel)gameBoard.getParent(), "game");
        gameBoard.requestFocusInWindow();
        gameBoard.start();
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            TetrisGame game = new TetrisGame();
            game.setVisible(true);
        });
    }
}