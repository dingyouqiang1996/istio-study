# 1什么是statistics

statiics就是统计信息，实际和metrics是一个东西。

# 2statistics分类

## 2.1按来源分

1:**Downstream**

进入的连接和请求，由listener，http connection manager ，tcp proxy filter等发出

2:**Upstream**

发出的连接和请求，由connection pool，route filter，tcp proxy filter等发出

3:**Server**

用于描述envoy sidecar 工作状态，比如服务工作时间，内存使用量

## 2.2按类型分

1:**Counters**

无符号整型，只会增加不会减少，比如请求总数

2:**Gauges**

无符号整型，会增加和会减少，比如当前活跃请求数

3:**Histograms**

无符号整型，是一串值，用于计算百分比，比如上游请求时间

# 3statistics案例

## 3.0准备工作

添加annotations

https://github.com/istio/istio/issues/38164

deploy-with-annotions.yaml

```
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: details-v1
  labels:
    app: details
    version: v1
spec:
  replicas: 1
  selector:
    matchLabels:
      app: details
      version: v1
  template:
    metadata:
      labels:
        app: details
        version: v1
      annotations:
        sidecar.istio.io/statsInclusionRegexps: ".*"
    spec:
      serviceAccountName: bookinfo-details
      containers:
      - name: details
        image: docker.io/istio/examples-bookinfo-details-v1:1.16.2
        imagePullPolicy: IfNotPresent
        ports:
        - containerPort: 9080
        securityContext:
          runAsUser: 1000
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: ratings-v1
  labels:
    app: ratings
    version: v1
spec:
  replicas: 1
  selector:
    matchLabels:
      app: ratings
      version: v1
  template:
    metadata:
      labels:
        app: ratings
        version: v1
      annotations:
        sidecar.istio.io/statsInclusionRegexps: ".*"
    spec:
      serviceAccountName: bookinfo-ratings
      containers:
      - name: ratings
        image: docker.io/istio/examples-bookinfo-ratings-v1:1.16.2
        imagePullPolicy: IfNotPresent
        ports:
        - containerPort: 9080
        securityContext:
          runAsUser: 1000
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: reviews-v1
  labels:
    app: reviews
    version: v1
spec:
  replicas: 1
  selector:
    matchLabels:
      app: reviews
      version: v1
  template:
    metadata:
      labels:
        app: reviews
        version: v1
      annotations:
        sidecar.istio.io/statsInclusionRegexps: ".*"
    spec:
      serviceAccountName: bookinfo-reviews
      containers:
      - name: reviews
        image: docker.io/istio/examples-bookinfo-reviews-v1:1.16.2
        imagePullPolicy: IfNotPresent
        env:
        - name: LOG_DIR
          value: "/tmp/logs"
        ports:
        - containerPort: 9080
        volumeMounts:
        - name: tmp
          mountPath: /tmp
        - name: wlp-output
          mountPath: /opt/ibm/wlp/output
        securityContext:
          runAsUser: 1000
      volumes:
      - name: wlp-output
        emptyDir: {}
      - name: tmp
        emptyDir: {}
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: reviews-v2
  labels:
    app: reviews
    version: v2
spec:
  replicas: 1
  selector:
    matchLabels:
      app: reviews
      version: v2
  template:
    metadata:
      labels:
        app: reviews
        version: v2
      annotations:
        sidecar.istio.io/statsInclusionRegexps: ".*"
    spec:
      serviceAccountName: bookinfo-reviews
      containers:
      - name: reviews
        image: docker.io/istio/examples-bookinfo-reviews-v2:1.16.2
        imagePullPolicy: IfNotPresent
        env:
        - name: LOG_DIR
          value: "/tmp/logs"
        ports:
        - containerPort: 9080
        volumeMounts:
        - name: tmp
          mountPath: /tmp
        - name: wlp-output
          mountPath: /opt/ibm/wlp/output
        securityContext:
          runAsUser: 1000
      volumes:
      - name: wlp-output
        emptyDir: {}
      - name: tmp
        emptyDir: {}
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: reviews-v3
  labels:
    app: reviews
    version: v3
spec:
  replicas: 1
  selector:
    matchLabels:
      app: reviews
      version: v3
  template:
    metadata:
      labels:
        app: reviews
        version: v3
      annotations:
        sidecar.istio.io/statsInclusionRegexps: ".*"
    spec:
      serviceAccountName: bookinfo-reviews
      containers:
      - name: reviews
        image: docker.io/istio/examples-bookinfo-reviews-v3:1.16.2
        imagePullPolicy: IfNotPresent
        env:
        - name: LOG_DIR
          value: "/tmp/logs"
        ports:
        - containerPort: 9080
        volumeMounts:
        - name: tmp
          mountPath: /tmp
        - name: wlp-output
          mountPath: /opt/ibm/wlp/output
        securityContext:
          runAsUser: 1000
      volumes:
      - name: wlp-output
        emptyDir: {}
      - name: tmp
        emptyDir: {}
---
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
        sidecar.istio.io/statsInclusionRegexps: ".*"
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



## 3.1配置stats_sinks

这里我们使用statd作为案例：

3.1.1部署statsd

deploy-statsd-influxdb-grafana.yaml

```
apiVersion: apps/v1
kind: Deployment
metadata:
  name: statsd-influxdb-grafana
