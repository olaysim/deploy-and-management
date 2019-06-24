package dk.syslab.supv.web.api;

import dk.syslab.supv.web.JwtService;
import dk.syslab.supv.web.Validator;
import dk.syslab.supv.web.api.model.TokenRequest;
import dk.syslab.supv.web.api.model.TokenResponse;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

@RestController
public class TokenController {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private JwtService jwtService;

    @Autowired
    Validator validator;

    @RequestMapping(
        value = "/api/token",
        method = RequestMethod.POST
    )
    public TokenResponse generateToken(@RequestHeader(value = "Authorization", required = false) String token, @RequestBody(required = false) TokenRequest tokenReq, HttpServletResponse response) throws IOException {
        if (token == null || token.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Requestbody is malformed");
            return null;
        }
        if (jwtService.getSigningKey() == null) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Key to sign token with is missing");
            return null;
        }
        Claims claims = validator.validate(token, response);
        if (claims != null) {
            if (!validator.isAdmin(claims) && tokenReq.getDays() > 60) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "You need ADMINISTRATIVE privileges to request a token that is valid for more than 60 days!");
                return null;
            }
            if (!validator.isAdmin(claims) && tokenReq.getAdmin()) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "You need ADMINISTRATIVE privileges to request a token that admin privileges!");
                return null;
            }

            LocalDate date = LocalDate.now();
            Date now = Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
            Date exp = Date.from(date.plusDays(tokenReq.getDays()).atStartOfDay(ZoneId.systemDefault()).toInstant());

            Claims newClaims = Jwts.claims()
                .setSubject(claims.getSubject())
                .setIssuedAt(now)
                .setExpiration(exp)
                .setIssuer("rest");
            newClaims.put("admin", tokenReq.getAdmin());

            String jwt = Jwts.builder()
                .setClaims(newClaims)
                .signWith(SignatureAlgorithm.RS256, jwtService.getSigningKey())
                .compact();

            return new TokenResponse(jwt);
        }
        return null;
    }
}
