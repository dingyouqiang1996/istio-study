FROM golang:1.14 as build

ENV GO111MODULE=on
ENV GOPROXY=https://goproxy.cn,direct
ENV CGO_ENABLED=0
WORKDIR /app

ADD . /app

RUN go mod download

RUN export GOBIN=/app/bin && go build -o server server/server.go
RUN export GOBIN=/app/bin && go build -o client client/client.go

FROM alpine
COPY --from=build /app/server /
COPY --from=build /app/client /

EXPOSE 50051
ENTRYPOINT ["/client"]
