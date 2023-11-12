package net.swofty.web;

import net.swofty.AI;
import net.swofty.tetris.Field;
import org.json.JSONObject;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;

import static spark.Spark.get;

public class FieldEndpoints {
    public static void handle() {
        get("/protected/update-field", (request, response) -> {
            User user = User.getFromAuthenticationToken(request.headers("token"));
            Field field = AuthenticationEndpoints.users.get(user);

            Field.Tetromino tetromino = Field.Tetromino.valueOf(request.headers("tetromino"));
            int rotation = Integer.parseInt(request.headers("rotation"));
            int x = Integer.parseInt(request.headers("x"));

            field.placeTetromino(tetromino, rotation, x);
            AuthenticationEndpoints.users.put(user, field);
            return new JSONObject().append("status", "success");
        });

        get("/protected/best-move", (request, response) -> {
            User user = User.getFromAuthenticationToken(request.headers("token"));
            Field field = AuthenticationEndpoints.users.get(user);

            Field.Tetromino tetromino = Field.Tetromino.valueOf(request.headers("tetromino"));

            double[] weights = new double[]{0.13491202796307222, 0.3256252789403291, -0.215034637907636, 5.277818414214094, 3.054917632613636, -4.240327972106979};

            List<AI.Move> possibleMoves = AI.getPossibleMoves(field, tetromino);
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
                return new JSONObject().append("status", "error");
            }

            return new JSONObject().append("status", "success").append("rotation", bestMove.getValue().rotation).append("x", bestMove.getValue().x);
        });

        get("/protected/clear-field", (request, response) -> {
            User user = User.getFromAuthenticationToken(request.headers("token"));
            AuthenticationEndpoints.users.put(user, new Field());
            return new JSONObject().append("status", "success");
        });
    }
}
