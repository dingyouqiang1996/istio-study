# 学习目标

![1618892822(1)](images\1618892822(1).jpg)







# 什么是RequestAuthentication

 RequestAuthentication defines what request authentication methods are supported by a workload. It will reject a request if the request contains invalid authentication information, based on the configured authentication rules. A request that does not contain any authentication credentials will be accepted but will not have any authenticated identity. 

就是用来配置jwt的



# 资源详解

| Field      | Type               | Description                                                  | Required |
| ---------- | ------------------ | ------------------------------------------------------------ | -------- |
| `selector` | `WorkloadSelector` | The selector determines the workloads to apply the RequestAuthentication on. If not set, the policy will be applied to all workloads in the same namespace as the policy. | No       |
| `jwtRules` | `JWTRule[]`        | Define the list of JWTs that can be validated at the selected workloads’ proxy. A valid token will be used to extract the authenticated identity. Each rule will be activated only when a token is presented at the location recorgnized by the rule. The token will be validated based on the JWT rule config. If validation fails, the request will be rejected. Note: if more than one token is presented (at different locations), the output principal is nondeterministic. | No       |

## selector

ra-example-productpage.yaml

kubectl apply -f ra-example-productpage.yaml -n istio

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

virtaulservice/vs-bookinfo-star.yaml

kubectl apply -f vs-bookinfo-star.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: bookinfo
spec:
  exportTo:
  - '*'
  hosts:
  - "*"
  gateways:
  - bookinfo-gateway
  http:
  - match:
    - uri:
        exact: /productpage
    - uri:
        prefix: /static
    - uri:
        exact: /login
    - uri:
        exact: /logout
    - uri:
        prefix: /api/v1/products
    route:
    - destination:
        host: productpage.istio.svc.cluster.local
        port:
          number: 9080
```



kubectl apply -f gateway-01.yaml -n istio

gateway/gateway-01.yaml

```
apiVersion: networking.istio.io/v1beta1
kind: Gateway
metadata:
  name: bookinfo-gateway
spec:
  selector:
    istio: ingressgateway
  servers:
  - port:
      number: 80
      name: http
      protocol: HTTP
    hosts:
    - "*"
