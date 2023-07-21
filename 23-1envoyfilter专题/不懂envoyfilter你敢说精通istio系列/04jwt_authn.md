# 1什么是jwt_authn

Json web token (JWT), 是为了在网络应用环境间传递声明而执行的一种基于JSON的开放标准[RFC 7519].该token被设计为紧凑且安全的，特别适用于分布式站点的单点登录（SSO）场景

jwt_authn实用来实现jwt功能http类型的filter，名称为envoy.filters.http.jwt_authn，type url为type.googleapis.com/envoy.extensions.filters.http.jwt_authn.v3.JwtAuthentication。jwt功能在istio中是通过RequestAuthorization实现的，但是功能不是全部，我们可以用envoyFilter实现同样的功能。

# 2配置详解

```
{
  "providers": "{...}",  jwt提供者，包含jwt相关信息
  "rules": [], 路由和provider之间的关系的规则
  "filter_state_rules": "{...}",若果rules没有匹配的就匹配这个
  "bypass_cors_preflight": "...",跳过cors预检请求jwt校验
  "requirement_map": "{...}" requirement配置的map，路由级别的filter可以用这个来配置
}
```



providers：

```
{
  "issuer": "...", 发布者
  "audiences": [], 观众
  "remote_jwks": "{...}", 远程jwk的url
  "local_jwks": "{...}",本地配置jwk
  "forward": "...",将令牌forward到上游
  "from_headers": [],token来自header
  "from_params": [],token来自params
  "from_cookies": [],token来自cookie
  "forward_payload_header": "...",将jwt的payload forward到上游
  "pad_forward_payload_header": "...",payload加padding
  "payload_in_metadata": "...",jwt payload转metadata
  "header_in_metadata": "...",jwt header转meatadata
  "clock_skew_seconds": "...",验证jwt时间约束，默认60s
  "jwt_cache_config": "{...}"缓存jwt
}
```

rules:

```
{
  "match": "{...}",匹配路由
  "requires": "{...}",需要的jwt
  "requirement_name": "...“ 名称
}
```

requires：

```
{
  "provider_name": "...",  provider的名称
  "provider_and_audiences": "{...}",带有audience的provider
  "requires_any": "{...}",匹配任意一个
  "requires_all": "{...}",匹配所有
  "allow_missing_or_failed": "{...}",是否允许不验证
  "allow_missing": "{...}"是否允许不验证
}
```



# 3实战

## 3.1案例1

RequestAuthorization实现

```
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
```

envoyfilter实现

```
cat << EOF > jwt-productpage.yaml
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: jwt
spec:
  workloadSelector:
    labels:
      app: productpage
  configPatches:
    - applyTo: HTTP_FILTER
      match:
        context: SIDECAR_INBOUND
        listener:
          filterChain:
            filter:
              name: "envoy.filters.network.http_connection_manager"
              subFilter:
                name: "envoy.filters.http.router"
      patch:
        operation: INSERT_BEFORE
        value:
          name: envoy.filters.http.jwt_authn
          typedConfig:
            '@type': type.googleapis.com/envoy.extensions.filters.http.jwt_authn.v3.JwtAuthentication
            providers:
              origins-0:
                forward: true
                issuer: testing@secure.istio.io
                localJwks:
                  inlineString: "{ \"keys\":\n   [ \n     {\n       \"e\":\"AQAB\",\n
                    \      \"kid\":\"DHFbpoIUqrY8t2zpA2qXfCmr5VO5ZEr4RzHU_-envvQ\",\n
                    \      \"kty\":\"RSA\",\n       \"n\":\"xAE7eB6qugXyCAG3yhh7pkDkT65pHymX-P7KfIupjf59vsdo91bSP9C8H07pSAGQO1MV_xFj9VswgsCg4R6otmg5PV2He95lZdHtOcU5DXIg_pbhLdKXbi66GlVeK6ABZOUW3WYtnNHD-91gVuoeJT_DwtGGcp4ignkgXfkiEm4sw-4sfb4qdt5oLbyVpmW6x9cfa7vs2WTfURiCrBoUqgBo_-4WTiULmmHSGZHOjzwa8WtrtOQGsAFjIbno85jp6MnGGGZPYZbDAa_b3y5u-YpW7ypZrvD8BgtKVjgtQgZhLAGezMt0ua3DRrWnKqTZ0BJ_EyxOGuHJrLsn00fnMQ\"\n
                    \    }\n   ]\n}\n"
                payloadInMetadata: testing@secure.istio.io
            rules:
            - match:
                prefix: /
              requires:
                requiresAny:
                  requirements:
                  - providerName: origins-0
                  - allowMissing: {}
EOF

kubectl apply -f jwt-productpage.yaml -n istio
```

访问：

TOKEN=eyJhbGciOiJSUzI1NiIsImtpZCI6IkRIRmJwb0lVcXJZOHQyenBBMnFYZkNtcjVWTzVaRXI0UnpIVV8tZW52dlEiLCJ0eXAiOiJKV1QifQ.eyJleHAiOjM1MzczOTExMDQsImdyb3VwcyI6WyJncm91cDEiLCJncm91cDIiXSwiaWF0IjoxNTM3MzkxMTA0LCJpc3MiOiJ0ZXN0aW5nQHNlY3VyZS5pc3Rpby5pbyIsInNjb3BlIjpbInNjb3BlMSIsInNjb3BlMiJdLCJzdWIiOiJ0ZXN0aW5nQHNlY3VyZS5pc3Rpby5pbyJ9.EdJnEZSH6X8hcyEii7c8H5lnhgjB5dwo07M5oheC8Xz8mOllyg--AHCFWHybM48reunF--oGaG6IXVngCEpVF0_P5DwsUoBgpPmK1JOaKN6_pe9sh0ZwTtdgK_RP01PuI7kUdbOTlkuUi2AO-qUyOm7Art2POzo36DLQlUXv8Ad7NBOqfQaKjE9ndaPWT7aexUsBHxmgiGbz1SyLH879f7uHYPbPKlpHU6P9S-DaKnGLaEchnoKnov7ajhrEhGXAQRukhDPKUHO9L30oPIr5IJllEQfHYtt6IZvlNUGeLUcif3wpry1R5tBXRicx2sXMQ7LyuDremDbcNy_iE76Upg

curl 192.168.229.128:30555/productpage -H "Authorization: Bearer ${TOKEN}"



## 3.2jwtRules-audiences

指定audiences

RequestAuthorization实现

```
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
    audiences:
    - "app"
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
```

envoyfilter实现

```
cat << EOF > jwt-productpage-audiences.yaml
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: jwt
spec:
  workloadSelector:
    labels:
      app: productpage
  configPatches:
    - applyTo: HTTP_FILTER
      match:
        context: SIDECAR_INBOUND
        listener:
          filterChain:
            filter:
              name: "envoy.filters.network.http_connection_manager"
              subFilter:
                name: "envoy.filters.http.router"
      patch:
        operation: INSERT_BEFORE
        value:
          name: envoy.filters.http.jwt_authn
          typedConfig:
            '@type': type.googleapis.com/envoy.extensions.filters.http.jwt_authn.v3.JwtAuthentication
            providers:
              origins-0:
                audiences:
                - app
                issuer: testing@secure.istio.io
                localJwks:
                  inlineString: |
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
                payloadInMetadata: testing@secure.istio.io
            rules:
            - match:
                prefix: /
              requires:
                requiresAny:
                  requirements:
                  - providerName: origins-0
                  - allowMissing: {}
EOF

kubectl apply -f jwt-productpage-audiences.yaml -n istio
```

访问：

TOKEN=eyJhbGciOiJSUzI1NiIsImtpZCI6IkRIRmJwb0lVcXJZOHQyenBBMnFYZkNtcjVWTzVaRXI0UnpIVV8tZW52dlEiLCJ0eXAiOiJKV1QifQ.eyJleHAiOjM1MzczOTExMDQsImdyb3VwcyI6WyJncm91cDEiLCJncm91cDIiXSwiaWF0IjoxNTM3MzkxMTA0LCJpc3MiOiJ0ZXN0aW5nQHNlY3VyZS5pc3Rpby5pbyIsInNjb3BlIjpbInNjb3BlMSIsInNjb3BlMiJdLCJzdWIiOiJ0ZXN0aW5nQHNlY3VyZS5pc3Rpby5pbyJ9.EdJnEZSH6X8hcyEii7c8H5lnhgjB5dwo07M5oheC8Xz8mOllyg--AHCFWHybM48reunF--oGaG6IXVngCEpVF0_P5DwsUoBgpPmK1JOaKN6_pe9sh0ZwTtdgK_RP01PuI7kUdbOTlkuUi2AO-qUyOm7Art2POzo36DLQlUXv8Ad7NBOqfQaKjE9ndaPWT7aexUsBHxmgiGbz1SyLH879f7uHYPbPKlpHU6P9S-DaKnGLaEchnoKnov7ajhrEhGXAQRukhDPKUHO9L30oPIr5IJllEQfHYtt6IZvlNUGeLUcif3wpry1R5tBXRicx2sXMQ7LyuDremDbcNy_iE76Upg

curl 192.168.229.128:30555/productpage -H "Authorization: Bearer ${TOKEN}"



TOKEN=eyJraWQiOiJjSWNBZlEzQjhrajJ1Y0pUREFCUzdqVHFWUTVmandLUVY3d2hQTkxGQ1c0IiwidHlwIjoiSldUIiwiYWxnIjoiUlMyNTYifQ.eyJmb28iOiJiYXIiLCJhdWQiOlsiYXBwIiwid2ViIl0sInN1YiI6InRlc3RpbmdAc2VjdXJlLmlzdGlvLmlvIiwidXNlcklkIjoibWFyayIsInNjb3BlIjpbInNjb3BlMSIsInNjb3BlMiJdLCJpc3MiOiJ0ZXN0aW5nQHNlY3VyZS5pc3Rpby5pbyIsImV4cCI6MTY1OTgzNzk5OSwiaWF0IjoxNjU2MzAwNjA4LCJncm91cCI6WyJncm91cDEiLCJncm91cDIiXX0.PuVoIsT96csmYpS7gC0XmZG5xmUWsTFtauELMysfYi980u7aIS_o9d0LNqRooscE_jxC2n4GopevrtrF_m0IyBBVC8ibvrWdlF4vJ0x2EA2oePoa6l6kfFm4FBcNOnydVoH_tnDtUoHxnmt62JcVlrqtv2tvPngy5vwZaLvh2iDiqtioCsSs3z9pI2_BuOJ0mEQmxhsTwXJsUbiQaJlgYQIDwKGf1WbeNOt4qWONj1oFCzXXMAoUE9wySkdPAc1ot9WFNGzwOUO8cr08UnqJXby3M5ROsEuXchOqgGJChaCwCfcbZH5IkHb5304-KZn69kyeFO8z0nWstdsjejxDyA



curl 192.168.229.128:30555/productpage -H "Authorization: Bearer ${TOKEN}"

## 3.2jwtRules-jwksUri

```
package org.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Hello world!
 *
 */
@SpringBootApplication
@RestController
public class App 
{
    public static void main( String[] args )
    {
        SpringApplication.run(App.class, args);
    }

    @RequestMapping("/")
    public String getJwks(){
        String jwks=
        "{ "+
           " \"kty\": \"RSA\","+
               " \"e\": \"AQAB\","+
               "\"use\": \"sig\","+
               " \"kid\": \"cIcAfQ3B8kj2ucJTDABS7jTqVQ5fjwKQV7whPNLFCW4\","+
                "\"alg\": \"RS256\","+
                "\"n\": \"qggabdCirBa84zr9G1gn4Ha2IJe6hDi4oP2FXOuSznGzXt40vXlHlJGAxlUNasA2dI3QjFtqj79Kxo2uh8-VcNs2Xs5NVv98rDRiE6ZfItBTzIfmhZAZANrLn2TEXWy4iQVBt1OhlR4uWEzMaQRAabOEEFCm-T8WGyrz8qJZFu4rJ5BWSMETVEw1gey-EqV_tJznoTju9t_LEFyLO_PMdUADRgMOCN0pckb8rMLcaV0a1twW1EEkQC15pRI2TwusOm-ooVwekDrTqmILqJdhvHzZiTOc7raVBYizjD8Qjdr_D4lAPLrCosYUkORpfAkVBpZ-MQ8Se4UPy6GnfBoHVQ\""+
        "}";
        return jwks;
    }
}

```

