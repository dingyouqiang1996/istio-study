

 <center><h2 style="color:red">istio多集群链路追踪，附实操视频</h2></center> 
## 理论篇

## 什么是可观测性

​      这里的可观察性主要指服务网格的可观察性，也就是需要观测服务网格中运行的微服务。为什么可观察性很重要，因为随着微服务架构的流行，一个系统可能运行成百上千微服务，如果系统出现故障，定位问题带来很大得问题。有了观测系统，就能更好的分析问题发生的原因，已经更好的监控告警。服务网格可观察性主要分为三个大类，分别是log，metrics，tracing。log是指将分布式系统的日志收集起来集中存储，用于日志分析，常用的工具如efk。metrics是指收集服务网格的监控指标，进行监控告警，常用工具比如prometheus。tracing是指分布式链路追踪，用于可视化显示服务调用的依赖关系，及获取延迟数据，常用工具如zipkin，jaeger等。本文关注的是tracing，这里我们用到了zipkin作为tracing工具。

## 什么是链路追踪

​       在分布式系统，尤其是微服务系统中，一次外部请求往往需要内部多个模块，多个中间件，多台机器的相互调用才能完成。在这一系列的调用中，可能有些是串行的，而有些是并行的。在这种情况下，我们如何才能确定这整个请求调用了哪些应用？哪些模块？哪些节点？以及它们的先后顺序和各部分的性能如何呢？ 

​      链路追踪是分布式系统下的一个概念，它的目的就是要解决上面所提出的问题，也就是将一次分布式请求还原成调用链路，将一次分布式请求的调用情况集中展示，比如，各个服务节点上的耗时、请求具体到达哪台机器上、每个服务节点的请求状态等等。

## 多集群链路追踪

​      我们这里要演示的链路追踪不是单个istio集群的，而是多个istio集群的。我们把多个istio集群部署成一个联邦的istio集群，把多个集群的tracing数据在zipkin集中存储分析。单个istio集群的链路追踪相对比较简单，只需配置istio的comfigmap就行，多个集群考虑到集群的部署方式有很多，需要所有proxy将信息传送到统一的一个zipkin，相对来说复杂一点。这里我们展示两集群istio联邦和三集群istio联邦，一共14个案例。有关集群部署的方法，我已经在上一篇文章中详细介绍，这里我们直接用就行了，不进行展开。



## 实操篇

### 环境说明

两集群部署是用的机子是：

cluster1

192.168.229.128   master

192.168.229.129   master

192.168.229.130   node

cluster2

192.168.229.131 master

192.168.229.132  master

192.168.229.133 node



三集群部署用的机子是;

cluster1

192.168.229.137  master

192.168.229.138  master

192.168.229.139  node

cluster2

192.168.229.140  master

192.168.229.141  master

192.168.229.142  node

cluster3

192.168.229.143  master

192.168.229.144  master

192.168.229.145  node



k8s版本

```
[root@node01 ~]# kubectl version --short
Client Version: v1.21.0
Server Version: v1.21.0
```



istio版本

```
[root@node01 ~]# istioctl version
client version: 1.11.2
control plane version: 1.11.2
data plane version: none
```



### 两集群准备

首先需要创建root-ca，多个istio集群的root-ca必须是一样的：

```
cluster1:
 mkdir -p certs
 make -f ../tools/certs/Makefile.selfsigned.mk root-ca
 make -f ../tools/certs/Makefile.selfsigned.mk cluster1-cacerts
 make -f ../tools/certs/Makefile.selfsigned.mk cluster2-cacerts
 scp -r cluster2 root@192.168.229.131:/root/cluster2
 
 kubectl create namespace istio-system
 kubectl create secret generic cacerts -n istio-system \
      --from-file=cluster1/ca-cert.pem \
      --from-file=cluster1/ca-key.pem \
      --from-file=cluster1/root-cert.pem \
      --from-file=cluster1/cert-chain.pem
      
      
 cluster2:
  kubectl create namespace istio-system
 kubectl create secret generic cacerts -n istio-system \
      --from-file=cluster2/ca-cert.pem \
      --from-file=cluster2/ca-key.pem \
      --from-file=cluster2/root-cert.pem \
      --from-file=cluster2/cert-chain.pem
```



### 两集群

#### 单个控制面板

##### 在同一个网络中

![arch (D:/公众号/图文/05image/arch (3).jpg)](05image\arch (3).jpg)





部署步骤：

```
两集群网络联通
集群1
128,129,130
集群2
131,132,133

两个网络联通
128。129.130
route add -net 172.21.1.0 netmask 255.255.255.0 gw 192.168.229.131
route add -net 172.21.2.0 netmask 255.255.255.0 gw 192.168.229.133
route add -net 172.21.0.0 netmask 255.255.255.0 gw 192.168.229.132
route add -net 10.69.0.0 netmask 255.255.0.0 gw 192.168.229.131

131,132，133
route add -net 172.20.0.0 netmask 255.255.255.0 gw 192.168.229.128
route add -net 172.20.1.0 netmask 255.255.255.0 gw 192.168.229.129
route add -net 172.20.2.0 netmask 255.255.255.0 gw 192.168.229.130
route add -net 10.68.0.0 netmask 255.255.0.0 gw 192.168.229.128

生成部署operator文件
 cat <<EOF > cluster1.yaml
apiVersion: install.istio.io/v1alpha1
kind: IstioOperator
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
EOF

这里我设置的cluster1东西向网关的ip试192.168.229.100
如果用的是loadblance，可以用下面命令获取
#  export DISCOVERY_ADDRESS=$(kubectl  -n istio-system get svc istio-eastwestgateway  -o jsonpath='{.status.loadBalancer.ingress[0].ip}')
然后替换remotePilotAddress

cat <<EOF > cluster2.yaml
apiVersion: install.istio.io/v1alpha1
kind: IstioOperator
spec:
  profile: demo
  values:
    global:
      meshID: mesh1
      multiCluster:
        clusterName: cluster2
      network: network1
      remotePilotAddress: 192.168.229.100
  meshConfig:
    accessLogFile: /dev/stdout
    enableTracing: true
  components:
    egressGateways:
    - name: istio-egressgateway
      enabled: true
EOF  

传输部署文件到另一个集群
scp cluster2.yaml root@192.168.229.131:/root

安装cluster1
istioctl install -f cluster1.yaml

生成东西向网关
/root/istio-1.11.2/samples/multicluster/gen-eastwest-gateway.sh --mesh mesh1 --cluster cluster1 --network network1 |  istioctl  install -y  -f -

 
配置东西向网关ip 
 kubectl patch svc  -n istio-system istio-eastwestgateway -p '{"spec":{"externalIPs":["192.168.229.100"]}}'
  
 暴露istiod
  kubectl apply  -n istio-system -f /root/istio-1.11.2/samples/multicluster/expose-istiod.yaml

cluster2:
生成访问apiserver secret
 istioctl x create-remote-secret --name=cluster2  --server=https://192.168.229.131:6443 > remote-secret-cluster2.yaml
 
 传输secret到cluster1
 scp remote-secret-cluster2.yaml root@192.168.229.128:/root
 
 cluster1:
 应用secret
  kubectl apply -f remote-secret-cluster2.yaml
 
 cluster2:
 安装cluster2
 istioctl install  -f cluster2.yaml
 
 
 cluster1: 
  重启pod
 kubectl rollout restart deploy -n istio
 kubectl rollout restart deploy -n istio-system
 
 cluster2:
   重启pod
 kubectl rollout restart deploy -n istio
 kubectl rollout restart deploy -n istio-system
 

    
```



cluster1部署zipkin

```
 kubectl apply -f extras/zipkin.yaml -n istio-system
```

cluster1增加东西向网关端口

```
 kubectl edit svc -n istio-system istio-eastwestgateway
 
  - name: http-zipkin
    nodePort: 32197
    port: 15018
    protocol: TCP
    targetPort: 15018
```

cluster1:

暴露zipkin

visilazation/zipkin-gw-vs.yaml

 kubectl apply -f zipkin-gw-vs.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: Gateway
metadata:
  name: zipkin-gateway
spec:
  selector:
    istio: eastwestgateway
  servers:
    - port:
        name: http-zipkin
        number: 15018
        protocol: http        
      hosts:
        - "*"
---
apiVersion: networking.istio.io/v1alpha3
kind: VirtualService
metadata:
  name: zipkin-vs
spec:
  hosts:
  - "*"
  gateways:
  - zipkin-gateway
  http:
  - route:
    - destination:
        host: zipkin.istio-system.svc.cluster.local
        port:
          number: 9411
```





cluster1，cluster2: cm istio

```
[root@node01 ~]# kubectl get cm istio -n istio-system -o yaml
apiVersion: v1
data:
  mesh: |-
    accessLogFile: /dev/stdout
    enableTracing: true
    defaultConfig:
      discoveryAddress: istiod.istio-system.svc:15012
      meshId: mesh1
      proxyMetadata: {}
      tracing:
        sampling: 100
        zipkin:
          address: 192.168.229.100:15018
    enablePrometheusMerge: true
    enableTracing: true
    rootNamespace: istio-system
    trustDomain: cluster.local
  meshNetworks: 'networks: {}'
```





暴露服务：

kubectl port-forward --address 0.0.0.0 -n istio-system zipkin-6b8c6bdc56-m2b4f 9411:9411

清理：

```
cluster1:

kubectl delete vs istiod-vs -n istio-system
kubectl delete gw istiod-gateway -n istio-system
kubectl delete secret istio-remote-secret-cluster2 -n istio-system
kubectl delete gw zipkin-gateway -n istio-system
kubectl delete vs zipkin-vs -n istio-system
istioctl x uninstall -f cluster1.yaml

reboot

cluster2:

istioctl x uninstall -f cluster2.yaml

reboot

```



##### 在不同网络中

![arch (D:/公众号/图文/05image/arch (2).jpg)](05image\arch (2).jpg)





```
集群1
128,129,130
集群2
131,132,133

给istio-system namespace 打标签
cluster1:
kubectl  label namespace istio-system topology.istio.io/network=network1
cluster2:
kubectl  label namespace istio-system topology.istio.io/network=network2

cluster1:
生成istio operator部署文件
cat <<EOF > cluster1.yaml
apiVersion: install.istio.io/v1alpha1
kind: IstioOperator
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
EOF

这里我设置的cluster1东西向网关的ip试192.168.229.100
如果用的是loadblance，可以用下面命令获取
#  export DISCOVERY_ADDRESS=$(kubectl  -n istio-system get svc istio-eastwestgateway  -o jsonpath='{.status.loadBalancer.ingress[0].ip}')
然后替换remotePilotAddress

cat <<EOF > cluster2.yaml
apiVersion: install.istio.io/v1alpha1
kind: IstioOperator
spec:
  profile: demo
  values:
    global:
      meshID: mesh1
      multiCluster:
        clusterName: cluster2
      network: network2
      remotePilotAddress: 192.168.229.100
  meshConfig:
    accessLogFile: /dev/stdout
    enableTracing: true
  components:
    egressGateways:
    - name: istio-egressgateway
      enabled: true
EOF  

传输部署文件到另一个集群
scp cluster2.yaml root@192.168.229.131:/root

安装istio
istioctl install  -f cluster1.yaml

安装东西向网关
/root/istio-1.11.2/samples/multicluster/gen-eastwest-gateway.sh  --mesh mesh1 --cluster cluster1 --network network1 |  istioctl install -y  -f -
    
配置东西向网关ip 
 kubectl patch svc  -n istio-system istio-eastwestgateway -p '{"spec":{"externalIPs":["192.168.229.100"]}}'

暴露istiod
kubectl apply  -n istio-system -f /root/istio-1.11.2/samples/multicluster/expose-istiod.yaml
暴露服务
kubectl  apply -n istio-system -f /root/istio-1.11.2/samples/multicluster/expose-services.yaml

cluster2:
生成istiod访问apiserver secret
istioctl x create-remote-secret --name=cluster2  --server=https://192.168.229.131:6443 > remote-secret-cluster2.yaml

传输secret到cluster1
 scp remote-secret-cluster2.yaml root@192.168.229.128:/root
 
cluster1
安装secret
kubectl apply -f remote-secret-cluster2.yaml -n istio-system
 

cluster2:
部署cluster2
istioctl install  -f cluster2.yaml

生成东西向网关
/root/istio-1.11.2/samples/multicluster/gen-eastwest-gateway.sh --mesh mesh1 --cluster cluster2 --network network2 | istioctl install -y -f -

配置东西向网关ip 
 kubectl patch svc  -n istio-system istio-eastwestgateway -p '{"spec":{"externalIPs":["192.168.229.101"]}}'
 
暴露服务
kubectl  apply -n istio-system -f /root/istio-1.11.2/samples/multicluster/expose-services.yaml

重启pod
 kubectl rollout restart deploy -n istio
 kubectl rollout restart deploy -n istio-system
 
 cluster1:
 重启pod
 kubectl rollout restart deploy -n istio
 kubectl rollout restart deploy -n istio-system

```



cluster1部署zipkin

```
 kubectl apply -f extras/zipkin.yaml -n istio-system
```

cluster1增加东西向网关端口

```
 kubectl edit svc -n istio-system istio-eastwestgateway
 
  - name: http-zipkin
    nodePort: 32197
    port: 15018
    protocol: TCP
    targetPort: 15018
```

cluster1:

暴露zipkin

visilazation/zipkin-gw-vs.yaml

 kubectl apply -f zipkin-gw-vs.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: Gateway
metadata:
  name: zipkin-gateway
spec:
  selector:
    istio: eastwestgateway
  servers:
    - port:
        name: http-zipkin
        number: 15018
        protocol: http        
      hosts:
        - "*"
---
apiVersion: networking.istio.io/v1alpha3
kind: VirtualService
metadata:
  name: zipkin-vs
spec:
  hosts:
  - "*"
  gateways:
  - zipkin-gateway
  http:
  - route:
    - destination:
        host: zipkin.istio-system.svc.cluster.local
        port:
          number: 9411
```





cluster1,cluster2 : cm istio

```
[root@node01 ~]# kubectl get cm istio -n istio-system -o yaml
apiVersion: v1
data:
  mesh: |-
    accessLogFile: /dev/stdout
    enableTracing: true
    defaultConfig:
      discoveryAddress: istiod.istio-system.svc:15012
      meshId: mesh1
      proxyMetadata: {}
      tracing:
        sampling: 100
        zipkin:
          address: 192.168.229.100:15018
    enablePrometheusMerge: true
    enableTracing: true
    rootNamespace: istio-system
    trustDomain: cluster.local
  meshNetworks: 'networks: {}'
```



暴露服务：

kubectl port-forward --address 0.0.0.0 -n istio-system zipkin-6b8c6bdc56-m2b4f 9411:9411



清理：

```
cluster1:

kubectl  label namespace istio-system topology.istio.io/network-
kubectl delete vs istiod-vs -n istio-system
kubectl delete gw istiod-gateway -n istio-system
kubectl delete gw cross-network-gateway -n istio-system
kubectl delete secret istio-remote-secret-cluster2 -n istio-system
kubectl delete gw zipkin-gateway -n istio-system
kubectl delete vs zipkin-vs -n istio-system
istioctl x uninstall -f cluster1.yaml

reboot

cluster2:

kubectl  label namespace istio-system topology.istio.io/network-
kubectl delete gw cross-network-gateway -n istio-system
istioctl x uninstall -f cluster2.yaml

reboot

```



