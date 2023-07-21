# VirtualService资源详解

## 学习目标

![1597551261(1)](images\1597551261(1).jpg)

## 6-1什么是virtualService

 `VirtualService`中文名称虚拟服务，是istio中一个重要的资源， 它定义了一系列针对指定服务的流量路由规则。每个路由规则都针对特定协议的匹配规则。如果流量符合这些特征，就会根据规则发送到服务注册表中的目标服务（或者目标服务的子集或版本）。 

### vs和k8s service的区别

如果没有 Istio virtual service，仅仅使用 k8s service 的话，那么只能实现最基本的流量负载均衡转发，但是就不能实现类似按百分比来分配流量等更加复杂、丰富、细粒度的流量控制了。

备注：虚拟服务相当于 K8s 服务的 sidecar，在原本 K8s 服务的功能之上，提供了更加丰富的路由控制。

### 例子：

```yaml
apiVersion: networking.istio.io/v1alpha3
kind: VirtualService
metadata:
  name: test-virtual-svc
spec:
  hosts:
  - "web-svc"
  http:
  - route:
    - destination:
        host: web-svc
        subset: nginx
      weight: 25
    - destination:
        host: web-svc
        subset: tomcat
      weight: 75
```

## 配置详解

### 6-2exportTo

#### 1只在当前名称空间有效

virtaulservice/vs-bookinfo-dot.yaml

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: bookinfo
spec:
  exportTo:
  - .
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

kubectl apply -f vs-bookinfo-dot.yaml -n istio

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

kubectl delete gw bookinfo-gateway -n istio

kubectl delete vs bookinfo -n istio

![1627277920(1)](images\1627277920(1).jpg)



 vs-bookinfo-dot-istio-system.yaml

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: bookinfo
spec:
  exportTo:
  - .
  hosts:
  - "*"
  gateways:
  - istio/bookinfo-gateway
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

kubectl apply -f vs-bookinfo-dot-istio-system.yaml -n istio-system

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

访问成功

清理：

kubectl delete gw bookinfo-gateway -n istio

kubectl delete vs bookinfo -n istio-system

![1627278007(1)](images\1627278007(1).jpg)



#### 2所有名称空间有效

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

清理：

kubectl delete gw bookinfo-gateway -n istio

kubectl delete vs bookinfo -n istio

![1627280440(1)](images\1627280440(1).jpg)

![1627280491(1)](images\1627280491(1).jpg)



#### 3特定名称空间有效

virtaulservice/vs-bookinfo-istio-system.yaml

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: bookinfo
spec:
  exportTo:
 # - "default"
 # - "istio"
  - "istio-system"
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

kubectl  apply -f vs-bookinfo-istio-system.yaml -n istio

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

清理：

kubectl delete gw bookinfo-gateway -n istio

kubectl delete vs bookinfo -n istio

访问成功

![1627280761(1)](images\1627280761(1).jpg)

 vs-bookinfo-not-istio-system.yaml 

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: bookinfo
spec:
  exportTo:
  - "default"
  - "istio"
 # - "istio-system"
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

kubectl apply -f  vs-bookinfo-not-istio-system.yaml  -n istio

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

kubectl delete gw bookinfo-gateway -n istio

kubectl delete vs bookinfo -n istio

![1627281002(1)](images\1627281002(1).jpg)



### 6-3gateways

 `Gateway` 名称列表，Sidecar 会据此使用路由。`VirtualService` 对象可以用于网格中的 Sidecar，也可以用于一个或多个 `Gateway`。这里公开的选择条件可以在协议相关的路由过滤条件中进行覆盖。保留字 `mesh` 用来指代网格中的所有 Sidecar。当这一字段被省略时，就会使用缺省值（`mesh`），也就是针对网格中的所有 Sidecar 生效。如果提供了 `gateways` 字段，这一规则就只会应用到声明的 `Gateway` 之中。要让规则同时对 `Gateway` 和网格内服务生效，需要显式的将 `mesh` 加入 `gateways` 列表。 

#### 1单个gateway

virtaulservice/vs-bookinfo-gw-single.yaml

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
          number: 9080
```

kubectl apply -f vs-bookinfo-gw-single.yaml -n istio

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

访问成功

清理：

kubectl delete gw bookinfo-gateway -n istio

kubectl delete vs bookinfo -n istio

![1627284742(1)](images\1627284742(1).jpg)

![1627284814(1)](images\1627284814(1).jpg)

#### 2多个gateway

1创建bookinfo-gateway-02

virtaulservice/gateway-https-02.yaml 

创建secret

kubectl create -n istio-system secret tls istio-ingressgateway-certs --key ./cert.key --cert=./cert.crt

kubectl exec deploy/istio-ingressgateway -n istio-system  -- ls /etc/istio/ingressgateway-certs

```
apiVersion: networking.istio.io/v1beta1
kind: Gateway
metadata:
  name: bookinfo-gateway-02
spec:
  selector:
    istio: ingressgateway
  servers:
  - port:
      number: 443
      name: https
      protocol: HTTPS
    hosts:
    - "bookinfo.com"
    tls:
      mode: SIMPLE
      serverCertificate: /etc/istio/ingressgateway-certs/tls.crt
      privateKey: /etc/istio/ingressgateway-certs/tls.key
```

kubectl  apply -f gateway-https-02.yaml  -n istio

2virtaulservice/vs-bookinfo-multi-gw.yaml

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
  - bookinfo-gateway-02
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

kubectl apply -f vs-bookinfo-multi-gw.yaml -n istio

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

https://bookinfo.com:32032/productpage

http://bookinfo.demo:31110/productpage

成功

清理：

kubectl delete gw bookinfo-gateway -n istio

kubectl delete vs bookinfo -n istio

kubectl delete gw bookinfo-gateway-02 -n istio

![1627285198(1)](images\1627285198(1).jpg)



![1627285241(1)](images\1627285241(1).jpg)

![1627285306(1)](images\1627285306(1).jpg)

![1627285355(1)](images\1627285355(1).jpg)



#### 3不同名称空间下的gateway

virtaulservice/vs-bookinfo-gw-namespace.yaml 

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: bookinfo
spec:
  hosts:
  - "*"
  gateways:
  - default/bookinfo-gateway
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

kubectl apply -f vs-bookinfo-gw-namespace.yaml  -n istio

kubectl apply -f gateway-01.yaml -n default

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

访问成功

清理：

kubectl delete gw bookinfo-gateway -n default

kubectl delete vs bookinfo -n istio

![1627357327(1)](images\1627357327(1).jpg)



![1627357386(1)](images\1627357386(1).jpg)



#### 4省略gateways默认为mesh

virtaulservice/vs-review-v2.yaml

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: reviews
spec:
  hosts:
  - reviews
  http:
  - route:
    - destination:
        host: reviews
        subset: v2
```

kubectl apply -f vs-review-v2.yaml -n istio

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

kubectl  apply -f vs-bookinfo-star.yaml -n istio

创建dr

virtaulservice/dr-review.yaml

```
apiVersion: networking.istio.io/v1beta1
kind: DestinationRule
metadata:
  name: reviews
spec:
  host: reviews
  subsets:
  - name: v1
    labels:
      version: v1
  - name: v2
    labels:
      version: v2
  - name: v3
    labels:
      version: v3
```

kubectl apply -f  dr-review.yaml -n istio

访问成功

清理：

kubectl delete dr reviews -n istio

kubectl delete vs bookinfo -n istio

kubectl delete vs reviews -n istio

kubectl delete gw bookinfo-gateway -n istio



kubectl port-forward --address 0.0.0.0 -n istio productpage-v1-6b746f74dc-5r4t4 15002:15000

![1627358104(1)](images\1627358104(1).jpg)

![1627358178(1)](images\1627358178(1).jpg)

#### 5gateways为mesh

virtaulservice/vs-review-mesh.yaml

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: reviews
spec:
  gateways:
  - mesh
  hosts:
  - reviews
  http:
  - route:
    - destination:
        host: reviews
        subset: v3
```

kubectl apply -f  vs-review-mesh.yaml -n istio

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

kubectl  apply -f vs-bookinfo-star.yaml -n istio

创建dr

virtaulservice/dr-review.yaml

```
apiVersion: networking.istio.io/v1beta1
kind: DestinationRule
metadata:
  name: reviews
spec:
  host: reviews
  subsets:
  - name: v1
    labels:
      version: v1
  - name: v2
    labels:
      version: v2
  - name: v3
    labels:
      version: v3
```

kubectl apply -f  dr-review.yaml -n istio

访问成功

清理：

kubectl delete dr reviews -n istio

kubectl delete vs bookinfo -n istio

kubectl delete vs reviews -n istio

kubectl delete gw bookinfo-gateway -n istio



kubectl port-forward --address 0.0.0.0 -n istio productpage-v1-6b746f74dc-5r4t4 15002:15000

![1627358956(1)](images\1627358956(1).jpg)



![1627359029(1)](images\1627359029(1).jpg)



### 6-4hosts

 必要字段：流量的目标主机。可以是带有通配符前缀的 DNS 名称，也可以是 IP 地址。根据所在平台情况，还可能使用短名称来代替 FQDN。这种场景下，短名称到 FQDN 的具体转换过程是要靠下层平台完成的。**一个主机名只能在一个 VirtualService 中定义。**同一个 `VirtualService` 中可以用于控制多个 HTTP 和 TCP 端口的流量属性。 Kubernetes 用户注意：当使用服务的短名称时（例如使用 `reviews`，而不是 `reviews.default.svc.cluster.local`），Istio 会根据规则所在的命名空间来处理这一名称，而非服务所在的命名空间。假设 “default” 命名空间的一条规则中包含了一个 `reviews` 的 `host`引用，就会被视为 `reviews.default.svc.cluster.local`，而不会考虑 `reviews` 服务所在的命名空间。**为了避免可能的错误配置，建议使用 FQDN 来进行服务引用。** `hosts` 字段对 HTTP 和 TCP 服务都是有效的。网格中的服务也就是在服务注册表中注册的服务，必须使用他们的注册名进行引用；只有 `Gateway` 定义的服务才可以使用 IP 地址。

#### ip

virtaulservice/vs-bookinfo-hosts-ip.yaml

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: bookinfo
spec:
  hosts:
  - "192.168.198.155"
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

kubectl apply -f vs-bookinfo-hosts-ip.yaml -n istio

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

访问http://192.168.198.155:31110/productpage 成功

清理：

kubectl delete gw bookinfo-gateway -n istio

kubectl delete vs bookinfo -n istio

![1627361821(1)](images\1627361821(1).jpg)



![1627361863(1)](images\1627361863(1).jpg)



####  多个hosts

virtaulservice/vs-bookinfo-hosts-multi.yaml

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: bookinfo
spec:
  hosts:
  - "bookinfo.com"
  - "bookinfo.demo"
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

kubectl  apply -f vs-bookinfo-hosts-multi.yaml -n istio

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

http://bookinfo.com:31110/productpage

http://bookinfo.demo:31110/productpage

成功

清理：

kubectl delete gw bookinfo-gateway -n istio

kubectl delete vs bookinfo -n istio

![1627362131(1)](images\1627362131(1).jpg)



![1627362172(1)](images\1627362172(1).jpg)



#### 匹配所有域名

virtaulservice/vs-bookinfo-hosts-star.yaml

```
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
          number: 9080
```

kubectl apply -f vs-bookinfo-hosts-star.yaml -n istio

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

访问成功

清理：

kubectl delete vs bookinfo -n istio

kubectl delete gw bookinfo-gateway -n istio



![1627363738(1)](images\1627363738(1).jpg)



![1627363833(1)](images\1627363833(1).jpg)





#### 短fqdn

virtaulservice/vs-bookinfo-hosts-fqdn-short.yaml

1在default名称空间创建vs

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: bookinfo
spec:
  hosts:
  - "bookinfo"
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

kubectl  apply -f vs-bookinfo-hosts-fqdn-short.yaml -n default



2同时要创建一个同名service

```
[root@master01 virtaulservice]# cat bookinfo-svc.yaml 
apiVersion: v1
kind: Service
metadata:
  name: bookinfo
  labels:
    app: productpage
    service: productpage
spec:
  ports:
  - port: 9080
    name: http
  selector:
    app: productpage
```

kubectl apply -f bookinfo-svc.yaml  -n default



3kubectl exec -it ratings-v1-b6994bb9-9q4gm -n istio /bin/bash

4curl bookinfo.default.svc.cluster.local/productpage



清理：

kubectl delete svc bookinfo -n default

kubectl delete vs bookinfo -n default



 kubectl port-forward --address 0.0.0.0 -n istio rating-v1-7bf8c9648f-h4dg2 15001:15000

![1627364247(1)](images\1627364247(1).jpg)



#### 长fqdn

virtaulservice/vs-bookinfo-hosts-fqdn-long.yaml

1在default名称空间创建vs

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: bookinfo
spec:
  hosts:
  - "bookinfo.default.svc.cluster.local"
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

kubectl apply -f vs-bookinfo-hosts-fqdn-long.yaml -n default

2同时在default名称空间创建bookinfo svc

virtaulservice/bookinfo-svc.yaml 

```
apiVersion: v1
kind: Service
metadata:
  name: bookinfo
  labels:
    app: productpage
    service: productpage
