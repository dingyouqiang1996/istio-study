# 学习目标

![1618284294(1)](images\1618284294(1).jpg)

# 8-1什么是ServiceEntry

使用服务条目资源（Service Entries）可以将条目添加到 Istio 内部维护的服务注册表中。添加服务条目后，Envoy 代理可以将流量发送到该服务，就好像该服务条目是网格中的服务一样。通过配置服务条目，可以管理在网格外部运行的服务的流量。

  此外，可以配置虚拟服务和目标规则，以更精细的方式控制到服务条目的流量，就像为网格中的其他任何服务配置流量一样。

# 资源详解

| Field              | Type               | Description                                                  | Required |
| ------------------ | ------------------ | ------------------------------------------------------------ | -------- |
| `hosts`            | `string[]`         | The hosts associated with the ServiceEntry. Could be a DNS name with wildcard prefix.The hosts field is used to select matching hosts in VirtualServices and DestinationRules.For HTTP traffic the HTTP Host/Authority header will be matched against the hosts field.For HTTPs or TLS traffic containing Server Name Indication (SNI), the SNI value will be matched against the hosts field.**NOTE 1:** When resolution is set to type DNS and no endpoints are specified, the host field will be used as the DNS name of the endpoint to route traffic to.**NOTE 2:** If the hostname matches with the name of a service from another service registry such as Kubernetes that also supplies its own set of endpoints, the ServiceEntry will be treated as a decorator of the existing Kubernetes service. Properties in the service entry will be added to the Kubernetes service if applicable. Currently, the only the following additional properties will be considered by `istiod`:subjectAltNames: In addition to verifying the SANs of the service accounts associated with the pods of the service, the SANs specified here will also be verified. | Yes      |
| `addresses`        | `string[]`         | The virtual IP addresses associated with the service. Could be CIDR prefix. For HTTP traffic, generated route configurations will include http route domains for both the `addresses` and `hosts` field values and the destination will be identified based on the HTTP Host/Authority header. If one or more IP addresses are specified, the incoming traffic will be identified as belonging to this service if the destination IP matches the IP/CIDRs specified in the addresses field. If the Addresses field is empty, traffic will be identified solely based on the destination port. In such scenarios, the port on which the service is being accessed must not be shared by any other service in the mesh. In other words, the sidecar will behave as a simple TCP proxy, forwarding incoming traffic on a specified port to the specified destination endpoint IP/host. Unix domain socket addresses are not supported in this field. | No       |
| `ports`            | `Port[]`           | The ports associated with the external service. If the Endpoints are Unix domain socket addresses, there must be exactly one port. | Yes      |
| `location`         | `Location`         | Specify whether the service should be considered external to the mesh or part of the mesh. | No       |
| `resolution`       | `Resolution`       | Service discovery mode for the hosts. Care must be taken when setting the resolution mode to NONE for a TCP port without accompanying IP addresses. In such cases, traffic to any IP on said port will be allowed (i.e. `0.0.0.0:`). | Yes      |
| `endpoints`        | `WorkloadEntry[]`  | One or more endpoints associated with the service. Only one of `endpoints` or `workloadSelector` can be specified. | No       |
| `workloadSelector` | `WorkloadSelector` | Applicable only for MESH_INTERNAL services. Only one of `endpoints` or `workloadSelector` can be specified. Selects one or more Kubernetes pods or VM workloads (specified using `WorkloadEntry`) based on their labels. The `WorkloadEntry` object representing the VMs should be defined in the same namespace as the ServiceEntry. | No       |
| `exportTo`         | `string[]`         | A list of namespaces to which this service is exported. Exporting a service allows it to be used by sidecars, gateways and virtual services defined in other namespaces. This feature provides a mechanism for service owners and mesh administrators to control the visibility of services across namespace boundaries.If no namespaces are specified then the service is exported to all namespaces by default.The value “.” is reserved and defines an export to the same namespace that the service is declared in. Similarly the value “*” is reserved and defines an export to all namespaces.For a Kubernetes Service, the equivalent effect can be achieved by setting the annotation “networking.istio.io/exportTo” to a comma-separated list of namespace names. | No       |
| `subjectAltNames`  | `string[]`         | If specified, the proxy will verify that the server certificate’s subject alternate name matches one of the specified values.NOTE: When using the workloadEntry with workloadSelectors, the service account specified in the workloadEntry will also be used to derive the additional subject alternate names that should be verified. | No       |

## `8-2exportTo`

### 1当前名称空间

1部署sleep

serviceentries/sleep.yaml 

kubectl apply -f sleep.yaml  -n istio

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

2修改默认访问策略

mesh下面

    outboundTrafficPolicy: 
      mode: REGISTRY_ONLY
重启pod istiod使之生效

2应用serviceentry

serviceentries/se-baidu-dot.yaml

kubectl apply -f se-baidu-dot.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: ServiceEntry
metadata:
  name: baidu
spec:
  exportTo: 
  - "."
  hosts:
  - "www.baidu.com"
  ports:
  - number: 80
    name: http
    protocol: HTTP
  location: MESH_EXTERNAL
  resolution: DNS
```

 kubectl exec -it -n istio sleep-557747455f-flnsw /bin/sh

curl www.baidu.com

清理：

kubectl delete -f sleep.yaml  -n istio

kubectl  delete se baidu -n istio



istio名称空间外，没有配置，当前名称空间有配置

kubectl port-forward --address 0.0.0.0 -n istio productpage-v1-659776cb44-rrr87 15001:15000



![1628144556(1)](images\1628144556(1).jpg)



![1628145043(1)](images\1628145043(1).jpg)



### 2名称空间

serviceentries/se-baidu-namespace.yaml

kubectl apply -f se-baidu-namespace.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: ServiceEntry
metadata:
  name: baidu
spec:
  exportTo: 
  - "istio-system"
  hosts:
  - "www.baidu.com"
  ports:
  - number: 80
    name: http
    protocol: HTTP
  location: MESH_EXTERNAL
  resolution: DNS
```

serviceentries/sleep.yaml 

kubectl apply -f sleep.yaml  -n istio

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

访问不同

修改名称空间为istio，再测试，可以访问

curl www.baidu.com

清理：

kubectl delete -f sleep.yaml  -n istio

kubectl  delete se baidu -n istio



