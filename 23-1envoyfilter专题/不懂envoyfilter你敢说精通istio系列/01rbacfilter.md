# 1什么是http rbac filter

rbac是基于角色的权限控制，http rbac filter是envoy中http类型的权限控制过滤器。我们istio中常用的权限控制资源AuthorizationPolicy，底层就是通过rbac filter实现的。rbac filter 名称为envoy.filters.http.rbac，type固定名称为type.googleapis.com/envoy.extensions.filters.http.rbac.v3.RBAC。rbac filter有两种类型，一种是network过滤器，还有一种是http过滤器，我们讲的是http类型的rbac。

# 2配置说明

{
  "rules": "{...}",
  "shadow_rules": "{...}",
  "shadow_rules_stat_prefix": "..."
}

rules: 配置规则

shadow_rules：规则不生效，但会记录日志

shadow_rules_stat_prefix：影子规则的stat前缀



新版：

**还不支持matcher**

```
{
  "rules": "{...}",
  "matcher": "{...}",
  "shadow_rules": "{...}",
  "shadow_matcher": "{...}",
  "shadow_rules_stat_prefix": "..."
}
```





rules:

{
  "action": "...",
  "policies": "{...}"
}

action：动作，可以是ALLOW,DENY,LOG

policies:策略配置，包括主体和权限，条件



policies:

{
  "permissions": [],
  "principals": [],
  "condition": "{...}"
}



permissions：权限配置

principals：主体配置

condition：条件配置



permissions:

{
  "and_rules": "{...}",
  "or_rules": "{...}",
  "any": "...",
  "header": "{...}",
  "url_path": "{...}",
  "destination_ip": "{...}",
  "destination_port": "...",
  "destination_port_range": "{...}",
  "metadata": "{...}",
  "not_rule": "{...}",
  "requested_server_name": "{...}"
}

and_rules：每个权限都必须满足

or_rules：至少一个权限满足

any：任意权限

header：请求头

url_path： url路径

destination_ip：目标ip

destination_port：目标端口

destination_port_range：目标端口范围

metadata：元数据

not_rule：必须不满足的权限

requested_server_name：请求服务主机名



principals：

{
  "and_ids": "{...}",
  "or_ids": "{...}",
  "any": "...",
  "authenticated": "{...}",
  "source_ip": "{...}",
  "direct_remote_ip": "{...}",
  "remote_ip": "{...}",
  "header": "{...}",
  "url_path": "{...}",
  "metadata": "{...}",
  "not_id": "{...}"
}

and_ids：与id，必须都满足主体

or_ids：或id，至少满足一个主体

any：任意主体

authenticated：认证过的主体

source_ip：来源ip

direct_remote_ip：直接远程地址

remote_ip：远程地址

header：请求头

url_path：请求路径

metadata：元数据

not_id：必须不能满足的主体



# 3用rbac filter实现权限控制

## 3.1allow nothing

authorizationPolicy实现方式

```
apiVersion: security.istio.io/v1beta1
kind: AuthorizationPolicy
metadata:
  name: allow-nothing
spec:
  {}
```

envoyfilter实现方式

**httprbac目录**

```
cat << EOF > ef-allow-nothing.yaml
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: allow-nothing
spec:
  configPatches:
  - applyTo: HTTP_FILTER
    match:
      context: SIDECAR_INBOUND
      listener:
        portNumber: 9080
        filterChain:
          destinationPort: 9080
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
              policies:
                ns[istio]-policy[allow-nothing]-rule[0]:
                  permissions:
                  - notRule:
                      any: true
                  principals:
                  - notId:
                      any: true
            shadowRulesStatPrefix: istio_dry_run_allow_
EOF

kubectl apply -f ef-allow-nothing.yaml -n istio 
```

清理：

```
kubectl delete -f ef-allow-nothing.yaml -n istio 
```



## 3.2名称空间拒绝所有

authorizationPolicy实现方式

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

envoyfilter实现

```
cat << EOF > ef-deny-all.yaml
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: match
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
          destinationPort: 9080
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
                ns[istio]-policy[deny-all]-rule[0]:
                  permissions:
                  - andRules:
                      rules:
                      - any: true
                  principals:
                  - andIds:
                      ids:
                      - any: true
            shadowRulesStatPrefix: istio_dry_run_allow_
EOF

kubectl apply -f ef-deny-all.yaml -n istio  --context context-cluster1
```

清理：

```
kubectl delete -f ef-deny-all.yaml -n istio  --context context-cluster1
```

## 3.3名称空间允许所有

authorizationPolicy实现方式

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

envoyfilter实现

```
cat << EOF > ef-allow-all.yaml
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: match
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
          destinationPort: 9080
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
              action: ALLOW
              policies:
                ns[istio]-policy[allow-all]-rule[0]:
                  permissions:
                  - andRules:
                      rules:
                      - any: true
                  principals:
                  - andIds:
                      ids:
                      - any: true
            shadowRulesStatPrefix: istio_dry_run_allow_
EOF

kubectl apply -f ef-allow-all.yaml -n istio  --context context-cluster1
```

## 3.4from-principals

authorizationPolicy实现方式

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

envoyfilter实现

```
cat << EOF > ef-from-principals.yaml
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: match
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
          destinationPort: 9080
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
              policies:
                ns[istio]-policy[productpage]-rule[0]:
                  permissions:
                  - andRules:
                      rules:
                      - any: true
                  principals:
                  - andIds:
                      ids:
                      - orIds:
                          ids:
                          - authenticated:
                              principalName:
                                exact: spiffe://cluster.local/ns/istio-system/sa/istio-ingressgateway-service-account
EOF

kubectl apply -f ef-from-principals.yaml -n istio  --context context-cluster1
```

## 3.5from-notPrincipals

authorizationPolicy实现方式

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

envoyfilter实现

```
cat << EOF > ef-from-notprincipals.yaml
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: match
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
          destinationPort: 9080
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
              policies:
                ns[istio]-policy[productpage]-rule[0]:
                  permissions:
                  - andRules:
                      rules:
                      - any: true
                  principals:
                  - andIds:
                      ids:
                      - notId:
                          orIds:
                            ids:
                            - authenticated:
                                principalName:
                                  exact: spiffe://cluster.local/ns/istio-system/sa/test
EOF

kubectl apply -f ef-from-notprincipals.yaml -n istio  --context context-cluster1
```

## 3.6from-requestPrincipals

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

authorizationPolicy实现方式

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

envoyfilter实现

```
cat << EOF > ef-from-requestPrincipals.yaml
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: match
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
          destinationPort: 9080
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
              policies:
                ns[istio]-policy[productpage]-rule[0]:
                  permissions:
                  - andRules:
                      rules:
                      - notRule:
                          orRules:
                            rules:
                            - urlPath:
                                path:
                                  exact: /healthz
                  principals:
                  - andIds:
                      ids:
                      - orIds:
                          ids:
                          - metadata:
                              filter: istio_authn
                              path:
                              - key: request.auth.principal
                              value:
                                stringMatch:
                                  safeRegex:
                                    googleRe2: {}
                                    regex: .+
            shadowRulesStatPrefix: istio_dry_run_allow_
EOF

kubectl apply -f ef-from-requestPrincipals.yaml -n istio  --context context-cluster1
```



