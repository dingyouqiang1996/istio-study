# 1什么是http header

http header是发送http请求和http响应时的头信息，分为request header和response header,按来源分可以i分为系统产生的头和自定义头。header生效规则如下：首先是weighted cluster，然后是 route level，然后是virtual host level ，最后是 global level 。

# 2有哪些重要http header

| 头名称                             | 说明                                                         |
| ---------------------------------- | ------------------------------------------------------------ |
| :scheme                            | http协议的scheme，比如http，https                            |
| user-agent                         | 上游连接的代理类型，比如某浏览器                             |
| server                             | 服务器名称，比如envoy                                        |
| referer                            | 网页是从哪里连接过来的                                       |
| x-client-trace-id                  | 外部客户端trace id                                           |
| x-envoy-downstream-service-cluster | 内部服务下游服务集群名称，外部服务这个字段wei空              |
| x-enovy-downstream-service-node    | 下游服务节点名称，来自--service-node                         |
| x-envoy-external-address           | 外部服务地址                                                 |
| x-envoy-force-trace                | 是否强制trace                                                |
| x-envoy-internal                   | 判断请求是否来自内部                                         |
| x-envoy-original-dst-host          | 当只用original destination负载均衡时，从这个头获取目标服务器地址 |
| x-forwarded-client-cert            | 客户端和代理证书信息                                         |
| x-forwarded-for                    | 著名的xff，从客户端到服务器，最近客户端的ip地址              |
| x-forwarded-host                   | 最近客户端的host                                             |
| x-forwarded-proto                  | 最近客户端的protocol                                         |
| x-request-id                       | 请求id,唯一标识一个请求                                      |
| x-ot-span-context                  | trace的时候标识父子span                                      |
| x-b3-traceid                       | 用在zipkin中标识一个trace的id                                |
| x-b3-spanid                        | 用在zipkin中，标识一个span的id                               |
| x-b3-parentspanid                  | 用在zipkin中，标识父span的id                                 |
| x-b3-sampled                       | 用在zipkin中，标识是否采样                                   |
| x-b3-flags                         | 用在zipkin中，显示标志位                                     |
| b3                                 | 用在zipkin中，不知干啥                                       |
| x-datadog-trace-id                 | 用在datadog中，标识一个trace id                              |
| x-datadog-parent-id                | 用在datadog中，用来标识父span id                             |
| x-datadog-sampling-priority        | 用在datadog中，标识是否采样                                  |
| sw8                                | 用在skywalking中，用来标识span关系                           |
| x-amzn-trace-id                    | 用在x-ray中，标识trace id                                    |

route中的http header

| http头                                   | 说明                         |
| ---------------------------------------- | ---------------------------- |
| x-envoy-max-retries                      | 重试策略最大重试次数         |
| x-envoy-retry-on                         | 重试条件                     |
| x-envoy-retry-grpc-on                    | grpc重试条件                 |
| x-envoy-retriable-header-names           | 哪个响应头被考虑是可以重试的 |
| x-envoy-retriable-status-code            | 哪个响应码被考虑是可以重试的 |
| x-envoy-upstream-alt-stat-name           | envoy产生stat的头            |
| x-envoy-upstream-rq-timeout-alt-response | 产生204响应码                |
| x-envoy-upstream-rq-timeout-ms           | 上游超时时间                 |
| x-envoy-upstream-rq-per-try-timeout-ms   | 上游每次重试超时时间         |
| x-envoy-hedge-on-per-try-timeout         | hedge每次重试超时时间        |
| x-envoy-decorator-operation              | 覆盖本地定义的span名称       |

fault injection中的http header

| http头                                       | 说明                   |
| -------------------------------------------- | ---------------------- |
| x-envoy-fault-abort-request                  | http 状态码用来abort   |
| x-envoy-fault-abort-grpc-request             | grpc 状态码用来abort   |
| x-envoy-fault-abort-request-percentage       | abort比例              |
| x-envoy-fault-delay-request                  | delay的时间            |
| x-envoy-fault-delay-request-percentage       | delay百分比            |
| x-envoy-fault-throughput-response            | 响应的速率限制         |
| x-envoy-fault-throughput-response-percentage | 采取响应速率限制的比例 |



# 3支持的变量

