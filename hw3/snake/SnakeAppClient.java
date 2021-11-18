package snake.app;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.io.IOException;
import java.util.Scanner;
import java.io.ByteArrayOutputStream;

import static snake.app.Config.GAME_SPEED_MS;
import static snake.app.Config.WAITING_OPPONENT;
import static snake.app.Config.WAITING_START;
import static snake.app.Config.GAME_OVER;
import static snake.app.Config.UPDATE_STATE;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class SnakeAppClient {
  private static final int SERVER_PORT = 8080;
  private static final int BUF_SIZE = 1024;
  private final ScheduledExecutorService scheduler;
  private boolean has_opponent = false;
  private boolean has_two_player = false;
  private boolean game_over = false;
  private String game_id = null;
  private String nick_name = null;
  private DatagramSocket clientSocket;
  private byte[] game_state_data = null;
  private int count = 0;

  private void sendDirection() {
      if (SnakeFrame.get().hasUpdate() == count) {
        // no direction change
        return;
      }
      try {
        MoveProtocol dirPkt = new MoveProtocol(game_id, nick_name);
        dirPkt.setDir(SnakeFrame.get().getNewDir());
        byte[] dir_message = dirPkt.parseArgs();
        InetAddress addr = InetAddress.getByName("localhost");
        DatagramPacket pkt = new DatagramPacket(dir_message,
                  dir_message.length, addr, SERVER_PORT);
        clientSocket.send(pkt);
        count = SnakeFrame.get().hasUpdate();
      } catch (IOException e) {
        e.printStackTrace();
      }
  }

  SnakeAppClient(String[] args) {
    System.out.println("UDP Snake client");
    scheduler = Executors.newSingleThreadScheduledExecutor();
    //scheduler = Executors.newScheduledThreadPool(3);

    scheduler.scheduleAtFixedRate(this::sendDirection, /* initialDelay */ 0,
            GAME_SPEED_MS, MILLISECONDS);
    try {
        // addr is server ip address
        InetAddress addr = InetAddress.getByName("localhost");
        CreateProtocol player = new CreateProtocol(args);
        byte[] message = player.parseArgs();
        clientSocket = new DatagramSocket(player.getPort());
        game_id = player.getGameId();
        nick_name = player.getNickName();
        DatagramPacket pkt = new DatagramPacket(message,
                message.length, addr, SERVER_PORT);
        clientSocket.send(pkt);
        System.out.println("send success");
        byte[] buf = new byte[BUF_SIZE];
        DatagramPacket receive_pkt = new DatagramPacket(buf, BUF_SIZE);

        SnakeFrame.get().setVisible(true);
        while(true) {
            clientSocket.receive(receive_pkt);
            byte[] data = receive_pkt.getData();
            byte num = data[0];
            int input_type =  num & 0xFF;
            switch(input_type) {
                case WAITING_OPPONENT:
                    if (!has_opponent) {
                        SnakeFrame.get().runOnce(input_type);
                        has_opponent = true;
                    }
                    break;
                case WAITING_START:
                    if (!has_two_player) {
                        SnakeFrame.get().runOnce(input_type);
                        has_two_player = true;
                    }
                    break;
                case GAME_OVER:
                    if (!game_over) {
                        SnakeFrame.get().runGameOver(data);
                        game_over = true;
                    }
                    break;
                case UPDATE_STATE:
                    SnakeFrame.get().runRepeat(data);
                    break;
                default:
                    System.out.println("Game Not Created Yet");
                    break;
            }
        }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
