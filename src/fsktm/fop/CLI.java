package fsktm.fop;

import fsktm.fop.Shape.Tetrominoe;

import javax.print.DocFlavor;
import java.util.ArrayList;
import java.util.Scanner;

public class CLI {

    /*
    TODO
    1 - Make the CLI version work with Shape class
    2 - Introduce Block preview
    3 - Once the core game functions are fully working, then make the GUI
     */
    int width = 10;
    int height = 10;
    int previewWidth = width;
    int previewHeight = 4;
    private Tetrominoe[] board = new Tetrominoe[height * width];
    private int[] numbers = new int [width * height];
    private Tetrominoe[] previewBoard = new Tetrominoe[previewHeight * previewWidth];
    private int[] previewNumbers = new int[previewHeight * previewWidth];
    private static ArrayList<Shape> previewShape = new ArrayList<Shape>(4);

    private static Shape currentShape, shape1;
    private int currentX, currentY;
    private boolean isFull = false;

    CLI() {
        initBoard();
        initPreviewBoard();

        /*
        TODO ArrayList of Shape class
         */
        currentX = width / 2 - 1;
        currentY =  height / 2 - 1;
        putShadowShapeOnBoard(currentX ,currentY, currentShape);

        printBoard();
        printBlockPreviews();

        Scanner scan = new Scanner(System.in);
        String input;
        while (true) {
            System.out.print("a [<-] d [->] w [↑] s [↓] r [ROTATE] i [INSERT] e [EXIT]: ");
            input = scan.nextLine();
            currentShape = previewShape.get(0);;
            if (input.equals("a")) {
                if (tryMove(currentX - 1, currentY, currentShape)) {
                    move(currentX - 1, currentY, currentShape);
                    currentX--;
                }
            } else if (input.equals("d")) {
                if (tryMove(currentX + 1, currentY, currentShape)) {
                    move(currentX + 1, currentY, currentShape);
                    currentX++;
                }
            } else if (input.equals("w")) {
                if (tryMove(currentX, currentY - 1, currentShape)) {
                    move(currentX, currentY - 1, currentShape);
                    currentY--;
                }
            } else if (input.equals("s")) {
                if (tryMove(currentX , currentY + 1, currentShape)) {
                    move(currentX, currentY + 1, currentShape);
                    currentY++;
                }
            } else if (input.equals("r")) {
                if (canRotate()) {
                    removeShadowShapeOnBoard(currentX, currentY, currentShape);
                    currentShape = currentShape.rotateRight();
                    previewShape.set(0, currentShape);
                    retainNumber();
                    putShadowShapeOnBoard(currentX, currentY, currentShape);
                }
            } else if (input.equals("i")) {
                insert();
            } else if (input.equals("e")) {
                break;
            }
            if (isFull) { // Game over
                System.out.println("GAME OVER!");
                break;
            }
            checkForColumnAndRow();
            printBoard();
            printBlockPreviews();
        }
    }

    private void retainNumber() {
        for (int i = 0; i < 4; i++) {
            currentShape.setNumber(i, shape1.getNumberAt(i));
        }
    }
    private void check() {
        for (int i = 0;i < 4; i++) {
            System.out.printf("%d ", previewShape.get(0).getNumberAt(i));
        }
        System.out.println();
    }

    private void checkForColumnAndRow() {
        /*
        TODO - What if both rows and columns are simultaneously even?
        boolean rowEven = false, columnEven = false;
        */
        for (int i = 0; i < width; i++) {
            if (sumIsEvenForRow(i)) {
                clearRow(i);
                System.out.println("Row " + (i+1) + " cleared!");
            }
        }
        for (int i = 0; i < height; i++) {
            if (sumIsEvenForColumn(i)) {
                clearColumn(i);
                System.out.println("Column " + (i+1) + " cleared!");
            }
        }
    }

