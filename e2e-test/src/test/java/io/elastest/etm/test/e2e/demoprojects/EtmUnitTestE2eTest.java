/*
 * (C) Copyright 2017-2019 ElasTest (http://elastest.io/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package io.elastest.etm.test.e2e.demoprojects;

import static io.github.bonigarcia.BrowserType.CHROME;
import static java.lang.invoke.MethodHandles.lookup;
import static org.openqa.selenium.support.ui.ExpectedConditions.textToBePresentInElementLocated;
import static org.slf4j.LoggerFactory.getLogger;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.By;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;

import io.elastest.etm.test.base.EtmBaseTest;
import io.github.bonigarcia.DockerBrowser;
import io.github.bonigarcia.SeleniumExtension;

/**
 * E2E ETM test.
 *
 * @author EduJG(https://github.com/EduJGURJC)
 * @since 0.1.1
 */
@Tag("e2e")
@DisplayName("ETM E2E test of Unit Test project")
@ExtendWith(SeleniumExtension.class)
public class EtmUnitTestE2eTest extends EtmBaseTest {

    final Logger log = getLogger(lookup().lookupClass());

    @Test
    @DisplayName("Create Unit Test project Test")
    void testCreateUnitTest(
            @DockerBrowser(type = CHROME) RemoteWebDriver driver)
            throws InterruptedException {
        this.driver = driver;

        navigateToTorm(driver);
        createNewETProject(driver, "Unit Tests");

        String tJobName = "Unit Test";
        String tJobTestResultPath = "/demo-projects/unit-java-test/target/surefire-reports/";
        String sutName = null;
        String tJobImage = "elastest/test-etm-alpinegitjava";
        String commands = "git clone https://github.com/elastest/demo-projects; cd demo-projects/unit-java-test; mvn -B test;";

        createNewTJob(driver, tJobName, tJobTestResultPath, sutName, tJobImage,
                false, commands, null, null);

        // Run TJob
        runTJobFromProjectPage(driver, tJobName);

        WebDriverWait waitLogs = new WebDriverWait(driver, 180);
        //log.info("Wait for metrics");
        //waitLogs.until(presenceOfElementLocated(By.className("tick")));
        log.info("Wait for build sucess traces");
        waitLogs.until(textToBePresentInElementLocated(By.tagName("logs-view"),
                "BUILD SUCCESS"));
    }

}
