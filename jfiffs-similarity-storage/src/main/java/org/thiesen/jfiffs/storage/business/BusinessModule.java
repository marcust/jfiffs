package org.thiesen.jfiffs.storage.business;

import com.google.inject.AbstractModule;
import org.thiesen.jfiffs.storage.business.impl.StorageServiceImpl;

public class BusinessModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(StorageService.class).to(StorageServiceImpl.class);
    }
}
