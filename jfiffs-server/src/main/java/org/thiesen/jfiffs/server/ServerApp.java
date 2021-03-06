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
package org.thiesen.jfiffs.server;

import org.jzenith.core.JZenith;
import org.jzenith.postgresql.PostgresqlPlugin;
import org.jzenith.rest.RestPlugin;
import org.thiesen.jfiffs.server.business.BusinessModule;
import org.thiesen.jfiffs.server.persistence.PersistenceModule;
import org.thiesen.jfiffs.server.resource.SimilarEntryResource;

public class ServerApp {

    public static void main(String[] args) {
        JZenith.application(args)
                .withPlugins(PostgresqlPlugin.create(),
                        RestPlugin.withResources(SimilarEntryResource.class))
                .withModules(new PersistenceModule(), new BusinessModule())
                .withConfiguration("postgresql.database", "jfiffs")
                .withConfiguration("postgresql.username", "jfiffs")
                .withConfiguration("postgresql.password", "jfiffs")
                .withConfiguration("rest.port", 8181)
                .run();
    }

}
