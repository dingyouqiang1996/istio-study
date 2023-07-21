# 什么是telemetry crd

elemetry defines how the telemetry is generated for workloads within a mesh.

For mesh level configuration, put the resource in root configuration namespace for your Istio installation *without* a workload selector.

For any namespace, including the root configuration namespace, it is only valid to have a single workload selector-less Telemetry resource.

For resources with a workload selector, it is only valid to have one resource selecting any given workload.

The hierarchy of Telemetry configuration is as follows:

1. Workload-specific configuration
2. Namespace-specific configuration
3. Root namespace configuration

# 资源详解

## selector

### 没有selector istio-system

注意需要安装zipkin，不然tracing会消失

需要添加zipkin extensionProviders

```
[root@node01 tracing]# kubectl get cm istio -n istio-system -o yaml
apiVersion: v1
data:
  mesh: |-
    accessLogFile: /dev/stdout
    enableTracing: true
    defaultConfig:
      discoveryAddress: istiod.istio-system.svc:15012
      proxyMetadata: {}
      tracing:
        zipkin:
          address: zipkin.istio-system:9411
    enablePrometheusMerge: true
    extensionProviders:
    - name: zipkin
      zipkin:
        service: zipkin.istio-system.svc.cluster.local
        port: 9411
    - envoyOtelAls:
        port: 4317
        service: otel-collector.istio-system.svc.cluster.local
      name: otel
    rootNamespace: istio-system
    trustDomain: cluster.local
  meshNetworks: 'networks: {}'
```

telemetry-istio-system.yaml

kubectl apply -f telemetry-istio-system.yaml -n istio-system

```
apiVersion: telemetry.istio.io/v1alpha1
kind: Telemetry
metadata:
  name: default
  namespace: istio-system
spec:
  tracing:
  - providers:
    - name: "zipkin"
    randomSamplingPercentage: 10.00

```



### 没有selector 名称空间

telemetry-istio.yaml

kubectl apply -f telemetry-istio.yaml -n istio

```
apiVersion: telemetry.istio.io/v1alpha1
kind: Telemetry
metadata:
  name: default
spec:
  tracing:
  - providers:
    - name: "zipkin"
    randomSamplingPercentage: 10.00

```



### 有selector

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
    randomSamplingPercentage: 10.00

```

provider:

| Provider Name | Functionality                    |
| ------------- | -------------------------------- |
| `prometheus`  | Metrics                          |
| `stackdriver` | Metrics, Tracing, Access Logging |
| `envoy`       | Access Logging                   |

```
data:
  mesh: |-
      extensionProviders: 
      - name: "localtrace"
        zipkin:
          service: "zipkin.istio-system.svc.cluster.local"
          port: 9411
          maxTagLength: 56
      - name: "cloudtrace"
        stackdriver:
          maxTagLength: 256
      - skywalking:
          service: skywalking-oap.istio-system.svc.cluster.local
          port: 11800
        name: envoy.tracers.skywalking
```

·	

## accessLogging

| Field       | Type            | Description                                                  | Required |
| ----------- | --------------- | ------------------------------------------------------------ | -------- |
| `providers` | `ProviderRef[]` | Optional. Name of providers to which this configuration should apply. If a provider is not specified, the *default logging provider* will be used. | No       |
| `disabled`  | `BoolValue`     | Controls logging. If set to true, no access logs will be generated for impacted workloads (for the specified providers). NOTE: currently default behavior will be controlled by the provider(s) selected above. Customization controls will be added to this API in future releases. | No       |
| `filter`    | `Filter`        | Optional. If specified, this filter will be used to select specific requests/connections for logging. | No       |

filter:

| Field        | Type     | Description                                                  | Required |
| ------------ | -------- | ------------------------------------------------------------ | -------- |
| `expression` | `string` | CEL expression for selecting when requests/connections should be logged.Examples:`response.code >= 400``connection.mtls && request.url_path.contains('v1beta3')` | No       |

### providers

telemetry-accessLogging-providers.yaml

kubectl apply -f telemetry-accessLogging-providers.yaml -n istio

```
apiVersion: telemetry.istio.io/v1alpha1
kind: Telemetry
metadata:
  name: telemetry-productpage
