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
package org.thiesen.jfiffs.spider.business.impl;

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
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.SocketConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.thiesen.jfiffs.common.persistence.FeedDao;
import org.thiesen.jfiffs.common.persistence.FeedEntryDao;
import org.thiesen.jfiffs.common.persistence.model.FeedDbo;
import org.thiesen.jfiffs.common.persistence.model.FeedEntryDbo;
import org.thiesen.jfiffs.common.persistence.model.FeedEntryState;
import org.thiesen.jfiffs.spider.business.FeedSpiderService;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;

@Slf4j
public class FeedSpiderServiceImpl implements FeedSpiderService {

    private final FeedDao feedDao;
    private final FeedEntryDao feedEntryDao;

    private final static ForkJoinPool POOL = new ForkJoinPool(Runtime.getRuntime().availableProcessors() * 10);
    private final CloseableHttpClient httpclient;

    @Inject
    public FeedSpiderServiceImpl(FeedDao feedDao, FeedEntryDao feedEntryDao) {
        this.feedDao = feedDao;
        this.feedEntryDao = feedEntryDao;

        this.httpclient = configureHttpClient();
    }

    private static CloseableHttpClient configureHttpClient() {
        final PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        cm.setMaxTotal(200);
        cm.setDefaultMaxPerRoute(10);

        final ConnectionConfig connectionConfig = ConnectionConfig.custom()
                .build();

        final SocketConfig socketConfig = SocketConfig.custom()
                .setSoTimeout(10000)
                .build();

        cm.setDefaultConnectionConfig(connectionConfig);
        cm.setDefaultSocketConfig(socketConfig);

        return HttpClientBuilder
                .create()
                .disableCookieManagement()
                .setConnectionManager(cm)
                .build();
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


            final HttpGet httpGet = new HttpGet(feedDbo.getUrl());
            CloseableHttpResponse response = httpclient.execute(httpGet);
            try {

                SyndFeed feed = input.build(new XmlReader(response.getEntity().getContent(), true, "UTF-8"));

                feed.getEntries().parallelStream()
                        .forEach(syndEntry -> handleEntry(feedDbo.getId(), syndEntry));

                feedDao.updateLastSuccess(feedDbo.getId());

            } finally {
                response.close();
            }
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
            if (existingEntry.getState() != FeedEntryState.NEW) {
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
            final HttpGet httpGet = new HttpGet(syndEntry.getLink());
            CloseableHttpResponse response = httpclient.execute(httpGet);
           try {
               final Document document = Jsoup.parse(response.getEntity().getContent(), "UTF-8", syndEntry.getLink());
               final String linkContentHtml = document.html();
               final String linkContentText = document.text();

               feedEntryDao.updateLinkContentAndComplete(id, linkContentHtml, linkContentText);
           } finally {
               response.close();
           }

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
