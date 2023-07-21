# 1什么是transport-socket

transport-socket是传输套接字配置，总体可以分为两类，分别上下游配置和上游配置。

# 2有哪些类型

## 2.1按方向分

- envoy.transport_sockets.downstream下游配置
- envoy.transport_sockets.upstream上游配置

## 2.2按功能分

### 2.2.1下游配置

- envoy.transport_sockets.alts   ALTS协议
- envoy.transport_sockets.raw_buffer 原生协议
- envoy.transport_sockets.starttls  文本到tls转换
- envoy.transport_sockets.tap  包装配置，用于窃取内容
- envoy.transport_sockets.tcp_stats  包装配置用于产生tcp 监控数据
- envoy.transport_sockets.tls  tls协议
- envoy.transport_sockets.quic http3协议

### 2.2.2上游配置

- envoy.transport_sockets.alts  ALTS协议
- envoy.transport_sockets.raw_buffer 原生协议
- envoy.transport_sockets.starttls  文本到tls转换
- envoy.transport_sockets.tap  包装配置，用于窃取内容
- envoy.transport_sockets.tcp_stats 包装配置用于产生tcp 监控数据
- envoy.transport_sockets.tls tls协议
- envoy.transport_sockets.upstream_proxy_protocol  PROXY协议
- envoy.transport_sockets.quic http3协议

# 3配置

## 3.1envoy.transport_sockets.alts

```
{
  "handshaker_service": "...",握手服务地址
  "peer_service_accounts": []对端可接受的服务账号
}
```

## 3.2envoy.transport_sockets.raw_buffer

没有配置

## 3.3envoy.transport_sockets.starttls

### 3.3.1下游

```
{
  "cleartext_socket_config": "{...}",纯文本socket配置
  "tls_socket_config": "{...}"tls套接字配置
}
```

cleartext_socket_config：

空

tls_socket_config：

```
{
  "common_tls_context": "{...}",tls上下文通用配置
  "require_client_certificate": "{...}",是否需要客户端证书
  "session_ticket_keys": "{...}",tls会话ticket键
  "session_ticket_keys_sds_secret_config": "{...}",tls会话ticket键，sds密钥配置
  "disable_stateless_session_resumption": "...",是否警用无状态会话恢复
  "session_timeout": "{...}",会话超时时间
  "ocsp_staple_policy": "..."没有伴随OCSP 响应是否使用证书
}
```

common_tls_context：

```
{
  "tls_params": "{...}",tls参数
  "tls_certificates": [],tls证书
  "tls_certificate_sds_secret_configs": [],tls证书发现服务配置
  "validation_context": "{...}",校验对端证书配置
  "validation_context_sds_secret_config": "{...}",校验对端证书服务发现配置
  "combined_validation_context": "{...}",组合校验对端证书配置
  "alpn_protocols": [],alpn协议
  "custom_handshaker": "{...}",自定义握手
  "key_log": "{...}"tls key日志配置
}
```

session_ticket_keys：

```
{
  "keys": []键来源
}
```

keys：

```
{
  "filename": "...",文件名
  "inline_bytes": "...",内嵌字节
  "inline_string": "...",内嵌字符串
  "environment_variable": "..."环境变量
}
```

session_ticket_keys_sds_secret_config：

```
{
  "name": "...",名称
  "sds_config": "{...}"证书发现配置
}
```

sds_config：

```
{
  "path": "...",路径
  "path_config_source": "{...}",基于路径配置
  "api_config_source": "{...}",基于api配置
  "ads": "{...}",ads配置
  "initial_fetch_timeout": "{...}",初始抓取超时时间
  "resource_api_version": "..."资源版本
}
```

ocsp_staple_policy：

- LENIENT_STAPLING

  *(DEFAULT)* ⁣OCSP responses are optional. If an OCSP response is absent or expired, the associated certificate will be used for connections without an OCSP staple.

- STRICT_STAPLING

  ⁣OCSP responses are optional. If an OCSP response is absent, the associated certificate will be used without an OCSP staple. If a response is provided but is expired, the associated certificate will not be used for subsequent connections. If no suitable certificate is found, the connection is rejected.

- MUST_STAPLE

  ⁣OCSP responses are required. Configuration will fail if a certificate is provided without an OCSP response. If a response expires, the associated certificate will not be used connections. If no suitable certificate is found, the connection is rejected.

### 3.3.2上游

```
{
  "cleartext_socket_config": "{...}",纯文本socket配置
  "tls_socket_config": "{...}"tls套接字配置
}
```

tls_socket_config：

```
{
  "common_tls_context": "{...}",tls上下文通用配置
  "sni": "...",sni名称
  "allow_renegotiation": "...",是否启用服务端会话谈判
  "max_session_keys": "{...}"会话键的最大数量
}
```

common_tls_context：

```
{
  "tls_params": "{...}",tls参数
  "tls_certificates": [],tls证书
  "tls_certificate_sds_secret_configs": [],tls证书发现服务配置
  "validation_context": "{...}",校验对端证书配置
  "validation_context_sds_secret_config": "{...}",校验对端证书服务发现配置
  "combined_validation_context": "{...}",组合校验对端证书配置
  "alpn_protocols": [],alpn协议
  "custom_handshaker": "{...}",自定义握手
  "key_log": "{...}"tls key日志配置
}
```



## 3.4envoy.transport_sockets.tap

```
{
  "common_config": "{...}",tap配置
  "transport_socket": "{...}"传输层套接字配置
}
```

common_config：

```
{
  "admin_config": "{...}",管理接口配置
  "static_config": "{...}"静态配置
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
  "match": "{...}",匹配配置
  "output_config": "{...}"输出配置
}
```

