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

package io.spine.tools.mc.java.gradle;

import io.spine.tools.gradle.Artifact;
import io.spine.tools.gradle.Dependency;
import io.spine.tools.gradle.DependencyVersions;
import io.spine.tools.gradle.ThirdPartyDependency;

import static io.spine.tools.gradle.Artifact.SPINE_TOOLS_GROUP;

final class Artifacts {

    private static final String JAR_EXTENSION = "jar";
    private static final String GRPC_GROUP = "io.grpc";
    private static final String GRPC_PLUGIN_NAME = "protoc-gen-grpc-java";
    private static final String MC_JAVA_NAME = "mc-java";
    private static final String EXECUTABLE_CLASSIFIER = "exe";

    static final String SPINE_PROTOC_PLUGIN_NAME = "mc-java-protoc";

    /**
     * The name of the Maven artifact of the Model Compiler Java Checks.
     */
    static final String MC_JAVA_CHECKS_ARTIFACT = "mc-java-checks";

    private static final DependencyVersions versions = DependencyVersions.loadFor("mc-java");

    /**
     * Prevents the utility class instantiation.
     */
    private Artifacts() {
    }

    static Artifact mcJavaChecks() {
        String version = mcJavaVersion();
        return Artifact.newBuilder()
                .useSpineToolsGroup()
                .setName(MC_JAVA_CHECKS_ARTIFACT)
                .setVersion(version)
                .build();
    }

    static Artifact gRpcProtocPlugin() {
        Dependency gRpcPlugin = new ThirdPartyDependency(GRPC_GROUP, GRPC_PLUGIN_NAME);
        return gRpcPlugin.withVersionFrom(versions);
    }

    static Artifact spineProtocPlugin() {
        String version = mcJavaVersion();
        return Artifact.newBuilder()
                .useSpineToolsGroup()
                .setName(SPINE_PROTOC_PLUGIN_NAME)
                .setVersion(version)
                .setClassifier(EXECUTABLE_CLASSIFIER)
                .setExtension(JAR_EXTENSION)
                .build();
    }

    public static String mcJavaVersion() {
        Dependency self = new ThirdPartyDependency(SPINE_TOOLS_GROUP, MC_JAVA_NAME);
        return versions.versionOf(self)
                       .orElseThrow(IllegalStateException::new);
    }
}
