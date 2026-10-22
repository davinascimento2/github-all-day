package com.vectorcore.storage;

import com.vectorcore.math.DistanceMetric;
import com.vectorcore.model.CollectionConfig;
import com.vectorcore.model.VectorRecord;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Collections;

/**
 * High-speed binary persistence engine for vector collections.
 */
public class SnapshotEngine {

    private static final byte[] MAGIC = new byte[]{'V', 'C', 'T', 'R'};
    private static final int VERSION = 1;

    public static void saveSnapshot(Collection collection, Path targetFile) throws IOException {
        Path parent = targetFile.getParent();
        if (parent != null) Files.createDirectories(parent);

        try (DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(Files.newOutputStream(targetFile)))) {
            dos.write(MAGIC);
            dos.writeInt(VERSION);

            CollectionConfig cfg = collection.getConfig();
            dos.writeUTF(cfg.name());
            dos.writeInt(cfg.dimension());
            dos.writeUTF(cfg.metric().name());
            dos.writeInt(cfg.hnswM());
            dos.writeInt(cfg.hnswEfConstruction());
            dos.writeInt(cfg.hnswEfSearch());

            var records = collection.getAllRecords();
            dos.writeInt(records.size());

            for (VectorRecord rec : records) {
                dos.writeUTF(rec.id());
                dos.writeUTF(rec.text() != null ? rec.text() : "");
                dos.writeLong(rec.createdAt().toEpochMilli());
                for (float f : rec.vector()) {
                    dos.writeFloat(f);
                }
            }
        }
    }

    public static Collection loadSnapshot(Path sourceFile) throws IOException {
        try (DataInputStream dis = new DataInputStream(new BufferedInputStream(Files.newInputStream(sourceFile)))) {
            byte[] magic = new byte[4];
            dis.readFully(magic);
            if (!java.util.Arrays.equals(MAGIC, magic)) {
                throw new IOException("Invalid snapshot magic header");
            }

            int version = dis.readInt();
            if (version != VERSION) {
                throw new IOException("Unsupported snapshot version: " + version);
            }

            String name = dis.readUTF();
            int dimension = dis.readInt();
            DistanceMetric metric = DistanceMetric.valueOf(dis.readUTF());
            int m = dis.readInt();
            int efConst = dis.readInt();
            int efSearch = dis.readInt();

            CollectionConfig cfg = new CollectionConfig(name, dimension, metric, m, efConst, efSearch);
            Collection collection = new Collection(cfg);

            int totalRecords = dis.readInt();
            for (int i = 0; i < totalRecords; i++) {
                String id = dis.readUTF();
                String text = dis.readUTF();
                long epochMs = dis.readLong();
                float[] vec = new float[dimension];
                for (int d = 0; d < dimension; d++) {
                    vec[d] = dis.readFloat();
                }
                collection.upsert(new VectorRecord(id, vec, text, Collections.emptyMap(), Instant.ofEpochMilli(epochMs)));
            }

            return collection;
        }
    }
}
