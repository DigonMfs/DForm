/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package be.digon.dform;

import static be.digon.dform.ControllerBean.facesMessage;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Comparator;
import java.util.List;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.faces.FacesException;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
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
public class ModelBean implements Serializable {

    @Inject private DataConnector dataConnector ;
    
    @Inject private MasterPassword masterPassword;

    private String newMasterPassword;

    /**
     * Creates a new instance of controllerbean
     */
    public ModelBean() {
       
    }

    private String getServletPrefix() {
        String contextUrl;

        try {
            FacesContext ctxt = FacesContext.getCurrentInstance();
            ExternalContext ext = ctxt.getExternalContext();
            URI uri = new URI(ext.getRequestScheme(),
                    null, ext.getRequestServerName(), ext.getRequestServerPort(),
                    ext.getRequestContextPath(), null, null);
            contextUrl = uri.toASCIIString();
        } catch (URISyntaxException e) {
            throw new FacesException(e);
        }
        return contextUrl;
    }

    public String getOutputServletUrl() {
        String contextUrl = getServletPrefix();
        String outTransId = "";
        if ((getSelectedOutputTransformation() != null) && getSelectedOutputTransformation().getId() != null) {
            outTransId = getSelectedOutputTransformation().getId().toString();
        }
        return contextUrl + "/OutputServlet?outtransid=" + outTransId;

    }

    public String getFormSourceUrl() {
        // The parameter of ui:include is src= an url. This URL is either a file in the war,
        // but may also be a general url. We use a servlet to generate the xhtml to be included.
        // It would be complete fun when ui:include could refer to a 'value' with #{bean.dynamiccontent},
        // but chicken-and-egg : the facelets view has to be created first before these expressions can be evaluated
        // and this expression would contain the xhtml to build a part of the facelets view.

        String contextUrl = System.getProperty("be.digon.dform.formServletPrefix", "http://localhost:8080/DForm");

        String uuid = "";
        if (getSelectedForm() != null && getSelectedForm().getUuid() != null) {
            uuid = getSelectedForm().getUuid();
        }
        return contextUrl + "/formsource?uuid=" + uuid;
    }
    private TreeMap<String, Object> keyValueMap = new TreeMap<String, Object>();

    public TreeMap<String, Object> getKeyValueMap() {
        return keyValueMap;
    }

    public void setKeyValueMap(TreeMap<String, Object> keyValueMap) {
        this.keyValueMap = keyValueMap;
    }
    private Form selectedForm = null;

    public Form getSelectedForm() {
        if ((selectedForm == null) || (userSelectedFormId != null && (!selectedForm.getId().equals(getUserSelectedFormId())))) {
            // Then we need to read selectedFormId, and look up selectedform.
            // Note that it would be easier if we could specify form objects in the "value" property of
            // the selectitems for jsf, and set selectedform directly, but we can not use objects as 
            // values in the selectitems array,
            // it should be primitives (or if you want to use objects, converters have to be registered)
            // (google says jsf specifies this).
            if ((getUserSelectedFormId() != null) && getUserSelectedFormId().intValue() != 0) {
                for (Form f : availableForms) {
                    if (f.getId().equals(getUserSelectedFormId())) {
                        selectedForm = f;

                        // Find an instance that was already submitted :

                        //loadLastSubmission(getSelectedInstance());
                        loadLastSubmittedInstance();
                        break;
                    }
                }
            }
            // Here we have a selectedForm-object, so we can set userSelectedFormId to null, and then when the selection dialog is displayed later on,
            // there will be no selection, and if the user selects the same thing as is selected now, a valuechangeevent will occur. Otherwise, the user can not
            // select the same form, and the view logic depends on the changelistener in the form select popupt.
         setUserSelectedFormId(null);     
        }
        return selectedForm;
    }

    public String retrieveLastSubmittedInstance(Form form, FormSubject subject) {
        String instanceId = "";
        if ((form != null) && (subject != null)) {
            List<XmlSubmissionData> lsd = dataConnector.retrieveSubmittedInstancesMetadata(form, subject);
            Integer largest = new Integer(0);
            for (XmlSubmissionData sd : lsd) {
                if (sd.getId().compareTo(largest) > 0) {
                    largest = sd.getId();
                    instanceId = sd.getInstance();
                }
            }
        }
        return instanceId;
    }

    public void loadLastSubmittedInstance() {
        try {
            String instanceName = retrieveLastSubmittedInstance(getSelectedForm(), getSelectedSubject());
            loadLastSubmission(instanceName);
        } catch (Exception e) {
            facesMessage("The form data could not be loaded :  " + e.getMessage(), FacesMessage.SEVERITY_ERROR);
            e.printStackTrace();
        }

    }

