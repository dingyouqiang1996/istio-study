# 1什么是admission control filter

admission control filter是envoy一个http类型的过滤器，作用是根据前面请求的成功率，概率性的拒绝请求连接。他是基于客户端的.被拒绝的概率的计算公式如下：

![1](28image\1.jpg)

n是在窗口中的请求数量

threshold是个可配置的值，表示最低的阈值，超过这个阈值就不会拒绝

aggression用来控制拒绝曲线的可能性，值越大可能性越高

![aggression_graph](28image\aggression_graph.png)



# 2配置

```
{
  "enabled": "{...}",是否启用
  "success_criteria": "{...}",请求成功条件
  "sampling_window": "{...}",采样窗口
  "aggression": "{...}",回归值
  "sr_threshold": "{...}",不拒绝的最低值
  "rps_threshold": "{...}",每秒并发请求数，低于这个值，不拒绝
  "max_rejection_probability": "{...}"最大拒绝可能性
}
```

success_criteria：

```
{
  "http_criteria": "{...}",http条件
  "grpc_criteria": "{...}"grpc条件
}
```

# 3实战

ef-admission_control.yaml

kubectl apply -f ef-admission_control.yaml -n istio

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
          "@type": type.googleapis.com/envoy.extensions.filters.http.admission_control.v3alph.AdmissionControl
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

测试：

```
[root@node01 ~]# go-stress-testing -c 1000 -n 100000 -u http://192.168.229.134:32406/productpage

 开始启动  并发数:1000 请求数:100000 请求参数: 
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
   1s│     12│      1│     11│   97.97│  943.41│  552.98│10206.81│   5,381│   5,377│200:1;500:11
   2s│     56│      2│     54│   23.20│ 1998.26│  552.98│43102.39│  10,992│   5,495│200:2;500:35;503:19
   3s│    438│      3│    544│    2.78│ 2944.99│   46.50│359608.01│  15,877│   5,291│200:3;500:74;503:470
   4s│    502│     40│    998│   26.42│ 3914.97│    5.56│37852.22│ 196,068│  49,014│200:40;500:100;503:898
   5s│    562│    100│   1112│   55.56│ 4987.91│    3.35│17998.52│ 487,234│  97,444│200:100;500:115;503:997
   6s│    615│    153│   1129│   73.30│ 5963.17│    3.35│13643.41│ 743,955│ 123,982│200:153;500:120;503:1009
   7s│    664│    202│   1156│   83.83│ 6987.22│    3.35│11929.29│ 981,966│ 140,270│200:202;500:126;503:1030
   8s│    710│    273│   1179│   94.72│ 7930.76│    3.35│10556.94│1,326,043│ 165,746│200:273;500:136;503:1043
   9s│    729│    322│   1192│  100.32│ 8594.45│    3.35│ 9968.20│1,564,054│ 173,776│200:322;500:142;503:1050
  10s│    741│    396│   1206│  106.04│ 9907.97│    3.35│ 9430.67│1,922,568│ 192,251│200:396;500:146;503:1060
  11s│    751│    455│   1241│  106.86│10361.89│    3.35│ 9358.25│2,211,485│ 201,040│200:455;500:157;503:1084
  12s│    751│    495│   1427│  100.25│10361.89│    3.35│ 9975.00│2,413,550│ 201,124│200:495;500:191;503:1236
```

错误率大概是80%，我们配置的阈值