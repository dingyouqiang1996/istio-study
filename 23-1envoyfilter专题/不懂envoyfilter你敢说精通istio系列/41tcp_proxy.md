# 1什么是tcp_proxy

tcp_proxy是envoy用来处理tcp协议的一个network类型的filter。它和http_connection_manager一样是envoy中两个最重要的network filter。名称为 *envoy.filters.network.tcp_proxy* 。

# 2配置 

```
{
  "stat_prefix": "...",stat前缀
  "cluster": "...",cluster名称
  "weighted_clusters": "{...}",加权cluster配置
  "metadata_match": "{...}",匹配元数据
  "idle_timeout": "{...}",空闲超时时间，默认1h
  "access_log": [],访问日志
  "max_connect_attempts": "{...}",最大失败尝试连接次数，默认1
  "hash_policy": [],hash策略
  "tunneling_config": "{...}",隧道配置
  "max_downstream_connection_duration": "{...}"最大下游连接时间
}
```

weighted_clusters：

```
{
  "clusters": []集群配置
}
```

clusters：

```
{
  "name": "...",集群名称
  "weight": "...",权重
  "metadata_match": "{...}"匹配元数据
}
```

access_log：

```
{
  "name": "...",名称
  "filter": "{...}",过滤
  "typed_config": "{...}"配置
}
```

hash_policy：

```
{
  "source_ip": "{...}"计算hash时使用源ip
}
```

tunneling_config：

```
{
  "hostname": "...",主机名
  "use_post": "...",是否使用post方法
  "headers_to_add": []额外添加的头
}
```

# 3实战

## 3.1准备工作

部署tcp echo

tcp-echo-services.yaml

kubectl apply -f tcp-echo-services.yaml -n istio

```
apiVersion: v1
kind: Service
metadata:
  name: tcp-echo
  labels:
    app: tcp-echo
    service: tcp-echo
spec:
  ports:
  - name: tcp
    port: 9000
  - name: tcp-other
    port: 9001
  # Port 9002 is omitted intentionally for testing the pass through filter chain.
  selector:
    app: tcp-echo
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: tcp-echo-v1
  labels:
    app: tcp-echo
    version: v1
spec:
  replicas: 1
  selector:
    matchLabels:
      app: tcp-echo
      version: v1
  template:
    metadata:
      labels:
        app: tcp-echo
        version: v1
    spec:
      containers:
      - name: tcp-echo
        image: docker.io/istio/tcp-echo-server:1.2
        imagePullPolicy: IfNotPresent
        args: [ "9000,9001,9002", "one" ]
        ports:
        - containerPort: 9000
        - containerPort: 9001
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: tcp-echo-v2
  labels:
    app: tcp-echo
    version: v2
spec:
  replicas: 1
  selector:
    matchLabels:
      app: tcp-echo
      version: v2
  template:
    metadata:
      labels:
        app: tcp-echo
        version: v2
    spec:
      containers:
      - name: tcp-echo
        image: docker.io/istio/tcp-echo-server:1.2
        imagePullPolicy: IfNotPresent
        args: [ "9000,9001,9002", "two" ]
        ports:
        - containerPort: 9000
        - containerPort: 9001
```



应用规则：

tcp-echo-all-v1.yaml

kubectl apply -f tcp-echo-all-v1.yaml -n istio

```
apiVersion: networking.istio.io/v1alpha3
kind: Gateway
metadata:
  name: tcp-echo-gateway
spec:
  selector:
    istio: ingressgateway
  servers:
  - port:
      number: 31400
      name: tcp
      protocol: TCP
    hosts:
    - "*"
---
apiVersion: networking.istio.io/v1alpha3
kind: DestinationRule
metadata:
  name: tcp-echo-destination
spec:
  host: tcp-echo
  subsets:
  - name: v1
    labels:
      version: v1
  - name: v2
    labels:
      version: v2
---
apiVersion: networking.istio.io/v1alpha3
kind: VirtualService
metadata:
  name: tcp-echo
spec:
  hosts:
  - "*"
  gateways:
  - tcp-echo-gateway
  tcp:
  - match:
    - port: 31400
    route:
    - destination:
        host: tcp-echo
        port:
          number: 9000
        subset: v1
```



## 3.1cluster

ef-tcp_proxy-cluster.yaml

kubectl apply -f ef-tcp_proxy-cluster.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: tcp
spec:
  workloadSelector:
    labels:
      istio: ingressgateway
  configPatches:
  - applyTo: NETWORK_FILTER
    match:
      context: GATEWAY
      listener:
        portNumber: 31400
        filterChain:
          filter:
            name: "envoy.filters.network.tcp_proxy"
    patch:
      operation: REPLACE
      value:
          name: envoy.filters.network.tcp_proxy
          typedConfig:
            '@type': type.googleapis.com/envoy.extensions.filters.network.tcp_proxy.v3.TcpProxy
            cluster: outbound|9000|v2|tcp-echo.istio.svc.cluster.local
            statPrefix: outbound|9000|v2|tcp-echo.istio.svc.cluster.local
