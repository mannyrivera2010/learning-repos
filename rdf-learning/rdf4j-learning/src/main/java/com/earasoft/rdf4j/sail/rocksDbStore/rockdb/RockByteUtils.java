package com.earasoft.rdf4j.sail.rocksDbStore.rockdb;

import com.google.common.base.Preconditions;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.UUID;

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

    //

    /**
     * RocksDB "uint64add" merge uses little-endian 64-bit counters
     * @param value
     * @return
     */
    public static long decodeCounter(byte[] value) {
        Preconditions.checkArgument(value.length == 8, "invalid encoded counter value length != 8");
        return ((long)(value[7] & 0xff) << 56)
                | ((long)(value[6] & 0xff) << 48)
                | ((long)(value[5] & 0xff) << 40)
                | ((long)(value[4] & 0xff) << 32)
                | ((long)(value[3] & 0xff) << 24)
                | ((long)(value[2] & 0xff) << 16)
                | ((long)(value[1] & 0xff) <<  8)
                | ((long)(value[0] & 0xff) <<  0);
    }

    /**
     * RocksDB "uint64add" merge uses little-endian 64-bit counters
     * https://www.codota.com/web/assistant/code/rs/5c66e45f1095a50001e79dd6#L187
     *
     * @param value
     * @return
     */
    public static byte[] encodeCounter(long value) {
        final byte[] bytes = new byte[8];
        bytes[0] = (byte)(value >>  0);
        bytes[1] = (byte)(value >>  8);
        bytes[2] = (byte)(value >> 16);
        bytes[3] = (byte)(value >> 24);
        bytes[4] = (byte)(value >> 32);
        bytes[5] = (byte)(value >> 40);
        bytes[6] = (byte)(value >> 48);
        bytes[7] = (byte)(value >> 56);
        return bytes;
    }

    public static byte[] getIdAsByteUUID(byte[] value)
    {
        UUID uuid = UUID.nameUUIDFromBytes(value);
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        return bb.array();
    }

    public UUID frombyteUUID(byte[] b)
    {
        return UUID.nameUUIDFromBytes(b);
    }

}
