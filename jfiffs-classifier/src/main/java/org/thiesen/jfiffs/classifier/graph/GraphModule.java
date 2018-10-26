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
package org.thiesen.jfiffs.classifier.graph;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.db.OrientDB;
import com.orientechnologies.orient.core.db.OrientDBConfig;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OType;
import org.jzenith.core.configuration.ConfigurationProvider;

public class GraphModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(OrientDbConfiguration.class).toProvider(new ConfigurationProvider<>(OrientDbConfiguration.class)).asEagerSingleton();

        bind(ODatabaseSession.class).toProvider(new GraphProvider()).asEagerSingleton();
    }

    private static class GraphProvider implements Provider<ODatabaseSession> {

        @Inject
        private OrientDbConfiguration configuration;

        @Override
        public ODatabaseSession get() {
            final OrientDB orientDB = new OrientDB(configuration.getUrl(), OrientDBConfig.defaultConfig());
            final ODatabaseSession session = orientDB.open(configuration.getDatabase(), configuration.getUser(), configuration.getPassword());

            OClass feedEntry = session.getClass("feedEntry");
            if (feedEntry == null) {
                feedEntry = session.createVertexClass("feedEntry");
            }

            if (feedEntry.getProperty("title") == null) {
                feedEntry.createProperty("title", OType.STRING);
            }
            if (feedEntry.getProperty("id") == null) {
                feedEntry.createProperty("id", OType.STRING);
                feedEntry.createIndex("entry_id_index", OClass.INDEX_TYPE.UNIQUE, "id");
            }

            OClass similarTo = session.getClass("similarTo");
            if (similarTo == null) {
                similarTo = session.createEdgeClass("similarTo");
            }
            if (similarTo.getProperty("value") == null) {
                similarTo.createProperty("value", OType.DOUBLE);
            }

            return session;
        }
    }
}
