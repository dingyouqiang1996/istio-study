# 学习目标

![1618719219(1)](images\1618719219(1).jpg)

# 10-1什么是sidecar

Sidecar描述了sidecar代理的配置，sidecar代理调解与其连接的工作负载的 inbound 和 outbound 通信。 默认情况下，Istio将为网格中的所有Sidecar代理服务，使其具有到达网格中每个工作负载所需的必要配置，并在与工作负载关联的所有端口上接收流量。 Sidecar资源提供了一种的方法，在向工作负载转发流量或从工作负载转发流量时，微调端口集合和代理将接收的协议。 此外，可以限制代理在从工作负载转发 outbound 流量时可以达到的服务集合。

网格中的服务和配置被组织成一个或多个名称空间（例如，Kubernetes名称空间或CF org/space）。 命名空间中的Sidecar资源将应用于同一命名空间中的一个或多个工作负载，由workloadSelector选择。 如果没有workloadSelector，它将应用于同一名称空间中的所有工作负载。 在确定要应用于工作负载的Sidecar资源时，将优先使用通过workloadSelector而选择到此工作负载的的资源，而不是没有任何workloadSelector的资源。

> 注意：每个命名空间只能有一个没有任何工作负载选择器的Sidecar资源。 如果给定命名空间中存在多个无选择器的Sidecar资源，则系统的行为是不确定的。 如果具有工作负载选择器的两个或多个Sidecar资源选择相同的工作负载，则系统的行为是不确定的。

# 资源详解

| Field                   | Type                     | Description                                                  | Required |
| ----------------------- | ------------------------ | ------------------------------------------------------------ | -------- |
| `workloadSelector`      | `WorkloadSelector`       | Criteria used to select the specific set of pods/VMs on which this `Sidecar` configuration should be applied. If omitted, the `Sidecar` configuration will be applied to all workload instances in the same namespace. | No       |
| `ingress`               | `IstioIngressListener[]` | Ingress specifies the configuration of the sidecar for processing inbound traffic to the attached workload instance. If omitted, Istio will automatically configure the sidecar based on the information about the workload obtained from the orchestration platform (e.g., exposed ports, services, etc.). If specified, inbound ports are configured if and only if the workload instance is associated with a service. | No       |
| `egress`                | `IstioEgressListener[]`  | Egress specifies the configuration of the sidecar for processing outbound traffic from the attached workload instance to other services in the mesh. | Yes      |
| `outboundTrafficPolicy` | `OutboundTrafficPolicy`  | This allows to configure the outbound traffic policy. If your application uses one or more external services that are not known apriori, setting the policy to `ALLOW_ANY` will cause the sidecars to route any unknown traffic originating from the application to its requested destination. | No       |

## 10-2全局有效

sc-default-global.yaml

kubectl apply -f sc-default-global.yaml -n istio-system

```
apiVersion: networking.istio.io/v1beta1
kind: Sidecar
metadata:
  name: default
  namespace: istio-system
spec:
  ingress:
  - port:
      number: 9080
      protocol: HTTP
      name: http
    defaultEndpoint: 127.0.0.1:9080
```

virtaulservice/vs-bookinfo-star.yam

kubectl apply -f vs-bookinfo-star.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: bookinfo
spec:
  exportTo:
  - '*'
  hosts:
  - "*"
  gateways:
  - bookinfo-gateway
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



kubectl apply -f gateway-01.yaml -n istio

gateway/gateway-01.yaml

```
apiVersion: networking.istio.io/v1beta1
kind: Gateway
metadata:
  name: bookinfo-gateway
spec:
  selector:
    istio: ingressgateway
  servers:
  - port:
      number: 80
      name: http
      protocol: HTTP
    hosts:
    - "*"
```

访问

清理：

kubectl delete sidecar default -n istio-system

kubectl delete vs bookinfo -n istio

kubectl delete gw bookinfo-gateway -n istio



kubectl port-forward --address 0.0.0.0 -n istio productpage-v1-659776cb44-rrr87 15000:15000

![1628565499(1)](images\1628565499(1).jpg)



![1628565566(1)](images\1628565566(1).jpg)





## 10-3workloadSelector

### 没有selector,名称空间有效

sc-default-istio-ingress.yaml 

kubectl apply -f sc-default-istio-ingress.yaml  -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: Sidecar
metadata:
  name: default
spec:
  ingress:
  - port:
      number: 9080
      protocol: HTTP
      name: http
    defaultEndpoint: 127.0.0.1:9080
```

virtaulservice/vs-bookinfo-star.yam

kubectl apply -f vs-bookinfo-star.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: bookinfo
spec:
  exportTo:
  - '*'
  hosts:
  - "*"
  gateways:
  - bookinfo-gateway
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



kubectl apply -f gateway-01.yaml -n istio

gateway/gateway-01.yaml

```
apiVersion: networking.istio.io/v1beta1
kind: Gateway
metadata:
  name: bookinfo-gateway
spec:
  selector:
    istio: ingressgateway
  servers:
  - port:
      number: 80
      name: http
      protocol: HTTP
    hosts:
    - "*"
```

访问

清理：

kubectl delete sidecar default -n istio

kubectl delete vs bookinfo -n istio

kubectl delete gw bookinfo-gateway -n istio



kubectl port-forward --address 0.0.0.0 -n istio productpage-v1-659776cb44-rrr87 15000:15000

![1628565769(1)](images\1628565769(1).jpg)

![1628565841(1)](images\1628565841(1).jpg)



### 有selector

sc-productpage-selector.yaml 

kubectl apply -f sc-productpage-selector.yaml  -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: Sidecar
metadata:
  name: productpage
spec:
  workloadSelector:
    labels:
      app: productpage
  ingress:
  - port:
      number: 9081
      protocol: HTTP
      name: http
    defaultEndpoint: 127.0.0.1:9080
```

监听端口和目标端口不一致，可用于端口转换

此时service需要添加端口

kubectl edit svc productpage -n istio

```
  - name: http9081
    port: 9081
    protocol: TCP
    targetPort: 9081
```

修改vs端口

sidecar/vs-bookinfo-hosts-star.yaml 

kubectl apply -f vs-bookinfo-hosts-star.yaml  -n istio

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
          number: 9081
```

kubectl apply -f gateway-01.yaml -n istio

gateway/gateway-01.yaml

```
apiVersion: networking.istio.io/v1beta1
kind: Gateway
metadata:
  name: bookinfo-gateway
spec:
  selector:
    istio: ingressgateway
  servers:
  - port:
      number: 80
      name: http
      protocol: HTTP
    hosts:
    - "*"