构建auth镜像

```
docker  build  . --tag registry.cn-hangzhou.aliyuncs.com/hxpdocker/jwt-server:1.0

docker push registry.cn-hangzhou.aliyuncs.com/hxpdocker/jwt-server:1.0
```

```
[root@master01 auth-simple]# cat Dockerfile 
FROM java:8

ADD  ./jwt-server.jar /app.jar

ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]
```

deploy-jwt-server.yaml

kubectl apply -f deploy-jwt-server.yaml -n istio

```
apiVersion: v1
kind: Service
metadata:
  name: jwt-server
  labels:
    app: jwt-server
spec:
  ports:
  - name: http
    port: 8080
    targetPort: 8080
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
      - image: registry.cn-hangzhou.aliyuncs.com/hxpdocker/jwt-server:1.0
        imagePullPolicy: IfNotPresent
        name: jwt-server
        ports:
        - containerPort: 8080
---
```



从jwksUri获取配置信息，生成后envoy配置直接配置获取的配置信息

RequestAuthorization实现

```
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
    jwksUri: http://jwt-server.istio.svc.cluster.local:8080
```

envoyfilter实现

```
cat << EOF > jwt-productpage-jwk-server.yaml
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: jwt
spec:
  workloadSelector:
    labels:
      app: productpage
  configPatches:
    - applyTo: HTTP_FILTER
      match:
        context: SIDECAR_INBOUND
        listener:
          filterChain:
            filter:
              name: "envoy.filters.network.http_connection_manager"
              subFilter:
                name: "envoy.filters.http.router"
      patch:
        operation: INSERT_BEFORE
        value:
          name: envoy.filters.http.jwt_authn
          typedConfig:
            '@type': type.googleapis.com/envoy.extensions.filters.http.jwt_authn.v3.JwtAuthentication
            providers:
              origins-0:
                issuer: testing@secure.istio.io
                localJwks:
                  inlineString: "{ \"keys\":\n   [ \n     {\n       \"e\":\"AQAB\",\n
                    \      \"kid\":\"DHFbpoIUqrY8t2zpA2qXfCmr5VO5ZEr4RzHU_-envvQ\",\n
                    \      \"kty\":\"RSA\",\n       \"n\":\"xAE7eB6qugXyCAG3yhh7pkDkT65pHymX-P7KfIupjf59vsdo91bSP9C8H07pSAGQO1MV_xFj9VswgsCg4R6otmg5PV2He95lZdHtOcU5DXIg_pbhLdKXbi66GlVeK6ABZOUW3WYtnNHD-91gVuoeJT_DwtGGcp4ignkgXfkiEm4sw-4sfb4qdt5oLbyVpmW6x9cfa7vs2WTfURiCrBoUqgBo_-4WTiULmmHSGZHOjzwa8WtrtOQGsAFjIbno85jp6MnGGGZPYZbDAa_b3y5u-YpW7ypZrvD8BgtKVjgtQgZhLAGezMt0ua3DRrWnKqTZ0BJ_EyxOGuHJrLsn00fnMQ\"\n
                    \    }\n   ]\n}\n"
                payloadInMetadata: testing@secure.istio.io
            rules:
            - match:
                prefix: /
              requires:
                requiresAny:
                  requirements:
                  - providerName: origins-0
                  - allowMissing: {}
EOF

kubectl apply -f jwt-productpage-jwk-server.yaml -n istio
```



```
TOKEN=eyJhbGciOiJSUzI1NiIsImtpZCI6IkRIRmJwb0lVcXJZOHQyenBBMnFYZkNtcjVWTzVaRXI0UnpIVV8tZW52dlEiLCJ0eXAiOiJKV1QifQ.eyJleHAiOjM1MzczOTExMDQsImdyb3VwcyI6WyJncm91cDEiLCJncm91cDIiXSwiaWF0IjoxNTM3MzkxMTA0LCJpc3MiOiJ0ZXN0aW5nQHNlY3VyZS5pc3Rpby5pbyIsInNjb3BlIjpbInNjb3BlMSIsInNjb3BlMiJdLCJzdWIiOiJ0ZXN0aW5nQHNlY3VyZS5pc3Rpby5pbyJ9.EdJnEZSH6X8hcyEii7c8H5lnhgjB5dwo07M5oheC8Xz8mOllyg--AHCFWHybM48reunF--oGaG6IXVngCEpVF0_P5DwsUoBgpPmK1JOaKN6_pe9sh0ZwTtdgK_RP01PuI7kUdbOTlkuUi2AO-qUyOm7Art2POzo36DLQlUXv8Ad7NBOqfQaKjE9ndaPWT7aexUsBHxmgiGbz1SyLH879f7uHYPbPKlpHU6P9S-DaKnGLaEchnoKnov7ajhrEhGXAQRukhDPKUHO9L30oPIr5IJllEQfHYtt6IZvlNUGeLUcif3wpry1R5tBXRicx2sXMQ7LyuDremDbcNy_iE76Upg

curl 192.168.229.128:30555/productpage -H "Authorization: Bearer ${TOKEN}"
```



## 3.3jwtRules-fromHeaders

从请求头获取token

RequestAuthorization实现

```
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
    fromHeaders:
    - name: my-token
      prefix: test
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
```

envoyfilter实现

```
cat << EOF > jwt-productpage-fromHeaders.yaml
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: jwt
spec:
  workloadSelector:
    labels:
      app: productpage
  configPatches:
    - applyTo: HTTP_FILTER
      match:
        context: SIDECAR_INBOUND
        listener:
          filterChain:
            filter:
              name: "envoy.filters.network.http_connection_manager"
              subFilter:
                name: "envoy.filters.http.router"
      patch:
        operation: INSERT_BEFORE
        value:
          name: envoy.filters.http.jwt_authn
          typedConfig:
            '@type': type.googleapis.com/envoy.extensions.filters.http.jwt_authn.v3.JwtAuthentication
            providers:
              origins-0:
                fromHeaders:
                - name: my-token
                  valuePrefix: test
                issuer: testing@secure.istio.io
                localJwks:
                  inlineString: |
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
                payloadInMetadata: testing@secure.istio.io
            rules:
            - match:
                prefix: /
              requires:
                requiresAny:
                  requirements:
                  - providerName: origins-0
                  - allowMissing: {}
EOF

kubectl apply -f jwt-productpage-fromHeaders.yaml -n istio
```

访问

TOKEN=eyJhbGciOiJSUzI1NiIsImtpZCI6IkRIRmJwb0lVcXJZOHQyenBBMnFYZkNtcjVWTzVaRXI0UnpIVV8tZW52dlEiLCJ0eXAiOiJKV1QifQ.eyJleHAiOjM1MzczOTExMDQsImdyb3VwcyI6WyJncm91cDEiLCJncm91cDIiXSwiaWF0IjoxNTM3MzkxMTA0LCJpc3MiOiJ0ZXN0aW5nQHNlY3VyZS5pc3Rpby5pbyIsInNjb3BlIjpbInNjb3BlMSIsInNjb3BlMiJdLCJzdWIiOiJ0ZXN0aW5nQHNlY3VyZS5pc3Rpby5pbyJ9.EdJnEZSH6X8hcyEii7c8H5lnhgjB5dwo07M5oheC8Xz8mOllyg--AHCFWHybM48reunF--oGaG6IXVngCEpVF0_P5DwsUoBgpPmK1JOaKN6_pe9sh0ZwTtdgK_RP01PuI7kUdbOTlkuUi2AO-qUyOm7Art2POzo36DLQlUXv8Ad7NBOqfQaKjE9ndaPWT7aexUsBHxmgiGbz1SyLH879f7uHYPbPKlpHU6P9S-DaKnGLaEchnoKnov7ajhrEhGXAQRukhDPKUHO9L30oPIr5IJllEQfHYtt6IZvlNUGeLUcif3wpry1R5tBXRicx2sXMQ7LyuDremDbcNy_iE76Upg

curl 192.168.229.128:30555/productpage -H "my-token: test ${TOKEN}"



## 3.4jwtRules-fromParams

从url参数获取token

RequestAuthorization实现

```
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
    fromParams:
    - my-token
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
```

envoyfilter实现

```
cat << EOF > jwt-productpage-fromParams.yaml
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: jwt
spec:
  workloadSelector:
    labels:
      app: productpage
  configPatches:
    - applyTo: HTTP_FILTER
      match:
        context: SIDECAR_INBOUND
        listener:
          filterChain:
            filter:
              name: "envoy.filters.network.http_connection_manager"
              subFilter:
                name: "envoy.filters.http.router"
      patch:
        operation: INSERT_BEFORE
        value:
          name: envoy.filters.http.jwt_authn
          typedConfig:
            '@type': type.googleapis.com/envoy.extensions.filters.http.jwt_authn.v3.JwtAuthentication
            providers:
              origins-0:
                fromParams:
                - my-token
                issuer: testing@secure.istio.io
                localJwks:
                  inlineString: |
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
                payloadInMetadata: testing@secure.istio.io
            rules:
            - match:
                prefix: /
              requires:
                requiresAny:
                  requirements:
                  - providerName: origins-0
                  - allowMissing: {}
EOF

kubectl apply -f jwt-productpage-fromParams.yaml -n istio
```

访问：

TOKEN=eyJhbGciOiJSUzI1NiIsImtpZCI6IkRIRmJwb0lVcXJZOHQyenBBMnFYZkNtcjVWTzVaRXI0UnpIVV8tZW52dlEiLCJ0eXAiOiJKV1QifQ.eyJleHAiOjM1MzczOTExMDQsImdyb3VwcyI6WyJncm91cDEiLCJncm91cDIiXSwiaWF0IjoxNTM3MzkxMTA0LCJpc3MiOiJ0ZXN0aW5nQHNlY3VyZS5pc3Rpby5pbyIsInNjb3BlIjpbInNjb3BlMSIsInNjb3BlMiJdLCJzdWIiOiJ0ZXN0aW5nQHNlY3VyZS5pc3Rpby5pbyJ9.EdJnEZSH6X8hcyEii7c8H5lnhgjB5dwo07M5oheC8Xz8mOllyg--AHCFWHybM48reunF--oGaG6IXVngCEpVF0_P5DwsUoBgpPmK1JOaKN6_pe9sh0ZwTtdgK_RP01PuI7kUdbOTlkuUi2AO-qUyOm7Art2POzo36DLQlUXv8Ad7NBOqfQaKjE9ndaPWT7aexUsBHxmgiGbz1SyLH879f7uHYPbPKlpHU6P9S-DaKnGLaEchnoKnov7ajhrEhGXAQRukhDPKUHO9L30oPIr5IJllEQfHYtt6IZvlNUGeLUcif3wpry1R5tBXRicx2sXMQ7LyuDremDbcNy_iE76Upg

curl 192.168.229.128:30555/productpage?my-token=${TOKEN}



## 3.5jwtRules-outputPayloadToHeader

把peyload传到上游

RequestAuthorization实现

```
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
    fromParams:
    - my-token
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
```

envoyfilter实现

