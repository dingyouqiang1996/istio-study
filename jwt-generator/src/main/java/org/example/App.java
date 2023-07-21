package org.example;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import sun.security.rsa.RSAPrivateKeyImpl;
import sun.security.rsa.RSAPublicKeyImpl;

import java.math.BigInteger;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPrivateCrtKeySpec;
import java.util.*;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        try {
            Map<String, Object> headerClaims = new HashMap();
            headerClaims.put("kid","DHFbpoIUqrY8t2zpA2qXfCmr5VO5ZEr4RzHU_-envvQ");
            Map<String, Object> payload = new HashMap();
            ArrayList<String> array=new ArrayList<String>();
            array.add("group1");
            array.add("group2");
            payload.put("group",array);
            Map<String, Object> payload2 = new HashMap();
            ArrayList<String> array2=new ArrayList<String>();
            array2.add("scope1");
            array2.add("scope2");
            payload.put("scope",array2);
            KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
            keyPairGen.initialize(2048,new SecureRandom());
            KeyPair keypair = keyPairGen.generateKeyPair();
            RSAPublicKey publicKey = (RSAPublicKey)keypair.getPublic();
            RSAPrivateKey privateKey = (RSAPrivateKey)keypair.getPrivate();
            //System.out.println(Base64.getEncoder().encode((publicKey.getEncoded())));


            //Algorithm algorithm = Algorithm.HMAC256("secret");
            Algorithm algorithm =Algorithm.RSA256(publicKey, privateKey);
            String token = JWT.create()
                    .withIssuer("testing@secure.istio.io")
                    .withSubject("testing@secure.istio.io")
                    .withPayload(payload)
                    .withPayload(payload2)
                    .withHeader(headerClaims)
                    .withExpiresAt(new Date(new Date().getTime()+ 3537391104L))
                    .withIssuedAt(new Date())
                    .sign(algorithm);
            System.out.println( token);
        } catch (JWTCreationException | NoSuchAlgorithmException   exception){
            //Invalid Signing configuration / Couldn't convert Claims.
        }

    }
}
