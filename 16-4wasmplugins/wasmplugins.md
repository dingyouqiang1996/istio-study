# 什么是wasmPlugin

WasmPlugins provides a mechanism to extend the functionality provided by the Istio proxy through WebAssembly filters.

Order of execution (as part of Envoy’s filter chain) is determined by phase and priority settings, allowing the configuration of complex interactions between user-supplied WasmPlugins and Istio’s internal filters.



| Field             | Type               | Description                                                  | Required |
| ----------------- | ------------------ | ------------------------------------------------------------ | -------- |
| `selector`        | `WorkloadSelector` | Criteria used to select the specific set of pods/VMs on which this plugin configuration should be applied. If omitted, this configuration will be applied to all workload instances in the same namespace. If the `WasmPlugin` is present in the config root namespace, it will be applied to all applicable workloads in any namespace. | No       |
| `url`             | `string`           | URL of a Wasm module or OCI container. If no scheme is present, defaults to `oci://`, referencing an OCI image. Other valid schemes are `file://` for referencing .wasm module files present locally within the proxy container, and `http[s]://` for .wasm module files hosted remotely. | No       |
| `sha256`          | `string`           | SHA256 checksum that will be used to verify Wasm module or OCI container. If the `url` field already references a SHA256 (using the `@sha256:` notation), it must match the value of this field. If an OCI image is referenced by tag and this field is set, its checksum will be verified against the contents of this field after pulling. | No       |
| `imagePullPolicy` | `PullPolicy`       | The pull behaviour to be applied when fetching an OCI image. Only relevant when images are referenced by tag instead of SHA. Defaults to IfNotPresent, except when an OCI image is referenced in the `url` and the `latest` tag is used, in which case `Always` is the default, mirroring K8s behaviour. Setting is ignored if `url` field is referencing a Wasm module directly using `file://` or `http[s]://` | No       |
| `imagePullSecret` | `string`           | Credentials to use for OCI image pulling. Name of a K8s Secret in the same namespace as the `WasmPlugin` that contains a docker pull secret which is to be used to authenticate against the registry when pulling the image. | No       |
| `verificationKey` | `string`           | Public key that will be used to verify signatures of signed OCI images or Wasm modules. Must be supplied in PEM format. | No       |
| `pluginConfig`    | `Struct`           | The configuration that will be passed on to the plugin.      | No       |
| `pluginName`      | `string`           | The plugin name to be used in the Envoy configuration (used to be called `rootID`). Some .wasm modules might require this value to select the Wasm plugin to execute. | No       |
| `phase`           | `PluginPhase`      | Determines where in the filter chain this `WasmPlugin` is to be injected. | No       |
| `priority`        | `Int64Value`       | Determines ordering of `WasmPlugins` in the same `phase`. When multiple `WasmPlugins` are applied to the same workload in the same `phase`, they will be applied by priority, in descending order. If `priority` is not set, or two `WasmPlugins` exist with the same value, the ordering will be deterministically derived from name and namespace of the `WasmPlugins`. Defaults to `0`. | No       |



PluginPhase

| Name                | Description                                                  |
| ------------------- | ------------------------------------------------------------ |
| `UNSPECIFIED_PHASE` | Control plane decides where to insert the plugin. This will generally be at the end of the filter chain, right before the Router. Do not specify `PluginPhase` if the plugin is independent of others. |
| `AUTHN`             | Insert plugin before Istio authentication filters.           |
| `AUTHZ`             | Insert plugin before Istio authorization filters and after Istio authentication filters. |
| `STATS`             | Insert plugin before Istio stats filters and after Istio authorization filters. |



PullPolicy

| Name                 | Description                                                  |
| -------------------- | ------------------------------------------------------------ |
| `UNSPECIFIED_POLICY` | Defaults to IfNotPresent, except for OCI images with tag `latest`, for which the default will be Always. |
| `IfNotPresent`       | If an existing version of the image has been pulled before, that will be used. If no version of the image is present locally, we will pull the latest version. |
| `Always`             | We will always pull the latest version of an image when applying this plugin. |

# 准备工作

```
git clone https://github.com/tetratelabs/wasm-rate-limiting.git

export GOPROXY=https://proxy.golang.com.cn,direct

tinygo build -o main.wasm -scheduler=none -target=wasi main.go

docker build --tag registry.cn-qingdao.aliyuncs.com/hxpdocker/wasm-rate-limiting:v1 .
docker push registry.cn-qingdao.aliyuncs.com/hxpdocker/wasm-rate-limiting:v1
```

代码解析：

