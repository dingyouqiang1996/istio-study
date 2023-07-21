# 1什么是listener

listener是监听器的意思，所有入口出口流量都要通过监听器，xds里有个lds，l就是监听器listener。如果是http协议，监听器必须配置httpConnectionManager network过滤器，tcp协议配置 tcp_proxy 过滤器。监听器最重要的配置是filter_chains,通过过滤器链配置实现各种功能。监听器有个监听器过滤器的配置即listener_filters,他比其他过滤器先起作用，主要用来配置协议嗅探等。过滤器链可配置默认过滤器链，当没有过滤器链匹配时就用默认过滤器链配置。

# 2配置

```
{
  "name": "...",监听器名称
  "address": "{...}",监听器地址
  "stat_prefix": "...",stat前缀
  "filter_chains": [],过滤器链配置
  "use_original_dst": "{...}",使用original_dst
  "default_filter_chain": "{...}",默认过滤器链
  "per_connection_buffer_limit_bytes": "{...}",每个连接缓存限值，默认1Mb
  "metadata": "{...}",元数据
  "drain_type": "...",listener级别的排水方式
  "listener_filters": [],监听器过滤器
  "listener_filters_timeout": "{...}",监听器过滤器超时时间
  "continue_on_listener_filters_timeout": "...",当监听器过滤器超时时是否创建连接
  "transparent": "{...}",是否设置透明socket
  "freebind": "{...}",是否设置IP_FREEBIND socket选项
  "socket_options": [],socket选项
  "tcp_fast_open_queue_length": "{...}",TCP Fast Open队列长度
  "traffic_direction": "...",流向方向
  "udp_listener_config": "{...}",udp监听器配置
  "api_listener": "{...}",api监听器 
  "connection_balance_config": "{...}",连接平衡配置
  "reuse_port": "...",废弃
  "enable_reuse_port": "{...}",是否重用端口,istio还没实现
  "access_log": [],访问日志配置
  "tcp_backlog_size": "{...}",tcp等待队列长度
  "bind_to_port": "{...}"是否绑定端口
}
```

filter_chains：

```
{
  "filter_chain_match": "{...}",过滤器链匹配条件
  "filters": [],网络过滤器配置
  "use_proxy_proto": "{...}",废弃
  "transport_socket": "{...}",传输socket配置
  "transport_socket_connect_timeout": "{...}"传输socket连接超时时间
}
```

filters：

