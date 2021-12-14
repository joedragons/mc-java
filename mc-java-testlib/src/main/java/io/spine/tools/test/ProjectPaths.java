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

package io.spine.tools.test;

import java.nio.file.Path;

/**
 * A factory of paths to conventional locations in a Gradle project.
 */
public final class ProjectPaths {

    /**
     * Prevents the utility class instantiation.
     */
    private ProjectPaths() {
    }

    /**
     * Obtains the path to the generated sources.
     *
     * @param projectDir
     *         the absolute path to the project
     * @param sourceSetName
     *         the name of the source set
     * @param generatorName
     *         the name of the source code generator
     * @return a path within the {@code projectDir}
     */
    public static Path protobufGeneratedDir(Path projectDir,
                                            String sourceSetName,
                                            String generatorName) {
        return projectDir.resolve("build")
                         .resolve("generated-proto")
                         .resolve(sourceSetName)
                         .resolve(generatorName);
    }

    /**
     * Obtains the path to the main-scope generated Java sources.
     *
     * @param projectDir
     *         the absolute path to the project
     * @return a path within the {@code projectDir}
     */
    public static Path protobufGeneratedDir(Path projectDir) {
        return protobufGeneratedDir(projectDir, "main", "java");
    }
}
