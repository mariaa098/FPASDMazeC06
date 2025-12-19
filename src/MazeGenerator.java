import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.sound.sampled.Clip;

public class MazeGenerator extends JFrame {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }

            MazeGenerator frame = new MazeGenerator();
            boolean startGame = WelcomeScreen.showWelcome(frame);
            if (startGame) {
                frame.setVisible(true);
                frame.bgMusic = SoundManager.playSoundLoop("backsound.wav");
                SwingUtilities.invokeLater(() -> frame.generateMaze());
            } else {
                System.exit(0);
            }
        });
    }

    private static final int ROWS = 20;  // Dikurangi dari 25
    private static final int COLS = 32;  // Dikurangi dari 30
    private static final int CELL_SIZE = 26;  // Dikurangi dari 32
    private static final int DELAY = 15;
    private static final int WALK_DELAY = 100;
    private boolean isWinDialogOpen = false;

    private static final Color BG_COLOR = new Color(92, 148, 252);
    private static final Color PANEL_COLOR = new Color(93, 145, 247);
    private static final Color BUTTON_COLOR = new Color(243, 145, 112);
    private static final Color BUTTON_GREEN = new Color(154, 218, 154);
    private static final Color BUTTON_YELLOW = new Color(248, 192, 0);
    private static final Color BUTTON_RED = new Color(255, 100, 100);
    private static final Color LABEL_TEXT_COLOR = Color.WHITE;
    private static final Color BUTTON_TEXT_COLOR = Color.WHITE;
    private static final Font PIXEL_FONT = new Font("Monospaced", Font.BOLD, 16);
    private static final Font TITLE_FONT = new Font("Monospaced", Font.BOLD, 22);
    private static final Font LEGEND_FONT = new Font("Monospaced", Font.BOLD, 12);

    private Cell[][] maze;
    private MazePanel mazePanel;
    private WeightedGraph graph;
    private Cell start, end;

    private Thread currentSolvingThread;
    private final AtomicBoolean stopCurrentSolving = new AtomicBoolean(false);

    private Clip bgMusic;
    private Clip endMusic;
    private Clip heroMusic;
    private Thread heroThread;

    public MazeGenerator() {
        setTitle("🍄 SUPER MARIO MAZE QUEST 🍄");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setBackground(BG_COLOR);
        setLayout(new BorderLayout(0, 0));

        JPanel topPanel = createTopPanel();
        topPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 4, 0, Color.BLACK));
        add(topPanel, BorderLayout.NORTH);

        mazePanel = new MazePanel(ROWS, COLS, CELL_SIZE);
        maze = new Cell[ROWS][COLS];
        initializeMaze();
        mazePanel.setMaze(maze);
        mazePanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 4));
        add(mazePanel, BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null);
        setResizable(false);
        customizeOptionPane();
    }

    private JPanel createTopPanel() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(PANEL_COLOR);
        mainPanel.setBorder(new EmptyBorder(12, 15, 12, 15));

        JLabel titleLabel = new JLabel("🌟 SUPER MARIO MAZE QUEST 🌟");
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setForeground(Color.YELLOW);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(titleLabel);
        mainPanel.add(Box.createVerticalStrut(6));

        JPanel terrainSection = new JPanel();
        terrainSection.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 3));
        terrainSection.setOpaque(false);

        JLabel terrainTitle = new JLabel("🏔 TERRAIN:");
        terrainTitle.setFont(PIXEL_FONT);
        terrainTitle.setForeground(Color.WHITE);
        terrainSection.add(terrainTitle);

        terrainSection.add(createCompactTerrainItem("🧱 BRICK (0)", Terrain.STONE.color));
        terrainSection.add(createCompactTerrainItem("🌿 GRASS (1)", Terrain.GRASS.color));
        terrainSection.add(createCompactTerrainItem("🏖 SAND (5)", Terrain.SAND.color));
        terrainSection.add(createCompactTerrainItem("🔥 LAVA (10)", Terrain.LAVA.color));

        mainPanel.add(terrainSection);
        mainPanel.add(Box.createVerticalStrut(5));

        JPanel groundLine = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;

                g2d.setColor(new Color(194, 133, 105));
                for(int x = 0; x < getWidth(); x += 28) {
                    g2d.fillRect(x, 0, 26, 6);
                    g2d.setColor(new Color(139, 90, 43));
                    g2d.drawRect(x, 0, 26, 6);
                    g2d.setColor(new Color(194, 133, 105));
                }

                g2d.setColor(new Color(0, 168, 0));
                g2d.fillRect(15, -30, 40, 36);
                g2d.setColor(new Color(0, 120, 0));
                g2d.fillRect(12, -30, 46, 10);

                int qx = getWidth() - 60;
                g2d.setColor(new Color(248, 192, 0));
                g2d.fillRect(qx, -24, 20, 20);
                g2d.setColor(new Color(200, 150, 0));
                g2d.drawRect(qx, -24, 20, 20);
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Arial", Font.BOLD, 16));
                g2d.drawString("?", qx + 6, -9);

                g2d.setColor(new Color(248, 192, 0));
                for(int i = 0; i < 3; i++) {
                    int cx = getWidth()/2 - 25 + i * 25;
                    g2d.fillOval(cx, -20, 16, 16);
                    g2d.setColor(new Color(200, 150, 0));
                    g2d.drawOval(cx, -20, 16, 16);
                    g2d.setColor(new Color(248, 192, 0));
                }
            }
        };

        groundLine.setPreferredSize(new Dimension(COLS * CELL_SIZE, 6));
        groundLine.setOpaque(false);
        mainPanel.add(groundLine);
        mainPanel.add(Box.createVerticalStrut(2));

        JPanel controlsSection = new JPanel();
        controlsSection.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 3));
        controlsSection.setOpaque(false);

        JLabel controlTitle = new JLabel("🎮 CONTROLS:");
        controlTitle.setFont(PIXEL_FONT);
        controlTitle.setForeground(Color.WHITE);
        controlsSection.add(controlTitle);

        JButton generateBtn = createMarioButton("NEW", BUTTON_GREEN, "🍄");
        JButton bfsBtn = createMarioButton("BFS", BUTTON_COLOR, "🔍");
        JButton dfsBtn = createMarioButton("DFS", BUTTON_COLOR, "🔍");
        JButton dijkstraBtn = createMarioButton("DIJKSTRA", BUTTON_COLOR, "⭐");
        JButton astarBtn = createMarioButton("A*", BUTTON_COLOR, "⚡");
        JButton resetBtn = createMarioButton("RESET", BUTTON_YELLOW, "🔄");
        JButton exitBtn = createMarioButton("EXIT", BUTTON_RED, "❌"); // TOMBOL EXIT

        generateBtn.addActionListener(e -> generateMaze());
        bfsBtn.addActionListener(e -> startNewSolving(() -> solveBFS()));
        dfsBtn.addActionListener(e -> startNewSolving(() -> solveDFS()));
        dijkstraBtn.addActionListener(e -> startNewSolving(() -> solveDijkstra()));
        astarBtn.addActionListener(e -> startNewSolving(() -> solveAStar()));
        resetBtn.addActionListener(e -> resetMaze());
        exitBtn.addActionListener(e -> exitGame());

        controlsSection.add(generateBtn);
        controlsSection.add(bfsBtn);
        controlsSection.add(dfsBtn);
        controlsSection.add(dijkstraBtn);
        controlsSection.add(astarBtn);
        controlsSection.add(resetBtn);
        controlsSection.add(exitBtn);

        mainPanel.add(controlsSection);
        return mainPanel;
    }

}