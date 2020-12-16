package apicurito.tests.steps.verification;

import apicurito.tests.utils.slenide.CommonUtils;
import apicurito.tests.utils.slenide.MainPageUtils;
import apicurito.tests.utils.slenide.PathUtils;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import io.cucumber.java.en.Then;
import org.openqa.selenium.By;

import static com.codeborne.selenide.Condition.matchText;
import static com.codeborne.selenide.Condition.text;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class PathVerifications {

    private static class PathElements {
        private static By PATH_PARAMETERS_SECTION = By.cssSelector("path-params-section");
        private static By PATH_PARAMETERS_ROW = By.cssSelector("path-param-row");
    }

    @Then("^check that operation \"([^\"]*)\" is created for path \"([^\"]*)\"$")
    public void checkThatOperationIsCreatedForPath(String operation, String path) {
        checkThatPathIsCreatedAndSelectIt(path);
        ElementsCollection ec = PathUtils.getPathPageRoot().$$(By.cssSelector("div." + operation.toLowerCase() + "-tab.enabled"));
        assertThat(ec.size() == 1).as("Operation %s is not created", operation).isTrue();
    }

    @Then("^check that path parameter \"([^\"]*)\" is created for path \"([^\"]*)\"$")
    public void checkThatPathParameterIsCreatedForPath(String parameter, String path) {
        checkThatPathIsCreatedAndSelectIt(path);
        SelenideElement parameterElement = PathUtils.getPathPageRoot().$(PathElements.PATH_PARAMETERS_SECTION).$$(PathElements.PATH_PARAMETERS_ROW).filter(matchText(parameter)).first();
        assertThat(parameterElement.$("div").getAttribute("class")).as("Path parameter %s is not created", parameter).doesNotContain("missing");
    }

    /**
     * @param page can only have values "path" and "operation"
     */
    @Then("^check that path parameter \"([^\"]*)\" on \"([^\"]*)\" page has description \"([^\"]*)\"$")
    public void checkThatPathParameterHasDescription(String parameter, String page, String description) {
        SelenideElement descriptionElement = PathUtils.getRoot(page).$(PathElements.PATH_PARAMETERS_SECTION).$$(PathElements.PATH_PARAMETERS_ROW)
                .filter(matchText(parameter)).first().$(By.className("description"));

        assertThat(descriptionElement.getText()).as("Description for path parameter %s is different", parameter).isEqualTo(description);
    }

    /**
     * @param page can only have values "path" and "operation"
     */
    @Then("^check that path parameter \"([^\"]*)\" on \"([^\"]*)\" page has \"([^\"]*)\" type with value \"([^\"]*)\"$")
    public void checkThatPathParameterHasTypeAs(String parameter, String page, String type, String expectedAs) {
        SelenideElement root = PathUtils.getRoot(page);
        PathUtils.openPathTypes(parameter,root);
        String as = root.$(PathElements.PATH_PARAMETERS_SECTION).$$(PathElements.PATH_PARAMETERS_ROW)
                .filter(text(parameter)).first().$(CommonUtils.getButtonId(type)).getText();
        assertThat(as).as("%s is %s but should be %s", type, as, expectedAs).isEqualTo(expectedAs);
    }

    private void checkThatPathIsCreatedAndSelectIt(String path){
        SelenideElement pathElement = MainPageUtils.getPathWithName(path);
        if (pathElement == null) {
            fail("Operation is not created because path %s is not found.", path);
        } else {
            pathElement.click();
        }
    }
}
