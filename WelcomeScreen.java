import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * The WelcomeScreen class displays an initial welcome message with game rules
 * and scoring information before the actual game starts.
 */
public class WelcomeScreen extends JPanel {
    private TetrisGame parentGame;
    
    /**
     * Constructor for the welcome screen.
     * 
     * @param parent The parent TetrisGame frame that contains this panel
     */
    public WelcomeScreen(TetrisGame parent) {
        this.parentGame = parent;
        
        // Set up the panel layout
        setLayout(new BorderLayout());
        setBackground(new Color(40, 40, 40));
        
        // Create the title label
        JLabel titleLabel = new JLabel("TETRIS");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 48));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setHorizontalAlignment(JLabel.CENTER);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(30, 0, 20, 0));
        
        // Create the content panel with game information and wrap it in a scroll pane
        JPanel contentPanel = createContentPanel();
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16); // Smoother scrolling
        
        // Create the button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(40, 40, 40));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 30, 0));
        
        // Create the start button
        JButton startButton = new JButton("Start Game");
        startButton.setFont(new Font("Arial", Font.BOLD, 18));
        startButton.setFocusPainted(false);
        startButton.setBackground(new Color(0, 150, 0));
        startButton.setForeground(Color.WHITE);
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                parentGame.startGame();
            }
        });
        
        // Add the button to the button panel
        buttonPanel.add(startButton);
        
        // Add components to the main panel
        add(titleLabel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER); // Add the scroll pane instead of content panel directly
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    /**
     * Creates the content panel with game rules and scoring information.
     * 
     * @return A JPanel containing formatted game information
     */
    private JPanel createContentPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(40, 40, 40));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 40, 10, 40));
        
        // Game description
        addSection(panel, "How to Play", 
            "Arrange falling blocks to create complete horizontal lines.\n" +
            "When a line is complete, it will disappear and you earn points.\n" +
            "The game ends when the blocks reach the top of the screen.");
        
        // Controls section
        addSection(panel, "Controls", 
            "Left Arrow: Move piece left\n" +
            "Right Arrow: Move piece right\n" +
            "Up Arrow: Rotate piece counter-clockwise\n" +
            "Down Arrow: Rotate piece clockwise\n" +
            "Space: Drop piece to bottom\n" +
            "D: Move piece down one line\n" +
            "P: Pause/Resume game");
        
        // Scoring system
        addSection(panel, "Scoring System", 
            "Single line: 40 × level\n" +
            "Double line: 100 × level\n" +
            "Triple line: 300 × level\n" +
            "Tetris (4 lines): 1200 × level + 400 bonus\n\n" +
            "Combo bonus: 50 × combo × level\n" +
            "Perfect clear: 3000 × level\n" +
            "Quick drop bonus: Up to 200 points");
        
        return panel;
    }
    
    /**
     * Helper method to add a formatted section to the content panel.
     * 
     * @param panel The panel to add the section to
     * @param title The section title
     * @param content The section content
     */
    private void addSection(JPanel panel, String title, String content) {
        // Add section title
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(new Color(255, 200, 0));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(5));
        
        // Add section content
        JTextArea contentArea = new JTextArea(content);
        contentArea.setFont(new Font("Arial", Font.PLAIN, 14));
        contentArea.setForeground(Color.WHITE);
        contentArea.setBackground(new Color(40, 40, 40));
        contentArea.setEditable(false);
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        contentArea.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(contentArea);
        panel.add(Box.createVerticalStrut(15));
    }
}