kubectl port-forward --address 0.0.0.0 -n istio-system istio-ingressgateway-8657768d87-bd767 15002:15000



![1628145433(1)](images\1628145433(1).jpg)





![1628145483(1)](images\1628145483(1).jpg)



### 3 所有名称空间

serviceentries/se-baidu-star.yaml

kubectl apply -f se-baidu-star.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: ServiceEntry
metadata:
  name: baidu
spec:
  exportTo: 
  - "*"
  hosts:
  - "www.baidu.com"
  ports:
  - number: 80
    name: http
    protocol: HTTP
  location: MESH_EXTERNAL
  resolution: DNS
```

serviceentries/sleep.yaml 

kubectl apply -f sleep.yaml  -n istio

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



curl www.baidu.com

清理：

kubectl delete -f sleep.yaml  -n istio

kubectl  delete se baidu -n istio



![1628145689(1)](images\1628145689(1).jpg)



![1628145724(1)](images\1628145724(1).jpg)



## 8-3hosts

serviceentries/se-baidu-hosts.yaml

kubectl apply -f se-baidu-hosts.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: ServiceEntry
metadata:
  name: baidu
spec:
  hosts:
  - "www.baidu.com"
  - "www.csdn.net"
  ports:
  - number: 80
    name: http
    protocol: HTTP
  location: MESH_EXTERNAL
  resolution: DNS
```

serviceentries/sleep.yaml 

kubectl apply -f sleep.yaml  -n istio

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

kubectl exec -it sleep-111-111 -n istio /bin/bash

curl www.baidu.com

清理：

kubectl delete -f sleep.yaml  -n istio

kubectl  delete se baidu -n istio



![1628305953(1)](images\1628305953(1).jpg)



![1628306068(1)](images\1628306068(1).jpg)



![1628306115(1)](images\1628306115(1).jpg)



![1628306180(1)](images\1628306180(1).jpg)



![1628306215(1)](images\1628306215(1).jpg)



![1628306252(1)](images\1628306252(1).jpg)





## 8-4resolution

`resolution` ：表示服务发现的模式

- 如果是一个明确IP，配置为`NONE` 。
- 如果使用了endpoints，配置为`STATIC` 。
- 如果使用了DNS域名，配置为`DNS` 。

###  DNS 

serviceentries/se-baidu-resolution-dns.yaml

kubectl apply -f se-baidu-resolution-dns.yaml -n istio

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

serviceentries/sleep.yaml 

kubectl apply -f sleep.yaml  -n istio

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

kubectl exec -it sleep-111-111 -n istio /bin/bash

curl www.baidu.com

清理：

kubectl delete -f sleep.yaml  -n istio

kubectl  delete se baidu -n istio



![1628306395(1)](images\1628306395(1).jpg)



![1628306435(1)](images\1628306435(1).jpg)





![1628306467(1)](images\1628306467(1).jpg)



###  STATIC 

se-baidu-resolution-static.yaml

kubectl apply -f se-baidu-resolution-static.yaml -n istio

```
apiVersion: networking.istio.io/v1alpha3
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
  resolution: STATIC
  endpoints:
  - address: 36.152.44.96
```

serviceentries/sleep.yaml 

kubectl apply -f sleep.yaml  -n istio

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

kubectl exec -it sleep-111-111 -n istio /bin/bash

curl www.baidu.com



清理：

kubectl delete -f sleep.yaml  -n istio

kubectl  delete se baidu -n istio



![1628306730(1)](images\1628306730(1).jpg)



![1628306812(1)](images\1628306812(1).jpg)



![1628306843(1)](images\1628306843(1).jpg)

mongodb-se-resolution-static.yaml

kubectl apply -f mongodb-se-resolution-static.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: ServiceEntry
metadata:
  name: mongodb-se
spec:
  hosts:
  - mymongodb.demo 
  ports:
  - number: 27017
    name: mongodb
    protocol: MONGO
  location: MESH_EXTERNAL
  resolution: STATIC
  endpoints:
  - address: 192.168.229.134
```

serviceentries/mongodb-deploy.yaml

kubectl apply -f mongodb-deploy.yaml  -n istio

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

kubectl exec -it mongodb-v1-111-111 -n istio /bin/bash

mongo --host mymongodb.demo --port 27017



清理：

kubectl delete -f mongodb-deploy.yaml  -n istio

kubectl delete se mongodb-se -n istio



![1628307125(1)](images\1628307125(1).jpg)



![1628307171(1)](images\1628307171(1).jpg)





![1628307206(1)](images\1628307206(1).jpg)



###  NONE 

se-baidu-resolution-none.yaml

kubectl apply -f se-baidu-resolution-none.yaml -n istio

配置静态dns

kubectl edit cm coredns -n kube-system

hosts {
            192.168.198.158 mymongodb.demo
            36.152.44.96 www.baidu.com
            fallthrough
        }

```
apiVersion: networking.istio.io/v1beta1
kind: ServiceEntry
metadata:
  name: baidu
spec:
  hosts:
  - www.baidu.com
  location: MESH_EXTERNAL
  ports:
  - number: 80
    name: http
    protocol: HTTP
  resolution: NONE
```

serviceentries/sleep.yaml 

kubectl apply -f sleep.yaml  -n istio

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

kubectl exec -it sleep-111-111 -n istio /bin/bash

curl www.baidu.com



清理：

kubectl delete -f sleep.yaml  -n istio

kubectl  delete se baidu -n istio



![1628307627(1)](images\1628307627(1).jpg)

![1628307720(1)](images\1628307720(1).jpg)



![1628307756(1)](images\1628307756(1).jpg)





## 8-5vs dr se联合使用

1部署mongodb

yum install mongodb-org

配置mongodb远程访问

bind 0.0.0.0

启动mongod

systemctl start mongod

2创建se

mongodb-se-resolution-static-multi-ep.yaml

kubectl apply -f mongodb-se-resolution-static-multi-ep.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: ServiceEntry
metadata:
  name: mongodb-se
spec:
  hosts:
  - mymongodb.demo 
  ports:
  - number: 27017
    name: mongodb
    protocol: MONGO
  location: MESH_EXTERNAL
  resolution: STATIC
  endpoints:
  - address: 192.168.198.154
  - address: 192.168.198.155
```

3创建vs

vs-mongodb.yaml 

kubectl apply -f vs-mongodb.yaml  -n istio

