package org.thiesen.jfiffs.persistence.impl;

import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Table;

import java.util.UUID;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.name;
import static org.jooq.impl.DSL.table;

/**
 *
 CREATE TABLE FEED_ENTRY (
 ID UUID,
 FEED_ID REFERENCES FEED (ID) ON DELETE CASCADE,
 URL TEXT CONSTRAINT unique_entry_url UNIQUE,

 ENTRY_CONTENT_HTML TEXT,
 ENTRY_CONTENT_TEXT TEXT,

 LINK_CONTENT_HTML TEXT,
 LINK_CONTENT_TEXT TEXT

 );
 */
public class FeedEntryTable {

    static final Table<Record> TABLE = table(name("feed_entry"));

    static final Field<UUID> ID_FIELD = field(name("id"), UUID.class);
    static final Field<UUID> FEED_ID_FIELD = field(name("feed_id"), UUID.class);

    static final Field<String> URL_FIELD = field(name("url"), String.class);
    static final Field<String> TITLE_FIELD = field(name("title"), String.class);

    static final Field<String> ENTRY_CONTENT_HTML_FIELD = field(name("entry_content_html"), String.class);
    static final Field<String> ENTRY_CONTENT_TEXT_FIELD = field(name("entry_content_text"), String.class);

    static final Field<String> LINK_CONTENT_HTML_FIELD = field(name("link_content_html"), String.class);
    static final Field<String> LINK_CONTENT_TEXT_FIELD = field(name("link_content_text"), String.class);



}
