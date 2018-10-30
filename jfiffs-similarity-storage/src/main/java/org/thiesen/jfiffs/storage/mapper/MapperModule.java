package org.thiesen.jfiffs.storage.mapper;

import com.google.inject.AbstractModule;

public class MapperModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(ReplyMapper.class);
    }
}
