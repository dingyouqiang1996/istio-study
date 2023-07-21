# 1使用场景

1gateway网关

用户浏览器访问网页时，在gateway网关配置压缩，减少传输数据，加快网页打开速度。

2mesh内部

微服务相互通信时，特别是用了rest协议，即用http协议通信，配置压缩和解压，可以有效加快数据传输速度，减少网路延迟

# 2实操

## 2.1网关配置压缩

### 2.1.1示例1

```
cat << EOF > ef-ingressgateway-http-filter-compression.yaml 
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  namespace: istio-system
  name: apply-to
spec:
  workloadSelector:
    labels:
      istio: ingressgateway
  configPatches:
    - applyTo: HTTP_FILTER
      match:
        context: GATEWAY
        listener:
          filterChain:
            filter:
              name: envoy.filters.network.http_connection_manager
              subFilter:
                name: envoy.filters.http.router
      patch:
        operation: INSERT_BEFORE
        value:
          name: envoy.filters.http.compressor
          typed_config:
            "@type": type.googleapis.com/envoy.extensions.filters.http.compressor.v3.Compressor
            response_direction_config:
              common_config:
                min_content_length: 100
                content_type:
                - 'text/html'
            compressor_library:
              name: text_optimized
              typed_config:
                "@type": type.googleapis.com/envoy.extensions.compression.gzip.compressor.v3.Gzip
                memory_level: 3
                window_bits: 10
                compression_level: BEST_COMPRESSION
                compression_strategy: DEFAULT_STRATEGY
EOF

kubectl apply -f ef-ingressgateway-http-filter-compression.yaml  -n istio-system
```

配置参数说明：

作用在http_filter上，type_url是固定的。response_direction_config对响应做配置，min_content_length最小启用压缩大小，content_type对哪些类型启用压缩。compressor_library压缩库配置，

window_bits：

窗口位大小，值从9到15，大的值会有更好的压缩，但内存消耗更大，默认是12，将产生4096字节窗口

compression_level

压缩级别，将影响压缩速度和压缩大小。BEST,高压缩，高延迟；SPEED低压缩，低延迟；DEFAULT优化的压缩，将介于BEST和SPEED之间。默认没设置是DEFAULT.

memory_level

内存级别，从1到9，控制压缩库内存的使用量，值越高内存用的多，但是更快，压缩结果更好。默认值是5.

compression_strategy:

 **DEFAULT** , **FILTERED** , **HUFFMAN** , **RLE** 

content_type:

默认值 “application/javascript”, “application/json”, “application/xhtml+xml”, “image/svg+xml”, “text/css”, “text/html”, “text/plain”, “text/xml”  



没启用压缩前：



![3](10image\3.jpg)



传输大小是4.6k



启用压缩后：

![1](10image\1.jpg)

![2](10image\4.jpg)



content-encoding为gzip，说明启用了gzip压缩

大小由4.6k降到了1.9k



### 2.1.2提高压缩参数

```
cat << EOF > ef-ingressgateway-http-filter-compression-2.yaml 
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  namespace: istio-system
  name: apply-to
spec:
  workloadSelector:
    labels:
      istio: ingressgateway
  configPatches:
    - applyTo: HTTP_FILTER
      match:
        context: GATEWAY
        listener:
          filterChain:
            filter:
              name: envoy.filters.network.http_connection_manager
              subFilter:
                name: envoy.filters.http.router
      patch:
        operation: INSERT_BEFORE
        value:
          name: envoy.filters.http.compressor
          typed_config:
            "@type": type.googleapis.com/envoy.extensions.filters.http.compressor.v3.Compressor
            response_direction_config:
              common_config:
                min_content_length: 100
                content_type:
                - 'text/html'
            compressor_library:
              name: text_optimized
              typed_config:
                "@type": type.googleapis.com/envoy.extensions.compression.gzip.compressor.v3.Gzip
                memory_level: 9
                window_bits: 15
                compression_level: BEST_COMPRESSION
                compression_strategy: DEFAULT_STRATEGY
EOF

kubectl apply -f ef-ingressgateway-http-filter-compression-2.yaml  -n istio-system
```

