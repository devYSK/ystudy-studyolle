package com.ystudy.infra.mail;

import org.springframework.stereotype.Component;

public interface EmailService {

    void sendEmail(EmailMessage emailMessage);
}
