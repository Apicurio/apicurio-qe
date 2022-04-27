@apicuritoTests
@ui
@buttonsTests
Feature: Buttons tests

  Background:
    Given delete file "tmp/download/openapi-spec.json"
    And log into apicurito
    And import API "src/test/resources/preparedAPIs/filledApi.json"

  #-------------CONTEXT CLICKS ---------------------
  @pathSubPath
  Scenario: path context click new sub-path
    When context click on and manage element
      | path | /path1 | New Sub-Path | /path11 |
    Then check that path "/path11" "is" created
    And check that path "/path1" "is" created

  @pathRename
  Scenario: path context click rename
    When context click on and manage element
      | path | /path1 | Rename | /path11 |
    Then check that path "/path11" "is" created
    And check that path "/path1" "is not" created

  @pathClone
  Scenario: path context click clone
    When context click on and manage element
      | path | /path1 | Clone | /path11 |
    Then check that path "/path1" "is" created
    And check that path "/path11" "is" created

  @pathDelete
  Scenario: path context click delete
    When context click on and manage element
      | path | /path1 | Delete |  |
    Then check that path "/path1" "is not" created

  @dataTypeRename
  Scenario: data type context click rename
    When context click on and manage element
      | data type | data1 | Rename | data11 |
    Then check that data type "data11" "is" created
    And check that data type "data1" "is not" created

  @dataTypeClone
  Scenario: data type context click clone
    When context click on and manage element
      | data type | data1 | Clone | data11 |
    Then check that data type "data1" "is" created
    And check that data type "data11" "is" created

  @dataTypeDelete
  Scenario: data type context click delete
    When context click on and manage element
      | data type | data1 | Delete |  |
    Then check that data type "data1" "is not" created

  #-------------KEBAB CLICKS - MAIN PAGE ---------------------
  @kebabSchemeRename
  Scenario: kebab rename security scheme
    When click on kebab menu and manage element
      | main page | security schema | s2(apiKey) | Rename | s22 |
    Then check security scheme
      | s22 | apiKey |  |

  @kebabSchemeDelete
  Scenario: kebab delete security scheme
    When click on kebab menu and manage element
      | main page | security schema | s3(oauth2) | Delete |  |
    Then check that scheme "s3" "oauth2" "is not" created

  @kebabMPsecurityReqDelete
  Scenario: kebab delete security requirement on main page
    When click on kebab menu and manage element
      | main page | security requirement | s1 | Delete |  |
    Then check that API security requirement "s1" "is" created

    #TODO EDIT security schemes and requirements

  #-------------KEBAB CLICKS - PATH PAGE---------------------
  @kebabPathSubPath
  Scenario: kebab path new sub-path
    When select path "/path1"
    And click on kebab menu and manage element
      | path |  |  | New Sub-Path | /path11 |
    Then check that path "/path11" "is" created

  @kebabPathRenamePath
  Scenario: kebab path rename path
    When select path "/path1"
    And click on kebab menu and manage element
      | path |  |  | Rename Path | /path11 |
    Then check that path "/path11" "is" created
    And check that path "/path1" "is not" created

  @kebabPathClonePath
  Scenario: kebab path clone path
    When select path "/path1"
    And click on kebab menu and manage element
      | path |  |  | Clone Path | /path11 |
    Then check that path "/path11" "is" created
    And check that path "/path1" "is" created

  @kebabPathDeletePath
  Scenario: kebab path delete path
    When select path "/path1"
    And click on kebab menu and manage element
      | path |  |  | Delete Path |  |
    Then check that path "/path1" "is not" created

    #TODO colapse and expand sections

  @kebabPathRenameQP
  Scenario: kebab path rename QP
    When select path "/path1"
    And click on kebab menu and manage element
      | path | query | qp2 | Rename | qp22 |
    Then check that "is" created "query" on "path" page with name "qp22"
    And check that "is not" created "query" on "path" page with name "qp2"

  @kebabPathDeleteQP
  Scenario: kebab path delete QP
    When select path "/path1"
    And click on kebab menu and manage element
      | path | query | qp3 | Delete |  |
    Then check that "is not" created "query" on "path" page with name "qp3"

  @kebabPathRenameHP
  Scenario: kebab path rename HP
    When select path "/path1"
    And click on kebab menu and manage element
      | path | header | hp2 | Rename | hp22 |
    Then check that "is" created "header" on "path" page with name "hp22"
    And check that "is not" created "header" on "path" page with name "hp2"

  @kebabPathDeleteHP
  Scenario: kebab path delete HP
    When select path "/path1"
    And click on kebab menu and manage element
      | path | header | hp3 | Delete |  |
    Then check that "is not" created "header" on "path" page with name "hp3"

  #-------------KEBAB CLICKS - OPERATIONS PAGE---------------------
  @kebabOperationRenameQP
  Scenario: kebab operation rename QP
    When select path "/path1"
    And click on kebab menu and manage element
      | operations | query | qp2 | Rename | qp22 |
    Then check that "is" created "query" on "path" page with name "qp22"
    And check that "is not" created "query" on "operations" page with name "qp2"

  @kebabOperationDeleteQP
  Scenario: kebab operation delete QP
    When select path "/path1"
    And click on kebab menu and manage element
      | operations | query | qp3 | Delete |  |
    Then check that "is not" overridden "query" on "operations" page with name "qp3"

  @kebabOperationRenameHP
  Scenario: kebab operation rename HP
    When select path "/path1"
    And click on kebab menu and manage element
      | operations | header | hp2 | Rename | hp22 |
    Then check that "is" created "header" on "operations" page with name "hp22"
    And check that "is not" created "header" on "operations" page with name "hp2"

  @kebabOperationDeleteHP
  Scenario: kebab operation delete HP
    When select path "/path1"
    And click on kebab menu and manage element
      | operations | header | hp3 | Delete |  |
    Then check that "is not" overridden "query" on "operations" page with name "qp3"

  @kebabOperationRenameRDF
  Scenario: kebab operation rename RDF
    When select path "/path1"
    And click on kebab menu and manage element
      | operations | RDF | fd2 | Rename | fd22 |
    Then check that "is" created "RDF" on "operations" page with name "fd22"
    And check that "is not" created "RDF" on "operations" page with name "fd2"

  @kebabOperationDeleteRDF
  Scenario: kebab operation delete RDF
    When select path "/path1"
    And click on kebab menu and manage element
      | operations | RDF | fd3 | Delete |  |
    Then check that "is not" created "RDF" on "operations" page with name "fd3"

  @kebabOperationDeleteResponse
  Scenario: kebab operation delete response
    When select path "/path1"
    And click on kebab menu and manage element
      | operations | response | 200 | Delete Response |  |
    Then check that "not exists" response 200

  @kebabOperationCloneResponse
  Scenario: kebab operation clone response
    When select path "/path1"
    And click on kebab menu and manage element
      | operations | response | 200 | Clone Response | 100 |
    Then check that "exists" response 200
    Then check that "exists" response 100


  @kebabOperationDeleteSecurityRequirement
  Scenario: kebab operation delete security requirement
    When select path "/path1"
    And click on kebab menu and manage element
      | operations | security requirement | s2 | Delete |  |
    Then check that operation security requirement "s2" "is not" created

  #-------------KEBAB CLICKS - DATA TYPES PAGE---------------------
  @kebabDatatypesRenameDataType
  Scenario: kebab data types rename data type
    When select data type "data1"
    And click on kebab menu and manage element
      | datatypes |  |  | Rename Data Type | data11 |
    Then check that data type "data11" "is" created
    And check that data type "data1" "is not" created

  @kebabDatatypesCloneDataType
  Scenario: kebab data types clone data type
    When select data type "data1"
    And click on kebab menu and manage element
      | datatypes |  |  | Clone Data Type | data11 |
    Then check that data type "data11" "is" created
    And check that data type "data1" "is" created

  @kebabDatatypesDeleteDataType
  Scenario: kebab data types delete data type
    When select data type "data1"
    And click on kebab menu and manage element
      | datatypes |  |  | Delete Data Type | data11 |
    Then check that data type "data1" "is not" created

  #@kebabDatatypesRenameProperty
  #Scenario: kebab data types rename property

  #TODO create tests: rename and delete property after closing
  #https://github.com/Apicurio/apicurito/issues/43