![2](10image\2.jpg)

提高参数后传输数据从1.9k下降到1.8k



### 2.1.3最快压缩速度

compression_level: best_speed

```
cat << EOF > ef-ingressgateway-http-filter-compression-3.yaml 
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  namespace: istio-system
  name: apply-to
spec:
  workloadSelector:
    labels:
      istio: ingressgateway
  configPatches:
    - applyTo: HTTP_FILTER
      match:
        context: GATEWAY
        listener:
          filterChain:
            filter:
              name: envoy.filters.network.http_connection_manager
              subFilter:
                name: envoy.filters.http.router
      patch:
        operation: INSERT_BEFORE
        value:
          name: envoy.filters.http.compressor
          typed_config:
            "@type": type.googleapis.com/envoy.extensions.filters.http.compressor.v3.Compressor
            response_direction_config:
              common_config:
                min_content_length: 100
                content_type:
                - 'text/html'
            compressor_library:
              name: text_optimized
              typed_config:
                "@type": type.googleapis.com/envoy.extensions.compression.gzip.compressor.v3.Gzip
                memory_level: 9
                window_bits: 15
                compression_level: BEST_SPEED
                compression_strategy: DEFAULT_STRATEGY
EOF

kubectl apply -f ef-ingressgateway-http-filter-compression-3.yaml  -n istio-system
```

![5](10image\5.jpg)

BEST_SPEED传输大小从1.8k提升到1.9k



### 2.1.4请求启用压缩

```
cat << EOF > ef-ingressgateway-http-filter-compression-4.yaml 
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  namespace: istio-system
  name: apply-to
spec:
  workloadSelector:
    labels:
      istio: ingressgateway
  configPatches:
    - applyTo: HTTP_FILTER
      match:
        context: GATEWAY
        listener:
          filterChain:
            filter:
              name: envoy.filters.network.http_connection_manager
              subFilter:
                name: envoy.filters.http.router
      patch:
        operation: INSERT_BEFORE
        value:
          name: envoy.filters.http.compressor
          typed_config:
            "@type": type.googleapis.com/envoy.extensions.filters.http.compressor.v3.Compressor
            response_direction_config:
              common_config:
                min_content_length: 100
                content_type:
                - 'text/html'
            request_direction_config:
              common_config:
                enabled:
                  default_value: true
                  runtime_key: request_compressor_enabled
            compressor_library:
              name: text_optimized
              typed_config:
                "@type": type.googleapis.com/envoy.extensions.compression.gzip.compressor.v3.Gzip
                memory_level: 9
                window_bits: 15
                compression_level: BEST_SPEED
                compression_strategy: DEFAULT_STRATEGY
EOF

kubectl apply -f ef-ingressgateway-http-filter-compression-4.yaml  -n istio-system
```

request_direction_config配置请求压缩



### 2.1.5禁用响应压缩，只用请求压缩

```
cat << EOF > ef-ingressgateway-http-filter-compression-5.yaml 
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  namespace: istio-system
  name: apply-to
spec:
  workloadSelector:
    labels:
      istio: ingressgateway
  configPatches:
    - applyTo: HTTP_FILTER
      match:
        context: GATEWAY
        listener:
          filterChain:
            filter:
              name: envoy.filters.network.http_connection_manager
              subFilter:
                name: envoy.filters.http.router
      patch:
        operation: INSERT_BEFORE
        value:
          name: envoy.filters.http.compressor
          typed_config:
            "@type": type.googleapis.com/envoy.extensions.filters.http.compressor.v3.Compressor
            response_direction_config:
              common_config:
                enabled:
                  default_value: false
                  runtime_key: response_compressor_enabled
                min_content_length: 100
                content_type:
                - 'text/html'
            request_direction_config:
              common_config:
                enabled:
                  default_value: true
                  runtime_key: request_compressor_enabled
            compressor_library:
              name: text_optimized
              typed_config:
                "@type": type.googleapis.com/envoy.extensions.compression.gzip.compressor.v3.Gzip
                memory_level: 9
                window_bits: 15
                compression_level: BEST_SPEED
                compression_strategy: DEFAULT_STRATEGY
EOF

kubectl apply -f ef-ingressgateway-http-filter-compression-5.yaml  -n istio-system
```



