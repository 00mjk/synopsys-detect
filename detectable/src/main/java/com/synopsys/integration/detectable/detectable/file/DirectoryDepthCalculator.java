package com.synopsys.integration.detectable.detectable.file;

import java.io.File;
import java.nio.file.Files;

public class DirectoryDepthCalculator {

    private DirectoryDepthCalculator() {

    }

    public static final int calculateDepth(File directoryToStart) {
        return calculateDepth(directoryToStart, 0);
    }

    private static final int calculateDepth(File directoryToStart, int currentDepth) {
        File[] allFiles = directoryToStart.listFiles();
        if (allFiles == null) {
            return currentDepth;
        }
        if (0 == allFiles.length || currentDepth == Integer.MAX_VALUE) {
            return currentDepth;
        }
        int maxSubDirectoryDepth = 0;
        for (File file : allFiles) {
            if (file.isDirectory() && !Files.isSymbolicLink(file.toPath())) {
                int subDirectoryDepth = calculateDepth(file, 1 + currentDepth);
                maxSubDirectoryDepth = Math.max(maxSubDirectoryDepth, subDirectoryDepth);
            }
        }
        return Math.max(currentDepth, maxSubDirectoryDepth);
    }
}
