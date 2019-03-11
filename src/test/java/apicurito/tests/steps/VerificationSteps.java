package apicurito.tests.steps;

import apicurito.tests.utils.slenide.CommonUtils;
import apicurito.tests.utils.slenide.MainPageUtils;
import apicurito.tests.utils.slenide.OperationUtils;
import apicurito.tests.utils.slenide.PathUtils;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import io.cucumber.datatable.DataTable;
import org.assertj.core.api.SoftAssertions;
import org.junit.Rule;
import org.openqa.selenium.By;

import java.util.List;

import static com.codeborne.selenide.Condition.*;

public class VerificationSteps {

    @Rule
    private SoftAssertions collector = new SoftAssertions();

    @Then("^check all for errors$")
    public void checkAllForErrors() throws Throwable {
        collector.assertAll();
    }

    /**********************************************************************
     *********************** MAIN PAGE verification steps *****************
     **********************************************************************/

    @And("^check that API name is \"([^\"]*)\"$")
    public void checkThatAPINameIs(String expectedName)  {
        String apiName = CommonUtils.getAppRoot().$("title-bar").$("h1").getText();
        collector.assertThat(apiName).as("Checking API name:").isEqualTo(expectedName);
    }

    @And("^check that API version is \"([^\"]*)\"$")
    public void checkThatAPIVersionIs(String expectedVersion) {
        CommonUtils.getAppRoot().$("title-bar").$("h1").click();
        String version = MainPageUtils.getMainPageRoot().$("info-section").$(By.className("version")).getText();
        collector.assertThat(version).as("Checking API version:").isEqualTo(expectedVersion);
    }

    @And("^check that API description is \"([^\"]*)\"$")
    public void checkThatAPIDescriptionIs(String expectedDescription) {
        CommonUtils.getAppRoot().$("title-bar").$("h1").click();
        String description = MainPageUtils.getMainPageRoot().$("info-section").$(By.className("description")).getText();
        collector.assertThat(description).as("Checking API descritpion:").isEqualTo(expectedDescription);

    }

    @And("^check that API consume \"([^\"]*)\"$")
    public void checkThatAPIConsume(String expectedConsume){
        CommonUtils.getAppRoot().$("title-bar").$("h1").click();
        String consume = MainPageUtils.getMainPageRoot().$(By.className("consumes")).getText();
        collector.assertThat(consume).as("Checking API consume:").isEqualTo(expectedConsume);
    }

    @And("^check that API produce \"([^\"]*)\"$")
    public void checkThatAPIProduce(String expectedProduce){
        CommonUtils.getAppRoot().$("title-bar").$("h1").click();
        String produce = MainPageUtils.getMainPageRoot().$(By.className("produces")).getText();
        collector.assertThat(produce).as("Checking API consume:").isEqualTo(expectedProduce);
    }

    @And("^check that API contact info is$")
    public void checkThatAPIContactInfoIs(DataTable table) {
        CommonUtils.getAppRoot().$("title-bar").$("h1").click();
        for (List<String> dataRow : table.cells()) {
            String name = MainPageUtils.getMainPageRoot().$("contact-section").$(By.className("name")).getText();
            String email = MainPageUtils.getMainPageRoot().$("contact-section").$(By.className("email")).getText();
            String url = MainPageUtils.getMainPageRoot().$("contact-section").$(By.className("url")).getText();

            collector.assertThat(name).as("Checking API contatct name:").isEqualTo(dataRow.get(0));
            collector.assertThat(email).as("Checking API contatct email:").isEqualTo(dataRow.get(1));
            collector.assertThat(url).as("Checking API contatct url:").isEqualTo(dataRow.get(2));
        }
    }

    @And("^check that API license is \"([^\"]*)\"$")
    public void checkThatAPILicenseIs(String expectedLincense) {
        CommonUtils.getAppRoot().$("title-bar").$("h1").click();
        ElementsCollection licenses = MainPageUtils.getMainPageRoot().$("license-section").$$("button").filter(text("Change License"));
        if (licenses.size() == 1){
            String license = MainPageUtils.getMainPageRoot().$("license-section").$("h2").getText();
            collector.assertThat(license).as("Checking API linces:").isEqualTo(expectedLincense);
        }else{
            collector.fail("License is not set!");
        }
    }

