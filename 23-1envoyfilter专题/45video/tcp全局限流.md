部署mysql

```
cat << EOF > mysql.yaml
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
EOF

kubectl apply -f mysql.yaml -n istio
```

部署mysql版ratings

```
cat << EOF > ratings-mysql.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: ratings-v2-mysql
  labels:
    app: ratings
    version: v2-mysql
spec:
  replicas: 1
  selector:
    matchLabels:
      app: ratings
      version: v2-mysql
  template:
    metadata:
      labels:
        app: ratings
        version: v2-mysql
    spec:
      containers:
      - name: ratings
        image: docker.io/istio/examples-bookinfo-ratings-v2:1.16.2
        imagePullPolicy: IfNotPresent
        env:
          # ratings-v2 will use mongodb as the default db backend.
          # if you would like to use mysqldb then you can use this file
          # which sets DB_TYPE = 'mysql' and the rest of the parameters shown
          # here and also create the # mysqldb service using bookinfo-mysql.yaml
          # NOTE: This file is mutually exclusive to bookinfo-ratings-v2.yaml
          - name: DB_TYPE
            value: "mysql"
          - name: MYSQL_DB_HOST
            value: mysqldb
          - name: MYSQL_DB_PORT
            value: "3306"
          - name: MYSQL_DB_USER
            value: root
          - name: MYSQL_DB_PASSWORD
            value: password
        ports:
        - containerPort: 9080
        securityContext:
          runAsUser: 1000
EOF

kubectl apply -f ratings-mysql.yaml -n istio
```



部署ratelimit

1创建cm

```
cat << EOF > ratelimit-config-mysql-inside.yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: ratelimit-config
data:
  config.yaml: |
    domain: mysql-ratelimit
    descriptors:
      - key: PATH
        value: "/productpage"
        rate_limit:
          unit: minute
          requests_per_unit: 1
      - key: PATH
        rate_limit:
          unit: minute
          requests_per_unit: 100
EOF

kubectl apply -f ratelimit-config-mysql-inside.yaml -n istio
```

说明: 这个configmap是限速服务用到的配置文件，他是envoy v3版本的限速格式。domain是域名，他会在envoyfilter中被引用，descriptors的PATH,表示请求的路径可以有多个值，rate_limit配置限速配额，这里productpage配了1分钟1个请求，其他url是1分钟100个请求



2创建限速服务deployment

```
cat << EOF > ratelimit-deploy.yaml
apiVersion: v1
kind: Service
metadata:
  name: redis
  labels:
    app: redis
spec:
  ports:
  - name: redis
    port: 6379
  selector:
    app: redis
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: redis
spec:
  replicas: 1
  selector:
    matchLabels:
      app: redis
  template:
    metadata:
      labels:
        app: redis
    spec:
      containers:
      - image: redis:alpine
        imagePullPolicy: Always
        name: redis
        ports:
        - name: redis
          containerPort: 6379
      restartPolicy: Always
      serviceAccountName: ""
---
apiVersion: v1
kind: Service
metadata:
  name: ratelimit
  labels:
    app: ratelimit
spec:
  ports:
  - name: http-port
    port: 8080
    targetPort: 8080
    protocol: TCP
  - name: grpc-port
    port: 8081
    targetPort: 8081
    protocol: TCP
  - name: http-debug
    port: 6070
    targetPort: 6070
    protocol: TCP
  selector:
    app: ratelimit
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: ratelimit
spec:
  replicas: 1
  selector:
    matchLabels:
      app: ratelimit
  strategy:
    type: Recreate
  template:
    metadata:
      labels:
        app: ratelimit
    spec:
      containers:
      - image: envoyproxy/ratelimit:6f5de117 # 2021/01/08
        imagePullPolicy: Always
        name: ratelimit
        command: ["/bin/ratelimit"]
        env:
        - name: LOG_LEVEL
          value: debug
        - name: REDIS_SOCKET_TYPE
          value: tcp
        - name: REDIS_URL
          value: redis:6379
        - name: USE_STATSD
          value: "false"
        - name: RUNTIME_ROOT
          value: /data
        - name: RUNTIME_SUBDIRECTORY
          value: ratelimit
        ports:
        - containerPort: 8080
        - containerPort: 8081
        - containerPort: 6070
        volumeMounts:
        - name: config-volume
          mountPath: /data/ratelimit/config/config.yaml
          subPath: config.yaml
      volumes:
      - name: config-volume
        configMap:
          name: ratelimit-config
EOF

kubectl apply -f ratelimit-deploy.yaml -n istio
```

创建了redis，和官方的一个ratelimit服务。



3创建envoy-filter

```
cat << EOF > envoyfilter-filter-mysql-inside.yaml
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: filter-ratelimit
  namespace: istio
spec:
  workloadSelector:
    labels:
      app: mysqldb
      version: v1
  configPatches:
    - applyTo: NETWORK_FILTER
      match:
        listener:
          portNumber: 3306
          filterChain:
            filter:
              name: "envoy.filters.network.tcp_proxy"
      patch:
        operation: INSERT_BEFORE
        value:
          name: envoy.filters.network.ratelimit
          typed_config:
            "@type": type.googleapis.com/envoy.extensions.filters.network.ratelimit.v3.RateLimit
            domain: mysql-ratelimit
            failure_mode_deny: true
            descriptors:
            -
            rate_limit_service:
              grpc_service:
                envoy_grpc:
                  cluster_name: rate_limit_cluster
                timeout: 10s
              transport_api_version: V3
    - applyTo: CLUSTER
      match:
        cluster:
          service: ratelimit.istio.svc.cluster.local
      patch:
        operation: ADD
        value:
          name: rate_limit_cluster
          type: STRICT_DNS
          connect_timeout: 10s
          lb_policy: ROUND_ROBIN
          http2_protocol_options: {}
          load_assignment:
            cluster_name: rate_limit_cluster
            endpoints:
            - lb_endpoints:
              - endpoint:
                  address:
                     socket_address:
                      address: ratelimit.istio.svc.cluster.local
                      port_value: 8081
EOF

kubectl apply -f envoyfilter-filter-mysql-inside.yaml -n istio
```

这个envoyfilter作用在网关上面，配置了一个http过滤器envoy.filters.http.ratelimit，和一个cluster。http 过滤器的cluster地址指向cluster配置的地址，这里就是我们的ratelimit service所在的地址。domain是上面configmap的值一样，failure_mode_deny表示超过请求限值就拒绝，rate_limit_service配置ratelimit服务的地址（cluster），这里可以配置grpc类型的也可以配置http类型的。







清理：

```
kubectl delete -f mysql.yaml -n istio
kubectl delete -f ratings-mysql.yaml -n istio
kubectl delete -f envoyfilter-filter-mysql-inside.yaml -n istio
kubectl delete -f ratelimit-deploy.yaml -n istio
kubectl delete -f ratelimit-config-mysql-inside.yaml -n istio
kubectl delete envoyfilter filter-ratelimit-svc -n istio
```

