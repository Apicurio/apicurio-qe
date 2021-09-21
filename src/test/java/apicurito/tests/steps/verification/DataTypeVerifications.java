package apicurito.tests.steps.verification;

import static com.codeborne.selenide.Condition.text;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import org.openqa.selenium.By;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;

import apicurito.tests.steps.DataTypeSteps;
import apicurito.tests.utils.slenide.CommonUtils;
import apicurito.tests.utils.slenide.DataTypeUtils;
import io.cucumber.java.en.Then;

public class DataTypeVerifications {

    private static class DataTypesElements {
        private static By INFO_SECTION = By.cssSelector("definition-info-section");
        private static By EXAMPLE_SECTION = By.cssSelector("definition-example-section");

        private static By PROPERTY_ROW = By.cssSelector("property-row");
        private static By DESCRIPTION = By.className("description");
    }

    @Then("^check that data type description is \"([^\"]*)\"$")
    public void checkThatDataTypeDescriptionIs(String expectedDescription) {
        String description = DataTypeUtils.getDataTypesRoot().$(DataTypesElements.INFO_SECTION).$(DataTypesElements.DESCRIPTION).getText();
        assertThat(description).as("Description should be %s but is %s", expectedDescription, description).isEqualTo(expectedDescription);
    }

    @Then("^check that data type property \"([^\"]*)\" is created$")
    public void checkThatDataTypePropertyIsCreated(String expectedProperty) {
        ElementsCollection properties = DataTypeSteps.PROPERTIES_SECTION.$$(DataTypesElements.PROPERTY_ROW);
        for (SelenideElement property : properties) {
            String name = property.$(By.className("name")).getText();
            if (name.equals(expectedProperty)) {
                return;
            }
        }
        fail("Property with name %s is not created", expectedProperty);
    }

    @Then("^check that description is \"([^\"]*)\" for property \"([^\"]*)\"$")
    public void checkThatDescriptionIsForProperty(String expectedDescription, String property) {
        DataTypeUtils.openPropertyDescription(property);
        String description = DataTypeSteps.PROPERTIES_SECTION.$$(DataTypesElements.PROPERTY_ROW).filter(Condition.text(property)).first().$(DataTypesElements.DESCRIPTION).getText();
        assertThat(description).as("Description of property %s should be %s but is %s", property, expectedDescription, description).isEqualTo(expectedDescription);
    }

    @Then("^check that \"([^\"]*)\" is \"([^\"]*)\" for property \"([^\"]*)\"$")
    public void checkThatTypeAsIsForProperty(String subject, String expectedSubjectValue, String property) {
        String buttonId = CommonUtils.getButtonId(subject);

        DataTypeUtils.openPropertyTypes(property);
        String subjectValue = DataTypeSteps.PROPERTIES_SECTION.$$(DataTypesElements.PROPERTY_ROW)
            .filter(text(property)).first().$(buttonId).getText();
        assertThat(subjectValue).as("%s is %s but should be %s for data type property %s", subject, subjectValue, expectedSubjectValue, property).isEqualTo(expectedSubjectValue);
    }

    @Then("^check that example is (.*)$")
    public void checkThatExampleIsRegEx(String expectedExample) {
        String example = DataTypeUtils.getDataTypesRoot().$(DataTypesElements.EXAMPLE_SECTION).$(By.className("example")).getText();
        assertThat(example.replaceAll("\\s", "")).as("Example should be %s but is %s", expectedExample, example).isEqualTo(expectedExample.replaceAll("\\s", ""));
    }
}
