package com.acasado.opored_scraping.service;

import com.acasado.opored_scraping.dto.BocylAnnouncementDTO;
import com.acasado.opored_scraping.exception.NoDataException;
import com.acasado.opored_scraping.service.kafka.KafkaProducer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service("bocyl")
@Slf4j
@RequiredArgsConstructor
public class BocylExtractDataService implements ExtractDataService {

    private final KafkaProducer kafkaProducer;

    public void extractData(String jsonContent) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode jsonNodes = mapper.readTree(jsonContent);
            JsonNode results = jsonNodes.get("results");

            if (results.isEmpty()) {
                throw new NoDataException("No results found");
            }
            int resultsSent = 0;
            for (JsonNode result : results) {

                if (result.get("subseccion").asText().equals("B. AUTORIDADES Y PERSONAL")) {
                    kafkaProducer.sendBocylMessage(new BocylAnnouncementDTO(result.get("titulo").asText(),
                            result.get("enlace_fichero_html").asText(),
                            result.get("enlace_fichero_pdf").asText(),
                            LocalDate.now().minusDays(1)));
                    resultsSent++;
                }
            }
            if (resultsSent > 0) {
                log.info("BOCYL data extracted and sent successfully");
            }
            else {
                log.info("BOCYL data extracted but not suitable announcements found");
            }
        }
        catch (JsonProcessingException e) {
            log.error("Error parsing json: {}", e.getMessage());
        }
    }
}
