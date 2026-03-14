package com.acasado.opored_scraping.service.extraction;

import com.acasado.opored_scraping.exception.NoDataException;
import com.acasado.opored_scraping.service.BoeExtractDataService;
import com.acasado.opored_scraping.service.kafka.KafkaProducer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class BoeExtractDataServiceTest {

    @Mock
    private KafkaProducer kafkaProducer;

    @InjectMocks
    private BoeExtractDataService service;

    @Test
    void shouldNotThrow_whenValidXmlProvided() {
        String xml = """
                <seccion codigo="2B" nombre="II. Autoridades y personal. - B. Oposiciones y concursos">
                  <departamento codigo="1820" nombre="CONSEJO GENERAL DEL PODER JUDICIAL">
                    <item>
                    <identificador>BOE-A-2026-2339</identificador>
                    <control>2026/1542</control>
                    <titulo>Acuerdo de 20 de enero de 2026</titulo>
                    <url_pdf>https://www.boe.es/BOE-A-2026-2339.pdf</url_pdf>
                    <url_html>https://www.boe.es/</url_html>
                    <url_xml>https://www.boe.es/</url_xml>
                    </item>
                  </departamento>
                </seccion>
                """;

        assertDoesNotThrow(() -> service.extractData(xml));
    }

    @Test
    void shouldThrowNoDataException_whenNoResults() {
        String xmlEmpty = "<sumario></sumario>";
        assertThrows(NoDataException.class, () -> service.extractData(xmlEmpty));
    }
}
