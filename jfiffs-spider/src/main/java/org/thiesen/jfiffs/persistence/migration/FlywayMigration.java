package org.thiesen.jfiffs.persistence.migration;

import com.google.inject.Inject;
import org.flywaydb.core.Flyway;

import javax.sql.DataSource;

public class FlywayMigration {

    private final DataSource dataSource;

    @Inject
    public FlywayMigration(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void migrate() {
        // Create the Flyway instance
        final Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .load();

        // Start the migration
        flyway.migrate();
    }

}
