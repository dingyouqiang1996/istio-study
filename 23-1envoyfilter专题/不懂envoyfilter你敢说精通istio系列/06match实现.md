# 1什么是http match

virtualservice是istio里面用于配置路由等的crd资源。vs配置路由有三种方式分别是http，tcp，tls。这里我们讲的是http的配置。http match就是vs里面用于配置http类型的路由的。具体生成envoy配置时，作用在dynamic route里面。这里我们演示了用envoyfilter配置路由，我们配置的路由是静态的路由，作用在static route里面。我们配置的路由优先级高于dynamic route。

```
{
  "prefix": "...",
  "path": "...",
  "safe_regex": "{...}",
  "connect_matcher": "{...}",
  "path_separated_prefix": "...",
  "case_sensitive": "{...}",
  "runtime_fraction": "{...}",
  "headers": [],
  "query_parameters": [],
  "grpc": "{...}",
  "tls_context": "{...}",
  "dynamic_metadata": []
}
```

# 2实战

**httpmatch**

## 2.1authority

### 2.1.1exact

virtualservice实现

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: bookinfo
spec:
  gateways:
  - bookinfo-gateway
  hosts:
  - '*'
  http:
  - match:
    - authority:
        exact: "192.168.229.134:32688"
    route:
    - destination:
        host: productpage
        port:
          number: 9080
```



envoyfilter实现

```
cat << EOF > ef-http-match-authority-exact.yaml
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: match
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
            name: http.8080
            virtual_hosts:
            - name: “*.80”
              domains:
              - "*"
              routes:
              - match:
                  caseSensitive: true
                  headers:
                  - exactMatch: 192.168.229.128:30555
                    name: ":authority"
                  prefix: /
                route:
                  cluster: outbound|9080||productpage.istio.svc.cluster.local
EOF

kubectl apply -f  ef-http-match-authority-exact.yaml -n istio-system --context context-cluster1
```

### 2.1.2prefix

virtualservice实现

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: bookinfo
spec:
  gateways:
  - bookinfo-gateway
  hosts:
  - '*'
  http:
  - match:
    - authority:
        prefix: "192.168"
    route:
    - destination:
        host: productpage
        port:
          number: 9080
```

envoyfilter实现

```
cat << EOF > ef-http-match-authority-prefix.yaml
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: match
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
            name: http.8080
            virtual_hosts:
            - name: “*.80”
              domains:
              - "*"
              routes:
              - match:
                  caseSensitive: true
                  headers:
                  - prefixMatch: "192.168"
                    name: ":authority"
                  prefix: /
                route:
                  cluster: outbound|9080||productpage.istio.svc.cluster.local
EOF

kubectl apply -f  ef-http-match-authority-prefix.yaml -n istio-system --context context-cluster1
```



### 2.1.3regex

virtualservice实现

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: bookinfo
spec:
  gateways:
  - bookinfo-gateway
  hosts:
  - '*'
  http:
  - match:
    - authority:
        regex: "192.*"
    route:
    - destination:
        host: productpage
        port:
          number: 9080
```

envoyfilter实现

```
cat << EOF > ef-http-match-authority-regex.yaml
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: match
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
            name: http.8080
            virtual_hosts:
            - name: “*.80”
              domains:
              - "*"
              routes:
              - match:
                  caseSensitive: true
                  headers:
                  - safeRegexMatch:
                      googleRe2: {}
                      regex: 192.*
                    name: ":authority"
                  prefix: /
                route:
                  cluster: outbound|9080||productpage.istio.svc.cluster.local
EOF

kubectl apply -f  ef-http-match-authority-regex.yaml -n istio-system --context context-cluster1
```



## 2.2headers

### 2.2.1exact

virtualservice实现

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: reviews
spec:
  hosts:
  - reviews
  http:
  - match:
    - headers:
        end-user:
          exact: mark
    route:
    - destination:
        host: reviews
        subset: v2
  - route:
    - destination:
        host: reviews
        subset: v3
```

envoyfilter实现

```
cat << EOF > ef-http-match-headers-exact.yaml
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: match
spec:
  workloadSelector:
    labels:
      app: productpage
  configPatches:
  - applyTo: NETWORK_FILTER
    match:
      context: SIDECAR_OUTBOUND
      listener:
        #name: 0.0.0.0_8080  
        portNumber: 9080
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
            name: http.9080
            virtual_hosts:
            - name: "9080"
              domains:
              - "*"
              routes:
              - match:
                  caseSensitive: true
                  prefix: /details
                route:
                  cluster: outbound|9080|v1|details.istio.svc.cluster.local
              - match:
                  caseSensitive: true
                  headers:
                  - exactMatch: mark
                    name: end-user
                  prefix: /
                route:
                  cluster: outbound|9080|v2|reviews.istio.svc.cluster.local
              - match:
                  caseSensitive: true
                  prefix: /
                route:
                  cluster: outbound|9080|v3|reviews.istio.svc.cluster.local
EOF

kubectl apply -f  ef-http-match-headers-exact.yaml -n istio --context context-cluster1
```



### 2.2.2prefix

virtualservice实现

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: reviews
spec:
  hosts:
  - reviews
  http:
  - match:
    - headers:
        end-user:
          prefix: ma
    route:
    - destination:
        host: reviews
        subset: v2
  - route:
    - destination:
        host: reviews
        subset: v3
```

envoyfilter实现

```
cat << EOF > ef-http-match-headers-prefix.yaml
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: match
spec:
  workloadSelector:
    labels:
      app: productpage
  configPatches:
  - applyTo: NETWORK_FILTER
    match:
      listener:
        #name: 0.0.0.0_8080  
        portNumber: 9080
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
            name: http.9080
            virtual_hosts:
            - name: “*.9080”
              domains:
              - "*"
              routes:
              - match:
                  caseSensitive: true
                  prefix: /details
                route:
                  cluster: outbound|9080|v1|details.istio.svc.cluster.local
              - match:
                  caseSensitive: true
                  headers:
                  - prefixMatch: ma
                    name: end-user
                  prefix: /
                route:
                  cluster: outbound|9080|v2|reviews.istio.svc.cluster.local
              - match:
                  caseSensitive: true
                  prefix: /
                route:
                  cluster: outbound|9080|v3|reviews.istio.svc.cluster.local