spec:
  replicas: 1
  selector:
    matchLabels:
      app: statsd-influxdb-grafana
  template:
    metadata:
      labels:
        app: statsd-influxdb-grafana
    spec:
      containers:
      - name: statsd-influxdb-grafana
        image: samuelebistoletti/docker-statsd-influxdb-grafana:2.2.0
        ports:
        - name: grafana
          containerPort: 3003
        - name: influxdb-admin
          containerPort: 8888
        - name: influxdb
          containerPort: 8086
        - name: statsd
          containerPort: 8125
          protocol: UDP
---
apiVersion: v1
kind: Service
metadata:
  name: statsd-influxdb-grafana-svc
spec:
  ports:
  - name: http-grafana
    port: 3003
    targetPort: 3003
  - name: http-influxdb-admin
    port: 3004
    targetPort: 8888
  - name: tcp-influxdb
    port: 8086
    targetPort: 8086
  - name: udp-statsd
    port: 8125
    targetPort: 8125
    protocol: UDP
  selector:
    app: statsd-influxdb-grafana
```

gw,vs

gw-vs-statsd.yaml

```
apiVersion: networking.istio.io/v1beta1
kind: Gateway
metadata:
  name: statsd
spec:
  selector:
    istio: ingressgateway
  servers:
  - port:
      number: 80
      name: http
      protocol: HTTP
    hosts:
    - "demo.grafana.com"
    - "demo.influxdb.com"
---
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: statsd-grafana
spec:
  hosts:
    - "demo.grafana.com"
  gateways:
  - statsd
  http:
  - match:
    - uri:
        prefix: /
    route:
    - destination:
        host: statsd-influxdb-grafana-svc.istio.svc.cluster.local
        port:
          number: 3003
---
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: statsd-influxdb
spec:
  hosts:
    - "demo.influxdb.com"
  gateways:
  - statsd
  http:
  - match:
    - uri:
        prefix: /
    route:
    - destination:
        host: statsd-influxdb-grafana-svc.istio.svc.cluster.local
        port:
          number: 3004

```



效果：

![1](17statistics\1.jpg)



![2](17statistics\2.jpg)



grafana

账号密码：root/root



Add data source on Grafana

```
Url: http://localhost:8086
Database: telegraf
User: telegraf
Password: telegraf
```

influxdb portal

URL: [http://localhost:3004](http://localhost:3004/)
Username: root
Password: root
Port: 8086

```
  meshConfig:
      proxyMetadata:
        # Enable dynamic bootstrap generation
        # https://github.com/istio/istio/pull/33456
        BOOTSTRAP_XDS_AGENT: "true"
```

ef-statictis.yaml

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: statictis
spec:
  configPatches:
  - applyTo: BOOTSTRAP
    patch:
      operation: MERGE
      value:
          stats_sinks:
          - name: envoy.stat_sinks.statsd
            typed_config:
              "@type": type.googleapis.com/envoy.config.metrics.v3.StatsdSink
              address: 
                socket_address:
                  address: 10.68.19.208
                  port_value: 8125
                  protocol: UDP
              prefix: statictis
```



## 3.2具体案例

### 3.2.1Rate limit

#### 3.2.1.2envoy.filters.http.local_ratelimit

envoyfilter-local-rate-limit.yaml

