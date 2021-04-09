/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package be.digon.dform;

import java.util.List;
import javax.inject.Inject;

/**
 *
 * @author rombouts
 */
public class Form {
    
    private DataConnector dataConnector = new DataConnector();
    
    public Form(){
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

    private FormSubject formSubject;

    /**
     * Get the subject (e.g. the patient) to which this form corresponds
     * @return the formSubject
     */
    public FormSubject getFormSubject() {
        return formSubject;
    }

    /**
     * Set the subject (e.g. patient) to which this form corresponds.
     * @param formSubject 
     */
    public void setFormSubject(FormSubject formSubject) {
        this.formSubject = formSubject;
    }

    private List<Form> existingInstances = null;

    /**
     * Get the list of instances of this form (= with this id) that were filled out before for the current formSubject (= e.g. the current patient)
     *
     * @return the value of existingInstances
     */
    public List<Form> getExistingInstances() throws Exception {
        if (existingInstances == null){
            /**
             * @TODO : read from database if formSubject is set. Otherwise (if patient is not set), throw an exception.
             */
            throw new Exception("Not implemented yet");
        }
        return existingInstances;
    }

    /**
     * Set the list of instances of this form (= with this id) that were filled out before for the current formSubject (= e.g. the current patient)
     *
     * @param existingInstances new value of existingInstances
     */
    public void setExistingInstances(List<Form> existingInstances) {
        this.existingInstances = existingInstances;
    }
    private String description;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    
    private String formSource = null;

    public String getFormSource() {
        // Lazy loading
        if (formSource == null){
            formSource = dataConnector.retrieveFormSource(getUuid());
        }
        return formSource;
    }

    public void setFormSource(String formSource) {
        this.formSource = formSource;
    }

    
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    private int version;

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }
    
    private Boolean markAsRegistrationForm;

    public Boolean getMarkAsRegistrationForm() {
        return markAsRegistrationForm;
    }

    public void setMarkAsRegistrationForm(Boolean markAsRegistrationForm) {
        this.markAsRegistrationForm = markAsRegistrationForm;
    }


}
