学习目标

![1597990624(1)](images/\1597990624(1).jpg)

# 5-1什么是gateway

![1597990869(1)](images/\1597990869(1).jpg)

 在Kubernetes环境中，Kubernetes Ingress用于配置需要在集群外部公开的服务。但是在Istio服务网格中，更好的方法是使用新的配置模型，即Istio Gateway。Gateway允许将Istio流量管理的功能应用于进入集群的流量。 

gateway 分为两种，分别是ingress-gateway和egress-gateway，分别用来处理入口流量和出口流量。gateway本质也是一个envoy pod。

```
生成东西向网关
samples/multicluster/gen-eastwest-gateway.sh --mesh mesh1 --cluster cluster1 --network network1 |  istioctl  install  -y -f -
```



# 资源详解

## servers

### 5-2hosts

#### 所有域名：

gateway/gateway-server-hosts-star.yaml

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

```
kubectl apply -f gateway-server-hosts-star.yaml -n istio
vs在同一个名称空间中
kubectl apply -f vs-bookinfo-hosts-star.yaml -n istio


kubectl apply -f gateway-server-hosts-star.yaml -n istio-system
vs在同一个名称空间中
kubectl apply -f vs-bookinfo-hosts-star.yaml -n istio-system

gateway在istio-system名称空间，vs和gateway在不同名称空间
gateway/vs-bookinfo-hosts-star-diff-namespace.yaml
kubectl apply -f vs-bookinfo-hosts-star-diff-namespace.yaml -n istio
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: bookinfo
spec:
  hosts:
  - "*"
  gateways:
  - istio-system/bookinfo-gateway
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

```
vs和gateway都在istio-system名称空间中
vs host没有指定名称空间
gateway/vs-bookinfo-hosts-star-host-no-namespace.yaml
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: bookinfo
spec:
  hosts:
  - "*"
  gateways:
  - istio-system/bookinfo-gateway
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
访问不成功
host指定名称空间
productpage.istio.svc.cluster.local
kubectl edit vs  bookinfo -n istio-system
访问成功


```

**最佳实践1，域名用fqdn**

生成配置

![1627099539(1)](images/\1627099539(1).jpg)

#### 具体域名：

gateway/gateway-server-hosts-bookinfo-com.yaml

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
    - "bookinfo.com"
```

```
kubectl apply -f gateway-server-hosts-bookinfo-com.yaml -n istio-system
访问http://bookinfo.com:31110/productpage成功


gateway和vs  hosts关系:
gw和vs host一样
gateway/vs-bookinfo-hosts-star-gw-host-same.yaml
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: bookinfo
spec:
  hosts:
  - "bookinfo.com"
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
kubectl apply -f vs-bookinfo-hosts-star-gw-host-same.yaml -n istio-system
访问http://bookinfo.com:31110/productpage成功

gw和vs host是具体值，且步一样
gateway/vs-bookinfo-hosts-star-gw-host-diff.yaml
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: bookinfo
spec:
  hosts:
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
kubectl apply -f vs-bookinfo-hosts-star-gw-host-diff.yaml -n istio-system
访问http://bookinfo.com:31110/productpage失败

vs包含gw
gateway/vs-bookinfo-hosts-star-host-contain-gw.yaml
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: bookinfo
spec:
  hosts:
  - "*.com"
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
访问http://bookinfo.com:31110/productpage成功

vs host为任意
gateway/vs-bookinfo-hosts-star.yaml
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
kubectl apply -f vs-bookinfo-hosts-star.yaml -n istio-system
访问http://bookinfo.com:31110/productpage成功

vs host 为bookinfo.*
gateway/vs-bookinfo-hosts-star-mix-error.yaml
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: bookinfo
spec:
  hosts:
  - "bookinfo.*"
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
 kubectl apply -f vs-bookinfo-hosts-star-mix-error.yaml -n istio-system
Name: "bookinfo", Namespace: "istio-system"
for: "vs-bookinfo-hosts-star-mix-error.yaml": admission webhook "validation.istio.io" denied the request: configuration is invalid: domain name "*" invalid (label "*" invalid)

```

**vs和gw域名有效值是交叉关系**

![1627099760(1)](images/\1627099760(1).jpg)



#### 多个域名：

gateway/gateway-server-hosts-multi.yaml

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
    - "bookinfo.com"
    - "bookinfo.demo"
```

```
gateway/vs-bookinfo-hosts-star.yaml
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
kubectl apply -f vs-bookinfo-hosts-star.yaml -n istio-system
访问http://bookinfo.com:31110/productpage成功
访问http://bookinfo.demo:31110/productpage成功
```

![1627100070(1)](images/\1627100070(1).jpg)

#### 混合域名

gateway/gateway-server-hosts-mix.yaml

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
    - "*.com"
---
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: bookinfo
spec:
  hosts:
  - "bookinfo.com"
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

![1627100281(1)](images/\1627100281(1).jpg)

```
gateway/vs-bookinfo-hosts-mix.yaml
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: bookinfo
spec:
  hosts:
  - "*.com"
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

访问http://bookinfo.com:31110/productpage失败，端口问题

访问http://mypage.com/productpage 成功用了externalIp和80端口

![1627100449(1)](images/\1627100449(1).jpg)

### 5-3name 

gateway/gateway-server-name.yaml

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
    name: bookinfo-gateway
```

name的作用，比如stat_prefix

proxy配置暂时找不到这个配置

### port

| Field    | Type   | Description                                                  | Required |
| -------- | ------ | ------------------------------------------------------------ | -------- |
| number   | uint32 | 一个有效的端口号                                             | 是       |
| protocol | string | 所使用的协议，支持HTTP\\|HTTPS\|GRPC\|HTTP2\|MONGO\|TCP\|TLS. | 是       |
| name     | string | 给端口分配一个名称                                           | 是       |

istio支持的协议：

- `grpc`
- `grpc-web`
- `http`
- `http2`
- `https`
- `mongo`
- `mysql`*
- `redis`*
- `tcp`
- `tls`
- `udp`

\\* These protocols are disabled by default to avoid accidentally enabling experimental features. To enable them, configure the corresponding Pilot [environment variables](https://istio.io/latest/docs/reference/commands/pilot-discovery/#envvars).

#### 5-4http

1部署gateway

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

2部署vs

virtaulservice/vs-bookinfo-hosts-star.yaml 

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

3访问浏览器

![1627102638(1)](images/\1627102638(1).jpg)



![1627102698(1)](images/\1627102698(1).jpg)



#### 5-5https

0创建证书

 openssl req -x509 -sha256 -nodes -days 365 -newkey rsa:2048  -keyout cert.key -out cert.crt  -config  openssl.conf

1创建secret

kubectl create -n istio-system secret tls istio-ingressgateway-certs --key ./cert.key --cert=./cert.crt

kubectl exec deploy/istio-ingressgateway -n istio-system  -- ls /etc/istio/ingressgateway-certs

2创建gateway

gateway/gateway-https.yaml

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
      name: https
      protocol: HTTPS
    hosts:
    - "bookinfo.demo"
    - "ratings.demo"
    - "nginx.example.com"
    tls:
      mode: SIMPLE
      serverCertificate: /etc/istio/ingressgateway-certs/tls.crt
      privateKey: /etc/istio/ingressgateway-certs/tls.key
```

