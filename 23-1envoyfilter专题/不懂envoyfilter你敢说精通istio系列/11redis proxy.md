# 1什么是redis proxy

redis proxy是envoy用来代理redis协议的一个network类型的过滤器，名称是envoy.filters.network.redis_proxy 。他具有元数据感知功能，当redis节点down掉的是否，自动剔除节点。他可以代理多个redis集群，根据prefix路由到不同的redis集群。还有故障注入的功能。读取策略也有很多种。

# 2配置

```
{
  "stat_prefix": "...",stat前缀
  "settings": "{...}",连接池设置
  "latency_in_micros": "...",延迟stats以毫秒为单位
  "prefix_routes": "{...}",前缀路由规则
  "downstream_auth_password": "{...}",密码
  "faults": [],
  "downstream_auth_username": "{...}"用户名
}
```

settings：

```
{
  "op_timeout": "{...}",操作超时时间，单位毫秒
  "enable_hashtagging": "...",key启用hash，相同hash的key将发送到同一个redis
  "enable_redirection": "...",启用重定向
  "max_buffer_size_before_flush": "...",刷新前的缓存大小，建议1024
  "buffer_flush_timeout": "{...}",缓存刷新超时时间，默认3ms
  "max_upstream_unknown_connections": "{...}",最大上游未知连接，默认100
  "enable_command_stats": "...",启用命令stats
  "read_policy": "..."读取策略
}
```

prefix_routes：

```
{
  "routes": [],路由规则
  "case_insensitive": "...",大小写敏感
  "catch_all_route": "{...}"未匹配路由
}
```

routes，catch_all_route：

```
{
  "prefix": "...",前缀
  "remove_prefix": "...",是否删除前缀
  "cluster": "...",上游cluster
  "request_mirror_policy": []镜像策略
}
```

request_mirror_policy：

```
{
  "cluster": "...",上游cluster
  "runtime_fraction": "{...}",百分比
  "exclude_read_commands": "..."是否排除读方法
}
```

faults：

```
{
  "fault_type": "...",错误类型
  "fault_enabled": "{...}",百分比
  "delay": "{...}",延迟
  "commands": []命令
}
```

# 3实战

## 3.1准备工作

1部署redis

envoyfilter/redis/redis-cluster-deploy.yaml

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

redis-mirror-deploy.yaml

kubectl apply -f redis-mirror-deploy.yaml -n istio

```
apiVersion: apps/v1
kind: Deployment
metadata:
  name: redis-mirror
  labels:
    app: redis-mirror
spec:
  selector:
    matchLabels:
      app: redis-mirror
  replicas: 1
  template:
    metadata:
      labels:
        app: redis-mirror
    spec:
      containers:
      - name: redis-mirror
        image: redis
        imagePullPolicy: IfNotPresent
        ports:
        - containerPort: 6379

---
apiVersion: v1
kind: Service
metadata:
  name: redis-mirror
spec:
  selector:
    app: redis-mirror
  ports:
  - name: tcp  
    port: 6379
    protocol: TCP
    targetPort: 6379
```





2设置 envoyfilter cluster

envoyfilter-cluster.yaml

kubectl apply -f envoyfilter-cluster.yaml -n istio-system

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



## 3.2settings

envoyfilter-redis-proxy-settings.yaml

kubectl apply -f envoyfilter-redis-proxy-settings.yaml -n istio-system

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
        name: 10.68.139.110_6379               # Replace REDIS_VIP with the cluster IP of "redis-cluster service
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
            enable_hashtagging: true
            max_upstream_unknown_connections: 100
            max_buffer_size_before_flush: 1024
            buffer_flush_timeout: 3ms
            read_policy: REPLICA
