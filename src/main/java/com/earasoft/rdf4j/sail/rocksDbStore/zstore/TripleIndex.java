package com.earasoft.rdf4j.sail.rocksDbStore.zstore;

import com.earasoft.rdf4j.sail.nativeStore.btree.BTree;

import java.io.IOException;

class TripleIndex {

    private final TripleStore tripleStore;
    private final TripleComparator tripleComparator;

    public final BTree btree;

    public TripleIndex(TripleStore tripleStore, String fieldSeq) throws IOException {
        this.tripleStore = tripleStore;
        tripleComparator = new TripleComparator(fieldSeq);
        btree = new BTree(tripleStore.dir, getFilenamePrefix(fieldSeq), 2048,
                TripleStore.RECORD_LENGTH, tripleComparator,
                tripleStore.forceSync);
    }

    private String getFilenamePrefix(String fieldSeq) {
        return "triples-" + fieldSeq;
    }

    public char[] getFieldSeq() {
        return tripleComparator.getFieldSeq();
    }

    public BTree getBTree() {
        return btree;
    }

    /**
     * Determines the 'score' of this index on the supplied pattern of subject, predicate, object and context IDs.
     * The higher the score, the better the index is suited for matching the pattern. Lowest score is 0, which means
     * that the index will perform a sequential scan.
     */
    public int getPatternScore(int subj, int pred, int obj, int context) {
        int score = 0;

        for (char field : tripleComparator.getFieldSeq()) {
            switch (field) {
                case 's':
                    if (subj >= 0) {
                        score++;
                    } else {
                        return score;
                    }
                    break;
                case 'p':
                    if (pred >= 0) {
                        score++;
                    } else {
                        return score;
                    }
                    break;
                case 'o':
                    if (obj >= 0) {
                        score++;
                    } else {
                        return score;
                    }
                    break;
                case 'c':
                    if (context >= 0) {
                        score++;
                    } else {
                        return score;
                    }
                    break;
                default:
                    throw new RuntimeException("invalid character '" + field + "' in field sequence: "
                            + new String(tripleComparator.getFieldSeq()));
            }
        }

        return score;
    }

    @Override
    public String toString() {
        return new String(getFieldSeq());
    }
}
