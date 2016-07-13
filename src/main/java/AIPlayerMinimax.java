
import java.util.*;

/** AIPlayer using Minimax algorithm */
public class AIPlayerMinimax extends AIPlayer {

    /** Constructor with the given game board */
    public AIPlayerMinimax(Seed[][] board) {
        super(board);
    }

    /** Get next best move for computer. Return int[2] of {row, col} */
    @Override
    int[] move() {
        int[] result = minimax(2, aiSeed); // depth, max turn
        return new int[] {result[1], result[2]};   // row, col
    }

    /** Recursive minimax at level of depth for either maximizing or minimizing player.
     Return int[3] of {score, row, col}  */
    private int[] minimax(int depth, Seed player) {
        // Generate possible next moves in a List of int[2] of {row, col}.
        List<int[]> nextMoves = generateMoves();

        // aiSeed is maximizing; while playerSeed is minimizing
        int bestScore = (player == aiSeed) ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        int currentScore;
        int bestRow = -1;
        int bestCol = -1;

        if (nextMoves.isEmpty() || depth == 0) {
            // Gameover or depth reached, evaluate score
            bestScore = evaluate();
        } else {
            for (int[] move : nextMoves) {
                // Try this move for the current "player"
                this.board[move[0]][move[1]] = player;
                if (player == aiSeed) {  // aiSeed (computer) is maximizing player
                    currentScore = minimax(depth - 1, playerSeed)[0];
                    if (currentScore > bestScore) {
                        bestScore = currentScore;
                        bestRow = move[0];
                        bestCol = move[1];
                    }
                } else {  // playerSeed is minimizing player
                    currentScore = minimax(depth - 1, aiSeed)[0];
                    if (currentScore < bestScore) {
                        bestScore = currentScore;
                        bestRow = move[0];
                        bestCol = move[1];
                    }
                }
                // Undo move
                this.board[move[0]][move[1]] = Seed.EMPTY;
            }
        }
        return new int[] {bestScore, bestRow, bestCol};
    }

    /** Find all valid next moves.
     Return List of moves in int[2] of {row, col} or empty list if gameover */
    private List<int[]> generateMoves() {
        List<int[]> nextMoves = new ArrayList<int[]>(); // allocate List

        // If gameover, i.e., no next move
        if (hasWon(aiSeed) || hasWon(playerSeed)) {
            return nextMoves;   // return empty list
        }

        // Search for empty cells and add to the List
        for (int row = 0; row < ROWS; ++row) {
            for (int col = 0; col < COLS; ++col) {
                if (this.board[row][col] == Seed.EMPTY) {
                    nextMoves.add(new int[] {row, col});
                }
            }
        }
        return nextMoves;
    }

    /** The heuristic evaluation function for the current board
     @Return +100, +10, +1 for EACH 3-, 2-, 1-in-a-line for computer.
     -100, -10, -1 for EACH 3-, 2-, 1-in-a-line for opponent.
     0 otherwise   */
    private int evaluate() {
        int score = 0;
        // Evaluate score for each of the 8 lines (3 rows, 3 columns, 2 diagonals)
        score += evaluateLine(0, 0, 0, 1, 0, 2);  // row 0
        score += evaluateLine(1, 0, 1, 1, 1, 2);  // row 1
        score += evaluateLine(2, 0, 2, 1, 2, 2);  // row 2
        score += evaluateLine(0, 0, 1, 0, 2, 0);  // col 0
        score += evaluateLine(0, 1, 1, 1, 2, 1);  // col 1
        score += evaluateLine(0, 2, 1, 2, 2, 2);  // col 2
        score += evaluateLine(0, 0, 1, 1, 2, 2);  // diagonal
        score += evaluateLine(0, 2, 1, 1, 2, 0);  // alternate diagonal
        return score;
    }

    /** The heuristic evaluation function for the given line of 3 cells
     @Return +100, +10, +1 for 3-, 2-, 1-in-a-line for computer.
     -100, -10, -1 for 3-, 2-, 1-in-a-line for opponent.
     0 otherwise */
    private int evaluateLine(int row1, int col1, int row2, int col2, int row3, int col3) {
        int score = 0;

        // First cell
        if (this.board[row1][col1] == aiSeed) {
            score = 1;
        } else if (this.board[row1][col1] == playerSeed) {
            score = -1;
        }

        // Second cell
        if (this.board[row2][col2] == aiSeed) {
            if (score == 1) {   // cell1 is aiSeed
                score = 10;
            } else if (score == -1) {  // cell1 is playerSeed
                return 0;
            } else {  // cell1 is empty
                score = 1;
            }
        } else if (this.board[row2][col2] == playerSeed) {
            if (score == -1) { // cell1 is playerSeed
                score = -10;
            } else if (score == 1) { // cell1 is aiSeed
                return 0;
            } else {  // cell1 is empty
                score = -1;
            }
        }

        // Third cell
        if (this.board[row3][col3] == aiSeed) {
            if (score > 0) {  // cell1 and/or cell2 is aiSeed
                score *= 10;
            } else if (score < 0) {  // cell1 and/or cell2 is playerSeed
                return 0;
            } else {  // cell1 and cell2 are empty
                score = 1;
            }
        } else if (this.board[row3][col3] == playerSeed) {
            if (score < 0) {  // cell1 and/or cell2 is playerSeed
                score *= 10;
            } else if (score > 1) {  // cell1 and/or cell2 is aiSeed
                return 0;
            } else {  // cell1 and cell2 are empty
                score = -1;
            }
        }
        return score;
    }

    private int[] winningPatterns = {
            0b111000000, 0b000111000, 0b000000111, // rows
            0b100100100, 0b010010010, 0b001001001, // cols
            0b100010001, 0b001010100               // diagonals
    };

    /** Returns true if thePlayer wins */
    private boolean hasWon(Seed thePlayer) {
        int pattern = 0b000000000;  // 9-bit pattern for the 9 cells
        for (int row = 0; row < ROWS; ++row) {
            for (int col = 0; col < COLS; ++col) {
                if (this.board[row][col] == thePlayer) {
                    pattern |= (1 << (row * COLS + col));
                }
            }
        }
        for (int winningPattern : winningPatterns) {
            if ((pattern & winningPattern) == winningPattern) return true;
        }
        return false;
    }
}