    @And("^check that API have tag \"([^\"]*)\" with description \"([^\"]*)\"$")
    public void checkThatAPIHaveTagWithDescription(String expectedTag, String expectedDescription) {
        CommonUtils.getAppRoot().$("title-bar").$("h1").click();
        ElementsCollection tagRows = MainPageUtils.getMainPageRoot().$("tags-section").$$("tag-row");

        if (tagRows.size() > 0 ) {
            ElementsCollection tag = MainPageUtils.getMainPageRoot().$("tags-section").$$(By.className("name")).filter((text(expectedTag)));

            if (tag.size() == 0) {
                collector.fail("Tag %s is not created!", expectedTag);
            } else {
                String desc = tag.first().parent().$(By.className("description")).getText();
                collector.assertThat(desc).as("Checking tag description:").isEqualTo(expectedDescription);
            }
        }else{
            collector.fail("Tags are not created!");
        }
    }

    @And("^check that path \"([^\"]*)\" is created$")
    public void checkThatPathIsCreated(String expectedPathName) {
        try {
            Thread.sleep(1000L * 1);        //need to wait at least for a second because of Stale Element Reference Exception
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ElementsCollection paths  = MainPageUtils.getMainPageRoot().$$("section").filter(attribute("label", "Paths")).first()
                .$$(By.className("api-path")).filter(exactText(expectedPathName));

        collector.assertThat(paths.size()).as("Path %s is not created:", expectedPathName).isEqualTo(1);
    }

    @And("^check that data type \"([^\"]*)\" is created$")
    public void checkThatDataTypeIsCreated(String datatype) {
        ElementsCollection types = MainPageUtils.getMainPageRoot().$$("section").filter(attribute("label", "Data Types")).first()
                .$$(By.className("api-definition")).filter(exactText(datatype));
        collector.assertThat(types.size()).as("Data type %s is not created!", datatype).isEqualTo(1);
    }

    /************************************************************
     ************** PATH PAGE verifications steps ****************
     ************************************************************/

    @And("^check that operation \"([^\"]*)\" is created for path \"([^\"]*)\"$")
    public void checkThatOperationIsCreatedForPath(String operation, String path) {
        SelenideElement pathElement = MainPageUtils.getPathWithName(path);      //TODO private method to check that path is created
        if(pathElement == null){
            collector.assertThat(pathElement).as("Operation %s is not created because path %s is not found.", operation, path).isNotNull();
            return;
        }else{
            pathElement.click();
        }

        ElementsCollection ec = PathUtils.getPathPageRoot().$$(By.cssSelector("div." + operation.toLowerCase() +"-tab.enabled"));
        collector.assertThat(ec.size() == 1 ).as("Operation %s is not created", operation).isTrue();
    }

    @And("^check that path parameter \"([^\"]*)\" is created for path \"([^\"]*)\"$")
    public void checkThatPathParameterIsCreatedForPath(String parameter, String path) {
        SelenideElement pathElement = MainPageUtils.getPathWithName(path);
        if(pathElement == null){
            collector.assertThat(pathElement).as("Parameter %s is not created because path %s is not found.", parameter, path).isNotNull();
            return;
        }else{
            pathElement.click();
        }

        SelenideElement parameterElement = PathUtils.getPathPageRoot().$("path-params-section").$$("path-param-row").filter(matchText(parameter)).first();
        collector.assertThat(parameterElement.$("div").getAttribute("class")).as("Path parameter %s is not created", parameter).doesNotContain("missing");
    }

    @And("^check that path parameter \"([^\"]*)\" has description \"([^\"]*)\"$")
    public void checkThatPathParameterHasDescription(String parameter, String description) {
        SelenideElement descriptionElement = PathUtils.getPathPageRoot().$("path-params-section").$$("path-param-row")
                .filter(matchText(parameter)).first().$(By.className("description"));

        collector.assertThat(descriptionElement.getText()).as("Description for path parameter %s is different", parameter).isEqualTo(description);
    }

    @And("^check that path parameter \"([^\"]*)\" has type \"([^\"]*)\" formatted as \"([^\"]*)\"$")
    public void checkThatPathParameterHasTypeFormattedAs(String parameter, String type, String as) {
        SelenideElement summaryElement = PathUtils.getPathPageRoot().$("path-params-section").$$("path-param-row")
                .filter(matchText(parameter)).first().$(By.className("summary"));

        collector.assertThat(summaryElement.getText()).as("Parameter has not type %s formatted as %s", type, as).isEqualTo(type.toLowerCase() + " as " + as.toLowerCase());
                                                                                                                                        //TODO need support for array
                                                                                                                                        //TODO e.g. int32 will not work (different strings in summary)
                                                                                                                                        //TODO if both type and as are the same ->fail
    }

    /**************************************************************
     ************* OPERATION PAGE verification steps **************
     **************************************************************/

    @And("^check that operation summary is \"([^\"]*)\"$")
    public void checkThatOperationSummaryIs(String expectedSummary) {
        String summary = OperationUtils.getOperationRoot().$(By.className("summary")).getText();
        collector.assertThat(summary).as("Checking operation summary:").isEqualTo(expectedSummary);
    }

    @And("^check that operation ID is \"([^\"]*)\"$")
    public void checkThatOperationIDIs(String expectedID) {
        String id = OperationUtils.getOperationRoot().$(By.className("operationId")).getText();
        collector.assertThat(id).as("Checking operation ID:").isEqualTo(expectedID);
    }

    @And("^check that operation description is \"([^\"]*)\"$")
    public void checkThatOperationDescriptionIs(String expectedDescription) {
        String description = OperationUtils.getOperationRoot().$(By.className("description")).getText();
        collector.assertThat(description).as("Checking operation description:").isEqualTo(expectedDescription);
    }

    /**
     * Tags must be in same order
     * @param expectedTags separate tags with ","
     *
     */
    @And("^check that operation tags are \"([^\"]*)\"$")
    public void checkThatOperationTagsAre(String expectedTags){
        String[] list = expectedTags.split(",");
        ElementsCollection ec = OperationUtils.getOperationRoot().$(By.className("tags")).$$(By.className("label-default"));
        for ( int i = 0; i < ec.size(); ++i ){
            collector.assertThat(ec.get(i).getText()).as("Checking %d. tag:",i+1).isEqualTo(list[i]);
        }
    }

    @And("^check that path parameter \"([^\"]*)\" is overridden$")
    public void checkThatPathParameterIsOverridden(String parameter) {
        SelenideElement parameterElement = OperationUtils.getOperationRoot().$("path-params-section").$$("path-param-row").filter(matchText(parameter)).first();
        collector.assertThat(parameterElement.$("div").getAttribute("class")).as("Path parameter %s is not overridden", parameter).doesNotContain("overridable");
    }

    @And("^check that overridden path parameter \"([^\"]*)\" has description \"([^\"]*)\"$")
    public void checkThatOverriddenPathParameterHasDescription(String parameter, String description) {
        SelenideElement descriptionElement = OperationUtils.getOperationRoot().$("path-params-section").$$("path-param-row")
                .filter(matchText(parameter)).first().$(By.className("description"));

        collector.assertThat(descriptionElement.getText()).as("Description for path parameter %s is different", parameter).isEqualTo(description);
    }


    @And("^check that exist response (\\d+)$")
    public void checkThatExistResponse(Integer response) {
        ElementsCollection rows = OperationUtils.getOperationRoot().$$(By.className("statusCode")).filter(text(response.toString()));
        collector.assertThat(rows.size()).as("Response %d is not exist", response).isEqualTo(1);
    }

    @And("^check that description is \"([^\"]*)\" for response (\\d+)$")
    public void checkThatDescriptionIsForResponse(String expectedDescription, Integer response) {
        OperationUtils.selectResponse(response);
        String description = OperationUtils.getOperationRoot().$("responses-section").$(By.className("response-description")).$(By.className("grow")).getText();
        collector.assertThat(description).as("Checking description:").isEqualTo(expectedDescription);
    }

    @And("^check that type is \"([^\"]*)\" for response (\\d+)$")
    public void checkThatTypeIsForResponse(String expectedType, Integer response) {
        OperationUtils.selectResponse(response);
        String type = OperationUtils.getOperationRoot().$("responses-section").$("#api-property-type").getText();
        collector.assertThat(type).as("Type is %s but should be %s", type, expectedType).isEqualTo(expectedType);
    }

    @And("^check that type of is \"([^\"]*)\" for response (\\d+)$")
    public void checkThatTypeOfIsForResponse(String expectedOf, Integer response) {
        OperationUtils.selectResponse(response);
        String of = OperationUtils.getOperationRoot().$("responses-section").$("#api-property-type-of").getText();
        collector.assertThat(of).as("Type of is %s but should be %s", of, expectedOf).isEqualTo(expectedOf);
    }

    @And("^check that type as is \"([^\"]*)\" for response (\\d+)$")
    public void checkThatTypeAsIsForResponse(String expectedAs, Integer response) {
        OperationUtils.selectResponse(response);
        String as = OperationUtils.getOperationRoot().$("responses-section").$("#api-property-type-as").getText();
        collector.assertThat(as).as("Type as is %s but should be %s", as, expectedAs).isEqualTo(expectedAs);
    }
}