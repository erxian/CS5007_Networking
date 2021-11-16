package snake.app;

class Config {
  public static final int BOARD_SIZE = 32;
  public static final int UNIT_SIZE = 10;
  public static final int GAME_SPEED_MS = 100;
  public static final int GAME_UPDATE_MS = 50;

  public static final int CREATE_GAME = 1;
  public static final int JOIN_GAME = 2;
  public static final int CHANGE_DIR = 3;
  public static final int WAITING_OPPONENT = 4;
  public static final int WAITING_START = 5;
  public static final int GAME_OVER = 6;
  public static final int UPDATE_STATE = 7;
}
