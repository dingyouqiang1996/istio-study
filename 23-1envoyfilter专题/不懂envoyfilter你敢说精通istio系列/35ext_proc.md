# 1什么是 ext_proc 

 External Processing filter  即外部处理过滤器，他允许使用一个外部grpc服务来处理请求，修改头或体，或者直接响应连接。

 external processor 外部处理器，即配置的grpc服务，能做的事如下：

1修改请求或响应头信息

2修改请求或响应体信息

3修改动态流元数据

4直接响应客户端，终止后续处理

# 2配置

```
{
  "grpc_service": "{...}",
  "failure_mode_allow": "...",
  "processing_mode": "{...}",
  "message_timeout": "{...}"
}
```

grpc_service:

```
{
  "envoy_grpc": "{...}",
  "google_grpc": "{...}",
  "timeout": "{...}",
  "initial_metadata": []
}
```

 **processing_mode** ：

```
{
  "request_header_mode": "...",
  "response_header_mode": "...",
  "request_body_mode": "...",
  "response_body_mode": "...",
  "request_trailer_mode": "...",
  "response_trailer_mode": "..."
}
```

header_mode:

Control how headers and trailers are handled

- DEFAULT

  *(DEFAULT)* ⁣The default HeaderSendMode depends on which part of the message is being processed. By default, request and response headers are sent, while trailers are skipped.

- SEND

  ⁣Send the header or trailer.

- SKIP

  ⁣Do not send the header or trailer.

body_mode:

Control how the request and response bodies are handled

- NONE

  *(DEFAULT)* ⁣Do not send the body at all. This is the default.

- STREAMED

  ⁣Stream the body to the server in pieces as they arrive at the proxy.

- BUFFERED

  ⁣Buffer the message body in memory and send the entire body at once. If the body exceeds the configured buffer limit, then the downstream system will receive an error.

- BUFFERED_PARTIAL

  ⁣Buffer the message body in memory and send the entire body in one chunk. If the body exceeds the configured buffer limit, then the body contents up to the buffer limit will be sent.

# 3实战