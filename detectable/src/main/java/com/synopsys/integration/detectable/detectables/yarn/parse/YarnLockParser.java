/**
 * detectable
 *
 * Copyright (c) 2020 Synopsys, Inc.
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
package com.synopsys.integration.detectable.detectables.yarn.parse;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class YarnLockParser {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private static final String COMMENT_PREFIX = "#";
    private static final String VERSION_PREFIX = "version \"";
    private static final String VERSION_SUFFIX = "\"";
    private static final String OPTIONAL_DEPENDENCIES_TOKEN = "optionalDependencies:";

    public YarnLock parseYarnLock(List<String> yarnLockFileAsList) {
        List<YarnLockEntry> entries = new ArrayList<>();
        String resolvedVersion = "";
        List<YarnLockDependency> dependencies = new ArrayList<>();
        List<YarnLockEntryId> ids;
        boolean inOptionalDependencies = false;

        List<String> cleanedYarnLockFileAsList = cleanList(yarnLockFileAsList);

        int indexOfFirstLevelZeroLine = findIndexOfFirstLevelZeroLine(cleanedYarnLockFileAsList);

        if (indexOfFirstLevelZeroLine == -1 || indexOfFirstLevelZeroLine == cleanedYarnLockFileAsList.size() - 1) {
            return new YarnLock(entries);
        }

        // We need to set ids with the first level zero line
        ids = parseMultipleEntryLine(cleanedYarnLockFileAsList.get(indexOfFirstLevelZeroLine));

        List<String> yarnLinesThatMatter = cleanedYarnLockFileAsList.subList(indexOfFirstLevelZeroLine + 1, cleanedYarnLockFileAsList.size());

        for (String line : yarnLinesThatMatter) {

            String trimmedLine = line.trim();
            int level = countIndent(line);
            if (level == 0) {
                entries.add(new YarnLockEntry(ids, resolvedVersion, dependencies));
                resolvedVersion = "";
                dependencies = new ArrayList<>();
                inOptionalDependencies = false;
                ids = parseMultipleEntryLine(line);
            } else if (level == 1 && trimmedLine.startsWith(VERSION_PREFIX)) {
                resolvedVersion = parseVersionFromLine(trimmedLine);
            } else if (level == 1 && trimmedLine.startsWith(OPTIONAL_DEPENDENCIES_TOKEN)) {
                inOptionalDependencies = true;
            } else if (level == 2) {
                Optional<YarnLockDependency> dep = parseDependencyFromLine(trimmedLine, inOptionalDependencies);
                if (dep.isPresent()) {
                    dependencies.add(dep.get());
                }
            }
        }
        if (StringUtils.isNotBlank(resolvedVersion)) {
            entries.add(new YarnLockEntry(ids, resolvedVersion, dependencies));
        }

        return new YarnLock(entries);
    }

    @NotNull
    private Integer findIndexOfFirstLevelZeroLine(List<String> cleanedYarnLockFileAsList) {
        return cleanedYarnLockFileAsList
                   .stream()
                   .filter(line -> countIndent(line) == 0)
                   .findFirst()
                   .map(line -> cleanedYarnLockFileAsList.indexOf(line))
                   .orElse(-1);
    }

    @NotNull
    private List<String> cleanList(List<String> yarnLockFileAsList) {
        return yarnLockFileAsList
                   .stream()
                   .filter(StringUtils::isNotBlank)
                   .filter(line -> !line.trim().startsWith(COMMENT_PREFIX))
                   .collect(Collectors.toList());
    }

    public int countIndent(String line) {
        int count = 0;
        while (line.startsWith("  ")) {
            count++;
            line = line.substring(2);
        }
        return count;
    }

    private Optional<YarnLockDependency> parseDependencyFromLine(String line, boolean optional) {
        String[] pieces = StringUtils.split(line, " ", 2);
        if (pieces.length < 2) {
            logger.debug(String.format("Unable to parse dependency from Yarn lock line: '%s'", line));
            return Optional.empty();
        }
        YarnLockDependency dep = new YarnLockDependency(removeWrappingQuotes(pieces[0]), removeWrappingQuotes(pieces[1]), optional);
        return Optional.of(dep);
    }

    private String removeWrappingQuotes(String s) {
        return StringUtils.removeStart(StringUtils.removeEnd(s.trim(), "\""), "\"");
    }

    //Takes a line of the form "entry \"entry\" entry:"
    public List<YarnLockEntryId> parseMultipleEntryLine(String line) {
        List<YarnLockEntryId> ids = new ArrayList<>();
        String[] entries = line.split(",");
        for (String entryRaw : entries) {
            String entryNoColon = StringUtils.removeEnd(entryRaw.trim(), ":");
            String entryNoColonOrQuotes = removeWrappingQuotes(entryNoColon);
            YarnLockEntryId entry = parseSingleEntry(entryNoColonOrQuotes);
            ids.add(entry);
        }
        return ids;
    }

    //Takes an entry of format "name@version" or "@name@version" where name has an @ symbol.
    public YarnLockEntryId parseSingleEntry(String entry) {
        if (StringUtils.countMatches(entry, "@") == 1 && entry.startsWith("@")) {
            return new YarnLockEntryId(entry, "");
        } else {
            String name = StringUtils.substringBeforeLast(entry, "@");
            String version = StringUtils.substringAfterLast(entry, "@");
            return new YarnLockEntryId(name, version);
        }
    }

    private String parseVersionFromLine(String line) {
        String rawVersion = line.substring(VERSION_PREFIX.length(), line.lastIndexOf(VERSION_SUFFIX));
        return removeWrappingQuotes(rawVersion);
    }
}