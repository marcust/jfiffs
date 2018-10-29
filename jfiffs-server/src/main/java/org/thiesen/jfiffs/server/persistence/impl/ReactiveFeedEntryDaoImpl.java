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

import io.reactiverse.reactivex.pgclient.Row;
import io.reactivex.Maybe;
import org.jooq.DSLContext;
import org.jooq.Select;
import org.jzenith.postgresql.PostgresqlClient;
import org.thiesen.jfiffs.common.persistence.tables.FeedEntryTable;
import org.thiesen.jfiffs.server.persistence.ReactiveFeedEntryDao;
import org.thiesen.jfiffs.server.persistence.model.FeedEntryDbo;

import javax.inject.Inject;
import java.util.UUID;

public class ReactiveFeedEntryDaoImpl implements ReactiveFeedEntryDao {

    private final PostgresqlClient client;
    private final DSLContext dslContext;

    @Inject
    public ReactiveFeedEntryDaoImpl(PostgresqlClient client, DSLContext dslContext) {
        this.client = client;
        this.dslContext = dslContext;
    }

    @Override
    public Maybe<FeedEntryDbo> getById(UUID id) {
        final Select<?> select = dslContext.select(FeedEntryTable.ID, FeedEntryTable.TITLE, FeedEntryTable.LINK)
                .from(FeedEntryTable.TABLE)
                .where(FeedEntryTable.ID.eq(id));

        return client.executeForSingleRow(select)
                .map(this::toDbo);
    }

    private FeedEntryDbo toDbo(Row row) {
        return new FeedEntryDbo(
                (UUID)row.getValue(FeedEntryTable.ID.getName()),
                row.getString(FeedEntryTable.TITLE.getName()),
                row.getString(FeedEntryTable.LINK.getName())
        );
    }
}
