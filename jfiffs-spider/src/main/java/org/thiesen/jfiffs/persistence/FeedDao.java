package org.thiesen.jfiffs.persistence;

import org.thiesen.jfiffs.persistence.model.FeedDbo;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FeedDao {
    Optional<FeedDbo> getFeedByUrl(String xmlUrl);

    boolean createEntry(String title, String xmlUrl);

    List<FeedDbo> listActiveFeeds();

    boolean updateFailCount(UUID id);

    boolean markAsFailed(UUID id);

    boolean updateLastAttempt(UUID id);

    boolean updateLastSuccess(UUID id);
}
