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

package io.spine.tools.mc.java.gradle

import io.spine.tools.gradle.Artifact
import io.spine.tools.gradle.Artifact.SPINE_TOOLS_GROUP
import io.spine.tools.gradle.Dependency
import io.spine.tools.gradle.DependencyVersions
import io.spine.tools.gradle.ThirdPartyDependency

private const val JAR_EXTENSION = "jar"
private const val GRPC_GROUP = "io.grpc"
private const val GRPC_PLUGIN_NAME = "protoc-gen-grpc-java"
private const val MC_JAVA_NAME = "mc-java"
private const val EXECUTABLE_CLASSIFIER = "exe"

const val SPINE_PROTOC_PLUGIN_NAME = "mc-java-protoc"

/**
 * The name of the Maven artifact of the Model Compiler Java Checks.
 */
const val MC_JAVA_CHECKS_ARTIFACT = "mc-java-checks"

private val versions = DependencyVersions.loadFor("mc-java")

@get:JvmName("mcJavaChecks")
val mcJavaChecks: Artifact by lazy {
    Artifact.newBuilder()
        .useSpineToolsGroup()
        .setName(MC_JAVA_CHECKS_ARTIFACT)
        .setVersion(mcJavaVersion)
        .build()
}

@get:JvmName("gRpcProtocPlugin")
val gRpcProtocPlugin: Artifact by lazy {
    val gRpcPlugin: Dependency = ThirdPartyDependency(GRPC_GROUP, GRPC_PLUGIN_NAME)
    gRpcPlugin.withVersionFrom(versions)
}

@get:JvmName("spineProtocPlugin")
val spineProtocPlugin: Artifact by lazy {
    Artifact.newBuilder()
        .useSpineToolsGroup()
        .setName(SPINE_PROTOC_PLUGIN_NAME)
        .setVersion(mcJavaVersion)
        .setClassifier(EXECUTABLE_CLASSIFIER)
        .setExtension(JAR_EXTENSION)
        .build()
}

@get:JvmName("mcJavaVersion")
val mcJavaVersion: String by lazy {
    val self: Dependency = ThirdPartyDependency(SPINE_TOOLS_GROUP, MC_JAVA_NAME)
    versions.versionOf(self)
        .orElseThrow { IllegalStateException() }
}