```





访问

清理：

kubectl delete sidecar productpage -n istio

kubectl delete vs bookinfo -n istio

kubectl delete gw bookinfo-gateway -n istio



kubectl port-forward --address 0.0.0.0 -n istio productpage-v1-659776cb44-rrr87 15000:15000

![1628566017(1)](images\1628566017(1).jpg)

![1628566055(1)](images\1628566055(1).jpg)

![1628566103(1)](images\1628566103(1).jpg)





## 10-4egress

| Field         | Type          | Description                                                  | Required |
| ------------- | ------------- | ------------------------------------------------------------ | -------- |
| `port`        | `Port`        | The port associated with the listener. If using Unix domain socket, use 0 as the port number, with a valid protocol. The port if specified, will be used as the default destination port associated with the imported hosts. If the port is omitted, Istio will infer the listener ports based on the imported hosts. Note that when multiple egress listeners are specified, where one or more listeners have specific ports while others have no port, the hosts exposed on a listener port will be based on the listener with the most specific port. | No       |
| `bind`        | `string`      | The IP or the Unix domain socket to which the listener should be bound to. Port MUST be specified if bind is not empty. Format: `x.x.x.x` or `unix:///path/to/uds` or `unix://@foobar` (Linux abstract namespace). If omitted, Istio will automatically configure the defaults based on imported services, the workload instances to which this configuration is applied to and the captureMode. If captureMode is `NONE`, bind will default to 127.0.0.1. | No       |
| `captureMode` | `CaptureMode` | When the bind address is an IP, the captureMode option dictates how traffic to the listener is expected to be captured (or not). captureMode must be DEFAULT or `NONE` for Unix domain socket binds. | No       |
| `hosts`       | `string[]`    | One or more service hosts exposed by the listener in `namespace/dnsName` format. Services in the specified namespace matching `dnsName` will be exposed. The corresponding service can be a service in the service registry (e.g., a Kubernetes or cloud foundry service) or a service specified using a `ServiceEntry` or `VirtualService` configuration. Any associated `DestinationRule` in the same namespace will also be used.The `dnsName` should be specified using FQDN format, optionally including a wildcard character in the left-most component (e.g., `prod/*.example.com`). Set the `dnsName` to `*` to select all services from the specified namespace (e.g., `prod/*`).The `namespace` can be set to `*`, `.`, or `~`, representing any, the current, or no namespace, respectively. For example, `*/foo.example.com` selects the service from any available namespace while `./foo.example.com` only selects the service from the namespace of the sidecar. If a host is set to `*/*`, Istio will configure the sidecar to be able to reach every service in the mesh that is exported to the sidecar’s namespace. The value `~/*` can be used to completely trim the configuration for sidecars that simply receive traffic and respond, but make no outbound connections of their own.NOTE: Only services and configuration artifacts exported to the sidecar’s namespace (e.g., `exportTo` value of `*`) can be referenced. Private configurations (e.g., `exportTo` set to `.`) will not be available. Refer to the `exportTo` setting in `VirtualService`, `DestinationRule`, and `ServiceEntry` configurations for details.**WARNING:** The list of egress hosts in a `Sidecar` must also include the Mixer control plane services if they are enabled. Envoy will not be able to reach them otherwise. For example, add host `istio-system/istio-telemetry.istio-system.svc.cluster.local` if telemetry is enabled, `istio-system/istio-policy.istio-system.svc.cluster.local` if policy is enabled, or add `istio-system/*` to allow all services in the `istio-system` namespace. This requirement is temporary and will be removed in a future Istio release. | Yes      |

### port

egress/sc-productpage-egress-port.yaml

kubectl apply -f sc-productpage-egress-port.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: Sidecar
metadata:
  name: productpage
spec:
  workloadSelector:
    labels:
      app: productpage
  egress:
  - hosts:
    - "./*"
    port:
      number: 9080
      protocol: HTTP
      name: egresshttp
```

virtaulservice/vs-bookinfo-star.yam

kubectl apply -f vs-bookinfo-star.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: bookinfo
spec:
  exportTo:
  - '*'
  hosts:
  - "*"
  gateways:
  - bookinfo-gateway
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



kubectl apply -f gateway-01.yaml -n istio

gateway/gateway-01.yaml

```
apiVersion: networking.istio.io/v1beta1
kind: Gateway
metadata:
  name: bookinfo-gateway
spec:
  selector:
    istio: ingressgateway
  servers:
  - port:
      number: 80
      name: http
      protocol: HTTP
    hosts:
    - "*"
```

访问

清理：

kubectl delete sidecar productpage -n istio

kubectl delete vs bookinfo -n istio

kubectl delete gw bookinfo-gateway -n istio





设置了规则后，cluster变少了

![1628566347(1)](images\1628566347(1).jpg)

没设置规则

![1628566406(1)](images\1628566406(1).jpg)

### bind

#### 0.0.0.0

sc-productpage-egress-bind.yaml

kubectl apply -f sc-productpage-egress-bind.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: Sidecar
metadata:
  name: productpage
spec:
  workloadSelector:
    labels:
      app: productpage
  egress:
  - hosts:
    - "./*"
    port:
      number: 9080
      protocol: HTTP
      name: egresshttp
    bind: 0.0.0.0
```

virtaulservice/vs-bookinfo-star.yam

kubectl apply -f vs-bookinfo-star.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: bookinfo
spec:
  exportTo:
  - '*'
  hosts:
  - "*"
  gateways:
  - bookinfo-gateway
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



kubectl apply -f gateway-01.yaml -n istio

gateway/gateway-01.yaml

```
apiVersion: networking.istio.io/v1beta1
kind: Gateway
metadata:
  name: bookinfo-gateway
spec:
  selector:
    istio: ingressgateway
  servers:
  - port:
      number: 80
      name: http
      protocol: HTTP
    hosts:
    - "*"
```

访问

清理：

kubectl delete sidecar productpage -n istio

kubectl delete vs bookinfo -n istio

kubectl delete gw bookinfo-gateway -n istio



kubectl port-forward --address 0.0.0.0 -n istio productpage-v1-659776cb44-rrr87 15000:15000

默认是0.0.0.0

![1628566982(1)](images\1628566982(1).jpg)





#### 目标svc ip

sc-productpage-egress-bind-svc-ip.yaml

kubectl apply -f sc-productpage-egress-bind-svc-ip.yaml -n istio

注意修改ip

```
apiVersion: networking.istio.io/v1beta1
kind: Sidecar
metadata:
  name: productpage
spec:
  workloadSelector:
    labels:
      app: productpage
  egress:
  - hosts:
    - "./*"
    port:
      number: 9080
      protocol: HTTP
      name: egresshttp
    bind: 10.68.190.94
```

virtaulservice/vs-bookinfo-star.yam

kubectl apply -f vs-bookinfo-star.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: bookinfo
spec:
  exportTo:
  - '*'
  hosts:
  - "*"
  gateways:
  - bookinfo-gateway
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



