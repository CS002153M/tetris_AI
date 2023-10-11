package net.swofty.tetris;

public class Field {
    private int rows = 20;
    private int cols = 10;
    private int[][] board;

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
    }

    public Field() {
        board = new int[rows][cols];
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
        return startRow;  // return the row where the Tetromino was placed; -1 if it couldn't be placed
    }

    // Check for complete lines and clear them
    public void clearLines() {
        for (int r = 0; r < rows; r++) {
            boolean lineComplete = true;
            for (int c = 0; c < cols; c++) {
                if (board[r][c] == 0) {
                    lineComplete = false;
                    break;
                }
            }
            if (lineComplete) {
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
    }

    // Get value at a specific point
    public int getValueAt(int row, int col) {
        return board[row][col];
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
}
