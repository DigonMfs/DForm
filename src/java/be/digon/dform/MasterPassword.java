/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.digon.dform;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.enterprise.context.ApplicationScoped;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.transaction.Transactional;
import org.apache.commons.codec.binary.Hex;

/**
 * Stores the database submissions in-memory master password, during the
 * lifetime of the application. Ref. :
 * https://mkyong.com/java/java-aes-encryption-and-decryption/
 *
 * @author rombouts
 */
@ApplicationScoped
public class MasterPassword {

    private static final String ENCRYPT_ALGO = "AES/GCM/NoPadding";

    private static final int TAG_LENGTH_BIT = 128; // must be one of {128, 120, 112, 104, 96}
    private static final int IV_LENGTH_BYTE = 12;
    private static final int SALT_LENGTH_BYTE = 16;
    private static final Charset UTF_8 = StandardCharsets.UTF_8;

    private String masterPassword = null;
    private Boolean useMasterPassword = false;
    private Boolean masterPasswordCorrectlySetCache = false;
    
    private SecureRandom secureRandom = new SecureRandom(); // May take a VERY long time to initialize. https://stackoverflow.com/questions/137212/how-to-deal-with-a-slow-securerandom-generator

    byte[] passwordHashReference = null;
    String passwordHashSaltString = null;

    /**
     * May, for critical operations like writing in the database, be re-checked
     * (to avoid garbage data getting into the DB when the in-memory master
     * password accidentally would be overwritten due to e.g. programming
     * error).
     *
     * Further work : does the availability of a salted sha2-hash provide more
     * information about the password in the AES context ?
     *
     * Example of how to set a password in mysql : insert into parameters
     * (mp_hash,mp_salt) values
     * (unhex(sha2("ThisIsThePasswordThisIsTheSalt",256)),'ThisIsTheSalt'); (!
     * Choose better password/salt strings than these toy examples of course)
     *
     * @return
     * @throws NoSuchAlgorithmException
     */
    public Boolean checkMasterPasswordCorrectlySet() throws NoSuchAlgorithmException {
        masterPasswordCorrectlySetCache = false;

        if ((masterPassword != null) && (passwordHashReference != null)) {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String pwAndHash = masterPassword + passwordHashSaltString;
            byte[] encodedhash = digest.digest(pwAndHash.getBytes(UTF_8));
            masterPasswordCorrectlySetCache = Arrays.equals(encodedhash, passwordHashReference);
            if (!masterPasswordCorrectlySetCache) {
                //Reset to empty :
                masterPassword = null;
            }
        }
        return masterPasswordCorrectlySetCache;
    }

    public Boolean getMasterPasswordCorrectlySetCache() {
        return masterPasswordCorrectlySetCache;
    }

    public Boolean getMasterPasswordShouldBeSet() {
        return useMasterPassword && !getMasterPasswordCorrectlySetCache();
    }

    public String getMasterPassword() {
        return masterPassword;
    }

    public void setMasterPassword(String masterPassword) {
        this.masterPassword = masterPassword;

    }

    public Boolean getUseMasterPassword() {
        return useMasterPassword;
    }

    private DataSource getDFormPool() throws NamingException {
        Context c = new InitialContext();
        return (DataSource) c.lookup("java:comp/env/DFormPool");
    }

    @PostConstruct
    public void init() {
        useMasterPassword = false;

        try (Connection con = getDFormPool().getConnection();
                PreparedStatement ps = con.prepareStatement("select mp_hash, mp_salt from parameters limit 1");
                ResultSet rs = ps.executeQuery();) {
            if (rs.first()) {
                passwordHashReference = rs.getBytes("mp_hash");
                passwordHashSaltString = rs.getString("mp_salt");
                if (passwordHashReference != null) {
                    useMasterPassword = true;
                }
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Master password from DB", e);
        }

    }
    private static final Logger LOG = Logger.getLogger(MasterPassword.class.getName());

    /**
     * AES-encryption in Java, otherwise password has to be sent to the database
     * (database logging could reveal it). Initialisation vector and salt are
     * generated and stored in the encryption result. Different salt and IV for
     * each encryption block.
     *
     * Note that if data is null, the return value will also be null (and hence
     * a null value is not kept secret).
     *
     * @param password
     * @param strKey
     * @return
     * @throws java.lang.Exception
     */
    public byte[] aesEncrypt(String data) throws Exception {
        if (data == null) {
            return null; // This is not completely secret ...
        }
        byte[] salt = generateNonce(SALT_LENGTH_BYTE);
        SecretKey key = getKeyFromPassword(masterPassword, salt);
        byte[] iv = generateNonce(IV_LENGTH_BYTE);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_LENGTH_BIT, iv));

        byte[] cleartext = data.getBytes(UTF_8);
        byte[] ciphertextBytes = cipher.doFinal(cleartext);

