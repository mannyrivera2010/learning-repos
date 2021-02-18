package com.earasoft.rdf4j.sail.nativeStore;

import com.earasoft.rdf4j.sail.nativeStore.btree.RecordComparator;
import org.eclipse.rdf4j.common.io.ByteArrayUtil;

/**
 * A RecordComparator that can be used to create indexes with a configurable order of the subject, predicate, object
 * and context fields.
 */
class TripleComparator implements RecordComparator {

    private final char[] fieldSeq;

    public TripleComparator(String fieldSeq) {
        this.fieldSeq = fieldSeq.toCharArray();
    }

    public char[] getFieldSeq() {
        return fieldSeq;
    }

    @Override
    public final int compareBTreeValues(byte[] key, byte[] data, int offset, int length) {
        for (char field : fieldSeq) {
            int fieldIdx = 0;

            switch (field) {
                case 's':
                    fieldIdx = TripleStore.SUBJ_IDX;
                    break;
                case 'p':
                    fieldIdx = TripleStore.PRED_IDX;
                    break;
                case 'o':
                    fieldIdx = TripleStore.OBJ_IDX;
                    break;
                case 'c':
                    fieldIdx = TripleStore.CONTEXT_IDX;
                    break;
                default:
                    throw new IllegalArgumentException(
                            "invalid character '" + field + "' in field sequence: " + new String(fieldSeq));
            }

            int diff = ByteArrayUtil.compareRegion(key, fieldIdx, data, offset + fieldIdx, 4);

            if (diff != 0) {
                return diff;
            }
        }

        return 0;
    }
}
