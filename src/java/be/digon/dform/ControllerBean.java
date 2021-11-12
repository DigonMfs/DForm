/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package be.digon.dform;

import static be.digon.dform.ControllerBean.facesMessage;
import java.io.Serializable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.application.FacesMessage;
import javax.faces.application.FacesMessage.Severity;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

/**
 *
 * @author rombouts
 */
@ViewScoped
@Named
public class ControllerBean implements Serializable {

    @Inject
    private ModelBean modelBean;

    @Inject
    private ReportModel reportModel;

    @Inject
    private DataConnector dataConnector;

    /**
     * Creates a new instance of controllerbean
     */
    public ControllerBean() {

    }

    public ModelBean getModelBean() {
        return modelBean;
    }

    public void setModelBean(ModelBean modelBean) {
        this.modelBean = modelBean;
    }

    public ReportModel getReportModel() {
        return reportModel;
    }

    public void setReportModel(ReportModel reportModel) {
        this.reportModel = reportModel;
    }

    private String controllerMessage = "";

    public String getControllerMessage() {
        return controllerMessage;
    }

    public void setControllerMessage(String controllerMessage) {
        this.controllerMessage = controllerMessage;
    }
    private boolean formSelectVisible = false;

    public boolean isFormSelectVisible() {
        return formSelectVisible;
    }

    public void setFormSelectVisible(boolean formSelectVisible) {
        this.formSelectVisible = formSelectVisible;
    }
    private boolean formVisible = false;

    public boolean isFormVisible() {
        return formVisible;
    }

    public void setFormVisible(boolean formVisible) {
        this.formVisible = formVisible;
    }
    private boolean usersScreenVisible = false;

    public boolean isUsersScreenVisible() {
        return usersScreenVisible;
    }

    public void setUsersScreenVisible(boolean usersScreenVisible) {
        this.usersScreenVisible = usersScreenVisible;
    }

    private boolean statsScreenVisible = false;

    public boolean isStatsScreenVisible() {
        return statsScreenVisible;
    }

    public void setStatsScreenVisible(boolean statsScreenVisible) {
        this.statsScreenVisible = statsScreenVisible;
    }

    private boolean reportsScreenVisible = false;

    /**
     * Get the value of reportsScreenVisible
     *
     * @return the value of reportsScreenVisible
     */
    public boolean isReportsScreenVisible() {
        return reportsScreenVisible;
    }

    /**
     * Set the value of reportsScreenVisible
     *
     * @param reportsScreenVisible new value of reportsScreenVisible
     */
    public void setReportsScreenVisible(boolean reportsScreenVisible) {
        this.reportsScreenVisible = reportsScreenVisible;
    }

    public void formSelectAction(ActionEvent actionEvent) {
        setFormSelectVisible(true);
    }

    public void menuAction(ActionEvent actionEvent) {
        String fullId = actionEvent.getComponent().getClientId();
        String[] results = fullId.split(":");
        String id = results[results.length - 1].toUpperCase();

        //     setControllerMessage(id);
        boolean wasFormVisible = isFormVisible();
        boolean wasFormfillScreenActive = isFormfillScreenActive();
        setFormSelectVisible(false);
        setFormVisible(false);
        setFormEditVisible(false);
        setStatsScreenVisible(false);
        setFormfillScreenActive(false);
        setDataOutputScreenVisible(false);
        setUsersScreenVisible(false);
        setReportsScreenVisible(false);
        // Datatable loads "value" really many times (10s of times per pagination, and even when NOT !!!! rendered. Hack required : value forced to null when not displayed/rendered AND
        // also : value getter returns memory cached list (no database access everytime). This means that the in-memory cache list is to be cleared when the "report" tab is unselected by the user -->
        reportModel.clearSubmissionStatusCache();

        if (id.equals("FORMS_MENU")) {
            setFormfillScreenActive(true);
            if (wasFormfillScreenActive || (modelBean.getSelectedForm() == null)) {
                // Dus als "forms"-menu gekozen werd vanop het formulier-invulscherm, dan laten we een nieuwe kiezen
                if (modelBean.getSelectedSubject() != null) {
                    setFormSelectVisible(true);
                    setFormVisible(wasFormVisible); // Als form vooraf zichtbaar was : zichtbaar laten, zodat als selectbox gecancelled wordt, je terug in form zit.
                }
            } else {
                // Als vanop een ander scherm gekozen dan formulier-invulscherm EN als er al een formulier geselecteerd is, dan gewoon formulier tonen.
                setFormVisible(true);
            }
        } else if (id.equals("DATA_OUTPUT_MENU")) {
            setDataOutputScreenVisible(true);
        } else if (id.equals("EDIT_FORMS_MENU")) {
            //      setFormSelectVisible(true);
            setFormEditVisible(true);
        } else if (id.equals("REPORTS_MENU")) {
            setReportsScreenVisible(true);
        } else if (id.equals("USERS_MENU")) {
            setUsersScreenVisible(true);
        } else if (id.equals("STATS_MENU")) {
            setStatsScreenVisible(true);
        } else if (id.equals("LOGOFF")) {
            FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
            try {
                FacesContext.getCurrentInstance().getExternalContext().redirect("https://cmvreg.be");
            } catch (Exception e) {

                e.printStackTrace();
            }
        }

    }
    private boolean dataOutputScreenVisible;

