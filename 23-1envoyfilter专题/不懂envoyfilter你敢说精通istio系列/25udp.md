# 1什么是udp

udp是一个没有连接的传输层协议。这里的udp配置主要有两处，一个是listener_filter的udp_proxy和dns_filter，还有一个是listener配置。listener配置分为UdpListenerConfig和ActiveRawUdpListenerConfig。

# 2配置

## 2.1udp_proxy

```
{
  "stat_prefix": "...",stat前缀
  "cluster": "...",目标集群名称
  "idle_timeout": "{...}",空闲超时时间，默认1分钟
  "use_original_src_ip": "...",是否使用原始来源ip
  "hash_policies": [],hash策略
  "upstream_socket_config": "{...}",上游套接字配置
  "use_per_packet_load_balancing": "...",是否使用每个包的负载均衡配置，默认false
  "access_log": []日志配置
}
```

hash_policies：

```
{
  "source_ip": "...",原地址hash
  "key": "..."关键字hash
}
```

upstream_socket_config：

```
{
  "max_rx_datagram_size": "{...}",最大接收数据包大小
  "prefer_gro": "{...}"  Generic Receive Offload 
}
```

access_log：

```
{
  "name": "...", 日志插件名称	
  "filter": "{...}",过滤条件
  "typed_config": "{...}"日志配置
}
```

## 2.2dns_filter

```
{
  "stat_prefix": "...",stat前缀
  "server_config": "{...}",服务器配置
  "client_config": "{...}"客户端配置，控制envoy连接dns设备行为
}
```

server_config：

```
{
  "inline_dns_table": "{...}",行内dns表配置
  "external_dns_table": "{...}"外部dns表配置
}
```

inline_dns_table：

```
{
  "external_retry_count": "...",尝试dns服务器的重试次数
  "virtual_domains": [],dns服务器域名
  "known_suffixes": []废弃
}
```

virtual_domains：

```
{
  "name": "...",域名
  "endpoint": "{...}",dns端点
  "answer_ttl": "{...}"答案有效期，默认300秒
}
```

endpoint：

```
{
  "address_list": "{...}",ip地址列表
  "cluster_name": "...",集群名称
  "service_list": "{...}"服务列表
}
```

service_list：

```
{
  "services": []服务
}
```

services：

```
{
  "service_name": "...",名称
  "protocol": "{...}",协议
  "ttl": "{...}",生存时间
  "targets": []目标列表
}
```

protocol：

```
{
  "number": "...",协议数字号
  "name": "..."协议名称
}
```

targets：

```
{
  "host_name": "...",主机名称
  "cluster_name": "...",集群名称
  "priority": "...",优先级
  "weight": "...",权重
  "port": "..."端口
}
```

known_suffixes：废弃

```
{
  "exact": "...",
  "prefix": "...",
  "suffix": "...",
  "safe_regex": "{...}",
  "contains": "...",
  "ignore_case": "..."
}
```



## 2.3UdpListenerConfig

```
{
  "downstream_socket_config": "{...}",下游socket配置
  "quic_options": "{...}"quic选项
}
```

downstream_socket_config：

```
{
  "max_rx_datagram_size": "{...}",最大接收数据包大小
  "prefer_gro": "{...}"generic revieve offload
}
```

quic_options：

```
{
  "quic_protocol_options": "{...}",quic协议选项
  "idle_timeout": "{...}",空闲超时时间
  "crypto_handshake_timeout": "{...}",握手超时时间
  "enabled": "{...}",是否启用
  "packets_to_read_to_connection_count_ratio": "{...}",每个事件循环读取包的数量，默认32
  "crypto_stream_config": "{...}",crypto流9配置
  "proof_source_config": "{...}"proof源配置
}
```

quic_protocol_options：

```
{
  "max_concurrent_streams": "{...}",最大并发流
  "initial_stream_window_size": "{...}",初始流窗口大小
  "initial_connection_window_size": "{...}",初始连接窗口大小
  "num_timeouts_to_trigger_port_migration": "{...}",触发端口转移的超时时间
  "connection_keepalive": "{...}"keepalive配置
}
```

connection_keepalive：

```
{
  "max_interval": "{...}",发送探测包最大间隔时间
  "initial_interval": "{...}"发送探测包的初始间隔时间+
}
```

## 2.4ActiveRawUdpListenerConfig

无

# 3案例

## 3.1udp_proxy

### 3.1.1upstream_socket_config

ef-udp_proxy-upstream_socket_config.yaml

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: udp
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
        udp_listener_config:
          downstream_socket_config: {}
          quic_options: { }
        listener_filters:
        - name: envoy.filters.udp_listener.udp_proxy
          typed_config:
            '@type': type.googleapis.com/envoy.extensions.filters.udp.udp_proxy.v3.UdpProxyConfig
            stat_prefix: service
            cluster: service_udp
            upstream_socket_config:
              max_rx_datagram_size: 9000
              prefer_gro: true
  - applyTo: CLUSTER
    patch:
      operation: ADD
      value:
        name: service_udp
        type: STATIC
        lb_policy: ROUND_ROBIN
        load_assignment:
          cluster_name: service_udp
          endpoints:
          - lb_endpoints:
            - endpoint:
                address:
                  socket_address:
                    address: 127.0.0.1
                    port_value: 1235