EOF

kubectl apply -f  ef-http-match-headers-prefix.yaml -n istio --context context-cluster1
```



### 2.2.3regex

virtualservice实现

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: reviews
spec:
  hosts:
  - reviews
  http:
  - match:
    - headers:
        end-user:
          regex: "m.*k"
    route:
    - destination:
        host: reviews
        subset: v2
  - route:
    - destination:
        host: reviews
        subset: v3
```

envoyfilter实现

```
cat << EOF > ef-http-match-headers-regex.yaml
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: match
spec:
  workloadSelector:
    labels:
      app: productpage
  configPatches:
  - applyTo: NETWORK_FILTER
    match:
      listener:
        #name: 0.0.0.0_8080  
        portNumber: 9080
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
            name: http.9080
            virtual_hosts:
            - name: “*.9080”
              domains:
              - "*"
              routes:
              - match:
                  caseSensitive: true
                  prefix: /details
                route:
                  cluster: outbound|9080|v1|details.istio.svc.cluster.local
              - match:
                  caseSensitive: true
                  headers:
                  - safeRegexMatch:
                      googleRe2: {}
                      regex: m.*k
                    name: end-user
                  prefix: /
                route:
                  cluster: outbound|9080|v2|reviews.istio.svc.cluster.local
              - match:
                  caseSensitive: true
                  prefix: /
                route:
                  cluster: outbound|9080|v3|reviews.istio.svc.cluster.local
EOF

kubectl apply -f  ef-http-match-headers-regex.yaml -n istio --context context-cluster1
```



### 2.2.4cookie

virtualservice实现

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: bookinfo
spec:
  gateways:
  - bookinfo-gateway
  hosts:
  - '*'
  http:
  - match:
    - headers:
        cookie:
          regex: "^(.*?;)?(session=.*)(;.*)?$"
    route:
    - destination:
        host: productpage
        port:
          number: 9080
```

envoyfilter实现

```
cat << EOF > ef-http-match-headers-cookie.yaml
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: match
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
            name: http.8080
            virtual_hosts:
            - name: “*.8080”
              domains:
              - "*"
              routes:
              - match:
                  caseSensitive: true
                  headers:
                  - name: cookie
                    safeRegexMatch:
                      googleRe2: {}
                      regex: ^(.*?;)?(session=.*)(;.*)?$
                  prefix: /
                route:
                  cluster: outbound|9080||productpage.istio.svc.cluster.local
EOF

kubectl apply -f  ef-http-match-headers-cookie.yaml -n istio-system --context context-cluster1
```



### 2.2.5user-agent

virtualservice实现

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: bookinfo
spec:
  gateways:
  - bookinfo-gateway
  hosts:
  - '*'
  http:
  - match:
    - headers:
        user-agent:
          regex: ".*Chrome.*"
    route:
    - destination:
        host: productpage
        port:
          number: 9080
```

envoyfilter实现

```
cat << EOF > ef-http-match-headers-user-agent.yaml
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: match
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
            name: http.8080
            virtual_hosts:
            - name: “*.8080”
              domains:
              - "*"
              routes:
              - match:
                  caseSensitive: true
                  headers:
                  - name: user-agent
                    safeRegexMatch:
                      googleRe2: {}
                      regex: .*Chrome.*
                  prefix: /
                route:
                  cluster: outbound|9080||productpage.istio.svc.cluster.local
EOF

kubectl apply -f  ef-http-match-headers-user-agent.yaml -n istio-system --context context-cluster1
```



## 2.3ignoreUriCase

virtualservice实现

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: bookinfo
spec:
  gateways:
  - bookinfo-gateway
  hosts:
  - '*'
  http:
  - match:
    - uri:
        exact: "/pRODUCTPAGE"
      ignoreUriCase: true
    route:
    - destination:
        host: productpage
        port:
          number: 9080
```

envoyfilter实现

```
cat << EOF > ef-http-match-ignoreUriCase.yaml
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: match
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
            name: http.8080
            virtual_hosts:
            - name: “*.8080”
              domains:
              - "*"
              routes:
              - match:
                  caseSensitive: false
                  path: /pRODUCTPAGE
                route:
                  cluster: outbound|9080||productpage.istio.svc.cluster.local
EOF

kubectl apply -f  ef-http-match-ignoreUriCase.yaml -n istio-system --context context-cluster1
```



## 2.3method

### 2.3.1exact

virtualservice实现

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: bookinfo
spec:
  gateways:
  - bookinfo-gateway
  hosts:
  - '*'
  http:
  - match:
    - method:
        exact: "GET"
    route:
    - destination:
        host: productpage
        port:
          number: 9080
```

envoyfilter实现

```
cat << EOF > ef-http-match-method-exact.yaml
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: match
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
            name: http.8080
            virtual_hosts:
            - name: “*.8080”
              domains:
              - "*"
              routes:
              - match:
                  caseSensitive: true
                  headers:
                  - exactMatch: GET
                    name: ":method"
                  prefix: /
                route:
                  cluster: outbound|9080||productpage.istio.svc.cluster.local
EOF

kubectl apply -f  ef-http-match-method-exact.yaml -n istio-system --context context-cluster1
```



### 2.3.2prefix

virtualservice实现

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: bookinfo
spec:
  gateways:
  - bookinfo-gateway
  hosts:
  - '*'
  http:
  - match:
    - method:
        prefix: "G"
    route:
    - destination:
        host: productpage
        port:
          number: 9080