    private boolean canRotate() {
        for (int i = 0; i < 4; i++) {
            int newX = currentX + currentShape.x(i);
            int newY = currentY + currentShape.y(i);
            if (!tryMove(newX, newY, currentShape)) {
                return false;
            }
        }
        return true;
    }
    private void initBoard() {
        for (int i = 0; i < width * height; i++) {
            board[i] = Tetrominoe.NoShape;
            numbers[i] = -1; // Use -1 to indicate its empty
        }
    }

    private void initPreviewBoard() {
        clearPreviewBoard();
        for (int i = 0; i < 3; i++) {
            previewShape.add(generateRandomShape());
        }
        updatePreviewBoard(); // <- Redundant (TODO)
        shape1 = previewShape.get(0);
        currentShape = previewShape.get(0);
    }

    private void clearPreviewBoard() {
        for (int i = 0; i < previewWidth * previewHeight; i++) {
            previewBoard[i] = Tetrominoe.NoShape;
            previewNumbers[i] = -1;
        }
    }

    private void updatePreviewBoard() {
        clearPreviewBoard();
        putShapeOnPreviewBoard(1,1,previewShape.get(2));
        putShapeOnPreviewBoard(4,1,previewShape.get(1));
        putShapeOnPreviewBoard(8,1,previewShape.get(0));
    }

    private void insert() {
        putShapeOnBoard(currentX, currentY, currentShape);
        previewShape.remove(0);
        previewShape.add(generateRandomShape());
        currentShape = previewShape.get(0);
        shape1 = previewShape.get(0);

        if (blocksIsAvailable()) {
            putShadowShapeOnBoard(currentX, currentY, currentShape);
        }
        updatePreviewBoard();
    }

    // TODO -
    private boolean blocksIsAvailable() {
        int x, y;
        for (int i = 0; i < width * height; i++) {
            if (numbers[i] >= 0) continue;
            x = i % width;
            y = (i - x) / 10;
            if (tryMove(x, y, currentShape)) {
                currentX = x;
                currentY = y;
                return true;
            }
        }
        isFull = true;
        return false;
    }
    /*
    TODO - Total count of vertical and horizontal columns
     */
    private boolean sumIsEvenForRow(int row) {
        int sumHorizontal = 0;
        int count = 0;
        for (int j = 0; j < width; j++) {
            if (board[(row * width) + j] != Tetrominoe.NoShape && numbers[(row * width) + j] >= 0) {
                sumHorizontal += numbers[(row * width) + j];
                count++;
            }
        }
        if ((sumHorizontal % 2 == 0) && (count == width)) {
            return true;
        }
        return false;
    }
    private boolean sumIsEvenForColumn(int column) {
        int sumVertical = 0;
        int count = 0;
        for (int j = 0; j < width; j++) {
            if (board[(j * height) + column] != Tetrominoe.NoShape && numbers[(j * height) + column] >= 0) {
                sumVertical += numbers[(j * height) + column];
                count++;
            }
        }
        if (sumVertical % 2 == 0 && count == height) {
            return true;
        }
        return false;
    }

    private void clearRow(int index) {
        for (int i = 0; i < width; i++) {
            board[(index * width) + i] = Tetrominoe.NoShape;
            numbers[(index * width) + i] = -1;
        }
    }

    private void clearColumn(int index) {
        for (int i = 0; i < height; i++) {
            board[(i * height) + index] = Tetrominoe.NoShape;
            numbers[(i * height) + index] = -1;
        }
    }

    private void putShapeOnBoard(int a, int b, Shape shape) {
        for (int i = 0; i < 4; i++) {
            int x = a + shape.x(i);
            int y = b + shape.y(i);
            board[(y * width) + x] = shape.getShape();
            numbers[(y * width) + x] = shape.getNumberAt(i);
        }
    }

