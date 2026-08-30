package com.lmdk.course_management_system.utils;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtUtils {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expirationMs;

    public String generateToken(String username, String role) throws Exception {
        JWSSigner signer = new MACSigner(secret);
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + expirationMs);

        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject(username)
                .claim("role", role)
                .issueTime(now)
                .expirationTime(expirationDate)
                .build();

        SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claimsSet);
        signedJWT.sign(signer);

        return signedJWT.serialize();
    }

    public String validateTokenAndGetUsername(String token) throws Exception {
        SignedJWT signedJWT = SignedJWT.parse(token);
        JWSVerifier verifier = new MACVerifier(secret);

        if(!signedJWT.verify(verifier))
            return null;

        JWTClaimsSet claims = signedJWT.getJWTClaimsSet();
        Date expiration = claims.getExpirationTime();

        if(expiration == null || expiration.before(new Date()))
            return null;

        String username = claims.getSubject();

        if(username == null || username.isBlank())
            return null;

        return username;
    }

    public String getUsernameFromToken(String token) throws Exception {
        return SignedJWT.parse(token)
                .getJWTClaimsSet()
                .getSubject();
    }

    public String getRoleFromToken(String token) throws Exception {
        return SignedJWT.parse(token)
                .getJWTClaimsSet()
                .getStringClaim("role");
    }

    public boolean isTokenValid(String token) {
        try {
            String username = validateTokenAndGetUsername(token);
            return username != null && !username.isBlank();
        } catch(Exception ex) {
            return false;
        }
    }
}