#### 两个控制面板

##### 在同一个网络中

![arch](D:/公众号/图文/05image/arch.jpg)




```
集群1
128,129,130
集群2
131,132,133

两个网络联通
128。129.130
route add -net 172.21.1.0 netmask 255.255.255.0 gw 192.168.229.131
route add -net 172.21.2.0 netmask 255.255.255.0 gw 192.168.229.133
route add -net 172.21.0.0 netmask 255.255.255.0 gw 192.168.229.132
route add -net 10.69.0.0 netmask 255.255.0.0 gw 192.168.229.131

131,132，133
route add -net 172.20.0.0 netmask 255.255.255.0 gw 192.168.229.128
route add -net 172.20.1.0 netmask 255.255.255.0 gw 192.168.229.129
route add -net 172.20.2.0 netmask 255.255.255.0 gw 192.168.229.130
route add -net 10.68.0.0 netmask 255.255.0.0 gw 192.168.229.128


cluster1:
生成istio安装operator文件
cat <<EOF > cluster1.yaml
apiVersion: install.istio.io/v1alpha1
kind: IstioOperator
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
EOF


生成istio安装operator文件
cat <<EOF > cluster2.yaml
apiVersion: install.istio.io/v1alpha1
kind: IstioOperator
spec:
  profile: demo
  values:
    global:
      meshID: mesh1
      multiCluster:
        clusterName: cluster2
      network: network1
  meshConfig:
    accessLogFile: /dev/stdout
    enableTracing: true
  components:
    egressGateways:
    - name: istio-egressgateway
      enabled: true
EOF

把部署文件传到cluster2
scp cluster2.yaml root@192.168.229.131:/root


cluster1:
生成访问apiserver secret
 istioctl x create-remote-secret --name=cluster1  --server=https://192.168.229.128:6443 > remote-secret-cluster1.yaml
 传输secret到cluster2
scp remote-secret-cluster1.yaml root@192.168.229.131:/root

cluster2
生成访问apiserver secret
 istioctl x create-remote-secret --name=cluster2  --server=https://192.168.229.131:6443 > remote-secret-cluster2.yaml
  传输secret到cluster2
 scp remote-secret-cluster2.yaml root@192.168.229.128:/root
 
 cluster1
 应用secret
 kubectl apply -f remote-secret-cluster2.yaml
 
 部署集群
 istioctl install  -f cluster1.yaml
  
  
  cluster2:
  应用secret
  kubectl apply -f remote-secret-cluster1.yaml
  部署集群
istioctl install  -f cluster2.yaml

 cluster1: 
  重启pod
 kubectl rollout restart deploy -n istio
 kubectl rollout restart deploy -n istio-system
 
 cluster2:
   重启pod
 kubectl rollout restart deploy -n istio
 kubectl rollout restart deploy -n istio-system
  


```



部署zipkin

```
 kubectl apply -f extras/zipkin.yaml -n istio-system
```

由于cluster2 dns 无法解析zipkin.istio-system，所以cluster1需要安装东西向网关

```
部署东西向网关
/root/istio-1.11.2/samples/multicluster/gen-eastwest-gateway.sh --mesh mesh1 --cluster cluster1 --network network1 | istioctl  install -y  -f -

配置东西向网关ip 
 kubectl patch svc  -n istio-system istio-eastwestgateway -p '{"spec":{"externalIPs":["192.168.229.100"]}}'
```

cluster1增加东西向网关端口

```
 kubectl edit svc -n istio-system istio-eastwestgateway
 
  - name: http-zipkin
    nodePort: 32197
    port: 15018
    protocol: TCP
    targetPort: 15018
```

cluster1:

暴露zipkin

visilazation/zipkin-gw-vs.yaml

 kubectl apply -f zipkin-gw-vs.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: Gateway
metadata:
  name: zipkin-gateway
spec:
  selector:
    istio: eastwestgateway
  servers:
    - port:
        name: http-zipkin
        number: 15018
        protocol: http        
      hosts:
        - "*"
---
apiVersion: networking.istio.io/v1alpha3
kind: VirtualService
metadata:
  name: zipkin-vs
spec:
  hosts:
  - "*"
  gateways:
  - zipkin-gateway
  http:
  - route:
    - destination:
        host: zipkin.istio-system.svc.cluster.local
        port:
          number: 9411
```





cluster1,cluster2: cm istio

```
[root@node01 ~]# kubectl get cm istio -n istio-system -o yaml
apiVersion: v1
data:
  mesh: |-
    accessLogFile: /dev/stdout
    enableTracing: true
    defaultConfig:
      discoveryAddress: istiod.istio-system.svc:15012
      meshId: mesh1
      proxyMetadata: {}
      tracing:
        sampling: 100
        zipkin:
          address: 192.168.229.100:15018
    enablePrometheusMerge: true
    enableTracing: true
    rootNamespace: istio-system
    trustDomain: cluster.local
  meshNetworks: 'networks: {}'
```

```
 cluster1: 
  重启pod
 kubectl rollout restart deploy -n istio
 
 cluster2:
   重启pod
 kubectl rollout restart deploy -n istio
```



暴露服务：

kubectl port-forward --address 0.0.0.0 -n istio-system zipkin-6b8c6bdc56-m2b4f 9411:9411

清理：

```
cluster1:

kubectl delete secret istio-remote-secret-cluster2 -n istio-system
kubectl delete gw zipkin-gateway -n istio-system
kubectl delete vs zipkin-vs -n istio-system
istioctl x uninstall -f cluster1.yaml


reboot

cluster2:


kubectl delete secret istio-remote-secret-cluster1 -n istio-system
istioctl x uninstall -f cluster2.yaml

reboot

```



##### 在不同网络中

![arch (D:/公众号/图文/05image/arch (1).jpg)](05image\arch (1).jpg)



```
集群1
128,129,130
集群2
131,132,133

给istio-system namespace打标签
cluster1:
 kubectl  label namespace istio-system topology.istio.io/network=network1
 cluster2:
 kubectl  label namespace istio-system topology.istio.io/network=network2
 
 cluster1:
 生成istio operator部署文件
 cat <<EOF > cluster1.yaml
apiVersion: install.istio.io/v1alpha1
kind: IstioOperator
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
EOF

生成istio operator部署文件
cat <<EOF > cluster2.yaml
apiVersion: install.istio.io/v1alpha1
kind: IstioOperator
spec:
  profile: demo
  values:
    global:
      meshID: mesh1
      multiCluster:
        clusterName: cluster2
      network: network2
  meshConfig:
    accessLogFile: /dev/stdout
    enableTracing: true
  components:
    egressGateways:
    - name: istio-egressgateway
      enabled: true
EOF

传输部署文件到cluster2
scp cluster2.yaml root@192.168.229.131:/root

生成监控apiserver secret
 istioctl x create-remote-secret --name=cluster1  --server=https://192.168.229.128:6443 > remote-secret-cluster1.yaml
 传输secret到cluster2
scp remote-secret-cluster1.yaml root@192.168.229.131:/root

cluster2
 生成监控apiserver secret
 istioctl x create-remote-secret --name=cluster2  --server=https://192.168.229.131:6443 > remote-secret-cluster2.yaml
 
 传输secret到cluster1
 scp remote-secret-cluster2.yaml root@192.168.229.128:/root

cluster1:
部署监控apiserver secret
kubectl apply -f remote-secret-cluster2.yaml

部署istio
istioctl install  -f cluster1.yaml

部署东西向网关
/root/istio-1.11.2/samples/multicluster/gen-eastwest-gateway.sh --mesh mesh1 --cluster cluster1 --network network1 | istioctl  install -y  -f -

配置东西向网关ip 
 kubectl patch svc  -n istio-system istio-eastwestgateway -p '{"spec":{"externalIPs":["192.168.229.100"]}}'
 
 暴露服务
 kubectl  apply -n istio-system -f /root/istio-1.11.2/samples/multicluster/expose-services.yaml
 

cluster2:
部署监控apiserver secret
kubectl apply -f remote-secret-cluster1.yaml

部署istio
 istioctl install -f cluster2.yaml

部署东西向网关
 /root/istio-1.11.2/samples/multicluster/gen-eastwest-gateway.sh --mesh mesh1 --cluster cluster2 --network network2 |  istioctl install -y -f -

配置东西向网关ip 
 kubectl patch svc  -n istio-system istio-eastwestgateway -p '{"spec":{"externalIPs":["192.168.229.101"]}}'
 
 暴露服务
 kubectl  apply -n istio-system -f /root/istio-1.11.2/samples/multicluster/expose-services.yaml
 
 cluster1
 重启pod
 kubectl rollout restart deploy -n istio
 kubectl rollout restart deploy -n istio-system
 
 cluster2:
 重启pod
 kubectl rollout restart deploy -n istio
 kubectl rollout restart deploy -n istio-system
 

 

```



cluster1:部署zipkin

```
 kubectl apply -f extras/zipkin.yaml -n istio-system
```

cluster1 : cm istio

```
[root@node01 ~]# kubectl get cm istio -n istio-system -o yaml
apiVersion: v1
data:
  mesh: |-
    accessLogFile: /dev/stdout
    enableTracing: true
    defaultConfig:
      discoveryAddress: istiod.istio-system.svc:15012
      meshId: mesh1
      proxyMetadata: {}
      tracing:
        sampling: 100
        zipkin:
          address: zipkin.istio-system:9411
    enablePrometheusMerge: true
    enableTracing: true
    rootNamespace: istio-system
    trustDomain: cluster.local
  meshNetworks: 'networks: {}'
```



cluster1:暴露服务：

kubectl port-forward --address 0.0.0.0 -n istio-system zipkin-6b8c6bdc56-m2b4f 9411:9411



增加东西向网关端口

```
 kubectl edit svc -n istio-system istio-eastwestgateway
 
  - name: http-zipkin
    nodePort: 32197
    port: 15018
    protocol: TCP
    targetPort: 15018
```

暴露zipkin到cluster2

visilazation/zipkin-gw-vs.yaml

 kubectl apply -f zipkin-gw-vs.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: Gateway
metadata:
  name: zipkin-gateway
spec:
  selector:
    istio: eastwestgateway
  servers:
    - port:
        name: http-zipkin
        number: 15018
        protocol: http        
      hosts:
        - "*"
---
apiVersion: networking.istio.io/v1alpha3
kind: VirtualService
metadata:
  name: zipkin-vs
spec:
  hosts:
  - "*"
  gateways:
  - zipkin-gateway
  http:
  - route:
    - destination:
        host: zipkin.istio-system.svc.cluster.local
        port:
          number: 9411
```







cluster2 : cm istio

```
[root@node01 ~]# kubectl get cm istio -n istio-system -o yaml
apiVersion: v1
data:
  mesh: |-
    accessLogFile: /dev/stdout
    enableTracing: true
    defaultConfig:
      discoveryAddress: istiod.istio-system.svc:15012
      meshId: mesh1
      proxyMetadata: {}
      tracing:
        sampling: 100
        zipkin:
          address: 192.168.229.100:15018
    enablePrometheusMerge: true
    enableTracing: true
    rootNamespace: istio-system
    trustDomain: cluster.local
  meshNetworks: 'networks: {}'
```



清理：

```
cluster1:

kubectl  label namespace istio-system topology.istio.io/network-
kubectl delete gw cross-network-gateway -n istio-system
kubectl delete secret istio-remote-secret-cluster2 -n istio-system
kubectl delete gw zipkin-gateway -n istio-system
kubectl delete vs zipkin-vs -n istio-system
istioctl x uninstall -f cluster1.yaml

reboot

cluster2:

kubectl  label namespace istio-system topology.istio.io/network-
kubectl delete gw cross-network-gateway -n istio-system
kubectl delete secret istio-remote-secret-cluster1 -n istio-system
istioctl x uninstall -f cluster2.yaml

reboot

```



### 三集群准备

首先需要创建root-ca，多个istio集群的root-ca必须是一样的：

```
cluster1:
mkdir -p certs
 make -f ../tools/certs/Makefile.selfsigned.mk root-ca
 make -f ../tools/certs/Makefile.selfsigned.mk cluster1-cacerts
 make -f ../tools/certs/Makefile.selfsigned.mk cluster2-cacerts
 make -f ../tools/certs/Makefile.selfsigned.mk cluster3-cacerts
 scp -r cluster2 root@192.168.229.140:/root/cluster2
  scp -r cluster3 root@192.168.229.140:/root/cluster3
 
 cluster1：
 kubectl create namespace istio-system
 kubectl create secret generic cacerts -n istio-system \
      --from-file=cluster1/ca-cert.pem \
      --from-file=cluster1/ca-key.pem \
      --from-file=cluster1/root-cert.pem \
      --from-file=cluster1/cert-chain.pem
      
  cluster2：
  kubectl create namespace istio-system
 kubectl create secret generic cacerts -n istio-system \
      --from-file=cluster2/ca-cert.pem \
      --from-file=cluster2/ca-key.pem \
      --from-file=cluster2/root-cert.pem \
      --from-file=cluster2/cert-chain.pem
 cluster3:
 kubectl create namespace istio-system
 kubectl create secret generic cacerts -n istio-system \
      --from-file=cluster3/ca-cert.pem \
      --from-file=cluster3/ca-key.pem \
      --from-file=cluster3/root-cert.pem \
      --from-file=cluster3/cert-chain.pem

```



### 三集群

#### 单控制面板

##### 单网络

![three-01](D:/公众号/图文/05image/three-01.bmp)





