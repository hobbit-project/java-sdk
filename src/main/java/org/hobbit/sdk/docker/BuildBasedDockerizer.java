package org.hobbit.sdk.docker;

import org.hobbit.sdk.docker.builders.BuildBasedDockersBuilder;
import com.spotify.docker.client.exceptions.DockerCertificateException;
import com.spotify.docker.client.exceptions.DockerException;

import java.io.*;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import static java.lang.String.format;

/**
 * @author Pavel Smirnov
 */

public class BuildBasedDockerizer extends AbstractDockerizer {

    private static final Charset charset = Charset.forName("UTF-8");


    private final String imageName;
    private final String containerName;
    private final Boolean useCachedImage;
    private Path buildDirectory;
    private Reader dockerFileReader;

    private String imageId;
    private Path jarFilePath;
    //private String dockerfilePath;
    //private String tempDockerFileName;



    public BuildBasedDockerizer(BuildBasedDockersBuilder builder) {
        super(builder);
        imageName = builder.getImageName();
        containerName = builder.getContainerName();
        buildDirectory = builder.getBuildDirectory();
        dockerFileReader = builder.getDockerFileReader();
        useCachedImage = builder.getUseCachedImage();
        jarFilePath = builder.getJarFilePath();

    }

    @Override
    public void prepareImage(String imageName) throws Exception {
        logger.debug("Building image (imageName={})", imageName);

        if(jarFilePath!=null && !jarFilePath.toFile().exists())
            throw new Exception(jarFilePath +" not found. Package it by 'make package or mvn package -DskipTests=true'");

        if(dockerFileReader==null)
            throw new Exception("dockerFile reader is not specified for "+this.getClass().getSimpleName()+".");

        Path filePath = createTempDockerFile();
        fillDockerFile(filePath);

        try {
            imageId = getDockerClient().build(buildDirectory, imageName, filePath.getFileName().toString(), message -> {

            });
            Files.delete(filePath);
        }
        catch (Exception e){
            InterruptedException e2 = new InterruptedException("Failed to build image " + imageName +" from "+ filePath+": "+e.getLocalizedMessage());
            logger.error(e2.getLocalizedMessage());
            e.printStackTrace();
            throw e2;        }


    }

    @Override
    public void stop(Boolean onstart) throws InterruptedException, DockerException, DockerCertificateException {
        if(this.useCachedImage==null || this.useCachedImage==false)
            useCachedContainer=false;

        super.stop(onstart);

        if(onstart) {
            if (this.useCachedImage == null || this.useCachedImage == false) {
                removeAllSameNamedImages();
            }
        }
    }

//    private void buildImage(String imageName) throws
//            InterruptedException, DockerException, IOException, IllegalStateException, DockerCertificateException {
//
//
//    }

    private Path createTempDockerFile() throws IOException {
        File file = File.createTempFile("Dockerfile", ".tmp", buildDirectory.toFile());
        return Paths.get(file.getPath());
    }

    private void fillDockerFile(Path filePath) throws IOException {
        try (OutputStream outputStream = Files.newOutputStream(filePath, StandardOpenOption.WRITE)) {
            outputStream.write(readAllFromDockerFileReader());
        }
    }

    private byte[] readAllFromDockerFileReader() throws IOException {
        try (BufferedReader reader = new BufferedReader(dockerFileReader)) {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            String line;
            while ((line = reader.readLine()) != null) {
                buffer.write(line.getBytes(charset));
                buffer.write(format("%n").getBytes(charset));
            }
            return buffer.toByteArray();
        }
    }


    private void removeTempDockerFile(Path path) throws IOException {
        Files.deleteIfExists(path);
    }

    public Reader getDockerFileReader() {
        return dockerFileReader;
    }

    public Path getBuildDirectory() {
        return buildDirectory;
    }

    public Boolean getUseCachedImage() {
        return useCachedImage;
    }

//    public void pushImage() throws DockerCertificateException, DockerException, InterruptedException {
//        getDockerClient().push(imageName, RegistryAuth.builder().serverAddress("git.project-hobbit.eu:4567").identityToken("").benchmarkSignalsReaction());
//    }

}
