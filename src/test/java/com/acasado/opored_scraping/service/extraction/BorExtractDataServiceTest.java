package com.acasado.opored_scraping.service.extraction;

import com.acasado.opored_scraping.exception.NoDataException;
import com.acasado.opored_scraping.service.BorExtractDataService;
import com.acasado.opored_scraping.service.kafka.KafkaProducer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class BorExtractDataServiceTest {

    @Mock
    private KafkaProducer kafkaProducer;

    @InjectMocks
    private BorExtractDataService service;

    @Test
    void shouldNotThrow_whenValidXmlProvided() {
        String xml = """
                <aplication status="ok" id="" resource="">
                    <boletin>
                        <cabecera>
                        <fecha>02-02-2026</fecha>
                        <anio>2026</anio>
                        <numero>21</numero>
                        <contenido tipo="pdf"> 38657717-2-X </contenido>
                        </cabecera>
                        <anuncios>
                            <romano valor="II" denominacion="AUTORIDADES Y PERSONAL">
                                <letra valor="" denominacion="***">
                                    <organo denominacion="CONSEJERÍA DE HACIENDA, GOBERNANZA PÚBLICA, SOCIEDAD DIGITAL Y PORTAVOCÍA DEL GOBIERNO">
                                        <comite denominacion="">
                                            <anuncio>
                                                <titulo>
                                                <![CDATA[ LIBRE DESIGNACIÓN: adjudicación de un puesto (LD.21/25) ]]>
                                                </titulo>
                                                <contenido tipo="pdf"> 38658060-1-PDF-574893 </contenido>
                                                <contenido tipo="html"> 38585535-3-HTML-574893-X </contenido>
                                            </anuncio>
                                        </comite>
                                    </organo>
                                </letra>
                            </romano>
                        </anuncios>
                    </boletin>
                </aplication>
                """;

        assertDoesNotThrow(() -> service.extractData(xml));
    }

    @Test
    void shouldThrowNoDataException_whenNoResults() {
        String xmlEmpty = "<boletin></boletin>";
        assertThrows(NoDataException.class, () -> service.extractData(xmlEmpty));
    }
}
