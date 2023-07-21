<h2 style="color:red;align:center;" align=“center”>istio业务权限控制，原来可以这么玩</h2>

# 1用jwt获取认证信息

## 1.1jwt介绍

 jwt 是 JSON web token ，为了在网络应用环境中传递声明而执行的一种基于json的开放标准。非常适用于分布式的单点登录场景。主要是用来校验身份提供者和服务提供者之间传递的用户身份信息。 

https://jwt.io/

## 1.2istio如何用jwt

### 1.2.1写死



```
cat << EOF > ra-productpage-jwtrules-audiences.yaml
apiVersion: "security.istio.io/v1beta1"
kind: "RequestAuthentication"
metadata:
  name: "productpage"
spec:
  selector:
    matchLabels:
      app: productpage
  jwtRules:
  - issuer: "testing@secure.istio.io"
    outputPayloadToHeader: auth
    jwks: |
      { "keys":
         [
           {
             "e":"AQAB",
             "kid":"DHFbpoIUqrY8t2zpA2qXfCmr5VO5ZEr4RzHU_-envvQ",
             "kty":"RSA",
             "n":"xAE7eB6qugXyCAG3yhh7pkDkT65pHymX-P7KfIupjf59vsdo91bSP9C8H07pSAGQO1MV_xFj9VswgsCg4R6otmg5PV2He95lZdHtOcU5DXIg_pbhLdKXbi66GlVeK6ABZOUW3WYtnNHD-91gVuoeJT_DwtGGcp4ignkgXfkiEm4sw-4sfb4qdt5oLbyVpmW6x9cfa7vs2WTfURiCrBoUqgBo_-4WTiULmmHSGZHOjzwa8WtrtOQGsAFjIbno85jp6MnGGGZPYZbDAa_b3y5u-YpW7ypZrvD8BgtKVjgtQgZhLAGezMt0ua3DRrWnKqTZ0BJ_EyxOGuHJrLsn00fnMQ"
           }
         ]
      }
EOF

kubectl apply -f ra-productpage-jwtrules-audiences.yaml -n istio
```

ap-productpage.yaml

kubectl apply -f ap-productpage.yaml -n istio

```
apiVersion: security.istio.io/v1beta1
kind: AuthorizationPolicy
metadata:
  name: productpage
spec:
  selector:
    matchLabels:
      app: productpage
  action: DENY
  rules:
  - to:
    - operation:
        paths: ["/productpage"]
    from:
    - source:
        notRequestPrincipals:
        - "*"
```

创建RequestAuthentication，实现jwt

测试

```
TOKEN=eyJhbGciOiJSUzI1NiIsImtpZCI6IkRIRmJwb0lVcXJZOHQyenBBMnFYZkNtcjVWTzVaRXI0UnpIVV8tZW52dlEiLCJ0eXAiOiJKV1QifQ.eyJleHAiOjM1MzczOTExMDQsImdyb3VwcyI6WyJncm91cDEiLCJncm91cDIiXSwiaWF0IjoxNTM3MzkxMTA0LCJpc3MiOiJ0ZXN0aW5nQHNlY3VyZS5pc3Rpby5pbyIsInNjb3BlIjpbInNjb3BlMSIsInNjb3BlMiJdLCJzdWIiOiJ0ZXN0aW5nQHNlY3VyZS5pc3Rpby5pbyJ9.EdJnEZSH6X8hcyEii7c8H5lnhgjB5dwo07M5oheC8Xz8mOllyg--AHCFWHybM48reunF--oGaG6IXVngCEpVF0_P5DwsUoBgpPmK1JOaKN6_pe9sh0ZwTtdgK_RP01PuI7kUdbOTlkuUi2AO-qUyOm7Art2POzo36DLQlUXv8Ad7NBOqfQaKjE9ndaPWT7aexUsBHxmgiGbz1SyLH879f7uHYPbPKlpHU6P9S-DaKnGLaEchnoKnov7ajhrEhGXAQRukhDPKUHO9L30oPIr5IJllEQfHYtt6IZvlNUGeLUcif3wpry1R5tBXRicx2sXMQ7LyuDremDbcNy_iE76Upg

curl 192.168.229.134:30986/productpage -H "Authorization: Bearer ${TOKEN}"
```



### 1.2.2生成jwt

代码生成方式

https://blog.csdn.net/badboy_fzk/article/details/107650945



```
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


```

