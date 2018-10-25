package org.thiesen.jfiffs.business.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.common.base.Stopwatch;
import com.google.inject.Inject;
import com.rometools.rome.feed.synd.SyndContent;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.thiesen.jfiffs.business.FeedSpiderService;
import org.thiesen.jfiffs.persistence.FeedDao;
import org.thiesen.jfiffs.persistence.FeedEntryDao;
import org.thiesen.jfiffs.persistence.model.FeedDbo;
import org.thiesen.jfiffs.persistence.model.FeedEntryDbo;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
public class FeedSpiderServiceImpl implements FeedSpiderService {

    private final static Pattern WHITESPACE = Pattern.compile("\\s+");

    private final FeedDao feedDao;
    private final FeedEntryDao feedEntryDao;

    @Inject
    public FeedSpiderServiceImpl(FeedDao feedDao, FeedEntryDao feedEntryDao) {
        this.feedDao = feedDao;
        this.feedEntryDao = feedEntryDao;
    }

    @Override
    public void run() {
        final Stopwatch stopwatch = Stopwatch.createStarted();
        feedDao.listActiveFeeds()
                .parallelStream()
                .forEach(this::updateFeed);

        log.info("Feed update took: {}", stopwatch.stop());
    }

    private void updateFeed(FeedDbo feedDbo) {
        SyndFeedInput input = new SyndFeedInput();
        try {
            feedDao.updateLastAttempt(feedDbo.getId());

            SyndFeed feed = input.build(new XmlReader(URI.create(feedDbo.getUrl()).toURL()));

            feed.getEntries().parallelStream()
                    .forEach(syndEntry -> handleEntry(feedDbo.getId(), syndEntry));

            feedDao.updateLastSuccess(feedDbo.getId());

        } catch (IllegalArgumentException | FeedException e) {
            countFail(feedDbo);
        } catch (MalformedURLException e) {
            permanentlyFailed(feedDbo);
        } catch (IOException e) {
            countFail(feedDbo);
        }
    }

    private void handleEntry(UUID feedId, SyndEntry syndEntry) {
        final String uri = syndEntry.getUri();
        if (StringUtils.isBlank(uri)) {
            log.warn("Entry has no uri: {}", syndEntry);
            return;
        }

        final Optional<FeedEntryDbo> entryOptional = feedEntryDao.getEntryByUri(uri);
        if (entryOptional.isPresent()) {
            return;
        }

        final String title = StringUtils.trim(syndEntry.getTitle());
        try {
            final String titleText = Jsoup.parse(title).text();

            final String content = StreamEx.of(syndEntry.getContents())
                    .map(SyndContent::getValue)
                    .select(String.class)
                    .collect(Collectors.joining("\n"));
            final String contentHtml = Jsoup.parse(content).html();
            final String contentText = Jsoup.parse(content).text();

            final Document document = Jsoup.connect(syndEntry.getUri()).get();
            final String linkContentHtml = document.html();
            final String linkContentText = document.text();

            feedEntryDao.insert(feedId, syndEntry.getUri(), titleText, contentHtml, contentText, linkContentHtml, linkContentText);


        } catch (IOException e) {
            log.warn("Could not parse " + syndEntry.getUri(), e);
        }


    }

    private void permanentlyFailed(FeedDbo feedDbo) {
        feedDao.markAsFailed(feedDbo.getId());
    }

    private void countFail(FeedDbo feedDbo) {
        feedDao.updateFailCount(feedDbo.getId());
    }


}
