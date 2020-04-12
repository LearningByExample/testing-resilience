package org.learning.by.example.failures.testingresilience;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.learning.by.example.failures.testingresilience.test.BaseK8sTest;

public class LocalK8sTestIT extends BaseK8sTest {

    private static final String DEFAULT_NAME_SPACE = "default";
    private static final String POSTGRES_DEPLOYMENT_NAME = "test-postgre-deploy";
    private static final String APP_NAME = "testing-resilience";
    private static final String POSTGRES_BASE_IMAGE = "postgres:9.6.12";
    private static final int POSTGRES_PORT = 5432;
    private static final int REPLICAS = 1;

    public LocalK8sTestIT() throws K8sTestException {
        super();
    }

    @Test
    @DisplayName("We should test in K8s")
    public void weShouldTestInK8s() throws Exception {
        /*if (podsExists()) {
            deletePods();
            await().atMost(5, MINUTES).and().with().pollInterval(3, SECONDS).until(() -> !podsExists());
        }
        createDatabase();*/
        createDeployment(DEFAULT_NAME_SPACE, APP_NAME, POSTGRES_DEPLOYMENT_NAME, POSTGRES_BASE_IMAGE, POSTGRES_PORT, REPLICAS);
    }
}
