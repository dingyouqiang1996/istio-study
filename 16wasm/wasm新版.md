# wasm是什么

 WASM 的诞生源自前端，是一种为了解决日益复杂的前端 web 应用以及有限的 JavaScript 性能而诞生的技术。它本身并不是一种语言，而是一种字节码标准，一个“编译目标”。WASM 字节码和机器码非常接近，因此可以非常快速的装载运行。任何一种语言，都可以被编译成 WASM 字节码，然后在 WASM 虚拟机中执行（本身是为 web 设计，必然天然跨平台，同时为了沙箱运行保障安全，所以直接编译成机器码并不是最佳选择）。理论上，所有语言，包括 JavaScript、C、C++、Rust、Go、Java 等都可以编译成 WASM 字节码并在 WASM 虚拟机中执行。 

![11](D:/istio入门到精通/16wasm/images/11.jpg)

# 案例

案例来自：https://github.com/tetratelabs/proxy-wasm-go-sdk

调整日志级别：

```
istioctl -n istio-system proxy-config log istio-ingressgateway-8d7d49b55-dlv9c --level info
```

或：

```
template:
    metadata:
      annotations:
        "sidecar.istio.io/logLevel": info
```



## dispatch_call_on_tick

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
        "crypto/rand"

        "github.com/tetratelabs/proxy-wasm-go-sdk/proxywasm"
        "github.com/tetratelabs/proxy-wasm-go-sdk/proxywasm/types"
)

const tickMilliseconds uint32 = 100

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
        return &pluginContext{contextID: contextID}
}

type pluginContext struct {
        // Embed the default plugin context here,
        // so that we don't need to reimplement all the methods.
        types.DefaultPluginContext
        contextID uint32
        callBack  func(numHeaders, bodySize, numTrailers int)
        cnt       int
}

// Override types.DefaultPluginContext.
func (ctx *pluginContext) OnPluginStart(pluginConfigurationSize int) types.OnPluginStartStatus {
        if err := proxywasm.SetTickPeriodMilliSeconds(tickMilliseconds); err != nil {
                proxywasm.LogCriticalf("failed to set tick period: %v", err)
                return types.OnPluginStartStatusFailed
        }
        proxywasm.LogInfof("set tick period milliseconds: %d", tickMilliseconds)
        ctx.callBack = func(numHeaders, bodySize, numTrailers int) {
                ctx.cnt++
                proxywasm.LogInfof("called %d for contextID=%d", ctx.cnt, ctx.contextID)
                headers, err := proxywasm.GetHttpCallResponseHeaders()
                if err != nil && err != types.ErrorStatusNotFound {
                        panic(err)
                }
                for _, h := range headers {
                        proxywasm.LogInfof("response header for the dispatched call: %s: %s", h[0], h[1])
                }
                headers, err = proxywasm.GetHttpCallResponseTrailers()
                if err != nil && err != types.ErrorStatusNotFound {
                        panic(err)
                }
                for _, h := range headers {
                        proxywasm.LogInfof("response trailer for the dispatched call: %s: %s", h[0], h[1])
                }
        }
        return types.OnPluginStartStatusOK
}

func (*pluginContext) NewHttpContext(contextID uint32) types.HttpContext {
        return &httpContext{contextID: contextID}
}

type httpContext struct {
        // Embed the default http context here,
        // so that we don't need to reimplement all the methods.
        types.DefaultHttpContext
        contextID uint32
}

// Override types.DefaultPluginContext.
func (ctx *pluginContext) OnTick() {
        headers := [][2]string{
                {":method", "GET"}, {":authority", "some_authority"}, {"accept", "*/*"},
        }
        // Pick random value to select the request path.
        buf := make([]byte, 1)
        _, _ = rand.Read(buf)
        if buf[0]%2 == 0 {
                headers = append(headers, [2]string{":path", "/ok"})
        } else {
                headers = append(headers, [2]string{":path", "/fail"})
        }
        if _, err := proxywasm.DispatchHttpCall("web_service", headers, nil, nil, 5000, ctx.callBack); err != nil {
                proxywasm.LogCriticalf("dispatch httpcall failed: %v", err)
        }
}
```

编译

```
export GOPROXY=https://proxy.golang.com.cn,direct

tinygo build -o main.wasm -scheduler=none -target=wasi main.go

docker build --tag registry.cn-qingdao.aliyuncs.com/hxpdocker/dispatch_call_on_tick:v1 . --no-cache
docker push registry.cn-qingdao.aliyuncs.com/hxpdocker/dispatch_call_on_tick:v1
```

wp-ingressgateway-dispatch_call_on_tick.yaml

kubectl apply -f wp-ingressgateway-dispatch_call_on_tick.yaml -n istio-system

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
  url: oci://registry.cn-qingdao.aliyuncs.com/hxpdocker/dispatch_call_on_tick:v1
  phase: STATS	
  imagePullPolicy: Always
```

ef-dispatch.yaml

kubectl apply -f ef-dispatch.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: dispatch
spec:
  workloadSelector:
    labels:
      istio: ingressgateway
  configPatches:
  - applyTo: LISTENER
    match:
      context: SIDECAR_INBOUND
    patch:
      operation: ADD
      value:
          name: staticreply
          address:
            socket_address:
              address: 127.0.0.1
              port_value: 8099
          filter_chains:
          - filters:
            - name: envoy.http_connection_manager
              typed_config:
                "@type": type.googleapis.com/envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager
                stat_prefix: ingress_http
                codec_type: AUTO
                route_config:
                  name: local_route
                  virtual_hosts:
                    - name: local_service
                      domains:
                        - "*"
                      routes:
                        - match:
                            prefix: "/ok"
                          direct_response:
                            status: 200
                            body:
                              inline_string: "example body\n"
                        - match:
                            prefix: "/fail"
                          direct_response:
                            status: 503
                http_filters:
                  - name: envoy.filters.http.router
                    typed_config:
                      "@type": type.googleapis.com/envoy.extensions.filters.http.router.v3.Router
  - applyTo: CLUSTER
    patch:
      operation: ADD
      value: 
          name: web_service
          connect_timeout: 0.25s
          type: STATIC
          lb_policy: ROUND_ROBIN
          load_assignment:
            cluster_name: mock_service
            endpoints:
            - lb_endpoints:
              - endpoint:
                  address:
                    socket_address:
                      address: 127.0.0.1
                      port_value: 8099
```



envoyfilter实现：

```
kubectl create cm wasm --from-file=main.wasm -n istio
```

deploy-productpage-with-sidecar-volume.yaml

kubectl apply -f deploy-productpage-with-sidecar-volume.yaml -n istio

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
```



ef-dispatch_call_on_tick.yaml

kubectl apply -f dispatch_call_on_tick.yaml -n istio

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: dispatch
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
              subFilter:
                name: "envoy.filters.http.router"
      patch:
        operation: INSERT_BEFORE
        value:
                    name: envoy.filters.http.wasm
                    typed_config:
                      "@type": type.googleapis.com/udpa.type.v1.TypedStruct
                      type_url: type.googleapis.com/envoy.extensions.filters.http.wasm.v3.Wasm
                      value:
                        config:
                          configuration:
                            "@type": type.googleapis.com/google.protobuf.StringValue
                            value: "plugin1"
                          vm_config:
                            runtime: "envoy.wasm.runtime.v8"
                            code:
                              local:
                                filename: /var/local/lib/wasm-filters/main.wasm
```

ef-dispatch-productpage.yaml

kubectl apply -f ef-dispatch-productpage.yaml -n istio

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: dispatch2
spec:
  workloadSelector:
    labels:
      app: productpage
  configPatches:
  - applyTo: LISTENER
    match:
      context: SIDECAR_INBOUND
    patch:
      operation: ADD
      value:
          name: staticreply
          address:
            socket_address:
              address: 127.0.0.1
              port_value: 8099
          filter_chains:
          - filters:
            - name: envoy.http_connection_manager
              typed_config:
                "@type": type.googleapis.com/envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager
                stat_prefix: ingress_http
                codec_type: AUTO
                route_config:
                  name: local_route
                  virtual_hosts:
                    - name: local_service
                      domains:
                        - "*"
                      routes:
                        - match:
                            prefix: "/ok"
                          direct_response:
                            status: 200
                            body:
                              inline_string: "example body\n"
                        - match:
                            prefix: "/fail"
                          direct_response:
                            status: 503
                http_filters:
                  - name: envoy.filters.http.router
                    typed_config:
                      "@type": type.googleapis.com/envoy.extensions.filters.http.router.v3.Router
  - applyTo: CLUSTER
    patch:
      operation: ADD
      value: 
          name: web_service
          connect_timeout: 0.25s
          type: STATIC
          lb_policy: ROUND_ROBIN
          load_assignment:
            cluster_name: mock_service
            endpoints:
            - lb_endpoints:
              - endpoint:
                  address:
                    socket_address:
                      address: 127.0.0.1
                      port_value: 8099
```