| 标量名                                          | 说明                                        |
| ----------------------------------------------- | ------------------------------------------- |
| %DOWNSTREAM_REMOTE_ADDRESS%                     | 下游远程地址，包含地址和端口                |
| %DOWNSTREAM_REMOTE_ADDRESS_WITHOUT_PORT%        | 下游远程地址，没有端口                      |
| %DOWNSTREAM_REMOTE_PORT%                        | 下游远程端口                                |
| %DOWNSTREAM_DIRECT_REMOTE_ADDRESS%              | 下游远程直接地址，包含端口                  |
| %DOWNSTREAM_DIRECT_REMOTE_ADDRESS_WITHOUT_PORT% | 下游远程直接地址，没有端口                  |
| %DOWNSTREAM_DIRECT_REMOTE_PORT%                 | 下游远程直接端口                            |
| %DOWNSTREAM_LOCAL_ADDRESS%                      | 下游本地地址，包含端口                      |
| %DOWNSTREAM_LOCAL_ADDRESS_WITHOUT_PORT%         | 下游本地地址，没有端口                      |
| %DOWNSTREAM_LOCAL_PORT%                         | 下游本地端口                                |
| %DOWNSTREAM_LOCAL_URI_SAN%                      | 本地证书SAN的uri，支持tcp和http             |
| %DOWNSTREAM_PEER_URI_SAN%                       | 对端证书SAN的uri，支持tcp和http             |
| %DOWNSTREAM_LOCAL_SUBJECT%                      | 本地证书的subject，支持tcp和http            |
| %DOWNSTREAM_PEER_SUBJECT%                       | 对端证书的subject，支持tcp和http            |
| %DOWNSTREAM_PEER_ISSUER%                        | 对端证书的issuer，支持tcp和http             |
| %DOWNSTREAM_TLS_SESSION_ID%                     | 下游tls连接的session id，支持tcp和http      |
| %DOWNSTREAM_TLS_CIPHER%                         | 下游tls连接的cipher，支持tcp和http          |
| %DOWNSTREAM_TLS_VERSION%                        | 下游tls连接的版本，支持tcp和http            |
| %DOWNSTREAM_PEER_FINGERPRINT_256%               | 对端tls连接的指纹sha256算法，支持tcp和http  |
| %DOWNSTREAM_PEER_FINGERPRINT_1%                 | 对端tls连接的指纹sha1算法，支持tcp和http    |
| %DOWNSTREAM_PEER_SERIAL%                        | 下游连接对端serial，支持tcp和http           |
| %DOWNSTREAM_PEER_CERT%                          | 下游连接对端证书，支持tcp和http             |
| %DOWNSTREAM_PEER_CERT_V_START%                  | 下游连接对端证书校验开始时间，支持tcp和http |
| %DOWNSTREAM_PEER_CERT_V_END%                    | 下游连接对端证书校验结束时间，截止tcp和http |
| %HOSTNAME%                                      | 主机名称                                    |
| %PROTOCOL%                                      | 协议                                        |
| %REQUESTED_SERVER_NAME%                         | sni值，支持tcp和http                        |
| %UPSTREAM_METADATA([“namespace”, “key”, …])%    | 获取上游元数据                              |
| %DYNAMIC_METADATA([“namespace”, “key”, …])%     | 获取动态元数据                              |
| %UPSTREAM_LOCAL_ADDRESS%                        | 上游本地地址，包含端口                      |
| %UPSTREAM_LOCAL_ADDRESS_WITHOUT_PORT%           | 上游本地地址，不包含端口                    |
| %UPSTREAM_LOCAL_PORT%                           | 上游本地端口                                |
| %UPSTREAM_REMOTE_ADDRESS%                       | 上游远程地址，包含端口                      |
| %UPSTREAM_REMOTE_ADDRESS_WITHOUT_PORT%          | 上游远程地址，不包含端口                    |
| %UPSTREAM_REMOTE_PORT%                          | 上游远程端口                                |
| %PER_REQUEST_STATE(reverse.dns.data.name)%      | 获取每个请求状态数据                        |
| %REQ(header-name)%                              | 获取请求头信息                              |
| %START_TIME%                                    | 请求开始时间                                |
| %RESPONSE_FLAGS%                                | 响应标志位                                  |
| %RESPONSE_CODE_DETAILS%                         | 响应码详情                                  |
| %VIRTUAL_CLUSTER_NAME%                          | 虚拟cluster名称                             |



