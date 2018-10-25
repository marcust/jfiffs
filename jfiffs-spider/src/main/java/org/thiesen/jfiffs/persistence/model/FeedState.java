package org.thiesen.jfiffs.persistence.model;

import org.jooq.EnumType;
import org.jooq.Schema;
import org.jooq.impl.DSL;

public enum FeedState implements EnumType {

    ACTIVE, FAILED;

    @Override
    public String getLiteral() {
        return this.name();
    }

    @Override
    public String getName() {
        return "feed_state";
    }

    @Override
    public Schema getSchema() {
        return DSL.schema(DSL.name("public"));
    }
}
