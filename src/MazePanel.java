import java.awt.*;
import java.util.Random;
import javax.swing.*;

public class MazePanel extends JPanel {
    private Cell[][] maze;
    private int rows, cols, cellSize;
    private int playerRow = 0;
    private int playerCol = 0;

    private static final Color VISITED_COLOR = new Color(9, 8, 8, 99);  // White transparent
    private static final Color PATH_COLOR = new Color(255, 255, 255);           // Lime Green (#32CD32)

    public MazePanel(int rows, int cols, int cellSize) {
        this.rows = rows;
        this.cols = cols;
        this.cellSize = cellSize;
        this.setPreferredSize(new Dimension(cols * cellSize, rows * cellSize));
        this.setBackground(new Color(71, 56, 40));
    }

    public void setMaze(Cell[][] maze) {
        this.maze = maze;
        this.playerRow = 0;
        this.playerCol = 0;
        repaint();
    }

    public void setPlayerPosition(int row, int col) {
        this.playerRow = row;
        this.playerCol = col;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (maze == null) return;

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                drawCell(g2d, maze[i][j]);
            }
        }
    }

    private void drawTerrainTexture(Graphics2D g, Cell cell, int x, int y) {
        Color lighter = cell.terrain.color.brighter();
        Color darker = cell.terrain.color.darker();
        Random rand = new Random(cell.row * 1000 + cell.col);
        for (int i = 0; i < 3; i++) {
            int px = x + rand.nextInt(cellSize - 4) + 2;
            int py = y + rand.nextInt(cellSize - 4) + 2;
            g.setColor(rand.nextBoolean() ? lighter : darker);
            g.fillRect(px, py, 2, 2);
        }
    }

    private void drawPixelPlayer(Graphics2D g, int x, int y) {
        int offset = 4;
        int size = cellSize - 8;
        int unit = size / 8;

        g.setColor(new Color(231, 32, 32)); // Red
        g.fillRect(x + offset + unit*2, y + offset, unit*4, unit); // Top of hat
        g.fillRect(x + offset + unit, y + offset + unit, unit*6, unit); // Brim of hat

        g.setColor(new Color(255, 224, 189));
        g.fillRect(x + offset + unit*2, y + offset + unit*2, unit*4, unit*3); // Face

        g.setColor(new Color(101, 67, 33));
        g.fillRect(x + offset + unit, y + offset + unit*2, unit, unit); // Left hair
        g.fillRect(x + offset + unit*6, y + offset + unit*2, unit, unit); // Right hair

        g.setColor(Color.BLACK);
        g.fillRect(x + offset + unit*2, y + offset + unit*3, unit, unit); // Left eye
        g.fillRect(x + offset + unit*5, y + offset + unit*3, unit, unit); // Right eye

        g.setColor(new Color(101, 67, 33));
        g.fillRect(x + offset + unit*2, y + offset + unit*4, unit*4, unit); // Mustache

        g.setColor(new Color(65, 105, 225));
        g.fillRect(x + offset + unit*2, y + offset + unit*5, unit*4, unit); // Shirt

        g.setColor(new Color(30, 60, 180));
        g.fillRect(x + offset + unit*2, y + offset + unit*6, unit*4, unit*2); // Overalls

        g.setColor(new Color(255, 253, 150));
        g.fillRect(x + offset + unit*3, y + offset + unit*6, unit, unit); // Left button
        g.fillRect(x + offset + unit*4, y + offset + unit*6, unit, unit); // Right button

        g.setColor(new Color(255, 224, 189));
        g.fillRect(x + offset + unit, y + offset + unit*5, unit, unit*2); // Left arm
        g.fillRect(x + offset + unit*6, y + offset + unit*5, unit, unit*2); // Right arm

        g.setColor(Color.WHITE);
        g.fillRect(x + offset + unit, y + offset + unit*7, unit, unit); // Left glove
        g.fillRect(x + offset + unit*6, y + offset + unit*7, unit, unit); // Right glove

        g.setColor(Color.WHITE);
        g.fillRect(x + offset + unit*3, y + offset + unit/2, unit, unit/2);
        g.fillRect(x + offset + unit*4, y + offset + unit/2, unit, unit/2);
    }


    private void drawCell(Graphics2D g, Cell cell) {
        int x = cell.col * cellSize;
        int y = cell.row * cellSize;

        g.setColor(cell.terrain.color);
        g.fillRect(x + 1, y + 1, cellSize - 2, cellSize - 2);
        drawTerrainTexture(g, cell, x, y);

        if (cell.isPath) {
            g.setColor(PATH_COLOR);
            g.fillRect(x + 1, y + 1, cellSize - 2, cellSize - 2);
            g.setColor(PATH_COLOR.darker());
            g.drawRect(x + 1, y + 1, cellSize - 3, cellSize - 3);
        } else if (cell.isVisited) {
            g.setColor(VISITED_COLOR);
            g.fillRect(x + 1, y + 1, cellSize - 2, cellSize - 2);
        }

        g.setColor(new Color(80, 80, 80));
        int wallSize = 3;
        if (cell.topWall) g.fillRect(x, y, cellSize, wallSize);
        if (cell.rightWall) g.fillRect(x + cellSize - wallSize, y, wallSize, cellSize);
        if (cell.bottomWall) g.fillRect(x, y + cellSize - wallSize, cellSize, wallSize);
        if (cell.leftWall) g.fillRect(x, y, wallSize, cellSize);

        if (cell.row == playerRow && cell.col == playerCol) {
            drawPixelPlayer(g, x, y);
        }

        if (cell.isEnd) drawTrophy(g, x, y);
    }

    private void drawTrophy(Graphics2D g, int x, int y) {
        int cx = x + cellSize/2;
        int cy = y + cellSize/2;
        int size = cellSize / 3;

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g.setColor(new Color(255, 215, 0)); // Bright yellow
        g.fillRect(cx - size/2 - 4, cy + size + 3, size + 8, 2);
        g.fillRect(cx - size/2 - 2, cy + size + 1, size + 4, 2);
        g.fillRect(cx - size/2, cy + size - 1, size, 2);

        g.setColor(new Color(255, 215, 0)); // Bright yellow
        g.fillRect(cx - 3, cy + size/2, 6, size/2);

        g.setColor(new Color(255, 200, 0));
        g.fillRect(cx - 3, cy + size/2, 2, size/2);

        g.setColor(new Color(255, 215, 0)); // Bright yellow
        int[] cupX = {cx - size/2 - 1, cx + size/2 + 1, cx + size/3, cx - size/3};
        int[] cupY = {cy - size/2, cy - size/2, cy + size/2, cy + size/2};
        g.fillPolygon(cupX, cupY, 4);

        g.setColor(new Color(255, 200, 0));
        int[] leftX = {cx - size/2 - 1, cx - size/3, cx - size/3, cx - size/2 - 1};
        int[] leftY = {cy - size/2, cy + size/2, cy + size/2 - 1, cy - size/2 + 1};
        g.fillPolygon(leftX, leftY, 4);

        g.setColor(new Color(255, 235, 50)); // Very bright yellow
        int[] rightX = {cx + size/3, cx + size/2 + 1, cx + size/2, cx + size/3 - 1};
        int[] rightY = {cy + size/2, cy - size/2, cy - size/2 + 1, cy + size/2 - 1};
        g.fillPolygon(rightX, rightY, 4);

        g.setColor(new Color(255, 235, 50)); // Very bright yellow
        g.fillRect(cx - size/2 - 1, cy - size/2 - 2, size + 2, 2);
        g.setColor(new Color(255, 200, 0));
        g.fillRect(cx - size/2 - 1, cy - size/2, size + 2, 1);

        g.setColor(new Color(255, 200, 0));
        g.setStroke(new BasicStroke(2.5f));
        g.drawArc(cx - size/2 - 5, cy - size/4, 6, size/2, 90, 180);
        g.drawArc(cx + size/2 - 1, cy - size/4, 6, size/2, -90, 180);

        g.setColor(new Color(255, 215, 0));
        g.setStroke(new BasicStroke(1.5f));
        g.drawArc(cx - size/2 - 5, cy - size/4, 6, size/2, 120, 60);
        g.drawArc(cx + size/2 - 1, cy - size/4, 6, size/2, 0, 60);
        g.setStroke(new BasicStroke(1));

        g.setColor(new Color(255, 255, 100, 200)); // Bright yellow shine
        int[] hlX = {cx - size/4, cx - size/5, cx - size/6, cx - size/5};
        int[] hlY = {cy - size/3, cy - size/6, cy + size/6, cy + size/3};
        g.fillPolygon(hlX, hlY, 4);

        g.setColor(new Color(255, 255, 150, 220));
        g.fillRect(cx - size/4 + 1, cy - size/4, 1, size/3);

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
    }
}
