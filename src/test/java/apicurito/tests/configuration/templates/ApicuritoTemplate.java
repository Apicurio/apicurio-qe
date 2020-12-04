package apicurito.tests.configuration.templates;

import static org.assertj.core.api.Assertions.fail;

import org.json.JSONArray;
import org.json.JSONObject;
import org.yaml.snakeyaml.Yaml;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import apicurito.tests.configuration.Component;
import apicurito.tests.configuration.TestConfiguration;
import apicurito.tests.utils.HttpUtils;
import apicurito.tests.utils.openshift.OpenShiftUtils;
import apicurito.tests.utils.slenide.ConfigurationOCPUtils;
import cz.xtf.core.waiting.WaiterException;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.KubernetesList;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.ReplicaSet;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.openshift.api.model.Template;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ApicuritoTemplate {

    public static Template getTemplate() {
        try (InputStream is = new URL(TestConfiguration.templateUrl()).openStream()) {
            return OpenShiftUtils.getInstance().templates().load(is).get();
        } catch (IOException ex) {
            throw new IllegalArgumentException("Unable to read apicurito template ", ex);
        }
    }

    /**
     * Loads the default deployment for operator (if custom deployment wasn't specified)
     * and updates it so it contains specified custom operator image
     * <p>
     * This method assumes that operator image has been specified in test.properties!
     *
     * @return Deployment containing the correct image
     */
    public static Deployment getUpdatedOperatorDeployment() {
        try (InputStream is = new URL(TestConfiguration.apicuritoOperatorDeploymentUrl()).openStream()) {
            Deployment deployment = OpenShiftUtils.getInstance().apps().deployments().load(is).get();
            // containers should contain only one object - get(0)
            deployment.getSpec().getTemplate().getSpec().getContainers().get(0).setImage(TestConfiguration.apicuritoOperatorImageUrl());
            return deployment;
        } catch (IOException e) {
            throw new IllegalArgumentException("Unable to read apicurito operator deployment", e);
        }
    }

    /**
     * Apply image stream
     * If Apicurito UI image is set in properties, add it with the TestConfiguration.APICURITO_IMAGE_VERSION tag
     */
    public static void setImageStreams() {
        TestConfiguration.printDivider("Setting up image streams");
        log.info("Deploying image stream " + TestConfiguration.templateImageStreamUrl());

        // Fishing out imagestream for apicurito-ui
        Optional<JSONObject> imageStream = Optional.empty();
        boolean error = false;
        try {
            JSONArray imagestreams =
                new JSONObject(HttpUtils.doGetRequest(TestConfiguration.templateImageStreamUrl()).body().string()).getJSONArray("items");
                for (int i = 0; i < imagestreams.length(); i++) {
                    if ("apicurito-ui".equals(imagestreams.getJSONObject(i).getJSONObject("metadata").getString("name"))){
                        imageStream = Optional.of(imagestreams.getJSONObject(i));
                        break;
                    }
                }
        } catch (IOException ex) {
            log.error("Couldn't fetch imagestream, applying whole url to specified ocp");
            error = true;
        }

        if (imageStream.isPresent()) {
            // create temp file with the is
            try {
                File imageStreamFile = File.createTempFile("imagestream", ".json");
                imageStreamFile.deleteOnExit();
                final BufferedWriter bw = new BufferedWriter(new FileWriter(imageStreamFile));
                bw.write(imageStream.get().toString());
                bw.close();
                OpenShiftUtils.binary().execute(
                    "apply",
                    "-n", TestConfiguration.openShiftNamespace(),
                    "-f", imageStreamFile.getPath()
                );
            } catch (IOException ex) {
                log.error("Couldn't create imagestream file, using original list of imagestreams");
                error = true;
            }
        }

        if (!imageStream.isPresent() || error) {
            final String output = OpenShiftUtils.binary().execute(
                "apply",
                "-n", TestConfiguration.openShiftNamespace(),
                "-f", TestConfiguration.templateImageStreamUrl()
            );
        }

        if (TestConfiguration.apicuritoUiImageUrl() != null) {
            log.info("UI image specified, updating image stream with {}", TestConfiguration.apicuritoUiImageUrl());
            OpenShiftUtils.binary().execute(
                "tag",
                TestConfiguration.apicuritoUiImageUrl(),
                "apicurito-ui:" + TestConfiguration.APICURITO_IMAGE_VERSION
            );
        }
    }

    public static void deploy() {
        if (TestConfiguration.useOperator()) {
            deployUsingGoOperator();
        } else {
            deployUsingTemplate();
        }
    }

    public static void deployUsingTemplate() {
        TestConfiguration.printDivider("Deploying using template");

        // get the template
        Template template = getTemplate();
        // set params
        Map<String, String> templateParams = new HashMap<>();
        templateParams.put("ROUTE_HOSTNAME", TestConfiguration.openShiftNamespace() + "." + TestConfiguration.openShiftRouteSuffix());
        log.info("Deploying on address: https://" + TestConfiguration.openShiftNamespace() + "." + TestConfiguration.openShiftRouteSuffix());
        templateParams.put("OPENSHIFT_MASTER", TestConfiguration.openShiftUrl());
        templateParams.put("OPENSHIFT_PROJECT", TestConfiguration.openShiftNamespace());
        templateParams.put("IMAGE_STREAM_NAMESPACE", TestConfiguration.openShiftNamespace());

        if (TestConfiguration.apicuritoUiImageUrl() != null) {
            templateParams.put("APP_VERSION", TestConfiguration.APICURITO_IMAGE_VERSION);

            // add apicurito-pull-secret to default SA so the pod can pull the image with provided pull secret
            OpenShiftUtils.addImagePullSecretToServiceAccount("default", "apicurito-pull-secret");
        }

        // process & create
        KubernetesList processedTemplate = OpenShiftUtils.getInstance().recreateAndProcessTemplate(template, templateParams);
        for (HasMetadata hasMetadata : processedTemplate.getItems()) {
            OpenShiftUtils.getInstance().createResources(hasMetadata);
        }
    }

    private static void deployUsingGoOperator() {
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

    public static String getOperatorImage() {
        try {
            String deploymentConfig = HttpUtils.readFileFromURL(new URL(TestConfiguration.apicuritoOperatorDeploymentUrl()));
            Map<String, Object> deployment = new Yaml().load(deploymentConfig);
            return ((Map<String, String>) ((Map<String, List<Map>>) ((Map<String, Map>) deployment.get("spec")).get("template").get("spec")).get("containers").get(0)).get("image");
        } catch (MalformedURLException e) {
            log.error("Proper URL was not supplied", e);
            return null;
        }
    }

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

    public static void reinstallApicurito(){
        setImageStreams();
        OpenShiftUtils.createPullSecret();
        deploy();
        if (TestConfiguration.useOperator()) {
            waitForApicurito("component", 6, Component.SERVICE);
        } else {
            waitForApicurito("component", 1, Component.UI);
        }
    }
}
