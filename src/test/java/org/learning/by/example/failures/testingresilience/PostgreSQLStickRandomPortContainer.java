package org.learning.by.example.failures.testingresilience;

import org.testcontainers.containers.InternetProtocol;
import org.testcontainers.containers.PostgreSQLContainer;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Random;

/**
 * Create a PostgreSQL container that has a random stick port so after creation the container could be stop or started
 * as many times as we need but the random port will be remain the same that was originally.
 */
public class PostgreSQLStickRandomPortContainer<SELF extends PostgreSQLStickRandomPortContainer<SELF>> extends PostgreSQLContainer<SELF> {
    private static final int LOW_RANDOM_PORT = 30000;
    private static final int HIGH_RANDOM_PORT = 40000;

    private final int randomStickPort;

    public PostgreSQLStickRandomPortContainer() {
        super();
        this.randomStickPort = getRandomPort();
        this.addFixedExposedPort(randomStickPort, POSTGRESQL_PORT, InternetProtocol.TCP);
    }

    public int getRandomStickPort() {
        return this.randomStickPort;
    }

    private static int getRandomPort() {
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
