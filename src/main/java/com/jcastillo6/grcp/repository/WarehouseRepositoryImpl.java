package com.jcastillo6.grcp.repository;

import org.springframework.stereotype.Repository;

import com.jcastillo6.api.grpc.v1.CreateWarehouseReq;
import com.jcastillo6.api.grpc.v1.WarehouseId;

@Repository
public class WarehouseRepositoryImpl implements WarehouseRepository {
    private final DbStore dbStore;

    public WarehouseRepositoryImpl(DbStore dbStore) {
        this.dbStore = dbStore;
    }

    @Override
    public CreateWarehouseReq.Response create(CreateWarehouseReq req) {
        return dbStore.createSource(req);
    }

    @Override
    public WarehouseId.Response retrieve(String warehouseId) {
        return dbStore.retrieveSource(warehouseId);
    }

    @Override
    public WarehouseId.Response retrieveAll(String customerId) {
        return null;
    }
}