```
cat << EOF > jwt-productpage-outputPayloadToHeader.yaml
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: jwt
spec:
  workloadSelector:
    labels:
      app: productpage
  configPatches:
    - applyTo: HTTP_FILTER
      match:
        context: SIDECAR_INBOUND
        listener:
          filterChain:
            filter:
              name: "envoy.filters.network.http_connection_manager"
              subFilter:
                name: "envoy.filters.http.router"
      patch:
        operation: INSERT_BEFORE
        value:
          name: envoy.filters.http.jwt_authn
          typedConfig:
            '@type': type.googleapis.com/envoy.extensions.filters.http.jwt_authn.v3.JwtAuthentication
            providers:
              origins-0:
                forwardPayloadHeader: auth
                fromParams:
                - my-token
                issuer: testing@secure.istio.io
                localJwks:
                  inlineString: |
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
                payloadInMetadata: testing@secure.istio.io
            rules:
            - match:
                prefix: /
              requires:
                requiresAny:
                  requirements:
                  - providerName: origins-0
                  - allowMissing: {}
EOF

kubectl apply -f jwt-productpage-outputPayloadToHeader.yaml -n istio
```



```
TOKEN=eyJhbGciOiJSUzI1NiIsImtpZCI6IkRIRmJwb0lVcXJZOHQyenBBMnFYZkNtcjVWTzVaRXI0UnpIVV8tZW52dlEiLCJ0eXAiOiJKV1QifQ.eyJleHAiOjM1MzczOTExMDQsImdyb3VwcyI6WyJncm91cDEiLCJncm91cDIiXSwiaWF0IjoxNTM3MzkxMTA0LCJpc3MiOiJ0ZXN0aW5nQHNlY3VyZS5pc3Rpby5pbyIsInNjb3BlIjpbInNjb3BlMSIsInNjb3BlMiJdLCJzdWIiOiJ0ZXN0aW5nQHNlY3VyZS5pc3Rpby5pbyJ9.EdJnEZSH6X8hcyEii7c8H5lnhgjB5dwo07M5oheC8Xz8mOllyg--AHCFWHybM48reunF--oGaG6IXVngCEpVF0_P5DwsUoBgpPmK1JOaKN6_pe9sh0ZwTtdgK_RP01PuI7kUdbOTlkuUi2AO-qUyOm7Art2POzo36DLQlUXv8Ad7NBOqfQaKjE9ndaPWT7aexUsBHxmgiGbz1SyLH879f7uHYPbPKlpHU6P9S-DaKnGLaEchnoKnov7ajhrEhGXAQRukhDPKUHO9L30oPIr5IJllEQfHYtt6IZvlNUGeLUcif3wpry1R5tBXRicx2sXMQ7LyuDremDbcNy_iE76Upg

curl 192.168.229.128:30555/productpage?my-token=${TOKEN}
```



## 3.6jwtRules-forwardOriginalToken

forward原始的token

RequestAuthorization实现

```
apiVersion: "security.istio.io/v1beta1"
kind: "RequestAuthentication"
metadata:
  name: "jwt-example"
  namespace: istio-system
spec:
  selector:
    matchLabels:
      istio: ingressgateway
  jwtRules:
  - issuer: "testing@secure.istio.io"
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
    forwardOriginalToken: true  # 转发 Authorization 请求头
```

envoyfilter实现

```
cat << EOF > jwt-ingressgateway-forwardOriginalToken.yaml
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: jwt
spec:
  workloadSelector:
    labels:
      istio: ingressgateway
  configPatches:
    - applyTo: HTTP_FILTER
      match:
        context: GATEWAY
        listener:
          filterChain:
            filter:
              name: "envoy.filters.network.http_connection_manager"
              subFilter:
                name: "envoy.filters.http.router"
      patch:
        operation: INSERT_BEFORE
        value:
          name: envoy.filters.http.jwt_authn
          typedConfig:
            '@type': type.googleapis.com/envoy.extensions.filters.http.jwt_authn.v3.JwtAuthentication
            providers:
              origins-0:
                forward: true
                issuer: testing@secure.istio.io
                localJwks:
                  inlineString: "{ \"keys\":\n   [ \n     {\n       \"e\":\"AQAB\",\n
                    \      \"kid\":\"DHFbpoIUqrY8t2zpA2qXfCmr5VO5ZEr4RzHU_-envvQ\",\n
                    \      \"kty\":\"RSA\",\n       \"n\":\"xAE7eB6qugXyCAG3yhh7pkDkT65pHymX-P7KfIupjf59vsdo91bSP9C8H07pSAGQO1MV_xFj9VswgsCg4R6otmg5PV2He95lZdHtOcU5DXIg_pbhLdKXbi66GlVeK6ABZOUW3WYtnNHD-91gVuoeJT_DwtGGcp4ignkgXfkiEm4sw-4sfb4qdt5oLbyVpmW6x9cfa7vs2WTfURiCrBoUqgBo_-4WTiULmmHSGZHOjzwa8WtrtOQGsAFjIbno85jp6MnGGGZPYZbDAa_b3y5u-YpW7ypZrvD8BgtKVjgtQgZhLAGezMt0ua3DRrWnKqTZ0BJ_EyxOGuHJrLsn00fnMQ\"\n
                    \    }\n   ]\n}\n"
                payloadInMetadata: testing@secure.istio.io
            rules:
            - match:
                prefix: /
              requires:
                providerName: origins-0
EOF

kubectl apply -f jwt-ingressgateway-forwardOriginalToken.yaml -n istio-system
```



```
cat << EOF > jwt-productpage-forwardOriginalToken.yaml
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: jwt
spec:
  workloadSelector:
    labels:
      app: productpage
  configPatches:
    - applyTo: HTTP_FILTER
      match:
        context: SIDECAR_INBOUND
        listener:
          filterChain:
            filter:
              name: "envoy.filters.network.http_connection_manager"
              subFilter:
                name: "envoy.filters.http.router"
      patch:
        operation: INSERT_BEFORE
        value:
          name: envoy.filters.http.jwt_authn
          typedConfig:
            '@type': type.googleapis.com/envoy.extensions.filters.http.jwt_authn.v3.JwtAuthentication
            providers:
              origins-0:
                issuer: testing@secure.istio.io
                localJwks:
                  inlineString: |
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
                payloadInMetadata: testing@secure.istio.io
            rules:
            - match:
                prefix: /
              requires:
                providerName: origins-0
EOF

kubectl apply -f jwt-productpage-forwardOriginalToken.yaml -n istio
```

```
TOKEN=eyJhbGciOiJSUzI1NiIsImtpZCI6IkRIRmJwb0lVcXJZOHQyenBBMnFYZkNtcjVWTzVaRXI0UnpIVV8tZW52dlEiLCJ0eXAiOiJKV1QifQ.eyJleHAiOjM1MzczOTExMDQsImdyb3VwcyI6WyJncm91cDEiLCJncm91cDIiXSwiaWF0IjoxNTM3MzkxMTA0LCJpc3MiOiJ0ZXN0aW5nQHNlY3VyZS5pc3Rpby5pbyIsInNjb3BlIjpbInNjb3BlMSIsInNjb3BlMiJdLCJzdWIiOiJ0ZXN0aW5nQHNlY3VyZS5pc3Rpby5pbyJ9.EdJnEZSH6X8hcyEii7c8H5lnhgjB5dwo07M5oheC8Xz8mOllyg--AHCFWHybM48reunF--oGaG6IXVngCEpVF0_P5DwsUoBgpPmK1JOaKN6_pe9sh0ZwTtdgK_RP01PuI7kUdbOTlkuUi2AO-qUyOm7Art2POzo36DLQlUXv8Ad7NBOqfQaKjE9ndaPWT7aexUsBHxmgiGbz1SyLH879f7uHYPbPKlpHU6P9S-DaKnGLaEchnoKnov7ajhrEhGXAQRukhDPKUHO9L30oPIr5IJllEQfHYtt6IZvlNUGeLUcif3wpry1R5tBXRicx2sXMQ7LyuDremDbcNy_iE76Upg

curl 192.168.229.128:30555/productpage -H "Authorization: Bearer ${TOKEN}"
```



## 3.7multi provider 

ef-jwt-multi-provider.yaml

kubectl apply -f ef-jwt-multi-provider.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: jwt
spec:
  workloadSelector:
    labels:
      istio: ingressgateway
  configPatches:
    - applyTo: HTTP_FILTER
      match:
        context: GATEWAY
        listener:
          filterChain:
            filter:
              name: "envoy.filters.network.http_connection_manager"
              subFilter:
                name: "envoy.filters.http.router"
      patch:
        operation: INSERT_BEFORE
        value:
          name: envoy.filters.http.jwt_authn
          typedConfig:
            '@type': type.googleapis.com/envoy.extensions.filters.http.jwt_authn.v3.JwtAuthentication
            providers:
              origins-0:
                forward: true
                issuer: testing@secure.istio.io
                localJwks:
                  inlineString: "{ \"keys\":\n   [ \n     {\n       \"e\":\"AQAB\",\n
                    \      \"kid\":\"DHFbpoIUqrY8t2zpA2qXfCmr5VO5ZEr4RzHU_-envvQ\",\n
                    \      \"kty\":\"RSA\",\n       \"n\":\"xAE7eB6qugXyCAG3yhh7pkDkT65pHymX-P7KfIupjf59vsdo91bSP9C8H07pSAGQO1MV_xFj9VswgsCg4R6otmg5PV2He95lZdHtOcU5DXIg_pbhLdKXbi66GlVeK6ABZOUW3WYtnNHD-91gVuoeJT_DwtGGcp4ignkgXfkiEm4sw-4sfb4qdt5oLbyVpmW6x9cfa7vs2WTfURiCrBoUqgBo_-4WTiULmmHSGZHOjzwa8WtrtOQGsAFjIbno85jp6MnGGGZPYZbDAa_b3y5u-YpW7ypZrvD8BgtKVjgtQgZhLAGezMt0ua3DRrWnKqTZ0BJ_EyxOGuHJrLsn00fnMQ\"\n
                    \    }\n   ]\n}\n"
                payloadInMetadata: testing@secure.istio.io
              origins-1:
                forward: true
                issuer: testing@secure.istio.io
                localJwks:
                  inlineString: "{\"keys\": [{
                      \      \"kty\": \"RSA\",
                      \      \"e\": \"AQAB\",
                      \      \"use\": \"sig\",
                      \      \"kid\": \"cIcAfQ3B8kj2ucJTDABS7jTqVQ5fjwKQV7whPNLFCW4\",
                      \      \"alg\": \"RS256\",
                      \      \"n\": \"qggabdCirBa84zr9G1gn4Ha2IJe6hDi4oP2FXOuSznGzXt40vXlHlJGAxlUNasA2dI3QjFtqj79Kxo2uh8-VcNs2Xs5NVv98rDRiE6ZfItBTzIfmhZAZANrLn2TEXWy4iQVBt1OhlR4uWEzMaQRAabOEEFCm-T8WGyrz8qJZFu4rJ5BWSMETVEw1gey-EqV_tJznoTju9t_LEFyLO_PMdUADRgMOCN0pckb8rMLcaV0a1twW1EEkQC15pRI2TwusOm-ooVwekDrTqmILqJdhvHzZiTOc7raVBYizjD8Qjdr_D4lAPLrCosYUkORpfAkVBpZ-MQ8Se4UPy6GnfBoHVQ\"
                       \ }]}"
                payloadInMetadata: testing@secure.istio.io
            rules:
            - match:
                prefix: /
              requires:
                requiresAny:
                  requirements:
                  - providerName: origins-0
                  - providerName: origins-1
                  - allowMissing: {}
