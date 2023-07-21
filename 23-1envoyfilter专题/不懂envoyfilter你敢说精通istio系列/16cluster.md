# 1什么是cluster

cluster翻译成中文就是集群，在envoy中表示一些端点的集合。在xds中有cds，c就是cluster的意思。cluster的主要作用是服务发现，负载均衡，连接参数，断路器，dns配置等功能。cluster很重要，建议大家重点掌握。

# 2配置

```
{
  "transport_socket_matches": [],传输socket匹配配置
  "name": "...",cluster名称，route引用时会用到
  "alt_stat_name": "...",stat的cluste名称
  "type": "...",集群类型
  "cluster_type": "{...}",自定义cluster类型
  "eds_cluster_config": "{...}",eds获取endpoint集群配置
  "connect_timeout": "{...}",连接超时时间
  "per_connection_buffer_limit_bytes": "{...}",每个连接缓存限值
  "lb_policy": "...",负载均衡策略
  "load_assignment": "{...}",端点信息配置
  "health_checks": [],健康检查配置
  "max_requests_per_connection": "{...}",每个连接最大请求数量
  "circuit_breakers": "{...}",连接池配置
  "upstream_http_protocol_options": "{...}",上游http协议选项
  "common_http_protocol_options": "{...}",共性http协议参数
  "http_protocol_options": "{...}",http1.1参数
  "http2_protocol_options": "{...}",http2参数
  "typed_extension_protocol_options": "{...}",扩展协议选项
  "dns_refresh_rate": "{...}",dns刷新频率
  "dns_failure_refresh_rate": "{...}",dns失败刷新频率
  "respect_dns_ttl": "...",dns刷新频率写入ttl
  "dns_lookup_family": "...",dns查找ip类型
  "dns_resolvers": [],已废弃
  "use_tcp_for_dns_lookups": "...",dns查找时用tcp协议
  "dns_resolution_config": "{...}",dns服务器，及参数配置
  "wait_for_warm_on_init": "{...}",等待warm-up完成
  "outlier_detection": "{...}",断路器配置
  "cleanup_interval": "{...}",过期的hosts清理间隔，默认5000ms，ORIGINAL_DST生效
  "upstream_bind_config": "{...}",绑定新连接的上游连接配置
  "lb_subset_config": "{...}",子集路由配置
  "ring_hash_lb_config": "{...}",一致性hash负载均衡配置
  "maglev_lb_config": "{...}",Maglev 负载均衡配置
  "original_dst_lb_config": "{...}",original_dst负载均衡配置
  "least_request_lb_config": "{...}",最少请求负载均衡配置
  "round_robin_lb_config": "{...}",轮训负载均衡配置
  "common_lb_config": "{...}",共性负载均衡配置
  "transport_socket": "{...}",传输socket配置
  "metadata": "{...}",元数据设置
  "protocol_selection": "...",已废弃
  "upstream_connection_options": "{...}",上游连接选项
  "close_connections_on_host_health_failure": "...",当host不健康时关闭连接
  "ignore_health_on_host_removal": "...",当host删除时忽略健康状态
  "filters": [],network filter chain
  "load_balancing_policy": "{...}",负载均衡策略配置
  "track_timeout_budgets": "...",已废弃
  "upstream_config": "{...}",配置上游连接值和上游类型
  "track_cluster_stats": "{...}",跟踪cluster状态配置
  "preconnect_policy": "{...}",预连接配置
  "connection_pool_per_downstream_connection": "..."每个下游连接使用一个连接池
}
```

transport_socket_matches：

```
{
  "name": "...",名称
  "match": "{...}",匹配条件
  "transport_socket": "{...}"传输socket配置
}
```

 **transport_socket** ：

