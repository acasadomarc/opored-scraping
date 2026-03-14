package com.acasado.opored_scraping.service;

import com.acasado.opored_scraping.exception.ClientException;
import com.acasado.opored_scraping.exception.NoDataException;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.shaded.com.google.protobuf.ServiceException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.netty.http.client.HttpClient;
import reactor.util.retry.Retry;

import java.net.URI;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class WebClientService {
    private final WebClient webClient;
    private final Map<String, ExtractDataService> extractors;

    public WebClientService(WebClient.Builder webClientBuilder, Map<String, ExtractDataService> extractors) {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .responseTimeout(Duration.ofMillis(5000))
                .doOnConnected(conn ->
                        conn.addHandlerLast(new ReadTimeoutHandler(5000, TimeUnit.MILLISECONDS))
                                .addHandlerLast(new WriteTimeoutHandler(5000, TimeUnit.MILLISECONDS)));

        this.webClient = webClientBuilder
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML_VALUE)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                /* Codecs are used to increase the memory size for the buffer and avoid DataBufferLimitException
                 that happened sometimes with BOE call */
                .codecs(codecs -> codecs
                        .defaultCodecs()
                        .maxInMemorySize(500 * 1024))
                .build();

        this.extractors = extractors;
    }

    public Mono<Object> extractData(URI uri, String organization) {
        return webClient.get()
                .uri(uri)
                .retrieve()
                .onStatus(HttpStatusCode::is5xxServerError, response -> Mono.error(new ServiceException(response.statusCode().toString()))) // Server error
                .onStatus(HttpStatusCode::is4xxClientError, response -> { // Client error
                    if (response.statusCode().equals(HttpStatus.NOT_FOUND)) {
                        return Mono.error(new NoDataException(response.statusCode().toString()));
                    }
                    else {
                        return Mono.error(new ClientException(response.statusCode().toString()));
                    }
                })
                .bodyToMono(String.class)
                .retryWhen(Retry.fixedDelay(3, Duration.ofSeconds(5))
                        .filter(ServiceException.class::isInstance) // Retry only when it is a server failure and not a client failure.
                        .doBeforeRetry(retrySignal -> log.warn("Retrying connection with {}. Try number {}", organization, retrySignal.totalRetries())))
                .flatMap(json -> Mono.fromRunnable(() -> {
                            ExtractDataService extractor = extractors.get(organization.toLowerCase());
                            if (extractor == null) {
                                throw new IllegalArgumentException("No extractor registered for " + organization);
                            }
                            extractor.extractData(json);
                        }) //fromCallable prevents execution until requested via subscription
                        .subscribeOn(Schedulers.boundedElastic())) // Send the task to a thread pool to prevent it from blocking the main thread and, therefore, the entire service.
                .onErrorResume(NoDataException.class, e -> {
                    log.error("No data found for date {} in {}", LocalDate.now(), organization.toUpperCase());
                    return Mono.empty();
                })
                .onErrorResume(ClientException.class, e -> {
                    log.error("Client error: {}", e.getMessage());
                    return Mono.empty();
                })
                .onErrorResume(Exceptions::isRetryExhausted, e -> {
                    log.error("Retries exhausted for organization {}: {}", organization, e.getCause().getMessage());
                    return Mono.empty();
                })
                .doOnNext(success -> log.info("Successfully extracted data from {}", organization)); // onSuccess is not used because it is displayed even if there is a previous error.
    }
}
