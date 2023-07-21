# 1什么是kafka_mesh

kafka_mesh用于在多个kafka集群中，路由消息。

# 2配置

## 2.1kafka_mesh 

```
{
  "advertised_host": "...",advertised主机
  "advertised_port": "...",advertised端口
  "upstream_clusters": [],上游集群
  "forwarding_rules": []forward规则
}
```

upstream_clusters：

```
{
  "cluster_name": "...",集群名称
  "bootstrap_servers": "...",集群服务器
  "partition_count": "...",分区数量
  "producer_config": "{...}"producer配置
}
```

forwarding_rules：

```
{
  "target_cluster": "...",集群名称
  "topic_prefix": "..."topic前缀
}
```

## 2.2kafka_broker 

```
{
  "stat_prefix": "..."stat前缀
}
```



# 3案例

## 3.1准备工作

### 部署kafka operator

参考：

https://github.com/banzaicloud/koperator



```
git clone https://github.com/banzaicloud/koperator.git
cd koperator/
cd charts/kafka-operator/

编辑values.yaml

helm repo add banzaicloud-stable https://kubernetes-charts.banzaicloud.com
helm install kafka-operator --create-namespace --namespace=kafka .


[root@node01 kafka-operator]# kubectl get crd|grep kafka
kafkaclusters.kafka.banzaicloud.io         2022-03-30T00:23:54Z
kafkatopics.kafka.banzaicloud.io           2022-03-30T00:23:54Z
kafkausers.kafka.banzaicloud.io            2022-03-30T00:23:54Z

[root@node01 kafka-operator]# kubectl get pod -n kafka -w
NAME                                       READY   STATUS    RESTARTS   AGE
kafka-operator-operator-58cff759c5-smqds   1/1     Running   0          18s
```



values.yaml

```
# Default values for Koperator Helm chart.
# This is a YAML-formatted file.
# Declare variables to be passed into your templates.

replicaCount: 1

# Lists the secrets you need to use to pull kafka-operator image from a private registry.
imagePullSecrets: []
  # - private-registry-key

operator:
  annotations: {}
  image:
    repository: ghcr.io/banzaicloud/kafka-operator
    tag: ""
    pullPolicy: IfNotPresent
  namespaces: ""
  verboseLogging: false
  developmentLogging: false
  resources:
    limits:
      cpu: 200m
      memory: 256Mi
    requests:
      cpu: 100m
      memory: 128Mi
  serviceAccount:
    create: true
    name: kafka-operator

webhook:
  enabled: true
#  serverPort:
#  tls:
#    certDir: ""
  certs:
    generate: true
    secret: "kafka-operator-serving-cert"

certManager:
  namespace: "cert-manager"
  enabled: false

certSigning:
  enabled: true

alertManager:
  enable: false

prometheusMetrics:
  enabled: false
  authProxy:
    enabled: false
    image:
      repository: gcr.io/kubebuilder/kube-rbac-proxy
      tag: v0.8.0
      pullPolicy: IfNotPresent
    serviceAccount:
      create: true
      name: kafka-operator-authproxy

#metricEndpoint:
#  port:

nameOverride: ""
fullnameOverride: ""

rbac:
  enabled: true

crd:
  enabled: true

nodeSelector: {}

tolerations: []

affinity: {}

## Additional Sidecars Configuration.
additionalSidecars: {}
# - name: nginx
#   image: nginx:latest

## Additional Environment Variables.
additionalEnv: {}

## Additional volumes required for sidecar(s).
additionalVolumes: {}
# - name: volume1
#   emptyDir: {}
# - name: volume2
#   emptyDir: {}

# Pod Security Context
# See https://kubernetes.io/docs/tasks/configure-pod-container/security-context/
podSecurityContext: {}
# Container Security Context
containerSecurityContext: {}
```

### 部署nfs

192.168.229.132

```
yum install -y nfs-utils
mkdir /nfs_data

编辑/etc/exports文件添加需要共享目录，每个目录的设置独占一行，编写
/nfs_data 192.168.0.0/16(rw,sync,insecure,no_subtree_check,no_root_squash)

设置开机启动rpcbind和nfs服务
systemctl enable rpcbind.service
systemctl enable nfs-server.service

然后分别启动rpcbind和nfs服务：
systemctl start rpcbind.service
systemctl start nfs-server.service
```

192.168.229.131

```
在客户机使用命令检查：
[root@node01 ~]# showmount -e 192.168.229.132
Export list for 192.168.229.132:
/nfs_data 192.168.0.0/16

```

安装 nfs-client-provisioner

```
[root@node01 ~]# helm search repo c7n/nfs-client-provisioner
WARNING: Kubernetes configuration file is group-readable. This is insecure. Location: /root/.kube/config
WARNING: Kubernetes configuration file is world-readable. This is insecure. Location: /root/.kube/config
NAME                            CHART VERSION   APP VERSION     DESCRIPTION                         
c7n/nfs-client-provisioner      0.1.1           v3.1.0-k8s1.11  nfs-client-provisioner for Choerodon

helm pull c7n/nfs-client-provisioner

tar zvxf nfs-client-provisioner-0.1.1.tgz 

cd nfs-client-provisioner/


```



```
# Default values for redis.
# This is a YAML-formatted file.
# Declare variables to be passed into your templates.

image:
  repository: registry.cn-shanghai.aliyuncs.com/c7n/nfs-client-provisioner
  tag: v3.1.0-k8s1.11
  pullPolicy: IfNotPresent

strategy:
  type: Recreate 

storageClass: 
  name: nfs-client-provisioner
  provisioner: choerodon.io/nfs-client-provisioner
  archiveOnDelete: true

persistence:
  enabled: true
  nfsServer: 192.168.229.132
  nfsPath: /nfs_data
  

rbac:
  create: true
  serviceAccountName: default

resources: {}
  # We usually recommend not to specify default resources and to leave this as a conscious
  # choice for the user. This also increases chances charts run on environments with little
  # resources, such as Minikube. If you do want to specify resources, uncomment the following
  # lines, adjust them as necessary, and remove the curly braces after 'resources:'.
  # limits:
  #  cpu: 100m
  #  memory: 128Mi
  # requests:
  #  cpu: 100m
  #  memory: 128Mi
```

安装

```
kubectl create ns nfs
helm install nfs -n nfs .
```



### 部署zookeeper

安装zookeeper-operator

```
git clone https://github.com/pravega/zookeeper-operator.git
cd zookeeper-operator/charts/zookeeper-operator/

kubectl create ns zookeeper
helm install zookeeper-operator -n zookeeper .
```

zookeeperCluster-demo.yaml

kubectl apply -f zookeeperCluster-demo.yaml -n zookeeper

```
apiVersion: zookeeper.pravega.io/v1beta1
kind: ZookeeperCluster
metadata:
  name: zookeeper
spec:
  replicas: 3
  image:
    repository: pravega/zookeeper
    tag: 0.2.13
  storageType: persistence
  persistence:
    reclaimPolicy: Retain
    spec:
      storageClassName: "nfs-client-provisioner"
      resources:
        requests:
          storage: 1Gi
```

### 部署istio-operator

https://github.com/banzaicloud/istio-operator/

```
git clone https://github.com/banzaicloud/istio-operator.git
cd istio-operator/deploy/charts/istio-operator/
编辑values.yaml

helm install istio-operator -n kafka .

[root@node01 templates]# kubectl get crd|grep servicemesh
istiocontrolplanes.servicemesh.cisco.com       2022-03-30T01:07:55Z
istiomeshes.servicemesh.cisco.com              2022-03-30T01:07:56Z
istiomeshgateways.servicemesh.cisco.com        2022-03-30T01:07:55Z
peeristiocontrolplanes.servicemesh.cisco.com   2022-03-30T01:07:56Z
```

