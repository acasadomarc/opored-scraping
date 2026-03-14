package com.acasado.opored_scraping.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public abstract class KafkaAnnouncementDTO {
    private String title;
    private String htmlUrl;
    private String pdfUrl;
    private LocalDate publicationDate;
}
