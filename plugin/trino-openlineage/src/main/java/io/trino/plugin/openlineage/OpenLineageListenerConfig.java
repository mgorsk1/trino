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

import io.airlift.configuration.Config;
import io.airlift.configuration.ConfigDescription;
import io.trino.spi.resourcegroups.QueryType;

import java.util.Arrays;
import java.util.List;

public class OpenLineageListenerConfig
{
    private String trinoHost = "localhost";
    private Integer trinoPort = 8080;
    private boolean metadataFacetEnabled = true;
    private boolean queryContextFacetEnabled = true;
    private boolean queryStatisticsFacetEnabled = true;
    private List<QueryType> includeQueryTypes = Arrays.asList(QueryType.DELETE,
            QueryType.INSERT, QueryType.MERGE, QueryType.UPDATE, QueryType.ALTER_TABLE_EXECUTE);

    @ConfigDescription("Hostname of trino server. Used for namespace rendering.")
    @Config("openlineage-event-listener.trino-host")
    public OpenLineageListenerConfig setTrinoHost(String trinoHost)
    {
        this.trinoHost = trinoHost;
        return this;
    }

    public String getTrinoHost()
    {
        return trinoHost;
    }

    @ConfigDescription("Port of trino server. Used for namespace rendering.")
    @Config("openlineage-event-listener.trino-port")
    public OpenLineageListenerConfig setTrinoPort(Integer trinoPort)
    {
        this.trinoPort = trinoPort;
        return this;
    }

    public Integer getTrinoPort()
    {
        return trinoPort;
    }

    @ConfigDescription("Should metadata facet be added to run facet.")
    @Config("openlineage-event-listener.facets-metadata-enabled")
    public OpenLineageListenerConfig setMetadataFacetEnabled(Boolean metadataFacetEnabled)
    {
        this.metadataFacetEnabled = metadataFacetEnabled;
        return this;
    }

    public boolean isMetadataFacetEnabled()
    {
        return metadataFacetEnabled;
    }

    @ConfigDescription("Should query context facet be added to run facet.")
    @Config("openlineage-event-listener.facets-query-context-enabled")
    public OpenLineageListenerConfig setQueryContextFacetEnabled(Boolean queryContextFacetEnabled)
    {
        this.queryContextFacetEnabled = queryContextFacetEnabled;
        return this;
    }

    public boolean isQueryContextFacetEnabled()
    {
        return queryContextFacetEnabled;
    }

    @ConfigDescription("Should query statistics facet be added to run facet.")
    @Config("openlineage-event-listener.facets-query-statistics-enabled")
    public OpenLineageListenerConfig setQueryStatisticsFacetEnabled(Boolean queryStatisticsFacetEnabled)
    {
        this.queryStatisticsFacetEnabled = queryStatisticsFacetEnabled;
        return this;
    }

    public boolean isQueryStatisticsFacetEnabled()
    {
        return queryStatisticsFacetEnabled;
    }

    @ConfigDescription("Which query types emitted by Trino should generate OpenLineage events. Other query types will be filtered out.")
    @Config("openlineage-event-listener.include-query-types")
    public OpenLineageListenerConfig setIncludeQueryTypes(String includeQueryTypes)
    {
        this.includeQueryTypes = Arrays.stream(includeQueryTypes.split(","))
                .map(String::trim)
                .map(QueryType::valueOf)
                .toList();
        return this;
    }

    public List<QueryType> getIncludeQueryTypes()
    {
        return includeQueryTypes;
    }
}
