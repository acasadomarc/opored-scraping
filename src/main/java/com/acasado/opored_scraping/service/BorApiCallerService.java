package com.acasado.opored_scraping.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.LocalDate;

@Service
@EnableScheduling
@Slf4j
public class BorApiCallerService {
    private final WebClientService webClientService;

    public BorApiCallerService(WebClientService webClientService) {
        this.webClientService = webClientService;
    }
    // Although the BOR is usually published at the beginning of the day, we set up two calls to ensure data reception in case it has been published later or there was a problem with the API.
    @Scheduled(fixedRate = 150000)
    @Scheduled(cron = "0 0 11,17 * * *")
    public Mono<Object> extractBorData() {
        log.info("Automated BOR call for date: {}", LocalDate.now());
        URI completedUri = URI.create("https://ias1.larioja.org/boletin/ExportarBoletinServlet");

        return webClientService.extractData(completedUri, "bor");
    }
}
