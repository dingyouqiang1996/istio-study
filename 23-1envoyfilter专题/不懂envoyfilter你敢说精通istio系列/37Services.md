# 1什么service

service指的是envoy提供的grpc或http服务，大多数是grpc的。他的主要作用是与外部系统交互，发送数据，或接收配置。service是envoy重一块很重要的内容，需要重点掌握。

# 2envoy有哪些service

accesslog service（als）

load reporting service（lrs）

Attribute Context

Authorization Service

Health Discovery Service (HDS)

Metrics service

Rate Limit Service (RLS)

Runtime Discovery Service (RTDS)

Client Status Discovery Service (CSDS)

Tap Sink Service

Trace service

Extension Config Discovery Service (ECDS)

External Processing Service

Aggregated Discovery **Service** (ADS)

Cluster discovery **service** (CDS)

gRPC Event Reporting **Service**

Listener discovery **service** (LDS)

Route discovery **service** (RDS)

Secret discovery **service** (SDS)

Virtual Host Discovery **Service** (VHDS)

Endpoint discovery service（eds）

# 3各service介绍

## 3.1accesslog service（als）

als是envoy提供一个grpc服务，主要作用是外部应用（比如skywalking）收集envoy  proxy  istio-proxy container产生的访问日志，收集完可用于分析，告警。

配置方法：

```
  mesh: |-
    accessLogFile: /dev/stdout
    enableEnvoyAccessLogService: true
    defaultConfig:
      envoyAccessLogService:
        address: skywalking-oap.istio-system:11800
      discoveryAddress: istiod.istio-system.svc:15012
      proxyMetadata: {}
      tracing:
        zipkin:
          address: zipkin.istio-system:9411
    enablePrometheusMerge: true
    rootNamespace: istio-system
    trustDomain: cluster.local
  meshNetworks: 'networks: {}'
```



## 3.2load reporting service（lrs）

lrs提供一种机制，envoy可以向管理服务器周期性报告load report。

配置方法：

```
cluster_manager:
  load_stats_config:
    api_type: GRPC
    transport_api_version: V3
    grpc_services:
    - envoy_grpc:
        cluster_name: load_reporting_cluster
```

参考：

https://github.com/envoyproxy/envoy/tree/main/examples/load-reporting-service

## 3.3Attribute Context

attribute是一片描述活动的元数据，例如http请求的大小，http响应的状态码。每个attribute有一个类型和名字，他们是定义attribute context中的。attribute context 是独立attribute的集合用来支持enovy的外部授权系统。

## 3.4Authorization Service

Authorization Service指的是外部授权服务。外部服务可以是grpc服务和可以是http服务。外部授权服务提供了AuthorizationPolicy crd功能以外的外接式授权服务，比如和业务授权系统整合起来。

配置方法：

```
  mesh: |-
    # Add the following contents:
    extensionProviders:
    - name: "opa.istio"
      envoyExtAuthzGrpc:
        service: "opa.istio.svc.cluster.local"
        port: "9191"
```



## 3.5Health Discovery Service (HDS)

健康发现服务

配置方法：

```
hds_config:
  api_type: GRPC
  transport_api_version: v3
  set_node_on_first_message_only: true
  grpc_services:
    envoy_grpc: 
      cluster_name: xds-grpc
    timeout: 3s
```



## 3.6Metrics service

指标服务，用于监控

配置方法：

```
stats_sinks:
- name: envoy.stat_sinks.metrics_service
  typed_config:
    "@type": type.googleapis.com/envoy.config.metrics.v3.MetricsServiceConfig
    transport_api_version: V3
    grpc_service:
      envoy_grpc:
        cluster_name: metrics-server.istio.svc.cluster.local
      timeout: 3s
    report_counters_as_deltas: true
    emit_tags_as_labels: true
```



## 3.7Rate Limit Service (RLS)

全局限速服务

配置方法：

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: filter-ratelimit
  namespace: istio-system
spec:
  workloadSelector:
    labels:
      istio: ingressgateway
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
          name: envoy.filters.http.ratelimit
          typed_config:
            "@type": type.googleapis.com/envoy.extensions.filters.http.ratelimit.v3.RateLimit
            domain: productpage-ratelimit
            failure_mode_deny: true
            rate_limit_service:
              grpc_service:
                envoy_grpc:
                  cluster_name: rate_limit_cluster
                timeout: 10s
              transport_api_version: V3
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



## 3.8Runtime Discovery Service (RTDS)

运行时发现服务

配置方法

```
layered_runtime:
  layers:
  - rtds_layer:
      name: rtds_name
      rtds_config:
        ads: {}
        initial_fetch_timeout: 15s
        resource_api_version: V3
```



## 3.9Client Status Discovery Service (CSDS)

客户端状态发现服务

## 3.10Tap Sink Service

tap槽服务

## 3.11Trace service

链路跟踪服务

配置方法：

