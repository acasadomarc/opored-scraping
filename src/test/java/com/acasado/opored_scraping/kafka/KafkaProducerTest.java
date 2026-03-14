package com.acasado.opored_scraping.kafka;

import com.acasado.opored_scraping.dto.BoeAnnouncementDTO;
import com.acasado.opored_scraping.dto.BocylAnnouncementDTO;
import com.acasado.opored_scraping.dto.BorAnnouncementDTO;
import com.acasado.opored_scraping.service.kafka.KafkaProducer;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;


import static org.mockito.Mockito.*;

@RequiredArgsConstructor
class KafkaProducerTest {

    private KafkaTemplate<String, Object> kafkaTemplate;
    private KafkaProducer kafkaProducer;

    @BeforeEach

    void setUp() {
        // Create and inject the mock
        kafkaTemplate = mock(KafkaTemplate.class);
        kafkaProducer = new KafkaProducer(kafkaTemplate);
    }

    @Test
    void shouldForwardMessagesToKafkaTemplate() {
        // Arrange
        BocylAnnouncementDTO bocyl = new BocylAnnouncementDTO();
        BoeAnnouncementDTO boe = new BoeAnnouncementDTO();
        BorAnnouncementDTO bor = new BorAnnouncementDTO();

        // Act
        kafkaProducer.sendBocylMessage(bocyl);
        kafkaProducer.sendBoeMessage(boe);
        kafkaProducer.sendBorMessage(bor);

        // Assert
        verify(kafkaTemplate, times(1)).send("announcementsBOCYL-topic", bocyl);
        verify(kafkaTemplate, times(1)).send("announcementsBOE-topic", boe);
        verify(kafkaTemplate, times(1)).send("announcementsBOR-topic", bor);
    }
}
