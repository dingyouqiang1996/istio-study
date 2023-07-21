# 1什么是listener filter

listener filter中文名称是监听器过滤器的意思。

![架构](21image\架构.png)



从envoy架构图中可以看出listener filters是所有filter中，首先起作用的过滤器。它的主要作用有 检测协议、解析协议，通过它们解析出的信息被用于匹配 filter_chains 中的 filter 。

# 2有哪些listener filter

- [envoy.**filter**s.**listener**.http_inspector](https://www.envoyproxy.io/docs/envoy/latest/api-v3/extensions/filters/listener/http_inspector/v3/http_inspector.proto#extension-envoy-filters-listener-http-inspector)
- [envoy.**filter**s.**listener**.original_dst](https://www.envoyproxy.io/docs/envoy/latest/api-v3/extensions/filters/listener/original_dst/v3/original_dst.proto#extension-envoy-filters-listener-original-dst)
- [envoy.**filter**s.**listener**.original_src](https://www.envoyproxy.io/docs/envoy/latest/api-v3/extensions/filters/listener/original_src/v3/original_src.proto#extension-envoy-filters-listener-original-src)
- [envoy.**filter**s.**listener**.proxy_protocol](https://www.envoyproxy.io/docs/envoy/latest/api-v3/extensions/filters/listener/proxy_protocol/v3/proxy_protocol.proto#extension-envoy-filters-listener-proxy-protocol)
- [envoy.**filter**s.**listener**.tls_inspector](https://www.envoyproxy.io/docs/envoy/latest/api-v3/extensions/filters/listener/tls_inspector/v3/tls_inspector.proto#extension-envoy-filters-listener-tls-inspector)
- [envoy.**filter**s.udp_**listener**.dns_filter](https://www.envoyproxy.io/docs/envoy/latest/api-v3/extensions/filters/udp/dns_filter/v3/dns_filter.proto#extension-envoy-filters-udp-listener-dns-filter)
- [envoy.**filter**s.udp_**listener**.udp_proxy](https://www.envoyproxy.io/docs/envoy/latest/api-v3/extensions/filters/udp/udp_proxy/v3/udp_proxy.proto#extension-envoy-filters-udp-listener-udp-proxy)

**http_inspector:**

 判断应用层的数据是否使用 HTTP 协议，如果是，续继判断 HTTP 协议的版本号（HTTP 1.0、HTTP 1.1、HTTP 2） 

**original_dst:**

 用来读取 socket 的配置项 `SO_ORIGINAL_DST`，在使用 [透明代理模式](https://www.envoyproxy.io/docs/envoy/latest/api-v2/api/v2/lds.proto#envoy-api-field-listener-transparent) 时用到，在 envoy 中，用该 filter 获取报文的原始目地地址

**original_src**

 用于[透明代理](https://www.envoyproxy.io/docs/envoy/latest/api-v2/api/v2/lds.proto#envoy-api-field-listener-transparent)，让 uptream 看到的是请求端的 IP，双方均感知不到 envoy 的存在 。

[Original Source](https://www.envoyproxy.io/docs/envoy/latest/configuration/listener_filters/original_src_filter) 有点类似于 lvs 的 [DR 模式](http://www.linuxvirtualserver.org/VS-DRouting.html) ，假设 downstream 的 IP 是 10.1.2.3，envoy 的 IP 是 10.2.2.3。envoy 将报文转发给 upstream 时复用 downstream 的源 IP，upstream 看到的源 IP 是 downstream 的 IP 10.1.2.3，不是 envoy 的 IP 10.2.2.3。

与 lvs 的 [DR 模式](http://www.linuxvirtualserver.org/VS-DRouting.html) 区别是，在 lvs 中，upsteram 是直接将回应包发送给 downstream，而 envoy 的文档中强调，必须通过配置网络环境，让 uptream 的回应包发送到 envoy ，再由 envoy 转发。



原始地址的使用方式：

session粘性：基于ip的hash

安全策略：黑白名单设置

日志和stats



**proxy_protocol**

 解析代理协议，用该 filter 可以解析出真实的源 IP，已知支持 [HAProxy Proxy Protocol](https://www.haproxy.org/download/1.9/doc/proxy-protocol.txt) 

**tls_inspector**

 用来判断是否使用 TLS 协议，如果是 TLS 协议，解析出 Server Name、Negotiation 信息，解析出来的信息用于 FilterChain 的匹配。 

**dns_filter**

dns过滤器允许envoy执行通过一个授权的服务器查询dns，他可以指定dns服务器，也可以静态指定dns，或者通过外部文件设置静态dns。

**udp_proxy**

enovy通过这个filter实现一个非透明的udp代理。缺少透明指的是上游服务会看到envoy的源ip和端口而不是客户端的。所有的数据包，从客户端到envoy到上游，回来，再从上游到envoy到客户端。使用[use_original_src_ip](https://www.envoyproxy.io/docs/envoy/latest/api-v3/extensions/filters/udp/udp_proxy/v3/udp_proxy.proto#envoy-v3-api-msg-extensions-filters-udp-udp-proxy-v3-udpproxyconfig) 字段，udp proxy可以实现透明代理，但是他只代理ip，不透明代理端口。



# 3实战

## 3.1http_inspector

listener-filter-http_inspector.yaml

kubectl apply -f listener-filter-http_inspector.yaml -n istio

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

添加vs

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

kubectl apply -f gateway-01.yaml -n istio

gateway-01.yaml

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



## 3.2original_dst

listener-filter-original_dst.yaml

kubectl apply -f listener-filter-original_dst.yaml -n istio

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
        - name: envoy.filters.listener.original_dst
          typed_config:
            "@type": type.googleapis.com/envoy.extensions.filters.listener.original_dst.v3.OriginalDst
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

添加vs

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

kubectl apply -f gateway-01.yaml -n istio

gateway-01.yaml

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



## 3.3original_src

ingress-productpage.yaml

kubectl apply -f ingress-productpage.yaml -n istio

```
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: productpage
  annotations:
    nginx.ingress.kubernetes.io/service-upstream: "true"
    nginx.ingress.kubernetes.io/upstream-vhost: productpage.istio.svc.cluster.local
spec:
  rules:
  - http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: productpage
            port:
              number: 8083

```



listener-filter-original_src.yaml

kubectl apply -f listener-filter-original_src.yaml -n istio

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
        - name: envoy.filters.listener.original_src
          typed_config:
            "@type": type.googleapis.com/envoy.extensions.filters.listener.original_src.v3.OriginalSrc
            mark: 123
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

添加vs

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

kubectl apply -f gateway-01.yaml -n istio

gateway-01.yaml

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



## 3.4proxy_protocol

1配置haproxy

```
listen nginx
        mode tcp
        bind *:82
        balance roundrobin
        server nginx-01 192.168.229.128:30563 send-proxy-v2 weight 1 maxconn 10000 check inter 10s
```

ef-listenter-filter-ingressgateway.yaml

kubectl apply -f ef-listenter-filter-ingressgateway.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: listener
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
      operation: MERGE
      value:
        listener_filters:
        - name: envoy.filters.listener.proxy_protocol
          typed_config:
            "@type": type.googleapis.com/envoy.extensions.filters.listener.proxy_protocol.v3.ProxyProtocol           
```

访问：

http://192.168.229.128:82/productpage

## 3.5tls_inspector

listener-filter-tls_inspector.yaml

kubectl apply -f listener-filter-tls_inspector.yaml -n istio

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

添加vs

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

kubectl apply -f gateway-01.yaml -n istio

gateway-01.yaml

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



## 3.6dns_filter

Didn't find a registered implementation for name: 'envoy.filters.udp_listener.dns_filter''

istio不支持

### 3.6.1client_config，server_config

listener-filters-dns_filter.yaml

kubectl apply -f listener-filters-dns_filter.yaml -n istio

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
        - name: envoy.filters.udp_listener.dns_filter
          typed_config:
            "@type": "type.googleapis.com/envoy.extensions.filters.udp.dns_filter.v3.DnsFilterConfig"
            stat_prefix: "dns_filter_prefix"
            client_config:
              #resolution_timeout: 5s
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
                        - 127.0.0.1
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
        type: STRICT_DNS
        connect_timeout: 0.5s
        lb_policy: ROUND_ROBIN
        load_assignment:
          cluster_name: cluster123
          endpoints:
          - lb_endpoints:
            - endpoint:
                address:
                  socket_address:
                    address: www.domain1.com
                    port_value: 9080
```

添加vs

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

kubectl apply -f gateway-01.yaml -n istio

gateway-01.yaml

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



### 3.6.2external_dns_table

listener-filters-dns_filter.yaml

kubectl apply -f listener-filters-dns_filter.yaml -n istio

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





## 3.7udp_proxy

deploy-udp.yaml

kubectl apply -f deploy-udp.yaml -n istio

```
apiVersion: v1
kind: Service
metadata:
  name: udp
spec:
  ports:
  - port: 5005
    protocol: UDP
    name: udp
  selector:
    app: udp
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: udp-v1
  labels:
    app: udp
    version: v1
spec:
  replicas: 1
  selector:
    matchLabels:
      app: udp
      version: v1
  template:
    metadata:
      labels:
        app: udp
        version: v1
    spec:
      containers:
      - name: udp
        image: mendhak/udp-listener
        imagePullPolicy: IfNotPresent
        ports:
        - containerPort: 5005
---
```





listener-filters-udp_proxy.yaml

kubectl apply -f listener-filters-udp_proxy.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: listener
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
        name: proxy-udp
        address:
          socket_address:
            protocol: UDP
            address: 0.0.0.0
            port_value: 31400
        udp_listener_config:
          downstream_socket_config:
            max_rx_datagram_size: 9000
        listener_filters:
        - name: envoy.filters.udp_listener.udp_proxy
          typed_config:
            '@type': type.googleapis.com/envoy.extensions.filters.udp.udp_proxy.v3.UdpProxyConfig
            stat_prefix: service
            cluster: service_udp
            upstream_socket_config:
              max_rx_datagram_size: 9000
```