```
三个网络联通
集群1
137,138,139
集群2
140,141,142
集群3
143,144,145

网络联通
137,138,139
route add -net 172.21.2.0 netmask 255.255.255.0 gw 192.168.229.142
route add -net 172.21.0.0 netmask 255.255.255.0 gw 192.168.229.141
route add -net 172.21.1.0 netmask 255.255.255.0 gw 192.168.229.140

route add -net 172.22.2.0 netmask 255.255.255.0 gw 192.168.229.145
route add -net 172.22.0.0 netmask 255.255.255.0 gw 192.168.229.144
route add -net 172.22.1.0 netmask 255.255.255.0 gw 192.168.229.143

route add -net 10.70.0.0 netmask 255.255.0.0 gw 192.168.229.143
route add -net 10.69.0.0 netmask 255.255.0.0 gw 192.168.229.140

140,141,142
route add -net 172.20.2.0 netmask 255.255.255.0 gw 192.168.229.139
route add -net 172.20.0.0 netmask 255.255.255.0 gw 192.168.229.138
route add -net 172.20.1.0 netmask 255.255.255.0 gw 192.168.229.137

route add -net 172.22.2.0 netmask 255.255.255.0 gw 192.168.229.145
route add -net 172.22.0.0 netmask 255.255.255.0 gw 192.168.229.144
route add -net 172.22.1.0 netmask 255.255.255.0 gw 192.168.229.143

route add -net 10.70.0.0 netmask 255.255.0.0 gw 192.168.229.143
route add -net 10.68.0.0 netmask 255.255.0.0 gw 192.168.229.137


143,144,145
route add -net 172.21.2.0 netmask 255.255.255.0 gw 192.168.229.142
route add -net 172.21.0.0 netmask 255.255.255.0 gw 192.168.229.141
route add -net 172.21.1.0 netmask 255.255.255.0 gw 192.168.229.140

route add -net 172.20.2.0 netmask 255.255.255.0 gw 192.168.229.139
route add -net 172.20.0.0 netmask 255.255.255.0 gw 192.168.229.138
route add -net 172.20.1.0 netmask 255.255.255.0 gw 192.168.229.137

route add -net 10.69.0.0 netmask 255.255.0.0 gw 192.168.229.140
route add -net 10.68.0.0 netmask 255.255.0.0 gw 192.168.229.137



cluster1:
生成istio operator部署文件
 cat <<EOF > cluster1.yaml
apiVersion: install.istio.io/v1alpha1
kind: IstioOperator
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
EOF

这里我设置的cluster1东西向网关的ip试192.168.229.100
如果用的是loadblance，可以用下面命令获取
#  export DISCOVERY_ADDRESS=$(kubectl  -n istio-system get svc istio-eastwestgateway  -o jsonpath='{.status.loadBalancer.ingress[0].ip}')
然后替换remotePilotAddress

生成istio operator部署文件
cat <<EOF > cluster2.yaml
apiVersion: install.istio.io/v1alpha1
kind: IstioOperator
spec:
  profile: demo
  values:
    global:
      meshID: mesh1
      multiCluster:
        clusterName: cluster2
      network: network1
      remotePilotAddress: 192.168.229.100
  meshConfig:
    accessLogFile: /dev/stdout
    enableTracing: true
  components:
    egressGateways:
    - name: istio-egressgateway
      enabled: true
EOF  

传输部署文件到cluster2
scp cluster2.yaml root@192.168.229.140:/root


这里我设置的cluster1东西向网关的ip试192.168.229.100
如果用的是loadblance，可以用下面命令获取
#  export DISCOVERY_ADDRESS=$(kubectl  -n istio-system get svc istio-eastwestgateway  -o jsonpath='{.status.loadBalancer.ingress[0].ip}')
然后替换remotePilotAddress

生成istio operator部署文件
cat <<EOF > cluster3.yaml
apiVersion: install.istio.io/v1alpha1
kind: IstioOperator
spec:
  profile: demo
  values:
    global:
      meshID: mesh1
      multiCluster:
        clusterName: cluster3
      network: network1
      remotePilotAddress: 192.168.229.100
  meshConfig:
    accessLogFile: /dev/stdout
    enableTracing: true
  components:
    egressGateways:
    - name: istio-egressgateway
      enabled: true
EOF  

传输部署文件到cluster3
scp cluster3.yaml root@192.168.229.143:/root


部署istio
istioctl install -f cluster1.yaml

生成东西向网关
 /root/istio-1.11.2/samples/multicluster/gen-eastwest-gateway.sh --mesh mesh1 --cluster cluster1 --network network1 |  istioctl  install  -y -f -

 
配置东西向网关ip 
 kubectl patch svc  -n istio-system istio-eastwestgateway -p '{"spec":{"externalIPs":["192.168.229.100"]}}'
  
  暴露istiod
  kubectl apply  -n istio-system -f  /root/istio-1.11.2/samples/multicluster/expose-istiod.yaml
  
 
 
 cluster2:
 生成访问apiserver secret
 istioctl x create-remote-secret --name=cluster2  --server=https://192.168.229.140:6443 > remote-secret-cluster2.yaml
 
 传输secret到cluster1
 scp remote-secret-cluster2.yaml root@192.168.229.137:/root

 
 cluster3:
 生成访问apiserver secret
 istioctl x create-remote-secret --name=cluster3  --server=https://192.168.229.143:6443 > remote-secret-cluster3.yaml
 
 传输secret到cluster1
 scp remote-secret-cluster3.yaml root@192.168.229.137:/root
 
 cluster1
 应用secret
  kubectl apply -f remote-secret-cluster2.yaml
   kubectl apply -f remote-secret-cluster3.yaml
   
   
 
 cluster2:
 部署istio
 istioctl install  -f cluster2.yaml
 
 cluster3:
 部署istio
 istioctl install  -f cluster3.yaml
 
 cluster1:
 重启pod
 kubectl rollout restart deploy -n istio
 kubectl rollout restart deploy -n istio-system
 
 cluster2:
 重启pod
 kubectl rollout restart deploy -n istio
 kubectl rollout restart deploy -n istio-system
 
 cluster3:
 重启pod
 kubectl rollout restart deploy -n istio
 kubectl rollout restart deploy -n istio-system
 

    

```

cluster1:部署zipkin

```
 kubectl apply -f extras/zipkin.yaml -n istio-system
```

cluster1增加东西向网关端口

```
 
  kubectl patch svc  -n istio-system istio-eastwestgateway -p '{"spec":{"ports":[{"name": "http-zipkin", "nodePort": 32197,"port": 15018, "protocol": "TCP", "targetPort": 15018}]}}'
```

cluster1:

暴露zipkin

visilazation/zipkin-gw-vs.yaml

 kubectl apply -f zipkin-gw-vs.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: Gateway
metadata:
  name: zipkin-gateway
spec:
  selector:
    istio: eastwestgateway
  servers:
    - port:
        name: http-zipkin
        number: 15018
        protocol: http        
      hosts:
        - "*"
---
apiVersion: networking.istio.io/v1alpha3
kind: VirtualService
metadata:
  name: zipkin-vs
spec:
  hosts:
  - "*"
  gateways:
  - zipkin-gateway
  http:
  - route:
    - destination:
        host: zipkin.istio-system.svc.cluster.local
        port:
          number: 9411
```





cluster1，cluster2,cluster3: cm istio   

```
[root@node01 ~]# kubectl get cm istio -n istio-system -o yaml
apiVersion: v1
data:
  mesh: |-
    accessLogFile: /dev/stdout
    enableTracing: true
    defaultConfig:
      discoveryAddress: istiod.istio-system.svc:15012
      meshId: mesh1
      proxyMetadata: {}
      tracing:
        sampling: 100
        zipkin:
          address: 192.168.229.100:15018
    enablePrometheusMerge: true
    enableTracing: true
    rootNamespace: istio-system
    trustDomain: cluster.local
  meshNetworks: 'networks: {}'
  
  修改
        sampling: 100
        zipkin:
          address: 192.168.229.100:15018
```

```
 cluster1: 
  重启pod
 kubectl rollout restart deploy -n istio
 
 cluster2:
   重启pod
 kubectl rollout restart deploy -n istio
 
  
 cluster3:
   重启pod
 kubectl rollout restart deploy -n istio
```

我的集群的应用部署情况：

```
cluster1:
[root@node01 istio-teaching]# kubectl get pod -n istio
NAME                             READY   STATUS    RESTARTS   AGE
productpage-v1-655c9d8c9-dln7x   2/2     Running   0          2m50s
ratings-v1-86ccf5754f-bz867      2/2     Running   0          2m50s

cluster2:
[root@node01 ~]# kubectl get pod -n istio
NAME                          READY   STATUS    RESTARTS   AGE
reviews-v2-77f86758bd-9fb4n   2/2     Running   0          11m

cluster3:
[root@node01 ~]# kubectl get pod -n istio
NAME                          READY   STATUS    RESTARTS   AGE
details-v1-548fbfb4d5-2xhkk   2/2     Running   0          11m
ratings-v1-678964777c-wkg4c   2/2     Running   0          11m
reviews-v3-76857cf4bf-5vhck   2/2     Running   0          11m
```

暴露服务：

kubectl port-forward --address 0.0.0.0 -n istio-system zipkin-6b8c6bdc56-m2b4f 9411:9411



![1](44image\1.jpg)



清理：

```
cluster1:

kubectl delete secret istio-remote-secret-cluster2 -n istio-system
kubectl delete secret istio-remote-secret-cluster3 -n istio-system
kubectl delete gw zipkin-gateway -n istio-system
kubectl delete vs zipkin-vs -n istio-system
kubectl delete vs istiod-vs -n istio-system
kubectl delete gw istiod-gateway -n istio-system
istioctl x uninstall -f cluster1.yaml

reboot



cluster2:

istioctl x uninstall -f cluster2.yaml

reboot



cluster3:

istioctl x uninstall -f cluster3.yaml

reboot

```



##### 两网络

###### 两网关

![three-01-2](D:/公众号/图文/05image/three-01-2.bmp)



```
两个网络
network2 东西向网管可以在cluster2也可以在cluster3
cluster2有网关，cluster3没有网关
不建议使用，按地域负载均衡的时候会有问题

集群1
137,138,139
集群2
140,141,142
集群3
143,144,145

打通cluster2，cluster3网络
140,141,142
route add -net 172.22.2.0 netmask 255.255.255.0 gw 192.168.229.145
route add -net 172.22.0.0 netmask 255.255.255.0 gw 192.168.229.144
route add -net 172.22.1.0 netmask 255.255.255.0 gw 192.168.229.143

route add -net 10.70.0.0 netmask 255.255.0.0 gw 192.168.229.143


143,144,145
route add -net 172.21.2.0 netmask 255.255.255.0 gw 192.168.229.142
route add -net 172.21.0.0 netmask 255.255.255.0 gw 192.168.229.141
route add -net 172.21.1.0 netmask 255.255.255.0 gw 192.168.229.140

route add -net 10.69.0.0 netmask 255.255.0.0 gw 192.168.229.140

给isito-system namespace打标签
cluster1:
kubectl  label namespace istio-system topology.istio.io/network=network1

cluster1:
kubectl  label namespace istio-system topology.istio.io/network=network2

cluster1:
kubectl  label namespace istio-system topology.istio.io/network=network2

生成operator部署文件
cluster1:
cat <<EOF > cluster1.yaml
apiVersion: install.istio.io/v1alpha1
kind: IstioOperator
spec:
  profile: demo
  values:
    global:
      imagePullPolicy: IfNotPresent
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
EOF

这里我设置的cluster1东西向网关的ip试192.168.229.100
如果用的是loadblance，可以用下面命令获取
#  export DISCOVERY_ADDRESS=$(kubectl  -n istio-system get svc istio-eastwestgateway  -o jsonpath='{.status.loadBalancer.ingress[0].ip}')
然后替换remotePilotAddress

生成operator部署文件
cat <<EOF > cluster2.yaml
apiVersion: install.istio.io/v1alpha1
kind: IstioOperator
spec:
  profile: demo
  values:
    global:
      imagePullPolicy: IfNotPresent
      meshID: mesh1
      multiCluster:
        clusterName: cluster2
      network: network2
      remotePilotAddress: 192.168.229.100
  meshConfig:
    accessLogFile: /dev/stdout
    enableTracing: true
  components:
    egressGateways:
    - name: istio-egressgateway
      enabled: true
EOF

这里我设置的cluster1东西向网关的ip试192.168.229.100
如果用的是loadblance，可以用下面命令获取
#  export DISCOVERY_ADDRESS=$(kubectl  -n istio-system get svc istio-eastwestgateway  -o jsonpath='{.status.loadBalancer.ingress[0].ip}')
然后替换remotePilotAddress

生成operator部署文件
cat <<EOF > cluster3.yaml
apiVersion: install.istio.io/v1alpha1
kind: IstioOperator
spec:
  profile: demo
  values:
    global:
      imagePullPolicy: IfNotPresent
      meshID: mesh1
      multiCluster:
        clusterName: cluster3
      network: network2
      remotePilotAddress: 192.168.229.100
  meshConfig:
    accessLogFile: /dev/stdout
    enableTracing: true
  components:
    egressGateways:
    - name: istio-egressgateway
      enabled: true
EOF

把部署文件传到cluster2
scp cluster2.yaml root@192.168.229.140:/root
把部署文件传到cluster3
scp cluster3.yaml root@192.168.229.143:/root

部署cluster1
istioctl install  -f cluster1.yaml
部署东西向网关
/root/istio-1.11.2/samples/multicluster/gen-eastwest-gateway.sh  --mesh mesh1 --cluster cluster1 --network network1 |  istioctl install -y  -f -
    
 
配置东西向网关ip 
 kubectl patch svc  -n istio-system istio-eastwestgateway -p '{"spec":{"externalIPs":["192.168.229.100"]}}'
  
暴露istiod
kubectl apply  -n istio-system -f /root/istio-1.11.2/samples/multicluster/expose-istiod.yaml
暴露服务
kubectl  apply -n istio-system -f /root/istio-1.11.2/samples/multicluster/expose-services.yaml


cluster2:
生成监控apiserver secret
istioctl x create-remote-secret --name=cluster2  --server=https://192.168.229.140:6443 > remote-secret-cluster2.yaml
传输secret到cluster1
 scp remote-secret-cluster2.yaml root@192.168.229.137:/root


cluster3:
生成监控apiserver secret
istioctl x create-remote-secret --name=cluster3  --server=https://192.168.229.143:6443 > remote-secret-cluster3.yaml

传输secret到cluster1
 scp remote-secret-cluster3.yaml root@192.168.229.137:/root
 
 cluster1:
 应用监控apiserver secret
  kubectl apply -f remote-secret-cluster2.yaml
  kubectl apply -f remote-secret-cluster3.yaml


cluster2:
部署cluster2
istioctl install  -f cluster2.yaml
安装东西向网关
/root/istio-1.11.2/samples/multicluster/gen-eastwest-gateway.sh --mesh mesh1 --cluster cluster2 --network network2 | istioctl install -y  -f -

 
配置东西向网关ip 
 kubectl patch svc  -n istio-system istio-eastwestgateway -p '{"spec":{"externalIPs":["192.168.229.101"]}}'
  
暴露服务
kubectl  apply -n istio-system -f /root/istio-1.11.2/samples/multicluster/expose-services.yaml

 
 cluster3:
 部署cluster3
istioctl install  -f cluster3.yaml


cluster1:
重启pod
 kubectl rollout restart deploy -n istio
 kubectl rollout restart deploy -n istio-system
 
 cluster2:
 重启pod
 kubectl rollout restart deploy -n istio
 kubectl rollout restart deploy -n istio-system
 
 cluster1:
 重启pod
 kubectl rollout restart deploy -n istio
 kubectl rollout restart deploy -n istio-system

```

cluster1:部署zipkin

```
 kubectl apply -f extras/zipkin.yaml -n istio-system
```

cluster1增加东西向网关端口

```
 
  kubectl patch svc  -n istio-system istio-eastwestgateway -p '{"spec":{"ports":[{"name": "http-zipkin", "nodePort": 32197,"port": 15018, "protocol": "TCP", "targetPort": 15018}]}}'
```

cluster1:

暴露zipkin

visilazation/zipkin-gw-vs.yaml

 kubectl apply -f zipkin-gw-vs.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: Gateway
metadata:
  name: zipkin-gateway
spec:
  selector:
    istio: eastwestgateway
  servers:
    - port:
        name: http-zipkin
        number: 15018
        protocol: http        
      hosts:
        - "*"
---
apiVersion: networking.istio.io/v1alpha3
kind: VirtualService
metadata:
  name: zipkin-vs
spec:
  hosts:
  - "*"
  gateways:
  - zipkin-gateway
  http:
  - route:
    - destination:
        host: zipkin.istio-system.svc.cluster.local
        port:
          number: 9411
```





