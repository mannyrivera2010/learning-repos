package com.earasoft.rdf4j.utils;

import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.rio.ntriples.NTriplesUtil;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class HUtils {
    public static final byte[] EMPTY = new byte[0];
    private static final byte[] CF_NAME = "e".getBytes(StandardCharsets.UTF_8);
    private static final String MD_ALGORITHM = "SHA1";
    static final ThreadLocal<MessageDigest> MD = new ThreadLocal<MessageDigest>(){
        @Override
        protected MessageDigest initialValue() {
            return getMessageDigest(MD_ALGORITHM);
        }
    };
    private static final Base64.Encoder ENC = Base64.getUrlEncoder().withoutPadding();

    static MessageDigest getMessageDigest(String algorithm) {
        try {
            return MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Helper method concatenating keys
     * @param prefix key prefix byte
     * @param trailingZero boolean switch adding trailing zero to the resulting key
     * @param fragments variable number of the key fragments as byte arrays
     * @return concatenated key as byte array
     */
    public static byte[] concat(byte prefix, boolean trailingZero, byte[]...fragments) {
        int i = 1;
        for (byte[] fr : fragments) {
            i += fr.length;
        }
        byte[] res = new byte[trailingZero ? i + 1 : i];
        res[0] = prefix;
        i = 1;
        for (byte[] fr : fragments) {
            System.arraycopy(fr, 0, res, i, fr.length);
            i += fr.length;
        }
        if (trailingZero) {
            res[res.length - 1] = 0;
        }
        return res;
    }



    //   / * Conversion method from Subj, Pred, Obj and optional Context into an array of HBase keys
//     * @param subj subject Resource
//     * @param pred predicate IRI
//     * @param obj object Value
//     * @param context optional static context Resource
//     * @param delete boolean switch whether to get KeyValues for deletion instead of for insertion
//     * @param timestamp long timestamp value for time-ordering purposes
//     * @return array of KeyValues
//     */
    public static byte[] toKeyValues(Statement s) {
        return toKeyValues(s.getSubject(), s.getPredicate(), s.getObject(), s.getContext());
    }

    public static byte[] toKeyValues(Resource subj, IRI pred, Value obj, Resource context) {
        byte[] sb = writeBytes(subj); // subject bytes
        byte[] pb = writeBytes(pred); // predicate bytes
        byte[] ob = writeBytes(obj); // object bytes
        byte[] cb = context == null ? new byte[0] : writeBytes(context); // context (graph) bytes

//        byte[] sKey = hashKey(sb);  //subject key
//        byte[] pKey = hashKey(pb);  //predicate key
//        byte[] oKey = hashKey(ob);  //object key

        //bytes to be used
        byte[] cq = ByteBuffer.allocate(sb.length + pb.length + ob.length + cb.length + 12)
                .putInt(sb.length).putInt(pb.length).putInt(ob.length)
                .put(sb).put(pb).put(ob).put(cb).array();

        return cq;
    }

    public static byte[] hashKey(Value v) {
        return v == null ? null : hashKey(writeBytes(v));
    }

    public static byte[] hashKey(byte[] key) {
        MessageDigest md = HUtils.MD.get();
        try {
            md.update(key);
            return md.digest();
        } finally {
            md.reset();
        }
    }

    public static byte[] writeBytes(Value v) {
        return NTriplesUtil.toNTriplesString(v).getBytes(StandardCharsets.UTF_8);
    }

    public static Value readValue(byte[] b, ValueFactory vf) {
        return NTriplesUtil.parseValue(new String(b, StandardCharsets.UTF_8), vf);
    }

    public static Resource readResource(byte[] b, ValueFactory vf) {
        return NTriplesUtil.parseResource(new String(b, StandardCharsets.UTF_8), vf);
    }

    public static IRI readIRI(byte[] b, ValueFactory vf) {
        return NTriplesUtil.parseURI(new String(b, StandardCharsets.UTF_8), vf);
    }

    //    https://github.com/Merck/Halyard/blob/master/common/src/main/java/com/msd/gin/halyard/common/HalyardTableUtils.java
    public static Statement parseStatement(byte[] b, ValueFactory vf) {
        ByteBuffer bb = ByteBuffer.wrap(b);
        byte[] sb = new byte[bb.getInt()];
        byte[] pb = new byte[bb.getInt()];
        byte[] ob = new byte[bb.getInt()];
        bb.get(sb);
        bb.get(pb);
        bb.get(ob);
        byte[] cb = new byte[bb.remaining()];
        bb.get(cb);

        Resource subj = readResource(sb, vf);
        IRI pred = readIRI(pb, vf);
        Value value = readValue(ob, vf);
        Statement stmt;

        if (cb.length == 0) {
            stmt = vf.createStatement(subj, pred, value);
        } else {
            Resource context = readResource(cb, vf);
            stmt = vf.createStatement(subj, pred, value, context);
        }
        return stmt;
    }

    public static class ByteUtils {
        private static ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);

        public static byte[] longToBytes(long x) {
            buffer.putLong(0, x);
            return buffer.array();
        }

        public static long bytesToLong(byte[] bytes) {
            buffer.rewind(); //
            buffer.put(bytes, 0, bytes.length);
            buffer.flip();//need flip
            return buffer.getLong();
        }
    }
}
