package net.swofty;

import com.sun.jdi.connect.Connector;
import net.swofty.tetris.Field;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutionException;

public class Main {
    public static void main(String[] args) throws InterruptedException, ExecutionException {
        Field field = new Field();
        field.isActive = true;
        double[] weights = new double[]{0.3530402080118964, 2.714523087393916, 1.476554458549091, 2.914816700535999, 3.8358749995219608};

        int score = 0;
        while (field.isActive) {
            Field.Tetromino randomTetromino = Field.Tetromino.values()[new Random().nextInt(Field.Tetromino.values().length)];
            List<AI.Move> possibleMoves = AI.getPossibleMoves(field, randomTetromino);
            Map.Entry<Double, AI.Move> bestMove = null;

            for (int i = 0; i < possibleMoves.size(); i++) {
                AI.Move move = possibleMoves.get(i);
                Field fieldClone = field.clone();
                fieldClone.placeTetromino(move.tetromino, move.rotation, move.x);
                Double fieldScore = fieldClone.score(weights[0], weights[1], weights[2], weights[3], weights[4]);

                if (bestMove == null || bestMove.getKey() > fieldScore) {
                    bestMove = new AbstractMap.SimpleEntry<>(fieldScore, move);
                }
            }

            if (bestMove == null)  {
                field.isActive = false;
                break;
            }
            field.placeTetromino(bestMove.getValue().tetromino, bestMove.getValue().rotation, bestMove.getValue().x);
            score += 1;

            System.out.println("Placing tetromino \n" + bestMove.getValue().tetromino.toString(bestMove.getValue().rotation) + " at rotation " + bestMove.getValue().rotation + " at x " + bestMove.getValue().x + " with score " + score);
            System.out.println(field);
        }
    }
}
