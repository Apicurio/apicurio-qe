package apicurito.tests.configuration;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Properties;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TestConfiguration {

    public static final String OPENSHIFT_URL = "apicurito.config.openshift.url";
    public static final String OPENSHIFT_TOKEN = "apicurito.config.openshift.token";
    public static final String OPENSHIFT_NAMESPACE = "apicurito.config.openshift.namespace";
    public static final String OPENSHIFT_NAMESPACE_CLEANUP_AFTER = "apicurito.config.openshift.namespace.cleanup.after";
    public static final String OPENSHIFT_REINSTALL = "apicurito.config.openshift.reinstall";

    public static final String APICURITO_TEMPLATE_URL = "apicurito.config.template.url";
    public static final String APICURITO_IS_TEMPLATE_URL = "apicurito.config.inputstream.template.url";
    public static final String APICURITO_UI_BROWSER = "apicurito.config.ui.browser";
    public static final String APICURITO_UI_URL = "apicurito.config.ui.url";

    public static final String TESTSUITE_TIMEOUT = "apicurito.config.timeout";

    public static final String APP_ROOT = "apicurito.config.app.root";

    public static final String APICURITO_INSTALL_METHOD = "apicurito.config.install.method";

    public static final String APICURITO_OPERATOR_CRD_URL = "apicurito.config.operator.crd";
    public static final String APICURITO_OPERATOR_DEPLOYMENT_URL = "apicurito.config.operator.url";
    public static final String APICURITO_OPERATOR_SERVICE_URL = "apicurito.config.operator.service";
    public static final String APICURITO_OPERATOR_ROLE_URL = "apicurito.config.operator.role";
    public static final String APICURITO_OPERATOR_ROLE_BINDING_URL = "apicurito.config.operator.rolebinding";
    public static final String APICURITO_OPERATOR_CLUSTER_ROLE_URL = "apicurito.config.operator.cluster.role";
    public static final String APICURITO_OPERATOR_CLUSTER_ROLE_BINDING_URL = "apicurito.config.operator.cluster.rolebinding";
    public static final String APICURITO_OPERATOR_CR_URL = "apicurito.config.operator.cr";

    public static final String APICURITO_PULL_SECRET = "apicurito.config.pull.secret";

    public static final String APICURITO_OPERATOR_IMAGE_URL = "apicurito.config.operator.image.url";
    public static final String APICURITO_UI_IMAGE_URL = "apicurito.config.ui.image.url";
    public static final String APICURITO_GENERATOR_IMAGE_URL = "apicurito.config.generator.image.url";
    public static final String APICURITO_CATALOGSOURCE = "apicurito.config.catalogsource";

    public static final String APICURITO_UI_USERNAME = "apicurito.config.ui.username";
    public static final String APICURITO_UI_PASSWORD = "apicurito.config.ui.password";

    public static final String APICURIO_OPERATORS_DOWNSTREAM_COMMIT_HASH = "apicurito.config.commit.hash";

    private static final TestConfiguration INSTANCE = new TestConfiguration();

    private final Properties properties = new Properties();

    private TestConfiguration() {
        // first let's try product properties
        copyValues(fromPath("test.properties"), true);

        // then product properties
        copyValues(fromPath("../test.properties"));

        // then system variables
        copyValues(System.getProperties());

        // then environment variables
        // TODO: copyValues(fromEnvironment());

        // then defaults
        copyValues(defaultValues());
    }

    public static TestConfiguration get() {
        return INSTANCE;
    }

    public static String openShiftUrl() {
        return get().readValue(OPENSHIFT_URL);
    }

    public static String openShiftToken() {
        return get().readValue(OPENSHIFT_TOKEN);
    }

    public static String openShiftNamespace() {
        return get().readValue(OPENSHIFT_NAMESPACE, "apicurito");
    }

    public static String templateUrl() {
        return get()
            .readValue(APICURITO_TEMPLATE_URL,
                ReleaseSpecificParameters.APICURITO_TEMPLATE_URL);
    }

    public static String templateImageStreamUrl() {
        return get()
            .readValue(APICURITO_IS_TEMPLATE_URL, ReleaseSpecificParameters.APICURITO_IS_TEMPLATE_URL);
    }

    public static String apicuritoInstallMethod() {
        return get().readValue(APICURITO_INSTALL_METHOD);
    }

    public static String apicuritoOperatorCrdUrl() {
        return get().readValue(APICURITO_OPERATOR_CRD_URL);
    }

    public static String apicuritoOperatorCrUrl() {
        return get().readValue(APICURITO_OPERATOR_CR_URL);
    }

    public static String apicuritoOperatorDeploymentUrl() {
        return get().readValue(APICURITO_OPERATOR_DEPLOYMENT_URL);
    }

    public static String apicuritoOperatorImageUrl() {
        return get().readValue(APICURITO_OPERATOR_IMAGE_URL);
    }

    public static String apicuritoGeneratorImageUrl() {
        return get().readValue(APICURITO_GENERATOR_IMAGE_URL);
    }

    public static String apicuritoOperatorServiceUrl() {
        return get().readValue(APICURITO_OPERATOR_SERVICE_URL);
    }

    public static String apicuritoOperatorRoleUrl() {
        return get().readValue(APICURITO_OPERATOR_ROLE_URL);
    }

    public static String apicuritoOperatorRoleBindingUrl() {
        return get().readValue(APICURITO_OPERATOR_ROLE_BINDING_URL);
    }

    public static String apicuritoOperatorClusterRoleUrl() {
        return get().readValue(APICURITO_OPERATOR_CLUSTER_ROLE_URL);
    }

    public static String apicuritoOperatorClusterRoleBindingUrl() {
        return get().readValue(APICURITO_OPERATOR_CLUSTER_ROLE_BINDING_URL);
    }

    public static String apicuritoUiImageUrl() {
        return get().readValue(APICURITO_UI_IMAGE_URL);
    }

    public static int getConfigTimeout() {
        return Integer.parseInt(get().readValue(TESTSUITE_TIMEOUT, "300"));
    }

    public static String apicuritoBrowser() {
        return get().readValue(APICURITO_UI_BROWSER, "firefox");
    }

    public static boolean namespaceCleanupAfter() {
        return Boolean.parseBoolean(get().readValue(OPENSHIFT_NAMESPACE_CLEANUP_AFTER, "false"));
    }

    public static String apicuritoUrl() {
        return get().readValue(APICURITO_UI_URL);
    }

    public static String doReinstall() {
        return get().readValue(OPENSHIFT_REINSTALL, "true");
    }

    public static String getAppRoot() {
        return get().readValue(APP_ROOT, "app-root");
    }

    public static String openshiftUsername() {
        return get().readValue(APICURITO_UI_USERNAME);
    }

    public static String openshiftPassword() {
        return get().readValue(APICURITO_UI_PASSWORD);
    }

    public static String apicuritoPullSecret() {
        return get().readValue(APICURITO_PULL_SECRET);
    }

    public String getApicurioOperatorsDownstreamCommitHash() {
        return readValue(APICURIO_OPERATORS_DOWNSTREAM_COMMIT_HASH, "master");
    }

    public static String getApicuritoCatalogSource() {
        return get().readValue(APICURITO_CATALOGSOURCE, "apicurito-iib-images");
    }

    private Properties defaultValues() {
        final Properties props = new Properties();

        props.setProperty(OPENSHIFT_URL, "");
        props.setProperty(OPENSHIFT_TOKEN, "");

        if (props.getProperty(APICURITO_OPERATOR_CRD_URL) == null) {
            props.setProperty(APICURITO_OPERATOR_CRD_URL, String.format(
                "https://raw.githubusercontent.com/jboss-fuse/apicurio-operators/" +
                    getApicurioOperatorsDownstreamCommitHash() +
                    "/apicurito/config/crd/bases/apicur.io_apicuritoes.yaml"));
        }
        if (props.getProperty(APICURITO_OPERATOR_DEPLOYMENT_URL) == null) {
            downloadDeploymentTemplate();
            generateDeploymentFile();
            props.setProperty(APICURITO_OPERATOR_DEPLOYMENT_URL,
                String.format("src/test/resources/generatedFiles/deployment.gen.yaml"));
        }
        if (props.getProperty(APICURITO_OPERATOR_CR_URL) == null) {
            props.setProperty(APICURITO_OPERATOR_CR_URL, String.format(
                "https://raw.githubusercontent.com/jboss-fuse/apicurio-operators/"
                    + getApicurioOperatorsDownstreamCommitHash() +
                    "/apicurito/config/samples/apicur_v1_apicurito_cr.yaml"));
        }
        if (props.getProperty(APICURITO_OPERATOR_SERVICE_URL) == null) {
            props.setProperty(APICURITO_OPERATOR_SERVICE_URL,
                String
                    .format("https://raw.githubusercontent.com/jboss-fuse/apicurio-operators/" +
                        getApicurioOperatorsDownstreamCommitHash() +
                        "/apicurito/config/manager/service_account.yaml"));
        }
        if (props.getProperty(APICURITO_OPERATOR_ROLE_URL) == null) {
            props.setProperty(APICURITO_OPERATOR_ROLE_URL,
                String.format("https://raw.githubusercontent.com/jboss-fuse/apicurio-operators/" +
                    getApicurioOperatorsDownstreamCommitHash() + "/apicurito/config/rbac/role.yaml"));
        }
        if (props.getProperty(APICURITO_OPERATOR_ROLE_BINDING_URL) == null) {
            props.setProperty(APICURITO_OPERATOR_ROLE_BINDING_URL,
                String.format(
                    "https://raw.githubusercontent.com/jboss-fuse/apicurio-operators/" +
                        getApicurioOperatorsDownstreamCommitHash() + "/apicurito/config/rbac/role_binding.yaml"));
        }
        if (props.getProperty(APICURITO_OPERATOR_CLUSTER_ROLE_URL) == null) {
            props.setProperty(APICURITO_OPERATOR_CLUSTER_ROLE_URL,
                String.format(
                    "https://raw.githubusercontent.com/jboss-fuse/apicurio-operators/" +
                        getApicurioOperatorsDownstreamCommitHash() + "/apicurito/config/rbac/cluster_role.yaml"));
        }
        if (props.getProperty(APICURITO_OPERATOR_CLUSTER_ROLE_BINDING_URL) == null) {
            props.setProperty(APICURITO_OPERATOR_CLUSTER_ROLE_BINDING_URL,
                String.format("src/test/resources/generatedFiles/cluster_role_binding.yaml"));
        }

        props.setProperty(APICURITO_INSTALL_METHOD, "operatorhub");

        // Copy syndesis properties to their xtf counterparts - used by binary oc client
        if (System.getProperty("xtf.openshift.url") == null) {
            System.setProperty("xtf.openshift.url", properties.getProperty(OPENSHIFT_URL));
            System.setProperty("xtf.openshift.master.username", properties.getProperty(APICURITO_UI_USERNAME));
            System.setProperty("xtf.openshift.master.password", properties.getProperty(APICURITO_UI_PASSWORD));

            System.setProperty("xtf.openshift.namespace", properties.getProperty(OPENSHIFT_NAMESPACE));

            // Set oc version - this version of the client will be used as the binary client
            System.setProperty("xtf.openshift.version", "4.3");
        }
        return props;
    }

    private void generateDeploymentFile() {
        String[] imageAndTag = this.properties.getProperty(APICURITO_OPERATOR_IMAGE_URL, null).split(":");
        String image = imageAndTag[0];
        String tag = imageAndTag[1];
        ProcessBuilder pb = new ProcessBuilder("make");
        Map<String, String> env = pb.environment();
        env.put("IMAGE", image);
        env.put("TAG", tag);
        env.put("VERSION", ReleaseSpecificParameters.APICURITO_CURRENT_VERSION);

        pb.directory(new File("src/test/resources/generatedFiles"));
        try {
            Process p = pb.start();
            p.waitFor();
        } catch (Exception e) {
            log.debug("Exception", e);
        }
    }

    private void downloadDeploymentTemplate() {
        try {
            URL url = new URL("https://raw.githubusercontent.com/jboss-fuse/apicurio-operators/" +
                getApicurioOperatorsDownstreamCommitHash() + "/apicurito/config/manager/deployment.tmpl");
            File file = new File("src/test/resources/generatedFiles/deployment.tmpl");
            InputStream input = url.openStream();
            FileUtils.copyInputStreamToFile(input, file);
            input.close();
        } catch (Exception e) {
            log.debug("Exception", e);
        }
    }

    public String readValue(final String key) {
        return readValue(key, null);
    }

    public String readValue(final String key, final String defaultValue) {
        return this.properties.getProperty(key, defaultValue);
    }

    private Properties fromPath(final String path) {
        final Properties props = new Properties();

        final Path propsPath = Paths.get(path)
            .toAbsolutePath();
        if (Files.isReadable(propsPath)) {
            try (InputStream is = Files.newInputStream(propsPath)) {
                props.load(is);
            } catch (final IOException ex) {
                log.warn("Unable to read properties from '{}'", propsPath);
                log.debug("Exception", ex);
            }
        }

        return props;
    }

    private void copyValues(final Properties source) {
        copyValues(source, false);
    }

    private void copyValues(final Properties source, final boolean overwrite) {
        source.stringPropertyNames().stream()
            .filter(key -> overwrite || !this.properties.containsKey(key))
            .forEach(key -> this.properties.setProperty(key, source.getProperty(key)));
    }

    public static void printDivider(String label) {
        log.info("__________________________________________________________________");
        log.info("##################################################################");
        log.info("### -----> " + label);
        log.info("##################################################################");
    }
}