output_config：

```
{
  "sinks": [],输出槽
  "max_buffered_rx_bytes": "{...}",最大接收缓存字节
  "max_buffered_tx_bytes": "{...}",最大发送缓存字节
  "streaming": "..."是否是单个流
}
```

## 3.5envoy.transport_sockets.tcp_stats

```
{
  "transport_socket": "{...}",传输层套接字配置
  "update_period": "{...}"更新周期
}
```

## 3.6envoy.transport_sockets.tls

### 3.6.1上游

```
{
  "common_tls_context": "{...}",tls上下文通用配置
  "sni": "...",sni名称
  "allow_renegotiation": "...",是否启用服务端会话谈判
  "max_session_keys": "{...}"会话键的最大数量
}
```

common_tls_context：

```
{
  "tls_params": "{...}",tls参数
  "tls_certificates": [],tls证书
  "tls_certificate_sds_secret_configs": [],tls证书发现服务配置
  "validation_context": "{...}",校验对端证书配置
  "validation_context_sds_secret_config": "{...}",校验对端证书服务发现配置
  "combined_validation_context": "{...}",组合校验对端证书配置
  "alpn_protocols": [],alpn协议
  "custom_handshaker": "{...}",自定义握手
  "key_log": "{...}"tls key日志配置
}
```

### 3.6.2下游

```
{
  "common_tls_context": "{...}",tls上下文通用配置
  "require_client_certificate": "{...}",是否需要客户端证书
  "session_ticket_keys": "{...}",tls会话ticket键
  "session_ticket_keys_sds_secret_config": "{...}",tls会话ticket键，sds密钥配置
  "disable_stateless_session_resumption": "...",是否警用无状态会话恢复
  "session_timeout": "{...}",会话超时时间
  "ocsp_staple_policy": "..."没有伴随OCSP 响应是否使用证书
}
```

## 3.7envoy.transport_sockets.upstream_proxy_protocol

```
{
  "config": "{...}",配置
  "transport_socket": "{...}"传输层套接字配置
}
```

config：

```
{
  "version": "..."版本
}
```

version：

- V1

  *(DEFAULT)* ⁣PROXY protocol version 1. Human readable format.

- V2

  ⁣PROXY protocol version 2. Binary format.

## 3.8envoy.transport_sockets.quic

### 3.8.1上游

```
{
  "upstream_tls_context": "{...}"上游tls配置
}
```

### 3.8.2下游

```
{
  "downstream_tls_context": "{...}",下游tls配置
  "enable_early_data": "{...}"是否启用早期
}
```

# 4配置点

1cluster

transport_socket_matches

2cluster

transport_socket

3filter_chains



# 5案例

## 5.1cluster transport_socket_matches

### 5.1.2upstream_proxy_protocol

ef-add-filter-chain.yaml

