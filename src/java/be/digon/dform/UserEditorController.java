/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package be.digon.dform;

import java.io.Serializable;
import javax.faces.application.FacesMessage;
import javax.faces.event.ActionEvent;
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
public class UserEditorController implements Serializable {

    
    public UserEditorController() {
    }
    
    @Inject
    private UserEditorModel userEditorModel;

    public UserEditorModel getUserEditorModel() {
        return userEditorModel;
    }

    public void setUserEditorModel(UserEditorModel userEditorModel) {
        this.userEditorModel = userEditorModel;
    }

    public SelectItem[] getGroupItems() {
        SelectItem[] selectItems = new SelectItem[5];
        selectItems[0] = new SelectItem("Admin");
        selectItems[1] = new SelectItem("Pediatrician");
        selectItems[2] = new SelectItem("Center");
        selectItems[3] = new SelectItem("AllPatientsIndividual");
        selectItems[4] = new SelectItem("AllPatientsExport");
        return selectItems;
    }

    public SelectItem[] getSegmentItems() {
        SelectItem[] selectItems = new SelectItem[8];
        selectItems[0] = new SelectItem("1", "UZ Gent");
        selectItems[1] = new SelectItem("2", "UZ Antwerpen");
        selectItems[2] = new SelectItem("3", "AZ St.-Jan Brugge");
        selectItems[3] = new SelectItem("4", "Genk");
        selectItems[4] = new SelectItem("5", "UZ Leuven");
        selectItems[5] = new SelectItem("6", "St.-Augustinus Wilrijk");
        selectItems[6] = new SelectItem("7", "ZNA Middelheim Antwerpen");
        selectItems[7] = new SelectItem("8", "AZ Turnhout");

        return selectItems;
    }

    
    private User selectedUserInList;

    public User getSelectedUserInList() {
        return selectedUserInList;
    }

    public void setSelectedUserInList(User selectedUserInList) {
        this.selectedUserInList = selectedUserInList;
    }
    
    
    public void selectListener() {
        userEditorModel.setCurrentUser(selectedUserInList);
        //      setRenderSingleUserEditor(true);
    }

    private boolean renderSingleUserEditor = false;

    public void setRenderSingleUserEditor(boolean renderSingleUserEditor) {
        this.renderSingleUserEditor = renderSingleUserEditor;
        if (renderSingleUserEditor) {
          //  userLoginField.requestFocus();
        }
    }

    public boolean isRenderSingleUserEditor() {

        return renderSingleUserEditor;//userEditorModel.getCurrentUser() != null;
    }

    public void cancelEvent() {
        setRenderSingleUserEditor(false);
    }

    public void saveAndCloseEvent(ActionEvent event) {
        try {
            //            System.out.println(userEditorModel.getCurrentUser().getName());
            boolean isNewUser = userEditorModel.getCurrentUser().isNew();
            userEditorModel.saveUser();
            if (isNewUser) {
                userEditorModel.setUserList(null);// Will result in a reload.
            }
            ControllerBean.facesMessage("The data was successfully submitted", FacesMessage.SEVERITY_INFO);
            setRenderSingleUserEditor(false);
        } catch (Exception e) {
            ControllerBean.facesMessage("The data could not be submitted :  " + e.getMessage(), FacesMessage.SEVERITY_ERROR);

            e.printStackTrace();
        }

    }

    public void newUserEvent(ActionEvent event) {
        userEditorModel.newUser();
        setRenderSingleUserEditor(true);
    }

    public void editUserEvent(ActionEvent event) {
        if (userEditorModel.getCurrentUser() != null) {
            setRenderSingleUserEditor(true);
        }

    }

    public void passwordEvent(ActionEvent event) {
        if (userEditorModel.getCurrentUser() != null) {
            setPassPopupVisible(true);
        }
    }

    public void passwordCancelEvent(){
    setPassPopupVisible(false);    
    }
    
    public void passwordSetEvent(ActionEvent event) {
        if (userEditorModel.getCurrentUser() != null) {
            try {
                userEditorModel.savePassword();
                ControllerBean.facesMessage("The data was successfully submitted", FacesMessage.SEVERITY_INFO);
            } catch (Exception e) {
                ControllerBean.facesMessage("The data could not be submitted :  " + e.getMessage(), FacesMessage.SEVERITY_ERROR);

                e.printStackTrace();
            }
        }
        setPassPopupVisible(false);
    }
    private boolean passPopupVisible;

    public boolean isPassPopupVisible() {
        return passPopupVisible;
    }

    public void setPassPopupVisible(boolean passPopupVisible) {
        this.passPopupVisible = passPopupVisible;
        if (passPopupVisible){
            userEditorModel.getCurrentUser().generatePassword();
        }
    }
 
    private boolean showAccessibleSubjects = false;

    public boolean isShowAccessibleSubjects() {
        return showAccessibleSubjects;
    }

    public void setShowAccessibleSubjects(boolean showAccessibleSubjects) {
        this.showAccessibleSubjects = showAccessibleSubjects;
    }

}
