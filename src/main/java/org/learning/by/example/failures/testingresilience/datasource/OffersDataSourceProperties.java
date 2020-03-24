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

package org.learning.by.example.failures.testingresilience.datasource;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("offers-datasource")
public class OffersDataSourceProperties {
    private String protocol;
    private String host;
    private Integer port;
    private String database;
    private CredentialsProperties credentials;
    private Boolean readOnly;
    private PoolProperties pool;
    private SSLProperties ssl;

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public CredentialsProperties getCredentials() {
        return credentials;
    }

    public void setCredentials(CredentialsProperties credentials) {
        this.credentials = credentials;
    }

    public Boolean getReadOnly() {
        return readOnly;
    }

    public void setReadOnly(Boolean readOnly) {
        this.readOnly = readOnly;
    }

    public PoolProperties getPool() {
        return pool;
    }

    public void setPool(PoolProperties pool) {
        this.pool = pool;
    }

    public SSLProperties getSsl() {
        return ssl;
    }

    public void setSsl(SSLProperties ssl) {
        this.ssl = ssl;
    }

    public static class CredentialsProperties {
        private String user;
        private String password;

        public String getUser() {
            return user;
        }

        public void setUser(String user) {
            this.user = user;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    public static class PoolProperties {
        private ConnectionsProperties connections;

        public ConnectionsProperties getConnections() {
            return connections;
        }

        public void setConnections(ConnectionsProperties connection) {
            this.connections = connection;
        }

        public static class ConnectionsProperties {
            private Integer min;
            private Integer max;

            public Integer getMin() {
                return min;
            }

            public void setMin(Integer min) {
                this.min = min;
            }

            public Integer getMax() {
                return max;
            }

            public void setMax(Integer max) {
                this.max = max;
            }
        }
    }

    public static class SSLProperties {
        private Boolean enabled;
        private String mode;

        public Boolean getEnabled() {
            return enabled;
        }

        public void setEnabled(Boolean enabled) {
            this.enabled = enabled;
        }

        public String getMode() {
            return mode;
        }

        public void setMode(String mode) {
            this.mode = mode;
        }
    }
}
