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

import com.google.common.collect.ImmutableList;
import io.spine.code.proto.FileSet;
import io.spine.tools.gradle.ProtoFiles;
import io.spine.tools.gradle.SourceSetName;
import io.spine.tools.gradle.task.GradleTask;
import io.spine.tools.gradle.task.TaskName;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.file.FileCollection;

import java.util.function.Supplier;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static io.spine.tools.gradle.project.Projects.getSourceSetNames;
import static io.spine.tools.gradle.task.JavaTaskName.compileJava;
import static io.spine.tools.mc.java.gradle.McJavaTaskName.generateRejections;
import static io.spine.tools.mc.java.gradle.McJavaTaskName.mergeDescriptorSet;
import static io.spine.tools.mc.java.gradle.Projects.generatedRejectionsDir;
import static io.spine.tools.mc.java.gradle.Projects.protoDir;

/**
 * Plugin which generates Rejections declared in {@code rejections.proto} files.
 *
 * <p>Uses generated proto descriptors.
 *
 * <p>Logs a warning if there are no protobuf descriptors generated.
 */
public final class RejectionGenPlugin implements Plugin<Project> {

    /**
     * Applies the plug-in to a project.
     *
     * <p>Adds {@code :generateRejections} tasks for all source sets of the project.
     *
     * <p>Tasks depend on corresponding {@code :generateProto} tasks and are executed
     * before corresponding {@code :compileJava} tasks.
     */
    @Override
    public void apply(Project project) {
        Helper helper = new Helper(project);
        helper.configure();
        project.getLogger().info(
                "Rejection generation plugin initialized with tasks: `{}`.",
                helper.tasks
        );
    }

    /**
     * Creates tasks and applies them to the project.
     */
    private static final class Helper {

        private final Project project;
        private final ProtoModule module;

        /** Configured tasks are {@code null} until {@link #configure()} is called. */
        private @MonotonicNonNull ImmutableList<GradleTask> tasks;

        private Helper(Project project) {
            this.project = project;
            this.module = new ProtoModule(project);
        }

        private void configure() {
            this.tasks = getSourceSetNames(project).stream()
                    .map(this::createTask)
                    .collect(toImmutableList());
        }

        private GradleTask createTask(SourceSetName ssn) {
            Action<Task> action = createAction(ssn);
            return createTask(action, ssn);
        }

        private Action<Task> createAction(SourceSetName ssn) {
            Supplier<FileSet> protoFiles = ProtoFiles.collect(project, ssn);
            Supplier<String> rejectionsDir = () -> generatedRejectionsDir(project, ssn).toString();
            Supplier<String> protoDir = () -> protoDir(project, ssn).toString();
            return new RejectionGenAction(project, protoFiles, rejectionsDir, protoDir);
        }

        private GradleTask createTask(Action<Task> action, SourceSetName ssn) {
            TaskName rejections = generateRejections(ssn);
            TaskName mergeTask = mergeDescriptorSet(ssn);
            TaskName compileTask = compileJava(ssn);
            FileCollection inputFiles = module.protoSource(ssn);
            FileCollection outputFiles = module.generatedRejections(ssn);
            GradleTask task = GradleTask.newBuilder(rejections, action)
                    .insertBeforeTask(compileTask)
                    .insertAfterTask(mergeTask)
                    .withInputFiles(inputFiles)
                    .withOutputFiles(outputFiles)
                    .applyNowTo(project);
            return task;
        }
    }
}
