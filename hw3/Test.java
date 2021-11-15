//package snake.app;

import java.util.*;
import java.math.BigInteger;
import java.lang.Short;
import java.nio.ByteBuffer;

//import snake.app.Position;
//import static snake.app.Config.BOARD_SIZE;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.io.ByteArrayOutputStream;
import java.util.concurrent.ThreadLocalRandom;
import java.io.IOException;


class Position {
  private int row;
  private int col;
  private int size;

  private Position(int row, int col, int size) {
    this.row = row;
    this.col = col;
    this.size = size;
  }

  public static Position random(int size) {
    return new Position(ThreadLocalRandom.current().nextInt(size),
                        ThreadLocalRandom.current().nextInt(size),
                        size);
  }

  public int getRow() {
    return row;
  }

  public int getCol() {
    return col;
  }

  public static Position set(int row, int col, int size) {
    return new Position(row, col, size);
  }

  @Override
  public String toString() {
    return String.format("[%d,%d]", row, col);
  }
}


class Test {
	//private LinkedList<Position> position;
	//private ByteArrayOutputStream outputStream;
	private int BOARD_SIZE = 32;

  Test() {
    //this.position = parseBitmap(bitmap);
  }

  public void parseBitmap(byte[] bitmap) {
    //LinkedList<Position> snake = new LinkedList<>();
    String bitString = toBitString(bitmap);
    for (int i=0; i<BOARD_SIZE; i++) {
      for (int j=0; j<BOARD_SIZE; j++) {
        if(bitString.charAt(j + BOARD_SIZE*i) == '1')
          //Position pos = Position.set(i, j, BOARD_SIZE);
          //snake.add(pos);
          System.out.println(String.format("[%d,%d]", i, j));
      }
    }
    //snake.stream().forEach(s -> System.out.println(s.toString()));
    //return snake;
  }


  public String toBitString(byte[] b) {
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

	public static void main(String[] args) {
    byte[] bitmap = new byte[]{0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x02, 0x00, 0x00, 0x00, 0x02, 0x00, 0x00, 0x00, 0x02, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
    Test test = new Test();
    System.out.println(test.toBitString(bitmap));
    test.parseBitmap(bitmap);
	}
}