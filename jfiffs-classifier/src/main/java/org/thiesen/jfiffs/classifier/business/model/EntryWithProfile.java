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
package org.thiesen.jfiffs.classifier.business.model;

import lombok.Data;
import lombok.NonNull;
import org.thiesen.jfiffs.classifier.business.algorithms.Profile;
import org.thiesen.jfiffs.common.persistence.model.ExtractedTextFeedEntryDbo;

@Data
public class EntryWithProfile {

    @NonNull
    private final ExtractedTextFeedEntryDbo entry;

    @NonNull
    private final Profile profile;

    public boolean hasProfileOfUsefulSize() {
        return profile.getProfile().size() > 50;
    }

}