values.yaml

```
image:
  repository: ghcr.io/banzaicloud/istio-operator
  tag: "v2.12.0"
  pullPolicy: IfNotPresent
replicaCount: 1
extraArgs: []
resources:
  requests:
    cpu: 200m
    memory: 256Mi
podAnnotations:
  sidecar.istio.io/inject: "false"
podSecurityContext:
  fsGroup: 1337
securityContext:
  runAsUser: 1337
  runAsGroup: 1337
  runAsNonRoot: true
  capabilities:
    drop:
      - ALL
nodeSelector: {}
tolerations: []
affinity: {}
imagePullSecrets: []

# If you want the operator to expose the /metrics
prometheusMetrics:
  enabled: false
  # Enable or disable the auth proxy (https://github.com/brancz/kube-rbac-proxy)
  # which protects your /metrics endpoint.
  authProxy:
    enabled: true
    image:
      repository: gcr.io/kubebuilder/kube-rbac-proxy
      tag: "v0.8.0"
      pullPolicy: IfNotPresent

## Role Based Access
## Ref: https://kubernetes.io/docs/admin/authorization/rbac/
##
rbac:
  enabled: true

nameOverride: ""
fullnameOverride: ""

useNamespaceResource: false

leaderElection:
  enabled: false
  namespace: "istio-system"
  nameOverride: ""

apiServerEndpointAddress: ""
clusterRegistry:
  clusterAPI:
    enabled: false
  resourceSyncRules:
    enabled: false
```

继续报错

```
{"level":"error","ts":"2022-03-30T01:14:02.968Z","logger":"controller.KafkaCluster","msg":"Reconciler error","reconciler group":"kafka.banzaicloud.io","reconciler kind":"KafkaCluster","name":"kafka","namespace":"istio","error":"could not update status for external listeners: could not get service corresponding to the external listener: could not get LoadBalancer service: Service \"meshgateway-external-kafka\" not found","errorVerbose":"Service \"meshgateway-external-kafka\" not found\ncould not get LoadBalancer service\ngithub.com/banzaicloud/koperator/pkg/resources/kafka.getServiceFromExternalListener\n\t/workspace/pkg/resources/kafka/kafka.go:1154\ngithub.com/banzaicloud/koperator/pkg/resources/kafka.(*Reconciler).createExternalListenerStatuses\n\t/workspace/pkg/resources/kafka/kafka.go:997\ngithub.com/banzaicloud/koperator/pkg/resources/kafka.(*Reconciler).Reconcile\n\t/workspace/pkg/resources/kafka/kafka.go:180\ngithub.com/banzaicloud/koperator/controllers.(*KafkaClusterReconciler).Reconcile\n\t/workspace/controllers/kafkacluster_controller.go:125\nsigs.k8s.io/controller-runtime/pkg/internal/controller.(*Controller).Reconcile\n\t/go/pkg/mod/sigs.k8s.io/controller-runtime@v0.11.0/pkg/internal/controller/controller.go:114\nsigs.k8s.io/controller-runtime/pkg/internal/controller.(*Controller).reconcileHandler\n\t/go/pkg/mod/sigs.k8s.io/controller-runtime@v0.11.0/pkg/internal/controller/controller.go:311\nsigs.k8s.io/controller-runtime/pkg/internal/controller.(*Controller).processNextWorkItem\n\t/go/pkg/mod/sigs.k8s.io/controller-runtime@v0.11.0/pkg/internal/controller/controller.go:266\nsigs.k8s.io/controller-runtime/pkg/internal/controller.(*Controller).Start.func2.2\n\t/go/pkg/mod/sigs.k8s.io/controller-runtime@v0.11.0/pkg/internal/controller/controller.go:227\nruntime.goexit\n\t/usr/local/go/src/runtime/asm_amd64.s:1581\ncould not get service corresponding to the external listener\ncould not update status for external listeners","stacktrace":"sigs.k8s.io/controller-runtime/pkg/internal/controller.(*Controller).processNextWorkItem\n\t/go/pkg/mod/sigs.k8s.io/controller-runtime@v0.11.0/pkg/internal/controller/controller.go:266\nsigs.k8s.io/controller-runtime/pkg/internal/controller.(*Controller).Start.func2.2\n\t/go/pkg/mod/sigs.k8s.io/controller-runtime@v0.11.0/pkg/internal/controller/controller.go:227"}
```

越来越复杂了，必须部署istio-operator资源



### 部署istio

istioControlPlane-demo.yaml

```
apiVersion: servicemesh.cisco.com/v1alpha1
kind: IstioControlPlane
metadata:
  name: icp-v112x-sample
spec:
  version: 1.12.5
  mode: ACTIVE
  meshID: mesh1
  networkName: network1
  logging:
    level: "default:info"
  mountMtlsCerts: false
  meshExpansion:
    enabled: false
  istiod:
    deployment:
      replicas:
        min: 1
        max: 5
        count: 1
      image: "registry.cn-hangzhou.aliyuncs.com/hxpdocker/pilot:1.12.5"
      resources:
        requests:
          cpu: 50m
          memory: 1048Mi
      nodeSelector: {}
      affinity: {}
      tolerations: []
      podMetadata:
        labels: {}
        annotations: {}
      securityContext: {}
    enableAnalysis: false
    enableStatus: false
    externalIstiod:
      enabled: false
    traceSampling: 1.0
    enableProtocolSniffingOutbound: true
    enableProtocolSniffingInbound: true
    certProvider: ISTIOD
    spiffe:
      operatorEndpoints:
        enabled: false
  proxy:
    image: "registry.cn-hangzhou.aliyuncs.com/hxpdocker/proxyv2:1.12.5"
    privileged: false
    enableCoreDump: false
    logLevel: "WARNING"
    componentLogLevel: "misc:error"
    clusterDomain: "cluster.local"
    holdApplicationUntilProxyStarts: false
    lifecycle: {}
    resources:
      requests:
        cpu: 10m
        memory: 128Mi
      limits:
        cpu: 2000m
        memory: 1024Mi
    includeIPRanges: "*"
    excludeIPRanges: ""
    excludeInboundPorts: ""
    excludeOutboundPorts: ""
  proxyInit:
    image: "registry.cn-hangzhou.aliyuncs.com/hxpdocker/proxyv2:1.12.5"
    resources:
      limits:
        cpu: 2000m
        memory: 1024Mi
      requests:
        cpu: 10m
        memory: 10Mi
  telemetryV2:
    enabled: true
  sds:
    tokenAudience: "istio-ca"
  proxyWasm:
    enabled: false
  watchOneNamespace: false
  caAddress: ""
  distribution: "official"
  httpProxyEnvs:
    httpProxy: ""
    httpsProxy: ""
    noProxy: ""
  meshConfig:
    proxyListenPort: 15001
    connectTimeout: 10s
    protocolDetectionTimeout: 5s
    ingressClass: istio
    ingressService: istio-ingressgateway
    ingressControllerMode: STRICT
    ingressSelector: istio-ingressgateway
    enableTracing: false
    accessLogFile: /dev/stdout
    accessLogFormat: ""
    accessLogEncoding: TEXT
    enableEnvoyAccessLogService: false
    disableEnvoyListenerLog: false
    defaultConfig:
      configPath: ./etc/istio/proxy
      binaryPath: /usr/local/bin/envoy
      serviceCluster: istio-proxy
      drainDuration: 45s
      parentShutdownDuration: 60s
      proxyAdminPort: 15000
      controlPlaneAuthPolicy: MUTUAL_TLS
      concurrency: 2
    outboundTrafficPolicy:
      mode: ALLOW_ANY
    enableAutoMtls: true
    trustDomain: cluster.local
    trustDomainAliases: []
    rootNamespace: istio-system
    dnsRefreshRate: 5s
```