```
TOKEN=eyJhbGciOiJSUzI1NiIsImtpZCI6IkRIRmJwb0lVcXJZOHQyenBBMnFYZkNtcjVWTzVaRXI0UnpIVV8tZW52dlEiLCJ0eXAiOiJKV1QifQ.eyJleHAiOjM1MzczOTExMDQsImdyb3VwcyI6WyJncm91cDEiLCJncm91cDIiXSwiaWF0IjoxNTM3MzkxMTA0LCJpc3MiOiJ0ZXN0aW5nQHNlY3VyZS5pc3Rpby5pbyIsInNjb3BlIjpbInNjb3BlMSIsInNjb3BlMiJdLCJzdWIiOiJ0ZXN0aW5nQHNlY3VyZS5pc3Rpby5pbyJ9.EdJnEZSH6X8hcyEii7c8H5lnhgjB5dwo07M5oheC8Xz8mOllyg--AHCFWHybM48reunF--oGaG6IXVngCEpVF0_P5DwsUoBgpPmK1JOaKN6_pe9sh0ZwTtdgK_RP01PuI7kUdbOTlkuUi2AO-qUyOm7Art2POzo36DLQlUXv8Ad7NBOqfQaKjE9ndaPWT7aexUsBHxmgiGbz1SyLH879f7uHYPbPKlpHU6P9S-DaKnGLaEchnoKnov7ajhrEhGXAQRukhDPKUHO9L30oPIr5IJllEQfHYtt6IZvlNUGeLUcif3wpry1R5tBXRicx2sXMQ7LyuDremDbcNy_iE76Upg

curl 192.168.229.128:30555/productpage -H "Authorization: Bearer ${TOKEN}"
```



## 3.7from-notRequestPrincipals

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

authorizationPolicy实现方式

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

envoyfilter实现

```
cat << EOF > ef-from-notrequestPrincipals.yaml
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: match
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
          destinationPort: 9080
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
              policies:
                ns[istio]-policy[productpage]-rule[0]:
                  permissions:
                  - andRules:
                      rules:
                      - orRules:
                          rules:
                          - urlPath:
                              path:
                                exact: /productpage
                  principals:
                  - andIds:
                      ids:
                      - notId:
                          orIds:
                            ids:
                            - metadata:
                                filter: istio_authn
                                path:
                                - key: request.auth.principal
                                value:
                                  stringMatch:
                                    exact: testing@secure.istio.io/testing@secure.istio.io
            shadowRulesStatPrefix: istio_dry_run_allow_
EOF

kubectl apply -f ef-from-notrequestPrincipals.yaml -n istio  --context context-cluster1
```



```
TOKEN=eyJhbGciOiJSUzI1NiIsImtpZCI6IkRIRmJwb0lVcXJZOHQyenBBMnFYZkNtcjVWTzVaRXI0UnpIVV8tZW52dlEiLCJ0eXAiOiJKV1QifQ.eyJleHAiOjM1MzczOTExMDQsImdyb3VwcyI6WyJncm91cDEiLCJncm91cDIiXSwiaWF0IjoxNTM3MzkxMTA0LCJpc3MiOiJ0ZXN0aW5nQHNlY3VyZS5pc3Rpby5pbyIsInNjb3BlIjpbInNjb3BlMSIsInNjb3BlMiJdLCJzdWIiOiJ0ZXN0aW5nQHNlY3VyZS5pc3Rpby5pbyJ9.EdJnEZSH6X8hcyEii7c8H5lnhgjB5dwo07M5oheC8Xz8mOllyg--AHCFWHybM48reunF--oGaG6IXVngCEpVF0_P5DwsUoBgpPmK1JOaKN6_pe9sh0ZwTtdgK_RP01PuI7kUdbOTlkuUi2AO-qUyOm7Art2POzo36DLQlUXv8Ad7NBOqfQaKjE9ndaPWT7aexUsBHxmgiGbz1SyLH879f7uHYPbPKlpHU6P9S-DaKnGLaEchnoKnov7ajhrEhGXAQRukhDPKUHO9L30oPIr5IJllEQfHYtt6IZvlNUGeLUcif3wpry1R5tBXRicx2sXMQ7LyuDremDbcNy_iE76Upg

curl 192.168.229.128:30555/productpage -H "Authorization: Bearer ${TOKEN}"
```



## 3.8from-namespaces

authorizationPolicy实现方式

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

envoyfilter实现

```
cat << EOF > ef-from-namespaces.yaml
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: match
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
          destinationPort: 9080
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
              policies:
                ns[istio]-policy[productpage]-rule[0]:
                  permissions:
                  - andRules:
                      rules:
                      - any: true
                  principals:
                  - andIds:
                      ids:
                      - orIds:
                          ids:
                          - authenticated:
                              principalName:
                                safeRegex:
                                  googleRe2: {}
                                  regex: .*/ns/istio-system/.*
            shadowRulesStatPrefix: istio_dry_run_allow_
EOF

kubectl apply -f ef-from-namespaces.yaml -n istio  --context context-cluster1
```

## 3.9from-notNamespaces

authorizationPolicy实现方式

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

envoyfilter实现

```
cat << EOF > ef-from-notNamespaces.yaml
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: match
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
          destinationPort: 9080
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
              policies:
                ns[istio]-policy[productpage]-rule[0]:
                  permissions:
                  - andRules:
                      rules:
                      - any: true
                  principals:
                  - andIds:
                      ids:
                      - notId:
                          orIds:
                            ids:
                            - authenticated:
                                principalName:
                                  safeRegex:
                                    googleRe2: {}
                                    regex: .*/ns/test/.*
            shadowRulesStatPrefix: istio_dry_run_allow_
EOF

kubectl apply -f ef-from-notNamespaces.yaml -n istio  --context context-cluster1
```

## 3.10from-ipBlocks

authorizationPolicy实现方式

```
apiVersion: security.istio.io/v1beta1
kind: AuthorizationPolicy
metadata:
 name: ingressgateway
spec:
 selector:
   matchLabels:
      app: productpage
 action: ALLOW
 rules:
 - from:
   - source:
       ipBlocks:
       - "172.20.0.0/16"
```

envoyfilter实现

```
cat << EOF > ef-from-ipBlocks.yaml
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: match
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
          destinationPort: 9080
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
              policies:
                ns[istio-system]-policy[ingressgateway]-rule[0]:
                  permissions:
                  - andRules:
                      rules:
                      - any: true
                  principals:
                  - andIds:
                      ids:
                      - orIds:
                          ids:
                          - directRemoteIp:
                              addressPrefix: 172.20.0.0
                              prefixLen: 16
EOF

kubectl apply -f ef-from-ipBlocks.yaml -n istio  --context context-cluster1
```

## 3.11from-notIpBlocks

authorizationPolicy实现方式

```
apiVersion: security.istio.io/v1beta1
kind: AuthorizationPolicy
metadata:
 name: ingressgateway
spec:
 selector:
   matchLabels:
      app: productpage
 action: ALLOW
 rules:
 - from:
   - source:
       notIpBlocks:
       - "172.20.0.0/16"
```

envoyfilter实现

```
cat << EOF > ef-from-notIpBlocks.yaml
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: match
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
          destinationPort: 9080
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
              policies:
                ns[istio-system]-policy[ingressgateway]-rule[0]:
                  permissions:
                  - andRules:
                      rules:
                      - any: true
                  principals:
                  - andIds:
                      ids:
                      - notId:
                          orIds:
                            ids:
                            - directRemoteIp:
                                addressPrefix: 172.20.0.0
                                prefixLen: 16
            shadowRulesStatPrefix: istio_dry_run_allow_
EOF

kubectl apply -f ef-from-notIpBlocks.yaml -n istio  --context context-cluster1
```

## 3.12from-remoteIpBlocks

authorizationPolicy实现方式

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

envoyfilter实现

```
cat << EOF > ef-from-remoteIpBlocks.yaml
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: match
spec:
  workloadSelector:
    labels:
      app: istio-ingressgateway
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
                ns[istio-system]-policy[ingressgateway]-rule[0]:
                  permissions:
                  - andRules:
                      rules:
                      - any: true
                  principals:
                  - andIds:
                      ids:
                      - orIds:
                          ids:
                          - remoteIp:
                              addressPrefix: 192.168.198.1
                              prefixLen: 32
            shadowRulesStatPrefix: istio_dry_run_allow_
EOF

kubectl apply -f ef-from-remoteIpBlocks.yaml -n istio-system  --context context-cluster1
```

