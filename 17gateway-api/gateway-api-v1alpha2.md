# 什么是gateway-api

Gateway API（之前叫 Service API）是由 SIG-NETWORK 社区(网络兴趣小组)管理的开源项目，项目地址：https://gateway-api.sigs.k8s.io/。主要原因是 Ingress 资源对象不能很好的满足网络需求，很多场景下 Ingress 控制器都需要通过定义 annotations 或者 crd 来进行功能扩展，这对于使用标准和支持是非常不利的，新推出的 Gateway API 旨在通过可扩展的面向角色的接口来增强服务网络。

Gateway API 是 Kubernetes 中的一个 API 资源集合，包括 GatewayClass、Gateway、HTTPRoute、TCPRoute、Service 等，这些资源共同为各种网络用例构建模型。

**Gateway API 的改进比当前的 Ingress 资源对象有很多更好的设计：**

面向角色 - Gateway 由各种 API 资源组成，这些资源根据使用和配置 Kubernetes 服务网络的角色进行建模。
通用性 - 和 Ingress 一样是一个具有众多实现的通用规范，Gateway API 是一个被设计成由许多实现支持的规范标准。
更具表现力 - Gateway API 资源支持基于 Header 头的匹配、流量权重等核心功能，这些功能在 Ingress 中只能通过自定义注解才能实现。
可扩展性 - Gateway API 允许自定义资源链接到 API 的各个层，这就允许在 API 结构的适当位置进行更精细的定制。

**还有一些其他值得关注的功能：**

GatewayClasses - GatewayClasses 将负载均衡实现的类型形式化，这些类使用户可以很容易了解到通过 Kubernetes 资源可以获得什么样的能力。
共享网关和跨命名空间支持 - 它们允许共享负载均衡器和 VIP，允许独立的路由资源绑定到同一个网关，这使得团队可以安全地共享（包括跨命名空间）基础设施，而不需要直接协调。
规范化路由和后端 - Gateway API 支持类型化的路由资源和不同类型的后端，这使得 API 可以灵活地支持各种协议（如 HTTP 和 gRPC）和各种后端服务（如 Kubernetes Service、存储桶或函数）。



**面向角色设计**

无论是道路、电力、数据中心还是 Kubernetes 集群，基础设施都是为了共享而建的，然而共享基础设施提供了一个共同的挑战，那就是如何为基础设施用户提供灵活性的同时还能被所有者(基础设施)控制。

Gateway API 通过对 Kubernetes 服务网络进行面向角色的设计来实现这一目标，平衡了灵活性和集中控制。它允许共享的网络基础设施（硬件负载均衡器、云网络、集群托管的代理等）被许多不同的团队使用，所有这些都受到集群运维设置的各种策略和约束。下面的例子显示了是如何在实践中运行的。

![operator](images\operator.png)

# 架构

![gateway-api-arch](images\gateway-api-arch.png)



# 概念

在整个 Gateway API 中涉及到3个角色：基础设施提供商、集群管理员、应用开发人员，在某些场景下可能还会涉及到应用管理员等角色。Gateway API 中定义了3种主要的资源模型：GatewayClass、Gateway、Route。

**1.GatewayClass**
GatewayClass 定义了一组共享相同配置和动作的网关。每个GatewayClass 由一个控制器处理，是一个集群范围的资源，必须至少有一个 GatewayClass 被定义。

这与 Ingress 的 IngressClass 类似，在 Ingress v1beta1 版本中，与 GatewayClass 类似的是 ingress-class 注解，而在Ingress V1 版本中，最接近的就是 IngressClass 资源对象。

**2.Gateway**
gatewy你可以认为是一个真正的网络入口。

Gateway 网关描述了如何将流量转化为集群内的服务，也就是说，它定义了一个请求，要求将流量从不了解 Kubernetes 的地方转换到集群内的服务。例如，由云端负载均衡器、集群内代理或外部硬件负载均衡器发送到 Kubernetes 服务的流量。

它定义了对特定负载均衡器配置的请求，该配置实现了 GatewayClass 的配置和行为规范，该资源可以由管理员直接创建，也可以由处理 GatewayClass 的控制器创建。

Gateway 可以附加到一个或多个路由引用上，这些路由引用的作用是将流量的一个子集导向特定的服务。

**3.Route 资源**
路由资源定义了特定的规则，用于将请求从网关映射到 Kubernetes 服务。

从 v1alpha2 版本开始，API 中包含四种 Route 路由资源类型，对于其他未定义的协议，鼓励采用特定实现的自定义路由类型，当然未来也可能会添加新的路由类型。

HTTPRoute
HTTPRoute 是用于 HTTP 或 HTTPS 连接，适用于我们想要检查 HTTP 请求并使用 HTTP 请求进行路由或修改的场景，比如使用 HTTP Headers 头进行路由，或在请求过程中对它们进行修改。

TLSRoute
TLSRoute 用于 TLS 连接，通过 SNI 进行区分，它适用于希望使用 SNI 作为主要路由方法的地方，并且对 HTTP 等更高级别协议的属性不感兴趣，连接的字节流不经任何检查就被代理到后端。

TCPRoute 和 UDPRoute
TCPRoute（和UDPRoute）旨在用于将一个或多个端口映射到单个后端。在这种情况下，没有可以用来选择同一端口的不同后端的判别器，所以每个 TCPRoute 在监听器上需要一个不同的端口。你可以使用 TLS，在这种情况下，加密的字节流会被传递到后端，当然也可以不使用 TLS，这样未加密的字节流将传递到后端。

**4.组合**
GatewayClass、Gateway、xRoute 和 Service 的组合定义了一个可实施的负载均衡器。下图说明了不同资源之间的关系:
![conbine](images\conbine.png)

# 部署

kubectl kustomize "config/crd"|kubectl apply -f -

安装webhook

 kubectl apply -f config/webhook/ 

# 资源详解

## GatewayClass

### controllerName

gatewayclass/istio-gc.yaml

kubectl apply -f istio-gc.yaml -n istio-system

```
apiVersion: gateway.networking.k8s.io/v1alpha2
kind: GatewayClass
metadata:
  name: istio
spec:
  controllerName: istio.io/gateway-controller
```

gatewayclass/bookinfo-gw.yaml

kubectl apply -f bookinfo-gw.yaml -n istio-system

```
apiVersion: gateway.networking.k8s.io/v1alpha2
kind: Gateway
metadata:
  name: bookinfo
spec:
  addresses:
  - value: istio-ingressgateway.istio-system.svc.cluster.local
    type: Hostname
  gatewayClassName: istio
  listeners:  
  - protocol: HTTP
    port: 80
    name: bookinfo-gw
    allowedRoutes:
      namespaces:
        from: All
```

gatewayclass/bookinfo-httproute.yaml

kubectl apply -f bookinfo-httproute.yaml -n istio

```
apiVersion: gateway.networking.k8s.io/v1alpha2
kind: HTTPRoute
metadata:
  name: bookinfo
spec:
  parentRefs:
  - name: bookinfo
    namespace: istio-system
    kind: Gateway
  rules:
  - matches:
    - path:
        type: PathPrefix
        value: /
    backendRefs:
    - name: productpage
      port: 9080
```

清理：

kubectl delete GatewayClass istio -n istio-system

kubectl delete gateways.gateway.networking.k8s.io  -n istio-system bookinfo 

kubectl delete httproutes.gateway.networking.k8s.io -n istio bookinfo



![1653294760(1)](images\1653294760(1).jpg)



![1653294818(1)](images\1653294818(1).jpg)





### description

gatewayclass/istio-gc-description.yaml

kubectl apply -f istio-gc-description.yaml -n istio-system

```
apiVersion: gateway.networking.k8s.io/v1alpha2
kind: GatewayClass
metadata:
  name: istio
spec:
  controllerName: istio.io/gateway-controller
  description: this is a test gatewayclass
```

清理：

kubectl delete gatewayclasses.gateway.networking.k8s.io istio -n istio-system

### parametersRef

略



## Gateway

### gatewayClassName

指定gatewayclass，略

### addresses

#### type，value

##### Hostname

gw-type-hostname.yaml

kubectl apply -f gw-type-hostname.yaml -n istio-system

```
apiVersion: gateway.networking.k8s.io/v1alpha2
kind: Gateway
metadata:
  name: bookinfo
spec:
  addresses:
  - value: istio-ingressgateway.istio-system.svc.cluster.local
    type: Hostname
  gatewayClassName: istio
  listeners:  
  - protocol: HTTP
    port: 80
    name: bookinfo-gw
    allowedRoutes:
      namespaces:
        from: All
```

gatewayclass/istio-gc.yaml

kubectl apply -f istio-gc.yaml -n istio-system

```
apiVersion: gateway.networking.k8s.io/v1alpha2
kind: GatewayClass
metadata:
  name: istio
spec:
  controllerName: istio.io/gateway-controller
```

gateway/bookinfo-httproute-system.yaml

kubectl apply -f bookinfo-httproute-system.yaml -n istio

```
apiVersion: gateway.networking.k8s.io/v1alpha2
kind: HTTPRoute
metadata:
  name: bookinfo
spec:
  parentRefs:
  - name: bookinfo
    namespace: istio-system
    kind: Gateway
  rules:
  - matches:
    - path:
        type: PathPrefix
        value: /
    backendRefs:
    - name: productpage
      port: 9080
```

清理：

kubectl delete GatewayClass istio -n istio-system

kubectl delete gateways.gateway.networking.k8s.io  -n istio-system bookinfo 

kubectl delete httproutes.gateway.networking.k8s.io -n istio bookinfo

##### IPAddress

会生成一个新的gateway pod和svc



gw-type-IPAddress.yaml

kubectl apply -f gw-type-IPAddress.yaml -n istio

```
apiVersion: gateway.networking.k8s.io/v1alpha2
kind: Gateway
metadata:
  name: bookinfo
spec:
  addresses:
  - value: 192.168.229.188
    type: IPAddress
  gatewayClassName: istio
  listeners:  
  - protocol: HTTP
    port: 80
    name: bookinfo-gw
    allowedRoutes:
      namespaces:
        from: All
```

gatewayclass/istio-gc.yaml

kubectl apply -f istio-gc.yaml -n istio-system

```
apiVersion: gateway.networking.k8s.io/v1alpha2
kind: GatewayClass
metadata:
  name: istio
spec:
  controllerName: istio.io/gateway-controller
```

gateway/bookinfo-httproute.yaml

kubectl apply -f bookinfo-httproute.yaml -n istio

```
apiVersion: gateway.networking.k8s.io/v1alpha2
kind: HTTPRoute
metadata:
  name: bookinfo
spec:
  parentRefs:
  - name: bookinfo
    namespace: istio
    kind: Gateway
  rules:
  - matches:
    - path:
        type: PathPrefix
        value: /
    backendRefs:
    - name: productpage
      port: 9080
```

清理：

kubectl delete GatewayClass istio -n istio-system

kubectl delete gateways.gateway.networking.k8s.io  -n istio bookinfo 

kubectl delete httproutes.gateway.networking.k8s.io -n istio bookinfo



bookinfo svc和pod是gateway

```
[root@node01 gateway]# kubectl get svc -n istio
NAME             TYPE           CLUSTER-IP      EXTERNAL-IP   PORT(S)                        AGE
bookinfo         LoadBalancer   10.68.241.108   <pending>     15021:30591/TCP,80:32397/TCP   2s
details          ClusterIP      10.68.78.229    <none>        9080/TCP                       12d
dubbo-provider   ClusterIP      10.68.136.151   <none>        20880/TCP                      14d
productpage      ClusterIP      10.68.212.43    <none>        9080/TCP                       12d
ratings          ClusterIP      10.68.145.201   <none>        9080/TCP                       12d
reviews          ClusterIP      10.68.186.128   <none>        9080/TCP                       12d
tcp-echo         ClusterIP      10.68.61.178    <none>        9000/TCP,9001/TCP              2d23h

[root@node01 gateway]# kubectl get pod -n istio
NAME                                        READY   STATUS    RESTARTS   AGE
bookinfo-677dcbdcc9-qksvt                   1/1     Running   0          8m32s
details-v1-6888489fc4-f55lx                 2/2     Running   22         7d21h
dubbo-sample-provider-v1-77f5dd68b-zn4w8    2/2     Running   22         7d21h
dubbo-sample-provider-v2-5f698bf48f-twbnl   2/2     Running   22         7d21h
productpage-v1-6d9d596946-pr5ck             2/2     Running   4          46h
ratings-v1-6c8cb48b86-27fmq                 2/2     Running   22         7d21h
reviews-v1-6b4948c8fd-jdds2                 2/2     Running   22         7d21h
reviews-v2-848b4fbcc8-ng7rk                 2/2     Running   22         7d21h
reviews-v3-5bdb6bbc9d-2hxb7                 2/2     Running   22         7d21h
tcp-echo-7974d4fbc4-lw2zg                   2/2     Running   6          2d23h
```



##### NamedAddress

 failed to assign to any requested addresses: Only Hostname is supported, ignoring [istio-ingressgateway.istio-system.svc.cluster.local]

**不支持**

gw-type-NamedAddress.yaml

kubectl apply -f gw-type-NamedAddress.yaml -n istio-system

```
apiVersion: gateway.networking.k8s.io/v1alpha2
kind: Gateway
metadata:
  name: bookinfo
spec:
  addresses:
  - value: istio-ingressgateway.istio-system.svc.cluster.local
    type: NamedAddress
  gatewayClassName: istio
  listeners:  
  - protocol: HTTP
    port: 80
    name: bookinfo-gw
    allowedRoutes:
      namespaces:
        from: All
```

gatewayclass/istio-gc.yaml

kubectl apply -f istio-gc.yaml -n istio-system

```
apiVersion: gateway.networking.k8s.io/v1alpha2
kind: GatewayClass
metadata:
  name: istio
spec:
  controllerName: istio.io/gateway-controller
```

gateway/bookinfo-httproute.yaml

kubectl apply -f bookinfo-httproute.yaml -n istio

```
apiVersion: gateway.networking.k8s.io/v1alpha2
kind: HTTPRoute
metadata:
  name: bookinfo
spec:
  parentRefs:
  - name: bookinfo
    namespace: istio
    kind: Gateway
  rules:
  - matches:
    - path:
        type: PathPrefix
        value: /
    backendRefs:
    - name: productpage
      port: 9080
```

清理：

kubectl delete GatewayClass istio -n istio-system

kubectl delete gateways.gateway.networking.k8s.io  -n istio-system bookinfo 

kubectl delete httproutes.gateway.networking.k8s.io -n istio bookinfo



### listeners

#### name

略

#### hostname

不能是*

##### semi-star

gw-listeners-hostname-star.yaml

kubectl apply -f gw-listeners-hostname-star.yaml -n istio

```
apiVersion: gateway.networking.k8s.io/v1alpha2
kind: Gateway
metadata:
  name: bookinfo
spec:
  addresses:
  - value: 192.168.229.188
    type: IPAddress
  gatewayClassName: istio
  listeners:  
  - protocol: HTTP
    port: 80
    name: bookinfo-gw
    hostname: "*.com"
    allowedRoutes:
      namespaces:
        from: All
```

gatewayclass/istio-gc.yaml

kubectl apply -f istio-gc.yaml -n istio-system

```
apiVersion: gateway.networking.k8s.io/v1alpha2
kind: GatewayClass
metadata:
  name: istio
spec:
  controllerName: istio.io/gateway-controller
```

gateway/bookinfo-httproute.yaml

kubectl apply -f bookinfo-httproute.yaml -n istio

```
apiVersion: gateway.networking.k8s.io/v1alpha2
kind: HTTPRoute
metadata:
  name: bookinfo
spec:
  parentRefs:
  - name: bookinfo
    namespace: istio
    kind: Gateway
  rules:
  - matches:
    - path:
        type: PathPrefix
        value: /
    backendRefs:
    - name: productpage
      port: 9080
```

清理：

kubectl delete GatewayClass istio -n istio-system

kubectl delete gateways.gateway.networking.k8s.io  -n istio bookinfo 

kubectl delete httproutes.gateway.networking.k8s.io -n istio bookinfo

![1653372013(1)](images\1653372013(1).jpg)



![1653372063(1)](images\1653372063(1).jpg)



##### 具体

gw-listeners-hostname-bookinfo.yaml

kubectl apply -f gw-listeners-hostname-bookinfo.yaml -n istio

```
apiVersion: gateway.networking.k8s.io/v1alpha2
kind: Gateway
metadata:
  name: bookinfo
spec:
  addresses:
  - value: 192.168.229.188
    type: IPAddress
  gatewayClassName: istio
  listeners:  
  - protocol: HTTP
    port: 80
    name: bookinfo-gw
    hostname: "bookinfo.com"
    allowedRoutes:
      namespaces:
        from: All
```

gatewayclass/istio-gc.yaml

kubectl apply -f istio-gc.yaml -n istio-system

```
apiVersion: gateway.networking.k8s.io/v1alpha2
kind: GatewayClass
metadata:
  name: istio
spec:
  controllerName: istio.io/gateway-controller
```

gateway/bookinfo-httproute.yaml

kubectl apply -f bookinfo-httproute.yaml -n istio

```
apiVersion: gateway.networking.k8s.io/v1alpha2
kind: HTTPRoute
metadata:
  name: bookinfo
spec:
  parentRefs:
  - name: bookinfo
    namespace: istio
    kind: Gateway
  rules:
  - matches:
    - path:
        type: PathPrefix
        value: /
    backendRefs:
    - name: productpage
      port: 9080
```

清理：

kubectl delete GatewayClass istio -n istio-system

kubectl delete gateways.gateway.networking.k8s.io  -n istio bookinfo 

kubectl delete httproutes.gateway.networking.k8s.io -n istio bookinfo

![1653373628(1)](images\1653373628(1).jpg)

