package dk.syslab.supv.web.api;

import dk.syslab.supv.ldap.LdapService;
import dk.syslab.supv.ldap.LdapUser;
import dk.syslab.supv.web.JwtService;
import dk.syslab.supv.web.api.model.AuthRequest;
import dk.syslab.supv.web.api.model.AuthResponse;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@RestController
public class AuthController {
    private final static int VALID_PERIOD = 6; // hours

    @Autowired
    private JwtService jwtService;

    @Autowired
    private LdapService ldapService;

    @RequestMapping(
            value = "/api/auth",
            method = RequestMethod.POST
    )
    public AuthResponse auth(@RequestBody AuthRequest authRequest, HttpServletResponse response) throws IOException {

        if (!authRequest.isReady()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid input");
            return null;
        }

        if (!ldapService.authenticate(authRequest.username, authRequest.password)) {
            authRequest.erasePassword();
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Bad credentials");
            return null;
        }
        authRequest.erasePassword();

        LdapUser user = ldapService.getUser(authRequest.getUsername());
        if (user == null) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to find and map user");
            return null;
        }

        // validate that user is:
        //  member of ou=machines/cn=all-nodes - required to be allowed to modify settings in supervisor as this is required for ssh access to nodes
        //  member of either cn=user or cn=admin - required to a member of users or admins (otherwise you may have a user that simply plugged their laptop into a switch in SYSLAB)
        if (!user.hasRole("all-nodes") || !(user.hasRole("user") || user.hasRole("admin"))) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "User does not have correct permissions (directory) to use the system!");
            return null;
        }

        // create Json Web Token
        LocalDateTime date = LocalDateTime.now(ZoneId.systemDefault());
        Date now = Date.from(date.atZone(ZoneId.systemDefault()).toInstant());
        Date exp = Date.from(date.plusHours(VALID_PERIOD).atZone(ZoneId.systemDefault()).toInstant());

        boolean admin = user.hasRole("admin");

        Claims claims = Jwts.claims()
            .setSubject(user.getUsername())
            .setIssuedAt(now)
            .setExpiration(exp)
            .setIssuer("ldap");
        claims.put("admin", admin);

        String jwt = Jwts.builder()
            .setClaims(claims)
            .signWith(SignatureAlgorithm.RS256, jwtService.getSigningKey())
            .compact();

        // create json "body" output
        return new AuthResponse(jwt, user.getDisplayName(), user.getMail(), exp);
    }
}
