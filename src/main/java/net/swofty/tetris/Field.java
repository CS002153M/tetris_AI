package net.swofty.tetris;

import java.util.ArrayList;
import java.util.List;

public class Field {
    public int rows = 20;
    public int cols = 10;
    public int score = 0;
    public int linesClearedInLatestMove = 0;
    private int[][] board;
    public boolean isActive;

    public enum Tetromino {
        I(new int[][][] {
                {{1, 1, 1, 1}},
                {{1}, {1}, {1}, {1}}
        }),
        O(new int[][][] {
                {{1, 1}, {1, 1}}
        }),
        T(new int[][][] {
                {{0, 1, 0}, {1, 1, 1}},
                {{1, 0}, {1, 1}, {1, 0}},
                {{1, 1, 1}, {0, 1, 0}},
                {{0, 1}, {1, 1}, {0, 1}}
        }),
        S(new int[][][] {
                {{0, 1, 1}, {1, 1, 0}},
                {{1, 0}, {1, 1}, {0, 1}}
        }),
        Z(new int[][][] {
                {{1, 1, 0}, {0, 1, 1}},
                {{0, 1}, {1, 1}, {1, 0}}
        }),
        J(new int[][][] {
                {{1, 0, 0}, {1, 1, 1}},
                {{1, 1}, {1, 0}, {1, 0}},
                {{1, 1, 1}, {0, 0, 1}},
                {{0, 1}, {0, 1}, {1, 1}}
        }),
        L(new int[][][] {
                {{0, 0, 1}, {1, 1, 1}},
                {{1, 0}, {1, 0}, {1, 1}},
                {{1, 1, 1}, {1, 0, 0}},
                {{1, 1}, {0, 1}, {0, 1}}
        })
        ;

        int[][][] shapes;

        Tetromino(int[][][] shapes) {
            this.shapes = shapes;
        }

        // Returns a specific rotation (0-based index)
        public int[][] getRotation(int index) {
            return shapes[index % shapes.length];
        }

        // Return what it looks like on the board
        public String toString(int rotation) {
            StringBuilder sb = new StringBuilder();
            int[][] shape = getRotation(rotation);
            for (int[] row : shape) {
                for (int cell : row) {
                    sb.append(cell == 0 ? '.' : '#');
                }
                sb.append("\n");
            }
            sb.append("\n");
            return sb.toString();
        }
    }

    public Field() {
        board = new int[rows][cols];
        isActive = true; // Initialize to true as the game is active initially
    }

    public double score(double weightWell, double weightHoles, double weightHeight, double weightColumn, double weightRow) {
        return (sumWell() * weightWell) + (sumHoles() * weightHoles) + (sumHeight() * weightHeight) + (columnFlip() * weightColumn) + (rowFlip() * weightRow);
    }

    public void updateIsActive() {
        for (int c = 0; c < cols; c++) {
            if (board[0][c] != 0) { // Check if any cell in the top row is filled
                isActive = false;  // Set the game to inactive
                return;
            }
        }
        isActive = true; // Otherwise, the game is still active
    }

    public int placeTetromino(Tetromino tetromino, int rotationIndex, int startCol) {
        int[][] shape = tetromino.getRotation(rotationIndex);

        // Find the bottom-most possible row for placement
        int startRow = rows - shape.length;
        for (; startRow >= 0; startRow--) {
            boolean collision = false;
            for (int r = 0; r < shape.length; r++) {
                for (int c = 0; c < shape[r].length; c++) {
                    if (shape[r][c] != 0 && board[startRow + r][startCol + c] != 0) {
                        collision = true;
                        break;
                    }
                }
                if (collision) break;
            }
            if (!collision) break;
        }

        // If there's room, place the Tetromino
        if (startRow >= 0) {
            for (int r = 0; r < shape.length; r++) {
                for (int c = 0; c < shape[r].length; c++) {
                    if (shape[r][c] != 0) {
                        board[startRow + r][startCol + c] = 1;
                    }
                }
            }
        }

        linesClearedInLatestMove = clearLines() + 1;
        updateIsActive();
        score++;

        return startRow;  // return the row where the Tetromino was placed; -1 if it couldn't be placed
    }

    public Field clone() {
        Field clonedField = new Field();
        clonedField.isActive = this.isActive;
        clonedField.rows = this.rows;
        clonedField.cols = this.cols;

        // Deep cloning the 2D board array
        clonedField.board = new int[this.rows][this.cols];
        for (int r = 0; r < this.rows; r++) {
            for (int c = 0; c < this.cols; c++) {
                clonedField.board[r][c] = this.board[r][c];
            }
        }

        return clonedField;
    }