### 2.1.6全局设置

ef-ingressgateway-http-filter-compression-global.yaml

kubectl apply -f ef-ingressgateway-http-filter-compression-global.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  namespace: istio-system
  name: apply-to
spec:
  workloadSelector:
    labels:
      istio: ingressgateway
  configPatches:
    - applyTo: HTTP_FILTER
      match:
        context: GATEWAY
        listener:
          filterChain:
            filter:
              name: envoy.filters.network.http_connection_manager
              subFilter:
                name: envoy.filters.http.router
      patch:
        operation: INSERT_BEFORE
        value:
          name: envoy.filters.http.compressor
          typed_config:
            "@type": type.googleapis.com/envoy.extensions.filters.http.compressor.v3.Compressor
            content_length: 30
            content_type:
            - 'text/html'
            disable_on_etag_header: true
            remove_accept_encoding_header: true
            runtime_enabled:
              default_value: true
              runtime_key: compressor_enabled
            response_direction_config:
              common_config:
                min_content_length: 100
            compressor_library:
              name: text_optimized
              typed_config:
                "@type": type.googleapis.com/envoy.extensions.compression.gzip.compressor.v3.Gzip
                memory_level: 9
                window_bits: 15
                compression_level: BEST_SPEED
                compression_strategy: DEFAULT_STRATEGY
```

etag:

Etag [1] 是URL的Entity Tag，用于标示URL对象是否改变，区分不同语言和 [Session](http://baike.baidu.com/view/25258.htm)等等。具体内部含义是使 [服务器](http://baike.baidu.com/view/899.htm)控制的，就像 [Cookie](http://baike.baidu.com/view/835.htm)那样。

HTTP协议规格说明定义ETag为“被请求 [变量](http://baike.baidu.com/view/296689.htm)的实体值”。另一种说法是，ETag是一个可以与Web资源关联的记号（token）。典型的Web资源可以一个Web页，但也可能是JSON或XML文档。服务器单独负责判断记号是什么及其含义，并在HTTP响应头中将其传送到 [客户端](http://baike.baidu.com/view/930.htm)，以下是服务器端返回的格式：ETag:"50b1c1d4f775c61:df3"客户端的查询更新格式是这样的：If-None-Match : W / "50b1c1d4f775c61:df3"如果ETag没改变，则返回状态304然后不返回，这也和Last-Modified一样。测试Etag主要在断点下载时比较有用。

性能

聪明的服务器开发者会把ETags和GET请求的“If-None-Match”头一起使用，这样可利用客户端（例如浏览器）的缓存。因为服务器首先产生ETag，服务器可在稍后使用它来判断页面是否已经被修改。本质上， [客户端](http://baike.baidu.com/view/930.htm)通过将该记号传回服务器要求服务器验证其（客户端）缓存。

其过程如下：

客户端请求一个页面（A）。 服务器返回页面A，并在给A加上一个ETag。 客户端展现该页面，并将页面连同ETag一起缓存。 客户再次请求页面A，并将上次请求时服务器返回的ETag一起传递给服务器。 服务器检查该ETag，并判断出该页面自上次客户端请求之后还未被修改，直接返回响应304（未修改——Not Modified）和一个空的响应体。

https://blog.csdn.net/chenzhiqin20/article/details/10947857?ref=myread



### 2.1.7compressor_library

- [**envoy**.compression.brotli.**compressor**](https://www.envoyproxy.io/docs/envoy/latest/api-v3/extensions/compression/brotli/compressor/v3/brotli.proto#extension-envoy-compression-brotli-compressor)
- [**envoy**.compression.gzip.**compressor**](https://www.envoyproxy.io/docs/envoy/latest/api-v3/extensions/compression/gzip/compressor/v3/gzip.proto#extension-envoy-compression-gzip-compressor)
- [**envoy**.compression.zstd.**compressor**](https://www.envoyproxy.io/docs/envoy/latest/api-v3/extensions/compression/zstd/compressor/v3/zstd.proto#extension-envoy-compression-zstd-compressor)

#### 2.1.7.1brotli

```
{
  "quality": "{...}",
  "encoder_mode": "...",
  "window_bits": "{...}",
  "input_block_bits": "{...}",
  "chunk_size": "{...}",
  "disable_literal_context_modeling": "..."
}
```

ef-ingressgateway-http-filter-compression-brotli.yaml

kubectl apply -f ef-ingressgateway-http-filter-compression-brotli.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  namespace: istio-system
  name: apply-to
spec:
  workloadSelector:
    labels:
      istio: ingressgateway
  configPatches:
    - applyTo: HTTP_FILTER
      match:
        context: GATEWAY
        listener:
          filterChain:
            filter:
              name: envoy.filters.network.http_connection_manager
              subFilter:
                name: envoy.filters.http.router
      patch:
        operation: INSERT_BEFORE
        value:
          name: envoy.filters.http.compressor
          typed_config:
            "@type": type.googleapis.com/envoy.extensions.filters.http.compressor.v3.Compressor
            response_direction_config:
              common_config:
                enabled:
                  default_value: true
                  runtime_key: response_compressor_enabled
                min_content_length: 100
                content_type:
                - 'text/html'
            compressor_library:
              name: text_optimized
              typed_config:
                "@type": type.googleapis.com/envoy.extensions.compression.brotli.compressor.v3.Brotli
                quality: 3
                encoder_mode: DEFAULT
                window_bits: 18
                input_block_bits: 24
                chunk_size: 4096
                disable_literal_context_modeling: true
```