```



```
TOKEN=eyJhbGciOiJSUzI1NiIsImtpZCI6IkRIRmJwb0lVcXJZOHQyenBBMnFYZkNtcjVWTzVaRXI0UnpIVV8tZW52dlEiLCJ0eXAiOiJKV1QifQ.eyJleHAiOjM1MzczOTExMDQsImdyb3VwcyI6WyJncm91cDEiLCJncm91cDIiXSwiaWF0IjoxNTM3MzkxMTA0LCJpc3MiOiJ0ZXN0aW5nQHNlY3VyZS5pc3Rpby5pbyIsInNjb3BlIjpbInNjb3BlMSIsInNjb3BlMiJdLCJzdWIiOiJ0ZXN0aW5nQHNlY3VyZS5pc3Rpby5pbyJ9.EdJnEZSH6X8hcyEii7c8H5lnhgjB5dwo07M5oheC8Xz8mOllyg--AHCFWHybM48reunF--oGaG6IXVngCEpVF0_P5DwsUoBgpPmK1JOaKN6_pe9sh0ZwTtdgK_RP01PuI7kUdbOTlkuUi2AO-qUyOm7Art2POzo36DLQlUXv8Ad7NBOqfQaKjE9ndaPWT7aexUsBHxmgiGbz1SyLH879f7uHYPbPKlpHU6P9S-DaKnGLaEchnoKnov7ajhrEhGXAQRukhDPKUHO9L30oPIr5IJllEQfHYtt6IZvlNUGeLUcif3wpry1R5tBXRicx2sXMQ7LyuDremDbcNy_iE76Upg

curl 192.168.229.128:30555/productpage -H "Authorization: Bearer ${TOKEN}"

TOKEN=eyJraWQiOiJjSWNBZlEzQjhrajJ1Y0pUREFCUzdqVHFWUTVmandLUVY3d2hQTkxGQ1c0IiwidHlwIjoiSldUIiwiYWxnIjoiUlMyNTYifQ.eyJmb28iOiJiYXIiLCJzdWIiOiJ0ZXN0aW5nQHNlY3VyZS5pc3Rpby5pbyIsInVzZXJJZCI6Im1hcmsiLCJzY29wZSI6WyJzY29wZTEiLCJzY29wZTIiXSwiaXNzIjoidGVzdGluZ0BzZWN1cmUuaXN0aW8uaW8iLCJleHAiOjE2NTk4Mzc5OTksImlhdCI6MTY1NjMwMDYwOCwiZ3JvdXAiOlsiZ3JvdXAxIiwiZ3JvdXAyIl19.Dfp7uF_GO5h2tvjx74PpzT3Im46jUSmmJjPiAzDr9Z-VYJ1XR5KjIUj8jcB9kDA2rQA-K8IcvO8RE03j2VLg1BCbrOioYR1K-VU95HAjdWudM1MvCQjaMUkrgnPwHYHwd_tUhCRHRGSlyWpGfvn7nnAy4QX838UuyXVI91axpatvCaLjut7W1Y8-Fxwk8XIgVaB2KbyShbc2nfJMDU_cKx4SdXSJKesb1_EcpcdbsCERPDSoU3pNBtsWgyxbPg9E_HaUkiw0ROcgX01VZOONCFSCrtVR6rZombCCvlZ88M1oNWxUWMVsAriOK0zB_tmZM40yGkK78RxlPcdErLG3mg


curl 192.168.229.128:30555/productpage -H "Authorization: Bearer ${TOKEN}"
```



## 3.8remote_jwks

### 3.8.1http_uri

```
package org.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Hello world!
 *
 */
@SpringBootApplication
@RestController
public class App 
{
    public static void main( String[] args )
    {
        SpringApplication.run(App.class, args);
    }

    @RequestMapping("/")
    public String getJwks(){
        String jwks=
        "{ "+
           " \"kty\": \"RSA\","+
               " \"e\": \"AQAB\","+
               "\"use\": \"sig\","+
               " \"kid\": \"cIcAfQ3B8kj2ucJTDABS7jTqVQ5fjwKQV7whPNLFCW4\","+
                "\"alg\": \"RS256\","+
                "\"n\": \"qggabdCirBa84zr9G1gn4Ha2IJe6hDi4oP2FXOuSznGzXt40vXlHlJGAxlUNasA2dI3QjFtqj79Kxo2uh8-VcNs2Xs5NVv98rDRiE6ZfItBTzIfmhZAZANrLn2TEXWy4iQVBt1OhlR4uWEzMaQRAabOEEFCm-T8WGyrz8qJZFu4rJ5BWSMETVEw1gey-EqV_tJznoTju9t_LEFyLO_PMdUADRgMOCN0pckb8rMLcaV0a1twW1EEkQC15pRI2TwusOm-ooVwekDrTqmILqJdhvHzZiTOc7raVBYizjD8Qjdr_D4lAPLrCosYUkORpfAkVBpZ-MQ8Se4UPy6GnfBoHVQ\""+
        "}";
        return jwks;
    }
}

```

构建auth镜像

```
docker  build  . --tag registry.cn-hangzhou.aliyuncs.com/hxpdocker/jwt-server:1.0

docker push registry.cn-hangzhou.aliyuncs.com/hxpdocker/jwt-server:1.0
```

```
[root@master01 auth-simple]# cat Dockerfile 
FROM java:8

ADD  ./jwt-server.jar /app.jar

ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]
```

deploy-jwt-server.yaml

kubectl apply -f deploy-jwt-server.yaml -n istio

```
apiVersion: v1
kind: Service
metadata:
  name: jwt-server
  labels:
    app: jwt-server
spec:
  ports:
  - name: http
    port: 8080
    targetPort: 8080
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
      - image: registry.cn-hangzhou.aliyuncs.com/hxpdocker/jwt-server:1.0
        imagePullPolicy: IfNotPresent
        name: jwt-server
        ports:
        - containerPort: 8080
---
```

ef-remote-jwks-http-uri.yaml

kubectl apply -f ef-remote-jwks-http-uri.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: jwt
spec:
  workloadSelector:
    labels:
      istio: ingressgateway
  configPatches:
    - applyTo: HTTP_FILTER
      match:
        context: GATEWAY
        listener:
          filterChain:
            filter:
              name: "envoy.filters.network.http_connection_manager"
              subFilter:
                name: "envoy.filters.http.router"
      patch:
        operation: INSERT_BEFORE
        value:
          name: envoy.filters.http.jwt_authn
          typedConfig:
            '@type': type.googleapis.com/envoy.extensions.filters.http.jwt_authn.v3.JwtAuthentication
            providers:
              origins-0:
                forward: true
                issuer: testing@secure.istio.io
                remote_jwks:
                  http_uri: 
                    uri: jwt-server.istio.svc.cluster.local:8080
                    cluster: outbound|8080||jwt-server.istio.svc.cluster.local
                    timeout: 1s
            rules:
            - match:
                prefix: /
              requires:
                requiresAny:
                  requirements:
                  - providerName: origins-0
                  - allowMissing: {}
```



```
TOKEN=eyJraWQiOiJjSWNBZlEzQjhrajJ1Y0pUREFCUzdqVHFWUTVmandLUVY3d2hQTkxGQ1c0IiwidHlwIjoiSldUIiwiYWxnIjoiUlMyNTYifQ.eyJmb28iOiJiYXIiLCJzdWIiOiJ0ZXN0aW5nQHNlY3VyZS5pc3Rpby5pbyIsInVzZXJJZCI6Im1hcmsiLCJzY29wZSI6WyJzY29wZTEiLCJzY29wZTIiXSwiaXNzIjoidGVzdGluZ0BzZWN1cmUuaXN0aW8uaW8iLCJleHAiOjE2NTk4Mzc5OTksImlhdCI6MTY1NjMwMDYwOCwiZ3JvdXAiOlsiZ3JvdXAxIiwiZ3JvdXAyIl19.Dfp7uF_GO5h2tvjx74PpzT3Im46jUSmmJjPiAzDr9Z-VYJ1XR5KjIUj8jcB9kDA2rQA-K8IcvO8RE03j2VLg1BCbrOioYR1K-VU95HAjdWudM1MvCQjaMUkrgnPwHYHwd_tUhCRHRGSlyWpGfvn7nnAy4QX838UuyXVI91axpatvCaLjut7W1Y8-Fxwk8XIgVaB2KbyShbc2nfJMDU_cKx4SdXSJKesb1_EcpcdbsCERPDSoU3pNBtsWgyxbPg9E_HaUkiw0ROcgX01VZOONCFSCrtVR6rZombCCvlZ88M1oNWxUWMVsAriOK0zB_tmZM40yGkK78RxlPcdErLG3mg


curl 192.168.229.128:30555/productpage -H "Authorization: Bearer ${TOKEN}"
```



### 3.8.2cache_duration

ef-remote-jwks-cache_duration.yaml

kubectl apply -f ef-remote-jwks-cache_duration.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: jwt
spec:
  workloadSelector:
    labels:
      istio: ingressgateway
  configPatches:
    - applyTo: HTTP_FILTER
      match:
        context: GATEWAY
        listener:
          filterChain:
            filter:
              name: "envoy.filters.network.http_connection_manager"
              subFilter:
                name: "envoy.filters.http.router"
      patch:
        operation: INSERT_BEFORE
        value:
          name: envoy.filters.http.jwt_authn
          typedConfig:
            '@type': type.googleapis.com/envoy.extensions.filters.http.jwt_authn.v3.JwtAuthentication
            providers:
              origins-0:
                forward: true
                issuer: testing@secure.istio.io
                remote_jwks:
                  http_uri: 
                    uri: jwt-server.istio.svc.cluster.local:8080
                    cluster: outbound|8080||jwt-server.istio.svc.cluster.local
                    timeout: 1s
                  cache_duration: 60s
            rules:
            - match:
                prefix: /
              requires:
                requiresAny:
                  requirements:
                  - providerName: origins-0
                  - allowMissing: {}
```



```
TOKEN=eyJraWQiOiJjSWNBZlEzQjhrajJ1Y0pUREFCUzdqVHFWUTVmandLUVY3d2hQTkxGQ1c0IiwidHlwIjoiSldUIiwiYWxnIjoiUlMyNTYifQ.eyJmb28iOiJiYXIiLCJzdWIiOiJ0ZXN0aW5nQHNlY3VyZS5pc3Rpby5pbyIsInVzZXJJZCI6Im1hcmsiLCJzY29wZSI6WyJzY29wZTEiLCJzY29wZTIiXSwiaXNzIjoidGVzdGluZ0BzZWN1cmUuaXN0aW8uaW8iLCJleHAiOjE2NTk4Mzc5OTksImlhdCI6MTY1NjMwMDYwOCwiZ3JvdXAiOlsiZ3JvdXAxIiwiZ3JvdXAyIl19.Dfp7uF_GO5h2tvjx74PpzT3Im46jUSmmJjPiAzDr9Z-VYJ1XR5KjIUj8jcB9kDA2rQA-K8IcvO8RE03j2VLg1BCbrOioYR1K-VU95HAjdWudM1MvCQjaMUkrgnPwHYHwd_tUhCRHRGSlyWpGfvn7nnAy4QX838UuyXVI91axpatvCaLjut7W1Y8-Fxwk8XIgVaB2KbyShbc2nfJMDU_cKx4SdXSJKesb1_EcpcdbsCERPDSoU3pNBtsWgyxbPg9E_HaUkiw0ROcgX01VZOONCFSCrtVR6rZombCCvlZ88M1oNWxUWMVsAriOK0zB_tmZM40yGkK78RxlPcdErLG3mg