kubectl apply -f envoyfilter-local-rate-limit.yaml -n istio

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: filter-local-ratelimit-svc
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
```







![3](17statistics\3.jpg)

![6](17statistics\6.png)



### 3.2.2admission control filter

ef-admission_control.yaml

kubectlt apply -f ef-admission_control.yaml -n istio

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: admission
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
          filter:
            name: "envoy.filters.network.http_connection_manager"
            subFilter:
              name: "envoy.filters.http.router"
    patch:
      operation: INSERT_BEFORE
      value: 
        name: envoy.filters.http.admission_control
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.filters.http.admission_control.v3.AdmissionControl
          enabled:
            default_value: true
            runtime_key: "admission_control.enabled"
          sampling_window: 120s
          sr_threshold:
            default_value:
              value: 95.0
            runtime_key: "admission_control.sr_threshold"
          aggression:
            default_value: 1.5
            runtime_key: "admission_control.aggression"
          rps_threshold:
            default_value: 5
            runtime_key: "admission_control.rps_threshold"
          max_rejection_probability:
            default_value: 
              value: 80.0
            runtime_key: "admission_control.max_rejection_probability"
          success_criteria:
            http_criteria:
              http_success_status:
                - start: 100
                  end:   400
                - start: 404
                  end:   404
            grpc_criteria:
              grpc_success_status:
                - 0
                - 1
  - applyTo: HTTP_FILTER
    match:
      context: SIDECAR_INBOUND
      listener:
        portNumber: 9080
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
                    '@type': type.googleapis.com/envoy.extensions.filters.http.fault.v3.HTTPFault
                    abort:
                      httpStatus: 500
                      percentage:
                        denominator: MILLION
                        numerator: 100000
```

测试

```
[root@node01 ~]# go-stress-testing -c 1000 -n 100000 -u http://192.168.229.128:30494/productpage

 开始启动  并发数:1000 请求数:100000 请求参数: 
request:
 form:http 
 url:http://192.168.229.128:30494/productpage 
 method:GET 
 headers:map[] 
 data: 
 verify:statusCode 
 timeout:30s 
 debug:false 



─────┬───────┬───────┬───────┬────────┬────────┬────────┬────────┬────────┬────────┬────────
 耗时│ 并发数│ 成功数│ 失败数│   qps  │最长耗时│最短耗时│平均耗时│下载字节│字节每秒│ 错误码
─────┼───────┼───────┼───────┼────────┼────────┼────────┼────────┼────────┼────────┼────────
   1s│     17│      0│     17│    0.00│  977.83│  577.50│    0.00│     306│     305│500:17
   2s│    321│      0│    322│    0.00│ 1998.97│  378.81│    0.00│     990│     493│500:55;503:267
   3s│    524│      2│    739│    1.70│ 2404.46│  196.67│586605.68│  11,838│   3,943│200:2;500:82;503:657
   4s│    541│     19│    993│   13.05│ 3906.96│  107.01│76635.59│  92,267│  23,059│200:19;500:101;503:892
   5s│    578│     56│   1260│   33.06│ 4962.68│   16.99│30247.34│ 274,332│  54,817│200:56;500:120;503:1140
   6s│    620│     98│   1298│   50.86│ 6002.58│    5.61│19661.46│ 478,088│  79,621│200:98;500:127;503:1171
   7s│    666│    144│   1308│   64.66│ 6981.06│    5.61│15464.73│ 700,446│ 100,036│200:144;500:127;503:1181
   8s│    709│    187│   1331│   73.37│ 7929.99│    5.61│13630.45│ 909,327│ 113,645│200:187;500:131;503:1200
   9s│    754│    233│   1339│   79.36│ 8937.00│    5.02│12600.29│1,132,775│ 125,856│200:233;500:136;503:1203
  10s│    774│    274│   1344│   83.42│ 9832.80│    5.02│11986.97│1,331,240│ 133,114│200:274;500:137;503:1207
  11s│    785│    309│   1349│   85.85│10370.61│    5.02│11647.92│1,501,655│ 136,482│200:309;500:140;503:1209
  12s│    786│    313│   1662│   56.69│11652.97│    5.02│17638.57│1,535,504│ 127,931│200:313;500:156;503:1506
```



![4](17statistics\4.jpg)



![5](17statistics\5.jpg)



### 3.2.3Adaptive Concurrency

ef-adaptive-concurrency.yaml

kubectl apply -f ef-adaptive-concurrency.yaml -n istio

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: adaptive 
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
          filter:
            name: "envoy.filters.network.http_connection_manager"
            subFilter:
              name: "envoy.filters.http.router"
    patch:
      operation: INSERT_BEFORE
      value: 
        name: envoy.filters.http.adaptive_concurrency
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.filters.http.adaptive_concurrency.v3.AdaptiveConcurrency
          gradient_controller_config:
            sample_aggregate_percentile:
              value: 90
            concurrency_limit_params:
              concurrency_update_interval: 0.1s
              max_concurrency_limit: 10
            min_rtt_calc_params:
              jitter:
                value: 10
              interval: 60s
              request_count: 50
              min_concurrency: 2
              buffer: 
                value: 25
          enabled:
            default_value: true
            runtime_key: "adaptive_concurrency.enabled"