```
apiVersion: networking.istio.io/v1alpha3
kind: VirtualService
metadata:
  name: vs-mongodb
spec:
  hosts:
  - "mymongodb.demo"
  tcp:
  - route:
    - destination:
        host: mymongodb.demo
```

4创建dr

dr-mongodb-random.yaml

kubectl apply -f dr-mongodb-random.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: DestinationRule
metadata:
  name: mymongodb
spec:
  host: mymongodb.demo
  trafficPolicy:
    loadBalancer:
      simple: RANDOM
```



6进入mongodb pod

serviceentries/mongodb-deploy.yaml

kubectl apply -f mongodb-deploy.yaml  -n istio

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



kubectl exec -it mongodb-v1-64d4666575-6n2dq -n istio -- /bin/bash

7访问

mongo --host mymongodb.demo 

或

 mongo --host 192.168.198.158



mongodb-se-resolution-static-multi-ep-2.yaml

kubectl apply -f mongodb-se-resolution-static-multi-ep-2.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: ServiceEntry
metadata:
  name: mongodb-se
spec:
  hosts:
  - mymongodb.demo 
  ports:
  - number: 27017
    name: mongodb
    protocol: MONGO
  - number: 27018
    name: mongodb-2
    protocol: MONGO
  location: MESH_EXTERNAL
  resolution: STATIC
  endpoints:
  - address: 192.168.229.134
    labels:
      version: v1
  - address: 192.168.229.135
    labels:
      version: v2
```



dr-mongodb-random-2.yaml

kubectl apply -f dr-mongodb-random-2.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: DestinationRule
metadata:
  name: mymongodb
spec:
  host: mymongodb.demo
  subsets:
  - name: v1
    labels:
      version: v1
  - name: v2
    labels:
      version: v2
  trafficPolicy:
    loadBalancer:
      simple: RANDOM
```



vs-mongodb-route.yaml 

kubectl apply -f vs-mongodb-route.yaml  -n istio

```
apiVersion: networking.istio.io/v1alpha3
kind: VirtualService
metadata:
  name: vs-mongodb
spec:
  hosts:
  - "mymongodb.demo"
  tcp:
  - match:
    - port: 27018
    route:
    - destination:
        host: mymongodb.demo
        port:
          number: 27017
        subset: v2
  - route:
    - destination:
        host: mymongodb.demo
        subset: v1
```



mongo --host mymongodb.demo  --port 27018





清理：

kubectl delete -f mongodb-deploy.yaml  -n istio

kubectl delete dr mymongodb -n istio

kubectl delete vs vs-mongodb -n istio

kubectl delete se mongodb-se -n istio



![1628308223(1)](images\1628308223(1).jpg)



![1628308274(1)](images\1628308274(1).jpg)





![1628308333(1)](images\1628308333(1).jpg)



## 8-6location

| Name            | Description                                                  |
| --------------- | ------------------------------------------------------------ |
| `MESH_EXTERNAL` | Signifies that the service is external to the mesh. Typically used to indicate external services consumed through APIs. |
| `MESH_INTERNAL` | Signifies that the service is part of the mesh. Typically used to indicate services added explicitly as part of expanding the service mesh to include unmanaged infrastructure (e.g., VMs added to a Kubernetes based service mesh). |

### `MESH_EXTERNAL`

serviceentries/se-baidu-star.yaml

kubectl apply -f se-baidu-star.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: ServiceEntry
metadata:
  name: baidu
spec:
  exportTo: 
  - "*"
  hosts:
  - "www.baidu.com"
  ports:
  - number: 80
    name: http
    protocol: HTTP
  location: MESH_EXTERNAL
  resolution: DNS
```

serviceentries/sleep.yaml 

kubectl apply -f sleep.yaml  -n istio

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

kubectl exec -it sleep-111-111 -n istio /bin/bash

curl www.baidu.com

清理：

kubectl delete -f sleep.yaml  -n istio

kubectl  delete se baidu -n istio



![1628308575(1)](images\1628308575(1).jpg)



![1628308618(1)](images\1628308618(1).jpg)



![1628308650(1)](images\1628308650(1).jpg)





### `MESH_INTERNAL`

deploy-details-v2.yaml

kubectl apply -f deploy-details-v2.yaml 

```
apiVersion: apps/v1
kind: Deployment
metadata:
  name: details-v2
  labels:
    app: details
    version: v2
spec:
  replicas: 1
  selector:
    matchLabels:
      app: details
      version: v2
  template:
    metadata:
      labels:
        app: details
        version: v2
    spec:
      containers:
      - name: details
        image: docker.io/istio/examples-bookinfo-details-v1:1.16.2
        imagePullPolicy: IfNotPresent
        ports:
        - containerPort: 9080
        env:
        - name: DO_NOT_ENCRYPT
          value: "true"
        securityContext:
          runAsUser: 1000
---
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
    version: v2
```

se-details-location-internal-2.yaml

kubectl  apply  -f se-details-location-internal-2.yaml 

```
apiVersion: networking.istio.io/v1beta1
kind: ServiceEntry
metadata:
  name: details-se
spec:
  hosts:
  - details.bookinfo.com
  location: MESH_INTERNAL
  ports:
  - number: 9080
    name: http
    protocol: HTTP
  resolution: STATIC
  workloadSelector:
    labels:
      app: details
      version: v2
```



serviceentries/sleep.yaml 

kubectl apply -f sleep.yaml  -n istio

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

kubectl exec -it sleep-111-111 -n istio /bin/bash

curl details.bookinfo.com:9080/details/0

清理：

kubectl delete -f sleep.yaml  -n istio

kubectl  delete se details-se -n istio



![1628309223(1)](images\1628309223(1).jpg)





![1628309259(1)](images\1628309259(1).jpg)



![1628309287(1)](images\1628309287(1).jpg)



## 8-9addresses

deploy-details-v2.yaml

kubectl apply -f deploy-details-v2.yaml 

```
apiVersion: apps/v1
kind: Deployment
metadata:
  name: details-v2
  labels:
    app: details
    version: v2
spec:
  replicas: 1
  selector:
    matchLabels:
      app: details
      version: v2
  template:
    metadata:
      labels:
        app: details
        version: v2
    spec:
      containers:
      - name: details
        image: docker.io/istio/examples-bookinfo-details-v1:1.16.2
        imagePullPolicy: IfNotPresent
        ports:
        - containerPort: 9080
        env:
        - name: DO_NOT_ENCRYPT
          value: "true"
        securityContext:
          runAsUser: 1000
---
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
    version: v2
```



