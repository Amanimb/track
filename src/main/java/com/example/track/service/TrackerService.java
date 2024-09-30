package com.example.track.service;


import com.example.track.dto.TrackerRequest;
import com.example.track.dto.TrackerResponse;
import com.example.track.entity.EmailRecipient;
import com.example.track.repository.EmailRecipientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TrackerService {

    @Autowired
    private EmailRecipientRepository emailRecipientRepository;

    public List<TrackerResponse> getEmailAnalytics(TrackerRequest request) {
        List<EmailRecipient> recipients = emailRecipientRepository.findByEmailAddressAndEmailSubject(request.emailId(), request.emailSubject());
        return recipients.stream().map(r -> new TrackerResponse(r.getEmailAddress(), r.getCreationDateTime(), !r.isFailedToSend(), r.getReadCount() > 0)).toList();
    }

    public void updateEmailAnalyticsInfo(String id) {
        try {
            EmailRecipient recipient = emailRecipientRepository.findById(id).get();
            recipient.setReadCount(recipient.getReadCount() + 1);
            emailRecipientRepository.save(recipient);
        } catch (Exception ex) {
            //TODO add logs
            System.out.println(ex);
        }
    }
}
