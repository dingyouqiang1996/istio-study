# Brotli

响应压缩

两个场景：

1压缩文件

2压缩envoy统计信息

```
brotli-envoy.yaml  压缩envoy配置

static_resources:
  listeners:
  - address:
      socket_address:
        address: 0.0.0.0
        port_value: 10000
    filter_chains:
    - filters:
      - name: envoy.filters.network.http_connection_manager
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager
          stat_prefix: ingress_http
          route_config:
            name: local_route
            virtual_hosts:
            - name: backend
              domains:
              - "*"
              routes:
              - match:
                  prefix: "/"
                route:
                  cluster: service
          http_filters:
          - name: envoy.filters.http.compressor
            typed_config:
              "@type": type.googleapis.com/envoy.extensions.filters.http.compressor.v3.Compressor
              response_direction_config:
                common_config:
                  min_content_length: 100
                  content_type:
                    - application/json
                disable_on_etag_header: true
              compressor_library:
                name: text_optimized
                typed_config:
                  "@type": type.googleapis.com/envoy.extensions.compression.brotli.compressor.v3.Brotli
                  window_bits: 10
          - name: envoy.filters.http.router
      transport_socket:
        name: envoy.transport_sockets.tls
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.transport_sockets.tls.v3.DownstreamTlsContext
          common_tls_context:
            tls_certificates:
            # The following self-signed certificate pair is generated using:
            # $ openssl req -x509 -newkey rsa:2048 -keyout a/brotli-key.pem -out  a/brotli-crt.pem -days 3650 -nodes -subj '/CN=brotli'
            #
            # Instead of feeding it as an inline_string, certificate pair can also be fed to Envoy
            # via filename. Reference: https://www.envoyproxy.io/docs/envoy/latest/api-v3/config/core/v3/base.proto#config-core-v3-datasource.
            #
            # Or in a dynamic configuration scenario, certificate pair can be fetched remotely via
            # Secret Discovery Service (SDS). Reference: https://www.envoyproxy.io/docs/envoy/latest/configuration/security/secret.
            - certificate_chain:
                inline_string: |
                  -----BEGIN CERTIFICATE-----
                  MIICqDCCAZACCQCquzpHNpqBcDANBgkqhkiG9w0BAQsFADAWMRQwEgYDVQQDDAtm
                  cm9udC1lbnZveTAeFw0yMDA3MDgwMTMxNDZaFw0zMDA3MDYwMTMxNDZaMBYxFDAS
                  BgNVBAMMC2Zyb250LWVudm95MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKC
                  AQEAthnYkqVQBX+Wg7aQWyCCb87hBce1hAFhbRM8Y9dQTqxoMXZiA2n8G089hUou
                  oQpEdJgitXVS6YMFPFUUWfwcqxYAynLK4X5im26Yfa1eO8La8sZUS+4Bjao1gF5/
                  VJxSEo2yZ7fFBo8M4E44ZehIIocipCRS+YZehFs6dmHoq/MGvh2eAHIa+O9xssPt
                  ofFcQMR8rwBHVbKy484O10tNCouX4yUkyQXqCRy6HRu7kSjOjNKSGtjfG+h5M8bh
                  10W7ZrsJ1hWhzBulSaMZaUY3vh5ngpws1JATQVSK1Jm/dmMRciwlTK7KfzgxHlSX
                  58ENpS7yPTISkEICcLbXkkKGEQIDAQABMA0GCSqGSIb3DQEBCwUAA4IBAQCmj6Hg
                  vwOxWz0xu+6fSfRL6PGJUGq6wghCfUvjfwZ7zppDUqU47fk+yqPIOzuGZMdAqi7N
                  v1DXkeO4A3hnMD22Rlqt25vfogAaZVToBeQxCPd/ALBLFrvLUFYuSlS3zXSBpQqQ
                  Ny2IKFYsMllz5RSROONHBjaJOn5OwqenJ91MPmTAG7ujXKN6INSBM0PjX9Jy4Xb9
                  zT+I85jRDQHnTFce1WICBDCYidTIvJtdSSokGSuy4/xyxAAc/BpZAfOjBQ4G1QRe
                  9XwOi790LyNUYFJVyeOvNJwveloWuPLHb9idmY5YABwikUY6QNcXwyHTbRCkPB2I
                  m+/R4XnmL4cKQ+5Z
                  -----END CERTIFICATE-----
              private_key:
                inline_string: |
                  -----BEGIN PRIVATE KEY-----
                  MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQC2GdiSpVAFf5aD
                  tpBbIIJvzuEFx7WEAWFtEzxj11BOrGgxdmIDafwbTz2FSi6hCkR0mCK1dVLpgwU8
                  VRRZ/ByrFgDKcsrhfmKbbph9rV47wtryxlRL7gGNqjWAXn9UnFISjbJnt8UGjwzg
                  Tjhl6EgihyKkJFL5hl6EWzp2Yeir8wa+HZ4Achr473Gyw+2h8VxAxHyvAEdVsrLj
                  zg7XS00Ki5fjJSTJBeoJHLodG7uRKM6M0pIa2N8b6HkzxuHXRbtmuwnWFaHMG6VJ
                  oxlpRje+HmeCnCzUkBNBVIrUmb92YxFyLCVMrsp/ODEeVJfnwQ2lLvI9MhKQQgJw
                  tteSQoYRAgMBAAECggEAeDGdEkYNCGQLe8pvg8Z0ccoSGpeTxpqGrNEKhjfi6NrB
                  NwyVav10iq4FxEmPd3nobzDPkAftfvWc6hKaCT7vyTkPspCMOsQJ39/ixOk+jqFx
                  lNa1YxyoZ9IV2DIHR1iaj2Z5gB367PZUoGTgstrbafbaNY9IOSyojCIO935ubbcx
                  DWwL24XAf51ez6sXnI8V5tXmrFlNXhbhJdH8iIxNyM45HrnlUlOk0lCK4gmLJjy9
                  10IS2H2Wh3M5zsTpihH1JvM56oAH1ahrhMXs/rVFXXkg50yD1KV+HQiEbglYKUxO
                  eMYtfaY9i2CuLwhDnWp3oxP3HfgQQhD09OEN3e0IlQKBgQDZ/3poG9TiMZSjfKqL
                  xnCABMXGVQsfFWNC8THoW6RRx5Rqi8q08yJrmhCu32YKvccsOljDQJQQJdQO1g09
                  e/adJmCnTrqxNtjPkX9txV23Lp6Ak7emjiQ5ICu7iWxrcO3zf7hmKtj7z+av8sjO
                  mDI7NkX5vnlE74nztBEjp3eC0wKBgQDV2GeJV028RW3b/QyP3Gwmax2+cKLR9PKR
                  nJnmO5bxAT0nQ3xuJEAqMIss/Rfb/macWc2N/6CWJCRT6a2vgy6xBW+bqG6RdQMB
                  xEZXFZl+sSKhXPkc5Wjb4lQ14YWyRPrTjMlwez3k4UolIJhJmwl+D7OkMRrOUERO
                  EtUvc7odCwKBgBi+nhdZKWXveM7B5N3uzXBKmmRz3MpPdC/yDtcwJ8u8msUpTv4R
                  JxQNrd0bsIqBli0YBmFLYEMg+BwjAee7vXeDFq+HCTv6XMva2RsNryCO4yD3I359
                  XfE6DJzB8ZOUgv4Dvluie3TB2Y6ZQV/p+LGt7G13yG4hvofyJYvlg3RPAoGAcjDg
                  +OH5zLN2eqah8qBN0CYa9/rFt0AJ19+7/smLTJ7QvQq4g0gwS1couplcCEnNGWiK
                  72y1n/ckvvplmPeAE19HveMvR9UoCeV5ej86fACy8V/oVpnaaLBvL2aCMjPLjPP9
                  DWeCIZp8MV86cvOrGfngf6kJG2qZTueXl4NAuwkCgYEArKkhlZVXjwBoVvtHYmN2
                  o+F6cGMlRJTLhNc391WApsgDZfTZSdeJsBsvvzS/Nc0burrufJg0wYioTlpReSy4
                  ohhtprnQQAddfjHP7rh2LGt+irFzhdXXQ1ybGaGM9D764KUNCXLuwdly0vzXU4HU
                  q5sGxGrC1RECGB5Zwx2S2ZY=
                  -----END PRIVATE KEY-----
  - address:
      socket_address:
        address: 0.0.0.0
        port_value: 9902
    filter_chains:
    - filters:
      - name: envoy.filters.network.http_connection_manager
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager
          stat_prefix: ingress_http
          route_config:
            name: local_route
            virtual_hosts:
            - name: backend
              domains:
              - "*"
              routes:
              - match:
                  prefix: "/stats/prometheus"
                route:
                  cluster: envoy-stats
          http_filters:
          - name: envoy.filters.http.compressor
            typed_config:
              "@type": type.googleapis.com/envoy.extensions.filters.http.compressor.v3.Compressor
              response_direction_config:
                common_config:
                  min_content_length: 100
                  content_type:
                    - text/plain
                disable_on_etag_header: true
              compressor_library:
                name: text_optimized
                typed_config:
                  "@type": type.googleapis.com/envoy.extensions.compression.brotli.compressor.v3.Brotli
                  window_bits: 10
          - name: envoy.filters.http.router
      transport_socket:
        name: envoy.transport_sockets.tls
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.transport_sockets.tls.v3.DownstreamTlsContext
          common_tls_context:
            tls_certificates:
            # The following self-signed certificate pair is generated using:
            # $ openssl req -x509 -newkey rsa:2048 -keyout a/brotli.pem -out  a/brotli-crt.pem -days 3650 -nodes -subj '/CN=brotli'
            #
            # Instead of feeding it as an inline_string, certificate pair can also be fed to Envoy
            # via filename. Reference: https://www.envoyproxy.io/docs/envoy/latest/api-v3/config/core/v3/base.proto#config-core-v3-datasource.
            #
            # Or in a dynamic configuration scenario, certificate pair can be fetched remotely via
            # Secret Discovery Service (SDS). Reference: https://www.envoyproxy.io/docs/envoy/latest/configuration/security/secret.
            - certificate_chain:
                inline_string: |
                  -----BEGIN CERTIFICATE-----
                  MIICqDCCAZACCQCquzpHNpqBcDANBgkqhkiG9w0BAQsFADAWMRQwEgYDVQQDDAtm
                  cm9udC1lbnZveTAeFw0yMDA3MDgwMTMxNDZaFw0zMDA3MDYwMTMxNDZaMBYxFDAS
                  BgNVBAMMC2Zyb250LWVudm95MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKC
                  AQEAthnYkqVQBX+Wg7aQWyCCb87hBce1hAFhbRM8Y9dQTqxoMXZiA2n8G089hUou
                  oQpEdJgitXVS6YMFPFUUWfwcqxYAynLK4X5im26Yfa1eO8La8sZUS+4Bjao1gF5/
                  VJxSEo2yZ7fFBo8M4E44ZehIIocipCRS+YZehFs6dmHoq/MGvh2eAHIa+O9xssPt
                  ofFcQMR8rwBHVbKy484O10tNCouX4yUkyQXqCRy6HRu7kSjOjNKSGtjfG+h5M8bh
                  10W7ZrsJ1hWhzBulSaMZaUY3vh5ngpws1JATQVSK1Jm/dmMRciwlTK7KfzgxHlSX
                  58ENpS7yPTISkEICcLbXkkKGEQIDAQABMA0GCSqGSIb3DQEBCwUAA4IBAQCmj6Hg
                  vwOxWz0xu+6fSfRL6PGJUGq6wghCfUvjfwZ7zppDUqU47fk+yqPIOzuGZMdAqi7N
                  v1DXkeO4A3hnMD22Rlqt25vfogAaZVToBeQxCPd/ALBLFrvLUFYuSlS3zXSBpQqQ
                  Ny2IKFYsMllz5RSROONHBjaJOn5OwqenJ91MPmTAG7ujXKN6INSBM0PjX9Jy4Xb9
                  zT+I85jRDQHnTFce1WICBDCYidTIvJtdSSokGSuy4/xyxAAc/BpZAfOjBQ4G1QRe
                  9XwOi790LyNUYFJVyeOvNJwveloWuPLHb9idmY5YABwikUY6QNcXwyHTbRCkPB2I
                  m+/R4XnmL4cKQ+5Z
                  -----END CERTIFICATE-----
              private_key:
                inline_string: |
                  -----BEGIN PRIVATE KEY-----
                  MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQC2GdiSpVAFf5aD
                  tpBbIIJvzuEFx7WEAWFtEzxj11BOrGgxdmIDafwbTz2FSi6hCkR0mCK1dVLpgwU8
                  VRRZ/ByrFgDKcsrhfmKbbph9rV47wtryxlRL7gGNqjWAXn9UnFISjbJnt8UGjwzg
                  Tjhl6EgihyKkJFL5hl6EWzp2Yeir8wa+HZ4Achr473Gyw+2h8VxAxHyvAEdVsrLj
                  zg7XS00Ki5fjJSTJBeoJHLodG7uRKM6M0pIa2N8b6HkzxuHXRbtmuwnWFaHMG6VJ
                  oxlpRje+HmeCnCzUkBNBVIrUmb92YxFyLCVMrsp/ODEeVJfnwQ2lLvI9MhKQQgJw
                  tteSQoYRAgMBAAECggEAeDGdEkYNCGQLe8pvg8Z0ccoSGpeTxpqGrNEKhjfi6NrB
                  NwyVav10iq4FxEmPd3nobzDPkAftfvWc6hKaCT7vyTkPspCMOsQJ39/ixOk+jqFx
                  lNa1YxyoZ9IV2DIHR1iaj2Z5gB367PZUoGTgstrbafbaNY9IOSyojCIO935ubbcx
                  DWwL24XAf51ez6sXnI8V5tXmrFlNXhbhJdH8iIxNyM45HrnlUlOk0lCK4gmLJjy9
                  10IS2H2Wh3M5zsTpihH1JvM56oAH1ahrhMXs/rVFXXkg50yD1KV+HQiEbglYKUxO
                  eMYtfaY9i2CuLwhDnWp3oxP3HfgQQhD09OEN3e0IlQKBgQDZ/3poG9TiMZSjfKqL
                  xnCABMXGVQsfFWNC8THoW6RRx5Rqi8q08yJrmhCu32YKvccsOljDQJQQJdQO1g09
                  e/adJmCnTrqxNtjPkX9txV23Lp6Ak7emjiQ5ICu7iWxrcO3zf7hmKtj7z+av8sjO
                  mDI7NkX5vnlE74nztBEjp3eC0wKBgQDV2GeJV028RW3b/QyP3Gwmax2+cKLR9PKR
                  nJnmO5bxAT0nQ3xuJEAqMIss/Rfb/macWc2N/6CWJCRT6a2vgy6xBW+bqG6RdQMB
                  xEZXFZl+sSKhXPkc5Wjb4lQ14YWyRPrTjMlwez3k4UolIJhJmwl+D7OkMRrOUERO
                  EtUvc7odCwKBgBi+nhdZKWXveM7B5N3uzXBKmmRz3MpPdC/yDtcwJ8u8msUpTv4R
                  JxQNrd0bsIqBli0YBmFLYEMg+BwjAee7vXeDFq+HCTv6XMva2RsNryCO4yD3I359
                  XfE6DJzB8ZOUgv4Dvluie3TB2Y6ZQV/p+LGt7G13yG4hvofyJYvlg3RPAoGAcjDg
                  +OH5zLN2eqah8qBN0CYa9/rFt0AJ19+7/smLTJ7QvQq4g0gwS1couplcCEnNGWiK
                  72y1n/ckvvplmPeAE19HveMvR9UoCeV5ej86fACy8V/oVpnaaLBvL2aCMjPLjPP9
                  DWeCIZp8MV86cvOrGfngf6kJG2qZTueXl4NAuwkCgYEArKkhlZVXjwBoVvtHYmN2
                  o+F6cGMlRJTLhNc391WApsgDZfTZSdeJsBsvvzS/Nc0burrufJg0wYioTlpReSy4
                  ohhtprnQQAddfjHP7rh2LGt+irFzhdXXQ1ybGaGM9D764KUNCXLuwdly0vzXU4HU
                  q5sGxGrC1RECGB5Zwx2S2ZY=
                  -----END PRIVATE KEY-----

  clusters:
  - name: envoy-stats
    connect_timeout: 0.25s
    type: STATIC
    load_assignment:
      cluster_name: envoy-stats
      endpoints:
      - lb_endpoints:
        - endpoint:
            address:
              socket_address:
                address: 127.0.0.1
                port_value: 9901
  - name: service
    connect_timeout: 0.25s
    type: STRICT_DNS
    lb_policy: ROUND_ROBIN
    load_assignment:
      cluster_name: service
      endpoints:
      - lb_endpoints:
        - endpoint:
            address:
              socket_address:
                address: service
                port_value: 8080
admin:
  address:
    socket_address:
      address: 0.0.0.0
      port_value: 9901
      
      
docker-compose.yaml  docker-compose配置文件
version: "3.3"
services:
  envoy-stats:
    build:
      context: .
      dockerfile: Dockerfile-brotli
    ports:
      - "9901:9901"
      - "9902:9902"
      - "10000:10000"

  service:
    build:
      context: .
      dockerfile: Dockerfile-service
      
Dockerfile-brotli 压缩Dockerfile配置文件
FROM envoyproxy/envoy-dev:latest

COPY ./brotli-envoy.yaml /etc/brotli-envoy.yaml
RUN chmod go+r /etc/brotli-envoy.yaml
CMD ["/usr/local/bin/envoy", "-c", "/etc/brotli-envoy.yaml", "--service-cluster", "brotli"]

Dockerfile-service  service服务Dockerfile配置文件
FROM debian:buster-slim

RUN apt-get update \
    && apt-get install --no-install-recommends -y python3 python3-pip \
    && apt-get autoremove -y && apt-get clean \
    && rm -rf /tmp/* /var/tmp/* \
    && rm -rf /var/lib/apt/lists/*
RUN pip3 install -q flask
RUN mkdir -p /code/data
RUN dd if=/dev/zero of="/code/data/file.txt" bs=1024 count=10240 \
    && dd if=/dev/zero of="/code/data/file.json" bs=1024 count=10240
ADD ./service.py /code
ENTRYPOINT ["python3", "/code/service.py"]

README.md  

service.py  service服务python程序
from flask import Flask
from flask.helpers import send_from_directory

app = Flask(__name__)


@app.route('/file.txt')
def get_plain_file():
    return send_from_directory("data", "file.txt")


@app.route('/file.json')
def get_json_file():
    return send_from_directory("data", "file.json")


if __name__ == "__main__":
    app.run(host='0.0.0.0', port=8080, debug=True)
    
verify.sh 校验脚本

第一步
docker-compose build --pull
docker-compose up -d
docker-compose ps

第二部
curl -ski -H "Accept-Encoding: br" https://localhost:10000/file.json | grep "content-encoding"

curl -ski -H "Accept-Encoding: br" https://localhost:10000/file.txt | grep "content-encoding"

第三步
curl -ski -H "Accept-Encoding: br" http://localhost:9901/stats/prometheus | grep "content-encoding"
curl -ski -H "Accept-Encoding: br" https://localhost:9902/stats/prometheus | grep "content-encoding"
```



# Cache filter

 HTTP 缓存过滤器

```
docker-compose.yaml docker-compose配置文件
version: "3.3"
services:

  front-envoy:
    build:
      context: .
      dockerfile: Dockerfile-frontenvoy
    volumes:
    - /etc/localtime:/etc/localtime:ro
    ports:
    - "8000:8000"

  service1:
    build:
      context: .
      dockerfile: Dockerfile-service
    volumes:
    - ./responses.yaml:/etc/responses.yaml
    - /etc/localtime:/etc/localtime:ro
    environment:
    - SERVICE_NAME=1

  service2:
    build:
      context: .
      dockerfile: Dockerfile-service
    volumes:
    - /etc/localtime:/etc/localtime:ro
    - ./responses.yaml:/etc/responses.yaml
    environment:
    - SERVICE_NAME=2
```

```
Dockerfile-frontenvoy 前端envoy Dockerfile配置文件
FROM envoyproxy/envoy-dev:latest

COPY ./front-envoy.yaml /etc/front-envoy.yaml
RUN chmod go+r /etc/front-envoy.yaml
CMD /usr/local/bin/envoy -c /etc/front-envoy.yaml --service-cluster front-proxy
```

```
Dockerfile-service  service服务Dockerfile配置
FROM alpine:latest

RUN apk update && apk add py3-pip
RUN pip3 install -q Flask==0.11.1 pyyaml
RUN mkdir /code
COPY ./service.py /code
CMD ["python3", "/code/service.py"]
```

```
front-envoy.yaml 前端envoy配置
static_resources:
  listeners:
  - address:
      socket_address:
        address: 0.0.0.0
        port_value: 8000
    filter_chains:
    - filters:
      - name: envoy.filters.network.http_connection_manager
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager
          codec_type: AUTO
          stat_prefix: ingress_http
          route_config:
            name: local_route
            virtual_hosts:
            - name: backend
              domains:
              - "*"
              routes:
              - match:
                  prefix: "/service/1"
                route:
                  cluster: service1
              - match:
                  prefix: "/service/2"
                route:
                  cluster: service2
          http_filters:
          - name: "envoy.filters.http.cache"
            typed_config:
              "@type": "type.googleapis.com/envoy.extensions.filters.http.cache.v3alpha.CacheConfig"
              typed_config:
                "@type": "type.googleapis.com/envoy.extensions.cache.simple_http_cache.v3alpha.SimpleHttpCacheConfig"
          - name: envoy.filters.http.router

  clusters:
  - name: service1
    type: STRICT_DNS
    lb_policy: ROUND_ROBIN
    load_assignment:
      cluster_name: service1
      endpoints:
      - lb_endpoints:
        - endpoint:
            address:
              socket_address:
                address: service1
                port_value: 8000
  - name: service2
    type: STRICT_DNS
    lb_policy: ROUND_ROBIN
    load_assignment:
      cluster_name: service2
      endpoints:
      - lb_endpoints:
        - endpoint:
            address:
              socket_address:
                address: service2
                port_value: 8000
```

