package net.swofty;

import com.mongodb.Block;
import net.swofty.database.AuthenticationDatabase;
import net.swofty.tetris.Field;
import net.swofty.web.AuthenticationEndpoints;
import net.swofty.web.FieldEndpoints;
import net.swofty.web.User;
import org.bson.Document;
import spark.Spark;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutionException;

public class Main {
    public static void main(String[] args) throws InterruptedException, ExecutionException {
        // AI.train();
        Spark.port(6969);
        AuthenticationDatabase database = new AuthenticationDatabase("test");
        database.connect();

        AuthenticationDatabase.collection.find().forEach((Block<? super Document>) document -> {
            new User(document.getString("id"));
        });

        AuthenticationEndpoints.handle();
        FieldEndpoints.handle();
    }

    public static void test() throws InterruptedException {
        Field field = new Field();
        field.isActive = true;
        double[] weights = new double[]{0.13491202796307222, 0.3256252789403291, -0.215034637907636, 5.277818414214094, 3.054917632613636, -4.240327972106979};

        int score = 0;
        while (field.isActive) {
            Field.Tetromino randomTetromino = Field.Tetromino.values()[new Random().nextInt(Field.Tetromino.values().length)];
            List<AI.Move> possibleMoves = AI.getPossibleMoves(field, randomTetromino);
            Map.Entry<Double, AI.Move> bestMove = null;

            for (int i = 0; i < possibleMoves.size(); i++) {
                AI.Move move = possibleMoves.get(i);
                Field fieldClone = field.clone();
                fieldClone.placeTetromino(move.tetromino, move.rotation, move.x);
                Double fieldScore = fieldClone.score(weights[0], weights[1], weights[2], weights[3], weights[4], weights[5]);

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

            Thread.sleep(1000);
        }
    }
}