![1653373670(1)](images\1653373670(1).jpg)



#### port，protocol

##### http

略

##### https

 openssl req -x509 -sha256 -nodes -days 365 -newkey rsa:2048  -keyout cert.key -out cert.crt  -config  openssl.conf

创建secret

kubectl create -n istio-system secret tls bookinfo-gateway-secret  --key ./cert.key --cert=./cert.crt



gateway/gw-listeners-protocol-https.yaml

kubectl apply -f gw-listeners-protocol-https.yaml -n istio-system

```
kind: Gateway
apiVersion: gateway.networking.k8s.io/v1alpha2
metadata:
  name: bookinfo
spec:
  addresses:
  - value: istio-ingressgateway.istio-system.svc.cluster.local
    type: Hostname
  gatewayClassName: istio
  listeners:  
  - hostname: "bookinfo.com"
    port: 443
    name: bookinfo-gw
    protocol: HTTPS
    tls:
      options: {}
      certificateRefs:
      - name: bookinfo-gateway-secret
        kind: Secret
    allowedRoutes:
      namespaces:
        from: All
```

gatewayclass/istio-gc.yaml

kubectl apply -f istio-gc.yaml -n istio-system

```
apiVersion: gateway.networking.k8s.io/v1alpha2
kind: GatewayClass
metadata:
  name: istio
spec:
  controllerName: istio.io/gateway-controller
```

gateway/bookinfo-httproute-system.yaml

kubectl apply -f bookinfo-httproute-system.yaml -n istio

```
apiVersion: gateway.networking.k8s.io/v1alpha2
kind: HTTPRoute
metadata:
  name: bookinfo
spec:
  parentRefs:
  - name: bookinfo
    namespace: istio-system
    kind: Gateway
  rules:
  - matches:
    - path:
        type: PathPrefix
        value: /
    backendRefs:
    - name: productpage
      port: 9080
```

清理：

kubectl delete GatewayClass istio -n istio-system

kubectl delete gateways.gateway.networking.k8s.io  -n istio-system bookinfo 

kubectl delete httproutes.gateway.networking.k8s.io -n istio bookinfo

kubectl delete -n istio-system secret bookinfo-gateway-secret



![1653376731(1)](images\1653376731(1).jpg)

![1653376778(1)](images\1653376778(1).jpg)



##### TCP

gateway/gw-listeners-protocol-TCP.yaml

kubectl apply -f gw-listeners-protocol-TCP.yaml -n istio-system

```
kind: Gateway
apiVersion: gateway.networking.k8s.io/v1alpha2
metadata:
  name: echo
spec:
  addresses:
  - value: istio-ingressgateway.istio-system.svc.cluster.local
    type: Hostname
  gatewayClassName: istio
  listeners:
  - protocol: TCP
    port: 15443
    name: gw-tcp
    allowedRoutes:
      namespaces:
        from: All
```

gatewayclass/istio-gc.yaml

kubectl apply -f istio-gc.yaml -n istio-system

```
apiVersion: gateway.networking.k8s.io/v1alpha2
kind: GatewayClass
metadata:
  name: istio
spec:
  controllerName: istio.io/gateway-controller
```



tcp-echo-services.yaml

kubectl  apply -f tcp-echo-services.yaml -n istio

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

gateway/echo-tcp-route.yaml

kubectl apply -f echo-tcp-route.yaml -n istio

```
kind: TCPRoute
apiVersion: gateway.networking.k8s.io/v1alpha2
metadata:
  name: echo
spec:
  parentRefs:
  - name: echo
    namespace: istio-system
    kind: Gateway
  rules:
  - backendRefs:
    - name: tcp-echo
      port: 9000
```

telnet 192.168.229.134  31010

清理：

kubectl delete GatewayClass istio -n istio-system

kubectl delete gateways.gateway.networking.k8s.io  -n istio-system echo

kubectl delete tcproutes.gateway.networking.k8s.io echo -n istio

kubectl delete -f tcp-echo-services.yaml -n istio

![1653379371(1)](images\1653379371(1).jpg)

![1653379420(1)](images\1653379420(1).jpg)

![1653379458(1)](images\1653379458(1).jpg)



##### UDP

略

#### allowedRoutes

##### kinds

###### group，kind

gw-listeners-allowedRoutes-kinds.yaml

kubectl apply -f gw-listeners-allowedRoutes-kinds.yaml -n istio-system

```
apiVersion: gateway.networking.k8s.io/v1alpha2
kind: Gateway
metadata:
  name: bookinfo
spec:
  addresses:
  - value: istio-ingressgateway.istio-system.svc.cluster.local
    type: Hostname
  gatewayClassName: istio
  listeners:  
  - protocol: HTTP
    port: 80
    name: bookinfo-gw
    allowedRoutes:
      kinds:
      - group: gateway.networking.k8s.io
        kind: HTTPRoute
      namespaces:
        from: All
```

gatewayclass/istio-gc.yaml

kubectl apply -f istio-gc.yaml -n istio-system

```
apiVersion: gateway.networking.k8s.io/v1alpha2
kind: GatewayClass
metadata:
  name: istio
spec:
  controllerName: istio.io/gateway-controller
```

gateway/bookinfo-httproute-system.yaml

kubectl apply -f bookinfo-httproute-system.yaml -n istio

```
apiVersion: gateway.networking.k8s.io/v1alpha2
kind: HTTPRoute
metadata:
  name: bookinfo
spec:
  parentRefs:
  - name: bookinfo
    namespace: istio-system
    kind: Gateway
  rules:
  - matches:
    - path:
        type: PathPrefix
        value: /
    backendRefs:
    - name: productpage
      port: 9080
```

清理：

kubectl delete GatewayClass istio -n istio-system

kubectl delete gateways.gateway.networking.k8s.io  -n istio-system bookinfo 

kubectl delete httproutes.gateway.networking.k8s.io -n istio bookinfo



![1653380993(1)](images\1653380993(1).jpg)

![1653381022(1)](images\1653381022(1).jpg)

##### namespaces

###### from

All

略



Selector

kubectl label ns istio gateway=bookinfo

gw-listeners-allowedRoutes-namespaces-Selector.yaml

kubectl apply -f gw-listeners-allowedRoutes-namespaces-Selector.yaml -n istio-system

```
apiVersion: gateway.networking.k8s.io/v1alpha2
kind: Gateway
metadata:
  name: bookinfo
spec:
  addresses:
  - value: istio-ingressgateway.istio-system.svc.cluster.local
    type: Hostname
  gatewayClassName: istio
  listeners:  
  - protocol: HTTP
    port: 80
    name: bookinfo-gw
    allowedRoutes:
      kinds:
      - group: gateway.networking.k8s.io
        kind: HTTPRoute
      namespaces:
        from: Selector
        selector:
          matchLabels:
            gateway: bookinfo
```

gatewayclass/istio-gc.yaml

kubectl apply -f istio-gc.yaml -n istio-system

```
apiVersion: gateway.networking.k8s.io/v1alpha2
kind: GatewayClass
metadata:
  name: istio
spec:
  controllerName: istio.io/gateway-controller
```

gateway/bookinfo-httproute-system.yaml

kubectl apply -f bookinfo-httproute-system.yaml -n istio

```
apiVersion: gateway.networking.k8s.io/v1alpha2
kind: HTTPRoute
metadata:
  name: bookinfo
spec:
  parentRefs:
  - name: bookinfo
    namespace: istio-system
    kind: Gateway
  rules:
  - matches:
    - path:
        type: PathPrefix
        value: /
    backendRefs:
    - name: productpage
      port: 9080
```

清理：

kubectl delete GatewayClass istio -n istio-system

kubectl delete gateways.gateway.networking.k8s.io  -n istio-system bookinfo 

kubectl delete httproutes.gateway.networking.k8s.io -n istio bookinfo





Same

gw-allowedRoutes-namespaces-same.yaml

kubectl apply -f gw-allowedRoutes-namespaces-same.yaml -n istio

```
apiVersion: gateway.networking.k8s.io/v1alpha2
kind: Gateway
metadata:
  name: bookinfo
spec:
  addresses:
  - value: 192.168.229.188
    type: IPAddress
  gatewayClassName: istio
  listeners:  
  - protocol: HTTP
    port: 80
    name: bookinfo-gw
    allowedRoutes:
      namespaces:
        from: Same
```

gatewayclass/istio-gc.yaml

kubectl apply -f istio-gc.yaml -n istio-system

```
apiVersion: gateway.networking.k8s.io/v1alpha2
kind: GatewayClass
metadata:
  name: istio
spec:
  controllerName: istio.io/gateway-controller
```

gateway/bookinfo-httproute.yaml

kubectl apply -f bookinfo-httproute.yaml -n istio

```
apiVersion: gateway.networking.k8s.io/v1alpha2
kind: HTTPRoute
metadata:
  name: bookinfo
spec:
  parentRefs:
  - name: bookinfo
    namespace: istio
    kind: Gateway
  rules:
  - matches:
    - path:
        type: PathPrefix
        value: /
    backendRefs:
    - name: productpage
      port: 9080
```

清理：

kubectl delete GatewayClass istio -n istio-system

kubectl delete gateways.gateway.networking.k8s.io  -n istio bookinfo 

kubectl delete httproutes.gateway.networking.k8s.io -n istio bookinfo



###### selector

matchLabels

略



matchExpressions

kubectl label ns istio gateway=bookinfo

gw-listeners-allowedRoutes-namespaces-Selector-matchExpressions.yaml

kubectl apply -f gw-listeners-allowedRoutes-namespaces-Selector-matchExpressions.yaml -n istio-system

```
apiVersion: gateway.networking.k8s.io/v1alpha2
kind: Gateway
metadata:
  name: bookinfo
spec:
  addresses:
  - value: istio-ingressgateway.istio-system.svc.cluster.local
    type: Hostname
  gatewayClassName: istio
  listeners:  
  - protocol: HTTP
    port: 80
    name: bookinfo-gw
    allowedRoutes:
      kinds:
      - group: gateway.networking.k8s.io
        kind: HTTPRoute
      namespaces:
        from: Selector
        selector:
          matchExpressions:
          - key: gateway
            operator: In
            values:
            - bookinfo
```

gatewayclass/istio-gc.yaml

kubectl apply -f istio-gc.yaml -n istio-system

```
apiVersion: gateway.networking.k8s.io/v1alpha2
kind: GatewayClass
metadata:
  name: istio
spec:
  controllerName: istio.io/gateway-controller
```

gateway/bookinfo-httproute-system.yaml

kubectl apply -f bookinfo-httproute-system.yaml -n istio

```
apiVersion: gateway.networking.k8s.io/v1alpha2
kind: HTTPRoute
metadata:
  name: bookinfo
spec:
  parentRefs:
  - name: bookinfo
    namespace: istio-system
    kind: Gateway
  rules:
  - matches:
    - path:
        type: PathPrefix
        value: /
    backendRefs:
    - name: productpage
      port: 9080
```

清理：

kubectl delete GatewayClass istio -n istio-system

kubectl delete gateways.gateway.networking.k8s.io  -n istio-system bookinfo 

kubectl delete httproutes.gateway.networking.k8s.io -n istio bookinfo





#### tls

##### certificateRefs

###### group,kind,name

 openssl req -x509 -sha256 -nodes -days 365 -newkey rsa:2048  -keyout cert.key -out cert.crt  -config  openssl.conf

创建secret

kubectl create -n istio-system secret tls bookinfo-gateway-secret  --key ./cert.key --cert=./cert.crt



gateway/gw-listeners-protocol-https.yaml

kubectl apply -f gw-listeners-protocol-https.yaml -n istio-system

```
kind: Gateway
apiVersion: gateway.networking.k8s.io/v1alpha2
metadata:
  name: bookinfo
spec:
  addresses:
  - value: istio-ingressgateway.istio-system.svc.cluster.local
    type: Hostname
  gatewayClassName: istio
  listeners:  
  - hostname: "bookinfo.com"
    port: 443
    name: bookinfo-gw
    protocol: HTTPS
    tls:
      options: {}
      certificateRefs:
      - name: bookinfo-gateway-secret
        kind: Secret
    allowedRoutes:
      namespaces:
        from: All
```

gatewayclass/istio-gc.yaml

kubectl apply -f istio-gc.yaml -n istio-system

```
apiVersion: gateway.networking.k8s.io/v1alpha2
kind: GatewayClass
metadata:
  name: istio
spec:
  controllerName: istio.io/gateway-controller
```

gateway/bookinfo-httproute-system.yaml

kubectl apply -f bookinfo-httproute-system.yaml -n istio

```
apiVersion: gateway.networking.k8s.io/v1alpha2
kind: HTTPRoute
metadata:
  name: bookinfo
spec:
  parentRefs:
  - name: bookinfo
    namespace: istio-system
    kind: Gateway
  rules:
  - matches:
    - path:
        type: PathPrefix
        value: /
    backendRefs:
    - name: productpage
      port: 9080
```

清理：

kubectl delete GatewayClass istio -n istio-system

kubectl delete gateways.gateway.networking.k8s.io  -n istio-system bookinfo 

kubectl delete httproutes.gateway.networking.k8s.io -n istio bookinfo

kubectl delete -n istio-system secret bookinfo-gateway-secret





###### namespace



 openssl req -x509 -sha256 -nodes -days 365 -newkey rsa:2048  -keyout cert.key -out cert.crt  -config  openssl.conf

创建secret

kubectl create -n istio-system secret tls bookinfo-gateway-secret  --key ./cert.key --cert=./cert.crt



gateway/gw-listeners-tls-certificateRefs-namespace.yaml

kubectl apply -f gw-listeners-tls-certificateRefs-namespace.yaml -n istio-system

```
kind: Gateway
apiVersion: gateway.networking.k8s.io/v1alpha2
metadata:
  name: bookinfo
spec:
  addresses:
  - value: istio-ingressgateway.istio-system.svc.cluster.local
    type: Hostname
  gatewayClassName: istio
  listeners:  
  - hostname: "bookinfo.com"
    port: 443
    name: bookinfo-gw
    protocol: HTTPS
    tls:
      options: {}
      certificateRefs:
      - name: bookinfo-gateway-secret
        namespace: istio-system
        kind: Secret
    allowedRoutes:
      namespaces:
        from: All
```

gatewayclass/istio-gc.yaml

kubectl apply -f istio-gc.yaml -n istio-system

```
apiVersion: gateway.networking.k8s.io/v1alpha2
kind: GatewayClass
metadata:
  name: istio
spec:
  controllerName: istio.io/gateway-controller
```

gateway/bookinfo-httproute-system.yaml

kubectl apply -f bookinfo-httproute-system.yaml -n istio

```
apiVersion: gateway.networking.k8s.io/v1alpha2
kind: HTTPRoute
metadata:
  name: bookinfo
spec:
  parentRefs:
  - name: bookinfo
    namespace: istio-system
    kind: Gateway
  rules:
  - matches:
    - path:
        type: PathPrefix
        value: /
    backendRefs:
    - name: productpage
      port: 9080
```

清理：

kubectl delete GatewayClass istio -n istio-system

kubectl delete gateways.gateway.networking.k8s.io  -n istio-system bookinfo 

kubectl delete httproutes.gateway.networking.k8s.io -n istio bookinfo

kubectl delete -n istio-system secret bookinfo-gateway-secret





##### mode

###### Terminate

默认

openssl req -x509 -sha256 -nodes -days 365 -newkey rsa:2048  -keyout cert.key -out cert.crt  -config  openssl.conf

创建secret

kubectl create -n istio-system secret tls bookinfo-gateway-secret  --key ./cert.key --cert=./cert.crt



gateway/gw-listeners-tls-mode-Terminate.yaml

kubectl apply -f gw-listeners-tls-mode-Terminate.yaml -n istio-system

```
kind: Gateway
apiVersion: gateway.networking.k8s.io/v1alpha2
metadata:
  name: bookinfo
spec:
  addresses:
  - value: istio-ingressgateway.istio-system.svc.cluster.local
    type: Hostname
  gatewayClassName: istio
  listeners:  
  - hostname: "bookinfo.com"
    port: 443
    name: bookinfo-gw
    protocol: HTTPS
    tls:
      options: {}
      mode: Terminate
      certificateRefs:
      - name: bookinfo-gateway-secret
        kind: Secret
    allowedRoutes:
      namespaces:
        from: All
```

gatewayclass/istio-gc.yaml

kubectl apply -f istio-gc.yaml -n istio-system

```
apiVersion: gateway.networking.k8s.io/v1alpha2
kind: GatewayClass
metadata:
  name: istio
spec:
  controllerName: istio.io/gateway-controller
```

gateway/bookinfo-httproute-system.yaml

kubectl apply -f bookinfo-httproute-system.yaml -n istio

```
apiVersion: gateway.networking.k8s.io/v1alpha2
kind: HTTPRoute
metadata:
  name: bookinfo
spec:
  parentRefs:
  - name: bookinfo
    namespace: istio-system
    kind: Gateway
  rules:
  - matches:
    - path:
        type: PathPrefix
        value: /
    backendRefs:
    - name: productpage
      port: 9080
```

清理：

kubectl delete GatewayClass istio -n istio-system

kubectl delete gateways.gateway.networking.k8s.io  -n istio-system bookinfo 

kubectl delete httproutes.gateway.networking.k8s.io -n istio bookinfo

kubectl delete -n istio-system secret bookinfo-gateway-secret

![1653456209(1)](images\1653456209(1).jpg)

![1653456251(1)](images\1653456251(1).jpg)



![1653456301(1)](images\1653456301(1).jpg)

![1653456334(1)](images\1653456334(1).jpg)

###### Passthrough