```

envoyfilter实现

```
cat << EOF > ef-http-match-method-exact.yaml
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: match
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
            name: http.8080
            virtual_hosts:
            - name: “*.8080”
              domains:
              - "*"
              routes:
              - match:
                  caseSensitive: true
                  headers:
                  - name: ":method"
                    prefixMatch: G
                  prefix: /
                route:
                  cluster: outbound|9080||productpage.istio.svc.cluster.local
EOF

kubectl apply -f  ef-http-match-method-exact.yaml -n istio-system --context context-cluster1
```



### 2.3.3regex

virtualservice实现

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: bookinfo
spec:
  gateways:
  - bookinfo-gateway
  hosts:
  - '*'
  http:
  - match:
    - method:
        regex: "G.*T"
    route:
    - destination:
        host: productpage
        port:
          number: 9080
```

envoyfilter实现

```
cat << EOF > ef-http-match-method-regex.yaml
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: match
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
            name: http.8080
            virtual_hosts:
            - name: “*.8080”
              domains:
              - "*"
              routes:
              - match:
                  caseSensitive: true
                  headers:
                  - name: :method
                    safeRegexMatch:
                      googleRe2: {}
                      regex: G.*T
                  prefix: /
                route:
                  cluster: outbound|9080||productpage.istio.svc.cluster.local
EOF

kubectl apply -f  ef-http-match-method-regex.yaml -n istio-system --context context-cluster1
```



## 2.4port

virtualservice实现

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: bookinfo
spec:
  gateways:
  - bookinfo-gateway
  hosts:
  - '*'
  http:
  - match:
    - port: 80
    route:
    - destination:
        host: productpage
        port:
          number: 9080
```

envoyfilter实现

```
cat << EOF > ef-http-match-port.yaml
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: match
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
            name: http.9080
            virtual_hosts:
            - name: “*.9080”
              domains:
              - "*"
              routes:
              - match:
                  caseSensitive: true
                  prefix: /
                route:
                  cluster: outbound|9080||productpage.istio.svc.cluster.local
EOF

kubectl apply -f  ef-http-match-port.yaml -n istio-system --context context-cluster1
```



## 2.5queryParams

### 2.5.1exact

virtualservice实现

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: bookinfo
spec:
  gateways:
  - bookinfo-gateway
  hosts:
  - '*'
  http:
  - match:
    - queryParams:
        test:
          exact: test
    route:
    - destination:
        host: productpage
        port:
          number: 9080
```

envoyfilter实现

```
cat << EOF > ef-http-match-queryParams-exact.yaml
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: match
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
            name: http.9080
            virtual_hosts:
            - name: “*.9080”
              domains:
              - "*"
              routes:
              - match:
                  caseSensitive: true
                  prefix: /
                  queryParameters:
                  - name: test
                    stringMatch:
                      exact: test  
                route:
                  cluster: outbound|9080||productpage.istio.svc.cluster.local
EOF

kubectl apply -f  ef-http-match-queryParams-exact.yaml -n istio-system --context context-cluster1
```



### 2.5.2prefix

virtualservice实现

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: bookinfo
spec:
  gateways:
  - bookinfo-gateway
  hosts:
  - '*'
  http:
  - match:
    - queryParams:
        test:
          prefix: test
    route:
    - destination:
        host: productpage
        port:
          number: 9080
```

envoyfilter实现

```
cat << EOF > ef-http-match-queryParams-prefix.yaml
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: match
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
            name: http.9080
            virtual_hosts:
            - name: “*.9080”
              domains:
              - "*"
              routes:
              - match:
                  caseSensitive: true
                  prefix: /
                  queryParameters:
                  - name: test
                    stringMatch:
                      prefix: test  
                route:
                  cluster: outbound|9080||productpage.istio.svc.cluster.local
EOF

kubectl apply -f  ef-http-match-queryParams-prefix.yaml -n istio-system --context context-cluster1
```



### 2.5.3regex

virtualservice实现

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: bookinfo
spec:
  gateways:
  - bookinfo-gateway
  hosts:
  - '*'
  http:
  - match:
    - queryParams:
        test:
          regex: "\\d+$"
    route:
    - destination:
        host: productpage
        port:
          number: 9080
```

envoyfilter实现

```
cat << EOF > ef-http-match-queryParams-regex.yaml
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: match
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
            name: http.9080
            virtual_hosts:
            - name: “*.9080”
              domains:
              - "*"
              routes:
              - match:
                  caseSensitive: true
                  prefix: /
                  queryParameters:
                  - name: test
                    string_match:
                      safe_regex:
                        google_re2: {}
                        regex: \\d+$  
                route:
                  cluster: outbound|9080||productpage.istio.svc.cluster.local
EOF

kubectl apply -f  ef-http-match-queryParams-regex.yaml -n istio-system --context context-cluster1
```



## 2.6scheme

### 2.6.1exact

virtualservice实现

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: bookinfo
spec:
  gateways:
  - bookinfo-gateway
  hosts:
  - '*'
  http:
  - match:
    - scheme:
        exact: "https"
    route:
    - destination:
        host: productpage
        port:
          number: 9080
```

envoyfilter实现

```
cat << EOF > ef-http-match-scheme-exact.yaml
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: match
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
            name: http.9080
            virtual_hosts:
            - name: “*.9080”
              domains:
              - "*"
              routes:
              - match:
                  caseSensitive: true
                  headers:
                  - exactMatch: http
                    name: :scheme
                  prefix: /  
                route:
                  cluster: outbound|9080||productpage.istio.svc.cluster.local
EOF

kubectl apply -f  ef-http-match-scheme-exact.yaml -n istio-system --context context-cluster1
```

### 2.6.2prefix

virtualservice实现

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: bookinfo
spec:
  gateways:
  - bookinfo-gateway
  hosts:
  - '*'
  http:
  - match:
    - scheme:
        prefix: "http"
    route:
    - destination:
        host: productpage
        port:
          number: 9080
```

envoyfilter实现

