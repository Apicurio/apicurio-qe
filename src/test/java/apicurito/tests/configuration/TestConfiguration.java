package apicurito.tests.configuration;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

@Slf4j
public class TestConfiguration {

    public static final String OPENSHIFT_URL = "apicurito.config.openshift.url";
    public static final String OPENSHIFT_TOKEN = "apicurito.config.openshift.token";
    public static final String OPENSHIFT_NAMESPACE = "apicurito.config.openshift.namespace";
    public static final String OPENSHIFT_NAMESPACE_CLEANUP_AFTER = "apicurito.config.openshift.namespace.cleanup.after";
    public static final String OPENSHIFT_ROUTE_SUFFIX = "apicurito.config.openshift.route.suffix";
    public static final String OPENSHIFT_REINSTALL = "apicurito.config.openshift.reinstall";

    public static final String APICURITO_TEMPLATE_URL = "apicurito.config.template.url";
    public static final String APICURITO_IS_TEMPLATE_URL = "apicurito.config.inputstream.template.url";
    public static final String APICURITO_UI_BROWSER = "apicurito.config.ui.browser";
    public static final String APICURITO_UI_URL = "apicurito.config.ui.url";

    public static final String TESTSUITE_TIMEOUT = "apicurito.config.timeout";

    public static final String APP_ROOT = "apicurito.config.app.root";

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
        return get().readValue(APICURITO_TEMPLATE_URL, "https://raw.githubusercontent.com/jboss-fuse/application-templates/master/fuse-apicurito.yml");
    }

    public static String templateInputStreamUrl() {
        return get().readValue(APICURITO_IS_TEMPLATE_URL, "https://raw.githubusercontent.com/jboss-fuse/application-templates/master/fis-image-streams.json");
    }

    public static String openShiftRouteSuffix() {
        return get().readValue(OPENSHIFT_ROUTE_SUFFIX);
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

    private Properties defaultValues() {
        final Properties props = new Properties();

        props.setProperty(OPENSHIFT_URL, "");
        props.setProperty(OPENSHIFT_TOKEN, "");

        return props;
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