cluster1，cluster2,cluster3: cm istio   

```
[root@node01 ~]# kubectl get cm istio -n istio-system -o yaml
apiVersion: v1
data:
  mesh: |-
    accessLogFile: /dev/stdout
    enableTracing: true
    defaultConfig:
      discoveryAddress: istiod.istio-system.svc:15012
      meshId: mesh1
      proxyMetadata: {}
      tracing:
        sampling: 100
        zipkin:
          address: 192.168.229.100:15018
    enablePrometheusMerge: true
    enableTracing: true
    rootNamespace: istio-system
    trustDomain: cluster.local
  meshNetworks: 'networks: {}'
  
  修改
        sampling: 100
        zipkin:
          address: 192.168.229.100:15018
```

```
 cluster1: 
  重启pod
 kubectl rollout restart deploy -n istio
 
 cluster2:
   重启pod
 kubectl rollout restart deploy -n istio
 
  
 cluster3:
   重启pod
 kubectl rollout restart deploy -n istio
```



暴露服务：

kubectl port-forward --address 0.0.0.0 -n istio-system zipkin-6b8c6bdc56-m2b4f 9411:9411



![2](44image\2.jpg)



清理：

```
cluster1:

kubectl  label namespace istio-system topology.istio.io/network-
kubectl delete secret istio-remote-secret-cluster2 -n istio-system
kubectl delete secret istio-remote-secret-cluster3 -n istio-system
kubectl delete gw zipkin-gateway -n istio-system
kubectl delete vs zipkin-vs -n istio-system
kubectl delete gw cross-network-gateway -n istio-system
kubectl delete gw istiod-gateway -n istio-system
kubectl delete vs istiod-vs -n istio-system
istioctl x uninstall -f cluster1.yaml



reboot



cluster2:

kubectl  label namespace istio-system topology.istio.io/network-
kubectl delete gw cross-network-gateway -n istio-system
istioctl x uninstall -f cluster2.yaml



reboot



cluster3:

kubectl  label namespace istio-system topology.istio.io/network-
istioctl x uninstall -f cluster3.yaml



reboot
```



###### 三网关

![three-01-3](D:/公众号/图文/05image/three-01-3.bmp)



```
两个网络
三个东西向网关

集群1
137,138,139
集群2
140,141,142
集群3
143,144,145

打通cluster2，cluster3网络
140,141,142
route add -net 172.22.2.0 netmask 255.255.255.0 gw 192.168.229.145
route add -net 172.22.0.0 netmask 255.255.255.0 gw 192.168.229.144
route add -net 172.22.1.0 netmask 255.255.255.0 gw 192.168.229.143

route add -net 10.70.0.0 netmask 255.255.0.0 gw 192.168.229.143


143,144,145
route add -net 172.21.2.0 netmask 255.255.255.0 gw 192.168.229.142
route add -net 172.21.0.0 netmask 255.255.255.0 gw 192.168.229.141
route add -net 172.21.1.0 netmask 255.255.255.0 gw 192.168.229.140

route add -net 10.69.0.0 netmask 255.255.0.0 gw 192.168.229.140

给isito-system namespace打标签
cluster1:
kubectl  label namespace istio-system topology.istio.io/network=network1

cluster2:
kubectl  label namespace istio-system topology.istio.io/network=network2

cluster3:
kubectl  label namespace istio-system topology.istio.io/network=network2

生成operator部署文件
cluster1:
cat <<EOF > cluster1.yaml
apiVersion: install.istio.io/v1alpha1
kind: IstioOperator
spec:
  profile: demo
  values:
    global:
      imagePullPolicy: IfNotPresent
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
EOF

这里我设置的cluster1东西向网关的ip试192.168.229.100
如果用的是loadblance，可以用下面命令获取
#  export DISCOVERY_ADDRESS=$(kubectl  -n istio-system get svc istio-eastwestgateway  -o jsonpath='{.status.loadBalancer.ingress[0].ip}')
然后替换remotePilotAddress

生成operator部署文件
cat <<EOF > cluster2.yaml
apiVersion: install.istio.io/v1alpha1
kind: IstioOperator
spec:
  profile: demo
  values:
    global:
      imagePullPolicy: IfNotPresent
      meshID: mesh1
      multiCluster:
        clusterName: cluster2
      network: network2
      remotePilotAddress: 192.168.229.100
  meshConfig:
    accessLogFile: /dev/stdout
    enableTracing: true
  components:
    egressGateways:
    - name: istio-egressgateway
      enabled: true
EOF

这里我设置的cluster1东西向网关的ip试192.168.229.100
如果用的是loadblance，可以用下面命令获取
#  export DISCOVERY_ADDRESS=$(kubectl  -n istio-system get svc istio-eastwestgateway  -o jsonpath='{.status.loadBalancer.ingress[0].ip}')
然后替换remotePilotAddress

生成operator部署文件
cat <<EOF > cluster3.yaml
apiVersion: install.istio.io/v1alpha1
kind: IstioOperator
spec:
  profile: demo
  values:
    global:
      imagePullPolicy: IfNotPresent
      meshID: mesh1
      multiCluster:
        clusterName: cluster3
      network: network2
      remotePilotAddress: 192.168.229.100
  meshConfig:
    accessLogFile: /dev/stdout
    enableTracing: true
  components:
    egressGateways:
    - name: istio-egressgateway
      enabled: true
EOF

把部署文件传到cluster2
scp cluster2.yaml root@192.168.229.140:/root
把部署文件传到cluster3
scp cluster3.yaml root@192.168.229.143:/root

部署cluster1
istioctl install  -f cluster1.yaml
部署东西向网关
/root/istio-1.11.2/samples/multicluster/gen-eastwest-gateway.sh  --mesh mesh1 --cluster cluster1 --network network1 |  istioctl install -y  -f -
    
 
配置东西向网关ip 
 kubectl patch svc  -n istio-system istio-eastwestgateway -p '{"spec":{"externalIPs":["192.168.229.100"]}}'
  
暴露istiod
kubectl apply  -n istio-system -f /root/istio-1.11.2/samples/multicluster/expose-istiod.yaml
暴露服务
kubectl  apply -n istio-system -f /root/istio-1.11.2/samples/multicluster/expose-services.yaml


cluster2:
生成监控apiserver secret
istioctl x create-remote-secret --name=cluster2  --server=https://192.168.229.140:6443 > remote-secret-cluster2.yaml
传输secret到cluster1
 scp remote-secret-cluster2.yaml root@192.168.229.137:/root


cluster3:
生成监控apiserver secret
istioctl x create-remote-secret --name=cluster3  --server=https://192.168.229.143:6443 > remote-secret-cluster3.yaml

传输secret到cluster1
 scp remote-secret-cluster3.yaml root@192.168.229.137:/root
 
 cluster1:
 应用监控apiserver secret
  kubectl apply -f remote-secret-cluster2.yaml
  kubectl apply -f remote-secret-cluster3.yaml


cluster2:
部署cluster2
istioctl install  -f cluster2.yaml
安装东西向网关
/root/istio-1.11.2/samples/multicluster/gen-eastwest-gateway.sh --mesh mesh1 --cluster cluster2 --network network2 | istioctl install -y  -f -

 
配置东西向网关ip 
 kubectl patch svc  -n istio-system istio-eastwestgateway -p '{"spec":{"externalIPs":["192.168.229.101"]}}'
  
暴露服务
kubectl  apply -n istio-system -f /root/istio-1.11.2/samples/multicluster/expose-services.yaml

 
 cluster3:
 部署cluster3
istioctl install  -f cluster3.yaml

安装东西向网关
/root/istio-1.11.2/samples/multicluster/gen-eastwest-gateway.sh --mesh mesh1 --cluster cluster3 --network network2 | istioctl install -y  -f -

 
配置东西向网关ip 
 kubectl patch svc  -n istio-system istio-eastwestgateway -p '{"spec":{"externalIPs":["192.168.229.102"]}}'
  
暴露服务
kubectl  apply -n istio-system -f /root/istio-1.11.2/samples/multicluster/expose-services.yaml


cluster1:
重启pod
 kubectl rollout restart deploy -n istio
 kubectl rollout restart deploy -n istio-system
 
 cluster2:
 重启pod
 kubectl rollout restart deploy -n istio
 kubectl rollout restart deploy -n istio-system
 
 cluster1:
 重启pod
 kubectl rollout restart deploy -n istio
 kubectl rollout restart deploy -n istio-system

```

cluster1:部署zipkin

```
 kubectl apply -f extras/zipkin.yaml -n istio-system
```

cluster1增加东西向网关端口

```
 
  kubectl patch svc  -n istio-system istio-eastwestgateway -p '{"spec":{"ports":[{"name": "http-zipkin", "nodePort": 32197,"port": 15018, "protocol": "TCP", "targetPort": 15018}]}}'
```

cluster1:

暴露zipkin

visilazation/zipkin-gw-vs.yaml

 kubectl apply -f zipkin-gw-vs.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: Gateway
metadata:
  name: zipkin-gateway
spec:
  selector:
    istio: eastwestgateway
  servers:
    - port:
        name: http-zipkin
        number: 15018
        protocol: http        
      hosts:
        - "*"
---
apiVersion: networking.istio.io/v1alpha3
kind: VirtualService
metadata:
  name: zipkin-vs
spec:
  hosts:
  - "*"
  gateways:
  - zipkin-gateway
  http:
  - route:
    - destination:
        host: zipkin.istio-system.svc.cluster.local
        port:
          number: 9411
```





cluster1，cluster2,cluster3: cm istio   

```
[root@node01 ~]# kubectl get cm istio -n istio-system -o yaml
apiVersion: v1
data:
  mesh: |-
    accessLogFile: /dev/stdout
    enableTracing: true
    defaultConfig:
      discoveryAddress: istiod.istio-system.svc:15012
      meshId: mesh1
      proxyMetadata: {}
      tracing:
        sampling: 100
        zipkin:
          address: 192.168.229.100:15018
    enablePrometheusMerge: true
    enableTracing: true
    rootNamespace: istio-system
    trustDomain: cluster.local
  meshNetworks: 'networks: {}'
  
  修改
        sampling: 100
        zipkin:
          address: 192.168.229.100:15018
```

```
 cluster1: 
  重启pod
 kubectl rollout restart deploy -n istio
 
 cluster2:
   重启pod
 kubectl rollout restart deploy -n istio
 
  
 cluster3:
   重启pod
 kubectl rollout restart deploy -n istio
```



暴露服务：

kubectl port-forward --address 0.0.0.0 -n istio-system zipkin-6b8c6bdc56-m2b4f 9411:9411



![3](44image\3.jpg)

清理：

```
cluster1:

kubectl  label namespace istio-system topology.istio.io/network-
kubectl delete gw zipkin-gateway -n istio-system
kubectl delete vs zipkin-vs -n istio-system
kubectl delete secret istio-remote-secret-cluster2 -n istio-system
kubectl delete secret istio-remote-secret-cluster3 -n istio-system
kubectl delete gw cross-network-gateway -n istio-system
kubectl delete gw istiod-gateway -n istio-system
kubectl delete vs istiod-vs -n istio-system
istioctl x uninstall -f cluster1.yaml



reboot



cluster2:

kubectl  label namespace istio-system topology.istio.io/network-
kubectl delete gw cross-network-gateway -n istio-system
istioctl x uninstall -f cluster2.yaml



reboot



cluster3:

kubectl  label namespace istio-system topology.istio.io/network-
kubectl delete gw cross-network-gateway -n istio-system
istioctl x uninstall -f cluster3.yaml



reboot
```



##### 三网络

![three-02](D:/公众号/图文/05image/three-02.bmp)





```
三个网络
集群1
137,138,139
集群2
140,141,142
集群3
143,144,145

给istio-system namespace打标签
cluster1:
kubectl  label namespace istio-system topology.istio.io/network=network1

cluster2:
 kubectl  label namespace istio-system topology.istio.io/network=network2

cluster3:
 kubectl  label namespace istio-system topology.istio.io/network=network3


cluster1:
生成istio operator部署文件
cat <<EOF > cluster1.yaml
apiVersion: install.istio.io/v1alpha1
kind: IstioOperator
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
EOF

这里我设置的cluster1东西向网关的ip试192.168.229.100
如果用的是loadblance，可以用下面命令获取
#  export DISCOVERY_ADDRESS=$(kubectl  -n istio-system get svc istio-eastwestgateway  -o jsonpath='{.status.loadBalancer.ingress[0].ip}')
然后替换remotePilotAddress

生成istio operator部署文件
 cat <<EOF > cluster2.yaml
apiVersion: install.istio.io/v1alpha1
kind: IstioOperator
spec:
  profile: demo
  values:
    global:
      meshID: mesh1
      multiCluster:
        clusterName: cluster2
      network: network2
      remotePilotAddress: 192.168.229.100
  meshConfig:
    accessLogFile: /dev/stdout
    enableTracing: true
  components:
    egressGateways:
    - name: istio-egressgateway
      enabled: true
EOF

这里我设置的cluster1东西向网关的ip试192.168.229.100
如果用的是loadblance，可以用下面命令获取
#  export DISCOVERY_ADDRESS=$(kubectl  -n istio-system get svc istio-eastwestgateway  -o jsonpath='{.status.loadBalancer.ingress[0].ip}')
然后替换remotePilotAddress

生成istio operator部署文件
cat <<EOF > cluster3.yaml
apiVersion: install.istio.io/v1alpha1
kind: IstioOperator
spec:
  profile: demo
  values:
    global:
      meshID: mesh1
      multiCluster:
        clusterName: cluster3
      network: network3
      remotePilotAddress: 192.168.229.100
  meshConfig:
    accessLogFile: /dev/stdout
    enableTracing: true
  components:
    egressGateways:
    - name: istio-egressgateway
      enabled: true
EOF

传输部署文件到cluster2
scp cluster2.yaml root@192.168.229.140:/root

传输部署文件到cluster3
scp cluster3.yaml root@192.168.229.143:/root

安装istio
istioctl install  -f cluster1.yaml

生成东西向网关
/root/istio-1.11.2/samples/multicluster/gen-eastwest-gateway.sh  --mesh mesh1 --cluster cluster1 --network network1 |  istioctl install -y -f -
    
配置东西向网关ip 
 kubectl patch svc  -n istio-system istio-eastwestgateway -p '{"spec":{"externalIPs":["192.168.229.100"]}}'
 
 暴露istiod
kubectl apply  -n istio-system -f /root/istio-1.11.2/samples/multicluster/expose-istiod.yaml

暴露service
kubectl  apply -n istio-system -f /root/istio-1.11.2/samples/multicluster/expose-services.yaml

cluster2:
生成访问apiserver的secret
istioctl x create-remote-secret --name=cluster2  --server=https://192.168.229.140:6443 > remote-secret-cluster2.yaml

传输secret到cluster1
 scp remote-secret-cluster2.yaml root@192.168.229.137:/root

cluster3:
生成访问apiserver的secret
istioctl x create-remote-secret --name=cluster3  --server=https://192.168.229.143:6443 > remote-secret-cluster3.yaml

