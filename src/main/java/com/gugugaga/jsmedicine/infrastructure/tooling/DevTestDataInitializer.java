package com.gugugaga.jsmedicine.infrastructure.tooling;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.jdbc.datasource.init.ScriptException;

import javax.sql.DataSource;

/**
 * dev 环境启动时按开关导入幂等测试数据。
 */
@Profile("dev")
@Configuration
@EnableConfigurationProperties(DevTestDataProperties.class)
public class DevTestDataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DevTestDataInitializer.class);
    private static final String SCRIPT_PATH = "scripts/sql/seed_test_data.sql";

    private final DataSource dataSource;
    private final DevTestDataProperties properties;

    public DevTestDataInitializer(DataSource dataSource, DevTestDataProperties properties) {
        this.dataSource = dataSource;
        this.properties = properties;
    }

    @Override
    public void run(String... args) {
        if (!properties.isEnabled()) {
            log.info("Dev test data initialization skipped because app.dev.test-data.enabled=false");
            return;
        }
        ResourceDatabasePopulator databasePopulator = new ResourceDatabasePopulator(false, false, "UTF-8");
        databasePopulator.addScript(new ClassPathResource(SCRIPT_PATH));
        try {
            databasePopulator.execute(dataSource);
            log.info("Dev test data initialized from {}", SCRIPT_PATH);
        } catch (ScriptException exception) {
            throw new IllegalStateException("Failed to initialize dev test data", exception);
        }
    }
}