```

### 3.1.2hash_policies

#### 3.1.2.1source_ip

ef-udp_proxy-hash_policies-source_ip.yaml

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: udp
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
        udp_listener_config:
          downstream_socket_config: {}
          quic_options: { }
        listener_filters:
        - name: envoy.filters.udp_listener.udp_proxy
          typed_config:
            '@type': type.googleapis.com/envoy.extensions.filters.udp.udp_proxy.v3.UdpProxyConfig
            stat_prefix: service
            cluster: service_udp
            upstream_socket_config:
              max_rx_datagram_size: 9000
              prefer_gro: true
            hash_policies:
              source_ip: true
  - applyTo: CLUSTER
    patch:
      operation: ADD
      value:
        name: service_udp
        type: STATIC
        lb_policy: ROUND_ROBIN
        load_assignment:
          cluster_name: service_udp
          endpoints:
          - lb_endpoints:
            - endpoint:
                address:
                  socket_address:
                    address: 127.0.0.1
                    port_value: 1235
```



#### 3.1.2.2key

ef-udp_proxy-hash_policies-key.yaml

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: udp
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
        udp_listener_config:
          downstream_socket_config: {}
          quic_options: { }
        listener_filters:
        - name: envoy.filters.udp_listener.udp_proxy
          typed_config:
            '@type': type.googleapis.com/envoy.extensions.filters.udp.udp_proxy.v3.UdpProxyConfig
            stat_prefix: service
            cluster: service_udp
            upstream_socket_config:
              max_rx_datagram_size: 9000
              prefer_gro: true
            hash_policies:
              key: test
  - applyTo: CLUSTER
    patch:
      operation: ADD
      value:
        name: service_udp
        type: STATIC
        lb_policy: ROUND_ROBIN
        load_assignment:
          cluster_name: service_udp
          endpoints:
          - lb_endpoints:
            - endpoint:
                address:
                  socket_address:
                    address: 127.0.0.1
                    port_value: 1235
```



### 3.1.3access_log

ef-udp_proxy-access_log.yaml

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: udp
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
        udp_listener_config:
          downstream_socket_config: {}
          quic_options: { }
        listener_filters:
        - name: envoy.filters.udp_listener.udp_proxy
          typed_config:
            '@type': type.googleapis.com/envoy.extensions.filters.udp.udp_proxy.v3.UdpProxyConfig
            stat_prefix: service
            cluster: service_udp
            upstream_socket_config:
              max_rx_datagram_size: 9000
              prefer_gro: true
            access_log:
              - filter:
                  response_flag_filter:
                    flags:
                    - NR
                name: envoy.access_loggers.file
                typedConfig:
                  '@type': type.googleapis.com/envoy.extensions.access_loggers.file.v3.FileAccessLog
                  logFormat:
                    textFormat: |
                      [%START_TIME%] "%REQ(:METHOD)% %REQ(X-ENVOY-ORIGINAL-PATH?:PATH)% %PROTOCOL%"
                  path: /dev/stdout
  - applyTo: CLUSTER
    patch:
      operation: ADD
      value:
        name: service_udp
        type: STATIC
        lb_policy: ROUND_ROBIN
        load_assignment:
          cluster_name: service_udp
          endpoints:
          - lb_endpoints:
            - endpoint:
                address:
                  socket_address:
                    address: 127.0.0.1
                    port_value: 1235
```



### 3.1.4other

ef-udp_proxy-other.yaml

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: udp
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
        udp_listener_config:
          downstream_socket_config: {}
          quic_options: { }
        listener_filters:
        - name: envoy.filters.udp_listener.udp_proxy
          typed_config:
            '@type': type.googleapis.com/envoy.extensions.filters.udp.udp_proxy.v3.UdpProxyConfig
            stat_prefix: service
            cluster: service_udp
            upstream_socket_config:
              max_rx_datagram_size: 9000
              prefer_gro: true
            idle_timeout: 60s
            use_original_src_ip: true
            use_per_packet_load_balancing: true
  - applyTo: CLUSTER
    patch:
      operation: ADD
      value:
        name: service_udp
        type: STATIC
        lb_policy: ROUND_ROBIN
        load_assignment:
          cluster_name: service_udp
          endpoints:
          - lb_endpoints:
            - endpoint:
                address:
                  socket_address:
                    address: 127.0.0.1
                    port_value: 1235
