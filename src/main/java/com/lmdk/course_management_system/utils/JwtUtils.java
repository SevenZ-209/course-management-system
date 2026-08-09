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

    public String generateToken(
            String email,
            String role
    ) throws Exception {

        JWSSigner signer = new MACSigner(secret);

        Date now = new Date();

        Date expirationDate =
                new Date(
                        now.getTime() + expirationMs
                );

        JWTClaimsSet claimsSet =
                new JWTClaimsSet.Builder()
                        .subject(email)
                        .claim("role", role)
                        .issueTime(now)
                        .expirationTime(expirationDate)
                        .build();

        SignedJWT signedJWT =
                new SignedJWT(
                        new JWSHeader(
                                JWSAlgorithm.HS256
                        ),
                        claimsSet
                );

        signedJWT.sign(signer);

        return signedJWT.serialize();
    }

    public String validateTokenAndGetEmail(
            String token
    ) throws Exception {

        SignedJWT signedJWT =
                SignedJWT.parse(token);

        JWSVerifier verifier =
                new MACVerifier(secret);

        // Kiểm tra chữ ký
        boolean validSignature =
                signedJWT.verify(verifier);

        if (!validSignature) {
            return null;
        }

        JWTClaimsSet claims =
                signedJWT.getJWTClaimsSet();

        Date expiration =
                claims.getExpirationTime();

        // Token không có expiration
        if (expiration == null) {
            return null;
        }

        // Token đã hết hạn
        if (expiration.before(new Date())) {
            return null;
        }

        // Lấy email từ subject
        return claims.getSubject();
    }

    /**
     * Lấy role từ token.
     *
     * Ví dụ:
     * HOC_VIEN
     * GIAO_VIEN
     * QUAN_LY
     * ADMIN
     * PHU_HUYNH
     *
     * @param token JWT token
     * @return role
     */
    public String getRoleFromToken(
            String token
    ) throws Exception {

        SignedJWT signedJWT =
                SignedJWT.parse(token);

        return signedJWT
                .getJWTClaimsSet()
                .getStringClaim("role");
    }

    /**
     * Lấy email từ token.
     * Hàm này chỉ đọc dữ liệu,
     * không thực hiện validate token.
     */
    public String getEmailFromToken(
            String token
    ) throws Exception {

        SignedJWT signedJWT =
                SignedJWT.parse(token);

        return signedJWT
                .getJWTClaimsSet()
                .getSubject();
    }

    /**
     * Kiểm tra token còn hợp lệ hay không.
     */
    public boolean isTokenValid(
            String token
    ) {

        try {

            String email =
                    validateTokenAndGetEmail(token);

            return email != null
                    && !email.isBlank();

        } catch (Exception e) {
            return false;
        }
    }
}