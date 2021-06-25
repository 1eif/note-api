package com.leif.util;


import com.leif.exception.ServiceException;
import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

public class XmlUtil {

    /**
     * 解析Xml
     * @param xml
     * @return
     */
    public static Map<String, String> parseXmlToMap(String xml) {
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();

        DocumentBuilder builder = null;
        try {
            builder = builderFactory.newDocumentBuilder();
            Document document = builder.parse(IOUtils.toInputStream(xml, StandardCharsets.UTF_8));

            Element rootElement = document.getDocumentElement();
            NodeList nodes = rootElement.getChildNodes();

            Map<String, String> map = new LinkedHashMap<>();
            for (int i = 0; i < nodes.getLength(); i++) {
                Node node = nodes.item(i);
                if (node instanceof Element) {
                    map.put(node.getNodeName(), node.getTextContent());
                }
            }
            return map;

        } catch (Exception e) {
            throw new ServiceException("解析Xml异常", e);
        }
    }
}