se-details-adresses.yaml

kubectl apply -f se-details-adresses.yaml 

```
apiVersion: networking.istio.io/v1beta1
kind: ServiceEntry
metadata:
  name: details-se
spec:
  hosts:
  - details.bookinfo.com
  addresses:
  - 192.168.229.179
  - 192.168.229.178
  location: MESH_INTERNAL
  ports:
  - number: 9080
    name: http
    protocol: HTTP
  resolution: STATIC
  workloadSelector:
    labels:
      app: details
      version: v2
```

vs-details.yaml

kubectl apply -f vs-details.yaml -n istio

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
  - route:
    - destination:
        host: details.bookinfo.com
        port:
          number: 9080
```

gw.yaml

kubectl apply -f gw.yaml -n istio

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

访问浏览器：

http://192.168.229.134:30468/details/0



curl 192.168.229.178:9080/details/0

```
[root@node01 ~]# kubectl exec -it -n istio sleep-557747455f-g2rgh /bin/sh
kubectl exec [POD] [COMMAND] is DEPRECATED and will be removed in a future version. Use kubectl exec [POD] -- [COMMAND] instead.
/ $ curl 192.168.229.178:9080/details/0
{"id":0,"author":"William Shakespeare","year":1595,"type":"paperback","pages":200,"publisher":"PublisherA","language":"English","ISBN-10":"1234567890","ISBN-13":"123-1234567890"}/ $ 
```

第一个ip无效



清理：

kubectl delete -f sleep.yaml  -n istio

kubectl  delete se details-se -n istio



没有177的配置数据

![1628311397(1)](images\1628311397(1).jpg)



![1628311443(1)](images\1628311443(1).jpg)



![1628311481(1)](images\1628311481(1).jpg)



## 8-10ports

http端口：

serviceentries/se-baidu-star.yaml

kubectl apply -f se-baidu-star.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: ServiceEntry
metadata:
  name: baidu
spec:
  exportTo: 
  - "*"
  hosts:
  - "www.baidu.com"
  ports:
  - number: 80
    name: http
    protocol: HTTP
  location: MESH_EXTERNAL
  resolution: DNS
```

serviceentries/sleep.yaml 

kubectl apply -f sleep.yaml  -n istio

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

kubectl exec -it sleep-111-111 -n istio /bin/bash

curl www.baidu.com

清理：

kubectl delete -f sleep.yaml  -n istio

kubectl  delete se baidu -n istio

![1628311797(1)](images\1628311797(1).jpg)



![1628311843(1)](images\1628311843(1).jpg)





![1628311880(1)](images\1628311880(1).jpg)



443端口

se-baidu-ports-https.yaml

kubectl apply -f se-baidu-ports-https.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: ServiceEntry
metadata:
  name: baidu
spec:
  exportTo: 
  - "*"
  hosts:
  - "www.baidu.com"
  ports:
  - number: 443
    name: https
    protocol: HTTPS
  location: MESH_EXTERNAL
  resolution: DNS
```

serviceentries/sleep.yaml 

kubectl apply -f sleep.yaml  -n istio

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

kubectl exec -it sleep-111-111 -n istio /bin/bash

curl https://www.baidu.com

清理：

kubectl delete -f sleep.yaml  -n istio

kubectl  delete se baidu -n istio

![1628312052(1)](images\1628312052(1).jpg)



![1628312097(1)](images\1628312097(1).jpg)



![1628312140(1)](images\1628312140(1).jpg)



se-jd-ports-https.yaml

kubectl apply -f se-jd-ports-https.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: ServiceEntry
metadata:
  name: jd-api
spec:
  hosts:
  - api.jd.com
  ports:
  - number: 443
    name: https
    protocol: HTTPS
  resolution: DNS
```

serviceentries/sleep.yaml 

kubectl apply -f sleep.yaml  -n istio

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

kubectl exec -it sleep-111-111 -n istio /bin/bash

curl https://api.jd.com

清理：

kubectl delete -f sleep.yaml  -n istio

kubectl  delete se jd-api -n istio



![1628312288(1)](images\1628312288(1).jpg)

![1628312325(1)](images\1628312325(1).jpg)



![1628312359(1)](images\1628312359(1).jpg)



## 8-11使用egress

egress/se-cnn.yaml

kubectl apply -f se-cnn.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: ServiceEntry
metadata:
  name: cnn
spec:
  hosts:
  - edition.cnn.com
  ports:
  - number: 80
    name: http-port
    protocol: HTTP
  resolution: DNS
```

egress/cnn-egressgateway.yaml

kubectl apply -f cnn-egressgateway.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: Gateway
metadata:
  name: cnn-egressgateway
spec:
  selector:
    istio: egressgateway
  servers:
  - port:
      number: 80
      name: http
      protocol: HTTP
    hosts:
    - edition.cnn.com
```

egress/dr-egressgateway-cnn.yaml

kubectl apply -f dr-egressgateway-cnn.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: DestinationRule
metadata:
  name: dr-egressgateway-cnn
spec:
  host: istio-egressgateway.istio-system.svc.cluster.local
  subsets:
  - name: cnn
```



egress/vs-cnn.yaml

kubectl apply -f vs-cnn.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: direct-cnn-through-egress-gateway
spec:
  hosts:
  - edition.cnn.com
  gateways:
  - cnn-egressgateway
  - mesh
  http:
  - match:
    - gateways:
      - mesh
      port: 80
    route:
    - destination:
        host: istio-egressgateway.istio-system.svc.cluster.local
        subset: cnn
        port:
          number: 80
      weight: 100
  - match:
    - gateways:
      - cnn-egressgateway
      port: 80
    route:
    - destination:
        host: edition.cnn.com
        port:
          number: 80
      weight: 100
```

serviceentries/sleep.yaml 

kubectl apply -f sleep.yaml  -n istio

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

kubectl exec -it sleep-111-111 -n istio /bin/bash

curl http://edition.cnn.com/politics -I

查看egress日志

 kubectl logs istio-egressgateway-bd6d77495-vmhvg  -n istio-system -f

