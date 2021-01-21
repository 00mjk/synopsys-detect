package com.synopsys.integration.detectable.file.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.condition.OS.WINDOWS;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.condition.DisabledOnOs;

import com.synopsys.integration.detectable.annotations.UnitTest;
import com.synopsys.integration.detectable.detectable.file.DirectoryDepthCalculator;

public class DirectoryDepthCalculatorTest {

    private static Path initialDirectoryPath;

    @BeforeAll
    public static void setup() throws IOException {
        initialDirectoryPath = Files.createTempDirectory("DirectoryDepthCalculatorTest");
    }

    @AfterAll
    public static void cleanup() {
        initialDirectoryPath.toFile().delete();
    }

    @UnitTest
    @DisabledOnOs(WINDOWS)
    public void testCalculatingDepth() throws IOException {
        // Create a subDir with a symlink that loops back to its parent called linkToInitial
        // Directory visualization for depth test
        //        initialDir
        //            |
        //           sub
        //            |
        //       _____|___________
        //       |               |
        // linkToInitial     regularDir
        //                       |
        //              _________|___________
        //              |                   |
        //       regularSubDir1      regularSubDir2
        //              |
        //       regularSubDir3

        File initialDirectory = initialDirectoryPath.toFile();
        File subDir = new File(initialDirectory, "sub");
        subDir.mkdirs();
        File link = new File(subDir, "linkToInitial");
        Path linkPath = link.toPath();
        Files.createSymbolicLink(linkPath, initialDirectoryPath);

        File regularDir = new File(subDir, "regularDir");
        regularDir.mkdir();
        File regularFile = new File(subDir, "regularFile");
        regularFile.createNewFile();
        File regularSubDir1 = new File(regularDir, "regularSubDir1");
        regularSubDir1.mkdir();
        File regularSubDir2 = new File(regularDir, "regularSubDir2");
        regularSubDir2.mkdir();
        File regularSubDir3 = new File(regularSubDir1, "regularSubDir3");
        regularSubDir3.mkdir();
        int depth = DirectoryDepthCalculator.calculateDepth(initialDirectory);
        assertEquals(4, depth);
    }
}
