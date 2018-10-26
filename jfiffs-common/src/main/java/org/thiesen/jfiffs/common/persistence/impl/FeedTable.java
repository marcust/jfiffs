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

import lombok.experimental.UtilityClass;
import org.jooq.DataType;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Table;
import org.jooq.impl.SQLDataType;
import org.thiesen.jfiffs.common.persistence.model.FeedState;

import java.time.Instant;
import java.util.UUID;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.name;
import static org.jooq.impl.DSL.table;

@UtilityClass
public class FeedTable {

    static final DataType<Instant> INSTANT =
            SQLDataType.TIMESTAMP.asConvertedDataType(new InstantConverter());

    static final Table<Record> TABLE = table(name("feed"));

    static final Field<UUID> ID = field(name("id"), UUID.class);
    static final Field<String> TITLE = field(name("title"), String.class);
    static final Field<String> URL = field(name("url"), String.class);

    static final Field<FeedState> STATE = field(name("state"), FeedState.class);

    static final Field<Long> FAIL_COUNT = field(name("fail_count"), Long.class);

    static final Field<Instant> LAST_ATTEMPT = field(name("last_attempt"), INSTANT);
    static final Field<Instant> LAST_SUCCESS = field(name("last_success"), INSTANT);
    static final Field<Instant> LAST_FAIL = field(name("last_fail"), INSTANT);

    static final Field<Instant> CREATED = field(name("created"), INSTANT);
}