# 4哪里可以用http header

3.1weighted cluster

3.2route

3.3virtual host

3.4global route configuration level

3.5header-to-metadata

4.6fault-injection

4.7loadblancer simple PASSTHROUGH

4.8链路追踪

4.9各种header match



# 5使用案例

## 5.1fault injection

ef-http-header-fault-injection.yaml

kubectl apply -f ef-http-header-fault-injection.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: http-header
spec:
  workloadSelector:
    labels:
      istio: ingressgateway
  configPatches:
  - applyTo: HTTP_FILTER
    match:
      listener:
        #name: 0.0.0.0_8080  
        portNumber: 8080
        filterChain:
          filter:
            name: "envoy.filters.network.http_connection_manager"
            subFilter:
              name: "envoy.filters.http.router"
    patch:
      operation: INSERT_BEFORE
      value:
        name: envoy.filters.http.fault
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.filters.http.fault.v3.HTTPFault
          max_active_faults: 100
          abort:
            header_abort: {}
            percentage:
              numerator: 100
          delay:
            header_delay: {}
            percentage:
              numerator: 100
          response_rate_limit:
            header_limit: {}
            percentage:
              numerator: 100

```

curl http://192.168.229.128:30555/productpage  -H "x-envoy-fault-delay-request: 10000" -H "x-envoy-fault-delay-request-percentage: 100"



curl http://192.168.229.128:30563/productpage  -H "x-envoy-fault-abort-request: 500" -H "x-envoy-fault-abort-request-percentage: 100"



curl http://192.168.229.128:30563/productpage  -H "x-envoy-fault-throughput-response: 1" -H "x-envoy-fault-throughput-response-percentage: 100"



## 5.2header key 大小写处理

ef-http-header-preserve_case.yaml

kubectl apply -f ef-http-header-preserve_case.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: http-header
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
          http_protocol_options:
            header_key_format:
              stateful_formatter:
                name: preserve_case
                typed_config:
                  "@type": type.googleapis.com/envoy.extensions.http.header_formatters.preserve_case.v3.PreserveCaseFormatterConfig
                
                        
```

main.go

```
// Copyright 2020-2021 Tetrate
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package main

import (
        "github.com/tetratelabs/proxy-wasm-go-sdk/proxywasm"
        "github.com/tetratelabs/proxy-wasm-go-sdk/proxywasm/types"
)

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
        return &httpHeaders{contextID: contextID}
}

type httpHeaders struct {
        // Embed the default http context here,
        // so that we don't need to reimplement all the methods.
        types.DefaultHttpContext
        contextID uint32
}

// Override types.DefaultHttpContext.
func (ctx *httpHeaders) OnHttpRequestHeaders(numHeaders int, endOfStream bool) types.Action {
        err := proxywasm.ReplaceHttpRequestHeader("test", "best")
        if err != nil {
                proxywasm.LogCritical("failed to set request header: test")
        }

        hs, err := proxywasm.GetHttpRequestHeaders()
        if err != nil {
                proxywasm.LogCriticalf("failed to get request headers: %v", err)
        }

        for _, h := range hs {
                proxywasm.LogInfof("request header --> %s: %s", h[0], h[1])
        }
        return types.ActionContinue
}

// Override types.DefaultHttpContext.
func (ctx *httpHeaders) OnHttpResponseHeaders(numHeaders int, endOfStream bool) types.Action {
        hs, err := proxywasm.GetHttpResponseHeaders()
        if err != nil {
                proxywasm.LogCriticalf("failed to get response headers: %v", err)
        }

        for _, h := range hs {
                proxywasm.LogInfof("response header <-- %s: %s", h[0], h[1])
        }
        return types.ActionContinue
}

// Override types.DefaultHttpContext.
func (ctx *httpHeaders) OnHttpStreamDone() {
        proxywasm.LogInfof("%d finished", ctx.contextID)
}
```

编译：

