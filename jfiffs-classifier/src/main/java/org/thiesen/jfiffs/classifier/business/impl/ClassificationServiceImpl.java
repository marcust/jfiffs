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
import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;
import org.HdrHistogram.ConcurrentDoubleHistogram;
import org.HdrHistogram.DoubleHistogram;
import org.jooq.tools.StringUtils;
import org.thiesen.jfiffs.classifier.business.ClassificationService;
import org.thiesen.jfiffs.classifier.business.algorithms.Cosine;
import org.thiesen.jfiffs.classifier.business.model.EntryWithProfile;
import org.thiesen.jfiffs.common.persistence.FeedEntryDao;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class ClassificationServiceImpl implements ClassificationService {

    private final FeedEntryDao dao;

    @Inject
    public ClassificationServiceImpl(FeedEntryDao dao) {
        this.dao = dao;
    }

    @Override
    public void run() {
        final Stopwatch stopwatch = Stopwatch.createStarted();
        final Cosine cosine = new Cosine(3);

        log.info("Precomputing profiles...");
        final List<EntryWithProfile> entryWithProfiles = StreamEx.of(dao.listNormalizedSince(Instant.now().minus(Duration.ofDays(7)))
                .stream())
                .map(entry -> new EntryWithProfile(entry, cosine.getProfile(entry.getNormalizedText())))
                .collect(Collectors.toCollection(LinkedList::new));

        log.info("Precomputation took {} for {} values", stopwatch.toString(), entryWithProfiles.size());
        stopwatch.reset().start();

        final DoubleHistogram doubleHistogram = new ConcurrentDoubleHistogram(5);

        log.info("Computing distances...");
        StreamEx.cartesianPower(2, entryWithProfiles)
                .parallel()
                .filter(tuple -> tuple.get(0) != tuple.get(1))
                .map(tuple -> cosine.similarity(tuple.get(0).getProfile(), tuple.get(1).getProfile()))
                .forEach(doubleHistogram::recordValue);
        log.info("Distance computation took {}", stopwatch.toString());


        StreamEx.of(doubleHistogram.linearBucketValues(0.1).iterator())
                .forEach(value -> log.info("{}-{}: {} {}%",
                        roundDown(value.getValueIteratedFrom()), 
                        roundUp(value.getValueIteratedTo()),
                        StringUtils.leftPad(value.getCountAddedInThisIterationStep() + "", 10),
                        roundUp((value.getCountAddedInThisIterationStep() / (double)doubleHistogram.getTotalCount()) * 100)));


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
}
