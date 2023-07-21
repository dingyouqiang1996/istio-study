# 学习目标

![1620284843(1)](images\1620284843(1).jpg)

# 1什么是WorkloadEntry

 `WorkloadEntry` enables operators to describe the properties of a single non-Kubernetes workload such as a VM or a bare metal server as it is onboarded into the mesh. A `WorkloadEntry` must be accompanied by an Istio `ServiceEntry` that selects the workload through the appropriate labels and provides the service definition for a `MESH_INTERNAL` service (hostnames, port properties, etc.). A `ServiceEntry` object can select multiple workload entries as well as Kubernetes pods based on the label selector specified in the service entry. 

# 资源详解

| Field            | Type     | Description                                                  | Required |
| ---------------- | -------- | ------------------------------------------------------------ | -------- |
| `address`        | `string` | Address associated with the network endpoint without the port. Domain names can be used if and only if the resolution is set to DNS, and must be fully-qualified without wildcards. Use the form unix:///absolute/path/to/socket for Unix domain socket endpoints. | Yes      |
| `ports`          | `map`    | Set of ports associated with the endpoint. If the port map is specified, it must be a map of servicePortName to this endpoint’s port, such that traffic to the service port will be forwarded to the endpoint port that maps to the service’s portName. If omitted, and the targetPort is specified as part of the service’s port specification, traffic to the service port will be forwarded to one of the endpoints on the specified `targetPort`. If both the targetPort and endpoint’s port map are not specified, traffic to a service port will be forwarded to one of the endpoints on the same port.**NOTE 1:** Do not use for `unix://` addresses.**NOTE 2:** endpoint port map takes precedence over targetPort. | No       |
| `labels`         | `map`    | One or more labels associated with the endpoint.             | No       |
| `network`        | `string` | Network enables Istio to group endpoints resident in the same L3 domain/network. All endpoints in the same network are assumed to be directly reachable from one another. When endpoints in different networks cannot reach each other directly, an Istio Gateway can be used to establish connectivity (usually using the `AUTO_PASSTHROUGH` mode in a Gateway Server). This is an advanced configuration used typically for spanning an Istio mesh over multiple clusters. | No       |
| `locality`       | `string` | The locality associated with the endpoint. A locality corresponds to a failure domain (e.g., country/region/zone). Arbitrary failure domain hierarchies can be represented by separating each encapsulating failure domain by /. For example, the locality of an an endpoint in US, in US-East-1 region, within availability zone az-1, in data center rack r11 can be represented as us/us-east-1/az-1/r11. Istio will configure the sidecar to route to endpoints within the same locality as the sidecar. If none of the endpoints in the locality are available, endpoints parent locality (but within the same network ID) will be chosen. For example, if there are two endpoints in same network (networkID “n1”), say e1 with locality us/us-east-1/az-1/r11 and e2 with locality us/us-east-1/az-2/r12, a sidecar from us/us-east-1/az-1/r11 locality will prefer e1 from the same locality over e2 from a different locality. Endpoint e2 could be the IP associated with a gateway (that bridges networks n1 and n2), or the IP associated with a standard service endpoint. | No       |
| `weight`         | `uint32` | The load balancing weight associated with the endpoint. Endpoints with higher weights will receive proportionally higher traffic. | No       |
| `serviceAccount` | `string` | The service account associated with the workload if a sidecar is present in the workload. The service account must be present in the same namespace as the configuration ( WorkloadEntry or a ServiceEntry) |          |

## 2address,labels

wle-mongodb-address.yaml

kubectl apply -f wle-mongodb-address.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: WorkloadEntry
metadata:
  name: mongo-wle
spec:
  address: 192.168.198.154
  labels:
    app: mongodb
    instance-id: vm1
```

se-mongodb.yaml

kubectl apply -f se-mongodb.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: ServiceEntry
metadata:
  name: mongodb-se
spec:
  hosts:
  - mongodb.vm.demo
  addresses:
  - 192.168.198.177
  location: MESH_INTERNAL
  ports:
  - number: 27017
    name: tcp
    protocol: TCP
    targetPort: 27017
  resolution: STATIC
  workloadSelector:
    labels:
      app: mongodb
      instance-id: vm1
```

bookinfo-mongodb.yaml

kubectl apply -f bookinfo-mongodb.yaml -n istio

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

