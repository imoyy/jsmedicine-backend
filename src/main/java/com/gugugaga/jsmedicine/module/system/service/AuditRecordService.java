package com.gugugaga.jsmedicine.module.system.service;

import com.gugugaga.jsmedicine.module.system.entity.AuditRecord;
import com.gugugaga.jsmedicine.module.system.mapper.AuditRecordMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditRecordService {

    private final AuditRecordMapper auditRecordMapper;

    public AuditRecordService(AuditRecordMapper auditRecordMapper) {
        this.auditRecordMapper = auditRecordMapper;
    }

    @Transactional(rollbackFor = Exception.class)
    public void save(AuditRecord auditRecord) {
        int affectedRows = auditRecordMapper.insert(auditRecord);
        if (affectedRows != 1) {
            throw new IllegalStateException("Failed to persist audit record");
        }
    }
}
