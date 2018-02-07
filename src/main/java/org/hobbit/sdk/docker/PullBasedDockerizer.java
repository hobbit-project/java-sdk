package org.hobbit.sdk.docker;

import org.hobbit.sdk.docker.builders.PullBasedDockersBuilder;
import com.spotify.docker.client.exceptions.DockerCertificateException;
import com.spotify.docker.client.exceptions.DockerException;

import java.io.IOException;

/**
 * @author Pavel Smirnov
 */

public class PullBasedDockerizer extends AbstractDockerizer {

    public PullBasedDockerizer(PullBasedDockersBuilder builder) {
        super(builder);
    }


    @Override
    public void prepareImage(String imageName) throws InterruptedException, DockerException, DockerCertificateException, IOException {
        pullImage(imageName);
    }

    public void pullImage(String imageName) throws InterruptedException, DockerException, IOException, DockerCertificateException {
        logger.debug("Pulling image (imageName={})", imageName);
        this.getDockerClient().pull(imageName);
    }


}