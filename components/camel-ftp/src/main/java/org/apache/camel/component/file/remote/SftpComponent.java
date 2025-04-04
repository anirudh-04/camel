/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.component.file.remote;

import java.net.URI;
import java.util.Map;
import java.util.TreeMap;

import com.jcraft.jsch.JSch;
import org.apache.camel.CamelContext;
import org.apache.camel.api.management.ManagedOperation;
import org.apache.camel.api.management.ManagedResource;
import org.apache.camel.component.file.GenericFileEndpoint;
import org.apache.camel.spi.annotations.Component;
import org.apache.camel.util.StringHelper;

/**
 * Secure FTP Component
 */
@Component("sftp")
@ManagedResource(description = "Managed SFTP Component")
public class SftpComponent extends RemoteFileComponent<SftpRemoteFile> {

    public SftpComponent() {
    }

    public SftpComponent(CamelContext context) {
        super(context);
    }

    @Override
    protected GenericFileEndpoint<SftpRemoteFile> buildFileEndpoint(
            String uri, String remaining, Map<String, Object> parameters)
            throws Exception {
        // get the base uri part before the options as they can be non URI valid
        // such as the expression using $ chars
        // and the URI constructor will regard $ as an illegal character and we
        // dont want to enforce end users to
        // to escape the $ for the expression (file language)
        String baseUri = StringHelper.before(uri, "?", uri);

        // lets make sure we create a new configuration as each endpoint can
        // customize its own version
        SftpConfiguration config = new SftpConfiguration(new URI(baseUri));

        FtpUtils.ensureRelativeFtpDirectory(this, config);

        return new SftpEndpoint(uri, this, config);
    }

    @Override
    protected void afterPropertiesSet(GenericFileEndpoint<SftpRemoteFile> endpoint) throws Exception {
        // noop
    }

    @ManagedOperation(description = "Dump JSCH Configuration")
    public String dumpConfiguration() {
        StringBuilder sb = new StringBuilder();

        Map<String, String> map = new TreeMap<>(String::compareToIgnoreCase);
        map.putAll(JSch.getConfig());
        for (var e : map.entrySet()) {
            String v = e.getValue() != null ? e.getValue() : "";
            sb.append(String.format("%s = %s%n", e.getKey(), v));
        }

        return sb.toString();
    }

}
