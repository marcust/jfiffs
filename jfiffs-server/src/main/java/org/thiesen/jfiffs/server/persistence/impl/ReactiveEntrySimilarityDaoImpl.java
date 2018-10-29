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
package org.thiesen.jfiffs.server.persistence.impl;

import com.google.common.collect.ImmutableList;
import io.reactiverse.reactivex.pgclient.PgIterator;
import io.reactiverse.reactivex.pgclient.PgPool;
import io.reactiverse.reactivex.pgclient.PgRowSet;
import io.reactiverse.reactivex.pgclient.Row;
import io.reactivex.Single;
import one.util.streamex.StreamEx;
import org.jooq.DSLContext;
import org.jooq.Select;
import org.jzenith.postgresql.PostgresqlClient;
import org.jzenith.rest.model.Page;
import org.thiesen.jfiffs.common.persistence.model.EntrySimilarityDbo;
import org.thiesen.jfiffs.common.persistence.tables.EntrySimilarityTable;
import org.thiesen.jfiffs.server.persistence.ReactiveEntrySimilarityDao;

import javax.inject.Inject;

import java.util.List;
import java.util.UUID;

import static org.jooq.impl.DSL.count;
import static org.thiesen.jfiffs.common.persistence.tables.EntrySimilarityTable.FIRST_ID;
import static org.thiesen.jfiffs.common.persistence.tables.EntrySimilarityTable.SECOND_ID;
import static org.thiesen.jfiffs.common.persistence.tables.EntrySimilarityTable.SIMILARITY;

public class ReactiveEntrySimilarityDaoImpl implements ReactiveEntrySimilarityDao {

    private final PgPool pgPool;
    private final PostgresqlClient client;
    private final DSLContext dslContext;

    @Inject
    public ReactiveEntrySimilarityDaoImpl(PgPool pgPool, PostgresqlClient client, DSLContext dslContext) {
        this.pgPool = pgPool;
        this.client = client;
        this.dslContext = dslContext;
    }

    @Override
    public Single<Page<EntrySimilarityDbo>> listSimilarEntries(Integer offset, Integer limit) {
        final Select<?> select = dslContext.select(FIRST_ID, SECOND_ID, SIMILARITY)
                .from(EntrySimilarityTable.TABLE)
                .orderBy(SIMILARITY.desc())
                .offset(offset)
                .limit(limit);

        final Select<?> count = dslContext.select(count())
                .from(EntrySimilarityTable.TABLE);

        client.execute(select);

        return Single.zip(
                client.executeForSingleRow(count).toSingle(),
                client.execute(select),
                (countRow, valueRows) -> new Page<>(offset, limit, countRow.getLong(0), mapToEntries(valueRows)));

    }

    private List<EntrySimilarityDbo> mapToEntries(PgRowSet valueRows) {
        return StreamEx.of(valueRows.iterator().getDelegate())
                .map((io.reactiverse.pgclient.Row row) -> mapToEntry(Row.newInstance(row)))
                .collect(ImmutableList.toImmutableList());
    }

    private List<EntrySimilarityDbo> mapToEntries(List<Row> valueRows) {
        return valueRows.stream().map(this::mapToEntry).collect(ImmutableList.toImmutableList());
    }

    private EntrySimilarityDbo mapToEntry(Row row) {
        return new EntrySimilarityDbo(
                (UUID)row.getValue(FIRST_ID.getName()),
                (UUID)row.getValue(SECOND_ID.getName()),
                row.getDouble(SIMILARITY.getName())
        );
    }
}
