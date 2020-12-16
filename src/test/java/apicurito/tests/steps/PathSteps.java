package apicurito.tests.steps;

import apicurito.tests.utils.slenide.CommonUtils;
import apicurito.tests.utils.slenide.OperationUtils;
import apicurito.tests.utils.slenide.PathUtils;
import com.codeborne.selenide.SelenideElement;
import io.cucumber.java.en.When;
import org.openqa.selenium.By;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Condition.enabled;
import static com.codeborne.selenide.Condition.attribute;
import static com.codeborne.selenide.Condition.text;

public class PathSteps {

    private static class PathElements {
        private static By PARAMETERS_SECTION = By.cssSelector("path-params-section");
        private static By PATH_PARAMETERS_ROW = By.cssSelector("path-param-row");
    }

    /**
     * @param operation must be convertable to operation enum. See Operations enum
     */
    @When("^create new \"([^\"]*)\" operation$")
    public void createNewOperation(String operation) {
        PathUtils.getCreateOperationButton(Operations.valueOf(operation))
                .click();
    }

    @When("^select operation \"([^\"]*)\"$")
    public void selectOperation(String operation) {
        PathUtils.getOperationButton(Operations.valueOf(operation), CommonUtils.getAppRoot().shouldBe(visible, enabled).shouldNotHave(attribute("disabled")))
                .click();
    }

    /**
     * @param page can only have values "path" and "operation"
     */
    @When("^create path parameter \"([^\"]*)\" on \"([^\"]*)\" page")
    public void createPathParameter(String parameter, String page) {
        PathUtils.createPathParameter(parameter, PathUtils.getRoot(page));
    }

    /**
     * @param page can only have values "path" and "operation"
     */
    @When("^set description \"([^\"]*)\" for path parameter \"([^\"]*)\" on \"([^\"]*)\" page")
    public void setDescriptionPathParameter(String description, String parameter, String page) {
        SelenideElement root = PathUtils.getRoot(page);
        PathUtils.openPathDescription(parameter, PathUtils.getRoot(page));
        CommonUtils.setValueInTextArea(description, root.$(PathElements.PARAMETERS_SECTION));
    }

    /**
     * @param page can only have values "path" and "operation"
     */
    @When("^set path parameter type \"([^\"]*)\" for path parameter \"([^\"]*)\" on \"([^\"]*)\" page")
    public void setPathParameterTypeForPathParameter(String type, String parameter, String page) {
        SelenideElement root = PathUtils.getRoot(page);
        PathUtils.openPathTypes(parameter, root);
        SelenideElement parameterElement = root.$(PathElements.PARAMETERS_SECTION).$$(PathElements.PATH_PARAMETERS_ROW)
                .filter(text(parameter)).first();

        CommonUtils.setDropDownValue(CommonUtils.DropdownButtons.PROPERTY_TYPE.getButtonId(), type, parameterElement);
    }

    @When("^set path parameter type of \"([^\"]*)\" for path parameter \"([^\"]*)\" on \"([^\"]*)\" page")
    public void setPathParameterTypeOfForPathParameter(String of, String parameter, String page) {
        SelenideElement root = PathUtils.getRoot(page);
        PathUtils.openPathTypes(parameter, root);
        SelenideElement parameterElement = root.$(PathElements.PARAMETERS_SECTION).$$(PathElements.PATH_PARAMETERS_ROW)
                .filter(text(parameter)).first();

        CommonUtils.setDropDownValue(CommonUtils.DropdownButtons.PROPERTY_TYPE_OF.getButtonId(), of, parameterElement);
    }

    /**
     * @param page can only have values "path" and "operation"
     */
    @When("^set path parameter type as \"([^\"]*)\" for path parameter \"([^\"]*)\" on \"([^\"]*)\" page")
    public void setPathParameterTypeAsForPathParameter(String as, String parameter, String page) {
        SelenideElement root = PathUtils.getRoot(page);
        PathUtils.openPathTypes(parameter, root);
        SelenideElement parameterElement = root.$(PathElements.PARAMETERS_SECTION).$$(PathElements.PATH_PARAMETERS_ROW)
                .filter(text(parameter)).first();

        CommonUtils.setDropDownValue(CommonUtils.DropdownButtons.PROPERTY_TYPE_AS.getButtonId(), as, parameterElement);
    }

    public enum Operations {
        GET,
        PUT,
        POST,
        DELETE,
        OPTIONS,
        HEAD,
        PATCH,
        TRACE
    }
}
