# 学习目标

![1618889609(1)](images\1618889609(1).jpg)

# 什么是PeerAuthentication

 PeerAuthentication defines how traffic will be（mtls） tunneled (or not) to the sidecar. 

# 资源详解

| Field           | Type               | Description                                                  | Required |
| --------------- | ------------------ | ------------------------------------------------------------ | -------- |
| `selector`      | `WorkloadSelector` | The selector determines the workloads to apply the ChannelAuthentication on. If not set, the policy will be applied to all workloads in the same namespace as the policy. | No       |
| `mtls`          | `MutualTLS`        | Mutual TLS settings for workload. If not defined, inherit from parent. | No       |
| `portLevelMtls` | `map`              | Port specific mutual TLS settings.                           | No       |

## 没有selector

### 全局有效

pa-default-global.yaml

kubectl apply -f pa-default-global.yaml -n istio-system

```
apiVersion: security.istio.io/v1beta1
kind: PeerAuthentication
metadata:
  name: default
  namespace: istio-system
spec:
  mtls:
    mode: STRICT
```

 For mesh level, put the policy in root-namespace according to your Istio installation. 

默认工作负载都启用mtls





kubectl port-forward --address 0.0.0.0 -n istio productpage-v1-659776cb44-q8xzp 15000:15000

![1628670845(1)](images\1628670845(1).jpg)



![1628739068(1)](images\1628739068(1).jpg)



关闭productpage mtls

 dr-productpage-mtls-disable.yaml

kubectl apply -f  dr-productpage-mtls-disable.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: DestinationRule
metadata:
  name: productpage
spec:
  host: productpage
  subsets:
  - labels:
      version: v1
    name: v1
  trafficPolicy:
    tls:
      mode: DISABLE
```

virtaulservice/vs-bookinfo-star.yaml

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

kubectl apply -f vs-bookinfo-star.yaml -n istio

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



访问失败



清理：

kubectl delete PeerAuthentication default -n istio-system

kubectl delete dr productpage -n istio

kubectl delete gw bookinfo-gateway -n istio

kubectl delete vs bookinfo -n istio



kubectl port-forward --address 0.0.0.0 -n istio productpage-v1-659776cb44-q8xzp 15000:15000

没有匹配的tlsMode: "istio"所以访问失败

![1628739181(1)](images\1628739181(1).jpg)



![1628739255(1)](images\1628739255(1).jpg)



### 名称空间有效

pa-default.yaml

kubectl apply -f pa-default.yaml -n istio

```
apiVersion: security.istio.io/v1beta1
kind: PeerAuthentication
metadata:
  name: default
spec:
  mtls:
    mode: STRICT
```

virtaulservice/vs-bookinfo-star.yaml

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

kubectl apply -f vs-bookinfo-star.yaml -n istio

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

kubectl delete PeerAuthentication default -n istio

kubectl delete gw bookinfo-gateway -n istio

kubectl delete vs bookinfo -n istio



kubectl port-forward --address 0.0.0.0 -n istio productpage-v1-659776cb44-q8xzp 15000:15000

![1628739401(1)](images\1628739401(1).jpg)



![1628739437(1)](images\1628739437(1).jpg)



## selector

pa-productpage-selector.yaml

kubectl apply -f pa-productpage-selector.yaml -n istio

```
apiVersion: security.istio.io/v1beta1
kind: PeerAuthentication
metadata:
  name: productpage
spec:
  selector:
    matchLabels:
      app: productpage
  mtls:
    mode: STRICT
```

virtaulservice/vs-bookinfo-star.yaml

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

kubectl apply -f vs-bookinfo-star.yaml -n istio

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

kubectl delete PeerAuthentication productpage -n istio

kubectl delete gw bookinfo-gateway -n istio

kubectl delete vs bookinfo -n istio



kubectl port-forward --address 0.0.0.0 -n istio productpage-v1-659776cb44-q8xzp 15000:15000

![1628739568(1)](images\1628739568(1).jpg)

![1628739689(1)](images\1628739689(1).jpg)



## mtls

PeerAuthentication.MutualTLS.Mode

| Name         | Description                                                  |
| ------------ | ------------------------------------------------------------ |
| `UNSET`      | Inherit from parent, if has one. Otherwise treated as PERMISSIVE. |
| `DISABLE`    | Connection is not tunneled.                                  |
| `PERMISSIVE` | Connection can be either plaintext or mTLS tunnel.           |
| `STRICT`     | Connection is an mTLS tunnel (TLS with client cert must be presented). |

### mode

#### UNSET

pa-productpage-mode-UNSET.yaml

kubectl apply -f pa-productpage-mode-UNSET.yaml -n istio

```
apiVersion: security.istio.io/v1beta1
kind: PeerAuthentication
metadata:
  name: productpage
spec:
  selector:
    matchLabels:
      app: productpage
  mtls:
    mode: UNSET
