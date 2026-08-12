package com.main.AqarCustomer.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender javaMailSender;

    public void sendOtpMail(String toEmail, String otp, String name) {

        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("YOUR OTP CODE");
            helper.setText(buildEmailBody(name, otp), true); // true = HTML

            javaMailSender.send(mimeMessage);
            log.info("Otp email sent successfully to: {}", toEmail);
        } catch (MessagingException ex) {
            log.error("Failed to send otp email to: {}", toEmail, ex);
        }
    }

    // HTML Template for email otp
    private String buildEmailBody(String name, String otp) {
        return """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Your OTP Code</title>
            </head>
            <body style="margin:0; padding:0; background-color:#f4f5f7; font-family:'Segoe UI', Arial, sans-serif;">
                <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" style="background-color:#f4f5f7; padding:40px 0;">
                    <tr>
                        <td align="center">
                            <table role="presentation" width="480" cellpadding="0" cellspacing="0" style="background-color:#ffffff; border-radius:12px; overflow:hidden; box-shadow:0 2px 8px rgba(0,0,0,0.06);">
                                <tr>
                                    <td style="background-color:#1a73e8; padding:24px 32px;">
                                        <h1 style="margin:0; color:#ffffff; font-size:20px; font-weight:600;">Aqar</h1>
                                    </td>
                                </tr>
                                <tr>
                                    <td style="padding:32px;">
                                        <p style="margin:0 0 12px; color:#202124; font-size:16px;">Hi %s,</p>
                                        <p style="margin:0 0 24px; color:#5f6368; font-size:14px; line-height:1.5;">
                                            Use the verification code below to complete your action. This code will expire shortly, so please don't share it with anyone.
                                        </p>
                                        <div style="text-align:center; margin:0 0 24px;">
                                            <span style="display:inline-block; background-color:#f1f3f4; color:#1a73e8; font-size:32px; font-weight:700; letter-spacing:8px; padding:16px 24px; border-radius:8px;">
                                                %s
                                            </span>
                                        </div>
                                        <p style="margin:0 0 8px; color:#5f6368; font-size:13px; line-height:1.5;">
                                            If you didn't request this code, you can safely ignore this email.
                                        </p>
                                    </td>
                                </tr>
                                <tr>
                                    <td style="background-color:#f4f5f7; padding:16px 32px; text-align:center;">
                                        <p style="margin:0; color:#9aa0a6; font-size:12px;">© 2026 Aqar. All rights reserved.</p>
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                </table>
            </body>
            </html>
            """.formatted(name, otp);
    }
}
