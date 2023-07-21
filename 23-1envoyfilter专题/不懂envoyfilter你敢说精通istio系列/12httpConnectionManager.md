# 1什么是HttpConnectionManager

HttpConnectionManager是http1，http2，https协议必须经过的一个network filter，名称是envoy.filters.network.http_connection_manager。可以配置大量参数，其中http_filters，route_config最为复杂。http_filters配置http类型的filter，实现各种功能，其中route filter必须有一个，且必须是最后一个过滤器，实现路由功能。route_config用来配置静态路由功能，路由也可以用rds来配置，获取动态路由。HttpConnectionManager的实现原理及配置方法，是一个必须掌握的内容，理解了他就等于对非tcp连接的功能理解了一大半。更深入一点，需要掌握各种http filter的配置。

# 2配置

```
{
  "codec_type": "...",编解码器类型
  "stat_prefix": "...",stat前缀
  "rds": "{...}",动态路由配置
  "route_config": "{...}",静态路由配置
  "scoped_routes": "{...}",根据请求属性，动态路由配置
  "http_filters": [],http过滤器配置
  "add_user_agent": "{...}",是否处理user-agent属性
  "tracing": "{...}",链路跟踪配置
  "common_http_protocol_options": "{...}",http协议选项配置
  "http_protocol_options": "{...}",http1选项
  "http2_protocol_options": "{...}",http2选项
  "server_name": "...",服务器名称
  "server_header_transformation": "...",server_name写入头规则
  "scheme_header_transformation": "{...}",:scheme头处理
  "max_request_headers_kb": "{...}",最大请求头
  "stream_idle_timeout": "{...}",流空闲超时时间
  "request_timeout": "{...}",请求超时时间
  "request_headers_timeout": "{...}",请求头超时时间
  "drain_timeout": "{...}",排水超时时间
  "delayed_close_timeout": "{...}",延迟关闭超时时间
  "access_log": [],访问日志配置
  "use_remote_address": "{...}",使用远程地址
  "xff_num_trusted_hops": "...",x-forwarded-for跳数
  "original_ip_detection_extensions": [],原始ip检测扩展
  "internal_address_config": "{...}",内部地址配置规则
  "skip_xff_append": "...",是否跳过远程地址绑定到 x-forwarded-for
  "via": "...",绑定via头到请求和响应
  "generate_request_id": "{...}",是否产生x-request-id 头
  "preserve_external_request_id": "...",是否保留外部x-request-id
  "always_set_request_id_in_response": "...",是否总是将 x-request-id 放到响应头里
  "forward_client_cert_details": "...",forward x-forwarded-client-cert 头
  "set_current_client_cert_details": "{...}",设置forward_client_cert_details 
  "proxy_100_continue": "...",代理Expect: 100-continue头
  "upgrade_configs": [],http升级配置
  "normalize_path": "{...}",path是否正规化处理
  "merge_slashes": "...",合并斜线
  "path_with_escaped_slashes_action": "...",有escaped斜杠的处理方法
  "request_id_extension": "{...}",request id扩展配置
  "local_reply_config": "{...}",本地响应配置
  "strip_matching_host_port": "...",是否移除匹配的host/authority中的端口
  "strip_any_host_port": "...",是否移除任何host/authority中的端口
  "stream_error_on_invalid_http_message": "{...}",接到错误请求处理
  "strip_trailing_host_dot": "..."是否移除host的最后一个点
}
```

rds：

```
{
  "config_source": "{...}",配置来源
  "route_config_name": "..."路由名称
}
```

config_source：

```
{
  "path": "...",路径
  "api_config_source": "{...}",api来源配置
  "ads": "{...}",ads配置
  "initial_fetch_timeout": "{...}",初始抓取超时时间
  "resource_api_version": "..."api版本
}
```

api_config_source：

```
{
  "api_type": "...",api类型
  "transport_api_version": "...",api版本
  "cluster_names": [],集群名称
  "grpc_services": [],grpc服务
  "refresh_delay": "{...}",刷新延迟
  "request_timeout": "{...}",请求超时时间
  "rate_limit_settings": "{...}",限速设置
  "set_node_on_first_message_only": "..."只在第一个消息设置node
}
```

route_config：

```
{
  "name": "...",路由名称
  "virtual_hosts": [],虚拟主机配置
  "vhds": "{...}",虚拟主机发现服务配置
  "internal_only_headers": [],只在mesh内部使用的头
  "response_headers_to_add": [],添加响应头
  "response_headers_to_remove": [],删除响应头
  "request_headers_to_add": [],添加请求头
  "request_headers_to_remove": [],删除请求头
  "most_specific_header_mutations_wins": "...",最具体的头优先
  "validate_clusters": "{...}",是否校验clusters
  "max_direct_response_body_size_bytes": "{...}"直接响应最大body大小，默认4096字节
}
```

virtual_hosts：

```
{
  "name": "...",虚拟主机名称
  "domains": [],域名
  "routes": [],路由规则
  "require_tls": "...",tls是否必须
  "virtual_clusters": [],虚拟clusters
  "rate_limits": [],限速配置
  "request_headers_to_add": [],添加request头
  "request_headers_to_remove": [],删除request头
  "response_headers_to_add": [],添加response头
  "response_headers_to_remove": [],删除response头
  "cors": "{...}",跨站资源共享配置
  "typed_per_filter_config": "{...}",虚拟主机级别的过滤器配置
  "include_request_attempt_count": "...",x-envoy-attempt-count头是否放到上游请求中
  "include_attempt_count_in_response": "...",x-envoy-attempt-count头是否放到下游响应中
  "retry_policy": "{...}",重试策略
  "hedge_policy": "{...}",对冲策略
  "per_request_buffer_limit_bytes": "{...}"没个请求缓存限值
}
```

routes：

```
{
  "name": "...",路由名称
  "match": "{...}",匹配条件
  "route": "{...}",路由
  "redirect": "{...}",重定向
  "direct_response": "{...}",直接响应
  "metadata": "{...}",元数据
  "decorator": "{...}",装饰
  "typed_per_filter_config": "{...}",路由级别的filter配置
  "request_headers_to_add": [],添加请求头
  "request_headers_to_remove": [],删除请求头
  "response_headers_to_add": [],添加响应头
  "response_headers_to_remove": [],删除响应头
  "tracing": "{...}",覆盖连接管理器级别的tracing
  "per_request_buffer_limit_bytes": "{...}"每个请求缓存大小限值
}
```

match：

```
{
  "prefix": "...",前缀匹配
  "path": "...",精确路径匹配
  "safe_regex": "{...}",正则匹配
  "connect_matcher": "{...}",匹配CONNECT requests
  "case_sensitive": "{...}",大小写是否敏感
  "runtime_fraction": "{...}",运行时百分比
  "headers": [],匹配头
  "query_parameters": [],匹配参数
  "grpc": "{...}",匹配grpc连接
  "tls_context": "{...}",匹配tls
  "dynamic_metadata": []匹配动态元数据
}
```

route：

```
{
  "cluster": "...",集群名称
  "cluster_header": "...",透过请求头，获取要路由的集群
  "weighted_clusters": "{...}",加权的cluster
  "cluster_not_found_response_code": "...",集群没找到响应码
  "metadata_match": "{...}",subset负载均衡匹配的元数据
  "prefix_rewrite": "...",路径前缀重写
  "regex_rewrite": "{...}",路径正则重写
  "host_rewrite_literal": "...",host重写
  "auto_host_rewrite": "{...}",自动host重写为上游服务host
  "host_rewrite_header": "...",用请求头重写host
  "host_rewrite_path_regex": "{...}",用路径正则重写host
  "timeout": "{...}",上游超时时间
  "idle_timeout": "{...}",路由空闲超时时间
  "retry_policy": "{...}",重试策略
  "request_mirror_policies": [],请求镜像策略
  "priority": "...",路由优先级
  "rate_limits": [],限速配置
  "include_vh_rate_limits": "{...}",是否包含虚拟主机级别的限速
  "hash_policy": [],基于hash的路由的hash策略
  "cors": "{...}",跨站资源共享配置
  "max_grpc_timeout": "{...}",废弃
  "grpc_timeout_offset": "{...}",废弃
  "upgrade_configs": [],连接升级配置
  "internal_redirect_policy": "{...}",上游重定向策略
  "internal_redirect_action": "...",上游重定向动作
  "max_internal_redirects": "{...}",最大内部重定向次数
  "hedge_policy": "{...}",对冲策略
  "max_stream_duration": "{...}"流最大时间
}
```

weighted_clusters：

```
{
  "clusters": [],集群配置
  "total_weight": "{...}",总权重大小
  "runtime_key_prefix": "..."运行时key前缀
}
```

hash_policy：

```
{
  "header": "{...}",请求头hash策略
  "cookie": "{...}",cookie hash策略
  "connection_properties": "{...}",连接属性hash策略
  "query_parameter": "{...}",请求参数hash策略
  "filter_state": "{...}",过滤器状态hash策略
  "terminal": "..."最终hash策略
}
```

clusters：

```
{
  "name": "...",集群名称
  "cluster_header": "...",从请求头中获取集群名称
  "weight": "{...}",权重
  "metadata_match": "{...}",元数据匹配
  "request_headers_to_add": [],添加请求头
  "request_headers_to_remove": [],删除请求头
  "response_headers_to_add": [],添加响应头
  "response_headers_to_remove": [],删除响应头
  "typed_per_filter_config": "{...}",cluster级别的filter配置
  "host_rewrite_literal": "..."host重写
}
```

redirect：

```
{
  "https_redirect": "...",schema替换成https
  "scheme_redirect": "...",删除标准pod
  "host_redirect": "...",host替换
  "port_redirect": "...",端口替换
  "path_redirect": "...",路径替换
  "prefix_rewrite": "...",prefix或path替换
  "regex_rewrite": "{...}",path正则重写
  "response_code": "...",响应码，默认301
  "strip_query": "..."删除query部分
}
```

direct_response：

```
{
  "status": "...",状态码
  "body": "{...}"响应体
}
```

virtual_clusters：

```
{
  "headers": [],匹配头
  "name": "..."虚拟集群名称
}
```

rate_limits：

```
{
  "stage": "{...}",stage号码
  "disable_key": "...",禁用限速的key
  "actions": [],动作
  "limit": "{...}"追加到descriptor的限速参数配置
}
```

actions：

```
{
  "source_cluster": "{...}",来源cluster
  "destination_cluster": "{...}",目标cluster
  "request_headers": "{...}",请求头匹配
  "remote_address": "{...}",远程地址匹配
  "generic_key": "{...}",通用key匹配
  "header_value_match": "{...}",请求头匹配值
  "dynamic_metadata": "{...}",动态元数据匹配
  "metadata": "{...}",元数据匹配
  "extension": "{...}"descriptor 扩展匹配
}
```

cors：

```
{
  "allow_origin_string_match": [],允许的源
  "allow_methods": "...",允许的方法
  "allow_headers": "...",允许的头
  "expose_headers": "...",暴露的头
  "max_age": "...",有效时间
  "allow_credentials": "{...}",允许的cookie
  "filter_enabled": "{...}",生效百分比
  "shadow_enabled": "{...}"不生效但是记录百分比
}
```

retry_policy：

```
{
  "retry_on": "...",重试条件
  "num_retries": "{...}",重试次数
  "per_try_timeout": "{...}",每次重试超时时间
  "per_try_idle_timeout": "{...}",每次重试空闲超时时间
  "retry_priority": "{...}",决定负载的重试优先级
  "retry_host_predicate": [],重试host断言
  "retry_options_predicates": [],重试选项断言
  "host_selection_retry_max_attempts": "...",选择host重试最大次数
  "retriable_status_codes": [],除了重试条件以外的重试状态码
  "retry_back_off": "{...}",指数级重试的参数
  "rate_limited_retry_back_off": "{...}",限流条件下的重试等待策略
  "retriable_headers": [],响应头重试触发配置
  "retriable_request_headers": []请求头重试触发配置
}
```

hedge_policy：

```
{
  "hedge_on_per_try_timeout": "..."
}
```

vhds：

```
{
  "config_source": "{...}"配置源
}
```

scoped_routes：

```
{
  "name": "...",路由名称
  "scope_key_builder": "{...}",产生scope key的算法
  "rds_config_source": "{...}",rds配置源
  "scoped_route_configurations_list": "{...}",scoped路由列表
  "scoped_rds": "{...}"scoped路由
}
```

http_filters：

```
{
  "name": "...",过滤器名称
  "typed_config": "{...}",过滤器配置
  "config_discovery": "{...}",配置发现
  "is_optional": "..."是否可选
}
```

tracing：

```
{
  "client_sampling": "{...}",客户端tracing采样率
  "random_sampling": "{...}",随机采样率
  "overall_sampling": "{...}",采样率限值
  "verbose": "...",标记span更多信息
  "max_path_tag_length": "{...}",最长路径长度默认256
  "custom_tags": [],自定义标签
  "provider": "{...}"对接外部服务
}
```

custom_tags：

```
{
  "tag": "...",tag名称
  "literal": "{...}",字面类型的tag
  "environment": "{...}",environment类型tag
  "request_header": "{...}",请求头类型的tag
  "metadata": "{...}"元数据类型的tag
}
```

