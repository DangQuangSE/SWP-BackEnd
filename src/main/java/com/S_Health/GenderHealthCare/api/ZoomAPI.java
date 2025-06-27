package com.S_Health.GenderHealthCare.api;

import com.S_Health.GenderHealthCare.service.ZoomMeetingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/zoom")
public class ZoomAPI {
    @Autowired
    private ZoomMeetingService zoomMeetingService;

    @GetMapping("/test-create-meeting")
    public Map<String, String> testCreateMeeting(@RequestParam Long appointmentId) {
        return zoomMeetingService.createMeeting(appointmentId);
    }
}
