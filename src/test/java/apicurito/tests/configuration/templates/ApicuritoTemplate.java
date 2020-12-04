package apicurito.tests.configuration.templates;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import apicurito.tests.configuration.Component;
import apicurito.tests.configuration.TestConfiguration;
import apicurito.tests.utils.HttpUtils;
import apicurito.tests.utils.openshift.OpenShiftUtils;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.KubernetesList;
import io.fabric8.openshift.api.model.Template;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ApicuritoTemplate extends ApicuritoInstall {

    private static Template getTemplate() {
        try (InputStream is = new URL(TestConfiguration.templateUrl()).openStream()) {
            return OpenShiftUtils.getInstance().templates().load(is).get();
        } catch (IOException ex) {
            throw new IllegalArgumentException("Unable to read apicurito template ", ex);
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
                if ("apicurito-ui".equals(imagestreams.getJSONObject(i).getJSONObject("metadata").getString("name"))) {
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

    private static void deploy() {
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

    public static void reinstallApicurito() {
        setImageStreams();
        deploy();
        waitForApicurito("component", 1, Component.UI);
    }
}
