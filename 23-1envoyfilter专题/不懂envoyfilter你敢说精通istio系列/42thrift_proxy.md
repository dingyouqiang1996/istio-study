# 1什么是thrift_proxy

thrift_proxy是一个用来代理thrift协议的network filter，名称为 *envoy.filters.network.thrift_proxy* 。

# 2配置

```
{
  "transport": "...",传输类型
  "protocol": "...",协议
  "stat_prefix": "...",stat前缀
  "route_config": "{...}",路由配置
  "thrift_filters": [],过滤器配置
  "payload_passthrough": "...",是否不解码数据
  "max_requests_per_connection": "{...}"每个连接最大请求数量
}
```

transport：

Thrift transport types supported by Envoy.

- AUTO_TRANSPORT

  *(DEFAULT)* ⁣For downstream connections, the Thrift proxy will attempt to determine which transport to use. For upstream connections, the Thrift proxy will use same transport as the downstream connection.

- FRAMED

  ⁣The Thrift proxy will use the Thrift framed transport.

- UNFRAMED

  ⁣The Thrift proxy will use the Thrift unframed transport.

- HEADER

  ⁣The Thrift proxy will assume the client is using the Thrift header transport.

protocol：

Thrift Protocol types supported by Envoy.

- AUTO_PROTOCOL

  *(DEFAULT)* ⁣For downstream connections, the Thrift proxy will attempt to determine which protocol to use. Note that the older, non-strict (or lax) binary protocol is not included in automatic protocol detection. For upstream connections, the Thrift proxy will use the same protocol as the downstream connection.

- BINARY

  ⁣The Thrift proxy will use the Thrift binary protocol.

- LAX_BINARY

  ⁣The Thrift proxy will use Thrift non-strict binary protocol.

- COMPACT

  ⁣The Thrift proxy will use the Thrift compact protocol.

- TWITTER

  ⁣The Thrift proxy will use the Thrift “Twitter” protocol implemented by the finagle library.

route_config：

```
{
  "name": "...",名称
  "routes": []路由
}
```

routes：

```
{
  "match": "{...}",匹配条件
  "route": "{...}"路由动作
}
```

match：

```
{
  "method_name": "...",方法名称
  "service_name": "...",服务名称
  "invert": "...",反向匹配
  "headers": []头
}
```

headers：

```
{
  "name": "...",名称
  "exact_match": "...",精确匹配
  "safe_regex_match": "{...}",正则匹配
  "range_match": "{...}",范围匹配
  "present_match": "...",存在匹配
  "prefix_match": "...",前缀匹配
  "suffix_match": "...",后缀匹配
  "contains_match": "...",包含匹配
  "string_match": "{...}",字符串匹配
  "invert_match": "..."反向匹配
}
```

route：

```
{
  "cluster": "...",集群
  "weighted_clusters": "{...}",加权集群
  "cluster_header": "...",从头获取路由信息
  "metadata_match": "{...}",元数据匹配
  "rate_limits": [],限流配置
  "strip_service_name": "...",方法名除去service名称
  "request_mirror_policies": []镜像策略
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
  "name": "...",名称
  "weight": "{...}",权重
  "metadata_match": "{...}"元数据匹配
}
```

rate_limits：

```
{
  "stage": "{...}",阶段，默认0
  "disable_key": "...",禁用限流的key
  "actions": [],动作
  "limit": "{...}"覆盖限流
}
```

thrift_filters：

```
{
  "name": "...",名称
  "typed_config": "{...}"配置
}
```

