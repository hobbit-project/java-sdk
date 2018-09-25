package org.hobbit.sdk.docker;

import org.hobbit.core.components.Component;
import org.hobbit.sdk.docker.builders.AbstractDockersBuilder;
import com.google.common.collect.ImmutableList;
import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.LogStream;
import com.spotify.docker.client.exceptions.ContainerNotFoundException;
import com.spotify.docker.client.exceptions.DockerCertificateException;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.exceptions.DockerRequestException;
import com.spotify.docker.client.messages.*;
import org.hobbit.sdk.docker.builders.BuildBasedDockersBuilder;
import org.hobbit.sdk.docker.builders.PullBasedDockersBuilder;
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
    private int instanceId=1;


    protected AbstractDockerizer(AbstractDockersBuilder builder){
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

            if(containerId==null){
                stop(true);
                containerId = createContainerIfNotExists();
            }

            List<Container> results = findContainersByName(containerName, DockerClient.ListContainersParam.allContainers());
            Boolean requiresStart = true;
            for(Container container : results)
                if(container.state().equals("running"))
                    requiresStart = false;

            if(requiresStart) {
                int readLogsSince = (int) (System.currentTimeMillis() / 1000L);
                startContainer();
                startMonitoringAndLogsReading(readLogsSince);
            }

        }catch (DockerRequestException e){
            logger.error("Exception: {}", e);
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
        try {
            if(useCachedContainer!=null && useCachedContainer) {
                if(!onstart)
                   stopContainer();
            }else if(onstart)
                removeAllContainersWithSameImage();
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

    public int getInstanceId(){
        return instanceId;
    }

    public void setInstanceId(int value){
        instanceId = value;
    }

    public String getImageName(){
        return imageName;
    }
    public String getContainerName(){
        return containerName;
    }

    public String getHostName(){ return hostName;}

    public void removeAllContainersWithSameImage(){
        logger.debug("Removing containers (imageName={})", imageName);
        try {
            removeAllContainersWithSameImage(imageName);
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

    public void addEnvironmentVariable(String keyValue){
        String[] splitted = keyValue.split("=");
        for(String kv : environmentVariables.toArray(new String[0]))
            if (kv.startsWith(splitted[0]))
                environmentVariables.remove(kv);
        this.environmentVariables.add(keyValue);
    }

    public void prepareImage() throws InterruptedException, DockerException, DockerCertificateException, IOException{
        prepareImage(imageName);
    };

    public abstract void prepareImage(String imageName) throws InterruptedException, DockerException, DockerCertificateException, IOException;

    public String createContainerWithRemoveAllPrevs() throws DockerException, InterruptedException, DockerCertificateException, IOException {
        removeAllSameNamesContainers();
        containerId = createContainer();

        return containerId;
    }

    public String createContainerIfNotExists() throws DockerException, InterruptedException, DockerCertificateException, IOException {
        if(containerId!=null)
            return containerId;

        List<Container> results = findContainersByName(containerName, DockerClient.ListContainersParam.allContainers());
        for(Container container : results)
            if(container.state().equals("running") && useCachedContainer!=null)
                containerId = container.id();
            else
                getDockerClient().removeContainer(container.id(), DockerClient.RemoveContainerParam.forceKill());

        if(containerId==null)
            containerId = createContainer();

        return containerId;
    }

    public void startContainer() throws Exception {
        logger.debug("Starting container (imageName={}, containerId={})", imageName, containerId);
        getDockerClient().restartContainer(containerId);
        connectContainerToNetworks(networks);
        //logger.debug("Waiting till container will start (imageName={})", imageName);
        //awaitRunning(dockerClient, containerId);
        logger.debug("Container started (imageName={}, containerId={})", imageName, containerId);
    }

    public void startMonitoringAndLogsReading(int since) throws Exception {
        logger.debug("Starting monitoring & logs reading for container (imageName={})", imageName);

        ExecutorService threadPool = Executors.newCachedThreadPool();

        Callable<String> callable = () -> {
            //String ret="";
            LogStream logStream = null;

            String logs="";
            String prevLogs="";
            Boolean running = true;
            String containerId=null;

            while(running){
                if (skipLogsReading==null || !skipLogsReading){
                    if(containerId==null) {
                        List<Container> results = findContainersByName(containerName, DockerClient.ListContainersParam.allContainers());
                        for(Container container : results)
                            if(container.state().equals("running"))
                                containerId = container.id();
                    }
                    if(containerId!=null){
                        try {
                            logStream = getDockerClient().logs(containerId,
                                    DockerClient.LogsParam.stderr(),
                                    DockerClient.LogsParam.stdout(),
                                    DockerClient.LogsParam.since(since)
                            );
                            logs = logStream.readFully();
                        } catch (Exception e) {
                            logger.warn("No logs are available (containerId={}, imageName={}): {}", containerId, imageName, e.getLocalizedMessage().replace("\n",""));
                            containerId = null;
                        } finally {
                            if (logStream != null) {
                                logStream.close();
                            }
                        }

                        if(containerId==null)
                            continue;

                        if (logs.length() > prevLogs.length()) {
                            String logsToPrint = logs.substring(prevLogs.length());
                            if (logsToPrint.contains(" ERROR "))
                                logger.error(logsToPrint);
                            else
                                logger.debug(logsToPrint);
                            prevLogs = logs;
                        }
                        running = getDockerClient().inspectContainer(containerId).state().running();
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
                List<Container> results = findContainersByName(containerName, DockerClient.ListContainersParam.withStatusRunning());
                if(results.size()>0)
                    containerId = results.get(0).id();
            }
            if(containerId!=null)
                getDockerClient().stopContainer(containerId, 0);
        }
        catch (Exception e){
            logger.error("Exception", e);
            exception = e;
        }
    }


    public String createContainer() throws DockerException, InterruptedException, DockerCertificateException, IOException {

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
                .env(getEnvironmentVariables().toArray(new String[0]));

        ContainerConfig containerConfig = builder .build();
        ContainerCreation creation = getDockerClient().createContainer(containerConfig, containerName);
        String contId = creation.id();
        if (contId == null) {
            IllegalStateException exception = new IllegalStateException(format("Unable to create container %s", containerName));
            logger.error(String.format("Failed to create container (imageName=%s): ",imageName), exception);
            throw exception;
        }

        return contId;
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

    private Collection<String> getEnvironmentVariables() {
        return environmentVariables;
        //return environmentVariables.toArray(new String[environmentVariables.size()]);
    }

    private List<Container> findContainersByName(String containerName, DockerClient.ListContainersParam param) throws
            DockerException, InterruptedException, DockerCertificateException {
        List<Container> ret = new ArrayList<>();
        for(Container container : getDockerClient().listContainers(param)) {
            for(String name : container.names()){
                if (name.equals(dockerizeContainerName(containerName)))
                    ret.add(container);
            }
        }

        return ret;
    }

    private List<Container> findContainersByImageName(String imageName, DockerClient.ListContainersParam param) throws
            DockerException, InterruptedException, DockerCertificateException {
        List<Container> ret = new ArrayList<>();
        for(Container container : getDockerClient().listContainers(param)) {
            if (container.image().equals(imageName))
                ret.add(container);
        }

        return ret;
    }



    private void removeAllContainersWithSameImage(String imageName) throws
            DockerException, InterruptedException, DockerCertificateException {
        //for(Container container : findContainersByName(containerName, DockerClient.ListContainersParam.allContainers())) {
        for(Container container : findContainersByImageName(imageName, DockerClient.ListContainersParam.allContainers())) {

            boolean removed = false;
            while (!removed) {
                try {
                    getDockerClient().removeContainer(container.id(), DockerClient.RemoveContainerParam.forceKill());
                    removed = true;
                    containerId = null;
                } catch (Exception e) {
                    //if(!e.getMessage().contains("not found"))
                    //    logger.error("Exception", e);
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

    private void removeAllSameNamesContainers() throws InterruptedException, DockerException, DockerCertificateException {
        logger.debug("Removing all containers with the same name ({})", containerName);
        for(Container container : findContainersByName(containerName, DockerClient.ListContainersParam.allContainers())) {

            boolean removed = false;
            while (!removed) {
                try {
                    getDockerClient().removeContainer(container.id(), DockerClient.RemoveContainerParam.forceKill());
                    removed = true;
                    containerId = null;
                } catch (Exception e) {
                    //if(!e.getMessage().contains("not found"))
                    //    logger.error("Exception", e);
                }
            }
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

    public Map<String,List<PortBinding>> getPortBindings() {
        return portBindings;
    }

    public Collection<String> getNetworks() {
        return networks;
    }

    public Boolean getSkipLogsReading() {
        return skipLogsReading;
    }

    public Boolean getUseCachedContainer() {
        return useCachedContainer;
    }

    public Callable getOnTermination() {
        return onTermination;
    }

    //@Override
    public AbstractDockerizer clone(Collection<String> newEnvironmentVariables){
        AbstractDockerizer ret = null;
        AbstractDockersBuilder builder = null;

        if (BuildBasedDockerizer.class.isInstance(this))
            builder = new BuildBasedDockersBuilder(getName())
                    .dockerFileReader(((BuildBasedDockerizer)this).getDockerFileReader())
                    .buildDirectory(((BuildBasedDockerizer)this).getBuildDirectory().toString())
                    .useCachedImage(((BuildBasedDockerizer)this).getUseCachedImage());

        else
            builder = new PullBasedDockersBuilder(getImageName());

        try {
            builder.name(getName()+"_"+instanceId)
                    .imageName(imageName)
                    .hostName(hostName)
                    .containerName(containerName+"_"+instanceId)
                    .portBindings(portBindings)
                    .environmentVariables(newEnvironmentVariables)
                    .networks(networks)
                    .skipLogsReading(skipLogsReading)
                    .useCachedContainer(useCachedContainer)
                    .onTermination(onTermination);
            ret = builder.build();
        } catch (Exception e) {
            e.printStackTrace();
        }
        instanceId++;
        return ret;
    }

    public String getContainerId() {
        return containerId;
    }
}