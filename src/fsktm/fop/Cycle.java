package fsktm.fop;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Cycle implements ActionListener {

    private Board board;

    Cycle(Board b) {
        board = b;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        board.update();
        board.reload();
    }
}
