@apicuritoTests
@operatorhubUpgrade
Feature: OperatorHub upgrade test

  Background:
    Given clean openshift after operatorhub test
    And delete running instance of apicurito

  Scenario: test operatorhub upgrade
    When deploy older operator from operatorhub
    Then check that all deployed pods have older version

    When upgrade operator via operatorhub
    Then check that all deployed pods have newer version

    #Run simple smoke test
    When delete file "tmp/download/openapi-spec.json"
    And delete file "tmp/download/openapi-spec.yaml"
    And log into apicurito
    And import API "src/test/resources/preparedAPIs/basic.json"
    And save API as "json" and close editor
    And import API "tmp/download/openapi-spec.json"
    Then check that API name is "testAPI"

    When clean openshift after operatorhub test
    And delete running instance of apicurito
    Then reinstall apicurito
