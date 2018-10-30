package org.thiesen.jfiffs.storage.business.impl;

import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.core.WorkerExecutor;
import org.apache.commons.lang3.tuple.Pair;
import org.cliffc.high_scale_lib.NonBlockingHashMap;
import org.thiesen.jfiffs.storage.business.Result;
import org.thiesen.jfiffs.storage.business.StorageService;

import java.util.UUID;

public class StorageServiceImpl implements StorageService {

    private static final NonBlockingHashMap<UUID, NonBlockingHashMap<UUID, Double>> DATA =
            new NonBlockingHashMap<>();

    private final Vertx vertx;
    private final WorkerExecutor persistence;
    private final WriteAheadLog writeAheadLog;

    public StorageServiceImpl(Vertx vertx) {
        this.vertx = vertx;

        persistence = vertx.createSharedWorkerExecutor("persistence");

        writeAheadLog = new WriteAheadLog();

        writeAheadLog.loadInitial(this::store, this::delete);
    }

    @Override
    public Result store(Pair<UUID, UUID> uuids, double similarity) {
        persistence.executeBlocking(result -> {
            writeAheadLog.writeAdd(uuids, similarity);
        }, handler -> {});

        DATA.computeIfAbsent(uuids.getLeft(), uuid -> new NonBlockingHashMap<>())
            .put(uuids.getRight(), similarity);

        return Result.OK;
    }

    @Override
    public Result delete(Pair<UUID, UUID> uuids) {
        persistence.executeBlocking(result -> {
            writeAheadLog.writeDelete(uuids);
        }, handler -> {});


        DATA.computeIfAbsent(uuids.getLeft(), uuid -> new NonBlockingHashMap<>())
            .remove(uuids.getRight());

        return Result.OK;
    }

    @Override
    public Double get(Pair<UUID, UUID> uuids) {
        return DATA.computeIfAbsent(uuids.getLeft(), uuid -> new NonBlockingHashMap<>())
                .getOrDefault(uuids.getRight(), 0D);

    }
}
