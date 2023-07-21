# 学习目标

![1620285821(1)](images\1620285821(1).jpg)

# 什么是WorkloadGroup

 `WorkloadGroup` describes a collection of workload instances. It provides a specification that the workload instances can use to bootstrap their proxies, including the metadata and identity. It is only intended to be used with non-k8s workloads like Virtual Machines, and is meant to mimic the existing sidecar injection and deployment specification model used for Kubernetes workloads to bootstrap Istio proxies. 

![wk](images\wk.jpg)

# 虚拟机部署sidecar

## 单网络

![single-network](images\single-network.svg)



```
连通网络
route add -net 10.68.0.0 netmask 255.255.0.0 gw 192.168.229.134
route add -net 172.20.1.0 netmask 255.255.255.0 gw 192.168.229.135
route add -net 172.20.0.0 netmask 255.255.255.0 gw 192.168.229.134
route add -net 172.20.2.0 netmask 255.255.255.0 gw 192.168.229.136
```

singlenetwork/vm-cluster.yaml 

```
apiVersion: install.istio.io/v1alpha1
kind: IstioOperator
metadata:
  name: istio
spec:
  profile: demo
  values:
    global:
      meshID: mesh1
      multiCluster:
        clusterName: cluster1
      network: ""
  meshConfig:
    accessLogFile: /dev/stdout
    enableTracing: true
  components:
    egressGateways:
    - name: istio-egressgateway
      enabled: true
```

istioctl install -f vm-cluster.yaml --set values.pilot.env.PILOT_ENABLE_WORKLOAD_ENTRY_AUTOREGISTRATION=true --set values.pilot.env.PILOT_ENABLE_WORKLOAD_ENTRY_HEALTHCHECKS=true 



2东西向网关

/root/istio-1.11.2/samples/multicluster/gen-eastwest-gateway.sh --single-cluster | istioctl install -y -f -

kubectl apply -f  /root/istio-1.11.2/samples/multicluster/expose-istiod.yaml -n istio-system



3创建资源

 kubectl create namespace vm-httpd

 kubectl create serviceaccount  sa-httpd -n vm-httpd

  kubectl --namespace vm-httpd  apply -f wlg-labels.yaml

 workloadgroups/singlenetwork/wlg-labels.yaml

```
apiVersion: networking.istio.io/v1alpha3
kind: WorkloadGroup
metadata:
  name: httpd
  namespace: vm-httpd
spec:
  metadata:
    labels:
      app: httpd
      type: vm
  template:
    serviceAccount: sa-httpd
    network: ""
```

4生成配置文件

istioctl x workload entry configure -f wlg-labels.yaml  -o . --clusterID cluster1 --autoregister



externalips不能用的情况

cluster.env

CA_ADDR=istiod.istio-system.svc:31236

ISTIO_PILOT_PORT=31236



5考备配置文件

scp cluster.env  hosts  istio-token  mesh.yaml  root-cert.pem root@192.168.229.147:/root

6安装sidecar

rpm -ivh istio-sidecar.rpm

7复制配置文件

mkdir -p /etc/certs
\cp "${HOME}"/root-cert.pem /etc/certs/root-cert.pem -f

mkdir -p /var/run/secrets/tokens
\cp "${HOME}"/istio-token /var/run/secrets/tokens/istio-token -f

mkdir /var/lib/istio/envoy/ -p

\cp "${HOME}"/cluster.env /var/lib/istio/envoy/cluster.env  -f

\cp "${HOME}"/mesh.yaml /etc/istio/config/mesh  -f

sh -c 'cat $(eval echo ~$SUDO_USER)/hosts >> /etc/hosts'

 mkdir -p /etc/istio/proxy

chown -R istio-proxy /var/lib/istio /etc/certs /etc/istio/proxy /etc/istio/config /var/run/secrets /etc/certs/root-cert.pem

8启动istio

 systemctl start istio 



验证：

vm:

curl productpage.istio:9080/productpage



重启后：

mkdir -p /var/run/secrets/tokens
\cp "${HOME}"/istio-token /var/run/secrets/tokens/istio-token -f



清理：

kubectl delete workloadgroup httpd -n vm-httpd

## 多网络

![multi-network](images\multi-network.svg)



workloadgroups/multinetwork/vm-cluster.yaml 

```
apiVersion: install.istio.io/v1alpha1
kind: IstioOperator
metadata:
  name: istio
spec:
  profile: demo
  values:
    global:
      meshID: mesh1
      multiCluster:
        clusterName: cluster1
      network: network1
  meshConfig:
    accessLogFile: /dev/stdout
    enableTracing: true
  components:
    egressGateways:
    - name: istio-egressgateway
      enabled: true
```



istioctl install -f vm-cluster.yaml --set values.pilot.env.PILOT_ENABLE_WORKLOAD_ENTRY_AUTOREGISTRATION=true --set values.pilot.env.PILOT_ENABLE_WORKLOAD_ENTRY_HEALTHCHECKS=true 

2东西向网关

 /root/istio-1.11.2/samples/multicluster/gen-eastwest-gateway.sh --mesh mesh1 --cluster cluster1 --network network1 | istioctl install -y -f - 

kubectl apply -f  /root/istio-1.11.2/samples/multicluster/expose-istiod.yaml -n istio-system

 kubectl apply -n istio-system -f /root/istio-1.11.2/samples/multicluster/expose-services.yaml

3创建资源

 kubectl create namespace vm-httpd

 kubectl create serviceaccount  sa-httpd -n vm-httpd

  kubectl --namespace vm-httpd  apply -f wlg-labels.yaml

 workloadgroups/multinetwork/wlg-labels.yaml

```
apiVersion: networking.istio.io/v1alpha3
kind: WorkloadGroup
metadata:
  name: httpd
  namespace: vm-httpd
spec:
  metadata:
    labels:
      app: httpd
      type: vm
  template:
    serviceAccount: sa-httpd
    network: network2
```

4生成配置文件

istioctl x workload entry configure -f wlg-labels.yaml  -o . --clusterID cluster1 --autoregister



externalips不能用的情况

cluster.env

CA_ADDR=istiod.istio-system.svc:31279

ISTIO_PILOT_PORT=31279



5考备配置文件

scp cluster.env  hosts  istio-token  mesh.yaml  root-cert.pem root@192.168.229.147:/root

6安装sidecar

rpm -ivh istio-sidecar.rpm

7复制配置文件

mkdir -p /etc/certs
\cp "${HOME}"/root-cert.pem /etc/certs/root-cert.pem -f

mkdir -p /var/run/secrets/tokens
\cp "${HOME}"/istio-token /var/run/secrets/tokens/istio-token -f

mkdir /var/lib/istio/envoy/ -p

\cp "${HOME}"/cluster.env /var/lib/istio/envoy/cluster.env  -f

\cp "${HOME}"/mesh.yaml /etc/istio/config/mesh  -f

sh -c 'cat $(eval echo ~$SUDO_USER)/hosts >> /etc/hosts'

 mkdir -p /etc/istio/proxy

chown -R istio-proxy /var/lib/istio /etc/certs /etc/istio/proxy /etc/istio/config /var/run/secrets /etc/certs/root-cert.pem

8启动istio

 systemctl start istio 



验证：

vm:

curl productpage.istio:9080/productpage



重启后：

mkdir -p /var/run/secrets/tokens
\cp "${HOME}"/istio-token /var/run/secrets/tokens/istio-token -f