```
cat << EOF > ef-http-match-scheme-prefix.yaml
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: match
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
            name: http.9080
            virtual_hosts:
            - name: “*.9080”
              domains:
              - "*"
              routes:
              - match:
                  caseSensitive: true
                  headers:
                  - name: :scheme
                    prefixMatch: http
                  prefix: /  
                route:
                  cluster: outbound|9080||productpage.istio.svc.cluster.local
EOF

kubectl apply -f  ef-http-match-scheme-prefix.yaml -n istio-system --context context-cluster1
```



### 2.6.3regex

virtualservice实现

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: bookinfo
spec:
  gateways:
  - bookinfo-gateway
  hosts:
  - '*'
  http:
  - match:
    - scheme:
        regex: ".*"
    route:
    - destination:
        host: productpage
        port:
          number: 9080
```

envoyfilter实现

```
cat << EOF > ef-http-match-scheme-regex.yaml
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: match
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
            name: http.9080
            virtual_hosts:
            - name: “*.9080”
              domains:
              - "*"
              routes:
              - match:
                  caseSensitive: true
                  headers:
                  - name: :scheme
                    safeRegexMatch:
                      googleRe2: {}
                      regex: .*
                  prefix: /  
                route:
                  cluster: outbound|9080||productpage.istio.svc.cluster.local
EOF

kubectl apply -f  ef-http-match-scheme-regex.yaml -n istio-system --context context-cluster1
```



## 2.7sourceLabels

virtualservice实现

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: reviews
spec:
  hosts:
  - reviews
  http:
  - match:
    - sourceLabels:
        app: productpage
        version: v1
    route:
    - destination:
        host: reviews
        subset: v2
```

不生效，待研究

envoyfilter实现





## 2.8sourceNamespace

virtualservice实现

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: bookinfo
spec:
  gateways:
  - bookinfo-gateway
  hosts:
  - '*'
  http:
  - match:
    - sourceNamespace: istio-system
    route:
    - destination:
        host: productpage
        port:
          number: 9080
```

envoyfilter实现

```
cat << EOF > ef-http-match-sourceNamespace.yaml
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: match
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
            name: http.9080
            virtual_hosts:
            - name: “*.9080”
              domains:
              - "*"
              routes:
              - match:
                  caseSensitive: true
                  prefix: /  
                route:
                  cluster: outbound|9080||productpage.istio.svc.cluster.local
EOF

kubectl apply -f  ef-http-match-sourceNamespace.yaml -n istio-system --context context-cluster1
```



## 2.9uri

### 2.9.1exact

virtualservice实现

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: bookinfo
spec:
  gateways:
  - bookinfo-gateway
  hosts:
  - '*'
  http:
  - match:
    - uri:
        exact: /productpage
    route:
    - destination:
        host: productpage
        port:
          number: 9080
```

envoyfilter实现

```
cat << EOF > ef-http-match-uri-exact.yaml
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: match
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
            name: http.9080
            virtual_hosts:
            - name: “*.9080”
              domains:
              - "*"
              routes:
              - match:
                  caseSensitive: true
                  path: /productpage 
                route:
                  cluster: outbound|9080||productpage.istio.svc.cluster.local
EOF

kubectl apply -f  ef-http-match-uri-exact.yaml -n istio-system --context context-cluster1
```



### 2.9.2prefix

virtualservice实现

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: bookinfo
spec:
  gateways:
  - bookinfo-gateway
  hosts:
  - '*'
  http:
  - match:
    - uri:
        prefix: /product
    route:
    - destination:
        host: productpage
        port:
          number: 9080
```

envoyfilter实现

```
cat << EOF > ef-http-match-uri-prefix.yaml
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: match
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
            name: http.9080
            virtual_hosts:
            - name: “*.9080”
              domains:
              - "*"
              routes:
              - match:
                  caseSensitive: true
                  prefix: /product 
                route:
                  cluster: outbound|9080||productpage.istio.svc.cluster.local
EOF

kubectl apply -f  ef-http-match-uri-prefix.yaml -n istio-system --context context-cluster1
```



### 2.9.3regex

virtualservice实现

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: bookinfo
spec:
  gateways:
  - bookinfo-gateway
  hosts:
  - '*'
  http:
  - match:
    - uri:
        regex: "/p.*e"
    route:
    - destination:
        host: productpage
        port:
          number: 9080
```

envoyfilter实现

```
cat << EOF > ef-http-match-uri-regex.yaml
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: match
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
            name: http.9080
            virtual_hosts:
            - name: “*.9080”
              domains:
              - "*"
              routes:
              - match:
                  caseSensitive: true
                  safeRegex:
                    googleRe2: {}
                    regex: /p.*e
                route:
                  cluster: outbound|9080||productpage.istio.svc.cluster.local
EOF

kubectl apply -f  ef-http-match-uri-regex.yaml -n istio-system --context context-cluster1
```



## 2.10withoutHeaders

### 2.10.1exact

virtualservice实现

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: bookinfo
spec:
  gateways:
  - bookinfo-gateway
  hosts:
  - '*'
  http:
  - match:
    - withoutHeaders:
        end-user:
          exact: mark
    route:
    - destination:
        host: productpage
        port:
          number: 9080
```

envoyfilter实现

```
cat << EOF > ef-http-match-withoutHeaders-exact.yaml
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: match
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
            name: http.9080
            virtual_hosts:
            - name: “*.9080”
              domains:
              - "*"
              routes:
              - match:
                  path: /productpage
                  caseSensitive: true
                  headers:
                  - exactMatch: mark
                    invertMatch: true
                    name: end-user
                route:
                  cluster: outbound|9080||productpage.istio.svc.cluster.local
EOF

kubectl apply -f  ef-http-match-withoutHeaders-exact.yaml -n istio-system --context context-cluster1
```

```
curl http://192.168.229.128:30555/productpage -H "end-user: mark"
```



### 2.10.2prefix

virtualservice实现

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: bookinfo
spec:
  gateways:
  - bookinfo-gateway
  hosts:
  - '*'
  http:
  - match:
    - withoutHeaders:
        end-user:
          prefix: ma
    route:
    - destination:
        host: productpage
        port:
          number: 9080
```

