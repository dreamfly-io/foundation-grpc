package io.dreamfly.grpc.test;

import io.grpc.stub.StreamObserver;

public class ControllableServiceImpl extends ControllableServiceGrpc.ControllableServiceImplBase {

    @Override
    public void execute(ControllableRequest request, StreamObserver<ControllableResponse> responseObserver) {
        // Latency
        int expectedLatency = request.getExpectedLatency();
        if (expectedLatency > 0) {
            try {
                Thread.sleep(expectedLatency);
            } catch (InterruptedException e) {
                throw new RuntimeException("thread sleep interrupted", e);
            }
        }

        ControllableResponse response = ControllableResponse.newBuilder().setRequestId(request.getRequestId()).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
