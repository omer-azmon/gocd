<#--
 * Copyright 2022 Thoughtworks, Inc.
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
 -->
<#assign title = "${presenter.buildName} Job Details - Go">
<#assign _page_title = "Job Details for ${presenter.buildLocatorForDisplay}">
<#assign current_tab = "build">
<#include "../shared/_header.ftl">

<#include "_build_detail_summary_jstemplate.ftl">
<div id="yui-main">
  <div class="yui-b">
    <!-- breadcrumbs -->
      <#assign current_page="build_detail">
      <#assign pipelineName="${presenter.pipelineName}">
      <#assign stageLocator="${presenter.stageLocator}">

      <#include "../shared/_job_details_breadcrumbs.ftl">
    <!-- /breadcrumbs -->

    <div class="content_wrapper_outer">
      <div class="row">
        <div class="content_wrapper_inner">
          <div id="build-status-panel" class="bd-container rounded-corner-for-pipeline">
            <div class="maincol build_detail">
                <#include "../shared/_flash_message.ftl">
                <#assign jobConfigName = "${presenter.buildName?c}">
              <div id="build_detail_summary_container" class="build_detail_summary">
                <ul id="build-detail-summary" class="summary">
                  <li><span class="header">Scheduled on: </span><span id="build_scheduled_date">Loading...</span></li>
                  <li><span class="header">Agent: </span><span id="agent_name">Loading...</span></li>
                  <li><span class="header">Completed on: </span><span id="build_completed_date">Loading...</span></li>
                  <li><span class="header">Build cause: </span><span
                    id="stage-${presenter.id?c}-buildCause">${presenter.buildCauseMessage?html}</span></li>
                  <li class="timer_area">
                    <div class="progress-info">
                      <div id="${presenter.buildName}_progress_bar" class="progress-bar" style="display: none;">
                        <div id="${presenter.buildName}_progress" class="progress"></div>
                      </div>
                      <div class="progress-eta">
                        <span class="header" id="${presenter.buildName}_time_remaining_label"></span>
                        <span id="${presenter.buildName}_time_remaining">&nbsp;</span>
                      </div>
                    </div>
                  </li>
                </ul>
                <div class="clear"></div>
              </div>

              <div class="job_details_content" data-pipeline="${presenter.pipelineName?c}" data-pipeline-counter="${presenter.pipelineCounter?c}" data-pipeline-label="${presenter.pipelineLabel?c}" data-stage="${presenter.stageName}" data-stage-counter="${presenter.stageCounter?c}" data-job="${presenter.id?c}" data-build="${presenter.buildName}" data-result="${presenter.result?c}" data-websocket="${websocketEnabled?string("enabled", "disabled")}">
                <div class="sub_tabs_container">
                  <ul>
                    <li class="current_tab" id="build_console">
                      <a class="tab_button_body_match_text">console</a>
                      <a>Console</a>
                    </li>
                    <li>
                      <a class="tab_button_body_match_text">tests</a>
                      <a>Tests</a>
                    </li>
                    <li>
                      <a class="tab_button_body_match_text">artifacts</a>
                      <a>Artifacts</a>
                    </li>
                    <li>
                      <a class="tab_button_body_match_text">materials</a>
                      <a>Materials</a>
                    </li>
                    <#list presenter.customizedTabs as tab>
                        <li>
                          <a class="tab_button_body_match_text">${tab.name?lower_case}</a>
                          <a>${tab.name}</a>
                        </li>
                    </#list>
                  </ul>
                  <a href="#" id="link-to-this-page"
                     title="The permanent link of this tab, you can share this url with other team members.">Link to
                    this
                    tab</a>

                  <div class="clear"></div>
                </div>

              <div class="sidebar_history">
                  <#include "../sidebar/_sidebar_build_list.ftl">
              </div>
                <div class="build_detail_container sub_tab_container rounded-corner-for-tab-container">


                  <div class="sub_tab_container_content">
                    <div class="clear"></div>

                      <#assign buildoutput_extra_attrs="">
                      <#include "_buildoutput.ftl">

                      <#assign tests_extra_attrs="style='display:none'">
                      <#include "_tests.ftl">

                      <#assign artifacts_extra_attrs="style='display:none'">
                      <#include "_artifacts.ftl">

                      <#assign modification_extra_attrs="style='display:none'">
                      <#include "_materials.ftl">
                      <#list presenter.customizedTabs as tab>
                        <#assign customized_name="${tab.name}">
                        <#assign customized_path="${tab.path}">
                        <#include "_customized.ftl">
                      </#list>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</div>
</div>
<#include "../shared/_footer.ftl">
