/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package be.digon.dform;

import java.io.Serializable;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

/**
 *
 * @author rombouts
 */
@ViewScoped
@Named
public class ReportController implements Serializable {

    @Inject
    private ControllerBean controllerBean;

    public ControllerBean getControllerBean() {
        return controllerBean;
    }
    

    public void setControllerBean(ControllerBean controllerBean) {
        this.controllerBean = controllerBean;
    }

    private FormSubject selectedSubjectInReport;

    public FormSubject getSelectedSubjectInReport() {
        return selectedSubjectInReport;
    }

    public void setSelectedSubjectInReport(FormSubject selectedSubjectInReport) {
        this.selectedSubjectInReport = selectedSubjectInReport;
    }
    
    
    
    public void selectListener() {
        controllerBean.selectAnotherSubject(getSelectedSubjectInReport());
        //      setRenderSingleUserEditor(true);
    }
}
