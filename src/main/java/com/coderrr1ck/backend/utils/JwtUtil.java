package com.coderrr1ck.backend.utils;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;

@Service
public class JwtUtil {

    @Value("${application.security.jwt.accessExpiration:900000}")
    private long jwtExpiration;

    @Value("${application.security.jwt.refreshExpiration:604800000}")
    private long refreshExpiration;

    @Value("${application.security.jwt.secret-key:mySecretKeyForDevOnlyChangeThisInProdAndUse256BitKey12345678901234567890}")
    private String secretKey;

    public String generateRefreshToken(String username){
        return generateToken(username,refreshExpiration);
    }

    public String generateAccessToken(String username){
        return generateToken(username,jwtExpiration);
    }

    public String generateToken(String username,long expiration) {
        return Jwts.builder()
                .subject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(SignatureAlgorithm.HS256, getSecretKey())
                .compact();
    }
    public Key getSecretKey() {
//        use it when using base64 encoded secret key
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String validateAndExtractUsername(String jwtToken) {
        try {
            return Jwts.parser()
                    .setSigningKey(getSecretKey())
                    .build()
                    .parseClaimsJws(jwtToken)
                    .getBody()
                    .getSubject();
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
}