```
[root@node01 kafka_mesh]# kubectl apply -f istioControlPlane-demo.yaml -n istio-system
istiocontrolplane.servicemesh.cisco.com/icp-v112x-sample created
[root@node01 kafka_mesh]# kubectl get pod -n istio-system
NAME                                       READY   STATUS    RESTARTS   AGE
istiod-icp-v112x-sample-7ccd7ddc5d-jf69m   1/1     Running   0          4s
```

istiomeshgateway-demo.yaml

```
apiVersion: servicemesh.cisco.com/v1alpha1
kind: IstioMeshGateway
metadata:
  name: ingressgateway
spec:
  deployment:
    metadata:
      labels:
        app: istio-meshexpansion-gateway
        gateway-name: istio-meshexpansion-gateway-cp-v19x
        gateway-type: ingress
        istio: meshexpansiongateway
        istio.io/rev: cp-v19x.istio-system
    replicas:
      count: 1
      min: 1
      max: 1
    resources:
      limits:
        cpu: "2"
        memory: 1Gi
      requests:
        cpu: 100m
        memory: 128Mi
    securityContext:
      runAsGroup: 0
      runAsNonRoot: false
      runAsUser: 0
  istioControlPlane:
    name: icp-v112x-sample
    namespace: istio-system
  runAsRoot: true
  service:
    ports:
    - name: tcp-8080
      port: 80
      protocol: TCP
      targetPort: 8080
    - name: tls-443
      port: 443
      protocol: TCP
      targetPort: 8443
    - name: tcp-als-tls
      port: 50600
      protocol: TCP
      targetPort: 50600
    - name: tcp-zipkin-tls
      port: 59411
      protocol: TCP
      targetPort: 59411
    type: NodePort
  type: ingress
```



mesh-config.yaml

```
apiVersion: servicemesh.cisco.com/v1alpha1
kind: IstioMesh
metadata:
  name: mesh1
spec:
  config:
    connectTimeout: 9s
```

==The `istio.io/rev=<YOUR_ICP_NAME>.istio-system` label should be used on the namespace and no `istio-injection=enabled`==

==istio注入sidecar的注解有所变化==

istio.io/rev=icp-v112x-sample.istio-system

继续安装kafka cluster

```
 kubectl apply -f kafkacluster-with-istio.yaml -n istio
```



继续报错

```
{"level":"info","ts":"2022-03-30T04:03:10.985Z","logger":"controller.KafkaCluster","msg":"A new resource was not found or may not be ready","reconciler group":"kafka.banzaicloud.io","reconciler kind":"KafkaCluster","name":"kafka","namespace":"istio","error":"could not update status for external listeners: could not extract IP from LoadBalancer service: trying: loadbalancer ingress is not created waiting"}
```

### 部署metallb

https://github.com/metallb/metallb.git

```
git clone --branch v0.9 https://github.com/metallb/metallb.git
cd metallb/manifests
kubectl apply -f namespace.yaml 
kubectl apply -f metallb.yaml -n metallb-system
```



会有个问题，解决方法：

```
kubectl create secret generic -n metallb-system memberlist --from-literal=secretkey="$(openssl rand -base64 128)"
```



创建配置文件

example-layer2-config.yaml

```
apiVersion: v1
kind: ConfigMap
metadata:
  namespace: metallb-system
  name: config
data:
  config: |
    address-pools:
    - name: my-ip-space
      protocol: layer2
      addresses:
      - 192.168.229.240/28
```



### 部署kafka

cd config/samples/

kafkacluster-with-istio.yaml 

