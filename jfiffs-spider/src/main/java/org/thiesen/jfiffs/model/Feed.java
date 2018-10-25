package org.thiesen.jfiffs.model;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

@Builder
@Data
public class Feed {

    @NonNull
    private final String title;

    @NonNull
    private final String xmlUrl;

}