```

替换REDIS_VIP

4构建redis集群

获取pod ip

 kubectl get pods -l app=redis-cluster -o jsonpath='{range.items[*]}{.status.podIP}:6379 ' -n istio

构建集群

kubectl exec -it redis-cluster-0 -n istio  -- redis-cli --cluster create --cluster-replicas 1 172.20.2.112:6379 172.20.2.113:6379 172.20.2.114:6379 172.20.1.20:6379 172.20.2.115:6379 172.20.1.21:6379

验证集群是否成功

kubectl exec -it redis-cluster-0 -c redis -n istio -- redis-cli cluster info 

进入客户端

 kubectl exec -it redis-client-6c4b6c4fb5-7hbv9  -n istio -- /bin/bash

连接集群

 redis-cli -h redis-cluster  -p 6379

设置数据

set  a a

set  b b

set  c c

set  d e

set  e e

验证数据

kubectl exec redis-cluster-0 -c redis -n istio -- redis-cli --scan



## 3.3settings-read_policy

### 3.3.1MASTER

从master节点读取

envoyfilter-redis-proxy-settings-read_policy-MASTER.yaml

kubectl apply -f envoyfilter-redis-proxy-settings-read_policy-MASTER.yaml -n istio-system

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
        name: 10.68.139.110_6379               # Replace REDIS_VIP with the cluster IP of "redis-cluster service
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
            enable_hashtagging: true
            max_upstream_unknown_connections: 100
            max_buffer_size_before_flush: 1024
            buffer_flush_timeout: 3ms
            read_policy: MASTER
```

替换REDIS_VIP



### 3.3.2PREFER_MASTER

从master读取，如果master down掉，从slaver读取

envoyfilter-redis-proxy-settings-read_policy-PREFER_MASTER.yaml

kubectl apply -f envoyfilter-redis-proxy-settings-read_policy-PREFER_MASTER.yaml -n istio-system

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
        name: 10.68.139.110_6379               # Replace REDIS_VIP with the cluster IP of "redis-cluster service
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
            enable_hashtagging: true
            max_upstream_unknown_connections: 100
            max_buffer_size_before_flush: 1024
            buffer_flush_timeout: 3ms
            read_policy: PREFER_MASTER
```

替换REDIS_VIP



### 3.3.3REPLICA

从slaver读取

envoyfilter-redis-proxy-settings-read_policy-REPLICA.yaml

kubectl apply -f envoyfilter-redis-proxy-settings-read_policy-REPLICA.yaml -n istio-system

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
        name: 10.68.139.110_6379               # Replace REDIS_VIP with the cluster IP of "redis-cluster service
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
            enable_hashtagging: true
            max_upstream_unknown_connections: 100
            max_buffer_size_before_flush: 1024
            buffer_flush_timeout: 3ms
            read_policy: REPLICA
```

替换REDIS_VIP

### 3.3.4PREFER_REPLICA 

从slaver读取，如果slaver down掉从master读取

envoyfilter-redis-proxy-settings-read_policy-PREFER_REPLICA.yaml

kubectl apply -f envoyfilter-redis-proxy-settings-read_policy-PREFER_REPLICA.yaml -n istio-system

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
        name: 10.68.139.110_6379               # Replace REDIS_VIP with the cluster IP of "redis-cluster service
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
            enable_hashtagging: true
            max_upstream_unknown_connections: 100
            max_buffer_size_before_flush: 1024
            buffer_flush_timeout: 3ms
            read_policy: PREFER_REPLICA 
```

替换REDIS_VIP

### 3.3.5ANY

任意节点读取

envoyfilter-redis-proxy-settings-read_policy-ANY.yaml

kubectl apply -f envoyfilter-redis-proxy-settings-read_policy-ANY.yaml -n istio-system

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
        name: 10.68.139.110_6379               # Replace REDIS_VIP with the cluster IP of "redis-cluster service
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
            enable_hashtagging: true
            max_upstream_unknown_connections: 100
            max_buffer_size_before_flush: 1024
            buffer_flush_timeout: 3ms
            read_policy: ANY 
```

替换REDIS_VIP



## 3.4prefix_routes

### 3.4.1routes

#### 3.4.1.1prefix

envoyfilter-redis-proxy-prefix_routes-routes-prefix.yaml

