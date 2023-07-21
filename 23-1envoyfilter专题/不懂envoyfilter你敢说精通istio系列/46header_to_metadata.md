# 1什么是header_to_metadata

header_to_metadata是envoy的一个http filter，他的作用是将header转换成metadata。metadata就是元数据信息，他提供了基于匹配的  listeners, filter chains, routes and endpoints的 额外的输入参数到过滤器，他是一种map的格式，通常是filter的名字（反向dns格式）。

# 2配置

```
{
  "request_rules": [],请求规则
  "response_rules": []响应规则
}
```

request_rules，response_rules：

```
{
  "header": "...", http头
  "cookie": "...", cookie
  "on_header_present": "{...}",头存在配置
  "on_header_missing": "{...}",头不存在配置
  "remove": "..."是否删除http头
}
```

on_header_present，on_header_missing：

```
{
  "metadata_namespace": "...",元数据名称空间
  "key": "...",元数据健
  "value": "...",值
  "regex_value_rewrite": "{...}",正则值重写
  "type": "...",值类型
  "encode": "..."编码格式
}
```

regex_value_rewrite：

```
{
  "pattern": "{...}",模式
  "substitution": "..."替换
}
```

type：

- STRING字符串类型

  *(DEFAULT)* ⁣

- NUMBER数值类型

  ⁣

- PROTOBUF_VALUE   protobuf类型

encode：

- NONE  没有编码

  *(DEFAULT)* ⁣

- BASE64 base64编码

# 3案例

## 3.1request_rules

ef-header-to-metadata-request_rules.yaml

kubectl apply -f ef-header-to-metadata-request_rules.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: header-to-metadata 
spec:
  workloadSelector:
    labels:
      istio: ingressgateway
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
      operation: INSERT_FIRST
      value: 
        name: envoy.filters.http.header_to_metadata
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.filters.http.header_to_metadata.v3.Config
          request_rules:
            - header: x-version
              on_header_present:
                metadata_namespace: envoy.lb
                key: version
                type: STRING
              on_header_missing:
                metadata_namespace: envoy.lb
                key: default
                value: 'true'
                type: STRING
              remove: false

```

ef-rbac-metadata-header-to-metadata.yaml

kubectl apply -f ef-rbac-metadata-header-to-metadata.yaml  -n istio-system

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
                      filter: envoy.lb
                      path:
                      - key: version
                      value: 
                        string_match:
                          exact: "v1"
                  principals:
                  - any: true
```

curl -H "x-version: v1" http://192.168.229.128:30563/productpage

ef-rbac-metadata-header-to-metadata-mission.yaml

kubectl apply -f ef-rbac-metadata-header-to-metadata-mission.yaml  -n istio-system

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
                      filter: envoy.lb
                      path:
                      - key: default
                      value: 
                        string_match:
                          exact: "true"
                  principals:
                  - any: true
```

 curl  http://192.168.229.128:31485/productpage

## 3.2regex_value_rewrite

ef-header-to-metadata-regex_value_rewrite.yaml

kubectl apply -f ef-header-to-metadata-regex_value_rewrite.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: header-to-metadata 
spec:
  workloadSelector:
    labels:
      istio: ingressgateway
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
      operation: INSERT_FIRST
      value: 
        name: envoy.filters.http.header_to_metadata
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.filters.http.header_to_metadata.v3.Config
          request_rules:
            - header: ":path"
              on_header_present:
                metadata_namespace: envoy.lb
                key: cluster
                regex_value_rewrite:
                  pattern:
                    google_re2: {}
                    regex: "^/(cluster[\\d\\w-]+)/?.*$"
                  substitution: "\\1"

```

ef-rbac-metadata-header-to-metadata-regex_value_rewriteyaml

kubectl apply -f ef-rbac-metadata-header-to-metadata-regex_value_rewrite.yaml  -n istio-system

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
                      filter: envoy.lb
                      path:
                      - key: cluster
                      value: 
                        string_match:
                          prefix: "cluster"
                  principals:
                  - any: true
```

curl   http://192.168.229.128:30563/cluster123

## 3.3response_rules

ef-header-to-metadata-response_rules.yaml

kubectl apply -f ef-header-to-metadata-response_rules.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: header-to-metadata 
spec:
  workloadSelector:
    labels:
      istio: ingressgateway
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
      operation: INSERT_FIRST
      value: 
        name: envoy.filters.http.header_to_metadata
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.filters.http.header_to_metadata.v3.Config
          response_rules:
            - header: x-version
              on_header_present:
                metadata_namespace: envoy.lb
                key: version
                type: STRING
              on_header_missing:
                metadata_namespace: envoy.lb
                key: default
                value: 'true'
                type: STRING
              remove: false
```

