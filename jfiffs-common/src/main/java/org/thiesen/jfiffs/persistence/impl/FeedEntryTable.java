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

import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Table;
import org.thiesen.jfiffs.persistence.model.FeedEntryState;

import java.time.Instant;
import java.util.UUID;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.name;
import static org.jooq.impl.DSL.table;
import static org.thiesen.jfiffs.persistence.impl.FeedTable.INSTANT;

public class FeedEntryTable {

    static final Table<Record> TABLE = table(name("feed_entry"));

    static final Field<UUID> ID_FIELD = field(name("id"), UUID.class);
    static final Field<UUID> FEED_ID_FIELD = field(name("feed_id"), UUID.class);

    static final Field<String> URL_FIELD = field(name("url"), String.class);
    static final Field<String> TITLE_FIELD = field(name("title"), String.class);
    static final Field<String> LINK_FIELD = field(name("link"), String.class);

    static final Field<String> ENTRY_CONTENT_HTML_FIELD = field(name("entry_content_html"), String.class);
    static final Field<String> ENTRY_CONTENT_TEXT_FIELD = field(name("entry_content_text"), String.class);

    static final Field<String> LINK_CONTENT_HTML_FIELD = field(name("link_content_html"), String.class);
    static final Field<String> LINK_CONTENT_TEXT_FIELD = field(name("link_content_text"), String.class);

    static final Field<FeedEntryState> STATE_FIELD = field(name("state"), FeedEntryState.class);

    static final Field<Long> FAIL_COUNT_FIELD = field(name("fail_count"), Long.class);

    static final Field<Instant> LAST_FAIL = field(name("last_fail"), INSTANT);

}
