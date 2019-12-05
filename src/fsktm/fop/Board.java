package fsktm.fop;

import javax.swing.*;
import java.awt.*;

public class Board extends JPanel {

    int width = 10, height = 10;
    private Tetris tetris;
    private Timer timer;
    private Graphics graphics;
    int x = 100, y = 100;
    Color color, color2, color3;

    Board(Tetris game) {
        color = new Color(25,232, 125);
        color2 = new Color(234,12, 234);
        color3 = new Color(41,153,153);


        setFocusable(true);
        setBackground(color3);
        tetris = game;

        timer = new Timer(1000, new Cycle(this));
        timer.start();
    }

    @Override
    public void paintComponent(Graphics g) {

        super.paintComponent(g);
        drawSquare(g);
    }

    private void drawSquare(Graphics g) {

        g.setColor(color);
        g.fillRect(100, 100, 200 ,200);

        g.setColor(color2);
        g.fillRect(x+200, y+200, 200 ,200);


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
