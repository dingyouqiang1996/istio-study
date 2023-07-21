# 1什么是attribute

attribute是envoy在处理请求和连接时的上下文属性，他们都过点分割的路径命名（比如request.path）,有固定的类型（比如string或int），可能在上下文中存在或不存在。属性暴露给CEL运行时在rbac过滤器中或wasm扩展中通过get_property接口获取。

# 2attribute数据类型

| string                   | UTF-8字符串             |
| ------------------------ | ----------------------- |
| bytes                    | 字节缓存                |
| int                      | 64位有符号整型          |
| uint                     | 64位无符号整型          |
| bool                     | boolean类型             |
| list                     | 值列表                  |
| map                      | 键值对                  |
| timestamp                | 时间类型                |
| duration                 | 时间长度类型            |
| protocol buffer  message | protocol buffer协议类型 |



# 3有哪些类型attribute

## 3.1请求属性

| request.path      | string             | 请求路径                                                |
| ----------------- | ------------------ | ------------------------------------------------------- |
| request.url_path  | string             | 没有query string的path                                  |
| request.host      | string             | url的host                                               |
| request.scheme    | string             | url的scheme，比如http                                   |
| request.method    | string             | 请求方法，比如GET                                       |
| request.headers   | map<string,string> | 所有请求头                                              |
| request.referer   | string             | 请求的referer头                                         |
| request.useragent | string             | user agent请求头                                        |
| request.time      | timestamp          | 第一个字节到达的时间                                    |
| request.id        | string             | 根据x-request-id请求头值确定的request id                |
| request.protocol  | string             | 请求协议(“HTTP/1.0”, “HTTP/1.1”, “HTTP/2”, or “HTTP/3”) |

请求完成后额外的属性

| request.duration   | duration | 请求时间                     |
| ------------------ | -------- | ---------------------------- |
| request.size       | int      | 请求body的大小               |
| request.total_size | int      | 请求的总大小，包括未压缩的头 |



## 3.2响应属性

响应属性，只有请求完成后才有效。并且只在http filter中有效。

| 属性                  | 类型               | 说明                                  |
| --------------------- | ------------------ | ------------------------------------- |
| response.code         | int                | 响应http status code                  |
| response.code_details | string             | 响应码详情                            |
| response.flags        | int                | 位向量的响应标志                      |
| response.grpc_status  | int                | grpc响应码                            |
| response.headers      | map<string,string> | 所有响应头，逗号分割                  |
| response.trailers     | map<string,string> | 所有的响应trailers，逗号分割          |
| response.size         | int                | 响应body的大小                        |
| response.total_size   | int                | 响应大小，包括未压缩的响应头和tailers |



## 3.3连接属性

当下游连接建立时下面属性有效。

| 属性                                 | 类型   | 说明                                      |
| ------------------------------------ | ------ | ----------------------------------------- |
| source.address                       | string | 下游连接远程地址                          |
| source.port                          | int    | 下游连接远程端口                          |
| destination.adress                   | string | 下游连接本地地址                          |
| destination.port                     | int    | 下游连接本地端口                          |
| connection.id                        | uint   | 下游连接id                                |
| connection.mtls                      | bool   | 指示下游连接是否用了双向tls               |
| connection.requested_server_name     | string | 下游tls连接的请求服务名称                 |
| connection.tls_version               | string | 下游tls连接的tls版本                      |
| connection.subject_local_certificate | string | 下游tls连接的本地证书subject名称          |
| connection.subject_peer_certificate  | string | 下游tls连接的peer证书subject名称          |
| connection.dns_san_local_certificate | sting  | 下游tls连接的本地证书SAN中的第一个dns     |
| connection.dns_san_peer_certificate  | string | 下游tls连接的peer证书SAN字段中的第一个dns |
| connection.uri_san_local_certificate | string | 下游tls连接的本地证书SAN字段中的第一个uri |
| connection.uri_san_peer_certificate  | string | 下游tls连接的peer证书SAN字段中的第一个uri |

下游连接结束时的属性。

| 属性                           | 类型   | 说明             |
| ------------------------------ | ------ | ---------------- |
| connection.termination_details | string | 内部连接结束详情 |



## 3.4上游属性

当上游连接建立时下面属性有效。

