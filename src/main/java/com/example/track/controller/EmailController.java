package com.example.track.controller;

import com.example.track.dto.EmailRequest;
import com.example.track.dto.StandardResponse;

import com.example.track.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@ResponseBody
public class EmailController {

    @Autowired
    EmailService emailService;

    @PostMapping("/email/send")
    public ResponseEntity<StandardResponse> sendEmail(@RequestBody EmailRequest request) {
        return new ResponseEntity<>(emailService.sendEmail(request), HttpStatus.OK);
    }
}