envoyfilter实现

```
cat << EOF > ef-http-match-withoutHeaders-prefix.yaml
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: match
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
            name: http.9080
            virtual_hosts:
            - name: “*.9080”
              domains:
              - "*"
              routes:
              - match:
                  path: /productpage
                  caseSensitive: true
                  headers:
                  - prefixMatch: ma
                    invertMatch: true
                    name: end-user
                route:
                  cluster: outbound|9080||productpage.istio.svc.cluster.local
EOF

kubectl apply -f  ef-http-match-withoutHeaders-prefix.yaml -n istio-system --context context-cluster1
```



### 2.10.3regex

virtualservice实现

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: bookinfo
spec:
  gateways:
  - bookinfo-gateway
  hosts:
  - '*'
  http:
  - match:
    - withoutHeaders:
        end-user:
          regex: "m.*k"
    route:
    - destination:
        host: productpage
        port:
          number: 9080
```

envoyfilter实现

```
cat << EOF > ef-http-match-withoutHeaders-regex.yaml
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: match
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
            name: http.9080
            virtual_hosts:
            - name: “*.9080”
              domains:
              - "*"
              routes:
              - match:
                  path: /productpage
                  caseSensitive: true
                  headers:
                  - invertMatch: true
                    name: end-user
                    safeRegexMatch:
                      googleRe2: {}
                      regex: m.*k
                route:
                  cluster: outbound|9080||productpage.istio.svc.cluster.local
EOF

kubectl apply -f  ef-http-match-withoutHeaders-regex.yaml -n istio-system --context context-cluster1
```

## 2.11connect_matcher

ef-match-connect_matcher.yaml

kubectl apply -f ef-match-connect_matcher.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: match
  namespace: istio-system
spec:
  workloadSelector:
    labels:
      istio: ingressgateway
  configPatches:
  - applyTo: LISTENER
    patch:
      operation: ADD
      value:
        name: listener_0
        address:
          socket_address:
            protocol: TCP
            address: 0.0.0.0
            port_value: 31400
        filter_chains:
        - filters:
          - name: envoy.filters.network.http_connection_manager
            typed_config:
              "@type": type.googleapis.com/envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager
              stat_prefix: ingress_http
              route_config:
                name: local_route
                virtual_hosts:
                - name: local_service
                  domains:
                  - "*"
                  routes:
                  - match:
                      connect_matcher:
                        {}
                    route:
                      cluster: service_google
                      upgrade_configs:
                      - upgrade_type: CONNECT
                        connect_config:
                          {}
              http_filters:
              - name: envoy.filters.http.router
                typed_config:
                  "@type": type.googleapis.com/envoy.extensions.filters.http.router.v3.Router
              http_protocol_options: {}
              upgrade_configs:
              - upgrade_type: CONNECT
  - applyTo: CLUSTER
    patch:
      operation: ADD
      value: 
        name: service_google
        connect_timeout: 0.25s
        type: LOGICAL_DNS
        dns_lookup_family: V4_ONLY
        lb_policy: ROUND_ROBIN
        load_assignment:
          cluster_name: service_google
          endpoints:
          - lb_endpoints:
            - endpoint:
                address:
                  socket_address:
                    address: www.baidu.com
                    port_value: 443
```



## 2.12path_separated_prefix

```
cat << EOF > ef-http-match-path_separated_prefix.yaml
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: match
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
            name: http.9080
            virtual_hosts:
            - name: “*.9080”
              domains:
              - "*"
              routes:
              - match:
                  caseSensitive: true
                  path_separated_prefix: /productpage 
                route:
                  cluster: outbound|9080||productpage.istio.svc.cluster.local
EOF

kubectl apply -f  ef-http-match-path_separated_prefix.yaml -n istio-system --context context-cluster1
```

 匹配：

http://192.168.229.128:30555/productpage/

kubectl logs -n istio productpage-v1-6654bf587c-jg5k8 -c istio-proxy -f

**productpage**有日志说明匹配了

## 2.13runtime_fraction

```
cat << EOF > ef-http-match-runtime_fraction.yaml
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: match
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
            name: http.9080
            virtual_hosts:
            - name: “*.9080”
              domains:
              - "*"
              routes:
              - match:
                  caseSensitive: true
                  path_separated_prefix: /productpage 
                  runtime_fraction:
                    default_value:
                      numerator: 50
                      denominator: HUNDRED
                    runtime_key: reoutes_match
                route:
                  cluster: outbound|9080||productpage.istio.svc.cluster.local
EOF

kubectl apply -f  ef-http-match-runtime_fraction.yaml -n istio-system --context context-cluster1
```



## 2.14grpc



```
xport GOPATH=/root/grpc-demo
go env -w GOPROXY=https://goproxy.cn,direct
go env -w GO111MODULE=on
go get github.com/zhuge20100104/grpc-demo/grpc-1/client/services
```

client.go

```
package main

import (
        "context"
        "fmt"
        "log"
         "time"

        "github.com/zhuge20100104/grpc-demo/grpc-1/client/services"

        "google.golang.org/grpc"
)

func main() {
        conn, err := grpc.Dial("product-service:8081", grpc.WithInsecure())
        if err != nil {
                log.Fatalf("连接GRPC服务端失败 %v\n", err)
        }

        defer conn.Close()
        prodClient := services.NewProductServiceClient(conn)
        for true  {
                prodRes, err := prodClient.GetProductStock(context.Background(),
                        &services.ProdRequest{ProdId: 12})

                if err != nil {
                        log.Fatalf("请求GRPC服务端失败 %v\n", err)
                 }
                fmt.Println(prodRes.ProdStock)
                time.Sleep(time.Duration(1)*time.Second)
        } 
}
```

server.go

