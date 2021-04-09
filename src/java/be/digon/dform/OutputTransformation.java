/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package be.digon.dform;

import java.io.IOException;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author rombouts
 */
public class OutputTransformation {

    public OutputTransformation() {
        setUuid("new");
        setId(0);
        setTitle("New");
        setOutputType("xls");
    }

    public boolean isNew() {
        return getUuid().equals("new");
    }
    private Integer id;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
    private String uuid;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
    private String title;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
    private String outputType;

    public String getOutputType() {
        return outputType;
    }

    public void setOutputType(String outputType) {
        this.outputType = outputType;
    }
    private String xslt;

    public String getXslt() {
        return xslt;
    }

    public void setXslt(String xslt) {
        this.xslt = xslt;
    }

    public Document createXsltDocument() throws Exception {

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true); // required for using the resulting xslt document as a DOMSource for a transformer...
        DocumentBuilder builder = factory.newDocumentBuilder();

        Document document = builder.parse(new InputSource(new StringReader(xslt)));

        return document;
    }

    private static List<String> detectInstanceNamesInNodes(NodeList nl) {
        List<String> instanceNames = new ArrayList<String>();

        for (int i = 0; i < nl.getLength(); i++) {
            Element n = (Element) nl.item(i);
            if (n.getAttribute("value").equals("#{p.instance}")) {
                NodeList subNodes = n.getChildNodes();
                for (int j = 0; j < subNodes.getLength(); j++) {
                    NamedNodeMap attribs = subNodes.item(j).getAttributes();
                    if (attribs != null) {
                        Node itemValAttr = attribs.getNamedItem("itemValue");

                        if (itemValAttr != null) {
                            instanceNames.add(itemValAttr.getNodeValue());
                        }
                    }
                }
            }
        }
        return instanceNames;
    }

    public static OutputTransformation genFullOutTrans() throws IOException, ParserConfigurationException, SAXException {
        OutputTransformation ot = new OutputTransformation();
        SimpleDateFormat sdf = new SimpleDateFormat("yyy-MM-dd_HH:mm:ss");

        ot.setTitle("OutTrans_" + sdf.format(new Date()));
        StringBuilder outTransXslt = new StringBuilder();
        StringBuilder detailXslt = new StringBuilder();
        StringBuilder header1Xslt = new StringBuilder();
        StringBuilder header2Xslt = new StringBuilder();
        StringBuilder header3Xslt = new StringBuilder();
        DataConnector dc = new DataConnector();

        // Iterate over all forms
        // Iterate over all possible instances
        // Iterate over all parameters

        List<Form> lf = dc.retrieveAvailableFormsList();

        header1Xslt.append("<td>");
        header1Xslt.append("");
        header1Xslt.append("</td>");
        header2Xslt.append("<td>");
        header2Xslt.append("Patient");
        header2Xslt.append("</td>");
        header3Xslt.append("<td>");
        header3Xslt.append("code");
        header3Xslt.append("</td>");
        detailXslt.append("   <td>\n");
        detailXslt.append("   <xsl:value-of select=\"@alfacode\"/>\n");
        detailXslt.append("   </td>\n");

        for (Form f : lf) {
            String formSource = dc.retrieveFormSource(f.getUuid());
            String formName = f.getName();
            formSource = "<root>" + formSource + "</root>";
            //           outTrans = outTrans + formSource;
            try {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();

                Document document = builder.parse(new InputSource(new StringReader(formSource)));



                // Find identifiers of instances :
                List<String> instanceNames = new ArrayList<String>();
                instanceNames.addAll(detectInstanceNamesInNodes(document.getElementsByTagName("df:selectOneRadio")));
                instanceNames.addAll(detectInstanceNamesInNodes(document.getElementsByTagName("df:selectOneMenu")));

                if (instanceNames.isEmpty()) {
                    instanceNames.add("__singleinstance__");
                }

                for (String instanceName : instanceNames) {

//                    outTrans.append(" Instance : " + instanceName + "\n");

                    Pattern pattern = Pattern.compile("#\\{p.(\\w+)\\}");

                    Matcher matcher = pattern.matcher(formSource);

                    boolean found = false;
                    while (matcher.find()) {
                        /*
                         * outTrans.append(String.format(" Text" + " \"%s\"
                         * starting at " + "index %d and ending at index
                         * %d.%n\n", matcher.group(1), matcher.start(1),
                         * matcher.end(1)));
                         */
                        String parameter = matcher.group(1);

                        if (!parameter.equals("instance")) {

                            header1Xslt.append("<td>");
                            header1Xslt.append(formName);
                            header1Xslt.append("</td>");
                            header2Xslt.append("<td>");
                            header2Xslt.append(instanceName.equals("__singleinstance__") ? "" : instanceName);
                            header2Xslt.append("</td>");
                            header3Xslt.append("<td>");
                            header3Xslt.append(parameter);
                            header3Xslt.append("</td>");
                            detailXslt.append("   <td>\n");
                            detailXslt.append("   <xsl:value-of select=\"DigonFormData[@formname='");
                                    detailXslt.append(formName );
                                    detailXslt.append("']");
                            if (! instanceName.equals("__singleinstance__")){
                                detailXslt.append("[@instance='");
                                detailXslt.append(instanceName);
                                detailXslt.append("']");
                            }
                            detailXslt.append("/KeyValue[@Key='");
                            detailXslt.append(parameter);
                            detailXslt.append("']/@Value\"/>\n");
                            detailXslt.append("   </td>\n");
                        }

                    }

                    /*
                     * *
                     * <!-- Document : teststylesheet.xsl Created on : 28
                     * oktober 2012, 9:53 Author : rombouts Description: Purpose
                     * of transformation follows. -->
                     *
                     * <th> <td type="N">Patient ID</td> <td
                     * type="S">Pediatrician Name</td> <td type="D">Birth
                     * date</td> </th> <tr> <td >5.5</td> <td >test</td>
                     * <td>2012-09-09</td> *
                     *
                     * </tr>
                     *
                     *
                     *
                     */

                }
            } catch (Exception e) {
                System.out.println(e.getMessage() + formSource);
            }
        }
        outTransXslt.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        outTransXslt.append("<xsl:stylesheet xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\" version=\"1.0\">\n");
        outTransXslt.append("<xsl:output method=\"html\"/>\n");
        outTransXslt.append("<xsl:template match=\"/\">\n");
        outTransXslt.append(" <table>\n");

        // Headers :
        outTransXslt.append("  <th>");
        outTransXslt.append(header1Xslt);
        outTransXslt.append("  </th>\n");
        outTransXslt.append("  <th>");
        outTransXslt.append(header2Xslt);
        outTransXslt.append("  </th>\n");
        outTransXslt.append("  <th>");
        outTransXslt.append(header3Xslt);
        outTransXslt.append("  </th>\n");


        outTransXslt.append(" <xsl:for-each select=\"DigonDFormOutput/subject\">\n");
        outTransXslt.append("  <tr>\n");

        // Detail-xslt :
        outTransXslt.append(detailXslt);

        outTransXslt.append("  </tr>\n");
        outTransXslt.append(" </xsl:for-each>\n");
        outTransXslt.append(" </table>\n</xsl:template>\n");
        outTransXslt.append("</xsl:stylesheet>\n");
        ot.setXslt(outTransXslt.toString());
        return ot;
    }
}
