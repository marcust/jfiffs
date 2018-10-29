package org.thiesen.jfiffs.common.persistence.model;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import java.util.UUID;

@Builder
@Data
public class EntrySimilarityDbo {

    @NonNull
    private final UUID firstId;

    @NonNull
    private final UUID secondId;

    private final double similarity;

}