spec:
  selector:
    matchLabels:
      app: productpage
  accessLogging:
  - providers:
    - name: envoy
```



### disabled

telemetry-accessLogging-disabled.yaml

kubectl apply -f telemetry-accessLogging-disabled.yaml -n istio

```
apiVersion: telemetry.istio.io/v1alpha1
kind: Telemetry
metadata:
  name: telemetry-productpage
spec:
  selector:
    matchLabels:
      app: productpage
  accessLogging:
  - providers:
    - name: envoy
    disabled: true
```



### filter

telemetry-accessLogging-filter.yaml

kubectl apply -f telemetry-accessLogging-filter.yaml -n istio

```
apiVersion: telemetry.istio.io/v1alpha1
kind: Telemetry
metadata:
  name: telemetry-productpage
spec:
  selector:
    matchLabels:
      app: productpage
  accessLogging:
  - providers:
    - name: envoy
    filter: 
      expression: response.code >= 400
```



![1](images\1.jpg)

## metrics

| Field       | Type                 | Description                                                  | Required |
| ----------- | -------------------- | ------------------------------------------------------------ | -------- |
| `providers` | `ProviderRef[]`      | Optional. Name of providers to which this configuration should apply. If a provider is not specified, the *default metrics provider* will be used. | No       |
| `overrides` | `MetricsOverrides[]` | Optional. Ordered list of overrides to metrics generation behavior.Specified overrides will be applied in order. They will be applied on top of inherited overrides from other resources in the hierarchy in the following order: 1. Mesh-scoped overrides 2. Namespace-scoped overrides 3. Workload-scoped overridesBecause overrides are applied in order, users are advised to order their overrides from least specific to most specific matches. That is, it is a best practice to list any universal overrides first, with tailored overrides following them. | No       |

overrides:

| Field          | Type                       | Description                                                  | Required |
| -------------- | -------------------------- | ------------------------------------------------------------ | -------- |
| `match`        | `MetricSelector`           | Match allows provides the scope of the override. It can be used to select individual metrics, as well as the workload modes (server and/or client) in which the metrics will be generated.If match is not specified, the overrides will apply to *all* metrics for *both* modes of operation (client and server). | No       |
| `disabled`     | `BoolValue`                | Optional. Must explicitly set this to “true” to turn off metrics reporting for the listed metrics. If disabled has been set to “true” in a parent configuration, it must explicitly be set to “false” to turn metrics reporting on in the workloads selected by the Telemetry resource. | No       |
| `tagOverrides` | `map<string, TagOverride>` | Optional. Collection of tag names and tag expressions to override in the selected metric(s). The key in the map is the name of the tag. The value in the map is the operation to perform on the the tag. WARNING: some providers may not support adding/removing tags. See also: https://istio.io/latest/docs/reference/config/metrics/#labels | No       |

metricSelector:

| Field          | Type                  | Description                                                  | Required |
| -------------- | --------------------- | ------------------------------------------------------------ | -------- |
| `metric`       | `IstioMetric (oneof)` | One of the well-known Istio Standard Metrics.                | No       |
| `customMetric` | `string (oneof)`      | Allows free-form specification of a metric. No validation of custom metrics is provided. | No       |
| `mode`         | `WorkloadMode`        | Controls which mode of metrics generation is selected: CLIENT and/or SERVER. | No       |

IstioMetric :

| Name                     | Description                                                  |
| ------------------------ | ------------------------------------------------------------ |
| `ALL_METRICS`            | Use of this enum indicates that the override should apply to all Istio default metrics. |
| `REQUEST_COUNT`          | Counter of requests to/from an application, generated for HTTP, HTTP/2, and GRPC traffic.The Prometheus provider exports this metric as: `istio_requests_total`.The Stackdriver provider exports this metric as:`istio.io/service/server/request_count` (SERVER mode)`istio.io/service/client/request_count` (CLIENT mode) |
| `REQUEST_DURATION`       | Histogram of request durations, generated for HTTP, HTTP/2, and GRPC traffic.The Prometheus provider exports this metric as: `istio_request_duration_milliseconds`.The Stackdriver provider exports this metric as:`istio.io/service/server/response_latencies` (SERVER mode)`istio.io/service/client/roundtrip_latencies` (CLIENT mode) |
| `REQUEST_SIZE`           | Histogram of request body sizes, generated for HTTP, HTTP/2, and GRPC traffic.The Prometheus provider exports this metric as: `istio_request_bytes`.The Stackdriver provider exports this metric as:`istio.io/service/server/request_bytes` (SERVER mode)`istio.io/service/client/request_bytes` (CLIENT mode) |
| `RESPONSE_SIZE`          | Histogram of response body sizes, generated for HTTP, HTTP/2, and GRPC traffic.The Prometheus provider exports this metric as: `istio_response_bytes`.The Stackdriver provider exports this metric as:`istio.io/service/server/response_bytes` (SERVER mode)`istio.io/service/client/response_bytes` (CLIENT mode) |
| `TCP_OPENED_CONNECTIONS` | Counter of TCP connections opened over lifetime of workload.The Prometheus provider exports this metric as: `istio_tcp_connections_opened_total`.The Stackdriver provider exports this metric as:`istio.io/service/server/connection_open_count` (SERVER mode)`istio.io/service/client/connection_open_count` (CLIENT mode) |
| `TCP_CLOSED_CONNECTIONS` | Counter of TCP connections closed over lifetime of workload.The Prometheus provider exports this metric as: `istio_tcp_connections_closed_total`.The Stackdriver provider exports this metric as:`istio.io/service/server/connection_close_count` (SERVER mode)`istio.io/service/client/connection_close_count` (CLIENT mode) |
| `TCP_SENT_BYTES`         | Counter of bytes sent during a response over a TCP connection.The Prometheus provider exports this metric as: `istio_tcp_sent_bytes_total`.The Stackdriver provider exports this metric as:`istio.io/service/server/sent_bytes_count` (SERVER mode)`istio.io/service/client/sent_bytes_count` (CLIENT mode) |
| `TCP_RECEIVED_BYTES`     | Counter of bytes received during a request over a TCP connection.The Prometheus provider exports this metric as: `istio_tcp_received_bytes_total`.The Stackdriver provider exports this metric as:`istio.io/service/server/received_bytes_count` (SERVER mode)`istio.io/service/client/received_bytes_count` (CLIENT mode) |
| `GRPC_REQUEST_MESSAGES`  | Counter incremented for every gRPC messages sent from a client.The Prometheus provider exports this metric as: `istio_request_messages_total` |
| `GRPC_RESPONSE_MESSAGES` | Counter incremented for every gRPC messages sent from a server.The Prometheus provider exports this metric as: `istio_response_messages_total` |

mode:

| Name                | Description                                                  |
| ------------------- | ------------------------------------------------------------ |
| `CLIENT_AND_SERVER` | Selects for scenarios when the workload is either the source or destination of the network traffic. |
| `CLIENT`            | Selects for scenarios when the workload is the source of the network traffic. |
| `SERVER`            | Selects for scenarios when the workload is the destination of the network traffic. |

tagOverride

| Field       | Type        | Description                                                  | Required |
| ----------- | ----------- | ------------------------------------------------------------ | -------- |
| `operation` | `Operation` | Operation controls whether or not to update/add a tag, or to remove it. | No       |
| `value`     | `string`    | Value is only considered if the operation is `UPSERT`. Values are [CEL expressions](https://opensource.google/projects/cel) over attributes. Examples include: “string(destination.port)” and “request.host”. Istio exposes all standard [Envoy attributes](https://www.envoyproxy.io/docs/envoy/latest/intro/arch_overview/advanced/attributes). Additionally, Istio exposes node metadata as attributes. More information is provided in the [customization docs](https://istio.io/latest/docs/tasks/observability/metrics/customize-metrics/#use-expressions-for-values). | No       |

operation:

| Name     | Description                                                  |
| -------- | ------------------------------------------------------------ |
| `UPSERT` | Insert or Update the tag with the provided value expression. The `value` field MUST be specified if UPSERT is used as the operation. |
| `REMOVE` | Specifies that the tag should not be included in the metric when generated. |

### providers

telemetry-metrics-providers.yaml

kubectl apply -f telemetry-metrics-providers.yaml -n istio

```
apiVersion: telemetry.istio.io/v1alpha1
kind: Telemetry
metadata:
  name: telemetry-productpage
