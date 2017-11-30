package com.agtinternational.hobbit.sdk.docker;

import com.spotify.docker.client.DockerClient;
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
    private String tempDockerFileName;
    private String buildDirectory;
    private Reader dockerFileReader;
    private String imageId;

    protected BuildBasedDockerizer(Builder builder) {
        super(builder);

        buildDirectory = builder.buildDirectory;
        dockerFileReader = builder.dockerFileReader;
    }

    @Override
    public void prepareImage(String imageName) throws InterruptedException, DockerException, DockerCertificateException, IOException {
        buildImage(imageName);
    }

    private void buildImage(String imageName) throws
            InterruptedException, DockerException, IOException, IllegalStateException {

        logger.debug("Building image (imageName={})", imageName);
        createTempDockerFile();
        fillDockerFile();
        Path path = Paths.get(buildDirectory);
        imageId = dockerClient.build(path, imageName, tempDockerFileName, message -> {

        });
        removeTempDockerFile();
        if (imageId == null) {
            IllegalStateException exception = new IllegalStateException(format("Unable to create image %s", imageName));
            logger.error("Exception", exception);
            throw exception;
        }
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

    public static class Builder extends AbstractDockerizer.AbstractDockerizerBuilder {

        private String buildDirectory;
        private Reader dockerFileReader;

        public Builder(String dockerizerName){
            super(dockerizerName);
       }

        public Builder dockerFileReader(Reader value) {
            this.dockerFileReader = value;
            return this;
        }

        public Builder buildDirectory(String value) {
            this.buildDirectory = value;
            return this;
        }

        @Override
        public BuildBasedDockerizer build() throws Exception {
            BuildBasedDockerizer ret = new BuildBasedDockerizer(this);
            return ret;
        }
    }

}
