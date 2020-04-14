package org.learning.by.example.failures.testingresilience;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.learning.by.example.failures.testingresilience.test.BaseK8sTest;

import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;

public class LocalK8sTestIT extends BaseK8sTest {

    private static final String DEFAULT_NAME_SPACE = "default";
    private static final String POSTGRES_DEPLOYMENT_NAME = "test-postgre-deploy";
    private static final String APP_NAME = "testing-resilience";
    private static final String POSTGRES_IMAGE = "postgres:9.6.12";
    private static final int POSTGRES_PORT = 5432;

    public LocalK8sTestIT() throws K8sTestException {
        super();
    }

    @Test
    void docker() {
        printDockerVersion();
    }

    @Test
    @DisplayName("We should test in K8s")
    public void weShouldTestInK8s() throws Exception {
        if (checkIfDeploymentsExits(DEFAULT_NAME_SPACE, APP_NAME)) {
            deleteDeployments(DEFAULT_NAME_SPACE, APP_NAME);
            await()
                .atMost(5, MINUTES)
                .and().with().pollInterval(3, SECONDS)
                .until(() -> !checkIfDeploymentsExits(DEFAULT_NAME_SPACE, APP_NAME));
        }
        createDeployment(DEFAULT_NAME_SPACE, APP_NAME, POSTGRES_DEPLOYMENT_NAME, POSTGRES_IMAGE, POSTGRES_PORT);
    }
}
