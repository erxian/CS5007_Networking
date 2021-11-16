package snake.app;

class SnakeApp {

  SnakeApp() {

  }

  public static void main(String[] args) {
      String action = args[0];
      switch(action) {
          case "start_server":
              System.out.println("starting server");
              SnakeAppServer server = new SnakeAppServer();
              break;
          case "create":
              SnakeAppClient player = new SnakeAppClient(args);
              break;
          case "join":
              SnakeAppClient opponet = new SnakeAppClient(args);
              break;
          default:
              System.out.println("Invalid Commonds for SnakeApp");
              break;
      }
   }
}
