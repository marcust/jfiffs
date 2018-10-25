package org.thiesen.jfiffs.business;

import com.google.inject.AbstractModule;
import org.thiesen.jfiffs.business.impl.FeedSpiderServiceImpl;

public class BusinessModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(FeedSpiderService.class).to(FeedSpiderServiceImpl.class);

    }
}
