@apicuritoTests
@operatorhub
Feature: OperatorHub installation test

  Scenario: test operatorhub installation
    When delete running instance of apicurito
    And deploy operator from operatorhub
    Then check that apicurito operator is deployed and in running state

    When deploy "first" custom resource
    Then check that apicurito "image" is "registry.redhat.io/fuse7/fuse-apicurito"
    And check that apicurito "operator" is "registry.redhat.io/fuse7/fuse-apicurito-rhel7-operator"
    And check that "apicurito-service-generator" has 4 pods
    And check that "apicurito-service-ui" has 4 pods

    When clean openshift after operatorhub test
    And delete running instance of apicurito
    Then reinstall apicurito
