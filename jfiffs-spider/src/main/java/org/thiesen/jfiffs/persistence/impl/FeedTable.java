package org.thiesen.jfiffs.persistence.impl;

import lombok.experimental.UtilityClass;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Table;

import java.util.UUID;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.name;
import static org.jooq.impl.DSL.table;

@UtilityClass
public class FeedTable {

    static final Table<Record> TABLE = table(name("feed"));

    static final Field<UUID> ID_FIELD = field(name("id"), UUID.class);
    static final Field<String> TITLE_FIELD = field(name("title"), String.class);
    static final Field<String> URL_FIELD = field(name("url"), String.class);
}
