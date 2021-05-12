@apicuritoTests
@operatorhub
Feature: OperatorHub installation test

  Scenario: test operatorhub installation
    Given clean openshift after operatorhub test
    When delete running instance of apicurito
    Given setup operatorhub on cluster
    And deploy operator from operatorhub
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

#    When clean openshift after operatorhub test
#    And delete running instance of apicurito
#    Then reinstall apicurito

  @operatorhub-upgrade
  Scenario: test operatorhub upgrade
    Given clean openshift after operatorhub test
    Given setup operatorhub on cluster
    When delete running instance of apicurito
    And upgrade operator via operatorhub
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

#    When clean openshift after operatorhub test
#    And delete running instance of apicurito
#    Then reinstall apicurito
