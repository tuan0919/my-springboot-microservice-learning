package com.devteria.notification.service;

import com.devteria.notification.dto.request.EmailRequest;
import com.devteria.notification.dto.request.SendEmailRequest;
import com.devteria.notification.dto.request.Sender;
import com.devteria.notification.dto.response.EmailResponse;
import com.devteria.notification.exception.AppException;
import com.devteria.notification.exception.ErrorCode;
import com.devteria.notification.repository.httpclient.EmailClient;
import feign.FeignException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EmailService {
    EmailClient emailClient;
    String apiKey = "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";

    public EmailResponse sendEmail(SendEmailRequest request) {
        var email = EmailRequest.builder()
                        .sender(Sender.builder()
                                .name("NLU App")
                                .email("21130601@st.hcmuaf.edu.vn")
                                .build())
                        .to(List.of(request.getTo()))
                        .subject(request.getSubject())
                        .htmlContent(request.getHtmlContent())
                        .build();
        try {
            return emailClient.sendSmtpEmail(apiKey, email);
        } catch (FeignException e) {
            throw new AppException(ErrorCode.CANNOT_SEND_EMAIL);
        }
    }
}
