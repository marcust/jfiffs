package org.thiesen.jfiffs.common.persistence.impl;

import lombok.experimental.UtilityClass;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Table;

import java.util.UUID;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.name;
import static org.jooq.impl.DSL.table;

@UtilityClass
public class EntrySimilarityTable {

    static final Table<Record> TABLE = table(name("entry_similarity"));

    static final Field<UUID> FIRST_ID = field(name("first_id"), UUID.class);
    static final Field<UUID> SECOND_ID = field(name("second_id"), UUID.class);

    static final Field<Double> SIMILARITY = field(name("similarity"), Double.class);

}