传输secret到cluster1
 scp remote-secret-cluster3.yaml root@192.168.229.137:/root
 
cluster1:
应用secret
  kubectl apply -f remote-secret-cluster2.yaml
  kubectl apply -f remote-secret-cluster3.yaml  

cluster2:
部署istio
istioctl install  -f cluster2.yaml

生成东西向网关
/root/istio-1.11.2/samples/multicluster/gen-eastwest-gateway.sh --mesh mesh1 --cluster cluster2 --network network2 | istioctl install -y -f -

配置东西向网关ip 
 kubectl patch svc  -n istio-system istio-eastwestgateway -p '{"spec":{"externalIPs":["192.168.229.101"]}}'
 
 暴露service
kubectl  apply -n istio-system -f /root/istio-1.11.2/samples/multicluster/expose-services.yaml

 
 cluster3:
 部署istio
istioctl install  -f cluster3.yaml

生成东西向网关
/root/istio-1.11.2/samples/multicluster/gen-eastwest-gateway.sh --mesh mesh1 --cluster cluster3 --network network3 | istioctl install -y -f -

配置东西向网关ip 
 kubectl patch svc  -n istio-system istio-eastwestgateway -p '{"spec":{"externalIPs":["192.168.229.102"]}}'
  
 暴露service
kubectl  apply -n istio-system -f /root/istio-1.11.2/samples/multicluster/expose-services.yaml

   cluster1:
重启pod
 kubectl rollout restart deploy -n istio
 kubectl rollout restart deploy -n istio-system
  
  cluster2:
重启pod
 kubectl rollout restart deploy -n istio
 kubectl rollout restart deploy -n istio-system
 
 cluster3:
 重启pod
 kubectl rollout restart deploy -n istio
 kubectl rollout restart deploy -n istio-system

```

cluster1:部署zipkin

```
 kubectl apply -f extras/zipkin.yaml -n istio-system
```

cluster1增加东西向网关端口

```
 
  kubectl patch svc  -n istio-system istio-eastwestgateway -p '{"spec":{"ports":[{"name": "http-zipkin", "nodePort": 32197,"port": 15018, "protocol": "TCP", "targetPort": 15018}]}}'
```

cluster1:

暴露zipkin

visilazation/zipkin-gw-vs.yaml

 kubectl apply -f zipkin-gw-vs.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: Gateway
metadata:
  name: zipkin-gateway
spec:
  selector:
    istio: eastwestgateway
  servers:
    - port:
        name: http-zipkin
        number: 15018
        protocol: http        
      hosts:
        - "*"
---
apiVersion: networking.istio.io/v1alpha3
kind: VirtualService
metadata:
  name: zipkin-vs
spec:
  hosts:
  - "*"
  gateways:
  - zipkin-gateway
  http:
  - route:
    - destination:
        host: zipkin.istio-system.svc.cluster.local
        port:
          number: 9411
```





cluster1，cluster2,cluster3: cm istio   

```
[root@node01 ~]# kubectl get cm istio -n istio-system -o yaml
apiVersion: v1
data:
  mesh: |-
    accessLogFile: /dev/stdout
    enableTracing: true
    defaultConfig:
      discoveryAddress: istiod.istio-system.svc:15012
      meshId: mesh1
      proxyMetadata: {}
      tracing:
        sampling: 100
        zipkin:
          address: 192.168.229.100:15018
    enablePrometheusMerge: true
    enableTracing: true
    rootNamespace: istio-system
    trustDomain: cluster.local
  meshNetworks: 'networks: {}'
  
  修改
        sampling: 100
        zipkin:
          address: 192.168.229.100:15018
```

```
 cluster1: 
  重启pod
 kubectl rollout restart deploy -n istio
 
 cluster2:
   重启pod
 kubectl rollout restart deploy -n istio
 
  
 cluster3:
   重启pod
 kubectl rollout restart deploy -n istio
```



暴露服务：

kubectl port-forward --address 0.0.0.0 -n istio-system zipkin-6b8c6bdc56-m2b4f 9411:9411

![4](44image\4.jpg)



清理：

```
cluster1:

kubectl  label namespace istio-system topology.istio.io/network-
kubectl delete secret istio-remote-secret-cluster2 -n istio-system
kubectl delete secret istio-remote-secret-cluster3 -n istio-system
kubectl delete gw cross-network-gateway -n istio-system
kubectl delete gw istiod-gateway -n istio-system
kubectl delete vs istiod-vs -n istio-system
kubectl delete gw zipkin-gateway -n istio-system
kubectl delete vs zipkin-vs -n istio-system
istioctl x uninstall -f cluster1.yaml



reboot



cluster2:

kubectl  label namespace istio-system topology.istio.io/network-
kubectl delete gw cross-network-gateway -n istio-system
istioctl x uninstall -f cluster2.yaml



reboot



cluster3:

kubectl  label namespace istio-system topology.istio.io/network-
kubectl delete gw cross-network-gateway -n istio-system
istioctl x uninstall -f cluster3.yaml



reboot
```





#### 两控制面板

##### 单网络

![three-6](D:/公众号/图文/05image/three-6.bmp)





```
cluster1有一个控制面板
cluster2，cluster3共享一个控制面板

三个网络联通
集群1
137,138,139
集群2
140,141,142
集群3
143,144,145

网络联通
137,138,139
route add -net 172.21.2.0 netmask 255.255.255.0 gw 192.168.229.142
route add -net 172.21.0.0 netmask 255.255.255.0 gw 192.168.229.141
route add -net 172.21.1.0 netmask 255.255.255.0 gw 192.168.229.140

route add -net 172.22.2.0 netmask 255.255.255.0 gw 192.168.229.145
route add -net 172.22.0.0 netmask 255.255.255.0 gw 192.168.229.144
route add -net 172.22.1.0 netmask 255.255.255.0 gw 192.168.229.143

route add -net 10.70.0.0 netmask 255.255.0.0 gw 192.168.229.143
route add -net 10.69.0.0 netmask 255.255.0.0 gw 192.168.229.140

140,141,142
route add -net 172.20.2.0 netmask 255.255.255.0 gw 192.168.229.139
route add -net 172.20.0.0 netmask 255.255.255.0 gw 192.168.229.138
route add -net 172.20.1.0 netmask 255.255.255.0 gw 192.168.229.137

route add -net 172.22.2.0 netmask 255.255.255.0 gw 192.168.229.145
route add -net 172.22.0.0 netmask 255.255.255.0 gw 192.168.229.144
route add -net 172.22.1.0 netmask 255.255.255.0 gw 192.168.229.143

route add -net 10.70.0.0 netmask 255.255.0.0 gw 192.168.229.143
route add -net 10.68.0.0 netmask 255.255.0.0 gw 192.168.229.137


143,144,145
route add -net 172.21.2.0 netmask 255.255.255.0 gw 192.168.229.142
route add -net 172.21.0.0 netmask 255.255.255.0 gw 192.168.229.141
route add -net 172.21.1.0 netmask 255.255.255.0 gw 192.168.229.140

route add -net 172.20.2.0 netmask 255.255.255.0 gw 192.168.229.139
route add -net 172.20.0.0 netmask 255.255.255.0 gw 192.168.229.138
route add -net 172.20.1.0 netmask 255.255.255.0 gw 192.168.229.137

route add -net 10.69.0.0 netmask 255.255.0.0 gw 192.168.229.140
route add -net 10.68.0.0 netmask 255.255.0.0 gw 192.168.229.137


cluster1:
生成istio operator部署文件
cat <<EOF > cluster1.yaml
apiVersion: install.istio.io/v1alpha1
kind: IstioOperator
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
EOF


生成istio operator部署文件
cat <<EOF > cluster2.yaml
apiVersion: install.istio.io/v1alpha1
kind: IstioOperator
spec:
  profile: demo
  values:
    global:
      meshID: mesh1
      multiCluster:
        clusterName: cluster2
      network: network1
  meshConfig:
    accessLogFile: /dev/stdout
    enableTracing: true
  components:
    egressGateways:
    - name: istio-egressgateway
      enabled: true
EOF

传输部署文件到cluster2
scp cluster2.yaml root@192.168.229.140:/root



这里我设置的cluster2东西向网关的ip试192.168.229.101
如果用的是loadblance，可以用下面命令获取
#  export DISCOVERY_ADDRESS=$(kubectl  -n istio-system get svc istio-eastwestgateway  -o jsonpath='{.status.loadBalancer.ingress[0].ip}')
然后替换remotePilotAddress

生成istio operator部署文件
cat <<EOF > cluster3.yaml
apiVersion: install.istio.io/v1alpha1
kind: IstioOperator
spec:
  profile: demo
  values:
    global:
      meshID: mesh1
      multiCluster:
        clusterName: cluster3
      network: network1
      remotePilotAddress: 192.168.229.101
  meshConfig:
    accessLogFile: /dev/stdout
    enableTracing: true
  components:
    egressGateways:
    - name: istio-egressgateway
      enabled: true
EOF

传输部署文件到cluster3
scp cluster3.yaml root@192.168.229.143:/root


cluster1:
创建访问apiserver secret
 istioctl x create-remote-secret --name=cluster1  --server=https://192.168.229.137:6443 > remote-secret-cluster1.yaml
 
 传输secret到cluster2
scp remote-secret-cluster1.yaml root@192.168.229.140:/root

cluster2
创建访问apiserver secret
 istioctl x create-remote-secret --name=cluster2  --server=https://192.168.229.140:6443 > remote-secret-cluster2.yaml
 
  
 传输secret到cluster1
 scp remote-secret-cluster2.yaml root@192.168.229.137:/root
 
 cluster3
 创建访问apiserver secret
 istioctl x create-remote-secret --name=cluster3  --server=https://192.168.229.143:6443 > remote-secret-cluster3.yaml
 
 传输secret到cluster1
 scp remote-secret-cluster3.yaml root@192.168.229.137:/root
  传输secret到cluster2
 scp remote-secret-cluster3.yaml root@192.168.229.140:/root

 cluster1
 应用secret
  kubectl apply -f remote-secret-cluster2.yaml
  kubectl apply -f remote-secret-cluster3.yaml
  
  部署istio
  istioctl install  -f cluster1.yaml
  
  cluster2:
  应用secret
   kubectl apply -f remote-secret-cluster1.yaml
  kubectl apply -f remote-secret-cluster3.yaml
  
  部署istio
istioctl install  -f cluster2.yaml

生成东西向网关
/root/istio-1.11.2/samples/multicluster/gen-eastwest-gateway.sh --mesh mesh1 --cluster cluster2 --network network1 |  istioctl  install -y -f -

配置东西向网关ip 
 kubectl patch svc  -n istio-system istio-eastwestgateway -p '{"spec":{"externalIPs":["192.168.229.101"]}}'
  
  暴露服务
  kubectl apply  -n istio-system -f /root/istio-1.11.2/samples/multicluster/expose-istiod.yaml


  cluster3:
  部署istio
istioctl install  -f cluster3.yaml

cluster1:
重启pod
 kubectl rollout restart deploy -n istio
 kubectl rollout restart deploy -n istio-system
 
 cluster2:
重启pod
 kubectl rollout restart deploy -n istio
 kubectl rollout restart deploy -n istio-system
 
 cluster3:
重启pod
 kubectl rollout restart deploy -n istio
 kubectl rollout restart deploy -n istio-system


```



cluster1:部署zipkin

```
 kubectl apply -f extras/zipkin.yaml -n istio-system
```

cluster1：配置东西向网关

```
/root/istio-1.11.2/samples/multicluster/gen-eastwest-gateway.sh --mesh mesh1 --cluster cluster1 --network network1 |  istioctl  install -y -f -
```

配置东西向网关ip 
 kubectl patch svc  -n istio-system istio-eastwestgateway -p '{"spec":{"externalIPs":["192.168.229.100"]}}'

cluster1增加东西向网关端口

```
 
  kubectl patch svc  -n istio-system istio-eastwestgateway -p '{"spec":{"ports":[{"name": "http-zipkin", "nodePort": 32197,"port": 15018, "protocol": "TCP", "targetPort": 15018}]}}'
```

cluster1:

暴露zipkin

visilazation/zipkin-gw-vs.yaml

 kubectl apply -f zipkin-gw-vs.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: Gateway
metadata:
  name: zipkin-gateway
spec:
  selector:
    istio: eastwestgateway
  servers:
    - port:
        name: http-zipkin
        number: 15018
        protocol: http        
      hosts:
        - "*"
---
apiVersion: networking.istio.io/v1alpha3
kind: VirtualService
metadata:
  name: zipkin-vs
spec:
  hosts:
  - "*"
  gateways:
  - zipkin-gateway
  http:
  - route:
    - destination:
        host: zipkin.istio-system.svc.cluster.local
        port:
          number: 9411
```





cluster1，cluster2,cluster3: cm istio   

```
[root@node01 ~]# kubectl get cm istio -n istio-system -o yaml
apiVersion: v1
data:
  mesh: |-
    accessLogFile: /dev/stdout
    enableTracing: true
    defaultConfig:
      discoveryAddress: istiod.istio-system.svc:15012
      meshId: mesh1
      proxyMetadata: {}
      tracing:
        sampling: 100
        zipkin:
          address: 192.168.229.100:15018
    enablePrometheusMerge: true
    enableTracing: true
    rootNamespace: istio-system
    trustDomain: cluster.local
  meshNetworks: 'networks: {}'
  
  修改
        sampling: 100
        zipkin:
          address: 192.168.229.100:15018
```

```
 cluster1: 
  重启pod
 kubectl rollout restart deploy -n istio
 
 cluster2:
   重启pod
 kubectl rollout restart deploy -n istio
 
  
 cluster3:
   重启pod
 kubectl rollout restart deploy -n istio
```



暴露服务：

kubectl port-forward --address 0.0.0.0 -n istio-system zipkin-6b8c6bdc56-m2b4f 9411:9411



![5](44image\5.jpg)



清理：

```
cluster1:

kubectl  label namespace istio-system topology.istio.io/network-
kubectl delete gw zipkin-gateway -n istio-system
kubectl delete vs zipkin-vs -n istio-system
kubectl delete secret istio-remote-secret-cluster2 -n istio-system
kubectl delete secret istio-remote-secret-cluster3 -n istio-system
istioctl x uninstall -f cluster1.yaml



reboot



cluster2:

kubectl  label namespace istio-system topology.istio.io/network-
kubectl delete gw istiod-gateway -n istio-system
kubectl delete vs istiod-vs -n istio-system
istioctl x uninstall -f cluster2.yaml



reboot



cluster3:

kubectl  label namespace istio-system topology.istio.io/network-
istioctl x uninstall -f cluster3.yaml



reboot


```



##### 两网络

![three-5](D:/公众号/图文/05image/three-5.bmp)





```
cluster1有一个控制面板，与其他cluster不在一个网络
cluster2，cluster3共享一个控制面板,cluster3有一个控制面板，在同一个网络

集群1
137,138,139
集群2
140,141,142
集群3
143,144,145


cluster2，cluster3网络连通

