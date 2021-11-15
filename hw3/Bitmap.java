package snake.app;

import java.util.*;
import java.math.BigInteger;
import java.lang.Short;
import java.nio.ByteBuffer;

import static snake.app.Config.BOARD_SIZE;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.io.ByteArrayOutputStream;
import java.util.concurrent.ThreadLocalRandom;
import java.io.IOException;

class Bitmap {
	private LinkedList<Position> position;
	private ByteArrayOutputStream outputStream;

	Bitmap(LinkedList<Position> position){
		this.position = position;
		this.outputStream = new ByteArrayOutputStream();
	}

	// this is a helper function, print byte array
  public String print(byte[] bytes) {
    StringBuilder sb = new StringBuilder();
    sb.append("[ ");
    for (byte b : bytes) {
        sb.append(String.format("0x%02X ", b));
    }
    sb.append("]"); return sb.toString();
  }


  private StringBuilder convertToBitString(BitSet bitset) {
  	StringBuilder str = new StringBuilder();
  	for (int k=0; k<BOARD_SIZE; k++) {
  		if (bitset.get(k)) {
  			str.append("1");
  		} else {
  			str.append("0");
  		}
  	}
  	return str;
  }

  private byte[] bitStringToBytes(StringBuilder bitString) {
  	int offset = 0;
  	int bitTobyte = 8;
  	byte[] converted = new byte[4];
  	while(offset < BOARD_SIZE) {
  		short a = Short.parseShort(bitString.substring(offset, offset+bitTobyte), 2);
  		  ByteBuffer bytes = ByteBuffer.allocate(2).putShort(a);
  		  byte[] bval = bytes.array();
  		converted[offset/bitTobyte] = bval[1];
  		offset += bitTobyte;
  	}
  	return converted;
  }

	public byte[] generateBitmaps() throws IOException {
		for (int i=0; i<BOARD_SIZE; i++) {
			BitSet bits = new BitSet(BOARD_SIZE);			
			for (int j=0; j<BOARD_SIZE; j++) {
				int row = i;
				int col = j;
				Stream<Position> matchingObjects = this.position.stream().
    				filter(p -> p.getRow() == row);

    			matchingObjects.filter(p -> p.getCol() == col).
    				forEach(p -> bits.set(col));
    		}
    		StringBuilder bitString = convertToBitString(bits);
    		byte[] bitmap = bitStringToBytes(bitString);
    		outputStream.write(bitmap);
		}
		return outputStream.toByteArray();
	}
}