清理：

kubectl delete workloadgroup httpd -n vm-httpd



# 资源详解(多网络)

| Field      | Type             | Description                                                  | Required |
| ---------- | ---------------- | ------------------------------------------------------------ | -------- |
| `metadata` | `ObjectMeta`     | Metadata that will be used for all corresponding `WorkloadEntries`. User labels for a workload group should be set here in `metadata` rather than in `template`. | No       |
| `template` | `WorkloadEntry`  | Template to be used for the generation of `WorkloadEntry` resources that belong to this `WorkloadGroup`. Please note that `address` and `labels` fields should not be set in the template, and an empty `serviceAccount` should default to `default`. The workload identities (mTLS certificates) will be bootstrapped using the specified service account’s token. Workload entries in this group will be in the same namespace as the workload group, and inherit the labels and annotations from the above `metadata` field. | Yes      |
| `probe`    | `ReadinessProbe` | `ReadinessProbe` describes the configuration the user must provide for healthchecking on their workload. This configuration mirrors K8S in both syntax and logic for the most part. | No       |

## metadata

| Field         | Type  | Description           | Required |
| ------------- | ----- | --------------------- | -------- |
| `labels`      | `map` | Labels to attach      | No       |
| `annotations` | `map` | Annotations to attach | No       |

***安装集群***

workloadgroups/multinetwork/vm-cluster.yaml 

```
apiVersion: install.istio.io/v1alpha1
kind: IstioOperator
metadata:
  name: istio
spec:
  profile: demo
  values:
    global:
      meshID: mesh1
      multiCluster:
        clusterName: cluster1
      network: network1
  meshConfig:
    accessLogFile: /dev/stdout
    enableTracing: true
  components:
    egressGateways:
    - name: istio-egressgateway
      enabled: true
```



istioctl install -f vm-cluster.yaml --set values.pilot.env.PILOT_ENABLE_WORKLOAD_ENTRY_AUTOREGISTRATION=true --set values.pilot.env.PILOT_ENABLE_WORKLOAD_ENTRY_HEALTHCHECKS=true 

2东西向网关

 /root/istio-1.13.3/samples/multicluster/gen-eastwest-gateway.sh --mesh mesh1 --cluster cluster1 --network network1 | istioctl install -y -f - 

kubectl apply -f  /root/istio-1.13.3/samples/multicluster/expose-istiod.yaml -n istio-system

 kubectl apply -n istio-system -f /root/istio-1.13.3/samples/multicluster/expose-services.yaml

3创建资源

 kubectl create namespace vm-httpd

 kubectl create serviceaccount  sa-httpd -n vm-httpd



#### labels,annotations

workloadgroups/lables-annotations/wlg-labels-annotations.yaml

kubectl apply -f wlg-labels-annotations.yaml -n vm-httpd

```
apiVersion: networking.istio.io/v1alpha3
kind: WorkloadGroup
metadata:
  name: httpd
  namespace: vm-httpd
spec:
  metadata:
    labels:
      app: httpd
      type: vm
    annotations:
      test: test
  template:
    serviceAccount: sa-httpd
    network: network2
      
```



istioctl x workload entry configure -f wlg-labels-annotations.yaml  -o . --clusterID cluster1 --autoregister

```
scp cluster.env  hosts  istio-token  mesh.yaml  root-cert.pem root@192.168.229.146:/root

rm -rf /etc/certs

mkdir -p /etc/certs
\cp "${HOME}"/root-cert.pem /etc/certs/root-cert.pem -f

mkdir -p /var/run/secrets/tokens
\cp "${HOME}"/istio-token /var/run/secrets/tokens/istio-token -f

mkdir /var/lib/istio/envoy/ -p

\cp "${HOME}"/cluster.env /var/lib/istio/envoy/cluster.env  -f

\cp "${HOME}"/mesh.yaml /etc/istio/config/mesh  -f

sh -c 'cat $(eval echo ~$SUDO_USER)/hosts >> /etc/hosts'

 mkdir -p /etc/istio/proxy

chown -R istio-proxy /var/lib/istio /etc/certs /etc/istio/proxy /etc/istio/config /var/run/secrets /etc/certs/root-cert.pem

 systemctl restart istio 
 systemctl start httpd
```

创建se

se-httpd.yaml

 kubectl apply -f se-httpd.yaml -n vm-httpd 

```
apiVersion: networking.istio.io/v1beta1
kind: ServiceEntry
metadata:
  name: httpd-se
spec:
  hosts:
  - httpd.vm.demo
  addresses:
  - 192.168.229.11
  location: MESH_INTERNAL
  ports:
  - number: 80
    name: http-httpd
    protocol: TCP
    targetPort: 80
  resolution: STATIC
  workloadSelector:
    labels:
      app: httpd
      type: vm
```



清理：

kubectl delete workloadgroup httpd -n vm-httpd

kubectl delete se   httpd-se -n vm-httpd



vm:

reboot



```
[root@node01 workloadgroups]# kubectl get workloadentries.networking.istio.io -n vm-httpd httpd-192.168.229.147-network2  -o yaml
apiVersion: networking.istio.io/v1beta1
kind: WorkloadEntry
metadata:
  annotations:
    istio.io/autoRegistrationGroup: httpd
    istio.io/connectedAt: "2021-09-15T02:12:50.237824095Z"
    istio.io/workloadController: istiod-c8479896c-9tmqc
    test: test
  creationTimestamp: "2021-09-15T01:43:17Z"
  generation: 1
  labels:
    app: httpd
    service.istio.io/canonical-name: httpd
    service.istio.io/canonical-version: latest
    type: vm
  name: httpd-192.168.229.147-network2
  namespace: vm-httpd
  ownerReferences:
  - apiVersion: networking.istio.io/v1alpha3
    controller: true
    kind: WorkloadGroup
    name: httpd
    uid: 6f1cefeb-14f8-406c-a1d4-3fe0b6fb8037
  resourceVersion: "323612"
  uid: 1c639a13-7cbf-4d40-b23c-6b9085803c6c
spec:
  address: 192.168.229.147
  labels:
    app: httpd
    service.istio.io/canonical-name: httpd
    service.istio.io/canonical-version: latest
    type: vm
  network: network2
  serviceAccount: sa-httpd
```

kubectl port-forward --address 0.0.0.0 -n istio productpage-v1-77654975fc-xwcjz 15000:15000

![1631672096(1)](images\1631672096(1).jpg)

 

![1631672149(1)](images\1631672149(1).jpg)



![1631672181(1)](images\1631672181(1).jpg)





## probe

| Field                 | Type                            | Description                                                  | Required |
| --------------------- | ------------------------------- | ------------------------------------------------------------ | -------- |
| `initialDelaySeconds` | `int32`                         | Number of seconds after the container has started before readiness probes are initiated. | No       |
| `timeoutSeconds`      | `int32`                         | Number of seconds after which the probe times out. Defaults to 1 second. Minimum value is 1 second. | No       |
| `periodSeconds`       | `int32`                         | How often (in seconds) to perform the probe. Default to 10 seconds. Minimum value is 1 second. | No       |
| `successThreshold`    | `int32`                         | Minimum consecutive successes for the probe to be considered successful after having failed. Defaults to 1 second. | No       |
| `failureThreshold`    | `int32`                         | Minimum consecutive failures for the probe to be considered failed after having succeeded. Defaults to 3 seconds. | No       |
| `httpGet`             | `HTTPHealthCheckConfig (oneof)` | `httpGet` is performed to a given endpoint and the status/able to connect determines health. | No       |
| `tcpSocket`           | `TCPHealthCheckConfig (oneof)`  | Health is determined by if the proxy is able to connect.     | No       |
| `exec`                | `ExecHealthCheckConfig (oneof)` | Health is determined by how the command that is executed exited. | No       |