```



TOKEN=eyJhbGciOiJSUzI1NiIsImtpZCI6IkRIRmJwb0lVcXJZOHQyenBBMnFYZkNtcjVWTzVaRXI0UnpIVV8tZW52dlEiLCJ0eXAiOiJKV1QifQ.eyJleHAiOjM1MzczOTExMDQsImdyb3VwcyI6WyJncm91cDEiLCJncm91cDIiXSwiaWF0IjoxNTM3MzkxMTA0LCJpc3MiOiJ0ZXN0aW5nQHNlY3VyZS5pc3Rpby5pbyIsInNjb3BlIjpbInNjb3BlMSIsInNjb3BlMiJdLCJzdWIiOiJ0ZXN0aW5nQHNlY3VyZS5pc3Rpby5pbyJ9.EdJnEZSH6X8hcyEii7c8H5lnhgjB5dwo07M5oheC8Xz8mOllyg--AHCFWHybM48reunF--oGaG6IXVngCEpVF0_P5DwsUoBgpPmK1JOaKN6_pe9sh0ZwTtdgK_RP01PuI7kUdbOTlkuUi2AO-qUyOm7Art2POzo36DLQlUXv8Ad7NBOqfQaKjE9ndaPWT7aexUsBHxmgiGbz1SyLH879f7uHYPbPKlpHU6P9S-DaKnGLaEchnoKnov7ajhrEhGXAQRukhDPKUHO9L30oPIr5IJllEQfHYtt6IZvlNUGeLUcif3wpry1R5tBXRicx2sXMQ7LyuDremDbcNy_iE76Upg

curl 192.168.198.154:30986/productpage -H "Authorization: Bearer ${TOKEN}"



清理：

kubectl delete vs bookinfo -n istio

kubectl delete gw bookinfo-gateway -n istio

kubectl delete RequestAuthentication productpage -n istio



kubectl port-forward --address 0.0.0.0 -n istio productpage-v1-659776cb44-q8xzp 15000:15000

![1628746225(1)](images\1628746225(1).jpg)





## jwtRules

| Field                   | Type          | Description                                                  | Required |
| ----------------------- | ------------- | ------------------------------------------------------------ | -------- |
| `issuer`                | `string`      | Identifies the issuer that issued the JWT. See [issuer](https://tools.ietf.org/html/rfc7519#section-4.1.1) A JWT with different `iss` claim will be rejected.Example: https://foobar.auth0.com Example: 1234567-compute@developer.gserviceaccount.com | Yes      |
| `audiences`             | `string[]`    | The list of JWT [audiences](https://tools.ietf.org/html/rfc7519#section-4.1.3). that are allowed to access. A JWT containing any of these audiences will be accepted.The service name will be accepted if audiences is empty.Example:`audiences: - bookstore_android.apps.example.com  bookstore_web.apps.example.com ` | No       |
| `jwksUri`               | `string`      | URL of the provider’s public key set to validate signature of the JWT. See [OpenID Discovery](https://openid.net/specs/openid-connect-discovery-1_0.html#ProviderMetadata).Optional if the key set document can either (a) be retrieved from [OpenID Discovery](https://openid.net/specs/openid-connect-discovery-1_0.html) of the issuer or (b) inferred from the email domain of the issuer (e.g. a Google service account).Example: `https://www.googleapis.com/oauth2/v1/certs`Note: Only one of jwks*uri and jwks should be used. jwks*uri will be ignored if it does. | No       |
| `jwks`                  | `string`      | JSON Web Key Set of public keys to validate signature of the JWT. See https://auth0.com/docs/jwks.Note: Only one of jwks*uri and jwks should be used. jwks*uri will be ignored if it does. | No       |
| `fromHeaders`           | `JWTHeader[]` | List of header locations from which JWT is expected. For example, below is the location spec if JWT is expected to be found in `x-jwt-assertion` header, and have “Bearer ” prefix:`  fromHeaders:  - name: x-jwt-assertion    prefix: "Bearer " ` | No       |
| `fromParams`            | `string[]`    | List of query parameters from which JWT is expected. For example, if JWT is provided via query parameter `my_token` (e.g /path?my_token=), the config is:`  fromParams:  - "my_token" ` | No       |
| `outputPayloadToHeader` | `string`      | This field specifies the header name to output a successfully verified JWT payload to the backend. The forwarded data is `base64_encoded(jwt_payload_in_JSON)`. If it is not specified, the payload will not be emitted. | No       |
| `forwardOriginalToken`  | `bool`        | If set to true, the orginal token will be kept for the ustream request. Default is false. | No       |

### issuer

ra-example-productpage.yaml

kubectl apply -f ra-example-productpage.yaml -n istio

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

virtaulservice/vs-bookinfo-star.yaml

kubectl apply -f vs-bookinfo-star.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: bookinfo
spec:
  exportTo:
  - '*'
  hosts:
  - "*"
  gateways:
  - bookinfo-gateway
  http:
  - match:
    - uri:
        exact: /productpage
    - uri:
        prefix: /static
    - uri:
        exact: /login
    - uri:
        exact: /logout
    - uri:
        prefix: /api/v1/products
    route:
    - destination:
        host: productpage.istio.svc.cluster.local
        port:
          number: 9080
```



kubectl apply -f gateway-01.yaml -n istio

gateway/gateway-01.yaml

```
apiVersion: networking.istio.io/v1beta1
kind: Gateway
metadata:
  name: bookinfo-gateway
spec:
  selector:
    istio: ingressgateway
  servers:
  - port:
      number: 80
      name: http
      protocol: HTTP
    hosts:
    - "*"
