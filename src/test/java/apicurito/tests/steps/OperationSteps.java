package apicurito.tests.steps;

import apicurito.tests.utils.slenide.CommonUtils;
import apicurito.tests.utils.slenide.OperationUtils;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import cucumber.api.java.en.And;
import cucumber.api.java.en.When;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.transport.Transport;
import org.openqa.selenium.By;

import static com.codeborne.selenide.Condition.*;


@Slf4j
public class OperationSteps {

    private static By SUMMARY_SUBSECTION = By.className("summary");
    private static By OPERATION_ID_SUBSECTION = By.className("operationId");
    private static By DESCRIPTION_SUBSECTION = By.className("description");
    private static By TAGS_SUBSECTION = By.className("tags");
    private static By RESPONSE_SECTION = By.cssSelector("responses-section");

    @When("^set summary \"([^\"]*)\"$")
    public void setSummary(String summary){
        CommonUtils.setValueInLabel(summary, OperationUtils.getOperationRoot().$(SUMMARY_SUBSECTION), false);
    }

    @And("^set operation id \"([^\"]*)\"$")
    public void setOperationId(String operationId){
        CommonUtils.setValueInLabel(operationId, OperationUtils.getOperationRoot().$(OPERATION_ID_SUBSECTION), false);
    }

    @And("^set description \"([^\"]*)\"$")
    public void setDescription(String description){
        CommonUtils.setValueInTextArea(description, OperationUtils.getOperationRoot().$(DESCRIPTION_SUBSECTION));
    }

    @And("^set tags \"([^\"]*)\"$")
    public void setTags(String tags) {
        CommonUtils.setValueInLabel(tags, OperationUtils.getOperationRoot().$(TAGS_SUBSECTION), true);
    }

    @And("^set response (\\d+) with clickable link$")
    public void setResponseWithLink(Integer response){
        CommonUtils.getClickableLink(CommonUtils.Sections.RESPONSE, CommonUtils.getAppRoot().shouldBe(visible,enabled).shouldNotHave(attribute("disabled")))
                .click();
        OperationUtils.setResponseStatusCode(response);
    }

    @And("^set response (\\d+) with plus sign$")
    public void setResponseWithPlusSign(Integer response){
        CommonUtils.getNewPlusSignButton(CommonUtils.Sections.RESPONSE, CommonUtils.getAppRoot().shouldBe(visible,enabled).shouldNotHave(attribute("disabled")))
                .click();
        OperationUtils.setResponseStatusCode(response);
    }

    @And("^set response description \"([^\"]*)\" for response (\\d+)$")
    public void setDescriptionForResponse(String description, Integer response){
        OperationUtils.selectResponse(response);
        CommonUtils.setValueInTextArea(description, OperationUtils.getOperationRoot().$(RESPONSE_SECTION));
    }

    @And("^override parameter \"([^\"]*)\"$")
    public void overrideParameter(String parameter) {
        OperationUtils.overrideParameter(parameter);
    }

    @And("^set description \"([^\"]*)\" for override path parameter \"([^\"]*)\" in operation$")
    public void setDescriptionForOverridePathParameterInOperation(String description, String parameter) {
        log.info("Setting description {} for overriden parameter {}", description, parameter);

        SelenideElement parameterElement = OperationUtils.getOperationRoot().$("path-params-section")
                .$$("path-param-row").filter(text(parameter)).first();

        parameterElement.$(By.className("description")).click();

        CommonUtils.setValueInTextArea(description, parameterElement);
    }

    @And("^set response type \"([^\"]*)\" for response (\\d+)$")
    public void setResponseType(String type, Integer response) {
        OperationUtils.selectResponse(response);
        CommonUtils.setDropDownValue(CommonUtils.DropdownButtons.TYPE.getButtonId(), type, OperationUtils.getOperationRoot().$(RESPONSE_SECTION));
    }

    @And("^set response type of \"([^\"]*)\" for response (\\d+)$")
    public void setResponseTypeOf(String of, Integer response) {
        OperationUtils.selectResponse(response);
        CommonUtils.setDropDownValue(CommonUtils.DropdownButtons.TYPE_OF.getButtonId(), of, OperationUtils.getOperationRoot().$(RESPONSE_SECTION));
    }

    @And("^set response type as \"([^\"]*)\" for response (\\d+)$")
    public void setResponseTypeAs(String as, Integer response) {
        OperationUtils.selectResponse(response);
        CommonUtils.setDropDownValue(CommonUtils.DropdownButtons.TYPE_AS.getButtonId(), as, OperationUtils.getOperationRoot().$(RESPONSE_SECTION));
    }

    @When("^delete current operation$")
    public void deleteOperation() {
        OperationUtils.deleteOperation();
    }


    @When("^create request body$")
    public void createRequestBody() throws Throwable {
        // TODO: refactor this to use OperationUtils or whatever
        Selenide.$(".requestBody-section").$$("a")
                .filter(text("Add a request body")).shouldHaveSize(1).first()
                .shouldBe(visible).click();
    }

    @When("^set body description \"([^\"]*)\"$")
    public void setBodyDescription(String description) throws Throwable {
        CommonUtils.setValueInTextArea(description, OperationUtils.getOperationRoot().$(".requestBody-section"));
    }

    @When("^set body response type \"([^\"]*)\" of \"([^\"]*)\" as \"([^\"]*)\"$")
    public void setBodyResponseTypeOfAs(String type, String ofType, String asType) throws Throwable {
        // TODO: refactor this to use OperationUtils or whatever
        CommonUtils.setDropDownValue(CommonUtils.DropdownButtons.TYPE.getButtonId(),
                type, OperationUtils.getOperationRoot().$(".requestBody-section"));
        if (!ofType.isEmpty()) {
            CommonUtils.setDropDownValue(CommonUtils.DropdownButtons.TYPE_OF.getButtonId(),
                    ofType, OperationUtils.getOperationRoot().$(".requestBody-section"));
        }
        if (!asType.isEmpty()) {
            CommonUtils.setDropDownValue(CommonUtils.DropdownButtons.TYPE_AS.getButtonId(),
                    asType, OperationUtils.getOperationRoot().$(".requestBody-section"));
        }
    }
}