| 属性                               | 类型   | 说明                                  |
| ---------------------------------- | ------ | ------------------------------------- |
| upstream.address                   | string | 上游连接远程地址                      |
| upstream.port                      | int    | 上游连接远程端口                      |
| upstream.tls_version               | string | 上游连接tls版本                       |
| upstream.subject_local_certificate | string | 上游tls连接本地证书subject            |
| upstream.subject_peer_certificate  | string | 上游tls连接peer证书subject            |
| upstream.dns_san_local_certificate | string | 上游tls连接本地证书SAN中的第一个dns值 |
| upstream.dns_san_peer_certificate  | string | 上游tls连接peer证书SAN中的第一个dns值 |
| upstream.uri_san_local_certificate | string | 上游tls连接本地证书SAN中的第一个uri值 |
| upstream.uri_san_peer_certificate  | string | 上游tls连接peer证书SAN中的第一个uri值 |
| upstream.local_address             | string | 上游连接本地地址                      |
| upstream.transport_failure_reason  | string | 上游连接失败传输错误原因              |



## 3.5wasm属性

下面属性在wasm extension中有效

| 属性                   | 类型     | 说明                    |
| ---------------------- | -------- | ----------------------- |
| plugin_name            | string   | plugin的名称            |
| plugin_root_id         | string   | plugin的根id            |
| plugin_vm_id           | string   | plugin的vm id           |
| node                   | node     | 本地node描述            |
| cluster_name           | string   | 上游集群名称            |
| cluster_metadata       | Metadata | 上游集群元数据          |
| listener_direction     | int      | 监听器方向，比如INBOUND |
| listener_metadata      | Metadata | 监听器元数据            |
| route_name             | string   | 路由名称                |
| route_metadata         | Metadata | 路由元数据              |
| upstream_host_metadata | Metadata | 上游主机元数据          |



# 4使用attribute案例

## 4.1rbac

remote.ip用的是source.address属性。

authorizationPolicy实现方式

```
apiVersion: security.istio.io/v1beta1
kind: AuthorizationPolicy
metadata:
 name: productpage
spec:
 selector:
   matchLabels:
     app: productpage
 action: DENY
 rules:
 - when:
   - key: remote.ip
     values:
     - "192.168.198.1/32"
```

envoyfilter实现

```
cat << EOF > ef-when-remote.ip-values.yaml
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: match
spec:
  workloadSelector:
    labels:
      app: productpage
  configPatches:
  - applyTo: HTTP_FILTER
    match:
      context: SIDECAR_INBOUND
      listener:
        portNumber: 9080
        filterChain:
          destinationPort: 9080
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
                ns[istio]-policy[productpage]-rule[0]:
                  permissions:
                  - andRules:
                      rules:
                      - any: true
                  principals:
                  - andIds:
                      ids:
                      - orIds:
                          ids:
                          - remoteIp:
                              addressPrefix: 192.168.198.1
                              prefixLen: 32
            shadowRulesStatPrefix: istio_dry_run_allow_
EOF

kubectl apply -f ef-when-remote.ip-values.yaml -n istio  --context context-cluster1
```



## 4.2wasm

wasm扩展通过get_property获取，具体看sdk怎么实现。

main.go

```
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
        path :=[]string{"plugin_vm_id"}
        data,err := proxywasm.GetProperty(path) 
        if err != nil {
                proxywasm.LogCritical("failed to get property")
        }

        proxywasm.LogCriticalf("property(%s): %s", "plugin_vm_id",string(data))
        
        path=[]string{"route_metadata"}
        data2,err := proxywasm.GetProperty(path)
        
        if err != nil {
                proxywasm.LogCritical("failed to get property")
        }
        proxywasm.LogCriticalf("property(%s): %s", "route_metadata",string(data2))
        return types.ActionContinue
}
```

main.go

```
package main

import (
        "github.com/tetratelabs/proxy-wasm-go-sdk/proxywasm"
        "github.com/tetratelabs/proxy-wasm-go-sdk/proxywasm/types"
        "github.com/golang/protobuf/proto"
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

type Metadata struct{
        filter_metadata []map[string]Struct
}

// Override types.DefaultHttpContext.
func (ctx *httpHeaders) OnHttpRequestHeaders(numHeaders int, endOfStream bool) types.Action {
        path :=[]string{"plugin_vm_id"}
        data,err := proxywasm.GetProperty(path) 
        if err != nil {
                proxywasm.LogCritical("failed to get property")
        }

        proxywasm.LogCriticalf("property(%s): %s", "plugin_vm_id",string(data))
        
        path=[]string{"route_metadata"}
        data,err = proxywasm.GetProperty(path)
        
        if err != nil {
                proxywasm.LogCritical("failed to get property")
        }
        var data2 Metadata=Metadata{};
        err = proto.Unmarshal(data, &data2)
        proxywasm.LogCriticalf("property(%s): %s", "route_metadata",string(data2.(type)))
        return types.ActionContinue
}
```

