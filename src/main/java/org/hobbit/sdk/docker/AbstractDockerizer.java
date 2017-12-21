package org.hobbit.sdk.docker;

import org.hobbit.core.components.Component;
import org.hobbit.sdk.docker.builders.common.AbstractDockersBuilder;
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
import java.util.*;
import java.util.concurrent.*;

import static java.lang.String.format;

/**
 * @author Roman Katerinenko
 */
public abstract class AbstractDockerizer implements Component {

    private String name;
    public Logger logger;

    private String imageName;
    private String hostName;
    private String containerName;
    private Map<String, List<PortBinding>> portBindings;
    private Collection<String> environmentVariables;
    private Collection<String> networks;

    public Exception exception;
    private String containerId;

    private Boolean skipLogsReading;
    public Boolean useCachedContainer;
    public DockerClient dockerClient;
    private Callable onTermination;


    protected AbstractDockerizer(AbstractDockersBuilder builder) {
        name = builder.getName();
        logger = LoggerFactory.getLogger(name);
        imageName = builder.getImageName();
        hostName = builder.getHostName();
        containerName = builder.getContainerName();
        portBindings = builder.getPortBindings();
        environmentVariables = builder.getEnvironmentVariables();
        networks = builder.getNetworks();
        skipLogsReading = builder.getSkipLogsReading();
        useCachedContainer = builder.getUseCachedContainer();
        onTermination = builder.getOnTermination();
    }


    @Override
    public void init() {

    }

    @Override
    public void run() {
        try {
            stop(true);
            createContainerIfNotExists();
            int readLogsSince = (int)(System.currentTimeMillis() / 1000L);
            startContainer();
            startMonitoringAndLogsReading(readLogsSince);

        }catch (DockerRequestException e){
            logger.error("Exception: {}", e.getResponseBody());
            exception = e;
        }
        catch (Exception e){
            logger.error("Exception: {}", e);
            exception = e;
        }
    }

    public DockerClient getDockerClient() throws DockerCertificateException {
        if(dockerClient==null)
            dockerClient = DefaultDockerClient.fromEnv().build();
        return dockerClient;
    }

    public void stop() throws InterruptedException, DockerException, DockerCertificateException {
        stop(false);
    }

    public void stop(Boolean onstart) throws InterruptedException, DockerException, DockerCertificateException {
        logger.debug("Stopping containers and removing if needed (imageName={})", imageName);
        try {
            if(useCachedContainer!=null && useCachedContainer)
                stopContainer();
            else if(onstart)
                removeAllSameNamedContainers();
        }
        catch (Exception e){
            logger.error("Exception", e);
            exception = e;
        }
    }

    public void setOnTermination(Callable value){
        this.onTermination = value;
    }

    public String getName(){
        return name;
    }
    public String getImageName(){
        return imageName;
    }
    public String getContainerName(){
        return containerName;
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

    public void removeAllSameNamedImages() throws DockerException, InterruptedException, DockerCertificateException {
        logger.debug("Removing images (imageName={})", imageName);

        removeAllSameNamedImages(imageName);
    }


    public void prepareImage() throws InterruptedException, DockerException, DockerCertificateException, IOException{
        prepareImage(imageName);
    };

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
        getDockerClient().restartContainer(containerId);
        //connectContainerToNetworks(networks);
        //logger.debug("Waiting till container will start (imageName={})", imageName);
        //awaitRunning(dockerClient, containerId);
        logger.debug("Container started (imageName={})", imageName);
    }


    public void startMonitoringAndLogsReading(int since) throws Exception {
        logger.debug("Starting monitoring & logs reading for container (imageName={})", imageName);

        ExecutorService threadPool = Executors.newCachedThreadPool();

        Callable<String> callable = () -> {
            //String ret="";
            LogStream logStream = null;

            String logs="";
            String prevLogs="";
            Boolean running = getDockerClient().inspectContainer(containerId).state().running();
            while(running){
                running = getDockerClient().inspectContainer(containerId).state().running();
                if (skipLogsReading==null || !skipLogsReading){
                    try {
                        logStream = getDockerClient().logs(containerId,
                                DockerClient.LogsParam.stderr(),
                                DockerClient.LogsParam.stdout(),
                                DockerClient.LogsParam.since(since));
                        logs = logStream.readFully();
                    } catch (Exception e) {
                        logger.debug(String.format("No logs are available (imageName=%s):", imageName), e);
                    } finally {
                        if (logStream != null) {
                            logStream.close();
                        }
                    }

                    if (logs.length() > prevLogs.length()) {
                        String logsToPrint = logs.substring(prevLogs.length());
                        if (logsToPrint.toLowerCase().contains("error"))
                            logger.error(logsToPrint);
                        else
                            logger.debug(logsToPrint);
                        prevLogs = logs;
                    }
                }
                Thread.sleep(1000);
            }
            if(onTermination!=null)
                onTermination.call();
            stop();
            return "";
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
            //logger.debug("Logs reader was attached");
            //e.printStackTrace();
        }
        //}else
        //callable.call();

    }

    public void waitForContainerFinish() throws DockerException, InterruptedException, DockerCertificateException {
        logger.debug("Waiting for container finish (imageName={})", imageName);


        getDockerClient().waitContainer(containerId);

    }

