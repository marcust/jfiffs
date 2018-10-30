package org.thiesen.jfiffs.storage.business;

import org.apache.commons.lang3.tuple.Pair;

import java.util.UUID;

public interface StorageService {
    Result store(Pair<UUID, UUID> uuids, double similarity);

    Result delete(Pair<UUID, UUID> uuids);

    Double get(Pair<UUID, UUID> uuids);
}
