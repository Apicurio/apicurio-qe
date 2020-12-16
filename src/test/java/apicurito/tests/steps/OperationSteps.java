package apicurito.tests.steps;

import static com.codeborne.selenide.Condition.attribute;
import static com.codeborne.selenide.Condition.enabled;
import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;

import org.openqa.selenium.By;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;

import java.util.List;

import apicurito.tests.utils.slenide.CommonUtils;
import apicurito.tests.utils.slenide.MainPageUtils;
import apicurito.tests.utils.slenide.OperationUtils;
import apicurito.tests.utils.slenide.PathUtils;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.When;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OperationSteps {

    public static class OperationElements {
        private static By DESCRIPTION = By.className("description");
        private static By A = By.cssSelector("a");

        private static By RESPONSE_SECTION = By.cssSelector("responses-section");
        private static By REQUEST_BODY_SECTION = By.cssSelector("requestbody-section");
        private static By REQUIREMENTS_SECTION = By.cssSelector("security-requirements-section");

        public static By getDescription() {
            return DESCRIPTION;
        }
    }

    @When("^set operation summary \"([^\"]*)\"$")
    public void setSummary(String summary) {
        CommonUtils.setValueInLabel(summary, OperationUtils.getOperationRoot().$(By.className("summary")), false);
    }

    @When("^set operation id \"([^\"]*)\"$")
    public void setOperationId(String operationId) {
        CommonUtils.setValueInLabel(operationId, OperationUtils.getOperationRoot().$(By.className("operationId")), false);
    }

    @When("^set operation description \"([^\"]*)\"$")
    public void setDescription(String description) {
        CommonUtils.setValueInTextArea(description, OperationUtils.getOperationRoot().$(OperationElements.DESCRIPTION));
    }

    @When("^set operation tags \"([^\"]*)\"$")
    public void setTags(String tags) {
        CommonUtils.setValueInLabel(tags, OperationUtils.getOperationRoot().$(By.className("tags")), true);
    }

    /**
     * Data table params: response code --> 200, 404, ...
     *                    response definition
     *                    boolean true  --> create with plus sign
     *                            false --> create with link
     */
    @When("set response with plus sign")
    public void setResponseWithPlusSign(DataTable table) {
        for (List<String> dataRow : table.cells()) {
            if (Boolean.valueOf(dataRow.get(2))){
                CommonUtils.getNewPlusSignButton(CommonUtils.Sections.RESPONSE, OperationUtils.getOperationRoot().shouldBe(visible, enabled).shouldNotHave(attribute("disabled")))
                        .click();
            }else{
                CommonUtils.getClickableLink(CommonUtils.Sections.RESPONSE, OperationUtils.getOperationRoot().shouldBe(visible, enabled).shouldNotHave(attribute("disabled")))
                        .click();
            }
            OperationUtils.setResponseDetails(dataRow.get(0), dataRow.get(1));
        }
    }

    @When("^set response description \"([^\"]*)\" for response \"([^\"]*)\"$")
    public void setDescriptionForResponse(String description, String response) {
        OperationUtils.selectResponse(response);
        CommonUtils.setValueInTextArea(description, OperationUtils.getOperationRoot().$(OperationElements.RESPONSE_SECTION));
    }

    @When("^override parameter \"([^\"]*)\"$")
    public void overrideParameter(String parameter) {
        OperationUtils.overrideParameter(parameter);
    }

    /**
     * Table parameters:
     * Type of dropdown                            --> type | of | as | required
     * Value of dropdown                           --> Array | String | Integer | ...
     * Page where dropdown is located              --> path | operations | datatypes
     * Section where dropdown is located           --> query | header | response | request body
     * Boolean if dropdown is in Responses section --> true it is , false otherwise
     * Number of response                          --> 100 | 200 | 404 | ...
     */
    @When("set parameters types")
    public void setParametersTypes(DataTable table) {       //TODO  add specific parameter when it will be needed

        for (List<String> dataRow : table.cells()) {
            String buttonId = CommonUtils.getButtonId(dataRow.get(0));
            SelenideElement page = CommonUtils.getPageElement(dataRow.get(2));
            By section = CommonUtils.getSectionBy(dataRow.get(3));

            if (Boolean.valueOf(dataRow.get(4))) {
                OperationUtils.selectResponse(dataRow.get(5));
            } else {
                CommonUtils.openCollapsedSection(page, section);
            }
            CommonUtils.setDropDownValue(buttonId, dataRow.get(1), page.$(section));
        }
    }

    @When("^override consumes or produces \"([^\"]*)\" with \"([^\"]*)\" for operation \"([^\"]*)\"$")
    public void overrideProducesWithForOperation(String consumesProduces, String values, String operation) {
        SelenideElement subsection = OperationUtils.getOperationRoot().$(By.className(consumesProduces));

        PathUtils.getOperationButton(PathSteps.Operations.valueOf(operation), OperationUtils.getOperationRoot())
                .click();

        CommonUtils.getButtonWithText("Override", subsection)
                .click();
        CommonUtils.getLabelWithType("text", subsection).setValue(values);
        CommonUtils.getButtonWithTitle("Save changes.", subsection).click();
    }

    @When("^delete \"([^\"]*)\" operation$")
    public void deleteOperation(String operation) {
        PathUtils.getOperationButton(PathSteps.Operations.valueOf(operation), OperationUtils.getOperationRoot().shouldBe(visible, enabled).shouldNotHave(attribute("disabled")))
                .click();
        OperationUtils.deleteOperation();
    }

    @When("^create request body$")
    public void createRequestBody() {
        CommonUtils.openCollapsedSection(OperationUtils.getOperationRoot(), OperationElements.REQUEST_BODY_SECTION);
        OperationUtils.getOperationRoot().$(OperationElements.REQUEST_BODY_SECTION).$$(OperationElements.A)
                .filter(text("Add a request body")).shouldHaveSize(1).first()   //TODO refactor with enum Sections
                .shouldBe(visible).click();
    }

    @When("^set request body description \"([^\"]*)\"$")
    public void setRequestBodyDescription(String description) {
        CommonUtils.setValueInTextArea(description, OperationUtils.getOperationRoot().$(OperationElements.REQUEST_BODY_SECTION));
    }

    @When("^override security requirements in operation with$")
    public void overrideSecurityRequirementsInOperationWith(DataTable table) {      //TODO same method as in MainPageSteps --> join
        CommonUtils.getNewPlusSignButton(CommonUtils.Sections.REQUIREMENT, MainPageUtils.getMainPageRoot().$(OperationElements.REQUIREMENTS_SECTION))
                .click();
        SelenideElement requirementEditor = MainPageUtils.getMainPageRoot().$("security-requirement-editor");

        for (List<String> dataRow : table.cells()) {
            ElementsCollection listOfSchemes = requirementEditor.$$(By.className("list-group-item"));
            for (SelenideElement scheme : listOfSchemes) {
                if (scheme.$(By.className("name")).getText().equals(dataRow.get(0)) && !scheme.getAttribute("class").contains("active")) {
                    scheme.$("input").click();
                    break;
                }
            }
        }
        CommonUtils.getButtonWithText("Save", requirementEditor)
                .click();
    }
}
