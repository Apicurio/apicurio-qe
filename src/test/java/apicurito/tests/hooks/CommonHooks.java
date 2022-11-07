package apicurito.tests.hooks;

import org.junit.Assume;

import apicurito.tests.configuration.TestConfiguration;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CommonHooks {
    //@After("@scenario1,@scenario2")
    @After
    public void oneHook() {
        TestConfiguration.printDivider("Executing afterhook which does nothing so far");
    }

    @Before("@skip_scenario_template")
    public void skip_scenario_template(Scenario scenario) {
        if (!TestConfiguration.apicuritoInstallMethod().equalsIgnoreCase("operator")) {
            TestConfiguration.printDivider("SKIP SCENARIO: " + scenario.getName());
            Assume.assumeTrue(false);
        }
    }

    @Before("@skip_scenario")
    public void skip_scenario(Scenario scenario) {
        TestConfiguration.printDivider("SKIP SCENARIO: " + scenario.getName());
    }
}
