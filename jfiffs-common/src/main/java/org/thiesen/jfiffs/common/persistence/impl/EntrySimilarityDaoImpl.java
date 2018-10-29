package org.thiesen.jfiffs.common.persistence.impl;

import com.google.inject.Inject;
import org.apache.commons.lang3.tuple.Pair;
import org.jooq.DSLContext;
import org.thiesen.jfiffs.common.persistence.EntrySimilarityDao;

import java.util.Arrays;
import java.util.UUID;

public class EntrySimilarityDaoImpl implements EntrySimilarityDao {

    private final DSLContext dslContext;

    @Inject
    public EntrySimilarityDaoImpl(DSLContext dslContext) {
        this.dslContext = dslContext;
    }


    @Override
    public boolean exists(UUID id, UUID id1) {
        final Pair<UUID, UUID> ids = sortedIds(id, id1);
        return dslContext.fetchExists(dslContext.selectOne().from(EntrySimilarityTable.TABLE)
                .where(EntrySimilarityTable.FIRST_ID.eq(ids.getLeft())).and(EntrySimilarityTable.SECOND_ID.eq(ids.getRight())));
    }

    private Pair<UUID, UUID> sortedIds(UUID id, UUID id1) {
        if (id.compareTo(id1) > 0) {
            return Pair.of(id, id1);
        } else {
            return Pair.of(id1, id);
        }
    }

    @Override
    public void insert(UUID id, UUID id1, double similarity) {
        final Pair<UUID, UUID> ids = sortedIds(id, id1);

        dslContext.insertInto(EntrySimilarityTable.TABLE)
                .columns(EntrySimilarityTable.FIRST_ID, EntrySimilarityTable.SECOND_ID, EntrySimilarityTable.SIMILARITY)
                .values(Arrays.asList(ids.getLeft(), ids.getRight(), similarity))
                .onDuplicateKeyIgnore()
                .execute();

    }
}