- [envoy.filters.network.client_ssl_auth](https://www.envoyproxy.io/docs/envoy/latest/api-v3/extensions/filters/network/client_ssl_auth/v3/client_ssl_auth.proto#extension-envoy-filters-network-client-ssl-auth)
- [envoy.filters.network.connection_limit](https://www.envoyproxy.io/docs/envoy/latest/api-v3/extensions/filters/network/connection_limit/v3/connection_limit.proto#extension-envoy-filters-network-connection-limit)
- [envoy.filters.network.direct_response](https://www.envoyproxy.io/docs/envoy/latest/api-v3/extensions/filters/network/direct_response/v3/config.proto#extension-envoy-filters-network-direct-response)
- [envoy.filters.network.dubbo_proxy](https://www.envoyproxy.io/docs/envoy/latest/api-v3/extensions/filters/network/dubbo_proxy/v3/dubbo_proxy.proto#extension-envoy-filters-network-dubbo-proxy)
- [envoy.filters.network.echo](https://www.envoyproxy.io/docs/envoy/latest/api-v3/extensions/filters/network/echo/v3/echo.proto#extension-envoy-filters-network-echo)
- [envoy.filters.network.envoy_mobile_http_connection_manager](https://www.envoyproxy.io/docs/envoy/latest/api-v3/extensions/filters/network/http_connection_manager/v3/http_connection_manager.proto#extension-envoy-filters-network-envoy-mobile-http-connection-manager)
- [envoy.filters.network.ext_authz](https://www.envoyproxy.io/docs/envoy/latest/api-v3/extensions/filters/network/ext_authz/v3/ext_authz.proto#extension-envoy-filters-network-ext-authz)
- [envoy.filters.network.http_connection_manager](https://www.envoyproxy.io/docs/envoy/latest/api-v3/extensions/filters/network/http_connection_manager/v3/http_connection_manager.proto#extension-envoy-filters-network-http-connection-manager)
- [envoy.filters.network.local_ratelimit](https://www.envoyproxy.io/docs/envoy/latest/api-v3/extensions/filters/network/local_ratelimit/v3/local_rate_limit.proto#extension-envoy-filters-network-local-ratelimit)
- [envoy.filters.network.mongo_proxy](https://www.envoyproxy.io/docs/envoy/latest/api-v3/extensions/filters/network/mongo_proxy/v3/mongo_proxy.proto#extension-envoy-filters-network-mongo-proxy)
- [envoy.filters.network.ratelimit](https://www.envoyproxy.io/docs/envoy/latest/api-v3/extensions/filters/network/ratelimit/v3/rate_limit.proto#extension-envoy-filters-network-ratelimit)
- [envoy.filters.network.rbac](https://www.envoyproxy.io/docs/envoy/latest/api-v3/extensions/filters/network/rbac/v3/rbac.proto#extension-envoy-filters-network-rbac)
- [envoy.filters.network.redis_proxy](https://www.envoyproxy.io/docs/envoy/latest/api-v3/extensions/filters/network/redis_proxy/v3/redis_proxy.proto#extension-envoy-filters-network-redis-proxy)
- [envoy.filters.network.sni_cluster](https://www.envoyproxy.io/docs/envoy/latest/api-v3/extensions/filters/network/sni_cluster/v3/sni_cluster.proto#extension-envoy-filters-network-sni-cluster)
- [envoy.filters.network.sni_dynamic_forward_proxy](https://www.envoyproxy.io/docs/envoy/latest/api-v3/extensions/filters/network/sni_dynamic_forward_proxy/v3/sni_dynamic_forward_proxy.proto#extension-envoy-filters-network-sni-dynamic-forward-proxy)
- [envoy.filters.network.tcp_proxy](https://www.envoyproxy.io/docs/envoy/latest/api-v3/extensions/filters/network/tcp_proxy/v3/tcp_proxy.proto#extension-envoy-filters-network-tcp-proxy)
- [envoy.filters.network.thrift_proxy](https://www.envoyproxy.io/docs/envoy/latest/api-v3/extensions/filters/network/thrift_proxy/v3/thrift_proxy.proto#extension-envoy-filters-network-thrift-proxy)
- [envoy.filters.network.wasm](https://www.envoyproxy.io/docs/envoy/latest/api-v3/extensions/filters/network/wasm/v3/wasm.proto#extension-envoy-filters-network-wasm)
- [envoy.filters.network.zookeeper_proxy](https://www.envoyproxy.io/docs/envoy/latest/api-v3/extensions/filters/network/zookeeper_proxy/v3/zookeeper_proxy.proto#extension-envoy-filters-network-zookeeper-proxy)

The following extensions are available in [contrib](https://www.envoyproxy.io/docs/envoy/latest/start/install#install-contrib) images only:

- [envoy.filters.network.kafka_broker](https://www.envoyproxy.io/docs/envoy/latest/api-v3/extensions/filters/network/kafka_broker/v3/kafka_broker.proto#extension-envoy-filters-network-kafka-broker)
- [envoy.filters.network.kafka_mesh](https://www.envoyproxy.io/docs/envoy/latest/api-v3/extensions/filters/network/kafka_mesh/v3alpha/kafka_mesh.proto#extension-envoy-filters-network-kafka-mesh)
- [envoy.filters.network.mysql_proxy](https://www.envoyproxy.io/docs/envoy/latest/api-v3/extensions/filters/network/mysql_proxy/v3/mysql_proxy.proto#extension-envoy-filters-network-mysql-proxy)
- [envoy.filters.network.postgres_proxy](https://www.envoyproxy.io/docs/envoy/latest/api-v3/extensions/filters/network/postgres_proxy/v3alpha/postgres_proxy.proto#extension-envoy-filters-network-postgres-proxy)
- [envoy.filters.network.rocketmq_proxy](https://www.envoyproxy.io/docs/envoy/latest/api-v3/extensions/filters/network/rocketmq_proxy/v3/rocketmq_proxy.proto#extension-envoy-filters-network-rocketmq-proxy)
- [envoy.filters.network.sip_proxy](https://www.envoyproxy.io/docs/envoy/latest/api-v3/extensions/filters/network/sip_proxy/v3alpha/sip_proxy.proto#extension-envoy-filters-network-sip-proxy)

filter_chain_match：

```
{
  "destination_port": "{...}",目标端口
  "prefix_ranges": [],地址cidr
  "direct_source_prefix_ranges": [],直接连接的源ip cidr
  "source_type": "...",源类型
  "source_prefix_ranges": [],源ip地址cidr
  "source_ports": [],源端口
  "server_names": [],服务器名称
  "transport_protocol": "...",传输协议
  "application_protocols": []应用协议
}
```

prefix_ranges：

```
{
  "address_prefix": "...",地址前缀
  "prefix_len": "{...}"前缀长度
}
```

source_type：

- ANY

  *(DEFAULT)* ⁣Any connection source matches.

- SAME_IP_OR_LOOPBACK

  ⁣Match a connection originating from the same host.

- EXTERNAL

  ⁣Match a connection originating from a different host.

transport_socket：

- [envoy.transport_sockets.alts](https://www.envoyproxy.io/docs/envoy/latest/api-v3/extensions/transport_sockets/alts/v3/alts.proto#extension-envoy-transport-sockets-alts)
- [envoy.transport_sockets.raw_buffer](https://www.envoyproxy.io/docs/envoy/latest/api-v3/extensions/transport_sockets/raw_buffer/v3/raw_buffer.proto#extension-envoy-transport-sockets-raw-buffer)
- [envoy.transport_sockets.starttls](https://www.envoyproxy.io/docs/envoy/latest/api-v3/extensions/transport_sockets/starttls/v3/starttls.proto#extension-envoy-transport-sockets-starttls)
- [envoy.transport_sockets.tap](https://www.envoyproxy.io/docs/envoy/latest/api-v3/extensions/transport_sockets/tap/v3/tap.proto#extension-envoy-transport-sockets-tap)
- [envoy.transport_sockets.tls](https://www.envoyproxy.io/docs/envoy/latest/api-v3/extensions/transport_sockets/tls/v3/tls.proto#extension-envoy-transport-sockets-tls)

 default_filter_chain：

```
{
  "filter_chain_match": "{...}",过滤器链匹配条件
  "filters": [],网络过滤器配置
  "use_proxy_proto": "{...}",废弃
  "transport_socket": "{...}",传输socket配置
  "transport_socket_connect_timeout": "{...}"传输socket连接超时时间
}
```

metadata：

```
{
  "filter_metadata": "{...}",元数据配置
  "typed_filter_metadata": "{...}"元数据配置
}
```

listener_filters：

- [envoy.filters.listener.http_inspector](https://www.envoyproxy.io/docs/envoy/latest/api-v3/extensions/filters/listener/http_inspector/v3/http_inspector.proto#extension-envoy-filters-listener-http-inspector)
- [envoy.filters.listener.original_dst](https://www.envoyproxy.io/docs/envoy/latest/api-v3/extensions/filters/listener/original_dst/v3/original_dst.proto#extension-envoy-filters-listener-original-dst)
- [envoy.filters.listener.original_src](https://www.envoyproxy.io/docs/envoy/latest/api-v3/extensions/filters/listener/original_src/v3/original_src.proto#extension-envoy-filters-listener-original-src)
- [envoy.filters.listener.proxy_protocol](https://www.envoyproxy.io/docs/envoy/latest/api-v3/extensions/filters/listener/proxy_protocol/v3/proxy_protocol.proto#extension-envoy-filters-listener-proxy-protocol)
- [envoy.filters.listener.tls_inspector](https://www.envoyproxy.io/docs/envoy/latest/api-v3/extensions/filters/listener/tls_inspector/v3/tls_inspector.proto#extension-envoy-filters-listener-tls-inspector)

 

- [envoy.filters.udp_listener.dns_filter](https://www.envoyproxy.io/docs/envoy/latest/api-v3/extensions/filters/udp/dns_filter/v3/dns_filter.proto#extension-envoy-filters-udp-listener-dns-filter)
- [envoy.filters.udp_listener.udp_proxy](https://www.envoyproxy.io/docs/envoy/latest/api-v3/extensions/filters/udp/udp_proxy/v3/udp_proxy.proto#extension-envoy-filters-udp-listener-udp-proxy)

**socket_options** ：

```
{
  "description": "...",名称
  "level": "...",传给setsockopt的参数
  "name": "...",数字化的名称
  "int_value": "...",int值
  "buf_value": "...",buf值
  "state": "..."状态，只支持STATE_PREBIND 
}
```

udp_listener_config：

```
{
  "downstream_socket_config": "{...}",下游socket配置
  "quic_options": "{...}"quic选项
}
```

downstream_socket_config：

```
{
  "max_rx_datagram_size": "{...}",接收的udp包的最大大小，默认1500 bytes.
  "prefer_gro": "{...}"Generic Receive Offload prefer
}
```

quic_options：

```
{
  "quic_protocol_options": "{...}",quic协议选项
  "idle_timeout": "{...}",空闲超时时间，默认300000ms
  "crypto_handshake_timeout": "{...}",crypto握手连接超时时间，默认 20000ms
  "enabled": "{...}",启用百分比，默认启用
  "packets_to_read_to_connection_count_ratio": "{...}",每个事件循环读取多少包比率，默认32
  "crypto_stream_config": "{...}",pcypto stream配置
  "proof_source_config": "{...}"proof source配置
}
```

quic_protocol_options：

```
{
  "max_concurrent_streams": "{...}",最大并发流
  "initial_stream_window_size": "{...}",初始流窗口大小
  "initial_connection_window_size": "{...}"初始连接窗口大小
}
```

crypto_stream_config：

- [envoy.quic.crypto_stream.server.quiche](https://www.envoyproxy.io/docs/envoy/latest/api-v3/extensions/quic/crypto_stream/v3/crypto_stream.proto#extension-envoy-quic-crypto-stream-server-quiche)

proof_source_config：

- [envoy.quic.proof_source.filter_chain](https://www.envoyproxy.io/docs/envoy/latest/api-v3/extensions/quic/proof_source/v3/proof_source.proto#extension-envoy-quic-proof-source-filter-chain)

api_listener：

```
{
  "api_listener": "{...}"
}
```

 **access_log** ：

```
{
  "name": "...",日志名称
  "filter": "{...}",过滤
  "typed_config": "{...}"日志配置
}
```

- [envoy.access_loggers.file](https://www.envoyproxy.io/docs/envoy/latest/api-v3/extensions/access_loggers/file/v3/file.proto#extension-envoy-access-loggers-file)
- [envoy.access_loggers.http_grpc](https://www.envoyproxy.io/docs/envoy/latest/api-v3/extensions/access_loggers/grpc/v3/als.proto#extension-envoy-access-loggers-http-grpc)
- [envoy.access_loggers.open_telemetry](https://www.envoyproxy.io/docs/envoy/latest/api-v3/extensions/access_loggers/open_telemetry/v3/logs_service.proto#extension-envoy-access-loggers-open-telemetry)
- [envoy.access_loggers.stream](https://www.envoyproxy.io/docs/envoy/latest/api-v3/extensions/access_loggers/stream/v3/stream.proto#extension-envoy-access-loggers-stream)
- [envoy.access_loggers.tcp_grpc](https://www.envoyproxy.io/docs/envoy/latest/api-v3/extensions/access_loggers/grpc/v3/als.proto#extension-envoy-access-loggers-tcp-grpc)
- [envoy.access_loggers.wasm](https://www.envoyproxy.io/docs/envoy/latest/api-v3/extensions/access_loggers/wasm/v3/wasm.proto#extension-envoy-access-loggers-wasm)

# 3实战

## 3.1default

listener-default.yaml

kubectl apply -f listener-default.yaml -n istio

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: listener
  namespace: istio 
spec:
  workloadSelector:
    labels:
      app: productpage
  configPatches:
  - applyTo: LISTENER
    patch:
      operation: ADD     
      value:
          accessLog:
          - filter:
              responseFlagFilter:
                flags:
                - NR
            name: envoy.access_loggers.file
            typedConfig:
              '@type': type.googleapis.com/envoy.extensions.access_loggers.file.v3.FileAccessLog
              logFormat:
                textFormat: |
                  [%START_TIME%] "%REQ(:METHOD)% %REQ(X-ENVOY-ORIGINAL-PATH?:PATH)% %PROTOCOL%" %RESPONSE_CODE% %RESPONSE_FLAGS% %RESPONSE_CODE_DETAILS% %CONNECTION_TERMINATION_DETAILS% "%UPSTREAM_TRANSPORT_FAILURE_REASON%" %BYTES_RECEIVED% %BYTES_SENT% %DURATION% %RESP(X-ENVOY-UPSTREAM-SERVICE-TIME)% "%REQ(X-FORWARDED-FOR)%" "%REQ(USER-AGENT)%" "%REQ(X-REQUEST-ID)%" "%REQ(:AUTHORITY)%" "%UPSTREAM_HOST%" %UPSTREAM_CLUSTER% %UPSTREAM_LOCAL_ADDRESS% %DOWNSTREAM_LOCAL_ADDRESS% %DOWNSTREAM_REMOTE_ADDRESS% %REQUESTED_SERVER_NAME% %ROUTE_NAME%
              path: /dev/stdout
          address:
            socketAddress:
              address: 0.0.0.0
              portValue: 8083
          continueOnListenerFiltersTimeout: true
          defaultFilterChain:
            filterChainMatch: {}
            filters:
            - name: istio.stats
              typedConfig:
                '@type': type.googleapis.com/udpa.type.v1.TypedStruct
                typeUrl: type.googleapis.com/envoy.extensions.filters.network.wasm.v3.Wasm
                value:
                  config:
                    configuration:
                      '@type': type.googleapis.com/google.protobuf.StringValue
                      value: |
                        {
                          "debug": "false",
                          "stat_prefix": "istio"
                        }
                    root_id: stats_outbound
                    vm_config:
                      code:
                        local:
                          inline_string: envoy.wasm.stats
                      runtime: envoy.wasm.runtime.null
                      vm_id: tcp_stats_outbound
            - name: envoy.filters.network.tcp_proxy
              typedConfig:
                '@type': type.googleapis.com/envoy.extensions.filters.network.tcp_proxy.v3.TcpProxy
                accessLog:
                - name: envoy.access_loggers.file
                  typedConfig:
                    '@type': type.googleapis.com/envoy.extensions.access_loggers.file.v3.FileAccessLog
                    logFormat:
                      textFormat: |
                        [%START_TIME%] "%REQ(:METHOD)% %REQ(X-ENVOY-ORIGINAL-PATH?:PATH)% %PROTOCOL%" %RESPONSE_CODE% %RESPONSE_FLAGS% %RESPONSE_CODE_DETAILS% %CONNECTION_TERMINATION_DETAILS% "%UPSTREAM_TRANSPORT_FAILURE_REASON%" %BYTES_RECEIVED% %BYTES_SENT% %DURATION% %RESP(X-ENVOY-UPSTREAM-SERVICE-TIME)% "%REQ(X-FORWARDED-FOR)%" "%REQ(USER-AGENT)%" "%REQ(X-REQUEST-ID)%" "%REQ(:AUTHORITY)%" "%UPSTREAM_HOST%" %UPSTREAM_CLUSTER% %UPSTREAM_LOCAL_ADDRESS% %DOWNSTREAM_LOCAL_ADDRESS% %DOWNSTREAM_REMOTE_ADDRESS% %REQUESTED_SERVER_NAME% %ROUTE_NAME%
                    path: /dev/stdout
                cluster: PassthroughCluster
                statPrefix: PassthroughCluster
            name: PassthroughFilterChain
          deprecatedV1:
            bindToPort: false
          filterChains:
          - filterChainMatch:
              applicationProtocols:
              - http/1.0
              - http/1.1
              - h2c
              transportProtocol: raw_buffer
            filters:
            - name: envoy.filters.network.http_connection_manager
              typedConfig:
                '@type': type.googleapis.com/envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager
                accessLog:
                - name: envoy.access_loggers.file
                  typedConfig:
                    '@type': type.googleapis.com/envoy.extensions.access_loggers.file.v3.FileAccessLog
                    logFormat:
                      textFormat: |
                        [%START_TIME%] "%REQ(:METHOD)% %REQ(X-ENVOY-ORIGINAL-PATH?:PATH)% %PROTOCOL%" %RESPONSE_CODE% %RESPONSE_FLAGS% %RESPONSE_CODE_DETAILS% %CONNECTION_TERMINATION_DETAILS% "%UPSTREAM_TRANSPORT_FAILURE_REASON%" %BYTES_RECEIVED% %BYTES_SENT% %DURATION% %RESP(X-ENVOY-UPSTREAM-SERVICE-TIME)% "%REQ(X-FORWARDED-FOR)%" "%REQ(USER-AGENT)%" "%REQ(X-REQUEST-ID)%" "%REQ(:AUTHORITY)%" "%UPSTREAM_HOST%" %UPSTREAM_CLUSTER% %UPSTREAM_LOCAL_ADDRESS% %DOWNSTREAM_LOCAL_ADDRESS% %DOWNSTREAM_REMOTE_ADDRESS% %REQUESTED_SERVER_NAME% %ROUTE_NAME%
                    path: /dev/stdout
                delayedCloseTimeout: 1s
                httpFilters:
                - name: istio.metadata_exchange
                  typedConfig:
                    '@type': type.googleapis.com/udpa.type.v1.TypedStruct
                    typeUrl: type.googleapis.com/envoy.extensions.filters.http.wasm.v3.Wasm
                    value:
                      config:
                        configuration:
                          '@type': type.googleapis.com/google.protobuf.StringValue
                          value: |
                            {}
                        vm_config:
                          code:
                            local:
                              inline_string: envoy.wasm.metadata_exchange
                          runtime: envoy.wasm.runtime.null
                - name: istio.alpn
                  typedConfig:
                    '@type': type.googleapis.com/istio.envoy.config.filter.http.alpn.v2alpha1.FilterConfig
                    alpnOverride:
                    - alpnOverride:
                      - istio-http/1.0
                      - istio
                      - http/1.0
                    - alpnOverride:
                      - istio-http/1.1
                      - istio
                      - http/1.1
                      upstreamProtocol: HTTP11
                    - alpnOverride:
                      - istio-h2
                      - istio
                      - h2
                      upstreamProtocol: HTTP2
                - name: envoy.filters.http.cors
                  typedConfig:
                    '@type': type.googleapis.com/envoy.extensions.filters.http.cors.v3.Cors
                - name: envoy.filters.http.fault
                  typedConfig:
                    '@type': type.googleapis.com/envoy.extensions.filters.http.fault.v3.HTTPFault
                - name: istio.stats
                  typedConfig:
                    '@type': type.googleapis.com/udpa.type.v1.TypedStruct
                    typeUrl: type.googleapis.com/envoy.extensions.filters.http.wasm.v3.Wasm
                    value:
                      config:
                        configuration:
                          '@type': type.googleapis.com/google.protobuf.StringValue
                          value: |
                            {
                              "debug": "false",
                              "stat_prefix": "istio"
                            }
                        root_id: stats_outbound
                        vm_config:
                          code:
                            local:
                              inline_string: envoy.wasm.stats
                          runtime: envoy.wasm.runtime.null
                          vm_id: stats_outbound
                - name: envoy.filters.http.router
                  typedConfig:
                    '@type': type.googleapis.com/envoy.extensions.filters.http.router.v3.Router
                normalizePath: true
                pathWithEscapedSlashesAction: KEEP_UNCHANGED
                rds:
                  configSource:
                    ads: {}
                    initialFetchTimeout: 0s
                    resourceApiVersion: V3
                  routeConfigName: "9080"
                statPrefix: inbound_0.0.0.0_8083
                streamIdleTimeout: 0s
                tracing:
                  clientSampling:
                    value: 100
                  customTags:
                  - metadata:
                      kind:
                        request: {}
                      metadataKey:
                        key: envoy.filters.http.rbac
                        path:
                        - key: istio_dry_run_allow_shadow_effective_policy_id
                    tag: istio.authorization.dry_run.allow_policy.name
                  - metadata:
                      kind:
                        request: {}
                      metadataKey:
                        key: envoy.filters.http.rbac
                        path:
                        - key: istio_dry_run_allow_shadow_engine_result
                    tag: istio.authorization.dry_run.allow_policy.result
                  - metadata:
                      kind:
                        request: {}
                      metadataKey:
                        key: envoy.filters.http.rbac
                        path:
                        - key: istio_dry_run_deny_shadow_effective_policy_id
                    tag: istio.authorization.dry_run.deny_policy.name
                  - metadata:
                      kind:
                        request: {}
                      metadataKey:
                        key: envoy.filters.http.rbac
                        path:
                        - key: istio_dry_run_deny_shadow_engine_result
                    tag: istio.authorization.dry_run.deny_policy.result
                  - literal:
                      value: v1
                    tag: istio.canonical_revision
                  - literal:
                      value: productpage
                    tag: istio.canonical_service
                  - literal:
                      value: mesh1
                    tag: istio.mesh_id
                  - literal:
                      value: istio
                    tag: istio.namespace
                  overallSampling:
                    value: 100
                  randomSampling:
                    value: 1
                upgradeConfigs:
                - upgradeType: websocket
                useRemoteAddress: false
          listenerFilters:
          - name: envoy.filters.listener.tls_inspector
            typedConfig:
              '@type': type.googleapis.com/envoy.extensions.filters.listener.tls_inspector.v3.TlsInspector
          - name: envoy.filters.listener.http_inspector
            typedConfig:
              '@type': type.googleapis.com/envoy.extensions.filters.listener.http_inspector.v3.HttpInspector
          listenerFiltersTimeout: 0s
          name: 0.0.0.0_8083
          trafficDirection: INBOUND
```

vs-bookinfo.yaml

kubectl apply -f vs-bookinfo.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: bookinfo
spec:
  hosts:
  - "*"
  gateways:
  - bookinfo-gateway
  http:
  - match:
    - uri:
        prefix: /
    route:
    - destination:
        host: productpage.istio.svc.cluster.local
        port:
          number: 8083
```

k8s svc 添加端口

 kubectl edit svc productpage -n istio

```
  - name: http8083
    port: 8083
    protocol: TCP
    targetPort: 8083
```



## 3.2filter_chains

```
{
  "filter_chain_match": "{...}",过滤器链匹配条件
  "filters": [],网络过滤器配置
  "use_proxy_proto": "{...}",废弃
  "transport_socket": "{...}",传输socket配置
  "transport_socket_connect_timeout": "{...}"传输socket连接超时时间
}
```

### 3.2.1filter_chain_match

```
{
  "destination_port": "{...}",目标端口
  "prefix_ranges": [],地址cidr
  "direct_source_prefix_ranges": [],直接连接的源ip cidr
  "source_type": "...",源类型
  "source_prefix_ranges": [],源ip地址cidr
  "source_ports": [],源端口
  "server_names": [],服务器名称
  "transport_protocol": "...",传输协议
  "application_protocols": []应用协议
}
```

#### 3.2.1.1destination_port

listener-filter_chain_match-destination_port.yaml

kubectl apply -f listener-filter_chain_match-destination_port.yaml -n istio

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: listener
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
            port_value: 8083
        filter_chains:
        - filter_chain_match:
            destination_port: 8083
          filters:
          - name: "envoy.filters.network.http_connection_manager"
            typed_config:
              "@type": "type.googleapis.com/envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager"
              stat_prefix: ingress_proxy
              route_config:
                name: route_a
                virtual_hosts:
                - name: envoy_cyz
                  domains:
                  - "*"
                  routes:
                  - match:
                      prefix: "/"
                    route:
                      cluster: cluster123
              http_filters:
              - name: "envoy.filters.http.router"
                typed_config:
                  "@type": "type.googleapis.com/envoy.extensions.filters.http.router.v3.Router"
  - applyTo: CLUSTER
    patch:
      operation: ADD
      value: 
        name: "cluster123"
        type: STATIC
        connect_timeout: 0.5s
        lb_policy: ROUND_ROBIN
        load_assignment:
          cluster_name: cluster123
          endpoints:
          - lb_endpoints:
            - endpoint:
                address:
                  socket_address:
                    address: 127.0.0.1
                    port_value: 9080
```



vs-bookinfo.yaml

kubectl apply -f vs-bookinfo.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: bookinfo
spec:
  hosts:
  - "*"
  gateways:
  - bookinfo-gateway
  http:
  - match:
    - uri:
        prefix: /
    route:
    - destination:
        host: productpage.istio.svc.cluster.local
        port:
          number: 8083
```

k8s svc 添加端口

 kubectl edit svc productpage -n istio

```
  - name: http8083
    port: 8083
    protocol: TCP
    targetPort: 8083
```



#### 3.2.1.2prefix_ranges

listener-filter_chain_match-prefix_ranges.yaml

kubectl apply -f listener-filter_chain_match-prefix_ranges.yaml -n istio

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: listener
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
            port_value: 8083
        filter_chains:
        - filter_chain_match:
            destination_port: 8083
            prefix_ranges:
            - address_prefix: 172.20.1.37
              prefix_len: 32
          filters:
          - name: "envoy.filters.network.http_connection_manager"
            typed_config:
              "@type": "type.googleapis.com/envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager"
              stat_prefix: ingress_proxy
              route_config:
                name: route_a
                virtual_hosts:
                - name: envoy_cyz
                  domains:
                  - "*"
                  routes:
                  - match:
                      prefix: "/"
                    route:
                      cluster: cluster123
              http_filters:
              - name: "envoy.filters.http.router"
                typed_config:
                  "@type": "type.googleapis.com/envoy.extensions.filters.http.router.v3.Router"
  - applyTo: CLUSTER
    patch:
      operation: ADD
      value: 
        name: "cluster123"
        type: STATIC
        connect_timeout: 0.5s
        lb_policy: ROUND_ROBIN
        load_assignment:
          cluster_name: cluster123
          endpoints:
          - lb_endpoints:
            - endpoint:
                address:
                  socket_address:
                    address: 127.0.0.1
                    port_value: 9080
```

address_prefix: productpage-v1 pod的ip

#### 3.2.1.3direct_source_prefix_ranges

listener-filter_chain_match-direct_source_prefix_ranges.yaml

kubectl apply -f listener-filter_chain_match-direct_source_prefix_ranges.yaml -n istio

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: listener
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
            port_value: 8083
        filter_chains:
        - filter_chain_match:
            destination_port: 8083
            direct_source_prefix_ranges:
            - address_prefix: 0.0.0.0
              prefix_len: 0
          filters:
          - name: "envoy.filters.network.http_connection_manager"
            typed_config:
              "@type": "type.googleapis.com/envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager"
              stat_prefix: ingress_proxy
              route_config:
                name: route_a
                virtual_hosts:
                - name: envoy_cyz
                  domains:
                  - "*"
                  routes:
                  - match:
                      prefix: "/"
                    route:
                      cluster: cluster123
              http_filters:
              - name: "envoy.filters.http.router"
                typed_config:
                  "@type": "type.googleapis.com/envoy.extensions.filters.http.router.v3.Router"
  - applyTo: CLUSTER
    patch:
      operation: ADD
      value: 
        name: "cluster123"
        type: STATIC
        connect_timeout: 0.5s
        lb_policy: ROUND_ROBIN
        load_assignment:
          cluster_name: cluster123
          endpoints:
          - lb_endpoints:
            - endpoint:
                address:
                  socket_address:
                    address: 127.0.0.1
                    port_value: 9080
```

#### 3.2.1.4source_type

- ANY

  *(DEFAULT)* ⁣Any connection source matches.

- SAME_IP_OR_LOOPBACK

  ⁣Match a connection originating from the same host.

- EXTERNAL

  ⁣Match a connection originating from a different host.

##### 3.2.1.4.1ANY

listener-filter_chain_match-source_type-ANY.yaml

kubectl apply -f listener-filter_chain_match-source_type-ANY.yaml -n istio

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: listener
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
            port_value: 8083
        filter_chains:
        - filter_chain_match:
            destination_port: 8083
            source_type: ANY
          filters:
          - name: "envoy.filters.network.http_connection_manager"
            typed_config:
              "@type": "type.googleapis.com/envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager"
              stat_prefix: ingress_proxy
              route_config:
                name: route_a
                virtual_hosts:
                - name: envoy_cyz
                  domains:
                  - "*"
                  routes:
                  - match:
                      prefix: "/"
                    route:
                      cluster: cluster123
              http_filters:
              - name: "envoy.filters.http.router"
                typed_config:
                  "@type": "type.googleapis.com/envoy.extensions.filters.http.router.v3.Router"
  - applyTo: CLUSTER
    patch:
      operation: ADD
      value: 
        name: "cluster123"
        type: STATIC
        connect_timeout: 0.5s
        lb_policy: ROUND_ROBIN
        load_assignment:
          cluster_name: cluster123
          endpoints:
          - lb_endpoints:
            - endpoint:
                address:
                  socket_address:
                    address: 127.0.0.1
                    port_value: 9080
```

##### 3.2.1.4.2SAME_IP_OR_LOOPBACK

listener-filter_chain_match-source_type-SAME_IP_OR_LOOPBACK.yaml

kubectl apply -f listener-filter_chain_match-source_type-SAME_IP_OR_LOOPBACK.yaml -n istio

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: listener
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
            port_value: 8083
        filter_chains:
        - filter_chain_match:
            destination_port: 8083
            source_type: SAME_IP_OR_LOOPBACK
          filters:
          - name: "envoy.filters.network.http_connection_manager"
            typed_config:
              "@type": "type.googleapis.com/envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager"
              stat_prefix: ingress_proxy
              route_config:
                name: route_a
                virtual_hosts:
                - name: envoy_cyz
                  domains:
                  - "*"
                  routes:
                  - match:
                      prefix: "/"
                    route:
                      cluster: cluster123
              http_filters:
              - name: "envoy.filters.http.router"
                typed_config:
                  "@type": "type.googleapis.com/envoy.extensions.filters.http.router.v3.Router"
  - applyTo: CLUSTER
    patch:
      operation: ADD
      value: 
        name: "cluster123"
        type: STATIC
        connect_timeout: 0.5s
        lb_policy: ROUND_ROBIN
        load_assignment:
          cluster_name: cluster123
          endpoints:
          - lb_endpoints:
            - endpoint:
                address:
                  socket_address:
                    address: 127.0.0.1
                    port_value: 9080
```

##### 3.2.1.4.3EXTERNAL

listener-filter_chain_match-source_type-EXTERNAL.yaml

kubectl apply -f listener-filter_chain_match-source_type-EXTERNAL.yaml -n istio

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: listener
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
            port_value: 8083
        filter_chains:
        - filter_chain_match:
            destination_port: 8083
            source_type: EXTERNAL
          filters:
          - name: "envoy.filters.network.http_connection_manager"
            typed_config:
              "@type": "type.googleapis.com/envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager"
              stat_prefix: ingress_proxy
              route_config:
                name: route_a
                virtual_hosts:
                - name: envoy_cyz
                  domains:
                  - "*"
                  routes:
                  - match:
                      prefix: "/"
                    route:
                      cluster: cluster123
              http_filters:
              - name: "envoy.filters.http.router"
                typed_config:
                  "@type": "type.googleapis.com/envoy.extensions.filters.http.router.v3.Router"
  - applyTo: CLUSTER
    patch:
      operation: ADD
      value: 
        name: "cluster123"
        type: STATIC
        connect_timeout: 0.5s
        lb_policy: ROUND_ROBIN
        load_assignment:
          cluster_name: cluster123
          endpoints:
          - lb_endpoints:
            - endpoint:
                address:
                  socket_address:
                    address: 127.0.0.1
                    port_value: 9080
```

#### 3.2.1.5source_prefix_ranges

listener-filter_chain_match-source_prefix_ranges.yaml

kubectl apply -f listener-filter_chain_match-source_prefix_ranges.yaml -n istio

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: listener
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
            port_value: 8083
        filter_chains:
        - filter_chain_match:
            destination_port: 8083
            source_prefix_ranges:
            - address_prefix: 0.0.0.0
              prefix_len: 0
          filters:
          - name: "envoy.filters.network.http_connection_manager"
            typed_config:
              "@type": "type.googleapis.com/envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager"
              stat_prefix: ingress_proxy
              route_config:
                name: route_a
                virtual_hosts:
                - name: envoy_cyz
                  domains:
                  - "*"
                  routes:
                  - match:
                      prefix: "/"
                    route:
                      cluster: cluster123
              http_filters:
              - name: "envoy.filters.http.router"
                typed_config:
                  "@type": "type.googleapis.com/envoy.extensions.filters.http.router.v3.Router"
  - applyTo: CLUSTER
    patch:
      operation: ADD
      value: 
        name: "cluster123"
        type: STATIC
        connect_timeout: 0.5s
        lb_policy: ROUND_ROBIN
        load_assignment:
          cluster_name: cluster123
          endpoints:
          - lb_endpoints:
            - endpoint:
                address:
                  socket_address:
                    address: 127.0.0.1
                    port_value: 9080
```

#### 3.2.1.5source_ports

listener-filter_chain_match-source_ports.yaml

kubectl apply -f listener-filter_chain_match-source_ports.yaml -n istio

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: listener
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
            port_value: 8083
        filter_chains:
        - filter_chain_match:
            destination_port: 8083
            source_ports:
            - 8080
            - 30563
            - 80
            - 8083
            - 0
          filters:
          - name: "envoy.filters.network.http_connection_manager"
            typed_config:
              "@type": "type.googleapis.com/envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager"
              stat_prefix: ingress_proxy
              route_config:
                name: route_a
                virtual_hosts:
                - name: envoy_cyz
                  domains:
                  - "*"
                  routes:
                  - match:
                      prefix: "/"
                    route:
                      cluster: cluster123
              http_filters:
              - name: "envoy.filters.http.router"
                typed_config:
                  "@type": "type.googleapis.com/envoy.extensions.filters.http.router.v3.Router"
  - applyTo: CLUSTER
    patch:
      operation: ADD
      value: 
        name: "cluster123"
        type: STATIC
        connect_timeout: 0.5s
        lb_policy: ROUND_ROBIN
        load_assignment:
          cluster_name: cluster123
          endpoints:
          - lb_endpoints:
            - endpoint:
                address:
                  socket_address:
                    address: 127.0.0.1
                    port_value: 9080
```

source port动态生成的，不清楚具体用哪个



#### 3.2.1.6server_names

listener-filter_chain_match-server_names.yaml

kubectl apply -f listener-filter_chain_match-server_names.yaml -n istio

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: listener
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
            port_value: 8083
        listener_filters:
          - name: envoy.filters.listener.tls_inspector
            typedConfig:
              '@type': type.googleapis.com/envoy.extensions.filters.listener.tls_inspector.v3.TlsInspector
          - name: envoy.filters.listener.http_inspector
            typedConfig:
              '@type': type.googleapis.com/envoy.extensions.filters.listener.http_inspector.v3.HttpInspector
        filter_chains:
        - filter_chain_match:
            destination_port: 8083
            server_names:
            - 192.168.229.128
            - istio-envoy
            - outbound_.8083_._.productpage.istio.svc.cluster.local
          filters:
          - name: "envoy.filters.network.http_connection_manager"
            typed_config:
              "@type": "type.googleapis.com/envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager"
              stat_prefix: ingress_proxy
              route_config:
                name: route_a
                virtual_hosts:
                - name: envoy_cyz
                  domains:
                  - "*"
                  routes:
                  - match:
                      prefix: "/"
                    route:
                      cluster: cluster123
              http_filters:
              - name: "envoy.filters.http.router"
                typed_config:
                  "@type": "type.googleapis.com/envoy.extensions.filters.http.router.v3.Router"
  - applyTo: CLUSTER
    patch:
      operation: ADD
      value: 
        name: "cluster123"
        type: STATIC
        connect_timeout: 0.5s
        lb_policy: ROUND_ROBIN
        load_assignment:
          cluster_name: cluster123
          endpoints:
          - lb_endpoints:
            - endpoint:
                address:
                  socket_address:
                    address: 127.0.0.1
                    port_value: 9080
```

#### 3.2.1.7 transport_protocol

listener-filter_chain_match-transport_protocol.yaml

kubectl apply -f listener-filter_chain_match-transport_protocol.yaml -n istio

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: listener
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
            port_value: 8083
        filter_chains:
        - filter_chain_match:
            destination_port: 8083
            transport_protocol: raw_buffer
          filters:
          - name: "envoy.filters.network.http_connection_manager"
            typed_config:
              "@type": "type.googleapis.com/envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager"
              stat_prefix: ingress_proxy
              route_config:
                name: route_a
                virtual_hosts:
                - name: envoy_cyz
                  domains:
                  - "*"
                  routes:
                  - match:
                      prefix: "/"
                    route:
                      cluster: cluster123
              http_filters:
              - name: "envoy.filters.http.router"
                typed_config:
                  "@type": "type.googleapis.com/envoy.extensions.filters.http.router.v3.Router"
  - applyTo: CLUSTER
    patch:
      operation: ADD
      value: 
        name: "cluster123"
        type: STATIC
        connect_timeout: 0.5s
        lb_policy: ROUND_ROBIN
        load_assignment:
          cluster_name: cluster123
          endpoints:
          - lb_endpoints:
            - endpoint:
                address:
                  socket_address:
                    address: 127.0.0.1
                    port_value: 9080
```

#### 3.2.1.8 application_protocols

listener-filter_chain_match-application_protocols.yaml

kubectl apply -f listener-filter_chain_match-application_protocols.yaml -n istio

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: listener
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
            port_value: 8083
        listener_filters:
          - name: envoy.filters.listener.tls_inspector
            typedConfig:
              '@type': type.googleapis.com/envoy.extensions.filters.listener.tls_inspector.v3.TlsInspector
          - name: envoy.filters.listener.http_inspector
            typedConfig:
              '@type': type.googleapis.com/envoy.extensions.filters.listener.http_inspector.v3.HttpInspector
        filter_chains:
        - filter_chain_match:
            "transport_protocol": "raw_buffer"
            application_protocols:
            - "http/1.1"
            - "h2c"
          filters:
          - name: "envoy.filters.network.http_connection_manager"
            typed_config:
              "@type": "type.googleapis.com/envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager"
              stat_prefix: ingress_proxy
              route_config:
                name: route_a
                virtual_hosts:
                - name: envoy_cyz
                  domains:
                  - "*"
                  routes:
                  - match:
                      prefix: "/"
                    route:
                      cluster: cluster123
              http_filters:
              - name: "envoy.filters.http.router"
                typed_config:
                  "@type": "type.googleapis.com/envoy.extensions.filters.http.router.v3.Router"
  - applyTo: CLUSTER
    patch:
      operation: ADD
      value: 
        name: "cluster123"
        type: STATIC
        connect_timeout: 0.5s
        lb_policy: ROUND_ROBIN
        load_assignment:
          cluster_name: cluster123
          endpoints:
          - lb_endpoints:
            - endpoint:
                address:
                  socket_address:
                    address: 127.0.0.1
                    port_value: 9080
```





### 3.2.2filters

listener-filters.yaml

kubectl apply -f listener-filters.yaml -n istio

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: listener
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
            port_value: 8083
        filter_chains:
        - filter_chain_match:
            "transport_protocol": "raw_buffer"
          filters:
          - name: "envoy.filters.network.http_connection_manager"
            typed_config:
              "@type": "type.googleapis.com/envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager"
              stat_prefix: ingress_proxy
              route_config:
                name: route_a
                virtual_hosts:
                - name: envoy_cyz
                  domains:
                  - "*"
                  routes:
                  - match:
                      prefix: "/"
                    route:
                      cluster: cluster123
              http_filters:
              - name: "envoy.filters.http.router"
                typed_config:
                  "@type": "type.googleapis.com/envoy.extensions.filters.http.router.v3.Router"
  - applyTo: CLUSTER
    patch:
      operation: ADD
      value: 
        name: "cluster123"
        type: STATIC
        connect_timeout: 0.5s
        lb_policy: ROUND_ROBIN
        load_assignment:
          cluster_name: cluster123
          endpoints:
          - lb_endpoints:
            - endpoint:
                address:
                  socket_address:
                    address: 127.0.0.1
                    port_value: 9080
```



### 3.2.3transport_socket

listener-transport_socket.yaml

kubectl apply -f listener-transport_socket.yaml -n istio

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: listener
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
            port_value: 8083
        filter_chains:
        - filter_chain_match:
            "transport_protocol": "raw_buffer"
          filters:
          - name: "envoy.filters.network.http_connection_manager"
            typed_config:
              "@type": "type.googleapis.com/envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager"
              stat_prefix: ingress_proxy
              route_config:
                name: route_a
                virtual_hosts:
                - name: envoy_cyz
                  domains:
                  - "*"
                  routes:
                  - match:
                      prefix: "/"
                    route:
                      cluster: cluster123
              http_filters:
              - name: "envoy.filters.http.router"
                typed_config:
                  "@type": "type.googleapis.com/envoy.extensions.filters.http.router.v3.Router"
          transport_socket:
            name: envoy.transport_sockets.raw_buffer
            typed_config:
              "@type": type.googleapis.com/envoy.extensions.transport_sockets.raw_buffer.v3.RawBuffer
          transport_socket_connect_timeout: 10s
  - applyTo: CLUSTER
    patch:
      operation: ADD
      value: 
        name: "cluster123"
        type: STATIC
        connect_timeout: 0.5s
        lb_policy: ROUND_ROBIN
        load_assignment:
          cluster_name: cluster123
          endpoints:
          - lb_endpoints:
            - endpoint:
                address:
                  socket_address:
                    address: 127.0.0.1
                    port_value: 9080
```



## 3.3default_filter_chain

```
{
  "filter_chain_match": "{...}",过滤器链匹配条件
  "filters": [],网络过滤器配置
  "use_proxy_proto": "{...}",废弃
  "transport_socket": "{...}",传输socket配置
  "transport_socket_connect_timeout": "{...}"传输socket连接超时时间
}
```

listener-default_filter_chain.yaml

kubectl apply -f listener-default_filter_chain.yaml -n istio

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: listener
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
            port_value: 8083
        filter_chains:
        - filter_chain_match:
            "transport_protocol": "raw_buffer"
          filters:
          - name: "envoy.filters.network.http_connection_manager"
            typed_config:
              "@type": "type.googleapis.com/envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager"
              stat_prefix: ingress_proxy
              route_config:
                name: route_a
                virtual_hosts:
                - name: envoy_cyz
                  domains:
                  - "*"
                  routes:
                  - match:
                      prefix: "/"
                    route:
                      cluster: cluster123
              http_filters:
              - name: "envoy.filters.http.router"
                typed_config:
                  "@type": "type.googleapis.com/envoy.extensions.filters.http.router.v3.Router"
          transport_socket:
            name: envoy.transport_sockets.raw_buffer
            typed_config:
              "@type": type.googleapis.com/envoy.extensions.transport_sockets.raw_buffer.v3.RawBuffer
          transport_socket_connect_timeout: 10s
        default_filter_chain:
            name: PassthroughFilterChain
            filterChainMatch: {}
            filters:
            - name: istio.stats
              typedConfig:
                '@type': type.googleapis.com/udpa.type.v1.TypedStruct
                typeUrl: type.googleapis.com/envoy.extensions.filters.network.wasm.v3.Wasm
                value:
                  config:
                    configuration:
                      '@type': type.googleapis.com/google.protobuf.StringValue
                      value: |
                        {
                          "debug": "false",
                          "stat_prefix": "istio"
                        }
                    root_id: stats_outbound
                    vm_config:
                      code:
                        local:
                          inline_string: envoy.wasm.stats
                      runtime: envoy.wasm.runtime.null
                      vm_id: tcp_stats_outbound
            - name: envoy.filters.network.tcp_proxy
              typedConfig:
                '@type': type.googleapis.com/envoy.extensions.filters.network.tcp_proxy.v3.TcpProxy
                accessLog:
                - name: envoy.access_loggers.file
                  typedConfig:
                    '@type': type.googleapis.com/envoy.extensions.access_loggers.file.v3.FileAccessLog
                    logFormat:
                      textFormat: |
                        [%START_TIME%] "%REQ(:METHOD)% %REQ(X-ENVOY-ORIGINAL-PATH?:PATH)% %PROTOCOL%" %RESPONSE_CODE% %RESPONSE_FLAGS% %RESPONSE_CODE_DETAILS% %CONNECTION_TERMINATION_DETAILS% "%UPSTREAM_TRANSPORT_FAILURE_REASON%" %BYTES_RECEIVED% %BYTES_SENT% %DURATION% %RESP(X-ENVOY-UPSTREAM-SERVICE-TIME)% "%REQ(X-FORWARDED-FOR)%" "%REQ(USER-AGENT)%" "%REQ(X-REQUEST-ID)%" "%REQ(:AUTHORITY)%" "%UPSTREAM_HOST%" %UPSTREAM_CLUSTER% %UPSTREAM_LOCAL_ADDRESS% %DOWNSTREAM_LOCAL_ADDRESS% %DOWNSTREAM_REMOTE_ADDRESS% %REQUESTED_SERVER_NAME% %ROUTE_NAME%
                    path: /dev/stdout
                cluster: PassthroughCluster
                statPrefix: PassthroughCluster
  - applyTo: CLUSTER
    patch:
      operation: ADD
      value: 
        name: "cluster123"
        type: STATIC
        connect_timeout: 0.5s
        lb_policy: ROUND_ROBIN
        load_assignment:
          cluster_name: cluster123
          endpoints:
          - lb_endpoints:
            - endpoint:
                address:
                  socket_address:
                    address: 127.0.0.1
                    port_value: 9080
```

## 3.4metadata

listener-metadata.yaml

kubectl apply -f listener-metadata.yaml -n istio

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: listener
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
            port_value: 8083
        metadata:
          filter_metadata:
            envoy.lb:
              test: test
        filter_chains:
        - filter_chain_match:
            "transport_protocol": "raw_buffer"
          filters:
          - name: "envoy.filters.network.http_connection_manager"
            typed_config:
              "@type": "type.googleapis.com/envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager"
              stat_prefix: ingress_proxy
              route_config:
                name: route_a
                virtual_hosts:
                - name: envoy_cyz
                  domains:
                  - "*"
                  routes:
                  - match:
                      prefix: "/"
                    route:
                      cluster: cluster123
              http_filters:
              - name: envoy.filters.http.rbac
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
                              - key: test
                              value: 
                                string_match:
                                  exact: "test"
                          principals:
                          - any: true
              - name: "envoy.filters.http.router"
                typed_config:
                  "@type": "type.googleapis.com/envoy.extensions.filters.http.router.v3.Router"
  - applyTo: CLUSTER
    patch:
      operation: ADD
      value: 
        name: "cluster123"
        type: STATIC
        connect_timeout: 0.5s
        lb_policy: ROUND_ROBIN
        load_assignment:
          cluster_name: cluster123
          endpoints:
          - lb_endpoints:
            - endpoint:
                address:
                  socket_address:
                    address: 127.0.0.1
                    port_value: 9080
```

## 3.5listener_filters

listener-listener_filters.yaml

kubectl apply -f listener-listener_filters.yaml -n istio

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: listener
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
            port_value: 8083
        metadata:
          filter_metadata:
            envoy.lb:
              test: test
        listener_filters:
          - name: envoy.filters.listener.tls_inspector
            typedConfig:
              '@type': type.googleapis.com/envoy.extensions.filters.listener.tls_inspector.v3.TlsInspector
          - name: envoy.filters.listener.http_inspector
            typedConfig:
              '@type': type.googleapis.com/envoy.extensions.filters.listener.http_inspector.v3.HttpInspector
        filter_chains:
        - filter_chain_match:
            "transport_protocol": "raw_buffer"
          filters:
          - name: "envoy.filters.network.http_connection_manager"
            typed_config:
              "@type": "type.googleapis.com/envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager"
              stat_prefix: ingress_proxy
              route_config:
                name: route_a
                virtual_hosts:
                - name: envoy_cyz
                  domains:
                  - "*"
                  routes:
                  - match:
                      prefix: "/"
                    route:
                      cluster: cluster123
              http_filters:
              - name: "envoy.filters.http.router"
                typed_config:
                  "@type": "type.googleapis.com/envoy.extensions.filters.http.router.v3.Router"
  - applyTo: CLUSTER
    patch:
      operation: ADD
      value: 
        name: "cluster123"
        type: STATIC
        connect_timeout: 0.5s
        lb_policy: ROUND_ROBIN
        load_assignment:
          cluster_name: cluster123
          endpoints:
          - lb_endpoints:
            - endpoint:
                address:
                  socket_address:
                    address: 127.0.0.1
                    port_value: 9080
```

## 3.6traffic_direction

Identifies the direction of the traffic relative to the local Envoy.

- UNSPECIFIED

  *(DEFAULT)* ⁣Default option is unspecified.

- INBOUND

  ⁣The transport is used for incoming traffic.

- OUTBOUND

  ⁣The transport is used for outgoing traffic.

### 3.6.1UNSPECIFIED

listener-traffic_direction-UNSPECIFIED.yaml

kubectl apply -f listener-traffic_direction-UNSPECIFIED.yaml -n istio

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: listener
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
            port_value: 8083
        metadata:
          filter_metadata:
            envoy.lb:
              test: test
        traffic_direction: UNSPECIFIED
        listener_filters:
          - name: envoy.filters.listener.tls_inspector
            typedConfig:
              '@type': type.googleapis.com/envoy.extensions.filters.listener.tls_inspector.v3.TlsInspector
          - name: envoy.filters.listener.http_inspector
            typedConfig:
              '@type': type.googleapis.com/envoy.extensions.filters.listener.http_inspector.v3.HttpInspector
        filter_chains:
        - filter_chain_match:
            "transport_protocol": "raw_buffer"
          filters:
          - name: "envoy.filters.network.http_connection_manager"
            typed_config:
              "@type": "type.googleapis.com/envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager"
              stat_prefix: ingress_proxy
              route_config:
                name: route_a
                virtual_hosts:
                - name: envoy_cyz
                  domains:
                  - "*"
                  routes:
                  - match:
                      prefix: "/"
                    route:
                      cluster: cluster123
              http_filters:
              - name: "envoy.filters.http.router"
                typed_config:
                  "@type": "type.googleapis.com/envoy.extensions.filters.http.router.v3.Router"
  - applyTo: CLUSTER
    patch:
      operation: ADD
      value: 
        name: "cluster123"
        type: STATIC
        connect_timeout: 0.5s
        lb_policy: ROUND_ROBIN
        load_assignment:
          cluster_name: cluster123
          endpoints:
          - lb_endpoints:
            - endpoint:
                address:
                  socket_address:
                    address: 127.0.0.1
                    port_value: 9080
```



### 3.6.2INBOUND

listener-traffic_direction-INBOUND.yaml

kubectl apply -f listener-traffic_direction-INBOUND.yaml -n istio

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: listener
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
            port_value: 8083
        metadata:
          filter_metadata:
            envoy.lb:
              test: test
        traffic_direction: INBOUND
        listener_filters:
          - name: envoy.filters.listener.tls_inspector
            typedConfig:
              '@type': type.googleapis.com/envoy.extensions.filters.listener.tls_inspector.v3.TlsInspector
          - name: envoy.filters.listener.http_inspector
            typedConfig:
              '@type': type.googleapis.com/envoy.extensions.filters.listener.http_inspector.v3.HttpInspector
        filter_chains:
        - filter_chain_match:
            "transport_protocol": "raw_buffer"
          filters:
          - name: "envoy.filters.network.http_connection_manager"
            typed_config:
              "@type": "type.googleapis.com/envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager"
              stat_prefix: ingress_proxy
              route_config:
                name: route_a
                virtual_hosts:
                - name: envoy_cyz
                  domains:
                  - "*"
                  routes:
                  - match:
                      prefix: "/"
                    route:
                      cluster: cluster123
              http_filters:
              - name: "envoy.filters.http.router"
                typed_config:
                  "@type": "type.googleapis.com/envoy.extensions.filters.http.router.v3.Router"
  - applyTo: CLUSTER
    patch:
      operation: ADD
      value: 
        name: "cluster123"
        type: STATIC
        connect_timeout: 0.5s
        lb_policy: ROUND_ROBIN
        load_assignment:
          cluster_name: cluster123
          endpoints:
          - lb_endpoints:
            - endpoint:
                address:
                  socket_address:
                    address: 127.0.0.1
                    port_value: 9080
```



### 3.6.3OUTBOUND

listener-traffic_direction-OUTBOUND.yaml

kubectl apply -f listener-traffic_direction-OUTBOUND.yaml -n istio

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: listener
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
            port_value: 8083
        metadata:
          filter_metadata:
            envoy.lb:
              test: test
        traffic_direction: OUTBOUND
        listener_filters:
          - name: envoy.filters.listener.tls_inspector
            typedConfig:
              '@type': type.googleapis.com/envoy.extensions.filters.listener.tls_inspector.v3.TlsInspector
          - name: envoy.filters.listener.http_inspector
            typedConfig:
              '@type': type.googleapis.com/envoy.extensions.filters.listener.http_inspector.v3.HttpInspector
        filter_chains:
        - filter_chain_match:
            "transport_protocol": "raw_buffer"
          filters:
          - name: "envoy.filters.network.http_connection_manager"
            typed_config:
              "@type": "type.googleapis.com/envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager"
              stat_prefix: ingress_proxy
              route_config:
                name: route_a
                virtual_hosts:
                - name: envoy_cyz
                  domains:
                  - "*"
                  routes:
                  - match:
                      prefix: "/"
                    route:
                      cluster: cluster123
              http_filters:
              - name: "envoy.filters.http.router"
                typed_config:
                  "@type": "type.googleapis.com/envoy.extensions.filters.http.router.v3.Router"
  - applyTo: CLUSTER
    patch:
      operation: ADD
      value: 
        name: "cluster123"
        type: STATIC
        connect_timeout: 0.5s
        lb_policy: ROUND_ROBIN
        load_assignment:
          cluster_name: cluster123
          endpoints:
          - lb_endpoints:
            - endpoint:
                address:
                  socket_address:
                    address: 127.0.0.1
                    port_value: 9080
```

## 3.7 access_log 

listener-access_log.yaml

kubectl apply -f listener-access_log.yaml -n istio

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: listener
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
            port_value: 8083
        metadata:
          filter_metadata:
            envoy.lb:
              test: test
        traffic_direction: OUTBOUND
        access_log:
          - filter:
              responseFlagFilter:
                flags:
                - NR
            name: envoy.access_loggers.file
            typedConfig:
              '@type': type.googleapis.com/envoy.extensions.access_loggers.file.v3.FileAccessLog
              logFormat:
                textFormat: |
                  [%START_TIME%] "%REQ(:METHOD)% %REQ(X-ENVOY-ORIGINAL-PATH?:PATH)% %PROTOCOL%" %RESPONSE_CODE% %RESPONSE_FLAGS% %RESPONSE_CODE_DETAILS% %CONNECTION_TERMINATION_DETAILS% "%UPSTREAM_TRANSPORT_FAILURE_REASON%" %BYTES_RECEIVED% %BYTES_SENT% %DURATION% %RESP(X-ENVOY-UPSTREAM-SERVICE-TIME)% "%REQ(X-FORWARDED-FOR)%" "%REQ(USER-AGENT)%" "%REQ(X-REQUEST-ID)%" "%REQ(:AUTHORITY)%" "%UPSTREAM_HOST%" %UPSTREAM_CLUSTER% %UPSTREAM_LOCAL_ADDRESS% %DOWNSTREAM_LOCAL_ADDRESS% %DOWNSTREAM_REMOTE_ADDRESS% %REQUESTED_SERVER_NAME% %ROUTE_NAME%
              path: /dev/stdout
        listener_filters:
          - name: envoy.filters.listener.tls_inspector
            typedConfig:
              '@type': type.googleapis.com/envoy.extensions.filters.listener.tls_inspector.v3.TlsInspector
          - name: envoy.filters.listener.http_inspector
            typedConfig:
              '@type': type.googleapis.com/envoy.extensions.filters.listener.http_inspector.v3.HttpInspector
        filter_chains:
        - filter_chain_match:
            "transport_protocol": "raw_buffer"
          filters:
          - name: "envoy.filters.network.http_connection_manager"
            typed_config:
              "@type": "type.googleapis.com/envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager"
              stat_prefix: ingress_proxy
              route_config:
                name: route_a
                virtual_hosts:
                - name: envoy_cyz
                  domains:
                  - "*"
                  routes:
                  - match:
                      prefix: "/"
                    route:
                      cluster: cluster123
              http_filters:
              - name: "envoy.filters.http.router"
                typed_config:
                  "@type": "type.googleapis.com/envoy.extensions.filters.http.router.v3.Router"
  - applyTo: CLUSTER
    patch:
      operation: ADD
      value: 
        name: "cluster123"
        type: STATIC
        connect_timeout: 0.5s
        lb_policy: ROUND_ROBIN
        load_assignment:
          cluster_name: cluster123
          endpoints:
          - lb_endpoints:
            - endpoint:
                address:
                  socket_address:
                    address: 127.0.0.1
                    port_value: 9080
```

## 3.8general

listener-general.yaml

kubectl apply -f listener-general.yaml -n istio

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: listener
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
            port_value: 8083
        use_original_dst: true
        per_connection_buffer_limit_bytes: 1024000
        drain_type: DEFAULT
        listener_filters_timeout: 15s
        continue_on_listener_filters_timeout: true
        transparent: true
        freebind: true
        tcp_fast_open_queue_length: 1024
        connection_balance_config:
          exact_balance: {}
        #enable_reuse_port: true
        tcp_backlog_size: 128
        bind_to_port: true
        filter_chains:
        - filters:
          - name: "envoy.filters.network.http_connection_manager"
            typed_config:
              "@type": "type.googleapis.com/envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager"
              stat_prefix: ingress_proxy
              route_config:
                name: route_a
                virtual_hosts:
                - name: envoy_cyz
                  domains:
                  - "*"
                  routes:
                  - match:
                      prefix: "/"
                    route:
                      cluster: cluster123
              http_filters:
              - name: "envoy.filters.http.router"
                typed_config:
                  "@type": "type.googleapis.com/envoy.extensions.filters.http.router.v3.Router"
  - applyTo: CLUSTER
    patch:
      operation: ADD
      value: 
        name: "cluster123"
        type: STATIC
        connect_timeout: 0.5s
        lb_policy: ROUND_ROBIN
        load_assignment:
          cluster_name: cluster123
          endpoints:
          - lb_endpoints:
            - endpoint:
                address:
                  socket_address:
                    address: 127.0.0.1
                    port_value: 9080
```