### httpGet

workloadgroups/probe/httpGet/wlg-probe-httpGet.yaml

kubectl apply -f wlg-probe-httpGet.yaml -n vm-httpd

```
apiVersion: networking.istio.io/v1alpha3
kind: WorkloadGroup
metadata:
  name: httpd
  namespace: vm-httpd
spec:
  metadata:
    labels:
      app: httpd
      type: vm
    annotations:
      test: test
  probe:
    periodSeconds: 5
    initialDelaySeconds: 5
    successThreshold: 1
    failureThreshold: 3
    timeoutSeconds: 2
    httpGet:
      port: 80
      path: /
  template:
    serviceAccount: sa-httpd
    network: network2
      
```



istioctl x workload entry configure -f wlg-probe-httpGet.yaml  -o . --clusterID cluster1 --autoregister

```
scp cluster.env  hosts  istio-token  mesh.yaml  root-cert.pem root@192.168.229.146:/root


mkdir -p /etc/certs
\cp "${HOME}"/root-cert.pem /etc/certs/root-cert.pem -f

mkdir -p /var/run/secrets/tokens
\cp "${HOME}"/istio-token /var/run/secrets/tokens/istio-token -f

mkdir /var/lib/istio/envoy/ -p

\cp "${HOME}"/cluster.env /var/lib/istio/envoy/cluster.env  -f

\cp "${HOME}"/mesh.yaml /etc/istio/config/mesh  -f

sh -c 'cat $(eval echo ~$SUDO_USER)/hosts >> /etc/hosts'

 mkdir -p /etc/istio/proxy

chown -R istio-proxy /var/lib/istio /etc/certs /etc/istio/proxy /etc/istio/config /var/run/secrets /etc/certs/root-cert.pem

 systemctl restart istio 
 systemctl start httpd
```

创建se

workloadgroups/probe/httpGet/se-httpd.yaml

 kubectl apply -f se-httpd.yaml -n vm-httpd 

```
apiVersion: networking.istio.io/v1beta1
kind: ServiceEntry
metadata:
  name: httpd-se
spec:
  hosts:
  - httpd.vm.demo
  addresses:
  - 192.168.229.11
  location: MESH_INTERNAL
  ports:
  - number: 80
    name: http-httpd
    protocol: TCP
    targetPort: 80
  resolution: STATIC
  workloadSelector:
    labels:
      app: httpd
      type: vm
```



清理：

kubectl delete workloadgroup httpd -n vm-httpd

kubectl delete se   httpd-se -n vm-httpd



vm:

reboot



```
[root@node01 httpGet]# kubectl get workloadentries.networking.istio.io -n vm-httpd httpd-192.168.229.147-network2  -o yaml
apiVersion: networking.istio.io/v1beta1
kind: WorkloadEntry
metadata:
  annotations:
    istio.io/autoRegistrationGroup: httpd
    istio.io/connectedAt: "2021-09-15T02:27:16.22716924Z"
    istio.io/workloadController: istiod-c8479896c-9tmqc
    proxy.istio.io/health-checks-enabled: "true"
    test: test
  creationTimestamp: "2021-09-15T02:28:16Z"
  generation: 1
  labels:
    app: httpd
    service.istio.io/canonical-name: httpd
    service.istio.io/canonical-version: latest
    type: vm
  name: httpd-192.168.229.147-network2
  namespace: vm-httpd
  ownerReferences:
  - apiVersion: networking.istio.io/v1alpha3
    controller: true
    kind: WorkloadGroup
    name: httpd
    uid: 6f1cefeb-14f8-406c-a1d4-3fe0b6fb8037
  resourceVersion: "325443"
  uid: c4b139ba-e98a-4481-a989-42611744d969
spec:
  address: 192.168.229.147
  labels:
    app: httpd
    service.istio.io/canonical-name: httpd
    service.istio.io/canonical-version: latest
    type: vm
  network: network2
  serviceAccount: sa-httpd
status:
  conditions:
  - lastProbeTime: "2021-09-15T02:28:21.019041648Z"
    lastTransitionTime: "2021-09-15T02:28:21.019042209Z"
    status: "True"
    type: Healthy
```



![1631673004(1)](images\1631673004(1).jpg)

![1631673049(1)](images\1631673049(1).jpg)



![1631673080(1)](images\1631673080(1).jpg)





### exec

workloadgroups/probe/exec/wlg-probe-exec.yaml

kubectl apply -f wlg-probe-exec.yaml -n vm-httpd

```
apiVersion: networking.istio.io/v1alpha3
kind: WorkloadGroup
metadata:
  name: httpd
  namespace: vm-httpd
spec:
  metadata:
    labels:
      app: httpd
      type: vm
    annotations:
      test: test
  probe:
    periodSeconds: 5
    initialDelaySeconds: 5
    successThreshold: 1
    failureThreshold: 3
    timeoutSeconds: 2
    exec:
      command: 
      - echo
      - ok
  template:
    serviceAccount: sa-httpd
    network: network2
      
```



istioctl x workload entry configure -f wlg-probe-exec.yaml  -o . --clusterID cluster1 --autoregister

```
scp cluster.env  hosts  istio-token  mesh.yaml  root-cert.pem root@192.168.229.146:/root


mkdir -p /etc/certs
\cp "${HOME}"/root-cert.pem /etc/certs/root-cert.pem -f

mkdir -p /var/run/secrets/tokens
\cp "${HOME}"/istio-token /var/run/secrets/tokens/istio-token -f

mkdir /var/lib/istio/envoy/ -p

\cp "${HOME}"/cluster.env /var/lib/istio/envoy/cluster.env  -f

\cp "${HOME}"/mesh.yaml /etc/istio/config/mesh  -f

sh -c 'cat $(eval echo ~$SUDO_USER)/hosts >> /etc/hosts'

 mkdir -p /etc/istio/proxy

chown -R istio-proxy /var/lib/istio /etc/certs /etc/istio/proxy /etc/istio/config /var/run/secrets /etc/certs/root-cert.pem

 systemctl restart istio 
 systemctl start httpd
```

创建se

workloadgroups/probe/httpGet/se-httpd.yaml

 kubectl apply -f se-httpd.yaml -n vm-httpd 

```
apiVersion: networking.istio.io/v1beta1
kind: ServiceEntry
metadata:
  name: httpd-se
spec:
  hosts:
  - httpd.vm.demo
  addresses:
  - 192.168.229.11
  location: MESH_INTERNAL
  ports:
  - number: 80
    name: http-httpd
    protocol: TCP
    targetPort: 80
  resolution: STATIC
  workloadSelector:
    labels:
      app: httpd
      type: vm
```



清理：

kubectl delete workloadgroup httpd -n vm-httpd

kubectl delete se   httpd-se -n vm-httpd



vm:

reboot



![1631674952(1)](images\1631674952(1).jpg)



![1631675000(1)](images\1631675000(1).jpg)



![1631675514(1)](images\1631675514(1).jpg)

