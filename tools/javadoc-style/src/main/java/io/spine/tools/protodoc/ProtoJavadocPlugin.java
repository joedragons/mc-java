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
package io.spine.tools.protodoc;

import com.google.common.collect.ImmutableList;
import io.spine.tools.gradle.SpinePlugin;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Task;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static io.spine.tools.gradle.JavaTaskName.compileJava;
import static io.spine.tools.gradle.JavaTaskName.compileTestJava;
import static io.spine.tools.protodoc.JavadocPrettifierTaskName.formatProtoDoc;
import static io.spine.tools.protodoc.JavadocPrettifierTaskName.formatTestProtoDoc;
import static io.spine.tools.gradle.ProtobufTaskName.generateProto;
import static io.spine.tools.gradle.ProtobufTaskName.generateTestProto;
import static io.spine.tools.protodoc.Extension.getAbsoluteMainGenProtoDir;
import static io.spine.tools.protodoc.Extension.getAbsoluteTestGenProtoDir;
import static java.lang.String.format;

/**
 * The plugin, that formats Javadocs in sources generated from {@code .proto} files.
 *
 * <p>Does the following formatting:
 * <ul>
 *     <li>removes {@code <pre>} tags generated by Protobuf compiler;</li>
 *     <li>replaces a text in backticks by the text in {@code code} tag.</li>
 * </ul>
 *
 * <p>Configuration example:
 * <pre>{@code
 * protoJavadoc {
 *     mainGenProtoDir = "directory_with_main_sources"
 *     testGenProtoDir = "directory_with_test_sources"
 * }
 * }</pre>
 *
 * <p>All {@code .java} files in the specified directories (and subdirectories) will be formatted.
 * So, if the folders contain not only the sources generated basing on Protobuf definitions,
 * they will be formatted either.
 */
public class ProtoJavadocPlugin extends SpinePlugin {

    static final String PROTO_JAVADOC_EXTENSION_NAME = "protoJavadoc";

    @Override
    public void apply(Project project) {
        _debug().log("Adding the ProtoJavadocPlugin extension to the project.");
        project.getExtensions()
               .create(PROTO_JAVADOC_EXTENSION_NAME, Extension.class);

        Action<Task> mainAction = createAction(project, TaskType.MAIN);
        newTask(formatProtoDoc, mainAction)
                .insertBeforeTask(compileJava)
                .insertAfterTask(generateProto)
                .applyNowTo(project);

        Action<Task> testAction = createAction(project, TaskType.TEST);
        newTask(formatTestProtoDoc, testAction)
                .insertBeforeTask(compileTestJava)
                .insertAfterTask(generateTestProto)
                .applyNowTo(project);
    }

    private Action<Task> createAction(Project project, TaskType taskType) {
        return task -> formatJavadocs(project, taskType);
    }

    private void formatJavadocs(Project project, TaskType taskType) {
        String genProtoDir = taskType.getGenProtoDir(project);
        File file = new File(genProtoDir);
        if (!file.exists()) {
            _warn().log("Cannot perform formatting. Directory `%s` does not exist.", file);
            return;
        }

        JavadocFormatter formatter = new JavadocFormatter(
                ImmutableList.of(new BacktickFormatting(),
                                 new PreTagFormatting())
        );
        try {
            _debug().log("Starting Javadocs formatting in `%s`.", genProtoDir);
            Files.walkFileTree(file.toPath(), new FormattingFileVisitor(formatter));
        } catch (IOException e) {
            String errMsg = format("Failed to format the sources in `%s`.", genProtoDir);
            throw new IllegalStateException(errMsg, e);
        }
    }

    private enum TaskType {
        MAIN {
            @Override
            String getGenProtoDir(Project project) {
                return getAbsoluteMainGenProtoDir(project);
            }
        },
        TEST {
            @Override
            String getGenProtoDir(Project project) {
                return getAbsoluteTestGenProtoDir(project);
            }
        };

        abstract String getGenProtoDir(Project project);
    }
}
