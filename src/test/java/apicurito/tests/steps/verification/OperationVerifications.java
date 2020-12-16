package apicurito.tests.steps.verification;

import apicurito.tests.utils.slenide.OperationUtils;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import io.cucumber.java.en.Then;
import org.openqa.selenium.By;

import static com.codeborne.selenide.Condition.*;
import static org.assertj.core.api.Assertions.assertThat;

public class OperationVerifications {

    private static class OperationElements {
        private static By DESCRIPTION = By.className("description");

        private static By PATH_PARAMETERS_SECTION = By.cssSelector("path-params-section");
        private static By RESPONSES_SECTION = By.cssSelector("responses-section");
        private static By REQUEST_BODY_SECTION = By.cssSelector("requestbody-section");
        private static By REQUIREMENTS_SECTION = By.cssSelector("security-requirements-section");

        private static By PATH_PARAMETERS_ROW = By.cssSelector("path-param-row");
    }

    @Then("^check that operation summary is \"([^\"]*)\"$")
    public void checkThatOperationSummaryIs(String expectedSummary) {
        String summary = OperationUtils.getOperationRoot().$(By.className("summary")).getText();
        assertThat(summary).as("Checking operation summary:").isEqualTo(expectedSummary);
    }

    @Then("^check that operation ID is \"([^\"]*)\"$")
    public void checkThatOperationIDIs(String expectedID) {
        String id = OperationUtils.getOperationRoot().$(By.className("operationId")).getText();
        assertThat(id).as("Checking operation ID:").isEqualTo(expectedID);
    }

    @Then("^check that operation description is \"([^\"]*)\"$")
    public void checkThatOperationDescriptionIs(String expectedDescription) {
        String description = OperationUtils.getOperationRoot().$(OperationElements.DESCRIPTION).getText();
        assertThat(description).as("Checking operation description:").isEqualTo(expectedDescription);
    }

    /**
     * Tags must be in same order
     *
     * @param expectedTags separate tags with ","
     */
    @Then("^check that operation tags are \"([^\"]*)\"$")
    public void checkThatOperationTagsAre(String expectedTags) {
        String[] list = expectedTags.split(",");
        ElementsCollection ec = OperationUtils.getOperationRoot().$(By.className("tags")).$$(By.className("label-default"));
        for (int i = 0; i < ec.size(); ++i) {
            assertThat(ec.get(i).getText()).as("Checking %d. tag:", i + 1).isEqualTo(list[i]);
        }
    }

    @Then("^check that path parameter \"([^\"]*)\" is overridden$")
    public void checkThatPathParameterIsOverridden(String parameter) {
        SelenideElement parameterElement = OperationUtils.getOperationRoot().$(OperationElements.PATH_PARAMETERS_SECTION).$$(OperationElements.PATH_PARAMETERS_ROW).filter(matchText(parameter)).first();
        assertThat(parameterElement.$("div").getAttribute("class")).as("Path parameter %s is not overridden", parameter).doesNotContain("overridable");
    }

    @Then("^check that overridden path parameter \"([^\"]*)\" has description \"([^\"]*)\"$")
    public void checkThatOverriddenPathParameterHasDescription(String parameter, String description) {
        SelenideElement descriptionElement = OperationUtils.getOperationRoot().$(OperationElements.PATH_PARAMETERS_SECTION).$$(OperationElements.PATH_PARAMETERS_ROW)
                .filter(matchText(parameter)).first().$(OperationElements.DESCRIPTION);

        assertThat(descriptionElement.getText()).as("Description for path parameter %s is different", parameter).isEqualTo(description);
    }

    /*
        param isCreated - "exists" for check that response exists
                        - "not exists" for check that response do not exists
     */
    @Then("^check that \"([^\"]*)\" response (\\d+)$")
    public void checkThatExistResponse(String isCreated, Integer response) {
        ElementsCollection rows = OperationUtils.getOperationRoot().$$(By.className("statusCode")).filter(text(response.toString()));
        if ("exists".equals(isCreated)) {
            assertThat(rows.size()).as("Response %d is not exists, and should exists", response).isEqualTo(1);
        } else {
            assertThat(rows.size()).as("Response %d exists, and should not", response).isEqualTo(0);
        }
    }

    @Then("check that response {string} is created from response definition {string}")
    public void checkThatResponseIsCreatedFromResponseDefinition(String response, String definition) {
        OperationUtils.getOperationRoot().$$(By.className("statusCode")).filter(text(response)).first().click();
        SelenideElement responseBody = OperationUtils.getOperationRoot().$(OperationElements.RESPONSES_SECTION).$(By.className("response-ref"));
        assertThat(responseBody.exists()).as("Response %s was not created from response definition", response).isTrue();
        assertThat(responseBody.$("em").getText()).as("Response %s was not created from response definition", response, definition).isEqualTo(definition);
    }

    @Then("^check that description is \"([^\"]*)\" for response \"([^\"]*)\"$")
    public void checkThatDescriptionIsForResponse(String expectedDescription, String response) {
        OperationUtils.selectResponse(response);
        String description = OperationUtils.getOperationRoot().$(OperationElements.RESPONSES_SECTION).$(By.className("response-description")).$(By.className("grow")).getText();
        assertThat(description).as("Checking description:").isEqualTo(expectedDescription);
    }

    @Then("^check that exist request body$")
    public void checkThatExistRequestBody() {
        ElementsCollection collapsedSection = OperationUtils.getOperationRoot().$(OperationElements.REQUEST_BODY_SECTION).$$("a").filter(attribute("class", "collapsed"));
        if (collapsedSection.size() > 0) {
            collapsedSection.first().click();
        }

        ElementsCollection rb = OperationUtils.getOperationRoot().$(OperationElements.REQUEST_BODY_SECTION).$$(By.className("request-body-type"));
        assertThat(rb.size()).as("Request body is not created!").isEqualTo(1);
    }

    @Then("^check that request body description is \"([^\"]*)\"$")
    public void checkThatRequestBodyDescriptionIs(String expectedDescription) {
        String description = OperationUtils.getOperationRoot().$(OperationElements.REQUEST_BODY_SECTION).$("inline-markdown-editor").getText();
        assertThat(description).as("Request body description should be %s but is %s", expectedDescription, description).isEqualTo(expectedDescription);
    }

    @Then("^check that operation security requirement \"([^\"]*)\" \"([^\"]*)\" created$")
    public void checkThatOperationSecurityRequirementExist(String requirement, String isCreated) {        //TODO same method as in MainPageVerifications --> join
        ElementsCollection requirementList = OperationUtils.getOperationRoot().$(OperationElements.REQUIREMENTS_SECTION).$$(By.className("security-requirement")).filter(exactText(requirement));
        if ("is".equals(isCreated)) {
            assertThat(requirementList.size()).as("Requirement %s do not exists, and should", requirement).isEqualTo(1);
        } else {
            assertThat(requirementList.size()).as("Requirement %s exists, and should not", requirement).isEqualTo(0);
        }
    }

    @Then("^check that path parameter \"([^\"]*)\" is created in operation")
    public void checkThatPathParameterIsCreatedInOperation(String parameter) {
        SelenideElement parameterElement = OperationUtils.getOperationRoot().$(OperationElements.PATH_PARAMETERS_SECTION).
            $$(OperationElements.PATH_PARAMETERS_ROW).filter(matchText(parameter)).first();

        assertThat(parameterElement.$("div").getAttribute("class")).as("Path parameter %s is not created", parameter).doesNotContain("missing");
    }
}
