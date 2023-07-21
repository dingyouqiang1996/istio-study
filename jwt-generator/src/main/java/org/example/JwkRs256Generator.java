package org.example;


import com.chilkatsoft.CkJsonObject;
import com.chilkatsoft.CkPrivateKey;
import com.chilkatsoft.CkPublicKey;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.Data;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

// vm options -Djava.library.path=/Users/fengzhikui/data/fzknotebook/fzk-custom-project/fzk-encode
public class JwkRs256Generator {
    static {
        try {
            System.loadLibrary("chilkat");
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
    static final String JWT_BODY_PATH = "jwk/JwtBody.json";
    static final String PUBLIC_KEY_PATH = "jwk/PublicKey";
    static final String PAIR_KEY_PATH = "jwk/PublicAndPrivateKeypair";

    static final String RESULT_PATH = "/src/main/resources/result/";//相对当前路径的存放路径

    static String kid = null;
    static String path = null;
    static String publicKeyPath = null;
    static String privatePkcs1Path = null;
    static String privatePkcs8Path = null;
    static String tokenPath = null;

    public static void main(String[] args) throws Exception {
        initPath();
        String publicKeyStr = FileUtil.read(PUBLIC_KEY_PATH);
        String publicKeyFromJwk = getPublicKeyFromJwk(publicKeyStr);

        String privateKeyStr = FileUtil.read(PAIR_KEY_PATH);
        String privateKeyFromJwk = getPrivateKeyFromJwk(privateKeyStr);

        FileUtil.write(publicKeyFromJwk, publicKeyPath);
        FileUtil.write(privateKeyFromJwk, privatePkcs1Path);
        //pkcs1ToPkcs8();
        String path="result/private-key-pkcs1.pem";
        PrivateKey privateKey = getPrivateKeyFromExist(path);
        String token = generateToken(privateKey);
        //FileUtil.write(token, tokenPath);

        path="result/public-key.pem";
        PublicKey publicKey = getPublicKeyFromExist(path);
        Jws<Claims> claimsJws = Jwts.parser().setSigningKey(publicKey).parseClaimsJws(token);
        System.out.println(claimsJws);
        FileUtil.write("\n" + claimsJws.toString(), tokenPath, true);
    }

    public static String generateToken(PrivateKey privateKey) throws Exception {
        String jwtBody = FileUtil.read(JWT_BODY_PATH);
        JwtContent jwt = new ObjectMapper().readValue(jwtBody, JwtContent.class);
        jwt.getHeader().put("kid", kid);

        String token = Jwts.builder()
                .setHeader(jwt.getHeader())
                .setClaims(jwt.getBody())
                .signWith(privateKey, SignatureAlgorithm.RS256)
                .compact();
        System.out.println(token);
        return token;
    }

    private static PrivateKey getPrivateKeyFromExist(String path) throws Exception {
        return getPrivateKey(FileUtil.read(path));
    }

    private static PrivateKey getPrivateKey(String privateKey) throws Exception {
        privateKey = privateKey.replaceAll("-*BEGIN.*KEY-*", "")
                .replaceAll("-*END.*KEY-*", "")
                .replaceAll("\\s+","");

        byte[] encodedKey = Base64.decodeBase64(privateKey);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encodedKey);

        KeyFactory kf = KeyFactory.getInstance("RSA");
        PrivateKey privKey = kf.generatePrivate(keySpec);
        return privKey;
    }


    private static PublicKey getPublicKeyFromExist(String path) throws Exception {
        String s = FileUtil.read(path);
        return getPublicKey(s);
    }

    private static PublicKey getPublicKey(String publicKeyBase64) throws Exception {
        String pem = publicKeyBase64
                .replaceAll("-*BEGIN.*KEY-*", "")
                .replaceAll("-*END.*KEY-*", "")
                .replaceAll("\\s+","");
        X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(Base64.decodeBase64(pem));
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");

        PublicKey publicKey = keyFactory.generatePublic(pubKeySpec);
        return publicKey;
    }

    static void pkcs1ToPkcs8() throws Exception {
        String cmd = "openssl pkcs8 -topk8 -inform PEM -in %s -outform pem -nocrypt -out %s";
        cmd = String.format(cmd, privatePkcs1Path, privatePkcs8Path);
        BufferedReader br = null;
        try {
            Process p = Runtime.getRuntime().exec(cmd);
            br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line = null;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }
            p.waitFor();
        } finally {
            if (br != null) { br.close(); }
        }
    }