```
package main

import (
        "log"
        "net"

        "github.com/zhuge20100104/grpc-demo/grpc-1/server/services"
        "google.golang.org/grpc"
)

func main() {
        rpcServer := grpc.NewServer()
        services.RegisterProductServiceServer(rpcServer, new(services.ProdService))
        listen, err := net.Listen("tcp", ":8081")
        if err != nil {
                log.Fatalf("启动网络监听失败 %v\n", err)
        }
        rpcServer.Serve(listen)
}
```

deploy-product-service.yaml

kubectl apply -f deploy-product-service.yaml -n istio

```

---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: product-service-server
  labels:
    app: product-service-server
spec:
  selector:
    matchLabels:
      app: product-service-server
  replicas: 1
  template:
    metadata:
      labels:
        app: product-service-server
        version: v1
    spec:
      containers:
        - name: csrf
          image: registry.cn-qingdao.aliyuncs.com/hxpdocker/product-service-server:1.0
          imagePullPolicy: Always
          ports:
            - containerPort: 8081
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: product-service-client
  labels:
    app: product-service-client
spec:
  selector:
    matchLabels:
      app: product-service-client
  replicas: 1
  template:
    metadata:
      labels:
        app: product-service-client
        version: v1
    spec:
      containers:
        - name: csrf
          image: registry.cn-qingdao.aliyuncs.com/hxpdocker/product-service-client:1.0
          imagePullPolicy: Always
---
apiVersion: v1
kind: Service
metadata:
  name: product-service
spec:
  selector:
    app: product-service-server
  ports:
    - name: grpc-product-service
      protocol: TCP
      port: 8081
      targetPort: 8081
```



```
cat << EOF > ef-match-grpc.yaml
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: match
spec:
  workloadSelector:
    labels:
      app: product-service-server
  configPatches:
  - applyTo: HTTP_ROUTE
    match:
      context: SIDECAR_INBOUND
      routeConfiguration:
        vhost:
          name: "inbound|http|8081"
          route:
            action: ANY
    patch:
      operation: MERGE
      value:
        match:
          grpc: {}
          
EOF

kubectl apply -f  ef-match-grpc.yaml -n istio 
```



```
cat << EOF > ef-match-grpc-productpage.yaml
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: match
spec:
  workloadSelector:
    labels:
      app: productpage
  configPatches:
  - applyTo: HTTP_ROUTE
    match:
      context: SIDECAR_INBOUND
      routeConfiguration:
        vhost:
          name: "inbound|http|9080"
          route:
            action: ANY
    patch:
      operation: MERGE
      value:
        match:
          grpc: {}
          
EOF

kubectl apply -f  ef-match-grpc-productpage.yaml -n istio
```



## 2.15tls_context

deploy-service-https.yaml

kubectl apply -f deploy-service-https.yaml -n istio

```
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: service-https
  labels:
    app: service-https
spec:
  selector:
    matchLabels:
      app: service-https
  replicas: 1
  template:
    metadata:
      labels:
        app: service-https
        version: v1
    spec:
      containers:
        - name: csrf
          image: mendhak/http-https-echo
          imagePullPolicy: Always
          ports:
            - containerPort: 443
          env:
          - name: HTTP_PORT
            value: "0"
---
apiVersion: v1
kind: Service
metadata:
  name: service-https
spec:
  selector:
    app: service-https
  ports:
    - name: tls-service-https
      protocol: TCP
      port: 443
      targetPort: 443
```



**无效，tcp_proxy流量**

```
cat << EOF > ef-match-tls_context.yaml
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: match
spec:
  workloadSelector:
    labels:
      app: service-https
  configPatches:
  - applyTo: HTTP_ROUTE
    match:
      context: SIDECAR_INBOUND
      routeConfiguration:
        vhost:
          name: "inbound|http|443"
          route:
            action: ANY
    patch:
      operation: MERGE
      value:
        match:
          tls_context:
            presented: true
            validated: true
          
EOF

kubectl apply -f  ef-match-tls_context.yaml -n istio 
```



ef-match-add-listener.yaml

kubectl apply -f ef-match-add-listener.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: match
  namespace: istio-system 
