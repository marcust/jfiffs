/**
 * Copyright © 2018 Marcus Thiesen (marcus@thiesen.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.thiesen.jfiffs.common.persistence.impl;

import com.google.inject.Inject;
import org.apache.commons.lang3.tuple.Pair;
import org.jooq.DSLContext;
import org.thiesen.jfiffs.common.persistence.EntrySimilarityDao;
import org.thiesen.jfiffs.common.persistence.tables.EntrySimilarityTable;

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
