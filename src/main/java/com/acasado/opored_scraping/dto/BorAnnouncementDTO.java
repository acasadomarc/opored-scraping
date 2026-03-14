package com.acasado.opored_scraping.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class BorAnnouncementDTO extends KafkaAnnouncementDTO{
    public BorAnnouncementDTO(String title, String htmlUrl, String pdfUrl, LocalDate publicationDate) {
        super(title, htmlUrl, pdfUrl, publicationDate);
    }
}
