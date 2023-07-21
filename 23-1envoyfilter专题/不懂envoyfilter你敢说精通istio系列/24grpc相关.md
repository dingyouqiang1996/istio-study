# 1什么是grpc

[gRpc](http://www.grpc.io/) 是来自 Google 的 RPC 框架。它使用协议缓冲区作为底层 序列化 /IDL(接口描述语言的缩写) 格式。在传输层，它使用 HTTP/2 进行请求/响应复用。Envoy 在传输层和应用层都提供对 gRPC 的一流支持：

- gRPC 使用 HTTP/2 trailers 特性（可以在 HTTP 请求和响应报文后追加 HTTP Header)来传送请求状态。Envoy 是能够正确支持 HTTP/2 trailers 的少数几个 HTTP 代理之一，因此也是可以传输 gRPC 请求和响应的代理之一。
- 某些语言的 gRPC 运行时相对不成熟。Envoy 支持 gRPC [桥接过滤器](https://www.servicemesher.com/envoy/configuration/http_filters/grpc_http1_bridge_filter.html#config-http-filters-grpc-bridge)，允许 gRPC 请求通过 HTTP/1.1 发送给 Envoy。然后，Envoy 将请求转换为 HTTP/2 以传输到目标服务器。该响应被转换回 HTTP/1.1。
- 安装后，除了标准的全局 HTTP 统计数据之外，桥接过滤器还会根据每个 RPC 统计数据进行收集。
- gRPC-Web 由一个指定的[过滤器](https://www.servicemesher.com/envoy/configuration/http_filters/grpc_web_filter.html#config-http-filters-grpc-web)支持，该过滤器允许 gRPC-Web 客户端通过 HTTP/1.1 向 Envoy 发送请求并代理到 gRPC 服务器。
- gRPC-JSON 转码器由一个指定的[过滤器](https://www.servicemesher.com/envoy/configuration/http_filters/grpc_json_transcoder_filter.html#config-http-filters-grpc-json-transcoder)支持，该过滤器允许 RESTful JSON API 客户端通过 HTTP 向 Envoy 发送请求并获取代理到 gRPC 服务。

# 2grpc相关过滤器有哪些

**gRPC** HTTP/1.1 Bridge

允许grpc请求通过http/1/1方式发送到envoy，然后envoy转换成http/2或http/3传输到目标服务器。响应会转换回http/1.1

**gRPC** HTTP/1.1 Reverse Bridge

允许grpc请求发送到envoy，然后转换成http/1.1，到上游。响应转换回grpc到下游。上游可以不感知grpc的存在。

**gRPC** statistics

这个过滤器启用grpc调用遥测。他会计算成功和失败的调用，把他们通过方法名称分组。

**gRPC** Web

grpc-web是一个js客户端库，他支持相同的api如grpc-node方式访问grpc服务。

这个过滤器启用grpc-web客户端和grpc服务器桥接。

**gRPC**-JSON transcoder

这个过滤器允许restful json api客户端请求envoy用http的方式，代理到grpc服务端。



# 3实战

## 3.1gRPC HTTP/1.1 Bridge

例子来自：https://github.com/envoyproxy/envoy/tree/main/examples/grpc-bridge

bridge-deploy.yaml

kubectl apply -f bridge-deploy.yaml -n istio

```
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: grpc-server
  labels:
    app: grpc-server
spec:
  selector:
    matchLabels:
      app: grpc-server
  replicas: 1
  template:
    metadata:
      labels:
        app: grpc-server
        version: v1
    spec:
      containers:
        - name: csrf
          image: registry.cn-beijing.aliyuncs.com/hxpdocker/grpc-bridge-server:latest
          imagePullPolicy: Always
          ports:
            - containerPort: 8081
---
apiVersion: v1
kind: Service
metadata:
  name: grpc-server
spec:
  selector:
    app: grpc-server
  ports:
    - name: grpc-server
      protocol: TCP
      port: 8081
      targetPort: 8081
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: grpc-client
  labels:
    app: grpc-client
spec:
  selector:
    matchLabels:
      app: grpc-client
  replicas: 1
  template:
    metadata:
      labels:
        app: grpc-client
        version: v1
    spec:
      containers:
        - name: csrf
          image: registry.cn-beijing.aliyuncs.com/hxpdocker/grpc-bridge-client:latest
          imagePullPolicy: Always
          env:
          - name: CLIENT_PROXY
            value: http://grpc-server:8081

```



ef-bridge-client.yaml

kubectl apply -f ef-bridge-client.yaml -n istio

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: grpc-client
spec:
  workloadSelector:
    labels:
      app: grpc-client
  configPatches:
  - applyTo: HTTP_FILTER
    match:
        context: SIDECAR_OUTBOUND
        listener:
          name: 0.0.0.0_8081
          filterChain:
            filter:
              name: envoy.filters.network.http_connection_manager
              subFilter:
                name: envoy.filters.http.router
    patch:
        operation: INSERT_BEFORE
        value:           
          name: envoy.filters.http.grpc_http1_bridge
          typed_config:
            "@type": type.googleapis.com/envoy.extensions.filters.http.grpc_http1_bridge.v3.Config
```

测试：

```
 kubectl exec -it -n istio grpc-client-5f86876755-564t6 -- /bin/bash
 /client/grpc-kv-client.py set foo bar2
 
 [root@node01 ~]# kubectl logs -f -n istio grpc-server-59ff9799c4-7pcqn 
2021/10/15 03:14:36 starting grpc on :8081
2021/10/15 03:33:10 set: foo = bar
2021/10/15 03:33:20 get: foo
2021/10/15 03:33:40 set: foo = bar
2021/10/15 03:33:44 get: foo
2021/10/15 03:33:50 set: foo = bar2
2021/10/15 03:33:53 get: foo
2021/10/15 03:34:37 get: foo
2021/10/15 03:41:41 set: foo = bar2
2021/10/15 03:42:01 set: foo = bar2
```



## 3.2gRPC HTTP/1.1 Reverse Bridge

例子来自：https://github.com/sp-manuel-jurado/istio-grpc-http1-reverse-bridge

reverse-bridge-deploy.yaml

kubectl apply -f reverse-bridge-deploy.yaml -n istio

```
---
apiVersion: v1
kind: Service
metadata:
  name: ping-http
  labels:
    app: ping-http
spec:
  ports:
  - port: 8888
    name: http
  selector:
    app: ping-http
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: ping-http
spec:
  replicas: 1
  selector:
    matchLabels:
      app: ping-http
  template:
    metadata:
      labels:
        app: ping-http
    spec:
      containers:
      - name: ping-http
        image: registry.cn-beijing.aliyuncs.com/hxpdocker/ping-http:latest
        resources:
          requests:
            cpu: "100m"
            memory: "128Mi"
        imagePullPolicy: Always
        ports:
        - containerPort: 8888
---
apiVersion: v1
kind: Service
metadata:
  name: ping-grpc
  labels:
    app: ping-grpc
spec:
  ports:
  - port: 10005
    name: grpc
  selector:
    app: ping-grpc
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: ping-grpc
spec:
  replicas: 1
  selector:
    matchLabels:
      app: ping-grpc
  template:
    metadata:
      labels:
        app: ping-grpc
    spec:
      containers:
      - name: ping-grpc
        image: registry.cn-beijing.aliyuncs.com/hxpdocker/ping-grpc:latest
        resources:
          requests:
            cpu: "100m"
            memory: "128Mi"
        imagePullPolicy: Always
        ports:
        - containerPort: 10005
```

ef-reverse-bridge.yaml

kubectl  apply -f ef-reverse-bridge.yaml -n istio

```
---
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: lab-test
spec:
  workloadSelector:
    labels:
      app: ping-http
  configPatches:
    - applyTo: HTTP_FILTER
      match:
        context: SIDECAR_INBOUND
        listener:
          portNumber: 8888 
          filterChain:
            filter:
              name: envoy.filters.network.http_connection_manager
              subFilter:
                name: envoy.filters.http.router
      patch:
        operation: INSERT_BEFORE
        value:
          name: envoy.filters.http.grpc_http1_reverse_bridge
          typed_config:
            "@type": type.googleapis.com/envoy.extensions.filters.http.grpc_http1_reverse_bridge.v3.FilterConfig
            content_type: application/grpc+proto
            withhold_grpc_frames: true
```

测试

```
 grpcurl -v -proto ./proto/SP/Rpc/ping_service.proto -plaintext -import-path ./proto -d '{}'  10.68.10.2:10005 sp.rpc.PingService/Ping
 
 Resolved method descriptor:
rpc Ping ( .google.protobuf.Empty ) returns ( .google.protobuf.Empty );

Request metadata to send:
(empty)

Response headers received:
content-type: application/grpc
date: Fri, 15 Oct 2021 05:16:59 GMT
server: istio-envoy
x-envoy-decorator-operation: ping-grpc.istio.svc.cluster.local:10005/*
x-envoy-upstream-service-time: 0

Response contents:
{

}

Response trailers received:
(empty)
Sent 1 request and received 1 response


[root@node01 istio-grpc-http1-reverse-bridge]# grpcurl -v -proto ./proto/SP/Rpc/ping_service.proto -plaintext -import-path ./proto -d '{}'  10.68.117.193:8888 sp.rpc.PingService/Ping

Resolved method descriptor:
rpc Ping ( .google.protobuf.Empty ) returns ( .google.protobuf.Empty );

Request metadata to send:
(empty)

Response headers received:
content-type: application/grpc
date: Fri, 15 Oct 2021 05:23:09 GMT
server: istio-envoy
x-envoy-decorator-operation: ping-http.istio.svc.cluster.local:8888/*
x-envoy-upstream-service-time: 0

Response contents:
{

}

Response trailers received:
(empty)
Sent 1 request and received 1 response
```



## 3.3gRPC statistics

例子来自：https://github.com/envoyproxy/envoy/tree/main/examples/grpc-bridge

bridge-deploy.yaml

kubectl apply -f bridge-deploy.yaml -n istio

```
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: grpc-server
  labels:
    app: grpc-server
spec:
  selector:
    matchLabels:
      app: grpc-server
  replicas: 1
  template:
    metadata:
      labels:
        app: grpc-server
        version: v1
      annotations:
        sidecar.istio.io/statsInclusionRegexps: ".*"
    spec:
      containers:
        - name: csrf
          image: registry.cn-beijing.aliyuncs.com/hxpdocker/grpc-bridge-server:latest
          imagePullPolicy: Always
          ports:
            - containerPort: 8081
---
apiVersion: v1
kind: Service
metadata:
  name: grpc-server
spec:
  selector:
    app: grpc-server
  ports:
    - name: grpc-server
      protocol: TCP
      port: 8081
      targetPort: 8081
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: grpc-client
  labels:
    app: grpc-client
spec:
  selector:
    matchLabels:
      app: grpc-client
  replicas: 1
  template:
    metadata:
      labels:
        app: grpc-client
        version: v1
      annotations:
        sidecar.istio.io/statsInclusionRegexps: ".*"
    spec:
      containers:
        - name: csrf
          image: registry.cn-beijing.aliyuncs.com/hxpdocker/grpc-bridge-client:latest
          imagePullPolicy: Always
          env:
          - name: CLIENT_PROXY
            value: http://grpc-server:8081

```



ef-statistics-client.yaml

kubectl apply -f ef-statistics-client.yaml -n istio

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: grpc-client
spec:
  workloadSelector:
    labels:
      app: grpc-client
  configPatches:
  - applyTo: HTTP_FILTER
    match:
        context: SIDECAR_OUTBOUND
        listener:
          name: 0.0.0.0_8081
          filterChain:
            filter:
              name: envoy.filters.network.http_connection_manager
              subFilter:
                name: envoy.filters.http.router
    patch:
        operation: INSERT_BEFORE
        value:           
          name: envoy.filters.http.grpc_http1_bridge
          typed_config:
            "@type": type.googleapis.com/envoy.extensions.filters.http.grpc_http1_bridge.v3.Config
  - applyTo: HTTP_FILTER
    match:
        context: SIDECAR_OUTBOUND
        listener:
          name: 0.0.0.0_8081
          filterChain:
            filter:
              name: envoy.filters.network.http_connection_manager
              subFilter:
                name: envoy.filters.http.router
    patch:
        operation: INSERT_BEFORE
        value:           
          name: envoy.filters.http.grpc_stats
          typed_config:
            "@type": type.googleapis.com/envoy.extensions.filters.http.grpc_stats.v3.FilterConfig
            emit_filter_state: true
            stats_for_all_methods: true
            enable_upstream_stats: true
```

```
 kubectl exec -it -n istio grpc-client-5f86876755-564t6 -- /bin/bash
 /client/grpc-kv-client.py set foo bar2
 
 [root@node01 ~]# kubectl logs -f -n istio grpc-server-59ff9799c4-7pcqn 
2021/10/15 03:14:36 starting grpc on :8081
2021/10/15 03:33:10 set: foo = bar
2021/10/15 03:33:20 get: foo
2021/10/15 03:33:40 set: foo = bar
2021/10/15 03:33:44 get: foo
2021/10/15 03:33:50 set: foo = bar2
2021/10/15 03:33:53 get: foo
2021/10/15 03:34:37 get: foo
2021/10/15 03:41:41 set: foo = bar2
2021/10/15 03:42:01 set: foo = bar2
```



## 3.4grpc web

https://github.com/SafetyCulture/grpc-web-devtools

web-grpc-deploy.yaml

kubectl apply -f web-grpc-deploy.yaml -n istio

```
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: web-grpc
  labels:
    app: web-grpc
spec:
  selector:
    matchLabels:
      app: web-grpc
  replicas: 1
  template:
    metadata:
      labels:
        app: web-grpc
        version: v1
    spec:
      containers:
        - name: csrf
          image: registry.cn-hangzhou.aliyuncs.com/hxpdocker/server:latest
          imagePullPolicy: Always
          ports:
            - containerPort: 50051
---
apiVersion: v1
kind: Service
metadata:
  name: web-grpc
spec:
  selector:
    app: web-grpc
  ports:
    - name: grpc-web-grpc
      protocol: TCP
      port: 50051
      targetPort: 50051
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: web-grpc-client
  labels:
    app: web-grpc-client
spec:
  selector:
    matchLabels:
      app: web-grpc-client
  replicas: 1
  template:
    metadata:
      labels:
        app: web-grpc-client
        version: v1
    spec:
      containers:
        - name: csrf
          image: registry.cn-hangzhou.aliyuncs.com/hxpdocker/client:latest
          imagePullPolicy: Always
          ports:
            - containerPort: 8080
---
apiVersion: v1
kind: Service
metadata:
  name: web-grpc-client
spec:
  selector:
    app: web-grpc-client
  ports:
    - name: http-web-grpc-client
      protocol: TCP
      port: 8080
      targetPort: 8080
```

注意svc的port的名字grpc-web-grpc，表名用的是grpc协议

vs

vs-web-grpc.yaml

kubectl apply -f vs-web-grpc.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: web-grpc
spec:
  hosts:
  - "*"
  gateways:
  - bookinfo-gateway
  http:
  - match:
    - uri:
        prefix: /s12.example
    corsPolicy:
      allowOrigins:
      - exact: http://192.168.229.128:30563
      - exact: http://192.168.198.154:8081
      allowMethods:
      - GET
      - OPTIONS
      - POST
      - PUT
      - DELETE
      maxAge: "1m"
      allowHeaders:
      - keep-alive
      - user-agent
      - cache-control
      - content-type
      - content-transfer-encoding
      - custom-header-1
      - x-accept-content-transfer-encoding
      - x-accept-response-streaming
      - x-user-agent
      - x-grpc-web
      - grpc-timeout
      exposeHeaders: 
      - custom-header-1
      - grpc-status
      - grpc-message
    route:
    - destination:
        host: web-grpc.istio.svc.cluster.local
        port:
          number: 50051
```

vs-web-grpc-client.yaml

kubectl apply -f vs-web-grpc-client.yaml -n istio

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
        host: web-grpc-client.istio.svc.cluster.local
        port:
          number: 8080
```

ef-web-grpc.yaml

kubectl apply -f ef-web-grpc.yaml -n istio

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: grpc
spec:
  workloadSelector:
    labels:
      app: web-grpc
  configPatches:
  - applyTo: HTTP_FILTER
    match:
        listener:
          filterChain:
            destinationPort: 50051
            transportProtocol: "tls"
            filter:
              name: envoy.filters.network.http_connection_manager
              subFilter:
                name: istio.metadata_exchange
    patch:
        operation: INSERT_BEFORE
        value:           
          name: envoy.filters.http.grpc_web
          typed_config:
            "@type": type.googleapis.com/envoy.extensions.filters.http.grpc_web.v3.GrpcWeb
```

访问：

http://192.168.229.128:30563/



## 3.5gRPC-JSON transcoder

wind.proto

```
syntax = "proto3";

import "google/api/annotations.proto";

package wind_power;

service WindServer {
    rpc wind_predict(Request) returns (Response) {
        option (google.api.http) = {
          get: "/predict"
        };
    }
    
    rpc send_data(Request) returns (Response) {
        option (google.api.http) = {
          post: "/send",
          body: "*"
        };
    }
}

message Request {
    string content = 1;
}

message Response {
    string msg = 1;
    int32 code = 2;
}
```

生成pb

```
python -m grpc_tools.protoc -I../../googleapis-master -I. \
--include_imports --include_source_info --descriptor_set_out=proto.pb \
--python_out=.. --grpc_python_out=.. wind.proto
```

 **需要先将googleapis这个[googleapis项目](https://links.jianshu.com/go?to=https%3A%2F%2Fgithub.com%2Fgoogleapis%2Fgoogleapis)下载都指定的路径下，并将上述命令中的第一个-I替换成googleapis所在的路径。** 



服务端代码：

```
import grpc
import logging
from concurrent import futures

import proto.wind_pb2 as wind_pb2
import proto.wind_pb2_grpc as wind_pb2_grpc

class WindPredictSrv(wind_pb2_grpc.WindServerServicer):

    def wind_predict(self, request, context):
        print("call wind_predict")
        return wind_pb2.Response(msg='%s!' % request.content)
        
    def send_data(self, request, context):
        print("call send_data")
        return wind_pb2.Response(msg='%s!' % request.content)

def server():
    grpc_server = grpc.server(futures.ThreadPoolExecutor(max_workers=10))
    wind_pb2_grpc.add_WindServerServicer_to_server(WindPredictSrv(), grpc_server)
    grpc_server.add_insecure_port('[::]:50052')
    grpc_server.start()
    grpc_server.wait_for_termination()

if __name__ == '__main__':
    logging.basicConfig()
    server()
```



客户端代码：

```
import grpc
import logging
import time
import sys

import proto.wind_pb2 as wind_pb2
import proto.wind_pb2_grpc as wind_pb2_grpc

def run():
    option = [('grpc.keepalive_timeout_ms', 10000)]
    while True:
        with grpc.insecure_channel(target='wind-server:50052', options=option) as channel:
            stub = wind_pb2_grpc.WindServerStub(channel)
            request = wind_pb2.Request(content='hello grpc')
            response = stub.wind_predict(request, timeout=10)
        print("Greeter client received: " + response.msg)
        sys.stdout.flush()
        time.sleep(2)
if __name__ == '__main__':
    logging.basicConfig()
    run()
```



部署文件

grpc-json-deploy.yaml

kubectl apply -f grpc-json-deploy.yaml -n istio

```
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: wind-server
  labels:
    app: wind-server
spec:
  selector:
    matchLabels:
      app: wind-server
  replicas: 1
  template:
    metadata:
      labels:
        app: wind-server
        version: v1
    spec:
      containers:
        - name: csrf
          image: registry.cn-beijing.aliyuncs.com/hxpdocker/wind-server:latest
          imagePullPolicy: Always
          ports:
            - containerPort: 50052
---
apiVersion: v1
kind: Service
metadata:
  name: wind-server
spec:
  selector:
    app: wind-server
  ports:
    - name: grpc-wind-server
      protocol: TCP
      port: 50052
      targetPort: 50052
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: wind-client
  labels:
    app: wind-client
spec:
  selector:
    matchLabels:
      app: wind-client
  replicas: 1
  template:
    metadata:
      labels:
        app: wind-client
        version: v1
    annotation:
      sidecar.istio.io/userVolume: '[{"name":"proto-file","hostPath":{"path":"/var/lib/"}}]’ 
      sidecar.istio.io/userVolumeMount: '[{"mountPath":"/etc/envoy/","name":"proto-file"}]'
    spec:
      containers:
        - name: csrf
          image: registry.cn-beijing.aliyuncs.com/hxpdocker/wind-client:latest
          imagePullPolicy: Always

```

**client 使用了hostPath，这个目录放pb文件**



grpc-json-client.yaml

kubectl apply -f grpc-json-client.yaml -n istio

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: grpc
spec:
  workloadSelector:
    labels:
      app: wind-client
  configPatches:
  - applyTo: NETWORK_FILTER
    match:
        context: SIDECAR_OUTBOUND
        listener:
          name: 0.0.0.0_50052
          filterChain:
            filter:
              name: envoy.filters.network.http_connection_manager
    patch:
        operation: MERGE
        value:   
          name: envoy.filters.network.http_connection_manager
          typedConfig:
              '@type': type.googleapis.com/envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager
              stat_prefix: grpc_json
              codec_type: AUTO
              route_config:
                name: local_route
                virtual_hosts:
                - name: local_service
                  domains: ["*"]
                  routes:
                  - match:
                      prefix: "/wind_power.WindServer"
                    route: 
                      cluster: outbound|50052||wind-server.istio.svc.cluster.local
                      timeout: 60s
  - applyTo: HTTP_FILTER
    match:
        context: SIDECAR_OUTBOUND
        listener:
          name: 0.0.0.0_50052
          filterChain:
            filter:
              name: envoy.filters.network.http_connection_manager
              subFilter:
                name: envoy.filters.http.router
    patch:
        operation: INSERT_BEFORE
        value:   
                name: envoy.filters.http.grpc_json_transcoder
                typed_config:
                  "@type": type.googleapis.com/envoy.extensions.filters.http.grpc_json_transcoder.v3.GrpcJsonTranscoder
                  proto_descriptor: "/etc/envoy/proto.pb"
                  services: ["wind_power.WindServer"]
                  print_options:
                    add_whitespace: true
                    always_print_primitive_fields: true
                    always_print_enums_as_ints: false
                    preserve_proto_field_names: false
```



本文例子来自：

https://www.jianshu.com/p/a7e86058bf21