package org.learning.by.example.failures.testingresilience.test;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.AppsV1Api;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.*;
import io.kubernetes.client.util.ClientBuilder;
import io.kubernetes.client.util.KubeConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class BaseK8sTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseK8sTest.class);
    private static final String HOME_PROPERTY = "user.home";
    private static final String KUBE_CONFIG = "/.kube/config";


    private final CoreV1Api api;
    final AppsV1Api appsV1Api;

    public BaseK8sTest() throws K8sTestException {
        this.api = getK8sApi();
        this.appsV1Api = new AppsV1Api();
        this.appsV1Api.setApiClient(api.getApiClient());
    }

    public static class K8sTestException extends Exception {
        K8sTestException(final String message, final Exception parent) {
            super(message, parent);
        }
    }

    /*
    private List<V1Pod> getPods() throws K8sTestException {
        LOGGER.info("getting pods");
        final String label = "app=" + APP_NAME;

        try {
            final V1PodList v1PodList = api.listNamespacedPod(DEFAULT_NAME_SPACE, null, false, null, null, label, null, null, null, false);
            return v1PodList.getItems();
        } catch (final ApiException ex) {
            throw new K8sTestException(ex.getResponseBody(), ex);
        }
    }

    public boolean podsExists() throws K8sTestException {
        LOGGER.info("checking if pod exist");
        final boolean result = getPods().size() > 0;
        LOGGER.info("pod exists: {}", result);
        return result;
    }

    public void deletePods() throws K8sTestException {
        LOGGER.info("deleting pods");
        try {
            for (final V1Pod pod : getPods()) {
                LOGGER.info("deleting pod: {}", Objects.requireNonNull(pod.getMetadata()).getName());

                final V1DeleteOptions v1DeleteOptions = new V1DeleteOptions();
                v1DeleteOptions.setApiVersion("v1");
                v1DeleteOptions.setOrphanDependents(true);
                api.deleteNamespacedPod(pod.getMetadata().getName(), DEFAULT_NAME_SPACE, null, null, null, true, null, null);
            }
        } catch (final ApiException ex) {
            throw new K8sTestException(ex.getResponseBody(), ex);
        } catch (final JsonSyntaxException ignore) {
            LOGGER.warn("ignoring JsonSyntaxException due bug on kubernetes Swagger api https://github.com/kubernetes-client/java/issues/252");
        }
    }*/

    public void createDeployment(final String namespace, final String appName, final String deploymentName, final String image, int exposedPort, int replicas) throws K8sTestException {
        // deployment
        final V1Deployment deployment = new V1Deployment();

        // deployment meta-data
        final V1ObjectMeta metadata = new V1ObjectMeta();
        metadata.setName(deploymentName);
        final HashMap<String, String> labels = new HashMap<>();
        labels.put("app", appName);
        metadata.setLabels(labels);
        deployment.setMetadata(metadata);

        // deployment spec
        final V1DeploymentSpec spec = new V1DeploymentSpec();
        final V1LabelSelector labelSelector = new V1LabelSelector();
        labelSelector.setMatchLabels(labels);
        spec.setSelector(labelSelector);

        // template spec
        final V1PodTemplateSpec podTemplateSpec = new V1PodTemplateSpec();
        podTemplateSpec.setMetadata(metadata);

        // pod spec
        final V1PodSpec podSpec = new V1PodSpec();
        final List<V1Container> containers = new ArrayList<>();

        // container
        final V1Container container = new V1Container();
        container.setName(deploymentName);
        container.setImage(image);

        //port
        V1ContainerPort port = new V1ContainerPort();
        port.containerPort(exposedPort);
        container.setPorts(Collections.singletonList(port));

        containers.add(container);
        podSpec.setContainers(containers);
        podTemplateSpec.setSpec(podSpec);

        spec.setTemplate(podTemplateSpec);
        deployment.setSpec(spec);

        // replicas
        spec.setReplicas(replicas);

        try {
            appsV1Api.createNamespacedDeployment(namespace, deployment, null, null, null);
        } catch (final ApiException ex) {
            throw new K8sTestException(ex.getResponseBody(), ex);
        }
    }

    /*
    public void createDatabase() throws K8sTestException {
        LOGGER.info("creating database");
        final V1Pod pod = new V1Pod();
        pod.setApiVersion("v1");
        pod.setKind("Pod");

        final V1ObjectMeta metadata = new V1ObjectMeta();
        metadata.setName(POSTGRES_POD);
        final HashMap<String, String> labels = new HashMap<>();
        labels.put("app", APP_NAME);
        metadata.setLabels(labels);
        pod.setMetadata(metadata);

        final V1PodSpec spec = new V1PodSpec();
        spec.setContainers(new ArrayList<>());
        V1Container container = new V1Container();
        container.setName(POSTGRES_IMG);
        container.setImage(POSTGRES_BASE_IMAGE);
        V1ContainerPort port = new V1ContainerPort();
        port.containerPort(5432);
        container.setPorts(Collections.singletonList(port));

        spec.getContainers().add(container);
        pod.setSpec(spec);
        try {
            api.createNamespacedPod(DEFAULT_NAME_SPACE, pod, null, null, null);
        } catch (final ApiException ex) {
            throw new K8sTestException(ex.getResponseBody(), ex);
        }
    }*/

    private CoreV1Api getK8sApi() throws K8sTestException {
        LOGGER.info("connecting to K8s cluster");
        // get user home
        final String home = System.getProperty(HOME_PROPERTY);

        // get kube config
        final Path configPath = Paths.get(home, KUBE_CONFIG);
        try (final FileReader configReader = new FileReader(configPath.toString())) {
            // loading the out-of-cluster config
            final ApiClient client = ClientBuilder.kubeconfig(KubeConfig.loadKubeConfig(configReader)).build();

            // set the global default api-client to the in-cluster one from above
            Configuration.setDefaultApiClient(client);
            client.setVerifyingSsl(false);

            // the CoreV1Api loads default api-client from global configuration
            return new CoreV1Api();
        } catch (final Exception ex) {
            throw new K8sTestException("error getting k8s configuration", ex);
        }
    }
}
