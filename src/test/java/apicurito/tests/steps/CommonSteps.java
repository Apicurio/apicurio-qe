package apicurito.tests.steps;

import static com.codeborne.selenide.Condition.attribute;
import static com.codeborne.selenide.Condition.cssClass;
import static com.codeborne.selenide.Condition.enabled;
import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;
import static org.assertj.core.api.Assertions.assertThat;

import org.apache.maven.it.VerificationException;

import org.assertj.core.api.Condition;
import org.openqa.selenium.By;

import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import apicurito.tests.configuration.TestConfiguration;
import apicurito.tests.utils.slenide.CommonUtils;
import apicurito.tests.utils.slenide.ImportExportUtils;
import apicurito.tests.utils.slenide.MainPageUtils;
import apicurito.tests.utils.slenide.MavenUtils;
import apicurito.tests.utils.slenide.OperationUtils;
import apicurito.tests.utils.slenide.PathUtils;
import cz.xtf.core.http.Https;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CommonSteps {

    @Given("^log into apicurito$")
    public void login() {
        Selenide.open(TestConfiguration.apicuritoUrl());
    }

    @When("^create a new API version \"([^\"]*)\"$")
    public void createANewApi(String version) {
        if ("3".equals(version)) {
            CommonUtils.getButtonWithText("New API", CommonUtils.getAppRoot()).shouldBe(visible, enabled).shouldNotHave(attribute("disabled"))
                .click();
        } else {

            CommonUtils.getButtonWithText("Toggle Dropdown", CommonUtils.getAppRoot()).shouldBe(visible, enabled).shouldNotHave(attribute("disabled"))
                .click();

            CommonUtils.getAppRoot().$$("a").filter(text("New (OpenAPI 2)")).first().click();
        }
    }

    @Then("^sleep for (\\d+) seconds$")
    public void sleepFor(int seconds) {
        CommonUtils.sleepFor(seconds);
    }

    @When("^import API \"([^\"]*)\"$")
    public void importAPI(String pathtoFile) {
        ImportExportUtils.importAPI(new File(pathtoFile));
    }

    @And("^generate and export fuse camel project$")
    public void generateAndExportFuseCamelProject() {
        File exportedFuseCamelProject = ImportExportUtils.exportFuseCamelProject();
        assertThat(exportedFuseCamelProject)
                .exists()
                .isFile()
                .has(new Condition<>(f -> f.length() > 0, "File size should be greater than 0"));
    }

    @And("^unzip and run generated fuse camel project$")
    public void unzipAndRunGeneratedFuseCamelProject() throws Exception {
        final Path sourceFolder = Paths.get("tmp" + File.separator + "download");
        final File archive = sourceFolder.resolve("camel-project.zip").toFile();
        final File destination = sourceFolder.resolve("camel-project").toFile();
        ImportExportUtils.decompressZip(archive, destination);
        log.info("Fuse Camel Project is decompressed to {}", destination);
    }

    @And("^check that project is generated correctly$")
    public void checkThatProjectIsGeneratedCorrectly() throws VerificationException {
        final Path sourceFolder = Paths.get("tmp" + File.separator + "download");
        final Path destination = sourceFolder.resolve("camel-project");

        // Create a new thread and run the generated camel project with maven commands
        final MavenUtils mavenUtils = MavenUtils.forProject(destination).forkJvm();
        final ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(() -> {
            try {
                mavenUtils.executeGoals("clean", "package");
                mavenUtils.executeGoals("spring-boot:run");
            } catch (VerificationException e) {
                log.error("Error during running the app");
                log.error("Error message: ", e);
            }
        });

        // Open the web browser to verify the returned code is 200 OK and the generated camel project is correct
        final String testUrl = "http://localhost:8080/openapi.json";
        assertThat(Https.doesUrlReturnOK(testUrl).waitFor()).isTrue();
        Selenide.open(testUrl);
        $(By.xpath("//pre[contains(text(), 'A brand new API with no content.  Go nuts!')]")).should(com.codeborne.selenide.Condition.exist);

        // Shutdown the thread
        executorService.shutdown();
    }

    @Then("^save API as \"([^\"]*)\" and close editor$")
    public void saveAPIAsAndCloseEditor(String format) {
        File exportedIntegrationFile = ImportExportUtils.exportAPIUtil(format);
        assertThat(exportedIntegrationFile)
            .exists()
            .isFile()
            .has(new Condition<>(f -> f.length() > 0, "File size should be greater than 0"));

        CommonUtils.getButtonWithText("Close", CommonUtils.getAppRoot())
            .click();
        CommonUtils.getButtonWithText("Don't Save", CommonUtils.getAppRoot())
            .click();
    }

    @Then("^delete API \"([^\"]*)\"$")
    public void deleteAPI(String file) {
        new File(file).delete();
    }

    /*
        Create parameters or RDFs (Request data forms) on specified page
        param could be header|query|RFD
        page could be path|operations
     */
    @When("create {string} on {string} page with plus sign {string}")
    public void createParameterOnPage(String param, String page, String isPlus, DataTable table) {
        SelenideElement pageElement = page.equals("operations") ? OperationUtils.getOperationRoot() : PathUtils.getPathPageRoot();
        String aName = null;
        By section = CommonUtils.getSectionBy(param);

        switch (param) {
            case "query":
                aName = CommonUtils.Sections.QUERY_PARAM.getA();
                break;
            case "header":
                aName = CommonUtils.Sections.HEADER_PARAM.getA();
                break;
            case "RDF":
                aName = CommonUtils.Sections.RFD_PARAM.getA();
                break;
            case "cookie":
                aName = CommonUtils.Sections.COOKIE_PARAM.getA();
                break;
        }
        CommonUtils.openCollapsedSection(pageElement, section);

        if (Boolean.valueOf(isPlus)) {
            pageElement.$(section).$$("icon-button").filter(attribute("type", "add"))
                .shouldHaveSize(1).first().shouldBe(visible).$("button").click();
        } else {
            pageElement.$(section).$$("a").filter(text(aName)).shouldHaveSize(1).first().shouldBe(visible).click();
        }
        CommonUtils.fillEntityEditorForm(table);
    }

    @When("^(set|check) type of \"([^\"]*)\" media type (?:to|is) \"([^\"]*)\" on property \"([^\"]*)\" for response \"([^\"]*)\"$")
    public void setTypeOfMediaType(String check, String mediaType, String type, String property, String response) {
        OperationUtils.ensureMediaTypeExistsForResponse(mediaType, response);
        SelenideElement mediaRow = $$("media-type-row").find(text(mediaType));
        SelenideElement typeElement = mediaRow.$(".type");
        if (!typeElement.has(cssClass("selected"))) {
            typeElement.click();
        }
        String buttonId = CommonUtils.getButtonId(property);
        if ("set".equalsIgnoreCase(check)) {
            CommonUtils.setDropDownValue(buttonId, type, $("schema-type-editor"));
        } else {
            String value = mediaRow.$(buttonId).getText();
            assertThat(value).as("%s is %s but should be %s", property, value, type).isEqualTo(type);
        }
    }

    @When("create a server on {string} page")
    public void createAServerOnPage(String page, DataTable table) {
        By section = CommonUtils.getSectionBy("server");
        SelenideElement pageElement = CommonUtils.getPageElement(page);
        CommonUtils.openCollapsedSection(pageElement, section);

        for (List<String> dataRow : table.cells()) {

            //open editor by plus button(true) or by link (false)
            if (Boolean.valueOf(dataRow.get(2))) {
                pageElement.$(section).$$("icon-button").filter(attribute("type", "add"))
                    .shouldHaveSize(1).first().shouldBe(visible).$("button").click();
            } else {
                pageElement.$(section).$$("a").filter(text("Add a server")).shouldHaveSize(1).first().shouldBe(visible).click();
            }

            //set server name and click Apply
            SelenideElement editorPage = MainPageUtils.getMainPageRoot().$("#entity-editor-form");
            editorPage.$("#serverUrl").sendKeys(dataRow.get(0));
            CommonUtils.getButtonWithText("Apply", editorPage).click();

            if (!dataRow.get(1).isEmpty()) {
                editorPage.$("#description").sendKeys(dataRow.get(1));
            }

            CommonUtils.getButtonWithText("Save", editorPage).click();
        }
    }

    @When("configure server variables for {string} on {string} page")
    public void configureServerVariablesForOnPage(String url, String page, DataTable table) {
        By section = CommonUtils.getSectionBy("server");
        SelenideElement pageElement = CommonUtils.getPageElement(page);
        CommonUtils.openCollapsedSection(pageElement, section);
        SelenideElement server = pageElement.$(section).$$(By.className("url")).filter((text(url))).first().parent();
        CommonUtils.getKebabButtonOnElement(server).click();
        CommonUtils.getDropdownMenuItem("Edit").click();

        SelenideElement variablesArea = MainPageUtils.getMainPageRoot().$("#entity-editor-form").$(By.className("server-variables"));
        for (List<String> dataRow : table.cells()) {
            //select right variable
            variablesArea.$$("li").filter(text(dataRow.get(0))).first().click();

            CommonUtils.getLabelWithType("text", variablesArea.$$(By.className("panel-body")).filter(visible).first()).sendKeys(dataRow.get(1));

            variablesArea.$$("textarea").filter(visible).first().sendKeys(dataRow.get(2));
        }
        CommonUtils.getButtonWithText("Save", MainPageUtils.getMainPageRoot().$("#entity-editor-form")).click();
    }
}
