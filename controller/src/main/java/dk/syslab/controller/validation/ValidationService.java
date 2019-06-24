package dk.syslab.controller.validation;

import io.jsonwebtoken.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class ValidationService {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final Pattern fileNamepattern      = Pattern.compile("^[a-zA-Z0-9_.\\s-]*$");
    private final Pattern directoryNamepattern = Pattern.compile("^[a-zA-Z0-9_\\\\/-]*$");
    private final Pattern namepattern          = Pattern.compile("^[a-zA-Z0-9-]*$");
    private final static List<String> BANNED_NAMES = Arrays.asList("all", "system", "web", "clear", "group", "website", "page", "admin", "test", "info", "controller", "coordinator");

    private JwtService jwtService;

    public ValidationService(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    public Claims validate(String token) {
        if (token == null || token.isEmpty()) {
            throw new ValidationException(ValidationStatus.SC_BAD_REQUEST, "No authentication token supplied! A authentication token must be supplied in the header. { Authorization: Bearer <token> }");
        }
        try {
            if (token.startsWith("Bearer")) token = token.replace("Bearer", "").trim();
            if (token.startsWith("bearer")) token = token.replace("bearer", "").trim();
            if (token.startsWith("BEARER")) token = token.replace("BEARER", "").trim();
            return Jwts.parser().setSigningKey(jwtService.getValidationKey()).parseClaimsJws(token).getBody();
        } catch (ExpiredJwtException e) {
            log.debug("Token was expired", e);
            throw new ValidationException(ValidationStatus.SC_UNAUTHORIZED, "Token has expired!");
        } catch (UnsupportedJwtException | MalformedJwtException | SignatureException e) {
            log.debug("Unable to validate token", e);
            throw new ValidationException(ValidationStatus.SC_UNAUTHORIZED, "Token was malformed or unsupported!");
        }
    }

    public boolean isAdmin(Claims claims) {
        if (claims.containsKey("admin")) {
            try {
                return (claims.get("admin", Boolean.class));
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }

    public boolean validateFilename(String name) {
        return fileNamepattern.matcher(name).matches();
    }
    public boolean validateDirectory(String name) {
        return directoryNamepattern.matcher(name).matches();
    }
    public boolean validateName(String name) {
        return namepattern.matcher(name).matches();
    }

    public boolean validateBannedName(String name) {
        // note: this is not meant to be a strict validation of names but simply to avoid stupid mistakes by using e.g. keywords for the api as names etc.
        // it is assumed that "authenticated" users will not be malicious on purpose ;)
        return name != null && !name.isEmpty() && !BANNED_NAMES.contains(name.toLowerCase());
    }
}