```
export GOPROXY=https://proxy.golang.com.cn,direct

tinygo build -o main.wasm -scheduler=none -target=wasi main.go

docker build --tag registry.cn-qingdao.aliyuncs.com/hxpdocker/http_headers:v1 .  --no-cache
docker push registry.cn-qingdao.aliyuncs.com/hxpdocker/http_headers:v1
```

wp-ingressgateway-http_headers.yaml

kubectl apply -f wp-ingressgateway-http_headers.yaml -n istio-system

```
apiVersion: extensions.istio.io/v1alpha1
kind: WasmPlugin
metadata:
  name: ingressgateway
  namespace: istio-system
spec:
  selector:
    matchLabels:
      istio: ingressgateway
  url: oci://registry.cn-qingdao.aliyuncs.com/hxpdocker/http_headers:v1
  phase: STATS
  imagePullPolicy: Always
```

wp-productpage-http_headers.yaml

kubectl apply -f wp-productpage-http_headers.yaml -n istio

```
apiVersion: extensions.istio.io/v1alpha1
kind: WasmPlugin
metadata:
  name: productpage
  namespace: istio
spec:
  selector:
    matchLabels:
      app: productpage
  url: oci://registry.cn-qingdao.aliyuncs.com/hxpdocker/http_headers:v1
  phase: STATS
  imagePullPolicy: Always
```



## 5.3header 匹配

### 5.3.1virtual_hosts routes

ef-route_config-virtual_hosts-routes-match-headers-exact_match.yaml

kubectl apply -f ef-route_config-virtual_hosts-routes-match-headers-exact_match.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: http-header
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
                        path: /tEst
                        case_sensitive: false
                        headers:
                        - name: ":authority"
                          exact_match: 192.168.229.128:30563
                      direct_response:
                        status: 200
                        body: 
                          inline_string: "runtime_fraction"
```

### 5.3.2virtual_hosts virtual_clusters

ef-route_config-virtual_hosts-virtual_clusters.yaml

kubectl apply -f ef-route_config-virtual_hosts-virtual_clusters.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: http-header
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
                    virtual_clusters:
                    - headers:
                      - name: test
                        exact_match: test
                      name: test
                    routes:
                    - name: testroute
                      match: 
                        prefix: /
                      direct_response:
                        status: 200
                        body: 
                          inline_string: "prefix"
```

### 3.3.3fault injection abort

```
cat << EOF > ef-fault-abort-headers.yaml
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: http-header
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

kubectl apply -f  ef-fault-abort-headers.yaml -n istio-system 
```

访问：curl http://192.168.229.128:30563/productpage -H "test:test"

### 3.3.4fault injection delay

```
cat << EOF > ef-fault-delay-headers.yaml
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: http-header
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

kubectl apply -f   ef-fault-delay-headers.yaml -n istio-system
```

访问：curl http://192.168.229.128:30563/productpage -H "test:test"

## 5.4local_ratelimit

```
cat <<EOF > envoyfilter-local-rate-limit.yaml
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: http-header
spec:
  workloadSelector:
    labels:
      app: productpage
  configPatches:
    - applyTo: HTTP_FILTER
      match:
        listener:
          filterChain:
            filter:
              name: "envoy.filters.network.http_connection_manager"
      patch:
        operation: INSERT_BEFORE
        value:
          name: envoy.filters.http.local_ratelimit
          typed_config:
            "@type": type.googleapis.com/udpa.type.v1.TypedStruct
            type_url: type.googleapis.com/envoy.extensions.filters.http.local_ratelimit.v3.LocalRateLimit
            value:
              stat_prefix: http_local_rate_limiter
              token_bucket:
                max_tokens: 10
                tokens_per_fill: 10
                fill_interval: 60s
              filter_enabled:
                runtime_key: local_rate_limit_enabled
                default_value:
                  numerator: 100
                  denominator: HUNDRED
              filter_enforced:
                runtime_key: local_rate_limit_enforced
                default_value:
                  numerator: 100
                  denominator: HUNDRED
              response_headers_to_add:
                - append: false
                  header:
                    key: x-local-rate-limit
                    value: 'true'
EOF

kubectl apply -f envoyfilter-local-rate-limit.yaml -n istio
```

