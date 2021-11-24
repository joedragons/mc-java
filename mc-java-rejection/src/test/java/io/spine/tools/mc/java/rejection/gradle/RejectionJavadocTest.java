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
import io.spine.tools.gradle.testing.GradleProject;
import io.spine.tools.java.code.BuilderSpec;
import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaDocCapableSource;
import org.jboss.forge.roaster.model.source.JavaDocSource;
import org.jboss.forge.roaster.model.source.MethodSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.tools.gradle.task.JavaTaskName.compileJava;
import static io.spine.tools.mc.java.rejection.gradle.TestEnv.expectedBuilderClassComment;
import static io.spine.tools.mc.java.rejection.gradle.TestEnv.expectedClassComment;
import static io.spine.tools.mc.java.rejection.gradle.TestEnv.expectedFirstFieldComment;
import static io.spine.tools.mc.java.rejection.gradle.TestEnv.expectedSecondFieldComment;
import static io.spine.tools.mc.java.rejection.gradle.TestEnv.newProjectWithRejectionsJavadoc;
import static io.spine.tools.mc.java.rejection.gradle.TestEnv.rejectionsJavadocThrowableSource;
import static io.spine.util.Exceptions.newIllegalStateException;

@DisplayName("Rejection Javadoc ")
public class RejectionJavadocTest {

    private File testProjectDir;

    @BeforeEach
    void setUp() {
        testProjectDir = TempDir.forClass(getClass());
    }


    @Test
    @DisplayName("generate rejection Javadoc")
    void generateRejectionJavadoc() throws IOException {
        GradleProject project = newProjectWithRejectionsJavadoc(testProjectDir);
        project.executeTask(compileJava);
        String projectAbsolutePath = testProjectDir.getAbsolutePath();
        File generatedFile = new File(projectAbsolutePath + rejectionsJavadocThrowableSource());
        JavaClassSource generatedSource = Roaster.parse(JavaClassSource.class, generatedFile);
        assertRejectionJavadoc(generatedSource);
        assertBuilderJavadoc(
                (JavaClassSource) generatedSource.getNestedType(SimpleClassName.ofBuilder().value())
        );
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
        MethodSource<JavaClassSource> method = findMethod(source, methodName);
        assertDoc(expectedComment, method);
    }

    private static MethodSource<JavaClassSource>
    findMethod(JavaClassSource source, String methodName) {
        MethodSource<JavaClassSource> method =
                source.getMethods()
                        .stream()
                        .filter(m -> methodName.equals(m.getName()))
                        .findFirst()
                        .orElseThrow(() -> newIllegalStateException(
                                "Cannot find the method `%s`.", methodName)
                        );
        return method;
    }

    private static void assertDoc(String expectedText, JavaDocCapableSource<?> source) {
        JavaDocSource<?> javadoc = source.getJavaDoc();
        assertThat(javadoc.getFullText())
                .isEqualTo(expectedText);
    }
}
