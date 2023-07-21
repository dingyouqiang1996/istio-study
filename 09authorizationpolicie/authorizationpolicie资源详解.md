# 学习目标

# ![1618469480(1)](images\1618469609(1).jpg)

# 9-1什么是AuthorizationPolicy

授权功能是 Istio 中安全体系的一个重要组成部分，它用来实现访问控制的功能，即判断一个请求是否允许通过，这个请求可以是从外部进入 Istio 内部的请求，也可以是在 Istio 内部从服务 A 到服务 B 的请求。可以把授权功能近似地认为是一种四层到七层的“防火墙”，它会像传统防火墙一样，对数据流进行分析和匹配，然后执行相应的动作。

# 流程

 Authorization policies support `ALLOW`, `DENY` and `CUSTOM` actions. The policy precedence is `CUSTOM`, `DENY` and `ALLOW`. The following graph shows the policy precedence in detail: 

![authz-eval](images\authz-eval.png)

# 资源详解

| Field      | Type                        | Description                                                  | Required |
| ---------- | --------------------------- | ------------------------------------------------------------ | -------- |
| `selector` | `WorkloadSelector`          | Optional. Workload selector decides where to apply the authorization policy. If not set, the authorization policy will be applied to all workloads in the same namespace as the authorization policy. | No       |
| `rules`    | `Rule[]`                    | Optional. A list of rules to match the request. A match occurs when at least one rule matches the request.If not set, the match will never occur. This is equivalent to setting a default of deny for the target workloads. | No       |
| `action`   | `Action`                    | Optional. The action to take if the request is matched with the rules. | No       |
| `provider` | `ExtensionProvider (oneof)` | Specifies detailed configuration of the CUSTOM action. Must be used only with CUSTOM action. | No       |

## 9-2允许nothing

allow-nothing.yaml

所有名称空间

kubectl apply -f allow-nothing.yaml -n istio-system

istio名称空间

kubectl apply -f allow-nothing.yaml -n istio

```
apiVersion: security.istio.io/v1beta1
kind: AuthorizationPolicy
metadata:
  name: allow-nothing
spec:
  # This matches nothing, the action defaults to ALLOW if not specified.
  {}
```

 The following example shows an `ALLOW` policy that matches nothing. If there are no other `ALLOW` policies, requests will always be denied because of the “deny by default” behavior. 

默认拒绝，有通过则通过

gateway/gateway-01.yaml

kubectl apply -f gateway-01.yaml -n istio

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



清理：

kubectl delete gw bookinfo-gateway -n istio

kubectl delete vs bookinfo -n istio

kubectl delete AuthorizationPolicy allow-nothing -n istio-system

kubectl delete AuthorizationPolicy allow-nothing -n istio

![1628473489](images\1628473489.jpg)







## 全局拒绝所有

kubectl apply -f global-deny-all.yaml -n istio-system

global-deny-all.yaml

```
apiVersion: security.istio.io/v1beta1
kind: AuthorizationPolicy
metadata:
  name: deny-all
  namespace: istio-system
spec:
  action: DENY
  # This matches everything.
  rules:
  - {}
```

gateway/gateway-01.yaml

kubectl apply -f gateway-01.yaml -n istio

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



清理：

kubectl delete gw bookinfo-gateway -n istio

kubectl delete vs bookinfo -n istio

kubectl delete AuthorizationPolicy deny-all -n istio-system

![1628473828(1)](images\1628473828(1).jpg)





## 名称空间拒绝所有

deny-all.yaml

kubectl  apply -f deny-all.yaml -n istio

```
apiVersion: security.istio.io/v1beta1
kind: AuthorizationPolicy
metadata:
  name: deny-all
spec:
  action: DENY
  # This matches everything.
  rules:
  - {}
```

名称空间级别

gateway/gateway-01.yaml

kubectl apply -f gateway-01.yaml -n istio

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



清理：

kubectl delete gw bookinfo-gateway -n istio

kubectl delete vs bookinfo -n istio

kubectl delete AuthorizationPolicy deny-all -n istio



kubectl port-forward --address 0.0.0.0 -n istio productpage-v1-659776cb44-rrr87 15000:15000

![1628474082(1)](images\1628474082(1).jpg)



## 名称空间允许所有

allow-all.yaml

kubectl apply -f allow-all.yaml -n istio

```
apiVersion: security.istio.io/v1beta1
kind: AuthorizationPolicy
metadata:
 name: allow-all
spec:
 action: ALLOW
 rules:
 - {}
```

名称空间级别

gateway/gateway-01.yaml

kubectl apply -f gateway-01.yaml -n istio

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



清理：

kubectl delete gw bookinfo-gateway -n istio

kubectl delete vs bookinfo -n istio

kubectl delete AuthorizationPolicy allow-all -n istio



kubectl port-forward --address 0.0.0.0 -n istio productpage-v1-659776cb44-rrr87 15000:15000

![1628474232(1)](images\1628474232(1).jpg)





## 9-3action

| Name     | Description                                                  |
| -------- | ------------------------------------------------------------ |
| `ALLOW`  | Allow a request only if it matches the rules. This is the default type. |
| `DENY`   | Deny a request if it matches any of the rules.               |
| `AUDIT`  | Audit a request if it matches any of the rules.              |
| `CUSTOM` | The CUSTOM action allows an extension to handle the user request if the matching rules evaluate to true. The extension is evaluated independently and before the native ALLOW and DENY actions. When used together, A request is allowed if and only if all the actions return allow, in other words, the extension cannot bypass the authorization decision made by ALLOW and DENY action. Extension behavior is defined by the named providers declared in MeshConfig. The authorization policy refers to the extension by specifying the name of the provider. One example use case of the extension is to integrate with a custom external authorization system to delegate the authorization decision to it.Note: The CUSTOM action is currently an **experimental feature** and is subject to breaking changes in later versions. |

### ALLOW

productpage-allow-all.yaml 

kubectl apply -f productpage-allow-all.yaml  -n istio

```
apiVersion: security.istio.io/v1beta1
kind: AuthorizationPolicy
metadata:
 name: productpage-allow-all
spec:
 selector:
   matchLabels:
     app: productpage
     version: v1
 action: ALLOW
 rules:
 - to:
   - operation:
       methods: ["GET", "POST"]
```

allow-nothing.yaml

所有名称空间

kubectl apply -f allow-nothing.yaml -n istio

```
apiVersion: security.istio.io/v1beta1
kind: AuthorizationPolicy
metadata:
  name: allow-nothing
spec:
  # This matches nothing, the action defaults to ALLOW if not specified.
  {}
```

 

gateway/gateway-01.yaml

kubectl apply -f gateway-01.yaml -n istio

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



清理：

kubectl delete gw bookinfo-gateway -n istio

kubectl delete vs bookinfo -n istio

kubectl delete AuthorizationPolicy productpage-allow-all  -n istio

kubectl delete   AuthorizationPolicy  allow-nothing -n istio-system



kubectl port-forward --address 0.0.0.0 -n istio productpage-v1-659776cb44-rrr87 15000:15000

![1628477919(1)](images\1628477919(1).jpg)





### DENY

禁止访问produtpage

productpage-deny-allyaml

kubectl apply -f productpage-deny-allyaml -n istio

```
apiVersion: security.istio.io/v1beta1
kind: AuthorizationPolicy
metadata:
 name: productpage-allow-all
spec:
 selector:
   matchLabels:
     app: productpage
     version: v1
 action: DENY
 rules:
 - {}
```

gateway/gateway-01.yaml

kubectl apply -f gateway-01.yaml -n istio

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



清理：

kubectl delete gw bookinfo-gateway -n istio

kubectl delete vs bookinfo -n istio

kubectl delete AuthorizationPolicy productpage-allow-all  -n istio



kubectl port-forward --address 0.0.0.0 -n istio productpage-v1-659776cb44-rrr87 15000:15000



![1628478113(1)](images\1628478113(1).jpg)





### AUDIT

productpage-audit-all.yaml

kubectl  apply -f productpage-audit-all.yaml -n istio