```
-----BEGIN PUBLIC KEY-----
MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAqggabdCirBa84zr9G1gn
4Ha2IJe6hDi4oP2FXOuSznGzXt40vXlHlJGAxlUNasA2dI3QjFtqj79Kxo2uh8+V
cNs2Xs5NVv98rDRiE6ZfItBTzIfmhZAZANrLn2TEXWy4iQVBt1OhlR4uWEzMaQRA
abOEEFCm+T8WGyrz8qJZFu4rJ5BWSMETVEw1gey+EqV/tJznoTju9t/LEFyLO/PM
dUADRgMOCN0pckb8rMLcaV0a1twW1EEkQC15pRI2TwusOm+ooVwekDrTqmILqJdh
vHzZiTOc7raVBYizjD8Qjdr/D4lAPLrCosYUkORpfAkVBpZ+MQ8Se4UPy6GnfBoH
VQIDAQAB
-----END PUBLIC KEY-----

-----BEGIN RSA PRIVATE KEY-----
MIIEpAIBAAKCAQEAqggabdCirBa84zr9G1gn4Ha2IJe6hDi4oP2FXOuSznGzXt40
vXlHlJGAxlUNasA2dI3QjFtqj79Kxo2uh8+VcNs2Xs5NVv98rDRiE6ZfItBTzIfm
hZAZANrLn2TEXWy4iQVBt1OhlR4uWEzMaQRAabOEEFCm+T8WGyrz8qJZFu4rJ5BW
SMETVEw1gey+EqV/tJznoTju9t/LEFyLO/PMdUADRgMOCN0pckb8rMLcaV0a1twW
1EEkQC15pRI2TwusOm+ooVwekDrTqmILqJdhvHzZiTOc7raVBYizjD8Qjdr/D4lA
PLrCosYUkORpfAkVBpZ+MQ8Se4UPy6GnfBoHVQIDAQABAoIBAD4bC5xE8sYs3/2X
1E+imZ4xfkDVavlUxot3TrksCsmBWmUygM0ceXrY0tKeynHNl5ZnHl/GHFw5IRT5
N2C///Pi/i8O58z5MQ6ta42F3YYX1iQFF7x9b8SSJoIoClmdlqHl26Uc1Q5kocT6
+Eb7mJODUJ+s9fBgjLCLteNQTX5EElwLyQoh4C4OA7sZKffhoOrMp7mQiAN5/FAm
TX5b5MznUfZTyxPx3wKAgpFhjTBwgmE2p7rcfVrbVfqzkqSU9Pu328nnbs4i+Zks
szTyOIskTalsUUsZKg6oZ+eGDV19ImHteU39mZ1Gpm6dX+9Kg1CbxNVy3EK4ckKY
/dw3m8ECgYEA8PQnaPh5r9jKIZpJHraJ3lZTB/AmB4AmS7HwxR49z5VbBq4TDE9d
L0gg7m1ezDZGa/8thGpT5claLXUeFZYTl+rRIEzFZzOa29LBsXvHy/ZGV7l7pQDJ
yEXLCh3qlrbVsL99flCfBbSJHIVI6UGJ5isPBJCgCf3v533+eyGOGM0CgYEAtKYz
bCuQ9kXn/yVg4PKJxeU71lewLc+NKUdAJR35Xu7Hlup6jAao553jLvkrvbQxftl3
rcDpacL7lbMSCTx4al8YeWHhHpvv/FURNzrZEM7otUvxmmHINoR31+42eskyx4gm
VJF0Lx1rJmb7ReqFJNtpIXMtwiBoCdWyvT+aSKkCgYBUx2MycPLH53w/VHzTvKBH
xZHTYjDPRMy0p3gysxBGgKOVdxwmoaoE39kOaxcGvTYL+631QEAiwrTi96lNKQ/d
zPYl6j0usZ6UJ9SU8Rt8bTuU1CNoDB3zRHNLLPmEa/JGDSpHjnBVXo87pL3hrHir
rozLK2PUADAwAEnQ4x6PJQKBgQCxt2OVQdLNhH40UrhY9CG3rslMF13541pyxmaD
XVaLHwr5G9nNUWvu2DO8bWKrsM6UhSoopES8SBaq8cyHi5bGxqDUIQePzkasjZ9v
HxSWZYcne/vGLfYCNBWWNwIfjznGc9mJjsKm6yETWXJPGOU9GiD1yFK2zJytOC8r
4JtOaQKBgQCEU4hhmek2Hc20Qtfvy2SyicsbxtF0qryoZOW7renTDJkRyvKjidio
XI1cLR6AY9itJ9pkXuB2W3reXSV1KvZf8Jg2lFO3zLcaEH92gVf62Kwu2TxFlWeR
6ThdpsDhBa78HLwtj3XvvPNB1LjCDBZGwR/sDJJ+pp/sOuXzg8gb5Q==
-----END RSA PRIVATE KEY-----

eyJraWQiOiJjSWNBZlEzQjhrajJ1Y0pUREFCUzdqVHFWUTVmandLUVY3d2hQTkxGQ1c0IiwidHlwIjoiSldUIiwiYWxnIjoiUlMyNTYifQ.eyJzdWIiOiJ0ZXN0aW5nQHNlY3VyZS5pc3Rpby5pbyIsInNjb3BlIjpbInNjb3BlMSIsInNjb3BlMiJdLCJpc3MiOiJ0ZXN0aW5nQHNlY3VyZS5pc3Rpby5pbyIsImV4cCI6MTY1OTgzNzk5OSwiaWF0IjoxNjU2MzAwNjA4LCJncm91cCI6WyJncm91cDEiLCJncm91cDIiXX0.UnSqkJVq7uTtk-B4s0QysbMGZ2YF3ObdZZ6Uintp8OIn5drLiMqsI2gkOufL_bwcPCWDSTvMN8A9K6vZn0qRfx70DeniiDi6-qzjUdSSq0-oE8hvE62iIb6m3ZL2U9aGuGM_eXwD6WrmzZTwcT4-rkSn1oqYNl21y0dwZ39jCIw6qtgmtqw6C3CC0SWELI9b8zvrrXsAaw0EfzZwIjuLgxCLJG1yZFUJW7s8vhjmSIkd9mcqTA6clIhRvdo5ATUrPNDXV5oAes-6qxkLAfN_HNRiZWLuzAGrVagAKiNXGyNxW33xGqDiVNb459aAVS_Dv3Zr9w19EmLdpVlmOpn4Qg

header={kid=cIcAfQ3B8kj2ucJTDABS7jTqVQ5fjwKQV7whPNLFCW4, typ=JWT, alg=RS256},body={sub=testing@secure.istio.io, scope=[scope1, scope2], iss=testing@secure.istio.io, exp=1659837999, iat=1656300608, group=[group1, group2]},signature=UnSqkJVq7uTtk-B4s0QysbMGZ2YF3ObdZZ6Uintp8OIn5drLiMqsI2gkOufL_bwcPCWDSTvMN8A9K6vZn0qRfx70DeniiDi6-qzjUdSSq0-oE8hvE62iIb6m3ZL2U9aGuGM_eXwD6WrmzZTwcT4-rkSn1oqYNl21y0dwZ39jCIw6qtgmtqw6C3CC0SWELI9b8zvrrXsAaw0EfzZwIjuLgxCLJG1yZFUJW7s8vhjmSIkd9mcqTA6clIhRvdo5ATUrPNDXV5oAes-6qxkLAfN_HNRiZWLuzAGrVagAKiNXGyNxW33xGqDiVNb459aAVS_Dv3Zr9w19EmLdpVlmOpn4Qg

```