3创建vs

1.7.0/virtaulservice/vs-bookinfo-hosts-star.yaml 

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

4访问浏览器

![1627103656(1)](images/\1627103656(1).jpg)

![1627103739(1)](images/\1627103739(1).jpg)



#### 5-6tcp

0部署tcp echo

kubectl apply -f samples/tcp-echo/tcp-echo-services.yaml -n istio

1创建gateway

gateway/gateway-tcp.yaml

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
```

2创建vs dr

1.7.0/gateway/protocol/vs-dr-tcp-echo.yaml

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

3添加端口

kubectl edit svc istio-ingressgateway -n istio-system

4测试

telnet 10.68.12.164 31400

5改变vs subset

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
        subset: v2
```

6测试

telnet 10.68.12.164 31400

![1627105644(1)](images/\1627105644(1).jpg)

![1627105749(1)](images/\1627105749(1).jpg)



#### 5-7http2

1创建gateway

gateway/gateway-http2.yaml

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
      number: 15444
      name: http2
      protocol: HTTP2
      targetPort: 15444
    hosts:
    - "*"
```

2部署vs

1.7.0/virtaulservice/ vs-bookinfo-hosts-star.yaml 

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

3访问浏览器

```
curl -sk --http2  http://192.168.229.134:32385/productpage 
```

测试成功

![1627106226(1)](images/\1627106226(1).jpg)

![1627106284(1)](images/\1627106284(1).jpg)



#### 5-8mongo

0部署mongodb

bookinfo-db.yaml 

```
apiVersion: v1
kind: Service
metadata:
  name: mongodb
  labels:
    app: mongodb
    service: mongodb
spec:
  ports:
  - port: 27017
    name: mongo
  selector:
    app: mongodb
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: mongodb-v1
  labels:
    app: mongodb
    version: v1
spec:
  replicas: 1
  selector:
    matchLabels:
      app: mongodb
      version: v1
  template:
    metadata:
      labels:
        app: mongodb
        version: v1
      annotations:
        sidecar.istio.io/statsInclusionRegexps: ".*"
    spec:
      containers:
      - name: mongodb 
        image: docker.io/istio/examples-bookinfo-mongodb:1.16.2
        imagePullPolicy: IfNotPresent
        ports:
        - containerPort: 27017
        volumeMounts:
        - name: data-db
          mountPath: /data/db
      volumes:
      - name: data-db
        emptyDir: {}
---
```

kubectl apply -f bookinfo-db.yaml -n istio



部署statsd

deploy-statsd-influxdb-grafana.yaml

```
apiVersion: apps/v1
kind: Deployment
metadata:
  name: statsd-influxdb-grafana
spec:
  replicas: 1
  selector:
    matchLabels:
      app: statsd-influxdb-grafana
  template:
    metadata:
      labels:
        app: statsd-influxdb-grafana
    spec:
      containers:
      - name: statsd-influxdb-grafana
        image: samuelebistoletti/docker-statsd-influxdb-grafana:2.2.0
        ports:
        - name: grafana
          containerPort: 3003
        - name: influxdb-admin
          containerPort: 8888
        - name: influxdb
          containerPort: 8086
        - name: statsd
          containerPort: 8125
          protocol: UDP
---
apiVersion: v1
kind: Service
metadata:
  name: statsd-influxdb-grafana-svc
spec:
  ports:
  - name: http-grafana
    port: 3003
    targetPort: 3003
  - name: http-influxdb-admin
    port: 3004
    targetPort: 8888
  - name: tcp-influxdb
    port: 8086
    targetPort: 8086
  - name: udp-statsd
    port: 8125
    targetPort: 8125
    protocol: UDP
  selector:
    app: statsd-influxdb-grafana
```

gw,vs

gw-vs-statsd.yaml

```
apiVersion: networking.istio.io/v1beta1
kind: Gateway
metadata:
  name: statsd
spec:
  selector:
    istio: ingressgateway
  servers:
  - port:
      number: 80
      name: http
      protocol: HTTP
    hosts:
    - "demo.grafana.com"
    - "demo.influxdb.com"
---
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: statsd-grafana
spec:
  hosts:
    - "demo.grafana.com"
  gateways:
  - statsd
  http:
  - match:
    - uri:
        prefix: /
    route:
    - destination:
        host: statsd-influxdb-grafana-svc.istio.svc.cluster.local
        port:
          number: 3003
---
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: statsd-influxdb
spec:
  hosts:
    - "demo.influxdb.com"
  gateways:
  - statsd
  http:
  - match:
    - uri:
        prefix: /
    route:
    - destination:
        host: statsd-influxdb-grafana-svc.istio.svc.cluster.local
        port:
          number: 3004

```



效果：

![1](D:\\公众号\收费文章\不懂envoyfilter你敢说精通istio系列\17statistics\1.jpg)



![2](D:\\公众号\收费文章\不懂envoyfilter你敢说精通istio系列\17statistics\2.jpg)



grafana

账号密码：root/root



Add data source on Grafana

```
Url: http://localhost:8086
Database: telegraf
User: telegraf
Password: telegraf
```

influxdb portal

URL: [http://localhost:3004](http://localhost:3004/)
Username: root
Password: root
Port: 8086



```
  meshConfig:
      proxyMetadata:
        # Enable dynamic bootstrap generation
        # https://github.com/istio/istio/pull/33456
        BOOTSTRAP_XDS_AGENT: "true"
