# 1什么是buffer filter

buffer filter是envoy的一个http filter，名称是 *envoy.filters.http.**buffer*** 。他的作用是用来缓存请求，可以有效提高访问速度，保护高延迟请求。他可以配置在http_filters上面，也可以配置在host或route上面，覆盖http_filter上的配置。

# 2配置

extensions.filters.http.buffer.v3.Buffer

```
{
  "max_request_bytes": "{...}"最大请求大小
}
```

extensions.filters.http.buffer.v3.BufferPerRoute

```
{
  "disabled": "...",是否禁用host或路由级别buffer
  "buffer": "{...}"buffer参数配置
}
```

buffer：

```
{
  "max_request_bytes": "{...}"最大请求大小
}
```



# 3实战

https://github.com/envoyproxy/envoy/issues/20861

## 3.1http_filters

ef-buffer_filter-http_filters.yaml

kubectl apply -f ef-buffer_filter-http_filters.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: buffer 
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
      operation: INSERT_FIRST
      value: 
        name: envoy.filters.http.buffer
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.filters.http.buffer.v3.Buffer
          max_request_bytes:  102400
```



## 3.2host

ef-buffer_filter-host.yaml

kubectl apply -f ef-buffer_filter-host.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: buffer-2
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
                    typed_per_filter_config:
                      envoy.filters.http.buffer:
                          "@type": type.googleapis.com/envoy.extensions.filters.http.buffer.v3.BufferPerRoute
                          buffer:
                            max_request_bytes:  102400
                          disabled: false
                    routes:
                    - name: testroute
                      match: 
                        path: /productpage
                        case_sensitive: false
                      route:
                        cluster: outbound|9080||productpage.istio.svc.cluster.local
```



## 3.3cluster

ef-buffer_filter-cluster.yaml

kubectl apply -f ef-buffer_filter-cluster.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: buffer-2
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
                        weighted_clusters:
                          clusters:
                          - name: outbound|9080||productpage.istio.svc.cluster.local
                            weight: 100
                            typed_per_filter_config:
                              envoy.filters.http.buffer:
                                "@type": type.googleapis.com/envoy.extensions.filters.http.buffer.v3.BufferPerRoute
                                buffer:
                                  max_request_bytes:  102400
                                disabled: false
                          total_weight: 100
                          runtime_key_prefix: test
```

## 3.4route

ef-buffer_filter-route.yaml

kubectl apply -f ef-buffer_filter-route.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: buffer-2
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
                      typed_per_filter_config:
                        envoy.filters.http.buffer:
                          "@type": type.googleapis.com/envoy.extensions.filters.http.buffer.v3.BufferPerRoute
                          buffer:
                            max_request_bytes:  102400
                          disabled: false
                      route:
                        cluster: outbound|9080||productpage.istio.svc.cluster.local
```