清理：

kubectl delete -f sleep.yaml  -n istio

kubectl  delete se cnn  -n istio

kubectl  delete vs direct-cnn-through-egress-gateway -n istio

kubectl delete dr dr-egressgateway-cnn -n istio

kubectl delete gw cnn-egressgateway -n istio

![1628312915(1)](images\1628312915(1).jpg)



![1628312958(1)](images\1628312958(1).jpg)



![1628312988(1)](images\1628312988(1).jpg)

![1628313064(1)](images\1628313064(1).jpg)



![1628313112(1)](images\1628313112(1).jpg)

![1628313152(1)](images\1628313152(1).jpg)



 kubectl port-forward --address 0.0.0.0 -n istio-system istio-egressgateway-585f7668fc-4nxdz 15001:15000

![1628313420(1)](images\1628313420(1).jpg)

![1628313463(1)](images\1628313463(1).jpg)





## 8-12endpoints

| Field            | Type     | Description                                                  | Required |
| ---------------- | -------- | ------------------------------------------------------------ | -------- |
| `address`        | `string` | Address associated with the network endpoint without the port. Domain names can be used if and only if the resolution is set to DNS, and must be fully-qualified without wildcards. Use the form unix:///absolute/path/to/socket for Unix domain socket endpoints. | Yes      |
| `ports`          | `map`    | Set of ports associated with the endpoint. If the port map is specified, it must be a map of servicePortName to this endpoint’s port, such that traffic to the service port will be forwarded to the endpoint port that maps to the service’s portName. If omitted, and the targetPort is specified as part of the service’s port specification, traffic to the service port will be forwarded to one of the endpoints on the specified `targetPort`. If both the targetPort and endpoint’s port map are not specified, traffic to a service port will be forwarded to one of the endpoints on the same port.**NOTE 1:** Do not use for `unix://` addresses.**NOTE 2:** endpoint port map takes precedence over targetPort. | No       |
| `labels`         | `map`    | One or more labels associated with the endpoint.             | No       |
| `network`        | `string` | Network enables Istio to group endpoints resident in the same L3 domain/network. All endpoints in the same network are assumed to be directly reachable from one another. When endpoints in different networks cannot reach each other directly, an Istio Gateway can be used to establish connectivity (usually using the `AUTO_PASSTHROUGH` mode in a Gateway Server). This is an advanced configuration used typically for spanning an Istio mesh over multiple clusters. | No       |
| `locality`       | `string` | The locality associated with the endpoint. A locality corresponds to a failure domain (e.g., country/region/zone). Arbitrary failure domain hierarchies can be represented by separating each encapsulating failure domain by /. For example, the locality of an an endpoint in US, in US-East-1 region, within availability zone az-1, in data center rack r11 can be represented as us/us-east-1/az-1/r11. Istio will configure the sidecar to route to endpoints within the same locality as the sidecar. If none of the endpoints in the locality are available, endpoints parent locality (but within the same network ID) will be chosen. For example, if there are two endpoints in same network (networkID “n1”), say e1 with locality us/us-east-1/az-1/r11 and e2 with locality us/us-east-1/az-2/r12, a sidecar from us/us-east-1/az-1/r11 locality will prefer e1 from the same locality over e2 from a different locality. Endpoint e2 could be the IP associated with a gateway (that bridges networks n1 and n2), or the IP associated with a standard service endpoint. | No       |
| `weight`         | `uint32` | The load balancing weight associated with the endpoint. Endpoints with higher weights will receive proportionally higher traffic. | No       |
| `serviceAccount` | `string` | The service account associated with the workload if a sidecar is present in the workload. The service account must be present in the same namespace as the configuration ( WorkloadEntry or a ServiceEntry) |          |



### address

mongodb-se-resolution-static-multi-ep.yaml

kubectl apply -f mongodb-se-resolution-static-multi-ep.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: ServiceEntry
metadata:
  name: mongodb-se
spec:
  hosts:
  - mymongodb.demo 
  addresses:
  - "192.168.198.158/32"
  ports:
  - number: 27017
    name: mongodb
    protocol: MONGO
  location: MESH_EXTERNAL
  resolution: STATIC
  endpoints:
  - address: 192.168.198.154
  - address: 192.168.198.155
```

vs-mongodb.yaml

kubectl apply -f vs-mongodb.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: mongo
spec:
  hosts:
  - "mymongodb.demo"
  - "mongodb.com"
  gateways:
  - mongo
  - mesh
  tcp:
  - match:
    - port: 27017
    route:
    - destination:
        host: mymongodb.demo
        port:
          number: 27017
```



gw.yaml

kubectl apply -f gw.yaml -n istio

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



serviceentries/mongodb-deploy.yaml

kubectl apply -f mongodb-deploy.yaml  -n istio

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



kubectl exec -it mongodb-v1-64d4666575-6n2dq -n istio -- /bin/bash

7访问

mongo --host mymongodb.demo 

或

 mongo --host 192.168.198.158



清理:

kubectl delete -f mongodb-deploy.yaml  -n istio

kubectl delete se mongodb-se -n istio

![1628391150(1)](images\1628391150(1).jpg)



![1628391259(1)](images\1628391259(1).jpg)



![1628391292(1)](images\1628391292(1).jpg)

### labels

1创建se

endpoints/se-mongodb-labels.yaml

kubectl apply -f se-mongodb-labels.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: ServiceEntry
metadata:
  name: mongodb-se
spec:
  hosts:
  - mymongodb.demo 
  addresses:
  - "192.168.198.158/32"
  ports:
  - number: 27017
    name: mongodb
    protocol: MONGO
  location: MESH_EXTERNAL
  resolution: STATIC
  endpoints:
  - address: 192.168.198.154
    labels:
      version: v1
  - address: 192.168.198.155
    labels:
      version: v2
```

2创建vs

endpoints/vs-mongodb-v1.yaml

kubectl apply -f vs-mongodb-v1.yaml -n istio

```
apiVersion: networking.istio.io/v1alpha3
kind: VirtualService
metadata:
  name: vs-mongodb
spec:
  hosts:
  - "mymongodb.demo"
  tcp:
  - route:
    - destination:
        host: mymongodb.demo
        subset: v1
