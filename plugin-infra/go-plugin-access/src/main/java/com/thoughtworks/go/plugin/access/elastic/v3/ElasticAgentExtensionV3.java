/*
 * Copyright 2018 ThoughtWorks, Inc.
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

package com.thoughtworks.go.plugin.access.elastic.v3;

import com.thoughtworks.go.domain.JobIdentifier;
import com.thoughtworks.go.plugin.access.DefaultPluginInteractionCallback;
import com.thoughtworks.go.plugin.access.PluginRequestHelper;
import com.thoughtworks.go.plugin.access.elastic.VersionedElasticAgentExtension;
import com.thoughtworks.go.plugin.access.elastic.models.AgentMetadata;
import com.thoughtworks.go.plugin.api.response.validation.ValidationResult;
import com.thoughtworks.go.plugin.domain.common.PluginConfiguration;
import com.thoughtworks.go.plugin.domain.elastic.Capabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

import static com.thoughtworks.go.plugin.access.elastic.v3.ElasticAgentPluginConstantsV3.*;

public class ElasticAgentExtensionV3 implements VersionedElasticAgentExtension {
    private static final Logger LOG = LoggerFactory.getLogger(ElasticAgentExtensionV3.class);
    public static final String VERSION = "3.0";
    private final PluginRequestHelper pluginRequestHelper;
    private final ElasticAgentExtensionConverterV3 elasticAgentExtensionConverterV3;

    public ElasticAgentExtensionV3(PluginRequestHelper pluginRequestHelper) {
        this.pluginRequestHelper = pluginRequestHelper;
        this.elasticAgentExtensionConverterV3 = new ElasticAgentExtensionConverterV3();
    }

    @Override
    public com.thoughtworks.go.plugin.domain.common.Image getIcon(String pluginId) {
        return pluginRequestHelper.submitRequest(pluginId, REQUEST_GET_PLUGIN_SETTINGS_ICON, new DefaultPluginInteractionCallback<com.thoughtworks.go.plugin.domain.common.Image>() {
            @Override
            public com.thoughtworks.go.plugin.domain.common.Image onSuccess(String responseBody, String resolvedExtensionVersion) {
                return elasticAgentExtensionConverterV3.getImageResponseFromBody(responseBody);
            }
        });
    }

    @Override
    public Capabilities getCapabilities(String pluginId) {
        return pluginRequestHelper.submitRequest(pluginId, REQUEST_CAPABILITIES, new DefaultPluginInteractionCallback<Capabilities>() {
            @Override
            public Capabilities onSuccess(String responseBody, String resolvedExtensionVersion) {
                return elasticAgentExtensionConverterV3.getCapabilitiesFromResponseBody(responseBody);
            }
        });
    }

    @Override
    public List<PluginConfiguration> getElasticProfileMetadata(String pluginId) {
        return pluginRequestHelper.submitRequest(pluginId, REQUEST_GET_PROFILE_METADATA, new DefaultPluginInteractionCallback<List<PluginConfiguration>>() {
            @Override
            public List<PluginConfiguration> onSuccess(String responseBody, String resolvedExtensionVersion) {
                return elasticAgentExtensionConverterV3.getElasticProfileMetadataResponseFromBody(responseBody);
            }
        });
    }

    @Override
    public String getElasticProfileView(String pluginId) {
        return pluginRequestHelper.submitRequest(pluginId, REQUEST_GET_PROFILE_VIEW, new DefaultPluginInteractionCallback<String>() {
            @Override
            public String onSuccess(String responseBody, String resolvedExtensionVersion) {
                return elasticAgentExtensionConverterV3.getProfileViewResponseFromBody(responseBody);
            }
        });
    }

    @Override
    public ValidationResult validateElasticProfile(final String pluginId, final Map<String, String> configuration) {
        return pluginRequestHelper.submitRequest(pluginId, REQUEST_VALIDATE_PROFILE, new DefaultPluginInteractionCallback<ValidationResult>() {
            @Override
            public String requestBody(String resolvedExtensionVersion) {
                return elasticAgentExtensionConverterV3.validateElasticProfileRequestBody(configuration);
            }

            @Override
            public ValidationResult onSuccess(String responseBody, String resolvedExtensionVersion) {
                return elasticAgentExtensionConverterV3.getElasticProfileValidationResultResponseFromBody(responseBody);
            }
        });
    }


    @Override
    public void createAgent(String pluginId, final String autoRegisterKey, final String environment, final Map<String, String> configuration, JobIdentifier jobIdentifier) {
        pluginRequestHelper.submitRequest(pluginId, REQUEST_CREATE_AGENT, new DefaultPluginInteractionCallback<Void>() {
            @Override
            public String requestBody(String resolvedExtensionVersion) {
                return elasticAgentExtensionConverterV3.createAgentRequestBody(autoRegisterKey, environment, configuration, jobIdentifier);
            }
        });
    }

    @Override
    public void serverPing(final String pluginId) {
        pluginRequestHelper.submitRequest(pluginId, REQUEST_SERVER_PING, new DefaultPluginInteractionCallback<Void>());
    }

    @Override
    public boolean shouldAssignWork(String pluginId, final AgentMetadata agent, final String environment, final Map<String, String> configuration, JobIdentifier identifier) {
        return pluginRequestHelper.submitRequest(pluginId, REQUEST_SHOULD_ASSIGN_WORK, new DefaultPluginInteractionCallback<Boolean>() {
            @Override
            public String requestBody(String resolvedExtensionVersion) {
                return elasticAgentExtensionConverterV3.shouldAssignWorkRequestBody(agent, environment, configuration, identifier);
            }

            @Override
            public Boolean onSuccess(String responseBody, String resolvedExtensionVersion) {
                return elasticAgentExtensionConverterV3.shouldAssignWorkResponseFromBody(responseBody);
            }
        });
    }

    @Override
    public String getPluginStatusReport(String pluginId) {
        return pluginRequestHelper.submitRequest(pluginId, REQUEST_STATUS_REPORT, new DefaultPluginInteractionCallback<String>() {
            @Override
            public String onSuccess(String responseBody, String resolvedExtensionVersion) {
                return elasticAgentExtensionConverterV3.getStatusReportView(responseBody);
            }
        });
    }

    @Override
    public String getAgentStatusReport(String pluginId, JobIdentifier identifier, String elasticAgentId) {
        return pluginRequestHelper.submitRequest(pluginId, REQUEST_AGENT_STATUS_REPORT, new DefaultPluginInteractionCallback<String>() {
            @Override
            public String requestBody(String resolvedExtensionVersion) {
                return elasticAgentExtensionConverterV3.getAgentStatusReportRequestBody(identifier, elasticAgentId);
            }

            @Override
            public String onSuccess(String responseBody, String resolvedExtensionVersion) {
                return elasticAgentExtensionConverterV3.getStatusReportView(responseBody);
            }
        });
    }

    @Override
    public void jobCompletion(String pluginId, String elasticAgentId, JobIdentifier jobIdentifier) {
        LOG.debug("Plugin: '{}' uses elastic agent extension v3 and job completion is not supported by elastic agent V3", pluginId);
    }
}