- [envoy.filters.thrift.ratelimit](https://www.envoyproxy.io/docs/envoy/latest/api-v3/extensions/filters/network/thrift_proxy/filters/ratelimit/v3/rate_limit.proto#extension-envoy-filters-thrift-ratelimit)
- [envoy.filters.thrift.router](https://www.envoyproxy.io/docs/envoy/latest/api-v3/extensions/filters/network/thrift_proxy/router/v3/router.proto#extension-envoy-filters-thrift-router)

# 3实战

## 3.1准备工作

例子来自： https://github.com/aeraki-framework/thrift-envoyfilter-example

部署应用

thrift-deploy.yaml 

kubectl apply -f thrift-deploy.yaml -n istio

```
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: thrift-sample-server-v1
  labels:
    app: thrift-sample-server
spec:
  selector:
    matchLabels:
      app: thrift-sample-server
  replicas: 1
  template:
    metadata:
      labels:
        app: thrift-sample-server
        version: v1
    spec:
      containers:
        - name: thrift-sample-server
          image: registry.cn-hangzhou.aliyuncs.com/hxpdocker/thrift-sample-server:latest
          imagePullPolicy: IfNotPresent
          ports:
            - containerPort: 9090
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: thrift-sample-server-v2
  labels:
    app: thrift-sample-server
spec:
  selector:
    matchLabels:
      app: thrift-sample-server
  replicas: 1
  template:
    metadata:
      labels:
        app: thrift-sample-server
        version: v2
    spec:
      containers:
        - name: thrift-sample-server
          image: registry.cn-hangzhou.aliyuncs.com/hxpdocker/thrift-sample-server:latest
          imagePullPolicy: IfNotPresent
          ports:
            - containerPort: 9090
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: thrift-sample-client
  labels:
    app: thrift-sample-client
spec:
  selector:
    matchLabels:
      app: thrift-sample-client
  replicas: 1
  template:
    metadata:
      labels:
        app: thrift-sample-client
    spec:
      containers:
        - name: thrift-sample-client
          image: registry.cn-hangzhou.aliyuncs.com/hxpdocker/thrift-sample-client:latest
          imagePullPolicy: IfNotPresent
          env:
            - name: helloServer
              value: "thrift-sample-server"
            - name: mode
              value: "demo"
---
apiVersion: v1
kind: Service
metadata:
  name: thrift-sample-server
spec:
  selector:
    app: thrift-sample-server
  ports:
    - name: tcp-thrift-hello-server
      protocol: TCP
      port: 9090
      targetPort: 9090
---
```

应用配置

dr-thrift.yaml

kubectl apply -f dr-thrift.yaml -n istio

```
apiVersion: networking.istio.io/v1alpha3
kind: DestinationRule
metadata:
  name: thrift-sample-server
spec:
  host: thrift-sample-server
  subsets:
  - name: v1
    labels:
      version: v1
  - name: v2
    labels:
      version: v2
```

## 3.2cluster

ef-cluster.yaml

kubectl apply -f  ef-cluster.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: thrift-sample-server
spec:
  configPatches:
  - applyTo: NETWORK_FILTER
    match:
      listener:
        name: ${thrift-sample-server-vip}_9090    # sed -i .bak "s/\${thrift-sample-server-vip}/`kubectl get svc thrift-sample-server -n thrift -o=jsonpath='{.spec.clusterIP}'`/" istio/envoyfilter-thrift-proxy.yaml
        filterChain:
          filter:
            name: "envoy.filters.network.tcp_proxy"
    patch:
      operation: REPLACE
      value:
        name: envoy.filters.network.thrift_proxy
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.filters.network.thrift_proxy.v3.ThriftProxy
          stat_prefix: "outbound|9090||thrift-sample-server.thrift.svc.cluster.local"
          transport: AUTO_TRANSPORT
          protocol: AUTO_PROTOCOL
          thrift_filters:
          - name: envoy.filters.thrift.router
          route_config:
            routes:
            - match:
                # empty string matches any request method name
                method_name: ""
              route:
                cluster: "outbound|9090|v2|thrift-sample-server.istio.svc.cluster.local"
  - applyTo: NETWORK_FILTER
    match:
      listener:
        name: virtualInbound
        filterChain:
          destinationPort: 9090
          filter:
            name: "envoy.filters.network.tcp_proxy"
    patch:
      operation: REPLACE
      value:
        name: envoy.filters.network.thrift_proxy
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.filters.network.thrift_proxy.v3.ThriftProxy
          stat_prefix: inbound|9090||
          transport: AUTO_TRANSPORT
          protocol: AUTO_PROTOCOL
          thrift_filters:
          - name: envoy.filters.thrift.router
          route_config:
            routes:
            - match:
                # empty string matches any request method name
                method_name: ""
              route:
                cluster: inbound|9090||
```

替换${thrift-sample-server-vip}

## 3.3weighted_cluster

ef-weighted_cluster.yaml

kubectl apply -f ef-weighted_cluster.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: thrift-sample-server
spec:
  configPatches:
  - applyTo: NETWORK_FILTER
    match:
      listener:
        name: 10.68.153.249_9090    # sed -i .bak "s/\${thrift-sample-server-vip}/`kubectl get svc thrift-sample-server -n thrift -o=jsonpath='{.spec.clusterIP}'`/" istio/envoyfilter-thrift-proxy.yaml
        filterChain:
          filter:
            name: "envoy.filters.network.tcp_proxy"
    patch:
      operation: REPLACE
      value:
        name: envoy.filters.network.thrift_proxy
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.filters.network.thrift_proxy.v3.ThriftProxy
          stat_prefix: "outbound|9090||thrift-sample-server.thrift.svc.cluster.local"
          transport: AUTO_TRANSPORT
          protocol: AUTO_PROTOCOL
          thrift_filters:
          - name: envoy.filters.thrift.router
          route_config:
            routes:
            - match:
                # empty string matches any request method name
                method_name: ""
              route:
                weighted_clusters:
                  clusters:
                    - name: "outbound|9090|v1|thrift-sample-server.istio.svc.cluster.local"
                      weight: 50
                    - name: "outbound|9090|v2|thrift-sample-server.istio.svc.cluster.local"
                      weight: 50
  - applyTo: NETWORK_FILTER
    match:
      listener:
        name: virtualInbound
        filterChain:
          destinationPort: 9090
          filter:
            name: "envoy.filters.network.tcp_proxy"
    patch:
      operation: REPLACE
      value:
        name: envoy.filters.network.thrift_proxy
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.filters.network.thrift_proxy.v3.ThriftProxy
          stat_prefix: inbound|9090||
          transport: AUTO_TRANSPORT
          protocol: AUTO_PROTOCOL
          thrift_filters:
          - name: envoy.filters.thrift.router
          route_config:
            routes:
            - match:
                # empty string matches any request method name
                method_name: ""
              route:
                cluster: inbound|9090||
```

替换${thrift-sample-server-vip}

## 3.4match

```
{
  "method_name": "...",方法名称
  "service_name": "...",服务名称
  "invert": "...",反向匹配
  "headers": []头
}
```

### 3.4.1method_name

ef-match-method_name.yaml

kubectl apply -f ef-match-method_name.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: thrift-sample-server
spec:
  configPatches:
  - applyTo: NETWORK_FILTER
    match:
      listener:
        name: 10.68.153.249_9090    # sed -i .bak "s/\${thrift-sample-server-vip}/`kubectl get svc thrift-sample-server -n thrift -o=jsonpath='{.spec.clusterIP}'`/" istio/envoyfilter-thrift-proxy.yaml
        filterChain:
          filter:
            name: "envoy.filters.network.tcp_proxy"
    patch:
      operation: REPLACE
      value:
        name: envoy.filters.network.thrift_proxy
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.filters.network.thrift_proxy.v3.ThriftProxy
          stat_prefix: "outbound|9090||thrift-sample-server.thrift.svc.cluster.local"
          transport: AUTO_TRANSPORT
          protocol: AUTO_PROTOCOL
          thrift_filters:
          - name: envoy.filters.thrift.router
          route_config:
            routes:
            - match:
                method_name: "sayHello"
              route:
                weighted_clusters:
                  clusters:
                    - name: "outbound|9090|v1|thrift-sample-server.istio.svc.cluster.local"
                      weight: 50
                    - name: "outbound|9090|v2|thrift-sample-server.istio.svc.cluster.local"
                      weight: 50
  - applyTo: NETWORK_FILTER
    match:
      listener:
        name: virtualInbound
        filterChain:
          destinationPort: 9090
          filter:
            name: "envoy.filters.network.tcp_proxy"
    patch:
      operation: REPLACE
      value:
        name: envoy.filters.network.thrift_proxy
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.filters.network.thrift_proxy.v3.ThriftProxy
          stat_prefix: inbound|9090||
          transport: AUTO_TRANSPORT
          protocol: AUTO_PROTOCOL
          thrift_filters:
          - name: envoy.filters.thrift.router
          route_config:
            routes:
            - match:
                method_name: "sayHello"
              route:
                cluster: inbound|9090||
```



### 3.4.2service_name

ef-match-service_name.yaml

kubectl apply -f  ef-match-service_name.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: thrift-sample-server
spec:
  configPatches:
  - applyTo: NETWORK_FILTER
    match:
      listener:
        name: 10.68.153.249_9090    # sed -i .bak "s/\${thrift-sample-server-vip}/`kubectl get svc thrift-sample-server -n thrift -o=jsonpath='{.spec.clusterIP}'`/" istio/envoyfilter-thrift-proxy.yaml
        filterChain:
          filter:
            name: "envoy.filters.network.tcp_proxy"
    patch:
      operation: REPLACE
      value:
        name: envoy.filters.network.thrift_proxy
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.filters.network.thrift_proxy.v3.ThriftProxy
          stat_prefix: "outbound|9090||thrift-sample-server.thrift.svc.cluster.local"
          transport: AUTO_TRANSPORT
          protocol: AUTO_PROTOCOL
          thrift_filters:
          - name: envoy.filters.thrift.router
          route_config:
            routes:
            - match:
                method_name: ""
                service_name: ""
              route:
                cluster: "outbound|9090|v2|thrift-sample-server.istio.svc.cluster.local"
  - applyTo: NETWORK_FILTER
    match:
      listener:
        name: virtualInbound
        filterChain:
          destinationPort: 9090
          filter:
            name: "envoy.filters.network.tcp_proxy"
    patch:
      operation: REPLACE
      value:
        name: envoy.filters.network.thrift_proxy
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.filters.network.thrift_proxy.v3.ThriftProxy
          stat_prefix: inbound|9090||
          transport: AUTO_TRANSPORT
          protocol: AUTO_PROTOCOL
          thrift_filters:
          - name: envoy.filters.thrift.router
          route_config:
            routes:
            - match:
                method_name: ""
                service_name: ""
              route:
                cluster: inbound|9090||
```



### 3.4.3invert

ef-match-invert.yaml

kubectl apply -f  ef-match-invert.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: thrift-sample-server
spec:
  configPatches:
  - applyTo: NETWORK_FILTER
    match:
      listener:
        name: 10.68.153.249_9090    # sed -i .bak "s/\${thrift-sample-server-vip}/`kubectl get svc thrift-sample-server -n thrift -o=jsonpath='{.spec.clusterIP}'`/" istio/envoyfilter-thrift-proxy.yaml
        filterChain:
          filter:
            name: "envoy.filters.network.tcp_proxy"
    patch:
      operation: REPLACE
      value:
        name: envoy.filters.network.thrift_proxy
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.filters.network.thrift_proxy.v3.ThriftProxy
          stat_prefix: "outbound|9090||thrift-sample-server.thrift.svc.cluster.local"
          transport: AUTO_TRANSPORT
          protocol: AUTO_PROTOCOL
          thrift_filters:
          - name: envoy.filters.thrift.router
          route_config:
            routes:
            - match:
                service_name: "org.aeraki.HelloService"
                invert: true
              route:
                cluster: "outbound|9090|v2|thrift-sample-server.istio.svc.cluster.local"
  - applyTo: NETWORK_FILTER
    match:
      listener:
        name: virtualInbound
        filterChain:
          destinationPort: 9090
          filter:
            name: "envoy.filters.network.tcp_proxy"
    patch:
      operation: REPLACE
      value:
        name: envoy.filters.network.thrift_proxy
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.filters.network.thrift_proxy.v3.ThriftProxy
          stat_prefix: inbound|9090||
          transport: AUTO_TRANSPORT
          protocol: AUTO_PROTOCOL
          thrift_filters:
          - name: envoy.filters.thrift.router
          route_config:
            routes:
            - match:
                service_name: "org.aeraki.HelloService"
                invert: true
              route:
                cluster: inbound|9090||
```



### 3.4.4headers

ef-match-headers.yaml

kubectl apply -f  ef-match-headers.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: thrift-sample-server
spec:
  configPatches:
  - applyTo: NETWORK_FILTER
    match:
      listener:
        name: 10.68.153.249_9090    # sed -i .bak "s/\${thrift-sample-server-vip}/`kubectl get svc thrift-sample-server -n thrift -o=jsonpath='{.spec.clusterIP}'`/" istio/envoyfilter-thrift-proxy.yaml
        filterChain:
          filter:
            name: "envoy.filters.network.tcp_proxy"
    patch:
      operation: REPLACE
      value:
        name: envoy.filters.network.thrift_proxy
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.filters.network.thrift_proxy.v3.ThriftProxy
          stat_prefix: "outbound|9090||thrift-sample-server.thrift.svc.cluster.local"
          transport: AUTO_TRANSPORT
          protocol: AUTO_PROTOCOL
          thrift_filters:
          - name: envoy.filters.thrift.router
          route_config:
            routes:
            - match:
                service_name: "org.aeraki.HelloService"
                invert: true
                headers:
                - name: test
                  present_match: false
              route:
                cluster: "outbound|9090|v1|thrift-sample-server.istio.svc.cluster.local"
  - applyTo: NETWORK_FILTER
    match:
      listener:
        name: virtualInbound
        filterChain:
          destinationPort: 9090
          filter:
            name: "envoy.filters.network.tcp_proxy"
    patch:
      operation: REPLACE
      value:
        name: envoy.filters.network.thrift_proxy
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.filters.network.thrift_proxy.v3.ThriftProxy
          stat_prefix: inbound|9090||
          transport: AUTO_TRANSPORT
          protocol: AUTO_PROTOCOL
          thrift_filters:
          - name: envoy.filters.thrift.router
          route_config:
            routes:
            - match:
                service_name: "org.aeraki.HelloService"
                invert: true
                headers:
                - name: test
                  present_match: false
              route:
                cluster: inbound|9090||
```

## 3.5metadata_match

### 3.5.1metadata_match

ef-match-metadata_match.yaml

kubectl apply -f  ef-match-metadata_match.yaml -n istio

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: thrift-sample-server
spec:
  configPatches:
  - applyTo: NETWORK_FILTER
    match:
      listener:
        name: 10.68.153.249_9090    # sed -i .bak "s/\${thrift-sample-server-vip}/`kubectl get svc thrift-sample-server -n thrift -o=jsonpath='{.spec.clusterIP}'`/" istio/envoyfilter-thrift-proxy.yaml
        filterChain:
          filter:
            name: "envoy.filters.network.tcp_proxy"
    patch:
      operation: REPLACE
      value:
        name: envoy.filters.network.thrift_proxy
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.filters.network.thrift_proxy.v3.ThriftProxy
          stat_prefix: "outbound|9090||thrift-sample-server.thrift.svc.cluster.local"
          transport: AUTO_TRANSPORT
          protocol: AUTO_PROTOCOL
          thrift_filters:
          - name: envoy.filters.thrift.router
          route_config:
            routes:
            - match:
                service_name: "org.aeraki.HelloService"
                invert: true
                headers:
                - name: test
                  present_match: false
              route:
                cluster: "cluster123"
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
                    address: thrift-sample-server.istio.svc.cluster.local
                    port_value: 9090
              metadata:
                filter_metadata:
                  envoy.lb:
                    env: hema
            - endpoint:
                address:
                  socket_address:
                    address: thrift-sample-server.istio-2.svc.cluster.local
                    port_value: 9090
              metadata:
                filter_metadata:
                  envoy.lb:
                    env: taobao
```

### 3.5.2weighted_clusters metadata_match

ef-weighted_clusters-metadata_match.yaml

kubectl apply -f  ef-weighted_clusters-metadata_match.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: thrift-sample-server
spec:
  configPatches:
  - applyTo: NETWORK_FILTER
    match:
      listener:
        name: 10.68.153.249_9090    # sed -i .bak "s/\${thrift-sample-server-vip}/`kubectl get svc thrift-sample-server -n thrift -o=jsonpath='{.spec.clusterIP}'`/" istio/envoyfilter-thrift-proxy.yaml
        filterChain:
          filter:
            name: "envoy.filters.network.tcp_proxy"
    patch:
      operation: REPLACE
      value:
        name: envoy.filters.network.thrift_proxy
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.filters.network.thrift_proxy.v3.ThriftProxy
          stat_prefix: "outbound|9090||thrift-sample-server.thrift.svc.cluster.local"
          transport: AUTO_TRANSPORT
          protocol: AUTO_PROTOCOL
          thrift_filters:
          - name: envoy.filters.thrift.router
          route_config:
            routes:
            - match:
                service_name: "org.aeraki.HelloService"
                invert: true
                headers:
                - name: test
                  present_match: false
              route:
                weighted_clusters:
                  clusters:
                    - name: "cluster123"
                      weight: 100
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
                    address: thrift-sample-server.istio.svc.cluster.local
                    port_value: 9090
              metadata:
                filter_metadata:
                  envoy.lb:
                    env: hema
            - endpoint:
                address:
                  socket_address:
                    address: thrift-sample-server.istio-2.svc.cluster.local
                    port_value: 9090
              metadata:
                filter_metadata:
                  envoy.lb:
                    env: taobao
```

## 3.6rate_limits

```
{
  "stage": "{...}",阶段，默认0
  "disable_key": "...",禁用限流的key
  "actions": [],动作
  "limit": "{...}"覆盖限流
}
```

actions：

```
{
  "source_cluster": "{...}",源集群动作
  "destination_cluster": "{...}",目标集群动作
  "request_headers": "{...}",请求头动作
  "remote_address": "{...}",远程地址动作
  "generic_key": "{...}",通用key动作
  "header_value_match": "{...}",头匹配动作
  "dynamic_metadata": "{...}",动态元数据动作
  "metadata": "{...}",元数据动作
  "extension": "{...}"扩展动作
}
```

部署ratelimit

1创建cm

```
cat << EOF > ratelimit-config.yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: ratelimit-config
data:
  config.yaml: |
    domain: productpage-ratelimit
    descriptors:
      - key: source_cluster
        value: "thrift-sample-client.istio"
        rate_limit:
          unit: minute
          requests_per_unit: 1
      - key: source_cluster
        rate_limit:
          unit: minute
          requests_per_unit: 10
EOF

kubectl apply -f ratelimit-config.yaml -n istio
```



2创建限速服务deployment

```
cat << EOF > ratelimit-deploy.yaml
apiVersion: v1
kind: Service
metadata:
  name: redis
  labels:
    app: redis
spec:
  ports:
  - name: redis
    port: 6379
  selector:
    app: redis
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: redis
spec:
  replicas: 1
  selector:
    matchLabels:
      app: redis
  template:
    metadata:
      labels:
        app: redis
    spec:
      containers:
      - image: redis:alpine
        imagePullPolicy: IfNotPresent
        name: redis
        ports:
        - name: redis
          containerPort: 6379
      restartPolicy: Always
      serviceAccountName: ""
---
apiVersion: v1
kind: Service
metadata:
  name: ratelimit
  labels:
    app: ratelimit
spec:
  ports:
  - name: http-port
    port: 8080
    targetPort: 8080
    protocol: TCP
  - name: grpc-port
    port: 8081
    targetPort: 8081
    protocol: TCP
  - name: http-debug
    port: 6070
    targetPort: 6070
    protocol: TCP
  selector:
    app: ratelimit
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: ratelimit
spec:
  replicas: 1
  selector:
    matchLabels:
      app: ratelimit
  strategy:
    type: Recreate
  template:
    metadata:
      labels:
        app: ratelimit
    spec:
      containers:
      - image: envoyproxy/ratelimit:6f5de117 # 2021/01/08
        imagePullPolicy: IfNotPresent
        name: ratelimit
        command: ["/bin/ratelimit"]
        env:
        - name: LOG_LEVEL
          value: debug
        - name: REDIS_SOCKET_TYPE
          value: tcp
        - name: REDIS_URL
          value: redis:6379
        - name: USE_STATSD
          value: "false"
        - name: RUNTIME_ROOT
          value: /data
        - name: RUNTIME_SUBDIRECTORY
          value: ratelimit
        ports:
        - containerPort: 8080
        - containerPort: 8081
        - containerPort: 6070
        volumeMounts:
        - name: config-volume
          mountPath: /data/ratelimit/config/config.yaml
          subPath: config.yaml
      volumes:
      - name: config-volume
        configMap:
          name: ratelimit-config
EOF

kubectl apply -f ratelimit-deploy.yaml -n istio
```

ef-ratelimit-source_cluster.yaml

kubectl apply -f ef-ratelimit-source_cluster.yaml -n istio

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: thrift-sample-server
spec:
  configPatches:
  - applyTo: NETWORK_FILTER
    match:
      listener:
        name: 10.68.153.249_9090    # sed -i .bak "s/\${thrift-sample-server-vip}/`kubectl get svc thrift-sample-server -n thrift -o=jsonpath='{.spec.clusterIP}'`/" istio/envoyfilter-thrift-proxy.yaml
        filterChain:
          filter:
            name: "envoy.filters.network.tcp_proxy"
    patch:
      operation: REPLACE
      value:
        name: envoy.filters.network.thrift_proxy
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.filters.network.thrift_proxy.v3.ThriftProxy
          stat_prefix: "outbound|9090||thrift-sample-server.thrift.svc.cluster.local"
          transport: AUTO_TRANSPORT
          protocol: AUTO_PROTOCOL
          thrift_filters:
          - name: envoy.filters.thrift.ratelimit
            typed_config:
              "@type": type.googleapis.com/envoy.extensions.filters.network.thrift_proxy.filters.ratelimit.v3.RateLimit
              domain: productpage-ratelimit
              stage: 0
              timeout: 3s
              failure_mode_deny: true
              rate_limit_service:
                grpc_service:
                  envoy_grpc:
                    cluster_name: rate_limit_cluster
                  timeout: 10s
                transport_api_version: V3
          - name: envoy.filters.thrift.router
          route_config:
            routes:
            - match:
                # empty string matches any request method name
                method_name: ""
              route:
                rate_limits:
                - stage: 0
                  actions:
                  - source_cluster: {}
                weighted_clusters:
                  clusters:
                    - name: "outbound|9090|v1|thrift-sample-server.istio.svc.cluster.local"
                      weight: 50
                    - name: "outbound|9090|v2|thrift-sample-server.istio.svc.cluster.local"
                      weight: 50
  - applyTo: NETWORK_FILTER
    match:
      listener:
        name: virtualInbound
        filterChain:
          destinationPort: 9090
          filter:
            name: "envoy.filters.network.tcp_proxy"
    patch:
      operation: REPLACE
      value:
        name: envoy.filters.network.thrift_proxy
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.filters.network.thrift_proxy.v3.ThriftProxy
          stat_prefix: inbound|9090||
          transport: AUTO_TRANSPORT
          protocol: AUTO_PROTOCOL
          thrift_filters:
          - name: envoy.filters.thrift.router
          route_config:
            routes:
            - match:
                # empty string matches any request method name
                method_name: ""
              route:
                cluster: inbound|9090||
                rate_limits:
                - stage: 0
                  actions:
                  - source_cluster: {}
  - applyTo: CLUSTER
    match:
        cluster:
          service: ratelimit.istio.svc.cluster.local
    patch:
        operation: ADD
        value:
          name: rate_limit_cluster
          type: STRICT_DNS
          connect_timeout: 10s
          lb_policy: ROUND_ROBIN
          http2_protocol_options: {}
          load_assignment:
            cluster_name: rate_limit_cluster
            endpoints:
            - lb_endpoints:
              - endpoint:
                  address:
                     socket_address:
                      address: ratelimit.istio.svc.cluster.local
                      port_value: 8081
```



## 3.7request_mirror_policies

thrift-sample-server-v3.yaml

kubectl apply -f thrift-sample-server-v3.yaml -n istio

```
apiVersion: apps/v1
kind: Deployment
metadata:
  name: thrift-sample-server-v3
  labels:
    app: thrift-sample-server
spec:
  selector:
    matchLabels:
      app: thrift-sample-server
  replicas: 1
  template:
    metadata:
      labels:
        app: thrift-sample-server
        version: v3
    spec:
      containers:
        - name: thrift-sample-server
          image: registry.cn-hangzhou.aliyuncs.com/hxpdocker/thrift-sample-server:latest
          imagePullPolicy: IfNotPresent
          ports:
            - containerPort: 9090
---
```

dr-thrift-v3.yaml

kubectl apply -f dr-thrift-v3.yaml -n istio

```
apiVersion: networking.istio.io/v1alpha3
kind: DestinationRule
metadata:
  name: thrift-sample-server
spec:
  host: thrift-sample-server
  subsets:
  - name: v1
    labels:
      version: v1
  - name: v2
    labels:
      version: v2
  - name: v3
    labels:
      version: v3
```

ef-request_mirror_policies.yaml

kubectl apply -f ef-request_mirror_policies.yaml -n istio

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: thrift-sample-server
spec:
  configPatches:
  - applyTo: NETWORK_FILTER
    match:
      listener:
        name: 10.68.153.249_9090    # sed -i .bak "s/\${thrift-sample-server-vip}/`kubectl get svc thrift-sample-server -n thrift -o=jsonpath='{.spec.clusterIP}'`/" istio/envoyfilter-thrift-proxy.yaml
        filterChain:
          filter:
            name: "envoy.filters.network.tcp_proxy"
    patch:
      operation: REPLACE
      value:
        name: envoy.filters.network.thrift_proxy
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.filters.network.thrift_proxy.v3.ThriftProxy
          stat_prefix: "outbound|9090||thrift-sample-server.thrift.svc.cluster.local"
          transport: AUTO_TRANSPORT
          protocol: AUTO_PROTOCOL
          thrift_filters:
          - name: envoy.filters.thrift.router
          route_config:
            routes:
            - match:
                # empty string matches any request method name
                method_name: ""
              route:
                cluster: "outbound|9090|v2|thrift-sample-server.istio.svc.cluster.local"
                request_mirror_policies:
                  - cluster: outbound|9090|v3|thrift-sample-server.istio.svc.cluster.local
                    runtime_fraction:
                      default_value:
                        numerator: 100
                        denominator: HUNDRED
                      runtime_key: thrift.enforce
  - applyTo: NETWORK_FILTER
    match:
      listener:
        name: virtualInbound
        filterChain:
          destinationPort: 9090
          filter:
            name: "envoy.filters.network.tcp_proxy"
    patch:
      operation: REPLACE
      value:
        name: envoy.filters.network.thrift_proxy
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.filters.network.thrift_proxy.v3.ThriftProxy
          stat_prefix: inbound|9090||
          transport: AUTO_TRANSPORT
          protocol: AUTO_PROTOCOL
          thrift_filters:
          - name: envoy.filters.thrift.router
          route_config:
            routes:
            - match:
                # empty string matches any request method name
                method_name: ""
              route:
                cluster: inbound|9090||
```



## 3.8thrift_filters

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: thrift-sample-server
spec:
  configPatches:
  - applyTo: NETWORK_FILTER
    match:
      listener:
        name: ${thrift-sample-server-vip}_9090    # sed -i .bak "s/\${thrift-sample-server-vip}/`kubectl get svc thrift-sample-server -n thrift -o=jsonpath='{.spec.clusterIP}'`/" istio/envoyfilter-thrift-proxy.yaml
        filterChain:
          filter:
            name: "envoy.filters.network.tcp_proxy"
    patch:
      operation: REPLACE
      value:
        name: envoy.filters.network.thrift_proxy
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.filters.network.thrift_proxy.v3.ThriftProxy
          stat_prefix: "outbound|9090||thrift-sample-server.thrift.svc.cluster.local"
          transport: AUTO_TRANSPORT
          protocol: AUTO_PROTOCOL
          thrift_filters:
          - name: envoy.filters.thrift.ratelimit
            typed_config:
              "@type": type.googleapis.com/envoy.extensions.filters.network.thrift_proxy.filters.ratelimit.v3.RateLimit
              domain: productpage-ratelimit
              stage: 0
              timeout: 3s
              failure_mode_deny: true
              rate_limit_service:
                grpc_service:
                  envoy_grpc:
                    cluster_name: rate_limit_cluster
                  timeout: 10s
                transport_api_version: V3
          - name: envoy.filters.thrift.router
          route_config:
            routes:
            - match:
                # empty string matches any request method name
                method_name: ""
              route:
                rate_limits:
                - stage: 0
                  actions:
                  - source_cluster: {}
                weighted_clusters:
                  clusters:
                    - name: "outbound|9090|v1|thrift-sample-server.istio.svc.cluster.local"
                      weight: 50
                    - name: "outbound|9090|v2|thrift-sample-server.istio.svc.cluster.local"
                      weight: 50
  - applyTo: NETWORK_FILTER
    match:
      listener:
        name: virtualInbound
        filterChain:
          destinationPort: 9090
          filter:
            name: "envoy.filters.network.tcp_proxy"
    patch:
      operation: REPLACE
      value:
        name: envoy.filters.network.thrift_proxy
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.filters.network.thrift_proxy.v3.ThriftProxy
          stat_prefix: inbound|9090||
          transport: AUTO_TRANSPORT
          protocol: AUTO_PROTOCOL
          thrift_filters:
          - name: envoy.filters.thrift.router
          route_config:
            routes:
            - match:
                # empty string matches any request method name
                method_name: ""
              route:
                cluster: inbound|9090||
                rate_limits:
                - stage: 0
                  actions:
                  - source_cluster: {}
  - applyTo: CLUSTER
    match:
        cluster:
          service: ratelimit.istio.svc.cluster.local
    patch:
        operation: ADD
        value:
          name: rate_limit_cluster
          type: STRICT_DNS
          connect_timeout: 10s
          lb_policy: ROUND_ROBIN
          http2_protocol_options: {}
          load_assignment:
            cluster_name: rate_limit_cluster
            endpoints:
            - lb_endpoints:
              - endpoint:
                  address:
                     socket_address:
                      address: ratelimit.istio.svc.cluster.local
                      port_value: 8081
```





```
kubectl delete -f ratelimit-deploy.yaml -n istio
kubectl delete -f ratelimit-config.yaml -n istio
kubectl delete -f thrift-deploy.yaml -n istio
kubectl delete -f thrift-deploy.yaml -n istio-2
kubectl delete envoyfilter thrift-sample-server -n istio-system
```

