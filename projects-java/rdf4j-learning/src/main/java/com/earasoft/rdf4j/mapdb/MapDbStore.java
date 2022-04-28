package com.earasoft.rdf4j.mapdb;

import org.jetbrains.annotations.NotNull;
import org.mapdb.BTreeMap;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;

public class MapDbStore {
    final DB db;
    final BTreeMap<Long, byte[]> map;
    final BTreeMap<byte[], byte[]> spo_map;

    public MapDbStore(String mapDbFileName) {
        this.db = DBMaker.fileDB(mapDbFileName)
                .fileMmapEnable()
                .make();
        this.map = getLongBTreeMap(db);
        this.spo_map = getSpocMap(db);
    }

    @NotNull
    private static BTreeMap<byte[], byte[]> getSpocMap(DB db) {
        return db
                .treeMap("spoc_map", Serializer.BYTE_ARRAY, Serializer.BYTE_ARRAY)
                .createOrOpen();
    }

    @NotNull
    private static BTreeMap<Long, byte[]> getLongBTreeMap(DB db) {
        return db
                .treeMap("map", Serializer.LONG, (Serializer.BYTE_ARRAY))
                .valuesOutsideNodesEnable()
                .createOrOpen();
    }
}
