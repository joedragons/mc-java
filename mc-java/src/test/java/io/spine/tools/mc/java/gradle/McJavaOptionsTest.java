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

import io.spine.testing.TempDir;
import io.spine.tools.mc.java.gradle.given.StubProject;
import org.gradle.api.Project;
import org.gradle.api.artifacts.dsl.RepositoryHandler;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.File;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.tools.mc.java.StandardRepos.applyStandard;
import static io.spine.tools.mc.java.gradle.McJavaOptions.getGeneratedMainRejectionsDir;
import static io.spine.tools.mc.java.gradle.McJavaOptions.getGeneratedMainResourcesDir;
import static io.spine.tools.mc.java.gradle.McJavaOptions.getGeneratedTestResourcesDir;
import static io.spine.tools.mc.java.gradle.given.ModelCompilerTestEnv.MC_JAVA_GRADLE_PLUGIN_ID;
import static io.spine.tools.mc.java.gradle.given.ModelCompilerTestEnv.newUuid;
import static org.junit.jupiter.api.Assertions.assertFalse;

@DisplayName("`McJavaExtension` should")
class McJavaOptionsTest {

    private static Project project = null;
    private static McJavaOptions extension = null;

    @BeforeAll
    static void setUp() {
        File projectDir = TempDir.forClass(McJavaOptionsTest.class);
        project = StubProject.createAt(projectDir);
        RepositoryHandler repositories = project.getRepositories();
        applyStandard(repositories);
        repositories.mavenLocal();
        repositories.mavenCentral();
        project.getPluginManager()
               .apply(MC_JAVA_GRADLE_PLUGIN_ID);
        extension = McJavaOptions.extension(project);
    }

    @Nested
    @DisplayName("for `mainTargetGenResourcesDir` return")
    class MainTargetGenResourceDir {

        @Test
        @DisplayName("default value, if not set")
        void defaultValue() {
            String dir = getGeneratedMainResourcesDir(project);

            assertNotEmptyAndIsInProjectDir(dir);
        }

        @Test
        @DisplayName("specified value, if set")
        void setValue() {
            extension.generatedMainResourcesDir = newUuid();

            String dir = getGeneratedMainResourcesDir(project);

            assertThat(dir)
                    .isEqualTo(extension.generatedMainResourcesDir);
        }
    }

    @Nested
    @DisplayName("for `testTargetGenResourcesDir` return")
    class TestTargetGenResourcesDir {

        @Test
        @DisplayName("default value, if not set")
        void defaultValue() {
            String dir = getGeneratedTestResourcesDir(project);

            assertNotEmptyAndIsInProjectDir(dir);
        }

        @Test
        @DisplayName("specified value, if set")
        void specifiedValue() {
            extension.generatedTestResourcesDir = newUuid();

            String dir = getGeneratedTestResourcesDir(project);

            assertThat(dir)
                    .isEqualTo(extension.generatedTestResourcesDir);
        }
    }

    @Nested
    @DisplayName("for `targetGenRejectionsRootDir` return")
    class TargetGenRejectionsRootDir {

        @Test
        @DisplayName("default value, if not set")
        void defaultValue() {
            String dir = getGeneratedMainRejectionsDir(project);

            assertNotEmptyAndIsInProjectDir(dir);
        }

        @Test
        @DisplayName("specified value, if set")
        void specifiedValue() {
            extension.generatedMainRejectionsDir = newUuid();

            String dir = getGeneratedMainRejectionsDir(project);

            assertThat(dir)
                    .isEqualTo(extension.generatedMainRejectionsDir);
        }
    }

    private static void assertNotEmptyAndIsInProjectDir(String path) {
        assertFalse(path.trim()
                        .isEmpty());
        assertThat(path)
                .startsWith(project.getProjectDir().getAbsolutePath());
    }
}
