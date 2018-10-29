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
package org.thiesen.jfiffs.server.business.impl;

import com.google.inject.Inject;
import io.reactivex.Observable;
import io.reactivex.Single;
import lombok.NonNull;
import org.jzenith.rest.model.Page;
import org.thiesen.jfiffs.common.persistence.model.EntrySimilarityDbo;
import org.thiesen.jfiffs.server.business.SimilarEntryService;
import org.thiesen.jfiffs.server.business.model.SimilarEntryPair;
import org.thiesen.jfiffs.server.persistence.ReactiveEntrySimilarityDao;
import org.thiesen.jfiffs.server.persistence.ReactiveFeedEntryDao;
import org.thiesen.jfiffs.server.persistence.model.FeedEntryDbo;

import java.util.List;

public class SimilarEntryServiceImpl implements SimilarEntryService {

    private final ReactiveEntrySimilarityDao similarityDao;
    private final ReactiveFeedEntryDao reactiveFeedEntryDao;

    @Inject
    public SimilarEntryServiceImpl(ReactiveEntrySimilarityDao similarityDao, ReactiveFeedEntryDao reactiveFeedEntryDao) {
        this.similarityDao = similarityDao;
        this.reactiveFeedEntryDao = reactiveFeedEntryDao;
    }

    @Override
    public Single<Page<SimilarEntryPair>> listSimilarEntries(@NonNull final Integer offset, @NonNull final Integer limit) {
        return similarityDao.listSimilarEntries(offset, limit)
                .flatMap(this::enrich);
    }

    private Single<Page<SimilarEntryPair>> enrich(@NonNull final Page<EntrySimilarityDbo> entrySimilarityDboPage) {
        final Single<List<SimilarEntryPair>> listSingle = Observable.fromIterable(entrySimilarityDboPage.getElements())
                .flatMapSingle(this::entryPairFromDbo)
                .toList();

        return listSingle.map(entrySimilarityDboPage::withElements);
    }

    private Single<SimilarEntryPair> entryPairFromDbo(@NonNull final EntrySimilarityDbo entry) {
        final Single<Double> similaritySingle = Single.just(entry.getSimilarity());
        final Single<FeedEntryDbo> firstDboSingle = reactiveFeedEntryDao.getById(entry.getFirstId()).toSingle();
        final Single<FeedEntryDbo> secondDboSingle = reactiveFeedEntryDao.getById(entry.getSecondId()).toSingle();

        return Single.zip(similaritySingle, firstDboSingle, secondDboSingle,
                (similarity, first, second) -> new SimilarEntryPair(similarity,
                        first.getId(), first.getTitle(), first.getLink(),
                        second.getId(), second.getTitle(), second.getLink()));
    }
}