1创建证书

 openssl req -x509 -sha256 -nodes -days 365 -newkey rsa:2048 -subj '/O=example Inc./CN=example.com' -keyout example.com.key -out example.com.crt 

 openssl req -out nginx.example.com.csr -newkey rsa:2048 -nodes -keyout nginx.example.com.key -subj "/CN=nginx.example.com/O=some organization" 

 openssl x509 -req -days 365 -CA example.com.crt -CAkey example.com.key -set_serial 0 -in nginx.example.com.csr -out nginx.example.com.crt 

2创建secret

 kubectl create secret tls nginx-server-certs --key nginx.example.com.key --cert nginx.example.com.crt  -n istio

3创建nginx配置文件

gateway/passthough/nginx.conf

```
events {
}

http {
  log_format main '$remote_addr - $remote_user [$time_local]  $status '
  '"$request" $body_bytes_sent "$http_referer" '
  '"$http_user_agent" "$http_x_forwarded_for"';
  access_log /var/log/nginx/access.log main;
  error_log  /var/log/nginx/error.log;

  server {
    listen 443 ssl;

    root /usr/share/nginx/html;
    index index.html;

    server_name nginx.example.com;
    ssl_certificate /etc/nginx-server-certs/tls.crt;
    ssl_certificate_key /etc/nginx-server-certs/tls.key;
  }
}
```

 kubectl create configmap nginx-configmap --from-file=nginx.conf=./nginx.conf  -nistio

4创建deploy

gateway/passthough/nginx-deploy.yaml

kubectl apply -f nginx-deploy.yaml -n istio

```
apiVersion: v1
kind: Service
metadata:
  name: my-nginx
  labels:
    run: my-nginx
spec:
  ports:
  - port: 443
    protocol: TCP
  selector:
    run: my-nginx
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: my-nginx
spec:
  selector:
    matchLabels:
      run: my-nginx
  replicas: 1
  template:
    metadata:
      labels:
        run: my-nginx
    spec:
      containers:
      - name: my-nginx
        image: nginx
        ports:
        - containerPort: 443
        volumeMounts:
        - name: nginx-config
          mountPath: /etc/nginx
          readOnly: true
        - name: nginx-server-certs
          mountPath: /etc/nginx-server-certs
          readOnly: true
      volumes:
      - name: nginx-config
        configMap:
          name: nginx-configmap
      - name: nginx-server-certs
        secret:
          secretName: nginx-server-certs
```



gateway/passthough/gw-listeners-tls-mode-Passthrough.yaml

kubectl apply -f gw-listeners-tls-mode-Passthrough.yaml -n istio-system

```
kind: Gateway
apiVersion: gateway.networking.k8s.io/v1alpha2
metadata:
  name: bookinfo
spec:
  addresses:
  - value: istio-ingressgateway.istio-system.svc.cluster.local
    type: Hostname
  gatewayClassName: istio
  listeners:  
  - hostname: "nginx.example.com"
    port: 443
    name: gw-bookinfo
    protocol: TLS
    tls:
      mode: Passthrough
    allowedRoutes:
      namespaces:
        from: All
```

gatewayclass/isito-gc.yaml

kubectl apply -f isito-gc.yaml -n istio-system

```
apiVersion: networking.x-k8s.io/v1alpha1
kind: GatewayClass
metadata:
  name: istio
spec:
  controller: istio.io/gateway-controller
```

gateway/passthough/nginx-tls-route.yaml

kubectl apply -f nginx-tls-route.yaml -n istio

```
apiVersion: gateway.networking.k8s.io/v1alpha2
kind: TLSRoute
metadata:
  name: bookinfo
spec:
  parentRefs:
  - name: bookinfo
    namespace: istio-system
    kind: Gateway
  rules:
  - backendRefs:
    - name: my-nginx
      port: 443
```

访问

清理：

kubectl delete GatewayClass istio -n istio-system

kubectl delete gateways.gateway.networking.k8s.io  -n istio-system bookinfo 

kubectl delete tlsroutes.gateway.networking.k8s.io -n istio bookinfo

kubectl delete -f nginx-deploy.yaml -n istio

 kubectl delete configmap nginx-configmap -n istio

kubectl delete secret nginx-server-certs   -n istio



https://nginx.example.com:32388/

![1653457342(1)](images\1653457342(1).jpg)

![1653457408(1)](images\1653457408(1).jpg)

![1653457437(1)](images\1653457437(1).jpg)



##### options

略



## HttpRoute

### parentRefs

#### group，kind，name，namespace

httproute/bookinfo-httproute-system.yaml

kubectl apply -f bookinfo-httproute-system.yaml -n istio

```
apiVersion: gateway.networking.k8s.io/v1alpha2
kind: HTTPRoute
metadata:
  name: bookinfo
spec:
  parentRefs:
  - name: bookinfo
    namespace: istio-system
    group: gateway.networking.k8s.io
    kind: Gateway
  rules:
  - matches:
    - path:
        type: PathPrefix
        value: /
    backendRefs:
    - name: productpage
      port: 9080
```



httproute/gw-bookinfo.yaml

kubectl apply -f gw-bookinfo.yaml -n istio-system

```
apiVersion: gateway.networking.k8s.io/v1alpha2
kind: Gateway
metadata:
  name: bookinfo
spec:
  addresses:
  - value: istio-ingressgateway.istio-system.svc.cluster.local
    type: Hostname
  gatewayClassName: istio
  listeners:  
  - protocol: HTTP
    port: 80
    name: bookinfo-gw
    allowedRoutes:
      namespaces:
        from: All
```

gatewayclass/istio-gc.yaml

kubectl apply -f istio-gc.yaml -n istio-system

```
apiVersion: gateway.networking.k8s.io/v1alpha2
kind: GatewayClass
metadata:
  name: istio
spec:
  controllerName: istio.io/gateway-controller
```

清理：

kubectl delete GatewayClass istio -n istio-system

kubectl delete gateways.gateway.networking.k8s.io  -n istio-system bookinfo 

kubectl delete httproutes.gateway.networking.k8s.io -n istio bookinfo



#### sectionName

**gateway listeners的名字**

httproute/bookinfo-httproute-system-sectionName.yaml

kubectl apply -f bookinfo-httproute-system-sectionName.yaml -n istio

```
apiVersion: gateway.networking.k8s.io/v1alpha2
kind: HTTPRoute
metadata:
  name: bookinfo
spec:
  parentRefs:
  - name: bookinfo
    namespace: istio-system
    group: gateway.networking.k8s.io
    sectionName: bookinfo-gw
    kind: Gateway
  rules:
  - matches:
    - path:
        type: PathPrefix
        value: /
    backendRefs:
    - name: productpage
      port: 9080
```



httproute/gw-bookinfo.yaml

kubectl apply -f gw-bookinfo.yaml -n istio-system

```
apiVersion: gateway.networking.k8s.io/v1alpha2
kind: Gateway
metadata:
  name: bookinfo
spec:
  addresses:
  - value: istio-ingressgateway.istio-system.svc.cluster.local
    type: Hostname
  gatewayClassName: istio
  listeners:  
  - protocol: HTTP
    port: 80
    name: bookinfo-gw
    allowedRoutes:
      namespaces:
        from: All
```

gatewayclass/istio-gc.yaml

kubectl apply -f istio-gc.yaml -n istio-system

```
apiVersion: gateway.networking.k8s.io/v1alpha2
kind: GatewayClass
metadata:
  name: istio
spec:
  controllerName: istio.io/gateway-controller
```

清理：

kubectl delete GatewayClass istio -n istio-system

kubectl delete gateways.gateway.networking.k8s.io  -n istio-system bookinfo 

kubectl delete httproutes.gateway.networking.k8s.io -n istio bookinfo





### hostnames

**不指定为所有**

#### 具体

httproute/bookinfo-httproute-system-hostnames-specific.yaml

kubectl apply -f bookinfo-httproute-system-hostnames-specific.yaml -n istio

```
apiVersion: gateway.networking.k8s.io/v1alpha2
kind: HTTPRoute
metadata:
  name: bookinfo
spec:
  hostnames:
  - bookinfo.com
  - bookinfo.demo
  parentRefs:
  - name: bookinfo
    namespace: istio-system
    group: gateway.networking.k8s.io
    kind: Gateway
  rules:
  - matches:
    - path:
        type: PathPrefix
        value: /
    backendRefs:
    - name: productpage
      port: 9080
```



httproute/gw-bookinfo.yaml

kubectl apply -f gw-bookinfo.yaml -n istio-system

```
apiVersion: gateway.networking.k8s.io/v1alpha2
kind: Gateway
metadata:
  name: bookinfo
spec:
  addresses:
  - value: istio-ingressgateway.istio-system.svc.cluster.local
    type: Hostname
  gatewayClassName: istio
  listeners:  
  - protocol: HTTP
    port: 80
    name: bookinfo-gw
    allowedRoutes:
      namespaces:
        from: All
```

gatewayclass/istio-gc.yaml

kubectl apply -f istio-gc.yaml -n istio-system

```
apiVersion: gateway.networking.k8s.io/v1alpha2
kind: GatewayClass
metadata:
  name: istio
spec:
  controllerName: istio.io/gateway-controller
```

清理：

kubectl delete GatewayClass istio -n istio-system

kubectl delete gateways.gateway.networking.k8s.io  -n istio-system bookinfo 

kubectl delete httproutes.gateway.networking.k8s.io -n istio bookinfo



![1653461571(1)](images\1653461571(1).jpg)

![1653461614(1)](images\1653461614(1).jpg)



#### semi-star

**只能通过externalIp访问**

httproute/bookinfo-httproute-system-hostnames-star.yaml

kubectl apply -f bookinfo-httproute-system-hostnames-star.yaml -n istio

```
apiVersion: gateway.networking.k8s.io/v1alpha2
kind: HTTPRoute
metadata:
  name: bookinfo
spec:
  hostnames:
  - "*.com"
  parentRefs:
  - name: bookinfo
    namespace: istio-system
    group: gateway.networking.k8s.io
    kind: Gateway
  rules:
  - matches:
    - path:
        type: PathPrefix
        value: /
    backendRefs:
    - name: productpage
      port: 9080
```



httproute/gw-bookinfo.yaml

kubectl apply -f gw-bookinfo.yaml -n istio-system

```
apiVersion: gateway.networking.k8s.io/v1alpha2
kind: Gateway
metadata:
  name: bookinfo
spec:
  addresses:
  - value: istio-ingressgateway.istio-system.svc.cluster.local
    type: Hostname
  gatewayClassName: istio
  listeners:  
  - protocol: HTTP
    port: 80
    name: bookinfo-gw
    allowedRoutes:
      namespaces:
        from: All
```

gatewayclass/istio-gc.yaml

kubectl apply -f istio-gc.yaml -n istio-system

```
apiVersion: gateway.networking.k8s.io/v1alpha2
kind: GatewayClass
metadata:
  name: istio
spec:
  controllerName: istio.io/gateway-controller
```

清理：

kubectl delete GatewayClass istio -n istio-system

kubectl delete gateways.gateway.networking.k8s.io  -n istio-system bookinfo 

kubectl delete httproutes.gateway.networking.k8s.io -n istio bookinfo



### rules

#### matches

##### headers

###### Exact

httproute/bookinfo-httproute-system-rules-matches-headers.yaml

kubectl apply -f bookinfo-httproute-system-rules-matches-headers.yaml -n istio

```
apiVersion: gateway.networking.k8s.io/v1alpha2
kind: HTTPRoute
metadata:
  name: bookinfo
spec:
  parentRefs:
  - name: bookinfo
    namespace: istio-system
    group: gateway.networking.k8s.io
    kind: Gateway
  rules:
  - matches:
    - headers:
      - type: Exact
        name: test
        value: test
    backendRefs:
    - name: productpage
      port: 9080
```



httproute/gw-bookinfo.yaml

kubectl apply -f gw-bookinfo.yaml -n istio-system

```
apiVersion: gateway.networking.k8s.io/v1alpha2
kind: Gateway
metadata:
  name: bookinfo
spec:
  addresses:
  - value: istio-ingressgateway.istio-system.svc.cluster.local
    type: Hostname
  gatewayClassName: istio
  listeners:  
  - protocol: HTTP
    port: 80
    name: bookinfo-gw
    allowedRoutes:
      namespaces:
        from: All
```

gatewayclass/istio-gc.yaml

kubectl apply -f istio-gc.yaml -n istio-system

```
apiVersion: gateway.networking.k8s.io/v1alpha2
kind: GatewayClass
metadata:
  name: istio
spec:
  controllerName: istio.io/gateway-controller
```

清理：

kubectl delete GatewayClass istio -n istio-system

kubectl delete gateways.gateway.networking.k8s.io  -n istio-system bookinfo 

kubectl delete httproutes.gateway.networking.k8s.io -n istio bookinfo



curl http://bookinfo.com:32542/productpage -H "test: test"

![1653463701(1)](images\1653463701(1).jpg)



###### RegularExpression

httproute/bookinfo-httproute-system-rules-matches-headers-RegularExpression.yaml

kubectl apply -f bookinfo-httproute-system-rules-matches-headers-RegularExpression.yaml -n istio

```
apiVersion: gateway.networking.k8s.io/v1alpha2
kind: HTTPRoute
metadata:
  name: bookinfo
spec:
  parentRefs:
  - name: bookinfo
    namespace: istio-system
    group: gateway.networking.k8s.io
    kind: Gateway
  rules:
  - matches:
    - headers:
      - type: RegularExpression
        name: test
        value: "value.*"
    backendRefs:
    - name: productpage
      port: 9080
```



httproute/gw-bookinfo.yaml

kubectl apply -f gw-bookinfo.yaml -n istio-system

```
apiVersion: gateway.networking.k8s.io/v1alpha2
kind: Gateway
metadata:
  name: bookinfo
spec:
  addresses:
  - value: istio-ingressgateway.istio-system.svc.cluster.local
    type: Hostname
  gatewayClassName: istio
  listeners:  
  - protocol: HTTP
    port: 80
    name: bookinfo-gw
    allowedRoutes:
      namespaces:
        from: All
```

gatewayclass/istio-gc.yaml

kubectl apply -f istio-gc.yaml -n istio-system

```
apiVersion: gateway.networking.k8s.io/v1alpha2
kind: GatewayClass
metadata:
  name: istio
spec:
  controllerName: istio.io/gateway-controller
```

清理：

kubectl delete GatewayClass istio -n istio-system

kubectl delete gateways.gateway.networking.k8s.io  -n istio-system bookinfo 

kubectl delete httproutes.gateway.networking.k8s.io -n istio bookinfo



curl http://bookinfo.com:32542/productpage -H "test: value123"



![1653463845(1)](images\1653463845(1).jpg)



##### method

httproute/bookinfo-httproute-system-rules-matches-method.yaml

kubectl apply -f bookinfo-httproute-system-rules-matches-method.yaml -n istio

```
apiVersion: gateway.networking.k8s.io/v1alpha2
kind: HTTPRoute
metadata:
  name: bookinfo
spec:
  parentRefs:
  - name: bookinfo
    namespace: istio-system
    group: gateway.networking.k8s.io
    kind: Gateway
  rules:
  - matches:
    - method: GET
    backendRefs:
    - name: productpage
      port: 9080
```



httproute/gw-bookinfo.yaml

kubectl apply -f gw-bookinfo.yaml -n istio-system

```
apiVersion: gateway.networking.k8s.io/v1alpha2
kind: Gateway
metadata:
  name: bookinfo
spec:
  addresses:
  - value: istio-ingressgateway.istio-system.svc.cluster.local
    type: Hostname
  gatewayClassName: istio
  listeners:  
  - protocol: HTTP
    port: 80
    name: bookinfo-gw
    allowedRoutes:
      namespaces:
        from: All
```

gatewayclass/istio-gc.yaml

kubectl apply -f istio-gc.yaml -n istio-system

```
apiVersion: gateway.networking.k8s.io/v1alpha2
kind: GatewayClass
metadata:
  name: istio
spec:
  controllerName: istio.io/gateway-controller
```

清理：

kubectl delete GatewayClass istio -n istio-system

kubectl delete gateways.gateway.networking.k8s.io  -n istio-system bookinfo 

kubectl delete httproutes.gateway.networking.k8s.io -n istio bookinfo

![1653465066(1)](images\1653465066(1).jpg)



##### path

###### Exact

httproute/bookinfo-httproute-system-rules-matches-path-Exact.yaml

kubectl apply -f bookinfo-httproute-system-rules-matches-path-Exact.yaml -n istio

```
apiVersion: gateway.networking.k8s.io/v1alpha2
kind: HTTPRoute
metadata:
  name: bookinfo
spec:
  parentRefs:
  - name: bookinfo
    namespace: istio-system
    group: gateway.networking.k8s.io
    kind: Gateway
  rules:
  - matches:
    - path: 
        type: Exact
        value: /productpage
    backendRefs:
    - name: productpage
      port: 9080
```



httproute/gw-bookinfo.yaml

kubectl apply -f gw-bookinfo.yaml -n istio-system

```
apiVersion: gateway.networking.k8s.io/v1alpha2
kind: Gateway
metadata:
  name: bookinfo
spec:
  addresses:
  - value: istio-ingressgateway.istio-system.svc.cluster.local
    type: Hostname
  gatewayClassName: istio
  listeners:  
  - protocol: HTTP
    port: 80
    name: bookinfo-gw
    allowedRoutes:
      namespaces:
        from: All
```

gatewayclass/istio-gc.yaml

kubectl apply -f istio-gc.yaml -n istio-system

```
apiVersion: gateway.networking.k8s.io/v1alpha2
kind: GatewayClass
metadata:
  name: istio
spec:
  controllerName: istio.io/gateway-controller
```

清理：

kubectl delete GatewayClass istio -n istio-system

kubectl delete gateways.gateway.networking.k8s.io  -n istio-system bookinfo 

kubectl delete httproutes.gateway.networking.k8s.io -n istio bookinfo



