# 1什么是fault filter

fault filter是envoy用来实现故障注入的一个http类型的filter，名称为 envoy.filters.http.**fault** ，type url为envoy.extensions.filters.http.**fault**.v3.HTTPFault.故障注入有两种类型，一种是注入延迟，还有一种是返回错误。

# 2配置详解

```
{
  "delay": "{...}",  注入延迟
  "abort": "{...}",  注入错误
  "upstream_cluster": "...",匹配上游cluster
  "headers": [], 匹配请求头
  "downstream_nodes": [],匹配下游node
  "max_active_faults": "{...}",最大活跃错误
  "response_rate_limit": "{...}",限流
  "delay_percent_runtime": "...", 下面是运行时key
  "abort_percent_runtime": "...",
  "delay_duration_runtime": "...",
  "abort_http_status_runtime": "...",
  "max_active_faults_runtime": "...",
  "response_rate_limit_percent_runtime": "...",
  "abort_grpc_status_runtime": "...",
  "disable_downstream_cluster_stats": "..."
}
```



# 3实战

## 3.1abort

VirtualService实现

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: bookinfo
  namespace: istio
spec:
  gateways:
  - bookinfo-gateway
  hosts:
  - '*'
  http:
  - fault:
      abort:
        httpStatus: 500
        percentage:
          value: 100
    match:
    - uri:
        exact: /productpage
    - uri:
        prefix: /static
    - uri:
        exact: /login
    - uri:
        exact: /logout
    - uri:
        prefix: /api/v1/products
    route:
    - destination:
        host: productpage
        port:
          number: 9080
```

envoyfilter实现

```
cat << EOF > ef-fault-abort.yaml
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: fault
spec:
  workloadSelector:
    labels:
      istio: ingressgateway
  configPatches:
  - applyTo: NETWORK_FILTER
    match:
      listener:
        #name: 0.0.0.0_8080  
        portNumber: 8080
        filterChain:
          filter:
            name: "envoy.filters.network.http_connection_manager"
    patch:
      operation: MERGE
      value:
        name: envoy.filters.network.http_connection_manager
        typed_config:
          "@type": "type.googleapis.com/envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager"
          codec_type: AUTO
          stat_prefix: ingress_http
          route_config:
            name: http.8080
            virtual_hosts:
            - name: “*.80”
              domains:
              - "*"
              routes:
              - match:
                  path: "/productpage"
                route:
                  cluster: outbound|9080||productpage.istio.svc.cluster.local
                typed_per_filter_config:
                  envoy.filters.http.fault:
                    '@type': type.googleapis.com/envoy.extensions.filters.http.fault.v3.HTTPFault
                    abort:
                      httpStatus: 500
                      percentage:
                        denominator: MILLION
                        numerator: 1000000
              - match:
                  prefix: "/static"
                route:
                  cluster: outbound|9080||productpage.istio.svc.cluster.local
                typed_per_filter_config:
                  envoy.filters.http.fault:
                    '@type': type.googleapis.com/envoy.extensions.filters.http.fault.v3.HTTPFault
                    abort:
                      httpStatus: 500
                      percentage:
                        denominator: MILLION
                        numerator: 1000000
EOF

kubectl apply -f  ef-fault-abort.yaml -n istio-system --context context-cluster1
```

## 3.2delay

VirtualService实现

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: bookinfo
  namespace: istio
spec:
  gateways:
  - bookinfo-gateway
  hosts:
  - '*'
  http:
  - fault:
      delay:
        percentage:
          value: 100.0
        fixedDelay: 7s
    match:
    - uri:
        exact: /productpage
    - uri:
        prefix: /static
    - uri:
        exact: /login
    - uri:
        exact: /logout
    - uri:
        prefix: /api/v1/products
    route:
    - destination:
        host: productpage
        port:
          number: 9080
```

envoyfilter实现

```
cat << EOF > ef-fault-delay.yaml
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: fault
spec:
  workloadSelector:
    labels:
      istio: ingressgateway
  configPatches:
  - applyTo: NETWORK_FILTER
    match:
      listener:
        #name: 0.0.0.0_8080  
        portNumber: 8080
        filterChain:
          filter:
            name: "envoy.filters.network.http_connection_manager"
    patch:
      operation: MERGE
      value:
        name: envoy.filters.network.http_connection_manager
        typed_config:
          "@type": "type.googleapis.com/envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager"
          codec_type: AUTO
          stat_prefix: ingress_http
          route_config:
            name: http.8080
            virtual_hosts:
            - name: “*.80”
              domains:
              - "*"
              routes:
              - match:
                  path: "/productpage"
                route:
                  cluster: outbound|9080||productpage.istio.svc.cluster.local
                typed_per_filter_config:
                  envoy.filters.http.fault:
                    '@type': type.googleapis.com/envoy.extensions.filters.http.fault.v3.HTTPFault
                    delay:
                      fixedDelay: 7s
                      percentage:
                        denominator: MILLION
                        numerator: 1000000
              - match:
                  prefix: "/static"
                route:
                  cluster: outbound|9080||productpage.istio.svc.cluster.local
                typed_per_filter_config:
                  envoy.filters.http.fault:
                    '@type': type.googleapis.com/envoy.extensions.filters.http.fault.v3.HTTPFault
                    delay:
                      fixedDelay: 7s
                      percentage:
                        denominator: MILLION
                        numerator: 1000000
EOF

kubectl apply -f   ef-fault-delay.yaml -n istio-system --context context-cluster1
```

