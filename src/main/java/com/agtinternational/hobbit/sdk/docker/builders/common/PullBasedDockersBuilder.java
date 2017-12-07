package com.agtinternational.hobbit.sdk.docker.builders.common;

import com.agtinternational.hobbit.sdk.docker.PullBasedDockerizer;


public class PullBasedDockersBuilder extends AbstractDockersBuilder {

    public PullBasedDockersBuilder(String name){
        super(name);
    }

    public PullBasedDockersBuilder imageName(String value) {
        super.imageName(value);
        return this;
    }

    @Override
    public PullBasedDockerizer build() {
        return new PullBasedDockerizer(this);

    }
}
