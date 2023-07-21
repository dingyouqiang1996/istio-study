# 1什么是bootstrap

bootstrap是envoy sidecar启动时加载的初始配置。

# 2配置

```
{
  "node": "{...}",节点信息
  "static_resources": "{...}",静态资源
  "dynamic_resources": "{...}",动态资源
  "cluster_manager": "{...}",集群管理器
  "hds_config": "{...}",健康检查配置
  "flags_path": "...",获取标志的路径
  "stats_sinks": [],metrix 槽
  "stats_config": "{...}",metrix配置
  "stats_flush_interval": "{...}",metrix刷新频率
  "stats_flush_on_admin": "...",是否在admin端点刷新metrix
  "watchdog": "{...}",看门狗
  "watchdogs": "{...}",看门狗
  "tracing": "{...}",链路跟踪
  "layered_runtime": "{...}",运行时
  "admin": "{...}",管理端点配置
  "overload_manager": "{...}",负载管理器
  "enable_dispatcher_stats": "...",启用stats重定向
  "header_prefix": "...",头前缀
  "stats_server_version_override": "{...}",stats server.version metrix覆盖
  "use_tcp_for_dns_lookups": "...",是否使用tcp来查找dns
  "dns_resolution_config": "{...}",dns配置
  "typed_dns_resolver_config": "{...}",dns配置
  "bootstrap_extensions": [],启动扩展
  "fatal_actions": [],致命动作
  "default_socket_interface": "...",默认socket接口
  "inline_headers": []内联头
}
```

node:

```
{
  "id": "...",节点id
  "cluster": "...",集群
  "metadata": "{...}",元数据
  "dynamic_parameters": "{...}",动态参数
  "locality": "{...}",位置
  "user_agent_name": "...",代理名称
  "user_agent_version": "...",代理版本
  "user_agent_build_version": "{...}",代理建造版本
  "extensions": [],扩展
  "client_features": [],客户端属性
  "listening_addresses": []监听地址
}
```

locality:

```
{
  "region": "...",区域
  "zone": "...",分区
  "sub_zone": "..."子分区
}
```

user_agent_build_version：

```
{
  "version": "{...}",版本
  "metadata": "{...}"元数据
}
```

extensions：

```
{
  "name": "...",名称
  "category": "...",分类
  "version": "{...}",版本
  "disabled": "..."是否禁用
}
```

listening_addresses：

```
{
  "socket_address": "{...}",socket地址
  "pipe": "{...}"管道地址
}
```

static_resources：

```
{
  "listeners": [],监听器
  "clusters": [],集群
  "secrets": []secret
}
```

dynamic_resources：

```
{
  "lds_config": "{...}",lds配置
  "cds_config": "{...}",cds配置
  "ads_config": "{...}"ads配置
}
```

cluster_manager：

```
{
  "local_cluster_name": "...",本地集群名称
  "outlier_detection": "{...}",断路器配置
  "upstream_bind_config": "{...}",上游bind配置
  "load_stats_config": "{...}"负载stats配置
}
```

 **outlier_detection** ：

```
{
  "event_log_path": "..."事件日志路径
}
```

 **upstream_bind_config** ：

```
{
  "source_address": "{...}",原地址
  "freebind": "{...}",绑定
  "socket_options": []socket选项
}
```

 **load_stats_config** ：

```
{
  "api_type": "...",api类型
  "transport_api_version": "...",传输api版本
  "cluster_names": [],集群名称
  "grpc_services": [],grpc服务地址
  "refresh_delay": "{...}",刷新延迟
  "request_timeout": "{...}",请求超时
  "rate_limit_settings": "{...}",限速配置
  "set_node_on_first_message_only": "..."只在第一个消息设置node
}
```

stats_sinks：

```
{
  "name": "...",名称
  "typed_config": "{...}"配置
}
```

