package org.thiesen.jfiffs.persistence.impl;

import lombok.experimental.UtilityClass;
import org.jooq.DataType;
import org.jooq.Field;
import org.jooq.Nullability;
import org.jooq.Record;
import org.jooq.SQLDialect;
import org.jooq.Table;
import org.jooq.impl.DefaultBinding;
import org.jooq.impl.DefaultDataType;
import org.jooq.impl.EnumConverter;
import org.jooq.impl.SQLDataType;
import org.jooq.util.mysql.MySQLDataType;
import org.thiesen.jfiffs.persistence.model.FeedState;

import java.time.Instant;
import java.util.UUID;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.name;
import static org.jooq.impl.DSL.table;

@UtilityClass
public class FeedTable {

    private static final DataType<Instant> INSTANT =
            SQLDataType.TIMESTAMP.asConvertedDataType(new InstantConverter());
            //new DefaultDataType<>(SQLDialect.POSTGRES_10, Instant.class, "instant");

    static final Table<Record> TABLE = table(name("feed"));

    static final Field<UUID> ID_FIELD = field(name("id"), UUID.class);
    static final Field<String> TITLE_FIELD = field(name("title"), String.class);
    static final Field<String> URL_FIELD = field(name("url"), String.class);

    static final Field<FeedState> STATE_FIELD = field(name("state"), FeedState.class);

    static final Field<Long> FAIL_COUNT = field(name("fail_count"), Long.class);

    static final Field<Instant> LAST_ATTEMPT = field(name("last_attempt"), INSTANT);
    static final Field<Instant> LAST_SUCCESS = field(name("last_success"), INSTANT);
    static final Field<Instant> LAST_FAIL = field(name("last_fail"), INSTANT);


}