- [envoy.transport_sockets.alts](https://www.envoyproxy.io/docs/envoy/latest/api-v3/extensions/transport_sockets/alts/v3/alts.proto#extension-envoy-transport-sockets-alts)
- [envoy.transport_sockets.raw_buffer](https://www.envoyproxy.io/docs/envoy/latest/api-v3/extensions/transport_sockets/raw_buffer/v3/raw_buffer.proto#extension-envoy-transport-sockets-raw-buffer)
- [envoy.transport_sockets.starttls](https://www.envoyproxy.io/docs/envoy/latest/api-v3/extensions/transport_sockets/starttls/v3/starttls.proto#extension-envoy-transport-sockets-starttls)
- [envoy.transport_sockets.tap](https://www.envoyproxy.io/docs/envoy/latest/api-v3/extensions/transport_sockets/tap/v3/tap.proto#extension-envoy-transport-sockets-tap)
- [envoy.transport_sockets.tls](https://www.envoyproxy.io/docs/envoy/latest/api-v3/extensions/transport_sockets/tls/v3/tls.proto#extension-envoy-transport-sockets-tls)
- [envoy.transport_sockets.upstream_proxy_protocol](https://www.envoyproxy.io/docs/envoy/latest/api-v3/extensions/transport_sockets/proxy_protocol/v3/upstream_proxy_protocol.proto#extension-envoy-transport-sockets-upstream-proxy-protocol)

type：

Refer to [service discovery type](https://www.envoyproxy.io/docs/envoy/latest/intro/arch_overview/upstream/service_discovery#arch-overview-service-discovery-types) for an explanation on each type.

- STATIC

  *(DEFAULT)* ⁣Refer to the [static discovery type](https://www.envoyproxy.io/docs/envoy/latest/intro/arch_overview/upstream/service_discovery#arch-overview-service-discovery-types-static) for an explanation.

- STRICT_DNS

  ⁣Refer to the [strict DNS discovery type](https://www.envoyproxy.io/docs/envoy/latest/intro/arch_overview/upstream/service_discovery#arch-overview-service-discovery-types-strict-dns) for an explanation.

- LOGICAL_DNS

  ⁣Refer to the [logical DNS discovery type](https://www.envoyproxy.io/docs/envoy/latest/intro/arch_overview/upstream/service_discovery#arch-overview-service-discovery-types-logical-dns) for an explanation.

- EDS

  ⁣Refer to the [service discovery type](https://www.envoyproxy.io/docs/envoy/latest/intro/arch_overview/upstream/service_discovery#arch-overview-service-discovery-types-eds) for an explanation.

- ORIGINAL_DST

  ⁣Refer to the [original destination discovery type](https://www.envoyproxy.io/docs/envoy/latest/intro/arch_overview/upstream/service_discovery#arch-overview-service-discovery-types-original-destination) for an explanation.

cluster_type：

```
{
  "name": "...",名称
  "typed_config": "{...}"配置
}
```

- [envoy.clusters.aggregate](https://www.envoyproxy.io/docs/envoy/latest/api-v3/extensions/clusters/aggregate/v3/cluster.proto#extension-envoy-clusters-aggregate)
- [envoy.clusters.dynamic_forward_proxy](https://www.envoyproxy.io/docs/envoy/latest/api-v3/extensions/clusters/dynamic_forward_proxy/v3/cluster.proto#extension-envoy-clusters-dynamic-forward-proxy)
- [envoy.clusters.redis](https://www.envoyproxy.io/docs/envoy/latest/api-v3/extensions/clusters/redis/v3/redis_cluster.proto#extension-envoy-clusters-redis)

eds_cluster_config：

```
{
  "eds_config": "{...}",eds来源配置
  "service_name": "..."服务名称
}
```

lb_policy：

Refer to [load balancer type](https://www.envoyproxy.io/docs/envoy/latest/intro/arch_overview/upstream/load_balancing/load_balancers#arch-overview-load-balancing-types) architecture overview section for information on each type.

- ROUND_ROBIN

  *(DEFAULT)* ⁣Refer to the [round robin load balancing policy](https://www.envoyproxy.io/docs/envoy/latest/intro/arch_overview/upstream/load_balancing/load_balancers#arch-overview-load-balancing-types-round-robin) for an explanation.

- LEAST_REQUEST

  ⁣Refer to the [least request load balancing policy](https://www.envoyproxy.io/docs/envoy/latest/intro/arch_overview/upstream/load_balancing/load_balancers#arch-overview-load-balancing-types-least-request) for an explanation.

- RING_HASH

  ⁣Refer to the [ring hash load balancing policy](https://www.envoyproxy.io/docs/envoy/latest/intro/arch_overview/upstream/load_balancing/load_balancers#arch-overview-load-balancing-types-ring-hash) for an explanation.

- RANDOM

  ⁣Refer to the [random load balancing policy](https://www.envoyproxy.io/docs/envoy/latest/intro/arch_overview/upstream/load_balancing/load_balancers#arch-overview-load-balancing-types-random) for an explanation.

- MAGLEV

  ⁣Refer to the [Maglev load balancing policy](https://www.envoyproxy.io/docs/envoy/latest/intro/arch_overview/upstream/load_balancing/load_balancers#arch-overview-load-balancing-types-maglev) for an explanation.

- CLUSTER_PROVIDED

  ⁣This load balancer type must be specified if the configured cluster provides a cluster specific load balancer. Consult the configured cluster’s documentation for whether to set this option or not.

- LOAD_BALANCING_POLICY_CONFIG

  ⁣Use the new [load_balancing_policy](https://www.envoyproxy.io/docs/envoy/latest/api-v3/config/cluster/v3/cluster.proto.html#envoy-v3-api-field-config-cluster-v3-cluster-load-balancing-policy) field to determine the LB policy.

load_assignment：

```
{
  "cluster_name": "...",cluster名称
  "endpoints": [],端点配置
  "policy": "{...}"策略配置
}
```

endpoints：

```
{
  "locality": "{...}",位置信息
  "lb_endpoints": [],端点
  "load_balancer_endpoints": "{...}",端点
  "leds_cluster_locality_config": "{...}",位置配置
  "load_balancing_weight": "{...}",负载均衡权重
  "priority": "..."优先级
}
```

lb_endpoints：

```
{
  "endpoint": "{...}",端点信息
  "health_status": "...",健康状态
  "metadata": "{...}",元数据信息
  "load_balancing_weight": "{...}"负载均衡权重
}
```

endpoint：

```
{
  "address": "{...}",地址
  "health_check_config": "{...}",健康检查配置
  "hostname": "..."主机名
}
```

policy：

```
{
  "overprovisioning_factor": "{...}",优先级和位置端点被认为是overprovisioning的因子
  "endpoint_stale_after": "{...}"端点可以使用的最大时间，超过这个时间没有新的端点被认为是过期的
}
```

health_checks：

```
{
  "timeout": "{...}",健康检查超时时间
  "interval": "{...}",周期
  "initial_jitter": "{...}",初始抖动
  "interval_jitter": "{...}",周期性抖动
  "interval_jitter_percent": "...",周期性抖动百分比
  "unhealthy_threshold": "{...}",不健康次数限值
  "healthy_threshold": "{...}",健康次数限值
  "reuse_connection": "{...}",重用健康检查的连接
  "http_health_check": "{...}",http类型的健康检查
  "tcp_health_check": "{...}",tcp类型的健康检查
  "grpc_health_check": "{...}",grpc类型的健康检查
  "custom_health_check": "{...}",自定义类型的健康检查
  "no_traffic_interval": "{...}",没有流量的健康检查周期
  "no_traffic_healthy_interval": "{...}",健康节点的，没有流量的健康检查周期
  "unhealthy_interval": "{...}",不健康节点的，健康检查周期
  "unhealthy_edge_interval": "{...}",当节点不健康时，第一次健康检查的周期
  "healthy_edge_interval": "{...}",当节点健康时，第一次的健康检查周期
  "event_log_path": "...",健康检查事件日志路径
  "always_log_health_check_failures": "...",是否总是记录健康检查失败事件日志
  "tls_options": "{...}",覆盖tls选项
  "transport_socket_match_criteria": "{...}"匹配transport_socket_match的条件
}
```

http_health_check：

```
{
  "host": "...",主机名
  "path": "...",路径
  "request_headers_to_add": [],添加请求头
  "request_headers_to_remove": [],删除请求头
  "expected_statuses": [],期望的状态码
  "retriable_statuses": [],可重试的状态码
  "codec_client_type": "...",客户端codec类型
  "service_name_matcher": "{...}"服务名称匹配
}
```

tcp_health_check：

```
{
  "send": "{...}",16进制发送的消息
  "receive": []16进制接收的消息
}
```

grpc_health_check：

```
{
  "service_name": "...",服务名称
  "authority": "...":authority头值
}
```

custom_health_check：

```
{
  "name": "...",名称
  "typed_config": "{...}"配置
}
```

- [envoy.health_checkers.redis](https://www.envoyproxy.io/docs/envoy/latest/api-v3/extensions/health_checkers/redis/v3/redis.proto#extension-envoy-health-checkers-redis)

circuit_breakers：

```
{
  "priority": "...",路由优先级
  "max_connections": "{...}",最大连接数量，默认1024
  "max_pending_requests": "{...}",最大等待请求数量，默认1024
  "max_requests": "{...}",最大请求数量，默认1024
  "max_retries": "{...}",重大重试次数，默认3
  "retry_budget": "{...}",活跃连接百分比设置并发重试限值
  "track_remaining": "...",短路器打开前资源存量是否记录到stat
  "max_connection_pools": "{...}"最大连接池数量
}
```

upstream_http_protocol_options：

```
{
  "auto_sni": "...",是否自动sni
  "auto_san_validation": "...",自动san校验
  "override_auto_sni_header": "..."覆盖sni头
}
```

common_http_protocol_options：

```
{
  "idle_timeout": "{...}",空闲超时时间
  "max_connection_duration": "{...}",连接最大时间
  "max_headers_count": "{...}",最大头数量
  "max_stream_duration": "{...}",流最大时间
  "headers_with_underscores_action": "...",有下划线头处理动作
  "max_requests_per_connection": "{...}"每个连接最大请求数量
}
```

http_protocol_options：

```
{
  "allow_absolute_url": "{...}",是否允许处理连接用完全的url
  "accept_http_10": "...",是否接受http1.0 0.9协议
  "default_host_for_http_10": "...",http1.0默认host
  "header_key_format": "{...}",header键格式
  "enable_trailers": "...",是否启用trailer
  "allow_chunked_length": "...",是否允许分块的长度
  "override_stream_error_on_invalid_http_message": "{...}"覆盖流错误当有错误的http消息时
}
```

http2_protocol_options：

```
{
  "hpack_table_size": "{...}",hpack头表大小
  "max_concurrent_streams": "{...}",最大并发流
  "initial_stream_window_size": "{...}",初始流窗口大小
  "initial_connection_window_size": "{...}",初始连接窗口大小
  "allow_connect": "...",是否允许升级的协议连接
  "max_outbound_frames": "{...}",最大出口帧
  "max_outbound_control_frames": "{...}",最大出口控制帧
  "max_consecutive_inbound_frames_with_empty_payload": "{...}",最大连续的入口帧当消息体是空时
  "max_inbound_priority_frames_per_stream": "{...}",每个流最大入口优先帧
  "max_inbound_window_update_frames_per_data_frame_sent": "{...}",每个数据帧发送的最大入口窗口更新帧
  "stream_error_on_invalid_http_messaging": "...",当http协议错误时，流错误
  "override_stream_error_on_invalid_http_message": "{...}",当http出错时覆盖流错误
  "connection_keepalive": "{...}"保持连接配置信息
}
```

dns_lookup_family：

- AUTO

  *(DEFAULT)* ⁣

- V4_ONLY

  ⁣

- V6_ONLY

  ⁣

- V4_PREFERRED

  ⁣‘

outlier_detection：

```
{
  "consecutive_5xx": "{...}",最大连续5xx错误，默认5
  "interval": "{...}",异常检测间隔，默认10s
  "base_ejection_time": "{...}",弹出时间
  "max_ejection_percent": "{...}",最大弹出百分比
  "enforcing_consecutive_5xx": "{...}",连续5xx错误超出时弹出机率，默认100
  "enforcing_success_rate": "{...}",成功率探测超出时弹出几率，默认100
  "success_rate_minimum_hosts": "{...}",成功几率异常检测最小host数量
  "success_rate_request_volume": "{...}",最小请求数量，用成功率检测时
  "success_rate_stdev_factor": "{...}",成功率计算公式mean - (stdev * success_rate_stdev_factor). 默认1900
  "consecutive_gateway_failure": "{...}",连续gateway错误，弹出，默认5
  "enforcing_consecutive_gateway_failure": "{...}",连续网关错误弹出几率，默认0
  "split_external_local_origin_errors": "...",是否区分本地源错误和外部错误
  "consecutive_local_origin_failure": "{...}",连续本地错误触发弹出数值，默认5
  "enforcing_consecutive_local_origin_failure": "{...}",连续本地错误触发弹出的几率，默认100
  "enforcing_local_origin_success_rate": "{...}",本地错误成功率触发弹出几率，默认100
  "failure_percentage_threshold": "{...}",错误百分比触发弹出，默认85
  "enforcing_failure_percentage": "{...}",错误百分比达到限值弹出的几率，默认0
  "enforcing_failure_percentage_local_origin": "{...}",本地源错误百分比弹出几率，默认0
  "failure_percentage_minimum_hosts": "{...}",基于错误百分比的弹出的最小host数量，默认5
  "failure_percentage_request_volume": "{...}",基于错误百分比弹出的请求数量，默认50
  "max_ejection_time": "{...}"最大弹出时间，默认300s
}
```

lb_subset_config：

```
{
  "fallback_policy": "...",没有endpoint匹配时的处理策略
  "default_subset": "{...}",没有endpoint匹配时的默认子集
  "subset_selectors": [],子集选择
  "locality_weight_aware": "...",是否感知位置
  "scale_locality_weight": "...",调整位置权重
  "panic_mode_any": "...",当回退策略设置没有选择端点时，匹配任何节点
  "list_as_any": "..."匹配metadata列表中的任意值
}
```

ring_hash_lb_config：

```
{
  "minimum_ring_size": "{...}",最小ring环大小
  "hash_function": "...",hash函数
  "maximum_ring_size": "{...}"最大ring环大小
}
```

 **maglev_lb_config** ：

```
{
  "table_size": "{...}"表大小，默认65537
}
```

 **original_dst_lb_config** ：

```
{
  "use_http_header": "..."是否使用x-envoy-original-dst-host 头作为目标集群
}
```

 **least_request_lb_config** ：

```
{
  "choice_count": "{...}",最小连接的主机选择数量，默认2
  "active_request_bias": "{...}",weight = load_balancing_weight / (active_requests + 1)^active_request_bias
  "slow_start_config": "{...}"慢开始配置
}
```

 **round_robin_lb_config** ：

```
{
  "slow_start_config": "{...}"慢开始配置
}
```

 **common_lb_config** ：

```
{
  "healthy_panic_threshold": "{...}",panic模式限值，默认 50%
  "zone_aware_lb_config": "{...}",区域感知负载均衡配置
  "locality_weighted_lb_config": "{...}",位置权重负载均衡配置
  "update_merge_window": "{...}",health check/weight/metadata更新合并，默认1s
  "ignore_new_hosts_until_first_hc": "...",忽略新主机知道他们第一次健康检测成功
  "close_connections_on_host_set_change": "...",当主机添加或删除时，drain 连接
  "consistent_hashing_lb_config": "{...}"一致性hash负载均衡配置
}
```

zone_aware_lb_config：

```
{
  "routing_enabled": "{...}",启用区域感知路由的请求比例，默认100%
  "min_cluster_size": "{...}",使用区域感知路由上游主机数量，默认6
  "fail_traffic_on_panic": "..."当panic模式时，请求连接失败
}
```

transport_socket：

```
{
  "name": "...",名称
  "typed_config": "{...}"配置
}
```

metadata：

```
{
  "filter_metadata": "{...}",元数据
  "typed_filter_metadata": "{...}"typed元数据
}
```

 **upstream_connection_options** ：

```
{
  "tcp_keepalive": "{...}"保持连接配置
}
```

 **filters** ：

```
{
  "name": "...",名称
  "typed_config": "{...}"配置
}
```

 **upstream_config** ：

- [envoy.upstreams.http.generic](https://www.envoyproxy.io/docs/envoy/latest/api-v3/extensions/upstreams/http/generic/v3/generic_connection_pool.proto#extension-envoy-upstreams-http-generic)
- [envoy.upstreams.http.http](https://www.envoyproxy.io/docs/envoy/latest/api-v3/extensions/upstreams/http/http/v3/http_connection_pool.proto#extension-envoy-upstreams-http-http)
- [envoy.upstreams.http.http_protocol_options](https://www.envoyproxy.io/docs/envoy/latest/api-v3/extensions/upstreams/http/v3/http_protocol_options.proto#extension-envoy-upstreams-http-http-protocol-options)
- [envoy.upstreams.http.tcp](https://www.envoyproxy.io/docs/envoy/latest/api-v3/extensions/upstreams/http/tcp/v3/tcp_connection_pool.proto#extension-envoy-upstreams-http-tcp)
- [envoy.upstreams.tcp.generic](https://www.envoyproxy.io/docs/envoy/latest/api-v3/extensions/upstreams/tcp/generic/v3/generic_connection_pool.proto#extension-envoy-upstreams-tcp-generic)





# 3实战

## 3.1default

wechat/envoyfilter/cluster/cluster-default.yaml

kubectl apply -f cluster-default.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: cluster
  namespace: istio-system 
spec:
  workloadSelector:
    labels:
      istio: ingressgateway
  configPatches:
  - applyTo: CLUSTER
    patch:
      operation: REPLACE
      value:
          circuitBreakers:
            thresholds:
            - maxConnections: 4294967295
              maxPendingRequests: 4294967295
              maxRequests: 4294967295
              maxRetries: 4294967295
              trackRemaining: true
          connectTimeout: 10s
          edsClusterConfig:
            edsConfig:
              ads: {}
              initialFetchTimeout: 0s
              resourceApiVersion: V3
            serviceName: outbound|9080||productpage.istio.svc.cluster.local
          filters:
          - name: istio.metadata_exchange
            typedConfig:
              '@type': type.googleapis.com/udpa.type.v1.TypedStruct
              typeUrl: type.googleapis.com/envoy.tcp.metadataexchange.config.MetadataExchange
              value:
                protocol: istio-peer-exchange
          metadata:
            filterMetadata:
              istio:
                default_original_port: 9080
                services:
                - host: productpage.istio.svc.cluster.local
                  name: productpage
                  namespace: istio
          name: outbound|9080||productpage.istio.svc.cluster.local
          transportSocketMatches:
          - match:
              tlsMode: istio
            name: tlsMode-istio
            transportSocket:
              name: envoy.transport_sockets.tls
              typedConfig:
                '@type': type.googleapis.com/envoy.extensions.transport_sockets.tls.v3.UpstreamTlsContext
                commonTlsContext:
                  alpnProtocols:
                  - istio-peer-exchange
                  - istio
                  combinedValidationContext:
                    defaultValidationContext:
                      matchSubjectAltNames:
                      - exact: spiffe://cluster.local/ns/istio/sa/bookinfo-productpage
                    validationContextSdsSecretConfig:
                      name: ROOTCA
                      sdsConfig:
                        apiConfigSource:
                          apiType: GRPC
                          grpcServices:
                          - envoyGrpc:
                              clusterName: sds-grpc
                          setNodeOnFirstMessageOnly: true
                          transportApiVersion: V3
                        initialFetchTimeout: 0s
                        resourceApiVersion: V3
                  tlsCertificateSdsSecretConfigs:
                  - name: default
                    sdsConfig:
                      apiConfigSource:
                        apiType: GRPC
                        grpcServices:
                        - envoyGrpc:
                            clusterName: sds-grpc
                        setNodeOnFirstMessageOnly: true
                        transportApiVersion: V3
                      initialFetchTimeout: 0s
                      resourceApiVersion: V3
                sni: outbound_.9080_._.productpage.istio.svc.cluster.local
          - match: {}
            name: tlsMode-disabled
            transportSocket:
              name: envoy.transport_sockets.raw_buffer
          type: EDS
```

## 3.2transport_socket_matches

- [envoy.transport_sockets.alts](https://www.envoyproxy.io/docs/envoy/latest/api-v3/extensions/transport_sockets/alts/v3/alts.proto#extension-envoy-transport-sockets-alts)
- [envoy.transport_sockets.internal_upstream](https://www.envoyproxy.io/docs/envoy/latest/api-v3/extensions/transport_sockets/internal_upstream/v3/internal_upstream.proto#extension-envoy-transport-sockets-internal-upstream)
- [envoy.transport_sockets.raw_buffer](https://www.envoyproxy.io/docs/envoy/latest/api-v3/extensions/transport_sockets/raw_buffer/v3/raw_buffer.proto#extension-envoy-transport-sockets-raw-buffer)
- [envoy.transport_sockets.starttls](https://www.envoyproxy.io/docs/envoy/latest/api-v3/extensions/transport_sockets/starttls/v3/starttls.proto#extension-envoy-transport-sockets-starttls)
- [envoy.transport_sockets.tap](https://www.envoyproxy.io/docs/envoy/latest/api-v3/extensions/transport_sockets/tap/v3/tap.proto#extension-envoy-transport-sockets-tap)
- [envoy.transport_sockets.tcp_stats](https://www.envoyproxy.io/docs/envoy/latest/api-v3/extensions/transport_sockets/tcp_stats/v3/tcp_stats.proto#extension-envoy-transport-sockets-tcp-stats)
- [envoy.transport_sockets.tls](https://www.envoyproxy.io/docs/envoy/latest/api-v3/extensions/transport_sockets/tls/v3/tls.proto#extension-envoy-transport-sockets-tls)
- [envoy.transport_sockets.upstream_proxy_protocol](https://www.envoyproxy.io/docs/envoy/latest/api-v3/extensions/transport_sockets/proxy_protocol/v3/upstream_proxy_protocol.proto#extension-envoy-transport-sockets-upstream-proxy-protocol)

略

## 3.3type

https://www.envoyproxy.io/docs/envoy/latest/intro/arch_overview/upstream/service_discovery#arch-overview-service-discovery-types-logical-dns

Refer to [service discovery type](https://www.envoyproxy.io/docs/envoy/latest/intro/arch_overview/upstream/service_discovery#arch-overview-service-discovery-types) for an explanation on each type.

- STATIC

  *(DEFAULT)* ⁣Refer to the [static discovery type](https://www.envoyproxy.io/docs/envoy/latest/intro/arch_overview/upstream/service_discovery#arch-overview-service-discovery-types-static) for an explanation.

- STRICT_DNS

  ⁣Refer to the [strict DNS discovery type](https://www.envoyproxy.io/docs/envoy/latest/intro/arch_overview/upstream/service_discovery#arch-overview-service-discovery-types-strict-dns) for an explanation.

- LOGICAL_DNS

  ⁣Refer to the [logical DNS discovery type](https://www.envoyproxy.io/docs/envoy/latest/intro/arch_overview/upstream/service_discovery#arch-overview-service-discovery-types-logical-dns) for an explanation.

- EDS

  ⁣Refer to the [service discovery type](https://www.envoyproxy.io/docs/envoy/latest/intro/arch_overview/upstream/service_discovery#arch-overview-service-discovery-types-eds) for an explanation.

- ORIGINAL_DST

  ⁣Refer to the [original destination discovery type](https://www.envoyproxy.io/docs/envoy/latest/intro/arch_overview/upstream/service_discovery#arch-overview-service-discovery-types-original-destination) for an explanation.

### 3.3.1STATIC

cluster-type-STATIC.yaml

kubectl apply -f cluster-type-STATIC.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: cluster
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
                    - "www.baidu.demo:30563"
                    routes:
                    - name: testroute
                      match: 
                        path: /
                      route:
                        cluster: static-cluster
  - applyTo: CLUSTER
    patch:
      operation: ADD
      value:
        name: static-cluster
        connect_timeout: 0.25s
        type: STATIC
        lb_policy: ROUND_ROBIN
        load_assignment:
          cluster_name: static-cluster
          endpoints:
          - lb_endpoints:
            - endpoint:
                address:
                  socket_address:
                    address: 36.152.44.95
                    port_value: 80
```



### 3.3.2STRICT_DNS

ef-type-STRICT_DNS.yaml

kubectl apply -f ef-type-STRICT_DNS.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: cluster
spec:
  workloadSelector:
    labels:
      istio: ingressgateway
  configPatches:
  - applyTo: NETWORK_FILTER
    match:
      listener:
        #name: 0.0.0.0_8080  
        portNumber: 8080
        filterChain:
          filter:
            name: "envoy.filters.network.http_connection_manager"
    patch:
      operation: MERGE
      value:
        name: envoy.filters.network.http_connection_manager
        typed_config:
          "@type": "type.googleapis.com/envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager"
          codec_type: AUTO
          stat_prefix: ingress_http
          route_config:
            name: http.8080
            virtual_hosts:
            - name: “*.8080”
              domains:
              - "*"
              routes:
              - match:
                  caseSensitive: true
                  headers:
                  - name: :method
                    safeRegexMatch:
                      googleRe2: {}
                      regex: G.*T
                  prefix: /
                route:
                  cluster: my-productpage
  - applyTo: CLUSTER
    patch:
      operation: ADD
      value:
          name: my-productpage
          type: STRICT_DNS
          connect_timeout: 10s
          lb_policy: ROUND_ROBIN
          load_assignment:
            cluster_name: my-productpage
            endpoints:
            - lb_endpoints:
              - endpoint:
                  address:
                     socket_address:
                      address: productpage.istio.svc.cluster.local
                      port_value: 9080
```



### 3.3.3LOGICAL_DNS

  a logical DNS cluster only uses the first IP address returned *when a new connection needs to be initiated*.   Connections are never drained, including on a successful DNS resolution that returns 0 hosts. 

 This service discovery type is optimal for large scale web services that must be accessed via DNS. Such services typically use round robin DNS to return many different IP addresses. Typically a different result is returned for each query.  



cluster-type-LOGICAL_DNS.yaml

kubectl apply -f cluster-type-LOGICAL_DNS.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: cluster
spec:
  workloadSelector:
    labels:
      istio: ingressgateway
  configPatches:
  - applyTo: NETWORK_FILTER
    match:
      listener:
        #name: 0.0.0.0_8080  
        portNumber: 8080
        filterChain:
          filter:
            name: "envoy.filters.network.http_connection_manager"
    patch:
      operation: MERGE
      value:
        name: envoy.filters.network.http_connection_manager
        typed_config:
          "@type": "type.googleapis.com/envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager"
          codec_type: AUTO
          stat_prefix: ingress_http
          route_config:
            name: http.8080
            virtual_hosts:
            - name: “*.8080”
              domains:
              - "*"
              routes:
              - match:
                  caseSensitive: true
                  headers:
                  - name: :method
                    safeRegexMatch:
                      googleRe2: {}
                      regex: G.*T
                  prefix: /
                route:
                  cluster: my-productpage
  - applyTo: CLUSTER
    patch:
      operation: ADD
      value:
          name: my-productpage
          type: LOGICAL_DNS
          connect_timeout: 10s
          lb_policy: ROUND_ROBIN
          load_assignment:
            cluster_name: my-productpage
            endpoints:
            - lb_endpoints:
              - endpoint:
                  address:
                     socket_address:
                      address: productpage.istio.svc.cluster.local
                      port_value: 9080
```



### 3.3.4EDS

cluster-type-EDS.yaml

kubectl apply -f cluster-type-EDS.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: cluster
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
                        path: /productpage
                      route:
                        cluster: my-productpage
  - applyTo: CLUSTER
    patch:
      operation: ADD
      value:
          edsClusterConfig:
            edsConfig:
              ads: {}
              initialFetchTimeout: 0s
              resourceApiVersion: V3
            serviceName: outbound|9080||productpage.istio.svc.cluster.local
          name: my-productpage
          type: EDS
```



### 3.3.5ORIGINAL_DST

cluster-type-ORIGINAL_DST.yaml

kubectl apply -f cluster-type-ORIGINAL_DST.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: cluster
spec:
  workloadSelector:
    labels:
      istio: ingressgateway
  configPatches:
  - applyTo: NETWORK_FILTER
    match:
      listener:
        #name: 0.0.0.0_8080  
        portNumber: 8080
        filterChain:
          filter:
            name: "envoy.filters.network.http_connection_manager"
    patch:
      operation: MERGE
      value:
        name: envoy.filters.network.http_connection_manager
        typed_config:
          "@type": "type.googleapis.com/envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager"
          codec_type: AUTO
          stat_prefix: ingress_http
          route_config:
            name: http.9080
            virtual_hosts:
            - name: “*.9080”
              domains:
              - "*"
              routes:
              - match:
                  prefix: /
                  caseSensitive: true
                route:
                  cluster: cluster123
  - applyTo: CLUSTER
    patch:
      operation: ADD
      value: 
        name: "cluster123"
        connect_timeout: 0.5s
        type: ORIGINAL_DST
        lb_policy: CLUSTER_PROVIDED
        original_dst_lb_config:
          use_http_header: true
          http_header_name: x-envoy-original-dst-host 
```

 curl -H "x-envoy-original-dst-host: 172.20.0.235:9080"  http://192.168.229.128:30555/productpage

 curl -H "x-envoy-original-dst-host: 172.20.0.109:9080" http://192.168.229.128:30563/productpage

## 3.4cluster_type

- [envoy.**cluster**s.aggregate](https://www.envoyproxy.io/docs/envoy/latest/api-v3/extensions/clusters/aggregate/v3/cluster.proto#extension-envoy-clusters-aggregate)
- [envoy.**cluster**s.dynamic_forward_proxy](https://www.envoyproxy.io/docs/envoy/latest/api-v3/extensions/clusters/dynamic_forward_proxy/v3/cluster.proto#extension-envoy-clusters-dynamic-forward-proxy)
- [envoy.**cluster**s.redis](https://www.envoyproxy.io/docs/envoy/latest/api-v3/extensions/clusters/redis/v3/redis_cluster.proto#extension-envoy-clusters-redis)

### 3.4.1aggregate

 **Aggregate** **cluster** is used for failover between **cluster**s with different configuration, e.g., from EDS upstream **cluster** to STRICT_DNS upstream **cluster**, from cluster using ROUND_ROBIN load balancing policy to cluster using MAGLEV, from cluster with 0.1s connection timeout to cluster with 1s connection timeout, etc. **Aggregate** cluster loosely couples multiple clusters by referencing their name in the [configuration](https://www.envoyproxy.io/docs/envoy/latest/api-v3/extensions/clusters/aggregate/v3/cluster.proto#envoy-v3-api-msg-extensions-clusters-aggregate-v3-clusterconfig). The fallback priority is defined implicitly by the ordering in the [clusters list](https://www.envoyproxy.io/docs/envoy/latest/api-v3/extensions/clusters/aggregate/v3/cluster.proto#envoy-v3-api-field-extensions-clusters-aggregate-v3-clusterconfig-clusters). 

cluster-cluster_type-aggregate.yaml

kubectl apply -f cluster-cluster_type-aggregate.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: cluster
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
                      route:
                        cluster: aggregate_cluster
  - applyTo: CLUSTER
    patch:
      operation: ADD
      value:
        name: aggregate_cluster
        connect_timeout: 0.25s
        lb_policy: CLUSTER_PROVIDED
        cluster_type:
          name: envoy.clusters.aggregate
          typed_config:
            "@type": type.googleapis.com/envoy.extensions.clusters.aggregate.v3.ClusterConfig
            clusters:
            - outbound|9080||productpage.istio-2.svc.cluster.local
            - outbound|9080||productpage.istio.svc.cluster.local
```

deploy-productpage.yaml

kubectl apply -f deploy-productpage.yaml -n istio-2

```
apiVersion: v1
kind: Service
metadata:
  name: productpage
  labels:
    app: productpage
    service: productpage
spec:
  ports:
  - port: 9080
    name: http
  selector:
    app: productpage
---
apiVersion: v1
kind: ServiceAccount
metadata:
  name: bookinfo-productpage
  labels:
    account: productpage
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
    spec:
      serviceAccountName: bookinfo-productpage
      containers:
      - name: productpage
        image: docker.io/istio/examples-bookinfo-productpage-v1:1.16.4
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
---
```



### 3.4.2dynamic_forward_proxy

 Through the combination of both an [HTTP filter](https://www.envoyproxy.io/docs/envoy/latest/configuration/http/http_filters/dynamic_forward_proxy_filter#config-http-filters-dynamic-forward-proxy) and [custom cluster](https://www.envoyproxy.io/docs/envoy/latest/api-v3/extensions/clusters/dynamic_forward_proxy/v3/cluster.proto#envoy-v3-api-msg-extensions-clusters-dynamic-forward-proxy-v3-clusterconfig), Envoy supports HTTP dynamic forward proxy. This means that Envoy can perform the role of an HTTP proxy without prior knowledge of all configured DNS addresses, while still retaining the vast majority of Envoy’s benefits including asynchronous DNS resolution. 

```
{
  "dns_cache_config": "{...}",
  "allow_insecure_cluster_options": "..."
}
```

dns_cache_config：

```
{
  "name": "...",
  "dns_lookup_family": "...",
  "dns_refresh_rate": "{...}",
  "host_ttl": "{...}",
  "max_hosts": "{...}",
  "dns_failure_refresh_rate": "{...}",
  "dns_cache_circuit_breaker": "{...}",
  "use_tcp_for_dns_lookups": "...",
  "dns_resolution_config": "{...}",
  "preresolve_hostnames": [],
  "dns_query_timeout": "{...}"
}
```

cluster-cluster_type-dynamic_forward_proxy.yaml

kubectl apply -f cluster-cluster_type-dynamic_forward_proxy.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: cluster
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
                  stat_prefix: ingress_http
                  route_config:
                    name: local_route
                    virtual_hosts:
                    - name: local_service
                      domains: ["*"]
                      routes:
                      - match:
                          prefix: "/"
                        route:
                          cluster: dynamic_forward_proxy_cluster
                        typed_per_filter_config:
                          envoy.filters.http.dynamic_forward_proxy:
                            "@type": type.googleapis.com/envoy.extensions.filters.http.dynamic_forward_proxy.v3.PerRouteConfig
                            host_rewrite_literal: www.baidu.com
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
            name: envoy.filters.http.dynamic_forward_proxy
            typed_config:
              "@type": type.googleapis.com/envoy.extensions.filters.http.dynamic_forward_proxy.v3.FilterConfig
              dns_cache_config:
                name: dynamic_forward_proxy_cache_config
                dns_lookup_family: V4_ONLY
                dns_resolution_config:
                  resolvers:
                  - socket_address:
                      address: "8.8.8.8"
                      port_value: 53
                  dns_resolver_options:
                    use_tcp_for_dns_lookups: true
                    no_default_search_domain: true
                            
  - applyTo: CLUSTER
    patch:
      operation: ADD
      value:
        name: dynamic_forward_proxy_cluster
        lb_policy: CLUSTER_PROVIDED
        cluster_type:
          name: envoy.clusters.dynamic_forward_proxy
          typed_config:
            "@type": type.googleapis.com/envoy.extensions.clusters.dynamic_forward_proxy.v3.ClusterConfig
            dns_cache_config:
              name: dynamic_forward_proxy_cache_config
              dns_lookup_family: V4_ONLY
              dns_resolution_config:
                resolvers:
                - socket_address:
                    address: "8.8.8.8"
                    port_value: 53
                dns_resolver_options:
                  use_tcp_for_dns_lookups: true
                  no_default_search_domain: true
```

访问http://192.168.229.128:30563/



### 3.4.3redis

 This cluster adds support for [Redis Cluster](https://redis.io/topics/cluster-spec), as part of [Envoy’s support for Redis Cluster](https://www.envoyproxy.io/docs/envoy/latest/intro/arch_overview/other_protocols/redis#arch-overview-redis). 

```
{
  "cluster_refresh_rate": "{...}",
  "cluster_refresh_timeout": "{...}",
  "redirect_refresh_interval": "{...}",
  "redirect_refresh_threshold": "{...}",
  "failure_refresh_threshold": "...",
  "host_degraded_refresh_threshold": "..."
}
```



1部署redis

redis-cluster-deploy.yaml

kubectl apply -f redis-cluster-deploy.yaml -n istio

```
---
apiVersion: v1
kind: ConfigMap
metadata:
  name: redis-cluster
data:
  update-node.sh: |
    #!/bin/sh
    REDIS_NODES="/data/nodes.conf"
    sed -i -e "/myself/ s/[0-9]\{1,3\}\.[0-9]\{1,3\}\.[0-9]\{1,3\}\.[0-9]\{1,3\}/${POD_IP}/" ${REDIS_NODES}
    exec "$@"
  redis.conf: |+
    cluster-enabled yes
    cluster-require-full-coverage no
    cluster-node-timeout 15000
    cluster-config-file /data/nodes.conf
    cluster-migration-barrier 1
    appendonly yes
    protected-mode no
---
apiVersion: apps/v1
kind: StatefulSet
metadata:
  name: redis-cluster
spec:
  serviceName: redis-cluster
  replicas: 6
  selector:
    matchLabels:
      app: redis-cluster
  template:
    metadata:
      labels:
        app: redis-cluster
    spec:
      containers:
      - name: redis
        image: redis:6.0.8-alpine
        imagePullPolicy: IfNotPresent
        ports:
        - containerPort: 6379
          name: tcp-client
        - containerPort: 16379
          name: tcp-gossip
        command: ["/conf/update-node.sh", "redis-server", "/conf/redis.conf", "--cluster-announce-ip $(POD_IP)"]
        env:
        - name: POD_IP
          valueFrom:
            fieldRef:
              fieldPath: status.podIP
        volumeMounts:
        - name: conf
          mountPath: /conf
          readOnly: false
      volumes:
      - name: conf
        configMap:
          name: redis-cluster
          defaultMode: 0755

---
apiVersion: v1
kind: Service
metadata:
  name: redis-cluster
spec:
  type: ClusterIP
  clusterIP: None
  ports:
  - port: 6379
    targetPort: 6379
    name: tcp-client
  - port: 16379
    targetPort: 16379
    name: tcp-gossip
  selector:
    app: redis-cluster
    
---
apiVersion: v1
kind: Service
metadata:
  name: redis-cluster-op
spec:
  type: ClusterIP
  ports:
  - port: 6379
    targetPort: 6379
    name: tcp-client
  - port: 16379
    targetPort: 16379
    name: tcp-gossip
  selector:
    app: redis-cluster
```

redis-client-deploy.yaml

kubectl apply -f redis-client-deploy.yaml -n istio

```
apiVersion: apps/v1
kind: Deployment
metadata:
  name: redis-client
  labels:
    app: redis-client
spec:
  selector:
    matchLabels:
      app: redis-client
  replicas: 1
  template:
    metadata:
      labels:
        app: redis-client
    spec:
      containers:
      - name: redis-client
        image: redis
        imagePullPolicy: IfNotPresent
```

2设置 envoyfilter cluster

cluster-cluster_type-redis.yaml

kubectl apply -f cluster-cluster_type-redis.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: custom-redis-cluster
  namespace: istio-system
spec:
  configPatches:
  - applyTo: CLUSTER
    patch:
      operation: ADD
      value:
        name: "custom-redis-cluster"
        type: STRICT_DNS
        connect_timeout: 0.5s
        lb_policy: MAGLEV
        load_assignment:
          cluster_name: custom-redis-cluster
          endpoints:
          - lb_endpoints:
            - endpoint:
                address:
                  socket_address:
                    address: redis-cluster-0.redis-cluster.istio.svc.cluster.local
                    port_value: 6379
            - endpoint:
                address:
                  socket_address:
                    address: redis-cluster-1.redis-cluster.istio.svc.cluster.local
                    port_value: 6379
            - endpoint:
                address:
                  socket_address:
                    address: redis-cluster-2.redis-cluster.istio.svc.cluster.local
                    port_value: 6379
            - endpoint:
                address:
                  socket_address:
                    address: redis-cluster-3.redis-cluster.istio.svc.cluster.local
                    port_value: 6379
            - endpoint:
                address:
                  socket_address:
                    address: redis-cluster-4.redis-cluster.istio.svc.cluster.local
                    port_value: 6379
            - endpoint:
                address:
                  socket_address:
                    address: redis-cluster-5.redis-cluster.istio.svc.cluster.local
                    port_value: 6379
        cluster_type:
          name: envoy.clusters.redis
          typed_config:
            "@type": type.googleapis.com/google.protobuf.Struct
            value:
              cluster_refresh_rate: 5s
              cluster_refresh_timeout: 3s
              redirect_refresh_interval: 5s
              redirect_refresh_threshold: 5
```

3设置redis_proxy

envoyfilter-redis-proxy.yaml

kubectl apply -f envoyfilter-redis-proxy.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: add-redis-proxy
  namespace: istio-system
spec:
  configPatches:
  - applyTo: NETWORK_FILTER
    match:
      listener:
        name: 10.68.100.56_6379               # Replace REDIS_VIP with the cluster IP of "redis-cluster service
        filterChain:
          filter:
            name: "envoy.filters.network.tcp_proxy"
    patch:
      operation: REPLACE
      value:
        name: envoy.filters.network.redis_proxy
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.filters.network.redis_proxy.v3.RedisProxy
          stat_prefix: redis_stats
          prefix_routes:
            catch_all_route:
              cluster: custom-redis-cluster
          settings:
            op_timeout: 5s
            enable_redirection: true
            enable_command_stats: true
            read_policy: REPLICA
```

替换REDIS_VIP

4构建redis集群

获取pod ip

 kubectl get pods -l app=redis-cluster -o jsonpath='{range.items[*]}{.status.podIP}:6379 ' -n istio

构建集群

kubectl exec -it  redis-cluster-0 -n istio  -- redis-cli --cluster create --cluster-replicas 1 172.20.0.227:6379 172.20.1.238:6379 172.20.2.209:6379 172.20.0.228:6379 172.20.1.239:6379 172.20.2.210:6379

验证集群是否成功

kubectl exec -it redis-cluster-0 -c redis -n istio -- redis-cli cluster info 

进入客户端

 kubectl exec -it redis-client-6c4b6c4fb5-7hbv9  -n istio -- /bin/bash

连接集群

 redis-cli -h redis-cluster-op  -p 6379 -c

设置数据

set  a a等

验证数据

kubectl exec redis-cluster-0 -c redis -n istio -- redis-cli --scan

```
kubectl delete -f redis-cluster-deploy.yaml -n istio
kubectl delete -f redis-client-deploy.yaml -n istio
kubectl delete envoyfilter custom-redis-cluster -n istio-system
kubectl delete envoyfilter add-redis-proxy -n istio-system
```



## 3.5lb_policy

Refer to [load balancer type](https://www.envoyproxy.io/docs/envoy/latest/intro/arch_overview/upstream/load_balancing/load_balancers#arch-overview-load-balancing-types) architecture overview section for information on each type.

- ROUND_ROBIN

  *(DEFAULT)* ⁣Refer to the [round robin load balancing policy](https://www.envoyproxy.io/docs/envoy/latest/intro/arch_overview/upstream/load_balancing/load_balancers#arch-overview-load-balancing-types-round-robin) for an explanation.

- LEAST_REQUEST

  ⁣Refer to the [least request load balancing policy](https://www.envoyproxy.io/docs/envoy/latest/intro/arch_overview/upstream/load_balancing/load_balancers#arch-overview-load-balancing-types-least-request) for an explanation.

- RING_HASH

  ⁣Refer to the [ring hash load balancing policy](https://www.envoyproxy.io/docs/envoy/latest/intro/arch_overview/upstream/load_balancing/load_balancers#arch-overview-load-balancing-types-ring-hash) for an explanation.

- RANDOM

  ⁣Refer to the [random load balancing policy](https://www.envoyproxy.io/docs/envoy/latest/intro/arch_overview/upstream/load_balancing/load_balancers#arch-overview-load-balancing-types-random) for an explanation.

- MAGLEV

  ⁣Refer to the [Maglev load balancing policy](https://www.envoyproxy.io/docs/envoy/latest/intro/arch_overview/upstream/load_balancing/load_balancers#arch-overview-load-balancing-types-maglev) for an explanation.

- CLUSTER_PROVIDED

  ⁣This load balancer type must be specified if the configured **cluster** provides a cluster specific load balancer. Consult the configured cluster’s documentation for whether to set this option or not.

- LOAD_BALANCING_POLICY_CONFIG

  ⁣Use the new [load_balancing_policy](https://www.envoyproxy.io/docs/envoy/latest/api-v3/config/cluster/v3/cluster.proto.html?highlight=cluster#envoy-v3-api-field-config-cluster-v3-cluster-load-balancing-policy) field to determine the LB policy.

### 3.5.1ROUND_ROBIN

默认

cluster-lb_policy-ROUND_ROBIN.yaml

kubectl apply -f cluster-lb_policy-ROUND_ROBIN.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: cluster
  namespace: istio-system 
spec:
  workloadSelector:
    labels:
      istio: ingressgateway
  configPatches:
  - applyTo: CLUSTER
    match:
      cluster:
        name: outbound|9080||productpage.istio.svc.cluster.local
    patch:
      operation: MERGE
      value:
         lb_policy: ROUND_ROBIN
```

kubectl scale deploy productpage-v1 --replicas=3 -n istio

### 3.5.2LEAST_REQUEST

cluster-lb_policy-LEAST_REQUEST.yaml

kubectl apply -f cluster-lb_policy-LEAST_REQUEST.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: cluster
  namespace: istio-system 
spec:
  workloadSelector:
    labels:
      istio: ingressgateway
  configPatches:
  - applyTo: CLUSTER
    match:
      cluster:
        name: outbound|9080||productpage.istio.svc.cluster.local
    patch:
      operation: MERGE
      value:
         lb_policy: LEAST_REQUEST
```



### 3.5.3RING_HASH

cluster-lb_policy-RING_HASH.yaml

kubectl apply -f cluster-lb_policy-RING_HASH.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: cluster
  namespace: istio-system 
spec:
  workloadSelector:
    labels:
      istio: ingressgateway
  configPatches:
  - applyTo: CLUSTER
    match:
      cluster:
        name: outbound|9080||productpage.istio.svc.cluster.local
    patch:
      operation: MERGE
      value:
         lb_policy: RING_HASH
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
                        case_sensitive: false
                      route:
                        cluster: outbound|9080||productpage.istio.svc.cluster.local
                        hash_policy:
                        - header:
                            header_name: test
```



### 3.5.4RANDOM

cluster-lb_policy-RANDOM.yaml

kubectl apply -f cluster-lb_policy-RANDOM.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: cluster
  namespace: istio-system 
spec:
  workloadSelector:
    labels:
      istio: ingressgateway
  configPatches:
  - applyTo: CLUSTER
    match:
      cluster:
        name: outbound|9080||productpage.istio.svc.cluster.local
    patch:
      operation: MERGE
      value:
         lb_policy: RANDOM
```



### 3.5.5MAGLEV

 Maglev 可以用作环哈希负载均衡器的替代品，可以在任何需要一致性哈希的地方使用。就像环哈希负载均衡器一样，只有在使用指定哈希值的协议路由时，一致性哈希负载均衡器才有效。 

 一般来说，与环形散列（“ketama”）算法相比，Maglev 具有快得多的查表编译时间以及主机选择时间（当使用 256K 条目的大环时大约分别为 10 倍和 5 倍）。Maglev 的缺点是它不像环哈希那样稳定。当主机被移除时，更多的键将移动位置（模拟显示键将移动大约两倍）。据说，对于包括 Redis 在内的许多应用程序来说，Maglev 很可能是环形哈希替代品的一大优势。 

cluster-lb_policy-MAGLEV.yaml

kubectl apply -f cluster-lb_policy-MAGLEV.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: cluster
  namespace: istio-system 
spec:
  workloadSelector:
    labels:
      istio: ingressgateway
  configPatches:
  - applyTo: CLUSTER
    match:
      cluster:
        name: outbound|9080||productpage.istio.svc.cluster.local
    patch:
      operation: MERGE
      value:
         lb_policy: MAGLEV
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
                        case_sensitive: false
                      route:
                        cluster: outbound|9080||productpage.istio.svc.cluster.local
                        hash_policy:
                        - header:
                            header_name: test
```



### 3.5.6CLUSTER_PROVIDED

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: cluster
  namespace: istio-system
spec:
  configPatches:
  - applyTo: CLUSTER
    patch:
      operation: ADD
      value:
        name: "custom-redis-cluster"
        connect_timeout: 0.5s
        lb_policy: CLUSTER_PROVIDED
        load_assignment:
          cluster_name: custom-redis-cluster
          endpoints:
          - lb_endpoints:
            - endpoint:
                address:
                  socket_address:
                    address: redis-cluster-0.redis-cluster.istio.svc.cluster.local
                    port_value: 6379
            - endpoint:
                address:
                  socket_address:
                    address: redis-cluster-1.redis-cluster.istio.svc.cluster.local
                    port_value: 6379
            - endpoint:
                address:
                  socket_address:
                    address: redis-cluster-2.redis-cluster.istio.svc.cluster.local
                    port_value: 6379
            - endpoint:
                address:
                  socket_address:
                    address: redis-cluster-3.redis-cluster.istio.svc.cluster.local
                    port_value: 6379
            - endpoint:
                address:
                  socket_address:
                    address: redis-cluster-4.redis-cluster.istio.svc.cluster.local
                    port_value: 6379
            - endpoint:
                address:
                  socket_address:
                    address: redis-cluster-5.redis-cluster.istio.svc.cluster.local
                    port_value: 6379
        cluster_type:
          name: envoy.clusters.redis
          typed_config:
            "@type": type.googleapis.com/google.protobuf.Struct
            value:
              cluster_refresh_rate: 5s
              cluster_refresh_timeout: 3s
              redirect_refresh_interval: 5s
              redirect_refresh_threshold: 5
```



### 3.5.7LOAD_BALANCING_POLICY_CONFIG

跳过

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: cluster
  namespace: istio-system 
spec:
  workloadSelector:
    labels:
      istio: ingressgateway
  configPatches:
  - applyTo: CLUSTER
    match:
      cluster:
        name: outbound|9080||productpage.istio.svc.cluster.local
    patch:
      operation: MERGE
      value:
         lb_policy: LOAD_BALANCING_POLICY_CONFIG
         load_balancing_policy:
           policies:
           - typed_extension_config:
           
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
                        case_sensitive: false
                      route:
                        cluster: outbound|9080||productpage.istio.svc.cluster.local
                        hash_policy:
                        - header:
                            header_name: test
```



## 3.6load_assignment

```
{
  "cluster_name": "...",
  "endpoints": [],
  "policy": "{...}"
}
```

endpoints：

```
{
  "locality": "{...}",
  "lb_endpoints": [],
  "load_balancer_endpoints": "{...}",
  "leds_cluster_locality_config": "{...}",
  "load_balancing_weight": "{...}",
  "priority": "..."
}
```

lb_endpoints：

```
{
  "endpoint": "{...}",
  "health_status": "...",
  "metadata": "{...}",
  "load_balancing_weight": "{...}"
}
```

endpoint：

```
{
  "address": "{...}",
  "health_check_config": "{...}",
  "hostname": "..."
}
```

locality：

```
{
  "region": "...",
  "zone": "...",
  "sub_zone": "..."
}
```

policy：

```
{
  "overprovisioning_factor": "{...}",
  "endpoint_stale_after": "{...}"
}
```

部署ratelimit

1创建cm

```
cat << EOF > ratelimit-config.yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: ratelimit-config
data:
  config.yaml: |
    domain: productpage-ratelimit
    descriptors:
      - key: PATH
        value: "/productpage"
        rate_limit:
          unit: minute
          requests_per_unit: 1
      - key: PATH
        rate_limit:
          unit: minute
          requests_per_unit: 100
EOF

kubectl apply -f ratelimit-config.yaml -n istio
```

说明: 这个configmap是限速服务用到的配置文件，他是envoy v3版本的限速格式。domain是域名，他会在envoyfilter中被引用，descriptors的PATH,表示请求的路径可以有多个值，rate_limit配置限速配额，这里productpage配了1分钟1个请求，其他url是1分钟100个请求



2创建限速服务deployment

```
cat << EOF > ratelimit-deploy.yaml
apiVersion: v1
kind: Service
metadata:
  name: redis
  labels:
    app: redis
spec:
  ports:
  - name: redis
    port: 6379
  selector:
    app: redis
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: redis
spec:
  replicas: 1
  selector:
    matchLabels:
      app: redis
  template:
    metadata:
      labels:
        app: redis
    spec:
      containers:
      - image: redis:alpine
        imagePullPolicy: Always
        name: redis
        ports:
        - name: redis
          containerPort: 6379
      restartPolicy: Always
      serviceAccountName: ""
---
apiVersion: v1
kind: Service
metadata:
  name: ratelimit
  labels:
    app: ratelimit
spec:
  ports:
  - name: http-port
    port: 8080
    targetPort: 8080
    protocol: TCP
  - name: grpc-port
    port: 8081
    targetPort: 8081
    protocol: TCP
  - name: http-debug
    port: 6070
    targetPort: 6070
    protocol: TCP
  selector:
    app: ratelimit
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: ratelimit
spec:
  replicas: 1
  selector:
    matchLabels:
      app: ratelimit
  strategy:
    type: Recreate
  template:
    metadata:
      labels:
        app: ratelimit
    spec:
      containers:
      - image: envoyproxy/ratelimit:6f5de117 # 2021/01/08
        imagePullPolicy: Always
        name: ratelimit
        command: ["/bin/ratelimit"]
        env:
        - name: LOG_LEVEL
          value: debug
        - name: REDIS_SOCKET_TYPE
          value: tcp
        - name: REDIS_URL
          value: redis:6379
        - name: USE_STATSD
          value: "false"
        - name: RUNTIME_ROOT
          value: /data
        - name: RUNTIME_SUBDIRECTORY
          value: ratelimit
        ports:
        - containerPort: 8080
        - containerPort: 8081
        - containerPort: 6070
        volumeMounts:
        - name: config-volume
          mountPath: /data/ratelimit/config/config.yaml
          subPath: config.yaml
      volumes:
      - name: config-volume
        configMap:
          name: ratelimit-config
EOF

kubectl apply -f ratelimit-deploy.yaml -n istio
```

创建了redis，和官方的一个ratelimit服务。



cluster-load_assignment.yaml

kubectl apply -f cluster-load_assignment.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: filter-ratelimit
  namespace: istio-system
spec:
  workloadSelector:
    # select by label in the same namespace
    labels:
      istio: ingressgateway
  configPatches:
    # The Envoy config you want to modify
    - applyTo: HTTP_FILTER
      match:
        context: GATEWAY
        listener:
          filterChain:
            filter:
              name: "envoy.filters.network.http_connection_manager"
              subFilter:
                name: "envoy.filters.http.router"
      patch:
        operation: INSERT_BEFORE
        # Adds the Envoy Rate Limit Filter in HTTP filter chain.
        value:
          name: envoy.filters.http.ratelimit
          typed_config:
            "@type": type.googleapis.com/envoy.extensions.filters.http.ratelimit.v3.RateLimit
            # domain can be anything! Match it to the ratelimter service config
            domain: productpage-ratelimit
            failure_mode_deny: true
            rate_limit_service:
              grpc_service:
                envoy_grpc:
                  cluster_name: rate_limit_cluster
                timeout: 10s
              transport_api_version: V3
    - applyTo: CLUSTER
      match:
        cluster:
          service: ratelimit.istio.svc.cluster.local
      patch:
        operation: ADD
        # Adds the rate limit service cluster for rate limit service defined in step 1.
        value:
          name: rate_limit_cluster
          type: STRICT_DNS
          connect_timeout: 10s
          lb_policy: ROUND_ROBIN
          http2_protocol_options: {}
          load_assignment:
            cluster_name: rate_limit_cluster
            policy:
              overprovisioning_factor: 140
              endpoint_stale_after: 60s
            endpoints:
            - lb_endpoints:
              - endpoint:
                  address:
                     socket_address:
                      address: ratelimit.istio.svc.cluster.local
                      port_value: 8081
                metadata:
                  filter_metadata:
                    envoy.lb:
                      test: test
                load_balancing_weight: 100
              locality:
                region: ch-beijin
                zone: zone-1
                sub_zone: sub-zone-1
              load_balancing_weight: 100
              priority: 0
              
```

4创建action envoyfilter

```
cat << EOF > envoyfilter-action.yaml 
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: filter-ratelimit-svc
  namespace: istio-system
spec:
  workloadSelector:
    labels:
      istio: ingressgateway
  configPatches:
    - applyTo: VIRTUAL_HOST
      match:
        context: GATEWAY
        routeConfiguration:
          vhost:
            name: "*:80"
            route:
              action: ANY
      patch:
        operation: MERGE
        # Applies the rate limit rules.
        value:
          rate_limits:
            - actions: # any actions in here
              - request_headers:
                  header_name: ":path"
                  descriptor_key: "PATH"
EOF

kubectl apply -f envoyfilter-action.yaml  -n istio-system
```

这个envoyfilter作用在入口网关处，给80端口的虚拟主机配置了一个rate_limits 动作，descriptor_key用于选择在configmap里配置的key。

```
kubectl delete cm ratelimit-config -n istio
kubectl delete -f ratelimit-deploy.yaml -n istio
kubectl delete envoyfilter filter-ratelimit -n istio-system
kubectl delete envoyfilter filter-ratelimit-svc -n istio-system
```



## 3.7health_checks

```
{
  "timeout": "{...}",超时时间
  "interval": "{...}",检测间隔
  "initial_jitter": "{...}",初始抖动
  "interval_jitter": "{...}",间隔抖动
  "interval_jitter_percent": "...",间隔抖动比例
  "unhealthy_threshold": "{...}",不健康阈值
  "healthy_threshold": "{...}",健康阈值
  "reuse_connection": "{...}",重用连接
  "http_health_check": "{...}",http类型健康检测
  "tcp_health_check": "{...}",tcp类型健康检测
  "grpc_health_check": "{...}",grpc类型健康检测
  "custom_health_check": "{...}",自定义健康检测
  "no_traffic_interval": "{...}",没有流量时的间隔
  "no_traffic_healthy_interval": "{...}",没有流量健康后的间隔
  "unhealthy_interval": "{...}",不健康的间隔
  "unhealthy_edge_interval": "{...}",不健康边缘间隔
  "healthy_edge_interval": "{...}",健康边缘间隔
  "event_log_path": "...",日志路径
  "always_log_health_check_failures": "...",失败总是记录日志
  "tls_options": "{...}",选项
  "transport_socket_match_criteria": "{...}"trasport_socket匹配条件
}
```

http_health_check:

```
{
  "host": "...",
  "path": "...",
  "request_headers_to_add": [],
  "request_headers_to_remove": [],
  "expected_statuses": [],
  "retriable_statuses": [],
  "codec_client_type": "...",
  "service_name_matcher": "{...}"
}
```

tcp_health_check:

```
{
  "send": "{...}",
  "receive": []
}
```

grpc_health_check:

```
{
  "service_name": "...",
  "authority": "..."
}
```

custom_health_check:

```
{
  "name": "...",
  "typed_config": "{...}"
}
```

- [envoy.health_checkers.redis](https://www.envoyproxy.io/docs/envoy/latest/api-v3/extensions/health_checkers/redis/v3/redis.proto#extension-envoy-health-checkers-redis)

service_name_matcher:

 The Envoy HTTP health checker supports the [service_name_matcher](https://www.envoyproxy.io/docs/envoy/latest/api-v3/config/core/v3/health_check.proto#envoy-v3-api-field-config-core-v3-healthcheck-httphealthcheck-service-name-matcher) option. If this option is set, the health checker additionally compares the value of the *x-envoy-upstream-healthchecked-cluster* response header to *service_name_matcher*. If the values do not match, the health check does not pass. The upstream health check filter appends *x-envoy-upstream-healthchecked-cluster* to the response headers. The appended value is determined by the [`--service-cluster`](https://www.envoyproxy.io/docs/envoy/latest/operations/cli#cmdoption-service-cluster) command line option. 

```
{
  "exact": "...",
  "prefix": "...",
  "suffix": "...",
  "safe_regex": "{...}",
  "contains": "...",
  "ignore_case": "..."
}
```



### 3.7.1http_health_check

cluster-health_checks-http.yaml

kubectl apply -f cluster-health_checks-http.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: cluster
  namespace: istio-system
spec:
  workloadSelector:
    labels:
      istio: ingressgateway
  configPatches:
  - applyTo: NETWORK_FILTER
    match:
      listener:
        #name: 0.0.0.0_8080  
        portNumber: 8080
        filterChain:
          filter:
            name: "envoy.filters.network.http_connection_manager"
    patch:
      operation: MERGE
      value:
        name: envoy.filters.network.http_connection_manager
        typed_config:
          "@type": "type.googleapis.com/envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager"
          codec_type: AUTO
          stat_prefix: ingress_http
          route_config:
            name: http.8080
            virtual_hosts:
            - name: “*.8080”
              domains:
              - "*"
              routes:
              - match:
                  caseSensitive: true
                  headers:
                  - name: :method
                    safeRegexMatch:
                      googleRe2: {}
                      regex: G.*T
                  prefix: /
                route:
                  cluster: productpage_cluster
  - applyTo: CLUSTER
    patch:
      operation: ADD
      value:
          name: productpage_cluster
          type: STRICT_DNS
          connect_timeout: 10s
          lb_policy: ROUND_ROBIN
          health_checks:
          - timeout: 10s
            interval: 20s
            initial_jitter: 1s
            interval_jitter: 1s
            interval_jitter_percent: 50
            unhealthy_threshold: 5
            healthy_threshold: 3
            reuse_connection: true
            no_traffic_interval: 60s
            no_traffic_healthy_interval: 100s
            unhealthy_interval: 10s
            unhealthy_edge_interval: 30s
            healthy_edge_interval: 30s
            always_log_health_check_failures: false
            tls_options:
              alpn_protocols:
              - http1.1
              - h2
            transport_socket_match_criteria:
              useMTLS: false
            http_health_check:
              host: "productpage.istio.svc.cluster.local:9080"
              path: /productpage
              request_headers_to_add:
              - header:
                  key: test
                  value: test
                append: true
              request_headers_to_remove:
              - test2
              expected_statuses:
              - start: 200
                end: 201
              #retriable_statuses:
              #- start: 401
              #  end: 500
              codec_client_type: HTTP1
              service_name_matcher:
                prefix: "product"
          load_assignment:
            cluster_name: productpage_cluster
            endpoints:
            - lb_endpoints:
              - endpoint:
                  address:
                     socket_address:
                      address: productpage.istio.svc.cluster.local
                      port_value: 9080
```

### 3.7.2tcp_health_check

deploy-tcp-echo.yaml

kubectl apply -f deploy-tcp-echo.yaml -n istio

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



cluster-health_checks-tcp.yaml

kubectl apply -f cluster-health_checks-tcp.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: cluster
  namespace: istio-system
spec:
  workloadSelector:
    labels:
      istio: ingressgateway
  configPatches:
  - applyTo: LISTENER
    match:
      context: GATEWAY
    patch:
      operation: ADD
      value:
        name: proxy
        address:
          socket_address:
            protocol: TCP
            address: 0.0.0.0
            port_value: 31400
        traffic_direction: "OUTBOUND"
        filter_chains:
        - filters:
          - name: envoy.filters.network.tcp_proxy
            typedConfig:
              '@type': type.googleapis.com/envoy.extensions.filters.network.tcp_proxy.v3.TcpProxy
              cluster: tcp-echo_cluster
              statPrefix: tcp-echo_cluster
  - applyTo: CLUSTER
    patch:
      operation: ADD
      value:
          name: tcp-echo_cluster
          type: STRICT_DNS
          connect_timeout: 10s
          lb_policy: ROUND_ROBIN
          http2_protocol_options: {}
          health_checks:
          - timeout: 10s
            interval: 20s
            initial_jitter: 1s
            interval_jitter: 1s
            interval_jitter_percent: 50
            unhealthy_threshold: 5
            healthy_threshold: 3
            reuse_connection: true
            no_traffic_interval: 60s
            no_traffic_healthy_interval: 100s
            unhealthy_interval: 10s
            unhealthy_edge_interval: 30s
            healthy_edge_interval: 30s
            always_log_health_check_failures: false
            tls_options:
              alpn_protocols:
              - http1.1
              - h2
            transport_socket_match_criteria:
              useMTLS: false
            tcp_health_check:
              send:
                text: 000000FF
              receive: 
              - text: 000000FF
          load_assignment:
            cluster_name: tcp-echo_cluster
            endpoints:
            - lb_endpoints:
              - endpoint:
                  address:
                     socket_address:
                      address: tcp-echo.istio.svc.cluster.local
                      port_value: 9000
```



### 3.7.3 custom_health_check

If set, optionally perform `EXISTS <key>` instead of `PING`. A return value from Redis of 0 (does not exist) is considered a passing healthcheck. A return value other than 0 is considered a failure. This allows the user to mark a Redis instance for maintenance by setting the specified key to any value and waiting for traffic to drain.



1部署redis

redis-cluster-deploy.yaml

kubectl apply -f redis-cluster-deploy.yaml -n istio

```
---
apiVersion: v1
kind: ConfigMap
metadata:
  name: redis-cluster
data:
  update-node.sh: |
    #!/bin/sh
    REDIS_NODES="/data/nodes.conf"
    sed -i -e "/myself/ s/[0-9]\{1,3\}\.[0-9]\{1,3\}\.[0-9]\{1,3\}\.[0-9]\{1,3\}/${POD_IP}/" ${REDIS_NODES}
    exec "$@"
  redis.conf: |+
    cluster-enabled yes
    cluster-require-full-coverage no
    cluster-node-timeout 15000
    cluster-config-file /data/nodes.conf
    cluster-migration-barrier 1
    appendonly yes
    protected-mode no
---
apiVersion: apps/v1
kind: StatefulSet
metadata:
  name: redis-cluster
spec:
  serviceName: redis-cluster
  replicas: 6
  selector:
    matchLabels:
      app: redis-cluster
  template:
    metadata:
      labels:
        app: redis-cluster
    spec:
      containers:
      - name: redis
        image: redis:6.0.8-alpine
        imagePullPolicy: IfNotPresent
        ports:
        - containerPort: 6379
          name: tcp-client
        - containerPort: 16379
          name: tcp-gossip
        command: ["/conf/update-node.sh", "redis-server", "/conf/redis.conf", "--cluster-announce-ip $(POD_IP)"]
        env:
        - name: POD_IP
          valueFrom:
            fieldRef:
              fieldPath: status.podIP
        volumeMounts:
        - name: conf
          mountPath: /conf
          readOnly: false
      volumes:
      - name: conf
        configMap:
          name: redis-cluster
          defaultMode: 0755

---
apiVersion: v1
kind: Service
metadata:
  name: redis-cluster
spec:
  type: ClusterIP
  clusterIP: None
  ports:
  - port: 6379
    targetPort: 6379
    name: tcp-client
  - port: 16379
    targetPort: 16379
    name: tcp-gossip
  selector:
    app: redis-cluster
    
---
apiVersion: v1
kind: Service
metadata:
  name: redis-cluster-op
spec:
  type: ClusterIP
  ports:
  - port: 6379
    targetPort: 6379
    name: tcp-client
  - port: 16379
    targetPort: 16379
    name: tcp-gossip
  selector:
    app: redis-cluster
```

redis-client-deploy.yaml

kubectl apply -f redis-client-deploy.yaml -n istio

```
apiVersion: apps/v1
kind: Deployment
metadata:
  name: redis-client
  labels:
    app: redis-client
spec:
  selector:
    matchLabels:
      app: redis-client
  replicas: 1
  template:
    metadata:
      labels:
        app: redis-client
    spec:
      containers:
      - name: redis-client
        image: redis
        imagePullPolicy: IfNotPresent
```

2设置 envoyfilter cluster

cluster-health_checks-custom.yaml

kubectl apply -f cluster-health_checks-custom.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: custom-redis-cluster
  namespace: istio-system
spec:
  configPatches:
  - applyTo: CLUSTER
    patch:
      operation: ADD
      value:
        name: "custom-redis-cluster"
        type: STRICT_DNS
        connect_timeout: 0.5s
        lb_policy: MAGLEV
        health_checks:
          - timeout: 10s
            interval: 20s
            initial_jitter: 1s
            interval_jitter: 1s
            interval_jitter_percent: 50
            unhealthy_threshold: 5
            healthy_threshold: 3
            reuse_connection: true
            no_traffic_interval: 60s
            no_traffic_healthy_interval: 100s
            unhealthy_interval: 10s
            unhealthy_edge_interval: 30s
            healthy_edge_interval: 30s
            always_log_health_check_failures: false
            tls_options:
              alpn_protocols:
              - http1.1
              - h2
            transport_socket_match_criteria:
              useMTLS: true
            custom_health_check:
              name: envoy.health_checkers.redis
              typed_config:
                "@type": type.googleapis.com/envoy.extensions.health_checkers.redis.v3.Redis
                key: test
        load_assignment:
          cluster_name: custom-redis-cluster
          endpoints:
          - lb_endpoints:
            - endpoint:
                address:
                  socket_address:
                    address: redis-cluster-0.redis-cluster.istio.svc.cluster.local
                    port_value: 6379
            - endpoint:
                address:
                  socket_address:
                    address: redis-cluster-1.redis-cluster.istio.svc.cluster.local
                    port_value: 6379
            - endpoint:
                address:
                  socket_address:
                    address: redis-cluster-2.redis-cluster.istio.svc.cluster.local
                    port_value: 6379
            - endpoint:
                address:
                  socket_address:
                    address: redis-cluster-3.redis-cluster.istio.svc.cluster.local
                    port_value: 6379
            - endpoint:
                address:
                  socket_address:
                    address: redis-cluster-4.redis-cluster.istio.svc.cluster.local
                    port_value: 6379
            - endpoint:
                address:
                  socket_address:
                    address: redis-cluster-5.redis-cluster.istio.svc.cluster.local
                    port_value: 6379
        cluster_type:
          name: envoy.clusters.redis
          typed_config:
            "@type": type.googleapis.com/google.protobuf.Struct
            value:
              cluster_refresh_rate: 5s
              cluster_refresh_timeout: 3s
              redirect_refresh_interval: 5s
              redirect_refresh_threshold: 5
```

3设置redis_proxy

envoyfilter-redis-proxy.yaml

kubectl apply -f envoyfilter-redis-proxy.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: add-redis-proxy
  namespace: istio-system
spec:
  configPatches:
  - applyTo: NETWORK_FILTER
    match:
      listener:
        name: 10.68.187.93_6379               # Replace REDIS_VIP with the cluster IP of "redis-cluster service
        filterChain:
          filter:
            name: "envoy.filters.network.tcp_proxy"
    patch:
      operation: REPLACE
      value:
        name: envoy.filters.network.redis_proxy
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.filters.network.redis_proxy.v3.RedisProxy
          stat_prefix: redis_stats
          prefix_routes:
            catch_all_route:
              cluster: custom-redis-cluster
          settings:
            op_timeout: 5s
            enable_redirection: true
            enable_command_stats: true
            read_policy: REPLICA
```

替换REDIS_VIP

4构建redis集群

获取pod ip

 kubectl get pods -l app=redis-cluster -o jsonpath='{range.items[*]}{.status.podIP}:6379 ' -n istio

构建集群

kubectl exec -it  redis-cluster-0 -n istio  -- redis-cli --cluster create --cluster-replicas 1 172.20.0.227:6379 172.20.1.238:6379 172.20.2.209:6379 172.20.0.228:6379 172.20.1.239:6379 172.20.2.210:6379

验证集群是否成功

kubectl exec -it redis-cluster-0 -c redis -n istio -- redis-cli cluster info 

进入客户端

 kubectl exec -it redis-client-6c4b6c4fb5-7hbv9  -n istio -- /bin/bash

连接集群

 redis-cli -h redis-cluster-op  -p 6379 -c

设置数据

set  a a等

验证数据

kubectl exec redis-cluster-0 -c redis -n istio -- redis-cli --scan

```
kubectl delete -f redis-cluster-deploy.yaml -n istio
kubectl delete -f redis-client-deploy.yaml -n istio
kubectl delete envoyfilter custom-redis-cluster -n istio-system
kubectl delete envoyfilter add-redis-proxy -n istio-system
```



## 3.8circuit_breakers

```
{
  "thresholds": []
}
```

thresholds:

```
{
  "priority": "...",路由优先级
  "max_connections": "{...}",最大连接数，默认1024
  "max_pending_requests": "{...}",最大等待请求数，默认1024
  "max_requests": "{...}",最大并发请求数，默认1024
  "max_retries": "{...}",最大并发重试次数，默认3
  "retry_budget": "{...}",根据活动请求的并发重试配置
  "track_remaining": "...",暴露资源存量的stat
  "max_connection_pools": "{...}"最大连接池数量
}
```



retry_budget：

```
{
  "budget_percent": "{...}",活动请求的百分比
  "min_retry_concurrency": "{...}"最小并发重试，默认3
}
```

cluster-circuit_breakers.yaml

kubectl apply -f cluster-circuit_breakers.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: cluster
  namespace: istio-system
spec:
  workloadSelector:
    labels:
      istio: ingressgateway
  configPatches:
    - applyTo: CLUSTER
      patch:
        operation: ADD
        value:
          name: productpage_cluster
          type: STRICT_DNS
          connect_timeout: 10s
          lb_policy: ROUND_ROBIN
          http2_protocol_options: {}
          circuit_breakers:
            thresholds:
            - priority: HIGH
              max_connections: 1024
              max_pending_requests: 1024
              max_requests: 1024
              max_retries: 3
              retry_budget:
                budget_percent:
                  value: 30
                min_retry_concurrency: 3
              track_remaining: true
              max_connection_pools: 1024
          load_assignment:
            cluster_name: productpage_cluster
            endpoints:
            - lb_endpoints:
              - endpoint:
                  address:
                     socket_address:
                      address: productpage.istio.svc.cluster.local
                      port_value: 9080
```



## 3.9upstream_http_protocol_options

```
{
  "auto_sni": "...",自动设置sni
  "auto_san_validation": "...",自动san验证
  "override_auto_sni_header": "..."覆盖host/authority 头,还不支持
}
```

cluster-upstream_http_protocol_options.yaml

kubectl apply -f cluster-upstream_http_protocol_options.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: cluster
  namespace: istio-system
spec:
  workloadSelector:
    labels:
      istio: ingressgateway
  configPatches:
  - applyTo: NETWORK_FILTER
    match:
      listener:
        #name: 0.0.0.0_8080  
        portNumber: 8080
        filterChain:
          filter:
            name: "envoy.filters.network.http_connection_manager"
    patch:
      operation: MERGE
      value:
        name: envoy.filters.network.http_connection_manager
        typed_config:
          "@type": "type.googleapis.com/envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager"
          codec_type: AUTO
          stat_prefix: ingress_http
          route_config:
            name: http.8080
            virtual_hosts:
            - name: “*.8080”
              domains:
              - "*"
              routes:
              - match:
                  caseSensitive: true
                  headers:
                  - name: :method
                    safeRegexMatch:
                      googleRe2: {}
                      regex: G.*T
                  prefix: /
                route:
                  cluster: productpage_cluster
  - applyTo: CLUSTER
    patch:
      operation: ADD
      value:
          name: productpage_cluster
          type: STRICT_DNS
          connect_timeout: 10s
          lb_policy: ROUND_ROBIN
          http2_protocol_options: {}
          upstream_http_protocol_options:
            auto_sni: true
            auto_san_validation: true
            #override_auto_sni_header: my-header
          load_assignment:
            cluster_name: productpage_cluster
            endpoints:
            - lb_endpoints:
              - endpoint:
                  address:
                     socket_address:
                      address: productpage.istio.svc.cluster.local
                      port_value: 9080
```

## 3.10common_http_protocol_options

```
{
  "idle_timeout": "{...}",空闲超时时间
  "max_connection_duration": "{...}",最大连接时间
  "max_headers_count": "{...}",最大头数量
  "max_stream_duration": "{...}",最大流时间
  "headers_with_underscores_action": "...",带有下划线的头处理动作
  "max_requests_per_connection": "{...}"每个连接的最大请求数量,istio未实现
}
```

cluster-common_http_protocol_options.yaml

kubectl apply -f cluster-common_http_protocol_options.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: cluster
  namespace: istio-system
spec:
  workloadSelector:
    labels:
      istio: ingressgateway
  configPatches:
  - applyTo: NETWORK_FILTER
    match:
      listener:
        #name: 0.0.0.0_8080  
        portNumber: 8080
        filterChain:
          filter:
            name: "envoy.filters.network.http_connection_manager"
    patch:
      operation: MERGE
      value:
        name: envoy.filters.network.http_connection_manager
        typed_config:
          "@type": "type.googleapis.com/envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager"
          codec_type: AUTO
          stat_prefix: ingress_http
          route_config:
            name: http.8080
            virtual_hosts:
            - name: “*.8080”
              domains:
              - "*"
              routes:
              - match:
                  caseSensitive: true
                  headers:
                  - name: :method
                    safeRegexMatch:
                      googleRe2: {}
                      regex: G.*T
                  prefix: /
                route:
                  cluster: productpage_cluster
  - applyTo: CLUSTER
    patch:
      operation: ADD
      value:
          name: productpage_cluster
          type: STRICT_DNS
          connect_timeout: 10s
          lb_policy: ROUND_ROBIN
          http2_protocol_options: {}
          common_http_protocol_options:
            idle_timeout: 1h
            max_connection_duration: 10m
            max_headers_count: 100
            max_stream_duration: 1m
            headers_with_underscores_action: ALLOW
            #max_requests_per_connection: 1024
          load_assignment:
            cluster_name: productpage_cluster
            endpoints:
            - lb_endpoints:
              - endpoint:
                  address:
                     socket_address:
                      address: productpage.istio.svc.cluster.local
                      port_value: 9080
```



## 3.11http_protocol_options

```
{
  "allow_absolute_url": "{...}",是否允许完全url
  "accept_http_10": "...",是否接受http1.0 0.9连接
  "default_host_for_http_10": "...",http1.0的默认host
  "header_key_format": "{...}",头键格式
  "enable_trailers": "...",是否启用trailer
  "allow_chunked_length": "...",是否允许chunked长度
  "override_stream_error_on_invalid_http_message": "{...}"http错误时覆盖流错误
}
```

header_key_format:

```
{
  "proper_case_words": "{...}",首字母大写
  "stateful_formatter": "{...}"
}
```

 stateful_formatter：

- [envoy.http.stateful_header_formatters.preserve_case](https://www.envoyproxy.io/docs/envoy/latest/api-v3/extensions/http/header_formatters/preserve_case/v3/preserve_case.proto#extension-envoy-http-stateful-header-formatters-preserve-case)

cluster-http_protocol_options.yaml

kubectl apply -f cluster-http_protocol_options.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: cluster
  namespace: istio-system
spec:
  workloadSelector:
    labels:
      istio: ingressgateway
  configPatches:
  - applyTo: NETWORK_FILTER
    match:
      listener:
        #name: 0.0.0.0_8080  
        portNumber: 8080
        filterChain:
          filter:
            name: "envoy.filters.network.http_connection_manager"
    patch:
      operation: MERGE
      value:
        name: envoy.filters.network.http_connection_manager
        typed_config:
          "@type": "type.googleapis.com/envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager"
          codec_type: AUTO
          stat_prefix: ingress_http
          route_config:
            name: http.8080
            virtual_hosts:
            - name: “*.8080”
              domains:
              - "*"
              routes:
              - match:
                  caseSensitive: true
                  headers:
                  - name: :method
                    safeRegexMatch:
                      googleRe2: {}
                      regex: G.*T
                  prefix: /
                route:
                  cluster: productpage_cluster
  - applyTo: CLUSTER
    patch:
      operation: ADD
      value:
          name: productpage_cluster
          type: STRICT_DNS
          connect_timeout: 10s
          lb_policy: ROUND_ROBIN
          http_protocol_options:
            allow_absolute_url: true
            accept_http_10: true
            default_host_for_http_10: productpage
            header_key_format:
              proper_case_words: {}
              stateful_formatter:
                name: nvoy.http.stateful_header_formatters.preserve_case
                typed_config:
                  '@type': type.googleapis.com/envoy.extensions.http.header_formatters.preserve_case.v3.PreserveCaseFormatterConfig
            enable_trailers: true
            allow_chunked_length: true
            override_stream_error_on_invalid_http_message: true
          load_assignment:
            cluster_name: productpage_cluster
            endpoints:
            - lb_endpoints:
              - endpoint:
                  address:
                     socket_address:
                      address: productpage.istio.svc.cluster.local
                      port_value: 9080
```





## 3.12http2_protocol_options

```
{
  "hpack_table_size": "{...}",hpack表大小
  "max_concurrent_streams": "{...}",最大并发流
  "initial_stream_window_size": "{...}",初始流窗口大小
  "initial_connection_window_size": "{...}",初始连接窗口大小
  "allow_connect": "...",是否允许升级连接
  "max_outbound_frames": "{...}",最大出口帧
  "max_outbound_control_frames": "{...}",最大出口控制帧
  "max_consecutive_inbound_frames_with_empty_payload": "{...}",带有空payload的最大连续入口帧
  "max_inbound_priority_frames_per_stream": "{...}",每个流最大入口优先帧
  "max_inbound_window_update_frames_per_data_frame_sent": "{...}",每个数据帧发送的最大窗口更新帧
  "stream_error_on_invalid_http_messaging": "...",错误http消息流错误,已废弃
  "override_stream_error_on_invalid_http_message": "{...}",http消息错误覆盖流错误
  "connection_keepalive": "{...}"保持连接配置
}
```

connection_keepalive：

```
{
  "interval": "{...}",检测周期
  "timeout": "{...}",超时时间
  "interval_jitter": "{...}",周期性抖动
  "connection_idle_interval": "{...}"连接空闲检测周期
}
```

cluster-http2_protocol_options.yaml

kubectl apply -f cluster-http2_protocol_options.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: cluster
  namespace: istio-system
spec:
  workloadSelector:
    labels:
      istio: ingressgateway
  configPatches:
  - applyTo: NETWORK_FILTER
    match:
      listener:
        #name: 0.0.0.0_8080  
        portNumber: 8080
        filterChain:
          filter:
            name: "envoy.filters.network.http_connection_manager"
    patch:
      operation: MERGE
      value:
        name: envoy.filters.network.http_connection_manager
        typed_config:
          "@type": "type.googleapis.com/envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager"
          codec_type: AUTO
          stat_prefix: ingress_http
          route_config:
            name: http.8080
            virtual_hosts:
            - name: “*.8080”
              domains:
              - "*"
              routes:
              - match:
                  caseSensitive: true
                  headers:
                  - name: :method
                    safeRegexMatch:
                      googleRe2: {}
                      regex: G.*T
                  prefix: /
                route:
                  cluster: productpage_cluster
  - applyTo: CLUSTER
    patch:
      operation: ADD
      value:
          name: productpage_cluster
          type: STRICT_DNS
          connect_timeout: 10s
          lb_policy: ROUND_ROBIN
          http2_protocol_options: {}
          http2_protocol_options:
            hpack_table_size: 4096
            max_concurrent_streams: 2147483647
            initial_stream_window_size: 268435456 
            initial_connection_window_size: 268435456
            allow_connect: true
            max_outbound_frames: 10000
            max_outbound_control_frames: 1000
            max_consecutive_inbound_frames_with_empty_payload: 1
            max_inbound_priority_frames_per_stream: 100
            max_inbound_window_update_frames_per_data_frame_sent: 10
            stream_error_on_invalid_http_messaging: true
            override_stream_error_on_invalid_http_message: true
            connection_keepalive:
              interval: 10s
              timeout: 10s
              interval_jitter: 
                value: 30
              connection_idle_interval: 30s
          load_assignment:
            cluster_name: productpage_cluster
            endpoints:
            - lb_endpoints:
              - endpoint:
                  address:
                     socket_address:
                      address: productpage.istio.svc.cluster.local
                      port_value: 9080
```

## 3.13typed_extension_protocol_options

cluster-typed_extension_protocol_options.yaml

kubectl apply -f cluster-typed_extension_protocol_options.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: cluster
  namespace: istio-system
spec:
  workloadSelector:
    labels:
      istio: ingressgateway
  configPatches:
  - applyTo: NETWORK_FILTER
    match:
      listener:
        #name: 0.0.0.0_8080  
        portNumber: 8080
        filterChain:
          filter:
            name: "envoy.filters.network.http_connection_manager"
    patch:
      operation: MERGE
      value:
        name: envoy.filters.network.http_connection_manager
        typed_config:
          "@type": "type.googleapis.com/envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager"
          codec_type: AUTO
          stat_prefix: ingress_http
          route_config:
            name: http.8080
            virtual_hosts:
            - name: “*.8080”
              domains:
              - "*"
              routes:
              - match:
                  caseSensitive: true
                  headers:
                  - name: :method
                    safeRegexMatch:
                      googleRe2: {}
                      regex: G.*T
                  prefix: /
                route:
                  cluster: productpage_cluster
  - applyTo: CLUSTER
    patch:
      operation: ADD
      value:
          name: productpage_cluster
          type: STRICT_DNS
          connect_timeout: 10s
          lb_policy: ROUND_ROBIN
          typed_extension_protocol_options: 
            envoy.extensions.upstreams.http.v3.HttpProtocolOptions:
                "@type": type.googleapis.com/envoy.extensions.upstreams.http.v3.HttpProtocolOptions
                upstream_http_protocol_options:
                  auto_sni: true
                common_http_protocol_options:
                  idle_timeout: 1s
                explicit_http_config:
                  http2_protocol_options:
                    max_concurrent_streams: 100
          load_assignment:
            cluster_name: productpage_cluster
            endpoints:
            - lb_endpoints:
              - endpoint:
                  address:
                     socket_address:
                      address: productpage.istio.svc.cluster.local
                      port_value: 9080
```

## 3.14dns

dns_failure_refresh_rate：

```
{
  "base_interval": "{...}",基础间隔
  "max_interval": "{...}"最大间隔
}
```

dns_resolution_config：

```
{
  "resolvers": [],dns服务器地址
  "dns_resolver_options": "{...}"解析选项
}
```

dns_resolver_options:

```
{
  "use_tcp_for_dns_lookups": "...",使用tcp协议查找
  "no_default_search_domain": "..."没有默认查找域名
}
```

cluster-dns.yaml

kubectl apply -f cluster-dns.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: cluster
  namespace: istio-system
spec:
  workloadSelector:
    labels:
      istio: ingressgateway
  configPatches:
  - applyTo: NETWORK_FILTER
    match:
      listener:
        #name: 0.0.0.0_8080  
        portNumber: 8080
        filterChain:
          filter:
            name: "envoy.filters.network.http_connection_manager"
    patch:
      operation: MERGE
      value:
        name: envoy.filters.network.http_connection_manager
        typed_config:
          "@type": "type.googleapis.com/envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager"
          codec_type: AUTO
          stat_prefix: ingress_http
          route_config:
            name: http.8080
            virtual_hosts:
            - name: “*.8080”
              domains:
              - "*"
              routes:
              - match:
                  caseSensitive: true
                  headers:
                  - name: :method
                    safeRegexMatch:
                      googleRe2: {}
                      regex: G.*T
                  prefix: /
                route:
                  cluster: productpage_cluster
  - applyTo: CLUSTER
    patch:
      operation: ADD
      value:
          name: productpage_cluster
          type: STRICT_DNS
          connect_timeout: 10s
          lb_policy: ROUND_ROBIN
          dns_refresh_rate: 5000ms
          dns_failure_refresh_rate:
            base_interval: 5000ms
            max_interval: 50000ms
          respect_dns_ttl: true
          dns_lookup_family: AUTO
          #dns_resolvers:已废弃
          use_tcp_for_dns_lookups: true
          dns_resolution_config:
            resolvers:
            - socket_address:
                address: 10.68.0.2
                port_value: 53
            dns_resolver_options:
              use_tcp_for_dns_lookups: true
              no_default_search_domain: false
          load_assignment:
            cluster_name: productpage_cluster
            endpoints:
            - lb_endpoints:
              - endpoint:
                  address:
                     socket_address:
                      address: productpage.istio.svc.cluster.local
                      port_value: 9080
```

## 3.15outlier_detection

```
{
  "consecutive_5xx": "{...}",连续的5xx错误，触发熔断，默认5
  "interval": "{...}",弹出分析间隔，默认10s
  "base_ejection_time": "{...}",基础弹出时间，默认30s
  "max_ejection_percent": "{...}",最大弹出百分比，默认10%
  "enforcing_consecutive_5xx": "{...}",连续5xx错误的弹出可能性，默认100%
  "enforcing_success_rate": "{...}",健康检测的弹出可能性，默认100%
  "success_rate_minimum_hosts": "{...}",成功率检测的最小主机数，默认5
  "success_rate_request_volume": "{...}",一个主机的一次成功率检测的最小请求数，默认100
  "success_rate_stdev_factor": "{...}",mean - (stdev * success_rate_stdev_factor)，默认1900
  "consecutive_gateway_failure": "{...}",连续的网关错误数，触发弹出，默认5
  "enforcing_consecutive_gateway_failure": "{...}",网关错误弹出的可能性，默认0
  "split_external_local_origin_errors": "...",区分 local origin failures 和 external errors
  "consecutive_local_origin_failure": "{...}",连续的local origin错误触发弹出，默认5
  "enforcing_consecutive_local_origin_failure": "{...}",连续local origin错误触发弹出的可能性，默认100
  "enforcing_local_origin_success_rate": "{...}",连续本地源成功率弹出可能性。默认100
  "failure_percentage_threshold": "{...}",错误百分比阈值触发弹出，默认85
  "enforcing_failure_percentage": "{...}",错误百分比触发弹出可能性，默认0
  "enforcing_failure_percentage_local_origin": "{...}",本地源错误百分比弹出可能性，默认0
  "failure_percentage_minimum_hosts": "{...}",基于错误百分比弹出的最小host数量，默认5
  "failure_percentage_request_volume": "{...}",基于错误百分比弹出的每个主机的请求最小数量，默认50
  "max_ejection_time": "{...}"最大弹出时间，默认300s
}
```

cluster-outlier_detection.yaml

kubectl apply -f cluster-outlier_detection.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: cluster
  namespace: istio-system
spec:
  workloadSelector:
    labels:
      istio: ingressgateway
  configPatches:
    - applyTo: CLUSTER
      patch:
        operation: ADD
        value:
          name: productpage_cluster
          type: STRICT_DNS
          connect_timeout: 10s
          lb_policy: ROUND_ROBIN
          outlier_detection:
            consecutive_5xx: 5
            interval: 10s
            base_ejection_time: 30s
            max_ejection_percent: 10
            enforcing_consecutive_5xx: 100
            enforcing_success_rate: 100
            success_rate_minimum_hosts: 5
            success_rate_request_volume: 100
            success_rate_stdev_factor: 1900
            consecutive_gateway_failure: 5
            enforcing_consecutive_gateway_failure: 0
            split_external_local_origin_errors: true
            consecutive_local_origin_failure: 5
            enforcing_consecutive_local_origin_failure: 100
            enforcing_local_origin_success_rate: 100
            failure_percentage_threshold: 85
            enforcing_failure_percentage: 0
            enforcing_failure_percentage_local_origin: 0
            failure_percentage_minimum_hosts: 5
            failure_percentage_request_volume: 50
            max_ejection_time: 300s
          load_assignment:
            cluster_name: productpage_cluster
            endpoints:
            - lb_endpoints:
              - endpoint:
                  address:
                     socket_address:
                      address: productpage.istio.svc.cluster.local
                      port_value: 9080
```

## 3.16 lb_subset_config

将cluster分割成多个子集

```
{
  "fallback_policy": "...",端点子集没有匹配路由元数据时的策略
  "default_subset": "{...}",默认子集
  "subset_selectors": [],子集选择器
  "locality_weight_aware": "...",是否地理位置感知
  "scale_locality_weight": "...",根据host数量，调整权重
  "panic_mode_any": "...",恐慌时选择任意一个host
  "list_as_any": "..."匹配列表中的任意一个
}
```

 **subset_selectors** ：

```
{
  "keys": [],子集的key
  "single_host_per_subset": "...",一个子集一个host
  "fallback_policy": "...",回退策略
  "fallback_keys_subset": []key_subset回退策略的key
}
```

cluster-lb_subset_config.yaml

kubectl apply -f cluster-lb_subset_config.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: cluster
  namespace: istio-system
spec:
  workloadSelector:
    labels:
      istio: ingressgateway
  configPatches:
    - applyTo: CLUSTER
      patch:
        operation: ADD
        value:
          name: productpage_cluster
          type: STRICT_DNS
          connect_timeout: 10s
          lb_policy: ROUND_ROBIN
          lb_subset_config:
            fallback_policy: DEFAULT_SUBSET
            default_subset:
              version: v1
            subset_selectors:
            - keys:
              - version
              - stage
              single_host_per_subset: true
              fallback_policy: KEYS_SUBSET
              fallback_keys_subset:
              - version
            locality_weight_aware: true
            scale_locality_weight: true
            panic_mode_any: true
            list_as_any: true
          load_assignment:
            cluster_name: productpage_cluster
            endpoints:
            - lb_endpoints:
              - endpoint:
                  address:
                     socket_address:
                      address: productpage.istio.svc.cluster.local
                      port_value: 9080
```

## 3.17ring_hash_lb_config

```
{
  "minimum_ring_size": "{...}",最小环数量,默认1024 
  "hash_function": "...",hash函数
  "maximum_ring_size": "{...}"最大环数量 默认8M
}
```

cluster-ring_hash_lb_config.yaml

kubectl apply -f cluster-ring_hash_lb_config.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: cluster
  namespace: istio-system 
spec:
  workloadSelector:
    labels:
      istio: ingressgateway
  configPatches:
  - applyTo: CLUSTER
    match:
      cluster:
        name: outbound|9080||productpage.istio.svc.cluster.local
    patch:
      operation: MERGE
      value:
         lb_policy: RING_HASH
         ring_hash_lb_config:
           minimum_ring_size: 1024 
           maximum_ring_size: 10240
           hash_function: XX_HASH
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
                        case_sensitive: false
                      route:
                        cluster: outbound|9080||productpage.istio.svc.cluster.local
                        hash_policy:
                        - header:
                            header_name: test
```

## 3.18maglev_lb_config

```
{
  "table_size": "{...}" 表大小，默认65537
}
```

cluster-maglev_lb_config.yaml

kubectl apply -f cluster-maglev_lb_config.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: cluster
  namespace: istio-system 
spec:
  workloadSelector:
    labels:
      istio: ingressgateway
  configPatches:
  - applyTo: CLUSTER
    match:
      cluster:
        name: outbound|9080||productpage.istio.svc.cluster.local
    patch:
      operation: MERGE
      value:
         lb_policy: MAGLEV
         maglev_lb_config:
           table_size: 65537
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
                        case_sensitive: false
                      route:
                        cluster: outbound|9080||productpage.istio.svc.cluster.local
                        hash_policy:
                        - header:
                            header_name: test
```

## 3.19original_dst_lb_config

```
{
  "use_http_header": "..."使用http头获取上游地址
}
```

cluster-original_dst_lb_config.yaml

kubectl apply -f cluster-original_dst_lb_config.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: cluster
  namespace: istio-system 
spec:
  workloadSelector:
    labels:
      istio: ingressgateway
  configPatches:
  - applyTo: CLUSTER
    match:   
      cluster:
        name: outbound|9080||productpage.istio.svc.cluster.local
    patch:
      operation: REMOVE
  - applyTo: CLUSTER
    patch:
      operation: ADD
      value:
          circuitBreakers:
            thresholds:
            - maxConnections: 4294967295
              maxPendingRequests: 4294967295
              maxRequests: 4294967295
              maxRetries: 4294967295
              trackRemaining: true
          connectTimeout: 10s
          filters:
          - name: istio.metadata_exchange
            typedConfig:
              '@type': type.googleapis.com/udpa.type.v1.TypedStruct
              typeUrl: type.googleapis.com/envoy.tcp.metadataexchange.config.MetadataExchange
              value:
                protocol: istio-peer-exchange
          lbPolicy: CLUSTER_PROVIDED
          metadata:
            filterMetadata:
              istio:
                config: /apis/networking.istio.io/v1alpha3/namespaces/istio/destination-rule/productpage
                default_original_port: 9080
                services:
                - host: productpage.istio.svc.cluster.local
                  name: productpage
                  namespace: istio
          name: outbound|9080||productpage.istio.svc.cluster.local
          originalDstLbConfig:
            useHttpHeader: true
          transportSocket:
            name: envoy.transport_sockets.tls
            typedConfig:
              '@type': type.googleapis.com/envoy.extensions.transport_sockets.tls.v3.UpstreamTlsContext
              commonTlsContext:
                alpnProtocols:
                - istio-peer-exchange
                - istio
                combinedValidationContext:
                  defaultValidationContext:
                    matchSubjectAltNames:
                    - exact: spiffe://cluster.local/ns/istio/sa/bookinfo-productpage
                  validationContextSdsSecretConfig:
                    name: ROOTCA
                    sdsConfig:
                      apiConfigSource:
                        apiType: GRPC
                        grpcServices:
                        - envoyGrpc:
                            clusterName: sds-grpc
                        setNodeOnFirstMessageOnly: true
                        transportApiVersion: V3
                      initialFetchTimeout: 0s
                      resourceApiVersion: V3
                tlsCertificateSdsSecretConfigs:
                - name: default
                  sdsConfig:
                    apiConfigSource:
                      apiType: GRPC
                      grpcServices:
                      - envoyGrpc:
                          clusterName: sds-grpc
                      setNodeOnFirstMessageOnly: true
                      transportApiVersion: V3
                    initialFetchTimeout: 0s
                    resourceApiVersion: V3
              sni: outbound_.9080_._.productpage.istio.svc.cluster.local
          type: ORIGINAL_DST
```

## 3.20least_request_lb_config

```
{
  "choice_count": "{...}",选择host的数量,默认2
  "active_request_bias": "{...}",活跃请求偏向 weight = load_balancing_weight / (active_requests + 1)^active_request_bias
  "slow_start_config": "{...}"慢开始配置，istio没实现
}
```

slow_start_config:

```
{
  "slow_start_window": "{...}",慢开始窗口
  "aggression": "{...}"控制慢开始流量增长的配置new_weight = weight * time_factor ^ (1 / aggression,time_factor=(time_since_start_seconds / slow_start_time_seconds).
}
```

aggression:

```
{
  "default_value": "...",默认值
  "runtime_key": "..."运行时键
}
```

cluster-least_request_lb_config.yaml

kubectl apply -f cluster-least_request_lb_config.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: cluster
  namespace: istio-system 
spec:
  workloadSelector:
    labels:
      istio: ingressgateway
  configPatches:
  - applyTo: CLUSTER
    match:
      cluster:
        name: outbound|9080||productpage.istio.svc.cluster.local
    patch:
      operation: MERGE
      value:
         lb_policy: LEAST_REQUEST
         least_request_lb_config:
           choice_count: 2
           active_request_bias:
             default_value: 0.2
           #slow_start_config:
            # slow_start_window: 10s
            # aggression:
            #   default_value: 30
               
```



## 3.21round_robin_lb_config

```
{
  "slow_start_config": "{...}"慢开始配置
}
```

slow_start_config:

```
{
  "slow_start_window": "{...}",慢开始窗口
  "aggression": "{...}"控制慢开始流量增长的配置new_weight = weight * time_factor ^ (1 / aggression,time_factor=(time_since_start_seconds / slow_start_time_seconds).
}
```

aggression:

```
{
  "default_value": "...",默认值
  "runtime_key": "..."运行时键
}
```

cluster-round_robin_lb_config.yaml

kubectl apply -f cluster-round_robin_lb_config.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: cluster
  namespace: istio-system 
spec:
  workloadSelector:
    labels:
      istio: ingressgateway
  configPatches:
  - applyTo: CLUSTER
    match:
      cluster:
        name: outbound|9080||productpage.istio.svc.cluster.local
    patch:
      operation: MERGE
      value:
         lb_policy: ROUND_ROBIN
         round_robin_lb_config: #istio未实现
           slow_start_config:
             slow_start_window: 10s
             aggression:
               default_value: 30
```

## 3.22common_lb_config

```
{
  "healthy_panic_threshold": "{...}",健康恐慌的阈值，默认50%
  "zone_aware_lb_config": "{...}",位置感知负载均衡配置
  "locality_weighted_lb_config": "{...}",基于位置加权负载均衡配置
  "update_merge_window": "{...}",health check/weight/metadata 更新合并窗口
  "ignore_new_hosts_until_first_hc": "...",忽略新host知道他通过health check
  "close_connections_on_host_set_change": "...",关闭连接当host集合改变的时候
  "consistent_hashing_lb_config": "{...}"一致性hash负载均衡配置
}
```

zone_aware_lb_config：

```
{
  "routing_enabled": "{...}",配置启用位置感知负载均衡配置的请求比例，默认100%
  "min_cluster_size": "{...}",最小集群大小，启用位置感知服务，默认6
  "fail_traffic_on_panic": "..."当panic模式是是否使用任意host
}
```

consistent_hashing_lb_config：

```
{
  "use_hostname_for_hashing": "...",使用hostname而不是使用ip用作hash，只对StrictDNS 有效
  "hash_balance_factor": "{...}"配置上游服务的平均负载， between 120 and 200. Minimum is 100.
}
```

cluster-common_lb_config.yaml

kubectl apply -f cluster-common_lb_config.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: cluster
  namespace: istio-system 
spec:
  workloadSelector:
    labels:
      istio: ingressgateway
  configPatches:
  - applyTo: CLUSTER
    match:
      cluster:
        name: outbound|9080||productpage.istio.svc.cluster.local
    patch:
      operation: MERGE
      value:
         lb_policy: LEAST_REQUEST
         common_lb_config:
           healthy_panic_threshold:
             value: 50
           zone_aware_lb_config:
             routing_enabled:
               value: 100
             min_cluster_size: 6
             fail_traffic_on_panic: true
           update_merge_window: 60s
           ignore_new_hosts_until_first_hc: true
           close_connections_on_host_set_change: true
           consistent_hashing_lb_config:
             use_hostname_for_hashing: true
             hash_balance_factor: 120
```



## 3.23transport_socket

- [envoy.transport_sockets.alts](https://www.envoyproxy.io/docs/envoy/latest/api-v3/extensions/transport_sockets/alts/v3/alts.proto#extension-envoy-transport-sockets-alts)
- [envoy.transport_sockets.raw_buffer](https://www.envoyproxy.io/docs/envoy/latest/api-v3/extensions/transport_sockets/raw_buffer/v3/raw_buffer.proto#extension-envoy-transport-sockets-raw-buffer)
- [envoy.transport_sockets.starttls](https://www.envoyproxy.io/docs/envoy/latest/api-v3/extensions/transport_sockets/starttls/v3/starttls.proto#extension-envoy-transport-sockets-starttls)
- [envoy.transport_sockets.tap](https://www.envoyproxy.io/docs/envoy/latest/api-v3/extensions/transport_sockets/tap/v3/tap.proto#extension-envoy-transport-sockets-tap)
- [envoy.transport_sockets.tls](https://www.envoyproxy.io/docs/envoy/latest/api-v3/extensions/transport_sockets/tls/v3/tls.proto#extension-envoy-transport-sockets-tls)
- [envoy.transport_sockets.upstream_proxy_protocol](https://www.envoyproxy.io/docs/envoy/latest/api-v3/extensions/transport_sockets/proxy_protocol/v3/upstream_proxy_protocol.proto#extension-envoy-transport-sockets-upstream-proxy-protocol)

略

## 3.24metadata

```
{
  "filter_metadata": "{...}",
  "typed_filter_metadata": "{...}"
}
```

cluster-metadata.yaml

kubectl apply -f cluster-metadata.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: cluster
  namespace: istio-system 
spec:
  workloadSelector:
    labels:
      istio: ingressgateway
  configPatches:
  - applyTo: CLUSTER
    match:
      cluster:
        name: outbound|9080||productpage.istio.svc.cluster.local
    patch:
      operation: MERGE
      value:
         lb_policy: LEAST_REQUEST
         metadata:
           filter_metadata:
             envoy.lb:
               canary: true
```



## 3.25filters

```
{
  "name": "...",名称
  "typed_config": "{...}"配置
}
```



cluster-filters.yaml

kubectl apply -f cluster-filters.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: cluster
  namespace: istio-system 
spec:
  workloadSelector:
    labels:
      istio: ingressgateway
  configPatches:
  - applyTo: CLUSTER
    match:
      cluster:
        name: outbound|9080||productpage.istio.svc.cluster.local
    patch:
      operation: MERGE
      value:
          lb_policy: LEAST_REQUEST
          filters:
          - name: istio.metadata_exchange
            typedConfig:
              '@type': type.googleapis.com/udpa.type.v1.TypedStruct
              typeUrl: type.googleapis.com/envoy.tcp.metadataexchange.config.MetadataExchange
              value:
                protocol: istio-peer-exchange
```

## 3.26 upstream_connection_options

```
{
  "tcp_keepalive": "{...}"保持连接配置
}
```

tcp_keepalive：

```
{
  "keepalive_probes": "{...}",决定死亡前最大探测次数，默认9
  "keepalive_time": "{...}",保持连接时间 默认7200s
  "keepalive_interval": "{...}"保持连接检测周期，默认75s.
}
```

cluster-upstream_connection_options.yaml

kubectl apply -f cluster-upstream_connection_options.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: cluster
  namespace: istio-system 
spec:
  workloadSelector:
    labels:
      istio: ingressgateway
  configPatches:
  - applyTo: CLUSTER
    match:
      cluster:
        name: outbound|9080||productpage.istio.svc.cluster.local
    patch:
      operation: MERGE
      value:
          lb_policy: LEAST_REQUEST
          upstream_connection_options:
            tcp_keepalive:
              keepalive_probes: 9
              keepalive_time: 7200
              keepalive_interval: 75
```

## 3.27 **upstream_config**

 upstream connection pool, and upstream type 

 Currently this field only applies for HTTP traffic but is designed for eventual use for custom TCP upstreams 

- [envoy.upstreams.http.generic](https://www.envoyproxy.io/docs/envoy/latest/api-v3/extensions/upstreams/http/generic/v3/generic_connection_pool.proto#extension-envoy-upstreams-http-generic)
- [envoy.upstreams.http.http](https://www.envoyproxy.io/docs/envoy/latest/api-v3/extensions/upstreams/http/http/v3/http_connection_pool.proto#extension-envoy-upstreams-http-http)
- [envoy.upstreams.http.http_protocol_options](https://www.envoyproxy.io/docs/envoy/latest/api-v3/extensions/upstreams/http/v3/http_protocol_options.proto#extension-envoy-upstreams-http-http-protocol-options)
- [envoy.upstreams.http.tcp](https://www.envoyproxy.io/docs/envoy/latest/api-v3/extensions/upstreams/http/tcp/v3/tcp_connection_pool.proto#extension-envoy-upstreams-http-tcp)
- [envoy.upstreams.tcp.generic](https://www.envoyproxy.io/docs/envoy/latest/api-v3/extensions/upstreams/tcp/generic/v3/generic_connection_pool.proto#extension-envoy-upstreams-tcp-generic)



## 3.28track_cluster_stats

```
{
  "timeout_budgets": "...",统计超时柱状图
  "request_response_sizes": "..."头和体的请求响应的柱状图
}
```

cluster-track_cluster_stats.yaml

kubectl apply -f cluster-track_cluster_stats.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: cluster
  namespace: istio-system 
spec:
  workloadSelector:
    labels:
      istio: ingressgateway
  configPatches:
  - applyTo: CLUSTER
    match:
      cluster:
        name: outbound|9080||productpage.istio.svc.cluster.local
    patch:
      operation: MERGE
      value:
          lb_policy: LEAST_REQUEST
          track_cluster_stats:
            timeout_budgets: true
            request_response_sizes: true
```

## 3.29preconnect_policy

```
{
  "per_upstream_preconnect_ratio": "{...}",对一个进来得请求的每个流的多少可以预期处理，对高并发有用
  "predictive_preconnect_ratio": "{...}"集群范围每个流预期处理比例，对低并发有用
}
```

per_upstream_preconnect_ratio：

  In steady state for non-multiplexed connections a value of 1.5 would mean if there were 100 active streams, there would be 100 connections in use, and 50 connections preconnected. 



cluster-preconnect_policy.yaml

kubectl apply -f cluster-preconnect_policy.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: cluster
  namespace: istio-system 
spec:
  workloadSelector:
    labels:
      istio: ingressgateway
  configPatches:
  - applyTo: CLUSTER
    match:
      cluster:
        name: outbound|9080||productpage.istio.svc.cluster.local
    patch:
      operation: MERGE
      value:
          lb_policy: LEAST_REQUEST
          preconnect_policy:
            per_upstream_preconnect_ratio: 1.5
            predictive_preconnect_ratio: 2
```

## 3.30general

upstream_bind_config:

```
{
  "source_address": "{...}",地址
  "freebind": "{...}",是否设置IP_FREEBIND选项
  "socket_options": []socke选项
}
```

source_address:

```
{
  "protocol": "...",协议
  "address": "...",ip地址
  "port_value": "...",端口
  "named_port": "...",命名的端口
  "resolver_name": "...",解析器名称
  "ipv4_compat": "..."是否兼容ipv4
}
```

socket_options：

```
{
  "description": "...",用来调试的名称
  "level": "...",传给setsockopt函数的参数
  "name": "...",数字化的名字
  "int_value": "...",int值
  "buf_value": "...",buf值
  "state": "..."STATE_PREBIND是唯一的值
}
```

cluster-general.yaml

kubectl apply -f cluster-general.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: cluster
  namespace: istio-system 
spec:
  workloadSelector:
    labels:
      istio: ingressgateway
  configPatches:
  - applyTo: CLUSTER
    match:
      cluster:
        name: outbound|9080||productpage.istio.svc.cluster.local
    patch:
      operation: MERGE
      value:
          alt_stat_name: test
          connect_timeout: 20s
          per_connection_buffer_limit_bytes: 32768
          max_requests_per_connection: 1024
          #wait_for_warm_on_init:  only applicable for STRICT_DNS, or LOGICAL_DNS.
          cleanup_interval: 5000ms
          upstream_bind_config:
            source_address:
              address: 0
              port_value: 9080
              protocol: TCP
              ipv4_compat: true
            freebind: true
            socket_options: []
          #protocol_selection: 废弃
          close_connections_on_host_health_failure: true
          ignore_health_on_host_removal: true
          track_timeout_budgets: true
          connection_pool_per_downstream_connection: true
```

