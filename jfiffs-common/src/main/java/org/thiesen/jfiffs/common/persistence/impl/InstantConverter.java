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