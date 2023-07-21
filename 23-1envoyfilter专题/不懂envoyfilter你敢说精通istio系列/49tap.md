# 1什么是tap

tap在envoy这里是窃取窃听的意思，具体是请求流量通过配置后窃取到第三方sink里，sink可以是admin的sink或其他sink。tap可以配置在传输层里，也可以配置在http filter里。

# 2配置

common_config：

```
{
  "admin_config": "{...}",通过admin handle配置
  "static_config": "{...}"通过静态配置，不可更改
}
```

admin_config：

```
{
  "config_id": "..."配置id
}
```

static_config：

```
{
  "match_config": "{...}",匹配配置
  "match": "{...}",匹配配置，如果match_config和match都配置了，match生效
  "output_config": "{...}"输出配置
}
```

match_config：

```
{
  "or_match": "{...}",或匹配
  "and_match": "{...}",与匹配
  "not_match": "{...}",非匹配
  "any_match": "...",任意匹配
  "http_request_headers_match": "{...}",http请求头匹配
  "http_request_trailers_match": "{...}",http请求trailer匹配
  "http_response_headers_match": "{...}",响应头匹配
  "http_response_trailers_match": "{...}",响应trailer匹配
  "http_request_generic_body_match": "{...}",请求体匹配
  "http_response_generic_body_match": "{...}"响应体匹配
}
```

match:

```
{
  "or_match": "{...}",或匹配
  "and_match": "{...}",与匹配
  "not_match": "{...}",非匹配
  "any_match": "...",任意匹配
  "http_request_headers_match": "{...}",http请求头匹配
  "http_request_trailers_match": "{...}",http请求trailer匹配
  "http_response_headers_match": "{...}",响应头匹配
  "http_response_trailers_match": "{...}",响应trailer匹配
  "http_request_generic_body_match": "{...}",请求体匹配
  "http_response_generic_body_match": "{...}"响应体匹配
}
```

output_config：

```
{
  "sinks": [],输出位置配置
  "max_buffered_rx_bytes": "{...}",最大缓存接收的字节
  "max_buffered_tx_bytes": "{...}",最大缓存发送的字节
  "streaming": "..."一个tap单个缓存消息还是多个
}
```

sinks:

```
{
  "format": "...",输出格式
  "streaming_admin": "{...}",输出管理接口
  "file_per_tap": "{...}"每个tap一个文件配置
}
```

format:

