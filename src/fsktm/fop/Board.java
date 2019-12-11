package fsktm.fop;

import fsktm.fop.Shape.Tetrominoe;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

public class Board extends JPanel {

    /*
    TODO
    3 - Bug: preview is skewed when the game is done (fixed is after tembus & hold feature is done)
    (use tryMove to solve the preview bugs)
    4 - Once the core game functions are fully working, then make the GUI
     */
    int width = 10;
    int height = 10;
    int previewWidth = width;
    int previewHeight = 4;
    int holdBlockWidth = 4;
    int holdBlockHeight = height;
    private Tetrominoe[] board = new Tetrominoe[height * width];
    private int[] numbers = new int [width * height];
    private Tetrominoe[] previewBoard = new Tetrominoe[previewHeight * previewWidth];
    private int[] previewNumbers = new int[previewHeight * previewWidth];
    private Tetrominoe[] holdBlockBoard = new Tetrominoe[holdBlockWidth * holdBlockHeight];
    private int[] holdBlockNumbers = new int[holdBlockWidth * holdBlockHeight];
    private static ArrayList<Shape> previewShape = new ArrayList<Shape>(4);

    private static Shape currentShape, shape1;
    private int currentX, currentY;
    private boolean isFull = false;

    Font minecraftFont;
    private PreviewBoard boardPreview;
    private HoldBlock holdBlock;

    private int currentScore = 0;
    private int newScore;

    /*
    TODO
    1 - Retain number when the block tries to rotate on the edges
    2 - Add preview and hold block board
    3 - Make an easy game mode
    4 - Scoring system
     */

    Board() {
        initBoard();
        boardPreview = new PreviewBoard();
        holdBlock = new HoldBlock();

        addKeyListener(new ShortcutAdapter());

        currentX = width / 2 - 1; // start from the middle
        currentY =  height / 2 - 1;
        putShadowShapeOnBoard(currentX ,currentY, currentShape, currentShape.getShape(), -2);
        setFocusable(true);

        try {
            minecraftFont = Font.createFont(Font.TRUETYPE_FONT, new File("Minecraft.ttf")).deriveFont(18f);
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(minecraftFont);
        } catch (IOException e) {
            e.printStackTrace();
        } catch(FontFormatException e) {
            e.printStackTrace();
        }

    }

    public int squareWidth() {
        return (int) getSize().getWidth() / width;
    }

    public int squareHeight() {
        return (int) getSize().getHeight() / height;
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
        retainNumber();
        if (count == 4) return true;
        return false;
    }

    private void rotate() {
        currentShape = currentShape.rotateRight();
        previewShape.set(0, currentShape);
        retainNumber();
        boardPreview.updatePreviewBoard();
        boardPreview.repaint();
    }

