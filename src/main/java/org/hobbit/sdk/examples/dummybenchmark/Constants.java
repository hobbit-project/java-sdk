package org.hobbit.sdk.examples.dummybenchmark;

public class Constants {
    //public static final String GIT_REPO_PATH = "git.project-hobbit.eu:4567/smirnp/";
    public static final String GIT_REPO_PATH = "";
    public static final String PROJECT_NAME = "dummybenchmark/";

    public static final String DUMMY_BENCHMARK_IMAGE_NAME = GIT_REPO_PATH+PROJECT_NAME +"benchmark-controller";
    public static final String DUMMY_SYSTEM_IMAGE_NAME = GIT_REPO_PATH+PROJECT_NAME +"system-adapter";
    public static final String DUMMY_SYSTEM_SLAVE_IMAGE_NAME = GIT_REPO_PATH+PROJECT_NAME +"system-slave";

    //use these constants within BenchmarkController
    public static final String DUMMY_DATAGEN_IMAGE_NAME = GIT_REPO_PATH+PROJECT_NAME +"datagen";
    public static final String DUMMY_TASKGEN_IMAGE_NAME = GIT_REPO_PATH+PROJECT_NAME +"taskgen";
    public static final String DUMMY_EVAL_STORAGE_IMAGE_NAME = GIT_REPO_PATH+PROJECT_NAME +"eval-storage";
    public static final String DUMMY_EVALMODULE_IMAGE_NAME = GIT_REPO_PATH+PROJECT_NAME +"eval-module";

    public static final String BENCHMARK_URI = "http://project-hobbit.eu/"+PROJECT_NAME;
    public static final String SYSTEM_URI = "http://project-hobbit.eu/"+PROJECT_NAME+"system";
    public static final String SYSTEM_CONTAINERS_COUNT_KEY = SYSTEM_URI+"/systemContainersCount";
}
