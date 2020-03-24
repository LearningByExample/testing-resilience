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

import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ConnectionFactoryOptions;
import io.r2dbc.spi.Option;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration;

import java.util.HashMap;
import java.util.Map;

import static io.r2dbc.pool.PoolingConnectionFactoryProvider.*;
import static io.r2dbc.spi.ConnectionFactoryOptions.*;

@Configuration
public class OffersDataSource extends AbstractR2dbcConfiguration {

    private static final String VALIDATION_SQL = "SELECT 1";
    private static final String POSTGRESQL_OPTION_READ_ONLY_TRX = "default_transaction_read_only";
    private static final Option<String> POSTGRESQL_SSL_MODE = Option.valueOf("sslMode");
    private static final Option<Map<String, String>> POSTGRESQL_OPTIONS = Option.valueOf("options");

    final OffersDataSourceProperties offersDataSourceProperties;

    public OffersDataSource(final OffersDataSourceProperties offersDataSourceProperties) {
        super();
        this.offersDataSourceProperties = offersDataSourceProperties;
    }

    @Override
    @Bean
    public ConnectionFactory connectionFactory() {
        final Map<String, String> options = new HashMap<>();
        options.put(POSTGRESQL_OPTION_READ_ONLY_TRX, offersDataSourceProperties.getReadOnly().toString());

        return ConnectionFactories.get(ConnectionFactoryOptions.builder()
            .option(PROTOCOL, offersDataSourceProperties.getProtocol())
            .option(DRIVER, POOLING_DRIVER)
            .option(HOST, offersDataSourceProperties.getHost())
            .option(PORT, offersDataSourceProperties.getPort())
            .option(USER, offersDataSourceProperties.getCredentials().getUser())
            .option(PASSWORD, offersDataSourceProperties.getCredentials().getPassword())
            .option(SSL, offersDataSourceProperties.getSsl().getEnabled())
            .option(POSTGRESQL_SSL_MODE, offersDataSourceProperties.getSsl().getMode())
            .option(DATABASE, offersDataSourceProperties.getDatabase())
            .option(INITIAL_SIZE, offersDataSourceProperties.getPool().getConnections().getMin())
            .option(MAX_SIZE, offersDataSourceProperties.getPool().getConnections().getMax())
            .option(VALIDATION_QUERY, VALIDATION_SQL)
            .option(POSTGRESQL_OPTIONS, options)
            .build());
    }
}