## 3.3headers

VirtualService没有实现方式

envoyfilter实现

### 3.3.1abort

```
cat << EOF > ef-fault-abort-headers.yaml
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: fault
spec:
  workloadSelector:
    labels:
      istio: ingressgateway
  configPatches:
  - applyTo: NETWORK_FILTER
    match:
      listener:
        #name: 0.0.0.0_8080  
        portNumber: 8080
        filterChain:
          filter:
            name: "envoy.filters.network.http_connection_manager"
    patch:
      operation: MERGE
      value:
        name: envoy.filters.network.http_connection_manager
        typed_config:
          "@type": "type.googleapis.com/envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager"
          codec_type: AUTO
          stat_prefix: ingress_http
          route_config:
            name: http.8080
            virtual_hosts:
            - name: “*.80”
              domains:
              - "*"
              routes:
              - match:
                  path: "/productpage"
                route:
                  cluster: outbound|9080||productpage.istio.svc.cluster.local
                typed_per_filter_config:
                  envoy.filters.http.fault:
                    '@type': type.googleapis.com/envoy.extensions.filters.http.fault.v3.HTTPFault
                    abort:
                      httpStatus: 500
                      percentage:
                        denominator: MILLION
                        numerator: 1000000
                    headers:
                    - name: test
                      exact_match: test
              - match:
                  prefix: "/static"
                route:
                  cluster: outbound|9080||productpage.istio.svc.cluster.local
                typed_per_filter_config:
                  envoy.filters.http.fault:
                    '@type': type.googleapis.com/envoy.extensions.filters.http.fault.v3.HTTPFault
                    abort:
                      httpStatus: 500
                      percentage:
                        denominator: MILLION
                        numerator: 1000000
                    headers:
                    - name: test
                      exact_match: test
EOF

kubectl apply -f  ef-fault-abort-headers.yaml -n istio-system --context context-cluster1
```

访问：curl http://192.168.229.134:32688/productpage -H "test:test"

### 3.3.2delay

```
cat << EOF > ef-fault-delay-headers.yaml
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: fault
spec:
  workloadSelector:
    labels:
      istio: ingressgateway
  configPatches:
  - applyTo: NETWORK_FILTER
    match:
      listener:
        #name: 0.0.0.0_8080  
        portNumber: 8080
        filterChain:
          filter:
            name: "envoy.filters.network.http_connection_manager"
    patch:
      operation: MERGE
      value:
        name: envoy.filters.network.http_connection_manager
        typed_config:
          "@type": "type.googleapis.com/envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager"
          codec_type: AUTO
          stat_prefix: ingress_http
          route_config:
            name: http.8080
            virtual_hosts:
            - name: “*.80”
              domains:
              - "*"
              routes:
              - match:
                  path: "/productpage"
                route:
                  cluster: outbound|9080||productpage.istio.svc.cluster.local
                typed_per_filter_config:
                  envoy.filters.http.fault:
                    '@type': type.googleapis.com/envoy.extensions.filters.http.fault.v3.HTTPFault
                    delay:
                      fixedDelay: 7s
                      percentage:
                        denominator: MILLION
                        numerator: 1000000
                    headers:
                    - name: test
                      exact_match: test
              - match:
                  prefix: "/static"
                route:
                  cluster: outbound|9080||productpage.istio.svc.cluster.local
                typed_per_filter_config:
                  envoy.filters.http.fault:
                    '@type': type.googleapis.com/envoy.extensions.filters.http.fault.v3.HTTPFault
                    delay:
                      fixedDelay: 7s
                      percentage:
                        denominator: MILLION
                        numerator: 1000000
                    headers:
                    - name: test
                      exact_match: test
EOF

kubectl apply -f   ef-fault-delay-headers.yaml -n istio-system --context context-cluster1
```

## 3.4 **upstream_cluster** 

VirtualService没有实现方式

envoyfilter实现

### 3.4.1abort

```
cat << EOF > ef-fault-abort-upstream-clusters.yaml
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: fault
spec:
  workloadSelector:
    labels:
      istio: ingressgateway
  configPatches:
  - applyTo: NETWORK_FILTER
    match:
      listener:
        #name: 0.0.0.0_8080  
        portNumber: 8080
        filterChain:
          filter:
            name: "envoy.filters.network.http_connection_manager"
    patch:
      operation: MERGE
      value:
        name: envoy.filters.network.http_connection_manager
        typed_config:
          "@type": "type.googleapis.com/envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager"
          codec_type: AUTO
          stat_prefix: ingress_http
          route_config:
            name: http.8080
            virtual_hosts:
            - name: “*.80”
              domains:
              - "*"
              routes:
              - match:
                  path: "/productpage"
                route:
                  cluster: outbound|9080||productpage.istio.svc.cluster.local
                typed_per_filter_config:
                  envoy.filters.http.fault:
                    '@type': type.googleapis.com/envoy.extensions.filters.http.fault.v3.HTTPFault
                    abort:
                      httpStatus: 500
                      percentage:
                        denominator: MILLION
                        numerator: 1000000
                    upstream_cluster: outbound|9080||productpage.istio.svc.cluster.local
              - match:
                  prefix: "/static"
                route:
                  cluster: outbound|9080||productpage.istio.svc.cluster.local
                typed_per_filter_config:
                  envoy.filters.http.fault:
                    '@type': type.googleapis.com/envoy.extensions.filters.http.fault.v3.HTTPFault
                    abort:
                      httpStatus: 500
                      percentage:
                        denominator: MILLION
                        numerator: 1000000
                    upstream_cluster: outbound|9080||productpage.istio.svc.cluster.local
EOF

kubectl apply -f  ef-fault-abort-upstream-clusters.yaml -n istio-system --context context-cluster1
```

