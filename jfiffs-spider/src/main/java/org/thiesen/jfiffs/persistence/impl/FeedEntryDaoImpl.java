package org.thiesen.jfiffs.persistence.impl;

import com.google.inject.Inject;
import org.jooq.DSLContext;
import org.jooq.Record2;
import org.jooq.Select;
import org.thiesen.jfiffs.persistence.FeedEntryDao;
import org.thiesen.jfiffs.persistence.model.FeedEntryDbo;

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
        final Select<Record2<UUID, String>> select = dslContext
                .select(FeedEntryTable.ID_FIELD, FeedEntryTable.URL_FIELD)
                .from(FeedEntryTable.TABLE)
                .where(FeedEntryTable.URL_FIELD.eq(uri));

        select.execute();

        return select.getResult().map(this::mapRecord)
                .stream()
                .findFirst();
    }

    private FeedEntryDbo mapRecord(Record2<UUID, String> record) {
        return new FeedEntryDbo(record.component1(), record.component2());
    }

    @Override
    public boolean insert(UUID feedId, String uri, String titleText, String contentHtml, String contentText, String linkContentHtml, String linkContentText) {
        return dslContext.insertInto(FeedEntryTable.TABLE)
                .columns(FeedEntryTable.ID_FIELD, FeedEntryTable.FEED_ID_FIELD, FeedEntryTable.URL_FIELD, FeedEntryTable.TITLE_FIELD,
                        FeedEntryTable.ENTRY_CONTENT_HTML_FIELD, FeedEntryTable.ENTRY_CONTENT_TEXT_FIELD,
                        FeedEntryTable.LINK_CONTENT_HTML_FIELD, FeedEntryTable.LINK_CONTENT_TEXT_FIELD)
                .values(Arrays.asList(UUID.randomUUID(), feedId, uri, titleText,
                        contentHtml, contentText,
                        linkContentHtml, linkContentText))
                .execute() > 0;
    }
}
