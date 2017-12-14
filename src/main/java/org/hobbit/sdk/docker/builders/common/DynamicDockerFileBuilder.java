package org.hobbit.sdk.docker.builders.common;

import org.hobbit.sdk.docker.BuildBasedDockerizer;

import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Pavel Smirnov
 */

public class DynamicDockerFileBuilder extends BuildBasedDockersBuilder {

    private Class[] runnerClass;
    private String dockerWorkDir;
    private String jarFilePath;
    //private Reader dockerFileReader;

    public DynamicDockerFileBuilder(String dockerizerName) {

        super(dockerizerName);

   }

    public DynamicDockerFileBuilder runnerClass(Class... values) {
        this.runnerClass = values;
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

    public DynamicDockerFileBuilder customDockerFileReader(Reader value) {
        super.dockerFileReader(value);
        return this;
    }

    protected Reader createDefaultReader() throws Exception {
        if(runnerClass==null)
            throw new Exception("Runner class is not specified for "+this.getClass().getSimpleName());

        if(dockerWorkDir ==null)
            throw new Exception("WorkingDirName class is not specified for "+this.getClass().getSimpleName());

        if(jarFilePath==null)
            throw new Exception("JarFileName class is not specified for "+this.getClass().getSimpleName());

        if(!Paths.get(getBuildDirectory(), jarFilePath).toFile().exists())
            throw new Exception(jarFilePath+" not found in "+getBuildDirectory());

        List<String> classNames = Arrays.stream(runnerClass).map(c->"\""+c.getCanonicalName()+"\"").collect(Collectors.toList());
        String content =
                "FROM java\n" +
                        "RUN mkdir -p "+ dockerWorkDir +"\n" +
                        "WORKDIR "+ dockerWorkDir +"\n" +
                        "ADD ./"+ jarFilePath +" "+ dockerWorkDir +"\n" +
                        "CMD [\"java\", \"-cp\", \""+ jarFilePath +"\", "+ String.join(",", classNames) +"]\n"
                        //"CMD [\"java\", \"-cp\", \""+ jarFilePath +"\", \""+runnerClass.getCanonicalName()+"\"]\n"
                ;
        return new StringReader(content);
    }

    public DynamicDockerFileBuilder init() throws Exception {
        if(getDockerFileReader()==null)
            super.dockerFileReader(createDefaultReader());
        return this;
    }

    @Override
    public BuildBasedDockerizer build() throws Exception {
        if(getDockerFileReader()==null)
            super.dockerFileReader(createDefaultReader());
        return super.build();
    }
}
