package org.learning.by.example.failures.testingresilience;

import org.junit.jupiter.api.Test;
import org.learning.by.example.failures.testingresilience.test.BaseK8sTest;

public class LocalK8sTest extends BaseK8sTest {
    public LocalK8sTest() throws K8sTestException {
        super();
    }

    @Test
    public void oneTest() throws Exception {
        if(podsExists()){
            deletePods();
        }
        createDatabase();
    }
}
