package snake.app;

import java.util.*;
import java.io.ByteArrayOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.io.IOException;
import java.lang.*;

import static snake.app.Config.GAME_SPEED_MS;
import static snake.app.Config.GAME_UPDATE_MS;
import static snake.app.Config.CREATE_GAME;
import static snake.app.Config.JOIN_GAME;
import static snake.app.Config.CHANGE_DIR;
import static snake.app.Config.WAITING_OPPONENT;
import static snake.app.Config.WAITING_START;
import static snake.app.Config.GAME_OVER;
import static snake.app.Config.UPDATE_STATE;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;


public class SnakeAppServer {
  private static final int PORT = 8080;
  private static final int BUF_SIZE = 1024;
  private List<Integer> portList;
  private List<InetAddress> addrList;
  private final ScheduledExecutorService scheduler;
  private DatagramSocket serverSocket;
  private int opt = -1; //output type initialized with -1
  private CreateProtocol snake_params;
  private CreateProtocol snake_oppo_params;

  // covert an int to byte array, using two bytes to store the value
  private static byte[] intToByteArray(int value) {
      return new byte[] {
          (byte)value }; 
  }

  // this is a helper function, print byte array
  private String print(byte[] bytes) {
      StringBuilder sb = new StringBuilder();
      sb.append("[ ");
      for (byte b : bytes) {
          sb.append(String.format("0x%02X ", b));
      }
      sb.append("]"); return sb.toString();
  }

  private byte[] sendGameState() throws IOException {
        if (GameState.get().isGameOver()) {
            return sendGameOver();
        }
        // get game state
        byte[] sequence_num = intToByteArray(1);
        Position apple = GameState.get().getApplePosition();
        byte[] row = intToByteArray(apple.getRow());
        byte[] col = intToByteArray(apple.getCol());

        GameState.get().moveSnake();
        GameState.get().moveSnakeOpponent();
        byte[] snake_bitmap = GameState.get().getSnakeBitmap();
        byte[] snakeOppo_bitmap = GameState.get().getSnakeOpponentBitmap();

        //LinkedList<Position> snake = GameState.get().getSnakePosition();
        //snake.stream().forEach(s -> System.out.println(s.toString()));

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
        outputStream.write(intToByteArray(opt));
        outputStream.write(sequence_num);
        outputStream.write(row);
        outputStream.write(col);
        outputStream.write(snake_bitmap);
        outputStream.write(snakeOppo_bitmap);

        return outputStream.toByteArray();
  }

  private byte[] sendGameOver() throws IOException {
    this.opt = GAME_OVER;
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
    int result = GameState.get().isDraw() == true ? 0 : 1;
    outputStream.write(intToByteArray(opt));
    outputStream.write(intToByteArray(result));

    if (result == 1) {
      if (GameState.get().isSnakeWin()) {
        outputStream.write(intToByteArray(snake_params.getNickName().length()));
        outputStream.write(snake_params.getNickName().getBytes());
      } else {
        outputStream.write(intToByteArray(snake_oppo_params.getNickName().length()));
        outputStream.write(snake_oppo_params.getNickName().getBytes());
      }
    }
    return outputStream.toByteArray();
  }

  private void sendOnce() {
      byte[] state_info = intToByteArray(opt);
      try {
        if (opt == UPDATE_STATE) {
          state_info = sendGameState();
        }
        for(int i=0; i<portList.size(); i++) {
            DatagramPacket statePkt = new DatagramPacket(state_info,
                    state_info.length, addrList.get(i), portList.get(i));
            serverSocket.send(statePkt);
        }
      } catch (IOException e) {
          e.printStackTrace();
      }
  }

  private void moveUpdate() {
      GameState.get().moveSnake();
      GameState.get().moveSnakeOpponent();
  }

  private boolean isValid() {
    if (!snake_oppo_params.getGameId().equals(snake_params.getGameId()) || 
        snake_oppo_params.getNickName().equals(snake_params.getNickName())) {
      return false;
    }
    return true;
  }

  private void updateDirection(MoveProtocol player) {
    String dir = player.getDir();
    Direction newDir = Direction.random();
    switch(dir) {
      case "U":
        newDir = Direction.UP;
        break;
      case "R":
        newDir = Direction.RIGHT;
        break;
      case "D":
        newDir = Direction.DOWN;
        break;
      case "L":
        newDir = Direction.LEFT;
        break;
    }

    //System.out.println("player :" + player.getNickName() + ", create: " + snake_params.getNickName());
    if (player.getNickName().equals(snake_params.getNickName())) {
      //System.out.println("snake direction changed");
      GameState.get().updateSnakeDirection(newDir);
    } else if (player.getNickName().equals(snake_oppo_params.getNickName())) {
      //System.out.println("snake opponent direction changed");
      GameState.get().updateSnakeOppoDirection(newDir);
    }
  }

  SnakeAppServer() {
      portList = new ArrayList<>();
      addrList = new ArrayList<>();
      snake_params = new CreateProtocol();
      snake_oppo_params = new CreateProtocol();
      //scheduler = Executors.newSingleThreadScheduledExecutor();
      scheduler = Executors.newScheduledThreadPool(3);

      scheduler.scheduleAtFixedRate(this::sendOnce, /* initialDelay */ 0,
        GAME_SPEED_MS, MILLISECONDS);

      //scheduler.scheduleAtFixedRate(this::moveUpdate, /* initialDelay */ 0,
      //  GAME_SPEED_MS, MILLISECONDS);
      
      try {
          serverSocket = new DatagramSocket(PORT);
          System.out.println("UDP Snake server");
          System.out.println("send message to client every 100ms");
          
          while (true) {
              byte[] buf = new byte[BUF_SIZE];
              DatagramPacket pkt = new DatagramPacket(buf, BUF_SIZE);
              serverSocket.receive(pkt);
              InetAddress addr = pkt.getAddress();
              int port = pkt.getPort();
              System.out.println("port is: " + String.valueOf(port));
              if (portList.size() <= 2) {
                addrList.add(addr);     
                portList.add(port);
              }
              byte[] data = pkt.getData(); 
              byte num = data[0];
              int input_type = num & 0xFF;
              switch(input_type) {
                  case CREATE_GAME:
                      snake_params = this.snake_params.decodeBytes(data);
                      System.out.println(snake_params.toString());
                      opt = WAITING_OPPONENT;
                      break;
                  case JOIN_GAME:
                      snake_oppo_params = this.snake_oppo_params.decodeBytes(data);
                      System.out.println(snake_oppo_params.toString());
                      if (!isValid()) {
                        addrList.remove(1);     
                        portList.remove(1);
                        break;
                      }
                      opt = WAITING_START;
                      Thread.sleep(3000);
                      opt = UPDATE_STATE;
                      break;
                  case CHANGE_DIR:
                      MoveProtocol whoami = new MoveProtocol();
                      whoami = whoami.decodeBytes(data);
                      updateDirection(whoami);
                      break;
                  default:
                      System.out.println("Wrong Commands for SnakeApp");
                      break;
              }
          }
      } catch (IOException e) {
          e.printStackTrace();
      } catch (InterruptedException e) {
          System.out.println("I was interrupted!");
          e.printStackTrace();
      }
    }  
}