```



## 3.2weighted_clusters

ef-tcp_proxy-weighted_clusters.yaml

kubectl apply -f ef-tcp_proxy-weighted_clusters.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: tcp
spec:
  workloadSelector:
    labels:
      istio: ingressgateway
  configPatches:
  - applyTo: NETWORK_FILTER
    match:
      context: GATEWAY
      listener:
        portNumber: 31400
        filterChain:
          filter:
            name: "envoy.filters.network.tcp_proxy"
    patch:
      operation: REPLACE
      value:
          name: envoy.filters.network.tcp_proxy
          typedConfig:
            '@type': type.googleapis.com/envoy.extensions.filters.network.tcp_proxy.v3.TcpProxy
            weighted_clusters:
              clusters:
              - name:  outbound|9000|v2|tcp-echo.istio.svc.cluster.local
                weight: 50
              - name:  outbound|9000|v1|tcp-echo.istio.svc.cluster.local
                weight: 50
            statPrefix: tcp-echo.istio.svc.cluster.local
```

## 3.3metadata_match

### 3.3.1 metadata_match

ef-tcp_proxy-metadata_match.yaml

kubectl apply -f ef-tcp_proxy-metadata_match.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: tcp
spec:
  workloadSelector:
    labels:
      istio: ingressgateway
  configPatches:
  - applyTo: NETWORK_FILTER
    match:
      context: GATEWAY
      listener:
        portNumber: 31400
        filterChain:
          filter:
            name: "envoy.filters.network.tcp_proxy"
    patch:
      operation: REPLACE
      value:
          name: envoy.filters.network.tcp_proxy
          typedConfig:
            '@type': type.googleapis.com/envoy.extensions.filters.network.tcp_proxy.v3.TcpProxy
            cluster: cluster123
            statPrefix: outbound||v2|
            metadata_match:
              filter_metadata:
                envoy.lb: 
                  env: mark
  - applyTo: CLUSTER
    patch:
      operation: ADD
      value: 
        name: "cluster123"
        type: STRICT_DNS
        connect_timeout: 0.5s
        lb_policy: ROUND_ROBIN
        lb_subset_config:
          fallback_policy: DEFAULT_SUBSET
          default_subset:
            env: "taobao"
          subset_selectors:
          - keys:
            - env
        load_assignment:
          cluster_name: cluster123
          endpoints:
          - lb_endpoints:
            - endpoint:
                address:
                  socket_address:
                    address: tcp-echo.istio.svc.cluster.local
                    port_value: 9000
              metadata:
                filter_metadata:
                  envoy.lb:
                    env: hema
            - endpoint:
                address:
                  socket_address:
                    address: tcp-echo.istio-2.svc.cluster.local
                    port_value: 9000
              metadata:
                filter_metadata:
                  envoy.lb:
                    env: taobao
```



### 3.3.2 weighted_clusters metadata_match

ef-tcp_proxy-weighted_clusters-metadata_match.yaml

kubectl apply -f ef-tcp_proxy-weighted_clusters-metadata_match.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: tcp
spec:
  workloadSelector:
    labels:
      istio: ingressgateway
  configPatches:
  - applyTo: NETWORK_FILTER
    match:
      context: GATEWAY
      listener:
        portNumber: 31400
        filterChain:
          filter:
            name: "envoy.filters.network.tcp_proxy"
    patch:
      operation: REPLACE
      value:
          name: envoy.filters.network.tcp_proxy
          typedConfig:
            '@type': type.googleapis.com/envoy.extensions.filters.network.tcp_proxy.v3.TcpProxy
            weighted_clusters:
              clusters:
              - name:  cluster123
                weight: 100
                metadata_match:
                  filter_metadata:
                    envoy.lb: 
                      env: mark
            statPrefix: tcp-echo.istio.svc.cluster.local
  - applyTo: CLUSTER
    patch:
      operation: ADD
      value: 
        name: "cluster123"
        type: STRICT_DNS
        connect_timeout: 0.5s
        lb_policy: ROUND_ROBIN
        lb_subset_config:
          fallback_policy: DEFAULT_SUBSET
          default_subset:
            env: "taobao"
          subset_selectors:
          - keys:
            - env
        load_assignment:
          cluster_name: cluster123
          endpoints:
          - lb_endpoints:
            - endpoint:
                address:
                  socket_address:
                    address: tcp-echo.istio.svc.cluster.local
                    port_value: 9000
              metadata:
                filter_metadata:
                  envoy.lb:
                    env: hema
            - endpoint:
                address:
                  socket_address:
                    address: tcp-echo.istio-2.svc.cluster.local
                    port_value: 9000
              metadata:
                filter_metadata:
                  envoy.lb:
                    env: taobao
```



