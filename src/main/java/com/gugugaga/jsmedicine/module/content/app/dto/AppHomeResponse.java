package com.gugugaga.jsmedicine.module.content.app.dto;

import java.util.List;

public record AppHomeResponse(
        List<AppHomeSectionResponse> sections
) {
}
