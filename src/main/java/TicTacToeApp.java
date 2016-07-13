
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 * Tic-Tac-Toe Application against an AI Program
 */

@SuppressWarnings("serial")
public class TicTacToeApp extends Application {

    public static final int ROWS = 3;
    public static final int COLS = 3;

    public static final int CELL_SIZE = 200; // cell width and height (square)

    private Seed[][] board; // Game board of ROWS-by-COLS cells
    private GameState currentState; // the current game state

    private Seed currentPlayer; // the current player
    private AIPlayerMinimax aiPlayer; // the AI player

    private HBox statusBar;
    private Text statusText;
    private Cell[][] cells = new Cell[ROWS][COLS];

    private Parent createContent() {
        Pane root = new Pane();
        root.setPrefSize(600, 650);
        root.setStyle("-fx-background-color: #CFCDD6;");

        for(int row=0; row<ROWS; row++) {
            for(int col=0; col<COLS; col++) {
                root.getChildren().add(cells[row][col]);
            }
        }

        root.setOnMouseClicked(e -> {
            if(e.getButton() == MouseButton.PRIMARY) {
                int mouseX = (int) e.getX();
                int mouseY = (int) e.getY();

                // Don't draw when clicking on the status-bar
                if(mouseX < 600 && mouseY < 600) {
                    // Get the row and column clicked
                    int rowSelected = mouseY / CELL_SIZE;
                    int colSelected = mouseX / CELL_SIZE;

                    draw(rowSelected, colSelected);
                }
            }
        });

        // Setup the status-bar to display status message
        statusBar = new HBox();
        statusBar.setPrefSize(600, 50);
        statusBar.setTranslateX(0);
        statusBar.setTranslateY(600);
        statusBar.setStyle("-fx-background-color: #4652FF;");

        statusText = new Text("X's Turn");
        statusText.setFont(Font.font(20));

        VBox centerBox = new VBox();
        centerBox.setPrefSize(600, 50);
        centerBox.setAlignment(Pos.CENTER);
        centerBox.getChildren().add(statusText);
        statusBar.getChildren().add(centerBox);
        root.getChildren().add(statusBar);

        return root;
    }

    /** Cell Class */
    private class Cell extends StackPane {
        private Text text = new Text();

        public Cell() {
            Rectangle border = new Rectangle(200, 200);
            border.setFill(null);
            border.setStroke(Color.BLACK);

            text.setFont(Font.font(72));

            setAlignment(Pos.CENTER);
            getChildren().addAll(border, text);
        }
    }

    private void draw(int rowSelected, int colSelected) {
        if (currentState == GameState.PLAYING) {
            if (board[rowSelected][colSelected] == Seed.EMPTY) {

                // Player's Move
                board[rowSelected][colSelected] = currentPlayer; // Make a move
                updateGame(currentPlayer, rowSelected, colSelected); // update state
                cells[rowSelected][colSelected].text.setText("X");

                // AI's Move
                if(currentState == GameState.PLAYING) {
                    int[] t = aiPlayer.move();
                    int row = t[0];
                    int col = t[1];
                    board[row][col] = aiPlayer.aiSeed;
                    updateGame(aiPlayer.aiSeed, row, col);
                    cells[row][col].text.setText("O");
                }
            }
        } else { // game over
            initGame(); // restart the game
        }
        printStatus();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Scene scene = new Scene(createContent());
        primaryStage.setScene(scene);
        primaryStage.setTitle("Tic Tac Toe vs AI");
        primaryStage.setOnCloseRequest(e -> Platform.exit());

        primaryStage.show();

        new Thread(new Runnable() {
            @Override
            public void run() {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        new TicTacToeApp();
                    }
                });
            }
        }).start();
    }

    /** Constructor to setup the game */
    public TicTacToeApp() {
        board = new Seed[ROWS][COLS];
        initGame(); // initialize the game board contents and game variables
    }

    /** Print status-bar message */
    public void printStatus() {
        if (currentState == GameState.PLAYING) {
            if (currentPlayer == Seed.CROSS) {
                statusText.setText("X's Turn");
            } else {
                statusText.setText("O's Turn");
            }
        } else if (currentState == GameState.DRAW) {
            statusText.setText("It's a Draw! Click to play again.");
        } else if (currentState == GameState.CROSS_WON) {
            statusText.setText("'X' Won! Click to play again.");
        } else if (currentState == GameState.NOUGHT_WON) {
            statusText.setText("'O' Won! Click to play again.");
        }
    }

    /** Initialize the game-board contents and the status */
    public void initGame() {
        for (int row = 0; row < ROWS; ++row) {
            for (int col = 0; col < COLS; ++col) {
                board[row][col] = Seed.EMPTY; // all cells empty

                if(cells[row][col] == null) {
                    Cell cell = new Cell();
                    cell.text.setText("");
                    cell.setTranslateX(col * 200);
                    cell.setTranslateY(row * 200);
                    cells[row][col] = cell;
                } else {
                    cells[row][col].text.setText("");
                }


            }
        }
        currentState = GameState.PLAYING; // ready to play
        currentPlayer = Seed.CROSS;       // cross plays first
        aiPlayer = new AIPlayerMinimax(board);
        aiPlayer.setSeed(Seed.NOUGHT);
    }

    public void updateGame(Seed theSeed, int rowSelected, int colSelected) {
        if (hasWon(theSeed, rowSelected, colSelected)) { // check for win
            currentState = (theSeed == Seed.CROSS) ? GameState.CROSS_WON : GameState.NOUGHT_WON;
        } else if (isDraw()) { // check for draw
            currentState = GameState.DRAW;
        }
        // Otherwise, no change to current state
    }

    public boolean isDraw() {
        for (int row = 0; row < ROWS; ++row) {
            for (int col = 0; col < COLS; ++col) {
                if (board[row][col] == Seed.EMPTY) {
                    return false; // an empty cell found, not draw
                }
            }
        }
        return true;
    }

    public boolean hasWon(Seed theSeed, int rowSelected, int colSelected) {
        return (board[rowSelected][0] == theSeed &&  // 3-in-the-row
                board[rowSelected][1] == theSeed &&
                board[rowSelected][2] == theSeed ||
                board[0][colSelected] == theSeed &&  // 3-in-the-column
                board[1][colSelected] == theSeed &&
                board[2][colSelected] == theSeed ||
                rowSelected == colSelected &&        // 3-in-the-diagonal
                board[0][0] == theSeed &&
                board[1][1] == theSeed &&
                board[2][2] == theSeed ||
                rowSelected + colSelected == 2 &&   // 3-in-the-opposite-diagonal
                board[0][2] == theSeed &&
                board[1][1] == theSeed &&
                board[2][0] == theSeed);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
