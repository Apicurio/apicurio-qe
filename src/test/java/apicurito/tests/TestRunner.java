package apicurito.tests;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

import com.codeborne.selenide.Configuration;

import apicurito.tests.configuration.TestConfiguration;
import apicurito.tests.configuration.templates.ApicuritoTemplate;
import apicurito.tests.utils.openshift.OpenShiftUtils;
import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RunWith(Cucumber.class)
@CucumberOptions(
        features = "classpath:features",
        tags = {"not @manual", "not @wip", "not @ignore"},
        plugin = {"pretty", "html:target/cucumber/cucumber-html", "junit:target/cucumber/cucumber-junit.xml", "json:target/cucumber/cucumber-report.json"})
public class TestRunner {

    @BeforeClass
    public static void beforeTests() {

        if (OpenShiftUtils.xtf().getProject(TestConfiguration.openShiftNamespace()) == null) {
            OpenShiftUtils.xtf().createProjectRequest(TestConfiguration.openShiftNamespace());
            try {
                Thread.sleep(10 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (Boolean.valueOf(TestConfiguration.doReinstall())) {
            ApicuritoTemplate.cleanNamespace();
            ApicuritoTemplate.setInputStreams();
            ApicuritoTemplate.deploy();
            ApicuritoTemplate.waitForApicurito();
        }

        //set up Selenide
        Configuration.timeout = TestConfiguration.getConfigTimeout() * 1000;
        //We will now use custom web driver
        //Configuration.browser = TestConfiguration.apicuritoBrowser();
        Configuration.browser = "apicurito.tests.configuration.CustomWebDriverProvider";
        //TODO temporary workaround for jenkins job
        //Configuration.browserSize = "1920x1080";
    }

    @AfterClass
    public static void afterTests() {
        log.info("After Tests");
        if (TestConfiguration.namespaceCleanupAfter()) {
            TestConfiguration.printDivider("Cleaning namespace after tests");
            OpenShiftUtils.getInstance().clean();
        }
    }
}
