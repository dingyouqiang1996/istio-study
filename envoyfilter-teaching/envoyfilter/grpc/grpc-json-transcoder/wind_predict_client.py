import grpc
import logging
import time

import proto.wind_pb2 as wind_pb2
import proto.wind_pb2_grpc as wind_pb2_grpc

def run():
    option = [('grpc.keepalive_timeout_ms', 10000)]
    while True:
        with grpc.insecure_channel(target='wind-server:50052', options=option) as channel:
            stub = wind_pb2_grpc.WindServerStub(channel)
            request = wind_pb2.Request(content='hello grpc')
            response = stub.wind_predict(request, timeout=10)
        print("Greeter client received: " + response.msg)
        time.sleep(2)
if __name__ == '__main__':
    logging.basicConfig()
    run()