编译：

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

ef-wasm.yaml

kubectl apply -f ef-wasm.yaml -n istio

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
  priority: 20
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
      operation: INSERT_BEFORE
      value:
        name: envoy.filters.http.wasm
        typed_config:
                '@type': type.googleapis.com/envoy.extensions.filters.http.wasm.v3.Wasm
                config:
                  root_id: attribute_id
                  name: my_plugin
                  configuration:
                    "@type": type.googleapis.com/google.protobuf.StringValue
                    value: |
                      {}
                  vm_config:
                    vm_id: attribute_vm_id
                    runtime: "envoy.wasm.runtime.v8"
                    code:
                      local:
                        filename: /var/local/lib/wasm-filters/main.wasm

```

ef-productpage-metadata.yaml

kubectl apply -f ef-productpage-metadata.yaml -n istio

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: metadata
spec:
  workloadSelector:
    labels:
      app: productpage
  priority: 10
  configPatches:
  - applyTo: NETWORK_FILTER
    match:
      context: SIDECAR_INBOUND
      listener:
        filterChain:
          destinationPort: 9080
          filter:
            name: "envoy.filters.network.http_connection_manager"
    patch:
      operation: REPLACE
      value:
              name: envoy.filters.network.http_connection_manager
              typedConfig:
                '@type': type.googleapis.com/envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager
                stat_prefix: "inbound_0.0.0.0_9080"
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
                      route:
                        cluster: inbound|9080||
                        timeout: "0s"
                        max_stream_duration:
                          max_stream_duration: "0s"
                http_filters:
                - name: "envoy.filters.http.router"
                  typed_config:
                    "@type": "type.googleapis.com/envoy.extensions.filters.http.router.v3.Router"
```



## 4.3Rate limit descriptor expression

部署ratelimit

1创建cm

```
cat << EOF > ratelimit-config-outside-http.yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: ratelimit-config
data:
  config.yaml: |
    domain: productpage-ratelimit
    descriptors:
      - key: PATH
        value: "/"
        rate_limit:
          unit: minute
          requests_per_unit: 1
      - key: PATH
        value: "/aa"
        rate_limit:
          unit: minute
          requests_per_unit: 1
      - key: PATH
        rate_limit:
          unit: minute
          requests_per_unit: 100
EOF

kubectl apply -f ratelimit-config-outside-http.yaml -n istio
```

说明: 这个configmap是限速服务用到的配置文件，他是envoy v3版本的限速格式。domain是域名，他会在envoyfilter中被引用，descriptors的PATH,表示请求的路径可以有多个值，rate_limit配置限速配额，这里productpage配了1分钟1个请求，/aa一分钟1个请求，其他url是1分钟100个请求



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
        imagePullPolicy: Always
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
        imagePullPolicy: Always
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

创建了redis，和官方的一个ratelimit服务。



```
cat << EOF > se-baidu.yaml
apiVersion: networking.istio.io/v1alpha3
kind: ServiceEntry
metadata:
  name: baidu
spec:
  hosts:
    - www.baidu.com
  ports:
    - number: 80
      name: http-port
      protocol: HTTP
  resolution: DNS
EOF

kubectl apply -f se-baidu.yaml -n istio
```

创建访问百度的serviceentry



创建envoy-filter

```
cat << EOF > envoyfilter-filter-outside-http.yaml
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: filter-ratelimit
  namespace: istio
spec:
  workloadSelector:
    # select by label in the same namespace
    labels:
      app: ratings
  configPatches:
    - applyTo: HTTP_FILTER
      match:
        context: SIDECAR_OUTBOUND
        listener:
          filterChain:
            filter:
              name: "envoy.filters.network.http_connection_manager"
              subFilter:
                name: "envoy.filters.http.router"
      patch:
        operation: INSERT_BEFORE
        # Adds the Envoy Rate Limit Filter in HTTP filter chain.
        value:
          name: envoy.filters.http.ratelimit
          typed_config:
            "@type": type.googleapis.com/envoy.extensions.filters.http.ratelimit.v3.RateLimit
            # domain can be anything! Match it to the ratelimter service config
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
        # Adds the rate limit service cluster for rate limit service defined in step 1.
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
EOF

kubectl apply -f envoyfilter-filter-outside-http.yaml -n istio
```