### 3.4.2delay

```
cat << EOF > ef-fault-delay-upstream-clusters.yaml
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: fault
spec:
  workloadSelector:
    labels:
      istio: ingressgateway
  configPatches:
  - applyTo: NETWORK_FILTER
    match:
      listener:
        #name: 0.0.0.0_8080  
        portNumber: 8080
        filterChain:
          filter:
            name: "envoy.filters.network.http_connection_manager"
    patch:
      operation: MERGE
      value:
        name: envoy.filters.network.http_connection_manager
        typed_config:
          "@type": "type.googleapis.com/envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager"
          codec_type: AUTO
          stat_prefix: ingress_http
          route_config:
            name: http.8080
            virtual_hosts:
            - name: “*.80”
              domains:
              - "*"
              routes:
              - match:
                  path: "/productpage"
                route:
                  cluster: outbound|9080||productpage.istio.svc.cluster.local
                typed_per_filter_config:
                  envoy.filters.http.fault:
                    '@type': type.googleapis.com/envoy.extensions.filters.http.fault.v3.HTTPFault
                    delay:
                      fixedDelay: 7s
                      percentage:
                        denominator: MILLION
                        numerator: 1000000
                    upstream_cluster: outbound|9080||productpage.istio.svc.cluster.local
              - match:
                  prefix: "/static"
                route:
                  cluster: outbound|9080||productpage.istio.svc.cluster.local
                typed_per_filter_config:
                  envoy.filters.http.fault:
                    '@type': type.googleapis.com/envoy.extensions.filters.http.fault.v3.HTTPFault
                    delay:
                      fixedDelay: 7s
                      percentage:
                        denominator: MILLION
                        numerator: 1000000
                    upstream_cluster: outbound|9080||productpage.istio.svc.cluster.local
EOF

kubectl apply -f   ef-fault-delay-upstream-clusters.yaml -n istio-system --context context-cluster1
```

## 3.5downstream_nodes

VirtualService没有实现方式

envoyfilter实现

不知道downstream_nodes的值略

### 3.5.1abort

```
cat << EOF > ef-fault-abort-downstream_nodes.yaml
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: fault
spec:
  workloadSelector:
    labels:
      app: productpage
      version: v1
  configPatches:
  - applyTo: HTTP_ROUTE
    match:
      context: SIDECAR_INBOUND
      routeConfiguration:
        vhost:
          name: "inbound|http|9080"
          route:
            action: ANY
    patch:
      operation: MERGE
      value:
                typed_per_filter_config:
                  envoy.filters.http.fault:
                    '@type': type.googleapis.com/envoy.extensions.filters.http.fault.v3.HTTPFault
                    abort:
                      httpStatus: 500
                      percentage:
                        denominator: MILLION
                        numerator: 1000000
                    response_rate_limit:
                      fixed_limit:
                        limit_kbps: 1
                      percentage:
                        denominator: MILLION
                        numerator: 1000000
                    downstream_nodes: 
                    - router~172.20.2.62~istio-ingressgateway-d8859589f-gp4js.istio-system~istio-system.svc.cluster.local
EOF

kubectl apply -f  ef-fault-abort-downstream_nodes.yaml -n istio --context context-cluster1
```

## 3.6max_active_faults

### 3.6.1abort

```
cat << EOF > ef-fault-abort-max_active_faults.yaml
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: fault
spec:
  workloadSelector:
    labels:
      istio: ingressgateway
  configPatches:
  - applyTo: NETWORK_FILTER
    match:
      listener:
        #name: 0.0.0.0_8080  
        portNumber: 8080
        filterChain:
          filter:
            name: "envoy.filters.network.http_connection_manager"
    patch:
      operation: MERGE
      value:
        name: envoy.filters.network.http_connection_manager
        typed_config:
          "@type": "type.googleapis.com/envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager"
          codec_type: AUTO
          stat_prefix: ingress_http
          route_config:
            name: http.8080
            virtual_hosts:
            - name: “*.80”
              domains:
              - "*"
              routes:
              - match:
                  path: "/productpage"
                route:
                  cluster: outbound|9080||productpage.istio.svc.cluster.local
                typed_per_filter_config:
                  envoy.filters.http.fault:
                    '@type': type.googleapis.com/envoy.extensions.filters.http.fault.v3.HTTPFault
                    abort:
                      httpStatus: 500
                      percentage:
                        denominator: MILLION
                        numerator: 100000
                    max_active_faults: 1
              - match:
                  prefix: "/static"
                route:
                  cluster: outbound|9080||productpage.istio.svc.cluster.local
                typed_per_filter_config:
                  envoy.filters.http.fault:
                    '@type': type.googleapis.com/envoy.extensions.filters.http.fault.v3.HTTPFault
                    abort:
                      httpStatus: 500
                      percentage:
                        denominator: MILLION
                        numerator: 100000
                    max_active_faults: 1
EOF

kubectl apply -f  ef-fault-abort-max_active_faults.yaml -n istio-system --context context-cluster1
```

### 3.6.2delay

