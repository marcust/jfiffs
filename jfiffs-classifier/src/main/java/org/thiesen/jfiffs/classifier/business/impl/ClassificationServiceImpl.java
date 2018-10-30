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
package org.thiesen.jfiffs.classifier.business.impl;

import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import cyclops.data.Vector;
import cyclops.futurestream.LazyReact;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashBigSet;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;
import org.HdrHistogram.ConcurrentDoubleHistogram;
import org.HdrHistogram.DoubleHistogram;
import org.apache.commons.lang3.tuple.Pair;
import org.jooq.tools.StringUtils;
import org.thiesen.jfiffs.classifier.business.ClassificationService;
import org.thiesen.jfiffs.classifier.business.algorithms.Cosine;
import org.thiesen.jfiffs.classifier.business.model.EntryWithProfile;
import org.thiesen.jfiffs.classifier.business.model.SimilarityEntry;
import org.thiesen.jfiffs.common.persistence.EntrySimilarityDao;
import org.thiesen.jfiffs.common.persistence.FeedEntryDao;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;

@Slf4j
public class ClassificationServiceImpl implements ClassificationService {

    private final static ForkJoinPool POOL = new ForkJoinPool(Runtime.getRuntime().availableProcessors() * 2);

    private final FeedEntryDao dao;

    private final EntrySimilarityDao similarityDao;

    private final Cosine cosine;

    @Inject
    public ClassificationServiceImpl(FeedEntryDao dao, EntrySimilarityDao similarityDao) {
        this.dao = dao;
        this.similarityDao = similarityDao;

        cosine = new Cosine(3);

    }

    @Override
    public void run() {

        final List<EntryWithProfile> entryWithProfiles = precomputeProfiles(cosine);

        final DoubleHistogram doubleHistogram = new ConcurrentDoubleHistogram(5);

        final Stopwatch stopwatch = Stopwatch.createStarted();
        log.info("Computing distances...");
        StreamEx.cartesianPower(2, entryWithProfiles)
                .parallel()
                .filter(tuple -> tuple.get(0) != tuple.get(1))
                .mapToDouble(tuple -> cosine.similarity(tuple.get(0).getProfile(), tuple.get(1).getProfile()))
                .forEach(doubleHistogram::recordValue);
        log.info("Distance computation took {}", stopwatch.toString());


        StreamEx.of(doubleHistogram.linearBucketValues(0.1).iterator())
                .forEach(value -> log.info("{}: {} {}%",
                        roundDown(value.getValueIteratedFrom()),
                        StringUtils.leftPad(value.getCountAddedInThisIterationStep() + "", 10),
                        StringUtils.leftPad(roundUp((value.getCountAddedInThisIterationStep() / (double) doubleHistogram.getTotalCount()) * 100) + "", 6)));
    }

    private List<EntryWithProfile> precomputeProfiles(Cosine cosine) {
        log.info("Precomputing profiles...");
        final Stopwatch stopwatch = Stopwatch.createStarted();

        final LinkedList<EntryWithProfile> entryWithProfiles = StreamEx.of(dao.listExtractedSince(Instant.now().minus(Duration.ofDays(7)))
                .stream())
                .map(entry -> new EntryWithProfile(entry, cosine.getProfile(entry.getExtractedText(), entry.getWordCount())))
                .filter(EntryWithProfile::hasProfileOfUsefulSize)
                .collect(Collectors.toCollection(LinkedList::new));

        log.info("Precomputation took {} for {} values", stopwatch.toString(), entryWithProfiles.size());

        return entryWithProfiles;
    }

    private String roundUp(double value) {
        BigDecimal bigDecimal = new BigDecimal(value);
        BigDecimal roundedWithScale = bigDecimal.setScale(2, RoundingMode.HALF_UP);
        return roundedWithScale.toString();
    }

    private String roundDown(double value) {
        BigDecimal bigDecimal = new BigDecimal(value);
        BigDecimal roundedWithScale = bigDecimal.setScale(2, RoundingMode.HALF_DOWN);
        return roundedWithScale.toString();
    }

    @Override
    public void writeClassifications() {
        final List<EntryWithProfile> entryWithProfiles = precomputeProfiles(cosine);

        final Stopwatch stopwatch = Stopwatch.createStarted();
        log.info("Loading existing distances...");
        final int batch_size = 1000;
        final Integer count = similarityDao.count();
        final Set<Pair<UUID,UUID>> existingDistances = similarityDao.load()
                .collect(Collectors.toCollection(ObjectOpenHashBigSet::new));
        log.info("Distance loading took {}", stopwatch.toString());

        stopwatch.reset().start();
        log.info("Computing distances...");
        final StreamEx<SimilarityEntry> stream = StreamEx.cartesianPower(2, entryWithProfiles)
                .parallel(POOL)
                .filter(tuple -> tuple.get(0) != tuple.get(1))
                .map(tuple -> Pair.of(tuple.get(0), tuple.get(1)))
                .filter(tuple -> !existingDistances.contains(tuple))
                .map(this::computeSimilarty);

        new LazyReact(POOL)
                .fromStream(stream)
                .grouped(100)
                .map(this::writeEntriesBatch)
                .forEach(stage -> stage.thenAccept(written -> {
                    if (written > 0) {
                        log.info("Wrote {}", written);
                    }
                }));

        log.info("Distance computation took {}", stopwatch.toString());
    }

    private SimilarityEntry computeSimilarty(Pair<EntryWithProfile, EntryWithProfile> pair) {
        final EntryWithProfile first = pair.getLeft();
        final EntryWithProfile second = pair.getRight();

        double similarity = cosine.similarity(first.getProfile(), second.getProfile());
        // Nan similarity to zero
        if (Double.isNaN(similarity)) {
            similarity = 0.0D;
        }

        // High similarity from same host is same entry
        if (similarity > 0.99D) {
            final URI firstEntryUri = URI.create(first.getEntry().getLink());
            final URI secondEntryUri = URI.create(second.getEntry().getLink());

            if (firstEntryUri.getHost().equals(secondEntryUri.getHost())) {
                similarity = 0;
            }
        }

        return new SimilarityEntry(first, second, similarity);
    }

    private boolean notEntryExists(@NonNull final Pair<EntryWithProfile, EntryWithProfile> entryWithProfiles) {
        final EntryWithProfile first = entryWithProfiles.getLeft();
        final EntryWithProfile second = entryWithProfiles.getRight();

        return !similarityDao.exists(first.getEntry().getId(), second.getEntry().getId());
    }

    private CompletionStage<Integer> writeEntriesBatch(@NonNull final Vector<SimilarityEntry> similarityEntries) {
        if (similarityEntries.isEmpty()) {
            return CompletableFuture.completedFuture(0);
        }
        return similarityDao.insert(similarityEntries.stream().map(SimilarityEntry::toValues).collect(ImmutableList.toImmutableList()));
    }

}
