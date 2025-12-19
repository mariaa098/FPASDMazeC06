import java.awt.Color;

public class Terrain {
    public final String name;
    public final int weight;
    public final Color color;

    public Terrain(String name, int weight, Color color) {
        this.name = name;
        this.weight = weight;
        this.color = color;
    }

    public static final Terrain STONE =
            new Terrain("BRICK", 0, new Color(194, 166, 145));  // Soft beige/tan
    public static final Terrain GRASS =
            new Terrain("GRASS", 1, new Color(144, 238, 144));  // Soft sage green
    public static final Terrain SAND =
            new Terrain("SAND", 5, new Color(230, 198, 124));   // Soft butter yellow
    public static final Terrain LAVA =
            new Terrain("LAVA", 10, new Color(255, 165, 0));  // Soft coral/rose
}
