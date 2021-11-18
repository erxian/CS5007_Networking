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
  private static final Image GREEN = new ImageIcon("snake/images/green_dot.png").getImage();
  private static final Image PINK = new ImageIcon("snake/images/pink_dot.png").getImage();
  private static final Image APPLE = new ImageIcon("snake/images/apple.png").getImage();
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
    Position p = SnakeFrame.get().getApplePosition();
    render(g, APPLE, p);
  }

  private void renderSnake(Graphics g) {
    SnakeFrame.get()
        .getSnakePosition()
        .stream()
        .forEach(p -> render(g, GREEN, p));
  }

  private void renderSnakeOpponent(Graphics g) {
    SnakeFrame.get()
        .getSnakeOpponentPosition()
        .stream()
        .forEach(p -> render(g, PINK, p));
  }

  private void renderGameOver(Graphics g) {
    g.setColor(Color.RED);
    g.setFont(new Font("TimesRoman", Font.PLAIN, 30));
    g.drawString("Game Over", 80, 50);
  }

  private void renderWinner(Graphics g) {
    g.setColor(Color.GREEN);
    g.setFont(new Font("TimesRoman", Font.PLAIN, 30));
    if (SnakeFrame.get().getWinner() == null) {
      g.drawString("It is a draw", 80, 90);;
    } else {
      g.drawString("winner is " + SnakeFrame.get().getWinner(), 40, 90);
    }
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
