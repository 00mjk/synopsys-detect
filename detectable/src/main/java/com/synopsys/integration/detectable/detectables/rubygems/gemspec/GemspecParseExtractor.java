/**
 * detectable
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.synopsys.integration.detectable.detectables.rubygems.gemspec;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.synopsys.integration.bdio.graph.DependencyGraph;
import com.synopsys.integration.detectable.extraction.Extraction;
import com.synopsys.integration.detectable.detectable.codelocation.CodeLocation;
import com.synopsys.integration.detectable.detectables.rubygems.gemspec.parse.GemspecParser;

public class GemspecParseExtractor {
    private final GemspecParser gemspecParser;

    public GemspecParseExtractor(final GemspecParser gemspecParser) {
        this.gemspecParser = gemspecParser;
    }

    public Extraction extract(final File gemspec, final boolean includeRuntime, final boolean includeDev) {
        try (final InputStream inputStream = new FileInputStream(gemspec)) {
            final DependencyGraph dependencyGraph = gemspecParser.parse(inputStream, includeRuntime, includeDev);
            final CodeLocation codeLocation = new CodeLocation(dependencyGraph);

            return new Extraction.Builder().success(codeLocation).build();
        } catch (final IOException e) {
            return new Extraction.Builder().exception(e).build();
        }
    }
}
