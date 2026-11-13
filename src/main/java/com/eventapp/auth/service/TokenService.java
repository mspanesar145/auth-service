package com.eventapp.auth.service;
import com.nimbusds.jose.*; import com.nimbusds.jose.crypto.*; import com.nimbusds.jwt.*; import org.springframework.beans.factory.annotation.Value; import org.springframework.stereotype.Service;
import java.security.*; import java.security.spec.*; import java.time.*; import java.util.*; import java.util.Base64;
@Service public class TokenService {
 private final PrivateKey privateKey; private final PublicKey publicKey; private final String issuer; private final long accessSeconds;
 public TokenService(@Value("${security.jwt.private-key}") String privatePem,@Value("${security.jwt.public-key}") String publicPem,@Value("${security.jwt.issuer}") String issuer,@Value("${security.jwt.access-token-seconds:900}") long seconds) throws Exception {
  this.privateKey=KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(decodePem(privatePem,"PRIVATE KEY")));
  this.publicKey=KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(decodePem(publicPem,"PUBLIC KEY")));this.issuer=issuer;this.accessSeconds=seconds;
 }
 private byte[] decodePem(String pem,String type){return Base64.getDecoder().decode(pem.replace("-----BEGIN "+type+"-----","").replace("-----END "+type+"-----","").replaceAll("\\s",""));}
 public String createAccessToken(UUID userId){try{Instant now=Instant.now();JWTClaimsSet claims=new JWTClaimsSet.Builder().issuer(issuer).subject(userId.toString()).issueTime(Date.from(now)).expirationTime(Date.from(now.plusSeconds(accessSeconds))).claim("token_type","access").build();SignedJWT jwt=new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.RS256).keyID("auth-key-1").type(JOSEObjectType.JWT).build(),claims);jwt.sign(new RSASSASigner(privateKey));return jwt.serialize();}catch(Exception e){throw new IllegalStateException("Unable to create access token",e);}}
 public boolean verify(String token){try{SignedJWT jwt=SignedJWT.parse(token);return jwt.verify(new RSASSAVerifier((java.security.interfaces.RSAPublicKey)publicKey))&&jwt.getJWTClaimsSet().getExpirationTime().after(new Date())&&issuer.equals(jwt.getJWTClaimsSet().getIssuer());}catch(Exception e){return false;}}
 public long getAccessSeconds(){return accessSeconds;} public PublicKey getPublicKey(){return publicKey;}
}