![1653465276(1)](images\1653465276(1).jpg)



###### PathPrefix

httproute/bookinfo-httproute-system-rules-matches-path-PathPrefix.yaml

kubectl apply -f bookinfo-httproute-system-rules-matches-path-PathPrefix.yaml -n istio

```
apiVersion: gateway.networking.k8s.io/v1alpha2
kind: HTTPRoute
metadata:
  name: bookinfo
spec:
  parentRefs:
  - name: bookinfo
    namespace: istio-system
    group: gateway.networking.k8s.io
    kind: Gateway
  rules:
  - matches:
    - path: 
        type: PathPrefix
        value: /
    backendRefs:
    - name: productpage
      port: 9080
```



httproute/gw-bookinfo.yaml

kubectl apply -f gw-bookinfo.yaml -n istio-system

```
apiVersion: gateway.networking.k8s.io/v1alpha2
kind: Gateway
metadata:
  name: bookinfo
spec:
  addresses:
  - value: istio-ingressgateway.istio-system.svc.cluster.local
    type: Hostname
  gatewayClassName: istio
  listeners:  
  - protocol: HTTP
    port: 80
    name: bookinfo-gw
    allowedRoutes:
      namespaces:
        from: All
```

gatewayclass/istio-gc.yaml

kubectl apply -f istio-gc.yaml -n istio-system

```
apiVersion: gateway.networking.k8s.io/v1alpha2
kind: GatewayClass
metadata:
  name: istio
spec:
  controllerName: istio.io/gateway-controller
```

清理：

kubectl delete GatewayClass istio -n istio-system

kubectl delete gateways.gateway.networking.k8s.io  -n istio-system bookinfo 

kubectl delete httproutes.gateway.networking.k8s.io -n istio bookinfo



![1653465367(1)](images\1653465367(1).jpg)



###### RegularExpression

httproute/bookinfo-httproute-system-rules-matches-path-RegularExpression.yaml

kubectl apply -f bookinfo-httproute-system-rules-matches-path-RegularExpression.yaml -n istio

```
apiVersion: gateway.networking.k8s.io/v1alpha2
kind: HTTPRoute
metadata:
  name: bookinfo
spec:
  parentRefs:
  - name: bookinfo
    namespace: istio-system
    group: gateway.networking.k8s.io
    kind: Gateway
  rules:
  - matches:
    - path: 
        type: RegularExpression
        value: "/pro.*"
    backendRefs:
    - name: productpage
      port: 9080
```



httproute/gw-bookinfo.yaml

kubectl apply -f gw-bookinfo.yaml -n istio-system

```
apiVersion: gateway.networking.k8s.io/v1alpha2
kind: Gateway
metadata:
  name: bookinfo
spec:
  addresses:
  - value: istio-ingressgateway.istio-system.svc.cluster.local
    type: Hostname
  gatewayClassName: istio
  listeners:  
  - protocol: HTTP
    port: 80
    name: bookinfo-gw
    allowedRoutes:
      namespaces:
        from: All
```

gatewayclass/istio-gc.yaml

kubectl apply -f istio-gc.yaml -n istio-system

```
apiVersion: gateway.networking.k8s.io/v1alpha2
kind: GatewayClass
metadata:
  name: istio
spec:
  controllerName: istio.io/gateway-controller
```

清理：

kubectl delete GatewayClass istio -n istio-system

kubectl delete gateways.gateway.networking.k8s.io  -n istio-system bookinfo 

kubectl delete httproutes.gateway.networking.k8s.io -n istio bookinfo



![1653465467(1)](images\1653465467(1).jpg)



##### queryParams

###### Exact

httproute/bookinfo-httproute-system-rules-matches-queryParams-Exact.yaml

kubectl apply -f bookinfo-httproute-system-rules-matches-queryParams-Exact.yaml -n istio

```
apiVersion: gateway.networking.k8s.io/v1alpha2
kind: HTTPRoute
metadata:
  name: bookinfo
spec:
  parentRefs:
  - name: bookinfo
    namespace: istio-system
    group: gateway.networking.k8s.io
    kind: Gateway
  rules:
  - matches:
    - queryParams: 
      - type: Exact
        name: test
        value: test
    backendRefs:
    - name: productpage
      port: 9080
```



httproute/gw-bookinfo.yaml

kubectl apply -f gw-bookinfo.yaml -n istio-system

```
apiVersion: gateway.networking.k8s.io/v1alpha2
kind: Gateway
metadata:
  name: bookinfo
spec:
  addresses:
  - value: istio-ingressgateway.istio-system.svc.cluster.local
    type: Hostname
  gatewayClassName: istio
  listeners:  
  - protocol: HTTP
    port: 80
    name: bookinfo-gw
    allowedRoutes:
      namespaces:
        from: All
```

gatewayclass/istio-gc.yaml

kubectl apply -f istio-gc.yaml -n istio-system

```
apiVersion: gateway.networking.k8s.io/v1alpha2
kind: GatewayClass
metadata:
  name: istio
spec:
  controllerName: istio.io/gateway-controller
```

清理：

kubectl delete GatewayClass istio -n istio-system

kubectl delete gateways.gateway.networking.k8s.io  -n istio-system bookinfo 

kubectl delete httproutes.gateway.networking.k8s.io -n istio bookinfo

http://bookinfo.com:32542/productpage?test=test

![1653466080(1)](images\1653466080(1).jpg)



###### RegularExpression

httproute/bookinfo-httproute-system-rules-matches-queryParams-RegularExpression.yaml

kubectl apply -f bookinfo-httproute-system-rules-matches-queryParams-RegularExpression.yaml -n istio

```
apiVersion: gateway.networking.k8s.io/v1alpha2
kind: HTTPRoute
metadata:
  name: bookinfo
spec:
  parentRefs:
  - name: bookinfo
    namespace: istio-system
    group: gateway.networking.k8s.io
    kind: Gateway
  rules:
  - matches:
    - queryParams: 
      - type: RegularExpression
        name: test
        value: "value.*"
    backendRefs:
    - name: productpage
      port: 9080
```



httproute/gw-bookinfo.yaml

kubectl apply -f gw-bookinfo.yaml -n istio-system

```
apiVersion: gateway.networking.k8s.io/v1alpha2
kind: Gateway
metadata:
  name: bookinfo
spec:
  addresses:
  - value: istio-ingressgateway.istio-system.svc.cluster.local
    type: Hostname
  gatewayClassName: istio
  listeners:  
  - protocol: HTTP
    port: 80
    name: bookinfo-gw
    allowedRoutes:
      namespaces:
        from: All
```

gatewayclass/istio-gc.yaml

kubectl apply -f istio-gc.yaml -n istio-system

```
apiVersion: gateway.networking.k8s.io/v1alpha2
kind: GatewayClass
metadata:
  name: istio
spec:
  controllerName: istio.io/gateway-controller
```

清理：

kubectl delete GatewayClass istio -n istio-system

kubectl delete gateways.gateway.networking.k8s.io  -n istio-system bookinfo 

kubectl delete httproutes.gateway.networking.k8s.io -n istio bookinfo

http://bookinfo.com:32542/productpage?test=value123



![1653466196(1)](images\1653466196(1).jpg)



#### filters

##### requestRedirect

###### hostname,port,scheme

httproute/bookinfo-httproute-system-rules-filters-requestRedirect.yaml

kubectl apply -f bookinfo-httproute-system-rules-filters-requestRedirect.yaml -n istio

```
apiVersion: gateway.networking.k8s.io/v1alpha2
kind: HTTPRoute
metadata:
  name: bookinfo
spec:
  parentRefs:
  - name: bookinfo
    namespace: istio-system
    group: gateway.networking.k8s.io
    kind: Gateway
    sectionName: bookinfo-gw
  rules:
  - filters:
    - requestRedirect:
        hostname: bookinfo.com
        port: 32388
        scheme: https
      type: RequestRedirect
    backendRefs:
    - name: productpage
      port: 9080
---
apiVersion: gateway.networking.k8s.io/v1alpha2
kind: HTTPRoute
metadata:
  name: bookinfo-https
spec:
  parentRefs:
  - name: bookinfo
    namespace: istio-system
    group: gateway.networking.k8s.io
    kind: Gateway
    sectionName: bookinfo-gw-https
  rules:
  - matches:
    - path: 
        type: PathPrefix
        value: /
    backendRefs:
    - name: productpage
      port: 9080
```



 openssl req -x509 -sha256 -nodes -days 365 -newkey rsa:2048  -keyout cert.key -out cert.crt  -config  openssl.conf

创建secret

kubectl create -n istio-system secret tls bookinfo-gateway-secret  --key ./cert.key --cert=./cert.crt



httproute/gw-bookinfo-http-https.yaml

kubectl apply -f gw-bookinfo-http-https.yaml -n istio-system

```
apiVersion: gateway.networking.k8s.io/v1alpha2
kind: Gateway
metadata:
  name: bookinfo
spec:
  addresses:
  - value: istio-ingressgateway.istio-system.svc.cluster.local
    type: Hostname
  gatewayClassName: istio
  listeners:  
  - protocol: HTTP
    port: 80
    name: bookinfo-gw
    allowedRoutes:
      namespaces:
        from: All
  - protocol: HTTPS
    port: 443
    name: bookinfo-gw-https
    allowedRoutes:
      namespaces:
        from: All
    tls:
      options: {}
      certificateRefs:
      - name: bookinfo-gateway-secret
        kind: Secret
```

gatewayclass/istio-gc.yaml

kubectl apply -f istio-gc.yaml -n istio-system

```
apiVersion: gateway.networking.k8s.io/v1alpha2
kind: GatewayClass
metadata:
  name: istio
spec:
  controllerName: istio.io/gateway-controller
```

清理：

kubectl delete GatewayClass istio -n istio-system

kubectl delete gateways.gateway.networking.k8s.io  -n istio-system bookinfo 

kubectl delete httproutes.gateway.networking.k8s.io -n istio bookinfo

kubectl delete httproutes.gateway.networking.k8s.io -n istio bookinfo-https

kubectl delete -n istio-system secret  bookinfo-gateway-secret





###### statusCode

**supported values: "301", "302"**

httproute/bookinfo-httproute-system-rules-filters-requestRedirect-statusCode.yaml

kubectl apply -f bookinfo-httproute-system-rules-filters-requestRedirect-statusCode.yaml -n istio

```
apiVersion: gateway.networking.k8s.io/v1alpha2
kind: HTTPRoute
metadata:
  name: bookinfo
spec:
  parentRefs:
  - name: bookinfo
    namespace: istio-system
    group: gateway.networking.k8s.io
    kind: Gateway
    sectionName: bookinfo-gw
  rules:
  - filters:
    - requestRedirect:
        hostname: bookinfo.com
        port: 32388
        scheme: https
        statusCode: 301
      type: RequestRedirect
    backendRefs:
    - name: productpage
      port: 9080
---
apiVersion: gateway.networking.k8s.io/v1alpha2
kind: HTTPRoute
metadata:
  name: bookinfo-https
spec:
  parentRefs:
  - name: bookinfo
    namespace: istio-system
    group: gateway.networking.k8s.io
    kind: Gateway
    sectionName: bookinfo-gw-https
  rules:
  - matches:
    - path: 
        type: PathPrefix
        value: /
    backendRefs:
    - name: productpage
      port: 9080
```



 openssl req -x509 -sha256 -nodes -days 365 -newkey rsa:2048  -keyout cert.key -out cert.crt  -config  openssl.conf

创建secret

kubectl create -n istio-system secret tls bookinfo-gateway-secret  --key ./cert.key --cert=./cert.crt



httproute/gw-bookinfo-http-https.yaml

kubectl apply -f gw-bookinfo-http-https.yaml -n istio-system

```
apiVersion: gateway.networking.k8s.io/v1alpha2
kind: Gateway
metadata:
  name: bookinfo
spec:
  addresses:
  - value: istio-ingressgateway.istio-system.svc.cluster.local
    type: Hostname
  gatewayClassName: istio
  listeners:  
  - protocol: HTTP
    port: 80
    name: bookinfo-gw
    allowedRoutes:
      namespaces:
        from: All
  - protocol: HTTPS
    port: 443
    name: bookinfo-gw-https
    allowedRoutes:
      namespaces:
        from: All
    tls:
      options: {}
      certificateRefs:
      - name: bookinfo-gateway-secret
        kind: Secret
```

gatewayclass/istio-gc.yaml

kubectl apply -f istio-gc.yaml -n istio-system

```
apiVersion: gateway.networking.k8s.io/v1alpha2
kind: GatewayClass
metadata:
  name: istio
spec:
  controllerName: istio.io/gateway-controller
```

清理：

kubectl delete GatewayClass istio -n istio-system

kubectl delete gateways.gateway.networking.k8s.io  -n istio-system bookinfo 

kubectl delete httproutes.gateway.networking.k8s.io -n istio bookinfo

kubectl delete httproutes.gateway.networking.k8s.io -n istio bookinfo-https

kubectl delete -n istio-system secret  bookinfo-gateway-secret





##### requestMirror

###### backendRef

httproute/bookinfo-httproute-system-rules-filters-requestMirror-backendRef.yaml

kubectl apply -f bookinfo-httproute-system-rules-filters-requestMirror-backendRef.yaml -n istio

```
apiVersion: gateway.networking.k8s.io/v1alpha2
kind: HTTPRoute
metadata:
  name: bookinfo
spec:
  parentRefs:
  - name: bookinfo
    namespace: istio-system
    group: gateway.networking.k8s.io
    kind: Gateway
  rules:
  - matches:
    - path: 
        type: PathPrefix
        value: /
    filters:
    - requestMirror:
        backendRef:
          name: productpage-v2
          port: 9080
      type: RequestMirror
    backendRefs:
    - name: productpage
      port: 9080
```

deploy-productpage-v2.yaml

kubectl apply -f deploy-productpage-v2.yaml -n istio

```
apiVersion: v1
kind: Service
metadata:
  name: productpage-v2
  labels:
    app: productpage-v2
    service: productpage-v2
spec:
  ports:
  - port: 9080
    name: http
  selector:
    app: productpage-v2
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: productpage-v2
  labels:
    app: productpage-v2
    version: v2
spec:
  replicas: 1
  selector:
    matchLabels:
      app: productpage-v2
      version: v2
  template:
    metadata:
      labels:
        app: productpage-v2
        version: v2
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
---
```

httproute/gw-bookinfo.yaml

kubectl apply -f gw-bookinfo.yaml -n istio-system

```
apiVersion: gateway.networking.k8s.io/v1alpha2
kind: Gateway
metadata:
  name: bookinfo
spec:
  addresses:
  - value: istio-ingressgateway.istio-system.svc.cluster.local
    type: Hostname
  gatewayClassName: istio
  listeners:  
  - protocol: HTTP
    port: 80
    name: bookinfo-gw
    allowedRoutes:
      namespaces:
        from: All
```

gatewayclass/istio-gc.yaml

kubectl apply -f istio-gc.yaml -n istio-system

```
apiVersion: gateway.networking.k8s.io/v1alpha2
kind: GatewayClass
metadata:
  name: istio
spec:
  controllerName: istio.io/gateway-controller
```

清理：

kubectl delete GatewayClass istio -n istio-system

kubectl delete gateways.gateway.networking.k8s.io  -n istio-system bookinfo 

kubectl delete httproutes.gateway.networking.k8s.io -n istio bookinfo

kubectl delete -f deploy-productpage-v2.yaml -n istio



```
[root@node01 ~]# kubectl logs -n istio productpage-v2-7b59b6d467-rn2dp -f
INFO:root:start at port 9080
 * Serving Flask app "productpage" (lazy loading)
 * Environment: production
   WARNING: Do not use the development server in a production environment.
   Use a production WSGI server instead.
 * Debug mode: on
INFO:werkzeug: * Running on http://0.0.0.0:9080/ (Press CTRL+C to quit)
INFO:werkzeug: * Restarting with stat
INFO:root:start at port 9080
WARNING:werkzeug: * Debugger is active!
INFO:werkzeug: * Debugger PIN: 803-096-848


DEBUG:urllib3.connectionpool:Starting new HTTP connection (1): details:9080
send: b'GET /details/0 HTTP/1.1\r\nHost: details:9080\r\nuser-agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/99.0.4844.82 Safari/537.36\r\nAccept-Encoding: gzip, deflate\r\nAccept: */*\r\nConnection: keep-alive\r\nX-B3-TraceId: ce3843950b07ec8c32e7c188e65c1b98\r\nX-B3-SpanId: 69fe3f492d08b8bb\r\nX-B3-ParentSpanId: 07aa28c5622602e2\r\nx-request-id: ff0a4cc8-aef7-975f-9fb5-7b807a9f2215\r\n\r\n'
reply: 'HTTP/1.1 200 OK\r\n'
header: content-type: application/json
header: server: envoy
header: date: Wed, 25 May 2022 08:57:40 GMT
header: content-length: 178
header: x-envoy-upstream-service-time: 16
DEBUG:urllib3.connectionpool:http://details:9080 "GET /details/0 HTTP/1.1" 200 178
DEBUG:urllib3.connectionpool:Starting new HTTP connection (1): reviews:9080
send: b'GET /reviews/0 HTTP/1.1\r\nHost: reviews:9080\r\nuser-agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/99.0.4844.82 Safari/537.36\r\nAccept-Encoding: gzip, deflate\r\nAccept: */*\r\nConnection: keep-alive\r\nX-B3-TraceId: ce3843950b07ec8c32e7c188e65c1b98\r\nX-B3-SpanId: 69fe3f492d08b8bb\r\nX-B3-ParentSpanId: 07aa28c5622602e2\r\nx-request-id: ff0a4cc8-aef7-975f-9fb5-7b807a9f2215\r\n\r\n'
reply: 'HTTP/1.1 200 OK\r\n'
header: x-powered-by: Servlet/3.1
header: content-type: application/json
header: date: Wed, 25 May 2022 08:57:40 GMT
header: content-language: en-US
header: content-length: 295
header: x-envoy-upstream-service-time: 26
header: server: envoy
DEBUG:urllib3.connectionpool:http://reviews:9080 "GET /reviews/0 HTTP/1.1" 200 295
INFO:werkzeug:127.0.0.6 - - [25/May/2022 08:57:40] "GET /productpage HTTP/1.1" 200 -
```