### 1.2.3jwk的生成方式

在线生成方式：

http://mkjwk.org/

命令生成方式

https://github.com/mitreid-connect/json-web-key-generator

```
java -jar json-web-key-generator-jdk8.jar --type RSA --size 2048 --id DHFbpoIUqrY8t2zpA2qXfCmr5VO5ZEr4RzHU_-envvQ  -u sig -a RS256
```



### 1.2.4url的方式



```
cat << EOF > ra-productpage-jwtrules-jwksUri.yaml
apiVersion: "security.istio.io/v1beta1"
kind: "RequestAuthentication"
metadata:
  name: "productpage"
spec:
  selector:
    matchLabels:
      app: productpage
  jwtRules:
  - issuer: "testing@secure.istio.io"
    jwksUri: http://jwt-server.istio.svc.cluster.local:8000
    outputPayloadToHeader: auth
EOF

kubectl apply -f ra-productpage-jwtrules-jwksUri.yaml -n istio
```

创建RequestAuthentication，实现jwt



```
cat << EOF > jwt-server.yaml
apiVersion: v1
kind: Service
metadata:
  name: jwt-server
  labels:
    app: jwt-server
spec:
  ports:
  - name: http
    port: 8000
    targetPort: 8000
  selector:
    app: jwt-server
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: jwt-server
spec:
  replicas: 1
  selector:
    matchLabels:
      app: jwt-server
  template:
    metadata:
      labels:
        app: jwt-server
    spec:
      containers:
      - image: docker.io/istio/jwt-server:0.5
        imagePullPolicy: IfNotPresent
        name: jwt-server
        ports:
        - containerPort: 8000
---
EOF

kubectl apply -f jwt-server.yaml -n istio
```

创建jwt服务器

测试

```
TOKEN=eyJraWQiOiJjSWNBZlEzQjhrajJ1Y0pUREFCUzdqVHFWUTVmandLUVY3d2hQTkxGQ1c0IiwidHlwIjoiSldUIiwiYWxnIjoiUlMyNTYifQ.eyJmb28iOiJiYXIiLCJzdWIiOiJ0ZXN0aW5nQHNlY3VyZS5pc3Rpby5pbyIsInNjb3BlIjpbInNjb3BlMSIsInNjb3BlMiJdLCJpc3MiOiJ0ZXN0aW5nQHNlY3VyZS5pc3Rpby5pbyIsImV4cCI6MTY1OTgzNzk5OSwiaWF0IjoxNjU2MzAwNjA4LCJncm91cCI6WyJncm91cDEiLCJncm91cDIiXX0.d4UKCY6csS9NzPyJEUyd07Iq4fulzDPXWvETNVj5lxWKEiiqqE6Ty_9GpT1BCfORfHnl97NsmQ8uDs-vXGI60Gsby5YO3Y1D9B5w_8Q8SyQftr_-yV96QXDJEOnznhiw4POwERNYI9ExmR-LN7woCKc76ApjRtvDh0dT7yzAdebM9KfUHu9p3E6hc8YVhN9U9CxWTQyDGYjCwR5pjbw5rMD-EBSfdZPrPdd1GQQ_zf73WTHaOVpFW3s1m2dRBYcZ8r6PWbTHb1IFKgmliREpcTyFr-0hy0pbF22FR5yr6NiCCMefs36pTbjfv9M3deQvSKgSQ1YlLRy1JXylgZAuHA

curl 192.168.198.154:30986/productpage -H "Authorization: Bearer ${TOKEN}"
```



# 2基于AuthorizationPolicy Custom Action实现

先验证jwt

```
cat << EOF > ra-ingressgateway-jwtrules-ap.yaml
apiVersion: "security.istio.io/v1beta1"
kind: "RequestAuthentication"
metadata:
  name: ingressgateway-ra
spec:
  selector:
    matchLabels:
      app: istio-ingressgateway
  jwtRules:
  - issuer: "testing@secure.istio.io"
    outputPayloadToHeader: auth
    jwks: |
      { "keys":
         [
           {
             "e":"AQAB",
             "kid":"DHFbpoIUqrY8t2zpA2qXfCmr5VO5ZEr4RzHU_-envvQ",
             "kty":"RSA",
             "n":"xAE7eB6qugXyCAG3yhh7pkDkT65pHymX-P7KfIupjf59vsdo91bSP9C8H07pSAGQO1MV_xFj9VswgsCg4R6otmg5PV2He95lZdHtOcU5DXIg_pbhLdKXbi66GlVeK6ABZOUW3WYtnNHD-91gVuoeJT_DwtGGcp4ignkgXfkiEm4sw-4sfb4qdt5oLbyVpmW6x9cfa7vs2WTfURiCrBoUqgBo_-4WTiULmmHSGZHOjzwa8WtrtOQGsAFjIbno85jp6MnGGGZPYZbDAa_b3y5u-YpW7ypZrvD8BgtKVjgtQgZhLAGezMt0ua3DRrWnKqTZ0BJ_EyxOGuHJrLsn00fnMQ"
           }
         ]
      }
EOF

kubectl apply -f ra-ingressgateway-jwtrules-ap.yaml -n istio-system
```