kubectl apply -f gateway-01.yaml -n istio

gateway/gateway-01.yaml

```
apiVersion: networking.istio.io/v1beta1
kind: Gateway
metadata:
  name: bookinfo-gateway
spec:
  selector:
    istio: ingressgateway
  servers:
  - port:
      number: 80
      name: http
      protocol: HTTP
    hosts:
    - "*"
```

访问

清理：

kubectl delete sidecar productpage -n istio

kubectl delete vs bookinfo -n istio

kubectl delete gw bookinfo-gateway -n istio



kubectl port-forward --address 0.0.0.0 -n istio productpage-v1-659776cb44-rrr87 15000:15000

svc ip

![1628567240(1)](images\1628567240(1).jpg)

![1628567280(1)](images\1628567280(1).jpg)



### captureMode

| Name       | Description                                                  |
| ---------- | ------------------------------------------------------------ |
| `DEFAULT`  | The default capture mode defined by the environment.         |
| `IPTABLES` | Capture traffic using IPtables redirection.                  |
| `NONE`     | No traffic capture. When used in an egress listener, the application is expected to explicitly communicate with the listener port or Unix domain socket. When used in an ingress listener, care needs to be taken to ensure that the listener port is not in use by other processes on the host. |

#### DEFAULT

sc-productpage-egress-captureMode-DEFAULT.yaml

kubectl apply -f sc-productpage-egress-captureMode-DEFAULT.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: Sidecar
metadata:
  name: productpage
spec:
  workloadSelector:
    labels:
      app: productpage
  egress:
  - hosts:
    - "./*"
    port:
      number: 9080
      protocol: HTTP
      name: egresshttp
    bind: 0.0.0.0
    captureMode: DEFAULT
```

virtaulservice/vs-bookinfo-star.yam

kubectl apply -f vs-bookinfo-star.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: bookinfo
spec:
  exportTo:
  - '*'
  hosts:
  - "*"
  gateways:
  - bookinfo-gateway
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



kubectl apply -f gateway-01.yaml -n istio

gateway/gateway-01.yaml

```
apiVersion: networking.istio.io/v1beta1
kind: Gateway
metadata:
  name: bookinfo-gateway
spec:
  selector:
    istio: ingressgateway
  servers:
  - port:
      number: 80
      name: http
      protocol: HTTP
    hosts:
    - "*"
```

访问

清理：

kubectl delete sidecar productpage -n istio

kubectl delete vs bookinfo -n istio

kubectl delete gw bookinfo-gateway -n istio

默认是iptables模式

kubectl port-forward --address 0.0.0.0 -n istio productpage-v1-659776cb44-rrr87 15000:15000



![1628570054(1)](images\1628570054(1).jpg)







#### IPTABLES

sc-productpage-egress-captureMode-IPTABLES.yaml

kubectl apply -f sc-productpage-egress-captureMode-IPTABLES.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: Sidecar
metadata:
  name: productpage
spec:
  workloadSelector:
    labels:
      app: productpage
  egress:
  - hosts:
    - "./*"
    port:
      number: 9080
      protocol: HTTP
      name: egresshttp
    bind: 0.0.0.0
    captureMode: IPTABLES
```

virtaulservice/vs-bookinfo-star.yam

kubectl apply -f vs-bookinfo-star.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: bookinfo
spec:
  exportTo:
  - '*'
  hosts:
  - "*"
  gateways:
  - bookinfo-gateway
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



kubectl apply -f gateway-01.yaml -n istio

gateway/gateway-01.yaml

```
apiVersion: networking.istio.io/v1beta1
kind: Gateway
metadata:
  name: bookinfo-gateway
spec:
  selector:
    istio: ingressgateway
  servers:
  - port:
      number: 80
      name: http
      protocol: HTTP
    hosts:
    - "*"
```

访问

清理：

kubectl delete sidecar productpage -n istio

kubectl delete vs bookinfo -n istio

kubectl delete gw bookinfo-gateway -n istio



kubectl port-forward --address 0.0.0.0 -n istio productpage-v1-659776cb44-rrr87 15000:15000

![1628570150(1)](images\1628570150(1).jpg)









#### NONE

sc-productpage-egress-captureMode-NONE.yaml

kubectl apply -f sc-productpage-egress-captureMode-NONE.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: Sidecar
metadata:
  name: productpage
spec:
  workloadSelector:
    labels:
      app: productpage
  egress:
  - hosts:
    - "./*"
    port:
      number: 9080
      protocol: HTTP
      name: egresshttp
    bind: 0.0.0.0
    captureMode: NONE
```

virtaulservice/vs-bookinfo-star.yam

kubectl apply -f vs-bookinfo-star.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: bookinfo
spec:
  exportTo:
  - '*'
  hosts:
  - "*"
  gateways:
  - bookinfo-gateway
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



kubectl apply -f gateway-01.yaml -n istio

gateway/gateway-01.yaml

```
apiVersion: networking.istio.io/v1beta1
kind: Gateway
metadata:
  name: bookinfo-gateway
spec:
  selector:
    istio: ingressgateway
  servers:
  - port:
      number: 80
      name: http
      protocol: HTTP
    hosts:
    - "*"
```

访问

查看iptables

 yum install util-linux

ps -ef|grep productpage

nsenter -t 29767  -n iptables -t nat -S 

清理：

kubectl delete sidecar productpage -n istio

kubectl delete vs bookinfo -n istio

kubectl delete gw bookinfo-gateway -n istio



kubectl port-forward --address 0.0.0.0 -n istio productpage-v1-659776cb44-rrr87 15000:15000

none表示不拦截出口流量









sc-productpage-ingress-captureMode-NONE.yaml

kubectl  apply -f sc-productpage-ingress-captureMode-NONE.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: Sidecar
metadata:
  name: productpage
spec:
  workloadSelector:
    labels:
      app: productpage
  ingress:
  - captureMode: NONE
    defaultEndpoint: 127.0.0.1:9080
    port:
      number: 9080
      protocol: HTTP
      name: http
  egress:
  - hosts:
    - "./*"
    port:
      number: 9080
      protocol: HTTP
      name: egresshttp
    bind: 127.0.0.1
    captureMode: NONE
```

virtaulservice/vs-bookinfo-star.yam

kubectl apply -f vs-bookinfo-star.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: bookinfo
spec:
  exportTo:
  - '*'
  hosts:
  - "*"
  gateways:
  - bookinfo-gateway
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



kubectl apply -f gateway-01.yaml -n istio

gateway/gateway-01.yaml

```
apiVersion: networking.istio.io/v1beta1
kind: Gateway
metadata:
  name: bookinfo-gateway
spec:
  selector:
    istio: ingressgateway
  servers:
  - port:
      number: 80
      name: http
      protocol: HTTP
    hosts:
    - "*"
```