spec:
  selector:
    matchLabels:
      app: productpage
  metrics:
  - providers:
    - name: prometheus
```

### overrides

#### match

##### metric

telemetry-metrics-overrides-match-metric.yaml

kubectl apply -f telemetry-metrics-overrides-match-metric.yaml -n istio

```
apiVersion: telemetry.istio.io/v1alpha1
kind: Telemetry
metadata:
  name: telemetry-productpage
spec:
  selector:
    matchLabels:
      app: productpage
  metrics:
  - providers:
    - name: prometheus
    overrides:
    - match:
        metric: ALL_METRICS
      tagOverrides:
        source_version: 
          operation: UPSERT
          value: "request.method"

```

value必须是个变量

![3](images\3.jpg)





##### customMetric

telemetry-metrics-overrides-match-customMetric.yaml

kubectl apply -f telemetry-metrics-overrides-match-customMetric.yaml -n istio

```
apiVersion: telemetry.istio.io/v1alpha1
kind: Telemetry
metadata:
  name: telemetry-productpage
spec:
  selector:
    matchLabels:
      app: productpage
  metrics:
  - providers:
    - name: prometheus
    overrides:
    - match:
        customMetric: requests_total
      tagOverrides:
        source_version: 
          operation: UPSERT
          value: "request.method"