说明：本地限流需要通过EnvoyFilter来实现，他不会请求外部服务，在envoy内部实现支持，是一个令牌桶的算法。http filter的名称必须是envoy.filters.http.local_ratelimit，type和typeurl是固定的，stat_prefix可以随便改，表示生成stat的指标前缀。token_bucket配置令牌桶，max_tokens表示最大令牌数量，tokens_per_fill表示每次填充的令牌数量，fill_interval表示填充令牌的间隔。filter_enabled表示启用但不是强制，filter_enforced表示强制，可以配置百分比。response_headers_to_add修改响应头信息，append为false表示修改，true表示添加。runtime_key 运行时的key，具体有啥用不清楚。



执行压测：

```
[root@node01 45]# go-stress-testing -c 10 -n 10000 -u http://192.168.229.134:30945/productpage

 开始启动  并发数:10 请求数:10000 请求参数: 
request:
 form:http 
 url:http://192.168.229.134:30945/productpage 
 method:GET 
 headers:map[] 
 data: 
 verify:statusCode 
 timeout:30s 
 debug:false 



─────┬───────┬───────┬───────┬────────┬────────┬────────┬────────┬────────┬────────┬────────
 耗时│ 并发数│ 成功数│ 失败数│   qps  │最长耗时│最短耗时│平均耗时│下载字节│字节每秒│ 错误码
─────┼───────┼───────┼───────┼────────┼────────┼────────┼────────┼────────┼────────┼────────
   1s│      7│      2│    761│    2.94│  124.68│    1.98│ 3406.97│  21,476│  21,470│200:2;429:761
   2s│     10│      5│   1636│    2.55│ 1788.46│    1.98│ 3928.11│  52,771│  26,383│200:5;429:1636
   3s│     10│      5│   2962│    1.70│ 1788.46│    1.04│ 5871.68│  76,639│  25,545│200:5;429:2962
   4s│     10│      5│   4459│    1.28│ 1788.46│    1.04│ 7810.78│ 103,585│  25,896│200:5;429:4459
```

429 Too Many Requests (太多请求)

当你需要限制客户端请求某个服务的数量，也就是限制请求速度时，该状态码就会非常有用



## 5.5jwt_authn

RequestAuthentication实现

ra-productpage-jwtrules-fromHeaders.yaml

kubectl apply -f ra-productpage-jwtrules-fromHeaders.yaml -n istio

```
apiVersion: "security.istio.io/v1beta1"
kind: "RequestAuthentication"
metadata:
  name: http-header
spec:
  selector:
    matchLabels:
      app: productpage
  jwtRules:
  - issuer: "testing@secure.istio.io"
    fromHeaders:
    - name: my-token
      prefix: test
    jwks: |
      { "keys":
         [
           {
             "e":"AQAB",
             "kid":"DHFbpoIUqrY8t2zpA2qXfCmr5VO5ZEr4RzHU_-envvQ",
             "kty":"RSA",
             "n":"xAE7eB6qugXyCAG3yhh7pkDkT65pHymX-P7KfIupjf59vsdo91bSP9C8H07pSAGQO1MV_xFj9VswgsCg4R6otmg5PV2He95lZdHtOcU5DXIg_pbhLdKXbi66GlVeK6ABZOUW3WYtnNHD-91gVuoeJT_DwtGGcp4ignkgXfkiEm4sw-4sfb4qdt5oLbyVpmW6x9cfa7vs2WTfURiCrBoUqgBo_-4WTiULmmHSGZHOjzwa8WtrtOQGsAFjIbno85jp6MnGGGZPYZbDAa_b3y5u-YpW7ypZrvD8BgtKVjgtQgZhLAGezMt0ua3DRrWnKqTZ0BJ_EyxOGuHJrLsn00fnMQ"
           }
         ]
      }
```

envoyfilter实现

