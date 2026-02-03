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

import java.io.IOException;
import java.net.Socket;

import javax.net.SocketFactory;

import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.mongo.transitions.ImmutableMongod;
import de.flapdoodle.embed.mongo.transitions.Mongod;
import de.flapdoodle.embed.mongo.transitions.RunningMongodProcess;
import de.flapdoodle.reverse.TransitionWalker;
import de.flapdoodle.reverse.transitions.Start;

/**
 * @author Allard Buijze
 */
public class MongoLauncher {

	public static final int MONGO_DEFAULT_PORT = 27017;
	public static final String LOCALHOST = "127.0.0.1";
//    private static final Logger logger = LoggerFactory.getLogger(MongoLauncher.class);
//    private static final AtomicInteger counter = new AtomicInteger();

	private static boolean isMongoRunning() {
		try {
			final Socket mongoSocket = SocketFactory.getDefault().createSocket(LOCALHOST, MONGO_DEFAULT_PORT);

			if (mongoSocket.isConnected()) {
				mongoSocket.close();
				return true;
			}
		} catch (IOException e) {
			return false;
		}
		return false;
	}

	public static TransitionWalker.ReachedState<RunningMongodProcess> startMongoDB() throws IOException {
        if (isMongoRunning()) {
            return null;
        }

        ImmutableMongod mongodConfig = Mongod.builder()
        		.net(Start.to(Net.class)
        				.initializedWith(Net.defaults()
        						.withPort(MONGO_DEFAULT_PORT)))
        		.build();
        Version.Main version = Version.Main.V7_0;

        return mongodConfig.start(version);

//        IMongodConfig mongodConfig = new MongodConfigBuilder()
//                .version(Version.Main.PRODUCTION)
//                .net(new Net(MONGO_DEFAULT_PORT, false))
//                .build();
//
//        Command command = Command.MongoD;
//        IRuntimeConfig runtimeConfig = new RuntimeConfigBuilder()
//                .defaults(command)
//                .artifactStore(new ArtifactStoreBuilder()
//                        .defaults(command)
//                        .download(new DownloadConfigBuilder()
//                                .defaultsForCommand(command))
//                        .executableNaming((prefix, postfix) -> prefix + "_axontest_" + counter.getAndIncrement() + "_" + postfix))
//                .build();
//
//        MongodStarter runtime = MongodStarter.getInstance(runtimeConfig);
//
//        return runtime.prepare(mongodConfig);
    }
}