**quality**:Value from 0 to 11 that controls the main compression speed-density lever. The higher quality, the slower compression. The default value is 3.

**encoder_mode**:  A value used to tune encoder for specific input

- DEFAULT

- GENERIC

- TEXT

- FONT

**window_bits**:Value from 10 to 24 that represents the base two logarithmic of the compressor’s window size. Larger window results in better compression at the expense of memory usage. The default is 18. 

**input_block_bits**: Value from 16 to 24 that represents the base two logarithmic of the compressor’s input block size. Larger input block results in better compression at the expense of memory usage. The default is 24

**chunk_size**: Value for compressor’s next output buffer. If not set, defaults to 4096

**disable_literal_context_modeling**:If true, disables “literal context modeling” format feature. This flag is a “decoding-speed vs compression ratio” trade-off.

#### 2.1.7.2gzip

略

#### 2.1.7.3zstd

```
{
  "compression_level": "{...}",
  "enable_checksum": "...",
  "strategy": "...",
  "dictionary": "{...}",
  "chunk_size": "{...}"
}
```

ef-ingressgateway-http-filter-compression-zstd.yaml

kubectl apply -f ef-ingressgateway-http-filter-compression-zstd.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  namespace: istio-system
  name: apply-to
spec:
  workloadSelector:
    labels:
      istio: ingressgateway
  configPatches:
    - applyTo: HTTP_FILTER
      match:
        context: GATEWAY
        listener:
          filterChain:
            filter:
              name: envoy.filters.network.http_connection_manager
              subFilter:
                name: envoy.filters.http.router
      patch:
        operation: INSERT_BEFORE
        value:
          name: envoy.filters.http.compressor
          typed_config:
            "@type": type.googleapis.com/envoy.extensions.filters.http.compressor.v3.Compressor
            response_direction_config:
              common_config:
                enabled:
                  default_value: true
                  runtime_key: response_compressor_enabled
                min_content_length: 100
                content_type:
                - 'text/html'
            compressor_library:
              name: text_optimized
              typed_config:
                "@type": type.googleapis.com/envoy.extensions.compression.zstd.compressor.v3.Zstd
                compression_level: 3
                enable_checksum: false
                strategy: DEFAULT
                chunk_size: 4096
