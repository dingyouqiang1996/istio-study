# 2-8配置简介

##  Bootstrap 

```
{
  "node": "{...}",
  "static_resources": "{...}",
  "dynamic_resources": "{...}",
  "cluster_manager": "{...}",
  "hds_config": "{...}",
  "flags_path": "...",
  "stats_sinks": [],
  "stats_config": "{...}",
  "stats_flush_interval": "{...}",
  "stats_flush_on_admin": "...",
  "watchdog": "{...}",
  "watchdogs": "{...}",
  "tracing": "{...}",
  "layered_runtime": "{...}",
  "admin": "{...}",
  "overload_manager": "{...}",
  "enable_dispatcher_stats": "...",
  "header_prefix": "...",
  "stats_server_version_override": "{...}",
  "use_tcp_for_dns_lookups": "...",
  "dns_resolution_config": "{...}",
  "typed_dns_resolver_config": "{...}",
  "bootstrap_extensions": [],
  "fatal_actions": [],
  "default_socket_interface": "...",
  "inline_headers": [],
  "perf_tracing_file_path": "..."
}
```

node : 节点标识，配置的是 Envoy 的标记信息，management server 利用它来标识不同的 Envoy 实例。

static_resources : 定义静态配置，是 Envoy 核心工作需要的资源，由 Listener、Cluster 和 Secret 三部分组成。

dynamic_resources : 定义动态配置，通过 xDS 来获取配置。可以同时配置动态和静态。

cluster_manager : 管理所有的上游集群。它封装了连接后端服务的操作，当 Filter 认为可以建立连接时，便调用 cluster_manager 的 API 来建立连接。cluster_manager 负责处理负载均衡、健康检查等细节。

hds_config : 健康检查服务发现动态配置。

flags_path：启动flag文件路径

stats_sinks : 状态输出插件。可以将状态数据输出到多种采集系统中。一般通过 Envoy 的管理接口 /stats/prometheus 就可以获取 Prometheus 格式的指标，这里的配置应该是为了支持其他的监控系统。

stats_config : 状态指标配置。

stats_flush_interval : 状态指标刷新时间。

stats_flush_on_admin：如果查询admin接口是否刷新stats

watchdog : 看门狗配置。Envoy 内置了一个看门狗系统，可以在 Envoy 没有响应时增加相应的计数器，并根据计数来决定是否关闭 Envoy 服务。

watchdogs：为不同子系统设置开门狗配置

tracing : 分布式追踪相关配置。

layered_runtime : 层级化的运行时状态配置。可以静态配置，也可以通过 RTDS 动态加载配置。

admin : 管理接口。

overload_manager : 过载过滤器

enable_dispatcher_stats：是否启用事件分发stats

header_prefix : Header 字段前缀修改。例如，如果将该字段设为 X-Foo，那么 Header 中的 x-envoy-retry-on 将被会变成 x-foo-retry-on。

stats_server_version_override：用于服务器版本统计，设置代理版本

use_tcp_for_dns_lookups : 强制使用 TCP 查询 DNS。可以在 Cluster 的配置中覆盖此配置。

dns_resolution_config: dns解析配置，废弃

typed_dns_resolution_config：dns解析配置

bootstrap_extensions：配置启动时的启动扩展

fatal_actions：致命错误动作

default_socket_interface：默认套接字接口

inline_headers:同名的头内联

perf_tracing_file_path:perf性能跟踪文件路径


## Listener

```
{
  "name": "...", 名称
  "address": "{...}", listener监听的地址
  "stat_prefix": "...",监听器stat数据前缀
  "filter_chains": [],过滤器链
  "filter_chain_matcher": {}过滤器链匹配
  "use_original_dst": "{...}",使用原始目标地址
  "default_filter_chain": "{...}",默认过滤器链
  "per_connection_buffer_limit_bytes": "{...}",链接读写缓存限制大小
  "metadata": "{...}",监听器原数据
  "drain_type": "...",监听器级别的排水类型
  "listener_filters": [],监听器过滤器
  "listener_filters_timeout": "{...}",所有监听器过滤器的超时时间
  "continue_on_listener_filters_timeout": "...",当监听器过滤器超时时是否创建链接
  "transparent": "{...}",监听器是否设置为transparent socket
  "freebind": "{...}",监听器是否设置 IP_FREEBIND socket 选项
  "socket_options": [],socket选项
  "tcp_fast_open_queue_length": "{...}",TCP Fast Open (TFO) 队列大小
  "traffic_direction": "...",流量的方向
  "udp_listener_config": "{...}",udp类型的监听器配置
  "api_listener": "{...}",用于non-proxy 客户端
  "connection_balance_config": "{...}",监听器 链接 balancing配置
  "reuse_port": "...",设置SO_REUSEPORT socket选项
  "enable_reuse_port": "",是否重用端口
  "access_log": [],日志
  "tcp_backlog_size": "{...}",等待链接的队列大小
  "bind_to_port": "{...}"监听器是否绑定端口
  "enable_mptcp": ""是否启用multi-path TCP)
  "ignore_global_conn_limit": ""是否忽略global_downstream_max_connections
}
```

