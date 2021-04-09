/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package be.digon.dform;

import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author rombouts
 */
public class FormSubject {

    public FormSubject() {
        setUuid("new");
        setId(0);
        setName("New");
    }

    public FormSubject(Integer id, String uuid, String name, int version, String alfacode, String createdBy, boolean regularAccess) {
        this.id = id;
        this.uuid = uuid;
        this.name = name;
        this.version = version;
        this.alfacode = alfacode;
        this.createdBy = createdBy;
        this.regularAccess = regularAccess;
    }

    public boolean isNew() {
        return getUuid().equals("new");
    }

    private Map<String, Object> stringProperties = new TreeMap<String, Object>();

    public Map<String, Object> getProperties() {
        return stringProperties;
    }

    public void setStringProperties(Map<String, Object> stringProperties) {
        this.stringProperties = stringProperties;
    }

    public void putProperty(String name, String value) {
        getProperties().put(name, value);
    }

    public String getPropertyAsString() {
        return (String) getProperties().get(id);
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
    private String alfacode;

    public String getAlfacode() {
        return alfacode;
    }

    public void setAlfacode(String alfacode) {
        this.alfacode = alfacode;
    }

    private String createdBy;

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    private boolean regularAccess = true;

    /**
     * If true, this subject is regularly accessible by the logged in user,
     * based on the user's group. Otherwise, the user has received special
     * access rights for this patient, although the patient is not in his
     * segment etc.
     *
     * @return
     */
    public boolean isRegularAccess() {
        return regularAccess;
    }

    public void setRegularAccess(boolean regularAccess) {
        this.regularAccess = regularAccess;
    }

}
