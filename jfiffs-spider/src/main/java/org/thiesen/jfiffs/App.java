package org.thiesen.jfiffs;

import com.google.inject.Inject;
import com.google.inject.Injector;
import org.jzenith.core.JZenith;
import org.thiesen.jfiffs.business.BusinessModule;
import org.thiesen.jfiffs.business.FeedSpiderService;
import org.thiesen.jfiffs.opml.OpmlImporter;
import org.thiesen.jfiffs.persistence.PersistenceModule;
import org.thiesen.jfiffs.persistence.migration.FlywayMigration;

public class App {

    @Inject
    private FlywayMigration migration;

    @Inject
    private OpmlImporter opmlImporter;

    @Inject
    private FeedSpiderService feedSpiderService;

    public static void main(String[] args) {
        final Injector injector = JZenith.application(args)
                .withModules(new PersistenceModule(), new BusinessModule())
                .createInjectorForTesting();

        final App app = new App();
        injector.injectMembers(app);

        app.run(args);

    }

    private void run(String[] args) {
        migration.migrate();
        opmlImporter.importOpml();

        feedSpiderService.run();

    }

}
