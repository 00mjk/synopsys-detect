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
package com.synopsys.integration.detectable.detectables.conan.lockfile;

import java.io.File;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.FileUtils;

import com.synopsys.integration.detectable.detectables.conan.ConanDetectableResult;
import com.synopsys.integration.detectable.detectables.conan.lockfile.parser.ConanLockfileParser;
import com.synopsys.integration.detectable.extraction.Extraction;

public class ConanLockfileExtractor {
    private final ConanLockfileParser conanLockfileParser;

    public ConanLockfileExtractor(ConanLockfileParser conanLockfileParser) {
        this.conanLockfileParser = conanLockfileParser;
    }

    public Extraction extract(File lockfile, ConanLockfileExtractorOptions conanLockfileExtractorOptions) {
        try {
            String conanLockfileContents = FileUtils.readFileToString(lockfile, StandardCharsets.UTF_8);
            ConanDetectableResult result = conanLockfileParser.generateCodeLocationFromConanLockfileContents(
                conanLockfileContents,
                conanLockfileExtractorOptions.shouldIncludeDevDependencies(),
                conanLockfileExtractorOptions.preferLongFormExternalIds());
            return new Extraction.Builder()
                       .success(result.getCodeLocation())
                       .projectName(result.getProjectName())
                       .projectVersion(result.getProjectVersion())
                       .build();
        } catch (Exception e) {
            return new Extraction.Builder().exception(e).build();
        }
    }
}