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

package io.spine.tools.mc.java.rejection.gradle;

import io.spine.code.java.SimpleClassName;
import io.spine.protobuf.Messages;
import io.spine.testing.TempDir;
import io.spine.tools.code.SourceSetName;
import io.spine.tools.gradle.testing.GradleProject;
import io.spine.tools.java.code.BuilderSpec;
import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaDocCapableSource;
import org.jboss.forge.roaster.model.source.MethodSource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.tools.mc.java.gradle.McJavaTaskName.generateRejections;
import static io.spine.tools.mc.java.rejection.gradle.TestEnv.expectedBuilderClassComment;
import static io.spine.tools.mc.java.rejection.gradle.TestEnv.expectedClassComment;
import static io.spine.tools.mc.java.rejection.gradle.TestEnv.expectedFirstFieldComment;
import static io.spine.tools.mc.java.rejection.gradle.TestEnv.expectedSecondFieldComment;
import static io.spine.tools.mc.java.rejection.gradle.TestEnv.rejectionWithJavadoc;
import static io.spine.tools.mc.java.rejection.gradle.TestEnv.rejectionsJavadocThrowableSource;
import static io.spine.util.Exceptions.newIllegalStateException;

@DisplayName("Rejection code generator should")
class RejectionJavadocTest {

    private static JavaClassSource generatedSource = null;

    @BeforeAll
    static void generateSources() throws IOException {
        var projectDir = TempDir.forClass(RejectionJavadocTest.class);
        var project = GradleProject.setupAt(projectDir)
                .copyBuildSrc()
                .fromResources("rejection-javadoc-test") // Contains `build.gradle.kts`
                .addFile("src/main/proto/javadoc_rejections.proto", rejectionWithJavadoc())
                .create();
        project.executeTask(generateRejections(SourceSetName.main));
        var generatedFile = rejectionsJavadocThrowableSource(projectDir.toPath()).toFile();
        generatedSource = Roaster.parse(JavaClassSource.class, generatedFile);
    }

    @Nested
    @DisplayName("generate Javadoc for")
    class GenerateJavadoc {

        @Test
        @DisplayName("rejection type")
        void forRejection() {
            assertRejectionJavadoc(generatedSource);
        }

        @Test
        @DisplayName("`Builder` of rejection")
        void forBuilder() {
            var builderTypeName = SimpleClassName.ofBuilder().value();
            var builderType = generatedSource.getNestedType(builderTypeName);
            assertBuilderJavadoc((JavaClassSource) builderType);
        }
    }

    private static void assertRejectionJavadoc(JavaClassSource rejection) {
        assertDoc(expectedClassComment(), rejection);
        assertMethodDoc("@return a new builder for the rejection", rejection,
                        Messages.METHOD_NEW_BUILDER
        );
    }

    private static void assertBuilderJavadoc(JavaClassSource builder) {
        assertDoc(expectedBuilderClassComment(), builder);
        assertMethodDoc(
                "Creates the rejection from the builder and validates it.", builder,
                BuilderSpec.BUILD_METHOD_NAME
        );
        assertMethodDoc(expectedFirstFieldComment(), builder, "setId");
        assertMethodDoc(expectedSecondFieldComment(), builder, "setRejectionMessage");
    }

    private static void assertMethodDoc(String expectedComment,
                                        JavaClassSource source,
                                        String methodName) {
        var method = findMethod(source, methodName);
        assertDoc(expectedComment, method);
    }

    private static MethodSource<JavaClassSource>
    findMethod(JavaClassSource source, String methodName) {
        var method = source.getMethods().stream()
                .filter(m -> methodName.equals(m.getName()))
                .findFirst()
                .orElseThrow(() -> newIllegalStateException(
                        "Cannot find the method `%s`.", methodName)
                );
        return method;
    }

    private static void assertDoc(String expectedText, JavaDocCapableSource<?> source) {
        var javadoc = source.getJavaDoc();
        assertThat(javadoc.getFullText())
                .isEqualTo(expectedText);
    }
}
