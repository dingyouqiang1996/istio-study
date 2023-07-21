# -*- coding: utf-8 -*- 

import grpc
import logging
from concurrent import futures

import proto.wind_pb2 as wind_pb2
import proto.wind_pb2_grpc as wind_pb2_grpc

class WindPredictSrv(wind_pb2_grpc.WindServerServicer):

    def wind_predict(self, request, context):
        print("call wind_predict")
        return wind_pb2.Response(msg='%s!' % request.content)
        
    def send_data(self, request, context):
        print("call send_data")
        return wind_pb2.Response(msg='%s!' % request.content)

def server():
    grpc_server = grpc.server(futures.ThreadPoolExecutor(max_workers=10))
    wind_pb2_grpc.add_WindServerServicer_to_server(WindPredictSrv(), grpc_server)
    grpc_server.add_insecure_port('[::]:50052')
    grpc_server.start()
    grpc_server.wait_for_termination()

if __name__ == '__main__':
    logging.basicConfig()
    server()