curl 192.168.229.128:30555/productpage -H "Authorization: Bearer ${TOKEN}"
```



### 3.8.3async_fetch

ef-remote-jwks-async_fetch.yaml

kubectl apply -f ef-remote-jwks-async_fetch.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: jwt
spec:
  workloadSelector:
    labels:
      istio: ingressgateway
  configPatches:
    - applyTo: HTTP_FILTER
      match:
        context: GATEWAY
        listener:
          filterChain:
            filter:
              name: "envoy.filters.network.http_connection_manager"
              subFilter:
                name: "envoy.filters.http.router"
      patch:
        operation: INSERT_BEFORE
        value:
          name: envoy.filters.http.jwt_authn
          typedConfig:
            '@type': type.googleapis.com/envoy.extensions.filters.http.jwt_authn.v3.JwtAuthentication
            providers:
              origins-0:
                forward: true
                issuer: testing@secure.istio.io
                remote_jwks:
                  http_uri: 
                    uri: jwt-server.istio.svc.cluster.local:8080
                    cluster: outbound|8080||jwt-server.istio.svc.cluster.local
                    timeout: 1s
                  async_fetch:
                    fast_listener: false
                  cache_duration: 60s
            rules:
            - match:
                prefix: /
              requires:
                requiresAny:
                  requirements:
                  - providerName: origins-0
                  - allowMissing: {}
```



```
TOKEN=eyJraWQiOiJjSWNBZlEzQjhrajJ1Y0pUREFCUzdqVHFWUTVmandLUVY3d2hQTkxGQ1c0IiwidHlwIjoiSldUIiwiYWxnIjoiUlMyNTYifQ.eyJmb28iOiJiYXIiLCJzdWIiOiJ0ZXN0aW5nQHNlY3VyZS5pc3Rpby5pbyIsInVzZXJJZCI6Im1hcmsiLCJzY29wZSI6WyJzY29wZTEiLCJzY29wZTIiXSwiaXNzIjoidGVzdGluZ0BzZWN1cmUuaXN0aW8uaW8iLCJleHAiOjE2NTk4Mzc5OTksImlhdCI6MTY1NjMwMDYwOCwiZ3JvdXAiOlsiZ3JvdXAxIiwiZ3JvdXAyIl19.Dfp7uF_GO5h2tvjx74PpzT3Im46jUSmmJjPiAzDr9Z-VYJ1XR5KjIUj8jcB9kDA2rQA-K8IcvO8RE03j2VLg1BCbrOioYR1K-VU95HAjdWudM1MvCQjaMUkrgnPwHYHwd_tUhCRHRGSlyWpGfvn7nnAy4QX838UuyXVI91axpatvCaLjut7W1Y8-Fxwk8XIgVaB2KbyShbc2nfJMDU_cKx4SdXSJKesb1_EcpcdbsCERPDSoU3pNBtsWgyxbPg9E_HaUkiw0ROcgX01VZOONCFSCrtVR6rZombCCvlZ88M1oNWxUWMVsAriOK0zB_tmZM40yGkK78RxlPcdErLG3mg


curl 192.168.229.128:30555/productpage -H "Authorization: Bearer ${TOKEN}"
```



### 3.8.4retry_policy

ef-remote-jwks-retry_policy.yaml

kubectl apply -f ef-remote-jwks-retry_policy.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: jwt
spec:
  workloadSelector:
    labels:
      istio: ingressgateway
  configPatches:
    - applyTo: HTTP_FILTER
      match:
        context: GATEWAY
        listener:
          filterChain:
            filter:
              name: "envoy.filters.network.http_connection_manager"
              subFilter:
                name: "envoy.filters.http.router"
      patch:
        operation: INSERT_BEFORE
        value:
          name: envoy.filters.http.jwt_authn
          typedConfig:
            '@type': type.googleapis.com/envoy.extensions.filters.http.jwt_authn.v3.JwtAuthentication
            providers:
              origins-0:
                forward: true
                issuer: testing@secure.istio.io
                remote_jwks:
                  http_uri: 
                    uri: jwt-server.istio.svc.cluster.local:8080
                    cluster: outbound|8080||jwt-server.istio.svc.cluster.local
                    timeout: 1s
                  retry_policy:
                    retry_back_off:
                      base_interval: 0.01s
                      max_interval: 20s
                    num_retries: 10
                  cache_duration: 60s
            rules:
            - match:
                prefix: /
              requires:
                requiresAny:
                  requirements:
                  - providerName: origins-0
                  - allowMissing: {}
```



```
TOKEN=eyJraWQiOiJjSWNBZlEzQjhrajJ1Y0pUREFCUzdqVHFWUTVmandLUVY3d2hQTkxGQ1c0IiwidHlwIjoiSldUIiwiYWxnIjoiUlMyNTYifQ.eyJmb28iOiJiYXIiLCJzdWIiOiJ0ZXN0aW5nQHNlY3VyZS5pc3Rpby5pbyIsInVzZXJJZCI6Im1hcmsiLCJzY29wZSI6WyJzY29wZTEiLCJzY29wZTIiXSwiaXNzIjoidGVzdGluZ0BzZWN1cmUuaXN0aW8uaW8iLCJleHAiOjE2NTk4Mzc5OTksImlhdCI6MTY1NjMwMDYwOCwiZ3JvdXAiOlsiZ3JvdXAxIiwiZ3JvdXAyIl19.Dfp7uF_GO5h2tvjx74PpzT3Im46jUSmmJjPiAzDr9Z-VYJ1XR5KjIUj8jcB9kDA2rQA-K8IcvO8RE03j2VLg1BCbrOioYR1K-VU95HAjdWudM1MvCQjaMUkrgnPwHYHwd_tUhCRHRGSlyWpGfvn7nnAy4QX838UuyXVI91axpatvCaLjut7W1Y8-Fxwk8XIgVaB2KbyShbc2nfJMDU_cKx4SdXSJKesb1_EcpcdbsCERPDSoU3pNBtsWgyxbPg9E_HaUkiw0ROcgX01VZOONCFSCrtVR6rZombCCvlZ88M1oNWxUWMVsAriOK0zB_tmZM40yGkK78RxlPcdErLG3mg


curl 192.168.229.128:30555/productpage -H "Authorization: Bearer ${TOKEN}"
```



## 3.9providers

### 3.9.1header_in_metadata

ef-jwt-providers-header_in_metadata.yaml

kubectl apply -f ef-jwt-providers-header_in_metadata.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: jwt
spec:
  workloadSelector:
    labels:
      istio: ingressgateway
  priority: 20
  configPatches:
    - applyTo: HTTP_FILTER
      match:
        context: GATEWAY
        listener:
          filterChain:
            filter:
              name: "envoy.filters.network.http_connection_manager"
              subFilter:
                name: "envoy.filters.http.router"
      patch:
        operation: INSERT_BEFORE
        value:
          name: envoy.filters.http.jwt_authn
          typedConfig:
            '@type': type.googleapis.com/envoy.extensions.filters.http.jwt_authn.v3.JwtAuthentication
            providers:
              origins-0:
                forward: true
                issuer: testing@secure.istio.io
                header_in_metadata: my_header
                remote_jwks:
                  http_uri: 
                    uri: jwt-server.istio.svc.cluster.local:8080
                    cluster: outbound|8080||jwt-server.istio.svc.cluster.local
                    timeout: 1s
                  retry_policy:
                    retry_back_off:
                      base_interval: 0.01s
                      max_interval: 20s
                    num_retries: 10
                  cache_duration: 60s
            rules:
            - match:
                prefix: /
              requires:
                requiresAny:
                  requirements:
                  - providerName: origins-0
                  - allowMissing: {}
```

ef-rbac-metadata.yaml

kubectl apply -f ef-rbac-metadata.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: match
spec:
  workloadSelector:
    labels:
      istio: ingressgateway
  priority: 20
  configPatches:
  - applyTo: HTTP_FILTER
    match:
      context: GATEWAY
      listener:
        portNumber: 8080
        filterChain:
          filter:
            name: "envoy.filters.network.http_connection_manager"
            subFilter:
              name: "envoy.filters.http.router"
    patch:
      operation: INSERT_BEFORE
      value:         
          name: envoy.filters.http.rbac
          typedConfig:
            '@type': type.googleapis.com/envoy.extensions.filters.http.rbac.v3.RBAC
            rules:
              action: DENY
              policies:
                "product-viewer":
                  permissions:
                  - metadata:
                      filter: envoy.filters.http.jwt_authn
                      path:
                      - key: my_header
                      - key: kid
                      value:
                        string_match:
                          exact: "cIcAfQ3B8kj2ucJTDABS7jTqVQ5fjwKQV7whPNLFCW4"
                  principals:
                  - any: true
```



```
TOKEN=eyJraWQiOiJjSWNBZlEzQjhrajJ1Y0pUREFCUzdqVHFWUTVmandLUVY3d2hQTkxGQ1c0IiwidHlwIjoiSldUIiwiYWxnIjoiUlMyNTYifQ.eyJmb28iOiJiYXIiLCJzdWIiOiJ0ZXN0aW5nQHNlY3VyZS5pc3Rpby5pbyIsInVzZXJJZCI6Im1hcmsiLCJzY29wZSI6WyJzY29wZTEiLCJzY29wZTIiXSwiaXNzIjoidGVzdGluZ0BzZWN1cmUuaXN0aW8uaW8iLCJleHAiOjE2NTk4Mzc5OTksImlhdCI6MTY1NjMwMDYwOCwiZ3JvdXAiOlsiZ3JvdXAxIiwiZ3JvdXAyIl19.Dfp7uF_GO5h2tvjx74PpzT3Im46jUSmmJjPiAzDr9Z-VYJ1XR5KjIUj8jcB9kDA2rQA-K8IcvO8RE03j2VLg1BCbrOioYR1K-VU95HAjdWudM1MvCQjaMUkrgnPwHYHwd_tUhCRHRGSlyWpGfvn7nnAy4QX838UuyXVI91axpatvCaLjut7W1Y8-Fxwk8XIgVaB2KbyShbc2nfJMDU_cKx4SdXSJKesb1_EcpcdbsCERPDSoU3pNBtsWgyxbPg9E_HaUkiw0ROcgX01VZOONCFSCrtVR6rZombCCvlZ88M1oNWxUWMVsAriOK0zB_tmZM40yGkK78RxlPcdErLG3mg


curl 192.168.229.128:30555/productpage -H "Authorization: Bearer ${TOKEN}"
```



### 3.9.2clock_skew_seconds

ef-jwt-provider-clock_skew_seconds.yaml

kubectl apply -f ef-jwt-provider-clock_skew_seconds.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: jwt
spec:
  workloadSelector:
    labels:
      istio: ingressgateway
  priority: 20
  configPatches:
    - applyTo: HTTP_FILTER
      match:
        context: GATEWAY
        listener:
          filterChain:
            filter:
              name: "envoy.filters.network.http_connection_manager"
              subFilter:
                name: "envoy.filters.http.router"
      patch:
        operation: INSERT_BEFORE
        value:
          name: envoy.filters.http.jwt_authn
          typedConfig:
            '@type': type.googleapis.com/envoy.extensions.filters.http.jwt_authn.v3.JwtAuthentication
            providers:
              origins-0:
                clock_skew_seconds: 60
                forward: true
                issuer: testing@secure.istio.io
                remote_jwks:
                  http_uri: 
                    uri: jwt-server.istio.svc.cluster.local:8080
                    cluster: outbound|8080||jwt-server.istio.svc.cluster.local
                    timeout: 1s
                  retry_policy:
                    retry_back_off:
                      base_interval: 0.01s
                      max_interval: 20s
                    num_retries: 10
                  cache_duration: 60s
            rules:
            - match:
                prefix: /
              requires:
                requiresAny:
                  requirements:
                  - providerName: origins-0
                  - allowMissing: {}
```

