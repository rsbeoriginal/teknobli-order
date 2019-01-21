package com.tecknobli.order.service.Impl;

import com.tecknobli.order.config.SwaggerConfig;
import com.tecknobli.order.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import springfox.documentation.spring.web.plugins.Docket;

@Component
public class EmailServiceImpl implements EmailService {

//    @Autowired
    public JavaMailSender emailSender = SwaggerConfig.getJavaMailSender();


    @Autowired
    private Docket docket;

    public void sendSimpleMessage(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        emailSender.send(message);
    }
}
