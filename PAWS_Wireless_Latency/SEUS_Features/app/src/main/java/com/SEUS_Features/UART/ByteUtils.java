package com.SEUS_Features.UART;

import java.nio.ByteBuffer;

/**
 * Created by stephen on 9/28/2016.
 */
public class ByteUtils {
    private static ByteBuffer buffer = ByteBuffer.allocate(8);

    public static byte[] longToBytes(long x) {
        buffer.putLong(0, x);
        return buffer.array();
    }

    public static long bytesToLong(byte[] bytes, int offset, int length) {

        for(int i = 0; i < length; i++) {
            buffer.put(i, bytes[offset + i]);
        }
        //buffer.flip();//Big Endian default
        return buffer.getLong();
    }
}