```
[root@node01 wasmplugin]# cat wasm-rate-limiting/main.go 
package main

import (
        "time"

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
        // the remaining token for rate limiting, refreshed periodically.
        remainToken int
        // // the preconfigured request per second for rate limiting.
        // requestPerSecond int
        // NOTE(jianfeih): any concerns about the threading and mutex usage for tinygo wasm?
        // the last time the token is refilled with `requestPerSecond`.
        lastRefillNanoSec int64
}

// Override types.DefaultPluginContext.
func (p *pluginContext) NewHttpContext(contextID uint32) types.HttpContext {
        return &httpHeaders{contextID: contextID, pluginContext: p}
}

type httpHeaders struct {
        // Embed the default http context here,
        // so that we don't need to reimplement all the methods.
        types.DefaultHttpContext
        contextID     uint32
        pluginContext *pluginContext
}

// Additional headers supposed to be injected to response headers.
var additionalHeaders = map[string]string{
        "who-am-i":    "wasm-extension",
        "injected-by": "istio-api!",
}

func (ctx *httpHeaders) OnHttpResponseHeaders(numHeaders int, endOfStream bool) types.Action {
        for key, value := range additionalHeaders {
                proxywasm.AddHttpResponseHeader(key, value)
        }
        return types.ActionContinue
}

func (ctx *httpHeaders) OnHttpRequestHeaders(int, bool) types.Action {
        current := time.Now().UnixNano()
        // We use nanoseconds() rather than time.Second() because the proxy-wasm has the known limitation.
        // TODO(incfly): change to time.Second() once https://github.com/proxy-wasm/proxy-wasm-cpp-host/issues/199
        // is resolved and released.
        if current > ctx.pluginContext.lastRefillNanoSec+1e9 {
                ctx.pluginContext.remainToken = 2
                ctx.pluginContext.lastRefillNanoSec = current
        }
        proxywasm.LogCriticalf("Current time %v, last refill time %v, the remain token %v",
                current, ctx.pluginContext.lastRefillNanoSec, ctx.pluginContext.remainToken)
        if ctx.pluginContext.remainToken == 0 {
                if err := proxywasm.SendHttpResponse(403, [][2]string{
                        {"powered-by", "proxy-wasm-go-sdk!!"},
                }, []byte("rate limited, wait and retry."), -1); err != nil {
                        proxywasm.LogErrorf("failed to send local response: %v", err)
                        proxywasm.ResumeHttpRequest()
                }
                return types.ActionPause
        }
        ctx.pluginContext.remainToken -= 1
        return types.ActionContinue
}
```



# 资源详解

## 全局有效

wp-istio-system.yaml

kubectl apply -f wp-istio-system.yaml -n istio-system

```
apiVersion: extensions.istio.io/v1alpha1
kind: WasmPlugin
metadata:
  name: default
  namespace: istio-system
spec:
  url: oci://registry.cn-qingdao.aliyuncs.com/hxpdocker/wasm-rate-limiting:v1
  phase: STATS	
```

![1](images\1.jpg)



## 名称空间有效

wp-istio.yaml

kubectl apply -f wp-istio.yaml -n istio

```
apiVersion: extensions.istio.io/v1alpha1
kind: WasmPlugin
metadata:
  name: default
spec:
  url: oci://registry.cn-qingdao.aliyuncs.com/hxpdocker/wasm-rate-limiting:v1
  phase: STATS	
```



## 工作负载有效

wp-ingressgateway.yaml

kubectl apply -f wp-ingressgateway.yaml -n istio-system

```
apiVersion: extensions.istio.io/v1alpha1
kind: WasmPlugin
metadata:
  name: default
  namespace: istio-system
spec:
  selector:
    matchLabels:
      istio: ingressgateway
  url: oci://registry.cn-qingdao.aliyuncs.com/hxpdocker/wasm-rate-limiting:v1
  phase: STATS	
```

## url

### oci://

略

### file://

```
cd wasm-rate-limiting
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

wp-productpage-url-file.yaml

kubectl apply -f wp-productpage-url-file.yaml -n istio

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
  url: file:///var/local/lib/wasm-filters/main.wasm
  phase: STATS	
```

![2](images\2.jpg)



## sha256

```
[root@node01 wasm-rate-limiting]# sha256sum main.wasm
d3f2d1b3c0ae26100e36a6ddb901ca55732aeea79329e43a14686c1338a1ed03  main.wasm
```

### 正确：

wp-productpage-sha256-correct.yaml

kubectl apply -f wp-productpage-sha256-correct.yaml -n istio

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
  url: oci://registry.cn-qingdao.aliyuncs.com/hxpdocker/wasm-rate-limiting:v1
  phase: STATS	
  sha256: d3f2d1b3c0ae26100e36a6ddb901ca55732aeea79329e43a14686c1338a1ed03
```

![3](images\3.jpg)

### 错误：

任然有效

wp-productpage-sha256-wrong.yaml

kubectl apply -f wp-productpage-sha256-wrong.yaml -n istio

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
  url: oci://registry.cn-qingdao.aliyuncs.com/hxpdocker/wasm-rate-limiting:v1
  phase: STATS	
  sha256: d3f2d1b3c0ae26100e36a6ddb901ca55732aeea79329e43a14686c1338a1ed04
```



## imagePullPolicy

### UNSPECIFIED_POLICY

wp-productpage-imagePullPolicy-UNSPECIFIED_POLICY.yaml

