package org.hobbit.sdk.docker.builders;

import org.hobbit.sdk.docker.AbstractDockerizer;

public abstract class BothTypesDockersBuilder {

    private final AbstractDockersBuilder ret;
    private String imageName;

    public BothTypesDockersBuilder(AbstractDockersBuilder builder){
        ret = builder;
    }

//    public BothTypesDockersBuilder useCachedContainer(Boolean value){
//        ret.useCachedContainer(value);
//        return this;
//    }
//
//    public AbstractDockersBuilder useCachedContainer(Boolean value){
//        this.useCachedContainer = value;
//        return this;
//    }

    public abstract void addEnvVars(AbstractDockersBuilder ret);
    public abstract String getName();

    public String getImageName(){
        return ret.getImageName();
    }

    public AbstractDockerizer build() throws Exception {
        ret.name(getName()+"-dockerizer");
        if(ret.getImageName()==null)
            throw new Exception("ImageName not specified for "+getName());
        addEnvVars(ret);
        return ret.build();
    }

}
