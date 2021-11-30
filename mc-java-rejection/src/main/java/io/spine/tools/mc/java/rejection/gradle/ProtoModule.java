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

import io.spine.tools.gradle.ProtobufDependencies;
import io.spine.tools.gradle.SourceSetName;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.gradle.api.Project;
import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.SourceSet;

import java.nio.file.Path;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.tools.gradle.ProtobufDependencies.sourceSetExtensionName;
import static io.spine.tools.gradle.project.Projects.sourceSet;
import static io.spine.tools.mc.java.gradle.Projects.generatedRejectionsDir;

/**
 * A source code module with Protobuf.
 *
 * <p>A module is a set of source code, generated artifacts and temporary files aligned in a certain
 * layout. In terms of Gradle, a module is all the contents of a Gradle project.
 *
 * <p>It is assumed that the model compiler plugin is applied to the Gradle project represented by
 * this module.
 */
final class ProtoModule {

    private final Project project;

    /**
     * Creates a new instance atop of the given Gradle project.
     */
    ProtoModule(Project project) {
        this.project = checkNotNull(project);
    }

    /**
     * Obtains files with the Protobuf sources defined in this module for the given source set.
     *
     * @param ssn
     *         the name of the source set for which to obtain the source code of rejections
     * @apiNote The returned collection is a live view on the files, i.e. as the generated
     *         directory is changing, the contents of the collection are mutated.
     */
    FileCollection protoSource(SourceSetName ssn) {
        @Nullable FileCollection protoFiles = protoFiles(ssn);
        return protoFiles != null
               ? protoFiles
               : project.getLayout().files() /* Empty collection */;
    }

    private @Nullable FileCollection protoFiles(SourceSetName ssn) {
        SourceSet sourceSet = sourceSet(project, ssn);
        @Nullable Object extension =
                sourceSet.getExtensions()
                         .findByName(sourceSetExtensionName());
        return (FileCollection) extension;
    }

    /**
     * Obtains rejection files generated in this module for the given source set.
     *
     * @param ssn
     *         the name of the source set for which to obtain the source code of rejections
     * @apiNote The returned collection is a live view on the files, i.e. as the generated
     *         directory is changing, the contents of the collection is updated.
     */
    FileCollection generatedRejections(SourceSetName ssn) {
        Path targetDir = generatedRejectionsDir(project, ssn);
        FileCollection files = project.fileTree(targetDir);
        return files;
    }
}
