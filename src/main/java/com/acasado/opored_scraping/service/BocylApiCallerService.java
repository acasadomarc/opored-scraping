package com.acasado.opored_scraping.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
@EnableScheduling
@Slf4j
public class BocylApiCallerService {
    private final WebClientService webClientService;
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public BocylApiCallerService(WebClientService webClientService) {
        this.webClientService = webClientService;
    }
    // Although the BOCYL is usually published at the beginning of the day, we set up two calls to ensure data reception in case it has been published later or there was a problem with the API.
    @Scheduled(cron = "0 0 12,18 * * *")
    public Mono<Object> extractBocylData() {
        // Added mono defer to reevaluate the code in each execution and pick the right date and not the date when the service is first executed
        return Mono.defer(() -> {
            log.info("Automated BOCYL call for date: {}", LocalDate.now());
            final String BOCYL_BASE_URL = "https://jcyl.opendatasoft.com/api/explore/v2.1/catalog/datasets/bocyl/records";
            URI completedUri;

            String yesterday = "%27"+ LocalDate.now().minusDays(1).format(formatter) + "%27";

            if (LocalDate.now().getDayOfWeek() == DayOfWeek.MONDAY) {
                String lastFriday = "%27"+ LocalDate.now().minusDays(3).format(formatter) + "%27";
                completedUri = URI.create(BOCYL_BASE_URL + "?where=fecha_publicacion%3Ddate" + lastFriday);
            }
            else {
                completedUri = URI.create(BOCYL_BASE_URL + "?where=fecha_publicacion%3Ddate" + yesterday);
            }
            return webClientService.extractData(completedUri, "bocyl");
        });
    }
}
