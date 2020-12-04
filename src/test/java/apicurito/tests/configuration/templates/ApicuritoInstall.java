package apicurito.tests.configuration.templates;

import static org.assertj.core.api.Assertions.fail;

import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import apicurito.tests.configuration.Component;
import apicurito.tests.configuration.TestConfiguration;
import apicurito.tests.utils.openshift.OpenShiftUtils;
import cz.xtf.core.waiting.WaiterException;
import io.fabric8.kubernetes.api.model.apps.ReplicaSet;
import io.fabric8.kubernetes.client.KubernetesClientException;
import lombok.extern.slf4j.Slf4j;

@Slf4j

public class ApicuritoInstall {

    public static void cleanNamespace() {
        TestConfiguration.printDivider("Deleting namespace resources");

        try {
            OpenShiftUtils.getInstance().customResourceDefinitions().withName("apicuritos.apicur.io").delete();
            OpenShiftUtils.binary().execute("delete", "ClusterRole", "apicurito");
            OpenShiftUtils.binary().execute("delete", "ClusterRoleBinding", "apicurito");
            //OCP4HACK - openshift-client 4.3.0 isn't supported with OCP4 and can't create/delete templates, following line can be removed later
            OpenShiftUtils.binary().execute("delete", "template", "--all", "--namespace", TestConfiguration.openShiftNamespace());
            OpenShiftUtils.getInstance().apps().statefulSets().inNamespace(TestConfiguration.openShiftNamespace()).delete();
            OpenShiftUtils.getInstance().apps().deployments().inNamespace(TestConfiguration.openShiftNamespace()).delete();
            OpenShiftUtils.getInstance().serviceAccounts().inNamespace(TestConfiguration.openShiftNamespace()).delete();
        } catch (KubernetesClientException ex) {
            // Probably user does not have permissions to delete.. a nice exception will be printed when deploying
        }
        try {
            OpenShiftUtils.getInstance().clean();

            List<ReplicaSet> operatorReplicaSets =
                OpenShiftUtils.getInstance().apps().replicaSets().inNamespace(TestConfiguration.openShiftNamespace()).list().getItems();

            for (ReplicaSet rs : operatorReplicaSets) {
                OpenShiftUtils.binary().execute("delete", "rs", rs.getMetadata().getName());
            }

            OpenShiftUtils.getInstance().waiters().isProjectClean().waitFor();
        } catch (WaiterException e) {
            log.warn("Project was not clean after 20s, retrying once again");
            OpenShiftUtils.getInstance().clean();
            OpenShiftUtils.getInstance().waiters().isProjectClean().waitFor();
        }
        OpenShiftUtils.xtf().getTemplates().forEach(OpenShiftUtils.xtf()::deleteTemplate);
    }

    public static void waitForApicurito(String key, Integer numberOfPods, Component component) {
        TestConfiguration.printDivider("Waiting for Apicurito to become ready...");

        EnumSet<Component> components = EnumSet.noneOf(Component.class);
        components.add(component);

        ExecutorService executorService = Executors.newFixedThreadPool(components.size());
        components.forEach(c -> {
            Runnable runnable = () ->
                OpenShiftUtils.xtf().waiters()
                    .areExactlyNPodsReady(numberOfPods, key, c.getName())
                    .interval(TimeUnit.SECONDS, 10)
                    .timeout(TimeUnit.MINUTES, 6)
                    .waitFor();
            executorService.submit(runnable);
        });

        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(20, TimeUnit.MINUTES)) {
                executorService.shutdownNow();
                fail("Apicurito wasn't initilized in time");
            }
        } catch (InterruptedException e) {
            fail("Apicurito wasn't initilized in time");
        }
    }

    public static void reinstallApicurito() {
        OpenShiftUtils.createPullSecret();
        if (TestConfiguration.useOperator()) {
            ApicuritoOperator.reinstallApicurito();
        } else {
            ApicuritoTemplate.reinstallApicurito();
        }
    }
}
