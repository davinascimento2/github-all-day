package com.vectorcore.storage;

import com.vectorcore.model.CollectionConfig;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe multi-tenant manager for collections.
 */
public class CollectionManager {

    private final Map<String, Collection> collections = new ConcurrentHashMap<>();

    public Collection createCollection(CollectionConfig config) {
        if (collections.containsKey(config.name())) {
            throw new IllegalStateException("Collection already exists: " + config.name());
        }
        Collection coll = new Collection(config);
        collections.put(config.name(), coll);
        return coll;
    }

    public Optional<Collection> getCollection(String name) {
        return Optional.ofNullable(collections.get(name));
    }

    public Collection getOrCreate(CollectionConfig config) {
        return collections.computeIfAbsent(config.name(), k -> new Collection(config));
    }

    public boolean dropCollection(String name) {
        Collection coll = collections.remove(name);
        if (coll != null) {
            coll.clear();
            return true;
        }
        return false;
    }

    public Set<String> listCollections() {
        return Collections.unmodifiableSet(collections.keySet());
    }

    public int count() {
        return collections.size();
    }
}
