# DForm form data collection interface

## Introduction
This data entry system provides a secure web-based multi-user form-filling interface, a form design interface, data storage, and an output filter. The data can be exported in a standard format to a spreadsheet for further processing. Several forms can be created, and they are linked to "subjects". The subjects in the default setup are in the table "patients".

## Dependencies

Libraries necessary for compilation :

 * commons-codec-1.13.jar
 * commons-collections4-4.4.jar
 * commons-compress-1.19.jar
 * commons-logging-1.1.jar
 * commons-math3-3.6.1.jar
 * omnifaces-3.8.1.jar
 * poi-4.1.2.jar
 * poi.ooxml-4.1.2.jar
 * poi.ooxml-schemas-4.1.2.jar
 * primefaces-8.0.jar
 * xmlbeans-3.1.0.jar

Environment : 
 * MySQL or MariaDB database server
 * Payara application server

## Installation
 * Create the database structure from DForm/src/java/be/digon/dform/resources/dform.sql
 * Create segments for users in table segments (a non-admin user can only see subjects in his/her own segment).
 * Create an admin user in table users, use md5 for the password hash : insert into users (uuid, name, login, passwd_hash, segment) values (uuid(),'Test Name','test',md5('testpassword'),1);
 * In the Payara server, create a datasource with JDBC-name : jdbc/DFormPool, referring to a database with driver org.mariadb.jdbc.MariaDbDataSource
 * In the Payara server, configure security as follows :
  * Configurations,Server-config/Security/Realms, make a realm called "DFormRealm"
  * JAAS Context : jdbcRealm, 
  * jNDI : jdbc/DFormPool
  * User Table : users
  * User Name Column : login
  * Password Column : passwd_hash
  * Group Table : usergroups
  * Group table User Name Column : user_login
  * Group Name Column : group_id
  * Digest Algorithm : MD5


 * Optionally create a master password for encryption of all submitted data. If this is set, all submission-data will be AES-encrypted. If this password is lost, there is no way back. To set a master password "ChangeThisMasterPassword" with a salt "ThisIsASalt", do : insert into parameters (mp_hash,mp_salt) values (unhex(sha2("ChangeThisMasterPasswordThisIsASalt",256)),'ThisIsASalt');

## License
 * GNU Affero GPL version 3, see https://www.gnu.org
