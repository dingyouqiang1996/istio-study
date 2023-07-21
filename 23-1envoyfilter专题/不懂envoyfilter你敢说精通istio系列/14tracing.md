# 1什么是tracing

tracing是isito三类遥测技术之一，其他两类分别是metric，log。 Istio为每种服务生成分布式跟踪范围，从而使我们可以详细了解网格中的调用流程和服务依赖性。 可以看到每个步骤的调用时间，可用来分析慢的微服务。tracing为定位错误，和系统优化，提供了支持。

# 2配置点

bootstrap

HttpConnectionManager

route

mesh配置文件

crd telemetry 配置

# 3配置

## 3.1bootstrap tracing配置

```
{
  "http": "{...}"http tracer
}
```

http：

```
{
  "name": "...",tracer名称
  "typed_config": "{...}"tracer配置
}
```



typed_config：

- [envoy.tracers.zipkin](https://www.envoyproxy.io/docs/envoy/latest/api-v3/config/trace/v3/zipkin.proto#extension-envoy-tracers-zipkin)

```
{
  "collector_cluster": "...",tracer集群名称
  "collector_endpoint": "...",路径，一般为/api/v2/spans
  "trace_id_128bit": "...",是否使用128位id
  "shared_span_context": "{...}",客户端和服务器是否共享同一个span上下文，默认true
  "collector_endpoint_version": "...",端点版本
  "collector_hostname": "..."发送span到tracer服务器时的hostname，默认collector_cluster相同
}
```

collector_endpoint_version：

Available Zipkin collector endpoint versions.

- DEPRECATED_AND_UNAVAILABLE_DO_NOT_USE

  *(DEFAULT)* ⁣Zipkin API v1, JSON over HTTP.

- HTTP_JSON

  ⁣Zipkin API v2, JSON over HTTP.

- HTTP_PROTO

  ⁣Zipkin API v2, protobuf over HTTP.

## 3.2HttpConnectionManager tracing配置

tracing:

```
{
  "client_sampling": "{...}",被采样的客户端请求的百分比，默认100%
  "random_sampling": "{...}",随机采样率，默认100%
  "overall_sampling": "{...}",采样限值
  "verbose": "...",是否给span添加额外信息
  "max_path_tag_length": "{...}",最大路径长度，默认256
  "custom_tags": [],给span创建的自定义tag
  "provider": "{...}"tracer提供者
}
```

custom_tags：

```
{
  "tag": "...",tag名称
  "literal": "{...}",literal类型值
  "environment": "{...}",环境变量值
  "request_header": "{...}",请求头类型值
  "metadata": "{...}"元数据类型值
}
```

literal：

```
{
  "value": "..."值
}
```

environment：

```
{
  "name": "...",环境变量名称
  "default_value": "..."默认值
}
```

request_header：

```
{
  "name": "...",请求头名称
  "default_value": "..."默认值
}
```

metadata：

```
{
  "kind": "{...}",元数据类型
  "metadata_key": "{...}",元数据key
  "default_value": "..."默认值
}
```

kind：

```
{
  "request": "{...}",请求类型
  "route": "{...}",路由类型
  "cluster": "{...}",集群类型
  "host": "{...}"主机类型
}
```

metadata_key：

```
{
  "key": "...",元数据键，元数据名称空间
  "path": []路径
}
```

path：

```
{
  "key": "..."路径名
}
```

## 3.3route tracing配置

同上：

```
{
  "client_sampling": "{...}",
  "random_sampling": "{...}",
  "overall_sampling": "{...}",
  "custom_tags": []
}
```

## 3.4telemetry crd配置

```
[root@node01 ~]# kubectl explain telemetry.spec.tracing
KIND:     Telemetry
VERSION:  telemetry.istio.io/v1alpha1

RESOURCE: tracing <[]Object>

DESCRIPTION:
     Optional.

FIELDS:
   customTags   <map[string]Object>  自定义标签
     Optional.

   disableSpanReporting <>   是否警用span报告
     Controls span reporting.

   providers    <[]Object>  提供者
     Optional.

   randomSamplingPercentage     <> 随机采用率
   
   
[root@node01 ~]# kubectl explain telemetry.spec.tracing.customTags
KIND:     Telemetry
VERSION:  telemetry.istio.io/v1alpha1

RESOURCE: customTags <map[string]Object>

DESCRIPTION:
     Optional.

FIELDS:
   environment  <Object>  来自环境变量值
     Environment adds the value of an environment variable to each span.

   header       <Object>   来自请求头
     RequestHeader adds the value of an header from the request to each span.

   literal      <Object>   字面常量
     Literal adds the same, hard-coded value to each span.

```



# 4实战

## 4.1全局配置

```
[root@node01 datadog]# kubectl get cm istio -n istio-system -o yaml
apiVersion: v1
data:
  mesh: |-
    accessLogFile: /dev/stdout
    enableTracing: true
    defaultConfig:
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

这里主要配置两个地方：

一个是：enableTracing: true

表示启用tracing

另一个是：

```
      tracing:
        zipkin:
          address: zipkin.istio-system:9411
```

配置tracing后端地址。



配置完这个后所有的proxy bootstrap都会生成配置：

![1](14image\1.jpg)



![2](14image\2.jpg)



## 4.2去掉全局配置，改用envoyfilter

```
apiVersion: v1
data:
  mesh: |-
    accessLogFile: /dev/stdout
    enableTracing: false
    defaultConfig:
      discoveryAddress: istiod.istio-system.svc:15012
      proxyMetadata: {}
      tracing: {}
    enablePrometheusMerge: true
    rootNamespace: istio-system
    trustDomain: cluster.local
  meshNetworks: 'networks: {}'
```

我们把zipkin装到别的名称空间。

```
[root@node01 tracing]# cat zipkin.yaml 
apiVersion: apps/v1
kind: Deployment
metadata:
  name: zipkin
  labels:
    app: zipkin
spec:
  selector:
    matchLabels:
      app: zipkin
  template:
    metadata:
      labels:
        app: zipkin
      annotations:
        sidecar.istio.io/inject: "false"
    spec:
      containers:
        - name: zipkin
          image: openzipkin/zipkin-slim:2.23.0
          env:
            - name: STORAGE_METHOD
              value: "mem"
          readinessProbe:
            httpGet:
              path: /health
              port: 9411
            initialDelaySeconds: 5
            periodSeconds: 5
---
apiVersion: v1
kind: Service
metadata:
  name: tracing
  labels:
    app: zipkin
spec:
  type: ClusterIP
  ports:
    - name: http-query
      port: 80
      protocol: TCP
      targetPort: 9411
  selector:
    app: zipkin
---
apiVersion: v1
kind: Service
metadata:
  labels:
    name: zipkin
  name: zipkin
spec:
  ports:
    - port: 9411
      targetPort: 9411
      name: http-query
  selector:
    app: zipkin
```

kubectl create ns zipkin

kubectl apply -f zipkin.yaml  -n zipkin

```
  meshConfig:
      proxyMetadata:
        # Enable dynamic bootstrap generation
        # https://github.com/istio/istio/pull/33456
        BOOTSTRAP_XDS_AGENT: "true"
```

使用envoyfilter

ef-tracing-zipkin.yaml

 kubectl apply -f ef-tracing-zipkin.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: tracing
  namespace: istio-system
spec:
  configPatches:
  - applyTo: BOOTSTRAP
    patch:
      operation: MERGE
      value:
        node:
          metadata:
            PROXY_CONFIG:
              tracing:
                zipkin:
                  address: zipkin.zipkin:9411
        tracing:
          http:
            name: envoy.tracers.zipkin
            typed_config:
              "@type": type.googleapis.com/envoy.config.trace.v3.ZipkinConfig
              collector_cluster: outbound|9411||zipkin.zipkin.svc.cluster.local
              collector_endpoint: /api/v2/spans
              trace_id_128bit: true
              shared_span_context: false
              collector_endpoint_version: HTTP_JSON
```

## 4.3HttpConnectionManager tracing配置

### 4.3.1sampling

ef-tracing-sampling.yaml

kubectl apply -f ef-tracing-sampling.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: tracing
spec:
  configPatches:
  - applyTo: NETWORK_FILTER
    match:
      listener:
        filterChain:
          filter:
            name: "envoy.filters.network.http_connection_manager"
    patch:
      operation: MERGE
      value:
              name: envoy.filters.network.http_connection_manager
              typedConfig:
                '@type': type.googleapis.com/envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager
                tracing:
                  client_sampling:
                    value: 40
                  random_sampling:
                    value: 50
                  overall_sampling:
                    value: 45 
```

### 4.3.2 custom_tags 

ef-tracing-custom_tags.yaml

kubectl apply -f ef-tracing-custom_tags.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: tracing
spec:
  configPatches:
  - applyTo: NETWORK_FILTER
    match:
      listener:
        filterChain:
          filter:
            name: "envoy.filters.network.http_connection_manager"
    patch:
      operation: MERGE
      value:
              name: envoy.filters.network.http_connection_manager
              typedConfig:
                '@type': type.googleapis.com/envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager
                tracing:
                  client_sampling:
                    value: 40
                  random_sampling:
                    value: 50
                  overall_sampling:
                    value: 45 
                  custom_tags:
                  - tag: test1
                    literal:
                      value: test1
                  - tag: test2
                    environment:
                      name: myenv
                      default_value: test2
                  - tag: test3
                    request_header:
                      name: test3
                      default_value: test3
                  - tag: test4
                    metadata:
                      kind: 
                        request: {}
                      metadata_key:
                        key: envoy.lb
                        path:
                        - key: test4
                      default_value: test4
```

### 4.3.3provder

ef-tracing-provider.yaml

kubectl apply -f ef-tracing-provider.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: tracing
spec:
  configPatches:
  - applyTo: NETWORK_FILTER
    match:
      listener:
        filterChain:
          filter:
            name: "envoy.filters.network.http_connection_manager"
    patch:
      operation: MERGE
      value:
              name: envoy.filters.network.http_connection_manager
              typedConfig:
                '@type': type.googleapis.com/envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager
                tracing:
                  client_sampling:
                    value: 40
                  random_sampling:
                    value: 50
                  overall_sampling:
                    value: 45 
                  verbose: true
                  max_path_tag_length: 256
                  provider:
                    name: envoy.tracers.zipkin
                    typed_config:
                      "@type": type.googleapis.com/envoy.config.trace.v3.ZipkinConfig
                      collector_cluster: outbound|9411||zipkin.istio-system.svc.cluster.local
                      collector_endpoint: /api/v2/spans
                      trace_id_128bit: true
                      shared_span_context: false
                      collector_endpoint_version: HTTP_JSON
```



## 4.4telemetry crd配置

```
data:
  mesh: |-
      extensionProviders: 
      - name: "localtrace"
        zipkin:
          service: "zipkin.istio-system.svc.cluster.local"
          port: 9411
          maxTagLength: 56
      - name: "cloudtrace"
        stackdriver:
          maxTagLength: 256
      - skywalking:
          service: skywalking-oap.istio-system.svc.cluster.local
          port: 11800
        name: envoy.tracers.skywalking
```

telemetry-tracing.yaml

kubectl apply -f telemetry-tracing.yaml -n istio-system

```
apiVersion: telemetry.istio.io/v1alpha1
kind: Telemetry
metadata:
  name: tracing
spec:
  tracing:
  - customTags:
      test1:
        environment:
          name: test1
          defaultValue: test1    
      test2:
        header:
          name: test2
          defaultValue: test2
      test3:
        literal:
          value: test3
    providers:
    - name: localtrace
    randomSamplingPercentage: 30
    
```



禁用tracing

telemetry-tracing-disableSpanReporting.yaml

kubectl apply -f telemetry-tracing-disableSpanReporting.yaml -n istio-system

```
apiVersion: telemetry.istio.io/v1alpha1
kind: Telemetry
metadata:
  name: tracing
spec:
  tracing:
  - disableSpanReporting: true
    providers:
    - name: zipkin
```



## 4.5route tracing配置

ef-tracing-route.yaml

kubectl apply -f ef-tracing-route.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: tracing
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
                        path: /productpage
                        case_sensitive: false
                      route:
                        cluster: outbound|9080||productpage.istio.svc.cluster.local
                      tracing:
                        custom_tags:
                        - tag: test1
                          literal:
                            value: test1
                        - tag: test2
                          environment:
                            name: myenv
                            default_value: test2
                        - tag: test3
                          request_header:
                            name: test3
                            default_value: test3
                        - tag: test4
                          metadata:
                            kind: 
                              request: {}
                            metadata_key:
                              key: envoy.lb
                              path:
                              - key: test4
                            default_value: test4
                        overallSampling:
                          numerator: 20
                          denominator: HUNDRED
                        randomSampling:
                          numerator: 30
                          denominator: HUNDRED
                        clientSampling:
                          numerator: 30
                          denominator: HUNDRED
```

