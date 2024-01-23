package com.jcastillo6.grcp.server;

import static org.junit.jupiter.api.Assertions.*;


import java.io.IOException;

import org.junit.Rule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;

import com.jcastillo6.api.grpc.v1.CreateWarehouseReq;
import com.jcastillo6.api.grpc.v1.WarehouseId;
import com.jcastillo6.api.grpc.v1.WarehouseServiceGrpc;
import com.jcastillo6.grcp.interceptor.ExceptionInterceptor;
import com.jcastillo6.grcp.service.WarehouseService;

import io.grpc.StatusRuntimeException;
import io.grpc.testing.GrpcCleanupRule;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;


@ActiveProfiles("test")
@SpringBootTest
@TestMethodOrder(OrderAnnotation.class)
class GrpcServerTest {
    @Autowired
    private ApplicationContext context;
    @Rule
    public final static GrpcCleanupRule grpcCleanup = new GrpcCleanupRule();
    private static WarehouseServiceGrpc.WarehouseServiceBlockingStub blockingStub;
    private static String newlyCreatedWarehouseId = null;

    @BeforeAll
    public static void setup(@Autowired WarehouseService warehouseService,
                             @Autowired ExceptionInterceptor exceptionInterceptor)
        throws IOException {
        // Generate a unique in-process server name.
        String serverName = InProcessServerBuilder.generateName();

        // Create a server, add service, start, and register for automatic graceful shutdown.
        grpcCleanup.register(InProcessServerBuilder
            .forName(serverName).directExecutor().addService(warehouseService)
            .intercept(exceptionInterceptor)
            .build().start());

        blockingStub = WarehouseServiceGrpc.newBlockingStub(
            // Create a client channel and register for automatic graceful shutdown.
            grpcCleanup.register(InProcessChannelBuilder.forName(serverName).directExecutor().build()));
    }

    @Test
    @Order(1)
    void beanGrpcServerRunnerTest() {
        assertNotNull(context.getBean(GrpcServer.class));
        assertThrows(NoSuchBeanDefinitionException.class,
            () -> context.getBean(GrpcServerRunner.class),
            "GrpcServerRunner should not be loaded during test");
    }

    @Test
    @Order(2)
    @DisplayName("Creates the warehouse object using create RPC call")
     void WarehouseService_Create() {
        CreateWarehouseReq.Response response =
            blockingStub.create(CreateWarehouseReq.newBuilder()
                .setName("Valencia 1")
                .setActive(true)
                .setAddress("zona industrial castillito")
                .build());
        assertNotNull(response);
        assertNotNull(response.getWarehouse());
        newlyCreatedWarehouseId = response.getWarehouse().getId();
        assertEquals("Valencia 1", response.getWarehouse().getName());
        assertEquals("zona industrial castillito", response.getWarehouse().getAddress());
    }

    @Test
    @Order(3)
    @DisplayName("Throws the exception when invalid warehouse id is passed to retrieve RPC call")
    public void WarehouseService_RetrieveForInvalidId() {
        Throwable throwable = assertThrows(
            StatusRuntimeException.class,
            () -> blockingStub.retrieve(WarehouseId.newBuilder().setId("").build()));
        assertEquals("INVALID_ARGUMENT: Invalid Warehouse ID is passed.", throwable.getMessage());
    }

    @Test
    @Order(4)
    @DisplayName("Retrieves the warehouse object created using create RPC call")
     void SourceService_Retrieve() {
        WarehouseId.Response response =
            blockingStub.retrieve(WarehouseId.newBuilder().setId(newlyCreatedWarehouseId).build());
        assertNotNull(response);
        assertNotNull(response.getWarehouse());
        assertEquals("Valencia 1", response.getWarehouse().getName());
        assertEquals("zona industrial castillito", response.getWarehouse().getAddress());
    }
}