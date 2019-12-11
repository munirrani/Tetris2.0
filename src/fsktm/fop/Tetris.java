package fsktm.fop;

import javax.swing.*;
import java.awt.*;

public class Tetris extends JFrame {

    private JLabel statusbar;
    private Board.PreviewBoard previewBoard;
    private Board.HoldBlock holdBlock;

    Tetris() {
        int width = 600, height = 1000;
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.X_AXIS));
        JPanel leftside = new JPanel();
        leftside.setLayout(new BoxLayout(leftside, BoxLayout.Y_AXIS));
        statusbar = new JLabel("0");
        Board board = new Board();
        board.setPreferredSize(new Dimension(width, height * 3 / 5));
        leftside.add(board);
        previewBoard = board.getBoardPreview();
        previewBoard.setPreferredSize(new Dimension(width, height * 2 / 5));
        leftside.add(previewBoard);

        holdBlock = board.getHoldBlock();
        holdBlock.setPreferredSize(new Dimension(width / 2, height));

        container.add(leftside);
        container.add(holdBlock);

        add(container);

        setSize(600, 600);
        setTitle("My First Game!");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        if(board.gameIsOver()) setTitle("Game Over!");
    }


    public JLabel getStatusbar() {
        return statusbar;
    }
}
