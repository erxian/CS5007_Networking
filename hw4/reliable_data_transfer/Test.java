import java.util.*;


class Test {
  private boolean start = false;
	public Test() {}
  	// this is a helper function, print byte array
  	public String print(byte[] bytes) {
    	StringBuilder sb = new StringBuilder();
    	sb.append("[ ");
    	for (byte b : bytes) {
      		sb.append(String.format("0x%02X ", b));
    	}
    	sb.append("]"); return sb.toString();
  	}


    // covert an int to byte array, using two bytes to store the value
    public byte[] intToByteArrays(int value) {
    		return new byte[] {
        		(byte)(value >> 8),
        		(byte)value };
    }

    public int invertBits(int num) {
        // calculating number of
        // bits in the number
        int x = (int)(Math.log(num) /
                      Math.log(2)) + 1;
        // Inverting the
        // bits one by one
        for (int i = 0; i < x; i++) {
          num = (num ^ (1 << i));
        }
        return num;      
    }

  public int byteArrayToInt(byte[] bytes) {
      return ((bytes[0] & 0xFF) << 8) |
              ((bytes[1] & 0xFF) << 0);
  }

  public void repeat(int i) {
    if (!this.start) {
      Thread back =  new Thread(() -> {
        while(true) {
          System.out.println("thread");
        }
      });
      back.setDaemon(true);
      back.start();
      this.start = true;
    }
    System.out.println("output: " + String.valueOf(i));
  }

	public static void main(String[] args) {
    Test test = new Test();
    for (int i=0; i<10; i++) {
      test.repeat(i);
    }
    /**int x = 66782;
    if (x > 65535) {
      x++;
      //System.out.println(String.valueOf(invertBits(temp)));;
    }
    int val = invertBits(66783);
    byte[] b = intToByteArrays(val);
    byte[] a = intToByteArrays(66783);
    System.out.println("b: " + print(b));
    System.out.println("a: " + print(a));
    byte x1 = (byte)(a[0]+b[0]);
    byte x2 = (byte)(a[1]+b[1]);
    byte[] arr = new byte[]{x1, x2};
    System.out.println(print(arr));
		/**int b1 = 1;
		int b2 = 0;
		String data = "MSG:11";
		byte[] b3 = data.getBytes();
		System.out.println(print(b3));
		byte[] bytes = {(byte) 0x80, (byte) 0x00, (byte) 0x81, (byte) 0x01, (byte) 0x80, (byte) 0x01,(byte) 0x00, (byte)  0x00, (byte) 0x00, (byte) 0x74};

		int sum = b1 + b2;
		for(int b:b3){
    		sum += (b & 0xff);
		}
		System.out.println(print(intToByteArrays(sum)));*/
	}
}
