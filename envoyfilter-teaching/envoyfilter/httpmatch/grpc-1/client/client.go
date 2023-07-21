package main

import (
	"context"
	"fmt"
	"log"
         "time"

	"github.com/zhuge20100104/grpc-demo/grpc-1/client/services"

	"google.golang.org/grpc"
)

func main() {
	conn, err := grpc.Dial("product-service:8081", grpc.WithInsecure())
	if err != nil {
		log.Fatalf("连接GRPC服务端失败 %v\n", err)
	}

	defer conn.Close()
	prodClient := services.NewProductServiceClient(conn)
        for true  {
        	prodRes, err := prodClient.GetProductStock(context.Background(),
	        	&services.ProdRequest{ProdId: 12})

        	if err != nil {
	        	log.Fatalf("请求GRPC服务端失败 %v\n", err)
	         }
        	fmt.Println(prodRes.ProdStock)
                time.Sleep(time.Duration(1)*time.Second)
        } 
}
