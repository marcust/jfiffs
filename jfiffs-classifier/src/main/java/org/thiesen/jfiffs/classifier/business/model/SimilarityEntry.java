package org.thiesen.jfiffs.classifier.business.model;

import lombok.Data;
import lombok.NonNull;
import org.apache.commons.lang3.tuple.Pair;
import org.thiesen.jfiffs.common.persistence.impl.EntrySimilarityDaoImpl;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Data
public class SimilarityEntry {

    @NonNull
    private final EntryWithProfile left;

    @NonNull
    private final EntryWithProfile right;

    private final double similarity;

    public List<Object> toValues() {
        final Pair<UUID, UUID> uuids = EntrySimilarityDaoImpl.sortedIds(left.getEntry().getId(), right.getEntry().getId());

        return Arrays.asList(uuids.getLeft(), uuids.getRight(), similarity);
    }

}