```
responses.yaml 响应缓存配置
valid-for-minute:
  body: This response will stay fresh for one minute
  headers:
    cache-control: max-age=60
    custom-header: any value
private:
  body: This is a private response, it will not be cached by Envoy
  headers:
    cache-control: private
no-cache:
  body: This response can be cached, but it has to be validated on each request
  headers:
    cache-control: max-age=0, no-cache
```

```
service.py python程序
from flask import Flask
from flask import request
from flask import make_response, abort
import yaml
import os
import datetime

app = Flask(__name__)


@app.route('/service/<service_number>/<response_id>')
def get(service_number, response_id):
    stored_response = yaml.safe_load(open('/etc/responses.yaml', 'r')).get(response_id)

    if stored_response is None:
        abort(404, 'No response found with the given id')

    response = make_response(stored_response.get('body') + '\n')
    if stored_response.get('headers'):
        response.headers.update(stored_response.get('headers'))

    # Generate etag header
    response.add_etag()

    # Append the date of response generation
    body_with_date = "{}\nResponse generated at: {}\n".format(
        response.get_data(as_text=True),
        datetime.datetime.utcnow().strftime("%a, %d %b %Y %H:%M:%S GMT"))

    response.set_data(body_with_date)

    # response.make_conditional() will change the response to a 304 response
    # if a 'if-none-match' header exists in the request and matches the etag
    return response.make_conditional(request)


if __name__ == "__main__":
    if not os.path.isfile('/etc/responses.yaml'):
        print('Responses file not found at /etc/responses.yaml')
        exit(1)
    app.run(host='0.0.0.0', port=8000, debug=True)
```

```
verify.sh校验
```

```
第一步
docker-compose build --pull
docker-compose up -d
docker-compose ps
第二步
curl -i localhost:8000/service/<service_no>/<response>
curl -i localhost:8000/service/1/valid-for-minute
curl -i localhost:8000/service/1/private
curl -i localhost:8000/service/1/no-cache

```



# CORS filter

跨站资源共享过滤器，跨域

```
frontend/docker-compose.yaml 前端docker-compose配置文件
version: "3.3"
services:

  front-envoy:
    build:
      context: .
      dockerfile: Dockerfile-frontenvoy
    ports:
    - "8000:8000"

  frontend-service:
    build:
      context: .
      dockerfile: Dockerfile-service
```

```
frontend/Dockerfile-frontenvoy 前端envoy Dockerfile配置文件
FROM envoyproxy/envoy-dev:latest

COPY ./front-envoy.yaml /etc/front-envoy.yaml
RUN chmod go+r /etc/front-envoy.yaml
CMD ["/usr/local/bin/envoy", "-c", "/etc/front-envoy.yaml", "--service-cluster", "front-proxy"]
```

```
frontend/Dockerfile-service前端service Dockerfile配置文件
FROM alpine:latest

RUN apk update && apk add py3-pip
RUN pip3 install -q Flask==0.11.1
RUN mkdir /code
ADD ./service.py ./index.html /code/

CMD ["python3", "/code/service.py"]
```

```
frontend/front-envoy.yaml前端envoy配置文件
static_resources:
  listeners:
  - address:
      socket_address:
        address: 0.0.0.0
        port_value: 8000
    filter_chains:
    - filters:
      - name: envoy.filters.network.http_connection_manager
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager
          codec_type: AUTO
          stat_prefix: ingress_http
          access_log:
          - name: envoy.access_loggers.stdout
            typed_config:
              "@type": type.googleapis.com/envoy.extensions.access_loggers.stream.v3.StdoutAccessLog
          route_config:
            name: local_route
            virtual_hosts:
            - name: services
              domains:
              - "*"
              routes:
              - match:
                  prefix: "/"
                route:
                  cluster: frontend_service
          http_filters:
          - name: envoy.filters.http.cors
          - name: envoy.filters.http.router
  clusters:
  - name: frontend_service
    type: STRICT_DNS
    lb_policy: ROUND_ROBIN
    load_assignment:
      cluster_name: frontend_service
      endpoints:
      - lb_endpoints:
        - endpoint:
            address:
              socket_address:
                address: frontend-service
                port_value: 8000
```

```
frontend/index.html前端网页文件
<!DOCTYPE html>
<html>
<head>
    <title>Envoy CORS Webpage</title>
    <link rel="shortcut icon" href="https://www.envoyproxy.io/img/favicon.ico">
    <script type="text/javascript">
        var client = new XMLHttpRequest();
        var resultText;

        function invokeRemoteDomain() {
            var remoteIP = document.getElementById("remoteip").value;
            var enforcement = document.querySelector('input[name="cors"]:checked').value;
            if(client) {
                var url = `http://${remoteIP}:8002/cors/${enforcement}`;
                client.open('GET', url, true);
                client.onreadystatechange = handler;
                client.send();
            } else {
                resultText = "Could not find client to make request.";
                document.getElementById("results").textContent = resultText;
            }
        }

        function handler() {
            var responseHeaders = client.getAllResponseHeaders()
            if (responseHeaders === "") {
                document.getElementById("results").textContent = 'CORS Error';
            }
            if (client.readyState == 4 && client.status == 200) {
                resultText = client.responseText;
                document.getElementById("results").textContent = resultText;
            }
        }
    </script>
</head>
<body>
    <h1>
        Envoy CORS Demo
    </h1>
    <p>
        This page requests an asset from another domain via cross-site XMLHttpRequest mitigated by Access Control.<br/>
        This scenario demonstrates a <a href="https://www.w3.org/TR/cors/#simple-method">simple method</a>.<br/>
        It does <b>NOT</b> dispatch a preflight request.
    </p>
    <p>
        Enter the IP address of backend Docker container. As we are running Docker Compose this should just be localhost.<br/>
    </p>
    <div>
        <input id="remoteip" type="text" placeholder="Remote IP" value="localhost"/>
        <button id="submit" onclick="invokeRemoteDomain()">Fetch asset</button><br/>
        <div style="width:20%;float:left;">
            <h5>CORS Enforcement</h5>
            <input type="radio" name="cors" value="disabled" checked="checked"/> Disabled<br/>
            <input type="radio" name="cors" value="open"/> Open<br/>
            <input type="radio" name="cors" value="restricted"/> Restricted<br/>
            <br/>
        </div>
        <div style="float:left;">
            <h3>Request Results</h3>
            <p id="results"></p>
        </div>
    </div>
</body>
<script>
    var input = document.getElementById("remoteip");
    input.addEventListener("keyup", function(event) {
        event.preventDefault();
        if (event.keyCode === 13) {
            document.getElementById("submit").click();
        }
    });
</script>
</html>
```

```
frontend/service.py前端service python程序
from flask import Flask, send_from_directory
import os

app = Flask(__name__)


@app.route('/')
def index():
    file_dir = os.path.dirname(os.path.realpath(__file__))
    return send_from_directory(file_dir, 'index.html')


if __name__ == "__main__":
    app.run(host='0.0.0.0', port=8000, debug=True)
```

```
backend/docker-compose.yaml后端docker-compose配置文件
version: "3.3"
services:

  front-envoy:
    build:
      context: .
      dockerfile: Dockerfile-frontenvoy
    ports:
    - "8002:8000"
    - "8003:8001"

  backend-service:
    build:
      context: .
      dockerfile: Dockerfile-service
```

```
backend/Dockerfile-frontenvoy后端envoy Dockerfile配置文件
FROM envoyproxy/envoy-dev:latest

COPY ./front-envoy.yaml /etc/front-envoy.yaml
RUN chmod go+r /etc/front-envoy.yaml
CMD ["/usr/local/bin/envoy", "-c", "/etc/front-envoy.yaml", "--service-cluster", "front-proxy"]
```

```
backend/Dockerfile-service后端service Dockerfile配置文件

FROM alpine:latest

RUN apk update && apk add py3-pip
RUN pip3 install -q Flask==0.11.1
RUN mkdir /code
ADD ./service.py /code/

CMD ["python3", "/code/service.py"]
```

```
backend/front-envoy.yaml后端envoy配置文件
static_resources:
  listeners:
  - address:
      socket_address:
        address: 0.0.0.0
        port_value: 8000
    filter_chains:
    - filters:
      - name: envoy.filters.network.http_connection_manager
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager
          codec_type: AUTO
          stat_prefix: ingress_http
          access_log:
          - name: envoy.access_loggers.stdout
            typed_config:
              "@type": type.googleapis.com/envoy.extensions.access_loggers.stream.v3.StdoutAccessLog
          route_config:
            name: local_route
            virtual_hosts:
            - name: www
              domains:
              - "*"
              cors:
                allow_origin_string_match:
                - safe_regex:
                    google_re2: {}
                    regex: \*
                allow_methods: "GET"
                filter_enabled:
                  default_value:
                    numerator: 100
                    denominator: HUNDRED
                  runtime_key: cors.www.enabled
                shadow_enabled:
                  default_value:
                    numerator: 0
                    denominator: HUNDRED
                  runtime_key: cors.www.shadow_enabled
              routes:
              - match:
                  prefix: "/cors/open"
                route:
                  cluster: backend_service
              - match:
                  prefix: "/cors/disabled"
                route:
                  cluster: backend_service
                  cors:
                    filter_enabled:
                      default_value:
                        numerator: 0
                        denominator: HUNDRED
              - match:
                  prefix: "/cors/restricted"
                route:
                  cluster: backend_service
                  cors:
                    allow_origin_string_match:
                    - safe_regex:
                        google_re2: {}
                        regex: .*\.envoyproxy\.io
                    allow_methods: "GET"
              - match:
                  prefix: "/"
                route:
                  cluster: backend_service
          http_filters:
          - name: envoy.filters.http.cors
          - name: envoy.filters.http.router
  clusters:
  - name: backend_service
    type: STRICT_DNS
    lb_policy: ROUND_ROBIN
    load_assignment:
      cluster_name: backend_service
      endpoints:
      - lb_endpoints:
        - endpoint:
            address:
              socket_address:
                address: backend-service
                port_value: 8000

admin:
  address:
    socket_address:
      address: 0.0.0.0
      port_value: 8001
```

```
backend/service.py 后端service python程序
from flask import Flask

app = Flask(__name__)


@app.route('/cors/<status>')
def cors_enabled(status):
    return 'Success!'


if __name__ == "__main__":
    app.run(host='0.0.0.0', port=8000)
```



```
第一步
envoy/examples/cors/frontend
docker-compose pull
docker-compose up --build -d
docker-compose ps


envoy/examples/cors/backend
docker-compose pull
docker-compose up --build -d
docker-compose ps

第二步
http://192.168.198.154:8000

第三步
http://192.168.198.154:8003/stats

```



# CSRF filter

防止跨站请求伪造攻击过滤器

```
samesite/docker-compose.yml samesite docker-compose配置文件
version: '3.3'
services:

  front-envoy:
    build:
      context: .
      dockerfile: Dockerfile-frontenvoy
    ports:
    - "8000:8000"
    - "8001:8001"

  service:
    build:
      context: ..
      dockerfile: samesite/Dockerfile-service
```

```
samesite/Dockerfile-frontenvoy samesite envoy Dockerfile配置文件
FROM envoyproxy/envoy-dev:latest

COPY ./front-envoy.yaml /etc/front-envoy.yaml
RUN chmod go+r /etc/front-envoy.yaml
CMD ["/usr/local/bin/envoy", "-c",  "/etc/front-envoy.yaml", "--service-cluster", "front-proxy"]
```

```
samesite/Dockerfile-service samesite service Dockerfile配置文件
FROM alpine:latest

RUN apk update && apk add py3-pip
RUN pip3 install -q Flask==0.11.1
RUN mkdir /code
ADD ./samesite/service.py ./index.html /code/
CMD ["python3", "/code/service.py"]
```

```
samesite/front-envoy.yaml samesite envoy配置文件
static_resources:
  listeners:
  - address:
      socket_address:
        address: 0.0.0.0
        port_value: 8000
    filter_chains:
    - filters:
      - name: envoy.filters.network.http_connection_manager
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager
          codec_type: AUTO
          stat_prefix: ingress_http
          access_log:
          - name: envoy.access_loggers.stdout
            typed_config:
              "@type": type.googleapis.com/envoy.extensions.access_loggers.stream.v3.StdoutAccessLog
          route_config:
            name: local_route
            virtual_hosts:
            - name: www
              domains:
              - "*"
              cors:
                allow_origin_string_match:
                - safe_regex:
                    google_re2: {}
                    regex: \*
                filter_enabled:
                  default_value:
                    numerator: 100
                    denominator: HUNDRED
              typed_per_filter_config:
                envoy.filters.http.csrf:
                  "@type": type.googleapis.com/envoy.extensions.filters.http.csrf.v3.CsrfPolicy
                  filter_enabled:
                    default_value:
                      numerator: 100
                      denominator: HUNDRED
                    runtime_key: csrf.www.enabled
                  shadow_enabled:
                    default_value:
                      numerator: 0
                      denominator: HUNDRED
                    runtime_key: csrf.www.shadow_enabled
              routes:
              - match:
                  prefix: "/csrf/disabled"
                route:
                  cluster: generic_service
                typed_per_filter_config:
                  envoy.filters.http.csrf:
                    "@type": type.googleapis.com/envoy.extensions.filters.http.csrf.v3.CsrfPolicy
                    filter_enabled:
                      default_value:
                        numerator: 0
                        denominator: HUNDRED
              - match:
                  prefix: "/csrf/shadow"
                route:
                  cluster: generic_service
                typed_per_filter_config:
                  envoy.filters.http.csrf:
                    "@type": type.googleapis.com/envoy.extensions.filters.http.csrf.v3.CsrfPolicy
                    filter_enabled:
                      default_value:
                        numerator: 0
                        denominator: HUNDRED
                    shadow_enabled:
                      default_value:
                        numerator: 100
                        denominator: HUNDRED
              - match:
                  prefix: "/csrf/additional_origin"
                route:
                  cluster: generic_service
                typed_per_filter_config:
                  envoy.filters.http.csrf:
                    "@type": type.googleapis.com/envoy.extensions.filters.http.csrf.v3.CsrfPolicy
                    filter_enabled:
                      default_value:
                        numerator: 100
                        denominator: HUNDRED
                    additional_origins:
                    - safe_regex:
                        google_re2: {}
                        regex: .*
              - match:
                  prefix: "/"
                route:
                  cluster: generic_service
          http_filters:
          - name: envoy.filters.http.cors
          - name: envoy.filters.http.csrf
            typed_config:
              "@type": type.googleapis.com/envoy.extensions.filters.http.csrf.v3.CsrfPolicy
              filter_enabled:
                default_value:
                  numerator: 0
                  denominator: HUNDRED
          - name: envoy.filters.http.router
  clusters:
  - name: generic_service
    type: STRICT_DNS
    lb_policy: ROUND_ROBIN
    load_assignment:
      cluster_name: generic_service
      endpoints:
      - lb_endpoints:
        - endpoint:
            address:
              socket_address:
                address: service
                port_value: 8000

admin:
  address:
    socket_address:
      address: 0.0.0.0
      port_value: 8001
```

```
samesite/service.py samesite service python程序
import os

from flask import Flask, send_from_directory

app = Flask(__name__)
app.url_map.strict_slashes = False


@app.route('/csrf/ignored', methods=['GET'])
def csrf_ignored():
    return 'Success!'


@app.route('/csrf/<status>', methods=['POST'])
def csrf_with_status(status):
    return 'Success!'


@app.route('/', methods=['GET'])
def index():
    file_dir = os.path.dirname(os.path.realpath(__file__))
    return send_from_directory(file_dir, 'index.html')


if __name__ == "__main__":
    app.run(host='0.0.0.0', port=8000)
```

```
crosssite/docker-compose.yml  corsssite docker-compose配置文件
version: '3.3'
services:

  front-envoy:
    build:
      context: .
      dockerfile: Dockerfile-frontenvoy
    ports:
    - "8002:8000"

  service:
    build:
      context: ..
      dockerfile: crosssite/Dockerfile-service
```

```
crosssite/Dockerfile-frontenvoy  crosssite envoy Dockerfile配置文件
FROM envoyproxy/envoy-dev:latest

COPY ./front-envoy.yaml /etc/front-envoy.yaml
RUN chmod go+r /etc/front-envoy.yaml
CMD ["/usr/local/bin/envoy", "-c", "/etc/front-envoy.yaml", "--service-cluster", "front-proxy"]
```

```
crosssite/Dockerfile-service  crosssite service Dockerfile配置文件
FROM alpine:latest

RUN apk update && apk add py3-pip
RUN pip3 install -q Flask==0.11.1
RUN mkdir /code
ADD ./crosssite/service.py ./index.html /code/
CMD ["python3", "/code/service.py"]
```

```
crosssite/front-envoy.yaml  crosssite envoy配置文件
static_resources:
  listeners:
  - address:
      socket_address:
        address: 0.0.0.0
        port_value: 8000
    filter_chains:
    - filters:
      - name: envoy.filters.network.http_connection_manager
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager
          codec_type: AUTO
          stat_prefix: ingress_http
          access_log:
          - name: envoy.access_loggers.stdout
            typed_config:
              "@type": type.googleapis.com/envoy.extensions.access_loggers.stream.v3.StdoutAccessLog
          route_config:
            name: local_route
            virtual_hosts:
            - name: www
              domains:
              - "*"
              routes:
              - match:
                  prefix: "/"
                route:
                  cluster: generic_service
          http_filters:
          - name: envoy.filters.http.router
  clusters:
  - name: generic_service
    type: STRICT_DNS
    lb_policy: ROUND_ROBIN
    load_assignment:
      cluster_name: generic_service
      endpoints:
      - lb_endpoints:
        - endpoint:
            address:
              socket_address:
                address: service
                port_value: 8000
```

```
crosssite/service.py crosssite service python程序
import os

from flask import Flask, send_from_directory

app = Flask(__name__)
app.url_map.strict_slashes = False


@app.route('/', methods=['GET'])
def index():
    file_dir = os.path.dirname(os.path.realpath(__file__))
    return send_from_directory(file_dir, 'index.html')


if __name__ == "__main__":
    app.run(host='0.0.0.0', port=8000)
```

```
index.html网页文件
<!DOCTYPE html>
<html>
<head>
    <title>Envoy CSRF Wepage</title>
    <link rel="shortcut icon" href="https://www.envoyproxy.io/img/favicon.ico">
    <script type="text/javascript">
        var client = new XMLHttpRequest();
        var resultText;

        function submitToDomain() {
            var remoteIP = document.getElementById("destinationip").value;
            var enforcement = document.querySelector('input[name="csrf"]:checked').value;
            var method = enforcement !== 'ignored' ? 'POST' : 'GET';
            if(client) {
                var url = `http://${remoteIP}:8000/csrf/${enforcement}`;
                client.open(method, url, true);
                client.onreadystatechange = handler;
                client.send();
            } else {
                resultText = "Could not find client to make request.";
                document.getElementById("results").textContent = resultText;
            }
        }

        function handler() {
            var responseCode = client.status;
            if (client.readyState == 4 && responseCode == 403) {
                resultText = 'Rejected by CSRF';
            }
            else if (client.readyState == 4 && responseCode == 200) {
                resultText = client.responseText;
            }
            else if (client.readyState == 4) {
                resultText = 'Unknown Error. Check the console.';
            }
            document.getElementById("results").textContent = resultText;
        }
    </script>
</head>
<body>
    <h1>
        Envoy CSRF Demo
    </h1>
    <p>
        This page demonstrates a few scenarios for CSRF.
    </p>
    <p>
        Enter the IP address of the destination Docker container.<br/>
    </p>
    <div>
        <input id="destinationip" type="text" placeholder="Destination IP" value="localhost"/>
        <button id="submit" onclick="submitToDomain()">Post to destination</button><br/>
        <div style="width:20%;float:left;">
            <h5>CSRF Enforcement</h5>
            <input type="radio" name="csrf" value="disabled" checked="checked"/> Disabled<br/>
            <input type="radio" name="csrf" value="shadow"/> Shadow Mode<br/>
            <input type="radio" name="csrf" value="enabled"/> Enabled<br/>
            <input type="radio" name="csrf" value="ignored"/> Ignored<br/>
            <input type="radio" name="csrf" value="additional_origin"/> Additional Origin<br/>
            <br/>
        </div>
        <div style="float:left;">
            <h3>Request Results</h3>
            <p id="results"></p>
        </div>
    </div>
</body>
<script>
    var input = document.getElementById("remoteip");
    if (input) {
        input.addEventListener("keyup", function(event) {
            event.preventDefault();
            if (event.keyCode === 13) {
                document.getElementById("submit").click();
            }
        });
    }
</script>
</html>
```

```
start_service.sh 启动service脚本
#!/bin/sh
python3 /code/service.py &
envoy -c /etc/service-envoy.yaml --service-cluster service
```



```
第一步
envoy/examples/csrf/samesite
docker-compose pull
docker-compose up --build -d
docker-compose ps

envoy/examples/csrf/crosssite
docker-compose up --build -d
docker-compose ps
第二步
http://192.168.198.154:8002
 http://192.168.198.154:8000 