    public boolean isDataOutputScreenVisible() {
        return dataOutputScreenVisible;
    }

    public void setDataOutputScreenVisible(boolean dataOutputScreenVisible) {
        this.dataOutputScreenVisible = dataOutputScreenVisible;
    }
    private boolean formfillScreenActive;

    public boolean isFormfillScreenActive() {
        return formfillScreenActive;
    }

    public void setFormfillScreenActive(boolean formfillScreenActive) {
        this.formfillScreenActive = formfillScreenActive;
    }
    private boolean formEditVisible = false;

    public boolean isFormEditVisible() {
        return formEditVisible;
    }

    public void setFormEditVisible(boolean formEditVisible) {
        this.formEditVisible = formEditVisible;
    }

    public void performOutput(ActionEvent actionEvent) {
        /*
         * System.out.println("Perform output"); Output output = new
         * Output(dataConnector); try {
         * System.out.println(output.generateOutput(null,
         * modelBean.getSelectedOutputTransformation())); } catch (Exception e)
         * { facesMessage("The form could not be submitted : " + e.getMessage(),
         * FacesMessage.SEVERITY_ERROR); e.printStackTrace(); }
         */    }

    public void genFullOutTrans(ActionEvent event) {
        try {
            OutputTransformation ot = OutputTransformation.genFullOutTrans();

            dataConnector.saveOutputTransformation(ot);
            modelBean.setAvailableOutputTransformations(null);

            //System.out.println(ot.genFullOutTrans());
        } catch (Exception e) {
            facesMessage("Error : " + e.getMessage(), FacesMessage.SEVERITY_ERROR);
            e.printStackTrace();
        }
    }

    public void instanceChanged() {
        if (modelBean.getKeyValueMap().get("instance") == null) {
            facesMessage("Fout : geen parameter met de exacte naam 'instance'", FacesMessage.SEVERITY_ERROR);
        }
        try {
            String instance = (String) modelBean.getKeyValueMap().get("instance");
            modelBean.loadLastSubmission(instance);
            // Now all values may have been cleared, but the instance value must be set correctly again :
            modelBean.getKeyValueMap().put("instance", instance);
            facesMessage("Instance " + modelBean.getKeyValueMap().get("instance") + " geladen.", FacesMessage.SEVERITY_INFO);

        } catch (Exception e) {
            facesMessage("Error : " + e.getMessage(), FacesMessage.SEVERITY_ERROR);
            e.printStackTrace();
        }
    }

    public void formSubmitFinal(ActionEvent actionEvent) {
        formSubmit(false);
    }

    public void formSubmitDraft(ActionEvent actionEvent) {
        formSubmit(true);
    }

    public void backToDraft(ActionEvent actionEvent) {
        modelBean.setFinalSubmission(false);
    }

    public void formSubmit(boolean draft) {
//        System.out.println("Form submitted");
        try {
            modelBean.getKeyValueMap().put("subject_uuid", modelBean.getSelectedSubject().getUuid());
            modelBean.getKeyValueMap().put("form_uuid", modelBean.getSelectedForm().getUuid());
            String newSubjectUUID = dataConnector.insertSubmission(modelBean.getSelectedForm(), modelBean.getKeyValueMap(), draft);
            // insertSubmission MAY create a new subject (if current subject_uuid is "New".
            // If it did, select the new subject as current subject.
            if (newSubjectUUID != null) {
                FormSubject subject = dataConnector.retrieveFormSubject(newSubjectUUID);
                modelBean.setSelectedSubject(subject);

            }
            facesMessage("The form was successfully submitted", FacesMessage.SEVERITY_INFO);
            setFormVisible(false);
            modelBean.setUserSelectedFormId(null);
            modelBean.setSelectedForm(null);
        } catch (Exception e) {
            facesMessage("The form could not be submitted :  " + e.getMessage(), FacesMessage.SEVERITY_ERROR);
            e.printStackTrace();
        }

    }

    public void clearResultFormEditor() {
        modelBean.setCheckResultSelectedForm(null);
    }

    public void formEditorSubmit(ActionEvent actionEvent) {
        // System.out.println("Form source submitted");
        try {
            modelBean.generateCheckResultSelectedForm();
            dataConnector.insertFormVersion(modelBean.getSelectedForm());
            modelBean.setAvailableForms(null);
            modelBean.setUserSelectedFormId(null);
            modelBean.setSelectedForm(null);
            // TODO : store formname of selectedform, and after rereading availableforms : restore selected form to updated version.
            facesMessage("The data was successfully stored ", FacesMessage.SEVERITY_INFO);
        } catch (Exception e) {
            facesMessage("The data could not be stored :  " + e.getMessage(), FacesMessage.SEVERITY_ERROR);
        }
    }

