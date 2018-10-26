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
package org.thiesen.jfiffs.common.persistence;

import org.thiesen.jfiffs.common.persistence.model.FeedEntryDbo;
import org.thiesen.jfiffs.common.persistence.model.NormalizedTextFeedEntryDbo;
import org.thiesen.jfiffs.common.persistence.model.TextFeedEntryDbo;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FeedEntryDao {
    Optional<FeedEntryDbo> getEntryByUri(String uri);

    boolean markFailed(UUID id);

    UUID create(UUID feedId, String uri, String titleText, String link, String contentHtml, String contentText);

    boolean updateLinkContentAndComplete(UUID id, String linkContentHtml, String linkContentText);

    boolean incrementFailCount(UUID id);

    List<TextFeedEntryDbo> listCompletedSince(Instant createdAfter);

    boolean updateNormalization(UUID id, String language, Integer wordCount, String normalizedText);

    List<NormalizedTextFeedEntryDbo> listNormalizedSince(Instant createdAfter);
}
