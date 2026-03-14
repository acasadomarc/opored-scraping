package com.acasado.opored_scraping.service.kafka;

import com.acasado.opored_scraping.dto.BocylAnnouncementDTO;
import com.acasado.opored_scraping.dto.BoeAnnouncementDTO;
import com.acasado.opored_scraping.dto.BorAnnouncementDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendBoeMessage(BoeAnnouncementDTO boeAnnouncementDTO) {
        kafkaTemplate.send("announcementsBOE-topic", boeAnnouncementDTO);
    }

    public void sendBocylMessage(BocylAnnouncementDTO bocylAnnouncementDTO) {
        kafkaTemplate.send("announcementsBOCYL-topic", bocylAnnouncementDTO);
    }

    public void sendBorMessage(BorAnnouncementDTO borAnnouncementDTO) {
        kafkaTemplate.send("announcementsBOR-topic", borAnnouncementDTO);
    }
}
