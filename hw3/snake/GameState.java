package snake.app;

import static snake.app.Config.BOARD_SIZE;
import java.util.Arrays;
import java.util.List;
import java.io.IOException;
import java.util.LinkedList;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


class GameState {
  private static GameState INSTANCE;

  private Position apple;
  private volatile LinkedList<Position> snake;
  private volatile LinkedList<Position> snake_opponent;
  private Direction dir;
  private Direction opponent_dir;
  private boolean gameOver;
  private boolean snake_win = true;
  private boolean snake_opponent_win = true;
  //private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
  //private Lock readLock = lock.readLock();
  //private Lock writeLock = lock.writeLock();

  private GameState() {
    dir = Direction.random();
    opponent_dir = Direction.random();
    initSnake();
    initSnakeOpponent();
    generateApple();
    dir = oppositeDirection(dir);
    opponent_dir = oppositeDirection(opponent_dir);
    gameOver = false;
  }

  public static synchronized GameState get() {
    if (INSTANCE == null) {
      INSTANCE = new GameState();
    }
    return INSTANCE;
  }

  private Direction oppositeDirection(Direction currDir) {
    switch(currDir) {
            case UP:
                return Direction.DOWN;
            case RIGHT:
                return Direction.LEFT;
            case DOWN:
                return Direction.UP;
            case LEFT:
                return Direction.RIGHT;
        }
        return null;
  }

  public Position getApplePosition() {
    return apple;
  }

  public LinkedList<Position> getSnakePosition() {
    return this.snake;
  }

  public byte[] getSnakeBitmap() throws IOException {
    Bitmap snake_bitmap = new Bitmap(snake);
    return snake_bitmap.generateBitmaps();
  }

  public LinkedList<Position> getSnakeOpponentPosition() {
    //readLock.lock();
    //try {
      return this.snake_opponent;
    //} finally {
    //  readLock.unlock();
    //}
  }

  public byte[] getSnakeOpponentBitmap() throws IOException {
    Bitmap snakeOppo_bitmap = new Bitmap(snake_opponent);
    return snakeOppo_bitmap.generateBitmaps();
  }

  public boolean isGameOver() {
    return gameOver;
  }

  public boolean isDraw() {
    return snake_win && snake_opponent_win;
  }

  public boolean isSnakeWin() {
    return snake_win;
  }

  public boolean isSnakeOpponentWin() {
    return snake_opponent_win;
  }

  public void updateSnakeDirection(Direction newDir) {
    Position next = Position.copy(snake.getFirst()).move(newDir);
    if (next.equals(snake.get(1))) {
      return;
    }
    dir = newDir;
  }

  public void updateSnakeOppoDirection(Direction newDir) {
    Position next = Position.copy(snake_opponent.getFirst()).move(newDir);
    if (next.equals(snake_opponent.get(1))) {
      return;
    }
    opponent_dir = newDir;
  }

  public void moveSnake() {
    if (this.gameOver) {
      return;
    }
    Position nextHead = Position.copy(this.snake.getFirst()).move(this.dir);
    if (isInSnakeBody(nextHead) || isInSnakeOpponentBody(nextHead) ||
          isCollideWindow(nextHead)) {
      this.gameOver = true;
      snake_win = false;
      return;
    }

    if (isInSnakeOpponentHead(nextHead)) {
      this.gameOver = true;
      return;
    }
    //writeLock.lock();
    //try {
      this.snake.addFirst(nextHead);
      if (nextHead.equals(this.apple)) {
        generateApple();
        return;
      }
      this.snake.removeLast();
    //} finally {
    //  writeLock.unlock();
    //}
  }

  public void moveSnakeOpponent() {
    if (this.gameOver) {
      return;
    }
    Position nextHead = Position.copy(snake_opponent.getFirst()).move(opponent_dir);
    if (isInSnakeOpponentBody(nextHead) || isInSnakeBody(nextHead) ||
          isCollideWindow(nextHead)) {
      this.gameOver = true;
      snake_opponent_win = false;
      return;
    }

    if (isInSnakeHead(nextHead)) {
      gameOver = true;
      return;
    }
    //writeLock.unlock();
    //try {
      snake_opponent.addFirst(nextHead);
      if (nextHead.equals(apple)) {
        generateApple();
        return;
      }
      snake_opponent.removeLast();
    //  } finally {
    //  writeLock.unlock();
    //}
  }

  private void initSnake() {
    Position head = Position.random(BOARD_SIZE);
    this.snake = Stream.of(head,
                      Position.copy(head).move(this.dir),
                      Position.copy(head).move(this.dir).move(this.dir))
        .collect(Collectors.toCollection(LinkedList::new));
  }

  private void initSnakeOpponent() {
    Position head = Position.random(BOARD_SIZE);
    this.snake_opponent = Stream.of(head,
                      Position.copy(head).move(this.opponent_dir),
                      Position.copy(head).move(this.opponent_dir).move(this.opponent_dir))
        .collect(Collectors.toCollection(LinkedList::new));
  }


  private void generateApple() {
    do {
      apple = Position.random(BOARD_SIZE);
    } while (isInSnake(apple) || isInSnakeOpponent(apple));
  }

  private boolean isInSnake(Position o) {
    return snake.stream().anyMatch(p -> p.equals(o));
  }

  private boolean isInSnakeOpponent(Position o) {
    return snake_opponent.stream().anyMatch(p -> p.equals(o));
  }

  private boolean isInSnakeHead(Position o) {
    if (snake.getFirst().equals(o)) {
      return true;
    }
    return false;
  }

  private boolean isInSnakeBody(Position o) {
    for (Position p : snake) {
      if (p.equals(snake.getFirst())) {
        continue;
      }
      if (p.equals(o)) {
        return true;
      }
    }
    return false;
  }

  private boolean isInSnakeOpponentHead(Position o) {
    if (snake_opponent.getFirst().equals(o)) {
      return true;
    }
    return false;
  }

  private boolean isInSnakeOpponentBody(Position o) {
    for (Position p : snake_opponent) {
      if (p.equals(snake_opponent.getFirst())) {
        continue;
      }
      if (p.equals(o)) {
        return true;
      }
    }
    return false;
  }

  private boolean isCollideWindow(Position o) {
    if (o.getRow() == 0 || o.getRow() == (BOARD_SIZE-1) ||
      o.getCol() == 0 || o.getCol() == (BOARD_SIZE-1)) {
        return true;
      }
    return false;
  }
}
