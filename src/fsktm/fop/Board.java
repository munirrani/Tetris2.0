package fsktm.fop;

import fsktm.fop.Shape.Tetrominoe;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.TimerTask;

public class Board extends JPanel {

    /*

    Display the keyboard shortcuts
    Print text of top 5 players, ikut turutan
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

    private static Font minecraftFont;
    private PreviewBoard boardPreview;
    private HoldBlock holdBlock;

    private int columnCleared, rowCleared;
    private int currentScore = 0;
    private int newScore;

    private static BufferedImage background;
    private Tetris parent;
    private ScoringFileSystem scoringFileSystem;

    private int timerDelay = 1000;
    private int time = 10;
    private Timer timer;

    Board(Tetris tetris) {
        scoringFileSystem = new ScoringFileSystem();
        parent = tetris;

        try {
            minecraftFont = Font.createFont(Font.TRUETYPE_FONT, new File("Minecraft.ttf")).deriveFont(18f);
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(minecraftFont);
            background = ImageIO.read(new File("wallpaper.jpg"));
        } catch (IOException e) {
            e.printStackTrace();
        } catch(FontFormatException e) {
            e.printStackTrace();
        }

        timer = new Timer(timerDelay, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (time == 0) {
                    insert();
                }
                time--;
                holdBlock.setTimerText(time+1);
            }
        });
        timer.start();

        initGame();
    }

    private void initGame() {
        initBoard();
        previewShape.clear();
        currentScore = 0;

        boardPreview = new PreviewBoard();
        holdBlock = new HoldBlock();
        addKeyListener(new ShortcutAdapter());

        currentX = width / 2 - 1; // start from the middle
        currentY =  height / 2 - 1;
        putShadowShapeOnBoard(currentX ,currentY, currentShape, currentShape.getShape(), -2);
        setFocusable(true);
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
        boolean[] rowClerared = new boolean[10];
        boolean[] columnCleared = new boolean[10];
        this.rowCleared = 0;
        this.columnCleared = 0;
        newScore = 0;

        for (int i = 0; i < 10; i++) {
            rowClerared[i] = false;
            columnCleared[i] = false;
        }
        for (int i = 0; i < width; i++) {
            if (sumEvenForRow(i)) {
                rowClerared[i] = true;
                newScore += 100 * (int) Math.pow(2, this.rowCleared++);
                System.out.println("Row " + (i+1) + " cleared!");
            }
        }
        for (int j = 0; j < height; j++) {
            if (sumEvenForColumn(j)) {
                columnCleared[j] = true;
                newScore += 100 * (int) Math.pow(2, this.columnCleared++);
                System.out.println("Column " + (j+1) + " cleared!");
            }
        }

        for (int i = 0; i < 10; i++) {
            if (rowClerared[i] == true) {
                clearRow(i);
            }
            if (columnCleared[i] == true) {
                clearColumn(i);
            }
        }
        if (this.rowCleared > 0 && this.columnCleared > 0) newScore *= 10;
    }

    private boolean canRotate() {
        int count = 0;
        currentShape = currentShape.rotateRight();
        for (int i = 0; i < 4; i++) {
            int x = currentX + currentShape.x(i);
            int y = currentY + currentShape.y(i);
            if (tryMove(x, y, currentShape)) {
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
            putShadowShapeOnBoard(currentX, currentY, currentShape, Tetrominoe.NoShape, -1); //remove
            Collections.swap(previewShape, 0, 3);
            Collections.swap(previewShape, 1, 2);
            Collections.swap(previewShape, 0, 2);
            currentShape = previewShape.get(0);
            putShadowShapeOnBoard(currentX, currentY, currentShape, currentShape.getShape(), -2); //add
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
        time = 10;
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
            updateScore();
            holdBlock.setScoreText(currentScore);
        }

        if (blocksIsAvailable()) {
            putShadowShapeOnBoard(currentX, currentY, currentShape, currentShape.getShape(), -2);
            shape1 = currentShape;
            boardPreview.updatePreviewBoard();
            boardPreview.repaint();
            repaint();
        } else {
            isFull = true;
            gameOver();
            repaint();
            return;
        }
    }

    private void gameOver() {
        timer.stop();
        JOptionPane.showMessageDialog(null, "Game Over!", "Tetris 2.0",
                JOptionPane.INFORMATION_MESSAGE, parent.getImageIcon());
        scoringFileSystem.addNewScore(parent.getName(), currentScore);
        scoringFileSystem.write();
        System.exit(0); // bye
    }

    private void updateScore() {
        currentScore += newScore;
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
            if (!isFull) rotate();
        }
        return false;
    }

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
            setNumberAt(oldX, oldY, -1);
        }
        for (int i = 0; i < 4; i++) {
            int x = newX + shape.x(i);
            int y = newY + shape.y(i);
            setShapeAt(x, y, tetrominoe);
            setNumberAt(x, y, -2); //can only move shadow
        }
        repaint();
    }

    public void drawSquare(Graphics g, int x, int y, Tetrominoe shape, int value, boolean isShadow, Color c) {

        Color colors[] = {new Color(0, 0, 0), new Color(255, 55, 55),
                new Color(82, 185, 68), new Color(60, 60, 240),
                new Color(244, 244, 33), new Color(170, 53, 221),
                new Color(57, 221, 221), new Color(240, 120, 60)
        };

        var color = colors[shape.ordinal()];
        if (isShadow) color = Color.GRAY;
        if (c != null) color = c;

        g.setColor(color);
        g.fillRect(x + 1, y + 1, squareWidth() - 2, squareHeight() - 2);

        g.setColor(color.brighter());
        g.drawLine(x, y + squareHeight() - 1, x, y); //vertical line
        g.drawLine(x, y, x + squareWidth() - 1, y); //horizontal

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
        g.drawImage(background, 0,0, (int)getSize().getWidth(), (int)getSize().getHeight(), null);

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                Tetrominoe shape = shapeAt(j, i);
                int number = numberAt(j, i);
                if (shape != Tetrominoe.NoShape && number >= 0) {
                    drawSquare(g, j * squareWidth(),
                            i * squareHeight(), shape, number, false, null);
                } else if (shape != Tetrominoe.NoShape && number == -2) {
                    drawSquare(g, j * squareWidth(),
                            i * squareHeight(), shape, number, true, null);
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
            super();
            initPreviewBoard();
            Board.this.setFocusable(true);
        }

        private void initPreviewBoard() {
            clearPreviewBoard();
            for (int i = 0; i < 3; i++) {
                previewShape.add(generateRandomShape());
            }
            updatePreviewBoard();
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
                                i * squareHeight(), shape, number, false, null);
                    }
                }
            }
        }
    }

    class HoldBlock extends JPanel {

        JTextArea score;
        JTextArea timerText;

        HoldBlock() {
            super();
            initHoldBlockBoard();
            Board.this.setFocusable(true);
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

            JTextArea tetris = new JTextArea();
            tetris.setAlignmentX(Component.CENTER_ALIGNMENT);
            tetris.setAlignmentY(Component.TOP_ALIGNMENT);
            tetris.setText("TETRIS 2.0");
            tetris.setFocusable(false);
            tetris.setVisible(true);
            add(tetris);
            Font minecraftFontBigger = minecraftFont.deriveFont(24f);
            tetris.setFont(minecraftFontBigger);
            tetris.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));

            JTextArea holdText = new JTextArea();
            holdText.setText("Hold ");
            holdText.setFocusable(false);
            holdText.setAlignmentX(Component.CENTER_ALIGNMENT);
            holdText.setAlignmentY(Component.BOTTOM_ALIGNMENT);
            holdText.setVisible(true);
            add(holdText);
            holdText.setFont(minecraftFont);
            holdText.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));

            add(Box.createRigidArea(new Dimension(0, 300)));

            timerText = new JTextArea();
            timerText.setAlignmentX(Component.CENTER_ALIGNMENT);
            timerText.setAlignmentY(Component.BOTTOM_ALIGNMENT);
            timerText.setText(String.valueOf(time));
            timerText.setFocusable(false);
            timerText.setVisible(true);
            add(timerText);
            timerText.setFont(minecraftFontBigger);
            timerText.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));

            score = new JTextArea();
            score.setAlignmentX(Component.CENTER_ALIGNMENT);
            score.setAlignmentY(Component.BOTTOM_ALIGNMENT);
            score.setText("Score: " + String.valueOf(currentScore));
            score.setFocusable(false);
            score.setVisible(true);
            add(score);
            score.setFont(minecraftFont);
            score.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));

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

        public void setScoreText(int value) {
            score.setText("Score: " + String.valueOf(value));
        }

        public void setTimerText(int value) {
            timerText.setText(String.valueOf(value));
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
                                i * squareHeight(), shape, number, false, null);
                    }
                }
            }
        }
    }
}