    static void initPath() throws Exception{
        String absolutePath = FileUtil.getAbsolutePath(PUBLIC_KEY_PATH);
        String publicKeyStr = FileUtil.read(PUBLIC_KEY_PATH);
        PublicKeyJwk publicKeyJwk = new ObjectMapper().readValue(publicKeyStr, PublicKeyJwk.class);
        path = RESULT_PATH;
        File f = new File("");
        String absolute = f.getAbsolutePath();
        path=absolute+path;
        kid = publicKeyJwk.getKid();
        publicKeyPath = path + "public-key.pem";
        privatePkcs1Path = path + "private-key-pkcs1.pem";
        privatePkcs8Path = path + "private-key-pkcs8.pem";
        tokenPath = path + "token.txt";
    }


    static String getPublicKeyFromJwk(String value) throws Exception {
        PublicKeyJwk publicKeyJwk = new ObjectMapper().readValue(value, PublicKeyJwk.class);
        CkJsonObject json = new CkJsonObject();
        json.UpdateString("kty",publicKeyJwk.getKty());
        json.UpdateString("n",publicKeyJwk.getN());
        json.UpdateString("e",publicKeyJwk.getE());
        json.UpdateString("kid", publicKeyJwk.getKid());
        json.put_EmitCompact(false);

        String jwkStr = json.emit();
        CkPublicKey pubKey = new CkPublicKey();
        boolean success = pubKey.LoadFromString(jwkStr);
        if (!success) {
            System.out.println(pubKey.lastErrorText());
            throw new Exception(pubKey.lastErrorText());
        }
        boolean bPreferPkcs1 = false;
        String pem = pubKey.getPem(bPreferPkcs1);
        System.out.println(pem);

        return pem;
    }

    static String getPrivateKeyFromJwk(String value) throws Exception{
        KeyPairJwk jwk = new ObjectMapper().readValue(value, KeyPairJwk.class);
        CkJsonObject json = new CkJsonObject();
        json.UpdateString("kty",jwk.getKty());
        json.UpdateString("n",jwk.getN());
        json.UpdateString("e",jwk.getE());
        json.UpdateString("d",jwk.getD());
        json.UpdateString("p",jwk.getP());
        json.UpdateString("q",jwk.getQ());
        json.UpdateString("dp",jwk.getDp());
        json.UpdateString("dq",jwk.getDq());
        json.UpdateString("qi",jwk.getQi());
        json.put_EmitCompact(false);

        String jwkStr = json.emit();

        CkPrivateKey privKey = new CkPrivateKey();
        boolean success = privKey.LoadJwk(jwkStr);
        if (!success) {
            System.out.println("load error: \n" + privKey.lastErrorText());
            throw new Exception(privKey.lastErrorText());
        }
        String secret = privKey.getRsaPem();
        System.out.println(secret);
        return secret;
    }

    static class FileUtil {
        static String read(String filename) throws Exception {
            if (filename.startsWith("/")) {
                File file = new File(filename);
                return IOUtils.toString(new FileInputStream(file));
            } else {
                URL url = JwkRs256Generator.class.getClassLoader().getResource(filename);
                File file = new File(url.getFile());
                return IOUtils.toString(new FileInputStream(file));
            }
        }
        static void write(String value, String filename) throws Exception {
            File file = new File(filename);
            FileUtils.touch(file);
            IOUtils.write(value, new FileOutputStream(file));
        }
        static void write(String value, String filename, boolean append) throws Exception {
            File file = new File(filename);
            FileUtils.touch(file);
            FileUtils.write(file, value,"UTF-8", append);
        }
        static String getAbsolutePath(String path) {
            ClassLoader classLoader = JwkRs256Generator.class.getClassLoader();
            URL url = classLoader.getResource(path);
            File file = new File(url.getFile());
            return file.getAbsolutePath();
        }
    }

    @Data@JsonIgnoreProperties(ignoreUnknown = true)
    private static class KeyPairJwk {
        String p;
        String kty;
        String q;
        String d;
        String e;
        String kid;
        String qi;
        String dp;
        String dq;
        String n;
    }
    @Data@JsonIgnoreProperties(ignoreUnknown = true)
    private static class PublicKeyJwk {
        String kty;
        String e;
        String kid;
        String n;
    }
    @Data@JsonIgnoreProperties(ignoreUnknown = true)
    private static class JwtContent {
        Map<String, Object> header;
        Map<String, Object> body;
    }
}

