package com.agtinternational.hobbit.sdk.docker;

import com.google.common.collect.ImmutableList;
import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.LogStream;
import com.spotify.docker.client.exceptions.ContainerNotFoundException;
import com.spotify.docker.client.exceptions.DockerCertificateException;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.exceptions.DockerRequestException;
import com.spotify.docker.client.messages.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Stream;

import static java.lang.String.format;

/**
 * @author Roman Katerinenko
 */
public abstract class AbstractDockerizer implements Runnable {

    private String name;
    public Logger logger;

    public String imageName;
    public String hostName;
    private String containerName;
    private Map<String, List<PortBinding>> portBindings;
    private Collection<String> environmentVariables;
    private Collection<String> networks;

    public Exception exception;
    public String containerId;
    public Boolean useCachedContainer;
    private Boolean skipLogsReading;
    public DockerClient dockerClient;


    protected AbstractDockerizer(AbstractDockerizerBuilder abstractDockerizerBuilder) {
        name = abstractDockerizerBuilder.name;
        logger = LoggerFactory.getLogger(name);
        imageName = abstractDockerizerBuilder.imageName;
        hostName = abstractDockerizerBuilder.hostName;
        containerName = abstractDockerizerBuilder.containerName;
        portBindings = abstractDockerizerBuilder.portBindings;
        environmentVariables = abstractDockerizerBuilder.environmentVariables;
        networks = abstractDockerizerBuilder.networks;
        useCachedContainer = abstractDockerizerBuilder.useCachedContainer;
        skipLogsReading = abstractDockerizerBuilder.skipLogsReading;
    }

        @Override
    public void run() {
        try {
            dockerClient = DefaultDockerClient.fromEnv().build();
            stop(true);
            createContainerIfNotExists();
            startContainer();
            if (skipLogsReading==null || !skipLogsReading)
                attachToContainerAndReadLogs();

        }catch (DockerRequestException e){
            logger.error("Exception: {}", e.getResponseBody());
            exception = e;
        }
        catch (Exception e){
            logger.error("Exception: {}", e);
            exception = e;
        }
    }

    public void stop() throws InterruptedException, DockerException, DockerCertificateException {
        stop(false);
    }

    public void stop(Boolean onStart) throws InterruptedException, DockerException, DockerCertificateException {
        logger.debug("Stopping containers and removing if needed (imageName={})", imageName);
        try {
//            if (!onStart && (skipLogsReading==null || !skipLogsReading))
//                attachToContainerAndReadLogs();

            if (this.useCachedContainer!=null && this.useCachedContainer) {
                stopCachedContainer();
            } else {
                removeAllSameNamedContainers();
                removeAllSameNamedImages();
            }
        }
        catch (Exception e){
            logger.error("Exception", e);
            exception = e;
        }
    }

    public String getName(){
        return name;
    }
    public String getHostName(){ return hostName;}

    public void removeAllSameNamedContainers(){
        logger.debug("Removing containers (imageName={})", imageName);
        try {
            removeAllSameNamedContainers(containerName);
        }
        catch (Exception e){
            logger.error("Exception", e);
            exception = e;
        }
    }

    private void removeAllSameNamedImages() throws DockerException, InterruptedException, DockerCertificateException {
        logger.debug("Removing images (imageName={})", imageName);

        removeAllSameNamedImages(imageName);
    }


    public abstract void prepareImage(String imageName) throws InterruptedException, DockerException, DockerCertificateException, IOException;


    private void createContainerIfNotExists() throws DockerException, InterruptedException, DockerCertificateException, IOException {
        if(containerId!=null)
            return;

        List<String> results = findContainersByName(containerName);
        if(results.size()==0)
            createContainer();
        else
            containerId = results.get(0);
    }

    private void startContainer() throws Exception {
        logger.debug("Starting container (imageName={})", imageName);
        dockerClient.startContainer(containerId);
        connectContainerToNetworks(networks);
        //logger.debug("Waiting till container will start (imageName={})", imageName);
        //awaitRunning(dockerClient, containerId);
        logger.debug("Container started (imageName={})", imageName);
    }

    /**
     * Blocks until the container is terminated
     */
    public void attachToContainerAndReadLogs() throws Exception {
        logger.debug("Attaching to logs for container (imageName={})", imageName);

        ExecutorService threadPool = Executors.newCachedThreadPool();
        Callable<String> callable = () -> {
            String ret="";
            LogStream logStream = null;
            try{
                String logs;
                String prevLogs="";
                while(true) {
                    logStream = dockerClient.logs(containerId, DockerClient.LogsParam.stderr(), DockerClient.LogsParam.stdout());
                    logs = logStream.readFully();
                    if(logs.length()>0 && !logs.equals(prevLogs)) {
                        String logsToPrint = logs.substring(prevLogs.length());
                        if (logsToPrint.toLowerCase().contains("error"))
                            logger.error(logsToPrint);
                        else
                            logger.debug(logsToPrint);
                        prevLogs = logs;
                    }
                    Thread.sleep(1000);
                }
            } catch (Exception e) {
                logger.debug(String.format("No logs are available (imageName=%s):",imageName), e);
            } finally {
                if (logStream != null) {
                    logStream.close();
                }
            }
            return ret;
        };
        //if(interruptable) {
        Future<String> future = threadPool.submit(callable);
        try {
            future.get(3000, TimeUnit.MILLISECONDS);
        } catch (ExecutionException e) {
            throw new RuntimeException(e.getCause());
        } catch (InterruptedException e) {
            logger.debug("Logs reader was failed to attach");
            Thread.currentThread().interrupt();
            //throw new TimeoutException();
        } catch (TimeoutException e) {
            logger.debug("Logs reader was attached");
            //e.printStackTrace();
        }
        //}else
            //callable.call();

    }