ef-rbac-metadata-header-to-metadata-response_rules.yaml

kubectl apply -f ef-rbac-metadata-header-to-metadata-response_rules.yaml  -n istio-system

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
                      filter: envoy.lb
                      path:
                      - key: default
                      value: 
                        string_match:
                          exact: "true"
                  principals:
                  - any: true
```

curl  http://192.168.229.128:30563/productpage

## 3.4encode 

### 3.4.1BASE64 

ef-header-to-metadata-BASE64.yaml

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: header-to-metadata 
spec:
  workloadSelector:
    labels:
      istio: ingressgateway
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
      operation: INSERT_FIRST
      value: 
        name: envoy.filters.http.header_to_metadata
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.filters.http.header_to_metadata.v3.Config
          request_rules:
            - header: x-version
              on_header_present:
                metadata_namespace: envoy.lb
                key: version
                type: STRING
                encode: BASE64
              on_header_missing:
                metadata_namespace: envoy.lb
                key: default
                value: 'true'
                type: STRING
                encode: BASE64
              remove: false

```

ef-rbac-metadata-header-to-metadata-base64.yaml

kubectl apply -f ef-rbac-metadata-header-to-metadata-base64.yaml  -n istio-system

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
                      filter: envoy.lb
                      path:
                      - key: version
                      value: 
                        string_match:
                          exact: "djEK"
                  principals:
                  - any: true
```

curl -H "x-version: v1" http://192.168.229.128:30563/productpage

### 3.4.2NONE 

ef-header-to-metadata-NONE.yaml

kubectl apply -f ef-header-to-metadata-NONE.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: header-to-metadata 
spec:
  workloadSelector:
    labels:
      istio: ingressgateway
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
      operation: INSERT_FIRST
      value: 
        name: envoy.filters.http.header_to_metadata
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.filters.http.header_to_metadata.v3.Config
          request_rules:
            - header: x-version
              on_header_present:
                metadata_namespace: envoy.lb
                key: version
                type: STRING
                encode: NONE
              on_header_missing:
                metadata_namespace: envoy.lb
                key: default
                value: 'true'
                type: STRING
                encode: NONE
              remove: false

```

ef-rbac-metadata-header-to-metadata.yaml

kubectl apply -f ef-rbac-metadata-header-to-metadata.yaml  -n istio-system

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
                      filter: envoy.lb
                      path:
                      - key: version
                      value: 
                        string_match:
                          exact: "v1"
                  principals:
                  - any: true
```

curl -H "x-version: v1" http://192.168.229.128:30563/productpage

## 3.5type

### 3.5.1NUMBER

ef-header-to-metadata-type-NUMBER.yaml

kubectl apply -f ef-header-to-metadata-type-NUMBER.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: header-to-metadata 
spec:
  workloadSelector:
    labels:
      istio: ingressgateway
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
      operation: INSERT_FIRST
      value: 
        name: envoy.filters.http.header_to_metadata
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.filters.http.header_to_metadata.v3.Config
          request_rules:
            - header: x-version
              on_header_present:
                metadata_namespace: envoy.lb
                key: version
                type: NUMBER
              on_header_missing:
                metadata_namespace: envoy.lb
                key: default
                value: "1"
                type: NUMBER
              remove: false

```

ef-rbac-metadata-header-to-metadata-number.yaml

kubectl apply -f ef-rbac-metadata-header-to-metadata-number.yaml  -n istio-system

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
                      filter: envoy.lb
                      path:
                      - key: version
                      value: 
                        present_match: true
                  principals:
                  - any: true
```

curl -H "x-version: 1" http://192.168.229.128:30563/productpage

### 3.5.2PROTOBUF_VALUE   

ef-header-to-metadata-type-PROTOBUF_VALUE.yaml

kubectl apply -f ef-header-to-metadata-type-PROTOBUF_VALUE.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: header-to-metadata 
spec:
  workloadSelector:
    labels:
      istio: ingressgateway
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
      operation: INSERT_FIRST
      value: 
        name: envoy.filters.http.header_to_metadata
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.filters.http.header_to_metadata.v3.Config
          request_rules:
            - header: x-version
              on_header_present:
                metadata_namespace: envoy.lb
                key: version
                type: PROTOBUF_VALUE
              remove: false

```

ef-rbac-metadata-header-to-metadata-PROTOBUF_VALUE.yaml

kubectl apply -f ef-rbac-metadata-header-to-metadata-PROTOBUF_VALUE.yaml  -n istio-system

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
                      filter: envoy.lb
                      path:
                      - key: version
                      value: 
                        present_match: true
                  principals:
                  - any: true
```

curl -H "x-version: v1" http://192.168.229.128:30563/productpage