package com.gugugaga.jsmedicine.module.auth.service;

import com.gugugaga.jsmedicine.module.system.entity.SysAdmin;

import java.util.List;

public record AdminAuthorizationInfo(
        SysAdmin admin,
        List<String> roleCodes,
        List<String> permissionCodes
) {
}