```

ef-statictis.yaml

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: statictis
spec:
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
                  address: 10.68.124.118
                  port_value: 8125
                  protocol: UDP
              prefix: statictis
```





1部署gateway

gateway/gateway-mongo.yaml

```
apiVersion: networking.istio.io/v1beta1
kind: Gateway
metadata:
  name: mongo
spec:
  selector:
    istio: ingressgateway
  servers:
  - port:
      number: 27017
      name: mongo
      protocol: MONGO
    hosts:
    - "*"
```

2部署vs

1.7.0/gateway/protocol/vs-mongodb.yaml 

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: mongo
spec:
  hosts:
  - "*"
  gateways:
  - mongo
  tcp:
  - match:
    - port: 27017
    route:
    - destination:
        host: mongodb.istio.svc.cluster.local
        port:
          number: 27017
```

3添加端口

kubectl edit svc istio-ingressgateway -n istio-system

4测试

mongo --host 192.168.198.154 --port 30150

![1627182880(1)](images/\1627182880(1).jpg)

![1627183141(1)](images/\1627183141(1).jpg)



#### 5-9tls

0创建证书

 openssl req -x509 -sha256 -nodes -days 365 -newkey rsa:2048  -keyout cert.key -out cert.crt  -config  openssl.conf

1创建secret

kubectl create -n istio-system secret tls istio-ingressgateway-certs --key ./cert.key --cert=./cert.crt

kubectl exec deploy/istio-ingressgateway -n istio-system  -- ls /etc/istio/ingressgateway-certs

1创建gateway

gateway/gateway-tls.yaml

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
      number: 15443
      name: tls
      protocol: TLS
    hosts:
    - "bookinfo.com"
    tls:
      mode: SIMPLE
      serverCertificate: /etc/istio/ingressgateway-certs/tls.crt
      privateKey: /etc/istio/ingressgateway-certs/tls.key
```

2创建vs

1.7.0/gateway/protocol/vs-tls-protocol-echo.yaml  

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: tcp-echo
spec:
  hosts:
  - "*"
  gateways:
  - bookinfo-gateway
  tcp:
  - match:
    - port: 15443
    route:
    - destination:
        host: tcp-echo
        port:
          number: 9000
```

3修改/etc/hosts

10.68.70.69 bookinfo.com

4测试

openssl s_client -connect bookinfo.com:15443 -servername bookinfo.com

![1627183247(1)](images/\1627183247(1).jpg)

![1627183316(1)](images/\1627183316(1).jpg)



#### 5-10mysql

0部署mysql

bookinfo-mysql.yaml 

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
      annotations:
        sidecar.istio.io/statsInclusionRegexps: ".*"
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

kubectl apply -f bookinfo-mysql.yaml -n istio



1创建gateway

gateway/gateway-mysql.yaml

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

2创建vs

gateway/protocol/vs-mysql.yaml

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

3新增端口

kubectl edit svc istio-ingressgateway -nistio-system

4istio启用mysql协议

kubectl set env deploy istiod -n istio-system PILOT_ENABLE_MYSQL_FILTER=true

5测试

 mysql -h 192.168.198.154 --port 37031 -uroot -p

![1627183491(1)](images/\1627183491(1).jpg)



![1627183541(1)](images/\1627183541(1).jpg)

#### 5-11redis

1创建gateway

gateway/gateway-redis.yaml

```
apiVersion: networking.istio.io/v1beta1
kind: Gateway
metadata:
  name: redis
spec:
  selector:
    istio: ingressgateway
  servers:
  - port:
      number: 6379
      name: redis
      protocol: REDIS
    hosts:
    - "*"
~
```

2部署redis

gateway/protocol/redis-deploy.yaml 

 kubectl apply -f redis-deploy.yaml -n istio

```
apiVersion: apps/v1
kind: Deployment
metadata:
  labels:
    app: bcia
    ms-name: redis
  name: bcia-redis
spec:
  replicas: 1
  selector:
    matchLabels:
      app: bcia
      ms-name: redis
  template:
    metadata:
      labels:
        app: bcia
        ms-name: redis
      name: bcia-redis
      annotations:
        sidecar.istio.io/statsInclusionRegexps: ".*"
    spec:
     containers:
     - name: bcia-redis
       image: redis:5.0.8
       command:
         - "redis-server"
---
apiVersion: v1
kind: Service
metadata:
  name: redis
spec:
  selector:
    app: bcia
    ms-name: redis
  ports:
  - port: 6379
    targetPort: 6379
```

3创建vs

gateway/protocol/vs-redis.yaml 

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: redis
spec:
  hosts:
  - "*"
  gateways:
  - redis
  tcp:
  - match:
    - port: 6379
    route:
    - destination:
        host: redis.istio.svc.cluster.local
        port:
          number: 6379
```

4新增端口

kubectl edit svc istio-ingressgateway -nistio-system

5istio启用redis协议

kubectl set env deploy istiod -n istio-system  PILOT_ENABLE_REDIS_FILTER=true

6测试

redis-cli -h 192.168.198.154 -p 29525

#### 

![1627183717(1)](images/\1627183717(1).jpg)



![1627183763(1)](images/\1627183763(1).jpg)

#### 5-12grpc-web

1deployment

gateway/protocol/web-grpc/web-grpc-deploy.yaml

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
---
```

2vs

gateway/protocol/web-grpc/vs-web-grpc-client.yaml

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

gateway/protocol/web-grpc/vs-web-grpc.yaml

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
      - regex: ".*"
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

3gateway

gateway/protocol/web-grpc/gateway-grpc-web.yaml

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
      name: grpc-web
      protocol: GRPC-WEB
      targetPort: 80
    hosts:
    - "*"
```

4访问

http://192.168.198.154:30986/



![1627184200(1)](images/\1627184200(1).jpg)

![1627184267(1)](images/\1627184267(1).jpg)



### tls