kubectl apply -f envoyfilter-redis-proxy-prefix_routes-routes-prefix.yaml -n istio-system

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
        name: 10.68.139.110_6379               # Replace REDIS_VIP with the cluster IP of "redis-cluster service
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
            routes:
            - cluster: custom-redis-cluster
              prefix: xx
          settings:
            op_timeout: 5s
            enable_redirection: true
            enable_command_stats: true
            enable_hashtagging: true
            max_upstream_unknown_connections: 100
            max_buffer_size_before_flush: 1024
            buffer_flush_timeout: 3ms
            read_policy: REPLICA 
```

替换REDIS_VIP

进入客户端

 kubectl exec -it redis-client-6c4b6c4fb5-7hbv9  -n istio -- /bin/bash

连接集群

 redis-cli -h redis-cluster  -p 6379

设置数据

set  xx:a a

set  xx:b b

set  xx:c c

set  xx:d e

set  xx:e e

验证数据

kubectl exec redis-cluster-0 -c redis -n istio -- redis-cli --scan

#### 3.4.1.2remove_prefix

envoyfilter-redis-proxy-prefix_routes-routes-remove_prefix.yaml

kubectl apply -f envoyfilter-redis-proxy-prefix_routes-routes-remove_prefix.yaml -n istio-system

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
        name: 10.68.139.110_6379               # Replace REDIS_VIP with the cluster IP of "redis-cluster service
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
            routes:
            - cluster: custom-redis-cluster
              prefix: xx
              remove_prefix: true
          settings:
            op_timeout: 5s
            enable_redirection: true
            enable_command_stats: true
            enable_hashtagging: true
            max_upstream_unknown_connections: 100
            max_buffer_size_before_flush: 1024
            buffer_flush_timeout: 3ms
            read_policy: REPLICA 
```

替换REDIS_VIP

进入客户端

 kubectl exec -it redis-client-6c4b6c4fb5-7hbv9  -n istio -- /bin/bash

连接集群

 redis-cli -h redis-cluster  -p 6379

设置数据

set  xx:a a

set  xx:b b

set  xx:c c

set  xx:d e

set  xx:e e

验证数据

kubectl exec redis-cluster-0 -c redis -n istio -- redis-cli --scan

#### 3.4.1.3request_mirror_policy

envoyfilter-redis-proxy-with-mirror.yaml

kubectl apply -f envoyfilter-redis-proxy-with-mirror.yaml  -n istio-system 

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
        name: 10.68.139.110_6379             # Replace REDIS_VIP with the cluster IP of "redis-cluster service
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
            routes:
            - cluster: custom-redis-cluster
              prefix: xx
              remove_prefix: true
              request_mirror_policy:
              - cluster: outbound|6379||redis-mirror.istio.svc.cluster.local
                exclude_read_commands: True     # Mirror write commands only:
          settings:
            op_timeout: 5s
            enable_redirection: true
            enable_command_stats: true
            enable_hashtagging: true
            max_upstream_unknown_connections: 100
            max_buffer_size_before_flush: 1024
            buffer_flush_timeout: 3ms
            read_policy: REPLICA 
```

替换REDIS_VIP

进入客户端

 kubectl exec -it redis-client-6c4b6c4fb5-7hbv9  -n istio -- /bin/bash

连接集群

 redis-cli -h redis-cluster  -p 6379

设置数据

set  xx:a a

set  xx:b b

set  xx:c c

set  xx:d e

set  xx:e e

连接mirror，验证数据

kubectl exec redis-mirror-566cbb7cb9-f7mpc -c redis-mirror -n istio -- redis-cli --scan



### 3.4.2case_insensitive

前缀匹配是否大小写不敏感

envoyfilter-redis-proxy-prefix_routes-case_insensitive.yaml

kubectl apply -f envoyfilter-redis-proxy-prefix_routes-case_insensitive.yaml -n istio-system

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
        name: 10.68.139.110_6379               # Replace REDIS_VIP with the cluster IP of "redis-cluster service
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
            case_insensitive: true
            routes:
            - cluster: custom-redis-cluster
              prefix: xx
              remove_prefix: true
              request_mirror_policy:
              - cluster: outbound|6379||redis-mirror.istio.svc.cluster.local
                exclude_read_commands: True     # Mirror write commands only:
          settings:
            op_timeout: 5s
            enable_redirection: true
            enable_command_stats: true
            enable_hashtagging: true
            max_upstream_unknown_connections: 100
            max_buffer_size_before_flush: 1024
            buffer_flush_timeout: 3ms
            read_policy: REPLICA 
```

