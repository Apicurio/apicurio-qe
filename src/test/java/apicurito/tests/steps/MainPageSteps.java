package apicurito.tests.steps;

import apicurito.tests.utils.slenide.CommonUtils;
import apicurito.tests.utils.slenide.ImportExportUtils;
import apicurito.tests.utils.slenide.MainPageUtils;
import com.codeborne.selenide.SelenideElement;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import io.cucumber.datatable.DataTable;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Condition;
import org.openqa.selenium.By;

import java.io.File;
import java.util.List;

import static com.codeborne.selenide.Condition.*;
import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class MainPageSteps {

    @And("^create a new path with link with name \"([^\"]*)\"$")
    public void createNewPathWithLink(String newPathName){      //TODO join two methods
        CommonUtils.getClickableLink(CommonUtils.Sections.PATH, CommonUtils.getAppRoot().shouldBe(visible,enabled).shouldNotHave(attribute("disabled")))
                .click();
        CommonUtils.getLabelWithName("path", CommonUtils.getAppRoot().shouldBe(visible,enabled).shouldNotHave(attribute("disabled")))
                .setValue("/" + newPathName);
        CommonUtils.getButtonWithText("Add", CommonUtils.getAppRoot()).shouldBe(visible, enabled).shouldNotHave(attribute("disabled"))
                .click();
    }

    @And("^create a new path with plus sign with name \"([^\"]*)\"$")
    public void createNewPathWithPlusSign(String newPathName){
        CommonUtils.getNewPlusSignButton(CommonUtils.Sections.PATH, CommonUtils.getAppRoot().shouldBe(visible,enabled).shouldNotHave(attribute("disabled")))
                .click();
        CommonUtils.getLabelWithName("path", CommonUtils.getAppRoot().shouldBe(visible,enabled).shouldNotHave(attribute("disabled")))
                .setValue("/" + newPathName);
        CommonUtils.getButtonWithText("Add", CommonUtils.getAppRoot()).shouldBe(visible, enabled).shouldNotHave(attribute("disabled"))
                .click();
    }

    @When("^delete path \"([^\"]*)\"$")
    public void deletePath(String path) {
        MainPageUtils.getPathWithName(path).contextClick();
        MainPageUtils.getDropdownMenuItem("Delete").shouldBe(visible).click();
    }

    @And("^change API name to \"([^\"]*)\"$")
    public void changeAPINameTo(String newName){
        SelenideElement se = CommonUtils.getAppRoot().$(By.className("editor-title-bar-edit"));
        se.hover();
        se.click();

        CommonUtils.getLabelWithName("title-input", CommonUtils.getAppRoot())
                .setValue(newName);
        CommonUtils.getButtonWithTitle("Save changes.", CommonUtils.getAppRoot().$("title-bar"))
                .click();
    }


    @Then("^save API as \"([^\"]*)\" and close editor$")
    public void saveAPIAsAndCloseEditor(String format) throws InterruptedException {
        File exportedIntegrationFile = ImportExportUtils.exportAPIUtil(format);
        assertThat(exportedIntegrationFile)
                .exists()
                .isFile()
                .has(new Condition<>(f -> f.length() > 0, "File size should be greater than 0"));
        //TODO ExportedIntegrationJSONUtil.testExportedFile(exportedIntegrationFile);

        CommonUtils.getButtonWithText("Close", CommonUtils.getAppRoot())
                .click();
        CommonUtils.getButtonWithText("Don't Save", CommonUtils.getAppRoot())
                .click();
    }

    @And("^set API version to \"([^\"]*)\"$")
    public void setAPIVersionTo(String version) {
        CommonUtils.setValueInLabel(version, CommonUtils.getAppRoot().$("info-section").$(By.className("version")), false);
    }

    @And("^change description to \"([^\"]*)\"$")
    public void changeDescriptionTo(String description) {
        CommonUtils.setValueInTextArea(description, CommonUtils.getAppRoot().$("info-section").$(By.className("description")));
    }

    @And("^add contact info$")
    public void addContactInfo(DataTable table) {
        CommonUtils.getClickableLink(CommonUtils.Sections.CONTACT, MainPageUtils.CONTACT_SECTION).click();
        for (List<String> dataRow : table.cells()) {
            CommonUtils.setValueInLabel(dataRow.get(0), CommonUtils.getAppRoot().$("contact-section").$(By.className("name")), false);
            CommonUtils.setValueInLabel(dataRow.get(1), CommonUtils.getAppRoot().$("contact-section").$(By.className("email")), false);
            CommonUtils.setValueInLabel(dataRow.get(2), CommonUtils.getAppRoot().$("contact-section").$(By.className("url")), false);
        }
    }

    @And("^add license \"([^\"]*)\"$")
    public void addLicense(String license){
        CommonUtils.getClickableLink(CommonUtils.Sections.LICENSE, MainPageUtils.LICENSE_SECTION).click();
        MainPageUtils.setLicense(license);
    }

    @And("^add tag \"([^\"]*)\" with description \"([^\"]*)\"$")
    public void addTagWithDescription(String tag, String description)  {
        CommonUtils.getClickableLink(CommonUtils.Sections.TAG, MainPageUtils.TAGS_SECTION).click();
        MainPageUtils.addTag(tag, description);
    }

    @And("^select path \"([^\"]*)\"$")
    public void selectPath(String path) {
        MainPageUtils.getPathWithName(path).click();
    }

    @And("^set consumes to \"([^\"]*)\"$")      //TODO this is add consume but application/json is still there --> delete it
    public void setConsumesTo(String consumes) {
        log.info("Setting consumes to {}", consumes);
        CommonUtils.setValueInLabel(consumes, MainPageUtils.getMainPageRoot().$(By.className("consumes")),true);
    }

    @And("^set produces to \"([^\"]*)\"$")      //TODO this is add consume but application/json is still there --> delete it
    public void setProducesTo(String produces) {
        log.info("Setting produces to {}", produces);
        CommonUtils.setValueInLabel(produces, MainPageUtils.getMainPageRoot().$(By.className("produces")),true);
    }

    @And("^create a new data type with link with$")
    public void createANewDataTypeWithLinkWith(DataTable table) {
        CommonUtils.getClickableLink(CommonUtils.Sections.DATA_TYPES, CommonUtils.getAppRoot()).click();

        for (List<String> dataRow : table.cells()) {
            MainPageUtils.createDataType(dataRow.get(0), dataRow.get(1), Boolean.valueOf(dataRow.get(2)));
        }
    }

    @And("^create a new data type with plus sign with$")
    public void createANewDataTypeWithPlusSignWith(DataTable table) {
        CommonUtils.getNewPlusSignButton(CommonUtils.Sections.DATA_TYPES, CommonUtils.getAppRoot()).click();
        for (List<String> dataRow : table.cells()) {
            MainPageUtils.createDataType(dataRow.get(0), dataRow.get(1), Boolean.valueOf(dataRow.get(2)));
        }
    }

    @And("^search path or data type with substring \"([^\"]*)\"$")
    public void searchPathOrDataTypeWithSubstring(String substring) {
        MainPageUtils.putSearchSubstring(substring);
    }

    @And("^cancel searching$")
    public void cancelSearching() {
        MainPageUtils.cancelSearching();
    }

    @And("^select data type \"([^\"]*)\"$")
    public void selectDataType(String dataTypeName) {
        MainPageUtils.getDataTypeWithName(dataTypeName).click();
    }

    @Then("^delete API \"([^\"]*)\"$")
    public void deleteAPI(String file) {
        new File(file).delete();
    }
}