- JSON_BODY_AS_BYTES

  *(DEFAULT)* ⁣Each message will be written as JSON. Any [body](https://www.envoyproxy.io/docs/envoy/latest/api-v3/data/tap/v3/common.proto#envoy-v3-api-msg-data-tap-v3-body) data will be present in the [as_bytes](https://www.envoyproxy.io/docs/envoy/latest/api-v3/data/tap/v3/common.proto#envoy-v3-api-field-data-tap-v3-body-as-bytes) field. This means that body data will be base64 encoded as per the [proto3 JSON mappings](https://developers.google.com/protocol-buffers/docs/proto3#json).

- JSON_BODY_AS_STRING

  ⁣Each message will be written as JSON. Any [body](https://www.envoyproxy.io/docs/envoy/latest/api-v3/data/tap/v3/common.proto#envoy-v3-api-msg-data-tap-v3-body) data will be present in the [as_string](https://www.envoyproxy.io/docs/envoy/latest/api-v3/data/tap/v3/common.proto#envoy-v3-api-field-data-tap-v3-body-as-string) field. This means that body data will be string encoded as per the [proto3 JSON mappings](https://developers.google.com/protocol-buffers/docs/proto3#json). This format type is useful when it is known that that body is human readable (e.g., JSON over HTTP) and the user wishes to view it directly without being forced to base64 decode the body.

- PROTO_BINARY

  ⁣Binary proto format. Note that binary proto is not self-delimiting. If a sink writes multiple binary messages without any length information the data stream will not be useful. However, for certain sinks that are self-delimiting (e.g., one message per file) this output format makes consumption simpler.

- PROTO_BINARY_LENGTH_DELIMITED

  ⁣Messages are written as a sequence tuples, where each tuple is the message length encoded as a [protobuf 32-bit varint](https://developers.google.com/protocol-buffers/docs/reference/cpp/google.protobuf.io.coded_stream) followed by the binary message. The messages can be read back using the language specific protobuf coded stream implementation to obtain the message length and the message.

- PROTO_TEXT

  ⁣Text proto format.

file_per_tap：

```
{
  "path_prefix": "..."
}
```

extensions.transport_sockets.**tap**.v3.Tap

```
{
  "common_config": "{...}",通用配置
  "transport_socket": "{...}"传输socket配置
}
```

 envoy.filters.http.tap

```
typed_config:
  "@type": type.googleapis.com/envoy.extensions.filters.http.tap.v3.Tap
  common_config:
    admin_config:
      config_id: test_config_id
```



# 3案例

## 3.1admin

ef-tap-httpfilter-adminl.yaml

kubectl apply -f ef-tap-httpfilter-adminl.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: tap
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
        name: envoy.filters.http.tap
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.filters.http.tap.v3.Tap
          common_config:
            admin_config:
              config_id: test_config_id
```

测试

An example POST body:

```
config_id: test_config_id
tap_config:
  match_config:
        http_request_headers_match:
          headers:
            - name: ":path"
              string_match:
                contains: "productpage"
  output_config:
    sinks:
      - streaming_admin: {}
```

![1](49images\1.png)

post body示例

```
config_id: test_config_id
tap_config:
  match_config:
    or_match:
      rules:
        - http_request_headers_match:
            headers:
              - name: foo
                string_match:
                  exact: bar
        - http_response_headers_match:
            headers:
              - name: bar
                string_match:
                  exact: baz
  output_config:
    sinks:
      - streaming_admin: {}
```



```
config_id: test_config_id
tap_config:
  match_config:
    any_match: true
  output_config:
    sinks:
      - streaming_admin: {}
```



```
config_id: test_config_id
tap_config:
  match_config:
    and_match:
      rules:
        - http_request_headers_match:
            headers:
              - name: foo
                string_match:
                  exact: bar
        - http_request_generic_body_match:
            patterns:
              - string_match: test
              - binary_match: 3q2+7w==
            bytes_limit: 128
        - http_response_generic_body_match:
            patterns:
              - binary_match: vu8=
            bytes_limit: 64
  output_config:
    sinks:
      - streaming_admin: {}
```



```
config_id: test_config_id
tap_config:
  match_config:
    any_match: true
  output_config:
    sinks:
      - format: JSON_BODY_AS_STRING
        streaming_admin: {}
```



## 3.2static_config

### 3.2.1output_config

#### 3.2.1.1streaming

ef-tap-httpfilter-static_config.yaml

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: tap
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
        name: envoy.filters.http.tap
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.filters.http.tap.v3.Tap
          common_config:
            static_config:
              match_config:
                http_request_headers_match:
                  headers:
                    - name: ":path"
                      string_match:
                        contains: "productpage"
              output_config:
                streaming: true
                sinks:
                  - format: PROTO_BINARY_LENGTH_DELIMITED
                    file_per_tap:
                      path_prefix: /var/lib/istio/data/
              
```



![2](49images\2.jpg)

#### 3.2.1.2format

##### 3.2.1.2.1JSON_BODY_AS_BYTES

ef-tap-httpfilter-static_config-format-JSON_BODY_AS_BYTES.yaml

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: tap
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
        name: envoy.filters.http.tap
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.filters.http.tap.v3.Tap
          common_config:
            static_config:
              match_config:
                http_request_headers_match:
                  headers:
                    - name: ":path"
                      string_match:
                        contains: "productpage"
              output_config:
                streaming: true
                sinks:
                  - format: JSON_BODY_AS_BYTES
                    file_per_tap:
                      path_prefix: /var/lib/istio/data/
              
```



##### 3.2.1.2.2JSON_BODY_AS_STRING

ef-tap-httpfilter-static_config-format-JSON_BODY_AS_STRING.yaml

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: tap
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
        name: envoy.filters.http.tap
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.filters.http.tap.v3.Tap
          common_config:
            static_config:
              match_config:
                http_request_headers_match:
                  headers:
                    - name: ":path"
                      string_match:
                        contains: "productpage"
              output_config:
                streaming: true
                sinks:
                  - format: JSON_BODY_AS_STRING
                    file_per_tap:
                      path_prefix: /var/lib/istio/data/
              
```



##### 3.2.1.2.3PROTO_BINARY

ef-tap-httpfilter-static_config-format-PROTO_BINARY.yaml

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: tap
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
        name: envoy.filters.http.tap
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.filters.http.tap.v3.Tap
          common_config:
            static_config:
              match_config:
                http_request_headers_match:
                  headers:
                    - name: ":path"
                      string_match:
                        contains: "productpage"
              output_config:
                streaming: true
                sinks:
                  - format: PROTO_BINARY
                    file_per_tap:
                      path_prefix: /var/lib/istio/data/
              
```



##### 3.2.1.2.4PROTO_BINARY_LENGTH_DELIMITED

ef-tap-httpfilter-static_config-format-PROTO_BINARY_LENGTH_DELIMITED.yaml

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: tap
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
        name: envoy.filters.http.tap
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.filters.http.tap.v3.Tap
          common_config:
            static_config:
              match_config:
                http_request_headers_match:
                  headers:
                    - name: ":path"
                      string_match:
                        contains: "productpage"
              output_config:
                streaming: true
                sinks:
                  - format: PROTO_BINARY_LENGTH_DELIMITED
                    file_per_tap:
                      path_prefix: /var/lib/istio/data/
              
```



##### 3.2.1.2.54PROTO_TEXT

ef-tap-httpfilter-static_config-format-PROTO_TEXT.yaml

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: tap
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
        name: envoy.filters.http.tap
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.filters.http.tap.v3.Tap
          common_config:
            static_config:
              match_config:
                http_request_headers_match:
                  headers:
                    - name: ":path"
                      string_match:
                        contains: "productpage"
              output_config:
                streaming: true
                sinks:
                  - format: PROTO_TEXT
                    file_per_tap:
                      path_prefix: /var/lib/istio/data/
              
```



#### 3.2.1.3max_buffered_rx_bytes

ef-tap-httpfilter-static_config-max_buffered_rx_bytes.yaml

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: tap
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
        name: envoy.filters.http.tap
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.filters.http.tap.v3.Tap
          common_config:
            static_config:
              match_config:
                http_request_headers_match:
                  headers:
                    - name: ":path"
                      string_match:
                        contains: "productpage"
              output_config:
                max_buffered_rx_bytes: 2048
                streaming: true
                sinks:
                  - format: PROTO_TEXT
                    file_per_tap:
                      path_prefix: /var/lib/istio/data/
              
```



#### 3.2.1.4max_buffered_tx_bytes

ef-tap-httpfilter-static_config-max_buffered_tx_bytes.yaml

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: tap
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
        name: envoy.filters.http.tap
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.filters.http.tap.v3.Tap
          common_config:
            static_config:
              match_config:
                http_request_headers_match:
                  headers:
                    - name: ":path"
                      string_match:
                        contains: "productpage"
              output_config:
                max_buffered_tx_bytes: 2048
                streaming: true
                sinks:
                  - format: PROTO_TEXT
                    file_per_tap:
                      path_prefix: /var/lib/istio/data/
              
```



### 3.2.2match_config

```
{
  "or_match": "{...}",或匹配
  "and_match": "{...}",与匹配
  "not_match": "{...}",非匹配
  "any_match": "...",任意匹配
  "http_request_headers_match": "{...}",http请求头匹配
  "http_request_trailers_match": "{...}",http请求trailer匹配
  "http_response_headers_match": "{...}",响应头匹配
  "http_response_trailers_match": "{...}",响应trailer匹配
  "http_request_generic_body_match": "{...}",请求体匹配
  "http_response_generic_body_match": "{...}"响应体匹配
}
```

#### 3.2.2.1or_match

ef-tap-httpfilter-static_config-or_match.yaml

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: tap
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
        name: envoy.filters.http.tap
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.filters.http.tap.v3.Tap
          common_config:
            static_config:
              match_config:
                or_match:
                  rules: 
                  - http_request_headers_match:
                      headers:
                        - name: ":path"
                          string_match:
                            contains: "productpage"
                  - http_request_headers_match:
                      headers:
                        - name: ":method"
                          string_match:
                            contains: "GET"
              output_config:
                max_buffered_tx_bytes: 2048
                streaming: true
                sinks:
                  - format: PROTO_TEXT
                    file_per_tap:
                      path_prefix: /var/lib/istio/data/
              
```



#### 3.2.2.2and_match

ef-tap-httpfilter-static_config-and_match.yaml

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: tap
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
        name: envoy.filters.http.tap
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.filters.http.tap.v3.Tap
          common_config:
            static_config:
              match_config:
                and_match:
                  rules: 
                  - http_request_headers_match:
                      headers:
                        - name: ":path"
                          string_match:
                            contains: "productpage"
                  - http_request_headers_match:
                      headers:
                        - name: ":method"
                          string_match:
                            contains: "GET"
              output_config:
                max_buffered_tx_bytes: 2048
                streaming: true
                sinks:
                  - format: PROTO_TEXT
                    file_per_tap:
                      path_prefix: /var/lib/istio/data/
              
```



#### 3.2.2.3not_match

ef-tap-httpfilter-static_config-not_match.yaml

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: tap
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
        name: envoy.filters.http.tap
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.filters.http.tap.v3.Tap
          common_config:
            static_config:
              match_config:
                not_match:
                  http_request_headers_match:
                    headers:
                      - name: ":path"
                        string_match:
                          contains: "test"
              output_config:
                max_buffered_tx_bytes: 2048
                streaming: true
                sinks:
                  - format: PROTO_TEXT
                    file_per_tap:
                      path_prefix: /var/lib/istio/data/
              
```



#### 3.2.3.4any_match

ef-tap-httpfilter-static_config-any_match.yaml

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: tap
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
        name: envoy.filters.http.tap
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.filters.http.tap.v3.Tap
          common_config:
            static_config:
              match_config:
                any_match: true
              output_config:
                max_buffered_tx_bytes: 2048
                streaming: true
                sinks:
                  - format: PROTO_TEXT
                    file_per_tap:
                      path_prefix: /var/lib/istio/data/
              
```



#### 3.2.3.5http_request_headers_match

ef-tap-httpfilter-static_config-http_request_headers_match.yaml

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: tap
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
        name: envoy.filters.http.tap
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.filters.http.tap.v3.Tap
          common_config:
            static_config:
              match_config:
                http_request_headers_match:
                  headers:
                    - name: ":path"
                      string_match:
                        contains: "productpage"
              output_config:
                max_buffered_tx_bytes: 2048
                streaming: true
                sinks:
                  - format: PROTO_TEXT
                    file_per_tap:
                      path_prefix: /var/lib/istio/data/
              
```



#### 3.2.3.6http_request_trailers_match

启用trailer         

ef-tap-httpfilter-static_config-http_request_trailers_match.yaml

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: tap
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
        name: envoy.filters.http.tap
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.filters.http.tap.v3.Tap
          common_config:
            static_config:
              match_config:
                http_request_trailers_match:
                  headers:
                    - name: "request_end"
                      string_match:
                        contains: "test"
              output_config:
                max_buffered_tx_bytes: 2048
                streaming: true
                sinks:
                  - format: PROTO_TEXT
                    file_per_tap:
                      path_prefix: /var/lib/istio/data/
  - applyTo: NETWORK_FILTER
    match:
      context: GATEWAY
      listener:
        portNumber: 8080
        filterChain:
          filter:
            name: "envoy.filters.network.http_connection_manager"
    patch:
      operation: MERGE
      value:
        name: envoy.filters.network.http_connection_manager
        typedConfig:
          '@type': type.googleapis.com/envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager
          http_protocol_options:
            enable_trailers: true            
```



#### 3.2.2.7http_response_headers_match

ef-tap-httpfilter-static_config-http_response_headers_match.yaml

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: tap
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
        name: envoy.filters.http.tap
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.filters.http.tap.v3.Tap
          common_config:
            static_config:
              match_config:
                http_response_headers_match:
                  headers:
                    - name: "server"
                      string_match:
                        contains: "envoy"
              output_config:
                max_buffered_tx_bytes: 2048
                streaming: true
                sinks:
                  - format: PROTO_TEXT
                    file_per_tap:
                      path_prefix: /var/lib/istio/data/           
```



#### 3.2.2.8http_response_trailers_match

ef-tap-httpfilter-static_config-http_response_trailers_match.yaml

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: tap
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
        name: envoy.filters.http.tap
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.filters.http.tap.v3.Tap
          common_config:
            static_config:
              match_config:
                http_response_trailers_match:
                  headers:
                    - name: "Expiress"
                      safe_regex_match:
                        google_re2: {}
                        regex: ".*"
              output_config:
                max_buffered_tx_bytes: 2048
                streaming: true
                sinks:
                  - format: PROTO_TEXT
                    file_per_tap:
                      path_prefix: /var/lib/istio/data/
  - applyTo: NETWORK_FILTER
    match:
      context: GATEWAY
      listener:
        portNumber: 8080
        filterChain:
          filter:
            name: "envoy.filters.network.http_connection_manager"
    patch:
      operation: MERGE
      value:
        name: envoy.filters.network.http_connection_manager
        typedConfig:
          '@type': type.googleapis.com/envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager
          http_protocol_options:
            enable_trailers: true            
```



#### 3.2.2.9http_request_generic_body_match

ef-tap-httpfilter-static_config-http_request_generic_body_match.yaml

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: tap
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
        name: envoy.filters.http.tap
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.filters.http.tap.v3.Tap
          common_config:
            static_config:
              match_config:
                http_request_generic_body_match:
                  patterns:
                    - string_match: test
                    - binary_match: 3q2+7w==
                  bytes_limit: 128
              output_config:
                max_buffered_tx_bytes: 2048
                streaming: true
                sinks:
                  - format: PROTO_TEXT
                    file_per_tap:
                      path_prefix: /var/lib/istio/data/
              
```



#### 3.2.2.10http_response_generic_body_match

ef-tap-httpfilter-static_config-http_response_generic_body_match.yaml

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: tap
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
        name: envoy.filters.http.tap
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.filters.http.tap.v3.Tap
          common_config:
            static_config:
              match_config:
                http_response_generic_body_match:
                  patterns:
                    - binary_match: vu8=
                  bytes_limit: 128
              output_config:
                max_buffered_tx_bytes: 2048
                streaming: true
                sinks:
                  - format: PROTO_TEXT
                    file_per_tap:
                      path_prefix: /var/lib/istio/data/
              
```



### 3.2.3match

ef-tap-httpfilter-static_config-match.yaml

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: tap
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
        name: envoy.filters.http.tap
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.filters.http.tap.v3.Tap
          common_config:
            static_config:
              match:
                http_request_headers_match:
                  headers:
                    - name: ":path"
                      string_match:
                        contains: "productpage"
              output_config:
                max_buffered_tx_bytes: 2048
                streaming: true
                sinks:
                  - format: PROTO_TEXT
                    file_per_tap:
                      path_prefix: /var/lib/istio/data/
              
```



## 3.3transport_socket

ef-tap-transport_socket.yaml

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: tap
spec:
  workloadSelector:
    labels:
      istio: ingressgateway
  configPatches:
  - applyTo: NETWORK_FILTER
    match:
      context: GATEWAY
      listener:
        portNumber: 8080
        filterChain:
          filter:
            name: "envoy.filters.network.http_connection_manager"
    patch:
      operation: MERGE
      value:
              name: envoy.filters.network.http_connection_manager
              typedConfig:
                '@type': type.googleapis.com/envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager
                route_config:
                  name: test
                  virtual_hosts:
                  - name: test
                    domains:
                    - "*"
                    routes:
                    - name: testroute
                      match: 
                        prefix: /
                      route:
                        cluster: productpage
  - applyTo: CLUSTER
    patch:
      operation: ADD
      value:
            name: productpage
            connect_timeout: 30s
            type: LOGICAL_DNS
            dns_lookup_family: V4_ONLY
            lb_policy: ROUND_ROBIN
            load_assignment:
              cluster_name: productpage
              endpoints:
              - lb_endpoints:
                - endpoint:
                    address:
                      socket_address:
                        address: productpage.istio
                        port_value: 9080
            transport_socket:
              name: envoy.transport_sockets.tap
              typed_config:
                "@type": type.googleapis.com/envoy.extensions.transport_sockets.tap.v3.Tap
                common_config:
                    static_config:
                      match_config:
                        http_request_headers_match:
                          headers:
                            - name: ":path"
                              string_match:
                                contains: "productpage"
                      output_config:
                        max_buffered_tx_bytes: 2048
                        streaming: true
                        sinks:
                          - format: PROTO_TEXT
                            file_per_tap:
                              path_prefix: /var/lib/istio/data/
                transport_socket:
                  name: envoy.transport_sockets.raw_buffer
                
```







