package com.bankflow.ai;

import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class DrawioDocumentParser implements RagDocumentParser {

    public static final String DRAWIO_XML = ".drawio.xml";

    @Override
    public boolean supports(Path path) {

        if (path == null || path.getFileName() == null) {
            return false;
        }

        return path.getFileName()
                .toString()
                .toLowerCase()
                .endsWith(DRAWIO_XML);
    }

    @Override
    public RagDocument parse(Path path) {

        if (path == null) {
            throw new IllegalArgumentException(
                    "Draw.io document path must not be null"
            );
        }

        try {
            String content = Files.readString(path);

            Document xmlDocument = parseXml(content);

            Element diagram = findDiagram(xmlDocument);

            String title = extractTitle(
                    diagram,
                    path
            );

            return new RagDocument(
                    path.toString(),
                    RagSourceType.DRAWIO_XML,
                    title,
                    content
            );

        } catch (IOException | SAXException | ParserConfigurationException e) {
            throw new IllegalStateException(
                    "Failed to parse Draw.io XML document: " + path,
                    e
            );
        }
    }

    private Document parseXml(String content)
            throws ParserConfigurationException, IOException, SAXException {

        DocumentBuilderFactory factory =
                DocumentBuilderFactory.newInstance();

        /*
         * Draw.io files are repository documents, so do not allow
         * external entities, external DTDs, or external schemas.
         */
        factory.setFeature(
                "http://apache.org/xml/features/disallow-doctype-decl",
                true
        );

        factory.setFeature(
                "http://xml.org/sax/features/external-general-entities",
                false
        );

        factory.setFeature(
                "http://xml.org/sax/features/external-parameter-entities",
                false
        );

        factory.setFeature(
                "http://apache.org/xml/features/nonvalidating/load-external-dtd",
                false
        );

        factory.setAttribute(
                XMLConstants.ACCESS_EXTERNAL_DTD,
                ""
        );

        factory.setAttribute(
                XMLConstants.ACCESS_EXTERNAL_SCHEMA,
                ""
        );

        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);

        return factory
                .newDocumentBuilder()
                .parse(new InputSource(new StringReader(content)));
    }

    private Element findDiagram(Document document) {

        var diagrams = document.getElementsByTagName("diagram");

        if (diagrams.getLength() == 0) {
            return null;
        }

        return (Element) diagrams.item(0);
    }

    private String extractTitle(
            Element diagram,
            Path path) {

        if (diagram != null) {

            String name = diagram.getAttribute("name");

            if (!name.isBlank()) {
                return name.trim();
            }
        }

        String fileName =
                path.getFileName().toString();

        if (fileName.toLowerCase().endsWith(DRAWIO_XML)) {
            return fileName.substring(
                    0,
                    fileName.length() - DRAWIO_XML.length()
            );
        }

        return fileName;
    }
}