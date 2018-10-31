package org.hobbit.sdk.docker.builders;

import org.hobbit.sdk.docker.BuildBasedDockerizer;
import com.spotify.docker.client.messages.PortBinding;

import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;

public class BuildBasedDockersBuilder extends AbstractDockersBuilder {

    //private String imageNamePrefix="";
    private Path buildDirectory;
    private Reader dockerFileReader;
    private Boolean useCachedImage;
    private String dockerfilePath;
    protected Path jarFilePath;

    public BuildBasedDockersBuilder(String dockerizerName){
        super(dockerizerName);
    }

    public BuildBasedDockersBuilder dockerFileReader(Reader value) {
        this.dockerFileReader = value;
        return this;
    }

    public BuildBasedDockersBuilder buildDirectory(String value) {
        if(value.equals("."))
            value = "";
        this.buildDirectory = Paths.get(value).toAbsolutePath();
        return this;
    }


    public BuildBasedDockersBuilder useCachedImage() {
        this.useCachedImage = true;
        return this;
    }

    public BuildBasedDockersBuilder useCachedImage(Boolean value) {
        this.useCachedImage = value;
        return this;
    }

    public BuildBasedDockersBuilder addPortBindings(String containerPort, PortBinding... hostPorts) {
        super.addPortBindings(containerPort, hostPorts);
        return this;
    }

    public BuildBasedDockersBuilder EnvironmentVariables(Collection<String> value) {
        super.environmentVariables(value);
        return this;
    }

    public BuildBasedDockersBuilder addEnvironmentVariable(String key, String value) {
        super.addEnvironmentVariable(key, value);
        return this;
    }

    public BuildBasedDockersBuilder containerName(String containerName) {
        super.containerName(containerName);
        return this;
    }

    public BuildBasedDockersBuilder hostName(String value) {
        super.hostName(value);
        return this;
    }

    public BuildBasedDockersBuilder imageName(String value) {
        super.imageName(value);
        return this;
    }


    public BuildBasedDockersBuilder skipLogsReading() {
        super.skipLogsReading();
        return this;
    }

    public BuildBasedDockersBuilder skipLogsReading(Boolean value) {
        super.skipLogsReading(value);
        return this;
    }

    public BuildBasedDockersBuilder useCachedContainer(){
        super.useCachedContainer();
        return this;
    }

    public BuildBasedDockersBuilder useCachedContainer(Boolean value){
        super.useCachedContainer(value);
        return this;
    }

    public BuildBasedDockersBuilder addNetworks(String... nets){
        super.addNetworks(nets);
        return this;
    }

    public BuildBasedDockersBuilder dockerfilePath(String value){
        dockerfilePath = value;
        return this;
    }


    public Path getBuildDirectory(){ return buildDirectory; }
    public Reader getDockerFileReader(){ return dockerFileReader; }
    public Boolean getUseCachedImage(){ return useCachedImage;}
    public Path getJarFilePath() { return jarFilePath; }

    @Override
    public BuildBasedDockerizer build() throws Exception {

        if(buildDirectory==null)
            buildDirectory=Paths.get(".");

        if(dockerfilePath!=null){
            byte[] bytes = Files.readAllBytes(Paths.get(dockerfilePath));
            dockerFileReader = new StringReader(new String(bytes));
        }

        BuildBasedDockerizer ret = new BuildBasedDockerizer(this);
        return ret;
    }


}
