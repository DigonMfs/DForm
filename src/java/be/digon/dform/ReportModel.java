/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package be.digon.dform;

import java.io.Serializable;
import java.util.List;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

/**
 *
 * @author rombouts
 */
@ViewScoped
@Named
public class ReportModel implements Serializable {

    @Inject private DataConnector dataConnector ;

    public void clearSubmissionStatusCache(){
        submissionStatusCache = null;
    }
    
    private List<FormSubject> submissionStatusCache = null;
    
    public List<FormSubject> getSubmissionStatusForSubjects() {
    // Datatable loads "value" really many times (10s of times per pagination, and even when NOT !!!! rendered. Hack required : value forced to null when not displayed/rendered AND
    // also : value getter returns memory cached list (no database access everytime). This means that the in-memory cache list is to be cleared when the "report" tab is unselected by the user -->
        if (submissionStatusCache == null){
            submissionStatusCache = dataConnector.retrieveStatusOfAllSubmissionsForAllSubjects();
        }
        return submissionStatusCache;
    }
   private List<FormSubject> filteredSubmissionStatus;

    public List<FormSubject> getFilteredSubmissionStatus() {
        return filteredSubmissionStatus;
    }

    public void setFilteredSubmissionStatus(List<FormSubject> filteredSubmissionStatus) {
        this.filteredSubmissionStatus = filteredSubmissionStatus;
    }
   

}
