@apicuritoTests
@operatorhub
@operatorhubSmokeTests
@notUi

Feature: OperatorHub smoke tests

  Scenario: test operatorhub installation
    When check that apicurito "image" is "registry.redhat.io/fuse7/fuse-apicurito"
    Then check that apicurito "operator" is "registry.redhat.io/fuse7/fuse-apicurito-rhel8-operator"
    And check that name and image of operator in operatorhub are correct