```
apiVersion: v1
data:
  mesh: |-
    accessLogFile: /dev/stdout
    enableTracing: true
    defaultConfig:
      discoveryAddress: istiod.istio-system.svc:15012
      meshId: mesh1
      proxyMetadata: {}
      tracing:
        sampling: 100
        zipkin:
          address: zipkin.istio-system:9411
    enablePrometheusMerge: true
    enableTracing: true
    rootNamespace: istio-system
    trustDomain: cluster.local
  meshNetworks: 'networks: {}'
```



## 3.12Extension Config Discovery Service (ECDS)

扩展配置发现服务

配置方法：

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: wasm-example
  namespace: myns
spec:
  configPatches:
  # The first patch defines a named Wasm extension and provides a URL to fetch Wasm binary from,
  # and the binary configuration. It should come before the next patch that applies it.
  # This resource is visible to all proxies in the namespace "myns". It is possible to provide
  # multiple definitions for the same name "my-wasm-extension" in multiple namespaces. We recommend that:
  # - if overriding is desired, then the root level definition can be overriden per namespace with REPLACE.
  # - if overriding is not desired, then the name should be qualified with the namespace "myns/my-wasm-extension",
  #   to avoid accidental name collisions.
  - applyTo: EXTENSION_CONFIG
    patch:
      operation: ADD # REPLACE is also supported, and would override a cluster level resource with the same name.
      value:
        name: my-wasm-extension
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.filters.http.wasm.v3.Wasm
          config:
            root_id: my-wasm-root-id
            vm_config:
              vm_id: my-wasm-vm-id
              runtime: envoy.wasm.runtime.v8
              code:
                remote:
                  http_uri:
                    uri: http://my-wasm-binary-uri
            configuration:
              "@type": "type.googleapis.com/google.protobuf.StringValue"
              value: |
                {}
  # The second patch instructs to apply the above Wasm filter to the listener/http connection manager.
  - applyTo: HTTP_FILTER
    match:
      context: SIDECAR_INBOUND
    patch:
      operation: ADD
      filterClass: AUTHZ # This filter will run *after* the Istio authz filter.
      value:
        name: my-wasm-extension # This must match the name above
        config_discovery:
          config_source:
            api_config_source:
              api_type: GRPC
              transport_api_version: V3
              grpc_services:
              - envoy_grpc:
                  cluster_name: xds-grpc
          type_urls: ["envoy.extensions.filters.http.wasm.v3.Wasm"]

```



## 3.13External Processing Service

外部处理服务，他允许使用一个外部grpc服务来处理请求，修改头或体，或者直接响应连接。

配置方法：

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: bandth 
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
      operation: INSERT_BEFORE
      value: 
        name: envoy.filters.http.ext_proc
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.filters.http.ext_proc.v3.ExternalProcessor
          grpc_service:
            envoy_grpc:
              cluster_name: external-server.istio.svc.cluster.local
            timeout: 3s
          failure_mode_allow: true
          message_timeout: 200ms
```



## 3.14Aggregated Discovery Service (ADS)

ads是cds，lds，rds，eds的总称。

配置方式

```
    dynamic_resources:
      ads_config:
        api_type: GRPC
        grpc_services:
        - envoy_grpc:
            cluster_name: xds-grpc
        set_node_on_first_message_only: true
        transport_api_version: V3
```



## 3.15Cluster discovery service (CDS)

cds是集群发现服务，用于发现cluster，然后发送到envoy

配置方式：

```
   dynamic_resources:
     cds_config:
        ads: {}
        initial_fetch_timeout: 0s
        resource_api_version: V3
```



## 3.16gRPC Event Reporting Service

事件报告服务

## 3.17Listener discovery service (LDS)

lds是监听器发现服务，用于发现listener，然后发送到envoy

配置方式：

```
    dynamic_resources:
      lds_config:
        ads: {}
        initial_fetch_timeout: 0s
        resource_api_version: V3
```



## 3.18Route discovery service (RDS)

rds是路由发现服务，用于发现路由，然后发送到envoy

配置方式

```
route_config_name: some_route_name
config_source:
  resource_api_version: V3
  api_config_source:
    api_type: GRPC
    transport_api_version: V3
    grpc_services:
      envoy_grpc:
        cluster_name: some_xds_cluster
```



## 3.19Secret discovery service (SDS)

sds是secret发现服务，用于发现secret，然后发送到envoy

配置方式：

```
name: some_secret_name
config_source:
  resource_api_version: V3
  api_config_source:
    api_type: GRPC
    transport_api_version: V3
    grpc_services:
      envoy_grpc:
        cluster_name: some_xds_cluster
```



## 3.20Virtual Host Discovery Service (VHDS)

vhds是虚拟主机发现服务，用于发现virtual host，然后发送到envoy

配置方式：

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: httpconnectionmanager
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
                  vhds:
                    config_source:
                      ads: {}
                      initial_fetch_timeout: 15s
```



## 3.21Endpoint discovery service（eds）

eds是endpoint 发现服务，用于发现endpoint，然后发送到envoy

配置方式

```
eds_config:
  resource_api_version: V3
  api_config_source:
    api_type: REST
    transport_api_version: V3
    cluster_names: [some_xds_cluster]
```

