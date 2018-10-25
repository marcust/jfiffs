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

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import org.jooq.DSLContext;
import org.jooq.Record4;
import org.jooq.Select;
import org.thiesen.jfiffs.persistence.FeedEntryDao;
import org.thiesen.jfiffs.persistence.model.FeedEntryDbo;
import org.thiesen.jfiffs.persistence.model.FeedEntryState;

import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

public class FeedEntryDaoImpl implements FeedEntryDao {

    private final DSLContext dslContext;

    @Inject
    public FeedEntryDaoImpl(DSLContext dslContext) {
        this.dslContext = dslContext;
    }

    @Override
    public Optional<FeedEntryDbo> getEntryByUri(String uri) {
        final Select<Record4<UUID, String, FeedEntryState, Long>> select = dslContext
                .select(FeedEntryTable.ID_FIELD, FeedEntryTable.URL_FIELD, FeedEntryTable.STATE_FIELD, FeedEntryTable.FAIL_COUNT_FIELD)
                .from(FeedEntryTable.TABLE)
                .where(FeedEntryTable.URL_FIELD.eq(uri));

        select.execute();

        return select.getResult().map(this::mapRecord)
                .stream()
                .findFirst();
    }

    private FeedEntryDbo mapRecord(Record4<UUID, String, FeedEntryState, Long> record) {
        return new FeedEntryDbo(record.component1(), record.component2(), record.component3(), record.component4());
    }

    @Override
    public boolean markFailed(UUID id) {
        return dslContext.update(FeedEntryTable.TABLE)
                .set(FeedEntryTable.FAIL_COUNT_FIELD, FeedEntryTable.FAIL_COUNT_FIELD.add(1))
                .set(FeedEntryTable.LAST_FAIL, Instant.now())
                .set(FeedEntryTable.STATE_FIELD, FeedEntryState.FAILED)
                .where(FeedEntryTable.ID_FIELD.eq(id))
                .execute() == 1;
    }

    @Override
    public UUID create(UUID feedId, String uri, String titleText, String link, String contentHtml, String contentText) {
        final UUID uuid = UUID.randomUUID();
        final int execute = dslContext.insertInto(FeedEntryTable.TABLE)
                .columns(FeedEntryTable.ID_FIELD, FeedEntryTable.FEED_ID_FIELD, FeedEntryTable.URL_FIELD,
                        FeedEntryTable.TITLE_FIELD, FeedEntryTable.LINK_FIELD,
                        FeedEntryTable.ENTRY_CONTENT_HTML_FIELD, FeedEntryTable.ENTRY_CONTENT_TEXT_FIELD)
                .values(Arrays.asList(uuid, feedId, uri,
                        titleText, link,
                        contentHtml, contentText))
                .execute();

        Preconditions.checkState(execute > 0, "Should have been executed");

        return uuid;
    }

    @Override
    public boolean updateLinkContentAndComplete(UUID id, String linkContentHtml, String linkContentText) {
        return dslContext.update(FeedEntryTable.TABLE)
                .set(FeedEntryTable.LINK_CONTENT_HTML_FIELD, linkContentHtml)
                .set(FeedEntryTable.LINK_CONTENT_TEXT_FIELD, linkContentText)
                .set(FeedEntryTable.STATE_FIELD, FeedEntryState.COMPLETED)
                .where(FeedEntryTable.ID_FIELD.eq(id))
                .execute() > 0;
    }

    @Override
    public boolean incrementFailCount(UUID id) {
        return dslContext.update(FeedEntryTable.TABLE)
                .set(FeedEntryTable.FAIL_COUNT_FIELD, FeedEntryTable.FAIL_COUNT_FIELD.add(1))
                .set(FeedEntryTable.LAST_FAIL, Instant.now())
                .where(FeedEntryTable.ID_FIELD.eq(id))
                .execute() == 1;
    }
}