| Field                 | Type        | Description                                                  | Required |
| --------------------- | ----------- | ------------------------------------------------------------ | -------- |
| httpsRedirect         | bool        | 是否要做 HTTP 重定向                                         | 否       |
| mode                  | TLSmode     | 在配置的外部端口上使用 TLS 服务时，可以取 PASSTHROUGH、SIMPLE、MUTUAL、AUTO_PASSTHROUGH 这 4 种模式 | 否       |
| serverCertificate     | string      | 服务端证书的路径。当模式是 SIMPLE 和 MUTUAL 时必须指定       | 否       |
| privateKey            | string      | 服务端密钥的路径。当模式是 SIMPLE 和 MUTUAL 时必须指定       | 否       |
| caCertificates        | string      | CA 证书路径。当模式是 MUTUAL 时指定                          | 否       |
| credentialName        | string      | 用于唯一标识服务端证书和秘钥。Gateway 使用 credentialName从远端的凭证存储中获取证书和秘钥，而不是使用 Mount 的文件 | 否       |
| subjectAltNames       | string[]    | SAN 列表，SubjectAltName 允许一个证书指定多个域名            | 否       |
| verifyCertificateSpki | string[]    | 授权客户端证书的SKPI的base64编码的SHA-256哈希值的可选列表    | 否       |
| verifyCertificateHash | string[]    | 授权客户端证书的十六进制编码SHA-256哈希值的可选列表          | 否       |
| minProtocolVersion    | TLSProtocol | TLS 协议的最小版本                                           | 否       |
| maxProtocolVersion    | TLSProtocol | TLS 协议的最大版本                                           | 否       |
| cipherSuites          | string[]    | 指定的加密套件，默认使用 Envoy 支持的加密套件                | 否       |

#### 5-13httpsRedirect

0创建证书

 openssl req -x509 -sha256 -nodes -days 365 -newkey rsa:2048  -keyout cert.key -out cert.crt  -config  openssl.conf

1创建secret

kubectl create -n istio-system secret tls istio-ingressgateway-certs --key ./cert.key --cert=./cert.crt

kubectl exec deploy/istio-ingressgateway -n istio-system  -- ls /etc/istio/ingressgateway-certs

gateway/tls/gw-httpsRedirect.yaml

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
    tls:
      httpsRedirect: true
  - port:
      number: 443
      name: https-443
      protocol: HTTPS
    hosts:
    - "*"
    tls:
      mode: SIMPLE
      serverCertificate: /etc/istio/ingressgateway-certs/tls.crt
      privateKey: /etc/istio/ingressgateway-certs/tls.key
```

wget http://bookinfo.com:80/productpage --no-check-certificate

编辑istio-ingressgateway svc 添加externalIPs  192.168.198.199

测试访问浏览器

http://192.168.198.199/productpage

端口没法转

![1627185801(1)](images/\1627185801(1).jpg)



![1627185734(1)](images/\1627185734(1).jpg)

![1627186092](images/\1627186092.jpg)



#### 5-14mode

| Name             | Description                                                  |
| ---------------- | ------------------------------------------------------------ |
| PASSTHROUGH      | 客户端提供的SNI字符串将用作VirtualService TLS路由中的匹配条件，以根据服务注册表确定目标服务 |
| SIMPLE           | 使用标准TLS语义的安全连接                                    |
| MUTUAL           | 通过提供服务器证书进行身份验证，使用双边TLS来保护与下游的连接 |
| AUTO_PASSTHROUGH | 与直通模式相似，不同之处在于具有此TLS模式的服务器不需要关联的VirtualService即可从SNI值映射到注册表中的服务。目标详细信息（例如服务/子集/端口）被编码在SNI值中。代理将转发到SNI值指定的上游（Envoy）群集（一组端点）。 |
| ISTIO_MUTUAL     | 通过提供用于身份验证的服务器证书，使用相互TLS使用来自下游的安全连接 |

##### PASSTHROUGH

1创建证书

 openssl req -x509 -sha256 -nodes -days 365 -newkey rsa:2048 -subj '/O=example Inc./CN=example.com' -keyout example.com.key -out example.com.crt 

 openssl req -out nginx.example.com.csr -newkey rsa:2048 -nodes -keyout nginx.example.com.key -subj "/CN=nginx.example.com/O=some organization" 

 openssl x509 -req -days 365 -CA example.com.crt -CAkey example.com.key -set_serial 0 -in nginx.example.com.csr -out nginx.example.com.crt 

2创建secret

 kubectl create secret tls nginx-server-certs --key nginx.example.com.key --cert nginx.example.com.crt  -n istio

3创建nginx配置文件

gateway/tls/passthrough/nginx.conf

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

gateway/tls/passthrough/nginx-deploy.yaml

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

gateway/tls/passthrough/gw-mode-passthrough.yaml 

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

gateway/tls/passthrough/vs-nginx.yaml

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
```

第二种实现方法：(不行)

创建vs

gateway/tls/passthrough/vs-nginx-tcp.yaml

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
  tcp:
  - match:
    - port: 443
    route:
    - destination:
        host: my-nginx
        port:
          number: 443
```





7访问url

 https://nginx.example.com:39329/ 

![1627190483(1)](images/\1627190483(1).jpg)



![1627190543(1)](images/\1627190543(1).jpg)

**没有证书是passthough**

##### SIMPLE

1创建gateway

1.7.0/gateway/tls/simple/gateway-simple.yaml

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
      name: https
      protocol: HTTPS
    hosts:
    - "bookinfo.demo"
    - "ratings.demo"
    - "nginx.example.com"
    tls:
      mode: SIMPLE
      serverCertificate: /etc/istio/ingressgateway-certs/tls.crt
      privateKey: /etc/istio/ingressgateway-certs/tls.key
```

2创建vs

1.7.0/virtaulservice/vs-bookinfo-hosts-star.yaml

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

3访问 https://bookinfo.demo:39329/productpage 

![1627194952(1)](images/\1627194952(1).jpg)

![1627195019(1)](images/\1627195019(1).jpg)



##### MUTUAL

###### mutual-1

1创建证书

openssl req -x509 -sha256 -nodes -days 365 -newkey rsa:2048 -subj '/O=example Inc./CN=example.com' -keyout example.com.key -out example.com.crt 

 openssl req -out bookinfo.example.com.csr -newkey rsa:2048 -nodes -keyout bookinfo.example.com.key -subj "/CN=bookinfo.example.com/O=some organization"  

 openssl x509 -req -days 365 -CA example.com.crt -CAkey example.com.key -set_serial 0 -in bookinfo.example.com.csr -out bookinfo.example.com.crt 

2创建secret

 kubectl create -n istio-system   secret generic  bookinfo-credential --from-file=tls.key=bookinfo.example.com.key   --from-file=tls.crt=bookinfo.example.com.crt --from-file=ca.crt=example.com.crt 

