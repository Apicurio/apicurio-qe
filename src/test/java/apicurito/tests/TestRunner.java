package apicurito.tests;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

import com.codeborne.selenide.Configuration;

import apicurito.tests.configuration.TestConfiguration;
import apicurito.tests.configuration.templates.ApicuritoInstall;
import apicurito.tests.utils.openshift.OpenShiftUtils;
import apicurito.tests.utils.slenide.CommonUtils;
import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RunWith(Cucumber.class)
@CucumberOptions(
    features = "classpath:features",
    tags = "not @manual and not @wip and not @ignore",
    plugin = {"pretty", "junit:target/cucumber/cucumber-junit.xml", "json:target/cucumber/cucumber-report.json"})
public class TestRunner {

    @BeforeClass
    public static void beforeTests() {

        boolean createdProject = false;

        if (OpenShiftUtils.xtf().getProject(TestConfiguration.openShiftNamespace()) == null) {
            createdProject = true;
            log.info("Creating new project " + TestConfiguration.openShiftNamespace());
            OpenShiftUtils.getInstance().createProjectRequest(TestConfiguration.openShiftNamespace());
            if (OpenShiftUtils.getInstance().getProject() == null) {
                log.info("Waiting for " + TestConfiguration.openShiftNamespace() + " to be created");
                CommonUtils.sleepFor(10);
            } else {
                log.info(TestConfiguration.openShiftNamespace() + " project created");
            }
        }

        if (Boolean.parseBoolean(TestConfiguration.doReinstall())) {
            if (!createdProject) {
                ApicuritoInstall.cleanNamespace();
            }
            ApicuritoInstall.reinstallApicurito();
        }

        //set up Selenide
        Configuration.timeout = TestConfiguration.getConfigTimeout() * 1000;
        //We will now use custom web driver
        //Configuration.browser = TestConfiguration.apicuritoBrowser();
        Configuration.browser = "apicurito.tests.configuration.CustomWebDriverProvider";
        //TODO temporary workaround for jenkins job
        Configuration.browserSize = "1920x1080";
    }

    @AfterClass
    public static void afterTests() {
        log.info("After Tests");
        if (TestConfiguration.namespaceCleanupAfter()) {
            TestConfiguration.printDivider("Cleaning namespace after tests");
            OpenShiftUtils.getInstance().clean();
            OpenShiftUtils.getInstance().waiters().isProjectClean().waitFor();
        }
    }
}
