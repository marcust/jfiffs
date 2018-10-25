package org.thiesen.jfiffs.persistence;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.jooq.Configuration;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.conf.Settings;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultConfiguration;
import org.jzenith.core.configuration.ConfigurationProvider;
import org.postgresql.ds.PGSimpleDataSource;
import org.thiesen.jfiffs.persistence.impl.FeedDaoImpl;
import org.thiesen.jfiffs.persistence.impl.FeedEntryDaoImpl;
import org.thiesen.jfiffs.persistence.migration.FlywayMigration;

import javax.sql.DataSource;

public class PersistenceModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(PostgresqlConfiguration.class).toProvider(new ConfigurationProvider<>(PostgresqlConfiguration.class)).asEagerSingleton();
        bind(DataSource.class).toProvider(new DataSourceProvider()).asEagerSingleton();

        bind(FeedDao.class).to(FeedDaoImpl.class).asEagerSingleton();
        bind(FeedEntryDao.class).to(FeedEntryDaoImpl.class).asEagerSingleton();

        bind(DSLContext.class).toProvider(new DslContextProvider()).asEagerSingleton();

        bind(FlywayMigration.class).asEagerSingleton();
    }

    private static class DataSourceProvider implements Provider<DataSource> {

        @Inject
        private PostgresqlConfiguration configuration;

        @Override
        public DataSource get() {
            final PGSimpleDataSource dataSource = new PGSimpleDataSource();
            dataSource.setPortNumber(configuration.getPort());
            dataSource.setServerName(configuration.getHost());
            dataSource.setDatabaseName(configuration.getDatabase());
            dataSource.setUser(configuration.getUsername());
            dataSource.setPassword(configuration.getPassword());

            final HikariConfig config = new HikariConfig();
            config.setDataSource(dataSource);

            final HikariDataSource hikariDataSource = new HikariDataSource(config);

            return hikariDataSource;
        }
    }

    private class DslContextProvider implements Provider<DSLContext> {

        @Inject
        private DataSource dataSource;

        @Override
        public DSLContext get() {
            final DSLContext context = DSL.using(dataSource, SQLDialect.POSTGRES_10);

            return context;
        }
    }
}