    public void loadLastSubmission(String instance) {
        try {
            if ((getSelectedForm() == null) || (getSelectedSubject() == null)) {
                getKeyValueMap().clear();
                return;
            }
            if ((instance == null) || instance.isEmpty()) {
                instance = "__singleinstance__";
            }
            setFinalSubmission(false);
            XmlSubmissionData xmlSubmissionData = dataConnector.retrieveLastSubmission(getSelectedForm(), getSelectedSubject(), instance);
            if (xmlSubmissionData != null) {
                TreeMap<String, Object> keyValueMap = xmlSubmissionData.getKeyValueMap();
                if (keyValueMap != null) {
                    setKeyValueMap(keyValueMap);
                    setFinalSubmission(xmlSubmissionData.isFinalSubmission());

                } else {
                    getKeyValueMap().clear();
                }
            } else {
                getKeyValueMap().clear();
            }
        } catch (Exception e) {
            facesMessage("The form data could not be loaded :  " + e.getMessage(), FacesMessage.SEVERITY_ERROR);
            e.printStackTrace();
        }
    }

    public void setSelectedForm(Form form) {
        // if selected by this method, then setUserSelectedFormId is set to null, because else it has priority
        userSelectedFormId = null;
        this.selectedForm = form;
    }
    private OutputTransformation selectedOutputTransformation = null;

    public OutputTransformation getSelectedOutputTransformation() {
        if ((selectedOutputTransformation == null) || (getUserSelectedOutTransId() != null && (!selectedOutputTransformation.getId().equals(getUserSelectedOutTransId())))) {
            if ((getUserSelectedOutTransId() != null) && getUserSelectedOutTransId().intValue() != 0) {
                for (OutputTransformation ot : getAvailableOutputTransformations()) {
                    if (ot.getId().equals(getUserSelectedOutTransId())) {
                        selectedOutputTransformation = ot;
                    }
                }
            }
        }
        if (selectedOutputTransformation == null) {
            selectedOutputTransformation = new OutputTransformation();
        }
        return selectedOutputTransformation;
    }
    private Integer userSelectedFormId = null;

    public Integer getUserSelectedFormId() {
        return userSelectedFormId;
    }

    public void setUserSelectedFormId(Integer userSelectedFormId) {
        // WE NEED a primitive type (or else one needs to register a converter) as the "value" field in a selectItem.
        this.userSelectedFormId = userSelectedFormId;
    }
    private Integer userSelectedOutTransId = new Integer(0);

    public Integer getUserSelectedOutTransId() {
        return userSelectedOutTransId;
    }

    public void setUserSelectedOutTransId(Integer userSelectedOutTransId) {
        this.userSelectedOutTransId = userSelectedOutTransId;
    }
    private FormSubject selectedSubject;

    public FormSubject getSelectedSubject() {
        /*
         * if ((selectedSubject == null) || (!
         * selectedSubject.getId().equals(getSelectedSubjectId()))) { // Then we
         * need to read selectedFormId, and look up selectedform. // Note that
         * it would be easier if we could specify form objects in the "value"
         * property of // the selectitems for jsf, and set selectedform
         * directly, but we can not use objects as // values in the selectitems
         * array, // it should be primitives (or if you want to use objects,
         * converters have to be registered) // (google says jsf specifies
         * this). if ((getSelectedSubjectId()!=null) &&
         * (getSelectedSubjectId().intValue() != 0) && (availableForms!=null)) {
         * for (FormSubject f : matchingSubjectsList){ if
         * (f.getId().equals(getSelectedSubjectId())){ selectedSubject = f; } }
         * } }
         */
        return selectedSubject;
    }

    public void setSelectedSubject(FormSubject subject) {
        this.selectedSubject = subject;
    }
    private List<Form> availableForms = null;

    public List<Form> getAvailableForms() {
        if (availableForms == null) {
            availableForms = dataConnector.retrieveAvailableFormsList();
        }
        return availableForms;
    }

    public void setAvailableForms(List<Form> availableForms) {
        this.availableForms = availableForms;

    }
    private List<OutputTransformation> availableOutputTransformations;

    public List<OutputTransformation> getAvailableOutputTransformations() {
        if (availableOutputTransformations == null) {
            availableOutputTransformations = dataConnector.retrieveOutputTransformations(null);
        }
        return availableOutputTransformations;
    }

    public void setAvailableOutputTransformations(List<OutputTransformation> availableOutputTransformations) {
        this.availableOutputTransformations = availableOutputTransformations;
        selectedOutputTransformation = null;
    }

