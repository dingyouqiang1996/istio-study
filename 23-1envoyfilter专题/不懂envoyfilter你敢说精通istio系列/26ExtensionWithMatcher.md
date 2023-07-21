# 1什么是按条件使用过滤器

条件过滤就是HttpConnectionManager的过滤器可以有条件的执行，比如请求头或响应头是某个值时执行这个过滤器，如果匹配某个值就跳过。他可以和Composite过滤器结合，当条件满足时执行某个过滤器。按条件的过滤器是通过ExtensionWithMatcher实现的，其中extension配置要执行的过滤器，matcher匹配条件，条件匹配后执行某个动作。

# 2相关配置

## 2.1ExtensionWithMatcher

```
{
  "matcher": "{...}",匹配
  "xds_matcher": "{...}",匹配条件和动作,还没实现
  "extension_config": "{...}"过滤器扩展配置
}
```

xds_matcher：

```
{
  "matcher_list": "{...}",线性matcher
  "matcher_tree": "{...}",树性matcher
  "on_no_match": "{...}"没有match情况处理
}
```

matcher_list：

```
{
  "matchers": []匹配
}
```

matchers：

```
{
  "predicate": "{...}",判断是否match
  "on_match": "{...}"如果match做什么
}
```

predicate：

```
{
  "single_predicate": "{...}",单条match
  "or_matcher": "{...}",或match
  "and_matcher": "{...}",与match
  "not_matcher": "{...}"非match
}
```

single_predicate：

```
{
  "input": "{...}",输入
  "value_match": "{...}",值匹配
  "custom_match": "{...}"自定义匹配
}
```

input：

