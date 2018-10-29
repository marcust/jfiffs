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
package org.thiesen.jfiffs.common.persistence.tables;

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

    public static final Table<Record> TABLE = table(name("entry_similarity"));

    public static final Field<UUID> FIRST_ID = field(name("first_id"), UUID.class);
    public static final Field<UUID> SECOND_ID = field(name("second_id"), UUID.class);

    public static final Field<Double> SIMILARITY = field(name("similarity"), Double.class);

}
