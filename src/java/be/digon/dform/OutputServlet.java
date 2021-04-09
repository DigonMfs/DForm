/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package be.digon.dform;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.w3c.dom.*;

/**
 *
 * @author rombouts
 */
@WebServlet(name = "OutputServlet", urlPatterns = {"/OutputServlet"})
public class OutputServlet extends HttpServlet {

    public static final String TYPE_NUMERIC = "N";
    public static final String TYPE_STRING = "S";
    public static final String TYPE_DATE = "D";
    public static final String TYPE_DATETIME = "T";
    public static final String TYPE_FORMULA = "F";
    public static final String TYPE_BOOLEAN = "B";
    
      @Inject private DataConnector dataConnector ;
    

    public Document generateAggregateXml(DataConnector dataConnector) throws Exception {

        List<XmlSubmissionData> xmlSubmissionDataList = dataConnector.retrieveSubmissionDataOrderedBySubject(true);

        if (xmlSubmissionDataList == null) {
            return null;
        }

        Integer previousSubjectId = null;

        Document document = null;

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        DOMImplementation impl = builder.getDOMImplementation();
        document = impl.createDocument(null, null, null);

        Element el = document.createElement("DigonDFormOutput");
        el.setAttribute("Version", "1");
        document.appendChild(el);

        Element currentSubject = null;
        for (XmlSubmissionData xmlSubmissionData : xmlSubmissionDataList) {
            if (xmlSubmissionData.getXml() != null) {
                if ((previousSubjectId == null) || (!previousSubjectId.equals(xmlSubmissionData.getSubjectId()))) {
                    currentSubject = document.createElement("subject");
                    currentSubject.setAttribute("id", xmlSubmissionData.getSubjectId().toString());
                    currentSubject.setAttribute("alfacode", xmlSubmissionData.getSubjectAlfacode());
                    el.appendChild(currentSubject);
                    previousSubjectId = xmlSubmissionData.getSubjectId();
                }

                Document subDocument = xmlSubmissionData.getDocument();

                Node imported = document.importNode(subDocument.getDocumentElement(), true);

                currentSubject.appendChild(imported);
            }
        }

        return document;
    }

    private void humanReadableResponse(HttpServletResponse response, String txt) throws IOException {
        response.setContentType("text/html;charset=UTF-8");
        response.getOutputStream().println(txt);

    }

    private Element transform(Document xml, Document xsltTransformation) throws DOMException, ParserConfigurationException, TransformerException, TransformerFactoryConfigurationError {
      
        TransformerFactory tf = TransformerFactory.newInstance();

        Document transformedDocument = null;

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        DOMImplementation impl = builder.getDOMImplementation();
        transformedDocument = impl.createDocument(null, null, null);

        Element documentElement = transformedDocument.createElement("DigonDFormOutputMatrix");
        documentElement.setAttribute("Version", "1");
        transformedDocument.appendChild(documentElement);

        DOMResult domResult = new DOMResult(documentElement);
//        streamResult.setOutputStream(response.getOutputStream());
        DOMSource xsltSource = new DOMSource(xsltTransformation); // Note that this must be a Document resulting from a setNameSpaceAware(true)- factory
        DOMSource xmlSource = new DOMSource(xml);
        Transformer transformer = tf.newTransformer(xsltSource);
        transformer.transform(xmlSource, domResult);
        return documentElement;
      
    /*
      Document transformedDocument = null;

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        DOMImplementation impl = builder.getDOMImplementation();
        transformedDocument = impl.createDocument(null, null, null);

        Element documentElement = transformedDocument.createElement("DigonDFormOutputMatrix");
        documentElement.setAttribute("Version", "1");
        transformedDocument.appendChild(documentElement);
      
      Processor processor = new Processor(false);
   XsltCompiler compiler = processor.newXsltCompiler();
   XsltExecutable stylesheet = compiler.compile(new StreamSource(new File("styles/books.xsl")));
   Serializer out = processor.newSerializer(new File("books.html"));
   out.setOutputProperty(Serializer.Property.METHOD, "html");
   //out.setOutputProperty(Serializer.Property.INDENT, "yes");
   Xslt30Transformer transformer = stylesheet.load30();
   transformer.transform(new StreamSource(new File("data/books.xml")), out);
  */
    }