140,141,142
route add -net 172.22.2.0 netmask 255.255.255.0 gw 192.168.229.145
route add -net 172.22.0.0 netmask 255.255.255.0 gw 192.168.229.144
route add -net 172.22.1.0 netmask 255.255.255.0 gw 192.168.229.143

route add -net 10.70.0.0 netmask 255.255.0.0 gw 192.168.229.143


143,144,145
route add -net 172.21.2.0 netmask 255.255.255.0 gw 192.168.229.142
route add -net 172.21.0.0 netmask 255.255.255.0 gw 192.168.229.141
route add -net 172.21.1.0 netmask 255.255.255.0 gw 192.168.229.140

route add -net 10.69.0.0 netmask 255.255.0.0 gw 192.168.229.140


给istio-system namesapce打标签
cluster1:
 kubectl  label namespace istio-system topology.istio.io/network=network1
 
 cluster2:
 kubectl  label namespace istio-system topology.istio.io/network=network2
 
 cluster3:
 kubectl  label namespace istio-system topology.istio.io/network=network2
 
cluster1:
生成istio operator部署文件
cat <<EOF > cluster1.yaml
apiVersion: install.istio.io/v1alpha1
kind: IstioOperator
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
EOF

这里我设置的cluster3东西向网关的ip试192.168.229.102
如果用的是loadblance，可以用下面命令获取
#  export DISCOVERY_ADDRESS=$(kubectl  -n istio-system get svc istio-eastwestgateway  -o jsonpath='{.status.loadBalancer.ingress[0].ip}')
然后替换remotePilotAddress

生成istio operator部署文件
cat <<EOF > cluster2.yaml
apiVersion: install.istio.io/v1alpha1
kind: IstioOperator
spec:
  profile: demo
  values:
    global:
      meshID: mesh1
      multiCluster:
        clusterName: cluster2
      network: network2
      remotePilotAddress: 192.168.229.102
  meshConfig:
    accessLogFile: /dev/stdout
    enableTracing: true
  components:
    egressGateways:
    - name: istio-egressgateway
      enabled: true
EOF

传输部署文件到cluster2
scp cluster2.yaml root@192.168.229.140:/root


生成istio operator部署文件
cat <<EOF > cluster3.yaml
apiVersion: install.istio.io/v1alpha1
kind: IstioOperator
spec:
  profile: demo
  values:
    global:
      meshID: mesh1
      multiCluster:
        clusterName: cluster3
      network: network2
  meshConfig:
    accessLogFile: /dev/stdout
    enableTracing: true
  components:
    egressGateways:
    - name: istio-egressgateway
      enabled: true
EOF

传输部署文件到cluster3
scp cluster3.yaml root@192.168.229.143:/root


cluster1:
生成apiserver访问secret
 istioctl x create-remote-secret --name=cluster1  --server=https://192.168.229.137:6443 > remote-secret-cluster1.yaml
 
 传输secret到cluster3
scp remote-secret-cluster1.yaml root@192.168.229.143:/root

cluster2
生成apiserver访问secret
 istioctl x create-remote-secret --name=cluster2  --server=https://192.168.229.140:6443 > remote-secret-cluster2.yaml
 
  传输secret到cluster1
 scp remote-secret-cluster2.yaml root@192.168.229.137:/root
 
  传输secret到cluster3
 scp remote-secret-cluster2.yaml root@192.168.229.143:/root
 
 cluster3
 
 生成apiserver访问secret
 istioctl x create-remote-secret --name=cluster3  --server=https://192.168.229.143:6443 > remote-secret-cluster3.yaml
 
 传输secret到cluster1
 scp remote-secret-cluster3.yaml root@192.168.229.137:/root


 cluster1:
 应用secret
  kubectl apply -f remote-secret-cluster2.yaml
  kubectl apply -f remote-secret-cluster3.yaml
  
  部署istio
  istioctl install  -f cluster1.yaml
  
  生成东西向网关
  /root/istio-1.11.2/samples/multicluster/gen-eastwest-gateway.sh --mesh mesh1 --cluster cluster1 --network network1 |  istioctl  install -y  -f -

配置东西向网关ip 
 kubectl patch svc  -n istio-system istio-eastwestgateway -p '{"spec":{"externalIPs":["192.168.229.100"]}}'
  
  暴露服务
  kubectl  apply -n istio-system -f /root/istio-1.11.2/samples/multicluster/expose-services.yaml
  
  
  cluster3:
  应用secret
   kubectl apply -f remote-secret-cluster1.yaml
  kubectl apply -f remote-secret-cluster2.yaml

部署istio
istioctl install  -f cluster3.yaml

  生成东西向网关
/root/istio-1.11.2/samples/multicluster/gen-eastwest-gateway.sh --mesh mesh1 --cluster cluster3 --network network2 |  istioctl  install -y  -f -

配置东西向网关ip 
 kubectl patch svc  -n istio-system istio-eastwestgateway -p '{"spec":{"externalIPs":["192.168.229.102"]}}'
  
  暴露istiod
  kubectl apply  -n istio-system -f /root/istio-1.11.2/samples/multicluster/expose-istiod.yaml
  
  暴露服务
  kubectl  apply -n istio-system -f /root/istio-1.11.2/samples/multicluster/expose-services.yaml


  cluster2:
  部署istio
istioctl install  -f cluster2.yaml

cluster1:
重启pod
 kubectl rollout restart deploy -n istio
 kubectl rollout restart deploy -n istio-system

cluster2:
重启pod
 kubectl rollout restart deploy -n istio
 kubectl rollout restart deploy -n istio-system

cluster3:
重启pod
 kubectl rollout restart deploy -n istio
 kubectl rollout restart deploy -n istio-system

```

cluster1:部署zipkin

```
 kubectl apply -f extras/zipkin.yaml -n istio-system
```

cluster1增加东西向网关端口

```
 
  kubectl patch svc  -n istio-system istio-eastwestgateway -p '{"spec":{"ports":[{"name": "http-zipkin", "nodePort": 32197,"port": 15018, "protocol": "TCP", "targetPort": 15018}]}}'
```

cluster1:

暴露zipkin

visilazation/zipkin-gw-vs.yaml

 kubectl apply -f zipkin-gw-vs.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: Gateway
metadata:
  name: zipkin-gateway
spec:
  selector:
    istio: eastwestgateway
  servers:
    - port:
        name: http-zipkin
        number: 15018
        protocol: http        
      hosts:
        - "*"
---
apiVersion: networking.istio.io/v1alpha3
kind: VirtualService
metadata:
  name: zipkin-vs
spec:
  hosts:
  - "*"
  gateways:
  - zipkin-gateway
  http:
  - route:
    - destination:
        host: zipkin.istio-system.svc.cluster.local
        port:
          number: 9411
```





cluster1，cluster2,cluster3: cm istio   

```
[root@node01 ~]# kubectl get cm istio -n istio-system -o yaml
apiVersion: v1
data:
  mesh: |-
    accessLogFile: /dev/stdout
    enableTracing: true
    defaultConfig:
      discoveryAddress: istiod.istio-system.svc:15012
      meshId: mesh1
      proxyMetadata: {}
      tracing:
        sampling: 100
        zipkin:
          address: 192.168.229.100:15018
    enablePrometheusMerge: true
    enableTracing: true
    rootNamespace: istio-system
    trustDomain: cluster.local
  meshNetworks: 'networks: {}'
  
  修改
        sampling: 100
        zipkin:
          address: 192.168.229.100:15018
```

```
 cluster1: 
  重启pod
 kubectl rollout restart deploy -n istio
 
 cluster2:
   重启pod
 kubectl rollout restart deploy -n istio
 
  
 cluster3:
   重启pod
 kubectl rollout restart deploy -n istio
```



暴露服务：

kubectl port-forward --address 0.0.0.0 -n istio-system zipkin-6b8c6bdc56-m2b4f 9411:9411

![6](44image\6.jpg)



清理：

```
cluster1:

kubectl  label namespace istio-system topology.istio.io/network-
kubectl delete secret istio-remote-secret-cluster2 -n istio-system
kubectl delete secret istio-remote-secret-cluster3 -n istio-system
kubectl delete gw cross-network-gateway -n istio-system
kubectl delete gw zipkin-gateway -n istio-system
kubectl delete vs zipkin-vs -n istio-system
istioctl x uninstall -f cluster1.yaml



reboot



cluster2:

kubectl  label namespace istio-system topology.istio.io/network-
istioctl x uninstall -f cluster2.yaml



reboot



cluster3:

kubectl  label namespace istio-system topology.istio.io/network-
kubectl delete gw istiod-gateway -n istio-system
kubectl delete vs istiod-vs -n istio-system
kubectl delete secret istio-remote-secret-cluster1 -n istio-system
kubectl delete secret istio-remote-secret-cluster2 -n istio-system
kubectl delete gw cross-network-gateway -n istio-system
istioctl x uninstall -f cluster3.yaml



reboot
```



##### 三网络

![three-7](D:/公众号/图文/05image/three-7.bmp)





```
cluster1有一个控制面板，与其他cluster不在一个网络
cluster2，cluster3共享一个控制面板,cluster2有一个控制面板，不在同一个网络

集群1
137,138,139
集群2
140,141,142
集群3
143,144,145

给istio-system namespace打标签
cluster1:
 kubectl  label namespace istio-system topology.istio.io/network=network1

cluster2:
kubectl  label namespace istio-system topology.istio.io/network=network2

cluster3:
 kubectl  label namespace istio-system topology.istio.io/network=network3
 
 cluster1:
 生成istio operator部署文件
cat <<EOF > cluster1.yaml
apiVersion: install.istio.io/v1alpha1
kind: IstioOperator
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
EOF

生成istio operator部署文件
cat <<EOF > cluster2.yaml
apiVersion: install.istio.io/v1alpha1
kind: IstioOperator
spec:
  profile: demo
  values:
    global:
      meshID: mesh1
      multiCluster:
        clusterName: cluster2
      network: network2
  meshConfig:
    accessLogFile: /dev/stdout
    enableTracing: true
  components:
    egressGateways:
    - name: istio-egressgateway
      enabled: true
EOF

传输部署文件到cluster2
scp cluster2.yaml root@192.168.229.140:/root

这里我设置的cluster2东西向网关的ip试192.168.229.101
如果用的是loadblance，可以用下面命令获取
#  export DISCOVERY_ADDRESS=$(kubectl  -n istio-system get svc istio-eastwestgateway  -o jsonpath='{.status.loadBalancer.ingress[0].ip}')
然后替换remotePilotAddress

生成istio operator部署文件
cat <<EOF > cluster3.yaml
apiVersion: install.istio.io/v1alpha1
kind: IstioOperator
spec:
  profile: demo
  values:
    global:
      meshID: mesh1
      multiCluster:
        clusterName: cluster3
      network: network3
      remotePilotAddress: 192.168.229.101
  meshConfig:
    accessLogFile: /dev/stdout
    enableTracing: true
  components:
    egressGateways:
    - name: istio-egressgateway
      enabled: true
EOF

传输部署文件到cluster3
scp cluster3.yaml root@192.168.229.143:/root


cluster1:
生成访问apiserver secret
 istioctl x create-remote-secret --name=cluster1  --server=https://192.168.229.137:6443 > remote-secret-cluster1.yaml
 
 传输secret到cluster2
scp remote-secret-cluster1.yaml root@192.168.229.140:/root

cluster2
生成访问apiserver secret
 istioctl x create-remote-secret --name=cluster2  --server=https://192.168.229.140:6443 > remote-secret-cluster2.yaml
 
  传输secret到cluster1
 scp remote-secret-cluster2.yaml root@192.168.229.137:/root

 
 cluster3
 生成访问apiserver secret
 istioctl x create-remote-secret --name=cluster3  --server=https://192.168.229.143:6443 > remote-secret-cluster3.yaml
 
 传输secret到cluster1
 scp remote-secret-cluster3.yaml root@192.168.229.137:/root
 
 传输secret到cluster2
 scp remote-secret-cluster3.yaml root@192.168.229.140:/root


 cluster1:
 应用secret
  kubectl apply -f remote-secret-cluster2.yaml
  kubectl apply -f remote-secret-cluster3.yaml
  
  部署istio
  istioctl install  -f cluster1.yaml
  
  部署东西向网关
  /root/istio-1.11.2/samples/multicluster/gen-eastwest-gateway.sh --mesh mesh1 --cluster cluster1 --network network1 |  istioctl  install -y  -f -

配置东西向网关ip 
 kubectl patch svc  -n istio-system istio-eastwestgateway -p '{"spec":{"externalIPs":["192.168.229.100"]}}'
  
  暴露服务
  kubectl  apply -n istio-system -f /root/istio-1.11.2/samples/multicluster/expose-services.yaml

  
  cluster2:
  应用secret
   kubectl apply -f remote-secret-cluster1.yaml
  kubectl apply -f remote-secret-cluster3.yaml
  
  部署istio
istioctl install  -f cluster2.yaml

生成东西向网关
/root/istio-1.11.2/samples/multicluster/gen-eastwest-gateway.sh --mesh mesh1 --cluster cluster2 --network network2 |  istioctl  install -y  -f -

配置东西向网关ip 
 kubectl patch svc  -n istio-system istio-eastwestgateway -p '{"spec":{"externalIPs":["192.168.229.101"]}}'
  
  暴露istiod
  kubectl apply  -n istio-system -f /root/istio-1.11.2/samples/multicluster/expose-istiod.yaml
  
  暴露服务
  kubectl  apply -n istio-system -f /root/istio-1.11.2/samples/multicluster/expose-services.yaml

  cluster3:
  部署istio
istioctl install  -f cluster3.yaml

生成东西向网关
/root/istio-1.11.2/samples/multicluster/gen-eastwest-gateway.sh --mesh mesh1 --cluster cluster3 --network network3 |  istioctl  install -y -f -

配置东西向网关ip 
 kubectl patch svc  -n istio-system istio-eastwestgateway -p '{"spec":{"externalIPs":["192.168.229.102"]}}'
  
   暴露服务
  kubectl  apply -n istio-system -f /root/istio-1.11.2/samples/multicluster/expose-services.yaml

cluster1:
重启pod
 kubectl rollout restart deploy -n istio
 kubectl rollout restart deploy -n istio-system
 
cluster2:
重启pod
 kubectl rollout restart deploy -n istio
 kubectl rollout restart deploy -n istio-system
 
cluster3:
重启pod
 kubectl rollout restart deploy -n istio
 kubectl rollout restart deploy -n istio-system


```

cluster1:部署zipkin

```
 kubectl apply -f extras/zipkin.yaml -n istio-system
```

cluster1增加东西向网关端口

```
 
  kubectl patch svc  -n istio-system istio-eastwestgateway -p '{"spec":{"ports":[{"name": "http-zipkin", "nodePort": 32197,"port": 15018, "protocol": "TCP", "targetPort": 15018}]}}'
```

cluster1:

暴露zipkin

visilazation/zipkin-gw-vs.yaml

 kubectl apply -f zipkin-gw-vs.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: Gateway
metadata:
  name: zipkin-gateway
spec:
  selector:
    istio: eastwestgateway
  servers:
    - port:
        name: http-zipkin
        number: 15018
        protocol: http        
      hosts:
        - "*"
---
apiVersion: networking.istio.io/v1alpha3
kind: VirtualService
metadata:
  name: zipkin-vs