```

3创建dr

endpoints/dr-mongodb.yaml

kubectl apply -f dr-mongodb.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: DestinationRule
metadata:
  name: mymongodb
spec:
  host: mymongodb.demo
  trafficPolicy:
    loadBalancer:
      simple: RANDOM
  subsets:
  - name: v1
    labels:
      version: v1
  - name: v2
    labels:
      version: v2
```



serviceentries/mongodb-deploy.yaml

kubectl apply -f mongodb-deploy.yaml  -n istio

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



kubectl exec -it mongodb-v1-64d4666575-6n2dq -n istio -- /bin/bash

7访问

mongo --host mymongodb.demo 

或

 mongo --host 192.168.198.158

结果都路由到v1版本



清理:

kubectl delete -f mongodb-deploy.yaml  -n istio

kubectl delete se mongodb-se -n istio

kubectl delete dr mymongodb -n istio

kubectl delete vs vs-mongodb -n istio

![1628391381(1)](images\1628391381(1).jpg)

![1628391419(1)](images\1628391419(1).jpg)



![1628391510(1)](images\1628391510(1).jpg)

![1628391560(1)](images\1628391560(1).jpg)

![1628391589(1)](images\1628391589(1).jpg)



![1628391633(1)](images\1628391633(1).jpg)



![1628391673(1)](images\1628391673(1).jpg)





### locality

region/zone/subzone

#### distribute

```
[root@master01 kube]# kubectl get node --show-labels
NAME              STATUS   ROLES    AGE   VERSION   LABELS
192.168.198.154   Ready    master   22d   v1.20.5   beta.kubernetes.io/arch=amd64,beta.kubernetes.io/os=linux,kubernetes.io/arch=amd64,kubernetes.io/hostname=192.168.198.154,kubernetes.io/os=linux,kubernetes.io/role=master,topology.istio.io/subzone=sz01,topology.kubernetes.io/region=us-central1,topology.kubernetes.io/zone=z1
192.168.198.155   Ready    master   22d   v1.20.5   beta.kubernetes.io/arch=amd64,beta.kubernetes.io/os=linux,kubernetes.io/arch=amd64,kubernetes.io/hostname=192.168.198.155,kubernetes.io/os=linux,kubernetes.io/role=master,topology.istio.io/subzone=sz02,topology.kubernetes.io/region=us-central2,topology.kubernetes.io/zone=z2
192.168.198.156   Ready    node     22d   v1.20.5   beta.kubernetes.io/arch=amd64,beta.kubernetes.io/os=linux,kubernetes.io/arch=amd64,kubernetes.io/hostname=192.168.198.156,kubernetes.io/os=linux,kubernetes.io/role=node,topology.istio.io/subzone=sz03,topology.kubernetes.io/region=us-central3,topology.kubernetes.io/zone=z3
```



```
kubectl label node 192.168.229.134 topology.kubernetes.io/region=us-central1
kubectl label node 192.168.229.134 topology.kubernetes.io/zone=z1
kubectl label node 192.168.229.134 topology.istio.io/subzone=sz01

kubectl label node 192.168.229.135 topology.kubernetes.io/region=us-central2
kubectl label node 192.168.229.135 topology.kubernetes.io/zone=z2
kubectl label node 192.168.229.135 topology.istio.io/subzone=sz02

kubectl label node 192.168.229.136 topology.kubernetes.io/region=us-central3 --overwrite
kubectl label node 192.168.229.136 topology.kubernetes.io/zone=z3 --overwrite
kubectl label node 192.168.229.136 topology.istio.io/subzone=sz03 --overwrite
```

endpoints/se-mongodb-locality.yaml

kubectl apply -f se-mongodb-locality.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: ServiceEntry
metadata:
  name: mongodb-se
spec:
  hosts:
  - mymongodb.demo 
  addresses:
  - "192.168.198.158/32"
  ports:
  - number: 27017
    name: mongodb
    protocol: MONGO
  location: MESH_EXTERNAL
  resolution: STATIC
  endpoints:
  - address: 192.168.198.154
    locality: "us-central1/z1/sz01"
    labels:
      version: v1
  - address: 192.168.198.155
    labels:
      version: v2
    locality: "us-central2/z2/sz02"
```

topology.kubernetes.io/region=us-central1

topology.kubernetes.io/zone=z1

topology.istio.io/subzone=sz01

endpoints/dr-mongodb-locality.yaml

kubectl apply -f dr-mongodb-locality.yaml -n istio

```
apiVersion: networking.istio.io/v1alpha3
kind: DestinationRule
metadata:
  name: dr-mongodb
spec:
  host: mymongodb.demo
  trafficPolicy:
    loadBalancer:
      localityLbSetting:
        enabled: true
        distribute:
        - from: "us-central1/z1/*"
          to:
            #"us-central3/z3/*": 100
            "us-central2/z2/*": 100
            #"us-central1/z1/*": 100
    outlierDetection:
      consecutive5xxErrors: 1
      interval: 5m
      baseEjectionTime: 15m
```

endpoints/vs-mongodb-locality.yaml

kubectl apply -f vs-mongodb-locality.yaml -n istio

```
apiVersion: networking.istio.io/v1alpha3
kind: VirtualService
metadata:
  name: vs-mongodb
spec:
  hosts:
  - "mymongodb.demo"
  tcp:
  - route:
    - destination:
        host: mymongodb.demo
```

serviceentries/mongodb-deploy.yaml

kubectl apply -f mongodb-deploy.yaml  -n istio

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



kubectl exec -it mongodb-v1-64d4666575-6n2dq -n istio -- /bin/bash

7访问

mongo --host mymongodb.demo 

或

 mongo --host 192.168.198.158



清理:

kubectl delete -f mongodb-deploy.yaml  -n istio

kubectl delete se mongodb-se -n istio

kubectl delete dr dr-mongodb -n istio

kubectl delete vs vs-mongodb -n istio

![1628392087(1)](images\1628392087(1).jpg)

![1628392134(1)](images\1628392134(1).jpg)

![1628392173(1)](images\1628392173(1).jpg)





#### failover

endpoints/dr-mongodb-locality-failover.yaml

kubectl apply -f dr-mongodb-locality-failover.yaml -n istio

```
apiVersion: networking.istio.io/v1alpha3
kind: DestinationRule
metadata:
  name: dr-mongodb