## 3.13from-notRemoteIpBlocks

authorizationPolicy实现方式

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

envoyfilter实现

```
cat << EOF > ef-from-notRemoteIpBlocks.yaml
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: match
spec:
  workloadSelector:
    labels:
      app: istio-ingressgateway
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
              policies:
                ns[istio-system]-policy[ingressgateway]-rule[0]:
                  permissions:
                  - andRules:
                      rules:
                      - any: true
                  principals:
                  - andIds:
                      ids:
                      - notId:
                          orIds:
                            ids:
                            - remoteIp:
                                addressPrefix: 192.168.198.1
                                prefixLen: 32
EOF

kubectl apply -f ef-from-notRemoteIpBlocks.yaml -n istio-system  --context context-cluster1
```

## 3.14to-hosts

authorizationPolicy实现方式

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
       - "192.168.229.128:30555"
   from:
   - source:
       namespaces:
       - "istio-system"
```

envoyfilter实现

```
cat << EOF > ef-to-hosts.yaml
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: match
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
          destinationPort: 9080
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
              policies:
                ns[istio]-policy[productpage]-rule[0]:
                  permissions:
                  - andRules:
                      rules:
                      - orRules:
                          rules:
                          - header:
                              name: :authority
                              safeRegexMatch:
                                googleRe2: {}
                                regex: (?i)192\.168\.229\.128:30555
                  principals:
                  - andIds:
                      ids:
                      - orIds:
                          ids:
                          - authenticated:
                              principalName:
                                safeRegex:
                                  googleRe2: {}
                                  regex: .*/ns/istio-system/.*
            shadowRulesStatPrefix: istio_dry_run_allow_
EOF

kubectl apply -f ef-to-hosts.yaml -n istio  --context context-cluster1
```

## 3.15to-notHosts

authorizationPolicy实现方式

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



envoyfilter实现

```
cat << EOF > ef-to-notHosts.yaml
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: match
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
          destinationPort: 9080
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
              policies:
                ns[istio]-policy[productpage]-rule[0]:
                  permissions:
                  - andRules:
                      rules:
                      - notRule:
                          orRules:
                            rules:
                            - header:
                                name: :authority
                                safeRegexMatch:
                                  googleRe2: {}
                                  regex: (?i)test
                  principals:
                  - andIds:
                      ids:
                      - orIds:
                          ids:
                          - authenticated:
                              principalName:
                                safeRegex:
                                  googleRe2: {}
                                  regex: .*/ns/istio-system/.*
            shadowRulesStatPrefix: istio_dry_run_allow_
EOF

kubectl apply -f ef-to-notHosts.yaml -n istio  --context context-cluster1
```

## 3.16to-ports

authorizationPolicy实现方式

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

envoyfilter实现

```
cat << EOF > ef-to-ports.yaml
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: match
spec:
  workloadSelector:
    labels:
      app: details
      version: v1
  configPatches:
  - applyTo: HTTP_FILTER
    match:
      context: SIDECAR_INBOUND
      listener:
        portNumber: 9080
        filterChain:
          destinationPort: 9080
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
              policies:
                ns[istio]-policy[details]-rule[0]:
                  permissions:
                  - andRules:
                      rules:
                      - orRules:
                          rules:
                          - destinationPort: 9080
                  principals:
                  - andIds:
                      ids:
                      - any: true
            shadowRulesStatPrefix: istio_dry_run_allow_
EOF

kubectl apply -f ef-to-ports.yaml -n istio  --context context-cluster1
```

## 3.17to-notPorts

authorizationPolicy实现方式

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

envoyfilter实现

```
cat << EOF > ef-to-notPorts.yaml
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: match
spec:
  workloadSelector:
    labels:
      app: details
      version: v1
  configPatches:
  - applyTo: HTTP_FILTER
    match:
      context: SIDECAR_INBOUND
      listener:
        portNumber: 9080
        filterChain:
          destinationPort: 9080
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
              policies:
                ns[istio]-policy[details]-rule[0]:
                  permissions:
                  - andRules:
                      rules:
                      - notRule:
                          orRules:
                            rules:
                            - destinationPort: 9080
                  principals:
                  - andIds:
                      ids:
                      - any: true
            shadowRulesStatPrefix: istio_dry_run_allow_
EOF

kubectl apply -f ef-to-notPorts.yaml -n istio  --context context-cluster1
```

## 3.18to-methods

authorizationPolicy实现方式

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

envoyfilter实现

```
cat << EOF > ef-to-methods.yaml
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: match
spec:
  workloadSelector:
    labels:
      app: details
      version: v1
  configPatches:
  - applyTo: HTTP_FILTER
    match:
      context: SIDECAR_INBOUND
      listener:
        portNumber: 9080
        filterChain:
          destinationPort: 9080
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
              policies:
                ns[istio]-policy[details]-rule[0]:
                  permissions:
                  - andRules:
                      rules:
                      - orRules:
                          rules:
                          - header:
                              exactMatch: GET
                              name: ":method"
                  principals:
                  - andIds:
                      ids:
                      - any: true
            shadowRulesStatPrefix: istio_dry_run_allow_
EOF

kubectl apply -f ef-to-methods.yaml -n istio  --context context-cluster1
```

## 3.19to-notMethods

authorizationPolicy实现方式

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

envoyfilter实现

```
cat << EOF > ef-to-notMethods.yaml
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: match
spec:
  workloadSelector:
    labels:
      app: details
      version: v1
  configPatches:
  - applyTo: HTTP_FILTER
    match:
      context: SIDECAR_INBOUND
      listener:
        portNumber: 9080
        filterChain:
          destinationPort: 9080
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
              policies:
                ns[istio]-policy[details]-rule[0]:
                  permissions:
                  - andRules:
                      rules:
                      - notRule:
                          orRules:
                            rules:
                            - header:
                                exactMatch: GET
                                name: ":method"
                  principals:
                  - andIds:
                      ids:
                      - any: true
            shadowRulesStatPrefix: istio_dry_run_allow_
EOF

kubectl apply -f ef-to-notMethods.yaml -n istio  --context context-cluster1
```

## 3.20to-paths

authorizationPolicy实现方式

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

envoyfilter实现

```
cat << EOF > ef-to-paths.yaml
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: match
spec:
  workloadSelector:
    labels:
      app: details
      version: v1
  configPatches:
  - applyTo: HTTP_FILTER
    match:
      context: SIDECAR_INBOUND
      listener:
        portNumber: 9080
        filterChain:
          destinationPort: 9080
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
              policies:
                ns[istio]-policy[details]-rule[0]:
                  permissions:
                  - andRules:
                      rules:
                      - orRules:
                          rules:
                          - urlPath:
                              path:
                                exact: /details/0
                  principals:
                  - andIds:
                      ids:
                      - any: true
            shadowRulesStatPrefix: istio_dry_run_allow_
EOF

kubectl apply -f ef-to-paths.yaml -n istio  --context context-cluster1
```

## 3.21to-notPaths

authorizationPolicy实现方式

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

envoyfilter实现

```
cat << EOF > ef-to-notPaths.yaml
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: match
spec:
  workloadSelector:
    labels:
      app: details
      version: v1
  configPatches:
  - applyTo: HTTP_FILTER
    match:
      context: SIDECAR_INBOUND
      listener:
        portNumber: 9080
        filterChain:
          destinationPort: 9080
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
              policies:
                ns[istio]-policy[details]-rule[0]:
                  permissions:
                  - andRules:
                      rules:
                      - notRule:
                          orRules:
                            rules:
                            - urlPath:
                                path:
                                  exact: /details/0
                  principals:
                  - andIds:
                      ids:
                      - any: true
            shadowRulesStatPrefix: istio_dry_run_allow_