### tcpSocket

workloadgroups/probe/tcpSocket/wlg-probe-tcpSocket.yaml

kubectl apply -f wlg-probe-tcpSocket.yaml -n vm-httpd

```
apiVersion: networking.istio.io/v1alpha3
kind: WorkloadGroup
metadata:
  name: httpd
  namespace: vm-httpd
spec:
  metadata:
    labels:
      app: httpd
      type: vm
    annotations:
      test: test
  probe:
    periodSeconds: 5
    initialDelaySeconds: 5
    successThreshold: 1
    failureThreshold: 3
    timeoutSeconds: 2
    tcpSocket:
      port: 80
  template:
    serviceAccount: sa-httpd
    network: network2
      
```



istioctl x workload entry configure -f wlg-probe-tcpSocket.yaml  -o . --clusterID cluster1 --autoregister

```
scp cluster.env  hosts  istio-token  mesh.yaml  root-cert.pem root@192.168.229.146:/root


mkdir -p /etc/certs
\cp "${HOME}"/root-cert.pem /etc/certs/root-cert.pem -f

mkdir -p /var/run/secrets/tokens
\cp "${HOME}"/istio-token /var/run/secrets/tokens/istio-token -f

mkdir /var/lib/istio/envoy/ -p

\cp "${HOME}"/cluster.env /var/lib/istio/envoy/cluster.env  -f

\cp "${HOME}"/mesh.yaml /etc/istio/config/mesh  -f

sh -c 'cat $(eval echo ~$SUDO_USER)/hosts >> /etc/hosts'

 mkdir -p /etc/istio/proxy

chown -R istio-proxy /var/lib/istio /etc/certs /etc/istio/proxy /etc/istio/config /var/run/secrets /etc/certs/root-cert.pem

 systemctl restart istio 
 systemctl start httpd
```

创建se

workloadgroups/probe/tcpSocket/se-httpd.yaml

 kubectl apply -f se-httpd.yaml -n vm-httpd 

```
apiVersion: networking.istio.io/v1beta1
kind: ServiceEntry
metadata:
  name: httpd-se
spec:
  hosts:
  - httpd.vm.demo
  addresses:
  - 192.168.229.11
  location: MESH_INTERNAL
  ports:
  - number: 80
    name: http-httpd
    protocol: TCP
    targetPort: 80
  resolution: STATIC
  workloadSelector:
    labels:
      app: httpd
      type: vm
```



清理：

kubectl delete workloadgroup httpd -n vm-httpd

kubectl delete se   httpd-se -n vm-httpd



vm:

reboot



![1631675874(1)](images\1631675874(1).jpg)



![1631675917(1)](images\1631675917(1).jpg)

![1631675950(1)](images\1631675950(1).jpg)

## template

| Field            | Type     | Description                                                  | Required |
| ---------------- | -------- | ------------------------------------------------------------ | -------- |
| `address`        | `string` | Address associated with the network endpoint without the port. Domain names can be used if and only if the resolution is set to DNS, and must be fully-qualified without wildcards. Use the form unix:///absolute/path/to/socket for Unix domain socket endpoints. | Yes      |
| `ports`          | `map`    | Set of ports associated with the endpoint. If the port map is specified, it must be a map of servicePortName to this endpoint’s port, such that traffic to the service port will be forwarded to the endpoint port that maps to the service’s portName. If omitted, and the targetPort is specified as part of the service’s port specification, traffic to the service port will be forwarded to one of the endpoints on the specified `targetPort`. If both the targetPort and endpoint’s port map are not specified, traffic to a service port will be forwarded to one of the endpoints on the same port.**NOTE 1:** Do not use for `unix://` addresses.**NOTE 2:** endpoint port map takes precedence over targetPort. | No       |
| `labels`         | `map`    | One or more labels associated with the endpoint.             | No       |
| `network`        | `string` | Network enables Istio to group endpoints resident in the same L3 domain/network. All endpoints in the same network are assumed to be directly reachable from one another. When endpoints in different networks cannot reach each other directly, an Istio Gateway can be used to establish connectivity (usually using the `AUTO_PASSTHROUGH` mode in a Gateway Server). This is an advanced configuration used typically for spanning an Istio mesh over multiple clusters. | No       |
| `locality`       | `string` | The locality associated with the endpoint. A locality corresponds to a failure domain (e.g., country/region/zone). Arbitrary failure domain hierarchies can be represented by separating each encapsulating failure domain by /. For example, the locality of an an endpoint in US, in US-East-1 region, within availability zone az-1, in data center rack r11 can be represented as us/us-east-1/az-1/r11. Istio will configure the sidecar to route to endpoints within the same locality as the sidecar. If none of the endpoints in the locality are available, endpoints parent locality (but within the same network ID) will be chosen. For example, if there are two endpoints in same network (networkID “n1”), say e1 with locality us/us-east-1/az-1/r11 and e2 with locality us/us-east-1/az-2/r12, a sidecar from us/us-east-1/az-1/r11 locality will prefer e1 from the same locality over e2 from a different locality. Endpoint e2 could be the IP associated with a gateway (that bridges networks n1 and n2), or the IP associated with a standard service endpoint. | No       |
| `weight`         | `uint32` | The load balancing weight associated with the endpoint. Endpoints with higher weights will receive proportionally higher traffic. | No       |
| `serviceAccount` | `string` | The service account associated with the workload if a sidecar is present in the workload. The service account must be present in the same namespace as the configuration ( WorkloadEntry or a ServiceEntry) | No       |

### address

workloadgroups/template/address/wlg-template-address.yaml

kubectl apply -f wlg-template-address.yaml -n vm-httpd

```
apiVersion: networking.istio.io/v1alpha3
kind: WorkloadGroup
metadata:
  name: httpd
  namespace: vm-httpd
spec:
  metadata:
    labels:
      app: httpd
      type: vm
    annotations:
      test: test
  probe:
    periodSeconds: 5
    initialDelaySeconds: 5
    successThreshold: 1
    failureThreshold: 3
    timeoutSeconds: 2
    tcpSocket:
      port: 80
  template:
    serviceAccount: sa-httpd
    network: network2
    address: 192.168.229.146
      
```



istioctl x workload entry configure -f wlg-template-address.yaml  -o . --clusterID cluster1 --autoregister

```
scp cluster.env  hosts  istio-token  mesh.yaml  root-cert.pem root@192.168.229.146:/root


mkdir -p /etc/certs
\cp "${HOME}"/root-cert.pem /etc/certs/root-cert.pem -f

mkdir -p /var/run/secrets/tokens
\cp "${HOME}"/istio-token /var/run/secrets/tokens/istio-token -f

mkdir /var/lib/istio/envoy/ -p

\cp "${HOME}"/cluster.env /var/lib/istio/envoy/cluster.env  -f

\cp "${HOME}"/mesh.yaml /etc/istio/config/mesh  -f

sh -c 'cat $(eval echo ~$SUDO_USER)/hosts >> /etc/hosts'

 mkdir -p /etc/istio/proxy

chown -R istio-proxy /var/lib/istio /etc/certs /etc/istio/proxy /etc/istio/config /var/run/secrets /etc/certs/root-cert.pem

 systemctl restart istio 
 systemctl start httpd
```

创建se

workloadgroups/telplate/address/se-httpd.yaml

 kubectl apply -f se-httpd.yaml -n vm-httpd 