```
apiVersion: kafka.banzaicloud.io/v1beta1
kind: KafkaCluster
metadata:
  labels:
    controller-tools.k8s.io: "1.0"
  name: kafka
spec:
  headlessServiceEnabled: false
  ingressController: "istioingress"
  istioControlPlane:
    name: icp-v112x-sample
    namespace: istio-system
  istioIngressConfig:
    gatewayConfig:
      mode: ISTIO_MUTUAL
  zkAddresses:
    - "zookeeper-client.zookeeper:2181"
  oneBrokerPerNode: false
  clusterImage: "ghcr.io/banzaicloud/kafka:2.13-3.1.0"
  readOnlyConfig: |
    auto.create.topics.enable=false
    cruise.control.metrics.topic.auto.create=true
    cruise.control.metrics.topic.num.partitions=1
    cruise.control.metrics.topic.replication.factor=2
  brokerConfigGroups:
    default:
      resourceRequirements:
        requests:
          cpu: 10m
          memory: 20Mi
      brokerAnnotations:
        sidecar.istio.io/userVolumeMount: '[{"name":"exitfile", "mountPath":"/var/run/wait", "readonly":true}]'
      storageConfigs:
        - mountPath: "/kafka-logs"
          pvcSpec:
            accessModes:
              - ReadWriteOnce
            storageClassName: nfs-client-provisioner
            resources:
              requests:
                storage: 1Gi
  brokers:
    - id: 0
      brokerConfigGroup: "default"
    - id: 1
      brokerConfigGroup: "default"
    - id: 2
      brokerConfigGroup: "default"
  rollingUpgradeConfig:
    failureThreshold: 1
  listenersConfig:
    internalListeners:
      - type: "plaintext"
        name: "internal"
        containerPort: 29092
        usedForInnerBrokerCommunication: true
      - type: "plaintext"
        name: "controller"
        containerPort: 29093
        usedForInnerBrokerCommunication: false
        usedForControllerCommunication: true
    externalListeners:
      - type: "plaintext"
        name: "external"
        externalStartingPort: 19090
        containerPort: 9094
  cruiseControlConfig:
    topicConfig:
      partitions: 12
      replicationFactor: 3
    config: |
      # Copyright 2017 LinkedIn Corp. Licensed under the BSD 2-Clause License (the "License"). See License in the project root for license information.
      #
      # This is an example property file for Kafka Cruise Control. See KafkaCruiseControlConfig for more details.
      # Configuration for the metadata client.
      # =======================================
      # The maximum interval in milliseconds between two metadata refreshes.
      #metadata.max.age.ms=300000
      # Client id for the Cruise Control. It is used for the metadata client.
      #client.id=kafka-cruise-control
      # The size of TCP send buffer bytes for the metadata client.
      #send.buffer.bytes=131072
      # The size of TCP receive buffer size for the metadata client.
      #receive.buffer.bytes=131072
      # The time to wait before disconnect an idle TCP connection.
      #connections.max.idle.ms=540000
      # The time to wait before reconnect to a given host.
      #reconnect.backoff.ms=50
      # The time to wait for a response from a host after sending a request.
      #request.timeout.ms=30000
      # Configurations for the load monitor
      # =======================================
      # The number of metric fetcher thread to fetch metrics for the Kafka cluster
      num.metric.fetchers=1
      # The metric sampler class
      metric.sampler.class=com.linkedin.kafka.cruisecontrol.monitor.sampling.CruiseControlMetricsReporterSampler
      # Configurations for CruiseControlMetricsReporterSampler
      metric.reporter.topic.pattern=__CruiseControlMetrics
      # The sample store class name
      sample.store.class=com.linkedin.kafka.cruisecontrol.monitor.sampling.KafkaSampleStore
      # The config for the Kafka sample store to save the partition metric samples
      partition.metric.sample.store.topic=__KafkaCruiseControlPartitionMetricSamples
      # The config for the Kafka sample store to save the model training samples
      broker.metric.sample.store.topic=__KafkaCruiseControlModelTrainingSamples
      # The replication factor of Kafka metric sample store topic
      sample.store.topic.replication.factor=2
      # The config for the number of Kafka sample store consumer threads
      num.sample.loading.threads=8
      # The partition assignor class for the metric samplers
      metric.sampler.partition.assignor.class=com.linkedin.kafka.cruisecontrol.monitor.sampling.DefaultMetricSamplerPartitionAssignor
      # The metric sampling interval in milliseconds
      metric.sampling.interval.ms=120000
      metric.anomaly.detection.interval.ms=180000
      # The partition metrics window size in milliseconds
      partition.metrics.window.ms=300000
      # The number of partition metric windows to keep in memory
      num.partition.metrics.windows=1
      # The minimum partition metric samples required for a partition in each window
      min.samples.per.partition.metrics.window=1
      # The broker metrics window size in milliseconds
      broker.metrics.window.ms=300000
      # The number of broker metric windows to keep in memory
      num.broker.metrics.windows=20
      # The minimum broker metric samples required for a partition in each window
      min.samples.per.broker.metrics.window=1
      # The configuration for the BrokerCapacityConfigFileResolver (supports JBOD and non-JBOD broker capacities)
      capacity.config.file=config/capacity.json
      #capacity.config.file=config/capacityJBOD.json
      # Configurations for the analyzer
      # =======================================
      # The list of goals to optimize the Kafka cluster for with pre-computed proposals
      default.goals=com.linkedin.kafka.cruisecontrol.analyzer.goals.ReplicaCapacityGoal,com.linkedin.kafka.cruisecontrol.analyzer.goals.DiskCapacityGoal,com.linkedin.kafka.cruisecontrol.analyzer.goals.NetworkInboundCapacityGoal,com.linkedin.kafka.cruisecontrol.analyzer.goals.NetworkOutboundCapacityGoal,com.linkedin.kafka.cruisecontrol.analyzer.goals.CpuCapacityGoal,com.linkedin.kafka.cruisecontrol.analyzer.goals.ReplicaDistributionGoal,com.linkedin.kafka.cruisecontrol.analyzer.goals.PotentialNwOutGoal,com.linkedin.kafka.cruisecontrol.analyzer.goals.DiskUsageDistributionGoal,com.linkedin.kafka.cruisecontrol.analyzer.goals.NetworkInboundUsageDistributionGoal,com.linkedin.kafka.cruisecontrol.analyzer.goals.NetworkOutboundUsageDistributionGoal,com.linkedin.kafka.cruisecontrol.analyzer.goals.CpuUsageDistributionGoal,com.linkedin.kafka.cruisecontrol.analyzer.goals.TopicReplicaDistributionGoal,com.linkedin.kafka.cruisecontrol.analyzer.goals.LeaderBytesInDistributionGoal
      # The list of supported goals
      goals=com.linkedin.kafka.cruisecontrol.analyzer.goals.ReplicaCapacityGoal,com.linkedin.kafka.cruisecontrol.analyzer.goals.DiskCapacityGoal,com.linkedin.kafka.cruisecontrol.analyzer.goals.NetworkInboundCapacityGoal,com.linkedin.kafka.cruisecontrol.analyzer.goals.NetworkOutboundCapacityGoal,com.linkedin.kafka.cruisecontrol.analyzer.goals.CpuCapacityGoal,com.linkedin.kafka.cruisecontrol.analyzer.goals.ReplicaDistributionGoal,com.linkedin.kafka.cruisecontrol.analyzer.goals.PotentialNwOutGoal,com.linkedin.kafka.cruisecontrol.analyzer.goals.DiskUsageDistributionGoal,com.linkedin.kafka.cruisecontrol.analyzer.goals.NetworkInboundUsageDistributionGoal,com.linkedin.kafka.cruisecontrol.analyzer.goals.NetworkOutboundUsageDistributionGoal,com.linkedin.kafka.cruisecontrol.analyzer.goals.CpuUsageDistributionGoal,com.linkedin.kafka.cruisecontrol.analyzer.goals.TopicReplicaDistributionGoal,com.linkedin.kafka.cruisecontrol.analyzer.goals.LeaderBytesInDistributionGoal,com.linkedin.kafka.cruisecontrol.analyzer.kafkaassigner.KafkaAssignerDiskUsageDistributionGoal,com.linkedin.kafka.cruisecontrol.analyzer.goals.PreferredLeaderElectionGoal
      # The list of supported hard goals
      hard.goals=com.linkedin.kafka.cruisecontrol.analyzer.goals.ReplicaCapacityGoal,com.linkedin.kafka.cruisecontrol.analyzer.goals.DiskCapacityGoal,com.linkedin.kafka.cruisecontrol.analyzer.goals.NetworkInboundCapacityGoal,com.linkedin.kafka.cruisecontrol.analyzer.goals.NetworkOutboundCapacityGoal,com.linkedin.kafka.cruisecontrol.analyzer.goals.CpuCapacityGoal
      # The minimum percentage of well monitored partitions out of all the partitions
      min.monitored.partition.percentage=0.95
      # The balance threshold for CPU
      cpu.balance.threshold=1.1
      # The balance threshold for disk
      disk.balance.threshold=1.1
      # The balance threshold for network inbound utilization
      network.inbound.balance.threshold=1.1
      # The balance threshold for network outbound utilization
      network.outbound.balance.threshold=1.1
      # The balance threshold for the replica count
      replica.count.balance.threshold=1.1
      # The capacity threshold for CPU in percentage
      cpu.capacity.threshold=0.8
      # The capacity threshold for disk in percentage
      disk.capacity.threshold=0.8
      # The capacity threshold for network inbound utilization in percentage
      network.inbound.capacity.threshold=0.8
      # The capacity threshold for network outbound utilization in percentage
      network.outbound.capacity.threshold=0.8
      # The threshold to define the cluster to be in a low CPU utilization state
      cpu.low.utilization.threshold=0.0
      # The threshold to define the cluster to be in a low disk utilization state
      disk.low.utilization.threshold=0.0
      # The threshold to define the cluster to be in a low network inbound utilization state
      network.inbound.low.utilization.threshold=0.0
      # The threshold to define the cluster to be in a low disk utilization state
      network.outbound.low.utilization.threshold=0.0
      # The metric anomaly percentile upper threshold
      metric.anomaly.percentile.upper.threshold=90.0
      # The metric anomaly percentile lower threshold
      metric.anomaly.percentile.lower.threshold=10.0
      # How often should the cached proposal be expired and recalculated if necessary
      proposal.expiration.ms=60000
      # The maximum number of replicas that can reside on a broker at any given time.
      max.replicas.per.broker=10000
      # The number of threads to use for proposal candidate precomputing.
      num.proposal.precompute.threads=1
      # the topics that should be excluded from the partition movement.
      #topics.excluded.from.partition.movement
      # Configurations for the executor
      # =======================================
      # The max number of partitions to move in/out on a given broker at a given time.
      num.concurrent.partition.movements.per.broker=10
      # The interval between two execution progress checks.
      execution.progress.check.interval.ms=10000
      # Configurations for anomaly detector
      # =======================================
      # The goal violation notifier class
      anomaly.notifier.class=com.linkedin.kafka.cruisecontrol.detector.notifier.SelfHealingNotifier
      # The metric anomaly finder class
      metric.anomaly.finder.class=com.linkedin.kafka.cruisecontrol.detector.KafkaMetricAnomalyFinder
      # The anomaly detection interval
      anomaly.detection.interval.ms=10000
      # The goal violation to detect.
      anomaly.detection.goals=com.linkedin.kafka.cruisecontrol.analyzer.goals.ReplicaCapacityGoal,com.linkedin.kafka.cruisecontrol.analyzer.goals.DiskCapacityGoal,com.linkedin.kafka.cruisecontrol.analyzer.goals.NetworkInboundCapacityGoal,com.linkedin.kafka.cruisecontrol.analyzer.goals.NetworkOutboundCapacityGoal,com.linkedin.kafka.cruisecontrol.analyzer.goals.CpuCapacityGoal
      # The interested metrics for metric anomaly analyzer.
      metric.anomaly.analyzer.metrics=BROKER_PRODUCE_LOCAL_TIME_MS_MAX,BROKER_PRODUCE_LOCAL_TIME_MS_MEAN,BROKER_CONSUMER_FETCH_LOCAL_TIME_MS_MAX,BROKER_CONSUMER_FETCH_LOCAL_TIME_MS_MEAN,BROKER_FOLLOWER_FETCH_LOCAL_TIME_MS_MAX,BROKER_FOLLOWER_FETCH_LOCAL_TIME_MS_MEAN,BROKER_LOG_FLUSH_TIME_MS_MAX,BROKER_LOG_FLUSH_TIME_MS_MEAN
      ## Adjust accordingly if your metrics reporter is an older version and does not produce these metrics.
      #metric.anomaly.analyzer.metrics=BROKER_PRODUCE_LOCAL_TIME_MS_50TH,BROKER_PRODUCE_LOCAL_TIME_MS_999TH,BROKER_CONSUMER_FETCH_LOCAL_TIME_MS_50TH,BROKER_CONSUMER_FETCH_LOCAL_TIME_MS_999TH,BROKER_FOLLOWER_FETCH_LOCAL_TIME_MS_50TH,BROKER_FOLLOWER_FETCH_LOCAL_TIME_MS_999TH,BROKER_LOG_FLUSH_TIME_MS_50TH,BROKER_LOG_FLUSH_TIME_MS_999TH
      # The zk path to store failed broker information.
      failed.brokers.zk.path=/CruiseControlBrokerList
      # Topic config provider class
      topic.config.provider.class=com.linkedin.kafka.cruisecontrol.config.KafkaTopicConfigProvider
      # The cluster configurations for the KafkaTopicConfigProvider
      cluster.configs.file=config/clusterConfigs.json
      # The maximum time in milliseconds to store the response and access details of a completed user task.
      completed.user.task.retention.time.ms=21600000
      # The maximum time in milliseconds to retain the demotion history of brokers.
      demotion.history.retention.time.ms=86400000
      # The maximum number of completed user tasks for which the response and access details will be cached.
      max.cached.completed.user.tasks=100
      # The maximum number of user tasks for concurrently running in async endpoints across all users.
      max.active.user.tasks=25
      # Enable self healing for all anomaly detectors, unless the particular anomaly detector is explicitly disabled
      self.healing.enabled=true
      # Enable self healing for broker failure detector
      #self.healing.broker.failure.enabled=true
      # Enable self healing for goal violation detector
      #self.healing.goal.violation.enabled=true
      # Enable self healing for metric anomaly detector
      #self.healing.metric.anomaly.enabled=true
      # configurations for the webserver
      # ================================
      # HTTP listen port
      webserver.http.port=9090
      # HTTP listen address
      webserver.http.address=0.0.0.0
      # Whether CORS support is enabled for API or not
      webserver.http.cors.enabled=false
      # Value for Access-Control-Allow-Origin
      webserver.http.cors.origin=http://localhost:8080/
      # Value for Access-Control-Request-Method
      webserver.http.cors.allowmethods=OPTIONS,GET,POST
      # Headers that should be exposed to the Browser (Webapp)
      # This is a special header that is used by the
      # User Tasks subsystem and should be explicitly
      # Enabled when CORS mode is used as part of the
      # Admin Interface
      webserver.http.cors.exposeheaders=User-Task-ID
      # REST API default prefix
      # (dont forget the ending *)
      webserver.api.urlprefix=/kafkacruisecontrol/*
      # Location where the Cruise Control frontend is deployed
      webserver.ui.diskpath=./cruise-control-ui/dist/
      # URL path prefix for UI
      # (dont forget the ending *)
      webserver.ui.urlprefix=/*
      # Time After which request is converted to Async
      webserver.request.maxBlockTimeMs=10000
      # Default Session Expiry Period
      webserver.session.maxExpiryTimeMs=60000
      # Session cookie path
      webserver.session.path=/
      # Server Access Logs
      webserver.accesslog.enabled=true
      # Location of HTTP Request Logs
      webserver.accesslog.path=access.log
      # HTTP Request Log retention days
      webserver.accesslog.retention.days=14
    clusterConfig: |
      {
        "min.insync.replicas": 3
      }
```

