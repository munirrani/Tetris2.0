package fsktm.fop;
import java.util.ArrayList;
import java.util.Random;

public class Shape {

    protected enum Tetrominoe { NoShape, ZShape, SShape, LineShape,
        TShape, SquareShape, LShape, MirroredLShape }

    private Tetrominoe pieceShape;
    private int coords[][];
    private int[][][] coordsTable;
    private int numbers[] = new int[4];
    private static Random random = new Random();

    public Shape() {

        coords = new int[4][2];

        coordsTable = new int[][][] {
                { { 0, 0 },   { 0, 0 },   { 0, 0 },   { 0, 0 } }, // No Shape
                { { 0, -1 },  { 0, 0 },   { -1, 0 },  { -1, 1 } }, // Z Shape
                { { 0, -1 },  { 0, 0 },   { 1, 0 },   { 1, 1 } }, // S Shape
                { { 0, -1 },  { 0, 0 },   { 0, 1 },   { 0, 2 } }, // Line Shape
                { { -1, 0 },  { 0, 0 },   { 1, 0 },   { 0, 1 } }, // T Shape
                { { 0, 0 },   { 1, 0 },   { 0, 1 },   { 1, 1 } }, // Square Shape
                { { -1, -1 }, { 0, -1 },  { 0, 0 },   { 0, 1 } }, // L Shape
                { { 1, -1 },  { 0, -1 },  { 0, 0 },   { 0, 1 } } // Mirrored L Shape
        };

        setShape(Tetrominoe.NoShape);
    }

    protected void setShape(Tetrominoe shape) {

        for (int i = 0; i < 4 ; i++) {

            for (int j = 0; j < 2; ++j) {

                coords[i][j] = coordsTable[shape.ordinal()][i][j];
            }
        }

        pieceShape = shape;
    }

    private void setX(int index, int x) {
        coords[index][0] = x;
    }

    private void setY(int index, int y) {
        coords[index][1] = y;
    }

    public int x(int index) {
        return coords[index][0];
    }

    public int y(int index) {
        return coords[index][1];
    }

    public Tetrominoe getShape()  {
        return pieceShape;
    }

    public void setRandomShape() {
        var r = new Random();
        int x = Math.abs(r.nextInt()) % 7 + 1;
        Tetrominoe[] values = Tetrominoe.values();
        setShape(values[x]);
    }

    public int minX() {

        int m = coords[0][0];

        for (int i=0; i < 4; i++) {

            m = Math.min(m, coords[i][0]);
        }

        return m;
    }

    public int minY() {

        int m = coords[0][1];

        for (int i=0; i < 4; i++) {

            m = Math.min(m, coords[i][1]);
        }

        return m;
    }

    public Shape rotateLeft() {

        if (pieceShape == Tetrominoe.SquareShape) {
            return this;
        }
        var result = new Shape();
        result.pieceShape = pieceShape;

        for (int i = 0; i < 4; ++i) {
            result.setX(i, y(i));
            result.setY(i, -x(i));
        }

        return result;
    }

    public Shape rotateRight() {

        if (pieceShape == Tetrominoe.SquareShape) {
            return this;
        }

        var result = new Shape();
        result.pieceShape = pieceShape;

        for (int i = 0; i < 4; ++i) {
            result.setX(i, -y(i));
            result.setY(i, x(i));
        }

        return result;
    }

    /*
    TODO - Avoid using ArrayList
     */

    // private int numbers[] = new int[4];

    public void setRandomNumber() {
        ArrayList<Integer> numberList = new ArrayList<Integer>(4);
        int randomNumber, count = 0;
        while (count != 4) {
            randomNumber = random.nextInt(10);
            if (!numberList.contains(randomNumber)) {
                numberList.add(randomNumber);
                count++;
            }
        }
        for (int i = 0; i < 4; i++) {
            numbers[i] = numberList.get(i);
        }
    }

    public int getNumberAt(int index) {
        return numbers[index];
    }

    public void setNumber(int index, int value) {
        numbers[index] = value;
    }
}
