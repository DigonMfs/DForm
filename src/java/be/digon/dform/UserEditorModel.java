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
public class UserEditorModel implements Serializable {

    @Inject private DataConnector dataConnector;

    @Inject private AccessibleSubjects accessibleSubjects ;

    public AccessibleSubjects getAccessibleSubjects() {
        return accessibleSubjects;
    }

    public void setAccessibleSubjects(AccessibleSubjects accessibleSubjects) {
        this.accessibleSubjects = accessibleSubjects;
    }

    public UserEditorModel() {
    }

    private List<User> userList = null;

    public List<User> getUserList() {
        if (userList == null) {
            userList = dataConnector.retrieveAvailableUsersList(null, 0);
        }
        return userList;
    }

    public void setUserList(List<User> userList) {
        this.userList = userList;
    }

    private List<User> filteredUserList;

    public List<User> getFilteredUserList() {
        return filteredUserList;
    }

    public void setFilteredUserList(List<User> filteredUserList) {
        this.filteredUserList = filteredUserList;
    }
    
    public void refreshUserList() {
        userList.clear();
        userList = null;
    }

    private User currentUser;

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;

        accessibleSubjects.setUser(currentUser);
    }

    public void newUser() {
        setCurrentUser(new User());
    }

    public void saveUser() throws Exception {
        dataConnector.saveUser(getCurrentUser());
    }

    public void savePassword() throws Exception {
        dataConnector.savePassword(getCurrentUser());
    }

}
