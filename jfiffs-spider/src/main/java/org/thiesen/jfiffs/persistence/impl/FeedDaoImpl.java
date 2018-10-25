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
package org.thiesen.jfiffs.persistence.impl;

import com.google.inject.Inject;
import lombok.NonNull;
import org.jooq.DSLContext;
import org.jooq.Record2;
import org.jooq.Record3;
import org.jooq.Select;
import org.jooq.SelectConditionStep;
import org.thiesen.jfiffs.persistence.FeedDao;
import org.thiesen.jfiffs.persistence.model.FeedDbo;
import org.thiesen.jfiffs.persistence.model.FeedState;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class FeedDaoImpl implements FeedDao {

    private final DSLContext dslContext;

    @Inject
    public FeedDaoImpl(DSLContext dslContext) {
        this.dslContext = dslContext;
    }

    @Override
    public Optional<FeedDbo> getFeedByUrl(@NonNull String xmlUrl) {
        final Select<Record3<UUID, String, Long>> select = dslContext
                .select(FeedTable.ID_FIELD, FeedTable.URL_FIELD, FeedTable.FAIL_COUNT_FIELD)
                .from(FeedTable.TABLE)
                .where(FeedTable.URL_FIELD.eq(xmlUrl));

        select.execute();

        return select.getResult().map(this::mapRecord)
                .stream()
                .findFirst();

    }

    private FeedDbo mapRecord(Record3<UUID, String, Long> record) {
        return new FeedDbo(record.component1(), record.component2(), record.component3());
    }

    @Override
    public boolean createEntry(@NonNull String title, @NonNull String xmlUrl) {
        return dslContext.insertInto(FeedTable.TABLE)
                .columns(FeedTable.ID_FIELD, FeedTable.TITLE_FIELD, FeedTable.URL_FIELD, FeedTable.STATE_FIELD)
                .values(Arrays.asList(UUID.randomUUID(), title, xmlUrl, FeedState.ACTIVE))
                .execute() == 1;
    }

    @Override
    public List<FeedDbo> listActiveFeeds() {
        final Select<Record3<UUID, String, Long>> select = dslContext
                .select(FeedTable.ID_FIELD, FeedTable.URL_FIELD, FeedTable.FAIL_COUNT_FIELD)
                .from(FeedTable.TABLE)
                .where(FeedTable.STATE_FIELD.eq(FeedState.ACTIVE))
                .orderBy(FeedTable.ID_FIELD);

        select.execute();

        return select.getResult().map(this::mapRecord);
    }

    @Override
    public boolean incrementFailCount(UUID id) {
        return dslContext.update(FeedTable.TABLE)
                .set(FeedTable.FAIL_COUNT_FIELD, FeedTable.FAIL_COUNT_FIELD.add(1))
                .set(FeedTable.LAST_FAIL, Instant.now())
                .where(FeedTable.ID_FIELD.eq(id))
                .execute() == 1;
    }

    @Override
    public boolean markAsFailed(UUID id) {
        return dslContext.update(FeedTable.TABLE)
                .set(FeedTable.FAIL_COUNT_FIELD, FeedTable.FAIL_COUNT_FIELD.add(1))
                .set(FeedTable.LAST_FAIL, Instant.now())
                .set(FeedTable.STATE_FIELD, FeedState.FAILED)
                .where(FeedTable.ID_FIELD.eq(id))
                .execute() == 1;
    }

    @Override
    public boolean updateLastAttempt(UUID id) {
        return dslContext.update(FeedTable.TABLE)
                .set(FeedTable.LAST_ATTEMPT, Instant.now())
                .where(FeedTable.ID_FIELD.eq(id))
                .execute() == 1;
    }

    @Override
    public boolean updateLastSuccess(UUID id) {
        return dslContext.update(FeedTable.TABLE)
                .set(FeedTable.LAST_SUCCESS, Instant.now())
                .set(FeedTable.FAIL_COUNT_FIELD, 0L)
                .set(FeedTable.LAST_FAIL, (Instant)null)
                .where(FeedTable.ID_FIELD.eq(id))
                .execute() == 1;

    }
}
