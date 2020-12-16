@apicuritoTests
@ui
@pathsTests
Feature: Path tests

  Background:
    Given delete API "tmp/download/openapi-spec.json"
    And log into apicurito

  @setOperationSummary
  Scenario: set operation summary
    When import API "src/test/resources/preparedAPIs/basic.json"
    And select path "/operations"
    And select operation "GET"
    And set operation summary "MySummary"

    Then save API as "json" and close editor
    When import API "tmp/download/openapi-spec.json"

    And select path "/operations"
    And select operation "GET"
    Then check that operation summary is "MySummary"

  @sethOperationID
  Scenario: set operation id
    When import API "src/test/resources/preparedAPIs/basic.json"
    And select path "/operations"
    And select operation "GET"
    And set operation id "MyOperationID"

    Then save API as "json" and close editor
    When import API "tmp/download/openapi-spec.json"

    And select path "/operations"
    And select operation "GET"
    Then check that operation ID is "MyOperationID"

  @sethOperationDescription
  Scenario: set operation description
    When import API "src/test/resources/preparedAPIs/basic.json"
    And select path "/operations"
    And select operation "GET"
    And set operation description "MyDescription"

    Then save API as "json" and close editor
    When import API "tmp/download/openapi-spec.json"

    And select path "/operations"
    And select operation "GET"
    Then check that operation description is "MyDescription"

  @setOperationTags
  Scenario: set operation tags
    When import API "src/test/resources/preparedAPIs/basic.json"
    And select path "/operations"
    And select operation "GET"
    And set operation tags "tag1,tag2"

    Then save API as "json" and close editor
    When import API "tmp/download/openapi-spec.json"

    And select path "/operations"
    And select operation "GET"
    Then check that operation tags are "tag1,tag2"

  @overrideConsumes
  Scenario: override consumes in operation
    When import API "src/test/resources/preparedAPIs/basic.json"
    And select path "/operations"
    And select operation "GET"
    #TODO cut "for operation"
    And override consumes or produces "consumes" with "text/xml" for operation "GET"

    Then save API as "json" and close editor
    When import API "tmp/download/openapi-spec.json"

    And select path "/operations"
    And select operation "GET"
    Then check that API "consumes" values "text/xml" on page "operations"

  @overrideProduces
  Scenario: override produces in operation
    When import API "src/test/resources/preparedAPIs/basic.json"
    And select path "/operations"
    And select operation "GET"
    #TODO cut "for operation"
    And override consumes or produces "produces" with "text/xml" for operation "GET"

    Then save API as "json" and close editor
    When import API "tmp/download/openapi-spec.json"

    And select path "/operations"
    And select operation "GET"
    Then check that API "produces" values "text/xml" on page "operations"

  @createOperationQP
  Scenario: create query parameter for operation
    When import API "src/test/resources/preparedAPIs/basic.json"
    And select path "/operations"
    And select operation "GET"
    And create "query" on "operations" page with plus sign "false"
      | QueryParameter | Description for Query Param | Required | Array | String | Date |

    Then save API as "json" and close editor
    When import API "tmp/download/openapi-spec.json"

    And select path "/operations"
    And select operation "GET"
    Then check that exist "query" on "operations" page
      | QueryParameter | Description for Query Param | Required | Array | String | Date |

  @createOperationHP
  Scenario: create header parameter for operation
    When import API "src/test/resources/preparedAPIs/basic.json"
    And select path "/operations"
    And select operation "GET"
    And create "header" on "operations" page with plus sign "true"
      | HeaderParameter | Description for Header Param | Required | Array | String | Date |

    Then save API as "json" and close editor
    When import API "tmp/download/openapi-spec.json"

    And select path "/operations"
    And select operation "GET"
    Then check that exist "header" on "operations" page
      | HeaderParameter | Description for Header Param | Required | Array | String | Date |

  @createOperationRequestBody
  Scenario: create request body
    When import API "src/test/resources/preparedAPIs/basic.json"
    And select path "/operations"
    And select operation "GET"

    And create request body
    And set request body description "Description for RB"
    And set parameters types
      | type | Array  | operations | request body | false |  |
      | of   | String | operations | request body | false |  |
      | as   | Date   | operations | request body | false |  |

    Then save API as "json" and close editor
    When import API "tmp/download/openapi-spec.json"

    And select path "/operations"
    And select operation "GET"

    Then check that exist request body
    And check that request body description is "Description for RB"
    And check parameters types
      | type | Array  | operations | request body | false |  |
      | of   | String | operations | request body | false |  |
      | as   | Date   | operations | request body | false |  |

  @createOperationRDF
  Scenario: create request data form
    When import API "src/test/resources/preparedAPIs/basic.json"
    And select path "/operations"
    And select operation "GET"
    And create "RDF" on "operations" page with plus sign "false"
      | FormData | Description for FormData | Required | Array | String | Date |

    Then save API as "json" and close editor
    When import API "tmp/download/openapi-spec.json"

    And select path "/operations"
    And select operation "GET"
    Then check that exist "RDF" on "operations" page
      | FormData | Description for FormData | Required | Array | String | Date |

  @createOperationResponseLink
  Scenario: create response by link
    When import API "src/test/resources/preparedAPIs/basic.json"
    And select path "/operations"
    And select operation "GET"

    And set response with plus sign
      | 200 |  | false |

    And set response description "Description for response 200" for response "200"
    And set parameters types
      | type | Array  | operations | response | true | 200 |
      | of   | String | operations | response | true | 200 |
      | as   | String | operations | response | true | 200 |

    Then save API as "json" and close editor
    When import API "tmp/download/openapi-spec.json"

    And select path "/operations"
    And select operation "GET"

    Then check that "exists" response 200
    And check that description is "Description for response 200" for response "200"
    And check parameters types
      | type | Array  | operations | response | true | 200 |
      | of   | String | operations | response | true | 200 |
      | as   | String | operations | response | true | 200 |

  @createOperationResponsePlus
  Scenario: create response by plus
    When import API "src/test/resources/preparedAPIs/basic.json"
    And select path "/operations"
    And select operation "GET"

    And set response with plus sign
      | 100 |  | true |
    And set response description "Description for response 100" for response "100"
    And set parameters types
      | type | Number | operations | response | true | 100 |
      | as   | Float  | operations | response | true | 100 |

    Then save API as "json" and close editor
    When import API "tmp/download/openapi-spec.json"

    And select path "/operations"
    And select operation "GET"

    Then check that "exists" response 100
    And check that description is "Description for response 100" for response "100"
    And check parameters types
      | type | Number | operations | response | true | 100 |
      | as   | Float  | operations | response | true | 100 |

  @createOperationResponsePlus-openapi
  Scenario: create response by plus OpenAPIv3
    When import API "src/test/resources/preparedAPIs/openapi-spec.json"
    And select path "/operations"
    And select operation "GET"

    And set response with plus sign
      | 100 |  | true |
    And set response description "Description for response 100" for response "100"
    And set type of "application/json" media type to "Number" on property "type" for response "100"
    And set type of "application/json" media type to "Float" on property "as" for response "100"

    Then save API as "json" and close editor
    When import API "tmp/download/openapi-spec.json"

    And select path "/operations"
    And select operation "GET"

    Then check that "exists" response 100
    And check that description is "Description for response 100" for response "100"
    Then check type of "application/json" media type is "Number" on property "type" for response "100"
    And check type of "application/json" media type is "Float" on property "as" for response "100"


  @overrideSecurityRequirements
  Scenario: override security requirements
    When import API "src/test/resources/preparedAPIs/paramsAndSecurity.json"
    And select path "/first/{id}"
    And select operation "GET"

    And override security requirements in operation with
      | oauth |
      | api   |

    And override security requirements in operation with
      | No Security |

    Then save API as "json" and close editor
    When import API "tmp/download/openapi-spec.json"

    And select path "/first/{id}"
    And select operation "GET"
    Then check that operation security requirement "oauth, api" "is" created
    And check that operation security requirement "No Security" "is" created

  @createPathParameter
  Scenario: create one path parameter
    When import API "src/test/resources/preparedAPIs/paramsAndSecurity.json"
    And select path "/first/{id}"

    And create path parameter "id" on "path" page
    And set description "parameter id desc" for path parameter "id" on "path" page
    And set path parameter type "File" for path parameter "id" on "path" page

    Then save API as "json" and close editor
    When import API "tmp/download/openapi-spec.json"

    And select path "/first/{id}"

    Then check that path parameter "id" is created for path "/first/{id}"
    And check that path parameter "id" on "path" page has description "parameter id desc"
    And check that path parameter "id" on "path" page has "type" type with value "File"

  @overridePathParameter
  Scenario: override path parameter
    When import API "src/test/resources/preparedAPIs/paramsAndSecurity.json"
    And select path "/first/{id}/{name}{email}"

    And override parameter "id"
    And set description "override parameter id desc" for path parameter "id" on "operation" page

    Then save API as "json" and close editor
    When import API "tmp/download/openapi-spec.json"

    And select path "/first/{id}/{name}{email}"

    Then check that path parameter "id" is overridden
    And check that overridden path parameter "id" has description "override parameter id desc"

    #TODO Add test -> create path parameter in operation

  @createPathQP
  Scenario: create query parameter on path page
    When import API "src/test/resources/preparedAPIs/basic.json"
    And select path "/clearPath"
    And create "query" on "path" page with plus sign "true"
      | QueryParameter | Description for Query Param | Required | Array | String | Date |

    Then save API as "json" and close editor
    When import API "tmp/download/openapi-spec.json"

    And select path "/clearPath"
    Then check that exist "query" on "path" page
      | QueryParameter | Description for Query Param | Required | Array | String | Date |

  @createPathHP
  Scenario: create header parameter on path page
    When import API "src/test/resources/preparedAPIs/basic.json"
    And select path "/clearPath"
    And create "header" on "path" page with plus sign "false"
      | HeaderParameter | Description for Header Param | Required | Array | String | Date |

    Then save API as "json" and close editor
    When import API "tmp/download/openapi-spec.json"

    And select path "/clearPath"
    Then check that exist "header" on "path" page
      | HeaderParameter | Description for Header Param | Required | Array | String | Date |

  @allGetParameters
  Scenario: fill almost all get parameters
    # There are missing: request data form, override parameter and security requirement
    When import API "src/test/resources/preparedAPIs/basic.json"
    And select path "/operations"
    And select operation "GET"

    And set operation summary "MySummary"
    And set operation id "MyOperationID"
    And set operation description "MyDescription"
    And set operation tags "tag1,tag2"
    And override consumes or produces "consumes" with "text/xml" for operation "GET"
    And override consumes or produces "produces" with "text/xml" for operation "GET"

    And create "query" on "operations" page with plus sign "false"
      | QueryParameter | Description for Query Param | Required | Array | String | Date |

    And create "header" on "operations" page with plus sign "false"
      | HeaderParameter | Description for Header Param | Required | Array | String | Date |

    And create request body
    And set request body description "Description for RB"
    And set parameters types
      | type | Array  | operations | request body | false |  |
      | of   | String | operations | request body | false |  |
      | as   | Date   | operations | request body | false |  |

    And set response with plus sign
      | 100 |  | true |
    And set response description "Description for response 100" for response "100"
    And set parameters types
      | type | Number | operations | response | true | 100 |
      | as   | Float  | operations | response | true | 100 |

    Then save API as "json" and close editor
    When import API "tmp/download/openapi-spec.json"

    And select path "/operations"
    And select operation "GET"

    Then check that operation summary is "MySummary"
    And check that operation ID is "MyOperationID"
    And check that operation description is "MyDescription"
    And check that operation tags are "tag1,tag2"
    Then check that API "consumes" values "text/xml" on page "operations"
    Then check that API "produces" values "text/xml" on page "operations"

    And check that exist "query" on "operations" page
      | QueryParameter | Description for Query Param | Required | Array | String | Date |

    And check that exist "header" on "operations" page
      | HeaderParameter | Description for Header Param | Required | Array | String | Date |

    And check that exist request body
    And check that request body description is "Description for RB"
    And check parameters types
      | type | Array  | operations | request body | false |  |
      | of   | String | operations | request body | false |  |
      | as   | Date   | operations | request body | false |  |

    And check that "exists" response 100
    And check that description is "Description for response 100" for response "100"
    And check parameters types
      | type | Number | operations | response | true | 100 |
      | as   | Float  | operations | response | true | 100 |

  @otherParameters
  Scenario: fill other operation parameters
    When import API "src/test/resources/preparedAPIs/paramsAndSecurity.json"
    And select path "/first/{id}/{name}{email}"
    And select operation "POST"

    And create "RDF" on "operations" page with plus sign "false"
      | FormData | Description for FormData | Required | Array | String | Date |

    And override security requirements in operation with
      | oauth |
      | basic |

    And override security requirements in operation with
      | No Security |

    Then save API as "json" and close editor
    When import API "tmp/download/openapi-spec.json"
    And select path "/first/{id}/{name}{email}"
    And select operation "POST"

    Then check that exist "RDF" on "operations" page
      | FormData | Description for FormData | Required | Array | String | Date |

    And check that operation security requirement "oauth, basic" "is" created
    And check that operation security requirement "No Security" "is" created

  @pathWithParameters
  Scenario: create path with parameters, one override and one create in operation
    When import API "src/test/resources/preparedAPIs/paramsAndSecurity.json"
    And select path "/first/{id}/{name}{email}"

    And create path parameter "email" on "path" page
    And set description "parameter email desc" for path parameter "email" on "path" page
    And set path parameter type "String" for path parameter "email" on "path" page
    And set path parameter type as "Byte" for path parameter "email" on "path" page

    And select operation "GET"
    And override parameter "email"
    And set description "override parameter email desc" for path parameter "email" on "operation" page
    And create path parameter "name" on "operation" page
    And set description "parameter name desc" for path parameter "name" on "operation" page
    And set path parameter type "String" for path parameter "name" on "operation" page

    Then save API as "json" and close editor
    When import API "tmp/download/openapi-spec.json"

    And select path "/first/{id}/{name}{email}"

    Then check that path parameter "email" is created for path "/first/{id}/{name}{email}"
    And check that path parameter "email" on "path" page has description "parameter email desc"

    And check that path parameter "email" on "path" page has "type" type with value "String"
    And check that path parameter "email" on "path" page has "as" type with value "Byte"

    And select operation "GET"

    And check that path parameter "email" is overridden
    And check that overridden path parameter "email" has description "override parameter email desc"

    And check that path parameter "name" is created in operation
    And check that path parameter "name" on "operation" page has description "parameter name desc"
    And check that path parameter "name" on "operation" page has "type" type with value "String"

  @createPathCP
  Scenario: create cookie parameter on path page
    When import API "src/test/resources/preparedAPIs/basicV3.yaml"
    And select path "/clearPath"
    And create "cookie" on "path" page with plus sign "false"
      | CookieParameter | Description for Cookie Param | Required | Array | String | DateTime |

    Then save API as "json" and close editor
    When import API "tmp/download/openapi-spec.json"

    And select path "/clearPath"
    Then check that exist "cookie" on "path" page
      | CookieParameter | Description for Cookie Param | Required | Array | String | DateTime |

  @createOperationCP
  Scenario: create cookie parameter on operations page
    When import API "src/test/resources/preparedAPIs/basicV3.yaml"
    And select path "/operations"
    And create "cookie" on "operations" page with plus sign "false"
      | CookieParameter | Description for Cookie Param | Required | Array | String | DateTime |

    Then save API as "json" and close editor
    When import API "tmp/download/openapi-spec.json"

    And select path "/operations"
    Then check that exist "cookie" on "operations" page
      | CookieParameter | Description for Cookie Param | Required | Array | String | DateTime |

  @createResponseWithResponseDef
  Scenario: create a reponse with response definition
    When import API "src/test/resources/preparedAPIs/basicV3.yaml"
    And create a new response by link
      | response1 | response desc | false |

    And select path "/operations"
    And set response with plus sign
      | 404 | response1 | true |

    Then check that "exists" response 404
    Then check that response "404" is created from response definition "response1"