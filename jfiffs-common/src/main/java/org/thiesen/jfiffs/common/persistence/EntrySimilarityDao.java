package org.thiesen.jfiffs.common.persistence;

import java.util.UUID;

public interface EntrySimilarityDao {

    boolean exists(UUID id, UUID id1);

    void insert(UUID id, UUID id1, double similarity);
}
