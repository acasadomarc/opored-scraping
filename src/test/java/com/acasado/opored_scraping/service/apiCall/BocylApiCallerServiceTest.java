package com.acasado.opored_scraping.service.apiCall;

import com.acasado.opored_scraping.service.BocylApiCallerService;
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
class BocylApiCallerServiceTest {

    @Mock
    private WebClientService webClientService;

    @InjectMocks
    private BocylApiCallerService bocylApiCallerService;

    @Test
    void shouldCallWebClientService_whenExtractBocylData() {
        when(webClientService.extractData(any(URI.class), eq("bocyl")))
                .thenReturn(Mono.just("ok"));

        Mono<Object> resultMono = bocylApiCallerService.extractBocylData();

        Object result = resultMono.block();
        assertNotNull(result);
        assertEquals("ok", result);

        // Verify it called with a URI that contains the BOCYL base path
        ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
        verify(webClientService, times(1)).extractData(uriCaptor.capture(), eq("bocyl"));
        URI used = uriCaptor.getValue();
        assertTrue(used.toString().contains("https://jcyl.opendatasoft.com/"));
    }

    @Test
    void shouldPropagateEmptyMono_whenWebClientServiceReturnsEmpty() {
        when(webClientService.extractData(any(URI.class), eq("bocyl")))
                .thenReturn(Mono.empty());

        Mono<Object> resultMono = bocylApiCallerService.extractBocylData();
        assertNull(resultMono.block());
    }
}