```
[root@node01 ~]# kubectl logs -n istio productpage-v1-7cf98697f6-8wtcz -f --tail 10
reply: 'HTTP/1.1 200 OK\r\n'
header: x-powered-by: Servlet/3.1
header: content-type: application/json
header: date: Wed, 25 May 2022 08:57:40 GMT
header: content-language: en-US
header: content-length: 375
header: x-envoy-upstream-service-time: 31
header: server: envoy
DEBUG:urllib3.connectionpool:http://reviews:9080 "GET /reviews/0 HTTP/1.1" 200 375
INFO:werkzeug:127.0.0.6 - - [25/May/2022 08:57:40] "GET /productpage HTTP/1.1" 200 -



DEBUG:urllib3.connectionpool:Starting new HTTP connection (1): details:9080
send: b'GET /details/0 HTTP/1.1\r\nHost: details:9080\r\nuser-agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/99.0.4844.82 Safari/537.36\r\nAccept-Encoding: gzip, deflate\r\nAccept: */*\r\nConnection: keep-alive\r\nX-B3-TraceId: 4ba5acc30b4070b178f8ddad826c732c\r\nX-B3-SpanId: 394c1913af3bac4b\r\nX-B3-ParentSpanId: 78f8ddad826c732c\r\nX-B3-Sampled: 1\r\nx-request-id: 5a6921af-f4dc-9697-85db-cc928948ba79\r\n\r\n'
reply: 'HTTP/1.1 200 OK\r\n'
header: content-type: application/json
header: server: envoy
header: date: Wed, 25 May 2022 08:58:54 GMT
header: content-length: 178
header: x-envoy-upstream-service-time: 2
DEBUG:urllib3.connectionpool:http://details:9080 "GET /details/0 HTTP/1.1" 200 178
DEBUG:urllib3.connectionpool:Starting new HTTP connection (1): reviews:9080
send: b'GET /reviews/0 HTTP/1.1\r\nHost: reviews:9080\r\nuser-agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/99.0.4844.82 Safari/537.36\r\nAccept-Encoding: gzip, deflate\r\nAccept: */*\r\nConnection: keep-alive\r\nX-B3-TraceId: 4ba5acc30b4070b178f8ddad826c732c\r\nX-B3-SpanId: 394c1913af3bac4b\r\nX-B3-ParentSpanId: 78f8ddad826c732c\r\nX-B3-Sampled: 1\r\nx-request-id: 5a6921af-f4dc-9697-85db-cc928948ba79\r\n\r\n'
reply: 'HTTP/1.1 200 OK\r\n'
header: x-powered-by: Servlet/3.1
header: content-type: application/json
header: date: Wed, 25 May 2022 08:58:54 GMT
header: content-language: en-US
header: content-length: 379
header: x-envoy-upstream-service-time: 58
header: server: envoy
DEBUG:urllib3.connectionpool:http://reviews:9080 "GET /reviews/0 HTTP/1.1" 200 379
INFO:werkzeug:127.0.0.6 - - [25/May/2022 08:58:54] "GET /productpage HTTP/1.1" 200 -
```





![1653469220(1)](images\1653469220(1).jpg)



##### requestHeaderModifier

###### add

httproute/bookinfo-httproute-system-rules-filters-requestHeaderModifier-add.yaml

kubectl apply -f bookinfo-httproute-system-rules-filters-requrequestHeaderModifierestMirror-add.yaml -n istio

```
apiVersion: gateway.networking.k8s.io/v1alpha2
kind: HTTPRoute
metadata:
  name: bookinfo
spec:
  parentRefs:
  - name: bookinfo
    namespace: istio-system
    group: gateway.networking.k8s.io
    kind: Gateway
  rules:
  - matches:
    - path: 
        type: PathPrefix
        value: /
    filters:
    - requestHeaderModifier:
        add:
        - name: test
          value: test
      type: RequestHeaderModifier
    backendRefs:
    - name: productpage
      port: 9080
```

httproute/gw-bookinfo.yaml

kubectl apply -f gw-bookinfo.yaml -n istio-system

```
apiVersion: gateway.networking.k8s.io/v1alpha2
kind: Gateway
metadata:
  name: bookinfo
spec:
  addresses:
  - value: istio-ingressgateway.istio-system.svc.cluster.local
    type: Hostname
  gatewayClassName: istio
  listeners:  
  - protocol: HTTP
    port: 80
    name: bookinfo-gw
    allowedRoutes:
      namespaces:
        from: All
```

gatewayclass/istio-gc.yaml

kubectl apply -f istio-gc.yaml -n istio-system

```
apiVersion: gateway.networking.k8s.io/v1alpha2
kind: GatewayClass
metadata:
  name: istio
spec:
  controllerName: istio.io/gateway-controller
```

清理：

kubectl delete GatewayClass istio -n istio-system

kubectl delete gateways.gateway.networking.k8s.io  -n istio-system bookinfo 

kubectl delete httproutes.gateway.networking.k8s.io -n istio bookinfo



![1653533752(1)](images\1653533752(1).jpg)

###### remove

httproute/bookinfo-httproute-system-rules-filters-requestHeaderModifier-remove.yaml

kubectl apply -f bookinfo-httproute-system-rules-filters-requrequestHeaderModifierestMirror-remove.yaml -n istio

```
apiVersion: gateway.networking.k8s.io/v1alpha2
kind: HTTPRoute
metadata:
  name: bookinfo
spec:
  parentRefs:
  - name: bookinfo
    namespace: istio-system
    group: gateway.networking.k8s.io
    kind: Gateway
  rules:
  - matches:
    - path: 
        type: PathPrefix
        value: /
    filters:
    - requestHeaderModifier:
        remove:
        - test1
        - test2
      type: RequestHeaderModifier
    backendRefs:
    - name: productpage
      port: 9080
```

httproute/gw-bookinfo.yaml

kubectl apply -f gw-bookinfo.yaml -n istio-system

```
apiVersion: gateway.networking.k8s.io/v1alpha2
kind: Gateway
metadata:
  name: bookinfo
spec:
  addresses:
  - value: istio-ingressgateway.istio-system.svc.cluster.local
    type: Hostname
  gatewayClassName: istio
  listeners:  
  - protocol: HTTP
    port: 80
    name: bookinfo-gw
    allowedRoutes:
      namespaces:
        from: All
```

gatewayclass/istio-gc.yaml

kubectl apply -f istio-gc.yaml -n istio-system

```
apiVersion: gateway.networking.k8s.io/v1alpha2
kind: GatewayClass
metadata:
  name: istio
spec:
  controllerName: istio.io/gateway-controller
```

清理：

kubectl delete GatewayClass istio -n istio-system

kubectl delete gateways.gateway.networking.k8s.io  -n istio-system bookinfo 

kubectl delete httproutes.gateway.networking.k8s.io -n istio bookinfo



![1653533885(1)](images\1653533885(1).jpg)

###### set

httproute/bookinfo-httproute-system-rules-filters-requestHeaderModifier-set.yaml

kubectl apply -f bookinfo-httproute-system-rules-filters-requrequestHeaderModifierestMirror-set.yaml -n istio

```
apiVersion: gateway.networking.k8s.io/v1alpha2
kind: HTTPRoute
metadata:
  name: bookinfo
spec:
  parentRefs:
  - name: bookinfo
    namespace: istio-system
    group: gateway.networking.k8s.io
    kind: Gateway
  rules:
  - matches:
    - path: 
        type: PathPrefix
        value: /
    filters:
    - requestHeaderModifier:
        set:
        - name: test1
          value: value1
        - name: test2
          value: value2
      type: RequestHeaderModifier
    backendRefs:
    - name: productpage
      port: 9080
```

httproute/gw-bookinfo.yaml

kubectl apply -f gw-bookinfo.yaml -n istio-system

```
apiVersion: gateway.networking.k8s.io/v1alpha2
kind: Gateway
metadata:
  name: bookinfo
spec:
  addresses:
  - value: istio-ingressgateway.istio-system.svc.cluster.local
    type: Hostname
  gatewayClassName: istio
  listeners:  
  - protocol: HTTP
    port: 80
    name: bookinfo-gw
    allowedRoutes:
      namespaces:
        from: All
```

gatewayclass/istio-gc.yaml

kubectl apply -f istio-gc.yaml -n istio-system

```
apiVersion: gateway.networking.k8s.io/v1alpha2
kind: GatewayClass
metadata:
  name: istio
spec:
  controllerName: istio.io/gateway-controller
```

清理：

kubectl delete GatewayClass istio -n istio-system

kubectl delete gateways.gateway.networking.k8s.io  -n istio-system bookinfo 

kubectl delete httproutes.gateway.networking.k8s.io -n istio bookinfo



![1653534001(1)](images\1653534001(1).jpg)

##### extensionRef

略



#### backendRefs

##### name,port

略



##### weight

httproute/bookinfo-httproute-system-rules-backendRefs-weight.yaml

kubectl apply -f bookinfo-httproute-system-rules-backendRefs-weight.yaml -n istio

```
apiVersion: gateway.networking.k8s.io/v1alpha2
kind: HTTPRoute
metadata:
  name: bookinfo
spec:
  parentRefs:
  - name: bookinfo
    namespace: istio-system
    group: gateway.networking.k8s.io
    kind: Gateway
  rules:
  - matches:
    - path: 
        type: PathPrefix
        value: /
    backendRefs:
    - name: productpage
      port: 9080
      weight: 50
    - name: productpage-v2
      port: 9080
      weight: 50      
```

deploy-productpage-v2.yaml

kubectl apply -f deploy-productpage-v2.yaml -n istio

```
apiVersion: v1
kind: Service
metadata:
  name: productpage-v2
  labels:
    app: productpage-v2
    service: productpage-v2
spec:
  ports:
  - port: 9080
    name: http
  selector:
    app: productpage-v2
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: productpage-v2
  labels:
    app: productpage-v2
    version: v2
spec:
  replicas: 1
  selector:
    matchLabels:
      app: productpage-v2
      version: v2
  template:
    metadata:
      labels:
        app: productpage-v2
        version: v2
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
---
```





httproute/gw-bookinfo.yaml

kubectl apply -f gw-bookinfo.yaml -n istio-system

```
apiVersion: gateway.networking.k8s.io/v1alpha2
kind: Gateway
metadata:
  name: bookinfo
spec:
  addresses:
  - value: istio-ingressgateway.istio-system.svc.cluster.local
    type: Hostname
  gatewayClassName: istio
  listeners:  
  - protocol: HTTP
    port: 80
    name: bookinfo-gw
    allowedRoutes:
      namespaces:
        from: All
```

gatewayclass/istio-gc.yaml

kubectl apply -f istio-gc.yaml -n istio-system

```
apiVersion: gateway.networking.k8s.io/v1alpha2
kind: GatewayClass
metadata:
  name: istio
spec:
  controllerName: istio.io/gateway-controller
```

清理：

kubectl delete GatewayClass istio -n istio-system

kubectl delete gateways.gateway.networking.k8s.io  -n istio-system bookinfo 

kubectl delete httproutes.gateway.networking.k8s.io -n istio bookinfo

kubectl delete -f deploy-productpage-v2.yaml -n istio



##### filters

###### requestRedirect

**unsupported filter type "RequestRedirect"**

hostname,port,scheme

httproute/bookinfo-httproute-system-rules-backendRefs-filters-requestRedirect.yaml

kubectl apply -f bookinfo-httproute-system-rules-backendRefs-filters-requestRedirect.yaml -n istio

```
apiVersion: gateway.networking.k8s.io/v1alpha2
kind: HTTPRoute
metadata:
  name: bookinfo
spec:
  parentRefs:
  - name: bookinfo
    namespace: istio-system
    group: gateway.networking.k8s.io
    kind: Gateway
    sectionName: bookinfo-gw
  rules:
  - matches:
    - path: 
        type: PathPrefix
        value: /
    backendRefs:
    - name: productpage
      port: 9080
      filters:
      - requestRedirect:
          hostname: bookinfo.com
          port: 32388
          scheme: https
        type: RequestRedirect
---
apiVersion: gateway.networking.k8s.io/v1alpha2
kind: HTTPRoute
metadata:
  name: bookinfo-https
spec:
  parentRefs:
  - name: bookinfo
    namespace: istio-system
    group: gateway.networking.k8s.io
    kind: Gateway
    sectionName: bookinfo-gw-https
  rules:
  - matches:
    - path: 
        type: PathPrefix
        value: /
    backendRefs:
    - name: productpage
      port: 9080
```



 openssl req -x509 -sha256 -nodes -days 365 -newkey rsa:2048  -keyout cert.key -out cert.crt  -config  openssl.conf

创建secret

kubectl create -n istio-system secret tls bookinfo-gateway-secret  --key ./cert.key --cert=./cert.crt



httproute/gw-bookinfo-http-https.yaml

kubectl apply -f gw-bookinfo-http-https.yaml -n istio-system

```
apiVersion: gateway.networking.k8s.io/v1alpha2
kind: Gateway
metadata:
  name: bookinfo
spec:
  addresses:
  - value: istio-ingressgateway.istio-system.svc.cluster.local
    type: Hostname
  gatewayClassName: istio
  listeners:  
  - protocol: HTTP
    port: 80
    name: bookinfo-gw
    allowedRoutes:
      namespaces:
        from: All
  - protocol: HTTPS
    port: 443
    name: bookinfo-gw-https
    allowedRoutes:
      namespaces:
        from: All
    tls:
      options: {}
      certificateRefs:
      - name: bookinfo-gateway-secret
        kind: Secret
```

gatewayclass/istio-gc.yaml

kubectl apply -f istio-gc.yaml -n istio-system

```
apiVersion: gateway.networking.k8s.io/v1alpha2
kind: GatewayClass
metadata:
  name: istio
spec:
  controllerName: istio.io/gateway-controller
```

清理：

kubectl delete GatewayClass istio -n istio-system

kubectl delete gateways.gateway.networking.k8s.io  -n istio-system bookinfo 

kubectl delete httproutes.gateway.networking.k8s.io -n istio bookinfo

kubectl delete httproutes.gateway.networking.k8s.io -n istio bookinfo-https

kubectl delete -n istio-system secret  bookinfo-gateway-secret







statusCode

略



###### requestMirror

**unsupported filter type "RequestMirror"**

httproute/bookinfo-httproute-system-rules-backendRefs-filters-requestMirror.yaml

kubectl apply -f bookinfo-httproute-system-rules-backendRefs-filters-requestMirror.yaml -n istio

```
apiVersion: gateway.networking.k8s.io/v1alpha2
kind: HTTPRoute
metadata:
  name: bookinfo
spec:
  parentRefs:
  - name: bookinfo
    namespace: istio-system
    group: gateway.networking.k8s.io
    kind: Gateway
  rules:
  - matches:
    - path: 
        type: PathPrefix
        value: /
    backendRefs:
    - name: productpage
      port: 9080
      filters:
      - requestMirror:
          backendRef:
            name: productpage-v2
            port: 9080
        type: RequestMirror
```

deploy-productpage-v2.yaml

kubectl apply -f deploy-productpage-v2.yaml -n istio

```
apiVersion: v1
kind: Service
metadata:
  name: productpage-v2
  labels:
    app: productpage-v2
    service: productpage-v2
spec:
  ports:
  - port: 9080
    name: http
  selector:
    app: productpage-v2
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: productpage-v2
  labels:
    app: productpage-v2
    version: v2
spec:
  replicas: 1
  selector:
    matchLabels:
      app: productpage-v2
      version: v2
  template:
    metadata:
      labels:
        app: productpage-v2
        version: v2
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
---
```

httproute/gw-bookinfo.yaml

kubectl apply -f gw-bookinfo.yaml -n istio-system

```
apiVersion: gateway.networking.k8s.io/v1alpha2
kind: Gateway
metadata:
  name: bookinfo
spec:
  addresses:
  - value: istio-ingressgateway.istio-system.svc.cluster.local
    type: Hostname
  gatewayClassName: istio
  listeners:  
  - protocol: HTTP
    port: 80
    name: bookinfo-gw
    allowedRoutes:
      namespaces:
        from: All
```

gatewayclass/istio-gc.yaml

kubectl apply -f istio-gc.yaml -n istio-system

```
apiVersion: gateway.networking.k8s.io/v1alpha2
kind: GatewayClass
metadata:
  name: istio
spec:
  controllerName: istio.io/gateway-controller
```

清理：

kubectl delete GatewayClass istio -n istio-system

kubectl delete gateways.gateway.networking.k8s.io  -n istio-system bookinfo 

kubectl delete httproutes.gateway.networking.k8s.io -n istio bookinfo

kubectl delete -f deploy-productpage-v2.yaml -n istio







###### requestHeaderModifier

add

httproute/bookinfo-httproute-system-rules-backendRefs-filters-requestHeaderModifier-add.yaml

kubectl apply -f bookinfo-httproute-system-rules-backendRefs-filters-requrequestHeaderModifierestMirror-add.yaml -n istio

```
apiVersion: gateway.networking.k8s.io/v1alpha2
kind: HTTPRoute
metadata:
  name: bookinfo
spec:
  parentRefs:
  - name: bookinfo
    namespace: istio-system
    group: gateway.networking.k8s.io
    kind: Gateway
  rules:
  - matches:
    - path: 
        type: PathPrefix
        value: /
    backendRefs:
    - name: productpage
      port: 9080
      filters:
      - requestHeaderModifier:
          add:
          - name: test
            value: test
        type: RequestHeaderModifier
```