这个envoyfilter作用在ratings上面，SIDECAR_OUTBOUND作用的对外流量上面，配置了一个http过滤器envoy.filters.http.ratelimit，和一个cluster。http 过滤器的cluster地址指向cluster配置的地址，这里就是我们的ratelimit service所在的地址。domain是上面configmap的值一样，failure_mode_deny表示超过请求限值就拒绝，rate_limit_service配置ratelimit服务的地址（cluster），这里可以配置grpc类型的也可以配置http类型的。



4创建action envoyfilter

```
cat << EOF > envoyfilter-action-outside-http.yaml 
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: filter-ratelimit-svc
  namespace: istio
spec:
  workloadSelector:
    labels:
      app: ratings
  configPatches:
    - applyTo: VIRTUAL_HOST
      match:
        context: SIDECAR_OUTBOUND
        routeConfiguration:
          vhost:
            name: "www.baidu.com:80"
            route:
              action: ANY
      patch:
        operation: MERGE
        # Applies the rate limit rules.
        value:
          rate_limits:
            - actions: # any actions in here
              - request_headers:
                  header_name: ":path"
                  descriptor_key: "PATH"
EOF

kubectl apply -f envoyfilter-action-outside-http.yaml   -n istio
```

host是我们配置的百度的地址（www.baidu.com:80），这个envoyfilter作用在ratings处，给80端口的虚拟主机配置了一个rate_limits 动作，descriptor_key用于选择在configmap里配置的key。

```
kubectl exec -it -n istio ratings-v2-mysql-vm-66dc56449d-lk6gv /bin/bash

node@ratings-v2-mysql-vm-66dc56449d-lk6gv:/opt/microservices$ curl www.baidu.com/ -I
HTTP/1.1 429 Too Many Requests
x-envoy-ratelimited: true
date: Fri, 17 Sep 2021 23:51:33 GMT
server: envoy
transfer-encoding: chunked
```

进入rating容器，向百度发请求，409错误，说明限流成功

清理：

```
kubectl delete cm ratelimit-config -n istio
kubectl delete -f ratelimit-deploy.yaml -n istio
kubectl delete envoyfilter filter-ratelimit -n istio
kubectl delete envoyfilter filter-ratelimit-svc -n istio
```



## 4.4External Authorization

外部授权请求和响应中会包含属性信息。

先验证jwt

```
cat << EOF > ra-ingressgateway-jwtrules-ap.yaml
apiVersion: "security.istio.io/v1beta1"
kind: "RequestAuthentication"
metadata:
  name: ingressgateway-ra
spec:
  selector:
    matchLabels:
      app: istio-ingressgateway
  jwtRules:
  - issuer: "testing@secure.istio.io"
    outputPayloadToHeader: auth
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
EOF

kubectl apply -f ra-ingressgateway-jwtrules-ap.yaml -n istio-system
```

1创建opa策略

opa介绍

http://blog.newbmiao.com/2020/03/13/opa-quick-start.html

https://www.openpolicyagent.org/docs/latest/

验证opa

https://play.openpolicyagent.org/p/ZXkIlAEPCY

```
cat << EOF > policy.rego 
package envoy.authz

import input.attributes.request.http as http_request

default allow = false

token = {"payload": payload} {
    [_, encoded] := split(http_request.headers.authorization, " ")
    [_, payload, _] := io.jwt.decode(encoded)
}

allow {
    action_allowed
}


bar := "bar"

action_allowed {
  bar ==token.payload.foo
}

EOF
```

2创建secret

  kubectl create secret generic opa-policy --from-file policy.rego  -n istio

3创建opa

