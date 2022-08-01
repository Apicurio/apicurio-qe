@apicuritoTests
@notUi
@meteringLabels
Feature: Check metering labels

  Scenario: Check that metering labels have correct values
    When check that "OPERATOR" has "1" pods
    Then check that "GENERATOR" has "2" pods
    And check that "SERVICE" has "2" pods

    Then check that metering labels have correct values for "OPERATOR"
    And check that metering labels have correct values for "SERVICE"
    And check that metering labels have correct values for "GENERATOR"

