package com.acasado.opored_scraping.service.apiCall;

import com.acasado.opored_scraping.service.BoeApiCallerService;
import com.acasado.opored_scraping.service.WebClientService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BoeApiCallerServiceTest {

    @Mock
    private WebClientService webClientService;

    @InjectMocks
    private BoeApiCallerService boeApiCallerService;

    @Test
    void shouldCallWebClientService_whenExtractBoeData() {
        when(webClientService.extractData(any(URI.class), eq("boe")))
                .thenReturn(Mono.just("boe-response"));

        Object response = boeApiCallerService.extractBoeData().block();
        assertNotNull(response);
        assertEquals("boe-response", response);

        ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
        verify(webClientService, times(1)).extractData(uriCaptor.capture(), eq("boe"));
        URI used = uriCaptor.getValue();
        assertTrue(used.toString().contains("boe.es"));
    }

    @Test
    void shouldReturnEmpty_whenWebClientServiceReturnsEmpty() {
        when(webClientService.extractData(any(URI.class), eq("boe"))).thenReturn(Mono.empty());
        assertNull(boeApiCallerService.extractBoeData().block());
    }
}
