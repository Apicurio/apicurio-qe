@apicuritoTests
@notUi
@customRouteHostnamesTest
Feature: Check custom route names

  Background:
    Given clean openshift after operatorhub test
    And delete running instance of apicurito

  Scenario: Check that custom route hostnames are set correctly
    When generate CR with "my-apicurito" service and "my-generator" generator route hostnames based on Openshift URL
    And deploy operator with custom route hostnames from operatorhub

    Then check that "OPERATOR" has "1" pods
    And check that "GENERATOR" has "2" pods
    And check that "SERVICE" has "2" pods

    Then check that "SERVICE" route has correct hostname
    And check that "GENERATOR" route has correct hostname

    #Run simple smoke test
    When delete file "tmp/download/openapi-spec.json"
    And delete file "tmp/download/openapi-spec.yaml"
    And log into apicurito using custom UI route hostname
    And import API "src/test/resources/preparedAPIs/basic.json"
    And save API as "json" and close editor
    And import API "tmp/download/openapi-spec.json"
    Then check that API name is "testAPI"

    When clean openshift after operatorhub test
    And delete running instance of apicurito
    Then reinstall apicurito