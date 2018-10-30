package org.thiesen.jfiffs.storage.business.impl;

import com.google.common.base.Splitter;
import lombok.SneakyThrows;
import org.apache.commons.lang3.tuple.Pair;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class WriteAheadLog {

    private static final Splitter TAB_SPLITTER = Splitter.on('\t');

    private static final String ADD = "ADD";
    private static final String DELETE = "DELETE";

    private final FileWriter writer;
    private final File walFile;

    @SneakyThrows
    public WriteAheadLog() {
        walFile = new File("storeage.wal");
        writer = new FileWriter(walFile);
    }

    @SneakyThrows
    public void loadInitial(BiConsumer<Pair<UUID, UUID>, Double> store, Consumer<Pair<UUID, UUID>> delete) {
        try (final BufferedReader reader = new BufferedReader(new FileReader(walFile))) {
            reader.lines()
                    .map(TAB_SPLITTER::splitToList)
                    .forEach(list -> {
                        final String s = list.get(0);
                        switch (s) {
                            case ADD: doStore(store, list); break;
                            case DELETE: doDelete(delete, list); break;
                        }
                    });
        }
    }

    private void doDelete(Consumer<Pair<UUID, UUID>> delete, List<String> list) {
        final Pair<UUID, UUID> pair = toPair(list);

        delete.accept(pair);
    }

    private void doStore(BiConsumer<Pair<UUID, UUID>, Double> store, List<String> list) {
        final Pair<UUID, UUID> pair = toPair(list);
        final Double value = Double.valueOf(list.get(3));

        store.accept(pair, value);
    }

    private Pair<UUID, UUID> toPair(List<String> list) {
        return Pair.of(UUID.fromString(list.get(1)), UUID.fromString(list.get(2)));
    }

    @SneakyThrows
    public void writeAdd(Pair<UUID, UUID> uuids, double similarity) {
        writer.write(ADD);
        writer.write(uuids.getLeft().toString());
        writer.write(uuids.getRight().toString());
        writer.write(String.valueOf(similarity));
        writer.write("\n");
        writer.flush();
    }

    @SneakyThrows
    public void writeDelete(Pair<UUID, UUID> uuids) {
        writer.write(DELETE);
        writer.write(uuids.getLeft().toString());
        writer.write(uuids.getRight().toString());
        writer.write("\n");
        writer.flush();
    }
}