common_http_protocol_options：

```
{
  "idle_timeout": "{...}",空闲超时时间
  "max_connection_duration": "{...}",最大连接时间
  "max_headers_count": "{...}",最大头数量
  "max_stream_duration": "{...}",最大流时间
  "headers_with_underscores_action": "...",带有下划线的请求头的处理动作
  "max_requests_per_connection": "{...}"每个连接的最大请求数量
}
```

http_protocol_options：

```
{
  "allow_absolute_url": "{...}",是否允许完全url
  "accept_http_10": "...",是否接受http1.0,0.9请求
  "default_host_for_http_10": "...",http1.0请求的默认host
  "header_key_format": "{...}",响应头的key格式
  "enable_trailers": "...",启用trailers 
  "allow_chunked_length": "...",允许带有Content-Length and Transfer-Encoding头的请求
  "override_stream_error_on_invalid_http_message": "{...}"允许无效的http消息
}
```

http2_protocol_options：

```
{
  "hpack_table_size": "{...}",hpack表大小，默认4096
  "max_concurrent_streams": "{...}",最大并发流
  "initial_stream_window_size": "{...}",初始流窗口大小
  "initial_connection_window_size": "{...}",初始连接窗口大小
  "allow_connect": "...",是否允许Websocket 代理
  "max_outbound_frames": "{...}",最大出口帧
  "max_outbound_control_frames": "{...}",最大出口控制帧
  "max_consecutive_inbound_frames_with_empty_payload": "{...}",空内容的最大连续入口帧
  "max_inbound_priority_frames_per_stream": "{...}",每个流的最大入口优先帧
  "max_inbound_window_update_frames_per_data_frame_sent": "{...}",每个数据帧发送的最大入口窗口更新帧
  "stream_error_on_invalid_http_messaging": "...",在错误的htt消息时流错误
  "override_stream_error_on_invalid_http_message": "{...}",当http消息错误时覆盖流错误
  "connection_keepalive": "{...}"连接保持参数
}
```

access_log：

```
{
  "name": "...",日志类型名称
  "filter": "{...}",过滤日志配置
  "typed_config": "{...}"具体配置
}
```

upgrade_configs：

```
{
  "upgrade_type": "...",升级类型
  "filters": [],http过滤器
  "enabled": "{...}"是否启用
}
```

local_reply_config：

```
{
  "mappers": [],映射配置
  "body_format": "{...}"内容格式
}
```

mappers：

```
{
  "filter": "{...}",日志过滤器配置
  "status_code": "{...}",状态码
  "body": "{...}",内容
  "body_format_override": "{...}",映射级别的body格式
  "headers_to_add": []添加头
}
```

filter：

```
{
  "status_code_filter": "{...}",状态码过滤器
  "duration_filter": "{...}",时间过滤器
  "not_health_check_filter": "{...}",非健康检查过滤器
  "traceable_filter": "{...}",是否可跟踪过滤器
  "runtime_filter": "{...}",运行时过滤器
  "and_filter": "{...}",与过滤器
  "or_filter": "{...}",或过滤器
  "header_filter": "{...}",头过滤器
  "response_flag_filter": "{...}",响应标记过滤器
  "grpc_status_filter": "{...}",grpc状态过滤器
  "extension_filter": "{...}",扩展过滤器
  "metadata_filter": "{...}"元数据过滤器
}
```

# 3实战

## 3.1默认配置

envoyfilter/httpconnectionmanager/ef-productpage-general.yaml

kubectl apply -f ef-productpage-general.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: httpconnectionmanager
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
      operation: REPLACE
      value:
              name: envoy.filters.network.http_connection_manager
              typedConfig:
                '@type': type.googleapis.com/envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager
                accessLog:
                - name: envoy.access_loggers.file
                  typedConfig:
                    '@type': type.googleapis.com/envoy.extensions.access_loggers.file.v3.FileAccessLog
                    logFormat:
                      textFormat: |
                        [%START_TIME%] "%REQ(:METHOD)% %REQ(X-ENVOY-ORIGINAL-PATH?:PATH)% %PROTOCOL%" %RESPONSE_CODE% %RESPONSE_FLAGS% %RESPONSE_CODE_DETAILS% %CONNECTION_TERMINATION_DETAILS% "%UPSTREAM_TRANSPORT_FAILURE_REASON%" %BYTES_RECEIVED% %BYTES_SENT% %DURATION% %RESP(X-ENVOY-UPSTREAM-SERVICE-TIME)% "%REQ(X-FORWARDED-FOR)%" "%REQ(USER-AGENT)%" "%REQ(X-REQUEST-ID)%" "%REQ(:AUTHORITY)%" "%UPSTREAM_HOST%" %UPSTREAM_CLUSTER% %UPSTREAM_LOCAL_ADDRESS% %DOWNSTREAM_LOCAL_ADDRESS% %DOWNSTREAM_REMOTE_ADDRESS% %REQUESTED_SERVER_NAME% %ROUTE_NAME%
                    path: /dev/stdout
                delayedCloseTimeout: 1s
                forwardClientCertDetails: SANITIZE_SET
                httpFilters:
                - name: istio.metadata_exchange
                  typedConfig:
                    '@type': type.googleapis.com/udpa.type.v1.TypedStruct
                    typeUrl: type.googleapis.com/envoy.extensions.filters.http.wasm.v3.Wasm
                    value:
                      config:
                        configuration:
                          '@type': type.googleapis.com/google.protobuf.StringValue
                          value: |
                            {}
                        vm_config:
                          code:
                            local:
                              inline_string: envoy.wasm.metadata_exchange
                          runtime: envoy.wasm.runtime.null
                - name: envoy.filters.http.jwt_authn
                  typedConfig:
                    '@type': type.googleapis.com/envoy.extensions.filters.http.jwt_authn.v3.JwtAuthentication
                    providers:
                      origins-0:
                        forward: true
                        issuer: testing@secure.istio.io
                        localJwks:
                          inlineString: "{ \"keys\":\n   [ \n     {\n       \"e\":\"AQAB\",\n
                            \      \"kid\":\"DHFbpoIUqrY8t2zpA2qXfCmr5VO5ZEr4RzHU_-envvQ\",\n
                            \      \"kty\":\"RSA\",\n       \"n\":\"xAE7eB6qugXyCAG3yhh7pkDkT65pHymX-P7KfIupjf59vsdo91bSP9C8H07pSAGQO1MV_xFj9VswgsCg4R6otmg5PV2He95lZdHtOcU5DXIg_pbhLdKXbi66GlVeK6ABZOUW3WYtnNHD-91gVuoeJT_DwtGGcp4ignkgXfkiEm4sw-4sfb4qdt5oLbyVpmW6x9cfa7vs2WTfURiCrBoUqgBo_-4WTiULmmHSGZHOjzwa8WtrtOQGsAFjIbno85jp6MnGGGZPYZbDAa_b3y5u-YpW7ypZrvD8BgtKVjgtQgZhLAGezMt0ua3DRrWnKqTZ0BJ_EyxOGuHJrLsn00fnMQ\"\n
                            \    }\n   ]\n}\n"
                        payloadInMetadata: testing@secure.istio.io
                    rules:
                    - match:
                        prefix: /
                      requires:
                        requiresAny:
                          requirements:
                          - providerName: origins-0
                          - allowMissing: {}
                - name: istio_authn
                  typedConfig:
                    '@type': type.googleapis.com/istio.envoy.config.filter.http.authn.v2alpha1.FilterConfig
                    policy:
                      originIsOptional: true
                      origins:
                      - jwt:
                          issuer: testing@secure.istio.io
                      principalBinding: USE_ORIGIN
                    skipValidateTrustDomain: true
                - name: envoy.filters.http.cors
                  typedConfig:
                    '@type': type.googleapis.com/envoy.extensions.filters.http.cors.v3.Cors
                - name: envoy.filters.http.fault
                  typedConfig:
                    '@type': type.googleapis.com/envoy.extensions.filters.http.fault.v3.HTTPFault
                - name: istio.stats
                  typedConfig:
                    '@type': type.googleapis.com/udpa.type.v1.TypedStruct
                    typeUrl: type.googleapis.com/envoy.extensions.filters.http.wasm.v3.Wasm
                    value:
                      config:
                        configuration:
                          '@type': type.googleapis.com/google.protobuf.StringValue
                          value: |
                            {
                              "debug": "false",
                              "stat_prefix": "istio",
                              "disable_host_header_fallback": true
                            }
                        root_id: stats_outbound
                        vm_config:
                          code:
                            local:
                              inline_string: envoy.wasm.stats
                          runtime: envoy.wasm.runtime.null
                          vm_id: stats_outbound
                - name: envoy.filters.http.router
                  typedConfig:
                    '@type': type.googleapis.com/envoy.extensions.filters.http.router.v3.Router
                httpProtocolOptions: {}
                normalizePath: true
                pathWithEscapedSlashesAction: KEEP_UNCHANGED
                rds:
                  configSource:
                    ads: {}
                    initialFetchTimeout: 0s
                    resourceApiVersion: V3
                  routeConfigName: http.8080
                serverName: istio-envoy
                setCurrentClientCertDetails:
                  cert: true
                  dns: true
                  subject: true
                  uri: true
                statPrefix: outbound_0.0.0.0_8080
                streamIdleTimeout: 0s
                tracing:
                  clientSampling:
                    value: 100
                  customTags:
                  - metadata:
                      kind:
                        request: {}
                      metadataKey:
                        key: envoy.filters.http.rbac
                        path:
                        - key: istio_dry_run_allow_shadow_effective_policy_id
                    tag: istio.authorization.dry_run.allow_policy.name
                  - metadata:
                      kind:
                        request: {}
                      metadataKey:
                        key: envoy.filters.http.rbac
                        path:
                        - key: istio_dry_run_allow_shadow_engine_result
                    tag: istio.authorization.dry_run.allow_policy.result
                  - metadata:
                      kind:
                        request: {}
                      metadataKey:
                        key: envoy.filters.http.rbac
                        path:
                        - key: istio_dry_run_deny_shadow_effective_policy_id
                    tag: istio.authorization.dry_run.deny_policy.name
                  - metadata:
                      kind:
                        request: {}
                      metadataKey:
                        key: envoy.filters.http.rbac
                        path:
                        - key: istio_dry_run_deny_shadow_engine_result
                    tag: istio.authorization.dry_run.deny_policy.result
                  - literal:
                      value: latest
                    tag: istio.canonical_revision
                  - literal:
                      value: istio-ingressgateway
                    tag: istio.canonical_service
                  - literal:
                      value: mesh1
                    tag: istio.mesh_id
                  - literal:
                      value: istio-system
                    tag: istio.namespace
                  overallSampling:
                    value: 100
                  randomSampling:
                    value: 1
                upgradeConfigs:
                - upgradeType: websocket
                useRemoteAddress: true
```

## 3.2codec_type

### 3.2.1 AUTO

默认是auto，自动匹配

ef-codec_type-AUTO.yaml

kubectl apply -f ef-codec_type-AUTO.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: httpconnectionmanager
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
                codec_type: AUTO
```



### 3.2.2 HTTP1

ef-codec_type-HTTP1.yaml

kubectl apply -f ef-codec_type-HTTP1.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: httpconnectionmanager
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
                codec_type: HTTP1
```



### 3.2.3 HTTP2

ef-codec_type-HTTP2.yaml

kubectl apply -f ef-codec_type-HTTP2.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: httpconnectionmanager
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
                codec_type: HTTP2
```

不能访问，因为我们用的是http1.1协议

## 3.3rds

ef-rds.yaml

kubectl apply -f ef-rds.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: httpconnectionmanager
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
                rds:
                  configSource:
                    ads: {}
                    initialFetchTimeout: 10s
                    resourceApiVersion: V3
                  routeConfigName: http.8080
```

## 3.4route_config

### 3.4.1general

ef-route_config-general.yaml

kubectl apply -f ef-route_config-general.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: httpconnectionmanager
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
                      direct_response:
                        status: 200
                        body: 
                          inline_string: "test"
                  internal_only_headers:
                  - test5
                  response_headers_to_add:
                  - header:
                      key: test1
                      value: test1 
                    append: true
                  response_headers_to_remove:
                  - test3
                  request_headers_to_add:
                  - header:
                      key: test2
                      value: test2 
                    append: true
                  request_headers_to_remove:
                  - test3
                  most_specific_header_mutations_wins: true
                  validate_clusters: true
                  max_direct_response_body_size_bytes: 1024
```





### 3.4.2virtual_hosts

#### 3.4.2.1routes

##### 3.4.2.1.1match

###### 3.4.2.1.1.1prefix

ef-route_config-virtual_hosts-routes-match-prefix.yaml

kubectl apply -f ef-route_config-virtual_hosts-routes-match-prefix.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: httpconnectionmanager
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
                      direct_response:
                        status: 200
                        body: 
                          inline_string: "prefix"
```



###### 3.4.2.1.1.2path

ef-route_config-virtual_hosts-routes-match-path.yaml

kubectl apply -f ef-route_config-virtual_hosts-routes-match-path.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: httpconnectionmanager
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
                        path: /test
                      direct_response:
                        status: 200
                        body: 
                          inline_string: "path"
```



