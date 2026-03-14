package com.acasado.opored_scraping.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.net.URI;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
@EnableScheduling
@Slf4j
public class BoeApiCallerService {
    private final WebClientService webClientService;
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

    public BoeApiCallerService(WebClientService webClientService) {
        this.webClientService = webClientService;
    }
    // Although the BOE is usually published at the beginning of the day, we set up two calls to ensure data reception in case it has been published later or there was a problem with the API.
    @Scheduled(fixedRate = 180000)
    @Scheduled(cron = "0 0 10,16 * * *")
    public Mono<Object> extractBoeData() {
        log.info("Automated BOE call for date: {}", LocalDate.now());
        final String BOE_BASE_URL = "https://boe.es/datosabiertos/api/boe/sumario/";
        String today = LocalDate.now().format(formatter);

        URI completedUri = URI.create(BOE_BASE_URL + today);

        return webClientService.extractData(completedUri, "boe");
    }
}