## foreign_call_on_tick

可调用的foreign func

https://github.com/envoyproxy/envoy/blob/main/source/extensions/common/wasm/foreign.cc

```
compress
uncompress
expr_create
expr_evaluate
expr_delete
declare_property
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
        "encoding/hex"

        "github.com/tetratelabs/proxy-wasm-go-sdk/proxywasm"
        "github.com/tetratelabs/proxy-wasm-go-sdk/proxywasm/types"
)

const tickMilliseconds uint32 = 1

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
        contextID uint32
        callNum   uint32
}

// Override types.DefaultPluginContext.
func (ctx *pluginContext) OnPluginStart(pluginConfigurationSize int) types.OnPluginStartStatus {
        if err := proxywasm.SetTickPeriodMilliSeconds(tickMilliseconds); err != nil {
                proxywasm.LogCriticalf("failed to set tick period: %v", err)
                return types.OnPluginStartStatusFailed
        }
        proxywasm.LogInfof("set tick period milliseconds: %d", tickMilliseconds)
        return types.OnPluginStartStatusOK
}

// Override types.DefaultPluginContext.
func (ctx *pluginContext) OnTick() {
        ctx.callNum++
        ret, err := proxywasm.CallForeignFunction("compress", []byte("hello world!"))
        if err != nil {
                proxywasm.LogCriticalf("foreign function (compress) failed: %v", err)
        }
        proxywasm.LogInfof("foreign function (compress) called: %d, result: %s", ctx.callNum, hex.EncodeToString(ret))
}

func (*pluginContext) NewHttpContext(contextID uint32) types.HttpContext {
        return &httpContext{contextID: contextID}
}

type httpContext struct {
        // Embed the default http context here,
        // so that we don't need to reimplement all the methods.
        types.DefaultHttpContext
        contextID uint32
}
```

编译：

```
export GOPROXY=https://proxy.golang.com.cn,direct

tinygo build -o main.wasm -scheduler=none -target=wasi main.go

docker build --tag registry.cn-qingdao.aliyuncs.com/hxpdocker/foreign_call_on_tick:v1 . --no-cache
docker push registry.cn-qingdao.aliyuncs.com/hxpdocker/foreign_call_on_tick:v1
```

wp-ingressgateway-foreign_call_on_tick.yaml

kubectl apply -f wp-ingressgateway-foreign_call_on_tick.yaml -n istio-system

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
  url: oci://registry.cn-qingdao.aliyuncs.com/hxpdocker/foreign_call_on_tick:v1
  phase: STATS	
  imagePullPolicy: Always
```

## http_auth_random

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
        "hash/fnv"

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
        return &httpAuthRandom{contextID: contextID}
}

type httpAuthRandom struct {
        // Embed the default http context here,
        // so that we don't need to reimplement all the methods.
        types.DefaultHttpContext
        contextID uint32
}

// Override types.DefaultHttpContext.
func (ctx *httpAuthRandom) OnHttpRequestHeaders(numHeaders int, endOfStream bool) types.Action {
        //hs, err := proxywasm.GetHttpRequestHeaders()
        //if err != nil {
        //      proxywasm.LogCriticalf("failed to get request headers: %v", err)
        //      return types.ActionContinue
        //}
        hs := [][2]string{
                {":method", "GET"}, {":authority", "192.168.229.128:32376"},{":path", "/uuid"}, {"accept", "*/*"},
        }
        for _, h := range hs {
                proxywasm.LogInfof("request header: %s: %s", h[0], h[1])
        }

        if _, err := proxywasm.DispatchHttpCall(clusterName, hs, nil, nil,
                50000, httpCallResponseCallback); err != nil {
                proxywasm.LogCriticalf("dipatch httpcall failed: %v", err)
                return types.ActionContinue
        }

        proxywasm.LogInfof("http call dispatched to %s", clusterName)
        return types.ActionPause
}

func httpCallResponseCallback(numHeaders, bodySize, numTrailers int) {
        hs, err := proxywasm.GetHttpCallResponseHeaders()
        if err != nil {
                proxywasm.LogCriticalf("failed to get response body: %v", err)
                return
        }

        for _, h := range hs {
                proxywasm.LogInfof("response header from %s: %s: %s", clusterName, h[0], h[1])
        }

        b, err := proxywasm.GetHttpCallResponseBody(0, bodySize)
        if err != nil {
                proxywasm.LogCriticalf("failed to get response body: %v", err)
                proxywasm.ResumeHttpRequest()
                return
        }

        s := fnv.New32a()
        if _, err := s.Write(b); err != nil {
                proxywasm.LogCriticalf("failed to calculate hash: %v", err)
                proxywasm.ResumeHttpRequest()
                return
        }

        if s.Sum32()%2 == 0 {
                proxywasm.LogInfo("access granted")
                proxywasm.ResumeHttpRequest()
                return
        }

        body := "access forbidden"
        proxywasm.LogInfo(body)
        if err := proxywasm.SendHttpResponse(403, [][2]string{
                {"powered-by", "proxy-wasm-go-sdk!!"},
        }, []byte(body), -1); err != nil {
                proxywasm.LogErrorf("failed to send local response: %v", err)
                proxywasm.ResumeHttpRequest()
        }
}
```

编译：

```
export GOPROXY=https://proxy.golang.com.cn,direct

tinygo build -o main.wasm -scheduler=none -target=wasi main.go

docker build --tag registry.cn-qingdao.aliyuncs.com/hxpdocker/http_auth_random:v1 . --no-cache
docker push registry.cn-qingdao.aliyuncs.com/hxpdocker/http_auth_random:v1
```

ef-auth.yaml

kubectl apply -f ef-auth.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: auth
spec:
  workloadSelector:
    labels:
      istio: ingressgateway
  configPatches:
  - applyTo: CLUSTER
    patch:
      operation: ADD
      value: 
          name: httpbin
          connect_timeout: 5000s
          type: STRICT_DNS
          lb_policy: ROUND_ROBIN
          load_assignment:
            cluster_name: httpbin
            endpoints:
            - lb_endpoints:
              - endpoint:
                  address:
                    socket_address:
                      address: httpbin.org
                      port_value: 80
```

wp-ingressgateway-http_auth_random.yaml

kubectl apply -f wp-ingressgateway-http_auth_random.yaml -n istio-system

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
  url: oci://registry.cn-qingdao.aliyuncs.com/hxpdocker/http_auth_random:v1
  phase: STATS	
  imagePullPolicy: Always
```

请求productpage

查看ingressgateway日志

## http_body

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

const (
        bufferOperationAppend  = "append"
        bufferOperationPrepend = "prepend"
        bufferOperationReplace = "replace"
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
        shouldEchoBody bool
}

// Override types.DefaultPluginContext.
func (ctx *pluginContext) NewHttpContext(contextID uint32) types.HttpContext {
        if ctx.shouldEchoBody {
                return &echoBodyContext{}
        }
        return &setBodyContext{}
}

// Override types.DefaultPluginContext.
func (ctx *pluginContext) OnPluginStart(pluginConfigurationSize int) types.OnPluginStartStatus {
        data, err := proxywasm.GetPluginConfiguration()
        if err != nil {
                proxywasm.LogCriticalf("error reading plugin configuration: %v", err)
        }
        proxywasm.LogCriticalf("configuration data: %s", string(data))
        ctx.shouldEchoBody = string(data) == "{\"echo\":true}"
        return types.OnPluginStartStatusOK
}

type setBodyContext struct {
        // Embed the default root http context here,
        // so that we don't need to reimplement all the methods.
        types.DefaultHttpContext
        totalRequestBodySize int
        bufferOperation      string
}