1创建opa策略

opa介绍

http://blog.newbmiao.com/2020/03/13/opa-quick-start.html

https://www.openpolicyagent.org/docs/latest/

验证opa

https://play.openpolicyagent.org/p/ZXkIlAEPCY

```
cat << EOF > policy.rego 
package envoy.authz

import input.attributes.request.http as http_request

default allow = false

token = {"payload": payload} {
    [_, encoded] := split(http_request.headers.authorization, " ")
    [_, payload, _] := io.jwt.decode(encoded)
}

allow {
    action_allowed
}


bar := "bar"

action_allowed {
  bar ==token.payload.foo
}

EOF
```

2创建secret

  kubectl create secret generic opa-policy --from-file policy.rego  -n istio

3创建opa

```
cat << EOF > opa-deployment.yaml
apiVersion: v1
kind: Service
metadata:
  name: opa
  labels:
    app: opa
spec:
  ports:
  - name: grpc
    port: 9191
    targetPort: 9191
  selector:
    app: opa
---
kind: Deployment
apiVersion: apps/v1
metadata:
  name: opa
  labels:
    app: opa
spec:
  replicas: 1
  selector:
    matchLabels:
      app: opa
  template:
    metadata:
      labels:
        app: opa
    spec:
      containers:
        - name: opa
          image: openpolicyagent/opa:latest-envoy
          securityContext:
            runAsUser: 1111
          volumeMounts:
          - readOnly: true
            mountPath: /policy
            name: opa-policy
          args:
          - "run"
          - "--server"
          - "--addr=localhost:8181"
          - "--diagnostic-addr=0.0.0.0:8282"
          - "--set=plugins.envoy_ext_authz_grpc.addr=:9191"
          - "--set=plugins.envoy_ext_authz_grpc.query=data.envoy.authz.allow"
          - "--set=decision_logs.console=true"
          - "--ignore=.*"
          - "/policy/policy.rego"
          ports:
          - containerPort: 9191
          livenessProbe:
            httpGet:
              path: /health?plugins
              scheme: HTTP
              port: 8282
            initialDelaySeconds: 5
            periodSeconds: 5
          readinessProbe:
            httpGet:
              path: /health?plugins
              scheme: HTTP
              port: 8282
            initialDelaySeconds: 5
            periodSeconds: 5
      volumes:
        - name: opa-policy
          secret:
            secretName: opa-policy
EOF
 
kubectl apply -f opa-deployment.yaml -n istio
```

4编辑meshconfig

 kubectl edit configmap istio -n istio-system 

```
  mesh: |-
    # Add the following contents:
    extensionProviders:
    - name: "opa.istio"
      envoyExtAuthzGrpc:
        service: "opa.istio.svc.cluster.local"
        port: "9191"
```

5创建ap

```
cat << EOF >ext-authz.yaml
apiVersion: security.istio.io/v1beta1
kind: AuthorizationPolicy
metadata:
 name: ext-authz
 namespace: istio-system
spec:
 selector:
   matchLabels:
     app: istio-ingressgateway
 action: CUSTOM
 provider:
   name: "opa.istio"
 rules:
 - to:
   - operation:
       paths: ["/productpage"]
EOF

kubectl apply -f ext-authz.yaml -n istio-system
```

测试：

```
TOKEN=eyJhbGciOiJSUzI1NiIsImtpZCI6IkRIRmJwb0lVcXJZOHQyenBBMnFYZkNtcjVWTzVaRXI0UnpIVV8tZW52dlEiLCJ0eXAiOiJKV1QifQ.eyJleHAiOjQ2ODU5ODk3MDAsImZvbyI6ImJhciIsImlhdCI6MTUzMjM4OTcwMCwiaXNzIjoidGVzdGluZ0BzZWN1cmUuaXN0aW8uaW8iLCJzdWIiOiJ0ZXN0aW5nQHNlY3VyZS5pc3Rpby5pbyJ9.CfNnxWP2tcnR9q0vxyxweaF3ovQYHYZl82hAUsn21bwQd9zP7c-LS9qd_vpdLG4Tn1A15NxfCjp5f7QNBUo-KC9PJqYpgGbaXhaGx7bEdFWjcwv3nZzvc7M__ZpaCERdwU7igUmJqYGBYQ51vr2njU9ZimyKkfDe3axcyiBZde7G6dabliUosJvvKOPcKIWPccCgefSj_GNfwIip3-SsFdlR7BtbVUcqR-yv-XOxJ3Uc1MI0tz3uMiiZcyPV7sNCU4KRnemRIMHVOfuvHsU60_GhGbiSFzgPTAa9WTltbnarTbxudb_YEOx12JiwYToeX0DCPb43W1tzIBxgm8NxUg

curl 192.168.229.128:30555/productpage -H "Authorization: Bearer ${TOKEN}"
```

```
kubectl delete -f ext-authz.yaml -n istio-system
kubectl delete -f opa-deployment.yaml -n istio
kubectl delete secret opa-policy   -n istio
kubectl delete requestauthentications ingressgateway-ra -n istio-system
```



# 3基于LuaFilter实现

## 3.1先进行jwt认证

