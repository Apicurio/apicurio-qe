package apicurito.tests.steps;

import static org.assertj.core.api.Assertions.assertThat;

import io.syndesis.qe.marketplace.manifests.Bundle;
import io.syndesis.qe.marketplace.manifests.Index;
import io.syndesis.qe.marketplace.manifests.Opm;
import io.syndesis.qe.marketplace.openshift.OpenShiftConfiguration;
import io.syndesis.qe.marketplace.openshift.OpenShiftService;
import io.syndesis.qe.marketplace.openshift.OpenShiftUser;
import io.syndesis.qe.marketplace.quay.QuayUser;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import apicurito.tests.configuration.Component;
import apicurito.tests.configuration.TestConfiguration;
import apicurito.tests.configuration.templates.ApicuritoTemplate;
import apicurito.tests.utils.openshift.OpenShiftUtils;
import apicurito.tests.utils.slenide.ConfigurationOCPUtils;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.fabric8.kubernetes.api.model.Pod;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConfigurationOCPSteps {

    @When("check that {string} has {int} pods")
    public void checkThatComponentHasPods(String name, int count) {
        log.info("Checking that " + name + " has exatly " + count + " pods");
        List<Pod> pods = OpenShiftUtils.getInstance().getPods();
        int counter = 0;
        for (Pod pod : pods) {
            if (pod.getMetadata().getName().contains(name) && pod.getStatus().getPhase().equals("Running")) {
                ++counter;
            }
        }
        assertThat(counter).as(name + " should have %s pods but currently run %s", count, counter).isEqualTo(count);
    }

    @When("deploy {string} custom resource")
    public void deployCustomResource(String sequenceNumber) {
        log.info("Deploying " + sequenceNumber + " custom resource");
        String cr =
            "https://gist.githubusercontent.com/mmajerni/e47e14f2a1c2bf934219cb3d4508e81c/raw/749535da7044dcdb8f35c0d9aa88d4f25a444f43/" +
                "operatorUpdateTest.yaml";

        ConfigurationOCPUtils.applyInOCP("Custom Resource", cr);

        log.info("Waiting for Apicurito pods");
        if ("first".equals(sequenceNumber)) {
            ApicuritoTemplate.waitForApicurito("component", 4, Component.SERVICE);
        } else {
            ConfigurationOCPUtils.waitForRollout();
        }
    }

    @When("deploy another operator with ui image {string}")
    public void deployAnotherOperator(String uiImage) {
        log.info("Deploying another operator with UI image: " + uiImage);
        String operator =
            "https://gist.githubusercontent.com/mmajerni/e7a4b5287f92c7ef228ba655883048be/raw/f0bdb833c9092f7b4d3057b242790a5687e3d403/" +
                "apicuritoOperatorUpdate.yaml";
        ConfigurationOCPUtils.applyInOCP("Operator", operator);
        ConfigurationOCPUtils.setTestEnvToOperator("RELATED_IMAGE_APICURITO", uiImage);
        ConfigurationOCPUtils.waitForOperatorUpdate();
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

    @When("deploy operator from operatorhub")
    public void deployOperatorHub() {
        log.info("Creating apicurito index.");
        Opm opm = new Opm();
        QuayUser quayUser = new QuayUser(
            TestConfiguration.getQuayUsername(),
            TestConfiguration.getQuayPassword(),
            TestConfiguration.getQuayNamespace(),
            TestConfiguration.getQuayToken()
        );

        Index index = opm.createIndex("quay.io/marketplace/fuse-apicurito-index:" + TestConfiguration.APICURITO_IMAGE_VERSION);
        Bundle apicuritoBundle = index.addBundle(TestConfiguration.apicuritoOperatorMetadataUrl());
        index.push(quayUser);
        OpenShiftService ocpSvc = getOpenShiftService("fuse-apicurito");

        try {
            // OCP stuff - add index
            ocpSvc.patchGlobalSecrets(TestConfiguration.getQuayPullSecret());
            index.addIndexToCluster(ocpSvc, "apicurito-test-catalog");
            log.info("Creating apcurito subscription");
            apicuritoBundle.createSubscription(ocpSvc);
        } catch (IOException | TimeoutException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static OpenShiftService getOpenShiftService(String quayProject) {
        OpenShiftUser adminUser = new OpenShiftUser(
            TestConfiguration.openshiftUsername(),
            TestConfiguration.openshiftPassword(),
            TestConfiguration.openShiftUrl()
        );
        OpenShiftConfiguration openShiftConfiguration = OpenShiftConfiguration.builder()
            .namespace(TestConfiguration.openShiftNamespace())
            .pullSecretName("apicurito-pull-secret")
            .pullSecret(TestConfiguration.apicuritoPullSecret())
            .quayOpsrcToken(TestConfiguration.getQuayOpsrcToken())
            .build();
        return new OpenShiftService(
            TestConfiguration.getQuayNamespace(),
            quayProject,
            openShiftConfiguration,
            adminUser,
            null
        );
    }

    @When("delete running instance of apicurito")
    public void deleteRunningInstanceOfApicurito() {
        ApicuritoTemplate.cleanNamespace();
    }

    @Then("clean openshift after operatorhub test")
    public void cleanOpenshiftAfterOperatorhubTest() {
        String csv =
            OpenShiftUtils.getInstance().apps().deployments().inNamespace(TestConfiguration.openShiftNamespace()).withName("fuse-apicurito").get()
                .getMetadata().getOwnerReferences().get(0).getName();
        OpenShiftUtils.binary().execute("delete", "csv", csv);
        OpenShiftUtils.binary().execute("delete", "subscription", "fuse-apicurito");
        OpenShiftUtils.binary().execute("delete", "CatalogSource", "apicurito-test-catalog", "-n", "openshift-marketplace");
        ApicuritoTemplate.cleanNamespace();
    }

    @Then("reinstall apicurito")
    public void reinstallApicurito() {
        ApicuritoTemplate.cleanNamespace();
        ApicuritoTemplate.reinstallApicurito();
    }

    @Then("check that metering labels have correct values for \"([^\"]*)\"$")
    public void checkThatMeteringLabelsHaveCorrectValues(Component component) {
        final String version = Double.toString(Double.parseDouble(TestConfiguration.APICURITO_IMAGE_VERSION) + 6);
        final String company = "Red_Hat";
        final String prodName = "Red_Hat_Integration";
        final String componentName = "Fuse";
        final String subcomponent_t = "infrastructure";

        List<Pod> pods = OpenShiftUtils.getInstance().getPods();

        for (Pod p : pods) {
            if (p.getStatus().getPhase().contains("Running") && p.getMetadata().getName().contains(component.getName())) {
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
                assertThat(labels.get("rht.prod_ver")).isEqualTo(version);
                assertThat(labels.get("rht.comp")).isEqualTo(componentName);
                assertThat(labels.get("rht.comp_ver")).isEqualTo(version);
                assertThat(labels.get("rht.subcomp")).isEqualTo(component.getName());
                assertThat(labels.get("rht.subcomp_t")).isEqualTo(subcomponent_t);
            }
        }
    }
}