进入和出去流量都不捕获，相当于去掉了sidecar，对这个pod的istio资源将不起作用。



注意mesh配置，允许集群外访问

outboundTrafficPolicy: 
  mode: REGISTRY_ONLY| ALLOW_ANY 



访问

清理：

kubectl delete sidecar productpage -n istio

kubectl delete vs bookinfo -n istio

kubectl delete gw bookinfo-gateway -n istio



kubectl port-forward --address 0.0.0.0 -n istio productpage-v1-659776cb44-rrr87 15000:15000









### hosts

#### dot

sc-productpage-egress-hosts-dot.yaml

kubectl apply -f sc-productpage-egress-hosts-dot.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: Sidecar
metadata:
  name: productpage
spec:
  workloadSelector:
    labels:
      app: productpage
  egress:
  - hosts:
    - "./*"
```

virtaulservice/vs-bookinfo-star.yam

kubectl apply -f vs-bookinfo-star.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: bookinfo
spec:
  exportTo:
  - '*'
  hosts:
  - "*"
  gateways:
  - bookinfo-gateway
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



kubectl apply -f gateway-01.yaml -n istio

gateway/gateway-01.yaml

```
apiVersion: networking.istio.io/v1beta1
kind: Gateway
metadata:
  name: bookinfo-gateway
spec:
  selector:
    istio: ingressgateway
  servers:
  - port:
      number: 80
      name: http
      protocol: HTTP
    hosts:
    - "*"
```

访问

清理：

kubectl delete sidecar productpage -n istio

kubectl delete vs bookinfo -n istio

kubectl delete gw bookinfo-gateway -n istio

![1628573868(1)](images\1628573868(1).jpg)





#### semi-star

sc-productpage-egress-hosts-semi-star.yaml

kubectl apply -f sc-productpage-egress-hosts-semi-star.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: Sidecar
metadata:
  name: productpage
spec:
  workloadSelector:
    labels:
      app: productpage
  egress:
  - hosts:
    - "istio/*"
```

virtaulservice/vs-bookinfo-star.yam

kubectl apply -f vs-bookinfo-star.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: bookinfo
spec:
  exportTo:
  - '*'
  hosts:
  - "*"
  gateways:
  - bookinfo-gateway
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



kubectl apply -f gateway-01.yaml -n istio

gateway/gateway-01.yaml

```
apiVersion: networking.istio.io/v1beta1
kind: Gateway
metadata:
  name: bookinfo-gateway
spec:
  selector:
    istio: ingressgateway
  servers:
  - port:
      number: 80
      name: http
      protocol: HTTP
    hosts:
    - "*"
```

访问

清理：

kubectl delete sidecar productpage -n istio

kubectl delete vs bookinfo -n istio

kubectl delete gw bookinfo-gateway -n istio

![1628573950(1)](images\1628573950(1).jpg)







#### double-star

sc-productpage-egress-hosts-double-star.yaml

kubectl apply -f sc-productpage-egress-hosts-double-star.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: Sidecar
metadata:
  name: productpage
spec:
  workloadSelector:
    labels:
      app: productpage
  egress:
  - hosts:
    - "*/*"
```

virtaulservice/vs-bookinfo-star.yam

kubectl apply -f vs-bookinfo-star.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: bookinfo
spec:
  exportTo:
  - '*'
  hosts:
  - "*"
  gateways:
  - bookinfo-gateway
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



kubectl apply -f gateway-01.yaml -n istio

gateway/gateway-01.yaml

```
apiVersion: networking.istio.io/v1beta1
kind: Gateway
metadata:
  name: bookinfo-gateway
spec:
  selector:
    istio: ingressgateway
  servers:
  - port:
      number: 80
      name: http
      protocol: HTTP
    hosts:
    - "*"
```

访问

清理：

kubectl delete sidecar productpage -n istio

kubectl delete vs bookinfo -n istio

kubectl delete gw bookinfo-gateway -n istio

![1628574002](images\1628574002.jpg)







#### specific

sc-productpage-egress-hosts-specific.yaml

kubectl apply -f sc-productpage-egress-hosts-specific.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: Sidecar
metadata:
  name: productpage
spec:
  workloadSelector:
    labels:
      app: productpage
  egress:
  - hosts:
    - "istio/details.istio.svc.cluster.local"
```

virtaulservice/vs-bookinfo-star.yam

kubectl apply -f vs-bookinfo-star.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: bookinfo
spec:
  exportTo:
  - '*'
  hosts:
  - "*"
  gateways:
  - bookinfo-gateway
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



kubectl apply -f gateway-01.yaml -n istio

gateway/gateway-01.yaml

```
apiVersion: networking.istio.io/v1beta1
kind: Gateway
metadata:
  name: bookinfo-gateway
spec:
  selector:
    istio: ingressgateway
  servers:
  - port:
      number: 80
      name: http
      protocol: HTTP
    hosts:
    - "*"
```

访问

清理：

kubectl delete sidecar productpage -n istio

kubectl delete vs bookinfo -n istio

kubectl delete gw bookinfo-gateway -n istio

![1628574096(1)](images\1628574096(1).jpg)







## 10-5ingress

| Field             | Type          | Description                                                  | Required |
| ----------------- | ------------- | ------------------------------------------------------------ | -------- |
| `port`            | `Port`        | The port associated with the listener.                       | Yes      |
| `bind`            | `string`      | The IP to which the listener should be bound. Must be in the format `x.x.x.x`. Unix domain socket addresses are not allowed in the bind field for ingress listeners. If omitted, Istio will automatically configure the defaults based on imported services and the workload instances to which this configuration is applied to. | No       |
| `captureMode`     | `CaptureMode` | The captureMode option dictates how traffic to the listener is expected to be captured (or not). | No       |
| `defaultEndpoint` | `string`      | The loopback IP endpoint or Unix domain socket to which traffic should be forwarded to. This configuration can be used to redirect traffic arriving at the bind `IP:Port` on the sidecar to a `localhost:port` or Unix domain socket where the application workload instance is listening for connections. Format should be `127.0.0.1:PORT` or `unix:///path/to/socket` | Yes      |

### port

sc-productpage-ingress-port.yaml

kubectl apply -f sc-productpage-ingress-port.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: Sidecar
metadata:
  name: productpage
spec:
  workloadSelector:
    labels:
      app: productpage
  ingress:
  - captureMode: IPTABLES
    defaultEndpoint: 127.0.0.1:9080
    port:
      number: 9080
      protocol: HTTP
      name: http
```

virtaulservice/vs-bookinfo-star.yam

kubectl apply -f vs-bookinfo-star.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: bookinfo
spec:
  exportTo:
  - '*'
  hosts:
  - "*"
  gateways:
  - bookinfo-gateway
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



kubectl apply -f gateway-01.yaml -n istio

gateway/gateway-01.yaml

```
apiVersion: networking.istio.io/v1beta1
kind: Gateway
metadata:
  name: bookinfo-gateway