kubectl apply -f wp-productpage-imagePullPolicy-UNSPECIFIED_POLICY.yaml -n istio

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
  url: oci://registry.cn-qingdao.aliyuncs.com/hxpdocker/wasm-rate-limiting:v1
  phase: STATS
  imagePullPolicy: UNSPECIFIED_POLICY
```



### IfNotPresent

wp-productpage-imagePullPolicy-IfNotPresent.yaml

kubectl apply -f wp-productpage-imagePullPolicy-IfNotPresent.yaml -n istio

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
  url: oci://registry.cn-qingdao.aliyuncs.com/hxpdocker/wasm-rate-limiting:v1
  phase: STATS
  imagePullPolicy: IfNotPresent
```



### Always

wp-productpage-imagePullPolicy-Always.yaml

kubectl apply -f wp-productpage-imagePullPolicy-Always.yaml -n istio

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
  url: oci://registry.cn-qingdao.aliyuncs.com/hxpdocker/wasm-rate-limiting:v1
  phase: STATS
  imagePullPolicy: Always
```



## imagePullSecret

```
kubectl create secret docker-registry aliyum-mark --namespace=istio --docker-username=195446040@qq.com --docker-password=hxp123456 --docker-server=registry.cn-qingdao.aliyuncs.com

```

wp-productpage-imagePullSecret.yaml

kubectl apply -f wp-productpage-imagePullSecret.yaml -n istio

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
  url: oci://registry.cn-qingdao.aliyuncs.com/hxpdocker/wasm-rate-limiting-private:v1
  phase: STATS
  imagePullPolicy: Always
  imagePullSecret: aliyum-mark
```



## pluginConfig

wasm plugin configuration内容：

![8](images\8.png)

wp-productpage-pluginConfig.yaml

kubectl apply -f wp-productpage-pluginConfig.yaml -n istio

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
  url: oci://registry.cn-qingdao.aliyuncs.com/hxpdocker/wasm-rate-limiting:v1
  phase: STATS
  pluginConfig: 
    testConfigName1: testConfigValue1
    testConfigName2: testConfigValue2
```



## pluginName

rootid

wp-productpage-pluginName.yaml

kubectl apply -f wp-productpage-pluginName.yaml -n istio

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
  url: oci://registry.cn-qingdao.aliyuncs.com/hxpdocker/wasm-rate-limiting:v1
  phase: STATS
  pluginName: testId
```



## phase

准备工作：

```
kubectl apply -f ingress-allow-all.yaml -n istio-system
kubectl apply -f ra-example.yaml -n istio-system
```

### UNSPECIFIED_PHASE

wp-ingressgateway-phase-UNSPECIFIED_PHASE.yaml

kubectl apply -f wp-ingressgateway-phase-UNSPECIFIED_PHASE.yaml -n istio-system

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
  url: oci://registry.cn-qingdao.aliyuncs.com/hxpdocker/wasm-rate-limiting:v1
  phase: UNSPECIFIED_PHASE	
```



![4](images\4.jpg)



### AUTHN

wp-ingressgateway-phase-AUTHN.yaml

kubectl apply -f wp-ingressgateway-phase-AUTHN.yaml -n istio-system

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
  url: oci://registry.cn-qingdao.aliyuncs.com/hxpdocker/wasm-rate-limiting:v1
  phase: AUTHN	
```



![5](images\5.jpg)



### AUTHZ

wp-ingressgateway-phase-AUTHZ.yaml

kubectl apply -f wp-ingressgateway-phase-AUTHZ.yaml -n istio-system

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
  url: oci://registry.cn-qingdao.aliyuncs.com/hxpdocker/wasm-rate-limiting:v1
  phase: AUTHZ
```



![6](images\6.jpg)



### STATS

wp-ingressgateway-phase-STATS.yaml

kubectl apply -f wp-ingressgateway-phase-STATS.yaml -n istio-system

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
  url: oci://registry.cn-qingdao.aliyuncs.com/hxpdocker/wasm-rate-limiting:v1
  phase: STATS
```

![7](images\7.jpg)



## priority

降序排列

wp-ingressgateway-phase-STATS-p10.yaml

kubectl apply -f wp-ingressgateway-phase-STATS-p10.yaml -n istio-system

```
apiVersion: extensions.istio.io/v1alpha1
kind: WasmPlugin
metadata:
  name: ingressgateway-p10
  namespace: istio-system
spec:
  selector:
    matchLabels:
      istio: ingressgateway
  url: oci://registry.cn-qingdao.aliyuncs.com/hxpdocker/wasm-rate-limiting:v1
  phase: STATS
  priority: 10
```

wp-ingressgateway-phase-STATS-p20.yaml

kubectl apply -f wp-ingressgateway-phase-STATS-p20.yaml -n istio-system

```
apiVersion: extensions.istio.io/v1alpha1
kind: WasmPlugin
metadata:
  name: ingressgateway-p20
  namespace: istio-system
spec:
  selector:
    matchLabels:
      istio: ingressgateway
  url: oci://registry.cn-qingdao.aliyuncs.com/hxpdocker/wasm-rate-limiting:v1
  phase: STATS
  priority: 20
```

![9](images\9.png)

