package apicurito.tests.utils.slenide;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import apicurito.tests.configuration.Component;
import apicurito.tests.configuration.TestConfiguration;
import apicurito.tests.utils.openshift.OpenShiftUtils;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.apps.ReplicaSet;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConfigurationOCPUtils {

    public static void waitForOperatorUpdate(int numberOfPods) {
        log.info("Waiting for 2 created replica sets");
        //Wait for 2 minutes for replica sets
        if (!areTwoReplicaSetsAvailable()) {
            fail("TIMEOUT 2 minutes : Failed to load 2 replica sets");
        }

        log.info("Waiting for exactly" + numberOfPods + " running Apicurito UI pod.");
        //Wait 3 minutes for n running pods
        if (!areExactlyNUiPodsRunning(numberOfPods)) {
            fail("TIMEOUT 3 minutes: Failed to load " + numberOfPods + " running Apicurito pods.");
        }
    }

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

    private static List<ReplicaSet> getApicuritoUIreplicaSets() {
        List<ReplicaSet> uiRs = new ArrayList<>();

        List<ReplicaSet> listRs = OpenShiftUtils.getInstance().apps().replicaSets().list().getItems();
        for (ReplicaSet rs : listRs) {
            if (Component.SERVICE.getName().equals(rs.getMetadata().getOwnerReferences().get(0).getName())) {
                uiRs.add(rs);
            }
        }
        return uiRs;
    }

    private static boolean areTwoReplicaSetsAvailable() {
        int counter = 0;
        while (counter < 12) {
            CommonUtils.sleepFor(10);

            if (getApicuritoUIreplicaSets().size() == 2) {
                return true;
            }
            ++counter;
        }
        return false;
    }

    private static boolean areExactlyNUiPodsRunning(int numberOfPods) {
        List<ReplicaSet> uiRs = new ArrayList<>();
        int counter = 0;

        while (counter < 18) {
            uiRs.clear();
            uiRs = getApicuritoUIreplicaSets();
            CommonUtils.sleepFor(10);

            if (uiRs.get(0).getStatus().getReplicas() + uiRs.get(1).getStatus().getReplicas() == numberOfPods) {
                return true;
            }
            ++counter;
        }
        return false;
    }
}
