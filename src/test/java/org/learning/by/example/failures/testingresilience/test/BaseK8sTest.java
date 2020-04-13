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
import java.util.*;

public class BaseK8sTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseK8sTest.class);
    private static final String HOME_PROPERTY = "user.home";
    private static final String KUBE_CONFIG = "/.kube/config";
    private static final String PROPAGATION_POLICY_BACKGROUND = "Background";
    private static final String IMAGE_PULL_POLICY_ALWAYS = "Always";


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

    private List<V1Deployment> getDeployments(final String namespace, final String app) throws K8sTestException {
        LOGGER.info("getting deployments for application: {} in namespace: {}", app, namespace);
        final String label = "app=" + app;
        try {
            final V1DeploymentList v1DeploymentList = appsV1Api.listNamespacedDeployment(namespace, null, false, null, null, label, null, null, null, null);
            return v1DeploymentList.getItems();
        } catch (final ApiException ex) {
            throw new K8sTestException(ex.getResponseBody(), ex);
        }
    }

    public boolean checkIfDeploymentsExits(final String namespace, final String app) throws K8sTestException {
        LOGGER.info("checking if deployments exit for application: {} in namespace: {}", app, namespace);
        final boolean result = getDeployments(namespace, app).size() > 0;
        LOGGER.info("deployments exit for application: {} in namespace: {} is: {}", app, namespace, result);
        return result;
    }

    public void deleteDeployments(final String namespace, final String app) throws K8sTestException {
        try {
            LOGGER.info("deleting deployments for application: {} in namespace: {}", app, namespace);
            final List<V1Deployment> deployments = getDeployments(namespace, app);
            for (V1Deployment deployment : deployments) {
                final String name = Objects.requireNonNull(deployment.getMetadata()).getName();
                LOGGER.info("deleting deployment: {}", name);
                appsV1Api.deleteNamespacedDeployment(name, namespace, null, null, 0, null, PROPAGATION_POLICY_BACKGROUND, null);
            }

        } catch (final ApiException ex) {
            throw new K8sTestException(ex.getResponseBody(), ex);
        }
    }

    public void createDeployment(final String namespace, final String appName, final String deploymentName, final String image, int exposedPort, int replicas) throws K8sTestException {
        LOGGER.info("creating deployment: {} for application: {} in namespace: {}", deploymentName, appName, namespace);
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
        container.setImagePullPolicy(IMAGE_PULL_POLICY_ALWAYS);

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
