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

import java.util.HashMap;
import java.util.Map;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * A response DTO.
 */
@XmlRootElement
class ResponseDTO
{
    @XmlElement(name = "label")
    String label;

    @XmlElement(name = "times")
    HashMap<String, String> times;

    public ResponseDTO()
    {
    }

    public ResponseDTO(String label, Map<String, String> times)
    {
        this.label = label;
        this.times = times != null ? new HashMap<String, String>(times) : new HashMap<String, String>();
    }

    @Override
    public String toString()
    {
        return "ResponseDTO{label='" + label + '\'' + ", times=" + times + '}';
    }
}
