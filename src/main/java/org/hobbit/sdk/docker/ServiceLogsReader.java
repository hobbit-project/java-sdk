package org.hobbit.sdk.docker;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.LogStream;
import com.spotify.docker.client.exceptions.DockerCertificateException;
import com.spotify.docker.client.exceptions.ServiceNotFoundException;
import org.hobbit.core.components.Component;
import com.spotify.docker.client.messages.swarm.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class ServiceLogsReader implements Component {

    private Logger logger;

    private DockerClient dockerClient;
    //private String serviceId;
    private String name;
    private String imageName;
    int readLogsSince;


    public ServiceLogsReader(String imageName){
        this.imageName = imageName;
        String[] splitted = imageName.split("/");
        this.name = splitted[splitted.length-1];
        logger = LoggerFactory.getLogger(this.name);
    }


    @Override
    public void init() throws Exception {
        readLogsSince = (int) (System.currentTimeMillis() / 1000L);
    }



    public DockerClient getDockerClient() throws DockerCertificateException {
        if(dockerClient==null)
            dockerClient = DefaultDockerClient.fromEnv().build();
        return dockerClient;
    }

    public String getName(){
        return name;
    }

    @Override
    public void run(){

        ExecutorService threadPool = Executors.newCachedThreadPool();

        Callable<String> callable = () -> {
            //String ret="";
            LogStream logStream = null;

            String logs = "";
            Map<String, String> prevLogs = new HashMap<>();
            Boolean finished = false;
            try {
                while (!finished) {


                        List<String> serviceIds = getDockerClient().listServices().stream().filter(s ->
                                        s.spec().taskTemplate().containerSpec().image().equals(imageName)
                                                && s.createdAt().getTime()>readLogsSince
                                //&& (s.spec().mode().replicated()!=null && s.spec().mode().replicated().replicas().intValue() > 0)
                        ).map(s -> s.id()).collect(Collectors.toList());


                        for(String serviceId: serviceIds) {
                            String loggerName = this.name+"-"+serviceId;
                                logs="";
                                try {
                                    logStream = getDockerClient().serviceLogs(serviceId,
                                            DockerClient.LogsParam.stderr(),
                                            DockerClient.LogsParam.stdout(),
                                            DockerClient.LogsParam.since(readLogsSince)
                                    );
                                    logs = logStream.readFully();
                                } catch (ServiceNotFoundException e) {
                                    logger.warn("Service not found {}", loggerName, e);
                                } catch (Exception e) {
                                    logger.warn("No service logs are available {}", loggerName, e);
                                } finally {
                                    if (logStream != null) {
                                        logStream.close();
                                    }
                                }

                                int prevLogsLength = (prevLogs.containsKey(serviceId)?prevLogs.get(serviceId).length():0);
                                if (logs.length() > prevLogsLength) {
                                    logger = LoggerFactory.getLogger(loggerName);
                                    String logsToPrint = logs.substring(prevLogsLength);
                                    if (logsToPrint.contains(" ERROR "))
                                        logger.error(logsToPrint);
                                    else
                                        logger.debug(logsToPrint);
                                    prevLogs.put(serviceId, logs);
                                }
                                //finished = getDockerClient().inspectService(serviceId).spec().mode().replicated().replicas().intValue() == 0;

                        }
                    Thread.sleep(1000);
                }
            }
            catch (Exception e){
                logger.error(e.getMessage());
                e.printStackTrace();
            }
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

    }

    @Override
    public void close() throws IOException {

    }
}