报错

```
{"level":"error","ts":"2022-03-30T00:54:42.290Z","logger":"controller.KafkaCluster","msg":"Reconciler error","reconciler group":"kafka.banzaicloud.io","reconciler kind":"KafkaCluster","name":"kafka","namespace":"istio","error":"getting resource failed: no matches for kind \"IstioMeshGateway\" in version \"servicemesh.cisco.com/v1alpha1\"","stacktrace":"sigs.k8s.io/controller-runtime/pkg/internal/controller.(*Controller).processNextWorkItem\n\t/go/pkg/mod/sigs.k8s.io/controller-runtime@v0.11.0/pkg/internal/controller/controller.go:266\nsigs.k8s.io/controller-runtime/pkg/internal/controller.(*Controller).Start.func2.2\n\t/go/pkg/mod/sigs.k8s.io/controller-runtime@v0.11.0/pkg/internal/controller/controller.go:227"}
```



### 部署kafka manager

```
helm repo add stable http://mirror.azure.cn/kubernetes/charts

[root@node01 config]# helm search repo kafka-manager
WARNING: Kubernetes configuration file is group-readable. This is insecure. Location: /root/.kube/config
WARNING: Kubernetes configuration file is world-readable. This is insecure. Location: /root/.kube/config
NAME                    CHART VERSION   APP VERSION     DESCRIPTION                                   
stable/kafka-manager    2.3.5           1.3.3.22        DEPRECATED - A tool for managing Apache Kafka

helm pull stable/kafka-manager

 tar zvxf kafka-manager-2.3.5.tgz 
 
 helm install kafka-manager -n kafka .
```

需要启动zookeeper，修改values .yaml



### 部署第二个kafka集群

kafkacluster-with-istio-2.yaml

kubectl apply -f kafkacluster-with-istio-2.yaml -n zookeeper

