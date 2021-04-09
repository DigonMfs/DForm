/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package be.digon.dform;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import javax.inject.Named;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author rombouts
 *
 * Fill submissions of registration forms with patients from database (init) :
 * insert into submissions
 * (formdata,uuid,instance,version,created,status,subject_id,formsource_rowid,formname,
 * formversion) select concat('<?xml version="1.0" encoding="UTF-8"
 * standalone="no"?><DigonFormData Version="1" formname="Registration"
 * formversion="14" instance="__singleinstance__"><KeyValue Key="name" Type="S"
 * Value="',name,'"/><KeyValue Key="patient_name" Type="S"
 * Value="',name,'"/><KeyValue Key="subject_uuid" Type="S"
 * Value="new"/><KeyValue Key="patient_date_of_birth" Type="D"
 * Value="',street,'"/></DigonFormData>'),uuid(),'__singleinstance__',1,current_timestamp,'edit',row_id,33,'Registration',14
 * from patients;
 *
 */
@Named
@SessionScoped
public class DataConnector implements Serializable {

    @Inject
    private UserEditorController uec;

    @Inject
    private MasterPassword masterPassword;

    public void insertFormVersion(Form form) throws Exception {
        Connection con = null;
        ResultSet rs = null;
        PreparedStatement ps = null;
        try {

            con = getDFormPool().getConnection();
            con.setAutoCommit(false);
            ps = con.prepareStatement("update forms set active=0 where formname=?");
            ps.setString(1, form.getName());
            ps.executeUpdate();

            int isRegistrationForm = 0;
            if (form.getMarkAsRegistrationForm().booleanValue()) {
                ps = con.prepareStatement("update forms set is_registrationform=0");
                ps.executeUpdate();
                isRegistrationForm = 1;
            }

            ps = con.prepareStatement("insert into forms (uuid, created, title, description, active, formsource, formname, formversion, is_registrationform)"
                    + " select ?, current_timestamp, ?, ?, 1, ?, formname, max(formversion)+1, ? from forms where formname=?");

            UUID uuid = UUID.randomUUID();
            int i = 1;
            ps.setString(i++, uuid.toString());
            ps.setString(i++, form.getTitle());
            ps.setString(i++, form.getDescription());
            ps.setString(i++, form.getFormSource());
            ps.setInt(i++, isRegistrationForm);
            ps.setString(i++, form.getName());
            ps.executeUpdate();
            con.commit();

        } catch (Exception e) {
            try {
                con.rollback();
            } catch (Exception ex) {
            }
            e.printStackTrace();
            throw e;
        } finally {
            con.setAutoCommit(true);
            if (rs != null) {
                try {
                    rs.close();
                } catch (Exception e) {
                }
            }
            if (ps != null) {
                try {
                    ps.close();
                } catch (Exception e) {
                }
            }
            if (con != null) {
                try {
                    con.close();
                } catch (Exception e) {
                }
            }
        }
    }

    public String retrieveFormSource(String uuid) {
        String source = null;
        Connection con = null;
        ResultSet rs = null;
        PreparedStatement ps = null;
        try {

            con = getDFormPool().getConnection();
            ps = con.prepareStatement("select formsource from forms where uuid = ?");
            ps.setString(1, uuid);
            rs = ps.executeQuery();
            if (rs.first()) {
                source = rs.getString("formsource");
            }

        } catch (Exception e) {
            e.printStackTrace();
            source = null;
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (Exception e) {
                }
            }
            if (ps != null) {
                try {
                    ps.close();
                } catch (Exception e) {
                }
            }
            if (con != null) {
                try {
                    con.close();
                } catch (Exception e) {
                }
            }
        }
        return source;
    }

    public Form retrieveRegistrationForm() {
        Connection con = null;
        ResultSet rs = null;
        PreparedStatement ps = null;
        Form form = null;
        try {

            con = getDFormPool().getConnection();
            ps = con.prepareStatement("select row_id, title, description, uuid, formversion, formname, is_registrationform from forms where active=1 and is_registrationform=1 limit 1");
            rs = ps.executeQuery();
            while (rs.next()) {
                form = new Form();
                form.setId(rs.getInt("row_id"));
                form.setTitle(rs.getString("title"));
                form.setDescription(rs.getString("description"));
                form.setUuid(rs.getString("uuid"));
                form.setName(rs.getString("formname"));
                form.setVersion(rs.getInt("formversion"));
                form.setMarkAsRegistrationForm(rs.getInt("is_registrationform") != 0 ? Boolean.TRUE : Boolean.FALSE);
            }

        } catch (Exception e) {
            e.printStackTrace();
            form = null;
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (Exception e) {
                }
            }
            if (ps != null) {
                try {
                    ps.close();
                } catch (Exception e) {
                }
            }
            if (con != null) {
                try {
                    con.close();
                } catch (Exception e) {
                }
            }
        }
        return form;
    }

    public List<Form> retrieveAvailableFormsList() {

        ArrayList<Form> availableForms = null;
        Connection con = null;
        ResultSet rs = null;
        PreparedStatement ps = null;
        try {

            con = getDFormPool().getConnection();
            ps = con.prepareStatement("select row_id, title, description, uuid, formversion, formname, is_registrationform from forms where active=1 group by formname");
            rs = ps.executeQuery();
            availableForms = new ArrayList<Form>();
            while (rs.next()) {
                Form form = new Form();
                form.setId(rs.getInt("row_id"));
                form.setTitle(rs.getString("title"));
                form.setDescription(rs.getString("description"));
                form.setUuid(rs.getString("uuid"));
                form.setName(rs.getString("formname"));
                form.setVersion(rs.getInt("formversion"));
                form.setMarkAsRegistrationForm(rs.getInt("is_registrationform") != 0 ? Boolean.TRUE : Boolean.FALSE);
                availableForms.add(form);
            }

        } catch (Exception e) {
            e.printStackTrace();
            availableForms = null;
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (Exception e) {
                }
            }
            if (ps != null) {
                try {
                    ps.close();
                } catch (Exception e) {
                }
            }
            if (con != null) {
                try {
                    con.close();
                } catch (Exception e) {
                }
            }
        }
        return availableForms;
    }

    /**
     * Retrieves a list of all outputtransformations (if id == null) or a list
     * with ONE outputTransformation with identifier id if id != null
     */
    public List<OutputTransformation> retrieveOutputTransformations(Integer id) {

        ArrayList<OutputTransformation> AvailableOutputTransformations = null;
        Connection con = null;
        ResultSet rs = null;
        PreparedStatement ps = null;
        try {

            con = getDFormPool().getConnection();
            String sql = "select row_id, title, uuid, xslt, outputtype from outputtransformations";
            if (id != null) {
                sql += " where row_id=" + id.toString();
            }
            ps = con.prepareStatement(sql);
            rs = ps.executeQuery();
            AvailableOutputTransformations = new ArrayList<OutputTransformation>();
            while (rs.next()) {
                OutputTransformation outputTransformation = new OutputTransformation();
                outputTransformation.setId(rs.getInt("row_id"));
                outputTransformation.setTitle(rs.getString("title"));
                outputTransformation.setUuid(rs.getString("uuid"));
                outputTransformation.setOutputType(rs.getString("outputtype"));
                outputTransformation.setXslt(rs.getString("xslt"));

//                System.out.println("Overriding database xslt with identity xslt !!!!!!");
//                outputTransformation.setXslt("<?xml version=\"1.0\" encoding=\"UTF-8\"?><xsl:stylesheet xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\" version=\"1.0\">"
//                        + "<xsl:output method=\"xml\"/><xsl:template match=\"@*|node()\"><xsl:copy><xsl:apply-templates select=\"@*|node()\"/></xsl:copy></xsl:template></xsl:stylesheet>");
                AvailableOutputTransformations.add(outputTransformation);
            }

        } catch (Exception e) {
            e.printStackTrace();
            AvailableOutputTransformations = null;
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (Exception e) {
                }
            }
            if (ps != null) {
                try {
                    ps.close();
                } catch (Exception e) {
                }
            }
            if (con != null) {
                try {
                    con.close();
                } catch (Exception e) {
                }
            }
        }
        return AvailableOutputTransformations;
    }

    public FormSubject retrieveFormSubject(String uuid) {
        Connection con = null;
        ResultSet rs = null;
        PreparedStatement ps = null;
        FormSubject subject = null;
        try {
            con = getDFormPool().getConnection();
            ps = con.prepareStatement("select patients.row_id, patients.uuid, patients.name, "
                    + " patients.street, patients.zip, patients.city, patients.version,"
                    + " patients.alfacode, users.name as createdby_name "
                    + " from patients "
                    + " left join users on users.uuid = patients.created_by "
                    + "where patients.uuid=?");
            ps.setString(1, uuid);
            rs = ps.executeQuery();
            while (rs.next()) {
                subject = new FormSubject(rs.getInt("row_id"),
                        rs.getString("uuid"),
                        rs.getString("name"),
                        rs.getInt("version"),
                        rs.getString("alfacode"),
                        rs.getString("createdby_name"),
                        true);
                subject.putProperty("zip", rs.getString("zip"));
                subject.putProperty("street", rs.getString("street"));
                subject.putProperty("city", rs.getString("city"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            subject = null;
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (Exception e) {
                }
            }
            if (ps != null) {
                try {
                    ps.close();
                } catch (Exception e) {
                }
            }
            if (con != null) {
                try {
                    con.close();
                } catch (Exception e) {
                }
            }
        }
        return subject;

    }

    /**
     * Retrieves all patients for who alfacode or name contains searchterm. Set
     * search term to empty string to get all results. Set maxResults to 0 to
     * get all results
     *
     * @param searchTerm
     * @param maxResults
     * @return
     */
    public List<FormSubject> retrieveAvailableSubjectsList(String searchTerm, int maxResults) {

        ArrayList<FormSubject> availableSubjects = null;
        Connection con = null;
        ResultSet rs = null;
        PreparedStatement ps = null;
        try {
            if (searchTerm != null) {
                con = getDFormPool().getConnection();

                boolean onlyMySegment = true; // First : strict
                boolean onlyCreatedByMe = true; // First : strict
                ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();

                if (ec.isUserInRole("UseAllSubjects")) {
                    onlyMySegment = false; // relax
                    onlyCreatedByMe = false; // relax
                } else if (ec.isUserInRole("UseSegmentSubjects")) {
                    onlyMySegment = true;
                    onlyCreatedByMe = false;
                } else if (ec.isUserInRole("UseMySubjects")) {
                    onlyMySegment = false;
                    onlyCreatedByMe = true;
                }

                String principal = "";
                String sqlFields = "select 1 as regularaccess, patients.row_id, patients.uuid, patients.name, "
                        + "patients.street, patients.zip, patients.city, patients.version, "
                        + "patients.alfacode, users.name as createdby_name from patients ";
                String sql = sqlFields;
                if (onlyCreatedByMe) {
                    sql += " inner join users on users.uuid = patients.created_by and users.login=? ";
                } else {
                    sql += " left join users on users.uuid = patients.created_by ";
                }
                if (onlyMySegment) {
                    sql += " inner join users segments on segments.segment=patients.segment and segments.login=? ";
                }
                sql += " where ((patients.name like ?) or (patients.alfacode = ?))";

                if (maxResults > 0) {
                    sql += " limit ?";
                }
                ps = con.prepareStatement(sql);
                int i = 0;
                if (onlyCreatedByMe) {
                    ps.setString(++i, ec.getUserPrincipal().getName());
                }
                if (onlyMySegment) {
                    ps.setString(++i, ec.getUserPrincipal().getName());
                }
                ps.setString(++i, "%" + searchTerm.trim() + "%");
                ps.setString(++i, searchTerm.trim());
                if (maxResults > 0) {
                    ps.setInt(++i, maxResults);
                }
                rs = ps.executeQuery();
                availableSubjects = new ArrayList<FormSubject>();
                while (rs.next()) {
                    FormSubject subject = new FormSubject(rs.getInt("row_id"),
                            rs.getString("uuid"),
                            rs.getString("name"),
                            rs.getInt("version"),
                            rs.getString("alfacode"),
                            rs.getString("createdby_name"),
                            true);

                    subject.putProperty("zip", rs.getString("zip"));
                    subject.putProperty("street", rs.getString("street"));
                    subject.putProperty("city", rs.getString("city"));
                    availableSubjects.add(subject);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            availableSubjects = null;
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (Exception e) {
                }
            }
            if (ps != null) {
                try {
                    ps.close();
                } catch (Exception e) {
                }
            }
            if (con != null) {
                try {
                    con.close();
                } catch (Exception e) {
                }
            }
        }
        return availableSubjects;
    }

    /**
     * Retrieves all subjects accessible to a specified user
     *
     * @return
     */
    public List<FormSubject> retrieveAvailableSubjectsList(User user) {
        ArrayList<FormSubject> availableSubjects = null;
        Connection con = null;
        ResultSet rs = null;
        PreparedStatement ps = null;
        try {
            con = getDFormPool().getConnection();

            boolean onlyMySegment = true; // First : strict
            boolean onlyCreatedByMe = true; // First : strict
            ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();

            // MAKE SURE THESE MATCH WITH THE ROLE MAPPINGS IN GLASSFISH-WEB.XML, or you get wrong results
            boolean userInRoleUseAllSubjects = false;
            boolean userInRoleUseSegmentSubjects = false;
            boolean userInRoleUseMySubjects = false;

            if (user.getGroups() != null) {
                userInRoleUseAllSubjects = Arrays.stream(user.getGroups()).anyMatch("AllPatientsIndividual"::equals)
                        || Arrays.stream(user.getGroups()).anyMatch("Admin"::equals);
                userInRoleUseSegmentSubjects = Arrays.stream(user.getGroups()).anyMatch("Center"::equals)
                        || Arrays.stream(user.getGroups()).anyMatch("Admin"::equals);
                userInRoleUseMySubjects = Arrays.stream(user.getGroups()).anyMatch("Pediatrician"::equals)
                        || Arrays.stream(user.getGroups()).anyMatch("Center"::equals)
                        || Arrays.stream(user.getGroups()).anyMatch("Admin"::equals);
            }

            if (userInRoleUseAllSubjects) {
                onlyMySegment = false; // relax initially strict condition
                onlyCreatedByMe = false; // relax
            } else if (userInRoleUseSegmentSubjects) {
                onlyMySegment = true;
                onlyCreatedByMe = false;
            } else if (userInRoleUseMySubjects) {
                onlyMySegment = false;
                onlyCreatedByMe = true;
            }
            String sqlFields = "select 1 as regularaccess, patients.row_id, patients.uuid, patients.name, patients.street, patients.zip, patients.city, patients.version, patients.alfacode, users.name as createdby from patients ";
            String sql = sqlFields;
            if (onlyCreatedByMe) {
                sql += " inner join users on users.uuid = patients.created_by and users.login=? ";
            } else {
                sql += " left join users on users.uuid = patients.created_by ";
            }
            if (onlyMySegment) {
                sql += " inner join users segments on segments.segment=patients.segment and segments.login=? ";
            }

            ps = con.prepareStatement(sql);
            int i = 0;
            if (onlyCreatedByMe) {
                ps.setString(++i, user.getLogin());
            }
            if (onlyMySegment) {
                ps.setString(++i, user.getLogin());
            }
            rs = ps.executeQuery();
            availableSubjects = new ArrayList<FormSubject>();
            while (rs.next()) {

                FormSubject subject = new FormSubject(rs.getInt("row_id"),
                        rs.getString("uuid"),
                        rs.getString("name"),
                        rs.getInt("version"),
                        rs.getString("alfacode"),
                        rs.getString("createdby"),
                        true);
                subject.putProperty("zip", rs.getString("zip"));
                subject.putProperty("street", rs.getString("street"));
                subject.putProperty("city", rs.getString("city"));
                availableSubjects.add(subject);
            }
        } catch (Exception e) {
            e.printStackTrace();
            availableSubjects = null;
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (Exception e) {
                }
            }
            if (ps != null) {
                try {
                    ps.close();
                } catch (Exception e) {
                }
            }
            if (con != null) {
                try {
                    con.close();
                } catch (Exception e) {
                }
            }
        }
        return availableSubjects;
    }

    /**
     * Inserts all parameters from params in the database. Some predefined
     * fields go into database fields (but also in the key-value-xml-document)
     * All other fields only go into the xml document
     *
     * We work with UUID's for all references to other tables, since then a
     * hacker will have to be very lucky to guess an existing uuid. Guessing a
     * sequential number is much easier.
     *
     * For performance, uuids are stored, but also complementary integer ids
     * (auto-increment columns in the referenced table) on insertion in the
     * submissions table.
     *
     * Fields that go into the database : <ul><li> instance (if not present,
     * '__singleinstance__ is inserted) <li> subject_uuid (required) <li>
     * form_uuid (required, also determines formversion and formname) <li>
     * subject_type (not required, database provides a default value)</ul>
     *
     * If subject_uuid = "new", a NEW subject is created in the subject table.
     * In that case, also the following fields are required (and written to
     * database columns in the subject table) <ul> <li> name </ul> The
     * subject_type then may be used to determine the subject table (lookup
     * table).
     *
     * For now : hardcoded : the subject table = 'patients'. Later on, this may
     * be made configurable, but always with a LUT (never let the user determine
     * the name of the table freely).
     *
     * @param params @returns new subject uuid, if a subject was created .
     */
    public String insertSubmission(Form form, Map params, boolean draft) throws Exception {
        params.put("form_uuid", form.getUuid());

        Document document = null;
        String newSubjectUUID = null;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            DOMImplementation impl = builder.getDOMImplementation();
            document = impl.createDocument(null, null, null);
            // work with the document...

        } catch (FactoryConfigurationError e) {
            System.out.println("Could not locate a JAXP DocumentBuilderFactory class");
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            System.out.println("Could not locate a JAXP DocumentBuilder class");
            e.printStackTrace();
        }

        Element el = document.createElement("DigonFormData");
        el.setAttribute("Version", "1");
        document.appendChild(el);

        String subjectUuid = "";
        String subjectType = "";
        String instance = "";
        String formUuid = "";
        String name = "";

        String formDataXml;

        Iterator i = params.keySet().iterator();

        SimpleDateFormat tf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        df.setTimeZone(TimeZone.getTimeZone("GMT"));

        while (i.hasNext()) {
            String key = (String) i.next();
//            System.out.print("Key : " + key + " Value : ");
            Object objVal = params.get(key);
//            System.out.println(objVal);
            String value = "";
            String tpe = "";
            if (objVal != null) {
                if (objVal instanceof BigDecimal) {
                    value = ((BigDecimal) objVal).toPlainString();
                    tpe = OutputServlet.TYPE_NUMERIC;
                } else if (objVal instanceof Date) {
                    value = df.format((Date) objVal);
                    tpe = OutputServlet.TYPE_DATE;
//                } else if (objVal instanceof Date) {
//                    value = tf.format((Date) objVal);
                } else if (objVal instanceof Boolean) {
                    boolean b = ((Boolean) objVal).booleanValue();
                    value = b ? "true" : "false";
                    tpe = OutputServlet.TYPE_BOOLEAN;
                } else {
                    value = (String) objVal;
                    tpe = OutputServlet.TYPE_STRING;
                }
                //              System.out.println("Value : " + value);

                if (key.equalsIgnoreCase("subject_uuid")) {
                    subjectUuid = value;
                } else if (key.equalsIgnoreCase("form_uuid")) {
                    formUuid = value;
                } else if (key.equalsIgnoreCase("instance")) {
                    instance = value;
                } else if (key.equalsIgnoreCase("subject_type")) {
                    subjectType = value;
                } else if (key.equalsIgnoreCase("name")) {
                    name = value;
                }

                Element data = document.createElement("KeyValue");
                data.setAttribute("Key", key);
                data.setAttribute("Value", value);
                data.setAttribute("Type", tpe);
                el.appendChild(data);
            } else {
                //               System.out.println("NO VALUE");
            }
        }

        if ((instance == null) || (instance.equals(""))) {
            instance = "__singleinstance__";
        }

        boolean createNewSubject = subjectUuid.equalsIgnoreCase("new");
        // Check required fields (fields that go into separate database columns :
        String requiredFieldError = "";
        if (formUuid.equals("")) {
            requiredFieldError += " form_uuid";
        }
        if (subjectUuid.equals("")) {
            requiredFieldError += " subject_uuid";
        }
        if (createNewSubject && name.equals("")) {
            requiredFieldError += " name";
        }

        if (!requiredFieldError.equals("")) {
            throw new Exception("Required field not specified, please contact the administrator who set up the form definitions. Field(s) : " + requiredFieldError);
        }

        el.setAttribute("instance", instance);
        el.setAttribute("formname", form.getName());
        el.setAttribute("formversion", Integer.toString(form.getVersion()));

        // XML document to text :
        formDataXml = XmlUtils.getStringFromDocument(document);

        Connection con = null;
        PreparedStatement ps = null;
        try {
            con = getDFormPool().getConnection();

            if (createNewSubject) {

                UUID subjectUuidentifier = UUID.randomUUID();
                subjectUuid = subjectUuidentifier.toString();
                newSubjectUUID = subjectUuid;

                String principal = FacesContext.getCurrentInstance().getExternalContext().getUserPrincipal().getName();

                ps = con.prepareStatement("insert into patients (uuid, name,created, created_by, segment) select ?, ?, current_timestamp, users.uuid, users.segment from users where users.login=?");
                int j = 1;
                ps.setString(j++, subjectUuid); // Database will generate a (faster for indexing) auto-inc. row_id, but we know the uuid here !
                ps.setString(j++, name);
                ps.setString(j++, principal);
                ps.executeUpdate();

                // Now generate an alfa code for the new subject (this is specific for the CMV-database :
                ps.close();

                ps = con.prepareStatement("select max(substring(alfacode,2)) as max_sub_alfacode, segment from patients where segment = (select segment from patients where uuid=?) ");
                ps.setString(1, subjectUuid);
                ResultSet rs = ps.executeQuery();
                String newAlfacode = "";
                if (rs.first()) {
                    int m = rs.getInt(1);
                    String segment = rs.getString(2);
                    if (m < 1000) {
                        newAlfacode = segment + String.format("%03d", m + 1);
                    } else {
                        newAlfacode = segment + Integer.toString(m + 1);
                    }
                }
                rs.close();
                ps.close();

                ps = con.prepareStatement("update patients set alfacode=? where uuid=?");
                ps.setString(1, newAlfacode);
                ps.setString(2, subjectUuid);
                ps.execute();
                ps.close();

            }

            UUID uuid = UUID.randomUUID();

            // the forms table contains the form layouts; 
            // A form may be updated after a while, but then it keeps the same formname. Its version number formversion is incremented.
            // Also the row_id and the uuid change.
            // The submissions table keeps a reference to the form series identifier (formname refers to forms.formname), but also of course to the 
            // specific form (form_id refers to forms.row_id)
            StringBuilder sb = new StringBuilder();
            sb.append("insert into submissions (uuid, instance, formsource_rowid, formname, formversion,");
            if (masterPassword.getUseMasterPassword()) {
                if (!masterPassword.checkMasterPasswordCorrectlySet()) {
                    // And fail / throws if not. Make sure data is not stored encrypted with an unknown/wrong password. Check this HERE, each time again, on writing.
                    throw new Exception("Master password is not correctly set. Cannot write data.");
                }
                sb.append("formdata_aes,");

            } else {
                sb.append("formdata,");
            }
            sb.append(" subject_id, status, created) "
                    + " select ?, ?, forms.row_id, forms.formname, forms.formversion, ?, patients.row_id, ?, current_timestamp from forms left join patients "
                    + " on patients.uuid=? "
                    + " where forms.uuid=? ");

            ps = con.prepareStatement(sb.toString());
            int j = 1;
            ps.setString(j++, uuid.toString());
            ps.setString(j++, instance);
            if (masterPassword.getUseMasterPassword()) {
                ps.setBytes(j++, masterPassword.aesEncrypt(formDataXml));
            } else {
                ps.setString(j++, formDataXml);

            }
            ps.setString(j++, draft ? "edit" : "complete");
            ps.setString(j++, subjectUuid);
            ps.setString(j++, formUuid);
            ps.executeUpdate();

        } finally {
            try {
                ps.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            try {
                con.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        return newSubjectUUID;
    }

    private DataSource getDFormPool() throws NamingException {
        Context c = new InitialContext();
        return (DataSource) c.lookup("java:comp/env/DFormPool");
    }

    List<XmlSubmissionData> retrieveSubmittedInstancesMetadata(Form form, FormSubject subject) {
        List<XmlSubmissionData> sdl = new ArrayList<XmlSubmissionData>();
        XmlSubmissionData submission = null;
        Connection con = null;
        ResultSet rs = null;
        PreparedStatement ps = null;
        try {
            con = getDFormPool().getConnection();
            ps = con.prepareStatement("select submissions.row_id, submissions.formname, submissions.subject_id, submissions.instance,  submissions.status, p.alfacode  from submissions  "
                    + " left join patients p on submissions.subject_id=p.row_id "
                    + " where submissions.row_id in (select max(row_id) as mx_row from submissions "
                    + "  where submissions.formname=? and submissions.subject_id=?  "
                    + "  group by submissions.formname, submissions.subject_id, submissions.instance"
                    + " )"
                    + " order by submissions.row_id ");
            int i = 0;
            ps.setString(++i, form.getName());
            ps.setInt(++i, subject.getId().intValue());
            rs = ps.executeQuery();
            submission = new XmlSubmissionData();
            while (rs.next()) {
                submission.setId(rs.getInt("row_id"));
                submission.setSubjectId(rs.getInt("subject_id"));
                submission.setSubjectAlfacode(rs.getString("alfacode"));
                submission.setInstance(rs.getString("instance"));
                submission.setFinalSubmission(rs.getString("status").equals("complete"));
                sdl.add(submission);
            }
        } catch (Exception e) {
            e.printStackTrace();
            submission = null;
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (Exception e) {
                }
            }
            if (ps != null) {
                try {
                    ps.close();
                } catch (Exception e) {
                }
            }
            if (con != null) {
                try {
                    con.close();
                } catch (Exception e) {
                }
            }
        }
        return sdl;

    }

    XmlSubmissionData retrieveLastSubmission(Form form, FormSubject subject, String instance) throws Exception {
        XmlSubmissionData submission = null;
        Connection con = null;
        ResultSet rs = null;
        PreparedStatement ps = null;
        try {
            con = getDFormPool().getConnection();
            ps = con.prepareStatement("select submissions.row_id, submissions.formname, submissions.subject_id, submissions.instance, submissions.formdata, submissions.formdata_aes, submissions.status, p.alfacode  from submissions  "
                    + " left join patients p on submissions.subject_id=p.row_id "
                    + " where submissions.row_id= (select max(row_id) as mx_row from submissions "
                    + "  where submissions.formname=? and submissions.subject_id=? and submissions.instance=? "
                    + "  group by submissions.formname, submissions.subject_id, submissions.instance"
                    + " )"
                    + " ");
            int i = 0;
            ps.setString(++i, form.getName());
            ps.setInt(++i, subject.getId().intValue());
            ps.setString(++i, instance);
            rs = ps.executeQuery();
            submission = new XmlSubmissionData();
            if (rs.first()) {
                submission.setId(rs.getInt("row_id"));
                submission.setSubjectId(rs.getInt("subject_id"));
                submission.setSubjectAlfacode(rs.getString("alfacode"));
                submission.setInstance(rs.getString("instance"));
                if (masterPassword.getUseMasterPassword()) {
                    submission.setXml(masterPassword.aesDecrypt(rs.getBytes("formdata_aes")));
                } else {
                    submission.setXml(rs.getString("formdata"));
                }
                submission.setFinalSubmission(rs.getString("status").equals("complete"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            submission = null;
            throw new Exception(e);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (Exception e) {
                }
            }
            if (ps != null) {
                try {
                    ps.close();
                } catch (Exception e) {
                }
            }
            if (con != null) {
                try {
                    con.close();
                } catch (Exception e) {
                }
            }
        }
        return submission;

    }

    List<XmlSubmissionData> retrieveSubmissionDataOrderedBySubject(boolean onlyFinal) {

        ArrayList<XmlSubmissionData> submissions = null;
        Connection con = null;
        ResultSet rs = null;
        PreparedStatement ps = null;
        try {
            con = getDFormPool().getConnection();
            // Note that the situation where older submissions are marked as "complete", and the last submission marked as "edit"
            // MAY exist (when the submission has been converted from "final" back to "draft"). Only look at the LAST submission, 
            // previous submissions are history. If the LAST one is draft (status=edit"), don't include it.
            String sql = "select s.row_id, s.formname, s.subject_id, s.instance, s.formdata, s.formdata_aes, s.status, p.alfacode from submissions  s "
                    + " inner join (select max(row_id) as mx_row, formname, subject_id, instance from submissions group by formname, subject_id, instance) as onlyLastSubmission on onlyLastSubmission.mx_row=s.row_id "
                    + " left join patients p on s.subject_id=p.row_id ";

            if (onlyFinal) {
                sql = sql + " where s.status = 'complete' ";
            }
            sql = sql + "  order by s.subject_id, s.created";
            ps = con.prepareStatement(sql);
            rs = ps.executeQuery();
            submissions = new ArrayList<XmlSubmissionData>();
            while (rs.next()) {
                XmlSubmissionData submission = new XmlSubmissionData();
                submission.setId(rs.getInt("row_id"));
                submission.setSubjectId(rs.getInt("subject_id"));
                submission.setSubjectAlfacode(rs.getString("alfacode"));
                submission.setInstance(rs.getString("instance"));
                if (masterPassword.getUseMasterPassword()) {
                    submission.setXml(masterPassword.aesDecrypt(rs.getBytes("formdata_aes")));
                } else {
                    submission.setXml(rs.getString("formdata"));
                }
                submission.setFinalSubmission(rs.getString("status").equals("complete"));
                submissions.add(submission);
            }
        } catch (Exception e) {
            e.printStackTrace();
            submissions = null;
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (Exception e) {
                }
            }
            if (ps != null) {
                try {
                    ps.close();
                } catch (Exception e) {
                }
            }
            if (con != null) {
                try {
                    con.close();
                } catch (Exception e) {
                }
            }
        }
        return submissions;
    }

    /**
     * retrieves all users, search term and maxResults are ignored at this time.
     *
     * @param searchTerm : ignored
     * @param maxResults : ignored
     * @return
     */
    public List<User> retrieveAvailableUsersList(String searchTerm, int maxResults) {

        ArrayList<User> availableUsers = null;
        Connection con = null;
        ResultSet rs = null;
        PreparedStatement ps = null;
        try {
//            if (searchTerm != null) {
            con = getDFormPool().getConnection();
            ps = con.prepareStatement("select users.row_id, users.uuid, users.name,users.login, users.street,"
                    + " users.zip, users.city, users.email, users.affiliation, users.segment, "
                    + " group_concat(usergroups.group_id separator 'QQQQQQQQQ') as groups  from users "
                    + "left join usergroups on usergroups.user_login=users.login "
                    + "group by users.row_id");
//                ps.setString(1, "%" + searchTerm.trim() + "%");
//                ps.setInt(2, maxResults);
            rs = ps.executeQuery();
            availableUsers = new ArrayList<User>();
            while (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("row_id"));
                user.setZip(rs.getString("zip"));
                user.setAddress(rs.getString("street"));
                user.setCity(rs.getString("city"));
                user.setUuid(rs.getString("uuid"));
                user.setName(rs.getString("name"));
                user.setEmail(rs.getString("email"));
                user.setAffiliation(rs.getString("affiliation"));
                user.setSegment(rs.getString("segment"));
                user.setLogin(rs.getString("login"));
                user.setGroups(rs.getString("groups") == null ? null : rs.getString("groups").split("QQQQQQQQQ"));
                availableUsers.add(user);
//                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            availableUsers = null;
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (Exception e) {
                }
            }
            if (ps != null) {
                try {
                    ps.close();
                } catch (Exception e) {
                }
            }
            if (con != null) {
                try {
                    con.close();
                } catch (Exception e) {
                }
            }
        }
        return availableUsers;
    }

    /**
     * updates the user's password in the database to the password in the user
     * object. If password in user is empty, password in database is set to
     * NULL, which means : login not possible
     */
    public void savePassword(User user) throws Exception {
        Connection con = null;
        ResultSet rs = null;
        PreparedStatement ps = null;
        try {
            con = getDFormPool().getConnection();
            String uuid = user.getUuid();
            int i = 0;
            if (user.getPassword() == null || user.getPassword().isEmpty()) {
                ps = con.prepareStatement("update users set passwd_hash=null where uuid=?");
            } else {
                ps = con.prepareStatement("update users set passwd_hash=md5(?) where uuid=?");
                ps.setObject(++i, user.getPassword());
            }

            ps.setString(++i, uuid);
            ps.execute();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (Exception e) {
                }
            }
            if (ps != null) {
                try {
                    ps.close();
                } catch (Exception e) {
                }
            }
            if (con != null) {
                try {
                    con.close();
                } catch (Exception e) {
                }
            }
        }
    }

    /**
     * saves user, if uuid is "new", creates a new user. Returns UUID of new or
     * updated user. Does NOT save the password, even if it is set in the user
     * object.
     *
     * @param user
     * @return
     */
    public String saveUser(User user) throws Exception {

        Connection con = null;
        ResultSet rs = null;
        PreparedStatement ps = null;
        String uuid = null;
        try {
            con = getDFormPool().getConnection();
            con.setAutoCommit(false);
            boolean createNewUser = user.isNew();
            if (createNewUser) {
                uuid = UUID.randomUUID().toString();

                ps = con.prepareStatement("insert into users (uuid, name, login, street,"
                        + "  zip,  city,  email,  affiliation,  segment) values ("
                        + " ?,?,?,?,?,?,?,?,?)");
                int i = 0;
                ps.setString(++i, uuid);
                ps.setString(++i, user.getName());
                ps.setString(++i, user.getLogin());
                ps.setString(++i, user.getAddress());
                ps.setString(++i, user.getZip());
                ps.setString(++i, user.getCity());
                ps.setString(++i, user.getEmail());
                ps.setString(++i, user.getAffiliation());
                ps.setString(++i, user.getSegment());
            } else {
                uuid = user.getUuid();
                ps = con.prepareStatement("update users set name=?, login=?, street=?,"
                        + "  zip=?,  city=?,  email=?,  affiliation=?,  segment=? where uuid=?");
                int i = 0;
                ps.setObject(++i, user.getName());
                ps.setObject(++i, user.getLogin());
                ps.setObject(++i, user.getAddress());
                ps.setObject(++i, user.getZip());
                ps.setObject(++i, user.getCity());
                ps.setObject(++i, user.getEmail());
                ps.setObject(++i, user.getAffiliation());
                ps.setObject(++i, user.getSegment());
                ps.setString(++i, uuid);
            }
            ps.execute();
            ps.close();
            ps = con.prepareStatement("delete from usergroups where user_login=?");
            ps.setString(1, user.getLogin());
            ps.execute();
            ps.close();
            ps = con.prepareStatement("insert into usergroups (user_login,group_id) values (?,?)");
            if (user.getGroups() != null) {
                for (String gr : user.getGroups()) {
                    ps.setString(1, user.getLogin());
                    ps.setString(2, gr);
                    ps.execute();
                }
            }
            con.commit();

            user.setUuid(uuid.toString());// Only if execute did not throw an exception otherwise user is not "new" anymore.
        } catch (Exception e) {
            con.rollback();
            e.printStackTrace();
            throw e;
        } finally {
            con.setAutoCommit(true);
            if (rs != null) {
                try {
                    rs.close();
                } catch (Exception e) {
                }
            }
            if (ps != null) {
                try {
                    ps.close();
                } catch (Exception e) {
                }
            }
            if (con != null) {
                try {
                    con.close();
                } catch (Exception e) {
                }
            }
        }
        return uuid;
    }

    /**
     * saves output transformation, if it's isNew method returns true, creates a
     * new outputtransformation. Returns UUID of new or updated output
     * transformation. D
     *
     * @param outputTransformation
     * @return
     */
    public String saveOutputTransformation(OutputTransformation outputTransformation) throws Exception {

        Connection con = null;
        ResultSet rs = null;
        PreparedStatement ps = null;
        String uuid = null;
        try {
            con = getDFormPool().getConnection();
            con.setAutoCommit(false);
            boolean createNew = outputTransformation.isNew();
            if (createNew) {
                uuid = UUID.randomUUID().toString();

                ps = con.prepareStatement("insert into outputtransformations (uuid, title, xslt, outputtype) values ("
                        + " ?,?,?,?)");
                int i = 0;
                ps.setString(++i, uuid);
                ps.setString(++i, outputTransformation.getTitle());
                ps.setString(++i, outputTransformation.getXslt());
                ps.setString(++i, outputTransformation.getOutputType());
            } else {
                uuid = outputTransformation.getUuid();
                ps = con.prepareStatement("update outputtransformations set title=?, xslt=?, outputtype=? where uuid=?");
                int i = 0;
                ps.setObject(++i, outputTransformation.getTitle());
                ps.setObject(++i, outputTransformation.getXslt());
                ps.setObject(++i, outputTransformation.getOutputType());
                ps.setString(++i, uuid);
            }
            ps.execute();
            ps.close();
            con.commit();

            outputTransformation.setUuid(uuid.toString());// Only if execute did not throw an exception otherwise user is not "new" anymore.
        } catch (Exception e) {
            con.rollback();
            e.printStackTrace();
            throw e;
        } finally {
            con.setAutoCommit(true);
            if (rs != null) {
                try {
                    rs.close();
                } catch (Exception e) {
                }
            }
            if (ps != null) {
                try {
                    ps.close();
                } catch (Exception e) {
                }
            }
            if (con != null) {
                try {
                    con.close();
                } catch (Exception e) {
                }
            }
        }
        return uuid;
    }

    /**
     * Delete output transformation
     *
     * @param outputTransformation
     *
     * @throws Exception
     */
    public void deleteOutputTransformation(OutputTransformation outputTransformation) throws Exception {

        Connection con = null;
        ResultSet rs = null;
        PreparedStatement ps = null;
        String uuid = null;
        try {
            con = getDFormPool().getConnection();
            con.setAutoCommit(false);
            uuid = outputTransformation.getUuid();
            ps = con.prepareStatement("delete from outputtransformations where uuid=?");
            int i = 0;
            ps.setString(++i, uuid);
            ps.execute();
            ps.close();
            con.commit();

            outputTransformation.setUuid(uuid.toString());// Only if execute did not throw an exception otherwise user is not "new" anymore.
        } catch (Exception e) {
            con.rollback();
            e.printStackTrace();
            throw e;
        } finally {
            con.setAutoCommit(true);
            if (rs != null) {
                try {
                    rs.close();
                } catch (Exception e) {
                }
            }
            if (ps != null) {
                try {
                    ps.close();
                } catch (Exception e) {
                }
            }
            if (con != null) {
                try {
                    con.close();
                } catch (Exception e) {
                }
            }
        }
    }

    public List retrieveStatusOfAllSubmissionsForAllSubjects() {
        ArrayList<FormSubject> subjectStatusList = null;
        Connection con = null;
        ResultSet rs = null;
        PreparedStatement ps = null;
        try {
            con = getDFormPool().getConnection();

            boolean onlyMySegment = true; // First : strict
            boolean onlyCreatedByMe = true; // First : strict
            ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();

            if (ec.isUserInRole("UseAllSubjects")) {
                onlyMySegment = false; // relax initially strict condition
                onlyCreatedByMe = false; // relax
            } else if (ec.isUserInRole("UseSegmentSubjects")) {
                onlyMySegment = true;
                onlyCreatedByMe = false;
            } else if (ec.isUserInRole("UseMySubjects")) {
                onlyMySegment = false;
                onlyCreatedByMe = true;
            }

            // First sort by cast instance as signed : if instances are numeric, then sort order is numeric. Otherwise, cast is 0, and 3rd sort term is used. If mixed, then problem.
            String sqlFields = "select patients.row_id, patients.uuid, patients.name, patients.version, patients.alfacode, users.name as createdby, submissionlist.status from patients ";
            String firstJoin = "left join "
                    + " ( select "
                    + "      "
                    + "      group_concat("
                    + "        concat_ws('', case when submissions.status='edit' then '<i>' else '<b>' end, "
                    + "                  left(submissions.formname,1), "
                    + "                  case when submissions.instance<>'__singleinstance__' then submissions.instance else null end, "
                    + "                  case when submissions.status='edit' then '</i>' else '</b>' end) "
                    + "        order by submissions.formname desc, cast(submissions.instance as signed), submissions.instance"
                    + "        separator ' '"
                    + "      ) as status, "
                    + "      submissions.subject_id "
                    + "    from submissions, "
                    + "    (select max(row_id) as lastrowid "
                    + "     from submissions "
                    + "     group by formname, instance, subject_id) lastsubmission "
                    + "    where "
                    + "    lastsubmission.lastrowid=submissions.row_id  "
                    + "    group by subject_id "
                    + "  ) submissionlist on patients.row_id=submissionlist.subject_id";

            String sql = sqlFields;
            if (onlyCreatedByMe) {
                sql += " inner join users on users.uuid = patients.created_by and users.login=? ";
            } else {
                sql += " left join users on users.uuid = patients.created_by ";
            }
            if (onlyMySegment) {
                sql += " inner join users segments on segments.segment=patients.segment and segments.login=? ";
            }

            sql += firstJoin;

            ps = con.prepareStatement(sql);
            int i = 0;
            if (onlyCreatedByMe) {
                ps.setString(++i, ec.getUserPrincipal().getName());
            }
            if (onlyMySegment) {
                ps.setString(++i, ec.getUserPrincipal().getName());
            }
            rs = ps.executeQuery();

            subjectStatusList = new ArrayList<>();
            while (rs.next()) {
                FormSubject subject = new FormSubject(rs.getInt("row_id"),
                        rs.getString("uuid"),
                        rs.getString("name"),
                        rs.getInt("version"),
                        rs.getString("alfacode"),
                        rs.getString("createdby"),
                        true);
                subject.putProperty("statusdescription", rs.getString("status"));
                subjectStatusList.add(subject);
            }
        } catch (Exception e) {
            e.printStackTrace();
            subjectStatusList = null;
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (Exception e) {
                }
            }
            if (ps != null) {
                try {
                    ps.close();
                } catch (Exception e) {
                }
            }
            if (con != null) {
                try {
                    con.close();
                } catch (Exception e) {
                }
            }
        }
        return subjectStatusList;
    }

    Map<String, BigDecimal> getSegmentPatientStatData() throws SQLException, NamingException {
        Map<String, BigDecimal> patPerSeg = new TreeMap();
        try (Connection con = getDFormPool().getConnection();
                PreparedStatement ps = con.prepareStatement("select segment, count(*) as nr from patients group by segment");
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int segInt = rs.getInt("segment");
                SelectItem si = Arrays.stream(uec.getSegmentItems()).filter(p -> p.getValue().equals(Integer.toString(segInt))).findFirst().orElse(null);
                if (si != null) {
                    patPerSeg.put(si.getLabel(), rs.getBigDecimal("nr"));
                }
            }
        }
        return patPerSeg;
    }

    Map<String, BigDecimal> getSubPerTypeStatData(boolean draftNotFinal) throws SQLException, NamingException {
        Map<String, BigDecimal> subPerInstance = new TreeMap();
        String sql = " select "
                + "      "
                + "        case when submissions.status='edit' then 'Draft' else 'Final' end as status, "
                + "        concat_ws('',left(submissions.formname,1),right(submissions.formname,1), "
                + "                  case when (submissions.instance<>'__singleinstance__')  then lpad(submissions.instance,2,0) else '' end) as subm, "
                + "                  count(*) as nr "
                + "    from submissions left join forms on forms.row_id=submissions.formsource_rowid, "
                + "    (select max(row_id) as lastrowid "
                + "     from submissions "
                + "     group by formname, instance, subject_id) lastsubmission "
                + "    where "
                + "    lastsubmission.lastrowid=submissions.row_id  "
                + "    group by submissions.status, subm ";

        try (Connection con = getDFormPool().getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {

                if (draftNotFinal ? rs.getString("status").equals("Draft") : rs.getString("status").equals("Final")) {
                    subPerInstance.put(rs.getString("subm"), rs.getBigDecimal("nr"));
                }
            }
        }
        return subPerInstance;
    }

}
