package apicurito.tests.configuration;

public class ReleaseSpecificParameters {
    public static final String APICURITO_IMAGE_VERSION = "1.10";
    public static final String APICURITO_CURRENT_VERSION = "7.10";
    public static final String APICURITO_PREVIOUS_VERSION = "7.9";
    public static final String APICURITO_TEMPLATE_URL = "https://raw.githubusercontent.com/jboss-fuse/application-templates/2.1.x.sb2.redhat-7-x/fuse-apicurito.yml";
    public static final String APICURITO_IS_TEMPLATE_URL = "https://raw.githubusercontent.com/jboss-fuse/application-templates/2.1.x.sb2.redhat-7-x/fis-image-streams.json";
    public static final String OLD_OPERATOR_URL = "quay.io/rh_integration/fuse-apicurito-rhel7-operator:1.9-20"; //GA image
//    public static final String OLD_OPERATOR_URL = "quay.io/rh_integration/fuse-apicurito-rhel8-operator:1.9-14"; //GA image
    public static final String APICURITO_OPERATOR_PREVIOUS_METADATA_URL = "registry-proxy.engineering.redhat.com/rh-osbs/fuse7-fuse-apicurito-operator-metadata:1.9-10";
//    public static final String APICURITO_OPERATOR_PREVIOUS_METADATA_URL ="quay.io/rh_integration/fuse-apicurito-rhel8-operator-metadata:1.9-17";

}