// Override types.DefaultHttpContext.
func (ctx *setBodyContext) OnHttpRequestHeaders(numHeaders int, endOfStream bool) types.Action {
        //if _, err := proxywasm.GetHttpRequestHeader("content-length"); err != nil {
        //          proxywasm.LogErrorf("context-length: %v", err)
        //      if err := proxywasm.SendHttpResponse(400, nil, []byte("content must be provided"), -1); err != nil {
        //              panic(err)
        //      }
        //      return types.ActionPause
        //}

        // Remove Content-Length in order to prevent severs from crashing if we set different body from downstream.
        if err := proxywasm.RemoveHttpRequestHeader("content-length"); err != nil {
                panic(err)
        }

        // Get "Buffer-Operation" header value.
        op, err := proxywasm.GetHttpRequestHeader("buffer-operation")
        if err != nil || (op != bufferOperationAppend &&
                op != bufferOperationPrepend &&
                op != bufferOperationReplace) {
                // Fallback to replace
                op = bufferOperationReplace
        }
        ctx.bufferOperation = op
        return types.ActionContinue
}

// Override types.DefaultHttpContext.
func (ctx *setBodyContext) OnHttpRequestBody(bodySize int, endOfStream bool) types.Action {
        ctx.totalRequestBodySize += bodySize
        if !endOfStream {
                // Wait until we see the entire body to replace.
                return types.ActionPause
        }

        originalBody, err := proxywasm.GetHttpRequestBody(0, ctx.totalRequestBodySize)
        if err != nil {
                proxywasm.LogErrorf("failed to get request body: %v", err)
                return types.ActionContinue
        }
        proxywasm.LogInfof("original request body: %s", string(originalBody))

        switch ctx.bufferOperation {
        case bufferOperationAppend:
                err = proxywasm.AppendHttpRequestBody([]byte(`[this is appended body]`))
        case bufferOperationPrepend:
                err = proxywasm.PrependHttpRequestBody([]byte(`[this is prepended body]`))
        case bufferOperationReplace:
                err = proxywasm.ReplaceHttpRequestBody([]byte(`[this is replaced body]`))
        }
        if err != nil {
                proxywasm.LogErrorf("failed to %s request body: %v", ctx.bufferOperation, err)
                return types.ActionContinue
        }
        return types.ActionContinue
}

type echoBodyContext struct {
        // mbed the default plugin context
        // so that you don't need to reimplement all the methods by yourself.
        types.DefaultHttpContext
        totalRequestBodySize int
}

// Override types.DefaultHttpContext.
func (ctx *echoBodyContext) OnHttpRequestBody(bodySize int, endOfStream bool) types.Action {
        ctx.totalRequestBodySize += bodySize
        //proxywasm.LogCriticalf("echo request body: %s", string(111111))
        if !endOfStream {
                // Wait until we see the entire body to replace.
                return types.ActionPause
        }

        // Send the request body as the response body.
        body, _ := proxywasm.GetHttpRequestBody(0, ctx.totalRequestBodySize)
        proxywasm.LogCriticalf("echo request body: %s", string(body))
        if err := proxywasm.SendHttpResponse(200, nil, body, -1); err != nil {
                panic(err)
        }
        return types.ActionPause
}

func (ctx *echoBodyContext) OnHttpRequestHeaders(numHeaders int, endOfStream bool) types.Action {
  proxywasm.LogInfof("echo request body2: %d 222", numHeaders)
  return types.ActionContinue
}
```

编译：

```
export GOPROXY=https://proxy.golang.com.cn,direct

tinygo build -o main.wasm -scheduler=none -target=wasi main.go

docker build --tag registry.cn-qingdao.aliyuncs.com/hxpdocker/http_body:v1 . --no-cache
docker push registry.cn-qingdao.aliyuncs.com/hxpdocker/http_body:v1
```

wp-ingressgateway-http_body.yaml

kubectl apply -f wp-ingressgateway-http_body.yaml -n istio-system

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
  url: oci://registry.cn-qingdao.aliyuncs.com/hxpdocker/http_body:v1
  phase: STATS	
  pluginConfig:
    echo: true
  priority: 10
  imagePullPolicy: Always
```

wp-ingressgateway-http_body-bodyset.yaml

kubectl apply -f wp-ingressgateway-http_body-bodyset.yaml -n istio-system

```
apiVersion: extensions.istio.io/v1alpha1
kind: WasmPlugin
metadata:
  name: ingressgateway-bodyset
  namespace: istio-system
spec:
  selector:
    matchLabels:
      istio: ingressgateway
  url: oci://registry.cn-qingdao.aliyuncs.com/hxpdocker/http_body:v1
  phase: STATS	
  priority: 20
  imagePullPolicy: Always
```



curl   -d "test" -H "buffer-operation: append" -XPOST http://192.168.229.128:32376/productpage

curl   -d "test" -H "buffer-operation: prepend" -XPOST http://192.168.229.128:32376/productpage

 curl   -d "test" -H "buffer-operation: replace" -XPOST http://192.168.229.128:32376/productpage



## http_headers

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



## http_routing

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
        "crypto/rand"
        "encoding/binary"

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
        return &httpRouting{}
}

type httpRouting struct {
        // Embed the default http context here,
        // so that we don't need to reimplement all the methods.
        types.DefaultHttpContext
}

// Unittest purpose.
var dice = func() uint32 {
        buf := make([]byte, 4)
        _, _ = rand.Read(buf)
        return binary.LittleEndian.Uint32(buf)
}

// Override types.DefaultHttpContext.
func (ctx *httpRouting) OnHttpRequestHeaders(numHeaders int, endOfStream bool) types.Action {
        // Randomly routing to the canary cluster.
        value := dice()
        proxywasm.LogInfof("value: %d\n", value)
        if value%2 == 0 {
                const authorityKey = ":authority"
                value, err := proxywasm.GetHttpRequestHeader(authorityKey)
                if err != nil {
                        proxywasm.LogCritical("failed to get request header: ':authority'")
                        return types.ActionPause
                }
                // Append "-canary" suffix to route this request to the canary cluster.
                value += "-canary"
                if err := proxywasm.ReplaceHttpRequestHeader(":authority", value); err != nil {
                        proxywasm.LogCritical("failed to set request header: test")
                        return types.ActionPause
                }
        }
        return types.ActionContinue
}
```

编译：

```
export GOPROXY=https://proxy.golang.com.cn,direct

tinygo build -o main.wasm -scheduler=none -target=wasi main.go

docker build --tag registry.cn-qingdao.aliyuncs.com/hxpdocker/http_routing:v1 . --no-cache
docker push registry.cn-qingdao.aliyuncs.com/hxpdocker/http_routing:v1
```

wp-ingressgateway-http_routing.yaml

kubectl apply -f wp-ingressgateway-http_routing.yaml -n istio-system

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
  url: oci://registry.cn-qingdao.aliyuncs.com/hxpdocker/http_routing:v1
  phase: STATS
  imagePullPolicy: Always
```

ef-http_route.yaml

kubectl apply -f ef-http_route.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: http-route
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
                          inline_string: "response primary"
                  - name: test2
                    domains:
                    - "*-canary"
                    routes:
                    - name: testroute2
                      match: 
                        prefix: /
                      direct_response:
                        status: 200
                        body: 
                          inline_string: "response canary"
```



## json_validation

main.go

```
package main

import (
        "fmt"

        "github.com/tidwall/gjson"

        "github.com/tetratelabs/proxy-wasm-go-sdk/proxywasm"
        "github.com/tetratelabs/proxy-wasm-go-sdk/proxywasm/types"
)

func main() {
        // SetVMContext is the entrypoint for setting up this entire Wasm VM.
        // Please make sure that this entrypoint be called during "main()" function, otherwise
        // this VM would fail.
        proxywasm.SetVMContext(&vmContext{})
}

// vmContext implements types.VMContext interface of proxy-wasm-go SDK.
type vmContext struct {
        // Embed the default VM context here,
        // so that we don't need to reimplement all the methods.
        types.DefaultVMContext
}

// Override types.DefaultVMContext.
func (*vmContext) NewPluginContext(contextID uint32) types.PluginContext {
        return &pluginContext{}
}

// pluginContext implements types.PluginContext interface of proxy-wasm-go SDK.
type pluginContext struct {
        // Embed the default plugin context here,
        // so that we don't need to reimplement all the methods.
        types.DefaultPluginContext
        configuration pluginConfiguration
}

// pluginConfiguration is a type to represent an example configuration for this wasm plugin.
type pluginConfiguration struct {
        // Example configuration field.
        // The plugin will validate if those fields exist in the json payload.
        requiredKeys []string
}