```
apiVersion: networking.istio.io/v1beta1
kind: ServiceEntry
metadata:
  name: httpd-se
spec:
  hosts:
  - httpd.vm.demo
  addresses:
  - 192.168.229.11
  location: MESH_INTERNAL
  ports:
  - number: 80
    name: http-httpd
    protocol: TCP
    targetPort: 80
  resolution: STATIC
  workloadSelector:
    labels:
      app: httpd
      type: vm
```



清理：

kubectl delete workloadgroup httpd -n vm-httpd

kubectl delete se   httpd-se -n vm-httpd



vm:

reboot



![1631676229(1)](images\1631676229(1).jpg)



![1631676268(1)](images\1631676268(1).jpg)



![1631676301(1)](images\1631676301(1).jpg)



### ports

workloadgroups/template/ports/wlg-template-ports.yaml

kubectl apply -f wlg-template-ports.yaml -n vm-httpd

```
apiVersion: networking.istio.io/v1alpha3
kind: WorkloadGroup
metadata:
  name: httpd
  namespace: vm-httpd
spec:
  metadata:
    labels:
      app: httpd
      type: vm
    annotations:
      test: test
  probe:
    periodSeconds: 5
    initialDelaySeconds: 5
    successThreshold: 1
    failureThreshold: 3
    timeoutSeconds: 2
    tcpSocket:
      port: 80
  template:
    serviceAccount: sa-httpd
    network: network2
    ports:
      http: 80
      
```



istioctl x workload entry configure -f wlg-template-ports.yaml  -o . --clusterID cluster1 --autoregister

```
scp cluster.env  hosts  istio-token  mesh.yaml  root-cert.pem root@192.168.229.146:/root

新版本不需要执行
需要执行下面命令修改mesh.yaml,不然报错
find . -type f -exec sed -i 's/"\[{\\"name\\":\\"http\\",\\"containerPort\\":80,\\"protocol\\":\\"\\"}\]"/[{"name":"http","containerPort":9080,"protocol":""}]/g' {} +


mkdir -p /etc/certs
\cp "${HOME}"/root-cert.pem /etc/certs/root-cert.pem -f

mkdir -p /var/run/secrets/tokens
\cp "${HOME}"/istio-token /var/run/secrets/tokens/istio-token -f

mkdir /var/lib/istio/envoy/ -p

\cp "${HOME}"/cluster.env /var/lib/istio/envoy/cluster.env  -f

\cp "${HOME}"/mesh.yaml /etc/istio/config/mesh  -f

sh -c 'cat $(eval echo ~$SUDO_USER)/hosts >> /etc/hosts'

 mkdir -p /etc/istio/proxy

chown -R istio-proxy /var/lib/istio /etc/certs /etc/istio/proxy /etc/istio/config /var/run/secrets /etc/certs/root-cert.pem

 systemctl restart istio 
 systemctl start httpd
```

创建se

workloadgroups/template/ports/se-httpd.yaml

 kubectl apply -f se-httpd.yaml -n vm-httpd 

```
apiVersion: networking.istio.io/v1beta1
kind: ServiceEntry
metadata:
  name: httpd-se
spec:
  hosts:
  - httpd.vm.demo
  addresses:
  - 192.168.229.11
  location: MESH_INTERNAL
  ports:
  - number: 80
    name: http-httpd
    protocol: TCP
    targetPort: 80
  resolution: STATIC
  workloadSelector:
    labels:
      app: httpd
      type: vm
```



清理：

kubectl delete workloadgroup httpd -n vm-httpd

kubectl delete se   httpd-se -n vm-httpd



vm:

reboot



```
[root@node01 sentinel-cluster-server-envoy-rls]# kubectl get workloadentries.networking.istio.io -n vm-httpd httpd-192.168.229.147-network2  -o yaml
apiVersion: networking.istio.io/v1beta1
kind: WorkloadEntry
metadata:
  annotations:
    istio.io/autoRegistrationGroup: httpd
    istio.io/connectedAt: "2021-09-15T03:27:33.090388591Z"
    istio.io/workloadController: istiod-c8479896c-9tmqc
    proxy.istio.io/health-checks-enabled: "true"
    test: test
  creationTimestamp: "2021-09-15T03:28:32Z"
  generation: 1
  labels:
    app: httpd
    service.istio.io/canonical-name: httpd
    service.istio.io/canonical-version: latest
    type: vm
  name: httpd-192.168.229.147-network2
  namespace: vm-httpd
  ownerReferences:
  - apiVersion: networking.istio.io/v1alpha3
    controller: true
    kind: WorkloadGroup
    name: httpd
    uid: 6f1cefeb-14f8-406c-a1d4-3fe0b6fb8037
  resourceVersion: "332566"
  uid: f9680d92-cd11-4d1e-a40d-522f0bd2937e
spec:
  address: 192.168.229.147
  labels:
    app: httpd
    service.istio.io/canonical-name: httpd
    service.istio.io/canonical-version: latest
    type: vm
  network: network2
  ports:
    http: 80
  serviceAccount: sa-httpd
status:
  conditions:
  - lastProbeTime: "2021-09-15T03:28:37.724841119Z"
    lastTransitionTime: "2021-09-15T03:28:37.724841722Z"
    status: "True"
    type: Healthy
```

![1631676607(1)](images\1631676607(1).jpg)



![1631676690(1)](images\1631676690(1).jpg)

![1631676718(1)](images\1631676718(1).jpg)

### labels

workloadgroups/template/labels/wlg-template-labels.yaml

kubectl apply -f wlg-template-labels.yaml -n vm-httpd

```
apiVersion: networking.istio.io/v1alpha3
kind: WorkloadGroup
metadata:
  name: httpd
  namespace: vm-httpd
spec:
  metadata:
    labels:
      app: httpd
      type: vm
    annotations:
      test: test
  probe:
    periodSeconds: 5
    initialDelaySeconds: 5
    successThreshold: 1
    failureThreshold: 3
    timeoutSeconds: 2
    tcpSocket:
      port: 80
  template:
    serviceAccount: sa-httpd
    network: network2
    address: 192.168.229.146
    labels:
      testLbel: testLabel
      
```



istioctl x workload entry configure -f wlg-template-labels.yaml  -o . --clusterID cluster1 --autoregister

```
scp cluster.env  hosts  istio-token  mesh.yaml  root-cert.pem root@192.168.229.146:/root


mkdir -p /etc/certs
\cp "${HOME}"/root-cert.pem /etc/certs/root-cert.pem -f

mkdir -p /var/run/secrets/tokens
\cp "${HOME}"/istio-token /var/run/secrets/tokens/istio-token -f

mkdir /var/lib/istio/envoy/ -p

\cp "${HOME}"/cluster.env /var/lib/istio/envoy/cluster.env  -f

\cp "${HOME}"/mesh.yaml /etc/istio/config/mesh  -f

sh -c 'cat $(eval echo ~$SUDO_USER)/hosts >> /etc/hosts'

 mkdir -p /etc/istio/proxy

chown -R istio-proxy /var/lib/istio /etc/certs /etc/istio/proxy /etc/istio/config /var/run/secrets /etc/certs/root-cert.pem

 systemctl restart istio 
 systemctl start httpd
```

创建se

workloadgroups/probe/httpGet/se-httpd.yaml

 kubectl apply -f se-httpd.yaml -n vm-httpd 

