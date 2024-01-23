package com.jcastillo6.grcp.repository;

import com.jcastillo6.api.grpc.v1.CreateWarehouseReq;
import com.jcastillo6.api.grpc.v1.WarehouseId;

public interface WarehouseRepository {
    CreateWarehouseReq.Response create(CreateWarehouseReq req);
    WarehouseId.Response retrieve(String warehouseId);
    WarehouseId.Response retrieveAll(String customerId);
}