spec:
  ports:
  - port: 9080
    name: http
  selector:
    app: productpage
```

kubectl apply -f bookinfo-svc.yaml  -n default

3kubectl exec -it ratings-v1-b6994bb9-9q4gm -n istio /bin/bash

4curl bookinfo.default.svc.cluster.local/productpage

访问成功

kubectl port-forward --address 0.0.0.0 -n istio rating-v1-7bf8c9648f-h4dg2 15001:15000



清理：

kubectl delete svc bookinfo -n default

kubectl delete vs bookinfo -n default



![1627364926(1)](images\1627364926(1).jpg)







### http

 HTTP 流量规则的有序列表。这个列表对名称前缀为 `http-`、`http2-`、`grpc-` 的服务端口，或者协议为 `HTTP`、`HTTP2`、`GRPC` 以及终结的 TLS，另外还有使用 `HTTP`、`HTTP2` 以及 `GRPC` 协议的 `ServiceEntry` 都是有效的。进入流量会使用匹配到的第一条规则。 

#### 6-5corsPolicy

cors介绍 https://blog.csdn.net/java_green_hand0909/article/details/78740765 

配置httpd服务

```
[root@master01 html]# cat index.html 
<html>
<head><title></title></head>
<body>
<script type="text/javascript" src="https://code.jquery.com/jquery-3.2.1.min.js"></script>  
<script>
$(function(){
        $("#cors").click(
                function(){
                        $.ajax({
                                type:"get",
                                dataType : "html",
                                url:"http://bookinfo.demo:30468/productpage",
                                success:function(data){
                                        alert(data);
                                }
                        })
                });

        $("#cors2").click(
                function(){
                        $.ajax({
                                type:"get",
                                dataType : "json",
                                url:"http://bookinfo.demo:30468/reviews/1",
                                contentType : 'application/json;charset=UTF-8',
                                success:function(data){
                                        var jsonStr = JSON.stringify(data);
                                        alert(jsonStr);
                                }
                        })
                });
          $("#cors3").click(
                function(){
                        $.ajax({
                                type:"delete",
                                contentType : 'application/json;charset=UTF-8',
                                dataType : "json",
                                url:"http://bookinfo.demo:30468/reviews/1",
                                success:function(data){
                                        var jsonStr = JSON.stringify(data);
                                        alert(jsonStr);
                                }
                        })
                });
           $("#cors4").click(
                function(){
                        $.ajax({
                                type:"get",
                                contentType : 'application/json;charset=UTF-8',
                                dataType : "json",
                                headers:{"X-Custom-Header":"value"},
                                url:"http://bookinfo.demo:30468/reviews/1",
                                success:function(data){
                                        var jsonStr = JSON.stringify(data);
                                        alert(jsonStr);
                                }
                        })
                });
         
});

</script>
<input type="button" id="cors" value="简单请求"/>
<input type="button" id="cors2" value="非简单请求"/>
<input type="button" id="cors3" value="非简单请求delete"/>
<input type="button" id="cors4" value="非简单请求headers"/>
</body>
</html>
```

注意替换端口 url:"http://bookinfo.demo:27941/productpage",

启动nginx

systemctl start httpd

##### 简单请求，配置cors

virtaulservice/corsPolicy/vs-productpage-cors.yaml

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: bookinfo
spec:
  exportTo:
  - '*'
  gateways:
  - bookinfo-gateway
  hosts:
  - '*'
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
    corsPolicy:
      allowOrigins:
      - exact: "http://mytest.com:8081"
    route:
    - destination:
        host: productpage
        port:
          number: 9080
```

kubectl apply -f vs-productpage-cors.yaml -n istio

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

访问：

 http://mytest.com:8081/ 

清理：

kubectl delete vs bookinfo -n istio

kubectl delete gw bookinfo-gateway -n istio



![1627454831(1)](images\1627454831(1).jpg)

##### 简单请求allowCredentials

virtaulservice/corsPolicy/vs-productpage-cors-allowCredentials.yaml

是否发送cookie

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: bookinfo
spec:
  exportTo:
  - '*'
  gateways:
  - bookinfo-gateway
  hosts:
  - '*'
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
    corsPolicy:
      allowCredentials: true
      allowOrigins:
      - exact: "http://mytest.com:8081"
    route:
    - destination:
        host: productpage
        port:
          number: 9080
```

kubectl apply -f vs-productpage-cors-allowCredentials.yaml -n istio

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

访问成功

清理：

kubectl delete vs bookinfo -n istio

kubectl delete gw bookinfo-gateway -n istio



![1627455038(1)](images\1627455038(1).jpg)



##### 简单请求allowOrigins prefix

virtaulservice/corsPolicy/vs-productpage-cors-allowOrigins-prefix.yaml

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: bookinfo
spec:
  exportTo:
  - '*'
  gateways:
  - bookinfo-gateway
  hosts:
  - '*'
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
    corsPolicy:
      allowOrigins:
      - prefix: "http://mytest"
    route:
    - destination:
        host: productpage
        port:
          number: 9080
```

kubectl apply -f vs-productpage-cors-allowOrigins-prefix.yaml -n istio

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

访问成功

清理：

kubectl delete vs bookinfo -n istio

kubectl delete gw bookinfo-gateway -n istio



![1627455153(1)](images\1627455153(1).jpg)

##### 简单请求allowOrigins regex

virtaulservice/corsPolicy/vs-productpage-cors-allowOrigins-regex.yaml

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: bookinfo
spec:
  exportTo:
  - '*'
  gateways:
  - bookinfo-gateway
  hosts:
  - '*'
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
    corsPolicy:
      allowOrigins:
      - regex: ".*"
    route:
    - destination:
        host: productpage
        port:
          number: 9080
```

kubectl apply -f vs-productpage-cors-allowOrigins-regex.yaml -n istio

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

访问成功

清理：

kubectl delete vs bookinfo -n istio

kubectl delete gw bookinfo-gateway -n istio



![1627455304(1)](images\1627455304(1).jpg)

##### 简单请求exposeHeaders

virtaulservice/corsPolicy/vs-productpage-cors-exposeHeaders.yaml

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: bookinfo
spec:
  exportTo:
  - '*'
  gateways:
  - bookinfo-gateway
  hosts:
  - '*'
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
    corsPolicy:
      allowOrigins:
      - exact: "http://mytest.com:8081"
      exposeHeaders: 
      - test
      - test2
    route:
    - destination:
        host: productpage
        port:
          number: 9080
```

kubectl apply -f vs-productpage-cors-exposeHeaders.yaml -n istio

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

清理：

kubectl delete vs bookinfo -n istio

kubectl delete gw bookinfo-gateway -n istio

![1627455432(1)](images\1627455432(1).jpg)



##### 非简单请求

virtaulservice/corsPolicy/vs-reviews-cors.yaml 

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: bookreviews
spec:
  exportTo:
  - '*'
  gateways:
  - bookinfo-gateway
  hosts:
  - '*'
  http:
  - match:
    - uri:
        prefix: /reviews
    corsPolicy:
      allowOrigins:
      - exact: "http://mytest.com:8081"
      allowMethods:
      - GET
      - OPTIONS
      maxAge: "1m"
      allowHeaders:
      - content-type
    route:
    - destination:
        host: reviews
        port:
          number: 9080
```

kubectl  apply -f vs-reviews-cors.yaml  -n istio

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

清理：

kubectl delete vs bookreviews -n istio

kubectl delete gw bookinfo-gateway -n istio

![1627455630(1)](images\1627455630(1).jpg)

##### 非简单请求allowMethods

virtaulservice/corsPolicy/vs-reviews-cors-allowMethods.yaml

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: bookreviews
spec:
  exportTo:
  - '*'
  gateways:
  - bookinfo-gateway
  hosts:
  - '*'
  http:
  - match:
    - uri:
        prefix: /reviews
    corsPolicy:
      allowOrigins:
      - exact: "http://mytest.com:8081"
      allowMethods:
      - POST
      - OPTIONS
      maxAge: "1m"
      allowHeaders:
      - content-type
    route:
    - destination:
        host: reviews
        port:
          number: 9080
```

kubectl apply -f vs-reviews-cors-allowMethods.yaml -n istio

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

清理：

kubectl delete vs bookreviews -n istio

kubectl delete gw bookinfo-gateway -n istio



![1627455869(1)](images\1627455869(1).jpg)



##### 非简单请求allowHeaders

virtaulservice/corsPolicy/vs-reviews-cors-allowHeaders.yaml

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: bookreviews
spec:
  exportTo:
  - '*'
  gateways:
  - bookinfo-gateway
  hosts:
  - '*'
  http:
  - match:
    - uri:
        prefix: /reviews
    corsPolicy:
      allowOrigins:
      - exact: "http://mytest.com:8081"
      allowMethods:
      - GET
      - OPTIONS
      maxAge: "1m"
      allowHeaders:
      - X-Custom-Header
      - content-type
    route:
    - destination:
        host: reviews
        port:
          number: 9080
```

kubectl apply -f vs-reviews-cors-allowHeaders.yaml -n istio

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

清理：

kubectl delete vs bookreviews -n istio

kubectl delete gw bookinfo-gateway -n istio



![1627455991(1)](images\1627455991(1).jpg)



##### 非简单请求maxAge

virtaulservice/corsPolicy/vs-reviews-cors-maxAge.yaml

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: bookreviews
spec:
  exportTo:
  - '*'
  gateways:
  - bookinfo-gateway
  hosts:
  - '*'
  http:
  - match:
    - uri:
        prefix: /reviews
    corsPolicy:
      allowOrigins:
      - exact: "http://mytest.com:8081"
      allowMethods:
      - GET
      - OPTIONS
      allowHeaders:
      - content-type
      maxAge: "10s"
      #maxAge: "1m"
      #maxAge: "1h"
    route:
    - destination:
        host: reviews
        port:
          number: 9080
```

 kubectl apply -f vs-reviews-cors-maxAge.yaml -n istio

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

该字段可选，用来指定本次预检请求的有效期，单位为秒。上面结果中，有效期是20天（1728000秒），即允许缓存该条回应1728000秒（即20天），在此期间，不用发出另一条预检请求。

超时发出option请求

清理：

kubectl delete vs bookreviews -n istio

kubectl delete gw bookinfo-gateway -n istio

![1627456104(1)](images\1627456104(1).jpg)



#### 6-6delegate

向istiod容器设置环境变量

最新版本已默认支持

PILOT_ENABLE_VIRTUAL_SERVICE_DELEGATE=true

kubectl set env deploy istiod -n istio-system --list

kubectl set env deploy istiod -n istio-system PILOT_ENABLE_VIRTUAL_SERVICE_DELEGATE=true

配置文件

virtaulservice/delegate/vs-delegate.yaml

kubectl apply -f vs-delegate.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: bookinfo
spec:
  gateways:
  - bookinfo-gateway
  hosts:
  - '*'
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
    delegate:
      name: productpage
      namespace: istio
  - match:
    - uri:
        prefix: /reviews
    delegate:
      name: reviews
      namespace: istio
```

vs productpage

virtaulservice/delegate/vs-productpage.yaml

kubectl apply -f vs-productpage.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: productpage
spec:
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

virtualservice/delegate/vs-review-v2.yaml 

kubectl apply -f vs-review-v2.yaml  -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: reviews
spec:
  http:
  - route:
    - destination:
        host: reviews
        subset: v2
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

virtualservice/delegate/dr-review.yaml

kubectl apply -f dr-review.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: DestinationRule
metadata:
  name: reviews
spec:
  host: reviews
  subsets:
  - name: v1
    labels:
      version: v1
  - name: v2
    labels:
      version: v2
  - name: v3
    labels:
      version: v3
```

访问url

http://bookinfo.com:30986/productpage

http://bookinfo.com:30986/reviews/1

清理：

kubectl delete gw bookinfo-gateway -n istio

kubectl delete vs reviews -n istio

kubectl delete vs productpage -n istio

kubectl delete vs bookinfo -n istio

kubectl delete dr reviews -n istio



![1627542226(1)](images\1627542226(1).jpg)

#### 6-7fault

##### abort

1创建dr

virtaulservice/fault/dr-productpage.yaml

kubectl apply -f dr-productpage.yaml -n istio

```
apiVersion: networking.istio.io/v1alpha3
kind: DestinationRule
metadata:
  name: productpage
spec:
  host: productpage
  subsets:
  - name: v1
    labels:
      version: v1
```

2创建vs

virtaulservice/fault/vs-productpage-fault-abort.yaml 

kubectl apply -f vs-productpage-fault-abort.yaml  -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: bookinfo
  namespace: istio
spec:
  gateways:
  - bookinfo-gateway
  hosts:
  - '*'
  http:
  - fault:
      abort:
        httpStatus: 500
        percentage:
          value: 100
    match:
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
        host: productpage
        subset: v1
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

3访问

http://bookinfo.com:30986/productpage

清理：

