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

package io.spine.tools.mc.java.annotation.gradle;

import com.google.common.collect.ImmutableSet;
import io.spine.code.java.ClassName;
import io.spine.logging.Logging;
import io.spine.tools.mc.java.annotation.mark.AnnotatorFactory;
import io.spine.tools.mc.java.annotation.mark.DefaultAnnotatorFactory;
import io.spine.tools.mc.java.annotation.mark.ModuleAnnotator;
import io.spine.tools.mc.java.gradle.CodeGenAnnotations;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Task;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import static io.spine.tools.mc.java.annotation.mark.ApiOption.beta;
import static io.spine.tools.mc.java.annotation.mark.ApiOption.experimental;
import static io.spine.tools.mc.java.annotation.mark.ApiOption.internal;
import static io.spine.tools.mc.java.annotation.mark.ApiOption.spi;
import static io.spine.tools.mc.java.annotation.mark.ModuleAnnotator.translate;
import static io.spine.tools.mc.java.gradle.McJavaOptions.descriptorSetFileOf;
import static io.spine.tools.mc.java.gradle.McJavaOptions.getCodeGenAnnotations;
import static io.spine.tools.mc.java.gradle.McJavaOptions.getGeneratedMainGrpcDir;
import static io.spine.tools.mc.java.gradle.McJavaOptions.getGeneratedMainJavaDir;
import static io.spine.tools.mc.java.gradle.McJavaOptions.getGeneratedTestGrpcDir;
import static io.spine.tools.mc.java.gradle.McJavaOptions.getGeneratedTestJavaDir;
import static io.spine.tools.mc.java.gradle.McJavaOptions.getInternalClassPatterns;
import static io.spine.tools.mc.java.gradle.McJavaOptions.getInternalMethodNames;

/**
 * A task action which performs generated code annotation.
 */
final class AnnotationAction implements Action<Task>, Logging {

    private final boolean mainCode;

    /**
     * Creates a new action instance.
     *
     * @param mainCode
     *         if {@code true} the production code will be annotated,
     *         otherwise the action will annotate the code of tests
     */
    AnnotationAction(boolean mainCode) {
        this.mainCode = mainCode;
    }

    @Override
    public void execute(Task task) {
        Project project = task.getProject();
        File descriptorSetFile = descriptorSetFileOf(project, mainCode);
        if (!descriptorSetFile.exists()) {
            logMissing(descriptorSetFile);
            return;
        }
        ModuleAnnotator annotator = createAnnotator(project);
        annotator.annotate();
    }

    private ModuleAnnotator createAnnotator(Project project) {
        AnnotatorFactory annotatorFactory = createAnnotationFactory(project);
        CodeGenAnnotations annotations = getCodeGenAnnotations(project);
        ClassName internalClassName = annotations.internalClassName();
        ImmutableSet<String> internalClassPatterns = getInternalClassPatterns(project);
        ImmutableSet<String> internalMethodNames = getInternalMethodNames(project);
        return ModuleAnnotator.newBuilder()
                .setAnnotatorFactory(annotatorFactory)
                .add(translate(spi()).as(annotations.spiClassName()))
                .add(translate(beta()).as(annotations.betaClassName()))
                .add(translate(experimental()).as(annotations.experimentalClassName()))
                .add(translate(internal()).as(internalClassName))
                .setInternalPatterns(internalClassPatterns)
                .setInternalMethodNames(internalMethodNames)
                .setInternalAnnotation(internalClassName)
                .build();
    }

    @NonNull
    private AnnotatorFactory createAnnotationFactory(Project project) {
        File descriptorSetFile = descriptorSetFileOf(project, mainCode);
        Path generatedJavaPath = Paths.get(generatedJavaDir(project));
        Path generatedGrpcPath = Paths.get(generatedGrpcDir(project));
        AnnotatorFactory annotatorFactory = DefaultAnnotatorFactory
                .newInstance(descriptorSetFile, generatedJavaPath, generatedGrpcPath);
        return annotatorFactory;
    }

    private String generatedJavaDir(Project project) {
        return mainCode
               ? getGeneratedMainJavaDir(project)
               : getGeneratedTestJavaDir(project);
    }

    private String generatedGrpcDir(Project project) {
        return mainCode
               ? getGeneratedMainGrpcDir(project)
               : getGeneratedTestGrpcDir(project);
    }

    private void logMissing(File descriptorSetFile) {
        _debug().log(
                "Missing descriptor set file `%s`.%n" +
                        "Please enable descriptor set generation.%n" +
                        "See: " +
                        "https://github.com/google/protobuf-gradle-plugin/blob/master/README.md" +
                        "#generate-descriptor-set-files",
                descriptorSetFile.getPath()
        );
    }
}
