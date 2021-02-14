package com.earasoft.rdf4j.sail.nativerockrdf.rockdb;

public class RockDbIteratorEntry {
    public final byte[] keyBytes;
    public final byte[] valueBytes;

    public RockDbIteratorEntry(byte[] keyBytes, byte[] valueBytes) {
        this.keyBytes = keyBytes;
        this.valueBytes = valueBytes;
    }
}