kubectl delete gw bookinfo-gateway -n istio

kubectl delete vs bookinfo -n istio

kubectl delete dr productpage -n istio



![1627542587(1)](images\1627542587(1).jpg)



http2 abort

virtaulservice/fault/vs-productpage-fault-abort-http2Error.yaml

kubectl apply -f vs-productpage-fault-abort-http2Error.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: bookinfo
  namespace: istio
spec:
  gateways:
  - bookinfo-gateway
  hosts:
  - '*'
  http:
  - fault:
      abort:
        #grpcStatus: "test"
        http2Error: "test"
        #httpStatus: 500
        percentage:
          value: 100
    match:
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
        host: productpage
        subset: v1
```

* HTTP/2 abort fault injection not supported yet

  不支持

##### delay

1创建dr

virtaulservice/fault/dr-productpage.yaml

kubectl apply -f dr-productpage.yaml -n istio

```
apiVersion: networking.istio.io/v1alpha3
kind: DestinationRule
metadata:
  name: productpage
spec:
  host: productpage
  subsets:
  - name: v1
    labels:
      version: v1
```



virtaulservice/fault/vs-productpage-fault-delay.yaml

kubectl apply -f vs-productpage-fault-delay.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: bookinfo
  namespace: istio
spec:
  gateways:
  - bookinfo-gateway
  hosts:
  - '*'
  http:
  - fault:
      delay:
        percentage:
          value: 100.0
        fixedDelay: 7s
    match:
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
        host: productpage
        subset: v1
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

kubectl  delete gw bookinfo-gateway -n istio

kubectl delete vs bookinfo -n istio

kubectl delete dr productpage -n istio



![1627542875(1)](images\1627542875(1).jpg)



exponentialDelay

virtaulservice/fault/vs-productpage-fault-delay-exponentialDelay.yaml

kubectl apply -f vs-productpage-fault-delay-exponentialDelay.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: bookinfo
  namespace: istio
spec:
  gateways:
  - bookinfo-gateway
  hosts:
  - '*'
  http:
  - fault:
      delay:
        percentage:
          value: 100.0
        exponentialDelay: 7s
    match:
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
        host: productpage
        subset: v1
```

exponentialDelay not supported yet

不支持

#### 6-8headers

##### request

###### add

virtaulservice/headers/vs-headers-request-add.yaml

kubectl apply -f vs-headers-request-add.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: bookinfo
spec:
  exportTo:
  - '*'
  gateways:
  - bookinfo-gateway
  hosts:
  - '*'
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
    headers:
      request:
        add:
          TEST_REQUEST_HEADER: XX
    route:
    - destination:
        host: productpage
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

kubectl  delete gw bookinfo-gateway -n istio

kubectl delete vs bookinfo -n istio



![1627543045(1)](images\1627543045(1).jpg)



###### remove

virtaulservice/headers/vs-headers-request-remove.yaml

kubectl  apply -f vs-headers-request-remove.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: bookinfo
spec:
  exportTo:
  - '*'
  gateways:
  - bookinfo-gateway
  hosts:
  - '*'
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
    headers:
      request:
        remove:
        - TEST_REQUEST_HEADER
    route:
    - destination:
        host: productpage
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

kubectl  delete gw bookinfo-gateway -n istio

kubectl delete vs bookinfo -n istio



![1627543173(1)](images\1627543173(1).jpg)

###### set

virtaulservice/headers/vs-headers-request-set.yaml

kubectl  apply -f vs-headers-request-set.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: bookinfo
spec:
  exportTo:
  - '*'
  gateways:
  - bookinfo-gateway
  hosts:
  - '*'
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
    headers:
      request:
        set:
          TEST_REQUEST_HEADER: XX
    route:
    - destination:
        host: productpage
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

kubectl  delete gw bookinfo-gateway -n istio

kubectl delete vs bookinfo -n istio



![1627543282(1)](images\1627543282(1).jpg)



##### response

###### add

virtaulservice/headers/vs-headers-response-add.yaml

kubectl apply -f vs-headers-response-add.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: bookinfo
spec:
  exportTo:
  - '*'
  gateways:
  - bookinfo-gateway
  hosts:
  - '*'
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
    headers:
      response:
        add:
          TEST_REQUEST_HEADER: XX
    route:
    - destination:
        host: productpage
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

kubectl  delete gw bookinfo-gateway -n istio

kubectl delete vs bookinfo -n istio



![1627543425(1)](images\1627543425(1).jpg)



###### remove

virtaulservice/headers/vs-headers-response-remove.yaml

kubectl apply -f vs-headers-response-remove.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: bookinfo
spec:
  exportTo:
  - '*'
  gateways:
  - bookinfo-gateway
  hosts:
  - '*'
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
    headers:
      response:
        remove:
        - x-envoy-upstream-service-time
    route:
    - destination:
        host: productpage
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

kubectl  delete gw bookinfo-gateway -n istio

kubectl delete vs bookinfo -n istio



![1627543546(1)](images\1627543546(1).jpg)



###### set

virtaulservice/headers/vs-headers-response-set.yaml

没有就添加，有就修改

kubectl apply -f vs-headers-response-set.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: bookinfo
spec:
  exportTo:
  - '*'
  gateways:
  - bookinfo-gateway
  hosts:
  - '*'
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
    headers:
      response:
        set:
          content-type: "text/html"
          Test: "test"
          x-envoy-upstream-service-time: "1111111111"
    route:
    - destination:
        host: productpage
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

kubectl  delete gw bookinfo-gateway -n istio

kubectl delete vs bookinfo -n istio



![1627543677(1)](images\1627543677(1).jpg)



#### 6-9match

##### authority

###### exact

virtaulservice/match/vs-match-authority-exact.yaml

kubectl apply -f vs-match-authority-exact.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: bookinfo
spec:
  gateways:
  - bookinfo-gateway
  hosts:
  - '*'
  http:
  - match:
    - authority:
        exact: "bookinfo.demo:27941"
    route:
    - destination:
        host: productpage
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

kubectl  delete gw bookinfo-gateway -n istio

kubectl delete vs bookinfo -n istio



![1627618264](images\1627618264.jpg)



###### prefix

virtaulservice/match/vs-match-authority-prefix.yaml

kubectl apply -f vs-match-authority-prefix.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: bookinfo
spec:
  gateways:
  - bookinfo-gateway
  hosts:
  - '*'
  http:
  - match:
    - authority:
        prefix: "bookinfo"
    route:
    - destination:
        host: productpage
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

kubectl  delete gw bookinfo-gateway -n istio

kubectl delete vs bookinfo -n istio



![1627618442(1)](images\1627618442(1).jpg)



###### regex

virtaulservice/match/vs-match-authority-regex.yaml 

kubectl apply -f vs-match-authority-regex.yaml  -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: bookinfo
spec:
  gateways:
  - bookinfo-gateway
  hosts:
  - '*'
  http:
  - match:
    - authority:
        regex: "bookinfo.de.*"
    route:
    - destination:
        host: productpage
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

kubectl  delete gw bookinfo-gateway -n istio

kubectl delete vs bookinfo -n istio



![1627618575(1)](images\1627618575(1).jpg)





##### gateways

virtaulservice/match/vs-match-gateways.yaml

kubectl apply -f vs-match-gateways.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: bookinfo
spec:
  gateways:
  - bookinfo-gateway
  - bookinfo-gateway-02
  hosts:
  - '*'
  http:
  - match:
    - uri:
        exact: /productpage
      gateways:
      - bookinfo-gateway-02
    - uri:
        prefix: /static
    route:
    - destination:
        host: productpage
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

virtualservice/gateway-https-02.yaml 

kubectl apply -f gateway-https-02.yaml  -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: Gateway
metadata:
  name: bookinfo-gateway-02
spec:
  selector:
    istio: ingressgateway
  servers:
  - port:
      number: 443
      name: https
      protocol: HTTPS
    hosts:
    - "bookinfo.com"
    tls:
      mode: SIMPLE
      serverCertificate: /etc/istio/ingressgateway-certs/tls.crt
      privateKey: /etc/istio/ingressgateway-certs/tls.key
```

访问

清理：

kubectl  delete gw bookinfo-gateway -n istio

kubectl delete gw bookinfo-gateway-02 -n istio

kubectl delete vs bookinfo -n istio



![1627618977(1)](images\1627618977(1).jpg)



![1627619069(1)](images\1627619069(1).jpg)

##### headers

###### exact

virtaulservice/match/vs-match-headers-exact.yaml

kubectl apply -f vs-match-headers-exact.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: reviews
spec:
  hosts:
  - reviews
  http:
  - match:
    - headers:
        end-user:
          exact: mark
    route:
    - destination:
        host: reviews
        subset: v2
  - route:
    - destination:
        host: reviews
        subset: v3
```

virtualservice/vs-bookinfo-hosts-star.yaml 

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

virtualservice/dr-review.yaml

kubectl apply -f dr-review.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: DestinationRule
metadata:
  name: reviews
spec:
  host: reviews
  subsets:
  - name: v1
    labels:
      version: v1
  - name: v2
    labels:
      version: v2
  - name: v3
    labels:
      version: v3
```

访问

清理：

kubectl  delete gw bookinfo-gateway -n istio

kubectl delete vs reviews -n istio

kubectl delete dr reviews -n istio

kubectl delete vs bookinfo -n istio



![1627619532(1)](images\1627619532(1).jpg)

kubectl port-forward --address 0.0.0.0 -n istio reviews-v1-545db77b95-zqr6s 15001:15000



![1627619734](images\1627619734.jpg)



###### prefix

virtaulservice/match/vs-match-headers-prefix.yaml

kubectl apply -f vs-match-headers-prefix.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: reviews
spec:
  hosts:
  - reviews
  http:
  - match:
    - headers:
        end-user:
          prefix: ma
    route:
    - destination:
        host: reviews
        subset: v2
  - route:
    - destination:
        host: reviews
        subset: v3
```

virtualservice/vs-bookinfo-hosts-star.yaml 

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

virtualservice/dr-review.yaml

kubectl apply -f dr-review.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: DestinationRule
metadata:
  name: reviews
spec:
  host: reviews
  subsets:
  - name: v1
    labels:
      version: v1
  - name: v2
    labels:
      version: v2
  - name: v3
    labels:
      version: v3
```

访问

清理：

kubectl  delete gw bookinfo-gateway -n istio

kubectl delete vs reviews -n istio

kubectl delete dr reviews -n istio

kubectl delete vs bookinfo -n istio



kubectl port-forward --address 0.0.0.0 -n istio reviews-v1-545db77b95-zqr6s 15001:15000



![1627620092(1)](images\1627620092(1).jpg)

###### regex

virtaulservice/match/vs-match-headers-regex.yaml

kubectl apply -f vs-match-headers-regex.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: reviews
spec:
  hosts:
  - reviews
  http:
  - match:
    - headers:
        end-user:
          regex: "m.*k"
    route:
    - destination:
        host: reviews
        subset: v2
  - route:
    - destination:
        host: reviews
        subset: v3
```

virtualservice/vs-bookinfo-hosts-star.yaml 

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

virtualservice/dr-review.yaml

kubectl apply -f dr-review.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: DestinationRule
metadata:
  name: reviews
spec:
  host: reviews
  subsets:
  - name: v1
    labels:
      version: v1
  - name: v2
    labels:
      version: v2
  - name: v3
    labels:
      version: v3
