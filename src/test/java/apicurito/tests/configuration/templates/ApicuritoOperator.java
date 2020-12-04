package apicurito.tests.configuration.templates;

import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import apicurito.tests.configuration.Component;
import apicurito.tests.configuration.TestConfiguration;
import apicurito.tests.utils.HttpUtils;
import apicurito.tests.utils.openshift.OpenShiftUtils;
import apicurito.tests.utils.slenide.ConfigurationOCPUtils;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ApicuritoOperator extends ApicuritoInstall {

    /**
     * Loads the default deployment for operator (if custom deployment wasn't specified)
     * and updates it so it contains specified custom operator image
     * <p>
     * This method assumes that operator image has been specified in test.properties!
     *
     * @return Deployment containing the correct image
     */
    private static Deployment getUpdatedOperatorDeployment() {
        try (InputStream is = new URL(TestConfiguration.apicuritoOperatorDeploymentUrl()).openStream()) {
            Deployment deployment = OpenShiftUtils.getInstance().apps().deployments().load(is).get();
            // containers should contain only one object - get(0)
            deployment.getSpec().getTemplate().getSpec().getContainers().get(0).setImage(TestConfiguration.apicuritoOperatorImageUrl());
            return deployment;
        } catch (IOException e) {
            throw new IllegalArgumentException("Unable to read apicurito operator deployment", e);
        }
    }

    private static void deploy() {
        log.info("Deploying using GO operator");

        ConfigurationOCPUtils.createInOCP("CRD", TestConfiguration.apicuritoOperatorCrdUrl());
        ConfigurationOCPUtils.createInOCP("Service", TestConfiguration.apicuritoOperatorServiceUrl());
        ConfigurationOCPUtils.createInOCP("Cluster Role", TestConfiguration.apicuritoOperatorClusterRoleUrl());
        ConfigurationOCPUtils.createInOCP("Cluster Role binding", TestConfiguration.apicuritoOperatorClusterRoleBindingUrl());
        ConfigurationOCPUtils.createInOCP("Role", TestConfiguration.apicuritoOperatorRoleUrl());
        ConfigurationOCPUtils.createInOCP("Role binding", TestConfiguration.apicuritoOperatorRoleBindingUrl());

        // Add pull secret to both apicurito and default service accounts - apicurito for operator, default for UI image
        OpenShiftUtils.addImagePullSecretToServiceAccount("default", "apicurito-pull-secret");
        OpenShiftUtils.addImagePullSecretToServiceAccount("apicurito", "apicurito-pull-secret");

        // if operator image url was specified, use this image
        if (TestConfiguration.apicuritoOperatorImageUrl() != null) {
            OpenShiftUtils.getInstance().apps().deployments().create(getUpdatedOperatorDeployment());
            ConfigurationOCPUtils.setTestEnvToOperator("RELATED_IMAGE_APICURITO_OPERATOR", TestConfiguration.apicuritoOperatorImageUrl());
        } else {
            ConfigurationOCPUtils.createInOCP("Operator", TestConfiguration.apicuritoOperatorDeploymentUrl());
        }

        // as this testsuite is not testing generator, set it to empty string
        // when not set to empty string, there is ImagePullBackOff when trying to access private registry
        ConfigurationOCPUtils.setTestEnvToOperator("RELATED_IMAGE_GENERATOR", "docker.io/mmajerni/gentest:latest");

        if (TestConfiguration.apicuritoUiImageUrl() != null) {
            ConfigurationOCPUtils.setTestEnvToOperator("RELATED_IMAGE_APICURITO", TestConfiguration.apicuritoUiImageUrl());
        }

        ConfigurationOCPUtils.applyInOCP("Custom Resource", TestConfiguration.apicuritoOperatorCrUrl());
    }

    public static void reinstallApicurito() {
        deploy();
        waitForApicurito("component", 6, Component.SERVICE);
    }
}