// Override types.DefaultPluginContext.
func (ctx *pluginContext) OnPluginStart(pluginConfigurationSize int) types.OnPluginStartStatus {
        data, err := proxywasm.GetPluginConfiguration()
        if err != nil && err != types.ErrorStatusNotFound {
                proxywasm.LogCriticalf("error reading plugin configuration: %v", err)
                return types.OnPluginStartStatusFailed
        }
        config, err := parsePluginConfiguration(data)
        if err != nil {
                proxywasm.LogCriticalf("error parsing plugin configuration: %v", err)
                return types.OnPluginStartStatusFailed
        }
        ctx.configuration = config
        return types.OnPluginStartStatusOK
}

// parsePluginConfiguration parses the json plugin confiuration data and returns pluginConfiguration.
// Note that this parses the json data by gjson, since TinyGo doesn't support encoding/json.
// You can also try https://github.com/mailru/easyjson, which supports decoding to a struct.
func parsePluginConfiguration(data []byte) (pluginConfiguration, error) {
        if len(data) == 0 {
                return pluginConfiguration{}, nil
        }

        config := &pluginConfiguration{}
        if !gjson.ValidBytes(data) {
                return pluginConfiguration{}, fmt.Errorf("the plugin configuration is not a valid json: %q", string(data))
        }

        jsonData := gjson.ParseBytes(data)
        requiredKeys := jsonData.Get("requiredKeys").Array()
        for _, requiredKey := range requiredKeys {
                config.requiredKeys = append(config.requiredKeys, requiredKey.Str)
        }

        return *config, nil
}

// Override types.DefaultPluginContext.
func (ctx *pluginContext) NewHttpContext(contextID uint32) types.HttpContext {
        return &payloadValidationContext{requiredKeys: ctx.configuration.requiredKeys}
}

// payloadValidationContext implements types.HttpContext interface of proxy-wasm-go SDK.
type payloadValidationContext struct {
        // Embed the default root http context here,
        // so that we don't need to reimplement all the methods.
        types.DefaultHttpContext
        totalRequestBodySize int
        requiredKeys         []string
}

var _ types.HttpContext = (*payloadValidationContext)(nil)

// Override types.DefaultHttpContext.
func (*payloadValidationContext) OnHttpRequestHeaders(numHeaders int, _ bool) types.Action {
        contentType, err := proxywasm.GetHttpRequestHeader("content-type")
        if err != nil || contentType != "application/json" {
                // If the header doesn't have the expected content value, send the 403 response,
                if err := proxywasm.SendHttpResponse(403, nil, []byte("content-type must be provided"), -1); err != nil {
                        panic(err)
                }
                // and terminates the further processing of this traffic by ActionPause.
                return types.ActionPause
        }

        // ActionContinue lets the host continue the processing the body.
        return types.ActionContinue
}

// Override types.DefaultHttpContext.
func (ctx *payloadValidationContext) OnHttpRequestBody(bodySize int, endOfStream bool) types.Action {
        ctx.totalRequestBodySize += bodySize
        if !endOfStream {
                // OnHttpRequestBody may be called each time a part of the body is received.
                // Wait until we see the entire body to replace.
                return types.ActionPause
        }

        body, err := proxywasm.GetHttpRequestBody(0, ctx.totalRequestBodySize)
        if err != nil {
                proxywasm.LogErrorf("failed to get request body: %v", err)
                return types.ActionContinue
        }
        if !ctx.validatePayload(body) {
                // If the validation fails, send the 403 response,
                if err := proxywasm.SendHttpResponse(403, nil, []byte("invalid payload"), -1); err != nil {
                        proxywasm.LogErrorf("failed to send the 403 response: %v", err)
                }
                // and terminates this traffic.
                return types.ActionPause
        }

        return types.ActionContinue
}

// validatePayload validates the given json payload.
// Note that this function parses the json data by gjson, since TinyGo doesn't support encoding/json.
func (ctx *payloadValidationContext) validatePayload(body []byte) bool {
        if !gjson.ValidBytes(body) {
                proxywasm.LogErrorf("body is not a valid json: %q", string(body))
                return false
        }
        jsonData := gjson.ParseBytes(body)

        // Do any validation on the json. Check if required keys exist here as an example.
        // The required keys are configurable via the plugin configuration.
        for _, requiredKey := range ctx.requiredKeys {
                if !jsonData.Get(requiredKey).Exists() {
                        proxywasm.LogErrorf("required key (%v) is missing: %v", requiredKey, jsonData)
                        return false
                }
        }

        return true
}
```

编译：

```
export GOPROXY=https://proxy.golang.com.cn,direct

tinygo build -o main.wasm -scheduler=none -target=wasi main.go

docker build --tag registry.cn-qingdao.aliyuncs.com/hxpdocker/json_validation:v1 . --no-cache
docker push registry.cn-qingdao.aliyuncs.com/hxpdocker/json_validation:v1
```

wp-ingressgateway-json_validation.yaml

kubectl apply -f wp-ingressgateway-json_validation.yaml -n istio-system

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
  url: oci://registry.cn-qingdao.aliyuncs.com/hxpdocker/json_validation:v1
  phase: STATS	
  imagePullPolicy: Always
  pluginConfig:
    requiredKeys: ["id", "token"]
```

curl   -d '{"token":"111"}' -H "content-type: application/json" -XGET http://192.168.229.128:32376/productpage

2022-05-21T04:06:57.647900Z     error   envoy wasm      wasm log istio-system.ingressgateway: required key (id) is missing: {"token":"111"}



curl   -d '{"id":1,"token":"111"}' -H "content-type: application/json" -XGET http://192.168.229.128:32376/productpage



## metrics

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
        "fmt"

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
        return &metricPluginContext{}
}

type metricPluginContext struct {
        // Embed the default plugin context here,
        // so that we don't need to reimplement all the methods.
        types.DefaultPluginContext
}

// Override types.DefaultPluginContext.
func (ctx *metricPluginContext) NewHttpContext(contextID uint32) types.HttpContext {
        return &metricHttpContext{}
}

type metricHttpContext struct {
        // Embed the default http context here,
        // so that we don't need to reimplement all the methods.
        types.DefaultHttpContext
}

const (
        customHeaderKey         = "my-custom-header"
        customHeaderValueTagKey = "value"
)

var counters = map[string]proxywasm.MetricCounter{}

// Override types.DefaultHttpContext.
func (ctx *metricHttpContext) OnHttpRequestHeaders(numHeaders int, endOfStream bool) types.Action {
        customHeaderValue, err := proxywasm.GetHttpRequestHeader(customHeaderKey)
        if err == nil {
                counter, ok := counters[customHeaderValue]
                if !ok {
                        // This metric is processed as: custom_header_value_counts{value="foo",reporter="wasmgosdk"} n.
                        // The extraction rule is defined in envoy.yaml as a bootstrap configuration.
                        // See https://www.envoyproxy.io/docs/envoy/latest/api-v3/config/metrics/v3/stats.proto#config-metrics-v3-statsconfig.
                        fqn := fmt.Sprintf("custom_header_value_counts_%s=%s_reporter=wasmgosdk", customHeaderValueTagKey, customHeaderValue)
                        counter = proxywasm.DefineCounterMetric(fqn)
                        counters[customHeaderValue] = counter
                }
                counter.Increment(1)
        }
        return types.ActionContinue
}
```

编译：

```
export GOPROXY=https://proxy.golang.com.cn,direct

tinygo build -o main.wasm -scheduler=none -target=wasi main.go

docker build --tag registry.cn-qingdao.aliyuncs.com/hxpdocker/metrics:v1 . --no-cache
docker push registry.cn-qingdao.aliyuncs.com/hxpdocker/metrics:v1
```

wp-ingressgateway-metrics.yaml

kubectl apply -f wp-ingressgateway-metrics.yaml -n istio-system

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
  url: oci://registry.cn-qingdao.aliyuncs.com/hxpdocker/metrics:v1
  phase: STATS	
  imagePullPolicy: Always
```



```
  meshConfig:
      proxyMetadata:
        # Enable dynamic bootstrap generation
        # https://github.com/istio/istio/pull/33456
        BOOTSTRAP_XDS_AGENT: "true"
```

ef-metrics.yaml

kubectl apply -f ef-metrics.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: ef-metrics
spec:
  workloadSelector:
    labels:
      app: productpage
  configPatches:
  - applyTo: BOOTSTRAP
    patch:
      operation: MERGE
      value:
          stats_config:
            stats_tags:
            - tag_name: value
              regex: '(_value=([a-zA-Z]+))'
            - tag_name: myreporter
              regex: '(_reporter=([a-zA-Z]+))'
