package com.agtinternational.hobbit.sdk.docker;

import java.io.Reader;
import java.io.StringReader;

/**
 * @author Pavel Smirnov
 */

public class DynamicDockerFileBuilder extends BuildBasedDockerizer.Builder{

    private Class runnerClass;
    private String dockerWorkDir;
    private String jarFilePath;

    public DynamicDockerFileBuilder(String dockerizerName) {
        super(dockerizerName);

    }

    public DynamicDockerFileBuilder runnerClass(Class runnerClass) {
        this.runnerClass = runnerClass;
        return this;
    }

    public DynamicDockerFileBuilder dockerWorkDir(String value) {
        this.dockerWorkDir = value;
        return this;
    }

    public DynamicDockerFileBuilder jarFilePath(String value) {
        this.jarFilePath = value;
        return this;
    }

    protected Reader getDockerFileContent() throws Exception {
        if(runnerClass==null)
            throw new Exception("Runner class is not specified for "+this.getClass().getSimpleName());

        if(dockerWorkDir ==null)
            throw new Exception("WorkingDirName class is not specified for "+this.getClass().getSimpleName());

        if(jarFilePath ==null)
            throw new Exception("JarFileName class is not specified for "+this.getClass().getSimpleName());

        String content =
                "FROM java\n" +
                        "RUN mkdir -p "+ dockerWorkDir +"\n" +
                        "WORKDIR "+ dockerWorkDir +"\n" +
                        "ADD ./"+ jarFilePath +" "+ dockerWorkDir +"\n" +
                        "CMD [\"java\", \"-cp\", \""+ jarFilePath +"\", \"org.hobbit.core.run.ComponentStarter\", \""+runnerClass.getCanonicalName()+"\"]\n"
                ;
        return new StringReader(content);
    }

    @Override
    public BuildBasedDockerizer build() throws Exception {

        dockerFileReader(getDockerFileContent());
        return super.build();
    }
}
