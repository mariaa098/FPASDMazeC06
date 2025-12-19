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

    private void exitGame() {
        final boolean[] wasPlayingEndMusic = {(endMusic != null && endMusic.isRunning())};

        JButton yesButton = createGlossyOptionButton("YES", BUTTON_RED );
        JButton noButton = createGlossyOptionButton("NO", BUTTON_GREEN);

        JPanel panel = new JPanel();
        panel.setBackground(PANEL_COLOR);
        JLabel messageLabel = new JLabel("Are you sure you want to exit?");
        messageLabel.setFont(PIXEL_FONT);
        messageLabel.setForeground(Color.WHITE);
        panel.add(messageLabel);

        JOptionPane optionPane = new JOptionPane(
                panel,
                JOptionPane.QUESTION_MESSAGE,
                JOptionPane.YES_NO_OPTION,
                null,
                new Object[]{},null
        );

        JDialog dialog = optionPane.createDialog(this, "Exit Game");
        dialog.getContentPane().setBackground(PANEL_COLOR);

        yesButton.addActionListener(e -> {
            dialog.dispose();
            stopCurrentThread();
            SoundManager.stopSound(bgMusic);  // Stop all music
            SoundManager.stopSound(endMusic);
            SoundManager.stopSound(heroMusic);
            System.exit(0);
        });

        noButton.addActionListener(e -> {
            dialog.dispose();

            // Kalau dari WIN dialog, play backsound lagi
            if (isWinDialogOpen) {
                isWinDialogOpen = false;  // Reset flag
                bgMusic = SoundManager.playSoundLoop("backsound.wav");
            }
        });

        noButton.addActionListener(e -> {
            dialog.dispose();  // Tutup dialog aja
        });

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.setBackground(PANEL_COLOR);
        buttonPanel.add(yesButton);
        buttonPanel.add(noButton);

        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setBackground(PANEL_COLOR);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        contentPanel.add(panel, BorderLayout.CENTER);
        contentPanel.add(buttonPanel, BorderLayout.SOUTH);

        dialog.setContentPane(contentPanel);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private JButton createGlossyOptionButton(String text, Color bgColor) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Monospaced", Font.BOLD, 16));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(100, 40));

        btn.setUI(new BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                AbstractButton button = (AbstractButton) c;
                int width = button.getWidth();
                int height = button.getHeight();

                Color currentColor = button.getModel().isRollover() ? bgColor.brighter() : bgColor;
                if (button.getModel().isPressed()) {
                    currentColor = bgColor.darker();
                }

                GradientPaint gradient = new GradientPaint(
                        0, 0, brighten(currentColor, 0.4f),
                        0, height, currentColor
                );
                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, width, height, 12, 12);

                GradientPaint highlight = new GradientPaint(
                        0, 0, new Color(255, 255, 255, 120),
                        0, height / 2, new Color(255, 255, 255, 0)
                );
                g2d.setPaint(highlight);
                g2d.fillRoundRect(0, 0, width, height / 2, 12, 12);

                g2d.setColor(new Color(0, 0, 0, 40));
                g2d.drawRoundRect(0, 0, width - 1, height - 1, 12, 12);
                super.paint(g, c);
            }
        });

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.repaint();
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.repaint();
            }
        });
        return btn;
    }

    private JPanel createCompactTerrainItem(String text, Color color) {
        JPanel item = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        item.setOpaque(false);

        JPanel colorBox = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(color);
                g.fillRect(0, 0, getWidth(), getHeight());

                Random rng = new Random(text.hashCode());
                if (color.equals(Terrain.STONE.color)) {
                    g.setColor(new Color(139, 90, 43));
                    for(int i = 0; i < 2; i++) {
                        g.drawRect(1, i * 9, 16, 9);
                    }
                } else if (color.equals(Terrain.GRASS.color)) {
                    g.setColor(new Color(43, 140, 33));
                    for(int i=0; i<3; i++) g.fillRect(rng.nextInt(14), rng.nextInt(14), 2, 3);
                } else if (color.equals(Terrain.SAND.color)) {
                    g.setColor(new Color(200, 150, 0));
                    for(int i=0; i<5; i++) g.fillRect(rng.nextInt(14), rng.nextInt(14), 1, 1);
                } else if (color.equals(Terrain.LAVA.color)) {
                    g.setColor(new Color(255, 140, 0));
                    for(int i=0; i<2; i++) g.fillRect(rng.nextInt(12), rng.nextInt(12), 3, 3);
                }
            }
        };

        colorBox.setPreferredSize(new Dimension(18, 18));
        colorBox.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        item.add(colorBox);

        JLabel label = new JLabel(text);
        label.setForeground(LABEL_TEXT_COLOR);
        label.setFont(LEGEND_FONT);
        item.add(label);
        return item;
    }

    private JButton createMarioButton(String text, Color bgColor, String emoji) {
        JButton btn = new JButton(emoji + " " + text);
        btn.setFont(new Font("Monospaced", Font.BOLD, 14));
        btn.setBackground(bgColor);
        btn.setForeground(BUTTON_TEXT_COLOR);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setOpaque(false);

        btn.setUI(new BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                AbstractButton button = (AbstractButton) c;
                int width = button.getWidth();
                int height = button.getHeight();

                Color currentColor = button.getModel().isRollover() ? bgColor.brighter() : bgColor;
                if (button.getModel().isPressed()) {
                    currentColor = bgColor.darker();
                }

                GradientPaint gradient = new GradientPaint(
                        0, 0, brighten(currentColor, 0.4f),  // Atas terang (pantulan cahaya)
                        0, height, currentColor  // Bawah normal
                );
                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, width, height, 12, 12);  // Rounded corners

                GradientPaint highlight = new GradientPaint(
                        0, 0, new Color(255, 255, 255, 100),  // Putih transparan
                        0, height / 2, new Color(255, 255, 255, 0)  // Fade ke transparan
                );
                g2d.setPaint(highlight);
                g2d.fillRoundRect(0, 0, width, height / 2, 12, 12);

                g2d.setColor(new Color(0, 0, 0, 30));
                g2d.drawRoundRect(0, 0, width - 1, height - 1, 12, 12);
                super.paint(g, c);
            }
        });

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.repaint();
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.repaint();
            }
        });
        return btn;
    }

    private Color brighten(Color color, float factor) {
        int r = Math.min(255, (int)(color.getRed() + (255 - color.getRed()) * factor));
        int g = Math.min(255, (int)(color.getGreen() + (255 - color.getGreen()) * factor));
        int b = Math.min(255, (int)(color.getBlue() + (255 - color.getBlue()) * factor));
        return new Color(r, g, b);
    }

    private void customizeOptionPane() {
        UIManager.put("OptionPane.background", PANEL_COLOR);
        UIManager.put("Panel.background", PANEL_COLOR);
        UIManager.put("OptionPane.messageFont", PIXEL_FONT);
        UIManager.put("OptionPane.buttonFont", PIXEL_FONT);
        UIManager.put("OptionPane.messageForeground", Color.WHITE);

        UIManager.put("Button.background", BUTTON_COLOR);
        UIManager.put("Button.foreground", BUTTON_TEXT_COLOR);
        UIManager.put("Button.font", PIXEL_FONT);
        UIManager.put("Button.border", BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.BLACK, 3),
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(Color.WHITE, 2),
                        BorderFactory.createEmptyBorder(8, 12, 8, 12)
                )
        ));
        UIManager.put("Button.select", BUTTON_COLOR);
        UIManager.put("Button.focus", new Color(0,0,0,0));
    }

    private void stopCurrentThread() {
        if (currentSolvingThread != null && currentSolvingThread.isAlive()) {
            stopCurrentSolving.set(true);
            currentSolvingThread.interrupt();
            try {
                currentSolvingThread.join(300);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void initializeMaze() {
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                maze[i][j] = new Cell(i, j);
            }
        }
    }

    private void generateMaze() {
        stopCurrentThread();
        resetMaze();
        new Thread(() -> {
            primAlgorithmWithMorePaths();
            assignTerrainTypes();
            graph = new WeightedGraph(maze, ROWS, COLS);
            start = maze[0][0];
            end = maze[ROWS-1][COLS-1];
            start.isStart = true;
            end.isEnd = true;
            SwingUtilities.invokeLater(() -> {
                mazePanel.setMaze(maze);
                mazePanel.setPlayerPosition(0, 0);
            });
        }).start();
    }

    private void assignTerrainTypes() {
        Random rand = new Random();
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                int r = rand.nextInt(100);
                if (r < 45) maze[i][j].terrain = Terrain.STONE;
                else if (r < 65) maze[i][j].terrain = Terrain.GRASS;
                else if (r < 82) maze[i][j].terrain = Terrain.SAND;
                else maze[i][j].terrain = Terrain.LAVA;
            }
        }
        maze[0][0].terrain = Terrain.STONE;
        maze[ROWS-1][COLS-1].terrain = Terrain.STONE;
    }

    private void primAlgorithmWithMorePaths() {
        Random rand = new Random();
        List<Wall> walls = new ArrayList<>();
        Set<Cell> visited = new HashSet<>();

        Cell current = maze[rand.nextInt(ROWS)][rand.nextInt(COLS)];
        visited.add(current);
        addWalls(current, walls);

        while (!walls.isEmpty()) {
            Wall wall = walls.remove(rand.nextInt(walls.size()));
            Cell cell1 = wall.cell1;
            Cell cell2 = wall.cell2;

            if (visited.contains(cell1) != visited.contains(cell2)) {
                removeWall(cell1, cell2);
                Cell unvisited = visited.contains(cell1) ? cell2 : cell1;
                visited.add(unvisited);
                addWalls(unvisited, walls);
            }
        }

        List<Wall> allWalls = new ArrayList<>();
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                Cell c = maze[i][j];
                if (c.rightWall && j < COLS - 1) allWalls.add(new Wall(c, maze[i][j+1]));
                if (c.bottomWall && i < ROWS - 1) allWalls.add(new Wall(c, maze[i+1][j]));
            }
        }
        int wallsToRemove = (int) (allWalls.size() * 0.3);
        for (int i = 0; i < wallsToRemove && !allWalls.isEmpty(); i++) {
            Wall wall = allWalls.remove(rand.nextInt(allWalls.size()));
            removeWall(wall.cell1, wall.cell2);
        }
    }

    private void addWalls(Cell cell, List<Wall> walls) {
        int[][] dirs = {{-1,0}, {1,0}, {0,-1}, {0,1}};
        for (int[] dir : dirs) {
            int nr = cell.row + dir[0];
            int nc = cell.col + dir[1];
            if (nr >= 0 && nr < ROWS && nc >= 0 && nc < COLS) {
                walls.add(new Wall(cell, maze[nr][nc]));
            }
        }
    }

    private void removeWall(Cell c1, Cell c2) {
        if (c1.row == c2.row) {
            if (c1.col < c2.col) { c1.rightWall = false; c2.leftWall = false; }
            else { c1.leftWall = false; c2.rightWall = false; }
        } else {
            if (c1.row < c2.row) { c1.bottomWall = false; c2.topWall = false; }
            else { c1.topWall = false; c2.bottomWall = false; }
        }
    }

    private void solveBFS() {
        if (graph == null) return;

        Queue<Cell> queue = new LinkedList<>();
        Map<Cell, Cell> parent = new HashMap<>();
        Set<Cell> visited = new HashSet<>();
        queue.offer(start);
        visited.add(start);

        while (!queue.isEmpty() && !stopCurrentSolving.get()) {
            Cell current = queue.poll();
            current.isVisited = true;
            SwingUtilities.invokeLater(() -> mazePanel.repaint());
            if (!sleepInterruptible(DELAY)) return;

            if (current == end) {
                tracePath(parent, end);
                return;
            }

            for (Cell neighbor : graph.getNeighbors(current)) {
                if (!visited.contains(neighbor) && !stopCurrentSolving.get()) {
                    visited.add(neighbor);
                    parent.put(neighbor, current);
                    queue.offer(neighbor);
                }
            }
        }
    }

    private void solveDFS() {
        if (graph == null) return;

        Stack<Cell> stack = new Stack<>();
        Map<Cell, Cell> parent = new HashMap<>();
        Set<Cell> visited = new HashSet<>();
        stack.push(start);
        visited.add(start);

        while (!stack.isEmpty() && !stopCurrentSolving.get()) {
            Cell current = stack.pop();
            current.isVisited = true;
            SwingUtilities.invokeLater(() -> mazePanel.repaint());
            if (!sleepInterruptible(DELAY)) return;

            if (current == end) {
                tracePath(parent, end);
                return;
            }

            for (Cell neighbor : graph.getNeighbors(current)) {
                if (!visited.contains(neighbor) && !stopCurrentSolving.get()) {
                    visited.add(neighbor);
                    parent.put(neighbor, current);
                    stack.push(neighbor);
                }
            }
        }
    }

    private void solveDijkstra() {
        if (graph == null) return;

        PriorityQueue<Node> pq = new PriorityQueue<>();
        Map<Cell, Integer> dist = new HashMap<>();
        Map<Cell, Cell> parent = new HashMap<>();
        Set<Cell> visited = new HashSet<>();

        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) dist.put(maze[i][j], Integer.MAX_VALUE);
        }

        dist.put(start, 0);
        pq.offer(new Node(start, 0));

        while (!pq.isEmpty() && !stopCurrentSolving.get()) {
            Node node = pq.poll();
            Cell current = node.cell;

            if (visited.contains(current)) continue;
            visited.add(current);
            current.isVisited = true;
            SwingUtilities.invokeLater(() -> mazePanel.repaint());
            if (!sleepInterruptible(DELAY)) return;

            if (current == end) {
                tracePath(parent, end);
                return;
            }

            for (Cell neighbor : graph.getNeighbors(current)) {
                if (!visited.contains(neighbor) && !stopCurrentSolving.get()) {
                    int newDist = dist.get(current) + neighbor.terrain.weight;
                    if (newDist < dist.get(neighbor)) {
                        dist.put(neighbor, newDist);
                        parent.put(neighbor, current);
                        pq.offer(new Node(neighbor, newDist));
                    }
                }
            }
        }
    }

    private void solveAStar() {
        if (graph == null) return;
        PriorityQueue<Node> pq = new PriorityQueue<>();
        Map<Cell, Integer> gScore = new HashMap<>();
        Map<Cell, Cell> parent = new HashMap<>();
        Set<Cell> visited = new HashSet<>();

        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) gScore.put(maze[i][j], Integer.MAX_VALUE);
        }

        gScore.put(start, 0);
        int fScore = heuristic(start, end);
        pq.offer(new Node(start, fScore));

        while (!pq.isEmpty() && !stopCurrentSolving.get()) {
            Node node = pq.poll();
            Cell current = node.cell;

            if (visited.contains(current)) continue;
            visited.add(current);
            current.isVisited = true;
            SwingUtilities.invokeLater(() -> mazePanel.repaint());
            if (!sleepInterruptible(DELAY)) return;

            if (current == end) {
                tracePath(parent, end);
                return;
            }

            for (Cell neighbor : graph.getNeighbors(current)) {
                if (!visited.contains(neighbor) && !stopCurrentSolving.get()) {
                    int tentativeG = gScore.get(current) + neighbor.terrain.weight;
                    if (tentativeG < gScore.get(neighbor)) {
                        gScore.put(neighbor, tentativeG);
                        parent.put(neighbor, current);
                        int f = tentativeG + heuristic(neighbor, end);
                        pq.offer(new Node(neighbor, f));
                    }
                }
            }
        }
    }

    private int heuristic(Cell a, Cell b) {
        return Math.abs(a.row - b.row) + Math.abs(a.col - b.col);
    }

    private void tracePath(Map<Cell, Cell> parent, Cell end) {
        if (stopCurrentSolving.get()) return;

        SoundManager.stopSound(bgMusic);
        SoundManager.playSound("show_time.wav");

        new Thread(() -> {
            try {
                Thread.sleep(1000);
                if (!stopCurrentSolving.get()) {
                    heroMusic = SoundManager.playSoundLoop("hero.wav");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();

        List<Cell> path = new ArrayList<>();
        int totalCost = 0;
        Cell current = end;

        while (current != null) {
            path.add(current);
            if (current != start) totalCost += current.terrain.weight;
            current = parent.get(current);
        }
        Collections.reverse(path);

        for (Cell cell : path) {
            if (stopCurrentSolving.get()) return;
            cell.isPath = true;
            SwingUtilities.invokeLater(() -> mazePanel.repaint());
            if (!sleepInterruptible(DELAY)) return;
        }

        for (Cell cell : path) {
            if (stopCurrentSolving.get()) return;
            final int row = cell.row;
            final int col = cell.col;
            SwingUtilities.invokeLater(() -> mazePanel.setPlayerPosition(row, col));
            if (!sleepInterruptible(WALK_DELAY)) return;
        }

        if (!stopCurrentSolving.get()) {
            final int cost = totalCost;
            SwingUtilities.invokeLater(() -> showPathCost(cost));
        }
    }

    private boolean sleepInterruptible(int ms) {
        try {
            Thread.sleep(ms);
            return !stopCurrentSolving.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    private void showPathCost(int cost) {
        isWinDialogOpen = true;

        SoundManager.stopSound(heroMusic);
        endMusic = SoundManager.playSoundLoop("end.wav");

        if (heroThread != null && heroThread.isAlive()) {
            heroThread.interrupt();
        }
        SoundManager.stopSound(heroMusic);

        JDialog dialog = new JDialog(this, "🍄 SUCCESS! 🍄", true);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(new Color(65, 105, 225));

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(65, 105, 225));
        panel.setBorder(BorderFactory.createEmptyBorder(25, 40, 20, 40));

        JLabel titleLabel = new JLabel("🏆 LEVEL COMPLETE! 🏆");
        titleLabel.setFont(new Font("Monospaced", Font.BOLD, 24));
        titleLabel.setForeground(new Color(255, 253, 150));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(15));

        JLabel costLabel = new JLabel("TOTAL COST: " + cost);
        costLabel.setFont(new Font("Monospaced", Font.BOLD, 32));
        costLabel.setForeground(Color.WHITE);
        costLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(costLabel);
        panel.add(Box.createVerticalStrut(12));

        JLabel messageLabel = new JLabel("⭐ CONGRATULATIONS! ⭐");
        messageLabel.setFont(new Font("Monospaced", Font.BOLD, 18));
        messageLabel.setForeground(new Color(50, 205, 50));
        messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(messageLabel);
        panel.add(Box.createVerticalStrut(18));

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setOpaque(false);

        JButton okButton = createWinButton("PLAY AGAIN", BUTTON_GREEN);
        okButton.addActionListener(e -> {
            SoundManager.stopSound(endMusic);  // ✅ STOP end.wav
            bgMusic = SoundManager.playSoundLoop("backsound.wav");  // ✅ PLAY backsound lagi
            isWinDialogOpen = false;
            dialog.dispose();  // Tutup dialog
        });
        buttonPanel.add(okButton);

        JButton exitButton = createWinButton("EXIT", BUTTON_RED);
        exitButton.addActionListener(e -> {
            SoundManager.stopSound(endMusic);  // ✅ STOP end.wav juga saat exit
            dialog.dispose();
            exitGame();
        });
        buttonPanel.add(exitButton);

        buttonPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(buttonPanel);
        dialog.add(panel);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);
        dialog.setVisible(true);

        dialog.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                // Cek apakah bgMusic masih null (berarti user belum klik PLAY AGAIN)
                if (bgMusic == null || !bgMusic.isRunning()) {
                    // Stop end music dan play backsound lagi
                    SoundManager.stopSound(endMusic);
                    bgMusic = SoundManager.playSoundLoop("backsound.wav");
                }
            }
        });

    }

    private JButton createWinButton(String text, Color bgColor) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Monospaced", Font.BOLD, 18));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(160, 45));
        btn.setMaximumSize(new Dimension(160, 45));
        btn.setMinimumSize(new Dimension(160, 45));

        btn.setUI(new BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                AbstractButton button = (AbstractButton) c;
                int width = button.getWidth();
                int height = button.getHeight();
                Color currentColor = button.getModel().isRollover() ? bgColor.brighter() : bgColor;
                if (button.getModel().isPressed()) {
                    currentColor = bgColor.darker();
                }

                GradientPaint gradient = new GradientPaint(
                        0, 0, brighten(currentColor, 0.4f),
                        0, height, currentColor
                );
                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, width, height, 15, 15);

                GradientPaint highlight = new GradientPaint(
                        0, 0, new Color(255, 255, 255, 120),
                        0, height / 2, new Color(255, 255, 255, 0)
                );
                g2d.setPaint(highlight);
                g2d.fillRoundRect(0, 0, width, height / 2, 15, 15);
                g2d.setColor(new Color(0, 0, 0, 40));
                g2d.drawRoundRect(0, 0, width - 1, height - 1, 15, 15);
                super.paint(g, c);
            }
        });

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.repaint();
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.repaint();
            }
        });
        return btn;
    }

    private void resetMaze() {
        stopCurrentThread();
        initializeMaze();
        graph = null;
        start = null;
        end = null;
        SwingUtilities.invokeLater(() -> {
            mazePanel.setMaze(maze);
            mazePanel.setPlayerPosition(0, 0);
        });
    }

    private void resetSolution() {
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                maze[i][j].isVisited = false;
                maze[i][j].isPath = false;
            }
        }
        mazePanel.repaint();
    }

    private void startNewSolving(Runnable solvingTask) {
        stopCurrentThread();
        SwingUtilities.invokeLater(() -> {
            resetSolution();
            if (start != null) {
                mazePanel.setPlayerPosition(start.row, start.col);
            }
        });

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        stopCurrentSolving.set(false);
        currentSolvingThread = new Thread(solvingTask);
        currentSolvingThread.start();
    }

    private static class MarioIcon implements Icon {
        public enum Type { COIN, STAR }
        private int width, height;
        private Type type;

        public MarioIcon(int w, int h, Type type) {
            this.width = w; this.height = h; this.type = type;
        }
        @Override public int getIconWidth() { return width; }
        @Override public int getIconHeight() { return height; }
        @Override

        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            int size = Math.min(width, height);
            if (type == Type.COIN) {
                g2d.setColor(new Color(248, 192, 0));
                g2d.fillOval(x, y, size, size);
                g2d.setColor(new Color(200, 150, 0));
                g2d.drawOval(x, y, size, size);
                g2d.drawOval(x + 2, y + 2, size - 4, size - 4);
                g2d.setColor(new Color(255, 220, 100));
                g2d.fillOval(x + size/4, y + size/4, size/4, size/4);
            } else if (type == Type.STAR) {
                g2d.setColor(new Color(248, 192, 0));
                int s = size/4;
                g2d.fillRect(x + s, y, s*2, s*4);
                g2d.fillRect(x, y + s, s*4, s*2);
                g2d.setColor(new Color(255, 255, 255));
                g2d.fillRect(x + s, y + s, s*2, s*2);
            }
        }
    }

}