3创建gateway

1.7.0/gateway/tls/mutual/gateway-mutual.yaml

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
     name: https
     protocol: HTTPS
   tls:
     mode: MUTUAL
     credentialName: bookinfo-credential 
   hosts:
   - bookinfo.example.com
```

4创建vs

1.7.0/virtaulservice/vs-bookinfo-hosts-star.yaml

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

5生成客户端证书

 openssl req -out client.example.com.csr -newkey rsa:2048 -nodes -keyout client.example.com.key -subj "/CN=client.example.com/O=client organization" 

  openssl x509 -req -days 365 -CA example.com.crt -CAkey example.com.key -set_serial 1 -in client.example.com.csr -out client.example.com.crt 

6访问

 curl -v -HHost:bookinfo.example.com --resolve "bookinfo.example.com:39329:192.168.198.154"  --cacert ./example.com.crt --cert ./client.example.com.crt --key ./client.example.com.key  "https://bookinfo.example.com:39329/productpage" 

![1627195421(1)](images/\1627195421(1).jpg)



![1627195527(1)](images/\1627195527(1).jpg)

###### mutual-2

1生成证书

openssl req -x509 -sha256 -nodes -days 365 -newkey rsa:2048 -subj '/O=example Inc./CN=example.com' -keyout example.com.key -out example.com.crt 

 openssl req -out bookinfo.example.com.csr -newkey rsa:2048 -nodes -keyout bookinfo.example.com.key -subj "/CN=bookinfo.example.com/O=some organization" 

 openssl x509 -req -days 365 -CA example.com.crt -CAkey example.com.key -set_serial 0 -in bookinfo.example.com.csr -out bookinfo.example.com.crt 

2创建secret

kubectl create -n istio-system   secret generic istio-ingressgateway-certs  --from-file=tls.key=bookinfo.example.com.key   --from-file=tls.crt=bookinfo.example.com.crt --from-file=ca.crt=example.com.crt

检查配置是否生效：

kubectl exec deploy/istio-ingressgateway -n istio-system  -- ls /etc/istio/ingressgateway-certs

3创建gateway

1.7.0/gateway/tls/gateway-caCertificates.yaml

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
      name: https
      protocol: HTTPS
    hosts:
    - "*"
    tls:
      mode: MUTUAL
      caCertificates: /etc/istio/ingressgateway-certs/ca.crt
      serverCertificate: /etc/istio/ingressgateway-certs/tls.crt
      privateKey: /etc/istio/ingressgateway-certs/tls.key
```

4创建vs

1.7.0/virtaulservice/vs-bookinfo-hosts-star.yaml

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

5生成客户端证书

 openssl req -out client.example.com.csr -newkey rsa:2048 -nodes -keyout client.example.com.key -subj "/CN=client.example.com/O=client organization" 

  openssl x509 -req -days 365 -CA example.com.crt -CAkey example.com.key -set_serial 1 -in client.example.com.csr -out client.example.com.crt 

6访问

 curl -v -HHost:bookinfo.example.com --resolve "bookinfo.example.com:39329:192.168.198.154"  --cacert ./example.com.crt --cert ./client.example.com.crt --key ./client.example.com.key  "https://bookinfo.example.com:39329/productpage" 



##### AUTO_PASSTHROUGH

通常来说，istio ingress gateway 需要配套指定服务的 VirtualService，用以指定 ingress 流量的后端服务。但在「多网络模式」中，该 ingress gateway 需要作为本数据面所有服务的流量入口。也就是所有服务共享单个 ingress gateway (单个 IP)，这里其实是利用了 TLS 中的 SNI(Server Name Indication)[1]。

传统的 ingress gateway 承载的是南北流量 (server-client)，这里的 ingress gateway 属于网格内部流量，承载的是东西流量 (server-server)。设置 AUTO_PASSTHROUGH，可以允许服务无需配置 VirtualService，而直接使用 TLS 中的 SNI 值来表示 upstream，服务相关的 service/subset/port 都可以编码到 SNI 内容中。

![arch (1)](images/\arch (1).jpg)




主要用于多k8s集群

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
      name: tls
      protocol: TLS
    tls:
      mode: AUTO_PASSTHROUGH
    hosts:
    - "*.local"
```

部署多集群

```
mkdir -p certs
 make -f ../tools/certs/Makefile.selfsigned.mk root-ca
 make -f ../tools/certs/Makefile.selfsigned.mk cluster1-cacerts
 make -f ../tools/certs/Makefile.selfsigned.mk cluster2-cacerts
 scp
 kubectl create namespace istio-system
 kubectl create secret generic cacerts -n istio-system \\
      --from-file=cluster1/ca-cert.pem \\
      --from-file=cluster1/ca-key.pem \\
      --from-file=cluster1/root-cert.pem \\
      --from-file=cluster1/cert-chain.pem
 kubectl create secret generic cacerts -n istio-system \\
      --from-file=cluster2/ca-cert.pem \\
      --from-file=cluster2/ca-key.pem \\
      --from-file=cluster2/root-cert.pem \\
      --from-file=cluster2/cert-chain.pem
```

```
cluster1:
 kubectl  label namespace istio-system topology.istio.io/network=network1
 
 cat <<EOF > cluster1.yaml
apiVersion: install.istio.io/v1alpha1
kind: IstioOperator
spec:
  values:
    global:
      meshID: mesh1
      multiCluster:
        clusterName: cluster1
      network: network1
EOF

istioctl install  -f cluster1.yaml

samples/multicluster/gen-eastwest-gateway.sh --mesh mesh1 --cluster cluster1 --network network1 | istioctl  install -y -f -
添加externalIP
kubectl edit svc -n istio-system istio-eastwestgateway
  externalIPs:
  - 192.168.229.100
 kubectl  apply -n istio-system -f samples/multicluster/expose-services.yaml
 
 
 cluster2:
 kubectl  label namespace istio-system topology.istio.io/network=network2
 
 cluster1:
 cat <<EOF > cluster2.yaml
apiVersion: install.istio.io/v1alpha1
kind: IstioOperator
spec:
  values:
    global:
      meshID: mesh1
      multiCluster:
        clusterName: cluster2
      network: network2
EOF

scp cluster2.yaml root@192.168.229.131:/root