## 3.4access_log

ef-tcp_proxy-access_log.yaml

kubectl apply -f ef-tcp_proxy-access_log.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: tcp
spec:
  workloadSelector:
    labels:
      istio: ingressgateway
  configPatches:
  - applyTo: NETWORK_FILTER
    match:
      context: GATEWAY
      listener:
        portNumber: 31400
        filterChain:
          filter:
            name: "envoy.filters.network.tcp_proxy"
    patch:
      operation: REPLACE
      value:
          name: envoy.filters.network.tcp_proxy
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
            cluster: outbound|9000|v1|tcp-echo.istio.svc.cluster.local
            statPrefix: outbound|9000|v1|tcp-echo.istio.svc.cluster.local
```

## 3.5hash_policy

ef-tcp_proxy-hash_policy.yaml

kubectl apply -f ef-tcp_proxy-hash_policy.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: tcp
spec:
  workloadSelector:
    labels:
      istio: ingressgateway
  configPatches:
  - applyTo: NETWORK_FILTER
    match:
      context: GATEWAY
      listener:
        portNumber: 31400
        filterChain:
          filter:
            name: "envoy.filters.network.tcp_proxy"
    patch:
      operation: REPLACE
      value:
          name: envoy.filters.network.tcp_proxy
          typedConfig:
            '@type': type.googleapis.com/envoy.extensions.filters.network.tcp_proxy.v3.TcpProxy
            cluster: outbound|9000|v1|tcp-echo.istio.svc.cluster.local
            statPrefix: tcp-echo.istio.svc.cluster.local
            hash_policy:
            - source_ip: {}
  - applyTo: CLUSTER
    match:
      cluster:
        name: outbound|9000|v1|tcp-echo.istio.svc.cluster.local
    patch:
      operation: MERGE
      value: 
        lb_policy: RING_HASH
        ring_hash_lb_config:
          minimum_ring_size: 1024
```

一致性hash，是每个子集的多个节点的一致性hash

 kubectl scale deploy -n istio tcp-echo-v1 --replicas=3

查看三个pod的日志：

kubectl logs -f -n istio tcp-echo-v1-65875b4c8c-zt5m4

## 3.6其他参数配置

ef-tcp_proxy-general.yaml

kubectl apply -f ef-tcp_proxy-general.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: tcp
spec:
  workloadSelector:
    labels:
      istio: ingressgateway
  configPatches:
  - applyTo: NETWORK_FILTER
    match:
      context: GATEWAY
      listener:
        portNumber: 31400
        filterChain:
          filter:
            name: "envoy.filters.network.tcp_proxy"
    patch:
      operation: REPLACE
      value:
          name: envoy.filters.network.tcp_proxy
          typedConfig:
            '@type': type.googleapis.com/envoy.extensions.filters.network.tcp_proxy.v3.TcpProxy
            cluster: outbound|9000|v2|tcp-echo.istio.svc.cluster.local
            statPrefix: outbound|9000|v2|tcp-echo.istio.svc.cluster.local
            idle_timeout: 1h
            max_connect_attempts: 1
            max_downstream_connection_duration: 10s
```

## 3.7tunneling_config

### 3.7.1 HTTP/1.1 CONNECT 

ef-tcp_proxy-tunneling_config-hostname.yaml

kubectl apply -f ef-tcp_proxy-tunneling_config-hostname.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: tcp
spec:
  workloadSelector:
    labels:
      istio: ingressgateway
  configPatches:
  - applyTo: NETWORK_FILTER
    match:
      context: GATEWAY
      listener:
        portNumber: 31400
        filterChain:
          filter:
            name: "envoy.filters.network.tcp_proxy"
    patch:
      operation: REPLACE
      value:
          name: envoy.filters.network.tcp_proxy
          typedConfig:
            '@type': type.googleapis.com/envoy.extensions.filters.network.tcp_proxy.v3.TcpProxy
            stat_prefix: tcp_stats
            cluster: "cluster_0"
            tunneling_config:
              hostname: "%REQUESTED_SERVER_NAME%:80"
  - applyTo: CLUSTER
    patch:
      operation: ADD
      value: 
        name: cluster_0
        connect_timeout: 5s
        typed_extension_protocol_options:
          envoy.extensions.upstreams.http.v3.HttpProtocolOptions:
            "@type": type.googleapis.com/envoy.extensions.upstreams.http.v3.HttpProtocolOptions
            explicit_http_config:
              http_protocol_options: {}
        load_assignment:
          cluster_name: cluster_0
          endpoints:
          - lb_endpoints:
            - endpoint:
                address:
                  socket_address:
                    address: 127.0.0.1
                    port_value: 10001  
  - applyTo: LISTENER
    match:
      context: GATEWAY
    patch:
      operation: ADD
      value:
        name: listener_0
        address:
          socket_address:
            protocol: TCP
            address: 0.0.0.0
            port_value: 10001
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
                      cluster: outbound|9000|v2|tcp-echo.istio.svc.cluster.local
                      upgrade_configs:
                      - upgrade_type: CONNECT
                        connect_config:
                          {}
              http_filters:
              - name: envoy.filters.http.router
              http_protocol_options: {}
              upgrade_configs:
              - upgrade_type: CONNECT
```

