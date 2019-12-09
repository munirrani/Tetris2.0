package fsktm.fop;

import javax.swing.*;
import java.awt.*;

public class Board extends JPanel {

    int width = 10, height = 10;
    private Tetris tetris;
    private Timer timer;
    private Graphics graphics;
    Color color, color2, color3;

    Board(Tetris game) {
        color = new Color(25,232, 125);
        color2 = new Color(234,12, 234);
        color3 = new Color(41,153,153);

        setFocusable(true);
        setBackground(color3);
        tetris = game;
    }

    @Override
    public void paintComponent(Graphics g) {

        super.paintComponent(g);
        drawSquare(g);
    }

    private void drawSquare(Graphics g) {

        int squareWidth = 50, squareHeight = 50;
        int x = 50, y = 50;

        g.setColor(color);
        g.fillRect(x, y, squareWidth ,squareHeight);

        // TODO - Put number string in the middle of a square block
        g.setColor(color.darker());
        String number = "1";
        FontMetrics fontMetrics = g.getFontMetrics();
        int textWidth = fontMetrics.stringWidth(number);
        int textHeight = fontMetrics.getHeight();
        g.drawString(number, (x + (squareWidth / 2) - textWidth / 2), (y + (squareHeight / 2) + (textWidth / 2)));
    }

    public void update() {
        System.out.println(timer.getDelay());
        getGraphics().setColor(color);
        getGraphics().fillRect(100, 300, 200 ,200);

    }

    public void reload() {
        getGraphics().setColor(color3);
        getGraphics().fillRect(100, 300, 200 ,200);
    }


}
