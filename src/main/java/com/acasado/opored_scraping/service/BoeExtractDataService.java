package com.acasado.opored_scraping.service;

import com.acasado.opored_scraping.dto.BoeAnnouncementDTO;
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

@Service("boe")
@Slf4j
@RequiredArgsConstructor
public class BoeExtractDataService implements ExtractDataService {

    private final KafkaProducer kafkaProducer;

    public void extractData(String xmlContent) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(xmlContent)));

            XPath xpath = XPathFactory.newInstance().newXPath();

            String expression = "//seccion[@codigo='2B']";
            NodeList section2B = (NodeList) xpath.compile(expression).evaluate(doc, XPathConstants.NODESET);

            if (section2B.getLength() > 0) {
                // Extract departments and items
                String deptExpression = "./departamento";
                NodeList departments = (NodeList) xpath.compile(deptExpression)
                        .evaluate(section2B.item(0), XPathConstants.NODESET);
                for (int i = 0; i < departments.getLength(); i++) {
                    String itemExpression = ".//item";
                    NodeList items = (NodeList) xpath.compile(itemExpression)
                            .evaluate(departments.item(i), XPathConstants.NODESET);

                    for (int j = 0; j < items.getLength(); j++) {
                        kafkaProducer.sendBoeMessage(new BoeAnnouncementDTO(
                                (String) xpath.evaluate("./titulo", items.item(j), XPathConstants.STRING),
                                (String) xpath.evaluate("./url_html", items.item(j), XPathConstants.STRING),
                                (String) xpath.evaluate("./url_pdf", items.item(j), XPathConstants.STRING),
                                (String) xpath.evaluate("./identificador", items.item(j), XPathConstants.STRING),
                                LocalDate.now()));

                    }
                }
                log.info("BOE data extracted and sent successfully");
            }
            else {
                throw new NoDataException("No results found");
            }
            // Catch all possible exception types instead of the generic Exception to allow NoDataException to cross
        } catch (SAXException | IOException | XPathExpressionException | ParserConfigurationException e) {
            log.error("Error parsing xml: {}", e.getMessage());
        }
    }
}