```

curl   -H "my-custom-header: foo" -XGET http://192.168.229.128:32376/productpage

http://192.168.229.128:15000/stats/prometheus

```
custom_header_value_counts{value="foo",myreporter="wasmgosdk"} 2
```



## network

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
        return &pluginContext{counter: proxywasm.DefineCounterMetric("proxy_wasm_go.connection_counter")}
}

type pluginContext struct {
        // Embed the default plugin context here,
        // so that we don't need to reimplement all the methods.
        types.DefaultPluginContext
        counter proxywasm.MetricCounter
}

// Override types.DefaultPluginContext.
func (ctx *pluginContext) NewTcpContext(contextID uint32) types.TcpContext {
        return &networkContext{counter: ctx.counter}
}

type networkContext struct {
        // Embed the default tcp context here,
        // so that we don't need to reimplement all the methods.
        types.DefaultTcpContext
        counter proxywasm.MetricCounter
}

// Override types.DefaultTcpContext.
func (ctx *networkContext) OnNewConnection() types.Action {
        proxywasm.LogInfo("new connection!")
        return types.ActionContinue
}

// Override types.DefaultTcpContext.
func (ctx *networkContext) OnDownstreamData(dataSize int, endOfStream bool) types.Action {
        if dataSize == 0 {
                return types.ActionContinue
        }

        data, err := proxywasm.GetDownstreamData(0, dataSize)
        if err != nil && err != types.ErrorStatusNotFound {
                proxywasm.LogCriticalf("failed to get downstream data: %v", err)
                return types.ActionContinue
        }

        proxywasm.LogInfof(">>>>>> downstream data received >>>>>>\n%s", string(data))
        return types.ActionContinue
}

// Override types.DefaultTcpContext.
func (ctx *networkContext) OnDownstreamClose(types.PeerType) {
        proxywasm.LogInfo("downstream connection close!")
        return
}

// Override types.DefaultTcpContext.
func (ctx *networkContext) OnUpstreamData(dataSize int, endOfStream bool) types.Action {
        if dataSize == 0 {
                return types.ActionContinue
        }

        ret, err := proxywasm.GetProperty([]string{"upstream", "address"})
        if err != nil {
                proxywasm.LogCriticalf("failed to get upstream data: %v", err)
                return types.ActionContinue
        }

        proxywasm.LogInfof("remote address: %s", string(ret))

        data, err := proxywasm.GetUpstreamData(0, dataSize)
        if err != nil && err != types.ErrorStatusNotFound {
                proxywasm.LogCritical(err.Error())
        }

        proxywasm.LogInfof("<<<<<< upstream data received <<<<<<\n%s", string(data))
        return types.ActionContinue
}

// Override types.DefaultTcpContext.
func (ctx *networkContext) OnStreamDone() {
        ctx.counter.Increment(1)
        proxywasm.LogInfo("connection complete!")
}
```

编译：

```
export GOPROXY=https://proxy.golang.com.cn,direct

tinygo build -o main.wasm -scheduler=none -target=wasi main.go

```



```
kubectl create cm wasm --from-file=main.wasm -n istio
```

tcp-echo.yaml

kubectl apply -f tcp-echo.yaml -n istio

```
apiVersion: v1
kind: Service
metadata:
  name: tcp-echo
  labels:
    app: tcp-echo
    service: tcp-echo
spec:
  ports:
  - name: tcp
    port: 9000
  - name: tcp-other
    port: 9001
  # Port 9002 is omitted intentionally for testing the pass through filter chain.
  selector:
    app: tcp-echo
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: tcp-echo
spec:
  replicas: 1
  selector:
    matchLabels:
      app: tcp-echo
      version: v1
  template:
    metadata:
      labels:
        app: tcp-echo
        version: v1
      annotations:
        sidecar.istio.io/userVolumeMount: '[{"name":"wasm", "mountPath":"/var/local/lib/wasm-filters", "readonly":true}]'
        sidecar.istio.io/userVolume: '[{"name":"wasm", "configmap":{"name":"wasm"}}]'
    spec:
      containers:
      - name: tcp-echo
        image: docker.io/istio/tcp-echo-server:1.2
        imagePullPolicy: IfNotPresent
        args: [ "9000,9001,9002", "hello" ]
        ports:
        - containerPort: 9000
        - containerPort: 9001
```

gateway-tcp.yaml

kubectl apply -f gateway-tcp.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: Gateway
metadata:
  name: tcp-echo-gateway
spec:
  selector:
    istio: ingressgateway
  servers:
  - port:
      number: 31400
      name: tcp
      protocol: TCP
    hosts:
    - "*"
```

vs-tcp-echo.yaml

kubectl apply -f vs-tcp-echo.yaml -n istio

```
apiVersion: networking.istio.io/v1alpha3
kind: VirtualService
metadata:
  name: tcp-echo
spec:
  hosts:
  - "*"
  gateways:
  - tcp-echo-gateway
  tcp:
  - match:
    - port: 31400
    route:
    - destination:
        host: tcp-echo
        port:
          number: 9000
```



ef-network-wasm.yaml

kubectl apply -f ef-network-wasm.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: network
  namespace: istio
spec:
  workloadSelector:
    labels:
      app: tcp-echo
  configPatches:
  - applyTo: NETWORK_FILTER
    match:
      context: SIDECAR_INBOUND
      listener:
        filterChain:
          destinationPort: 9000
          filter:
            name: "envoy.filters.network.tcp_proxy"
    patch:
      operation: INSERT_BEFORE
      value:
        name: envoy.filters.network.wasm
        typed_config:
                '@type': type.googleapis.com/envoy.extensions.filters.network.wasm.v3.Wasm
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
2022-05-21T05:35:28.794685Z     info    envoy wasm      wasm log my_plugin: new connection!
2022-05-21T05:35:35.566658Z     info    envoy wasm      wasm log my_plugin: >>>>>> downstream data received >>>>>>
sdfs

2022-05-21T05:35:35.566902Z     info    envoy wasm      wasm log my_plugin: remote address: 172.20.2.97:9000
2022-05-21T05:35:35.566908Z     info    envoy wasm      wasm log my_plugin: <<<<<< upstream data received <<<<<<
hello sdfs
```

## postpone_requests

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

const tickMilliseconds uint32 = 1000

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
        return &pluginContext{
                contextID: contextID,
                postponed: make([]uint32, 0, 1024),
        }
}

type pluginContext struct {
        // Embed the default plugin context here,
        // so that we don't need to reimplement all the methods.
        types.DefaultPluginContext
        contextID uint32
        postponed []uint32
}

// Override types.DefaultPluginContext.
func (ctx *pluginContext) OnPluginStart(pluginConfigurationSize int) types.OnPluginStartStatus {
        if err := proxywasm.SetTickPeriodMilliSeconds(tickMilliseconds); err != nil {
                proxywasm.LogCriticalf("failed to set tick period: %v", err)
        }

        return types.OnPluginStartStatusOK
}

// Override types.DefaultPluginContext.
func (ctx *pluginContext) OnTick() {
        for len(ctx.postponed) > 0 {
                httpCtxId, tail := ctx.postponed[0], ctx.postponed[1:]
                proxywasm.LogInfof("resume request with contextID=%v", httpCtxId)
                proxywasm.SetEffectiveContext(httpCtxId)
                proxywasm.ResumeHttpRequest()
                ctx.postponed = tail
        }
}

// Override types.DefaultPluginContext.
func (ctx *pluginContext) NewHttpContext(contextID uint32) types.HttpContext {
        return &httpContext{
                contextID: contextID,
                pluginCtx: ctx,
        }
}

type httpContext struct {
        // Embed the default http context here,
        // so that we don't need to reimplement all the methods.
        types.DefaultHttpContext
        contextID uint32
        pluginCtx *pluginContext
}

// Override types.DefaultHttpContext.
func (ctx *httpContext) OnHttpRequestHeaders(numHeaders int, endOfStream bool) types.Action {
        proxywasm.LogInfof("postpone request with contextID=%d", ctx.contextID)
        ctx.pluginCtx.postponed = append(ctx.pluginCtx.postponed, ctx.contextID)
        return types.ActionPause
}
```



```
export GOPROXY=https://proxy.golang.com.cn,direct

tinygo build -o main.wasm -scheduler=none -target=wasi main.go