```
apiVersion: kafka.banzaicloud.io/v1beta1
kind: KafkaCluster
metadata:
  labels:
    controller-tools.k8s.io: "1.0"
  name: kafka-c2
spec:
  headlessServiceEnabled: false
  ingressController: "istioingress"
  istioControlPlane:
    name: icp-v112x-sample
    namespace: istio-system
  istioIngressConfig:
    gatewayConfig:
      mode: ISTIO_MUTUAL
  zkAddresses:
    - "zookeeper-2-client.zookeeper:2181"
  oneBrokerPerNode: false
  clusterImage: "ghcr.io/banzaicloud/kafka:2.13-3.1.0"
  readOnlyConfig: |
    auto.create.topics.enable=false
    cruise.control.metrics.topic.auto.create=true
    cruise.control.metrics.topic.num.partitions=1
    cruise.control.metrics.topic.replication.factor=2
  brokerConfigGroups:
    default:
      resourceRequirements:
        requests:
          cpu: 10m
          memory: 20Mi
      brokerAnnotations:
        sidecar.istio.io/userVolumeMount: '[{"name":"exitfile", "mountPath":"/var/run/wait", "readonly":true}]'
      storageConfigs:
        - mountPath: "/kafka-logs"
          pvcSpec:
            accessModes:
              - ReadWriteOnce
            storageClassName: nfs-client-provisioner
            resources:
              requests:
                storage: 1Gi
  brokers:
    - id: 0
      brokerConfigGroup: "default"
    - id: 1
      brokerConfigGroup: "default"
    - id: 2
      brokerConfigGroup: "default"
  rollingUpgradeConfig:
    failureThreshold: 1
  listenersConfig:
    internalListeners:
      - type: "plaintext"
        name: "internal"
        containerPort: 29092
        usedForInnerBrokerCommunication: true
      - type: "plaintext"
        name: "controller"
        containerPort: 29093
        usedForInnerBrokerCommunication: false
        usedForControllerCommunication: true
    externalListeners:
      - type: "plaintext"
        name: "external"
        externalStartingPort: 19090
        containerPort: 9094
  cruiseControlConfig:
    topicConfig:
      partitions: 12
      replicationFactor: 3
    config: |
      # Copyright 2017 LinkedIn Corp. Licensed under the BSD 2-Clause License (the "License"). See License in the project root for license information.
      #
      # This is an example property file for Kafka Cruise Control. See KafkaCruiseControlConfig for more details.
      # Configuration for the metadata client.
      # =======================================
      # The maximum interval in milliseconds between two metadata refreshes.
      #metadata.max.age.ms=300000
      # Client id for the Cruise Control. It is used for the metadata client.
      #client.id=kafka-cruise-control
      # The size of TCP send buffer bytes for the metadata client.
      #send.buffer.bytes=131072
      # The size of TCP receive buffer size for the metadata client.
      #receive.buffer.bytes=131072
      # The time to wait before disconnect an idle TCP connection.
      #connections.max.idle.ms=540000
      # The time to wait before reconnect to a given host.
      #reconnect.backoff.ms=50
      # The time to wait for a response from a host after sending a request.
      #request.timeout.ms=30000
      # Configurations for the load monitor
      # =======================================
      # The number of metric fetcher thread to fetch metrics for the Kafka cluster
      num.metric.fetchers=1
      # The metric sampler class
      metric.sampler.class=com.linkedin.kafka.cruisecontrol.monitor.sampling.CruiseControlMetricsReporterSampler
      # Configurations for CruiseControlMetricsReporterSampler
      metric.reporter.topic.pattern=__CruiseControlMetrics
      # The sample store class name
      sample.store.class=com.linkedin.kafka.cruisecontrol.monitor.sampling.KafkaSampleStore
      # The config for the Kafka sample store to save the partition metric samples
      partition.metric.sample.store.topic=__KafkaCruiseControlPartitionMetricSamples
      # The config for the Kafka sample store to save the model training samples
      broker.metric.sample.store.topic=__KafkaCruiseControlModelTrainingSamples
      # The replication factor of Kafka metric sample store topic
      sample.store.topic.replication.factor=2
      # The config for the number of Kafka sample store consumer threads
      num.sample.loading.threads=8
      # The partition assignor class for the metric samplers
      metric.sampler.partition.assignor.class=com.linkedin.kafka.cruisecontrol.monitor.sampling.DefaultMetricSamplerPartitionAssignor
      # The metric sampling interval in milliseconds
      metric.sampling.interval.ms=120000
      metric.anomaly.detection.interval.ms=180000
      # The partition metrics window size in milliseconds
      partition.metrics.window.ms=300000
      # The number of partition metric windows to keep in memory
      num.partition.metrics.windows=1
      # The minimum partition metric samples required for a partition in each window
      min.samples.per.partition.metrics.window=1
      # The broker metrics window size in milliseconds
      broker.metrics.window.ms=300000
      # The number of broker metric windows to keep in memory
      num.broker.metrics.windows=20
      # The minimum broker metric samples required for a partition in each window
      min.samples.per.broker.metrics.window=1
      # The configuration for the BrokerCapacityConfigFileResolver (supports JBOD and non-JBOD broker capacities)
      capacity.config.file=config/capacity.json
      #capacity.config.file=config/capacityJBOD.json
      # Configurations for the analyzer
      # =======================================
      # The list of goals to optimize the Kafka cluster for with pre-computed proposals
      default.goals=com.linkedin.kafka.cruisecontrol.analyzer.goals.ReplicaCapacityGoal,com.linkedin.kafka.cruisecontrol.analyzer.goals.DiskCapacityGoal,com.linkedin.kafka.cruisecontrol.analyzer.goals.NetworkInboundCapacityGoal,com.linkedin.kafka.cruisecontrol.analyzer.goals.NetworkOutboundCapacityGoal,com.linkedin.kafka.cruisecontrol.analyzer.goals.CpuCapacityGoal,com.linkedin.kafka.cruisecontrol.analyzer.goals.ReplicaDistributionGoal,com.linkedin.kafka.cruisecontrol.analyzer.goals.PotentialNwOutGoal,com.linkedin.kafka.cruisecontrol.analyzer.goals.DiskUsageDistributionGoal,com.linkedin.kafka.cruisecontrol.analyzer.goals.NetworkInboundUsageDistributionGoal,com.linkedin.kafka.cruisecontrol.analyzer.goals.NetworkOutboundUsageDistributionGoal,com.linkedin.kafka.cruisecontrol.analyzer.goals.CpuUsageDistributionGoal,com.linkedin.kafka.cruisecontrol.analyzer.goals.TopicReplicaDistributionGoal,com.linkedin.kafka.cruisecontrol.analyzer.goals.LeaderBytesInDistributionGoal
      # The list of supported goals
      goals=com.linkedin.kafka.cruisecontrol.analyzer.goals.ReplicaCapacityGoal,com.linkedin.kafka.cruisecontrol.analyzer.goals.DiskCapacityGoal,com.linkedin.kafka.cruisecontrol.analyzer.goals.NetworkInboundCapacityGoal,com.linkedin.kafka.cruisecontrol.analyzer.goals.NetworkOutboundCapacityGoal,com.linkedin.kafka.cruisecontrol.analyzer.goals.CpuCapacityGoal,com.linkedin.kafka.cruisecontrol.analyzer.goals.ReplicaDistributionGoal,com.linkedin.kafka.cruisecontrol.analyzer.goals.PotentialNwOutGoal,com.linkedin.kafka.cruisecontrol.analyzer.goals.DiskUsageDistributionGoal,com.linkedin.kafka.cruisecontrol.analyzer.goals.NetworkInboundUsageDistributionGoal,com.linkedin.kafka.cruisecontrol.analyzer.goals.NetworkOutboundUsageDistributionGoal,com.linkedin.kafka.cruisecontrol.analyzer.goals.CpuUsageDistributionGoal,com.linkedin.kafka.cruisecontrol.analyzer.goals.TopicReplicaDistributionGoal,com.linkedin.kafka.cruisecontrol.analyzer.goals.LeaderBytesInDistributionGoal,com.linkedin.kafka.cruisecontrol.analyzer.kafkaassigner.KafkaAssignerDiskUsageDistributionGoal,com.linkedin.kafka.cruisecontrol.analyzer.goals.PreferredLeaderElectionGoal
      # The list of supported hard goals
      hard.goals=com.linkedin.kafka.cruisecontrol.analyzer.goals.ReplicaCapacityGoal,com.linkedin.kafka.cruisecontrol.analyzer.goals.DiskCapacityGoal,com.linkedin.kafka.cruisecontrol.analyzer.goals.NetworkInboundCapacityGoal,com.linkedin.kafka.cruisecontrol.analyzer.goals.NetworkOutboundCapacityGoal,com.linkedin.kafka.cruisecontrol.analyzer.goals.CpuCapacityGoal
      # The minimum percentage of well monitored partitions out of all the partitions
      min.monitored.partition.percentage=0.95
      # The balance threshold for CPU
      cpu.balance.threshold=1.1
      # The balance threshold for disk
      disk.balance.threshold=1.1
      # The balance threshold for network inbound utilization
      network.inbound.balance.threshold=1.1
      # The balance threshold for network outbound utilization
      network.outbound.balance.threshold=1.1
      # The balance threshold for the replica count
      replica.count.balance.threshold=1.1
      # The capacity threshold for CPU in percentage
      cpu.capacity.threshold=0.8
      # The capacity threshold for disk in percentage
      disk.capacity.threshold=0.8
      # The capacity threshold for network inbound utilization in percentage
      network.inbound.capacity.threshold=0.8
      # The capacity threshold for network outbound utilization in percentage
      network.outbound.capacity.threshold=0.8
      # The threshold to define the cluster to be in a low CPU utilization state
      cpu.low.utilization.threshold=0.0
      # The threshold to define the cluster to be in a low disk utilization state
      disk.low.utilization.threshold=0.0
      # The threshold to define the cluster to be in a low network inbound utilization state
      network.inbound.low.utilization.threshold=0.0
      # The threshold to define the cluster to be in a low disk utilization state
      network.outbound.low.utilization.threshold=0.0
      # The metric anomaly percentile upper threshold
      metric.anomaly.percentile.upper.threshold=90.0
      # The metric anomaly percentile lower threshold
      metric.anomaly.percentile.lower.threshold=10.0
      # How often should the cached proposal be expired and recalculated if necessary
      proposal.expiration.ms=60000
      # The maximum number of replicas that can reside on a broker at any given time.
      max.replicas.per.broker=10000
      # The number of threads to use for proposal candidate precomputing.
      num.proposal.precompute.threads=1
      # the topics that should be excluded from the partition movement.
      #topics.excluded.from.partition.movement
      # Configurations for the executor
      # =======================================
      # The max number of partitions to move in/out on a given broker at a given time.
      num.concurrent.partition.movements.per.broker=10
      # The interval between two execution progress checks.
      execution.progress.check.interval.ms=10000
      # Configurations for anomaly detector
      # =======================================
      # The goal violation notifier class
      anomaly.notifier.class=com.linkedin.kafka.cruisecontrol.detector.notifier.SelfHealingNotifier
      # The metric anomaly finder class
      metric.anomaly.finder.class=com.linkedin.kafka.cruisecontrol.detector.KafkaMetricAnomalyFinder
      # The anomaly detection interval
      anomaly.detection.interval.ms=10000
      # The goal violation to detect.
      anomaly.detection.goals=com.linkedin.kafka.cruisecontrol.analyzer.goals.ReplicaCapacityGoal,com.linkedin.kafka.cruisecontrol.analyzer.goals.DiskCapacityGoal,com.linkedin.kafka.cruisecontrol.analyzer.goals.NetworkInboundCapacityGoal,com.linkedin.kafka.cruisecontrol.analyzer.goals.NetworkOutboundCapacityGoal,com.linkedin.kafka.cruisecontrol.analyzer.goals.CpuCapacityGoal
      # The interested metrics for metric anomaly analyzer.
      metric.anomaly.analyzer.metrics=BROKER_PRODUCE_LOCAL_TIME_MS_MAX,BROKER_PRODUCE_LOCAL_TIME_MS_MEAN,BROKER_CONSUMER_FETCH_LOCAL_TIME_MS_MAX,BROKER_CONSUMER_FETCH_LOCAL_TIME_MS_MEAN,BROKER_FOLLOWER_FETCH_LOCAL_TIME_MS_MAX,BROKER_FOLLOWER_FETCH_LOCAL_TIME_MS_MEAN,BROKER_LOG_FLUSH_TIME_MS_MAX,BROKER_LOG_FLUSH_TIME_MS_MEAN
      ## Adjust accordingly if your metrics reporter is an older version and does not produce these metrics.
      #metric.anomaly.analyzer.metrics=BROKER_PRODUCE_LOCAL_TIME_MS_50TH,BROKER_PRODUCE_LOCAL_TIME_MS_999TH,BROKER_CONSUMER_FETCH_LOCAL_TIME_MS_50TH,BROKER_CONSUMER_FETCH_LOCAL_TIME_MS_999TH,BROKER_FOLLOWER_FETCH_LOCAL_TIME_MS_50TH,BROKER_FOLLOWER_FETCH_LOCAL_TIME_MS_999TH,BROKER_LOG_FLUSH_TIME_MS_50TH,BROKER_LOG_FLUSH_TIME_MS_999TH
      # The zk path to store failed broker information.
      failed.brokers.zk.path=/CruiseControlBrokerList
      # Topic config provider class
      topic.config.provider.class=com.linkedin.kafka.cruisecontrol.config.KafkaTopicConfigProvider
      # The cluster configurations for the KafkaTopicConfigProvider
      cluster.configs.file=config/clusterConfigs.json
      # The maximum time in milliseconds to store the response and access details of a completed user task.
      completed.user.task.retention.time.ms=21600000
      # The maximum time in milliseconds to retain the demotion history of brokers.
      demotion.history.retention.time.ms=86400000
      # The maximum number of completed user tasks for which the response and access details will be cached.
      max.cached.completed.user.tasks=100
      # The maximum number of user tasks for concurrently running in async endpoints across all users.
      max.active.user.tasks=25
      # Enable self healing for all anomaly detectors, unless the particular anomaly detector is explicitly disabled
      self.healing.enabled=true
      # Enable self healing for broker failure detector
      #self.healing.broker.failure.enabled=true
      # Enable self healing for goal violation detector
      #self.healing.goal.violation.enabled=true
      # Enable self healing for metric anomaly detector
      #self.healing.metric.anomaly.enabled=true
      # configurations for the webserver
      # ================================
      # HTTP listen port
      webserver.http.port=9090
      # HTTP listen address
      webserver.http.address=0.0.0.0
      # Whether CORS support is enabled for API or not
      webserver.http.cors.enabled=false
      # Value for Access-Control-Allow-Origin
      webserver.http.cors.origin=http://localhost:8080/
      # Value for Access-Control-Request-Method
      webserver.http.cors.allowmethods=OPTIONS,GET,POST
      # Headers that should be exposed to the Browser (Webapp)
      # This is a special header that is used by the
      # User Tasks subsystem and should be explicitly
      # Enabled when CORS mode is used as part of the
      # Admin Interface
      webserver.http.cors.exposeheaders=User-Task-ID
      # REST API default prefix
      # (dont forget the ending *)
      webserver.api.urlprefix=/kafkacruisecontrol/*
      # Location where the Cruise Control frontend is deployed
      webserver.ui.diskpath=./cruise-control-ui/dist/
      # URL path prefix for UI
      # (dont forget the ending *)
      webserver.ui.urlprefix=/*
      # Time After which request is converted to Async
      webserver.request.maxBlockTimeMs=10000
      # Default Session Expiry Period
      webserver.session.maxExpiryTimeMs=60000
      # Session cookie path
      webserver.session.path=/
      # Server Access Logs
      webserver.accesslog.enabled=true
      # Location of HTTP Request Logs
      webserver.accesslog.path=access.log
      # HTTP Request Log retention days
      webserver.accesslog.retention.days=14
    clusterConfig: |
      {
        "min.insync.replicas": 3
      }
```