spec:
  selector:
    istio: ingressgateway
  servers:
  - port:
      number: 80
      name: http
      protocol: HTTP
    hosts:
    - "*"
```

访问

这个port影响的是监听的端口

清理：

kubectl delete sidecar productpage -n istio

kubectl delete vs bookinfo -n istio

kubectl delete gw bookinfo-gateway -n istio

kubectl port-forward --address 0.0.0.0 -n istio productpage-v1-659776cb44-rrr87 15000:15000

![1628577377(1)](images\1628577377(1).jpg)







### bind

sc-productpage-ingress-bind.yaml

kubectl apply -f sc-productpage-ingress-bind.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: Sidecar
metadata:
  name: productpage
spec:
  workloadSelector:
    labels:
      app: productpage
  ingress:
  - captureMode: IPTABLES
    bind: 0.0.0.0
    defaultEndpoint: 127.0.0.1:9080
    port:
      number: 9080
      protocol: HTTP
      name: http
```

virtaulservice/vs-bookinfo-star.yam

kubectl apply -f vs-bookinfo-star.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: bookinfo
spec:
  exportTo:
  - '*'
  hosts:
  - "*"
  gateways:
  - bookinfo-gateway
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



kubectl apply -f gateway-01.yaml -n istio

gateway/gateway-01.yaml

```
apiVersion: networking.istio.io/v1beta1
kind: Gateway
metadata:
  name: bookinfo-gateway
spec:
  selector:
    istio: ingressgateway
  servers:
  - port:
      number: 80
      name: http
      protocol: HTTP
    hosts:
    - "*"
```

访问

这个port影响的是监听的端口

清理：

kubectl delete sidecar productpage -n istio

kubectl delete vs bookinfo -n istio

kubectl delete gw bookinfo-gateway -n istio

kubectl port-forward --address 0.0.0.0 -n istio productpage-v1-659776cb44-rrr87 15000:15000

0.0.0.0不显示

![1628577917(1)](images\1628577917(1).jpg)







sc-productpage-ingress-bind-pod-ip.yaml

kubectl apply -f sc-productpage-ingress-bind-pod-ip.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: Sidecar
metadata:
  name: productpage
spec:
  workloadSelector:
    labels:
      app: productpage
  ingress:
  - captureMode: IPTABLES
    bind: 172.20.1.174
    defaultEndpoint: 127.0.0.1:9080
    port:
      number: 9080
      protocol: HTTP
      name: http
```

bind pod ip

virtaulservice/vs-bookinfo-star.yam

kubectl apply -f vs-bookinfo-star.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: bookinfo
spec:
  exportTo:
  - '*'
  hosts:
  - "*"
  gateways:
  - bookinfo-gateway
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



kubectl apply -f gateway-01.yaml -n istio

gateway/gateway-01.yaml

```
apiVersion: networking.istio.io/v1beta1
kind: Gateway
metadata:
  name: bookinfo-gateway
spec:
  selector:
    istio: ingressgateway
  servers:
  - port:
      number: 80
      name: http
      protocol: HTTP
    hosts:
    - "*"
```

访问

这个port影响的是监听的端口

清理：

kubectl delete sidecar productpage -n istio

kubectl delete vs bookinfo -n istio

kubectl delete gw bookinfo-gateway -n istio

kubectl port-forward --address 0.0.0.0 -n istio productpage-v1-659776cb44-rrr87 15000:15000

![1628577848](images\1628577848.jpg)





### captureMode

| Name       | Description                                                  |
| ---------- | ------------------------------------------------------------ |
| `DEFAULT`  | The default capture mode defined by the environment.         |
| `IPTABLES` | Capture traffic using IPtables redirection.                  |
| `NONE`     | No traffic capture. When used in an egress listener, the application is expected to explicitly communicate with the listener port or Unix domain socket. When used in an ingress listener, care needs to be taken to ensure that the listener port is not in use by other processes on the host. |

#### DEFAULT

sc-productpage-ingress-capture-mode-DEFAULT.yaml

kubectl apply -f sc-productpage-ingress-capture-mode-DEFAULT.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: Sidecar
metadata:
  name: productpage
spec:
  workloadSelector:
    labels:
      app: productpage
  ingress:
  - captureMode: DEFAULT
    bind: 0.0.0.0
    defaultEndpoint: 127.0.0.1:9080
    port:
      number: 9080
      protocol: HTTP
      name: http
```

virtaulservice/vs-bookinfo-star.yam

kubectl apply -f vs-bookinfo-star.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: bookinfo
spec:
  exportTo:
  - '*'
  hosts:
  - "*"
  gateways:
  - bookinfo-gateway
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



kubectl apply -f gateway-01.yaml -n istio

gateway/gateway-01.yaml

```
apiVersion: networking.istio.io/v1beta1
kind: Gateway
metadata:
  name: bookinfo-gateway
spec:
  selector:
    istio: ingressgateway
  servers:
  - port:
      number: 80
      name: http
      protocol: HTTP
    hosts:
    - "*"
```

访问

默认是iptables

清理：

kubectl delete sidecar productpage -n istio

kubectl delete vs bookinfo -n istio

kubectl delete gw bookinfo-gateway -n istio

kubectl port-forward --address 0.0.0.0 -n istio productpage-v1-659776cb44-rrr87 15000:15000



![1628578363(1)](images\1628578363(1).jpg)





#### IPTABLES

sc-productpage-ingress-capture-mode-IPTABLES.yaml

kubectl apply -f sc-productpage-ingress-capture-mode-IPTABLES.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: Sidecar
metadata:
  name: productpage
spec:
  workloadSelector:
    labels:
      app: productpage
  ingress:
  - captureMode: IPTABLES
    bind: 0.0.0.0
    defaultEndpoint: 127.0.0.1:9080
    port:
      number: 9080
      protocol: HTTP
      name: http
```

virtaulservice/vs-bookinfo-star.yam

kubectl apply -f vs-bookinfo-star.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: bookinfo
spec:
  exportTo:
  - '*'
  hosts:
  - "*"
  gateways:
  - bookinfo-gateway
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



kubectl apply -f gateway-01.yaml -n istio

gateway/gateway-01.yaml

```
apiVersion: networking.istio.io/v1beta1
kind: Gateway
metadata:
  name: bookinfo-gateway
spec:
  selector:
    istio: ingressgateway
  servers:
  - port:
      number: 80
      name: http
      protocol: HTTP
    hosts:
    - "*"