spec:
  workloadSelector:
    labels:
      istio: ingressgateway
  configPatches:
  - applyTo: LISTENER
    match:
      context: GATEWAY
    patch:
      operation: ADD
      value:
        name: proxy
        address:
          socket_address:
            address: 0.0.0.0
            port_value: 31400
        filter_chains:
        - filters:
          - name: envoy.filters.network.http_connection_manager
            typed_config:
              "@type": type.googleapis.com/envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager
              codec_type: AUTO
              stat_prefix: ingress_http
              route_config:
                name: local_route
                virtual_hosts:
                - name: app
                  domains:
                  - "*"
                  routes:
                  - match:
                      prefix: "/"
                    route:
                      cluster: service-https
              http_filters:
              - name: envoy.filters.http.router
          transport_socket:
            name: envoy.transport_sockets.tls
            typed_config:
              "@type": type.googleapis.com/envoy.extensions.transport_sockets.tls.v3.DownstreamTlsContext
              common_tls_context:
                tls_certificates:
                # The following self-signed certificate pair is generated using:
                # $ openssl req -x509 -newkey rsa:2048 -keyout a/front-proxy-key.pem -out  a/front-proxy-crt.pem -days 3650 -nodes -subj '/CN=front-envoy'
                #
                # Instead of feeding it as an inline_string, certificate pair can also be fed to Envoy
                # via filename. Reference: https://www.envoyproxy.io/docs/envoy/latest/api-v3/config/core/v3/base.proto#config-core-v3-datasource.
                #
                # Or in a dynamic configuration scenario, certificate pair can be fetched remotely via
                # Secret Discovery Service (SDS). Reference: https://www.envoyproxy.io/docs/envoy/latest/configuration/security/secret.
                - certificate_chain:
                    inline_string: |
                      -----BEGIN CERTIFICATE-----
                      MIICqDCCAZACCQCquzpHNpqBcDANBgkqhkiG9w0BAQsFADAWMRQwEgYDVQQDDAtm
                      cm9udC1lbnZveTAeFw0yMDA3MDgwMTMxNDZaFw0zMDA3MDYwMTMxNDZaMBYxFDAS
                      BgNVBAMMC2Zyb250LWVudm95MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKC
                      AQEAthnYkqVQBX+Wg7aQWyCCb87hBce1hAFhbRM8Y9dQTqxoMXZiA2n8G089hUou
                      oQpEdJgitXVS6YMFPFUUWfwcqxYAynLK4X5im26Yfa1eO8La8sZUS+4Bjao1gF5/
                      VJxSEo2yZ7fFBo8M4E44ZehIIocipCRS+YZehFs6dmHoq/MGvh2eAHIa+O9xssPt
                      ofFcQMR8rwBHVbKy484O10tNCouX4yUkyQXqCRy6HRu7kSjOjNKSGtjfG+h5M8bh
                      10W7ZrsJ1hWhzBulSaMZaUY3vh5ngpws1JATQVSK1Jm/dmMRciwlTK7KfzgxHlSX
                      58ENpS7yPTISkEICcLbXkkKGEQIDAQABMA0GCSqGSIb3DQEBCwUAA4IBAQCmj6Hg
                      vwOxWz0xu+6fSfRL6PGJUGq6wghCfUvjfwZ7zppDUqU47fk+yqPIOzuGZMdAqi7N
                      v1DXkeO4A3hnMD22Rlqt25vfogAaZVToBeQxCPd/ALBLFrvLUFYuSlS3zXSBpQqQ
                      Ny2IKFYsMllz5RSROONHBjaJOn5OwqenJ91MPmTAG7ujXKN6INSBM0PjX9Jy4Xb9
                      zT+I85jRDQHnTFce1WICBDCYidTIvJtdSSokGSuy4/xyxAAc/BpZAfOjBQ4G1QRe
                      9XwOi790LyNUYFJVyeOvNJwveloWuPLHb9idmY5YABwikUY6QNcXwyHTbRCkPB2I
                      m+/R4XnmL4cKQ+5Z
                      -----END CERTIFICATE-----
                  private_key:
                    inline_string: |
                      -----BEGIN PRIVATE KEY-----
                      MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQC2GdiSpVAFf5aD
                      tpBbIIJvzuEFx7WEAWFtEzxj11BOrGgxdmIDafwbTz2FSi6hCkR0mCK1dVLpgwU8
                      VRRZ/ByrFgDKcsrhfmKbbph9rV47wtryxlRL7gGNqjWAXn9UnFISjbJnt8UGjwzg
                      Tjhl6EgihyKkJFL5hl6EWzp2Yeir8wa+HZ4Achr473Gyw+2h8VxAxHyvAEdVsrLj
                      zg7XS00Ki5fjJSTJBeoJHLodG7uRKM6M0pIa2N8b6HkzxuHXRbtmuwnWFaHMG6VJ
                      oxlpRje+HmeCnCzUkBNBVIrUmb92YxFyLCVMrsp/ODEeVJfnwQ2lLvI9MhKQQgJw
                      tteSQoYRAgMBAAECggEAeDGdEkYNCGQLe8pvg8Z0ccoSGpeTxpqGrNEKhjfi6NrB
                      NwyVav10iq4FxEmPd3nobzDPkAftfvWc6hKaCT7vyTkPspCMOsQJ39/ixOk+jqFx
                      lNa1YxyoZ9IV2DIHR1iaj2Z5gB367PZUoGTgstrbafbaNY9IOSyojCIO935ubbcx
                      DWwL24XAf51ez6sXnI8V5tXmrFlNXhbhJdH8iIxNyM45HrnlUlOk0lCK4gmLJjy9
                      10IS2H2Wh3M5zsTpihH1JvM56oAH1ahrhMXs/rVFXXkg50yD1KV+HQiEbglYKUxO
                      eMYtfaY9i2CuLwhDnWp3oxP3HfgQQhD09OEN3e0IlQKBgQDZ/3poG9TiMZSjfKqL
                      xnCABMXGVQsfFWNC8THoW6RRx5Rqi8q08yJrmhCu32YKvccsOljDQJQQJdQO1g09
                      e/adJmCnTrqxNtjPkX9txV23Lp6Ak7emjiQ5ICu7iWxrcO3zf7hmKtj7z+av8sjO
                      mDI7NkX5vnlE74nztBEjp3eC0wKBgQDV2GeJV028RW3b/QyP3Gwmax2+cKLR9PKR
                      nJnmO5bxAT0nQ3xuJEAqMIss/Rfb/macWc2N/6CWJCRT6a2vgy6xBW+bqG6RdQMB
                      xEZXFZl+sSKhXPkc5Wjb4lQ14YWyRPrTjMlwez3k4UolIJhJmwl+D7OkMRrOUERO
                      EtUvc7odCwKBgBi+nhdZKWXveM7B5N3uzXBKmmRz3MpPdC/yDtcwJ8u8msUpTv4R
                      JxQNrd0bsIqBli0YBmFLYEMg+BwjAee7vXeDFq+HCTv6XMva2RsNryCO4yD3I359
                      XfE6DJzB8ZOUgv4Dvluie3TB2Y6ZQV/p+LGt7G13yG4hvofyJYvlg3RPAoGAcjDg
                      +OH5zLN2eqah8qBN0CYa9/rFt0AJ19+7/smLTJ7QvQq4g0gwS1couplcCEnNGWiK
                      72y1n/ckvvplmPeAE19HveMvR9UoCeV5ej86fACy8V/oVpnaaLBvL2aCMjPLjPP9
                      DWeCIZp8MV86cvOrGfngf6kJG2qZTueXl4NAuwkCgYEArKkhlZVXjwBoVvtHYmN2
                      o+F6cGMlRJTLhNc391WApsgDZfTZSdeJsBsvvzS/Nc0burrufJg0wYioTlpReSy4
                      ohhtprnQQAddfjHP7rh2LGt+irFzhdXXQ1ybGaGM9D764KUNCXLuwdly0vzXU4HU
                      q5sGxGrC1RECGB5Zwx2S2ZY=
                      -----END PRIVATE KEY-----

  - applyTo: CLUSTER
    patch:
      operation: ADD
      value: 
        name: service-https
        type: STRICT_DNS
        lb_policy: ROUND_ROBIN
        load_assignment:
          cluster_name: service-https
          endpoints:
          - lb_endpoints:
            - endpoint:
                address:
                  socket_address:
                    address: service-https.istio.svc.cluster.local
                    port_value: 443
        transport_socket:
          name: envoy.transport_sockets.tls
          typed_config:
            "@type": type.googleapis.com/envoy.extensions.transport_sockets.tls.v3.UpstreamTlsContext 

