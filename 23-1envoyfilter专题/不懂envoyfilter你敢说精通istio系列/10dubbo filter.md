# 1什么是dubbo filter

用来配置dubbo的过滤器，name是envoy.filters.network.dubbo_proxy，是一个网络过滤器。

# 2配置

```
{
  "stat_prefix": "...", stat的前缀
  "protocol_type": "...",协议
  "serialization_type": "...",序列化类型
  "route_config": [],路由配置
  "dubbo_filters": []过滤器
}
```

route_config：

```
{
  "name": "...", 路由名称
  "interface": "...",接口名称，支持通配符
  "group": "...",组
  "version": "...",版本
  "routes": []路由，第一个匹配的将被使用
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
  "method": "{...}",匹配方法
  "headers": []匹配头
}
```

method：

```
{
  "name": "{...}",方法名字
  "params_match": "{...}"参数匹配
}
```

params_match：

```
{
  "exact_match": "...",精确匹配
  "range_match": "{...}"范围匹配
}
```

range_match：

```
{
  "start": "...",开始值
  "end": "..."结束值
}
```

headers：

```
{
  "name": "...",头名称
  "exact_match": "...",精确匹配
  "safe_regex_match": "{...}",正则匹配
  "range_match": "{...}",范围匹配
  "present_match": "...",存在性匹配
  "prefix_match": "...",前缀匹配
  "suffix_match": "...",后缀匹配
  "contains_match": "...",包含匹配
  "string_match": "{...}",string匹配
  "invert_match": "..."反向匹配
}
```

route：

```
{
  "cluster": "...",上游cluster名称
  "weighted_clusters": "{...}"加权cluster配置
}
```

weighted_clusters:

```
{
  "clusters": [],cluster配置
  "total_weight": "{...}",总的权重，默认100
  "runtime_key_prefix": "..."运行时key前缀
}
```

clusters:

```
{
  "name": "...",cluster名称
  "cluster_header": "...",头
  "weight": "{...}",权重
  "metadata_match": "{...}",匹配元数据
  "request_headers_to_add": [],添加请求头
  "request_headers_to_remove": [],删除请求头
  "response_headers_to_add": [],添加响应头
  "response_headers_to_remove": [],删除响应头
  "typed_per_filter_config": "{...}",过滤器配置
  "host_rewrite_literal": "..."host重写
}
```

dubbo_filters:

```
{
  "name": "...",过滤器名称，目前只支持route过滤器
  "config": "{...}"配置
}
```

# 3实战

没有istio crd实现



## 3.1准备工作

provider-deploy.yaml

kubectl apply -f provider-deploy.yaml -n istio

```
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: dubbo-hello-provider-v1
  labels:
    app: dubbo-hello-provider
spec:
  selector:
    matchLabels:
      app: dubbo-hello-provider
      version: v1
  replicas: 1
  template:
    metadata:
      labels:
        app: dubbo-hello-provider
        version: v1
    spec:
      containers:
        - name: dubbo-hello-provider
          image: registry.cn-hangzhou.aliyuncs.com/hxpdocker/dubbo-hello-provider:1.0
          imagePullPolicy: Always
          env:
          - name: dubbo.provider.port
            value: 20880
          resources:
            requests:
              cpu: 0.05
              memory: 500Mi
            limits:
              cpu: 1
              memory: 4Gi
          env:
          - name: version
            value: v1
          ports:
            - containerPort: 20880
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: dubbo-hello-provider-v2
  labels:
    app: dubbo-hello-provider
spec:
  selector:
    matchLabels:
      app: dubbo-hello-provider
      version: v2
  replicas: 1
  template:
    metadata:
      labels:
        app: dubbo-hello-provider
        version: v2
    spec:
      containers:
        - name: dubbo-hello-provider
          image: registry.cn-hangzhou.aliyuncs.com/hxpdocker/dubbo-hello-provider:1.0
          imagePullPolicy: Always
          resources:
            requests:
              cpu: 0.05
              memory: 500Mi
            limits:
              cpu: 1
              memory: 4Gi
          env:
          - name: version
            value: v2
          ports:
            - containerPort: 20880
---
apiVersion: v1
kind: Service
metadata:
  name: dubbo-hello-provider
spec:
 selector:  
   app: dubbo-hello-provider
 type: ClusterIP
 ports:
 -  name: tcp-dubbo
    port: 20880      
    targetPort: 20880
```

