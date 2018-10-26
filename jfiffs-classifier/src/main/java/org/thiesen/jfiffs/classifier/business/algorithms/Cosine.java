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
package org.thiesen.jfiffs.classifier.business.algorithms;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;

import java.util.Map;
import java.util.regex.Pattern;

public class Cosine {

    private static final Pattern SPACE_REG = Pattern.compile("\\s+");

    private final int k;

    public Cosine(final int k) {
        this.k = k;
    }

    private static double norm(final Map<String, Integer> profile) {
        double agg = 0;

        for (Map.Entry<String, Integer> entry : profile.entrySet()) {
            agg += 1.0 * entry.getValue() * entry.getValue();
        }

        return Math.sqrt(agg);
    }

    private static double dotProduct(
            final Object2IntMap<String> profile1,
            final Object2IntMap<String> profile2) {

        // Loop over the smallest map
        Object2IntMap<String> small_profile = profile2;
        Object2IntMap<String> large_profile = profile1;
        if (profile1.size() < profile2.size()) {
            small_profile = profile1;
            large_profile = profile2;
        }

        double agg = 0;
        final Object2IntMap.FastEntrySet<String> entries = (Object2IntMap.FastEntrySet<String>) small_profile.object2IntEntrySet();
        final ObjectIterator<Object2IntMap.Entry<String>> entryObjectIterator = entries.fastIterator();
        while (entryObjectIterator.hasNext()) {
            final Object2IntMap.Entry<String> entry = entryObjectIterator.next();
            int i = large_profile.getInt(entry.getKey());
            if (i == large_profile.defaultReturnValue()) {
                continue;
            }
            agg += 1.0 * entry.getIntValue() * i;
        }

        return agg;
    }

    public final double similarity(
            final Profile profile1,
            final Profile profile2) {

        return dotProduct(profile1.getProfile(), profile2.getProfile())
                / (profile1.getNorm() * profile2.getNorm());
    }

    public final Profile getProfile(final String string, final int wordCount) {
        final Object2IntMap<String> shingles = new Object2IntOpenHashMap<>(wordCount);

        String string_no_space = SPACE_REG.matcher(string).replaceAll(" ");
        for (int i = 0; i < (string_no_space.length() - k + 1); i++) {
            String shingle = string_no_space.substring(i, i + k);
            ((Object2IntOpenHashMap<String>) shingles).addTo(shingle, 1);
        }

        return new Profile(shingles, norm(shingles));
    }

}

