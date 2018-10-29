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
package org.thiesen.jfiffs.normalizer;

import com.google.inject.Inject;
import com.google.inject.Injector;
import org.jzenith.core.JZenith;
import org.thiesen.jfiffs.normalizer.business.BusinessModule;
import org.thiesen.jfiffs.normalizer.business.ContentExtractionService;
import org.thiesen.jfiffs.normalizer.business.impl.NormalizationServiceImpl;
import org.thiesen.jfiffs.common.persistence.PersistenceModule;
import org.thiesen.jfiffs.common.persistence.migration.FlywayMigration;

public class NormalizationApp {

    @Inject
    private FlywayMigration migration;

    @Inject
    private NormalizationServiceImpl normalization;

    @Inject
    private ContentExtractionService contentExtractionService;

    public static void main(String[] args) {
        final Injector injector = JZenith.application(args)
                .withModules(new PersistenceModule(), new BusinessModule())
                .createInjectorForTesting();

        final NormalizationApp app = new NormalizationApp();
        injector.injectMembers(app);

        app.run(args);

    }

    private void run(String[] args) {
        migration.migrate();

        normalization.run();
        contentExtractionService.run();
    }

}
