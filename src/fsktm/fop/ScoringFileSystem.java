package fsktm.fop;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

public class ScoringFileSystem {

    private int score;
    private ArrayList<String> names = new ArrayList<String>();
    private ArrayList<Integer> scores = new ArrayList<Integer>();

    String filenamePath = "NANDEMONAI.txt";

    ScoringFileSystem() {
        try {
            read();
        } catch (FileNotFoundException e){
            System.out.println("File cannot be found.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void read() throws IOException {
        Scanner scanner = new Scanner(new FileInputStream(filenamePath));
        String line;
        String[] splitted;
        while (scanner.hasNextLine()) {
            line = scanner.nextLine();
            splitted = line.split(",");
            addNewScore(splitted[0], Integer.valueOf(splitted[1]));
        }
    }

    public void write() {
        try {
            PrintWriter printWriter = new PrintWriter(
                    new FileWriter(new File(filenamePath))
            );

            for (int i = 0; i < names.size(); i++) {
                printWriter.println(names.get(i) + "," + String.valueOf(scores.get(i)));
            }
            printWriter.close();
        } catch (IOException e) {

        }
    }

    public void addNewScore(String name, int score) {
        if (names.contains(name)) { //update score if name exists
            int index = names.indexOf(name);
            if(score < scores.get(index)) return; //dont update if marks lower
            scores.set(index, score);
        } else {
            names.add(name);
            scores.add(score);
        }
        sort();
    }

    private void sort() {
        for (int i = 0; i < names.size() - 1; i++) {
            for (int j = 0; j < names.size() - 1; j++) {
                if (scores.get(j + 1) > scores.get(j)) {
                    Collections.swap(scores, j, j + 1);
                    Collections.swap(names, j, j + 1);
                }
            }
        }
    }
}