consumer-deploy.yaml

kubectl apply -f consumer-deploy.yaml -n istio

```
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: dubbo-hello-consumer
  labels:
    app: dubbo-hello-consumer
spec:
  selector:
    matchLabels:
      app: dubbo-hello-consumer
  replicas: 1
  template:
    metadata:
      labels:
        app: dubbo-hello-consumer
    spec:
      containers:
        - name: dubbo-hello-consumer
          image: registry.cn-beijing.aliyuncs.com/hxpdocker/dubbo-hello-consumer:1.0
          imagePullPolicy: Always
---
apiVersion: v1
kind: Service
metadata:
  name: dubbo-hello-consumer
spec:
 selector:  
   app: dubbo-hello-consumer
 type: ClusterIP
 ports:
 -  name: http
    port: 8081      
    targetPort: 8081

```

vs-dubbo.yaml

kubectl apply -f vs-dubbo.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: dubbo
spec:
  hosts:
  - "*"
  gateways:
  - dubbo-gateway
  http:
  - match:
    - uri:
        prefix: /test
    route:
    - destination:
        host: dubbo-hello-consumer.istio.svc.cluster.local
        port:
          number: 8081

```

gw-dubbo.yaml

kubectl apply -f gw-dubbo.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: Gateway
metadata:
  name: dubbo-gateway
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

dr-dubbo.yaml

kubectl apply -f dr-dubbo.yaml -n istio

```
apiVersion: networking.istio.io/v1alpha3
kind: DestinationRule
metadata:
  name: dubbo-hello-provider
spec:
  host: dubbo-hello-provider
  subsets:
  - name: v1
    labels:
      version: v1
  - name: v2
    labels:
      version: v2
```

访问

```
http://192.168.229.128:30555/test?id=1
```

端点：

```
/test/group1?id=1
/test/group2?id=1
/test/version1?id=1
/test/version2?id=1
```



## 3.2配置envoyfilter

### 3.2.1group,version

**group不生效**

ef-dubbo-group.yaml

kubectl apply -f ef-dubbo-group.yaml -n istio

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: envoyfilter-dubbo-proxy
spec:
  workloadSelector:
    labels:
      app: dubbo-hello-consumer
  configPatches:
  - applyTo: NETWORK_FILTER
    match:
      listener:
        name: 10.68.242.66_20880
        filterChain:
          filter:
            name: "envoy.filters.network.tcp_proxy"
    patch:
      operation: REPLACE
      value:
        name: envoy.filters.network.dubbo_proxy
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.filters.network.dubbo_proxy.v3.DubboProxy
          stat_prefix: outbound|20880||org.example.api
          protocol_type: Dubbo
          serialization_type: Hessian2
          route_config:
          - name: outbound|20880||org.example.api.IGroupService1
            interface: org.example.api.IGroupService
            group: group1
            routes:
            - match:
                method:
                  name:
                    exact: dubboCallProiderService
              route:
                cluster: outbound|20880|v1|dubbo-hello-provider.istio.svc.cluster.local
          - name: outbound|20880||org.example.api.IGroupService2
            interface: org.example.api.IGroupService
            group: group2
            routes:
            - match:
                method:
                  name:
                    exact: dubboCallProiderService
              route:
                cluster: outbound|20880|v2|dubbo-hello-provider.istio.svc.cluster.local
          - name: outbound|20880||org.example.api.IVersionService1
            interface: org.example.api.IVersionService
            version: 1.0.0
            routes:
            - match:
                method:
                  name:
                    exact: dubboCallProiderService
              route:
                cluster: outbound|20880|v1|dubbo-hello-provider.istio.svc.cluster.local
          - name: outbound|20880||org.example.api.IVersionService2
            interface: org.example.api.IVersionService
            version: 2.0.0
            routes:
            - match:
                method:
                  name:
                    exact: dubboCallProiderService
              route:
                cluster: outbound|20880|v2|dubbo-hello-provider.istio.svc.cluster.local
          - name: outbound|20880||org.example.api.ITestService
            interface: org.example.api.ITestService
            routes:
            - match:
                method:
                  name:
                    exact: dubboCallProiderService
              route:
                cluster: outbound|20880|v2|dubbo-hello-provider.istio.svc.cluster.local
|
```

