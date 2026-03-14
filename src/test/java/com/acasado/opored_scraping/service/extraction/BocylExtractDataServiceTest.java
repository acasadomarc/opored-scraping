package com.acasado.opored_scraping.service.extraction;

import com.acasado.opored_scraping.exception.NoDataException;
import com.acasado.opored_scraping.service.BocylExtractDataService;
import com.acasado.opored_scraping.service.kafka.KafkaProducer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class BocylExtractDataServiceTest {

    @Mock
    private KafkaProducer kafkaProducer;

    @InjectMocks
    private BocylExtractDataService service;

    @Test
    void shouldNotThrow_whenValidJsonProvided() {
        String validJson = """
                {
                    "results": [
                        {
                          "no_edicion": "20/2026",
                          "fecha_publicacion": "2026-01-30",
                          "seccion": "III. ADMINISTRACIÓN LOCAL",
                          "subseccion": "B. AUTORIDADES Y PERSONAL",
                          "apartado": "B.2. Oposiciones y Concursos",
                          "organismo": "DIPUTACIÓN PROVINCIAL DE LEÓN",
                          "suborganismo": null,
                          "rango": "ACUERDO",
                          "no_oficial": null,
                          "fecha_disposicion": "2025-12-05",
                          "titulo": "ACUERDO de 5 de diciembre de 2025, de la Junta de Gobierno de la Diputación Provincial de León, relativo a las bases y la convocatoria del proceso selectivo para cubrir, mediante el sistema de oposición por turno libre, 1 plaza de Oficial/a 1.ª Albañil.",
                          "pagina_inicial": 166,
                          "pagina_final": 185,
                          "enlace_fichero_pdf": "https://bocyl.jcyl.es/boletines/2026/01/30/pdf/BOCYL-D-30012026-20-38.pdf",
                          "enlace_fichero_xml": "https://bocyl.jcyl.es/boletines/2026/01/30/xml/BOCYL-D-30012026-20-38.xml",
                          "enlace_fichero_html": "https://bocyl.jcyl.es/html/2026/01/30/html/BOCYL-D-30012026-20-38.do"
                        }
                    ]
                }
                """;

        assertDoesNotThrow(() -> service.extractData(validJson));
    }

    @Test
    void shouldThrowNoDataException_whenResultsEmpty() {
        String emptyJson = """
                {
                  "results": []
                }
                """;

        assertThrows(NoDataException.class, () -> service.extractData(emptyJson));
    }
}
