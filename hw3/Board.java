package snake.app;

import static snake.app.Config.BOARD_SIZE;
import static snake.app.Config.UNIT_SIZE;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.util.stream.Stream;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JPanel;


public class Board extends JPanel {
  private static final Image DOT = new ImageIcon("snake/images/dot.png").getImage();
  private static final Image BODY = new ImageIcon("snake/images/oppo.png").getImage();
  private static final Image APPLE = new ImageIcon("snake/images/apple.png").getImage();
  private static final Image HEAD = new ImageIcon("snake/images/head.png").getImage();
  private int STATE = -1;

  public Board() {
    int size = UNIT_SIZE * BOARD_SIZE;
    setPreferredSize(new Dimension(size, size));
    setBorder(BorderFactory.createLineBorder(Color.BLUE));
    setBackground(Color.BLACK);
  }

  public void setState(int state) {
    this.STATE = state;
  }

  @Override
  public void paintComponent(Graphics g) {
    super.paintComponent(g);
      if (STATE == 4) {
        renderWaitingComponent(g);
      } else if (STATE == 5) {
        renderStarting(g);
      } else if (STATE == 7) {
        renderApple(g);
        renderSnake(g);
        renderSnakeOpponent(g);
      } else if (STATE == 6) {
        renderGameOver(g);
        renderWinner(g);
      }
  }

  private void renderApple(Graphics g) {
    //Position p = GameState.get().getApplePosition();
    Position p = SnakeFrame.get().getApplePosition();
    render(g, APPLE, p);
  }

  private void renderSnake(Graphics g) {
    SnakeFrame.get()
        .getSnakePosition()
        .stream()
        .findFirst()
        .ifPresent(p -> render(g, HEAD, p));
    SnakeFrame.get()
        .getSnakePosition()
        .stream()
        .skip(1)
        .forEach(p -> render(g, DOT, p));
  }

  private void renderSnakeOpponent(Graphics g) {
    SnakeFrame.get()
        .getSnakeOpponentPosition()
        .stream()
        .findFirst()
        .ifPresent(p -> render(g, HEAD, p));
    SnakeFrame.get()
        .getSnakeOpponentPosition()
        .stream()
        .skip(1)
        .forEach(p -> render(g, BODY, p));
  }

  private void renderGameOver(Graphics g) {
    g.setColor(Color.RED);
    g.setFont(new Font("TimesRoman", Font.PLAIN, 30));
    g.drawString("Game Over", 80, 50);
  }

  private void renderWinner(Graphics g) {
    if (SnakeFrame.get().getWinner() == null)
      return;
    g.setColor(Color.GREEN);
    g.setFont(new Font("TimesRoman", Font.PLAIN, 30));
    g.drawString(SnakeFrame.get().getWinner() + " win", 70, 90);
  }

  private void renderWaitingComponent(Graphics g) {
    g.setColor(Color.BLUE);
    g.setFont(new Font("TimesRoman", Font.PLAIN, 30));
    g.drawString("waiting for opponet", 50, 50);
  }

  private void renderStarting(Graphics g) {
    g.setColor(Color.RED);
    g.setFont(new Font("TimesRoman", Font.PLAIN, 30));
    g.drawString("game start after 3 s", 50, 50);
  }

  private void render(Graphics g, Image image, Position p) {
    g.drawImage(image, p.getCol() * UNIT_SIZE, p.getRow() * UNIT_SIZE, this);
  }
}