```

测试：

连pod ip

curl 172.20.1.198:15020/stats/prometheus|grep source_version=\"GET\"



![2](images\2.png)

##### mode

###### CLIENT_AND_SERVER

telemetry-metrics-overrides-match-mode-CLIENT_AND_SERVER.yaml

kubectl apply -f telemetry-metrics-overrides-match-mode-CLIENT_AND_SERVER.yaml -n istio

```
apiVersion: telemetry.istio.io/v1alpha1
kind: Telemetry
metadata:
  name: telemetry-productpage
spec:
  selector:
    matchLabels:
      app: productpage
  metrics:
  - providers:
    - name: prometheus
    overrides:
    - match:
        customMetric: requests_total
        mode: CLIENT_AND_SERVER
      tagOverrides:
        source_version: 
          operation: UPSERT
          value: "request.method"
```



![4](images\4.jpg)



###### CLIENT

telemetry-metrics-overrides-match-mode-CLIENT.yaml

kubectl apply -f telemetry-metrics-overrides-match-mode-CLIENT.yaml -n istio

```
apiVersion: telemetry.istio.io/v1alpha1
kind: Telemetry
metadata:
  name: telemetry-productpage
spec:
  selector:
    matchLabels:
      app: productpage
  metrics:
  - providers:
    - name: prometheus
    overrides:
    - match:
        customMetric: requests_total
        mode: CLIENT
      tagOverrides:
        source_version: 
          operation: UPSERT
          value: "request.method"
```

![4](images\4.jpg)

- **traffic_direction**: "OUTBOUND",

###### SERVER

telemetry-metrics-overrides-match-mode-SERVER.yaml

kubectl apply -f telemetry-metrics-overrides-match-mode-SERVER.yaml -n istio

```
apiVersion: telemetry.istio.io/v1alpha1
kind: Telemetry
metadata:
  name: telemetry-productpage
spec:
  selector:
    matchLabels:
      app: productpage
  metrics:
  - providers:
    - name: prometheus
    overrides:
    - match:
        customMetric: requests_total
        mode: SERVER
      tagOverrides:
        source_version: 
          operation: UPSERT
          value: "request.method"
```

- **traffic_direction**: "OUTBOUND",

![5](images\5.png)



#### disabled

telemetry-metrics-overrides-disabled.yaml

kubectl apply -f telemetry-metrics-overrides-disabled.yaml -n istio

```
apiVersion: telemetry.istio.io/v1alpha1
kind: Telemetry
metadata:
  name: telemetry-productpage
