/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package be.digon.dform;

import java.io.StringWriter;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;

/**
 *
 * @author rombouts
 */
public class XmlUtils {
       /** method to convert Document to String
     *
     * @param doc
     * @return
     * @throws Exception
     */
    public static String getStringFromDocument(Document doc) throws Exception {
        String retValue;

        try {
            DOMSource domSource = new DOMSource(doc);
            StringWriter writer = new StringWriter();
            StreamResult result = new StreamResult(writer);
            TransformerFactory tf = TransformerFactory.newInstance();
         //if specified, then error : transformer does not recognize attribute "ENCODING" :    tf.setAttribute(OutputKeys.ENCODING, "UTF-8");

            Transformer transformer = tf.newTransformer();

            transformer.transform(domSource, result);
            retValue = writer.toString();
        } catch (TransformerException ex) {
            throw ex;
        }

        return retValue;
    }

    
}