cluster2:
 istioctl install -f cluster2.yaml
 samples/multicluster/gen-eastwest-gateway.sh --mesh mesh1 --cluster cluster2 --network network2 |  istioctl install -y -f -
 添加externalIp
 kubectl edit svc -n istio-system istio-eastwestgateway
   externalIPs:
  - 192.168.229.101
 kubectl  apply -n istio-system -f samples/multicluster/expose-services.yaml
 
 cluster1:
 istioctl x create-remote-secret --name=cluster1  --server=https://192.168.229.128:6443 > remote-secret-cluster1.yaml
scp remote-secret-cluster1.yaml root@192.168.229.131:/root

cluster2
 kubectl apply -f remote-secret-cluster1.yaml
 istioctl x create-remote-secret --name=cluster2  --server=https://192.168.229.131:6443 > remote-secret-cluster2.yaml
 scp remote-secret-cluster2.yaml root@192.168.229.128:/root
 
 cluster1
  kubectl apply -f remote-secret-cluster2.yaml
  
  验证
  kubectl exec  -n istio   "$(kubectl get pod  -n istio -l app=ratings -o jsonpath='{.items[0].metadata.name}')"  -- curl productpage.istio:9080/productpage
```

```
[root@node01 ~]# kubectl get gw -n istio-system  cross-network-gateway  -o yaml
apiVersion: networking.istio.io/v1beta1
kind: Gateway
metadata:
  annotations:
    kubectl.kubernetes.io/last-applied-configuration: |
      {"apiVersion":"networking.istio.io/v1alpha3","kind":"Gateway","metadata":{"annotations":{},"name":"cross-network-gateway","namespace":"istio-system"},"spec":{"selector":{"istio":"eastwestgateway"},"servers":[{"hosts":["*.local"],"port":{"name":"tls","number":15443,"protocol":"TLS"},"tls":{"mode":"AUTO_PASSTHROUGH"}}]}}
  creationTimestamp: "2021-09-07T05:30:44Z"
  generation: 1
  name: cross-network-gateway
  namespace: istio-system
  resourceVersion: "168128"
  uid: b32a2db5-8705-44f7-b548-fa900273b4ee
spec:
  selector:
    istio: eastwestgateway
  servers:
  - hosts:
    - '*.local'
    port:
      name: tls
      number: 15443
      protocol: TLS
    tls:
      mode: AUTO_PASSTHROUGH
```



##### ISTIO_MUTUAL

1创建证书

openssl req -x509 -sha256 -nodes -days 365 -newkey rsa:2048 -subj '/O=example Inc./CN=example.com' -keyout example.com.key -out example.com.crt 

 openssl req -out bookinfo.example.com.csr -newkey rsa:2048 -nodes -keyout bookinfo.example.com.key -subj "/CN=bookinfo.example.com/O=some organization" 

 openssl x509 -req -days 365 -CA example.com.crt -CAkey example.com.key -set_serial 0 -in bookinfo.example.com.csr -out bookinfo.example.com.crt 

2创建secret

 kubectl create -n istio-system   secret generic  bookinfo-credential --from-file=tls.key=bookinfo.example.com.key   --from-file=tls.crt=bookinfo.example.com.crt --from-file=ca.crt=example.com.crt 

3创建gateway

1.7.0/gateway/tls/istio-mutual/gateway-istio-mutual.yaml

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
      name: https
      protocol: HTTPS
    tls:
      mode: ISTIO_MUTUAL
      credentialName: "bookinfo-credential"
    hosts:
    - "*"
```

4创建vs

1.7.0/virtaulservice/vs-bookinfo-hosts-star.yaml

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

5访问

curl -v -HHost:bookinfo.example.com --resolve "bookinfo.example.com:39329:192.168.198.154"  --cacert example.com.crt  "https://bookinfo.example.com:39329/productpage"

![1627196479(1)](images/\1627196479(1).jpg)



#### 5-15credentialName

1创建secret

 cd 1.7.0/gateway/certs

kubectl create -n istio-system secret tls bookinfo-secret --key ./cert.key --cert=./cert.crt

2创建gateway

kubectl apply -f gateway-credentialName.yaml -n istio

1.7.0/gateway/tls/gateway-credentialName.yaml 

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
      name: https
      protocol: HTTPS
    hosts:
    - "*"
    tls:
      credentialName: bookinfo-secret
      mode: SIMPLE
```

3创建vs

1.7.0/virtaulservice/vs-bookinfo-hosts-star.yaml

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

4访问 https://bookinfo.demo:39329/productpage 

![1627274201](images/\1627274201.jpg)



![1627274270(1)](images/\1627274270(1).jpg)

#### 5-16caCertificates

1生成证书

openssl req -x509 -sha256 -nodes -days 365 -newkey rsa:2048 -subj '/O=example Inc./CN=example.com' -keyout example.com.key -out example.com.crt 

 openssl req -out bookinfo.example.com.csr -newkey rsa:2048 -nodes -keyout bookinfo.example.com.key -subj "/CN=bookinfo.example.com/O=some organization" 

 openssl x509 -req -days 365 -CA example.com.crt -CAkey example.com.key -set_serial 0 -in bookinfo.example.com.csr -out bookinfo.example.com.crt 

2创建secret

kubectl create -n istio-system   secret generic istio-ingressgateway-certs  --from-file=tls.key=bookinfo.example.com.key   --from-file=tls.crt=bookinfo.example.com.crt --from-file=ca.crt=example.com.crt

检查配置是否生效：

kubectl exec deploy/istio-ingressgateway -n istio-system  -- ls /etc/istio/ingressgateway-certs

3创建gateway

1.7.0/gateway/tls/gateway-caCertificates.yaml

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
      name: https
      protocol: HTTPS
    hosts:
    - "*"
    tls:
      mode: MUTUAL
      caCertificates: /etc/istio/ingressgateway-certs/ca.crt
      serverCertificate: /etc/istio/ingressgateway-certs/tls.crt
      privateKey: /etc/istio/ingressgateway-certs/tls.key
```

4创建vs

1.7.0/virtaulservice/vs-bookinfo-hosts-star.yaml

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

5生成客户端证书

 openssl req -out client.example.com.csr -newkey rsa:2048 -nodes -keyout client.example.com.key -subj "/CN=client.example.com/O=client organization" 

  openssl x509 -req -days 365 -CA example.com.crt -CAkey example.com.key -set_serial 1 -in client.example.com.csr -out client.example.com.crt 