```
cat << EOF > jwt-productpage-fromHeaders.yaml
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: http-header
spec:
  workloadSelector:
    labels:
      app: productpage
  configPatches:
    - applyTo: HTTP_FILTER
      match:
        context: SIDECAR_INBOUND
        listener:
          filterChain:
            filter:
              name: "envoy.filters.network.http_connection_manager"
              subFilter:
                name: "envoy.filters.http.router"
      patch:
        operation: INSERT_BEFORE
        value:
          name: envoy.filters.http.jwt_authn
          typedConfig:
            '@type': type.googleapis.com/envoy.extensions.filters.http.jwt_authn.v3.JwtAuthentication
            providers:
              origins-0:
                fromHeaders:
                - name: my-token
                  valuePrefix: test
                issuer: testing@secure.istio.io
                localJwks:
                  inlineString: |
                    { "keys":
                       [
                         {
                           "e":"AQAB",
                           "kid":"DHFbpoIUqrY8t2zpA2qXfCmr5VO5ZEr4RzHU_-envvQ",
                           "kty":"RSA",
                           "n":"xAE7eB6qugXyCAG3yhh7pkDkT65pHymX-P7KfIupjf59vsdo91bSP9C8H07pSAGQO1MV_xFj9VswgsCg4R6otmg5PV2He95lZdHtOcU5DXIg_pbhLdKXbi66GlVeK6ABZOUW3WYtnNHD-91gVuoeJT_DwtGGcp4ignkgXfkiEm4sw-4sfb4qdt5oLbyVpmW6x9cfa7vs2WTfURiCrBoUqgBo_-4WTiULmmHSGZHOjzwa8WtrtOQGsAFjIbno85jp6MnGGGZPYZbDAa_b3y5u-YpW7ypZrvD8BgtKVjgtQgZhLAGezMt0ua3DRrWnKqTZ0BJ_EyxOGuHJrLsn00fnMQ"
                         }
                       ]
                    }
                payloadInMetadata: testing@secure.istio.io
            rules:
            - match:
                prefix: /
              requires:
                requiresAny:
                  requirements:
                  - providerName: origins-0
                  - allowMissing: {}
EOF

kubectl apply -f jwt-productpage-fromHeaders.yaml -n istio
```



TOKEN=eyJhbGciOiJSUzI1NiIsImtpZCI6IkRIRmJwb0lVcXJZOHQyenBBMnFYZkNtcjVWTzVaRXI0UnpIVV8tZW52dlEiLCJ0eXAiOiJKV1QifQ.eyJleHAiOjM1MzczOTExMDQsImdyb3VwcyI6WyJncm91cDEiLCJncm91cDIiXSwiaWF0IjoxNTM3MzkxMTA0LCJpc3MiOiJ0ZXN0aW5nQHNlY3VyZS5pc3Rpby5pbyIsInNjb3BlIjpbInNjb3BlMSIsInNjb3BlMiJdLCJzdWIiOiJ0ZXN0aW5nQHNlY3VyZS5pc3Rpby5pbyJ9.EdJnEZSH6X8hcyEii7c8H5lnhgjB5dwo07M5oheC8Xz8mOllyg--AHCFWHybM48reunF--oGaG6IXVngCEpVF0_P5DwsUoBgpPmK1JOaKN6_pe9sh0ZwTtdgK_RP01PuI7kUdbOTlkuUi2AO-qUyOm7Art2POzo36DLQlUXv8Ad7NBOqfQaKjE9ndaPWT7aexUsBHxmgiGbz1SyLH879f7uHYPbPKlpHU6P9S-DaKnGLaEchnoKnov7ajhrEhGXAQRukhDPKUHO9L30oPIr5IJllEQfHYtt6IZvlNUGeLUcif3wpry1R5tBXRicx2sXMQ7LyuDremDbcNy_iE76Upg

curl 192.168.229.128:30563/productpage -H "my-token: test ${TOKEN}"

## 5.6添加删除头

### 5.6.1weighted_clusters

ef-route_config-virtual_hosts-routes-route-weighted_clusters-general.yaml

kubectl apply -f ef-route_config-virtual_hosts-routes-route-weighted_clusters-general.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: http-header
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
                            request_headers_to_add:
                            - header:
                                key: test
                                value: test
                              append: true
                            request_headers_to_remove:
                            - test2
                            response_headers_to_add:
                            - header:
                                key: test3
                                value: test3
                              append: true
                            response_headers_to_remove:
                            - test4
                            host_rewrite_literal: mytest
                          total_weight: 100
                          runtime_key_prefix: test
                       