```



TOKEN=eyJhbGciOiJSUzI1NiIsImtpZCI6IkRIRmJwb0lVcXJZOHQyenBBMnFYZkNtcjVWTzVaRXI0UnpIVV8tZW52dlEiLCJ0eXAiOiJKV1QifQ.eyJleHAiOjM1MzczOTExMDQsImdyb3VwcyI6WyJncm91cDEiLCJncm91cDIiXSwiaWF0IjoxNTM3MzkxMTA0LCJpc3MiOiJ0ZXN0aW5nQHNlY3VyZS5pc3Rpby5pbyIsInNjb3BlIjpbInNjb3BlMSIsInNjb3BlMiJdLCJzdWIiOiJ0ZXN0aW5nQHNlY3VyZS5pc3Rpby5pbyJ9.EdJnEZSH6X8hcyEii7c8H5lnhgjB5dwo07M5oheC8Xz8mOllyg--AHCFWHybM48reunF--oGaG6IXVngCEpVF0_P5DwsUoBgpPmK1JOaKN6_pe9sh0ZwTtdgK_RP01PuI7kUdbOTlkuUi2AO-qUyOm7Art2POzo36DLQlUXv8Ad7NBOqfQaKjE9ndaPWT7aexUsBHxmgiGbz1SyLH879f7uHYPbPKlpHU6P9S-DaKnGLaEchnoKnov7ajhrEhGXAQRukhDPKUHO9L30oPIr5IJllEQfHYtt6IZvlNUGeLUcif3wpry1R5tBXRicx2sXMQ7LyuDremDbcNy_iE76Upg

curl 192.168.198.154:30986/productpage -H "Authorization: Bearer ${TOKEN}"



清理：

kubectl delete vs bookinfo -n istio

kubectl delete gw bookinfo-gateway -n istio

kubectl delete RequestAuthentication productpage -n istio



kubectl port-forward --address 0.0.0.0 -n istio productpage-v1-659776cb44-q8xzp 15000:15000

![1628827835(3)](images\1628827835(3).jpg)





### audiences

ra-productpage-jwtrules-audiences.yaml

kubectl apply -f ra-productpage-jwtrules-audiences.yaml -n istio

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

Audiences in Jwt are not allowed

jwt里没有audience

virtaulservice/vs-bookinfo-star.yaml

kubectl apply -f vs-bookinfo-star.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: bookinfo
spec:
  exportTo:
  - '*'
  hosts:
  - "*"
  gateways:
  - bookinfo-gateway
  http:
  - match:
    - uri:
        exact: /productpage
    - uri:
        prefix: /static
    - uri:
        exact: /login
    - uri:
        exact: /logout
    - uri:
        prefix: /api/v1/products
    route:
    - destination:
        host: productpage.istio.svc.cluster.local
        port:
          number: 9080
```



kubectl apply -f gateway-01.yaml -n istio

gateway/gateway-01.yaml

```
apiVersion: networking.istio.io/v1beta1
kind: Gateway
metadata:
  name: bookinfo-gateway
spec:
  selector:
    istio: ingressgateway
  servers:
  - port:
      number: 80
      name: http
      protocol: HTTP
    hosts:
    - "*"
```



TOKEN=eyJhbGciOiJSUzI1NiIsImtpZCI6IkRIRmJwb0lVcXJZOHQyenBBMnFYZkNtcjVWTzVaRXI0UnpIVV8tZW52dlEiLCJ0eXAiOiJKV1QifQ.eyJleHAiOjM1MzczOTExMDQsImdyb3VwcyI6WyJncm91cDEiLCJncm91cDIiXSwiaWF0IjoxNTM3MzkxMTA0LCJpc3MiOiJ0ZXN0aW5nQHNlY3VyZS5pc3Rpby5pbyIsInNjb3BlIjpbInNjb3BlMSIsInNjb3BlMiJdLCJzdWIiOiJ0ZXN0aW5nQHNlY3VyZS5pc3Rpby5pbyJ9.EdJnEZSH6X8hcyEii7c8H5lnhgjB5dwo07M5oheC8Xz8mOllyg--AHCFWHybM48reunF--oGaG6IXVngCEpVF0_P5DwsUoBgpPmK1JOaKN6_pe9sh0ZwTtdgK_RP01PuI7kUdbOTlkuUi2AO-qUyOm7Art2POzo36DLQlUXv8Ad7NBOqfQaKjE9ndaPWT7aexUsBHxmgiGbz1SyLH879f7uHYPbPKlpHU6P9S-DaKnGLaEchnoKnov7ajhrEhGXAQRukhDPKUHO9L30oPIr5IJllEQfHYtt6IZvlNUGeLUcif3wpry1R5tBXRicx2sXMQ7LyuDremDbcNy_iE76Upg