```

访问

清理：

kubectl  delete gw bookinfo-gateway -n istio

kubectl delete vs reviews -n istio

kubectl delete dr reviews -n istio

kubectl delete vs bookinfo -n istio



kubectl port-forward --address 0.0.0.0 -n istio reviews-v1-545db77b95-zqr6s 15001:15000

![1627620534(1)](images\1627620534(1).jpg)





###### cookie

vs-match-header-cookie-bookinfo.yaml

kubectl  apply -f vs-match-header-cookie-bookinfo.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: bookinfo
spec:
  gateways:
  - bookinfo-gateway
  hosts:
  - '*'
  http:
  - match:
    - headers:
        cookie:
          regex: "^(.*?;)?(session=.*)(;.*)?$"
    route:
    - destination:
        host: productpage
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

kubectl  delete gw bookinfo-gateway -n istio

kubectl delete vs bookinfo -n istio



![1627620763(1)](images\1627620763(1).jpg)



###### user-agent

header user-agent例子

safari 5.1 – MAC
User-Agent:Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_6_8; en-us) AppleWebKit/534.50 (KHTML, like Gecko) Version/5.1 Safari/534.50

safari 5.1 – Windows
User-Agent:Mozilla/5.0 (Windows; U; Windows NT 6.1; en-us) AppleWebKit/534.50 (KHTML, like Gecko) Version/5.1 Safari/534.50

IE 9.0
User-Agent:Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; Trident/5.0;

IE 8.0
User-Agent:Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.0; Trident/4.0)

IE 7.0
User-Agent:Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.0)

IE 6.0
User-Agent: Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1)

Firefox 4.0.1 – MAC
User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10.6; rv:2.0.1) Gecko/20100101 Firefox/4.0.1

Firefox 4.0.1 – Windows
User-Agent:Mozilla/5.0 (Windows NT 6.1; rv:2.0.1) Gecko/20100101 Firefox/4.0.1

Opera 11.11 – MAC
User-Agent:Opera/9.80 (Macintosh; Intel Mac OS X 10.6.8; U; en) Presto/2.8.131 Version/11.11

Opera 11.11 – Windows
User-Agent:Opera/9.80 (Windows NT 6.1; U; en) Presto/2.8.131 Version/11.11

Chrome 17.0 – MAC
User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_0) AppleWebKit/535.11 (KHTML, like Gecko) Chrome/17.0.963.56 Safari/535.11

傲游（Maxthon）
User-Agent: Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; Maxthon 2.0)

腾讯TT
User-Agent: Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; TencentTraveler 4.0)

世界之窗（The World） 2.x
User-Agent: Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1)

世界之窗（The World） 3.x
User-Agent: Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; The World)

搜狗浏览器 1.x
User-Agent: Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; Trident/4.0; SE 2.X MetaSr 1.0; SE 2.X MetaSr 1.0; .NET CLR 2.0.50727; SE 2.X MetaSr 1.0)

360浏览器
User-Agent: Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; 360SE)

Avant
User-Agent: Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; Avant Browser)

Green Browser
User-Agent: Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1)



virtaulservice/match/vs-match-header-user-agent.yaml 

kubectl apply -f vs-match-header-user-agent.yaml  -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: bookinfo
spec:
  gateways:
  - bookinfo-gateway
  hosts:
  - '*'
  http:
  - match:
    - headers:
        user-agent:
          regex: ".*Chrome.*"
    route:
    - destination:
        host: productpage
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

kubectl  delete gw bookinfo-gateway -n istio

kubectl delete vs bookinfo -n istio



![1627620890(1)](images\1627620890(1).jpg)



##### ignoreUriCase

virtaulservice/match/vs-match-ignoreUriCase.yaml 

kubectl apply -f vs-match-ignoreUriCase.yaml  -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: bookinfo
spec:
  gateways:
  - bookinfo-gateway
  hosts:
  - '*'
  http:
  - match:
    - uri:
        exact: "/PRODUCTPAGE"
      ignoreUriCase: true
    route:
    - destination:
        host: productpage
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

kubectl  delete gw bookinfo-gateway -n istio

kubectl delete vs bookinfo -n istio





![1627621153(1)](images\1627621153(1).jpg)



##### method

###### exact

virtaulservice/match/vs-match-method-exact.yaml

kubectl apply -f vs-match-method-exact.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: bookinfo
spec:
  gateways:
  - bookinfo-gateway
  hosts:
  - '*'
  http:
  - match:
    - method:
        exact: "GET"
    route:
    - destination:
        host: productpage
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

kubectl  delete gw bookinfo-gateway -n istio

kubectl delete vs bookinfo -n istio



![1627622852(1)](images\1627622852(1).jpg)



###### prefix

virtaulservice/match/vs-match-method-prefix.yaml

kubectl apply -f vs-match-method-prefix.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: bookinfo
spec:
  gateways:
  - bookinfo-gateway
  hosts:
  - '*'
  http:
  - match:
    - method:
        prefix: "G"
    route:
    - destination:
        host: productpage
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

kubectl  delete gw bookinfo-gateway -n istio

kubectl delete vs bookinfo -n istio



![1627622962(1)](images\1627622962(1).jpg)



###### regex

virtaulservice/match/vs-match-method-regex.yaml

kubectl apply -f vs-match-method-regex.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: bookinfo
spec:
  gateways:
  - bookinfo-gateway
  hosts:
  - '*'
  http:
  - match:
    - method:
        regex: "G.*T"
    route:
    - destination:
        host: productpage
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

kubectl  delete gw bookinfo-gateway -n istio

kubectl delete vs bookinfo -n istio



![1627623068(1)](images\1627623068(1).jpg)



##### name

virtaulservice/match/vs-match-name.yaml

kubectl apply -f vs-match-name.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: bookinfo
spec:
  gateways:
  - bookinfo-gateway
  hosts:
  - '*'
  http:
  - match:
    - uri:
        exact: /productpage
      name: book
    route:
    - destination:
        host: productpage
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

kubectl  delete gw bookinfo-gateway -n istio

kubectl delete vs bookinfo -n istio



![1627623194(1)](images\1627623194(1).jpg)



##### port

virtaulservice/match/vs-match-port.yaml

kubectl apply -f vs-match-port.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: bookinfo
spec:
  gateways:
  - bookinfo-gateway
  hosts:
  - '*'
  http:
  - match:
    - port: 80
    route:
    - destination:
        host: productpage
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

kubectl  delete gw bookinfo-gateway -n istio

kubectl delete vs bookinfo -n istio



![1627623498(1)](images\1627623498(1).jpg)

##### queryParams

###### exact

virtaulservice/match/vs-match-queryParams-exact.yaml

kubectl apply -f vs-match-queryParams-exact.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: bookinfo
spec:
  gateways:
  - bookinfo-gateway
  hosts:
  - '*'
  http:
  - match:
    - queryParams:
        test:
          exact: test
    route:
    - destination:
        host: productpage
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

kubectl  delete gw bookinfo-gateway -n istio

kubectl delete vs bookinfo -n istio



![1627623638(1)](images\1627623638(1).jpg)



###### prefix

virtaulservice/match/vs-match-queryParams-prefix.yaml

kubectl apply -f vs-match-queryParams-prefix.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: bookinfo
spec:
  gateways:
  - bookinfo-gateway
  hosts:
  - '*'
  http:
  - match:
    - queryParams:
        test:
          prefix: test
    route:
    - destination:
        host: productpage
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

不起作用，value值可任意

访问

清理：

kubectl  delete gw bookinfo-gateway -n istio

kubectl delete vs bookinfo -n istio



![1627623771(1)](images\1627623771(1).jpg)



###### regex

virtaulservice/match/vs-match-queryParams-regex.yaml 

kubectl apply -f vs-match-queryParams-regex.yaml  -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: bookinfo
spec:
  gateways:
  - bookinfo-gateway
  hosts:
  - '*'
  http:
  - match:
    - queryParams:
        test:
          regex: "\\d+$"
    route:
    - destination:
        host: productpage
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

test值必须是数字

访问

清理：

kubectl  delete gw bookinfo-gateway -n istio

kubectl delete vs bookinfo -n istio



![1627623883(1)](images\1627623883(1).jpg)







##### scheme

###### exact

vs-match-scheme-exact.yaml  

kubectl apply -f vs-match-scheme-exact.yaml   -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: bookinfo
spec:
  gateways:
  - bookinfo-gateway
  hosts:
  - '*'
  http:
  - match:
    - scheme:
        exact: "https"
    route:
    - destination:
        host: productpage
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

kubectl  delete gw bookinfo-gateway -n istio

kubectl delete vs bookinfo -n istio



![1627624057(1)](images\1627624057(1).jpg)





###### prefix

vs-match-scheme-prefix.yaml

kubectl apply -f vs-match-scheme-prefix.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: bookinfo
spec:
  gateways:
  - bookinfo-gateway
  hosts:
  - '*'
  http:
  - match:
    - scheme:
        prefix: "http"
    route:
    - destination:
        host: productpage
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

kubectl  delete gw bookinfo-gateway -n istio

kubectl delete vs bookinfo -n istio



![1627624161(1)](images\1627624161(1).jpg)





###### regex

vs-match-scheme-regex.yaml

kubectl apply -f vs-match-scheme-regex.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: bookinfo
spec:
  gateways:
  - bookinfo-gateway
  hosts:
  - '*'
  http:
  - match:
    - scheme:
        regex: ".*"
    route:
    - destination:
        host: productpage
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

kubectl  delete gw bookinfo-gateway -n istio

kubectl delete vs bookinfo -n istio



![1627624264(1)](images\1627624264(1).jpg)





##### sourceLabels

virtaulservice/match/vs-match-sourceLabels.yaml

kubectl apply -f vs-match-sourceLabels.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: reviews
spec:
  hosts:
  - reviews
  http:
  - match:
    - sourceLabels:
        app: productpage
        version: v1
    route:
    - destination:
        host: reviews
        subset: v2
```

virtualservice/vs-bookinfo-hosts-star.yaml 

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

virtualservice/dr-review.yaml 

kubectl apply -f dr-review.yaml  -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: DestinationRule
metadata:
  name: reviews
spec:
  host: reviews
  subsets:
  - name: v1
    labels:
      version: v1
  - name: v2
    labels:
      version: v2
  - name: v3
    labels:
      version: v3
```

访问

清理：

kubectl  delete gw bookinfo-gateway -n istio

kubectl delete vs bookinfo -n istio

kubectl delete vs reviews -n istio

kubectl delete dr reviews -n istio

找不到配置





##### sourceNamespace

virtaulservice/match/vs-match-sourceNamespace.yaml

kubectl  apply -f vs-match-sourceNamespace.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: bookinfo
spec:
  gateways:
  - bookinfo-gateway
  hosts:
  - '*'
  http:
  - match:
    - sourceNamespace: istio-system
    route:
    - destination:
        host: productpage
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

kubectl  delete gw bookinfo-gateway -n istio

kubectl delete vs bookinfo -n istio

![1627626095(1)](images\1627626095(1).jpg)





##### uri

###### exact

virtaulservice/match/vs-match-uri-exact.yaml

kubectl apply -f vs-match-uri-exact.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: bookinfo
spec:
  gateways:
  - bookinfo-gateway
  hosts:
  - '*'
  http:
  - match:
    - uri:
        exact: /productpage
    route:
    - destination:
        host: productpage
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

kubectl  delete gw bookinfo-gateway -n istio

kubectl delete vs bookinfo -n istio



![1627626253(1)](images\1627626253(1).jpg)



###### prefix

virtaulservice/match/vs-match-uri-prefix.yaml

kubectl apply -f vs-match-uri-prefix.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: bookinfo
spec:
  gateways:
  - bookinfo-gateway
  hosts:
  - '*'
  http:
  - match:
    - uri:
        prefix: /product
    route:
    - destination:
        host: productpage
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

kubectl  delete gw bookinfo-gateway -n istio

kubectl delete vs bookinfo -n istio



![1627626355(1)](images\1627626355(1).jpg)





###### regex

virtaulservice/match/vs-match-uri-regex.yaml

kubectl  apply -f vs-match-uri-regex.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: bookinfo
spec:
  gateways:
  - bookinfo-gateway
  hosts:
  - '*'
  http:
  - match:
    - uri:
        regex: "/p.*e"
    route:
    - destination:
        host: productpage
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

kubectl  delete gw bookinfo-gateway -n istio

kubectl delete vs bookinfo -n istio



![1627626450(1)](images\1627626450(1).jpg)





##### withoutHeaders

###### exact

vs-match-withoutHeaders-bookinfo-exact.yaml

kubectl apply -f vs-match-withoutHeaders-bookinfo-exact.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: bookinfo
spec:
  gateways:
  - bookinfo-gateway
  hosts:
  - '*'
  http:
  - match:
    - withoutHeaders:
        end-user:
          exact: mark
    route:
    - destination:
        host: productpage
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



curl http://bookinfo.demo:30986/productpage -H "end-user: mark" -I

curl http://bookinfo.demo:30986/productpage -H "end-user: hxp"

如果header不存在也是不匹配

访问

清理：

kubectl  delete gw bookinfo-gateway -n istio

kubectl delete vs bookinfo -n istio



![1627626588(1)](images\1627626588(1).jpg)



###### prefix

vs-match-withoutHeaders-bookinfo-prefix.yaml

kubectl apply -f vs-match-withoutHeaders-bookinfo-prefix.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: bookinfo
spec:
  gateways:
  - bookinfo-gateway
  hosts:
  - '*'
  http:
  - match:
    - withoutHeaders:
        end-user:
          prefix: ma
    route:
    - destination:
        host: productpage
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



curl http://bookinfo.demo:30986/productpage -H "end-user: mark" -I

curl http://bookinfo.demo:30986/productpage -H "end-user: hxp"

如果header不存在也是不匹配



清理：

kubectl  delete gw bookinfo-gateway -n istio

kubectl delete vs bookinfo -n istio

![1627626689(1)](images\1627626689(1).jpg)





###### regex

vs-match-withoutHeaders-bookinfo-regex.yaml

kubectl apply -f vs-match-withoutHeaders-bookinfo-regex.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: bookinfo
spec:
  gateways:
  - bookinfo-gateway
  hosts:
  - '*'
  http:
  - match:
    - withoutHeaders:
        end-user:
          regex: "m.*k"
    route:
    - destination:
        host: productpage
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

curl http://bookinfo.demo:30986/productpage -H "end-user: mark" -I

curl http://bookinfo.demo:30986/productpage -H "end-user: hxp"

如果header不存在也是不匹配

清理：

kubectl  delete gw bookinfo-gateway -n istio

kubectl delete vs bookinfo -n istio



![1627626796(1)](images\1627626796(1).jpg)





#### 6-10mirror

virtaulservice/mirror/vs-http-mirror.yaml

kubectl apply -f vs-http-mirror.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: bookinfo
spec:
  exportTo:
  - '*'
  gateways:
  - bookinfo-gateway
  hosts:
  - '*'
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
        host: productpage
        port:
          number: 9080
    mirror:
      host: productpage.istio-2.svc.cluster.local
      port: 
        number: 9080
    mirrorPercentage:
      value: 100
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

1创建namespace

kubectl create ns istio-2

2打标签

kubectl label ns istio-2 istio-injection=enabled

3部署deployment

kubectl apply -f productpage-deploy.yaml -n istio-2

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
        image: docker.io/istio/examples-bookinfo-productpage-v1:1.16.2
        imagePullPolicy: IfNotPresent
        ports:
        - containerPort: 9080
        volumeMounts:
        - name: tmp
          mountPath: /tmp
      volumes:
      - name: tmp
        emptyDir: {}
```

