/*
 * Copyright 2020 Learning by Example maintainers.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.learning.by.example.failures.testingresilience.test;

import org.testcontainers.containers.InternetProtocol;
import org.testcontainers.containers.PostgreSQLContainer;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Random;

/**
 * Create a PostgreSQL container that has a random stick port so after creation the container could be stop or started
 * as many times as we need but the random port will be remain the same that was originally.
 */
public class PostgreSQLStickRandomPortContainer<SELF extends PostgreSQLStickRandomPortContainer<SELF>>
    extends PostgreSQLContainer<SELF> {

    private static final int LOW_RANDOM_PORT = 30000;
    private static final int HIGH_RANDOM_PORT = 40000;

    private final int randomStickPort;

    public PostgreSQLStickRandomPortContainer() {
        super();
        this.randomStickPort = getFreeRandomPort();
        this.addFixedExposedPort(randomStickPort, POSTGRESQL_PORT, InternetProtocol.TCP);
    }

    public int getRandomStickPort() {
        return this.randomStickPort;
    }

    private static int getFreeRandomPort() {
        final Random rnd = new Random();
        while (true) {
            int port = rnd.nextInt(HIGH_RANDOM_PORT - LOW_RANDOM_PORT) + LOW_RANDOM_PORT;
            if (isPortAvailable(port)) {
                return port;
            }
        }
    }

    private static boolean isPortAvailable(int port) {
        try (final ServerSocket serverSocket = new ServerSocket(port)) {
            serverSocket.setReuseAddress(true);
            return true;
        } catch (IOException ignored) {
        }
        return false;
    }
}