    public SelectItem[] getAvailableOutTransSelectItems() {
        List<OutputTransformation> slist=getAvailableOutputTransformations().stream().sorted(Comparator.comparing(OutputTransformation::getTitle).reversed()).collect(Collectors.toList());
        SelectItem[] available = new SelectItem[slist.size()];
        for (int i = 0; i < available.length; i++) {
            OutputTransformation ot = slist.get(i);
            SelectItem item = new SelectItem();
            available[i] = item;
            available[i].setLabel(ot.getTitle());
            available[i].setValue(ot.getId());
        }
        return available;
    }

    public TimeZone getTimeZone() {
        return java.util.TimeZone.getDefault();
    }

    public SelectItem[] getAvailableFormsSelectItems() {
        SelectItem[] available = new SelectItem[getAvailableForms().size()];
        for (int i = 0; i < available.length; i++) {
            Form f = getAvailableForms().get(i);
            SelectItem item = new SelectItem();
            available[i] = item;
            available[i].setLabel(f.getTitle());
            available[i].setValue(f.getId());
        }
        return available;
    }
    private List<FormSubject> matchingSubjectsList;

    public List<FormSubject> getMatchingSubjectsList() {
        if (matchingSubjectsList == null) {

            matchingSubjectsList = dataConnector.retrieveAvailableSubjectsList(subjectSearchWord, 10);
        }
        return matchingSubjectsList;
    }

    public List getSubjectAutoCompleteList() {
        List<FormSubject> asl = getMatchingSubjectsList();
        return asl;
    }
    
    
    private FormSubject selectedSubjectAutoComplete;

    public FormSubject getSelectedSubjectAutoComplete() {
        return selectedSubjectAutoComplete;
    }

    public void setSelectedSubjectAutoComplete(FormSubject selectedSubjectAutoComplete) {
        this.selectedSubjectAutoComplete = selectedSubjectAutoComplete;
    }
    
    
    private String subjectSearchWord;

    public String getSubjectSearchWord() {
        return subjectSearchWord;
    }

    public void setSubjectSearchWord(String subjectSearchWord) {
        this.subjectSearchWord = subjectSearchWord;
        matchingSubjectsList = null;
    }
    
    public List<FormSubject> handleSubjectSearchWord(String subjectSearchWord){
        setSubjectSearchWord(subjectSearchWord);
        return getSubjectAutoCompleteList();
        
    }
    public String getSelectedInstance() {
        return (String) keyValueMap.get("instance");
    }
    private boolean finalSubmission = false;

    public boolean isFinalSubmission() {
        return finalSubmission;
    }

    public void setFinalSubmission(boolean finalSubmission) {
        this.finalSubmission = finalSubmission;
    }
    
    public void selectNewSubject(FormSubject s) {
        setSelectedSubject(s);
        setUserSelectedFormId(null);
        setSelectedForm(null);

    }

    String checkResultSelectedForm;

    public String getCheckResultSelectedForm() {
        return checkResultSelectedForm;
    }

    public void setCheckResultSelectedForm(String checkResultSelectedForm) {
        this.checkResultSelectedForm = checkResultSelectedForm;
    }
    
    void generateCheckResultSelectedForm() {
        StringBuilder cr = new StringBuilder();
        if (getSelectedForm() != null){
            // List all "for"-attributes, and make sure they can be found as "id". Primefaces fails to render a page if they don't exist as ids.
            Pattern pattern = Pattern.compile(" for=\"(\\w+)\"[ >]");
            Matcher matcher = pattern.matcher(getSelectedForm().getFormSource());
            while (matcher.find()){
                // try to find id somewhere :
                Pattern idPattern = Pattern.compile(" id=\""+matcher.group(1)+"\"[ >]");
                Matcher idMatcher = idPattern.matcher(getSelectedForm().getFormSource());
                if (!idMatcher.find()){
                    cr.append(matcher.group(1));
                    cr.append(", ");
                }
            }
            
        }
        
        checkResultSelectedForm = cr.toString();
       
    }

    public MasterPassword getMasterPassword() {
        return masterPassword;
    }

    public void setMasterPassword(MasterPassword masterPassword) {
        this.masterPassword = masterPassword;
    }

    void encryptWithNewMasterPassword() throws Exception{
        dataConnector.encryptAllSubmissionsAndSetMasterPassword(newMasterPassword);

    }

    public String getNewMasterPassword() {
        return newMasterPassword;
    }

    public void setNewMasterPassword(String newMasterPassword) {
        this.newMasterPassword = newMasterPassword;
    }

    void decryptAllData()throws Exception{
         dataConnector.decryptAllSubmissionsAndRemoveMasterPassword();
    }

}
