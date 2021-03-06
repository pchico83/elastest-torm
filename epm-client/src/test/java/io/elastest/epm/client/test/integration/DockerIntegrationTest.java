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
package io.elastest.epm.client.test.integration;

import static com.github.dockerjava.api.model.ExposedPort.tcp;
import static com.github.dockerjava.api.model.Ports.Binding.bindPort;
import static io.elastest.epm.client.DockerContainer.dockerBuilder;
import static java.lang.invoke.MethodHandles.lookup;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports.Binding;

import io.elastest.epm.client.DockerContainer.DockerBuilder;
import io.elastest.epm.client.service.DockerService;
import io.elastest.epm.client.service.ShellService;

/**
 * Tests for Docker service.
 *
 * @author Boni Garcia (boni.garcia@urjc.es)
 * @since 0.0.1
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = { DockerService.class,
        ShellService.class }, webEnvironment = RANDOM_PORT)
@Tag("integration")
@DisplayName("Integration test for Docker Service")
@EnableAutoConfiguration
public class DockerIntegrationTest {

    final Logger log = getLogger(lookup().lookupClass());

    @Autowired
    private DockerService dockerService;

    @Test
    @DisplayName("Ask for Chrome to Docker")
    void testDocker() throws Exception {
        // Test data (input)
        String imageId = "selenium/standalone-chrome-debug:3.5.3";
        log.debug("Starting Hub with image {}", imageId);

        String containerName = dockerService
                .generateContainerName("hub-for-test-");

        Binding bindPort = bindPort(dockerService.findRandomOpenPort());
        ExposedPort exposedPort = tcp(4444);

        Binding bindHubVncPort = bindPort(dockerService.findRandomOpenPort());
        ExposedPort exposedHubVncPort = tcp(5900);

        List<PortBinding> portBindings = asList(
                new PortBinding(bindPort, exposedPort),
                new PortBinding(bindHubVncPort, exposedHubVncPort));

        DockerBuilder dockerBuilder = dockerBuilder(imageId, containerName)
                .portBindings(portBindings);

        dockerService.startAndWaitContainer(dockerBuilder.build());

        // Assertions
        assertTrue(dockerService.existsContainer(containerName));

        // Tear down
        log.debug("Stoping Hub");
        dockerService.stopAndRemoveContainer(containerName);
    }

}
