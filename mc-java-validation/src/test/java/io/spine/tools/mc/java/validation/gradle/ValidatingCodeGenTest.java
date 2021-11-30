/*
 * Copyright 2019, TeamDev. All rights reserved.
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

package io.spine.tools.mc.java.validation.gradle;

import io.spine.tools.gradle.testing.GradleProject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;

import static io.spine.tools.gradle.task.JavaTaskName.compileJava;

@DisplayName("Validation code generation should")
class ValidatingCodeGenTest {

    /**
     * The name of the directory under {@code test/resource} which will be used for creating
     * the test project.
     */
    private static final String PROJECT_NAME = "validation-gen-plugin-test";

    private File projectDir;
    private GradleProject project;

    @BeforeEach
    void createProject(@TempDir Path tempDir) {
        projectDir = tempDir.toFile();
        project = newProject();
    }

    @Test
    @DisplayName("generate valid Java code")
    void generatingJavaCode() {
        project.executeTask(compileJava);
    }

    private GradleProject newProject() {
        GradleProject project = GradleProject.setupAt(projectDir)
                .fromResources(PROJECT_NAME)
                .create();
        return project;
    }
}
