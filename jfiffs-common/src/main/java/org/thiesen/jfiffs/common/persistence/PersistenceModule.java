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
package org.thiesen.jfiffs.common.persistence;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jzenith.core.configuration.ConfigurationProvider;
import org.postgresql.ds.PGSimpleDataSource;
import org.thiesen.jfiffs.common.persistence.impl.EntrySimilarityDaoImpl;
import org.thiesen.jfiffs.common.persistence.impl.FeedDaoImpl;
import org.thiesen.jfiffs.common.persistence.impl.FeedEntryDaoImpl;
import org.thiesen.jfiffs.common.persistence.migration.FlywayMigration;

import javax.sql.DataSource;

public class PersistenceModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(PostgresqlConfiguration.class).toProvider(new ConfigurationProvider<>(PostgresqlConfiguration.class)).asEagerSingleton();
        bind(DataSource.class).toProvider(new DataSourceProvider()).asEagerSingleton();

        bind(FeedDao.class).to(FeedDaoImpl.class).asEagerSingleton();
        bind(FeedEntryDao.class).to(FeedEntryDaoImpl.class).asEagerSingleton();
        bind(EntrySimilarityDao.class).to(EntrySimilarityDaoImpl.class).asEagerSingleton();

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
            config.setMaximumPoolSize(Runtime.getRuntime().availableProcessors() * 10);

            final HikariDataSource hikariDataSource = new HikariDataSource(config);

            return hikariDataSource;
        }
    }

    private class DslContextProvider implements Provider<DSLContext> {

        @Inject
        private DataSource dataSource;

        @Override
        public DSLContext get() {
            final DSLContext context = DSL.using(dataSource, SQLDialect.POSTGRES_9_5);

            return context;
        }
    }
}
