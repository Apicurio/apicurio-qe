package apicurito.tests.utils.slenide;

import static com.codeborne.selenide.Condition.attribute;
import static com.codeborne.selenide.Condition.matchesText;
import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

import org.openqa.selenium.By;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MainPageUtils {

    private static class MainPageElements {
        private static By LICENSE_SECTION = By.cssSelector("license-section");
        private static By TAGS_SECTION = By.cssSelector("tags-section");
        private static By ENTITY_EDITOR_FORM = By.id("entity-editor-form");
        private static By TEXT_AREA = By.cssSelector("ace-editor textarea");
    }

    public static SelenideElement getMainPageRoot() {
        return $("editor").shouldBe(visible);
    }

    public static void setLicense(String license) {
        log.info("Setting license to {}", license);

        SelenideElement parentElement = getMainPageRoot().$(MainPageElements.LICENSE_SECTION).$$("a").filter(text(license)).first().parent().parent();
        parentElement.$$("button").filter(text("Use This License")).first().click();
    }

    public static void addTag(String tag, String description) {
        log.info("Adding tag {} with description {}", tag, description);

        getMainPageRoot().$(MainPageElements.TAGS_SECTION).$("#tag").setValue(tag);
        getMainPageRoot().$(MainPageElements.TAGS_SECTION).$("#description").setValue(description);
        CommonUtils.getButtonWithText("Add", getMainPageRoot().$(MainPageElements.TAGS_SECTION)).click();
    }

    public static SelenideElement getPathWithName(String pathName) {
        log.info("Getting path with name {}", pathName);

        ElementsCollection allPaths = CommonUtils.getAppRoot().$$("div")
            .filter(attribute("class", "section-body")).first()
            .findAll(By.cssSelector("div[class*=\"api-path \"]"));
        for (SelenideElement path : allPaths) {
            String actualPathName = path.$$("div").filter(attribute("path-item")).first().getText();
            if (actualPathName.equals(pathName)) {
                return path.$("div");
            }
        }
        //path is not found
        return null;
    }

    public static SelenideElement getDataTypeWithName(String datatypeName) {
        log.info("Getting data type with name {}", datatypeName);

        return CommonUtils.getAppRoot().$$("section").filter(attribute("label", "Data Types")).first()
            .$$(By.className("api-definition")).filter(text(datatypeName)).first();
    }

    /**
     * @param name        is name of Data Type
     * @param description is OPTIONAL description for Data Type
     * @param example     is OPTIONAL example for Data Type
     * @param isRest      is boolean value which indicates using REST Resource, true -> use REST Resource , false -> No Resource
     */
    public static void createDataType(String name, String description, String example, Boolean isRest) {
        log.info("Creating data type with name {} and description {} and rest resources are {}", name, description, isRest);

        SelenideElement entityForm = CommonUtils.getAppRoot().$(MainPageElements.ENTITY_EDITOR_FORM);

        CommonUtils.getLabelWithName("name", entityForm).sendKeys(name);

        if (description != null) {
            entityForm.$(By.id("description")).$(MainPageElements.TEXT_AREA).sendKeys(description);
        }

        if (example != null) {
            entityForm.$$("div")
                .filter(Condition.attribute("class", "form-group"))
                .filter(matchesText("JSON Example"))
                .first().$(MainPageElements.TEXT_AREA).sendKeys(example);
        }

        if (isRest) {
            entityForm.$$(By.className("create-option")).filter(text("REST Resource")).first().click();
        }

        CommonUtils.sleepFor(1); // firefox needs at least second to process EXAMPLE

        CommonUtils.getButtonWithText("Save", entityForm).click();
    }

    public static void putSearchSubstring(String substring) {
        log.info("Searching for {}", substring);
        CommonUtils.getAppRoot().$("#masterSearch").setValue(substring);
    }
}