6访问

 curl -v -HHost:bookinfo.example.com --resolve "bookinfo.example.com:39329:192.168.198.154"  --cacert ./example.com.crt --cert ./client.example.com.crt --key ./client.example.com.key  "https://bookinfo.example.com:39329/productpage" 

![1627274674(1)](images/\1627274674(1).jpg)

![1627274717(1)](images/\1627274717(1).jpg)



#### 5-17cipherSuites

部署gateway

1.7.0/gateway/tls/gateway-cipherSuites.yaml

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
      name: https
      protocol: HTTPS
    hosts:
    - "bookinfo.demo"
    - "ratings.demo"
    - "nginx.example.com"
    tls:
      mode: SIMPLE
      cipherSuites: 
      - ECDHE-RSA-AES256-GCM-SHA384
      - ECDHE-RSA-AES128-GCM-SHA256
      credentialName: bookinfo-secret
```

加密算法类型

- CDHE-ECDSA-AES256-GCM-SHA384

- ECDHE-RSA-AES256-GCM-SHA384

- ECDHE-ECDSA-AES128-GCM-SHA256

- ECDHE-RSA-AES128-GCM-SHA256

- AES256-GCM-SHA384

- AES128-GCM-SHA256

  

  openssl s_client -connect 192.168.198.154:30030 -servername bookinfo.demo -tls1_2
  
  



![1627274924(1)](images/\1627274924(1).jpg)



#### 5-18minProtocolVersion maxProtocolVersion

| Name     | Description     |
| -------- | --------------- |
| TLS_AUTO | 自动选择DLS版本 |
| TLSV1_0  | TLS 1.0         |
| TLSV1_1  | TLS 1.1         |
| TLSV1_2  | TLS 1.2         |
| TLSV1_3  | TLS 1.3         |

![1617595703103](images/\1617595703103.png)

##### TLS_AUTO

1.7.0/gateway/tls/protocolVersion/gateway-tls-version-tls_auto.yaml

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
      name: https
      protocol: HTTPS
    hosts:
    - "*"
    tls:
      credentialName: bookinfo-secret
      mode: SIMPLE
      minProtocolVersion: TLS_AUTO
      maxProtocolVersion: TLS_AUTO
```

![1627275321(1)](images/\1627275321(1).jpg)



##### TLSV1_0

1.7.0/gateway/tls/protocolVersion/gateway-tls-version-tlsv1_0.yaml

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
      name: https
      protocol: HTTPS
    hosts:
    - "*"
    tls:
      credentialName: bookinfo-secret
      mode: SIMPLE
      minProtocolVersion: TLSV1_0
      maxProtocolVersion: TLSV1_0
```

![1617596315271](images/\1617596315271.png)

tls1.0被废弃



![1627275428(1)](images/\1627275428(1).jpg)

##### TLSV1_0  - TLSV1_3

gateway-tls-version-tlsv1_0-tlsv1_3.yaml

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
      name: https
      protocol: HTTPS
    hosts:
    - "*"
    tls:
      credentialName: bookinfo-secret
      mode: SIMPLE
      minProtocolVersion: TLSV1_0
      maxProtocolVersion: TLSV1_3
```

![1627275507(1)](images/\1627275507(1).jpg)

![1627275543(1)](images/\1627275543(1).jpg)





#### 5-19subjectAltNames

1创建证书

openssl req -x509 -sha256 -nodes -days 365 -newkey rsa:2048 -subj '/O=example Inc./CN=example.com' -keyout example.com.key -out example.com.crt 

 openssl req -out bookinfo.example.com.csr -newkey rsa:2048 -nodes -keyout bookinfo.example.com.key -subj "/CN=bookinfo.example.com/O=some organization"  -config  openssl.cnf 

 openssl x509 -req -days 365 -CA example.com.crt -CAkey example.com.key -set_serial 0 -in bookinfo.example.com.csr -out bookinfo.example.com.crt    -extensions  v3_req  -extfile  openssl.cnf 

 

 

```
[ alt_names ]
DNS.1 = bookinfo.example.com
DNS.2 = bookinfo.com
DNS.3 = bookinfo.demo
```



2创建secret

 kubectl create -n istio-system   secret generic  bookinfo-credential --from-file=tls.key=bookinfo.example.com.key   --from-file=tls.crt=bookinfo.example.com.crt

3创建gateway

gateway-subjectAltNames.yaml

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
     name: https
     protocol: HTTPS
   tls:
     mode: SIMPLE
     credentialName: bookinfo-credential
     subjectAltNames:
     - bookinfo.example.com
     - bookinfo.com
     - bookinfo.demo
   hosts:
   - bookinfo.example.com
   - bookinfo.com
   - bookinfo.demo

```



4访问

https://bookinfo.example.com:37000/productpage

https://bookinfo.demo:37000/productpage

https://bookinfo.com:37000/productpage





![1627275816(1)](images/\1627275816(1).jpg)

## 6http3案例

![1648250942(1)](images/\1648250942(1).jpg)



把443端口改为udp协议，使用nodeport类型service

```
          - name: http3
            port: 443
            targetPort: 8443
            protocol: UDP
```

设置env

```
kubectl set env -n istio-system deploy istiod PILOT_ENABLE_QUIC_LISTENERS=true
kubectl rollout restart deploy -n istio-system istiod
```

部署httpbin

```
kubectl create namespace httpbin
kubectl label namespace httpbin istio-injection=enabled
kubectl -n httpbin apply -f samples/httpbin/httpbin.yaml
```

创建secret

httpbin.cnf

```
[req]
default_bits       = 2048
prompt             = no
distinguished_name = req_distinguished_name
req_extensions     = san_reqext

[ req_distinguished_name ]
countryName         = IN
stateOrProvinceName = KA
organizationName    = QuicCorp

[ san_reqext ]
subjectAltName      = @alt_names

[alt_names]
DNS.0   = httpbin.quic-corp.com
```

```
openssl req -x509 -sha256 -nodes -days 365 -newkey rsa:4096 -subj "/C=IN/ST=KA/O=QuicCorp" -keyout quiccorp-ca.key -out quiccorp-ca.crt

openssl req -out httpbin.csr -newkey rsa:2048 -nodes -keyout httpbin.key -config httpbin.cnf

