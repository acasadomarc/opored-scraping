package com.acasado.opored_scraping.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class BoeAnnouncementDTO extends KafkaAnnouncementDTO {
    private String identifier;
    public BoeAnnouncementDTO(String title, String htmlUrl, String pdfUrl, String identifier, LocalDate publicationDate) {
        super(title, htmlUrl, pdfUrl, publicationDate);
        setIdentifier(identifier);
    }
}
