/*
 * Copyright 2019 Tango Controls
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

package org.tango.web.server.util;

import javax.ws.rs.core.UriBuilder;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 11/19/18
 */
public class TangoDeviceProxyUtils {
    private TangoDeviceProxyUtils(){}

    public static UriBuilder toUriBuilder(String fullName){
        return UriBuilder.fromPath("tango://").path(fullName);
    }

    public static UriBuilder toUriBuilder(String host, String name){
        return toUriBuilder(host + "/" + name);
    }
}
