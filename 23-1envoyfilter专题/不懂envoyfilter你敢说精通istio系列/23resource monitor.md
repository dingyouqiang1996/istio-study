# 1什么是resource monitor

resource monitor用来监视envoy sidecar使用的内存资源，配置在overload manager中，当内存使用到一定量的时候，可以触发一些动作。resource monitor目前支持两种配置，分别是fixedHeap和injected_resource.injected_resource用于测试。

# 2配置

## 2.1resource monitor

envoy.resource_monitors.fixed_heap：

```
{
  "max_heap_size_bytes": "..."最大堆栈字节数
}
```

envoy.resource_monitors.injected_resource：

```
{
  "filename": "..."文件名称
}
```

## 2.2overload manager

```
{
  "refresh_interval": "{...}",刷新间隔
  "resource_monitors": [],资源监视
  "actions": [],动作
  "buffer_factory_config": "{...}"缓存工厂配置
}
```

actions：

```
{
  "name": "...",动作类型名称
  "triggers": [],触发器
  "typed_config": "{...}"typed配置
}
```

triggers：

```
{
  "name": "...",资源监视名称
  "threshold": "{...}",阈值
  "scaled": "{...}"标量值
}
```

threshold：

```
{
  "value": "..."值
}
```

scaled：

```
{
  "scaling_threshold": "...",标量阈值
  "saturation_threshold": "..."饱和阈值
}
```

支持的name：

![1](23images\1.jpg)

支持的typed_config：

 type.googleapis.com/envoy.config.overload.v3.ScaleTimersOverloadActionConfig



buffer_factory_config：

```
{
  "minimum_account_to_track_power_of_two": "..."2的n次方内存
}
```

# 3示例

```
  meshConfig:
      proxyMetadata:
        # Enable dynamic bootstrap generation
        # https://github.com/istio/istio/pull/33456
        BOOTSTRAP_XDS_AGENT: "true"
```



## 3.1resource_monitors

### 3.1.1fixed_heap

ef-rm-fixed_heap.yaml

kubectl apply -f ef-rm-fixed_heap.yaml -n istio

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: rm
spec:
  workloadSelector:
    labels:
      app: productpage
  configPatches:
  - applyTo: BOOTSTRAP
    patch:
      operation: MERGE
      value:
        overload_manager:
          actions:
          - name: envoy.overload_actions.shrink_heap
            triggers:
            - name: envoy.resource_monitors.fixed_heap
              threshold:
                value: 0.9
          - name: envoy.overload_actions.stop_accepting_requests
            triggers:
            - name: envoy.resource_monitors.fixed_heap
              threshold:
                value: 0.95
          refresh_interval: 0.25s
          resource_monitors:
          - name: envoy.resource_monitors.fixed_heap
            typed_config:
              "@type": type.googleapis.com/envoy.extensions.resource_monitors.fixed_heap.v3.FixedHeapConfig
              max_heap_size_bytes: 70741824
```



### 3.1.2injected_resource

injected_resource.txt

```
0.8
```

创建configmap

```
kubectl create configmap -n istio injected-resource --from-file=injected_resource.txt
```

执行以下命令更新productpage-v1

```
kubectl patch -n istio deployment productpage-v1 -p '{"spec":{"template":{"metadata":{"annotations":{"sidecar.istio.io/userVolume":"[{\"name\":\"injected-resource\",\"configMap\": {\"name\": \"injected-resource\"}}]","sidecar.istio.io/userVolumeMount":"[{\"mountPath\":\"/var/local/lib/injected-resource\",\"name\":\"injected-resource\"}]"}}}}}'
```

```
kubectl exec -it deployment/productpage-v1 -n istio -c istio-proxy -- ls /var/local/lib/injected-resource/
```



ef-rm-injected_resource.yaml

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: rm
spec:
  workloadSelector:
    labels:
      app: productpage
  configPatches:
  - applyTo: BOOTSTRAP
    patch:
      operation: MERGE
      value:
        overload_manager:
          actions:
          - name: envoy.overload_actions.shrink_heap
            triggers:
            - name: envoy.resource_monitors.injected_resource
              threshold:
                value: 0.9
          - name: envoy.overload_actions.stop_accepting_requests
            triggers:
            - name: envoy.resource_monitors.injected_resource
              threshold:
                value: 0.95
          refresh_interval: 0.25s
          resource_monitors:
          - name: envoy.resource_monitors.injected_resource
            typed_config:
              '@type': type.googleapis.com/envoy.extensions.resource_monitors.injected_resource.v3.InjectedResourceConfig
              filename: /var/local/lib/injected-resource/injected_resource.txt
```

