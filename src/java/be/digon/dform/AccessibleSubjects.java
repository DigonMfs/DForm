/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package be.digon.dform;

import java.io.Serializable;
import java.util.List;
import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.inject.Named;

/**
 *
 * @author rombouts
 */

@Dependent
public class AccessibleSubjects implements Serializable {
    
    
    @Inject
    private DataConnector dataconnector;
    private User user;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        User oldUser = this.user;
       
        this.user = user;
        if ((oldUser == null) || !oldUser.getId().equals(user.getId())){
            subjects= null;
        }
    }
    private List<FormSubject> subjects;

    public List<FormSubject> getSubjects() {
        if (subjects == null){
            subjects = dataconnector.retrieveAvailableSubjectsList(getUser());
        }
        return subjects;
    }

    public void setSubjects(List<FormSubject> subjects) {
        this.subjects = subjects;
    }
   private List<FormSubject> filteredSubjects;

    public List<FormSubject> getFilteredSubjects() {
   
        return filteredSubjects;
    }

    public void setFilteredSubjects(List<FormSubject> filteredSubjects) {
        this.filteredSubjects = filteredSubjects;
    }
    public void add(FormSubject subject){
        
    }
    
    public void remove(FormSubject subject){
        
    }
}
