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
package org.thiesen.jfiffs.opml;

import be.ceau.opml.OpmlParser;
import be.ceau.opml.entity.Opml;
import be.ceau.opml.entity.Outline;
import com.google.inject.Inject;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.thiesen.jfiffs.model.Feed;
import org.thiesen.jfiffs.persistence.FeedDao;
import org.thiesen.jfiffs.persistence.model.FeedDbo;

import java.util.Optional;
import java.util.stream.Stream;

public class OpmlImporter {

    private final FeedDao dao;

    @Inject
    public OpmlImporter(FeedDao dao) {
        this.dao = dao;
    }

    @SneakyThrows
    public void importOpml() {
        final Opml opml = new OpmlParser().parse(this.getClass().getResourceAsStream("/feeds.opml"));

        opml.getBody().getOutlines().stream()
                .flatMap(this::importOutline)
                .forEach(this::syncWithDatabase);
    }

    private void syncWithDatabase(Feed feed) {
        final Optional<FeedDbo> existingEntry = dao.getFeedByUrl(feed.getXmlUrl());

        if (existingEntry.isEmpty()) {
            dao.createEntry(feed.getTitle(), feed.getXmlUrl());
        }
    }

    private Stream<Feed> importOutline(Outline outline) {
        if (!outline.getSubElements().isEmpty()) {
            return outline.getSubElements().stream().flatMap(this::importOutline);
        }

        final String title = outline.getAttribute("title");
        final String xmlUrl = outline.getAttribute("xmlUrl");

        if (!StringUtils.isBlank(xmlUrl)) {
            return Stream.of(Feed.builder().xmlUrl(xmlUrl).title(title).build());
        }

        return Stream.of();

    }
}
