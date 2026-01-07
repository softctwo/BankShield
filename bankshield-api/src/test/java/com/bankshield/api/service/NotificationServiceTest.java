package com.bankshield.api.service;

import com.bankshield.api.entity.AlertRecord;
import com.bankshield.api.service.impl.NotificationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private AlertRecord testAlert;

    @BeforeEach
    public void setUp() {
        testAlert = new AlertRecord();
        testAlert.setId(1L);
        testAlert.setRuleName("CPU使用率告警");
        testAlert.setAlertLevel("HIGH");
        testAlert.setAlertMessage("CPU使用率超过80%");
        testAlert.setAlertTime(LocalDateTime.now());
    }

    @Test
    public void testSendEmailNotification_Success() {
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        assertDoesNotThrow(() -> {
            notificationService.sendEmailNotification("test@example.com", testAlert);
        });

        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    public void testSendEmailNotification_NullRecipient() {
        assertThrows(IllegalArgumentException.class, () -> {
            notificationService.sendEmailNotification(null, testAlert);
        });

        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    public void testSendEmailNotification_EmptyRecipient() {
        assertThrows(IllegalArgumentException.class, () -> {
            notificationService.sendEmailNotification("", testAlert);
        });

        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    public void testSendEmailNotification_NullAlert() {
        assertThrows(IllegalArgumentException.class, () -> {
            notificationService.sendEmailNotification("test@example.com", null);
        });

        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    public void testFormatAlertMessage() {
        String message = notificationService.formatAlertMessage(testAlert);

        assertNotNull(message);
        assertTrue(message.contains("CPU使用率告警"));
        assertTrue(message.contains("HIGH"));
    }
}