docker build --tag registry.cn-qingdao.aliyuncs.com/hxpdocker/postpone_requests:v1 . --no-cache
docker push registry.cn-qingdao.aliyuncs.com/hxpdocker/postpone_requests:v1
```

wp-ingressgateway-postpone_requests.yaml

kubectl apply -f wp-ingressgateway-postpone_requests.yaml -n istio-system

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
  url: oci://registry.cn-qingdao.aliyuncs.com/hxpdocker/postpone_requests:v1
  phase: STATS	
  imagePullPolicy: Always
```



```
2022-05-21T05:53:34.641291Z     info    envoy wasm      wasm log istio-system.ingressgateway: resume request with contextID=26
2022-05-21T05:53:34.641293Z     info    envoy wasm      wasm log istio-system.ingressgateway: resume request with contextID=27
2022-05-21T05:53:34.641295Z     info    envoy wasm      wasm log istio-system.ingressgateway: resume request with contextID=28
2022-05-21T05:53:34.641297Z     info    envoy wasm      wasm log istio-system.ingressgateway: resume request with contextID=29
2022-05-21T05:53:34.641298Z     info    envoy wasm      wasm log istio-system.ingressgateway: resume request with contextID=30
```



## shared_data

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
        "encoding/binary"
        "errors"

        "github.com/tetratelabs/proxy-wasm-go-sdk/proxywasm"
        "github.com/tetratelabs/proxy-wasm-go-sdk/proxywasm/types"
)

const (
        sharedDataKey                 = "shared_data_key"
        sharedDataInitialValue uint64 = 10000000
)

func main() {
        proxywasm.SetVMContext(&vmContext{})
}

type (
        vmContext     struct{}
        pluginContext struct {
                // Embed the default plugin context here,
                // so that we don't need to reimplement all the methods.
                types.DefaultPluginContext
        }

        httpContext struct {
                // Embed the default http context here,
                // so that we don't need to reimplement all the methods.
                types.DefaultHttpContext
        }
)

// Override types.VMContext.
func (*vmContext) OnVMStart(vmConfigurationSize int) types.OnVMStartStatus {
        initialValueBuf := make([]byte, 8)
        binary.LittleEndian.PutUint64(initialValueBuf, sharedDataInitialValue)
        if err := proxywasm.SetSharedData(sharedDataKey, initialValueBuf, 0); err != nil {
                proxywasm.LogWarnf("error setting shared data on OnVMStart: %v", err)
        }
        return types.OnVMStartStatusOK
}

// Override types.DefaultVMContext.
func (*vmContext) NewPluginContext(contextID uint32) types.PluginContext {
        return &pluginContext{}
}

// Override types.DefaultPluginContext.
func (*pluginContext) NewHttpContext(contextID uint32) types.HttpContext {
        return &httpContext{}
}

// Override types.DefaultHttpContext.
func (ctx *httpContext) OnHttpRequestHeaders(numHeaders int, endOfStream bool) types.Action {
        for {
                value, err := ctx.incrementData()
                if err == nil {
                        proxywasm.LogInfof("shared value: %d", value)
                } else if errors.Is(err, types.ErrorStatusCasMismatch) {
                        continue
                }
                break
        }
        return types.ActionContinue
}

func (ctx *httpContext) incrementData() (uint64, error) {
        value, cas, err := proxywasm.GetSharedData(sharedDataKey)
        if err != nil {
                proxywasm.LogWarnf("error getting shared data on OnHttpRequestHeaders: %v", err)
                return 0, err
        }

        buf := make([]byte, 8)
        ret := binary.LittleEndian.Uint64(value) + 1
        binary.LittleEndian.PutUint64(buf, ret)
        if err := proxywasm.SetSharedData(sharedDataKey, buf, cas); err != nil {
                proxywasm.LogWarnf("error setting shared data on OnHttpRequestHeaders: %v", err)
                return 0, err
        }
        return ret, err
}
```



```
export GOPROXY=https://proxy.golang.com.cn,direct

tinygo build -o main.wasm -scheduler=none -target=wasi main.go

docker build --tag registry.cn-qingdao.aliyuncs.com/hxpdocker/shared_data:v1 . --no-cache
docker push registry.cn-qingdao.aliyuncs.com/hxpdocker/shared_data:v1
```

wp-ingressgateway-shared_data.yaml

kubectl apply -f wp-ingressgateway-shared_data.yaml -n istio-system

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
  url: oci://registry.cn-qingdao.aliyuncs.com/hxpdocker/shared_data:v1
  phase: STATS	
  imagePullPolicy: Always
```



```
2022-05-21T06:00:08.804713Z     info    envoy wasm      wasm log istio-system.ingressgateway: shared value: 10000003
2022-05-21T06:00:09.012004Z     info    envoy wasm      wasm log istio-system.ingressgateway: shared value: 10000004
2022-05-21T06:00:09.196238Z     info    envoy wasm      wasm log istio-system.ingressgateway: shared value: 10000005
2022-05-21T06:00:09.388202Z     info    envoy wasm      wasm log istio-system.ingressgateway: shared value: 10000006
2022-05-21T06:00:09.572011Z     info    envoy wasm      wasm log istio-system.ingressgateway: shared value: 10000007
```



## shared_queue

receiver/main.go

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
        "fmt"

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
        return &receiverPluginContext{contextID: contextID}
}

type receiverPluginContext struct {
        // Embed the default plugin context here,
        // so that we don't need to reimplement all the methods.
        contextID uint32
        types.DefaultPluginContext
        queueName string
}

// Override types.DefaultPluginContext.
func (ctx *receiverPluginContext) OnPluginStart(pluginConfigurationSize int) types.OnPluginStartStatus {
        // Get Plugin configuration.
        config, err := proxywasm.GetPluginConfiguration()
        if err != nil {
                panic(fmt.Sprintf("failed to get plugin config: %v", err))
        }

        // Treat the config as the queue name for receiving.
        ctx.queueName = string(config)

        queueID, err := proxywasm.RegisterSharedQueue(ctx.queueName)
        if err != nil {
                panic("failed register queue")
        }
        proxywasm.LogInfof("queue \"%s\" registered as queueID=%d by contextID=%d", ctx.queueName, queueID, ctx.contextID)
        return types.OnPluginStartStatusOK
}