    private void xmlToExcel(HttpServletResponse response, Document xml, Document xsltTransformation) throws IOException, TransformerConfigurationException, TransformerException, ParserConfigurationException, Exception {

        PrintWriter out = new PrintWriter(System.err, true);

        response.setContentType("vnd/ms-excel");
        String fileName = "DFormData.xlsx";
        response.setHeader("Content-disposition", "attachment; filename=\"" + fileName + "\"");

        Element documentElement = transform(xml, xsltTransformation);

        // allow xml/xslttransformation to be garbage collected , they are large and useless now :
        xml=null;
        xsltTransformation = null;
        
        //      System.out.println(XmlUtils.getStringFromDocument(transformedDocument));
        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        //Workbook wb = new HSSFWorkbook();
        Workbook wb = new XSSFWorkbook();
        CreationHelper createHelper = wb.getCreationHelper();
        Sheet sheet = wb.createSheet("Sheet 1");

        //  It is important to
        // create a new cell style from the workbook otherwise you can end up
        // modifying the built in style and effecting not only this cell but other cells.
        CellStyle cellDateStyle = wb.createCellStyle();
        cellDateStyle.setDataFormat(createHelper.createDataFormat().getFormat("d/m/yy"));

        CellStyle cellDateTimeStyle = wb.createCellStyle();
        cellDateTimeStyle.setDataFormat(createHelper.createDataFormat().getFormat("d/m/yy h:mm"));

        CellStyle titleCellStyle = wb.createCellStyle();
        titleCellStyle.setFillBackgroundColor(IndexedColors.YELLOW.getIndex());
        //   titleCellStyle.setBorderBottom(CellStyle.BORDER_THIN);

        Element table = (Element) documentElement.getElementsByTagName("table").item(0);

        NodeList headerLines = table.getElementsByTagName("th");
        int nrHeaderLines = headerLines.getLength();
        for (int i = 0; i < nrHeaderLines; i++) {
            Element headerLine = (Element) headerLines.item(i);

            NodeList columnHeaders = headerLine.getElementsByTagName("td");
//        String columnTypes[] = new String[columnHeaders.getLength()];
            Row sheetTitleRow = sheet.createRow(i);
            for (int j = 0; j < columnHeaders.getLength(); j++) {

                Element columnHeader = (Element) columnHeaders.item(j);
//            String type = columnHeader.getAttribute("type");
//            columnTypes[j] = type;
                String value = columnHeader.getTextContent();
                //          System.out.println("NodeValue:"+value+" type : "+type);
                Cell titleCell = sheetTitleRow.createCell(j);
                titleCell.setCellValue(value);
                titleCell.setCellStyle(titleCellStyle);
            }
        }
        NodeList rows = table.getElementsByTagName("tr");

        for (int i = 0; i < rows.getLength(); i++) {
            Element row = (Element) rows.item(i);
            Row sheetRow = sheet.createRow(i + nrHeaderLines);
            NodeList columns = row.getElementsByTagName("td");
            for (int j = 0; j < columns.getLength(); j++) {
                Element column = (Element) columns.item(j);
//                String type = columnTypes[j];
                String type = column.getAttribute("Type");
                String value = column.getTextContent();
                Cell sheetCell = sheetRow.createCell(j);
                if (!value.isEmpty()) {
                    if (type.equals(TYPE_NUMERIC)) {
                        try {
                            double dbl = Double.parseDouble(value);
                            sheetCell.setCellValue(dbl);
                        } catch (Exception e) {
                            sheetCell.setCellValue("Error:" + e.getMessage());
                        }
                    } else if (type.equals(TYPE_STRING)) {
                        sheetCell.setCellValue(value);
                    } else if (type.equals(TYPE_FORMULA)) {
                        sheetCell.setCellFormula(value);
                    } else if (type.equals(TYPE_DATE)) {
                        Date dte = null;
                        try {
                            dte = df.parse(value);
                            sheetCell.setCellValue(dte);
                            sheetCell.setCellStyle(cellDateStyle);
                        } catch (Exception e) {
                            sheetCell.setCellValue("Error:" + e.getMessage());

                        }
                    } else if (type.equals(TYPE_DATETIME)) {
                        Date dte = null;
                        try {
                            dte = dateTimeFormat.parse(value);
                            sheetCell.setCellValue(dte);
                            sheetCell.setCellStyle(cellDateTimeStyle);
                        } catch (Exception e) {
                            sheetCell.setCellValue("Error:" + e.getMessage());

                        }
                    } else {
                        // Assume string
                        sheetCell.setCellValue(value);
                    }
                }
            }
        }

        /*
         * // Create a row and put some cells in it. Rows are 0 based. Row row
         * = sheet.createRow((short)0); // Create a cell and put a value in it.
         * Cell cell = row.createCell(0); cell.setCellValue(1);
         *
         * // Or do it on one line. row.createCell(1).setCellValue(1.2);
         * row.createCell(2).setCellValue(
         * createHelper.createRichTextString("This is a string"));
         * row.createCell(3).setCellValue(true);
         */
        wb.write(response.getOutputStream());

    }

    private void xmlToXml(HttpServletResponse response, Document xml) throws IOException, TransformerConfigurationException, TransformerException, Exception {
        response.setContentType("text/xml;charset=UTF-8");
        String fileName = "DFormData.xml";
        response.setHeader("Content-disposition", "attachment; filename=\"" + fileName + "\"");

        response.getOutputStream().print(XmlUtils.getStringFromDocument(xml));
    }

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @RolesAllowed("Admin")
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // get actual content
        ServletOutputStream out = response.getOutputStream();
        response.setHeader("Cache-Control", "no-cache, no-store, max-age=0, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
        try {

          
            System.out.println("OutputServlet for " + request.getParameter("outtransid"));
            Document aggregateXml = generateAggregateXml(dataConnector);
            if (aggregateXml == null) {
                throw new Exception("Can not generate aggregate XML");
            }

//            System.out.println(XmlUtils.getStringFromDocument(aggregateXml));
            OutputTransformation ot = dataConnector.retrieveOutputTransformations(Integer.parseInt(request.getParameter("outtransid"))).get(0);
            Document xsltTransformation = ot.createXsltDocument();

            if (ot.getOutputType() == null) {
                humanReadableResponse(response, "Output type not specified for output transformation.");
            } else if (ot.getOutputType().equalsIgnoreCase("xls")) {
                xmlToExcel(response, aggregateXml, xsltTransformation);
            } else if (ot.getOutputType().equalsIgnoreCase("xml")) {
                xmlToXml(response, aggregateXml);
            } else {
                humanReadableResponse(response, "Unknown output type");
            }
        } catch (Exception e) {
            e.printStackTrace();
            out.println("Error : " + e.getMessage());
        } finally {
            out.close();
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
}
