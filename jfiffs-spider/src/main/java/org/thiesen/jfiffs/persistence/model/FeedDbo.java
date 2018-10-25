package org.thiesen.jfiffs.persistence.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;

import java.util.UUID;

@Data
@AllArgsConstructor
public class FeedDbo {

    @NonNull
    private final UUID id;

    @NonNull
    private final String url;

}