###### 3.4.2.1.1.3safe_regex

ef-route_config-virtual_hosts-routes-match-safe_regex.yaml

kubectl apply -f ef-route_config-virtual_hosts-routes-match-safe_regex.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: httpconnectionmanager
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
                        safe_regex:
                          google_re2: {}
                          regex: ".*regex.*"
                      direct_response:
                        status: 200
                        body: 
                          inline_string: "regex"
```



###### 3.4.2.1.1.4connect_matcher

ef-route_config-virtual_hosts-routes-match-connect_matcher.yaml

kubectl apply -f ef-route_config-virtual_hosts-routes-match-connect_matcher.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: httpconnectionmanager
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
                        connect_matcher: {}
                      direct_response:
                        status: 200
                        body: 
                          inline_string: "regex"
```



###### 3.4.2.1.1.5case_sensitive

ef-route_config-virtual_hosts-routes-match-case_sensitive.yaml

kubectl apply -f ef-route_config-virtual_hosts-routes-match-case_sensitive.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: httpconnectionmanager
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
                        path: /tEst
                        case_sensitive: false
                      direct_response:
                        status: 200
                        body: 
                          inline_string: "case_sensitive"
```



###### 3.4.2.1.1.6runtime_fraction

ef-route_config-virtual_hosts-routes-match-runtime_fraction.yaml

kubectl apply -f ef-route_config-virtual_hosts-routes-match-runtime_fraction.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: httpconnectionmanager
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
                        path: /tEst
                        case_sensitive: false
                        runtime_fraction:
                          default_value:
                            numerator: 10
                            denominator: HUNDRED
                      direct_response:
                        status: 200
                        body: 
                          inline_string: "runtime_fraction"
```



###### 3.4.2.1.1.7headers

```
{
  "name": "...",头名称
  "exact_match": "...",精确匹配
  "safe_regex_match": "{...}",正则匹配
  "range_match": "{...}",范围匹配
  "present_match": "...",存在匹配
  "prefix_match": "...",前缀匹配
  "suffix_match": "...",后缀匹配
  "contains_match": "...",包含匹配
  "string_match": "{...}",字符串匹配
  "invert_match": "..."反向匹配
}
```

1exact_match：

ef-route_config-virtual_hosts-routes-match-headers-exact_match.yaml

kubectl apply -f ef-route_config-virtual_hosts-routes-match-headers-exact_match.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: httpconnectionmanager
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
                        path: /tEst
                        case_sensitive: false
                        headers:
                        - name: ":authority"
                          exact_match: 192.168.229.128:30555
                      direct_response:
                        status: 200
                        body: 
                          inline_string: "headers-exact_match"
```

2safe_regex_match：

ef-route_config-virtual_hosts-routes-match-headers-safe_regex_match.yaml

kubectl apply -f ef-route_config-virtual_hosts-routes-match-headers-safe_regex_match.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: httpconnectionmanager
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
                        path: /tEst
                        case_sensitive: false
                        headers:
                        - name: ":authority"
                          safe_regex_match:
                            google_re2: {}
                            regex: ".*92.168.*"
                      direct_response:
                        status: 200
                        body: 
                          inline_string: "headers-safe_regex_match"
```



3range_match

ef-route_config-virtual_hosts-routes-match-headers-range_match.yaml

kubectl apply -f ef-route_config-virtual_hosts-routes-match-headers-range_match.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: httpconnectionmanager
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
                        path: /tEst
                        case_sensitive: false
                        headers:
                        - name: "test"
                          range_match:
                            start: 1
                            end: 10
                      direct_response:
                        status: 200
                        body: 
                          inline_string: "headers-range_match"
```

curl -H "test: 2" http://192.168.229.128:30555/test



4present_match

ef-route_config-virtual_hosts-routes-match-headers-present_match.yaml

kubectl apply -f ef-route_config-virtual_hosts-routes-match-headers-present_match.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: httpconnectionmanager
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
                        path: /tEst
                        case_sensitive: false
                        headers:
                        - name: "test"
                          present_match: true
                      direct_response:
                        status: 200
                        body: 
                          inline_string: "headers-present_match"
```



5prefix_match

ef-route_config-virtual_hosts-routes-match-headers-prefix_match.yaml

kubectl apply -f ef-route_config-virtual_hosts-routes-match-headers-prefix_match.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: httpconnectionmanager
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
                        path: /tEst
                        case_sensitive: false
                        headers:
                        - name: "test"
                          prefix_match: test
                      direct_response:
                        status: 200
                        body: 
                          inline_string: "headers-prefix_match"
```



6suffix_match

ef-route_config-virtual_hosts-routes-match-headers-suffix_match.yaml

kubectl apply -f ef-route_config-virtual_hosts-routes-match-headers-suffix_match.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: httpconnectionmanager
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
                        path: /tEst
                        case_sensitive: false
                        headers:
                        - name: "test"
                          suffix_match: "111"
                      direct_response:
                        status: 200
                        body: 
                          inline_string: "headers-suffix_match"
```



7contains_match

ef-route_config-virtual_hosts-routes-match-headers-contains_match.yaml

kubectl apply -f ef-route_config-virtual_hosts-routes-match-headers-contains_match.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: httpconnectionmanager
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
                        path: /tEst
                        case_sensitive: false
                        headers:
                        - name: "test"
                          contains_match: test
                      direct_response:
                        status: 200
                        body: 
                          inline_string: "headers-contains_match"
```



8string_match

```
{
  "exact": "...",精确匹配
  "prefix": "...",前缀
  "suffix": "...",后缀
  "safe_regex": "{...}",正则
  "contains": "...",包含
  "ignore_case": "..."忽略大小写
}
```

8.1exact

ef-route_config-virtual_hosts-routes-match-headers-string_match-exact.yaml

kubectl apply -f ef-route_config-virtual_hosts-routes-match-headers-string_match-exact.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: httpconnectionmanager
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
                        path: /tEst
                        case_sensitive: false
                        headers:
                        - name: "test"
                          string_match:
                            exact: "test"
                      direct_response:
                        status: 200
                        body: 
                          inline_string: "headers-string_match-exact"
```



9invert_match

ef-route_config-virtual_hosts-routes-match-headers-invert_match.yaml

kubectl apply -f ef-route_config-virtual_hosts-routes-match-headers-invert_match.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: httpconnectionmanager
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
                        path: /tEst
                        case_sensitive: false
                        headers:
                        - name: "test"
                          contains_match: test
                          invert_match: true
                      direct_response:
                        status: 200
                        body: 
                          inline_string: "headers-invert_match"
```



###### 3.4.2.1.1.8query_parameters

```
{
  "name": "...",参数名称
  "string_match": "{...}",string匹配
  "present_match": "..."存在匹配
}
```

string_match：

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

1string_match

1.1exact

ef-route_config-virtual_hosts-routes-match-query_parameters-string_match-exact.yaml

kubectl apply -f ef-route_config-virtual_hosts-routes-match-query_parameters-string_match-exact.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: httpconnectionmanager
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
                        path: /tEst
                        case_sensitive: false
                        query_parameters:
                        - name: test
                          string_match: 
                            exact: test
                      direct_response:
                        status: 200
                        body: 
                          inline_string: "query_parameters-string_match-exact"
```

