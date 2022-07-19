@apicuritoTests
@generatorTests
@smokeTests

Feature: Apicurito Generator tests

  Background:
    Given delete file "tmp/download/openapi-spec.json"
    And delete file "tmp/download/openapi-spec.yaml"
    And delete file "tmp/download/camel-project.zip"
    And delete file "tmp/download/camel-project"
    And log into apicurito

  Scenario: Generate Fuse Camel Project and run it
    When import API "src/test/resources/preparedAPIs/openapi-spec.json"
    And sleep for 10 seconds

    Then generate and export fuse camel project
    And unzip and run generated fuse camel project
    And check that project is generated correctly