```

### 5.6.2route_config

ef-route_config-general.yaml

kubectl apply -f ef-route_config-general.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: http-header
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
                        prefix: /
                      direct_response:
                        status: 200
                        body: 
                          inline_string: "test"
                  internal_only_headers:
                  - test5
                  response_headers_to_add:
                  - header:
                      key: test1
                      value: test1 
                    append: true
                  response_headers_to_remove:
                  - test3
                  request_headers_to_add:
                  - header:
                      key: test2
                      value: test2 
                    append: true
                  request_headers_to_remove:
                  - test3
                  most_specific_header_mutations_wins: true
                  validate_clusters: true
                  max_direct_response_body_size_bytes: 1024
```

### 5.6.3routes

ef-route_config-virtual_hosts-routes-general.yaml

kubectl apply -f ef-route_config-virtual_hosts-routes-general.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: http-header
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
                        prefix: /
                      metadata:
                        filter_metadata:
                          "envoy.lb": 
                            canary: true
                      decorator:
                        operation: test
                        propagate: true
                      typed_per_filter_config:
                        envoy.filters.http.bandwidth_limit:
                          "@type": type.googleapis.com/envoy.extensions.filters.http.bandwidth_limit.v3.BandwidthLimit
                          stat_prefix: bandwidth_limiter_custom_route
                          enable_mode: REQUEST_AND_RESPONSE
                          limit_kbps: 500
                          fill_interval: 0.1s
                      request_headers_to_add:
                      - header:
                          key: test1
                          value: test1
                        append: true
                      request_headers_to_remove:
                      - test2
                      response_headers_to_add:
                      - header:
                          key: test3
                          value: test3
                        append: true
                      response_headers_to_remove:
                      - test4
                      per_request_buffer_limit_bytes: 1024
                      direct_response:
                        status: 200
                        body: 
                          inline_string: "prefix"
```



## 5.7header-to-metadata

ef-header-to-metadata-request_rules.yaml

kubectl apply -f ef-header-to-metadata-request_rules.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: http-header
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
        name: envoy.filters.http.header_to_metadata
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.filters.http.header_to_metadata.v3.Config
          request_rules:
            - header: x-version
              on_header_present:
                metadata_namespace: envoy.lb
                key: version
                type: STRING
              on_header_missing:
                metadata_namespace: envoy.lb
                key: default
                value: 'true'
                type: STRING
              remove: false

```



ef-rbac-metadata-header-to-metadata.yaml

kubectl apply -f ef-rbac-metadata-header-to-metadata.yaml  -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: rbac
spec:
  workloadSelector:
    labels:
      istio: ingressgateway
  priority: 20
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
          name: envoy.filters.http.rbac
          typedConfig:
            '@type': type.googleapis.com/envoy.extensions.filters.http.rbac.v3.RBAC
            rules:
              action: DENY
              policies:
                "product-viewer":
                  permissions:
                  - metadata:
                      filter: envoy.lb
                      path:
                      - key: version
                      value: 
                        string_match:
                          exact: "v1"
                  principals:
                  - any: true
```

curl -H "x-version: v1" http://192.168.229.128:30563/productpage



## 5.8loadblancer simple PASSTHROUGH

不使用负载均衡策略

```
no healthy upstream
```

dr-productpage-passthrough.yaml

kubectl apply -f dr-productpage-passthrough.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: DestinationRule
metadata:
  name: productpage
spec:
  host: productpage.istio.svc.cluster.local
  subsets:
  - name: v1
    labels:
      version: v1
  trafficPolicy:
    loadBalancer:
      simple: PASSTHROUGH
```

使用方法，现在cluster配置中设置use_http_header为true，然后在请求中加入header。x-envoy-original-dst-host=10.195.16.237:8888 指向up stream

ef-passthrouth.yaml

kubectl apply -f ef-passthrouth.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: http-header
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
            name: http.9080
            virtual_hosts:
            - name: “*.9080”
              domains:
              - "*"
              routes:
              - match:
                  prefix: /
                  caseSensitive: true
                route:
                  cluster: cluster123
  - applyTo: CLUSTER
    patch:
      operation: ADD
      value: 
        name: "cluster123"
        connect_timeout: 0.5s
        type: ORIGINAL_DST
        lb_policy: CLUSTER_PROVIDED
        original_dst_lb_config:
          use_http_header: true
          http_header_name: x-envoy-original-dst-host 
```

 curl -H "x-envoy-original-dst-host: 172.20.1.16:9080"  http://192.168.229.128:30563/productpage

