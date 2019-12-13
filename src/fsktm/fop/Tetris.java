package fsktm.fop;

import javax.swing.*;
import java.awt.*;

public class Tetris extends JFrame {

    private Board.PreviewBoard previewBoard;
    private Board.HoldBlock holdBlock;
    private Board board;

    Tetris() {
        int width = 700, height = 1000;

        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.X_AXIS));

        JPanel leftside = new JPanel();
        leftside.setLayout(new BoxLayout(leftside, BoxLayout.Y_AXIS));

        board = new Board();
        board.setPreferredSize(new Dimension(width, height * 15 / 22));

        Border horizontalBorder = new Border(10, 1, board.squareWidth(), board.squareHeight());
        horizontalBorder.setPreferredSize(new Dimension(width, height * 1 / 22));

        previewBoard = board.getBoardPreview();
        previewBoard.setPreferredSize(new Dimension(width, height * 6 / 22));

        leftside.add(board);
        leftside.add(horizontalBorder);
        leftside.add(previewBoard);

        Dimension holdBlockDimension = new Dimension(width / 3, height);
        holdBlock = board.getHoldBlock();
        holdBlock.setPreferredSize(holdBlockDimension);
        holdBlock.setMaximumSize(holdBlockDimension);

        Border verticalBorder = new Border(1, 10 + 4, board.squareWidth(), board.squareHeight());
        verticalBorder.setPreferredSize(new Dimension(width / 14, height));

        container.add(leftside);
        container.add(verticalBorder);
        container.add(holdBlock);
        add(container);

        setSize(600, 600);
        setTitle("Tetris 2.0");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        if(board.gameIsOver()) {
            System.out.println("woi");
            setTitle("Game Over!");
        }
    }


}
