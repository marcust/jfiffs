package org.thiesen.jfiffs.persistence.impl;

import com.google.inject.Inject;
import lombok.NonNull;
import org.jooq.DSLContext;
import org.jooq.Record2;
import org.jooq.Result;
import org.jooq.Select;
import org.jooq.SelectConditionStep;
import org.thiesen.jfiffs.persistence.FeedDao;
import org.thiesen.jfiffs.persistence.model.FeedDbo;

import java.util.Arrays;
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
        final Select<Record2<UUID, String>> select = dslContext
                .select(FeedTable.ID_FIELD, FeedTable.URL_FIELD)
                .from(FeedTable.TABLE)
                .where(FeedTable.URL_FIELD.eq(xmlUrl));

        select.execute();

        return select.getResult().map(this::mapRecord)
                .stream()
                .findFirst();

    }

    private FeedDbo mapRecord(Record2<UUID, String> record) {
        return new FeedDbo(record.component1(), record.component2());
    }

    @Override
    public boolean createEntry(@NonNull String title, @NonNull String xmlUrl) {
        return dslContext.insertInto(FeedTable.TABLE)
                .columns(FeedTable.ID_FIELD, FeedTable.TITLE_FIELD, FeedTable.URL_FIELD)
                .values(Arrays.asList(UUID.randomUUID(), title, xmlUrl))
                .execute() == 1;
    }
}
