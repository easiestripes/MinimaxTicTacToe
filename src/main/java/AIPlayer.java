
public abstract class AIPlayer {

    protected int ROWS = 3;
    protected int COLS = 3;
    protected Seed[][] board;
    protected Seed aiSeed;
    protected Seed playerSeed;

    public AIPlayer(Seed[][] board) {
        this.board = board;
    }

    /** Set/change the seed used by computer and opponent */
    public void setSeed(Seed seed) {
        this.aiSeed = seed;
        playerSeed = (aiSeed == Seed.CROSS) ? Seed.NOUGHT : Seed.CROSS;
    }

    /** Abstract method to get next move. Return int[2] of {row, col} */
    abstract int[] move();  // to be implemented by subclasses
}
