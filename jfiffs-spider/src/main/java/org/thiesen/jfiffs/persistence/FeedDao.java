package org.thiesen.jfiffs.persistence;

import org.thiesen.jfiffs.persistence.model.FeedDbo;

import java.util.Optional;

public interface FeedDao {
    Optional<FeedDbo> getFeedByUrl(String xmlUrl);

    boolean createEntry(String title, String xmlUrl);
}