    // Check for complete lines and clear them
    public int clearLines() {
        int linesCleared = 0;  // Initialize counter to 0

        for (int r = 0; r < rows; r++) {
            boolean lineComplete = true;

            for (int c = 0; c < cols; c++) {
                if (board[r][c] == 0) {
                    lineComplete = false;
                    break;
                }
            }

            if (lineComplete) {
                linesCleared++;  // Increment counter when a line is cleared

                // Shift all lines down
                for (int dr = r; dr >= 1; dr--) {
                    System.arraycopy(board[dr - 1], 0, board[dr], 0, cols);
                }

                // Clear the top line
                for (int c = 0; c < cols; c++) {
                    board[0][c] = 0;
                }
            }
        }

        return linesCleared;  // Return the counter
    }

    // Get value at a specific point
    public int getValueAt(int row, int col) {
        return board[row][col];
    }

    public double[] extractFeatures() {
        List<Double> features = new ArrayList<>();

        // Extracting basic metrics
        features.add((double) sumHoles());
        features.add((double) sumHeight());
        features.add((double) rowFlip());
        features.add((double) columnFlip());
        features.add((double) sumWell());

        // Extracting metrics for each Tetromino
        for (Tetromino tetromino : Tetromino.values()) {
            for (int rotation = 0; rotation < tetromino.shapes.length; rotation++) {
                for (int col = 0; col < cols; col++) {
                    int pieceHeight = pieceHeight(tetromino, rotation, col);
                    if (pieceHeight != -1) {
                        features.add((double) pieceHeight);
                    }
                }
            }
        }

        // Convert ArrayList to array
        double[] featureArray = new double[features.size()];
        for (int i = 0; i < features.size(); i++) {
            featureArray[i] = features.get(i);
        }

        return featureArray;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                sb.append(board[r][c] == 0 ? '.' : '#');
            }
            sb.append("\n");
        }
        return sb.toString();
    }



    public int canPlaceTetromino(Tetromino tetromino, int rotationIndex, int startCol) {
        int[][] shape = tetromino.getRotation(rotationIndex);
        int shapeHeight = shape.length;
        int shapeWidth = shape[0].length;

        // Check if the Tetromino fits within the bounds of the board
        if (startCol < 0 || startCol + shapeWidth > cols) {
            return -1;
        }

        // Loop through each row from bottom to top to find where the piece can land
        for (int startRow = rows - shapeHeight; startRow >= 0; startRow--) {
            boolean collision = false;

            // Check for collision with existing pieces
            for (int r = 0; r < shapeHeight; r++) {
                for (int c = 0; c < shapeWidth; c++) {
                    if (shape[r][c] != 0 && board[startRow + r][startCol + c] != 0) {
                        collision = true;
                        break;
                    }
                }

                if (collision) {
                    break;
                }
            }

            if (!collision) {
                return startRow;
            }
        }

        return -1;  // Couldn't place the Tetromino
    }

    /*
    Training Methods
     */
    public int sumHoles() {
        int sum = 0;
        for (int c = 0; c < cols; c++) {
            boolean filledCellFound = false;
            for (int r = 0; r < rows; r++) {
                if (board[r][c] == 1) filledCellFound = true;
                if (filledCellFound && board[r][c] == 0) sum++;
            }
        }
        return sum;
    }

    public int sumHeight() {
        int sum = 0;
        for (int c = 0; c < cols; c++) {
            for (int r = 0; r < rows; r++) {
                if (board[r][c] == 1) {
                    sum += (rows - r);
                    break;
                }
            }
        }
        return sum;
    }

    public int rowFlip() {
        int flips = 0;
        for (int r = 0; r < rows; r++) {
            for (int c = 1; c < cols; c++) {
                if (board[r][c] != board[r][c-1]) flips++;
            }
        }
        return flips;
    }

    public int columnFlip() {
        int flips = 0;
        for (int c = 0; c < cols; c++) {
            for (int r = 1; r < rows; r++) {
                if (board[r][c] != board[r-1][c]) flips++;
            }
        }
        return flips;
    }

    public int pieceHeight(Tetromino tetromino, int rotationIndex, int startCol) {
        int[][] shape = tetromino.getRotation(rotationIndex);
        int startRow = placeTetromino(tetromino, rotationIndex, startCol);
        return startRow >= 0 ? (rows - startRow) : -1;
    }

    public int sumWell() {
        int sum = 0;
        for (int c = 1; c < cols - 1; c++) {
            int wellHeight = 0;
            for (int r = 0; r < rows; r++) {
                if (board[r][c] == 0 && board[r][c-1] == 1 && board[r][c+1] == 1) {
                    wellHeight++;
                }
            }
            sum += wellHeight * wellHeight;
        }
        return sum;
    }
}