```

访问

清理：

kubectl delete sidecar productpage -n istio

kubectl delete vs bookinfo -n istio

kubectl delete gw bookinfo-gateway -n istio

kubectl port-forward --address 0.0.0.0 -n istio productpage-v1-659776cb44-rrr87 15000:15000

![1628578416(1)](images\1628578416(1).jpg)







#### NONE

sc-productpage-ingress-capture-mode-NONE.yaml

kubectl apply -f sc-productpage-ingress-capture-mode-NONE.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: Sidecar
metadata:
  name: productpage
spec:
  workloadSelector:
    labels:
      app: productpage
  ingress:
  - captureMode: NONE
    defaultEndpoint: 127.0.0.1:9080
    port:
      number: 9080
      protocol: HTTP
      name: http
```

virtaulservice/vs-bookinfo-star.yam

kubectl apply -f vs-bookinfo-star.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: bookinfo
spec:
  exportTo:
  - '*'
  hosts:
  - "*"
  gateways:
  - bookinfo-gateway
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



kubectl apply -f gateway-01.yaml -n istio

gateway/gateway-01.yaml

```
apiVersion: networking.istio.io/v1beta1
kind: Gateway
metadata:
  name: bookinfo-gateway
spec:
  selector:
    istio: ingressgateway
  servers:
  - port:
      number: 80
      name: http
      protocol: HTTP
    hosts:
    - "*"
```

访问

清理：

kubectl delete sidecar productpage -n istio

kubectl delete vs bookinfo -n istio

kubectl delete gw bookinfo-gateway -n istio

kubectl port-forward --address 0.0.0.0 -n istio productpage-v1-659776cb44-rrr87 15000:15000









### defaultEndpoint

#### unix socket

1部署mysqlgateway

gateway/gateway-mysql.yam

kubectl apply -f gateway-mysql.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: Gateway
metadata:
  name: mysql
spec:
  selector:
    istio: ingressgateway
  servers:
  - port:
      number: 3306
      name: mysql
      protocol: MYSQL
    hosts:
    - "*"
```

2部署mysql vs

gateway/protocol/vs-mysql.yaml

kubectl apply -f vs-mysql.yaml

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: mysql
spec:
  hosts:
  - "*"
  gateways:
  - mysql
  tcp:
  - match:
    - port: 3306
    route:
    - destination:
        host: mysqldb.istio.svc.cluster.local
        port:
          number: 3306
```

3添加svc 端口

kubectl edit svc istio-ingressgateway -n istio-system

3306端口



4部署sidecar

sc-mysql-defaultEndpoint-unix.yaml

 当绑定地址是IP时，captureMode选项指示如何劫持（或不劫持）到监听器的流量。 对于Unix domain socket，captureMode必须为DEFAULT或NONE。

kubectl apply -f sc-mysql-defaultEndpoint-unix.yaml -n istio 

```
apiVersion: networking.istio.io/v1beta1
kind: Sidecar
metadata:
  name: mysql
spec:
  workloadSelector:
    labels:
      app: mysqldb
  ingress:
  - bind: 0.0.0.0
    port:
      number: 3306
      protocol: MYSQL
      name: mysql
    defaultEndpoint: unix:///var/run/mysqld/mysqld.sock
    captureMode: NONE
```

ingress/bookinfo-mysql.yaml

部署mysql

kubectl apply -f bookinfo-mysql.yaml -n istio

```
apiVersion: v1
kind: Secret
metadata:
  name: mysql-credentials
type: Opaque
data:
  rootpasswd: cGFzc3dvcmQ=
---
apiVersion: v1
kind: Service
metadata:
  name: mysqldb
  labels:
    app: mysqldb
    service: mysqldb
spec:
  ports:
  - port: 3306
    name: tcp
  selector:
    app: mysqldb
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: mysqldb-v1
  labels:
    app: mysqldb
    version: v1
spec:
  replicas: 1
  selector:
    matchLabels:
      app: mysqldb
      version: v1
  template:
    metadata:
      labels:
        app: mysqldb
        version: v1
    spec:
      containers:
      - name: mysqldb
        image: docker.io/istio/examples-bookinfo-mysqldb:1.16.2
        imagePullPolicy: IfNotPresent
        ports:
        - containerPort: 3306
        env:
          - name: MYSQL_ROOT_PASSWORD
            valueFrom:
              secretKeyRef:
                name: mysql-credentials
                key: rootpasswd
        args: ["--default-authentication-plugin","mysql_native_password"]
        volumeMounts:
        - name: var-lib-mysql
          mountPath: /var/lib/mysql
      volumes:
      - name: var-lib-mysql
        emptyDir: {}
---
```

访问

mysql  -h 192.168.229.134 --port 31778 -u root -p

清理：

kubectl delete -f bookinfo-mysql.yaml -n istio

kubectl delete sidecar mysql -n istio

kubectl delete vs mysql -n istio

kubectl delete gw mysql -n istio



kubectl port-forward --address 0.0.0.0 -n istio mysqldb-v1-6f68664cbf-mrl22 15001:15000

![1628578839(1)](images\1628578839(1).jpg)

![1628578879(1)](images\1628578879(1).jpg)





#### ip -port

sc-productpage-ingerss-defaultEndpoint-ip.yaml

kubectl apply -f sc-productpage-ingerss-defaultEndpoint-ip.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: Sidecar
metadata:
  name: productpage
spec:
  workloadSelector:
    labels:
      app: productpage
  ingress:
  - captureMode: NONE
    defaultEndpoint: 127.0.0.1:9080
    port:
      number: 9080
      protocol: HTTP
      name: http
```

virtaulservice/vs-bookinfo-star.yam

kubectl apply -f vs-bookinfo-star.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: bookinfo
spec:
  exportTo:
  - '*'
  hosts:
  - "*"
  gateways:
  - bookinfo-gateway
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



kubectl apply -f gateway-01.yaml -n istio

gateway/gateway-01.yaml

```
apiVersion: networking.istio.io/v1beta1
kind: Gateway
metadata:
  name: bookinfo-gateway
spec:
  selector:
    istio: ingressgateway
  servers:
  - port:
      number: 80
      name: http
      protocol: HTTP
    hosts:
    - "*"
