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
package org.thiesen.jfiffs.normalizer.business.model;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import lombok.Data;
import lombok.NonNull;
import org.thiesen.jfiffs.common.persistence.model.TextFeedEntryDbo;

import java.util.List;

@Data
public class FeedEntryMeta {

    private final Splitter WORD_SPLITTER = Splitter.on(CharMatcher.whitespace()).trimResults().omitEmptyStrings();

    private final Supplier<String> fullTextSupplier;
    private final Supplier<List<String>> wordListSupplier;

    public FeedEntryMeta(@NonNull TextFeedEntryDbo dbo) {
        this.feedEntryDbo = dbo;

        this.fullTextSupplier = Suppliers.memoize(() -> {
            final StringBuilder builder = new StringBuilder();
            builder.append(dbo.getTitle()).append('\n');
            builder.append(dbo.getContentText()).append('\n');
            builder.append(dbo.getLinkText()).append('\n');

            return builder.toString();
        });

        this.wordListSupplier = Suppliers.memoize(() -> WORD_SPLITTER.splitToList(getFullText()));
    }

    @NonNull
    private TextFeedEntryDbo feedEntryDbo;

    private Integer wordCount;

    private String language;

    private String normalizedText;

    public String getFullText() {
        return fullTextSupplier.get();
    }

    public List<String> getWordList() {
        return wordListSupplier.get();
    }

}
