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

import com.google.common.base.Stopwatch;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.inject.Inject;
import com.optimaize.langdetect.DetectedLanguage;
import com.optimaize.langdetect.LanguageDetector;
import com.optimaize.langdetect.LanguageDetectorBuilder;
import com.optimaize.langdetect.ngram.NgramExtractors;
import com.optimaize.langdetect.profiles.LanguageProfile;
import com.optimaize.langdetect.profiles.LanguageProfileReader;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.thiesen.jfiffs.normalizer.business.model.FeedEntryMeta;
import org.thiesen.jfiffs.common.persistence.FeedEntryDao;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
public class NormalizationServiceImpl implements org.thiesen.jfiffs.normalizer.business.NormalizationService {

    private final static Pattern NON_WORD_CHARACTERS = Pattern.compile("\\W+");

    private final static LoadingCache<String, Set<String>> STOP_WORDS_BY_LANGUAGE = CacheBuilder.newBuilder()
            .build(new CacheLoader<String, Set<String>>() {
                @Override
                public Set<String> load(String key) throws Exception {
                    final InputStream resourceAsStream = this.getClass().getResourceAsStream("/stopwords-" + key + ".txt");
                    if (resourceAsStream == null) {
                        return Collections.emptySet();
                    }

                    return IOUtils.readLines(resourceAsStream, StandardCharsets.UTF_8)
                            .stream()
                            .map(line -> NON_WORD_CHARACTERS.matcher(line).replaceAll(""))
                            .map(StringUtils::lowerCase)
                            .collect(Collectors.toSet());

                }
            });


    private final FeedEntryDao feedEntryDao;
    private final LanguageDetector languageDetector;

    @Inject
    @SneakyThrows
    public NormalizationServiceImpl(FeedEntryDao feedEntryDao) {
        this.feedEntryDao = feedEntryDao;

        //load all languages:
        List<LanguageProfile> languageProfiles = new LanguageProfileReader().readAllBuiltIn();

        //build language detector:
        languageDetector = LanguageDetectorBuilder.create(NgramExtractors.standard())
                .withProfiles(languageProfiles)
                .build();
    }

    @Override
    public void run() {
        final Stopwatch stopwatch = Stopwatch.createStarted();

        feedEntryDao.listCompletedSince(Instant.now().minus(Duration.ofDays(7)))
                .stream()
                .map(FeedEntryMeta::new)
                .map(this::countWords)
                .map(this::detectLanguage)
                .map(this::normalizeText)
                .forEach(this::updateEntry);

        log.info("Normalization took: {}", stopwatch.stop());
    }

    private boolean updateEntry(FeedEntryMeta feedEntryMeta) {
        return feedEntryDao.updateNormalization(feedEntryMeta.getFeedEntryDbo().getId(),
                feedEntryMeta.getLanguage(), feedEntryMeta.getWordCount(), feedEntryMeta.getNormalizedText());
    }

    private FeedEntryMeta countWords(FeedEntryMeta feedEntryMeta) {
        feedEntryMeta.setWordCount(feedEntryMeta.getWordList().size());

        return feedEntryMeta;
    }

    private FeedEntryMeta normalizeText(FeedEntryMeta feedEntryMeta) {
        final String normalized = feedEntryMeta.getWordList()
                .stream()
                .map(StringUtils::lowerCase)
                .map(word -> NON_WORD_CHARACTERS.matcher(word).replaceAll(""))
                .filter(word -> word.length() > 3) // remove small words
                .filter(word -> notStopWord(feedEntryMeta.getLanguage(), word))
                .map(word -> StringUtils.substring(word, 0, -3)) // poor mans stemming
                .filter(word -> word.length() > 3)
                .collect(Collectors.joining(" "));

        feedEntryMeta.setNormalizedText(normalized);

        return feedEntryMeta;
    }

    private boolean notStopWord(String language, String word) {
        return !STOP_WORDS_BY_LANGUAGE.getUnchecked(language).contains(word);
    }


    private FeedEntryMeta detectLanguage(FeedEntryMeta feedEntryMeta) {
        final List<DetectedLanguage> probabilities = languageDetector.getProbabilities(feedEntryMeta.getFullText());

        final Optional<DetectedLanguage> localeOptional = probabilities
                .stream()
                .sorted(Comparator.comparing(DetectedLanguage::getProbability).reversed())
                .findFirst();

        localeOptional.ifPresentOrElse(
                detectedLanguage -> feedEntryMeta.setLanguage(detectedLanguage.getLocale().getLanguage().toLowerCase()),
                () -> feedEntryMeta.setLanguage("NA")
        );

        return feedEntryMeta;
    }

}