```

测试

```
使用了adaptive concurrency效果

[root@node01 ~]# go-stress-testing -c 10 -n 100000 -u http://192.168.229.128:30494/productpage

 开始启动  并发数:10 请求数:100000 请求参数: 
request:
 form:http 
 url:http://192.168.229.134:32406/productpage 
 method:GET 
 headers:map[] 
 data: 
 verify:statusCode 
 timeout:30s 
 debug:false 



─────┬───────┬───────┬───────┬────────┬────────┬────────┬────────┬────────┬────────┬────────
 耗时│ 并发数│ 成功数│ 失败数│   qps  │最长耗时│最短耗时│平均耗时│下载字节│字节每秒│ 错误码
─────┼───────┼───────┼───────┼────────┼────────┼────────┼────────┼────────┼────────┼────────
   1s│     10│     27│    123│   28.33│  161.73│   15.18│  353.01│ 132,984│ 132,980│200:27;503:123
   2s│     10│     54│    225│   27.92│  239.54│   15.18│  358.11│ 266,439│ 133,207│200:54;503:225
   3s│     10│     67│    312│   22.71│  632.94│   14.89│  440.37│ 331,973│ 110,645│200:67;503:312
   4s│     10│     93│    387│   23.62│  632.94│   14.89│  423.32│ 459,574│ 114,886│200:93;503:387
   5s│     10│    123│    465│   25.00│  632.94│   14.89│  399.92│ 606,974│ 121,388│200:123;503:465
   6s│     10│    145│    542│   24.46│  632.94│   14.89│  408.76│ 715,893│ 119,312│200:145;503:542
   7s│     10│    174│    618│   25.15│  632.94│   14.89│  397.60│ 858,064│ 122,579│200:174;503:618
   8s│     10│    206│    691│   25.92│  632.94│   14.89│  385.73│1,015,701│ 126,954│200:206;503:691
   9s│     10│    242│    746│   27.07│  632.94│   14.89│  369.41│1,191,616│ 132,399│200:242;503:746
  10s│     10│    278│    808│   27.99│  632.94│   14.89│  357.25│1,366,706│ 136,663│200:278;503:808
  11s│     10│    306│    879│   28.10│  632.94│   14.89│  355.81│1,504,569│ 136,775│200:306;503:879
  12s│     10│    343│    941│   28.78│  632.94│   14.89│  347.46│1,685,838│ 140,484│200:343;503:941
  13s│     10│    378│   1013│   29.21│  632.94│   14.89│  342.40│1,856,999│ 142,844│200:378;503:1013
  
  
没有使用adaptive concurrency效果
[root@node01 ~]# go-stress-testing -c 10 -n 100000 -u http://192.168.229.134:32406/productpage

 开始启动  并发数:10 请求数:100000 请求参数: 
request:
 form:http 
 url:http://192.168.229.134:32406/productpage 
 method:GET 
 headers:map[] 
 data: 
 verify:statusCode 
 timeout:30s 
 debug:false 



─────┬───────┬───────┬───────┬────────┬────────┬────────┬────────┬────────┬────────┬────────
 耗时│ 并发数│ 成功数│ 失败数│   qps  │最长耗时│最短耗时│平均耗时│下载字节│字节每秒│ 错误码
─────┼───────┼───────┼───────┼────────┼────────┼────────┼────────┼────────┼────────┼────────
   1s│     10│     65│      0│   69.41│  238.32│   66.33│  144.06│ 314,807│ 314,645│200:65
   2s│     10│    121│      0│   63.40│  268.78│   66.33│  157.73│ 586,979│ 293,421│200:121
   3s│     10│    178│      0│   61.49│  395.12│   66.33│  162.64│ 862,338│ 287,407│200:178
   4s│     10│    228│      0│   58.30│  395.12│   66.33│  171.53│1,106,416│ 276,579│200:228
   5s│     10│    271│      0│   55.88│  395.12│   66.33│  178.97│1,314,233│ 262,820│200:271
   6s│     10│    321│      0│   54.25│  395.12│   66.33│  184.34│1,556,315│ 259,368│200:321
