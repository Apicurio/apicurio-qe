@apicuritoTests
@check-apicurito-metering-labels
@notUi
  Feature: Check metering labels
    Scenario: Check that metering labels have correct values
      When check that "OPERATOR" has "1" pods
      And check that "GENERATOR" has "3" pods
      And check that "SERVICE" has "3" pods
      Then check that metering labels have correct values for "OPERATOR"
      And check that metering labels have correct values for "SERVICE"
      And check that metering labels have correct values for "GENERATOR"