EOF

kubectl apply -f ef-to-notPaths.yaml -n istio  --context context-cluster1
```

## 3.22when-request.headers-values

authorizationPolicy实现方式

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

envoyfilter实现

```
cat << EOF > ef-when-request.headers.yaml
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: match
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
          destinationPort: 9080
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
              policies:
                ns[istio]-policy[productpage]-rule[0]:
                  permissions:
                  - andRules:
                      rules:
                      - any: true
                  principals:
                  - andIds:
                      ids:
                      - orIds:
                          ids:
                          - header:
                              exactMatch: test
                              name: test
            shadowRulesStatPrefix: istio_dry_run_allow_
EOF

kubectl apply -f ef-when-request.headers.yaml -n istio  --context context-cluster1
```

## 3.23when-request.headers-notvalues

authorizationPolicy实现方式

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

envoyfilter实现

```
cat << EOF > ef-when-request.headers-notvalues.yaml
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: match
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
          destinationPort: 9080
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
              policies:
                ns[istio]-policy[productpage]-rule[0]:
                  permissions:
                  - andRules:
                      rules:
                      - any: true
                  principals:
                  - andIds:
                      ids:
                      - notId:
                          orIds:
                            ids:
                            - header:
                                exactMatch: test
                                name: test
            shadowRulesStatPrefix: istio_dry_run_allow_
EOF

kubectl apply -f ef-when-request.headers-notvalues.yaml -n istio  --context context-cluster1
```

## 3.24when-source.ip-values

authorizationPolicy实现方式

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

envoyfilter实现

```
cat << EOF > ef-when-source.ip-values.yaml
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: match
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
          destinationPort: 9080
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
              policies:
                ns[istio]-policy[productpage]-rule[0]:
                  permissions:
                  - andRules:
                      rules:
                      - any: true
                  principals:
                  - andIds:
                      ids:
                      - orIds:
                          ids:
                          - directRemoteIp:
                              addressPrefix: 172.20.0.0
                              prefixLen: 16
            shadowRulesStatPrefix: istio_dry_run_allow_
EOF

kubectl apply -f ef-when-source.ip-values.yaml -n istio  --context context-cluster1
```

## 3.25when-source.ip-notvalues

authorizationPolicy实现方式

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

envoyfilter实现

```
cat << EOF > ef-when-source.ip-notvalues.yaml
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: match
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
          destinationPort: 9080
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
              policies:
                ns[istio]-policy[productpage]-rule[0]:
                  permissions:
                  - andRules:
                      rules:
                      - any: true
                  principals:
                  - andIds:
                      ids:
                      - notId:
                          orIds:
                            ids:
                            - directRemoteIp:
                                addressPrefix: 172.20.0.0
                                prefixLen: 16
            shadowRulesStatPrefix: istio_dry_run_allow_
EOF

kubectl apply -f ef-when-source.ip-notvalues.yaml -n istio  --context context-cluster1
```

## 3.26when-remote.ip-values

authorizationPolicy实现方式

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

envoyfilter实现

```
cat << EOF > ef-when-remote.ip-values.yaml
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: match
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
          destinationPort: 9080
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
                ns[istio]-policy[productpage]-rule[0]:
                  permissions:
                  - andRules:
                      rules:
                      - any: true
                  principals:
                  - andIds:
                      ids:
                      - orIds:
                          ids:
                          - remoteIp:
                              addressPrefix: 192.168.198.1
                              prefixLen: 32
            shadowRulesStatPrefix: istio_dry_run_allow_
EOF

kubectl apply -f ef-when-remote.ip-values.yaml -n istio  --context context-cluster1
```

## 3.27when-remote.ip-notvalues

authorizationPolicy实现方式

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

envoyfilter实现

```
cat << EOF > ef-when-remote.ip-values.yaml
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: match
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
          destinationPort: 9080
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
              policies:
                ns[istio]-policy[productpage]-rule[0]:
                  permissions:
                  - andRules:
                      rules:
                      - any: true
                  principals:
                  - andIds:
                      ids:
                      - notId:
                          orIds:
                            ids:
                            - remoteIp:
                                addressPrefix: 192.168.198.1
                                prefixLen: 32
            shadowRulesStatPrefix: istio_dry_run_allow_
EOF

kubectl apply -f ef-when-remote.ip-values.yaml -n istio  --context context-cluster1
```

## 3.28when-source.namespace-values

authorizationPolicy实现方式

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

envoyfilter实现

```
cat << EOF > when-source.namespace-values.yaml
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: match
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
          destinationPort: 9080
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
              policies:
                ns[istio]-policy[productpage]-rule[0]:
                  permissions:
                  - andRules:
                      rules:
                      - any: true
                  principals:
                  - andIds:
                      ids:
                      - orIds:
                          ids:
                          - authenticated:
                              principalName:
                                safeRegex:
                                  googleRe2: {}
                                  regex: .*/ns/istio-system/.*
            shadowRulesStatPrefix: istio_dry_run_allow_
EOF

kubectl apply -f when-source.namespace-values.yaml -n istio  --context context-cluster1
```

3.29when-source.namespace-notvalues

authorizationPolicy实现方式

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

envoyfilter实现

```
cat << EOF > ef-when-source.namespace-notvalues.yaml
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: match
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
          destinationPort: 9080
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
              policies:
                ns[istio]-policy[productpage]-rule[0]:
                  permissions:
                  - andRules:
                      rules:
                      - any: true
                  principals:
                  - andIds:
                      ids:
                      - notId:
                          orIds:
                            ids:
                            - authenticated:
                                principalName:
                                  safeRegex:
                                    googleRe2: {}
                                    regex: .*/ns/istio-system/.*
            shadowRulesStatPrefix: istio_dry_run_allow_
EOF

kubectl apply -f ef-when-source.namespace-notvalues.yaml -n istio  --context context-cluster1
```

## 3.29when-source.principal-values

authorizationPolicy实现方式

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

envoyfilter实现

```
cat << EOF > ef-when-source.principal-values.yaml
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: match
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
          destinationPort: 9080
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
              policies:
                ns[istio]-policy[productpage]-rule[0]:
                  permissions:
                  - andRules:
                      rules:
                      - any: true
                  principals:
                  - andIds:
                      ids:
                      - orIds:
                          ids:
                          - authenticated:
                              principalName:
                                exact: spiffe://cluster.local/ns/istio-system/sa/istio-ingressgateway-service-account
            shadowRulesStatPrefix: istio_dry_run_allow_
EOF

kubectl apply -f ef-when-source.principal-values.yaml -n istio  --context context-cluster1
```

## 3.30when-source.principal-notvalues

authorizationPolicy实现方式

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

envoyfilter实现

```
cat << EOF > ef-when-source.principal-notvalues.yaml
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: match
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
          destinationPort: 9080
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
              policies:
                ns[istio]-policy[productpage]-rule[0]:
                  permissions:
                  - andRules:
                      rules:
                      - any: true
                  principals:
                  - andIds:
                      ids:
                      - notId:
                          orIds:
                            ids:
                            - authenticated:
                                principalName:
                                  exact: spiffe://cluster.local/ns/istio-system/sa/istio-ingressgateway-service-account
            shadowRulesStatPrefix: istio_dry_run_allow_
EOF

kubectl apply -f ef-when-source.principal-notvalues.yaml -n istio  --context context-cluster1
```

## 3.31when-request.auth.principal-values

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

authorizationPolicy实现方式

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

envoyfilter实现

```
cat << EOF > ef-when-request.auth.principal-values.yaml
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: match
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
          destinationPort: 9080
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
              policies:
                ns[istio]-policy[productpage]-rule[0]:
                  permissions:
                  - andRules:
                      rules:
                      - any: true
                  principals:
                  - andIds:
                      ids:
                      - orIds:
                          ids:
                          - metadata:
                              filter: istio_authn
                              path:
                              - key: request.auth.principal
                              value:
                                stringMatch:
                                  exact: testing@secure.istio.io/testing@secure.istio.io
            shadowRulesStatPrefix: istio_dry_run_allow_
