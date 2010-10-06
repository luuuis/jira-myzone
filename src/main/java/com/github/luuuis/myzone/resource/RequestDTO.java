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
package com.github.luuuis.myzone.resource;

import java.util.Arrays;
import java.util.Iterator;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * A request DTO.
 */
@XmlRootElement
class RequestDTO implements Iterable<String>
{
    @XmlElement (name = "times")
    String[] times = new String[0];

    RequestDTO()
    {
    }

    RequestDTO(String[] times)
    {
        this.times = times;
    }

    public Iterator<String> iterator()
    {
        return Arrays.asList(times).iterator();
    }

    @Override
    public String toString()
    {
        return "RequestDTO{times=" + (times == null ? null : Arrays.asList(times)) + '}';
    }

    public int length()
    {
        return times.length;
    }
}
