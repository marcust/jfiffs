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
package org.thiesen.jfiffs.normalizer.business.impl;

import com.github.difflib.text.DiffRow;
import com.github.difflib.text.DiffRowGenerator;
import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.inject.Inject;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;
import org.apache.commons.lang3.tuple.Pair;
import org.thiesen.jfiffs.common.persistence.FeedEntryDao;
import org.thiesen.jfiffs.common.persistence.model.NormalizedTextFeedEntryDbo;
import org.thiesen.jfiffs.normalizer.business.ContentExtractionService;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class ContentExtractionServiceImpl implements ContentExtractionService {

    private static final Splitter WHITESPACE_SPLITTER = Splitter.on(CharMatcher.whitespace()).trimResults().omitEmptyStrings();

    private final FeedEntryDao dao;

    @Inject
    public ContentExtractionServiceImpl(FeedEntryDao dao) {
        this.dao = dao;
    }

    @Override
    @SneakyThrows
    public void run() {
        final Stopwatch stopwatch = Stopwatch.createStarted();

        final Multimap<UUID, NormalizedTextFeedEntryDbo> byFeedId =
                Multimaps.index(
                this.dao.listNormalizedAndExtractedSince(Instant.now().minus(Duration.ofDays(7))),
                NormalizedTextFeedEntryDbo::getFeedId);

        for (final Map.Entry<UUID, Collection<NormalizedTextFeedEntryDbo>> entry : byFeedId.asMap().entrySet()) {
            final Collection<NormalizedTextFeedEntryDbo> entries = entry.getValue();
            if (entries.size() < 2) {
                continue;
            }

            final NormalizedTextFeedEntryDbo first = Iterables.get(entries, 0);
            final NormalizedTextFeedEntryDbo second = Iterables.get(entries, 1);

            final List<String> text1 = WHITESPACE_SPLITTER.splitToList(first.getNormalizedText());
            final List<String> text2 = WHITESPACE_SPLITTER.splitToList(second.getNormalizedText());

            final DiffRowGenerator generator = DiffRowGenerator.create()
                    .inlineDiffByWord(true)
                    .build();

            final List<DiffRow> diffRows = generator.generateDiffRows(text1, text2);

            final Set<String> equalWords = StreamEx.of(diffRows)
                    .pairMap(Pair::of)
                    .pairMap(Pair::of)
                    .filter(this::onlyEquals)
                    .flatMap(pairs -> Stream.of(pairs.getLeft(), pairs.getRight()))
                    .flatMap(pair -> Stream.of(pair.getLeft(), pair.getRight()))
                    .map(DiffRow::getNewLine)
                    .collect(Collectors.toSet());

            entry.getValue()
                    .stream()
                    .forEach(normalizedTextFeedEntryDbo -> removeEqualWords(equalWords, normalizedTextFeedEntryDbo));

        }

        log.info("Extraction took: {}", stopwatch.stop());
    }

    private boolean onlyEquals(Pair<Pair<DiffRow, DiffRow>, Pair<DiffRow, DiffRow>> pairPairPair) {
        final Pair<DiffRow, DiffRow> left = pairPairPair.getLeft();
        final Pair<DiffRow, DiffRow> right = pairPairPair.getRight();

        return left.getLeft().getTag() == DiffRow.Tag.EQUAL &&
                left.getRight().getTag() == DiffRow.Tag.EQUAL &&
                right.getLeft().getTag() == DiffRow.Tag.EQUAL &&
                right.getRight().getTag() == DiffRow.Tag.EQUAL;
    }

    private void removeEqualWords(Set<String> filterWords, NormalizedTextFeedEntryDbo normalizedTextFeedEntryDbo) {
        final List<String> words = WHITESPACE_SPLITTER.splitToList(normalizedTextFeedEntryDbo.getNormalizedText());

        final String filteredText = words.stream()
                .filter(word -> !filterWords.contains(word))
                .collect(Collectors.joining(" "));

        dao.updateExtraction(normalizedTextFeedEntryDbo.getId(), filteredText);
    }


}