```
apiVersion: security.istio.io/v1beta1
kind: AuthorizationPolicy
metadata:
 name: productpage-allow-all
spec:
 selector:
   matchLabels:
     app: productpage
     version: v1
 action: AUDIT
 rules:
 - {}
```

 the only supported plugin is the [Stackdriver](https://istio.io/latest/docs/reference/config/proxy_extensions/stackdriver/) plugin 

需要安装audit插件

gateway/gateway-01.yaml

kubectl apply -f gateway-01.yaml -n istio

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



清理：

kubectl delete gw bookinfo-gateway -n istio

kubectl delete vs bookinfo -n istio

kubectl delete AuthorizationPolicy productpage-allow-all  -n istio





kubectl port-forward --address 0.0.0.0 -n istio productpage-v1-659776cb44-rrr87 15000:15000



![1628478188(1)](images\1628478188(1).jpg)





### CUSTOM

 The CUSTOM action is currently an **experimental feature** and is subject to breaking changes in later versions. 

1创建opa策略

opa介绍

http://blog.newbmiao.com/2020/03/13/opa-quick-start.html

验证opa

https://play.openpolicyagent.org/p/ZXkIlAEPCY



policy.rego 

```
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

```

2创建secret

  kubectl create secret generic opa-policy --from-file policy.rego  -n istio

3创建opa

opa-deployment.yaml

kubectl apply -f opa-deployment.yaml -n istio

```
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

5闯将ap

ext-authz.yaml

kubectl apply -f ext-authz.yaml -n istio-system

```
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
```

gateway/gateway-01.yaml

kubectl apply -f gateway-01.yaml -n istio

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





6测试

TOKEN=eyJhbGciOiJSUzI1NiIsImtpZCI6IkRIRmJwb0lVcXJZOHQyenBBMnFYZkNtcjVWTzVaRXI0UnpIVV8tZW52dlEiLCJ0eXAiOiJKV1QifQ.eyJleHAiOjQ2ODU5ODk3MDAsImZvbyI6ImJhciIsImlhdCI6MTUzMjM4OTcwMCwiaXNzIjoidGVzdGluZ0BzZWN1cmUuaXN0aW8uaW8iLCJzdWIiOiJ0ZXN0aW5nQHNlY3VyZS5pc3Rpby5pbyJ9.CfNnxWP2tcnR9q0vxyxweaF3ovQYHYZl82hAUsn21bwQd9zP7c-LS9qd_vpdLG4Tn1A15NxfCjp5f7QNBUo-KC9PJqYpgGbaXhaGx7bEdFWjcwv3nZzvc7M__ZpaCERdwU7igUmJqYGBYQ51vr2njU9ZimyKkfDe3axcyiBZde7G6dabliUosJvvKOPcKIWPccCgefSj_GNfwIip3-SsFdlR7BtbVUcqR-yv-XOxJ3Uc1MI0tz3uMiiZcyPV7sNCU4KRnemRIMHVOfuvHsU60_GhGbiSFzgPTAa9WTltbnarTbxudb_YEOx12JiwYToeX0DCPb43W1tzIBxgm8NxUg

curl 192.168.198.154:30986/productpage -H "Authorization: Bearer ${TOKEN}"



TOKEN=eyJhbGciOiJSUzI1NiIsImtpZCI6IkRIRmJwb0lVcXJZOHQyenBBMnFYZkNtcjVWTzVaRXI0UnpIVV8tZW52dlEiLCJ0eXAiOiJKV1QifQ.eyJleHAiOjM1MzczOTExMDQsImdyb3VwcyI6WyJncm91cDEiLCJncm91cDIiXSwiaWF0IjoxNTM3MzkxMTA0LCJpc3MiOiJ0ZXN0aW5nQHNlY3VyZS5pc3Rpby5pbyIsInNjb3BlIjpbInNjb3BlMSIsInNjb3BlMiJdLCJzdWIiOiJ0ZXN0aW5nQHNlY3VyZS5pc3Rpby5pbyJ9.EdJnEZSH6X8hcyEii7c8H5lnhgjB5dwo07M5oheC8Xz8mOllyg--AHCFWHybM48reunF--oGaG6IXVngCEpVF0_P5DwsUoBgpPmK1JOaKN6_pe9sh0ZwTtdgK_RP01PuI7kUdbOTlkuUi2AO-qUyOm7Art2POzo36DLQlUXv8Ad7NBOqfQaKjE9ndaPWT7aexUsBHxmgiGbz1SyLH879f7uHYPbPKlpHU6P9S-DaKnGLaEchnoKnov7ajhrEhGXAQRukhDPKUHO9L30oPIr5IJllEQfHYtt6IZvlNUGeLUcif3wpry1R5tBXRicx2sXMQ7LyuDremDbcNy_iE76Upg

curl 192.168.198.154:30986/productpage -H "Authorization: Bearer ${TOKEN}"



清理：

kubectl delete gw bookinfo-gateway -n istio

kubectl delete vs bookinfo -n istio

kubectl delete AuthorizationPolicy ext-authz  -n istio-system

kubectl delete -f opa-deployment.yaml -n istio

 kubectl delete secret opa-policy  -n istio



![1628478699(1)](images\1628478699(1).jpg)



## rules

| Field  | Type          | Description                                                  | Required |
| ------ | ------------- | ------------------------------------------------------------ | -------- |
| `from` | `From[]`      | Optional. from specifies the source of a request.If not set, any source is allowed. | No       |
| `to`   | `To[]`        | Optional. to specifies the operation of a request.If not set, any operation is allowed. | No       |
| `when` | `Condition[]` | Optional. when specifies a list of additional conditions of a request.If not set, any condition is allowed. | No       |

### 9-4from

| Field    | Type     | Description                               | Required |
| -------- | -------- | ----------------------------------------- | -------- |
| `source` | `Source` | Source specifies the source of a request. | No       |

| Field                  | Type       | Description                                                  | Required |
| ---------------------- | ---------- | ------------------------------------------------------------ | -------- |
| `principals`           | `string[]` | Optional. A list of source peer identities (i.e. service account), which matches to the “source.principal” attribute. This field requires mTLS enabled.If not set, any principal is allowed. | No       |
| `notPrincipals`        | `string[]` | Optional. A list of negative match of source peer identities. | No       |
| `requestPrincipals`    | `string[]` | Optional. A list of request identities (i.e. “iss/sub” claims), which matches to the “request.auth.principal” attribute.If not set, any request principal is allowed. | No       |
| `notRequestPrincipals` | `string[]` | Optional. A list of negative match of request identities.    | No       |
| `namespaces`           | `string[]` | Optional. A list of namespaces, which matches to the “source.namespace” attribute. This field requires mTLS enabled.If not set, any namespace is allowed. | No       |
| `notNamespaces`        | `string[]` | Optional. A list of negative match of namespaces.            | No       |
| `ipBlocks`             | `string[]` | Optional. A list of IP blocks, which matches to the “source.ip” attribute. Populated from the source address of the IP packet. Single IP (e.g. “1.2.3.4”) and CIDR (e.g. “1.2.3.0/24”) are supported.If not set, any IP is allowed. | No       |
| `notIpBlocks`          | `string[]` | Optional. A list of negative match of IP blocks.             | No       |
| `remoteIpBlocks`       | `string[]` | Optional. A list of IP blocks, which matches to the “remote.ip” attribute. Populated from X-Forwarded-For header or proxy protocol. To make use of this field, you must configure the numTrustedProxies field of the gatewayTopology under the meshConfig when you install Istio or using an annotation on the ingress gateway. See the documentation here: [Configuring Gateway Network Topology](https://istio.io/latest/docs/ops/configuration/traffic-management/network-topologies/). Single IP (e.g. “1.2.3.4”) and CIDR (e.g. “1.2.3.0/24”) are supported.If not set, any IP is allowed. | No       |
| `notRemoteIpBlocks`    | `string[]` | Optional. A list of negative match of remote IP blocks.      | No       |

#### principals

productpage-rules-from-principals.yaml

kubectl apply -f productpage-rules-from-principals.yaml -n istio

```
apiVersion: security.istio.io/v1beta1
kind: AuthorizationPolicy
metadata:
 name: productpage
spec:
 selector:
   matchLabels:
     app: productpage
     version: v1
 action: ALLOW
 rules:
 - from:
     - source:
         principals: ["cluster.local/ns/istio-system/sa/istio-ingressgateway-service-account"]
```

allow-nothing.yaml

所有名称空间

kubectl apply -f allow-nothing.yaml -n istio-system

```
apiVersion: security.istio.io/v1beta1
kind: AuthorizationPolicy
metadata:
  name: allow-nothing
spec:
  # This matches nothing, the action defaults to ALLOW if not specified.
  {}
```

 

gateway/gateway-01.yaml

kubectl apply -f gateway-01.yaml -n istio

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



清理：

kubectl delete gw bookinfo-gateway -n istio

kubectl delete vs bookinfo -n istio

kubectl delete AuthorizationPolicy productpage  -n istio

kubectl delete   AuthorizationPolicy  allow-nothing -n istio-system



kubectl port-forward --address 0.0.0.0 -n istio productpage-v1-659776cb44-rrr87 15000:15000





![1628478955(1)](images\1628478955(1).jpg)





#### notPrincipals

productpage-rules-from-notPrincipals.yaml

kubectl apply -f productpage-rules-from-notPrincipals.yaml -n istio

```
apiVersion: security.istio.io/v1beta1
kind: AuthorizationPolicy
metadata:
 name: productpage
spec:
 selector:
   matchLabels:
     app: productpage
     version: v1
 action: ALLOW
 rules:
 - from:
     - source:
         notPrincipals: ["cluster.local/ns/istio-system/sa/test"]
```

allow-nothing.yaml

所有名称空间

kubectl apply -f allow-nothing.yaml -n istio-system

```
apiVersion: security.istio.io/v1beta1
kind: AuthorizationPolicy
metadata:
  name: allow-nothing
spec:
  # This matches nothing, the action defaults to ALLOW if not specified.
  {}
```

 

gateway/gateway-01.yaml

kubectl apply -f gateway-01.yaml -n istio

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



清理：

kubectl delete gw bookinfo-gateway -n istio

kubectl delete vs bookinfo -n istio

kubectl delete AuthorizationPolicy productpage  -n istio

kubectl delete   AuthorizationPolicy  allow-nothing -n istio-system



kubectl port-forward --address 0.0.0.0 -n istio productpage-v1-659776cb44-rrr87 15000:15000



![1628479095(1)](images\1628479095(1).jpg)





#### requestPrincipals

 The principal of the authenticated JWT token, constructed from the JWT claims in the format of `/`, requires request authentication policy applied 

jwt相关

productpage-rules-from-requestPrincipals-star.yaml

kubectl apply -f productpage-rules-from-requestPrincipals-star.yaml -n istio

```
apiVersion: security.istio.io/v1beta1
kind: AuthorizationPolicy
metadata:
  name: productpage
spec:
  selector:
    matchLabels:
      app: productpage
  action: ALLOW
  rules:
  - to:
    - operation:
        notPaths: ["/healthz"]
    from:
    - source:
        requestPrincipals: ["*"]
```

1启用jwt

requestauthentications/ra-example-productpage.yaml

kubectl apply -f ra-example-productpage.yaml -n istio

```
apiVersion: "security.istio.io/v1beta1"
kind: "RequestAuthentication"
metadata:
  name: "jwt-example"
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
    forwardOriginalToken: true
```

allow-nothing.yaml

所有名称空间

kubectl apply -f allow-nothing.yaml -n istio-system

```
apiVersion: security.istio.io/v1beta1
kind: AuthorizationPolicy
metadata:
  name: allow-nothing
spec:
  # This matches nothing, the action defaults to ALLOW if not specified.
  {}
```

 

gateway/gateway-01.yaml

kubectl apply -f gateway-01.yaml -n istio

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

访问

TOKEN=eyJhbGciOiJSUzI1NiIsImtpZCI6IkRIRmJwb0lVcXJZOHQyenBBMnFYZkNtcjVWTzVaRXI0UnpIVV8tZW52dlEiLCJ0eXAiOiJKV1QifQ.eyJleHAiOjQ2ODU5ODk3MDAsImZvbyI6ImJhciIsImlhdCI6MTUzMjM4OTcwMCwiaXNzIjoidGVzdGluZ0BzZWN1cmUuaXN0aW8uaW8iLCJzdWIiOiJ0ZXN0aW5nQHNlY3VyZS5pc3Rpby5pbyJ9.CfNnxWP2tcnR9q0vxyxweaF3ovQYHYZl82hAUsn21bwQd9zP7c-LS9qd_vpdLG4Tn1A15NxfCjp5f7QNBUo-KC9PJqYpgGbaXhaGx7bEdFWjcwv3nZzvc7M__ZpaCERdwU7igUmJqYGBYQ51vr2njU9ZimyKkfDe3axcyiBZde7G6dabliUosJvvKOPcKIWPccCgefSj_GNfwIip3-SsFdlR7BtbVUcqR-yv-XOxJ3Uc1MI0tz3uMiiZcyPV7sNCU4KRnemRIMHVOfuvHsU60_GhGbiSFzgPTAa9WTltbnarTbxudb_YEOx12JiwYToeX0DCPb43W1tzIBxgm8NxUg

curl 192.168.198.154:30986/productpage -H "Authorization: Bearer ${TOKEN}"



TOKEN=eyJhbGciOiJSUzI1NiIsImtpZCI6IkRIRmJwb0lVcXJZOHQyenBBMnFYZkNtcjVWTzVaRXI0UnpIVV8tZW52dlEiLCJ0eXAiOiJKV1QifQ.eyJleHAiOjM1MzczOTExMDQsImdyb3VwcyI6WyJncm91cDEiLCJncm91cDIiXSwiaWF0IjoxNTM3MzkxMTA0LCJpc3MiOiJ0ZXN0aW5nQHNlY3VyZS5pc3Rpby5pbyIsInNjb3BlIjpbInNjb3BlMSIsInNjb3BlMiJdLCJzdWIiOiJ0ZXN0aW5nQHNlY3VyZS5pc3Rpby5pbyJ9.EdJnEZSH6X8hcyEii7c8H5lnhgjB5dwo07M5oheC8Xz8mOllyg--AHCFWHybM48reunF--oGaG6IXVngCEpVF0_P5DwsUoBgpPmK1JOaKN6_pe9sh0ZwTtdgK_RP01PuI7kUdbOTlkuUi2AO-qUyOm7Art2POzo36DLQlUXv8Ad7NBOqfQaKjE9ndaPWT7aexUsBHxmgiGbz1SyLH879f7uHYPbPKlpHU6P9S-DaKnGLaEchnoKnov7ajhrEhGXAQRukhDPKUHO9L30oPIr5IJllEQfHYtt6IZvlNUGeLUcif3wpry1R5tBXRicx2sXMQ7LyuDremDbcNy_iE76Upg

curl 192.168.198.154:30986/productpage -H "Authorization: Bearer ${TOKEN}"

验证token：

https://jwt.io/

清理：

kubectl delete gw bookinfo-gateway -n istio

kubectl delete vs bookinfo -n istio

kubectl delete AuthorizationPolicy productpage  -n istio

kubectl delete   AuthorizationPolicy  allow-nothing -n istio-system

kubectl delete -f ra-example-productpage.yaml -n istio



kubectl port-forward --address 0.0.0.0 -n istio productpage-v1-659776cb44-rrr87 15000:15000

![1628480840(1)](images\1628480840(1).jpg)





使用authorizationPolicy

productpage-rules-from-requestPrincipals.yaml

kubectl apply -f productpage-rules-from-requestPrincipals.yaml -n istio

```
apiVersion: security.istio.io/v1beta1
kind: AuthorizationPolicy
metadata:
  name: productpage
spec:
  selector:
    matchLabels:
      app: productpage
  action: ALLOW
  rules:
  - to:
    - operation:
        paths: ["/productpage"]
    from:
    - source:
        requestPrincipals:
        - "testing@secure.istio.io/testing@secure.istio.io"
```

启用jwt

requestauthentications/ra-example-productpage.yaml

kubectl apply -f ra-example-productpage.yaml -n istio

```
apiVersion: "security.istio.io/v1beta1"
kind: "RequestAuthentication"
metadata:
  name: "jwt-example"
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
    forwardOriginalToken: true
```



allow-nothing.yaml

所有名称空间

kubectl apply -f allow-nothing.yaml -n istio-system

```
apiVersion: security.istio.io/v1beta1
kind: AuthorizationPolicy
metadata:
  name: allow-nothing
spec:
  # This matches nothing, the action defaults to ALLOW if not specified.
  {}
```

 

gateway/gateway-01.yaml

kubectl apply -f gateway-01.yaml -n istio

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



访问

TOKEN=eyJhbGciOiJSUzI1NiIsImtpZCI6IkRIRmJwb0lVcXJZOHQyenBBMnFYZkNtcjVWTzVaRXI0UnpIVV8tZW52dlEiLCJ0eXAiOiJKV1QifQ.eyJleHAiOjQ2ODU5ODk3MDAsImZvbyI6ImJhciIsImlhdCI6MTUzMjM4OTcwMCwiaXNzIjoidGVzdGluZ0BzZWN1cmUuaXN0aW8uaW8iLCJzdWIiOiJ0ZXN0aW5nQHNlY3VyZS5pc3Rpby5pbyJ9.CfNnxWP2tcnR9q0vxyxweaF3ovQYHYZl82hAUsn21bwQd9zP7c-LS9qd_vpdLG4Tn1A15NxfCjp5f7QNBUo-KC9PJqYpgGbaXhaGx7bEdFWjcwv3nZzvc7M__ZpaCERdwU7igUmJqYGBYQ51vr2njU9ZimyKkfDe3axcyiBZde7G6dabliUosJvvKOPcKIWPccCgefSj_GNfwIip3-SsFdlR7BtbVUcqR-yv-XOxJ3Uc1MI0tz3uMiiZcyPV7sNCU4KRnemRIMHVOfuvHsU60_GhGbiSFzgPTAa9WTltbnarTbxudb_YEOx12JiwYToeX0DCPb43W1tzIBxgm8NxUg

curl 192.168.198.154:30986/productpage -H "Authorization: Bearer ${TOKEN}"



TOKEN=eyJhbGciOiJSUzI1NiIsImtpZCI6IkRIRmJwb0lVcXJZOHQyenBBMnFYZkNtcjVWTzVaRXI0UnpIVV8tZW52dlEiLCJ0eXAiOiJKV1QifQ.eyJleHAiOjM1MzczOTExMDQsImdyb3VwcyI6WyJncm91cDEiLCJncm91cDIiXSwiaWF0IjoxNTM3MzkxMTA0LCJpc3MiOiJ0ZXN0aW5nQHNlY3VyZS5pc3Rpby5pbyIsInNjb3BlIjpbInNjb3BlMSIsInNjb3BlMiJdLCJzdWIiOiJ0ZXN0aW5nQHNlY3VyZS5pc3Rpby5pbyJ9.EdJnEZSH6X8hcyEii7c8H5lnhgjB5dwo07M5oheC8Xz8mOllyg--AHCFWHybM48reunF--oGaG6IXVngCEpVF0_P5DwsUoBgpPmK1JOaKN6_pe9sh0ZwTtdgK_RP01PuI7kUdbOTlkuUi2AO-qUyOm7Art2POzo36DLQlUXv8Ad7NBOqfQaKjE9ndaPWT7aexUsBHxmgiGbz1SyLH879f7uHYPbPKlpHU6P9S-DaKnGLaEchnoKnov7ajhrEhGXAQRukhDPKUHO9L30oPIr5IJllEQfHYtt6IZvlNUGeLUcif3wpry1R5tBXRicx2sXMQ7LyuDremDbcNy_iE76Upg

curl 192.168.198.154:30986/productpage -H "Authorization: Bearer ${TOKEN}"

验证token：

https://jwt.io/

清理：

kubectl delete gw bookinfo-gateway -n istio

kubectl delete vs bookinfo -n istio

kubectl delete AuthorizationPolicy productpage  -n istio

kubectl delete   AuthorizationPolicy  allow-nothing -n istio-system

kubectl delete -f ra-example-productpage.yaml -n istio



kubectl port-forward --address 0.0.0.0 -n istio productpage-v1-659776cb44-rrr87 15000:15000

![1628480970(1)](images\1628480970(1).jpg)



productpage-rules-from-requestPrincipals-semi-star.yaml

kubectl apply -f productpage-rules-from-requestPrincipals-semi-star.yaml -n istio

```
apiVersion: security.istio.io/v1beta1
kind: AuthorizationPolicy
metadata:
  name: productpage
spec:
  selector:
    matchLabels:
      app: productpage
  action: ALLOW
  rules:
  - to:
    - operation:
        paths: ["/productpage"]
    from:
    - source:
        requestPrincipals:
        - "testing@secure.istio.io/*"
```

启用jwt

requestauthentications/ra-example-productpage.yaml

kubectl apply -f ra-example-productpage.yaml -n istio

```
apiVersion: "security.istio.io/v1beta1"
kind: "RequestAuthentication"
metadata:
  name: "jwt-example"
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
    forwardOriginalToken: true
```



allow-nothing.yaml

所有名称空间

kubectl apply -f allow-nothing.yaml -n istio-system

```
apiVersion: security.istio.io/v1beta1
kind: AuthorizationPolicy
metadata:
  name: allow-nothing
spec:
  # This matches nothing, the action defaults to ALLOW if not specified.
  {}
```

 

gateway/gateway-01.yaml

kubectl apply -f gateway-01.yaml -n istio

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

访问

TOKEN=eyJhbGciOiJSUzI1NiIsImtpZCI6IkRIRmJwb0lVcXJZOHQyenBBMnFYZkNtcjVWTzVaRXI0UnpIVV8tZW52dlEiLCJ0eXAiOiJKV1QifQ.eyJleHAiOjQ2ODU5ODk3MDAsImZvbyI6ImJhciIsImlhdCI6MTUzMjM4OTcwMCwiaXNzIjoidGVzdGluZ0BzZWN1cmUuaXN0aW8uaW8iLCJzdWIiOiJ0ZXN0aW5nQHNlY3VyZS5pc3Rpby5pbyJ9.CfNnxWP2tcnR9q0vxyxweaF3ovQYHYZl82hAUsn21bwQd9zP7c-LS9qd_vpdLG4Tn1A15NxfCjp5f7QNBUo-KC9PJqYpgGbaXhaGx7bEdFWjcwv3nZzvc7M__ZpaCERdwU7igUmJqYGBYQ51vr2njU9ZimyKkfDe3axcyiBZde7G6dabliUosJvvKOPcKIWPccCgefSj_GNfwIip3-SsFdlR7BtbVUcqR-yv-XOxJ3Uc1MI0tz3uMiiZcyPV7sNCU4KRnemRIMHVOfuvHsU60_GhGbiSFzgPTAa9WTltbnarTbxudb_YEOx12JiwYToeX0DCPb43W1tzIBxgm8NxUg

curl 192.168.198.154:30986/productpage -H "Authorization: Bearer ${TOKEN}"



TOKEN=eyJhbGciOiJSUzI1NiIsImtpZCI6IkRIRmJwb0lVcXJZOHQyenBBMnFYZkNtcjVWTzVaRXI0UnpIVV8tZW52dlEiLCJ0eXAiOiJKV1QifQ.eyJleHAiOjM1MzczOTExMDQsImdyb3VwcyI6WyJncm91cDEiLCJncm91cDIiXSwiaWF0IjoxNTM3MzkxMTA0LCJpc3MiOiJ0ZXN0aW5nQHNlY3VyZS5pc3Rpby5pbyIsInNjb3BlIjpbInNjb3BlMSIsInNjb3BlMiJdLCJzdWIiOiJ0ZXN0aW5nQHNlY3VyZS5pc3Rpby5pbyJ9.EdJnEZSH6X8hcyEii7c8H5lnhgjB5dwo07M5oheC8Xz8mOllyg--AHCFWHybM48reunF--oGaG6IXVngCEpVF0_P5DwsUoBgpPmK1JOaKN6_pe9sh0ZwTtdgK_RP01PuI7kUdbOTlkuUi2AO-qUyOm7Art2POzo36DLQlUXv8Ad7NBOqfQaKjE9ndaPWT7aexUsBHxmgiGbz1SyLH879f7uHYPbPKlpHU6P9S-DaKnGLaEchnoKnov7ajhrEhGXAQRukhDPKUHO9L30oPIr5IJllEQfHYtt6IZvlNUGeLUcif3wpry1R5tBXRicx2sXMQ7LyuDremDbcNy_iE76Upg

curl 192.168.198.154:30986/productpage -H "Authorization: Bearer ${TOKEN}"

验证token：

https://jwt.io/

清理：

kubectl delete gw bookinfo-gateway -n istio

kubectl delete vs bookinfo -n istio

kubectl delete AuthorizationPolicy productpage  -n istio

kubectl delete   AuthorizationPolicy  allow-nothing -n istio-system

kubectl delete -f ra-example-productpage.yaml -n istio



kubectl port-forward --address 0.0.0.0 -n istio productpage-v1-659776cb44-rrr87 15000:15000

![1628481068(1)](images\1628481068(1).jpg)





#### notRequestPrincipals

jwt相关

productpage-rules-from-notRequestPrincipals.yaml

kubectl apply -f productpage-rules-from-notRequestPrincipals.yaml -n istio

```
apiVersion: security.istio.io/v1beta1
kind: AuthorizationPolicy
metadata:
  name: productpage
spec:
  selector:
    matchLabels:
      app: productpage
  action: ALLOW
  rules:
  - to:
    - operation:
        paths: ["/productpage"]
    from:
    - source:
        notRequestPrincipals:
        - "testing@secure.istio.io/testing@secure.istio.io"
```

启用jwt

requestauthentications/ra-example-productpage.yaml

kubectl apply -f ra-example-productpage.yaml -n istio

```
apiVersion: "security.istio.io/v1beta1"
kind: "RequestAuthentication"
metadata:
  name: "jwt-example"
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
    forwardOriginalToken: true
```



allow-nothing.yaml

所有名称空间

kubectl apply -f allow-nothing.yaml -n istio-system

```
apiVersion: security.istio.io/v1beta1
kind: AuthorizationPolicy
metadata:
  name: allow-nothing
spec:
  # This matches nothing, the action defaults to ALLOW if not specified.
  {}
```

 

gateway/gateway-01.yaml

kubectl apply -f gateway-01.yaml -n istio

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

访问

TOKEN=eyJhbGciOiJSUzI1NiIsImtpZCI6IkRIRmJwb0lVcXJZOHQyenBBMnFYZkNtcjVWTzVaRXI0UnpIVV8tZW52dlEiLCJ0eXAiOiJKV1QifQ.eyJleHAiOjQ2ODU5ODk3MDAsImZvbyI6ImJhciIsImlhdCI6MTUzMjM4OTcwMCwiaXNzIjoidGVzdGluZ0BzZWN1cmUuaXN0aW8uaW8iLCJzdWIiOiJ0ZXN0aW5nQHNlY3VyZS5pc3Rpby5pbyJ9.CfNnxWP2tcnR9q0vxyxweaF3ovQYHYZl82hAUsn21bwQd9zP7c-LS9qd_vpdLG4Tn1A15NxfCjp5f7QNBUo-KC9PJqYpgGbaXhaGx7bEdFWjcwv3nZzvc7M__ZpaCERdwU7igUmJqYGBYQ51vr2njU9ZimyKkfDe3axcyiBZde7G6dabliUosJvvKOPcKIWPccCgefSj_GNfwIip3-SsFdlR7BtbVUcqR-yv-XOxJ3Uc1MI0tz3uMiiZcyPV7sNCU4KRnemRIMHVOfuvHsU60_GhGbiSFzgPTAa9WTltbnarTbxudb_YEOx12JiwYToeX0DCPb43W1tzIBxgm8NxUg

curl 192.168.198.154:30986/productpage -H "Authorization: Bearer ${TOKEN}"



TOKEN=eyJhbGciOiJSUzI1NiIsImtpZCI6IkRIRmJwb0lVcXJZOHQyenBBMnFYZkNtcjVWTzVaRXI0UnpIVV8tZW52dlEiLCJ0eXAiOiJKV1QifQ.eyJleHAiOjM1MzczOTExMDQsImdyb3VwcyI6WyJncm91cDEiLCJncm91cDIiXSwiaWF0IjoxNTM3MzkxMTA0LCJpc3MiOiJ0ZXN0aW5nQHNlY3VyZS5pc3Rpby5pbyIsInNjb3BlIjpbInNjb3BlMSIsInNjb3BlMiJdLCJzdWIiOiJ0ZXN0aW5nQHNlY3VyZS5pc3Rpby5pbyJ9.EdJnEZSH6X8hcyEii7c8H5lnhgjB5dwo07M5oheC8Xz8mOllyg--AHCFWHybM48reunF--oGaG6IXVngCEpVF0_P5DwsUoBgpPmK1JOaKN6_pe9sh0ZwTtdgK_RP01PuI7kUdbOTlkuUi2AO-qUyOm7Art2POzo36DLQlUXv8Ad7NBOqfQaKjE9ndaPWT7aexUsBHxmgiGbz1SyLH879f7uHYPbPKlpHU6P9S-DaKnGLaEchnoKnov7ajhrEhGXAQRukhDPKUHO9L30oPIr5IJllEQfHYtt6IZvlNUGeLUcif3wpry1R5tBXRicx2sXMQ7LyuDremDbcNy_iE76Upg

curl 192.168.198.154:30986/productpage -H "Authorization: Bearer ${TOKEN}"

验证token：

https://jwt.io/

清理：

kubectl delete gw bookinfo-gateway -n istio

kubectl delete vs bookinfo -n istio

kubectl delete AuthorizationPolicy productpage  -n istio

kubectl delete   AuthorizationPolicy  allow-nothing -n istio-system

kubectl delete -f ra-example-productpage.yaml -n istio



kubectl port-forward --address 0.0.0.0 -n istio productpage-v1-659776cb44-rrr87 15000:15000

![1628481193(1)](images\1628481193(1).jpg)





#### namespaces

productpage-rules-from-namespaces.yaml

kubectl apply -f productpage-rules-from-namespaces.yaml -n istio

```
apiVersion: security.istio.io/v1beta1
kind: AuthorizationPolicy
metadata:
 name: productpage
spec:
 selector:
   matchLabels:
     app: productpage
     version: v1
 action: ALLOW
 rules:
 - from:
   - source:
       namespaces:
       - "istio-system"
```

allow-nothing.yaml

所有名称空间

kubectl apply -f allow-nothing.yaml -n istio-system

```
apiVersion: security.istio.io/v1beta1
kind: AuthorizationPolicy
metadata:
  name: allow-nothing
spec:
  # This matches nothing, the action defaults to ALLOW if not specified.
  {}
```

 

gateway/gateway-01.yaml

kubectl apply -f gateway-01.yaml -n istio

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



清理：

kubectl delete gw bookinfo-gateway -n istio

kubectl delete vs bookinfo -n istio

kubectl delete AuthorizationPolicy productpage  -n istio

kubectl delete   AuthorizationPolicy  allow-nothing -n istio-system



kubectl port-forward --address 0.0.0.0 -n istio productpage-v1-659776cb44-rrr87 15000:15000

![1628481322(1)](images\1628481322(1).jpg)



#### notNamespaces

productpage-rules-from-notNamespaces.yaml

kubectl apply -f productpage-rules-from-notNamespaces.yaml -n istio

```
apiVersion: security.istio.io/v1beta1
kind: AuthorizationPolicy
metadata:
 name: productpage
spec:
 selector:
   matchLabels:
     app: productpage
     version: v1
 action: ALLOW
 rules:
 - from:
   - source:
       notNamespaces:
       - "test"
```

allow-nothing.yaml

所有名称空间

kubectl apply -f allow-nothing.yaml -n istio-system

```
apiVersion: security.istio.io/v1beta1
kind: AuthorizationPolicy
metadata:
  name: allow-nothing
spec:
  # This matches nothing, the action defaults to ALLOW if not specified.
  {}
```

 

gateway/gateway-01.yaml

kubectl apply -f gateway-01.yaml -n istio

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



清理：

kubectl delete gw bookinfo-gateway -n istio

kubectl delete vs bookinfo -n istio

kubectl delete AuthorizationPolicy productpage  -n istio

kubectl delete   AuthorizationPolicy  allow-nothing -n istio-system



kubectl port-forward --address 0.0.0.0 -n istio productpage-v1-659776cb44-rrr87 15000:15000

![1628481414(1)](images\1628481414(1).jpg)







#### ipBlocks

ingressgateway-rules-from-ipBlocks.yaml

kubectl apply -f ingressgateway-rules-from-ipBlocks.yaml -n istio-system

```
apiVersion: security.istio.io/v1beta1
kind: AuthorizationPolicy
metadata:
 name: ingressgateway
spec:
 selector:
   matchLabels:
     app: istio-ingressgateway
 action: ALLOW
 rules:
 - from:
   - source:
       ipBlocks:
       - "172.20.0.0/16"
```

设置xff，原地址保持



allow-nothing.yaml

所有名称空间

kubectl apply -f allow-nothing.yaml -n istio-system

```
apiVersion: security.istio.io/v1beta1
kind: AuthorizationPolicy
metadata:
  name: allow-nothing
spec:
  # This matches nothing, the action defaults to ALLOW if not specified.
  {}
```

 

gateway/gateway-01.yaml

kubectl apply -f gateway-01.yaml -n istio

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



清理：

kubectl delete gw bookinfo-gateway -n istio

kubectl delete vs bookinfo -n istio

kubectl delete AuthorizationPolicy ingressgateway -n istio-system

kubectl delete   AuthorizationPolicy  allow-nothing -n istio-system



![1628481581(1)](images\1628481581(1).jpg)





#### notIpBlocks

ingressgateway-rules-from-notIpBlocks.yaml

kubectl apply -f ingressgateway-rules-from-notIpBlocks.yaml -n istio-system

```
apiVersion: security.istio.io/v1beta1
kind: AuthorizationPolicy
metadata:
 name: ingressgateway
spec:
 selector:
   matchLabels:
     app: istio-ingressgateway
 action: ALLOW
 rules:
 - from:
   - source:
       notIpBlocks:
       - "172.20.0.0/16"
```

allow-nothing.yaml

所有名称空间

kubectl apply -f allow-nothing.yaml -n istio-system

```
apiVersion: security.istio.io/v1beta1
kind: AuthorizationPolicy
metadata:
  name: allow-nothing
spec:
  # This matches nothing, the action defaults to ALLOW if not specified.
  {}
```

 

gateway/gateway-01.yaml

kubectl apply -f gateway-01.yaml -n istio

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



清理：

kubectl delete gw bookinfo-gateway -n istio

kubectl delete vs bookinfo -n istio

kubectl delete AuthorizationPolicy ingressgateway -n istio-system

kubectl delete   AuthorizationPolicy  allow-nothing -n istio-system



![1628481679(1)](images\1628481679(1).jpg)







#### remoteIpBlocks

修改svc

kubectl edit svc istio-ingressgateway  -n istio-system

externalTrafficPolicy: Local



用于设置白名单

ingressgateway-rules-from-remoteIpBlocks.yaml

kubectl apply -f ingressgateway-rules-from-remoteIpBlocks.yaml -n istio-system

```
apiVersion: security.istio.io/v1beta1
kind: AuthorizationPolicy
metadata:
 name: ingressgateway
spec:
 selector:
   matchLabels:
     app: istio-ingressgateway
 action: DENY
 rules:
 - from:
   - source:
       remoteIpBlocks:
       - 192.168.198.1/32
```

allow-nothing.yaml

所有名称空间

kubectl apply -f allow-nothing.yaml -n istio-system

```
apiVersion: security.istio.io/v1beta1
kind: AuthorizationPolicy
metadata:
  name: allow-nothing
spec:
  # This matches nothing, the action defaults to ALLOW if not specified.
  {}
```

 

gateway/gateway-01.yaml

kubectl apply -f gateway-01.yaml -n istio

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



清理：

kubectl delete gw bookinfo-gateway -n istio

kubectl delete vs bookinfo -n istio

kubectl delete AuthorizationPolicy ingressgateway -n istio-system

kubectl delete   AuthorizationPolicy  allow-nothing -n istio-system



![1628481815(1)](images\1628481815(1).jpg)





#### notRemoteIpBlocks

修改svc

kubectl edit svc istio-ingressgateway  -n istio-system

externalTrafficPolicy: Local



用于设置黑名单

ingressgateway-rules-from-notRemoteIpBlocks.yaml

kubectl apply -f ingressgateway-rules-from-notRemoteIpBlocks.yaml -n istio-system

```
apiVersion: security.istio.io/v1beta1
kind: AuthorizationPolicy
metadata:
 name: ingressgateway
spec:
 selector:
   matchLabels:
     app: istio-ingressgateway
 action: ALLOW
 rules:
 - from:
   - source:
       notRemoteIpBlocks:
       - "192.168.198.1/32
```

allow-nothing.yaml

所有名称空间

kubectl apply -f allow-nothing.yaml -n istio-system

```
apiVersion: security.istio.io/v1beta1
kind: AuthorizationPolicy
metadata:
  name: allow-nothing
spec:
  # This matches nothing, the action defaults to ALLOW if not specified.
  {}
```

 

gateway/gateway-01.yaml

kubectl apply -f gateway-01.yaml -n istio

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



清理：

kubectl delete gw bookinfo-gateway -n istio

kubectl delete vs bookinfo -n istio

kubectl delete AuthorizationPolicy ingressgateway -n istio-system

kubectl delete   AuthorizationPolicy  allow-nothing -n istio-system



![1628481872(1)](images\1628481872(1).jpg)





### 9-5to

| Field       | Type        | Description                                     | Required |
| ----------- | ----------- | ----------------------------------------------- | -------- |
| `operation` | `Operation` | Operation specifies the operation of a request. | No       |

| Field        | Type       | Description                                                  | Required |
| ------------ | ---------- | ------------------------------------------------------------ | -------- |
| `hosts`      | `string[]` | Optional. A list of hosts, which matches to the “request.host” attribute.If not set, any host is allowed. Must be used only with HTTP. | No       |
| `notHosts`   | `string[]` | Optional. A list of negative match of hosts.                 | No       |
| `ports`      | `string[]` | Optional. A list of ports, which matches to the “destination.port” attribute.If not set, any port is allowed. | No       |
| `notPorts`   | `string[]` | Optional. A list of negative match of ports.                 | No       |
| `methods`    | `string[]` | Optional. A list of methods, which matches to the “request.method” attribute. For gRPC service, this will always be “POST”.If not set, any method is allowed. Must be used only with HTTP. | No       |
| `notMethods` | `string[]` | Optional. A list of negative match of methods.               | No       |
| `paths`      | `string[]` | Optional. A list of paths, which matches to the “request.url_path” attribute. For gRPC service, this will be the fully-qualified name in the form of “/package.service/method”.If not set, any path is allowed. Must be used only with HTTP. | No       |
| `notPaths`   | `string[]` | Optional. A list of negative match of paths.                 | No       |

#### hosts

productpage-rules-to-hosts.yaml

kubectl apply -f productpage-rules-to-hosts.yaml -n istio

```
apiVersion: security.istio.io/v1beta1
kind: AuthorizationPolicy
metadata:
 name: productpage
spec:
 selector:
   matchLabels:
     app: productpage
     version: v1
 action: ALLOW
 rules:
 - to:
   - operation:
       hosts:
       - "bookinfo.demo:30986"
   from:
   - source:
       namespaces:
       - "istio-system"
 
```

allow-nothing.yaml

所有名称空间

kubectl apply -f allow-nothing.yaml -n istio-system

```
apiVersion: security.istio.io/v1beta1
kind: AuthorizationPolicy
metadata:
  name: allow-nothing
spec:
  # This matches nothing, the action defaults to ALLOW if not specified.
  {}
```

 

gateway/gateway-01.yaml

kubectl apply -f gateway-01.yaml -n istio

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



清理：

kubectl delete gw bookinfo-gateway -n istio

kubectl delete vs bookinfo -n istio

kubectl delete AuthorizationPolicy productpage  -n istio

kubectl delete   AuthorizationPolicy  allow-nothing -n istio-system



kubectl port-forward --address 0.0.0.0 -n istio productpage-v1-659776cb44-rrr87 15000:15000



![1628484022](images\1628484022.jpg)







details-rules-to-hosts.yaml

kubectl apply -f details-rules-to-hosts.yaml -n istio

```
apiVersion: security.istio.io/v1beta1
kind: AuthorizationPolicy
metadata:
 name: details
spec:
 selector:
   matchLabels:
     app: details
     version: v1
 action: ALLOW
 rules:
 - to:
   - operation:
       hosts:
       - "details:9080"
```

其实是authority，必须加上端口

allow-nothing.yaml

所有名称空间

kubectl apply -f allow-nothing.yaml -n istio-system

```
apiVersion: security.istio.io/v1beta1
kind: AuthorizationPolicy
metadata:
  name: allow-nothing
spec:
  # This matches nothing, the action defaults to ALLOW if not specified.
  {}
```

 

gateway/gateway-01.yaml

kubectl apply -f gateway-01.yaml -n istio

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



清理：

kubectl delete gw bookinfo-gateway -n istio

kubectl delete vs bookinfo -n istio

kubectl delete AuthorizationPolicy details -n istio

kubectl delete   AuthorizationPolicy  allow-nothing -n istio-system



kubectl port-forward --address 0.0.0.0 -n istio details-v1-79f774bdb9-jbvb7 15002:15000

![1628484156(1)](images\1628484156(1).jpg)





#### notHosts

productpage-rules-to-notHosts.yaml

kubectl apply -f productpage-rules-to-notHosts.yaml -n istio

```
apiVersion: security.istio.io/v1beta1
kind: AuthorizationPolicy
metadata:
 name: productpage
spec:
 selector:
   matchLabels:
     app: productpage
     version: v1
 action: ALLOW
 rules:
 - to:
   - operation:
       notHosts:
       - "test"
   from:
   - source:
       namespaces:
       - "istio-system"
```

allow-nothing.yaml

所有名称空间

kubectl apply -f allow-nothing.yaml -n istio-system

```
apiVersion: security.istio.io/v1beta1
kind: AuthorizationPolicy
metadata:
  name: allow-nothing
spec:
  # This matches nothing, the action defaults to ALLOW if not specified.
  {}
```

 

gateway/gateway-01.yaml

kubectl apply -f gateway-01.yaml -n istio

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



清理：

kubectl delete gw bookinfo-gateway -n istio

kubectl delete vs bookinfo -n istio

kubectl delete AuthorizationPolicy productpage  -n istio

kubectl delete   AuthorizationPolicy  allow-nothing -n istio-system



kubectl port-forward --address 0.0.0.0 -n istio productpage-v1-659776cb44-rrr87 15000:15000



![1628484261(1)](images\1628484261(1).jpg)





#### ports

details-rules-to-ports.yaml

kubectl apply -f details-rules-to-ports.yaml -n istio

```
apiVersion: security.istio.io/v1beta1
kind: AuthorizationPolicy
metadata:
 name: details
spec:
 selector:
   matchLabels:
     app: details
     version: v1
 action: ALLOW
 rules:
 - to:
   - operation:
       ports:
       - "9080"
```

其实是authority，必须加上端口

allow-nothing.yaml

所有名称空间

kubectl apply -f allow-nothing.yaml -n istio-system

```
apiVersion: security.istio.io/v1beta1
kind: AuthorizationPolicy
metadata:
  name: allow-nothing
spec:
  # This matches nothing, the action defaults to ALLOW if not specified.
  {}
```

 

gateway/gateway-01.yaml

kubectl apply -f gateway-01.yaml -n istio

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



清理：

kubectl delete gw bookinfo-gateway -n istio

kubectl delete vs bookinfo -n istio

kubectl delete AuthorizationPolicy details -n istio

kubectl delete   AuthorizationPolicy  allow-nothing -n istio-system



kubectl port-forward --address 0.0.0.0 -n istio details-v1-79f774bdb9-jbvb7 15002:15000



![1628484393(1)](images\1628484393(1).jpg)







#### notPorts

details-rules-to-notPorts.yaml

kubectl apply -f details-rules-to-notPorts.yaml -n istio

```
apiVersion: security.istio.io/v1beta1
kind: AuthorizationPolicy
metadata:
 name: details
spec:
 selector:
   matchLabels:
     app: details
     version: v1
 action: ALLOW
 rules:
 - to:
   - operation:
       notPorts:
       - "9080"
```

其实是authority，必须加上端口

allow-nothing.yaml

所有名称空间

kubectl apply -f allow-nothing.yaml -n istio-system

```
apiVersion: security.istio.io/v1beta1
kind: AuthorizationPolicy
metadata:
  name: allow-nothing
spec:
  # This matches nothing, the action defaults to ALLOW if not specified.
  {}
```

 

gateway/gateway-01.yaml

kubectl apply -f gateway-01.yaml -n istio

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



清理：

kubectl delete gw bookinfo-gateway -n istio

kubectl delete vs bookinfo -n istio

kubectl delete AuthorizationPolicy details -n istio

kubectl delete   AuthorizationPolicy  allow-nothing -n istio-system



kubectl port-forward --address 0.0.0.0 -n istio details-v1-79f774bdb9-jbvb7 15002:15000



![1628484440(1)](images\1628484440(1).jpg)







#### methods

details-rules-to-methods.yaml

kubectl apply -f details-rules-to-methods.yaml -n istio

```
apiVersion: security.istio.io/v1beta1
kind: AuthorizationPolicy
metadata:
 name: details
spec:
 selector:
   matchLabels:
     app: details
     version: v1
 action: ALLOW
 rules:
 - to:
   - operation:
       methods:
       - "GET"
```

allow-nothing.yaml

所有名称空间

kubectl apply -f allow-nothing.yaml -n istio-system

```
apiVersion: security.istio.io/v1beta1
kind: AuthorizationPolicy
metadata:
  name: allow-nothing
spec:
  # This matches nothing, the action defaults to ALLOW if not specified.
  {}
```

 

gateway/gateway-01.yaml

kubectl apply -f gateway-01.yaml -n istio

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



清理：

kubectl delete gw bookinfo-gateway -n istio

kubectl delete vs bookinfo -n istio

kubectl delete AuthorizationPolicy details -n istio

kubectl delete   AuthorizationPolicy  allow-nothing -n istio-system



kubectl port-forward --address 0.0.0.0 -n istio details-v1-79f774bdb9-jbvb7 15002:15000



![1628484665(1)](images\1628484665(1).jpg)





#### notMethods

details-rules-to-notMethods.yaml

kubectl apply -f details-rules-to-notMethods.yaml -n istio

```
apiVersion: security.istio.io/v1beta1
kind: AuthorizationPolicy
metadata:
 name: details
spec:
 selector:
   matchLabels:
     app: details
     version: v1
 action: ALLOW
 rules:
 - to:
   - operation:
       notMethods:
       - "GET"
```

allow-nothing.yaml

所有名称空间

kubectl apply -f allow-nothing.yaml -n istio-system

```
apiVersion: security.istio.io/v1beta1
kind: AuthorizationPolicy
metadata:
  name: allow-nothing
spec:
  # This matches nothing, the action defaults to ALLOW if not specified.
  {}
```

 

gateway/gateway-01.yaml

kubectl apply -f gateway-01.yaml -n istio

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



清理：

kubectl delete gw bookinfo-gateway -n istio

kubectl delete vs bookinfo -n istio

kubectl delete AuthorizationPolicy details -n istio

kubectl delete   AuthorizationPolicy  allow-nothing -n istio-system



kubectl port-forward --address 0.0.0.0 -n istio details-v1-79f774bdb9-jbvb7 15002:15000



![1628484711(1)](images\1628484711(1).jpg)







#### paths

details-rules-to-paths.yaml

kubectl apply -f details-rules-to-paths.yaml -n istio

```
apiVersion: security.istio.io/v1beta1
kind: AuthorizationPolicy
metadata:
 name: details
spec:
 selector:
   matchLabels:
     app: details
     version: v1
 action: ALLOW
 rules:
 - to:
   - operation:
       paths:
       - "/details/0"
```

allow-nothing.yaml

所有名称空间

kubectl apply -f allow-nothing.yaml -n istio-system

```
apiVersion: security.istio.io/v1beta1
kind: AuthorizationPolicy
metadata:
  name: allow-nothing
spec:
  # This matches nothing, the action defaults to ALLOW if not specified.
  {}
```

 

gateway/gateway-01.yaml

kubectl apply -f gateway-01.yaml -n istio

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



清理：

kubectl delete gw bookinfo-gateway -n istio

kubectl delete vs bookinfo -n istio

kubectl delete AuthorizationPolicy details -n istio

kubectl delete   AuthorizationPolicy  allow-nothing -n istio-system



kubectl port-forward --address 0.0.0.0 -n istio details-v1-79f774bdb9-jbvb7 15002:15000

![1628484754(1)](images\1628484754(1).jpg)









统配符

details-rules-to-paths-star.yaml

kubectl apply -f details-rules-to-paths-star.yaml -n istio

```
apiVersion: security.istio.io/v1beta1
kind: AuthorizationPolicy
metadata:
 name: details
spec:
 selector:
   matchLabels:
     app: details
     version: v1
 action: ALLOW
 rules:
 - to:
   - operation:
       paths:
       - "/details/*"
```

allow-nothing.yaml

所有名称空间

kubectl apply -f allow-nothing.yaml -n istio-system

```
apiVersion: security.istio.io/v1beta1
kind: AuthorizationPolicy
metadata:
  name: allow-nothing
spec:
  # This matches nothing, the action defaults to ALLOW if not specified.
  {}
```

 

gateway/gateway-01.yaml

kubectl apply -f gateway-01.yaml -n istio

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



清理：

kubectl delete gw bookinfo-gateway -n istio

kubectl delete vs bookinfo -n istio

kubectl delete AuthorizationPolicy details -n istio

kubectl delete   AuthorizationPolicy  allow-nothing -n istio-system



kubectl port-forward --address 0.0.0.0 -n istio details-v1-79f774bdb9-jbvb7 15002:15000

![1628484805(1)](images\1628484805(1).jpg)







#### notPaths

details-rules-to-notPaths.yaml

kubectl apply -f details-rules-to-notPaths.yaml -n istio

```
apiVersion: security.istio.io/v1beta1
kind: AuthorizationPolicy
metadata:
 name: details
spec:
 selector:
   matchLabels:
     app: details
     version: v1
 action: ALLOW
 rules:
 - to:
   - operation:
       notPaths:
       - "/details/0"
```

allow-nothing.yaml

所有名称空间

kubectl apply -f allow-nothing.yaml -n istio-system

```
apiVersion: security.istio.io/v1beta1
kind: AuthorizationPolicy
metadata:
  name: allow-nothing
spec:
  # This matches nothing, the action defaults to ALLOW if not specified.
  {}
```

 

gateway/gateway-01.yaml

kubectl apply -f gateway-01.yaml -n istio

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



清理：

kubectl delete gw bookinfo-gateway -n istio

kubectl delete vs bookinfo -n istio

kubectl delete AuthorizationPolicy details -n istio

kubectl delete   AuthorizationPolicy  allow-nothing -n istio-system



kubectl port-forward --address 0.0.0.0 -n istio details-v1-79f774bdb9-jbvb7 15002:15000

![1628484847(1)](images\1628484847(1).jpg)









通配符

details-rules-to-notPaths-star.yaml

kubectl apply -f details-rules-to-notPaths-star.yaml -n istio

```
apiVersion: security.istio.io/v1beta1
kind: AuthorizationPolicy
metadata:
 name: details
spec:
 selector:
   matchLabels:
     app: details
     version: v1
 action: ALLOW
 rules:
 - to:
   - operation:
       notPaths:
       - "/details/*"
```

allow-nothing.yaml

所有名称空间

kubectl apply -f allow-nothing.yaml -n istio-system

```
apiVersion: security.istio.io/v1beta1
kind: AuthorizationPolicy
metadata:
  name: allow-nothing
spec:
  # This matches nothing, the action defaults to ALLOW if not specified.
  {}
```

 

gateway/gateway-01.yaml

kubectl apply -f gateway-01.yaml -n istio

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



清理：

kubectl delete gw bookinfo-gateway -n istio

kubectl delete vs bookinfo -n istio

kubectl delete AuthorizationPolicy details -n istio

kubectl delete   AuthorizationPolicy  allow-nothing -n istio-system



kubectl port-forward --address 0.0.0.0 -n istio details-v1-79f774bdb9-jbvb7 15002:15000

![1628484923(1)](images\1628484923(1).jpg)







### 9-6when

| Field       | Type       | Description                                                  | Required |
| ----------- | ---------- | ------------------------------------------------------------ | -------- |
| `key`       | `string`   | The name of an Istio attribute. See the [full list of supported attributes](https://istio.io/latest/docs/reference/config/security/conditions/). | Yes      |
| `values`    | `string[]` | Optional. A list of allowed values for the attribute. Note: at least one of values or not_values must be set. | No       |
| `notValues` | `string[]` | Optional. A list of negative match of values for the attribute. Note: at least one of values or not_values must be set. | No       |

https://istio.io/latest/docs/reference/config/security/conditions/

| Name                           | Description                                                  | Supported Protocols | Example                                                      |
| ------------------------------ | ------------------------------------------------------------ | ------------------- | ------------------------------------------------------------ |
| `request.headers`              | HTTP request headers. The header name is surrounded by `[]` without any quotes | HTTP only           | `key: request.headers[User-Agent]` `values: ["Mozilla/*"]`   |
| `source.ip`                    | Source workload instance IP address, supports single IP or CIDR | HTTP and TCP        | `key: source.ip` `values: ["10.1.2.3", "10.2.0.0/16"]`       |
| `remote.ip`                    | Original client IP address as determined by X-Forwarded-For header or Proxy Protocol, supports single IP or CIDR | HTTP and TCP        | `key: remote.ip` `values: ["10.1.2.3", "10.2.0.0/16"]`       |
| `source.namespace`             | Source workload instance namespace, requires mutual TLS enabled | HTTP and TCP        | `key: source.namespace` `values: ["default"]`                |
| `source.principal`             | The identity of the source workload, requires mutual TLS enabled | HTTP and TCP        | `key: source.principal` `values: ["cluster.local/ns/default/sa/productpage"]` |
| `request.auth.principal`       | The principal of the authenticated JWT token, constructed from the JWT claims in the format of `/`, requires request authentication policy applied | HTTP only           | `key: request.auth.principal` `values: ["issuer.example.com/subject-admin"]` |
| `request.auth.audiences`       | The intended audiences of the authenticated JWT token, constructed from the JWT claim ``, requires request authentication policy applied | HTTP only           | `key: request.auth.audiences` `values: ["example.com"]`      |
| `request.auth.presenter`       | The authorized presenter of the authenticated JWT token, constructed from the JWT claim ``, requires request authentication policy applied | HTTP only           | `key: request.auth.presenter` `values: ["123456789012.example.com"]` |
| `request.auth.claims`          | Raw claims of the authenticated JWT token. The claim name is surrounded by `[]` without any quotes, nested claim can also be used, requires request authentication policy applied. Note only support claim of type string or list of string | HTTP only           | `key: request.auth.claims[iss]` `values: ["*@foo.com"]` — `key: request.auth.claims[nested1][nested2]` `values: ["some-value"]` |
| `destination.ip`               | Destination workload instance IP address, supports single IP or CIDR | HTTP and TCP        | `key: destination.ip` `values: ["10.1.2.3", "10.2.0.0/16"]`  |
| `destination.port`             | Destination workload instance port, must be in the range [0, 65535]. Note this is not the service port | HTTP and TCP        | `key: destination.port` `values: ["80", "443"]`              |
| `connection.sni`               | The server name indication, requires TLS enabled             | HTTP and TCP        | `key: connection.sni` `values: ["www.example.com"]`          |
| `experimental.envoy.filters.*` | Experimental metadata matching for filters, values wrapped in `[]` are matched as a list | HTTP and TCP        | `key: experimental.envoy.filters.network.mysql_proxy[db.table]` `values: ["[update]"]` |

| field       | sub field                | JWT claims   |
| :---------- | :----------------------- | :----------- |
| from.source | requestPrincipals        | iss/sub      |
| from.source | notRequestPrincipals     | iss/sub      |
| when.key    | request.auth.principal   | iss/sub      |
| when.key    | request.auth.audiences   | aud          |
| when.key    | request.auth.presenter   | azp          |
| when.key    | request.auth.claims[key] | JWT 全部属性 |

#### request.headers

##### values

productpage-rules-when-request-headers-values.yaml

kubectl apply -f productpage-rules-when-request-headers-values.yaml -n istio

```
apiVersion: security.istio.io/v1beta1
kind: AuthorizationPolicy
metadata:
 name: productpage
spec:
 selector:
   matchLabels:
     app: productpage
     version: v1
 action: ALLOW
 rules:
 - when:
   - key: request.headers[test]
     values:
     - "test"
```



allow-nothing.yaml

所有名称空间

kubectl apply -f allow-nothing.yaml -n istio-system

```
apiVersion: security.istio.io/v1beta1
kind: AuthorizationPolicy
metadata:
  name: allow-nothing
spec:
  # This matches nothing, the action defaults to ALLOW if not specified.
  {}
```

 

gateway/gateway-01.yaml

kubectl apply -f gateway-01.yaml -n istio

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

curl 192.168.198.154:30986/productpage --header "test:test"

清理：

kubectl delete gw bookinfo-gateway -n istio

kubectl delete vs bookinfo -n istio

kubectl delete AuthorizationPolicy productpage  -n istio

kubectl delete   AuthorizationPolicy  allow-nothing -n istio-system



kubectl port-forward --address 0.0.0.0 -n istio productpage-v1-659776cb44-rrr87 15000:15000

![1628490152(1)](images\1628490152(1).jpg)









##### notValues

productpage-rules-when-request-headers-notValues.yaml

kubectl apply -f productpage-rules-when-request-headers-notValues.yaml -n istio

```
apiVersion: security.istio.io/v1beta1
kind: AuthorizationPolicy
metadata:
 name: productpage
spec:
 selector:
   matchLabels:
     app: productpage
     version: v1
 action: ALLOW
 rules:
 - when:
   - key: request.headers[test]
     notValues:
     - "test"
```



allow-nothing.yaml

所有名称空间

kubectl apply -f allow-nothing.yaml -n istio-system

```
apiVersion: security.istio.io/v1beta1
kind: AuthorizationPolicy
metadata:
  name: allow-nothing
spec:
  # This matches nothing, the action defaults to ALLOW if not specified.
  {}
```

 

gateway/gateway-01.yaml

kubectl apply -f gateway-01.yaml -n istio

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

curl 192.168.198.154:30986/productpage --header "test:test"

curl 192.168.198.154:30986/productpage --header "test:test2"

清理：

kubectl delete gw bookinfo-gateway -n istio

kubectl delete vs bookinfo -n istio

kubectl delete AuthorizationPolicy productpage  -n istio

kubectl delete   AuthorizationPolicy  allow-nothing -n istio-system



kubectl port-forward --address 0.0.0.0 -n istio productpage-v1-659776cb44-rrr87 15000:15000

![1628490228(1)](images\1628490228(1).jpg)







#### source.ip

##### values

productpage-when-source-ip-values.yaml

kubectl apply -f productpage-when-source-ip-values.yaml -n istio

```
apiVersion: security.istio.io/v1beta1
kind: AuthorizationPolicy
metadata:
 name: productpage
spec:
 selector:
   matchLabels:
     app: productpage
 action: ALLOW
 rules:
 - when:
   - key: source.ip
     values:
     - "172.20.0.0/16"
```

allow-nothing.yaml

所有名称空间

kubectl apply -f allow-nothing.yaml -n istio-system

```
apiVersion: security.istio.io/v1beta1
kind: AuthorizationPolicy
metadata:
  name: allow-nothing
spec:
  # This matches nothing, the action defaults to ALLOW if not specified.
  {}
```

 

gateway/gateway-01.yaml

kubectl apply -f gateway-01.yaml -n istio

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



清理：

kubectl delete gw bookinfo-gateway -n istio

kubectl delete vs bookinfo -n istio

kubectl delete AuthorizationPolicy productpage  -n istio

kubectl delete   AuthorizationPolicy  allow-nothing -n istio-system



kubectl port-forward --address 0.0.0.0 -n istio productpage-v1-659776cb44-rrr87 15000:15000



![1628490290(1)](images\1628490290(1).jpg)





##### notValues

productpage-when-source-ip-notValues.yaml

kubectl apply -f productpage-when-source-ip-notValues.yaml -n istio

```
apiVersion: security.istio.io/v1beta1
kind: AuthorizationPolicy
metadata:
 name: productpage
spec:
 selector:
   matchLabels:
     app: productpage
 action: ALLOW
 rules:
 - when:
   - key: source.ip
     notValues:
     - "172.20.0.0/16"
```

allow-nothing.yaml

所有名称空间

kubectl apply -f allow-nothing.yaml -n istio-system

```
apiVersion: security.istio.io/v1beta1
kind: AuthorizationPolicy
metadata:
  name: allow-nothing
spec:
  # This matches nothing, the action defaults to ALLOW if not specified.
  {}
```

 

gateway/gateway-01.yaml

kubectl apply -f gateway-01.yaml -n istio

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



清理：

kubectl delete gw bookinfo-gateway -n istio

kubectl delete vs bookinfo -n istio

kubectl delete AuthorizationPolicy productpage  -n istio

kubectl delete   AuthorizationPolicy  allow-nothing -n istio-system



kubectl port-forward --address 0.0.0.0 -n istio productpage-v1-659776cb44-rrr87 15000:15000

![1628490344(1)](images\1628490344(1).jpg)





#### remote.ip

修改svc

kubectl edit svc istio-ingressgateway  -n istio-system

externalTrafficPolicy: Local

黑白名单

##### values

productpage-when-remote-ip-values.yaml

kubectl apply -f productpage-when-remote-ip-values.yaml -n istio

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
 - when:
   - key: remote.ip
     values:
     - "192.168.198.1/32"
```

allow-nothing.yaml

所有名称空间

kubectl apply -f allow-nothing.yaml -n istio-system

```
apiVersion: security.istio.io/v1beta1
kind: AuthorizationPolicy
metadata:
  name: allow-nothing
spec:
  # This matches nothing, the action defaults to ALLOW if not specified.
  {}
```

 

gateway/gateway-01.yaml

kubectl apply -f gateway-01.yaml -n istio

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



清理：

kubectl delete gw bookinfo-gateway -n istio

kubectl delete vs bookinfo -n istio

kubectl delete AuthorizationPolicy productpage  -n istio

kubectl delete   AuthorizationPolicy  allow-nothing -n istio-system



kubectl port-forward --address 0.0.0.0 -n istio productpage-v1-659776cb44-rrr87 15000:15000

![1628490404](images\1628490404.jpg)







##### notValues

修改svc

kubectl edit svc istio-ingressgateway  -n istio-system

externalTrafficPolicy: Local

黑白名单

productpage-when-remote-ip-notValues.yaml

kubectl apply -f productpage-when-remote-ip-notValues.yaml -n istio

```
apiVersion: security.istio.io/v1beta1
kind: AuthorizationPolicy
metadata:
 name: productpage
spec:
 selector:
   matchLabels:
     app: productpage
 action: ALLOW
 rules:
 - when:
   - key: remote.ip
     notValues:
     - "192.168.198.1/32"
```

allow-nothing.yaml

所有名称空间

kubectl apply -f allow-nothing.yaml -n istio-system

```
apiVersion: security.istio.io/v1beta1
kind: AuthorizationPolicy
metadata:
  name: allow-nothing
spec:
  # This matches nothing, the action defaults to ALLOW if not specified.
  {}
```

 

gateway/gateway-01.yaml

kubectl apply -f gateway-01.yaml -n istio

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



清理：

kubectl delete gw bookinfo-gateway -n istio

kubectl delete vs bookinfo -n istio

kubectl delete AuthorizationPolicy productpage  -n istio

kubectl delete   AuthorizationPolicy  allow-nothing -n istio-system



kubectl port-forward --address 0.0.0.0 -n istio productpage-v1-659776cb44-rrr87 15000:15000

![1628490459(1)](images\1628490459(1).jpg)







#### source.namespace

##### values

productpage-when-source-namespace-values.yaml

kubectl apply -f productpage-when-source-namespace-values.yaml -n istio

```
apiVersion: security.istio.io/v1beta1
kind: AuthorizationPolicy
metadata:
 name: productpage
spec:
 selector:
   matchLabels:
     app: productpage
 action: ALLOW
 rules:
 - when:
   - key: source.namespace
     values:
     - "istio-system"
```

allow-nothing.yaml

所有名称空间

kubectl apply -f allow-nothing.yaml -n istio-system

```
apiVersion: security.istio.io/v1beta1
kind: AuthorizationPolicy
metadata:
  name: allow-nothing
spec:
  # This matches nothing, the action defaults to ALLOW if not specified.
  {}
```

 

gateway/gateway-01.yaml

kubectl apply -f gateway-01.yaml -n istio

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



清理：

kubectl delete gw bookinfo-gateway -n istio

kubectl delete vs bookinfo -n istio

kubectl delete AuthorizationPolicy productpage  -n istio

kubectl delete   AuthorizationPolicy  allow-nothing -n istio-system



kubectl port-forward --address 0.0.0.0 -n istio productpage-v1-659776cb44-rrr87 15000:15000

![1628490521(1)](images\1628490521(1).jpg)







##### notValues

productpage-when-source-namespace-notValues.yaml

kubectl apply -f productpage-when-source-namespace-notValues.yaml -n istio

```
apiVersion: security.istio.io/v1beta1
kind: AuthorizationPolicy
metadata:
 name: productpage
spec:
 selector:
   matchLabels:
     app: productpage
 action: ALLOW
 rules:
 - when:
   - key: source.namespace
     notValues:
     - "istio-system"
```

allow-nothing.yaml

所有名称空间

kubectl apply -f allow-nothing.yaml -n istio-system

```
apiVersion: security.istio.io/v1beta1
kind: AuthorizationPolicy
metadata:
  name: allow-nothing
spec:
  # This matches nothing, the action defaults to ALLOW if not specified.
  {}
```

 

gateway/gateway-01.yaml

kubectl apply -f gateway-01.yaml -n istio

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



清理：

kubectl delete gw bookinfo-gateway -n istio

kubectl delete vs bookinfo -n istio

kubectl delete AuthorizationPolicy productpage  -n istio

kubectl delete   AuthorizationPolicy  allow-nothing -n istio-system



kubectl port-forward --address 0.0.0.0 -n istio productpage-v1-659776cb44-rrr87 15000:15000

![1628490583(1)](images\1628490583(1).jpg)







#### source.principal

##### values

productpage-when-source-principal-values.yaml

kubectl apply -f productpage-when-source-principal-values.yaml -n istio

```
apiVersion: security.istio.io/v1beta1
kind: AuthorizationPolicy
metadata:
 name: productpage
spec:
 selector:
   matchLabels:
     app: productpage
     version: v1
 action: ALLOW
 rules:
 - when:
   - key: source.principal
     values: 
     - "cluster.local/ns/istio-system/sa/istio-ingressgateway-service-account"
```

allow-nothing.yaml

所有名称空间

kubectl apply -f allow-nothing.yaml -n istio-system

```
apiVersion: security.istio.io/v1beta1
kind: AuthorizationPolicy
metadata:
  name: allow-nothing
spec:
  # This matches nothing, the action defaults to ALLOW if not specified.
  {}
```

 

gateway/gateway-01.yaml

kubectl apply -f gateway-01.yaml -n istio

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



清理：

kubectl delete gw bookinfo-gateway -n istio

kubectl delete vs bookinfo -n istio

kubectl delete AuthorizationPolicy productpage  -n istio

kubectl delete   AuthorizationPolicy  allow-nothing -n istio-system



kubectl port-forward --address 0.0.0.0 -n istio productpage-v1-659776cb44-rrr87 15000:15000

![1628490663(1)](images\1628490663(1).jpg)







##### notValues

productpage-when-source-principal-notValues.yaml

kubectl apply -f productpage-when-source-principal-notValues.yaml -n istio

```
apiVersion: security.istio.io/v1beta1
kind: AuthorizationPolicy
metadata:
 name: productpage
spec:
 selector:
   matchLabels:
     app: productpage
     version: v1
 action: ALLOW
 rules:
 - when:
   - key: source.principal
     notValues: 
     - "cluster.local/ns/istio-system/sa/istio-ingressgateway-service-account"
```

allow-nothing.yaml

所有名称空间

kubectl apply -f allow-nothing.yaml -n istio-system

```
apiVersion: security.istio.io/v1beta1
kind: AuthorizationPolicy
metadata:
  name: allow-nothing
spec:
  # This matches nothing, the action defaults to ALLOW if not specified.
  {}
```

 

gateway/gateway-01.yaml

kubectl apply -f gateway-01.yaml -n istio

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



清理：

kubectl delete gw bookinfo-gateway -n istio

kubectl delete vs bookinfo -n istio

kubectl delete AuthorizationPolicy productpage  -n istio

kubectl delete   AuthorizationPolicy  allow-nothing -n istio-system



kubectl port-forward --address 0.0.0.0 -n istio productpage-v1-659776cb44-rrr87 15000:15000

![1628490711(1)](images\1628490711(1).jpg)







#### request.auth.principal

jwt相关

##### values

productpage-when-request-auth-principal-values.yaml

kubectl apply -f productpage-when-request-auth-principal-values.yaml -n istio

```
apiVersion: security.istio.io/v1beta1
kind: AuthorizationPolicy
metadata:
 name: productpage
spec:
 selector:
   matchLabels:
     app: productpage
     version: v1
 action: ALLOW
 rules:
 - when:
   - key: request.auth.principal
     values: 
     - "testing@secure.istio.io/testing@secure.istio.io"
```

启用jwt

requestauthentications/ra-example-productpage.yaml

kubectl apply -f ra-example-productpage.yaml -n istio

```
apiVersion: "security.istio.io/v1beta1"
kind: "RequestAuthentication"
metadata:
  name: "jwt-example"
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
    forwardOriginalToken: true
```





allow-nothing.yaml

所有名称空间

kubectl apply -f allow-nothing.yaml -n istio-system

```
apiVersion: security.istio.io/v1beta1
kind: AuthorizationPolicy
metadata:
  name: allow-nothing
spec:
  # This matches nothing, the action defaults to ALLOW if not specified.
  {}
```

 

gateway/gateway-01.yaml

kubectl apply -f gateway-01.yaml -n istio

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

TOKEN=eyJhbGciOiJSUzI1NiIsImtpZCI6IkRIRmJwb0lVcXJZOHQyenBBMnFYZkNtcjVWTzVaRXI0UnpIVV8tZW52dlEiLCJ0eXAiOiJKV1QifQ.eyJleHAiOjM1MzczOTExMDQsImdyb3VwcyI6WyJncm91cDEiLCJncm91cDIiXSwiaWF0IjoxNTM3MzkxMTA0LCJpc3MiOiJ0ZXN0aW5nQHNlY3VyZS5pc3Rpby5pbyIsInNjb3BlIjpbInNjb3BlMSIsInNjb3BlMiJdLCJzdWIiOiJ0ZXN0aW5nQHNlY3VyZS5pc3Rpby5pbyJ9.EdJnEZSH6X8hcyEii7c8H5lnhgjB5dwo07M5oheC8Xz8mOllyg--AHCFWHybM48reunF--oGaG6IXVngCEpVF0_P5DwsUoBgpPmK1JOaKN6_pe9sh0ZwTtdgK_RP01PuI7kUdbOTlkuUi2AO-qUyOm7Art2POzo36DLQlUXv8Ad7NBOqfQaKjE9ndaPWT7aexUsBHxmgiGbz1SyLH879f7uHYPbPKlpHU6P9S-DaKnGLaEchnoKnov7ajhrEhGXAQRukhDPKUHO9L30oPIr5IJllEQfHYtt6IZvlNUGeLUcif3wpry1R5tBXRicx2sXMQ7LyuDremDbcNy_iE76Upg

curl 192.168.198.154:30986/productpage -H "Authorization: Bearer ${TOKEN}"

清理：

kubectl delete gw bookinfo-gateway -n istio

kubectl delete vs bookinfo -n istio

kubectl delete AuthorizationPolicy productpage  -n istio

kubectl delete   AuthorizationPolicy  allow-nothing -n istio-system

kubectl delete -f ra-example-productpage.yaml -n istio



kubectl port-forward --address 0.0.0.0 -n istio productpage-v1-659776cb44-rrr87 15000:15000

![1628490801(1)](images\1628490801(1).jpg)







##### notValues

productpage-when-request-auth-principal-notValues.yaml

kubectl apply -f productpage-when-request-auth-principal-notValues.yaml -n istio

```
apiVersion: security.istio.io/v1beta1
kind: AuthorizationPolicy
metadata:
 name: productpage
spec:
 selector:
   matchLabels:
     app: productpage
     version: v1
 action: ALLOW
 rules:
 - when:
   - key: request.auth.principal
     notValues: 
     - "testing@secure.istio.io/testing@secure.istio.io"
```

启用jwt

requestauthentications/ra-example-productpage.yaml

kubectl apply -f ra-example-productpage.yaml -n istio

```
apiVersion: "security.istio.io/v1beta1"
kind: "RequestAuthentication"
metadata:
  name: "jwt-example"
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
    forwardOriginalToken: true
```



allow-nothing.yaml

所有名称空间

kubectl apply -f allow-nothing.yaml -n istio-system

```
apiVersion: security.istio.io/v1beta1
kind: AuthorizationPolicy
metadata:
  name: allow-nothing
spec:
  # This matches nothing, the action defaults to ALLOW if not specified.
  {}
```

 

gateway/gateway-01.yaml

kubectl apply -f gateway-01.yaml -n istio

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



TOKEN=eyJhbGciOiJSUzI1NiIsImtpZCI6IkRIRmJwb0lVcXJZOHQyenBBMnFYZkNtcjVWTzVaRXI0UnpIVV8tZW52dlEiLCJ0eXAiOiJKV1QifQ.eyJleHAiOjM1MzczOTExMDQsImdyb3VwcyI6WyJncm91cDEiLCJncm91cDIiXSwiaWF0IjoxNTM3MzkxMTA0LCJpc3MiOiJ0ZXN0aW5nQHNlY3VyZS5pc3Rpby5pbyIsInNjb3BlIjpbInNjb3BlMSIsInNjb3BlMiJdLCJzdWIiOiJ0ZXN0aW5nQHNlY3VyZS5pc3Rpby5pbyJ9.EdJnEZSH6X8hcyEii7c8H5lnhgjB5dwo07M5oheC8Xz8mOllyg--AHCFWHybM48reunF--oGaG6IXVngCEpVF0_P5DwsUoBgpPmK1JOaKN6_pe9sh0ZwTtdgK_RP01PuI7kUdbOTlkuUi2AO-qUyOm7Art2POzo36DLQlUXv8Ad7NBOqfQaKjE9ndaPWT7aexUsBHxmgiGbz1SyLH879f7uHYPbPKlpHU6P9S-DaKnGLaEchnoKnov7ajhrEhGXAQRukhDPKUHO9L30oPIr5IJllEQfHYtt6IZvlNUGeLUcif3wpry1R5tBXRicx2sXMQ7LyuDremDbcNy_iE76Upg

curl 192.168.198.154:30986/productpage -H "Authorization: Bearer ${TOKEN}"

清理：

kubectl delete gw bookinfo-gateway -n istio

kubectl delete vs bookinfo -n istio

kubectl delete AuthorizationPolicy productpage  -n istio

kubectl delete   AuthorizationPolicy  allow-nothing -n istio-system

kubectl delete -f ra-example-productpage.yaml -n istio



kubectl port-forward --address 0.0.0.0 -n istio productpage-v1-659776cb44-rrr87 15000:15000

![1628490857(1)](images\1628490857(1).jpg)







#### request.auth.audiences

相当于request.auth.claims[aud]

##### values

productpage-when-request-auth-audiences-values.yaml

kubectl apply -f productpage-when-request-auth-audiences-values.yaml -n istio

```
apiVersion: security.istio.io/v1beta1
kind: AuthorizationPolicy
metadata:
 name: productpage
spec:
 selector:
   matchLabels:
     app: productpage
     version: v1
 action: ALLOW
 rules:
 - when:
   - key: request.auth.audiences
     values: 
     - "app"
     - “web”
```

启用jwt

requestauthentications/ra-example-productpage.yaml

kubectl apply -f ra-example-productpage.yaml -n istio

```
apiVersion: "security.istio.io/v1beta1"
kind: "RequestAuthentication"
metadata:
  name: "jwt-example"
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
    forwardOriginalToken: true
```



allow-nothing.yaml

所有名称空间

kubectl apply -f allow-nothing.yaml -n istio-system

```
apiVersion: security.istio.io/v1beta1
kind: AuthorizationPolicy
metadata:
  name: allow-nothing
spec:
  # This matches nothing, the action defaults to ALLOW if not specified.
  {}
```

 

gateway/gateway-01.yaml

kubectl apply -f gateway-01.yaml -n istio

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

TOKEN=eyJhbGciOiJSUzI1NiIsImtpZCI6IkRIRmJwb0lVcXJZOHQyenBBMnFYZkNtcjVWTzVaRXI0UnpIVV8tZW52dlEiLCJ0eXAiOiJKV1QifQ.eyJleHAiOjM1MzczOTExMDQsImdyb3VwcyI6WyJncm91cDEiLCJncm91cDIiXSwiaWF0IjoxNTM3MzkxMTA0LCJpc3MiOiJ0ZXN0aW5nQHNlY3VyZS5pc3Rpby5pbyIsInNjb3BlIjpbInNjb3BlMSIsInNjb3BlMiJdLCJzdWIiOiJ0ZXN0aW5nQHNlY3VyZS5pc3Rpby5pbyJ9.EdJnEZSH6X8hcyEii7c8H5lnhgjB5dwo07M5oheC8Xz8mOllyg--AHCFWHybM48reunF--oGaG6IXVngCEpVF0_P5DwsUoBgpPmK1JOaKN6_pe9sh0ZwTtdgK_RP01PuI7kUdbOTlkuUi2AO-qUyOm7Art2POzo36DLQlUXv8Ad7NBOqfQaKjE9ndaPWT7aexUsBHxmgiGbz1SyLH879f7uHYPbPKlpHU6P9S-DaKnGLaEchnoKnov7ajhrEhGXAQRukhDPKUHO9L30oPIr5IJllEQfHYtt6IZvlNUGeLUcif3wpry1R5tBXRicx2sXMQ7LyuDremDbcNy_iE76Upg

curl 192.168.198.154:30986/productpage -H "Authorization: Bearer ${TOKEN}"

清理：

kubectl delete gw bookinfo-gateway -n istio

kubectl delete vs bookinfo -n istio

kubectl delete AuthorizationPolicy productpage  -n istio

kubectl delete   AuthorizationPolicy  allow-nothing -n istio-system



kubectl port-forward --address 0.0.0.0 -n istio productpage-v1-659776cb44-rrr87 15000:15000

![1628490932(1)](images\1628490932(1).jpg)







##### notValues

productpage-when-request-auth-audiences-notValues.yaml

kubectl apply -f productpage-when-request-auth-audiences-notValues.yaml -n istio

```
apiVersion: security.istio.io/v1beta1
kind: AuthorizationPolicy
metadata:
 name: productpage
spec:
 selector:
   matchLabels:
     app: productpage
     version: v1
 action: ALLOW
 rules:
 - when:
   - key: request.auth.audiences
     notValues: 
     - "app"
     - “web”
```

启用jwt

requestauthentications/ra-example-productpage.yaml

kubectl apply -f ra-example-productpage.yaml -n istio

```
apiVersion: "security.istio.io/v1beta1"
kind: "RequestAuthentication"
metadata:
  name: "jwt-example"
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
    forwardOriginalToken: true
```



allow-nothing.yaml

所有名称空间

kubectl apply -f allow-nothing.yaml -n istio-system

```
apiVersion: security.istio.io/v1beta1
kind: AuthorizationPolicy
metadata:
  name: allow-nothing
spec:
  # This matches nothing, the action defaults to ALLOW if not specified.
  {}
```

 

gateway/gateway-01.yaml

kubectl apply -f gateway-01.yaml -n istio

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

TOKEN=eyJhbGciOiJSUzI1NiIsImtpZCI6IkRIRmJwb0lVcXJZOHQyenBBMnFYZkNtcjVWTzVaRXI0UnpIVV8tZW52dlEiLCJ0eXAiOiJKV1QifQ.eyJleHAiOjM1MzczOTExMDQsImdyb3VwcyI6WyJncm91cDEiLCJncm91cDIiXSwiaWF0IjoxNTM3MzkxMTA0LCJpc3MiOiJ0ZXN0aW5nQHNlY3VyZS5pc3Rpby5pbyIsInNjb3BlIjpbInNjb3BlMSIsInNjb3BlMiJdLCJzdWIiOiJ0ZXN0aW5nQHNlY3VyZS5pc3Rpby5pbyJ9.EdJnEZSH6X8hcyEii7c8H5lnhgjB5dwo07M5oheC8Xz8mOllyg--AHCFWHybM48reunF--oGaG6IXVngCEpVF0_P5DwsUoBgpPmK1JOaKN6_pe9sh0ZwTtdgK_RP01PuI7kUdbOTlkuUi2AO-qUyOm7Art2POzo36DLQlUXv8Ad7NBOqfQaKjE9ndaPWT7aexUsBHxmgiGbz1SyLH879f7uHYPbPKlpHU6P9S-DaKnGLaEchnoKnov7ajhrEhGXAQRukhDPKUHO9L30oPIr5IJllEQfHYtt6IZvlNUGeLUcif3wpry1R5tBXRicx2sXMQ7LyuDremDbcNy_iE76Upg

curl 192.168.198.154:30986/productpage --header "test:test"

清理：

kubectl delete gw bookinfo-gateway -n istio

kubectl delete vs bookinfo -n istio

kubectl delete AuthorizationPolicy productpage  -n istio

kubectl delete   AuthorizationPolicy  allow-nothing -n istio-system



kubectl port-forward --address 0.0.0.0 -n istio productpage-v1-659776cb44-rrr87 15000:15000

![1628490988(1)](images\1628490988(1).jpg)







#### request.auth.presenter

相当于request.auth.claims[azp]

 authorized presenter 

##### values

productpage-when-request-auth-presenter-values.yaml

kubectl apply -f productpage-when-request-auth-presenter-values.yaml -n istio

```
apiVersion: security.istio.io/v1beta1
kind: AuthorizationPolicy
metadata:
 name: productpage
spec:
 selector:
   matchLabels:
     app: productpage
     version: v1
 action: ALLOW
 rules:
 - when:
   - key: request.auth.presenter
     values: 
     - "app"
```

启用jwt

requestauthentications/ra-example-productpage.yaml

kubectl apply -f ra-example-productpage.yaml -n istio

```
apiVersion: "security.istio.io/v1beta1"
kind: "RequestAuthentication"
metadata:
  name: "jwt-example"
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
    forwardOriginalToken: true
```



allow-nothing.yaml

所有名称空间

kubectl apply -f allow-nothing.yaml -n istio-system

```
apiVersion: security.istio.io/v1beta1
kind: AuthorizationPolicy
metadata:
  name: allow-nothing
spec:
  # This matches nothing, the action defaults to ALLOW if not specified.
  {}
```

 

gateway/gateway-01.yaml

kubectl apply -f gateway-01.yaml -n istio

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

TOKEN=eyJhbGciOiJSUzI1NiIsImtpZCI6IkRIRmJwb0lVcXJZOHQyenBBMnFYZkNtcjVWTzVaRXI0UnpIVV8tZW52dlEiLCJ0eXAiOiJKV1QifQ.eyJleHAiOjM1MzczOTExMDQsImdyb3VwcyI6WyJncm91cDEiLCJncm91cDIiXSwiaWF0IjoxNTM3MzkxMTA0LCJpc3MiOiJ0ZXN0aW5nQHNlY3VyZS5pc3Rpby5pbyIsInNjb3BlIjpbInNjb3BlMSIsInNjb3BlMiJdLCJzdWIiOiJ0ZXN0aW5nQHNlY3VyZS5pc3Rpby5pbyJ9.EdJnEZSH6X8hcyEii7c8H5lnhgjB5dwo07M5oheC8Xz8mOllyg--AHCFWHybM48reunF--oGaG6IXVngCEpVF0_P5DwsUoBgpPmK1JOaKN6_pe9sh0ZwTtdgK_RP01PuI7kUdbOTlkuUi2AO-qUyOm7Art2POzo36DLQlUXv8Ad7NBOqfQaKjE9ndaPWT7aexUsBHxmgiGbz1SyLH879f7uHYPbPKlpHU6P9S-DaKnGLaEchnoKnov7ajhrEhGXAQRukhDPKUHO9L30oPIr5IJllEQfHYtt6IZvlNUGeLUcif3wpry1R5tBXRicx2sXMQ7LyuDremDbcNy_iE76Upg

curl 192.168.198.154:30986/productpage -H "Authorization: Bearer ${TOKEN}"

清理：

kubectl delete gw bookinfo-gateway -n istio

kubectl delete vs bookinfo -n istio

kubectl delete AuthorizationPolicy productpage  -n istio

kubectl delete   AuthorizationPolicy  allow-nothing -n istio-system



kubectl port-forward --address 0.0.0.0 -n istio productpage-v1-659776cb44-rrr87 15000:15000

![1628491081(1)](images\1628491081(1).jpg)







##### notValues

productpage-when-request-auth-presenter-notValues.yaml

kubectl apply -f productpage-when-request-auth-presenter-notValues.yaml -n istio

```
apiVersion: security.istio.io/v1beta1
kind: AuthorizationPolicy
metadata:
 name: productpage
spec:
 selector:
   matchLabels:
     app: productpage
     version: v1
 action: ALLOW
 rules:
 - when:
   - key: request.auth.presenter
     notValues: 
     - "app"
```

启用jwt

requestauthentications/ra-example-productpage.yaml

kubectl apply -f ra-example-productpage.yaml -n istio

```
apiVersion: "security.istio.io/v1beta1"
kind: "RequestAuthentication"
metadata:
  name: "jwt-example"
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
    forwardOriginalToken: true
```



allow-nothing.yaml

所有名称空间

kubectl apply -f allow-nothing.yaml -n istio-system

```
apiVersion: security.istio.io/v1beta1
kind: AuthorizationPolicy
metadata:
  name: allow-nothing
spec:
  # This matches nothing, the action defaults to ALLOW if not specified.
  {}
```

 

gateway/gateway-01.yaml

kubectl apply -f gateway-01.yaml -n istio

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

TOKEN=eyJhbGciOiJSUzI1NiIsImtpZCI6IkRIRmJwb0lVcXJZOHQyenBBMnFYZkNtcjVWTzVaRXI0UnpIVV8tZW52dlEiLCJ0eXAiOiJKV1QifQ.eyJleHAiOjM1MzczOTExMDQsImdyb3VwcyI6WyJncm91cDEiLCJncm91cDIiXSwiaWF0IjoxNTM3MzkxMTA0LCJpc3MiOiJ0ZXN0aW5nQHNlY3VyZS5pc3Rpby5pbyIsInNjb3BlIjpbInNjb3BlMSIsInNjb3BlMiJdLCJzdWIiOiJ0ZXN0aW5nQHNlY3VyZS5pc3Rpby5pbyJ9.EdJnEZSH6X8hcyEii7c8H5lnhgjB5dwo07M5oheC8Xz8mOllyg--AHCFWHybM48reunF--oGaG6IXVngCEpVF0_P5DwsUoBgpPmK1JOaKN6_pe9sh0ZwTtdgK_RP01PuI7kUdbOTlkuUi2AO-qUyOm7Art2POzo36DLQlUXv8Ad7NBOqfQaKjE9ndaPWT7aexUsBHxmgiGbz1SyLH879f7uHYPbPKlpHU6P9S-DaKnGLaEchnoKnov7ajhrEhGXAQRukhDPKUHO9L30oPIr5IJllEQfHYtt6IZvlNUGeLUcif3wpry1R5tBXRicx2sXMQ7LyuDremDbcNy_iE76Upg

curl 192.168.198.154:30986/productpage -H "Authorization: Bearer ${TOKEN}"

清理：

kubectl delete gw bookinfo-gateway -n istio

kubectl delete vs bookinfo -n istio

kubectl delete AuthorizationPolicy productpage  -n istio

kubectl delete   AuthorizationPolicy  allow-nothing -n istio-system



kubectl port-forward --address 0.0.0.0 -n istio productpage-v1-659776cb44-rrr87 15000:15000

![1628491142(1)](images\1628491142(1).jpg)







#### request.auth.claims

jwt相关

##### values

productpage-when-request-auth-claims-values.yaml

kubectl apply -f productpage-when-request-auth-claims-values.yaml -n istio

```
apiVersion: security.istio.io/v1beta1
kind: AuthorizationPolicy
metadata:
 name: productpage
spec:
 selector:
   matchLabels:
     app: productpage
     version: v1
 action: ALLOW
 rules:
 - when:
   - key: request.auth.claims[groups]
     values: 
     - "group1"
```

启用jwt

requestauthentications/ra-example-productpage.yaml

kubectl apply -f ra-example-productpage.yaml -n istio

```
apiVersion: "security.istio.io/v1beta1"
kind: "RequestAuthentication"
metadata:
  name: "jwt-example"
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
    forwardOriginalToken: true
```



allow-nothing.yaml

所有名称空间

kubectl apply -f allow-nothing.yaml -n istio-system

```
apiVersion: security.istio.io/v1beta1
kind: AuthorizationPolicy
metadata:
  name: allow-nothing
spec:
  # This matches nothing, the action defaults to ALLOW if not specified.
  {}
```

 

gateway/gateway-01.yaml

kubectl apply -f gateway-01.yaml -n istio

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

TOKEN=eyJhbGciOiJSUzI1NiIsImtpZCI6IkRIRmJwb0lVcXJZOHQyenBBMnFYZkNtcjVWTzVaRXI0UnpIVV8tZW52dlEiLCJ0eXAiOiJKV1QifQ.eyJleHAiOjM1MzczOTExMDQsImdyb3VwcyI6WyJncm91cDEiLCJncm91cDIiXSwiaWF0IjoxNTM3MzkxMTA0LCJpc3MiOiJ0ZXN0aW5nQHNlY3VyZS5pc3Rpby5pbyIsInNjb3BlIjpbInNjb3BlMSIsInNjb3BlMiJdLCJzdWIiOiJ0ZXN0aW5nQHNlY3VyZS5pc3Rpby5pbyJ9.EdJnEZSH6X8hcyEii7c8H5lnhgjB5dwo07M5oheC8Xz8mOllyg--AHCFWHybM48reunF--oGaG6IXVngCEpVF0_P5DwsUoBgpPmK1JOaKN6_pe9sh0ZwTtdgK_RP01PuI7kUdbOTlkuUi2AO-qUyOm7Art2POzo36DLQlUXv8Ad7NBOqfQaKjE9ndaPWT7aexUsBHxmgiGbz1SyLH879f7uHYPbPKlpHU6P9S-DaKnGLaEchnoKnov7ajhrEhGXAQRukhDPKUHO9L30oPIr5IJllEQfHYtt6IZvlNUGeLUcif3wpry1R5tBXRicx2sXMQ7LyuDremDbcNy_iE76Upg

curl 192.168.198.154:30986/productpage -H "Authorization: Bearer ${TOKEN}"

清理：

kubectl delete gw bookinfo-gateway -n istio

kubectl delete vs bookinfo -n istio

kubectl delete AuthorizationPolicy productpage  -n istio

kubectl delete   AuthorizationPolicy  allow-nothing -n istio-system



kubectl port-forward --address 0.0.0.0 -n istio productpage-v1-659776cb44-rrr87 15000:15000

![1628491216(1)](images\1628491216(1).jpg)







##### notValues

productpage-when-request-auth-claims-notValues.yaml

kubectl apply -f productpage-when-request-auth-claims-notValues.yaml -n istio

```
apiVersion: security.istio.io/v1beta1
kind: AuthorizationPolicy
metadata:
 name: productpage
spec:
 selector:
   matchLabels:
     app: productpage
     version: v1
 action: ALLOW
 rules:
 - when:
   - key: request.auth.claims[groups]
     notValues: 
     - "group1"
```

启用jwt

requestauthentications/ra-example-productpage.yaml

kubectl apply -f ra-example-productpage.yaml -n istio

```
apiVersion: "security.istio.io/v1beta1"
kind: "RequestAuthentication"
metadata:
  name: "jwt-example"
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
    forwardOriginalToken: true
```



allow-nothing.yaml

所有名称空间

kubectl apply -f allow-nothing.yaml -n istio-system

```
apiVersion: security.istio.io/v1beta1
kind: AuthorizationPolicy
metadata:
  name: allow-nothing
spec:
  # This matches nothing, the action defaults to ALLOW if not specified.
  {}
```

 

gateway/gateway-01.yaml

kubectl apply -f gateway-01.yaml -n istio

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

TOKEN=eyJhbGciOiJSUzI1NiIsImtpZCI6IkRIRmJwb0lVcXJZOHQyenBBMnFYZkNtcjVWTzVaRXI0UnpIVV8tZW52dlEiLCJ0eXAiOiJKV1QifQ.eyJleHAiOjM1MzczOTExMDQsImdyb3VwcyI6WyJncm91cDEiLCJncm91cDIiXSwiaWF0IjoxNTM3MzkxMTA0LCJpc3MiOiJ0ZXN0aW5nQHNlY3VyZS5pc3Rpby5pbyIsInNjb3BlIjpbInNjb3BlMSIsInNjb3BlMiJdLCJzdWIiOiJ0ZXN0aW5nQHNlY3VyZS5pc3Rpby5pbyJ9.EdJnEZSH6X8hcyEii7c8H5lnhgjB5dwo07M5oheC8Xz8mOllyg--AHCFWHybM48reunF--oGaG6IXVngCEpVF0_P5DwsUoBgpPmK1JOaKN6_pe9sh0ZwTtdgK_RP01PuI7kUdbOTlkuUi2AO-qUyOm7Art2POzo36DLQlUXv8Ad7NBOqfQaKjE9ndaPWT7aexUsBHxmgiGbz1SyLH879f7uHYPbPKlpHU6P9S-DaKnGLaEchnoKnov7ajhrEhGXAQRukhDPKUHO9L30oPIr5IJllEQfHYtt6IZvlNUGeLUcif3wpry1R5tBXRicx2sXMQ7LyuDremDbcNy_iE76Upg

curl 192.168.198.154:30986/productpage -H "Authorization: Bearer ${TOKEN}"

清理：

kubectl delete gw bookinfo-gateway -n istio

kubectl delete vs bookinfo -n istio

kubectl delete AuthorizationPolicy productpage  -n istio

kubectl delete   AuthorizationPolicy  allow-nothing -n istio-system



kubectl port-forward --address 0.0.0.0 -n istio productpage-v1-659776cb44-rrr87 15000:15000

![1628491281(1)](images\1628491281(1).jpg)







#### destination.ip

##### values

productpage-when-destination-ip-values.yaml

kubectl apply -f productpage-when-destination-ip-values.yaml -n istio

```
apiVersion: security.istio.io/v1beta1
kind: AuthorizationPolicy
metadata:
 name: productpage
spec:
 selector:
   matchLabels:
     app: productpage
     version: v1
 action: ALLOW
 rules:
 - when:
   - key: destination.ip
     values: 
     - "172.20.0.0/16"
```

allow-nothing.yaml

所有名称空间

kubectl apply -f allow-nothing.yaml -n istio-system

```
apiVersion: security.istio.io/v1beta1
kind: AuthorizationPolicy
metadata:
  name: allow-nothing
spec:
  # This matches nothing, the action defaults to ALLOW if not specified.
  {}
```

 

gateway/gateway-01.yaml

kubectl apply -f gateway-01.yaml -n istio

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



清理：

kubectl delete gw bookinfo-gateway -n istio

kubectl delete vs bookinfo -n istio

kubectl delete AuthorizationPolicy productpage  -n istio

kubectl delete   AuthorizationPolicy  allow-nothing -n istio-system



kubectl port-forward --address 0.0.0.0 -n istio productpage-v1-659776cb44-rrr87 15000:15000

![1628491348(1)](images\1628491348(1).jpg)







##### notValues

productpage-when-destination-ip-notValues.yaml

kubectl apply -f productpage-when-destination-ip-notValues.yaml -n istio

```
apiVersion: security.istio.io/v1beta1
kind: AuthorizationPolicy
metadata:
 name: productpage
spec:
 selector:
   matchLabels:
     app: productpage
     version: v1
 action: ALLOW
 rules:
 - when:
   - key: destination.ip
     notValues: 
     - "172.20.0.0/16"
```

allow-nothing.yaml

所有名称空间

kubectl apply -f allow-nothing.yaml -n istio-system

```
apiVersion: security.istio.io/v1beta1
kind: AuthorizationPolicy
metadata:
  name: allow-nothing
spec:
  # This matches nothing, the action defaults to ALLOW if not specified.
  {}
```

 

gateway/gateway-01.yaml

kubectl apply -f gateway-01.yaml -n istio

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



清理：

kubectl delete gw bookinfo-gateway -n istio

kubectl delete vs bookinfo -n istio

kubectl delete AuthorizationPolicy productpage  -n istio

kubectl delete   AuthorizationPolicy  allow-nothing -n istio-system



kubectl port-forward --address 0.0.0.0 -n istio productpage-v1-659776cb44-rrr87 15000:15000

![1628491412(1)](images\1628491412(1).jpg)







#### destination.port

##### values

productpage-when-destination-port-values.yaml

kubectl apply -f productpage-when-destination-port-values.yaml -n istio

```
apiVersion: security.istio.io/v1beta1
kind: AuthorizationPolicy
metadata:
 name: productpage
spec:
 selector:
   matchLabels:
     app: productpage
     version: v1
 action: ALLOW
 rules:
 - when:
   - key: destination.port
     values: 
     - "9080"
```

allow-nothing.yaml

所有名称空间

kubectl apply -f allow-nothing.yaml -n istio-system

```
apiVersion: security.istio.io/v1beta1
kind: AuthorizationPolicy
metadata:
  name: allow-nothing
spec:
  # This matches nothing, the action defaults to ALLOW if not specified.
  {}
```

 

gateway/gateway-01.yaml

kubectl apply -f gateway-01.yaml -n istio

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



清理：

kubectl delete gw bookinfo-gateway -n istio

kubectl delete vs bookinfo -n istio

kubectl delete AuthorizationPolicy productpage  -n istio

kubectl delete   AuthorizationPolicy  allow-nothing -n istio-system



kubectl port-forward --address 0.0.0.0 -n istio productpage-v1-659776cb44-rrr87 15000:15000

![1628491491](images\1628491491.jpg)







##### notValues

productpage-when-destination-port-notValues.yaml

kubectl apply -f productpage-when-destination-port-notValues.yaml -n istio

```
apiVersion: security.istio.io/v1beta1
kind: AuthorizationPolicy
metadata:
 name: productpage
spec:
 selector:
   matchLabels:
     app: productpage
     version: v1
 action: ALLOW
 rules:
 - when:
   - key: destination.port
     notValues: 
     - "9080"
```

allow-nothing.yaml

所有名称空间

kubectl apply -f allow-nothing.yaml -n istio-system

```
apiVersion: security.istio.io/v1beta1
kind: AuthorizationPolicy
metadata:
  name: allow-nothing
spec:
  # This matches nothing, the action defaults to ALLOW if not specified.
  {}
```

 

gateway/gateway-01.yaml

kubectl apply -f gateway-01.yaml -n istio

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



清理：

kubectl delete gw bookinfo-gateway -n istio

kubectl delete vs bookinfo -n istio

kubectl delete AuthorizationPolicy productpage  -n istio

kubectl delete   AuthorizationPolicy  allow-nothing -n istio-system



kubectl port-forward --address 0.0.0.0 -n istio productpage-v1-659776cb44-rrr87 15000:15000

![1628491553(1)](images\1628491553(1).jpg)







#### connection.sni

##### values

productpage-when-connection-sni-values.yaml

kubectl apply -f productpage-when-connection-sni-values.yaml -n istio

```
apiVersion: security.istio.io/v1beta1
kind: AuthorizationPolicy
metadata:
 name: productpage
spec:
 selector:
   matchLabels:
     app: productpage
     version: v1
 action: ALLOW
 rules:
 - when:
   - key: connection.sni
     values: 
     - "outbound_.9080_._.productpage.istio.svc.cluster.local"
```

requestedServerName的值

allow-nothing.yaml

所有名称空间

kubectl apply -f allow-nothing.yaml -n istio-system

```
apiVersion: security.istio.io/v1beta1
kind: AuthorizationPolicy
metadata:
  name: allow-nothing
spec:
  # This matches nothing, the action defaults to ALLOW if not specified.
  {}
```

 

gateway/gateway-01.yaml

kubectl apply -f gateway-01.yaml -n istio

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



清理：

kubectl delete gw bookinfo-gateway -n istio

kubectl delete vs bookinfo -n istio

kubectl delete AuthorizationPolicy productpage  -n istio

kubectl delete   AuthorizationPolicy  allow-nothing -n istio-system



kubectl port-forward --address 0.0.0.0 -n istio productpage-v1-659776cb44-rrr87 15000:15000

![1628491613(1)](images\1628491613(1).jpg)







##### notValues

productpage-when-connection-sni-notValues.yaml

kubectl apply -f productpage-when-connection-sni-notValues.yaml -n istio

```
apiVersion: security.istio.io/v1beta1
kind: AuthorizationPolicy
metadata:
 name: productpage
spec:
 selector:
   matchLabels:
     app: productpage
     version: v1
 action: ALLOW
 rules:
 - when:
   - key: connection.sni
     notValues: 
     - "outbound_.9080_._.productpage.istio.svc.cluster.local"
```

allow-nothing.yaml

所有名称空间

kubectl apply -f allow-nothing.yaml -n istio-system

```
apiVersion: security.istio.io/v1beta1
kind: AuthorizationPolicy
metadata:
  name: allow-nothing
spec:
  # This matches nothing, the action defaults to ALLOW if not specified.
  {}
```

 

gateway/gateway-01.yaml

kubectl apply -f gateway-01.yaml -n istio

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



清理：

kubectl delete gw bookinfo-gateway -n istio

kubectl delete vs bookinfo -n istio

kubectl delete AuthorizationPolicy productpage  -n istio

kubectl delete   AuthorizationPolicy  allow-nothing -n istio-system



kubectl port-forward --address 0.0.0.0 -n istio productpage-v1-659776cb44-rrr87 15000:15000

![1628491676(1)](images\1628491676(1).jpg)







#### experimental.envoy.filters.*

试验性的

暂时不验证

##### values

productpage-when-envoy-filters-mysql_proxy-values.yaml

kubectl apply -f productpage-when-envoy-filters-mysql_proxy-values.yaml -n istio

```
apiVersion: security.istio.io/v1beta1
kind: AuthorizationPolicy
metadata:
 name: productpage
spec:
 selector:
   matchLabels:
     app: productpage
     version: v1
 action: ALLOW
 rules:
 - when:
   - key: experimental.envoy.filters.network.mysql_proxy[db.table]
     values: 
     - "[update]"
```



##### notValues

productpage-when-envoy-filters-mysql_proxy-notValues.yaml

kubectl apply -f productpage-when-envoy-filters-mysql_proxy-notValues.yaml -n istio

```
apiVersion: security.istio.io/v1beta1
kind: AuthorizationPolicy
metadata:
 name: productpage
spec:
 selector:
   matchLabels:
     app: productpage
     version: v1
 action: ALLOW
 rules:
 - when:
   - key: experimental.envoy.filters.network.mysql_proxy[db.table]
     notValues: 
     - "[update]"
```



### 9-7组合配置

authorizationpolicies/productpage-complex.yaml

kubectl apply -f productpage-complex.yaml -n istio

```
apiVersion: security.istio.io/v1beta1
kind: AuthorizationPolicy
metadata:
  name: productpage
spec:
  action: ALLOW
  rules:
  - from:
    - source:
        principals: 
        - cluster.local/ns/istio-system/sa/istio-ingressgateway-service-account
        namespaces: 
        - istio-system
    to:
    - operation:
        methods: ["GET"]
        paths: ["/productpage"]
    - operation:
        methods: ["GET"]
        paths: ["/static/*"]
    - operation:
        methods: ["GET"]
        paths: ["/api/v1/products/*"]
    - operation:
        methods: ["GET"]
        paths: ["/logout"]
    - operation:
        methods: ["POST"]
        paths: ["/login"]
    when:
    - key: source.ip
      values:
      - "172.20.0.0/16"
```

allow-nothing.yaml

所有名称空间

kubectl apply -f allow-nothing.yaml -n istio-system

```
apiVersion: security.istio.io/v1beta1
kind: AuthorizationPolicy
metadata:
  name: allow-nothing
spec:
  # This matches nothing, the action defaults to ALLOW if not specified.
  {}
```

 

gateway/gateway-01.yaml

kubectl apply -f gateway-01.yaml -n istio

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

curl 192.168.198.154:30986/productpage --header "test:test"

清理：

kubectl delete gw bookinfo-gateway -n istio

kubectl delete vs bookinfo -n istio

kubectl delete AuthorizationPolicy productpage  -n istio

kubectl delete   AuthorizationPolicy  allow-nothing -n istio-system



kubectl port-forward --address 0.0.0.0 -n istio productpage-v1-659776cb44-rrr87 15000:15000

![1628491814(1)](images\1628491814(1).jpg)



![1628491866(1)](images\1628491866(1).jpg)





![1628491933(1)](images\1628491933(1).jpg)





### 9-8Dependency on mutual TLS

Istio uses mutual TLS to securely pass some information from the client to the server. Mutual TLS must be enabled before using any of the following fields in the authorization policy:

- the `principals` and `notPrincipals` field under the `source` section
- the `namespaces` and `notNamespaces` field under the `source` section
- the `source.principal` custom condition
- the `source.namespace` custom condition

Mutual TLS is not required if you don’t use any of the above fields in the authorization policy.