```

访问

清理：

kubectl delete sidecar productpage -n istio

kubectl delete vs bookinfo -n istio

kubectl delete gw bookinfo-gateway -n istio

kubectl port-forward --address 0.0.0.0 -n istio productpage-v1-659776cb44-rrr87 15000:15000

![1628578976(1)](images\1628578976(1).jpg)

![1628579017(1)](images\1628579017(1).jpg)





## 10-6outboundTrafficPolicy

### egressProxy

| egressProxy | Destination |      | Specifies the details of the egress proxy to which unknown traffic should be forwarded to from the sidecar. Valid only if the mode is set to ALLOW_ANY. If not specified when the mode is ALLOW_ANY, the sidecar will send the unknown traffic directly to the IP requested by the application. ** NOTE 1**: The specified egress host must be imported in the egress section for the traffic forwarding to work. ** NOTE 2**: An Envoy based egress gateway is unlikely to be able to handle plain text TCP connections forwarded from the sidecar. Envoy's dynamic forward proxy can handle only HTTP and TLS connections. $hide_from_docs |
| ----------- | ----------- | ---- | ------------------------------------------------------------ |
|             |             |      |                                                              |

| FIELD  |     TYPE     | LABEL |                         DESCRIPTION                          |
| :----: | :----------: | :---: | :----------------------------------------------------------: |
|  host  |    string    |       | The name of a service from the service registry. Service names are looked up from the platform's service registry (e.g., Kubernetes services, Consul services, etc.) and from the hosts declared by [ServiceEntry](https://istio.io/docs/reference/config/networking/service-entry/#ServiceEntry). Traffic forwarded to destinations that are not found in either of the two, will be dropped. *Note for Kubernetes users*: When short names are used (e.g. “reviews” instead of “reviews.default.svc.cluster.local”), Istio will interpret the short name based on the namespace of the rule, not the service. A rule in the “default” namespace containing a host “reviews will be interpreted as “reviews.default.svc.cluster.local”, irrespective of the actual namespace associated with the reviews service. To avoid potential misconfiguration, it is recommended to always use fully qualified domain names over short names. |
| subset |    string    |       | The name of a subset within the service. Applicable only to services within the mesh. The subset must be defined in a corresponding DestinationRule. |
|  port  | PortSelector |       | Specifies the port on the host that is being addressed. If a service exposes only a single port it is not required to explicitly select the port |

#### host，port

sc-productpage-outboundTrafficPolicy-egressProxy-host.yaml

kubectl  apply -f sc-productpage-outboundTrafficPolicy-egressProxy-host.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: Sidecar
metadata:
  name: sleep
spec:
  workloadSelector:
    labels:
      app: sleep
  outboundTrafficPolicy:
    egressProxy:
      host: "details.default.svc.cluster.local"
      port:
        number: 9080
    mode: ALLOW_ANY
```

sleep.yaml

kubectl apply -f sleep.yaml -n istio

```
apiVersion: v1
kind: ServiceAccount
metadata:
  name: sleep
---
apiVersion: v1
kind: Service
metadata:
  name: sleep
  labels:
    app: sleep
    service: sleep
spec:
  ports:
  - port: 80
    name: http
  selector:
    app: sleep
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: sleep
spec:
  replicas: 1
  selector:
    matchLabels:
      app: sleep
  template:
    metadata:
      labels:
        app: sleep
    spec:
      terminationGracePeriodSeconds: 0
      serviceAccountName: sleep
      containers:
      - name: sleep
        image: curlimages/curl
        command: ["/bin/sleep", "3650d"]
        imagePullPolicy: IfNotPresent
        volumeMounts:
        - mountPath: /etc/sleep/tls
          name: secret-volume
      volumes:
      - name: secret-volume
        secret:
          secretName: sleep-secret
          optional: true
---
```



detail-deploy.yaml

kubectl apply -f detail-deploy.yaml -n default

```
apiVersion: v1
kind: Service
metadata:
  name: details
  labels:
    app: details
    service: details
spec:
  ports:
  - port: 9080
    name: http
  selector:
    app: details
---
apiVersion: v1
kind: ServiceAccount
metadata:
  name: bookinfo-details
  labels:
    account: details
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: details-v1
  labels:
    app: details
    version: v1
spec:
  replicas: 1
  selector:
    matchLabels:
      app: details
      version: v1
  template:
    metadata:
      labels:
        app: details
        version: v1
    spec:
      serviceAccountName: bookinfo-details
      containers:
      - name: details
        image: docker.io/istio/examples-bookinfo-details-v1:1.16.2
        imagePullPolicy: IfNotPresent
        ports:
        - containerPort: 9080
        securityContext:
          runAsUser: 1000
```



kubectl exec -it -n istio sleep-557747455f-9n9tn -- /bin/sh

curl details.default.svc.cluster.local:9080/details/0



清理：

kubectl delete -f detail-deploy.yaml -n default

kubectl delete sidecar sleep -n istio

kubectl delete -f sleep.yaml -n istio





#### subset

sc-productpage-outboundTrafficPolicy-egressProxy-subset.yaml

kubectl apply -f sc-productpage-outboundTrafficPolicy-egressProxy-subset.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: Sidecar
metadata:
  name: sleep
spec:
  workloadSelector:
    labels:
      app: sleep
  outboundTrafficPolicy:
    egressProxy:
      host: "details.default.svc.cluster.local"
      port:
        number: 9080
      subset: v1
    mode: ALLOW_ANY
```

dr-details.yaml

kubectl apply -f dr-details.yaml

```
apiVersion: networking.istio.io/v1alpha3
kind: DestinationRule
metadata:
  name: details
spec:
  host: details
  subsets:
  - name: v1
    labels:
      version: v1
```

sleep.yaml

kubectl apply -f sleep.yaml -n istio

```
apiVersion: v1
kind: ServiceAccount
metadata:
  name: sleep
---
apiVersion: v1
kind: Service
metadata:
  name: sleep
  labels:
    app: sleep
    service: sleep
spec:
  ports:
  - port: 80
    name: http
  selector:
    app: sleep
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: sleep
spec:
  replicas: 1
  selector:
    matchLabels:
      app: sleep
  template:
    metadata:
      labels:
        app: sleep
    spec:
      terminationGracePeriodSeconds: 0
      serviceAccountName: sleep
      containers:
      - name: sleep
        image: curlimages/curl
        command: ["/bin/sleep", "3650d"]
        imagePullPolicy: IfNotPresent
        volumeMounts:
        - mountPath: /etc/sleep/tls
          name: secret-volume
      volumes:
      - name: secret-volume
        secret:
          secretName: sleep-secret
          optional: true
---
```



detail-deploy.yaml

kubectl apply -f detail-deploy.yaml -n default

```
apiVersion: v1
kind: Service
metadata:
  name: details
  labels:
    app: details
    service: details
spec:
  ports:
  - port: 9080
    name: http
  selector:
    app: details
---
apiVersion: v1
kind: ServiceAccount
metadata:
  name: bookinfo-details
  labels:
    account: details
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: details-v1
  labels:
    app: details
    version: v1
spec:
  replicas: 1
  selector:
    matchLabels:
      app: details
      version: v1
  template:
    metadata:
      labels:
        app: details
        version: v1
    spec:
      serviceAccountName: bookinfo-details
      containers:
      - name: details
        image: docker.io/istio/examples-bookinfo-details-v1:1.16.2
        imagePullPolicy: IfNotPresent
        ports:
        - containerPort: 9080
        securityContext:
          runAsUser: 1000
