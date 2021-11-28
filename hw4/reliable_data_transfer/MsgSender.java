package hw4;

import static hw4.Config.SENDER_LISTEN_PORT;
import static hw4.Config.RECEIVER_LISTEN_PORT;
import static hw4.Util.log;

public class MsgSender {
  public static void main(String... args) throws Exception {
    if (args.length != 1) {
      log("Usage: MsgSender [transport-layer-name]");
      return;
    }
    String name = args[0];
    log("MsgSender start with transport layer " + name);
    TransportLayer t = TransportLayerFactory.create(name, SENDER_LISTEN_PORT, RECEIVER_LISTEN_PORT);
    for (int i = 0; i < 20; ++i) {
      String msg = "MSG:" + i;
      log(msg);
      t.send(msg.getBytes());
    }
    t.close();
    log("MsgSender complete");
  }
}