curl 192.168.198.154:30986/productpage -H "Authorization: Bearer ${TOKEN}"



清理：

kubectl delete vs bookinfo -n istio

kubectl delete gw bookinfo-gateway -n istio

kubectl delete RequestAuthentication productpage -n istio



kubectl port-forward --address 0.0.0.0 -n istio productpage-v1-659776cb44-q8xzp 15000:15000



![1628827891(1)](images\1628827891(1).jpg)





### jwksUri

ra-productpage-jwtrules-jwksUri.yaml

kubectl apply -f ra-productpage-jwtrules-jwksUri.yaml -n istio

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
    jwksUri: http://jwt-server.istio.svc.cluster.local:8000
```



jwt-server.yaml

kubectl apply -f jwt-server.yaml -n istio

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
```

virtaulservice/vs-bookinfo-star.yaml

kubectl apply -f vs-bookinfo-star.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: bookinfo
spec:
  exportTo:
  - '*'
  hosts:
  - "*"
  gateways:
  - bookinfo-gateway
  http:
  - match:
    - uri:
        exact: /productpage
    - uri:
        prefix: /static
    - uri:
        exact: /login
    - uri:
        exact: /logout
    - uri:
        prefix: /api/v1/products
    route:
    - destination:
        host: productpage.istio.svc.cluster.local
        port:
          number: 9080
```



kubectl apply -f gateway-01.yaml -n istio

gateway/gateway-01.yaml

```
apiVersion: networking.istio.io/v1beta1
kind: Gateway
metadata:
  name: bookinfo-gateway
spec:
  selector:
    istio: ingressgateway
  servers:
  - port:
      number: 80
      name: http
      protocol: HTTP
    hosts:
    - "*"
```



TOKEN=eyJhbGciOiJSUzI1NiIsImtpZCI6IkRIRmJwb0lVcXJZOHQyenBBMnFYZkNtcjVWTzVaRXI0UnpIVV8tZW52dlEiLCJ0eXAiOiJKV1QifQ.eyJleHAiOjM1MzczOTExMDQsImdyb3VwcyI6WyJncm91cDEiLCJncm91cDIiXSwiaWF0IjoxNTM3MzkxMTA0LCJpc3MiOiJ0ZXN0aW5nQHNlY3VyZS5pc3Rpby5pbyIsInNjb3BlIjpbInNjb3BlMSIsInNjb3BlMiJdLCJzdWIiOiJ0ZXN0aW5nQHNlY3VyZS5pc3Rpby5pbyJ9.EdJnEZSH6X8hcyEii7c8H5lnhgjB5dwo07M5oheC8Xz8mOllyg--AHCFWHybM48reunF--oGaG6IXVngCEpVF0_P5DwsUoBgpPmK1JOaKN6_pe9sh0ZwTtdgK_RP01PuI7kUdbOTlkuUi2AO-qUyOm7Art2POzo36DLQlUXv8Ad7NBOqfQaKjE9ndaPWT7aexUsBHxmgiGbz1SyLH879f7uHYPbPKlpHU6P9S-DaKnGLaEchnoKnov7ajhrEhGXAQRukhDPKUHO9L30oPIr5IJllEQfHYtt6IZvlNUGeLUcif3wpry1R5tBXRicx2sXMQ7LyuDremDbcNy_iE76Upg

curl 192.168.198.154:30986/productpage -H "Authorization: Bearer ${TOKEN}"



清理：

kubectl delete vs bookinfo -n istio

kubectl delete gw bookinfo-gateway -n istio

kubectl delete RequestAuthentication productpage -n istio

kubectl delete -f jwt-server.yaml -n istio



已通过jwksUrl 获取local_jwks

kubectl port-forward --address 0.0.0.0 -n istio productpage-v1-659776cb44-q8xzp 15000:15000

![1628828358(1)](images\1628828358(1).jpg)







### jwks

ra-example-productpage.yaml

kubectl apply -f ra-example-productpage.yaml -n istio

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

virtaulservice/vs-bookinfo-star.yaml

kubectl apply -f vs-bookinfo-star.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: bookinfo
spec:
  exportTo:
  - '*'
  hosts:
  - "*"
  gateways:
  - bookinfo-gateway
  http:
  - match:
    - uri:
        exact: /productpage
    - uri:
        prefix: /static
    - uri:
        exact: /login
    - uri:
        exact: /logout
    - uri:
        prefix: /api/v1/products
    route:
    - destination:
        host: productpage.istio.svc.cluster.local
        port:
          number: 9080
```



