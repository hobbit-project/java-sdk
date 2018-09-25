package org.hobbit.sdk.docker.builders;

import org.hobbit.sdk.docker.AbstractDockerizer;
import com.spotify.docker.client.messages.PortBinding;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

public abstract class AbstractDockersBuilder {
    private String name;
    private String imageName;

    private String hostName;
    private String containerName;
    private Boolean useCachedContainer;
    private Callable onTermination;

    private Map<String, List<PortBinding>> portBindings = new HashMap<>();
    private Collection<String> environmentVariables = new HashSet<>();
    private Collection<String> networks = new HashSet<>();

    private Boolean skipLogsReading;

    public AbstractDockersBuilder(String name){
        this.name = name;
    }

    public AbstractDockersBuilder addNetworks(String... nets) {
        if (nets != null) {
            Stream.of(nets).forEach(networks::add);
        }
        return this;
    }

    public AbstractDockersBuilder networks(Collection<String> value) {
        networks = value;
        return this;
    }

    public AbstractDockersBuilder addPortBindings(String containerPort, PortBinding... hostPorts) {
        List<PortBinding> hostPortsList = new ArrayList<>();
        hostPortsList.addAll(Arrays.asList(hostPorts));
        portBindings.put(String.valueOf(containerPort), hostPortsList);
        return this;
    }

    public AbstractDockersBuilder addPortBindings(String containerPort, String hostIp, String hostPort) {
        List<PortBinding> hostPortsList = new ArrayList<>();
        hostPortsList.add(new PortBinding() {
            @Nullable
            @Override
            public String hostIp() {
                return hostIp;
            }

            @Override
            public String hostPort() {
                return hostPort;
            }
        });
        portBindings.put(String.valueOf(containerPort), hostPortsList);
        return this;
    }

    public AbstractDockersBuilder portBindings(Map<String, List<PortBinding>> value) {
        portBindings = value;
        return this;
    }

    public AbstractDockersBuilder environmentVariables(Collection<String> value) {
        environmentVariables = value;
        return this;
    }

    public AbstractDockersBuilder addEnvironmentVariable(String key, String value) {
        if(environmentVariables.stream().filter(v->v.startsWith(key+"=")).count()==0)
            environmentVariables.add(AbstractDockerizer.toEnvironmentEntry(key, value));
        return this;
    }

    public AbstractDockersBuilder name(String value) {
        this.name = value;
        return this;
    }

    public AbstractDockersBuilder containerName(String containerName) {
        this.containerName = containerName;
        return this;
    }

    public AbstractDockersBuilder hostName(String value) {
        this.hostName = value;
        return this;
    }

    public AbstractDockersBuilder imageName(String value) {
        this.imageName = value;
        return this;
    }

    public AbstractDockersBuilder onTermination(Callable value){
        this.onTermination = value;
        return this;
    }

    public AbstractDockersBuilder skipLogsReading() {
        this.skipLogsReading = true;
        return this;
    }

    public AbstractDockersBuilder skipLogsReading(Boolean value) {
        this.skipLogsReading = value;
        return this;
    }

    public AbstractDockersBuilder useCachedContainer(){
        this.useCachedContainer = true;
        return this;
    }

    public AbstractDockersBuilder useCachedContainer(Boolean value){
        this.useCachedContainer = value;
        return this;
    }

    public String getName(){ return name; }
    public String getImageName(){ return imageName; }
    public String getHostName(){ return hostName;}
    public String getContainerName(){ return containerName; }
    public Boolean getUseCachedContainer(){ return useCachedContainer; }
    public Map<String, List<PortBinding>> getPortBindings(){ return portBindings; };
    public Collection<String> getEnvironmentVariables(){ return environmentVariables;}
    public Collection<String> getNetworks(){ return networks; }
    public Boolean getSkipLogsReading(){ return skipLogsReading; };
    public Callable getOnTermination(){ return onTermination; };

    public abstract AbstractDockerizer build() throws Exception;


}
