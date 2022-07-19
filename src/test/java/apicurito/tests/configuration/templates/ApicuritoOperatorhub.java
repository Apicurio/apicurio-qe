package apicurito.tests.configuration.templates;

import io.syndesis.qe.marketplace.manifests.Bundle;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

import apicurito.tests.configuration.Component;
import apicurito.tests.configuration.ReleaseSpecificParameters;
import apicurito.tests.configuration.TestConfiguration;
import apicurito.tests.steps.ConfigurationOCPSteps;
import apicurito.tests.utils.openshift.OpenShiftUtils;
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ApicuritoOperatorhub extends ApicuritoInstall {
    public static final String SUBSCRIPTION_NAME = "fuse-apicurito";

    public static void deploy(String updateChannel, String crName) {
        log.info("Deploying using Operatorhub");

        Bundle.createSubscription(ConfigurationOCPSteps.getOpenShiftService(), SUBSCRIPTION_NAME,
            updateChannel, "''",
            TestConfiguration.getApicuritoIibImage());

        OpenShiftUtils.xtf().waiters()
            .areExactlyNPodsReady(1, "name", "fuse-apicurito")
            .interval(TimeUnit.SECONDS, 2)
            .timeout(TimeUnit.MINUTES, 3)
            .waitFor();

        ConfigurationOCPSteps.deployCustomResource(crName);
        OpenShiftUtils.getInstance().addRoleToUser("view", TestConfiguration.openshiftUsername());
    }

    public static CustomResourceDefinitionContext getSubscriptionContext() {
        return (new CustomResourceDefinitionContext.Builder())
            .withGroup("operators.coreos.com")
            .withPlural("subscriptions")
            .withScope("Namespaced")
            .withVersion("v1alpha1")
            .build();
    }

    public static JSONObject getSubscription() {
        CustomResourceDefinitionContext context = getSubscriptionContext();
        JSONObject subs = new JSONObject(OpenShiftUtils.getInstance().customResource(context).list(TestConfiguration.openShiftNamespace()));
        JSONArray items = subs.getJSONArray("items");
        for (int i = 0; i < items.length(); i++) {
            if (items.getJSONObject(i).getJSONObject("spec").getString("name").equals(SUBSCRIPTION_NAME)) {
                return items.getJSONObject(i);
            }
        }
        return null;
    }

    public static void reinstallApicurito() {
        deploy(ReleaseSpecificParameters.APICURITO_CURRENT_UPDATE_CHANNEL, "current");
        waitForApicurito("component", 2, Component.SERVICE);
    }
}
