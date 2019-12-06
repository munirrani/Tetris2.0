package fsktm.fop;

import fsktm.fop.Shape.Tetrominoe;

import java.util.ArrayList;
import java.util.Scanner;

public class CLI {

    /*
    TODO
    1 - Hold mode
    2 - Bug: when rotating preview line shape, it goes out of border
    3 - Bug: preview is skewed when the game is done (fixed is after tembus & hold feature is done)
    4 - Once the core game functions are fully working, then make the GUI
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
    private int[] currentNumbers = new int[4];

    private static Shape currentShape, shape1;
    private int currentX, currentY;
    private boolean isFull = false;

    CLI() {
        initBoard();
        initPreviewBoard();

        currentX = width / 2 - 1; // start from the middle
        currentY =  height / 2 - 1;
        putShadowShapeOnBoard(currentX ,currentY, currentShape, currentShape.getShape(), -2);

        printBoard();
        printBlockPreviews();

        Scanner scan = new Scanner(System.in);
        String input;
        int testX, testY;
        while (true) {
            System.out.print("a [<-] d [->] w [↑] s [↓] r [ROTATE] i [INSERT] e [EXIT]: ");
            input = scan.nextLine();
            currentShape = previewShape.get(0);
             /*
              TODO - Tembus
             */
            if (input.equals("a")) {
                testX = currentX - 1;
                testY = currentY;
                while (true) {
                    if (testX < 0) break;
                    if (tryMove(testX, testY, currentShape)) {
                        move(testX, testY, currentShape);
                        currentX = testX;
                        break;
                    } else {
                        testX--;
                    }
                }
            } else if (input.equals("d")) {
                testX = currentX + 1;
                testY = currentY;
                while (true) {
                    if (testX > width - 1) break;
                    if (tryMove(testX, testY, currentShape)) {
                        move(testX, testY, currentShape);
                        currentX = testX;
                        break;
                    } else {
                        testX++;
                    }
                }
            } else if (input.equals("w")) {
                testX = currentX;
                testY = currentY - 1;
                while (true) {
                    if (testY < 0) break;
                    if (tryMove(testX, testY, currentShape)) {
                        move(testX, testY, currentShape);
                        currentY = testY;
                        break;
                    } else {
                        testY--;
                    }
                }
            } else if (input.equals("s")) {
                testX = currentX;
                testY = currentY + 1;
                while (true) {
                    if (testY > height - 1) break;
                    if (tryMove(testX, testY, currentShape)) {
                        move(testX, testY, currentShape);
                        currentY = testY;
                        break;
                    } else {
                        testY++;
                    }
                }
            } else if (input.equals("r")) {
                if (canRotate()) {
                    putShadowShapeOnBoard(currentX, currentY, currentShape, Tetrominoe.NoShape, -1); // remove
                    rotate();
                    putShadowShapeOnBoard(currentX, currentY, currentShape, currentShape.getShape(), -2); // add
                }
            } else if (input.equals("i")) {
                insert();
            } else if (input.equals("e")) {
                break;
            }
            updatePreviewBoard();
            printBoard();
            printBlockPreviews();
            if (isFull) { // Game over
                System.out.println("GAME OVER!");
                break;
            }
        }
    }

    private void retainNumber() {
        for (int i = 0; i < 4; i++) {
            currentShape.setNumber(i, shape1.getNumberAt(i));
        }
    }

    private void checkForColumnAndRow() {
        /*
        TODO - What if both rows and columns are simultaneously even?
        boolean rowEven = false, columnEven = false;
        */
        for (int i = 0; i < width; i++) {
            if (sumEvenForRow(i)) {
                clearRow(i);
                System.out.println("Row " + (i+1) + " cleared!");
            }
        }
        for (int i = 0; i < height; i++) {
            if (sumEvenForColumn(i)) {
                clearColumn(i);
                System.out.println("Column " + (i+1) + " cleared!");
            }
        }
    }

    private boolean canRotate() {
        int count = 0;
        currentShape = currentShape.rotateRight();
        for (int i = 0; i < 4; i++) {
            int newX = currentX + currentShape.x(i);
            int newY = currentY + currentShape.y(i);
            if (tryMove(newX, newY, currentShape)) {
                count++;
            }
        }
        currentShape = currentShape.rotateLeft();
        if (count == 4) return true;
        return false;
    }

    private void rotate() {
        currentShape = currentShape.rotateRight();
        previewShape.set(0, currentShape);
        retainNumber();
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
        currentShape = previewShape.get(0);
        shape1 = currentShape;
    }

    private Tetrominoe shapeAt(int x, int y) {
        return board[(y * width) + x];
    }

    private int numberAt(int x, int y) {
        return numbers[(y * width) + x];
    }

    private void setShapeAt(int x, int y, Tetrominoe tetrominoe) {
        board[(y * width) + x] = tetrominoe;
    }
    private void setNumberAt(int x, int y, int value) {
        numbers[(y * width) + x] = value;
    }

    private Tetrominoe previewShapeAt(int x, int y) {
        return previewBoard[(y * width) + x];
    }

    private int previewNumberAt(int x, int y) {
        return previewNumbers[(y * width) + x];
    }

    private void setPreviewShapeAt(int x, int y, Tetrominoe tetrominoe) {
        previewBoard[(y * previewWidth) + x] = tetrominoe;
    }
    private void setPreviewNumberAt(int x, int y, int value) {
        previewNumbers[(y * previewWidth) + x] = value;
    }

    private Shape generateRandomShape() {
        var shape = new Shape();
        shape.setRandomShape();
        shape.setRandomNumber();
        return shape;
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
        checkForColumnAndRow();

        if (blocksIsAvailable()) {
            putShadowShapeOnBoard(currentX, currentY, currentShape, currentShape.getShape(), -2);
        } else {
            isFull = true;
        }
        shape1 = currentShape;
    }

    private boolean blocksIsAvailable() {
        int x, y;
        for (int i = 0; i < 3; i++) {
            for (int k = 0; k < width * height; k++) {
                if (numbers[k] >= 0) continue;
                x = k % width;
                y = (k - x) / 10;
                if (tryMove(x, y, currentShape)) {
                    currentX = x;
                    currentY = y;
                    return true;
                }
            }
            rotate();
        }
        return false;
    }
    /*
    TODO - Total count of vertical and horizontal columns
     */
    private boolean sumEvenForRow(int row) {
        int sum = 0;
        int count = 0;
        for (int j = 0; j < width; j++) {
            if (shapeAt(j, row) != Tetrominoe.NoShape && numberAt(j, row) >= 0) {
                sum += numberAt(j, row);
                count++;
            }
        }
        if ((sum % 2 == 0) && (count == width)) {
            return true;
        }
        return false;
    }

    private boolean sumEvenForColumn(int column) {
        int sum = 0;
        int count = 0;
        for (int j = 0; j < height; j++) {
            if (board[(j * height) + column] != Tetrominoe.NoShape && numbers[(j * height) + column] >= 0) {
                sum += numbers[(j * height) + column];
                count++;
            }
        }
        if (sum % 2 == 0 && count == height) {
            return true;
        }
        return false;
    }


    private void clearRow(int index) {
        for (int i = 0; i < width; i++) {
            setShapeAt(i, index, Tetrominoe.NoShape);
            setNumberAt(i, index, -1);
        }
    }

    private void clearColumn(int index) {
        for (int i = 0; i < height; i++) {
            setShapeAt(index, i, Tetrominoe.NoShape);
            setNumberAt(index, i, -1);
        }
    }

    private void putShapeOnBoard(int a, int b, Shape shape) {
        for (int i = 0; i < 4; i++) {
            int x = a + shape.x(i);
            int y = b + shape.y(i);
            setShapeAt(x, y, shape.getShape());
            setNumberAt(x, y, shape.getNumberAt(i));
        }
    }

    private void putShadowShapeOnBoard(int a, int b, Shape newShape, Tetrominoe tetrominoe, int value) {
        for (int i = 0; i < 4; i++) {
            int x = a + newShape.x(i);
            int y = b + newShape.y(i);
            setShapeAt(x, y, tetrominoe);
            setNumberAt(x, y, value); //-1 to indicate its nothing, -2 as a value for shadow
        }
    }

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
            setShapeAt(oldX, oldY, Tetrominoe.NoShape);
            setNumberAt(oldX, oldY, -2);
        }
        for (int i = 0; i < 4; i++) {
            int x = newX + shape.x(i);
            int y = newY + shape.y(i);
            setShapeAt(x, y, tetrominoe);
            setNumberAt(x, y, -2); //can only move shadow
        }
    }

    private void printBoard() {
        for(int a = 0; a < width + 2; a++) System.out.printf("/  ");
        System.out.println();
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                if (j == 0) System.out.printf("/ "); //border stuff
                if (shapeAt(j, i) != Tetrominoe.NoShape && numberAt(j, i) >= 0) {
                    System.out.printf(" %d ", numbers[(i * width) + j]); //The number
                } else if (shapeAt(j, i) != Tetrominoe.NoShape && numberAt(j, i) == -2) {
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
            setPreviewShapeAt(x, y, newShape.getShape());
            setPreviewNumberAt(x, y, newShape.getNumberAt(i));
        }
    }

    private void printBlockPreviews() {
        for (int i = 0; i < previewHeight; i++) { // for the height of 4
            for (int j = 0; j < previewWidth; j++) { // for the width of 10
                if (previewShapeAt(j, i) != Tetrominoe.NoShape && previewNumberAt(j, i) != -1) {
                    System.out.printf(" %d ", previewNumberAt(j, i));
                } else {
                    System.out.printf("   ");
                }
            }
            System.out.println();
        }
        System.out.println();
    }
}
