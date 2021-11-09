@apicuritoTests
@notUi
@meteringLabels
Feature: Check metering labels

  Background:
    Given clean openshift after operatorhub test
    And delete running instance of apicurito
    And setup operatorhub on cluster

  Scenario: Check that metering labels have correct values
    When deploy operator from operatorhub
    Then check that apicurito operator is deployed and in running state

    When deploy "first" custom resource
    Then check that "OPERATOR" has "1" pods
    And check that "GENERATOR" has "2" pods
    And check that "SERVICE" has "2" pods
    Then check that metering labels have correct values for "OPERATOR"
    And check that metering labels have correct values for "SERVICE"
    And check that metering labels have correct values for "GENERATOR"

    When clean openshift after operatorhub test
    And delete running instance of apicurito
    Then reinstall apicurito
