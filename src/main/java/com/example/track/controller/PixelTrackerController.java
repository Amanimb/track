package com.rkv.trackmail.controller;

import com.example.track.dto.TrackerRequest;
import com.example.track.dto.TrackerResponse;
import com.example.track.service.TrackerService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@ResponseBody
public class PixelTrackerController {

    @Autowired
    TrackerService trackerService;

    @GetMapping("/email/analytics")
    public ResponseEntity<List<TrackerResponse>> getEmailAnalytics(@RequestBody TrackerRequest request) {
        return new ResponseEntity<>(trackerService.getEmailAnalytics(request), HttpStatus.OK);
    }

    @GetMapping("/tracker/{id}")
    public ResponseEntity<Void> updateEmailAnalyticsInfo(@PathVariable String id) {
        trackerService.updateEmailAnalyticsInfo(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
