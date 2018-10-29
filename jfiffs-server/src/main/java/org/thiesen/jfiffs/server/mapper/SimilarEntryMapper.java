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
package org.thiesen.jfiffs.server.mapper;

import org.jzenith.rest.model.Page;
import org.thiesen.jfiffs.server.business.model.SimilarEntryPair;
import org.thiesen.jfiffs.server.resource.response.SimilarEntryResponse;

public class SimilarEntryMapper {

    public Page<SimilarEntryResponse> mapToPageSimilarEntryResponse(Page<SimilarEntryPair> similarEntryPairPage) {
        return similarEntryPairPage.map(this::mapToSimilarEntryResponse);
    }

    private SimilarEntryResponse mapToSimilarEntryResponse(SimilarEntryPair similarEntryPair) {
        return new SimilarEntryResponse(similarEntryPair.getSimilarity(),
                similarEntryPair.getFirstTitle(), similarEntryPair.getFirstLink(),
                similarEntryPair.getSecondTitle(), similarEntryPair.getSecondLink());
    }
}