spec:
  host: mymongodb.demo
  trafficPolicy:
    loadBalancer:
      localityLbSetting:
        enabled: true
        failover:
        - from: us-central1/z1/sz01
          to: us-central2/z2/sz02
        - from: us-central2/z2/sz02
          to: us-central1/z1/sz01
    outlierDetection:
      consecutive5xxErrors: 1
      interval: 1s
      baseEjectionTime: 15m
```

#### 

```
[root@master01 kube]# kubectl get node --show-labels
NAME              STATUS   ROLES    AGE   VERSION   LABELS
192.168.198.154   Ready    master   22d   v1.20.5   beta.kubernetes.io/arch=amd64,beta.kubernetes.io/os=linux,kubernetes.io/arch=amd64,kubernetes.io/hostname=192.168.198.154,kubernetes.io/os=linux,kubernetes.io/role=master,topology.istio.io/subzone=sz01,topology.kubernetes.io/region=us-central1,topology.kubernetes.io/zone=z1
192.168.198.155   Ready    master   22d   v1.20.5   beta.kubernetes.io/arch=amd64,beta.kubernetes.io/os=linux,kubernetes.io/arch=amd64,kubernetes.io/hostname=192.168.198.155,kubernetes.io/os=linux,kubernetes.io/role=master,topology.istio.io/subzone=sz02,topology.kubernetes.io/region=us-central2,topology.kubernetes.io/zone=z2
192.168.198.156   Ready    node     22d   v1.20.5   beta.kubernetes.io/arch=amd64,beta.kubernetes.io/os=linux,kubernetes.io/arch=amd64,kubernetes.io/hostname=192.168.198.156,kubernetes.io/os=linux,kubernetes.io/role=node,topology.istio.io/subzone=sz03,topology.kubernetes.io/region=us-central3,topology.kubernetes.io/zone=z3
```

endpoints/se-mongodb-locality.yaml

kubectl apply -f se-mongodb-locality.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: ServiceEntry
metadata:
  name: mongodb-se
spec:
  hosts:
  - mymongodb.demo 
  addresses:
  - "192.168.198.158/32"
  ports:
  - number: 27017
    name: mongodb
    protocol: MONGO
  location: MESH_EXTERNAL
  resolution: STATIC
  endpoints:
  - address: 192.168.198.154
    locality: "us-central1/z1/sz01"
    labels:
      version: v1
  - address: 192.168.198.155
    labels:
      version: v2
    locality: "us-central2/z2/sz02"
```

topology.kubernetes.io/region=us-central1

topology.kubernetes.io/zone=z1

topology.istio.io/subzone=sz01



endpoints/vs-mongodb-locality.yaml

kubectl apply -f vs-mongodb-locality.yaml -n istio

```
apiVersion: networking.istio.io/v1alpha3
kind: VirtualService
metadata:
  name: vs-mongodb
spec:
  hosts:
  - "mymongodb.demo"
  tcp:
  - route:
    - destination:
        host: mymongodb.demo
```

serviceentries/mongodb-deploy.yaml

kubectl apply -f mongodb-deploy.yaml  -n istio

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



kubectl exec -it mongodb-v1-64d4666575-6n2dq -n istio -- /bin/bash

7访问

mongo --host mymongodb.demo 

或

 mongo --host 192.168.198.158



清理:

kubectl delete -f mongodb-deploy.yaml  -n istio

kubectl delete se mongodb-se -n istio

kubectl delete dr dr-mongodb -n istio

kubectl delete vs vs-mongodb -n istio

![1628392465(1)](images\1628392465(1).jpg)



![1628392511(1)](images\1628392511(1).jpg)



![1628392562(1)](images\1628392562(1).jpg)



### network

endpoints/se-mongodb-network.yaml

```
apiVersion: networking.istio.io/v1beta1
kind: ServiceEntry
metadata:
  name: mongodb-se
spec:
  hosts:
  - mymongodb.demo 
  addresses:
  - "192.168.198.158/32"
  ports:
  - number: 27017
    name: mongodb
    protocol: MONGO
  location: MESH_EXTERNAL
  resolution: STATIC
  endpoints:
  - address: 192.168.198.154
    network: n1
  - address: 192.168.198.155
```

不成功

### weight

endpoints/se-mongodb-weight.yaml

kubectl  apply -f se-mongodb-weight.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: ServiceEntry
metadata:
  name: mongodb-se
spec:
  hosts:
  - mymongodb.demo 
  addresses:
  - "192.168.198.158/32"
  ports:
  - number: 27017
    name: mongodb
    protocol: MONGO
  location: MESH_EXTERNAL
  resolution: STATIC
  endpoints:
  - address: 192.168.198.154
    weight: 10
  - address: 192.168.198.155
    weight: 90
```

serviceentries/mongodb-deploy.yaml

kubectl apply -f mongodb-deploy.yaml  -n istio

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



kubectl exec -it mongodb-v1-64d4666575-6n2dq -n istio -- /bin/bash

7访问

mongo --host mymongodb.demo 

或

 mongo --host 192.168.198.158



清理:

kubectl delete -f mongodb-deploy.yaml  -n istio

kubectl delete se mongodb-se -n istio



![1628392729(1)](images\1628392729(1).jpg)

![1628392772(1)](images\1628392772(1).jpg)



![1628392832(1)](images\1628392832(1).jpg)

### serviceAccount

endpoints/se-mongodb-serviceaccount.yaml

kubectl apply -f se-mongodb-serviceaccount.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: ServiceEntry
metadata:
  name: mongodb-se
spec:
  hosts:
  - mymongodb.demo 
  addresses:
  - "192.168.198.158/32"
  ports:
  - number: 27017
    name: mongodb
    protocol: MONGO
  location: MESH_EXTERNAL
  resolution: STATIC
  endpoints:
  - address: 192.168.198.154
    serviceAccount: mongov1
  - address: 192.168.198.155
    serviceAccount: mongov2
```

workloadentry的serviceaccount

和workloadGroup相关

### ports

endpoints/se-mongodb-endpoint-ports.yaml

kubectl apply -f se-mongodb-endpoint-ports.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: ServiceEntry
metadata:
  name: mongodb-se
spec:
  hosts:
  - mymongodb.demo 
  addresses:
  - "192.168.198.158/32"
  ports:
  - number: 27019
    name: mongodb
    protocol: MONGO
  location: MESH_EXTERNAL
  resolution: STATIC
  endpoints:
  - address: 192.168.198.154
    ports:
      mongodb: 27017
  - address: 192.168.198.155
    ports:
      mongodb: 27017
