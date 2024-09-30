package com.example.track.service;

import jakarta.mail.Authenticator; // Change this import
import jakarta.mail.PasswordAuthentication; // Ensure this is also imported
import com.example.track.config.EmailServer;
import com.example.track.dto.EmailRequest;
import com.example.track.dto.StandardResponse;
import com.example.track.entity.EmailAccount;
import com.example.track.entity.EmailRecipient;
import com.example.track.exception.CustomException;
import com.example.track.exception.GatewayException;
import com.example.track.repository.EmailRecipientRepository;
import com.example.track.repository.EmailServerRepository;
import com.example.track.util.EmailUtil;
import jakarta.annotation.PostConstruct;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.time.LocalDate;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;

@Service
public class EmailService {

    private static Properties prop = new Properties();

    @Autowired
    private EmailServer emailServer;

    @Autowired
    private EmailRecipientRepository emailRecipientRepository;

    @Autowired
    private EmailServerRepository emailServerRepository;


    @PostConstruct
    public void init() {
        prop.put("mail.smtp.auth", emailServer.isAuth());
        prop.put("mail.smtp.host", emailServer.getHost());
        prop.put("mail.smtp.port", emailServer.getPort());
        prop.put("mail.smtp.ssl.enable", emailServer.isSsl());
        prop.put("mail.smtp.ssl.protocols", emailServer.getProtocol());
        prop.put("mail.smtp.socketFactory.class", emailServer.getSocket());
        prop.put("mail.smtp.socketFactory.port", emailServer.getPort());
    }

    public StandardResponse sendEmail(EmailRequest request) {
        if (!EmailUtil.isEmailAddressValid(request.toAddress())) {
            throw new CustomException("Email address is not acceptable");
        }
        CompletableFuture.runAsync(() -> processEmailCommunication(request));
        return new StandardResponse(true);
    }

    private void processEmailCommunication(EmailRequest request) {
        try {
            String pixelId = request.toAddress().split("@")[0] + System.currentTimeMillis();
            EmailRecipient rec = new EmailRecipient(pixelId, request.toAddress(), request.subject(), LocalDate.now());
            emailRecipientRepository.save(rec);

            Optional<EmailAccount> emailAcctOptional = emailServerRepository.findById("admin");
            if (emailAcctOptional.isEmpty()) {
                throw new GatewayException("Attempt to fetch server details failed");
            }
            EmailAccount emailAcct = emailAcctOptional.get();

            Session session = Session.getInstance(prop, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(emailAcct.getEmailId(), emailAcct.getAppPwd());
                }
            });

            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(emailAcct.getEmailId()));
            message.setRecipients(MimeMessage.RecipientType.TO, InternetAddress.parse(request.toAddress()));
            message.setSubject(request.subject());

            MimeBodyPart mimeBodyPart = new MimeBodyPart();
            String formattedContent = getEmailBodyWithTracker(request, pixelId);
            mimeBodyPart.setContent(formattedContent, "text/html; charset=utf-8");
            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(mimeBodyPart);

            message.setContent(multipart);
            try {
                Transport.send(message);
            } catch (MessagingException ex) {
                EmailRecipient recipient = emailRecipientRepository.findById(pixelId).get();
                recipient.setFailedToSend(true);
                emailRecipientRepository.save(recipient);
            }
        } catch (Exception ex) {
            //TODO add logs
            System.out.println(ex);
            throw new GatewayException("Failed to process Email, Try Again!");
        }
    }

    private static String getEmailBodyWithTracker(EmailRequest request, String pixelId) {
        String imgUrl = "http://localhost:8080/tracker/" + pixelId;
        // Correctly formatted string
        return String.format("<html><body>%s<img src=\"%s\" width=\"1\" height=\"1\"></body></html>",
                request.content(), imgUrl);
    }
}
