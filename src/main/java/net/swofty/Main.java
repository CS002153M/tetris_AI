package net.swofty;

import net.swofty.tetris.Field;

public class Main {
    public static void main(String[] args) {
        Field field = new Field();
        int columnI = field.placeTetromino(Field.Tetromino.T, 0, 4);

        // 	The sum of squared well heights. A well is an opening 1 cell wide, which happens to function as a chokepoint.
        System.out.println("Sum Well " + field.sumWell());

        // The count of all holes on the grid. A cell is a hole if it is empty and a filled cell is above it.
        System.out.println("Sum Holes " + field.sumHoles());

        // The sum of the column heights.
        System.out.println("Sum Height " + field.sumHeight());

        // The number of times neighboring cells flip between empty and filled along a column.
        System.out.println("Column Flip " + field.columnFlip());

        // The number of times neighboring cells flip between empty and filled along a row.
        System.out.println("Row Flip " + field.rowFlip());

        System.out.println(field);
    }
}