4打开日志

 kubectl logs -f productpage-v1-64794f5db4-ng9sn  -n istio-2 

5创建资源

kubectl  apply -f vs-http-mirror.yaml -n istio

6访问url

 http://192.168.198.154:27941/productpage

清理：

kubectl delete -f productpage-deploy.yaml -n istio-2

kubectl delete vs bookinfo -n istio

kubectl delete gw bookinfo-gateway -n istio



![1627702387(1)](images\1627702387(1).jpg)



##### subset

1创建dr

kubectl apply -f dr-productpage.yaml -n istio-2

```
apiVersion: networking.istio.io/v1beta1
kind: DestinationRule
metadata:
  name: productpage
spec:
  host: productpage
  subsets:
  - name: v1
    labels:
      version: v1
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

2创建mirror资源

kubectl apply -f vs-http-mirror-subset.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: bookinfo
spec:
  exportTo:
  - '*'
  gateways:
  - bookinfo-gateway
  hosts:
  - '*'
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
        host: productpage
        port:
          number: 9080
    mirror:
      host: productpage.istio-2.svc.cluster.local
      subset: v1
    mirrorPercentage:
      value: 100
```

kubectl apply -f productpage-deploy.yaml -n istio-2

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
        image: docker.io/istio/examples-bookinfo-productpage-v1:1.16.2
        imagePullPolicy: IfNotPresent
        ports:
        - containerPort: 9080
        volumeMounts:
        - name: tmp
          mountPath: /tmp
      volumes:
      - name: tmp
        emptyDir: {}
```

3访问

 http://192.168.198.154:27941/productpage 

4观察日志

清理：

kubectl delete -f productpage-deploy.yaml -n istio-2

kubectl delete vs bookinfo -n istio

kubectl delete gw bookinfo-gateway -n istio

kubectl  delete dr productpage -n istio-2



![1627702495(1)](images\1627702495(1).jpg)



#### 6-11name

virtaulservice/vs-bookinfo-name.yaml

kubectl apply -f vs-bookinfo-name.yaml -n istio

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
    name: bookinfo
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

kubectl delete vs bookinfo -n istio

kubectl delete gw bookinfo-gateway -n istio



![1627702715(1)](images\1627702715(1).jpg)





#### 6-12redirect

virtaulservice/redirect/vs-productpage-redirect.yaml

kubectl apply -f vs-productpage-redirect.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: bookinfo
spec:
  exportTo:
  - '*'
  gateways:
  - bookinfo-gateway
  hosts:
  - '*'
  http:
  - match:
    - uri:
        exact: /mypage
    redirect:
      uri: /productpage
      authority: 192.168.198.154:27941
      redirectCode: 308
  - match:
    - uri:
        prefix: /productpage
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
        host: productpage
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

访问：

 http://192.168.198.154:27941/mypage 

清理：

kubectl delete vs bookinfo -n istio

kubectl delete gw bookinfo-gateway -n istio



![1627702875(1)](images\1627702875(1).jpg)





#### 6-13retries

- attempts：必选字段，定义重试的次数
- perTryTimeout：每次重试超时的时间，单位可以是ms、s、m和h
- retryOn：进行重试的条件，可以是多个条件，以逗号分隔

其中重试条件retryOn的取值可以包括以下几种。

- 5xx：在上游服务返回5xx应答码，或者在没有返回时重试
- gateway-error：类似于5xx异常，只对502、503和504应答码进行重试。
- connect-failure：在链接上游服务失败时重试 retriable-4xx：在上游服务返回可重试的4xx应答码时执行重试。
- refused-stream：在上游服务使用REFUSED_STREAM错误码重置时执行重试。
- cancelled：gRPC应答的Header中状态码是cancelled时执行重试。
- deadline-exceeded：在gRPC应答的Header中状态码是deadline-exceeded时执行重试
- internal：在gRPC应答的Header中状态码是internal时执行重试
- resource-exhausted：在gRPC应答的Header中状态码是resource-exhausted时执行重试
- unavailable：在gRPC应答的Header中状态码是unavailable时执行重试。

设置延迟错误：

virtaulservice/retry/vs-reviews.yaml

kubectl apply -f vs-reviews.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: reviews
spec:
  hosts:
  - reviews
  http:
  - route:
    - destination:
        host: reviews
        subset: v3
    fault:
      delay:
        percentage:
          value: 100.0
        fixedDelay: 7s
```

dr-review.yaml

kubectl apply -f dr-review.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: DestinationRule
metadata:
  name: reviews
spec:
  host: reviews
  subsets:
  - name: v1
    labels:
      version: v1
  - name: v2
    labels:
      version: v2
  - name: v3
    labels:
      version: v3
```

'设置重试

virtaulservice/retry/vs-bookinfo.yaml

kubectl apply -f vs-bookinfo.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: bookinfo
spec:
  gateways:
  - bookinfo-gateway
  hosts:
  - '*'
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
        host: productpage
        subset: v1
        port:
          number: 9080
    retries:
      attempts: 5
      perTryTimeout: 3s
      retryOn: 5xx,connect-failure
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

dr-productpage.yaml

kubectl apply -f dr-productpage.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: DestinationRule
metadata:
  name: productpage
spec:
  host: productpage
  subsets:
  - name: v1
    labels:
      version: v1
```

查看重试日志

kubectl  logs -f productpage-v1-6b746f74dc-nb8sg -n istio

清理：

kubectl delete vs bookinfo -n istio

kubectl delete gw bookinfo-gateway -n istio

kubectl delete dr reviews -n istio

kubectl delete vs reviews -n istio

kubectl delete dr productpage -n istio





![1627703309(1)](images\1627703309(1).jpg)



是否重试其他机子

virtaulservice/retry/vs-bookinfo-retryRemoteLocalities.yaml

kubectl apply -f vs-bookinfo-retryRemoteLocalities.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: bookinfo
spec:
  gateways:
  - bookinfo-gateway
  hosts:
  - '*'
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
        host: productpage
        subset: v1
        port:
          number: 9080
    retries:
      attempts: 5
      perTryTimeout: 3s
      retryOn: 5xx,connect-failure
      retryRemoteLocalities: true
```

virtaulservice/retry/vs-reviews.yaml

kubectl apply -f vs-reviews.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: reviews
spec:
  hosts:
  - reviews
  http:
  - route:
    - destination:
        host: reviews
        subset: v3
    fault:
      delay:
        percentage:
          value: 100.0
        fixedDelay: 7s
```

dr-review.yaml

kubectl apply -f dr-review.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: DestinationRule
metadata:
  name: reviews
spec:
  host: reviews
  subsets:
  - name: v1
    labels:
      version: v1
  - name: v2
    labels:
      version: v2
  - name: v3
    labels:
      version: v3
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

dr-productpage.yaml

kubectl apply -f dr-productpage.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: DestinationRule
metadata:
  name: productpage
spec:
  host: productpage
  subsets:
  - name: v1
    labels:
      version: v1
```

查看重试日志

kubectl  logs -f productpage-v1-6b746f74dc-nb8sg -n istio

清理：

kubectl delete vs bookinfo -n istio

kubectl delete gw bookinfo-gateway -n istio

kubectl delete dr reviews -n istio

kubectl delete vs reviews -n istio

kubectl delete dr productpage -n istio



没看出配置和上一个有啥不同

![1627703786(1)](images\1627703786(1).jpg)



#### 6-14rewrite

##### uri

virtaulservice/rewrite/vs-http-rewrite.yaml

kubectl apply -f vs-http-rewrite.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: bookinfo
spec:
  gateways:
  - bookinfo-gateway
  hosts:
  - '*'
  http:
  - match:
    - uri:
        regex: "/m.*k"
    rewrite:
      uri: "/productpage"
    route:
    - destination:
        host: productpage
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

kubectl delete vs bookinfo -n istio

kubectl delete gw bookinfo-gateway -n istio



![1627704005(1)](images\1627704005(1).jpg)





##### authority

virtaulservice/rewrite/vs-http-rewrite-authority.yaml

kubectl apply -f vs-http-rewrite-authority.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: bookinfo
spec:
  gateways:
  - bookinfo-gateway
  hosts:
  - '*'
  http:
  - match:
    - uri:
        regex: "/m.*k"
    rewrite:
      uri: "/productpage"
      authority: bookinfo.com:27941
    route:
    - destination:
        host: productpage
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

kubectl delete vs bookinfo -n istio

kubectl delete gw bookinfo-gateway -n istio



![1627704157(1)](images\1627704157(1).jpg)





#### 6-15route

##### destination

###### host

virtaulservice/route/vs-reviews-host.yaml

kubectl apply -f vs-reviews-host.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: reviews
spec:
  hosts:
    - reviews
  http:
  - route:
    - destination:
        host: reviews
```

virtaulservice/vs-bookinfo-hosts-star.yaml

kubectl apply -f vs-bookinfo-hosts-star.yaml -n istio

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

kubectl delete vs bookinfo -n istio

kubectl delete gw bookinfo-gateway -n istio

kubectl delete vs reviews -n istio





![1627706461(1)](images\1627706461(1).jpg)



###### port

virtaulservice/route/vs-reviews-port.yaml

kubectl  apply -f vs-reviews-port.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: reviews
spec:
  hosts:
    - reviews
  http:
  - route:
    - destination:
        host: reviews
        port:
          number: 9080
```

virtaulservice/vs-bookinfo-hosts-star.yaml

kubectl apply -f vs-bookinfo-hosts-star.yaml -n istio

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

kubectl delete vs bookinfo -n istio

kubectl delete gw bookinfo-gateway -n istio

kubectl delete vs reviews -n istio

![1627706549(1)](images\1627706549(1).jpg)







###### subset

virtaulservice/route/vs-reviews-subset.yaml

kubectl  apply -f vs-reviews-subset.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: reviews
spec:
  hosts:
    - reviews
  http:
  - route:
    - destination:
        host: reviews
        subset: v1
```

virtaulservice/vs-bookinfo-hosts-star.yaml

kubectl apply -f vs-bookinfo-hosts-star.yaml -n istio

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

dr-review.yaml

kubectl apply -f dr-review.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: DestinationRule
metadata:
  name: reviews
spec:
  host: reviews
  subsets:
  - name: v1
    labels:
      version: v1
  - name: v2
    labels:
      version: v2
  - name: v3
    labels:
      version: v3
```

访问

清理：

kubectl delete vs bookinfo -n istio

kubectl delete gw bookinfo-gateway -n istio

kubectl delete vs reviews -n istio

kubectl  delete dr reviews -n  istio



![1627706742(1)](images\1627706742(1).jpg)



##### headers

###### request

add

virtaulservice/route/vs-reviews-headers-request-add.yaml

kubectl apply -f vs-reviews-headers-request-add.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: reviews
spec:
  hosts:
    - reviews
  http:
  - route:
    - destination:
        host: reviews
        subset: v1
      headers:
        request:
          add:
            test: test
```

virtaulservice/vs-bookinfo-hosts-star.yaml

kubectl apply -f vs-bookinfo-hosts-star.yaml -n istio

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

dr-review.yaml

kubectl apply -f dr-review.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: DestinationRule
metadata:
  name: reviews
spec:
  host: reviews
  subsets:
  - name: v1
    labels:
      version: v1
  - name: v2
    labels:
      version: v2
  - name: v3
    labels:
      version: v3
```

访问

清理：

kubectl delete vs bookinfo -n istio

kubectl delete gw bookinfo-gateway -n istio

kubectl delete vs reviews -n istio

kubectl  delete dr reviews -n  istio



![1627706909(1)](images\1627706909(1).jpg)



remove

virtaulservice/route/vs-reviews-headers-request-remove.yaml

kubectl apply -f vs-reviews-headers-request-remove.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: reviews
spec:
  hosts:
    - reviews
  http:
  - route:
    - destination:
        host: reviews
        subset: v1
      headers:
        request:
          remove:
          - test
```

virtaulservice/vs-bookinfo-hosts-star.yaml

kubectl apply -f vs-bookinfo-hosts-star.yaml -n istio

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

dr-review.yaml

kubectl apply -f dr-review.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: DestinationRule
metadata:
  name: reviews