访问 [test.test:32688/test?test=test](http://test.test:32688/test?test=test) 



1.2prefix

ef-route_config-virtual_hosts-routes-match-query_parameters-string_match-prefix.yaml

kubectl apply -f ef-route_config-virtual_hosts-routes-match-query_parameters-string_match-prefix.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: httpconnectionmanager
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
                        path: /tEst
                        case_sensitive: false
                        query_parameters:
                        - name: test
                          string_match: 
                            prefix: te
                      direct_response:
                        status: 200
                        body: 
                          inline_string: "query_parameters-string_match-prefix"
```

1.3suffix

ef-route_config-virtual_hosts-routes-match-query_parameters-string_match-suffix.yaml

kubectl apply -f ef-route_config-virtual_hosts-routes-match-query_parameters-string_match-suffix.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: httpconnectionmanager
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
                        path: /tEst
                        case_sensitive: false
                        query_parameters:
                        - name: test
                          string_match: 
                            suffix: "t"
                      direct_response:
                        status: 200
                        body: 
                          inline_string: "query_parameters-string_match-suffix"
```



1.4safe_regex

ef-route_config-virtual_hosts-routes-match-query_parameters-string_match-safe_regex.yaml

kubectl apply -f ef-route_config-virtual_hosts-routes-match-query_parameters-string_match-safe_regex.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: httpconnectionmanager
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
                        path: /tEst
                        case_sensitive: false
                        query_parameters:
                        - name: test
                          string_match: 
                            safe_regex: 
                              google_re2: {}
                              regex: ".*test.*"
                      direct_response:
                        status: 200
                        body: 
                          inline_string: "query_parameters-string_match-safe_regex"
```



1.5contains

ef-route_config-virtual_hosts-routes-match-query_parameters-string_match-contains.yaml

kubectl apply -f ef-route_config-virtual_hosts-routes-match-query_parameters-string_match-contains.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: httpconnectionmanager
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
                        path: /tEst
                        case_sensitive: false
                        query_parameters:
                        - name: test
                          string_match: 
                            contains: est
                      direct_response:
                        status: 200
                        body: 
                          inline_string: "query_parameters-string_match-contains"
```



1.6ignore_case

ef-route_config-virtual_hosts-routes-match-query_parameters-string_match-ignore_case.yaml

kubectl apply -f ef-route_config-virtual_hosts-routes-match-query_parameters-string_match-ignore_case.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: httpconnectionmanager
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
                        path: /tEst
                        case_sensitive: false
                        query_parameters:
                        - name: test
                          string_match: 
                            contains: EST
                            ignore_case: true
                      direct_response:
                        status: 200
                        body: 
                          inline_string: "query_parameters-string_match-ignore_case"
```



2present_match

ef-route_config-virtual_hosts-routes-match-query_parameters-present_match.yaml

kubectl apply -f ef-route_config-virtual_hosts-routes-match-query_parameters-present_match.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: httpconnectionmanager
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
                        path: /tEst
                        case_sensitive: false
                        query_parameters:
                        - name: test
                          present_match: true
                      direct_response:
                        status: 200
                        body: 
                          inline_string: "query_parameters-present_match"
```



###### 3.4.2.1.1.9grpc

ef-route_config-virtual_hosts-routes-match-grpc.yaml

kubectl apply -f ef-route_config-virtual_hosts-routes-match-grpc.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: httpconnectionmanager
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
                        path: /tEst
                        case_sensitive: false
                        grpc: {}
                      direct_response:
                        status: 200
                        body: 
                          inline_string: "match-grpc"
```

访问 http://test.test:32688/test?test=test

因为不是grpc连接所以报错

###### 3.4.2.1.1.10tls_context

ef-route_config-virtual_hosts-routes-match-tls_context.yaml

kubectl apply -f ef-route_config-virtual_hosts-routes-match-tls_context.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: httpconnectionmanager
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
                        path: /tEst
                        case_sensitive: false
                        tls_context:
                          presented: true
                          validated: true
                      direct_response:
                        status: 200
                        body: 
                          inline_string: "tls_context"
```



###### 3.4.2.1.1.11dynamic_metadata

https://github.com/envoyproxy/envoy/issues/22000

ef-route_config-virtual_hosts-routes-match-dynamic_metadata.yaml

kubectl apply -f ef-route_config-virtual_hosts-routes-match-dynamic_metadata.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: httpconnectionmanager
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
                        path: /tEst
                        case_sensitive: false
                        dynamic_metadata:
                          - filter: envoy.filters.http.jwt_authn
                            path:
                            - key: my_payload
                            - key: iss
                            value: 
                              string_match:
                                exact: "testing@secure.istio.io"
                            invert: false
                      direct_response:
                        status: 200
                        body: 
                          inline_string: "dynamic_metadata"
```



##### 3.4.2.1.2route

###### 3.4.2.1.2.1cluster

ef-route_config-virtual_hosts-routes-route-cluster.yaml

kubectl apply -f ef-route_config-virtual_hosts-routes-route-cluster.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: httpconnectionmanager
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
                        case_sensitive: false
                      route:
                        cluster: outbound|9080||productpage.istio.svc.cluster.local
```



###### 3.4.2.1.2.2cluster_header

ef-route_config-virtual_hosts-routes-route-cluster_header.yaml

kubectl apply -f ef-route_config-virtual_hosts-routes-route-cluster_header.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: httpconnectionmanager
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
                        case_sensitive: false
                      route:
                        cluster_header: upstream_cluster
```



![1](12image\1.jpg)



###### 3.4.2.1.2.3weighted_clusters

weighted_clusters:

```
{
  "clusters": [],
  "total_weight": "{...}",
  "runtime_key_prefix": "..."
}
```

clusters:

```
{
  "name": "...",
  "cluster_header": "...",
  "weight": "{...}",
  "metadata_match": "{...}",
  "request_headers_to_add": [],
  "request_headers_to_remove": [],
  "response_headers_to_add": [],
  "response_headers_to_remove": [],
  "typed_per_filter_config": "{...}",
  "host_rewrite_literal": "..."
}
```

1general

ef-route_config-virtual_hosts-routes-route-weighted_clusters-general.yaml

kubectl apply -f ef-route_config-virtual_hosts-routes-route-weighted_clusters-general.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: httpconnectionmanager
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
                        case_sensitive: false
                      route:
                        weighted_clusters:
                          clusters:
                          - name: outbound|9080||productpage.istio.svc.cluster.local
                            weight: 100
                            request_headers_to_add:
                            - header:
                                key: test
                                value: test
                              append: true
                            request_headers_to_remove:
                            - test2
                            response_headers_to_add:
                            - header:
                                key: test3
                                value: test3
                              append: true
                            response_headers_to_remove:
                            - test4
                            host_rewrite_literal: mytest
                          total_weight: 100
                          runtime_key_prefix: test
                       
```

2cluster_header

ef-route_config-virtual_hosts-routes-route-weighted_clusters-cluster_header.yaml

kubectl apply -f ef-route_config-virtual_hosts-routes-route-weighted_clusters-cluster_header.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: httpconnectionmanager
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
                        case_sensitive: false
                      route:
                        weighted_clusters:
                          clusters:
                          - cluster_header: upstream_cluster
                            weight: 100
                          total_weight: 100
                          runtime_key_prefix: test
                       
```



3typed_per_filter_config

ef-route_config-virtual_hosts-routes-route-weighted_clusters-typed_per_filter_config.yaml

kubectl apply -f ef-route_config-virtual_hosts-routes-route-weighted_clusters-typed_per_filter_config.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: httpconnectionmanager
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
                        case_sensitive: false
                      route:
                        weighted_clusters:
                          clusters:
                          - name: outbound|9080||productpage.istio.svc.cluster.local
                            weight: 100
                            typed_per_filter_config:
                              envoy.filters.http.bandwidth_limit:
                                "@type": type.googleapis.com/envoy.extensions.filters.http.bandwidth_limit.v3.BandwidthLimit
                                stat_prefix: bandwidth_limiter_custom_route
                                enable_mode: REQUEST_AND_RESPONSE
                                limit_kbps: 1
                                fill_interval: 1s
                          total_weight: 100
                          runtime_key_prefix: test
                       
```



###### 3.4.2.1.2.4prefix_rewrite

ef-route_config-virtual_hosts-routes-route-prefix_rewrite.yaml

kubectl apply -f ef-route_config-virtual_hosts-routes-route-prefix_rewrite.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: httpconnectionmanager
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
                        path: /mytest/productpage
                      route:
                        prefix_rewrite: /productpage
                        weighted_clusters:
                          clusters:
                          - name: outbound|9080||productpage.istio.svc.cluster.local
                            weight: 100
                          total_weight: 100
                          runtime_key_prefix: test
                    - name: testroute2
                      match: 
                        prefix: /mytest/
                      route:
                        prefix_rewrite: /
                        weighted_clusters:
                          clusters:
                          - name: outbound|9080||productpage.istio.svc.cluster.local
                            weight: 100
                          total_weight: 100
                          runtime_key_prefix: test
                       
```



###### 3.4.2.1.2.5regex_rewrite

ef-route_config-virtual_hosts-routes-route-regex_rewrite.yaml

kubectl apply -f ef-route_config-virtual_hosts-routes-route-regex_rewrite.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: httpconnectionmanager
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
                        prefix: /service
                      route:
                        regex_rewrite:
                          pattern:
                            google_re2: {}
                            regex: "/service/([^/]+)/(.*)$"
                          substitution: /\2\1
                        weighted_clusters:
                          clusters:
                          - name: outbound|9080||productpage.istio.svc.cluster.local
                            weight: 100
                          total_weight: 100
                          runtime_key_prefix: test
                       
```

访问http://test.test:32688/service/page/product

###### 3.4.2.1.2.6host_rewrite_literal

ef-route_config-virtual_hosts-routes-route-host_rewrite_literal.yaml

kubectl apply -f ef-route_config-virtual_hosts-routes-route-host_rewrite_literal.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: httpconnectionmanager
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
                        prefix: /productpage
                      route:
                        host_rewrite_literal: testhost
                        weighted_clusters:
                          clusters:
                          - name: outbound|9080||productpage.istio.svc.cluster.local
                            weight: 100
                          total_weight: 100
                          runtime_key_prefix: test
                       
```



###### 3.4.2.1.2.7auto_host_rewrite

  *strict_dns* or *logical_dns* 类型的cluster才会生效

ef-route_config-virtual_hosts-routes-route-auto_host_rewrite.yaml

kubectl apply -f ef-route_config-virtual_hosts-routes-route-auto_host_rewrite.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: httpconnectionmanager
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
                        prefix: /product
                      route:
                        auto_host_rewrite: true
                        weighted_clusters:
                          clusters:
                          - name: outbound|9080||productpage.istio.svc.cluster.local
                            weight: 100
                          total_weight: 100
                          runtime_key_prefix: test
                       
```



###### 3.4.2.1.2.8host_rewrite_header

ef-route_config-virtual_hosts-routes-route-host_rewrite_header.yaml

kubectl apply -f ef-route_config-virtual_hosts-routes-route-host_rewrite_header.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: httpconnectionmanager
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
                    - "test.test:8080"
                    - "test.test:32688"
                    routes:
                    - name: testroute
                      match: 
                        prefix: /product
                      route:
                        host_rewrite_header: host_header
                        weighted_clusters:
                          clusters:
                          - name: outbound|9080||productpage.istio.svc.cluster.local
                            weight: 100
                          total_weight: 100
                          runtime_key_prefix: test
                       
```



###### 3.4.2.1.2.9host_rewrite_path_regex

 ef-route_config-virtual_hosts-routes-route-host_rewrite_path_regex.yaml 

kubectl apply -f ef-route_config-virtual_hosts-routes-route-host_rewrite_path_regex.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: httpconnectionmanager
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
                        prefix: /product
                      route:
                        host_rewrite_path_regex:
                          pattern:
                            google_re2: {}
                            regex: "^/(.+)$"
                          substitution: \1
                        weighted_clusters:
                          clusters:
                          - name: outbound|9080||productpage.istio.svc.cluster.local
                            weight: 100
                          total_weight: 100
                          runtime_key_prefix: test
                       
```



###### 3.4.2.1.2.10retry_policy

```
{
  "retry_on": "...",
  "num_retries": "{...}",
  "per_try_timeout": "{...}",
  "per_try_idle_timeout": "{...}",
  "retry_priority": "{...}",
  "retry_host_predicate": [],
  "retry_options_predicates": [],
  "host_selection_retry_max_attempts": "...",
  "retriable_status_codes": [],
  "retry_back_off": "{...}",
  "rate_limited_retry_back_off": "{...}",
  "retriable_headers": [],
  "retriable_request_headers": []
}
```

1general

x-envoy-retry-on

 **5xx** , **gateway-error** , **reset** , **connect-failure** , **envoy-ratelimited** , **retriable-4xx** , **refused-stream** , **retriable-status-codes** , **retriable-headers** 

ef-route_config-virtual_hosts-routes-route-retry_policy-general.yaml 

kubectl apply -f ef-route_config-virtual_hosts-routes-route-retry_policy-general.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: httpconnectionmanager
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
                        prefix: /product
                      route:
                        retry_policy:
                          retry_on: 5xx,gateway-error,reset,connect-failure
                          num_retries: 3
                          per_try_timeout: 10s
                          #per_try_idle_timeout: 10s
                          retry_priority:
                            name: envoy.retry_priorities.previous_priorities
                            typed_config:
                              "@type": type.googleapis.com/envoy.extensions.retry.priority.previous_priorities.v3.PreviousPrioritiesConfig
                              update_frequency: 2
                          retry_host_predicate:
                          - name: envoy.retry_host_predicates.previous_hosts  
                          host_selection_retry_max_attempts: 3
                          retriable_status_codes: 
                          - 503 
                          retry_back_off:
                            base_interval: 10ms
                            max_interval: 50ms
                          rate_limited_retry_back_off:
                            reset_headers:
                            - name: Retry-After
                              format: SECONDS
                            - name: X-RateLimit-Reset
                              format: UNIX_TIMESTAMP
                            max_interval: "300s"
                          retriable_headers:
                          - name: test
                            exact_match: test
                          retriable_request_headers:
                          - name: test
                            exact_match: test
                        weighted_clusters:
                          clusters:
                          - name: outbound|9080||productpage.istio.svc.cluster.local
                            weight: 100
                          total_weight: 100
                          runtime_key_prefix: test
                       
```





###### 3.4.2.1.2.11request_mirror_policies

```
{
  "cluster": "...",
  "runtime_fraction": "{...}",
  "trace_sampled": "{...}"
}
```

ef-route_config-virtual_hosts-routes-route-request_mirror_policies.yaml 

kubectl apply -f ef-route_config-virtual_hosts-routes-route-request_mirror_policies.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: httpconnectionmanager
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
                        prefix: /product
                      route:
                        request_mirror_policies:
                        - cluster: outbound|9080||productpage-2.istio.svc.cluster.local
                          runtime_fraction:
                            default_value:
                              numerator: 100
                              denominator: HUNDRED
                          trace_sampled: true
                        weighted_clusters:
                          clusters:
                          - name: outbound|9080||productpage.istio.svc.cluster.local
                            weight: 100
                          total_weight: 100
                          runtime_key_prefix: test
                       
```

deploy-productpage-2.yaml

kubectl apply -f deploy-productpage-2.yaml -n istio

```
apiVersion: v1
kind: Service
metadata:
  name: productpage-2
  labels:
    app: productpage
    service: productpage
spec:
  ports:
  - port: 9080
    name: http
  selector:
    app: productpage-2
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: productpage-v2
  labels:
    app: productpage
    version: v1
spec:
  replicas: 1
  selector:
    matchLabels:
      app: productpage-2
      version: v2
  template:
    metadata:
      labels:
        app: productpage-2
        version: v2
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



###### 3.4.2.1.2.12rate_limits

```
{
  "stage": "{...}",
  "disable_key": "...",
  "actions": [],
  "limit": "{...}"
}
```

actions:

```
{
  "source_cluster": "{...}",
  "destination_cluster": "{...}",
  "request_headers": "{...}",
  "remote_address": "{...}",
  "generic_key": "{...}",
  "header_value_match": "{...}",
  "dynamic_metadata": "{...}",
  "metadata": "{...}",
  "extension": "{...}"
}
```

limit:

```
{
  "dynamic_metadata": "{...}"
}
```

部署ratelimit

1source_cluster

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
      - key: source_cluster
        value: "outbound|80||istio-ingressgateway.istio-system.svc.cluster.local"
        rate_limit:
          unit: minute
          requests_per_unit: 1
      - key: source_cluster
        rate_limit:
          unit: minute
          requests_per_unit: 10
EOF

kubectl apply -f ratelimit-config.yaml -n istio
```



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



3创建envoy-filter

```
cat << EOF > envoyfilter-filter.yaml
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
            endpoints:
            - lb_endpoints:
              - endpoint:
                  address:
                     socket_address:
                      address: ratelimit.istio.svc.cluster.local
                      port_value: 8081
EOF

kubectl apply -f envoyfilter-filter.yaml -n istio-system
```

这个envoyfilter作用在网关上面，配置了一个http过滤器envoy.filters.http.ratelimit，和一个cluster。http 过滤器的cluster地址指向cluster配置的地址，这里就是我们的ratelimit service所在的地址。domain是上面configmap的值一样，failure_mode_deny表示超过请求限值就拒绝，rate_limit_service配置ratelimit服务的地址（cluster），这里可以配置grpc类型的也可以配置http类型的。





ef-route_config-virtual_hosts-routes-route-rate_limits-source_cluster.yaml 

kubectl apply -f ef-route_config-virtual_hosts-routes-route-rate_limits-source_cluster.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: httpconnectionmanager
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
                        prefix: /product
                      route:
                        rate_limits:
                        - stage: 0
                          disable_key: test
                          actions:
                          - source_cluster: {}
                        weighted_clusters:
                          clusters:
                          - name: outbound|9080||productpage.istio.svc.cluster.local
                            weight: 100
                          total_weight: 100
                          runtime_key_prefix: test
                       
```



2destination_cluster

ef-route_config-virtual_hosts-routes-route-rate_limits-destination_cluster.yaml 

kubectl apply -f ef-route_config-virtual_hosts-routes-route-rate_limits-destination_cluster.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: httpconnectionmanager
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
                        prefix: /product
                      route:
                        rate_limits:
                        - stage: 0
                          disable_key: test
                          actions:
                          - destination_cluster: {}
                          limit:
                            dynamic_metadata:
                              metadata_key:
                                key: envoy.xxx
                                path:
                                - key: prop
                                - key: foo
                        weighted_clusters:
                          clusters:
                          - name: outbound|9080||productpage.istio.svc.cluster.local
                            weight: 100
                          total_weight: 100
                          runtime_key_prefix: test
                       
```



3request_headers

ef-route_config-virtual_hosts-routes-route-rate_limits-request_headers.yaml 

kubectl apply -f ef-route_config-virtual_hosts-routes-route-rate_limits-request_headers.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: httpconnectionmanager
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
                        prefix: /product
                      route:
                        rate_limits:
                        - stage: 0
                          disable_key: test
                          actions:
                          - request_headers:
                              header_name: ":path"
                              descriptor_key: "PATH"
                              skip_if_absent: true
                          limit:
                            dynamic_metadata:
                              metadata_key:
                                key: envoy.xxx
                                path:
                                - key: prop
                                - key: foo
                        weighted_clusters:
                          clusters:
                          - name: outbound|9080||productpage.istio.svc.cluster.local
                            weight: 100
                          total_weight: 100
                          runtime_key_prefix: test
                       
```



4remote_address

ef-route_config-virtual_hosts-routes-route-rate_limits-remote_address.yaml 

kubectl apply -f ef-route_config-virtual_hosts-routes-route-rate_limits-remote_address.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: httpconnectionmanager
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
                        prefix: /product
                      route:
                        rate_limits:
                        - stage: 0
                          disable_key: test
                          actions:
                          - remote_address: {}
                          limit:
                            dynamic_metadata:
                              metadata_key:
                                key: envoy.xxx
                                path:
                                - key: prop
                                - key: foo
                        weighted_clusters:
                          clusters:
                          - name: outbound|9080||productpage.istio.svc.cluster.local
                            weight: 100
                          total_weight: 100
                          runtime_key_prefix: test
                       
```



5generic_key

ef-route_config-virtual_hosts-routes-route-rate_limits-generic_key.yaml 

kubectl apply -f ef-route_config-virtual_hosts-routes-route-rate_limits-generic_key.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: httpconnectionmanager
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
                        prefix: /product
                      route:
                        rate_limits:
                        - stage: 0
                          disable_key: test
                          actions:
                          - generic_key: 
                              descriptor_key: test
                              descriptor_value: test
                          limit:
                            dynamic_metadata:
                              metadata_key:
                                key: envoy.xxx
                                path:
                                - key: prop
                                - key: foo
                        weighted_clusters:
                          clusters:
                          - name: outbound|9080||productpage.istio.svc.cluster.local
                            weight: 100
                          total_weight: 100
                          runtime_key_prefix: test
                       
```



6header_value_match

ef-route_config-virtual_hosts-routes-route-rate_limits-header_value_match.yaml 

kubectl apply -f ef-route_config-virtual_hosts-routes-route-rate_limits-header_value_match.yaml -n istio-system

headers:

```
{
  "name": "...",
  "exact_match": "...",
  "safe_regex_match": "{...}",
  "range_match": "{...}",
  "present_match": "...",
  "prefix_match": "...",
  "suffix_match": "...",
  "contains_match": "...",
  "string_match": "{...}",
  "invert_match": "..."
}
```

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: httpconnectionmanager
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
                        prefix: /product
                      route:
                        rate_limits:
                        - stage: 0
                          disable_key: test
                          actions:
                          - header_value_match:
                              descriptor_value: test
                              expect_match: true
                              headers:
                              - name: test
                                exact_match: test
                          limit:
                            dynamic_metadata:
                              metadata_key:
                                key: envoy.xxx
                                path:
                                - key: prop
                                - key: foo
                        weighted_clusters:
                          clusters:
                          - name: outbound|9080||productpage.istio.svc.cluster.local
                            weight: 100
                          total_weight: 100
                          runtime_key_prefix: test
                       
```



7dynamic_metadata

ef-route_config-virtual_hosts-routes-route-rate_limits-dynamic_metadata.yaml 

kubectl apply -f ef-route_config-virtual_hosts-routes-route-rate_limits-dynamic_metadata.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: httpconnectionmanager
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
                        prefix: /product
                      route:
                        rate_limits:
                        - stage: 0
                          disable_key: test
                          actions:
                          - dynamic_metadata: 
                              descriptor_key: test
                              default_value: test
                              metadata_key:
                                key: envoy.xxx
                                path:
                                - key: prop
                                - key: foo
                          limit:
                            dynamic_metadata:
                              metadata_key:
                                key: envoy.xxx
                                path:
                                - key: prop
                                - key: foo
                        weighted_clusters:
                          clusters:
                          - name: outbound|9080||productpage.istio.svc.cluster.local
                            weight: 100
                          total_weight: 100
                          runtime_key_prefix: test
                       
```



8metadata

source:

 **ROUTE_ENTRY** , **DYNAMIC** 

ef-route_config-virtual_hosts-routes-route-rate_limits-metadata.yaml 

kubectl apply -f ef-route_config-virtual_hosts-routes-route-rate_limits-metadata.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: httpconnectionmanager
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
                        prefix: /product
                      route:
                        rate_limits:
                        - stage: 0
                          disable_key: test
                          actions:
                          - metadata: 
                              descriptor_key: test
                              default_value: test
                              metadata_key:
                                key: envoy.xxx
                                path:
                                - key: prop
                                - key: foo
                              source: DYNAMIC
                          limit:
                            dynamic_metadata:
                              metadata_key:
                                key: envoy.xxx
                                path:
                                - key: prop
                                - key: foo
                        weighted_clusters:
                          clusters:
                          - name: outbound|9080||productpage.istio.svc.cluster.local
                            weight: 100
                          total_weight: 100
                          runtime_key_prefix: test
                       
```



9extension

ef-route_config-virtual_hosts-routes-route-rate_limits-extension.yaml 

kubectl apply -f ef-route_config-virtual_hosts-routes-route-rate_limits-extension.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: httpconnectionmanager
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
                        prefix: /product
                      route:
                        rate_limits:
                        - stage: 0
                          disable_key: test
                          actions:
                          - extension: 
                              name: envoy.rate_limit_descriptors.expr
                              typed_config:
                                "@type": type.googleapis.com/envoy.extensions.rate_limit_descriptors.expr.v3.Descriptor
                                descriptor_key: test
                                skip_if_error: true
                                text: connection.requested_server_name
                          limit:
                            dynamic_metadata:
                              metadata_key:
                                key: envoy.xxx
                                path:
                                - key: prop
                                - key: foo
                        weighted_clusters:
                          clusters:
                          - name: outbound|9080||productpage.istio.svc.cluster.local
                            weight: 100
                          total_weight: 100
                          runtime_key_prefix: test
                       
```



###### 3.4.2.1.2.13hash_policy

```
{
  "header": "{...}",
  "cookie": "{...}",
  "connection_properties": "{...}",
  "query_parameter": "{...}",
  "filter_state": "{...}",
  "terminal": "..."
}
```

1header

ef-route_config-virtual_hosts-routes-route-hash_policy-header.yaml 

kubectl apply -f ef-route_config-virtual_hosts-routes-route-hash_policy-header.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: httpconnectionmanager
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
                        prefix: /product
                      route:
                        hash_policy:
                        - header:
                            header_name: test
                            regex_rewrite:
                              pattern:
                                google_re2: {}
                                regex: "/([^/]+)/(.*)$"
                              substitution: /\2\1  
                        weighted_clusters:
                          clusters:
                          - name: outbound|9080||productpage.istio.svc.cluster.local
                            weight: 100
                          total_weight: 100
                          runtime_key_prefix: test
                       
```



2cookie

当cookie的path设置了值不为null的时候，以设置的值为准。
当cookie的path为null时候，获取请求的URI的path值 
    当URI的path值是以“/”结尾的时候，直接设置为cookie的path值
    当URI的path值不是以“/”结尾的时候，查看path里面是否有“/” 
           如果有“/”的话，直接截取到最后一个“/”，然后设置为cookie的path值。
           如果没有“/”的话，将cookie的path设置为”/”。

ef-route_config-virtual_hosts-routes-route-hash_policy-cookie.yaml 

kubectl apply -f ef-route_config-virtual_hosts-routes-route-hash_policy-cookie.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: httpconnectionmanager
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
                        prefix: /product
                      route:
                        hash_policy:
                        - cookie:
                            name: test
                            ttl: 10h
                            path: "/productpage"
                        weighted_clusters:
                          clusters:
                          - name: outbound|9080||productpage.istio.svc.cluster.local
                            weight: 100
                          total_weight: 100
                          runtime_key_prefix: test
                       
```



3connection_properties

ef-route_config-virtual_hosts-routes-route-hash_policy-connection_properties.yaml 

kubectl apply -f ef-route_config-virtual_hosts-routes-route-hash_policy-connection_properties.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: httpconnectionmanager
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
                        prefix: /product
                      route:
                        hash_policy:
                        - connection_properties:
                            source_ip: true
                        weighted_clusters:
                          clusters:
                          - name: outbound|9080||productpage.istio.svc.cluster.local
                            weight: 100
                          total_weight: 100
                          runtime_key_prefix: test
                       
```



4query_parameter

ef-route_config-virtual_hosts-routes-route-hash_policy-query_parameter.yaml 

kubectl apply -f ef-route_config-virtual_hosts-routes-route-hash_policy-query_parameter.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: httpconnectionmanager
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
                        prefix: /product
                      route:
                        hash_policy:
                        - query_parameter:
                            name: test_param
                        weighted_clusters:
                          clusters:
                          - name: outbound|9080||productpage.istio.svc.cluster.local
                            weight: 100
                          total_weight: 100
                          runtime_key_prefix: test
                       
```



5filter_state

ef-route_config-virtual_hosts-routes-route-hash_policy-filter_state.yaml 

kubectl apply -f ef-route_config-virtual_hosts-routes-route-hash_policy-filter_state.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: httpconnectionmanager
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
                        prefix: /product
                      route:
                        hash_policy:
                        - filter_state:
                            key: test
                        weighted_clusters:
                          clusters:
                          - name: outbound|9080||productpage.istio.svc.cluster.local
                            weight: 100
                          total_weight: 100
                          runtime_key_prefix: test
                       
```



6terminal

ef-route_config-virtual_hosts-routes-route-hash_policy-terminal.yaml 

kubectl apply -f ef-route_config-virtual_hosts-routes-route-hash_policy-terminal.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: httpconnectionmanager
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
                        prefix: /product
                      route:
                        hash_policy:
                        - filter_state:
                            key: test
                        - query_parameter:
                            name: test_param
                        - terminal: true
                        weighted_clusters:
                          clusters:
                          - name: outbound|9080||productpage.istio.svc.cluster.local
                            weight: 100
                          total_weight: 100
                          runtime_key_prefix: test
                       
```



###### 3.4.2.1.2.14cors

```
{
  "allow_origin_string_match": [],
  "allow_methods": "...",
  "allow_headers": "...",
  "expose_headers": "...",
  "max_age": "...",
  "allow_credentials": "{...}",
  "filter_enabled": "{...}",
  "shadow_enabled": "{...}"
}
```

ef-route_config-virtual_hosts-routes-route-cors.yaml 

kubectl apply -f ef-route_config-virtual_hosts-routes-route-cors.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: httpconnectionmanager
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
                        prefix: /product
                      route:
                        cors:
                          allow_origin_string_match:
                          - safe_regex:
                              google_re2: {}
                              regex: ".*"
                          allow_methods: POST,GET,OPTION
                          allow_headers: test
                          expose_headers: test2
                          max_age: 1m
                          allow_credentials: true
                          filter_enabled:
                            default_value:
                              numerator: 100
                              denominator: 	HUNDRED
                        weighted_clusters:
                          clusters:
                          - name: outbound|9080||productpage.istio.svc.cluster.local
                            weight: 100
                          total_weight: 100
                          runtime_key_prefix: test
                       
```





###### 3.4.2.1.2.15upgrade_configs

```
{
  "upgrade_type": "...",
  "enabled": "{...}",
  "connect_config": "{...}"
}
```

connect_config：

```
{
  "proxy_protocol_config": "{...}",
  "allow_post": "..."
}
```

proxy_protocol_config：

```
{
  "version": "..."
}
```

version：

- V1

  *(DEFAULT)* ⁣PROXY protocol version 1. Human readable format.

- V2

  ⁣PROXY protocol version 2. Binary format.

https://github.com/hiroakis/tornado-websocket-example

deploy-websocket.yaml

kubectl apply -f deploy-websocket.yaml -n istio

```
apiVersion: v1
kind: Service
metadata:
  name: tornado
  labels:
    app: tornado
    service: tornado
spec:
  ports:
  - port: 8888
    name: http
  selector:
    app: tornado
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: tornado
spec:
  replicas: 1
  selector:
    matchLabels:
      app: tornado
      version: v1
  template:
    metadata:
      labels:
        app: tornado
        version: v1
    spec:
      containers:
      - name: tornado
        image: registry.cn-qingdao.aliyuncs.com/hxpdocker/websocket:1.0
        imagePullPolicy: IfNotPresent
        ports:
        - containerPort: 8888
---
```



ef-route_config-virtual_hosts-routes-route-upgrade_configs.yaml 

kubectl apply -f ef-route_config-virtual_hosts-routes-route-upgrade_configs.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: httpconnectionmanager
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
                        prefix: /ws
                      route:
                        upgrade_configs:
                        - upgrade_type: websocket
                          enabled: true
                        weighted_clusters:
                          clusters:
                          - name: outbound|8888||tornado.istio.svc.cluster.local
                            weight: 100
                          total_weight: 100
                          runtime_key_prefix: test
                    - name: testroute2
                      match: 
                        prefix: /
                      route:
                        cluster: outbound|8888||tornado.istio.svc.cluster.local                   
```





###### 3.4.2.1.2.16internal_redirect_policy

```
{
  "max_internal_redirects": "{...}",
  "redirect_response_codes": [],
  "predicates": [],
  "allow_cross_scheme_redirect": "..."
}
```

redirect_response_codes:

  301, 302, 303, 307 and 308 

 **predicates** :

- [envoy.internal_redirect_predicates.allow_listed_routes](https://www.envoyproxy.io/docs/envoy/latest/api-v3/extensions/internal_redirect/allow_listed_routes/v3/allow_listed_routes_config.proto#extension-envoy-internal-redirect-predicates-allow-listed-routes)
- [envoy.internal_redirect_predicates.previous_routes](https://www.envoyproxy.io/docs/envoy/latest/api-v3/extensions/internal_redirect/previous_routes/v3/previous_routes_config.proto#extension-envoy-internal-redirect-predicates-previous-routes)
- [envoy.internal_redirect_predicates.safe_cross_scheme](https://www.envoyproxy.io/docs/envoy/latest/api-v3/extensions/internal_redirect/safe_cross_scheme/v3/safe_cross_scheme_config.proto#extension-envoy-internal-redirect-predicates-safe-cross-scheme)

ef-route_config-virtual_hosts-routes-route-internal_redirect_policy.yaml 

kubectl apply -f ef-route_config-virtual_hosts-routes-route-internal_redirect_policy.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: httpconnectionmanager
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
                        prefix: /product
                      route:
                        internal_redirect_policy:
                          max_internal_redirects: 10
                          redirect_response_codes:
                          - 301
                          - 302
                          predicates:
                          - name: envoy.internal_redirect_predicates.safe_cross_scheme
                            typed_config:
                              '@type': type.googleapis.com/envoy.extensions.internal_redirect.safe_cross_scheme.v3.SafeCrossSchemeConfig
                          allow_cross_scheme_redirect: false
                        weighted_clusters:
                          clusters:
                          - name: outbound|9080||productpage.istio.svc.cluster.local
                            weight: 100
                          total_weight: 100
                          runtime_key_prefix: test
                       
```





###### 3.4.2.1.2.17internal_redirect_action

 **PASS_THROUGH_INTERNAL_REDIRECT** , **HANDLE_INTERNAL_REDIRECT** 

ef-route_config-virtual_hosts-routes-route-internal_redirect_action.yaml 

kubectl apply -f ef-route_config-virtual_hosts-routes-route-internal_redirect_action.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: httpconnectionmanager
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
                        prefix: /product
                      route:
                        internal_redirect_policy:
                          max_internal_redirects: 10
                          redirect_response_codes:
                          - 301
                          - 302
                          allow_cross_scheme_redirect: true
                        internal_redirect_action: HANDLE_INTERNAL_REDIRECT
                        weighted_clusters:
                          clusters:
                          - name: outbound|9080||productpage.istio.svc.cluster.local
                            weight: 100
                          total_weight: 100
                          runtime_key_prefix: test
                       
```





###### 3.4.2.1.2.18hedge_policy

https://www.envoyproxy.io/docs/envoy/latest/intro/arch_overview/http/http_routing#arch-overview-http-routing-hedging

ef-route_config-virtual_hosts-routes-route-hedge_policy.yaml 

kubectl apply -f ef-route_config-virtual_hosts-routes-route-hedge_policy.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: httpconnectionmanager
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
                        prefix: /product
                      route:
                        hedge_policy:
                          hedge_on_per_try_timeout: true
                        weighted_clusters:
                          clusters:
                          - name: outbound|9080||productpage.istio.svc.cluster.local
                            weight: 100
                          total_weight: 100
                          runtime_key_prefix: test
                       
```



###### 3.4.2.1.2.19genral

priority: HIGH, **DEFAULT** 

ef-route_config-virtual_hosts-routes-route-genral.yaml 

kubectl apply -f ef-route_config-virtual_hosts-routes-route-genral.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: httpconnectionmanager
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
                        prefix: /product
                      route:
                        cluster_not_found_response_code: NOT_FOUND
                        metadata_match:
                          filter_metadata:
                            "envoy.lb": 
                              canary: aaa
                        timeout: 10s
                        idle_timeout: 5s
                        priority: HIGH
                        max_stream_duration: 
                          max_stream_duration: 10s
                          grpc_timeout_header_max: 5s
                          grpc_timeout_header_offset: 3s
                        cluster: outbound|9080||productpage.istio.svc.cluster.local
                       
```



##### 3.4.2.1.3redirect

###### 3.4.2.1.3.1https_redirect

0创建证书

 openssl req -x509 -sha256 -nodes -days 365 -newkey rsa:2048  -keyout cert.key -out cert.crt  -config  openssl.conf

1创建secret

kubectl create -n istio-system secret tls istio-ingressgateway-certs --key ./cert.key --cert=./cert.crt

kubectl exec deploy/istio-ingressgateway -n istio-system  -- ls /etc/istio/ingressgateway-certs

2创建gateway

gateway-https.yaml

```
apiVersion: networking.istio.io/v1beta1
kind: Gateway
metadata:
  name: bookinfo-gateway-https
spec:
  selector:
    istio: ingressgateway
  servers:
  - port:
      number: 443
      name: https
      protocol: HTTPS
    hosts:
    - "*"
    tls:
      mode: SIMPLE
      serverCertificate: /etc/istio/ingressgateway-certs/tls.crt
      privateKey: /etc/istio/ingressgateway-certs/tls.key
```

3创建vs

vs-bookinfo-hosts-star.yaml 

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: bookinfo
spec:
  hosts:
  - "*"
  gateways:
  - bookinfo-gateway-https
  http:
  - match:
    - uri:
        exact: /productpage
    - uri:
        prefix: /static
    - uri:
        exact: /login
    - uri:
        exact: /logout
    - uri:
        prefix: /api/v1/products
    route:
    - destination:
        host: productpage.istio.svc.cluster.local
        port:
          number: 9080
```



ef-route_config-virtual_hosts-routes-redirect-https_redirect.yaml 

kubectl apply -f ef-route_config-virtual_hosts-routes-redirect-https_redirect.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: httpconnectionmanager
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
                        prefix: /product
                      redirect:
                        https_redirect: true
                        port_redirect: 30508
```



###### 3.4.2.1.3.2scheme_redirect

ef-route_config-virtual_hosts-routes-redirect-scheme_redirect.yaml 

kubectl apply -f ef-route_config-virtual_hosts-routes-redirect-scheme_redirect.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: httpconnectionmanager
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
                        prefix: /product
                      redirect:
                        scheme_redirect: https
                        host_redirect: bookinfo.com
                        port_redirect: 30508
```





###### 3.4.2.1.3.3host_redirect

ef-route_config-virtual_hosts-routes-redirect-host_redirect.yaml 

kubectl apply -f ef-route_config-virtual_hosts-routes-redirect-host_redirect.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: httpconnectionmanager
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
                        prefix: /product
                      redirect:
                        host_redirect: 192.168.229.134
                       
```



###### 3.4.2.1.3.4port_redirect

ef-route_config-virtual_hosts-routes-redirect-port_redirect.yaml 

kubectl apply -f ef-route_config-virtual_hosts-routes-redirect-port_redirect.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: httpconnectionmanager
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
                        prefix: /product
                      redirect:
                        host_redirect: 192.168.229.134
                        port_redirect: 32688
```



###### 3.4.2.1.3.5path_redirect

ef-route_config-virtual_hosts-routes-redirect-path_redirect.yaml 

kubectl apply -f ef-route_config-virtual_hosts-routes-redirect-path_redirect.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: httpconnectionmanager
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
                        prefix: /product
                      redirect:
                        scheme_redirect: https
                        host_redirect: bookinfo.com
                        port_redirect: 30508
                        path_redirect: /productpage
```



###### 3.4.2.1.3.6prefix_rewrite

ef-route_config-virtual_hosts-routes-redirect-prefix_rewrite.yaml 

kubectl apply -f ef-route_config-virtual_hosts-routes-redirect-prefix_rewrite.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: httpconnectionmanager
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
                        prefix: /test
                      redirect:
                        scheme_redirect: https
                        host_redirect: bookinfo.com
                        port_redirect: 30508
                        prefix_rewrite: /productpage
```



###### 3.4.2.1.3.7regex_rewrite

ef-route_config-virtual_hosts-routes-redirect-regex_rewrite.yaml 

kubectl apply -f ef-route_config-virtual_hosts-routes-redirect-regex_rewrite.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: httpconnectionmanager
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
                        prefix: /service
                      redirect:
                        scheme_redirect: https
                        host_redirect: bookinfo.com
                        port_redirect: 30508
                        regex_rewrite:
                          pattern:
                            google_re2: {}
                            regex: "/service/([^/]+)/(.*)$"
                          substitution: /\2\1
```



###### 3.4.2.1.3.8response_code

- MOVED_PERMANENTLY

  *(DEFAULT)* ⁣Moved Permanently HTTP Status Code - 301.

- FOUND

  ⁣Found HTTP Status Code - 302.

- SEE_OTHER

  ⁣See Other HTTP Status Code - 303.

- TEMPORARY_REDIRECT

  ⁣Temporary Redirect HTTP Status Code - 307.

- PERMANENT_REDIRECT

  ⁣Permanent Redirect HTTP Status Code - 308.

ef-route_config-virtual_hosts-routes-redirect-response_code.yaml 

kubectl apply -f ef-route_config-virtual_hosts-routes-redirect-response_code.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: httpconnectionmanager
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
                        prefix: /product
                      redirect:
                        scheme_redirect: https
                        host_redirect: bookinfo.com
                        port_redirect: 30508
                        response_code: MOVED_PERMANENTLY
```



###### 3.4.2.1.3.9strip_query

ef-route_config-virtual_hosts-routes-redirect-strip_query.yaml 

kubectl apply -f ef-route_config-virtual_hosts-routes-redirect-strip_query.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: httpconnectionmanager
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
                        prefix: /product
                      redirect:
                        scheme_redirect: https
                        host_redirect: bookinfo.com
                        port_redirect: 30508
                        response_code: MOVED_PERMANENTLY
                        strip_query: true
```



##### 3.4.2.1.4direct_response

```
{
  "status": "...",
  "body": "{...}"
}
```

body:

```
{
  "filename": "...",
  "inline_bytes": "...",
  "inline_string": "..."
}
```



ef-route_config-virtual_hosts-routes-direct_response.yaml

kubectl apply -f ef-route_config-virtual_hosts-routes-direct_response.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: httpconnectionmanager
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
                      direct_response:
                        status: 200
                        body: 
                          inline_string: "direct_response"
```



##### 3.4.2.1.5tracing

```
{
  "client_sampling": "{...}",
  "random_sampling": "{...}",
  "overall_sampling": "{...}",
  "custom_tags": []
}
```

ef-route_config-virtual_hosts-routes-tracing.yaml

kubectl apply -f ef-route_config-virtual_hosts-routes-tracing.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: httpconnectionmanager
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
                      direct_response:
                        status: 200
                        body: 
                          inline_string: "direct_response2"
                      tracing:
                          customTags:
                          - metadata:
                              kind:
                                request: {}
                              metadataKey:
                                key: envoy.filters.http.rbac
                                path:
                                - key: istio_dry_run_allow_shadow_effective_policy_id
                            tag: istio.authorization.dry_run.allow_policy.name
                          - metadata:
                              kind:
                                request: {}
                              metadataKey:
                                key: envoy.filters.http.rbac
                                path:
                                - key: istio_dry_run_allow_shadow_engine_result
                            tag: istio.authorization.dry_run.allow_policy.result
                          - metadata:
                              kind:
                                request: {}
                              metadataKey:
                                key: envoy.filters.http.rbac
                                path:
                                - key: istio_dry_run_deny_shadow_effective_policy_id
                            tag: istio.authorization.dry_run.deny_policy.name
                          - metadata:
                              kind:
                                request: {}
                              metadataKey:
                                key: envoy.filters.http.rbac
                                path:
                                - key: istio_dry_run_deny_shadow_engine_result
                            tag: istio.authorization.dry_run.deny_policy.result
                          - literal:
                              value: latest
                            tag: istio.canonical_revision
                          - literal:
                              value: istio-ingressgateway
                            tag: istio.canonical_service
                          - literal:
                              value: mesh1
                            tag: istio.mesh_id
                          - literal:
                              value: istio-system
                            tag: istio.namespace
                          overallSampling:
                            numerator: 100
                            denominator: HUNDRED
                          randomSampling:
                            numerator: 1
                            denominator: HUNDRED
                          clientSampling:
                            numerator: 100
                            denominator: HUNDRED
```



##### 3.4.2.1.6general

decorator:

```
{
  "operation": "...",
  "propagate": "{...}"
}
```

ef-route_config-virtual_hosts-routes-general.yaml

kubectl apply -f ef-route_config-virtual_hosts-routes-general.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: httpconnectionmanager
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
                      metadata:
                        filter_metadata:
                          "envoy.lb": 
                            canary: true
                      decorator:
                        operation: test
                        propagate: true
                      typed_per_filter_config:
                        envoy.filters.http.bandwidth_limit:
                          "@type": type.googleapis.com/envoy.extensions.filters.http.bandwidth_limit.v3.BandwidthLimit
                          stat_prefix: bandwidth_limiter_custom_route
                          enable_mode: REQUEST_AND_RESPONSE
                          limit_kbps: 1
                          fill_interval: 1s
                      request_headers_to_add:
                      - header:
                          key: test1
                          value: test1
                        append: true
                      request_headers_to_remove:
                      - test2
                      response_headers_to_add:
                      - header:
                          key: test3
                          value: test3
                        append: true
                      response_headers_to_remove:
                      - test3
                      per_request_buffer_limit_bytes: 1024
                      direct_response:
                        status: 200
                        body: 
                          inline_string: "general"
```



#### 3.4.2.2require_tls

##### 3.4.2.2.1 NONE 

ef-route_config-virtual_hosts-require_tls.yaml

kubectl apply -f ef-route_config-virtual_hosts-require_tls.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: httpconnectionmanager
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
                    require_tls: NONE 
                    routes:
                    - name: testroute
                      match: 
                        prefix: /
                      direct_response:
                        status: 200
                        body: 
                          inline_string: "require_tls-NONE"
```



##### 3.4.2.2.2 EXTERNAL_ONLY

ef-route_config-virtual_hosts-require_tls-EXTERNAL_ONLY.yaml

kubectl apply -f ef-route_config-virtual_hosts-require_tls-EXTERNAL_ONLY.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: httpconnectionmanager
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
                    require_tls: EXTERNAL_ONLY 
                    routes:
                    - name: testroute
                      match: 
                        prefix: /
                      direct_response:
                        status: 200
                        body: 
                          inline_string: "require_tls-EXTERNAL_ONLY"
```



##### 3.4.2.2.3 ALL

ef-route_config-virtual_hosts-require_tls-ALL.yaml

kubectl apply -f ef-route_config-virtual_hosts-require_tls-ALL.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: httpconnectionmanager
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
                    require_tls: ALL 
                    routes:
                    - name: testroute
                      match: 
                        prefix: /
                      direct_response:
                        status: 200
                        body: 
                          inline_string: "require_tls-ALL"
```





#### 3.4.2.3virtual_clusters

```
{
  "headers": [],
  "name": "..."
}
```

headers:

```
{
  "name": "...",
  "exact_match": "...",
  "safe_regex_match": "{...}",
  "range_match": "{...}",
  "present_match": "...",
  "prefix_match": "...",
  "suffix_match": "...",
  "contains_match": "...",
  "string_match": "{...}",
  "invert_match": "..."
}
```

ef-route_config-virtual_hosts-virtual_clusters.yaml

kubectl apply -f ef-route_config-virtual_hosts-virtual_clusters.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: httpconnectionmanager
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
                    virtual_clusters:
                    - headers:
                      - name: test
                        exact_match: test
                      name: test
                    routes:
                    - name: testroute
                      match: 
                        prefix: /
                      direct_response:
                        status: 200
                        body: 
                          inline_string: "virtual_clusters"
```



#### 3.4.2.4rate_limits

```
{
  "stage": "{...}",
  "disable_key": "...",
  "actions": [],
  "limit": "{...}"
}
```

actions：

```
{
  "source_cluster": "{...}",
  "destination_cluster": "{...}",
  "request_headers": "{...}",
  "remote_address": "{...}",
  "generic_key": "{...}",
  "header_value_match": "{...}",
  "dynamic_metadata": "{...}",
  "metadata": "{...}",
  "extension": "{...}"
}
```

ef-route_config-virtual_hosts-rate_limits.yaml

kubectl apply -f ef-route_config-virtual_hosts-rate_limits.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: httpconnectionmanager
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
                    rate_limits:
                    - stage: 0
                      disable_key: test
                      actions:
                      - source_cluster: {}
                      limit:
                        dynamic_metadata:
                          metadata_key:
                            key: envoy.xxx
                            path:
                            - key: prop
                            - key: foo
                    routes:
                    - name: testroute
                      match: 
                        prefix: /
                      direct_response:
                        status: 200
                        body: 
                          inline_string: "prefix"
```





#### 3.4.2.5headers

ef-route_config-virtual_hosts-headers.yaml

kubectl apply -f ef-route_config-virtual_hosts-headers.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: httpconnectionmanager
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
                    request_headers_to_add:
                    - header:
                        key: test1
                        value: test1
                      append: true
                    request_headers_to_remove:
                    - test2
                    response_headers_to_add:
                    - header:
                        key: test3
                        value: test3
                      append: true
                    response_headers_to_remove:
                    - test4
                    routes:
                    - name: testroute
                      match: 
                        prefix: /
                      direct_response:
                        status: 200
                        body: 
                          inline_string: "prefix"
```



#### 3.4.2.6cors

```
{
  "allow_origin_string_match": [],
  "allow_methods": "...",
  "allow_headers": "...",
  "expose_headers": "...",
  "max_age": "...",
  "allow_credentials": "{...}",
  "filter_enabled": "{...}",
  "shadow_enabled": "{...}"
}
```

ef-route_config-virtual_hosts-cors.yaml

kubectl apply -f ef-route_config-virtual_hosts-cors.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: httpconnectionmanager
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
                    cors:
                      allow_origin_string_match:
                      - safe_regex:
                          google_re2: {}
                          regex: ".*test.*"
                      allow_methods: POST,GET,OPTION
                      allow_headers: test
                      expose_headers: test2
                      max_age: 1h
                      allow_credentials: true
                      filter_enabled:
                        default_value:
                           numerator: 100
                           denominator: HUNDRED
                    routes:
                    - name: testroute
                      match: 
                        prefix: /
                      direct_response:
                        status: 200
                        body: 
                          inline_string: "prefix"
```





#### 3.4.2.7typed_per_filter_config

ef-route_config-virtual_hosts-typed_per_filter_config.yaml

kubectl apply -f ef-route_config-virtual_hosts-typed_per_filter_config.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: httpconnectionmanager
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
                    typed_per_filter_config:
                      envoy.filters.http.bandwidth_limit:
                        "@type": type.googleapis.com/envoy.extensions.filters.http.bandwidth_limit.v3.BandwidthLimit
                        stat_prefix: bandwidth_limiter_custom_route
                        enable_mode: REQUEST_AND_RESPONSE
                        limit_kbps: 1
                        fill_interval: 1s
                    routes:
                    - name: testroute
                      match: 
                        prefix: /
                      direct_response:
                        status: 200
                        body: 
                          inline_string: "typed_per_filter_config"
```



#### 3.4.2.8retry_policy

```
{
  "retry_on": "...",
  "num_retries": "{...}",
  "per_try_timeout": "{...}",
  "per_try_idle_timeout": "{...}",
  "retry_priority": "{...}",
  "retry_host_predicate": [],
  "retry_options_predicates": [],
  "host_selection_retry_max_attempts": "...",
  "retriable_status_codes": [],
  "retry_back_off": "{...}",
  "rate_limited_retry_back_off": "{...}",
  "retriable_headers": [],
  "retriable_request_headers": []
}
```

ef-route_config-virtual_hosts-retry_policy.yaml

kubectl apply -f ef-route_config-virtual_hosts-retry_policy.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: httpconnectionmanager
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
                    retry_policy:
                      retry_on: 5xx,gateway-error,reset,connect-failure
                      num_retries: 3
                      per_try_timeout: 10s
                      #per_try_idle_timeout: 10s
                      retry_priority:
                        name: envoy.retry_priorities.previous_priorities
                        typed_config:
                          "@type": type.googleapis.com/envoy.extensions.retry.priority.previous_priorities.v3.PreviousPrioritiesConfig
                          update_frequency: 2
                      retry_host_predicate:
                      - name: envoy.retry_host_predicates.previous_hosts  
                      host_selection_retry_max_attempts: 3
                      retriable_status_codes: 
                      - 503 
                      retry_back_off:
                        base_interval: 10ms
                        max_interval: 50ms
                      rate_limited_retry_back_off:
                        reset_headers:
                        - name: Retry-After
                          format: SECONDS
                        - name: X-RateLimit-Reset
                          format: UNIX_TIMESTAMP
                        max_interval: "300s"
                      retriable_headers:
                      - name: test
                        exact_match: test
                      retriable_request_headers:
                      - name: test
                        exact_match: test
                    routes:
                    - name: testroute
                      match: 
                        prefix: /
                      direct_response:
                        status: 200
                        body: 
                          inline_string: "prefix"
```





#### 3.4.2.9general

ef-route_config-virtual_hosts-general.yaml

kubectl apply -f ef-route_config-virtual_hosts-general.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: httpconnectionmanager
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
                    include_request_attempt_count: true
                    include_attempt_count_in_response: true
                    hedge_policy:
                      hedge_on_per_try_timeout: true
                    per_request_buffer_limit_bytes: 1024
                    routes:
                    - name: testroute
                      match: 
                        prefix: /
                      direct_response:
                        status: 200
                        body: 
                          inline_string: "general"
```

## 3.5 scoped_routes 

```
{
  "name": "...",
  "scope_key_builder": "{...}",
  "rds_config_source": "{...}",
  "scoped_route_configurations_list": "{...}",
  "scoped_rds": "{...}"
}
```

### 1scoped_route_configurations_list

header_value_extractor:

```
{
  "name": "...",
  "element_separator": "...",
  "index": "...",
  "element": "{...}"
}
```

rds_config_source:

```
{
  "path": "...",
  "api_config_source": "{...}",
  "ads": "{...}",
  "initial_fetch_timeout": "{...}",
  "resource_api_version": "..."
}
```

scoped_route_configurations:

```
{
  "on_demand": "...",
  "name": "...",
  "route_configuration_name": "...",
  "key": "{...}"
}
```

ef-scoped_routes-scoped_route_configurations_list.yaml

kubectl apply -f ef-scoped_routes-scoped_route_configurations_list.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: httpconnectionmanager
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
                scoped_routes:
                  name: test
                  scope_key_builder:
                    fragments:
                    - header_value_extractor:
                        name: X-Header
                        element_separator: ";"
                        index: 0
                        element:
                          separator: "="
                          key: test
                  scoped_route_configurations_list:
                    scoped_route_configurations:
                    - on_demand: true
                      name: test
                      route_configuration:
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
                                  inline_string: "scoped_route_configurations"
                      key:
                        fragments:
                        - string_key: test
```



```
 curl http://192.168.229.128:30563/productpage -H "X-Header: test=test "
```



### 2scoped_rds

ef-scoped_routes-scoped_rds.yaml

kubectl apply -f ef-scoped_routes-scoped_rds.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: httpconnectionmanager
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
                scoped_routes:
                  name: test
                  scope_key_builder:
                    fragments:
                    - header_value_extractor:
                        name: X-Header
                        element_separator: ";"
                        index: 0
                        element:
                          separator: "="
                          key: test
                  scoped_rds:
                    scoped_rds_config_source:
                      ads: {}
                      initialFetchTimeout: 10s
                      resourceApiVersion: V3
```



## 3.6http_filters

略

## 3.7tracing

```
{
  "client_sampling": "{...}",
  "random_sampling": "{...}",
  "overall_sampling": "{...}",
  "verbose": "...",
  "max_path_tag_length": "{...}",
  "custom_tags": [],
  "provider": "{...}"
}
```

provider:

- [envoy.tracers.datadog](https://www.envoyproxy.io/docs/envoy/latest/api-v3/config/trace/v3/datadog.proto#extension-envoy-tracers-datadog)
- [envoy.tracers.dynamic_ot](https://www.envoyproxy.io/docs/envoy/latest/api-v3/config/trace/v3/dynamic_ot.proto#extension-envoy-tracers-dynamic-ot)
- [envoy.tracers.lightstep](https://www.envoyproxy.io/docs/envoy/latest/api-v3/config/trace/v3/lightstep.proto#extension-envoy-tracers-lightstep)
- [envoy.tracers.opencensus](https://www.envoyproxy.io/docs/envoy/latest/api-v3/config/trace/v3/opencensus.proto#extension-envoy-tracers-opencensus)
- [envoy.tracers.skywalking](https://www.envoyproxy.io/docs/envoy/latest/api-v3/config/trace/v3/skywalking.proto#extension-envoy-tracers-skywalking)
- [envoy.tracers.xray](https://www.envoyproxy.io/docs/envoy/latest/api-v3/config/trace/v3/xray.proto#extension-envoy-tracers-xray)
- [envoy.tracers.zipkin](https://www.envoyproxy.io/docs/envoy/latest/api-v3/config/trace/v3/zipkin.proto#extension-envoy-tracers-zipkin)

ef-tracing.yaml

kubectl apply -f ef-tracing.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: httpconnectionmanager
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
                      direct_response:
                        status: 200
                        body: 
                          inline_string: "tracing"
                tracing:
                  overallSampling:
                    value: 99
                  randomSampling:
                    value: 1
                  clientSampling:
                    value: 99
                  verbose: true
                  max_path_tag_length: 256
                 # provider:
                 #   name: envoy.tracers.zipkin
                    
```



## 3.8common_http_protocol_options

```
{
  "idle_timeout": "{...}",
  "max_connection_duration": "{...}",
  "max_headers_count": "{...}",
  "max_stream_duration": "{...}",
  "headers_with_underscores_action": "...",
  "max_requests_per_connection": "{...}"
}
```

headers_with_underscores_action:

- ALLOW

  *(DEFAULT)* ⁣Allow headers with underscores. This is the default behavior.

- REJECT_REQUEST

  ⁣Reject client request. HTTP/1 requests are rejected with the 400 status. HTTP/2 requests end with the stream reset. The “httpN.requests_rejected_with_underscores_in_headers” counter is incremented for each rejected request.

- DROP_HEADER

  ⁣Drop the header with name containing underscores. The header is dropped before the filter chain is invoked and as such filters will not see dropped headers. The “httpN.dropped_headers_with_underscores” is incremented for each dropped header.

ef-common_http_protocol_options.yaml

kubectl apply -f ef-common_http_protocol_options.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: httpconnectionmanager
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
                      direct_response:
                        status: 200
                        body: 
                          inline_string: "common_http_protocol_options"
                common_http_protocol_options:
                  idle_timeout: 10s
                  max_connection_duration: 10s
                  max_headers_count: 1
                  max_stream_duration: 10s
                  headers_with_underscores_action: ALLOW 
                  #max_requests_per_connection: 1 
```



## 3.9http_protocol_options

```
{
  "allow_absolute_url": "{...}",
  "accept_http_10": "...",
  "default_host_for_http_10": "...",
  "header_key_format": "{...}",
  "enable_trailers": "...",
  "allow_chunked_length": "...",
  "override_stream_error_on_invalid_http_message": "{...}"
}
```

header_key_format:

```
{
  "proper_case_words": "{...}",
  "stateful_formatter": "{...}"
}
```

ef-http_protocol_options.yaml

kubectl apply -f ef-http_protocol_options.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: httpconnectionmanager
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
                      direct_response:
                        status: 200
                        body: 
                          inline_string: "http_protocol_options"
                http_protocol_options:
                  allow_absolute_url: true
                  accept_http_10: true
                  default_host_for_http_10: test
                  header_key_format:
                    proper_case_words: {}
                  enable_trailers: true
                  allow_chunked_length: true
                  override_stream_error_on_invalid_http_message: true
                    
                  
```



## 3.10http2_protocol_options

```
{
  "hpack_table_size": "{...}",
  "max_concurrent_streams": "{...}",
  "initial_stream_window_size": "{...}",
  "initial_connection_window_size": "{...}",
  "allow_connect": "...",
  "max_outbound_frames": "{...}",
  "max_outbound_control_frames": "{...}",
  "max_consecutive_inbound_frames_with_empty_payload": "{...}",
  "max_inbound_priority_frames_per_stream": "{...}",
  "max_inbound_window_update_frames_per_data_frame_sent": "{...}",
  "stream_error_on_invalid_http_messaging": "...",
  "override_stream_error_on_invalid_http_message": "{...}",
  "connection_keepalive": "{...}"
}
```

HPACK（HTTP2 头部压缩算法）

connection_keepalive:

```
{
  "interval": "{...}",
  "timeout": "{...}",
  "interval_jitter": "{...}",
  "connection_idle_interval": "{...}"
}
```

ef-http2_protocol_options.yaml

kubectl apply -f ef-http2_protocol_options.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: httpconnectionmanager
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
                      direct_response:
                        status: 200
                        body: 
                          inline_string: "http2_protocol_options"
                http2_protocol_options:
                  hpack_table_size: 4096
                  max_concurrent_streams: 10
                  initial_stream_window_size: 268435456 
                  initial_connection_window_size: 268435456
                  allow_connect: true
                  max_outbound_frames: 10000
                  max_outbound_control_frames: 1000
                  max_consecutive_inbound_frames_with_empty_payload: 1
                  max_inbound_priority_frames_per_stream: 100
                  max_inbound_window_update_frames_per_data_frame_sent: 10
                  #stream_error_on_invalid_http_messaging: true
                  override_stream_error_on_invalid_http_message: true
                  connection_keepalive:
                    interval: 100ms
                    timeout: 10ms
                    interval_jitter:
                      value: 10
                    connection_idle_interval: 10s
                    
                  
```



## 3.11access_log

略

## 3.12upgrade_configs

```
{
  "upgrade_type": "...",
  "filters": [],
  "enabled": "{...}"
}
```

filters：

```
{
  "name": "...",
  "typed_config": "{...}",
  "config_discovery": "{...}",
  "is_optional": "..."
}
```

deploy-websocket.yaml

kubectl apply -f deploy-websocket.yaml -n istio

```
apiVersion: v1
kind: Service
metadata:
  name: tornado
  labels:
    app: tornado
    service: tornado
spec:
  ports:
  - port: 8888
    name: http
  selector:
    app: tornado
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: tornado
spec:
  replicas: 1
  selector:
    matchLabels:
      app: tornado
      version: v1
  template:
    metadata:
      labels:
        app: tornado
        version: v1
    spec:
      containers:
      - name: tornado
        image: registry.cn-qingdao.aliyuncs.com/hxpdocker/websocket:1.0
        imagePullPolicy: IfNotPresent
        ports:
        - containerPort: 8888
---
```



重复

ef-3.12upgrade_configs.yaml 

kubectl apply -f ef-3.12upgrade_configs.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: httpconnectionmanager
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
                        weighted_clusters:
                          clusters:
                          - name: outbound|8888||tornado.istio.svc.cluster.local
                            weight: 100
                          total_weight: 100
                          runtime_key_prefix: test
                upgrade_configs:
                - upgrade_type: websocket
                  enabled: true
                  #filters:
                        
```



## 3.13general

server_header_transformation:

- OVERWRITE

  *(DEFAULT)* ⁣Overwrite any Server header with the contents of server_name.

- APPEND_IF_ABSENT

  ⁣If no Server header is present, append Server server_name If a Server header is present, pass it through.

- PASS_THROUGH

  ⁣Pass through the value of the server header, and do not append a header if none is present.

original_ip_detection_extensions:

- [envoy.http.original_ip_detection.custom_header](https://www.envoyproxy.io/docs/envoy/latest/api-v3/extensions/http/original_ip_detection/custom_header/v3/custom_header.proto#extension-envoy-http-original-ip-detection-custom-header)
- [envoy.http.original_ip_detection.xff](https://www.envoyproxy.io/docs/envoy/latest/api-v3/extensions/http/original_ip_detection/xff/v3/xff.proto#extension-envoy-http-original-ip-detection-xff)

envoy.http.original_ip_detection.custom_header

```
{
  "header_name": "...",
  "allow_extension_to_set_address_as_trusted": "...",
  "reject_with_status": "{...}"
}
```

forward_client_cert_details:

How to handle the [x-forwarded-client-cert](https://www.envoyproxy.io/docs/envoy/latest/configuration/http/http_conn_man/headers#config-http-conn-man-headers-x-forwarded-client-cert) (XFCC) HTTP header.

- SANITIZE

  *(DEFAULT)* ⁣Do not send the XFCC header to the next hop. This is the default value.

- FORWARD_ONLY

  ⁣When the client connection is mTLS (Mutual TLS), forward the XFCC header in the request.

- APPEND_FORWARD

  ⁣When the client connection is mTLS, append the client certificate information to the request’s XFCC header and forward it.

- SANITIZE_SET

  ⁣When the client connection is mTLS, reset the XFCC header with the client certificate information and send it to the next hop.

- ALWAYS_FORWARD_ONLY

  ⁣Always forward the XFCC header in the request, regardless of whether the client connection is mTLS.

set_current_client_cert_details:

```
{
  "subject": "{...}",
  "cert": "...",
  "chain": "...",
  "dns": "...",
  "uri": "..."
}
```

path_with_escaped_slashes_action:

Determines the action for request that contain %2F, %2f, %5C or %5c sequences in the URI path. This operation occurs before URL normalization and the merge slashes transformations if they were enabled.

- IMPLEMENTATION_SPECIFIC_DEFAULT

  *(DEFAULT)* ⁣Default behavior specific to implementation (i.e. Envoy) of this configuration option. Envoy, by default, takes the KEEP_UNCHANGED action. NOTE: the implementation may change the default behavior at-will.

- KEEP_UNCHANGED

  ⁣Keep escaped slashes.

- REJECT_REQUEST

  ⁣Reject client request with the 400 status. gRPC requests will be rejected with the INTERNAL (13) error code. The “httpN.downstream_rq_failed_path_normalization” counter is incremented for each rejected request.

- UNESCAPE_AND_REDIRECT

  ⁣Unescape %2F and %5C sequences and redirect request to the new path if these sequences were present. Redirect occurs after path normalization and merge slashes transformations if they were configured. NOTE: gRPC requests will be rejected with the INTERNAL (13) error code. This option minimizes possibility of path confusion exploits by forcing request with unescaped slashes to traverse all parties: downstream client, intermediate proxies, Envoy and upstream server. The “httpN.downstream_rq_redirected_with_normalized_path” counter is incremented for each redirected request.

- UNESCAPE_AND_FORWARD

  ⁣Unescape %2F and %5C sequences. Note: this option should not be enabled if intermediaries perform path based access control as it may lead to path confusion vulnerabilities.

request_id_extension:

```
{
  "pack_trace_reason": "{...}",
  "use_request_id_for_trace_sampling": "{...}"
}
```

ef-general.yaml 

kubectl apply -f ef-general.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: httpconnectionmanager
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
                        prefix: /product
                      route:
                        weighted_clusters:
                          clusters:
                          - name: outbound|9080||productpage.istio.svc.cluster.local
                            weight: 100
                          total_weight: 100
                          runtime_key_prefix: test
                add_user_agent: true
                server_name: envoy
                server_header_transformation: OVERWRITE
                scheme_header_transformation:
                  scheme_to_overwrite: http
                max_request_headers_kb: 60
                stream_idle_timeout: 5m
                request_timeout: 1m
                request_headers_timeout: 30s
                drain_timeout: 10s
                delayed_close_timeout: 1000ms
                use_remote_address: true
                xff_num_trusted_hops: 3
                internal_address_config:
                  unix_sockets: true
                skip_xff_append: false
                via: server
                generate_request_id: true
                preserve_external_request_id: true
                always_set_request_id_in_response: true
                forward_client_cert_details: SANITIZE_SET
                set_current_client_cert_details:
                  subject: true
                  cert: true
                  chain: true
                  dns: true
                  uri: true
                proxy_100_continue: true
                normalize_path: true
                merge_slashes: true
                request_id_extension:
                  typed_config:
                    "@type": type.googleapis.com/envoy.extensions.request_id.uuid.v3.UuidRequestIdConfig
                    pack_trace_reason: false
                    use_request_id_for_trace_sampling: true
                path_with_escaped_slashes_action: IMPLEMENTATION_SPECIFIC_DEFAULT
                strip_matching_host_port: true
                #strip_any_host_port: true
                stream_error_on_invalid_http_message: true
                strip_trailing_host_dot: true
                
                        
```



## 3.14local_reply_config

```
{
  "mappers": [],
  "body_format": "{...}"
}
```

mappers:

```
{
  "filter": "{...}",
  "status_code": "{...}",
  "body": "{...}",
  "body_format_override": "{...}",
  "headers_to_add": []
}
```

filter:

```
{
  "status_code_filter": "{...}",
  "duration_filter": "{...}",
  "not_health_check_filter": "{...}",
  "traceable_filter": "{...}",
  "runtime_filter": "{...}",
  "and_filter": "{...}",
  "or_filter": "{...}",
  "header_filter": "{...}",
  "response_flag_filter": "{...}",
  "grpc_status_filter": "{...}",
  "extension_filter": "{...}",
  "metadata_filter": "{...}"
}
```

body_format:

```
{
  "text_format": "...",
  "json_format": "{...}",
  "text_format_source": "{...}",
  "omit_empty_values": "...",
  "content_type": "...",
  "formatters": []
}
```

ef-local_reply_config.yaml 

kubectl apply -f ef-local_reply_config.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: httpconnectionmanager
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
                        prefix: /product
                      route:
                        weighted_clusters:
                          clusters:
                          - name: outbound|9080||productpage.istio.svc.cluster.local
                            weight: 100
                          total_weight: 100
                          runtime_key_prefix: test
                local_reply_config:
                  mappers:
                  - status_code: 200
                    filter:
                      header_filter:
                        header:
                          name: test
                          exact_match: test
                    body: 
                      inline_string: "test"
                    body_format_override:
                      text_format: "%LOCAL_REPLY_BODY%:%RESPONSE_CODE%:path=%REQ(:path)%\n" 
                    headers_to_add:
                    - header:
                        key: test
                        value: test
                      append: true
                  body_format:
                    text_format: "%LOCAL_REPLY_BODY%:%RESPONSE_CODE%:path=%REQ(:path)%\n"      
```









