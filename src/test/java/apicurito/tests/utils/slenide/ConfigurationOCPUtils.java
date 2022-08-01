package apicurito.tests.utils.slenide;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import io.syndesis.qe.marketplace.util.HelperFunctions;

import com.jayway.jsonpath.Criteria;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.Filter;
import com.jayway.jsonpath.JsonPath;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import apicurito.tests.configuration.Component;
import apicurito.tests.configuration.ReleaseSpecificParameters;
import apicurito.tests.configuration.TestConfiguration;
import apicurito.tests.steps.ConfigurationOCPSteps;
import apicurito.tests.utils.openshift.OpenShiftUtils;
import cz.xtf.core.openshift.OpenShift;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.apps.ReplicaSet;
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConfigurationOCPUtils {

    public static void waitForRollout() {
        //Wait for Rollout until there is no unavailable pod
        log.info("Waiting for pods rollout.");
        Integer tmp = Integer.MAX_VALUE;
        while (tmp != null) {
            //Wait for 5 seconds
            CommonUtils.sleepFor(5);

            tmp = OpenShiftUtils.getInstance().apps().deployments().inNamespace(TestConfiguration.openShiftNamespace()).list().getItems().get(1)
                .getStatus().getUnavailableReplicas();
        }

        //Wait another 15 seconds because of termination running pods
        CommonUtils.sleepFor(15);
    }

    public static void waitForPodsUpdate(int numberOfPods) {
        log.info("Waiting for 2 created replica sets");
        //Wait for 2 minutes for replica sets
        if (!areTwoReplicaSetsAvailable(Component.SERVICE)) {
            fail("TIMEOUT 2 minutes : Failed to load 2 UI replica sets");
        }
        if (!areTwoReplicaSetsAvailable(Component.GENERATOR)) {
            fail("TIMEOUT 2 minutes : Failed to load 2 generator replica sets");
        }
        log.info("Waiting for exactly " + numberOfPods + " running Apicurito pod.");
        //Wait 3 minutes for n running pods
        if (!areExactlyNPodsRunning(numberOfPods, Component.SERVICE)) {
            fail("TIMEOUT 3 minutes: Failed to load " + numberOfPods + " running Apicurito UI pods.");
        }
        if (!areExactlyNPodsRunning(numberOfPods, Component.GENERATOR)) {
            fail("TIMEOUT 3 minutes: Failed to load " + numberOfPods + " running Apicurito generator pods.");
        }
    }

    public static void waitForOperatorUpdate() {
        try {
            OpenShift ocp = ConfigurationOCPSteps.getOpenShiftService().getClient();
            String previousCSV = ReleaseSpecificParameters.APICURITO_OLD_CSV;
            String newCSV = ReleaseSpecificParameters.APICURITO_CURRENT_CSV;
            Filter completeFilter = Filter.filter(Criteria.where("phase").is("Complete"));
            Filter matchesCSVs = Filter.filter(Criteria.where("bundleLookups.identifier").eq(newCSV).and("bundleLookups.replaces").eq(previousCSV));
            HelperFunctions.waitFor(() -> {
                DocumentContext documentContext = JsonPath.parse(ocp.customResource(installPlanContext()).list(ocp.getNamespace()));
                Object read = documentContext.read("$.items[*].status[?]", completeFilter);
                Object found = JsonPath.parse(read).read("$", matchesCSVs);
                return found != null;
            }, 2L, 300L);
        } catch (Exception e) {
            log.error("Exception", e);
            throw new RuntimeException(e);
        }
    }

    private static CustomResourceDefinitionContext installPlanContext() {
        return (new CustomResourceDefinitionContext.Builder()).withGroup("operators.coreos.com").withPlural("installplans").withScope("Namespaced")
            .withVersion("v1alpha1").build();
    }

    public static void setTestEnvToOperator(String nameOfEnv, String valueOfEnv) {
        log.info("Setting test ENV: " + nameOfEnv + "=" + valueOfEnv);
        final String output = OpenShiftUtils.binary().execute(
            "set",
            "env",
            "deployment",
            "fuse-apicurito",
            nameOfEnv + "=" + valueOfEnv
        );
    }

    public static void createInOCP(String itemName, String item) {
        log.info("Creating " + itemName + " from: " + item);

        final String output = OpenShiftUtils.binary().execute(
            "create",
            "-n", TestConfiguration.openShiftNamespace(),
            "-f", item
        );
    }

    public static void applyInOCP(String itemName, String item) {
        log.info("Applying {} from: {}", itemName, item);
        final String output = OpenShiftUtils.binary().execute(
            "apply", "-n", TestConfiguration.openShiftNamespace(), "-f", item
        );
    }

    public static void checkPodsVersion(String version) {
        List<Pod> pods = OpenShiftUtils.getInstance().getPods();
        for (Pod p : pods) {
            if (p.getStatus().getPhase().contains("Running")) {
                Map<String, String> labels = p.getMetadata().getLabels();
                assertThat(labels).containsKey("rht.prod_ver");
                assertThat(labels).containsKey("rht.comp_ver");

                assertThat(labels.get("rht.prod_ver")).isEqualTo(version);
                assertThat(labels.get("rht.comp_ver")).isEqualTo(version);
            }
        }
    }

    private static List<ReplicaSet> getApicuritoReplicaSets(Component component) {
        List<ReplicaSet> uiRs = new ArrayList<>();

        List<ReplicaSet> listRs = OpenShiftUtils.getInstance().apps().replicaSets().list().getItems();
        for (ReplicaSet rs : listRs) {
            if (component.getName().equals(rs.getMetadata().getOwnerReferences().get(0).getName())) {
                uiRs.add(rs);
            }
        }
        return uiRs;
    }

    private static boolean areTwoReplicaSetsAvailable(Component component) {
        int counter = 0;
        while (counter < 12) {
            CommonUtils.sleepFor(10);

            if (getApicuritoReplicaSets(component).size() == 2) {
                return true;
            }
            ++counter;
        }
        return false;
    }

    private static boolean areExactlyNPodsRunning(int numberOfPods, Component component) {
        List<ReplicaSet> uiRs = new ArrayList<>();
        int counter = 0;

        while (counter < 18) {
            uiRs.clear();
            uiRs = getApicuritoReplicaSets(component);
            CommonUtils.sleepFor(10);

            if (uiRs.get(0).getStatus().getReplicas() + uiRs.get(1).getStatus().getReplicas() == numberOfPods) {
                return true;
            }
            ++counter;
        }
        return false;
    }

    public static void waitForOneReplicaSet() {
        int counter = 0;
        boolean oneReplica = false;
        while (!oneReplica) {
            CommonUtils.sleepFor(5);

            if (getApicuritoReplicaSets(Component.SERVICE).size() == 1 && getApicuritoReplicaSets(Component.GENERATOR).size() == 1) {
                oneReplica = true;
            }
            ++counter;
            if (counter == 6) {
                fail("waiting too long for replica set");
            }
        }
    }
}