### 部署第二个zookeeper

zookeeperCluster-demo-2.yaml

```
apiVersion: zookeeper.pravega.io/v1beta1
kind: ZookeeperCluster
metadata:
  name: zookeeper-2
spec:
  replicas: 3
  image:
    repository: pravega/zookeeper
    tag: 0.2.13
  storageType: persistence
  persistence:
    reclaimPolicy: Retain
    spec:
      storageClassName: "nfs-client-provisioner"
      resources:
        requests:
          storage: 1Gi
```



### 部署成功后的pod

```
[root@node01 kafka_mesh]# kubectl get pod -n istio -w
NAME                                             READY   STATUS    RESTARTS   AGE
kafka-0-pvb2s                                    2/2     Running   0          79m
kafka-1-bwmbl                                    2/2     Running   0          78m
kafka-2-7t7s7                                    2/2     Running   0          78m
kafka-c2-0-4d84w                                 2/2     Running   0          37s
kafka-c2-1-hks7q                                 2/2     Running   0          31s
kafka-c2-2-xs2z7                                 2/2     Running   0          29s
kafka-c2-cruisecontrol-54cc56f5c8-2x2nq          2/2     Running   0          11s
kafka-consumer                                   2/2     Running   0          38m
kafka-cruisecontrol-6ccbcf6546-fp2vl             2/2     Running   3          15h
kafka-producer                                   2/2     Running   0          44m
meshgateway-external-kafka-7866c78c94-k97tc      1/1     Running   1          15h
meshgateway-external-kafka-c2-765899d5b8-62shg   1/1     Running   0          42s
```



