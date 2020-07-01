@apicuritoTests
@update
@updateTest
Feature: Update scenario test

  @skip_scenario_template
  @testCustomeResourceUpdate
  Scenario: test update custom resource
    When deploy another custom resource
    Then check that apicurito has 6 pods

  @testOperatorhub
  Scenario: test operatorhub
    When deploy operator from operatorhub
    Then check that apicurito operator is deployed and in running state


  #TODO in 7.7.0.ER1 when another operator will be available
  #@testUpdateOperator
  #Scenario: test update operator
  #  When deploy another operator
  #  Then check that apicurito "operator" is "mmajerni/apicurito-operator-update:latest"
  #  And check that apicurito "image" is "mmajerni/apicurito-update:latest"

