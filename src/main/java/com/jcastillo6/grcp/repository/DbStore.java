package com.jcastillo6.grcp.repository;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;


import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Component;

import com.jcastillo6.api.grpc.v1.CreateWarehouseReq;
import com.jcastillo6.api.grpc.v1.StorageLocation;
import com.google.protobuf.Any;
import com.google.rpc.Code;
import io.grpc.protobuf.StatusProto;
import com.jcastillo6.api.grpc.v1.Warehouse;
import com.jcastillo6.api.grpc.v1.WarehouseId;

@Component
public class DbStore {
    private static final Map<String, Warehouse> warehouseEntities = new ConcurrentHashMap<>();
    private static final Map<String, StorageLocation> storageLocator = new ConcurrentHashMap<>();

    public DbStore() {
        // Seed StorageLocation for testing
        StorageLocation storageLocation = StorageLocation.newBuilder()
            .setId(RandomHolder.randomKey())
            .setType(StorageLocation.Type.Pallet)
            .setAisleNumber(2)
            .setIsOccupied(true)
            .setShelfNumber(1)
            .setMaxCapacity(100.5)
            .build();
        storageLocator.put(storageLocation.getId(), storageLocation);
        // Seed warehouse for testing
        Warehouse warehouse = Warehouse.newBuilder()
            .setId(RandomHolder.randomKey())
            .setName("Valencia")
            .setType(Warehouse.Type.DISTRIBUTION)
            .setAddress("Zona industrial castillito")
            .setActive(true)
            .addAllStorageLocation(List.of(storageLocation))
            .build();
        warehouseEntities.put(warehouse.getId(), warehouse);
    }

    public CreateWarehouseReq.Response createSource(CreateWarehouseReq req) {
        // validate request object
        // Owner and receiver should be taken from req. in the form of ID
        var warehouseBuilder = Warehouse.newBuilder()
            .setId(RandomHolder.randomKey())
            .setType(Warehouse.Type.valueOf(req.getType().name()))
            .setName(req.getName())
            .setActive(req.getActive())
            .setAddress(req.getAddress());
        var storageLocationList = req.getStorageLocationList().stream()
            .map(storageLocationReq ->
                StorageLocation.newBuilder()
                    .setId(RandomHolder.randomKey())
                    .setType(StorageLocation.Type.valueOf(storageLocationReq.getType().name()))
                    .setMaxCapacity(storageLocationReq.getMaxCapacity())
                    .setShelfNumber(storageLocationReq.getShelfNumber())
                    .setIsOccupied(storageLocationReq.getIsOccupied())
                    .setAisleNumber(storageLocationReq.getAisleNumber())
                    .setBinNumber(storageLocationReq.getBinNumber())
                    .build()
            ).toList();
        var warehouse = warehouseBuilder.addAllStorageLocation(storageLocationList).build();
        warehouseEntities.put(warehouse.getId(), warehouse);
        return CreateWarehouseReq.Response.newBuilder().setWarehouse(warehouse).build();
    }

    public WarehouseId.Response retrieveSource(String warehouseId) {
        if (Strings.isBlank(warehouseId)) {
            com.google.rpc.Status status = com.google.rpc.Status.newBuilder()
                .setCode(Code.INVALID_ARGUMENT.getNumber())
                .setMessage("Invalid Warehouse ID is passed.")
                .build();
            throw StatusProto.toStatusRuntimeException(status);
        }
        Warehouse warehouse = warehouseEntities.get(warehouseId);
        if (Objects.isNull(warehouse)) {
            com.google.rpc.Status status = com.google.rpc.Status.newBuilder()
                .setCode(Code.INVALID_ARGUMENT.getNumber())
                .setMessage("Requested Warehouse is not available")
                .addDetails(Any.pack(WarehouseId.Response.getDefaultInstance()))
                .build();
            throw StatusProto.toStatusRuntimeException(status);
        }
        return WarehouseId.Response.newBuilder().setWarehouse(warehouse).build();
    }


    // https://stackoverflow.com/a/31214709/109354
    // or can use org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric(n)
    private static class RandomHolder {

        static final Random random = new SecureRandom();

        public static String randomKey() {
            return randomKey(32);
        }

        public static String randomKey(int length) {
            return String.format("%" + length + "s", new BigInteger(length * 5/*base 32,2^5*/, random)
                .toString(32)).replace('\u0020', '0');
        }
    }
}
