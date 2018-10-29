package org.thiesen.jfiffs.common.persistence.model;

import lombok.Data;
import lombok.NonNull;

import java.util.UUID;

@Data
public class ExtractedTextFeedEntryDbo {

    @NonNull
    private final UUID id;

    @NonNull
    private final String extractedText;

    @NonNull
    private final Integer wordCount;

}