openssl x509 -req -days 365 -CA quiccorp-ca.crt -CAkey quiccorp-ca.key -set_serial 0 -in httpbin.csr -out httpbin.crt -extfile httpbin.cnf -extensions san_reqext

```

```
kubectl -n istio-system create secret tls httpbin-cred --key=httpbin.key --cert=httpbin.crt
```

创建gateway

gw-httpbin.yaml

```
apiVersion: networking.istio.io/v1beta1
kind: Gateway
metadata:
  name: httpbin-gateway
spec:
  selector:
    app: istio-ingressgateway
    istio: ingressgateway
  servers:
  - port:
      number: 443
      name: https
      protocol: HTTPS
    hosts:
    - httpbin.quic-corp.com
    tls:
      mode: SIMPLE
      credentialName: httpbin-cred
```

创建vs

vs-httpbin.yaml

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: httpbin-route
spec:
  hosts:
  - "*"
  gateways:
  - httpbin-gateway
  http:
  - name: httpbin-default-route
    route:
    - destination:
        host: httpbin.httpbin.svc.cluster.local
        port: 
          number: 8000
```



显示listener配置

```
[root@node01 http3]#  istioctl proxy-config listeners istio-ingressgateway-78b99cd5fb-fgx7q.istio-system
ADDRESS PORT  MATCH                      DESTINATION
0.0.0.0 8443  SNI: httpbin.quic-corp.com Route: https.443.https.httpbin-gateway.httpbin
0.0.0.0 8443  SNI: httpbin.quic-corp.com Route: https.443.https.httpbin-gateway.httpbin
0.0.0.0 15021 ALL                        Inline Route: /healthz/ready*
0.0.0.0 15090 ALL                        Inline Route: /stats/prometheus*
```



测试

```
INGRESS_IP=192.168.229.128
curl -svk --http2 --resolve httpbin.quic-corp.com:31805:192.168.229.128 https://httpbin.quic-corp.com:31805/headers


[root@node01 http3]# curl -svk --http2 --resolve httpbin.quic-corp.com:31805:192.168.229.128 https://httpbin.quic-corp.com:31805/headers
* Added httpbin.quic-corp.com:31805:192.168.229.128 to DNS cache
* Hostname httpbin.quic-corp.com was found in DNS cache
*   Trying 192.168.229.128...
* TCP_NODELAY set
* Connected to httpbin.quic-corp.com (192.168.229.128) port 31805 (#0)
* ALPN, offering h2
* ALPN, offering http/1.1
* successfully set certificate verify locations:
*   CAfile: /etc/pki/tls/certs/ca-bundle.crt
  CApath: none
* TLSv1.3 (OUT), TLS handshake, Client hello (1):
* TLSv1.3 (IN), TLS handshake, Server hello (2):
* TLSv1.3 (IN), TLS handshake, [no content] (0):
* TLSv1.3 (IN), TLS handshake, Encrypted Extensions (8):
* TLSv1.3 (IN), TLS handshake, Certificate (11):
* TLSv1.3 (IN), TLS handshake, CERT verify (15):
* TLSv1.3 (IN), TLS handshake, Finished (20):
* TLSv1.3 (OUT), TLS change cipher, Change cipher spec (1):
* TLSv1.3 (OUT), TLS handshake, [no content] (0):
* TLSv1.3 (OUT), TLS handshake, Finished (20):
* SSL connection using TLSv1.3 / TLS_AES_256_GCM_SHA384
* ALPN, server accepted to use h2
* Server certificate:
*  subject: C=IN; ST=KA; O=QuicCorp
*  start date: Mar 24 07:43:39 2022 GMT
*  expire date: Mar 24 07:43:39 2023 GMT
*  issuer: C=IN; ST=KA; O=QuicCorp
*  SSL certificate verify result: self signed certificate (18), continuing anyway.
* Using HTTP2, server supports multi-use
* Connection state changed (HTTP/2 confirmed)
* Copying HTTP/2 data in stream buffer to connection buffer after upgrade: len=0
* TLSv1.3 (OUT), TLS app data, [no content] (0):
* TLSv1.3 (OUT), TLS app data, [no content] (0):
* TLSv1.3 (OUT), TLS app data, [no content] (0):
* Using Stream ID: 1 (easy handle 0x5621a15454a0)
* TLSv1.3 (OUT), TLS app data, [no content] (0):
> GET /headers HTTP/2
> Host: httpbin.quic-corp.com:31805
> User-Agent: curl/7.61.1
> Accept: */*
> 
* TLSv1.3 (IN), TLS handshake, [no content] (0):
* TLSv1.3 (IN), TLS handshake, Newsession Ticket (4):
* TLSv1.3 (IN), TLS handshake, Newsession Ticket (4):
* TLSv1.3 (IN), TLS app data, [no content] (0):
* Connection state changed (MAX_CONCURRENT_STREAMS == 2147483647)!
* TLSv1.3 (OUT), TLS app data, [no content] (0):
* TLSv1.3 (IN), TLS app data, [no content] (0):
< HTTP/2 200 
< server: istio-envoy
< date: Thu, 24 Mar 2022 09:07:36 GMT
< content-type: application/json
< content-length: 607
< access-control-allow-origin: *
< access-control-allow-credentials: true
< x-envoy-upstream-service-time: 20
< alt-svc: h3=":443"; ma=86400
< 
{
  "headers": {
    "Accept": "*/*", 
    "Host": "httpbin.quic-corp.com:31805", 
    "User-Agent": "curl/7.61.1", 
    "X-B3-Parentspanid": "0a5359cd0f3e5983", 
    "X-B3-Sampled": "1", 
    "X-B3-Spanid": "99bee14f8f0526b0", 
    "X-B3-Traceid": "9a3ce75f175273360a5359cd0f3e5983", 
    "X-Envoy-Attempt-Count": "1", 
    "X-Envoy-Internal": "true", 
    "X-Forwarded-Client-Cert": "By=spiffe://cluster.local/ns/httpbin/sa/httpbin;Hash=4ee32a2457c6acdc280580b014dce248d7af449724d817dd34ef265d2a259871;Subject=\\"\";URI=spiffe://cluster.local/ns/istio-system/sa/istio-ingressgateway-service-account"
  }
}
* Connection #0 to host httpbin.quic-corp.com left intact

```



![1648113510(1)](images/\1648113510(1).jpg)



总结，启用http3后，会同时生成http2和http3的服务器功能。
