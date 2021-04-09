/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package be.digon.dform;

import java.io.Serializable;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

/**
 *
 * @author rombouts
 */
@ViewScoped
@Named
public class FormEditorController implements Serializable {

   
//        static public final String DATETIME_INPUT = "DATETIME_INPUT";
    @Inject
    private FormEditorModel formEditorModel;

    public FormEditorModel getFormEditorModel() {
        return formEditorModel;
    }

    public void setFormEditorModel(FormEditorModel formEditorModel) {
        this.formEditorModel = formEditorModel;
    }

    public void inputFieldTypeChanged(ValueChangeEvent e) {
        formEditorModel.inputFieldTypeChanged((String) e.getNewValue());
       
    }

    public void updateXHTML(ActionEvent e) {
        formEditorModel.updatePreCode();
    }
}
