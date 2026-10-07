package com.bankflow.ai;

import com.bankflow.ai.rag.RagAudience;
import com.bankflow.ai.rag.RagChunk;
import com.bankflow.ai.rag.RagDocument;
import com.bankflow.ai.rag.RagSourceType;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.util.*;

@Component
public class DrawioChunker {

    public List<RagChunk> chunk(RagDocument document) {

        if (document == null) {
            throw new IllegalArgumentException(
                    "RAG document must not be null"
            );
        }

        if (document.sourceType()
                != RagSourceType.DRAWIO_XML) {

            throw new IllegalArgumentException(
                    "DrawioChunker supports only DRAWIO_XML documents"
            );
        }

        Document xmlDocument =
                parseXml(document.content());

        Map<String, DrawioNode> nodes =
                extractNodes(xmlDocument);

        List<DrawioEdge> edges =
                extractEdges(xmlDocument);

        String content =
                buildSemanticContent(
                        document.title(),
                        nodes,
                        edges
                );

        if (content.isBlank()) {
            return List.of();
        }

        return List.of(
                new RagChunk(
                        0,
                        "Diagram > " + document.title(),
                        content,
                        determineAudience(document)
                )
        );
    }

    private Map<String, DrawioNode> extractNodes(
            Document document) {

        Map<String, DrawioNode> nodes =
                new LinkedHashMap<>();

        NodeList cells =
                document.getElementsByTagName("mxCell");

        for (int i = 0; i < cells.getLength(); i++) {

            Node node =
                    cells.item(i);

            if (!(node instanceof Element cell)) {
                continue;
            }

            String id =
                    cell.getAttribute("id");

            String value =
                    cleanText(cell.getAttribute("value"));

            String source =
                    cell.getAttribute("source");

            String target =
                    cell.getAttribute("target");

            /*
             * A cell with source/target is an edge, not a node.
             */
            if (!source.isBlank() || !target.isBlank()) {
                continue;
            }

            /*
             * Ignore structural Draw.io cells.
             */
            if (id.isBlank()
                    || value.isBlank()) {
                continue;
            }

            nodes.put(
                    id,
                    new DrawioNode(id, value)
            );
        }

        return nodes;
    }

    private List<DrawioEdge> extractEdges(
            Document document) {

        List<DrawioEdge> edges =
                new ArrayList<>();

        NodeList cells =
                document.getElementsByTagName("mxCell");

        for (int i = 0; i < cells.getLength(); i++) {

            Node node =
                    cells.item(i);

            if (!(node instanceof Element cell)) {
                continue;
            }

            String source =
                    cell.getAttribute("source");

            String target =
                    cell.getAttribute("target");

            if (source.isBlank() || target.isBlank()) {
                continue;
            }

            String label =
                    cleanText(cell.getAttribute("value"));

            edges.add(
                    new DrawioEdge(
                            source,
                            target,
                            label
                    )
            );
        }

        return edges;
    }

    private String buildSemanticContent(
            String title,
            Map<String, DrawioNode> nodes,
            List<DrawioEdge> edges) {

        StringBuilder content =
                new StringBuilder();

        content.append("Diagram: ")
                .append(title)
                .append("\n\n");

        appendNodes(
                content,
                nodes
        );

        appendFlow(
                content,
                nodes,
                edges
        );

        return content.toString().trim();
    }

    private void appendNodes(
            StringBuilder content,
            Map<String, DrawioNode> nodes) {

        content.append("Nodes:\n");

        for (DrawioNode node : nodes.values()) {

            content.append("- ")
                    .append(node.label())
                    .append("\n");
        }

        content.append("\n");
    }

    private void appendFlow(
            StringBuilder content,
            Map<String, DrawioNode> nodes,
            List<DrawioEdge> edges) {

        content.append("Flow:\n");

        Set<String> emittedEdges =
                new HashSet<>();

        for (DrawioEdge edge : edges) {

            DrawioNode source =
                    nodes.get(edge.sourceId());

            DrawioNode target =
                    nodes.get(edge.targetId());

            if (source == null || target == null) {
                continue;
            }

            StringBuilder line =
                    new StringBuilder();

            line.append(singleLine(source.label()))
                    .append(" -> ")
                    .append(singleLine(target.label()));

            if (!edge.label().isBlank()) {

                line.append(" [")
                        .append(edge.label())
                        .append("]");
            }

            String flowLine =
                    line.toString();

            if (emittedEdges.add(flowLine)) {

                content.append(flowLine)
                        .append("\n");
            }
        }
    }

    private String singleLine(String text) {
        return text
                .replaceAll("\\s*\\n\\s*", " — ")
                .trim();
    }

    private String cleanText(String value) {

        if (value == null || value.isBlank()) {
            return "";
        }

        String text = value
                .replace("<br>", "\n")
                .replace("<br/>", "\n")
                .replace("<br />", "\n")
                .replace("&nbsp;", " ");

        text = text.replaceAll("<[^>]+>", "");

        text = text
                .replace("&amp;", "&")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&quot;", "\"")
                .replace("&#39;", "'");

        return text
                .replaceAll("[ \\t]+", " ")
                .replaceAll("\\s*\\n\\s*", "\n")
                .trim();
    }

    private RagAudience determineAudience(
            RagDocument document) {

        /*
         * Draw.io architecture/workflow diagrams currently describe
         * shared BankFlow system behavior.
         */
        return RagAudience.SHARED;
    }

    private Document parseXml(String content) {

        try {

            DocumentBuilderFactory factory =
                    DocumentBuilderFactory.newInstance();

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
                    .parse(
                            new InputSource(
                                    new StringReader(content)
                            )
                    );

        } catch (
                ParserConfigurationException
                | IOException
                | SAXException e) {

            throw new IllegalStateException(
                    "Failed to parse Draw.io XML content",
                    e
            );
        }
    }

    private record DrawioNode(
            String id,
            String label
    ) {
    }

    private record DrawioEdge(
            String sourceId,
            String targetId,
            String label
    ) {
    }
}