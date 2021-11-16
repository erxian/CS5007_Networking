package snake.app;

import java.util.*;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;

public class CreateProtocol {
    private int type;
    private String game_id;
    private String nick_name;
    private String ip;
    private String port;

    public CreateProtocol() {
    }
    
    public CreateProtocol(String[] args) throws IOException {
        this.type = parseType(args[0]);
        this.game_id = args[1];
        this.nick_name = args[2];
        this.ip = args[3];
        this.port = args[4];
    }

    public CreateProtocol decodeBytes(byte[] bytes) throws
            UnsupportedEncodingException {
        this.type = bytes[0] & 0xFF;
        int game_len = bytes[1] & 0xFF;
        int name_len = bytes[2] & 0xFF;
        int offset = 3 + game_len;
        this.game_id = new String(
                Arrays.copyOfRange(bytes, 3, offset));
        this.nick_name = new String(
                Arrays.copyOfRange(
                    bytes, offset, offset + name_len));
        offset = offset + name_len;
        this.ip = generateIp(Arrays.copyOfRange(bytes, offset, offset+4));
        offset = offset + 4;
        this.port = generatePort(Arrays.copyOfRange(bytes, offset, offset+2)); 
        return this;
    }

    private String generateIp(byte[] b) {
        ArrayList<String> ip = new ArrayList<String>();
        for(int i=0; i<b.length; i++) {
            ip.add(String.valueOf(b[i] & 0xFF));
        }
        return String.join(".", ip);
    }

    private String generatePort(byte[] b) {
        ArrayList<String> port = new ArrayList<String>();
        for(int i=0; i<b.length; i++) {
            port.add(String.valueOf(b[i] & 0xFF));
        }
        return String.join("", port); 
    }

    public int getType() {
        return this.type;
    }

    public String getGameId() {
        return this.game_id;
    }

    public String getNickName() {
        return this.nick_name;
    }

    public String getIp() {
        return this.ip;
    }

    public String getPort() {
        return this.port;
    }

    private int parseType(String arg) {
        if (arg.equals("create"))
            return 1;
        if (arg.equals("join"))
            return 2;
        return -1;
    }

    // covert an int to byte array, using two bytes to store the value
    private static byte[] intToByteArray(int value) {
        return new byte[] {
            (byte)value };
    }

    // covert an int to byte array, using two bytes to store the value
    private static byte[] intToByteArrays(int value) {
    		return new byte[] {
        		(byte)(value >> 8),
        		(byte)value };
    }

    private byte[] ipToBytes(String ip) {
        String[] addrs = ip.split("\\.");
        if (addrs.length != 4)
            //throw new IllegalArgumentException("ip should be format x.x.x.x");
            System.out.println("ip should be format x.x.x.x");
        byte[] bytes = new byte[4];
        for (int i = 0; i < 4; i++) {
            int a = Integer.parseInt(addrs[i]);
            if (a > 255)
                //throw new IllegalArgumentException("not a legal ip address");
                System.out.println("not a legal ip address");
            bytes[i] = (byte)a;
        }
        return bytes;
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

    public byte[] parseArgs() throws IOException {

        byte[] name_len = intToByteArray(this.game_id.length());
        byte[] game_len = intToByteArray(this.nick_name.length());

        byte[] ipToByte = ipToBytes(this.ip);
        byte[] portToByte = intToByteArrays(Integer.parseInt(this.port));
        //System.out.println(print(portToByte));

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
        outputStream.write(intToByteArray(this.type));
        outputStream.write(name_len);
        outputStream.write(game_len);
        outputStream.write(this.game_id.getBytes());
        outputStream.write(this.nick_name.getBytes());
        outputStream.write(ipToByte);
        outputStream.write(portToByte);

        return outputStream.toByteArray();
    }


    @Override
    public String toString() {
        return String.format("%s, %s, %s, %s", game_id, nick_name, ip, port);
    }
}
