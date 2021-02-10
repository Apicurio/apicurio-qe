@apicuritoTests
@generatorTests
@notUi
Feature: Apicurito Generator tests

  Scenario: Deploy clean Apicurito
    When delete running instance of apicurito
    And deploy operator from operatorhub
    Then check that apicurito operator is deployed and in running state

    When deploy "first" custom resource
    Then check that apicurito "image" is "registry.redhat.io/fuse7/fuse-apicurito"
    And check that apicurito "operator" is "registry.redhat.io/fuse7/fuse-apicurito-rhel7-operator"
    And check that "GENERATOR" has "2" pods
    And check that "SERVICE" has "2" pods
    And check that name and image of operator in operatorhub are correct

  # Run smoke tests
    When log into apicurito
    And import API "src/test/resources/preparedAPIs/basic.json"
    And generate and export fuse camel project
    And unzip and run generated fuse camel project
    Then check that project is generated correctly

    When clean openshift after operatorhub test
    Then delete running instance of apicurito