```
TOKEN=eyJraWQiOiJjSWNBZlEzQjhrajJ1Y0pUREFCUzdqVHFWUTVmandLUVY3d2hQTkxGQ1c0IiwidHlwIjoiSldUIiwiYWxnIjoiUlMyNTYifQ.eyJmb28iOiJiYXIiLCJzdWIiOiJ0ZXN0aW5nQHNlY3VyZS5pc3Rpby5pbyIsInVzZXJJZCI6Im1hcmsiLCJzY29wZSI6WyJzY29wZTEiLCJzY29wZTIiXSwiaXNzIjoidGVzdGluZ0BzZWN1cmUuaXN0aW8uaW8iLCJleHAiOjE2NTk4Mzc5OTksImlhdCI6MTY1NjMwMDYwOCwiZ3JvdXAiOlsiZ3JvdXAxIiwiZ3JvdXAyIl19.Dfp7uF_GO5h2tvjx74PpzT3Im46jUSmmJjPiAzDr9Z-VYJ1XR5KjIUj8jcB9kDA2rQA-K8IcvO8RE03j2VLg1BCbrOioYR1K-VU95HAjdWudM1MvCQjaMUkrgnPwHYHwd_tUhCRHRGSlyWpGfvn7nnAy4QX838UuyXVI91axpatvCaLjut7W1Y8-Fxwk8XIgVaB2KbyShbc2nfJMDU_cKx4SdXSJKesb1_EcpcdbsCERPDSoU3pNBtsWgyxbPg9E_HaUkiw0ROcgX01VZOONCFSCrtVR6rZombCCvlZ88M1oNWxUWMVsAriOK0zB_tmZM40yGkK78RxlPcdErLG3mg


curl 192.168.229.128:30555/productpage -H "Authorization: Bearer ${TOKEN}"
```



### 3.9.3jwt_cache_config

ef-jwt-provider-jwt_cache_config.yaml

kubectl apply -f ef-jwt-provider-jwt_cache_config.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: jwt
spec:
  workloadSelector:
    labels:
      istio: ingressgateway
  priority: 20
  configPatches:
    - applyTo: HTTP_FILTER
      match:
        context: GATEWAY
        listener:
          filterChain:
            filter:
              name: "envoy.filters.network.http_connection_manager"
              subFilter:
                name: "envoy.filters.http.router"
      patch:
        operation: INSERT_BEFORE
        value:
          name: envoy.filters.http.jwt_authn
          typedConfig:
            '@type': type.googleapis.com/envoy.extensions.filters.http.jwt_authn.v3.JwtAuthentication
            providers:
              origins-0:
                jwt_cache_config:
                  jwt_cache_size: 100
                forward: true
                issuer: testing@secure.istio.io
                remote_jwks:
                  http_uri: 
                    uri: jwt-server.istio.svc.cluster.local:8080
                    cluster: outbound|8080||jwt-server.istio.svc.cluster.local
                    timeout: 1s
                  retry_policy:
                    retry_back_off:
                      base_interval: 0.01s
                      max_interval: 20s
                    num_retries: 10
                  cache_duration: 60s
            rules:
            - match:
                prefix: /
              requires:
                requiresAny:
                  requirements:
                  - providerName: origins-0
                  - allowMissing: {}
```

```
TOKEN=eyJraWQiOiJjSWNBZlEzQjhrajJ1Y0pUREFCUzdqVHFWUTVmandLUVY3d2hQTkxGQ1c0IiwidHlwIjoiSldUIiwiYWxnIjoiUlMyNTYifQ.eyJmb28iOiJiYXIiLCJzdWIiOiJ0ZXN0aW5nQHNlY3VyZS5pc3Rpby5pbyIsInVzZXJJZCI6Im1hcmsiLCJzY29wZSI6WyJzY29wZTEiLCJzY29wZTIiXSwiaXNzIjoidGVzdGluZ0BzZWN1cmUuaXN0aW8uaW8iLCJleHAiOjE2NTk4Mzc5OTksImlhdCI6MTY1NjMwMDYwOCwiZ3JvdXAiOlsiZ3JvdXAxIiwiZ3JvdXAyIl19.Dfp7uF_GO5h2tvjx74PpzT3Im46jUSmmJjPiAzDr9Z-VYJ1XR5KjIUj8jcB9kDA2rQA-K8IcvO8RE03j2VLg1BCbrOioYR1K-VU95HAjdWudM1MvCQjaMUkrgnPwHYHwd_tUhCRHRGSlyWpGfvn7nnAy4QX838UuyXVI91axpatvCaLjut7W1Y8-Fxwk8XIgVaB2KbyShbc2nfJMDU_cKx4SdXSJKesb1_EcpcdbsCERPDSoU3pNBtsWgyxbPg9E_HaUkiw0ROcgX01VZOONCFSCrtVR6rZombCCvlZ88M1oNWxUWMVsAriOK0zB_tmZM40yGkK78RxlPcdErLG3mg


curl 192.168.229.128:30555/productpage -H "Authorization: Bearer ${TOKEN}"
```



## 3.10rules

### 3.10.1requires_all

ef-jwt-rules-requires_all.yaml

kubectl apply -f ef-jwt-rules-requires_all.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: jwt
spec:
  workloadSelector:
    labels:
      istio: ingressgateway
  configPatches:
    - applyTo: HTTP_FILTER
      match:
        context: GATEWAY
        listener:
          filterChain:
            filter:
              name: "envoy.filters.network.http_connection_manager"
              subFilter:
                name: "envoy.filters.http.router"
      patch:
        operation: INSERT_BEFORE
        value:
          name: envoy.filters.http.jwt_authn
          typedConfig:
            '@type': type.googleapis.com/envoy.extensions.filters.http.jwt_authn.v3.JwtAuthentication
            providers:
              origins-0:
                forward: true
                issuer: testing@secure.istio.io
                localJwks:
                  inlineString: "{ \"keys\":\n   [ \n     {\n       \"e\":\"AQAB\",\n
                    \      \"kid\":\"DHFbpoIUqrY8t2zpA2qXfCmr5VO5ZEr4RzHU_-envvQ\",\n
                    \      \"kty\":\"RSA\",\n       \"n\":\"xAE7eB6qugXyCAG3yhh7pkDkT65pHymX-P7KfIupjf59vsdo91bSP9C8H07pSAGQO1MV_xFj9VswgsCg4R6otmg5PV2He95lZdHtOcU5DXIg_pbhLdKXbi66GlVeK6ABZOUW3WYtnNHD-91gVuoeJT_DwtGGcp4ignkgXfkiEm4sw-4sfb4qdt5oLbyVpmW6x9cfa7vs2WTfURiCrBoUqgBo_-4WTiULmmHSGZHOjzwa8WtrtOQGsAFjIbno85jp6MnGGGZPYZbDAa_b3y5u-YpW7ypZrvD8BgtKVjgtQgZhLAGezMt0ua3DRrWnKqTZ0BJ_EyxOGuHJrLsn00fnMQ\"\n
                    \    }\n   ]\n}\n"
                payloadInMetadata: testing@secure.istio.io
              origins-1:
                forward: true
                issuer: testing@secure.istio.io
                localJwks:
                  inlineString: "{\"keys\": [{
                      \      \"kty\": \"RSA\",
                      \      \"e\": \"AQAB\",
                      \      \"use\": \"sig\",
                      \      \"kid\": \"cIcAfQ3B8kj2ucJTDABS7jTqVQ5fjwKQV7whPNLFCW4\",
                      \      \"alg\": \"RS256\",
                      \      \"n\": \"qggabdCirBa84zr9G1gn4Ha2IJe6hDi4oP2FXOuSznGzXt40vXlHlJGAxlUNasA2dI3QjFtqj79Kxo2uh8-VcNs2Xs5NVv98rDRiE6ZfItBTzIfmhZAZANrLn2TEXWy4iQVBt1OhlR4uWEzMaQRAabOEEFCm-T8WGyrz8qJZFu4rJ5BWSMETVEw1gey-EqV_tJznoTju9t_LEFyLO_PMdUADRgMOCN0pckb8rMLcaV0a1twW1EEkQC15pRI2TwusOm-ooVwekDrTqmILqJdhvHzZiTOc7raVBYizjD8Qjdr_D4lAPLrCosYUkORpfAkVBpZ-MQ8Se4UPy6GnfBoHVQ\"
                       \ }]}"
                payloadInMetadata: testing@secure.istio.io
            rules:
            - match:
                prefix: /
              requires:
                requires_all:
                  requirements:
                  - providerName: origins-0
                  - providerName: origins-1
```



### 3.10.2requirement_name

ef-jwt-rules-requirement_name.yaml

kubectl apply -f ef-jwt-rules-requirement_name.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: jwt
spec:
  workloadSelector:
    labels:
      istio: ingressgateway
  priority: 20
  configPatches:
    - applyTo: HTTP_FILTER
      match:
        context: GATEWAY
        listener:
          filterChain:
            filter:
              name: "envoy.filters.network.http_connection_manager"
              subFilter:
                name: "envoy.filters.http.router"
      patch:
        operation: INSERT_BEFORE
        value:
          name: envoy.filters.http.jwt_authn
          typedConfig:
            '@type': type.googleapis.com/envoy.extensions.filters.http.jwt_authn.v3.JwtAuthentication
            providers:
              origins-0:
                jwt_cache_config:
                  jwt_cache_size: 100
                forward: true
                issuer: testing@secure.istio.io
                remote_jwks:
                  http_uri: 
                    uri: jwt-server.istio.svc.cluster.local:8080
                    cluster: outbound|8080||jwt-server.istio.svc.cluster.local
                    timeout: 1s
                  retry_policy:
                    retry_back_off:
                      base_interval: 0.01s
                      max_interval: 20s
                    num_retries: 10
                  cache_duration: 60s
            rules:
            - match:
                prefix: /
              requirement_name: test1
            requirement_map:
              test1:
                requiresAny:
                  requirements:
                  - providerName: origins-0
                  - allow_missing: {}
```



### 3.10.3provider_and_audiences

ef-jwt-rules-provider_and_audiences.yaml

kubectl apply -f ef-jwt-rules-provider_and_audiences.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: jwt
spec:
  workloadSelector:
    labels:
      istio: ingressgateway
  priority: 20
  configPatches:
    - applyTo: HTTP_FILTER
      match:
        context: GATEWAY
        listener:
          filterChain:
            filter:
              name: "envoy.filters.network.http_connection_manager"
              subFilter:
                name: "envoy.filters.http.router"
      patch:
        operation: INSERT_BEFORE
        value:
          name: envoy.filters.http.jwt_authn
          typedConfig:
            '@type': type.googleapis.com/envoy.extensions.filters.http.jwt_authn.v3.JwtAuthentication
            providers:
              origins-0:
                audiences:
                - test
                jwt_cache_config:
                  jwt_cache_size: 100
                forward: true
                issuer: testing@secure.istio.io
                remote_jwks:
                  http_uri: 
                    uri: jwt-server.istio.svc.cluster.local:8080
                    cluster: outbound|8080||jwt-server.istio.svc.cluster.local
                    timeout: 1s
                  retry_policy:
                    retry_back_off:
                      base_interval: 0.01s
                      max_interval: 20s
                    num_retries: 10
                  cache_duration: 60s
            rules:
            - match:
                prefix: /
              requires:
                provider_and_audiences:
                  provider_name: origins-0
                  audiences:
                  - app
```