### 3.7.2 HTTP/2 CONNECT 

ef-tcp_proxy-http2_connect.yaml

kubectl apply -f ef-tcp_proxy-http2_connect.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: tcp
spec:
  workloadSelector:
    labels:
      istio: ingressgateway
  configPatches:
  - applyTo: NETWORK_FILTER
    match:
      context: GATEWAY
      listener:
        portNumber: 31400
        filterChain:
          filter:
            name: "envoy.filters.network.tcp_proxy"
    patch:
      operation: REPLACE
      value:
          name: envoy.filters.network.tcp_proxy
          typedConfig:
            '@type': type.googleapis.com/envoy.extensions.filters.network.tcp_proxy.v3.TcpProxy
            stat_prefix: tcp_stats
            cluster: "cluster_0"
            tunneling_config:
              hostname: host.com:443
  - applyTo: CLUSTER
    patch:
      operation: ADD
      value: 
        name: cluster_0
        connect_timeout: 5s
        typed_extension_protocol_options:
          envoy.extensions.upstreams.http.v3.HttpProtocolOptions:
            "@type": type.googleapis.com/envoy.extensions.upstreams.http.v3.HttpProtocolOptions
            explicit_http_config:
              http2_protocol_options: {}
        load_assignment:
          cluster_name: cluster_0
          endpoints:
          - lb_endpoints:
            - endpoint:
                address:
                  socket_address:
                    address: 127.0.0.1
                    port_value: 10001  
  - applyTo: LISTENER
    match:
      context: GATEWAY
    patch:
      operation: ADD
      value:
        name: listener_0
        address:
          socket_address:
            protocol: TCP
            address: 0.0.0.0
            port_value: 10001
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
                      cluster: outbound|9000|v2|tcp-echo.istio.svc.cluster.local
                      upgrade_configs:
                      - upgrade_type: CONNECT
                        connect_config:
                          {}
              http_filters:
              - name: envoy.filters.http.router
              http2_protocol_options:
                allow_connect: true
              upgrade_configs:
              - upgrade_type: CONNECT
```



### 3.7.3 HTTP/2 POST 

ef-tcp_proxy-http2_post.yaml

kubectl apply -f ef-tcp_proxy-http2_post.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: tcp
spec:
  workloadSelector:
    labels:
      istio: ingressgateway
  configPatches:
  - applyTo: NETWORK_FILTER
    match:
      context: GATEWAY
      listener:
        portNumber: 31400
        filterChain:
          filter:
            name: "envoy.filters.network.tcp_proxy"
    patch:
      operation: REPLACE
      value:
          name: envoy.filters.network.tcp_proxy
          typedConfig:
            '@type': type.googleapis.com/envoy.extensions.filters.network.tcp_proxy.v3.TcpProxy
            stat_prefix: tcp_stats
            cluster: "cluster_0"
            tunneling_config:
              hostname: host.com:443
              use_post: true
  - applyTo: CLUSTER
    patch:
      operation: ADD
      value: 
        name: cluster_0
        connect_timeout: 5s
        typed_extension_protocol_options:
          envoy.extensions.upstreams.http.v3.HttpProtocolOptions:
            "@type": type.googleapis.com/envoy.extensions.upstreams.http.v3.HttpProtocolOptions
            explicit_http_config:
              http2_protocol_options: {}
        load_assignment:
          cluster_name: cluster_0
          endpoints:
          - lb_endpoints:
            - endpoint:
                address:
                  socket_address:
                    address: 127.0.0.1
                    port_value: 10001  
  - applyTo: LISTENER
    match:
      context: GATEWAY
    patch:
      operation: ADD
      value:
        name: listener_0
        address:
          socket_address:
            protocol: TCP
            address: 0.0.0.0
            port_value: 10001
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
                      prefix: "/"
                      headers:
                      - name: ":method"
                        string_match:
                          exact: "POST"
                    route:
                      cluster: outbound|9000|v2|tcp-echo.istio.svc.cluster.local
                      upgrade_configs:
                      - upgrade_type: CONNECT
                        connect_config:
                          allow_post: true
              http_filters:
              - name: envoy.filters.http.router
              http2_protocol_options:
                allow_connect: true
```





