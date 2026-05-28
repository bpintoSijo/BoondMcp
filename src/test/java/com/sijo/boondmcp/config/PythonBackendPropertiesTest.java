package com.sijo.boondmcp.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class PythonBackendPropertiesTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(PropertiesConfiguration.class);

    @Test
    void givenMcpPythonProperties_whenContextStarts_thenBindsBaseUrl() {
        contextRunner
                .withPropertyValues(
                        "mcp.python.base-url=http://localhost:9999",
                        "mcp.python.timeout=3s"
                )
                .run(context -> {
                    assertThat(context).hasSingleBean(PythonBackendProperties.class);
                    assertThat(context.getBean(PythonBackendProperties.class).baseUrl())
                            .isEqualTo("http://localhost:9999");
                });
    }

    @Test
    void givenTimeoutProperty_whenContextStarts_thenBindsDuration() {
        contextRunner
                .withPropertyValues(
                        "mcp.python.base-url=http://localhost:9999",
                        "mcp.python.timeout=1500ms"
                )
                .run(context -> assertThat(context.getBean(PythonBackendProperties.class).timeout())
                        .isEqualTo(Duration.ofMillis(1_500)));
    }

    @Test
    void givenMissingBaseUrl_whenContextStarts_thenFailsConfigurationBinding() {
        contextRunner
                .withPropertyValues("mcp.python.timeout=3s")
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure()).hasStackTraceContaining("baseUrl");
                });
    }

    @Configuration(proxyBeanMethods = false)
    @EnableConfigurationProperties(PythonBackendProperties.class)
    static class PropertiesConfiguration {
    }
}