### 测试

my-topic.yaml

```
apiVersion: kafka.banzaicloud.io/v1alpha1
kind: KafkaTopic
metadata:
  name: my-topic
spec:
  clusterRef:
    name: kafka
  name: my-topic
  partitions: 3
  replicationFactor: 2
  config:
    "retention.ms": "604800000"
    "cleanup.policy": "delete"
```

my-topic2.yaml

```
apiVersion: kafka.banzaicloud.io/v1alpha1
kind: KafkaTopic
metadata:
  name: my-topic-2
spec:
  clusterRef:
    name: kafka-c2
  name: my-topic2
  partitions: 3
  replicationFactor: 2
  config:
    "retention.ms": "604800000"
    "cleanup.policy": "delete"
```



```
kubectl -n istio run kafka-producer -it  --image=strimzi/kafka:0.12.2-kafka-2.2.1 --rm --restart=Never -- bin/kafka-console-producer.sh --broker-list kafka-all-broker.istio:29092 --topic my-topic

kubectl -n istio run kafka-consumer -it --image=strimzi/kafka:0.12.2-kafka-2.2.1 --rm  --restart=Never -- bin/kafka-console-consumer.sh --bootstrap-server kafka-all-broker.istio:29092 --topic my-topic --from-beginning

kubectl -n istio run kafka-producer2 -it  --image=strimzi/kafka:0.12.2-kafka-2.2.1 --rm --restart=Never -- bin/kafka-console-producer.sh --broker-list kafka-c2-all-broker.istio:29092 --topic my-topic2

kubectl -n istio run kafka-consumer2 -it --image=strimzi/kafka:0.12.2-kafka-2.2.1 --rm  --restart=Never -- bin/kafka-console-consumer.sh --bootstrap-server kafka-c2-all-broker.istio:29092 --topic my-topic2 --from-beginning

```

参考：

https://blog.csdn.net/qq_29116427/article/details/105912397



## 3.2kafka_mesh

### 3.2.1general

ef-kafka_mesh.yaml

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: kafka-mesh
  namespace: istio
spec:
  workloadSelector:
    labels:
      app: kafka-producer
  configPatches:
  - applyTo: LISTENER
    match:
      context: SIDECAR_OUTBOUND
    patch:
      operation: ADD
      value:
        name: kafka
        address:
          socket_address:
            protocol: TCP
            address: 127.0.0.1
            port_value: 29092
        filter_chains:
        - filters:
           -  name: envoy.filters.network.kafka_mesh
              typed_config:
                "@type": type.googleapis.com/envoy.extensions.filters.network.kafka_mesh.v3alpha.KafkaMesh
                advertised_host: "127.0.0.1"
                advertised_port: 29092
                upstream_clusters:
                - cluster_name: kafka-c1
                  bootstrap_servers: kafka-all-broker.istio:29092
                  partition_count: 3
                - cluster_name: kafka-c2
                  bootstrap_servers: kafka-c2-all-broker.istio:29092
                  partition_count: 3
                forwarding_rules:
                - target_cluster: kafka-c1
                  topic_prefix: my-topic
                - target_cluster: kafka-c2
                  topic_prefix: test
```



my-topic-test.yaml

```
apiVersion: kafka.banzaicloud.io/v1alpha1
kind: KafkaTopic
metadata:
  name: test
spec:
  clusterRef:
    name: kafka-c2
  name: test
  partitions: 3
  replicationFactor: 2
  config:
    "retention.ms": "604800000"
    "cleanup.policy": "delete"
```

测试客户端

```
kubectl -n istio run kafka-producer -it  --image=strimzi/kafka:0.12.2-kafka-2.2.1 --labels="app=kafka-producer" --rm --restart=Never -- bin/kafka-console-producer.sh --broker-list 127.0.0.1:29092 --topic my-topic

kubectl -n istio run kafka-consumer -it --image=strimzi/kafka:0.12.2-kafka-2.2.1 --rm  --restart=Never -- bin/kafka-console-consumer.sh --bootstrap-server kafka-all-broker.istio:29092 --topic my-topic --from-beginning


kubectl -n istio run kafka-producer2 -it  --image=strimzi/kafka:0.12.2-kafka-2.2.1 --rm --labels="app=kafka-producer"  --restart=Never -- bin/kafka-console-producer.sh --broker-list 127.0.0.1:29092 --topic test

kubectl -n istio run kafka-consumer2 -it --image=strimzi/kafka:0.12.2-kafka-2.2.1 --rm  --restart=Never -- bin/kafka-console-consumer.sh --bootstrap-server kafka-c2-all-broker.istio:29092 --topic test --from-beginning

```

https://github.com/istio/istio/issues/38194



### 3.2.2upstream_clusters

**partition_count**,**producer_config**

ef-kafka_mesh-upstream_clusters.yaml

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: kafka-mesh
  namespace: istio
spec:
  workloadSelector:
    labels:
      app: kafka-producer
  configPatches:
  - applyTo: LISTENER
    match:
      context: SIDECAR_OUTBOUND
    patch:
      operation: ADD
      value:
        name: kafka
        address:
          socket_address:
            protocol: TCP
            address: 127.0.0.1
            port_value: 29092
        filter_chains:
        - filters:
           -  name: envoy.filters.network.kafka_mesh
              typed_config:
                "@type": type.googleapis.com/envoy.extensions.filters.network.kafka_mesh.v3alpha.KafkaMesh
                advertised_host: "127.0.0.1"
                advertised_port: 29092
                upstream_clusters:
                - cluster_name: kafka-c1
                  bootstrap_servers: kafka-all-broker.istio:29092
                  partition_count: 3
                  producer_config:
                    acks: "1"
                    linger.ms: "500"
                - cluster_name: kafka-c2
                  bootstrap_servers: kafka-c2-all-broker.istio:29092
                  partition_count: 3
                  producer_config:
                    acks: "1"
                    linger.ms: "500"
                forwarding_rules:
                - target_cluster: kafka-c1
                  topic_prefix: my-topic
                - target_cluster: kafka-c2
                  topic_prefix: test
```

测试客户端

```
kubectl -n istio run kafka-producer -it  --image=strimzi/kafka:0.12.2-kafka-2.2.1 --labels="app=kafka-producer" --rm --restart=Never -- bin/kafka-console-producer.sh --broker-list 127.0.0.1:29092 --topic my-topic

kubectl -n istio run kafka-consumer -it --image=strimzi/kafka:0.12.2-kafka-2.2.1 --rm  --restart=Never -- bin/kafka-console-consumer.sh --bootstrap-server kafka-all-broker.istio:29092 --topic my-topic --from-beginning


kubectl -n istio run kafka-producer2 -it  --image=strimzi/kafka:0.12.2-kafka-2.2.1 --rm --labels="app=kafka-producer"  --restart=Never -- bin/kafka-console-producer.sh --broker-list 127.0.0.1:29092 --topic test

kubectl -n istio run kafka-consumer2 -it --image=strimzi/kafka:0.12.2-kafka-2.2.1 --rm  --restart=Never -- bin/kafka-console-consumer.sh --bootstrap-server kafka-c2-all-broker.istio:29092 --topic test --from-beginning

```

