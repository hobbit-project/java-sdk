package org.hobbit.sdk.utils.commandreactions;

import com.google.gson.Gson;
import org.hobbit.core.Commands;
import org.hobbit.core.components.Component;
import org.hobbit.sdk.utils.ComponentsExecutor;
import org.hobbit.sdk.utils.CommandQueueListener;
import org.hobbit.sdk.utils.CommandSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;


public class PlatformCommandsReaction implements CommandReaction {
    private static final Logger logger = LoggerFactory.getLogger(PlatformCommandsReaction.class);

    private ComponentsExecutor componentsExecutor;
    private CommandQueueListener commandQueueListener;

    private Component benchmarkController;
    private Component dataGenerator;
    private Component taskGenerator;
    private Component evalStorage;
    private Component evalModule;
    private Component systemAdapter;

    private String benchmarkControllerImageName;
    private String dataGeneratorImageName;
    private String taskGeneratorImageName;
    private String evalStorageImageName;
    private String evalModuleImageName;
    private String systemAdapterImageName;

    private int dataGeneratorsCount = 0;
    private int taskGeneratorsCount = 0;
    private int systemContainersCount = 0;

    private Gson gson = new Gson();

    private boolean benchmarkReady = false;
    private boolean dataGenReady = false;
    private boolean taskGenReady = false;
    private boolean evalStorageReady = false;
    private boolean systemReady = false;

    private boolean startBenchmarkCommandSent = false;
    private Map<String, Component> customContainers = new HashMap<>();
    private Map<String, Integer> customContainersRunning = new HashMap<>();
    //private String systemContainerId = null;

    public PlatformCommandsReaction(CommandReactionsBuilder builder){
        this.componentsExecutor = builder.componentsExecutor;
        this.commandQueueListener = builder.commandQueueListener;

        this.benchmarkController=builder.benchmarkController;
        this.dataGenerator=builder.dataGenerator;
        this.taskGenerator=builder.taskGenerator;
        this.evalStorage=builder.evalStorage;
        this.evalModule=builder.evalModule;
        this.systemAdapter=builder.systemAdapter;

        this.benchmarkControllerImageName = builder.benchmarkControllerImageName;
        this.dataGeneratorImageName = builder.dataGeneratorImageName;
        this.taskGeneratorImageName = builder.taskGeneratorImageName;
        this.evalStorageImageName = builder.evalStorageImageName ;
        this.evalModuleImageName = builder.evalModuleImageName;
        this.systemAdapterImageName = builder.systemAdapterImageName;
        this.customContainers = builder.customContainers;
    }

    @Override
    public void handleCmd(Byte command, byte[] bytes, String replyTo) throws Exception {


        if (command == Commands.BENCHMARK_FINISHED_SIGNAL){
            logger.debug("BENCHMARK_FINISHED_SIGNAL received");
            try {
                commandQueueListener.terminate();
                componentsExecutor.shutdown();
            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
                //Assert.fail(e.getMessage());
            }
        }

        if (command == Commands.BENCHMARK_READY_SIGNAL) {
            benchmarkReady = true;
            logger.debug("BENCHMARK_READY_SIGNAL signal received");
        }

        if (command == Commands.DATA_GENERATOR_READY_SIGNAL) {
            dataGenReady = true;
            logger.debug("DATA_GENERATOR_READY_SIGNAL signal received");
        }

        if (command == Commands.TASK_GENERATOR_READY_SIGNAL) {
            taskGenReady = true;
            logger.debug("TASK_GENERATOR_READY_SIGNAL signal received");
        }

        if (command == Commands.EVAL_STORAGE_READY_SIGNAL) {
            evalStorageReady = true;
            logger.debug("EVAL_STORAGE_READY_SIGNAL signal received");
        }

        if (command == Commands.SYSTEM_READY_SIGNAL) {
            systemReady = true;
            logger.debug("SYSTEM_READY_SIGNAL signal received");
        }

        synchronized (this){
            if (benchmarkReady &&
                    (dataGenerator==null || dataGenReady) &&
                    (taskGenerator==null || taskGenReady) &&
                    (evalStorage==null || evalStorageReady) &&
                    systemReady && !startBenchmarkCommandSent) {
                startBenchmarkCommandSent = true;
                try {
                    logger.debug("sending START_BENCHMARK_SIGNAL");

                    new CommandSender(Commands.START_BENCHMARK_SIGNAL, systemAdapterImageName+"_0").send();
                } catch (Exception e) {
                    logger.error(e.getMessage());
                    //Assert.fail(e.getMessage());
                }
            }
        }

    }


}