```
cat << EOF > ra-productpage-jwtrules-audiences.yaml
apiVersion: "security.istio.io/v1beta1"
kind: "RequestAuthentication"
metadata:
  name: "productpage"
spec:
  selector:
    matchLabels:
      app: productpage
  jwtRules:
  - issuer: "testing@secure.istio.io"
    outputPayloadToHeader: auth
    jwks: |
      { "keys":
         [
           {
             "e":"AQAB",
             "kid":"DHFbpoIUqrY8t2zpA2qXfCmr5VO5ZEr4RzHU_-envvQ",
             "kty":"RSA",
             "n":"xAE7eB6qugXyCAG3yhh7pkDkT65pHymX-P7KfIupjf59vsdo91bSP9C8H07pSAGQO1MV_xFj9VswgsCg4R6otmg5PV2He95lZdHtOcU5DXIg_pbhLdKXbi66GlVeK6ABZOUW3WYtnNHD-91gVuoeJT_DwtGGcp4ignkgXfkiEm4sw-4sfb4qdt5oLbyVpmW6x9cfa7vs2WTfURiCrBoUqgBo_-4WTiULmmHSGZHOjzwa8WtrtOQGsAFjIbno85jp6MnGGGZPYZbDAa_b3y5u-YpW7ypZrvD8BgtKVjgtQgZhLAGezMt0ua3DRrWnKqTZ0BJ_EyxOGuHJrLsn00fnMQ"
           }
         ]
      }
EOF

kubectl apply -f ra-productpage-jwtrules-audiences.yaml -n istio
```



## 3.2进行权限认证

auth服务器代码

```
package com.mark;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import javax.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.RequestHeader;


@SpringBootApplication
@RestController
public class SpringBootDemoHelloworldApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringBootDemoHelloworldApplication.class, args);
	}


	@GetMapping("/auth")
	public String auth(@RequestHeader("userId") String userId) {
		//String userId=request.getHeader("UserId");
		if ("admin".equals(userId)){
			return "ok";
		}
		return "fail";
	}
}

```

构建auth镜像

```
docker  build  . --tag registry.cn-hangzhou.aliyuncs.com/hxpdocker/auth-simple:1.5

docker push registry.cn-hangzhou.aliyuncs.com/hxpdocker/auth-simple:1.5
```

```
[root@master01 auth-simple]# cat Dockerfile 
FROM java:8

ADD  ./auth-simple.jar /app.jar

ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]
```



部署auth-simple

```
cat << EOF > k8s.yaml 
apiVersion: v1
kind: Service
metadata:
  name: auth-simple
  labels:
    app: auth-simple
spec:
  ports:
    - port: 8080
      name: http
      protocol: TCP
  selector:
    app: auth-simple
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: auth-simple
  labels:
    app: auth-simple
    version: v1
spec:
  replicas: 1
  selector:
    matchLabels:
      app: auth-simple
      version: v1
  template:
    metadata:
      labels:
        app: auth-simple
        version: v1
    spec:
      containers:
        - name: auth-simple
          image: registry.cn-hangzhou.aliyuncs.com/hxpdocker/auth-simple:1.5
          imagePullPolicy: IfNotPresent
          ports:
            - containerPort: 8080
---
EOF

kubectl apply -f k8s.yaml -n istio
```

安装cjson lua库

Dockerfile

```
FROM    docker.io/istio/proxyv2:1.14.1

RUN     apt update && apt install -y \
            luarocks

RUN     luarocks install lua-cjson 
```

 docker push registry.cn-qingdao.aliyuncs.com/hxpdocker/proxyv2:1.14.1-custom

**修改cm istio-sidecar-injector hub和tag**

报错

```
2022-06-28T03:15:39.736999Z     warning envoy config    gRPC config for type.googleapis.com/envoy.config.listener.v3.Listener rejected: Error adding/updating listener(s) virtualInbound: script load error: error loading module 'cjson' from file '/usr/local/lib/lua/5.1/cjson.so':
        /usr/local/lib/lua/5.1/cjson.so: undefined symbol: lua_checkstack

```

https://github.com/envoyproxy/envoy/issues/20859



```
cat << EOF > ef-http-filter-lua.yaml
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: apply-to
spec:
  workloadSelector:
    labels:
      app: productpage
  configPatches:
  - applyTo: HTTP_FILTER
    match:
      context: SIDECAR_INBOUND
      listener:
        portNumber: 9080
        filterChain:
          filter:
            name: "envoy.filters.network.http_connection_manager"
            subFilter:
              name: "envoy.filters.http.router"
    patch:
      operation: INSERT_BEFORE
      value: 
       name: envoy.filters.http.lua
       typed_config:
          "@type": "type.googleapis.com/envoy.extensions.filters.http.lua.v3.Lua"
          inlineCode: |
                   -- local cjson = require "cjson"
                   
                   local function decodeBase64(str64)
                        local b64chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/'
                        local temp={}
                        for i=1,64 do
                            temp[string.sub(b64chars,i,i)] = i
                        end
                        temp['=']=0
                        local str=""
                        for i=1,#str64,4 do
                            if i>#str64 then
                                break
                            end
                            local data = 0
                            local str_count=0
                            for j=0,3 do
                                local str1=string.sub(str64,i+j,i+j)
                                if not temp[str1] then
                                    return
                                end
                                if temp[str1] < 1 then
                                    data = data * 64
                                else
                                    data = data * 64 + temp[str1]-1
                                    str_count = str_count + 1
                                end
                            end
                            for j=16,0,-8 do
                                if str_count > 0 then
                                    str=str..string.char(math.floor(data/math.pow(2,j)))
                                    data=math.fmod(data,math.pow(2,j))
                                    str_count = str_count - 1
                                end
                            end
                        end

                        local last = tonumber(string.byte(str, string.len(str), string.len(str)))
                        if last == 0 then
                            str = string.sub(str, 1, string.len(str) - 1)
                        end
                        return str
                    end

                   function envoy_on_request(handle)
                      handle:logWarn(" ============= envoy_on_request ============= ")
                      local headers = handle:headers()
                      local authToken = headers:get("auth")
                      handle:logWarn(authToken)
                     -- local decoded=decodeBase64(authToken)
                     -- handle:logWarn(decoded)
                     -- local data = cjson.decode(decoded)
                     -- local userId=data["userId"]
                      local headers, body = handle:httpCall(
                          "outbound|8080||auth-simple.istio.svc.cluster.local",
                          {
                            [":method"] = "GET",
                            [":path"] = "/auth",
                            [":authority"] = "auth-simple:8080",
                            ["userId"] = "aaa"
                          },
                          "",
                          8080)
                          if(body=="fail")
                          then
                              handle:respond(
                                {[":status"] = "403",
                                 ["upstream_foo"] = headers["foo"]},
                                "nope")
                          end
                         
                      handle:logWarn(" ============================================= ")
                    end
            
EOF

kubectl apply -f ef-http-filter-lua.yaml -n istio
```

