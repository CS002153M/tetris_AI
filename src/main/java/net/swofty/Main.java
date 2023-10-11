package net.swofty;

import net.swofty.tetris.Field;

public class Main {
    public static void main(String[] args) {
        Field field = new Field();
        int columnI = field.placeTetromino(Field.Tetromino.T, 0, 4);

        System.out.println(field);
    }
}