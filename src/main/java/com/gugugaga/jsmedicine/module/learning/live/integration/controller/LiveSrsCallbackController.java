package com.gugugaga.jsmedicine.module.learning.live.integration.controller;

import com.gugugaga.jsmedicine.module.learning.live.admin.dto.SrsHookResponse;
import com.gugugaga.jsmedicine.module.learning.live.admin.dto.SrsLiveHookRequest;
import com.gugugaga.jsmedicine.module.learning.live.service.LiveStreamService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "SRS直播回调")
@RestController
@RequestMapping("/api/v1/integrations/srs")
public class LiveSrsCallbackController {

    private final LiveStreamService liveStreamService;

    public LiveSrsCallbackController(LiveStreamService liveStreamService) {
        this.liveStreamService = liveStreamService;
    }

    @PostMapping("/live-hooks")
    public SrsHookResponse liveHooks(@RequestBody(required = false) SrsLiveHookRequest request,
                                     @RequestParam(required = false) String token) {
        return liveStreamService.handleHook(request, token);
    }
}
