package apicurito.tests.steps.verification;

import static com.codeborne.selenide.Condition.attribute;
import static com.codeborne.selenide.Condition.exactText;
import static com.codeborne.selenide.Condition.text;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import org.openqa.selenium.By;

import com.codeborne.selenide.ElementsCollection;

import java.util.List;

import apicurito.tests.utils.slenide.CommonUtils;
import apicurito.tests.utils.slenide.MainPageUtils;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;

public class MainPageVerifications {

    private static class MainPageElements {
        private static By TITLE_BAR = By.cssSelector("title-bar");
        private static By H1 = By.cssSelector("h1");

        private static By INFO_SECTION = By.cssSelector("info-section");
        private static By CONTACT_SECTION = By.cssSelector("contact-section");
        private static By LICENSE_SECTION = By.cssSelector("license-section");
        private static By TAGS_SECTION = By.cssSelector("tags-section");
        private static By SECURITY_SECTION = By.cssSelector("security-schemes-section");
        private static By REQUIREMENTS_SECTION = By.cssSelector("security-requirements-section");

        private static By DESCRIPTION = By.className("description");
        private static By SECTION = By.cssSelector("section");
    }

    @Then("^check that API name is \"([^\"]*)\"$")
    public void checkThatAPINameIs(String expectedName) {
        String apiName = CommonUtils.getAppRoot().$(MainPageElements.TITLE_BAR).$(MainPageElements.H1).getText();
        assertThat(apiName).as("Checking API name:").isEqualTo(expectedName);
    }

    @Then("^check that API version is \"([^\"]*)\"$")
    public void checkThatAPIVersionIs(String expectedVersion) {
        openMainPage();
        String version = MainPageUtils.getMainPageRoot().$(MainPageElements.INFO_SECTION).$(By.className("version")).getText();
        assertThat(version).as("Checking API version:").isEqualTo(expectedVersion);
    }

    @Then("^check that API description is \"([^\"]*)\"$")
    public void checkThatAPIDescriptionIs(String expectedDescription) {
        openMainPage();
        String description = MainPageUtils.getMainPageRoot().$(MainPageElements.INFO_SECTION).$(MainPageElements.DESCRIPTION).getText();
        assertThat(description).as("Checking API descritpion:").isEqualTo(expectedDescription);
    }

    @Then("^check that API contact info is$")
    public void checkThatAPIContactInfoIs(DataTable table) {
        openMainPage();
        for (List<String> dataRow : table.cells()) {
            String name = MainPageUtils.getMainPageRoot().$(MainPageElements.CONTACT_SECTION).$(By.className("name")).getText();
            assertThat(name).as("Checking API contact name:").isEqualTo(dataRow.get(0));

            String email = MainPageUtils.getMainPageRoot().$(MainPageElements.CONTACT_SECTION).$(By.className("email")).getText();
            assertThat(email).as("Checking API contact email:").isEqualTo(dataRow.get(1));

            String url = MainPageUtils.getMainPageRoot().$(MainPageElements.CONTACT_SECTION).$(By.className("url")).getText();
            assertThat(url).as("Checking API contact url:").isEqualTo(dataRow.get(2));
        }
    }

    @Then("^check that API license is \"([^\"]*)\"$")
    public void checkThatAPILicenseIs(String expectedLincense) {
        openMainPage();
        ElementsCollection licenses = MainPageUtils.getMainPageRoot().$(MainPageElements.LICENSE_SECTION).$$("button").filter(text("Change License"));
        if (licenses.size() == 1) {
            String license = MainPageUtils.getMainPageRoot().$(MainPageElements.LICENSE_SECTION).$("h2").getText();
            assertThat(license).as("Checking API linces:").isEqualTo(expectedLincense);
        } else {
            fail("License is not set!");
        }
    }

    @Then("^check that API have tag \"([^\"]*)\" with description \"([^\"]*)\"$")
    public void checkThatAPIHaveTagWithDescription(String expectedTag, String expectedDescription) {
        openMainPage();
        ElementsCollection tagRows = MainPageUtils.getMainPageRoot().$(MainPageElements.TAGS_SECTION).$$("tag-row");

        if (tagRows.size() > 0) {
            ElementsCollection tag = MainPageUtils.getMainPageRoot().$(MainPageElements.TAGS_SECTION).$$(By.className("name")).filter((text(expectedTag)));

            if (tag.size() == 0) {
                fail("Tag %s is not created!", expectedTag);
            } else {
                String desc = tag.first().parent().$(MainPageElements.DESCRIPTION).getText();
                assertThat(desc).as("Checking tag description:").isEqualTo(expectedDescription);
            }
        } else {
            fail("Tags are not created!");
        }
    }

