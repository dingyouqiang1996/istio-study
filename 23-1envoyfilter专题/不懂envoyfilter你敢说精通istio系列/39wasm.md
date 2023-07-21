# 1什么是wasm

wasm即webAssembly。首先 `WebAssembly` 是由 `Web` 和 `Assembly` 两个词构成，其中 `Web` 表明它一定和前端有关。`Assembly` 的意思是汇编，汇编对应机器码，而机器码和 `CPU` 的指令集有关，接下来补一下相关的知识。

`WebAssembly` 的历史：

> 2015 年 4 月，WebAssembly Community Group 成立；
> 2015 年 6 月，WebAssembly 第一次以 WCG 的官方名义向外界公布；
> 2016 年 8 月，WebAssembly 开始进入了漫长的 “Browser Preview” 阶段；
> 2017 年 2 月，WebAssembly 官方 LOGO 在 Github 上的众多讨论中被最终确定；同年同月，一个历史性的阶段，四大浏览器（FireFox、Chrome、Edge、WebKit）在 WebAssembly 的 MVP（最小可用版本）标准实现上达成共识，这意味着 WebAssembly 在其 MVP 标准上的 “Brower Preview” 阶段已经结束；
> 2017 年 8 月，W3C WebAssembly Working Group 成立，意味着 WebAssembly 正式成为 W3C 众多技术标准中的一员。

`WebAssembly` 于 `2019` 年 `12` 月 `5` 日成为万维网联盟（`W3C`）的推荐标准，与 `HTML`，`CSS` 和 `JavaScript` 一起成为 `Web` 的第四种语言。



# 2什么envoy中的wasm

Envoy 社区在 Envoy 中嵌入了 WASM 虚拟机以获得一个安全的沙箱环境，用于动态加载和运行可拔插的扩展代码（被编译为 WASM 字节码），简化 Envoy 二次开发和功能增强的复杂度。实际上，在 Envoy 社区将该特性合入主干之前，Istio 社区就已经在力推该特性，并基于该特性重写了部分的功能扩展。

## 2.1架构:

![1](39wasm\1.png)



## 2.2为什么要使用WASM Filter？

使用WASM实现过滤器的扩展，有如下优势：

- 敏捷性：过滤器可以动态加载到正在运行的Envoy进程中，而无需停止或重新编译。
- 可维护性：不必更改Envoy自身基础代码库即可扩展其功能。
- 多样性：可以将流行的编程语言（例如C/C++和Rust）编译为WASM，因此开发人员可以选择实现过滤器的编程语言。
- 可靠性和隔离性：过滤器会被部署到VM沙箱中，因此与Envoy进程本身是隔离的；即使当WASM Filter出现问题导致崩溃时，它也不会影响Envoy进程。
- 安全性：过滤器通过预定义API与Envoy代理进行通信，因此它们可以访问并只能修改有限数量的连接或请求属性。

当前WASM实现过滤器的扩展，也需要考虑以下缺点是否可以容忍：

- 性能约为C++编写的原生静态编译的Filter的70％。
- 由于需要启动一个或多个WASM虚拟机，因此会消耗一定的内存使用量。

# 3envoy wasm的用途

1bootstrap_extensions

2access log

3http filter 可用于授权,外部系统调用等

4network filter

5stats sink

# 4配置

```
{
  "config": "{...}"配置
}
```

```
{
  "config": "{...}",配置
  "singleton": "..."是否单例
}
```

config:

```
{
  "name": "...",名称
  "root_id": "...",根id
  "vm_config": "{...}",vm配置
  "configuration": "{...}",参数
  "fail_open": "...",如果vm有错误，关闭vm
  "capability_restriction_config": "{...}"能力限制
}
```

vm_config:

```
{
  "vm_id": "...",vm id
  "runtime": "...",运行时
  "code": "{...}",代码
  "configuration": "{...}",配置参数
  "allow_precompiled": "...",允许预编译
  "nack_on_code_cache_miss": "...",没有缓存时，是否同步拉取代码
  "environment_variables": "{...}"环境变量
}
```

runtime：