spec:
  host: reviews
  subsets:
  - name: v1
    labels:
      version: v1
  - name: v2
    labels:
      version: v2
  - name: v3
    labels:
      version: v3
```

访问

清理：

kubectl delete vs bookinfo -n istio

kubectl delete gw bookinfo-gateway -n istio

kubectl delete vs reviews -n istio

kubectl  delete dr reviews -n  istio



![1627707004(1)](images\1627707004(1).jpg)





set

virtaulservice/route/vs-reviews-headers-request-set.yaml

kubectl  apply -f vs-reviews-headers-request-set.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: reviews
spec:
  hosts:
    - reviews
  http:
  - route:
    - destination:
        host: reviews
        subset: v1
      headers:
        request:
          set:
            test: test
```

virtaulservice/vs-bookinfo-hosts-star.yaml

kubectl apply -f vs-bookinfo-hosts-star.yaml -n istio

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

dr-review.yaml

kubectl apply -f dr-review.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: DestinationRule
metadata:
  name: reviews
spec:
  host: reviews
  subsets:
  - name: v1
    labels:
      version: v1
  - name: v2
    labels:
      version: v2
  - name: v3
    labels:
      version: v3
```

访问

清理：

kubectl delete vs bookinfo -n istio

kubectl delete gw bookinfo-gateway -n istio

kubectl delete vs reviews -n istio

kubectl  delete dr reviews -n  istio

![1627707113(1)](images\1627707113(1).jpg)





###### response

add

virtaulservice/route/vs-bookinfo-headers-response-add.yaml

kubectl apply -f vs-bookinfo-headers-response-add.yaml -n istio

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
          number: 9080
      headers:
        response:
          add:
            test: test
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

kubectl delete vs bookinfo -n istio

kubectl delete gw bookinfo-gateway -n istio



![1627707336(1)](images\1627707336(1).jpg)



remove

virtaulservice/route/vs-bookinfo-headers-response-remove.yaml

kubectl apply -f vs-bookinfo-headers-response-remove.yaml -n istio

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
          number: 9080
      headers:
        response:
          remove:
          - x-envoy-upstream-service-time
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

kubectl delete vs bookinfo -n istio

kubectl delete gw bookinfo-gateway -n istio

![1627707398(1)](images\1627707398(1).jpg)





set

virtaulservice/route/vs-bookinfo-headers-response-set.yaml

kubectl apply -f vs-bookinfo-headers-response-set.yaml -n istio

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
          number: 9080
      headers:
        response:
          set:
            content-type: "text/html"
            test: test
            x-envoy-upstream-service-time: "1111"

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

kubectl delete vs bookinfo -n istio

kubectl delete gw bookinfo-gateway -n istio





![1627707474(1)](images\1627707474(1).jpg)



##### weight

virtaulservice/route/vs-reviews-weight.yaml

kubectl apply -f vs-reviews-weight.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: reviews
spec:
  hosts:
    - reviews
  http:
  - route:
    - destination:
        host: reviews
        subset: v1
      weight: 50
    - destination:
        host: reviews
        subset: v3
      weight: 50
```

virtaulservice/vs-bookinfo-hosts-star.yaml

kubectl apply -f vs-bookinfo-hosts-star.yaml -n istio

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

dr-review.yaml

kubectl apply -f dr-review.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: DestinationRule
metadata:
  name: reviews
spec:
  host: reviews
  subsets:
  - name: v1
    labels:
      version: v1
  - name: v2
    labels:
      version: v2
  - name: v3
    labels:
      version: v3
```

访问

清理：

kubectl delete vs bookinfo -n istio

kubectl delete gw bookinfo-gateway -n istio

kubectl delete vs reviews -n istio

kubectl  delete dr reviews -n  istio



 kubectl port-forward --address 0.0.0.0 -n istio reviews-v1-545db77b95-zqr6s 15001:15000



![1627707791(1)](images\1627707791(1).jpg)





#### 6-16timeout

virtaulservice/timeout/vs-http-timeout.yaml

kubectl apply -f vs-http-timeout.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: bookinfo
spec:
  exportTo:
  - '*'
  gateways:
  - bookinfo-gateway
  hosts:
  - '*'
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
        host: productpage
        port:
          number: 9080
    timeout: 0.03s
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

kubectl delete vs bookinfo -n istio

kubectl delete gw bookinfo-gateway -n istio

![1627708031(1)](images\1627708031(1).jpg)





###  tls 

 一个有序列表，对应的是透传 TLS 和 HTTPS 流量。路由过程通常利用 `ClientHello` 消息中的 SNI 来完成。TLS 路由通常应用在 `https-`、`tls-` 前缀的平台服务端口，或者经 `Gateway` 透传的 HTTPS、TLS 协议端口，以及使用 HTTPS 或者 TLS 协议的 `ServiceEntry` 端口上。**注意：没有关联 VirtualService 的 https- 或者 tls- 端口流量会被视为透传 TCP 流量。**



tls termination和tls origination

https://blog.csdn.net/zhongbeida_xue/article/details/113629092

#### 6-17match 

##### sniHosts

1创建证书

 openssl req -x509 -sha256 -nodes -days 365 -newkey rsa:2048 -subj '/O=example Inc./CN=example.com' -keyout example.com.key -out example.com.crt 

 openssl req -out nginx.example.com.csr -newkey rsa:2048 -nodes -keyout nginx.example.com.key -subj "/CN=nginx.example.com/O=some organization" 

 openssl x509 -req -days 365 -CA example.com.crt -CAkey example.com.key -set_serial 0 -in nginx.example.com.csr -out nginx.example.com.crt 

2创建secret

 kubectl create secret tls nginx-server-certs --key nginx.example.com.key --cert nginx.example.com.crt  -n istio

3创建nginx配置文件

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

nginx-deploy.yaml

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

5创建gateway

virtualservice/tls/gw-mode-passthrough.yaml

kubectl apply -f gw-mode-passthrough.yaml -n istio

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
      number: 443
      name: https-443
      protocol: HTTPS
    hosts:
    - "nginx.example.com"
    tls:
      mode: PASSTHROUGH
```

6创建vs

virtualservice/tls/vs-nginx-sniHosts.yaml

kubectl apply -f vs-nginx-sniHosts.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: nginx
spec:
  hosts:
  - nginx.example.com
  - bookinfo.example
  gateways:
  - bookinfo-gateway
  tls:
  - match:
    - sniHosts:
      - nginx.example.com
      - bookinfo.example
    route:
    - destination:
        host: my-nginx
        port:
          number: 443
```

7访问url

 https://nginx.example.com:39329/ 

清理：

kubectl  delete -f nginx-deploy.yaml -n istio

kubectl delete vs nginx -n istio

kubectl delete gw bookinfo-gateway -n istio

kubectl delete configmap nginx-configmap   -nistio

kubectl delete secret  nginx-server-certs   -n istio



![1627709092(1)](images\1627709092(1).jpg)



##### port

1创建证书

 openssl req -x509 -sha256 -nodes -days 365 -newkey rsa:2048 -subj '/O=example Inc./CN=example.com' -keyout example.com.key -out example.com.crt 

 openssl req -out nginx.example.com.csr -newkey rsa:2048 -nodes -keyout nginx.example.com.key -subj "/CN=nginx.example.com/O=some organization" 

 openssl x509 -req -days 365 -CA example.com.crt -CAkey example.com.key -set_serial 0 -in nginx.example.com.csr -out nginx.example.com.crt 

2创建secret

 kubectl create secret tls nginx-server-certs --key nginx.example.com.key --cert nginx.example.com.crt  -n istio

3创建nginx配置文件

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

nginx-deploy-v1.yaml

kubectl apply -f nginx-deploy-v1.yaml -n istio

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

5创建gateway

virtualservice/tls/gw-mode-passthrough.yaml

kubectl apply -f gw-mode-passthrough.yaml -n istio

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
      number: 443
      name: https-443
      protocol: HTTPS
    hosts:
    - "nginx.example.com"
    tls:
      mode: PASSTHROUGH
```

dr-my-nginx-v1-v2.yaml

kubectl  apply -f dr-my-nginx-v1-v2.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: DestinationRule
metadata:
  name: my-nginx
spec:
  host: my-nginx
  subsets:
  - name: v1
    labels:
      run: my-nginx
      version: v1
  - name: v2
    labels:
      run: my-nginx
      version: v2
```



创建vs

virtualservice/tls/vs-nginx-port.yaml

kubectl apply -f vs-nginx-port.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: nginx
spec:
  hosts:
  - nginx.example.com
  gateways:
  - bookinfo-gateway
  tls:
  - match:
    - port: 443
      sniHosts:
      - nginx.example.com
    route:
    - destination:
        host: my-nginx
        port:
          number: 443
        subset: v1
```

访问url

 https://nginx.example.com:39329/ 

清理：

kubectl  delete -f nginx-deploy.yaml -n istio

kubectl delete vs nginx -n istio

kubectl delete gw bookinfo-gateway -n istio

kubectl delete configmap nginx-configmap   -nistio

kubectl delete secret  nginx-server-certs   -n istio

kubectl delete dr my-nginx -n istio



端口匹配才会有下面配置

![1627709743(1)](images\1627709743(1).jpg)

##### gateways

1创建证书

 openssl req -x509 -sha256 -nodes -days 365 -newkey rsa:2048 -subj '/O=example Inc./CN=example.com' -keyout example.com.key -out example.com.crt 

 openssl req -out nginx.example.com.csr -newkey rsa:2048 -nodes -keyout nginx.example.com.key -subj "/CN=nginx.example.com/O=some organization" 

 openssl x509 -req -days 365 -CA example.com.crt -CAkey example.com.key -set_serial 0 -in nginx.example.com.csr -out nginx.example.com.crt 

2创建secret

 kubectl create secret tls nginx-server-certs --key nginx.example.com.key --cert nginx.example.com.crt  -n istio

3创建nginx配置文件

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

nginx-deploy-v1.yaml

kubectl apply -f nginx-deploy-v1.yaml -n istio

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

5创建gateway

virtualservice/tls/gw-mode-passthrough.yaml

kubectl apply -f gw-mode-passthrough.yaml -n istio

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
      number: 443
      name: https-443
      protocol: HTTPS
    hosts:
    - "nginx.example.com"
    tls:
      mode: PASSTHROUGH
```

dr-my-nginx-v1-v2.yaml

kubectl  apply -f dr-my-nginx-v1-v2.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: DestinationRule
metadata:
  name: my-nginx
spec:
  host: my-nginx
  subsets:
  - name: v1
    labels:
      run: my-nginx
      version: v1
  - name: v2
    labels:
      run: my-nginx
      version: v2
```

1.7.0/virtaulservice/tls/vs-nginx-gateways.yaml 

kubectl apply -f vs-nginx-gateways.yaml  -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: nginx
spec:
  hosts:
  - nginx.example.com
  gateways:
  - bookinfo-gateway
  tls:
  - match:
    - port: 443
      sniHosts:
      - nginx.example.com
      gateways:
      - bookinfo-gateway
    route:
    - destination:
        host: my-nginx
        port:
          number: 443
        subset: v1
```



清理：

kubectl  delete -f nginx-deploy.yaml -n istio

kubectl delete vs nginx -n istio

kubectl delete gw bookinfo-gateway -n istio

kubectl delete configmap nginx-configmap   -nistio

kubectl delete secret  nginx-server-certs   -n istio

kubectl delete dr my-nginx -n istio



gateway匹配才会有下面配置

![1627710122(1)](images\1627710122(1).jpg)





##### destinationSubnets

只对mesh traffic有效，destinationSubnets为svc ip

cd certs-2

 openssl req -x509 -sha256 -nodes -days 365 -newkey rsa:2048 -subj '/O=example Inc./CN=example.com' -keyout example.com.key -out example.com.crt 

openssl req -out nginx.example.com.csr -newkey rsa:2048 -nodes -keyout nginx.example.com.key -subj "/CN=my-nginx/O=some organization" 

 openssl x509 -req -days 365 -CA example.com.crt -CAkey example.com.key -set_serial 0 -in nginx.example.com.csr -out nginx.example.com.crt 

2创建secret

 kubectl create secret tls nginx-server-certs --key nginx.example.com.key --cert nginx.example.com.crt  -n istio

创建nginx配置文件

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

3

部署v1

nginx-deploy-v1.yaml

kubectl apply -f nginx-deploy-v1.yaml -n istio

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
    name: https-nginx
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
      version: v1
  replicas: 1
  template:
    metadata:
      labels:
        run: my-nginx
        version: v1
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



部署v2

kubectl apply -f nginx-deploy-v2.yaml -nistio

```
apiVersion: apps/v1
kind: Deployment
metadata:
  name: my-nginx-v2
spec:
  selector:
    matchLabels:
      run: my-nginx
      version: v2
  replicas: 1
  template:
    metadata:
      labels:
        run: my-nginx
        version: v2
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

 kubectl exec -it -n istio my-nginx-57fb7765cb-nfxb7 -- /bin/bash

echo "nginx-01" > /usr/share/nginx/html/index.html 

kubectl exec -it -n istio my-nginx-v2-78bdfbf89f-slcmx -- /bin/bash

 echo "nginx-02" > /usr/share/nginx/html/index.html