```

serviceentries/mongodb-deploy.yaml

kubectl apply -f mongodb-deploy.yaml  -n istio

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



kubectl exec -it mongodb-v1-64d4666575-6n2dq -n istio -- /bin/bash

7访问

mongo --host mymongodb.demo --port 27019

清理:

kubectl delete -f mongodb-deploy.yaml  -n istio

kubectl delete se mongodb-se -n istio

![1628393154(1)](images\1628393154(1).jpg)

![1628393204(1)](images\1628393204(1).jpg)



![1628393240(1)](images\1628393240(1).jpg)

## 8-13subjectAltNames

在default部署details2

details2-deploy.yaml

kubectl apply -f details2-deploy.yaml -n default

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
    app: default-details
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
    app: default-details
    version: v1
spec:
  replicas: 1
  selector:
    matchLabels:
      app: default-details
      version: v1
  template:
    metadata:
      labels:
        app: default-details
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



se-details-subject-alt-names.yaml

kubectl apply -f se-details-subject-alt-names.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: ServiceEntry
metadata:
  name: details-se
spec:
  hosts:
  - details.bookinfo.com
  addresses:
  - 192.168.198.159
  location: MESH_INTERNAL
  ports:
  - number: 9080
    name: http
    protocol: HTTP
  resolution: STATIC
  subjectAltNames:
  - "details.default.com"
  workloadSelector:
    labels:
      app: default-details
```

如果使用了证书，配置san

添加静态路由

hosts {
            192.168.198.158 mymongodb.demo
            36.152.44.96 www.baidu.com
            192.168.198.159 details.bookinfo.com
            fallthrough
        }

serviceentries/sleep.yaml 

kubectl apply -f sleep.yaml  -n istio

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

kubectl exec -it sleep-111-111 -n istio /bin/bash

curl details.bookinfo.com:9080/details/0



清理：

kubectl delete -f sleep.yaml  -n istio

kubectl delete -f details2-deploy.yaml -n default

kubectl  delete se details-se -n istio

![1628394508(1)](images\1628394508(1).jpg)



![1628394586(1)](images\1628394586(1).jpg)



![1628394629(1)](images\1628394629(1).jpg)





cd subjectaltnames

1创建证书

 openssl req -x509 -sha256 -nodes -days 365 -newkey rsa:2048 -subj '/O=example Inc./CN=example.com' -keyout example.com.key -out example.com.crt 

 openssl req -out bookinfo.example.com.csr -newkey rsa:2048 -nodes -keyout bookinfo.example.com.key -subj "/CN=details.bookinfo.com/O=some organization"  -config  openssl.cnf 

 openssl x509 -req -days 365 -CA example.com.crt -CAkey example.com.key -set_serial 0 -in bookinfo.example.com.csr -out bookinfo.example.com.crt    -extensions  v3_req  -extfile  openssl.cnf 



2创建secret

 kubectl create secret tls nginx-server-certs --key bookinfo.example.com.key --cert bookinfo.example.com.crt  -n default

3创建nginx配置文件

subjectaltnames/nginx.conf

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

    server_name details.bookinfo.com;
    ssl_certificate /etc/nginx-server-certs/tls.crt;
    ssl_certificate_key /etc/nginx-server-certs/tls.key;
  }
}
```

 kubectl create configmap nginx-configmap --from-file=nginx.conf=./nginx.conf  -n default

4创建deploy

subjectaltnames/nginx-deploy.yaml

kubectl  apply -f nginx-deploy.yaml -n default

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

subjectaltnames/se-details-subject-alt-names.yaml

kubectl apply -f se-details-subject-alt-names.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: ServiceEntry
metadata:
  name: details-se
spec:
  hosts:
  - details.bookinfo.com
  addresses:
  - 192.168.198.159
  location: MESH_INTERNAL
  ports:
  - number: 443
    name: https
    protocol: HTTPS
  resolution: STATIC
  subjectAltNames:
  - "details.default.com"
  workloadSelector:
    labels:
      run: my-nginx
```

如果使用了证书，配置san

serviceentries/sleep.yaml 

kubectl apply -f sleep.yaml  -n istio

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

kubectl exec -it sleep-111-111 -n istio /bin/sh

curl -k https://details.bookinfo.com:443 --resolve details.bookinfo.com:443:192.168.229.159

curl -k https://details.default.com:443 --resolve details.default.com:443:192.168.229.159



清理：

kubectl delete -f sleep.yaml  -n istio

kubectl  delete -f nginx-deploy.yaml -n default

kubectl delete configmap nginx-configmap  -n default

kubectl delete secret nginx-server-certs  -n default

kubectl  delete se details-se -n istio











## 8-14workloadSelector

在default部署details2

details2-deploy.yaml

kubectl apply -f details2-deploy.yaml -n default

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
    app: default-details
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
    app: default-details
    version: v1
spec:
  replicas: 1
  selector:
    matchLabels:
      app: default-details
      version: v1
  template:
    metadata:
      labels:
        app: default-details
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

se-details-workloadSelector.yaml

kubectl apply -f se-details-workloadSelector.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: ServiceEntry
metadata:
  name: details-se
spec:
  hosts:
  - details.bookinfo.com
  addresses:
  - 192.168.198.159
  location: MESH_INTERNAL
  ports:
  - number: 9080
    name: http
    protocol: HTTP
  resolution: STATIC
  workloadSelector:
    labels:
      app: default-details
```

添加静态路由

hosts {
            192.168.198.158 mymongodb.demo
            36.152.44.96 www.baidu.com
            192.168.198.159 details.bookinfo.com
            fallthrough
        }

serviceentries/sleep.yaml 

kubectl apply -f sleep.yaml  -n istio

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

kubectl exec -it sleep-111-111 -n istio /bin/bash

curl details.bookinfo.com:9080/details/0

清理：

kubectl delete -f sleep.yaml  -n istio

kubectl delete -f details2-deploy.yaml -n default

kubectl  delete se details-se -n istio

![1628393773(1)](images\1628393773(1).jpg)



![1628393809(1)](images\1628393809(1).jpg)



![1628393841(1)](images\1628393841(1).jpg)