httproute/gw-bookinfo.yaml

kubectl apply -f gw-bookinfo.yaml -n istio-system

```
apiVersion: gateway.networking.k8s.io/v1alpha2
kind: Gateway
metadata:
  name: bookinfo
spec:
  addresses:
  - value: istio-ingressgateway.istio-system.svc.cluster.local
    type: Hostname
  gatewayClassName: istio
  listeners:  
  - protocol: HTTP
    port: 80
    name: bookinfo-gw
    allowedRoutes:
      namespaces:
        from: All
```

gatewayclass/istio-gc.yaml

kubectl apply -f istio-gc.yaml -n istio-system

```
apiVersion: gateway.networking.k8s.io/v1alpha2
kind: GatewayClass
metadata:
  name: istio
spec:
  controllerName: istio.io/gateway-controller
```

清理：

kubectl delete GatewayClass istio -n istio-system

kubectl delete gateways.gateway.networking.k8s.io  -n istio-system bookinfo 

kubectl delete httproutes.gateway.networking.k8s.io -n istio bookinfo

![1653615940(1)](images\1653615940(1).jpg)

remove

httproute/bookinfo-httproute-system-rules-backendRefs-filters-requestHeaderModifier-remove.yaml

kubectl apply -f bookinfo-httproute-system-rules-backendRefs-filters-requrequestHeaderModifierestMirror-remove.yaml -n istio

```
apiVersion: gateway.networking.k8s.io/v1alpha2
kind: HTTPRoute
metadata:
  name: bookinfo
spec:
  parentRefs:
  - name: bookinfo
    namespace: istio-system
    group: gateway.networking.k8s.io
    kind: Gateway
  rules:
  - matches:
    - path: 
        type: PathPrefix
        value: /
    backendRefs:
    - name: productpage
      port: 9080
      filters:
      - requestHeaderModifier:
          remove:
          - test1
          - test2
        type: RequestHeaderModifier
```

httproute/gw-bookinfo.yaml

kubectl apply -f gw-bookinfo.yaml -n istio-system

```
apiVersion: gateway.networking.k8s.io/v1alpha2
kind: Gateway
metadata:
  name: bookinfo
spec:
  addresses:
  - value: istio-ingressgateway.istio-system.svc.cluster.local
    type: Hostname
  gatewayClassName: istio
  listeners:  
  - protocol: HTTP
    port: 80
    name: bookinfo-gw
    allowedRoutes:
      namespaces:
        from: All
```

gatewayclass/istio-gc.yaml

kubectl apply -f istio-gc.yaml -n istio-system

```
apiVersion: gateway.networking.k8s.io/v1alpha2
kind: GatewayClass
metadata:
  name: istio
spec:
  controllerName: istio.io/gateway-controller
```

清理：

kubectl delete GatewayClass istio -n istio-system

kubectl delete gateways.gateway.networking.k8s.io  -n istio-system bookinfo 

kubectl delete httproutes.gateway.networking.k8s.io -n istio bookinfo



![1653616052(1)](images\1653616052(1).jpg)



set

httproute/bookinfo-httproute-system-rules-backendRefs-filters-requestHeaderModifier-set.yaml

kubectl apply -f bookinfo-httproute-system-rules-backendRefs-filters-requrequestHeaderModifierestMirror-set.yaml -n istio

```
apiVersion: gateway.networking.k8s.io/v1alpha2
kind: HTTPRoute
metadata:
  name: bookinfo
spec:
  parentRefs:
  - name: bookinfo
    namespace: istio-system
    group: gateway.networking.k8s.io
    kind: Gateway
  rules:
  - matches:
    - path: 
        type: PathPrefix
        value: /
    backendRefs:
    - name: productpage
      port: 9080
      filters:
      - requestHeaderModifier:
          set:
          - name: test1
            value: value1
          - name: test2
            value: value2
        type: RequestHeaderModifier
```

httproute/gw-bookinfo.yaml

kubectl apply -f gw-bookinfo.yaml -n istio-system

```
apiVersion: gateway.networking.k8s.io/v1alpha2
kind: Gateway
metadata:
  name: bookinfo
spec:
  addresses:
  - value: istio-ingressgateway.istio-system.svc.cluster.local
    type: Hostname
  gatewayClassName: istio
  listeners:  
  - protocol: HTTP
    port: 80
    name: bookinfo-gw
    allowedRoutes:
      namespaces:
        from: All
```

gatewayclass/istio-gc.yaml

kubectl apply -f istio-gc.yaml -n istio-system

```
apiVersion: gateway.networking.k8s.io/v1alpha2
kind: GatewayClass
metadata:
  name: istio
spec:
  controllerName: istio.io/gateway-controller
```

清理：

kubectl delete GatewayClass istio -n istio-system

kubectl delete gateways.gateway.networking.k8s.io  -n istio-system bookinfo 

kubectl delete httproutes.gateway.networking.k8s.io -n istio bookinfo

![1653616173(1)](images\1653616173(1).jpg)



###### extensionRef



##### group，kind

httproute/bookinfo-httproute-system-rules-backendRefs-group-kind.yaml

kubectl apply -f bookinfo-httproute-system-rules-backendRefs-group-kind.yaml -n istio

```
apiVersion: gateway.networking.k8s.io/v1alpha2
kind: HTTPRoute
metadata:
  name: bookinfo
spec:
  parentRefs:
  - name: bookinfo
    namespace: istio-system
    group: gateway.networking.k8s.io
    kind: Gateway
  rules:
  - matches:
    - path: 
        type: PathPrefix
        value: /
    backendRefs:
    - name: productpage
      group: ""
      kind: Service
      port: 9080
```



httproute/gw-bookinfo.yaml

kubectl apply -f gw-bookinfo.yaml -n istio-system

```
apiVersion: gateway.networking.k8s.io/v1alpha2
kind: Gateway
metadata:
  name: bookinfo
spec:
  addresses:
  - value: istio-ingressgateway.istio-system.svc.cluster.local
    type: Hostname
  gatewayClassName: istio
  listeners:  
  - protocol: HTTP
    port: 80
    name: bookinfo-gw
    allowedRoutes:
      namespaces:
        from: All
```

gatewayclass/istio-gc.yaml

kubectl apply -f istio-gc.yaml -n istio-system

```
apiVersion: gateway.networking.k8s.io/v1alpha2
kind: GatewayClass
metadata:
  name: istio
spec:
  controllerName: istio.io/gateway-controller
```

清理：

kubectl delete GatewayClass istio -n istio-system

kubectl delete gateways.gateway.networking.k8s.io  -n istio-system bookinfo 

kubectl delete httproutes.gateway.networking.k8s.io -n istio bookinfo



##### namespace

httproute/bookinfo-httproute-system-rules-backendRefs-namespace.yaml

kubectl apply -f bookinfo-httproute-system-rules-backendRefs-namespace.yaml -n istio-system

```
apiVersion: gateway.networking.k8s.io/v1alpha2
kind: HTTPRoute
metadata:
  name: bookinfo
spec:
  parentRefs:
  - name: bookinfo
    namespace: istio-system
    group: gateway.networking.k8s.io
    kind: Gateway
  rules:
  - matches:
    - path: 
        type: PathPrefix
        value: /
    backendRefs:
    - name: productpage
      group: ""
      kind: Service
      port: 9080
      namespace: istio
```





httproute/gw-bookinfo.yaml

kubectl apply -f gw-bookinfo.yaml -n istio-system

```
apiVersion: gateway.networking.k8s.io/v1alpha2
kind: Gateway
metadata:
  name: bookinfo
spec:
  addresses:
  - value: istio-ingressgateway.istio-system.svc.cluster.local
    type: Hostname
  gatewayClassName: istio
  listeners:  
  - protocol: HTTP
    port: 80
    name: bookinfo-gw
    allowedRoutes:
      namespaces:
        from: All
```

gatewayclass/istio-gc.yaml

kubectl apply -f istio-gc.yaml -n istio-system

```
apiVersion: gateway.networking.k8s.io/v1alpha2
kind: GatewayClass
metadata:
  name: istio
spec:
  controllerName: istio.io/gateway-controller
```

清理：

kubectl delete GatewayClass istio -n istio-system

kubectl delete gateways.gateway.networking.k8s.io  -n istio-system bookinfo 

kubectl delete httproutes.gateway.networking.k8s.io -n istio-system bookinfo





## TLSRoute

### rules

#### backendRefs

##### name,port

1创建证书

 openssl req -x509 -sha256 -nodes -days 365 -newkey rsa:2048 -subj '/O=example Inc./CN=example.com' -keyout example.com.key -out example.com.crt 

 openssl req -out nginx.example.com.csr -newkey rsa:2048 -nodes -keyout nginx.example.com.key -subj "/CN=nginx.example.com/O=some organization" 

 openssl x509 -req -days 365 -CA example.com.crt -CAkey example.com.key -set_serial 0 -in nginx.example.com.csr -out nginx.example.com.crt 

2创建secret

 kubectl create secret tls nginx-server-certs --key nginx.example.com.key --cert nginx.example.com.crt  -n istio

3创建nginx配置文件

tlsroute/nginx.conf

```
events {
}

http {
  log_format main '$remote_addr - $remote_user [$time_local]  $status '
  '"$request" $body_bytes_sent "$http_referer" '
  '"$http_user_agent" "$http_x_forwarded_for"';
  access_log /var/log/nginx/access.log main;
  error_log  /var/log/nginx/error.log;

  server {
    listen 443 ssl;

    root /usr/share/nginx/html;
    index index.html;

    server_name nginx.example.com;
    ssl_certificate /etc/nginx-server-certs/tls.crt;
    ssl_certificate_key /etc/nginx-server-certs/tls.key;
  }
}
```

 kubectl create configmap nginx-configmap --from-file=nginx.conf=./nginx.conf  -nistio

4创建deploy

tlsroute/nginx-deploy.yaml

kubectl apply -f nginx-deploy.yaml -n istio

```
apiVersion: v1
kind: Service
metadata:
  name: my-nginx
  labels:
    run: my-nginx
spec:
  ports:
  - port: 443
    protocol: TCP
  selector:
    run: my-nginx
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: my-nginx
spec:
  selector:
    matchLabels:
      run: my-nginx
  replicas: 1
  template:
    metadata:
      labels:
        run: my-nginx
    spec:
      containers:
      - name: my-nginx
        image: nginx
        ports:
        - containerPort: 443
        volumeMounts:
        - name: nginx-config
          mountPath: /etc/nginx
          readOnly: true
        - name: nginx-server-certs
          mountPath: /etc/nginx-server-certs
          readOnly: true
      volumes:
      - name: nginx-config
        configMap:
          name: nginx-configmap
      - name: nginx-server-certs
        secret:
          secretName: nginx-server-certs
```



tlsroute/gw-listeners-tls.yaml

kubectl apply -f gw-listeners-tls.yaml -n istio-system

```
kind: Gateway
apiVersion: gateway.networking.k8s.io/v1alpha2
metadata:
  name: bookinfo
spec:
  addresses:
  - value: istio-ingressgateway.istio-system.svc.cluster.local
    type: Hostname
  gatewayClassName: istio
  listeners:  
  - hostname: "nginx.example.com"
    port: 443
    name: gw-bookinfo
    protocol: TLS
    tls:
      mode: Passthrough
    allowedRoutes:
      namespaces:
        from: All
```

gatewayclass/isito-gc.yaml

kubectl apply -f isito-gc.yaml -n istio-system

```
apiVersion: networking.x-k8s.io/v1alpha1
kind: GatewayClass
metadata:
  name: istio
spec:
  controller: istio.io/gateway-controller
```

tlsroute/nginx-tls-rules-backendRefs-nameport.yaml

kubectl apply -f nginx-tls-rules-backendRefs-nameport.yaml -n istio

```
apiVersion: gateway.networking.k8s.io/v1alpha2
kind: TLSRoute
metadata:
  name: bookinfo
spec:
  parentRefs:
  - name: bookinfo
    namespace: istio-system
    kind: Gateway
  rules:
  - backendRefs:
    - name: my-nginx
      port: 443
```

访问

清理：

kubectl delete GatewayClass istio -n istio-system

kubectl delete gateways.gateway.networking.k8s.io  -n istio-system bookinfo 

kubectl delete tlsroutes.gateway.networking.k8s.io -n istio bookinfo

kubectl delete -f nginx-deploy.yaml -n istio

 kubectl delete configmap nginx-configmap -n istio

kubectl delete secret nginx-server-certs   -n istio



https://nginx.example.com:32388/



##### group,kind

1创建证书

 openssl req -x509 -sha256 -nodes -days 365 -newkey rsa:2048 -subj '/O=example Inc./CN=example.com' -keyout example.com.key -out example.com.crt 

 openssl req -out nginx.example.com.csr -newkey rsa:2048 -nodes -keyout nginx.example.com.key -subj "/CN=nginx.example.com/O=some organization" 

 openssl x509 -req -days 365 -CA example.com.crt -CAkey example.com.key -set_serial 0 -in nginx.example.com.csr -out nginx.example.com.crt 

2创建secret

 kubectl create secret tls nginx-server-certs --key nginx.example.com.key --cert nginx.example.com.crt  -n istio

3创建nginx配置文件

tlsroute/nginx.conf

```
events {
}

http {
  log_format main '$remote_addr - $remote_user [$time_local]  $status '
  '"$request" $body_bytes_sent "$http_referer" '
  '"$http_user_agent" "$http_x_forwarded_for"';
  access_log /var/log/nginx/access.log main;
  error_log  /var/log/nginx/error.log;

  server {
    listen 443 ssl;

    root /usr/share/nginx/html;
    index index.html;

    server_name nginx.example.com;
    ssl_certificate /etc/nginx-server-certs/tls.crt;
    ssl_certificate_key /etc/nginx-server-certs/tls.key;
  }
}
```

 kubectl create configmap nginx-configmap --from-file=nginx.conf=./nginx.conf  -nistio

4创建deploy

tlsroute/nginx-deploy.yaml

kubectl apply -f nginx-deploy.yaml -n istio

```
apiVersion: v1
kind: Service
metadata:
  name: my-nginx
  labels:
    run: my-nginx
spec:
  ports:
  - port: 443
    protocol: TCP
  selector:
    run: my-nginx
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: my-nginx
spec:
  selector:
    matchLabels:
      run: my-nginx
  replicas: 1
  template:
    metadata:
      labels:
        run: my-nginx
    spec:
      containers:
      - name: my-nginx
        image: nginx
        ports:
        - containerPort: 443
        volumeMounts:
        - name: nginx-config
          mountPath: /etc/nginx
          readOnly: true
        - name: nginx-server-certs
          mountPath: /etc/nginx-server-certs
          readOnly: true
      volumes:
      - name: nginx-config
        configMap:
          name: nginx-configmap
      - name: nginx-server-certs
        secret:
          secretName: nginx-server-certs
```



tlsroute/gw-listeners-tls.yaml

kubectl apply -f gw-listeners-tls.yaml -n istio-system

```
kind: Gateway
apiVersion: gateway.networking.k8s.io/v1alpha2
metadata:
  name: bookinfo
spec:
  addresses:
  - value: istio-ingressgateway.istio-system.svc.cluster.local
    type: Hostname
  gatewayClassName: istio
  listeners:  
  - hostname: "nginx.example.com"
    port: 443
    name: gw-bookinfo
    protocol: TLS
    tls:
      mode: Passthrough
    allowedRoutes:
      namespaces:
        from: All
```

gatewayclass/isito-gc.yaml

kubectl apply -f isito-gc.yaml -n istio-system

```
apiVersion: networking.x-k8s.io/v1alpha1
kind: GatewayClass
metadata:
  name: istio
spec:
  controller: istio.io/gateway-controller
```

tlsroute/nginx-tls-rules-backendRefs-groupkind.yaml

kubectl apply -f nginx-tls-rules-backendRefs-groupkind.yaml -n istio

```
apiVersion: gateway.networking.k8s.io/v1alpha2
kind: TLSRoute
metadata:
  name: bookinfo
spec:
  parentRefs:
  - name: bookinfo
    namespace: istio-system
    kind: Gateway
  rules:
  - backendRefs:
    - name: my-nginx
      port: 443
      group: ""
      kind: Service
```

访问

清理：

kubectl delete GatewayClass istio -n istio-system

kubectl delete gateways.gateway.networking.k8s.io  -n istio-system bookinfo 

kubectl delete tlsroutes.gateway.networking.k8s.io -n istio bookinfo

kubectl delete -f nginx-deploy.yaml -n istio

 kubectl delete configmap nginx-configmap -n istio

kubectl delete secret nginx-server-certs   -n istio



https://nginx.example.com:32388/





##### namespace

1创建证书

 openssl req -x509 -sha256 -nodes -days 365 -newkey rsa:2048 -subj '/O=example Inc./CN=example.com' -keyout example.com.key -out example.com.crt 

 openssl req -out nginx.example.com.csr -newkey rsa:2048 -nodes -keyout nginx.example.com.key -subj "/CN=nginx.example.com/O=some organization" 

 openssl x509 -req -days 365 -CA example.com.crt -CAkey example.com.key -set_serial 0 -in nginx.example.com.csr -out nginx.example.com.crt 

2创建secret

 kubectl create secret tls nginx-server-certs --key nginx.example.com.key --cert nginx.example.com.crt  -n istio

3创建nginx配置文件

tlsroute/nginx.conf