```
apiVersion: networking.istio.io/v1beta1
kind: ServiceEntry
metadata:
  name: httpd-se
spec:
  hosts:
  - httpd.vm.demo
  addresses:
  - 192.168.229.11
  location: MESH_INTERNAL
  ports:
  - number: 80
    name: http-httpd
    protocol: TCP
    targetPort: 80
  resolution: STATIC
  workloadSelector:
    labels:
      app: httpd
      type: vm
```



清理：

kubectl delete workloadgroup httpd -n vm-httpd

kubectl delete se   httpd-se -n vm-httpd



vm:

reboot



```
[root@node01 sentinel-cluster-server-envoy-rls]# kubectl get workloadentries.networking.istio.io -n vm-httpd httpd-192.168.229.147-network2  -o yaml
apiVersion: networking.istio.io/v1beta1
kind: WorkloadEntry
metadata:
  annotations:
    istio.io/autoRegistrationGroup: httpd
    istio.io/connectedAt: "2021-09-15T03:34:09.907385276Z"
    istio.io/workloadController: istiod-c8479896c-9tmqc
    proxy.istio.io/health-checks-enabled: "true"
    test: test
  creationTimestamp: "2021-09-15T03:35:10Z"
  generation: 1
  labels:
    app: httpd
    service.istio.io/canonical-name: httpd
    service.istio.io/canonical-version: latest
    testLbel: testLabel
    type: vm
  name: httpd-192.168.229.147-network2
  namespace: vm-httpd
  ownerReferences:
  - apiVersion: networking.istio.io/v1alpha3
    controller: true
    kind: WorkloadGroup
    name: httpd
    uid: 6f1cefeb-14f8-406c-a1d4-3fe0b6fb8037
  resourceVersion: "333350"
  uid: 672ff72b-71f7-456b-8fa8-a8b2e0d6230d
spec:
  address: 192.168.229.147
  labels:
    app: httpd
    service.istio.io/canonical-name: httpd
    service.istio.io/canonical-version: latest
    testLbel: testLabel
    type: vm
  network: network2
  serviceAccount: sa-httpd
status:
  conditions:
  - lastProbeTime: "2021-09-15T03:35:14.774811264Z"
    lastTransitionTime: "2021-09-15T03:35:14.774812021Z"
    status: "True"
    type: Healthy
```



![1631676967(1)](images\1631676967(1).jpg)

![1631676999(1)](images\1631676999(1).jpg)



![1631677055(1)](images\1631677055(1).jpg)

### network,serviceAccount

略



### locality

workloadgroups/template/locality/wlg-template-locality-146.yaml

kubectl apply -f wlg-template-locality-146.yaml -n vm-httpd



istioctl x workload entry configure -f wlg-template-locality-146.yaml  -o 146  --clusterID cluster1 --autoregister

```
apiVersion: networking.istio.io/v1alpha3
kind: WorkloadGroup
metadata:
  name: httpd-146
  namespace: vm-httpd
spec:
  metadata:
    labels:
      app: httpd
      type: vm
      region: us-central1
    annotations:
      test: test
  probe:
    periodSeconds: 5
    initialDelaySeconds: 5
    successThreshold: 1
    failureThreshold: 3
    timeoutSeconds: 2
    tcpSocket:
      port: 80
  template:
    serviceAccount: sa-httpd
    network: network2
    locality: "us-central1/z1/sz01"
      
```



```
cd 146
scp cluster.env  hosts  istio-token  mesh.yaml  root-cert.pem root@192.168.229.146:/root


mkdir -p /etc/certs
\cp "${HOME}"/root-cert.pem /etc/certs/root-cert.pem -f

mkdir -p /var/run/secrets/tokens
\cp "${HOME}"/istio-token /var/run/secrets/tokens/istio-token -f

mkdir /var/lib/istio/envoy/ -p

\cp "${HOME}"/cluster.env /var/lib/istio/envoy/cluster.env  -f

\cp "${HOME}"/mesh.yaml /etc/istio/config/mesh  -f

sh -c 'cat $(eval echo ~$SUDO_USER)/hosts >> /etc/hosts'

 mkdir -p /etc/istio/proxy

chown -R istio-proxy /var/lib/istio /etc/certs /etc/istio/proxy /etc/istio/config /var/run/secrets /etc/certs/root-cert.pem

 systemctl restart istio 
 systemctl start httpd
```

workloadgroups/template/locality/wlg-template-locality-147.yaml

kubectl apply -f wlg-template-locality-147.yaml -n vm-httpd



istioctl x workload entry configure -f wlg-template-locality-147.yaml  -o 147 --clusterID cluster1 --autoregister

```
apiVersion: networking.istio.io/v1alpha3
kind: WorkloadGroup
metadata:
  name: httpd-147
  namespace: vm-httpd
spec:
  metadata:
    labels:
      app: httpd
      type: vm
      region: us-central2
    annotations:
      test: test
  probe:
    periodSeconds: 5
    initialDelaySeconds: 5
    successThreshold: 1
    failureThreshold: 3
    timeoutSeconds: 2
    tcpSocket:
      port: 80
  template:
    serviceAccount: sa-httpd
    network: network2
    locality: "us-central2/z2/sz02"
      
```





```
cd 147
scp cluster.env  hosts  istio-token  mesh.yaml  root-cert.pem root@192.168.229.147:/root


mkdir -p /etc/certs
\cp "${HOME}"/root-cert.pem /etc/certs/root-cert.pem -f

mkdir -p /var/run/secrets/tokens
\cp "${HOME}"/istio-token /var/run/secrets/tokens/istio-token -f

mkdir /var/lib/istio/envoy/ -p

\cp "${HOME}"/cluster.env /var/lib/istio/envoy/cluster.env  -f

\cp "${HOME}"/mesh.yaml /etc/istio/config/mesh  -f

sh -c 'cat $(eval echo ~$SUDO_USER)/hosts >> /etc/hosts'

 mkdir -p /etc/istio/proxy

chown -R istio-proxy /var/lib/istio /etc/certs /etc/istio/proxy /etc/istio/config /var/run/secrets /etc/certs/root-cert.pem

 systemctl restart istio 
 systemctl start httpd
```

创建se

workloadgroups/template/locality/se-httpd.yaml

 kubectl apply -f se-httpd.yaml -n vm-httpd 

```
apiVersion: networking.istio.io/v1beta1
kind: ServiceEntry
metadata:
  name: httpd-se
spec:
  hosts:
  - httpd.vm.demo
  addresses:
  - 192.168.229.11
  location: MESH_INTERNAL
  ports:
  - number: 80
    name: http-httpd
    protocol: TCP
    targetPort: 80
  resolution: STATIC
  workloadSelector:
    labels:
      app: httpd
      type: vm
```



```
[root@node01 locality]# kubectl exec -it -n istio ratings-v1-fbdbfdc5b-dbz9g -- /bin/bash
node@ratings-v1-fbdbfdc5b-dbz9g:/opt/microservices$ curl httpd.vm.demo
146 welcome
node@ratings-v1-fbdbfdc5b-dbz9g:/opt/microservices$ curl httpd.vm.demo
147 welcome
node@ratings-v1-fbdbfdc5b-dbz9g:/opt/microservices$ curl httpd.vm.demo
147 welcome
node@ratings-v1-fbdbfdc5b-dbz9g:/opt/microservices$ curl httpd.vm.demo
146 welcome
node@ratings-v1-fbdbfdc5b-dbz9g:/opt/microservices$ curl httpd.vm.demo
147 welcome
node@ratings-v1-fbdbfdc5b-dbz9g:/opt/microservices$ curl httpd.vm.demo
146 welcome
```