        byte[] cipherTextWithIvSalt = ByteBuffer.allocate(iv.length + salt.length + ciphertextBytes.length)
                .put(iv)
                .put(salt)
                .put(ciphertextBytes)
                .array();

        return cipherTextWithIvSalt;

    }

    public SecretKey getKeyFromPassword(String password, byte[] salt)
            throws NoSuchAlgorithmException, InvalidKeySpecException {

        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 256);
        SecretKey secret = new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
        return secret;
    }

    public byte[] generateNonce(int nrBytes) {
        byte[] iv = new byte[nrBytes];
        secureRandom.nextBytes(iv);
        return iv;
    }

    /**
     * AES-encryption in Java, otherwise password has to be sent to the database
     * (database logging could reveal it).
     *
     *
     * @param passwordhex
     * @param strKey
     * @return
     * @throws java.lang.Exception
     */
    public String aesDecrypt(byte[] data) throws Exception {

        if (data == null) {
            return null;
        }

        ByteBuffer bb = ByteBuffer.wrap(data);

        byte[] iv = new byte[IV_LENGTH_BYTE];
        bb.get(iv);

        byte[] salt = new byte[SALT_LENGTH_BYTE];
        bb.get(salt);

        byte[] cipherText = new byte[bb.remaining()];
        bb.get(cipherText);

        // get back the aes key from the same password and salt
        SecretKey key = getKeyFromPassword(masterPassword, salt);

        Cipher cipher = Cipher.getInstance(ENCRYPT_ALGO);

        cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_LENGTH_BIT, iv));

        byte[] plainBytes = cipher.doFinal(cipherText);
        return new String(plainBytes, UTF_8);
    }

    /**
     * This stores a new master password. Make sure to encrypt the database submissions within the same transaction.
     * A master password can only be set when in the parameter table a database administrator (with direct access to the database) has set mp_unlock to <> 0. This avoids a rogue application administrator to encrypt the database (or to decrypt it).
     * Do sufficient checks to avoid ending up with an unreadable database.
     * 
     *
     * @param newPassword
     * @throws Exception
     */
    @Transactional(rollbackOn = Exception.class)
    void storePasswordHash(String newPassword) throws Exception {
        if (useMasterPassword) {
            throw new Exception("A master password hash is already set.");
        }
        throwIfEnDecryptionNotAllowed();
        try (Connection con = getDFormPool().getConnection();
                PreparedStatement ps = con.prepareStatement("update parameters set mp_hash=?, mp_salt=?, mp_unlock=0 ");) {
            byte[] passwordHashBytes = generateNonce(SALT_LENGTH_BYTE);
            String tempPasswordHashSaltString = Hex.encodeHexString(passwordHashBytes);
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String pwAndHash = newPassword + tempPasswordHashSaltString;
            byte[] encodedhash = digest.digest(pwAndHash.getBytes(UTF_8));
            ps.setBytes(1, encodedhash);
            ps.setString(2, tempPasswordHashSaltString);
            ps.execute();

            // And now : set this as the master password, and check, and rollback when error :
            masterPassword = newPassword;
            passwordHashReference = encodedhash;
            passwordHashSaltString = tempPasswordHashSaltString;
            useMasterPassword = true;
            if (!checkMasterPasswordCorrectlySet()) {
                clearPasswordData();
                throw new Exception("Error setting password");
            }
        }
    }

    public void throwIfEnDecryptionNotAllowed() throws Exception{
        try (Connection con = getDFormPool().getConnection();
                ResultSet rsAllowed = con.prepareStatement("select mp_unlock from parameters limit 1").executeQuery();){
            if (!rsAllowed.first() || (rsAllowed.getInt("mp_unlock")==0)) {
                throw new Exception("Removing or setting master password prohibited by database administrator (parameters.mp_unlock=0 or no parameter rows exist).");
            }
        }     
    }
    
    /**
     * Do not call this without having unencrypted all data before, because the
     * unencryption code will not run without the master password hash in place.
     *
     * @param newPassword
     * @throws Exception
     */
    @Transactional(rollbackOn = Exception.class)
    void deletePasswordHash() throws Exception {
        throwIfEnDecryptionNotAllowed();
        try (Connection con = getDFormPool().getConnection();
                PreparedStatement ps = con.prepareStatement("update parameters set mp_hash=null, mp_salt=null,mp_unlock=0 ");) {
            ps.execute();

            // And now : set this as the master password, and check, and rollback when error :
            masterPassword = null;
            passwordHashReference = null;
            passwordHashSaltString = null;
        }
    }

    void clearPasswordData() {
        masterPassword = null;
        passwordHashReference = null;
        passwordHashSaltString = null;
        masterPasswordCorrectlySetCache = false;
        useMasterPassword=false;
    }

}