启动154 mongodb

systemctl start mongod

注意如果ingress svc 有27017端口会冲突



访问：

kubectl exec -it -n istio mongodb-v1-64d4666575-2q7qf -- /bin/sh

mongo --host 192.168.198.177 

清理：

kubectl delete  -f bookinfo-mongodb.yaml -n istio

kubectl delete se mongodb-se -n istio

kubectl delete workloadentry mongo-wle -n istio



kubectl port-forward --address 0.0.0.0 -n istio mongodb-v1-64d4666575-2q7qf 15000:15000

![1629352143(1)](images\1629352143(1).jpg)

![1629352191(1)](images\1629352191(1).jpg)

![1629352224(1)](images\1629352224(1).jpg)



## 3locality

wle-mongodb-locality-01.yaml

kubectl apply -f wle-mongodb-locality-01.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: WorkloadEntry
metadata:
  name: mongo-wle-01
spec:
  address: 192.168.198.154
  labels:
    app: mongodb
    instance-id: vm1
    type: vm
  locality: "us-central1/z1/sz01"
    
```

wle-mongodb-locality-02.yaml

kubectl apply -f wle-mongodb-locality-02.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: WorkloadEntry
metadata:
  name: mongo-wle-02
spec:
  address: 192.168.198.155
  labels:
    app: mongodb
    instance-id: vm1
    type: vm
  locality: "us-central2/z2/sz02"
```

se-mongodb-locality.yaml

kubectl apply -f se-mongodb-locality.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: ServiceEntry
metadata:
  name: mongodb-se
spec:
  hosts:
  - mongodb.vm.demo
  addresses:
  - 192.168.198.177
  location: MESH_INTERNAL
  ports:
  - number: 27017
    name: tcp
    protocol: TCP
    targetPort: 27017
  resolution: STATIC
  workloadSelector:
    labels:
      app: mongodb
      type: vm
```

dr-mongodb-locality-failover.yaml

kubectl apply -f dr-mongodb-locality-failover.yaml -n istio

```
apiVersion: networking.istio.io/v1alpha3
kind: DestinationRule
metadata:
  name: dr-mongodb
spec:
  host: mongodb.vm.demo
  trafficPolicy:
    loadBalancer:
      consistentHash:
        useSourceIp: true
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
      baseEjectionTime: 1m
```

bookinfo-mongodb.yaml

kubectl apply -f bookinfo-mongodb.yaml -n istio

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

启动154 mongodb

systemctl start mongod

注意如果ingress svc 有27017端口会冲突

启动155 mongodb

systemctl start mongod



配置coredns

kubectl edit cm coredns -n kube-system

```
hosts {
            192.168.198.158 mymongodb.demo
            192.168.198.177 mongodb.vm.demo
            36.152.44.96 www.baidu.com
            192.168.198.159 details.bookinfo.com
            192.168.198.166 org.apache.dubbo.samples.basic.api.demoservice
            fallthrough
        }
```



访问：

kubectl exec -it -n istio mongodb-v1-64d4666575-2q7qf -- /bin/bash

mongo --host mongodb.vm.demo

清理：

kubectl delete  -f bookinfo-mongodb.yaml -n istio

kubectl delete se mongodb-se -n istio

kubectl delete workloadentry  mongo-wle-02  -n istio

kubectl delete workloadentry  mongo-wle-01  -n istio

kubectl  delete dr dr-mongodb -n istio



kubectl port-forward --address 0.0.0.0 -n istio mongodb-v1-64d4666575-2q7qf 15000:15000

![1629353020(1)](images\1629353020(1).jpg)

![1629353075(1)](images\1629353075(1).jpg)



![1629353119(1)](images\1629353119(1).jpg)



## network

network和多集群有关

wle-mongodb-network.yaml

```
apiVersion: networking.istio.io/v1beta1
kind: WorkloadEntry
metadata:
  name: mongo-wle
spec:
  address: 192.168.198.154
  labels:
    app: mongodb
    instance-id: vm1
    type: vm
  network: n1
```



## 4ports

wle-mongodb-ports.yaml

kubectl apply -f wle-mongodb-ports.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: WorkloadEntry
metadata:
  name: mongo-wle
spec:
  address: 192.168.198.154
  ports:
    mongodb: 27017
  labels:
    app: mongodb
    instance-id: vm1
    type: vm
```

