/*
 * Copyright (c) 2010-2014. Axon Framework
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.axonframework.common.caching;

import org.ehcache.core.Ehcache;
import org.ehcache.event.CacheEvent;
import org.ehcache.event.CacheEventListener;
import org.ehcache.event.EventFiring;
import org.ehcache.event.EventOrdering;
import org.ehcache.event.EventType;
import org.axonframework.common.Registration;

import java.util.EnumSet;

/**
 * Cache implementation that delegates all calls to an EhCache instance.
 *
 * @author Allard Buijze
 * @since 2.1.2
 */
public class EhCacheAdapter extends AbstractCacheAdapter<CacheEventListener> {

    private final Ehcache ehCache;

    /**
     * Initialize the adapter to forward all call to the given {@code ehCache} instance
     *
     * @param ehCache The cache instance to forward calls to
     */
    public EhCacheAdapter(Ehcache ehCache) {
        this.ehCache = ehCache;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <K, V> V get(K key) {
        final Object value = ehCache.get(key);
        return value != null ? (V) value : null;
    }

    @Override
    public <K, V> void put(K key, V value) {
        ehCache.put(key, value);
    }

    @Override
    public <K, V> boolean putIfAbsent(K key, V value) {
        return ehCache.putIfAbsent(key, value) == null;
    }

    @Override
    public <K> boolean remove(K key) {
        Object value = ehCache.get(key);
        if (value == null) {
            return false;
        }
        return ehCache.remove(key, value);
    }

    @Override
    public <K> boolean containsKey(K key) {
        return ehCache.containsKey(key);
    }

    @SuppressWarnings("ClassEscapesDefinedScope")
    @Override
    protected EhCacheAdapter.CacheEventListenerAdapter createListenerAdapter(EntryListener cacheEntryListener) {
        return new EhCacheAdapter.CacheEventListenerAdapter(cacheEntryListener);
    }

    @Override
    protected Registration doRegisterListener(CacheEventListener listenerAdapter) {
        ehCache.getRuntimeConfiguration().registerCacheEventListener(listenerAdapter, EventOrdering.ORDERED, EventFiring.SYNCHRONOUS, EnumSet.allOf(EventType.class));
        return () -> {
            try {
                ehCache.getRuntimeConfiguration().deregisterCacheEventListener(listenerAdapter);
            } catch (IllegalStateException e) {
                return false;
            }
            return true;
        };
    }

    @SuppressWarnings("unchecked")
    private static class CacheEventListenerAdapter implements CacheEventListener {
        private EntryListener delegate;

        public CacheEventListenerAdapter(EntryListener delegate) {
            this.delegate = delegate;
        }

        @Override
        public void onEvent(CacheEvent event) {
            switch (event.getType()) {
                case CREATED:
                    delegate.onEntryCreated(event.getKey(), event.getNewValue());
                    break;
                case UPDATED:
                    delegate.onEntryUpdated(event.getKey(), event.getNewValue());
                    break;
                case REMOVED:
                case EVICTED:
                    delegate.onEntryRemoved(event.getKey());
                    break;
                case EXPIRED:
                    delegate.onEntryExpired(event.getKey());
                    break;
                default:
                    throw new AssertionError("Unsupported event type " + event.getType());
            }
        }
    }
}
