# 什么是proxyConfig

`ProxyConfig` exposes proxy level configuration options. `ProxyConfig` can be configured on a per-workload basis, a per-namespace basis, or mesh-wide. `ProxyConfig` is not a required resource; there are default values in place, which are documented inline with each field.

**NOTE**: fields in ProxyConfig are not dynamically configured - changes will require restart of workloads to take effect.

For any namespace, including the root configuration namespace, it is only valid to have a single workload selector-less `ProxyConfig` resource.

For resources with a workload selector, it is only valid to have one resource selecting any given workload.



| Field                  | Type                  | Description                                                  | Required |
| ---------------------- | --------------------- | ------------------------------------------------------------ | -------- |
| `selector`             | `WorkloadSelector`    | Optional. Selectors specify the set of pods/VMs on which this `ProxyConfig` resource should be applied. If not set, the `ProxyConfig` resource will be applied to all workloads in the namespace where this resource is defined. | No       |
| `concurrency`          | `Int32Value`          | The number of worker threads to run. If unset, defaults to 2. If set to 0, this will be configured to use all cores on the machine using CPU requests and limits to choose a value, with limits taking precedence over requests. | No       |
| `environmentVariables` | `map<string, string>` | Additional environment variables for the proxy. Names starting with `ISTIO_META_` will be included in the generated bootstrap configuration and sent to the XDS server. | No       |
| `image`                | `ProxyImage`          | Specifies the details of the proxy image.                    | No       |

# 全局有效

需要重启pod

https://github.com/istio/istio/pull/38964

pc-default-istio-system.yaml

kubectl apply -f pc-default-istio-system.yaml -n istio-system

```
apiVersion: networking.istio.io/v1beta1
kind: ProxyConfig
metadata:
  name: default
  namespace: istio-system
spec:
  concurrency: 0
  image:
    imageType: distroless

```



# 名称空间有效

pc-default-istio.yaml

kubectl apply -f pc-default-istio.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: ProxyConfig
metadata:
  name: default
spec:
  concurrency: 0
  image:
    imageType: distroless
```



# 工作负载有效

pc-productpage.yaml

kubectl apply -f pc-productpage.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: ProxyConfig
metadata:
  name: productpage
spec:
  selector:
    matchLabels:
      app: productpage
  concurrency: 0
  image:
    imageType: debug
```



# 资源详解

## concurrency

### 按cpu核数自动选择

```
apiVersion: networking.istio.io/v1beta1
kind: ProxyConfig
metadata:
  name: productpage
spec:
  selector:
    matchLabels:
      app: productpage
  concurrency: 0
  image:
    imageType: distroless
```

### 配置固定值，默认是2个

pc-productpage-concurrency.yaml

kubectl apply -f pc-productpage-concurrency.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: ProxyConfig
metadata:
  name: productpage
spec:
  selector:
    matchLabels:
      app: productpage
  concurrency: 3
  image:
    imageType: distroless
```



## environmentVariables

### special

pc-productpage-environmentVariables-special.yaml

kubectl apply -f pc-productpage-environmentVariables-special.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: ProxyConfig
metadata:
  name: productpage
spec:
  selector:
    matchLabels:
      app: productpage
  concurrency: 0
  environmentVariables:
    ISTIO_META_Test: test1
  image:
    imageType: distroless
```

### normal

pc-productpage-environmentVariables-normal.yaml

kubectl apply -f pc-productpage-environmentVariables-normal.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: ProxyConfig
metadata:
  name: productpage
spec:
  selector:
    matchLabels:
      app: productpage
  concurrency: 0
  environmentVariables:
    test: test2
  image:
    imageType: distroless
```

telemetry-productpage.yaml

kubectl apply -f telemetry-productpage.yaml -n istio

```
apiVersion: telemetry.istio.io/v1alpha1
kind: Telemetry
metadata:
  name: telemetry-productpage
spec:
  selector:
    matchLabels:
      app: productpage
  tracing:
  - providers:
    - name: "zipkin"
    customTags:
      test:
        environment:
          name: test
          defaultValue: test
```



## image

###  default

pc-productpage-image-default.yaml

kubectl apply -f pc-productpage-image-default.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: ProxyConfig
metadata:
  name: productpage
spec:
  selector:
    matchLabels:
      app: productpage
  concurrency: 0
  image:
    imageType: default
```



### debug

pc-productpage-image-debug.yaml

kubectl apply -f pc-productpage-image-debug.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: ProxyConfig
metadata:
  name: productpage
spec:
  selector:
    matchLabels:
      app: productpage
  concurrency: 0
  image:
    imageType: debug
```



### distroless

略
