package dk.syslab.supv.ldap;

import com.unboundid.ldap.sdk.*;
import com.unboundid.ldap.sdk.extensions.StartTLSExtendedRequest;
import com.unboundid.util.ssl.SSLUtil;
import com.unboundid.util.ssl.TrustAllTrustManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import javax.net.ssl.SSLContext;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

@Service
public class LdapService {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private SSLContext sslContext;
    private LdapConfiguration config;

    @Autowired
    public LdapService(Environment env) throws GeneralSecurityException {
        this.config = new LdapConfiguration(
            env.getRequiredProperty("ldap.contextSource.server"),
            Integer.parseInt(env.getRequiredProperty("ldap.contextSource.port")),
            env.getRequiredProperty("ldap.contextSource.userDn"),
            env.getRequiredProperty("ldap.contextSource.password"),
            env.getRequiredProperty("ldap.contextSource.base"),
            env.getRequiredProperty("ldap.defaultLdapAuthoritiesPopulator.groupSearchBase"));
        SSLUtil sslUtil = new SSLUtil(new TrustAllTrustManager());
        sslContext = sslUtil.createSSLContext();
    }

    private LDAPConnection getConnection() {
        try {
            LDAPConnection connection = new LDAPConnection(config.getServer(), config.getPort());
            ExtendedResult extendedResult = connection.processExtendedOperation(new StartTLSExtendedRequest(sslContext));
            if (extendedResult.getResultCode() == ResultCode.SUCCESS) {
                BindResult auth = connection.bind(config.getBindUser(), config.getBindPassword());
                if (auth.getResultCode().isConnectionUsable()) {
                    return connection;
                } else {
                    connection.close();
                    return null;
                }
            } else {
                log.error("Unable to connect to LDAP server");
                connection.close();
                return null;
            }
        } catch (LDAPException e) {
            log.error("Unable to connect to LDAP server", e);
            return null;
        }
    }

    private String getUserDN(LDAPConnection connection, String userid) throws LDAPSearchException {
        SearchResult result = connection.search(config.getBaseDN(), SearchScope.SUB, "(uid=" + userid + ")");
        if (result.getEntryCount() > 0) {
            return result.getSearchEntries().get(0).getDN(); // get the first entry... userid's are supposed to be unique
        } else {
            return null;
        }
    }

    public boolean authenticate(String username, char[] password) {
        LDAPConnection connection = getConnection();
        if (connection != null) {
            try {
                String dn = getUserDN(connection, username);
                if (dn == null || dn.isEmpty()) return false;
                BindResult result = connection.bind(dn, String.valueOf(password));
                return result.getResultCode() == ResultCode.SUCCESS;
            } catch (LDAPException e) {
                if (e.getResultCode() != ResultCode.INVALID_CREDENTIALS) { // don't log invalid login exception, that case is just fine
                    log.error("Unable to authenticate user: " + username, e);
                }
            } finally {
                connection.close();
            }
        }
        return false;
    }

    public LdapUser getUser(String userid) {
        LDAPConnection connection = getConnection();
        if (connection != null) {
            try {
                SearchResult result = connection.search(config.getBaseDN(), SearchScope.SUB, "(uid=" + userid + ")");
                if (result.getEntryCount() > 0) {
                    LdapUser ldapUser = new LdapUser();

                    // get attributes
                    SearchResultEntry entry = result.getSearchEntries().get(0);
                    if (entry.getAttribute("uid") != null) ldapUser.setUsername(entry.getAttributeValue("uid"));
                    if (entry.getAttribute("distinguishedname") != null) ldapUser.setDn(entry.getAttributeValue("distinguishedname"));
                    if (entry.getAttribute("mail") != null) ldapUser.setMail(entry.getAttributeValue("mail"));
                    if (entry.getAttribute("displayname") != null) ldapUser.setDisplayName(entry.getAttributeValue("displayname"));
                    if (entry.getAttribute("givenname") != null) ldapUser.setGivenName(entry.getAttributeValue("givenname"));
                    if (entry.getAttribute("sn") != null) ldapUser.setLastName(entry.getAttributeValue("sn"));
                    if (entry.getAttribute("telephonenumber") != null) ldapUser.setPhone(entry.getAttributeValue("telephonenumber"));
                    if (entry.getAttribute("mobile") != null) ldapUser.setMobile(entry.getAttributeValue("mobile"));
                    if (entry.getAttribute("title") != null) ldapUser.setTitle(entry.getAttributeValue("title"));
                    if (entry.getAttribute("postalcode") != null) ldapUser.setPostalCode(entry.getAttributeValueAsInteger("postalcode"));
                    if (entry.getAttribute("street") != null) ldapUser.setStreet(entry.getAttributeValue("street"));
                    if (entry.getAttribute("physicaldeliveryofficename") != null) ldapUser.setPhysicalDeliveryOfficeName(entry.getAttributeValue("physicaldeliveryofficename"));
                    if (entry.getAttribute("preferredlanguage") != null) ldapUser.setLanguage(entry.getAttributeValue("preferredlanguage"));
                    if (entry.getAttribute("l") != null) ldapUser.setLocation(entry.getAttributeValue("l"));
                    if (entry.getAttribute("co") != null) ldapUser.setCountry(entry.getAttributeValue("co"));
                    if (entry.getAttribute("ou") != null) ldapUser.setOrganizationalUnit(entry.getAttributeValue("ou"));
                    if (entry.getAttribute("o") != null) ldapUser.setOrganization(entry.getAttributeValue("o"));
                    if (entry.getAttribute("initials") != null) ldapUser.setInitials(entry.getAttributeValue("initials"));
                    if (entry.getAttribute("jpegphoto") != null) ldapUser.setPhoto(entry.getAttributeValueBytes("jpegphoto"));

                    // get roles
                    SearchResult rolesResult = connection.search(config.getFullGroupSearchBase(), SearchScope.SUB, "(memberUid=" + userid + ")");
                    List<String> roles = new ArrayList<>();
                    for (SearchResultEntry roleEntry : rolesResult.getSearchEntries()) {
                        roles.add(roleEntry.getAttributeValue("cn").toLowerCase());
                    }
                    ldapUser.setRoles(roles);

                    return ldapUser;
                } else {
                    return null;
                }
            } catch (LDAPSearchException e) {
                return null;
            } finally {
                connection.close();
            }
        }
        return null;
    }

}
