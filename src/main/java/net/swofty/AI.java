package net.swofty;

import net.swofty.tetris.Field;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class AI {
    public static void train() throws ExecutionException, InterruptedException {
        int populationSize = 100;
        int numGenerations = 50;
        int numThreads = 32;  // Number of threads

        long startTime = System.currentTimeMillis();

        ExecutorService executor = Executors.newFixedThreadPool(numThreads);

        // Initialize Population
        List<double[]> population = new ArrayList<>();
        for (int i = 0; i < populationSize; i++) {
            population.add(initializeRandomWeights());
        }

        // Run Genetic Algorithm
        for (int generation = 0; generation < numGenerations; generation++) {
            long generationStartTime = System.currentTimeMillis();
            Map<double[], Double> scoredPopulation = new HashMap<>();
            final Double[] bestScore = {0D};
            final double[][] bestWeights = {null};

            List<Future<Void>> futures = new ArrayList<>();

            for (double[] individual : population) {
                futures.add(executor.submit(() -> {
                    double score = runGameWithWeights(individual);
                    scoredPopulation.put(individual, score);
                    synchronized (bestScore[0]) {
                        if (score > bestScore[0]) {
                            bestScore[0] = score;
                            bestWeights[0] = individual;
                        }
                    }
                    return null;
                }));
            }

            // Wait for all threads to complete
            for (Future<Void> future : futures) {
                future.get();
            }

            // Sort by score
            population.sort(Comparator.comparing(scoredPopulation::get).reversed());

            // Select top 50% of population
            population = population.subList(0, populationSize / 2);

            // Crossover and Mutation
            List<double[]> newGeneration = new ArrayList<>(population);
            while (newGeneration.size() < populationSize) {
                double[] parent1 = population.get(new Random().nextInt(population.size() / 2));
                double[] parent2 = population.get(new Random().nextInt(population.size() / 2));

                double[] child = crossover(parent1, parent2);
                mutate(child);

                newGeneration.add(child);
            }
            population = newGeneration;

            // Print loading bar and time elapsed
            printLoadingBar((generation + 1), numGenerations);
            long generationEndTime = System.currentTimeMillis();
            System.out.println("Generation " + (generation + 1) + " completed in " + (generationEndTime - generationStartTime) + " ms " + "with best score of " + bestScore[0] + " and best weights " + Arrays.toString(bestWeights[0]));
        }

        double[] bestWeights = population.get(0);
        System.out.println("Best Weights: " + Arrays.toString(bestWeights));
        System.out.println("Total time elapsed: " + (System.currentTimeMillis() - startTime) + " ms");
    }


    public static void printLoadingBar(int currentGeneration, int totalGenerations) {
        int barLength = 50; // Length of the loading bar
        int progress = (int) ((currentGeneration / (float) totalGenerations) * barLength);
        StringBuilder bar = new StringBuilder("[");

        for (int i = 0; i < barLength; i++) {
            if (i < progress) {
                bar.append("=");
            } else if (i == progress) {
                bar.append(">");
            } else {
                bar.append(" ");
            }
        }
        bar.append("]");
        System.out.print("\r" + bar.toString());
    }

    public static double[] initializeRandomWeights() {
        double[] weights = new double[5];
        for (int i = 0; i < 5; i++) {
            weights[i] = (Math.random() * 10) - 5;
        }
        return weights;
    }

    public static int runGameWithWeights(double[] weights) {
        Field field = new Field();
        field.isActive = true;

        int score = 0;
        while (field.isActive) {
            Field.Tetromino randomTetromino = Field.Tetromino.values()[new Random().nextInt(Field.Tetromino.values().length)];
            List<Move> possibleMoves = getPossibleMoves(field, randomTetromino);
            Map.Entry<Double, Move> bestMove = null;

            for (int i = 0; i < possibleMoves.size(); i++) {
                Move move = possibleMoves.get(i);
                Field fieldClone = field.clone();
                fieldClone.placeTetromino(move.tetromino, move.rotation, move.x);
                Double fieldScore = fieldClone.score(weights[0], weights[1], weights[2], weights[3], weights[4]);

                if (bestMove == null || bestMove.getKey() > fieldScore) {
                    bestMove = new AbstractMap.SimpleEntry<>(fieldScore, move);
                }
            }

            if (bestMove == null) {
                field.isActive = false;
                break;
            }
            field.placeTetromino(bestMove.getValue().tetromino, bestMove.getValue().rotation, bestMove.getValue().x);
            score += 1;
        }
        return score;
    }

    public static List<Move> getPossibleMoves(Field field, Field.Tetromino tetromino) {
        List<Move> moves = new ArrayList<>();
        for (int rotation = 0; rotation < 4; rotation++) {
            for (int x = 0; x < field.cols; x++) {
                int y = field.canPlaceTetromino(tetromino, rotation, x);
                if (y > 0) {
                    moves.add(new Move(tetromino, rotation, x, y));
                }
            }
        }
        return moves;
    }

    public static double[] crossover(double[] parent1, double[] parent2) {
        double[] child = new double[5];
        for (int i = 0; i < 5; i++) {
            child[i] = Math.random() < 0.5 ? parent1[i] : parent2[i];
        }
        return child;
    }

    public static void mutate(double[] individual) {
        for (int i = 0; i < 5; i++) {
            if (Math.random() < 0.1) {
                individual[i] += (Math.random() * 2) - 1;
            }
        }
    }

    public static class Move {
        Field.Tetromino tetromino;
        int rotation;
        int x;
        int y;

        public Move(Field.Tetromino tetromino, int rotation, int x, int y) {
            this.tetromino = tetromino;
            this.rotation = rotation;
            this.x = x;
            this.y = y;
        }

        @Override
        public String toString() {
            return "Move{" +
                    "tetromino=" + tetromino +
                    ", rotation=" + rotation +
                    ", x=" + x +
                    ", y=" + y +
                    '}';
        }
    }
}