### 3.2.2match-method-name-exact

ef-dubbo-match-method-name-exact.yaml

kubectl apply -f ef-dubbo-match-method-name-exact.yaml -n istio

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: envoyfilter-dubbo-proxy
spec:
  workloadSelector:
    labels:
      app: dubbo-hello-consumer
  configPatches:
  - applyTo: NETWORK_FILTER
    match:
      listener:
        name: 10.68.242.66_20880
        filterChain:
          filter:
            name: "envoy.filters.network.tcp_proxy"
    patch:
      operation: REPLACE
      value:
        name: envoy.filters.network.dubbo_proxy
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.filters.network.dubbo_proxy.v3.DubboProxy
          stat_prefix: outbound|20880||org.example.api.IGroupService
          protocol_type: Dubbo
          serialization_type: Hessian2
          route_config:
          - name: outbound|20880||org.example.api.IGroupService
            interface: org.example.api.ITestService
            routes:
            - match:
                method:
                  name:
                    exact: dubboCallProiderService
              route:
                cluster: outbound|20880|v2|dubbo-hello-provider.istio.svc.cluster.local

```

### 3.2.3match-method-name-prefix

ef-dubbo-match-method-name-prefix.yaml

kubectl apply -f ef-dubbo-match-method-name-prefix.yaml -n istio

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: envoyfilter-dubbo-proxy
spec:
  workloadSelector:
    labels:
      app: dubbo-hello-consumer
  configPatches:
  - applyTo: NETWORK_FILTER
    match:
      listener:
        name: 10.68.242.66_20880
        filterChain:
          filter:
            name: "envoy.filters.network.tcp_proxy"
    patch:
      operation: REPLACE
      value:
        name: envoy.filters.network.dubbo_proxy
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.filters.network.dubbo_proxy.v3.DubboProxy
          stat_prefix: outbound|20880||org.example.api.ITestService
          protocol_type: Dubbo
          serialization_type: Hessian2
          route_config:
          - name: outbound|20880||org.example.api.ITestService
            interface: org.example.api.ITestService
            routes:
            - match:
                method:
                  name:
                    prefix: dubboCall
              route:
                cluster: outbound|20880|v1|dubbo-hello-provider.istio.svc.cluster.local
```



### 3.2.4match-method-name-suffix

ef-dubbo-match-method-name-suffix.yaml

kubectl apply -f ef-dubbo-match-method-name-suffix.yaml -n istio

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: envoyfilter-dubbo-proxy
spec:
  workloadSelector:
    labels:
      app: dubbo-hello-consumer
  configPatches:
  - applyTo: NETWORK_FILTER
    match:
      listener:
        name: 10.68.242.66_20880
        filterChain:
          filter:
            name: "envoy.filters.network.tcp_proxy"
    patch:
      operation: REPLACE
      value:
        name: envoy.filters.network.dubbo_proxy
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.filters.network.dubbo_proxy.v3.DubboProxy
          stat_prefix: outbound|20880||org.example.api.ITestService
          protocol_type: Dubbo
          serialization_type: Hessian2
          route_config:
          - name: outbound|20880||org.example.api.ITestService
            interface: org.example.api.ITestService
            routes:
            - match:
                method:
                  name:
                    suffix: ProiderService
              route:
                cluster: outbound|20880|v2|dubbo-hello-provider.istio.svc.cluster.local
