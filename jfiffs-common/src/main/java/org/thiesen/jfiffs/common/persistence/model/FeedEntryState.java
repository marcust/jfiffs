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
package org.thiesen.jfiffs.common.persistence.model;

import org.jooq.EnumType;
import org.jooq.Schema;

import static org.thiesen.jfiffs.common.persistence.model.FeedState.SCHEMA;

public enum FeedEntryState implements EnumType {

    NEW, COMPLETED, FAILED, NORMALIZED, EXTRACTED;

    @Override
    public String getLiteral() {
        return this.name();
    }

    @Override
    public String getName() {
        return "feed_entry_state";
    }

    @Override
    public Schema getSchema() {
        return SCHEMA;
    }

}