```



![7](17statistics\7.jpg)



![8](17statistics\8.jpg)



### 3.2.4Composite Filter

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
                name: envoy.filters.http.composite
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

![9](17statistics\9.jpg)



![10](17statistics\10.jpg)



### 3.2.5Compressor

```
cat << EOF > ef-ratings-http-filter-compression.yaml 
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: ratings
spec:
  workloadSelector:
    labels:
      app: ratings
  configPatches:
    - applyTo: HTTP_FILTER
      match:
        context: SIDECAR_INBOUND
        listener:
          filterChain:
            filter:
              name: envoy.filters.network.http_connection_manager
              subFilter:
                name: envoy.filters.http.router
      patch:
        operation: INSERT_BEFORE
        value:
          name: envoy.filters.http.compressor
          typed_config:
            "@type": type.googleapis.com/envoy.extensions.filters.http.compressor.v3.Compressor
            response_direction_config:
              common_config:
                enabled:
                  default_value: true
                  runtime_key: response_compressor_enabled
                min_content_length: 10
                content_type:
                - 'application/json'
            request_direction_config:
              common_config:
                enabled:
                  default_value: true
                  runtime_key: request_compressor_enabled
            compressor_library:
              name: text_optimized
              typed_config:
                "@type": type.googleapis.com/envoy.extensions.compression.gzip.compressor.v3.Gzip
                memory_level: 9
                window_bits: 12
                compression_level: BEST_SPEED
                compression_strategy: DEFAULT_STRATEGY
EOF

kubectl apply -f ef-ratings-http-filter-compression.yaml  -n istio
```

![11](17statistics\11.jpg)



![12](17statistics\12.jpg)

![13](17statistics\13.jpg)



### 3.2.6Connection limit

![14](17statistics\14.jpg)

ef-connection_limit.yaml

kubectl apply -f ef-connection_limit.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: simple
  namespace: istio-system
spec:
  workloadSelector:
    labels:
      istio: ingressgateway
  configPatches:
  - applyTo: NETWORK_FILTER
    match:
      context: GATEWAY
      listener:
        portNumber: 31400 
        filterChain:
          filter:
            name: "envoy.filters.network.tcp_proxy"
    patch:
      operation: INSERT_BEFORE
      value:
        name: envoy.filters.network.connection_limit
        typed_config:
          '@type': type.googleapis.com/envoy.extensions.filters.network.connection_limit.v3.ConnectionLimit
          stat_prefix: connectionLimit
          max_connections: 1
          delay: 1s
          runtime_enabled:
            default_value: true
            runtime_key: connectionlimit.enabled
```

开启两个telnet，第二个1秒后就关闭了

==ingressgateway貌似没有statistics==

案例就先写到这里，其他案例大家可以查看envoyproxy.io网址上去查看





ef-tcp-echo-connection_limit.yaml

kubectl apply -f ef-tcp-echo-connection_limit.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: simple

spec:
  workloadSelector:
    labels:
      app: tcp-echo
  configPatches:
  - applyTo: NETWORK_FILTER
    match:
      context: SIDECAR_INBOUND
      listener:
        portNumber: 9000 
        filterChain:
          filter:
            name: "envoy.filters.network.tcp_proxy"
    patch:
      operation: INSERT_BEFORE
      value:
        name: envoy.filters.network.connection_limit
        typed_config:
          '@type': type.googleapis.com/envoy.extensions.filters.network.connection_limit.v3.ConnectionLimit
          stat_prefix: connectionLimit
          max_connections: 1
          delay: 1s
          runtime_enabled:
            default_value: true
            runtime_key: connectionlimit.enabled
```

tcp-echo-services.yaml

kubectl apply -f tcp-echo-services.yaml -n istio

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
  name: tcp-echo-v1
  labels:
    app: tcp-echo
    version: v1
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
        sidecar.istio.io/statsInclusionRegexps: ".*"
    spec:
      containers:
      - name: tcp-echo
        image: docker.io/istio/tcp-echo-server:1.2
        imagePullPolicy: IfNotPresent
        args: [ "9000,9001,9002", "one" ]
        ports:
        - containerPort: 9000
        - containerPort: 9001
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: tcp-echo-v2
  labels:
    app: tcp-echo
    version: v2
spec:
  replicas: 1
  selector:
    matchLabels:
      app: tcp-echo
      version: v2
  template:
    metadata:
      labels:
        app: tcp-echo
        version: v2
      annotations:
        sidecar.istio.io/statsInclusionRegexps: ".*"
    spec:
      containers:
      - name: tcp-echo
        image: docker.io/istio/tcp-echo-server:1.2
        imagePullPolicy: IfNotPresent
        args: [ "9000,9001,9002", "two" ]
        ports:
        - containerPort: 9000
        - containerPort: 9001
```