se-mongodb-port.yaml

kubectl apply -f se-mongodb-port.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: ServiceEntry
metadata:
  name: mongodb-se
spec:
  hosts:
  - mongodb.vm.demo
  addresses:
  - 192.168.198.177
  location: MESH_INTERNAL
  ports:
  - number: 27019
    name: tcp
    protocol: TCP
    targetPort: 27017
  resolution: STATIC
  workloadSelector:
    labels:
      app: mongodb
      instance-id: vm1
```

bookinfo-mongodb.yaml

kubectl apply -f bookinfo-mongodb.yaml -n istio

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

启动154 mongodb

systemctl start mongod

注意如果ingress svc 有27017端口会冲突



访问：

kubectl exec -it -n istio mongodb-v1-64d4666575-2q7qf -- /bin/bash

mongo --host 192.168.198.177  --port 27017

清理：

kubectl delete  -f bookinfo-mongodb.yaml -n istio

kubectl delete se mongodb-se -n istio

kubectl delete workloadentry mongo-wle -n istio



kubectl port-forward --address 0.0.0.0 -n istio mongodb-v1-64d4666575-2q7qf 15000:15000

![1629353954(1)](images\1629353954(1).jpg)







## serviceAccount

wle-mongodb-serviceAccount.yaml

```
apiVersion: networking.istio.io/v1beta1
kind: WorkloadEntry
metadata:
  name: mongo-wle
spec:
  serviceAccount: mongodb-vm
  address: 192.168.198.154
  ports:
    mongodb: 27017
  labels:
    app: mongodb
    instance-id: vm1
    type: vm
```

加了serviceaccount访问不了

虚拟机上必须安装sidecar

将在workloadGroup中进行介绍

## 5weight

wle-mongodb-weight-01.yaml

kubectl apply -f wle-mongodb-weight-01.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: WorkloadEntry
metadata:
  name: mongo-wle-01
spec:
  weight: 20
  address: 192.168.198.154
  ports:
    mongodb: 27017
  labels:
    app: mongodb
    instance-id: vm1
    type: vm
```

wle-mongodb-weight-02.yaml

kubectl apply -f wle-mongodb-weight-02.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: WorkloadEntry
metadata:
  name: mongo-wle-02
spec:
  weight: 80
  address: 192.168.198.155
  ports:
    mongodb: 27017
  labels:
    app: mongodb
    instance-id: vm2
    type: vm
```

se-mongodb.yaml

kubectl apply -f se-mongodb.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: ServiceEntry
metadata:
  name: mongodb-se
spec:
  hosts:
  - mongodb.vm.demo
  addresses:
  - 192.168.198.177
  location: MESH_INTERNAL
  ports:
  - number: 27017
    name: tcp
    protocol: TCP
    targetPort: 27017
  resolution: STATIC
  workloadSelector:
    labels:
      app: mongodb
      type: vm
```

dr-mongodb.yaml

kubectl apply -f dr-mongodb.yaml -n istio

```
apiVersion: networking.istio.io/v1alpha3
kind: DestinationRule
metadata:
  name: dr-mongodb
spec:
  host: mongodb.vm.demo
  trafficPolicy:
    loadBalancer:
      simple: ROUND_ROBIN
```

bookinfo-mongodb.yaml

kubectl apply -f bookinfo-mongodb.yaml -n istio

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

启动154 mongodb

systemctl start mongod

注意如果ingress svc 有27017端口会冲突

启动155 mongodb

systemctl start mongod



访问：

kubectl exec -it -n istio mongodb-v1-64d4666575-2q7qf -- /bin/bash

mongo --host mongodb.vm.demo   --port 27017

清理：

kubectl delete  -f bookinfo-mongodb.yaml -n istio

kubectl delete se mongodb-se -n istio

kubectl delete workloadentry mongo-wle-01 -n istio

kubectl delete workloadentry mongo-wle-02 -n istio

kubectl delete dr dr-mongodb -n istio



kubectl port-forward --address 0.0.0.0 -n istio mongodb-v1-64d4666575-2q7qf 15000:15000



![1629354258(1)](images\1629354258(1).jpg)

![1629354317(1)](images\1629354317(1).jpg)



![1629354355(1)](images\1629354355(1).jpg)