// Override types.DefaultPluginContext.
func (ctx *receiverPluginContext) OnQueueReady(queueID uint32) {
        data, err := proxywasm.DequeueSharedQueue(queueID)
        switch err {
        case types.ErrorStatusEmpty:
                return
        case nil:
                proxywasm.LogInfof("(contextID=%d) dequeued data from %s(queueID=%d): %s", ctx.contextID, ctx.queueName, queueID, string(data))
        default:
                proxywasm.LogCriticalf("error retrieving data from queue %d: %v", queueID, err)
        }
}
```

sender/main.go 

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
        "encoding/hex"
        "fmt"
        "hash/fnv"

        "github.com/tetratelabs/proxy-wasm-go-sdk/proxywasm"
        "github.com/tetratelabs/proxy-wasm-go-sdk/proxywasm/types"
)

const (
        receiverVMID = "receiver"
        queueName    = "http_headers"
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
        return &senderPluginContext{contextID: contextID}
}

type senderPluginContext struct {
        // Embed the default plugin context here,
        // so that we don't need to reimplement all the methods.
        types.DefaultPluginContext
        config    string
        contextID uint32
}

func newPluginContext(uint32) types.PluginContext {
        return &senderPluginContext{}
}

// Override types.DefaultPluginContext.
func (ctx *senderPluginContext) OnPluginStart(pluginConfigurationSize int) types.OnPluginStartStatus {
        // Get Plugin configuration.
        config, err := proxywasm.GetPluginConfiguration()
        if err != nil {
                panic(fmt.Sprintf("failed to get plugin config: %v", err))
        }
        ctx.config = string(config)
        proxywasm.LogInfof("contextID=%d is configured for %s", ctx.contextID, ctx.config)
        return types.OnPluginStartStatusOK
}

// Override types.DefaultPluginContext.
func (ctx *senderPluginContext) NewHttpContext(contextID uint32) types.HttpContext {
        // If this PluginContext is not configured for Http, then return nil.
        if ctx.config != "http" {
                return nil
        }

        // Resolve queues.
        requestHeadersQueueID, err := proxywasm.ResolveSharedQueue(receiverVMID, "http_request_headers")
        if err != nil {
                proxywasm.LogCriticalf("error resolving queue id: %v", err)
        }

        responseHeadersQueueID, err := proxywasm.ResolveSharedQueue(receiverVMID, "http_response_headers")
        if err != nil {
                proxywasm.LogCriticalf("error resolving queue id: %v", err)
        }

        // Pass the resolved queueIDs to http contexts so they can enqueue.
        return &senderHttpContext{
                requestHeadersQueueID:  requestHeadersQueueID,
                responseHeadersQueueID: responseHeadersQueueID,
                contextID:              contextID,
        }
}

type senderHttpContext struct {
        // Embed the default http context here,
        // so that we don't need to reimplement all the methods.
        types.DefaultHttpContext
        contextID, requestHeadersQueueID, responseHeadersQueueID uint32
}

// Override types.DefaultHttpContext.
func (ctx *senderHttpContext) OnHttpRequestHeaders(int, bool) types.Action {
        headers, err := proxywasm.GetHttpRequestHeaders()
        if err != nil {
                proxywasm.LogCriticalf("error getting request headers: %v", err)
        }
        for _, h := range headers {
                msg := fmt.Sprintf("{\"key\": \"%s\",\"value\": \"%s\"}", h[0], h[1])
                if err := proxywasm.EnqueueSharedQueue(ctx.requestHeadersQueueID, []byte(msg)); err != nil {
                        proxywasm.LogCriticalf("error queueing: %v", err)
                } else {
                        proxywasm.LogInfof("enqueued data: %s", msg)
                }
        }
        return types.ActionContinue
}

// Override types.DefaultHttpContext.
func (ctx *senderHttpContext) OnHttpResponseHeaders(int, bool) types.Action {
        headers, err := proxywasm.GetHttpResponseHeaders()
        if err != nil {
                proxywasm.LogCriticalf("error getting response headers: %v", err)
        }
        for _, h := range headers {
                msg := fmt.Sprintf("{\"key\": \"%s\",\"value\": \"%s\"}", h[0], h[1])
                if err := proxywasm.EnqueueSharedQueue(ctx.responseHeadersQueueID, []byte(msg)); err != nil {
                        proxywasm.LogCriticalf("error queueing: %v", err)
                } else {
                        proxywasm.LogInfof("(contextID=%d) enqueued data: %s", ctx.contextID, msg)
                }
        }
        return types.ActionContinue
}

func (ctx *senderPluginContext) NewTcpContext(contextID uint32) types.TcpContext {
        // If this PluginContext is not configured for Tcp, then return nil.
        if ctx.config != "tcp" {
                return nil
        }

        // Resolve queue.
        queueID, err := proxywasm.ResolveSharedQueue(receiverVMID, "tcp_data_hashes")
        if err != nil {
                proxywasm.LogCriticalf("error resolving queue id: %v", err)
        }

        // Pass the resolved queueID to tcp contexts so they can enqueue.
        return &senderTcpContext{
                tcpHashesQueueID: queueID,
                contextID:        contextID,
        }
}

type senderTcpContext struct {
        types.DefaultTcpContext
        // Embed the default http context here,
        // so that we don't need to reimplement all the methods.
        tcpHashesQueueID uint32
        contextID        uint32
}

func (ctx *senderTcpContext) OnUpstreamData(dataSize int, endOfStream bool) types.Action {
        if dataSize == 0 {
                return types.ActionContinue
        }

        // Calculate the hash of the data frame.
        data, err := proxywasm.GetUpstreamData(0, dataSize)
        if err != nil && err != types.ErrorStatusNotFound {
                proxywasm.LogCritical(err.Error())
        }
        s := fnv.New128a()
        _, _ = s.Write(data)
        var buf []byte
        buf = s.Sum(buf)
        hash := hex.EncodeToString(buf)

        // Enqueue the hashed data frame.
        if err := proxywasm.EnqueueSharedQueue(ctx.tcpHashesQueueID, []byte(hash)); err != nil {
                proxywasm.LogCriticalf("error queueing: %v", err)
        } else {
                proxywasm.LogInfof("(contextID=%d) enqueued data: %s", ctx.contextID, hash)
        }
        return types.ActionContinue
}
```



```
export GOPROXY=https://proxy.golang.com.cn,direct

cd receiver/
tinygo build -o main.wasm -scheduler=none -target=wasi main.go

cd sender/
tinygo build -o main.wasm -scheduler=none -target=wasi main.go
```

envoyfilter实现：

```
kubectl create cm wasm-sender --from-file=sender/main.wasm -n istio
kubectl create cm wasm-reciever --from-file=receiver/main.wasm -n istio
```

deploy-productpage-shared_queue.yaml

kubectl apply -f deploy-productpage-shared_queue.yaml -n istio

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
        sidecar.istio.io/userVolumeMount: '[{"name":"wasm-sender", "mountPath":"/var/local/lib/wasm-filters/sender", "readonly":true},{"name":"wasm-reciever", "mountPath":"/var/local/lib/wasm-filters/reciever", "readonly":true}]'
        sidecar.istio.io/userVolume: '[{"name":"wasm-sender", "configmap":{"name":"wasm-sender"}},{"name":"wasm-reciever", "configmap":{"name":"wasm-reciever"}}]'
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
```





ef-bootstrap_extensions.yaml

kubectl apply -f ef-bootstrap_extensions.yaml -n istio

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: bootstrap-extensions
spec:
  workloadSelector:
    labels:
      app: productpage
  configPatches:
  - applyTo: BOOTSTRAP
    patch:
      operation: MERGE
      value:
        bootstrap_extensions:
        - name: envoy.bootstrap.wasm
          typed_config:
            "@type": type.googleapis.com/envoy.extensions.wasm.v3.WasmService
            singleton: true
            config:
              # Used as a queue name
              configuration:
                "@type": type.googleapis.com/google.protobuf.StringValue
                value: "http_request_headers"
              # Use the same vm_config as below, so we can reuse the same VM for multiple queues.
              vm_config:
                vm_id: "receiver"
                runtime: "envoy.wasm.runtime.v8"
                code:
                  local:
                    filename: "/var/local/lib/wasm-filters/reciever/main.wasm"

        - name: envoy.bootstrap.wasm
          typed_config:
            "@type": type.googleapis.com/envoy.extensions.wasm.v3.WasmService
            singleton: true
            config:
              # Used as a queue name
              configuration:
                "@type": type.googleapis.com/google.protobuf.StringValue
                value: "http_response_headers"
              # Use the same vm_config as above, so we can reuse the same VM for multiple queues.
              vm_config:
                vm_id: "receiver"
                runtime: "envoy.wasm.runtime.v8"
                code:
                  local:
                    filename: "/var/local/lib/wasm-filters/reciever/main.wasm"

        - name: envoy.bootstrap.wasm
          typed_config:
            "@type": type.googleapis.com/envoy.extensions.wasm.v3.WasmService
            singleton: true
            config:
              configuration:
                # Used as a queue name
                "@type": type.googleapis.com/google.protobuf.StringValue
                value: "tcp_data_hashes"
              # Use the same vm_config as above, so we can reuse the same VM for multiple queues.
              vm_config:
                vm_id: "receiver"
                runtime: "envoy.wasm.runtime.v8"
                code:
                  local:
                    filename: "/var/local/lib/wasm-filters/reciever/main.wasm"
```

ef-sender-wasm.yaml

kubectl apply -f ef-sender-wasm.yaml -n istio

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: sender
spec:
  workloadSelector:
    labels:
      app: productpage
  configPatches:
    - applyTo: HTTP_FILTER
      match:
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
                      "@type": type.googleapis.com/udpa.type.v1.TypedStruct
                      type_url: type.googleapis.com/envoy.extensions.filters.http.wasm.v3.Wasm
                      value:
                        config:
                          configuration:
                            "@type": type.googleapis.com/google.protobuf.StringValue
                            value: "http"
                          vm_config:
                            vm_id: "sender"
                            runtime: "envoy.wasm.runtime.v8"
                            code:
                              local:
                                filename: /var/local/lib/wasm-filters/sender/main.wasm
