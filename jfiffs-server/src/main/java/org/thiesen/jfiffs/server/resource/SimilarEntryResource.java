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
package org.thiesen.jfiffs.server.resource;

import com.google.inject.Inject;
import io.reactivex.Single;
import org.jzenith.rest.model.Page;
import org.thiesen.jfiffs.server.business.SimilarEntryService;
import org.thiesen.jfiffs.server.mapper.SimilarEntryMapper;
import org.thiesen.jfiffs.server.resource.response.SimilarEntryResponse;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

@Path("/similar")
public class SimilarEntryResource {

    private final SimilarEntryService similarEntryService;

    private final SimilarEntryMapper mapper;

    @Inject
    public SimilarEntryResource(SimilarEntryService similarEntryService, SimilarEntryMapper mapper) {
        this.similarEntryService = similarEntryService;
        this.mapper = mapper;
    }

    @Produces(MediaType.APPLICATION_JSON)
    @GET
    public Single<Page<SimilarEntryResponse>> listUsers(@QueryParam("offset") @DefaultValue("0") Integer offset,
                                                        @QueryParam("limit") @DefaultValue("20") @Min(1) @Max(100) Integer limit) {
        return similarEntryService
                .listSimilarEntries(offset, limit)
                .map(mapper::mapToPageSimilarEntryResponse);
    }



}