从jwt传过来的auth都里获取用户信息，传到认证服务器进行认证

测试：

```
TOKEN=eyJhbGciOiJSUzI1NiIsImtpZCI6IkRIRmJwb0lVcXJZOHQyenBBMnFYZkNtcjVWTzVaRXI0UnpIVV8tZW52dlEiLCJ0eXAiOiJKV1QifQ.eyJleHAiOjM1MzczOTExMDQsImdyb3VwcyI6WyJncm91cDEiLCJncm91cDIiXSwiaWF0IjoxNTM3MzkxMTA0LCJpc3MiOiJ0ZXN0aW5nQHNlY3VyZS5pc3Rpby5pbyIsInNjb3BlIjpbInNjb3BlMSIsInNjb3BlMiJdLCJzdWIiOiJ0ZXN0aW5nQHNlY3VyZS5pc3Rpby5pbyJ9.EdJnEZSH6X8hcyEii7c8H5lnhgjB5dwo07M5oheC8Xz8mOllyg--AHCFWHybM48reunF--oGaG6IXVngCEpVF0_P5DwsUoBgpPmK1JOaKN6_pe9sh0ZwTtdgK_RP01PuI7kUdbOTlkuUi2AO-qUyOm7Art2POzo36DLQlUXv8Ad7NBOqfQaKjE9ndaPWT7aexUsBHxmgiGbz1SyLH879f7uHYPbPKlpHU6P9S-DaKnGLaEchnoKnov7ajhrEhGXAQRukhDPKUHO9L30oPIr5IJllEQfHYtt6IZvlNUGeLUcif3wpry1R5tBXRicx2sXMQ7LyuDremDbcNy_iE76Upg

curl 192.168.229.128:30555/productpage -H "Authorization: Bearer ${TOKEN}"
```

```
kubectl delete envoyfilter apply-to -n istio
kubectl delete -f k8s.yaml -n istio
kubectl delete -f ra-productpage-jwtrules-audiences.yaml -n istio
```



# 4基于wasm实现

## 4.1什么实wasm

 WASM 的诞生源自前端，是一种为了解决日益复杂的前端 web 应用以及有限的 JavaScript 性能而诞生的技术。它本身并不是一种语言，而是一种字节码标准，一个“编译目标”。WASM 字节码和机器码非常接近，因此可以非常快速的装载运行。任何一种语言，都可以被编译成 WASM 字节码，然后在 WASM 虚拟机中执行（本身是为 web 设计，必然天然跨平台，同时为了沙箱运行保障安全，所以直接编译成机器码并不是最佳选择）。理论上，所有语言，包括 JavaScript、C、C++、Rust、Go、Java 等都可以编译成 WASM 字节码并在 WASM 虚拟机中执行。 

istio中的wasm，是一种扩展机制，主要用来扩展envoy的功能，以wasm filter的方式运行在envoy中。

## 4.2怎么用wasm实现权限控制

## 4.2.1先进行jwt认证

```
cat << EOF > ra-productpage-jwtrules-my-generated.yaml
apiVersion: "security.istio.io/v1beta1"
kind: "RequestAuthentication"
metadata:
  name: "productpage"
spec:
  selector:
    matchLabels:
      app: productpage
  jwtRules:
  - issuer: "testing@secure.istio.io"
    outputPayloadToHeader: auth
    jwks: |
      { "keys":
         [
            {
                "kty": "RSA",
                "e": "AQAB",
                "use": "sig",
                "kid": "cIcAfQ3B8kj2ucJTDABS7jTqVQ5fjwKQV7whPNLFCW4",
                "alg": "RS256",
                "n": "qggabdCirBa84zr9G1gn4Ha2IJe6hDi4oP2FXOuSznGzXt40vXlHlJGAxlUNasA2dI3QjFtqj79Kxo2uh8-VcNs2Xs5NVv98rDRiE6ZfItBTzIfmhZAZANrLn2TEXWy4iQVBt1OhlR4uWEzMaQRAabOEEFCm-T8WGyrz8qJZFu4rJ5BWSMETVEw1gey-EqV_tJznoTju9t_LEFyLO_PMdUADRgMOCN0pckb8rMLcaV0a1twW1EEkQC15pRI2TwusOm-ooVwekDrTqmILqJdhvHzZiTOc7raVBYizjD8Qjdr_D4lAPLrCosYUkORpfAkVBpZ-MQ8Se4UPy6GnfBoHVQ"
            }
         ]
      }
EOF

kubectl apply -f ra-productpage-jwtrules-my-generated.yaml -n istio
```



## 4.2.2进行权限认证

### go wasm代码

main.go