```
events {
}

http {
  log_format main '$remote_addr - $remote_user [$time_local]  $status '
  '"$request" $body_bytes_sent "$http_referer" '
  '"$http_user_agent" "$http_x_forwarded_for"';
  access_log /var/log/nginx/access.log main;
  error_log  /var/log/nginx/error.log;

  server {
    listen 443 ssl;

    root /usr/share/nginx/html;
    index index.html;

    server_name nginx.example.com;
    ssl_certificate /etc/nginx-server-certs/tls.crt;
    ssl_certificate_key /etc/nginx-server-certs/tls.key;
  }
}
```

 kubectl create configmap nginx-configmap --from-file=nginx.conf=./nginx.conf  -nistio

4创建deploy

tlsroute/nginx-deploy.yaml

kubectl apply -f nginx-deploy.yaml -n istio

```
apiVersion: v1
kind: Service
metadata:
  name: my-nginx
  labels:
    run: my-nginx
spec:
  ports:
  - port: 443
    protocol: TCP
  selector:
    run: my-nginx
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: my-nginx
spec:
  selector:
    matchLabels:
      run: my-nginx
  replicas: 1
  template:
    metadata:
      labels:
        run: my-nginx
    spec:
      containers:
      - name: my-nginx
        image: nginx
        ports:
        - containerPort: 443
        volumeMounts:
        - name: nginx-config
          mountPath: /etc/nginx
          readOnly: true
        - name: nginx-server-certs
          mountPath: /etc/nginx-server-certs
          readOnly: true
      volumes:
      - name: nginx-config
        configMap:
          name: nginx-configmap
      - name: nginx-server-certs
        secret:
          secretName: nginx-server-certs
```



tlsroute/gw-listeners-tls.yaml

kubectl apply -f gw-listeners-tls.yaml -n istio-system

```
kind: Gateway
apiVersion: gateway.networking.k8s.io/v1alpha2
metadata:
  name: bookinfo
spec:
  addresses:
  - value: istio-ingressgateway.istio-system.svc.cluster.local
    type: Hostname
  gatewayClassName: istio
  listeners:  
  - hostname: "nginx.example.com"
    port: 443
    name: gw-bookinfo
    protocol: TLS
    tls:
      mode: Passthrough
    allowedRoutes:
      namespaces:
        from: All
```

gatewayclass/isito-gc.yaml

kubectl apply -f isito-gc.yaml -n istio-system

```
apiVersion: networking.x-k8s.io/v1alpha1
kind: GatewayClass
metadata:
  name: istio
spec:
  controller: istio.io/gateway-controller
```

tlsroute/nginx-tls-rules-backendRefs-namespace.yaml

kubectl apply -f nginx-tls-rules-backendRefs-namespace.yaml -n istio-system

```
apiVersion: gateway.networking.k8s.io/v1alpha2
kind: TLSRoute
metadata:
  name: bookinfo
spec:
  parentRefs:
  - name: bookinfo
    namespace: istio-system
    kind: Gateway
  rules:
  - backendRefs:
    - name: my-nginx
      port: 443
      group: ""
      kind: Service
      namespace: istio
```

访问

清理：

kubectl delete GatewayClass istio -n istio-system

kubectl delete gateways.gateway.networking.k8s.io  -n istio-system bookinfo 

kubectl delete tlsroutes.gateway.networking.k8s.io -n istio-system bookinfo

kubectl delete -f nginx-deploy.yaml -n istio

 kubectl delete configmap nginx-configmap -n istio

kubectl delete secret nginx-server-certs   -n istio



https://nginx.example.com:32388/





##### weight

**按比例路由貌似有点问题，一定时期会只路由一个服务**

1创建证书

 openssl req -x509 -sha256 -nodes -days 365 -newkey rsa:2048 -subj '/O=example Inc./CN=example.com' -keyout example.com.key -out example.com.crt 

 openssl req -out nginx.example.com.csr -newkey rsa:2048 -nodes -keyout nginx.example.com.key -subj "/CN=nginx.example.com/O=some organization" 

 openssl x509 -req -days 365 -CA example.com.crt -CAkey example.com.key -set_serial 0 -in nginx.example.com.csr -out nginx.example.com.crt 

2创建secret

 kubectl create secret tls nginx-server-certs --key nginx.example.com.key --cert nginx.example.com.crt  -n istio

3创建nginx配置文件

tlsroute/nginx.conf

```
events {
}

http {
  log_format main '$remote_addr - $remote_user [$time_local]  $status '
  '"$request" $body_bytes_sent "$http_referer" '
  '"$http_user_agent" "$http_x_forwarded_for"';
  access_log /var/log/nginx/access.log main;
  error_log  /var/log/nginx/error.log;

  server {
    listen 443 ssl;

    root /usr/share/nginx/html;
    index index.html;

    server_name nginx.example.com;
    ssl_certificate /etc/nginx-server-certs/tls.crt;
    ssl_certificate_key /etc/nginx-server-certs/tls.key;
  }
}
```

 kubectl create configmap nginx-configmap --from-file=nginx.conf=./nginx.conf  -nistio

4创建deploy

tlsroute/nginx-deploy.yaml

kubectl apply -f nginx-deploy.yaml -n istio

```
apiVersion: v1
kind: Service
metadata:
  name: my-nginx
  labels:
    run: my-nginx
spec:
  ports:
  - port: 443
    protocol: TCP
  selector:
    run: my-nginx
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: my-nginx
spec:
  selector:
    matchLabels:
      run: my-nginx
  replicas: 1
  template:
    metadata:
      labels:
        run: my-nginx
    spec:
      containers:
      - name: my-nginx
        image: nginx
        ports:
        - containerPort: 443
        volumeMounts:
        - name: nginx-config
          mountPath: /etc/nginx
          readOnly: true
        - name: nginx-server-certs
          mountPath: /etc/nginx-server-certs
          readOnly: true
      volumes:
      - name: nginx-config
        configMap:
          name: nginx-configmap
      - name: nginx-server-certs
        secret:
          secretName: nginx-server-certs
```

tlsroute/nginx-deploy-v2.yaml

kubectl apply -f nginx-deploy-v2.yaml -n istio

```
apiVersion: v1
kind: Service
metadata:
  name: my-nginx-v2
  labels:
    run: my-nginx-v2
spec:
  ports:
  - port: 443
    protocol: TCP
  selector:
    run: my-nginx-v2
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: my-nginx-v2
spec:
  selector:
    matchLabels:
      run: my-nginx-v2
  replicas: 1
  template:
    metadata:
      labels:
        run: my-nginx-v2
    spec:
      containers:
      - name: my-nginx
        image: nginx
        ports:
        - containerPort: 443
        volumeMounts:
        - name: nginx-config
          mountPath: /etc/nginx
          readOnly: true
        - name: nginx-server-certs
          mountPath: /etc/nginx-server-certs
          readOnly: true
      volumes:
      - name: nginx-config
        configMap:
          name: nginx-configmap
      - name: nginx-server-certs
        secret:
          secretName: nginx-server-certs
```



tlsroute/gw-listeners-tls.yaml

kubectl apply -f gw-listeners-tls.yaml -n istio-system

```
kind: Gateway
apiVersion: gateway.networking.k8s.io/v1alpha2
metadata:
  name: bookinfo
spec:
  addresses:
  - value: istio-ingressgateway.istio-system.svc.cluster.local
    type: Hostname
  gatewayClassName: istio
  listeners:  
  - hostname: "nginx.example.com"
    port: 443
    name: gw-bookinfo
    protocol: TLS
    tls:
      mode: Passthrough
    allowedRoutes:
      namespaces:
        from: All
```

gatewayclass/isito-gc.yaml

kubectl apply -f isito-gc.yaml -n istio-system

```
apiVersion: networking.x-k8s.io/v1alpha1
kind: GatewayClass
metadata:
  name: istio
spec:
  controller: istio.io/gateway-controller
```

tlsroute/nginx-tls-rules-backendRefs-weight.yaml

kubectl apply -f nginx-tls-rules-backendRefs-weight.yaml -n istio

```
apiVersion: gateway.networking.k8s.io/v1alpha2
kind: TLSRoute
metadata:
  name: bookinfo
spec:
  parentRefs:
  - name: bookinfo
    namespace: istio-system
    kind: Gateway
  rules:
  - backendRefs:
    - name: my-nginx
      port: 443
      weight: 50
    - name: my-nginx-v2
      port: 443
      weight: 50      
```

访问

清理：

kubectl delete GatewayClass istio -n istio-system

kubectl delete gateways.gateway.networking.k8s.io  -n istio-system bookinfo 

kubectl delete tlsroutes.gateway.networking.k8s.io -n istio bookinfo

kubectl delete -f nginx-deploy.yaml -n istio

kubectl delete -f nginx-deploy-v2.yaml -n istio

 kubectl delete configmap nginx-configmap -n istio

kubectl delete secret nginx-server-certs   -n istio



https://nginx.example.com:32388/

![1653542757(1)](images\1653542757(1).jpg)



### hostnames

1创建证书

 openssl req -x509 -sha256 -nodes -days 365 -newkey rsa:2048 -subj '/O=example Inc./CN=example.com' -keyout example.com.key -out example.com.crt 

 openssl req -out nginx.example.com.csr -newkey rsa:2048 -nodes -keyout nginx.example.com.key -subj "/CN=nginx.example.com/O=some organization" 

 openssl x509 -req -days 365 -CA example.com.crt -CAkey example.com.key -set_serial 0 -in nginx.example.com.csr -out nginx.example.com.crt 

2创建secret

 kubectl create secret tls nginx-server-certs --key nginx.example.com.key --cert nginx.example.com.crt  -n istio

3创建nginx配置文件

tlsroute/nginx.conf

```
events {
}

http {
  log_format main '$remote_addr - $remote_user [$time_local]  $status '
  '"$request" $body_bytes_sent "$http_referer" '
  '"$http_user_agent" "$http_x_forwarded_for"';
  access_log /var/log/nginx/access.log main;
  error_log  /var/log/nginx/error.log;

  server {
    listen 443 ssl;

    root /usr/share/nginx/html;
    index index.html;

    server_name nginx.example.com;
    ssl_certificate /etc/nginx-server-certs/tls.crt;
    ssl_certificate_key /etc/nginx-server-certs/tls.key;
  }
}
```

 kubectl create configmap nginx-configmap --from-file=nginx.conf=./nginx.conf  -nistio

4创建deploy

tlsroute/nginx-deploy.yaml

kubectl apply -f nginx-deploy.yaml -n istio

```
apiVersion: v1
kind: Service
metadata:
  name: my-nginx
  labels:
    run: my-nginx
spec:
  ports:
  - port: 443
    protocol: TCP
  selector:
    run: my-nginx
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: my-nginx
spec:
  selector:
    matchLabels:
      run: my-nginx
  replicas: 1
  template:
    metadata:
      labels:
        run: my-nginx
    spec:
      containers:
      - name: my-nginx
        image: nginx
        ports:
        - containerPort: 443
        volumeMounts:
        - name: nginx-config
          mountPath: /etc/nginx
          readOnly: true
        - name: nginx-server-certs
          mountPath: /etc/nginx-server-certs
          readOnly: true
      volumes:
      - name: nginx-config
        configMap:
          name: nginx-configmap
      - name: nginx-server-certs
        secret:
          secretName: nginx-server-certs
```



tlsroute/gw-listeners-tls.yaml

kubectl apply -f gw-listeners-tls.yaml -n istio-system

```
kind: Gateway
apiVersion: gateway.networking.k8s.io/v1alpha2
metadata:
  name: bookinfo
spec:
  addresses:
  - value: istio-ingressgateway.istio-system.svc.cluster.local
    type: Hostname
  gatewayClassName: istio
  listeners:  
  - hostname: "nginx.example.com"
    port: 443
    name: gw-bookinfo
    protocol: TLS
    tls:
      mode: Passthrough
    allowedRoutes:
      namespaces:
        from: All
```

gatewayclass/isito-gc.yaml

kubectl apply -f isito-gc.yaml -n istio-system

```
apiVersion: networking.x-k8s.io/v1alpha1
kind: GatewayClass
metadata:
  name: istio
spec:
  controller: istio.io/gateway-controller
```

tlsroute/nginx-tls-hostnames.yaml

kubectl apply -f nginx-tls-hostnames.yaml -n istio

```
apiVersion: gateway.networking.k8s.io/v1alpha2
kind: TLSRoute
metadata:
  name: bookinfo
spec:
  parentRefs:
  - name: bookinfo
    namespace: istio-system
    kind: Gateway
  rules:
  - backendRefs:
    - name: my-nginx
      port: 443
  hostnames:
  - nginx.example.com
```

访问

清理：

kubectl delete GatewayClass istio -n istio-system

kubectl delete gateways.gateway.networking.k8s.io  -n istio-system bookinfo 

kubectl delete tlsroutes.gateway.networking.k8s.io -n istio bookinfo

kubectl delete -f nginx-deploy.yaml -n istio

 kubectl delete configmap nginx-configmap -n istio

kubectl delete secret nginx-server-certs   -n istio



https://nginx.example.com:32388/



### parentRefs

#### group，kind，name，namespace

1创建证书

 openssl req -x509 -sha256 -nodes -days 365 -newkey rsa:2048 -subj '/O=example Inc./CN=example.com' -keyout example.com.key -out example.com.crt 

 openssl req -out nginx.example.com.csr -newkey rsa:2048 -nodes -keyout nginx.example.com.key -subj "/CN=nginx.example.com/O=some organization" 

 openssl x509 -req -days 365 -CA example.com.crt -CAkey example.com.key -set_serial 0 -in nginx.example.com.csr -out nginx.example.com.crt 

2创建secret

 kubectl create secret tls nginx-server-certs --key nginx.example.com.key --cert nginx.example.com.crt  -n istio

3创建nginx配置文件

tlsroute/nginx.conf

```
events {
}

http {
  log_format main '$remote_addr - $remote_user [$time_local]  $status '
  '"$request" $body_bytes_sent "$http_referer" '
  '"$http_user_agent" "$http_x_forwarded_for"';
  access_log /var/log/nginx/access.log main;
  error_log  /var/log/nginx/error.log;

  server {
    listen 443 ssl;

    root /usr/share/nginx/html;
    index index.html;

    server_name nginx.example.com;
    ssl_certificate /etc/nginx-server-certs/tls.crt;
    ssl_certificate_key /etc/nginx-server-certs/tls.key;
  }
}
```

 kubectl create configmap nginx-configmap --from-file=nginx.conf=./nginx.conf  -nistio

4创建deploy

tlsroute/nginx-deploy.yaml

kubectl apply -f nginx-deploy.yaml -n istio

```
apiVersion: v1
kind: Service
metadata:
  name: my-nginx
  labels:
    run: my-nginx
spec:
  ports:
  - port: 443
    protocol: TCP
  selector:
    run: my-nginx
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: my-nginx
spec:
  selector:
    matchLabels:
      run: my-nginx
  replicas: 1
  template:
    metadata:
      labels:
        run: my-nginx
    spec:
      containers:
      - name: my-nginx
        image: nginx
        ports:
        - containerPort: 443
        volumeMounts:
        - name: nginx-config
          mountPath: /etc/nginx
          readOnly: true
        - name: nginx-server-certs
          mountPath: /etc/nginx-server-certs
          readOnly: true
      volumes:
      - name: nginx-config
        configMap:
          name: nginx-configmap
      - name: nginx-server-certs
        secret:
          secretName: nginx-server-certs
```



tlsroute/gw-listeners-tls.yaml

kubectl apply -f gw-listeners-tls.yaml -n istio-system

```
kind: Gateway
apiVersion: gateway.networking.k8s.io/v1alpha2
metadata:
  name: bookinfo
spec:
  addresses:
  - value: istio-ingressgateway.istio-system.svc.cluster.local
    type: Hostname
  gatewayClassName: istio
  listeners:  
  - hostname: "nginx.example.com"
    port: 443
    name: gw-bookinfo
    protocol: TLS
    tls:
      mode: Passthrough
    allowedRoutes:
      namespaces:
        from: All
```

gatewayclass/isito-gc.yaml

kubectl apply -f isito-gc.yaml -n istio-system

```
apiVersion: networking.x-k8s.io/v1alpha1
kind: GatewayClass
metadata:
  name: istio
spec:
  controller: istio.io/gateway-controller
```

tlsroute/nginx-tls-parentRefs.yaml

kubectl apply -f nginx-tls-parentRefs.yaml -n istio

```
apiVersion: gateway.networking.k8s.io/v1alpha2
kind: TLSRoute
metadata:
  name: bookinfo
spec:
  parentRefs:
  - name: bookinfo
    namespace: istio-system
    kind: Gateway
    group: gateway.networking.k8s.io
  rules:
  - backendRefs:
    - name: my-nginx
      port: 443
```

访问

清理：

kubectl delete GatewayClass istio -n istio-system

kubectl delete gateways.gateway.networking.k8s.io  -n istio-system bookinfo 

kubectl delete tlsroutes.gateway.networking.k8s.io -n istio bookinfo

kubectl delete -f nginx-deploy.yaml -n istio

kubectl delete configmap nginx-configmap -n istio

kubectl delete secret nginx-server-certs   -n istio



https://nginx.example.com:32388/



#### sectionName

1创建证书

 openssl req -x509 -sha256 -nodes -days 365 -newkey rsa:2048 -subj '/O=example Inc./CN=example.com' -keyout example.com.key -out example.com.crt 

 openssl req -out nginx.example.com.csr -newkey rsa:2048 -nodes -keyout nginx.example.com.key -subj "/CN=nginx.example.com/O=some organization" 

 openssl x509 -req -days 365 -CA example.com.crt -CAkey example.com.key -set_serial 0 -in nginx.example.com.csr -out nginx.example.com.crt 

2创建secret

 kubectl create secret tls nginx-server-certs --key nginx.example.com.key --cert nginx.example.com.crt  -n istio

3创建nginx配置文件

tlsroute/nginx.conf