```



### 3.2.5match-method-name-safe_regex

ef-dubbo-match-method-name-safe_regex.yaml

kubectl apply -f ef-dubbo-match-method-name-safe_regex.yaml -n istio

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: envoyfilter-dubbo-proxy
spec:
  workloadSelector:
    labels:
      app: dubbo-hello-consumer
  configPatches:
  - applyTo: NETWORK_FILTER
    match:
      listener:
        name: 10.68.242.66_20880
        filterChain:
          filter:
            name: "envoy.filters.network.tcp_proxy"
    patch:
      operation: REPLACE
      value:
        name: envoy.filters.network.dubbo_proxy
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.filters.network.dubbo_proxy.v3.DubboProxy
          stat_prefix: outbound|20880||org.example.api.ITestService
          protocol_type: Dubbo
          serialization_type: Hessian2
          route_config:
          - name: outbound|20880||org.example.api.ITestService
            interface: org.example.api.ITestService
            routes:
            - match:
                method:
                  name:
                    safe_regex: 
                      google_re2: {}
                      regex: ".*Proider.*"                       
              route:
                cluster: outbound|20880|v1|dubbo-hello-provider.istio.svc.cluster.local
```



### 3.2.6match-method-name-contains

ef-dubbo-match-method-name-contains.yaml

kubectl apply -f ef-dubbo-match-method-name-contains.yaml -n istio

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: envoyfilter-dubbo-proxy
spec:
  workloadSelector:
    labels:
      app: dubbo-hello-consumer
  configPatches:
  - applyTo: NETWORK_FILTER
    match:
      listener:
        name: 10.68.242.66_20880
        filterChain:
          filter:
            name: "envoy.filters.network.tcp_proxy"
    patch:
      operation: REPLACE
      value:
        name: envoy.filters.network.dubbo_proxy
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.filters.network.dubbo_proxy.v3.DubboProxy
          stat_prefix: outbound|20880||org.example.api.ITestService
          protocol_type: Dubbo
          serialization_type: Hessian2
          route_config:
          - name: outbound|20880||org.example.api.ITestService
            interface: org.example.api.ITestService
            routes:
            - match:
                method:
                  name:
                    contains: ProiderService
              route:
                cluster: outbound|20880|v2|dubbo-hello-provider.istio.svc.cluster.local
```



### 3.2.7match-method-name-ignore_case

ef-dubbo-match-method-name-ignore_case.yaml

kubectl apply -f ef-dubbo-match-method-name-ignore_case.yaml -n istio

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: envoyfilter-dubbo-proxy
spec:
  workloadSelector:
    labels:
      app: dubbo-hello-consumer
  configPatches:
  - applyTo: NETWORK_FILTER
    match:
      listener:
        name: 10.68.242.66_20880
        filterChain:
          filter:
            name: "envoy.filters.network.tcp_proxy"
    patch:
      operation: REPLACE
      value:
        name: envoy.filters.network.dubbo_proxy
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.filters.network.dubbo_proxy.v3.DubboProxy
          stat_prefix: outbound|20880||org.example.api.ITestService
          protocol_type: Dubbo
          serialization_type: Hessian2
          route_config:
          - name: outbound|20880||org.example.api.ITestService
            interface: org.example.api.ITestService
            routes:
            - match:
                method:
                  name:
                    contains: PROiderService
                    ignore_case: true
              route:
                cluster: outbound|20880|v1|dubbo-hello-provider.istio.svc.cluster.local
 
```

### 3.2.8match-method-params_match

ef-dubbo-match-method-name-ignore_case.yaml

kubectl apply -f ef-dubbo-match-method-name-ignore_case.yaml -n istio

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: envoyfilter-dubbo-proxy
spec:
  workloadSelector:
    labels:
      app: dubbo-hello-consumer
  configPatches:
  - applyTo: NETWORK_FILTER
    match:
      listener:
        name: 10.68.242.66_20880
        filterChain:
          filter:
            name: "envoy.filters.network.tcp_proxy"
    patch:
      operation: REPLACE
      value:
        name: envoy.filters.network.dubbo_proxy
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.filters.network.dubbo_proxy.v3.DubboProxy
          stat_prefix: outbound|20880||org.example.api.ITestService
          protocol_type: Dubbo
          serialization_type: Hessian2
          route_config:
          - name: outbound|20880||org.example.api.ITestService
            interface: org.example.api.ITestService
            routes:
            - match:
                method:
                  name:
                    contains: PROiderService
                    ignore_case: true
                  params_match:
                    0:
                      exact_match: "1"
              route:
                cluster: outbound|20880|v2|dubbo-hello-provider.istio.svc.cluster.local