```
cat << EOF > opa-deployment.yaml
apiVersion: v1
kind: Service
metadata:
  name: opa
  labels:
    app: opa
spec:
  ports:
  - name: grpc
    port: 9191
    targetPort: 9191
  selector:
    app: opa
---
kind: Deployment
apiVersion: apps/v1
metadata:
  name: opa
  labels:
    app: opa
spec:
  replicas: 1
  selector:
    matchLabels:
      app: opa
  template:
    metadata:
      labels:
        app: opa
    spec:
      containers:
        - name: opa
          image: openpolicyagent/opa:latest-envoy
          securityContext:
            runAsUser: 1111
          volumeMounts:
          - readOnly: true
            mountPath: /policy
            name: opa-policy
          args:
          - "run"
          - "--server"
          - "--addr=localhost:8181"
          - "--diagnostic-addr=0.0.0.0:8282"
          - "--set=plugins.envoy_ext_authz_grpc.addr=:9191"
          - "--set=plugins.envoy_ext_authz_grpc.query=data.envoy.authz.allow"
          - "--set=decision_logs.console=true"
          - "--ignore=.*"
          - "/policy/policy.rego"
          ports:
          - containerPort: 9191
          livenessProbe:
            httpGet:
              path: /health?plugins
              scheme: HTTP
              port: 8282
            initialDelaySeconds: 5
            periodSeconds: 5
          readinessProbe:
            httpGet:
              path: /health?plugins
              scheme: HTTP
              port: 8282
            initialDelaySeconds: 5
            periodSeconds: 5
      volumes:
        - name: opa-policy
          secret:
            secretName: opa-policy
EOF
 
kubectl apply -f opa-deployment.yaml -n istio
```

4编辑meshconfig

 kubectl edit configmap istio -n istio-system 

```
  mesh: |-
    # Add the following contents:
    extensionProviders:
    - name: "opa.istio"
      envoyExtAuthzGrpc:
        service: "opa.istio.svc.cluster.local"
        port: "9191"
```

5创建ap

```
cat << EOF >ext-authz.yaml
apiVersion: security.istio.io/v1beta1
kind: AuthorizationPolicy
metadata:
 name: ext-authz
 namespace: istio-system
spec:
 selector:
   matchLabels:
     app: istio-ingressgateway
 action: CUSTOM
 provider:
   name: "opa.istio"
 rules:
 - to:
   - operation:
       paths: ["/productpage"]
EOF

kubectl apply -f ext-authz.yaml -n istio-system
```

测试：

```
TOKEN=eyJhbGciOiJSUzI1NiIsImtpZCI6IkRIRmJwb0lVcXJZOHQyenBBMnFYZkNtcjVWTzVaRXI0UnpIVV8tZW52dlEiLCJ0eXAiOiJKV1QifQ.eyJleHAiOjQ2ODU5ODk3MDAsImZvbyI6ImJhciIsImlhdCI6MTUzMjM4OTcwMCwiaXNzIjoidGVzdGluZ0BzZWN1cmUuaXN0aW8uaW8iLCJzdWIiOiJ0ZXN0aW5nQHNlY3VyZS5pc3Rpby5pbyJ9.CfNnxWP2tcnR9q0vxyxweaF3ovQYHYZl82hAUsn21bwQd9zP7c-LS9qd_vpdLG4Tn1A15NxfCjp5f7QNBUo-KC9PJqYpgGbaXhaGx7bEdFWjcwv3nZzvc7M__ZpaCERdwU7igUmJqYGBYQ51vr2njU9ZimyKkfDe3axcyiBZde7G6dabliUosJvvKOPcKIWPccCgefSj_GNfwIip3-SsFdlR7BtbVUcqR-yv-XOxJ3Uc1MI0tz3uMiiZcyPV7sNCU4KRnemRIMHVOfuvHsU60_GhGbiSFzgPTAa9WTltbnarTbxudb_YEOx12JiwYToeX0DCPb43W1tzIBxgm8NxUg

curl 192.168.229.128:30563/productpage -H "Authorization: Bearer ${TOKEN}"
```

```
kubectl delete -f ext-authz.yaml -n istio-system
kubectl delete -f opa-deployment.yaml -n istio
kubectl delete secret opa-policy   -n istio
kubectl delete requestauthentications ingressgateway-ra -n istio-system
```



## 4.5ExpressionFilter

ef-expressionFilter.yaml

kubectl apply -f ef-expressionFilter.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: accesslog
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
                access_log:
                  - name: envoy.access_loggers.file
                    filter:
                      extension_filter:
                        name: envoy.access_loggers.extension_filters.cel
                        typed_config:
                          '@type': type.googleapis.com/envoy.extensions.access_loggers.filters.cel.v3.ExpressionFilter
                          expression: "request.url_path.contains('productpage')"
                    typedConfig:
                      '@type': type.googleapis.com/envoy.extensions.access_loggers.file.v3.FileAccessLog
                      logFormat:
                        textFormat: |
                          [%START_TIME%] "%REQ(:METHOD)%  %ROUTE_NAME%
                      path: /dev/stdout
```

