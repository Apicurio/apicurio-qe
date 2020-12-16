package apicurito.tests.utils.slenide;

import static org.assertj.core.api.Assertions.assertThat;

import static com.codeborne.selenide.Condition.attribute;
import static com.codeborne.selenide.Condition.enabled;
import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

import org.openqa.selenium.By;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;

import apicurito.tests.steps.PathSteps;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PathUtils {

    private static class PathElements {
        private static By PATH_PARAMETERS_ROW = By.cssSelector("path-param-row");
    }

    public static SelenideElement getPathPageRoot() {
        return $("path-form").shouldBe(visible);
    }

    public static SelenideElement getCreateOperationButton(PathSteps.Operations operation) {
        log.info("searching for add operation button for operation {}", operation);

        ElementsCollection operations = getPathPageRoot().$$("div").filter(attribute("class", "operation-tab " + operation.toString().toLowerCase() + "-tab"));
        if (operations.size() > 0) {     //if size is 0, operation tab is already selected
            operations.first().click();
        } else {
            //Check that selected operation is ours $operation
            assertThat(getPathPageRoot().$$("div").filter(attribute("class", "operation-tab " + operation.toString().toLowerCase() + "-tab selected")).size()).isEqualTo(1);
        }
        return CommonUtils.getButtonWithText("Add Operation", getPathPageRoot());
    }

    public static SelenideElement getOperationButton(PathSteps.Operations operation, SelenideElement differentRoot) {
        log.info("searching for operation button for operation {}", operation);

        return differentRoot.$(By.className(operation.toString().toLowerCase() + "-tab")).waitUntil(visible, 5000).shouldBe(enabled);
    }

    public static void createPathParameter(String parameter, SelenideElement root) {
        CommonUtils.getButtonWithText("Create", root.$$(PathElements.PATH_PARAMETERS_ROW)
            .filter(text(parameter)).first()).click();
    }

    public static void openPathDescription(String parameter, SelenideElement root) {
        ElementsCollection elements = root.$$(PathElements.PATH_PARAMETERS_ROW)
                .filter(text(parameter)).first()
                .$$("div").filter(attribute("class", "description"));
        if (elements.size() == 1) {
            elements.first().click();
        }
    }

    public static void openPathTypes(String parameter, SelenideElement root) {
        ElementsCollection elements = root.$$(PathElements.PATH_PARAMETERS_ROW)
                .filter(text(parameter)).first()
                .$$("div").filter(attribute("class", "summary"));
        if (elements.size() == 1) {
            elements.first().click();
        }
    }

    public static SelenideElement getRoot(String page) {
        return page.equals("path") ? PathUtils.getPathPageRoot() : OperationUtils.getOperationRoot();
    }
}
