/*
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
package io.trino.plugin.openlineage;

import com.google.inject.Injector;
import com.google.inject.Scopes;
import io.airlift.bootstrap.Bootstrap;
import io.airlift.json.JsonModule;
import io.airlift.log.Logger;
import io.trino.spi.eventlistener.EventListener;
import io.trino.spi.eventlistener.EventListenerFactory;

import java.util.Map;

import static io.airlift.configuration.ConfigBinder.configBinder;

public class OpenLineageListenerFactory
        implements EventListenerFactory
{
    private static final Logger logger = Logger.get(OpenLineageListenerFactory.class);

    @Override
    public String getName()
    {
        return "openlineage";
    }

    @Override
    public EventListener create(Map<String, String> config)
    {
        Bootstrap app = new Bootstrap(
                        new JsonModule(),
                        binder -> {
                            configBinder(binder).bindConfig(OpenLineageListenerConfig.class);
                            configBinder(binder).bindConfig(OpenLineageClientConfig.class);
                            binder.bind(OpenLineageListener.class).in(Scopes.SINGLETON);
                        });

        Injector injector = app
                .doNotInitializeLogging()
                .setRequiredConfigurationProperties(config)
                .initialize();

        return injector.getInstance(OpenLineageListener.class);
    }
}
