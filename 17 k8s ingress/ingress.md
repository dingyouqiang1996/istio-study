# 什么是gateway api

Ingress 资源是 Kubernetes 众多成功案例中的一个。它创造了一个[多样化的 Ingress 控制器的生态系统](https://link.zhihu.com/?target=https%3A//kubernetes.io/docs/concepts/services-networking/ingress-controllers/)，这些控制器以标准化和一致的方式在数十万个集群中使用。这种标准化有助于用户采用 Kubernetes。然而，在 Ingress 创建五年后，有迹象表明它被分割成不同但[惊人相似的 CRD](https://link.zhihu.com/?target=https%3A//dave.cheney.net/paste/ingress-is-dead-long-live-ingressroute.pdf) 和 [过载的注释](https://link.zhihu.com/?target=https%3A//kubernetes.github.io/ingress-nginx/user-guide/nginx-configuration/annotations/)。Ingress 普遍存在的可移植性问题也限制了它的未来。

那是在 2019 年圣地亚哥的 Kubecon 上，一群充满激情的贡献者聚集在一起，讨论 [Ingress 的发展](https://link.zhihu.com/?target=https%3A//static.sched.com/hosted_files/kccncna19/a5/Kubecon%20San%20Diego%202019%20-%20Evolving%20the%20Kubernetes%20Ingress%20APIs%20to%20GA%20and%20Beyond%20%5BPUBLIC%5D.pdf)。拥挤的人群溢出到了街对面的酒店大堂，而讨论出来的东西后来被称为 [Gateway API](https://link.zhihu.com/?target=https%3A//gateway-api.sigs.k8s.io/)。这次讨论是基于几个关键的假设：

1. 路由匹配、流量管理和服务暴露所依据的 API 标准已经商业化，对其实施者和用户提供的定制 API 的价值很小。
2. 可以通过共同的核心 API 资源来表示 L4/L7 的路由和流量管理。
3. 可以在不牺牲核心 API 的用户体验的前提下，为更复杂的功能提供扩展性。

Gateway API是一个由 [SIG-NETWORK](https://github.com/kubernetes/community/tree/master/sig-network) 社区管理的开源项目。它是一些网络资源模型。资源包括`GatewayClass`,`Gateway`, `HTTPRoute`, `TCPRoute`, `UDPRoute，TLSRoute`等，目的是通过易表达，可扩展，基于校色接口解决k8s服务网络，它由很多供应商实现，由广泛的工业支持。

# 资源类型

Gateway API 引入了一些新的资源类型：

- **[GatewayClasses](https://link.zhihu.com/?target=https%3A//gateway-api.sigs.k8s.io/references/spec/%23networking.x-k8s.io/v1alpha1.GatewayClass)** 是集群范围内的资源，作为模板，明确地定义由其衍生的网关的行为。这与 StorageClasses 的概念类似，但用于网络数据平面。
- **[Gateway](https://link.zhihu.com/?target=https%3A//gateway-api.sigs.k8s.io/references/spec/%23networking.x-k8s.io/v1alpha1.Gateway)** 是 GatewayClasses 的部署实例。它们是执行路由的数据平面的逻辑表示，它可能是集群内的代理、硬件 LB 或云 LB。
- **路由** 不是一个单一的资源，而是代表许多不同的特定协议的路由资源。[HTTPRoute](https://link.zhihu.com/?target=https%3A//gateway-api.sigs.k8s.io/references/spec/%23networking.x-k8s.io/v1alpha1.HTTPRoute) 有匹配、过滤和路由规则，这些规则被应用到可以处理 HTTP 和 HTTPS 流量的网关。同样，还有 [TCPRoutes](https://link.zhihu.com/?target=https%3A//gateway-api.sigs.k8s.io/references/spec/%23networking.x-k8s.io/v1alpha1.TCPRoute)、[UDPRoutes](https://link.zhihu.com/?target=https%3A//gateway-api.sigs.k8s.io/references/spec/%23networking.x-k8s.io/v1alpha1.UDPRoute) 和 [TLSRoutes](https://link.zhihu.com/?target=https%3A//gateway-api.sigs.k8s.io/references/spec/%23networking.x-k8s.io/v1alpha1.TLSRoute)，它们也有协议特定的语义。这种模式也允许网关 API 在未来逐步扩展其协议支持。



# 资源之前的关系

![v2-3eafa9788be3e6736a4b5b3ddecbf7db_1440w](images\v2-3eafa9788be3e6736a4b5b3ddecbf7db_1440w.jpg)



# **相关概念**

在服务 API 中有3个主要的角色。

- 基础设施提供者
- 集群[运维](https://cloud.tencent.com/solution/operation?from=10680)
- 应用开发人员

在某些用例中，可能会有第四个角色应用程序管理员。

服务 API 的相关资源最初将作为 CRD 定义在 `networking.x-k8s.io` API 组中。在我们的资源模型中，有3种主要类型的对象：

- `GatewayClass` 定义了一组具有共同配置和行为的网关。
- `Gateway` 网关请求一个可以将流量转换到集群内服务的点。
- `Routes` 路由描述了通过网关而来的流量如何映射到服务。

## **GatewayClass**

`GatewayClass` 定义了一组共享共同配置和行为的网关，每个GatewayClass 将由一个控制器处理，尽管控制器可以处理多个GatewayClass。

GatewayClass 是一个集群范围的资源，必须定义至少一个GatewayClass 才能提供 Gateways 功能。实现 Gateway API 的控制器通过提供相关联的 GatewayClass 资源来实现，用户可以从他们的Gateway 中引用该资源。

这类似于 Ingress 的 `IngressClass` 和 `PersistentVolumes` 的`StorageClass`。在 Ingress v1beta1 中，最接近 GatewayClass 的是 `ingress-class` 注解，而在 IngressV1 中，最接近的就是 `IngressClass` 对象。

## **Gateway**

Gateway 网关描述了如何将流量路由到集群内的服务。也就是说，它定义了将流量从不了解 Kubernetes 的地方路由到 Kubernetes 的地方的方法请求。例如，由云负载均衡器、集群内代理或外部硬件负载均衡器发送到 Kubernetes 服务的流量，虽然许多用例的客户端流量源自集群的 "外部"，但这并不是强制要求的。

它定义了对实现 GatewayClass 配置和行为协定的特定负载均衡器配置的请求。该资源可以由运维人员直接创建，也可以由处理 GatewayClass 的控制器创建。

由于 Gateway 规范声明了用户意图，因此它可能不包含规范中所有属性的完整规范。例如，用户可以省略地址、端口、TLS 等字段，这使得管理 GatewayClass 的控制器可以为用户提供这些设置，从而使规范更具可移植性，使用 GatewayClass Status 对象将使此行为更清楚。

一个 Gateway 可以包含一个或多个 *Route 引用，这些引用的作用是将一个子集的流量路由到一个特定的服务上。

## **{HTTP,TCP,Foo}Route**

Route 对象定义了特定协议的规则，用于将请求从网关映射到 Kubernetes 服务。

`HTTPRoute` 和 `TCPRoute` 是目前唯一定义的Route对象，未来可能会添加其他特定协议的 Route 对象。

## **BackendPolicy**

BackendPolicy 提供了一种配置网关和后端之间连接的方法。在这个 API 中，后端是指路由可以转发流量的任何资源。这个级别的配置目前仅限于 TLS，但将来会扩展到支持更高级的策略，如健康检查。

一些后端配置可能会根据针对后端的 Route 而有所不同。在这些情况下，配置字段将放在 Routes 上，而不是 BackendPolicy 上，有关该资源未来可能配置的更多信息，请参考相关的 GitHub Issue。

# 部署

kubectl kustomize "config/crd"|kubectl apply -f -



# 资源详解

## GatewayClass

### controller

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

gatewayclass/bookinfo-gateway.yaml

kubectl apply -f bookinfo-gateway.yaml -n istio-system

```
kind: Gateway
apiVersion: networking.x-k8s.io/v1alpha1
metadata:
  name: bookinfo
spec:
  gatewayClassName: istio
  listeners:  
  - hostname: "*"
    port: 80
    protocol: HTTP
    routes:
      namespaces:
        from: All
      selector:
        matchLabels:
          gateway: bookinfo
      kind: HTTPRoute
```



gatewayclass/productpage-http-route.yaml

kubectl apply -f productpage-http-route.yaml -n istio

```
kind: HTTPRoute
apiVersion: networking.x-k8s.io/v1alpha1
metadata:
  name: productpage
  labels:
    gateway: bookinfo
spec:
  gateways:
    allow: All
  hostnames: ["*"]
  rules:
  - matches:
    - path:
        type: Prefix
        value: /productpage
    - path:
        type: Prefix
        value: /static
    forwardTo:
    - serviceName: productpage
      port: 9080
```

访问

清理：

kubectl delete httproute productpage -n istio

kubectl delete gateways.networking.x-k8s.io bookinfo -n istio-system

kubectl delete gatewayclass istio -n istio-system



### parametersRef

用于添加参数，略

## Gateway

### gatewayClassName

指定gatewayclass，略

### addresses

#### type，value

##### NamedAddress

gateway/bookinfo-gateway-NamedAddress.yaml

kubectl apply -f bookinfo-gateway-NamedAddress.yaml -n istio-system

```
kind: Gateway
apiVersion: networking.x-k8s.io/v1alpha1
metadata:
  name: bookinfo
spec:
  gatewayClassName: istio
  addresses:
  - type: NamedAddress
    value: istio-ingressgateway
  listeners:  
  - hostname: "*"
    port: 80
    protocol: HTTP
    routes:
      namespaces:
        from: All
      selector:
        matchLabels:
          gateway: bookinfo
      kind: HTTPRoute
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

gateway/productpage-http-route.yaml

kubectl apply -f productpage-http-route.yaml -n istio

```
kind: HTTPRoute
apiVersion: networking.x-k8s.io/v1alpha1
metadata:
  name: productpage
  labels:
    gateway: bookinfo
spec:
  gateways:
    allow: All
  hostnames: ["*"]
  rules:
  - matches:
    - path:
        type: Prefix
        value: /productpage
    - path:
        type: Prefix
        value: /static
    forwardTo:
    - serviceName: productpage
      port: 9080
```

访问

清理：

kubectl delete httproute productpage -n istio

kubectl delete gateways.networking.x-k8s.io bookinfo -n istio-system

kubectl delete gatewayclass istio -n istio-system



kubectl port-forward --address 0.0.0.0 -n istio-system istio-ingressgateway-746c595f8b-bszqn 15000:15000

![1629944536(1)](images\1629944536(1).jpg)

![1629944638(1)](images\1629944638(1).jpg)





##### IPAddress

gateway/bookinfo-gateway-IPAddress.yaml

kubectl apply -f bookinfo-gateway-IPAddress.yaml -n istio-system

```
kind: Gateway
apiVersion: networking.x-k8s.io/v1alpha1
metadata:
  name: bookinfo
spec:
  gatewayClassName: istio
  addresses:
  - type: IPAddress
    value: 192.168.198.194
  listeners:  
  - hostname: "*"
    port: 80
    protocol: HTTP
    routes:
      namespaces:
        from: All
      selector:
        matchLabels:
          gateway: bookinfo
      kind: HTTPRoute
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

gateway/productpage-http-route.yaml

kubectl apply -f productpage-http-route.yaml -n istio

```
kind: HTTPRoute
apiVersion: networking.x-k8s.io/v1alpha1
metadata:
  name: productpage
  labels:
    gateway: bookinfo
spec:
  gateways:
    allow: All
  hostnames: ["*"]
  rules:
  - matches:
    - path:
        type: Prefix
        value: /productpage
    - path:
        type: Prefix
        value: /static
    forwardTo:
    - serviceName: productpage
      port: 9080
```

访问

清理：

kubectl delete httproute productpage -n istio

kubectl delete gateways.networking.x-k8s.io bookinfo -n istio-system

kubectl delete gatewayclass istio -n istio-system



failed to assign to any requested addresses: Only NamedAddress is supported, ignoring [192.168.198.194]

IPAddress不支持

##### 不指定type，默认IPAddress

gateway/bookinfo-gateway-type-none.yaml

kubectl apply -f bookinfo-gateway-type-none.yaml -n istio-system

```
kind: Gateway
apiVersion: networking.x-k8s.io/v1alpha1
metadata:
  name: bookinfo
spec:
  gatewayClassName: istio
  addresses:
  - value: 192.168.198.154
  listeners:
  - hostname: "*"
    port: 80
    protocol: HTTP
    routes:
      namespaces:
        from: All
      selector:
        matchLabels:
          gateway: bookinfo
      kind: HTTPRoute
```

failed to assign to any requested addresses: Only NamedAddress is supported, ignoring [192.168.198.194]

IPAddress不支持



### listeners

#### hostname

##### 所有

gateway/bookinfo-gateway-listeners-hostname-star.yaml

kubectl apply -f bookinfo-gateway-listeners-hostname-star.yaml -n istio-system

```
kind: Gateway
apiVersion: networking.x-k8s.io/v1alpha1
metadata:
  name: bookinfo
spec:
  gatewayClassName: istio
  listeners:  
  - hostname: "*"
    port: 80
    protocol: HTTP
    routes:
      namespaces:
        from: All
      selector:
        matchLabels:
          gateway: bookinfo
      kind: HTTPRoute
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

gateway/productpage-http-route.yaml

kubectl apply -f productpage-http-route.yaml -n istio

```
kind: HTTPRoute
apiVersion: networking.x-k8s.io/v1alpha1
metadata:
  name: productpage
  labels:
    gateway: bookinfo
spec:
  gateways:
    allow: All
  hostnames: ["*"]
  rules:
  - matches:
    - path:
        type: Prefix
        value: /productpage
    - path:
        type: Prefix
        value: /static
    forwardTo:
    - serviceName: productpage
      port: 9080
```

访问

清理：

kubectl delete httproute productpage -n istio

kubectl delete gateways.networking.x-k8s.io bookinfo -n istio-system

kubectl delete gatewayclass istio -n istio-system



![1629944908(1)](images\1629944908(1).jpg)

![1629944950(1)](images\1629944950(1).jpg)



##### 具体

gateway/bookinfo-gateway-listeners-hostname-specific.yaml

kubectl apply -f bookinfo-gateway-listeners-hostname-specific.yaml -n istio-system

```
kind: Gateway
apiVersion: networking.x-k8s.io/v1alpha1
metadata:
  name: bookinfo
spec:
  gatewayClassName: istio
  listeners:  
  - hostname: "bookinfo.demo"
    port: 80
    protocol: HTTP
    routes:
      namespaces:
        from: All
      selector:
        matchLabels:
          gateway: bookinfo
      kind: HTTPRoute
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

gateway/productpage-http-route.yaml

kubectl apply -f productpage-http-route.yaml -n istio

```
kind: HTTPRoute
apiVersion: networking.x-k8s.io/v1alpha1
metadata:
  name: productpage
  labels:
    gateway: bookinfo
spec:
  gateways:
    allow: All
  hostnames: ["*"]
  rules:
  - matches:
    - path:
        type: Prefix
        value: /productpage
    - path:
        type: Prefix
        value: /static
    forwardTo:
    - serviceName: productpage
      port: 9080
```

访问

清理：

kubectl delete httproute productpage -n istio

kubectl delete gateways.networking.x-k8s.io bookinfo -n istio-system

kubectl delete gatewayclass istio -n istio-system

![1629945065(1)](images\1629945065(1).jpg)



![1629945115(1)](images\1629945115(1).jpg)



##### semi-star

gateway/bookinfo-gateway-listeners-hostname-semi-star.yaml

kubectl apply -f bookinfo-gateway-listeners-hostname-semi-star.yaml -n istio-system

```
kind: Gateway
apiVersion: networking.x-k8s.io/v1alpha1
metadata:
  name: bookinfo
spec:
  gatewayClassName: istio
  listeners:  
  - hostname: "*.demo"
    port: 80
    protocol: HTTP
    routes:
      namespaces:
        from: All
      selector:
        matchLabels:
          gateway: bookinfo
      kind: HTTPRoute
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

gateway/productpage-http-route.yaml

kubectl apply -f productpage-http-route.yaml -n istio

```
kind: HTTPRoute
apiVersion: networking.x-k8s.io/v1alpha1
metadata:
  name: productpage
  labels:
    gateway: bookinfo
spec:
  gateways:
    allow: All
  hostnames: ["*"]
  rules:
  - matches:
    - path:
        type: Prefix
        value: /productpage
    - path:
        type: Prefix
        value: /static
    forwardTo:
    - serviceName: productpage
      port: 9080
```

访问

清理：

kubectl delete httproute productpage -n istio

kubectl delete gateways.networking.x-k8s.io bookinfo -n istio-system

kubectl delete gatewayclass istio -n istio-system

![1629945215(1)](images\1629945215(1).jpg)

![1629945251(1)](images\1629945251(1).jpg)



#### port,protocol

![1629869437(1)](images\1629869437(1).jpg)

##### http

gateway/bookinfo-gateway-listeners-protocol-http.yaml

kubectl apply -f bookinfo-gateway-listeners-protocol-http.yaml -n istio-system

```
kind: Gateway
apiVersion: networking.x-k8s.io/v1alpha1
metadata:
  name: bookinfo
spec:
  gatewayClassName: istio
  listeners:  
  - hostname: "*"
    port: 80
    protocol: HTTP
    routes:
      namespaces:
        from: All
      selector:
        matchLabels:
          gateway: bookinfo
      kind: HTTPRoute
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

gateway/productpage-http-route.yaml

kubectl apply -f productpage-http-route.yaml -n istio

```
kind: HTTPRoute
apiVersion: networking.x-k8s.io/v1alpha1
metadata:
  name: productpage
  labels:
    gateway: bookinfo
spec:
  gateways:
    allow: All
  hostnames: ["*"]
  rules:
  - matches:
    - path:
        type: Prefix
        value: /productpage
    - path:
        type: Prefix
        value: /static
    forwardTo:
    - serviceName: productpage
      port: 9080
```

访问

清理：

kubectl delete httproute productpage -n istio

kubectl delete gateways.networking.x-k8s.io bookinfo -n istio-system

kubectl delete gatewayclass istio -n istio-system

![1629948906(1)](images\1629948906(1).jpg)

![1629948959(1)](images\1629948959(1).jpg)



##### https

 openssl req -x509 -sha256 -nodes -days 365 -newkey rsa:2048  -keyout cert.key -out cert.crt  -config  openssl.conf

创建secret

kubectl create -n istio-system secret tls bookinfo-gateway-secret  --key ./cert.key --cert=./cert.crt



gateway/bookinfo-gateway-listeners-protocol-https.yaml

kubectl apply -f bookinfo-gateway-listeners-protocol-https.yaml -n istio-system

```
kind: Gateway
apiVersion: networking.x-k8s.io/v1alpha1
metadata:
  name: bookinfo
spec:
  gatewayClassName: istio
  listeners:  
  - hostname: "*"
    port: 443
    protocol: HTTPS
    tls:
      options: {}
      certificateRef:
        name: bookinfo-gateway-secret
        kind: Secret
        group: core
    routes:
      namespaces:
        from: All
      selector:
        matchLabels:
          gateway: bookinfo
      kind: HTTPRoute
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

gateway/productpage-http-route.yaml

kubectl apply -f productpage-http-route.yaml -n istio

```
kind: HTTPRoute
apiVersion: networking.x-k8s.io/v1alpha1
metadata:
  name: productpage
  labels:
    gateway: bookinfo
spec:
  gateways:
    allow: All
  hostnames: ["*"]
  rules:
  - matches:
    - path:
        type: Prefix
        value: /productpage
    - path:
        type: Prefix
        value: /static
    forwardTo:
    - serviceName: productpage
      port: 9080
```

访问

清理：

kubectl delete httproute productpage -n istio

kubectl delete gateways.networking.x-k8s.io bookinfo -n istio-system

kubectl delete gatewayclass istio -n istio-system

kubectl delete -n istio-system secret bookinfo-gateway-secret

![1629949188(1)](images\1629949188(1).jpg)

![1629949248(1)](images\1629949248(1).jpg)





##### TCP

gateway/bookinfo-gateway-listeners-protocol-TCP.yaml

kubectl apply -f bookinfo-gateway-listeners-protocol-TCP.yaml -n istio-system

```
kind: Gateway
apiVersion: networking.x-k8s.io/v1alpha1
metadata:
  name: echo
spec:
  gatewayClassName: istio
  listeners:
  - protocol: TCP
    port: 15443
    routes:
      namespaces:
        from: All
      kind: TCPRoute
      selector:
        matchLabels:
          gateway: echo
```

ingress svc 添加端口

```
  kubectl edit svc -n istio-system istio-ingressgateway 
  - name: tcp-echo
    nodePort: 31011
    port: 9000
    protocol: TCP
    targetPort: 9000
```

添加端口不能用，改为15443端口



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
apiVersion: networking.x-k8s.io/v1alpha1
metadata:
  name: echo
  labels:
    gateway: echo
spec:
  gateways:
    allow: All
  rules:
  - forwardTo:
    - serviceName: tcp-echo
      port: 9000
```

telnet 192.168.198.154 31010

清理：

kubectl  delete -f tcp-echo-services.yaml -n istio

kubectl delete TCPRoute echo -n istio

kubectl delete gateways.networking.x-k8s.io echo -n istio-system

![1629949918(1)](images\1629949918(1).jpg)



![1629949728(1)](images\1629949728(1).jpg)

![1629949778(1)](images\1629949778(1).jpg)



##### UDP

没有udp端口，不做烟瘴

```
kind: Gateway
apiVersion: networking.x-k8s.io/v1alpha1
metadata:
  name: test
spec:
  gatewayClassName: istio
  listeners:  
  - protocol: UDP
    port: 15443
    routes:
      kind: UDPRoute
      selector:
        matchLabels:
          "app": "foo"
      namespaces:
        from: "All"
```



```
kind: UDPRoute
apiVersion: networking.x-k8s.io/v1alpha1
metadata:
  name: udp-app-1
  labels:
    app: foo
spec:
  gateways:
    allow: All
  rules:
  - forwardTo:
    - serviceName: tcp-echo
      port: 9000
```



#### routes

##### group，kind

gateway/bookinfo-gateway-listeners-routes-group-kind.yaml

kubectl apply -f bookinfo-gateway-listeners-routes-group-kind.yaml -n istio-system

```
kind: Gateway
apiVersion: networking.x-k8s.io/v1alpha1
metadata:
  name: bookinfo
spec:
  gatewayClassName: istio
  listeners:  
  - hostname: "*"
    port: 80
    protocol: HTTP
    routes:
      namespaces:
        from: All
      selector:
        matchLabels:
          gateway: bookinfo
      kind: HTTPRoute
      group: networking.x-k8s.io
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

gateway/productpage-http-route.yaml

kubectl apply -f productpage-http-route.yaml -n istio

```
kind: HTTPRoute
apiVersion: networking.x-k8s.io/v1alpha1
metadata:
  name: productpage
  labels:
    gateway: bookinfo
spec:
  gateways:
    allow: All
  hostnames: ["*"]
  rules:
  - matches:
    - path:
        type: Prefix
        value: /productpage
    - path:
        type: Prefix
        value: /static
    forwardTo:
    - serviceName: productpage
      port: 9080
```

访问

清理：

kubectl delete httproute productpage -n istio

kubectl delete gateways.networking.x-k8s.io bookinfo -n istio-system

kubectl delete gatewayclass istio -n istio-system

![1629950256(1)](images\1629950256(1).jpg)

![1629950305(1)](images\1629950305(1).jpg)

![1629950346(1)](images\1629950346(1).jpg)

![1629950378(1)](images\1629950378(1).jpg)



##### namespaces

###### from

All

gateway/bookinfo-gateway-listeners-routes-namespaces-from.yaml

kubectl apply -f bookinfo-gateway-listeners-routes-namespaces-from.yaml -n istio-system

```
kind: Gateway
apiVersion: networking.x-k8s.io/v1alpha1
metadata:
  name: bookinfo
spec:
  gatewayClassName: istio
  listeners:  
  - hostname: "*"
    port: 80
    protocol: HTTP
    routes:
      namespaces:
        from: All
      selector:
        matchLabels:
          gateway: bookinfo
      kind: HTTPRoute
      group: networking.x-k8s.io
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

gateway/productpage-http-route.yaml

kubectl apply -f productpage-http-route.yaml -n istio

```
kind: HTTPRoute
apiVersion: networking.x-k8s.io/v1alpha1
metadata:
  name: productpage
  labels:
    gateway: bookinfo
spec:
  gateways:
    allow: All
  hostnames: ["*"]
  rules:
  - matches:
    - path:
        type: Prefix
        value: /productpage
    - path:
        type: Prefix
        value: /static
    forwardTo:
    - serviceName: productpage
      port: 9080
```

访问

清理：

kubectl delete httproute productpage -n istio

kubectl delete gateways.networking.x-k8s.io bookinfo -n istio-system

kubectl delete gatewayclass istio -n istio-system

![1629950489(1)](images\1629950489(1).jpg)

![1629950540(1)](images\1629950540(1).jpg)



###### Selector

matchExpressions

gateway/bookinfo-gateway-listeners-routes-namespaces-Selector-matchExpressions.yaml

kubectl apply -f bookinfo-gateway-listeners-routes-namespaces-Selector-matchExpressions.yaml -n istio-system

```
kind: Gateway
apiVersion: networking.x-k8s.io/v1alpha1
metadata:
  name: bookinfo
spec:
  gatewayClassName: istio
  listeners:  
  - hostname: "*"
    port: 80
    protocol: HTTP
    routes:
      namespaces:
        from: Selector
        selector:
          matchExpressions:
          - key: istio-injection
            operator: Exists
      selector:
        matchLabels:
          gateway: bookinfo
      kind: HTTPRoute
      group: networking.x-k8s.io
```

1. In
2. NotIn
3. Exists
4. DoesNotExist

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

gateway/productpage-http-route.yaml

kubectl apply -f productpage-http-route.yaml -n istio

```
kind: HTTPRoute
apiVersion: networking.x-k8s.io/v1alpha1
metadata:
  name: productpage
  labels:
    gateway: bookinfo
spec:
  gateways:
    allow: All
  hostnames: ["*"]
  rules:
  - matches:
    - path:
        type: Prefix
        value: /productpage
    - path:
        type: Prefix
        value: /static
    forwardTo:
    - serviceName: productpage
      port: 9080
```

访问

清理：

kubectl delete httproute productpage -n istio

kubectl delete gateways.networking.x-k8s.io bookinfo -n istio-system

kubectl delete gatewayclass istio -n istio-system

![1629950605(1)](images\1629950605(1).jpg)

![1629950660(1)](images\1629950660(1).jpg)





matchLabels

gateway/bookinfo-gateway-listeners-routes-namespaces-Selector-matchLabels.yaml

kubectl apply -f bookinfo-gateway-listeners-routes-namespaces-Selector-matchLabels.yaml -n istio-system

```
kind: Gateway
apiVersion: networking.x-k8s.io/v1alpha1
metadata:
  name: bookinfo
spec:
  gatewayClassName: istio
  listeners:  
  - hostname: "*"
    port: 80
    protocol: HTTP
    routes:
      namespaces:
        from: Selector
        selector:
          matchLabels:
            istio-injection: enabled
      selector:
        matchLabels:
          gateway: bookinfo
      kind: HTTPRoute
      group: networking.x-k8s.io
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

gateway/productpage-http-route.yaml

kubectl apply -f productpage-http-route.yaml -n istio

```
kind: HTTPRoute
apiVersion: networking.x-k8s.io/v1alpha1
metadata:
  name: productpage
  labels:
    gateway: bookinfo
spec:
  gateways:
    allow: All
  hostnames: ["*"]
  rules:
  - matches:
    - path:
        type: Prefix
        value: /productpage
    - path:
        type: Prefix
        value: /static
    forwardTo:
    - serviceName: productpage
      port: 9080
```

访问

清理：

kubectl delete httproute productpage -n istio

kubectl delete gateways.networking.x-k8s.io bookinfo -n istio-system

kubectl delete gatewayclass istio -n istio-system

![1629950744(1)](images\1629950744(1).jpg)

![1629950782(1)](images\1629950782(1).jpg)



###### Same

默认

gateway/bookinfo-gateway-listeners-routes-namespaces-Sames.yaml

kubectl apply -f bookinfo-gateway-listeners-routes-namespaces-Same.yaml -n istio-system

```
kind: Gateway
apiVersion: networking.x-k8s.io/v1alpha1
metadata:
  name: bookinfo
spec:
  gatewayClassName: istio
  listeners:  
  - hostname: "*"
    port: 80
    protocol: HTTP
    routes:
      namespaces:
        from: Same
      selector:
        matchLabels:
          gateway: bookinfo
      kind: HTTPRoute
      group: networking.x-k8s.io
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

gateway/productpage-http-route.yaml

kubectl apply -f productpage-http-route.yaml -n istio-system

```
kind: HTTPRoute
apiVersion: networking.x-k8s.io/v1alpha1
metadata:
  name: productpage
  labels:
    gateway: bookinfo
spec:
  gateways:
    allow: All
  hostnames: ["*"]
  rules:
  - matches:
    - path:
        type: Prefix
        value: /productpage
    - path:
        type: Prefix
        value: /static
    forwardTo:
    - serviceName: productpage
      port: 9080
```

访问

 HTTP ERROR 503 找不到服务

serviceName不能用长名称productpage.istio.svc.cluster.local

svc只有在istio-system名称空间下才有效

gateway，只有在istio-system名称空间才有效

清理：

kubectl delete httproute productpage -n istio

kubectl delete gateways.networking.x-k8s.io bookinfo -n istio-system

kubectl delete gatewayclass istio -n istio-system





##### selector

###### matchExpressions

gateway/bookinfo-gateway-listeners-routes-selector-matchExpressions.yaml

kubectl apply -f bookinfo-gateway-listeners-routes-selector-matchExpressions.yaml -n istio-system

```
kind: Gateway
apiVersion: networking.x-k8s.io/v1alpha1
metadata:
  name: bookinfo
spec:
  gatewayClassName: istio
  listeners:  
  - hostname: "*"
    port: 80
    protocol: HTTP
    routes:
      namespaces:
        from: All
      selector:
        matchExpressions:
        - key: gateway
          operator: In
          values:
          - bookinfo
      kind: HTTPRoute
      group: networking.x-k8s.io
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

gateway/productpage-http-route.yaml

kubectl apply -f productpage-http-route.yaml -n istio

```
kind: HTTPRoute
apiVersion: networking.x-k8s.io/v1alpha1
metadata:
  name: productpage
  labels:
    gateway: bookinfo
spec:
  gateways:
    allow: All
  hostnames: ["*"]
  rules:
  - matches:
    - path:
        type: Prefix
        value: /productpage
    - path:
        type: Prefix
        value: /static
    forwardTo:
    - serviceName: productpage
      port: 9080
```

访问



清理：

kubectl delete httproute productpage -n istio

kubectl delete gateways.networking.x-k8s.io bookinfo -n istio-system

kubectl delete gatewayclass istio -n istio-system



![1629951648(1)](images\1629951648(1).jpg)







###### matchLabels

已经使用过，略

#### tls

##### certificateRef

###### group,kind,name

gateway/bookinfo-gateway-listeners-tls-certificateRef.yaml

kubectl apply -f bookinfo-gateway-listeners-tls-certificateRef.yaml -n istio-system

```
kind: Gateway
apiVersion: networking.x-k8s.io/v1alpha1
metadata:
  name: bookinfo
spec:
  gatewayClassName: istio
  listeners:  
  - hostname: "*"
    port: 443
    protocol: HTTPS
    tls:
      options: {}
      certificateRef:
        name: bookinfo-gateway-secret
        kind: Secret
        group: core
    routes:
      namespaces:
        from: All
      selector:
        matchLabels:
          gateway: bookinfo
      kind: HTTPRoute
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

gateway/productpage-http-route.yaml

kubectl apply -f productpage-http-route.yaml -n istio

```
kind: HTTPRoute
apiVersion: networking.x-k8s.io/v1alpha1
metadata:
  name: productpage
  labels:
    gateway: bookinfo
spec:
  gateways:
    allow: All
  hostnames: ["*"]
  rules:
  - matches:
    - path:
        type: Prefix
        value: /productpage
    - path:
        type: Prefix
        value: /static
    forwardTo:
    - serviceName: productpage
      port: 9080
```

访问

清理：

kubectl delete httproute productpage -n istio

kubectl delete gateways.networking.x-k8s.io bookinfo -n istio-system

kubectl delete gatewayclass istio -n istio-system

kubectl delete -n istio-system secret bookinfo-gateway-secret

![1629952348(1)](images\1629952348(1).jpg)

![1629952401(1)](images\1629952401(1).jpg)

![1629952490(1)](images\1629952490(1).jpg)



##### mode

###### Terminate

默认

gateway/bookinfo-gateway-listeners-tls-mode-Terminate.yaml

kubectl apply -f bookinfo-gateway-listeners-tls-mode-Terminate.yaml -n istio-system

```
kind: Gateway
apiVersion: networking.x-k8s.io/v1alpha1
metadata:
  name: bookinfo
spec:
  gatewayClassName: istio
  listeners:  
  - hostname: "*"
    port: 443
    protocol: HTTPS
    tls:
      options: {}
      mode: Terminate
      certificateRef:
        name: bookinfo-gateway-secret
        kind: Secret
        group: core
    routes:
      namespaces:
        from: All
      selector:
        matchLabels:
          gateway: bookinfo
      kind: HTTPRoute
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

gateway/productpage-http-route.yaml

kubectl apply -f productpage-http-route.yaml -n istio

```
kind: HTTPRoute
apiVersion: networking.x-k8s.io/v1alpha1
metadata:
  name: productpage
  labels:
    gateway: bookinfo
spec:
  gateways:
    allow: All
  hostnames: ["*"]
  rules:
  - matches:
    - path:
        type: Prefix
        value: /productpage
    - path:
        type: Prefix
        value: /static
    forwardTo:
    - serviceName: productpage
      port: 9080
```

访问

清理：

kubectl delete httproute productpage -n istio

kubectl delete gateways.networking.x-k8s.io bookinfo -n istio-system

kubectl delete gatewayclass istio -n istio-system

![1629952904(1)](images\1629952904(1).jpg)

![1629952973(1)](images\1629952973(1).jpg)

![1629953009(1)](images\1629953009(1).jpg)

![1629953042(1)](images\1629953042(1).jpg)



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



gateway/passthough/bookinfo-gateway-listeners-tls-mode-Passthrough.yaml

kubectl apply -f bookinfo-gateway-listeners-tls-mode-Passthrough.yaml -n istio-system

```
kind: Gateway
apiVersion: networking.x-k8s.io/v1alpha1
metadata:
  name: bookinfo
spec:
  gatewayClassName: istio
  listeners:  
  - hostname: "*"
    port: 443
    protocol: TLS
    tls:
      mode: Passthrough
    routes:
      namespaces:
        from: All
      selector:
        matchLabels:
          gateway: bookinfo
      kind: TLSRoute
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
kind: TLSRoute
apiVersion: networking.x-k8s.io/v1alpha1
metadata:
  name: nginx
  labels:
    gateway: bookinfo
spec:
  gateways:
    allow: All
  rules:
  - forwardTo:
    - serviceName: my-nginx
      port: 443
```

访问

清理：

kubectl delete httproute nginx -n istio

kubectl delete gateways.networking.x-k8s.io bookinfo -n istio-system

kubectl delete gatewayclass istio -n istio-system

kubectl delete -f nginx-deploy.yaml -n istio

 kubectl delete configmap nginx-configmap -n istio

kubectl delete secret nginx-server-certs   -n istio

![1629953369(1)](images\1629953369(1).jpg)

![1629953429(1)](images\1629953429(1).jpg)

![1629953457(1)](images\1629953457(1).jpg)

##### options

gateway/bookinfo-gateway-listeners-tls-options.yaml

kubectl apply -f bookinfo-gateway-listeners-tls-options.yaml -n istio-system

```
kind: Gateway
apiVersion: networking.x-k8s.io/v1alpha1
metadata:
  name: bookinfo
spec:
  gatewayClassName: istio
  listeners:  
  - hostname: "*"
    port: 443
    protocol: HTTPS
    tls:
      options: 
        xx: yy
      mode: Terminate
      certificateRef:
        name: bookinfo-gateway-secret
        kind: Secret
        group: core
    routes:
      namespaces:
        from: All
      selector:
        matchLabels:
          gateway: bookinfo
      kind: HTTPRoute
```



##### routeOverride

###### certificate

Allow

cd certs

openssl req -x509 -sha256 -nodes -days 365 -newkey rsa:2048  -keyout cert.key -out cert.crt  -config  openssl.conf

创建secret

kubectl create -n istio-system secret tls bookinfo-gateway-secret  --key ./cert.key --cert=./cert.crt

cd http-certs

openssl req -x509 -sha256 -nodes -days 365 -newkey rsa:2048  -keyout cert.key -out cert.crt  -config  openssl.conf

创建secret

kubectl create -n istio-system secret tls bookinfo-gateway-secret-http  --key ./cert.key --cert=./cert.crt





gateway/bookinfo-gateway-listeners-tls-routeOverride-certificate-Allow.yaml

kubectl apply -f bookinfo-gateway-listeners-tls-routeOverride-certificate-Allow.yaml -n istio-system

```
kind: Gateway
apiVersion: networking.x-k8s.io/v1alpha1
metadata:
  name: bookinfo
spec:
  gatewayClassName: istio
  listeners:  
  - hostname: "*"
    port: 443
    protocol: HTTPS
    tls:
      routeOverride: 
        certificate: Allow
      mode: Terminate
      certificateRef:
        name: bookinfo-gateway-secret
        kind: Secret
        group: core 
    routes:
      namespaces:
        from: All
      selector:
        matchLabels:
          gateway: bookinfo
      kind: HTTPRoute
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

gateway/productpage-http-route-with-tls.yaml 

kubectl apply -f productpage-http-route-with-tls.yaml -n istio

```
kind: HTTPRoute
apiVersion: networking.x-k8s.io/v1alpha1
metadata:
  name: productpage
  labels:
    gateway: bookinfo
spec:
  gateways:
    allow: All
  hostnames: ["*"]
  tls:
    certificateRef:
      name: bookinfo-gateway-secret-http
      kind: Secret
      group: core  
  rules:
  - matches:
    - path:
        type: Prefix
        value: /productpage
    - path:
        type: Prefix
        value: /static
    forwardTo:
    - serviceName: productpage
      port: 9080
```

访问

清理：

kubectl delete httproute productpage -n istio

kubectl delete gateways.networking.x-k8s.io bookinfo -n istio-system

kubectl delete gatewayclass istio -n istio-system

kubectl delete -n istio-system secret bookinfo-gateway-secret

kubectl delete -n istio-system secret bookinfo-gateway-secret-http







Deny

gateway/bookinfo-gateway-listeners-tls-routeOverride-certificate-Deny.yaml

kubectl apply -f bookinfo-gateway-listeners-tls-routeOverride-certificate-Deny.yaml -n istio-system

```
kind: Gateway
apiVersion: networking.x-k8s.io/v1alpha1
metadata:
  name: bookinfo
spec:
  gatewayClassName: istio
  listeners:  
  - hostname: "*"
    port: 443
    protocol: HTTPS
    tls:
      routeOverride: 
        certificate: Deny
      mode: Terminate
      certificateRef:
        name: bookinfo-gateway-secret
        kind: Secret
        group: core
    routes:
      namespaces:
        from: All
      selector:
        matchLabels:
          gateway: bookinfo
      kind: HTTPRoute
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

gateway/productpage-http-route-with-tls.yaml 

kubectl apply -f productpage-http-route-with-tls.yaml -n istio

```
kind: HTTPRoute
apiVersion: networking.x-k8s.io/v1alpha1
metadata:
  name: productpage
  labels:
    gateway: bookinfo
spec:
  gateways:
    allow: All
  hostnames: ["*"]
  tls:
    certificateRef:
      name: bookinfo-gateway-secret-http
      kind: Secret
      group: core  
  rules:
  - matches:
    - path:
        type: Prefix
        value: /productpage
    - path:
        type: Prefix
        value: /static
    forwardTo:
    - serviceName: productpage
      port: 9080
```

访问

清理：

kubectl delete httproute productpage -n istio

kubectl delete gateways.networking.x-k8s.io bookinfo -n istio-system

kubectl delete gatewayclass istio -n istio-system

kubectl delete -n istio-system secret bookinfo-gateway-secret

kubectl delete -n istio-system secret bookinfo-gateway-secret-http

![1629954613](images\1629954613.jpg)

![1629954649(1)](images\1629954649(1).jpg)

![1629954682(1)](images\1629954682(1).jpg)

![1629954712(1)](images\1629954712(1).jpg)



##  **Routes**  

### httproutes

#### gateways

##### allow

###### All

httproute/productpage-httproute-gateways-allow-all.yaml

kubectl apply -f productpage-httproute-gateways-allow-all.yaml -n istio

```
kind: HTTPRoute
apiVersion: networking.x-k8s.io/v1alpha1
metadata:
  name: productpage
  labels:
    gateway: bookinfo
spec:
  gateways:
    allow: All
  hostnames: ["*"] 
  rules:
  - matches:
    - path:
        type: Prefix
        value: /productpage
    - path:
        type: Prefix
        value: /static
    forwardTo:
    - serviceName: productpage
      port: 9080
```

gateway/bookinfo-gateway-listeners-protocol-http.yaml

kubectl apply -f bookinfo-gateway-listeners-protocol-http.yaml -n istio-system

```
kind: Gateway
apiVersion: networking.x-k8s.io/v1alpha1
metadata:
  name: bookinfo
spec:
  gatewayClassName: istio
  listeners:  
  - hostname: "*"
    port: 80
    protocol: HTTP
    routes:
      namespaces:
        from: All
      selector:
        matchLabels:
          gateway: bookinfo
      kind: HTTPRoute
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

访问

清理：

kubectl delete httproute productpage -n istio

kubectl delete gateways.networking.x-k8s.io bookinfo -n istio-system

kubectl delete gatewayclass istio -n istio-system

![1629956695(1)](images\1629956695(1).jpg)

![1629956739(1)](images\1629956739(1).jpg)



###### FromList

httproute/productpage-httproute-gateways-allow-FromList.yaml

kubectl apply -f productpage-httproute-gateways-allow-FromList.yaml -n istio

```
kind: HTTPRoute
apiVersion: networking.x-k8s.io/v1alpha1
metadata:
  name: productpage
  labels:
    gateway: bookinfo
spec:
  gateways:
    allow: FromList
    gatewayRefs:
    - namespace: istio-system
      name: bookinfo
  hostnames: ["*"] 
  rules:
  - matches:
    - path:
        type: Prefix
        value: /productpage
    - path:
        type: Prefix
        value: /static
    forwardTo:
    - serviceName: productpage
      port: 9080
```

gateway/bookinfo-gateway-listeners-protocol-http.yaml

kubectl apply -f bookinfo-gateway-listeners-protocol-http.yaml -n istio-system

```
kind: Gateway
apiVersion: networking.x-k8s.io/v1alpha1
metadata:
  name: bookinfo
spec:
  gatewayClassName: istio
  listeners:  
  - hostname: "*"
    port: 80
    protocol: HTTP
    routes:
      namespaces:
        from: All
      selector:
        matchLabels:
          gateway: bookinfo
      kind: HTTPRoute
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

访问

清理：

kubectl delete httproute productpage -n istio

kubectl delete gateways.networking.x-k8s.io bookinfo -n istio-system

kubectl delete gatewayclass istio -n istio-system

![1629956802(1)](images\1629956802(1).jpg)

![1629956861(1)](images\1629956861(1).jpg)





###### SameNamespace

默认

httproute/productpage-httproute-gateways-allow-SameNamespace.yaml

kubectl apply -f productpage-httproute-gateways-allow-SameNamespace.yaml -n istio-system

```
kind: HTTPRoute
apiVersion: networking.x-k8s.io/v1alpha1
metadata:
  name: productpage
  labels:
    gateway: bookinfo
spec:
  gateways:
    allow: SameNamespace
  hostnames: ["*"] 
  rules:
  - matches:
    - path:
        type: Prefix
        value: /productpage
    - path:
        type: Prefix
        value: /static
    forwardTo:
    - serviceName: productpage
      port: 9080
```

gateway/bookinfo-gateway-listeners-protocol-http.yaml

kubectl apply -f bookinfo-gateway-listeners-protocol-http.yaml -n istio-system

```
kind: Gateway
apiVersion: networking.x-k8s.io/v1alpha1
metadata:
  name: bookinfo
spec:
  gatewayClassName: istio
  listeners:  
  - hostname: "*"
    port: 80
    protocol: HTTP
    routes:
      namespaces:
        from: All
      selector:
        matchLabels:
          gateway: bookinfo
      kind: HTTPRoute
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

访问 404

serviceName不能用长名称productpage.istio.svc.cluster.local

svc只有在istio-system名称空间下才有效

gateway，只有在istio-system名称空间才有效

清理：

kubectl delete httproute productpage -n istio

kubectl delete gateways.networking.x-k8s.io bookinfo -n istio-system

kubectl delete gatewayclass istio -n istio-system



##### gatewayRefs

###### name，namespace

上面已有示例

#### hostnames

##### 所有

httproute/productpage-httproute-hostnames-star.yaml

kubectl apply -f productpage-httproute-hostnames-star.yaml -n istio

```
kind: HTTPRoute
apiVersion: networking.x-k8s.io/v1alpha1
metadata:
  name: productpage
  labels:
    gateway: bookinfo
spec:
  gateways:
    allow: All
  hostnames: ["*"] 
  rules:
  - matches:
    - path:
        type: Prefix
        value: /productpage
    - path:
        type: Prefix
        value: /static
    forwardTo:
    - serviceName: productpage
      port: 9080
```

gateway/bookinfo-gateway-listeners-protocol-http.yaml

kubectl apply -f bookinfo-gateway-listeners-protocol-http.yaml -n istio-system

```
kind: Gateway
apiVersion: networking.x-k8s.io/v1alpha1
metadata:
  name: bookinfo
spec:
  gatewayClassName: istio
  listeners:  
  - hostname: "*"
    port: 80
    protocol: HTTP
    routes:
      namespaces:
        from: All
      selector:
        matchLabels:
          gateway: bookinfo
      kind: HTTPRoute
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

访问

清理：

kubectl delete httproute productpage -n istio

kubectl delete gateways.networking.x-k8s.io bookinfo -n istio-system

kubectl delete gatewayclass istio -n istio-system

![1629957528(1)](images\1629957528(1).jpg)



![1629957591(1)](images\1629957591(1).jpg)



##### 具体

httproute/productpage-httproute-hostnames-specific.yaml

kubectl apply -f productpage-httproute-hostnames-specific.yaml -n istio

```
kind: HTTPRoute
apiVersion: networking.x-k8s.io/v1alpha1
metadata:
  name: productpage
  labels:
    gateway: bookinfo
spec:
  gateways:
    allow: All
  hostnames: ["bookinfo.com"] 
  rules:
  - matches:
    - path:
        type: Prefix
        value: /productpage
    - path:
        type: Prefix
        value: /static
    forwardTo:
    - serviceName: productpage
      port: 9080
```

gateway/bookinfo-gateway-listeners-protocol-http.yaml

kubectl apply -f bookinfo-gateway-listeners-protocol-http.yaml -n istio-system

```
kind: Gateway
apiVersion: networking.x-k8s.io/v1alpha1
metadata:
  name: bookinfo
spec:
  gatewayClassName: istio
  listeners:  
  - hostname: "*"
    port: 80
    protocol: HTTP
    routes:
      namespaces:
        from: All
      selector:
        matchLabels:
          gateway: bookinfo
      kind: HTTPRoute
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

访问

清理：

kubectl delete httproute productpage -n istio

kubectl delete gateways.networking.x-k8s.io bookinfo -n istio-system

kubectl delete gatewayclass istio -n istio-system

![1629957638(1)](images\1629957638(1).jpg)

![1629957687(1)](images\1629957687(1).jpg)



##### semi-star

httproute/productpage-httproute-hostnames-semi-star.yaml

kubectl apply -f productpage-httproute-hostnames-semi-star.yaml -n istio

```
kind: HTTPRoute
apiVersion: networking.x-k8s.io/v1alpha1
metadata:
  name: productpage
  labels:
    gateway: bookinfo
spec:
  gateways:
    allow: All
  hostnames: ["*.demo"] 
  rules:
  - matches:
    - path:
        type: Prefix
        value: /productpage
    - path:
        type: Prefix
        value: /static
    forwardTo:
    - serviceName: productpage
      port: 9080
```

gateway/bookinfo-gateway-listeners-protocol-http.yaml

kubectl apply -f bookinfo-gateway-listeners-protocol-http.yaml -n istio-system

```
kind: Gateway
apiVersion: networking.x-k8s.io/v1alpha1
metadata:
  name: bookinfo
spec:
  gatewayClassName: istio
  listeners:  
  - hostname: "*"
    port: 80
    protocol: HTTP
    routes:
      namespaces:
        from: All
      selector:
        matchLabels:
          gateway: bookinfo
      kind: HTTPRoute
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

访问

vi /etc/hosts

192.168.198.199 bookinfo.demo

curl http://bookinfo.demo:80/productpage -I

清理：

kubectl delete httproute productpage -n istio

kubectl delete gateways.networking.x-k8s.io bookinfo -n istio-system

kubectl delete gatewayclass istio -n istio-system



#### rules

##### filters

###### extensionRef

group，kind，name



###### requestHeaderModifier

add

httproute/productpage-httproute-filters-requestHeaderModifier-add.yaml

kubectl apply -f productpage-httproute-filters-requestHeaderModifier-add.yaml -n istio

```
kind: HTTPRoute
apiVersion: networking.x-k8s.io/v1alpha1
metadata:
  name: productpage
  labels:
    gateway: bookinfo
spec:
  gateways:
    allow: All
  hostnames: ["*"] 
  rules:
  - filters:
    - requestHeaderModifier:
        add:
          xx: yy
      type: RequestHeaderModifier
    matches:
    - path:
        type: Prefix
        value: /productpage
    - path:
        type: Prefix
        value: /static
    forwardTo:
    - serviceName: productpage
      port: 9080
```

gateway/bookinfo-gateway-listeners-protocol-http.yaml

kubectl apply -f bookinfo-gateway-listeners-protocol-http.yaml -n istio-system

```
kind: Gateway
apiVersion: networking.x-k8s.io/v1alpha1
metadata:
  name: bookinfo
spec:
  gatewayClassName: istio
  listeners:  
  - hostname: "*"
    port: 80
    protocol: HTTP
    routes:
      namespaces:
        from: All
      selector:
        matchLabels:
          gateway: bookinfo
      kind: HTTPRoute
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

访问

清理：

kubectl delete httproute productpage -n istio

kubectl delete gateways.networking.x-k8s.io bookinfo -n istio-system

kubectl delete gatewayclass istio -n istio-system

![1629958686](images\1629958686.jpg)

![1629958737(1)](images\1629958737(1).jpg)



remove

httproute/productpage-httproute-filters-requestHeaderModifier-remove.yaml

kubectl apply -f productpage-httproute-filters-requestHeaderModifier-remove.yaml -n istio

```
kind: HTTPRoute
apiVersion: networking.x-k8s.io/v1alpha1
metadata:
  name: productpage
  labels:
    gateway: bookinfo
spec:
  gateways:
    allow: All
  hostnames: ["*"] 
  rules:
  - filters:
    - requestHeaderModifier:
        remove:
        - accept
      type: RequestHeaderModifier
    matches:
    - path:
        type: Prefix
        value: /productpage
    - path:
        type: Prefix
        value: /static
    forwardTo:
    - serviceName: productpage
      port: 9080
```

gateway/bookinfo-gateway-listeners-protocol-http.yaml

kubectl apply -f bookinfo-gateway-listeners-protocol-http.yaml -n istio-system

```
kind: Gateway
apiVersion: networking.x-k8s.io/v1alpha1
metadata:
  name: bookinfo
spec:
  gatewayClassName: istio
  listeners:  
  - hostname: "*"
    port: 80
    protocol: HTTP
    routes:
      namespaces:
        from: All
      selector:
        matchLabels:
          gateway: bookinfo
      kind: HTTPRoute
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

访问

清理：

kubectl delete httproute productpage -n istio

kubectl delete gateways.networking.x-k8s.io bookinfo -n istio-system

kubectl delete gatewayclass istio -n istio-system

![1629958808(1)](images\1629958808(1).jpg)

![1629958866(1)](images\1629958866(1).jpg)





set

httproute/productpage-httproute-filters-requestHeaderModifier-set.yaml

kubectl apply -f productpage-httproute-filters-requestHeaderModifier-set.yaml -n istio

```
kind: HTTPRoute
apiVersion: networking.x-k8s.io/v1alpha1
metadata:
  name: productpage
  labels:
    gateway: bookinfo
spec:
  gateways:
    allow: All
  hostnames: ["*"] 
  rules:
  - filters:
    - requestHeaderModifier:
        set:
          accept: text/html
          accept-encoding: gzip
      type: RequestHeaderModifier
    matches:
    - path:
        type: Prefix
        value: /productpage
    - path:
        type: Prefix
        value: /static
    forwardTo:
    - serviceName: productpage
      port: 9080
```

gateway/bookinfo-gateway-listeners-protocol-http.yaml

kubectl apply -f bookinfo-gateway-listeners-protocol-http.yaml -n istio-system

```
kind: Gateway
apiVersion: networking.x-k8s.io/v1alpha1
metadata:
  name: bookinfo
spec:
  gatewayClassName: istio
  listeners:  
  - hostname: "*"
    port: 80
    protocol: HTTP
    routes:
      namespaces:
        from: All
      selector:
        matchLabels:
          gateway: bookinfo
      kind: HTTPRoute
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

访问

清理：

kubectl delete httproute productpage -n istio

kubectl delete gateways.networking.x-k8s.io bookinfo -n istio-system

kubectl delete gatewayclass istio -n istio-system

![1629958916(1)](images\1629958916(1).jpg)

![1629958952(1)](images\1629958952(1).jpg)



###### requestMirror

backendRef

group，kind，name

httproute/productpage-httproute-filters-requestMirror-backendRef-group.yaml

kubectl apply -f productpage-httproute-filters-requestMirror-backendRef-group.yaml  -n istio

```
kind: HTTPRoute
apiVersion: networking.x-k8s.io/v1alpha1
metadata:
  name: productpage
  labels:
    gateway: bookinfo
spec:
  gateways:
    allow: All
  hostnames: ["*"] 
  rules:
  - filters:
    - requestMirror:
        backendRef:
          group: core
          kind: Service
          name: productpage-v2
      type: RequestMirror
    matches:
    - path:
        type: Prefix
        value: /productpage
    - path:
        type: Prefix
        value: /static
    forwardTo:
    - serviceName: productpage
      port: 9080
```

httproute/productpage-deploy-v2.yaml

kubectl apply -f productpage-deploy-v2.yaml -n istio

```
apiVersion: v1
kind: Service
metadata:
  name: productpage-v2
  labels:
    app: productpage
    service: productpage
spec:
  ports:
  - port: 9080
    name: http
  selector:
    app: productpage-v2
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
```

gateway/bookinfo-gateway-listeners-protocol-http.yaml

kubectl apply -f bookinfo-gateway-listeners-protocol-http.yaml -n istio-system

```
kind: Gateway
apiVersion: networking.x-k8s.io/v1alpha1
metadata:
  name: bookinfo
spec:
  gatewayClassName: istio
  listeners:  
  - hostname: "*"
    port: 80
    protocol: HTTP
    routes:
      namespaces:
        from: All
      selector:
        matchLabels:
          gateway: bookinfo
      kind: HTTPRoute
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

访问

unsupported filter type "RequestMirror"

清理：

kubectl delete httproute productpage -n istio

kubectl delete gateways.networking.x-k8s.io bookinfo -n istio-system

kubectl delete gatewayclass istio -n istio-system

kubectl delete -f productpage-deploy-v2.yaml -n istio



port



serviceName





###### type

已使用



##### forwardTo

###### backendRef

group，kind，name

httproute/productpage-httproute-forwardTo-backendRef-group.yaml

kubectl apply -f productpage-httproute-forwardTo-backendRef-group.yaml  -n istio

```
kind: HTTPRoute
apiVersion: networking.x-k8s.io/v1alpha1
metadata:
  name: productpage
  labels:
    gateway: bookinfo
spec:
  gateways:
    allow: All
  hostnames: ["*"] 
  rules:
  - matches:
    - path:
        type: Prefix
        value: /productpage
    - path:
        type: Prefix
        value: /static
    forwardTo:
    - backendRef: 
        group: core
        kind: Service
        name: productpage
      port: 9080
```

gateway/bookinfo-gateway-listeners-protocol-http.yaml

kubectl apply -f bookinfo-gateway-listeners-protocol-http.yaml -n istio-system

```
kind: Gateway
apiVersion: networking.x-k8s.io/v1alpha1
metadata:
  name: bookinfo
spec:
  gatewayClassName: istio
  listeners:  
  - hostname: "*"
    port: 80
    protocol: HTTP
    routes:
      namespaces:
        from: All
      selector:
        matchLabels:
          gateway: bookinfo
      kind: HTTPRoute
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

访问

kubectl describe httproutes.networking.x-k8s.io productpage -n istio

referencing unsupported destination; backendRef is not supported



清理：

kubectl delete httproute productpage -n istio

kubectl delete gateways.networking.x-k8s.io bookinfo -n istio-system

kubectl delete gatewayclass istio -n istio-system



###### filters

extensionRef



requestHeaderModifier

add

httproute/productpage-httproute-forwardTo-filters-requestHeaderModifier-add.yaml

kubectl apply -f productpage-httproute-forwardTo-filters-requestHeaderModifier-add.yaml  -n istio

```
kind: HTTPRoute
apiVersion: networking.x-k8s.io/v1alpha1
metadata:
  name: productpage
  labels:
    gateway: bookinfo
spec:
  gateways:
    allow: All
  hostnames: ["*"] 
  rules:
  - matches:
    - path:
        type: Prefix
        value: /productpage
    - path:
        type: Prefix
        value: /static
    forwardTo:
    - serviceName: productpage
      port: 9080
      filters:
      - requestHeaderModifier:
         add:
           xx: yy
        type: RequestHeaderModifier
          
```

gateway/bookinfo-gateway-listeners-protocol-http.yaml

kubectl apply -f bookinfo-gateway-listeners-protocol-http.yaml -n istio-system

```
kind: Gateway
apiVersion: networking.x-k8s.io/v1alpha1
metadata:
  name: bookinfo
spec:
  gatewayClassName: istio
  listeners:  
  - hostname: "*"
    port: 80
    protocol: HTTP
    routes:
      namespaces:
        from: All
      selector:
        matchLabels:
          gateway: bookinfo
      kind: HTTPRoute
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

访问



清理：

kubectl delete httproute productpage -n istio

kubectl delete gateways.networking.x-k8s.io bookinfo -n istio-system

kubectl delete gatewayclass istio -n istio-system

![1629959393(1)](images\1629959393(1).jpg)

![1629959464(1)](images\1629959464(1).jpg)



remove

httproute/productpage-httproute-forwardTo-filters-requestHeaderModifier-remove.yaml

kubectl apply -f productpage-httproute-forwardTo-filters-requestHeaderModifier-remove.yaml  -n istio

```
kind: HTTPRoute
apiVersion: networking.x-k8s.io/v1alpha1
metadata:
  name: productpage
  labels:
    gateway: bookinfo
spec:
  gateways:
    allow: All
  hostnames: ["*"] 
  rules:
  - matches:
    - path:
        type: Prefix
        value: /productpage
    - path:
        type: Prefix
        value: /static
    forwardTo:
    - serviceName: productpage
      port: 9080
      filters:
      - requestHeaderModifier:
         remove:
         - accept
        type: RequestHeaderModifier
          
```

gateway/bookinfo-gateway-listeners-protocol-http.yaml

kubectl apply -f bookinfo-gateway-listeners-protocol-http.yaml -n istio-system

```
kind: Gateway
apiVersion: networking.x-k8s.io/v1alpha1
metadata:
  name: bookinfo
spec:
  gatewayClassName: istio
  listeners:  
  - hostname: "*"
    port: 80
    protocol: HTTP
    routes:
      namespaces:
        from: All
      selector:
        matchLabels:
          gateway: bookinfo
      kind: HTTPRoute
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

访问



清理：

kubectl delete httproute productpage -n istio

kubectl delete gateways.networking.x-k8s.io bookinfo -n istio-system

kubectl delete gatewayclass istio -n istio-system

![1629959506(1)](images\1629959506(1).jpg)



![1629959548(1)](images\1629959548(1).jpg)



set

httproute/productpage-httproute-forwardTo-filters-requestHeaderModifier-set.yaml

kubectl apply -f productpage-httproute-forwardTo-filters-requestHeaderModifier-set.yaml  -n istio

```
kind: HTTPRoute
apiVersion: networking.x-k8s.io/v1alpha1
metadata:
  name: productpage
  labels:
    gateway: bookinfo
spec:
  gateways:
    allow: All
  hostnames: ["*"] 
  rules:
  - matches:
    - path:
        type: Prefix
        value: /productpage
    - path:
        type: Prefix
        value: /static
    forwardTo:
    - serviceName: productpage
      port: 9080
      filters:
      - requestHeaderModifier:
          set:
            accept: text/html
            accept-encoding: gzip
        type: RequestHeaderModifier
          
```

gateway/bookinfo-gateway-listeners-protocol-http.yaml

kubectl apply -f bookinfo-gateway-listeners-protocol-http.yaml -n istio-system

```
kind: Gateway
apiVersion: networking.x-k8s.io/v1alpha1
metadata:
  name: bookinfo
spec:
  gatewayClassName: istio
  listeners:  
  - hostname: "*"
    port: 80
    protocol: HTTP
    routes:
      namespaces:
        from: All
      selector:
        matchLabels:
          gateway: bookinfo
      kind: HTTPRoute
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

访问



清理：

kubectl delete httproute productpage -n istio

kubectl delete gateways.networking.x-k8s.io bookinfo -n istio-system

kubectl delete gatewayclass istio -n istio-system

![1629959599(1)](images\1629959599(1).jpg)

![1629959648(1)](images\1629959648(1).jpg)



requestMirror

backendRef

httproute/productpage-httproute-forwardTo-filters-requestMirror-backendRef-group.yaml

kubectl apply -f productpage-httproute-forwardTo-filters-requestMirror-backendRef-group.yaml  -n istio

```
kind: HTTPRoute
apiVersion: networking.x-k8s.io/v1alpha1
metadata:
  name: productpage
  labels:
    gateway: bookinfo
spec:
  gateways:
    allow: All
  hostnames: ["*"] 
  rules:
  - matches:
    - path:
        type: Prefix
        value: /productpage
    - path:
        type: Prefix
        value: /static
    forwardTo:
    - serviceName: productpage
      port: 9080
      filters:
      - requestMirror:
          backendRef:
            group: core
            kind: Service
            name: productpage-v2
          port: 9080
        type: RequestMirror
```

httproute/productpage-deploy-v2.yaml

kubectl apply -f productpage-deploy-v2.yaml -n istio

```
apiVersion: v1
kind: Service
metadata:
  name: productpage-v2
  labels:
    app: productpage
    service: productpage
spec:
  ports:
  - port: 9080
    name: http
  selector:
    app: productpage-v2
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
```

gateway/bookinfo-gateway-listeners-protocol-http.yaml

kubectl apply -f bookinfo-gateway-listeners-protocol-http.yaml -n istio-system

```
kind: Gateway
apiVersion: networking.x-k8s.io/v1alpha1
metadata:
  name: bookinfo
spec:
  gatewayClassName: istio
  listeners:  
  - hostname: "*"
    port: 80
    protocol: HTTP
    routes:
      namespaces:
        from: All
      selector:
        matchLabels:
          gateway: bookinfo
      kind: HTTPRoute
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

访问

kubectl describe httproutes.networking.x-k8s.io -n istio productpage 

unsupported filter type "RequestMirror"

清理：

kubectl delete httproute productpage -n istio

kubectl delete gateways.networking.x-k8s.io bookinfo -n istio-system

kubectl delete gatewayclass istio -n istio-system

kubectl delete -f productpage-deploy-v2.yaml -n istio



port



servicename







type



###### port，serviceName

httproute/productpage-httproute-forwardTo-port-serviceName.yaml

kubectl apply -f productpage-httproute-forwardTo-port-serviceName.yaml -n istio

```
kind: HTTPRoute
apiVersion: networking.x-k8s.io/v1alpha1
metadata:
  name: productpage
  labels:
    gateway: bookinfo
spec:
  gateways:
    allow: All
  hostnames: ["*"] 
  rules:
  - matches:
    - path:
        type: Prefix
        value: /productpage
    - path:
        type: Prefix
        value: /static
    forwardTo:
    - serviceName: productpage
      port: 9080
```

gateway/bookinfo-gateway-listeners-protocol-http.yaml

kubectl apply -f bookinfo-gateway-listeners-protocol-http.yaml -n istio-system

```
kind: Gateway
apiVersion: networking.x-k8s.io/v1alpha1
metadata:
  name: bookinfo
spec:
  gatewayClassName: istio
  listeners:  
  - hostname: "*"
    port: 80
    protocol: HTTP
    routes:
      namespaces:
        from: All
      selector:
        matchLabels:
          gateway: bookinfo
      kind: HTTPRoute
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

访问

清理：

kubectl delete httproute productpage -n istio

kubectl delete gateways.networking.x-k8s.io bookinfo -n istio-system

kubectl delete gatewayclass istio -n istio-system

![1629959771(1)](images\1629959771(1).jpg)

![1629959801(1)](images\1629959801(1).jpg)



###### weight

httproute/productpage-httproute-forwardTo-weight.yaml

kubectl apply -f productpage-httproute-forwardTo-weight.yaml  -n istio

```
kind: HTTPRoute
apiVersion: networking.x-k8s.io/v1alpha1
metadata:
  name: productpage
  labels:
    gateway: bookinfo
spec:
  gateways:
    allow: All
  hostnames: ["*"] 
  rules:
  - matches:
    - path:
        type: Prefix
        value: /productpage
    - path:
        type: Prefix
        value: /static
    forwardTo:
    - serviceName: productpage
      port: 9080
      weight: 20
    - serviceName: productpage-v2
      port: 9080
      weight: 80
```

httproute/productpage-deploy-v2.yaml

kubectl apply -f productpage-deploy-v2.yaml -n istio

```
apiVersion: v1
kind: Service
metadata:
  name: productpage-v2
  labels:
    app: productpage
    service: productpage
spec:
  ports:
  - port: 9080
    name: http
  selector:
    app: productpage-v2
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
```

gateway/bookinfo-gateway-listeners-protocol-http.yaml

kubectl apply -f bookinfo-gateway-listeners-protocol-http.yaml -n istio-system

```
kind: Gateway
apiVersion: networking.x-k8s.io/v1alpha1
metadata:
  name: bookinfo
spec:
  gatewayClassName: istio
  listeners:  
  - hostname: "*"
    port: 80
    protocol: HTTP
    routes:
      namespaces:
        from: All
      selector:
        matchLabels:
          gateway: bookinfo
      kind: HTTPRoute
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

访问

kubectl logs -f -n istio productpage-v2-7b59b6d467-4t2tw 



清理：

kubectl delete httproute productpage -n istio

kubectl delete gateways.networking.x-k8s.io bookinfo -n istio-system

kubectl delete gatewayclass istio -n istio-system

kubectl delete -f productpage-deploy-v2.yaml -n istio

![1629959856(1)](images\1629959856(1).jpg)

![1629959902(1)](images\1629959902(1).jpg)



##### matches

###### extensionRef

group，kind，name





###### headers

type,values

Exact

httproute/productpage-httproute-rules-matches-headers-type-Exact.yaml

kubectl apply -f productpage-httproute-rules-matches-headers-type-Exact.yaml -n istio

```
kind: HTTPRoute
apiVersion: networking.x-k8s.io/v1alpha1
metadata:
  name: productpage
  labels:
    gateway: bookinfo
spec:
  gateways:
    allow: All
  hostnames: ["*"] 
  rules:
  - matches:
    - headers:
        type: Exact
        values:
          test: test
    forwardTo:
    - serviceName: productpage
      port: 9080
```

gateway/bookinfo-gateway-listeners-protocol-http.yaml

kubectl apply -f bookinfo-gateway-listeners-protocol-http.yaml -n istio-system

```
kind: Gateway
apiVersion: networking.x-k8s.io/v1alpha1
metadata:
  name: bookinfo
spec:
  gatewayClassName: istio
  listeners:  
  - hostname: "*"
    port: 80
    protocol: HTTP
    routes:
      namespaces:
        from: All
      selector:
        matchLabels:
          gateway: bookinfo
      kind: HTTPRoute
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

访问

curl https://bookinfo.demo:31396/productpage -k  -H "test:test"

清理：

kubectl delete httproute productpage -n istio

kubectl delete gateways.networking.x-k8s.io bookinfo -n istio-system

kubectl delete gatewayclass istio -n istio-system

![1629960117(1)](images\1629960117(1).jpg)



![1629960186(1)](images\1629960186(1).jpg)



RegularExpression

httproute/productpage-httproute-rules-matches-headers-type-RegularExpression.yaml

kubectl apply -f productpage-httproute-rules-matches-headers-type-RegularExpression.yaml -n istio

```
kind: HTTPRoute
apiVersion: networking.x-k8s.io/v1alpha1
metadata:
  name: productpage
  labels:
    gateway: bookinfo
spec:
  gateways:
    allow: All
  hostnames: ["*"] 
  rules:
  - matches:
    - headers:
        type: RegularExpression
        values:
          test: "test.*"
    forwardTo:
    - serviceName: productpage
      port: 9080
```

gateway/bookinfo-gateway-listeners-protocol-http.yaml

kubectl apply -f bookinfo-gateway-listeners-protocol-http.yaml -n istio-system

```
kind: Gateway
apiVersion: networking.x-k8s.io/v1alpha1
metadata:
  name: bookinfo
spec:
  gatewayClassName: istio
  listeners:  
  - hostname: "*"
    port: 80
    protocol: HTTP
    routes:
      namespaces:
        from: All
      selector:
        matchLabels:
          gateway: bookinfo
      kind: HTTPRoute
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

访问

curl https://bookinfo.demo:31396/productpage -k  -H "test:test2"

清理：

kubectl delete httproute productpage -n istio

kubectl delete gateways.networking.x-k8s.io bookinfo -n istio-system

kubectl delete gatewayclass istio -n istio-system

![1629960245(1)](images\1629960245(1).jpg)

![1629960286(1)](images\1629960286(1).jpg)



ImplementationSpecific

这是啥





###### path

type，value

Exact

httproute/productpage-httproute-rules-matches-path-Exact.yaml

kubectl apply -f productpage-httproute-rules-matches-path-Exact.yaml -n istio

```
kind: HTTPRoute
apiVersion: networking.x-k8s.io/v1alpha1
metadata:
  name: productpage
  labels:
    gateway: bookinfo
spec:
  gateways:
    allow: All
  hostnames: ["*"] 
  rules:
  - matches:
    - path:
        type: Exact
        value: /productpage
    - path:
        type: Prefix
        value: /static
    forwardTo:
    - serviceName: productpage
      port: 9080
```

gateway/bookinfo-gateway-listeners-protocol-http.yaml

kubectl apply -f bookinfo-gateway-listeners-protocol-http.yaml -n istio-system

```
kind: Gateway
apiVersion: networking.x-k8s.io/v1alpha1
metadata:
  name: bookinfo
spec:
  gatewayClassName: istio
  listeners:  
  - hostname: "*"
    port: 80
    protocol: HTTP
    routes:
      namespaces:
        from: All
      selector:
        matchLabels:
          gateway: bookinfo
      kind: HTTPRoute
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

访问

清理：

kubectl delete httproute productpage -n istio

kubectl delete gateways.networking.x-k8s.io bookinfo -n istio-system

kubectl delete gatewayclass istio -n istio-system

![1629963486(1)](images\1629963486(1).jpg)

![1629963534(1)](images\1629963534(1).jpg)





Prefix

httproute/productpage-httproute-rules-matches-path-Prefix.yaml

kubectl apply -f productpage-httproute-rules-matches-path-Prefix.yaml -n istio

```
kind: HTTPRoute
apiVersion: networking.x-k8s.io/v1alpha1
metadata:
  name: productpage
  labels:
    gateway: bookinfo
spec:
  gateways:
    allow: All
  hostnames: ["*"] 
  rules:
  - matches:
    - path:
        type: Prefix
        value: /productpage
    - path:
        type: Prefix
        value: /static
    forwardTo:
    - serviceName: productpage
      port: 9080
```

gateway/bookinfo-gateway-listeners-protocol-http.yaml

kubectl apply -f bookinfo-gateway-listeners-protocol-http.yaml -n istio-system

```
kind: Gateway
apiVersion: networking.x-k8s.io/v1alpha1
metadata:
  name: bookinfo
spec:
  gatewayClassName: istio
  listeners:  
  - hostname: "*"
    port: 80
    protocol: HTTP
    routes:
      namespaces:
        from: All
      selector:
        matchLabels:
          gateway: bookinfo
      kind: HTTPRoute
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

访问

清理：

kubectl delete httproute productpage -n istio

kubectl delete gateways.networking.x-k8s.io bookinfo -n istio-system

kubectl delete gatewayclass istio -n istio-system

![1629963578(1)](images\1629963578(1).jpg)

![1629963620(1)](images\1629963620(1).jpg)





RegularExpression

httproute/productpage-httproute-rules-matches-path-RegularExpression.yaml

kubectl apply -f productpage-httproute-rules-matches-path-RegularExpression.yaml -n istio

```
kind: HTTPRoute
apiVersion: networking.x-k8s.io/v1alpha1
metadata:
  name: productpage
  labels:
    gateway: bookinfo
spec:
  gateways:
    allow: All
  hostnames: ["*"] 
  rules:
  - matches:
    - path:
        type: RegularExpression
        value: "/product.*"
    - path:
        type: Prefix
        value: /static
    forwardTo:
    - serviceName: productpage
      port: 9080
```

gateway/bookinfo-gateway-listeners-protocol-http.yaml

kubectl apply -f bookinfo-gateway-listeners-protocol-http.yaml -n istio-system

```
kind: Gateway
apiVersion: networking.x-k8s.io/v1alpha1
metadata:
  name: bookinfo
spec:
  gatewayClassName: istio
  listeners:  
  - hostname: "*"
    port: 80
    protocol: HTTP
    routes:
      namespaces:
        from: All
      selector:
        matchLabels:
          gateway: bookinfo
      kind: HTTPRoute
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

访问

清理：

kubectl delete httproute productpage -n istio

kubectl delete gateways.networking.x-k8s.io bookinfo -n istio-system

kubectl delete gatewayclass istio -n istio-system

![1629963661(1)](images\1629963661(1).jpg)

![1629963695(1)](images\1629963695(1).jpg)



ImplementationSpecific





###### queryParams

type，values

Exact

httproute/productpage-httproute-rules-matches-queryParams-Exact.yaml

kubectl apply -f productpage-httproute-rules-matches-queryParams-Exact.yaml  -n istio

```
kind: HTTPRoute
apiVersion: networking.x-k8s.io/v1alpha1
metadata:
  name: productpage
  labels:
    gateway: bookinfo
spec:
  gateways:
    allow: All
  hostnames: ["*"] 
  rules:
  - matches:
    - queryParams:
        type: Exact
        values:
          test: test
    - path:
        type: Prefix
        value: /static
    forwardTo:
    - serviceName: productpage
      port: 9080
```

gateway/bookinfo-gateway-listeners-protocol-http.yaml

kubectl apply -f bookinfo-gateway-listeners-protocol-http.yaml -n istio-system

```
kind: Gateway
apiVersion: networking.x-k8s.io/v1alpha1
metadata:
  name: bookinfo
spec:
  gatewayClassName: istio
  listeners:  
  - hostname: "*"
    port: 80
    protocol: HTTP
    routes:
      namespaces:
        from: All
      selector:
        matchLabels:
          gateway: bookinfo
      kind: HTTPRoute
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

访问

curl https://bookinfo.demo:31396/productpage?test=test -k

清理：

kubectl delete httproute productpage -n istio

kubectl delete gateways.networking.x-k8s.io bookinfo -n istio-system

kubectl delete gatewayclass istio -n istio-system

![1629963741(1)](images\1629963741(1).jpg)

![1629963782(1)](images\1629963782(1).jpg)





RegularExpression

httproute/productpage-httproute-rules-matches-queryParams-RegularExpression.yaml

kubectl apply -f productpage-httproute-rules-matches-queryParams-RegularExpression.yaml  -n istio

```
kind: HTTPRoute
apiVersion: networking.x-k8s.io/v1alpha1
metadata:
  name: productpage
  labels:
    gateway: bookinfo
spec:
  gateways:
    allow: All
  hostnames: ["*"] 
  rules:
  - matches:
    - queryParams:
        type: RegularExpression
        values:
          test: "test.*"
    - path:
        type: Prefix
        value: /static
    forwardTo:
    - serviceName: productpage
      port: 9080
```

gateway/bookinfo-gateway-listeners-protocol-http.yaml

kubectl apply -f bookinfo-gateway-listeners-protocol-http.yaml -n istio-system

```
kind: Gateway
apiVersion: networking.x-k8s.io/v1alpha1
metadata:
  name: bookinfo
spec:
  gatewayClassName: istio
  listeners:  
  - hostname: "*"
    port: 80
    protocol: HTTP
    routes:
      namespaces:
        from: All
      selector:
        matchLabels:
          gateway: bookinfo
      kind: HTTPRoute
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

访问

curl https://bookinfo.demo:31396/productpage?test=test2 -k

清理：

kubectl delete httproute productpage -n istio

kubectl delete gateways.networking.x-k8s.io bookinfo -n istio-system

kubectl delete gatewayclass istio -n istio-system

![1629963826(1)](images\1629963826(1).jpg)

![1629963870(1)](images\1629963870(1).jpg)





ImplementationSpecific





#### tls

##### certificateRef

###### group，kind，name

gateway/bookinfo-gateway-listeners-tls-routeOverride-certificate-Allow.yaml

kubectl apply -f bookinfo-gateway-listeners-tls-routeOverride-certificate-Allow.yaml -n istio-system

```
kind: Gateway
apiVersion: networking.x-k8s.io/v1alpha1
metadata:
  name: bookinfo
spec:
  gatewayClassName: istio
  listeners:  
  - hostname: "*"
    port: 443
    protocol: HTTPS
    tls:
      routeOverride: 
        certificate: Allow
      mode: Terminate
      certificateRef:
        name: bookinfo-gateway-secret
        kind: Secret
        group: core 
    routes:
      namespaces:
        from: All
      selector:
        matchLabels:
          gateway: bookinfo
      kind: HTTPRoute
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

httproute/productpage-http-route-with-tls.yaml 

kubectl apply -f productpage-http-route-with-tls.yaml -n istio

```
kind: HTTPRoute
apiVersion: networking.x-k8s.io/v1alpha1
metadata:
  name: productpage
  labels:
    gateway: bookinfo
spec:
  gateways:
    allow: All
  hostnames: ["*"]
  tls:
    certificateRef:
      name: bookinfo-gateway-secret-http
      kind: Secret
      group: core  
  rules:
  - matches:
    - path:
        type: Prefix
        value: /productpage
    - path:
        type: Prefix
        value: /static
    forwardTo:
    - serviceName: productpage
      port: 9080
```

访问

清理：

kubectl delete httproute productpage -n istio

kubectl delete gateways.networking.x-k8s.io bookinfo -n istio-system

kubectl delete gatewayclass istio -n istio-system

kubectl delete -n istio-system secret bookinfo-gateway-secret

kubectl delete -n istio-system secret bookinfo-gateway-secret-http

![1629964053(1)](images\1629964053(1).jpg)

![1629964113(1)](images\1629964113(1).jpg)



### tcproutes

#### gateways

##### allow

###### All

tcproute/echo-tcproute-gateways-allow-all.yaml

kubectl apply -f echo-tcproute-gateways-allow-all.yaml  -n istio

```
kind: TCPRoute
apiVersion: networking.x-k8s.io/v1alpha1
metadata:
  name: echo
  labels:
    gateway: echo
spec:
  gateways:
    allow: All
  rules:
  - forwardTo:
    - serviceName: tcp-echo
      port: 9000
```

gateway/bookinfo-gateway-listeners-protocol-TCP.yaml

kubectl apply -f bookinfo-gateway-listeners-protocol-TCP.yaml -n istio-system

```
kind: Gateway
apiVersion: networking.x-k8s.io/v1alpha1
metadata:
  name: echo
spec:
  gatewayClassName: istio
  listeners:
  - protocol: TCP
    port: 15443
    routes:
      namespaces:
        from: All
      kind: TCPRoute
      selector:
        matchLabels:
          gateway: echo
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

telnet 192.168.198.154 31010

清理：

kubectl  delete -f tcp-echo-services.yaml -n istio

kubectl delete TCPRoute echo -n istio

kubectl delete gateways.networking.x-k8s.io echo -n istio-system

kubectl delete gatewayclass istio -n istio-system

![1629964228(1)](images\1629964228(1).jpg)

![1629964270(1)](images\1629964270(1).jpg)





###### FromList

tcproute/echo-tcproute-gateways-allow-FromList.yaml

kubectl apply -f echo-tcproute-gateways-allow-FromList.yaml  -n istio

```
kind: TCPRoute
apiVersion: networking.x-k8s.io/v1alpha1
metadata:
  name: echo
  labels:
    gateway: echo
spec:
  gateways:
    allow: FromList
    gatewayRefs:
    - name: echo
      namespace: istio-system
  rules:
  - forwardTo:
    - serviceName: tcp-echo
      port: 9000
```

gateway/bookinfo-gateway-listeners-protocol-TCP.yaml

kubectl apply -f bookinfo-gateway-listeners-protocol-TCP.yaml -n istio-system

```
kind: Gateway
apiVersion: networking.x-k8s.io/v1alpha1
metadata:
  name: echo
spec:
  gatewayClassName: istio
  listeners:
  - protocol: TCP
    port: 15443
    routes:
      namespaces:
        from: All
      kind: TCPRoute
      selector:
        matchLabels:
          gateway: echo
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

telnet 192.168.198.154 31010

清理：

kubectl  delete -f tcp-echo-services.yaml -n istio

kubectl delete TCPRoute echo -n istio

kubectl delete gateways.networking.x-k8s.io echo -n istio-system

kubectl delete gatewayclass istio -n istio-system

![1629964332(1)](images\1629964332(1).jpg)

![1629964362(1)](images\1629964362(1).jpg)

![1629964391(1)](images\1629964391(1).jpg)



###### SameNamespace

默认

tcproute/echo-tcproute-gateways-allow-SameNamespace.yaml

kubectl apply -f echo-tcproute-gateways-allow-SameNamespace.yaml  -n istio-system

```
kind: TCPRoute
apiVersion: networking.x-k8s.io/v1alpha1
metadata:
  name: echo
  labels:
    gateway: echo
spec:
  gateways:
    allow: SameNamespace
  rules:
  - forwardTo:
    - serviceName: tcp-echo
      port: 9000
```

gateway/bookinfo-gateway-listeners-protocol-TCP.yaml

kubectl apply -f bookinfo-gateway-listeners-protocol-TCP.yaml -n istio-system

```
kind: Gateway
apiVersion: networking.x-k8s.io/v1alpha1
metadata:
  name: echo
spec:
  gatewayClassName: istio
  listeners:
  - protocol: TCP
    port: 15443
    routes:
      namespaces:
        from: All
      kind: TCPRoute
      selector:
        matchLabels:
          gateway: echo
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

telnet 192.168.198.154 31010

 连不上

serviceName不能用长名称productpage.istio.svc.cluster.local

svc只有在istio-system名称空间下才有效

gateway，只有在istio-system名称空间才有效

清理：

kubectl  delete -f tcp-echo-services.yaml -n istio

kubectl delete TCPRoute echo -n istio

kubectl delete gateways.networking.x-k8s.io echo -n istio-system

kubectl delete gatewayclass istio -n istio-system







##### gatewayRefs

###### name，namespace

已使用



#### rules

##### forwardTo

###### backendRef

group，kind，name

tcproute/echo-tcproute-forwardTo-backendRef-goup.yaml

kubectl apply -f echo-tcproute-forwardTo-backendRef-goup.yaml  -n istio

```
kind: TCPRoute
apiVersion: networking.x-k8s.io/v1alpha1
metadata:
  name: echo
  labels:
    gateway: echo
spec:
  gateways:
    allow: All
  rules:
  - forwardTo:
    - backendRef:
        group: core
        kind: Service
        name: tcp-echo
      port: 9000
```

gateway/bookinfo-gateway-listeners-protocol-TCP.yaml

kubectl apply -f bookinfo-gateway-listeners-protocol-TCP.yaml -n istio-system

```
kind: Gateway
apiVersion: networking.x-k8s.io/v1alpha1
metadata:
  name: echo
spec:
  gatewayClassName: istio
  listeners:
  - protocol: TCP
    port: 15443
    routes:
      namespaces:
        from: All
      kind: TCPRoute
      selector:
        matchLabels:
          gateway: echo
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

telnet 192.168.198.154 31010

referencing unsupported destination; backendRef is not supported

清理：

kubectl  delete -f tcp-echo-services.yaml -n istio

kubectl delete TCPRoute echo -n istio

kubectl delete gateways.networking.x-k8s.io echo -n istio-system

kubectl delete gatewayclass istio -n istio-system



###### port，serviceName

tcproute/echo-tcproute-gateways-allow-all.yaml

kubectl apply -f echo-tcproute-gateways-allow-all.yaml  -n istio

```
kind: TCPRoute
apiVersion: networking.x-k8s.io/v1alpha1
metadata:
  name: echo
  labels:
    gateway: echo
spec:
  gateways:
    allow: All
  rules:
  - forwardTo:
    - serviceName: tcp-echo
      port: 9000
```

gateway/bookinfo-gateway-listeners-protocol-TCP.yaml

kubectl apply -f bookinfo-gateway-listeners-protocol-TCP.yaml -n istio-system

```
kind: Gateway
apiVersion: networking.x-k8s.io/v1alpha1
metadata:
  name: echo
spec:
  gatewayClassName: istio
  listeners:
  - protocol: TCP
    port: 15443
    routes:
      namespaces:
        from: All
      kind: TCPRoute
      selector:
        matchLabels:
          gateway: echo
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

telnet 192.168.198.154 31010

清理：

kubectl  delete -f tcp-echo-services.yaml -n istio

kubectl delete TCPRoute echo -n istio

kubectl delete gateways.networking.x-k8s.io echo -n istio-system

kubectl delete gatewayclass istio -n istio-system

![1629967016(1)](images\1629967016(1).jpg)

![1629967054(1)](images\1629967054(1).jpg)

![1629967086(1)](images\1629967086(1).jpg)



###### weight

tcproute/echo-tcproute-rules-forwardTo-weight.yaml

kubectl apply -f echo-tcproute-rules-forwardTo-weight.yaml  -n istio

```
kind: TCPRoute
apiVersion: networking.x-k8s.io/v1alpha1
metadata:
  name: echo
  labels:
    gateway: echo
spec:
  gateways:
    allow: All
  rules:
  - forwardTo:
    - serviceName: tcp-echo
      port: 9000
      weight: 50
    - serviceName: tcp-echo-v2
      port: 9000
      weight: 50      
```

gateway/bookinfo-gateway-listeners-protocol-TCP.yaml

kubectl apply -f bookinfo-gateway-listeners-protocol-TCP.yaml -n istio-system

```
kind: Gateway
apiVersion: networking.x-k8s.io/v1alpha1
metadata:
  name: echo
spec:
  gatewayClassName: istio
  listeners:
  - protocol: TCP
    port: 15443
    routes:
      namespaces:
        from: All
      kind: TCPRoute
      selector:
        matchLabels:
          gateway: echo
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

tcp-echo-services-weight.yaml

kubectl  apply -f tcp-echo-services-weight.yaml -n istio

```
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
    app: tcp-echo-v2
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: tcp-echo-weight-2
  labels:
    app: tcp-echo-v2
    version: v1
spec:
  replicas: 1
  selector:
    matchLabels:
      app: tcp-echo-v2
      version: v1
  template:
    metadata:
      labels:
        app: tcp-echo-v2
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

```

kubectl logs -n istio tcp-echo-v1-7dd5c5dcfb-dzkhq 

kubectl logs -n istio tcp-echo-v2-7dd5c5dcfb-dzkhq 

kubectl logs -n istio tcp-echo-weight-2-56cd9b5c4f-hwsw4 



telnet 192.168.198.154 31010

清理：

kubectl  delete -f tcp-echo-services.yaml -n istio

kubectl delete TCPRoute echo -n istio

kubectl delete gateways.networking.x-k8s.io echo -n istio-system

kubectl delete gatewayclass istio -n istio-system

![1629967224](images\1629967224.jpg)







##### matches

###### extensionRef

不清楚

group，kind，name

tcproute/echo-tcproute-rules-matches-extensionRef.yaml

kubectl apply -f echo-tcproute-rules-matches-extensionRef.yaml  -n istio

```
kind: TCPRoute
apiVersion: networking.x-k8s.io/v1alpha1
metadata:
  name: echo
  labels:
    gateway: echo
spec:
  gateways:
    allow: All
  rules:
  - forwardTo:
    - serviceName: tcp-echo
      port: 9000
    matches:
    - extensionRef:
        group: core
        kind: test
        name: test
```

gateway/bookinfo-gateway-listeners-protocol-TCP.yaml

kubectl apply -f bookinfo-gateway-listeners-protocol-TCP.yaml -n istio-system

```
kind: Gateway
apiVersion: networking.x-k8s.io/v1alpha1
metadata:
  name: echo
spec:
  gatewayClassName: istio
  listeners:
  - protocol: TCP
    port: 15443
    routes:
      namespaces:
        from: All
      kind: TCPRoute
      selector:
        matchLabels:
          gateway: echo
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

telnet 192.168.198.154 31010

清理：

kubectl  delete -f tcp-echo-services.yaml -n istio

kubectl delete TCPRoute echo -n istio

kubectl delete gateways.networking.x-k8s.io echo -n istio-system

kubectl delete gatewayclass istio -n istio-system





### tlsroutes

#### gateways

##### allow



###### all

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



tlsroute/bookinfo-tlsroute-listeners-tls-mode-Passthrough.yaml

kubectl apply -f bookinfo-tlsroute-listeners-tls-mode-Passthrough.yaml -n istio-system

```
kind: Gateway
apiVersion: networking.x-k8s.io/v1alpha1
metadata:
  name: bookinfo
spec:
  gatewayClassName: istio
  listeners:  
  - hostname: "*"
    port: 443
    protocol: TLS
    tls:
      mode: Passthrough
    routes:
      namespaces:
        from: All
      selector:
        matchLabels:
          gateway: bookinfo
      kind: TLSRoute
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

tlsroute/nginx-tls-route-gateways-allow-ALL.yaml

kubectl apply -f nginx-tls-route-gateways-allow-ALL.yaml -n istio

```
kind: TLSRoute
apiVersion: networking.x-k8s.io/v1alpha1
metadata:
  name: nginx
  labels:
    gateway: bookinfo
spec:
  gateways:
    allow: All
  rules:
  - forwardTo:
    - serviceName: my-nginx
      port: 443
```

访问

https://bookinfo.com:31396/



清理：

kubectl delete httproute nginx -n istio

kubectl delete gateways.networking.x-k8s.io bookinfo -n istio-system

kubectl delete gatewayclass istio -n istio-system

kubectl delete -f nginx-deploy.yaml -n istio

 kubectl delete configmap nginx-configmap -n istio

kubectl delete secret nginx-server-certs   -n istio

![1629967383(1)](images\1629967383(1).jpg)

![1629967420(1)](images\1629967420(1).jpg)





###### FromList

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



tlsroute/bookinfo-tlsroute-listeners-tls-mode-Passthrough.yaml

kubectl apply -f bookinfo-tlsroute-listeners-tls-mode-Passthrough.yaml -n istio-system

```
kind: Gateway
apiVersion: networking.x-k8s.io/v1alpha1
metadata:
  name: bookinfo
spec:
  gatewayClassName: istio
  listeners:  
  - hostname: "*"
    port: 443
    protocol: TLS
    tls:
      mode: Passthrough
    routes:
      namespaces:
        from: All
      selector:
        matchLabels:
          gateway: bookinfo
      kind: TLSRoute
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

tlsroute/nginx-tls-route-gateways-all-FromList.yaml

kubectl apply -f nginx-tls-route-gateways-all-FromList.yaml -n istio

```
kind: TLSRoute
apiVersion: networking.x-k8s.io/v1alpha1
metadata:
  name: nginx
  labels:
    gateway: bookinfo
spec:
  gateways:
    allow: FromList
    gatewayRefs:
    - name: bookinfo
      namespace: istio-system
  rules:
  - forwardTo:
    - serviceName: my-nginx
      port: 443
```

访问

https://bookinfo.com:31396/

清理：

kubectl delete httproute nginx -n istio

kubectl delete gateways.networking.x-k8s.io bookinfo -n istio-system

kubectl delete gatewayclass istio -n istio-system

kubectl delete -f nginx-deploy.yaml -n istio

 kubectl delete configmap nginx-configmap -n istio

kubectl delete secret nginx-server-certs   -n istio

![1629967499(1)](images\1629967499(1).jpg)

![1629967531(1)](images\1629967531(1).jpg)

![1629967558(1)](images\1629967558(1).jpg)



###### SameNamespace

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



tlsroute/bookinfo-tlsroute-listeners-tls-mode-Passthrough.yaml

kubectl apply -f bookinfo-tlsroute-listeners-tls-mode-Passthrough.yaml -n istio-system

```
kind: Gateway
apiVersion: networking.x-k8s.io/v1alpha1
metadata:
  name: bookinfo
spec:
  gatewayClassName: istio
  listeners:  
  - hostname: "*"
    port: 443
    protocol: TLS
    tls:
      mode: Passthrough
    routes:
      namespaces:
        from: All
      selector:
        matchLabels:
          gateway: bookinfo
      kind: TLSRoute
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

tlsroute/nginx-tls-route-gateways-all-SameNamespace.yaml

kubectl apply -f nginx-tls-route-gateways-all-SameNamespace.yaml -n istio-system

```
kind: TLSRoute
apiVersion: networking.x-k8s.io/v1alpha1
metadata:
  name: nginx
  labels:
    gateway: bookinfo
spec:
  gateways:
    allow: SameNamespace
  rules:
  - forwardTo:
    - serviceName: my-nginx
      port: 443
```

访问

https://bookinfo.com:31396/

失败

serviceName不能用长名称productpage.istio.svc.cluster.local

svc只有在istio-system名称空间下才有效

gateway，只有在istio-system名称空间才有效

清理：

kubectl delete httproute nginx -n istio

kubectl delete gateways.networking.x-k8s.io bookinfo -n istio-system

kubectl delete gatewayclass istio -n istio-system

kubectl delete -f nginx-deploy.yaml -n istio

 kubectl delete configmap nginx-configmap -n istio

kubectl delete secret nginx-server-certs   -n istio







##### gatewayRefs

###### name，namespace

已介绍

#### rules

##### forwardTo

###### backendRef

group，kind，name

不支持



###### port，serviceName

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



tlsroute/bookinfo-tlsroute-listeners-tls-mode-Passthrough.yaml

kubectl apply -f bookinfo-tlsroute-listeners-tls-mode-Passthrough.yaml -n istio-system

```
kind: Gateway
apiVersion: networking.x-k8s.io/v1alpha1
metadata:
  name: bookinfo
spec:
  gatewayClassName: istio
  listeners:  
  - hostname: "*"
    port: 443
    protocol: TLS
    tls:
      mode: Passthrough
    routes:
      namespaces:
        from: All
      selector:
        matchLabels:
          gateway: bookinfo
      kind: TLSRoute
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

tlsroute/nginx-tls-route-forwardTo-port-serviceName.yaml

kubectl apply -f nginx-tls-route-forwardTo-port-serviceName.yaml -n istio

```
kind: TLSRoute
apiVersion: networking.x-k8s.io/v1alpha1
metadata:
  name: nginx
  labels:
    gateway: bookinfo
spec:
  gateways:
    allow: All
  rules:
  - forwardTo:
    - serviceName: my-nginx
      port: 443
```

访问

https://bookinfo.com:31396/



清理：

kubectl delete httproute nginx -n istio

kubectl delete gateways.networking.x-k8s.io bookinfo -n istio-system

kubectl delete gatewayclass istio -n istio-system

kubectl delete -f nginx-deploy.yaml -n istio

 kubectl delete configmap nginx-configmap -n istio

kubectl delete secret nginx-server-certs   -n istio

![1629968092(1)](images\1629968092(1).jpg)

![1629968124(1)](images\1629968124(1).jpg)



![1629968154(1)](images\1629968154(1).jpg)



###### weight

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
    name: https-nginx
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
    name: https-nginx-v2
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



tlsroute/bookinfo-tlsroute-listeners-tls-mode-Passthrough.yaml

kubectl apply -f bookinfo-tlsroute-listeners-tls-mode-Passthrough.yaml -n istio-system

```
kind: Gateway
apiVersion: networking.x-k8s.io/v1alpha1
metadata:
  name: bookinfo
spec:
  gatewayClassName: istio
  listeners:  
  - hostname: "*"
    port: 443
    protocol: TLS
    tls:
      mode: Passthrough
    routes:
      namespaces:
        from: All
      selector:
        matchLabels:
          gateway: bookinfo
      kind: TLSRoute
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

tlsroute/nginx-tls-route-forwardTo-weight.yaml

kubectl apply -f nginx-tls-route-forwardTo-weight.yaml -n istio

```
kind: TLSRoute
apiVersion: networking.x-k8s.io/v1alpha1
metadata:
  name: nginx
  labels:
    gateway: bookinfo
spec:
  gateways:
    allow: All
  rules:
  - forwardTo:
    - serviceName: my-nginx
      port: 443
      weight: 10
    - serviceName: my-nginx-v2
      port: 443
      weight: 90
```

访问

https://bookinfo.com:31396/

失败，一边倒

清理：

kubectl delete httproute nginx -n istio

kubectl delete gateways.networking.x-k8s.io bookinfo -n istio-system

kubectl delete gatewayclass istio -n istio-system

kubectl delete -f nginx-deploy.yaml -n istio

 kubectl delete configmap nginx-configmap -n istio

kubectl delete secret nginx-server-certs   -n istio

kubectl delete -f nginx-deploy-v2.yaml -n istio

![1629968219(1)](images\1629968219(1).jpg)



##### matches

###### extensionRef

group，kind，name





###### snis

具体



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



tlsroute/bookinfo-tlsroute-listeners-tls-mode-Passthrough.yaml

kubectl apply -f bookinfo-tlsroute-listeners-tls-mode-Passthrough.yaml -n istio-system

```
kind: Gateway
apiVersion: networking.x-k8s.io/v1alpha1
metadata:
  name: bookinfo
spec:
  gatewayClassName: istio
  listeners:  
  - hostname: "*"
    port: 443
    protocol: TLS
    tls:
      mode: Passthrough
    routes:
      namespaces:
        from: All
      selector:
        matchLabels:
          gateway: bookinfo
      kind: TLSRoute
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

tlsroute/nginx-tls-route-matches-snis.yaml

kubectl apply -f nginx-tls-route-matches-snis.yaml -n istio

```
kind: TLSRoute
apiVersion: networking.x-k8s.io/v1alpha1
metadata:
  name: nginx
  labels:
    gateway: bookinfo
spec:
  gateways:
    allow: All
  rules:
  - forwardTo:
    - serviceName: my-nginx
      port: 443
    matches:
    - snis:
      - “bookinfo.com”
```

访问

https://bookinfo.com:31396/

可以访问

清理：

kubectl delete httproute nginx -n istio

kubectl delete gateways.networking.x-k8s.io bookinfo -n istio-system

kubectl delete gatewayclass istio -n istio-system

kubectl delete -f nginx-deploy.yaml -n istio

 kubectl delete configmap nginx-configmap -n istio

kubectl delete secret nginx-server-certs   -n istio

![1629968696(1)](images\1629968696(1).jpg)



![1629968624(1)](images\1629968624(1).jpg)

![1629968657(1)](images\1629968657(1).jpg)

semi-star

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



tlsroute/bookinfo-tlsroute-listeners-tls-mode-Passthrough.yaml

kubectl apply -f bookinfo-tlsroute-listeners-tls-mode-Passthrough.yaml -n istio-system

```
kind: Gateway
apiVersion: networking.x-k8s.io/v1alpha1
metadata:
  name: bookinfo
spec:
  gatewayClassName: istio
  listeners:  
  - hostname: "*"
    port: 443
    protocol: TLS
    tls:
      mode: Passthrough
    routes:
      namespaces:
        from: All
      selector:
        matchLabels:
          gateway: bookinfo
      kind: TLSRoute
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

tlsroute/nginx-tls-route-matches-snis-semi-star.yaml

kubectl apply -f nginx-tls-route-matches-snis-semi-star.yaml -n istio

```
kind: TLSRoute
apiVersion: networking.x-k8s.io/v1alpha1
metadata:
  name: nginx
  labels:
    gateway: bookinfo
spec:
  gateways:
    allow: All
  rules:
  - forwardTo:
    - serviceName: my-nginx
      port: 443
    matches:
    - snis:
      - “*.com”
```

访问

https://bookinfo.com:31396/

  partial wildcards are not supported in "server_names"" 

清理：

kubectl delete httproute nginx -n istio

kubectl delete gateways.networking.x-k8s.io bookinfo -n istio-system

kubectl delete gatewayclass istio -n istio-system

kubectl delete -f nginx-deploy.yaml -n istio

 kubectl delete configmap nginx-configmap -n istio

kubectl delete secret nginx-server-certs   -n istio



### udproutes

不支持

#### gateways

##### allow



##### gatewayRefs

###### name，namespace





#### rules

##### forwardTo

###### backendRef

group，kind，name





###### port



###### serviceName



###### weight



##### matches

###### extensionRef

group，kind，name



## backendpolicies

```
BackendPolicy has been removed from the API for v1alpha2. This is in favor of the new policy attachment guidelines introduced by GEP
```

### backendRefs

#### group，kind，name

```
kind: BackendPolicy
apiVersion: networking.x-k8s.io/v1alpha1
metadata:
  name: my-app
  annotations:
    networking.x-k8s.io/app-protocol: https
spec:
  backendRefs:
  - name: my-service
    group: core
    kind: Service
  tls:
    certificateAuthorityRef:
      name: my-cluster-ca
      group: core
      kind: Secret
    options: {}
```



#### port





### tls

#### certificateAuthorityRef

##### group，kind，name





#### options