```
cat << EOF > ef-fault-delay-max_active_faults.yaml
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: fault
spec:
  workloadSelector:
    labels:
      istio: ingressgateway
  configPatches:
  - applyTo: NETWORK_FILTER
    match:
      listener:
        #name: 0.0.0.0_8080  
        portNumber: 8080
        filterChain:
          filter:
            name: "envoy.filters.network.http_connection_manager"
    patch:
      operation: MERGE
      value:
        name: envoy.filters.network.http_connection_manager
        typed_config:
          "@type": "type.googleapis.com/envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager"
          codec_type: AUTO
          stat_prefix: ingress_http
          route_config:
            name: http.8080
            virtual_hosts:
            - name: “*.80”
              domains:
              - "*"
              routes:
              - match:
                  path: "/productpage"
                route:
                  cluster: outbound|9080||productpage.istio.svc.cluster.local
                typed_per_filter_config:
                  envoy.filters.http.fault:
                    '@type': type.googleapis.com/envoy.extensions.filters.http.fault.v3.HTTPFault
                    delay:
                      fixedDelay: 7s
                      percentage:
                        denominator: MILLION
                        numerator: 1000000
                    max_active_faults: 1
              - match:
                  prefix: "/static"
                route:
                  cluster: outbound|9080||productpage.istio.svc.cluster.local
                typed_per_filter_config:
                  envoy.filters.http.fault:
                    '@type': type.googleapis.com/envoy.extensions.filters.http.fault.v3.HTTPFault
                    delay:
                      fixedDelay: 7s
                      percentage:
                        denominator: MILLION
                        numerator: 1000000
                    max_active_faults: 1
EOF

kubectl apply -f  ef-fault-delay-max_active_faults.yaml -n istio-system --context context-cluster1
```



## 3.7response_rate_limit

### 3.7.1abort

定制500异常页：

main.go

```
package main

import (
        "github.com/tetratelabs/proxy-wasm-go-sdk/proxywasm"
        "github.com/tetratelabs/proxy-wasm-go-sdk/proxywasm/types"
)

const clusterName = "httpbin"

func main() {
        proxywasm.SetVMContext(&vmContext{})
}

type vmContext struct {
        // Embed the default VM context here,
        // so that we don't need to reimplement all the methods.
        types.DefaultVMContext
}

// Override types.DefaultVMContext.
func (*vmContext) NewPluginContext(contextID uint32) types.PluginContext {
        return &pluginContext{}
}

type pluginContext struct {
        // Embed the default plugin context here,
        // so that we don't need to reimplement all the methods.
        types.DefaultPluginContext
}

// Override types.DefaultPluginContext.
func (*pluginContext) NewHttpContext(contextID uint32) types.HttpContext {
        return &httpContext{contextID: contextID}
}

type httpContext struct {
        // Embed the default http context here,
        // so that we don't need to reimplement all the methods.
        types.DefaultHttpContext
        contextID uint32
}

// Override types.DefaultHttpContext.
func (ctx *httpContext) OnHttpResponseHeaders(numHeaders int, endOfStream bool) types.Action {
       hs, err := proxywasm.GetHttpResponseHeaders()
       if err != nil {
                proxywasm.LogCriticalf("failed to get response body: %v", err)
                return types.ActionPause
        }

        for _, h := range hs {
             if ":status"==h[0]{
                  if h[1]!="500"{
                    	 return types.ActionContinue
                   }
              }
              proxywasm.LogCriticalf("response header from %s: %s: %s", clusterName, h[0], h[1])
        }
        

        s1:=`
           <html>
             <head>
             	<title>500错误</title>
             </head>
             <body>
               HTTP header manipulation¶
The HTTP connection manager manipulates several HTTP headers both during decoding (when the request is being received) as well as during encoding (when the response is being sent).

:scheme

user-agent

server

referer

x-client-trace-id

x-envoy-downstream-service-cluster

x-envoy-downstream-service-node

x-envoy-external-address

x-envoy-force-trace

x-envoy-internal

x-envoy-original-dst-host

x-forwarded-client-cert

x-forwarded-for

x-forwarded-host

x-forwarded-proto

x-request-id

x-ot-span-context

x-b3-traceid

x-b3-spanid

x-b3-parentspanid

x-b3-sampled

x-b3-flags

b3

x-datadog-trace-id

x-datadog-parent-id

x-datadog-sampling-priority

sw8

x-amzn-trace-id

Custom request/response headers

:scheme¶
Envoy will always set the :scheme header while processing a request. It should always be available to filters, and should be forwarded upstream for HTTP/2 and HTTP/3, where x-forwarded-proto will be sent for HTTP/1.1.

For HTTP/2, and HTTP/3, incoming :scheme headers are trusted and propogated through upstream. For HTTP/1, the :scheme header will be set 1) From the absolute URL if present and valid. An invalid (not “http” or “https”) scheme, or an https scheme over an unencrypted connection will result in Envoy rejecting the request. This is the only scheme validation Envoy performs as it avoids a HTTP/1.1-specific privilege escalation attack for edge Envoys 1 which doesn’t have a comparable vector for HTTP/2 and above 2. 2) From the value of the x-forwarded-proto header after sanitization (to valid x-forwarded-proto from trusted downstreams, otherwise based on downstream encryption level).

This default behavior can be overridden via the scheme_header_transformation configuration option.

