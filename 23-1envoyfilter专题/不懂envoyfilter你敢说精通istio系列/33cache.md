# 1什么是cache filter

cache filter是一个缓存响应的envoy http类型的filter。可以配置一个存储方式和可以改变的头，可以改变的头决定响应是否可以缓存和请求缓存的头。还在开发中，不建议在生产中使用。

# 2配置

```
{
  "typed_config": "{...}",缓存存储配置
  "allowed_vary_headers": []允许改变的头
}
```

 **typed_config** ：

- [envoy.**cache**.simple_http_cache](https://www.envoyproxy.io/docs/envoy/latest/api-v3/extensions/cache/simple_http_cache/v3/config.proto#extension-envoy-cache-simple-http-cache)

 **allowed_vary_headers** ：

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

# 3实战

https://blog.csdn.net/c_kite/article/details/79567864

ef-cache.yaml

kubectl apply -f ef-cache.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: cache 
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
        name: envoy.filters.http.cache
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.filters.http.cache.v3.CacheConfig
          allowed_vary_headers:
          - prefix: test
          - exact: test1
          - ignore_case: true
            exact: test
          - contains: test
          - safe_regex: 
              google_re2: {}
              regex: ".*test.*"
          typed_config:
            "@type": type.googleapis.com/envoy.extensions.cache.simple_http_cache.v3.SimpleHttpCacheConfig
```