替换REDIS_VIP

进入客户端

 kubectl exec -it redis-client-6c4b6c4fb5-7hbv9  -n istio -- /bin/bash

连接集群

 redis-cli -h redis-cluster  -p 6379

设置数据

set  xx:a a

set  XX:a b

set  xX:b b

set  xx:c c

set  xX:d e

set  xx:e e

连接mirror，验证数据

kubectl exec redis-mirror-566cbb7cb9-f7mpc -c redis-mirror -n istio -- redis-cli --scan



### 3.4.3catch_all_route

不匹配，默认路由, 如果没有配置route，必须配置

envoyfilter-redis-proxy-catch_all_route.yaml

kubectl apply -f envoyfilter-redis-proxy-catch_all_route.yaml -n istio-system

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
        name: 10.68.139.110_6379               # Replace REDIS_VIP with the cluster IP of "redis-cluster service
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
            enable_hashtagging: true
            max_upstream_unknown_connections: 100
            max_buffer_size_before_flush: 1024
            buffer_flush_timeout: 3ms
            read_policy: REPLICA
```

替换REDIS_VIP



## 3.5faults

### 3.5.1ERROR

envoyfilter-redis-proxy-faults-ERROR.yaml

kubectl apply -f envoyfilter-redis-proxy-faults-ERROR.yaml -n istio-system

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
        name: 10.68.139.110_6379               # Replace REDIS_VIP with the cluster IP of "redis-cluster service
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
          faults:
          - fault_type: ERROR
            fault_enabled:
              default_value:
                numerator: 100
                denominator: HUNDRED
            commands:
            - SET
          settings:
            op_timeout: 5s
            enable_redirection: true
            enable_command_stats: true
            enable_hashtagging: true
            max_upstream_unknown_connections: 100
            max_buffer_size_before_flush: 1024
            buffer_flush_timeout: 3ms
            read_policy: REPLICA
```

替换REDIS_VIP

进入客户端

 kubectl exec -it redis-client-6c4b6c4fb5-7hbv9  -n istio -- /bin/bash

连接集群

 redis-cli -h redis-cluster  -p 6379

设置数据

set  a a

set  b b



### 3.5.2DELAY

envoyfilter-redis-proxy-faults-DELAY.yaml

kubectl apply -f envoyfilter-redis-proxy-faults-DELAY.yaml -n istio-system

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
        name: 10.68.139.110_6379                  # Replace REDIS_VIP with the cluster IP of "redis-cluster service
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
          faults:
          - fault_type: DELAY
            delay: 10s
            fault_enabled:
              default_value:
                numerator: 100
                denominator: HUNDRED
            commands:
            - SET
          settings:
            op_timeout: 5s
            enable_redirection: true
            enable_command_stats: true
            enable_hashtagging: true
            max_upstream_unknown_connections: 100
            max_buffer_size_before_flush: 1024
            buffer_flush_timeout: 3ms
            read_policy: REPLICA
```

替换REDIS_VIP

进入客户端

 kubectl exec -it redis-client-6c4b6c4fb5-7hbv9  -n istio -- /bin/bash

连接集群

 redis-cli -h redis-cluster  -p 6379

设置数据

set  a a

set  b b



## 3.6清理

kubectl delete -f redis-cluster-deploy.yaml -n istio

kubectl delete -f redis-client-deploy.yaml -n istio

kubectl delete -f redis-mirror-deploy.yaml -n istio

kubectl delete envoyfilter custom-redis-cluster -n istio-system

kubectl delete envoyfilter add-redis-proxy -n istio-system

