package org.hobbit.sdk.docker.builders;

import org.hobbit.sdk.docker.BuildBasedDockerizer;

import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Pavel Smirnov
 */

public class DynamicDockerFileBuilder extends BuildBasedDockersBuilder {

    private Class[] runnerClass;
    private Path dockerWorkDir;
    private Path jarFilePath;
    private List<String> filesToAdd;

    public DynamicDockerFileBuilder(String dockerizerName){
        super(dockerizerName);
        filesToAdd = new ArrayList<>();
   }

    public DynamicDockerFileBuilder runnerClass(Class... values) {
        this.runnerClass = values;
        return this;
    }

    public DynamicDockerFileBuilder dockerWorkDir(String value) {
        this.dockerWorkDir = Paths.get(value);
        return this;
    }

    public DynamicDockerFileBuilder jarFilePath(String value) {
        this.jarFilePath = Paths.get(value).toAbsolutePath();
        return this;
    }

    public DynamicDockerFileBuilder useCachedImage(Boolean value) {
        super.useCachedImage(value);
        return this;
    }

    public DynamicDockerFileBuilder useCachedContainer(Boolean value) {
        super.useCachedContainer(value);
        return this;
    }

    public DynamicDockerFileBuilder customDockerFileReader(Reader value) {
        super.dockerFileReader(value);
        return this;
    }

    public DynamicDockerFileBuilder imageName(String value) {
        super.imageName(value);
        return this;
    }

    public DynamicDockerFileBuilder addFileOrFolder(String path) {
        filesToAdd.add(path);
        return this;
    }

    private DynamicDockerFileBuilder initFileReader() throws Exception {
        if(runnerClass==null)
            throw new Exception("Runner class is not specified for "+this.getClass().getSimpleName());

        if(dockerWorkDir ==null)
            throw new Exception("WorkingDirName class is not specified for "+this.getClass().getSimpleName());

        if(jarFilePath ==null)
            throw new Exception("JarFileName class is not specified for "+this.getClass().getSimpleName());

        if(!jarFilePath.toFile().exists())
            throw new Exception(jarFilePath +" not found. May be you did not packaged it by 'mvn package -DskipTests=true' first");

        //List<String> classNames = Arrays.stream(runnerClass).map(c->"\""+c.getCanonicalName()+"\"").collect(Collectors.toList());
        List<String> classNames = Arrays.stream(runnerClass).map(c->c.getCanonicalName()).collect(Collectors.toList());
        String datasetsStr = "";

        for(String dataSetPathStr : filesToAdd){

            Path destPathRel = Paths.get(dataSetPathStr);
            if(destPathRel.isAbsolute())
                destPathRel = getBuildDirectory().relativize(destPathRel);

            Path parent = destPathRel.getParent();
            List<String> dirsToCreate = new ArrayList<>();
            if(Files.isDirectory(destPathRel))
                dirsToCreate.add(destPathRel.toString());
            while(parent!=null){
                dirsToCreate.add(parent.toString());
                parent=parent.getParent();
            }

            for(int i=dirsToCreate.size()-1; i>=0; i--){
                datasetsStr += "RUN mkdir -p "+dockerWorkDir.resolve(dirsToCreate.get(i))+"\n";
            }

            Path sourcePath = destPathRel;
            String destPath = dockerWorkDir.resolve(destPathRel).toString();

            if(sourcePath.toFile().isDirectory()){
                sourcePath = sourcePath.resolve("*");
                destPath+="/";
            }else {
                if(sourcePath.getParent()!=null)
                    destPath = dockerWorkDir.resolve(sourcePath.getParent()).toString();
                else
                    destPath = dockerWorkDir.toString();
            }
            datasetsStr += "ADD ./" + sourcePath + " " + destPath + "\n";
        }

        Path jarPathRel = jarFilePath;
        if(jarPathRel.isAbsolute())
            jarPathRel = getBuildDirectory().relativize(jarFilePath);

        String content =
                "FROM java\n" +
                        "RUN mkdir -p "+ dockerWorkDir +"\n" +
                        "WORKDIR "+ dockerWorkDir +"\n" +
                         datasetsStr+
                        "ADD "+ jarPathRel +" "+ dockerWorkDir +"\n" +
                        "CMD java -cp "+ jarPathRel.getFileName() +" "+ String.join(" ", classNames) +"\n"
                        //"CMD [\"java\", \"-cp\", \""+ jarPathRel.getFileName() +"\", "+ String.join(",", classNames) +"]\n"
                        //"CMD [\"java\", \"-cp\", \""+ jarFilePath +"\", \""+runnerClass.getCanonicalName()+"\"]\n"
                ;
        dockerFileReader(new StringReader(content));
        return this;
    }

    @Override
    public BuildBasedDockerizer build() throws Exception {
        if(getDockerFileReader()==null)
            initFileReader();
        return super.build();
    }
}
