package org.hobbit.sdk.utils.commandreactions;

import org.hobbit.core.components.Component;
import org.hobbit.sdk.utils.CommandQueueListener;
import org.hobbit.sdk.utils.ComponentsExecutor;

import java.util.HashMap;
import java.util.Map;

public class CommandReactionsBuilder{

    protected ComponentsExecutor componentsExecutor;
    protected CommandQueueListener commandQueueListener;

    protected Component benchmarkController;
    protected Component dataGenerator;
    protected Component taskGenerator;
    protected Component evalStorage;
    protected Component evalModule;
    protected Component systemAdapter;

    protected String benchmarkControllerImageName;
    protected String dataGeneratorImageName;
    protected String taskGeneratorImageName;
    protected String evalStorageImageName;
    protected String evalModuleImageName;
    protected String systemAdapterImageName;

    protected Map<String, Component> customContainers = new HashMap<>();

//    private String dataGenContainerId;
//    private String taskGenContainerId;
    //private String systemAdapterImageName;
//    private String evalModuleContainerId;
//    private String evalStorageContainerId;

    public CommandReactionsBuilder(ComponentsExecutor componentsExecutor, CommandQueueListener commandQueueListener){
        this.componentsExecutor = componentsExecutor;
        this.commandQueueListener = commandQueueListener;
    }

    public CommandReactionsBuilder benchmarkController(Component component){
        this.benchmarkController = component;
        return this;
    }

    public CommandReactionsBuilder benchmarkControllerImageName(String value){
        this.benchmarkControllerImageName = value;
        return this;
    }

    public CommandReactionsBuilder dataGenerator(Component component){
        this.dataGenerator = component;
        return this;
    }

    public CommandReactionsBuilder dataGeneratorImageName(String value){
        this.dataGeneratorImageName = value;
        return this;
    }

    public CommandReactionsBuilder taskGenerator(Component component){
        this.taskGenerator = component;
        return this;
    }

    public CommandReactionsBuilder taskGeneratorImageName(String value){
        this.taskGeneratorImageName = value;
        return this;
    }

    public CommandReactionsBuilder evalStorage(Component component){
        this.evalStorage = component;
        return this;
    }

    public CommandReactionsBuilder evalStorageImageName(String value){
        this.evalStorageImageName = value;
        return this;
    }

    public CommandReactionsBuilder systemAdapter(Component value){
        this.systemAdapter = value;
        return this;
    }

    public CommandReactionsBuilder systemAdapterImageName(String value){
        this.systemAdapterImageName = value;
        return this;
    }

    public CommandReactionsBuilder customContainerImage(Component component, String imageName){
        String[] splitted = imageName.split("/");
        String cleanedImageName=splitted[splitted.length-1].split(":")[0];

        customContainers.put(cleanedImageName, component);
        return this;
    }


    public CommandReactionsBuilder evalModule(Component value){
        this.evalModule = value;
        return this;
    }

    public CommandReactionsBuilder evalModuleImageName(String value){
        this.evalModuleImageName = value;
        return this;
    }

    public ContainerCommandsReaction containerCommandsReaction(){
        return new ContainerCommandsReaction(this);
    }

    public BenchmarkSignalsReaction benchmarkSignalsReaction(){
        return new BenchmarkSignalsReaction(this);
    }

    public ServiceLogsReaderReaction serviceLogsReaderReaction(){
        return  new ServiceLogsReaderReaction(this);
    }

    public ServiceLogsReaderReaction serviceLogsReaderReaction(String[] imagesToListen){
        return  new ServiceLogsReaderReaction(this, imagesToListen);
    }


}