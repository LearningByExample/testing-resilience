package org.learning.by.example.failures.testingresilience;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.learning.by.example.failures.testingresilience.test.BaseK8sTest;

import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;

public class LocalK8sTestIT extends BaseK8sTest {
    public LocalK8sTestIT() throws K8sTestException {
        super();
    }

    @Test
    @DisplayName("We should test in K8s")
    public void weShouldTestInK8s() throws Exception {
        if (podsExists()) {
            deletePods();
            await().atMost(5, MINUTES).and().with().pollInterval(3, SECONDS).until(() -> !podsExists());
        }
        createDatabase();
    }
}
