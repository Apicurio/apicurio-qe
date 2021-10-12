@apicuritoTests
@operatorhub
Feature: OperatorHub installation test

  Background:
    Given clean openshift after operatorhub test
    And delete running instance of apicurito
    And setup operatorhub on cluster

  @operatorhubInstall
  Scenario: test operatorhub installation
    When deploy operator from operatorhub
    Then check that apicurito operator is deployed and in running state

    When deploy "first" custom resource
    Then check that apicurito "image" is "registry.redhat.io/fuse7/fuse-apicurito"
    And check that apicurito "operator" is "registry.redhat.io/fuse7/fuse-apicurito-rhel8-operator"
    And check that "GENERATOR" has "2" pods
    And check that "SERVICE" has "2" pods
    And check that name and image of operator in operatorhub are correct

    #Run simple smoke test
    When delete API "tmp/download/openapi-spec.json"
    And delete API "tmp/download/openapi-spec.yaml"
    And log into apicurito
    And import API "src/test/resources/preparedAPIs/basic.json"
    And save API as "json" and close editor
    And import API "tmp/download/openapi-spec.json"
    Then check that API name is "testAPI"

    When clean openshift after operatorhub test
    And delete running instance of apicurito
    Then reinstall apicurito

  @operatorhubUpgrade
  Scenario: test operatorhub upgrade
    When deploy older operator from operatorhub
    Then check that apicurito operator is deployed and in running state

    When deploy "first" custom resource
    Then check that all deployed pods have older version

    When upgrade operator via operatorhub
    Then check that all deployed pods have newer version

    #Run simple smoke test
    When delete API "tmp/download/openapi-spec.json"
    And delete API "tmp/download/openapi-spec.yaml"
    And log into apicurito
    And import API "src/test/resources/preparedAPIs/basic.json"
    And save API as "json" and close editor
    And import API "tmp/download/openapi-spec.json"
    Then check that API name is "testAPI"

    When clean openshift after operatorhub test
    And delete running instance of apicurito
    Then reinstall apicurito