    public void waitForContainerFinish() throws DockerException, InterruptedException, DockerCertificateException {
        logger.debug("Waiting for container finish (imageName={})", imageName);


            dockerClient.waitContainer(containerId);

    }

    public void stopCachedContainer(){
        logger.debug("Stopping containers (imageName={})", imageName);
        try{
            if(containerId==null){
                List<String> results = findContainersByName(containerName);
                if(results.size()>0)
                    containerId = results.get(0);
            }
            if(containerId!=null)
                dockerClient.stopContainer(containerId, 0);
        }
        catch (Exception e){
            logger.error("Exception", e);
            exception = e;
        }
    }

//    public void stopContainer() throws DockerException, InterruptedException, DockerCertificateException {
//        logger.debug("Stopping container (imageName={})", imageName);
//            dockerClient.stopContainer(containerId, 0);
//    }



    private void createContainer() throws DockerException, InterruptedException, DockerCertificateException, IOException {

        if(findImagesByName(imageName).size()==0)
            prepareImage(imageName);

        logger.debug("Creating container (imageName={})", imageName);
        boolean removeContainerWhenItExits = false;
        HostConfig hostConfig = HostConfig.builder()
                .autoRemove(removeContainerWhenItExits)
                .portBindings(portBindings)
                .dns("1.2.3.4")
                .dnsSearch("rabbit")
                .build();

        ContainerConfig.Builder builder = ContainerConfig.builder()
                .hostConfig(hostConfig)
                .exposedPorts(getExposedPorts())
                .image(imageName)
                .env(getEnvironmentVariables());

//        if(hostName!=null) {
//            Map endPointConf = new HashMap<>();
//            endPointConf.put("alias",hostName)
//            ContainerConfig.NetworkingConfig networkConfig = ContainerConfig.NetworkingConfig.create();
//            //networkConfig.endpointsConfig()..addOption("network-alias", hostName).build();
//            builder.hostname(hostName)
//                    .networkingConfig(networkConfig);
//        }

        ContainerConfig containerConfig = builder .build();
        ContainerCreation creation = dockerClient.createContainer(containerConfig, containerName);
        containerId = creation.id();
        if (containerId == null) {
            IllegalStateException exception = new IllegalStateException(format("Unable to create container %s", containerName));
            logger.error(String.format("Failed to create container (imageName=%s): ",imageName), exception);
            throw exception;
        }

        //connectContainerToNetworks(networks);

    }

    private static void awaitRunning(final DockerClient client, final String containerId)
            throws Exception {
        Boolean running = false;
        while (!running) {
            final ContainerInfo containerInfo = client.inspectContainer(containerId);
            running = containerInfo.state().running() ? true : false;
            Thread.sleep(300);
        }
    }


    public ContainerInfo getContainerInfo(DockerClient dockerClient) {
        if (containerId == null) {
            return null;
        }
        ContainerInfo info = null;
        try {
            info = dockerClient.inspectContainer(containerId);
        } catch (ContainerNotFoundException e) {
            logger.error("The container " + containerId + " is not known.", e);
        } catch (Throwable e) {
            logger.error("Error while checking status of container " + containerId + ".", e);
        }
        return info;
    }

    private void connectContainerToNetworks(Collection<String> networks) throws DockerException, InterruptedException {
        logger.debug("Connecting container to networks (imageName={})", imageName);

        ContainerInfo info = getContainerInfo(dockerClient);
        Map<String, AttachedNetwork> prev_networks = info.networkSettings().networks();
        for (String networkName : prev_networks.keySet()) {
            dockerClient.disconnectFromNetwork(containerId, networkName);
        }

        for (String network : networks) {
            String networkId = createDockerNetworkIfNeeded(dockerClient, network);
            dockerClient.connectToNetwork(containerId, networkId);
        }
    }

    public static String createDockerNetworkIfNeeded(DockerClient dockerClient, String networkName) throws
            DockerException, InterruptedException {
        for (Network network : dockerClient.listNetworks()) {
            if (network.name() != null && network.name().equals(networkName)) {
                return network.id();
            }
        }
        NetworkConfig networkConfig = NetworkConfig.builder()
                .name(networkName)
                .build();
        return dockerClient.createNetwork(networkConfig).id();
    }

