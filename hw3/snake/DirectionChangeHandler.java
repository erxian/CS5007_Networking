package snake.app;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;


class DirectionChangeHandler extends KeyAdapter {
  @Override
  public void keyPressed(KeyEvent e) {
    switch (e.getKeyCode()) {
      case KeyEvent.VK_LEFT:
        SnakeFrame.get().updateDirection(Direction.LEFT);
        break;
      case KeyEvent.VK_RIGHT:
        SnakeFrame.get().updateDirection(Direction.RIGHT);
        break;
      case KeyEvent.VK_UP:
        SnakeFrame.get().updateDirection(Direction.UP);
        break;
      case KeyEvent.VK_DOWN:
        SnakeFrame.get().updateDirection(Direction.DOWN);
        break;
    }
  }
}
