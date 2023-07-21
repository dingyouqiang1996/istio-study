# 1什么是 bandwidth limit  filter

bandwidth limit  filter是envoy的一个http过滤器，用来限值流量，可以限值请求或响应的流量，或请求响应同时限值。可以配置在host级别，route级别，或连接级别。名称是 envoy.**filter**s.**http**.bandwidth_limit 。

# 2配置

```
{
  "stat_prefix": "...",stat前缀
  "enable_mode": "...",启用模式
  "limit_kbps": "{...}",限速KiB/s
  "fill_interval": "{...}",令牌填充周期默认50ms，必须大于20ms
  "runtime_enabled": "{...}"是否启用，默认启用
}
```

enable_mode：

- DISABLED

  *(DEFAULT)* ⁣**Filter** is disabled.

- REQUEST

  ⁣**Filter** enabled only for incoming traffic.

- RESPONSE

  ⁣**Filter** enabled only for outgoing traffic.

- REQUEST_AND_RESPONSE

  ⁣**Filter** enabled for both incoming and outgoing traffic.

# 3实战

ef-bandwidth_limit-enable_mode-REQUEST_AND_RESPONSE.yaml

kubectl apply -f ef-bandwidth_limit-enable_mode-REQUEST_AND_RESPONSE.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: bandth 
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
      operation: INSERT_BEFORE
      value: 
        name: envoy.filters.http.bandwidth_limit
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.filters.http.bandwidth_limit.v3.BandwidthLimit
          stat_prefix:  bandthlimit
          enable_mode: REQUEST_AND_RESPONSE
          limit_kbps: 1
          fill_interval: 1s
          runtime_enabled:
            default_value: true
            runtime_key: bandwidth.enable
```

压测：

```
被限速后：
[root@node01 ~]# go-stress-testing -c 100 -n 100000 -u http://192.168.229.134:32406/productpage

 开始启动  并发数:100 请求数:100000 请求参数: 
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
   1s│     65│     65│      0│  186.18│  985.68│   72.61│  537.12│ 315,807│ 315,720│200:65
   2s│    100│    127│      0│  105.31│ 1580.34│   72.61│  949.58│ 615,073│ 307,492│200:127
   3s│    100│    188│      0│   86.60│ 1697.14│   72.61│ 1154.79│ 912,152│ 304,029│200:188
   4s│    100│    254│      0│   78.78│ 1697.14│   72.61│ 1269.41│1,231,142│ 307,777│200:254
   5s│    100│    322│      0│   75.46│ 1697.14│   72.61│ 1325.21│1,561,498│ 312,294│200:322
```

