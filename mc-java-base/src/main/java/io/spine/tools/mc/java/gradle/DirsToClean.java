/*
 * Copyright 2021, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Redistribution and use in source and/or binary forms, with or without
 * modification, must retain the above copyright notice and the following
 * disclaimer.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package io.spine.tools.mc.java.gradle;

import com.google.common.collect.ImmutableList;
import com.google.common.flogger.FluentLogger;
import io.spine.tools.java.fs.DefaultJavaPaths;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.gradle.api.Project;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static io.spine.tools.mc.java.gradle.McJavaOptions.def;
import static io.spine.tools.mc.java.gradle.McJavaOptions.getMcJavaOptions;
import static io.spine.util.Exceptions.newIllegalStateException;
import static java.util.stream.Collectors.toList;

/**
 * Calculates directories to be cleaned for a given project.
 */
public class DirsToClean {

    private static final FluentLogger log = FluentLogger.forEnclosingClass();

    /** Prevents instantiation of this utility class. */
    private DirsToClean() {
    }

    /**
     * Obtains directories to be cleaned in the given project.
     */
    public static List<File> getFor(Project project) {
        ImmutableList.Builder<File> result = ImmutableList.builder();
        result.addAll(tempArtifactDirsOf(project));
        List<File> dirs = fromOptionsOf(project);
        if (!dirs.isEmpty()) {
            log.atFine()
               .log("Found %d directories to clean: `%s`.", dirs.size(), dirs);
            result.addAll(dirs);
        } else {
            String defaultValue = def(project).generated().toString();
            log.atFine()
               .log("Default directory to clean: `%s`.", defaultValue);
            result.add(new File(defaultValue));
        }
        return result.build();
    }

    private static List<File> fromOptionsOf(Project project) {
        McJavaOptions options = getMcJavaOptions(project);
        List<File> dirs = options.dirsToClean
                    .stream()
                    .map(File::new)
                    .collect(toList());
        return dirs;
    }

    private static List<File> tempArtifactDirsOf(Project project) {
        List<File> result = new ArrayList<>();
        @Nullable File tempArtifactDir = tempArtifactsDirOf(project);
        @Nullable File tempArtifactDirOfRoot = tempArtifactsDirOf(project.getRootProject());
        if (tempArtifactDir != null) {
            result.add(tempArtifactDir);
            if (tempArtifactDirOfRoot != null
                    && !tempArtifactDir.equals(tempArtifactDirOfRoot)) {
                result.add(tempArtifactDirOfRoot);
            }
        }
        return result;
    }

    private static @Nullable File tempArtifactsDirOf(Project project) {
        File projectDir = canonicalDirOf(project);
        File tempArtifactsDir =
                DefaultJavaPaths.at(projectDir)
                                .tempArtifacts();
        if (tempArtifactsDir.exists()) {
            return tempArtifactsDir;
        } else {
            return null;
        }
    }

    private static File canonicalDirOf(Project project) {
        File result;
        File projectDir = project.getProjectDir();
        try {
            result = projectDir.getCanonicalFile();
        } catch (IOException e) {
            throw newIllegalStateException(
                    e, "Unable to obtain canonical project directory `%s`.", projectDir
            );
        }
        return result;
    }
}