kubectl apply -f gateway-01.yaml -n istio

gateway/gateway-01.yaml

```
apiVersion: networking.istio.io/v1beta1
kind: Gateway
metadata:
  name: bookinfo-gateway
spec:
  selector:
    istio: ingressgateway
  servers:
  - port:
      number: 80
      name: http
      protocol: HTTP
    hosts:
    - "*"
```



TOKEN=eyJhbGciOiJSUzI1NiIsImtpZCI6IkRIRmJwb0lVcXJZOHQyenBBMnFYZkNtcjVWTzVaRXI0UnpIVV8tZW52dlEiLCJ0eXAiOiJKV1QifQ.eyJleHAiOjM1MzczOTExMDQsImdyb3VwcyI6WyJncm91cDEiLCJncm91cDIiXSwiaWF0IjoxNTM3MzkxMTA0LCJpc3MiOiJ0ZXN0aW5nQHNlY3VyZS5pc3Rpby5pbyIsInNjb3BlIjpbInNjb3BlMSIsInNjb3BlMiJdLCJzdWIiOiJ0ZXN0aW5nQHNlY3VyZS5pc3Rpby5pbyJ9.EdJnEZSH6X8hcyEii7c8H5lnhgjB5dwo07M5oheC8Xz8mOllyg--AHCFWHybM48reunF--oGaG6IXVngCEpVF0_P5DwsUoBgpPmK1JOaKN6_pe9sh0ZwTtdgK_RP01PuI7kUdbOTlkuUi2AO-qUyOm7Art2POzo36DLQlUXv8Ad7NBOqfQaKjE9ndaPWT7aexUsBHxmgiGbz1SyLH879f7uHYPbPKlpHU6P9S-DaKnGLaEchnoKnov7ajhrEhGXAQRukhDPKUHO9L30oPIr5IJllEQfHYtt6IZvlNUGeLUcif3wpry1R5tBXRicx2sXMQ7LyuDremDbcNy_iE76Upg

curl 192.168.198.154:30986/productpage -H "Authorization: Bearer ${TOKEN}"



清理：

kubectl delete vs bookinfo -n istio

kubectl delete gw bookinfo-gateway -n istio

kubectl delete RequestAuthentication productpage -n istio



kubectl port-forward --address 0.0.0.0 -n istio productpage-v1-659776cb44-q8xzp 15000:15000

![1628828467(1)](images\1628828467(1).jpg)





### fromHeaders