EOF

kubectl apply -f ef-when-request.auth.principal-values.yaml -n istio  --context context-cluster1
```



```
TOKEN=eyJhbGciOiJSUzI1NiIsImtpZCI6IkRIRmJwb0lVcXJZOHQyenBBMnFYZkNtcjVWTzVaRXI0UnpIVV8tZW52dlEiLCJ0eXAiOiJKV1QifQ.eyJleHAiOjM1MzczOTExMDQsImdyb3VwcyI6WyJncm91cDEiLCJncm91cDIiXSwiaWF0IjoxNTM3MzkxMTA0LCJpc3MiOiJ0ZXN0aW5nQHNlY3VyZS5pc3Rpby5pbyIsInNjb3BlIjpbInNjb3BlMSIsInNjb3BlMiJdLCJzdWIiOiJ0ZXN0aW5nQHNlY3VyZS5pc3Rpby5pbyJ9.EdJnEZSH6X8hcyEii7c8H5lnhgjB5dwo07M5oheC8Xz8mOllyg--AHCFWHybM48reunF--oGaG6IXVngCEpVF0_P5DwsUoBgpPmK1JOaKN6_pe9sh0ZwTtdgK_RP01PuI7kUdbOTlkuUi2AO-qUyOm7Art2POzo36DLQlUXv8Ad7NBOqfQaKjE9ndaPWT7aexUsBHxmgiGbz1SyLH879f7uHYPbPKlpHU6P9S-DaKnGLaEchnoKnov7ajhrEhGXAQRukhDPKUHO9L30oPIr5IJllEQfHYtt6IZvlNUGeLUcif3wpry1R5tBXRicx2sXMQ7LyuDremDbcNy_iE76Upg

curl 192.168.229.128:30555/productpage -H "Authorization: Bearer ${TOKEN}"
```



## 3.32when-request.auth.principal-notvalues

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

authorizationPolicy实现方式

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

envoyfilter实现

```
cat << EOF > ef-when-request.auth.principal-notvalues.yaml
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: match
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
          destinationPort: 9080
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
              policies:
                ns[istio]-policy[productpage]-rule[0]:
                  permissions:
                  - andRules:
                      rules:
                      - any: true
                  principals:
                  - andIds:
                      ids:
                      - notId:
                          orIds:
                            ids:
                            - metadata:
                                filter: istio_authn
                                path:
                                - key: request.auth.principal
                                value:
                                  stringMatch:
                                    exact: testing@secure.istio.io/testing@secure.istio.io
            shadowRulesStatPrefix: istio_dry_run_allow_
EOF

kubectl apply -f ef-when-request.auth.principal-notvalues.yaml -n istio  --context context-cluster1
```

```
TOKEN=eyJhbGciOiJSUzI1NiIsImtpZCI6IkRIRmJwb0lVcXJZOHQyenBBMnFYZkNtcjVWTzVaRXI0UnpIVV8tZW52dlEiLCJ0eXAiOiJKV1QifQ.eyJleHAiOjM1MzczOTExMDQsImdyb3VwcyI6WyJncm91cDEiLCJncm91cDIiXSwiaWF0IjoxNTM3MzkxMTA0LCJpc3MiOiJ0ZXN0aW5nQHNlY3VyZS5pc3Rpby5pbyIsInNjb3BlIjpbInNjb3BlMSIsInNjb3BlMiJdLCJzdWIiOiJ0ZXN0aW5nQHNlY3VyZS5pc3Rpby5pbyJ9.EdJnEZSH6X8hcyEii7c8H5lnhgjB5dwo07M5oheC8Xz8mOllyg--AHCFWHybM48reunF--oGaG6IXVngCEpVF0_P5DwsUoBgpPmK1JOaKN6_pe9sh0ZwTtdgK_RP01PuI7kUdbOTlkuUi2AO-qUyOm7Art2POzo36DLQlUXv8Ad7NBOqfQaKjE9ndaPWT7aexUsBHxmgiGbz1SyLH879f7uHYPbPKlpHU6P9S-DaKnGLaEchnoKnov7ajhrEhGXAQRukhDPKUHO9L30oPIr5IJllEQfHYtt6IZvlNUGeLUcif3wpry1R5tBXRicx2sXMQ7LyuDremDbcNy_iE76Upg

curl 192.168.229.128:30555/productpage -H "Authorization: Bearer ${TOKEN}"
```



## 3.32when-request.auth.audiences-values

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

authorizationPolicy实现方式

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

envoyfilter实现

```
cat << EOF > ef-when-request.auth.audiences-values.yaml
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: match
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
          destinationPort: 9080
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
              policies:
                ns[istio]-policy[productpage]-rule[0]:
                  permissions:
                  - andRules:
                      rules:
                      - any: true
                  principals:
                  - andIds:
                      ids:
                      - orIds:
                          ids:
                          - metadata:
                              filter: istio_authn
                              path:
                              - key: request.auth.audiences
                              value:
                                stringMatch:
                                  exact: app
                          - metadata:
                              filter: istio_authn
                              path:
                              - key: request.auth.audiences
                              value:
                                stringMatch:
                                  exact: “web”
            shadowRulesStatPrefix: istio_dry_run_allow_
EOF

kubectl apply -f  ef-when-request.auth.audiences-values.yaml -n istio  --context context-cluster1
```

```
TOKEN=eyJhbGciOiJSUzI1NiIsImtpZCI6IkRIRmJwb0lVcXJZOHQyenBBMnFYZkNtcjVWTzVaRXI0UnpIVV8tZW52dlEiLCJ0eXAiOiJKV1QifQ.eyJleHAiOjM1MzczOTExMDQsImdyb3VwcyI6WyJncm91cDEiLCJncm91cDIiXSwiaWF0IjoxNTM3MzkxMTA0LCJpc3MiOiJ0ZXN0aW5nQHNlY3VyZS5pc3Rpby5pbyIsInNjb3BlIjpbInNjb3BlMSIsInNjb3BlMiJdLCJzdWIiOiJ0ZXN0aW5nQHNlY3VyZS5pc3Rpby5pbyJ9.EdJnEZSH6X8hcyEii7c8H5lnhgjB5dwo07M5oheC8Xz8mOllyg--AHCFWHybM48reunF--oGaG6IXVngCEpVF0_P5DwsUoBgpPmK1JOaKN6_pe9sh0ZwTtdgK_RP01PuI7kUdbOTlkuUi2AO-qUyOm7Art2POzo36DLQlUXv8Ad7NBOqfQaKjE9ndaPWT7aexUsBHxmgiGbz1SyLH879f7uHYPbPKlpHU6P9S-DaKnGLaEchnoKnov7ajhrEhGXAQRukhDPKUHO9L30oPIr5IJllEQfHYtt6IZvlNUGeLUcif3wpry1R5tBXRicx2sXMQ7LyuDremDbcNy_iE76Upg

curl 192.168.229.128:30555/productpage -H "Authorization: Bearer ${TOKEN}"
```



## 3.33when-request.auth.audiences-notvalues

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

authorizationPolicy实现方式

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

envoyfilter实现

```
cat << EOF > ef-when-request.auth.audiences-notvalues.yaml
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: match
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
          destinationPort: 9080
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
              policies:
                ns[istio]-policy[productpage]-rule[0]:
                  permissions:
                  - andRules:
                      rules:
                      - any: true
                  principals:
                  - andIds:
                      ids:
                      - notId:
                          orIds:
                            ids:
                            - metadata:
                                filter: istio_authn
                                path:
                                - key: request.auth.audiences
                                value:
                                  stringMatch:
                                    exact: app
                            - metadata:
                                filter: istio_authn
                                path:
                                - key: request.auth.audiences
                                value:
                                  stringMatch:
                                    exact: “web”
            shadowRulesStatPrefix: istio_dry_run_allow_
