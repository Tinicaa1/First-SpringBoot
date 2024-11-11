package net.javaguides.ems.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service

public class JwtService {

    private static final String SECRET_KEY = "133b0d1733efea43612a9491b72d16a62633ee5ef8b8b88361e8b1fe6f58abf28e8274953361387076d625f558af5ed6cf56f52826963cec130bb331d5637a14a8fc72331d69b6987221715b41d86626264a1511a8e4092bb1eed533d5f2fa77e91df321eddab0a2dca0d243a7d25ddd1c4d89fe0f990d71b4219ac9e5381aa9db045e203ec7f0fcc881b584a2e42555c409b71996eb88d978ba8f6c4092a438fd58798a7fe84abfe5598469968753367c82e9f453b1e05121a833c2ae162dbfac8ada08f4ba25f39fd0bad27575b34abccf6296263dd806528db84586d018cd8c3fca9d3ecd82b6c67c913f0a2ac56b7036ce4bdc0e5db685515feb0c1092b0V3pnNREb9QD8itHZ2keWrR3AtpyGeTcF";

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims,T> claimResolver) {
        final Claims claims = extractAllClaims(token);
        return claimResolver.apply(claims);
    }

    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    public String generateToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails
            )
    {
        return Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis()+1000*60*24))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public  boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSignInKey() {
        byte[] keyBytes= Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
