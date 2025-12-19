public class Node implements Comparable<Node> {
    public Cell cell;
    public int priority;

    public Node(Cell cell, int priority) {
        this.cell = cell;
        this.priority = priority;
    }

    @Override
    public int compareTo(Node other) {
        return Integer.compare(this.priority, other.priority);
    }
}