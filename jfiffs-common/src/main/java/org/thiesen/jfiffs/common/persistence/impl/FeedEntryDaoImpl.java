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

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import lombok.NonNull;
import org.jooq.DSLContext;
import org.jooq.Record3;
import org.jooq.Record4;
import org.jooq.Select;
import org.thiesen.jfiffs.common.persistence.model.FeedEntryState;
import org.thiesen.jfiffs.common.persistence.FeedEntryDao;
import org.thiesen.jfiffs.common.persistence.model.FeedEntryDbo;
import org.thiesen.jfiffs.common.persistence.model.NormalizedTextFeedEntryDbo;
import org.thiesen.jfiffs.common.persistence.model.TextFeedEntryDbo;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
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
                .select(FeedEntryTable.ID, FeedEntryTable.URL, FeedEntryTable.STATE, FeedEntryTable.FAIL_COUNT)
                .from(FeedEntryTable.TABLE)
                .where(FeedEntryTable.URL.eq(uri));

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
                .set(FeedEntryTable.FAIL_COUNT, FeedEntryTable.FAIL_COUNT.add(1))
                .set(FeedEntryTable.LAST_FAIL, Instant.now())
                .set(FeedEntryTable.STATE, FeedEntryState.FAILED)
                .where(FeedEntryTable.ID.eq(id))
                .execute() == 1;
    }

    @Override
    public UUID create(UUID feedId, String uri, String titleText, String link, String contentHtml, String contentText) {
        final UUID uuid = UUID.randomUUID();
        final int execute = dslContext.insertInto(FeedEntryTable.TABLE)
                .columns(FeedEntryTable.ID, FeedEntryTable.FEED_ID, FeedEntryTable.URL,
                        FeedEntryTable.TITLE, FeedEntryTable.LINK,
                        FeedEntryTable.ENTRY_CONTENT_HTML, FeedEntryTable.ENTRY_CONTENT_TEXT)
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
                .set(FeedEntryTable.LINK_CONTENT_HTML, linkContentHtml)
                .set(FeedEntryTable.LINK_CONTENT_TEXT, linkContentText)
                .set(FeedEntryTable.STATE, FeedEntryState.COMPLETED)
                .where(FeedEntryTable.ID.eq(id))
                .execute() > 0;
    }

    @Override
    public boolean incrementFailCount(UUID id) {
        return dslContext.update(FeedEntryTable.TABLE)
                .set(FeedEntryTable.FAIL_COUNT, FeedEntryTable.FAIL_COUNT.add(1))
                .set(FeedEntryTable.LAST_FAIL, Instant.now())
                .where(FeedEntryTable.ID.eq(id))
                .execute() == 1;
    }

    @Override
    public List<TextFeedEntryDbo> listCompletedSince(@NonNull Instant createdAfter) {
        final Select<Record4<UUID, String, String, String>> select = dslContext
                .select(FeedEntryTable.ID, FeedEntryTable.TITLE,
                        FeedEntryTable.ENTRY_CONTENT_TEXT, FeedEntryTable.LINK_CONTENT_TEXT)
                .from(FeedEntryTable.TABLE)
                .where(FeedEntryTable.STATE.eq(FeedEntryState.COMPLETED))
                .and(FeedEntryTable.CREATED.gt(createdAfter));

        select.execute();

        return select.getResult().map(record -> new TextFeedEntryDbo(record.component1(), record.component2(), record.component3(), record.component4()));
    }

    @Override
    public boolean updateNormalization(UUID id, String language, Integer wordCount, String normalizedText) {
        return dslContext.update(FeedEntryTable.TABLE)
                .set(FeedEntryTable.LANGUAGE, language)
                .set(FeedEntryTable.WORD_COUNT, wordCount)
                .set(FeedEntryTable.NORMALIZED_TEXT, normalizedText)
                .set(FeedEntryTable.STATE, FeedEntryState.NORMALIZED)
                .where(FeedEntryTable.ID.eq(id))
                .execute() > 0;
    }

    @Override
    public List<NormalizedTextFeedEntryDbo> listNormalizedSince(Instant createdAfter) {
        final Select<Record3<UUID, String, String>> select = dslContext
                .select(FeedEntryTable.ID, FeedEntryTable.TITLE,
                        FeedEntryTable.NORMALIZED_TEXT)
                .from(FeedEntryTable.TABLE)
                .where(FeedEntryTable.STATE.eq(FeedEntryState.NORMALIZED))
                .and(FeedEntryTable.CREATED.gt(createdAfter));

        select.execute();

        return select.getResult().map(record -> new NormalizedTextFeedEntryDbo(record.component1(), record.component2(), record.component3()));

    }
}