kubectl apply -f ef-add-filter-chain.yaml -n istio

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: add-filter-chain
spec:
  workloadSelector:
    labels:
      app: productpage
  configPatches:
  - applyTo: FILTER_CHAIN
    match:
      listener:
        name: virtualInbound
    patch:
      operation: ADD
      value:
        filter_chain_match:
          destination_port: 9080
          transport_protocol: tls
          application_protocols:
          - "http/1.1"
          - "h2c"
        filters:
        - name: istio.metadata_exchange
          typed_config:
            "@type": type.googleapis.com/envoy.tcp.metadataexchange.config.MetadataExchange
            protocol: istio-peer-exchange
        - name: envoy.filters.network.http_connection_manager
          typed_config:
            "@type": type.googleapis.com/envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager
            stat_prefix: inbound_0.0.0.0_9080
            route_config:
              name: inbound|9080||
              virtual_hosts:
              - name: inbound|http|9080
                domains:
                - "*"
                routes:
                - match:
                    prefix: "/"
                  route:
                    cluster: inbound|9080||
                    timeout: 0s
                    max_stream_duration:
                      max_stream_duration: 0s
                      grpc_timeout_header_max: 0s
                  decorator:
                    operation: productpage.istio.svc.cluster.local:9080/*
                  name: default
              validate_clusters: false
            http_filters:
            - name: istio.metadata_exchange
              typed_config:
                "@type": type.googleapis.com/envoy.extensions.filters.http.wasm.v3.Wasm
                config:
                  vm_config:
                    runtime: envoy.wasm.runtime.null
                    code:
                      local:
                        inline_string: envoy.wasm.metadata_exchange
                  configuration:
                    "@type": type.googleapis.com/envoy.tcp.metadataexchange.config.MetadataExchange
            - name: envoy.filters.http.fault
              typed_config:
                "@type": type.googleapis.com/envoy.extensions.filters.http.fault.v3.HTTPFault
            - name: envoy.filters.http.cors
              typed_config:
                "@type": type.googleapis.com/envoy.extensions.filters.http.cors.v3.Cors
            - name: istio.stats
              typed_config:
                "@type": type.googleapis.com/udpa.type.v1.TypedStruct
                type_url: type.googleapis.com/envoy.extensions.filters.http.wasm.v3.Wasm
                value:
                  config:
                    root_id: stats_inbound
                    vm_config:
                      vm_id: stats_inbound
                      runtime: envoy.wasm.runtime.null
                      code:
                        local:
                          inline_string: envoy.wasm.stats
                    configuration:
                      "@type": type.googleapis.com/google.protobuf.StringValue
                      value: |
                        {
                         "debug": "false",
                         "stat_prefix": "istio",
                         "disable_host_header_fallback": true,
                         "metrics": [
                         {
                         "dimensions": {
                         "destination_cluster": "node.metadata['CLUSTER_ID']",
                         "source_cluster": "downstream_peer.cluster_id"
                         }
                         }
                         ]
                        }
            - name: envoy.filters.http.router
              typed_config:
                "@type": type.googleapis.com/envoy.extensions.filters.http.router.v3.Router
            tracing:
              client_sampling:
                value: 100
              random_sampling:
                value: 100
              overall_sampling:
                value: 100
              custom_tags:
              - tag: istio.authorization.dry_run.allow_policy.name
                metadata:
                  kind:
                    request: {}
                  metadata_key:
                    key: envoy.filters.http.rbac
                    path:
                    - key: istio_dry_run_allow_shadow_effective_policy_id
              - tag: istio.authorization.dry_run.allow_policy.result
                metadata:
                  kind:
                    request: {}
                  metadata_key:
                    key: envoy.filters.http.rbac
                    path:
                    - key: istio_dry_run_allow_shadow_engine_result
              - tag: istio.authorization.dry_run.deny_policy.name
                metadata:
                  kind:
                    request: {}
                  metadata_key:
                    key: envoy.filters.http.rbac
                    path:
                    - key: istio_dry_run_deny_shadow_effective_policy_id
              - tag: istio.authorization.dry_run.deny_policy.result
                metadata:
                  kind:
                    request: {}
                  metadata_key:
                    key: envoy.filters.http.rbac
                    path:
                    - key: istio_dry_run_deny_shadow_engine_result
              - tag: istio.canonical_revision
                literal:
                  value: v1
              - tag: istio.canonical_service
                literal:
                  value: productpage
              - tag: istio.mesh_id
                literal:
                  value: cluster.local
              - tag: istio.namespace
                literal:
                  value: istio
            server_name: istio-envoy
            access_log:
            - name: envoy.access_loggers.file
              typed_config:
                "@type": type.googleapis.com/envoy.extensions.access_loggers.file.v3.FileAccessLog
                path: "/dev/stdout"
                log_format:
                  text_format_source:
                    inline_string: '[%START_TIME%] "%REQ(:METHOD)% %REQ(X-ENVOY-ORIGINAL-PATH?:PATH)%
                      %PROTOCOL%" %RESPONSE_CODE% %RESPONSE_FLAGS% %RESPONSE_CODE_DETAILS%
                      %CONNECTION_TERMINATION_DETAILS% "%UPSTREAM_TRANSPORT_FAILURE_REASON%"
                      %BYTES_RECEIVED% %BYTES_SENT% %DURATION% %RESP(X-ENVOY-UPSTREAM-SERVICE-TIME)%
                      "%REQ(X-FORWARDED-FOR)%" "%REQ(USER-AGENT)%" "%REQ(X-REQUEST-ID)%" "%REQ(:AUTHORITY)%"
                      "%UPSTREAM_HOST%" %UPSTREAM_CLUSTER% %UPSTREAM_LOCAL_ADDRESS% %DOWNSTREAM_LOCAL_ADDRESS%
                      %DOWNSTREAM_REMOTE_ADDRESS% %REQUESTED_SERVER_NAME% %ROUTE_NAME%'
            use_remote_address: false
            forward_client_cert_details: APPEND_FORWARD
            set_current_client_cert_details:
              subject: true
              dns: true
              uri: true
            upgrade_configs:
            - upgrade_type: websocket
            stream_idle_timeout: 0s
            normalize_path: true
            request_id_extension:
              typed_config:
                "@type": type.googleapis.com/envoy.extensions.request_id.uuid.v3.UuidRequestIdConfig
                use_request_id_for_trace_sampling: true
            path_with_escaped_slashes_action: KEEP_UNCHANGED
        transport_socket:
          name: envoy.transport_sockets.tls
          typed_config:
            "@type": type.googleapis.com/envoy.extensions.transport_sockets.tls.v3.DownstreamTlsContext
            common_tls_context:
              tls_params:
                tls_minimum_protocol_version: TLSv1_2
                tls_maximum_protocol_version: TLSv1_3
                cipher_suites:
                - ECDHE-ECDSA-AES256-GCM-SHA384
                - ECDHE-RSA-AES256-GCM-SHA384
                - ECDHE-ECDSA-AES128-GCM-SHA256
                - ECDHE-RSA-AES128-GCM-SHA256
                - AES256-GCM-SHA384
                - AES128-GCM-SHA256
              alpn_protocols:
              - h2
              - http/1.1
              tls_certificate_sds_secret_configs:
              - name: default
                sds_config:
                  api_config_source:
                    api_type: GRPC
                    grpc_services:
                    - envoy_grpc:
                        cluster_name: sds-grpc
                    set_node_on_first_message_only: true
                    transport_api_version: V3
                  initial_fetch_timeout: 0s
                  resource_api_version: V3
              combined_validation_context:
                default_validation_context:
                  match_subject_alt_names:
                  - prefix: spiffe://cluster.local/
                validation_context_sds_secret_config:
                  name: ROOTCA
                  sds_config:
                    api_config_source:
                      api_type: GRPC
                      grpc_services:
                      - envoy_grpc:
                          cluster_name: sds-grpc
                      set_node_on_first_message_only: true
                      transport_api_version: V3
                    initial_fetch_timeout: 0s
                    resource_api_version: V3
            require_client_certificate: true
        name: 0.0.0.0_9080

```



ef-proxy_protocol.yaml

kubectl apply -f ef-proxy_protocol.yaml -n istio

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: proxy-protocol
spec:
  workloadSelector:
    labels:
      app: productpage
  configPatches:
  - applyTo: LISTENER
    match:
      context: SIDECAR_INBOUND
      listener:
        name: virtualInbound
    patch:
      operation: MERGE
      value:
        listener_filters:
        - name: envoy.filters.listener.proxy_protocol
          typed_config:
            "@type": type.googleapis.com/envoy.extensions.filters.listener.proxy_protocol.v3.ProxyProtocol
              
```



ef-cluster-transport_socket_matches-upstream_proxy_protocol.yaml

kubectl apply -f ef-cluster-transport_socket_matches-upstream_proxy_protocol.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: transport-socket
  namespace: istio-system 
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
                  cluster: my-productpage
  - applyTo: CLUSTER
    patch:
      operation: ADD     
      value:
          name: my-productpage
          type: STRICT_DNS
          connect_timeout: 10s
          lb_policy: ROUND_ROBIN
          load_assignment:
            cluster_name: my-productpage
            endpoints:
            - lb_endpoints:
              - endpoint:
                  address:
                     socket_address:
                      address: productpage.istio.svc.cluster.local
                      port_value: 9080
          transportSocketMatches:
          - match: {}
            name: tlsMode-upstream_proxy_protocol
            transportSocket:
              name: envoy.transport_sockets.upstream_proxy_protocol
              typed_config:
                "@type": type.googleapis.com/envoy.extensions.transport_sockets.proxy_protocol.v3.ProxyProtocolUpstreamTransport
                config: 
                  version: V1
                transport_socket:
                  name: envoy.transport_sockets.tls
                  typed_config:
                    "@type": type.googleapis.com/envoy.extensions.transport_sockets.tls.v3.UpstreamTlsContext
                    common_tls_context:
                      combinedValidationContext:
                        defaultValidationContext:
                          matchSubjectAltNames:
                          - exact: spiffe://cluster.local/ns/istio/sa/bookinfo-productpage
                        validationContextSdsSecretConfig:
                          name: ROOTCA
                          sdsConfig:
                            apiConfigSource:
                              apiType: GRPC
                              grpcServices:
                              - envoyGrpc:
                                  clusterName: sds-grpc
                              setNodeOnFirstMessageOnly: true
                              transportApiVersion: V3
                            initialFetchTimeout: 0s
                            resourceApiVersion: V3
                      tlsCertificateSdsSecretConfigs:
                      - name: default
                        sdsConfig:
                          apiConfigSource:
                            apiType: GRPC
                            grpcServices:
                            - envoyGrpc:
                                clusterName: sds-grpc
                            setNodeOnFirstMessageOnly: true
                            transportApiVersion: V3
                          initialFetchTimeout: 0s
                          resourceApiVersion: V3
                    sni: outbound_.9080_._.productpage.istio.svc.cluster.local
```

ef-cluster-transport_socket_matches-upstream_proxy_protocol-rawbuffer.yaml

kubectl apply -f ef-cluster-transport_socket_matches-upstream_proxy_protocol-rawbuffer.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: transport-socket
  namespace: istio-system 
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
                  cluster: my-productpage
  - applyTo: CLUSTER
    patch:
      operation: ADD     
      value:
          name: my-productpage
          type: STRICT_DNS
          connect_timeout: 10s
          lb_policy: ROUND_ROBIN
          load_assignment:
            cluster_name: my-productpage
            endpoints:
            - lb_endpoints:
              - endpoint:
                  address:
                     socket_address:
                      address: productpage.istio.svc.cluster.local
                      port_value: 9080
          transportSocketMatches:
          - match: {}
            name: upstream_proxy_protocol
            transportSocket:
              name: envoy.transport_sockets.upstream_proxy_protocol
              typed_config:
                "@type": type.googleapis.com/envoy.extensions.transport_sockets.proxy_protocol.v3.ProxyProtocolUpstreamTransport
                config: 
                  version: V1
                transport_socket:
                  name: envoy.transport_sockets.raw_buffer
                  typed_config:
                    "@type": type.googleapis.com/envoy.extensions.transport_sockets.raw_buffer.v3.RawBuffer
```



### 5.1.3starttls upstream

ef-cluster-transport_socket_matches-starttls-upstream.yaml

kubectl apply -f ef-cluster-transport_socket_matches-starttls-upstream.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: transport-socket
  namespace: istio-system 
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
                  cluster: my-productpage
  - applyTo: CLUSTER
    patch:
      operation: ADD     
      value:
          name: my-productpage
          type: STRICT_DNS
          connect_timeout: 10s
          lb_policy: ROUND_ROBIN
          load_assignment:
            cluster_name: my-productpage
            endpoints:
            - lb_endpoints:
              - endpoint:
                  address:
                     socket_address:
                      address: productpage.istio.svc.cluster.local
                      port_value: 9080
          transportSocketMatches:
          - match: {}
            name: tlsMode-startTls
            transportSocket:
              name: envoy.transport_sockets.starttls
              typed_config:
                "@type": type.googleapis.com/envoy.extensions.transport_sockets.starttls.v3.UpstreamStartTlsConfig
                cleartext_socket_config: {}
                tls_socket_config:
                    common_tls_context:
                      combinedValidationContext:
                        defaultValidationContext:
                          matchSubjectAltNames:
                          - exact: spiffe://cluster.local/ns/istio/sa/bookinfo-productpage
                        validationContextSdsSecretConfig:
                          name: ROOTCA
                          sdsConfig:
                            apiConfigSource:
                              apiType: GRPC
                              grpcServices:
                              - envoyGrpc:
                                  clusterName: sds-grpc
                              setNodeOnFirstMessageOnly: true
                              transportApiVersion: V3
                            initialFetchTimeout: 0s
                            resourceApiVersion: V3
                      tlsCertificateSdsSecretConfigs:
                      - name: default
                        sdsConfig:
                          apiConfigSource:
                            apiType: GRPC
                            grpcServices:
                            - envoyGrpc:
                                clusterName: sds-grpc
                            setNodeOnFirstMessageOnly: true
                            transportApiVersion: V3
                          initialFetchTimeout: 0s
                          resourceApiVersion: V3
                    sni: outbound_.9080_._.productpage.istio.svc.cluster.local
```



## 5.2cluster transport_socket

### 5.2.1raw_buffer

ef-cluster-transport_socket-raw_buffer.yaml

kubectl apply -f ef-cluster-transport_socket-raw_buffer.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: transport-socket
  namespace: istio-system 
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
                  cluster: my-productpage
  - applyTo: CLUSTER
    patch:
      operation: ADD     
      value:
          name: my-productpage
          type: STRICT_DNS
          connect_timeout: 10s
          lb_policy: ROUND_ROBIN
          load_assignment:
            cluster_name: my-productpage
            endpoints:
            - lb_endpoints:
              - endpoint:
                  address:
                     socket_address:
                      address: productpage.istio.svc.cluster.local
                      port_value: 9080
          transport_socket:
            name: envoy.transport_sockets.raw_buffer
            typed_config:
              "@type": type.googleapis.com/envoy.extensions.transport_sockets.raw_buffer.v3.RawBuffer
            
```

### 5.2.2tcp_stats

istio还不支持

2022-07-16T03:24:46.258836Z     warn    ads     ADS:CDS: ACK ERROR istio-ingressgateway-7845746bf7-xpg7k.istio-system-287 Internal:Error adding/updating cluster(s) my-productpage: Didn't find a registered implementation for name: 'envoy.transport_sockets.tcp_stats



ef-cluster-transport_socket-tcp_stats.yaml

kubectl apply -f ef-cluster-transport_socket-tcp_stats.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: transport-socket
  namespace: istio-system 
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
                  cluster: my-productpage
  - applyTo: CLUSTER
    patch:
      operation: ADD     
      value:
          name: my-productpage
          type: STRICT_DNS
          connect_timeout: 10s
          lb_policy: ROUND_ROBIN
          load_assignment:
            cluster_name: my-productpage
            endpoints:
            - lb_endpoints:
              - endpoint:
                  address:
                     socket_address:
                      address: productpage.istio.svc.cluster.local
                      port_value: 9080
          transport_socket:
            name: envoy.transport_sockets.tcp_stats
            typed_config:
                "@type": type.googleapis.com/envoy.extensions.transport_sockets.tcp_stats.v3.Config
                update_period: 30s
                transport_socket:
                  name: envoy.transport_sockets.tls
                  typed_config:
                    "@type": type.googleapis.com/envoy.extensions.transport_sockets.tls.v3.UpstreamTlsContext
                    common_tls_context:
                      combinedValidationContext:
                        defaultValidationContext:
                          matchSubjectAltNames:
                          - exact: spiffe://cluster.local/ns/istio/sa/bookinfo-productpage
                        validationContextSdsSecretConfig:
                          name: ROOTCA
                          sdsConfig:
                            apiConfigSource:
                              apiType: GRPC
                              grpcServices:
                              - envoyGrpc:
                                  clusterName: sds-grpc
                              setNodeOnFirstMessageOnly: true
                              transportApiVersion: V3
                            initialFetchTimeout: 0s
                            resourceApiVersion: V3
                      tlsCertificateSdsSecretConfigs:
                      - name: default
                        sdsConfig:
                          apiConfigSource:
                            apiType: GRPC
                            grpcServices:
                            - envoyGrpc:
                                clusterName: sds-grpc
                            setNodeOnFirstMessageOnly: true
                            transportApiVersion: V3
                          initialFetchTimeout: 0s
                          resourceApiVersion: V3
                    sni: outbound_.9080_._.productpage.istio.svc.cluster.local
            
```

### 5.2.3tls upstream

ef-cluster-transport_socket-tls_upstream.yaml

kubectl apply -f ef-cluster-transport_socket-tls_upstream.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: transport-socket
  namespace: istio-system 
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
                  cluster: my-productpage
  - applyTo: CLUSTER
    patch:
      operation: ADD     
      value:
          name: my-productpage
          type: STRICT_DNS
          connect_timeout: 10s
          lb_policy: ROUND_ROBIN
          load_assignment:
            cluster_name: my-productpage
            endpoints:
            - lb_endpoints:
              - endpoint:
                  address:
                     socket_address:
                      address: productpage.istio.svc.cluster.local
                      port_value: 9080
          transport_socket:
                  name: envoy.transport_sockets.tls
                  typed_config:
                    "@type": type.googleapis.com/envoy.extensions.transport_sockets.tls.v3.UpstreamTlsContext
                    common_tls_context:
                      combinedValidationContext:
                        defaultValidationContext:
                          matchSubjectAltNames:
                          - exact: spiffe://cluster.local/ns/istio/sa/bookinfo-productpage
                          allow_expired_certificate: true
                        validationContextSdsSecretConfig:
                          name: ROOTCA
                          sdsConfig:
                            apiConfigSource:
                              apiType: GRPC
                              grpcServices:
                              - envoyGrpc:
                                  clusterName: sds-grpc
                              setNodeOnFirstMessageOnly: true
                              transportApiVersion: V3
                            initialFetchTimeout: 0s
                            resourceApiVersion: V3
                      tlsCertificateSdsSecretConfigs:
                      - name: default
                        sdsConfig:
                          apiConfigSource:
                            apiType: GRPC
                            grpcServices:
                            - envoyGrpc:
                                clusterName: sds-grpc
                            setNodeOnFirstMessageOnly: true
                            transportApiVersion: V3
                          initialFetchTimeout: 0s
                          resourceApiVersion: V3
                    sni: outbound_.9080_._.productpage.istio.svc.cluster.local
            
```

### 5.2.5quic upstream

ef-cluster-transport_socket-quic_upstream.yaml

kubectl apply -f ef-cluster-transport_socket-quic_upstream.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: transport-socket
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
                      host_rewrite_literal: firebase.google.cn
                      cluster: service_google
  - applyTo: CLUSTER
    patch:
        operation: ADD
        value:
            name: service_google
            connect_timeout: 30s
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
                        address: firebase.google.cn
                        port_value: 443
            typed_extension_protocol_options:
              envoy.extensions.upstreams.http.v3.HttpProtocolOptions:
                "@type": type.googleapis.com/envoy.extensions.upstreams.http.v3.HttpProtocolOptions
                explicit_http_config:
                  http3_protocol_options: {}
                common_http_protocol_options:
                  idle_timeout: 1s
            transport_socket:
              name: envoy.transport_sockets.quic
              typed_config:
                "@type": type.googleapis.com/envoy.extensions.transport_sockets.quic.v3.QuicUpstreamTransport
                upstream_tls_context:
                  sni: firebase.google.cn
```



## 5.3filter_chains

### 5.2.4tls downstream

ef-cluster-filter_chain-tls_downstream.yaml

kubectl apply -f ef-cluster-filter_chain-tls_downstream.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: transport-socket
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
            protocol: TCP
            address: 0.0.0.0
            port_value: 8443
        filter_chains:
        - filters:
          - name: "envoy.filters.network.http_connection_manager"
            typed_config:
              "@type": "type.googleapis.com/envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager"
              codec_type: AUTO
              stat_prefix: ingress_https
              http_filters:
              - name: "envoy.filters.http.router"
                typed_config:
                  "@type": "type.googleapis.com/envoy.extensions.filters.http.router.v3.Router"
              route_config:
                name: http.8443
                virtual_hosts:
                - name: “*.8443”
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
                      cluster: my-productpage
          transport_socket: 
                  name: envoy.transport_sockets.tls
                  typed_config:
                    "@type": type.googleapis.com/envoy.extensions.transport_sockets.tls.v3.DownstreamTlsContext
                    require_client_certificate: false
                    common_tls_context:
                      alpn_protocols:
                      - "h2"
                      - "http/1.1"
                      tlsCertificateSdsSecretConfigs:
                      - name: default
                        sdsConfig:
                          apiConfigSource:
                            apiType: GRPC
                            grpcServices:
                            - envoyGrpc:
                                clusterName: sds-grpc
                            setNodeOnFirstMessageOnly: true
                            transportApiVersion: V3
                          initialFetchTimeout: 0s
                          resourceApiVersion: V3   
        traffic_direction: "OUTBOUND"     
        listener_filters:    
        - name: "envoy.filters.listener.tls_inspector"
          typed_config:
            "@type": "type.googleapis.com/envoy.extensions.filters.listener.tls_inspector.v3.TlsInspector"

  - applyTo: CLUSTER
    patch:
      operation: ADD     
      value:
          name: my-productpage
          type: STRICT_DNS
          connect_timeout: 10s
          lb_policy: ROUND_ROBIN
          load_assignment:
            cluster_name: my-productpage
            endpoints:
            - lb_endpoints:
              - endpoint:
                  address:
                     socket_address:
                      address: productpage.istio.svc.cluster.local
                      port_value: 9080
            
```

### 5.1.1starttls downstream

ef-cluster-filter_chain-starttls-downstream.yaml

kubectl apply -f ef-cluster-filter_chain-starttls-downstream.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: transport-socket
  namespace: istio-system 
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
                  cluster: my-productpage
  - applyTo: FILTER_CHAIN
    match:
      listener:
        portNumber: 8080
    patch:
      operation: MERGE
      value:
            transportSocket:
              name: envoy.transport_sockets.starttls
              typed_config:
                "@type": type.googleapis.com/envoy.extensions.transport_sockets.starttls.v3.StartTlsConfig
                cleartext_socket_config: {}
                tls_socket_config:
                    common_tls_context:
                      combinedValidationContext:
                        defaultValidationContext:
                          matchSubjectAltNames:
                          - exact: spiffe://cluster.local/ns/istio/sa/bookinfo-productpage
                        validationContextSdsSecretConfig:
                          name: ROOTCA
                          sdsConfig:
                            apiConfigSource:
                              apiType: GRPC
                              grpcServices:
                              - envoyGrpc:
                                  clusterName: sds-grpc
                              setNodeOnFirstMessageOnly: true
                              transportApiVersion: V3
                            initialFetchTimeout: 0s
                            resourceApiVersion: V3
                      tlsCertificateSdsSecretConfigs:
                      - name: default
                        sdsConfig:
                          apiConfigSource:
                            apiType: GRPC
                            grpcServices:
                            - envoyGrpc:
                                clusterName: sds-grpc
                            setNodeOnFirstMessageOnly: true
                            transportApiVersion: V3
                          initialFetchTimeout: 0s
                          resourceApiVersion: V3
  - applyTo: CLUSTER
    patch:
      operation: ADD     
      value:
          name: my-productpage
          type: STRICT_DNS
          connect_timeout: 10s
          lb_policy: ROUND_ROBIN
          load_assignment:
            cluster_name: my-productpage
            endpoints:
            - lb_endpoints:
              - endpoint:
                  address:
                     socket_address:
                      address: productpage.istio.svc.cluster.local
                      port_value: 9080

```



### 5.3.1tap

ef-filter_chains-tap.yaml

kubectl apply -f ef-filter_chains-tap.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: transport-socket
  namespace: istio-system 
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
                  cluster: my-productpage
  - applyTo: CLUSTER
    patch:
      operation: ADD     
      value:
        name: my-productpage
        type: STRICT_DNS
        connect_timeout: 10s
        lb_policy: ROUND_ROBIN
        load_assignment:
          cluster_name: my-productpage
          endpoints:
          - lb_endpoints:
            - endpoint:
                address:
                   socket_address:
                    address: productpage.istio.svc.cluster.local
                    port_value: 9080
        transport_socket:
          name: envoy.transport_sockets.tap
          typed_config:
            "@type": type.googleapis.com/envoy.extensions.transport_sockets.tap.v3.Tap
            common_config:
              static_config:
                match_config:
                  any_match: true
                output_config:
                    streaming: true
                    sinks:
                      - format: PROTO_BINARY_LENGTH_DELIMITED
                        file_per_tap:
                          path_prefix: /var/lib/istio/data/         
            transport_socket:
                name: envoy.transport_sockets.tls
                typed_config:
                  '@type': type.googleapis.com/envoy.extensions.transport_sockets.tls.v3.UpstreamTlsContext
                  common_tls_context:
                    alpn_protocols:
                    - h2
                    - http/1.1
                    combined_validation_context:
                      default_validation_context:
                        match_subject_alt_names:
                        - prefix: spiffe://cluster.local/
                      validation_context_sds_secret_config:
                        name: ROOTCA
                        sds_config:
                          api_config_source:
                            api_type: GRPC
                            grpc_services:
                            - envoy_grpc:
                                cluster_name: sds-grpc
                            set_node_on_first_message_only: true
                            transport_api_version: V3
                          initial_fetch_timeout: 0s
                          resource_api_version: V3
                    tls_certificate_sds_secret_configs:
                    - name: default
                      sds_config:
                        api_config_source:
                          api_type: GRPC
                          grpc_services:
                          - envoy_grpc:
                              cluster_name: sds-grpc
                          set_node_on_first_message_only: true
                          transport_api_version: V3
                        initial_fetch_timeout: 0s
                        resource_api_version: V3
                    tls_params:
                      cipher_suites:
                      - ECDHE-ECDSA-AES256-GCM-SHA384
                      - ECDHE-RSA-AES256-GCM-SHA384
                      - ECDHE-ECDSA-AES128-GCM-SHA256
                      - ECDHE-RSA-AES128-GCM-SHA256
                      - AES256-GCM-SHA384
                      - AES128-GCM-SHA256
                      tls_minimum_protocol_version: TLSv1_2
            
```

###  5.3.2alts

只有gce应用才能用alts协议。

ef-filter_chains-alts.yaml

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: transport-socket
  namespace: istio 
spec:
  workloadSelector:
    labels:
      app: productpage
  configPatches:
  - applyTo: LISTENER
    match:
      context: SIDECAR_INBOUND
    patch:
      operation: ADD
      value:
        name: proxy
        address:
          socket_address:
            protocol: TCP
            address: 0.0.0.0
            port_value: 18081
        filter_chains:
        - filters:
          - name: envoy.filters.network.http_connection_manager
            typed_config:
              "@type": type.googleapis.com/envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager  
              stat_prefix: ingress_http
              codec_type: AUTO
              route_config:
                name: local_route
                virtual_hosts:
                - name: local_service
                  domains: ["*"]
                  routes:
                  - match:
                      prefix: "/"
                    route: 
                      cluster: service_httpbin               
              http_filters:
              - name: envoy.filters.http.router
          transport_socket:
            name: envoy.transport_sockets.alts
            typed_config:
              "@type": type.googleapis.com/envoy.extensions.transport_sockets.alts.v3.Alts
              handshaker_service: "169.254.169.254:8080"
              peer_service_accounts:
              - "alts-client@mineral-minutia-820.iam.gserviceaccount.com"
  - applyTo: CLUSTER
    patch:
      operation: ADD
      value: 
            name: service_httpbin
            connect_timeout: 2s
            type: LOGICAL_DNS
            dns_lookup_family: V4_ONLY
            lb_policy: ROUND_ROBIN
            load_assignment:
              cluster_name: cert_service
              endpoints:
              - lb_endpoints:
                - endpoint:
                    address:
                      socket_address:
                        address: httpbin.org
                        port_value: 443
            transport_socket:
              name: envoy.transport_sockets.tls
              typed_config:
                "@type": type.googleapis.com/envoy.extensions.transport_sockets.tls.v3.UpstreamTlsContext
```

### 5.3.3quic downstream

设置env

```
kubectl set env -n istio-system deploy istiod PILOT_ENABLE_QUIC_LISTENERS=true
kubectl rollout restart deploy -n istio-system istiod
```

把443端口改为udp协议，使用nodeport类型service

```
          - name: http3
            port: 443
            targetPort: 8443
            protocol: UDP
```



创建secret

httpbin.cnf

```
[req]
default_bits       = 2048
prompt             = no
distinguished_name = req_distinguished_name
req_extensions     = san_reqext

[ req_distinguished_name ]
countryName         = IN
stateOrProvinceName = KA
organizationName    = QuicCorp

[ san_reqext ]
subjectAltName      = @alt_names

[alt_names]
DNS.0   = httpbin.quic-corp.com
```

```
openssl req -x509 -sha256 -nodes -days 365 -newkey rsa:4096 -subj "/C=IN/ST=KA/O=QuicCorp" -keyout quiccorp-ca.key -out quiccorp-ca.crt

openssl req -out httpbin.csr -newkey rsa:2048 -nodes -keyout httpbin.key -config httpbin.cnf

openssl x509 -req -days 365 -CA quiccorp-ca.crt -CAkey quiccorp-ca.key -set_serial 0 -in httpbin.csr -out httpbin.crt -extfile httpbin.cnf -extensions san_reqext

```

```
kubectl -n istio-system create secret tls httpbin-cred --key=httpbin.key --cert=httpbin.crt
```



ef-filter_chains-quic_downstream.yaml

kubectl apply -f ef-filter_chains-quic_downstream.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: transport-socket
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
            protocol: UDP
            address: 0.0.0.0
            port_value: 8443
        filter_chains:
        - filters:
          - name: "envoy.filters.network.http_connection_manager"
            typed_config:
              "@type": "type.googleapis.com/envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager"
              stat_prefix: ingress_proxy
              codec_type: "HTTP3"
              route_config:
                name: route_a
                virtual_hosts:
                - name: envoy_cyz
                  domains:
                  - "*"
                  routes:
                  - name: testroute
                    match: 
                      prefix: /
                    direct_response:
                      status: 200
                      body: 
                        inline_string: "prefix"
                    response_headers_to_add:
                    - header:
                        key: "alt-svc"
                        value: "h3=:443; ma=86400"
                      append: true
              http_filters:
              - name: "envoy.filters.http.router"
                typed_config:
                  "@type": "type.googleapis.com/envoy.extensions.filters.http.router.v3.Router"
              http3_protocol_options:
                quic_protocol_options:
                  max_concurrent_streams: 100
                  initial_stream_window_size: 65536 
                  initial_connection_window_size: 65536 
                  num_timeouts_to_trigger_port_migration: 1
                  connection_keepalive:
                    max_interval: 60s
                    initial_interval: 30s
                override_stream_error_on_invalid_http_message: true
                allow_extended_connect: true
          transport_socket: 
            name: "envoy.transport_sockets.quic"
            typed_config:
              "@type": "type.googleapis.com/envoy.extensions.transport_sockets.quic.v3.QuicDownstreamTransport"
              downstream_tls_context:
                common_tls_context:
                  alpn_protocols:
                  - h3
                  tls_certificate_sds_secret_configs:
                  - name: "kubernetes://httpbin-cred"
                    sds_config:
                      ads: { }
                      resource_api_version: "V3"
                require_client_certificate: f
        traffic_direction: "OUTBOUND"
        udp_listener_config:
          downstream_socket_config: { }
          quic_options: { }
        reuse_port: true
```



## 5.4三层以上嵌套

 my-productpage: Didn't find a registered implementation for name: 'envoy.transport_sockets.tcp_stats'

tcp_stats不支持

ef-cluster-transport_socket-tap-tcp_stats.yaml

kubectl apply -f ef-cluster-transport_socket-tap-tcp_stats.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: transport-socket
  namespace: istio-system 
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
                  cluster: my-productpage
  - applyTo: CLUSTER
    patch:
      operation: ADD     
      value:
          name: my-productpage
          type: STRICT_DNS
          connect_timeout: 10s
          lb_policy: ROUND_ROBIN
          load_assignment:
            cluster_name: my-productpage
            endpoints:
            - lb_endpoints:
              - endpoint:
                  address:
                    socket_address:
                      address: productpage.istio.svc.cluster.local
                      port_value: 9080
          transport_socket:
            name: envoy.transport_sockets.tap
            typed_config:
              "@type": type.googleapis.com/envoy.extensions.transport_sockets.tap.v3.Tap
              common_config:
                  static_config:
                    match_config:
                      any_match: true
                    output_config:
                        streaming: true
                        sinks:
                          - format: PROTO_BINARY_LENGTH_DELIMITED
                            file_per_tap:
                              path_prefix: /var/lib/istio/data/
              transport_socket:
                name: envoy.transport_sockets.tcp_stats
                typed_config:
                    "@type": type.googleapis.com/envoy.extensions.transport_sockets.tcp_stats.v3.Config
                    update_period: 30s
                    transport_socket:
                      name: envoy.transport_sockets.tls
                      typed_config:
                        "@type": type.googleapis.com/envoy.extensions.transport_sockets.tls.v3.UpstreamTlsContext
                        common_tls_context:
                          combinedValidationContext:
                            defaultValidationContext:
                              matchSubjectAltNames:
                              - exact: spiffe://cluster.local/ns/istio/sa/bookinfo-productpage
                            validationContextSdsSecretConfig:
                              name: ROOTCA
                              sdsConfig:
                                apiConfigSource:
                                  apiType: GRPC
                                  grpcServices:
                                  - envoyGrpc:
                                      clusterName: sds-grpc
                                  setNodeOnFirstMessageOnly: true
                                  transportApiVersion: V3
                                initialFetchTimeout: 0s
                                resourceApiVersion: V3
                          tlsCertificateSdsSecretConfigs:
                          - name: default
                            sdsConfig:
                              apiConfigSource:
                                apiType: GRPC
                                grpcServices:
                                - envoyGrpc:
                                    clusterName: sds-grpc
                                setNodeOnFirstMessageOnly: true
                                transportApiVersion: V3
                              initialFetchTimeout: 0s
                              resourceApiVersion: V3
                        sni: outbound_.9080_._.productpage.istio.svc.cluster.local

```