```





```
022-05-21T07:52:58.788470Z     info    envoy wasm      wasm log sender: enqueued data: {"key": "accept-language","value": "en-GB,en-US;q=0.9,en;q=0.8"}
2022-05-21T07:52:58.788473Z     info    envoy wasm      wasm log sender: enqueued data: {"key": "cache-control","value": "max-age=0"}
2022-05-21T07:52:58.788478Z     info    envoy wasm      wasm log sender: enqueued data: {"key": "upgrade-insecure-requests","value": "1"}
2022-05-21T07:52:58.788481Z     info    envoy wasm      wasm log sender: enqueued data: {"key": "x-forwarded-for","value": "172.20.0.0"}
2022-05-21T07:52:58.788483Z     info    envoy wasm      wasm log sender: enqueued data: {"key": "x-forwarded-proto","value": "http"}
2022-05-21T07:52:58.788486Z     info    envoy wasm      wasm log sender: enqueued data: {"key": "x-request-id","value": "886d3a45-68ae-9171-a600-95469f56ba8f"}
2022-05-21T07:52:58.788488Z     info    envoy wasm      wasm log sender: enqueued data: {"key": "x-envoy-attempt-count","value": "1"}
2022-05-21T07:52:58.788493Z     info    envoy wasm      wasm log sender: enqueued data: {"key": "x-b3-traceid","value": "dfd4f63363949bac94418d60960ad602"}
2022-05-21T07:52:58.788495Z     info    envoy wasm      wasm log sender: enqueued data: {"key": "x-b3-spanid","value": "94418d60960ad602"}
2022-05-21T07:52:58.788498Z     info    envoy wasm      wasm log sender: enqueued data: {"key": "x-b3-sampled","value": "1"}
2022-05-21T07:52:58.788501Z     info    envoy wasm      wasm log sender: enqueued data: {"key": "x-envoy-internal","value": "true"}
2022-05-21T07:52:58.788506Z     info    envoy wasm      wasm log sender: enqueued data: {"key": "x-forwarded-client-cert","value": "By=spiffe://cluster.local/ns/istio/sa/bookinfo-productpage;Hash=5fdeb552c20a9a221ca172997ea77c365506b15beee96dc3b929dd2817da7651;Subject="";URI=spiffe://cluster.local/ns/istio-system/sa/istio-ingressgateway-service-account"}
2022-05-21T07:52:58.788846Z     info    envoy wasm      wasm log receiver: (contextID=1) dequeued data from http_request_headers(queueID=1): {"key": ":authority","value": "192.168.229.128:32376"}
2022-05-21T07:52:58.788858Z     info    envoy wasm      wasm log receiver: (contextID=1) dequeued data from http_request_headers(queueID=1): {"key": ":path","value": "/productpage"}
2022-05-21T07:52:58.788862Z     info    envoy wasm      wasm log receiver: (contextID=1) dequeued data from http_request_headers(queueID=1): {"key": ":method","value": "GET"}
2022-05-21T07:52:58.788864Z     info    envoy wasm      wasm log receiver: (contextID=1) dequeued data from http_request_headers(queueID=1): {"key": ":scheme","value": "http"}
2022-05-21T07:52:58.788868Z     info    envoy wasm      wasm log receiver: (contextID=1) dequeued data from http_request_headers(queueID=1): {"key": "user-agent","value": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/99.0.4844.82 Safari/537.36"}
2022-05-21T07:52:58.788872Z     info    envoy wasm      wasm log receiver: (contextID=1) dequeued data from http_request_headers(queueID=1): {"key": "accept","value": "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9"}
2022-05-21T07:52:58.788876Z     info    envoy wasm      wasm log receiver: (contextID=1) dequeued data from http_request_headers(queueID=1): {"key": "accept-encoding","value": "gzip, deflate"}
2022-05-21T07:52:58.788880Z     info    envoy wasm      wasm log receiver: (contextID=1) dequeued data from http_request_headers(queueID=1): {"key": "accept-language","value": "en-GB,en-US;q=0.9,en;q=0.8"}
2022-05-21T07:52:58.788883Z     info    envoy wasm      wasm log receiver: (contextID=1) dequeued data from http_request_headers(queueID=1): {"key": "cache-control","value": "max-age=0"}
2022-05-21T07:52:58.788886Z     info    envoy wasm      wasm log receiver: (contextID=1) dequeued data from http_request_headers(queueID=1): {"key": "upgrade-insecure-requests","value": "1"}
2022-05-21T07:52:58.788888Z     info    envoy wasm      wasm log receiver: (contextID=1) dequeued data from http_request_headers(queueID=1): {"key": "x-forwarded-for","value": "172.20.0.0"}
2022-05-21T07:52:58.788891Z     info    envoy wasm      wasm log receiver: (contextID=1) dequeued data from http_request_headers(queueID=1): {"key": "x-forwarded-proto","value": "http"}
2022-05-21T07:52:58.788898Z     info    envoy wasm      wasm log receiver: (contextID=1) dequeued data from http_request_headers(queueID=1): {"key": "x-request-id","value": "886d3a45-68ae-9171-a600-95469f56ba8f"}
2022-05-21T07:52:58.788901Z     info    envoy wasm      wasm log receiver: (contextID=1) dequeued data from http_request_headers(queueID=1): {"key": "x-envoy-attempt-count","value": "1"}
2022-05-21T07:52:58.788904Z     info    envoy wasm      wasm log receiver: (contextID=1) dequeued data from http_request_headers(queueID=1): {"key": "x-b3-traceid","value": "dfd4f63363949bac94418d60960ad602"}
2022-05-21T07:52:58.788907Z     info    envoy wasm      wasm log receiver: (contextID=1) dequeued data from http_request_headers(queueID=1): {"key": "x-b3-spanid","value": "94418d60960ad602"}
2022-05-21T07:52:58.788909Z     info    envoy wasm      wasm log receiver: (contextID=1) dequeued data from http_request_headers(queueID=1): {"key": "x-b3-sampled","value": "1"}
2022-05-21T07:52:58.788912Z     info    envoy wasm      wasm log receiver: (contextID=1) dequeued data from http_request_headers(queueID=1): {"key": "x-envoy-internal","value": "true"}
```



## vm_plugin_configuration

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

type vmContext struct{}

func (*vmContext) OnVMStart(vmConfigurationSize int) types.OnVMStartStatus {
        data, err := proxywasm.GetVMConfiguration()
        if err != nil {
                proxywasm.LogCriticalf("error reading vm configuration: %v", err)
        }

        proxywasm.LogInfof("vm config: %s", string(data))
        return types.OnVMStartStatusOK
}

// Implement types.VMContext.
func (*vmContext) NewPluginContext(uint32) types.PluginContext {
        return &pluginContext{}
}

type pluginContext struct {
        // Embed the default plugin context here,
        // so that we don't need to reimplement all the methods.
        types.DefaultPluginContext
}

// Override types.DefaultPluginContext.
func (ctx pluginContext) OnPluginStart(pluginConfigurationSize int) types.OnPluginStartStatus {
        data, err := proxywasm.GetPluginConfiguration()
        if err != nil {
                proxywasm.LogCriticalf("error reading plugin configuration: %v", err)
        }

        proxywasm.LogInfof("plugin config: %s", string(data))
        return types.OnPluginStartStatusOK
}
```

编译

```
export GOPROXY=https://proxy.golang.com.cn,direct

tinygo build -o main.wasm -scheduler=none -target=wasi main.go

docker build --tag registry.cn-qingdao.aliyuncs.com/hxpdocker/vm_plugin_configuration:v1 . --no-cache
docker push registry.cn-qingdao.aliyuncs.com/hxpdocker/vm_plugin_configuration:v1
```

wp-ingressgateway-vm_plugin_configuration.yaml

kubectl apply -f wp-ingressgateway-vm_plugin_configuration.yaml -n istio-system

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
  url: oci://registry.cn-qingdao.aliyuncs.com/hxpdocker/vm_plugin_configuration:v1
  phase: STATS	
  imagePullPolicy: Always
  pluginConfig:
    test1: test1
```



```
2022-05-21T08:01:14.554040Z     info    envoy wasm      wasm log: vm config: 
2022-05-21T08:01:14.554129Z     info    envoy wasm      wasm log: plugin config: {"test1":"test1"}
2022-05-21T08:01:14.559056Z     critical        envoy wasm      wasm log: error reading vm configuration: error status returned by host: not found
2022-05-21T08:01:14.559069Z     info    envoy wasm      wasm log: vm config: 
2022-05-21T08:01:14.559156Z     info    envoy wasm      wasm log: plugin config: {"test1":"test1"}
2022-05-21T08:01:14.559538Z     critical        envoy wasm      wasm log: error reading vm configuration: error status returned by host: not found
2022-05-21T08:01:14.559545Z     info    envoy wasm      wasm log: vm config: 
2022-05-21T08:01:14.559629Z     info    envoy wasm      wasm log: plugin config: {"test1":"test1"}
```

