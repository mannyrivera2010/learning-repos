package com.earasoft.rdf4j.sail.nativerockrdf.rockdb;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class RockByteUtils {

    private static byte[] longToByteA(long value) {
        return ByteBuffer.allocate(Long.BYTES)
                .order(ByteOrder.nativeOrder())
                .putLong(value).array();
    }

    private static long byteAToLong(byte[] bytes) {
        assert bytes.length == Long.BYTES;
        return ByteBuffer.wrap(bytes)
                .order(ByteOrder.nativeOrder())
                .getLong();
    }

    private static byte[] intToByteA(int value) {
        return ByteBuffer.allocate(Integer.BYTES)
                .order(ByteOrder.nativeOrder())
                .putLong(value).array();
    }

    private static int byteAToInt(byte[] bytes) {
        assert bytes.length == Integer.BYTES;
        return ByteBuffer.wrap(bytes)
                .order(ByteOrder.nativeOrder())
                .getInt();
    }

}