```
TOKEN=eyJraWQiOiJjSWNBZlEzQjhrajJ1Y0pUREFCUzdqVHFWUTVmandLUVY3d2hQTkxGQ1c0IiwidHlwIjoiSldUIiwiYWxnIjoiUlMyNTYifQ.eyJmb28iOiJiYXIiLCJhdWQiOlsiYXBwIiwid2ViIl0sInN1YiI6InRlc3RpbmdAc2VjdXJlLmlzdGlvLmlvIiwidXNlcklkIjoibWFyayIsInNjb3BlIjpbInNjb3BlMSIsInNjb3BlMiJdLCJpc3MiOiJ0ZXN0aW5nQHNlY3VyZS5pc3Rpby5pbyIsImV4cCI6MTY1OTgzNzk5OSwiaWF0IjoxNjU2MzAwNjA4LCJncm91cCI6WyJncm91cDEiLCJncm91cDIiXX0.PuVoIsT96csmYpS7gC0XmZG5xmUWsTFtauELMysfYi980u7aIS_o9d0LNqRooscE_jxC2n4GopevrtrF_m0IyBBVC8ibvrWdlF4vJ0x2EA2oePoa6l6kfFm4FBcNOnydVoH_tnDtUoHxnmt62JcVlrqtv2tvPngy5vwZaLvh2iDiqtioCsSs3z9pI2_BuOJ0mEQmxhsTwXJsUbiQaJlgYQIDwKGf1WbeNOt4qWONj1oFCzXXMAoUE9wySkdPAc1ot9WFNGzwOUO8cr08UnqJXby3M5ROsEuXchOqgGJChaCwCfcbZH5IkHb5304-KZn69kyeFO8z0nWstdsjejxDyA



curl 192.168.229.128:30555/productpage -H "Authorization: Bearer ${TOKEN}"
```



### 3.10.4allow_missing_or_failed

ef-jwt-rules-allow_missing_or_failed.yaml

kubectl apply -f ef-jwt-rules-allow_missing_or_failed.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: jwt
spec:
  workloadSelector:
    labels:
      istio: ingressgateway
  priority: 20
  configPatches:
    - applyTo: HTTP_FILTER
      match:
        context: GATEWAY
        listener:
          filterChain:
            filter:
              name: "envoy.filters.network.http_connection_manager"
              subFilter:
                name: "envoy.filters.http.router"
      patch:
        operation: INSERT_BEFORE
        value:
          name: envoy.filters.http.jwt_authn
          typedConfig:
            '@type': type.googleapis.com/envoy.extensions.filters.http.jwt_authn.v3.JwtAuthentication
            providers:
              origins-0:
                jwt_cache_config:
                  jwt_cache_size: 100
                forward: true
                issuer: testing@secure.istio.io
                remote_jwks:
                  http_uri: 
                    uri: jwt-server.istio.svc.cluster.local:8080
                    cluster: outbound|8080||jwt-server.istio.svc.cluster.local
                    timeout: 1s
                  retry_policy:
                    retry_back_off:
                      base_interval: 0.01s
                      max_interval: 20s
                    num_retries: 10
                  cache_duration: 60s
            rules:
            - match:
                prefix: /
              requires:
                requiresAny:
                  requirements:
                  - providerName: origins-0
                  - allow_missing_or_failed: {}
```

```
TOKEN=eyJraWQiOiJjSWNBZlEzQjhrajJ1Y0pUREFCUzdqVHFWUTVmandLUVY3d2hQTkxGQ1c0IiwidHlwIjoiSldUIiwiYWxnIjoiUlMyNTYifQ.eyJmb28iOiJiYXIiLCJhdWQiOlsiYXBwIiwid2ViIl0sInN1YiI6InRlc3RpbmdAc2VjdXJlLmlzdGlvLmlvIiwidXNlcklkIjoibWFyayIsInNjb3BlIjpbInNjb3BlMSIsInNjb3BlMiJdLCJpc3MiOiJ0ZXN0aW5nQHNlY3VyZS5pc3Rpby5pbyIsImV4cCI6MTY1OTgzNzk5OSwiaWF0IjoxNjU2MzAwNjA4LCJncm91cCI6WyJncm91cDEiLCJncm91cDIiXX0.PuVoIsT96csmYpS7gC0XmZG5xmUWsTFtauELMysfYi980u7aIS_o9d0LNqRooscE_jxC2n4GopevrtrF_m0IyBBVC8ibvrWdlF4vJ0x2EA2oePoa6l6kfFm4FBcNOnydVoH_tnDtUoHxnmt62JcVlrqtv2tvPngy5vwZaLvh2iDiqtioCsSs3z9pI2_BuOJ0mEQmxhsTwXJsUbiQaJlgYQIDwKGf1WbeNOt4qWONj1oFCzXXMAoUE9wySkdPAc1ot9WFNGzwOUO8cr08UnqJXby3M5ROsEuXchOqgGJChaCwCfcbZH5IkHb5304-KZn69kyeFO8z0nWstdsjejxDyA



curl 192.168.229.128:30555/productpage -H "Authorization: Bearer ${TOKEN}"
```



## 3.11filter_state_rules

ef-jwt-filter_state_rules.yaml

kubectl apply -f ef-jwt-filter_state_rules.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: jwt
spec:
  workloadSelector:
    labels:
      istio: ingressgateway
  priority: 20
  configPatches:
    - applyTo: HTTP_FILTER
      match:
        context: GATEWAY
        listener:
          filterChain:
            filter:
              name: "envoy.filters.network.http_connection_manager"
              subFilter:
                name: "envoy.filters.http.router"
      patch:
        operation: INSERT_BEFORE
        value:
          name: envoy.filters.http.jwt_authn
          typedConfig:
            '@type': type.googleapis.com/envoy.extensions.filters.http.jwt_authn.v3.JwtAuthentication
            providers:
              origins-0:
                forward: true
                issuer: testing@secure.istio.io
                localJwks:
                  inlineString: "{ \"keys\":\n   [ \n     {\n       \"e\":\"AQAB\",\n
                    \      \"kid\":\"DHFbpoIUqrY8t2zpA2qXfCmr5VO5ZEr4RzHU_-envvQ\",\n
                    \      \"kty\":\"RSA\",\n       \"n\":\"xAE7eB6qugXyCAG3yhh7pkDkT65pHymX-P7KfIupjf59vsdo91bSP9C8H07pSAGQO1MV_xFj9VswgsCg4R6otmg5PV2He95lZdHtOcU5DXIg_pbhLdKXbi66GlVeK6ABZOUW3WYtnNHD-91gVuoeJT_DwtGGcp4ignkgXfkiEm4sw-4sfb4qdt5oLbyVpmW6x9cfa7vs2WTfURiCrBoUqgBo_-4WTiULmmHSGZHOjzwa8WtrtOQGsAFjIbno85jp6MnGGGZPYZbDAa_b3y5u-YpW7ypZrvD8BgtKVjgtQgZhLAGezMt0ua3DRrWnKqTZ0BJ_EyxOGuHJrLsn00fnMQ\"\n
                    \    }\n   ]\n}\n"
                payloadInMetadata: testing@secure.istio.io
              origins-1:
                jwt_cache_config:
                  jwt_cache_size: 100
                forward: true
                issuer: testing@secure.istio.io
                remote_jwks:
                  http_uri: 
                    uri: jwt-server.istio.svc.cluster.local:8080
                    cluster: outbound|8080||jwt-server.istio.svc.cluster.local
                    timeout: 1s
                  retry_policy:
                    retry_back_off:
                      base_interval: 0.01s
                      max_interval: 20s
                    num_retries: 10
                  cache_duration: 60s
            filter_state_rules:
              name: jwt_selector
              requires:
                issuer_1:
                  provider_name: origins-1
```

```
TOKEN=eyJraWQiOiJjSWNBZlEzQjhrajJ1Y0pUREFCUzdqVHFWUTVmandLUVY3d2hQTkxGQ1c0IiwidHlwIjoiSldUIiwiYWxnIjoiUlMyNTYifQ.eyJmb28iOiJiYXIiLCJhdWQiOlsiYXBwIiwid2ViIl0sInN1YiI6InRlc3RpbmdAc2VjdXJlLmlzdGlvLmlvIiwidXNlcklkIjoibWFyayIsInNjb3BlIjpbInNjb3BlMSIsInNjb3BlMiJdLCJpc3MiOiJ0ZXN0aW5nQHNlY3VyZS5pc3Rpby5pbyIsImV4cCI6MTY1OTgzNzk5OSwiaWF0IjoxNjU2MzAwNjA4LCJncm91cCI6WyJncm91cDEiLCJncm91cDIiXX0.PuVoIsT96csmYpS7gC0XmZG5xmUWsTFtauELMysfYi980u7aIS_o9d0LNqRooscE_jxC2n4GopevrtrF_m0IyBBVC8ibvrWdlF4vJ0x2EA2oePoa6l6kfFm4FBcNOnydVoH_tnDtUoHxnmt62JcVlrqtv2tvPngy5vwZaLvh2iDiqtioCsSs3z9pI2_BuOJ0mEQmxhsTwXJsUbiQaJlgYQIDwKGf1WbeNOt4qWONj1oFCzXXMAoUE9wySkdPAc1ot9WFNGzwOUO8cr08UnqJXby3M5ROsEuXchOqgGJChaCwCfcbZH5IkHb5304-KZn69kyeFO8z0nWstdsjejxDyA



curl 192.168.229.128:30555/productpage -H "Authorization: Bearer ${TOKEN}"
```



## 3.12bypass_cors_preflight

```
cat << EOF > ef-cors-not-simple.yaml
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: cors
spec:
  workloadSelector:
    labels:
      istio: ingressgateway
  configPatches:
  - applyTo: NETWORK_FILTER
    match:
      listener:
        name: 0.0.0.0_8080  
        filterChain:
          filter:
            name: "envoy.filters.network.http_connection_manager"
    patch:
      operation: MERGE
      value:
        name: envoy.filters.network.http_connection_manager
        typed_config:
          "@type": "type.googleapis.com/envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager"
          codec_type: AUTO
          stat_prefix: ingress_http
          route_config:
            name: local_route
            virtual_hosts:
            - name: local_route
              domains:
              - "*"
              cors:
                allow_origin_string_match:
                - safeRegex:
                    googleRe2: {}
                    regex: .*
                allow_methods: "GET,OPTIONS"
                allow_headers: "content-type"
                max_age: "60"
                filter_enabled:
                  default_value:
                    numerator: 100
                    denominator: HUNDRED
              routes:
              - match:
                  prefix: "/reviews"
                route:
                  cluster: outbound|9080||reviews.istio.svc.cluster.local
EOF

kubectl apply -f ef-cors-not-simple.yaml -n istio-system --context context-cluster1
```

ef-jwt-bypass_cors_preflight.yaml

kubectl apply -f ef-jwt-bypass_cors_preflight.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: jwt
spec:
  workloadSelector:
    labels:
      istio: ingressgateway
  priority: 20
  configPatches:
    - applyTo: HTTP_FILTER
      match:
        context: GATEWAY
        listener:
          filterChain:
            filter:
              name: "envoy.filters.network.http_connection_manager"
              subFilter:
                name: "envoy.filters.http.router"
      patch:
        operation: INSERT_BEFORE
        value:
          name: envoy.filters.http.jwt_authn
          typedConfig:
            '@type': type.googleapis.com/envoy.extensions.filters.http.jwt_authn.v3.JwtAuthentication
            providers:
              origins-0:
                fromParams:
                - my-token
                jwt_cache_config:
                  jwt_cache_size: 100
                forward: true
                issuer: testing@secure.istio.io
                remote_jwks:
                  http_uri: 
                    uri: jwt-server.istio.svc.cluster.local:8080
                    cluster: outbound|8080||jwt-server.istio.svc.cluster.local
                    timeout: 1s
                  retry_policy:
                    retry_back_off:
                      base_interval: 0.01s
                      max_interval: 20s
                    num_retries: 10
                  cache_duration: 60s
            rules:
            - match:
                prefix: /
              requires:
                providerName: origins-0
            bypass_cors_preflight: true
