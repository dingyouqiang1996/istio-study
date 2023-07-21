



部署mysql

yum install mariadb-server

```
CREATE DATABASE test;
USE test;

CREATE TABLE `ratings` (
  `ReviewID` INT NOT NULL,
  `Rating` INT,
  PRIMARY KEY (`ReviewID`)
);
INSERT INTO ratings (ReviewID, Rating) VALUES (1, 5);
INSERT INTO ratings (ReviewID, Rating) VALUES (2, 4);
```

授权：

 GRANT ALL PRIVILEGES ON *.* TO 'root'@'%' IDENTIFIED BY 'root' WITH GRANT OPTION;  

 FLUSH PRIVILEGES;  



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

 kubectl patch svc  -n istio-system istio-eastwestgateway -p '{"spec":{"externalIPs":["192.168.229.10"]}}'

 kubectl apply -n istio-system -f /root/istio-1.11.2/samples/multicluster/expose-services.yaml



3创建资源

 kubectl create namespace vm

 kubectl create serviceaccount  sa-mysql -n vm

  kubectl --namespace vm apply -f wlg-mysql.yaml

 workloadgroups/singlenetwork/wlg-mysql.yaml

```
apiVersion: networking.istio.io/v1alpha3
kind: WorkloadGroup
metadata:
  name: httpd
  namespace: vm
spec:
  metadata:
    labels:
      app: mysql
      type: vm
  template:
    serviceAccount: sa-mysql
    network: network2
```

4生成配置文件

istioctl x workload entry configure -f wlg-mysql.yaml  -o . --clusterID cluster1 --autoregister

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

kubectl delete workloadgroup httpd -n vm