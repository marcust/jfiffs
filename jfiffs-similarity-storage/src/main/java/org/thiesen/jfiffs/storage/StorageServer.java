package org.thiesen.jfiffs.storage;

import com.google.inject.Inject;
import com.google.inject.Injector;
import io.vertx.core.Future;
import io.vertx.grpc.VertxServer;
import io.vertx.grpc.VertxServerBuilder;
import io.vertx.reactivex.core.Vertx;
import lombok.SneakyThrows;
import org.apache.commons.lang3.tuple.Pair;
import org.jzenith.core.JZenith;
import org.thiesen.jfiffs.storage.business.BusinessModule;
import org.thiesen.jfiffs.storage.business.Result;
import org.thiesen.jfiffs.storage.business.StorageService;
import org.thiesen.jfiffs.storage.mapper.MapperModule;
import org.thiesen.jfiffs.storage.mapper.ReplyMapper;
import org.thiesen.jfiffs.storage.proto.DeleteRequest;
import org.thiesen.jfiffs.storage.proto.GetReply;
import org.thiesen.jfiffs.storage.proto.GetRequest;
import org.thiesen.jfiffs.storage.proto.Key;
import org.thiesen.jfiffs.storage.proto.Reply;
import org.thiesen.jfiffs.storage.proto.StorageGrpc;
import org.thiesen.jfiffs.storage.proto.StoreRequest;
import org.thiesen.jfiffs.storage.proto.UUID;

public class StorageServer {

    @Inject
    private Vertx vertx;

    @Inject
    private StorageService storageService;

    @Inject
    private ReplyMapper mapper;

    public static void main(String[] args) {
        final Injector injector = JZenith.application(args)
                .withModules(new BusinessModule(), new MapperModule())
                .createInjectorForTesting();

        final StorageServer app = new StorageServer();
        injector.injectMembers(app);

        app.run(args);
    }

    @SneakyThrows
    private void run(String[] args) {
        final StorageGrpc.StorageVertxImplBase service = new StorageGrpc.StorageVertxImplBase() {

            @Override
            public void storeSimilarity(StoreRequest request, Future<Reply> response) {
                vertx.<Result>rxExecuteBlocking(result ->
                        result.complete(storageService.store(toUUIDPair(request.getKey()), request.getSimilarity())), false)
                        .map(mapper::toReply)
                        .subscribe(response::complete, response::fail);
            }

            @Override
            public void deleteSimilarity(DeleteRequest request, Future<Reply> response) {
                vertx.<Result>rxExecuteBlocking(result ->
                        result.complete(storageService.delete(toUUIDPair(request.getKey()))), false)
                        .map(mapper::toReply)
                        .subscribe(response::complete, response::fail);
            }

            @Override
            public void getSimilarity(GetRequest request, Future<GetReply> response) {
                vertx.<Double>rxExecuteBlocking(result ->
                        result.complete(storageService.get(toUUIDPair(request.getKey()))), false)
                        .map(mapper::toGetReply)
                        .subscribe(response::complete, response::fail);
            }
        };

        VertxServer rpcServer = VertxServerBuilder
                .forAddress(vertx.getDelegate(), "localhost", 8080)
                .addService(service)
                .build();

        // Start is asynchronous
        rpcServer.start();

    }

    private Pair<java.util.UUID, java.util.UUID> toUUIDPair(Key key) {
        return Pair.of(
                toUUID(key.getLeft()),
                toUUID(key.getRight())
        );
    }

    private java.util.UUID toUUID(UUID protoUUID) {
        return new java.util.UUID(protoUUID.getMostSigBits(), protoUUID.getLeastSigBits());
    }

}