```

### 3.2.9match-headers-exact_match

https://www.jianshu.com/p/53d0462e3d65

ef-dubbo-match-headers-exact_match.yaml

kubectl apply -f ef-dubbo-match-headers-exact_match.yaml -n istio

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: envoyfilter-dubbo-proxy
spec:
  workloadSelector:
    labels:
      app: dubbo-hello-consumer
  configPatches:
  - applyTo: NETWORK_FILTER
    match:
      listener:
        name: 10.68.242.66_20880
        filterChain:
          filter:
            name: "envoy.filters.network.tcp_proxy"
    patch:
      operation: REPLACE
      value:
        name: envoy.filters.network.dubbo_proxy
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.filters.network.dubbo_proxy.v3.DubboProxy
          stat_prefix: outbound|20880||org.example.api.ITestService
          protocol_type: Dubbo
          serialization_type: Hessian2
          route_config:
          - name: outbound|20880||org.example.api.ITestService
            interface: org.example.api.ITestService
            routes:
            - match:
                method:
                  name:
                    contains: PROiderService
                    ignore_case: true
                headers:
                - name: ":path"
                  prefix_match: "/"
              route:
                cluster: outbound|20880|v1|dubbo-hello-provider.istio.svc.cluster.local

```

### 3.2.10route-weighted_clusters

ef-dubbo-route-weighted_clusters-name-weight.yaml

kubectl apply -f ef-dubbo-route-weighted_clusters-name-weight.yaml -n istio

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: envoyfilter-dubbo-proxy
spec:
  workloadSelector:
    labels:
      app: dubbo-hello-consumer
  configPatches:
  - applyTo: NETWORK_FILTER
    match:
      listener:
        name: 10.68.242.66_20880
        filterChain:
          filter:
            name: "envoy.filters.network.tcp_proxy"
    patch:
      operation: REPLACE
      value:
        name: envoy.filters.network.dubbo_proxy
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.filters.network.dubbo_proxy.v3.DubboProxy
          stat_prefix: outbound|20880||org.example.api.ITestService
          protocol_type: Dubbo
          serialization_type: Hessian2
          route_config:
          - name: outbound|20880||org.example.api.ITestService
            interface: org.example.api.ITestService
            routes:
            - match:
                method:
                  name:
                    contains: PROiderService
                    ignore_case: true
              route:
                weighted_clusters:
                  clusters:
                  - name: outbound|20880|v1|dubbo-hello-provider.istio.svc.cluster.local
                    weight: 50
                  - name: outbound|20880|v2|dubbo-hello-provider.istio.svc.cluster.local
                    weight: 50
  
```

### 3.2.11request_headers_to_add

ef-dubbo-request_headers_to_add.yaml

kubectl apply -f ef-dubbo-request_headers_to_add.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: envoyfilter-dubbo-proxy
spec:
  configPatches:
  - applyTo: NETWORK_FILTER
    match:
      listener:
        name: 10.68.242.66_20880
        filterChain:
          filter:
            name: "envoy.filters.network.tcp_proxy"
    patch:
      operation: REPLACE
      value:
        name: envoy.filters.network.dubbo_proxy
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.filters.network.dubbo_proxy.v3.DubboProxy
          stat_prefix: outbound|20880||org.example.api.ITestService
          protocol_type: Dubbo
          serialization_type: Hessian2
          route_config:
          - name: outbound|20880||org.example.api.ITestService
            interface: org.example.api.ITestService
            routes:
            - match:
                method:
                  name:
                    contains: PROiderService
                    ignore_case: true
              route:
                weighted_clusters:
                  clusters:
                  - name: outbound|20880|v1|dubbo-hello-provider.istio.svc.cluster.local
                    weight: 50
                    request_headers_to_add:
                    - header:
                        key: test
                        value: test
                      append: true
                  - name: outbound|20880|v2|dubbo-hello-provider.istio.svc.cluster.local
                    weight: 50
                    request_headers_to_add:
                    - header:
                        key: test
                        value: test
                      append: true
```