| ield     | Type     | Description                                                  | Required |
| -------- | -------- | ------------------------------------------------------------ | -------- |
| `name`   | `string` | The HTTP header name.                                        | Yes      |
| `prefix` | `string` | The prefix that should be stripped before decoding the token. For example, for “Authorization: Bearer ”, prefix=“Bearer ” with a space at the end. If the header doesn’t have this exact prefix, it is considerred invalid. | No       |

ra-productpage-jwtrules-fromHeaders.yaml

kubectl apply -f ra-productpage-jwtrules-fromHeaders.yaml -n istio

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

 virtaulservice/vs-bookinfo-star.yaml

kubectl apply -f vs-bookinfo-star.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: bookinfo
spec:
  exportTo:
  - '*'
  hosts:
  - "*"
  gateways:
  - bookinfo-gateway
  http:
  - match:
    - uri:
        exact: /productpage
    - uri:
        prefix: /static
    - uri:
        exact: /login
    - uri:
        exact: /logout
    - uri:
        prefix: /api/v1/products
    route:
    - destination:
        host: productpage.istio.svc.cluster.local
        port:
          number: 9080
```



kubectl apply -f gateway-01.yaml -n istio

gateway/gateway-01.yaml

```
apiVersion: networking.istio.io/v1beta1
kind: Gateway
metadata:
  name: bookinfo-gateway
spec:
  selector:
    istio: ingressgateway
  servers:
  - port:
      number: 80
      name: http
      protocol: HTTP
    hosts:
    - "*"
```



TOKEN=eyJhbGciOiJSUzI1NiIsImtpZCI6IkRIRmJwb0lVcXJZOHQyenBBMnFYZkNtcjVWTzVaRXI0UnpIVV8tZW52dlEiLCJ0eXAiOiJKV1QifQ.eyJleHAiOjM1MzczOTExMDQsImdyb3VwcyI6WyJncm91cDEiLCJncm91cDIiXSwiaWF0IjoxNTM3MzkxMTA0LCJpc3MiOiJ0ZXN0aW5nQHNlY3VyZS5pc3Rpby5pbyIsInNjb3BlIjpbInNjb3BlMSIsInNjb3BlMiJdLCJzdWIiOiJ0ZXN0aW5nQHNlY3VyZS5pc3Rpby5pbyJ9.EdJnEZSH6X8hcyEii7c8H5lnhgjB5dwo07M5oheC8Xz8mOllyg--AHCFWHybM48reunF--oGaG6IXVngCEpVF0_P5DwsUoBgpPmK1JOaKN6_pe9sh0ZwTtdgK_RP01PuI7kUdbOTlkuUi2AO-qUyOm7Art2POzo36DLQlUXv8Ad7NBOqfQaKjE9ndaPWT7aexUsBHxmgiGbz1SyLH879f7uHYPbPKlpHU6P9S-DaKnGLaEchnoKnov7ajhrEhGXAQRukhDPKUHO9L30oPIr5IJllEQfHYtt6IZvlNUGeLUcif3wpry1R5tBXRicx2sXMQ7LyuDremDbcNy_iE76Upg

curl 192.168.198.154:30986/productpage -H "my-token: test ${TOKEN}"



清理：

kubectl delete vs bookinfo -n istio

kubectl delete gw bookinfo-gateway -n istio

kubectl delete RequestAuthentication productpage -n istio



kubectl port-forward --address 0.0.0.0 -n istio productpage-v1-659776cb44-q8xzp 15000:15000

![1628828520(1)](images\1628828520(1).jpg)





### fromParams

ra-productpage-jwtrules-fromParams.yaml

kubectl apply -f ra-productpage-jwtrules-fromParams.yaml -n istio

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



virtaulservice/vs-bookinfo-star.yaml

kubectl apply -f vs-bookinfo-star.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: bookinfo
spec:
  exportTo:
  - '*'
  hosts:
  - "*"
  gateways:
  - bookinfo-gateway
  http:
  - match:
    - uri:
        exact: /productpage
    - uri:
        prefix: /static
    - uri:
        exact: /login
    - uri:
        exact: /logout
    - uri:
        prefix: /api/v1/products
    route:
    - destination:
        host: productpage.istio.svc.cluster.local
        port:
          number: 9080
```



