package hw4;

import static hw4.Config.SENDER_LISTEN_PORT;
import static hw4.Config.RECEIVER_LISTEN_PORT;
import static hw4.Util.log;

public class MsgReceiver {
  public static void main(String... args) throws Exception {
    if (args.length != 1) {
      log("Usage: MsgReceiver [transport-layer-name]");
      return;
    }
    String name = args[0];
    log("MsgReceiver start with transport layer " + name);
    TransportLayer t = TransportLayerFactory.create(name, RECEIVER_LISTEN_PORT, SENDER_LISTEN_PORT);
    while (true) {
      byte[] data = t.recv();
      log(new String(data));
    }
  }
}
