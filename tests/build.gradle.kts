/*
 * Copyright 2022, TeamDev. All rights reserved.
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

@file:Suppress("RemoveRedundantQualifierName") // To prevent IDEA replacing FQN imports.

import io.spine.internal.dependency.ErrorProne
import io.spine.internal.dependency.JUnit
import io.spine.internal.dependency.Jackson
import io.spine.internal.dependency.Protobuf
import io.spine.internal.dependency.Truth
import io.spine.internal.gradle.applyStandard
import io.spine.internal.gradle.excludeProtobufLite
import io.spine.internal.gradle.forceVersions
import io.spine.internal.gradle.javac.configureErrorProne
import io.spine.internal.gradle.javac.configureJavac
import io.spine.internal.gradle.publish.PublishingRepos.gitHub
import io.spine.internal.gradle.testing.configureLogging

buildscript {

    val baseRoot = "${rootDir}/.."
    val versionGradle = "${baseRoot}/version.gradle.kts"
    apply(from = versionGradle)

    io.spine.internal.gradle.doApplyStandard(repositories)

    val mcJavaVersion: String by extra

    dependencies {
        classpath(io.spine.internal.dependency.Guava.lib)
        classpath(io.spine.internal.dependency.Protobuf.GradlePlugin.lib) {
            exclude(group = "com.google.guava")
        }
        classpath(io.spine.internal.dependency.ErrorProne.GradlePlugin.lib) {
            exclude(group = "com.google.guava")
        }
        classpath("io.spine.tools:spine-mc-java-plugins:${mcJavaVersion}:all")
    }

    val baseVersion: String by extra
    val timeVersion: String by extra
    val toolBaseVersion: String by extra
    with(configurations) {
        io.spine.internal.gradle.doForceVersions(this)
        all {
            resolutionStrategy {
                force(
                    "io.spine:spine-base:$baseVersion",
                    "io.spine:spine-time:$timeVersion",
                    "io.spine.tools:spine-tool-base:$toolBaseVersion",
                    "io.spine.tools:spine-plugin-base:$toolBaseVersion",
                    io.spine.internal.dependency.Jackson.core,
                    io.spine.internal.dependency.Jackson.moduleKotlin,
                    io.spine.internal.dependency.Jackson.databind,
                    io.spine.internal.dependency.Jackson.bom,
                    io.spine.internal.dependency.Jackson.annotations,
                    io.spine.internal.dependency.Jackson.dataformatYaml
                )
            }
        }
    }
}

@Suppress("RemoveRedundantQualifierName") // Cannot use imports here.
plugins {
    java
    idea
    id(io.spine.internal.dependency.Protobuf.GradlePlugin.id)
    id(io.spine.internal.dependency.ErrorProne.GradlePlugin.id)
}

val baseRoot = "$rootDir/.."

allprojects {
    apply(from = "$baseRoot/version.gradle.kts")
    apply(plugin = "java")

    repositories {
        applyStandard()
        gitHub("base")
        gitHub("tool-base")
        gitHub("model-compiler")
        mavenLocal()
    }

    group = "io.spine.tools.tests"
    version = extra["versionToPublish"]!!

    val baseVersion: String by extra
    val toolBaseVersion: String by extra
    val timeVersion: String by extra
    configurations {
        forceVersions()
        excludeProtobufLite()
        all {
            exclude(group = "io.spine.validation", module = "spine-validation-runtime")
            resolutionStrategy {
                force(
                    "io.spine:spine-base:$baseVersion",
                    "io.spine:spine-time:$timeVersion",
                    "io.spine.tools:spine-testlib:$baseVersion",
                    "io.spine.tools:spine-tool-base:$toolBaseVersion",
                    "io.spine.tools:spine-plugin-base:$toolBaseVersion",
                    Jackson.core,
                    Jackson.moduleKotlin,
                    Jackson.databind,
                    Jackson.bom,
                    Jackson.annotations,
                    Jackson.dataformatYaml
                )
            }
        }
    }
}

subprojects {

    apply {
        plugin(ErrorProne.GradlePlugin.id)
        plugin(Protobuf.GradlePlugin.id)
        plugin("io.spine.mc-java")
        plugin("idea")
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11

        tasks.withType<JavaCompile>().configureEach {
            configureJavac()
            configureErrorProne()
        }
    }

    val baseVersion: String by extra
    dependencies {
        errorprone(ErrorProne.core)
        errorproneJavac(ErrorProne.javacPlugin)
        ErrorProne.annotations.forEach { compileOnly(it) }
        implementation("io.spine:spine-base:$baseVersion")
        testImplementation("io.spine.tools:spine-testlib:$baseVersion")
        Truth.libs.forEach { testImplementation(it) }
        testRuntimeOnly(JUnit.runner)
    }


    idea.module {
        generatedSourceDirs.addAll(files(
                "$projectDir/generated/main/java",
                "$projectDir/generated/main/spine",
                "$projectDir/generated/test/java",
                "$projectDir/generated/test/spine"
        ))
    }

    sourceSets {
        main {
            proto.srcDir("$projectDir/src/main/proto")
            java.srcDirs("$projectDir/generated/main/java",
                         "$projectDir/generated/main/spine",
                         "$projectDir/src/main/java")
            resources.srcDir("$projectDir/generated/main/resources")
        }

        test {
            proto.srcDir("$projectDir/src/test/proto")
            java.srcDirs("$projectDir/generated/test/java",
                         "$projectDir/generated/test/spine",
                         "$projectDir/src/test/java")
            resources.srcDir("$projectDir/generated/test/resources")
        }
    }

    tasks.test {
        useJUnitPlatform {
            includeEngines("junit-jupiter")
        }

        include("**/*Test.class")
        configureLogging()
    }

    //TODO:2021-07-22:alexander.yevsyukov: Turn to WARN and investigate duplicates.
    // see https://github.com/SpineEventEngine/base/issues/657
    tasks.processTestResources.get().duplicatesStrategy = DuplicatesStrategy.INCLUDE
}