```

```
TOKEN=eyJraWQiOiJjSWNBZlEzQjhrajJ1Y0pUREFCUzdqVHFWUTVmandLUVY3d2hQTkxGQ1c0IiwidHlwIjoiSldUIiwiYWxnIjoiUlMyNTYifQ.eyJmb28iOiJiYXIiLCJhdWQiOlsiYXBwIiwid2ViIl0sInN1YiI6InRlc3RpbmdAc2VjdXJlLmlzdGlvLmlvIiwidXNlcklkIjoibWFyayIsInNjb3BlIjpbInNjb3BlMSIsInNjb3BlMiJdLCJpc3MiOiJ0ZXN0aW5nQHNlY3VyZS5pc3Rpby5pbyIsImV4cCI6MTY1OTgzNzk5OSwiaWF0IjoxNjU2MzAwNjA4LCJncm91cCI6WyJncm91cDEiLCJncm91cDIiXX0.PuVoIsT96csmYpS7gC0XmZG5xmUWsTFtauELMysfYi980u7aIS_o9d0LNqRooscE_jxC2n4GopevrtrF_m0IyBBVC8ibvrWdlF4vJ0x2EA2oePoa6l6kfFm4FBcNOnydVoH_tnDtUoHxnmt62JcVlrqtv2tvPngy5vwZaLvh2iDiqtioCsSs3z9pI2_BuOJ0mEQmxhsTwXJsUbiQaJlgYQIDwKGf1WbeNOt4qWONj1oFCzXXMAoUE9wySkdPAc1ot9WFNGzwOUO8cr08UnqJXby3M5ROsEuXchOqgGJChaCwCfcbZH5IkHb5304-KZn69kyeFO8z0nWstdsjejxDyA



curl 192.168.229.134:32542/productpage?my-token=${TOKEN}
```



## 3.13requirement_map

ef-jwt-requirement_map.yaml

kubectl apply -f ef-jwt-requirement_map.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: jwt
spec:
  workloadSelector:
    labels:
      istio: ingressgateway
  priority: 20
  configPatches:
    - applyTo: HTTP_FILTER
      match:
        context: GATEWAY
        listener:
          filterChain:
            filter:
              name: "envoy.filters.network.http_connection_manager"
              subFilter:
                name: "envoy.filters.http.router"
      patch:
        operation: INSERT_BEFORE
        value:
          name: envoy.filters.http.jwt_authn
          typedConfig:
            '@type': type.googleapis.com/envoy.extensions.filters.http.jwt_authn.v3.JwtAuthentication
            providers:
              origins-0:
                jwt_cache_config:
                  jwt_cache_size: 100
                forward: true
                issuer: testing@secure.istio.io
                remote_jwks:
                  http_uri: 
                    uri: jwt-server.istio.svc.cluster.local:8080
                    cluster: outbound|8080||jwt-server.istio.svc.cluster.local
                    timeout: 1s
                  retry_policy:
                    retry_back_off:
                      base_interval: 0.01s
                      max_interval: 20s
                    num_retries: 10
                  cache_duration: 60s
            rules:
            - match:
                prefix: /
              requirement_name: test1
            requirement_map:
              test1:
                requiresAny:
                  requirements:
                  - providerName: origins-0
                  - allow_missing: {}
```

```
TOKEN=eyJraWQiOiJjSWNBZlEzQjhrajJ1Y0pUREFCUzdqVHFWUTVmandLUVY3d2hQTkxGQ1c0IiwidHlwIjoiSldUIiwiYWxnIjoiUlMyNTYifQ.eyJmb28iOiJiYXIiLCJhdWQiOlsiYXBwIiwid2ViIl0sInN1YiI6InRlc3RpbmdAc2VjdXJlLmlzdGlvLmlvIiwidXNlcklkIjoibWFyayIsInNjb3BlIjpbInNjb3BlMSIsInNjb3BlMiJdLCJpc3MiOiJ0ZXN0aW5nQHNlY3VyZS5pc3Rpby5pbyIsImV4cCI6MTY1OTgzNzk5OSwiaWF0IjoxNjU2MzAwNjA4LCJncm91cCI6WyJncm91cDEiLCJncm91cDIiXX0.PuVoIsT96csmYpS7gC0XmZG5xmUWsTFtauELMysfYi980u7aIS_o9d0LNqRooscE_jxC2n4GopevrtrF_m0IyBBVC8ibvrWdlF4vJ0x2EA2oePoa6l6kfFm4FBcNOnydVoH_tnDtUoHxnmt62JcVlrqtv2tvPngy5vwZaLvh2iDiqtioCsSs3z9pI2_BuOJ0mEQmxhsTwXJsUbiQaJlgYQIDwKGf1WbeNOt4qWONj1oFCzXXMAoUE9wySkdPAc1ot9WFNGzwOUO8cr08UnqJXby3M5ROsEuXchOqgGJChaCwCfcbZH5IkHb5304-KZn69kyeFO8z0nWstdsjejxDyA



curl 192.168.229.128:30555/productpage -H "Authorization: Bearer ${TOKEN}"
```



## 3.14PerRouteConfig

ef-jwt-PerRouteConfig.yaml

kubectl apply -f ef-jwt-PerRouteConfig.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: jwt
spec:
  workloadSelector:
    labels:
      istio: ingressgateway
  configPatches:
  - applyTo: NETWORK_FILTER
    match:
      listener:
        #name: 0.0.0.0_8080  
        portNumber: 8080
        filterChain:
          filter:
            name: "envoy.filters.network.http_connection_manager"
    patch:
      operation: MERGE
      value:
        name: envoy.filters.network.http_connection_manager
        typed_config:
          "@type": "type.googleapis.com/envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager"
          codec_type: AUTO
          stat_prefix: ingress_http
          route_config:
            name: local_route
            virtual_hosts:
            - name: www
              domains:
              - "*"
              routes:
              - match:
                  prefix: "/productpage"
                route:
                  cluster: outbound|9080||productpage.istio.svc.cluster.local
                typed_per_filter_config:
                  envoy.filters.http.jwt_authn:
                    "@type": type.googleapis.com/envoy.extensions.filters.http.jwt_authn.v3.PerRouteConfig
                    requirement_name: test1
                    disabled: false
              - match:
                  prefix: "/"
                route:
                  cluster: outbound|9080||productpage.istio.svc.cluster.local
  - applyTo: HTTP_FILTER
    match:
        context: GATEWAY
        listener:
          filterChain:
            filter:
              name: "envoy.filters.network.http_connection_manager"
              subFilter:
                name: "envoy.filters.http.router"
    patch:
        operation: INSERT_BEFORE
        value:
          name: envoy.filters.http.jwt_authn
          typedConfig:
            '@type': type.googleapis.com/envoy.extensions.filters.http.jwt_authn.v3.JwtAuthentication
            providers:
              origins-0:
                forward: true
                issuer: testing@secure.istio.io
                localJwks:
                  inlineString: "{ \"keys\":\n   [ \n     {\n       \"e\":\"AQAB\",\n
                    \      \"kid\":\"DHFbpoIUqrY8t2zpA2qXfCmr5VO5ZEr4RzHU_-envvQ\",\n
                    \      \"kty\":\"RSA\",\n       \"n\":\"xAE7eB6qugXyCAG3yhh7pkDkT65pHymX-P7KfIupjf59vsdo91bSP9C8H07pSAGQO1MV_xFj9VswgsCg4R6otmg5PV2He95lZdHtOcU5DXIg_pbhLdKXbi66GlVeK6ABZOUW3WYtnNHD-91gVuoeJT_DwtGGcp4ignkgXfkiEm4sw-4sfb4qdt5oLbyVpmW6x9cfa7vs2WTfURiCrBoUqgBo_-4WTiULmmHSGZHOjzwa8WtrtOQGsAFjIbno85jp6MnGGGZPYZbDAa_b3y5u-YpW7ypZrvD8BgtKVjgtQgZhLAGezMt0ua3DRrWnKqTZ0BJ_EyxOGuHJrLsn00fnMQ\"\n
                    \    }\n   ]\n}\n"
                payloadInMetadata: testing@secure.istio.io
              origins-1:
                jwt_cache_config:
                  jwt_cache_size: 100
                forward: true
                issuer: testing@secure.istio.io
                remote_jwks:
                  http_uri: 
                    uri: jwt-server.istio.svc.cluster.local:8080
                    cluster: outbound|8080||jwt-server.istio.svc.cluster.local
                    timeout: 1s
                  retry_policy:
                    retry_back_off:
                      base_interval: 0.01s
                      max_interval: 20s
                    num_retries: 10
                  cache_duration: 60s
            rules:
            - match:
                prefix: /
              requires:
                requiresAny:
                  requirements:
                  - providerName: origins-0
                  - allow_missing: {}
            requirement_map:
              test1:
                requiresAny:
                  requirements:
                  - providerName: origins-1
                  - allow_missing: {}
```

```
TOKEN=eyJraWQiOiJjSWNBZlEzQjhrajJ1Y0pUREFCUzdqVHFWUTVmandLUVY3d2hQTkxGQ1c0IiwidHlwIjoiSldUIiwiYWxnIjoiUlMyNTYifQ.eyJmb28iOiJiYXIiLCJhdWQiOlsiYXBwIiwid2ViIl0sInN1YiI6InRlc3RpbmdAc2VjdXJlLmlzdGlvLmlvIiwidXNlcklkIjoibWFyayIsInNjb3BlIjpbInNjb3BlMSIsInNjb3BlMiJdLCJpc3MiOiJ0ZXN0aW5nQHNlY3VyZS5pc3Rpby5pbyIsImV4cCI6MTY1OTgzNzk5OSwiaWF0IjoxNjU2MzAwNjA4LCJncm91cCI6WyJncm91cDEiLCJncm91cDIiXX0.PuVoIsT96csmYpS7gC0XmZG5xmUWsTFtauELMysfYi980u7aIS_o9d0LNqRooscE_jxC2n4GopevrtrF_m0IyBBVC8ibvrWdlF4vJ0x2EA2oePoa6l6kfFm4FBcNOnydVoH_tnDtUoHxnmt62JcVlrqtv2tvPngy5vwZaLvh2iDiqtioCsSs3z9pI2_BuOJ0mEQmxhsTwXJsUbiQaJlgYQIDwKGf1WbeNOt4qWONj1oFCzXXMAoUE9wySkdPAc1ot9WFNGzwOUO8cr08UnqJXby3M5ROsEuXchOqgGJChaCwCfcbZH5IkHb5304-KZn69kyeFO8z0nWstdsjejxDyA



curl 192.168.229.128:30555/productpage -H "Authorization: Bearer ${TOKEN}"


TOKEN=eyJhbGciOiJSUzI1NiIsImtpZCI6IkRIRmJwb0lVcXJZOHQyenBBMnFYZkNtcjVWTzVaRXI0UnpIVV8tZW52dlEiLCJ0eXAiOiJKV1QifQ.eyJleHAiOjM1MzczOTExMDQsImdyb3VwcyI6WyJncm91cDEiLCJncm91cDIiXSwiaWF0IjoxNTM3MzkxMTA0LCJpc3MiOiJ0ZXN0aW5nQHNlY3VyZS5pc3Rpby5pbyIsInNjb3BlIjpbInNjb3BlMSIsInNjb3BlMiJdLCJzdWIiOiJ0ZXN0aW5nQHNlY3VyZS5pc3Rpby5pbyJ9.EdJnEZSH6X8hcyEii7c8H5lnhgjB5dwo07M5oheC8Xz8mOllyg--AHCFWHybM48reunF--oGaG6IXVngCEpVF0_P5DwsUoBgpPmK1JOaKN6_pe9sh0ZwTtdgK_RP01PuI7kUdbOTlkuUi2AO-qUyOm7Art2POzo36DLQlUXv8Ad7NBOqfQaKjE9ndaPWT7aexUsBHxmgiGbz1SyLH879f7uHYPbPKlpHU6P9S-DaKnGLaEchnoKnov7ajhrEhGXAQRukhDPKUHO9L30oPIr5IJllEQfHYtt6IZvlNUGeLUcif3wpry1R5tBXRicx2sXMQ7LyuDremDbcNy_iE76Upg

curl 192.168.229.128:30555/productpage -H "Authorization: Bearer ${TOKEN}"
```

