package com.vsms.notificationservice.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${notification.sender-email}")
    private String senderEmail;

    @Value("${notification.sender-name}")
    private String senderName;

    public void sendEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(senderEmail, senderName);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Email sent successfully to: {}", to);
        } catch (MessagingException | java.io.UnsupportedEncodingException e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }

    // Manager Created Email
    public void sendManagerCreatedEmail(String to, String name, String username, String password) {
        String subject = "Welcome to VSMS - Your Manager Account";
        String content = """
                <p>Welcome to VSMS, <b>%s</b>!</p>
                <p>Your manager account has been created.</p>
                <p>Username: <b>%s</b></p>
                <p>Temporary Password: <b>%s</b></p>
                <p style="color:red;"><b>Important:</b> Please change your password after first login.</p>
                <p>- VSMS Team</p>
                """.formatted(name, username, password);
        sendEmail(to, subject, content);
    }

    // Technician Approved Email
    public void sendTechnicianApprovedEmail(String to, String name, String username) {
        String subject = "Congratulations! You're Part of VSMS";
        String content = """
                <p>Congratulations <b>%s</b>!</p>
                <p>Your technician registration has been <b style="color:green;">APPROVED</b>.</p>
                <p>You can now login with:</p>
                <p>Username: <b>%s</b></p>
                <p>Password: Use the one you set during registration.</p>
                <p>- VSMS Team</p>
                """.formatted(name, username);
        sendEmail(to, subject, content);
    }

    // Technician Rejected Email
    public void sendTechnicianRejectedEmail(String to, String name, String reason) {
        String subject = "VSMS Registration Update";
        String content = """
                <p>Dear <b>%s</b>,</p>
                <p>Your technician registration was <b style="color:red;">NOT APPROVED</b>.</p>
                %s
                <p>Contact support if you have questions.</p>
                <p>- VSMS Team</p>
                """.formatted(name, reason != null ? "<p>Reason: " + reason + "</p>" : "");
        sendEmail(to, subject, content);
    }

    // Service Completed Email
    public void sendServiceCompletedEmail(String to, String customerName, String vehicleInfo, String serviceName) {
        String subject = "Your Vehicle Service is Complete!";
        String content = """
                <p>Dear <b>%s</b>,</p>
                <p>Your vehicle service has been <b style="color:green;">COMPLETED</b>.</p>
                <p>Vehicle: %s</p>
                <p>Service: %s</p>
                <p style="color:red;"><b>Please check your dashboard for invoice and payment.</b></p>
                <p>- VSMS Team</p>
                """.formatted(customerName, vehicleInfo, serviceName);
        sendEmail(to, subject, content);
    }

    // Invoice Generated Email
    public void sendInvoiceGeneratedEmail(String to, String customerName, String invoiceNumber, String amount) {
        String subject = "Invoice Ready - " + invoiceNumber;
        String content = """
                <p>Dear <b>%s</b>,</p>
                <p>Your invoice is ready.</p>
                <p>Invoice Number: <b>%s</b></p>
                <p>Total Amount: <b style="color:red;">₹%s</b></p>
                <p>Please login to your dashboard and click PAY to complete payment.</p>
                <p>- VSMS Team</p>
                """.formatted(customerName, invoiceNumber, amount);
        sendEmail(to, subject, content);
    }

    // Invoice Paid Email (to Manager)
    public void sendInvoicePaidEmail(String to, String invoiceNumber, String customerName, String amount,
            String paymentMethod) {
        String subject = "Payment Received - " + invoiceNumber;
        String content = """
                <p>Payment received!</p>
                <p>Invoice: <b>%s</b></p>
                <p>Customer: %s</p>
                <p>Amount: <b style="color:green;">₹%s</b></p>
                <p>Payment Method: %s</p>
                <p>- VSMS Team</p>
                """.formatted(invoiceNumber, customerName, amount, paymentMethod);
        sendEmail(to, subject, content);
    }

    // Customer Welcome Email
    public void sendCustomerWelcomeEmail(String to, String name) {
        String subject = "Welcome to VSMS!";
        String content = """
                <p>Welcome to VSMS, <b>%s</b>!</p>
                <p>Your account is ready. You can now:</p>
                <p>- Add your vehicles</p>
                <p>- Book service appointments</p>
                <p>- Track service progress</p>
                <p>- Pay invoices online</p>
                <p>Login to your dashboard to get started!</p>
                <p>- VSMS Team</p>
                """.formatted(name);
        sendEmail(to, subject, content);
    }
}
