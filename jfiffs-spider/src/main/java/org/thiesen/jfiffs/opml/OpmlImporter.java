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

        System.out.println(outline.getAttributes());

        final String title = outline.getAttribute("title");
        final String xmlUrl = outline.getAttribute("xmlUrl");

        if (!StringUtils.isBlank(xmlUrl)) {
            return Stream.of(Feed.builder().xmlUrl(xmlUrl).title(title).build());
        }

        return Stream.of();

    }
}
