package com.acasado.opored_scraping.service;

import com.acasado.opored_scraping.dto.BorAnnouncementDTO;
import com.acasado.opored_scraping.exception.NoDataException;
import com.acasado.opored_scraping.service.kafka.KafkaProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.StringReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service("bor")
@Slf4j
@RequiredArgsConstructor
public class BorExtractDataService implements ExtractDataService {

    private final KafkaProducer kafkaProducer;

    public void extractData(String xmlContent) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        final String DOCUMENTS_BASE_URL = "https://ias1.larioja.org/boletin/ExportarBoletinServlet?tipo=2&fecha=" + LocalDate.now().format(formatter) + "&referencia=";
        int extractedAnnouncements = 0;

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(false); // RSS format does not use namespaces
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(xmlContent)));
            doc.getDocumentElement().normalize(); // Also required to manage RSS

            XPath xpath = XPathFactory.newInstance().newXPath();

            NodeList announcements = (NodeList) xpath.evaluate("/aplication/boletin/anuncios//anuncio", doc, XPathConstants.NODESET);

            for (int i = 0; i < announcements.getLength(); i++) {
                if (xpath.evaluate("ancestor::romano/@denominacion", announcements.item(i)).contains("AUTORIDADES Y PERSONAL")) { // Only add the advertisement if it is related to public examinations.
                    kafkaProducer.sendBorMessage(new BorAnnouncementDTO(
                            xpath.evaluate("titulo", announcements.item(i)),
                            DOCUMENTS_BASE_URL + xpath.evaluate("contenido[@tipo='html']", announcements.item(i)),
                            DOCUMENTS_BASE_URL + xpath.evaluate("contenido[@tipo='pdf']", announcements.item(i)),
                            LocalDate.now()));
                    extractedAnnouncements++;
                }
            }
            if (extractedAnnouncements == 0) {
                throw new NoDataException("No results found");
            }
            log.info("BOR data extracted and sent successfully");
        }
        // Catch all possible exception types instead of the generic Exception to allow NoDataException to cross by
        catch (SAXException | IOException | XPathExpressionException | ParserConfigurationException e) {
            log.error("Error parsing xml: {}", e.getMessage());
        }
    }
}