```
events {
}

http {
  log_format main '$remote_addr - $remote_user [$time_local]  $status '
  '"$request" $body_bytes_sent "$http_referer" '
  '"$http_user_agent" "$http_x_forwarded_for"';
  access_log /var/log/nginx/access.log main;
  error_log  /var/log/nginx/error.log;

  server {
    listen 443 ssl;

    root /usr/share/nginx/html;
    index index.html;

    server_name nginx.example.com;
    ssl_certificate /etc/nginx-server-certs/tls.crt;
    ssl_certificate_key /etc/nginx-server-certs/tls.key;
  }
}
```

 kubectl create configmap nginx-configmap --from-file=nginx.conf=./nginx.conf  -nistio

4创建deploy

tlsroute/nginx-deploy.yaml

kubectl apply -f nginx-deploy.yaml -n istio

```
apiVersion: v1
kind: Service
metadata:
  name: my-nginx
  labels:
    run: my-nginx
spec:
  ports:
  - port: 443
    protocol: TCP
  selector:
    run: my-nginx
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: my-nginx
spec:
  selector:
    matchLabels:
      run: my-nginx
  replicas: 1
  template:
    metadata:
      labels:
        run: my-nginx
    spec:
      containers:
      - name: my-nginx
        image: nginx
        ports:
        - containerPort: 443
        volumeMounts:
        - name: nginx-config
          mountPath: /etc/nginx
          readOnly: true
        - name: nginx-server-certs
          mountPath: /etc/nginx-server-certs
          readOnly: true
      volumes:
      - name: nginx-config
        configMap:
          name: nginx-configmap
      - name: nginx-server-certs
        secret:
          secretName: nginx-server-certs
```



tlsroute/gw-listeners-tls.yaml

kubectl apply -f gw-listeners-tls.yaml -n istio-system

```
kind: Gateway
apiVersion: gateway.networking.k8s.io/v1alpha2
metadata:
  name: bookinfo
spec:
  addresses:
  - value: istio-ingressgateway.istio-system.svc.cluster.local
    type: Hostname
  gatewayClassName: istio
  listeners:  
  - hostname: "nginx.example.com"
    port: 443
    name: gw-bookinfo
    protocol: TLS
    tls:
      mode: Passthrough
    allowedRoutes:
      namespaces:
        from: All
```

gatewayclass/isito-gc.yaml

kubectl apply -f isito-gc.yaml -n istio-system

```
apiVersion: networking.x-k8s.io/v1alpha1
kind: GatewayClass
metadata:
  name: istio
spec:
  controller: istio.io/gateway-controller
```

tlsroute/nginx-tls-parentRefs-sectionName.yaml

kubectl apply -f nginx-tls-parentRefs-sectionName.yaml -n istio

```
apiVersion: gateway.networking.k8s.io/v1alpha2
kind: TLSRoute
metadata:
  name: bookinfo
spec:
  parentRefs:
  - name: bookinfo
    namespace: istio-system
    kind: Gateway
    group: gateway.networking.k8s.io
    sectionName: gw-bookinfo
  rules:
  - backendRefs:
    - name: my-nginx
      port: 443
```

访问

清理：

kubectl delete GatewayClass istio -n istio-system

kubectl delete gateways.gateway.networking.k8s.io  -n istio-system bookinfo 

kubectl delete tlsroutes.gateway.networking.k8s.io -n istio bookinfo

kubectl delete -f nginx-deploy.yaml -n istio

kubectl delete configmap nginx-configmap -n istio

kubectl delete secret nginx-server-certs   -n istio



https://nginx.example.com:32388/





## TCPRoute

### parentRefs

#### group，kind，name，namespace

tlsroute/gw-listeners-protocol-TCP.yaml

kubectl apply -f gw-listeners-protocol-TCP.yaml -n istio-system

```
kind: Gateway
apiVersion: gateway.networking.k8s.io/v1alpha2
metadata:
  name: echo
spec:
  addresses:
  - value: istio-ingressgateway.istio-system.svc.cluster.local
    type: Hostname
  gatewayClassName: istio
  listeners:
  - protocol: TCP
    port: 15443
    name: gw-tcp
    allowedRoutes:
      namespaces:
        from: All
```

gatewayclass/istio-gc.yaml

kubectl apply -f istio-gc.yaml -n istio-system

```
apiVersion: gateway.networking.k8s.io/v1alpha2
kind: GatewayClass
metadata:
  name: istio
spec:
  controllerName: istio.io/gateway-controller
```



tcp-echo-services.yaml

kubectl  apply -f tcp-echo-services.yaml -n istio

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

tcproute/echo-tcp-route-parentRefs.yaml

kubectl apply -f echo-tcp-route-parentRefs.yaml -n istio

```
kind: TCPRoute
apiVersion: gateway.networking.k8s.io/v1alpha2
metadata:
  name: echo
spec:
  parentRefs:
  - name: echo
    namespace: istio-system
    kind: Gateway
    group: gateway.networking.k8s.io
  rules:
  - backendRefs:
    - name: tcp-echo
      port: 9000
```

telnet 192.168.229.134  31390

清理：

kubectl delete GatewayClass istio -n istio-system

kubectl delete gateways.gateway.networking.k8s.io  -n istio-system echo

kubectl delete tcproutes.gateway.networking.k8s.io echo -n istio

kubectl delete -f tcp-echo-services.yaml -n istio

![1653546521(1)](images\1653546521(1).jpg)

![1653546568(1)](images\1653546568(1).jpg)



![1653546606(1)](images\1653546606(1).jpg)

#### sectionName

tlsroute/gw-listeners-protocol-TCP.yaml

kubectl apply -f gw-listeners-protocol-TCP.yaml -n istio-system

```
kind: Gateway
apiVersion: gateway.networking.k8s.io/v1alpha2
metadata:
  name: echo
spec:
  addresses:
  - value: istio-ingressgateway.istio-system.svc.cluster.local
    type: Hostname
  gatewayClassName: istio
  listeners:
  - protocol: TCP
    port: 15443
    name: gw-tcp
    allowedRoutes:
      namespaces:
        from: All
```

gatewayclass/istio-gc.yaml

kubectl apply -f istio-gc.yaml -n istio-system

```
apiVersion: gateway.networking.k8s.io/v1alpha2
kind: GatewayClass
metadata:
  name: istio
spec:
  controllerName: istio.io/gateway-controller
```



tcp-echo-services.yaml

kubectl  apply -f tcp-echo-services.yaml -n istio

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

tcproute/echo-tcp-route-parentRefs-sectionName.yaml

kubectl apply -f echo-tcp-route-parentRefs-sectionName.yaml -n istio

```
kind: TCPRoute
apiVersion: gateway.networking.k8s.io/v1alpha2
metadata:
  name: echo
spec:
  parentRefs:
  - name: echo
    namespace: istio-system
    kind: Gateway
    group: gateway.networking.k8s.io
    sectionName: gw-tcp
  rules:
  - backendRefs:
    - name: tcp-echo
      port: 9000
```

telnet 192.168.229.134  31390

清理：

kubectl delete GatewayClass istio -n istio-system

kubectl delete gateways.gateway.networking.k8s.io  -n istio-system echo

kubectl delete tcproutes.gateway.networking.k8s.io echo -n istio

kubectl delete -f tcp-echo-services.yaml -n istio



### rules

#### backendRefs

##### name,port

略

##### group,kind

tlsroute/gw-listeners-protocol-TCP.yaml

kubectl apply -f gw-listeners-protocol-TCP.yaml -n istio-system

```
kind: Gateway
apiVersion: gateway.networking.k8s.io/v1alpha2
metadata:
  name: echo
spec:
  addresses:
  - value: istio-ingressgateway.istio-system.svc.cluster.local
    type: Hostname
  gatewayClassName: istio
  listeners:
  - protocol: TCP
    port: 15443
    name: gw-tcp
    allowedRoutes:
      namespaces:
        from: All
```

gatewayclass/istio-gc.yaml

kubectl apply -f istio-gc.yaml -n istio-system

```
apiVersion: gateway.networking.k8s.io/v1alpha2
kind: GatewayClass
metadata:
  name: istio
spec:
  controllerName: istio.io/gateway-controller
```



tcp-echo-services.yaml

kubectl  apply -f tcp-echo-services.yaml -n istio

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

tcproute/echo-tcp-route-rules-backendRefs-groupkind.yaml

kubectl apply -f echo-tcp-route-rules-backendRefs-groupkind.yaml -n istio

```
kind: TCPRoute
apiVersion: gateway.networking.k8s.io/v1alpha2
metadata:
  name: echo
spec:
  parentRefs:
  - name: echo
    namespace: istio-system
    kind: Gateway
    group: gateway.networking.k8s.io
  rules:
  - backendRefs:
    - name: tcp-echo
      port: 9000
      group: ""
      kind: Service
```

telnet 192.168.229.134  31390

清理：

kubectl delete GatewayClass istio -n istio-system

kubectl delete gateways.gateway.networking.k8s.io  -n istio-system echo

kubectl delete tcproutes.gateway.networking.k8s.io echo -n istio

kubectl delete -f tcp-echo-services.yaml -n istio





##### namespace

tlsroute/gw-listeners-protocol-TCP.yaml

kubectl apply -f gw-listeners-protocol-TCP.yaml -n istio-system

```
kind: Gateway
apiVersion: gateway.networking.k8s.io/v1alpha2
metadata:
  name: echo
spec:
  addresses:
  - value: istio-ingressgateway.istio-system.svc.cluster.local
    type: Hostname
  gatewayClassName: istio
  listeners:
  - protocol: TCP
    port: 15443
    name: gw-tcp
    allowedRoutes:
      namespaces:
        from: All
```

gatewayclass/istio-gc.yaml

kubectl apply -f istio-gc.yaml -n istio-system

```
apiVersion: gateway.networking.k8s.io/v1alpha2
kind: GatewayClass
metadata:
  name: istio
spec:
  controllerName: istio.io/gateway-controller
```



tcp-echo-services.yaml

kubectl  apply -f tcp-echo-services.yaml -n istio

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

tcproute/echo-tcp-route-rules-backendRefs-namespace.yaml

kubectl apply -f echo-tcp-route-rules-backendRefs-namespace.yaml -n istio-system

```
kind: TCPRoute
apiVersion: gateway.networking.k8s.io/v1alpha2
metadata:
  name: echo
spec:
  parentRefs:
  - name: echo
    namespace: istio-system
    kind: Gateway
    group: gateway.networking.k8s.io
  rules:
  - backendRefs:
    - name: tcp-echo
      port: 9000
      group: ""
      kind: Service
      namespace: istio
```

telnet 192.168.229.134  31390

清理：

kubectl delete GatewayClass istio -n istio-system

kubectl delete gateways.gateway.networking.k8s.io  -n istio-system echo

kubectl delete tcproutes.gateway.networking.k8s.io echo -n istio-system

kubectl delete -f tcp-echo-services.yaml -n istio





##### weight

tlsroute/gw-listeners-protocol-TCP.yaml

kubectl apply -f gw-listeners-protocol-TCP.yaml -n istio-system

```
kind: Gateway
apiVersion: gateway.networking.k8s.io/v1alpha2
metadata:
  name: echo
spec:
  addresses:
  - value: istio-ingressgateway.istio-system.svc.cluster.local
    type: Hostname
  gatewayClassName: istio
  listeners:
  - protocol: TCP
    port: 15443
    name: gw-tcp
    allowedRoutes:
      namespaces:
        from: All
```

gatewayclass/istio-gc.yaml

kubectl apply -f istio-gc.yaml -n istio-system

```
apiVersion: gateway.networking.k8s.io/v1alpha2
kind: GatewayClass
metadata:
  name: istio
spec:
  controllerName: istio.io/gateway-controller
```



tcp-echo-services-v1-v2.yaml

kubectl  apply -f tcp-echo-services-v1-v2.yaml  -n istio

```
apiVersion: v1
kind: Service
metadata:
  name: tcp-echo-v1
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
    version: v1
---
apiVersion: v1
kind: Service
metadata:
  name: tcp-echo-v2
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
    version: v2
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

tcproute/echo-tcp-route-rules-backendRefs-weight.yaml

kubectl apply -f echo-tcp-route-rules-backendRefs-weight.yaml -n istio

```
kind: TCPRoute
apiVersion: gateway.networking.k8s.io/v1alpha2
metadata:
  name: echo
spec:
  parentRefs:
  - name: echo
    namespace: istio-system
    kind: Gateway
    group: gateway.networking.k8s.io
  rules:
  - backendRefs:
    - name: tcp-echo-v1
      port: 9000
      weight: 50
    - name: tcp-echo-v2
      port: 9000
      weight: 50
```

telnet 192.168.229.134  31390

清理：

kubectl delete GatewayClass istio -n istio-system

kubectl delete gateways.gateway.networking.k8s.io  -n istio-system echo

kubectl delete tcproutes.gateway.networking.k8s.io echo -n istio

kubectl delete -f tcp-echo-services-v1-v2.yaml -n istio

![1653547597(1)](images\1653547597(1).jpg)



## UDPRoute

==**udp不支持**==

### parentRefs

添加端口

```
  - name: udp
    port: 15449
    protocol: UDP
    targetPort: 15449
```

deploy-udp-listener.yaml

kubectl apply -f deploy-udp-listener.yaml -n istio

```
apiVersion: v1
kind: Service
metadata:
  name: udp-listener
spec:
  ports:
  - name: udp-listener
    port: 5005
  selector:
    app: udp-listener
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: udp-listener
spec:
  replicas: 1
  selector:
    matchLabels:
      app: udp-listener
  template:
    metadata:
      labels:
        app: udp-listener
    spec:
      containers:
      - name: udp-listener
        image: mendhak/udp-listener
        imagePullPolicy: IfNotPresent
        ports:
        - containerPort: 5005
```

tcproute/echo-udp-route-rules-backendRefs.yaml

kubectl apply -f echo-udp-route-rules-backendRefs.yaml -n istio

```
kind: UDPRoute
apiVersion: gateway.networking.k8s.io/v1alpha2
metadata:
  name: udp-listener
spec:
  parentRefs:
  - name: udp
    namespace: istio-system
    kind: Gateway
    group: gateway.networking.k8s.io
  rules:
  - backendRefs:
    - name: udp-listener
      port: 5005
```

tlsroute/gw-listeners-protocol-UDP.yaml

kubectl apply -f gw-listeners-protocol-UDP.yaml -n istio-system

```
kind: Gateway
apiVersion: gateway.networking.k8s.io/v1alpha2
metadata:
  name: udp
spec:
  addresses:
  - value: istio-ingressgateway.istio-system.svc.cluster.local
    type: Hostname
  gatewayClassName: istio
  listeners:
  - protocol: UDP
    port: 15449
    name: gw-udp
    allowedRoutes:
      namespaces:
        from: All
```

gatewayclass/istio-gc.yaml

kubectl apply -f istio-gc.yaml -n istio-system

```
apiVersion: gateway.networking.k8s.io/v1alpha2
kind: GatewayClass
metadata:
  name: istio
spec:
  controllerName: istio.io/gateway-controller
```





telnet 192.168.229.134  31390

### rules





## referencepolicies

==目前必须先创建ReferencePolicy==

https://github.com/istio/istio/issues/39142

 openssl req -x509 -sha256 -nodes -days 365 -newkey rsa:2048  -keyout cert.key -out cert.crt  -config  openssl.conf

创建secret

kubectl create -n istio secret tls bookinfo-gateway-secret  --key ./cert.key --cert=./cert.crt



rp-secret.yaml

kubectl apply -f rp-secret.yaml -n istio

```
kind: ReferencePolicy
apiVersion: gateway.networking.k8s.io/v1alpha2
metadata:
  name: rp-secret
  namespace: istio
spec:
  from:
  - group: gateway.networking.k8s.io
    kind: Gateway
    namespace: istio-system
  to:
  - group: ""
    kind: Secret
    name: bookinfo-gateway-secret
```



referencepolicies/gw-listeners-tls-certificateRefs-namespace.yaml

kubectl apply -f gw-listeners-tls-certificateRefs-namespace.yaml -n istio-system

```
kind: Gateway
apiVersion: gateway.networking.k8s.io/v1alpha2
metadata:
  name: bookinfo
spec:
  addresses:
  - value: istio-ingressgateway.istio-system.svc.cluster.local
    type: Hostname
  gatewayClassName: istio
  listeners:  
  - hostname: "bookinfo.com"
    port: 443
    name: bookinfo-gw
    protocol: HTTPS
    tls:
      options: {}
      certificateRefs:
      - name: bookinfo-gateway-secret
        namespace: istio
        kind: Secret
    allowedRoutes:
      namespaces:
        from: All
```

gatewayclass/istio-gc.yaml

kubectl apply -f istio-gc.yaml -n istio-system

```
apiVersion: gateway.networking.k8s.io/v1alpha2
kind: GatewayClass
metadata:
  name: istio
spec:
  controllerName: istio.io/gateway-controller
```

referencepolicies/bookinfo-httproute-system.yaml

kubectl apply -f bookinfo-httproute-system.yaml -n istio

```
apiVersion: gateway.networking.k8s.io/v1alpha2
kind: HTTPRoute
metadata:
  name: bookinfo
spec:
  parentRefs:
  - name: bookinfo
    namespace: istio-system
    kind: Gateway
  rules:
  - matches:
    - path:
        type: PathPrefix
        value: /
    backendRefs:
    - name: productpage
      port: 9080
```

清理：

kubectl delete GatewayClass istio -n istio-system

kubectl delete gateways.gateway.networking.k8s.io  -n istio-system bookinfo 

kubectl delete httproutes.gateway.networking.k8s.io -n istio bookinfo

kubectl delete -n istio secret bookinfo-gateway-secret



https://bookinfo.com:32388/productpage