第三步
http://192.168.198.154:8001/stats
```



# Double proxy (with mTLS encryption)

 `Envoy (front)` -> `Flask` -> `Envoy (postgres-front)` -> `Envoy (postgres-back)` -> `PostgreSQL` 

双层代理，连postgresql

```
docker-compose.yaml docker-compose配置文件
version: "3.3"
services:

  proxy-frontend:
    build:
      context: .
      dockerfile: Dockerfile-proxy
    networks:
      edge:
    ports:
    - "10000:10000"

  app:
    build:
      context: .
      dockerfile: Dockerfile-app
    networks:
      edge:
      postgres-frontend:

  proxy-postgres-frontend:
    build:
      context: .
      dockerfile: Dockerfile-proxy-frontend
    networks:
      postgres-frontend:
        aliases:
        - postgres
      postgres-in-between:

  proxy-postgres-backend:
    build:
      context: .
      dockerfile: Dockerfile-proxy-backend
    networks:
      postgres-backend:
      postgres-in-between:
        aliases:
        - proxy-postgres-backend.example.com

  postgres:
    image: postgres:latest
    networks:
      postgres-backend:
    environment:
      # WARNING! Do not use it on production environments because this will
      #          allow anyone with access to the Postgres port to access your
      #          database without a password, even if POSTGRES_PASSWORD is set.
      #          See PostgreSQL documentation about "trust":
      #          https://www.postgresql.org/docs/current/auth-trust.html
      POSTGRES_HOST_AUTH_METHOD: trust

networks:
  edge:
    name: edge

  postgres-backend:
    name: postgres-backend

  postgres-frontend:
    name: postgres-frontend

  postgres-in-between:
    name: postgres-in-between
```

```
Dockerfile-app  app Dockerfile配置文件
FROM python:3.8-alpine

RUN apk update && apk add postgresql-dev gcc python3-dev musl-dev
RUN pip3 install -q Flask==0.11.1 requests==2.18.4 psycopg2-binary
RUN mkdir /code
ADD ./service.py /code
ENTRYPOINT ["python3", "/code/service.py"]
```

```
Dockerfile-proxy  proxy dockerfile配置文件
FROM envoyproxy/envoy-dev:latest

COPY ./envoy.yaml /etc/envoy.yaml
RUN chmod go+r /etc/envoy.yaml
CMD ["/usr/local/bin/envoy", "-c /etc/envoy.yaml", "-l", "debug"]
```

```
Dockerfile-proxy-backend backend proxy Dockerfile配置文件
FROM envoyproxy/envoy-dev:latest

COPY ./envoy-backend.yaml /etc/envoy.yaml
COPY ./certs/ca.crt /certs/cacert.pem
COPY ./certs/postgres-backend.example.com.crt /certs/servercert.pem
COPY ./certs/example.com.key /certs/serverkey.pem

RUN chmod go+r /etc/envoy.yaml /certs/cacert.pem /certs/serverkey.pem /certs/servercert.pem
CMD ["/usr/local/bin/envoy", "-c /etc/envoy.yaml", "-l", "debug"]
```

```
Dockerfile-proxy-frontend frontend proxy Dockerfile配置文件
FROM envoyproxy/envoy-dev:latest

COPY ./envoy-frontend.yaml /etc/envoy.yaml
COPY ./certs/ca.crt /certs/cacert.pem
COPY ./certs/postgres-frontend.example.com.crt /certs/clientcert.pem
COPY ./certs/example.com.key /certs/clientkey.pem

RUN chmod go+r /etc/envoy.yaml /certs/cacert.pem /certs/clientkey.pem /certs/clientcert.pem
CMD ["/usr/local/bin/envoy", "-c /etc/envoy.yaml", "-l", "debug"]
```

```
envoy-backend.yaml backend envoy配置文件
static_resources:
  listeners:
  - name: postgres_listener
    address:
      socket_address:
        address: 0.0.0.0
        port_value: 5432
    listener_filters:
    - name: "envoy.filters.listener.tls_inspector"
    filter_chains:
    - filters:
      - name: envoy.filters.network.postgres_proxy
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.filters.network.postgres_proxy.v3alpha.PostgresProxy
          stat_prefix: egress_postgres
      - name: envoy.filters.network.tcp_proxy
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.filters.network.tcp_proxy.v3.TcpProxy
          stat_prefix: postgres_tcp
          cluster: postgres_cluster
      transport_socket:
        name: envoy.transport_sockets.tls
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.transport_sockets.tls.v3.DownstreamTlsContext
          require_client_certificate: true
          common_tls_context:
            tls_certificates:
            - certificate_chain:
                filename: certs/servercert.pem
              private_key:
                filename: certs/serverkey.pem
            validation_context:
              match_subject_alt_names:
              - exact: proxy-postgres-frontend.example.com
              trusted_ca:
                filename: certs/cacert.pem

  clusters:
  - name: postgres_cluster
    type: STRICT_DNS
    load_assignment:
      cluster_name: postgres_cluster
      endpoints:
      - lb_endpoints:
        - endpoint:
            address:
              socket_address:
                address: postgres
                port_value: 5432
```

```
envoy-frontend.yaml frontend envoy配置文件
static_resources:
  listeners:
  - name: postgres_listener
    address:
      socket_address:
        address: 0.0.0.0
        port_value: 5432
    filter_chains:
    - filters:
      - name: envoy.filters.network.postgres_proxy
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.filters.network.postgres_proxy.v3alpha.PostgresProxy
          stat_prefix: egress_postgres
      - name: envoy.filters.network.tcp_proxy
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.filters.network.tcp_proxy.v3.TcpProxy
          stat_prefix: postgres_tcp
          cluster: postgres_cluster

  clusters:
  - name: postgres_cluster
    type: STRICT_DNS
    load_assignment:
      cluster_name: postgres_cluster
      endpoints:
      - lb_endpoints:
        - endpoint:
            address:
              socket_address:
                address: proxy-postgres-backend.example.com
                port_value: 5432
    transport_socket:
      name: envoy.transport_sockets.tls
      typed_config:
        "@type": type.googleapis.com/envoy.extensions.transport_sockets.tls.v3.UpstreamTlsContext
        common_tls_context:
          tls_certificates:
          - certificate_chain:
              filename: certs/clientcert.pem
            private_key:
              filename: certs/clientkey.pem
          validation_context:
            match_subject_alt_names:
            - exact: proxy-postgres-backend.example.com
            trusted_ca:
              filename: certs/cacert.pem
```

```
envoy.yaml envoy 配置文件
static_resources:
  listeners:
  - address:
      socket_address:
        address: 0.0.0.0
        port_value: 10000
    filter_chains:
    - filters:
      - name: envoy.filters.network.http_connection_manager
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager
          codec_type: AUTO
          stat_prefix: ingress_http
          route_config:
            name: local_route
            virtual_hosts:
            - name: app
              domains:
              - "*"
              routes:
              - match:
                  prefix: "/"
                route:
                  cluster: service1
          http_filters:
          - name: envoy.filters.http.router

  clusters:
  - name: service1
    type: STRICT_DNS
    lb_policy: ROUND_ROBIN
    load_assignment:
      cluster_name: service1
      endpoints:
      - lb_endpoints:
        - endpoint:
            address:
              socket_address:
                address: app
                port_value: 8000
```

```
service.py service python程序
import sys

from flask import Flask

import psycopg2

app = Flask(__name__)


@app.route('/')
def hello():
    conn = psycopg2.connect("host=postgres user=postgres")
    cur = conn.cursor()
    cur.execute('SELECT version()')
    msg = 'Connected to Postgres, version: %s' % cur.fetchone()
    cur.close()
    return msg


if __name__ == "__main__":
    app.run(host='0.0.0.0', port=8000, debug=True)
```

```
第一步
mkdir -p certs
openssl genrsa -out certs/ca.key 4096
openssl req -x509 -new -nodes -key certs/ca.key -sha256 -days 1024 -out certs/ca.crt

第二步
openssl genrsa -out certs/example.com.key 2048

第三步
openssl req -new -sha256 \
     -key certs/example.com.key \
     -subj "/C=US/ST=CA/O=MyExample, Inc./CN=proxy-postgres-frontend.example.com" \
     -out certs/proxy-postgres-frontend.example.com.csr
openssl req -new -sha256 \
     -key certs/example.com.key \
     -subj "/C=US/ST=CA/O=MyExample, Inc./CN=proxy-postgres-backend.example.com" \
     -out certs/proxy-postgres-backend.example.com.csr

第四步
openssl x509 -req \
     -in certs/proxy-postgres-frontend.example.com.csr \
     -CA certs/ca.crt \
     -CAkey certs/ca.key \
     -CAcreateserial \
     -extfile <(printf "subjectAltName=DNS:proxy-postgres-frontend.example.com") \
     -out certs/postgres-frontend.example.com.crt \
     -days 500 \
     -sha256

openssl x509 -req \
     -in certs/proxy-postgres-backend.example.com.csr \
     -CA certs/ca.crt \
     -CAkey certs/ca.key \
     -CAcreateserial \
     -extfile <(printf "subjectAltName=DNS:proxy-postgres-backend.example.com") \
     -out certs/postgres-backend.example.com.crt \
     -days 500 \
     -sha256
     
 第五步
docker-compose build --pull
docker-compose up -d
docker-compose ps

第六步
curl -s http://localhost:10000

```



# Dynamic configuration (filesystem)

基于文件的动态配置

```
docker-compose.yaml docker-compose配置文件
version: "3.3"
services:

  proxy:
    build:
      context: .
      dockerfile: Dockerfile-proxy
    depends_on:
    - service1
    - service2
    ports:
    - 10000:10000
    - 19000:19000

  service1:
    image: jmalloc/echo-server
    hostname: service1

  service2:
    image: jmalloc/echo-server
    hostname: service2
```

```
Dockerfile-proxy proxy Dockerfile配置文件
FROM envoyproxy/envoy-dev:latest

