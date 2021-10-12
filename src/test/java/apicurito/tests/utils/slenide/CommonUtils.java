package apicurito.tests.utils.slenide;

import static com.codeborne.selenide.CollectionCondition.sizeGreaterThanOrEqual;
import static com.codeborne.selenide.Condition.attribute;
import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;

import java.util.List;

import apicurito.tests.configuration.TestConfiguration;
import io.cucumber.datatable.DataTable;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CommonUtils {

    private static class CommonElements {
        private static By TEXT_AREA = By.cssSelector("ace-editor textarea");

        private static By RESPONSE_SECTION = By.cssSelector("responses-section");
        private static By MEDIA_TYPE_SECTION = By.cssSelector("#addMediaTypeModal");
        private static By REQUEST_BODY_SECTION = By.cssSelector("requestbody-section");
        private static By QUERY_PARAM_SECTION = By.cssSelector("query-params-section");
        private static By HEADER_PARAM_SECTION = By.cssSelector("header-params-section");
        private static By SECURITY_REQ_SECTION = By.cssSelector("security-requirements-section");
        private static By SECURITY_SCHEMA_SECTION = By.cssSelector("security-schemes-section");
        private static By PATH_PARAM_SECTION = By.cssSelector("path-params-section");
        private static By COOKIE_PARAM_SECTION = By.cssSelector("cookie-params-section");
        private static By SERVER_SECTION = By.cssSelector("servers-section");

        private static By QUERY_PARAM_ROW = By.cssSelector("query-param-row");
        private static By HEADER_PARAM_ROW = By.cssSelector("header-param-row");
        private static By FORM_DATA_ROW = By.cssSelector("formdata-param-row");
        private static By SECURITY_REQ_ROW = By.className("security-requirement");
        private static By SECURITY_SCHEMA_ROW = By.cssSelector("security-scheme-row");
        private static By PATH_PARAM_ROW = By.cssSelector("path-param-row");
        private static By PROPERTY_ROW = By.cssSelector("property-row");

    }

    public static SelenideElement getAppRoot() {
        return $(By.cssSelector(TestConfiguration.getAppRoot())).shouldBe(visible);
    }

    public static SelenideElement getButtonWithText(String buttonText, SelenideElement differentRoot) {
        log.info("searching for button {}", buttonText);

        return differentRoot.shouldBe(visible).$$("button").filter(text(buttonText))
            .shouldHave(sizeGreaterThanOrEqual(1)).first();
    }

    public static SelenideElement getButtonWithTitle(String buttonTitle, SelenideElement differentRoot) {
        log.info("searching for button with title {}", buttonTitle);

        return differentRoot.shouldBe(visible).$$("button").filter(attribute("title", buttonTitle))
            .shouldHave(sizeGreaterThanOrEqual(1)).first();
    }

    public static SelenideElement getLabelWithName(String labelName, SelenideElement differentRoot) {
        log.info("searching for label {}", labelName);
        return differentRoot.shouldBe(visible).find(By.cssSelector(String.format("input[name*=\"%s\"]", labelName)));
    }

    public static SelenideElement getLabelWithType(String labelType, SelenideElement differentRoot) {
        log.info("searching for label {}", labelType);
        return differentRoot.shouldBe(visible).$$("input").filter(attribute("type", labelType)).first();
    }

    public static SelenideElement getClickableLink(Sections section, By differentRoot) {
        return getClickableLink(section, CommonUtils.getAppRoot().$(differentRoot));
    }

    public static SelenideElement getClickableLink(Sections section, SelenideElement differentRoot) {
        log.info("searching for link in section {}", section);

        return differentRoot.$$("a").find(text(section.getA()));
    }

    public static SelenideElement getNewPlusSignButton(Sections section, SelenideElement differentRoot) {
        log.info("searching for a new plus sign button for section {}", section);

        return differentRoot.$$("div").filter(attribute("class", section.getSectionElementName())).first().$("button");
    }

    public static void setValueInTextArea(String value, SelenideElement section) {
        log.info("Setting value {} into text area in section {}", value, section.getAttribute("class"));

        section.$$("div").filter(attribute("title", "Click to edit.")).first().click();

        section.$(CommonElements.TEXT_AREA).sendKeys(Keys.CONTROL + "a");
        section.$(CommonElements.TEXT_AREA).sendKeys(Keys.DELETE);
        section.$(CommonElements.TEXT_AREA).sendKeys(value);

        CommonUtils.sleepFor(1); // firefox needs at least second to process EXAMPLE

        getButtonWithTitle("Save changes.", section).click();
    }

    public static void setValueInLabel(String value, SelenideElement section, boolean isPencilLabel) {
        log.info("Setting value {} into label in section {}", value, section.getAttribute("class"));

        if (isPencilLabel) {
            section.$$("i").filter(attribute("title", "Click to edit.")).first().click();
        } else {
            section.$$("span").filter(attribute("title", "Click to edit.")).first().click();
        }
        getLabelWithType("text", section).setValue(value);
        getButtonWithTitle("Save changes.", section).click();
    }

    public static void setDropDownValue(String buttonId, String value, SelenideElement section) {
        log.info("Setting value {} in dropdown {} in section {}", value, buttonId, section.getAttribute("class"));
        section.$(buttonId).click();
        section.$$("li").filter(text(value)).first().click();
    }

    /**
     * It works for creating: Query parameters (both), Header parameters (both), Reqest form data, Data type property
     */
    public static void fillEntityEditorForm(DataTable table) {
        SelenideElement newDataFormPage = MainPageUtils.getMainPageRoot().$("#entity-editor-form");

        for (List<String> dataRow : table.cells()) {
            newDataFormPage.$("#qp_name").sendKeys(dataRow.get(0));

            if (!dataRow.get(1).isEmpty()) {
                newDataFormPage.$(CommonElements.TEXT_AREA).sendKeys(dataRow.get(1));
            }

            if (!dataRow.get(2).isEmpty()) {
                CommonUtils.setDropDownValue(CommonUtils.DropdownButtons.PARAM_REQUIRED.getButtonId(), dataRow.get(2), newDataFormPage);
            }

            if (!dataRow.get(3).isEmpty()) {
                CommonUtils.setDropDownValue(CommonUtils.DropdownButtons.PARAM_TYPE.getButtonId(), dataRow.get(3), newDataFormPage);
            }

            if (!dataRow.get(4).isEmpty()) {
                CommonUtils.setDropDownValue(CommonUtils.DropdownButtons.PARAM_TYPE_OF.getButtonId(), dataRow.get(4), newDataFormPage);
            }

            if (!dataRow.get(5).isEmpty()) {
                CommonUtils.setDropDownValue(CommonUtils.DropdownButtons.PARAM_TYPE_AS.getButtonId(), dataRow.get(5), newDataFormPage);
            }

            CommonUtils.getButtonWithText("Save", newDataFormPage).click();
        }
    }

    /**
     * Opens collapsed section. If section is already opened do nothing.
     *
     * @param section which should be opened
     */
    public static void openCollapsedSection(SelenideElement page, By section) {
        ElementsCollection collapsedSection = page.$(section).$$("a").filter(attribute("class", "collapsed"));
        if (collapsedSection.size() > 0) {
            collapsedSection.first().click();
        }
    }

    public enum Sections {
        PATH("section path-section panel-group", "Add a path"),
        DATA_TYPES("section definition-section panel-group", "Add a data type"),
        RESPONSE("section responses-section panel-group", "Add a response"),
        //Panel on the left side below paths and data types sections
        RESPONSES_PANEL("section response-section panel-group", "Add a response"),
        CONTACT("section contact-section panel-group", "Add contact info"),
        LICENSE("section license-section panel-group", "Set license"),
        TAG("section tags-section panel-group", "Add a tag"),
        SCHEME("section security-section panel-group", "Add a security scheme"),
        REQUIREMENT("section security-requirements-section panel-group", "Add security requirement"),
        PROPERTIES("section security-requirements-section panel-group", "Add a property"),
        QUERY_PARAM("section query-parameters-section panel-group", "Add a query parameter"),
        HEADER_PARAM("section header-parameters-section panel-group", "Add a header parameter"),
        RFD_PARAM("section requestBody-section panel-group", "add request form data"),
        COOKIE_PARAM("section cookie-parameters-section panel-group", "Add a cookie parameter");

        private String sectionElementName;
        private String a;

        Sections(String sectionElementName, String a) {
            this.sectionElementName = sectionElementName;
            this.a = a;
        }

        public String getSectionElementName() {
            return this.sectionElementName;
        }

        public String getA() {
            return this.a;
        }
    }

    public enum DropdownButtons {
        PROPERTY_TYPE("#api-property-type"),
        PROPERTY_TYPE_OF("#api-property-type-of"),
        PROPERTY_TYPE_AS("#api-property-type-as"),
        PROPERTY_REQUIRED("#api-property-required"),

        PARAM_REQUIRED("#api-param-required"),
        PARAM_TYPE("#api-param-type"),
        PARAM_TYPE_OF("#api-param-type-of"),
        PARAM_TYPE_AS("#api-param-type-as"),

        MEDIA_TYPE("#api-mediaType");

        private String buttonId;

        DropdownButtons(String buttonId) {
            this.buttonId = buttonId;
        }

        public String getButtonId() {
            return this.buttonId;
        }
    }

    public static By getSectionBy(String section) {
        switch (section) {
            case "query":
                return CommonElements.QUERY_PARAM_SECTION;
            case "header":
                return CommonElements.HEADER_PARAM_SECTION;
            case "response":
                return CommonElements.RESPONSE_SECTION;
            case "RDF":
            case "request body":
                return CommonElements.REQUEST_BODY_SECTION;
            case "security requirement":
                return CommonElements.SECURITY_REQ_SECTION;
            case "security schema":
                return CommonElements.SECURITY_SCHEMA_SECTION;
            case "path":
                return CommonElements.PATH_PARAM_SECTION;
            case "cookie":
                return CommonElements.COOKIE_PARAM_SECTION;
            case "server":
                return CommonElements.SERVER_SECTION;
        }
        return null;
    }

    public static SelenideElement getPageElement(String pageName) {
        switch (pageName) {
            case "operations":
                return OperationUtils.getOperationRoot();
            case "path":
                return PathUtils.getPathPageRoot();
            case "datatypes":
                return DataTypeUtils.getDataTypesRoot();
            case "main page":
                return MainPageUtils.getMainPageRoot();
        }
        return null;
    }

    public static String getButtonId(String buttonName) {
        switch (buttonName) {
            case "type":
                return DropdownButtons.PROPERTY_TYPE.getButtonId();
            case "of":
                return DropdownButtons.PROPERTY_TYPE_OF.getButtonId();
            case "as":
                return DropdownButtons.PROPERTY_TYPE_AS.getButtonId();
            case "required":
                return DropdownButtons.PROPERTY_REQUIRED.getButtonId();
            case "media type":
                return DropdownButtons.MEDIA_TYPE.getButtonId();
        }
        return null;
    }

    public static SelenideElement getDropdownMenuItem(String name) {
        return CommonUtils.getAppRoot()
            .$$(By.cssSelector(".dropdown-menu li a span"))
            .filter(text(name))
            .shouldHaveSize(1).first();
    }

    public static SelenideElement getKebabButtonOnElement(SelenideElement element) {
        return element.$(By.className("dropdown-toggle"));
    }

    public static SelenideElement getElementRow(SelenideElement section, String sectionName, String elementName) {
        By sectionRowType = null;
        switch (sectionName) {
            case "query":
                sectionRowType = CommonElements.QUERY_PARAM_ROW;
                break;
            case "header":
                sectionRowType = CommonElements.HEADER_PARAM_ROW;
                break;
            case "RDF":
                sectionRowType = CommonElements.FORM_DATA_ROW;
                break;
            case "security requirement":
                sectionRowType = CommonElements.SECURITY_REQ_ROW;
                break;
            case "security schema":
                sectionRowType = CommonElements.SECURITY_SCHEMA_ROW;
                break;
            case "path":
                sectionRowType = CommonElements.PATH_PARAM_ROW;
                break;
            case "property":
                sectionRowType = CommonElements.PROPERTY_ROW;
                break;

        }
        ElementsCollection rows = section.$$(sectionRowType);
        for (SelenideElement row : rows) {
            if (row.$(By.className("name")).getText().equals(elementName)) {
                return row;
            }
        }
        //element not found
        return null;
    }

    public static void sleepFor(int seconds) {
        try {
            Thread.sleep(1000L * seconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
