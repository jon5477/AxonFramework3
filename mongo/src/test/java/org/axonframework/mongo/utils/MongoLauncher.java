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

package org.axonframework.mongo.utils;

import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;

import javax.net.SocketFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.mongo.transitions.MongodStarter;
import de.flapdoodle.embed.process.runtime.Network;

/**
 * @author Allard Buijze
 */
public class MongoLauncher {

    public static final int MONGO_DEFAULT_PORT = 27017;
    public static final String LOCALHOST = "127.0.0.1";
    private static final Logger logger = LoggerFactory.getLogger(MongoLauncher.class);
    private static final AtomicInteger counter = new AtomicInteger();

    private static boolean isMongoRunning() {
        try {
            final Socket mongoSocket = SocketFactory.getDefault().createSocket(LOCALHOST, MONGO_DEFAULT_PORT);

            if (mongoSocket.isConnected()) {
                mongoSocket.close();
                return true;
            }
        } catch(IOException e) {
            return false;
        }
        return false;
    }

    public static MongodExecutable prepareExecutable() throws IOException {
        if (isMongoRunning()) {
            return mock(MongodExecutable.class);
        }

        MongodConfig mongodConfig = MongodConfig.builder()
                .version(Version.Main.PRODUCTION) // latest stable MongoDB (6.x)
                .net(MONGO_DEFAULT_PORT, Network.localhostIsIPv6())
                .build();

        // Starter automatically picks correct binaries
        MongodStarter starter = MongodStarter.getDefaultInstance();

        return starter.prepare(mongodConfig);
    }
}