**envoy.\**wasm\**.runtime.null**: Null sandbox, the **Wasm** module must be compiled and linked into the Envoy binary. The registered name is given in the *code* field as *inline_string*.

**envoy.\**wasm\**.runtime.v8**: [V8](https://v8.dev/)-based WebAssembly runtime.

**envoy.\**wasm\**.runtime.wamr**: [WAMR](https://github.com/bytecodealliance/wasm-micro-runtime/)-based WebAssembly runtime. This runtime is not enabled in the official build.

**envoy.\**wasm\**.runtime.wavm**: [WAVM](https://wavm.github.io/)-based WebAssembly runtime. This runtime is not enabled in the official build.

**envoy.\**wasm\**.runtime.wasmtime**: [**Wasm**time](https://wasmtime.dev/)-based WebAssembly runtime. This runtime is not enabled in the official build.



code：

```
{
  "local": "{...}",本地代码
  "remote": "{...}"远程代码
}
```

local:

```
{
  "filename": "...",本地文件
  "inline_bytes": "...",字节
  "inline_string": "...",字符串
  "environment_variable": "..."环境变量
}
```

remote:

```
{
  "http_uri": "{...}",http rui
  "sha256": "...",sha
  "retry_policy": "{...}"重试策略
}
```

environment_variables：

```
{
  "host_env_keys": [],如果vm中有这个环境变量，这注入，否则忽略
  "key_values": "{...}"key value对
}
```

capability_restriction_config：

```
{
  "allowed_capabilities": "{...}"允许的能力，未实现
}
```

allowed_capabilities：

extensions.**wasm**.v3.SanitizationConfig

This is currently unimplemented.

# 5示例

```
  meshConfig:
      proxyMetadata:
        # Enable dynamic bootstrap generation
        # https://github.com/istio/istio/pull/33456
        BOOTSTRAP_XDS_AGENT: "true"
```



## 5.2bootstrap_extensions

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



ef-wasm-bootstrap_extensions.yaml

kubectl apply -f ef-wasm-bootstrap_extensions.yaml -n istio

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





## 5.3access log

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

func (ctx *httpHeaders) OnHttpRequestHeaders(numHeaders int, endOfStream bool) types.Action {
        path := []string{"%START_TIME%"}
        startTime,err:=proxywasm.GetProperty(path)
        
        if err!=nil{
                proxywasm.LogCriticalf("GetProperty: %v", err)
        	
        }
        
        proxywasm.LogCriticalf("startTime: %s", string(startTime))

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
func (ctx *httpHeaders) OnHttpStreamDone() {
        path := []string{"%START_TIME%"}
        startTime,err:=proxywasm.GetProperty(path)
        
        if err!=nil{
                proxywasm.LogCriticalf("GetProperty: %v", err)
        	
        }
        
        proxywasm.LogCriticalf("startTime: %s", string(startTime))
        proxywasm.LogCriticalf("%d finished", ctx.contextID)
}
```



```
export GOPROXY=https://proxy.golang.com.cn,direct

tinygo build -o main.wasm -scheduler=none -target=wasi main.go
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



ef-wasm-access-log.yaml

kubectl apply -f ef-wasm-access-log.yaml -n istio

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: wasm
spec:
  workloadSelector:
    labels:
      app: productpage
  configPatches:
  - applyTo: LISTENER
    match:
      context: SIDECAR_INBOUND
      listener:
        name: virtualInbound
    patch:
      operation: MERGE
      value:
        access_log:
          - filter:
              response_flag_filter:
                flags:
                - NR
            name: envoy.access_loggers.wasm
            typedConfig:
                '@type': type.googleapis.com/envoy.extensions.access_loggers.wasm.v3.WasmAccessLog
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
  - applyTo: NETWORK_FILTER
    match:
      context: SIDECAR_INBOUND
      listener:
        name: virtualInbound
        filterChain:
          destinationPort: 9080
          filter:
            name: "envoy.filters.network.http_connection_manager"
    patch:
      operation: MERGE
      value:
              name: envoy.filters.network.http_connection_manager
              typedConfig:
                '@type': type.googleapis.com/envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager
                access_log:
                  - name: envoy.access_loggers.wasm
                    typedConfig:
                        '@type': type.googleapis.com/envoy.extensions.access_loggers.wasm.v3.WasmAccessLog
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



## 5.4http filter 

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



ef-wasm-http_filter.yaml

kubectl apply -f ef-wasm-http_filter.yaml -n istio

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: wasm
spec:
  workloadSelector:
    labels:
      app: productpage
  configPatches:
  - applyTo: HTTP_FILTER
    match:
      context: SIDECAR_INBOUND
      listener:
        name: virtualInbound
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
                "@type": type.googleapis.com/envoy.extensions.filters.http.wasm.v3.Wasm
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



## 5.5network filter

部署tcp

tcp-echo-deploy.yaml

kubectl apply -f tcp-echo-deploy.yaml -n istio

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

tcp-echo.yaml

kubectl apply -f tcp-echo.yaml -n istio

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
---
apiVersion: networking.istio.io/v1alpha3
kind: DestinationRule
metadata:
  name: tcp-echo-destination
spec:
  host: tcp-echo
  subsets:
  - name: v1
    labels:
      version: v1
  - name: v2
    labels:
      version: v2
---
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
        subset: v1
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



ef-wasm-network-filter.yaml

kubectl apply -f ef-wasm-network-filter.yaml -n istio

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: wasm
  namespace: istio
spec:
  workloadSelector:
    labels:
      app: tcp-echo
  configPatches:
  - applyTo: NETWORK_FILTER
    match:
      listener:
        name: virtualInbound
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



## 5.6stats sink

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

编译：

```
export GOPROXY=https://proxy.golang.com.cn,direct

tinygo build -o main.wasm -scheduler=none -target=wasi main.go

```



```
kubectl create cm wasm --from-file=main.wasm -n istio
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



ef-wasm-stats_sink.yaml

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: wasm
spec:
  workloadSelector:
    labels:
      app: productpage
  configPatches:
  - applyTo: BOOTSTRAP
    patch:
      operation: MERGE
      value:
          stats_sinks:
          - name: envoy.stat_sinks.wasm
            typed_config:
                "@type": type.googleapis.com/envoy.extensions.stat_sinks.wasm.v3.Wasm
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

 failed to marshal bootstrap as JSON: proto: not found

## 5.7config字段详解

### 5.7.1fail_open

ef-wasm-fail_open.yaml

kubectl apply -f ef-wasm-fail_open.yaml -n istio

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: wasm
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
                "@type": type.googleapis.com/envoy.extensions.filters.http.wasm.v3.Wasm
                config:
                  name: my_plugin
                  configuration:
                    "@type": type.googleapis.com/google.protobuf.StringValue
                    value: |
                      {}
                  fail_open: true
                  vm_config:
                    runtime: "envoy.wasm.runtime.v8"
                    code:
                      local:
                        filename: /var/local/lib/wasm-filters/main.wasm
```



### 5.7.2runtime

![2](39wasm\2.jpg)

WAMR(**WASM**-Micro-Runtime), Wasmtime and WAVM runtimes are not included in Envoy release image by default.

#### 5.7.2.1v8

略

#### 5.7.2.2null

ef-wasm-null.yaml

kubectl apply -f ef-wasm-null.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: wasm
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
        name: envoy.filters.http.wasm
        typed_config:
                "@type": type.googleapis.com/envoy.extensions.filters.http.wasm.v3.Wasm
                config:
                  name: my_plugin
                  root_id: "stats_inbound"
                  configuration:
                    "@type": type.googleapis.com/google.protobuf.StringValue
                    value: |
                      {   "debug":"false","stat_prefix":"istio", "disable_host_header_fallback":true}
                  fail_open: true
                  vm_config:
                    runtime: "envoy.wasm.runtime.null"
                    code:
                      local:
                        inline_string: "envoy.wasm.stats"
```



### 5.7.3code_local

创建configmap

```
kubectl create configmap -n istio wasm-example-filter --from-file=example-filter.wasm
```

使用以下两个annotation将WASM过滤器的二进制文件注入到应用程序对应的Kubernetes服务中

```
sidecar.istio.io/userVolume: '[{"name":"wasmfilters-dir","configMap": {"name": "wasm-example-filter"}}]'
sidecar.istio.io/userVolumeMount: '[{"mountPath":"/var/local/lib/wasm-filters","name":"wasmfilters-dir"}]'
```

执行以下命令更新productpage-v1

```
kubectl patch deployment productpage-v1 -p '{"spec":{"template":{"metadata":{"annotations":{"sidecar.istio.io/userVolume":"[{\"name\":\"wasmfilters-dir\",\"configMap\": {\"name\": \"wasm-example-filter\"}}]","sidecar.istio.io/userVolumeMount":"[{\"mountPath\":\"/var/local/lib/wasm-filters\",\"name\":\"wasmfilters-dir\"}]"}}}}}'
```

在istio-proxy容器中的路径/var/local/lib/wasm-filters下，找到WASM过滤器的二进制文件

```
kubectl exec -it deployment/productpage-v1 -c istio-proxy -- ls /var/local/lib/wasm-filters/
```

ef-wasm-local.yaml

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: wasm
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
        name: envoy.filters.http.wasm
        typed_config:
                "@type": type.googleapis.com/envoy.extensions.filters.http.wasm.v3.Wasm
                config:
                  name: my_plugin
                  configuration:
                    "@type": type.googleapis.com/google.protobuf.StringValue
                    value: |
                      {}
                  fail_open: true
                  vm_config:
                    runtime: "envoy.wasm.runtime.null"
                    allow_precompiled: true
                    code:
                      local:
                        filename: /var/local/lib/wasm-filters/example-filter.wasm
```



### 5.7.4allow_precompiled

ef-wasm-allow_precompiled.yaml

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: wasm
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
                "@type": type.googleapis.com/envoy.extensions.filters.http.wasm.v3.Wasm
                config:
                  name: my_plugin
                  configuration:
                    "@type": type.googleapis.com/google.protobuf.StringValue
                    value: |
                      {}
                  fail_open: true
                  vm_config:
                    runtime: "envoy.wasm.runtime.v8"
                    allow_precompiled: true
                    code:
                      local:
                        filename: /var/local/lib/wasm-filters/main.wasm
```



### 5.7.5nack_on_code_cache_miss

ef-wasm-nack_on_code_cache_miss.yaml

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: wasm
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
                "@type": type.googleapis.com/envoy.extensions.filters.http.wasm.v3.Wasm
                config:
                  name: my_plugin
                  configuration:
                    "@type": type.googleapis.com/google.protobuf.StringValue
                    value: |
                      {}
                  fail_open: true
                  vm_config:
                    runtime: "envoy.wasm.runtime.v8"
                    nack_on_code_cache_miss: true
                    code:
                      local:
                        filename: /var/local/lib/wasm-filters/main.wasm
```



### 5.7.6environment_variables

ef-wasm-environment_variables.yaml

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: wasm
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
                "@type": type.googleapis.com/envoy.extensions.filters.http.wasm.v3.Wasm
                config:
                  name: my_plugin
                  configuration:
                    "@type": type.googleapis.com/google.protobuf.StringValue
                    value: |
                      {}
                  fail_open: true
                  vm_config:
                    runtime: "envoy.wasm.runtime.v8"
                    environment_variables:
                      host_env_keys:
                      - test
                      key_values:
                        test2: test      
                    code:
                      local:
                        filename: /var/local/lib/wasm-filters/main.wasm
```



### 5.7.7configuration

ef-wasm-configuration.yaml

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: wasm
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
        name: envoy.filters.http.wasm
        typed_config:
                "@type": type.googleapis.com/envoy.extensions.filters.http.wasm.v3.Wasm
                config:
                  name: my_plugin
                  configuration:
                    "@type": type.googleapis.com/google.protobuf.StringValue
                    value: |
                        { "debug": "false", "stat_prefix": "istio", "disable_host_header_fallback": true } 
                  fail_open: true
                  vm_config:
                    runtime: "envoy.wasm.runtime.null"
                    code:
                      local:
                        inline_string: "envoy.wasm.stats"
```