failed to write updated envoy bootstrap: failed to marshal bootstrap as JSON: proto: not found

## 3.2overload_actions

### 3.2.1name

#### 3.2.1.1stop_accepting_requests

ef-rm-stop_accepting_requests.yaml

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: rm
spec:
  workloadSelector:
    labels:
      app: productpage
  configPatches:
  - applyTo: BOOTSTRAP
    patch:
      operation: MERGE
      value:
        overload_manager:
          actions:
          - name: envoy.overload_actions.stop_accepting_requests
            triggers:
            - name: envoy.resource_monitors.fixed_heap
              threshold:
                value: 0.95
          refresh_interval: 0.25s
          resource_monitors:
          - name: envoy.resource_monitors.fixed_heap
            typed_config:
              '@type': type.googleapis.com/envoy.extensions.resource_monitors.fixed_heap.v3.FixedHeapConfig
              max_heap_size_bytes: 1073741824.0
```

failed to write updated envoy bootstrap: failed to marshal bootstrap as JSON: proto: not found

#### 3.2.1.2disable_http_keepalive

ef-rm-disable_http_keepalive.yaml

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: rm
spec:
  workloadSelector:
    labels:
      app: productpage
  configPatches:
  - applyTo: BOOTSTRAP
    patch:
      operation: MERGE
      value:
        overload_manager:
          actions:
          - name: envoy.overload_actions.disable_http_keepalive
            triggers:
            - name: envoy.resource_monitors.fixed_heap
              threshold:
                value: 0.95
          refresh_interval: 0.25s
          resource_monitors:
          - name: envoy.resource_monitors.fixed_heap
            typed_config:
              '@type': type.googleapis.com/envoy.extensions.resource_monitors.fixed_heap.v3.FixedHeapConfig
              max_heap_size_bytes: 1073741824.0
```



#### 3.2.1.3stop_accepting_connections

ef-rm-stop_accepting_connections.yaml

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: rm
spec:
  workloadSelector:
    labels:
      app: productpage
  configPatches:
  - applyTo: BOOTSTRAP
    patch:
      operation: MERGE
      value:
        overload_manager:
          actions:
          - name: envoy.overload_actions.stop_accepting_connections
            triggers:
            - name: envoy.resource_monitors.fixed_heap
              threshold:
                value: 0.95
          refresh_interval: 0.25s
          resource_monitors:
          - name: envoy.resource_monitors.fixed_heap
            typed_config:
              '@type': type.googleapis.com/envoy.extensions.resource_monitors.fixed_heap.v3.FixedHeapConfig
              max_heap_size_bytes: 1073741824.0
```



#### 3.2.1.4reject_incoming_connections

ef-rm-reject_incoming_connections.yaml

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: rm
spec:
  workloadSelector:
    labels:
      app: productpage
  configPatches:
  - applyTo: BOOTSTRAP
    patch:
      operation: MERGE
      value:
        overload_manager:
          actions:
          - name: envoy.overload_actions.reject_incoming_connections
            triggers:
            - name: envoy.resource_monitors.fixed_heap
              threshold:
                value: 0.95
          refresh_interval: 0.25s
          resource_monitors:
          - name: envoy.resource_monitors.fixed_heap
            typed_config:
              '@type': type.googleapis.com/envoy.extensions.resource_monitors.fixed_heap.v3.FixedHeapConfig
              max_heap_size_bytes: 1073741824.0
```



#### 3.2.1.5shrink_heap

ef-rm-shrink_heap.yaml

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: rm
spec:
  workloadSelector:
    labels:
      app: productpage
  configPatches:
  - applyTo: BOOTSTRAP
    patch:
      operation: MERGE
      value:
        overload_manager:
          actions:
          - name: envoy.overload_actions.shrink_heap
            triggers:
            - name: envoy.resource_monitors.fixed_heap
              threshold:
                value: 0.95
          refresh_interval: 0.25s
          resource_monitors:
          - name: envoy.resource_monitors.fixed_heap
            typed_config:
              '@type': type.googleapis.com/envoy.extensions.resource_monitors.fixed_heap.v3.FixedHeapConfig
              max_heap_size_bytes: 1073741824.0
