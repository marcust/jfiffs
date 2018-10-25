package org.thiesen.jfiffs.persistence.impl;

import org.jooq.Converter;

import java.sql.Timestamp;
import java.time.Instant;

public class InstantConverter implements Converter<Timestamp, Instant> {
    @Override
    public Instant from(Timestamp databaseObject) {
        if (databaseObject == null) {
            return null;
        }
        return Instant.ofEpochMilli(databaseObject.getTime());
    }

    @Override
    public Timestamp to(Instant userObject) {
        if (userObject == null) {
            return null;
        }
        return new Timestamp(userObject.toEpochMilli());
    }

    @Override
    public Class<Timestamp> fromType() {
        return Timestamp.class;
    }

    @Override
    public Class<Instant> toType() {
        return Instant.class;
    }
}
