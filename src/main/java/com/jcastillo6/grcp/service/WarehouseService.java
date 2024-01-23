package com.jcastillo6.grcp.service;

import org.springframework.stereotype.Service;

import com.jcastillo6.api.grpc.v1.CreateWarehouseReq;
import com.jcastillo6.api.grpc.v1.WarehouseId;
import com.jcastillo6.api.grpc.v1.WarehouseServiceGrpc;
import com.jcastillo6.grcp.repository.WarehouseRepository;

import io.grpc.stub.StreamObserver;

@Service
public class WarehouseService extends WarehouseServiceGrpc.WarehouseServiceImplBase {
    private final WarehouseRepository repository;

    public WarehouseService(WarehouseRepository warehouseRepository) {
        this.repository = warehouseRepository;
    }

    @Override
    public void create(CreateWarehouseReq req, StreamObserver<CreateWarehouseReq.Response> resObserver) {
        CreateWarehouseReq.Response resp = repository.create(req);
        resObserver.onNext(resp);
        resObserver.onCompleted();
    }

    @Override
    public void retrieve(WarehouseId chargeId, StreamObserver<WarehouseId.Response> resObserver) {
        WarehouseId.Response resp = repository.retrieve(chargeId.getId());
        resObserver.onNext(resp);
        resObserver.onCompleted();
    }
}
