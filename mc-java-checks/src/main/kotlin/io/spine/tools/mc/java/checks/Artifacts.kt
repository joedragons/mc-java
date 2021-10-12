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
@file:JvmName("Artifacts")

package io.spine.tools.mc.java.checks

import io.spine.tools.gradle.Artifact
import io.spine.tools.gradle.Dependency
import io.spine.tools.gradle.DependencyVersions
import io.spine.tools.gradle.ThirdPartyDependency

/**
 * The name of the Maven artifact of the Model Compiler Java Checks.
 */
internal const val MC_JAVA_CHECKS_ARTIFACT = "mc-java-checks"

private val versions = DependencyVersions.loadFor(MC_JAVA_CHECKS_ARTIFACT)

/**
 * The Maven artifact containing the `mc-java-checks` module.
 */
@get:JvmName("mcJavaChecks")
internal val mcJavaChecks: Artifact by lazy {
    Artifact.newBuilder()
        .useSpineToolsGroup()
        .setName(MC_JAVA_CHECKS_ARTIFACT)
        .setVersion(mcJavaChecksVersion)
        .build()
}

/**
 * The version of the Model Compiler Java modules.
 *
 * This is the version of all the modules declared in this project.
 */
@get:JvmName("mcJavaChecksVersion")
internal val mcJavaChecksVersion: String by lazy {
    val self: Dependency = ThirdPartyDependency(Artifact.SPINE_TOOLS_GROUP, MC_JAVA_CHECKS_ARTIFACT)
    versions.versionOf(self)
        .orElseThrow { IllegalStateException() }
}


