/*
 * Copyright 2022, TeamDev. All rights reserved.
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

package io.spine.tools.mc.java.rejection.gradle;

import io.spine.testing.TempDir;
import io.spine.tools.gradle.testing.GradleProject;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Path;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.tools.fs.DirectoryName.spine;
import static io.spine.tools.gradle.task.JavaTaskName.compileTestJava;
import static io.spine.tools.test.ProjectPaths.protobufGeneratedDir;
import static java.lang.String.format;
import static java.nio.file.Files.exists;
import static org.gradle.api.tasks.SourceSet.MAIN_SOURCE_SET_NAME;
import static org.gradle.api.tasks.SourceSet.TEST_SOURCE_SET_NAME;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("`RejectionGenPlugin` should")
class RejectionGenPluginTest {

    private static @MonotonicNonNull File projectDir = null;

    @BeforeAll
    static void generateRejections() {
        projectDir = TempDir.forClass(RejectionGenPluginTest.class);

        var project = GradleProject.setupAt(projectDir)
                .fromResources("rejections-gen-plugin-test")
                .copyBuildSrc()
                .create();
        // Executing the `compileTestJava` task should generate rejection types from both
        // `test` and `main` source sets.
        project.executeTask(compileTestJava);
    }

    @Nested
    @DisplayName("place generated code under the `spine` directory for")
    class GeneratedRoot {

        @Test
        @DisplayName("`main` source set")
        void mainDir() {
            assertExists(targetMainDir());
        }

        @Test
        @DisplayName("`test` source set")
        void testDir() {
            assertExists(targetTestDir());
        }
    }

    private static Path generatedRoot(String sourceSetName) {
        checkNotNull(projectDir);
        return protobufGeneratedDir(projectDir.toPath(), sourceSetName, spine.name());
    }
    private static Path targetMainDir() {
        return generatedRoot(MAIN_SOURCE_SET_NAME);
    }

    private static Path targetTestDir() {
        return generatedRoot(TEST_SOURCE_SET_NAME);
    }

    @Nested
    @DisplayName("use the package specified in proto file options")
    class PackageDir {

        @Test
        @DisplayName("for 'main' source set")
        void mainPackageName() {
            // As defined in `resources/.../main_rejections.proto`.
            var packageDir = targetMainDir().resolve("io/spine/sample/rejections");
            assertExists(packageDir);

            // As defined in `resources/.../main_rejections.proto`.
            assertJavaFileExists(packageDir, "Rejection1");
            assertJavaFileExists(packageDir, "Rejection2");
            assertJavaFileExists(packageDir, "Rejection3");
            assertJavaFileExists(packageDir, "Rejection4");
            assertJavaFileExists(packageDir, "RejectionWithRepeatedField");
            assertJavaFileExists(packageDir, "RejectionWithMapField");
        }

        @Test
        @DisplayName("for 'test' source set")
        void testPackageName() {
            // As defined in `resources/.../test_rejections.proto`.
            var packageDir = targetTestDir().resolve("io/spine/sample/rejections");
            assertExists(packageDir);

            // As defined in `resources/.../test_rejections.proto`.
            assertJavaFileExists(packageDir, "TestRejection1");
            assertJavaFileExists(packageDir, "TestRejection2");
        }
    }

    private static void assertExists(Path path) {
        assertTrue(exists(path), () -> format("The path `%s` is expected to exist.", path));
    }

    private static void assertJavaFileExists(Path packageDir, String typeName) {
        var file = packageDir.resolve(typeName + ".java");
        assertExists(file);
    }
}