### 3.2.12response_headers_to_add

ef-dubbo-response_headers_to_add.yaml

kubectl apply -f ef-dubbo-response_headers_to_add.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: envoyfilter-dubbo-proxy
spec:
  configPatches:
  - applyTo: NETWORK_FILTER
    match:
      listener:
        name: 10.68.242.66_20880
        filterChain:
          filter:
            name: "envoy.filters.network.tcp_proxy"
    patch:
      operation: REPLACE
      value:
        name: envoy.filters.network.dubbo_proxy
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.filters.network.dubbo_proxy.v3.DubboProxy
          stat_prefix: outbound|20880||org.example.api.ITestService
          protocol_type: Dubbo
          serialization_type: Hessian2
          route_config:
          - name: outbound|20880||org.example.api.ITestService
            interface: org.example.api.ITestService
            routes:
            - match:
                method:
                  name:
                    contains: PROiderService
                    ignore_case: true
              route:
                weighted_clusters:
                  clusters:
                  - name: outbound|20880|v1|dubbo-hello-provider.istio.svc.cluster.local
                    weight: 50
                    response_headers_to_add:
                    - header:
                        key: test_resp
                        value: test_resp
                      append: true
                  - name: outbound|20880|v2|dubbo-hello-provider.istio.svc.cluster.local
                    weight: 50
                    response_headers_to_add:
                    - header:
                        key: test_resp
                        value: test_resp
                      append: true
```

### 3.2.13request_headers_to_remove

ef-dubbo-request_headers_to_remove.yaml

kubectl apply -f ef-dubbo-request_headers_to_remove.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: envoyfilter-dubbo-proxy
spec:
  configPatches:
  - applyTo: NETWORK_FILTER
    match:
      listener:
        name: 10.68.242.66_20880
        filterChain:
          filter:
            name: "envoy.filters.network.tcp_proxy"
    patch:
      operation: REPLACE
      value:
        name: envoy.filters.network.dubbo_proxy
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.filters.network.dubbo_proxy.v3.DubboProxy
          stat_prefix: outbound|20880||org.example.api.ITestService
          protocol_type: Dubbo
          serialization_type: Hessian2
          route_config:
          - name: outbound|20880||org.example.api.ITestService
            interface: org.example.api.ITestService
            routes:
            - match:
                method:
                  name:
                    contains: PROiderService
                    ignore_case: true
              route:
                weighted_clusters:
                  clusters:
                  - name: outbound|20880|v1|dubbo-hello-provider.istio.svc.cluster.local
                    weight: 50
                    request_headers_to_remove:
                    - "test2"
                  - name: outbound|20880|v2|dubbo-hello-provider.istio.svc.cluster.local
                    weight: 50
                    request_headers_to_remove:
                    - "test2"

```



### 3.2.14metadata_match

ef-dubbo-metadata_match.yaml

kubectl apply -f ef-dubbo-metadata_match.yaml  -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: envoyfilter-dubbo-proxy
spec:
  workloadSelector:
    labels:
      app: dubbo-hello-consumer
  configPatches:
  - applyTo: NETWORK_FILTER
    match:
      listener:
        name: 10.68.242.66_20880
        filterChain:
          filter:
            name: "envoy.filters.network.tcp_proxy"
    patch:
      operation: REPLACE
      value:
        name: envoy.filters.network.dubbo_proxy
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.filters.network.dubbo_proxy.v3.DubboProxy
          stat_prefix: outbound|20880||org.example.api.ITestService
          protocol_type: Dubbo
          serialization_type: Hessian2
          route_config:
          - name: outbound|20880||org.example.api.ITestService
            interface: org.example.api.ITestService
            routes:
            - match:
                method:
                  name:
                    contains: PROiderService
                    ignore_case: true
              route:
                cluster: cluster123
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
          fallback_policy: NO_FALLBACK
          default_subset:
            env: "mark"
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
                    address: dubbo-hello-provider.istio.svc.cluster.local
                    port_value: 20880
              metadata:
                filter_metadata:
                  envoy.lb:
                    env: mark
```