    private void initBoard() {
        for (int i = 0; i < width * height; i++) {
            board[i] = Tetrominoe.NoShape;
            numbers[i] = -1; // Use -1 to indicate its empty
        }
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

    public Shape generateRandomShape() {
        var shape = new Shape();
        shape.setRandomShape();
        shape.setRandomNumber();
        return shape;
    }

    private void swapBlock() {
        if (previewShape.size() == 3) {
            // Set position 3 in ArrayList to place hold blocks
            if (!canMove(currentX, currentY, previewShape.get(1))) return;
            previewShape.add(generateRandomShape());
            putShadowShapeOnBoard(currentX, currentY, currentShape, Tetrominoe.NoShape, -1);
            Collections.swap(previewShape, 0, 3);
            Collections.swap(previewShape, 1, 2);
            Collections.swap(previewShape, 0, 2);
            currentShape = previewShape.get(0);
            putShadowShapeOnBoard(currentX, currentY, currentShape, currentShape.getShape(), -2);
        } else {
            if (!canMove(currentX, currentY, previewShape.get(3))) return;
            putShadowShapeOnBoard(currentX, currentY, currentShape, Tetrominoe.NoShape, -1);
            Collections.swap(previewShape, 0, 3);
            currentShape = previewShape.get(0);
            putShadowShapeOnBoard(currentX, currentY, currentShape, currentShape.getShape(), -2);
        }
        holdBlock.updateHoldBlockBoard();
        holdBlock.repaint();
        boardPreview.updatePreviewBoard();
        boardPreview.repaint();
        repaint();
    }

    private void insert() {
        if (!isFull) {
            putShapeOnBoard(currentX, currentY, currentShape);
            if (previewShape.size() == 3) {
                previewShape.remove(0);
                previewShape.add(generateRandomShape());
            } else {
                Collections.swap(previewShape, 0, 1);
                Collections.swap(previewShape, 1, 2);
                previewShape.set(2, generateRandomShape());
            }
            currentShape = previewShape.get(0);
            checkForColumnAndRow();
        }

        if (blocksIsAvailable()) {
            putShadowShapeOnBoard(currentX, currentY, currentShape, currentShape.getShape(), -2);
            shape1 = currentShape;
            boardPreview.updatePreviewBoard();
            boardPreview.repaint();
            repaint();
        } else {
            isFull = true;
            repaint();
            return;
        }
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
        repaint();
        return true;
    }

    private boolean canMove(int newX, int newY, Shape shape) {
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
        repaint();
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
        repaint();
    }

    public void drawSquare(Graphics g, int x, int y, Tetrominoe shape, int value, boolean isShadow) {

        Color colors[] = {new Color(0, 0, 0), new Color(204, 102, 102),
                new Color(102, 204, 102), new Color(102, 102, 204),
                new Color(204, 204, 102), new Color(204, 102, 204),
                new Color(102, 204, 204), new Color(218, 170, 0)
        };

        var color = colors[shape.ordinal()];

        if (isShadow) {
            g.setColor(Color.GRAY);
        } else {
            g.setColor(color);
        }
        g.fillRect(x + 1, y + 1, squareWidth() - 2, squareHeight() - 2);

        g.setColor(color.brighter());
        g.drawLine(x, y + squareHeight() - 1, x, y);
        g.drawLine(x, y, x + squareWidth() - 1, y);

        g.setColor(color.darker());
        g.drawLine(x + 1, y + squareHeight() - 1,
                x + squareWidth() - 1, y + squareHeight() - 1);
        g.drawLine(x + squareWidth() - 1, y + squareHeight() - 1,
                x + squareWidth() - 1, y + 1);

        if (isShadow) return;
        g.setColor(color.darker());
        String number = String.valueOf(value);
        FontMetrics fontMetrics = g.getFontMetrics();
        int textWidth = fontMetrics.stringWidth(number);
        g.setFont(minecraftFont);
        g.drawString(number, (x + (squareWidth() / 2) - textWidth / 2), (y + (squareHeight() / 2) + (textWidth / 2)));
    }

    public boolean gameIsOver() {
        return isFull;
    }

    @Override
    public void paintComponent(Graphics g) {

        super.paintComponent(g);

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                Tetrominoe shape = shapeAt(j, i);
                int number = numberAt(j, i);
                if (shape != Tetrominoe.NoShape && number >= 0) {
                    drawSquare(g, j * squareWidth(),
                            i * squareHeight(), shape, number, false);
                } else if (shape != Tetrominoe.NoShape && number == -2) {
                    drawSquare(g, j * squareWidth(),
                            i * squareHeight(), shape, number, true);
                }
            }
        }
    }

    class ShortcutAdapter extends KeyAdapter {

        @Override
        public void keyPressed(KeyEvent e) {

            if (currentShape.getShape() == Tetrominoe.NoShape) {
                return;
            }

            int keycode = e.getKeyCode();
            int testX, testY;

            switch (keycode) {
                case KeyEvent.VK_A:
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
                    break;
                case KeyEvent.VK_D:
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
                    break;
                case KeyEvent.VK_W:
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
                    break;
                case KeyEvent.VK_S:
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
                    break;
                case KeyEvent.VK_R:
                    if (canRotate()) {
                        putShadowShapeOnBoard(currentX, currentY, currentShape, Tetrominoe.NoShape, -1); // remove
                        rotate();
                        putShadowShapeOnBoard(currentX, currentY, currentShape, currentShape.getShape(), -2); // add
                    }
                    repaint();
                    break;
                case KeyEvent.VK_H:
                    swapBlock();
                    break;
                case KeyEvent.VK_I:
                    insert();
                    break;
                case KeyEvent.VK_E:
                    break;
                case KeyEvent.VK_C:
                    initBoard();
                    repaint();
                    break;
            }
        }
    }

    public PreviewBoard getBoardPreview() {
        return boardPreview;
    }

    public HoldBlock getHoldBlock() {
        return holdBlock;
    }

    class PreviewBoard extends JPanel {

        PreviewBoard() {
            initPreviewBoard();
            Board.this.setFocusable(true);
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

        private void updateScore() {
            currentScore += newScore;
        }

        private Tetrominoe previewShapeAt(int x, int y) {
            return previewBoard[(y * previewWidth) + x];
        }

        private int previewNumberAt(int x, int y) {
            return previewNumbers[(y * previewWidth) + x];
        }

        private void setPreviewShapeAt(int x, int y, Tetrominoe tetrominoe) {
            previewBoard[(y * previewWidth) + x] = tetrominoe;
        }
        private void setPreviewNumberAt(int x, int y, int value) {
            previewNumbers[(y * previewWidth) + x] = value;
        }

        public void putShapeOnPreviewBoard(int a, int b, Shape newShape) {
            for (int i = 0; i < 4; i++) {
                int x = a + newShape.x(i);
                int y = b + newShape.y(i);
                // Line shape preview rotation fixes
                if (newShape.getShape() == Tetrominoe.LineShape && newShape.y(3) < 0) {
                    y++;
                }
                if (newShape.getShape() == Tetrominoe.LineShape && newShape.x(0) < 0) {
                    x--;
                }
                setPreviewShapeAt(x, y, newShape.getShape());
                setPreviewNumberAt(x, y, newShape.getNumberAt(i));
            }
        }

        @Override
        public void paintComponent(Graphics g) {

            super.paintComponent(g);

            for (int i = 0; i < previewHeight; i++) {
                for (int j = 0; j < previewWidth; j++) {
                    Tetrominoe shape = previewShapeAt(j, i);
                    int number = previewNumberAt(j, i);
                    if (shape != Tetrominoe.NoShape && number != -1) {
                        drawSquare(g, j * squareWidth(),
                                i * squareHeight(), shape, number, false);
                    }
                }
            }
        }
    }

    class HoldBlock extends JPanel {
        HoldBlock() {
            initHoldBlockBoard();
            Board.this.setFocusable(true);
        }

        private void initHoldBlockBoard() {
            clearHoldBlockBoard();
        }

        private Tetrominoe holdBlockAt(int x, int y) {
            return holdBlockBoard[(y * holdBlockWidth) + x];
        }

        private int holdBlockNumberAt(int x, int y) {
            return holdBlockNumbers[(y * holdBlockWidth) + x];
        }

        private void setHoldBlockAt(int x, int y, Tetrominoe tetrominoe) {
            holdBlockBoard[(y * holdBlockWidth) + x] = tetrominoe;
        }
        private void setHoldBlockNumberAt(int x, int y, int value) {
            holdBlockNumbers[(y * holdBlockWidth) + x] = value;
        }

        private void clearHoldBlockBoard() {
            for (int i = 0; i < holdBlockWidth * holdBlockHeight; i++) {
                holdBlockBoard[i] = Tetrominoe.NoShape;
                holdBlockNumbers[i] = -1; // -1 to indicate its nothing
            }
        }

        private void updateHoldBlockBoard() {
            clearHoldBlockBoard();
            putShapeOnHoldBlockBoard(1, 6, previewShape.get(3));
        }

        private void putShapeOnHoldBlockBoard(int a, int b, Shape shape) {
            for (int i = 0; i < 4; i++) {
                int x = a + shape.x(i);
                int y = b + shape.y(i);
                // Line shape fix
                if (shape.getShape() == Tetrominoe.LineShape && shape.x(3) == -2) {
                    x++;
                }
                setHoldBlockAt(x, y, shape.getShape());
                setHoldBlockNumberAt(x, y, shape.getNumberAt(i));
            }
        }

        @Override
        public void paintComponent(Graphics g) {

            super.paintComponent(g);

            for (int i = 0; i < holdBlockHeight; i++) {
                for (int j = 0; j < holdBlockWidth; j++) {
                    Tetrominoe shape = holdBlockAt(j, i);
                    int number = holdBlockNumberAt(j, i);
                    if (shape != Tetrominoe.NoShape && number != -1) {
                        drawSquare(g, j * squareWidth(),
                                i * squareHeight(), shape, number, false);
                    }
                }
            }
        }
    }
}
