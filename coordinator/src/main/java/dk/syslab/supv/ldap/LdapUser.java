package dk.syslab.supv.ldap;

import java.util.ArrayList;
import java.util.List;

public class LdapUser {
    private String username;    // uid/cn/name
    private String dn;          // distinguishedname
    private String mail;        // mail
    private String displayName; // displayname
    private String givenName;   // givenname
    private String lastName;    // sn
    private String phone;       // telephonenumber
    private String mobile;      // mobile
    private String title;       // title
    private int postalCode;     // postalcode
    private String street;      // street
    private String physicalDeliveryOfficeName;
    private String language;    // preferredlanguage
    private String location;    // l
    private String country;     // c/co
    private String organizationalUnit; // ou
    private String organization; // o
    private String initials;    // initials
    private byte[] photo;       // jpegphoto
    // ignored: description, proxyaddresses, homedirectory, gidnumber, othermailbox, uidnumber

    private List<String> roles;

    public LdapUser() {}

    public LdapUser(String username, String dn, String mail, String displayName, String givenName, String lastName, String phone, String mobile, String title, int postalCode, String street, String physicalDeliveryOfficeName, String language, String location, String country, String organizationalUnit, String organization, String initials, byte[] photo) {
        this.username = username;
        this.dn = dn;
        this.mail = mail;
        this.displayName = displayName;
        this.givenName = givenName;
        this.lastName = lastName;
        this.phone = phone;
        this.mobile = mobile;
        this.title = title;
        this.postalCode = postalCode;
        this.street = street;
        this.physicalDeliveryOfficeName = physicalDeliveryOfficeName;
        this.language = language;
        this.location = location;
        this.country = country;
        this.organizationalUnit = organizationalUnit;
        this.organization = organization;
        this.initials = initials;
        this.photo = photo;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDn() {
        return dn;
    }

    public void setDn(String dn) {
        this.dn = dn;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getGivenName() {
        return givenName;
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(int postalCode) {
        this.postalCode = postalCode;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getPhysicalDeliveryOfficeName() {
        return physicalDeliveryOfficeName;
    }

    public void setPhysicalDeliveryOfficeName(String physicalDeliveryOfficeName) {
        this.physicalDeliveryOfficeName = physicalDeliveryOfficeName;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getOrganizationalUnit() {
        return organizationalUnit;
    }

    public void setOrganizationalUnit(String organizationalUnit) {
        this.organizationalUnit = organizationalUnit;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public String getInitials() {
        return initials;
    }

    public void setInitials(String initials) {
        this.initials = initials;
    }

    public byte[] getPhoto() {
        return photo;
    }

    public void setPhoto(byte[] photo) {
        this.photo = photo;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public boolean hasRole(String role) {
        return roles != null && roles.size() != 0 && roles.contains(role.toLowerCase());
    }
}