- [envoy.stat_sinks.dog_statsd](https://www.envoyproxy.io/docs/envoy/latest/api-v3/config/metrics/v3/stats.proto#extension-envoy-stat-sinks-dog-statsd)
- [envoy.stat_sinks.graphite_statsd](https://www.envoyproxy.io/docs/envoy/latest/api-v3/extensions/stat_sinks/graphite_statsd/v3/graphite_statsd.proto#extension-envoy-stat-sinks-graphite-statsd)
- [envoy.stat_sinks.hystrix](https://www.envoyproxy.io/docs/envoy/latest/api-v3/config/metrics/v3/stats.proto#extension-envoy-stat-sinks-hystrix)
- [envoy.stat_sinks.metrics_service](https://www.envoyproxy.io/docs/envoy/latest/api-v3/config/metrics/v3/metrics_service.proto#extension-envoy-stat-sinks-metrics-service)
- [envoy.stat_sinks.statsd](https://www.envoyproxy.io/docs/envoy/latest/api-v3/config/metrics/v3/stats.proto#extension-envoy-stat-sinks-statsd)
- [envoy.stat_sinks.wasm](https://www.envoyproxy.io/docs/envoy/latest/api-v3/extensions/stat_sinks/wasm/v3/wasm.proto#extension-envoy-stat-sinks-wasm)

 **stats_config** ：

```
{
  "stats_tags": [],标签
  "use_all_default_tags": "{...}",是否使用所有默认标签
  "stats_matcher": "{...}",标签匹配
  "histogram_bucket_settings": []桶配置
}
```

 **watchdog** ：

```
{
  "actions": [],动作
  "miss_timeout": "{...}",miss超时时间
  "megamiss_timeout": "{...}",megamiss超时时间
  "kill_timeout": "{...}",kill超时时间
  "max_kill_timeout_jitter": "{...}",max kill超时抖动时间
  "multikill_timeout": "{...}",multiklill超时时间
  "multikill_threshold": "{...}"multikill阈值
}
```

 **watchdogs** ：

```
{
  "main_thread_watchdog": "{...}",主线程看门狗
  "worker_watchdog": "{...}"工作线程看门狗
}
```

 **tracing** ：

```
{
  "http": "{...}"
}
```

http:

```
{
  "name": "...",
  "typed_config": "{...}"
}
```

- [envoy.tracers.datadog](https://www.envoyproxy.io/docs/envoy/latest/api-v3/config/trace/v3/datadog.proto#extension-envoy-tracers-datadog)
- [envoy.tracers.dynamic_ot](https://www.envoyproxy.io/docs/envoy/latest/api-v3/config/trace/v3/dynamic_ot.proto#extension-envoy-tracers-dynamic-ot)
- [envoy.tracers.lightstep](https://www.envoyproxy.io/docs/envoy/latest/api-v3/config/trace/v3/lightstep.proto#extension-envoy-tracers-lightstep)
- [envoy.tracers.opencensus](https://www.envoyproxy.io/docs/envoy/latest/api-v3/config/trace/v3/opencensus.proto#extension-envoy-tracers-opencensus)
- [envoy.tracers.skywalking](https://www.envoyproxy.io/docs/envoy/latest/api-v3/config/trace/v3/skywalking.proto#extension-envoy-tracers-skywalking)
- [envoy.tracers.xray](https://www.envoyproxy.io/docs/envoy/latest/api-v3/config/trace/v3/xray.proto#extension-envoy-tracers-xray)
- [envoy.tracers.zipkin](https://www.envoyproxy.io/docs/envoy/latest/api-v3/config/trace/v3/zipkin.proto#extension-envoy-tracers-zipkin)

 **layered_runtime** :

```
{
  "layers": 层
}
```

layers:

```
{
  "name": "...",名称
  "static_layer": "{...}",静态层
  "disk_layer": "{...}",磁盘层
  "admin_layer": "{...}",管理层
  "rtds_layer": "{...}"运行时发现层
}
```

admin:

```
{
  "access_log": [],日志配置
  "access_log_path": "...",日志路径
  "profile_path": "...",性能文件路径
  "address": "{...}",地址
  "socket_options": []socket选项
}
```

 **overload_manager** :

```
{
  "refresh_interval": "{...}",刷新周期
  "resource_monitors": [],资源监视器
  "actions": [],动作
  "buffer_factory_config": "{...}"缓存工厂配置
}
```

 **dns_resolution_config** :

```
{
  "resolvers": [],解析器
  "dns_resolver_options": "{...}"选项
}
```

 **typed_dns_resolver_config** :

- [envoy.network.dns_resolver.apple](https://www.envoyproxy.io/docs/envoy/latest/api-v3/extensions/network/dns_resolver/apple/v3/apple_dns_resolver.proto#extension-envoy-network-dns-resolver-apple)
- [envoy.network.dns_resolver.cares](https://www.envoyproxy.io/docs/envoy/latest/api-v3/extensions/network/dns_resolver/cares/v3/cares_dns_resolver.proto#extension-envoy-network-dns-resolver-cares)

 bootstrap_extensions :

This extension category has the following known extensions:

- [envoy.**bootstrap**.wasm](https://www.envoyproxy.io/docs/envoy/latest/api-v3/extensions/wasm/v3/wasm.proto#extension-envoy-bootstrap-wasm)

The following extensions are available in [contrib](https://www.envoyproxy.io/docs/envoy/latest/start/install#install-contrib) images only:

- [envoy.**bootstrap**.vcl](https://www.envoyproxy.io/docs/envoy/latest/api-v3/extensions/vcl/v3alpha/vcl_socket_interface.proto#extension-envoy-bootstrap-vcl)

 **fatal_actions** :

```
{
  "config": "{...}"配置
}
```

 **inline_headers** :

```
{
  "inline_header_name": "...",内联头名称
  "inline_header_type": "..."内联头类型
}
```

# 3实战

## 3.1默认配置

```
{
   "@type": "type.googleapis.com/envoy.admin.v3.BootstrapConfigDump",
   "bootstrap": {
    "node": {
     "id": "sidecar~172.20.1.205~productpage-v1-84558c5f7b-22g6w.istio~istio.svc.cluster.local",
     "cluster": "productpage.istio",
     "metadata": {
      "ENVOY_STATUS_PORT": 15021,
      "POD_PORTS": "[{\"containerPort\":9080,\"protocol\":\"TCP\"}]",
      "LABELS": {
       "service.istio.io/canonical-name": "productpage",
       "version": "v1",
       "app": "productpage",
       "pod-template-hash": "84558c5f7b",
       "security.istio.io/tlsMode": "istio",
       "service.istio.io/canonical-revision": "v1"
      },
      "PROXY_CONFIG": {
       "configPath": "./etc/istio/proxy",
       "tracing": {
        "zipkin": {
         "address": "zipkin.istio-system:9411"
        }
       },
       "serviceCluster": "istio-proxy",
       "binaryPath": "/usr/local/bin/envoy",
       "statNameLength": 189,
       "concurrency": 2,
       "proxyAdminPort": 15000,
       "controlPlaneAuthPolicy": "MUTUAL_TLS",
       "discoveryAddress": "istiod.istio-system.svc:15012",
       "statusPort": 15020,
       "drainDuration": "45s",
       "terminationDrainDuration": "5s",
       "parentShutdownDuration": "60s"
      },
      "ENVOY_PROMETHEUS_PORT": 15090,
      "NAME": "productpage-v1-84558c5f7b-22g6w",
      "NAMESPACE": "istio",
      "SERVICE_ACCOUNT": "bookinfo-productpage",
      "INTERCEPTION_MODE": "REDIRECT",
      "OWNER": "kubernetes://apis/apps/v1/namespaces/istio/deployments/productpage-v1",
      "WORKLOAD_NAME": "productpage-v1",
      "PILOT_SAN": [
       "istiod.istio-system.svc"
      ],
      "ISTIO_VERSION": "1.13.3",
      "INSTANCE_IPS": "172.20.1.205,fe80::cc58:19ff:fef1:9ff4",
      "APP_CONTAINERS": "productpage",
      "ANNOTATIONS": {
       "prometheus.io/path": "/stats/prometheus",
       "sidecar.istio.io/status": "{\"initContainers\":[\"istio-init\"],\"containers\":[\"istio-proxy\"],\"volumes\":[\"istio-envoy\",\"istio-data\",\"istio-podinfo\",\"istio-token\",\"istiod-ca-cert\"],\"imagePullSecrets\":null,\"revision\":\"default\"}",
       "prometheus.io/port": "15020",
       "kubectl.kubernetes.io/default-container": "productpage",
       "prometheus.io/scrape": "true",
       "kubernetes.io/config.seen": "2022-03-22T12:57:33.005726029+08:00",
       "kubectl.kubernetes.io/default-logs-container": "productpage",
       "kubectl.kubernetes.io/restartedAt": "2022-03-22T12:57:32+08:00",
       "kubernetes.io/config.source": "api"
      },
      "MESH_ID": "cluster.local",
      "ISTIO_PROXY_SHA": "15cc87023540f5f81331c11ef9010784f7ef2460",
      "PROV_CERT": "var/run/secrets/istio/root-cert.pem",
      "CLUSTER_ID": "Kubernetes"
     },
     "locality": {},
     "user_agent_name": "envoy",
     "user_agent_build_version": {
      "version": {
       "major_number": 1,
       "minor_number": 21,
       "patch": 2
      },
      "metadata": {
       "revision.status": "Clean",
       "ssl.version": "BoringSSL",
       "build.type": "RELEASE",
       "build.label": "dev",
       "revision.sha": "15cc87023540f5f81331c11ef9010784f7ef2460"
      }
     },
     "extensions": [
      {
       "name": "envoy.client_ssl_auth",
       "category": "envoy.filters.network"
      },
      {
       "name": "envoy.echo",
       "category": "envoy.filters.network"
      },
      {
       "name": "envoy.ext_authz",
       "category": "envoy.filters.network"
      },
      {
       "name": "envoy.filters.network.client_ssl_auth",
       "category": "envoy.filters.network"
      },
      {
       "name": "envoy.filters.network.connection_limit",
       "category": "envoy.filters.network"
      },
      {
       "name": "envoy.filters.network.direct_response",
       "category": "envoy.filters.network"
      },
      {
       "name": "envoy.filters.network.dubbo_proxy",
       "category": "envoy.filters.network"
      },
      {
       "name": "envoy.filters.network.echo",
       "category": "envoy.filters.network"
      },
      {
       "name": "envoy.filters.network.ext_authz",
       "category": "envoy.filters.network"
      },
      {
       "name": "envoy.filters.network.http_connection_manager",
       "category": "envoy.filters.network"
      },
      {
       "name": "envoy.filters.network.local_ratelimit",
       "category": "envoy.filters.network"
      },
      {
       "name": "envoy.filters.network.metadata_exchange",
       "category": "envoy.filters.network"
      },
      {
       "name": "envoy.filters.network.mongo_proxy",
       "category": "envoy.filters.network"
      },
      {
       "name": "envoy.filters.network.mysql_proxy",
       "category": "envoy.filters.network"
      },
      {
       "name": "envoy.filters.network.ratelimit",
       "category": "envoy.filters.network"
      },
      {
       "name": "envoy.filters.network.rbac",
       "category": "envoy.filters.network"
      },
      {
       "name": "envoy.filters.network.redis_proxy",
       "category": "envoy.filters.network"
      },
      {
       "name": "envoy.filters.network.sni_cluster",
       "category": "envoy.filters.network"
      },
      {
       "name": "envoy.filters.network.sni_dynamic_forward_proxy",
       "category": "envoy.filters.network"
      },
      {
       "name": "envoy.filters.network.tcp_cluster_rewrite",
       "category": "envoy.filters.network"
      },
      {
       "name": "envoy.filters.network.tcp_proxy",
       "category": "envoy.filters.network"
      },
      {
       "name": "envoy.filters.network.thrift_proxy",
       "category": "envoy.filters.network"
      },
      {
       "name": "envoy.filters.network.wasm",
       "category": "envoy.filters.network"
      },
      {
       "name": "envoy.filters.network.zookeeper_proxy",
       "category": "envoy.filters.network"
      },
      {
       "name": "envoy.http_connection_manager",
       "category": "envoy.filters.network"
      },
      {
       "name": "envoy.mongo_proxy",
       "category": "envoy.filters.network"
      },
      {
       "name": "envoy.ratelimit",
       "category": "envoy.filters.network"
      },
      {
       "name": "envoy.redis_proxy",
       "category": "envoy.filters.network"
      },
      {
       "name": "envoy.tcp_proxy",
       "category": "envoy.filters.network"
      },
      {
       "name": "forward_downstream_sni",
       "category": "envoy.filters.network"
      },
      {
       "name": "sni_verifier",
       "category": "envoy.filters.network"
      },
      {
       "name": "envoy.network.dns_resolver.cares",
       "category": "envoy.network.dns_resolver"
      },
      {
       "name": "envoy.filters.dubbo.router",
       "category": "envoy.dubbo_proxy.filters"
      },
      {
       "name": "envoy.compression.brotli.decompressor",
       "category": "envoy.compression.decompressor"
      },
      {
       "name": "envoy.compression.gzip.decompressor",
       "category": "envoy.compression.decompressor"
      },
      {
       "name": "envoy.rate_limit_descriptors.expr",
       "category": "envoy.rate_limit_descriptors"
      },
      {
       "name": "envoy.http.stateful_session.cookie",
       "category": "envoy.http.stateful_session"
      },
      {
       "name": "envoy.request_id.uuid",
       "category": "envoy.request_id"
      },
      {
       "name": "envoy.cluster.eds",
       "category": "envoy.clusters"
      },
      {
       "name": "envoy.cluster.logical_dns",
       "category": "envoy.clusters"
      },
      {
       "name": "envoy.cluster.original_dst",
       "category": "envoy.clusters"
      },
      {
       "name": "envoy.cluster.static",
       "category": "envoy.clusters"
      },
      {
       "name": "envoy.cluster.strict_dns",
       "category": "envoy.clusters"
      },
      {
       "name": "envoy.clusters.aggregate",
       "category": "envoy.clusters"
      },
      {
       "name": "envoy.clusters.dynamic_forward_proxy",
       "category": "envoy.clusters"
      },
      {
       "name": "envoy.clusters.redis",
       "category": "envoy.clusters"
      },
      {
       "name": "envoy.retry_priorities.previous_priorities",
       "category": "envoy.retry_priorities"
      },
      {
       "name": "envoy.ip",
       "category": "envoy.resolvers"
      },
      {
       "name": "request-headers",
       "category": "envoy.matching.http.input"
      },
      {
       "name": "request-trailers",
       "category": "envoy.matching.http.input"
      },
      {
       "name": "response-headers",
       "category": "envoy.matching.http.input"
      },
      {
       "name": "response-trailers",
       "category": "envoy.matching.http.input"
      },
      {
       "name": "dubbo",
       "category": "envoy.dubbo_proxy.protocols"
      },
      {
       "name": "envoy.quic.crypto_stream.server.quiche",
       "category": "envoy.quic.server.crypto_stream"
      },
      {
       "name": "envoy.quic.proof_source.filter_chain",
       "category": "envoy.quic.proof_source"
      },
      {
       "name": "envoy.access_loggers.extension_filters.cel",
       "category": "envoy.access_logger.extension_filters"
      },
      {
       "name": "envoy.health_checkers.redis",
       "category": "envoy.health_checkers"
      },
      {
       "name": "envoy.retry_host_predicates.omit_canary_hosts",
       "category": "envoy.retry_host_predicates"
      },
      {
       "name": "envoy.retry_host_predicates.omit_host_metadata",
       "category": "envoy.retry_host_predicates"
      },
      {
       "name": "envoy.retry_host_predicates.previous_hosts",
       "category": "envoy.retry_host_predicates"
      },
      {
       "name": "dubbo.hessian2",
       "category": "envoy.dubbo_proxy.serializers"
      },
      {
       "name": "envoy.bootstrap.wasm",
       "category": "envoy.bootstrap"
      },
      {
       "name": "envoy.extensions.network.socket_interface.default_socket_interface",
       "category": "envoy.bootstrap"
      },
      {
       "name": "envoy.matching.matchers.consistent_hashing",
       "category": "envoy.matching.input_matchers"
      },
      {
       "name": "envoy.matching.matchers.ip",
       "category": "envoy.matching.input_matchers"
      },
      {
       "name": "envoy.filters.thrift.header_to_metadata",
       "category": "envoy.thrift_proxy.filters"
      },
      {
       "name": "envoy.filters.thrift.rate_limit",
       "category": "envoy.thrift_proxy.filters"
      },
      {
       "name": "envoy.filters.thrift.router",
       "category": "envoy.thrift_proxy.filters"
      },
      {
       "name": "envoy.wasm.runtime.null",
       "category": "envoy.wasm.runtime"
      },
      {
       "name": "envoy.wasm.runtime.v8",
       "category": "envoy.wasm.runtime"
      },
      {
       "name": "envoy.key_value.file_based",
       "category": "envoy.common.key_value"
      },
      {
       "name": "envoy.formatter.metadata",
       "category": "envoy.formatter"
      },
      {
       "name": "envoy.formatter.req_without_query",
       "category": "envoy.formatter"
      },
      {
       "name": "envoy.transport_sockets.alts",
       "category": "envoy.transport_sockets.upstream"
      },
      {
       "name": "envoy.transport_sockets.quic",
       "category": "envoy.transport_sockets.upstream"
      },
      {
       "name": "envoy.transport_sockets.raw_buffer",
       "category": "envoy.transport_sockets.upstream"
      },
      {
       "name": "envoy.transport_sockets.starttls",
       "category": "envoy.transport_sockets.upstream"
      },
      {
       "name": "envoy.transport_sockets.tap",
       "category": "envoy.transport_sockets.upstream"
      },
      {
       "name": "envoy.transport_sockets.tls",
       "category": "envoy.transport_sockets.upstream"
      },
      {
       "name": "envoy.transport_sockets.upstream_proxy_protocol",
       "category": "envoy.transport_sockets.upstream"
      },
      {
       "name": "raw_buffer",
       "category": "envoy.transport_sockets.upstream"
      },
      {
       "name": "starttls",
       "category": "envoy.transport_sockets.upstream"
      },
      {
       "name": "tls",
       "category": "envoy.transport_sockets.upstream"
      },
      {
       "name": "envoy.filters.listener.http_inspector",
       "category": "envoy.filters.listener"
      },
      {
       "name": "envoy.filters.listener.original_dst",
       "category": "envoy.filters.listener"
      },
      {
       "name": "envoy.filters.listener.original_src",
       "category": "envoy.filters.listener"
      },
      {
       "name": "envoy.filters.listener.proxy_protocol",
       "category": "envoy.filters.listener"
      },
      {
       "name": "envoy.filters.listener.tls_inspector",
       "category": "envoy.filters.listener"
      },
      {
       "name": "envoy.listener.http_inspector",
       "category": "envoy.filters.listener"
      },
      {
       "name": "envoy.listener.original_dst",
       "category": "envoy.filters.listener"
      },
      {
       "name": "envoy.listener.original_src",
       "category": "envoy.filters.listener"
      },
      {
       "name": "envoy.listener.proxy_protocol",
       "category": "envoy.filters.listener"
      },
      {
       "name": "envoy.listener.tls_inspector",
       "category": "envoy.filters.listener"
      },
      {
       "name": "composite-action",
       "category": "envoy.matching.action"
      },
      {
       "name": "skip",
       "category": "envoy.matching.action"
      },
      {
       "name": "envoy.filters.connection_pools.tcp.generic",
       "category": "envoy.upstreams"
      },
      {
       "name": "envoy.filters.network.upstream.metadata_exchange",
       "category": "envoy.filters.upstream_network"
      },
      {
       "name": "envoy.filters.udp.dns_filter",
       "category": "envoy.filters.udp_listener"
      },
      {
       "name": "envoy.filters.udp_listener.udp_proxy",
       "category": "envoy.filters.udp_listener"
      },
      {
       "name": "envoy.bandwidth_limit",
       "category": "envoy.filters.http"
      },
      {
       "name": "envoy.buffer",
       "category": "envoy.filters.http"
      },
      {
       "name": "envoy.cors",
       "category": "envoy.filters.http"
      },
      {
       "name": "envoy.csrf",
       "category": "envoy.filters.http"
      },
      {
       "name": "envoy.ext_authz",
       "category": "envoy.filters.http"
      },
      {
       "name": "envoy.ext_proc",
       "category": "envoy.filters.http"
      },
      {
       "name": "envoy.fault",
       "category": "envoy.filters.http"
      },
      {
       "name": "envoy.filters.http.adaptive_concurrency",
       "category": "envoy.filters.http"
      },
      {
       "name": "envoy.filters.http.admission_control",
       "category": "envoy.filters.http"
      },
      {
       "name": "envoy.filters.http.alternate_protocols_cache",
       "category": "envoy.filters.http"
      },
      {
       "name": "envoy.filters.http.aws_lambda",
       "category": "envoy.filters.http"
      },
      {
       "name": "envoy.filters.http.aws_request_signing",
       "category": "envoy.filters.http"
      },
      {
       "name": "envoy.filters.http.bandwidth_limit",
       "category": "envoy.filters.http"
      },
      {
       "name": "envoy.filters.http.buffer",
       "category": "envoy.filters.http"
      },
      {
       "name": "envoy.filters.http.cache",
       "category": "envoy.filters.http"
      },
      {
       "name": "envoy.filters.http.cdn_loop",
       "category": "envoy.filters.http"
      },
      {
       "name": "envoy.filters.http.composite",
       "category": "envoy.filters.http"
      },
      {
       "name": "envoy.filters.http.compressor",
       "category": "envoy.filters.http"
      },
      {
       "name": "envoy.filters.http.cors",
       "category": "envoy.filters.http"
      },
      {
       "name": "envoy.filters.http.csrf",
       "category": "envoy.filters.http"
      },
      {
       "name": "envoy.filters.http.decompressor",
       "category": "envoy.filters.http"
      },
      {
       "name": "envoy.filters.http.dynamic_forward_proxy",
       "category": "envoy.filters.http"
      },
      {
       "name": "envoy.filters.http.dynamo",
       "category": "envoy.filters.http"
      },
      {
       "name": "envoy.filters.http.ext_authz",
       "category": "envoy.filters.http"
      },
      {
       "name": "envoy.filters.http.ext_proc",
       "category": "envoy.filters.http"
      },
      {
       "name": "envoy.filters.http.fault",
       "category": "envoy.filters.http"
      },
      {
       "name": "envoy.filters.http.grpc_http1_bridge",
       "category": "envoy.filters.http"
      },
      {
       "name": "envoy.filters.http.grpc_http1_reverse_bridge",
       "category": "envoy.filters.http"
      },
      {
       "name": "envoy.filters.http.grpc_json_transcoder",
       "category": "envoy.filters.http"
      },
      {
       "name": "envoy.filters.http.grpc_stats",
       "category": "envoy.filters.http"
      },
      {
       "name": "envoy.filters.http.grpc_web",
       "category": "envoy.filters.http"
      },
      {
       "name": "envoy.filters.http.header_to_metadata",
       "category": "envoy.filters.http"
      },
      {
       "name": "envoy.filters.http.health_check",
       "category": "envoy.filters.http"
      },
      {
       "name": "envoy.filters.http.ip_tagging",
       "category": "envoy.filters.http"
      },
      {
       "name": "envoy.filters.http.jwt_authn",
       "category": "envoy.filters.http"
      },
      {
       "name": "envoy.filters.http.local_ratelimit",
       "category": "envoy.filters.http"
      },
      {
       "name": "envoy.filters.http.lua",
       "category": "envoy.filters.http"
      },
      {
       "name": "envoy.filters.http.oauth2",
       "category": "envoy.filters.http"
      },
      {
       "name": "envoy.filters.http.on_demand",
       "category": "envoy.filters.http"
      },
      {
       "name": "envoy.filters.http.original_src",
       "category": "envoy.filters.http"
      },
      {
       "name": "envoy.filters.http.ratelimit",
       "category": "envoy.filters.http"
      },
      {
       "name": "envoy.filters.http.rbac",
       "category": "envoy.filters.http"
      },
      {
       "name": "envoy.filters.http.router",
       "category": "envoy.filters.http"
      },
      {
       "name": "envoy.filters.http.set_metadata",
       "category": "envoy.filters.http"
      },
      {
       "name": "envoy.filters.http.stateful_session",
       "category": "envoy.filters.http"
      },
      {
       "name": "envoy.filters.http.tap",
       "category": "envoy.filters.http"
      },
      {
       "name": "envoy.filters.http.wasm",
       "category": "envoy.filters.http"
      },
      {
       "name": "envoy.grpc_http1_bridge",
       "category": "envoy.filters.http"
      },
      {
       "name": "envoy.grpc_json_transcoder",
       "category": "envoy.filters.http"
      },
      {
       "name": "envoy.grpc_web",
       "category": "envoy.filters.http"
      },
      {
       "name": "envoy.health_check",
       "category": "envoy.filters.http"
      },
      {
       "name": "envoy.http_dynamo_filter",
       "category": "envoy.filters.http"
      },
      {
       "name": "envoy.ip_tagging",
       "category": "envoy.filters.http"
      },
      {
       "name": "envoy.local_rate_limit",
       "category": "envoy.filters.http"
      },
      {
       "name": "envoy.lua",
       "category": "envoy.filters.http"
      },
      {
       "name": "envoy.rate_limit",
       "category": "envoy.filters.http"
      },
      {
       "name": "envoy.router",
       "category": "envoy.filters.http"
      },
      {
       "name": "istio.alpn",
       "category": "envoy.filters.http"
      },
      {
       "name": "istio_authn",
       "category": "envoy.filters.http"
      },
      {
       "name": "match-wrapper",
       "category": "envoy.filters.http"
      },
      {
       "name": "envoy.dog_statsd",
       "category": "envoy.stats_sinks"
      },
      {
       "name": "envoy.graphite_statsd",
       "category": "envoy.stats_sinks"
      },
      {
       "name": "envoy.metrics_service",
       "category": "envoy.stats_sinks"
      },
      {
       "name": "envoy.stat_sinks.dog_statsd",
       "category": "envoy.stats_sinks"
      },
      {
       "name": "envoy.stat_sinks.graphite_statsd",
       "category": "envoy.stats_sinks"
      },
      {
       "name": "envoy.stat_sinks.hystrix",
       "category": "envoy.stats_sinks"
      },
      {
       "name": "envoy.stat_sinks.metrics_service",
       "category": "envoy.stats_sinks"
      },
      {
       "name": "envoy.stat_sinks.statsd",
       "category": "envoy.stats_sinks"
      },
      {
       "name": "envoy.stat_sinks.wasm",
       "category": "envoy.stats_sinks"
      },
      {
       "name": "envoy.statsd",
       "category": "envoy.stats_sinks"
      },
      {
       "name": "envoy.internal_redirect_predicates.allow_listed_routes",
       "category": "envoy.internal_redirect_predicates"
      },
      {
       "name": "envoy.internal_redirect_predicates.previous_routes",
       "category": "envoy.internal_redirect_predicates"
      },
      {
       "name": "envoy.internal_redirect_predicates.safe_cross_scheme",
       "category": "envoy.internal_redirect_predicates"
      },
      {
       "name": "auto",
       "category": "envoy.thrift_proxy.protocols"
      },
      {
       "name": "binary",
       "category": "envoy.thrift_proxy.protocols"
      },
      {
       "name": "binary/non-strict",
       "category": "envoy.thrift_proxy.protocols"
      },
      {
       "name": "compact",
       "category": "envoy.thrift_proxy.protocols"
      },
      {
       "name": "twitter",
       "category": "envoy.thrift_proxy.protocols"
      },
      {
       "name": "envoy.grpc_credentials.aws_iam",
       "category": "envoy.grpc_credentials"
      },
      {
       "name": "envoy.grpc_credentials.default",
       "category": "envoy.grpc_credentials"
      },
      {
       "name": "envoy.grpc_credentials.file_based_metadata",
       "category": "envoy.grpc_credentials"
      },
      {
       "name": "envoy.rbac.matchers.upstream_ip_port",
       "category": "envoy.rbac.matchers"
      },
      {
       "name": "envoy.compression.brotli.compressor",
       "category": "envoy.compression.compressor"
      },
      {
       "name": "envoy.compression.gzip.compressor",
       "category": "envoy.compression.compressor"
      },
      {
       "name": "envoy.extensions.upstreams.http.v3.HttpProtocolOptions",
       "category": "envoy.upstream_options"
      },
      {
       "name": "envoy.upstreams.http.http_protocol_options",
       "category": "envoy.upstream_options"
      },
      {
       "name": "default",
       "category": "envoy.dubbo_proxy.route_matchers"
      },
      {
       "name": "envoy.transport_sockets.alts",
       "category": "envoy.transport_sockets.downstream"
      },
      {
       "name": "envoy.transport_sockets.quic",
       "category": "envoy.transport_sockets.downstream"
      },
      {
       "name": "envoy.transport_sockets.raw_buffer",
       "category": "envoy.transport_sockets.downstream"
      },
      {
       "name": "envoy.transport_sockets.starttls",
       "category": "envoy.transport_sockets.downstream"
      },
      {
       "name": "envoy.transport_sockets.tap",
       "category": "envoy.transport_sockets.downstream"
      },
      {
       "name": "envoy.transport_sockets.tls",
       "category": "envoy.transport_sockets.downstream"
      },
      {
       "name": "raw_buffer",
       "category": "envoy.transport_sockets.downstream"
      },
      {
       "name": "starttls",
       "category": "envoy.transport_sockets.downstream"
      },
      {
       "name": "tls",
       "category": "envoy.transport_sockets.downstream"
      },
      {
       "name": "envoy.access_loggers.file",
       "category": "envoy.access_loggers"
      },
      {
       "name": "envoy.access_loggers.http_grpc",
       "category": "envoy.access_loggers"
      },
      {
       "name": "envoy.access_loggers.open_telemetry",
       "category": "envoy.access_loggers"
      },
      {
       "name": "envoy.access_loggers.stderr",
       "category": "envoy.access_loggers"
      },
      {
       "name": "envoy.access_loggers.stdout",
       "category": "envoy.access_loggers"
      },
      {
       "name": "envoy.access_loggers.tcp_grpc",
       "category": "envoy.access_loggers"
      },
      {
       "name": "envoy.access_loggers.wasm",
       "category": "envoy.access_loggers"
      },
      {
       "name": "envoy.file_access_log",
       "category": "envoy.access_loggers"
      },
      {
       "name": "envoy.http_grpc_access_log",
       "category": "envoy.access_loggers"
      },
      {
       "name": "envoy.open_telemetry_access_log",
       "category": "envoy.access_loggers"
      },
      {
       "name": "envoy.stderr_access_log",
       "category": "envoy.access_loggers"
      },
      {
       "name": "envoy.stdout_access_log",
       "category": "envoy.access_loggers"
      },
      {
       "name": "envoy.tcp_grpc_access_log",
       "category": "envoy.access_loggers"
      },
      {
       "name": "envoy.wasm_access_log",
       "category": "envoy.access_loggers"
      },
      {
       "name": "envoy.resource_monitors.fixed_heap",
       "category": "envoy.resource_monitors"
      },
      {
       "name": "envoy.resource_monitors.injected_resource",
       "category": "envoy.resource_monitors"
      },
      {
       "name": "envoy.dynamic.ot",
       "category": "envoy.tracers"
      },
      {
       "name": "envoy.lightstep",
       "category": "envoy.tracers"
      },
      {
       "name": "envoy.tracers.datadog",
       "category": "envoy.tracers"
      },
      {
       "name": "envoy.tracers.dynamic_ot",
       "category": "envoy.tracers"
      },
      {
       "name": "envoy.tracers.lightstep",
       "category": "envoy.tracers"
      },
      {
       "name": "envoy.tracers.opencensus",
       "category": "envoy.tracers"
      },
      {
       "name": "envoy.tracers.skywalking",
       "category": "envoy.tracers"
      },
      {
       "name": "envoy.tracers.xray",
       "category": "envoy.tracers"
      },
      {
       "name": "envoy.tracers.zipkin",
       "category": "envoy.tracers"
      },
      {
       "name": "envoy.zipkin",
       "category": "envoy.tracers"
      },
      {
       "name": "envoy.extensions.http.cache.simple",
       "category": "envoy.http.cache"
      },
      {
       "name": "auto",
       "category": "envoy.thrift_proxy.transports"
      },
      {
       "name": "framed",
       "category": "envoy.thrift_proxy.transports"
      },
      {
       "name": "header",
       "category": "envoy.thrift_proxy.transports"
      },
      {
       "name": "unframed",
       "category": "envoy.thrift_proxy.transports"
      },
      {
       "name": "envoy.watchdog.abort_action",
       "category": "envoy.guarddog_actions"
      },
      {
       "name": "envoy.watchdog.profile_action",
       "category": "envoy.guarddog_actions"
      },
      {
       "name": "preserve_case",
       "category": "envoy.http.stateful_header_formatters"
      },
      {
       "name": "envoy.matching.common_inputs.environment_variable",
       "category": "envoy.matching.common_inputs"
      },
      {
       "name": "envoy.tls.cert_validator.default",
       "category": "envoy.tls.cert_validator"
      },
      {
       "name": "envoy.tls.cert_validator.spiffe",
       "category": "envoy.tls.cert_validator"
      },
      {
       "name": "envoy.http.original_ip_detection.custom_header",
       "category": "envoy.http.original_ip_detection"
      },
      {
       "name": "envoy.http.original_ip_detection.xff",
       "category": "envoy.http.original_ip_detection"
      }
     ]
    },
    "static_resources": {
     "listeners": [
      {
       "address": {
        "socket_address": {
         "address": "0.0.0.0",
         "port_value": 15090
        }
       },
       "filter_chains": [
        {
         "filters": [
          {
           "name": "envoy.filters.network.http_connection_manager",
           "typed_config": {
            "@type": "type.googleapis.com/envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager",
            "stat_prefix": "stats",
            "route_config": {
             "virtual_hosts": [
              {
               "name": "backend",
               "domains": [
                "*"
               ],
               "routes": [
                {
                 "match": {
                  "prefix": "/stats/prometheus"
                 },
                 "route": {
                  "cluster": "prometheus_stats"
                 }
                }
               ]
              }
             ]
            },
            "http_filters": [
             {
              "name": "envoy.filters.http.router",
              "typed_config": {
               "@type": "type.googleapis.com/envoy.extensions.filters.http.router.v3.Router"
              }
             }
            ]
           }
          }
         ]
        }
       ]
      },
      {
       "address": {
        "socket_address": {
         "address": "0.0.0.0",
         "port_value": 15021
        }
       },
       "filter_chains": [
        {
         "filters": [
          {
           "name": "envoy.filters.network.http_connection_manager",
           "typed_config": {
            "@type": "type.googleapis.com/envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager",
            "stat_prefix": "agent",
            "route_config": {
             "virtual_hosts": [
              {
               "name": "backend",
               "domains": [
                "*"
               ],
               "routes": [
                {
                 "match": {
                  "prefix": "/healthz/ready"
                 },
                 "route": {
                  "cluster": "agent"
                 }
                }
               ]
              }
             ]
            },
            "http_filters": [
             {
              "name": "envoy.filters.http.router",
              "typed_config": {
               "@type": "type.googleapis.com/envoy.extensions.filters.http.router.v3.Router"
              }
             }
            ]
           }
          }
         ]
        }
       ]
      }
     ],
     "clusters": [
      {
       "name": "prometheus_stats",
       "type": "STATIC",
       "connect_timeout": "0.250s",
       "load_assignment": {
        "cluster_name": "prometheus_stats",
        "endpoints": [
         {
          "lb_endpoints": [
           {
            "endpoint": {
             "address": {
              "socket_address": {
               "address": "127.0.0.1",
               "port_value": 15000
              }
             }
            }
           }
          ]
         }
        ]
       }
      },
      {
       "name": "agent",
       "type": "STATIC",
       "connect_timeout": "0.250s",
       "load_assignment": {
        "cluster_name": "agent",
        "endpoints": [
         {
          "lb_endpoints": [
           {
            "endpoint": {
             "address": {
              "socket_address": {
               "address": "127.0.0.1",
               "port_value": 15020
              }
             }
            }
           }
          ]
         }
        ]
       }
      },
      {
       "name": "sds-grpc",
       "type": "STATIC",
       "connect_timeout": "1s",
       "load_assignment": {
        "cluster_name": "sds-grpc",
        "endpoints": [
         {
          "lb_endpoints": [
           {
            "endpoint": {
             "address": {
              "pipe": {
               "path": "./etc/istio/proxy/SDS"
              }
             }
            }
           }
          ]
         }
        ]
       },
       "typed_extension_protocol_options": {
        "envoy.extensions.upstreams.http.v3.HttpProtocolOptions": {
         "@type": "type.googleapis.com/envoy.extensions.upstreams.http.v3.HttpProtocolOptions",
         "explicit_http_config": {
          "http2_protocol_options": {}
         }
        }
       }
      },
      {
       "name": "xds-grpc",
       "type": "STATIC",
       "connect_timeout": "1s",
       "max_requests_per_connection": 1,
       "circuit_breakers": {
        "thresholds": [
         {
          "max_connections": 100000,
          "max_pending_requests": 100000,
          "max_requests": 100000
         },
         {
          "priority": "HIGH",
          "max_connections": 100000,
          "max_pending_requests": 100000,
          "max_requests": 100000
         }
        ]
       },
       "upstream_connection_options": {
        "tcp_keepalive": {
         "keepalive_time": 300
        }
       },
       "load_assignment": {
        "cluster_name": "xds-grpc",
        "endpoints": [
         {
          "lb_endpoints": [
           {
            "endpoint": {
             "address": {
              "pipe": {
               "path": "./etc/istio/proxy/XDS"
              }
             }
            }
           }
          ]
         }
        ]
       },
       "typed_extension_protocol_options": {
        "envoy.extensions.upstreams.http.v3.HttpProtocolOptions": {
         "@type": "type.googleapis.com/envoy.extensions.upstreams.http.v3.HttpProtocolOptions",
         "explicit_http_config": {
          "http2_protocol_options": {}
         }
        }
       }
      },
      {
       "name": "zipkin",
       "type": "STRICT_DNS",
       "connect_timeout": "1s",
       "dns_refresh_rate": "30s",
       "dns_lookup_family": "V4_ONLY",
       "load_assignment": {
        "cluster_name": "zipkin",
        "endpoints": [
         {
          "lb_endpoints": [
           {
            "endpoint": {
             "address": {
              "socket_address": {
               "address": "zipkin.istio-system",
               "port_value": 9411
              }
             }
            }
           }
          ]
         }
        ]
       },
       "respect_dns_ttl": true
      }
     ]
    },
    "dynamic_resources": {
     "lds_config": {
      "ads": {},
      "initial_fetch_timeout": "0s",
      "resource_api_version": "V3"
     },
     "cds_config": {
      "ads": {},
      "initial_fetch_timeout": "0s",
      "resource_api_version": "V3"
     },
     "ads_config": {
      "api_type": "GRPC",
      "grpc_services": [
       {
        "envoy_grpc": {
         "cluster_name": "xds-grpc"
        }
       }
      ],
      "set_node_on_first_message_only": true,
      "transport_api_version": "V3"
     }
    },
    "tracing": {
     "http": {
      "name": "envoy.tracers.zipkin",
      "typed_config": {
       "@type": "type.googleapis.com/envoy.config.trace.v3.ZipkinConfig",
       "collector_cluster": "zipkin",
       "collector_endpoint": "/api/v2/spans",
       "trace_id_128bit": true,
       "shared_span_context": false,
       "collector_endpoint_version": "HTTP_JSON"
      }
     }
    },
    "admin": {
     "access_log_path": "/dev/null",
     "profile_path": "/var/lib/istio/data/envoy.prof",
     "address": {
      "socket_address": {
       "address": "127.0.0.1",
       "port_value": 15000
      }
     }
    },
    "stats_config": {
     "stats_tags": [
      {
       "tag_name": "cluster_name",
       "regex": "^cluster\\.((.+?(\\..+?\\.svc\\.cluster\\.local)?)\\.)"
      },
      {
       "tag_name": "tcp_prefix",
       "regex": "^tcp\\.((.*?)\\.)\\w+?$"
      },
      {
       "tag_name": "response_code",
       "regex": "(response_code=\\.=(.+?);\\.;)|_rq(_(\\.d{3}))$"
      },
      {
       "tag_name": "response_code_class",
       "regex": "_rq(_(\\dxx))$"
      },
      {
       "tag_name": "http_conn_manager_listener_prefix",
       "regex": "^listener(?=\\.).*?\\.http\\.(((?:[_.[:digit:]]*|[_\\[\\]aAbBcCdDeEfF[:digit:]]*))\\.)"
      },
      {
       "tag_name": "http_conn_manager_prefix",
       "regex": "^http\\.(((?:[_.[:digit:]]*|[_\\[\\]aAbBcCdDeEfF[:digit:]]*))\\.)"
      },
      {
       "tag_name": "listener_address",
       "regex": "^listener\\.(((?:[_.[:digit:]]*|[_\\[\\]aAbBcCdDeEfF[:digit:]]*))\\.)"
      },
      {
       "tag_name": "mongo_prefix",
       "regex": "^mongo\\.(.+?)\\.(collection|cmd|cx_|op_|delays_|decoding_)(.*?)$"
      },
      {
       "tag_name": "reporter",
       "regex": "(reporter=\\.=(.*?);\\.;)"
      },
      {
       "tag_name": "source_namespace",
       "regex": "(source_namespace=\\.=(.*?);\\.;)"
      },
      {
       "tag_name": "source_workload",
       "regex": "(source_workload=\\.=(.*?);\\.;)"
      },
      {
       "tag_name": "source_workload_namespace",
       "regex": "(source_workload_namespace=\\.=(.*?);\\.;)"
      },
      {
       "tag_name": "source_principal",
       "regex": "(source_principal=\\.=(.*?);\\.;)"
      },
      {
       "tag_name": "source_app",
       "regex": "(source_app=\\.=(.*?);\\.;)"
      },
      {
       "tag_name": "source_version",
       "regex": "(source_version=\\.=(.*?);\\.;)"
      },
      {
       "tag_name": "source_cluster",
       "regex": "(source_cluster=\\.=(.*?);\\.;)"
      },
      {
       "tag_name": "destination_namespace",
       "regex": "(destination_namespace=\\.=(.*?);\\.;)"
      },
      {
       "tag_name": "destination_workload",
       "regex": "(destination_workload=\\.=(.*?);\\.;)"
      },
      {
       "tag_name": "destination_workload_namespace",
       "regex": "(destination_workload_namespace=\\.=(.*?);\\.;)"
      },
      {
       "tag_name": "destination_principal",
       "regex": "(destination_principal=\\.=(.*?);\\.;)"
      },
      {
       "tag_name": "destination_app",
       "regex": "(destination_app=\\.=(.*?);\\.;)"
      },
      {
       "tag_name": "destination_version",
       "regex": "(destination_version=\\.=(.*?);\\.;)"
      },
      {
       "tag_name": "destination_service",
       "regex": "(destination_service=\\.=(.*?);\\.;)"
      },
      {
       "tag_name": "destination_service_name",
       "regex": "(destination_service_name=\\.=(.*?);\\.;)"
      },
      {
       "tag_name": "destination_service_namespace",
       "regex": "(destination_service_namespace=\\.=(.*?);\\.;)"
      },
      {
       "tag_name": "destination_port",
       "regex": "(destination_port=\\.=(.*?);\\.;)"
      },
      {
       "tag_name": "destination_cluster",
       "regex": "(destination_cluster=\\.=(.*?);\\.;)"
      },
      {
       "tag_name": "request_protocol",
       "regex": "(request_protocol=\\.=(.*?);\\.;)"
      },
      {
       "tag_name": "request_operation",
       "regex": "(request_operation=\\.=(.*?);\\.;)"
      },
      {
       "tag_name": "request_host",
       "regex": "(request_host=\\.=(.*?);\\.;)"
      },
      {
       "tag_name": "response_flags",
       "regex": "(response_flags=\\.=(.*?);\\.;)"
      },
      {
       "tag_name": "grpc_response_status",
       "regex": "(grpc_response_status=\\.=(.*?);\\.;)"
      },
      {
       "tag_name": "connection_security_policy",
       "regex": "(connection_security_policy=\\.=(.*?);\\.;)"
      },
      {
       "tag_name": "source_canonical_service",
       "regex": "(source_canonical_service=\\.=(.*?);\\.;)"
      },
      {
       "tag_name": "destination_canonical_service",
       "regex": "(destination_canonical_service=\\.=(.*?);\\.;)"
      },
      {
       "tag_name": "source_canonical_revision",
       "regex": "(source_canonical_revision=\\.=(.*?);\\.;)"
      },
      {
       "tag_name": "destination_canonical_revision",
       "regex": "(destination_canonical_revision=\\.=(.*?);\\.;)"
      },
      {
       "tag_name": "cache",
       "regex": "(cache\\.(.+?)\\.)"
      },
      {
       "tag_name": "component",
       "regex": "(component\\.(.+?)\\.)"
      },
      {
       "tag_name": "tag",
       "regex": "(tag\\.(.+?);\\.)"
      },
      {
       "tag_name": "wasm_filter",
       "regex": "(wasm_filter\\.(.+?)\\.)"
      },
      {
       "tag_name": "authz_enforce_result",
       "regex": "rbac(\\.(allowed|denied))"
      },
      {
       "tag_name": "authz_dry_run_action",
       "regex": "(\\.istio_dry_run_(allow|deny)_)"
      },
      {
       "tag_name": "authz_dry_run_result",
       "regex": "(\\.shadow_(allowed|denied))"
      }
     ],
     "use_all_default_tags": false,
     "stats_matcher": {
      "inclusion_list": {
       "patterns": [
        {
         "prefix": "reporter="
        },
        {
         "prefix": "cluster_manager"
        },
        {
         "prefix": "listener_manager"
        },
        {
         "prefix": "server"
        },
        {
         "prefix": "cluster.xds-grpc"
        },
        {
         "prefix": "wasm"
        },
        {
         "suffix": "rbac.allowed"
        },
        {
         "suffix": "rbac.denied"
        },
        {
         "suffix": "shadow_allowed"
        },
        {
         "suffix": "shadow_denied"
        },
        {
         "prefix": "component"
        }
       ]
      }
     }
    },
    "layered_runtime": {
     "layers": [
      {
       "name": "deprecation",
       "static_layer": {
        "envoy.reloadable_features.http_reject_path_with_fragment": false,
        "envoy.reloadable_features.require_strict_1xx_and_204_response_headers": false,
        "envoy.deprecated_features:envoy.config.listener.v3.Listener.hidden_envoy_deprecated_use_original_dst": true,
        "re2.max_program_size.error_level": 32768
       }
      },
      {
       "name": "global config",
       "static_layer": {
        "overload.global_downstream_max_connections": 2147483647
       }
      },
      {
       "name": "admin",
       "admin_layer": {}
      }
     ]
    }
   },
   "last_updated": "2022-03-22T04:57:34.955Z"
  },
```



## 3.2node

注意事项：

需要修改istio sidecar注入模板，添加如下环境变量

```
          env:
            - name: "BOOTSTRAP_XDS_AGENT"
              value: "true" 
```

```
  meshConfig:
      proxyMetadata:
        # Enable dynamic bootstrap generation
        # https://github.com/istio/istio/pull/33456
        BOOTSTRAP_XDS_AGENT: "true"
```



### 3.2.1metadata

#### 3.2.1.1ISTIO_VERSION

ef-productpage-bootstrap-metadata.yaml

kubectl apply -f ef-productpage-bootstrap-metadata.yaml -n istio

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: ef-productpage-bootstrap-metadata
spec:
  configPatches:
  - applyTo: BOOTSTRAP
    patch:
      operation: MERGE
      value:
        node:
          metadata:
            ISTIO_VERSION: 1.13.3        
```

#### 3.2.2ANNOTATIONS

ef-productpage-bootstrap-metadata-ANNOTATIONS.yaml

kubectl apply -f ef-productpage-bootstrap-metadata-ANNOTATIONS.yaml -n istio

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: ef-productpage-bootstrap-metadata
spec:
  workloadSelector:
    labels:
      app: productpage
  configPatches:
  - applyTo: BOOTSTRAP
    patch:
      operation: MERGE
      value:
        node:
          metadata:
            ANNOTATIONS:
              test: test
```

修改这里的annotation不会修改pod的annotation

#### 3.2.3LABELS

ef-productpage-bootstrap-metadata-LABELS.yaml

kubectl apply -f ef-productpage-bootstrap-metadata-LABELS.yaml -n istio

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: ef-productpage-bootstrap-metadata
spec:
  workloadSelector:
    labels:
      app: productpage
  configPatches:
  - applyTo: BOOTSTRAP
    patch:
      operation: MERGE
      value:
        node:
          metadata:
            LABELS:
              test: test
```

修改这里的label不会修改pod的label

#### 3.2.4NAMESPACE

ef-productpage-bootstrap-metadata-NAMESPACE.yaml

kubectl apply -f ef-productpage-bootstrap-metadata-NAMESPACE.yaml -n istio

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: ef-productpage-bootstrap-metadata
spec:
  workloadSelector:
    labels:
      app: productpage
  configPatches:
  - applyTo: BOOTSTRAP
    patch:
      operation: MERGE
      value:
        node:
          metadata:
            NAMESPACE: test
```

修改名称空间后无法启动

#### 3.2.5MESH_ID

ef-productpage-bootstrap-metadata-MESH_ID.yaml

kubectl apply -f ef-productpage-bootstrap-metadata-MESH_ID.yaml -n istio

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: ef-productpage-bootstrap-metadata
spec:
  workloadSelector:
    labels:
      app: productpage
  configPatches:
  - applyTo: BOOTSTRAP
    patch:
      operation: MERGE
      value:
        node:
          metadata:
            MESH_ID: cluster2.local
```

meshid代表你是哪个mesh，如果不是多网格这个值不影响使用

#### 3.2.6CLUSTER_ID

ef-productpage-bootstrap-metadata-CLUSTER_ID.yaml

kubectl apply -f ef-productpage-bootstrap-metadata-CLUSTER_ID.yaml -n istio

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: ef-productpage-bootstrap-metadata
spec:
  workloadSelector:
    labels:
      app: productpage
  configPatches:
  - applyTo: BOOTSTRAP
    patch:
      operation: MERGE
      value:
        node:
          metadata:
            CLUSTER_ID: kubernetes2
```

cluster id代表集群的标志，如果是单集群不同值不影响使用

#### 3.2.7NAME

ef-productpage-bootstrap-metadata-NAME.yaml

kubectl apply -f ef-productpage-bootstrap-metadata-NAME.yaml -n istio

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: ef-productpage-bootstrap-metadata
spec:
  workloadSelector:
    labels:
      app: productpage
  configPatches:
  - applyTo: BOOTSTRAP
    patch:
      operation: MERGE
      value:
        node:
          metadata:
            NAME: productpage-v1-test
```

这个name一般是pod的名称，同一名称空间下一般唯一

#### 3.2.8POD_PORTS

ef-productpage-bootstrap-metadata-POD_PORTS.yaml

kubectl apply -f ef-productpage-bootstrap-metadata-POD_PORTS.yaml -n istio

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: ef-productpage-bootstrap-metadata
spec:
  workloadSelector:
    labels:
      app: productpage
  configPatches:
  - applyTo: BOOTSTRAP
    patch:
      operation: MERGE
      value:
        node:
          metadata:
            POD_PORTS: "[{"containerPort":9081,"protocol":"TCP"}]"
```

这个端口乱设置会导致启动不了。

#### 3.2.9APP_CONTAINERS

ef-productpage-bootstrap-metadata-APP_CONTAINERS.yaml

kubectl apply -f ef-productpage-bootstrap-metadata-APP_CONTAINERS.yaml -n istio

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: ef-productpage-bootstrap-metadata
spec:
  workloadSelector:
    labels:
      app: productpage
  configPatches:
  - applyTo: BOOTSTRAP
    patch:
      operation: MERGE
      value:
        node:
          metadata:
            APP_CONTAINERS: test
```

这个值可以随便设置

#### 3.2.10WORKLOAD_NAME

ef-productpage-bootstrap-metadata-WORKLOAD_NAME.yaml

kubectl apply -f ef-productpage-bootstrap-metadata-WORKLOAD_NAME.yaml -n istio

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: ef-productpage-bootstrap-metadata
spec:
  workloadSelector:
    labels:
      app: productpage
  configPatches:
  - applyTo: BOOTSTRAP
    patch:
      operation: MERGE
      value:
        node:
          metadata:
            WORKLOAD_NAME: test
```

这个值可以随便设置

#### 3.2.11SERVICE_ACCOUNT

ef-productpage-bootstrap-metadata-SERVICE_ACCOUNT.yaml

kubectl apply -f ef-productpage-bootstrap-metadata-SERVICE_ACCOUNT.yaml -n istio

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: ef-productpage-bootstrap-metadata
spec:
  workloadSelector:
    labels:
      app: productpage
  configPatches:
  - applyTo: BOOTSTRAP
    patch:
      operation: MERGE
      value:
        node:
          metadata:
            SERVICE_ACCOUNT: test
```

这个值不能随便指定，不然会启动不了

#### 3.2.12PROXY_CONFIG

##### 3.2.12.1proxyAdminPort

ef-productpage-bootstrap-metadata-PROXY_CONFIG-proxyAdminPort.yaml

kubectl apply -f ef-productpage-bootstrap-metadata-PROXY_CONFIG-proxyAdminPort.yaml -n istio

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: ef-productpage-bootstrap-metadata
spec:
  workloadSelector:
    labels:
      app: productpage
  configPatches:
  - applyTo: BOOTSTRAP
    patch:
      operation: MERGE
      value:
        node:
          metadata:
            PROXY_CONFIG:
               "controlPlaneAuthPolicy": "MUTUAL_TLS"
               "configPath": "./etc/istio/proxy"
               "concurrency": 2
               "drainDuration": "45s"
               "tracing": 
                  "zipkin": 
                    "address": "zipkin.istio-system:9411"
               "statusPort": 15020
               "proxyAdminPort": 9999
               "discoveryAddress": "istiod.istio-system.svc:15012"
               "parentShutdownDuration": "60s"
               "binaryPath": "/usr/local/bin/envoy"
               "statNameLength": 189
               "terminationDrainDuration": "5s"
               "serviceCluster": "istio-proxy"
```

这些元数据不影响运行

### 3.2.2locality

ef-productpage-bootstrap-locality.yaml

kubectl apply -f ef-productpage-bootstrap-locality.yaml -n istio

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: ef-productpage-bootstrap
spec:
  workloadSelector:
    labels:
      app: productpage
  configPatches:
  - applyTo: BOOTSTRAP
    patch:
      operation: MERGE
      value:
        node:
          locality:
            region: en-us
            zone: east
            sub_zone: rack-a
```



### 3.2.3id

ef-productpage-bootstrap-id.yaml

kubectl apply -f ef-productpage-bootstrap-id.yaml -n istio

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: ef-productpage-bootstrap
spec:
  workloadSelector:
    labels:
      app: productpage
  configPatches:
  - applyTo: BOOTSTRAP
    patch:
      operation: MERGE
      value:
        node:
          id: test
```

id不能随便设置，不然启动不了

### 3.2.4cluster

ef-productpage-bootstrap-cluster.yaml

kubectl apply -f ef-productpage-bootstrap-cluster.yaml -n istio

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: ef-productpage-bootstrap
spec:
  workloadSelector:
    labels:
      app: productpage
  configPatches:
  - applyTo: BOOTSTRAP
    patch:
      operation: MERGE
      value:
        node:
          cluster: test
```

### 3.2.5user_agent_name

ef-productpage-bootstrap-user_agent_name.yaml

kubectl apply -f ef-productpage-bootstrap-user_agent_name.yaml -n istio

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: ef-productpage-bootstrap
spec:
  workloadSelector:
    labels:
      app: productpage
  configPatches:
  - applyTo: BOOTSTRAP
    patch:
      operation: MERGE
      value:
        node:
          user_agent_name: test
```

## 3.3cluster_manager

https://www.envoyproxy.io/docs/envoy/latest/intro/arch_overview/upstream/cluster_manager#arch-overview-cluster-manager



ef-bootstrap-cluster_manager.yaml

kubectl apply -f ef-bootstrap-cluster_manager.yaml -n istio

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: ef-bootstrap
spec:
  workloadSelector:
    labels:
      app: productpage
  configPatches:
  - applyTo: BOOTSTRAP
    patch:
      operation: MERGE
      value:
        cluster_manager:
          #local_cluster_name: productpage
          outlier_detection:
            event_log_path: /dev/null
          load_stats_config:
            api_type: "GRPC"
            transport_api_version: "V3"
            grpc_services:
            - envoy_grpc:
                cluster_name: xds-grpc
              timeout: 60s
            set_node_on_first_message_only: true
            rate_limit_settings:
              max_tokens: 100 
              fill_rate: 10
  - applyTo: BOOTSTRAP
    patch:
      operation: MERGE
      value:
        static_resources:
          clusters:
          - name: "productpage"    
            type: "STATIC"
            connect_timeout: "0.250s"
            load_assignment:
              cluster_name: "productpage"
              endpoints:
              - lb_endpoints:
                - endpoint:
                    address:
                      socket_address:
                        address: "127.0.0.1"
                        port_value: 9080
```

local_cluster_name必须在静态cluster中配置





## 3.4static_resources

静态资源配置

### 3.4.1listeners

ef-bootstrap-static-resource-listener.yaml

kubectl apply -f ef-bootstrap-static-resource-listener.yaml -n istio

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: ef-bootstrap
spec:
  workloadSelector:
    labels:
      app: productpage
  configPatches:
  - applyTo: BOOTSTRAP
    patch:
      operation: MERGE
      value:
        static_resources:
          listeners:
          - address:
              socket_address:
                address: 0.0.0.0
                port_value: 12111
            traffic_direction: INBOUND
            filter_chains:
            - filters:
              -   name: envoy.filters.network.http_connection_manager
                  typedConfig:
                    '@type': type.googleapis.com/envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager
                    stat_prefix: "bookinfo"
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
                          direct_response:
                            status: 200
                            body: 
                              inline_string: "prefix" 
                    http_filters:
                    - name: "envoy.filters.http.router"
                      typed_config:
                        "@type": "type.googleapis.com/envoy.extensions.filters.http.router.v3.Router"
```

vs-bookinfo-12111.yaml

kubectl apply -f vs-bookinfo-12111.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: bookinfo
spec:
  hosts:
  - "*"
  gateways:
  - bookinfo-gateway
  http:
  - match:
    - uri:
        prefix: /
    route:
    - destination:
        host: productpage.istio.svc.cluster.local
        port:
          number: 12111
```



### 3.4.2clusters

ef-bootstrap-static-resource-clusters.yaml

kubectl apply -f ef-bootstrap-static-resource-clusters.yaml -n istio

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: ef-bootstrap
spec:
  workloadSelector:
    labels:
      app: productpage
  configPatches:
  - applyTo: BOOTSTRAP
    patch:
      operation: MERGE
      value:
        static_resources:
          clusters:
          - name: "productpage"    
            type: "STATIC"
            connect_timeout: "0.250s"
            load_assignment:
              cluster_name: "productpage"
              endpoints:
              - lb_endpoints:
                - endpoint:
                    address:
                      socket_address:
                        address: "127.0.0.1"
                        port_value: 9080
```

ef-bootstrap-static-resource-listener-productpage.yaml

kubectl apply -f ef-bootstrap-static-resource-listener-productpage.yaml -n istio

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: ef-bootstrap-productpage
spec:
  workloadSelector:
    labels:
      app: productpage
  configPatches:
  - applyTo: BOOTSTRAP
    patch:
      operation: MERGE
      value:
        static_resources:
          listeners:
          - address:
              socket_address:
                address: 0.0.0.0
                port_value: 12111
            traffic_direction: INBOUND
            filter_chains:
            - filters:
              -   name: envoy.filters.network.http_connection_manager
                  typedConfig:
                    '@type': type.googleapis.com/envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager
                    stat_prefix: "bookinfo"
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
                            cluster: productpage
                    http_filters:
                    - name: "envoy.filters.http.router"
                      typed_config:
                        "@type": "type.googleapis.com/envoy.extensions.filters.http.router.v3.Router"
```

vs-bookinfo-12111.yaml

kubectl apply -f vs-bookinfo-12111.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: bookinfo
spec:
  hosts:
  - "*"
  gateways:
  - bookinfo-gateway
  http:
  - match:
    - uri:
        prefix: /
    route:
    - destination:
        host: productpage.istio.svc.cluster.local
        port:
          number: 12111
```



### 3.4.3secrets

#### 3.4.3.1tls_certificate

ef-bootstrap-static-resource-secrets-tls_certificate.yaml

kubectl apply -f ef-bootstrap-static-resource-secrets-tls_certificate.yaml -n istio

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: ef-bootstrap
spec:
  workloadSelector:
    labels:
      app: productpage
  configPatches:
  - applyTo: BOOTSTRAP
    patch:
      operation: MERGE
      value:
        static_resources:
          secrets:
          - name: default-test
            tls_certificate: 
              certificate_chain:
                inline_bytes: "LS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0tCk1JSURTekNDQWpPZ0F3SUJBZ0lRREV2TnBYY0FzQ3FDSWI2TjBvWXY5ekFOQmdrcWhraUc5dzBCQVFzRkFEQVkKTVJZd0ZBWURWUVFLRXcxamJIVnpkR1Z5TG14dlkyRnNNQjRYRFRJeU1ETXlNekEwTURFek9Gb1hEVEl5TURNeQpOREEwTURNek9Gb3dBRENDQVNJd0RRWUpLb1pJaHZjTkFRRUJCUUFEZ2dFUEFEQ0NBUW9DZ2dFQkFOTFNZSEl6CklTa3JxY1M5Q3pidFp3OVk0Rm43RngzdzJySDVOZkpGS1VRanh2N2UvQjI0eGJhVFB6ZFNLYjFaajNZeWFRVjEKOXAxZXNoZXRtZVJkRGZMeUxBdktRc3k1VGJLOGxlSWNLeU85SW94dnZTQU0rZDlicEhSUFhlU3o2NmdweDlvQwpVK3BidTcrZkY4dlNBK3dlS1FnTHJhVzAzYUtDUjZ1VVlES1J4UlJpMm5KYUFmWTg2S0tOMHBlS1ZKOVhIcVdFCmxvRUdrWW1LTmlZZDdZS0dmbDc1V0ZoTFBtT0Rjb0VZNVFZL3Q0emtmYVF4QW5XWklaaWVybGhqb2JJZ3lDMnEKNENQTkk1REI4S1JCc1JFaDN5enpiMGxDS3plcHRVanY0ZUQ5L0hwRGVoM2NBRXcvUjhhK0FvQ0ZaajBOelBBTAptS2pIZHZKUFpxQS9nTDBDQXdFQUFhT0JxRENCcFRBT0JnTlZIUThCQWY4RUJBTUNCYUF3SFFZRFZSMGxCQll3CkZBWUlLd1lCQlFVSEF3RUdDQ3NHQVFVRkJ3TUNNQXdHQTFVZEV3RUIvd1FDTUFBd0h3WURWUjBqQkJnd0ZvQVUKOVIzbk5jQ2FHd0N6ajdtNVhuV3BGdkVTcTZJd1JRWURWUjBSQVFIL0JEc3dPWVkzYzNCcFptWmxPaTh2WTJ4MQpjM1JsY2k1c2IyTmhiQzl1Y3k5cGMzUnBieTl6WVM5aWIyOXJhVzVtYnkxd2NtOWtkV04wY0dGblpUQU5CZ2txCmhraUc5dzBCQVFzRkFBT0NBUUVBTGxSS2RWbkxvaW5sdlphQUpyWGQzaGI2NjNxTmxialpXVkg0TXZJK0cvVG4KeVhUWGNPNTFrdkZMQVVUVmNPWmtsNGVyN3k2cXJmRWlETFhxSFRVenN1NGtuVUhzK3hNNnFMcFF1eVJkNkpGVAp3U1p6VHI5cFlaZFVmeWxubnBVQlRHa054WkFSTy9BQU9XZU5jempwWSsvQ041eHJrOWhnK3dxRldKNHhvZ2hjCmpOZE13RWZ0NnF3bkV5VDRNUllVQi9HZFR5WW5RcERSWHlyRWViU2oweElGcWdBaHc0VTVkU0FhR0hLZFB2WVEKamRWNTdRbDBnS2lDM1Zya294VWN0SkNSdW9wNW9hRnl4bFJzWmRvdjdqdmErRDlVVUQ0YXhMa0d6dlpBaW91MwpFVkpRd3F2Z0lKNldsWk40WDRyMHk4Y1pkdTNTaTQ2S1dlZjhoYUp2R3c9PQotLS0tLUVORCBDRVJUSUZJQ0FURS0tLS0tCi0tLS0tQkVHSU4gQ0VSVElGSUNBVEUtLS0tLQpNSUlDL0RDQ0FlU2dBd0lCQWdJUWJqSkltdER1UnNhdVQ2dFBWVmQ2dERBTkJna3Foa2lHOXcwQkFRc0ZBREFZCk1SWXdGQVlEVlFRS0V3MWpiSFZ6ZEdWeUxteHZZMkZzTUI0WERUSXlNRE15TWpBek5UY3lNRm9YRFRNeU1ETXgKT1RBek5UY3lNRm93R0RFV01CUUdBMVVFQ2hNTlkyeDFjM1JsY2k1c2IyTmhiRENDQVNJd0RRWUpLb1pJaHZjTgpBUUVCQlFBRGdnRVBBRENDQVFvQ2dnRUJBS1VhUGU4UldUQTRjRjdSUW5iRlFUK3JoaWlKSk9MUy91bUM2MEorClVIZjI2ekVKL2Z3QXBudGlLTTBLaTR6Zm5xK3pqM1k1azlFTEthR0M2cnNmNjNQVEJRRE5vRXExaTlhWXRraTYKY21CYkNLQlN5NGV3MndMSkxOZzN0VmJIK1orekgwY1dQQmhQUHo1MDZLRWx2Qjl4dVB1czVwWkltM25Ha3E3bQpDYmlzL1hSZGRXbkJZTFpLQWhxcSt5QS9RMkhqSkxsc0xHejJ5Y21KbEZGc3FqeGlQdlg5SmlZYUNKWDdOTTRkCk52YlhVOXROeTZsc0xiUWpkWTJHOXNlRWE4cGpFNW1OVnRWWHN5QXJma1VoK0VuZCtGN2g3bWowUDViR1FvSW4KZ0xkdmd0MU5vbnJQNUUraGx0ZWsxVzlJZEYzdk9zVGZINFI4STArZklCejNqOWNDQXdFQUFhTkNNRUF3RGdZRApWUjBQQVFIL0JBUURBZ0lFTUE4R0ExVWRFd0VCL3dRRk1BTUJBZjh3SFFZRFZSME9CQllFRlBVZDV6WEFtaHNBCnM0KzV1VjUxcVJieEVxdWlNQTBHQ1NxR1NJYjNEUUVCQ3dVQUE0SUJBUUNIUDdEbTFNTy9YRmEwSDV5QTJzYWMKRTlrWHdmeHN3WUEyaWtqR2F0RTFXY0cxaHhkWUJWVlpGRy9CUE8rM1NWZjB6QjVtR3kzemo5WGVta2dKMGsrKwpTOFp3M0RBcE9WcHpWVU0rRE91T3A1TjhNLy9rZ0xtRWdwSUswN29OS3RpUUMrc1hVMkF6alpIdHdUU1I4UzN6Ck9jK1YyUGh4VlBkVTFIZHlOaExDUW8yVTZHNHFDQU9pN3RkQ292UElJVmRvVGpzVUFhTFFLK2hLbU1uVFBFWTgKeGFvNy9VZXlsYmVNZTBNY3AyZnJLckFOdG9MV1hPM29tNXFkb3lVMVdDUlpuc3gxVCs4ZnJldHd5NGlra1dzdgpKL1MzdkxmSktROGJWLzRZWTlsNEtjSEtKN281d29YRllLT3d6YW5tSS9TZmtoK0RLbkYvZk4rSDlUd0JDSDFFCi0tLS0tRU5EIENFUlRJRklDQVRFLS0tLS0K"
              private_key: 
                inline_bytes: "W3JlZGFjdGVkXQ=="

```



#### 3.4.3.2session_ticket_keys

ef-bootstrap-static-resource-secrets-session_ticket_keys.yaml

kubectl apply -f ef-bootstrap-static-resource-secrets-session_ticket_keys.yaml -n istio

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: ef-bootstrap
spec:
  workloadSelector:
    labels:
      app: productpage
  configPatches:
  - applyTo: BOOTSTRAP
    patch:
      operation: MERGE
      value:
        static_resources:
          secrets:
          - name: keys-test
            session_ticket_keys: 
              keys:
              - inline_string: "haqlrzi4qUl3v0s/WNaZAMX1uBZfgbSVBzmavWhQ6TeILL/feHp09JJiZVa7ebOnusC1tSs+7lwlrwZmjtG4J7YRY/obsEFlA+q/Fzztq40="
```

rand -base64 80

#### 3.4.3.3validation_context

ef-bootstrap-static-resource-secrets-validation_context.yaml

kubectl apply -f ef-bootstrap-static-resource-secrets-validation_context.yaml -n istio

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: ef-bootstrap
spec:
  workloadSelector:
    labels:
      app: productpage
  configPatches:
  - applyTo: BOOTSTRAP
    patch:
      operation: MERGE
      value:
        static_resources:
          secrets:
          - name: ROOTCA-test
            validation_context: 
              trusted_ca:
                inline_bytes: "LS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0tCk1JSUMvRENDQWVTZ0F3SUJBZ0lRYmpKSW10RHVSc2F1VDZ0UFZWZDZ0REFOQmdrcWhraUc5dzBCQVFzRkFEQVkKTVJZd0ZBWURWUVFLRXcxamJIVnpkR1Z5TG14dlkyRnNNQjRYRFRJeU1ETXlNakF6TlRjeU1Gb1hEVE15TURNeApPVEF6TlRjeU1Gb3dHREVXTUJRR0ExVUVDaE1OWTJ4MWMzUmxjaTVzYjJOaGJEQ0NBU0l3RFFZSktvWklodmNOCkFRRUJCUUFEZ2dFUEFEQ0NBUW9DZ2dFQkFLVWFQZThSV1RBNGNGN1JRbmJGUVQrcmhpaUpKT0xTL3VtQzYwSisKVUhmMjZ6RUovZndBcG50aUtNMEtpNHpmbnEremozWTVrOUVMS2FHQzZyc2Y2M1BUQlFETm9FcTFpOWFZdGtpNgpjbUJiQ0tCU3k0ZXcyd0xKTE5nM3RWYkgrWit6SDBjV1BCaFBQejUwNktFbHZCOXh1UHVzNXBaSW0zbkdrcTdtCkNiaXMvWFJkZFduQllMWktBaHFxK3lBL1EySGpKTGxzTEd6MnljbUpsRkZzcWp4aVB2WDlKaVlhQ0pYN05NNGQKTnZiWFU5dE55NmxzTGJRamRZMkc5c2VFYThwakU1bU5WdFZYc3lBcmZrVWgrRW5kK0Y3aDdtajBQNWJHUW9JbgpnTGR2Z3QxTm9uclA1RStobHRlazFXOUlkRjN2T3NUZkg0UjhJMCtmSUJ6M2o5Y0NBd0VBQWFOQ01FQXdEZ1lEClZSMFBBUUgvQkFRREFnSUVNQThHQTFVZEV3RUIvd1FGTUFNQkFmOHdIUVlEVlIwT0JCWUVGUFVkNXpYQW1oc0EKczQrNXVWNTFxUmJ4RXF1aU1BMEdDU3FHU0liM0RRRUJDd1VBQTRJQkFRQ0hQN0RtMU1PL1hGYTBINXlBMnNhYwpFOWtYd2Z4c3dZQTJpa2pHYXRFMVdjRzFoeGRZQlZWWkZHL0JQTyszU1ZmMHpCNW1HeTN6ajlYZW1rZ0owaysrClM4WnczREFwT1ZwelZVTStET3VPcDVOOE0vL2tnTG1FZ3BJSzA3b05LdGlRQytzWFUyQXpqWkh0d1RTUjhTM3oKT2MrVjJQaHhWUGRVMUhkeU5oTENRbzJVNkc0cUNBT2k3dGRDb3ZQSUlWZG9UanNVQWFMUUsraEttTW5UUEVZOAp4YW83L1VleWxiZU1lME1jcDJmcktyQU50b0xXWE8zb201cWRveVUxV0NSWm5zeDFUKzhmcmV0d3k0aWtrV3N2CkovUzN2TGZKS1E4YlYvNFlZOWw0S2NIS0o3bzV3b1hGWUtPd3phbm1JL1Nma2grREtuRi9mTitIOVR3QkNIMUUKLS0tLS1FTkQgQ0VSVElGSUNBVEUtLS0tLQ=="
```



#### 3.4.3.4generic_secret

ef-bootstrap-static-resource-secrets-generic_secret.yaml

kubectl apply -f ef-bootstrap-static-resource-secrets-generic_secret.yaml -n istio

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: ef-bootstrap
spec:
  workloadSelector:
    labels:
      app: productpage
  configPatches:
  - applyTo: BOOTSTRAP
    patch:
      operation: MERGE
      value:
        static_resources:
          secrets:
          - name: generic-test
            generic_secret: 
              secret: 
                inline_bytes: "LS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0tCk1JSUMvRENDQWVTZ0F3SUJBZ0lRYmpKSW10RHVSc2F1VDZ0UFZWZDZ0REFOQmdrcWhraUc5dzBCQVFzRkFEQVkKTVJZd0ZBWURWUVFLRXcxamJIVnpkR1Z5TG14dlkyRnNNQjRYRFRJeU1ETXlNakF6TlRjeU1Gb1hEVE15TURNeApPVEF6TlRjeU1Gb3dHREVXTUJRR0ExVUVDaE1OWTJ4MWMzUmxjaTVzYjJOaGJEQ0NBU0l3RFFZSktvWklodmNOCkFRRUJCUUFEZ2dFUEFEQ0NBUW9DZ2dFQkFLVWFQZThSV1RBNGNGN1JRbmJGUVQrcmhpaUpKT0xTL3VtQzYwSisKVUhmMjZ6RUovZndBcG50aUtNMEtpNHpmbnEremozWTVrOUVMS2FHQzZyc2Y2M1BUQlFETm9FcTFpOWFZdGtpNgpjbUJiQ0tCU3k0ZXcyd0xKTE5nM3RWYkgrWit6SDBjV1BCaFBQejUwNktFbHZCOXh1UHVzNXBaSW0zbkdrcTdtCkNiaXMvWFJkZFduQllMWktBaHFxK3lBL1EySGpKTGxzTEd6MnljbUpsRkZzcWp4aVB2WDlKaVlhQ0pYN05NNGQKTnZiWFU5dE55NmxzTGJRamRZMkc5c2VFYThwakU1bU5WdFZYc3lBcmZrVWgrRW5kK0Y3aDdtajBQNWJHUW9JbgpnTGR2Z3QxTm9uclA1RStobHRlazFXOUlkRjN2T3NUZkg0UjhJMCtmSUJ6M2o5Y0NBd0VBQWFOQ01FQXdEZ1lEClZSMFBBUUgvQkFRREFnSUVNQThHQTFVZEV3RUIvd1FGTUFNQkFmOHdIUVlEVlIwT0JCWUVGUFVkNXpYQW1oc0EKczQrNXVWNTFxUmJ4RXF1aU1BMEdDU3FHU0liM0RRRUJDd1VBQTRJQkFRQ0hQN0RtMU1PL1hGYTBINXlBMnNhYwpFOWtYd2Z4c3dZQTJpa2pHYXRFMVdjRzFoeGRZQlZWWkZHL0JQTyszU1ZmMHpCNW1HeTN6ajlYZW1rZ0owaysrClM4WnczREFwT1ZwelZVTStET3VPcDVOOE0vL2tnTG1FZ3BJSzA3b05LdGlRQytzWFUyQXpqWkh0d1RTUjhTM3oKT2MrVjJQaHhWUGRVMUhkeU5oTENRbzJVNkc0cUNBT2k3dGRDb3ZQSUlWZG9UanNVQWFMUUsraEttTW5UUEVZOAp4YW83L1VleWxiZU1lME1jcDJmcktyQU50b0xXWE8zb201cWRveVUxV0NSWm5zeDFUKzhmcmV0d3k0aWtrV3N2CkovUzN2TGZKS1E4YlYvNFlZOWw0S2NIS0o3bzV3b1hGWUtPd3phbm1JL1Nma2grREtuRi9mTitIOVR3QkNIMUUKLS0tLS1FTkQgQ0VSVElGSUNBVEUtLS0tLQ=="
```



## 3.5dynamic_resources

ef-bootstrap-dynamic_resources.yaml

kubectl apply -f ef-bootstrap-dynamic_resources.yaml -n istio

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: ef-bootstrap
spec:
  workloadSelector:
    labels:
      app: productpage
  configPatches:
  - applyTo: BOOTSTRAP
    patch:
      operation: MERGE
      value:
        dynamic_resources:
          lds_config:
            ads: { }
            initial_fetch_timeout: "0s"
            resource_api_version: "V3"
          cds_config:
            ads: { }
            initial_fetch_timeout: "0s"
            resource_api_version: "V3"
          ads_config:
            api_type: "GRPC"
            transport_api_version: "V3"
            #grpc_services:
            #- envoy_grpc:
            #    cluster_name: xds-grpc
            #  timeout: 60s
            set_node_on_first_message_only: true
            rate_limit_settings:
              max_tokens: 100 
              fill_rate: 10
```

数组类合并会产生多个，可以考虑先删除再创建

## 3.6hds_config

ef-bootstrap-hds_config.yaml

kubectl apply -f ef-bootstrap-hds_config.yaml -n istio

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: ef-bootstrap
spec:
  workloadSelector:
    labels:
      app: productpage
  configPatches:
  - applyTo: BOOTSTRAP
    patch:
      operation: MERGE
      value:
          hds_config:
            api_type: "GRPC"
            transport_api_version: "V3"
            grpc_services:
            - envoy_grpc:
                cluster_name: xds-grpc
              timeout: 60s
            set_node_on_first_message_only: true
            rate_limit_settings:
              max_tokens: 100 
              fill_rate: 10
```

## 3.7stats_sinks

- [envoy.stat_sinks.dog_statsd](https://www.envoyproxy.io/docs/envoy/latest/api-v3/config/metrics/v3/stats.proto#extension-envoy-stat-sinks-dog-statsd)
- [envoy.stat_sinks.graphite_statsd](https://www.envoyproxy.io/docs/envoy/latest/api-v3/extensions/stat_sinks/graphite_statsd/v3/graphite_statsd.proto#extension-envoy-stat-sinks-graphite-statsd)
- [envoy.stat_sinks.hystrix](https://www.envoyproxy.io/docs/envoy/latest/api-v3/config/metrics/v3/stats.proto#extension-envoy-stat-sinks-hystrix)
- [envoy.stat_sinks.metrics_service](https://www.envoyproxy.io/docs/envoy/latest/api-v3/config/metrics/v3/metrics_service.proto#extension-envoy-stat-sinks-metrics-service)
- [envoy.stat_sinks.statsd](https://www.envoyproxy.io/docs/envoy/latest/api-v3/config/metrics/v3/stats.proto#extension-envoy-stat-sinks-statsd)
- [envoy.stat_sinks.wasm](https://www.envoyproxy.io/docs/envoy/latest/api-v3/extensions/stat_sinks/wasm/v3/wasm.proto#extension-envoy-stat-sinks-wasm)

### 3.7.1hystrix

ef-bootstrap-stats_sinks-hystrix.yaml

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: ef-bootstrap
spec:
  workloadSelector:
    labels:
      app: productpage
  configPatches:
  - applyTo: BOOTSTRAP
    patch:
      operation: MERGE
      value:
          stats_sinks:
          - name: envoy.stat_sinks.hystrix
            typed_config:
              "@type": type.googleapis.com/envoy.config.metrics.v3.HystrixSink
              num_buckets: 10
```

### 3.7.2metrics_service

ef-bootstrap-stats_sinks-metrics_service.yaml

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: ef-bootstrap
spec:
  workloadSelector:
    labels:
      app: productpage
  configPatches:
  - applyTo: BOOTSTRAP
    patch:
      operation: MERGE
      value:
          stats_sinks:
          - name: envoy.stat_sinks.metrics_service
            typed_config:
              "@type": type.googleapis.com/envoy.config.metrics.v3.MetricsServiceConfig
              transport_api_version: V3
              grpc_service:
                envoy_grpc:
                  cluster_name: test-metrics-service
                timeout: 60s
              report_counters_as_deltas: true
              emit_tags_as_labels: true
```

启动会报错，因为我们没有实现grpc形式的metrics_service

### 3.7.3dog_statsd

ef-bootstrap-stats_sinks-dog_statsd.yaml

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: ef-bootstrap
spec:
  workloadSelector:
    labels:
      app: productpage
  configPatches:
  - applyTo: BOOTSTRAP
    patch:
      operation: MERGE
      value:
          stats_sinks:
          - name: envoy.stat_sinks.dog_statsd
            typed_config:
              "@type": type.googleapis.com/envoy.config.metrics.v3.DogStatsdSink
              address: 
                socket_address:
                  address: 192.168.229.111
                  port_value: 9999
              max_bytes_per_datagram: 512
              prefix: productpage
```

socket_address填写实际dogstatd的地址

### 3.7.4graphite_statsd

ef-bootstrap-stats_sinks-graphite_statsd.yaml

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: ef-bootstrap
spec:
  workloadSelector:
    labels:
      app: productpage
  configPatches:
  - applyTo: BOOTSTRAP
    patch:
      operation: MERGE
      value:
          stats_sinks:
          - name: envoy.stat_sinks.graphite_statsd
            typed_config:
              "@type": type.googleapis.com/envoy.extensions.stat_sinks.graphite_statsd.v3.GraphiteStatsdSink
              address: 
                socket_address:
                  address: 192.168.229.111
                  port_value: 9999
              max_bytes_per_datagram: 512
              prefix: productpage
```

报proto不存在，可能还不支持

### 3.7.5statsd

ef-bootstrap-stats_sinks-statsd.yaml

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: ef-bootstrap
spec:
  workloadSelector:
    labels:
      app: productpage
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
                  address: 192.168.229.111
                  port_value: 9999
              prefix: productpage
```

### 3.7.6wasm

ef-bootstrap-stats_sinks-wasm.yaml

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: ef-bootstrap
spec:
  workloadSelector:
    labels:
      app: productpage
  configPatches:
  - applyTo: BOOTSTRAP
    patch:
      operation: MERGE
      value:
          stats_sinks:
          - name: envoy.stat_sinks.wasm
            typed_config:
              "@type": type.googleapis.com/envoy.extensions.stat_sinks.wasm.v3.Wasm 
              config: 
                root_id: my-wasm-root-id
                vm_config:
                  vm_id: my-wasm-vm-id
                  runtime: envoy.wasm.runtime.v8
                  code:
                    remote:
                      http_uri:
                        uri: http://my-wasm-binary-uri
                configuration:
                  "@type": "type.googleapis.com/google.protobuf.StringValue"
                  value: |
                    {}
```

proto有问题

## 3.8stats_config

ef-bootstrap-stats_config.yaml

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: ef-bootstrap
spec:
  workloadSelector:
    labels:
      app: productpage
  configPatches:
  - applyTo: BOOTSTRAP
    patch:
      operation: MERGE
      value:
          stats_config:
            stats_tags:
            - tag_name: envoy.cluster_name2
              regex: "^cluster\\.((.+?)\\.)"
            - "tag_name": "envoy.http_user_agent2"
              "regex": "^http(?=\\.).*?\\.user_agent\\.((.+?)\\.)\\w+?$"
            - "tag_name": "envoy.http_conn_manager_prefix2"
              "regex": "^http\\.((.*?)\\.)"
            - tag_name: app
              fixed_value: productpage
            use_all_default_tags: true
            stats_matcher:
              exclusion_list:
                patterns:
                - prefix: test
                - suffix: test
                - contains: animal
            histogram_bucket_settings:
            - match:
                prefix: productpage
              buckets:
              - 0.5
              - 1
              - 5
              - 10
              - 25
              - 50
              - 100

```

默认有配置，可以了解下，为防止冲突可以先删除默认配置





main.go

```
// Copyright 2020-2021 Tetrate
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package main

import (
        "fmt"

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
        return &metricPluginContext{}
}

type metricPluginContext struct {
        // Embed the default plugin context here,
        // so that we don't need to reimplement all the methods.
        types.DefaultPluginContext
}

// Override types.DefaultPluginContext.
func (ctx *metricPluginContext) NewHttpContext(contextID uint32) types.HttpContext {
        return &metricHttpContext{}
}

type metricHttpContext struct {
        // Embed the default http context here,
        // so that we don't need to reimplement all the methods.
        types.DefaultHttpContext
}

const (
        customHeaderKey         = "my-custom-header"
        customHeaderValueTagKey = "value"
)

var counters = map[string]proxywasm.MetricCounter{}

// Override types.DefaultHttpContext.
func (ctx *metricHttpContext) OnHttpRequestHeaders(numHeaders int, endOfStream bool) types.Action {
        customHeaderValue, err := proxywasm.GetHttpRequestHeader(customHeaderKey)
        if err == nil {
                counter, ok := counters[customHeaderValue]
                if !ok {
                        // This metric is processed as: custom_header_value_counts{value="foo",reporter="wasmgosdk"} n.
                        // The extraction rule is defined in envoy.yaml as a bootstrap configuration.
                        // See https://www.envoyproxy.io/docs/envoy/latest/api-v3/config/metrics/v3/stats.proto#config-metrics-v3-statsconfig.
                        fqn := fmt.Sprintf("custom_header_value_counts_%s=%s_reporter=wasmgosdk", customHeaderValueTagKey, customHeaderValue)
                        counter = proxywasm.DefineCounterMetric(fqn)
                        counters[customHeaderValue] = counter
                }
                counter.Increment(1)
        }
        return types.ActionContinue
}
```

编译：

```
export GOPROXY=https://proxy.golang.com.cn,direct

tinygo build -o main.wasm -scheduler=none -target=wasi main.go

docker build --tag registry.cn-qingdao.aliyuncs.com/hxpdocker/metrics:v1 . --no-cache
docker push registry.cn-qingdao.aliyuncs.com/hxpdocker/metrics:v1
```

wp-ingressgateway-metrics.yaml

kubectl apply -f wp-ingressgateway-metrics.yaml -n istio-system

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
  url: oci://registry.cn-qingdao.aliyuncs.com/hxpdocker/metrics:v1
  phase: STATS	
  imagePullPolicy: Always
```



```
  meshConfig:
      proxyMetadata:
        # Enable dynamic bootstrap generation
        # https://github.com/istio/istio/pull/33456
        BOOTSTRAP_XDS_AGENT: "true"
```

ef-metrics.yaml

kubectl apply -f ef-metrics.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: ef-metrics
spec:
  workloadSelector:
    labels:
      app: productpage
  configPatches:
  - applyTo: BOOTSTRAP
    patch:
      operation: MERGE
      value:
          stats_config:
            stats_tags:
            - tag_name: value
              regex: '(_value=([a-zA-Z]+))'
            - tag_name: myreporter
              regex: '(_reporter=([a-zA-Z]+))'
```

curl   -H "my-custom-header: foo" -XGET http://192.168.229.128:32376/productpage

http://192.168.229.128:15000/stats/prometheus

```
custom_header_value_counts{value="foo",myreporter="wasmgosdk"} 2
```





## 3.9stats_flush_interval

ef-bootstrap-stats_flush_interval.yaml

kubectl apply -f ef-bootstrap-stats_flush_interval.yaml -n istio

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: ef-bootstrap
spec:
  workloadSelector:
    labels:
      app: productpage
  configPatches:
  - applyTo: BOOTSTRAP
    patch:
      operation: MERGE
      value:
          stats_flush_interval: 5000ms
```

## 3.10stats_flush_on_admin

ef-bootstrap-stats_flush_on_admin.yaml

kubectl apply -f ef-bootstrap-stats_flush_on_admin.yaml -n istio

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: ef-bootstrap
spec:
  workloadSelector:
    labels:
      app: productpage
  configPatches:
  - applyTo: BOOTSTRAP
    patch:
      operation: MERGE
      value:
          stats_flush_on_admin: true
```

## 3.11watchdogs

ef-bootstrap-watchdogs.yaml

kubectl  apply -f ef-bootstrap-watchdogs.yaml -n istio

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: ef-bootstrap
spec:
  workloadSelector:
    labels:
      app: productpage
  configPatches:
  - applyTo: BOOTSTRAP
    patch:
      operation: MERGE
      value:
          watchdogs:
            main_thread_watchdog:
              actions:
              - event: KILL
              - event: MULTIKILL
              - event: MEGAMISS
              - event: MISS
              miss_timeout: 30s
              megamiss_timeout: 20s
              kill_timeout: 10s
              max_kill_timeout_jitter: 20s
              multikill_timeout: 20s
              multikill_threshold: 
                value: 70
            worker_watchdog:
              actions:
              - event: KILL
              - event: MULTIKILL
              - event: MEGAMISS
              - event: MISS
              miss_timeout: 30s
              megamiss_timeout: 20s
              kill_timeout: 10s
              max_kill_timeout_jitter: 20s
              multikill_timeout: 20s
              multikill_threshold: 
                value: 70
```

core.v3.TypedExtensionConfig不知道有哪些

## 3.12layered_runtime

ef-bootstrap-layered_runtime.yaml

kubectl apply -f ef-bootstrap-layered_runtime.yaml -n istio

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: ef-bootstrap
spec:
  workloadSelector:
    labels:
      app: productpage
  configPatches:
  - applyTo: BOOTSTRAP
    patch:
      operation: MERGE
      value:
          layered_runtime: 
            layers:
            - name: "rtds_layer"
              rtds_layer: 
                name: rtds
                rtds_config:
                  ads: {}
                  resource_api_version: V3
                  initial_fetch_timeout: 0s
```

admin_layer只能有一个

rtds_layer name可能不能直接指定，运行失败



## 3.13admin

ef-bootstrap-admin.yaml

kubectl apply -f ef-bootstrap-admin.yaml -n istio

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: ef-bootstrap
spec:
  workloadSelector:
    labels:
      app: productpage
  configPatches:
  - applyTo: BOOTSTRAP
    patch:
      operation: MERGE
      value:
          admin: 
            access_log:
            - name: file
              filter:
                status_code_filter:
                  comparison:
                    op: EQ
                    value:
                      default_value: 200 
                      runtime_key: file.log
              typed_config:
                "@type": type.googleapis.com/envoy.extensions.access_loggers.file.v3.FileAccessLog
                path: /dev/null
                log_format: 
                  text_format: "%LOCAL_REPLY_BODY%:%RESPONSE_CODE%:path=%REQ(:path)%\n"
            profile_path: "/var/lib/istio/data/envoy.prof"
            ignore_global_conn_limit: true
            address:
              socket_address: 
                address: "127.0.0.1"
                port_value: 15000
            
```

## 3.14overload_manager

ef-bootstrap-overload_manager.yaml

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: ef-bootstrap
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
              '@type': type.googleapis.com/envoy.extensions.resource_monitors.fixed_heap.v3.FixedHeapConfig
              max_heap_size_bytes: 1073741824.0
```

type还不支持

## 3.15enable_dispatcher_stats

ef-bootstrap-enable_dispatcher_stats.yaml

kubectl apply -f ef-bootstrap-enable_dispatcher_stats.yaml -n istio

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: ef-bootstrap
spec:
  workloadSelector:
    labels:
      app: productpage
  configPatches:
  - applyTo: BOOTSTRAP
    patch:
      operation: MERGE
      value:
        enable_dispatcher_stats: true
```

## 3.16header_prefix

ef-bootstrap-header_prefix.yaml

kubectl apply -f ef-bootstrap-header_prefix.yaml -n istio

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: ef-bootstrap
spec:
  workloadSelector:
    labels:
      app: productpage
  configPatches:
  - applyTo: BOOTSTRAP
    patch:
      operation: MERGE
      value:
        header_prefix: x-envoy
```

## 3.17stats_server_version_override

ef-bootstrap-stats_server_version_override.yaml

kubectl apply -f ef-bootstrap-stats_server_version_override.yaml -n istio

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: ef-bootstrap
spec:
  workloadSelector:
    labels:
      app: productpage
  configPatches:
  - applyTo: BOOTSTRAP
    patch:
      operation: MERGE
      value:
        stats_server_version_override: 16
```

set the value of [server.version statistic](https://www.envoyproxy.io/docs/envoy/latest/configuration/observability/statistics#server-statistics)

## 3.18typed_dns_resolver_config

ef-bootstrap-typed_dns_resolver_config.yaml

kubectl apply -f ef-bootstrap-typed_dns_resolver_config.yaml -n istio

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: ef-bootstrap
spec:
  workloadSelector:
    labels:
      app: productpage
  configPatches:
  - applyTo: BOOTSTRAP
    patch:
      operation: MERGE
      value:
        typed_dns_resolver_config:
          name: envoy.network.dns_resolver.cares
          typed_config:
            '@type': type.googleapis.com/envoy.extensions.network.dns_resolver.cares.v3.CaresDnsResolverConfig
            resolvers:
            - socket_address:
                address: 8.8.8.8
                port_value: 53
            use_resolvers_as_fallback: true
            filter_unroutable_families: true
            dns_resolver_options:
              use_tcp_for_dns_lookups: true
              no_default_search_domain: false
```

- [envoy.network.dns_resolver.apple](https://www.envoyproxy.io/docs/envoy/latest/api-v3/extensions/network/dns_resolver/apple/v3/apple_dns_resolver.proto#extension-envoy-network-dns-resolver-apple)
- [envoy.network.dns_resolver.cares](https://www.envoyproxy.io/docs/envoy/latest/api-v3/extensions/network/dns_resolver/cares/v3/cares_dns_resolver.proto#extension-envoy-network-dns-resolver-cares)

proto找不到

## 3.19bootstrap_extensions

ef-bootstrap-bootstrap_extensions.yaml

kubectl apply -f ef-bootstrap-bootstrap_extensions.yaml -n istio

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: ef-bootstrap
spec:
  workloadSelector:
    labels:
      app: productpage
  configPatches:
  - applyTo: BOOTSTRAP
    patch:
      operation: MERGE
      value:
        bootstrap_extensions:
        - name: envoy.bootstrap.wasm
          typed_config:
            "@type": type.googleapis.com/envoy.extensions.wasm.v3.WasmService
            singleton: true
            config:
              name: my_plugin
              configuration:
                "@type": type.googleapis.com/google.protobuf.StringValue
                value: |
                  {}
              vm_config:
                #runtime: "envoy.wasm.runtime.v8"
                #code:
                #  local:
                #    filename: "/etc/envoy_filter_http_wasm_example.wasm"
               runtime: envoy.wasm.runtime.null
               code:
                 local: { inline_string: "envoy.wasm.attributegen" }
```

valid path: /etc/envoy_filter_http_wasm_example.wasm

This extension category has the following known extensions:

- [envoy.**bootstrap**.wasm](https://www.envoyproxy.io/docs/envoy/latest/api-v3/extensions/wasm/v3/wasm.proto#extension-envoy-bootstrap-wasm)

The following extensions are available in [contrib](https://www.envoyproxy.io/docs/envoy/latest/start/install#install-contrib) images only:

- [envoy.**bootstrap**.vcl](https://www.envoyproxy.io/docs/envoy/latest/api-v3/extensions/vcl/v3alpha/vcl_socket_interface.proto#extension-envoy-bootstrap-vcl)

## 3.20fatal_actions

ef-bootstrap-fatal_actions.yaml

```

```

放弃，文档中没有TypedExtensionConfig 

## 3.21inline_headers

ef-bootstrap-inline_headers.yaml

kubectl apply -f ef-bootstrap-inline_headers.yaml -n istio

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: ef-bootstrap
spec:
  workloadSelector:
    labels:
      app: productpage
  configPatches:
  - applyTo: BOOTSTRAP
    patch:
      operation: MERGE
      value:
        inline_headers:
        - inline_header_name: foo
          inline_header_type: REQUEST_HEADER
```

- REQUEST_HEADER

  *(DEFAULT)* ⁣

- REQUEST_TRAILER

  ⁣

- RESPONSE_HEADER

  ⁣

- RESPONSE_TRAILER
