# 1http历史

发布的第一个 HTTP 版本是 HTTP/0.9。Tim Berners-Lee 于 1989 年创建了它，并于 1991 年将其命名为 HTTP/0.9。

HTTP/0.9 功能是有限的，只能做基本的事情。除了网页之外，它无法返回任何内容，并且不支持 cookie 和其他现代功能。

1996 年，HTTP/1.0 发布，带来了新功能，如 POST 请求和发送网页以外的内容的能力。

但是，与今天相比，还有很长的路要走。

HTTP / 1.1在1997年发布，并进行了两次修订，一次是在1999年，一次是在2007年。

它带来了许多主要的新功能，例如cookie和连接仍然存在。

最后，在 2015 年，HTTP/2 发布并允许提高性能，使诸如服务器发送事件和一次发送多个请求的能力成为可能。

HTTP/2 仍然是新的，只有不到一半的网站使用。

HTTP/3：最新版本的 HTTP

HTTP/3或HTTP over QUIC，改变了HTTP很多。

HTTP 传统上是通过 TCP（传输控制协议）完成的。但是，TCP于1974年互联网开始发展。当 TCP 最初创建时，它的作者无法预测网络的增长。

由于 TCP 已过时，因此 TCP 在一段时间内限制了 HTTP 的速度和安全性。

现在，由于 HTTP/3，HTTP 不再受限制。HTTP/3 没有使用 TCP，而是使用了一种由 Google 于 2012 年开发的新协议，称为 QUIC（发音为“quick”）。

这为 HTTP 引入了许多新功能。

# 2什么是http3

HTTP3 是 HTTP2 的复用和压缩，协议从 TCP 更改为 UDP。然后，谷歌的那些人在协议中添加了他们做的层，以确保稳定性、数据包接收顺序及安全性。

HTTP3 在保持 QUIC 稳定性的同时使用 UDP 来实现高速度，同时又不会牺牲 TLS 的安全性。是的，在 QUIC 中就有 TLS1.3，你可以用它发起优雅的 SSL。

2018 年，QUIC 演变成为 HTTP3。互联网工程任务组(Internet Engineerring Task Force)的那帮制定互联网协议的哥们同意了这个提案。这是个好消息，因为对于我们这些急躁的人们来说，互联网的速度永远都不够快

# 3什么是quic

QUIC（Quick UDP Internet Connections）是一种实验性传输层网络协议，提供与TLS/SSL相当的安全性，同时具有更低的连接和传输延迟。QUIC基于UDP，因此拥有极佳的弱网性能，在丢包和网络延迟严重的情况下仍可提供可用的服务。QUIC在应用程序层面就能实现不同的拥塞控制算法，不需要操作系统和内核支持，这相比于传统的TCP协议，拥有了更好的改造灵活性，非常适合在TCP协议优化遇到瓶颈的业务。

# 4http各版本架构

![http](30images\http.webp)

# 5http3案例

curl支持http3https://curl.se/docs/http3.html



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



## 5.1httpConnectionManager

ef-httpconnectionmanager-http3_protocol_options.yaml

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: http3
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
                require_client_certificate: 
        traffic_direction: "OUTBOUND"
        udp_listener_config:
          downstream_socket_config: { }
          quic_options: { }
        reuse_port: true
```



## 5.2udp_listener_config

### 5.2.1quic_options

ef-udp_listener_config-quic_options.yaml

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: http3
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
              http3_protocol_options: {}
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
                require_client_certificate: 
        traffic_direction: "OUTBOUND"
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
        reuse_port: true
```

### 5.2.2downstream_socket_config

ef-udp_listener_config-downstream_socket_config.yaml

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: http3
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
              http3_protocol_options: {}
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
                require_client_certificate: 
        traffic_direction: "OUTBOUND"
        udp_listener_config:
          downstream_socket_config: 
            max_rx_datagram_size: 1500
            prefer_gro: true
          quic_options: {}
        reuse_port: true
```

## 5.3cluster

### 5.3.1explicit_http_config

ef-cluster-explicit_http_config.yaml

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: http3
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
                    route:
                      cluster: productpage_cluster
                    response_headers_to_add:
                    - header:
                        key: "alt-svc"
                        value: "h3=:443; ma=86400"
                      append: true
              http3_protocol_options: {}
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
                require_client_certificate: 
        traffic_direction: "OUTBOUND"
        udp_listener_config:
          downstream_socket_config: 
            max_rx_datagram_size: 1500
            prefer_gro: true
          quic_options: {}
        reuse_port: true
  configPatches:
    - applyTo: CLUSTER
      patch:
        operation: ADD
        value:
          name: productpage_cluster
          type: STRICT_DNS
          connect_timeout: 10s
          lb_policy: ROUND_ROBIN
          typed_extension_protocol_options: 
            envoy.extensions.upstreams.http.v3.HttpProtocolOptions:
                "@type": type.googleapis.com/envoy.extensions.upstreams.http.v3.HttpProtocolOptions
                upstream_http_protocol_options:
                  auto_sni: true
                common_http_protocol_options:
                  idle_timeout: 1s
                explicit_http_config:
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
          load_assignment:
            cluster_name: productpage_cluster
            endpoints:
            - lb_endpoints:
              - endpoint:
                  address:
                     socket_address:
                      address: productpage.istio.svc.cluster.local
                      port_value: 9080
```

### 5.3.2QuicUpstreamTransport

ef-cluster-QuicUpstreamTransport.yaml

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: http3
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
            port_value: 8080
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
                  domains: ["*"]
                  routes:
                  - match:
                      prefix: "/"
                    route:
                      host_rewrite_literal: www.google.com
                      cluster: service_google
              http_filters:
              - name: envoy.filters.http.router
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
                        address: www.google.com
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
                  sni: www.google.com
```

