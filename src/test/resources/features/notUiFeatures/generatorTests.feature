@apicuritoTests
@generatorTests
@operatorhub

Feature: Apicurito Generator tests

  Background:
    Given clean openshift after operatorhub test
    And delete running instance of apicurito
    And setup operatorhub on cluster

  Scenario: Generate Fuse Camel Project and run it
    When deploy operator from operatorhub
    Then check that apicurito operator is deployed and in running state
    And deploy "first" custom resource

    Then log into apicurito
    And import API "src/test/resources/preparedAPIs/openapi-spec.json"

    Then generate and export fuse camel project
    And unzip and run generated fuse camel project
    And check that project is generated correctly
