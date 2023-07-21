# 1什么是Adaptive Concurrency filter

Adaptive Concurrency filter是envoy的一个http类型的过滤器，作用是根据采样的请求的延迟数据动态调整允许的并发的数量。有两个控制器：Concurrency Controllers用来决定forward决策和记录延迟数据。Gradient Controller用来实现基于周期性测量的 round-trip time (minRTT ）的forward决策。minRTT用来计算梯度，梯度用来计算新的并发limit值。

# 2配置

```
{
  "gradient_controller_config": "{...}",梯度控制器配置
  "enabled": "{...}"是否启用
}
```

gradient_controller_config：

```
{
  "sample_aggregate_percentile": "{...}",采样率，默认p50
  "concurrency_limit_params": "{...}",并发限值参数
  "min_rtt_calc_params": "{...}"minRTT计算参数
}
```

concurrency_limit_params：

```
{
  "max_concurrency_limit": "{...}",最大并发限值
  "concurrency_update_interval": "{...}"并发更新周期
}
```

min_rtt_calc_params：

```
{
  "interval": "{...}",计算周期
  "request_count": "{...}",请求的数量，默认50
  "jitter": "{...}",抖动
  "min_concurrency": "{...}",计算minRTT的最小并发，默认3
  "buffer": "{...}" 用来增加并发限值稳定性的值，默认25%
}
```

# 3实战

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

测试：

```
使用了adaptive concurrency效果

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