EOF

kubectl apply -f  ef-when-request.auth.audiences-notvalues.yaml -n istio  --context context-cluster1
```

```
TOKEN=eyJhbGciOiJSUzI1NiIsImtpZCI6IkRIRmJwb0lVcXJZOHQyenBBMnFYZkNtcjVWTzVaRXI0UnpIVV8tZW52dlEiLCJ0eXAiOiJKV1QifQ.eyJleHAiOjM1MzczOTExMDQsImdyb3VwcyI6WyJncm91cDEiLCJncm91cDIiXSwiaWF0IjoxNTM3MzkxMTA0LCJpc3MiOiJ0ZXN0aW5nQHNlY3VyZS5pc3Rpby5pbyIsInNjb3BlIjpbInNjb3BlMSIsInNjb3BlMiJdLCJzdWIiOiJ0ZXN0aW5nQHNlY3VyZS5pc3Rpby5pbyJ9.EdJnEZSH6X8hcyEii7c8H5lnhgjB5dwo07M5oheC8Xz8mOllyg--AHCFWHybM48reunF--oGaG6IXVngCEpVF0_P5DwsUoBgpPmK1JOaKN6_pe9sh0ZwTtdgK_RP01PuI7kUdbOTlkuUi2AO-qUyOm7Art2POzo36DLQlUXv8Ad7NBOqfQaKjE9ndaPWT7aexUsBHxmgiGbz1SyLH879f7uHYPbPKlpHU6P9S-DaKnGLaEchnoKnov7ajhrEhGXAQRukhDPKUHO9L30oPIr5IJllEQfHYtt6IZvlNUGeLUcif3wpry1R5tBXRicx2sXMQ7LyuDremDbcNy_iE76Upg

curl 192.168.229.128:30555/productpage -H "Authorization: Bearer ${TOKEN}"
```



## 3.34when-request.auth.presenter-values

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

authorizationPolicy实现方式

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

envoyfilter实现

```
cat << EOF > ef-when-request.auth.presenter-values.yaml
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: match
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
          destinationPort: 9080
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
              policies:
                ns[istio]-policy[productpage]-rule[0]:
                  permissions:
                  - andRules:
                      rules:
                      - any: true
                  principals:
                  - andIds:
                      ids:
                      - orIds:
                          ids:
                          - metadata:
                              filter: istio_authn
                              path:
                              - key: request.auth.presenter
                              value:
                                stringMatch:
                                  exact: app
            shadowRulesStatPrefix: istio_dry_run_allow_
EOF

kubectl apply -f  ef-when-request.auth.presenter-values.yaml -n istio  --context context-cluster1
```

```
TOKEN=eyJhbGciOiJSUzI1NiIsImtpZCI6IkRIRmJwb0lVcXJZOHQyenBBMnFYZkNtcjVWTzVaRXI0UnpIVV8tZW52dlEiLCJ0eXAiOiJKV1QifQ.eyJleHAiOjM1MzczOTExMDQsImdyb3VwcyI6WyJncm91cDEiLCJncm91cDIiXSwiaWF0IjoxNTM3MzkxMTA0LCJpc3MiOiJ0ZXN0aW5nQHNlY3VyZS5pc3Rpby5pbyIsInNjb3BlIjpbInNjb3BlMSIsInNjb3BlMiJdLCJzdWIiOiJ0ZXN0aW5nQHNlY3VyZS5pc3Rpby5pbyJ9.EdJnEZSH6X8hcyEii7c8H5lnhgjB5dwo07M5oheC8Xz8mOllyg--AHCFWHybM48reunF--oGaG6IXVngCEpVF0_P5DwsUoBgpPmK1JOaKN6_pe9sh0ZwTtdgK_RP01PuI7kUdbOTlkuUi2AO-qUyOm7Art2POzo36DLQlUXv8Ad7NBOqfQaKjE9ndaPWT7aexUsBHxmgiGbz1SyLH879f7uHYPbPKlpHU6P9S-DaKnGLaEchnoKnov7ajhrEhGXAQRukhDPKUHO9L30oPIr5IJllEQfHYtt6IZvlNUGeLUcif3wpry1R5tBXRicx2sXMQ7LyuDremDbcNy_iE76Upg

curl 192.168.229.128:30555/productpage -H "Authorization: Bearer ${TOKEN}"
```



## 3.35when-request.auth.presenter-notvalues

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

authorizationPolicy实现方式

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

envoyfilter实现

```
cat << EOF > ef-when-request.auth.presenter-notvalues.yaml
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: match
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
          destinationPort: 9080
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
              policies:
                ns[istio]-policy[productpage]-rule[0]:
                  permissions:
                  - andRules:
                      rules:
                      - any: true
                  principals:
                  - andIds:
                      ids:
                      - notId:
                          orIds:
                            ids:
                            - metadata:
                                filter: istio_authn
                                path:
                                - key: request.auth.presenter
                                value:
                                  stringMatch:
                                    exact: app
            shadowRulesStatPrefix: istio_dry_run_allow_
EOF

kubectl apply -f  ef-when-request.auth.presenter-notvalues.yaml -n istio  --context context-cluster1
```

```
TOKEN=eyJhbGciOiJSUzI1NiIsImtpZCI6IkRIRmJwb0lVcXJZOHQyenBBMnFYZkNtcjVWTzVaRXI0UnpIVV8tZW52dlEiLCJ0eXAiOiJKV1QifQ.eyJleHAiOjM1MzczOTExMDQsImdyb3VwcyI6WyJncm91cDEiLCJncm91cDIiXSwiaWF0IjoxNTM3MzkxMTA0LCJpc3MiOiJ0ZXN0aW5nQHNlY3VyZS5pc3Rpby5pbyIsInNjb3BlIjpbInNjb3BlMSIsInNjb3BlMiJdLCJzdWIiOiJ0ZXN0aW5nQHNlY3VyZS5pc3Rpby5pbyJ9.EdJnEZSH6X8hcyEii7c8H5lnhgjB5dwo07M5oheC8Xz8mOllyg--AHCFWHybM48reunF--oGaG6IXVngCEpVF0_P5DwsUoBgpPmK1JOaKN6_pe9sh0ZwTtdgK_RP01PuI7kUdbOTlkuUi2AO-qUyOm7Art2POzo36DLQlUXv8Ad7NBOqfQaKjE9ndaPWT7aexUsBHxmgiGbz1SyLH879f7uHYPbPKlpHU6P9S-DaKnGLaEchnoKnov7ajhrEhGXAQRukhDPKUHO9L30oPIr5IJllEQfHYtt6IZvlNUGeLUcif3wpry1R5tBXRicx2sXMQ7LyuDremDbcNy_iE76Upg

curl 192.168.229.128:30555/productpage -H "Authorization: Bearer ${TOKEN}"
```



## 3.36when-request.auth.claims-values

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

authorizationPolicy实现方式

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

envoyfilter实现

```
cat << EOF > ef-when-request.auth.claims-values.yaml
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: match
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
          destinationPort: 9080
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
              policies:
                ns[istio]-policy[productpage]-rule[0]:
                  permissions:
                  - andRules:
                      rules:
                      - any: true
                  principals:
                  - andIds:
                      ids:
                      - orIds:
                          ids:
                          - metadata:
                              filter: istio_authn
                              path:
                              - key: request.auth.claims
                              - key: groups
                              value:
                                listMatch:
                                  oneOf:
                                    stringMatch:
                                      exact: group1
            shadowRulesStatPrefix: istio_dry_run_allow_
