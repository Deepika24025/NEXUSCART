package com.ttn.nexuscart.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    @Autowired
    // dependency add kri hai "spring-boot-starter-mail"
    // javamailsender is sending mail and fetches properties from app.prop
    private JavaMailSender mailSender;

    @Async
    public void sendVerificationEmail(String to, String verificationUrl) {
        logger.info("Sending verification email to {}", to);
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message);

            helper.setTo(to);
            helper.setSubject(" Please Verify Your Email");
            helper.setText("Click the link to verify your email: " + verificationUrl);

            mailSender.send(message);
            logger.info("Verification email sent to {}", to);
        } catch (MessagingException e) {
            logger.error("Failed to send verification email to {}", to, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }

    @Async
    public void sendAsyncMail(String email) {
        logger.info("Sending registration acknowledgment email to {}", email);
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message);

            helper.setTo(email);
            helper.setSubject("Email Regarding Account Activation");
            helper.setText("Admin has received the request and will approve on verifying ");

            mailSender.send(message);
            logger.info("Registration acknowledgment email sent to {}", email);
        } catch (MessagingException e) {
            logger.error("Failed to send acknowledgment email to {}", email, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }

    @Async
    public void sendAccountLocked(String email) {
        logger.info("Sending account locked notification to {}", email);
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message);

            helper.setTo(email);
            helper.setSubject("Account Locked");
            helper.setText("Invalid Attempt Exceeded");

            mailSender.send(message);
            logger.info("Account locked email sent to {}", email);
        } catch (MessagingException e) {
            logger.error("Failed to send account locked email to {}", email, e);
            throw new RuntimeException("Failed to send email", e);
        }

    }

    @Async
    public void sendResetEmail(String email, String resetLink) {
        logger.info("Sending password reset email to {}", email);
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message);

            helper.setTo(email);
            helper.setSubject("Reset Password Link");
            helper.setText("click the link to reset password" + resetLink);

            mailSender.send(message);
            logger.info("Password reset email sent to {}", email);
        } catch (MessagingException e) {
            logger.error("Failed to send reset password email to {}", email, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }

    @Async
    public void sendActivationEmail(String email) {
        logger.info("Sending account activation email to {}", email);
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message);

            helper.setTo(email);
            helper.setSubject("Account Status");
            helper.setText("Account activated by Admin");
            mailSender.send(message);
            logger.info("Activation email sent to {}", email);
        } catch (MessagingException e) {
            logger.error("Failed to send activation email to {}", email, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }

    @Async
    public void sendDeActivationEmail(String email) {
        logger.info("Sending account deactivation email to {}", email);
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message);

            helper.setTo(email);
            helper.setSubject("Account Status");
            helper.setText("Account Deactivated by Admin");
            mailSender.send(message);
            logger.info("Deactivation email sent to {}", email);
        } catch (MessagingException e) {
            logger.error("Failed to send deactivation email to {}", email, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }

    @Async
    public void sendPasswordChangeConfirmation(String email) {
        logger.info("Sending password change confirmation to {}", email);
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message);

            helper.setTo(email);
            helper.setSubject("Password Status");
            helper.setText("Dear User your password has been updated. ");
            logger.info("Password change confirmation email sent to {}", email);
            mailSender.send(message);
        } catch (MessagingException e) {
            logger.error("Failed to send password change confirmation to {}", email, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }

    public void sendDeactivationEmail(String email, String name) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message);

            helper.setTo(email);
            helper.setSubject("Product Status");
            helper.setText("Your product is deactivated ");
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email", e);
        }
    }
    @Async
    public void sendProductActivationEmail(String email, String name) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message);

            helper.setTo(email);
            helper.setSubject("Product Status");
            helper.setText("Your product is activated successfully ");
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email", e);
        }
    }
    @Async
    public void sendActivationEmailToCustomer( String email) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message);

            helper.setTo(email);
            helper.setSubject("Account Status");
            helper.setText("Dear User your account is activated successfully ");
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email", e);
        }
    }

    @Async
    public void sendEmail(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }

}