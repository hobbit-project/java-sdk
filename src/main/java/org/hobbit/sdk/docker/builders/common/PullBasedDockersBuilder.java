package org.hobbit.sdk.docker.builders.common;

import org.hobbit.sdk.docker.PullBasedDockerizer;


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