```

网页浏览：

```
https://192.168.229.128:30639/

{
path: "/",
headers: {
host: "192.168.229.128:30639",
cache-control: "max-age=0",
sec-ch-ua: "" Not A;Brand";v="99", "Chromium";v="99", "Google Chrome";v="99"",
sec-ch-ua-mobile: "?0",
sec-ch-ua-platform: ""Windows"",
upgrade-insecure-requests: "1",
user-agent: "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/99.0.4844.82 Safari/537.36",
accept: "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9",
sec-fetch-site: "none",
sec-fetch-mode: "navigate",
sec-fetch-user: "?1",
sec-fetch-dest: "document",
accept-encoding: "gzip, deflate, br",
accept-language: "en-GB,en-US;q=0.9,en;q=0.8",
x-forwarded-proto: "https",
x-request-id: "ced8ce77-d0a9-4a58-8e09-cce854e03e1c",
x-envoy-expected-rq-timeout-ms: "15000",
},
method: "GET",
body: "",
fresh: false,
hostname: "192.168.229.128",
ip: "::ffff:127.0.0.6",
ips: [ ],
protocol: "https",
query: { },
subdomains: [ ],
xhr: false,
os: {
hostname: "service-https-9777c5679-9jpmh"
},
connection: {
servername: false
},
}
```



用curl实现：

```
/ $ curl  -k  https://service-https.istio:443 
{
  "path": "/",
  "headers": {
    "host": "service-https.istio",
    "user-agent": "curl/7.83.1-DEV",
    "accept": "*/*"
  },
  "method": "GET",
  "body": "",
  "fresh": false,
  "hostname": "service-https.istio",
  "ip": "::ffff:127.0.0.6",
  "ips": [],
  "protocol": "https",
  "query": {},
  "subdomains": [],
  "xhr": false,
  "os": {
    "hostname": "service-https-9777c5679-9jpmh"
  },
  "connection": {
    "servername": "service-https.istio"
  }
}/
```



## 2.16dynamic_metadata

envoyfilter实现

```
cat << EOF > jwt-ingressgateway-payloadInMetadata.yaml
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: jwt
spec:
  workloadSelector:
    labels:
      istio: ingressgateway
  priority: 10
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
                payloadInMetadata: my_payload
            rules:
            - match:
                prefix: /
              requires:
                requiresAny:
                  requirements:
                  - providerName: origins-0
                  - allowMissing: {}
EOF

kubectl apply -f jwt-ingressgateway-payloadInMetadata.yaml -n istio-system
```



```
cat << EOF > ef-http-match-dynamic_metadata.yaml
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
            name: http.9080
            virtual_hosts:
            - name: “*.9080”
              domains:
              - "*"
              routes:
              - match:
                  path: /productpage
                  dynamic_metadata:
                  - filter: envoy.filters.http.jwt_authn
                    path:
                    - key: my_payload
                    - key: iss
                    value: 
                      string_match:
                        exact: "testing@secure.istio.io"
                route:
                  cluster: outbound|9080||productpage.istio.svc.cluster.local
EOF

kubectl apply -f  ef-http-match-dynamic_metadata.yaml -n istio-system
```



```
TOKEN=eyJhbGciOiJSUzI1NiIsImtpZCI6IkRIRmJwb0lVcXJZOHQyenBBMnFYZkNtcjVWTzVaRXI0UnpIVV8tZW52dlEiLCJ0eXAiOiJKV1QifQ.eyJleHAiOjM1MzczOTExMDQsImdyb3VwcyI6WyJncm91cDEiLCJncm91cDIiXSwiaWF0IjoxNTM3MzkxMTA0LCJpc3MiOiJ0ZXN0aW5nQHNlY3VyZS5pc3Rpby5pbyIsInNjb3BlIjpbInNjb3BlMSIsInNjb3BlMiJdLCJzdWIiOiJ0ZXN0aW5nQHNlY3VyZS5pc3Rpby5pbyJ9.EdJnEZSH6X8hcyEii7c8H5lnhgjB5dwo07M5oheC8Xz8mOllyg--AHCFWHybM48reunF--oGaG6IXVngCEpVF0_P5DwsUoBgpPmK1JOaKN6_pe9sh0ZwTtdgK_RP01PuI7kUdbOTlkuUi2AO-qUyOm7Art2POzo36DLQlUXv8Ad7NBOqfQaKjE9ndaPWT7aexUsBHxmgiGbz1SyLH879f7uHYPbPKlpHU6P9S-DaKnGLaEchnoKnov7ajhrEhGXAQRukhDPKUHO9L30oPIr5IJllEQfHYtt6IZvlNUGeLUcif3wpry1R5tBXRicx2sXMQ7LyuDremDbcNy_iE76Upg

curl 192.168.229.128:30555/productpage?my-token=${TOKEN}
```



ef-rbac-metadata.yaml

kubectl apply -f ef-rbac-metadata.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: rbac
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
                      - key: my_payload
                      - key: iss
                      value: 
                        string_match:
                          exact: "testing@secure.istio.io"
                  principals:
                  - any: true
```