    public void stopContainer(){
        logger.debug("Stopping containers (imageName={})", imageName);
        try{
            if(containerId==null){
                List<String> results = findContainersByName(containerName);
                if(results.size()>0)
                    containerId = results.get(0);
            }
            if(containerId!=null)
                getDockerClient().stopContainer(containerId, 0);
        }
        catch (Exception e){
            logger.error("Exception", e);
            exception = e;
        }
    }


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

        ContainerConfig containerConfig = builder .build();
        ContainerCreation creation = getDockerClient().createContainer(containerConfig, containerName);
        containerId = creation.id();
        if (containerId == null) {
            IllegalStateException exception = new IllegalStateException(format("Unable to create container %s", containerName));
            logger.error(String.format("Failed to create container (imageName=%s): ",imageName), exception);
            throw exception;
        }

        connectContainerToNetworks(networks);

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



    private ContainerInfo getContainerInfo(DockerClient dockerClient) {
        if (containerId == null) {
            return null;
        }
        ContainerInfo info = null;
        try {
            info = getDockerClient().inspectContainer(containerId);
        } catch (ContainerNotFoundException e) {
            logger.error("The container " + containerId + " is not known.", e);
        } catch (Throwable e) {
            logger.error("Error while checking status of container " + containerId + ".", e);
        }
        return info;
    }

    private void connectContainerToNetworks(Collection<String> networks) throws DockerException, InterruptedException, DockerCertificateException {
        logger.debug("Connecting container to networks (imageName={})", imageName);

//        ContainerInfo info = getContainerInfo(dockerClient);
//        Map<String, AttachedNetwork> prev_networks = info.networkSettings().networks();
//        for (String networkName : prev_networks.keySet()) {
//            getDockerClient().disconnectFromNetwork(containerId, networkName);
//        }

        for (String network : networks) {
            String networkId = createDockerNetworkIfNeeded(dockerClient, network);
            getDockerClient().connectToNetwork(containerId, networkId);
        }
    }

    public  String createDockerNetworkIfNeeded(DockerClient dockerClient, String networkName) throws
            DockerException, InterruptedException, DockerCertificateException {
        for (Network network : getDockerClient().listNetworks()) {
            if (network.name() != null && network.name().equals(networkName)) {
                return network.id();
            }
        }
        NetworkConfig networkConfig = NetworkConfig.builder()
                .name(networkName)
                .build();
        return getDockerClient().createNetwork(networkConfig).id();
    }

    private Set<String> getExposedPorts() {
        return portBindings.keySet();
    }

    private String[] getEnvironmentVariables() {
        return environmentVariables.toArray(new String[environmentVariables.size()]);
    }

    private List<String> findContainersByName(String containerName) throws
            DockerException, InterruptedException, DockerCertificateException {
        List<String> ret = new ArrayList<String>();
        for(Container container : getDockerClient().listContainers(DockerClient.ListContainersParam.allContainers())) {
            for(String name : container.names()){
                if (name.equals(dockerizeContainerName(containerName)))
                    ret.add(container.id());
            }
        }

        return ret;
    }

    private void removeAllSameNamedContainers(String containerName) throws
            DockerException, InterruptedException, DockerCertificateException {
        for(String name : findContainersByName(containerName)) {
            boolean removed = false;
            while (!removed) {
                try {
                    getDockerClient().removeContainer(name, DockerClient.RemoveContainerParam.forceKill());
                    removed = true;
                    containerId = null;
                } catch (Exception e) {
                    //if(!e.getMessage().contains("not found"))
                    logger.error("Exception", e);
                }
            }
        }
    }

    private static String dockerizeContainerName(String intendedContainerName) {
        return format("/%s", intendedContainerName);
    }

    private List<String> findImagesByName(String imageName) throws DockerException, InterruptedException, DockerCertificateException {
        List<String> ret = new ArrayList<>();
        String imageNameToSearch = (imageName.contains(":")?imageName:imageName+":");
        for (Image image : getDockerClient().listImages(DockerClient.ListImagesParam.allImages())) {
            ImmutableList<String> repoTags = image.repoTags();
            if (repoTags != null) {
                boolean nameMatch = repoTags.stream().anyMatch(name -> name != null && name.contains(imageNameToSearch));
                if (nameMatch)
                    ret.add(image.id());
            }
        }
        return ret;
    }

    private void removeAllSameNamedImages(String imageName) throws DockerException, InterruptedException, DockerCertificateException {
        boolean force = true;
        boolean dontDeleteUntaggedParents = false;
        for (String id : findImagesByName(imageName)){
            getDockerClient().removeImage(id, force, dontDeleteUntaggedParents);
        }
    }


    public Exception anyExceptions() {
        return exception;
    }

    public static String toEnvironmentEntry(String key, String value) {
        return format("%s=%s", key, value);
    }


    @Override
    public void close(){
        logger.debug("Close() (imageName={})", imageName);
//        try {
//            stop();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        } catch (DockerException e) {
//            e.printStackTrace();
//        } catch (DockerCertificateException e) {
//            e.printStackTrace();
//        }
    }
}