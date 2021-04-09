/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package be.digon.dform;

import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import java.util.TreeMap;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author rombouts
 */
public class XmlSubmissionData {

    private String xml;

    public String getXml() {
        return xml;
    }

    public void setXml(String xml) {
        this.xml = xml;
        this.document = null; // Will lazily generate document next time it is requested.
        this.keyValueMap = null;
    }
    
    private String subjectAlfacode;

    public String getSubjectAlfacode() {
        return subjectAlfacode;
    }

    public void setSubjectAlfacode(String subjectAlfacode) {
        this.subjectAlfacode = subjectAlfacode;
    }

    
    private Integer subjectId;

    public Integer getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(Integer subjectId) {
        this.subjectId = subjectId;
    }
    private Integer id;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
    private String instance;

    public String getInstance() {
        return instance;
    }

    public void setInstance(String instance) {
        this.instance = instance;
    }
    private Document document = null;

    Document getDocument() throws IOException, ParserConfigurationException, SAXException {
        if (document == null) {

            if ((getXml() != null) && (!getXml().isEmpty())) {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();

                document = builder.parse(new InputSource(new StringReader(getXml())));
            }
        }
        return document;
    }
    TreeMap<String, Object> keyValueMap = null;

    TreeMap<String, Object> getKeyValueMap() throws IOException, ParserConfigurationException, SAXException {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        df.setTimeZone(TimeZone.getTimeZone("GMT"));
        if ((keyValueMap == null) && (getDocument() != null)) {
            Element r = getDocument().getDocumentElement();
            Node keyValue = r.getFirstChild();
            keyValueMap = new TreeMap<String, Object>();
            while (keyValue != null) {
                if (keyValue.getNodeName().equals("KeyValue")) {
                    Element kv = (Element) keyValue;
                    String value = kv.getAttribute("Value");
                    String tpe = kv.getAttribute("Type");
                    if (tpe == null) {
                        tpe = "";
                    }
                    Object v = null;
                    if (tpe.equals(OutputServlet.TYPE_NUMERIC)) {
                        v = new BigDecimal(value);
                    } else if (tpe.equals(OutputServlet.TYPE_DATE)) {
                        try {
                            v = df.parse(value);
                        } catch (Exception e) {
                            v = null;
                        }
                    } else if (tpe.equals(OutputServlet.TYPE_BOOLEAN)) {
                        try {
                            v = new Boolean(value);
                        } catch (Exception e) {
                            v = null;
                        }
                    } else {
                        v = value;
                    }

                    if (v != null) {

                        keyValueMap.put(kv.getAttribute("Key"), v);
                    }
                }
                keyValue = keyValue.getNextSibling();
            }
        }
        return keyValueMap;
    }
    private boolean finalSubmission = false;

    public boolean isFinalSubmission() {
        return finalSubmission;
    }

    public void setFinalSubmission(boolean finalSubmission) {
        this.finalSubmission = finalSubmission;
    }

    
}