spec:
  hosts:
  - "*"
  gateways:
  - zipkin-gateway
  http:
  - route:
    - destination:
        host: zipkin.istio-system.svc.cluster.local
        port:
          number: 9411
```





cluster1，cluster2,cluster3: cm istio   

```
[root@node01 ~]# kubectl get cm istio -n istio-system -o yaml
apiVersion: v1
data:
  mesh: |-
    accessLogFile: /dev/stdout
    enableTracing: true
    defaultConfig:
      discoveryAddress: istiod.istio-system.svc:15012
      meshId: mesh1
      proxyMetadata: {}
      tracing:
        sampling: 100
        zipkin:
          address: 192.168.229.100:15018
    enablePrometheusMerge: true
    enableTracing: true
    rootNamespace: istio-system
    trustDomain: cluster.local
  meshNetworks: 'networks: {}'
  
  修改
        sampling: 100
        zipkin:
          address: 192.168.229.100:15018
```

```
 cluster1: 
  重启pod
 kubectl rollout restart deploy -n istio
 
 cluster2:
   重启pod
 kubectl rollout restart deploy -n istio
 
  
 cluster3:
   重启pod
 kubectl rollout restart deploy -n istio
```



暴露服务：

kubectl port-forward --address 0.0.0.0 -n istio-system zipkin-6b8c6bdc56-m2b4f 9411:9411

![7](44image\7.jpg)

清理：

```
cluster1:

kubectl  label namespace istio-system topology.istio.io/network-
kubectl delete secret istio-remote-secret-cluster2 -n istio-system
kubectl delete secret istio-remote-secret-cluster3 -n istio-system
kubectl delete gw cross-network-gateway -n istio-system
kubectl delete gw zipkin-gateway -n istio-system
kubectl delete vs zipkin-vs -n istio-system
istioctl x uninstall -f cluster1.yaml



reboot



cluster2:

kubectl  label namespace istio-system topology.istio.io/network-
kubectl delete gw istiod-gateway -n istio-system
kubectl delete vs istiod-vs -n istio-system
kubectl delete secret istio-remote-secret-cluster1 -n istio-system
kubectl delete secret istio-remote-secret-cluster3 -n istio-system
kubectl delete gw cross-network-gateway -n istio-system
istioctl x uninstall -f cluster2.yaml



reboot



cluster3:

kubectl  label namespace istio-system topology.istio.io/network-
kubectl delete gw cross-network-gateway -n istio-system
istioctl x uninstall -f cluster3.yaml



reboot
```



### 三控制面板

##### 单网络

![three-03](D:/公众号/图文/05image/three-03.bmp)





```
三个网络联通
集群1
137,138,139
集群2
140,141,142
集群3
143,144,145

网络联通
137,138,139
route add -net 172.21.2.0 netmask 255.255.255.0 gw 192.168.229.142
route add -net 172.21.0.0 netmask 255.255.255.0 gw 192.168.229.141
route add -net 172.21.1.0 netmask 255.255.255.0 gw 192.168.229.140

route add -net 172.22.2.0 netmask 255.255.255.0 gw 192.168.229.145
route add -net 172.22.0.0 netmask 255.255.255.0 gw 192.168.229.144
route add -net 172.22.1.0 netmask 255.255.255.0 gw 192.168.229.143

route add -net 10.70.0.0 netmask 255.255.0.0 gw 192.168.229.143
route add -net 10.69.0.0 netmask 255.255.0.0 gw 192.168.229.140

140,141,142
route add -net 172.20.2.0 netmask 255.255.255.0 gw 192.168.229.139
route add -net 172.20.0.0 netmask 255.255.255.0 gw 192.168.229.138
route add -net 172.20.1.0 netmask 255.255.255.0 gw 192.168.229.137

route add -net 172.22.2.0 netmask 255.255.255.0 gw 192.168.229.145
route add -net 172.22.0.0 netmask 255.255.255.0 gw 192.168.229.144
route add -net 172.22.1.0 netmask 255.255.255.0 gw 192.168.229.143

route add -net 10.70.0.0 netmask 255.255.0.0 gw 192.168.229.143
route add -net 10.68.0.0 netmask 255.255.0.0 gw 192.168.229.137


143,144,145
route add -net 172.21.2.0 netmask 255.255.255.0 gw 192.168.229.142
route add -net 172.21.0.0 netmask 255.255.255.0 gw 192.168.229.141
route add -net 172.21.1.0 netmask 255.255.255.0 gw 192.168.229.140

route add -net 172.20.2.0 netmask 255.255.255.0 gw 192.168.229.139
route add -net 172.20.0.0 netmask 255.255.255.0 gw 192.168.229.138
route add -net 172.20.1.0 netmask 255.255.255.0 gw 192.168.229.137

route add -net 10.69.0.0 netmask 255.255.0.0 gw 192.168.229.140
route add -net 10.68.0.0 netmask 255.255.0.0 gw 192.168.229.137



cluster1:
生成isito operator部署文件
cat <<EOF > cluster1.yaml
apiVersion: install.istio.io/v1alpha1
kind: IstioOperator
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
EOF


生成isito operator部署文件
cat <<EOF > cluster2.yaml
apiVersion: install.istio.io/v1alpha1
kind: IstioOperator
spec:
  profile: demo
  values:
    global:
      meshID: mesh1
      multiCluster:
        clusterName: cluster2
      network: network1
  meshConfig:
    accessLogFile: /dev/stdout
    enableTracing: true
  components:
    egressGateways:
    - name: istio-egressgateway
      enabled: true
EOF
传输部署文件到cluster2
scp cluster2.yaml root@192.168.229.140:/root

生成isito operator部署文件
cat <<EOF > cluster3.yaml
apiVersion: install.istio.io/v1alpha1
kind: IstioOperator
spec:
  profile: demo
  values:
    global:
      meshID: mesh1
      multiCluster:
        clusterName: cluster3
      network: network1
  meshConfig:
    accessLogFile: /dev/stdout
    enableTracing: true
  components:
    egressGateways:
    - name: istio-egressgateway
      enabled: true
EOF

传输部署文件到cluster3
scp cluster3.yaml root@192.168.229.143:/root


cluster1:
 创建访问apiserver secret
 istioctl x create-remote-secret --name=cluster1  --server=https://192.168.229.137:6443 > remote-secret-cluster1.yaml
 
 传输secret
scp remote-secret-cluster1.yaml root@192.168.229.140:/root
scp remote-secret-cluster1.yaml root@192.168.229.143:/root

cluster2
 创建访问apiserver secret
 istioctl x create-remote-secret --name=cluster2  --server=https://192.168.229.140:6443 > remote-secret-cluster2.yaml
 
  传输secret
 scp remote-secret-cluster2.yaml root@192.168.229.137:/root
  scp remote-secret-cluster2.yaml root@192.168.229.143:/root

cluster3
创建访问apiserver secret
 istioctl x create-remote-secret --name=cluster3  --server=https://192.168.229.143:6443 > remote-secret-cluster3.yaml
 
 传输secret
 scp remote-secret-cluster3.yaml root@192.168.229.137:/root
  scp remote-secret-cluster3.yaml root@192.168.229.140:/root
 
 
 cluster1:
  应用secret
  kubectl apply -f remote-secret-cluster3.yaml
 kubectl apply -f remote-secret-cluster2.yaml
 
 部署istio
 istioctl install  -f cluster1.yaml
 
 cluster2:
 应用secret
  kubectl apply -f remote-secret-cluster1.yaml
 kubectl apply -f remote-secret-cluster3.yaml
 
  部署istio
 istioctl install  -f cluster2.yaml
 
 cluster3:
 应用secret
  kubectl apply -f remote-secret-cluster1.yaml
 kubectl apply -f remote-secret-cluster2.yaml
 
  部署istio
  istioctl install  -f cluster3.yaml

cluster1:
重启pod
 kubectl rollout restart deploy -n istio
 kubectl rollout restart deploy -n istio-system
 
 
 cluster2:
重启pod
  kubectl rollout restart deploy -n istio
 kubectl rollout restart deploy -n istio-system 
  
  
cluster3:
重启pod
 kubectl rollout restart deploy -n istio
 kubectl rollout restart deploy -n istio-system
  


```



cluster1:部署zipkin

```
 kubectl apply -f extras/zipkin.yaml -n istio-system
```

cluster1：配置东西向网关

```
/root/istio-1.11.2/samples/multicluster/gen-eastwest-gateway.sh --mesh mesh1 --cluster cluster1 --network network1 |  istioctl  install -y -f -
```

配置东西向网关ip 
 kubectl patch svc  -n istio-system istio-eastwestgateway -p '{"spec":{"externalIPs":["192.168.229.100"]}}'

cluster1增加东西向网关端口

```
 
  kubectl patch svc  -n istio-system istio-eastwestgateway -p '{"spec":{"ports":[{"name": "http-zipkin", "nodePort": 32197,"port": 15018, "protocol": "TCP", "targetPort": 15018}]}}'
```

cluster1:

暴露zipkin

visilazation/zipkin-gw-vs.yaml

 kubectl apply -f zipkin-gw-vs.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: Gateway
metadata:
  name: zipkin-gateway
spec:
  selector:
    istio: eastwestgateway
  servers:
    - port:
        name: http-zipkin
        number: 15018
        protocol: http        
      hosts:
        - "*"
---
apiVersion: networking.istio.io/v1alpha3
kind: VirtualService
metadata:
  name: zipkin-vs
spec:
  hosts:
  - "*"
  gateways:
  - zipkin-gateway
  http:
  - route:
    - destination:
        host: zipkin.istio-system.svc.cluster.local
        port:
          number: 9411
```





cluster1，cluster2,cluster3: cm istio   

```
[root@node01 ~]# kubectl get cm istio -n istio-system -o yaml
apiVersion: v1
data:
  mesh: |-
    accessLogFile: /dev/stdout
    enableTracing: true
    defaultConfig:
      discoveryAddress: istiod.istio-system.svc:15012
      meshId: mesh1
      proxyMetadata: {}
      tracing:
        sampling: 100
        zipkin:
          address: 192.168.229.100:15018
    enablePrometheusMerge: true
    enableTracing: true
    rootNamespace: istio-system
    trustDomain: cluster.local
  meshNetworks: 'networks: {}'
  
  修改
        sampling: 100
        zipkin:
          address: 192.168.229.100:15018
```

```
 cluster1: 
  重启pod
 kubectl rollout restart deploy -n istio
 
 cluster2:
   重启pod
 kubectl rollout restart deploy -n istio
 
  
 cluster3:
   重启pod
 kubectl rollout restart deploy -n istio
```



暴露服务：

kubectl port-forward --address 0.0.0.0 -n istio-system zipkin-6b8c6bdc56-m2b4f 9411:9411

![8](44image\8.png)



清理：

```
cluster1:


kubectl delete secret istio-remote-secret-cluster2 -n istio-system
kubectl delete secret istio-remote-secret-cluster3 -n istio-system
kubectl delete gw zipkin-gateway -n istio-system
kubectl delete vs zipkin-vs -n istio-system
istioctl x uninstall -f cluster1.yaml



reboot



cluster2:

kubectl delete secret istio-remote-secret-cluster1 -n istio-system
kubectl delete secret istio-remote-secret-cluster3 -n istio-system
istioctl x uninstall -f cluster2.yaml



reboot



cluster3:

kubectl delete secret istio-remote-secret-cluster2 -n istio-system
kubectl delete secret istio-remote-secret-cluster1 -n istio-system
istioctl x uninstall -f cluster3.yaml



reboot
```



##### 两网络

![three-8](D:/公众号/图文/05image/three-8.bmp)





```
三个集群
集群1
137,138,139
集群2
140,141,142
集群3
143,144,145

每个集群都有各自的istiod
cluster2和cluster3在一个网络中，他们和cluster1在不同网络中

打通cluster2和cluster3网络
140,141,142
route add -net 172.22.2.0 netmask 255.255.255.0 gw 192.168.229.145
route add -net 172.22.0.0 netmask 255.255.255.0 gw 192.168.229.144
route add -net 172.22.1.0 netmask 255.255.255.0 gw 192.168.229.143

route add -net 10.70.0.0 netmask 255.255.0.0 gw 192.168.229.143


143,144,145
route add -net 172.21.2.0 netmask 255.255.255.0 gw 192.168.229.142
route add -net 172.21.0.0 netmask 255.255.255.0 gw 192.168.229.141
route add -net 172.21.1.0 netmask 255.255.255.0 gw 192.168.229.140

route add -net 10.69.0.0 netmask 255.255.0.0 gw 192.168.229.140

给istio-system namespace打网络标签
cluster1:
 kubectl  label namespace istio-system topology.istio.io/network=network1
cluster2:
 kubectl  label namespace istio-system topology.istio.io/network=network2
cluster3:
 kubectl  label namespace istio-system topology.istio.io/network=network2

生成istio集群operator部署文件
cluster1: 
 cat <<EOF > cluster1.yaml
apiVersion: install.istio.io/v1alpha1
kind: IstioOperator
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
EOF

生成istio集群operator部署文件
cat <<EOF > cluster2.yaml
apiVersion: install.istio.io/v1alpha1
kind: IstioOperator
spec:
  profile: demo
  values:
    global:
      meshID: mesh1
      multiCluster:
        clusterName: cluster2
      network: network2
  meshConfig:
    accessLogFile: /dev/stdout
    enableTracing: true
  components:
    egressGateways:
    - name: istio-egressgateway
      enabled: true
EOF


 cat <<EOF > cluster3.yaml
apiVersion: install.istio.io/v1alpha1
kind: IstioOperator
spec:
  profile: demo
  values:
    global:
      meshID: mesh1
      multiCluster:
        clusterName: cluster3
      network: network2
  meshConfig:
    accessLogFile: /dev/stdout
    enableTracing: true
  components:
    egressGateways:
    - name: istio-egressgateway
      enabled: true
EOF

传输部署文件到相关主机
scp cluster2.yaml root@192.168.229.140:/root
scp cluster3.yaml root@192.168.229.143:/root


部署cluster1
istioctl install  -f cluster1.yaml

生成cluster1 东西向网关
/root/istio-1.11.2/samples/multicluster/gen-eastwest-gateway.sh --mesh mesh1 --cluster cluster1 --network network1 | istioctl  install -y -f -

配置东西向网关ip 
 kubectl patch svc  -n istio-system istio-eastwestgateway -p '{"spec":{"externalIPs":["192.168.229.100"]}}'

暴露cluster1中的服务
 kubectl  apply -n istio-system -f /root/istio-1.11.2/samples/multicluster/expose-services.yaml
 

cluster2:
安装集群2
 istioctl install -f cluster2.yaml
 
 配置东西向网关
/root/istio-1.11.2/samples/multicluster/gen-eastwest-gateway.sh --mesh mesh1 --cluster cluster2 --network network2 |  istioctl install -y  -f -

配置东西向网关ip 
 kubectl patch svc  -n istio-system istio-eastwestgateway -p '{"spec":{"externalIPs":["192.168.229.101"]}}'
  
暴露的服务
 kubectl  apply -n istio-system -f /root/istio-1.11.2/samples/multicluster/expose-services.yaml
 
 cluster3:
 安装集群3
 istioctl install -f cluster3.yaml
 
 配置东西向网关 
/root/istio-1.11.2/samples/multicluster/gen-eastwest-gateway.sh --mesh mesh1 --cluster cluster3 --network network2 |  istioctl install -y  -f -
 
配置东西向网关ip 
 kubectl patch svc  -n istio-system istio-eastwestgateway -p '{"spec":{"externalIPs":["192.168.229.102"]}}'
  
暴露的服务
 kubectl  apply -n istio-system -f /root/istio-1.11.2/samples/multicluster/expose-services.yaml
 
 cluster1:
 生成k8s访问secret
 istioctl x create-remote-secret --name=cluster1  --server=https://192.168.229.137:6443 > remote-secret-cluster1.yaml
 
 传输k8s访问secret
scp remote-secret-cluster1.yaml root@192.168.229.140:/root
scp remote-secret-cluster1.yaml root@192.168.229.143:/root

cluster2：
 生成k8s访问secret
 istioctl x create-remote-secret --name=cluster2  --server=https://192.168.229.140:6443 > remote-secret-cluster2.yaml
 传输k8s访问secret
 scp remote-secret-cluster2.yaml root@192.168.229.137:/root
  scp remote-secret-cluster2.yaml root@192.168.229.143:/root

cluster3
  生成k8s访问secret
 istioctl x create-remote-secret --name=cluster3  --server=https://192.168.229.143:6443 > remote-secret-cluster3.yaml
 传输k8s访问secret
 scp remote-secret-cluster3.yaml root@192.168.229.137:/root
  scp remote-secret-cluster3.yaml root@192.168.229.140:/root
 
 cluster1
 应用k8s访问secret
  kubectl apply -f remote-secret-cluster2.yaml
  kubectl apply -f remote-secret-cluster3.yaml
 
  cluster2:
  应用k8s访问secret
  kubectl apply -f remote-secret-cluster1.yaml
 kubectl apply -f remote-secret-cluster3.yaml
  
 cluster3:
  应用k8s访问secret
  kubectl apply -f remote-secret-cluster1.yaml
  kubectl apply -f remote-secret-cluster2.yaml
 
 cluster1:
 重启pod
 kubectl rollout restart deploy -n istio
 kubectl rollout restart deploy -n istio-system
 

  cluster2:
 重启pod
 kubectl rollout restart deploy -n istio
 kubectl rollout restart deploy -n istio-system

   cluster3:
  重启pod
 kubectl rollout restart deploy -n istio
 kubectl rollout restart deploy -n istio-system


```



