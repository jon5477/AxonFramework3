/*
 * Copyright (c) 2010-2016. Axon Framework
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

package org.axonframework.mongo.eventsourcing.eventstore;

import static junit.framework.Assert.assertEquals;

import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;

import com.mongodb.MongoClientSettings;

/**
 * @author Jettro Coenradie
 */
public class MongoOptionsFactoryTest {

    private MongoOptionsFactory factory;

    @Before
    public void setUp() {
        factory = new MongoOptionsFactory();
    }

    @Test
    public void testCreateMongoOptions_defaults() {
        MongoClientSettings options = factory.createMongoOptions().build();
        MongoClientSettings defaults = MongoClientSettings.builder().build();

        assertEquals(defaults.getConnectionPoolSettings().getMaxWaitTime(TimeUnit.MILLISECONDS), options.getConnectionPoolSettings().getMaxWaitTime(TimeUnit.MILLISECONDS));
        assertEquals(defaults.getSocketSettings().getReadTimeout(TimeUnit.MILLISECONDS), options.getSocketSettings().getReadTimeout(TimeUnit.MILLISECONDS));
        assertEquals(defaults.getConnectionPoolSettings().getMaxSize(), options.getConnectionPoolSettings().getMaxSize());
        assertEquals(defaults.getSocketSettings().getConnectTimeout(TimeUnit.MILLISECONDS), options.getSocketSettings().getConnectTimeout(TimeUnit.MILLISECONDS));
    }

    @Test
    public void testCreateMongoOptions_customSet() {
        factory.setConnectionsPerHost(9);
        factory.setConnectionTimeout(11);
        factory.setMaxWaitTime(3);
        factory.setSocketTimeOut(23);

        MongoClientSettings options = factory.createMongoOptions().build();
        assertEquals(3, options.getConnectionPoolSettings().getMaxWaitTime(TimeUnit.MILLISECONDS));
        assertEquals(23, options.getSocketSettings().getReadTimeout(TimeUnit.MILLISECONDS));
        assertEquals(9, options.getConnectionPoolSettings().getMaxSize());
        assertEquals(11, options.getSocketSettings().getConnectTimeout(TimeUnit.MILLISECONDS));
    }
}