```



kubectl exec -it -n istio sleep-557747455f-9n9tn -- /bin/sh

curl details.default.svc.cluster.local:9080/details/0

直接访问，访问的是service，不是dr v1，只有用vs指定subset，才可以访问dr v1



清理：

kubectl delete -f detail-deploy.yaml -n default

kubectl delete sidecar sleep -n istio

kubectl delete -f sleep.yaml -n istio

kubectl delete dr details 





### mode

| Name            | Description                                                  |
| --------------- | ------------------------------------------------------------ |
| `REGISTRY_ONLY` | Outbound traffic will be restricted to services defined in the service registry as well as those defined through `ServiceEntry` configurations. |
| `ALLOW_ANY`     | Outbound traffic to unknown destinations will be allowed, in case there are no services or `ServiceEntry` configurations for the destination port. |

#### REGISTRY_ONLY

sc-productpage-outboundTrafficPolicy-mode-REGISTRY_ONLY.yaml

kubectl apply -f sc-productpage-outboundTrafficPolicy-mode-REGISTRY_ONLY.yaml  -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: Sidecar
metadata:
  name: sleep
spec:
  workloadSelector:
    labels:
      app: sleep
  outboundTrafficPolicy:
    mode: REGISTRY_ONLY
```

sleep.yaml

kubectl apply -f sleep.yaml -n istio

```
apiVersion: v1
kind: ServiceAccount
metadata:
  name: sleep
---
apiVersion: v1
kind: Service
metadata:
  name: sleep
  labels:
    app: sleep
    service: sleep
spec:
  ports:
  - port: 80
    name: http
  selector:
    app: sleep
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: sleep
spec:
  replicas: 1
  selector:
    matchLabels:
      app: sleep
  template:
    metadata:
      labels:
        app: sleep
    spec:
      terminationGracePeriodSeconds: 0
      serviceAccountName: sleep
      containers:
      - name: sleep
        image: curlimages/curl
        command: ["/bin/sleep", "3650d"]
        imagePullPolicy: IfNotPresent
        volumeMounts:
        - mountPath: /etc/sleep/tls
          name: secret-volume
      volumes:
      - name: secret-volume
        secret:
          secretName: sleep-secret
          optional: true
---
```



kubectl exec -it -n istio sleep-557747455f-9n9tn -- /bin/sh

curl www.baidu.com



清理：

kubectl delete -f sleep.yaml -n istio

kubectl  delete sidecar sleep -n istio



#### ALLOW_ANY

sc-productpage-outboundTrafficPolicy-mode-ALLOW_ANY.yaml

kubectl apply -f sc-productpage-outboundTrafficPolicy-mode-ALLOW_ANY.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: Sidecar
metadata:
  name: sleep
spec:
  workloadSelector:
    labels:
      app: sleep
  outboundTrafficPolicy:
    mode: ALLOW_ANY
```

sleep.yaml

kubectl apply -f sleep.yaml -n istio

```
apiVersion: v1
kind: ServiceAccount
metadata:
  name: sleep
---
apiVersion: v1
kind: Service
metadata:
  name: sleep
  labels:
    app: sleep
    service: sleep
spec:
  ports:
  - port: 80
    name: http
  selector:
    app: sleep
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: sleep
spec:
  replicas: 1
  selector:
    matchLabels:
      app: sleep
  template:
    metadata:
      labels:
        app: sleep
    spec:
      terminationGracePeriodSeconds: 0
      serviceAccountName: sleep
      containers:
      - name: sleep
        image: curlimages/curl
        command: ["/bin/sleep", "3650d"]
        imagePullPolicy: IfNotPresent
        volumeMounts:
        - mountPath: /etc/sleep/tls
          name: secret-volume
      volumes:
      - name: secret-volume
        secret:
          secretName: sleep-secret
          optional: true
---
```



kubectl exec -it -n istio sleep-557747455f-9n9tn -- /bin/sh

curl www.baidu.com



清理：

kubectl delete -f sleep.yaml -n istio

kubectl  delete sidecar sleep -n istio



## 10-7组合应用

sc-productpage-complex.yaml

kubectl apply -f sc-productpage-complex.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: Sidecar
metadata:
  name: productpage
spec:
  workloadSelector:
    labels:
      app: productpage
  ingress:
  - captureMode: IPTABLES
    defaultEndpoint: 127.0.0.1:9080
    port:
      number: 9080
      protocol: HTTP
      name: http
  egress:
  - hosts:
    - "./*"
    port:
      number: 9080
      protocol: HTTP
      name: egresshttp
    bind: 0.0.0.0
    captureMode: IPTABLES
  outboundTrafficPolicy:
    mode: REGISTRY_ONLY
```

outbound将不能访问外部未注册服务

访问



## 使用ServiceEntry

1进入pod访问www.baidu.com

sleep.yaml

kubectl apply -f sleep.yaml -n istio

```
apiVersion: v1
kind: ServiceAccount
metadata:
  name: sleep
---
apiVersion: v1
kind: Service
metadata:
  name: sleep
  labels:
    app: sleep
    service: sleep
spec:
  ports:
  - port: 80
    name: http
  selector:
    app: sleep
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: sleep
spec:
  replicas: 1
  selector:
    matchLabels:
      app: sleep
  template:
    metadata:
      labels:
        app: sleep
    spec:
      terminationGracePeriodSeconds: 0
      serviceAccountName: sleep
      containers:
      - name: sleep
        image: curlimages/curl
        command: ["/bin/sleep", "3650d"]
        imagePullPolicy: IfNotPresent
        volumeMounts:
        - mountPath: /etc/sleep/tls
          name: secret-volume
      volumes:
      - name: secret-volume
        secret:
          secretName: sleep-secret
          optional: true
---
```



kubectl exec -it sleep-557747455f-ft9bs   -n istio -- /bin/sh

curl www.baidu.com

可以访问

2部署sidecar

sc-sleep-REGISTRY_ONLY.yaml

kubectl apply -f sc-sleep-REGISTRY_ONLY.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: Sidecar
metadata:
  name: sleep
spec:
  workloadSelector:
    labels:
      app: sleep
  outboundTrafficPolicy:
    mode: REGISTRY_ONLY
```

3在访问www.baidu.com

不能访问

4部署serviceentry

serviceentries/se-baidu.yaml 

kubectl apply -f se-baidu.yaml  -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: ServiceEntry
metadata:
  name: baidu
spec:
  hosts:
  - "www.baidu.com"
  ports:
  - number: 80
    name: http
    protocol: HTTP
  location: MESH_EXTERNAL
  resolution: DNS
```

5再访问www.baidu.com

可以访问

清理：

kubectl  delete se baidu -n istio

kubectl delete sidecar sleep -n istio

kubectl delete -f sleep.yaml -n istio