The :scheme header will be used by Envoy over x-forwarded-proto where the URI scheme is wanted, for example serving content from cache based on the :scheme header rather than X-Forwarded-Proto, or setting the scheme of redirects based on the scheme of the original URI. See Why is Envoy operating on X-Forwarded-Proto instead of :scheme or vice-versa? for more details.

1
Edge Envoys often have plaintext HTTP/1.1 listeners. If Envoy trusts absolute URL scheme from fully qualfied URLs, a MiTM can adjust relative URLs to https absolute URLs, and inadvertently cause the Envoy’s upstream to send PII or other sensitive data over what it then believes is a secure connection.

2
Unlike HTTP/1.1, HTTP/2 is in practice always served over TLS via ALPN for edge Envoys. In mesh networks using insecure HTTP/2, if the downstream is not trusted to set scheme, the scheme_header_transformation should be used.

user-agent¶
The user-agent header may be set by the connection manager during decoding if the add_user_agent option is enabled. The header is only modified if it is not already set. If the connection manager does set the header, the value is determined by the --service-cluster command line option.

server¶
The server header will be set during encoding to the value in the server_name option.

referer¶
The referer header will be sanitized during decoding. Multiple URLs or invalid URLs will be removed.

x-client-trace-id¶
If an external client sets this header, Envoy will join the provided trace ID with the internally generated x-request-id. x-client-trace-id needs to be globally unique and generating a uuid4 is recommended. If this header is set, it has similar effect to x-envoy-force-trace. See the tracing.client_enabled runtime configuration setting.

x-envoy-downstream-service-cluster¶
Internal services often want to know which service is calling them. This header is cleaned from external requests, but for internal requests will contain the service cluster of the caller. Note that in the current implementation, this should be considered a hint as it is set by the caller and could be easily spoofed by any internal entity. In the future Envoy will support a mutual authentication TLS mesh which will make this header fully secure. Like user-agent, the value is determined by the --service-cluster command line option. In order to enable this feature you need to set the user_agent option to true.

x-envoy-downstream-service-node¶
Internal services may want to know the downstream node request comes from. This header is quite similar to x-envoy-downstream-service-cluster, except the value is taken from the --service-node option.

x-envoy-external-address¶
It is a common case where a service wants to perform analytics based on the origin client’s IP address. Per the lengthy discussion on XFF, this can get quite complicated, so Envoy simplifies this by setting x-envoy-external-address to the trusted client address if the request is from an external client. x-envoy-external-address is not set or overwritten for internal requests. This header can be safely forwarded between internal services for analytics purposes without having to deal with the complexities of XFF.

x-envoy-force-trace¶
If an internal request sets this header, Envoy will modify the generated x-request-id such that it forces traces to be collected. This also forces x-request-id to be returned in the response headers. If this request ID is then propagated to other hosts, traces will also be collected on those hosts which will provide a consistent trace for an entire request flow. See the tracing.global_enabled and tracing.random_sampling runtime configuration settings.

x-envoy-internal¶
It is a common case where a service wants to know whether a request is internal origin or not. Envoy uses XFF to determine this and then will set the header value to true.

This is a convenience to avoid having to parse and understand XFF.

x-envoy-original-dst-host¶
The header used to override destination address when using the Original Destination load balancing policy.

It is ignored, unless the use of it is enabled via use_http_header.

x-forwarded-client-cert¶
x-forwarded-client-cert (XFCC) is a proxy header which indicates certificate information of part or all of the clients or proxies that a request has flowed through, on its way from the client to the server. A proxy may choose to sanitize/append/forward the XFCC header before proxying the request.

The XFCC header value is a comma (“,”) separated string. Each substring is an XFCC element, which holds information added by a single proxy. A proxy can append the current client certificate information as an XFCC element, to the end of the request’s XFCC header after a comma.

