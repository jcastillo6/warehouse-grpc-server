package com.jcastillo6.grcp.server;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.jcastillo6.grcp.interceptor.ExceptionInterceptor;
import com.jcastillo6.grcp.service.WarehouseService;

import io.grpc.Server;
import io.grpc.ServerBuilder;

@Component
public class GrpcServer {
    private final Logger LOG = LoggerFactory.getLogger(getClass());
    @Value("${grpc.port:8080}")
    private int port;
    private Server server;
    private WarehouseService warehouseService;
    private ExceptionInterceptor exceptionInterceptor;

    public GrpcServer(WarehouseService warehouseService) {
        this.warehouseService = warehouseService;
    }

    public void start() throws IOException, InterruptedException {
        LOG.info("gRPC server is starting on port: {}.", port);
        server = ServerBuilder.forPort(port)
            .addService(warehouseService)
            .intercept(exceptionInterceptor)
            .build().start();
        LOG.info("gRPC server started and listening on port: {}.", port);
        LOG.info("Following service are available: ");
        server.getServices().stream()
            .forEach(s -> LOG.info("Service Name: {}", s.getServiceDescriptor().getName()));
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOG.info("Shutting down gRPC server.");
            GrpcServer.this.stop();
            LOG.info("gRPC server shut down successfully.");
        }));
    }

    private void stop() {
        if (server != null) {
            server.shutdown();
        }
    }

    public void block() throws InterruptedException {
        if (server != null) {
            // received the request until application is terminated
            server.awaitTermination();
        }
    }
}
