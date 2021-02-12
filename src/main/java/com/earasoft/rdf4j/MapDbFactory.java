package com.earasoft.rdf4j;

import org.jetbrains.annotations.NotNull;
import org.mapdb.BTreeMap;
import org.mapdb.DB;
import org.mapdb.Serializer;

public class MapDbFactory {

    @NotNull
    static BTreeMap<byte[], byte[]> getSpocMap(DB db) {
        return db
                .treeMap("spoc_map", Serializer.BYTE_ARRAY, Serializer.BYTE_ARRAY)
                .createOrOpen();
    }

    @NotNull
    static BTreeMap<Long, byte[]> getLongBTreeMap(DB db) {
        return db
                .treeMap("map", Serializer.LONG, (Serializer.BYTE_ARRAY))
                .valuesOutsideNodesEnable()
                .createOrOpen();
    }
}
