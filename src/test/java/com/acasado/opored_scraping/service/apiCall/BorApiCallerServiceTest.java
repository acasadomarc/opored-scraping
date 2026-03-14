package com.acasado.opored_scraping.service.apiCall;

import com.acasado.opored_scraping.service.BorApiCallerService;
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
class BorApiCallerServiceTest {

    @Mock
    private WebClientService webClientService;

    @InjectMocks
    private BorApiCallerService borApiCallerService;

    @Test
    void shouldCallWebClientService_whenExtractBorData() {
        when(webClientService.extractData(any(URI.class), eq("bor")))
                .thenReturn(Mono.just("bor-response"));

        Object response = borApiCallerService.extractBorData().block();
        assertNotNull(response);
        assertEquals("bor-response", response);

        ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
        verify(webClientService, times(1)).extractData(uriCaptor.capture(), eq("bor"));
        URI used = uriCaptor.getValue();
        assertTrue(used.toString().contains("larioja.org"));
    }

    @Test
    void shouldReturnEmpty_whenWebClientServiceReturnsEmpty() {
        when(webClientService.extractData(any(URI.class), eq("bor"))).thenReturn(Mono.empty());
        assertNull(borApiCallerService.extractBorData().block());
    }
}
