package com.gugugaga.jsmedicine.infrastructure;

import com.gugugaga.jsmedicine.common.config.OperationAudit;
import com.gugugaga.jsmedicine.common.response.ApiResponse;
import com.gugugaga.jsmedicine.module.system.entity.AuditRecord;
import com.gugugaga.jsmedicine.module.system.mapper.AuditRecordMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(Stage2InfrastructureIntegrationTest.TestStage2Controller.class)
class Stage2InfrastructureIntegrationTest {

    private static final String CREATE_AUDIT_RECORDS_SQL = """
            CREATE TABLE IF NOT EXISTS audit_records (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                target_type VARCHAR(64) NOT NULL,
                target_id BIGINT NOT NULL,
                before_status TINYINT NULL,
                after_status TINYINT NOT NULL,
                audit_comment VARCHAR(512) NULL,
                auditor_id BIGINT NOT NULL,
                audited_at TIMESTAMP NOT NULL,
                created_at TIMESTAMP NOT NULL
            )
            """;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuditRecordMapper auditRecordMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void clearAuditRecords() {
        jdbcTemplate.execute(CREATE_AUDIT_RECORDS_SQL);
        auditRecordMapper.delete(null);
    }

    @Test
    void shouldReturnUnauthorizedResponseWhenNoAuthentication() throws Exception {
        mockMvc.perform(post("/internal/stage2/audit/12"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().exists("X-Request-Id"))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("Unauthorized"));
    }

    @Test
    @WithMockUser(username = "7")
    void shouldReturnStructuredValidationErrorResponse() throws Exception {
        mockMvc.perform(post("/internal/stage2/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(header().exists("X-Request-Id"))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.data.errors[0].field").value("name"));
    }

    @Test
    @WithMockUser(username = "9")
    void shouldPersistAuditRecordWhenOperationAuditAnnotationIsUsed() throws Exception {
        mockMvc.perform(post("/internal/stage2/audit/25"))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Request-Id"))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("OK"));

        List<AuditRecord> auditRecords = auditRecordMapper.selectList(null);
        assertThat(auditRecords).hasSize(1);
        AuditRecord auditRecord = auditRecords.get(0);
        assertThat(auditRecord.getTargetType()).isEqualTo("stage2-test");
        assertThat(auditRecord.getTargetId()).isEqualTo(25L);
        assertThat(auditRecord.getAfterStatus()).isEqualTo(1);
        assertThat(auditRecord.getAuditComment()).isEqualTo("approved");
        assertThat(auditRecord.getAuditorId()).isEqualTo(9L);
        assertThat(auditRecord.getAuditedAt()).isNotNull();
        assertThat(auditRecord.getCreatedAt()).isNotNull();
    }

    @RestController
    @RequestMapping("/internal/stage2")
    static class TestStage2Controller {

        @PostMapping("/validate")
        public ApiResponse<Void> validate(@Valid @RequestBody TestRequest request) {
            return ApiResponse.ok();
        }

        @PostMapping("/audit/{id}")
        @OperationAudit(
                targetType = "stage2-test",
                targetId = "#args[0]",
                afterStatus = "1",
                comment = "'approved'"
        )
        public ApiResponse<Void> audit(@PathVariable Long id) {
            return ApiResponse.ok();
        }
    }

    @Getter
    @Setter
    static class TestRequest {

        @NotBlank(message = "name must not be blank")
        private String name;
    }
}