```

**compression_level**:Value 0 means default, and default level is 3. Setting a level does not automatically set all other compression parameters to default. Setting this will however eventually dynamically impact the compression parameters which have not been manually set. The manually set ones will ‘stick’.

**enable_checksum**:A 32-bits checksum of content is written at end of frame. If not set, defaults to false.

**strategy**:The higher the value of selected strategy, the more complex it is, resulting in stronger and slower compression. Special: value 0 means “use default strategy”.

- DEFAULT⁣

- FAST

- DFAST

- GREEDY

- LAZY

- LAZY2

- BTLAZY2

- BTOPT

- BTULTRA⁣

- BTULTRA2

**dictionary**:A dictionary for compression. Zstd offers dictionary compression, which greatly improves efficiency on small files and messages. Each dictionary will be generated with a dictionary ID that can be used to search the same dictionary during decompression. Please refer to [zstd manual](https://github.com/facebook/zstd/blob/dev/programs/zstd.1.md#dictionary-builder) to train a specific dictionary for compression.

**chunk_size**:Value for compressor’s next output buffer. If not set, defaults to 4096.



## 2.2mesh内部配置压缩

reviews，ratings之间启用压缩

```
cat << EOF > ef-ratings-http-filter-compression.yaml 
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: ratings
spec:
  workloadSelector:
    labels:
      app: ratings
  configPatches:
    - applyTo: HTTP_FILTER
      match:
        context: SIDECAR_INBOUND
        listener:
          filterChain:
            filter:
              name: envoy.filters.network.http_connection_manager
              subFilter:
                name: envoy.filters.http.router
      patch:
        operation: INSERT_BEFORE
        value:
          name: envoy.filters.http.compressor
          typed_config:
            "@type": type.googleapis.com/envoy.extensions.filters.http.compressor.v3.Compressor
            response_direction_config:
              common_config:
                enabled:
                  default_value: true
                  runtime_key: response_compressor_enabled
                min_content_length: 10
                content_type:
                - 'application/json'
            request_direction_config:
              common_config:
                enabled:
                  default_value: true
                  runtime_key: request_compressor_enabled
            compressor_library:
              name: text_optimized
              typed_config:
                "@type": type.googleapis.com/envoy.extensions.compression.gzip.compressor.v3.Gzip
                memory_level: 9
                window_bits: 12
                compression_level: BEST_SPEED
                compression_strategy: DEFAULT_STRATEGY
EOF

kubectl apply -f ef-ratings-http-filter-compression.yaml  -n istio
```



![7](10image\7.jpg)

raings启用了压缩



reviews启用解压缩

```
cat << EOF > ef-reviews-http-filter-compression.yaml 
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: reviews
spec:
  workloadSelector:
    labels:
      app: reviews
  configPatches:
    - applyTo: HTTP_FILTER
      match:
        context: SIDECAR_OUTBOUND
        listener:
          filterChain:
            filter:
              name: envoy.filters.network.http_connection_manager
              subFilter:
                name: envoy.filters.http.router
      patch:
        operation: INSERT_BEFORE
        value:
          name: envoy.filters.http.decompressor
          typed_config:
            "@type": type.googleapis.com/envoy.extensions.filters.http.decompressor.v3.Decompressor
            response_direction_config:
              common_config:
                enabled:
                  default_value: true
                  runtime_key: response_decompressor_enabled
            request_direction_config:
              common_config:
                enabled:
                  default_value: false
                  runtime_key: request_decompressor_enabled
            decompressor_library:
              name: text_optimized
              typed_config:
                "@type": type.googleapis.com/envoy.extensions.compression.gzip.decompressor.v3.Gzip
                chunk_size: 4096
                window_bits: 15
EOF

kubectl apply -f ef-reviews-http-filter-compression.yaml  -n istio
```

- window_bits

  窗口位大小，值从9到15，解压的窗口位大小需要大于等于压缩的窗口位大小。默认值是15

- chunk_size

  块大小，用于输出缓存，默认值是4096

    value must be inside range [4096, 65536] 

![8](10image\8.jpg)