    public void outTransEditorSubmit(ActionEvent actionEvent) {
        // System.out.println("Form source submitted");
        try {

            dataConnector.saveOutputTransformation(modelBean.getSelectedOutputTransformation());
            modelBean.setAvailableOutputTransformations(null);
            facesMessage("The data was successfully stored ", FacesMessage.SEVERITY_INFO);
        } catch (Exception e) {
            facesMessage("The data could not be stored :  " + e.getMessage(), FacesMessage.SEVERITY_ERROR);
        }
    }

    public void outTransDelete(ActionEvent actionEvent) {
        try {

            dataConnector.deleteOutputTransformation(modelBean.getSelectedOutputTransformation());
            modelBean.setAvailableOutputTransformations(null);
            facesMessage("The data was successfully deleted ", FacesMessage.SEVERITY_INFO);
        } catch (Exception e) {
            facesMessage("The data could not be deleted :  " + e.getMessage(), FacesMessage.SEVERITY_ERROR);
        }

    }

    public void activeFormChanged() {
//        System.out.println("Active form changed");
        //       setFormSelectVisible(false);
//        if (isFormEditVisible()) {
        // Load form definition :
//        }
        try {
            if (isFormfillScreenActive()) {
                setFormVisible(true);
            }
            setFormSelectVisible(false);
            setReportsScreenVisible(false);
        } catch (Exception e) {
            facesMessage(e.getMessage(), FacesMessage.SEVERITY_ERROR);
        }
    }

    public boolean isAdmin() {
        return FacesContext.getCurrentInstance().getExternalContext().isUserInRole("Admin");
    }

    public void activeOutTransChanged() {
    }

    public static void facesMessage(String txt, Severity severity) {
        FacesContext fc = FacesContext.getCurrentInstance();
        FacesMessage fm = new FacesMessage(txt);
        fm.setSeverity(severity);
        fc.addMessage(null, fm);

    }

    public List<FormSubject> subjectAutocompleteChange(String searchTerm) {
        List<FormSubject> toReturn = null;
        try {
            toReturn = modelBean.handleSubjectSearchWord(searchTerm);
        } catch (Exception e) {
            facesMessage(e.getMessage(), FacesMessage.SEVERITY_ERROR);
        }
        return toReturn;
    }

    public void selectAnotherSubject(FormSubject s) {
        try {
            modelBean.selectNewSubject(s);
            setSelectingSubject(false);
            setFormVisible(false);
        } catch (Exception e) {
            facesMessage(e.getMessage(), FacesMessage.SEVERITY_ERROR);
        }

    }

    public void subjectListener() {
        selectAnotherSubject(modelBean.getSelectedSubjectAutoComplete());

    }
    private boolean selectingSubject;

    public boolean getSelectingSubject() {
        return selectingSubject;
    }

    public void setSelectingSubject(boolean selectingSubject) {
        this.selectingSubject = selectingSubject;
        if (selectingSubject) {
        }
    }

    public void selectSubject(ActionEvent actionEvent) {
        // TODO : First : check if anything not saved !!!

        setSelectingSubject(true);

    }

    public void newSubjectAction(ActionEvent actionEvent) {
        // Select the "registration form", and afterwards create new subject.
        // retrieve the "registration" form (which provides fields to register a new subject), and make it active.
        Form registration = dataConnector.retrieveRegistrationForm();
        if (registration == null) {
            facesMessage("Error : no registration form found. ", FacesMessage.SEVERITY_ERROR);
        } else {
            modelBean.setSelectedSubject(new FormSubject());
            modelBean.setSelectedForm(registration);
            setFormVisible(true);
            setFormSelectVisible(false);
            setReportsScreenVisible(false);
            setSelectingSubject(false);
        }
    }

    public void subjectPopupClose() {
        setSelectingSubject(false);
    }

    public void formselectPopupClose() {
        setFormSelectVisible(false);
    }

    public boolean isCreatingNewSubject() {
        return modelBean.getSelectedSubject().isNew();
    }
    private static final Logger LOG = Logger.getLogger(ControllerBean.class.getName());

    public void checkMasterPassword() {
        try {
            if (!modelBean.getMasterPassword().checkMasterPasswordCorrectlySet()) {
                facesMessage("Incorrect password", FacesMessage.SEVERITY_WARN);
            }
        } catch (Exception e) {
            facesMessage("Error : Check master password :  " + e.getMessage(), FacesMessage.SEVERITY_ERROR);
            LOG.log(Level.SEVERE, "Check master password : ", e);
        }
    }

    public void encryptWithNewMasterPassword() {
        try {
            modelBean.encryptWithNewMasterPassword();

        } catch (Exception e) {
            facesMessage("Error : encrypt all :  " + e.getMessage(), FacesMessage.SEVERITY_ERROR);
            LOG.log(Level.SEVERE, "Encrypt all : ", e);
        }
    }

    public void decryptAllData() {
        try {
            modelBean.decryptAllData();

        } catch (Exception e) {
            facesMessage("Error : Decrypt all :  " + e.getMessage(), FacesMessage.SEVERITY_ERROR);
            LOG.log(Level.SEVERE, "Decrypt all : ", e);
        }

    }
}