kubectl apply -f gateway-01.yaml -n istio

gateway/gateway-01.yaml

```
apiVersion: networking.istio.io/v1beta1
kind: Gateway
metadata:
  name: bookinfo-gateway
spec:
  selector:
    istio: ingressgateway
  servers:
  - port:
      number: 80
      name: http
      protocol: HTTP
    hosts:
    - "*"
```



TOKEN=eyJhbGciOiJSUzI1NiIsImtpZCI6IkRIRmJwb0lVcXJZOHQyenBBMnFYZkNtcjVWTzVaRXI0UnpIVV8tZW52dlEiLCJ0eXAiOiJKV1QifQ.eyJleHAiOjM1MzczOTExMDQsImdyb3VwcyI6WyJncm91cDEiLCJncm91cDIiXSwiaWF0IjoxNTM3MzkxMTA0LCJpc3MiOiJ0ZXN0aW5nQHNlY3VyZS5pc3Rpby5pbyIsInNjb3BlIjpbInNjb3BlMSIsInNjb3BlMiJdLCJzdWIiOiJ0ZXN0aW5nQHNlY3VyZS5pc3Rpby5pbyJ9.EdJnEZSH6X8hcyEii7c8H5lnhgjB5dwo07M5oheC8Xz8mOllyg--AHCFWHybM48reunF--oGaG6IXVngCEpVF0_P5DwsUoBgpPmK1JOaKN6_pe9sh0ZwTtdgK_RP01PuI7kUdbOTlkuUi2AO-qUyOm7Art2POzo36DLQlUXv8Ad7NBOqfQaKjE9ndaPWT7aexUsBHxmgiGbz1SyLH879f7uHYPbPKlpHU6P9S-DaKnGLaEchnoKnov7ajhrEhGXAQRukhDPKUHO9L30oPIr5IJllEQfHYtt6IZvlNUGeLUcif3wpry1R5tBXRicx2sXMQ7LyuDremDbcNy_iE76Upg

curl 192.168.198.154:30986/productpage?my-token=${TOKEN}



清理：

kubectl delete vs bookinfo -n istio

kubectl delete gw bookinfo-gateway -n istio

kubectl delete RequestAuthentication productpage -n istio



kubectl port-forward --address 0.0.0.0 -n istio productpage-v1-659776cb44-q8xzp 15000:15000

![1628828582(1)](images\1628828582(1).jpg)





### outputPayloadToHeader

ra-productpage-jwtrules-outputPayloadToHeader.yaml

kubectl apply -f ra-productpage-jwtrules-outputPayloadToHeader.yaml -n istio

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

virtaulservice/vs-bookinfo-star.yaml

kubectl apply -f vs-bookinfo-star.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: bookinfo
spec:
  exportTo:
  - '*'
  hosts:
  - "*"
  gateways:
  - bookinfo-gateway
  http:
  - match:
    - uri:
        exact: /productpage
    - uri:
        prefix: /static
    - uri:
        exact: /login
    - uri:
        exact: /logout
    - uri:
        prefix: /api/v1/products
    route:
    - destination:
        host: productpage.istio.svc.cluster.local
        port:
          number: 9080
```



kubectl apply -f gateway-01.yaml -n istio

gateway/gateway-01.yaml

```
apiVersion: networking.istio.io/v1beta1
kind: Gateway
metadata:
  name: bookinfo-gateway
spec:
  selector:
    istio: ingressgateway
  servers:
  - port:
      number: 80
      name: http
      protocol: HTTP
    hosts:
    - "*"
