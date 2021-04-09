/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package be.digon.dform;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author rombouts
 */
public class FormSourceServlet extends HttpServlet {

    
    public String getFormSource(String uuid) {

        // TODO : parameter of this method determines which xhtml to load from the DB
        // AND ui:composition should be added by this method, so then we can use other
        // xmlns:digon-namespace identifiers (and hide the implementation (ice: ace:) details.

        // TODO : the formsource servlet should either be accessible only from localhost, OR take a parameter "key" which is either a one time key,
        // or a random number generated at application startup. formsource is not protected by container
        // authentication (since ui:include should be able to call it), but we must avoid that 
        // it remains publicly available.
        String formSource;
        String formSourcePrefix = "<ui:composition  xmlns=\"http://www.w3.org/1999/xhtml\""
                + " xmlns:ui=\"http://java.sun.com/jsf/facelets\""
                + " xmlns:f=\"http://java.sun.com/jsf/core\""
                + " xmlns:h=\"http://java.sun.com/jsf/html\""
                + " xmlns:dc=\"http://primefaces.org/ui\""
                + " xmlns:dg=\"http://primefaces.org/ui\""
                + " xmlns:df=\"http://primefaces.org/ui\">";
        String formSourceSuffix = "</ui:composition>";
        DataConnector dataConnector = new DataConnector();

        formSource = dataConnector.retrieveFormSource(uuid);
        if ((formSource == null) || formSource.equals("")) {
            formSource = "<p>Form is not available</p>";
        }
        formSource = formSourcePrefix + formSource + formSourceSuffix;

        return formSource;
    }

    public String formSourceSubstituteParameters(String formSource) {
        // First : add actionlistener to instance field, and set is as required :
        //formSource = formSource.replace("value=\"#{p.instance}\"","partialSubmit=\"true\" required=\"true\" valueChangeListener=\"#{controllerBean.instanceChanged}\"  value=\"#{p.instance}\"");
    
        // Find the first ">" after the first occurrence of value=\"#{p.instance}\""
        // e.g. : <df:selectOneMenu layout="pageDirection" value="#{p.instance}" id="instance">
        // And append <df:ajax event="select... after that >
        formSource = formSource.replaceFirst("(value=\"#\\{p.instance\\}\".*>)","$1<df:ajax event=\"itemSelect\" listener=\"#{controllerBean.instanceChanged}\" update=\"belowmenu\" />");
        
        formSource = formSource.replace("value=\"#{p.", "value=\"#{modelBean.keyValueMap.");
   //     formSource = formSource.replace("<df:selectInputDate ","<df:calendar showOn=\"button\" pattern=\"dd-MM-yyyy\" "); // icefaces selectInputDate becomes primefaces datePicker
        // Then, replace all p.-parameters with keyValuemap-parameters.
        return formSource;
    }

    /**
     * Processes requests for both HTTP
     * <code>GET</code> and
     * <code>POST</code> methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        try {


            if (request.getRemoteAddr().equals("127.0.0.1")
                    || request.getRemoteAddr().equals("0:0:0:0:0:0:0:1%0")) {
                String formSource = getFormSource(request.getParameter("uuid"));
       //         System.out.println(formSourceSubstituteParameters(formSource));
                out.print(formSourceSubstituteParameters(formSource));
            } else {
                out.print("No access from " + request.getRemoteAddr());
                System.out.println("Servlet formsource : no access from " + request.getRemoteAddr());
            }
        } finally {
            out.close();
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP
     * <code>GET</code> method.
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
     * Handles the HTTP
     * <code>POST</code> method.
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