COPY ./envoy.yaml /etc/envoy.yaml
COPY ./configs /var/lib/envoy
RUN chmod go+x /var/lib/envoy \
    && chmod go+r /etc/envoy.yaml /var/lib/envoy/*
CMD ["/usr/local/bin/envoy", "-c /etc/envoy.yaml", "-l", "debug"]
```

```
 envoy.yaml envoy配置文件
 node:
  id: id_1
  cluster: test

dynamic_resources:
  cds_config:
    path: /var/lib/envoy/cds.yaml
  lds_config:
    path: /var/lib/envoy/lds.yaml

admin:
  address:
    socket_address:
      address: 0.0.0.0
      port_value: 19000
```

```
configs/cds.yaml cds配置
resources:
- "@type": type.googleapis.com/envoy.config.cluster.v3.Cluster
  name: example_proxy_cluster
  type: STRICT_DNS
  load_assignment:
    cluster_name: example_proxy_cluster
    endpoints:
    - lb_endpoints:
      - endpoint:
          address:
            socket_address:
              address: service1
              port_value: 8080
```

```
configs/lds.yaml lds配置文件
resources:
- "@type": type.googleapis.com/envoy.config.listener.v3.Listener
  name: listener_0
  address:
    socket_address:
      address: 0.0.0.0
      port_value: 10000
  filter_chains:
  - filters:
      name: envoy.http_connection_manager
      typed_config:
        "@type": type.googleapis.com/envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager
        stat_prefix: ingress_http
        http_filters:
        - name: envoy.router
        route_config:
          name: local_route
          virtual_hosts:
          - name: local_service
            domains:
            - "*"
            routes:
            - match:
                prefix: "/"
              route:
                cluster: example_proxy_cluster
```

```

```

```
第一步
envoy/examples/dynamic-config-fs
docker-compose build --pull
docker-compose up -d
docker-compose ps

第二步
curl -s http://localhost:10000

第三步
curl -s http://localhost:19000/config_dump | jq -r '.configs[1].dynamic_active_clusters'

第四步
docker-compose exec -T proxy sed -i s/service1/service2/ /var/lib/envoy/cds.yaml
curl http://localhost:10000 | grep "served by"
curl -s http://localhost:19000/config_dump | jq -r '.configs[1].dynamic_active_clusters'
```



# Dynamic configuration (control plane)



```
docker-compose.yaml docker-compose配置文件
version: "3.3"
services:

  proxy:
    build:
      context: .
      dockerfile: Dockerfile-proxy
    depends_on:
    - service1
    - service2
    ports:
    - 10000:10000
    - 19000:19000

  service1:
    image: jmalloc/echo-server
    hostname: service1

  service2:
    image: jmalloc/echo-server
    hostname: service2

  go-control-plane:
    build:
      context: .
      dockerfile: Dockerfile-control-plane
    command: bin/example -debug
    healthcheck:
      test: nc -zv localhost 18000
```

```
Dockerfile-control-plane 控制面板Dockerfile文件
FROM golang

RUN apt-get update \
    && apt-get install --no-install-recommends -y netcat \
    && apt-get autoremove -y \
    && apt-get clean \
    && rm -rf /tmp/* /var/tmp/* /var/lib/apt/lists/*

RUN git clone https://github.com/envoyproxy/go-control-plane
ADD ./resource.go /go/go-control-plane/internal/example/resource.go
RUN cd go-control-plane && make bin/example
WORKDIR /go/go-control-plane
```

```
Dockerfile-proxy proxy Dockerfile配置文件
FROM envoyproxy/envoy-dev:latest

COPY ./envoy.yaml /etc/envoy.yaml
RUN chmod go+r /etc/envoy.yaml
CMD ["/usr/local/bin/envoy", "-c /etc/envoy.yaml", "-l", "debug"]
```

```
envoy.yaml envoy配置文件
node:
  cluster: test-cluster
  id: test-id

dynamic_resources:
  ads_config:
    api_type: GRPC
    transport_api_version: V3
    grpc_services:
    - envoy_grpc:
        cluster_name: xds_cluster
  cds_config:
    resource_api_version: V3
    ads: {}
  lds_config:
    resource_api_version: V3
    ads: {}

static_resources:
  clusters:
  - type: STRICT_DNS
    typed_extension_protocol_options:
      envoy.extensions.upstreams.http.v3.HttpProtocolOptions:
        "@type": type.googleapis.com/envoy.extensions.upstreams.http.v3.HttpProtocolOptions
        explicit_http_config:
          http2_protocol_options: {}
    name: xds_cluster
    load_assignment:
      cluster_name: xds_cluster
      endpoints:
      - lb_endpoints:
        - endpoint:
            address:
              socket_address:
                address: go-control-plane
                port_value: 18000

admin:
  address:
    socket_address:
      address: 0.0.0.0
      port_value: 19000
```

```
resource.go go程序
// Copyright 2020 Envoyproxy Authors
//
//   Licensed under the Apache License, Version 2.0 (the "License");
//   you may not use this file except in compliance with the License.
//   You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.
package example

import (
        "time"

        "github.com/golang/protobuf/ptypes"

        cluster "github.com/envoyproxy/go-control-plane/envoy/config/cluster/v3"
        core "github.com/envoyproxy/go-control-plane/envoy/config/core/v3"
        endpoint "github.com/envoyproxy/go-control-plane/envoy/config/endpoint/v3"
        listener "github.com/envoyproxy/go-control-plane/envoy/config/listener/v3"
        route "github.com/envoyproxy/go-control-plane/envoy/config/route/v3"
        hcm "github.com/envoyproxy/go-control-plane/envoy/extensions/filters/network/http_connection_manager/v3"
        "github.com/envoyproxy/go-control-plane/pkg/cache/types"
        "github.com/envoyproxy/go-control-plane/pkg/cache/v3"
        "github.com/envoyproxy/go-control-plane/pkg/resource/v3"
        "github.com/envoyproxy/go-control-plane/pkg/wellknown"
)

const (
        ClusterName  = "example_proxy_cluster"
        RouteName    = "local_route"
        ListenerName = "listener_0"
        ListenerPort = 10000
        UpstreamHost = "service1"
        UpstreamPort = 8080
)

func makeCluster(clusterName string) *cluster.Cluster {
        return &cluster.Cluster{
                Name:                 clusterName,
                ConnectTimeout:       ptypes.DurationProto(5 * time.Second),
                ClusterDiscoveryType: &cluster.Cluster_Type{Type: cluster.Cluster_LOGICAL_DNS},
                LbPolicy:             cluster.Cluster_ROUND_ROBIN,
                LoadAssignment:       makeEndpoint(clusterName),
                DnsLookupFamily:      cluster.Cluster_V4_ONLY,
        }
}

func makeEndpoint(clusterName string) *endpoint.ClusterLoadAssignment {
        return &endpoint.ClusterLoadAssignment{
                ClusterName: clusterName,
                Endpoints: []*endpoint.LocalityLbEndpoints{{
                        LbEndpoints: []*endpoint.LbEndpoint{{
                                HostIdentifier: &endpoint.LbEndpoint_Endpoint{
                                        Endpoint: &endpoint.Endpoint{
                                                Address: &core.Address{
                                                        Address: &core.Address_SocketAddress{
                                                                SocketAddress: &core.SocketAddress{
                                                                        Protocol: core.SocketAddress_TCP,
                                                                        Address:  UpstreamHost,
                                                                        PortSpecifier: &core.SocketAddress_PortValue{
                                                                                PortValue: UpstreamPort,
                                                                        },
                                                                },
                                                        },
                                                },
                                        },
                                },
                        }},
                }},
        }
}

func makeRoute(routeName string, clusterName string) *route.RouteConfiguration {
        return &route.RouteConfiguration{
                Name: routeName,
                VirtualHosts: []*route.VirtualHost{{
                        Name:    "local_service",
                        Domains: []string{"*"},
                        Routes: []*route.Route{{
                                Match: &route.RouteMatch{
                                        PathSpecifier: &route.RouteMatch_Prefix{
                                                Prefix: "/",
                                        },
                                },
                                Action: &route.Route_Route{
                                        Route: &route.RouteAction{
                                                ClusterSpecifier: &route.RouteAction_Cluster{
                                                        Cluster: clusterName,
                                                },
                                        },
                                },
                        }},
                }},
        }
}

func makeHTTPListener(listenerName string, route string) *listener.Listener {
        // HTTP filter configuration
        manager := &hcm.HttpConnectionManager{
                CodecType:  hcm.HttpConnectionManager_AUTO,
                StatPrefix: "http",
                RouteSpecifier: &hcm.HttpConnectionManager_Rds{
                        Rds: &hcm.Rds{
                                ConfigSource:    makeConfigSource(),
                                RouteConfigName: route,
                        },
                },
                HttpFilters: []*hcm.HttpFilter{{
                        Name: wellknown.Router,
                }},
        }
        pbst, err := ptypes.MarshalAny(manager)
        if err != nil {
                panic(err)
        }

        return &listener.Listener{
                Name: listenerName,
                Address: &core.Address{
                        Address: &core.Address_SocketAddress{
                                SocketAddress: &core.SocketAddress{
                                        Protocol: core.SocketAddress_TCP,
                                        Address:  "0.0.0.0",
                                        PortSpecifier: &core.SocketAddress_PortValue{
                                                PortValue: ListenerPort,
                                        },
                                },
                        },
                },
                FilterChains: []*listener.FilterChain{{
                        Filters: []*listener.Filter{{
                                Name: wellknown.HTTPConnectionManager,
                                ConfigType: &listener.Filter_TypedConfig{
                                        TypedConfig: pbst,
                                },
                        }},
                }},
        }
}

func makeConfigSource() *core.ConfigSource {
        source := &core.ConfigSource{}
        source.ResourceApiVersion = resource.DefaultAPIVersion
        source.ConfigSourceSpecifier = &core.ConfigSource_ApiConfigSource{
                ApiConfigSource: &core.ApiConfigSource{
                        TransportApiVersion:       resource.DefaultAPIVersion,
                        ApiType:                   core.ApiConfigSource_GRPC,
                        SetNodeOnFirstMessageOnly: true,
                        GrpcServices: []*core.GrpcService{{
                                TargetSpecifier: &core.GrpcService_EnvoyGrpc_{
                                        EnvoyGrpc: &core.GrpcService_EnvoyGrpc{ClusterName: "xds_cluster"},
                                },
                        }},
                },
        }
        return source
}

func GenerateSnapshot() cache.Snapshot {
        return cache.NewSnapshot(
                "1",
                []types.Resource{}, // endpoints
                []types.Resource{makeCluster(ClusterName)},
                []types.Resource{makeRoute(RouteName, ClusterName)},
                []types.Resource{makeHTTPListener(ListenerName, RouteName)},
                []types.Resource{}, // runtimes
                []types.Resource{}, // secrets
        )
}
```

```
第一步
envoy/examples/dynamic-config-cp
docker-compose build --pull
docker-compose up -d proxy
docker-compose ps
第二步
curl http://localhost:10000
curl -s http://localhost:19000/config_dump  | jq '.configs[1].static_clusters'
curl -s http://localhost:19000/config_dump  | jq '.configs[1].dynamic_active_clusters'
第三步
docker-compose up --build -d go-control-plane
docker-compose ps
第四步
curl http://localhost:10000
第五步
curl -s http://localhost:19000/config_dump  | jq '.configs[1].dynamic_active_clusters'
第六步
docker-compose stop go-control-plane
curl http://localhost:10000 | grep "served by"



```



# External authorization (ext_authz) filter

调用外部服务授权

```
config/grpc-service/v3.yaml
static_resources:
  listeners:
  - address:
      socket_address:
        address: 0.0.0.0
        port_value: 8000
    filter_chains:
    - filters:
      - name: envoy.filters.network.http_connection_manager
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager
          codec_type: AUTO
          stat_prefix: ingress_http
          route_config:
            name: local_route
            virtual_hosts:
            - name: upstream
              domains:
              - "*"
              routes:
              - match:
                  prefix: "/"
                route:
                  cluster: upstream-service
          http_filters:
          - name: envoy.filters.http.ext_authz
            typed_config:
              "@type": type.googleapis.com/envoy.extensions.filters.http.ext_authz.v3.ExtAuthz
              grpc_service:
                envoy_grpc:
                  cluster_name: ext_authz-grpc-service
                timeout: 0.250s
              transport_api_version: V3
          - name: envoy.filters.http.router

  clusters:
  - name: upstream-service
    type: STRICT_DNS
    lb_policy: ROUND_ROBIN
    load_assignment:
      cluster_name: upstream-service
      endpoints:
      - lb_endpoints:
        - endpoint:
            address:
              socket_address:
                address: upstream-service
                port_value: 8080

  - name: ext_authz-grpc-service
    type: STRICT_DNS
    lb_policy: ROUND_ROBIN
    typed_extension_protocol_options:
      envoy.extensions.upstreams.http.v3.HttpProtocolOptions:
        "@type": type.googleapis.com/envoy.extensions.upstreams.http.v3.HttpProtocolOptions
        explicit_http_config:
          http2_protocol_options: {}
    load_assignment:
      cluster_name: ext_authz-grpc-service
      endpoints:
      - lb_endpoints:
        - endpoint:
            address:
              socket_address:
                address: ext_authz-grpc-service
                port_value: 9001
```

```
config/opa-service/v3.yaml 
static_resources:
  listeners:
  - address:
      socket_address:
        address: 0.0.0.0
        port_value: 8000
    filter_chains:
    - filters:
      - name: envoy.filters.network.http_connection_manager
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager
          codec_type: AUTO
          stat_prefix: ingress_http
          route_config:
            name: local_route
            virtual_hosts:
            - name: upstream
              domains:
              - "*"
              routes:
              - match:
                  prefix: "/"
                route:
                  cluster: upstream-service
          http_filters:
          - name: envoy.filters.http.ext_authz
            typed_config:
              "@type": type.googleapis.com/envoy.extensions.filters.http.ext_authz.v3.ExtAuthz
              grpc_service:
                envoy_grpc:
                  cluster_name: ext_authz-opa-service
                timeout: 0.250s
              transport_api_version: V3
          - name: envoy.filters.http.router

  clusters:
  - name: upstream-service
    type: STRICT_DNS
    lb_policy: ROUND_ROBIN
    load_assignment:
      cluster_name: upstream-service
      endpoints:
      - lb_endpoints:
        - endpoint:
            address:
              socket_address:
                address: upstream-service
                port_value: 8080

  - name: ext_authz-opa-service
    type: STRICT_DNS
    lb_policy: ROUND_ROBIN
    typed_extension_protocol_options:
      envoy.extensions.upstreams.http.v3.HttpProtocolOptions:
        "@type": type.googleapis.com/envoy.extensions.upstreams.http.v3.HttpProtocolOptions
        explicit_http_config:
          http2_protocol_options: {}
    load_assignment:
      cluster_name: ext_authz-opa-service
      endpoints:
      - lb_endpoints:
        - endpoint:
            address:
              socket_address:
                address: ext_authz-opa-service
                port_value: 9002
```

```
config/http-service.yaml
static_resources:
  listeners:
  - address:
      socket_address:
        address: 0.0.0.0
        port_value: 8000
    filter_chains:
    - filters:
      - name: envoy.filters.network.http_connection_manager
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager
          codec_type: AUTO
          stat_prefix: ingress_http
          route_config:
            name: local_route
            virtual_hosts:
            - name: upstream
              domains:
              - "*"
              routes:
              - match:
                  prefix: "/"
                route:
                  cluster: upstream-service
          http_filters:
          - name: envoy.filters.http.ext_authz
            typed_config:
              "@type": type.googleapis.com/envoy.extensions.filters.http.ext_authz.v3.ExtAuthz
              transport_api_version: V3
              http_service:
                server_uri:
                  uri: ext_authz
                  cluster: ext_authz-http-service
                  timeout: 0.250s
                authorization_response:
                  allowed_upstream_headers:
                    patterns:
                    - exact: x-current-user
          - name: envoy.filters.http.router

  clusters:
  - name: upstream-service
    type: STRICT_DNS
    lb_policy: ROUND_ROBIN
    load_assignment:
      cluster_name: upstream-service
      endpoints:
      - lb_endpoints:
        - endpoint:
            address:
              socket_address:
                address: upstream-service
                port_value: 8080

  - name: ext_authz-http-service
    type: STRICT_DNS
    lb_policy: ROUND_ROBIN
    load_assignment:
      cluster_name: ext_authz-http-service
      endpoints:
      - lb_endpoints:
        - endpoint:
            address:
              socket_address:
                address: ext_authz-http-service
                port_value: 9002
```

```
第一步
envoy/examples/ext_authz
docker-compose pull
docker-compose up --build -d
docker-compose ps

envoy/examples/ext_authz
docker-compose pull
# Tearing down the currently running setup
docker-compose down
FRONT_ENVOY_YAML=config/http-service.yaml docker-compose up --build -d

第二步
curl -v localhost:8000/service

第三步
curl -v -H "Authorization: Bearer token1" localhost:8000/service

第四步
envoy/examples/ext_authz
docker-compose pull
# Tearing down the currently running setup
docker-compose down
FRONT_ENVOY_YAML=config/opa-service/v3.yaml docker-compose up --build -d

第五步
curl localhost:8000/service --verbose

docker-compose logs ext_authz-opa-service | grep decision_id -A 30
```



# Fault injection filter

故障注入

```
envoy.yaml
static_resources:
  listeners:
  - address:
      socket_address:
        address: 0.0.0.0
        port_value: 9211
    filter_chains:
    - filters:
      - name: envoy.filters.network.http_connection_manager
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager
          codec_type: AUTO
          stat_prefix: ingress_http
          access_log:
          - name: envoy.access_loggers.stdout
            typed_config:
              "@type": type.googleapis.com/envoy.extensions.access_loggers.stream.v3.StdoutAccessLog
          route_config:
            name: local_route
            virtual_hosts:
            - name: service
              domains:
              - "*"
              routes:
              - match:
                  prefix: /
                route:
                  cluster: local_service
          http_filters:
          - name: envoy.filters.http.fault
            typed_config:
              "@type": type.googleapis.com/envoy.extensions.filters.http.fault.v3.HTTPFault
              abort:
                http_status: 503
                percentage:
                  numerator: 0
                  denominator: HUNDRED
              delay:
                fixed_delay: 3s
                percentage:
                  numerator: 0
                  denominator: HUNDRED
          - name: envoy.filters.http.router
  clusters:
  - name: local_service
    type: STRICT_DNS
    lb_policy: ROUND_ROBIN
    load_assignment:
      cluster_name: local_service
      endpoints:
      - lb_endpoints:
        - endpoint:
            address:
              socket_address:
                address: backend
                port_value: 80
layered_runtime:
  layers:
  - name: disk_layer_0
    disk_layer:
      symlink_root: /srv/runtime/current
      subdirectory: envoy
```

```
第一步
envoy/examples/fault-injection
docker-compose pull
docker-compose up --build -d
docker-compose ps

第二步
envoy/examples/fault-injection
docker-compose exec envoy bash
bash send_request.sh

第三步
docker-compose exec envoy bash
bash enable_abort_fault_injection.sh

bash disable_abort_fault_injection.sh


```



# Front proxy

服务前面加一个前置envoy

```
front-envoy.yaml 
static_resources:
  listeners:
  - address:
      socket_address:
        address: 0.0.0.0
        port_value: 8080
    filter_chains:
    - filters:
      - name: envoy.filters.network.http_connection_manager
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager
          codec_type: AUTO
          stat_prefix: ingress_http
          route_config:
            name: local_route
            virtual_hosts:
            - name: backend
              domains:
              - "*"
              routes:
              - match:
                  prefix: "/service/1"
                route:
                  cluster: service1
              - match:
                  prefix: "/service/2"
                route:
                  cluster: service2
          http_filters:
          - name: envoy.filters.http.router

  - address:
      socket_address:
        address: 0.0.0.0
        port_value: 8443
    filter_chains:
    - filters:
      - name: envoy.filters.network.http_connection_manager
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager
          codec_type: AUTO
          stat_prefix: ingress_http
          route_config:
            name: local_route
            virtual_hosts:
            - name: backend
              domains:
              - "*"
              routes:
              - match:
                  prefix: "/service/1"
                route:
                  cluster: service1
              - match:
                  prefix: "/service/2"
                route:
                  cluster: service2
          http_filters:
          - name: envoy.filters.http.router

      transport_socket:
        name: envoy.transport_sockets.tls
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.transport_sockets.tls.v3.DownstreamTlsContext
          common_tls_context:
            tls_certificates:
            # The following self-signed certificate pair is generated using:
            # $ openssl req -x509 -newkey rsa:2048 -keyout a/front-proxy-key.pem -out  a/front-proxy-crt.pem -days 3650 -nodes -subj '/CN=front-envoy'
            #
            # Instead of feeding it as an inline_string, certificate pair can also be fed to Envoy
            # via filename. Reference: https://www.envoyproxy.io/docs/envoy/latest/api-v3/config/core/v3/base.proto#config-core-v3-datasource.
            #
            # Or in a dynamic configuration scenario, certificate pair can be fetched remotely via
            # Secret Discovery Service (SDS). Reference: https://www.envoyproxy.io/docs/envoy/latest/configuration/security/secret.
            - certificate_chain:
                inline_string: |
                  -----BEGIN CERTIFICATE-----
                  MIICqDCCAZACCQCquzpHNpqBcDANBgkqhkiG9w0BAQsFADAWMRQwEgYDVQQDDAtm
                  cm9udC1lbnZveTAeFw0yMDA3MDgwMTMxNDZaFw0zMDA3MDYwMTMxNDZaMBYxFDAS
                  BgNVBAMMC2Zyb250LWVudm95MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKC
                  AQEAthnYkqVQBX+Wg7aQWyCCb87hBce1hAFhbRM8Y9dQTqxoMXZiA2n8G089hUou
                  oQpEdJgitXVS6YMFPFUUWfwcqxYAynLK4X5im26Yfa1eO8La8sZUS+4Bjao1gF5/
                  VJxSEo2yZ7fFBo8M4E44ZehIIocipCRS+YZehFs6dmHoq/MGvh2eAHIa+O9xssPt
                  ofFcQMR8rwBHVbKy484O10tNCouX4yUkyQXqCRy6HRu7kSjOjNKSGtjfG+h5M8bh
                  10W7ZrsJ1hWhzBulSaMZaUY3vh5ngpws1JATQVSK1Jm/dmMRciwlTK7KfzgxHlSX
                  58ENpS7yPTISkEICcLbXkkKGEQIDAQABMA0GCSqGSIb3DQEBCwUAA4IBAQCmj6Hg
                  vwOxWz0xu+6fSfRL6PGJUGq6wghCfUvjfwZ7zppDUqU47fk+yqPIOzuGZMdAqi7N
                  v1DXkeO4A3hnMD22Rlqt25vfogAaZVToBeQxCPd/ALBLFrvLUFYuSlS3zXSBpQqQ
                  Ny2IKFYsMllz5RSROONHBjaJOn5OwqenJ91MPmTAG7ujXKN6INSBM0PjX9Jy4Xb9
                  zT+I85jRDQHnTFce1WICBDCYidTIvJtdSSokGSuy4/xyxAAc/BpZAfOjBQ4G1QRe
                  9XwOi790LyNUYFJVyeOvNJwveloWuPLHb9idmY5YABwikUY6QNcXwyHTbRCkPB2I
                  m+/R4XnmL4cKQ+5Z
                  -----END CERTIFICATE-----
              private_key:
                inline_string: |
                  -----BEGIN PRIVATE KEY-----
                  MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQC2GdiSpVAFf5aD
                  tpBbIIJvzuEFx7WEAWFtEzxj11BOrGgxdmIDafwbTz2FSi6hCkR0mCK1dVLpgwU8
                  VRRZ/ByrFgDKcsrhfmKbbph9rV47wtryxlRL7gGNqjWAXn9UnFISjbJnt8UGjwzg
                  Tjhl6EgihyKkJFL5hl6EWzp2Yeir8wa+HZ4Achr473Gyw+2h8VxAxHyvAEdVsrLj
                  zg7XS00Ki5fjJSTJBeoJHLodG7uRKM6M0pIa2N8b6HkzxuHXRbtmuwnWFaHMG6VJ
                  oxlpRje+HmeCnCzUkBNBVIrUmb92YxFyLCVMrsp/ODEeVJfnwQ2lLvI9MhKQQgJw
                  tteSQoYRAgMBAAECggEAeDGdEkYNCGQLe8pvg8Z0ccoSGpeTxpqGrNEKhjfi6NrB
                  NwyVav10iq4FxEmPd3nobzDPkAftfvWc6hKaCT7vyTkPspCMOsQJ39/ixOk+jqFx
                  lNa1YxyoZ9IV2DIHR1iaj2Z5gB367PZUoGTgstrbafbaNY9IOSyojCIO935ubbcx
                  DWwL24XAf51ez6sXnI8V5tXmrFlNXhbhJdH8iIxNyM45HrnlUlOk0lCK4gmLJjy9
                  10IS2H2Wh3M5zsTpihH1JvM56oAH1ahrhMXs/rVFXXkg50yD1KV+HQiEbglYKUxO
                  eMYtfaY9i2CuLwhDnWp3oxP3HfgQQhD09OEN3e0IlQKBgQDZ/3poG9TiMZSjfKqL
                  xnCABMXGVQsfFWNC8THoW6RRx5Rqi8q08yJrmhCu32YKvccsOljDQJQQJdQO1g09
                  e/adJmCnTrqxNtjPkX9txV23Lp6Ak7emjiQ5ICu7iWxrcO3zf7hmKtj7z+av8sjO
                  mDI7NkX5vnlE74nztBEjp3eC0wKBgQDV2GeJV028RW3b/QyP3Gwmax2+cKLR9PKR
                  nJnmO5bxAT0nQ3xuJEAqMIss/Rfb/macWc2N/6CWJCRT6a2vgy6xBW+bqG6RdQMB
                  xEZXFZl+sSKhXPkc5Wjb4lQ14YWyRPrTjMlwez3k4UolIJhJmwl+D7OkMRrOUERO
                  EtUvc7odCwKBgBi+nhdZKWXveM7B5N3uzXBKmmRz3MpPdC/yDtcwJ8u8msUpTv4R
                  JxQNrd0bsIqBli0YBmFLYEMg+BwjAee7vXeDFq+HCTv6XMva2RsNryCO4yD3I359
                  XfE6DJzB8ZOUgv4Dvluie3TB2Y6ZQV/p+LGt7G13yG4hvofyJYvlg3RPAoGAcjDg
                  +OH5zLN2eqah8qBN0CYa9/rFt0AJ19+7/smLTJ7QvQq4g0gwS1couplcCEnNGWiK
                  72y1n/ckvvplmPeAE19HveMvR9UoCeV5ej86fACy8V/oVpnaaLBvL2aCMjPLjPP9
                  DWeCIZp8MV86cvOrGfngf6kJG2qZTueXl4NAuwkCgYEArKkhlZVXjwBoVvtHYmN2
                  o+F6cGMlRJTLhNc391WApsgDZfTZSdeJsBsvvzS/Nc0burrufJg0wYioTlpReSy4
                  ohhtprnQQAddfjHP7rh2LGt+irFzhdXXQ1ybGaGM9D764KUNCXLuwdly0vzXU4HU
                  q5sGxGrC1RECGB5Zwx2S2ZY=
                  -----END PRIVATE KEY-----

  clusters:
  - name: service1
    type: STRICT_DNS
    lb_policy: ROUND_ROBIN
    load_assignment:
      cluster_name: service1
      endpoints:
      - lb_endpoints:
        - endpoint:
            address:
              socket_address:
                address: service1
                port_value: 8000
  - name: service2
    type: STRICT_DNS
    lb_policy: ROUND_ROBIN
    load_assignment:
      cluster_name: service2
      endpoints:
      - lb_endpoints:
        - endpoint:
            address:
              socket_address:
                address: service2
                port_value: 8000
admin:
  address:
    socket_address:
      address: 0.0.0.0
      port_value: 8001
layered_runtime:
  layers:
  - name: static_layer_0
    static_layer:
      envoy:
        resource_limits:
          listener:
            example_listener_name:
              connection_limit: 10000
```

```
service-envoy.yaml
static_resources:
  listeners:
  - address:
      socket_address:
        address: 0.0.0.0
        port_value: 8000
    filter_chains:
    - filters:
      - name: envoy.filters.network.http_connection_manager
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager
          codec_type: AUTO
          stat_prefix: ingress_http
          route_config:
            name: local_route
            virtual_hosts:
            - name: service
              domains:
              - "*"
              routes:
              - match:
                  prefix: "/service"
                route:
                  cluster: local_service
          http_filters:
          - name: envoy.filters.http.router
  clusters:
  - name: local_service
    type: STRICT_DNS
    lb_policy: ROUND_ROBIN
    load_assignment:
      cluster_name: local_service
      endpoints:
      - lb_endpoints:
        - endpoint:
            address:
              socket_address:
                address: 127.0.0.1
                port_value: 8080
admin:
  address:
    socket_address:
      address: 0.0.0.0
      port_value: 8081
```

```
第一步
envoy/examples/front-proxy
docker-compose build --pull
docker-compose up -d
docker-compose ps

第二步
curl -v localhost:8080/service/1
curl -v localhost:8080/service/2

curl https://localhost:8443/service/1 -k -v

第三步
docker-compose scale service1=3
curl -v localhost:8080/service/1
curl -v localhost:8080/service/1
curl -v localhost:8080/service/1

第四步
docker-compose exec front-envoy /bin/bash

curl localhost:8080/service/1
Hello from behind Envoy (service 1)! hostname: 85ac151715c6 resolvedhostname: 172.19.0.3
curl localhost:8080/service/1
Hello from behind Envoy (service 1)! hostname: 20da22cfc955 resolvedhostname: 172.19.0.5
curl localhost:8080/service/1
Hello from behind Envoy (service 1)! hostname: f26027f1ce28 resolvedhostname: 172.19.0.6
curl localhost:8080/service/2
Hello from behind Envoy (service 2)! hostname: 92f4a3737bbc resolvedhostname: 172.19.0.2、

第五步
docker-compose exec front-envoy /bin/bash
curl localhost:8001/server_info
curl localhost:8001/stats

```



# gRPC bridge

grpc桥接

```
client/envoy-proxy.yaml
static_resources:
  listeners:
  - address:
      socket_address:
        address: 0.0.0.0
        port_value: 9911
    filter_chains:
    - filters:
      - name: envoy.filters.network.http_connection_manager
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager
          codec_type: AUTO
          add_user_agent: true
          access_log:
          - name: envoy.access_loggers.stdout
            typed_config:
              "@type": type.googleapis.com/envoy.extensions.access_loggers.stream.v3.StdoutAccessLog
          stat_prefix: egress_http
          common_http_protocol_options:
            idle_timeout: 0.840s
          use_remote_address: true
          route_config:
            name: local_route
            virtual_hosts:
            - name: backend
              domains:
              - grpc
              routes:
              - match:
                  prefix: "/"
                route:
                  cluster: backend-proxy
          http_filters:
          - name: envoy.filters.http.grpc_http1_bridge
          - name: envoy.filters.http.router
  clusters:
  - name: backend-proxy
    type: LOGICAL_DNS
    dns_lookup_family: V4_ONLY
    lb_policy: ROUND_ROBIN
    http_protocol_options: {}
    load_assignment:
      cluster_name: backend-proxy
      endpoints:
      - lb_endpoints:
        - endpoint:
            address:
              socket_address:
                address: kv-backend-proxy
                port_value: 8811
```

```
server/envoy-proxy.yaml
static_resources:
  listeners:
  - address:
      socket_address:
        address: 0.0.0.0
        port_value: 8811
    filter_chains:
    - filters:
      - name: envoy.filters.network.http_connection_manager
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager
          codec_type: AUTO
          stat_prefix: ingress_http
          access_log:
          - name: envoy.access_loggers.stdout
            typed_config:
              "@type": type.googleapis.com/envoy.extensions.access_loggers.stream.v3.StdoutAccessLog
          route_config:
            name: local_route
            virtual_hosts:
            - name: backend
              domains:
              - "*"
              routes:
              - match:
                  prefix: "/"
                  grpc: {}
                route:
                  cluster: backend_grpc_service
          http_filters:
          - name: envoy.filters.http.router
  clusters:
  - name: backend_grpc_service
    type: STRICT_DNS
    lb_policy: ROUND_ROBIN
    typed_extension_protocol_options:
      envoy.extensions.upstreams.http.v3.HttpProtocolOptions:
        "@type": type.googleapis.com/envoy.extensions.upstreams.http.v3.HttpProtocolOptions
        explicit_http_config:
          http2_protocol_options: {}
    load_assignment:
      cluster_name: backend_grpc_service
      endpoints:
      - lb_endpoints:
        - endpoint:
            address:
              socket_address:
                address: kv-backend-service
                port_value: 8081
```

```
第一步
envoy/examples/grpc-bridge
docker-compose -f docker-compose-protos.yaml up
第二步
envoy/examples/grpc-bridge
docker-compose pull
docker-compose up --build -d
docker-compose ps

第三步
docker-compose exec grpc-client python /client/grpc-kv-client.py set foo bar
docker-compose exec grpc-client python /client/grpc-kv-client.py get foo
docker-compose exec grpc-client python /client/grpc-kv-client.py set foo baz
docker-compose exec grpc-client python /client/grpc-kv-client.py get foo
docker-compose logs grpc-server



```



# Gzip

压缩

```
gzip-envoy.yaml
static_resources:
  listeners:
  - address:
      socket_address:
        address: 0.0.0.0
        port_value: 10000
    filter_chains:
    - filters:
      - name: envoy.filters.network.http_connection_manager
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager
          stat_prefix: ingress_http
          route_config:
            name: local_route
            virtual_hosts:
            - name: backend
              domains:
              - "*"
              routes:
              - match:
                  prefix: "/"
                route:
                  cluster: service
          http_filters:
          - name: envoy.filters.http.compressor
            typed_config:
              "@type": type.googleapis.com/envoy.extensions.filters.http.compressor.v3.Compressor
              response_direction_config:
                common_config:
                  min_content_length: 100
                  content_type:
                  - application/json
                disable_on_etag_header: true
              compressor_library:
                name: text_optimized
                typed_config:
                  "@type": type.googleapis.com/envoy.extensions.compression.gzip.compressor.v3.Gzip
                  memory_level: 3
                  window_bits: 10
          - name: envoy.filters.http.router
  - address:
      socket_address:
        address: 0.0.0.0
        port_value: 9902
    filter_chains:
    - filters:
      - name: envoy.filters.network.http_connection_manager
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager
          stat_prefix: ingress_http
          route_config:
            name: local_route
            virtual_hosts:
            - name: backend
              domains:
              - "*"
              routes:
              - match:
                  prefix: "/stats/prometheus"
                route:
                  cluster: envoy-stats
          http_filters:
          - name: envoy.filters.http.compressor
            typed_config:
              "@type": type.googleapis.com/envoy.extensions.filters.http.compressor.v3.Compressor
              response_direction_config:
                common_config:
                  min_content_length: 100
                  content_type:
                  - text/plain
                disable_on_etag_header: true
              compressor_library:
                name: text_optimized
                typed_config:
                  "@type": type.googleapis.com/envoy.extensions.compression.gzip.compressor.v3.Gzip
                  memory_level: 3
                  window_bits: 10
          - name: envoy.filters.http.router
  clusters:
  - name: envoy-stats
    connect_timeout: 0.25s
    type: STATIC
    load_assignment:
      cluster_name: envoy-stats
      endpoints:
      - lb_endpoints:
        - endpoint:
            address:
              socket_address:
                address: 127.0.0.1
                port_value: 9901
  - name: service
    connect_timeout: 0.25s
    type: STRICT_DNS
    lb_policy: ROUND_ROBIN
    load_assignment:
      cluster_name: service
      endpoints:
      - lb_endpoints:
        - endpoint:
            address:
              socket_address:
                address: service
                port_value: 8080
admin:
  address:
    socket_address:
      address: 0.0.0.0
      port_value: 9901
```

```
第一步
envoy/examples/gzip
docker-compose build --pull
docker-compose up -d
docker-compose ps

第二步
curl -si -H "Accept-Encoding: gzip" localhost:10000/file.json | grep "content-encoding"
curl -si -H "Accept-Encoding: gzip" localhost:10000/file.txt | grep "content-encoding"
第三步
curl -si -H "Accept-Encoding: gzip" localhost:9901/stats/prometheus | grep "content-encoding"
curl -si -H "Accept-Encoding: gzip" localhost:9902/stats/prometheus | grep "content-encoding"

```



# Jaeger native tracing

jaeger原生链路跟踪

```
front-envoy-jaeger.yaml
static_resources:
  listeners:
  - address:
      socket_address:
        address: 0.0.0.0
        port_value: 8000
    traffic_direction: OUTBOUND
    filter_chains:
    - filters:
      - name: envoy.filters.network.http_connection_manager
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager
          generate_request_id: true
          tracing:
            provider:
              name: envoy.tracers.dynamic_ot
              typed_config:
                "@type": type.googleapis.com/envoy.config.trace.v3.DynamicOtConfig
                library: /usr/local/lib/libjaegertracing_plugin.so
                config:
                  service_name: front-proxy
                  sampler:
                    type: const
                    param: 1
                  reporter:
                    localAgentHostPort: jaeger:6831
                  headers:
                    jaegerDebugHeader: jaeger-debug-id
                    jaegerBaggageHeader: jaeger-baggage
                    traceBaggageHeaderPrefix: uberctx-
                  baggage_restrictions:
                    denyBaggageOnInitializationFailure: false
                    hostPort: ""
          codec_type: auto
          stat_prefix: ingress_http
          route_config:
            name: local_route
            virtual_hosts:
            - name: backend
              domains:
              - "*"
              routes:
              - match:
                  prefix: "/"
                route:
                  cluster: service1
                decorator:
                  operation: checkAvailability
          http_filters:
          - name: envoy.filters.http.router
          use_remote_address: true
  clusters:
  - name: service1
    type: strict_dns
    lb_policy: round_robin
    load_assignment:
      cluster_name: service1
      endpoints:
      - lb_endpoints:
        - endpoint:
            address:
              socket_address:
                address: service1
                port_value: 8000
```

```
service1-envoy-jaeger.yaml
static_resources:
  listeners:
  - address:
      socket_address:
        address: 0.0.0.0
        port_value: 8000
    traffic_direction: INBOUND
    filter_chains:
    - filters:
      - name: envoy.filters.network.http_connection_manager
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager
          codec_type: auto
          stat_prefix: ingress_http
          route_config:
            name: service1_route
            virtual_hosts:
            - name: service1
              domains:
              - "*"
              routes:
              - match:
                  prefix: "/"
                route:
                  cluster: local_service
                decorator:
                  operation: checkAvailability
          http_filters:
          - name: envoy.filters.http.router
  - address:
      socket_address:
        address: 0.0.0.0
        port_value: 9000
    traffic_direction: OUTBOUND
    filter_chains:
    - filters:
      - name: envoy.filters.network.http_connection_manager
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager
          tracing:
            provider:
              name: envoy.tracers.dynamic_ot
              typed_config:
                "@type": type.googleapis.com/envoy.config.trace.v3.DynamicOtConfig
                library: /usr/local/lib/libjaegertracing_plugin.so
                config:
                  service_name: service1
                  sampler:
                    type: const
                    param: 1
                  reporter:
                    localAgentHostPort: jaeger:6831
                  headers:
                    jaegerDebugHeader: jaeger-debug-id
                    jaegerBaggageHeader: jaeger-baggage
                    traceBaggageHeaderPrefix: uberctx-
                  baggage_restrictions:
                    denyBaggageOnInitializationFailure: false
                    hostPort: ""
          codec_type: auto
          stat_prefix: egress_http
          route_config:
            name: service2_route
            virtual_hosts:
            - name: service2
              domains:
              - "*"
              routes:
              - match:
                  prefix: "/trace/2"
                route:
                  cluster: service2
                decorator:
                  operation: checkStock
          http_filters:
          - name: envoy.filters.http.router
  clusters:
  - name: local_service
    type: strict_dns
    lb_policy: round_robin
    load_assignment:
      cluster_name: local_service
      endpoints:
      - lb_endpoints:
        - endpoint:
            address:
              socket_address:
                address: 127.0.0.1
                port_value: 8080
  - name: service2
    type: strict_dns
    lb_policy: round_robin
    load_assignment:
      cluster_name: service2
      endpoints:
      - lb_endpoints:
        - endpoint:
            address:
              socket_address:
                address: service2
                port_value: 8000
```

```
service2-envoy-jaeger.yaml 
static_resources:
  listeners:
  - address:
      socket_address:
        address: 0.0.0.0
        port_value: 8000
    traffic_direction: INBOUND
    filter_chains:
    - filters:
      - name: envoy.filters.network.http_connection_manager
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager
          tracing:
            provider:
              name: envoy.tracers.dynamic_ot
              typed_config:
                "@type": type.googleapis.com/envoy.config.trace.v3.DynamicOtConfig
                library: /usr/local/lib/libjaegertracing_plugin.so
                config:
                  service_name: service2
                  sampler:
                    type: const
                    param: 1
                  reporter:
                    localAgentHostPort: jaeger:6831
                  headers:
                    jaegerDebugHeader: jaeger-debug-id
                    jaegerBaggageHeader: jaeger-baggage
                    traceBaggageHeaderPrefix: uberctx-
                  baggage_restrictions:
                    denyBaggageOnInitializationFailure: false
                    hostPort: ""
          codec_type: auto
          stat_prefix: ingress_http
          route_config:
            name: local_route
            virtual_hosts:
            - name: service2
              domains:
              - "*"
              routes:
              - match:
                  prefix: "/"
                route:
                  cluster: local_service
                decorator:
                  operation: checkStock
          http_filters:
          - name: envoy.filters.http.router
  clusters:
  - name: local_service
    type: strict_dns
    lb_policy: round_robin
    load_assignment:
      cluster_name: local_service
      endpoints:
      - lb_endpoints:
        - endpoint:
            address:
              socket_address:
                address: 127.0.0.1
                port_value: 8080
```

```
第一步
envoy/examples/jaeger-native-tracing
docker-compose pull
docker-compose up --build -d
docker-compose ps

第二步
curl -v localhost:8000/trace/1

第三步
 http://192.168.198.154:16686
```



# Jaeger tracing

jaeger链路跟踪

```
front-envoy-jaeger.yaml
static_resources:
  listeners:
  - address:
      socket_address:
        address: 0.0.0.0
        port_value: 8000
    traffic_direction: OUTBOUND
    filter_chains:
    - filters:
      - name: envoy.filters.network.http_connection_manager
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager
          generate_request_id: true
          tracing:
            provider:
              name: envoy.tracers.zipkin
              typed_config:
                "@type": type.googleapis.com/envoy.config.trace.v3.ZipkinConfig
                collector_cluster: jaeger
                collector_endpoint: "/api/v2/spans"
                shared_span_context: false
                collector_endpoint_version: HTTP_JSON
          codec_type: AUTO
          stat_prefix: ingress_http
          route_config:
            name: local_route
            virtual_hosts:
            - name: backend
              domains:
              - "*"
              routes:
              - match:
                  prefix: "/"
                route:
                  cluster: service1
                decorator:
                  operation: checkAvailability
          http_filters:
          - name: envoy.filters.http.router
          use_remote_address: true
  clusters:
  - name: service1
    type: STRICT_DNS
    lb_policy: ROUND_ROBIN
    load_assignment:
      cluster_name: service1
      endpoints:
      - lb_endpoints:
        - endpoint:
            address:
              socket_address:
                address: service1
                port_value: 8000
  - name: jaeger
    type: STRICT_DNS
    lb_policy: ROUND_ROBIN
    load_assignment:
      cluster_name: jaeger
      endpoints:
      - lb_endpoints:
        - endpoint:
            address:
              socket_address:
                address: jaeger
                port_value: 9411
admin:
  address:
    socket_address:
      address: 0.0.0.0
      port_value: 8001
```

```
service1-envoy-jaeger.yaml
static_resources:
  listeners:
  - address:
      socket_address:
        address: 0.0.0.0
        port_value: 8000
    traffic_direction: INBOUND
    filter_chains:
    - filters:
      - name: envoy.filters.network.http_connection_manager
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager
          tracing:
            provider:
              name: envoy.tracers.zipkin
              typed_config:
                "@type": type.googleapis.com/envoy.config.trace.v3.ZipkinConfig
                collector_cluster: jaeger
                collector_endpoint: "/api/v2/spans"
                shared_span_context: false
                collector_endpoint_version: HTTP_JSON
          codec_type: AUTO
          stat_prefix: ingress_http
          route_config:
            name: service1_route
            virtual_hosts:
            - name: service1
              domains:
              - "*"
              routes:
              - match:
                  prefix: "/"
                route:
                  cluster: local_service
                decorator:
                  operation: checkAvailability
          http_filters:
          - name: envoy.filters.http.router
  - address:
      socket_address:
        address: 0.0.0.0
        port_value: 9000
    traffic_direction: OUTBOUND
    filter_chains:
    - filters:
      - name: envoy.filters.network.http_connection_manager
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager
          tracing:
            provider:
              name: envoy.tracers.zipkin
              typed_config:
                "@type": type.googleapis.com/envoy.config.trace.v3.ZipkinConfig
                collector_cluster: jaeger
                collector_endpoint: "/api/v2/spans"
                shared_span_context: false
                collector_endpoint_version: HTTP_JSON
          codec_type: AUTO
          stat_prefix: egress_http
          route_config:
            name: service2_route
            virtual_hosts:
            - name: service2
              domains:
              - "*"
              routes:
              - match:
                  prefix: "/trace/2"
                route:
                  cluster: service2
                decorator:
                  operation: checkStock
          http_filters:
          - name: envoy.filters.http.router
  clusters:
  - name: local_service
    type: STRICT_DNS
    lb_policy: ROUND_ROBIN
    load_assignment:
      cluster_name: local_service
      endpoints:
      - lb_endpoints:
        - endpoint:
            address:
              socket_address:
                address: 127.0.0.1
                port_value: 8080
  - name: service2
    type: STRICT_DNS
    lb_policy: ROUND_ROBIN
    load_assignment:
      cluster_name: service2
      endpoints:
      - lb_endpoints:
        - endpoint:
            address:
              socket_address:
                address: service2
                port_value: 8000
  - name: jaeger
    type: STRICT_DNS
    lb_policy: ROUND_ROBIN
    load_assignment:
      cluster_name: jaeger
      endpoints:
      - lb_endpoints:
        - endpoint:
            address:
              socket_address:
                address: jaeger
                port_value: 9411
admin:
  address:
    socket_address:
      address: 0.0.0.0
      port_value: 8001
```

```
service2-envoy-jaeger.yaml
static_resources:
  listeners:
  - address:
      socket_address:
        address: 0.0.0.0
        port_value: 8000
    traffic_direction: INBOUND
    filter_chains:
    - filters:
      - name: envoy.filters.network.http_connection_manager
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager
          tracing:
            provider:
              name: envoy.tracers.zipkin
              typed_config:
                "@type": type.googleapis.com/envoy.config.trace.v3.ZipkinConfig
                collector_cluster: jaeger
                collector_endpoint: "/api/v2/spans"
                shared_span_context: false
                collector_endpoint_version: HTTP_JSON
          codec_type: AUTO
          stat_prefix: ingress_http
          route_config:
            name: local_route
            virtual_hosts:
            - name: service2
              domains:
              - "*"
              routes:
              - match:
                  prefix: "/"
                route:
                  cluster: local_service
                decorator:
                  operation: checkStock
          http_filters:
          - name: envoy.filters.http.router
  clusters:
  - name: local_service
    type: STRICT_DNS
    lb_policy: ROUND_ROBIN
    load_assignment:
      cluster_name: local_service
      endpoints:
      - lb_endpoints:
        - endpoint:
            address:
              socket_address:
                address: 127.0.0.1
                port_value: 8080
  - name: jaeger
    type: STRICT_DNS
    lb_policy: ROUND_ROBIN
    load_assignment:
      cluster_name: jaeger
      endpoints:
      - lb_endpoints:
        - endpoint:
            address:
              socket_address:
                address: jaeger
                port_value: 9411
admin:
  address:
    socket_address:
      address: 0.0.0.0
      port_value: 8001
```

```
第一步
envoy/examples/jaeger-tracing
docker-compose pull
docker-compose up --build -d
docker-compose ps

第二步
curl -v localhost:8000/trace/1

第三步
http://192.168.198.154:16686 
```



# Load reporting service (LRS)

Load reporting service

```
service-envoy-w-lrs.yaml
static_resources:
  listeners:
  - address:
      socket_address:
        address: 0.0.0.0
        port_value: 80
    filter_chains:
    - filters:
      - name: envoy.filters.network.http_connection_manager
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager
          codec_type: AUTO
          stat_prefix: ingress_http
          route_config:
            name: local_route
            virtual_hosts:
            - name: service
              domains:
              - "*"
              routes:
              - match:
                  prefix: "/service"
                route:
                  cluster: local_service
          http_filters:
          - name: envoy.filters.http.router
  clusters:
  - name: local_service
    type: STRICT_DNS
    lb_policy: ROUND_ROBIN
    load_assignment:
      cluster_name: local_service
      endpoints:
      - lb_endpoints:
        - endpoint:
            address:
              socket_address:
                address: http_service
                port_value: 8082
  - name: load_reporting_cluster
    type: STRICT_DNS
    lb_policy: ROUND_ROBIN
    load_assignment:
      cluster_name: load_reporting_cluster
      endpoints:
      - lb_endpoints:
        - endpoint:
            address:
              socket_address:
                address: lrs_server
                port_value: 18000
cluster_manager:
  load_stats_config:
    api_type: GRPC
    transport_api_version: V3
    grpc_services:
    - envoy_grpc:
        cluster_name: load_reporting_cluster
admin:
  address:
    socket_address:
      address: 0.0.0.0
      port_value: 8081
```

```
第一步
envoy/examples/load-reporting-service
$ docker-compose pull
$ docker-compose up --scale http_service=2
docker-compose ps
第二步
envoy/examples/load_reporting_service
$ bash send_requests.sh

```



# Lua filter

lua过滤器

```
envoy.yaml
static_resources:
  listeners:
  - name: main
    address:
      socket_address:
        address: 0.0.0.0
        port_value: 8000
    filter_chains:
    - filters:
      - name: envoy.filters.network.http_connection_manager
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager
          stat_prefix: ingress_http
          codec_type: AUTO
          route_config:
            name: local_route
            virtual_hosts:
            - name: local_service
              domains:
              - "*"
              routes:
              - match:
                  prefix: "/"
                route:
                  cluster: web_service
          http_filters:
          - name: envoy.filters.http.lua
            typed_config:
              "@type": type.googleapis.com/envoy.extensions.filters.http.lua.v3.Lua
              inline_code: |
                local mylibrary = require("lib.mylibrary")

                function envoy_on_request(request_handle)
                  request_handle:headers():add("foo", mylibrary.foobar())
                end
                function envoy_on_response(response_handle)
                  body_size = response_handle:body():length()
                  response_handle:headers():add("response-body-size", tostring(body_size))
                end
          - name: envoy.filters.http.router

  clusters:
  - name: web_service
    type: STRICT_DNS  # static
    lb_policy: ROUND_ROBIN
    load_assignment:
      cluster_name: web_service
      endpoints:
      - lb_endpoints:
        - endpoint:
            address:
              socket_address:
                address: web_service
                port_value: 80
```

```
第一步
envoy/examples/lua
docker-compose pull
docker-compose up --build -d
docker-compose ps

第二步
curl -v localhost:8000
```



# MySQL filter

mysql过滤器

```
envoy.yaml
static_resources:
  listeners:
  - name: mysql_listener
    address:
      socket_address:
        address: 0.0.0.0
        port_value: 1999
    filter_chains:
    - filters:
      - name: envoy.filters.network.mysql_proxy
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.filters.network.mysql_proxy.v3.MySQLProxy
          stat_prefix: egress_mysql
      - name: envoy.filters.network.tcp_proxy
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.filters.network.tcp_proxy.v3.TcpProxy
          stat_prefix: mysql_tcp
          cluster: mysql_cluster

  clusters:
  - name: mysql_cluster
    type: STRICT_DNS
    load_assignment:
      cluster_name: mysql_cluster
      endpoints:
      - lb_endpoints:
        - endpoint:
            address:
              socket_address:
                address: mysql
                port_value: 3306

admin:
  address:
    socket_address:
      address: 0.0.0.0
      port_value: 8001
```

```
第一步
envoy/examples/mysql
docker-compose pull
docker-compose up --build -d
docker-compose ps

第二步
docker run --rm -it --network envoymesh mysql:5.7 mysql -h proxy -P 1999 -u root --skip-ssl

CREATE DATABASE test;
USE test;
CREATE TABLE test ( text VARCHAR(255) );
SELECT COUNT(*) FROM test;
INSERT INTO test VALUES ('hello, world!');
SELECT COUNT(*) FROM test;

第三步
curl -s "http://localhost:8001/stats?filter=egress_mysql"
curl -s "http://localhost:8001/stats?filter=mysql_tcp"

```



# PostgreSQL filter

PostgreSQL 过滤器

```
envoy.yaml
static_resources:
  listeners:
  - name: postgres_listener
    address:
      socket_address:
        address: 0.0.0.0
        port_value: 1999
    filter_chains:
    - filters:
      - name: envoy.filters.network.postgres_proxy
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.filters.network.postgres_proxy.v3alpha.PostgresProxy
          stat_prefix: egress_postgres
      - name: envoy.filters.network.tcp_proxy
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.filters.network.tcp_proxy.v3.TcpProxy
          stat_prefix: postgres_tcp
          cluster: postgres_cluster

  clusters:
  - name: postgres_cluster
    type: STRICT_DNS
    load_assignment:
      cluster_name: postgres_cluster
      endpoints:
      - lb_endpoints:
        - endpoint:
            address:
              socket_address:
                address: postgres
                port_value: 5432

admin:
  address:
    socket_address:
      address: 0.0.0.0
      port_value: 8001
```

```
第一步
envoy/examples/postgres
docker-compose pull
docker-compose up --build -d
docker-compose ps

第二步
docker run --rm -it --network envoymesh -e PGSSLMODE=disable postgres:latest psql -U postgres -h proxy -p 1999

CREATE DATABASE testdb;
\c testdb
CREATE TABLE tbl ( f SERIAL PRIMARY KEY );
INSERT INTO tbl VALUES (DEFAULT);
 SELECT * FROM tbl;
UPDATE tbl SET f = 2 WHERE f = 1;
INSERT INTO tbl VALUES (DEFAULT);
DELETE FROM tbl;
 INSERT INTO tbl VALUES (DEFAULT);

第三步
curl -s http://localhost:8001/stats?filter=egress_postgres
curl -s http://localhost:8001/stats?filter=postgres_tcp

```



# Redis filter

redis过滤器

```
envoy.yaml
static_resources:
  listeners:
  - name: redis_listener
    address:
      socket_address:
        address: 0.0.0.0
        port_value: 1999
    filter_chains:
    - filters:
      - name: envoy.filters.network.redis_proxy
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.filters.network.redis_proxy.v3.RedisProxy
          stat_prefix: egress_redis
          settings:
            op_timeout: 5s
          prefix_routes:
            catch_all_route:
              cluster: redis_cluster
  clusters:
  - name: redis_cluster
    type: STRICT_DNS  # static
    lb_policy: MAGLEV
    load_assignment:
      cluster_name: redis_cluster
      endpoints:
      - lb_endpoints:
        - endpoint:
            address:
              socket_address:
                address: redis_server
                port_value: 6379
admin:
  address:
    socket_address:
      address: 0.0.0.0
      port_value: 8001
```

```
第一步
envoy/examples/redis
docker-compose pull
docker-compose up --build -d
docker-compose ps

第二步
redis-cli -h localhost -p 1999 set foo foo
redis-cli -h localhost -p 1999 set bar bar
redis-cli -h localhost -p 1999 get foo
redis-cli -h localhost -p 1999 get bar

第三步
http://localhost:8001/stats?usedonly&filter=redis.egress_redis.command

```



# SkyWalking tracing

skyWalking链路跟踪

```
front-envoy-skywalking.yaml 
static_resources:
  listeners:
  - address:
      socket_address:
        address: 0.0.0.0
        port_value: 8000
    traffic_direction: OUTBOUND
    filter_chains:
    - filters:
      - name: envoy.filters.network.http_connection_manager
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager
          generate_request_id: true
          tracing:
            provider:
              name: envoy.tracers.skywalking
              typed_config:
                "@type": type.googleapis.com/envoy.config.trace.v3.SkyWalkingConfig
                grpc_service:
                  envoy_grpc:
                    cluster_name: skywalking
                  timeout: 0.250s
                client_config:
                  service_name: front-envoy
                  instance_name: front-envoy-1
          codec_type: AUTO
          stat_prefix: ingress_http
          route_config:
            name: local_route
            virtual_hosts:
            - name: backend
              domains:
              - "*"
              routes:
              - match:
                  prefix: "/"
                route:
                  cluster: service1
                decorator:
                  operation: checkAvailability
          http_filters:
          - name: envoy.filters.http.router
            typed_config:
              "@type": type.googleapis.com/envoy.extensions.filters.http.router.v3.Router
              start_child_span: true
  clusters:
  - name: service1
    type: STRICT_DNS
    lb_policy: ROUND_ROBIN
    typed_extension_protocol_options:
      envoy.extensions.upstreams.http.v3.HttpProtocolOptions:
        "@type": type.googleapis.com/envoy.extensions.upstreams.http.v3.HttpProtocolOptions
        explicit_http_config:
          http2_protocol_options: {}
    load_assignment:
      cluster_name: service1
      endpoints:
      - lb_endpoints:
        - endpoint:
            address:
              socket_address:
                address: service1
                port_value: 8000
  - name: skywalking
    type: STRICT_DNS
    lb_policy: ROUND_ROBIN
    typed_extension_protocol_options:
      envoy.extensions.upstreams.http.v3.HttpProtocolOptions:
        "@type": type.googleapis.com/envoy.extensions.upstreams.http.v3.HttpProtocolOptions
        explicit_http_config:
          http2_protocol_options: {}
    load_assignment:
      cluster_name: skywalking
      endpoints:
      - lb_endpoints:
        - endpoint:
            address:
              socket_address:
                address: skywalking-oap
                port_value: 11800
admin:
  address:
    socket_address:
      address: 0.0.0.0
      port_value: 8001
```

```
service1-envoy-skywalking.yaml
static_resources:
  listeners:
  - address:
      socket_address:
        address: 0.0.0.0
        port_value: 8000
    traffic_direction: INBOUND
    filter_chains:
    - filters:
      - name: envoy.filters.network.http_connection_manager
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager
          tracing:
            provider:
              name: envoy.tracers.skywalking
              typed_config:
                "@type": type.googleapis.com/envoy.config.trace.v3.SkyWalkingConfig
                grpc_service:
                  envoy_grpc:
                    cluster_name: skywalking
                  timeout: 0.250s
                client_config:
                  service_name: service1-envoy
                  instance_name: service1-envoy-1
          codec_type: AUTO
          stat_prefix: ingress_http
          route_config:
            name: service1_route
            virtual_hosts:
            - name: service1
              domains:
              - "*"
              routes:
              - match:
                  prefix: "/"
                route:
                  cluster: local_service
                decorator:
                  operation: checkAvailability
          http_filters:
          - name: envoy.filters.http.router
            typed_config:
              "@type": type.googleapis.com/envoy.extensions.filters.http.router.v3.Router
              start_child_span: true
  - address:
      socket_address:
        address: 0.0.0.0
        port_value: 9000
    traffic_direction: OUTBOUND
    filter_chains:
    - filters:
      - name: envoy.filters.network.http_connection_manager
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager
          tracing:
            provider:
              name: envoy.tracers.skywalking
              typed_config:
                "@type": type.googleapis.com/envoy.config.trace.v3.SkyWalkingConfig
                grpc_service:
                  envoy_grpc:
                    cluster_name: skywalking
                  timeout: 0.250s
                client_config:
                  service_name: service1-envoy
                  instance_name: service1-envoy-1
          codec_type: AUTO
          stat_prefix: egress_http
          route_config:
            name: service2_route
            virtual_hosts:
            - name: service2
              domains:
              - "*"
              routes:
              - match:
                  prefix: "/trace/2"
                route:
                  cluster: service2
                decorator:
                  operation: checkStock
          http_filters:
          - name: envoy.filters.http.router
            typed_config:
              "@type": type.googleapis.com/envoy.extensions.filters.http.router.v3.Router
              start_child_span: true
  clusters:
  - name: local_service
    type: STRICT_DNS
    lb_policy: ROUND_ROBIN
    load_assignment:
      cluster_name: local_service
      endpoints:
      - lb_endpoints:
        - endpoint:
            address:
              socket_address:
                address: 127.0.0.1
                port_value: 8080
  - name: service2
    type: STRICT_DNS
    lb_policy: ROUND_ROBIN
    typed_extension_protocol_options:
      envoy.extensions.upstreams.http.v3.HttpProtocolOptions:
        "@type": type.googleapis.com/envoy.extensions.upstreams.http.v3.HttpProtocolOptions
        explicit_http_config:
          http2_protocol_options: {}
    load_assignment:
      cluster_name: service2
      endpoints:
      - lb_endpoints:
        - endpoint:
            address:
              socket_address:
                address: service2
                port_value: 8000
  - name: skywalking
    type: STRICT_DNS
    lb_policy: ROUND_ROBIN
    typed_extension_protocol_options:
      envoy.extensions.upstreams.http.v3.HttpProtocolOptions:
        "@type": type.googleapis.com/envoy.extensions.upstreams.http.v3.HttpProtocolOptions
        explicit_http_config:
          http2_protocol_options: {}
    load_assignment:
      cluster_name: skywalking
      endpoints:
      - lb_endpoints:
        - endpoint:
            address:
              socket_address:
                address: skywalking-oap
                port_value: 11800
```

```
service2-envoy-skywalking.yaml
static_resources:
  listeners:
  - address:
      socket_address:
        address: 0.0.0.0
        port_value: 8000
    traffic_direction: INBOUND
    filter_chains:
    - filters:
      - name: envoy.filters.network.http_connection_manager
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager
          tracing:
            provider:
              name: envoy.tracers.skywalking
              typed_config:
                "@type": type.googleapis.com/envoy.config.trace.v3.SkyWalkingConfig
                grpc_service:
                  envoy_grpc:
                    cluster_name: skywalking
                  timeout: 0.250s
                client_config:
                  service_name: service2-envoy
                  instance_name: service2-envoy-1
          codec_type: AUTO
          stat_prefix: ingress_http
          route_config:
            name: local_route
            virtual_hosts:
            - name: service2
              domains:
              - "*"
              routes:
              - match:
                  prefix: "/"
                route:
                  cluster: local_service
                decorator:
                  operation: checkStock
          http_filters:
          - name: envoy.filters.http.router
            typed_config:
              "@type": type.googleapis.com/envoy.extensions.filters.http.router.v3.Router
              start_child_span: true
  clusters:
  - name: local_service
    type: STRICT_DNS
    lb_policy: ROUND_ROBIN
    load_assignment:
      cluster_name: local_service
      endpoints:
      - lb_endpoints:
        - endpoint:
            address:
              socket_address:
                address: 127.0.0.1
                port_value: 8080
  - name: skywalking
    type: STRICT_DNS
    lb_policy: ROUND_ROBIN
    typed_extension_protocol_options:
      envoy.extensions.upstreams.http.v3.HttpProtocolOptions:
        "@type": type.googleapis.com/envoy.extensions.upstreams.http.v3.HttpProtocolOptions
        explicit_http_config:
          http2_protocol_options: {}
    load_assignment:
      cluster_name: skywalking
      endpoints:
      - lb_endpoints:
        - endpoint:
            address:
              socket_address:
                address: skywalking-oap
                port_value: 11800
```

```
第一步
envoy/examples/skywalking-tracing
docker-compose pull
docker-compose up --build -d
docker-compose ps

第二步
curl -v localhost:8000/trace/1
curl -s localhost:8001/stats | grep tracing.skywalking

第三步
http://192.168.198.154:8080
```



# TLS Inspector Listener Filter

tls检查监听过滤器

```
envoy.yaml
admin:
  access_log_path: "/dev/null"
  address:
    socket_address:
      address: 0.0.0.0
      port_value: 12345
static_resources:
  listeners:
  - address:
      socket_address:
        address: 0.0.0.0
        port_value: 10000
    listener_filters:
    - name: "envoy.filters.listener.tls_inspector"
      typed_config:
        "@type": type.googleapis.com/envoy.extensions.filters.listener.tls_inspector.v3.TlsInspector
    filter_chains:
    - filter_chain_match:
        transport_protocol: tls
        application_protocols: [h2]
      filters:
      - name: envoy.filters.network.tcp_proxy
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.filters.network.tcp_proxy.v3.TcpProxy
          cluster: service-https-http2
          stat_prefix: https_passthrough
    - filter_chain_match:
        transport_protocol: tls
        application_protocols: [http/1.1]
      filters:
      - name: envoy.filters.network.tcp_proxy
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.filters.network.tcp_proxy.v3.TcpProxy
          cluster: service-https-http1.1
          stat_prefix: https_passthrough
    - filter_chain_match:
      filters:
      - name: envoy.filters.network.tcp_proxy
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.filters.network.tcp_proxy.v3.TcpProxy
          cluster: service-http
          stat_prefix: ingress_http

  clusters:
  - name: service-https-http2
    type: STRICT_DNS
    lb_policy: ROUND_ROBIN
    load_assignment:
      cluster_name: service-https-http2
      endpoints:
      - lb_endpoints:
        - endpoint:
            address:
              socket_address:
                address: service-https-http2
                port_value: 443
  - name: service-https-http1.1
    type: STRICT_DNS
    lb_policy: ROUND_ROBIN
    load_assignment:
      cluster_name: service-https-http1.1
      endpoints:
      - lb_endpoints:
        - endpoint:
            address:
              socket_address:
                address: service-https-http1.1
                port_value: 443
  - name: service-http
    type: STRICT_DNS
    lb_policy: ROUND_ROBIN
    load_assignment:
      cluster_name: service-http
      endpoints:
      - lb_endpoints:
        - endpoint:
            address:
              socket_address:
                address: service-http
                port_value: 80
```

```
第一步
envoy/examples/tls-inspector
docker-compose pull
docker-compose up --build -d
docker-compose ps

第二步
curl -sk --http1.1 https://localhost:10000  | jq  '.os.hostname'
curl -sk --http2  https://localhost:10000  | jq  '.os.hostname'
curl -sk http://localhost:10000  | jq  '.os.hostname'

第三步
curl -sk http://localhost:12345/stats |grep tls_inspector

```



# TLS Server name indication (SNI)

 This example demonstrates an Envoy proxy that listens on three `TLS` domains on the same `IP` address. 

```
envoy-client.yaml
static_resources:
  listeners:
  - address:
      socket_address:
        address: 0.0.0.0
        port_value: 10000
    filter_chains:
    - filters:
      - name: envoy.filters.network.http_connection_manager
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager
          codec_type: AUTO
          stat_prefix: ingress_http
          route_config:
            name: local_route
            virtual_hosts:
            - name: app
              domains:
              - "*"
              routes:
              - match:
                  prefix: "/domain1"
                route:
                  cluster: proxy-client-domain1
              - match:
                  prefix: "/domain2"
                route:
                  cluster: proxy-client-domain2
              - match:
                  prefix: "/domain3"
                route:
                  cluster: proxy-client-domain3
          http_filters:
          - name: envoy.filters.http.router

  clusters:
  - name: proxy-client-domain1
    type: STRICT_DNS
    lb_policy: ROUND_ROBIN
    load_assignment:
      cluster_name: proxy-client-domain1
      endpoints:
      - lb_endpoints:
        - endpoint:
            address:
              socket_address:
                address: proxy
                port_value: 10000
    transport_socket:
      name: envoy.transport_sockets.tls
      typed_config:
        "@type": type.googleapis.com/envoy.extensions.transport_sockets.tls.v3.UpstreamTlsContext
        sni: domain1.example.com

  - name: proxy-client-domain2
    type: STRICT_DNS
    lb_policy: ROUND_ROBIN
    load_assignment:
      cluster_name: proxy-client-domain2
      endpoints:
      - lb_endpoints:
        - endpoint:
            address:
              socket_address:
                address: proxy
                port_value: 10000
    transport_socket:
      name: envoy.transport_sockets.tls
      typed_config:
        "@type": type.googleapis.com/envoy.extensions.transport_sockets.tls.v3.UpstreamTlsContext
        sni: domain2.example.com

  - name: proxy-client-domain3
    type: STRICT_DNS
    lb_policy: ROUND_ROBIN
    load_assignment:
      cluster_name: proxy-client-domain3
      endpoints:
      - lb_endpoints:
        - endpoint:
            address:
              socket_address:
                address: proxy
                port_value: 10000
    transport_socket:
      name: envoy.transport_sockets.tls
      typed_config:
        "@type": type.googleapis.com/envoy.extensions.transport_sockets.tls.v3.UpstreamTlsContext
        sni: domain3.example.com
```

```
envoy.yaml
static_resources:
  listeners:
  - address:
      socket_address:
        address: 0.0.0.0
        port_value: 10000
    listener_filters:
    - name: "envoy.filters.listener.tls_inspector"
    filter_chains:
    - filter_chain_match:
        server_names:
        - domain1.example.com
      filters:
      - name: envoy.filters.network.http_connection_manager
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager
          codec_type: AUTO
          stat_prefix: ingress_http
          route_config:
            name: local_route
            virtual_hosts:
            - name: app
              domains:
              - "*"
              routes:
              - match:
                  prefix: "/"
                route:
                  cluster: proxy-domain1
          http_filters:
          - name: envoy.filters.http.router
      transport_socket:
        name: envoy.transport_sockets.tls
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.transport_sockets.tls.v3.DownstreamTlsContext
          common_tls_context:
            tls_certificates:
            - certificate_chain:
                filename: certs/domain1.crt.pem
              private_key:
                filename: certs/domain1.key.pem

    - filter_chain_match:
        server_names:
        - domain2.example.com
      filters:
      - name: envoy.filters.network.http_connection_manager
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager
          codec_type: AUTO
          stat_prefix: ingress_http
          route_config:
            name: local_route
            virtual_hosts:
            - name: app
              domains:
              - "*"
              routes:
              - match:
                  prefix: "/"
                route:
                  cluster: proxy-domain2
          http_filters:
          - name: envoy.filters.http.router
      transport_socket:
        name: envoy.transport_sockets.tls
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.transport_sockets.tls.v3.DownstreamTlsContext
          common_tls_context:
            tls_certificates:
            - certificate_chain:
                filename: certs/domain2.crt.pem
              private_key:
                filename: certs/domain2.key.pem

    - filter_chain_match:
        server_names:
        - domain3.example.com
      filters:
      - name: envoy.filters.network.tcp_proxy
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.filters.network.tcp_proxy.v3.TcpProxy
          cluster: proxy-domain3
          stat_prefix: ingress_domain3

  clusters:
  - name: proxy-domain1
    type: STRICT_DNS
    lb_policy: ROUND_ROBIN
    load_assignment:
      cluster_name: proxy-domain1
      endpoints:
      - lb_endpoints:
        - endpoint:
            address:
              socket_address:
                address: http-upstream1
                port_value: 80

  - name: proxy-domain2
    type: STRICT_DNS
    lb_policy: ROUND_ROBIN
    load_assignment:
      cluster_name: proxy-domain2
      endpoints:
      - lb_endpoints:
        - endpoint:
            address:
              socket_address:
                address: http-upstream2
                port_value: 80

  - name: proxy-domain3
    type: STRICT_DNS
    lb_policy: ROUND_ROBIN
    load_assignment:
      cluster_name: proxy-domain3
      endpoints:
      - lb_endpoints:
        - endpoint:
            address:
              socket_address:
                address: https-upstream3
                port_value: 443
```

```
第一步
envoy/examples/tls-sni

mkdir -p certs

openssl req -new -newkey rsa:2048 -days 365 -nodes -x509 \
         -subj "/C=US/ST=CA/O=MyExample, Inc./CN=domain1.example.com" \
         -keyout certs/domain1.key.pem \
         -out certs/domain1.crt.pem

openssl req -new -newkey rsa:2048 -days 365 -nodes -x509 \
         -subj "/C=US/ST=CA/O=MyExample, Inc./CN=domain2.example.com" \
         -keyout certs/domain2.key.pem \
         -out certs/domain2.crt.pem

第二步
envoy/examples/tls-sni
docker-compose build --pull
docker-compose up -d
docker-compose ps

第三步
curl -sk --resolve domain1.example.com:10000:127.0.0.1 \
      https://domain1.example.com:10000 \
     | jq -r '.os.hostname'
curl -sk --resolve domain2.example.com:10000:127.0.0.1 \
      https://domain2.example.com:10000 \
     | jq -r '.os.hostname
curl -sk --resolve domain3.example.com:10000:127.0.0.1 \
      https://domain3.example.com:10000 \
     | jq -r '.os.hostname'

第三步
curl -s http://localhost:20000/domain1 \
     | jq '.os.hostname'
curl -s http://localhost:20000/domain2 \
     | jq '.os.hostname'
curl -s http://localhost:20000/domain3 \
     | jq '.os.hostname'
     
```



# Transport layer security (TLS)

 This example walks through some of the ways that Envoy can be configured to make use of encrypted connections using `HTTP` over `TLS`. 

- `https` -> `http`
- `https` -> `https`
- `http` -> `https`
- `https` passthrough

```
envoy-http-https.yaml
static_resources:
  listeners:
  - address:
      socket_address:
        address: 0.0.0.0
        port_value: 10000
    filter_chains:
    - filters:
      - name: envoy.filters.network.http_connection_manager
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager
          codec_type: AUTO
          stat_prefix: ingress_http
          route_config:
            name: local_route
            virtual_hosts:
            - name: app
              domains:
              - "*"
              routes:
              - match:
                  prefix: "/"
                route:
                  cluster: service-https
          http_filters:
          - name: envoy.filters.http.router

  clusters:
  - name: service-https
    type: STRICT_DNS
    lb_policy: ROUND_ROBIN
    load_assignment:
      cluster_name: service-https
      endpoints:
      - lb_endpoints:
        - endpoint:
            address:
              socket_address:
                address: service-https
                port_value: 443
    transport_socket:
      name: envoy.transport_sockets.tls
      typed_config:
        "@type": type.googleapis.com/envoy.extensions.transport_sockets.tls.v3.UpstreamTlsContext
```

```
envoy-https-https.yaml
static_resources:
  listeners:
  - address:
      socket_address:
        address: 0.0.0.0
        port_value: 10000
    filter_chains:
    - filters:
      - name: envoy.filters.network.http_connection_manager
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager
          codec_type: AUTO
          stat_prefix: ingress_http
          route_config:
            name: local_route
            virtual_hosts:
            - name: app
              domains:
              - "*"
              routes:
              - match:
                  prefix: "/"
                route:
                  cluster: service-https
          http_filters:
          - name: envoy.filters.http.router
      transport_socket:
        name: envoy.transport_sockets.tls
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.transport_sockets.tls.v3.DownstreamTlsContext
          common_tls_context:
            tls_certificates:
            # The following self-signed certificate pair is generated using:
            # $ openssl req -x509 -newkey rsa:2048 -keyout a/front-proxy-key.pem -out  a/front-proxy-crt.pem -days 3650 -nodes -subj '/CN=front-envoy'
            #
            # Instead of feeding it as an inline_string, certificate pair can also be fed to Envoy
            # via filename. Reference: https://www.envoyproxy.io/docs/envoy/latest/api-v3/config/core/v3/base.proto#config-core-v3-datasource.
            #
            # Or in a dynamic configuration scenario, certificate pair can be fetched remotely via
            # Secret Discovery Service (SDS). Reference: https://www.envoyproxy.io/docs/envoy/latest/configuration/security/secret.
            - certificate_chain:
                inline_string: |
                  -----BEGIN CERTIFICATE-----
                  MIICqDCCAZACCQCquzpHNpqBcDANBgkqhkiG9w0BAQsFADAWMRQwEgYDVQQDDAtm
                  cm9udC1lbnZveTAeFw0yMDA3MDgwMTMxNDZaFw0zMDA3MDYwMTMxNDZaMBYxFDAS
                  BgNVBAMMC2Zyb250LWVudm95MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKC
                  AQEAthnYkqVQBX+Wg7aQWyCCb87hBce1hAFhbRM8Y9dQTqxoMXZiA2n8G089hUou
                  oQpEdJgitXVS6YMFPFUUWfwcqxYAynLK4X5im26Yfa1eO8La8sZUS+4Bjao1gF5/
                  VJxSEo2yZ7fFBo8M4E44ZehIIocipCRS+YZehFs6dmHoq/MGvh2eAHIa+O9xssPt
                  ofFcQMR8rwBHVbKy484O10tNCouX4yUkyQXqCRy6HRu7kSjOjNKSGtjfG+h5M8bh
                  10W7ZrsJ1hWhzBulSaMZaUY3vh5ngpws1JATQVSK1Jm/dmMRciwlTK7KfzgxHlSX
                  58ENpS7yPTISkEICcLbXkkKGEQIDAQABMA0GCSqGSIb3DQEBCwUAA4IBAQCmj6Hg
                  vwOxWz0xu+6fSfRL6PGJUGq6wghCfUvjfwZ7zppDUqU47fk+yqPIOzuGZMdAqi7N
                  v1DXkeO4A3hnMD22Rlqt25vfogAaZVToBeQxCPd/ALBLFrvLUFYuSlS3zXSBpQqQ
                  Ny2IKFYsMllz5RSROONHBjaJOn5OwqenJ91MPmTAG7ujXKN6INSBM0PjX9Jy4Xb9
                  zT+I85jRDQHnTFce1WICBDCYidTIvJtdSSokGSuy4/xyxAAc/BpZAfOjBQ4G1QRe
                  9XwOi790LyNUYFJVyeOvNJwveloWuPLHb9idmY5YABwikUY6QNcXwyHTbRCkPB2I
                  m+/R4XnmL4cKQ+5Z
                  -----END CERTIFICATE-----
              private_key:
                inline_string: |
                  -----BEGIN PRIVATE KEY-----
                  MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQC2GdiSpVAFf5aD
                  tpBbIIJvzuEFx7WEAWFtEzxj11BOrGgxdmIDafwbTz2FSi6hCkR0mCK1dVLpgwU8
                  VRRZ/ByrFgDKcsrhfmKbbph9rV47wtryxlRL7gGNqjWAXn9UnFISjbJnt8UGjwzg
                  Tjhl6EgihyKkJFL5hl6EWzp2Yeir8wa+HZ4Achr473Gyw+2h8VxAxHyvAEdVsrLj
                  zg7XS00Ki5fjJSTJBeoJHLodG7uRKM6M0pIa2N8b6HkzxuHXRbtmuwnWFaHMG6VJ
                  oxlpRje+HmeCnCzUkBNBVIrUmb92YxFyLCVMrsp/ODEeVJfnwQ2lLvI9MhKQQgJw
                  tteSQoYRAgMBAAECggEAeDGdEkYNCGQLe8pvg8Z0ccoSGpeTxpqGrNEKhjfi6NrB
                  NwyVav10iq4FxEmPd3nobzDPkAftfvWc6hKaCT7vyTkPspCMOsQJ39/ixOk+jqFx
                  lNa1YxyoZ9IV2DIHR1iaj2Z5gB367PZUoGTgstrbafbaNY9IOSyojCIO935ubbcx
                  DWwL24XAf51ez6sXnI8V5tXmrFlNXhbhJdH8iIxNyM45HrnlUlOk0lCK4gmLJjy9
                  10IS2H2Wh3M5zsTpihH1JvM56oAH1ahrhMXs/rVFXXkg50yD1KV+HQiEbglYKUxO
                  eMYtfaY9i2CuLwhDnWp3oxP3HfgQQhD09OEN3e0IlQKBgQDZ/3poG9TiMZSjfKqL
                  xnCABMXGVQsfFWNC8THoW6RRx5Rqi8q08yJrmhCu32YKvccsOljDQJQQJdQO1g09
                  e/adJmCnTrqxNtjPkX9txV23Lp6Ak7emjiQ5ICu7iWxrcO3zf7hmKtj7z+av8sjO
                  mDI7NkX5vnlE74nztBEjp3eC0wKBgQDV2GeJV028RW3b/QyP3Gwmax2+cKLR9PKR
                  nJnmO5bxAT0nQ3xuJEAqMIss/Rfb/macWc2N/6CWJCRT6a2vgy6xBW+bqG6RdQMB
                  xEZXFZl+sSKhXPkc5Wjb4lQ14YWyRPrTjMlwez3k4UolIJhJmwl+D7OkMRrOUERO
                  EtUvc7odCwKBgBi+nhdZKWXveM7B5N3uzXBKmmRz3MpPdC/yDtcwJ8u8msUpTv4R
                  JxQNrd0bsIqBli0YBmFLYEMg+BwjAee7vXeDFq+HCTv6XMva2RsNryCO4yD3I359
                  XfE6DJzB8ZOUgv4Dvluie3TB2Y6ZQV/p+LGt7G13yG4hvofyJYvlg3RPAoGAcjDg
                  +OH5zLN2eqah8qBN0CYa9/rFt0AJ19+7/smLTJ7QvQq4g0gwS1couplcCEnNGWiK
                  72y1n/ckvvplmPeAE19HveMvR9UoCeV5ej86fACy8V/oVpnaaLBvL2aCMjPLjPP9
                  DWeCIZp8MV86cvOrGfngf6kJG2qZTueXl4NAuwkCgYEArKkhlZVXjwBoVvtHYmN2
                  o+F6cGMlRJTLhNc391WApsgDZfTZSdeJsBsvvzS/Nc0burrufJg0wYioTlpReSy4
                  ohhtprnQQAddfjHP7rh2LGt+irFzhdXXQ1ybGaGM9D764KUNCXLuwdly0vzXU4HU
                  q5sGxGrC1RECGB5Zwx2S2ZY=
                  -----END PRIVATE KEY-----

  clusters:
  - name: service-https
    type: STRICT_DNS
    lb_policy: ROUND_ROBIN
    load_assignment:
      cluster_name: service-https
      endpoints:
      - lb_endpoints:
        - endpoint:
            address:
              socket_address:
                address: service-https
                port_value: 443
    transport_socket:
      name: envoy.transport_sockets.tls
      typed_config:
        "@type": type.googleapis.com/envoy.extensions.transport_sockets.tls.v3.UpstreamTlsContext
```

```
envoy-https-http.yaml 
static_resources:
  listeners:
  - address:
      socket_address:
        address: 0.0.0.0
        port_value: 10000
    filter_chains:
    - filters:
      - name: envoy.filters.network.http_connection_manager
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager
          codec_type: AUTO
          stat_prefix: ingress_http
          route_config:
            name: local_route
            virtual_hosts:
            - name: app
              domains:
              - "*"
              routes:
              - match:
                  prefix: "/"
                route:
                  cluster: service-http
          http_filters:
          - name: envoy.filters.http.router
      transport_socket:
        name: envoy.transport_sockets.tls
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.transport_sockets.tls.v3.DownstreamTlsContext
          common_tls_context:
            tls_certificates:
            # The following self-signed certificate pair is generated using:
            # $ openssl req -x509 -newkey rsa:2048 -keyout a/front-proxy-key.pem -out  a/front-proxy-crt.pem -days 3650 -nodes -subj '/CN=front-envoy'
            #
            # Instead of feeding it as an inline_string, certificate pair can also be fed to Envoy
            # via filename. Reference: https://www.envoyproxy.io/docs/envoy/latest/api-v3/config/core/v3/base.proto#config-core-v3-datasource.
            #
            # Or in a dynamic configuration scenario, certificate pair can be fetched remotely via
            # Secret Discovery Service (SDS). Reference: https://www.envoyproxy.io/docs/envoy/latest/configuration/security/secret.
            - certificate_chain:
                inline_string: |
                  -----BEGIN CERTIFICATE-----
                  MIICqDCCAZACCQCquzpHNpqBcDANBgkqhkiG9w0BAQsFADAWMRQwEgYDVQQDDAtm
                  cm9udC1lbnZveTAeFw0yMDA3MDgwMTMxNDZaFw0zMDA3MDYwMTMxNDZaMBYxFDAS
                  BgNVBAMMC2Zyb250LWVudm95MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKC
                  AQEAthnYkqVQBX+Wg7aQWyCCb87hBce1hAFhbRM8Y9dQTqxoMXZiA2n8G089hUou
                  oQpEdJgitXVS6YMFPFUUWfwcqxYAynLK4X5im26Yfa1eO8La8sZUS+4Bjao1gF5/
                  VJxSEo2yZ7fFBo8M4E44ZehIIocipCRS+YZehFs6dmHoq/MGvh2eAHIa+O9xssPt
                  ofFcQMR8rwBHVbKy484O10tNCouX4yUkyQXqCRy6HRu7kSjOjNKSGtjfG+h5M8bh
                  10W7ZrsJ1hWhzBulSaMZaUY3vh5ngpws1JATQVSK1Jm/dmMRciwlTK7KfzgxHlSX
                  58ENpS7yPTISkEICcLbXkkKGEQIDAQABMA0GCSqGSIb3DQEBCwUAA4IBAQCmj6Hg
                  vwOxWz0xu+6fSfRL6PGJUGq6wghCfUvjfwZ7zppDUqU47fk+yqPIOzuGZMdAqi7N
                  v1DXkeO4A3hnMD22Rlqt25vfogAaZVToBeQxCPd/ALBLFrvLUFYuSlS3zXSBpQqQ
                  Ny2IKFYsMllz5RSROONHBjaJOn5OwqenJ91MPmTAG7ujXKN6INSBM0PjX9Jy4Xb9
                  zT+I85jRDQHnTFce1WICBDCYidTIvJtdSSokGSuy4/xyxAAc/BpZAfOjBQ4G1QRe
                  9XwOi790LyNUYFJVyeOvNJwveloWuPLHb9idmY5YABwikUY6QNcXwyHTbRCkPB2I
                  m+/R4XnmL4cKQ+5Z
                  -----END CERTIFICATE-----
              private_key:
                inline_string: |
                  -----BEGIN PRIVATE KEY-----
                  MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQC2GdiSpVAFf5aD
                  tpBbIIJvzuEFx7WEAWFtEzxj11BOrGgxdmIDafwbTz2FSi6hCkR0mCK1dVLpgwU8
                  VRRZ/ByrFgDKcsrhfmKbbph9rV47wtryxlRL7gGNqjWAXn9UnFISjbJnt8UGjwzg
                  Tjhl6EgihyKkJFL5hl6EWzp2Yeir8wa+HZ4Achr473Gyw+2h8VxAxHyvAEdVsrLj
                  zg7XS00Ki5fjJSTJBeoJHLodG7uRKM6M0pIa2N8b6HkzxuHXRbtmuwnWFaHMG6VJ
                  oxlpRje+HmeCnCzUkBNBVIrUmb92YxFyLCVMrsp/ODEeVJfnwQ2lLvI9MhKQQgJw
                  tteSQoYRAgMBAAECggEAeDGdEkYNCGQLe8pvg8Z0ccoSGpeTxpqGrNEKhjfi6NrB
                  NwyVav10iq4FxEmPd3nobzDPkAftfvWc6hKaCT7vyTkPspCMOsQJ39/ixOk+jqFx
                  lNa1YxyoZ9IV2DIHR1iaj2Z5gB367PZUoGTgstrbafbaNY9IOSyojCIO935ubbcx
                  DWwL24XAf51ez6sXnI8V5tXmrFlNXhbhJdH8iIxNyM45HrnlUlOk0lCK4gmLJjy9
                  10IS2H2Wh3M5zsTpihH1JvM56oAH1ahrhMXs/rVFXXkg50yD1KV+HQiEbglYKUxO
                  eMYtfaY9i2CuLwhDnWp3oxP3HfgQQhD09OEN3e0IlQKBgQDZ/3poG9TiMZSjfKqL
                  xnCABMXGVQsfFWNC8THoW6RRx5Rqi8q08yJrmhCu32YKvccsOljDQJQQJdQO1g09
                  e/adJmCnTrqxNtjPkX9txV23Lp6Ak7emjiQ5ICu7iWxrcO3zf7hmKtj7z+av8sjO
                  mDI7NkX5vnlE74nztBEjp3eC0wKBgQDV2GeJV028RW3b/QyP3Gwmax2+cKLR9PKR
                  nJnmO5bxAT0nQ3xuJEAqMIss/Rfb/macWc2N/6CWJCRT6a2vgy6xBW+bqG6RdQMB
                  xEZXFZl+sSKhXPkc5Wjb4lQ14YWyRPrTjMlwez3k4UolIJhJmwl+D7OkMRrOUERO
                  EtUvc7odCwKBgBi+nhdZKWXveM7B5N3uzXBKmmRz3MpPdC/yDtcwJ8u8msUpTv4R
                  JxQNrd0bsIqBli0YBmFLYEMg+BwjAee7vXeDFq+HCTv6XMva2RsNryCO4yD3I359
                  XfE6DJzB8ZOUgv4Dvluie3TB2Y6ZQV/p+LGt7G13yG4hvofyJYvlg3RPAoGAcjDg
                  +OH5zLN2eqah8qBN0CYa9/rFt0AJ19+7/smLTJ7QvQq4g0gwS1couplcCEnNGWiK
                  72y1n/ckvvplmPeAE19HveMvR9UoCeV5ej86fACy8V/oVpnaaLBvL2aCMjPLjPP9
                  DWeCIZp8MV86cvOrGfngf6kJG2qZTueXl4NAuwkCgYEArKkhlZVXjwBoVvtHYmN2
                  o+F6cGMlRJTLhNc391WApsgDZfTZSdeJsBsvvzS/Nc0burrufJg0wYioTlpReSy4
                  ohhtprnQQAddfjHP7rh2LGt+irFzhdXXQ1ybGaGM9D764KUNCXLuwdly0vzXU4HU
                  q5sGxGrC1RECGB5Zwx2S2ZY=
                  -----END PRIVATE KEY-----

  clusters:
  - name: service-http
    type: STRICT_DNS
    lb_policy: ROUND_ROBIN
    load_assignment:
      cluster_name: service-http
      endpoints:
      - lb_endpoints:
        - endpoint:
            address:
              socket_address:
                address: service-http
                port_value: 80
```

```
envoy-https-passthrough.yaml
static_resources:
  listeners:
  - address:
      socket_address:
        address: 0.0.0.0
        port_value: 10000
    filter_chains:
    - filters:
      - name: envoy.filters.network.tcp_proxy
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.filters.network.tcp_proxy.v3.TcpProxy
          cluster: service-https
          stat_prefix: https_passthrough

  clusters:
  - name: service-https
    type: STRICT_DNS
    lb_policy: ROUND_ROBIN
    load_assignment:
      cluster_name: service-https
      endpoints:
      - lb_endpoints:
        - endpoint:
            address:
              socket_address:
                address: service-https
                port_value: 443
```

```
第一步
envoy/examples/tls
docker-compose pull
docker-compose up --build -d
docker-compose ps

第二步
curl -sk https://localhost:10000  | jq -r '.headers["x-forwarded-proto"]'
curl -sk https://localhost:10000  | jq -r '.os.hostname'

第三步
curl -sk https://localhost:10001  | jq -r '.headers["x-forwarded-proto"]'
curl -sk https://localhost:10001  | jq -r '.os.hostname'

第四步
curl -s http://localhost:10002  | jq -r '.headers["x-forwarded-proto"]'
curl -s http://localhost:10002  | jq -r '.os.hostname'

第五步
curl -sk https://localhost:10003  | jq -r '.headers["x-forwarded-proto"]'
curl -sk https://localhost:10003  | jq -r '.os.hostname'
```



# User Datagram Protocol (UDP)

udp协议

```
envoy.yaml
static_resources:
  listeners:
  - name: listener_0
    reuse_port: true
    address:
      socket_address:
        protocol: UDP
        address: 0.0.0.0
        port_value: 10000
    listener_filters:
    - name: envoy.filters.udp_listener.udp_proxy
      typed_config:
        '@type': type.googleapis.com/envoy.extensions.filters.udp.udp_proxy.v3.UdpProxyConfig
        stat_prefix: service
        cluster: service_udp

  clusters:
  - name: service_udp
    type: STRICT_DNS
    lb_policy: ROUND_ROBIN
    load_assignment:
      cluster_name: service_udp
      endpoints:
      - lb_endpoints:
        - endpoint:
            address:
              socket_address:
                address: service-udp
                port_value: 5005

admin:
  access_log_path: "/dev/null"
  address:
    socket_address:
      address: 0.0.0.0
      port_value: 10001
```

```
第一步
envoy/examples/udp
docker-compose pull
docker-compose up --build -d
docker-compose ps

第二步
echo -n HELO | nc -4u -w1 127.0.0.1 10000
echo -n OLEH | nc -4u -w1 127.0.0.1 10000

第三步
docker-compose logs service-udp

第四步
curl -s http://127.0.0.1:10001/stats | grep udp | grep -v "\: 0"

```



# Wasm C++ filter

wasm c++过滤器

```
envoy.yaml 
static_resources:
  listeners:
  - address:
      socket_address:
        address: 0.0.0.0
        port_value: 8000
    filter_chains:
    - filters:
      - name: envoy.filters.network.http_connection_manager
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager
          codec_type: auto
          stat_prefix: ingress_http
          route_config:
            name: local_route
            virtual_hosts:
            - name: local_service
              domains:
              - "*"
              routes:
              - match:
                  prefix: "/"
                route:
                  cluster: web_service

          http_filters:
          - name: envoy.filters.http.wasm
            typed_config:
              "@type": type.googleapis.com/envoy.extensions.filters.http.wasm.v3.Wasm
              config:
                name: "my_plugin"
                root_id: "my_root_id"
                # if your wasm filter requires custom configuration you can add
                # as follows
                configuration:
                  "@type": "type.googleapis.com/google.protobuf.StringValue"
                  value: |
                    {}
                vm_config:
                  runtime: "envoy.wasm.runtime.v8"
                  vm_id: "my_vm_id"
                  code:
                    local:
                      filename: "lib/envoy_filter_http_wasm_example.wasm"
          - name: envoy.filters.http.router

  clusters:
  - name: web_service
    type: strict_dns
    lb_policy: round_robin
    load_assignment:
      cluster_name: service1
      endpoints:
      - lb_endpoints:
        - endpoint:
            address:
              socket_address:
                address: web_service
                port_value: 9000
```

```
第一步
envoy/examples/wasm-cc
docker-compose build --pull
docker-compose up -d
docker-compose ps

第二步
curl -s http://localhost:8000 | grep "Hello, world"
curl -v http://localhost:8000 | grep "content-type: "

curl -v http://localhost:8000 | grep "x-wasm-custom: "


```



# WebSockets

 This example walks through some of the ways that Envoy can be configured to proxy WebSockets. 

```
envoy-wss-passthrough.yaml
static_resources:
  listeners:
  - address:
      socket_address:
        address: 0.0.0.0
        port_value: 10000
    filter_chains:
    - filters:
      - name: envoy.filters.network.tcp_proxy
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.filters.network.tcp_proxy.v3.TcpProxy
          cluster: service_wss_passthrough
          stat_prefix: wss_passthrough

  clusters:
  - name: service_wss_passthrough
    type: STRICT_DNS
    lb_policy: ROUND_ROBIN
    load_assignment:
      cluster_name: service_wss_passthrough
      endpoints:
      - lb_endpoints:
        - endpoint:
            address:
              socket_address:
                address: service-wss
                port_value: 443
```

```
envoy-wss.yaml
static_resources:
  listeners:
  - address:
      socket_address:
        address: 0.0.0.0
        port_value: 10000
    filter_chains:
    - filters:
      - name: envoy.filters.network.http_connection_manager
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager
          stat_prefix: ingress_wss_to_wss
          upgrade_configs:
          - upgrade_type: websocket
          route_config:
            name: local_route
            virtual_hosts:
            - name: app
              domains:
              - "*"
              routes:
              - match:
                  prefix: "/"
                route:
                  cluster: service_wss
          http_filters:
          - name: envoy.filters.http.router
      transport_socket:
        name: envoy.transport_sockets.tls
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.transport_sockets.tls.v3.DownstreamTlsContext
          common_tls_context:
            tls_certificates:
            # The following self-signed certificate pair is generated using:
            # $ openssl req -x509 -newkey rsa:2048 -keyout a/front-proxy-key.pem -out  a/front-proxy-crt.pem -days 3650 -nodes -subj '/CN=front-envoy'
            #
            # Instead of feeding it as an inline_string, certificate pair can also be fed to Envoy
            # via filename. Reference: https://www.envoyproxy.io/docs/envoy/latest/api-v3/config/core/v3/base.proto#config-core-v3-datasource.
            #
            # Or in a dynamic configuration scenario, certificate pair can be fetched remotely via
            # Secret Discovery Service (SDS). Reference: https://www.envoyproxy.io/docs/envoy/latest/configuration/security/secret.
            - certificate_chain:
                inline_string: |
                  -----BEGIN CERTIFICATE-----
                  MIICqDCCAZACCQCquzpHNpqBcDANBgkqhkiG9w0BAQsFADAWMRQwEgYDVQQDDAtm
                  cm9udC1lbnZveTAeFw0yMDA3MDgwMTMxNDZaFw0zMDA3MDYwMTMxNDZaMBYxFDAS
                  BgNVBAMMC2Zyb250LWVudm95MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKC
                  AQEAthnYkqVQBX+Wg7aQWyCCb87hBce1hAFhbRM8Y9dQTqxoMXZiA2n8G089hUou
                  oQpEdJgitXVS6YMFPFUUWfwcqxYAynLK4X5im26Yfa1eO8La8sZUS+4Bjao1gF5/
                  VJxSEo2yZ7fFBo8M4E44ZehIIocipCRS+YZehFs6dmHoq/MGvh2eAHIa+O9xssPt
                  ofFcQMR8rwBHVbKy484O10tNCouX4yUkyQXqCRy6HRu7kSjOjNKSGtjfG+h5M8bh
                  10W7ZrsJ1hWhzBulSaMZaUY3vh5ngpws1JATQVSK1Jm/dmMRciwlTK7KfzgxHlSX
                  58ENpS7yPTISkEICcLbXkkKGEQIDAQABMA0GCSqGSIb3DQEBCwUAA4IBAQCmj6Hg
                  vwOxWz0xu+6fSfRL6PGJUGq6wghCfUvjfwZ7zppDUqU47fk+yqPIOzuGZMdAqi7N
                  v1DXkeO4A3hnMD22Rlqt25vfogAaZVToBeQxCPd/ALBLFrvLUFYuSlS3zXSBpQqQ
                  Ny2IKFYsMllz5RSROONHBjaJOn5OwqenJ91MPmTAG7ujXKN6INSBM0PjX9Jy4Xb9
                  zT+I85jRDQHnTFce1WICBDCYidTIvJtdSSokGSuy4/xyxAAc/BpZAfOjBQ4G1QRe
                  9XwOi790LyNUYFJVyeOvNJwveloWuPLHb9idmY5YABwikUY6QNcXwyHTbRCkPB2I
                  m+/R4XnmL4cKQ+5Z
                  -----END CERTIFICATE-----
              private_key:
                inline_string: |
                  -----BEGIN PRIVATE KEY-----
                  MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQC2GdiSpVAFf5aD
                  tpBbIIJvzuEFx7WEAWFtEzxj11BOrGgxdmIDafwbTz2FSi6hCkR0mCK1dVLpgwU8
                  VRRZ/ByrFgDKcsrhfmKbbph9rV47wtryxlRL7gGNqjWAXn9UnFISjbJnt8UGjwzg
                  Tjhl6EgihyKkJFL5hl6EWzp2Yeir8wa+HZ4Achr473Gyw+2h8VxAxHyvAEdVsrLj
                  zg7XS00Ki5fjJSTJBeoJHLodG7uRKM6M0pIa2N8b6HkzxuHXRbtmuwnWFaHMG6VJ
                  oxlpRje+HmeCnCzUkBNBVIrUmb92YxFyLCVMrsp/ODEeVJfnwQ2lLvI9MhKQQgJw
                  tteSQoYRAgMBAAECggEAeDGdEkYNCGQLe8pvg8Z0ccoSGpeTxpqGrNEKhjfi6NrB
                  NwyVav10iq4FxEmPd3nobzDPkAftfvWc6hKaCT7vyTkPspCMOsQJ39/ixOk+jqFx
                  lNa1YxyoZ9IV2DIHR1iaj2Z5gB367PZUoGTgstrbafbaNY9IOSyojCIO935ubbcx
                  DWwL24XAf51ez6sXnI8V5tXmrFlNXhbhJdH8iIxNyM45HrnlUlOk0lCK4gmLJjy9
                  10IS2H2Wh3M5zsTpihH1JvM56oAH1ahrhMXs/rVFXXkg50yD1KV+HQiEbglYKUxO
                  eMYtfaY9i2CuLwhDnWp3oxP3HfgQQhD09OEN3e0IlQKBgQDZ/3poG9TiMZSjfKqL
                  xnCABMXGVQsfFWNC8THoW6RRx5Rqi8q08yJrmhCu32YKvccsOljDQJQQJdQO1g09
                  e/adJmCnTrqxNtjPkX9txV23Lp6Ak7emjiQ5ICu7iWxrcO3zf7hmKtj7z+av8sjO
                  mDI7NkX5vnlE74nztBEjp3eC0wKBgQDV2GeJV028RW3b/QyP3Gwmax2+cKLR9PKR
                  nJnmO5bxAT0nQ3xuJEAqMIss/Rfb/macWc2N/6CWJCRT6a2vgy6xBW+bqG6RdQMB
                  xEZXFZl+sSKhXPkc5Wjb4lQ14YWyRPrTjMlwez3k4UolIJhJmwl+D7OkMRrOUERO
                  EtUvc7odCwKBgBi+nhdZKWXveM7B5N3uzXBKmmRz3MpPdC/yDtcwJ8u8msUpTv4R
                  JxQNrd0bsIqBli0YBmFLYEMg+BwjAee7vXeDFq+HCTv6XMva2RsNryCO4yD3I359
                  XfE6DJzB8ZOUgv4Dvluie3TB2Y6ZQV/p+LGt7G13yG4hvofyJYvlg3RPAoGAcjDg
                  +OH5zLN2eqah8qBN0CYa9/rFt0AJ19+7/smLTJ7QvQq4g0gwS1couplcCEnNGWiK
                  72y1n/ckvvplmPeAE19HveMvR9UoCeV5ej86fACy8V/oVpnaaLBvL2aCMjPLjPP9
                  DWeCIZp8MV86cvOrGfngf6kJG2qZTueXl4NAuwkCgYEArKkhlZVXjwBoVvtHYmN2
                  o+F6cGMlRJTLhNc391WApsgDZfTZSdeJsBsvvzS/Nc0burrufJg0wYioTlpReSy4
                  ohhtprnQQAddfjHP7rh2LGt+irFzhdXXQ1ybGaGM9D764KUNCXLuwdly0vzXU4HU
                  q5sGxGrC1RECGB5Zwx2S2ZY=
                  -----END PRIVATE KEY-----

  clusters:
  - name: service_wss
    type: STRICT_DNS
    lb_policy: ROUND_ROBIN
    load_assignment:
      cluster_name: service_wss
      endpoints:
      - lb_endpoints:
        - endpoint:
            address:
              socket_address:
                address: service-wss
                port_value: 443
    transport_socket:
      name: envoy.transport_sockets.tls
      typed_config:
        "@type": type.googleapis.com/envoy.extensions.transport_sockets.tls.v3.UpstreamTlsContext
```

```
envoy-ws.yaml
static_resources:
  listeners:
  - address:
      socket_address:
        address: 0.0.0.0
        port_value: 10000
    filter_chains:
    - filters:
      - name: envoy.filters.network.http_connection_manager
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager
          stat_prefix: ingress_ws_to_ws
          upgrade_configs:
          - upgrade_type: websocket
          route_config:
            name: local_route
            virtual_hosts:
            - name: app
              domains:
              - "*"
              routes:
              - match:
                  prefix: "/"
                route:
                  cluster: service_ws
          http_filters:
          - name: envoy.filters.http.router

  clusters:
  - name: service_ws
    type: STRICT_DNS
    lb_policy: ROUND_ROBIN
    load_assignment:
      cluster_name: service_ws
      endpoints:
      - lb_endpoints:
        - endpoint:
            address:
              socket_address:
                address: service-ws
                port_value: 80
```

```
第一步
envoy/examples/websocket
mkdir -p certs
openssl req -batch -new -x509 -nodes -keyout certs/key.pem -out certs/cert.pem
openssl pkcs12 -export -passout pass: -out certs/output.pkcs12 -inkey certs/key.pem -in certs/cert.pem

第二步
docker-compose pull
docker-compose up --build -d
docker-compose ps

第三步
docker run -ti --network=host solsson/websocat ws://localhost:10000

第四步
docker run -ti --network=host solsson/websocat --insecure wss://localhost:20000

第五步
docker run -ti --network=host solsson/websocat --insecure wss://localhost:30000

```



# Windows based Front proxy

windows平台运行

```
front-envoy.yaml
static_resources:
  listeners:
  - address:
      socket_address:
        address: 0.0.0.0
        port_value: 8080
    filter_chains:
    - filters:
      - name: envoy.filters.network.http_connection_manager
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager
          codec_type: AUTO
          stat_prefix: ingress_http
          route_config:
            name: local_route
            virtual_hosts:
            - name: backend
              domains:
              - "*"
              routes:
              - match:
                  prefix: "/service/1"
                route:
                  cluster: service1
              - match:
                  prefix: "/service/2"
                route:
                  cluster: service2
          http_filters:
          - name: envoy.filters.http.router

  - address:
      socket_address:
        address: 0.0.0.0
        port_value: 8443
    filter_chains:
    - filters:
      - name: envoy.filters.network.http_connection_manager
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager
          codec_type: AUTO
          stat_prefix: ingress_http
          route_config:
            name: local_route
            virtual_hosts:
            - name: backend
              domains:
              - "*"
              routes:
              - match:
                  prefix: "/service/1"
                route:
                  cluster: service1
              - match:
                  prefix: "/service/2"
                route:
                  cluster: service2
          http_filters:
          - name: envoy.filters.http.router

      transport_socket:
        name: envoy.transport_sockets.tls
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.transport_sockets.tls.v3.DownstreamTlsContext
          common_tls_context:
            tls_certificates:
            # The following self-signed certificate pair is generated using:
            # $ openssl req -x509 -newkey rsa:2048 -keyout a/front-proxy-key.pem -out  a/front-proxy-crt.pem -days 3650 -nodes -subj '/CN=front-envoy'
            #
            # Instead of feeding it as an inline_string, certificate pair can also be fed to Envoy
            # via filename. Reference: https://www.envoyproxy.io/docs/envoy/latest/api-v3/config/core/v3/base.proto#config-core-v3-datasource.
            #
            # Or in a dynamic configuration scenario, certificate pair can be fetched remotely via
            # Secret Discovery Service (SDS). Reference: https://www.envoyproxy.io/docs/envoy/latest/configuration/security/secret.
            - certificate_chain:
                inline_string: |
                  -----BEGIN CERTIFICATE-----
                  MIICqDCCAZACCQCquzpHNpqBcDANBgkqhkiG9w0BAQsFADAWMRQwEgYDVQQDDAtm
                  cm9udC1lbnZveTAeFw0yMDA3MDgwMTMxNDZaFw0zMDA3MDYwMTMxNDZaMBYxFDAS
                  BgNVBAMMC2Zyb250LWVudm95MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKC
                  AQEAthnYkqVQBX+Wg7aQWyCCb87hBce1hAFhbRM8Y9dQTqxoMXZiA2n8G089hUou
                  oQpEdJgitXVS6YMFPFUUWfwcqxYAynLK4X5im26Yfa1eO8La8sZUS+4Bjao1gF5/
                  VJxSEo2yZ7fFBo8M4E44ZehIIocipCRS+YZehFs6dmHoq/MGvh2eAHIa+O9xssPt
                  ofFcQMR8rwBHVbKy484O10tNCouX4yUkyQXqCRy6HRu7kSjOjNKSGtjfG+h5M8bh
                  10W7ZrsJ1hWhzBulSaMZaUY3vh5ngpws1JATQVSK1Jm/dmMRciwlTK7KfzgxHlSX
                  58ENpS7yPTISkEICcLbXkkKGEQIDAQABMA0GCSqGSIb3DQEBCwUAA4IBAQCmj6Hg
                  vwOxWz0xu+6fSfRL6PGJUGq6wghCfUvjfwZ7zppDUqU47fk+yqPIOzuGZMdAqi7N
                  v1DXkeO4A3hnMD22Rlqt25vfogAaZVToBeQxCPd/ALBLFrvLUFYuSlS3zXSBpQqQ
                  Ny2IKFYsMllz5RSROONHBjaJOn5OwqenJ91MPmTAG7ujXKN6INSBM0PjX9Jy4Xb9
                  zT+I85jRDQHnTFce1WICBDCYidTIvJtdSSokGSuy4/xyxAAc/BpZAfOjBQ4G1QRe
                  9XwOi790LyNUYFJVyeOvNJwveloWuPLHb9idmY5YABwikUY6QNcXwyHTbRCkPB2I
                  m+/R4XnmL4cKQ+5Z
                  -----END CERTIFICATE-----
              private_key:
                inline_string: |
                  -----BEGIN PRIVATE KEY-----
                  MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQC2GdiSpVAFf5aD
                  tpBbIIJvzuEFx7WEAWFtEzxj11BOrGgxdmIDafwbTz2FSi6hCkR0mCK1dVLpgwU8
                  VRRZ/ByrFgDKcsrhfmKbbph9rV47wtryxlRL7gGNqjWAXn9UnFISjbJnt8UGjwzg
                  Tjhl6EgihyKkJFL5hl6EWzp2Yeir8wa+HZ4Achr473Gyw+2h8VxAxHyvAEdVsrLj
                  zg7XS00Ki5fjJSTJBeoJHLodG7uRKM6M0pIa2N8b6HkzxuHXRbtmuwnWFaHMG6VJ
                  oxlpRje+HmeCnCzUkBNBVIrUmb92YxFyLCVMrsp/ODEeVJfnwQ2lLvI9MhKQQgJw
                  tteSQoYRAgMBAAECggEAeDGdEkYNCGQLe8pvg8Z0ccoSGpeTxpqGrNEKhjfi6NrB
                  NwyVav10iq4FxEmPd3nobzDPkAftfvWc6hKaCT7vyTkPspCMOsQJ39/ixOk+jqFx
                  lNa1YxyoZ9IV2DIHR1iaj2Z5gB367PZUoGTgstrbafbaNY9IOSyojCIO935ubbcx
                  DWwL24XAf51ez6sXnI8V5tXmrFlNXhbhJdH8iIxNyM45HrnlUlOk0lCK4gmLJjy9
                  10IS2H2Wh3M5zsTpihH1JvM56oAH1ahrhMXs/rVFXXkg50yD1KV+HQiEbglYKUxO
                  eMYtfaY9i2CuLwhDnWp3oxP3HfgQQhD09OEN3e0IlQKBgQDZ/3poG9TiMZSjfKqL
                  xnCABMXGVQsfFWNC8THoW6RRx5Rqi8q08yJrmhCu32YKvccsOljDQJQQJdQO1g09
                  e/adJmCnTrqxNtjPkX9txV23Lp6Ak7emjiQ5ICu7iWxrcO3zf7hmKtj7z+av8sjO
                  mDI7NkX5vnlE74nztBEjp3eC0wKBgQDV2GeJV028RW3b/QyP3Gwmax2+cKLR9PKR
                  nJnmO5bxAT0nQ3xuJEAqMIss/Rfb/macWc2N/6CWJCRT6a2vgy6xBW+bqG6RdQMB
                  xEZXFZl+sSKhXPkc5Wjb4lQ14YWyRPrTjMlwez3k4UolIJhJmwl+D7OkMRrOUERO
                  EtUvc7odCwKBgBi+nhdZKWXveM7B5N3uzXBKmmRz3MpPdC/yDtcwJ8u8msUpTv4R
                  JxQNrd0bsIqBli0YBmFLYEMg+BwjAee7vXeDFq+HCTv6XMva2RsNryCO4yD3I359
                  XfE6DJzB8ZOUgv4Dvluie3TB2Y6ZQV/p+LGt7G13yG4hvofyJYvlg3RPAoGAcjDg
                  +OH5zLN2eqah8qBN0CYa9/rFt0AJ19+7/smLTJ7QvQq4g0gwS1couplcCEnNGWiK
                  72y1n/ckvvplmPeAE19HveMvR9UoCeV5ej86fACy8V/oVpnaaLBvL2aCMjPLjPP9
                  DWeCIZp8MV86cvOrGfngf6kJG2qZTueXl4NAuwkCgYEArKkhlZVXjwBoVvtHYmN2
                  o+F6cGMlRJTLhNc391WApsgDZfTZSdeJsBsvvzS/Nc0burrufJg0wYioTlpReSy4
                  ohhtprnQQAddfjHP7rh2LGt+irFzhdXXQ1ybGaGM9D764KUNCXLuwdly0vzXU4HU
                  q5sGxGrC1RECGB5Zwx2S2ZY=
                  -----END PRIVATE KEY-----

  clusters:
  - name: service1
    connect_timeout: 0.25s
    type: STRICT_DNS
    lb_policy: ROUND_ROBIN
    load_assignment:
      cluster_name: service1
      endpoints:
      - lb_endpoints:
        - endpoint:
            address:
              socket_address:
                address: service1
                port_value: 8000
  - name: service2
    connect_timeout: 0.25s
    type: STRICT_DNS
    lb_policy: ROUND_ROBIN
    load_assignment:
      cluster_name: service2
      endpoints:
      - lb_endpoints:
        - endpoint:
            address:
              socket_address:
                address: service2
                port_value: 8000
admin:
  address:
    socket_address:
      address: 0.0.0.0
      port_value: 8001
layered_runtime:
  layers:
  - name: static_layer_0
    static_layer:
      envoy:
        resource_limits:
          listener:
            example_listener_name:
              connection_limit: 10000
```

```
service-envoy.yaml
static_resources:
  listeners:
  - address:
      socket_address:
        address: 0.0.0.0
        port_value: 8000
    filter_chains:
    - filters:
      - name: envoy.filters.network.http_connection_manager
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager
          codec_type: AUTO
          stat_prefix: ingress_http
          route_config:
            name: local_route
            virtual_hosts:
            - name: service
              domains:
              - "*"
              routes:
              - match:
                  prefix: "/service"
                route:
                  cluster: local_service
          http_filters:
          - name: envoy.filters.http.router
  clusters:
  - name: local_service
    connect_timeout: 0.25s
    type: STRICT_DNS
    lb_policy: ROUND_ROBIN
    load_assignment:
      cluster_name: local_service
      endpoints:
      - lb_endpoints:
        - endpoint:
            address:
              socket_address:
                address: 127.0.0.1
                port_value: 8080
admin:
  address:
    socket_address:
      address: 0.0.0.0
      port_value: 8081
```



# Zipkin tracing

zipkin链路跟踪

```
front-envoy-zipkin.yaml
static_resources:
  listeners:
  - address:
      socket_address:
        address: 0.0.0.0
        port_value: 8000
    traffic_direction: OUTBOUND
    filter_chains:
    - filters:
      - name: envoy.filters.network.http_connection_manager
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager
          generate_request_id: true
          tracing:
            provider:
              name: envoy.tracers.zipkin
              typed_config:
                "@type": type.googleapis.com/envoy.config.trace.v3.ZipkinConfig
                collector_cluster: zipkin
                collector_endpoint: "/api/v2/spans"
                collector_endpoint_version: HTTP_JSON
          codec_type: AUTO
          stat_prefix: ingress_http
          route_config:
            name: local_route
            virtual_hosts:
            - name: backend
              domains:
              - "*"
              routes:
              - match:
                  prefix: "/"
                route:
                  cluster: service1
                decorator:
                  operation: checkAvailability
              response_headers_to_add:
              - header:
                  key: "x-b3-traceid"
                  value: "%REQ(x-b3-traceid)%"
              - header:
                  key: "x-request-id"
                  value: "%REQ(x-request-id)%"
          http_filters:
          - name: envoy.filters.http.router
  clusters:
  - name: service1
    type: STRICT_DNS
    lb_policy: ROUND_ROBIN
    load_assignment:
      cluster_name: service1
      endpoints:
      - lb_endpoints:
        - endpoint:
            address:
              socket_address:
                address: service1
                port_value: 8000
  - name: zipkin
    type: STRICT_DNS
    lb_policy: ROUND_ROBIN
    load_assignment:
      cluster_name: zipkin
      endpoints:
      - lb_endpoints:
        - endpoint:
            address:
              socket_address:
                address: zipkin
                port_value: 9411
```

```
service1-envoy-zipkin.yaml
static_resources:
  listeners:
  - address:
      socket_address:
        address: 0.0.0.0
        port_value: 8000
    traffic_direction: INBOUND
    filter_chains:
    - filters:
      - name: envoy.filters.network.http_connection_manager
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager
          tracing:
            provider:
              name: envoy.tracers.zipkin
              typed_config:
                "@type": type.googleapis.com/envoy.config.trace.v3.ZipkinConfig
                collector_cluster: zipkin
                collector_endpoint: "/api/v2/spans"
                collector_endpoint_version: HTTP_JSON
          codec_type: AUTO
          stat_prefix: ingress_http
          route_config:
            name: service1_route
            virtual_hosts:
            - name: service1
              domains:
              - "*"
              routes:
              - match:
                  prefix: "/"
                route:
                  cluster: local_service
                decorator:
                  operation: checkAvailability
          http_filters:
          - name: envoy.filters.http.router
  - address:
      socket_address:
        address: 0.0.0.0
        port_value: 9000
    traffic_direction: OUTBOUND
    filter_chains:
    - filters:
      - name: envoy.filters.network.http_connection_manager
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager
          tracing:
            provider:
              name: envoy.tracers.zipkin
              typed_config:
                "@type": type.googleapis.com/envoy.config.trace.v3.ZipkinConfig
                collector_cluster: zipkin
                collector_endpoint: "/api/v2/spans"
                collector_endpoint_version: HTTP_JSON
          codec_type: AUTO
          stat_prefix: egress_http
          route_config:
            name: service2_route
            virtual_hosts:
            - name: service2
              domains:
              - "*"
              routes:
              - match:
                  prefix: "/trace/2"
                route:
                  cluster: service2
                decorator:
                  operation: checkStock
          http_filters:
          - name: envoy.filters.http.router
  clusters:
  - name: local_service
    type: STRICT_DNS
    lb_policy: ROUND_ROBIN
    load_assignment:
      cluster_name: local_service
      endpoints:
      - lb_endpoints:
        - endpoint:
            address:
              socket_address:
                address: 127.0.0.1
                port_value: 8080
  - name: service2
    type: STRICT_DNS
    lb_policy: ROUND_ROBIN
    load_assignment:
      cluster_name: service2
      endpoints:
      - lb_endpoints:
        - endpoint:
            address:
              socket_address:
                address: service2
                port_value: 8000
  - name: zipkin
    type: STRICT_DNS
    lb_policy: ROUND_ROBIN
    load_assignment:
      cluster_name: zipkin
      endpoints:
      - lb_endpoints:
        - endpoint:
            address:
              socket_address:
                address: zipkin
                port_value: 9411
admin:
  address:
    socket_address:
      protocol: TCP
      address: 0.0.0.0
      port_value: 19000
```

```
 service2-envoy-zipkin.yaml 
static_resources:
  listeners:
  - address:
      socket_address:
        address: 0.0.0.0
        port_value: 8000
    traffic_direction: INBOUND
    filter_chains:
    - filters:
      - name: envoy.filters.network.http_connection_manager
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager
          tracing:
            provider:
              name: envoy.tracers.zipkin
              typed_config:
                "@type": type.googleapis.com/envoy.config.trace.v3.ZipkinConfig
                collector_cluster: zipkin
                collector_endpoint: "/api/v2/spans"
                collector_endpoint_version: HTTP_JSON
          codec_type: AUTO
          stat_prefix: ingress_http
          route_config:
            name: local_route
            virtual_hosts:
            - name: service2
              domains:
              - "*"
              routes:
              - match:
                  prefix: "/"
                route:
                  cluster: local_service
                decorator:
                  operation: checkStock
          http_filters:
          - name: envoy.filters.http.router
  clusters:
  - name: local_service
    type: STRICT_DNS
    lb_policy: ROUND_ROBIN
    load_assignment:
      cluster_name: local_service
      endpoints:
      - lb_endpoints:
        - endpoint:
            address:
              socket_address:
                address: 127.0.0.1
                port_value: 8080
  - name: zipkin
    type: STRICT_DNS
    lb_policy: ROUND_ROBIN
    load_assignment:
      cluster_name: zipkin
      endpoints:
      - lb_endpoints:
        - endpoint:
            address:
              socket_address:
                address: zipkin
                port_value: 9411
```

```
第一步
envoy/examples/zipkin-tracing
docker-compose pull
docker-compose up --build -d
docker-compose ps

第二步
curl -v localhost:8000/trace/1

第三步
 http://192.168.198.154:9411 
```