EOF

kubectl apply -f  ef-when-request.auth.claims-values.yaml -n istio  --context context-cluster1
```

```
TOKEN=eyJhbGciOiJSUzI1NiIsImtpZCI6IkRIRmJwb0lVcXJZOHQyenBBMnFYZkNtcjVWTzVaRXI0UnpIVV8tZW52dlEiLCJ0eXAiOiJKV1QifQ.eyJleHAiOjM1MzczOTExMDQsImdyb3VwcyI6WyJncm91cDEiLCJncm91cDIiXSwiaWF0IjoxNTM3MzkxMTA0LCJpc3MiOiJ0ZXN0aW5nQHNlY3VyZS5pc3Rpby5pbyIsInNjb3BlIjpbInNjb3BlMSIsInNjb3BlMiJdLCJzdWIiOiJ0ZXN0aW5nQHNlY3VyZS5pc3Rpby5pbyJ9.EdJnEZSH6X8hcyEii7c8H5lnhgjB5dwo07M5oheC8Xz8mOllyg--AHCFWHybM48reunF--oGaG6IXVngCEpVF0_P5DwsUoBgpPmK1JOaKN6_pe9sh0ZwTtdgK_RP01PuI7kUdbOTlkuUi2AO-qUyOm7Art2POzo36DLQlUXv8Ad7NBOqfQaKjE9ndaPWT7aexUsBHxmgiGbz1SyLH879f7uHYPbPKlpHU6P9S-DaKnGLaEchnoKnov7ajhrEhGXAQRukhDPKUHO9L30oPIr5IJllEQfHYtt6IZvlNUGeLUcif3wpry1R5tBXRicx2sXMQ7LyuDremDbcNy_iE76Upg

curl 192.168.229.128:30555/productpage -H "Authorization: Bearer ${TOKEN}"
```



## 3.37when-request.auth.claims-notvalues

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

authorizationPolicy实现方式

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

envoyfilter实现

```
cat << EOF > ef-when-request.auth.claims-notvalues.yaml
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: match
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
          destinationPort: 9080
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
              policies:
                ns[istio]-policy[productpage]-rule[0]:
                  permissions:
                  - andRules:
                      rules:
                      - any: true
                  principals:
                  - andIds:
                      ids:
                      - notId:
                          orIds:
                            ids:
                            - metadata:
                                filter: istio_authn
                                path:
                                - key: request.auth.claims
                                - key: groups
                                value:
                                  listMatch:
                                    oneOf:
                                      stringMatch:
                                        exact: group1
            shadowRulesStatPrefix: istio_dry_run_allow_
EOF

kubectl apply -f  ef-when-request.auth.claims-notvalues.yaml -n istio  --context context-cluster1
```

```
TOKEN=eyJhbGciOiJSUzI1NiIsImtpZCI6IkRIRmJwb0lVcXJZOHQyenBBMnFYZkNtcjVWTzVaRXI0UnpIVV8tZW52dlEiLCJ0eXAiOiJKV1QifQ.eyJleHAiOjM1MzczOTExMDQsImdyb3VwcyI6WyJncm91cDEiLCJncm91cDIiXSwiaWF0IjoxNTM3MzkxMTA0LCJpc3MiOiJ0ZXN0aW5nQHNlY3VyZS5pc3Rpby5pbyIsInNjb3BlIjpbInNjb3BlMSIsInNjb3BlMiJdLCJzdWIiOiJ0ZXN0aW5nQHNlY3VyZS5pc3Rpby5pbyJ9.EdJnEZSH6X8hcyEii7c8H5lnhgjB5dwo07M5oheC8Xz8mOllyg--AHCFWHybM48reunF--oGaG6IXVngCEpVF0_P5DwsUoBgpPmK1JOaKN6_pe9sh0ZwTtdgK_RP01PuI7kUdbOTlkuUi2AO-qUyOm7Art2POzo36DLQlUXv8Ad7NBOqfQaKjE9ndaPWT7aexUsBHxmgiGbz1SyLH879f7uHYPbPKlpHU6P9S-DaKnGLaEchnoKnov7ajhrEhGXAQRukhDPKUHO9L30oPIr5IJllEQfHYtt6IZvlNUGeLUcif3wpry1R5tBXRicx2sXMQ7LyuDremDbcNy_iE76Upg

curl 192.168.229.128:30555/productpage -H "Authorization: Bearer ${TOKEN}"
```



## 3.38when-destination.ip-values

authorizationPolicy实现方式

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

envoyfilter实现

```
cat << EOF > ef-when-destination.ip-values.yaml
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: match
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
          destinationPort: 9080
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
              policies:
                ns[istio]-policy[productpage]-rule[0]:
                  permissions:
                  - andRules:
                      rules:
                      - orRules:
                          rules:
                          - destinationIp:
                              addressPrefix: 172.20.0.0
                              prefixLen: 16
                  principals:
                  - andIds:
                      ids:
                      - any: true
            shadowRulesStatPrefix: istio_dry_run_allow_
EOF

kubectl apply -f  ef-when-destination.ip-values.yaml -n istio  --context context-cluster1
```

## 3.39when-destination.ip-notvalues

authorizationPolicy实现方式

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

envoyfilter实现

```
cat << EOF > ef-when-destination.ip-notvalues.yaml
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: match
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
          destinationPort: 9080
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
              policies:
                ns[istio]-policy[productpage]-rule[0]:
                  permissions:
                  - andRules:
                      rules:
                      - notRule:
                          orRules:
                            rules:
                            - destinationIp:
                                addressPrefix: 172.20.0.0
                                prefixLen: 16
                  principals:
                  - andIds:
                      ids:
                      - any: true
            shadowRulesStatPrefix: istio_dry_run_allow_
EOF

kubectl apply -f  ef-when-destination.ip-notvalues.yaml -n istio  --context context-cluster1
```

## 3.40when-destination.port-values

authorizationPolicy实现方式

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

envoyfilter实现

```
cat << EOF > ef-when-destination.port-values.yaml
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: match
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
          destinationPort: 9080
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
              policies:
                ns[istio]-policy[productpage]-rule[0]:
                  permissions:
                  - andRules:
                      rules:
                      - orRules:
                          rules:
                          - destinationPort: 9080
                  principals:
                  - andIds:
                      ids:
                      - any: true
            shadowRulesStatPrefix: istio_dry_run_allow_
EOF

kubectl apply -f  ef-when-destination.port-values.yaml -n istio  --context context-cluster1
```

## 3.41when-destination.port-notvalues

authorizationPolicy实现方式

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

envoyfilter实现

```
cat << EOF > ef-when-destination.port-notvalues.yaml
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: match
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
          destinationPort: 9080
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
              policies:
                ns[istio]-policy[productpage]-rule[0]:
                  permissions:
                  - andRules:
                      rules:
                      - notRule:
                          orRules:
                            rules:
                            - destinationPort: 9080
                  principals:
                  - andIds:
                      ids:
                      - any: true
            shadowRulesStatPrefix: istio_dry_run_allow_
EOF

kubectl apply -f  ef-when-destination.port-notvalues.yaml -n istio  --context context-cluster1
```

## 3.42when-connection.sni-value

authorizationPolicy实现方式

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

envoyfilter实现

```
cat << EOF > ef-when-connection.sni-value.yaml
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: match
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
          destinationPort: 9080
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
              policies:
                ns[istio]-policy[productpage]-rule[0]:
                  permissions:
                  - andRules:
                      rules:
                      - orRules:
                          rules:
                          - requestedServerName:
                              exact: outbound_.9080_._.productpage.istio.svc.cluster.local
                  principals:
                  - andIds:
                      ids:
                      - any: true
            shadowRulesStatPrefix: istio_dry_run_allow_