部署sleep

virtualservice/tls/sleep.yaml

kubectl  apply -f sleep.yaml -n istio

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

 kubectl cp certs/example.com.crt -n istio sleep-557747455f-p24hz:/tmp/

4创建规则

dr-my-nginx-v1-v2.yaml 

kubectl apply -f dr-my-nginx-v1-v2.yaml  -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: DestinationRule
metadata:
  name: my-nginx
spec:
  host: my-nginx
  subsets:
  - name: v1
    labels:
      run: my-nginx
      version: v1
  - name: v2
    labels:
      run: my-nginx
      version: v2
```



1.7.0/virtaulservice/tls/vs-nginx-destinationSubnets.yaml

kubectl apply -f vs-nginx-destinationSubnets.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: my-nginx
spec:
  hosts:
  - my-nginx
  tls:
  - match:
    - sniHosts:
      - my-nginx
      destinationSubnets: 
      - "10.0.0.0/8"
    route:
    - destination:
        host: my-nginx
        subset: v2
```

kubectl cp certs-2/example.com.crt -n istio sleep-557747455f-p24hz:/tmp/

 kubectl exec -it -n istio sleep-557747455f-p24hz  -- /bin/sh

curl -v  --cacert /tmp/example.com.crt     "https://my-nginx" 

清理：

kubectl delete vs my-nginx -n istio

kubectl delete dr my-nginx -n istio

kubectl  delete -f sleep.yaml -n istio

kubectl delete -f nginx-deploy-v2.yaml -nistio

kubectl delete -f nginx-deploy-v1.yaml -n istio

kubectl delete configmap nginx-configmap -nistio

kubectl delete secret nginx-server-certs  -n istio



![1627715165(1)](images\1627715165(1).jpg)





##### sourceLabels

只有mesh traffic有效

cd certs-2

 openssl req -x509 -sha256 -nodes -days 365 -newkey rsa:2048 -subj '/O=example Inc./CN=example.com' -keyout example.com.key -out example.com.crt 

openssl req -out nginx.example.com.csr -newkey rsa:2048 -nodes -keyout nginx.example.com.key -subj "/CN=my-nginx/O=some organization" 

 openssl x509 -req -days 365 -CA example.com.crt -CAkey example.com.key -set_serial 0 -in nginx.example.com.csr -out nginx.example.com.crt 

2创建secret

 kubectl create secret tls nginx-server-certs --key nginx.example.com.key --cert nginx.example.com.crt  -n istio

创建nginx配置文件

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

3

部署v1

nginx-deploy-v1.yaml

kubectl apply -f nginx-deploy-v1.yaml -n istio

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
    name: https-nginx
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
      version: v1
  replicas: 1
  template:
    metadata:
      labels:
        run: my-nginx
        version: v1
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



部署v2

kubectl apply -f nginx-deploy-v2.yaml -nistio

```
apiVersion: apps/v1
kind: Deployment
metadata:
  name: my-nginx-v2
spec:
  selector:
    matchLabels:
      run: my-nginx
      version: v2
  replicas: 1
  template:
    metadata:
      labels:
        run: my-nginx
        version: v2
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

 kubectl exec -it -n istio my-nginx-57fb7765cb-nfxb7 -- /bin/bash

echo "nginx-01" > /usr/share/nginx/html/index.html 

kubectl exec -it -n istio my-nginx-v2-78bdfbf89f-slcmx -- /bin/bash

 echo "nginx-02" > /usr/share/nginx/html/index.html



部署sleep

virtualservice/tls/sleep.yaml

kubectl  apply -f sleep.yaml -n istio

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

 kubectl cp certs/example.com.crt -n istio sleep-557747455f-p24hz:/tmp/

4创建规则

dr-my-nginx-v1-v2.yaml 

kubectl apply -f dr-my-nginx-v1-v2.yaml  -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: DestinationRule
metadata:
  name: my-nginx
spec:
  host: my-nginx
  subsets:
  - name: v1
    labels:
      run: my-nginx
      version: v1
  - name: v2
    labels:
      run: my-nginx
      version: v2
```



1.7.0/virtaulservice/tls/vs-nginx-sourceLabels.yaml

kubectl apply -f vs-nginx-sourceLabels.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: my-nginx
spec:
  hosts:
  - my-nginx
  tls:
  - match:
    - sniHosts:
      - my-nginx
      sourceLabels:
        app: sleep
    route:
    - destination:
        host: my-nginx
        port:
          number: 443
        subset: v2
```

 kubectl exec -it -n istio sleep-557747455f-p24hz  -- /bin/sh

curl -v  --cacert /tmp/example.com.crt     "https://my-nginx" 

清理：

kubectl delete vs my-nginx -n istio

kubectl delete dr my-nginx -n istio

kubectl  delete -f sleep.yaml -n istio

kubectl delete -f nginx-deploy-v2.yaml -nistio

kubectl delete -f nginx-deploy-v1.yaml -n istio

kubectl delete configmap nginx-configmap -nistio

kubectl delete secret nginx-server-certs  -n istio



kubectl port-forward --address 0.0.0.0 -n istio sleep-557747455f-t7bt4  15001:15000

只有sleep pod 有这个配置

![1627716346(1)](images\1627716346(1).jpg)



##### sourceNamespace

只有mesh traffic有效

cd certs-2

 openssl req -x509 -sha256 -nodes -days 365 -newkey rsa:2048 -subj '/O=example Inc./CN=example.com' -keyout example.com.key -out example.com.crt 

openssl req -out nginx.example.com.csr -newkey rsa:2048 -nodes -keyout nginx.example.com.key -subj "/CN=my-nginx/O=some organization" 

 openssl x509 -req -days 365 -CA example.com.crt -CAkey example.com.key -set_serial 0 -in nginx.example.com.csr -out nginx.example.com.crt 

2创建secret

 kubectl create secret tls nginx-server-certs --key nginx.example.com.key --cert nginx.example.com.crt  -n istio

创建nginx配置文件

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

3

部署v1

nginx-deploy-v1.yaml

kubectl apply -f nginx-deploy-v1.yaml -n istio

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
    name: https-nginx
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
      version: v1
  replicas: 1
  template:
    metadata:
      labels:
        run: my-nginx
        version: v1
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



部署v2

kubectl apply -f nginx-deploy-v2.yaml -nistio

```
apiVersion: apps/v1
kind: Deployment
metadata:
  name: my-nginx-v2
spec:
  selector:
    matchLabels:
      run: my-nginx
      version: v2
  replicas: 1
  template:
    metadata:
      labels:
        run: my-nginx
        version: v2
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

 kubectl exec -it -n istio my-nginx-57fb7765cb-nfxb7 -- /bin/bash

echo "nginx-01" > /usr/share/nginx/html/index.html 

kubectl exec -it -n istio my-nginx-v2-78bdfbf89f-slcmx -- /bin/bash

 echo "nginx-02" > /usr/share/nginx/html/index.html



部署sleep

virtualservice/tls/sleep.yaml

kubectl  apply -f sleep.yaml -n istio

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

 kubectl cp certs-2/example.com.crt -n istio sleep-557747455f-p24hz:/tmp/

4创建规则

dr-my-nginx-v1-v2.yaml 

kubectl apply -f dr-my-nginx-v1-v2.yaml  -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: DestinationRule
metadata:
  name: my-nginx
spec:
  host: my-nginx
  subsets:
  - name: v1
    labels:
      run: my-nginx
      version: v1
  - name: v2
    labels:
      run: my-nginx
      version: v2
```





1.7.0/virtaulservice/tls/vs-nginx-sourceNamespace.yaml 

kubectl apply -f vs-nginx-sourceNamespace.yaml  -n istio

```
apiVersion: networking.istio.io/v1alpha3
kind: VirtualService
metadata:
  name: my-nginx
spec:
  hosts:
  - my-nginx
  tls:
  - match:
    - port: 443
      sniHosts:
      - my-nginx
      sourceNamespace: istio
    route:
    - destination:
        host: my-nginx
        port:
          number: 443
        subset: v1
```

 kubectl exec -it -n istio sleep-557747455f-p24hz  -- /bin/sh

curl -v  --cacert /tmp/example.com.crt     "https://my-nginx" 

清理：

kubectl delete vs my-nginx -n istio

kubectl delete dr my-nginx -n istio

kubectl  delete -f sleep.yaml -n istio

kubectl delete -f nginx-deploy-v2.yaml -nistio

kubectl delete -f nginx-deploy-v1.yaml -n istio

kubectl delete configmap nginx-configmap -nistio

kubectl delete secret nginx-server-certs  -n istio



kubectl port-forward --address 0.0.0.0 -n istio sleep-557747455f-t7bt4  15001:15000

有这个说明namespace生效

![1627716596(1)](images\1627716596(1).jpg)

#### route

和http基本一样，不介绍

### tcp

 一个针对透传 TCP 流量的有序路由列表。TCP 路由对所有 HTTP 和 TLS 之外的端口生效。进入流量会使用匹配到的第一条规则。

#### 6-18match

##### port

1部署deploy

 kubectl apply -f tcp-echo-services.yaml -n istio

tcp-echo-services.yaml

```
apiVersion: v1
kind: Service
metadata:
  name: tcp-echo
  labels:
    app: tcp-echo
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

2添加service 端口

kubectl edit svc istio-ingressgateway -n istio-system

```
  - name: tcp
    port: 31400
    protocol: TCP
    targetPort: 31400
```

3 创建资源

kubectl apply -f tcp-echo-all-v1.yaml -n istio

tcp-echo-all-v1.yaml  

```
apiVersion: networking.istio.io/v1beta1
kind: Gateway
metadata:
  name: tcp-echo-gateway
spec:
  selector:
    istio: ingressgateway
  servers:
  - port:
      number: 31400
      name: tcp
      protocol: TCP
    hosts:
    - "*"
---
apiVersion: networking.istio.io/v1beta1
kind: DestinationRule
metadata:
  name: tcp-echo-destination
spec:
  host: tcp-echo
  subsets:
  - name: v1
    labels:
      version: v1
  - name: v2
    labels:
      version: v2
---
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: tcp-echo
spec:
  hosts:
  - "*"
  gateways:
  - tcp-echo-gateway
  tcp:
  - match:
    - port: 31400
    route:
    - destination:
        host: tcp-echo
        port:
          number: 9000
        subset: v1
```

4访问

telnet 192.168.198.154 37048

清理：

kubectl delete -f tcp-echo-all-v1.yaml -n istio

kubectl delete -f tcp-echo-services.yaml -n istio



端口匹配

![1627717291(1)](images\1627717291(1).jpg)



##### destinationSubnets

只对mesh traffic有效，子网是service的子网

部署deploy

 kubectl apply -f tcp-echo-services.yaml -n istio

tcp-echo-services.yaml

```
apiVersion: v1
kind: Service
metadata:
  name: tcp-echo
  labels:
    app: tcp-echo
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



virtaulservice/tcp/vs-destinationSubnets.yaml

kubectl apply -f vs-destinationSubnets.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: tcp-echo
spec:
  hosts:
  - tcp-echo
  tcp:
  - match:
    - destinationSubnets:
      - 10.68.0.0/16
    route:
    - destination:
        host: tcp-echo
        subset: v1
```

dr-tcp-echo.yaml

kubectl apply -f dr-tcp-echo.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: DestinationRule
metadata:
  name: tcp-echo-destination
spec:
  host: tcp-echo
  subsets:
  - name: v1
    labels:
      version: v1
  - name: v2
    labels:
      version: v2
```

部署 busybox

 busybox.yaml

kubectl apply -f busybox.yaml -n istio

```
apiVersion: v1
kind: ServiceAccount
metadata:
  name: busybox
---
apiVersion: v1
kind: Service
metadata:
  name: busybox
  labels:
    app: busybox
    service: busybox
spec:
  ports:
  - port: 80
    name: http
  selector:
    app: busybox
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: busybox
spec:
  replicas: 1
  selector:
    matchLabels:
      app: busybox
  template:
    metadata:
      labels:
        app: busybox
    spec:
      terminationGracePeriodSeconds: 0
      serviceAccountName: busybox
      containers:
      - name: busybox
        image: busybox
        command: ["/bin/sleep", "3650d"]
        imagePullPolicy: IfNotPresent
---
```

kubectl exec -it busybox-557747455f-mxpl4 -n istio  /bin/sh

telnet tcp-echo 9000

清理：

kubectl delete -f busybox.yaml -n istio

kubectl delete -f dr-tcp-echo.yaml -n istio

kubectl delete vs tcp-echo -n istio

kubectl delete -f tcp-echo-services.yaml -n istio



kubectl port-forward --address 0.0.0.0 -n istio tcp-echo-v1-7dd5c5dcfb-4mzt9  15001:15000



![1627718421(1)](images\1627718421(1).jpg)





##### sourceSubnet

只对mesh有效

virtaulservice/tcp/vs-sourceSubnet.yaml

kubectl apply -f vs-sourceSubnet.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: tcp-echo
spec:
  hosts:
  - "tcp-echo"
  tcp:
  - match:
    - sourceSubnet: 172.20.0.0/16
    route:
    - destination:
        host: tcp-echo
        port:
          number: 9000
        subset: v2
```

