package net.swofty;

public class NeuralNetwork {
    private double[] weights;

    public NeuralNetwork(int numWeights) {
        weights = new double[numWeights];
        // Initialize weights randomly between -5 and 5
        for (int i = 0; i < numWeights; i++) {
            weights[i] = (Math.random() * 10) - 5;
        }
    }

    public double[] predict() {
        return weights;
    }

    public void update(double[] newWeights) {
        this.weights = newWeights;
    }
}
