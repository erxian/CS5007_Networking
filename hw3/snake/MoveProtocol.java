package snake.app;

import java.util.*;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import static snake.app.Config.CHANGE_DIR;

public class MoveProtocol {
    private int type;
    private String game_id;
    private String nick_name;
    private String dir;

    public MoveProtocol() {
    }

    public MoveProtocol(String game_id, String nick_name) throws IOException {
        this.type = CHANGE_DIR;
        this.game_id = game_id;
        this.nick_name = nick_name;
        this.dir = null;
    }


    public void setDir(Direction direction) {
        switch(direction) {
            case UP:
                this.dir = "U";
                break;
            case RIGHT:
                this.dir = "R";
                break;
            case DOWN:
                this.dir = "D";
                break;
            case LEFT:
                this.dir = "L";
                break;
        }
    }

    public MoveProtocol decodeBytes(byte[] bytes) throws
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
        this.dir = new String(
                Arrays.copyOfRange(bytes, offset, offset + 1));
        return this;
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

    public String getDir() {
        return this.dir;
    }

    public void setDir(String dir) {

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

    public byte[] parseArgs() throws IOException {

        byte[] name_len = intToByteArray(this.game_id.length());
        byte[] game_len = intToByteArray(this.nick_name.length());

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
        outputStream.write(intToByteArray(this.type));
        outputStream.write(name_len);
        outputStream.write(game_len);
        outputStream.write(this.game_id.getBytes());
        outputStream.write(this.nick_name.getBytes());
        outputStream.write(this.dir.getBytes());
        return outputStream.toByteArray();
    }

    @Override
    public String toString() {
        return String.format("%s, %s, %s", game_id, nick_name, dir);
    }
}
