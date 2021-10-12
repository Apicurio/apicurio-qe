package apicurito.tests.steps;

import org.openqa.selenium.By;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;

import apicurito.tests.utils.slenide.CommonUtils;
import apicurito.tests.utils.slenide.DataTypeUtils;
import io.cucumber.java.en.When;

public class DataTypeSteps {

    public static SelenideElement PROPERTIES_SECTION = DataTypeUtils.getDataTypesRoot().$$("section")
        .filter(Condition.attribute("label", "PROPERTIES")).first();

    private static class DataTypesElements {
        private static By INFO_SECTION = By.cssSelector("definition-info-section");
        private static By EXAMPLE_SECTION = By.cssSelector("definition-example-section");
    }

    @When("^create data type property with name \"([^\"]*)\"$")
    public void createDataTypeProperty(String property) {
        CommonUtils.getClickableLink(CommonUtils.Sections.PROPERTIES, PROPERTIES_SECTION).click();
        CommonUtils.getLabelWithName("name", CommonUtils.getAppRoot().$("property-editor")).sendKeys(property);
        CommonUtils.getButtonWithText("Save", CommonUtils.getAppRoot().$("property-editor")).click();
    }

    @When("^set description \"([^\"]*)\" for data type property \"([^\"]*)\"$")
    public void setDescriptionForProperty(String description, String property) {
        DataTypeUtils.openPropertyDescription(property);
        CommonUtils.setValueInTextArea(description, PROPERTIES_SECTION);
    }

    @When("^set data type description \"([^\"]*)\"$")
    public void setDataTypeDescription(String description) {
        CommonUtils.setValueInTextArea(description, DataTypeUtils.getDataTypesRoot().$(DataTypesElements.INFO_SECTION));
    }

    @When("^set data type example \"([^\"]*)\"$")
    public void setDataTypeExample(String example) {
        CommonUtils.setValueInTextArea(example, DataTypeUtils.getDataTypesRoot().$(DataTypesElements.EXAMPLE_SECTION));
    }

    @When("^set property \"([^\"]*)\" as \"([^\"]*)\" for property \"([^\"]*)\"$")
    public void setPropertyAsForProperty(String subject, String subjectValue, String property) {
        String buttonId = CommonUtils.getButtonId(subject);
        DataTypeUtils.openPropertyTypes(property);
        CommonUtils.setDropDownValue(buttonId, subjectValue, PROPERTIES_SECTION);
    }
}
