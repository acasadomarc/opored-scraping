package com.acasado.opored_scraping.service;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WebClientServiceTest {

    private MockWebServer mockWebServer;

    @BeforeEach
    void setUp() throws Exception {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @AfterEach
    void tearDown() throws Exception {
        mockWebServer.shutdown();
    }

    @Test
    void shouldCallExtractorAndReturnNonNull_whenResponseIs200() {
        // Arrange
        MockResponse response = new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/xml")
                .setBody("<root><item>ok</item></root>");
        mockWebServer.enqueue(response);

        WebClient.Builder builder = WebClient.builder()
                .baseUrl(mockWebServer.url("/").toString());

        ExtractDataService extractor = mock(ExtractDataService.class);
        Map<String, ExtractDataService> extractors = new HashMap<>();
        extractors.put("boe", extractor); // key is looked up using organization.toLowerCase()

        WebClientService service = new WebClientService(builder, extractors);

        // Act
        service.extractData(URI.create(mockWebServer.url("/").toString()), "boe")
                .block();

        // Assert
        verify(extractor, times(1)).extractData(anyString());
    }

    @Test
    void shouldReturnEmptyMono_whenResponseIs404() {
        // Arrange
        MockResponse response = new MockResponse()
                .setResponseCode(404);
        mockWebServer.enqueue(response);

        WebClient.Builder builder = WebClient.builder()
                .baseUrl(mockWebServer.url("/").toString());

        ExtractDataService extractor = mock(ExtractDataService.class);
        Map<String, ExtractDataService> extractors = new HashMap<>();
        extractors.put("boe", extractor);

        WebClientService service = new WebClientService(builder, extractors);

        // Act
        Object result = service.extractData(URI.create(mockWebServer.url("/notfound").toString()), "boe")
                .block();

        // Assert
        // In the service 404 -> NoDataException -> onErrorResume -> Mono.empty()
        assertNull(result, "Expected null (empty Mono) when remote returns 404");
        verify(extractor, never()).extractData(anyString());
    }

    @Test
    void shouldRetryAndEventuallyReturnEmpty_whenServerReturns5xx() {
        // Arrange: Encolamos 3 errores para agotar los reintentos (o 4 para estar seguros)
        mockWebServer.enqueue(new MockResponse().setResponseCode(500));
        mockWebServer.enqueue(new MockResponse().setResponseCode(500));
        mockWebServer.enqueue(new MockResponse().setResponseCode(500));
        mockWebServer.enqueue(new MockResponse().setResponseCode(500));

        WebClient.Builder builder = WebClient.builder();
        ExtractDataService extractor = mock(ExtractDataService.class);
        Map<String, ExtractDataService> extractors = Map.of("boe", extractor);
        WebClientService service = new WebClientService(builder, extractors);

        // Act
        Object result = service.extractData(URI.create(mockWebServer.url("/").toString()), "boe")
                .block();

        // Assert
        assertNull(result);
        verify(extractor, never()).extractData(anyString());
        // Opcional: verificar que se hicieron los reintentos en el mockWebServer
        assertTrue(mockWebServer.getRequestCount() > 1);
    }
}