spec:
  metrics:
  - providers:
    - name: prometheus
    overrides:
    - disabled: true
```



#### tagOverrides

##### operation

###### UPSERT

telemetry-metrics-overrides-tagOverrides-operation-UPSERT.yaml

kubectl apply -f telemetry-metrics-overrides-tagOverrides-operation-UPSERT-yaml -n istio

```
apiVersion: telemetry.istio.io/v1alpha1
kind: Telemetry
metadata:
  name: telemetry-productpage
spec:
  selector:
    matchLabels:
      app: productpage
  metrics:
  - providers:
    - name: prometheus
    overrides:
    - match:
        customMetric: requests_total
      tagOverrides:
        source_version: 
          operation: UPSERT
          value: "request.method"
```



###### REMOVE

telemetry-metrics-overrides-tagOverrides-operation-REMOVE.yaml

kubectl apply -f telemetry-metrics-overrides-tagOverrides-operation-REMOVE.yaml -n istio

```
apiVersion: telemetry.istio.io/v1alpha1
kind: Telemetry
metadata:
  name: telemetry-productpage
spec:
  selector:
    matchLabels:
      app: productpage
  metrics:
  - providers:
    - name: prometheus
    overrides:
    - match:
        customMetric: requests_total
      tagOverrides:
        source_version: 
          operation: REMOVE
```



![6](images\6.jpg)



## tracing

| Field                      | Type                     | Description                                                  | Required |
| -------------------------- | ------------------------ | ------------------------------------------------------------ | -------- |
| `providers`                | `ProviderRef[]`          | Optional. Name of provider(s) to use for span reporting. If a provider is not specified, the *default tracing provider* will be used. NOTE: At the moment, only a single provider can be specified in a given Tracing rule. | No       |
| `randomSamplingPercentage` | `DoubleValue`            | Controls the rate at which traffic will be selected for tracing if no prior sampling decision has been made. If a prior sampling decision has been made, that decision will be respected. However, if no sampling decision has been made (example: no `x-b3-sampled` tracing header was present in the requests), the traffic will be selected for telemetry generation at the percentage specified.Defaults to 0%. Valid values [0.00-100.00]. Can be specified in 0.01% increments. | No       |
| `disableSpanReporting`     | `BoolValue`              | Controls span reporting. If set to true, no spans will be reported for impacted workloads. This does NOT impact context propagation or trace sampling behavior. | No       |
| `customTags`               | `map<string, CustomTag>` | Optional. Configures additional custom tags to the generated trace spans. | No       |

customTag:

| Field         | Type                    | Description                                                  | Required |
| ------------- | ----------------------- | ------------------------------------------------------------ | -------- |
| `literal`     | `Literal (oneof)`       | Literal adds the same, hard-coded value to each span.        | No       |
| `environment` | `Environment (oneof)`   | Environment adds the value of an environment variable to each span. | No       |
| `header`      | `RequestHeader (oneof)` | RequestHeader adds the value of an header from the request to each span. | No       |

### providers

telemetry-tracing-providers.yaml

kubectl apply -f telemetry-tracing-providers.yaml -n istio

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
    randomSamplingPercentage: 10.00

```



### randomSamplingPercentage

略

### disableSpanReporting

telemetry-tracing-disableSpanReporting.yaml

kubectl apply -f telemetry-tracing-disableSpanReporting.yaml -n istio

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
    disableSpanReporting: true
```

tracing消失

### customTags

#### literal

telemetry-tracing-customTags-literal.yaml

kubectl apply -f telemetry-tracing-customTags-literal.yaml -n istio

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
        literal:
          value: test
```

![7](images\7.png)





![8](images\8.jpg)

#### environment

telemetry-tracing-customTags-environment.yaml

kubectl apply -f telemetry-tracing-customTags-environment.yaml -n istio

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
          name: myenv
          defaultValue: test
```

![9](images\9.jpg)



#### header

telemetry-tracing-customTags-header.yaml

kubectl apply -f telemetry-tracing-customTags-header.yaml -n istio

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
        header:
          name: ":path"
          defaultValue: test
```

![10](images\10.jpg)



![11](images\11.jpg)
