package org.thiesen.jfiffs.persistence;

import org.thiesen.jfiffs.persistence.model.FeedEntryDbo;

import java.util.Optional;
import java.util.UUID;

public interface FeedEntryDao {
    Optional<FeedEntryDbo> getEntryByUri(String uri);

    boolean insert(UUID feedId, String uri, String titleText, String contentHtml, String contentText, String linkContentHtml, String linkContentText);
}