```



#### 3.2.1.6reduce_timeouts

ef-rm-reduce_timeouts.yaml

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: rm
spec:
  workloadSelector:
    labels:
      app: productpage
  configPatches:
  - applyTo: BOOTSTRAP
    patch:
      operation: MERGE
      value:
        overload_manager:
          actions:
          - name: envoy.overload_actions.reduce_timeouts
            triggers:
            - name: envoy.resource_monitors.fixed_heap
              threshold:
                value: 0.95
            typed_config:
              "@type": type.googleapis.com/envoy.config.overload.v3.ScaleTimersOverloadActionConfig
              timer_scale_factors:
                - timer: HTTP_DOWNSTREAM_CONNECTION_IDLE
                  min_timeout: 2s
          refresh_interval: 0.25s
          resource_monitors:
          - name: envoy.resource_monitors.fixed_heap
            typed_config:
              '@type': type.googleapis.com/envoy.extensions.resource_monitors.fixed_heap.v3.FixedHeapConfig
              max_heap_size_bytes: 1073741824.0
```



#### 3.2.1.7reset_high_memory_stream

ef-rm-reset_high_memory_stream.yaml

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: rm
spec:
  workloadSelector:
    labels:
      app: productpage
  configPatches:
  - applyTo: BOOTSTRAP
    patch:
      operation: MERGE
      value:
        overload_manager:
          actions:
          - name: envoy.overload_actions.reset_high_memory_stream
            triggers:
            - name: envoy.resource_monitors.fixed_heap
              threshold:
                value: 0.95
          refresh_interval: 0.25s
          resource_monitors:
          - name: envoy.resource_monitors.fixed_heap
            typed_config:
              '@type': type.googleapis.com/envoy.extensions.resource_monitors.fixed_heap.v3.FixedHeapConfig
              max_heap_size_bytes: 1073741824.0
```



### 3.2.2triggers

#### 3.2.2.1threshold

ef-rm-triggers-threshold.yaml

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: rm
spec:
  workloadSelector:
    labels:
      app: productpage
  configPatches:
  - applyTo: BOOTSTRAP
    patch:
      operation: MERGE
      value:
        overload_manager:
          actions:
          - name: envoy.overload_actions.reset_high_memory_stream
            triggers:
            - name: envoy.resource_monitors.fixed_heap
              threshold:
                value: 0.95
          refresh_interval: 0.25s
          resource_monitors:
          - name: envoy.resource_monitors.fixed_heap
            typed_config:
              '@type': type.googleapis.com/envoy.extensions.resource_monitors.fixed_heap.v3.FixedHeapConfig
              max_heap_size_bytes: 1073741824.0
```



#### 3.2.2.2scaled

ef-rm-triggers-scaled.yaml

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: rm
spec:
  workloadSelector:
    labels:
      app: productpage
  configPatches:
  - applyTo: BOOTSTRAP
    patch:
      operation: MERGE
      value:
        overload_manager:
          actions:
          - name: envoy.overload_actions.reset_high_memory_stream
            triggers:
            - name: envoy.resource_monitors.fixed_heap
              scaled:
                scaling_threshold: 0.9
                saturation_threshold: 0.95
          refresh_interval: 0.25s
          resource_monitors:
          - name: envoy.resource_monitors.fixed_heap
            typed_config:
              '@type': type.googleapis.com/envoy.extensions.resource_monitors.fixed_heap.v3.FixedHeapConfig
              max_heap_size_bytes: 1073741824
```



## 3.3buffer_factory_config

ef-rm-buffer_factory_config.yaml

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: rm
spec:
  workloadSelector:
    labels:
      app: productpage
  configPatches:
  - applyTo: BOOTSTRAP
    patch:
      operation: MERGE
      value:
        overload_manager:
          buffer_factory_config:
            minimum_account_to_track_power_of_two: 20
          actions:
          - name: envoy.overload_actions.reset_high_memory_stream
            triggers:
            - name: envoy.resource_monitors.fixed_heap
              threshold:
                value: 0.95
          refresh_interval: 0.25s
          resource_monitors:
          - name: envoy.resource_monitors.fixed_heap
            typed_config:
              '@type': type.googleapis.com/envoy.extensions.resource_monitors.fixed_heap.v3.FixedHeapConfig
              max_heap_size_bytes: 1073741824.0
```



## 3.4refresh_interval

ef-rm-refresh_interval.yaml

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: rm
spec:
  workloadSelector:
    labels:
      app: productpage
  configPatches:
  - applyTo: BOOTSTRAP
    patch:
      operation: MERGE
      value:
        overload_manager:
          actions:
          - name: envoy.overload_actions.reset_high_memory_stream
            triggers:
            - name: envoy.resource_monitors.fixed_heap
              threshold:
                value: 0.95
          refresh_interval: 0.25s
          resource_monitors:
          - name: envoy.resource_monitors.fixed_heap
            typed_config:
              '@type': type.googleapis.com/envoy.extensions.resource_monitors.fixed_heap.v3.FixedHeapConfig
              max_heap_size_bytes: 1073741824.0
```