```



TOKEN=eyJhbGciOiJSUzI1NiIsImtpZCI6IkRIRmJwb0lVcXJZOHQyenBBMnFYZkNtcjVWTzVaRXI0UnpIVV8tZW52dlEiLCJ0eXAiOiJKV1QifQ.eyJleHAiOjM1MzczOTExMDQsImdyb3VwcyI6WyJncm91cDEiLCJncm91cDIiXSwiaWF0IjoxNTM3MzkxMTA0LCJpc3MiOiJ0ZXN0aW5nQHNlY3VyZS5pc3Rpby5pbyIsInNjb3BlIjpbInNjb3BlMSIsInNjb3BlMiJdLCJzdWIiOiJ0ZXN0aW5nQHNlY3VyZS5pc3Rpby5pbyJ9.EdJnEZSH6X8hcyEii7c8H5lnhgjB5dwo07M5oheC8Xz8mOllyg--AHCFWHybM48reunF--oGaG6IXVngCEpVF0_P5DwsUoBgpPmK1JOaKN6_pe9sh0ZwTtdgK_RP01PuI7kUdbOTlkuUi2AO-qUyOm7Art2POzo36DLQlUXv8Ad7NBOqfQaKjE9ndaPWT7aexUsBHxmgiGbz1SyLH879f7uHYPbPKlpHU6P9S-DaKnGLaEchnoKnov7ajhrEhGXAQRukhDPKUHO9L30oPIr5IJllEQfHYtt6IZvlNUGeLUcif3wpry1R5tBXRicx2sXMQ7LyuDremDbcNy_iE76Upg

curl 192.168.198.154:30986/productpage?my-token=${TOKEN}



清理：

kubectl delete vs bookinfo -n istio

kubectl delete gw bookinfo-gateway -n istio

kubectl delete RequestAuthentication productpage -n istio



kubectl port-forward --address 0.0.0.0 -n istio productpage-v1-659776cb44-q8xzp 15000:15000

![1628828669(1)](images\1628828669(1).jpg)





### forwardOriginalToken

1启用网关jwt

网关不启用token转发

kubectl apply -f ra-example.yaml -n istio-system

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
   # forwardOriginalToken: true  # 转发 Authorization 请求头
```

virtaulservice/vs-bookinfo-star.yaml

kubectl apply -f vs-bookinfo-star.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: bookinfo
spec:
  exportTo:
  - '*'
  hosts:
  - "*"
  gateways:
  - bookinfo-gateway
  http:
  - match:
    - uri:
        exact: /productpage
    - uri:
        prefix: /static
    - uri:
        exact: /login
    - uri:
        exact: /logout
    - uri:
        prefix: /api/v1/products
    route:
    - destination:
        host: productpage.istio.svc.cluster.local
        port:
          number: 9080
```



kubectl apply -f gateway-01.yaml -n istio

gateway/gateway-01.yaml

```
apiVersion: networking.istio.io/v1beta1
kind: Gateway
metadata:
  name: bookinfo-gateway
spec:
  selector:
    istio: ingressgateway
  servers:
  - port:
      number: 80
      name: http
      protocol: HTTP
    hosts:
    - "*"
```



启用productpage jwt

ra-productpage-jwtrules-forwardOriginalToken.yaml

kubectl apply -f ra-productpage-jwtrules-forwardOriginalToken.yaml -n istio

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
  - from:
    - source:
        notRequestPrincipals: ["*"]

```

curl 192.168.198.154:30986/productpage -H "Authorization: Bearer ${TOKEN}"

访问失败

3修改网关jwt

网关启用转发token



ra-example-forwardOriginalToken.yaml

kubectl apply -f ra-example-forwardOriginalToken.yaml -n istio-system

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

4访问

curl 192.168.198.154:30986/productpage -H "Authorization: Bearer ${TOKEN}"

成功



清理：

kubectl delete vs bookinfo -n istio

kubectl delete gw bookinfo-gateway -n istio

kubectl delete RequestAuthentication productpage -n istio

kubectl delete  RequestAuthentication   jwt-example -n istio-system



 kubectl port-forward --address 0.0.0.0 -n istio-system istio-ingressgateway-8657768d87-bd767 15002:15000

![1628828917(1)](images\1628828917(1).jpg)



