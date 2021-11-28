package hw4;

import static hw4.Config.SENDER_LISTEN_PORT;
import static hw4.Config.RECEIVER_LISTEN_PORT;
import static hw4.Util.log;

import java.io.FileOutputStream;
import java.io.OutputStream;

public class FileReceiver {
  public static void main(String... args) throws Exception {
    if (args.length != 2) {
      log("Usage: FileReceiver [transport-layer-name] [file-name]");
      return;
    }
    String name = args[0];
    String file = args[1];
    log("FileReceiver start");
    TransportLayer t = TransportLayerFactory.create(name, RECEIVER_LISTEN_PORT, SENDER_LISTEN_PORT);
    OutputStream outputStream = new FileOutputStream(file);
    while (true) {
      byte[] data = t.recv();
      //log("MSG of length " + data.length);
      outputStream.write(data);
      outputStream.flush();
    }
  }
}
