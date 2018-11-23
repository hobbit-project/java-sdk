package org.hobbit.sdk.docker.builders;

import org.hobbit.sdk.docker.PullBasedDockerizer;


public class PullBasedDockersBuilder extends AbstractDockersBuilder {

    public PullBasedDockersBuilder(String imageName){
        super(imageName);
        super.imageName(imageName);
        String[] splitted = imageName.split("/");
        super.containerName(splitted[splitted.length-1]);
    }



    @Override
    public PullBasedDockerizer build() {
        return new PullBasedDockerizer(this);

    }
}
