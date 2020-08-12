@apicuritoTests
@ui
@basicTests
Feature: Basic tests

  Background:
    Given delete API "tmp/download/openapi-spec.json"
    And delete API "tmp/download/openapi-spec.yaml"
    And log into apicurito

  @exportImportJson
  Scenario: export and import as json
    When import API "src/test/resources/preparedAPIs/basic.json"
    And save API as "json" and close editor
    And import API "tmp/download/openapi-spec.json"
    Then check that API name is "testAPI"

  @exportImportYaml
  Scenario: export and import as yaml
    When import API "src/test/resources/preparedAPIs/basic.yaml"
    And save API as "yaml" and close editor
    And import API "tmp/download/openapi-spec.yaml"
    Then check that API name is "testAPI"

  @createPathByLink
  Scenario: create path by link
    When create a new API version "2"
    And create a new path with link
      | MyPathByLink | true |

    And save API as "json" and close editor
    And import API "tmp/download/openapi-spec.json"
    Then check that path "/MyPathByLink" "is" created

  @createPathByPlus
  Scenario: create path by plus
    When create a new API version "2"
    And create a new path with link
      | MyPathByPlus | false |

    And save API as "json" and close editor
    And import API "tmp/download/openapi-spec.json"
    Then check that path "/MyPathByPlus" "is" created

  @createDataTypeByLink
  Scenario: create data type by link
    When create a new API version "2"
    And create a new data type by link
      | NewDataLink | best data type ever |  | false | true |

    And save API as "json" and close editor
    And import API "tmp/download/openapi-spec.json"
    Then check that data type "NewDataLink" "is" created

  @createDataTypeByPlus
  Scenario: create a new data type by plus
    When create a new API version "2"
    And create a new data type by link
      | NewDataPlus | best data type ever |  | false | false |

    And save API as "json" and close editor
    And import API "tmp/download/openapi-spec.json"
    Then check that data type "NewDataPlus" "is" created

  @createResponseByPlus
  Scenario: create path by plus
    When create a new API version "2"
    And create a new response by link
      | response1 | response desc | false |

    And save API as "json" and close editor
    And import API "tmp/download/openapi-spec.json"
    Then check that response "response1" "is" created

  @createResponseByLink
  Scenario: create path by link
    When create a new API version "2"
    And create a new response by link
      | response1 | response desc | true |

    And save API as "json" and close editor
    And import API "tmp/download/openapi-spec.json"
    Then check that response "response1" "is" created

  @createPutOperation
  Scenario: create PUT operation
    When import API "src/test/resources/preparedAPIs/basic.json"
    And select path "/clearPath"
    And create new "PUT" operation
    And save API as "json" and close editor
    And import API "tmp/download/openapi-spec.json"
    Then check that operation "PUT" is created for path "/clearPath"

  @createPostOperation
  Scenario: create POST operation
    When import API "src/test/resources/preparedAPIs/basic.json"
    And select path "/clearPath"
    And create new "POST" operation
    And save API as "json" and close editor
    And import API "tmp/download/openapi-spec.json"
    Then check that operation "POST" is created for path "/clearPath"

  @createDeleteOperation
  Scenario: create PUT operation
    When import API "src/test/resources/preparedAPIs/basic.json"
    And select path "/clearPath"
    And create new "DELETE" operation
    And save API as "json" and close editor
    And import API "tmp/download/openapi-spec.json"
    Then check that operation "DELETE" is created for path "/clearPath"

  @createOptionsOperation
  Scenario: create OPTIONS operation
    When import API "src/test/resources/preparedAPIs/basic.json"
    And select path "/clearPath"
    And create new "OPTIONS" operation
    And save API as "json" and close editor
    And import API "tmp/download/openapi-spec.json"
    Then check that operation "OPTIONS" is created for path "/clearPath"

  @createHeadOperation
  Scenario: create HEAD operation
    When import API "src/test/resources/preparedAPIs/basic.json"
    And select path "/clearPath"
    And create new "HEAD" operation
    And save API as "json" and close editor
    And import API "tmp/download/openapi-spec.json"
    Then check that operation "HEAD" is created for path "/clearPath"

  @createPatchOperation
  Scenario: create PATCH operation
    When import API "src/test/resources/preparedAPIs/basic.json"
    And select path "/clearPath"
    And create new "PATCH" operation
    And save API as "json" and close editor
    And import API "tmp/download/openapi-spec.json"
    Then check that operation "PATCH" is created for path "/clearPath"

  @createTraceOperation
  Scenario: create TRACE operation
    When import API "src/test/resources/preparedAPIs/basicV3.yaml"
    And select path "/clearPath"
    And create new "TRACE" operation
    And save API as "json" and close editor
    And import API "tmp/download/openapi-spec.json"
    Then check that operation "TRACE" is created for path "/clearPath"

  @createAllOperations
  Scenario: create all operations
    When import API "src/test/resources/preparedAPIs/basicV3.yaml"
    And select path "/clearPath"

    And create new "GET" operation
    And create new "PUT" operation
    And create new "POST" operation
    And create new "DELETE" operation
    And create new "OPTIONS" operation
    And create new "HEAD" operation
    And create new "PATCH" operation
    And create new "TRACE" operation

    And save API as "json" and close editor
    And import API "tmp/download/openapi-spec.json"

    Then check that operation "GET" is created for path "/clearPath"
    And check that operation "PUT" is created for path "/clearPath"
    And check that operation "POST" is created for path "/clearPath"
    And check that operation "DELETE" is created for path "/clearPath"
    And check that operation "OPTIONS" is created for path "/clearPath"
    And check that operation "HEAD" is created for path "/clearPath"
    And check that operation "PATCH" is created for path "/clearPath"
    And check that operation "TRACE" is created for path "/clearPath"

  @convertv2tov3
  Scenario: convert OpenAPIv2 to OpenAPIv3 and check it
    When import API "src/test/resources/preparedAPIs/basic.json"
    And convert OpenAPI two to OpenAPI three

    And create a server on "main page" page
      | http://{domain}.example.com/api/1 | server desc | false |

    Then check that server was created on "main page" page
      | http://{domain}.example.com/api/1 | server desc |

    And check that path "/clearpath" "is" created
    And check that path "/operations" "is" created
    And check that data type "clearDataType" "is" created
    And check that operation "GET" is created for path "/operations"
    And check that operation "HEAD" is created for path "/operations"
