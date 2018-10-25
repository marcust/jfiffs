package org.thiesen.jfiffs.opml;

import com.google.inject.AbstractModule;

public class OpmlModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(OpmlImporter.class).asEagerSingleton();


    }
}