    /**
     * @param expectedPathName expected name of path
     * @param isCreated        [ is | is not ] is for cheack that path exists, is not otherwise
     */
    @Then("^check that path \"([^\"]*)\" \"([^\"]*)\" created$")
    public void checkThatPathIsCreated(String expectedPathName, String isCreated) {
        CommonUtils.sleepFor(1); //need to wait at least for a second because of Stale Element Reference Exception

        ElementsCollection paths = MainPageUtils.getMainPageRoot().$$(MainPageElements.SECTION).filter(attribute("label", "Paths")).first()
            .$$(By.className("api-path")).filter(exactText(expectedPathName));

        if ("is".equals(isCreated)) {
            assertThat(paths.size()).as("Path %s is not created (should be)", expectedPathName).isEqualTo(1);
        } else {
            assertThat(paths.size()).as("Path %s is created (should not be)", expectedPathName).isEqualTo(0);
        }
    }

    /**
     * @param datatype  expected name of data type
     * @param isCreated [ is | is not ] is for cheack that path exists, is not otherwise
     */
    @Then("^check that data type \"([^\"]*)\" \"([^\"]*)\" created$")
    public void checkThatDataTypeIsCreated(String datatype, String isCreated) {
        ElementsCollection types = MainPageUtils.getMainPageRoot().$$(MainPageElements.SECTION).filter(attribute("label", "Data Types")).first()
            .$$(By.className("api-definition")).filter(exactText(datatype));
        if ("is".equals(isCreated)) {
            assertThat(types.size()).as("Data type %s is not created! (should be)", datatype).isEqualTo(1);
        } else {
            assertThat(types.size()).as("Data type %s is created! (should not be)", datatype).isEqualTo(0);
        }
    }

    /**
     * @param response  expected name of data type
     * @param isCreated [ is | is not ] is for cheack that path exists, is not otherwise
     */
    @Then("^check that response \"([^\"]*)\" \"([^\"]*)\" created$")
    public void checkThatResponseIsCreated(String response, String isCreated) {
        ElementsCollection responses = MainPageUtils.getMainPageRoot().$$(MainPageElements.SECTION).filter(attribute("label", "Responses")).first()
            .$$(By.className("api-response")).filter(exactText(response));
        if ("is".equals(isCreated)) {
            assertThat(responses.size()).as("Response %s is not created! (should be)", response).isEqualTo(1);
        } else {
            assertThat(responses.size()).as("Response %s is created! (should not be)", response).isEqualTo(0);
        }
    }

    /**************************************************************
     ********** SECURITY SUBSECTION verification steps ************
     **************************************************************/

    /**
     * Currently is supported just name and description without editing scheme.
     * #TODO after closing: https://github.com/Apicurio/apicurio-studio/issues/656
     */
    @Then("^check security scheme$")
    public void checkSecurityScheme(DataTable table) {
        openMainPage();
        for (List<String> dataRow : table.cells()) {
            ElementsCollection schemeElements = MainPageUtils.getMainPageRoot().$(MainPageElements.SECURITY_SECTION).$$("security-scheme-row").filter(text(dataRow.get(0))).filter(text(dataRow.get(1)));
            assertThat(schemeElements.size()).as("Scheme with name %s do not exist", dataRow.get(0)).isEqualTo(1);
            if (dataRow.get(2) != null) {
                String description = schemeElements.first().$(MainPageElements.DESCRIPTION).getText();
                assertThat(description).as("Description should be %s but is %s", dataRow.get(2), description).isEqualTo(dataRow.get(2));
            }
        }
    }

    /**
     * TEMPORARY method implement this into method above after closing https://github.com/Apicurio/apicurio-studio/issues/656
     */
    @Then("check that scheme {string} {string} {string} created")
    public void checkThatSchemeCreated(String schemeName, String schemeType, String isCreated) {
        ElementsCollection schemeElements = MainPageUtils.getMainPageRoot().$(MainPageElements.SECURITY_SECTION).$$("security-scheme-row").filter(text(schemeName)).filter(text(schemeType));
        if ("is".equals(isCreated)) {
            assertThat(schemeElements.size()).as("Scheme with name %s do not exists, and should", schemeName).isEqualTo(1);
        } else {
            assertThat(schemeElements.size()).as("Scheme with name %s exists, and should not", schemeName).isEqualTo(0);
        }

    }

    @Then("^check that API security requirement \"([^\"]*)\" \"([^\"]*)\" created$")
    public void checkThatAPISecurityRequirementExists(String requirement, String isCreated) {
        openMainPage();
        ElementsCollection requirementList = MainPageUtils.getMainPageRoot().$(MainPageElements.REQUIREMENTS_SECTION).$$(By.className("security-requirement")).filter(text(requirement));
        if ("is".equals(isCreated)) {
            assertThat(requirementList.size()).as("Requirement %s do not exists, and should", requirement).isEqualTo(1);
        } else {
            assertThat(requirementList.size()).as("Requirement %s exists, and should not", requirement).isEqualTo(0);
        }
    }

    private void openMainPage() {
        CommonUtils.getAppRoot().$(MainPageElements.TITLE_BAR).$(MainPageElements.H1).click();
    }
}
