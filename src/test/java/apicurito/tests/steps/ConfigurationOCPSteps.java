package apicurito.tests.steps;

import static org.assertj.core.api.Assertions.assertThat;

import io.syndesis.qe.marketplace.manifests.Bundle;
import io.syndesis.qe.marketplace.manifests.Index;
import io.syndesis.qe.marketplace.manifests.Opm;
import io.syndesis.qe.marketplace.openshift.OpenShiftConfiguration;
import io.syndesis.qe.marketplace.openshift.OpenShiftService;
import io.syndesis.qe.marketplace.openshift.OpenShiftUser;
import io.syndesis.qe.marketplace.quay.QuayUser;

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
import apicurito.tests.configuration.templates.ApicuritoTemplate;
import apicurito.tests.utils.openshift.OpenShiftUtils;
import apicurito.tests.utils.slenide.ConfigurationOCPUtils;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.fabric8.kubernetes.api.model.Pod;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConfigurationOCPSteps {

    private Bundle currentBundle, oldBundle;
    private Index index;
    private String csv;

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

    @When("deploy {string} custom resource")
    public void deployCustomResource(String sequenceNumber) {
        log.info("Deploying " + sequenceNumber + " custom resource");
        String cr = "https://gist.githubusercontent.com/mmajerni/e47e14f2a1c2bf934219cb3d4508e81c/raw/5ce734862bb98ad3da959a7d2a1deb702269f796/"
            + "operatorUpdateTest.yaml";

        ConfigurationOCPUtils.applyInOCP("Custom Resource", cr);

        log.info("Waiting for Apicurito pods");
        if ("first".equals(sequenceNumber)) {
            ApicuritoInstall.waitForApicurito("component", 2, Component.SERVICE);
        } else {
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
        ConfigurationOCPUtils.waitForOperatorUpdate(1);
    }

    /**
     * @param podType operator || image
     * @param value   pod image
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

    @When("setup operatorhub on cluster")
    public void setupOperatorhub() {
        if (index == null) {
            log.info("Creating apicurito index.");
            OpenShiftService ocpSvc = getOpenShiftService();
            Opm opm = new Opm(ocpSvc);
            QuayUser quayUser = new QuayUser(TestConfiguration.getQuayUsername(), TestConfiguration.getQuayPassword());

            index = opm.createIndex(
                "quay.io/marketplace/fuse-apicurito-index:" + ReleaseSpecificParameters.APICURITO_IMAGE_VERSION,
                quayUser);
            currentBundle = index.addBundle(TestConfiguration.apicuritoOperatorMetadataUrl());
            oldBundle = index.addBundle(ReleaseSpecificParameters.APICURITO_OPERATOR_PREVIOUS_METADATA_URL);
            csv = currentBundle.getCSVName();
            try {
                index.addIndexToCluster("apicurito-test-catalog");
            } catch (InterruptedException | TimeoutException | IOException e) {
                log.error("Adding the index to cluster failed", e);
            }
        }
    }

    @When("deploy operator from operatorhub")
    public void deployOperatorHub() {
        try {
            log.info("Creating apicurito subscription");
            currentBundle.createSubscription();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @When("deploy older operator from operatorhub")
    public void deployOlderOperatorHub() {
        try {
            log.info("Creating apicurito subscription");
            oldBundle.createSubscription();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @When("upgrade operator via operatorhub")
    public void upgradeOperatorhub() {
        log.info("Upgrading apicurito subscription");
        oldBundle.update(currentBundle, true);
        ConfigurationOCPUtils.waitForOperatorUpdate(3);
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
    public void cleanOpenshiftAfterOperatorhubTest() {
        OpenShiftUtils.binary().execute("delete", "csv", csv);
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
        final String output = OpenShiftUtils.binary().execute("describe", "csv", csv, "-n",
            TestConfiguration.openShiftNamespace());
        assertThat(output).contains("Display Name:  API Designer");
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
            "https://gist.githubusercontent.com/mmajerni/7365800673a3014d6da8773650377d6c/raw/42811ef45986795a8fa3466ad384c9e172057e70/cr_1_pod.yaml");

        ConfigurationOCPUtils.waitForOneReplicaSet();
    }

    private static OpenShiftService getOpenShiftService() {
        OpenShiftUser adminUser = new OpenShiftUser(TestConfiguration.openshiftUsername(),
            TestConfiguration.openshiftPassword(), TestConfiguration.openShiftUrl());
        OpenShiftConfiguration openShiftConfiguration = OpenShiftConfiguration.builder()
            .namespace(TestConfiguration.openShiftNamespace())
            .dockerRegistry(TestConfiguration.getOperatorhubRegistryName())
            .dockerUsername(TestConfiguration.getOperatorhubRegistryUsername())
            .dockerPassword(TestConfiguration.getOperatorhubRegistryPassword())
            .icspFile(TestConfiguration.operatorhubIcspScriptURL()).build();
        return new OpenShiftService(openShiftConfiguration,
            adminUser, null);
    }
}
