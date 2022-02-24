@apicuritoTests
@generatorTests
@operatorhub

Feature: Apicurito Generator tests

  Background:
    Given clean openshift after operatorhub test
    And delete running instance of apicurito
    And setup operatorhub on cluster
    And delete file "tmp/download/camel-project.zip"
    And delete file "tmp/download/camel-project"

  Scenario: Generate Fuse Camel Project and run it
    When deploy operator from operatorhub
    Then check that apicurito operator is deployed and in running state
    And deploy "first" custom resource

    Then log into apicurito
    And import API "src/test/resources/preparedAPIs/openapi-spec.json"
    And sleep for 5 seconds

    Then generate and export fuse camel project
    And unzip and run generated fuse camel project
    And check that project is generated correctly

    When clean openshift after operatorhub test
    And delete running instance of apicurito
    Then reinstall apicurito
