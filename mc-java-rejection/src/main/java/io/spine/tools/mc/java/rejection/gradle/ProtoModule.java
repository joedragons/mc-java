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

import io.spine.tools.gradle.SourceSetName;
import org.gradle.api.Project;
import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.SourceSet;

import java.nio.file.Path;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;
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
     * Obtains a {@linkplain FileCollection collection of files} containing all the production
     * Protobuf sources defined in this module.
     *
     * @apiNote The returned collection is a live view on the files, i.e. as the generated
     *        directory is changing, the contents of the collection are mutated.
     */
    FileCollection protoSource() {
        return protoSource(SourceSetName.main.toString());
    }

    /**
     * Obtains a {@linkplain FileCollection collection of files} containing all the test Protobuf
     * sources defined in this module.
     *
     * @apiNote The returned collection is a live view on the files, i.e. as the generated
     *        directory is changing, the contents of the collection are mutated.
     */
    FileCollection testProtoSource() {
        return protoSource(SourceSetName.test.toString());
    }

    private FileCollection protoSource(String sourceSetName) {
        SourceSet sourceSet = sourceSet(project, sourceSetName);
        Optional<FileCollection> files = protoSource(sourceSet);
        FileCollection emptyCollection = project.getLayout().files();
        return files.orElse(emptyCollection);
    }

    private static Optional<FileCollection> protoSource(SourceSet sourceSet) {
        Object rawExtension =
                sourceSet.getExtensions()
                         .findByName(SourceSetName.proto.toString());
        if (rawExtension == null) {
            return Optional.empty();
        } else {
            FileCollection protoSet = (FileCollection) rawExtension;
            return Optional.of(protoSet);
        }
    }

    /**
     * Obtains a {@linkplain FileCollection collection of files} containing all the production
     * {@linkplain io.spine.base.RejectionThrowable rejections} generated in this module.
     *
     * @apiNote The returned collection is a live view on the files, i.e. as the generated
     *        directory is changing, the contents of the collection are mutated.
     */
    FileCollection compiledRejections() {
        Path targetDir = generatedRejectionsDir(project, SourceSetName.main);
        FileCollection files = project.fileTree(targetDir);
        return files;
    }

    /**
     * Obtains a {@linkplain FileCollection collection of files} containing all the test
     * {@linkplain io.spine.base.RejectionThrowable rejections} generated in this module.
     *
     * @apiNote The returned collection is a live view on the files, i.e. as the generated
     *        directory is changing, the contents of the collection are mutated.
     */
    FileCollection testCompiledRejections() {
        Path targetDir = generatedRejectionsDir(project, SourceSetName.test);
        FileCollection files = project.fileTree(targetDir);
        return files;
    }
}
