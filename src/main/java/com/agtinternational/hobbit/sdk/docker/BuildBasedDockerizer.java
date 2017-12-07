package com.agtinternational.hobbit.sdk.docker;

import com.agtinternational.hobbit.sdk.docker.builders.common.BuildBasedDockersBuilder;
import com.spotify.docker.client.exceptions.DockerCertificateException;
import com.spotify.docker.client.exceptions.DockerException;

import java.io.*;
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
    private final Boolean useCachedImage;

    private final String buildDirectory;
    private final Reader dockerFileReader;

    private String imageId;
    private String tempDockerFileName;
    private String imageName;


    public BuildBasedDockerizer(BuildBasedDockersBuilder builder) {
        super(builder);
        imageName = builder.getImageName();
        buildDirectory = builder.getBuildDirectory();
        dockerFileReader = builder.getDockerFileReader();
        useCachedImage = builder.getUseCachedImage();
    }

    @Override
    public void prepareImage(String imageName) throws InterruptedException, DockerException, DockerCertificateException, IOException {
        buildImage(imageName);
    }

    @Override
    public void stop() throws InterruptedException, DockerException, DockerCertificateException {
        if(this.useCachedImage==null || this.useCachedImage==false)
            useCachedContainer=false;

        super.stop();
        if(this.useCachedImage==null || this.useCachedImage==false) {
            removeAllSameNamedImages();

        }
    }

    private void buildImage(String imageName) throws
            InterruptedException, DockerException, IOException, IllegalStateException {

        logger.debug("Building image (imageName={})", imageName);
        createTempDockerFile();
        fillDockerFile();
        Path path = Paths.get(buildDirectory);
        imageId = dockerClient.build(path, imageName, tempDockerFileName, message -> {

        });
        if (imageId == null) {
            IllegalStateException exception = new IllegalStateException(format("Unable to create image %s", imageName));
            logger.error("Exception", exception);
            throw exception;
        }
        removeTempDockerFile();
    }

    private void createTempDockerFile() throws IOException {
        File file = File.createTempFile("dockerFile", "temp", new File(buildDirectory));
        tempDockerFileName = file.getName();
    }

    private void fillDockerFile() throws IOException {
        Path path = Paths.get(buildDirectory, tempDockerFileName);
        try (OutputStream outputStream = Files.newOutputStream(path, StandardOpenOption.WRITE)) {
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


    private void removeTempDockerFile() throws IOException {
        Path path = Paths.get(buildDirectory, tempDockerFileName);
        Files.deleteIfExists(path);
    }

//    public void pushImage() throws DockerCertificateException, DockerException, InterruptedException {
//        getDockerClient().push(imageName, RegistryAuth.builder().serverAddress("git.project-hobbit.eu:4567").identityToken("").build());
//    }

}
