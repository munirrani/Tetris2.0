package fsktm.fop;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Tetris extends JFrame {

    private JLabel statusbar;

    Tetris() {
        statusbar = new JLabel("0");
        Board board = new Board(this);
        add(board);

        setSize(600, 600);
        setTitle("My First Game!");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);


    }


    public JLabel getStatusbar() {
        return statusbar;
    }
}