    private Set<String> getExposedPorts() {
        return portBindings.keySet();
    }

    private String[] getEnvironmentVariables() {
        return environmentVariables.toArray(new String[environmentVariables.size()]);
    }



    private List<String> findContainersByName(String containerName) throws
            DockerException, InterruptedException {
        List<String> ret = new ArrayList<String>();
        for(Container container : dockerClient.listContainers(DockerClient.ListContainersParam.allContainers())) {
            for(String name : container.names()){
                if (name.equals(dockerizeContainerName(containerName)))
                    ret.add(container.id());
            }
        }

        return ret;
    }

    private void removeAllSameNamedContainers(String containerName) throws
            DockerException, InterruptedException {
        for(String name : findContainersByName(containerName))
            try {
                dockerClient.removeContainer(name, DockerClient.RemoveContainerParam.forceKill());
            }
            catch (Exception e) {
                //if(!e.getMessage().contains("not found"))
                logger.error("Exception", e);
            }
    }

    private static String dockerizeContainerName(String intendedContainerName) {
        return format("/%s", intendedContainerName);
    }

    private List<String> findImagesByName(String imageName) throws DockerException, InterruptedException {
        List<String> ret = new ArrayList<>();
        String imageNameToSearch = (imageName.contains(":")?imageName:imageName+":");
        for (Image image : dockerClient.listImages(DockerClient.ListImagesParam.allImages())) {
            ImmutableList<String> repoTags = image.repoTags();
            if (repoTags != null) {
                boolean nameMatch = repoTags.stream().anyMatch(name -> name != null && name.contains(imageNameToSearch));
                if (nameMatch)
                    ret.add(image.id());
            }
        }
        return ret;
    }

    private void removeAllSameNamedImages(String imageName) throws DockerException, InterruptedException {
        boolean force = true;
        boolean dontDeleteUntaggedParents = false;
        for (String id : findImagesByName(imageName)){
            dockerClient.removeImage(id, force, dontDeleteUntaggedParents);
        }
    }



    public Exception anyExceptions() {
        return exception;
    }

    public static String toEnvironmentEntry(String key, String value) {
        return format("%s=%s", key, value);
    }

    public abstract static class AbstractDockerizerBuilder {
        private final String name;
        private String imageName;
        private String hostName;
        private String containerName;

        private final Map<String, List<PortBinding>> portBindings = new HashMap<>();
        private final Collection<String> environmentVariables = new HashSet<>();
        private final Collection<String> networks = new HashSet<>();

        private Boolean useCachedContainer;
        private Boolean skipLogsReading;

        public AbstractDockerizerBuilder(String name) {
            this.name = name;
        }

        public AbstractDockerizerBuilder addNetworks(String... nets) {
            if (nets != null) {
                Stream.of(nets).forEach(networks::add);
            }
            return this;
        }


        public AbstractDockerizerBuilder addPortBindings(String containerPort, PortBinding... hostPorts) {
            List<PortBinding> hostPortsList = new ArrayList<>();
            hostPortsList.addAll(Arrays.asList(hostPorts));
            portBindings.put(String.valueOf(containerPort), hostPortsList);
            return this;
        }

        public AbstractDockerizerBuilder addEnvironmentVariable(String key, String value) {
            environmentVariables.add(toEnvironmentEntry(key, value));
            return this;
        }

        public AbstractDockerizerBuilder containerName(String containerName) {
            this.containerName = containerName;
            return this;
        }

        public AbstractDockerizerBuilder imageName(String imageName) {
            this.imageName = imageName;
            return this;
        }

        public AbstractDockerizerBuilder hostName(String value) {
            this.hostName = value;
            return this;
        }


        public AbstractDockerizerBuilder skipLogsReading() {
            this.skipLogsReading = true;
            return this;
        }

        public AbstractDockerizerBuilder skipLogsReading(Boolean value) {
            this.skipLogsReading = value;
            return this;
        }

        public AbstractDockerizerBuilder useCachedContainer() {
            this.useCachedContainer = true;
            return this;
        }

        public AbstractDockerizerBuilder useCachedContainer(Boolean value) {
            this.useCachedContainer = value;
            return this;
        }

        public String getName() {
            return name;
        }

        public String getImageName() {
            return imageName;
        }

        public String getContainerName() {
            return containerName;
        }

        public abstract AbstractDockerizer build() throws Exception;


        @Override
        public String toString() {
            return "AbstractDockerizerBuilder{" +
                    "name='" + name + '\'' +
                    ", environmentVariables=" + environmentVariables +
                    ", networks=" + networks +
                    ", imageName='" + imageName + '\'' +
                    ", containerName='" + containerName + '\'' +
                    ", useCachedContainer='" + useCachedContainer + '\'' +
                    ", skipLogsReading='" + skipLogsReading + '\'' +
                    '}';
        }
    }
}