部署deploy

 kubectl apply -f tcp-echo-services.yaml -n istio

tcp-echo-services.yaml

```
apiVersion: v1
kind: Service
metadata:
  name: tcp-echo
  labels:
    app: tcp-echo
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



dr-tcp-echo.yaml

kubectl apply -f dr-tcp-echo.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: DestinationRule
metadata:
  name: tcp-echo-destination
spec:
  host: tcp-echo
  subsets:
  - name: v1
    labels:
      version: v1
  - name: v2
    labels:
      version: v2
```

部署busybox

busybox.yaml 

kubectl apply -f busybox.yaml -n istio

```
apiVersion: v1
kind: ServiceAccount
metadata:
  name: busybox
---
apiVersion: v1
kind: Service
metadata:
  name: busybox
  labels:
    app: busybox
    service: busybox
spec:
  ports:
  - port: 80
    name: http
  selector:
    app: busybox
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: busybox
spec:
  replicas: 1
  selector:
    matchLabels:
      app: busybox
  template:
    metadata:
      labels:
        app: busybox
    spec:
      terminationGracePeriodSeconds: 0
      serviceAccountName: busybox
      containers:
      - name: busybox
        image: busybox
        command: ["/bin/sleep", "3650d"]
        imagePullPolicy: IfNotPresent
---
```

kubectl exec -it busybox-557747455f-mxpl4 -n istio  /bin/sh

telnet tcp-echo 9000

清理：

kubectl delete -f busybox.yaml -n istio

kubectl delete -f dr-tcp-echo.yaml -n istio

kubectl delete vs tcp-echo -n istio

kubectl delete -f tcp-echo-services.yaml -n istio



kubectl port-forward --address 0.0.0.0 -n istio tcp-echo-v1-7dd5c5dcfb-4mzt9 15000:15000

![1627787352(1)](images\1627787352(1).jpg)





##### sourceLabels

virtaulservice/tcp/vs-sourceLabels.yaml

kubectl apply -f vs-sourceLabels.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: tcp-echo
spec:
  hosts:
  - "*"
  gateways:
  - tcp-echo-gateway
  tcp:
  - match:
    - sourceLabels:
        app: istio-ingressgateway
    route:
    - destination:
        host: tcp-echo
        port:
          number: 9000
        subset: v2
```

kubectl apply -f tcp-echo-services.yaml -n istio

tcp-echo-services.yaml

```
apiVersion: v1
kind: Service
metadata:
  name: tcp-echo
  labels:
    app: tcp-echo
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



dr-tcp-echo.yaml

kubectl apply -f dr-tcp-echo.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: DestinationRule
metadata:
  name: tcp-echo-destination
spec:
  host: tcp-echo
  subsets:
  - name: v1
    labels:
      version: v1
  - name: v2
    labels:
      version: v2
```

gw-tcp.yaml

kubectl apply -f gw-tcp.yaml -n istio

```
kind: Gateway
metadata:
  name: tcp-echo-gateway
spec:
  selector:
    istio: ingressgateway
  servers:
  - port:
      number: 31400
      name: tcp
      protocol: TCP
    hosts:
    - "*"
```

清理：

kubectl delete gw tcp-echo-gateway -n istio

kubectl delete -f dr-tcp-echo.yaml -n istio

kubectl delete vs tcp-echo -n istio

kubectl delete -f tcp-echo-services.yaml -n istio



![1627787598(1)](images\1627787598(1).jpg)





##### sourceNamespace

只对mesh traffic有效

virtaulservice/tcp/vs-sourceNamespace.yaml

kubectl apply -f vs-sourceNamespace.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: tcp-echo
spec:
  hosts:
  - "tcp-echo"
  tcp:
  - match:
    - sourceNamespace: istio
    route:
    - destination:
        host: tcp-echo
        port:
          number: 9000
        subset: v2
```

部署deploy

 kubectl apply -f tcp-echo-services.yaml -n istio

tcp-echo-services.yaml

```
apiVersion: v1
kind: Service
metadata:
  name: tcp-echo
  labels:
    app: tcp-echo
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



dr-tcp-echo.yaml

kubectl apply -f dr-tcp-echo.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: DestinationRule
metadata:
  name: tcp-echo-destination
spec:
  host: tcp-echo
  subsets:
  - name: v1
    labels:
      version: v1
  - name: v2
    labels:
      version: v2
```

部署busybox

busybox.yaml 

kubectl apply -f busybox.yaml -n istio

```
apiVersion: v1
kind: ServiceAccount
metadata:
  name: busybox
---
apiVersion: v1
kind: Service
metadata:
  name: busybox
  labels:
    app: busybox
    service: busybox
spec:
  ports:
  - port: 80
    name: http
  selector:
    app: busybox
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: busybox
spec:
  replicas: 1
  selector:
    matchLabels:
      app: busybox
  template:
    metadata:
      labels:
        app: busybox
    spec:
      terminationGracePeriodSeconds: 0
      serviceAccountName: busybox
      containers:
      - name: busybox
        image: busybox
        command: ["/bin/sleep", "3650d"]
        imagePullPolicy: IfNotPresent
---
```

kubectl exec -it busybox-557747455f-mxpl4 -n istio  /bin/sh

telnet tcp-echo 9000

清理：

kubectl delete -f busybox.yaml -n istio

kubectl delete -f dr-tcp-echo.yaml -n istio

kubectl delete vs tcp-echo -n istio

kubectl delete -f tcp-echo-services.yaml -n istio



kubectl port-forward --address 0.0.0.0 -n istio tcp-echo-v1-7dd5c5dcfb-4mzt9 15000:15000

![1627787765(1)](images\1627787765(1).jpg)





##### gateways

virtaulservice/tcp/vs-gateways.yaml

kubectl apply -f vs-gateways.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: tcp-echo
spec:
  hosts:
  - "*"
  gateways:
  - tcp-echo-gateway
  tcp:
  - match:
    - gateways:
      - tcp-echo-gateway
    route:
    - destination:
        host: tcp-echo
        port:
          number: 9000
        subset: v2
```

kubectl apply -f tcp-echo-services.yaml -n istio

tcp-echo-services.yaml

```
apiVersion: v1
kind: Service
metadata:
  name: tcp-echo
  labels:
    app: tcp-echo
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



dr-tcp-echo.yaml

kubectl apply -f dr-tcp-echo.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: DestinationRule
metadata:
  name: tcp-echo-destination
spec:
  host: tcp-echo
  subsets:
  - name: v1
    labels:
      version: v1
  - name: v2
    labels:
      version: v2
```

gw-tcp.yaml

kubectl apply -f gw-tcp.yaml -n istio

```
kind: Gateway
metadata:
  name: tcp-echo-gateway
spec:
  selector:
    istio: ingressgateway
  servers:
  - port:
      number: 31400
      name: tcp
      protocol: TCP
    hosts:
    - "*"
```

清理：

kubectl delete gw tcp-echo-gateway -n istio

kubectl delete -f dr-tcp-echo.yaml -n istio

kubectl delete vs tcp-echo -n istio

kubectl delete -f tcp-echo-services.yaml -n istio



![1627787962(1)](images\1627787962(1).jpg)



#### 6-19route

##### destination

###### host

virtaulservice/tcp/vs-route-host.yaml

kubectl apply -f vs-route-host.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: tcp-echo
spec:
  hosts:
  - "*"
  gateways:
  - tcp-echo-gateway
  tcp:
  - route:
    - destination:
        host: tcp-echo
        port:
          number: 9000
```

gw-tcp.yaml

kubectl apply -f gw-tcp.yaml -n istio

```
kind: Gateway
metadata:
  name: tcp-echo-gateway
spec:
  selector:
    istio: ingressgateway
  servers:
  - port:
      number: 31400
      name: tcp
      protocol: TCP
    hosts:
    - "*"
```

kubectl apply -f tcp-echo-services.yaml -n istio

tcp-echo-services.yaml

```
apiVersion: v1
kind: Service
metadata:
  name: tcp-echo
  labels:
    app: tcp-echo
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

清理：

kubectl delete gw tcp-echo-gateway -n istio

kubectl delete vs tcp-echo -n istio

kubectl delete -f tcp-echo-services.yaml -n istio



![1627788439(1)](images\1627788439(1).jpg)





###### port

virtaulservice/tcp/vs-route-port.yaml

kubectl apply -f vs-route-port.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: tcp-echo
spec:
  hosts:
  - "*"
  gateways:
  - tcp-echo-gateway
  tcp:
  - route:
    - destination:
        host: tcp-echo
        port:
          number: 9000
```

gw-tcp.yaml

kubectl apply -f gw-tcp.yaml -n istio

```
kind: Gateway
metadata:
  name: tcp-echo-gateway
spec:
  selector:
    istio: ingressgateway
  servers:
  - port:
      number: 31400
      name: tcp
      protocol: TCP
    hosts:
    - "*"
```

kubectl apply -f tcp-echo-services.yaml -n istio

tcp-echo-services.yaml

```
apiVersion: v1
kind: Service
metadata:
  name: tcp-echo
  labels:
    app: tcp-echo
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

清理：

kubectl delete gw tcp-echo-gateway -n istio

kubectl delete vs tcp-echo -n istio

kubectl delete -f tcp-echo-services.yaml -n istio

![1627788497(1)](images\1627788497(1).jpg)





###### subset

virtaulservice/tcp/vs-route-subset.yaml

kubectl apply -f vs-route-subset.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: tcp-echo
spec:
  hosts:
  - "*"
  gateways:
  - tcp-echo-gateway
  tcp:
  - route:
    - destination:
        host: tcp-echo
        subset: v2
        port:
          number: 9000
```

gw-tcp.yaml

kubectl apply -f gw-tcp.yaml -n istio

```
kind: Gateway
metadata:
  name: tcp-echo-gateway
spec:
  selector:
    istio: ingressgateway
  servers:
  - port:
      number: 31400
      name: tcp
      protocol: TCP
    hosts:
    - "*"
```

kubectl apply -f tcp-echo-services.yaml -n istio

tcp-echo-services.yaml

```
apiVersion: v1
kind: Service
metadata:
  name: tcp-echo
  labels:
    app: tcp-echo
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

dr-tcp-echo.yaml

kubectl apply -f dr-tcp-echo.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: DestinationRule
metadata:
  name: tcp-echo-destination
spec:
  host: tcp-echo
  subsets:
  - name: v1
    labels:
      version: v1
  - name: v2
    labels:
      version: v2
```

清理：

kubectl delete gw tcp-echo-gateway -n istio

kubectl delete -f dr-tcp-echo.yaml -n istio

kubectl delete vs tcp-echo -n istio

kubectl delete -f tcp-echo-services.yaml -n istio



![1627788555](images\1627788555.jpg)

##### weight

virtaulservice/tcp/tcp-echo-20-v2.yaml 

kubectl apply -f tcp-echo-20-v2.yaml  -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: tcp-echo
spec:
  hosts:
  - "*"
  gateways:
  - tcp-echo-gateway
  tcp:
  - match:
    - port: 31400
    route:
    - destination:
        host: tcp-echo
        port:
          number: 9000
        subset: v1
      weight: 80
    - destination:
        host: tcp-echo
        port:
          number: 9000
        subset: v2
      weight: 20
```

gw-tcp.yaml

kubectl apply -f gw-tcp.yaml -n istio

```
kind: Gateway
metadata:
  name: tcp-echo-gateway
spec:
  selector:
    istio: ingressgateway
  servers:
  - port:
      number: 31400
      name: tcp
      protocol: TCP
    hosts:
    - "*"
```

kubectl apply -f tcp-echo-services.yaml -n istio

tcp-echo-services.yaml

```
apiVersion: v1
kind: Service
metadata:
  name: tcp-echo
  labels:
    app: tcp-echo
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

dr-tcp-echo.yaml

kubectl apply -f dr-tcp-echo.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: DestinationRule
metadata:
  name: tcp-echo-destination
spec:
  host: tcp-echo
  subsets:
  - name: v1
    labels:
      version: v1
  - name: v2
    labels:
      version: v2
```

清理：

kubectl delete gw tcp-echo-gateway -n istio

kubectl delete -f dr-tcp-echo.yaml -n istio

kubectl delete vs tcp-echo -n istio

kubectl delete -f tcp-echo-services.yaml -n istio



![1627788640(1)](images\1627788640(1).jpg)



## 6-20三种协议路由规则对比

 VirtualService 在http、tls、tcp这三个字段上分别定义了应用于HTTP、TLS和TCP三种协议的路由规则。从规则构成上都是先定义一组匹配条件，然后对满足条件的的流量执行对应的操作。因为协议的内容不同，路由匹配条件不同，所以执行的操作也不同。如下表所示对比了三种路由规则。从各个维度来看，HTTP路由规则的内容最丰富，TCP路由规则的内容最少，这也符合协议分层的设计。 

![1597557680(1)](images\1597557680(1).jpg)