```



## 3.2dns_filter

### 3.2.1inline_dns_table

ef-dns_filter-inline_dns_table.yaml

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: udp
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
        - name: envoy.filters.udp_listener.dns_filter
          typed_config:
            "@type": "type.googleapis.com/envoy.extensions.filters.udp.dns_filter.v3.DnsFilterConfig"
            stat_prefix: "dns_filter_prefix"
            client_config:
              resolution_timeout: 5s
              dns_resolution_config:
                dns_resolver_options:
                  use_tcp_for_dns_lookups: false
                  no_default_search_domain: false
                resolvers:
                - socket_address:
                    address: "8.8.8.8"
                    port_value: 53
                - socket_address:
                    address: "8.8.4.4"
                    port_value: 53
              max_pending_lookups: 256
            server_config:
              inline_dns_table:
                virtual_domains:
                  - name: "www.domain1.com"
                    endpoint:
                      address_list:
                        address:
                        - 10.0.0.1
                        - 10.0.0.2
                  - name: "www.domain2.com"
                    endpoint:
                      address_list:
                        address:
                          - 2001:8a:c1::2800:7
                  - name: "www.domain3.com"
                    endpoint:
                      address_list:
                        address:
                        - 10.0.3.1
                  - name: "www.domain4.com"
                    endpoint:
                      cluster_name: cluster_0
                  - name: "voip.domain5.com"
                    endpoint:
                      service_list:
                        services:
                          - service_name: "sip"
                            protocol: { number: 6 }
                            ttl: 86400s
                            targets:
                            - host_name: "primary.voip.domain5.com"
                              priority: 10
                              weight: 30
                              port: 5060
                            - host_name: "secondary.voip.domain5.com"
                              priority: 10
                              weight: 20
                              port: 5060
                            - host_name: "backup.voip.domain5.com"
                              priority: 10
                              weight: 10
                              port: 5060
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



### 3.2.2external_dns_table

ef-dns_filter-external_dns_table.yaml

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: udp
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
        - name: envoy.filters.udp_listener.dns_filter
          typed_config:
            '@type': 'type.googleapis.com/envoy.extensions.filters.udp.dns_filter.v3.DnsFilterConfig'
            stat_prefix: "my_prefix"
            server_config:
              external_dns_table:
                filename: "/home/ubuntu/configs/dns_table.json"
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

/home/ubuntu/configs/dns_table.json

```
{
  "virtual_domains": [
    {
      "name": "www.suffix1.com",
      "endpoint": {
        "address_list": {
          "address": [ "10.0.0.1", "10.0.0.2" ]
        }
      }
    },
    {
      "name": "www.suffix2.com",
      "endpoint": {
        "address_list": {
          "address": [ "2001:8a:c1::2800:7" ]
        }
      }
    }
  ]
}
```

可以先创建configmap，然后用注解挂载进去。

## 3.3UdpListenerConfig

### 3.3.1downstream_socket_config

ef-UdpListenerConfig-downstream_socket_config.yaml

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: udp
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
        udp_listener_config:
          downstream_socket_config: 
            max_rx_datagram_size: 1500
            prefer_gro: true
          quic_options: {}
        listener_filters:
        - name: envoy.filters.udp_listener.udp_proxy
          typed_config:
            '@type': type.googleapis.com/envoy.extensions.filters.udp.udp_proxy.v3.UdpProxyConfig
            stat_prefix: service
            cluster: service_udp
            upstream_socket_config:
              max_rx_datagram_size: 9000
              prefer_gro: true
  - applyTo: CLUSTER
    patch:
      operation: ADD
      value:
        name: service_udp
        type: STATIC
        lb_policy: ROUND_ROBIN
        load_assignment:
          cluster_name: service_udp
          endpoints:
          - lb_endpoints:
            - endpoint:
                address:
                  socket_address:
                    address: 127.0.0.1
                    port_value: 1235
```



### 3.3.2quic_options

ef-UdpListenerConfig-quic_options.yaml

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: udp
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
        udp_listener_config:
          downstream_socket_config: { }
          quic_options:
            enabled:
              default_value: true
              runtime_key: udp.quic
            quic_protocol_options:
              max_concurrent_streams: 100
              initial_stream_window_size: 65536 
              initial_connection_window_size: 65536 
              num_timeouts_to_trigger_port_migration: 1
              connection_keepalive:
                max_interval: 60s
                initial_interval: 30s
            idle_timeout: 300000ms
            crypto_handshake_timeout: 20000ms
            packets_to_read_to_connection_count_ratio: 32
            crypto_stream_config:
              name: envoy.quic.crypto_stream.server.quiche
              typed_config:
                "@type": "type.googleapis.com/envoy.extensions.quic.crypto_stream.v3.CryptoServerStreamConfig"
            proof_source_config:
              name: envoy.quic.proof_source.filter_chain
              typed_config:
                "@type": "type.googleapis.com/envoy.extensions.quic.proof_source.v3.ProofSourceConfig"
        listener_filters:
        - name: envoy.filters.udp_listener.udp_proxy
          typed_config:
            '@type': type.googleapis.com/envoy.extensions.filters.udp.udp_proxy.v3.UdpProxyConfig
            stat_prefix: service
            cluster: service_udp
            upstream_socket_config:
              max_rx_datagram_size: 9000
              prefer_gro: true
  - applyTo: CLUSTER
    patch:
      operation: ADD
      value:
        name: service_udp
        type: STATIC
        lb_policy: ROUND_ROBIN
        load_assignment:
          cluster_name: service_udp
          endpoints:
          - lb_endpoints:
            - endpoint:
                address:
                  socket_address:
                    address: 127.0.0.1
                    port_value: 1235
```



## 3.4ActiveRawUdpListenerConfig

略，不知在哪里配置
