package org.hobbit.sdk.docker;

import org.hobbit.sdk.docker.builders.BuildBasedDockersBuilder;
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


    private final String imageName;
    private final String containerName;
    private final Path buildDirectory;
    private final Reader dockerFileReader;
    private final Boolean useCachedImage;


    private String imageId;
    //private String tempDockerFileName;



    public BuildBasedDockerizer(BuildBasedDockersBuilder builder) {
        super(builder);
        imageName = builder.getImageName();
        containerName = builder.getContainerName();
        buildDirectory = builder.getBuildDirectory();
        dockerFileReader = builder.getDockerFileReader();
        useCachedImage = builder.getUseCachedImage();

    }

    @Override
    public void prepareImage(String imageName) throws InterruptedException, DockerException, DockerCertificateException, IOException {
        buildImage(imageName);
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

    private void buildImage(String imageName) throws
            InterruptedException, DockerException, IOException, IllegalStateException, DockerCertificateException {

        logger.debug("Building image (imageName={})", imageName);
        Path filePath = createTempDockerFile();
        fillDockerFile(filePath);

        imageId = getDockerClient().build(buildDirectory, imageName, filePath.getFileName().toString(), message -> {

        });

        if (imageId == null) {
            IllegalStateException exception = new IllegalStateException(format("Unable to create image %s", imageName));
            logger.error("Exception", exception);
            throw exception;
        }
        removeTempDockerFile(filePath);
    }

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

//    public void pushImage() throws DockerCertificateException, DockerException, InterruptedException {
//        getDockerClient().push(imageName, RegistryAuth.builder().serverAddress("git.project-hobbit.eu:4567").identityToken("").build());
//    }

}
