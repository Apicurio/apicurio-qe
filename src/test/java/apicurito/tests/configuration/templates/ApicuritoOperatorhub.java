package apicurito.tests.configuration.templates;

import static org.assertj.core.api.Assertions.fail;

import io.syndesis.qe.marketplace.manifests.Bundle;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import apicurito.tests.configuration.Component;
import apicurito.tests.configuration.ReleaseSpecificParameters;
import apicurito.tests.configuration.TestConfiguration;
import apicurito.tests.steps.ConfigurationOCPSteps;
import apicurito.tests.utils.openshift.OpenShiftUtils;
import apicurito.tests.utils.openshift.OpenShiftWaitUtils;
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j

public class ApicuritoOperatorhub extends ApicuritoInstall {
    public static final String SUBSCRIPTION_NAME = "fuse-apicurito";

    private static void deploy() {
        log.info("Deploying using Operatorhub");

        if (subscriptionExists()) {
            return;
        }

        Bundle.createSubscription(ConfigurationOCPSteps.getOpenShiftService(), SUBSCRIPTION_NAME,
            ReleaseSpecificParameters.APICURITO_CURRENT_UPDATE_CHANNEL, "''",
            TestConfiguration.getApicuritoIibImage());

        OpenShiftUtils.xtf().waiters()
            .areExactlyNPodsReady(1, "name", "fuse-apicurito")
            .interval(TimeUnit.SECONDS, 2)
            .timeout(TimeUnit.MINUTES, 3)
            .waitFor();

        ConfigurationOCPSteps.deployCustomResource("first");
        OpenShiftUtils.getInstance().addRoleToUser("view", TestConfiguration.openshiftUsername());
    }

    public static void withRetry(Boolean supplier, int maxRetries, long delay, String failMessage) {
        int retries = 0;
        while (retries <= maxRetries) {
            if (supplier) {
                break;
            }
            if (retries == maxRetries) {
                fail(failMessage);
            } else {
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    log.error("Sleep was interrupted!");
                    e.printStackTrace();
                }
            }
            retries++;
        }
    }

    private static boolean enableTestSupportInCsv() {
        try {
            OpenShiftWaitUtils
                .waitFor(() -> "AtLatestKnown".equalsIgnoreCase(getSubscription().getJSONObject("status").getString("state")), null, 2, 60 * 3);
        } catch (Exception e) {
            e.printStackTrace();
        }
        JSONObject json = new JSONObject(
            OpenShiftUtils.getInstance().customResource(getCSVContext())
                .get(TestConfiguration.openShiftNamespace(), TestConfiguration.getOperatorHubCSVName()));
        JSONObject operatorDeployment =
            json.getJSONObject("spec").getJSONObject("install").getJSONObject("spec").getJSONArray("deployments").getJSONObject(0);
        JSONArray envVars =
            operatorDeployment.getJSONObject("spec").getJSONObject("template").getJSONObject("spec").getJSONArray("containers").getJSONObject(0)
                .getJSONArray("env");
        HashMap<String, String> vars = new HashMap<String, String>();
        vars.put("name", "TEST_SUPPORT");
        vars.put("value", "true");
        envVars.put(vars);
        try {
            OpenShiftUtils.getInstance().customResource(getCSVContext())
                .edit(TestConfiguration.openShiftNamespace(), TestConfiguration.getOperatorHubCSVName(), json.toMap());
            return true;
        } catch (Exception e) {
            log.error("Couldn't edit Apicurito CSV", e);
            return false;
        }
    }

    private static CustomResourceDefinitionContext getCSVContext() {
        return new CustomResourceDefinitionContext.Builder()
            .withGroup("operators.coreos.com")
            .withPlural("clusterserviceversions")
            .withScope("Namespaced")
            .withVersion("v1alpha1")
            .build();
    }
    public static CustomResourceDefinitionContext getSubscriptionContext() {
        return (new CustomResourceDefinitionContext.Builder())
        .withGroup("operators.coreos.com")
            .withPlural("subscriptions")
            .withScope("Namespaced")
            .withVersion("v1alpha1")
            .build();
    }

    private static CustomResourceDefinitionContext getSubscriptionCRDContext() {
        return new CustomResourceDefinitionContext.Builder()
            .withGroup("operators.coreos.com")
            .withPlural("subscriptions")
            .withScope("Namespaced")
            .withVersion("v1alpha1")
            .build();
    }

    public static JSONObject getSubscription() {
        CustomResourceDefinitionContext context = getSubscriptionCRDContext();
        JSONObject subs = new JSONObject(OpenShiftUtils.getInstance().customResource(context).list(TestConfiguration.openShiftNamespace()));
        JSONArray items = subs.getJSONArray("items");
        for (int i = 0; i < items.length(); i++) {
            if (items.getJSONObject(i).getJSONObject("spec").getString("name").equals(SUBSCRIPTION_NAME)) {
                return items.getJSONObject(i);
            }
        }
        return null;
    }

    public static boolean subscriptionExists() {
        return getSubscription() != null;
    }

    public static void reinstallApicurito() {
        deploy();
        waitForApicurito("component", 2, Component.SERVICE);
    }
}
