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

@file:JvmName("Projects")

package io.spine.tools.mc.java.gradle

import io.spine.tools.java.fs.DefaultJavaPaths
import io.spine.tools.mc.gradle.modelCompiler
import java.nio.file.Path
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.getByType

/**
 * Obtains options of Model Compiler for Java.
 */
public val Project.mcJava: McJavaOptions
    get() = (modelCompiler as ExtensionAware).extensions.getByType()

private val Project.defaultPaths: DefaultJavaPaths
    get() = DefaultJavaPaths.at(projectDir.toPath())

/**
 * Obtains the directory containing proto source code of the specified source set.
 */
public fun Project.protoDir(sourceSet: String): Path {
    requireValidSourceSetName(sourceSet)
    return defaultPaths.src().directory().resolve(sourceSet)
}

/**
 * Obtains the path to the generated code directory of this project.
 */
public val Project.generatedDir: Path
    get() = defaultPaths.generated().path()

/**
 * Obtains the directory containing generated Java source code for the specified source set.
 */
public fun Project.generatedJavaDir(sourceSet: String): Path {
    requireValidSourceSetName(sourceSet)
    val sourceSetDir = generatedDir.resolve(sourceSet)
    val result = sourceSetDir.resolve("java")
    return result
}

private fun requireValidSourceSetName(sourceSet: String) {
    require(sourceSet.isNotEmpty())
    require(sourceSet.isNotBlank())
}