dr-httpd-locality-failover.yaml

kubectl apply -f dr-httpd-locality-failover.yaml -n vm-httpd

```
apiVersion: networking.istio.io/v1alpha3
kind: DestinationRule
metadata:
  name: dr-httpd
spec:
  host: httpd.vm.demo
  trafficPolicy:
    loadBalancer:
      localityLbSetting:
        enabled: true
        failover:
        - from: us-central1
          to: us-central2
        - from: us-central2
          to: us-central1
    outlierDetection:
      consecutive5xxErrors: 1
      interval: 1s
      baseEjectionTime: 1m
```



```
[root@node01 locality]# kubectl exec -it -n istio ratings-v1-fbdbfdc5b-dbz9g -- /bin/bash
node@ratings-v1-fbdbfdc5b-dbz9g:/opt/microservices$ curl httpd.vm.demo
146 welcome
node@ratings-v1-fbdbfdc5b-dbz9g:/opt/microservices$ curl httpd.vm.demo
146 welcome
node@ratings-v1-fbdbfdc5b-dbz9g:/opt/microservices$ curl httpd.vm.demo
147 welcome
node@ratings-v1-fbdbfdc5b-dbz9g:/opt/microservices$ curl httpd.vm.demo
147 welcome
node@ratings-v1-fbdbfdc5b-dbz9g:/opt/microservices$ curl httpd.vm.demo
146 welcome
node@ratings-v1-fbdbfdc5b-dbz9g:/opt/microservices$ curl httpd.vm.demo
146 welcome
node@ratings-v1-fbdbfdc5b-dbz9g:/opt/microservices$ curl httpd.vm.demo
147 welcome
node@ratings-v1-fbdbfdc5b-dbz9g:/opt/microservices$ curl httpd.vm.demo
147 welcome
node@ratings-v1-fbdbfdc5b-dbz9g:/opt/microservices$ curl httpd.vm.demo
curl: (56) Recv failure: Connection reset by peer
node@ratings-v1-fbdbfdc5b-dbz9g:/opt/microservices$ curl httpd.vm.demo
147 welcome
node@ratings-v1-fbdbfdc5b-dbz9g:/opt/microservices$ curl httpd.vm.demo
147 welcome
node@ratings-v1-fbdbfdc5b-dbz9g:/opt/microservices$ curl httpd.vm.demo
147 welcome
node@ratings-v1-fbdbfdc5b-dbz9g:/opt/microservices$ curl httpd.vm.demo
147 welcome
node@ratings-v1-fbdbfdc5b-dbz9g:/opt/microservices$ curl httpd.vm.demo
147 welcome
node@ratings-v1-fbdbfdc5b-dbz9g:/opt/microservices$ curl httpd.vm.demo
147 welcome
```

清理：

kubectl delete workloadgroup httpd-146 -n vm-httpd

kubectl delete workloadgroup httpd-147 -n vm-httpd

kubectl delete se   httpd-se -n vm-httpd

kubectl delete dr dr-httpd -n vm-httpd



vm:

reboot



```
[root@node01 sentinel-cluster-server-envoy-rls]# kubectl get workloadentries.networking.istio.io -n vm-httpd   httpd-146-192.168.229.146-network2  -o yaml
apiVersion: networking.istio.io/v1beta1
kind: WorkloadEntry
metadata:
  annotations:
    istio.io/autoRegistrationGroup: httpd-146
    istio.io/connectedAt: "2021-09-15T03:42:24.483087351Z"
    istio.io/workloadController: istiod-c8479896c-9tmqc
    proxy.istio.io/health-checks-enabled: "true"
    test: test
  creationTimestamp: "2021-09-15T03:43:24Z"
  generation: 1
  labels:
    app: httpd
    region: us-central1
    service.istio.io/canonical-name: httpd
    service.istio.io/canonical-version: latest
    type: vm
  name: httpd-146-192.168.229.146-network2
  namespace: vm-httpd
  ownerReferences:
  - apiVersion: networking.istio.io/v1alpha3
    controller: true
    kind: WorkloadGroup
    name: httpd-146
    uid: 9c4bd6e0-a00f-40a9-bcdb-03aa8ab2cdda
  resourceVersion: "334325"
  uid: 67c97d13-3f93-404b-843e-ff64bea527c7
spec:
  address: 192.168.229.146
  labels:
    app: httpd
    region: us-central1
    service.istio.io/canonical-name: httpd
    service.istio.io/canonical-version: latest
    type: vm
  network: network2
  serviceAccount: sa-httpd
status:
  conditions:
  - lastProbeTime: "2021-09-15T03:43:29.365222341Z"
    lastTransitionTime: "2021-09-15T03:43:29.365222828Z"
    status: "True"
    type: Healthy
```

```
[root@node01 sentinel-cluster-server-envoy-rls]# kubectl get workloadentries.networking.istio.io -n vm-httpd  httpd-147-192.168.229.147-network2 -o yaml
apiVersion: networking.istio.io/v1beta1
kind: WorkloadEntry
metadata:
  annotations:
    istio.io/autoRegistrationGroup: httpd-147
    istio.io/connectedAt: "2021-09-15T03:43:20.231824977Z"
    istio.io/workloadController: istiod-c8479896c-9tmqc
    proxy.istio.io/health-checks-enabled: "true"
    test: test
  creationTimestamp: "2021-09-15T03:44:20Z"
  generation: 1
  labels:
    app: httpd
    region: us-central2
    service.istio.io/canonical-name: httpd
    service.istio.io/canonical-version: latest
    type: vm
  name: httpd-147-192.168.229.147-network2
  namespace: vm-httpd
  ownerReferences:
  - apiVersion: networking.istio.io/v1alpha3
    controller: true
    kind: WorkloadGroup
    name: httpd-147
    uid: 054dbcb1-bff6-4e3c-b1c2-bdb956c7a19a
  resourceVersion: "334438"
  uid: f58f7711-4d59-457e-b123-ec297f334093
spec:
  address: 192.168.229.147
  labels:
    app: httpd
    region: us-central2
    service.istio.io/canonical-name: httpd
    service.istio.io/canonical-version: latest
    type: vm
  network: network2
  serviceAccount: sa-httpd
status:
  conditions:
  - lastProbeTime: "2021-09-15T03:44:24.861826587Z"
    lastTransitionTime: "2021-09-15T03:44:24.861827107Z"
    status: "True"
    type: Healthy
```

![1631677823(1)](images\1631677823(1).jpg)



![1631677966(1)](images\1631677966(1).jpg)



![1631678010(1)](images\1631678010(1).jpg)

### weight

workloadgroups/template/weight/wlg-template-weight-146.yaml

kubectl apply -f wlg-template-weight-146.yaml -n vm-httpd



istioctl x workload entry configure -f wlg-template-weight-146.yaml  -o 146  --clusterID cluster1 --autoregister

```
apiVersion: networking.istio.io/v1alpha3
kind: WorkloadGroup
metadata:
  name: httpd-146
  namespace: vm-httpd
spec:
  metadata:
    labels:
      app: httpd
      type: vm
      region: us-central1
    annotations:
      test: test
  probe:
    periodSeconds: 5
    initialDelaySeconds: 5
    successThreshold: 1
    failureThreshold: 3
    timeoutSeconds: 2
    tcpSocket:
      port: 80
  template:
    serviceAccount: sa-httpd
    network: network2
    weight: 20
      
```



