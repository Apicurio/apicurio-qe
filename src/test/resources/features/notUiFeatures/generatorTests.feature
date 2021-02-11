@apicuritoTests
@ui
@generatorTests
Feature: Apicurito Generator tests

  Background:
    Given log into apicurito
    And import API "src/test/resources/preparedAPIs/openapi-spec.json"

  Scenario: Generate Fuse Camel Project and run it
    When generate and export fuse camel project
    And unzip and run generated fuse camel project
    Then check that project is generated correctly
