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

import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.thiesen.jfiffs.common.persistence.model.FeedEntryState;

import java.time.Instant;
import java.util.UUID;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.name;
import static org.jooq.impl.DSL.table;
import static org.thiesen.jfiffs.common.persistence.impl.FeedTable.INSTANT;

public class FeedEntryTable {

    static final Table<Record> TABLE = table(name("feed_entry"));

    static final Field<UUID> ID = field(name("id"), UUID.class);
    static final Field<UUID> FEED_ID = field(name("feed_id"), UUID.class);

    static final Field<String> URL = field(name("url"), String.class);
    static final Field<String> TITLE = field(name("title"), String.class);
    static final Field<String> LINK = field(name("link"), String.class);

    static final Field<String> ENTRY_CONTENT_HTML = field(name("entry_content_html"), String.class);
    static final Field<String> ENTRY_CONTENT_TEXT = field(name("entry_content_text"), String.class);

    static final Field<String> LINK_CONTENT_HTML = field(name("link_content_html"), String.class);
    static final Field<String> LINK_CONTENT_TEXT = field(name("link_content_text"), String.class);

    static final Field<FeedEntryState> STATE = field(name("state"), FeedEntryState.class);

    static final Field<Long> FAIL_COUNT = field(name("fail_count"), Long.class);

    static final Field<Instant> LAST_FAIL = DSL.field(name("last_fail"), INSTANT);

    static final Field<Instant> CREATED = field(name("created"), INSTANT);

    static final Field<String> LANGUAGE = field(name("language"), String.class);
    static final Field<String> NORMALIZED_TEXT = field(name("normalized_text"), String.class);
    static final Field<Integer> WORD_COUNT = field(name("word_count"), Integer.class);



}