    private void putShadowShapeOnBoard(int a, int b, Shape newShape) {
        for (int i = 0; i < 4; i++) {
            int x = a + newShape.x(i);
            int y = b + newShape.y(i);
            board[(y * width) + x] = newShape.getShape();
            numbers[(y * width) + x] = -2; //-2 as a value for shadow
        }
    }

    private void removeShadowShapeOnBoard(int a, int b, Shape oldShape) {
        for (int i = 0; i < 4; i++) {
            int x = a + oldShape.x(i);
            int y = b + oldShape.y(i);
            board[(y * width) + x] = Tetrominoe.NoShape;
            numbers[(y * width) + x] = -1; // -1 to indicate its nothing
        }
    }

    /*
    TODO - Tembus
     */
    private boolean tryMove(int newX, int newY, Shape shape) {
        for (int i = 0; i < 4; i++) {
            int x = newX + shape.x(i);
            int y = newY + shape.y(i);
            if (x < 0 || x >= width || y < 0 || y >= height) {
                return false;
            }

            if (shapeAt(x, y) != Tetrominoe.NoShape && numberAt(x, y) >= 0) {
                return false;
            }
        }
        currentShape = shape;
//        currentX = newX;
//        currentY = newY;
        return true;
    }

    private void move(int newX, int newY, Shape shape) {
        /*
        Replace board and numbers from old to new
         */
        Tetrominoe tetrominoe = shape.getShape();
        for (int i = 0; i < 4; i++) {
            int oldX = currentX + shape.x(i);
            int oldY = currentY + shape.y(i);
            board[(oldY * width) + oldX] = Tetrominoe.NoShape;
            numbers[(oldY * width) + oldX] = -2; //can only move shadow
        }
        for (int i = 0; i < 4; i++) {
            int x = newX + shape.x(i);
            int y = newY + shape.y(i);
            board[(y * width) + x] = tetrominoe;
            numbers[(y * width) + x] = -2; //can only move shadow
        }
    }

    private Tetrominoe shapeAt(int x, int y) {
        return board[(y * width) + x];
    }

    private int numberAt(int x, int y) {
        return numbers[(y * width) + x];
    }

    private Shape generateRandomShape() {
        var shape = new Shape();
        shape.setRandomShape();
        shape.setRandomNumber();
        return shape;
    }

    private void printBoard() {
        for(int a = 0; a < width + 2; a++) System.out.printf("/  ");
        System.out.println();
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                if (j == 0) System.out.printf("/ "); //border stuff
                if (board[(i * width) + j] != Tetrominoe.NoShape && numbers[(i * width) + j] >= 0) {
                    System.out.printf(" %d ", numbers[(i * width) + j]); //The number
                } else if (board[(i * width) + j] != Tetrominoe.NoShape && numbers[(i * width) + j] == -2) {
                    System.out.printf(" + "); // Shadow
                } else {
                    System.out.printf("   ");
                }
                if (j == width - 1) System.out.printf(" /"); //border stuff
            }
            System.out.println();
        }
        for(int a = 0; a < width + 2; a++) System.out.printf("/  ");
        System.out.println();
    }

    private void putShapeOnPreviewBoard(int a, int b, Shape newShape) {
        for (int i = 0; i < 4; i++) {
            int x = a + newShape.x(i);
            int y = b + newShape.y(i);
            previewBoard[(y * previewWidth) + x] = newShape.getShape();
            previewNumbers[(y * previewWidth) + x] = newShape.getNumberAt(i);
        }
    }

    private void printBlockPreviews() {
        for (int i = 0; i < previewHeight; i++) { // for the height of 4
            for (int j = 0; j < previewWidth; j++) { // for the width of 10
                if ((previewBoard[(i * previewWidth) + j] != Tetrominoe.NoShape) && (previewNumbers[(i * previewWidth) + j] != -1)) {
                    System.out.printf(" %d ", previewNumbers[(i * previewWidth) + j]);
                } else {
                    System.out.printf("   ");
                }
            }
            System.out.println();
        }
        System.out.println();
    }
}