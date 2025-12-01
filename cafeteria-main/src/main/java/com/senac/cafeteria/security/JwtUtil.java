package com.senac.cafeteria.security;

<<<<<<< HEAD
=======


>>>>>>> 3c1f5f0f962c0975e55a991c26f36f654955c71a
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {
<<<<<<< HEAD

    private final Key key;
    private final long expirationMillis;

    public JwtUtil(@Value("${jwt.secret}") String secret,
                   @Value("${jwt.expiration}") long expirationMillis) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.expirationMillis = expirationMillis;
    }

    public String generateToken(String username) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMillis);

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    public String extractUsername(String token) {
        Claims claims = Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(token).getBody();
        return claims.getSubject();
    }
}
=======
 private final Key key; private final long expirationMillis;

public JwtUtil(@Value("${jwt.secret}") String secret,
@Value("${jwt.expiration}") long expirationMillis) {
    this.key = Keys.hmacShaKeyFor(secret.getBytes());
 this.expirationMillis = expirationMillis;
}


 public String generateToken(String username) {

     Date now = new Date();

      Date expiry = new Date(now.getTime() + expirationMillis);

      return Jwts.builder()

       .setSubject(username)

        .setIssuedAt(now)

         .setExpiration(expiry)

          .signWith(key, SignatureAlgorithm.HS256)

           .compact();

         }


          public boolean validateToken(String token) {

            try {

                Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);

                 return true;

                 } catch (JwtException | IllegalArgumentException ex) {

                     return false;

                    }

                 }


                  public String extractUsername(String token) {

                     Claims claims = Jwts.parserBuilder().setSigningKey(key).build()

                      .parseClaimsJws(token).getBody();

                      return claims.getSubject();

                    }
}



>>>>>>> 3c1f5f0f962c0975e55a991c26f36f654955c71a
