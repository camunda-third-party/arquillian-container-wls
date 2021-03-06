/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * @author <a href="http://community.jboss.org/people/LightGuard">Jason Porter</a>
 */
package org.jboss.arquillian.container.wls.managed_12_1_2;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Logger;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.container.wls.managed_12_1_2.MyServlet;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Verifies Arquillian can perform multiple deployments and run tests against the deployments.
 *
 * @author Vineet Reynolds
 */
@RunWith(Arquillian.class)
public class WebLogicMultipleDeploymentsTest {
    private static final Logger log = Logger.getLogger(WebLogicMultipleDeploymentsTest.class.getName());

    @Deployment(name = "dep-1", testable = false)
    public static WebArchive getFirstTestArchive() {
        WebArchive war = ShrinkWrap
            .create(WebArchive.class, "test.war")
            .addClasses(MyServlet.class);
        return war;
    }

    @Deployment(name = "dep-2", testable = false)
    public static WebArchive getSecondTestArchive() {
        WebArchive war = ShrinkWrap
            .create(WebArchive.class, "another.war")
            .addClasses(MyServlet.class);

        log.info(war.toString(true));
        return war;
    }

    @Test
    @OperateOnDeployment("dep-1")
    public void assertFirstWarDeployed(@ArquillianResource URL deploymentUrl) throws Exception {
        assertThat(deploymentUrl.toString(), containsString("/test/"));
        final URLConnection response = new URL(deploymentUrl, MyServlet.URL_PATTERN).openConnection();

        BufferedReader in = new BufferedReader(new InputStreamReader(response.getInputStream()));
        final String result = in.readLine();
        in.close();

        assertThat(result, equalTo("hello"));
    }

    @Test
    @OperateOnDeployment("dep-2")
    public void assertSecondWarDeployed(@ArquillianResource URL deploymentUrl) throws Exception {
        assertThat(deploymentUrl.toString(), containsString("/another/"));
        final URLConnection anotherResponse = new URL(deploymentUrl, MyServlet.URL_PATTERN).openConnection();

        BufferedReader anotherIn = new BufferedReader(new InputStreamReader(anotherResponse.getInputStream()));
        final String anotherResult = anotherIn.readLine();
        anotherIn.close();

        assertThat(anotherResult, equalTo("hello"));
    }
}