```
package main

import (
        "encoding/base64"
        "github.com/tidwall/gjson"
        "github.com/tetratelabs/proxy-wasm-go-sdk/proxywasm"
        "github.com/tetratelabs/proxy-wasm-go-sdk/proxywasm/types"
)


func main() {
        proxywasm.SetVMContext(&vmContext{})
}

type vmContext struct {
        // Embed the default VM context here,
        // so that we don't need to reimplement all the methods.
        types.DefaultVMContext
}

// Override types.DefaultVMContext.
func (*vmContext) NewPluginContext(contextID uint32) types.PluginContext {
        return &pluginContext{}
}

type pluginContext struct {
        // Embed the default plugin context here,
        // so that we don't need to reimplement all the methods.
        types.DefaultPluginContext
}

// Override types.DefaultPluginContext.
func (*pluginContext) NewHttpContext(contextID uint32) types.HttpContext {
        return &httpAuth{contextID: contextID}
}

type httpAuth struct {
        // Embed the default http context here,
        // so that we don't need to reimplement all the methods.
        types.DefaultHttpContext
        contextID uint32
}

// Override types.DefaultHttpContext.
func (ctx *httpAuth) OnHttpRequestHeaders(numHeaders int, endOfStream bool) types.Action {
        auth, err := proxywasm.GetHttpRequestHeader("auth")
        if err != nil {
                proxywasm.LogCriticalf("failed to get request header: %v", err)
                return types.ActionContinue
        }
        decoded, err := base64.RawStdEncoding.DecodeString(auth)
        if err != nil {
                proxywasm.LogCriticalf("failed to decodestring: %v", err)
                return types.ActionContinue
        }
        
        if !gjson.ValidBytes(decoded) {
                proxywasm.LogCriticalf("json format is not right")
        }
        jsonData := gjson.ParseBytes(decoded)
        userId := jsonData.Get("userId").String()
        
        hs := [][2]string{
              {":method", "GET"}, {":authority", "auth-simple:8080"},{":path", "/auth"}, {"accept", "*/*"},
              {"userId",userId},
        }
        for _, h := range hs {
                proxywasm.LogInfof("request header: %s: %s", h[0], h[1])
        }

        if _, err := proxywasm.DispatchHttpCall("outbound|8080||auth-simple.istio.svc.cluster.local", hs, nil, nil,
                8080, httpCallResponseCallback); err != nil {
                proxywasm.LogCriticalf("dipatch httpcall failed: %v", err)
                return types.ActionContinue
        }

        proxywasm.LogInfof("http call dispatched to %s", "outbound|8080||auth-simple.istio.svc.cluster.local")
        return types.ActionPause
}

func httpCallResponseCallback(numHeaders, bodySize, numTrailers int) {
        hs, err := proxywasm.GetHttpCallResponseHeaders()
        if err != nil {
                proxywasm.LogCriticalf("failed to get response body: %v", err)
                return
        }

        for _, h := range hs {
                proxywasm.LogInfof("response header from %s: %s: %s", "outbound|8080||auth-simple.istio.svc.cluster.local", h[0], h[1])
        }

        b, err := proxywasm.GetHttpCallResponseBody(0, bodySize)
        if err != nil {
                proxywasm.LogCriticalf("failed to get response body: %v", err)
                proxywasm.ResumeHttpRequest()
                return
        }

        ret:=string(b)

        if ret=="ok" {
                proxywasm.LogInfo("access granted")
                proxywasm.ResumeHttpRequest()
                return
        }

        body := "access forbidden"
        proxywasm.LogInfo(body)
        if err := proxywasm.SendHttpResponse(403, [][2]string{
                {"powered-by", "proxy-wasm-go-sdk!!"},
        }, []byte(body), -1); err != nil {
                proxywasm.LogErrorf("failed to send local response: %v", err)
                proxywasm.ResumeHttpRequest()
        }
}
```

编译

```
export GOPROXY=https://proxy.golang.com.cn,direct

tinygo build -o main.wasm -scheduler=none -target=wasi main.go

```

部署envoyfilter：

kubectl create cm wasm --from-file=main.wasm -n istio



productpage-deploy-wasm-auth.yaml

kubectl apply -f productpage-deploy-wasm-auth.yaml -n istio

```
apiVersion: apps/v1
kind: Deployment
metadata:
  name: productpage-v1
  labels:
    app: productpage
    version: v1
spec:
  replicas: 1
  selector:
    matchLabels:
      app: productpage
      version: v1
  template:
    metadata:
      labels:
        app: productpage
        version: v1
      annotations:
        sidecar.istio.io/userVolumeMount: '[{"name":"wasm", "mountPath":"/var/local/lib/wasm-filters", "readonly":true}]'
        sidecar.istio.io/userVolume: '[{"name":"wasm", "configmap":{"name":"wasm"}}]'
    spec:
      serviceAccountName: bookinfo-productpage
      containers:
      - name: productpage
        image: docker.io/istio/examples-bookinfo-productpage-v1:1.16.4
        imagePullPolicy: IfNotPresent
        ports:
        - containerPort: 9080
        volumeMounts:
        - name: tmp
          mountPath: /tmp
        securityContext:
          runAsUser: 1000
      volumes:
      - name: tmp
        emptyDir: {}
```

ef-wasm-auth.yaml

kubectl apply -f ef-wasm-auth.yaml -n istio

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: wasm
  namespace: istio
spec:
  workloadSelector:
    labels:
      app: productpage
      version: v1
  configPatches:
  - applyTo: HTTP_FILTER
    match:
      context: SIDECAR_INBOUND
      listener:
        filterChain:
          destinationPort: 9080
          filter:
            name: "envoy.filters.network.http_connection_manager"
            subFilter:
              name: "envoy.filters.http.router"
    patch:
      operation: INSERT_BEFORE
      value:
        name: envoy.filters.http.wasm
        typed_config:
                '@type': type.googleapis.com/envoy.extensions.filters.http.wasm.v3.Wasm
                config:
                  name: my_plugin
                  configuration:
                    "@type": type.googleapis.com/google.protobuf.StringValue
                    value: |
                      {}
                  vm_config:
                    runtime: "envoy.wasm.runtime.v8"
                    code:
                      local:
                        filename: /var/local/lib/wasm-filters/main.wasm

