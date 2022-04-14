@apicuritoTests
@ui
@dataTypesTests
Feature: Data types tests

  Background:
    Given delete file "tmp/download/openapi-spec.json"
    And log into apicurito

  @setDataTypeDescription
  Scenario: set data types description
    When import API "src/test/resources/preparedAPIs/basic.json"
    And select data type "clearDataType"
    And set data type description "best data type"
    And save API as "json" and close editor
    And import API "tmp/download/openapi-spec.json"
    And select data type "clearDataType"
    Then check that data type description is "best data type"

  @setDataTypeProperty
  Scenario: set data type property
    When import API "src/test/resources/preparedAPIs/basic.json"
    And select data type "clearDataType"
    And create data type property with name "MyProperty"
    And set description "My desc" for data type property "MyProperty"
    And set property "type" as "Integer" for property "MyProperty"
    And set property "as" as "32-Bit Integer" for property "MyProperty"
    And set property "required" as "Required" for property "MyProperty"

    Then save API as "json" and close editor
    When import API "tmp/download/openapi-spec.json"
    And select data type "clearDataType"

    Then check that data type property "MyProperty" is created
    And check that description is "My desc" for property "MyProperty"

    And check that "type" is "Integer" for property "MyProperty"
    And check that "as" is "32-Bit Integer" for property "MyProperty"
    And check that "required" is "Required" for property "MyProperty"

  @setDataTypeExample
  Scenario: set data type example
    When import API "src/test/resources/preparedAPIs/basic.json"
    And select data type "clearDataType"
    And set data type example "MyExample"
    Then save API as "json" and close editor
    When import API "tmp/download/openapi-spec.json"
    And select data type "clearDataType"
    Then check that example is MyExample

  @createDataTypeWithRestResource
  Scenario: create data type with rest resource
    When create a new API version "2"
    And create a new data type by link
      | data2 |  |  | true | false |

    And save API as "json" and close editor
    Then import API "tmp/download/openapi-spec.json"

    And check that path "/data2" "is" created
    And check that operation "GET" is created for path "/data2"
    When select path "/data2"
    And select operation "GET"
    Then check that operation summary is "List All data2"
    And check that operation ID is "getdata2"
    And check that operation description is "Gets a list of all data2 entities."

    And check that "exists" response 200
    And check that description is "Successful response - returns an array of data2 entities." for response "200"

    And check parameters types
      | type | Array | operations | response | true | 200 |
      | of   | data2 | operations | response | true | 200 |

    And check that operation "POST" is created for path "/data2"
    When select path "/data2"
    And select operation "POST"
    Then check that operation summary is "Create a data2"
    And check that operation ID is "createdata2"
    And check that operation description is "Creates a new instance of a data2."

    And check that "exists" response 201
    And check that description is "Successful response." for response "201"
    #Choose type mean that the type is not set
    And check parameters types
      | type | Choose Type | operations | response | true | 201 |

    And check that request body description is "A new data2 to be created."
    And check parameters types
      | type | data2 | operations | request body | false | |

    And check that path "/data2/{data2Id}" "is" created
    And check that operation "GET" is created for path "/data2/{data2Id}"
    When select path "/data2/{data2Id}"

    And check that path parameter "data2Id" is created for path "/data2/{data2Id}"

    And select operation "GET"
    Then check that operation summary is "Get a data2"
    And check that operation ID is "getdata2"
    And check that operation description is "Gets the details of a single instance of a data2."

    And check that "exists" response 200
    And check that description is "Successful response - returns a single data2." for response "200"
    And check parameters types
      | type | data2 | operations | response | true | 200 |

    And check that operation "PUT" is created for path "/data2/{data2Id}"
    When select path "/data2/{data2Id}"
    And select operation "PUT"
    Then check that operation summary is "Update a data2"
    And check that operation ID is "updatedata2"
    And check that operation description is "Updates an existing data2."

    And check that "exists" response 202
    And check that description is "Successful response." for response "202"
    #Choose type mean that the type is not set
    And check parameters types
      | type | Choose Type | operations | response | true | 202 |

    And check that request body description is "Updated data2 information."
    And check parameters types
      | type | data2 | operations | request body | false | |

    And check that operation "DELETE" is created for path "/data2/{data2Id}"
    When select path "/data2/{data2Id}"
    And select operation "DELETE"
    Then check that operation summary is "Delete a data2"
    And check that operation ID is "deletedata2"
    And check that operation description is "Deletes an existing data2."

    And check that "exists" response 204
    And check that description is "Successful response." for response "204"
    #Choose type mean that the type is not set
    And check parameters types
      | type | Choose Type | operations | response | true | 204 |

  @createFullDataType
  Scenario: create full data type
    When create a new API version "2"
    And create a new data type by link
      | exampleTest | desc | {"name" : "John", "age" : 24, "isMan" : true} | false | true |

    Then save API as "json" and close editor
    When import API "tmp/download/openapi-spec.json"
    And select data type "exampleTest"

    Then check that data type description is "desc"

    And check that example is {"name" : "John", "age" : 24, "isMan" : true}

    # Test also checks that properties will be created for given json example
    And check that data type property "age" is created
    And check that "type" is "Integer" for property "age"
    And check that "as" is "32-Bit Integer" for property "age"
    And check that "required" is "Not Required" for property "age"

    And check that data type property "isMan" is created
    And check that "type" is "Boolean" for property "isMan"
    And check that "required" is "Not Required" for property "isMan"

    And check that data type property "name" is created
    And check that "type" is "String" for property "name"
    And check that "as" is "String" for property "name"
    And check that "required" is "Not Required" for property "name"