```
cd 146
scp cluster.env  hosts  istio-token  mesh.yaml  root-cert.pem root@192.168.229.146:/root


mkdir -p /etc/certs
\cp "${HOME}"/root-cert.pem /etc/certs/root-cert.pem -f

mkdir -p /var/run/secrets/tokens
\cp "${HOME}"/istio-token /var/run/secrets/tokens/istio-token -f

mkdir /var/lib/istio/envoy/ -p

\cp "${HOME}"/cluster.env /var/lib/istio/envoy/cluster.env  -f

\cp "${HOME}"/mesh.yaml /etc/istio/config/mesh  -f

sh -c 'cat $(eval echo ~$SUDO_USER)/hosts >> /etc/hosts'

 mkdir -p /etc/istio/proxy

chown -R istio-proxy /var/lib/istio /etc/certs /etc/istio/proxy /etc/istio/config /var/run/secrets /etc/certs/root-cert.pem

 systemctl restart istio 
 systemctl start httpd
```

workloadgroups/template/weight/wlg-template-weight-147.yaml

kubectl apply -f wlg-template-weight-147.yaml -n vm-httpd



istioctl x workload entry configure -f wlg-template-weight-147.yaml  -o 147 --clusterID cluster1 --autoregister

```
apiVersion: networking.istio.io/v1alpha3
kind: WorkloadGroup
metadata:
  name: httpd-147
  namespace: vm-httpd
spec:
  metadata:
    labels:
      app: httpd
      type: vm
      region: us-central2
    annotations:
      test: test
  probe:
    periodSeconds: 5
    initialDelaySeconds: 5
    successThreshold: 1
    failureThreshold: 3
    timeoutSeconds: 2
    tcpSocket:
      port: 80
  template:
    serviceAccount: sa-httpd
    network: network2
    weight: 80
      
```





```
cd 147
scp cluster.env  hosts  istio-token  mesh.yaml  root-cert.pem root@192.168.229.147:/root


mkdir -p /etc/certs
\cp "${HOME}"/root-cert.pem /etc/certs/root-cert.pem -f

mkdir -p /var/run/secrets/tokens
\cp "${HOME}"/istio-token /var/run/secrets/tokens/istio-token -f

mkdir /var/lib/istio/envoy/ -p

\cp "${HOME}"/cluster.env /var/lib/istio/envoy/cluster.env  -f

\cp "${HOME}"/mesh.yaml /etc/istio/config/mesh  -f

sh -c 'cat $(eval echo ~$SUDO_USER)/hosts >> /etc/hosts'

 mkdir -p /etc/istio/proxy

chown -R istio-proxy /var/lib/istio /etc/certs /etc/istio/proxy /etc/istio/config /var/run/secrets /etc/certs/root-cert.pem

 systemctl restart istio 
 systemctl start httpd
```

创建se

workloadgroups/template/weight/se-httpd.yaml

 kubectl apply -f se-httpd.yaml -n vm-httpd 

```
apiVersion: networking.istio.io/v1beta1
kind: ServiceEntry
metadata:
  name: httpd-se
spec:
  hosts:
  - httpd.vm.demo
  addresses:
  - 192.168.229.11
  location: MESH_INTERNAL
  ports:
  - number: 80
    name: http-httpd
    protocol: TCP
    targetPort: 80
  resolution: STATIC
  workloadSelector:
    labels:
      app: httpd
      type: vm
```



```

```

清理：

kubectl delete workloadgroup httpd-146 -n vm-httpd

kubectl delete workloadgroup httpd-147 -n vm-httpd

kubectl delete se   httpd-se -n vm-httpd



vm:

reboot



```
[root@node01 weight]# kubectl get workloadentries.networking.istio.io -n vm-httpd httpd-146-192.168.229.146-network2  -o yaml
apiVersion: networking.istio.io/v1beta1
kind: WorkloadEntry
metadata:
  annotations:
    istio.io/autoRegistrationGroup: httpd-146
    istio.io/connectedAt: "2021-09-15T03:57:19.931795863Z"
    istio.io/workloadController: istiod-c8479896c-9tmqc
    proxy.istio.io/health-checks-enabled: "true"
    test: test
  creationTimestamp: "2021-09-15T03:58:19Z"
  generation: 1
  labels:
    app: httpd
    region: us-central1
    service.istio.io/canonical-name: httpd
    service.istio.io/canonical-version: latest
    type: vm
  name: httpd-146-192.168.229.146-network2
  namespace: vm-httpd
  ownerReferences:
  - apiVersion: networking.istio.io/v1alpha3
    controller: true
    kind: WorkloadGroup
    name: httpd-146
    uid: 6f0a58c3-6939-457d-8d4d-865b1bdc1745
  resourceVersion: "336089"
  uid: 4488a7f7-4bb1-438d-806f-b851678438aa
spec:
  address: 192.168.229.146
  labels:
    app: httpd
    region: us-central1
    service.istio.io/canonical-name: httpd
    service.istio.io/canonical-version: latest
    type: vm
  network: network2
  serviceAccount: sa-httpd
  weight: 20
status:
  conditions:
  - lastProbeTime: "2021-09-15T03:58:24.436171603Z"
    lastTransitionTime: "2021-09-15T03:58:24.436172212Z"
    status: "True"
    type: Healthy
```

```
[root@node01 weight]# kubectl get workloadentries.networking.istio.io -n vm-httpd httpd-147-192.168.229.147-network2  -o yaml
apiVersion: networking.istio.io/v1beta1
kind: WorkloadEntry
metadata:
  annotations:
    istio.io/autoRegistrationGroup: httpd-147
    istio.io/connectedAt: "2021-09-15T03:58:05.9378022Z"
    istio.io/workloadController: istiod-c8479896c-9tmqc
    proxy.istio.io/health-checks-enabled: "true"
    test: test
  creationTimestamp: "2021-09-15T03:59:05Z"
  generation: 1
  labels:
    app: httpd
    region: us-central2
    service.istio.io/canonical-name: httpd
    service.istio.io/canonical-version: latest
    type: vm
  name: httpd-147-192.168.229.147-network2
  namespace: vm-httpd
  ownerReferences:
  - apiVersion: networking.istio.io/v1alpha3
    controller: true
    kind: WorkloadGroup
    name: httpd-147
    uid: 4bae3f9b-0f57-4aa2-bb4b-808b046905e7
  resourceVersion: "336183"
  uid: e6b24aed-5a9a-4f73-a1a0-c9fd0bf977e9
spec:
  address: 192.168.229.147
  labels:
    app: httpd
    region: us-central2
    service.istio.io/canonical-name: httpd
    service.istio.io/canonical-version: latest
    type: vm
  network: network2
  serviceAccount: sa-httpd
  weight: 80
status:
  conditions:
  - lastProbeTime: "2021-09-15T03:59:10.507812410Z"
    lastTransitionTime: "2021-09-15T03:59:10.507813423Z"
    status: "True"
    type: Healthy
```

![1631678391(1)](images\1631678391(1).jpg)



![1631678423(1)](images\1631678423(1).jpg)

![1631678461(1)](images\1631678461(1).jpg)