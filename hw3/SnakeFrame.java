package snake.app;

import static snake.app.Config.GAME_SPEED_MS;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import javax.swing.JFrame;
import java.io.UnsupportedEncodingException;

import static snake.app.Config.BOARD_SIZE;


class SnakeFrame extends JFrame {
  //private final ScheduledExecutorService scheduler;
  private final Board board;
  private static SnakeFrame INSTANCE;
  private Direction dir;
  private int DIR_UPDATE;
  private boolean gameOver;
  private Position apple;
  private LinkedList<Position> snake;
  private Position pos;
  private LinkedList<Position> snake_opponent;
  private String winner;

  SnakeFrame() {
    setTitle("Snake Game");
    board = new Board();
    add(board);
    setResizable(false);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    // Size the window so that all its contents are at or above their
    // preferred sizes.
    pack();
    dir = null; // initial previous dir as null
    winner = null;
    DIR_UPDATE = 0;
    addKeyListener(new DirectionChangeHandler());
    gameOver = false;
    apple = Position.random(BOARD_SIZE);
    pos = Position.random(BOARD_SIZE);
    snake = new LinkedList<>();
    snake_opponent = new LinkedList<>();
  }

  public static synchronized SnakeFrame get() {
    if (INSTANCE == null) {
      INSTANCE = new SnakeFrame();
    }
    return INSTANCE;
  }

  public void updateDirection(Direction newDir) {
      System.out.println(newDir.name());
      if (!newDir.equals(this.dir)) {
        this.dir = newDir;
        this.DIR_UPDATE += 1;
      }
  }

  public int hasUpdate() {
      return this.DIR_UPDATE;
  }

  public Direction getNewDir() {
    return this.dir;
  }

  private void setApplePosition(int row, int col) {
    this.apple = Position.set(row, col, BOARD_SIZE);
  }

  public Position getApplePosition() {
    return this.apple;
  }

  private void setSnakePositon(byte[] snake_bitmap) {
    this.snake = generatePosition(snake_bitmap);
    //snake.stream().forEach(s -> System.out.println(s.toString()));
  }

  private void setSnakeOppoPositon(byte[] snakeOppo_bitmap) {
    this.snake_opponent = generatePosition(snakeOppo_bitmap);
    //snake.stream().forEach(s -> System.out.println(s.toString()));
  }

  private LinkedList<Position> generatePosition (byte[] bitmap) {
    String bitString = toBitString(bitmap);
    LinkedList<Position> list = new LinkedList<>();
    for (int i=0; i<BOARD_SIZE; i++) {
      for (int j=0; j<BOARD_SIZE; j++) {
        if(bitString.charAt(j + BOARD_SIZE*i) == '1') {
          pos = Position.set(i, j, BOARD_SIZE);
          list.add(pos);
          //System.out.println(String.format("[%d,%d]", i, j));
        }
      }
    }
    return list;
  }

  public LinkedList<Position> getSnakePosition() {
    return this.snake;
  }

  public LinkedList<Position> getSnakeOpponentPosition() {
    return this.snake_opponent;
  }

  private String toBitString(byte[] b) {
    char[] bits = new char[8 * b.length];
    for(int i = 0; i < b.length; i++) {
        final byte byteval = b[i];
        int bytei = i << 3;
        int mask = 0x1;
        for(int j = 7; j >= 0; j--) {
            final int bitval = byteval & mask;
            if(bitval == 0) {
                bits[bytei + j] = '0';
            } else {
                bits[bytei + j] = '1';
            }
            mask <<= 1;
        }
    }
    return String.valueOf(bits);
  }

    // this is a helper function, print byte array
  String print(byte[] bytes) {
      StringBuilder sb = new StringBuilder();
      sb.append("[ ");
      for (byte b : bytes) {
          sb.append(String.format("0x%02X, ", b));
      }
      sb.append("]"); return sb.toString();
    }

  public void runOnce(int state) {
    board.setState(state);
    board.repaint();
  }

  public void runGameOver(byte[] data) throws
            UnsupportedEncodingException {
    int state = data[0] & 0xFF;
    int result = data[1] & 0xFF;
    if (result == 1) {
      int len = data[2] & 0xFF;
      this.winner = new String(
                Arrays.copyOfRange(data, 3, 3+len), "UTF-8");
    }
    board.setState(state);
    board.repaint();
  }

  public String getWinner() {
    return this.winner;
  }

  public void runRepeat(byte[] data) {
    int state = data[0] & 0xFF;
    int sequence_num = data[1] & 0xFF;
    int row = data[2] & 0xFF;
    int col = data[3] & 0xFF;
    byte[] snake_bitmap = Arrays.copyOfRange(data, 4, 132);
    byte[] snakeOppo_bitmap = Arrays.copyOfRange(data, 132, 260);
    setApplePosition(row, col);
    setSnakePositon(snake_bitmap);
    setSnakeOppoPositon(snakeOppo_bitmap);
    board.setState(state);
    board.repaint();
  }
}