Each XFCC element is a semicolon “;” separated string. Each substring is a key-value pair, grouped together by an equals (“=”) sign. The keys are case-insensitive, the values are case-sensitive. If “,”, “;” or “=” appear in a value, the value should be double-quoted. Double-quotes in the value should be replaced by backslash-double-quote (").

The following keys are supported:

By The Subject Alternative Name (URI type) of the current proxy’s certificate. The current proxy’s certificate may contain multiple URI type Subject Alternative Names, each will be a separate key-value pair.

Hash The SHA 256 digest of the current client certificate.

Cert The entire client certificate in URL encoded PEM format.

Chain The entire client certificate chain (including the leaf certificate) in URL encoded PEM format.

Subject The Subject field of the current client certificate. The value is always double-quoted.

URI The URI type Subject Alternative Name field of the current client certificate. A client certificate may contain multiple URI type Subject Alternative Names, each will be a separate key-value pair.

DNS The DNS type Subject Alternative Name field of the current client certificate. A client certificate may contain multiple DNS type Subject Alternative Names, each will be a separate key-value pair.

A client certificate may contain multiple Subject Alternative Name types. For details on different Subject Alternative Name types, please refer RFC 2459.

Some examples of the XFCC header are:

For one client certificate with only URI type Subject Alternative Name: x-forwarded-client-cert: By=http://frontend.lyft.com;Hash=468ed33be74eee6556d90c0149c1309e9ba61d6425303443c0748a02dd8de688;Subject="/C=US/ST=CA/L=San Francisco/OU=Lyft/CN=Test Client";URI=http://testclient.lyft.com

For two client certificates with only URI type Subject Alternative Name: x-forwarded-client-cert: By=http://frontend.lyft.com;Hash=468ed33be74eee6556d90c0149c1309e9ba61d6425303443c0748a02dd8de688;URI=http://testclient.lyft.com,By=http://backend.lyft.com;Hash=9ba61d6425303443c0748a02dd8de688468ed33be74eee6556d90c0149c1309e;URI=http://frontend.lyft.com

For one client certificate with both URI type and DNS type Subject Alternative Name: x-forwarded-client-cert: By=http://frontend.lyft.com;Hash=468ed33be74eee6556d90c0149c1309e9ba61d6425303443c0748a02dd8de688;Subject="/C=US/ST=CA/L=San Francisco/OU=Lyft/CN=Test Client";URI=http://testclient.lyft.com;DNS=lyft.com;DNS=www.lyft.com

How Envoy processes XFCC is specified by the forward_client_cert_details and the set_current_client_cert_details HTTP connection manager options. If forward_client_cert_details is unset, the XFCC header will be sanitized by default.

x-forwarded-for¶
x-forwarded-for (XFF) is a standard proxy header which indicates the IP addresses that a request has flowed through on its way from the client to the server. A compliant proxy will append the IP address of the nearest client to the XFF list before proxying the request. Some examples of XFF are:

x-forwarded-for: 50.0.0.1 (single client)

x-forwarded-for: 50.0.0.1, 40.0.0.1 (external proxy hop)

x-forwarded-for: 50.0.0.1, 10.0.0.1 (internal proxy hop)

Envoy will only append to XFF if the use_remote_address HTTP connection manager option is set to true and the skip_xff_append is set false. This means that if use_remote_address is false (which is the default) or skip_xff_append is true, the connection manager operates in a transparent mode where it does not modify XFF.

Attention

In general, use_remote_address should be set to true when Envoy is deployed as an edge node (aka a front proxy), whereas it may need to be set to false when Envoy is used as an internal service node in a mesh deployment.

The value of use_remote_address controls how Envoy determines the trusted client address. Given an HTTP request that has traveled through a series of zero or more proxies to reach Envoy, the trusted client address is the earliest source IP address that is known to be accurate. The source IP address of the immediate downstream node’s connection to Envoy is trusted. XFF sometimes can be trusted. Malicious clients can forge XFF, but the last address in XFF can be trusted if it was put there by a trusted proxy.

Alternatively, Envoy supports extensions for determining the trusted client address or original IP address.

Note

The use of such extensions cannot be mixed with use_remote_address nor xff_num_trusted_hops.

Envoy’s default rules for determining the trusted client address (before appending anything to XFF) are:

If use_remote_address is false and an XFF containing at least one IP address is present in the request, the trusted client address is the last (rightmost) IP address in XFF.

Otherwise, the trusted client address is the source IP address of the immediate downstream node’s connection to Envoy.

In an environment where there are one or more trusted proxies in front of an edge Envoy instance, the xff_num_trusted_hops configuration option can be used to trust additional addresses from XFF:

If use_remote_address is false and xff_num_trusted_hops is set to a value N that is greater than zero, the trusted client address is the (N+1)th address from the right end of XFF. (If the XFF contains fewer than N+1 addresses, Envoy falls back to using the immediate downstream connection’s source address as trusted client address.)

If use_remote_address is true and xff_num_trusted_hops is set to a value N that is greater than zero, the trusted client address is the Nth address from the right end of XFF. (If the XFF contains fewer than N addresses, Envoy falls back to using the immediate downstream connection’s source address as trusted client address.)

Envoy uses the trusted client address contents to determine whether a request originated externally or internally. This influences whether the x-envoy-internal header is set.
             </body>
           </html>
        `
       if err := proxywasm.SendHttpResponse(200, [][2]string{
                {"powered-by", "proxy-wasm-go-sdk!!"},
        }, []byte(s1), -1); err != nil {
                proxywasm.LogErrorf("failed to send local response: %v", err)
        }
        return types.ActionPause
}

```

```
export GOPROXY=https://proxy.golang.com.cn,direct

tinygo build -o main.wasm -scheduler=none -target=wasi main.go
```

部署envoyfilter：

kubectl create cm wasm --from-file=main.wasm -n istio



productpage-deploy-wasm.yaml

kubectl apply -f productpage-deploy-wasm.yaml -n istio

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
        sidecar.istio.io/userVolumeMount: '[{"name":"wasm", "mountPath":"/var/local/lib/wasm-filters", "readonly":true}]'
        sidecar.istio.io/userVolume: '[{"name":"wasm", "configmap":{"name":"wasm"}}]'
    spec:
      serviceAccountName: bookinfo-productpage
      containers:
      - name: productpage
        image: docker.io/istio/examples-bookinfo-productpage-v1:1.16.4
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
```

ef-wasm-auth.yaml

kubectl apply -f ef-wasm-auth.yaml -n istio

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: wasm
  namespace: istio
spec:
  workloadSelector:
    labels:
      app: productpage
      version: v1
  configPatches:
  - applyTo: HTTP_FILTER
    match:
      context: SIDECAR_INBOUND
      listener:
        filterChain:
          destinationPort: 9080
          filter:
            name: "envoy.filters.network.http_connection_manager"
            subFilter:
              name: "envoy.filters.http.router"
    patch:
      operation: INSERT_FIRST
      value:
        name: envoy.filters.http.wasm
        typed_config:
                '@type': type.googleapis.com/envoy.extensions.filters.http.wasm.v3.Wasm
                config:
                  name: my_plugin
                  configuration:
                    "@type": type.googleapis.com/google.protobuf.StringValue
                    value: |
                      {}
                  vm_config:
                    runtime: "envoy.wasm.runtime.v8"
                    code:
                      local:
                        filename: /var/local/lib/wasm-filters/main.wasm

```



```
cat << EOF > ef-fault-abort-response_rate_limit-http_route.yaml
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: fault
spec:
  workloadSelector:
    labels:
      app: productpage
      version: v1
  configPatches:
  - applyTo: HTTP_ROUTE
    match:
      context: SIDECAR_INBOUND
      routeConfiguration:
        vhost:
          name: "inbound|http|9080"
          route:
            action: ANY
    patch:
      operation: MERGE
      value:
                typed_per_filter_config:
                  envoy.filters.http.fault:
                    '@type': type.googleapis.com/envoy.extensions.filters.http.fault.v3.HTTPFault
                    abort:
                      httpStatus: 500
                      percentage:
                        denominator: MILLION
                        numerator: 1000000
                    response_rate_limit:
                      fixed_limit:
                        limit_kbps: 1
                      percentage:
                        denominator: MILLION
                        numerator: 1000000
EOF

kubectl apply -f  ef-fault-abort-response_rate_limit-http_route.yaml -n istio 
```

### 3.7.2delay

```
cat << EOF > ef-fault-delay-response_rate_limit.yaml
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: fault
spec:
  workloadSelector:
    labels:
      istio: ingressgateway
  configPatches:
  - applyTo: NETWORK_FILTER
    match:
      listener:
        #name: 0.0.0.0_8080  
        portNumber: 8080
        filterChain:
          filter:
            name: "envoy.filters.network.http_connection_manager"
    patch:
      operation: MERGE
      value:
        name: envoy.filters.network.http_connection_manager
        typed_config:
          "@type": "type.googleapis.com/envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager"
          codec_type: AUTO
          stat_prefix: ingress_http
          route_config:
            name: http.8080
            virtual_hosts:
            - name: “*.80”
              domains:
              - "*"
              routes:
              - match:
                  path: "/productpage"
                route:
                  cluster: outbound|9080||productpage.istio.svc.cluster.local
                typed_per_filter_config:
                  envoy.filters.http.fault:
                    '@type': type.googleapis.com/envoy.extensions.filters.http.fault.v3.HTTPFault
                    delay:
                      fixedDelay: 0.1s
                      percentage:
                        denominator: MILLION
                        numerator: 1000000
                    response_rate_limit:
                      fixed_limit:
                        limit_kbps: 1
                      percentage:
                        denominator: MILLION
                        numerator: 1000000
              - match:
                  prefix: "/static"
                route:
                  cluster: outbound|9080||productpage.istio.svc.cluster.local
                typed_per_filter_config:
                  envoy.filters.http.fault:
                    '@type': type.googleapis.com/envoy.extensions.filters.http.fault.v3.HTTPFault
                    delay:
                      fixedDelay: 0.1s
                      percentage:
                        denominator: MILLION
                        numerator: 1000000
                    response_rate_limit:
                      fixed_limit:
                        limit_kbps: 1
                      percentage:
                        denominator: MILLION
                        numerator: 1000000
EOF

kubectl apply -f  ef-fault-delay-response_rate_limit.yaml -n istio-system --context context-cluster1
```

### 3.7.3only

```
cat << EOF > ef-fault-response_rate_limit.yaml
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: fault
spec:
  workloadSelector:
    labels:
      istio: ingressgateway
  configPatches:
  - applyTo: NETWORK_FILTER
    match:
      listener:
        #name: 0.0.0.0_8080  
        portNumber: 8080
        filterChain:
          filter:
            name: "envoy.filters.network.http_connection_manager"
    patch:
      operation: MERGE
      value:
        name: envoy.filters.network.http_connection_manager
        typed_config:
          "@type": "type.googleapis.com/envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager"
          codec_type: AUTO
          stat_prefix: ingress_http
          route_config:
            name: http.8080
            virtual_hosts:
            - name: “*.80”
              domains:
              - "*"
              routes:
              - match:
                  path: "/productpage"
                route:
                  cluster: outbound|9080||productpage.istio.svc.cluster.local
                typed_per_filter_config:
                  envoy.filters.http.fault:
                    '@type': type.googleapis.com/envoy.extensions.filters.http.fault.v3.HTTPFault
                    response_rate_limit:
                      fixed_limit:
                        limit_kbps: 1
                      percentage:
                        denominator: MILLION
                        numerator: 1000000
              - match:
                  prefix: "/static"
                route:
                  cluster: outbound|9080||productpage.istio.svc.cluster.local
                typed_per_filter_config:
                  envoy.filters.http.fault:
                    '@type': type.googleapis.com/envoy.extensions.filters.http.fault.v3.HTTPFault
                    response_rate_limit:
                      fixed_limit:
                        limit_kbps: 1
                      percentage:
                        denominator: MILLION
                        numerator: 1000000
EOF

kubectl apply -f  ef-fault-response_rate_limit.yaml -n istio-system --context context-cluster1
```



## 3.8http request header

https://www.envoyproxy.io/docs/envoy/latest/configuration/http/http_filters/fault_filter.html

### 3.8.1delay

```
cat << EOF > ef-fault-delay-header_abort.yaml
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: fault
spec:
  workloadSelector:
    labels:
      istio: ingressgateway
  configPatches:
  - applyTo: NETWORK_FILTER
    match:
      listener:
        #name: 0.0.0.0_8080  
        portNumber: 8080
        filterChain:
          filter:
            name: "envoy.filters.network.http_connection_manager"
    patch:
      operation: MERGE
      value:
        name: envoy.filters.network.http_connection_manager
        typed_config:
          "@type": "type.googleapis.com/envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager"
          codec_type: AUTO
          stat_prefix: ingress_http
          route_config:
            name: http.8080
            virtual_hosts:
            - name: “*.80”
              domains:
              - "*"
              routes:
              - match:
                  path: "/productpage"
                route:
                  cluster: outbound|9080||productpage.istio.svc.cluster.local
                typed_per_filter_config:
                  envoy.filters.http.fault:
                    '@type': type.googleapis.com/envoy.extensions.filters.http.fault.v3.HTTPFault
                    delay:
                      header_delay: {}
                      percentage:
                        numerator: 100
              - match:
                  prefix: "/static"
                route:
                  cluster: outbound|9080||productpage.istio.svc.cluster.local
                typed_per_filter_config:
                  envoy.filters.http.fault:
                    '@type': type.googleapis.com/envoy.extensions.filters.http.fault.v3.HTTPFault
                    delay:
                      header_delay: {}
                      percentage:
                        numerator: 100
EOF

kubectl apply -f  ef-fault-delay-header_abort.yaml -n istio-system --context context-cluster1
```

```
curl http://192.168.229.128:30555/productpage  -H "x-envoy-fault-delay-request: 10000" -H "x-envoy-fault-delay-request-percentage: 100"

```



### 3.8.2abort

```
cat << EOF > ef-fault-abort-header_abort.yaml
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: fault
spec:
  workloadSelector:
    labels:
      istio: ingressgateway
  configPatches:
  - applyTo: NETWORK_FILTER
    match:
      listener:
        #name: 0.0.0.0_8080  
        portNumber: 8080
        filterChain:
          filter:
            name: "envoy.filters.network.http_connection_manager"
    patch:
      operation: MERGE
      value:
        name: envoy.filters.network.http_connection_manager
        typed_config:
          "@type": "type.googleapis.com/envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager"
          codec_type: AUTO
          stat_prefix: ingress_http
          route_config:
            name: http.8080
            virtual_hosts:
            - name: “*.80”
              domains:
              - "*"
              routes:
              - match:
                  path: "/productpage"
                route:
                  cluster: outbound|9080||productpage.istio.svc.cluster.local
                typed_per_filter_config:
                  envoy.filters.http.fault:
                    '@type': type.googleapis.com/envoy.extensions.filters.http.fault.v3.HTTPFault
                    abort:
                      header_abort: {}
                      percentage:
                        numerator: 100
              - match:
                  prefix: "/static"
                route:
                  cluster: outbound|9080||productpage.istio.svc.cluster.local
                typed_per_filter_config:
                  envoy.filters.http.fault:
                    '@type': type.googleapis.com/envoy.extensions.filters.http.fault.v3.HTTPFault
                    abort:
                      header_abort: {}
                      percentage:
                        numerator: 100
EOF

kubectl apply -f  ef-fault-abort-header_abort.yaml -n istio-system --context context-cluster1
```

```
curl http://192.168.229.128:30555/productpage  -H "x-envoy-fault-abort-request: 500" -H "x-envoy-fault-abort-request-percentage: 100"

[root@node01 fault]# curl http://192.168.229.128:30555/productpage  -H "x-envoy-fault-abort-request: 500" -H "x-envoy-fault-abort-request-percentage: 100"
fault filter abort
```



### 3.8.3ratelimit

```
cat << EOF > ef-fault-header_limit.yaml
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: fault
spec:
  workloadSelector:
    labels:
      istio: ingressgateway
  configPatches:
  - applyTo: NETWORK_FILTER
    match:
      listener:
        #name: 0.0.0.0_8080  
        portNumber: 8080
        filterChain:
          filter:
            name: "envoy.filters.network.http_connection_manager"
    patch:
      operation: MERGE
      value:
        name: envoy.filters.network.http_connection_manager
        typed_config:
          "@type": "type.googleapis.com/envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager"
          codec_type: AUTO
          stat_prefix: ingress_http
          route_config:
            name: http.8080
            virtual_hosts:
            - name: “*.80”
              domains:
              - "*"
              routes:
              - match:
                  path: "/productpage"
                route:
                  cluster: outbound|9080||productpage.istio.svc.cluster.local
                typed_per_filter_config:
                  envoy.filters.http.fault:
                    '@type': type.googleapis.com/envoy.extensions.filters.http.fault.v3.HTTPFault
                    response_rate_limit:
                      header_limit: {}
                      percentage:
                        numerator: 100
              - match:
                  prefix: "/static"
                route:
                  cluster: outbound|9080||productpage.istio.svc.cluster.local
                typed_per_filter_config:
                  envoy.filters.http.fault:
                    '@type': type.googleapis.com/envoy.extensions.filters.http.fault.v3.HTTPFault
                    response_rate_limit:
                      header_limit: {}
                      percentage:
                        numerator: 100
EOF

kubectl apply -f  ef-fault-header_limit.yaml -n istio-system --context context-cluster1
```

```
curl http://192.168.229.128:30555/productpage  -H "x-envoy-fault-throughput-response: 1" -H "x-envoy-fault-throughput-response-percentage: 100"
```

