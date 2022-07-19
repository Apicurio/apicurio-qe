# Apicurito tests

This repository contains tests for Apicurito. It tests both Apicurito UI image and Apicurito Operator image.
It supports both installation methods:
- Template installation (tests only UI image)
- Operator installation (tests both UI and Operator image)

There is also one test which tests installation from OperatorHub. It requires an extra configuration.

## Test.properties file

These tests require `test.properties` file to work. Create it in top folder with following parameters:

- Require parameters:
```
apicurito.config.openshift.url=
apicurito.config.ui.url=
apicurito.config.ui.username=
apicurito.config.ui.password=
```

- Optional parameters:
```
apicurito.config.openshift.namespace=
apicurito.config.openshift.namespace.cleanup.after=
apicurito.config.openshift.reinstall=
apicurito.config.ui.browser=
apicurito.config.install.method=

apicurito.config.operator.image.url=
apicurito.config.commit.hash=
apicurito.config.ui.image.url=
apicurito.config.generator.image.url=
apicurito.config.iib.image=

apicurito.config.pull.secret=
```

#### Description of parameters
**apicurito.config.openshift.url** must contains url to OCP API i.e.
- https://api.crc.testing:6443
- https://192.168.42.155:8443
- https://api.my.openshift.com:6443


**apicurito.config.ui.url** must contains url to Apicurito route in format:
`https://${APICURITO_POD_NAME}-${APICURITO_NAMESPACE}.apps.${OPENSHIFT_URL}`

**INFO**: For various OCP instances may route differs.
- Examples:
  - https://apicurito-service-ui-apicurito.apps.my.openshift.com
  - https://apicurito-service-ui-apicurito.apps-crc.testing
  - https://apicurito-service-ui-apicurito.192.168.42.155.nip.io
- Note:
  - ${APICURITO_POD_NAME} depends on metadata name in CR.
  - ${APICURITO_NAMESPACE} is **apicurito** by default but can be changed

**apicurito.config.ui.username** must contain user name of user with admin rights

**apicurito.config.ui.password** must contain the password for user defined above

**apicurito.config.openshift.namespace** can override namespace where apicurito will be installed. Default value is apicurito.

**apicurito.config.openshift.namespace.cleanup.after** if set to true it will delete Apicurito and clean openshift namespace. Default value is false.

**apicurito.config.openshift.reinstall** if set to true reinstall Apicurito before tests, otherwise it will keep already installed Apicurito. Default value is true.

**apicurito.config.ui.browser** set to change browser for UI tests.
- Supported options:
[ firefox | chrome ]. Default is firefox.

**apicurito.config.install.method** represents Apicurito installation type.
- Supported options:
[ operatorhub | operator | template ]. Default is operatorhub.

**apicurito.config.pull.secret** secret is needed for the following images which are stored at quay.io.

**apicurito.config.operator.image.url** can override the default operator image which is defined in the operator.yaml file.
- Examples:
  - quay.io/redhat/apicurito-operator:1.8-x
  - docker.io/redhat/apicurito-operator:1.8-x

**apicurito.config.commit.hash** specifies the version of the https://github.com/jboss-fuse/apicurio-operators repository.
- Needed only for Operator installation.
- Default is master.
- You can find the commit hash in the operator image build log.
 (e.g. http://download.eng.bos.redhat.com/brewroot/packages/fuse-apicurito-rhel-8-operator-container/1.11/21/data/logs/orchestrator.log)
**apicurito.config.ui.image.url** can override the default ui image which is defined in the operator or in operator.yaml as RELATED_IMAGE_UI.
- Examples:
  - quay.io/redhat/apicurito-ui:1.8-x
  - docker.io/redhat/apicurito-ui:1.8-x
- **Note**: This parameter is required if Apicurito is installed via template installation.

**apicurito.config.generator.image.url** can override the default generator image which is defined in the operator or in operator.yaml as RELATED_IMAGE_GENERATOR.
- Examples:
  - quay.io/redhat/apicurito-generator:1.8-x
  - docker.io/redhat/apicurito-generator:1.8-x

**apicurito.config.iib.image** is a name of the IIB image. Needed only for OperatorHub installation.
- Default value: apicurito-iib-images

## How to run tests

- From CLI:
  - From top folder run `mvn clean test`

- From IntelliJ IDEA:
  - Run **TestRunner** class as JUnit tests

#### Run subset of the tests

If you want to run just subset of tests add option `-Dcucumber.filter.tags="@tag1 or @tag2"`
- Examples:
  - CLI: `mvn clean test -Dcucumber.filter.tags="@smokeTests or @pathsTests"`
  - IDEA: Set VM options to: `-ea -Dcucumber.filter.tags="@smokeTests or @pathsTests"`
