import java.util.Objects;

public class Cell {
    public int row, col;
    public boolean topWall=true,rightWall=true,bottomWall=true,leftWall=true;
    public boolean isVisited=false,isPath=false,isStart=false,isEnd=false;
    public Terrain terrain = Terrain.STONE;

    public Cell(int r,int c){
        row=r; col=c;
    }

    @Override
    public boolean equals(Object o){
        if(this==o) return true;
        if(!(o instanceof Cell)) return false;
        Cell c=(Cell)o;
        return row==c.row && col==c.col;
    }

    @Override
    public int hashCode(){
        return Objects.hash(row,col);
    }
}
