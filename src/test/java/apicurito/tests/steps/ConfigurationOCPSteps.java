package apicurito.tests.steps;

import static org.assertj.core.api.Assertions.assertThat;

import io.syndesis.qe.marketplace.openshift.OpenShiftConfiguration;
import io.syndesis.qe.marketplace.openshift.OpenShiftService;
import io.syndesis.qe.marketplace.openshift.OpenShiftUser;
import io.syndesis.qe.marketplace.util.HelperFunctions;

import org.json.JSONObject;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import apicurito.tests.configuration.Component;
import apicurito.tests.configuration.ReleaseSpecificParameters;
import apicurito.tests.configuration.TestConfiguration;
import apicurito.tests.configuration.templates.ApicuritoInstall;
import apicurito.tests.configuration.templates.ApicuritoOperator;
import apicurito.tests.configuration.templates.ApicuritoOperatorhub;
import apicurito.tests.configuration.templates.ApicuritoTemplate;
import apicurito.tests.utils.openshift.OpenShiftUtils;
import apicurito.tests.utils.slenide.ConfigurationOCPUtils;
import cz.xtf.core.openshift.OpenShift;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.fabric8.kubernetes.api.model.Pod;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConfigurationOCPSteps {

    public static final String SUBSCRIPTION_NAME = "fuse-apicurito";

    @When("^check that \"([^\"]*)\" has \"([^\"]*)\" pods$")
    public void checkThatComponentHasPods(Component component, int count) {
        log.info("Checking that " + component.getName() + " has exatly " + count + " pods");
        List<Pod> pods = OpenShiftUtils.getInstance().getPods();
        int counter = 0;
        for (Pod pod : pods) {
            if (pod.getMetadata().getName().contains(component.getName())
                && pod.getStatus().getPhase().equals("Running")) {
                ++counter;
            }
        }
        assertThat(counter).as(component.getName() + " should have %s pods but currently run %s", count, counter)
            .isEqualTo(count);
    }

    /**
     * @param crName current || older || second
     * current CR - CR with the current API version
     * older CR - CR with the deprecated v1alpha1 API version
     * second CR - CR which replaced another CR (used in @update test)
     */
    @When("deploy {string} custom resource")
    public static void deployCustomResource(String crName) {
        log.info("Deploying " + crName + " custom resource");
        String cr;
        if (crName.equals("older")) {
            cr = "src/test/resources/CRs/v1alpha1CR.yaml";
        } else {
            cr = "src/test/resources/CRs/twoPodsCR.yaml";
        }
        ConfigurationOCPUtils.applyInOCP("Custom Resource", cr);

        log.info("Waiting for Apicurito pods");
        ApicuritoInstall.waitForApicurito("component", 2, Component.SERVICE);
        if ("second".equals(crName)) {
            ConfigurationOCPUtils.waitForRollout();
        }
    }

    @When("update operator to the new version")
    public void updateOperator() {
        log.info("Update operator to the new version");
        InputStream deploymentConfig = null;
        try {
            deploymentConfig = new FileInputStream("src/test/resources/generatedFiles/deployment.gen.yaml");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Map<String, Object> deployment = new Yaml().load(deploymentConfig);
        Map<String, String> address = ((Map<String, String>) ((Map<String, List<Map>>) ((Map<String, Map>) deployment
            .get("spec")).get("template").get("spec")).get("containers").get(0));
        address.put("image", TestConfiguration.apicuritoOperatorImageUrl());
        try {
            Path tmpFile = Files.createTempFile("temporaryOperator", ".yaml");
            Files.write(tmpFile, new Yaml().dump(deployment).getBytes(StandardCharsets.UTF_8));
            ConfigurationOCPUtils.applyInOCP("Operator", tmpFile.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }

        ConfigurationOCPUtils.setTestEnvToOperator("RELATED_IMAGE_APICURITO_OPERATOR",
            TestConfiguration.apicuritoOperatorImageUrl());

        if (TestConfiguration.apicuritoGeneratorImageUrl() != null) {
            ConfigurationOCPUtils.setTestEnvToOperator("RELATED_IMAGE_GENERATOR",
                TestConfiguration.apicuritoGeneratorImageUrl());
        }

        if (TestConfiguration.apicuritoUiImageUrl() != null) {
            ConfigurationOCPUtils.setTestEnvToOperator("RELATED_IMAGE_APICURITO",
                TestConfiguration.apicuritoUiImageUrl());
        }
        ConfigurationOCPUtils.waitForPodsUpdate(1);
    }

    /**
     * @param podType operator || image
     * @param value pod image
     */
    @When("check that apicurito {string} is {string}")
    public void checkThatApicuritoImageIs(String podType, String value) {
        log.info("Checking that Apicurito " + podType + " pod is created from image: " + value);
        String nameOfPod = "operator".equals(podType) ? Component.OPERATOR.getName() : Component.SERVICE.getName();

        List<Pod> pods = OpenShiftUtils.getInstance().getPods();
        String imageName = "";
        for (Pod pod : pods) {
            if (pod.getMetadata().getName().contains(nameOfPod)) {
                imageName = pod.getSpec().getContainers().get(0).getImage();
                break;
            }
        }
        if (value.equals("default")) {
            if ("operator".equals(podType)) {
                value = TestConfiguration.apicuritoOperatorImageUrl();
            } else {
                value = TestConfiguration.apicuritoUiImageUrl();
            }
        }
        assertThat(imageName).as("Apicurito has not container from %s", value).asString().contains(value);
    }

    @Then("check that apicurito operator is deployed and in running state")
    public void checkThatApicuritoOperatorIsDeployedAndInRunningState() {
        log.info("Checking that operator is deployed and in running state");
        OpenShiftUtils.xtf().waiters()
            .areExactlyNPodsReady(1, "name", "fuse-apicurito")
            .interval(TimeUnit.SECONDS, 2)
            .timeout(TimeUnit.MINUTES, 3)
            .waitFor();
    }

    @When("deploy older operator from operatorhub")
    public void deployOlderOperatorHub() {
        log.info("Creating apicurito subscription");
        ApicuritoOperatorhub.deploy(ReleaseSpecificParameters.APICURITO_OLD_UPDATE_CHANNEL, "older");
    }

    @When("upgrade operator via operatorhub")
    public void upgradeOperatorhub() {
        log.info("Upgrading apicurito subscription");

        try {
            OpenShift ocp = ConfigurationOCPSteps.getOpenShiftService().getClient();
            String namespace = ConfigurationOCPSteps.getOpenShiftService().getClient().getNamespace();
            JSONObject subscription = ApicuritoOperatorhub.getSubscription();
            assertThat(subscription).describedAs("Apicurito subscription was not found in " + namespace + " namespace.").isNotNull();
            subscription.getJSONObject("spec").put("channel", ReleaseSpecificParameters.APICURITO_CURRENT_UPDATE_CHANNEL);
            HelperFunctions.waitFor(() -> {
                try {
                    ocp.customResource(ApicuritoOperatorhub.getSubscriptionContext()).edit(namespace, SUBSCRIPTION_NAME, subscription.toString());
                    return true;
                } catch (Exception e) {
                    return false;
                }
            }, 120L, 300L);
            ConfigurationOCPUtils.waitForOperatorUpdate();
            ConfigurationOCPUtils.waitForPodsUpdate(2);
        } catch (InterruptedException | TimeoutException e) {
            log.error("Exception", e);
            throw new RuntimeException(e);
        }
    }

    @Then("check that all deployed pods have (newer|older) version$")
    public void checkThatPodsHaveVersion(String version) {
        if ("older".equals(version)) {
            ConfigurationOCPUtils.checkPodsVersion(ReleaseSpecificParameters.APICURITO_PREVIOUS_VERSION);
        } else {
            ConfigurationOCPUtils.checkPodsVersion(ReleaseSpecificParameters.APICURITO_CURRENT_VERSION);
        }
    }

    @When("delete running instance of apicurito")
    public void deleteRunningInstanceOfApicurito() {
        ApicuritoInstall.cleanNamespace();
    }

    @Then("clean openshift after operatorhub test")
    public static void cleanOpenshiftAfterOperatorhubTest() {
        OpenShiftUtils.binary().execute("delete", "csvs", "-l", "operators.coreos.com/fuse-apicurito.apicurito=");

        OpenShiftUtils.binary().execute("delete", "subscription", "fuse-apicurito");
        if (TestConfiguration.namespaceCleanupAfter()) {
            OpenShiftUtils.binary().execute("delete", "operatorgroup", "fo-operatorgroup");
            OpenShiftUtils.binary().execute("delete", "CatalogSource", "apicurito-test-catalog", "-n",
                "openshift-marketplace");
        }
    }

    @Then("reinstall apicurito")
    public void reinstallApicurito() {
        ApicuritoInstall.cleanNamespace();
        ApicuritoInstall.reinstallApicurito();
    }

    @Then("check that metering labels have correct values for \"([^\"]*)\"$")
    public void checkThatMeteringLabelsHaveCorrectValues(Component component) {
        final String company = "Red_Hat";
        final String prodName = "Red_Hat_Integration";
        final String componentName = "Fuse";
        final String subcomponent_t = "infrastructure";

        List<Pod> pods = OpenShiftUtils.getInstance().getPods();

        for (Pod p : pods) {
            if (p.getStatus().getPhase().contains("Running")
                && p.getMetadata().getName().contains(component.getName())) {
                Map<String, String> labels = p.getMetadata().getLabels();
                assertThat(labels).containsKey("com.company");
                assertThat(labels).containsKey("rht.prod_name");
                assertThat(labels).containsKey("rht.prod_ver");
                assertThat(labels).containsKey("rht.comp");
                assertThat(labels).containsKey("rht.comp_ver");
                assertThat(labels).containsKey("rht.subcomp");
                assertThat(labels).containsKey("rht.subcomp_t");

                assertThat(labels.get("com.company")).isEqualTo(company);
                assertThat(labels.get("rht.prod_name")).isEqualTo(prodName);
                assertThat(labels.get("rht.prod_ver")).isEqualTo(ReleaseSpecificParameters.APICURITO_CURRENT_VERSION);
                assertThat(labels.get("rht.comp")).isEqualTo(componentName);
                assertThat(labels.get("rht.comp_ver")).isEqualTo(ReleaseSpecificParameters.APICURITO_CURRENT_VERSION);
                assertThat(labels.get("rht.subcomp")).isEqualTo(component.getName());
                assertThat(labels.get("rht.subcomp_t")).isEqualTo(subcomponent_t);
            }
        }
    }

    @Then("check that name and image of operator in operatorhub are correct")
    public void checkThatNameAndImageOfOperatorInOperatorhubAreCorrect() {
        final String output = OpenShiftUtils.binary().execute("describe", "csvs", "-l", "operators.coreos.com/fuse-apicurito.apicurito=");
        assertThat(output).contains("Display Name:  Red Hat Integration - API Designer");
        assertThat(output).contains("PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHZpZXdCb3g9IjAgMCAxMD");
    }

    @When("install older version of apicurito")
    public void installOlderVersionOfApicurito() {
        ApicuritoTemplate.cleanNamespace();

        ConfigurationOCPUtils.createInOCP("CRD", TestConfiguration.apicuritoOperatorCrdUrl());
        ConfigurationOCPUtils.createInOCP("Service", TestConfiguration.apicuritoOperatorServiceUrl());
        ConfigurationOCPUtils.createInOCP("Cluster Role", TestConfiguration.apicuritoOperatorClusterRoleUrl());
        ConfigurationOCPUtils.createInOCP("Cluster Role binding",
            TestConfiguration.apicuritoOperatorClusterRoleBindingUrl());
        ConfigurationOCPUtils.createInOCP("Role", TestConfiguration.apicuritoOperatorRoleUrl());
        ConfigurationOCPUtils.createInOCP("Role binding", TestConfiguration.apicuritoOperatorRoleBindingUrl());

        // Add pull secret to both apicurito and default service accounts - apicurito
        // for operator, default for UI image
        OpenShiftUtils.addImagePullSecretToServiceAccount("default", "apicurito-pull-secret");
        OpenShiftUtils.addImagePullSecretToServiceAccount("apicurito", "apicurito-pull-secret");

        OpenShiftUtils.getInstance().apps().deployments()
            .create(ApicuritoOperator.getUpdatedOperatorDeployment(ReleaseSpecificParameters.OLD_OPERATOR_URL));

        ConfigurationOCPUtils.setTestEnvToOperator("RELATED_IMAGE_APICURITO_OPERATOR",
            ReleaseSpecificParameters.OLD_OPERATOR_URL);

        ConfigurationOCPUtils.applyInOCP("Custom Resource",
            "https://gist.githubusercontent.com/mmajerni/7365800673a3014d6da8773650377d6c/raw/42811ef45986795a8fa3466ad384c9e172057e70/cr_1_pod" +
                ".yaml");

        ConfigurationOCPUtils.waitForOneReplicaSet();
    }

    public static OpenShiftService getOpenShiftService() {
        OpenShiftUser adminUser = new OpenShiftUser(TestConfiguration.openshiftUsername(),
            TestConfiguration.openshiftPassword(), TestConfiguration.openShiftUrl());
        OpenShiftConfiguration openShiftConfiguration = OpenShiftConfiguration.builder()
            .namespace(TestConfiguration.openShiftNamespace()).build();
        return new OpenShiftService(openShiftConfiguration,
            adminUser, null);
    }
}
