/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.luuuis.myzone.filter;

import com.atlassian.jira.ComponentManager;
import com.atlassian.plugin.webresource.WebResourceManager;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 * This filter simply attaches our JS resources to JIRA screens. This is necessary because the version of plugins
 * included in JIRA 4.1 doesn't support web-resource contexts.
 * <p/>
 * This technique shamelessly copied from <a href="https://extranet.atlassian.com/x/zwaebg">Pawel Niewiadomski</a>.
 */
public class RequireJsResourcesFilter implements Filter
{
    public void init(FilterConfig filterConfig) throws ServletException
    {
        // empty
    }

    public void destroy()
    {
        // empty
    }

    public void doFilter(final ServletRequest servletRequest, final ServletResponse servletResponse, final FilterChain filterChain)
            throws IOException, ServletException
    {
        WebResourceManager webResourceManager = ComponentManager.getInstance().getWebResourceManager();

        // TODO move into filter params
        webResourceManager.requireResource("com.luuuis.jira-myzone-plugin:jira-myzone");
        filterChain.doFilter(servletRequest, servletResponse);
    }
}
