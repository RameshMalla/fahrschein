package org.zalando.fahrschein.inmemory;

import org.zalando.fahrschein.CursorManager;
import org.zalando.fahrschein.domain.Cursor;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public final class InMemoryCursorManager implements CursorManager {

    private final ConcurrentHashMap<String, ConcurrentHashMap<String, Cursor>> partitionsByEventName = new ConcurrentHashMap<>();

    private ConcurrentHashMap<String, Cursor> cursorsByPartition(final String eventName) {
        return partitionsByEventName.computeIfAbsent(eventName, key -> new ConcurrentHashMap<>());
    }

    @Override
    public void onSuccess(final String eventName, final Cursor cursor) {
        cursorsByPartition(eventName).put(cursor.getPartition(), cursor);
    }

    @Override
    public void onSuccess(String eventName, List<Cursor> cursors) throws IOException {
        for (Cursor cursor : cursors) {
            onSuccess(eventName, cursor);
        }
    }

    @Override
    public Collection<Cursor> getCursors(final String eventName) {
        return Collections.unmodifiableCollection(cursorsByPartition(eventName).values());
    }
}