## Cluster 

```
{
  "transport_socket_matches": [],//传输套接字匹配
  "name": "...",//集群名称
  "alt_stat_name": "...",//用于监控的名字
  "type": "...",//服务发现类型
  "cluster_type": "{...}",//集群类型
  "eds_cluster_config": "{...}",//eds更新配置
  "connect_timeout": "{...}",//链接超时时间
  "per_connection_buffer_limit_bytes": "{...}",//每个连接读写缓存限制
  "lb_policy": "...",//负载均衡策略
  "load_assignment": "{...}",负载分配，替代v2版本的host
  "health_checks": [],健康检查
  "max_requests_per_connection": "{...}",//每个连接的请求最大个数
  "circuit_breakers": "{...}",//断路器设置
  "upstream_http_protocol_options": "{...}",上游http协议选项
  "common_http_protocol_options": "{...}",公共http协议选项
  "http_protocol_options": "{...}",http1.1选项
  "http2_protocol_options": "{...}",http2选项
  "typed_extension_protocol_options": "{...}",扩展协议选项
  "dns_refresh_rate": "{...}",dns刷新频率
  "dns_failure_refresh_rate": "{...}",请求失败dns刷新频率
  "respect_dns_ttl": "...",dns刷新频率是否设置为dns ttl
  "dns_lookup_family": "...",dns ip解析策略
  "dns_resolvers": [],dns解析器
  "use_tcp_for_dns_lookups": "...",dns查找是否用tcp协议
  "dns_resolution_config": {},dns解析配置，废弃
  "typed_dns_resolution_config":{},dns解析配置
  "wait_for_warm_on_init":{},是否等待预热
  "outlier_detection": "{...}",异常检测
  "cleanup_interval": "{...}",删除过期host的间隔
  "upstream_bind_config": "{...}",绑定新发布的上游链接，覆盖bootstrap的bind_config 
  "lb_subset_config": "{...}",负载均衡子集配置
  "ring_hash_lb_config": "{...}",令牌hash负载均衡策略配置
  "maglev_lb_config": "{...}",Maglev 负载均衡配置
  "original_dst_lb_config": "{...}",Original Destination负载均衡配置
  "least_request_lb_config": "{...}",最少请求负载均衡配置
  "round_robin_lb_config":{}，轮训负载均衡配置
  "common_lb_config": "{...}",公共负载均衡配置
  "transport_socket": "{...}",传输套接字配置
  "metadata": "{...}",元数据
  "protocol_selection": "...",协议选择配置，废弃
  "upstream_connection_options": "{...}",上游链接选项
  "close_connections_on_host_health_failure": "...",主机不健康时是否关闭链接
  "ignore_health_on_host_removal": "...",主机删除时步不虑是否健康
  "filters": [],网络过滤器链
  "load_balancing_policy":{}如果设置代替lb_policy
  "track_timeout_budgets": "...",是否为每个请求发布超时预算直方图
  "upstream_config": "{...}",上游链接池和类型配置
  "track_cluster_stats": "{...}",跟踪集群stats
  "preconnect_policy": "{...}",预链接策略配置
  "connection_pool_per_downstream_connection": "..."是否为下游链接分开连接池
}
```

## route configuration

```
{
  "name": "...",路由名称
  "virtual_hosts": [],虚拟主机
  "vhds": "{...}",vhds 按需发现虚拟主机
  "internal_only_headers": [],内部使用的http头
  "response_headers_to_add": [],添加响应头
  "response_headers_to_remove": [],删除响应头
  "request_headers_to_add": [],添加请求头
  "request_headers_to_remove": [],删除响应头
  "most_specific_header_mutations_wins": "...",反向头生效级别
  "validate_clusters": "{...}",是否被集群管理器校验
  "max_direct_response_body_size_bytes": "{...}"直接响应体的最大大小
  "request_mirror_policies":[]请求镜像策略
}
```





# envoy配置完整示例阅读