EOF

kubectl apply -f  ef-when-connection.sni-value.yaml -n istio  --context context-cluster1
```

## 3.43when-connection.sni-notvalue

authorizationPolicy实现方式

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

envoyfilter实现

```
cat << EOF > ef-when-connection.sni-notvalue.yaml
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: match
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
          destinationPort: 9080
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
              policies:
                ns[istio]-policy[productpage]-rule[0]:
                  permissions:
                  - andRules:
                      rules:
                      - notRule:
                          orRules:
                            rules:
                            - requestedServerName:
                                exact: outbound_.9080_._.productpage.istio.svc.cluster.local
                  principals:
                  - andIds:
                      ids:
                      - any: true
            shadowRulesStatPrefix: istio_dry_run_allow_
EOF

kubectl apply -f  ef-when-connection.sni-notvalue.yaml -n istio  --context context-cluster1
```

## 3.44组合配置

authorizationPolicy实现方式

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

envoyfilter实现

```
cat << EOF > ef-combine.yaml
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: match
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
          destinationPort: 9080
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
              policies:
                ns[istio]-policy[productpage]-rule[0]:
                  permissions:
                  - andRules:
                      rules:
                      - orRules:
                          rules:
                          - header:
                              exactMatch: GET
                              name: ":method"
                      - orRules:
                          rules:
                          - urlPath:
                              path:
                                exact: /productpage
                  - andRules:
                      rules:
                      - orRules:
                          rules:
                          - header:
                              exactMatch: GET
                              name: ":method"
                      - orRules:
                          rules:
                          - urlPath:
                              path:
                                prefix: /static/
                  - andRules:
                      rules:
                      - orRules:
                          rules:
                          - header:
                              exactMatch: GET
                              name: ":method"
                      - orRules:
                          rules:
                          - urlPath:
                              path:
                                prefix: /api/v1/products/
                  - andRules:
                      rules:
                      - orRules:
                          rules:
                          - header:
                              exactMatch: GET
                              name: ":method"
                      - orRules:
                          rules:
                          - urlPath:
                              path:
                                exact: /logout
                  - andRules:
                      rules:
                      - orRules:
                          rules:
                          - header:
                              exactMatch: POST
                              name: ":method"
                      - orRules:
                          rules:
                          - urlPath:
                              path:
                                exact: /login
                  principals:
                  - andIds:
                      ids:
                      - orIds:
                          ids:
                          - authenticated:
                              principalName:
                                exact: spiffe://cluster.local/ns/istio-system/sa/istio-ingressgateway-service-account
                      - orIds:
                          ids:
                          - authenticated:
                              principalName:
                                safeRegex:
                                  googleRe2: {}
                                  regex: .*/ns/istio-system/.*
                      - orIds:
                          ids:
                          - directRemoteIp:
                              addressPrefix: 172.20.0.0
                              prefixLen: 16
            shadowRulesStatPrefix: istio_dry_run_allow_
EOF

kubectl apply -f  ef-combine.yaml -n istio  --context context-cluster1
```



## 3.45matcher

**还不支持**

```
cat << EOF > ef-matcher.yaml
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: match
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
          destinationPort: 9080
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
            matcher: {}
EOF

kubectl apply -f ef-matcher.yaml -n istio 
```

## 3.46shadow_rules

statistics：

| Name           | Type    | Description                                                  |
| -------------- | ------- | ------------------------------------------------------------ |
| allowed        | Counter | Total requests that were allowed access                      |
| denied         | Counter | Total requests that were denied access                       |
| shadow_allowed | Counter | Total requests that would be allowed access by the filter’s shadow rules |
| shadow_denied  | Counter | Total requests that would be denied access by the filter’s shadow rules |
| logged         | Counter | Total requests that should be logged                         |
| not_logged     | Counter | Total requests that should not be logged                     |



```
cat << EOF > ef-allow-nothing-shadow.yaml
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: match
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
          destinationPort: 9080
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
            shadow_rules:
              policies:
                ns[istio]-policy[allow-nothing]-rule[0]:
                  permissions:
                  - notRule:
                      any: true
                  principals:
                  - notId:
                      any: true
            shadowRulesStatPrefix: istio_dry_run_allow_
EOF

kubectl apply -f ef-allow-nothing-shadow.yaml -n istio 
```

3.1.1部署statsd

deploy-statsd-influxdb-grafana.yaml

kubectl apply -f deploy-statsd-influxdb-grafana.yaml -n istio

```
apiVersion: apps/v1
kind: Deployment
metadata:
  name: statsd-influxdb-grafana
spec:
  replicas: 1
  selector:
    matchLabels:
      app: statsd-influxdb-grafana
  template:
    metadata:
      labels:
        app: statsd-influxdb-grafana
    spec:
      containers:
      - name: statsd-influxdb-grafana
        image: samuelebistoletti/docker-statsd-influxdb-grafana:2.2.0
        ports:
        - name: grafana
          containerPort: 3003
        - name: influxdb-admin
          containerPort: 8888
        - name: influxdb
          containerPort: 8086
        - name: statsd
          containerPort: 8125
          protocol: UDP
---
apiVersion: v1
kind: Service
metadata:
  name: statsd-influxdb-grafana-svc
spec:
  ports:
  - name: http-grafana
    port: 3003
    targetPort: 3003
  - name: http-influxdb-admin
    port: 3004
    targetPort: 8888
  - name: tcp-influxdb
    port: 8086
    targetPort: 8086
  - name: udp-statsd
    port: 8125
    targetPort: 8125
    protocol: UDP
  selector:
    app: statsd-influxdb-grafana
```

gw,vs

gw-vs-statsd.yaml

kubectl apply -f gw-vs-statsd.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: Gateway
metadata:
  name: statsd
spec:
  selector:
    istio: ingressgateway
  servers:
  - port:
      number: 80
      name: http
      protocol: HTTP
    hosts:
    - "demo.grafana.com"
    - "demo.influxdb.com"
---
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: statsd-grafana
spec:
  hosts:
    - "demo.grafana.com"
  gateways:
  - statsd
  http:
  - match:
    - uri:
        prefix: /
    route:
    - destination:
        host: statsd-influxdb-grafana-svc.istio.svc.cluster.local
        port:
          number: 3003
---
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: statsd-influxdb
spec:
  hosts:
    - "demo.influxdb.com"
  gateways:
  - statsd
  http:
  - match:
    - uri:
        prefix: /
    route:
    - destination:
        host: statsd-influxdb-grafana-svc.istio.svc.cluster.local
        port:
          number: 3004

```



效果：

![1](17statistics\1.jpg)



![2](17statistics\2.jpg)



grafana

账号密码：root/root



Add data source on Grafana

```
Url: http://localhost:8086
Database: telegraf
User: telegraf
Password: telegraf
```

influxdb portal

URL: [http://localhost:3004](http://localhost:3004/)
Username: root
Password: root
Port: 8086

```
  meshConfig:
      proxyMetadata:
        # Enable dynamic bootstrap generation
        # https://github.com/istio/istio/pull/33456
        BOOTSTRAP_XDS_AGENT: "true"
```

ef-statictis.yaml

kubectl apply -f ef-statictis.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: statictis
spec:
  configPatches:
  - applyTo: BOOTSTRAP
    patch:
      operation: MERGE
      value:
          stats_sinks:
          - name: envoy.stat_sinks.statsd
            typed_config:
              "@type": type.googleapis.com/envoy.config.metrics.v3.StatsdSink
              address: 
                socket_address:
                  address: 10.68.19.208
                  port_value: 8125
                  protocol: UDP
              prefix: statictis
```



```
go-stress-testing -c 5 -n 100000 -u http://192.168.229.128:30555/productpage
```

![1656407549(1)](01image\1656407549(1).jpg)
