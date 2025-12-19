import java.util.*;

public class WeightedGraph {
    private Map<Cell, List<Cell>> adjList;
    private int rows, cols;

    public WeightedGraph(Cell[][] maze, int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        adjList = new HashMap<>();
        buildGraph(maze);
    }

    private void buildGraph(Cell[][] maze) {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                Cell cell = maze[i][j];
                adjList.put(cell, new ArrayList<>());

                if (!cell.topWall && i > 0)
                    adjList.get(cell).add(maze[i-1][j]);
                if (!cell.rightWall && j < cols-1)
                    adjList.get(cell).add(maze[i][j+1]);
                if (!cell.bottomWall && i < rows-1)
                    adjList.get(cell).add(maze[i+1][j]);
                if (!cell.leftWall && j > 0)
                    adjList.get(cell).add(maze[i][j-1]);
            }
        }
    }
    public List<Cell> getNeighbors(Cell cell) {
        return adjList.getOrDefault(cell, new ArrayList<>());
    }
}