```

pa-default-global-disable.yaml

kubectl apply -f pa-default-global-disable.yaml -n istio-system

```
apiVersion: security.istio.io/v1beta1
kind: PeerAuthentication
metadata:
  name: default
  namespace: istio-system
spec:
  mtls:
    mode: DISABLE
```

virtaulservice/vs-bookinfo-star.yaml

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

kubectl apply -f vs-bookinfo-star.yaml -n istio

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

kubectl delete PeerAuthentication productpage -n istio

kubectl delete PeerAuthentication default -n istio-system

kubectl delete gw bookinfo-gateway -n istio

kubectl delete vs bookinfo -n istio



kubectl port-forward --address 0.0.0.0 -n istio productpage-v1-659776cb44-q8xzp 15000:15000

![1628740553(1)](images\1628740553(1).jpg)



#### DISABLE

pa-productpage-mode-DISABLE.yaml

kubectl apply -f pa-productpage-mode-DISABLE.yaml -n istio

```
apiVersion: security.istio.io/v1beta1
kind: PeerAuthentication
metadata:
  name: productpage
spec:
  selector:
    matchLabels:
      app: productpage
  mtls:
    mode: DISABLE
```

pa-default-global-disable.yaml

kubectl apply -f pa-default-global-disable.yaml -n istio-system

```
apiVersion: security.istio.io/v1beta1
kind: PeerAuthentication
metadata:
  name: default
  namespace: istio-system
spec:
  mtls:
    mode: DISABLE
```



virtaulservice/vs-bookinfo-star.yaml

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

kubectl apply -f vs-bookinfo-star.yaml -n istio

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

kubectl delete PeerAuthentication productpage -n istio

kubectl delete PeerAuthentication default -n istio-system

kubectl delete gw bookinfo-gateway -n istio

kubectl delete vs bookinfo -n istio



kubectl port-forward --address 0.0.0.0 -n istio productpage-v1-659776cb44-q8xzp 15000:15000

![1628740858(1)](images\1628740858(1).jpg)

#### PERMISSIVE

pa-productpage-mode-PERMISSIVE.yaml

kubectl apply -f pa-productpage-mode-PERMISSIVE.yaml -n istio

```
apiVersion: security.istio.io/v1beta1
kind: PeerAuthentication
metadata:
  name: productpage
spec:
  selector:
    matchLabels:
      app: productpage
  mtls:
    mode: PERMISSIVE
```

virtaulservice/vs-bookinfo-star.yaml

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

kubectl apply -f vs-bookinfo-star.yaml -n istio

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

kubectl delete PeerAuthentication productpage -n istio

kubectl delete gw bookinfo-gateway -n istio

kubectl delete vs bookinfo -n istio



kubectl port-forward --address 0.0.0.0 -n istio productpage-v1-659776cb44-q8xzp 15000:15000

![1628741173(1)](images\1628741173(1).jpg)

#### STRICT

pa-productpage-mode-STRICT.yaml

kubectl apply -f pa-productpage-mode-STRICT.yaml -n istio

```
apiVersion: security.istio.io/v1beta1
kind: PeerAuthentication
metadata:
  name: productpage
spec:
  selector:
    matchLabels:
      app: productpage
  mtls:
    mode: STRICT
```

virtaulservice/vs-bookinfo-star.yaml

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

kubectl apply -f vs-bookinfo-star.yaml -n istio

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

kubectl delete PeerAuthentication productpage -n istio

kubectl delete gw bookinfo-gateway -n istio

kubectl delete vs bookinfo -n istio



kubectl port-forward --address 0.0.0.0 -n istio productpage-v1-659776cb44-q8xzp 15000:15000

![1628740702(1)](images\1628740702(1).jpg)



## portLevelMtls

pa-productpage-portLevelMtls.yaml

kubectl apply -f pa-productpage-portLevelMtls.yaml -n istio

```
apiVersion: security.istio.io/v1beta1
kind: PeerAuthentication
metadata:
  name: productpage
spec:
  selector:
    matchLabels:
      app: productpage
  mtls:
    mode: STRICT
  portLevelMtls:
    9080:
      mode: DISABLE
```

pa-default-global-disable.yaml

kubectl apply -f pa-default-global-disable.yaml -n istio-system

```
apiVersion: security.istio.io/v1beta1
kind: PeerAuthentication
metadata:
  name: default
  namespace: istio-system
spec:
  mtls:
    mode: DISABLE
```



virtaulservice/vs-bookinfo-star.yaml

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

kubectl apply -f vs-bookinfo-star.yaml -n istio

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

kubectl delete PeerAuthentication productpage -n istio

kubectl delete gw bookinfo-gateway -n istio

kubectl delete vs bookinfo -n istio

kubectl delete PeerAuthentication default -n istio-system



kubectl port-forward --address 0.0.0.0 -n istio productpage-v1-659776cb44-q8xzp 15000:15000

![1628741274(1)](images\1628741274(1).jpg)