cluster1:部署zipkin

```
 kubectl apply -f extras/zipkin.yaml -n istio-system
```

cluster1增加东西向网关端口

```
 
  kubectl patch svc  -n istio-system istio-eastwestgateway -p '{"spec":{"ports":[{"name": "http-zipkin", "nodePort": 32197,"port": 15018, "protocol": "TCP", "targetPort": 15018}]}}'
```

cluster1:

暴露zipkin

visilazation/zipkin-gw-vs.yaml

 kubectl apply -f zipkin-gw-vs.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: Gateway
metadata:
  name: zipkin-gateway
spec:
  selector:
    istio: eastwestgateway
  servers:
    - port:
        name: http-zipkin
        number: 15018
        protocol: http        
      hosts:
        - "*"
---
apiVersion: networking.istio.io/v1alpha3
kind: VirtualService
metadata:
  name: zipkin-vs
spec:
  hosts:
  - "*"
  gateways:
  - zipkin-gateway
  http:
  - route:
    - destination:
        host: zipkin.istio-system.svc.cluster.local
        port:
          number: 9411
```





cluster1，cluster2,cluster3: cm istio   

```
[root@node01 ~]# kubectl get cm istio -n istio-system -o yaml
apiVersion: v1
data:
  mesh: |-
    accessLogFile: /dev/stdout
    enableTracing: true
    defaultConfig:
      discoveryAddress: istiod.istio-system.svc:15012
      meshId: mesh1
      proxyMetadata: {}
      tracing:
        sampling: 100
        zipkin:
          address: 192.168.229.100:15018
    enablePrometheusMerge: true
    enableTracing: true
    rootNamespace: istio-system
    trustDomain: cluster.local
  meshNetworks: 'networks: {}'
  
  修改
        sampling: 100
        zipkin:
          address: 192.168.229.100:15018
```

```
 cluster1: 
  重启pod
 kubectl rollout restart deploy -n istio
 
 cluster2:
   重启pod
 kubectl rollout restart deploy -n istio
 
  
 cluster3:
   重启pod
 kubectl rollout restart deploy -n istio
```



暴露服务：

kubectl port-forward --address 0.0.0.0 -n istio-system zipkin-6b8c6bdc56-m2b4f 9411:9411



![9](44image\9.jpg)

清理：

```
cluster1:

kubectl  label namespace istio-system topology.istio.io/network-
kubectl delete secret istio-remote-secret-cluster2 -n istio-system
kubectl delete secret istio-remote-secret-cluster3 -n istio-system
kubectl delete gw cross-network-gateway -n istio-system
kubectl delete gw zipkin-gateway -n istio-system
kubectl delete vs zipkin-vs -n istio-system
istioctl x uninstall -f cluster1.yaml





reboot



cluster2:

kubectl  label namespace istio-system topology.istio.io/network-
kubectl delete secret istio-remote-secret-cluster1 -n istio-system
kubectl delete secret istio-remote-secret-cluster3 -n istio-system
kubectl delete gw cross-network-gateway -n istio-system
istioctl x uninstall -f cluster2.yaml





reboot



cluster3:

kubectl  label namespace istio-system topology.istio.io/network-
kubectl delete secret istio-remote-secret-cluster1 -n istio-system
kubectl delete secret istio-remote-secret-cluster3 -n istio-system
kubectl delete gw cross-network-gateway -n istio-system
istioctl x uninstall -f cluster3.yaml





reboot
```





##### 三网络

![three-04](D:/公众号/图文/05image/three-04.bmp)





<div  style="white-space: nowrap;overflow-x:scroll;">  </div>
```
三个网络
集群1
137,138,139
集群2
140,141,142
集群3
143,144,145

给istio-system namespace打标签

cluster1:
 kubectl  label namespace istio-system topology.istio.io/network=network1

cluster2:
 kubectl  label namespace istio-system topology.istio.io/network=network2
 
 cluster3:
 kubectl  label namespace istio-system topology.istio.io/network=network3
 
 cluster1:
 生成istio operator部署文件
 cat <<EOF > cluster1.yaml
apiVersion: install.istio.io/v1alpha1
kind: IstioOperator
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
EOF

 生成istio operator部署文件
 cat <<EOF > cluster2.yaml
apiVersion: install.istio.io/v1alpha1
kind: IstioOperator
spec:
  profile: demo
  values:
    global:
      meshID: mesh1
      multiCluster:
        clusterName: cluster2
      network: network2
  meshConfig:
    accessLogFile: /dev/stdout
    enableTracing: true
  components:
    egressGateways:
    - name: istio-egressgateway
      enabled: true
EOF

 生成istio operator部署文件
 cat <<EOF > cluster3.yaml
apiVersion: install.istio.io/v1alpha1
kind: IstioOperator
spec:
  profile: demo
  values:
    global:
      meshID: mesh1
      multiCluster:
        clusterName: cluster3
      network: network3
  meshConfig:
    accessLogFile: /dev/stdout
    enableTracing: true
  components:
    egressGateways:
    - name: istio-egressgateway
      enabled: true
EOF

传输部署文件
scp cluster2.yaml root@192.168.229.140:/root
scp cluster3.yaml root@192.168.229.143:/root

部署istio
istioctl install  -f cluster1.yaml

生成东西向网关
/root/istio-1.11.2/samples/multicluster/gen-eastwest-gateway.sh --mesh mesh1 --cluster cluster1 --network network1 | istioctl  install -y -f -

配置东西向网关ip 
 kubectl patch svc  -n istio-system istio-eastwestgateway -p '{"spec":{"externalIPs":["192.168.229.100"]}}'
 
 暴露服务
 kubectl  apply -n istio-system -f /root/istio-1.11.2/samples/multicluster/expose-services.yaml
 
 
cluster2:
部署istio
 istioctl install -f cluster2.yaml
 
 生成东西向网关
 /root/istio-1.11.2/samples/multicluster/gen-eastwest-gateway.sh --mesh mesh1 --cluster cluster2 --network network2 |  istioctl install -y  -f -
 
配置东西向网关ip 
 kubectl patch svc  -n istio-system istio-eastwestgateway -p '{"spec":{"externalIPs":["192.168.229.101"]}}'
 
 暴露服务
 kubectl  apply -n istio-system -f  /root/istio-1.11.2/samples/multicluster/expose-services.yaml
 
 cluster3:
 部署istio
 istioctl install -f cluster3.yaml
 
  生成东西向网关
  /root/istio-1.11.2/samples/multicluster/gen-eastwest-gateway.sh --mesh mesh1 --cluster cluster3 --network network3 |  istioctl install -y  -f -
  
配置东西向网关ip 
 kubectl patch svc  -n istio-system istio-eastwestgateway -p '{"spec":{"externalIPs":["192.168.229.102"]}}'
 
 暴露服务
 kubectl  apply -n istio-system -f   /root/istio-1.11.2/samples/multicluster/expose-services.yaml
 
 cluster1:
 生成访问apiserver secret
 istioctl x create-remote-secret --name=cluster1  --server=https://192.168.229.137:6443 > remote-secret-cluster1.yaml
 
 传输secret
scp remote-secret-cluster1.yaml root@192.168.229.140:/root
scp remote-secret-cluster1.yaml root@192.168.229.143:/root

cluster2
  生成访问apiserver secret
 istioctl x create-remote-secret --name=cluster2  --server=https://192.168.229.140:6443 > remote-secret-cluster2.yaml
 
  传输secret
 scp remote-secret-cluster2.yaml root@192.168.229.137:/root
  scp remote-secret-cluster2.yaml root@192.168.229.143:/root

cluster3
   生成访问apiserver secret
 istioctl x create-remote-secret --name=cluster3  --server=https://192.168.229.143:6443 > remote-secret-cluster3.yaml
 
  传输secret
 scp remote-secret-cluster3.yaml root@192.168.229.137:/root
  scp remote-secret-cluster3.yaml root@192.168.229.140:/root
 
 cluster1：
  kubectl apply -f remote-secret-cluster2.yaml
  kubectl apply -f remote-secret-cluster3.yaml
  
   cluster2：
  kubectl apply -f remote-secret-cluster1.yaml
  kubectl apply -f remote-secret-cluster3.yaml
  
   cluster3：
  kubectl apply -f remote-secret-cluster1.yaml
  kubectl apply -f remote-secret-cluster2.yaml
  
  cluster1:
 kubectl rollout restart deploy -n istio
 kubectl rollout restart deploy -n istio-system
 
 cluster2:
 kubectl apply -f remote-secret-cluster3.yaml
 
 kubectl rollout restart deploy -n istio
 kubectl rollout restart deploy -n istio-system
 
 cluster3:
 kubectl rollout restart deploy -n istio
 kubectl rollout restart deploy -n istio-system
 

```



cluster1:部署zipkin

```
 kubectl apply -f extras/zipkin.yaml -n istio-system
```

cluster1增加东西向网关端口

```
 
  kubectl patch svc  -n istio-system istio-eastwestgateway -p '{"spec":{"ports":[{"name": "http-zipkin", "nodePort": 32197,"port": 15018, "protocol": "TCP", "targetPort": 15018}]}}'
```

cluster1:

暴露zipkin

visilazation/zipkin-gw-vs.yaml

 kubectl apply -f zipkin-gw-vs.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: Gateway
metadata:
  name: zipkin-gateway
spec:
  selector:
    istio: eastwestgateway
  servers:
    - port:
        name: http-zipkin
        number: 15018
        protocol: http        
      hosts:
        - "*"
---
apiVersion: networking.istio.io/v1alpha3
kind: VirtualService
metadata:
  name: zipkin-vs
spec:
  hosts:
  - "*"
  gateways:
  - zipkin-gateway
  http:
  - route:
    - destination:
        host: zipkin.istio-system.svc.cluster.local
        port:
          number: 9411
```





cluster1，cluster2,cluster3: cm istio   

```
[root@node01 ~]# kubectl get cm istio -n istio-system -o yaml
apiVersion: v1
data:
  mesh: |-
    accessLogFile: /dev/stdout
    enableTracing: true
    defaultConfig:
      discoveryAddress: istiod.istio-system.svc:15012
      meshId: mesh1
      proxyMetadata: {}
      tracing:
        sampling: 100
        zipkin:
          address: 192.168.229.100:15018
    enablePrometheusMerge: true
    enableTracing: true
    rootNamespace: istio-system
    trustDomain: cluster.local
  meshNetworks: 'networks: {}'
  
  修改
        sampling: 100
        zipkin:
          address: 192.168.229.100:15018
```

```
 cluster1: 
  重启pod
 kubectl rollout restart deploy -n istio
 
 cluster2:
   重启pod
 kubectl rollout restart deploy -n istio
 
  
 cluster3:
   重启pod
 kubectl rollout restart deploy -n istio
```



暴露服务：

kubectl port-forward --address 0.0.0.0 -n istio-system zipkin-6b8c6bdc56-m2b4f 9411:9411

![10](44image\10.jpg)

清理：

```
cluster1:

kubectl  label namespace istio-system topology.istio.io/network-
kubectl delete secret istio-remote-secret-cluster2 -n istio-system
kubectl delete secret istio-remote-secret-cluster3 -n istio-system
kubectl delete gw cross-network-gateway -n istio-system
kubectl delete gw zipkin-gateway -n istio-system
kubectl delete vs zipkin-vs -n istio-system
istioctl x uninstall -f cluster1.yaml





reboot



cluster2:

kubectl  label namespace istio-system topology.istio.io/network-
kubectl delete secret istio-remote-secret-cluster1 -n istio-system
kubectl delete secret istio-remote-secret-cluster3 -n istio-system
kubectl delete gw cross-network-gateway -n istio-system
istioctl x uninstall -f cluster2.yaml





reboot



cluster3:

kubectl  label namespace istio-system topology.istio.io/network-
kubectl delete secret istio-remote-secret-cluster1 -n istio-system
kubectl delete secret istio-remote-secret-cluster3 -n istio-system
kubectl delete gw cross-network-gateway -n istio-system
istioctl x uninstall -f cluster3.yaml





reboot

```





# 总结

1不管是单istiod还是多istiod，mesh config都是本集群内的istiod生效，即使本集群proxy不连本集群istiod。我猜测如果proxy连接的是远程istiod，远程istiod也会通过k8s去获取本地istiod的的meshconfig。

2多集群如果是单网络，其他集群的带proxy的endpoint，本集群是可以直接用的，能解析成ip；但是不带proxy的服务不能在远端集群直接使用。所以如果要配置tracing地址，必须东西向网关暴露zipkin，然后把tracing的地址配成东西向网关的地址。

3mesh config配置修改后istiod直接生效，但是proxy不生效，需要重启proxy

4当zipkin所在集群没有东西向网关时需要创建东西向网关