- [envoy.matching.common_inputs.environment_variable](https://www.envoyproxy.io/docs/envoy/latest/api-v3/extensions/matching/common_inputs/environment_variable/v3/input.proto#extension-envoy-matching-common-inputs-environment-variable)

value_match：

```
{
  "exact": "...",精确匹配
  "prefix": "...",前缀匹配
  "suffix": "...",后缀匹配
  "safe_regex": "{...}",正则匹配
  "contains": "...",包含匹配
  "ignore_case": "..."忽略大小写
}
```

custom_match：

- [envoy.matching.input_matchers.consistent_hashing](https://www.envoyproxy.io/docs/envoy/latest/api-v3/extensions/matching/input_matchers/consistent_hashing/v3/consistent_hashing.proto#extension-envoy-matching-input-matchers-consistent-hashing)
- [envoy.matching.input_matchers.ip](https://www.envoyproxy.io/docs/envoy/latest/api-v3/extensions/matching/input_matchers/ip/v3/ip.proto#extension-envoy-matching-input-matchers-ip)

matcher_tree：

```
{
  "input": "{...}",输入
  "exact_match_map": "{...}",精确或前缀匹配
  "prefix_match_map": "{...}",前缀匹配
  "custom_match": "{...}"自定义前缀或精确匹配
}
```

exact_match_map，prefix_match_map

```
{
  "map": "{...}"
}
```

map：

```
{
  "matcher": "{...}",匹配
  "action": "{...}"动作
}
```

matcher：

```
{
  "matcher_list": "{...}",线性matcher
  "matcher_tree": "{...}",树性matcher
  "on_no_match": "{...}"没有match情况处理
}
```

input:

type.**matcher**.v3.HttpRequestHeaderMatchInput

type.**matcher**.v3.HttpRequestTrailerMatchInput

type.**matcher**.v3.HttpResponseHeaderMatchInput

type.**matcher**.v3.HttpResponseTrailerMatchInput



config.common.matcher.v3.MatchPredicate

```
{
  "or_match": "{...}",
  "and_match": "{...}",
  "not_match": "{...}",
  "any_match": "...",
  "http_request_headers_match": "{...}",
  "http_request_trailers_match": "{...}",
  "http_response_headers_match": "{...}",
  "http_response_trailers_match": "{...}",
  "http_request_generic_body_match": "{...}",
  "http_response_generic_body_match": "{...}"
}
```



# 3实战

默认istio的envoy是没有启用这个功能的，通过注解启用

istio-custom-bootstrap-config.yaml

kubectl apply -f  istio-custom-bootstrap-config.yaml -n istio

```
apiVersion: v1
kind: ConfigMap
metadata:
  name: istio-custom-bootstrap-config
data:
  custom_bootstrap.json: |
    {
      "layered_runtime": {
        "layers": [
          {
            "name": "static-layer",
            "static_layer": {
              "envoy": {
                "reloadable_features": {
                  "experimental_matching_api": true
                }
              }
            }
          }
        ]
      }
    }

```

productpage-deploy.yaml

kubectl apply -f productpage-deploy.yaml -n istio

```
apiVersion: apps/v1
kind: Deployment
metadata:
  name: productpage-v1
  labels:
    app: productpage
    version: v1
spec:
  replicas: 1
  selector:
    matchLabels:
      app: productpage
      version: v1
  template:
    metadata:
      labels:
        app: productpage
        version: v1
      annotations:
        sidecar.istio.io/bootstrapOverride: "istio-custom-bootstrap-config"
    spec:
      serviceAccountName: bookinfo-productpage
      containers:
      - name: productpage
        image: docker.io/istio/examples-bookinfo-productpage-v1:1.16.2
        imagePullPolicy: IfNotPresent
        ports:
        - containerPort: 9080
        volumeMounts:
        - name: tmp
          mountPath: /tmp
        securityContext:
          runAsUser: 1000
      volumes:
      - name: tmp
        emptyDir: {}
---
```



## 3.1ExtensionWithMatcher

### 3.1.1matcher_list

extension/ef-ExtensionWithMatcher-01.yaml

kubectl apply -f ef-ExtensionWithMatcher-01.yaml -n istio

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: extension
spec:
  workloadSelector:
    labels:
      app: productpage
  configPatches:
  - applyTo: HTTP_FILTER
    match:
      context: SIDECAR_OUTBOUND
      listener:
        name: 0.0.0.0_9080
        filterChain:
          filter:
            name: "envoy.filters.network.http_connection_manager"
            subFilter:
              name: "envoy.filters.http.router"
    patch:
      operation: INSERT_BEFORE
      value: 
       name: test
       typed_config:
              "@type": type.googleapis.com/envoy.extensions.common.matching.v3.ExtensionWithMatcher
              extension_config:
                name: envoy.filters.http.fault
                typed_config:
                  "@type": type.googleapis.com/envoy.extensions.filters.http.fault.v3.HTTPFault
                  abort:
                    http_status: 503
                    percentage:
                      numerator: 100
                      denominator: HUNDRED
                  delay:
                    fixed_delay: 3s
                    percentage:
                      numerator: 100
                      denominator: HUNDRED
              matcher:
                matcher_list:
                  matchers:
                  - predicate:
                      or_matcher:
                        predicate:
                        - single_predicate:
                            input:
                              name: request-headers
                              typed_config:
                                "@type": type.googleapis.com/envoy.type.matcher.v3.HttpRequestHeaderMatchInput
                                header_name: end-user
                            value_match:
                              exact: jason
                        - single_predicate:
                            input:
                              name: request-headers
                              typed_config:
                                "@type": type.googleapis.com/envoy.type.matcher.v3.HttpResponseHeaderMatchInput
                                header_name: test
                            value_match:
                              exact: bar
                    on_match:
                      action:
                        name: skip
                        typed_config:
                          "@type": type.googleapis.com/envoy.extensions.filters.common.matcher.action.v3.SkipFilter
```

### 3.1.2matcher_tree

ef-ExtensionWithMatcher-matcher_tree.yaml

kubectl apply -f ef-ExtensionWithMatcher-matcher_tree.yaml -n istio

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: extension
spec:
  workloadSelector:
    labels:
      app: productpage
  configPatches:
  - applyTo: HTTP_FILTER
    match:
      context: SIDECAR_OUTBOUND
      listener:
        name: 0.0.0.0_9080
        filterChain:
          filter:
            name: "envoy.filters.network.http_connection_manager"
            subFilter:
              name: "envoy.filters.http.router"
    patch:
      operation: INSERT_BEFORE
      value: 
       name: test
       typed_config:
              "@type": type.googleapis.com/envoy.extensions.common.matching.v3.ExtensionWithMatcher
              extension_config:
                name: envoy.filters.http.fault
                typed_config:
                  "@type": type.googleapis.com/envoy.extensions.filters.http.fault.v3.HTTPFault
                  abort:
                    http_status: 503
                    percentage:
                      numerator: 100
                      denominator: HUNDRED
                  delay:
                    fixed_delay: 3s
                    percentage:
                      numerator: 100
                      denominator: HUNDRED
              matcher:
                matcher_tree:
                  input:
                    name: request-headers
                    typed_config:
                      "@type": type.googleapis.com/envoy.type.matcher.v3.HttpRequestHeaderMatchInput
                      header_name: end-user
                  exact_match_map:
                    map:
                      jason: 
                        matcher:
                          matcher_list:
                            matchers:
                            - predicate:
                                or_matcher:
                                  predicate:
                                  - single_predicate:
                                      input:
                                        name: request-headers
                                        typed_config:
                                          "@type": type.googleapis.com/envoy.type.matcher.v3.HttpRequestHeaderMatchInput
                                          header_name: end-user
                                      value_match:
                                        exact: jason
                                  - single_predicate:
                                      input:
                                        name: request-headers
                                        typed_config:
                                          "@type": type.googleapis.com/envoy.type.matcher.v3.HttpRequestHeaderMatchInput
                                          header_name: end-user
                                      value_match:
                                        exact: bar
                              on_match:
                                action:
                                  name: skip
                                  typed_config:
                                    "@type": type.googleapis.com/envoy.extensions.filters.common.matcher.action.v3.SkipFilter
```

### 3.1.3Composite

ef-ExtensionWithMatcher-Composite.yaml

kubectl apply -f ef-ExtensionWithMatcher-Composite.yaml -n istio

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: extension
spec:
  workloadSelector:
    labels:
      app: productpage
  configPatches:
  - applyTo: HTTP_FILTER
    match:
      context: SIDECAR_OUTBOUND
      listener:
        name: 0.0.0.0_9080
        filterChain:
          filter:
            name: "envoy.filters.network.http_connection_manager"
            subFilter:
              name: "envoy.filters.http.router"
    patch:
      operation: INSERT_BEFORE
      value: 
            name: composite
            typed_config:
              "@type": type.googleapis.com/envoy.extensions.common.matching.v3.ExtensionWithMatcher
              extension_config:
                name: composite
                typed_config:
                  "@type": type.googleapis.com/envoy.extensions.filters.http.composite.v3.Composite
              matcher:
                matcher_tree:
                  input:
                    name: request-headers
                    typed_config:
                      "@type": type.googleapis.com/envoy.type.matcher.v3.HttpRequestHeaderMatchInput
                      header_name: end-user
                  exact_match_map:
                    map:
                      "mark":  
                        action:
                          name: composite-action
                          typed_config:
                            "@type": type.googleapis.com/envoy.extensions.filters.http.composite.v3.ExecuteFilterAction
                            typed_config:
                              name: http-fault
                              typed_config:
                                "@type": type.googleapis.com/envoy.extensions.filters.http.fault.v3.HTTPFault
                                delay:
                                  fixed_delay: 1s
                                  percentage:
                                    numerator: 100
                                    denominator: HUNDRED
                      "jason":  
                        action:
                          name: composite-action
                          typed_config:
                            "@type": type.googleapis.com/envoy.extensions.filters.http.composite.v3.ExecuteFilterAction
                            typed_config:
                              name: http-fault
                              typed_config:
                                "@type": type.googleapis.com/envoy.extensions.filters.http.fault.v3.HTTPFault
                                abort:
                                  http_status: 503
                                  percentage:
                                    numerator: 100
                                    denominator: HUNDRED
```

