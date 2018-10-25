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
package org.thiesen.jfiffs.business.impl;

import com.google.common.base.Stopwatch;
import com.google.common.base.Throwables;
import com.google.inject.Inject;
import com.rometools.rome.feed.synd.SyndContent;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.thiesen.jfiffs.business.FeedSpiderService;
import org.thiesen.jfiffs.persistence.FeedDao;
import org.thiesen.jfiffs.persistence.FeedEntryDao;
import org.thiesen.jfiffs.persistence.model.FeedDbo;
import org.thiesen.jfiffs.persistence.model.FeedEntryDbo;
import org.thiesen.jfiffs.persistence.model.FeedEntryState;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;

@Slf4j
public class FeedSpiderServiceImpl implements FeedSpiderService {

    private final FeedDao feedDao;
    private final FeedEntryDao feedEntryDao;

    private final static ForkJoinPool POOL = new ForkJoinPool(Runtime.getRuntime().availableProcessors() * 10);

    @Inject
    public FeedSpiderServiceImpl(FeedDao feedDao, FeedEntryDao feedEntryDao) {
        this.feedDao = feedDao;
        this.feedEntryDao = feedEntryDao;
    }

    @Override
    @SneakyThrows
    public void run() {
        final Stopwatch stopwatch = Stopwatch.createStarted();
        POOL.submit(() -> {
                    feedDao.listActiveFeeds()
                            .parallelStream()
                            .forEach(this::updateFeed);
                }).get();
        log.info("Feed update took: {}", stopwatch.stop());
    }

    private void updateFeed(FeedDbo feedDbo) {
        SyndFeedInput input = new SyndFeedInput();
        try {
            if (feedDbo.getFailCount() >= 3L) {
                feedDao.markAsFailed(feedDbo.getId());
                return;
            }

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
        ;
        if (StringUtils.isBlank(uri)) {
            log.warn("Entry has no uri: {}", syndEntry);
            return;
        }

        final Optional<FeedEntryDbo> entryOptional = feedEntryDao.getEntryByUri(uri);
        final UUID id;
        if (entryOptional.isPresent()) {
            final FeedEntryDbo existingEntry = entryOptional.get();
            if (existingEntry.getState() == FeedEntryState.FAILED) {
                return;
            }
            if (existingEntry.getFailCount() >= 3) {
                feedEntryDao.markFailed(existingEntry.getId());
                return;
            }

            id = existingEntry.getId();
        } else {
            final String title = StringUtils.trim(syndEntry.getTitle());
            final String titleText = Jsoup.parse(title).text();

            final String content = StreamEx.of(syndEntry.getContents())
                    .map(SyndContent::getValue)
                    .select(String.class)
                    .collect(Collectors.joining("\n"));
            final String contentHtml = Jsoup.parse(content).html();
            final String contentText = Jsoup.parse(content).text();

            id = feedEntryDao.create(feedId, uri, titleText, syndEntry.getLink(), contentHtml, contentText);
        }

        try {
            final Document document = Jsoup.connect(syndEntry.getLink()).get();
            final String linkContentHtml = document.html();
            final String linkContentText = document.text();

            feedEntryDao.updateLinkContentAndComplete(id, linkContentHtml, linkContentText);


        } catch (Exception e) {
            log.warn("Could not parse {}: {} (caused by {})", syndEntry.getLink(), e.getMessage(), Throwables.getRootCause(e).getMessage());
            feedEntryDao.incrementFailCount(id);
        }
    }

    private void permanentlyFailed(FeedDbo feedDbo) {
        feedDao.markAsFailed(feedDbo.getId());
    }

    private void countFail(FeedDbo feedDbo) {
        feedDao.incrementFailCount(feedDbo.getId());
    }


}
