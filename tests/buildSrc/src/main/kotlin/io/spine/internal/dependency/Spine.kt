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

package io.spine.internal.dependency

import org.gradle.api.Project
import org.gradle.kotlin.dsl.extra

/**
 * Dependencies on Spine `base` modules.
 *
 * @constructor
 * Creates a new instance of `Spine` taking the `spineBaseVersion` from the given project's
 * extra properties.
 */
class Spine(p: Project) {

    val base = "io.spine:spine-base:${p.spineVersion}"
    val testlib = "io.spine.tools:spine-testlib:${p.spineVersion}"

    val toolBase = "io.spine.tools:spine-tool-base:${p.mcVersion}"
    val pluginBase = "io.spine.tools:spine-plugin-base:${p.mcVersion}"
    val pluginTestlib = "io.spine.tools:spine-plugin-testlib:${p.mcVersion}"
    val modelCompiler = "io.spine.tools:spine-model-compiler:${p.mcVersion}"

    private val Project.spineVersion: String
        get() = extra["spineBaseVersion"] as String
    private val Project.mcVersion: String
        get() = extra["mcVersion"] as String
}