```



### auth服务器代码

```
package com.mark;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import javax.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.RequestHeader;


@SpringBootApplication
@RestController
public class SpringBootDemoHelloworldApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringBootDemoHelloworldApplication.class, args);
	}


	@GetMapping("/auth")
	public String auth(@RequestHeader("userId") String userId) {
		//String userId=request.getHeader("UserId");
		if ("admin".equals(userId)){
			return "ok";
		}
		return "fail";
	}
}

```

构建auth镜像

```
docker  build  . --tag registry.cn-hangzhou.aliyuncs.com/hxpdocker/auth-simple:1.1

docker push registry.cn-hangzhou.aliyuncs.com/hxpdocker/auth-simple:1.1
```

```
[root@master01 auth-simple]# cat Dockerfile 
FROM java:8

ADD  ./auth-simple.jar /app.jar

ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]
```



部署auth-simple

```
cat << EOF > k8s.yaml 
apiVersion: v1
kind: Service
metadata:
  name: auth-simple
  labels:
    app: auth-simple
spec:
  ports:
    - port: 8080
      name: http
      protocol: TCP
  selector:
    app: auth-simple
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: auth-simple
  labels:
    app: auth-simple
    version: v1
spec:
  replicas: 1
  selector:
    matchLabels:
      app: auth-simple
      version: v1
  template:
    metadata:
      labels:
        app: auth-simple
        version: v1
    spec:
      containers:
        - name: auth-simple
          image: registry.cn-hangzhou.aliyuncs.com/hxpdocker/auth-simple:1.5
          imagePullPolicy: IfNotPresent
          ports:
            - containerPort: 8080
---
EOF

kubectl apply -f k8s.yaml -n istio
```

测试：

```
TOKEN=eyJraWQiOiJjSWNBZlEzQjhrajJ1Y0pUREFCUzdqVHFWUTVmandLUVY3d2hQTkxGQ1c0IiwidHlwIjoiSldUIiwiYWxnIjoiUlMyNTYifQ.eyJmb28iOiJiYXIiLCJzdWIiOiJ0ZXN0aW5nQHNlY3VyZS5pc3Rpby5pbyIsInVzZXJJZCI6ImFkbWluIiwic2NvcGUiOlsic2NvcGUxIiwic2NvcGUyIl0sImlzcyI6InRlc3RpbmdAc2VjdXJlLmlzdGlvLmlvIiwiZXhwIjoxNjU5ODM3OTk5LCJpYXQiOjE2NTYzMDA2MDgsImdyb3VwIjpbImdyb3VwMSIsImdyb3VwMiJdfQ.no0reSCOc3SAEHeGVcPoI9wvwkvQ1EcJZnqGv7NPsdevqrY6QrKzIV_kfk6SDJmd6umgtMeoS5rYtZleSwrsdYR6x4Z0ya5plPoIbUdvFsXDf-VKyYU0_031l5WELm9uMKv_ZtVtTYaupEwLLOFmlm8cRPa8xpbyYt6u8Gkn74MatVR2nlDpRYQzGttpD8mJvXkDs-yiuE6bFJzhe2x4U8g8ADf3MZB4Wy-E67d0P3EvjrKoyys5wT9oTyLFlkSv_cxQ37cmo94gHHhfqy9KmJVTPLx4zOhrdSexHRRJWYYOQShpVWjpb5ocqCPSBldHyW1rJOEbO5VBIxxVo5rawg

curl 192.168.229.128:30555/productpage -H "Authorization: Bearer ${TOKEN}"

TOKEN=eyJraWQiOiJjSWNBZlEzQjhrajJ1Y0pUREFCUzdqVHFWUTVmandLUVY3d2hQTkxGQ1c0IiwidHlwIjoiSldUIiwiYWxnIjoiUlMyNTYifQ.eyJmb28iOiJiYXIiLCJzdWIiOiJ0ZXN0aW5nQHNlY3VyZS5pc3Rpby5pbyIsInVzZXJJZCI6Im1hcmsiLCJzY29wZSI6WyJzY29wZTEiLCJzY29wZTIiXSwiaXNzIjoidGVzdGluZ0BzZWN1cmUuaXN0aW8uaW8iLCJleHAiOjE2NTk4Mzc5OTksImlhdCI6MTY1NjMwMDYwOCwiZ3JvdXAiOlsiZ3JvdXAxIiwiZ3JvdXAyIl19.Dfp7uF_GO5h2tvjx74PpzT3Im46jUSmmJjPiAzDr9Z-VYJ1XR5KjIUj8jcB9kDA2rQA-K8IcvO8RE03j2VLg1BCbrOioYR1K-VU95HAjdWudM1MvCQjaMUkrgnPwHYHwd_tUhCRHRGSlyWpGfvn7nnAy4QX838UuyXVI91axpatvCaLjut7W1Y8-Fxwk8XIgVaB2KbyShbc2nfJMDU_cKx4SdXSJKesb1_EcpcdbsCERPDSoU3pNBtsWgyxbPg9E_HaUkiw0ROcgX01VZOONCFSCrtVR6rZombCCvlZ88M1oNWxUWMVsAriOK0zB_tmZM40yGkK78RxlPcdErLG3mg


curl 192.168.229.128:30555/productpage -H "Authorization: Bearer ${TOKEN}"
```

```
kubectl delete -f k8s.yaml -n istio
kubectl delete EnvoyFilter wasm -n istio
kubectl delete RequestAuthentication productpage -n istio
kubectl delete cm wasm  -n istio
```

